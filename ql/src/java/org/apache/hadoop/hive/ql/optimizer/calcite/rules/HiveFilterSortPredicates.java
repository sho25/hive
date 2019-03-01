begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|calcite
operator|.
name|rules
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptRuleCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexDynamicParam
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexFieldAccess
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexInputRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexShuttle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexVisitorImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
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
name|calcite
operator|.
name|stats
operator|.
name|FilterSelectivityEstimator
import|;
end_import

begin_import
import|import
name|org
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
name|calcite
operator|.
name|stats
operator|.
name|HiveRelMdSize
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

begin_comment
comment|/**  * Rule that sorts conditions in a filter predicate to accelerate query processing  * based on selectivity and compute cost. Currently it is not applied recursively,  * i.e., it is only applied to top predicates in the condition.  */
end_comment

begin_class
specifier|public
class|class
name|HiveFilterSortPredicates
extends|extends
name|RelOptRule
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
name|HiveFilterSortPredicates
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|noColsMissingStats
decl_stmt|;
specifier|public
name|HiveFilterSortPredicates
parameter_list|(
name|AtomicInteger
name|noColsMissingStats
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|noColsMissingStats
operator|=
name|noColsMissingStats
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HiveRulesRegistry
name|registry
init|=
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|HiveRulesRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// If this operator has been visited already by the rule,
comment|// we do not need to apply the optimization
if|if
condition|(
name|registry
operator|!=
literal|null
operator|&&
name|registry
operator|.
name|getVisited
argument_list|(
name|this
argument_list|)
operator|.
name|contains
argument_list|(
name|filter
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
try|try
block|{
specifier|final
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|RelNode
name|input
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Register that we have visited this operator in this rule
name|HiveRulesRegistry
name|registry
init|=
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|HiveRulesRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|registry
operator|!=
literal|null
condition|)
block|{
name|registry
operator|.
name|registerVisited
argument_list|(
name|this
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
specifier|final
name|RexNode
name|originalCond
init|=
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
specifier|final
name|RexSortPredicatesShuttle
name|sortPredicatesShuttle
init|=
operator|new
name|RexSortPredicatesShuttle
argument_list|(
name|input
argument_list|,
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getMetadataQuery
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|newCond
init|=
name|originalCond
operator|.
name|accept
argument_list|(
name|sortPredicatesShuttle
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|sortPredicatesShuttle
operator|.
name|modified
condition|)
block|{
comment|// We are done, bail out
return|return;
block|}
comment|// We register the new filter so we do not fire the rule on it again
specifier|final
name|Filter
name|newFilter
init|=
name|filter
operator|.
name|copy
argument_list|(
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|input
argument_list|,
name|newCond
argument_list|)
decl_stmt|;
if|if
condition|(
name|registry
operator|!=
literal|null
condition|)
block|{
name|registry
operator|.
name|registerVisited
argument_list|(
name|this
argument_list|,
name|newFilter
argument_list|)
expr_stmt|;
block|}
name|call
operator|.
name|transformTo
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|noColsMissingStats
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Missing column stats (see previous messages), skipping sort predicates in filter expressions in CBO"
argument_list|)
expr_stmt|;
name|noColsMissingStats
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
comment|/**    * If the expression is an AND/OR, it will sort predicates accordingly    * to maximize performance.    */
specifier|private
specifier|static
class|class
name|RexSortPredicatesShuttle
extends|extends
name|RexShuttle
block|{
specifier|private
name|FilterSelectivityEstimator
name|selectivityEstimator
decl_stmt|;
specifier|private
name|boolean
name|modified
decl_stmt|;
specifier|private
name|RexSortPredicatesShuttle
parameter_list|(
name|RelNode
name|inputRel
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
name|selectivityEstimator
operator|=
operator|new
name|FilterSelectivityEstimator
argument_list|(
name|inputRel
argument_list|,
name|mq
argument_list|)
expr_stmt|;
name|modified
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitCall
parameter_list|(
specifier|final
name|RexCall
name|call
parameter_list|)
block|{
switch|switch
condition|(
name|call
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|AND
case|:
name|List
argument_list|<
name|RexNode
argument_list|>
name|newAndOperands
init|=
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|pred
lambda|->
operator|new
name|Pair
argument_list|<>
argument_list|(
name|pred
argument_list|,
name|rankingAnd
argument_list|(
name|pred
argument_list|)
argument_list|)
argument_list|)
operator|.
name|sorted
argument_list|(
name|Comparator
operator|.
name|comparing
argument_list|(
name|Pair
operator|::
name|getValue
argument_list|,
name|Comparator
operator|.
name|nullsLast
argument_list|(
name|Double
operator|::
name|compare
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|Pair
operator|::
name|getKey
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|equals
argument_list|(
name|newAndOperands
argument_list|)
condition|)
block|{
name|modified
operator|=
literal|true
expr_stmt|;
return|return
name|call
operator|.
name|clone
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|,
name|newAndOperands
argument_list|)
return|;
block|}
break|break;
case|case
name|OR
case|:
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOrOperands
init|=
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|pred
lambda|->
operator|new
name|Pair
argument_list|<>
argument_list|(
name|pred
argument_list|,
name|rankingOr
argument_list|(
name|pred
argument_list|)
argument_list|)
argument_list|)
operator|.
name|sorted
argument_list|(
name|Comparator
operator|.
name|comparing
argument_list|(
name|Pair
operator|::
name|getValue
argument_list|,
name|Comparator
operator|.
name|nullsLast
argument_list|(
name|Double
operator|::
name|compare
argument_list|)
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|Pair
operator|::
name|getKey
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|equals
argument_list|(
name|newOrOperands
argument_list|)
condition|)
block|{
name|modified
operator|=
literal|true
expr_stmt|;
return|return
name|call
operator|.
name|clone
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|,
name|newOrOperands
argument_list|)
return|;
block|}
break|break;
block|}
return|return
name|call
return|;
block|}
comment|/**      * Nodes in an AND clause are sorted by a rank value calculated as:      * rank = (selectivity - 1) / cost per tuple      * The intuition is that more selective/cheaper conditions should be evaluated      * first in the AND clause, since FALSE will end the evaluation.      */
specifier|private
name|Double
name|rankingAnd
parameter_list|(
name|RexNode
name|e
parameter_list|)
block|{
name|Double
name|selectivity
init|=
name|selectivityEstimator
operator|.
name|estimateSelectivity
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|selectivity
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Double
name|costPerTuple
init|=
name|costPerTuple
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|costPerTuple
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
name|selectivity
operator|-
literal|1d
operator|)
operator|/
name|costPerTuple
return|;
block|}
comment|/**      * Nodes in an OR clause are sorted by a rank value calculated as:      * rank = (-selectivity) / cost per tuple      * The intuition is that less selective/cheaper conditions should be evaluated      * first in the OR clause, since TRUE will end the evaluation.      */
specifier|private
name|Double
name|rankingOr
parameter_list|(
name|RexNode
name|e
parameter_list|)
block|{
name|Double
name|selectivity
init|=
name|selectivityEstimator
operator|.
name|estimateSelectivity
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|selectivity
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Double
name|costPerTuple
init|=
name|costPerTuple
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|costPerTuple
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|-
name|selectivity
operator|/
name|costPerTuple
return|;
block|}
specifier|private
name|Double
name|costPerTuple
parameter_list|(
name|RexNode
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|accept
argument_list|(
operator|new
name|RexFunctionCost
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * The cost of a call expression e is computed as:    * cost(e) = functionCost + sum_1..n(byteSize(o_i) + cost(o_i))    * with the call having operands i in 1..n.    */
specifier|private
specifier|static
class|class
name|RexFunctionCost
extends|extends
name|RexVisitorImpl
argument_list|<
name|Double
argument_list|>
block|{
specifier|private
name|RexFunctionCost
parameter_list|()
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
if|if
condition|(
operator|!
name|deep
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Double
name|cost
init|=
literal|0.d
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|call
operator|.
name|operands
control|)
block|{
name|Double
name|operandCost
init|=
name|operand
operator|.
name|accept
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|operandCost
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|cost
operator|+=
name|operandCost
expr_stmt|;
name|Double
name|size
init|=
name|HiveRelMdSize
operator|.
name|averageTypeSize
argument_list|(
name|operand
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|cost
operator|+=
name|size
expr_stmt|;
block|}
return|return
name|cost
operator|+
name|functionCost
argument_list|(
name|call
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Double
name|functionCost
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
switch|switch
condition|(
name|call
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|EQUALS
case|:
case|case
name|NOT_EQUALS
case|:
case|case
name|LESS_THAN
case|:
case|case
name|GREATER_THAN
case|:
case|case
name|LESS_THAN_OR_EQUAL
case|:
case|case
name|GREATER_THAN_OR_EQUAL
case|:
case|case
name|IS_NOT_NULL
case|:
case|case
name|IS_NULL
case|:
case|case
name|IS_TRUE
case|:
case|case
name|IS_NOT_TRUE
case|:
case|case
name|IS_FALSE
case|:
case|case
name|IS_NOT_FALSE
case|:
return|return
literal|1d
return|;
case|case
name|BETWEEN
case|:
return|return
literal|3d
return|;
case|case
name|IN
case|:
return|return
literal|2d
operator|*
operator|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
return|;
case|case
name|AND
case|:
case|case
name|OR
case|:
return|return
literal|1d
operator|*
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
return|;
case|case
name|CAST
case|:
comment|// This heuristic represents that CAST operation is 8 times more expensive
comment|// than a comparison operation such as EQUALS, NOT_EQUALS, etc.
return|return
literal|8d
return|;
default|default:
comment|// By default, we give this heuristic value to unrecognized functions.
comment|// The idea is that those functions will be more expensive to evaluate
comment|// than the simple functions considered above.
comment|// TODO: Add more functions/improve the heuristic after running additional experiments.
return|return
literal|32d
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Double
name|visitInputRef
parameter_list|(
name|RexInputRef
name|inputRef
parameter_list|)
block|{
return|return
literal|0d
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|visitFieldAccess
parameter_list|(
name|RexFieldAccess
name|fieldAccess
parameter_list|)
block|{
return|return
literal|0d
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
return|return
literal|0d
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|visitDynamicParam
parameter_list|(
name|RexDynamicParam
name|dynamicParam
parameter_list|)
block|{
return|return
literal|0d
return|;
block|}
block|}
block|}
end_class

end_unit

