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
name|HashMap
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
name|Stack
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
name|Context
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
name|AbstractMapJoinOperator
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
name|ColumnInfo
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
name|FileSinkOperator
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
name|MoveTask
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
name|UDFArgumentException
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
name|UnionOperator
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|optimizer
operator|.
name|GenMRProcContext
operator|.
name|GenMRMapJoinCtx
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
name|RowResolver
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
name|SemanticAnalyzer
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
name|parse
operator|.
name|TypeCheckProcFactory
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
name|ConditionalResolverMergeFiles
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
name|ExtractDesc
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
name|FileSinkDesc
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
name|LoadFileDesc
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
name|MoveWork
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
name|ReduceSinkDesc
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
name|ql
operator|.
name|plan
operator|.
name|ConditionalResolverMergeFiles
operator|.
name|ConditionalResolverMergeFilesCtx
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
comment|/**  * Processor for the rule - table scan followed by reduce sink.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRFileSink1
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRFileSink1
parameter_list|()
block|{   }
comment|/**    * File Sink Operator encountered.    *     * @param nd    *          the file sink operator encountered    * @param opProcCtx    *          context    */
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|opProcCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|boolean
name|chDir
init|=
literal|false
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
comment|// Has the user enabled merging of files for map-only jobs or for all jobs
if|if
condition|(
operator|(
name|ctx
operator|.
name|getMvTask
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|ctx
operator|.
name|getMvTask
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|mvTasks
init|=
name|ctx
operator|.
name|getMvTask
argument_list|()
decl_stmt|;
comment|// In case of unions or map-joins, it is possible that the file has
comment|// already been seen.
comment|// So, no need to attempt to merge the files again.
if|if
condition|(
operator|(
name|ctx
operator|.
name|getSeenFileSinkOps
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|ctx
operator|.
name|getSeenFileSinkOps
argument_list|()
operator|.
name|contains
argument_list|(
name|nd
argument_list|)
operator|)
condition|)
block|{
comment|// no need of merging if the move is to a local file system
name|MoveTask
name|mvTask
init|=
operator|(
name|MoveTask
operator|)
name|findMoveTask
argument_list|(
name|mvTasks
argument_list|,
operator|(
name|FileSinkOperator
operator|)
name|nd
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|mvTask
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|mvTask
operator|.
name|isLocal
argument_list|()
condition|)
block|{
comment|// There are separate configuration parameters to control whether to
comment|// merge for a map-only job
comment|// or for a map-reduce job
if|if
condition|(
operator|(
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
name|HIVEMERGEMAPFILES
argument_list|)
operator|&&
operator|(
operator|(
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
operator|)
operator|)
operator|||
operator|(
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
name|HIVEMERGEMAPREDFILES
argument_list|)
operator|&&
operator|(
operator|(
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getReducer
argument_list|()
operator|!=
literal|null
operator|)
operator|)
condition|)
block|{
name|chDir
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
name|String
name|finalName
init|=
name|processFS
argument_list|(
name|nd
argument_list|,
name|stack
argument_list|,
name|opProcCtx
argument_list|,
name|chDir
argument_list|)
decl_stmt|;
comment|// If it is a map-only job, insert a new task to do the concatenation
if|if
condition|(
name|chDir
operator|&&
operator|(
name|finalName
operator|!=
literal|null
operator|)
condition|)
block|{
name|createMergeJob
argument_list|(
operator|(
name|FileSinkOperator
operator|)
name|nd
argument_list|,
name|ctx
argument_list|,
name|finalName
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|createMergeJob
parameter_list|(
name|FileSinkOperator
name|fsOp
parameter_list|,
name|GenMRProcContext
name|ctx
parameter_list|,
name|String
name|finalName
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|RowSchema
name|fsRS
init|=
name|fsOp
operator|.
name|getSchema
argument_list|()
decl_stmt|;
comment|// create a reduce Sink operator - key is the first column
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|keyCols
operator|.
name|add
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"rand"
argument_list|)
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|ci
range|:
name|fsRS
operator|.
name|getSignature
argument_list|()
control|)
block|{
name|valueCols
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|ci
operator|.
name|getType
argument_list|()
argument_list|,
name|ci
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|ci
operator|.
name|getTabAlias
argument_list|()
argument_list|,
name|ci
operator|.
name|getIsPartitionCol
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// create a dummy tableScan operator
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|ts_op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|TableScanDesc
operator|.
name|class
argument_list|,
name|fsRS
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|valueCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|outputColumns
operator|.
name|add
argument_list|(
name|SemanticAnalyzer
operator|.
name|getColumnInternalName
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ReduceSinkDesc
name|rsDesc
init|=
name|PlanUtils
operator|.
name|getReduceSinkDesc
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|,
name|valueCols
argument_list|,
name|outputColumns
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|rsDesc
argument_list|,
name|fsRS
argument_list|,
name|ts_op
argument_list|)
expr_stmt|;
name|MapredWork
name|cplan
init|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|()
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mergeTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|cplan
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|FileSinkDesc
name|fsConf
init|=
name|fsOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Add the extract operator to get the value fields
name|RowResolver
name|out_rwsch
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
name|RowResolver
name|interim_rwsch
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
operator|.
name|getOpParseCtx
argument_list|()
operator|.
name|get
argument_list|(
name|fsOp
argument_list|)
operator|.
name|getRR
argument_list|()
decl_stmt|;
name|Integer
name|pos
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|colInfo
range|:
name|interim_rwsch
operator|.
name|getColumnInfos
argument_list|()
control|)
block|{
name|String
index|[]
name|info
init|=
name|interim_rwsch
operator|.
name|reverseLookup
argument_list|(
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
decl_stmt|;
name|out_rwsch
operator|.
name|put
argument_list|(
name|info
index|[
literal|0
index|]
argument_list|,
name|info
index|[
literal|1
index|]
argument_list|,
operator|new
name|ColumnInfo
argument_list|(
name|pos
operator|.
name|toString
argument_list|()
argument_list|,
name|colInfo
operator|.
name|getType
argument_list|()
argument_list|,
name|info
index|[
literal|0
index|]
argument_list|,
name|colInfo
operator|.
name|getIsPartitionCol
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|pos
operator|.
name|intValue
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|Operator
name|extract
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|new
name|ExtractDesc
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|out_rwsch
operator|.
name|getColumnInfos
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|TableDesc
name|ts
init|=
operator|(
name|TableDesc
operator|)
name|fsConf
operator|.
name|getTableInfo
argument_list|()
operator|.
name|clone
argument_list|()
decl_stmt|;
name|fsConf
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getProperties
argument_list|()
operator|.
name|remove
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Constants
operator|.
name|META_TABLE_PARTITION_COLUMNS
argument_list|)
expr_stmt|;
name|FileSinkOperator
name|newOutput
init|=
operator|(
name|FileSinkOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|new
name|FileSinkDesc
argument_list|(
name|finalName
argument_list|,
name|ts
argument_list|,
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
name|COMPRESSRESULT
argument_list|)
argument_list|)
argument_list|,
name|fsRS
argument_list|,
name|extract
argument_list|)
decl_stmt|;
name|cplan
operator|.
name|setReducer
argument_list|(
name|extract
argument_list|)
expr_stmt|;
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
name|aliases
operator|.
name|add
argument_list|(
name|fsConf
operator|.
name|getDirName
argument_list|()
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|fsConf
operator|.
name|getDirName
argument_list|()
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|fsConf
operator|.
name|getDirName
argument_list|()
argument_list|,
name|ts_op
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|fsConf
operator|.
name|getDirName
argument_list|()
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|fsConf
operator|.
name|getTableInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|setNumReduceTasks
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|MoveWork
name|dummyMv
init|=
operator|new
name|MoveWork
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|LoadFileDesc
argument_list|(
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getDirName
argument_list|()
argument_list|,
name|finalName
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|dummyMergeTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|dummyMv
argument_list|,
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|)
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
name|listWorks
operator|.
name|add
argument_list|(
name|dummyMv
argument_list|)
expr_stmt|;
name|listWorks
operator|.
name|add
argument_list|(
name|mergeTask
operator|.
name|getWork
argument_list|()
argument_list|)
expr_stmt|;
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
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|)
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
name|listTasks
operator|.
name|add
argument_list|(
name|dummyMergeTask
argument_list|)
expr_stmt|;
name|listTasks
operator|.
name|add
argument_list|(
name|mergeTask
argument_list|)
expr_stmt|;
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
name|ConditionalResolverMergeFiles
argument_list|()
argument_list|)
expr_stmt|;
name|cndTsk
operator|.
name|setResolverCtx
argument_list|(
operator|new
name|ConditionalResolverMergeFilesCtx
argument_list|(
name|listTasks
argument_list|,
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getDirName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|cndTsk
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
name|mvTasks
init|=
name|ctx
operator|.
name|getMvTask
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mvTask
init|=
name|findMoveTask
argument_list|(
name|mvTasks
argument_list|,
name|newOutput
argument_list|)
decl_stmt|;
if|if
condition|(
name|mvTask
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
name|tsk
operator|.
name|addDependentTask
argument_list|(
name|mvTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|findMoveTask
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|mvTasks
parameter_list|,
name|FileSinkOperator
name|fsOp
parameter_list|)
block|{
comment|// find the move task
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mvTsk
range|:
name|mvTasks
control|)
block|{
name|MoveWork
name|mvWork
init|=
operator|(
name|MoveWork
operator|)
name|mvTsk
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|String
name|srcDir
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mvWork
operator|.
name|getLoadFileWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|srcDir
operator|=
name|mvWork
operator|.
name|getLoadFileWork
argument_list|()
operator|.
name|getSourceDir
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mvWork
operator|.
name|getLoadTableWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|srcDir
operator|=
name|mvWork
operator|.
name|getLoadTableWork
argument_list|()
operator|.
name|getSourceDir
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|srcDir
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|srcDir
operator|.
name|equalsIgnoreCase
argument_list|(
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getDirName
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
name|mvTsk
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|String
name|processFS
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|opProcCtx
parameter_list|,
name|boolean
name|chDir
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Is it the dummy file sink after the mapjoin
name|FileSinkOperator
name|fsOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
operator|(
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
operator|&&
operator|(
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|MapJoinOperator
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
name|seenFSOps
init|=
name|ctx
operator|.
name|getSeenFileSinkOps
argument_list|()
decl_stmt|;
if|if
condition|(
name|seenFSOps
operator|==
literal|null
condition|)
block|{
name|seenFSOps
operator|=
operator|new
name|ArrayList
argument_list|<
name|FileSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|seenFSOps
operator|.
name|contains
argument_list|(
name|fsOp
argument_list|)
condition|)
block|{
name|seenFSOps
operator|.
name|add
argument_list|(
name|fsOp
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setSeenFileSinkOps
argument_list|(
name|seenFSOps
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
comment|// If the directory needs to be changed, send the new directory
name|String
name|dest
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|chDir
condition|)
block|{
name|dest
operator|=
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getDirName
argument_list|()
expr_stmt|;
comment|// generate the temporary file
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|Context
name|baseCtx
init|=
name|parseCtx
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|String
name|tmpDir
init|=
name|baseCtx
operator|.
name|getMRTmpFileURI
argument_list|()
decl_stmt|;
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setDirName
argument_list|(
name|tmpDir
argument_list|)
expr_stmt|;
block|}
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mvTask
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|chDir
condition|)
block|{
name|mvTask
operator|=
name|findMoveTask
argument_list|(
name|ctx
operator|.
name|getMvTask
argument_list|()
argument_list|,
name|fsOp
argument_list|)
expr_stmt|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|ctx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|ctx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|seenOps
init|=
name|ctx
operator|.
name|getSeenOps
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
name|rootTasks
init|=
name|ctx
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
comment|// Set the move task to be dependent on the current task
if|if
condition|(
name|mvTask
operator|!=
literal|null
condition|)
block|{
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|mvTask
argument_list|)
expr_stmt|;
block|}
comment|// In case of multi-table insert, the path to alias mapping is needed for
comment|// all the sources. Since there is no
comment|// reducer, treat it as a plan with null reducer
comment|// If it is a map-only job, the task needs to be processed
if|if
condition|(
name|currTopOp
operator|!=
literal|null
condition|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mapTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapTask
operator|==
literal|null
condition|)
block|{
assert|assert
operator|(
operator|!
name|seenOps
operator|.
name|contains
argument_list|(
name|currTopOp
argument_list|)
operator|)
assert|;
name|seenOps
operator|.
name|add
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|opTaskMap
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|seenOps
operator|.
name|contains
argument_list|(
name|currTopOp
argument_list|)
condition|)
block|{
name|seenOps
operator|.
name|add
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
operator|(
name|MapredWork
operator|)
name|mapTask
operator|.
name|getWork
argument_list|()
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
comment|// mapTask and currTask should be merged by and join/union operator
comment|// (e.g., GenMRUnion1j) which has multiple topOps.
assert|assert
name|mapTask
operator|==
name|currTask
operator|:
literal|"mapTask.id = "
operator|+
name|mapTask
operator|.
name|getId
argument_list|()
operator|+
literal|"; currTask.id = "
operator|+
name|currTask
operator|.
name|getId
argument_list|()
assert|;
block|}
return|return
name|dest
return|;
block|}
name|UnionOperator
name|currUnionOp
init|=
name|ctx
operator|.
name|getCurrUnionOp
argument_list|()
decl_stmt|;
if|if
condition|(
name|currUnionOp
operator|!=
literal|null
condition|)
block|{
name|opTaskMap
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|initUnionPlan
argument_list|(
name|ctx
argument_list|,
name|currTask
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|dest
return|;
block|}
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|currMapJoinOp
init|=
name|ctx
operator|.
name|getCurrMapJoinOp
argument_list|()
decl_stmt|;
if|if
condition|(
name|currMapJoinOp
operator|!=
literal|null
condition|)
block|{
name|opTaskMap
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|GenMRMapJoinCtx
name|mjCtx
init|=
name|ctx
operator|.
name|getMapJoinCtx
argument_list|(
name|currMapJoinOp
argument_list|)
decl_stmt|;
name|MapredWork
name|plan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|String
name|taskTmpDir
init|=
name|mjCtx
operator|.
name|getTaskTmpDir
argument_list|()
decl_stmt|;
name|TableDesc
name|tt_desc
init|=
name|mjCtx
operator|.
name|getTTDesc
argument_list|()
decl_stmt|;
assert|assert
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|==
literal|null
assert|;
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|.
name|add
argument_list|(
name|taskTmpDir
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|tt_desc
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
name|mjCtx
operator|.
name|getRootMapJoinOp
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|dest
return|;
block|}
return|return
name|dest
return|;
block|}
block|}
end_class

end_unit

