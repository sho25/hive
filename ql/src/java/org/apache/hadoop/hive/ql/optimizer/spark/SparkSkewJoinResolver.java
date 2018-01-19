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
name|Collections
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
name|ql
operator|.
name|exec
operator|.
name|CommonJoinOperator
import|;
end_import

begin_import
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
name|lib
operator|.
name|DefaultGraphWalker
import|;
end_import

begin_import
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
name|PreOrderWalker
import|;
end_import

begin_import
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
name|PhysicalPlanResolver
import|;
end_import

begin_import
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
name|SkewJoinResolver
import|;
end_import

begin_import
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
name|ReduceWork
import|;
end_import

begin_import
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

begin_comment
comment|/**  * Spark version of SkewJoinResolver.  */
end_comment

begin_class
specifier|public
class|class
name|SparkSkewJoinResolver
implements|implements
name|PhysicalPlanResolver
block|{
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SparkSkewJoinProcFactory
operator|.
name|getVisitedJoinOp
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|SparkSkewJoinTaskDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
comment|// since we may split current task, use a pre-order walker
name|GraphWalker
name|ogw
init|=
operator|new
name|PreOrderWalker
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
name|pctx
operator|.
name|getRootTasks
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
return|return
name|pctx
return|;
block|}
class|class
name|SparkSkewJoinTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|public
name|SparkSkewJoinTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
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
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|task
operator|instanceof
name|SparkTask
condition|)
block|{
name|SparkWork
name|sparkWork
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
name|SparkSkewJoinProcCtx
name|skewJoinProcCtx
init|=
operator|new
name|SparkSkewJoinProcCtx
argument_list|(
name|task
argument_list|,
name|physicalContext
operator|.
name|getParseContext
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"R1"
argument_list|,
name|CommonJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|SparkSkewJoinProcFactory
operator|.
name|getJoinProc
argument_list|()
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|SparkSkewJoinProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|skewJoinProcCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
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
comment|// since we may need to split the task, let's walk the graph bottom-up
name|List
argument_list|<
name|ReduceWork
argument_list|>
name|reduceWorkList
init|=
name|sparkWork
operator|.
name|getAllReduceWork
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|reverse
argument_list|(
name|reduceWorkList
argument_list|)
expr_stmt|;
for|for
control|(
name|ReduceWork
name|reduceWork
range|:
name|reduceWorkList
control|)
block|{
name|topNodes
operator|.
name|add
argument_list|(
name|reduceWork
operator|.
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
name|skewJoinProcCtx
operator|.
name|getReducerToReduceWork
argument_list|()
operator|.
name|put
argument_list|(
name|reduceWork
operator|.
name|getReducer
argument_list|()
argument_list|,
name|reduceWork
argument_list|)
expr_stmt|;
block|}
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
return|return
literal|null
return|;
block|}
specifier|public
name|PhysicalContext
name|getPhysicalContext
parameter_list|()
block|{
return|return
name|physicalContext
return|;
block|}
specifier|public
name|void
name|setPhysicalContext
parameter_list|(
name|PhysicalContext
name|physicalContext
parameter_list|)
block|{
name|this
operator|.
name|physicalContext
operator|=
name|physicalContext
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|SparkSkewJoinProcCtx
extends|extends
name|SkewJoinResolver
operator|.
name|SkewJoinProcCtx
block|{
comment|// need a map from the reducer to the corresponding ReduceWork
specifier|private
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|ReduceWork
argument_list|>
name|reducerToReduceWork
decl_stmt|;
specifier|public
name|SparkSkewJoinProcCtx
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|super
argument_list|(
name|task
argument_list|,
name|parseCtx
argument_list|)
expr_stmt|;
name|reducerToReduceWork
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|ReduceWork
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|ReduceWork
argument_list|>
name|getReducerToReduceWork
parameter_list|()
block|{
return|return
name|reducerToReduceWork
return|;
block|}
block|}
block|}
end_class

end_unit

