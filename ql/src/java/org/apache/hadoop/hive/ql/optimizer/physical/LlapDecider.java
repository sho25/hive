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
import|import static
name|org
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
name|LlapDecider
operator|.
name|LlapMode
operator|.
name|all
import|;
end_import

begin_import
import|import static
name|org
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
name|LlapDecider
operator|.
name|LlapMode
operator|.
name|auto
import|;
end_import

begin_import
import|import static
name|org
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
name|LlapDecider
operator|.
name|LlapMode
operator|.
name|map
import|;
end_import

begin_import
import|import static
name|org
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
name|LlapDecider
operator|.
name|LlapMode
operator|.
name|none
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|FunctionInfo
import|;
end_import

begin_import
import|import
name|org
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
name|FunctionRegistry
import|;
end_import

begin_import
import|import
name|org
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedInputFormatInterface
import|;
end_import

begin_import
import|import
name|org
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
name|TaskGraphWalker
import|;
end_import

begin_import
import|import
name|org
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
name|AggregationDesc
import|;
end_import

begin_import
import|import
name|org
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
name|ExprNodeGenericFuncDesc
import|;
end_import

begin_import
import|import
name|org
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
name|Statistics
import|;
end_import

begin_import
import|import
name|org
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
comment|/**  * LlapDecider takes care of tagging certain vertices in the execution  * graph as "llap", which in turn causes them to be submitted to an  * llap daemon instead of a regular yarn container.  *  * The actual algoritm used is driven by LLAP_EXECUTION_MODE. "all",  * "none" and "map" mechanically tag those elements. "auto" tries to  * be smarter by looking for suitable vertices.  *  * Regardless of the algorithm used, it's always ensured that there's  * not user code that will be sent to the daemon (ie.: script  * operators, temporary functions, etc)  */
end_comment

begin_class
specifier|public
class|class
name|LlapDecider
implements|implements
name|PhysicalPlanResolver
block|{
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LlapDecider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|public
enum|enum
name|LlapMode
block|{
name|map
block|,
comment|// map operators only
name|all
block|,
comment|// all operators
name|none
block|,
comment|// no operators
name|auto
comment|// please hive, choose for me
block|}
specifier|private
name|LlapMode
name|mode
decl_stmt|;
class|class
name|LlapDecisionDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
name|PhysicalContext
name|pctx
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|LlapDecisionDispatcher
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|pctx
operator|.
name|getConf
argument_list|()
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
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
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
name|currTask
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
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|handleWork
argument_list|(
name|work
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|handleWork
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|evaluateWork
argument_list|(
name|tezWork
argument_list|,
name|work
argument_list|)
condition|)
block|{
name|convertWork
argument_list|(
name|tezWork
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|convertWork
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|work
operator|.
name|setLlapMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|evaluateWork
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Evaluating work item: "
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// no means no
if|if
condition|(
name|mode
operator|==
name|none
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// first we check if we *can* run in llap. If we need to use
comment|// user code to do so (script/udf) we don't.
if|if
condition|(
operator|!
name|evaluateOperators
argument_list|(
name|work
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"some operators cannot be run in llap"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// --- From here on out we choose whether we *want* to run in llap
comment|// if mode is all just run it
if|if
condition|(
name|mode
operator|==
name|all
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// if map mode run iff work is map work
if|if
condition|(
name|mode
operator|==
name|map
condition|)
block|{
return|return
name|work
operator|instanceof
name|MapWork
return|;
block|}
comment|// --- From here we evaluate the auto mode
assert|assert
name|mode
operator|==
name|auto
assert|;
comment|// if parents aren't in llap neither should the child
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_TREE
argument_list|)
operator|&&
operator|!
name|checkParentsInLlap
argument_list|(
name|tezWork
argument_list|,
name|work
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parent not in llap."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// only vectorized orc input is cached. so there's a reason to
comment|// limit to that for now.
if|if
condition|(
name|work
operator|instanceof
name|MapWork
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_VECTORIZED
argument_list|)
operator|&&
operator|!
name|checkInputsVectorized
argument_list|(
operator|(
name|MapWork
operator|)
name|work
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Inputs not vectorized."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// check if there's at least some degree of stats available
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_STATS
argument_list|)
operator|&&
operator|!
name|checkPartialStatsAvailable
argument_list|(
name|work
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No column stats available."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// now let's take a look at input sizes
name|long
name|maxInput
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
name|LLAP_AUTO_MAX_INPUT
argument_list|)
decl_stmt|;
name|long
name|expectedInput
init|=
name|computeInputSize
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxInput
operator|>=
literal|0
operator|&&
operator|(
name|expectedInput
operator|>
name|maxInput
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Inputs too big (%d> %d)"
argument_list|,
name|expectedInput
argument_list|,
name|maxInput
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// and finally let's check output sizes
name|long
name|maxOutput
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
name|LLAP_AUTO_MAX_OUTPUT
argument_list|)
decl_stmt|;
name|long
name|expectedOutput
init|=
name|computeOutputSize
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxOutput
operator|>=
literal|0
operator|&&
operator|(
name|expectedOutput
operator|>
name|maxOutput
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Outputs too big (%d> %d)"
argument_list|,
name|expectedOutput
argument_list|,
name|maxOutput
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// couldn't convince you otherwise? well then let's llap.
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkExpression
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
name|Deque
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
operator|new
name|LinkedList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|exprs
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|exprs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
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
name|String
operator|.
name|format
argument_list|(
literal|"Checking '%s'"
argument_list|,
name|expr
operator|.
name|getExprString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ExprNodeDesc
name|cur
init|=
name|exprs
operator|.
name|removeFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|cur
operator|==
literal|null
condition|)
continue|continue;
if|if
condition|(
name|cur
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|exprs
operator|.
name|addAll
argument_list|(
name|cur
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cur
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
comment|// getRequiredJars is currently broken (requires init in some cases before you can call it)
comment|// String[] jars = ((ExprNodeGenericFuncDesc)cur).getGenericUDF().getRequiredJars();
comment|// if (jars != null&& !(jars.length == 0)) {
comment|//   LOG.info(String.format("%s requires %s", cur.getExprString(), Joiner.on(", ").join(jars)));
comment|//   return false;
comment|// }
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isBuiltInFuncExpr
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|cur
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not a built-in function: "
operator|+
name|cur
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkAggregator
parameter_list|(
name|AggregationDesc
name|agg
parameter_list|)
throws|throws
name|SemanticException
block|{
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
name|String
operator|.
name|format
argument_list|(
literal|"Checking '%s'"
argument_list|,
name|agg
operator|.
name|getExprString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|boolean
name|result
init|=
name|checkExpressions
argument_list|(
name|agg
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|FunctionInfo
name|fi
init|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|agg
operator|.
name|getGenericUDAFName
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
name|result
operator|&&
operator|(
name|fi
operator|!=
literal|null
operator|)
operator|&&
name|fi
operator|.
name|isNative
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|result
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aggregator is not native: "
operator|+
name|agg
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|checkExpressions
parameter_list|(
name|Collection
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|true
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|exprs
control|)
block|{
name|result
operator|=
name|result
operator|&&
name|checkExpression
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|checkAggregators
parameter_list|(
name|Collection
argument_list|<
name|AggregationDesc
argument_list|>
name|aggs
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|true
decl_stmt|;
try|try
block|{
for|for
control|(
name|AggregationDesc
name|agg
range|:
name|aggs
control|)
block|{
name|result
operator|=
name|result
operator|&&
name|checkAggregator
argument_list|(
name|agg
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception testing aggregators."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|result
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|getRules
parameter_list|()
block|{
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
literal|"No scripts"
argument_list|,
name|ScriptOperator
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
name|c
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
block|{
return|return
operator|new
name|Boolean
argument_list|(
literal|false
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"No user code in fil"
argument_list|,
name|FilterOperator
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
name|c
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
block|{
name|ExprNodeDesc
name|expr
init|=
operator|(
operator|(
name|FilterOperator
operator|)
name|n
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
return|return
operator|new
name|Boolean
argument_list|(
name|checkExpression
argument_list|(
name|expr
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"No user code in gby"
argument_list|,
name|GroupByOperator
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
name|c
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
block|{
name|List
argument_list|<
name|AggregationDesc
argument_list|>
name|aggs
init|=
operator|(
operator|(
name|GroupByOperator
operator|)
name|n
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getAggregators
argument_list|()
decl_stmt|;
return|return
operator|new
name|Boolean
argument_list|(
name|checkAggregators
argument_list|(
name|aggs
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"No user code in select"
argument_list|,
name|SelectOperator
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
name|c
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
operator|(
operator|(
name|SelectOperator
operator|)
name|n
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getColList
argument_list|()
decl_stmt|;
return|return
operator|new
name|Boolean
argument_list|(
name|checkExpressions
argument_list|(
name|exprs
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|opRules
return|;
block|}
specifier|private
name|boolean
name|evaluateOperators
parameter_list|(
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// lets take a look at the operators. we're checking for user
comment|// code in those. we will not run that in llap.
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|getRules
argument_list|()
argument_list|,
literal|null
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
name|topNodes
operator|.
name|addAll
argument_list|(
name|work
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|n
range|:
name|nodeOutput
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeOutput
operator|.
name|get
argument_list|(
name|n
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
operator|(
operator|(
name|Boolean
operator|)
name|nodeOutput
operator|.
name|get
argument_list|(
name|n
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkParentsInLlap
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|BaseWork
name|base
parameter_list|)
block|{
for|for
control|(
name|BaseWork
name|w
range|:
name|tezWork
operator|.
name|getParents
argument_list|(
name|base
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|w
operator|.
name|getLlapMode
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not all parents are run in llap"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkInputsVectorized
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
for|for
control|(
name|String
name|path
range|:
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|PartitionDesc
name|pd
init|=
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|interfaceList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|pd
operator|.
name|getInputFileFormatClass
argument_list|()
operator|.
name|getInterfaces
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|interfaceList
operator|.
name|contains
argument_list|(
name|VectorizedInputFormatInterface
operator|.
name|class
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Input format: "
operator|+
name|pd
operator|.
name|getInputFileFormatClassName
argument_list|()
operator|+
literal|", doesn't provide vectorized input"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkPartialStatsAvailable
parameter_list|(
name|BaseWork
name|base
parameter_list|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|o
range|:
name|base
operator|.
name|getAllRootOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|o
operator|.
name|getStatistics
argument_list|()
operator|.
name|getColumnStatsState
argument_list|()
operator|==
name|Statistics
operator|.
name|State
operator|.
name|NONE
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|long
name|computeEdgeSize
parameter_list|(
name|BaseWork
name|base
parameter_list|,
name|boolean
name|input
parameter_list|)
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|o
range|:
operator|(
name|input
condition|?
name|base
operator|.
name|getAllRootOperators
argument_list|()
else|:
name|base
operator|.
name|getAllLeafOperators
argument_list|()
operator|)
control|)
block|{
if|if
condition|(
name|o
operator|.
name|getStatistics
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// return worst case if unknown
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
name|long
name|currSize
init|=
name|o
operator|.
name|getStatistics
argument_list|()
operator|.
name|getDataSize
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|currSize
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|Long
operator|.
name|MAX_VALUE
operator|-
name|size
operator|)
operator|<
name|currSize
operator|)
condition|)
block|{
comment|// overflow
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
name|size
operator|+=
name|currSize
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
specifier|private
name|long
name|computeInputSize
parameter_list|(
name|BaseWork
name|base
parameter_list|)
block|{
return|return
name|computeEdgeSize
argument_list|(
name|base
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|long
name|computeOutputSize
parameter_list|(
name|BaseWork
name|base
parameter_list|)
block|{
return|return
name|computeEdgeSize
argument_list|(
name|base
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
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
name|this
operator|.
name|physicalContext
operator|=
name|pctx
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|pctx
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|LlapMode
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_EXECUTION_MODE
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"llap mode: "
operator|+
name|this
operator|.
name|mode
argument_list|)
expr_stmt|;
if|if
condition|(
name|mode
operator|==
name|none
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LLAP disabled."
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|// create dispatcher and graph walker
name|Dispatcher
name|disp
init|=
operator|new
name|LlapDecisionDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|TaskGraphWalker
name|ogw
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// get all the tasks nodes from root task
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
comment|// begin to walk through the task tree.
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
block|}
end_class

end_unit

