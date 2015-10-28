begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|FilterOperator
import|;
end_import

begin_import
import|import
name|org
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
name|GroupByOperator
import|;
end_import

begin_import
import|import
name|org
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
name|ScriptOperator
import|;
end_import

begin_import
import|import
name|org
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
name|SelectOperator
import|;
end_import

begin_import
import|import
name|org
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
name|ConstantPropagateProcCtx
operator|.
name|ConstantPropagateOption
import|;
end_import

begin_import
import|import
name|org
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

begin_comment
comment|/**  * Implementation of one of the rule-based optimization steps. ConstantPropagate traverse the DAG  * from root to child. For each conditional expression, process as follows:  *  * 1. Fold constant expression: if the expression is a UDF and all parameters are constant.  *  * 2. Shortcut expression: if the expression is a logical operator and it can be shortcut by  * some constants of its parameters.  *  * 3. Propagate expression: if the expression is an assignment like column=constant, the expression  * will be propagate to parents to see if further folding operation is possible.  */
end_comment

begin_class
specifier|public
class|class
name|ConstantPropagate
implements|implements
name|Transform
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
name|ConstantPropagate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|private
name|ConstantPropagateOption
name|constantPropagateOption
decl_stmt|;
specifier|public
name|ConstantPropagate
parameter_list|()
block|{
name|this
argument_list|(
name|ConstantPropagateOption
operator|.
name|FULL
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantPropagate
parameter_list|(
name|ConstantPropagateOption
name|option
parameter_list|)
block|{
name|this
operator|.
name|constantPropagateOption
operator|=
name|option
expr_stmt|;
block|}
comment|/**    * Transform the query tree.    *    * @param pactx    *        the current parse context    */
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pactx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|pGraphContext
operator|=
name|pactx
expr_stmt|;
comment|// generate pruned column list for all relevant operators
name|ConstantPropagateProcCtx
name|cppCtx
init|=
operator|new
name|ConstantPropagateProcCtx
argument_list|(
name|constantPropagateOption
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
comment|// generates the plan from the operator tree
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
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getFilterProc
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
literal|"R2"
argument_list|,
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getGroupByProc
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
literal|"R3"
argument_list|,
name|SelectOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getSelectProc
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
literal|"R4"
argument_list|,
name|FileSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getFileSinkProc
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
literal|"R5"
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getReduceSinkProc
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
literal|"R6"
argument_list|,
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getJoinProc
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
literal|"R7"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getTableScanProc
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
literal|"R8"
argument_list|,
name|ScriptOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ConstantPropagateProcFactory
operator|.
name|getStopProc
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
name|ConstantPropagateProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|cppCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|ConstantPropagateWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of operator nodes to start the walking.
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
name|pGraphContext
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
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|opToDelete
range|:
name|cppCtx
operator|.
name|getOpToDelete
argument_list|()
control|)
block|{
if|if
condition|(
name|opToDelete
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
operator|||
name|opToDelete
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error pruning operator "
operator|+
name|opToDelete
operator|+
literal|". It should have only 1 parent."
argument_list|)
throw|;
block|}
name|opToDelete
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|opToDelete
argument_list|)
expr_stmt|;
block|}
name|cppCtx
operator|.
name|getOpToDelete
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|pGraphContext
return|;
block|}
comment|/**    * Walks the op tree in root first order.    */
specifier|public
specifier|static
class|class
name|ConstantPropagateWalker
extends|extends
name|DefaultGraphWalker
block|{
specifier|public
name|ConstantPropagateWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|Node
argument_list|>
name|parents
init|=
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|parents
operator|==
literal|null
operator|)
operator|||
name|getDispatchedList
argument_list|()
operator|.
name|containsAll
argument_list|(
name|parents
argument_list|)
condition|)
block|{
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
comment|// all children are done or no need to walk the children
name|dispatch
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|toWalk
operator|.
name|removeAll
argument_list|(
name|parents
argument_list|)
expr_stmt|;
name|toWalk
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|nd
argument_list|)
expr_stmt|;
name|toWalk
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|parents
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// move all the children to the front of queue
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|nd
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
name|toWalk
operator|.
name|removeAll
argument_list|(
name|children
argument_list|)
expr_stmt|;
name|toWalk
operator|.
name|addAll
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

