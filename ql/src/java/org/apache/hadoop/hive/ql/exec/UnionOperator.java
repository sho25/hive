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
name|io
operator|.
name|Serializable
import|;
end_import

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
name|UnionDesc
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
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
comment|/**  * Union Operator Just forwards. Doesn't do anything itself.  **/
end_comment

begin_class
specifier|public
class|class
name|UnionOperator
extends|extends
name|Operator
argument_list|<
name|UnionDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|StructObjectInspector
index|[]
name|parentObjInspectors
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
index|[]
name|parentFields
decl_stmt|;
name|ReturnObjectInspectorResolver
index|[]
name|columnTypeResolvers
decl_stmt|;
name|boolean
index|[]
name|needsTransform
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|outputRow
decl_stmt|;
comment|/**    * UnionOperator will transform the input rows if the inputObjInspectors from    * different parents are different. If one parent has exactly the same    * ObjectInspector as the output ObjectInspector, then we don't need to do    * transformation for that parent. This information is recorded in    * needsTransform[].    */
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
name|int
name|parents
init|=
name|parentOperators
operator|.
name|size
argument_list|()
decl_stmt|;
name|parentObjInspectors
operator|=
operator|new
name|StructObjectInspector
index|[
name|parents
index|]
expr_stmt|;
name|parentFields
operator|=
operator|new
name|List
index|[
name|parents
index|]
expr_stmt|;
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|parents
condition|;
name|p
operator|++
control|)
block|{
name|parentObjInspectors
index|[
name|p
index|]
operator|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|p
index|]
expr_stmt|;
name|parentFields
index|[
name|p
index|]
operator|=
name|parentObjInspectors
index|[
name|p
index|]
operator|.
name|getAllStructFieldRefs
argument_list|()
expr_stmt|;
block|}
comment|// Get columnNames from the first parent
name|int
name|columns
init|=
name|parentFields
index|[
literal|0
index|]
operator|.
name|size
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columns
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
condition|;
name|c
operator|++
control|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|parentFields
index|[
literal|0
index|]
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Get outputFieldOIs
name|columnTypeResolvers
operator|=
operator|new
name|ReturnObjectInspectorResolver
index|[
name|columns
index|]
expr_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
condition|;
name|c
operator|++
control|)
block|{
name|columnTypeResolvers
index|[
name|c
index|]
operator|=
operator|new
name|ReturnObjectInspectorResolver
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|parents
condition|;
name|p
operator|++
control|)
block|{
assert|assert
operator|(
name|parentFields
index|[
name|p
index|]
operator|.
name|size
argument_list|()
operator|==
name|columns
operator|)
assert|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
condition|;
name|c
operator|++
control|)
block|{
name|columnTypeResolvers
index|[
name|c
index|]
operator|.
name|update
argument_list|(
name|parentFields
index|[
name|p
index|]
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|outputFieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columns
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
condition|;
name|c
operator|++
control|)
block|{
name|outputFieldOIs
operator|.
name|add
argument_list|(
name|columnTypeResolvers
index|[
name|c
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// create output row ObjectInspector
name|outputObjInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|outputFieldOIs
argument_list|)
expr_stmt|;
name|outputRow
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|columns
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
condition|;
name|c
operator|++
control|)
block|{
name|outputRow
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// whether we need to do transformation for each parent
name|needsTransform
operator|=
operator|new
name|boolean
index|[
name|parents
index|]
expr_stmt|;
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|parents
condition|;
name|p
operator|++
control|)
block|{
comment|// Testing using != is good enough, because we use ObjectInspectorFactory
comment|// to
comment|// create ObjectInspectors.
name|needsTransform
index|[
name|p
index|]
operator|=
operator|(
name|inputObjInspectors
index|[
name|p
index|]
operator|!=
name|outputObjInspector
operator|)
expr_stmt|;
if|if
condition|(
name|needsTransform
index|[
name|p
index|]
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Union Operator needs to transform row from parent["
operator|+
name|p
operator|+
literal|"] from "
operator|+
name|inputObjInspectors
index|[
name|p
index|]
operator|+
literal|" to "
operator|+
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
block|}
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
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
name|parentObjInspectors
index|[
name|tag
index|]
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|parentFields
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|needsTransform
index|[
name|tag
index|]
condition|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
name|c
operator|++
control|)
block|{
name|outputRow
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|columnTypeResolvers
index|[
name|c
index|]
operator|.
name|convertIfNecessary
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|c
argument_list|)
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|forward
argument_list|(
name|outputRow
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|forward
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"UNION"
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|UNION
return|;
block|}
block|}
end_class

end_unit

