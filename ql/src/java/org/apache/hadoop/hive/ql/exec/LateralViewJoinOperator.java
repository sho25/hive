begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|LateralViewJoinDesc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|api
operator|.
name|OperatorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * The lateral view join operator is used for FROM src LATERAL VIEW udtf()...  * This operator was implemented with the following operator DAG in mind.  *  * For a query such as  *  * SELECT pageid, adid.* FROM example_table LATERAL VIEW explode(adid_list) AS  * adid  *  * The top of the operator DAG will look similar to  *  *            [Table Scan]  *                |  *       [Lateral View Forward]  *              /   \  *   [Select](*)    [Select](adid_list)  *            |      |  *            |     [UDTF] (explode)  *            \     /  *      [Lateral View Join]  *               |  *               |  *      [Select] (pageid, adid.*)  *               |  *              ....  *  * Rows from the table scan operator are first to a lateral view forward  * operator that just forwards the row and marks the start of a LV. The  * select operator on the left picks all the columns while the select operator  * on the right picks only the columns needed by the UDTF.  *  * The output of select in the left branch and output of the UDTF in the right  * branch are then sent to the lateral view join (LVJ). In most cases, the UDTF  * will generate> 1 row for every row received from the TS, while the left  * select operator will generate only one. For each row output from the TS, the  * LVJ outputs all possible rows that can be created by joining the row from the  * left select and one of the rows output from the UDTF.  *  * Additional lateral views can be supported by adding a similar DAG after the  * previous LVJ operator.  */
end_comment

begin_class
specifier|public
class|class
name|LateralViewJoinOperator
extends|extends
name|Operator
argument_list|<
name|LateralViewJoinDesc
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// The expected tags from the parent operators. See processOp() before
comment|// changing the tags.
specifier|static
specifier|final
name|int
name|SELECT_TAG
init|=
literal|0
decl_stmt|;
specifier|static
specifier|final
name|int
name|UDTF_TAG
init|=
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|conf
operator|.
name|getOutputInternalColNames
argument_list|()
decl_stmt|;
comment|// The output of the lateral view join will be the columns from the select
comment|// parent, followed by the column from the UDTF parent
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|SELECT_TAG
index|]
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|sfs
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|sf
range|:
name|sfs
control|)
block|{
name|ois
operator|.
name|add
argument_list|(
name|sf
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|soi
operator|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|UDTF_TAG
index|]
expr_stmt|;
name|sfs
operator|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
expr_stmt|;
for|for
control|(
name|StructField
name|sf
range|:
name|sfs
control|)
block|{
name|ois
operator|.
name|add
argument_list|(
name|sf
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|outputObjInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|ois
argument_list|)
expr_stmt|;
comment|// Initialize the rest of the operator DAG
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
comment|// acc is short for accumulator. It's used to build the row before forwarding
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|acc
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
comment|// selectObjs hold the row from the select op, until receiving a row from
comment|// the udtf op
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|selectObjs
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * An important assumption for processOp() is that for a given row from the    * TS, the LVJ will first get the row from the left select operator, followed    * by all the corresponding rows from the UDTF operator. And so on.    */
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|tag
operator|==
name|SELECT_TAG
condition|)
block|{
name|selectObjs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|selectObjs
operator|.
name|addAll
argument_list|(
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tag
operator|==
name|UDTF_TAG
condition|)
block|{
name|acc
operator|.
name|clear
argument_list|()
expr_stmt|;
name|acc
operator|.
name|addAll
argument_list|(
name|selectObjs
argument_list|)
expr_stmt|;
name|acc
operator|.
name|addAll
argument_list|(
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|forward
argument_list|(
name|acc
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Invalid tag"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"LVJ"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|LATERALVIEWJOIN
return|;
block|}
block|}
end_class

end_unit

