begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|Deque
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
name|LinkedList
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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|tez
operator|.
name|TezTask
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
name|optimizer
operator|.
name|ConvertJoinMapJoin
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
name|BaseWork
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
name|plan
operator|.
name|TezWork
import|;
end_import

begin_comment
comment|/**  * TezCompiler translates the operator plan into TezTasks.  */
end_comment

begin_class
specifier|public
class|class
name|TezCompiler
extends|extends
name|TaskCompiler
block|{
specifier|protected
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TezCompiler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|TezCompiler
parameter_list|()
block|{   }
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
comment|// Sequence of TableScan operators to be walked
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|deque
init|=
operator|new
name|LinkedList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|deque
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
comment|// Create the context for the walker
name|OptimizeTezProcContext
name|procCtx
init|=
operator|new
name|OptimizeTezProcContext
argument_list|(
name|conf
argument_list|,
name|pCtx
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|,
name|deque
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack.
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
literal|"Set parallelism - ReduceSink"
argument_list|)
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|SetReducerParallelism
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
literal|"Convert Join to Map-join"
argument_list|)
argument_list|,
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|ConvertJoinMapJoin
argument_list|()
argument_list|)
expr_stmt|;
comment|// if this is an explain statement add rule to generate statistics for
comment|// the whole tree.
if|if
condition|(
name|pCtx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplain
argument_list|()
condition|)
block|{
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
literal|"Set statistics - FileSink"
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
name|SetStatistics
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
name|procCtx
argument_list|)
decl_stmt|;
name|List
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
name|GraphWalker
name|ogw
init|=
operator|new
name|TezWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
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
extends|extends
name|Serializable
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
comment|// Sequence of TableScan operators to be walked
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|deque
init|=
operator|new
name|LinkedList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|deque
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
name|GenTezProcContext
name|procCtx
init|=
operator|new
name|GenTezProcContext
argument_list|(
name|conf
argument_list|,
name|tempParseContext
argument_list|,
name|mvTask
argument_list|,
name|rootTasks
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|,
name|deque
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
literal|"Split Work - ReduceSink"
argument_list|)
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GenTezWork
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
literal|"Split Work - FileSink"
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
name|GenTezWork
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
literal|null
argument_list|,
name|opRules
argument_list|,
name|procCtx
argument_list|)
decl_stmt|;
name|List
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
name|GraphWalker
name|ogw
init|=
operator|new
name|TezWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
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
annotation|@
name|Override
specifier|protected
name|void
name|setInputFormat
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|TezTask
condition|)
block|{
name|TezWork
name|work
init|=
operator|(
operator|(
name|TezTask
operator|)
name|task
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|roots
init|=
name|work
operator|.
name|getRoots
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|roots
control|)
block|{
assert|assert
name|w
operator|instanceof
name|MapWork
assert|;
name|MapWork
name|mapWork
init|=
operator|(
name|MapWork
operator|)
name|w
decl_stmt|;
name|HashMap
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
name|mapWork
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
name|mapWork
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
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
extends|extends
name|Serializable
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
extends|extends
name|Serializable
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
extends|extends
name|Serializable
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
annotation|@
name|Override
specifier|protected
name|void
name|generateCountersTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|TezTask
condition|)
block|{
name|TezWork
name|work
init|=
operator|(
operator|(
name|TezTask
operator|)
name|task
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|workItems
init|=
name|work
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|workItems
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|w
operator|.
name|getAllOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
control|)
block|{
name|generateCountersOperator
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
extends|extends
name|Serializable
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
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|listTasks
control|)
block|{
name|generateCountersTask
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
name|Operator
operator|.
name|resetLastEnumUsed
argument_list|()
expr_stmt|;
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
extends|extends
name|Serializable
argument_list|>
name|childTask
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
name|generateCountersTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|generateCountersOperator
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
parameter_list|)
block|{
name|op
operator|.
name|assignCounterNameToEnum
argument_list|()
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|child
range|:
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|generateCountersOperator
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
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
extends|extends
name|Serializable
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
comment|// currently all Tez work is on the cluster
return|return;
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
extends|extends
name|Serializable
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
comment|// no additional optimization needed
return|return;
block|}
block|}
end_class

end_unit

