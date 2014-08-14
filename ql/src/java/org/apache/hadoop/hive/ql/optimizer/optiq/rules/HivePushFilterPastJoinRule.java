begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|optiq
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ListIterator
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
name|optiq
operator|.
name|reloperators
operator|.
name|HiveFilterRel
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
name|optiq
operator|.
name|reloperators
operator|.
name|HiveJoinRel
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
name|optiq
operator|.
name|reloperators
operator|.
name|HiveRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|FilterRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|JoinRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|JoinRelType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptRuleCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptRuleOperand
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
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
name|eigenbase
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
name|eigenbase
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
name|eigenbase
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
name|eigenbase
operator|.
name|util
operator|.
name|Holder
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

begin_class
specifier|public
specifier|abstract
class|class
name|HivePushFilterPastJoinRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HivePushFilterPastJoinRule
name|FILTER_ON_JOIN
init|=
operator|new
name|HivePushFilterPastJoinRule
argument_list|(
name|operand
argument_list|(
name|HiveFilterRel
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJoinRel
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"HivePushFilterPastJoinRule:filter"
argument_list|,
literal|true
argument_list|)
block|{
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
name|HiveFilterRel
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HiveJoinRel
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|perform
argument_list|(
name|call
argument_list|,
name|filter
argument_list|,
name|join
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HivePushFilterPastJoinRule
name|JOIN
init|=
operator|new
name|HivePushFilterPastJoinRule
argument_list|(
name|operand
argument_list|(
name|HiveJoinRel
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
literal|"HivePushFilterPastJoinRule:no-filter"
argument_list|,
literal|false
argument_list|)
block|{
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
name|HiveJoinRel
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|perform
argument_list|(
name|call
argument_list|,
literal|null
argument_list|,
name|join
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|/** Whether to try to strengthen join-type. */
specifier|private
specifier|final
name|boolean
name|smart
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
comment|/**    * Creates a PushFilterPastJoinRule with an explicit root operand.    */
specifier|private
name|HivePushFilterPastJoinRule
parameter_list|(
name|RelOptRuleOperand
name|operand
parameter_list|,
name|String
name|id
parameter_list|,
name|boolean
name|smart
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|,
literal|"PushFilterRule: "
operator|+
name|id
argument_list|)
expr_stmt|;
name|this
operator|.
name|smart
operator|=
name|smart
expr_stmt|;
block|}
comment|// ~ Methods ----------------------------------------------------------------
specifier|protected
name|void
name|perform
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|FilterRelBase
name|filter
parameter_list|,
name|JoinRelBase
name|join
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|joinFilters
init|=
name|RelOptUtil
operator|.
name|conjunctions
argument_list|(
name|join
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
comment|/*      * todo: hb 6/26/14 for left SemiJoin we cannot push predicates yet. The      * assertion that num(JoinRel columns) = num(leftSrc) + num(rightSrc)      * doesn't hold. So RelOptUtil.classifyFilters fails.      */
if|if
condition|(
operator|(
operator|(
name|HiveJoinRel
operator|)
name|join
operator|)
operator|.
name|isLeftSemiJoin
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
comment|// There is only the joinRel
comment|// make sure it does not match a cartesian product joinRel
comment|// (with "true" condition) otherwise this rule will be applied
comment|// again on the new cartesian product joinRel.
name|boolean
name|onlyTrueFilter
init|=
literal|true
decl_stmt|;
for|for
control|(
name|RexNode
name|joinFilter
range|:
name|joinFilters
control|)
block|{
if|if
condition|(
operator|!
name|joinFilter
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
name|onlyTrueFilter
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|onlyTrueFilter
condition|)
block|{
return|return;
block|}
block|}
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|aboveFilters
init|=
name|filter
operator|!=
literal|null
condition|?
name|RelOptUtil
operator|.
name|conjunctions
argument_list|(
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|)
else|:
name|ImmutableList
operator|.
expr|<
name|RexNode
operator|>
name|of
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|leftFilters
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|rightFilters
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|origJoinFiltersSz
init|=
name|joinFilters
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// TODO - add logic to derive additional filters. E.g., from
comment|// (t1.a = 1 AND t2.a = 2) OR (t1.b = 3 AND t2.b = 4), you can
comment|// derive table filters:
comment|// (t1.a = 1 OR t1.b = 3)
comment|// (t2.a = 2 OR t2.b = 4)
comment|// Try to push down above filters. These are typically where clause
comment|// filters. They can be pushed down if they are not on the NULL
comment|// generating side.
name|boolean
name|filterPushed
init|=
literal|false
decl_stmt|;
specifier|final
name|Holder
argument_list|<
name|JoinRelType
argument_list|>
name|joinTypeHolder
init|=
name|Holder
operator|.
name|of
argument_list|(
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|RelOptUtil
operator|.
name|classifyFilters
argument_list|(
name|join
argument_list|,
name|aboveFilters
argument_list|,
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|,
operator|!
name|join
operator|.
name|getJoinType
argument_list|()
operator|.
name|generatesNullsOnLeft
argument_list|()
argument_list|,
operator|!
name|join
operator|.
name|getJoinType
argument_list|()
operator|.
name|generatesNullsOnRight
argument_list|()
argument_list|,
name|joinFilters
argument_list|,
name|leftFilters
argument_list|,
name|rightFilters
argument_list|,
name|joinTypeHolder
argument_list|,
name|smart
argument_list|)
condition|)
block|{
name|filterPushed
operator|=
literal|true
expr_stmt|;
block|}
comment|/*      * Any predicates pushed down to joinFilters that aren't equality      * conditions: put them back as aboveFilters because Hive doesn't support      * not equi join conditions.      */
name|ListIterator
argument_list|<
name|RexNode
argument_list|>
name|filterIter
init|=
name|joinFilters
operator|.
name|listIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|filterIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|RexNode
name|exp
init|=
name|filterIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|exp
operator|instanceof
name|RexCall
condition|)
block|{
name|RexCall
name|c
init|=
operator|(
name|RexCall
operator|)
name|exp
decl_stmt|;
if|if
condition|(
name|c
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
continue|continue;
block|}
block|}
name|aboveFilters
operator|.
name|add
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|filterIter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|/*      * if all pushed filters where put back then set filterPushed to false      */
if|if
condition|(
name|leftFilters
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|rightFilters
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|joinFilters
operator|.
name|size
argument_list|()
operator|==
name|origJoinFiltersSz
condition|)
block|{
name|filterPushed
operator|=
literal|false
expr_stmt|;
block|}
comment|// Try to push down filters in ON clause. A ON clause filter can only be
comment|// pushed down if it does not affect the non-matching set, i.e. it is
comment|// not on the side which is preserved.
if|if
condition|(
name|RelOptUtil
operator|.
name|classifyFilters
argument_list|(
name|join
argument_list|,
name|joinFilters
argument_list|,
literal|null
argument_list|,
operator|!
name|join
operator|.
name|getJoinType
argument_list|()
operator|.
name|generatesNullsOnRight
argument_list|()
argument_list|,
operator|!
name|join
operator|.
name|getJoinType
argument_list|()
operator|.
name|generatesNullsOnLeft
argument_list|()
argument_list|,
name|joinFilters
argument_list|,
name|leftFilters
argument_list|,
name|rightFilters
argument_list|,
name|joinTypeHolder
argument_list|,
name|smart
argument_list|)
condition|)
block|{
name|filterPushed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|filterPushed
condition|)
block|{
return|return;
block|}
comment|/*      * Remove always true conditions that got pushed down.      */
name|removeAlwaysTruePredicates
argument_list|(
name|leftFilters
argument_list|)
expr_stmt|;
name|removeAlwaysTruePredicates
argument_list|(
name|rightFilters
argument_list|)
expr_stmt|;
name|removeAlwaysTruePredicates
argument_list|(
name|joinFilters
argument_list|)
expr_stmt|;
comment|// create FilterRels on top of the children if any filters were
comment|// pushed to them
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
name|RelNode
name|leftRel
init|=
name|createFilterOnRel
argument_list|(
name|rexBuilder
argument_list|,
name|join
operator|.
name|getLeft
argument_list|()
argument_list|,
name|leftFilters
argument_list|)
decl_stmt|;
name|RelNode
name|rightRel
init|=
name|createFilterOnRel
argument_list|(
name|rexBuilder
argument_list|,
name|join
operator|.
name|getRight
argument_list|()
argument_list|,
name|rightFilters
argument_list|)
decl_stmt|;
comment|// create the new join node referencing the new children and
comment|// containing its new join filters (if there are any)
name|RexNode
name|joinFilter
decl_stmt|;
if|if
condition|(
name|joinFilters
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// if nothing actually got pushed and there is nothing leftover,
comment|// then this rule is a no-op
if|if
condition|(
name|leftFilters
operator|.
name|isEmpty
argument_list|()
operator|&&
name|rightFilters
operator|.
name|isEmpty
argument_list|()
operator|&&
name|joinTypeHolder
operator|.
name|get
argument_list|()
operator|==
name|join
operator|.
name|getJoinType
argument_list|()
condition|)
block|{
return|return;
block|}
name|joinFilter
operator|=
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|joinFilter
operator|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|joinFilters
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|RelNode
name|newJoinRel
init|=
name|HiveJoinRel
operator|.
name|getJoin
argument_list|(
name|join
operator|.
name|getCluster
argument_list|()
argument_list|,
name|leftRel
argument_list|,
name|rightRel
argument_list|,
name|joinFilter
argument_list|,
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// create a FilterRel on top of the join if needed
name|RelNode
name|newRel
init|=
name|createFilterOnRel
argument_list|(
name|rexBuilder
argument_list|,
name|newJoinRel
argument_list|,
name|aboveFilters
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newRel
argument_list|)
expr_stmt|;
block|}
comment|/**    * If the filter list passed in is non-empty, creates a FilterRel on top of    * the existing RelNode; otherwise, just returns the RelNode    *    * @param rexBuilder    *          rex builder    * @param rel    *          the RelNode that the filter will be put on top of    * @param filters    *          list of filters    * @return new RelNode or existing one if no filters    */
specifier|private
name|RelNode
name|createFilterOnRel
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|RelNode
name|rel
parameter_list|,
name|List
argument_list|<
name|RexNode
argument_list|>
name|filters
parameter_list|)
block|{
name|RexNode
name|andFilters
init|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|rexBuilder
argument_list|,
name|filters
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|andFilters
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
return|return
name|rel
return|;
block|}
return|return
operator|new
name|HiveFilterRel
argument_list|(
name|rel
operator|.
name|getCluster
argument_list|()
argument_list|,
name|rel
operator|.
name|getCluster
argument_list|()
operator|.
name|traitSetOf
argument_list|(
name|HiveRel
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|rel
argument_list|,
name|andFilters
argument_list|)
return|;
block|}
specifier|private
name|void
name|removeAlwaysTruePredicates
parameter_list|(
name|List
argument_list|<
name|RexNode
argument_list|>
name|predicates
parameter_list|)
block|{
if|if
condition|(
name|predicates
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
return|return;
block|}
name|ListIterator
argument_list|<
name|RexNode
argument_list|>
name|iter
init|=
name|predicates
operator|.
name|listIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|RexNode
name|exp
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|isAlwaysTrue
argument_list|(
name|exp
argument_list|)
condition|)
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|boolean
name|isAlwaysTrue
parameter_list|(
name|RexNode
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|predicate
operator|instanceof
name|RexCall
condition|)
block|{
name|RexCall
name|c
init|=
operator|(
name|RexCall
operator|)
name|predicate
decl_stmt|;
if|if
condition|(
name|c
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
return|return
name|isAlwaysTrue
argument_list|(
name|c
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|&&
name|isAlwaysTrue
argument_list|(
name|c
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
name|predicate
operator|.
name|isAlwaysTrue
argument_list|()
return|;
block|}
block|}
end_class

begin_comment
comment|// End PushFilterPastJoinRule.java
end_comment

end_unit

