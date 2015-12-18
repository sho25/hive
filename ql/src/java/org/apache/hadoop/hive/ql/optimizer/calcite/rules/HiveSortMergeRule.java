begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|math
operator|.
name|BigDecimal
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
name|HiveSortLimit
import|;
end_import

begin_comment
comment|/**  * This rule will merge two HiveSortLimit operators.  *   * It is applied when the top match is a pure limit operation (no sorting).  *   * If the bottom operator is not synthetic and does not contain a limit,  * we currently bail out. Thus, we avoid a lot of unnecessary limit operations  * in the middle of the execution plan that could create performance regressions.  */
end_comment

begin_class
specifier|public
class|class
name|HiveSortMergeRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveSortMergeRule
name|INSTANCE
init|=
operator|new
name|HiveSortMergeRule
argument_list|()
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
comment|/**    * Creates a HiveSortProjectTransposeRule.    */
specifier|private
name|HiveSortMergeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveSortLimit
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveSortLimit
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//~ Methods ----------------------------------------------------------------
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
name|HiveSortLimit
name|topSortLimit
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveSortLimit
name|bottomSortLimit
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// If top operator is not a pure limit, we bail out
if|if
condition|(
operator|!
name|HiveCalciteUtil
operator|.
name|pureLimitRelNode
argument_list|(
name|topSortLimit
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// If the bottom operator is not synthetic and it does not contain a limit,
comment|// we will bail out; we do not want to end up with limits all over the tree
if|if
condition|(
name|topSortLimit
operator|.
name|isRuleCreated
argument_list|()
operator|&&
operator|!
name|bottomSortLimit
operator|.
name|isRuleCreated
argument_list|()
operator|&&
operator|!
name|HiveCalciteUtil
operator|.
name|limitRelNode
argument_list|(
name|bottomSortLimit
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
comment|// implement RelOptRule
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|HiveSortLimit
name|topSortLimit
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveSortLimit
name|bottomSortLimit
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|newOffset
decl_stmt|;
specifier|final
name|RexNode
name|newLimit
decl_stmt|;
if|if
condition|(
name|HiveCalciteUtil
operator|.
name|limitRelNode
argument_list|(
name|bottomSortLimit
argument_list|)
condition|)
block|{
specifier|final
name|RexBuilder
name|rexBuilder
init|=
name|topSortLimit
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
name|int
name|topOffset
init|=
name|topSortLimit
operator|.
name|offset
operator|==
literal|null
condition|?
literal|0
else|:
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|topSortLimit
operator|.
name|offset
argument_list|)
decl_stmt|;
name|int
name|topLimit
init|=
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|topSortLimit
operator|.
name|fetch
argument_list|)
decl_stmt|;
name|int
name|bottomOffset
init|=
name|bottomSortLimit
operator|.
name|offset
operator|==
literal|null
condition|?
literal|0
else|:
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|bottomSortLimit
operator|.
name|offset
argument_list|)
decl_stmt|;
name|int
name|bottomLimit
init|=
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|bottomSortLimit
operator|.
name|fetch
argument_list|)
decl_stmt|;
comment|// Three different cases
if|if
condition|(
name|topOffset
operator|+
name|topLimit
operator|<=
name|bottomLimit
condition|)
block|{
comment|// 1. Fully contained
comment|// topOffset + topLimit<= bottomLimit
name|newOffset
operator|=
name|bottomOffset
operator|+
name|topOffset
operator|==
literal|0
condition|?
literal|null
else|:
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|bottomOffset
operator|+
name|topOffset
argument_list|)
argument_list|)
expr_stmt|;
name|newLimit
operator|=
name|topSortLimit
operator|.
name|fetch
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|topOffset
operator|<
name|bottomLimit
condition|)
block|{
comment|// 2. Partially contained
comment|// topOffset + topLimit> bottomLimit&& topOffset< bottomLimit
name|newOffset
operator|=
name|bottomOffset
operator|+
name|topOffset
operator|==
literal|0
condition|?
literal|null
else|:
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|bottomOffset
operator|+
name|topOffset
argument_list|)
argument_list|)
expr_stmt|;
name|newLimit
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|bottomLimit
operator|-
name|topOffset
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// 3. Outside
comment|// we need to create a new limit 0
name|newOffset
operator|=
literal|null
expr_stmt|;
name|newLimit
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Bottom operator does not contain offset/fetch
name|newOffset
operator|=
name|topSortLimit
operator|.
name|offset
expr_stmt|;
name|newLimit
operator|=
name|topSortLimit
operator|.
name|fetch
expr_stmt|;
block|}
specifier|final
name|HiveSortLimit
name|newSort
init|=
name|bottomSortLimit
operator|.
name|copy
argument_list|(
name|bottomSortLimit
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|bottomSortLimit
operator|.
name|getInput
argument_list|()
argument_list|,
name|bottomSortLimit
operator|.
name|collation
argument_list|,
name|newOffset
argument_list|,
name|newLimit
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newSort
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

