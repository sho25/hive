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
name|math
operator|.
name|BigDecimal
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
name|RelColumnOrigin
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
name|common
operator|.
name|StatsSetupConst
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
name|RelOptHiveTable
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
name|plan
operator|.
name|ColStatistics
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
name|ColStatistics
operator|.
name|Range
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

begin_comment
comment|/**  * This rule simplifies the condition in Filter operators using the  * column statistics (if available).  *  * For instance, given the following predicate:  *   a> 5  * we can infer that the predicate will evaluate to false if the max  * value for column a is 4.  *  * Currently we support the simplification of =,>=,<=,>,<, and  * IN operations.  */
end_comment

begin_class
specifier|public
class|class
name|HiveReduceExpressionsWithStatsRule
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
name|HiveReduceExpressionsWithStatsRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveReduceExpressionsWithStatsRule
name|INSTANCE
init|=
operator|new
name|HiveReduceExpressionsWithStatsRule
argument_list|()
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
argument_list|)
decl_stmt|;
specifier|private
name|HiveReduceExpressionsWithStatsRule
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
name|RelMetadataQuery
name|metadataProvider
init|=
name|RelMetadataQuery
operator|.
name|instance
argument_list|()
decl_stmt|;
comment|// 1. Recompose filter possibly by pulling out common elements from DNF
comment|// expressions
name|RexNode
name|newFilterCondition
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
comment|// 2. Reduce filter with stats information
name|RexReplacer
name|replacer
init|=
operator|new
name|RexReplacer
argument_list|(
name|filter
argument_list|,
name|rexBuilder
argument_list|,
name|metadataProvider
argument_list|)
decl_stmt|;
name|newFilterCondition
operator|=
name|replacer
operator|.
name|apply
argument_list|(
name|newFilterCondition
argument_list|)
expr_stmt|;
comment|// 3. Transform if we have created a new filter operator
if|if
condition|(
operator|!
name|filter
operator|.
name|getCondition
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|newFilterCondition
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
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
name|filter
operator|.
name|getInput
argument_list|()
argument_list|,
name|newFilterCondition
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Replaces expressions with their reductions. Note that we only have to    * look for RexCall, since nothing else is reducible in the first place.    */
specifier|protected
specifier|static
class|class
name|RexReplacer
extends|extends
name|RexShuttle
block|{
specifier|private
specifier|final
name|Filter
name|filterOp
decl_stmt|;
specifier|private
specifier|final
name|RexBuilder
name|rexBuilder
decl_stmt|;
specifier|private
specifier|final
name|RelMetadataQuery
name|metadataProvider
decl_stmt|;
name|RexReplacer
parameter_list|(
name|Filter
name|filterOp
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|,
name|RelMetadataQuery
name|metadataProvider
parameter_list|)
block|{
name|this
operator|.
name|filterOp
operator|=
name|filterOp
expr_stmt|;
name|this
operator|.
name|rexBuilder
operator|=
name|rexBuilder
expr_stmt|;
name|this
operator|.
name|metadataProvider
operator|=
name|metadataProvider
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
if|if
condition|(
name|COMPARISON
operator|.
name|contains
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
argument_list|)
condition|)
block|{
name|RexInputRef
name|ref
init|=
literal|null
decl_stmt|;
name|RexLiteral
name|literal
init|=
literal|null
decl_stmt|;
name|SqlKind
name|kind
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|call
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
name|call
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
operator|(
name|RexInputRef
operator|)
name|call
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|literal
operator|=
operator|(
name|RexLiteral
operator|)
name|call
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|kind
operator|=
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|call
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
name|call
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
operator|(
name|RexInputRef
operator|)
name|call
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|literal
operator|=
operator|(
name|RexLiteral
operator|)
name|call
operator|.
name|operands
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|kind
operator|=
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|.
name|reverse
argument_list|()
expr_stmt|;
block|}
comment|// Found an expression that we can try to reduce
name|Number
name|max
init|=
literal|null
decl_stmt|;
name|Number
name|min
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ref
operator|!=
literal|null
operator|&&
name|literal
operator|!=
literal|null
operator|&&
name|kind
operator|!=
literal|null
condition|)
block|{
name|Pair
argument_list|<
name|Number
argument_list|,
name|Number
argument_list|>
name|maxMin
init|=
name|extractMaxMin
argument_list|(
name|ref
argument_list|)
decl_stmt|;
name|max
operator|=
name|maxMin
operator|.
name|left
expr_stmt|;
name|min
operator|=
name|maxMin
operator|.
name|right
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|!=
literal|null
operator|&&
name|min
operator|!=
literal|null
condition|)
block|{
comment|// Stats were available, try to reduce
name|RexNode
name|reduced
init|=
name|reduceCall
argument_list|(
name|literal
argument_list|,
name|kind
argument_list|,
name|max
argument_list|,
name|min
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduced
operator|!=
literal|null
condition|)
block|{
return|return
name|reduced
return|;
block|}
block|}
comment|// We cannot apply the reduction
return|return
name|call
return|;
block|}
elseif|else
if|if
condition|(
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|IN
condition|)
block|{
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|RexInputRef
condition|)
block|{
comment|// Ref
name|RexInputRef
name|ref
init|=
operator|(
name|RexInputRef
operator|)
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Found an expression that we can try to reduce
name|Number
name|max
init|=
literal|null
decl_stmt|;
name|Number
name|min
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
name|Pair
argument_list|<
name|Number
argument_list|,
name|Number
argument_list|>
name|maxMin
init|=
name|extractMaxMin
argument_list|(
name|ref
argument_list|)
decl_stmt|;
name|max
operator|=
name|maxMin
operator|.
name|left
expr_stmt|;
name|min
operator|=
name|maxMin
operator|.
name|right
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|!=
literal|null
operator|&&
name|min
operator|!=
literal|null
condition|)
block|{
comment|// Stats were available, try to reduce
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|newOperands
operator|.
name|add
argument_list|(
name|ref
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|call
operator|.
name|getOperands
argument_list|()
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
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|operand
operator|instanceof
name|RexLiteral
condition|)
block|{
name|RexLiteral
name|literal
init|=
operator|(
name|RexLiteral
operator|)
name|operand
decl_stmt|;
name|RexNode
name|reduced
init|=
name|reduceCall
argument_list|(
name|literal
argument_list|,
name|SqlKind
operator|.
name|EQUALS
argument_list|,
name|max
argument_list|,
name|min
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduced
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|reduced
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|newOperands
operator|.
name|add
argument_list|(
name|literal
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|newOperands
operator|.
name|add
argument_list|(
name|operand
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newOperands
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
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
elseif|else
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|ROW
condition|)
block|{
comment|// Struct
name|RexCall
name|struct
init|=
operator|(
name|RexCall
operator|)
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RexInputRef
argument_list|>
name|refs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|Number
argument_list|,
name|Number
argument_list|>
argument_list|>
name|maxMinStats
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|struct
operator|.
name|getOperands
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|operand
operator|instanceof
name|RexInputRef
operator|)
condition|)
block|{
comment|// Cannot simplify, we bail out
return|return
name|call
return|;
block|}
name|RexInputRef
name|ref
init|=
operator|(
name|RexInputRef
operator|)
name|operand
decl_stmt|;
name|refs
operator|.
name|add
argument_list|(
name|ref
argument_list|)
expr_stmt|;
name|maxMinStats
operator|.
name|add
argument_list|(
name|extractMaxMin
argument_list|(
name|ref
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Try to reduce
name|List
argument_list|<
name|RexNode
argument_list|>
name|newOperands
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|newOperands
operator|.
name|add
argument_list|(
name|struct
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|RexCall
name|constStruct
init|=
operator|(
name|RexCall
operator|)
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|boolean
name|allTrue
init|=
literal|true
decl_stmt|;
name|boolean
name|addOperand
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|i
operator|<
name|constStruct
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
name|RexNode
name|operand
init|=
name|constStruct
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
name|operand
operator|instanceof
name|RexLiteral
condition|)
block|{
name|RexLiteral
name|literal
init|=
operator|(
name|RexLiteral
operator|)
name|operand
decl_stmt|;
name|RexNode
name|reduced
init|=
name|reduceCall
argument_list|(
name|literal
argument_list|,
name|SqlKind
operator|.
name|EQUALS
argument_list|,
name|maxMinStats
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|left
argument_list|,
name|maxMinStats
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduced
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|reduced
operator|.
name|isAlwaysFalse
argument_list|()
condition|)
block|{
name|allTrue
operator|=
literal|false
expr_stmt|;
name|addOperand
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
else|else
block|{
name|allTrue
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
name|allTrue
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|allTrue
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
if|if
condition|(
name|addOperand
condition|)
block|{
name|newOperands
operator|.
name|add
argument_list|(
name|constStruct
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newOperands
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
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
comment|// We cannot apply the reduction
return|return
name|call
return|;
block|}
comment|// If we did not reduce, check the children nodes
name|RexNode
name|node
init|=
name|super
operator|.
name|visitCall
argument_list|(
name|call
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|!=
name|call
condition|)
block|{
name|node
operator|=
name|RexUtil
operator|.
name|simplify
argument_list|(
name|rexBuilder
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|node
return|;
block|}
specifier|private
name|Pair
argument_list|<
name|Number
argument_list|,
name|Number
argument_list|>
name|extractMaxMin
parameter_list|(
name|RexInputRef
name|ref
parameter_list|)
block|{
name|Number
name|max
init|=
literal|null
decl_stmt|;
name|Number
name|min
init|=
literal|null
decl_stmt|;
name|RelColumnOrigin
name|columnOrigin
init|=
name|this
operator|.
name|metadataProvider
operator|.
name|getColumnOrigin
argument_list|(
name|filterOp
argument_list|,
name|ref
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnOrigin
operator|!=
literal|null
condition|)
block|{
name|RelOptHiveTable
name|table
init|=
operator|(
name|RelOptHiveTable
operator|)
name|columnOrigin
operator|.
name|getOriginTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|ColStatistics
name|colStats
init|=
name|table
operator|.
name|getColStat
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|columnOrigin
operator|.
name|getOriginColumnOrdinal
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|colStats
operator|!=
literal|null
operator|&&
name|StatsSetupConst
operator|.
name|areColumnStatsUptoDate
argument_list|(
name|table
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|,
name|colStats
operator|.
name|getColumnName
argument_list|()
argument_list|)
condition|)
block|{
name|Range
name|range
init|=
name|colStats
operator|.
name|getRange
argument_list|()
decl_stmt|;
if|if
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
name|max
operator|=
name|range
operator|.
name|maxValue
expr_stmt|;
name|min
operator|=
name|range
operator|.
name|minValue
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|Pair
operator|.
expr|<
name|Number
operator|,
name|Number
operator|>
name|of
argument_list|(
name|max
argument_list|,
name|min
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|RexNode
name|reduceCall
parameter_list|(
name|RexLiteral
name|literal
parameter_list|,
name|SqlKind
name|kind
parameter_list|,
name|Number
name|max
parameter_list|,
name|Number
name|min
parameter_list|)
block|{
comment|// Stats were available, try to reduce
if|if
condition|(
name|max
operator|!=
literal|null
operator|&&
name|min
operator|!=
literal|null
condition|)
block|{
name|BigDecimal
name|maxVal
init|=
operator|new
name|BigDecimal
argument_list|(
name|max
operator|.
name|floatValue
argument_list|()
argument_list|)
decl_stmt|;
name|BigDecimal
name|minVal
init|=
operator|new
name|BigDecimal
argument_list|(
name|min
operator|.
name|floatValue
argument_list|()
argument_list|)
decl_stmt|;
name|RexLiteral
name|maxLiteral
init|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|maxVal
argument_list|,
name|literal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|RexLiteral
name|minLiteral
init|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
name|minVal
argument_list|,
name|literal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
comment|// Equals
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|EQUALS
condition|)
block|{
if|if
condition|(
name|minLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|>
literal|0
operator|||
name|maxLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
block|}
comment|// Greater than (or equal), and less than (or equal)
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|GREATER_THAN
condition|)
block|{
if|if
condition|(
name|minLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|maxLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|<=
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|GREATER_THAN_OR_EQUAL
condition|)
block|{
if|if
condition|(
name|minLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|maxLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|LESS_THAN
condition|)
block|{
if|if
condition|(
name|minLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|maxLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|LESS_THAN_OR_EQUAL
condition|)
block|{
if|if
condition|(
name|minLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|false
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|maxLiteral
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|literal
operator|.
name|getValue
argument_list|()
argument_list|)
operator|<=
literal|0
condition|)
block|{
return|return
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

