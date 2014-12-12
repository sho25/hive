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
operator|.
name|spark
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
name|SMBMapJoinOperator
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
name|spark
operator|.
name|SparkTask
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
name|lib
operator|.
name|TypeRule
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
name|physical
operator|.
name|CrossProductCheck
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
name|MetadataOnlyOptimizer
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
name|NullScanOptimizer
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
name|SparkMapJoinResolver
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
name|spark
operator|.
name|SetSparkReducerParallelism
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
name|spark
operator|.
name|SparkMapJoinOptimizer
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
name|spark
operator|.
name|SparkReduceSinkMapJoinProc
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
name|spark
operator|.
name|SparkSkewJoinResolver
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
name|spark
operator|.
name|SparkSortMergeJoinFactory
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
name|spark
operator|.
name|SplitSparkWorkResolver
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
name|GlobalLimitCtx
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
name|parse
operator|.
name|TaskCompiler
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
name|SparkWork
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
comment|/**  * SparkCompiler translates the operator plan into SparkTasks.  *  * Pretty much cloned from TezCompiler.  *  * TODO: need to complete and make it fit to Spark.  */
end_comment

begin_class
specifier|public
class|class
name|SparkCompiler
extends|extends
name|TaskCompiler
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|logger
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkCompiler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|SparkCompiler
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
comment|//    TODO: Need to check if we require the use of recursive input dirs for union processing
comment|//    conf.setBoolean("mapred.input.dir.recursive", true);
comment|//    HiveConf.setBoolVar(conf, ConfVars.HIVE_HADOOP_SUPPORTS_SUBDIRECTORIES, true);
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
comment|// TODO: need to add spark specific optimization.
comment|// Sequence of TableScan operators to be walked
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
extends|extends
name|OperatorDesc
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
name|OptimizeSparkProcContext
name|procCtx
init|=
operator|new
name|OptimizeSparkProcContext
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
literal|"Set parallelism - ReduceSink"
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
name|SetSparkReducerParallelism
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: need to research and verify support convert join to map join optimization.
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
name|SparkMapJoinOptimizer
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
comment|/**    * TODO: need to turn on rules that's commented out and add more if necessary.    */
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
name|GenSparkUtils
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
name|GenSparkWork
name|genSparkWork
init|=
operator|new
name|GenSparkWork
argument_list|(
name|GenSparkUtils
operator|.
name|getUtils
argument_list|()
argument_list|)
decl_stmt|;
name|GenSparkProcContext
name|procCtx
init|=
operator|new
name|GenSparkProcContext
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
name|pCtx
operator|.
name|getTopOps
argument_list|()
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher generates the plan from the operator tree
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
name|genSparkWork
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
name|SparkReduceSinkMapJoinProc
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
name|SparkFileSinkProcessor
argument_list|()
argument_list|,
name|genSparkWork
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
literal|"Handle Analyze Command"
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
name|SparkProcessAnalyzeTable
argument_list|(
name|GenSparkUtils
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
name|GenSparkProcContext
name|context
init|=
operator|(
name|GenSparkProcContext
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
name|GenSparkWorkWalker
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
comment|// ------------------- Second Pass -----------------------
comment|// SMB Join optimizations to add the "localWork" and bucketing data structures to MapWork.
name|opRules
operator|.
name|clear
argument_list|()
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|TypeRule
argument_list|(
name|SMBMapJoinOperator
operator|.
name|class
argument_list|)
argument_list|,
name|SparkSortMergeJoinFactory
operator|.
name|getTableScanMapJoin
argument_list|()
argument_list|)
expr_stmt|;
name|disp
operator|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
name|procCtx
argument_list|)
expr_stmt|;
name|topNodes
operator|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
expr_stmt|;
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
operator|=
operator|new
name|GenSparkWorkWalker
argument_list|(
name|disp
argument_list|,
name|procCtx
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
name|GenSparkUtils
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
name|GenSparkUtils
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
name|SparkTask
condition|)
block|{
name|SparkWork
name|work
init|=
operator|(
operator|(
name|SparkTask
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
comment|// currently all Spark work is on the cluster
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
name|physicalCtx
operator|=
operator|new
name|SplitSparkWorkResolver
argument_list|()
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
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
name|HIVESKEWJOIN
argument_list|)
condition|)
block|{
operator|(
operator|new
name|SparkSkewJoinResolver
argument_list|()
operator|)
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping runtime skew join optimization"
argument_list|)
expr_stmt|;
block|}
name|physicalCtx
operator|=
operator|new
name|SparkMapJoinResolver
argument_list|()
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
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
name|HIVENULLSCANOPTIMIZE
argument_list|)
condition|)
block|{
name|physicalCtx
operator|=
operator|new
name|NullScanOptimizer
argument_list|()
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping null scan query optimization"
argument_list|)
expr_stmt|;
block|}
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
name|HIVEMETADATAONLYQUERIES
argument_list|)
condition|)
block|{
name|physicalCtx
operator|=
operator|new
name|MetadataOnlyOptimizer
argument_list|()
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping metadata only query optimization"
argument_list|)
expr_stmt|;
block|}
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
name|HIVE_CHECK_CROSS_PRODUCT
argument_list|)
condition|)
block|{
name|physicalCtx
operator|=
operator|new
name|CrossProductCheck
argument_list|()
operator|.
name|resolve
argument_list|(
name|physicalCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping cross product analysis"
argument_list|)
expr_stmt|;
block|}
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
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping vectorization"
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
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping stage id rearranger"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
end_class

end_unit

