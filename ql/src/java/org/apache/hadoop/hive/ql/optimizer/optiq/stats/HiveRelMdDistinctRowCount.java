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
name|stats
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
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
name|net
operator|.
name|hydromatic
operator|.
name|optiq
operator|.
name|BuiltinMethod
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
name|HiveOptiqUtil
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
name|cost
operator|.
name|HiveCost
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
name|HiveTableScanRel
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
name|RelNode
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
name|metadata
operator|.
name|ChainedRelMetadataProvider
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
name|metadata
operator|.
name|ReflectiveRelMetadataProvider
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
name|metadata
operator|.
name|RelMdDistinctRowCount
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
name|metadata
operator|.
name|RelMdUtil
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
name|metadata
operator|.
name|RelMetadataProvider
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
name|metadata
operator|.
name|RelMetadataQuery
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
name|RelOptCost
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
class|class
name|HiveRelMdDistinctRowCount
extends|extends
name|RelMdDistinctRowCount
block|{
specifier|private
specifier|static
specifier|final
name|HiveRelMdDistinctRowCount
name|INSTANCE
init|=
operator|new
name|HiveRelMdDistinctRowCount
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ChainedRelMetadataProvider
operator|.
name|of
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltinMethod
operator|.
name|DISTINCT_ROW_COUNT
operator|.
name|method
argument_list|,
name|INSTANCE
argument_list|)
argument_list|,
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltinMethod
operator|.
name|CUMULATIVE_COST
operator|.
name|method
argument_list|,
name|INSTANCE
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|HiveRelMdDistinctRowCount
parameter_list|()
block|{   }
comment|// Catch-all rule when none of the others apply.
annotation|@
name|Override
specifier|public
name|Double
name|getDistinctRowCount
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|BitSet
name|groupKey
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|HiveTableScanRel
condition|)
block|{
return|return
name|getDistinctRowCount
argument_list|(
operator|(
name|HiveTableScanRel
operator|)
name|rel
argument_list|,
name|groupKey
argument_list|,
name|predicate
argument_list|)
return|;
block|}
comment|/*      * For now use Optiq' default formulas for propagating NDVs up the Query      * Tree.      */
return|return
name|super
operator|.
name|getDistinctRowCount
argument_list|(
name|rel
argument_list|,
name|groupKey
argument_list|,
name|predicate
argument_list|)
return|;
block|}
specifier|private
name|Double
name|getDistinctRowCount
parameter_list|(
name|HiveTableScanRel
name|htRel
parameter_list|,
name|BitSet
name|groupKey
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|projIndxLst
init|=
name|HiveOptiqUtil
operator|.
name|translateBitSetToProjIndx
argument_list|(
name|groupKey
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
init|=
name|htRel
operator|.
name|getColStat
argument_list|(
name|projIndxLst
argument_list|)
decl_stmt|;
name|Double
name|noDistinctRows
init|=
literal|1.0
decl_stmt|;
for|for
control|(
name|ColStatistics
name|cStat
range|:
name|colStats
control|)
block|{
name|noDistinctRows
operator|*=
name|cStat
operator|.
name|getCountDistint
argument_list|()
expr_stmt|;
block|}
return|return
name|Math
operator|.
name|min
argument_list|(
name|noDistinctRows
argument_list|,
name|htRel
operator|.
name|getRows
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Double
name|getDistinctRowCount
parameter_list|(
name|RelNode
name|r
parameter_list|,
name|int
name|indx
parameter_list|)
block|{
name|BitSet
name|bitSetOfRqdProj
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
name|bitSetOfRqdProj
operator|.
name|set
argument_list|(
name|indx
argument_list|)
expr_stmt|;
return|return
name|RelMetadataQuery
operator|.
name|getDistinctRowCount
argument_list|(
name|r
argument_list|,
name|bitSetOfRqdProj
argument_list|,
name|r
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|getDistinctRowCount
parameter_list|(
name|JoinRelBase
name|rel
parameter_list|,
name|BitSet
name|groupKey
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|HiveJoinRel
condition|)
block|{
name|HiveJoinRel
name|hjRel
init|=
operator|(
name|HiveJoinRel
operator|)
name|rel
decl_stmt|;
comment|//TODO: Improve this
if|if
condition|(
name|hjRel
operator|.
name|isLeftSemiJoin
argument_list|()
condition|)
block|{
return|return
name|RelMetadataQuery
operator|.
name|getDistinctRowCount
argument_list|(
name|hjRel
operator|.
name|getLeft
argument_list|()
argument_list|,
name|groupKey
argument_list|,
name|rel
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|RelMdUtil
operator|.
name|getJoinDistinctRowCount
argument_list|(
name|rel
argument_list|,
name|rel
operator|.
name|getJoinType
argument_list|()
argument_list|,
name|groupKey
argument_list|,
name|predicate
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
return|return
name|RelMetadataQuery
operator|.
name|getDistinctRowCount
argument_list|(
name|rel
argument_list|,
name|groupKey
argument_list|,
name|predicate
argument_list|)
return|;
block|}
comment|/*    * Favor Broad Plans over Deep Plans.     */
specifier|public
name|RelOptCost
name|getCumulativeCost
parameter_list|(
name|HiveJoinRel
name|rel
parameter_list|)
block|{
name|RelOptCost
name|cost
init|=
name|RelMetadataQuery
operator|.
name|getNonCumulativeCost
argument_list|(
name|rel
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RelNode
argument_list|>
name|inputs
init|=
name|rel
operator|.
name|getInputs
argument_list|()
decl_stmt|;
name|RelOptCost
name|maxICost
init|=
name|HiveCost
operator|.
name|ZERO
decl_stmt|;
for|for
control|(
name|RelNode
name|input
range|:
name|inputs
control|)
block|{
name|RelOptCost
name|iCost
init|=
name|RelMetadataQuery
operator|.
name|getCumulativeCost
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxICost
operator|.
name|isLt
argument_list|(
name|iCost
argument_list|)
condition|)
block|{
name|maxICost
operator|=
name|iCost
expr_stmt|;
block|}
block|}
return|return
name|cost
operator|.
name|plus
argument_list|(
name|maxICost
argument_list|)
return|;
block|}
block|}
end_class

end_unit

