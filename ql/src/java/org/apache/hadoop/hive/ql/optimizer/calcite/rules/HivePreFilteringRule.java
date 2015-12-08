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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|List
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
name|plan
operator|.
name|RelOptUtil
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
name|core
operator|.
name|RelFactories
operator|.
name|FilterFactory
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
name|TableScan
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
name|RexBuilder
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
name|RexUtil
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
name|sql
operator|.
name|SqlKind
import|;
end_import

begin_import
import|import
name|org
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
name|HiveRelFactories
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveCalciteUtil
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|LinkedHashMultimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|HivePreFilteringRule
extends|extends
name|RelOptRule
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HivePreFilteringRule
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HivePreFilteringRule
name|INSTANCE
init|=
operator|new
name|HivePreFilteringRule
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|FilterFactory
name|filterFactory
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|SqlKind
argument_list|>
name|COMPARISON
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|SqlKind
operator|.
name|EQUALS
argument_list|,
name|SqlKind
operator|.
name|GREATER_THAN_OR_EQUAL
argument_list|,
name|SqlKind
operator|.
name|LESS_THAN_OR_EQUAL
argument_list|,
name|SqlKind
operator|.
name|GREATER_THAN
argument_list|,
name|SqlKind
operator|.
name|LESS_THAN
argument_list|,
name|SqlKind
operator|.
name|NOT_EQUALS
argument_list|)
decl_stmt|;
specifier|private
name|HivePreFilteringRule
parameter_list|()
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
name|filterFactory
operator|=
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
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
specifier|final
name|RelNode
name|filterChild
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// If the filter is already on top of a TableScan,
comment|// we can bail out
if|if
condition|(
name|filterChild
operator|instanceof
name|TableScan
condition|)
block|{
return|return
literal|false
return|;
block|}
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
comment|// 0. Register that we have visited this operator in this rule
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
name|RexBuilder
name|rexBuilder
init|=
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
comment|// 1. Recompose filter possibly by pulling out common elements from DNF
comment|// expressions
name|RexNode
name|topFilterCondition
init|=
name|RexUtil
operator|.
name|pullFactors
argument_list|(
name|rexBuilder
argument_list|,
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
comment|// 2. We extract possible candidates to be pushed down
name|List
argument_list|<
name|RexNode
argument_list|>
name|operandsToPushDown
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|deterministicExprs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|nonDeterministicExprs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|topFilterCondition
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|AND
case|:
name|ImmutableList
argument_list|<
name|RexNode
argument_list|>
name|operands
init|=
name|RexUtil
operator|.
name|flattenAnd
argument_list|(
operator|(
operator|(
name|RexCall
operator|)
name|topFilterCondition
operator|)
operator|.
name|getOperands
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|operandsToPushDownDigest
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|extractedCommonOperands
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|operands
control|)
block|{
if|if
condition|(
name|operand
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|OR
condition|)
block|{
name|extractedCommonOperands
operator|=
name|extractCommonOperands
argument_list|(
name|rexBuilder
argument_list|,
name|operand
argument_list|)
expr_stmt|;
for|for
control|(
name|RexNode
name|extractedExpr
range|:
name|extractedCommonOperands
control|)
block|{
if|if
condition|(
name|operandsToPushDownDigest
operator|.
name|add
argument_list|(
name|extractedExpr
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|operandsToPushDown
operator|.
name|add
argument_list|(
name|extractedExpr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// TODO: Make expr traversal recursive. Extend to traverse inside
comment|// elements of DNF/CNF& extract more deterministic pieces out.
if|if
condition|(
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|operand
argument_list|)
condition|)
block|{
name|deterministicExprs
operator|.
name|add
argument_list|(
name|operand
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nonDeterministicExprs
operator|.
name|add
argument_list|(
name|operand
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Pull out Deterministic exprs from non-deterministic and push down
comment|// deterministic expressions as a separate filter
comment|// NOTE: Hive by convention doesn't pushdown non deterministic expressions
if|if
condition|(
name|nonDeterministicExprs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|RexNode
name|expr
range|:
name|deterministicExprs
control|)
block|{
if|if
condition|(
operator|!
name|operandsToPushDownDigest
operator|.
name|contains
argument_list|(
name|expr
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|operandsToPushDown
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|operandsToPushDownDigest
operator|.
name|add
argument_list|(
name|expr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|topFilterCondition
operator|=
name|RexUtil
operator|.
name|pullFactors
argument_list|(
name|rexBuilder
argument_list|,
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|nonDeterministicExprs
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|OR
case|:
name|operandsToPushDown
operator|=
name|extractCommonOperands
argument_list|(
name|rexBuilder
argument_list|,
name|topFilterCondition
argument_list|)
expr_stmt|;
break|break;
default|default:
return|return;
block|}
comment|// 2. If we did not generate anything for the new predicate, we bail out
if|if
condition|(
name|operandsToPushDown
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// 3. If the new conjuncts are already present in the plan, we bail out
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|newConjuncts
init|=
name|HiveCalciteUtil
operator|.
name|getPredsNotPushedAlready
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|,
name|operandsToPushDown
argument_list|)
decl_stmt|;
if|if
condition|(
name|newConjuncts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// 4. Otherwise, we create a new condition
specifier|final
name|RexNode
name|newChildFilterCondition
init|=
name|RexUtil
operator|.
name|pullFactors
argument_list|(
name|rexBuilder
argument_list|,
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|newConjuncts
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
comment|// 5. We create the new filter that might be pushed down
name|RelNode
name|newChildFilter
init|=
name|filterFactory
operator|.
name|createFilter
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|,
name|newChildFilterCondition
argument_list|)
decl_stmt|;
name|RelNode
name|newTopFilter
init|=
name|filterFactory
operator|.
name|createFilter
argument_list|(
name|newChildFilter
argument_list|,
name|topFilterCondition
argument_list|)
decl_stmt|;
comment|// 6. We register both so we do not fire the rule on them again
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
name|newChildFilter
argument_list|)
expr_stmt|;
name|registry
operator|.
name|registerVisited
argument_list|(
name|this
argument_list|,
name|newTopFilter
argument_list|)
expr_stmt|;
block|}
name|call
operator|.
name|transformTo
argument_list|(
name|newTopFilter
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|RexNode
argument_list|>
name|extractCommonOperands
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|RexNode
name|condition
parameter_list|)
block|{
assert|assert
name|condition
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|OR
assert|;
name|Multimap
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
name|reductionCondition
init|=
name|LinkedHashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Data structure to control whether a certain reference is present in every
comment|// operand
name|Set
argument_list|<
name|String
argument_list|>
name|refsInAllOperands
init|=
literal|null
decl_stmt|;
comment|// 1. We extract the information necessary to create the predicate for the
comment|// new
comment|// filter; currently we support comparison functions, in and between
name|ImmutableList
argument_list|<
name|RexNode
argument_list|>
name|operands
init|=
name|RexUtil
operator|.
name|flattenOr
argument_list|(
operator|(
operator|(
name|RexCall
operator|)
name|condition
operator|)
operator|.
name|getOperands
argument_list|()
argument_list|)
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
name|operands
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|RexNode
name|operand
init|=
name|operands
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|operandCNF
init|=
name|RexUtil
operator|.
name|toCnf
argument_list|(
name|rexBuilder
argument_list|,
name|operand
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|conjunctions
init|=
name|RelOptUtil
operator|.
name|conjunctions
argument_list|(
name|operandCNF
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|refsInCurrentOperand
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|conjunction
range|:
name|conjunctions
control|)
block|{
comment|// We do not know what it is, we bail out for safety
if|if
condition|(
operator|!
operator|(
name|conjunction
operator|instanceof
name|RexCall
operator|)
operator|||
operator|!
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|conjunction
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
name|RexCall
name|conjCall
init|=
operator|(
name|RexCall
operator|)
name|conjunction
decl_stmt|;
name|RexNode
name|ref
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|COMPARISON
operator|.
name|contains
argument_list|(
name|conjCall
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|RexInputRef
operator|&&
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|RexLiteral
condition|)
block|{
name|ref
operator|=
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|RexInputRef
operator|&&
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|RexLiteral
condition|)
block|{
name|ref
operator|=
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We do not know what it is, we bail out for safety
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|conjCall
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|.
name|equals
argument_list|(
name|SqlKind
operator|.
name|IN
argument_list|)
condition|)
block|{
name|ref
operator|=
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|conjCall
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|.
name|equals
argument_list|(
name|SqlKind
operator|.
name|BETWEEN
argument_list|)
condition|)
block|{
name|ref
operator|=
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We do not know what it is, we bail out for safety
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
name|String
name|stringRef
init|=
name|ref
operator|.
name|toString
argument_list|()
decl_stmt|;
name|reductionCondition
operator|.
name|put
argument_list|(
name|stringRef
argument_list|,
name|conjCall
argument_list|)
expr_stmt|;
name|refsInCurrentOperand
operator|.
name|add
argument_list|(
name|stringRef
argument_list|)
expr_stmt|;
block|}
comment|// Updates the references that are present in every operand up till now
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|refsInAllOperands
operator|=
name|refsInCurrentOperand
expr_stmt|;
block|}
else|else
block|{
name|refsInAllOperands
operator|=
name|Sets
operator|.
name|intersection
argument_list|(
name|refsInAllOperands
argument_list|,
name|refsInCurrentOperand
argument_list|)
expr_stmt|;
block|}
comment|// If we did not add any factor or there are no common factors, we can
comment|// bail out
if|if
condition|(
name|refsInAllOperands
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
block|}
comment|// 2. We gather the common factors and return them
name|List
argument_list|<
name|RexNode
argument_list|>
name|commonOperands
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|ref
range|:
name|refsInAllOperands
control|)
block|{
name|commonOperands
operator|.
name|add
argument_list|(
name|RexUtil
operator|.
name|composeDisjunction
argument_list|(
name|rexBuilder
argument_list|,
name|reductionCondition
operator|.
name|get
argument_list|(
name|ref
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|commonOperands
return|;
block|}
block|}
end_class

end_unit

