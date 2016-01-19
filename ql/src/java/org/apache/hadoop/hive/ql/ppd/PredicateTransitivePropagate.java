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
name|HashMap
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
name|LevelOrderWalker
import|;
end_import

begin_import
import|import
name|org
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
name|ExprNodeDescUtils
import|;
end_import

begin_import
import|import
name|org
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
comment|/**  * propagates filters to other aliases based on join condition  */
end_comment

begin_class
specifier|public
class|class
name|PredicateTransitivePropagate
extends|extends
name|Transform
block|{
specifier|private
name|ParseContext
name|pGraphContext
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
name|pGraphContext
operator|=
name|pctx
expr_stmt|;
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
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
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
name|JoinTransitive
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|TransitiveContext
name|context
init|=
operator|new
name|TransitiveContext
argument_list|()
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
name|LevelOrderWalker
argument_list|(
name|disp
argument_list|,
literal|2
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
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
name|Map
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|newFilters
init|=
name|context
operator|.
name|getNewfilters
argument_list|()
decl_stmt|;
comment|// insert new filter between RS and parent of RS
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|entry
range|:
name|newFilters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ReduceSinkOperator
name|reducer
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
init|=
name|reducer
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|parent
operator|instanceof
name|FilterOperator
condition|)
block|{
name|exprs
operator|=
name|ExprNodeDescUtils
operator|.
name|split
argument_list|(
operator|(
operator|(
name|FilterOperator
operator|)
name|parent
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
argument_list|,
name|exprs
argument_list|)
expr_stmt|;
name|ExprNodeDesc
name|merged
init|=
name|ExprNodeDescUtils
operator|.
name|mergePredicates
argument_list|(
name|exprs
argument_list|)
decl_stmt|;
operator|(
operator|(
name|FilterOperator
operator|)
name|parent
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setPredicate
argument_list|(
name|merged
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ExprNodeDesc
name|merged
init|=
name|ExprNodeDescUtils
operator|.
name|mergePredicates
argument_list|(
name|exprs
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
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
name|newFilter
init|=
name|createFilter
argument_list|(
name|reducer
argument_list|,
name|parent
argument_list|,
name|parentRS
argument_list|,
name|merged
argument_list|)
decl_stmt|;
block|}
block|}
return|return
name|pGraphContext
return|;
block|}
comment|// insert filter operator between target(child) and input(parent)
specifier|private
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
operator|new
name|FilterDesc
argument_list|(
name|filterExpr
argument_list|,
literal|false
argument_list|)
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
name|TransitiveContext
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|CommonJoinOperator
argument_list|,
name|int
index|[]
index|[]
argument_list|>
name|filterPropagates
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|newFilters
decl_stmt|;
specifier|public
name|TransitiveContext
parameter_list|()
block|{
name|filterPropagates
operator|=
operator|new
name|HashMap
argument_list|<
name|CommonJoinOperator
argument_list|,
name|int
index|[]
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|newFilters
operator|=
operator|new
name|HashMap
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|CommonJoinOperator
argument_list|,
name|int
index|[]
index|[]
argument_list|>
name|getFilterPropagates
parameter_list|()
block|{
return|return
name|filterPropagates
return|;
block|}
specifier|public
name|Map
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getNewfilters
parameter_list|()
block|{
return|return
name|newFilters
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|JoinTransitive
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
name|FilterOperator
name|filter
init|=
operator|(
name|FilterOperator
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
literal|3
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
name|TransitiveContext
name|context
init|=
operator|(
name|TransitiveContext
operator|)
name|procCtx
decl_stmt|;
name|Map
argument_list|<
name|CommonJoinOperator
argument_list|,
name|int
index|[]
index|[]
argument_list|>
name|filterPropagates
init|=
name|context
operator|.
name|getFilterPropagates
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|newFilters
init|=
name|context
operator|.
name|getNewfilters
argument_list|()
decl_stmt|;
name|int
index|[]
index|[]
name|targets
init|=
name|filterPropagates
operator|.
name|get
argument_list|(
name|join
argument_list|)
decl_stmt|;
if|if
condition|(
name|targets
operator|==
literal|null
condition|)
block|{
name|filterPropagates
operator|.
name|put
argument_list|(
name|join
argument_list|,
name|targets
operator|=
name|getTargets
argument_list|(
name|join
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|ExprNodeDesc
name|predicate
init|=
name|filter
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|replaced
init|=
name|ExprNodeDescUtils
operator|.
name|replace
argument_list|(
name|predicate
argument_list|,
name|sourceKeys
argument_list|,
name|targetKeys
argument_list|)
decl_stmt|;
if|if
condition|(
name|replaced
operator|!=
literal|null
operator|&&
operator|!
name|filterExists
argument_list|(
name|target
argument_list|,
name|replaced
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|prev
init|=
name|newFilters
operator|.
name|get
argument_list|(
name|target
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|==
literal|null
condition|)
block|{
name|newFilters
operator|.
name|put
argument_list|(
name|target
argument_list|,
name|ExprNodeDescUtils
operator|.
name|split
argument_list|(
name|replaced
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ExprNodeDescUtils
operator|.
name|split
argument_list|(
name|replaced
argument_list|,
name|prev
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
comment|// check same filter exists already
specifier|private
name|boolean
name|filterExists
parameter_list|(
name|ReduceSinkOperator
name|target
parameter_list|,
name|ExprNodeDesc
name|replaced
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
init|=
name|target
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
init|;
name|operator
operator|instanceof
name|FilterOperator
condition|;
name|operator
operator|=
name|operator
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
control|)
block|{
name|ExprNodeDesc
name|predicate
init|=
operator|(
operator|(
name|FilterOperator
operator|)
name|operator
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
if|if
condition|(
name|ExprNodeDescUtils
operator|.
name|containsPredicate
argument_list|(
name|predicate
argument_list|,
name|replaced
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
comment|// calculate filter propagation directions for each alias
comment|// L<->R for inner/semi join, L->R for left outer join, R->L for right outer join
specifier|public
specifier|static
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
name|left
argument_list|,
name|right
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
name|right
argument_list|,
name|left
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
argument_list|,
name|pos
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
parameter_list|,
name|int
name|pos
parameter_list|)
block|{
name|values
operator|.
name|remove
argument_list|(
name|pos
argument_list|)
expr_stmt|;
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

