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
name|reloperators
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
name|util
operator|.
name|BitSets
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
name|TraitsUtil
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
name|eigenbase
operator|.
name|rel
operator|.
name|AggregateCall
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
name|AggregateRelBase
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
name|InvalidRelException
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
name|RelOptCluster
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
name|relopt
operator|.
name|RelOptPlanner
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
name|RelTraitSet
import|;
end_import

begin_class
specifier|public
class|class
name|HiveAggregateRel
extends|extends
name|AggregateRelBase
implements|implements
name|HiveRel
block|{
specifier|public
name|HiveAggregateRel
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|child
parameter_list|,
name|BitSet
name|groupSet
parameter_list|,
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggCalls
parameter_list|)
throws|throws
name|InvalidRelException
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|TraitsUtil
operator|.
name|getAggregateTraitSet
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|BitSets
operator|.
name|toList
argument_list|(
name|groupSet
argument_list|)
argument_list|,
name|aggCalls
argument_list|,
name|child
argument_list|)
argument_list|,
name|child
argument_list|,
name|groupSet
argument_list|,
name|aggCalls
argument_list|)
expr_stmt|;
block|}
specifier|public
name|AggregateRelBase
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|input
parameter_list|,
name|BitSet
name|groupSet
parameter_list|,
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggCalls
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|HiveAggregateRel
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|input
argument_list|,
name|groupSet
argument_list|,
name|aggCalls
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidRelException
name|e
parameter_list|)
block|{
comment|// Semantic error not possible. Must be a bug. Convert to
comment|// internal error.
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|implement
parameter_list|(
name|Implementor
name|implementor
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|RelOptCost
name|computeSelfCost
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|)
block|{
return|return
name|HiveCost
operator|.
name|FACTORY
operator|.
name|makeZeroCost
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getRows
parameter_list|()
block|{
return|return
name|RelMetadataQuery
operator|.
name|getDistinctRowCount
argument_list|(
name|this
argument_list|,
name|groupSet
argument_list|,
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
block|}
end_class

end_unit

