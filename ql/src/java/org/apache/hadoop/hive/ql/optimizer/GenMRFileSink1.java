begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|FetchTask
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
name|GenMapRedCtx
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
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenMRFileSink1
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|GenMRFileSink1
parameter_list|()
block|{   }
comment|/**    * File Sink Operator encountered.    *    * @param nd    *          the file sink operator encountered    * @param opProcCtx    *          context    */
annotation|@
name|Override
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
comment|// we should look take the parent of fsOp's task as the current task.
name|FileSinkOperator
name|fsOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|nd
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addRootIfPossible
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|boolean
name|isInsertTable
init|=
comment|// is INSERT OVERWRITE TABLE
name|GenMapRedUtils
operator|.
name|isInsertInto
argument_list|(
name|parseCtx
argument_list|,
name|fsOp
argument_list|)
decl_stmt|;
name|HiveConf
name|hconf
init|=
name|parseCtx
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Mark this task as a final map reduce task (ignoring the optional merge task)
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
name|setFinalMapRed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// If this file sink desc has been processed due to a linked file sink desc,
comment|// use that task
name|Map
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|fileSinkDescs
init|=
name|ctx
operator|.
name|getLinkedFileDescTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileSinkDescs
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
name|childTask
init|=
name|fileSinkDescs
operator|.
name|get
argument_list|(
name|fsOp
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|processLinkedFileDesc
argument_list|(
name|ctx
argument_list|,
name|childTask
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
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
name|chDir
operator|=
name|GenMapRedUtils
operator|.
name|isMergeRequired
argument_list|(
name|ctx
operator|.
name|getMvTask
argument_list|()
argument_list|,
name|hconf
argument_list|,
name|fsOp
argument_list|,
name|currTask
argument_list|,
name|isInsertTable
argument_list|)
expr_stmt|;
block|}
name|Path
name|finalName
init|=
name|processFS
argument_list|(
name|fsOp
argument_list|,
name|stack
argument_list|,
name|opProcCtx
argument_list|,
name|chDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|chDir
condition|)
block|{
comment|// Merge the files in the destination table/partitions by creating Map-only merge job
comment|// If underlying data is RCFile or OrcFile, RCFileBlockMerge task or
comment|// OrcFileStripeMerge task would be created.
name|LOG
operator|.
name|info
argument_list|(
literal|"using CombineHiveInputformat for the merge job"
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|createMRWorkForMergingFiles
argument_list|(
name|fsOp
argument_list|,
name|finalName
argument_list|,
name|ctx
operator|.
name|getDependencyTaskForMultiInsert
argument_list|()
argument_list|,
name|ctx
operator|.
name|getMvTask
argument_list|()
argument_list|,
name|hconf
argument_list|,
name|currTask
argument_list|,
name|parseCtx
operator|.
name|getQueryState
argument_list|()
operator|.
name|getLineageState
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|FileSinkDesc
name|fileSinkDesc
init|=
name|fsOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// There are linked file sink operators and child tasks are present
if|if
condition|(
name|fileSinkDesc
operator|.
name|isLinkedFileSink
argument_list|()
operator|&&
operator|(
name|currTask
operator|.
name|getChildTasks
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|currTask
operator|.
name|getChildTasks
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
condition|)
block|{
name|Map
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|linkedFileDescTasks
init|=
name|ctx
operator|.
name|getLinkedFileDescTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|linkedFileDescTasks
operator|==
literal|null
condition|)
block|{
name|linkedFileDescTasks
operator|=
operator|new
name|HashMap
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|setLinkedFileDescTasks
argument_list|(
name|linkedFileDescTasks
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FileSinkDesc
name|fileDesc
range|:
name|fileSinkDesc
operator|.
name|getLinkedFileSinkDesc
argument_list|()
control|)
block|{
name|linkedFileDescTasks
operator|.
name|put
argument_list|(
name|fileDesc
argument_list|,
name|currTask
operator|.
name|getChildTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|FetchTask
name|fetchTask
init|=
name|parseCtx
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchTask
operator|!=
literal|null
operator|&&
name|currTask
operator|.
name|getNumChild
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|fetchTask
operator|.
name|isFetchFrom
argument_list|(
name|fileSinkDesc
argument_list|)
condition|)
block|{
name|currTask
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/*    * Multiple file sink descriptors are linked.    * Use the task created by the first linked file descriptor    */
specifier|private
name|void
name|processLinkedFileDesc
parameter_list|(
name|GenMRProcContext
name|ctx
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
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
name|TableScanOperator
name|currTopOp
init|=
name|ctx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
if|if
condition|(
name|currTopOp
operator|!=
literal|null
operator|&&
operator|!
name|ctx
operator|.
name|isSeenOp
argument_list|(
name|currTask
argument_list|,
name|currTopOp
argument_list|)
condition|)
block|{
name|String
name|currAliasId
init|=
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|currTask
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|childTask
operator|!=
literal|null
condition|)
block|{
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Process the FileSink operator to generate a MoveTask if necessary.    *    * @param fsOp    *          current FileSink operator    * @param stack    *          parent operators    * @param opProcCtx    * @param chDir    *          whether the operator should be first output to a tmp dir and then merged    *          to the final dir later    * @return the final file name to which the FileSinkOperator should store.    * @throws SemanticException    */
specifier|private
name|Path
name|processFS
parameter_list|(
name|FileSinkOperator
name|fsOp
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
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
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
comment|// If the directory needs to be changed, send the new directory
name|Path
name|dest
init|=
literal|null
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
name|dest
operator|=
name|GenMapRedUtils
operator|.
name|createMoveTask
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
name|chDir
argument_list|,
name|fsOp
argument_list|,
name|ctx
operator|.
name|getParseCtx
argument_list|()
argument_list|,
name|ctx
operator|.
name|getMvTask
argument_list|()
argument_list|,
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|ctx
operator|.
name|getDependencyTaskForMultiInsert
argument_list|()
argument_list|)
expr_stmt|;
name|TableScanOperator
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
name|OperatorDesc
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
if|if
condition|(
operator|!
name|ctx
operator|.
name|isSeenOp
argument_list|(
name|currTask
argument_list|,
name|currTopOp
argument_list|)
condition|)
block|{
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|currTask
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
name|opTaskMap
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|ctx
operator|.
name|isSeenOp
argument_list|(
name|currTask
argument_list|,
name|currTopOp
argument_list|)
condition|)
block|{
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|mapTask
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|initUnionPlan
argument_list|(
name|ctx
argument_list|,
name|currUnionOp
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
block|}
comment|// mapTask and currTask should be merged by and join/union operator
comment|// (e.g., GenMRUnion1) which has multiple topOps.
comment|// assert mapTask == currTask : "mapTask.id = " + mapTask.getId()
comment|// + "; currTask.id = " + currTask.getId();
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
name|currUnionOp
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
return|return
name|dest
return|;
block|}
block|}
end_class

end_unit

