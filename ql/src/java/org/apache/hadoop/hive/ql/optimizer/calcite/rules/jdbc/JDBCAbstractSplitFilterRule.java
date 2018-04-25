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
name|calcite
operator|.
name|rules
operator|.
name|jdbc
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
name|sql
operator|.
name|SqlDialect
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
name|HiveFilter
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
name|jdbc
operator|.
name|HiveJdbcConverter
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
name|HiveJoin
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
comment|/**  * JDBCAbstractSplitFilterRule split a {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter} into  * two {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter} operators where the lower operator  * could be pushed down below the  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.jdbc.HiveJdbcConverter}}  * operator and therefore could be sent to the external table.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|JDBCAbstractSplitFilterRule
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JDBCAbstractSplitFilterRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JDBCAbstractSplitFilterRule
name|SPLIT_FILTER_ABOVE_JOIN
init|=
operator|new
name|JDBCSplitFilterAboveJoinRule
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JDBCAbstractSplitFilterRule
name|SPLIT_FILTER_ABOVE_CONVERTER
init|=
operator|new
name|JDBCSplitFilterRule
argument_list|()
decl_stmt|;
comment|/**    * FilterSupportedFunctionsVisitor traverse all of the Rex call and splits them into    * two lists, one with supported jdbc calls, and one with not supported jdbc calls.    */
specifier|public
specifier|static
class|class
name|FilterSupportedFunctionsVisitor
extends|extends
name|RexVisitorImpl
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
specifier|final
name|SqlDialect
name|dialect
decl_stmt|;
specifier|public
name|FilterSupportedFunctionsVisitor
parameter_list|(
name|SqlDialect
name|dialect
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|dialect
operator|=
name|dialect
expr_stmt|;
block|}
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|validJdbcNode
init|=
operator|new
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|invalidJdbcNode
init|=
operator|new
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|getValidJdbcNode
parameter_list|()
block|{
return|return
name|validJdbcNode
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|getInvalidJdbcNode
parameter_list|()
block|{
return|return
name|invalidJdbcNode
return|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
if|if
condition|(
name|call
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|AND
condition|)
block|{
return|return
name|super
operator|.
name|visitCall
argument_list|(
name|call
argument_list|)
return|;
block|}
else|else
block|{
name|boolean
name|isValidCall
init|=
name|JDBCRexCallValidator
operator|.
name|isValidJdbcOperation
argument_list|(
name|call
argument_list|,
name|dialect
argument_list|)
decl_stmt|;
if|if
condition|(
name|isValidCall
condition|)
block|{
name|validJdbcNode
operator|.
name|add
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|invalidJdbcNode
operator|.
name|add
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|canBeSplit
parameter_list|()
block|{
return|return
operator|!
name|validJdbcNode
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|invalidJdbcNode
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
specifier|protected
name|JDBCAbstractSplitFilterRule
parameter_list|(
name|RelOptRuleOperand
name|operand
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|canSplitFilter
parameter_list|(
name|RexNode
name|cond
parameter_list|,
name|SqlDialect
name|dialect
parameter_list|)
block|{
name|FilterSupportedFunctionsVisitor
name|visitor
init|=
operator|new
name|FilterSupportedFunctionsVisitor
argument_list|(
name|dialect
argument_list|)
decl_stmt|;
name|cond
operator|.
name|accept
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
return|return
name|visitor
operator|.
name|canBeSplit
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|SqlDialect
name|dialect
parameter_list|)
block|{
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"MySplitFilter.matches has been called"
argument_list|)
expr_stmt|;
specifier|final
name|HiveFilter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RexNode
name|cond
init|=
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
return|return
name|canSplitFilter
argument_list|(
name|cond
argument_list|,
name|dialect
argument_list|)
return|;
block|}
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|SqlDialect
name|dialect
parameter_list|)
block|{
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"MySplitFilter.onMatch has been called"
argument_list|)
expr_stmt|;
specifier|final
name|HiveFilter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RexCall
name|callExpression
init|=
operator|(
name|RexCall
operator|)
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
name|FilterSupportedFunctionsVisitor
name|visitor
init|=
operator|new
name|FilterSupportedFunctionsVisitor
argument_list|(
name|dialect
argument_list|)
decl_stmt|;
name|callExpression
operator|.
name|accept
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|validJdbcNode
init|=
name|visitor
operator|.
name|getValidJdbcNode
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|RexCall
argument_list|>
name|invalidJdbcNode
init|=
name|visitor
operator|.
name|getInvalidJdbcNode
argument_list|()
decl_stmt|;
assert|assert
name|validJdbcNode
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|&&
name|invalidJdbcNode
operator|.
name|size
argument_list|()
operator|!=
literal|0
assert|;
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
name|RexNode
name|validCondition
decl_stmt|;
if|if
condition|(
name|validJdbcNode
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|validCondition
operator|=
name|validJdbcNode
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|validCondition
operator|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|AND
argument_list|,
name|validJdbcNode
argument_list|)
expr_stmt|;
block|}
name|HiveFilter
name|newJdbcValidFilter
init|=
operator|new
name|HiveFilter
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
argument_list|,
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
name|validCondition
argument_list|)
decl_stmt|;
name|RexNode
name|invalidCondition
decl_stmt|;
if|if
condition|(
name|invalidJdbcNode
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|invalidCondition
operator|=
name|invalidJdbcNode
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|invalidCondition
operator|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|AND
argument_list|,
name|invalidJdbcNode
argument_list|)
expr_stmt|;
block|}
name|HiveFilter
name|newJdbcInvalidFilter
init|=
operator|new
name|HiveFilter
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
argument_list|,
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newJdbcValidFilter
argument_list|,
name|invalidCondition
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newJdbcInvalidFilter
argument_list|)
expr_stmt|;
block|}
comment|/**    * JDBCSplitFilterAboveJoinRule split splitter above a HiveJoin operator, so we could push it into the HiveJoin.    */
specifier|public
specifier|static
class|class
name|JDBCSplitFilterAboveJoinRule
extends|extends
name|JDBCAbstractSplitFilterRule
block|{
specifier|public
name|JDBCSplitFilterAboveJoinRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveFilter
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJdbcConverter
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
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
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"MyUpperJoinFilterFilter.matches has been called"
argument_list|)
expr_stmt|;
specifier|final
name|HiveJoin
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|HiveJdbcConverter
name|conv
init|=
name|call
operator|.
name|rel
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|RexNode
name|joinCond
init|=
name|join
operator|.
name|getCondition
argument_list|()
decl_stmt|;
return|return
name|super
operator|.
name|matches
argument_list|(
name|call
argument_list|)
operator|&&
name|JDBCRexCallValidator
operator|.
name|isValidJdbcOperation
argument_list|(
name|joinCond
argument_list|,
name|conv
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
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
name|HiveJdbcConverter
name|conv
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|super
operator|.
name|onMatch
argument_list|(
name|call
argument_list|,
name|conv
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * JDBCSplitFilterRule splits a HiveFilter rule so we could push part of the HiveFilter into the jdbc.    */
specifier|public
specifier|static
class|class
name|JDBCSplitFilterRule
extends|extends
name|JDBCAbstractSplitFilterRule
block|{
specifier|public
name|JDBCSplitFilterRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveFilter
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJdbcConverter
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
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|HiveJdbcConverter
name|conv
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
name|super
operator|.
name|matches
argument_list|(
name|call
argument_list|,
name|conv
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
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
name|HiveJdbcConverter
name|conv
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|super
operator|.
name|onMatch
argument_list|(
name|call
argument_list|,
name|conv
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

