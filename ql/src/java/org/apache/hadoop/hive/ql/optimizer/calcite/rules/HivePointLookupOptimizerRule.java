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
name|Collection
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
name|Map
operator|.
name|Entry
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
name|type
operator|.
name|RelDataType
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
name|calcite
operator|.
name|sql
operator|.
name|fun
operator|.
name|SqlStdOperatorTable
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveCalciteUtil
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
name|reloperators
operator|.
name|HiveIn
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
name|translator
operator|.
name|TypeConverter
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|ArrayListMultimap
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
name|ListMultimap
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
name|Lists
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
name|Maps
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
name|RelOptRuleOperand
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
name|AbstractRelNode
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
name|Join
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
name|JoinRelType
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HivePointLookupOptimizerRule
extends|extends
name|RelOptRule
block|{
comment|/**  * This optimization will take a Filter or expression, and if its predicate contains  * an OR operator whose children are constant equality expressions, it will try  * to generate an IN clause (which is more efficient). If the OR operator contains  * AND operator children, the optimization might generate an IN clause that uses  * structs.  */
specifier|public
specifier|static
class|class
name|FilterCondition
extends|extends
name|HivePointLookupOptimizerRule
block|{
specifier|public
name|FilterCondition
parameter_list|(
name|int
name|minNumORClauses
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
name|any
argument_list|()
argument_list|)
argument_list|,
name|minNumORClauses
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|RexNode
name|condition
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
name|analyzeCondition
argument_list|(
name|call
argument_list|,
name|rexBuilder
argument_list|,
name|filter
argument_list|,
name|condition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RelNode
name|copyNode
parameter_list|(
name|AbstractRelNode
name|node
parameter_list|,
name|RexNode
name|newCondition
parameter_list|)
block|{
specifier|final
name|Filter
name|filter
init|=
operator|(
name|Filter
operator|)
name|node
decl_stmt|;
return|return
name|filter
operator|.
name|copy
argument_list|(
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|filter
operator|.
name|getInput
argument_list|()
argument_list|,
name|newCondition
argument_list|)
return|;
block|}
block|}
comment|/**  * This optimization will take a Join or expression, and if its join condition contains  * an OR operator whose children are constant equality expressions, it will try  * to generate an IN clause (which is more efficient). If the OR operator contains  * AND operator children, the optimization might generate an IN clause that uses  * structs.  */
specifier|public
specifier|static
class|class
name|JoinCondition
extends|extends
name|HivePointLookupOptimizerRule
block|{
specifier|public
name|JoinCondition
parameter_list|(
name|int
name|minNumORClauses
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Join
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|minNumORClauses
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Join
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
name|join
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
specifier|final
name|RexNode
name|condition
init|=
name|RexUtil
operator|.
name|pullFactors
argument_list|(
name|rexBuilder
argument_list|,
name|join
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
name|analyzeCondition
argument_list|(
name|call
argument_list|,
name|rexBuilder
argument_list|,
name|join
argument_list|,
name|condition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RelNode
name|copyNode
parameter_list|(
name|AbstractRelNode
name|node
parameter_list|,
name|RexNode
name|newCondition
parameter_list|)
block|{
specifier|final
name|Join
name|join
init|=
operator|(
name|Join
operator|)
name|node
decl_stmt|;
return|return
name|join
operator|.
name|copy
argument_list|(
name|join
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newCondition
argument_list|,
name|join
operator|.
name|getLeft
argument_list|()
argument_list|,
name|join
operator|.
name|getRight
argument_list|()
argument_list|,
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|,
name|join
operator|.
name|isSemiJoinDone
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HivePointLookupOptimizerRule
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Minimum number of OR clauses needed to transform into IN clauses
specifier|protected
specifier|final
name|int
name|minNumORClauses
decl_stmt|;
specifier|protected
specifier|abstract
name|RelNode
name|copyNode
parameter_list|(
name|AbstractRelNode
name|node
parameter_list|,
name|RexNode
name|newCondition
parameter_list|)
function_decl|;
specifier|protected
name|HivePointLookupOptimizerRule
parameter_list|(
name|RelOptRuleOperand
name|operand
parameter_list|,
name|int
name|minNumORClauses
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|)
expr_stmt|;
name|this
operator|.
name|minNumORClauses
operator|=
name|minNumORClauses
expr_stmt|;
block|}
specifier|public
name|void
name|analyzeCondition
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|,
name|AbstractRelNode
name|node
parameter_list|,
name|RexNode
name|condition
parameter_list|)
block|{
comment|// 1. We try to transform possible candidates
name|RexTransformIntoInClause
name|transformIntoInClause
init|=
operator|new
name|RexTransformIntoInClause
argument_list|(
name|rexBuilder
argument_list|,
name|node
argument_list|,
name|minNumORClauses
argument_list|)
decl_stmt|;
name|RexNode
name|newCondition
init|=
name|transformIntoInClause
operator|.
name|apply
argument_list|(
name|condition
argument_list|)
decl_stmt|;
comment|// 2. We merge IN expressions
name|RexMergeInClause
name|mergeInClause
init|=
operator|new
name|RexMergeInClause
argument_list|(
name|rexBuilder
argument_list|)
decl_stmt|;
name|newCondition
operator|=
name|mergeInClause
operator|.
name|apply
argument_list|(
name|newCondition
argument_list|)
expr_stmt|;
comment|// 3. If we could not transform anything, we bail out
if|if
condition|(
name|newCondition
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|condition
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// 4. We create the Filter/Join with the new condition
name|RelNode
name|newNode
init|=
name|copyNode
argument_list|(
name|node
argument_list|,
name|newCondition
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newNode
argument_list|)
expr_stmt|;
block|}
comment|/**    * Transforms OR clauses into IN clauses, when possible.    */
specifier|protected
specifier|static
class|class
name|RexTransformIntoInClause
extends|extends
name|RexShuttle
block|{
specifier|private
specifier|final
name|RexBuilder
name|rexBuilder
decl_stmt|;
specifier|private
specifier|final
name|AbstractRelNode
name|nodeOp
decl_stmt|;
specifier|private
specifier|final
name|int
name|minNumORClauses
decl_stmt|;
name|RexTransformIntoInClause
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|AbstractRelNode
name|nodeOp
parameter_list|,
name|int
name|minNumORClauses
parameter_list|)
block|{
name|this
operator|.
name|nodeOp
operator|=
name|nodeOp
expr_stmt|;
name|this
operator|.
name|rexBuilder
operator|=
name|rexBuilder
expr_stmt|;
name|this
operator|.
name|minNumORClauses
operator|=
name|minNumORClauses
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|RexNode
name|node
decl_stmt|;
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
name|call
operator|)
operator|.
name|getOperands
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|operands
control|)
block|{
name|RexNode
name|newOperand
decl_stmt|;
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
try|try
block|{
name|newOperand
operator|=
name|transformIntoInClauseCondition
argument_list|(
name|rexBuilder
argument_list|,
name|nodeOp
operator|.
name|getRowType
argument_list|()
argument_list|,
name|operand
argument_list|,
name|minNumORClauses
argument_list|)
expr_stmt|;
if|if
condition|(
name|newOperand
operator|==
literal|null
condition|)
block|{
name|newOperand
operator|=
name|operand
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
name|error
argument_list|(
literal|"Exception in HivePointLookupOptimizerRule"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|call
return|;
block|}
block|}
else|else
block|{
name|newOperand
operator|=
name|operand
expr_stmt|;
block|}
name|newOperands
operator|.
name|add
argument_list|(
name|newOperand
argument_list|)
expr_stmt|;
block|}
name|node
operator|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|newOperands
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|OR
case|:
try|try
block|{
name|node
operator|=
name|transformIntoInClauseCondition
argument_list|(
name|rexBuilder
argument_list|,
name|nodeOp
operator|.
name|getRowType
argument_list|()
argument_list|,
name|call
argument_list|,
name|minNumORClauses
argument_list|)
expr_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|call
return|;
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
name|error
argument_list|(
literal|"Exception in HivePointLookupOptimizerRule"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|call
return|;
block|}
break|break;
default|default:
return|return
name|super
operator|.
name|visitCall
argument_list|(
name|call
argument_list|)
return|;
block|}
return|return
name|node
return|;
block|}
specifier|private
specifier|static
name|RexNode
name|transformIntoInClauseCondition
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|RelDataType
name|inputSchema
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|int
name|minNumORClauses
parameter_list|)
throws|throws
name|SemanticException
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
comment|// 1. We extract the information necessary to create the predicate for the new
comment|//    filter
name|ListMultimap
argument_list|<
name|RexInputRef
argument_list|,
name|RexLiteral
argument_list|>
name|columnConstantsMap
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|operands
operator|.
name|size
argument_list|()
operator|<
name|minNumORClauses
condition|)
block|{
comment|// We bail out
return|return
literal|null
return|;
block|}
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
name|operands
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|RexNode
name|conjunction
range|:
name|conjunctions
control|)
block|{
comment|// 1.1. If it is not a RexCall, we bail out
if|if
condition|(
operator|!
operator|(
name|conjunction
operator|instanceof
name|RexCall
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// 1.2. We extract the information that we need
name|RexCall
name|conjCall
init|=
operator|(
name|RexCall
operator|)
name|conjunction
decl_stmt|;
if|if
condition|(
name|conjCall
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|EQUALS
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
name|RexInputRef
name|ref
init|=
operator|(
name|RexInputRef
operator|)
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RexLiteral
name|literal
init|=
operator|(
name|RexLiteral
operator|)
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|columnConstantsMap
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|literal
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnConstantsMap
operator|.
name|get
argument_list|(
name|ref
argument_list|)
operator|.
name|size
argument_list|()
operator|!=
name|i
operator|+
literal|1
condition|)
block|{
comment|// If we have not added to this column before, we bail out
return|return
literal|null
return|;
block|}
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
name|RexInputRef
name|ref
init|=
operator|(
name|RexInputRef
operator|)
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|RexLiteral
name|literal
init|=
operator|(
name|RexLiteral
operator|)
name|conjCall
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|columnConstantsMap
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|literal
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnConstantsMap
operator|.
name|get
argument_list|(
name|ref
argument_list|)
operator|.
name|size
argument_list|()
operator|!=
name|i
operator|+
literal|1
condition|)
block|{
comment|// If we have not added to this column before, we bail out
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
comment|// Bail out
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|// 3. We build the new predicate and return it
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|(
name|operands
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// 3.1 Create structs
name|List
argument_list|<
name|RexInputRef
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|RexInputRef
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|paramsTypes
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|structReturnType
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|newOperandsTypes
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
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
name|List
argument_list|<
name|RexLiteral
argument_list|>
name|constantFields
init|=
operator|new
name|ArrayList
argument_list|<
name|RexLiteral
argument_list|>
argument_list|(
name|operands
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RexInputRef
name|ref
range|:
name|columnConstantsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|// If any of the elements was not referenced by every operand, we bail out
if|if
condition|(
name|columnConstantsMap
operator|.
name|get
argument_list|(
name|ref
argument_list|)
operator|.
name|size
argument_list|()
operator|<=
name|i
condition|)
block|{
return|return
literal|null
return|;
block|}
name|RexLiteral
name|columnConstant
init|=
name|columnConstantsMap
operator|.
name|get
argument_list|(
name|ref
argument_list|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|columns
operator|.
name|add
argument_list|(
name|ref
argument_list|)
expr_stmt|;
name|names
operator|.
name|add
argument_list|(
name|inputSchema
operator|.
name|getFieldNames
argument_list|()
operator|.
name|get
argument_list|(
name|ref
operator|.
name|getIndex
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|paramsTypes
operator|.
name|add
argument_list|(
name|ref
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|structReturnType
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|ref
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|constantFields
operator|.
name|add
argument_list|(
name|columnConstant
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|RexNode
name|columnsRefs
decl_stmt|;
if|if
condition|(
name|columns
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|columnsRefs
operator|=
name|columns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Create STRUCT clause
name|columnsRefs
operator|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|ROW
argument_list|,
name|columns
argument_list|)
expr_stmt|;
block|}
name|newOperands
operator|.
name|add
argument_list|(
name|columnsRefs
argument_list|)
expr_stmt|;
name|newOperandsTypes
operator|.
name|add
argument_list|(
name|columnsRefs
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|RexNode
name|values
decl_stmt|;
if|if
condition|(
name|constantFields
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|values
operator|=
name|constantFields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Create STRUCT clause
name|values
operator|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|ROW
argument_list|,
name|constantFields
argument_list|)
expr_stmt|;
block|}
name|newOperands
operator|.
name|add
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|newOperandsTypes
operator|.
name|add
argument_list|(
name|values
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// 4. Create and return IN clause
return|return
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|HiveIn
operator|.
name|INSTANCE
argument_list|,
name|newOperands
argument_list|)
return|;
block|}
block|}
comment|/**    * Merge IN clauses, when possible.    */
specifier|protected
specifier|static
class|class
name|RexMergeInClause
extends|extends
name|RexShuttle
block|{
specifier|private
specifier|final
name|RexBuilder
name|rexBuilder
decl_stmt|;
name|RexMergeInClause
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|)
block|{
name|this
operator|.
name|rexBuilder
operator|=
name|rexBuilder
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|RexNode
name|node
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|operands
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
name|stringToExpr
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|inLHSExprToRHSExprs
init|=
name|LinkedHashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
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
comment|// IN clauses need to be combined by keeping only common elements
name|operands
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|RexUtil
operator|.
name|flattenAnd
argument_list|(
operator|(
operator|(
name|RexCall
operator|)
name|call
operator|)
operator|.
name|getOperands
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|operand
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|IN
condition|)
block|{
name|RexCall
name|inCall
init|=
operator|(
name|RexCall
operator|)
name|operand
decl_stmt|;
if|if
condition|(
operator|!
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|ref
init|=
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|stringToExpr
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|inLHSExprToRHSExprs
operator|.
name|containsKey
argument_list|(
name|ref
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|expressions
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|String
name|expr
init|=
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|expressions
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|stringToExpr
operator|.
name|put
argument_list|(
name|expr
argument_list|,
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|inLHSExprToRHSExprs
operator|.
name|get
argument_list|(
name|ref
argument_list|)
operator|.
name|retainAll
argument_list|(
name|expressions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|String
name|expr
init|=
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|inLHSExprToRHSExprs
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|expr
argument_list|)
expr_stmt|;
name|stringToExpr
operator|.
name|put
argument_list|(
name|expr
argument_list|,
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|operands
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
operator|--
name|i
expr_stmt|;
block|}
block|}
comment|// Create IN clauses
name|newOperands
operator|=
name|createInClauses
argument_list|(
name|rexBuilder
argument_list|,
name|stringToExpr
argument_list|,
name|inLHSExprToRHSExprs
argument_list|)
expr_stmt|;
name|newOperands
operator|.
name|addAll
argument_list|(
name|operands
argument_list|)
expr_stmt|;
comment|// Return node
name|node
operator|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|newOperands
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|OR
case|:
comment|// IN clauses need to be combined by keeping all elements
name|operands
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|RexUtil
operator|.
name|flattenOr
argument_list|(
operator|(
operator|(
name|RexCall
operator|)
name|call
operator|)
operator|.
name|getOperands
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|operand
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|IN
condition|)
block|{
name|RexCall
name|inCall
init|=
operator|(
name|RexCall
operator|)
name|operand
decl_stmt|;
if|if
condition|(
operator|!
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|String
name|ref
init|=
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|stringToExpr
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|String
name|expr
init|=
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|inLHSExprToRHSExprs
operator|.
name|put
argument_list|(
name|ref
argument_list|,
name|expr
argument_list|)
expr_stmt|;
name|stringToExpr
operator|.
name|put
argument_list|(
name|expr
argument_list|,
name|inCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|operands
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
operator|--
name|i
expr_stmt|;
block|}
block|}
comment|// Create IN clauses
name|newOperands
operator|=
name|createInClauses
argument_list|(
name|rexBuilder
argument_list|,
name|stringToExpr
argument_list|,
name|inLHSExprToRHSExprs
argument_list|)
expr_stmt|;
name|newOperands
operator|.
name|addAll
argument_list|(
name|operands
argument_list|)
expr_stmt|;
comment|// Return node
name|node
operator|=
name|RexUtil
operator|.
name|composeDisjunction
argument_list|(
name|rexBuilder
argument_list|,
name|newOperands
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
default|default:
return|return
name|super
operator|.
name|visitCall
argument_list|(
name|call
argument_list|)
return|;
block|}
return|return
name|node
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|RexNode
argument_list|>
name|createInClauses
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
name|stringToExpr
parameter_list|,
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|inLHSExprToRHSExprs
parameter_list|)
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|newExpressions
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|inLHSExprToRHSExprs
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|ref
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|String
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
name|exprs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|newExpressions
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|(
name|exprs
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|newOperands
operator|.
name|add
argument_list|(
name|stringToExpr
operator|.
name|get
argument_list|(
name|ref
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|expr
range|:
name|exprs
control|)
block|{
name|newOperands
operator|.
name|add
argument_list|(
name|stringToExpr
operator|.
name|get
argument_list|(
name|expr
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|newExpressions
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|HiveIn
operator|.
name|INSTANCE
argument_list|,
name|newOperands
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|newExpressions
return|;
block|}
block|}
block|}
end_class

end_unit

