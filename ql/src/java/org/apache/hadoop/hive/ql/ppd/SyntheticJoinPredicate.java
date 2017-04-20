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
name|ppd
package|;
end_package

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
name|HashSet
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
name|parse
operator|.
name|SemiJoinHint
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
name|PreOrderOnceWalker
import|;
end_import

begin_import
import|import
name|org
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
name|Transform
import|;
end_import

begin_import
import|import
name|org
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
name|ExprNodeDynamicListDesc
import|;
end_import

begin_import
import|import
name|org
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
name|FilterDesc
import|;
end_import

begin_import
import|import
name|org
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
name|JoinCondDesc
import|;
end_import

begin_import
import|import
name|org
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * creates synthetic predicates that represent "IN (keylist other table)"  */
end_comment

begin_class
specifier|public
class|class
name|SyntheticJoinPredicate
extends|extends
name|Transform
block|{
specifier|private
specifier|static
specifier|transient
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SyntheticJoinPredicate
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|boolean
name|enabled
init|=
literal|false
decl_stmt|;
name|String
name|queryEngine
init|=
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryEngine
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
operator|&&
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_DYNAMIC_PARTITION_PRUNING
argument_list|)
condition|)
block|{
name|enabled
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|queryEngine
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
operator|&&
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING
argument_list|)
operator|)
condition|)
block|{
name|enabled
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
name|pctx
return|;
block|}
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
literal|"("
operator|+
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
literal|".*"
operator|+
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%)"
argument_list|)
argument_list|,
operator|new
name|JoinSynthetic
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|SyntheticContext
name|context
init|=
operator|new
name|SyntheticContext
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
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
name|context
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|PreOrderOnceWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of top op nodes
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
name|pctx
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
return|return
name|pctx
return|;
block|}
comment|// insert filter operator between target(child) and input(parent)
specifier|private
specifier|static
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
name|createFilter
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|target
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|RowSchema
name|parentRS
parameter_list|,
name|ExprNodeDesc
name|filterExpr
parameter_list|)
block|{
name|FilterDesc
name|filterDesc
init|=
operator|new
name|FilterDesc
argument_list|(
name|filterExpr
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|filterDesc
operator|.
name|setSyntheticJoinPredicate
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
name|filter
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|parent
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
name|filterDesc
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|parentRS
operator|.
name|getSignature
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|filter
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|filter
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|parent
operator|.
name|replaceChild
argument_list|(
name|target
argument_list|,
name|filter
argument_list|)
expr_stmt|;
name|target
operator|.
name|replaceParent
argument_list|(
name|parent
argument_list|,
name|filter
argument_list|)
expr_stmt|;
return|return
name|filter
return|;
block|}
specifier|private
specifier|static
class|class
name|SyntheticContext
implements|implements
name|NodeProcessorCtx
block|{
name|ParseContext
name|parseContext
decl_stmt|;
specifier|public
name|SyntheticContext
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|)
block|{
name|parseContext
operator|=
name|pCtx
expr_stmt|;
block|}
specifier|public
name|ParseContext
name|getParseContext
parameter_list|()
block|{
return|return
name|parseContext
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|JoinSynthetic
implements|implements
name|NodeProcessor
block|{
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
name|procCtx
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
name|CommonJoinOperator
argument_list|<
name|JoinDesc
argument_list|>
name|join
init|=
operator|(
name|CommonJoinOperator
argument_list|<
name|JoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|ReduceSinkOperator
name|source
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
name|int
name|srcPos
init|=
name|join
operator|.
name|getParentOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SemiJoinHint
argument_list|>
name|hints
init|=
name|join
operator|.
name|getConf
argument_list|()
operator|.
name|getSemiJoinHints
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parents
init|=
name|join
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|int
index|[]
index|[]
name|targets
init|=
name|getTargets
argument_list|(
name|join
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
name|source
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RowSchema
name|parentRS
init|=
name|parent
operator|.
name|getSchema
argument_list|()
decl_stmt|;
comment|// don't generate for null-safes.
if|if
condition|(
name|join
operator|.
name|getConf
argument_list|()
operator|.
name|getNullSafes
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|boolean
name|b
range|:
name|join
operator|.
name|getConf
argument_list|()
operator|.
name|getNullSafes
argument_list|()
control|)
block|{
if|if
condition|(
name|b
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
for|for
control|(
name|int
name|targetPos
range|:
name|targets
index|[
name|srcPos
index|]
control|)
block|{
if|if
condition|(
name|srcPos
operator|==
name|targetPos
condition|)
block|{
continue|continue;
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
literal|"Synthetic predicate: "
operator|+
name|srcPos
operator|+
literal|" --> "
operator|+
name|targetPos
argument_list|)
expr_stmt|;
block|}
name|ReduceSinkOperator
name|target
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parents
operator|.
name|get
argument_list|(
name|targetPos
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sourceKeys
init|=
name|source
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|targetKeys
init|=
name|target
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceKeys
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
continue|continue;
block|}
name|ExprNodeDesc
name|syntheticExpr
init|=
literal|null
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
name|sourceKeys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|inArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|inArgs
operator|.
name|add
argument_list|(
name|sourceKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ExprNodeDynamicListDesc
name|dynamicExpr
init|=
operator|new
name|ExprNodeDynamicListDesc
argument_list|(
name|targetKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|target
argument_list|,
name|i
argument_list|,
name|hints
argument_list|)
decl_stmt|;
name|inArgs
operator|.
name|add
argument_list|(
name|dynamicExpr
argument_list|)
expr_stmt|;
name|ExprNodeDesc
name|syntheticInExpr
init|=
name|ExprNodeGenericFuncDesc
operator|.
name|newInstance
argument_list|(
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"in"
argument_list|)
operator|.
name|getGenericUDF
argument_list|()
argument_list|,
name|inArgs
argument_list|)
decl_stmt|;
if|if
condition|(
name|syntheticExpr
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|andArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|andArgs
operator|.
name|add
argument_list|(
name|syntheticExpr
argument_list|)
expr_stmt|;
name|andArgs
operator|.
name|add
argument_list|(
name|syntheticInExpr
argument_list|)
expr_stmt|;
name|syntheticExpr
operator|=
name|ExprNodeGenericFuncDesc
operator|.
name|newInstance
argument_list|(
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"and"
argument_list|)
operator|.
name|getGenericUDF
argument_list|()
argument_list|,
name|andArgs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|syntheticExpr
operator|=
name|syntheticInExpr
expr_stmt|;
block|}
block|}
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
name|newFilter
init|=
name|createFilter
argument_list|(
name|source
argument_list|,
name|parent
argument_list|,
name|parentRS
argument_list|,
name|syntheticExpr
argument_list|)
decl_stmt|;
name|parent
operator|=
name|newFilter
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|// calculate filter propagation directions for each alias
comment|// L<->R for inner/semi join, L<-R for left outer join, R<-L for right outer
comment|// join
specifier|private
name|int
index|[]
index|[]
name|getTargets
parameter_list|(
name|CommonJoinOperator
argument_list|<
name|JoinDesc
argument_list|>
name|join
parameter_list|)
block|{
name|JoinCondDesc
index|[]
name|conds
init|=
name|join
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
decl_stmt|;
name|int
name|aliases
init|=
name|conds
operator|.
name|length
operator|+
literal|1
decl_stmt|;
name|Vectors
name|vector
init|=
operator|new
name|Vectors
argument_list|(
name|aliases
argument_list|)
decl_stmt|;
for|for
control|(
name|JoinCondDesc
name|cond
range|:
name|conds
control|)
block|{
name|int
name|left
init|=
name|cond
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|int
name|right
init|=
name|cond
operator|.
name|getRight
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|cond
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|JoinDesc
operator|.
name|INNER_JOIN
case|:
case|case
name|JoinDesc
operator|.
name|LEFT_SEMI_JOIN
case|:
name|vector
operator|.
name|add
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
name|vector
operator|.
name|add
argument_list|(
name|right
argument_list|,
name|left
argument_list|)
expr_stmt|;
break|break;
case|case
name|JoinDesc
operator|.
name|LEFT_OUTER_JOIN
case|:
name|vector
operator|.
name|add
argument_list|(
name|right
argument_list|,
name|left
argument_list|)
expr_stmt|;
break|break;
case|case
name|JoinDesc
operator|.
name|RIGHT_OUTER_JOIN
case|:
name|vector
operator|.
name|add
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
break|break;
case|case
name|JoinDesc
operator|.
name|FULL_OUTER_JOIN
case|:
break|break;
block|}
block|}
name|int
index|[]
index|[]
name|result
init|=
operator|new
name|int
index|[
name|aliases
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|aliases
condition|;
name|pos
operator|++
control|)
block|{
comment|// find all targets recursively
name|result
index|[
name|pos
index|]
operator|=
name|vector
operator|.
name|traverse
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Vectors
block|{
specifier|private
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
index|[]
name|vector
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Vectors
parameter_list|(
name|int
name|length
parameter_list|)
block|{
name|vector
operator|=
operator|new
name|Set
index|[
name|length
index|]
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|int
name|from
parameter_list|,
name|int
name|to
parameter_list|)
block|{
if|if
condition|(
name|vector
index|[
name|from
index|]
operator|==
literal|null
condition|)
block|{
name|vector
index|[
name|from
index|]
operator|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|vector
index|[
name|from
index|]
operator|.
name|add
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|traverse
parameter_list|(
name|int
name|pos
parameter_list|)
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|targets
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|traverse
argument_list|(
name|targets
argument_list|,
name|pos
argument_list|)
expr_stmt|;
return|return
name|toArray
argument_list|(
name|targets
argument_list|)
return|;
block|}
specifier|private
name|int
index|[]
name|toArray
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|values
parameter_list|)
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
name|int
index|[]
name|result
init|=
operator|new
name|int
index|[
name|values
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|value
range|:
name|values
control|)
block|{
name|result
index|[
name|index
operator|++
index|]
operator|=
name|value
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|traverse
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|targets
parameter_list|,
name|int
name|pos
parameter_list|)
block|{
if|if
condition|(
name|vector
index|[
name|pos
index|]
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|int
name|target
range|:
name|vector
index|[
name|pos
index|]
control|)
block|{
if|if
condition|(
name|targets
operator|.
name|add
argument_list|(
name|target
argument_list|)
condition|)
block|{
name|traverse
argument_list|(
name|targets
argument_list|,
name|target
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

