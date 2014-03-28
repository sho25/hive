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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|CompositeProcessor
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
name|ForwardWalker
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
name|optimizer
operator|.
name|ReduceSinkMapJoinProc
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
name|SetReducerParallelism
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
name|Vectorizer
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
name|StageIDsRearranger
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
specifier|public
name|void
name|init
parameter_list|(
name|HiveConf
name|conf
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
name|conf
argument_list|,
name|console
argument_list|,
name|db
argument_list|)
expr_stmt|;
comment|// Tez requires us to use RPC for the query plan
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_RPC_QUERY_PLAN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// We require the use of recursive input dirs for union processing
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_HADOOP_SUPPORTS_SUBDIRECTORIES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
name|ForwardWalker
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
name|GenTezUtils
operator|.
name|getUtils
argument_list|()
operator|.
name|resetSequenceNumber
argument_list|()
expr_stmt|;
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
name|GenTezWork
name|genTezWork
init|=
operator|new
name|GenTezWork
argument_list|(
name|GenTezUtils
operator|.
name|getUtils
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"Split Work - ReduceSink"
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|genTezWork
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"No more walking on ReduceSink-MapJoin"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|ReduceSinkMapJoinProc
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
literal|"Split Work + Move/Merge - FileSink"
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
name|CompositeProcessor
argument_list|(
operator|new
name|FileSinkProcessor
argument_list|()
argument_list|,
name|genTezWork
argument_list|)
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"Handle Potential Analyze Command"
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
name|ProcessAnalyzeTable
argument_list|(
name|GenTezUtils
operator|.
name|getUtils
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"Remember union"
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
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|n
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|s
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenTezProcContext
name|context
init|=
operator|(
name|GenTezProcContext
operator|)
name|procCtx
decl_stmt|;
name|UnionOperator
name|union
init|=
operator|(
name|UnionOperator
operator|)
name|n
decl_stmt|;
comment|// simply need to remember that we've seen a union.
name|context
operator|.
name|currentUnionOperators
operator|.
name|add
argument_list|(
name|union
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
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
name|GenTezWorkWalker
argument_list|(
name|disp
argument_list|,
name|procCtx
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
comment|// we need to clone some operator plans and remove union operators still
for|for
control|(
name|BaseWork
name|w
range|:
name|procCtx
operator|.
name|workWithUnionOperators
control|)
block|{
name|GenTezUtils
operator|.
name|getUtils
argument_list|()
operator|.
name|removeUnionOperators
argument_list|(
name|conf
argument_list|,
name|procCtx
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
comment|// finally make sure the file sink operators are set up right
for|for
control|(
name|FileSinkOperator
name|fileSink
range|:
name|procCtx
operator|.
name|fileSinkSet
control|)
block|{
name|GenTezUtils
operator|.
name|getUtils
argument_list|()
operator|.
name|processFileSink
argument_list|(
name|procCtx
argument_list|,
name|fileSink
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|BaseWork
argument_list|>
name|all
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
name|all
control|)
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
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
name|PhysicalContext
name|physicalCtx
init|=
operator|new
name|PhysicalContext
argument_list|(
name|conf
argument_list|,
name|pCtx
argument_list|,
name|pCtx
operator|.
name|getContext
argument_list|()
argument_list|,
name|rootTasks
argument_list|,
name|pCtx
operator|.
name|getFetchTask
argument_list|()
argument_list|)
decl_stmt|;
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
name|HIVE_VECTORIZATION_ENABLED
argument_list|)
condition|)
block|{
operator|(
operator|new
name|Vectorizer
argument_list|()
operator|)
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
literal|"none"
operator|.
name|equalsIgnoreCase
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTAGEIDREARRANGE
argument_list|)
argument_list|)
condition|)
block|{
operator|(
operator|new
name|StageIDsRearranger
argument_list|()
operator|)
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
end_class

end_unit

