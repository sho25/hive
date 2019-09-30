begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|parse
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|ContentSummary
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
name|fs
operator|.
name|PathFilter
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
name|QueryState
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
name|ReduceSinkOperator
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
name|exec
operator|.
name|mr
operator|.
name|ExecDriver
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
name|mr
operator|.
name|MapRedTask
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|DefaultRuleDispatcher
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
name|Dispatcher
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
name|GraphWalker
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
name|Rule
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
name|RuleRegExp
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
name|Hive
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
name|GenMRFileSink1
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
name|GenMROperator
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
name|optimizer
operator|.
name|GenMRRedSink1
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
name|GenMRRedSink2
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
name|GenMRRedSink3
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
name|GenMRTableScan1
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
name|GenMRUnion1
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
name|MapJoinFactory
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
name|physical
operator|.
name|PhysicalContext
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
name|physical
operator|.
name|PhysicalOptimizer
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
name|MapWork
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_class
specifier|public
class|class
name|MapReduceCompiler
extends|extends
name|TaskCompiler
block|{
specifier|protected
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MapReduceCompiler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|MapReduceCompiler
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|LogHelper
name|console
parameter_list|,
name|Hive
name|db
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|queryState
argument_list|,
name|console
argument_list|,
name|db
argument_list|)
expr_stmt|;
comment|//It is required the use of recursive input dirs when hive.optimize.union.remove = true
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_OPTIMIZE_UNION_REMOVE
argument_list|)
condition|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|// loop over all the tasks recursively
annotation|@
name|Override
specifier|protected
name|void
name|setInputFormat
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|ExecDriver
condition|)
block|{
name|MapWork
name|work
init|=
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|Map
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
name|opMap
init|=
name|work
operator|.
name|getAliasToWork
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|opMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|opMap
operator|.
name|values
argument_list|()
control|)
block|{
name|setInputFormat
argument_list|(
name|work
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|listTasks
init|=
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|tsk
range|:
name|listTasks
control|)
block|{
name|setInputFormat
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|task
operator|.
name|getChildTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|childTask
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
name|setInputFormat
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|optimizeOperatorPlan
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|runDynPartitionSortOptimizations
argument_list|(
name|pCtx
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setInputFormat
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
if|if
condition|(
name|op
operator|.
name|isUseBucketizedHiveInputFormat
argument_list|()
condition|)
block|{
name|work
operator|.
name|setUseBucketizedHiveInputFormat
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
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
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|setInputFormat
argument_list|(
name|work
argument_list|,
name|childOp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// loop over all the tasks recursively
specifier|private
name|void
name|breakTaskTree
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|ExecDriver
condition|)
block|{
name|Map
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
name|opMap
init|=
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getMapWork
argument_list|()
operator|.
name|getAliasToWork
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|opMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|opMap
operator|.
name|values
argument_list|()
control|)
block|{
name|breakOperatorTree
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|listTasks
init|=
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|tsk
range|:
name|listTasks
control|)
block|{
name|breakTaskTree
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|task
operator|.
name|getChildTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|childTask
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
name|breakTaskTree
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
comment|// loop over all the operators recursively
specifier|private
name|void
name|breakOperatorTree
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|topOp
parameter_list|)
block|{
if|if
condition|(
name|topOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|topOp
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|topOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|breakOperatorTree
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Make a best guess at trying to find the number of reducers    */
specifier|private
specifier|static
name|int
name|getNumberOfReducers
parameter_list|(
name|MapredWork
name|mrwork
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
if|if
condition|(
name|mrwork
operator|.
name|getReduceWork
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|mrwork
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getNumReduceTasks
argument_list|()
operator|>=
literal|0
condition|)
block|{
return|return
name|mrwork
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getNumReduceTasks
argument_list|()
return|;
block|}
return|return
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|decideExecMode
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Context
name|ctx
parameter_list|,
name|GlobalLimitCtx
name|globalLimitCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// bypass for explain queries for now
if|if
condition|(
name|ctx
operator|.
name|isExplainSkipExecution
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// user has told us to run in local mode or doesn't want auto-local mode
if|if
condition|(
name|ctx
operator|.
name|isLocalOnlyExecutionMode
argument_list|()
operator|||
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEAUTO
argument_list|)
condition|)
block|{
return|return;
block|}
specifier|final
name|Context
name|lCtx
init|=
name|ctx
decl_stmt|;
name|PathFilter
name|p
init|=
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
return|return
operator|!
name|lCtx
operator|.
name|isMRTmpFileURI
argument_list|(
name|file
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|ExecDriver
argument_list|>
name|mrtasks
init|=
name|Utilities
operator|.
name|getMRTasks
argument_list|(
name|rootTasks
argument_list|)
decl_stmt|;
comment|// map-reduce jobs will be run locally based on data size
comment|// first find out if any of the jobs needs to run non-locally
name|boolean
name|hasNonLocalJob
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ExecDriver
name|mrtask
range|:
name|mrtasks
control|)
block|{
try|try
block|{
name|ContentSummary
name|inputSummary
init|=
name|Utilities
operator|.
name|getInputSummary
argument_list|(
name|ctx
argument_list|,
name|mrtask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
argument_list|,
name|p
argument_list|)
decl_stmt|;
name|int
name|numReducers
init|=
name|getNumberOfReducers
argument_list|(
name|mrtask
operator|.
name|getWork
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|long
name|estimatedInput
decl_stmt|;
if|if
condition|(
name|globalLimitCtx
operator|!=
literal|null
operator|&&
name|globalLimitCtx
operator|.
name|isEnable
argument_list|()
condition|)
block|{
comment|// If the global limit optimization is triggered, we will
comment|// estimate input data actually needed based on limit rows.
comment|// estimated Input = (num_limit * max_size_per_row) * (estimated_map + 2)
comment|//
name|long
name|sizePerRow
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVELIMITMAXROWSIZE
argument_list|)
decl_stmt|;
name|estimatedInput
operator|=
operator|(
name|globalLimitCtx
operator|.
name|getGlobalOffset
argument_list|()
operator|+
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
operator|)
operator|*
name|sizePerRow
expr_stmt|;
name|long
name|minSplitSize
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZE
argument_list|)
decl_stmt|;
name|long
name|estimatedNumMap
init|=
name|inputSummary
operator|.
name|getLength
argument_list|()
operator|/
name|minSplitSize
operator|+
literal|1
decl_stmt|;
name|estimatedInput
operator|=
name|estimatedInput
operator|*
operator|(
name|estimatedNumMap
operator|+
literal|1
operator|)
expr_stmt|;
block|}
else|else
block|{
name|estimatedInput
operator|=
name|inputSummary
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Task: "
operator|+
name|mrtask
operator|.
name|getId
argument_list|()
operator|+
literal|", Summary: "
operator|+
name|inputSummary
operator|.
name|getLength
argument_list|()
operator|+
literal|","
operator|+
name|inputSummary
operator|.
name|getFileCount
argument_list|()
operator|+
literal|","
operator|+
name|numReducers
operator|+
literal|", estimated Input: "
operator|+
name|estimatedInput
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|MapRedTask
operator|.
name|isEligibleForLocalMode
argument_list|(
name|conf
argument_list|,
name|numReducers
argument_list|,
name|estimatedInput
argument_list|,
name|inputSummary
operator|.
name|getFileCount
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|hasNonLocalJob
operator|=
literal|true
expr_stmt|;
break|break;
block|}
else|else
block|{
name|mrtask
operator|.
name|setLocalMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
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
block|}
if|if
condition|(
operator|!
name|hasNonLocalJob
condition|)
block|{
comment|// Entire query can be run locally.
comment|// Save the current tracker value and restore it when done.
name|ctx
operator|.
name|setOriginalTracker
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getJobLauncherRpcAddress
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|setJobLauncherRpcAddress
argument_list|(
name|conf
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Automatically selecting local only mode for query"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|optimizeTaskPlan
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// reduce sink does not have any kids - since the plan by now has been
comment|// broken up into multiple
comment|// tasks, iterate over all tasks.
comment|// For each task, go over all operators recursively
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
range|:
name|rootTasks
control|)
block|{
name|breakTaskTree
argument_list|(
name|rootTask
argument_list|)
expr_stmt|;
block|}
name|PhysicalContext
name|physicalContext
init|=
operator|new
name|PhysicalContext
argument_list|(
name|conf
argument_list|,
name|getParseContext
argument_list|(
name|pCtx
argument_list|,
name|rootTasks
argument_list|)
argument_list|,
name|ctx
argument_list|,
name|rootTasks
argument_list|,
name|pCtx
operator|.
name|getFetchTask
argument_list|()
argument_list|)
decl_stmt|;
name|PhysicalOptimizer
name|physicalOptimizer
init|=
operator|new
name|PhysicalOptimizer
argument_list|(
name|physicalContext
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|physicalOptimizer
operator|.
name|optimize
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|generateTaskTree
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// generate map reduce plans
name|ParseContext
name|tempParseContext
init|=
name|getParseContext
argument_list|(
name|pCtx
argument_list|,
name|rootTasks
argument_list|)
decl_stmt|;
name|GenMRProcContext
name|procCtx
init|=
operator|new
name|GenMRProcContext
argument_list|(
name|conf
argument_list|,
comment|// Must be deterministic order map for consistent q-test output across Java versions
operator|new
name|LinkedHashMap
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
argument_list|>
argument_list|>
argument_list|()
condition|,
name|tempParseContext
condition|,
name|mvTask
condition|,
name|rootTasks
condition|,
operator|new
name|LinkedHashMap
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
argument_list|()
condition|,
name|inputs
condition|,
name|outputs
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack.
comment|// The dispatcher generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R1"
argument_list|)
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRTableScan1
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R2"
argument_list|)
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%.*"
operator|+
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRRedSink1
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R3"
argument_list|)
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%.*"
operator|+
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRRedSink2
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R4"
argument_list|)
argument_list|,
name|FileSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRFileSink1
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R5"
argument_list|)
argument_list|,
name|UnionOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRUnion1
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R6"
argument_list|)
argument_list|,
name|UnionOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%.*"
operator|+
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenMRRedSink3
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R7"
argument_list|)
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|MapJoinFactory
operator|.
name|getTableScanMapJoin
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
operator|new
name|GenMROperator
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|procCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|GenMapRedWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

