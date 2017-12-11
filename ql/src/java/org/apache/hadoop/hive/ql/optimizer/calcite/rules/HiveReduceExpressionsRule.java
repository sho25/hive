begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|List
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
name|RelOptPredicateList
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
name|rel
operator|.
name|rules
operator|.
name|ReduceExpressionsRule
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
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
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
name|tools
operator|.
name|RelBuilder
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
name|tools
operator|.
name|RelBuilderFactory
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
name|HiveJoin
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
name|HiveProject
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
comment|/**  * Collection of planner rules that apply various simplifying transformations on  * RexNode trees. Currently, there are two transformations:  *  *<ul>  *<li>Constant reduction, which evaluates constant subtrees, replacing them  * with a corresponding RexLiteral  *<li>Removal of redundant casts, which occurs when the argument into the cast  * is the same as the type of the resulting cast expression  *</ul>  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HiveReduceExpressionsRule
extends|extends
name|ReduceExpressionsRule
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
name|HiveReduceExpressionsRule
operator|.
name|class
argument_list|)
decl_stmt|;
comment|//~ Static fields/initializers ---------------------------------------------
comment|/**    * Singleton rule that reduces constants inside a    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter}.    */
specifier|public
specifier|static
specifier|final
name|ReduceExpressionsRule
name|FILTER_INSTANCE
init|=
operator|new
name|FilterReduceExpressionsRule
argument_list|(
name|HiveFilter
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|/**    * Singleton rule that reduces constants inside a    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveProject}.    */
specifier|public
specifier|static
specifier|final
name|ReduceExpressionsRule
name|PROJECT_INSTANCE
init|=
operator|new
name|ProjectReduceExpressionsRule
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|/**    * Singleton rule that reduces constants inside a    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveJoin}.    */
specifier|public
specifier|static
specifier|final
name|ReduceExpressionsRule
name|JOIN_INSTANCE
init|=
operator|new
name|JoinReduceExpressionsRule
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
comment|/**    * Creates a HiveReduceExpressionsRule.    *    * @param clazz class of rels to which this rule should apply    */
specifier|protected
name|HiveReduceExpressionsRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
name|clazz
parameter_list|,
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|clazz
argument_list|,
name|relBuilderFactory
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
comment|/**    * Rule that reduces constants inside a {@link org.apache.calcite.rel.core.Filter}.    * If the condition is a constant, the filter is removed (if TRUE) or replaced with    * an empty {@link org.apache.calcite.rel.core.Values} (if FALSE or NULL).    */
specifier|public
specifier|static
class|class
name|FilterReduceExpressionsRule
extends|extends
name|ReduceExpressionsRule
block|{
specifier|public
name|FilterReduceExpressionsRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|filterClass
parameter_list|,
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|filterClass
argument_list|,
name|relBuilderFactory
argument_list|,
literal|"ReduceExpressionsRule(Filter)"
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
name|List
argument_list|<
name|RexNode
argument_list|>
name|expList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
name|RexNode
name|newConditionExp
decl_stmt|;
name|boolean
name|reduced
decl_stmt|;
specifier|final
name|RelMetadataQuery
name|mq
init|=
name|call
operator|.
name|getMetadataQuery
argument_list|()
decl_stmt|;
specifier|final
name|RelOptPredicateList
name|predicates
init|=
name|mq
operator|.
name|getPulledUpPredicates
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduceExpressions
argument_list|(
name|filter
argument_list|,
name|expList
argument_list|,
name|predicates
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
assert|assert
name|expList
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|newConditionExp
operator|=
name|expList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|reduced
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// No reduction, but let's still test the original
comment|// predicate to see if it was already a constant,
comment|// in which case we don't need any runtime decision
comment|// about filtering.
name|newConditionExp
operator|=
name|filter
operator|.
name|getCondition
argument_list|()
expr_stmt|;
name|reduced
operator|=
literal|false
expr_stmt|;
block|}
comment|// Even if no reduction, let's still test the original
comment|// predicate to see if it was already a constant,
comment|// in which case we don't need any runtime decision
comment|// about filtering.
if|if
condition|(
name|newConditionExp
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|reduced
condition|)
block|{
if|if
condition|(
name|RexUtil
operator|.
name|isNullabilityCast
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|newConditionExp
argument_list|)
condition|)
block|{
name|newConditionExp
operator|=
operator|(
operator|(
name|RexCall
operator|)
name|newConditionExp
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// reduce might end up creating an expression with null type
comment|// e.g condition(null = null) is reduced to condition (null) with null type
comment|// since this is a condition which will always be boolean type we cast it to
comment|// boolean type
if|if
condition|(
name|newConditionExp
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
operator|==
name|SqlTypeName
operator|.
name|NULL
condition|)
block|{
name|newConditionExp
operator|=
name|call
operator|.
name|builder
argument_list|()
operator|.
name|cast
argument_list|(
name|newConditionExp
argument_list|,
name|SqlTypeName
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
block|}
name|call
operator|.
name|transformTo
argument_list|(
name|call
operator|.
name|builder
argument_list|()
operator|.
name|push
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|newConditionExp
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|newConditionExp
operator|instanceof
name|RexCall
condition|)
block|{
name|RexCall
name|rexCall
init|=
operator|(
name|RexCall
operator|)
name|newConditionExp
decl_stmt|;
name|boolean
name|reverse
init|=
name|rexCall
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|NOT
decl_stmt|;
if|if
condition|(
name|reverse
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|rexCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|RexCall
operator|)
condition|)
block|{
comment|// If child is not a RexCall instance, we can bail out
return|return;
block|}
name|rexCall
operator|=
operator|(
name|RexCall
operator|)
name|rexCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|reduceNotNullableFilter
argument_list|(
name|call
argument_list|,
name|filter
argument_list|,
name|rexCall
argument_list|,
name|reverse
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// New plan is absolutely better than old plan.
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|setImportance
argument_list|(
name|filter
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
block|}
comment|/**      * For static schema systems, a filter that is always false or null can be      * replaced by a values operator that produces no rows, as the schema      * information can just be taken from the input Rel. In dynamic schema      * environments, the filter might have an unknown input type, in these cases      * they must define a system specific alternative to a Values operator, such      * as inserting a limit 0 instead of a filter on top of the original input.      *      *<p>The default implementation of this method is to call      * {@link RelBuilder#empty}, which for the static schema will be optimized      * to an empty      * {@link org.apache.calcite.rel.core.Values}.      *      * @param input rel to replace, assumes caller has already determined      *              equivalence to Values operation for 0 records or a      *              false filter.      * @return equivalent but less expensive replacement rel      */
specifier|protected
name|RelNode
name|createEmptyRelOrEquivalent
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|Filter
name|input
parameter_list|)
block|{
return|return
name|call
operator|.
name|builder
argument_list|()
operator|.
name|push
argument_list|(
name|input
argument_list|)
operator|.
name|empty
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|void
name|reduceNotNullableFilter
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|RexCall
name|rexCall
parameter_list|,
name|boolean
name|reverse
parameter_list|)
block|{
comment|// If the expression is a IS [NOT] NULL on a non-nullable
comment|// column, then we can either remove the filter or replace
comment|// it with an Empty.
name|boolean
name|alwaysTrue
decl_stmt|;
switch|switch
condition|(
name|rexCall
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|IS_NULL
case|:
case|case
name|IS_UNKNOWN
case|:
name|alwaysTrue
operator|=
literal|false
expr_stmt|;
break|break;
case|case
name|IS_NOT_NULL
case|:
name|alwaysTrue
operator|=
literal|true
expr_stmt|;
break|break;
default|default:
return|return;
block|}
if|if
condition|(
name|reverse
condition|)
block|{
name|alwaysTrue
operator|=
operator|!
name|alwaysTrue
expr_stmt|;
block|}
name|RexNode
name|operand
init|=
name|rexCall
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|operand
operator|instanceof
name|RexInputRef
condition|)
block|{
name|RexInputRef
name|inputRef
init|=
operator|(
name|RexInputRef
operator|)
name|operand
decl_stmt|;
if|if
condition|(
operator|!
name|inputRef
operator|.
name|getType
argument_list|()
operator|.
name|isNullable
argument_list|()
condition|)
block|{
if|if
condition|(
name|alwaysTrue
condition|)
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|filter
operator|.
name|getInput
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|createEmptyRelOrEquivalent
argument_list|(
name|call
argument_list|,
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

begin_comment
comment|// End HiveReduceExpressionsRule.java
end_comment

end_unit

