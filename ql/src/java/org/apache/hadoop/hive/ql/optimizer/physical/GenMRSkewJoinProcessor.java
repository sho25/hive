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
name|optimizer
operator|.
name|physical
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|io
operator|.
name|UnsupportedEncodingException
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|ConditionalTask
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
name|JoinOperator
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
name|Operator
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
name|OperatorFactory
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
name|RowSchema
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
name|TableScanOperator
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
name|Task
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
name|TaskFactory
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
name|Utilities
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
name|io
operator|.
name|HiveInputFormat
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
name|parse
operator|.
name|ParseContext
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
name|parse
operator|.
name|SemanticException
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
name|ConditionalResolverSkewJoin
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
name|ConditionalWork
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
name|ExprNodeColumnDesc
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
name|ExprNodeDesc
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
name|FetchWork
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
name|JoinDesc
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
name|MapredLocalWork
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
name|MapredWork
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
name|OperatorDesc
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
name|PartitionDesc
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
name|PlanUtils
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
name|ql
operator|.
name|plan
operator|.
name|TableScanDesc
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * GenMRSkewJoinProcessor.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|GenMRSkewJoinProcessor
block|{
specifier|private
name|GenMRSkewJoinProcessor
parameter_list|()
block|{
comment|// prevent instantiation
block|}
comment|/**    * Create tasks for processing skew joins. The idea is (HIVE-964) to use    * separated jobs and map-joins to handle skew joins.    *<p>    *<ul>    *<li>    * Number of mr jobs to handle skew keys is the number of table minus 1 (we    * can stream the last table, so big keys in the last table will not be a    * problem).    *<li>    * At runtime in Join, we output big keys in one table into one corresponding    * directories, and all same keys in other tables into different dirs(one for    * each table). The directories will look like:    *<ul>    *<li>    * dir-T1-bigkeys(containing big keys in T1), dir-T2-keys(containing keys    * which is big in T1),dir-T3-keys(containing keys which is big in T1), ...    *<li>    * dir-T1-keys(containing keys which is big in T2), dir-T2-bigkeys(containing    * big keys in T2),dir-T3-keys(containing keys which is big in T2), ...    *<li>    * dir-T1-keys(containing keys which is big in T3), dir-T2-keys(containing big    * keys in T3),dir-T3-bigkeys(containing keys which is big in T3), ... .....    *</ul>    *</ul>    * For each table, we launch one mapjoin job, taking the directory containing    * big keys in this table and corresponding dirs in other tables as input.    * (Actally one job for one row in the above.)    *    *<p>    * For more discussions, please check    * https://issues.apache.org/jira/browse/HIVE-964.    *    */
specifier|public
specifier|static
name|void
name|processSkewJoin
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// We are trying to adding map joins to handle skew keys, and map join right
comment|// now does not work with outer joins
if|if
condition|(
operator|!
name|GenMRSkewJoinProcessor
operator|.
name|skewJoinEnabled
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
name|joinOp
argument_list|)
condition|)
block|{
return|return;
block|}
name|String
name|baseTmpDir
init|=
name|parseCtx
operator|.
name|getContext
argument_list|()
operator|.
name|getMRTmpFileURI
argument_list|()
decl_stmt|;
name|JoinDesc
name|joinDescriptor
init|=
name|joinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|joinValues
init|=
name|joinDescriptor
operator|.
name|getExprs
argument_list|()
decl_stmt|;
name|int
name|numAliases
init|=
name|joinValues
operator|.
name|size
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|bigKeysDirMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|smallKeysDirMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|skewJoinJobResultsDir
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Byte
index|[]
name|tags
init|=
name|joinDescriptor
operator|.
name|getTagOrder
argument_list|()
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
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|Byte
name|alias
init|=
name|tags
index|[
name|i
index|]
decl_stmt|;
name|String
name|bigKeysDir
init|=
name|getBigKeysDir
argument_list|(
name|baseTmpDir
argument_list|,
name|alias
argument_list|)
decl_stmt|;
name|bigKeysDirMap
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|bigKeysDir
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|smallKeysMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|smallKeysDirMap
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|smallKeysMap
argument_list|)
expr_stmt|;
for|for
control|(
name|Byte
name|src2
range|:
name|tags
control|)
block|{
if|if
condition|(
operator|!
name|src2
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|smallKeysMap
operator|.
name|put
argument_list|(
name|src2
argument_list|,
name|getSmallKeysDir
argument_list|(
name|baseTmpDir
argument_list|,
name|alias
argument_list|,
name|src2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|skewJoinJobResultsDir
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|getBigKeysSkewJoinResultDir
argument_list|(
name|baseTmpDir
argument_list|,
name|alias
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|joinDescriptor
operator|.
name|setHandleSkewJoin
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|joinDescriptor
operator|.
name|setBigKeysDirMap
argument_list|(
name|bigKeysDirMap
argument_list|)
expr_stmt|;
name|joinDescriptor
operator|.
name|setSmallKeysDirMap
argument_list|(
name|smallKeysDirMap
argument_list|)
expr_stmt|;
name|joinDescriptor
operator|.
name|setSkewKeyDefinition
argument_list|(
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESKEWJOINKEY
argument_list|)
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|bigKeysDirToTaskMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Serializable
argument_list|>
name|listWorks
init|=
operator|new
name|ArrayList
argument_list|<
name|Serializable
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|MapredWork
name|currPlan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|TableDesc
name|keyTblDesc
init|=
operator|(
name|TableDesc
operator|)
name|currPlan
operator|.
name|getKeyDesc
argument_list|()
operator|.
name|clone
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|joinKeys
init|=
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|keyTblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|joinKeyTypes
init|=
name|Utilities
operator|.
name|getColumnTypes
argument_list|(
name|keyTblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|tableDescList
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|newJoinValues
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|newJoinKeys
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// used for create mapJoinDesc, should be in order
name|List
argument_list|<
name|TableDesc
argument_list|>
name|newJoinValueTblDesc
init|=
operator|new
name|ArrayList
argument_list|<
name|TableDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Byte
name|tag
range|:
name|tags
control|)
block|{
name|newJoinValueTblDesc
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|Byte
name|alias
init|=
name|tags
index|[
name|i
index|]
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
init|=
name|joinValues
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|String
name|colNames
init|=
literal|""
decl_stmt|;
name|String
name|colTypes
init|=
literal|""
decl_stmt|;
name|int
name|columnSize
init|=
name|valueCols
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|newValueExpr
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|newKeyExpr
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|columnSize
condition|;
name|k
operator|++
control|)
block|{
name|TypeInfo
name|type
init|=
name|valueCols
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|String
name|newColName
init|=
name|i
operator|+
literal|"_VALUE_"
operator|+
name|k
decl_stmt|;
comment|// any name, it does not matter.
name|newValueExpr
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|type
argument_list|,
name|newColName
argument_list|,
literal|""
operator|+
name|i
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|colNames
operator|=
name|colNames
operator|+
literal|","
expr_stmt|;
name|colTypes
operator|=
name|colTypes
operator|+
literal|","
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|colNames
operator|=
name|colNames
operator|+
name|newColName
expr_stmt|;
name|colTypes
operator|=
name|colTypes
operator|+
name|valueCols
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getTypeString
argument_list|()
expr_stmt|;
block|}
comment|// we are putting join keys at last part of the spilled table
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|joinKeys
operator|.
name|size
argument_list|()
condition|;
name|k
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|colNames
operator|=
name|colNames
operator|+
literal|","
expr_stmt|;
name|colTypes
operator|=
name|colTypes
operator|+
literal|","
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|colNames
operator|=
name|colNames
operator|+
name|joinKeys
operator|.
name|get
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|colTypes
operator|=
name|colTypes
operator|+
name|joinKeyTypes
operator|.
name|get
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|newKeyExpr
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|joinKeyTypes
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
argument_list|,
name|joinKeys
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|,
literal|""
operator|+
name|i
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|newJoinValues
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|newValueExpr
argument_list|)
expr_stmt|;
name|newJoinKeys
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|newKeyExpr
argument_list|)
expr_stmt|;
name|tableDescList
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|colNames
argument_list|,
name|colTypes
argument_list|)
argument_list|)
expr_stmt|;
comment|// construct value table Desc
name|String
name|valueColNames
init|=
literal|""
decl_stmt|;
name|String
name|valueColTypes
init|=
literal|""
decl_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|columnSize
condition|;
name|k
operator|++
control|)
block|{
name|String
name|newColName
init|=
name|i
operator|+
literal|"_VALUE_"
operator|+
name|k
decl_stmt|;
comment|// any name, it does not matter.
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|valueColNames
operator|=
name|valueColNames
operator|+
literal|","
expr_stmt|;
name|valueColTypes
operator|=
name|valueColTypes
operator|+
literal|","
expr_stmt|;
block|}
name|valueColNames
operator|=
name|valueColNames
operator|+
name|newColName
expr_stmt|;
name|valueColTypes
operator|=
name|valueColTypes
operator|+
name|valueCols
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getTypeString
argument_list|()
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|newJoinValueTblDesc
operator|.
name|set
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|valueColNames
argument_list|,
name|valueColTypes
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|joinDescriptor
operator|.
name|setSkewKeysValuesTables
argument_list|(
name|tableDescList
argument_list|)
expr_stmt|;
name|joinDescriptor
operator|.
name|setKeyTableDesc
argument_list|(
name|keyTblDesc
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|Byte
name|src
init|=
name|tags
index|[
name|i
index|]
decl_stmt|;
name|MapredWork
name|newPlan
init|=
name|PlanUtils
operator|.
name|getMapRedWork
argument_list|()
decl_stmt|;
comment|// This code has been only added for testing
name|boolean
name|mapperCannotSpanPartns
init|=
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_MAPPER_CANNOT_SPAN_MULTIPLE_PARTITIONS
argument_list|)
decl_stmt|;
name|newPlan
operator|.
name|setMapperCannotSpanPartns
argument_list|(
name|mapperCannotSpanPartns
argument_list|)
expr_stmt|;
name|MapredWork
name|clonePlan
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|xmlPlan
init|=
name|currPlan
operator|.
name|toXML
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|xmlPlan
argument_list|)
decl_stmt|;
name|ByteArrayInputStream
name|bis
decl_stmt|;
name|bis
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|clonePlan
operator|=
name|Utilities
operator|.
name|deserializeMapRedWork
argument_list|(
name|bis
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
index|[]
name|parentOps
init|=
operator|new
name|TableScanOperator
index|[
name|tags
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|tags
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|ts
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|TableScanDesc
operator|.
name|class
argument_list|,
operator|(
name|RowSchema
operator|)
literal|null
argument_list|)
decl_stmt|;
operator|(
operator|(
name|TableScanOperator
operator|)
name|ts
operator|)
operator|.
name|setTableDesc
argument_list|(
name|tableDescList
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|k
argument_list|)
argument_list|)
expr_stmt|;
name|parentOps
index|[
name|k
index|]
operator|=
name|ts
expr_stmt|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|tblScan_op
init|=
name|parentOps
index|[
name|i
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|alias
init|=
name|src
operator|.
name|toString
argument_list|()
decl_stmt|;
name|aliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|String
name|bigKeyDirPath
init|=
name|bigKeysDirMap
operator|.
name|get
argument_list|(
name|src
argument_list|)
decl_stmt|;
name|newPlan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|bigKeyDirPath
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
name|newPlan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|tblScan_op
argument_list|)
expr_stmt|;
name|PartitionDesc
name|part
init|=
operator|new
name|PartitionDesc
argument_list|(
name|tableDescList
operator|.
name|get
argument_list|(
name|src
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|newPlan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|bigKeyDirPath
argument_list|,
name|part
argument_list|)
expr_stmt|;
name|newPlan
operator|.
name|getAliasToPartnInfo
argument_list|()
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|part
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|clonePlan
operator|.
name|getReducer
argument_list|()
decl_stmt|;
assert|assert
name|reducer
operator|instanceof
name|JoinOperator
assert|;
name|JoinOperator
name|cloneJoinOp
init|=
operator|(
name|JoinOperator
operator|)
name|reducer
decl_stmt|;
name|String
name|dumpFilePrefix
init|=
literal|"mapfile"
operator|+
name|PlanUtils
operator|.
name|getCountForMapJoinDumpFilePrefix
argument_list|()
decl_stmt|;
name|MapJoinDesc
name|mapJoinDescriptor
init|=
operator|new
name|MapJoinDesc
argument_list|(
name|newJoinKeys
argument_list|,
name|keyTblDesc
argument_list|,
name|newJoinValues
argument_list|,
name|newJoinValueTblDesc
argument_list|,
name|newJoinValueTblDesc
argument_list|,
name|joinDescriptor
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|,
name|i
argument_list|,
name|joinDescriptor
operator|.
name|getConds
argument_list|()
argument_list|,
name|joinDescriptor
operator|.
name|getFilters
argument_list|()
argument_list|,
name|joinDescriptor
operator|.
name|getNoOuterJoin
argument_list|()
argument_list|,
name|dumpFilePrefix
argument_list|)
decl_stmt|;
name|mapJoinDescriptor
operator|.
name|setTagOrder
argument_list|(
name|tags
argument_list|)
expr_stmt|;
name|mapJoinDescriptor
operator|.
name|setHandleSkewJoin
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|mapJoinDescriptor
operator|.
name|setNullSafes
argument_list|(
name|joinDescriptor
operator|.
name|getNullSafes
argument_list|()
argument_list|)
expr_stmt|;
name|MapredLocalWork
name|localPlan
init|=
operator|new
name|MapredLocalWork
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|smallTblDirs
init|=
name|smallKeysDirMap
operator|.
name|get
argument_list|(
name|src
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numAliases
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|j
operator|==
name|i
condition|)
block|{
continue|continue;
block|}
name|Byte
name|small_alias
init|=
name|tags
index|[
name|j
index|]
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|tblScan_op2
init|=
name|parentOps
index|[
name|j
index|]
decl_stmt|;
name|localPlan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|small_alias
operator|.
name|toString
argument_list|()
argument_list|,
name|tblScan_op2
argument_list|)
expr_stmt|;
name|Path
name|tblDir
init|=
operator|new
name|Path
argument_list|(
name|smallTblDirs
operator|.
name|get
argument_list|(
name|small_alias
argument_list|)
argument_list|)
decl_stmt|;
name|localPlan
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|put
argument_list|(
name|small_alias
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|FetchWork
argument_list|(
name|tblDir
operator|.
name|toString
argument_list|()
argument_list|,
name|tableDescList
operator|.
name|get
argument_list|(
name|small_alias
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|newPlan
operator|.
name|setMapLocalWork
argument_list|(
name|localPlan
argument_list|)
expr_stmt|;
comment|// construct a map join and set it as the child operator of tblScan_op
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|mapJoinDescriptor
argument_list|,
operator|(
name|RowSchema
operator|)
literal|null
argument_list|,
name|parentOps
argument_list|)
decl_stmt|;
comment|// change the children of the original join operator to point to the map
comment|// join operator
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOps
init|=
name|cloneJoinOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|childOps
control|)
block|{
name|childOp
operator|.
name|replaceParent
argument_list|(
name|cloneJoinOp
argument_list|,
name|mapJoinOp
argument_list|)
expr_stmt|;
block|}
name|mapJoinOp
operator|.
name|setChildOperators
argument_list|(
name|childOps
argument_list|)
expr_stmt|;
name|HiveConf
name|jc
init|=
operator|new
name|HiveConf
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
name|GenMRSkewJoinProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
name|newPlan
operator|.
name|setNumMapTasks
argument_list|(
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESKEWJOINMAPJOINNUMMAPTASK
argument_list|)
argument_list|)
expr_stmt|;
name|newPlan
operator|.
name|setMinSplitSize
argument_list|(
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESKEWJOINMAPJOINMINSPLIT
argument_list|)
argument_list|)
expr_stmt|;
name|newPlan
operator|.
name|setInputformat
argument_list|(
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|skewJoinMapJoinTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|newPlan
argument_list|,
name|jc
argument_list|)
decl_stmt|;
name|bigKeysDirToTaskMap
operator|.
name|put
argument_list|(
name|bigKeyDirPath
argument_list|,
name|skewJoinMapJoinTask
argument_list|)
expr_stmt|;
name|listWorks
operator|.
name|add
argument_list|(
name|skewJoinMapJoinTask
operator|.
name|getWork
argument_list|()
argument_list|)
expr_stmt|;
name|listTasks
operator|.
name|add
argument_list|(
name|skewJoinMapJoinTask
argument_list|)
expr_stmt|;
block|}
name|ConditionalWork
name|cndWork
init|=
operator|new
name|ConditionalWork
argument_list|(
name|listWorks
argument_list|)
decl_stmt|;
name|ConditionalTask
name|cndTsk
init|=
operator|(
name|ConditionalTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|cndWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|cndTsk
operator|.
name|setListTasks
argument_list|(
name|listTasks
argument_list|)
expr_stmt|;
name|cndTsk
operator|.
name|setResolver
argument_list|(
operator|new
name|ConditionalResolverSkewJoin
argument_list|()
argument_list|)
expr_stmt|;
name|cndTsk
operator|.
name|setResolverCtx
argument_list|(
operator|new
name|ConditionalResolverSkewJoin
operator|.
name|ConditionalResolverSkewJoinCtx
argument_list|(
name|bigKeysDirToTaskMap
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|oldChildTasks
init|=
name|currTask
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
name|currTask
operator|.
name|setChildTasks
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|cndTsk
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldChildTasks
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|cndTsk
operator|.
name|getListTasks
argument_list|()
control|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|oldChild
range|:
name|oldChildTasks
control|)
block|{
name|tsk
operator|.
name|addDependentTask
argument_list|(
name|oldChild
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return;
block|}
specifier|public
specifier|static
name|boolean
name|skewJoinEnabled
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|JoinOperator
name|joinOp
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|!=
literal|null
operator|&&
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESKEWJOIN
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isNoOuterJoin
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|byte
name|pos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Byte
name|tag
range|:
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTagOrder
argument_list|()
control|)
block|{
if|if
condition|(
name|tag
operator|!=
name|pos
condition|)
block|{
return|return
literal|false
return|;
block|}
name|pos
operator|++
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|String
name|skewJoinPrefix
init|=
literal|"hive_skew_join"
decl_stmt|;
specifier|private
specifier|static
name|String
name|UNDERLINE
init|=
literal|"_"
decl_stmt|;
specifier|private
specifier|static
name|String
name|BIGKEYS
init|=
literal|"bigkeys"
decl_stmt|;
specifier|private
specifier|static
name|String
name|SMALLKEYS
init|=
literal|"smallkeys"
decl_stmt|;
specifier|private
specifier|static
name|String
name|RESULTS
init|=
literal|"results"
decl_stmt|;
specifier|static
name|String
name|getBigKeysDir
parameter_list|(
name|String
name|baseDir
parameter_list|,
name|Byte
name|srcTbl
parameter_list|)
block|{
return|return
name|baseDir
operator|+
name|File
operator|.
name|separator
operator|+
name|skewJoinPrefix
operator|+
name|UNDERLINE
operator|+
name|BIGKEYS
operator|+
name|UNDERLINE
operator|+
name|srcTbl
return|;
block|}
specifier|static
name|String
name|getBigKeysSkewJoinResultDir
parameter_list|(
name|String
name|baseDir
parameter_list|,
name|Byte
name|srcTbl
parameter_list|)
block|{
return|return
name|baseDir
operator|+
name|File
operator|.
name|separator
operator|+
name|skewJoinPrefix
operator|+
name|UNDERLINE
operator|+
name|BIGKEYS
operator|+
name|UNDERLINE
operator|+
name|RESULTS
operator|+
name|UNDERLINE
operator|+
name|srcTbl
return|;
block|}
specifier|static
name|String
name|getSmallKeysDir
parameter_list|(
name|String
name|baseDir
parameter_list|,
name|Byte
name|srcTblBigTbl
parameter_list|,
name|Byte
name|srcTblSmallTbl
parameter_list|)
block|{
return|return
name|baseDir
operator|+
name|File
operator|.
name|separator
operator|+
name|skewJoinPrefix
operator|+
name|UNDERLINE
operator|+
name|SMALLKEYS
operator|+
name|UNDERLINE
operator|+
name|srcTblBigTbl
operator|+
name|UNDERLINE
operator|+
name|srcTblSmallTbl
return|;
block|}
block|}
end_class

end_unit

