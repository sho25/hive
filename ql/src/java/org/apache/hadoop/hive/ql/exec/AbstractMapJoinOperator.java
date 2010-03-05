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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|conf
operator|.
name|HiveConf
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
name|exec
operator|.
name|MapJoinOperator
operator|.
name|MapJoinObjectCtx
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
name|exec
operator|.
name|persistence
operator|.
name|HashMapWrapper
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
name|exec
operator|.
name|persistence
operator|.
name|MapJoinObjectKey
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
name|exec
operator|.
name|persistence
operator|.
name|MapJoinObjectValue
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
name|exec
operator|.
name|persistence
operator|.
name|RowContainer
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
name|MapJoinDesc
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
name|TableDesc
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
name|SerDe
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
name|SerDeException
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
name|ObjectInspectorUtils
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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractMapJoinOperator
parameter_list|<
name|T
extends|extends
name|MapJoinDesc
parameter_list|>
extends|extends
name|CommonJoinOperator
argument_list|<
name|T
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
comment|/**    * The expressions for join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
name|joinKeys
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinKeysObjectInspectors
decl_stmt|;
comment|/**    * The standard ObjectInspectors for the join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinKeysStandardObjectInspectors
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|posBigTable
init|=
operator|-
literal|1
decl_stmt|;
comment|// one of the tables that is not in memory
specifier|transient
name|int
name|mapJoinRowsKey
decl_stmt|;
comment|// rows for a given key
specifier|protected
specifier|transient
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|emptyList
init|=
literal|null
decl_stmt|;
specifier|transient
name|int
name|numMapRowsRead
decl_stmt|;
specifier|private
specifier|static
specifier|final
specifier|transient
name|String
index|[]
name|FATAL_ERR_MSG
init|=
block|{
literal|null
block|,
comment|// counter value 0 means no error
literal|"Mapside join size exceeds hive.mapjoin.maxsize. "
operator|+
literal|"Please increase that or remove the mapjoin hint."
block|}
decl_stmt|;
specifier|transient
name|boolean
name|firstRow
decl_stmt|;
specifier|transient
name|int
name|heartbeatInterval
decl_stmt|;
specifier|public
name|AbstractMapJoinOperator
parameter_list|()
block|{   }
specifier|public
name|AbstractMapJoinOperator
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mjop
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|CommonJoinOperator
operator|)
name|mjop
argument_list|)
expr_stmt|;
block|}
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
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|numMapRowsRead
operator|=
literal|0
expr_stmt|;
name|firstRow
operator|=
literal|true
expr_stmt|;
name|heartbeatInterval
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESENDHEARTBEAT
argument_list|)
expr_stmt|;
name|joinKeys
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|populateJoinKeyValue
argument_list|(
name|joinKeys
argument_list|,
name|conf
operator|.
name|getKeys
argument_list|()
argument_list|)
expr_stmt|;
name|joinKeysObjectInspectors
operator|=
name|getObjectInspectorsFromEvaluators
argument_list|(
name|joinKeys
argument_list|,
name|inputObjInspectors
argument_list|)
expr_stmt|;
name|joinKeysStandardObjectInspectors
operator|=
name|getStandardObjectInspectors
argument_list|(
name|joinKeysObjectInspectors
argument_list|)
expr_stmt|;
comment|// all other tables are small, and are cached in the hash table
name|posBigTable
operator|=
name|conf
operator|.
name|getPosBigTable
argument_list|()
expr_stmt|;
name|emptyList
operator|=
operator|new
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
name|RowContainer
name|bigPosRC
init|=
name|getRowContainer
argument_list|(
name|hconf
argument_list|,
operator|(
name|byte
operator|)
name|posBigTable
argument_list|,
name|order
index|[
name|posBigTable
index|]
argument_list|,
name|joinCacheSize
argument_list|)
decl_stmt|;
name|storage
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|posBigTable
argument_list|,
name|bigPosRC
argument_list|)
expr_stmt|;
name|mapJoinRowsKey
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINROWSIZE
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|structFields
init|=
operator|(
operator|(
name|StructObjectInspector
operator|)
name|outputObjInspector
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|structFields
operator|.
name|size
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Byte
name|alias
range|:
name|order
control|)
block|{
name|int
name|sz
init|=
name|conf
operator|.
name|getExprs
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|retained
init|=
name|conf
operator|.
name|getRetainList
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|int
name|pos
init|=
name|retained
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|structFieldObjectInspectors
operator|.
name|add
argument_list|(
name|structFields
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|outputObjInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|,
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
block|}
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|fatalErrorMessage
parameter_list|(
name|StringBuilder
name|errMsg
parameter_list|,
name|long
name|counterCode
parameter_list|)
block|{
name|errMsg
operator|.
name|append
argument_list|(
literal|"Operator "
operator|+
name|getOperatorId
argument_list|()
operator|+
literal|" (id="
operator|+
name|id
operator|+
literal|"): "
operator|+
name|FATAL_ERR_MSG
index|[
operator|(
name|int
operator|)
name|counterCode
index|]
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|reportProgress
parameter_list|()
block|{
comment|// Send some status periodically
name|numMapRowsRead
operator|++
expr_stmt|;
if|if
condition|(
operator|(
operator|(
name|numMapRowsRead
operator|%
name|heartbeatInterval
operator|)
operator|==
literal|0
operator|)
operator|&&
operator|(
name|reporter
operator|!=
literal|null
operator|)
condition|)
block|{
name|reporter
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
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
name|MAPJOIN
return|;
block|}
block|}
end_class

end_unit

