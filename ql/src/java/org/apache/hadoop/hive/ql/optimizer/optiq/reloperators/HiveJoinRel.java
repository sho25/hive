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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|HiveCostUtil
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
name|RelOptUtil
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

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeField
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

begin_comment
comment|//TODO: Should we convert MultiJoin to be a child of HiveJoinRelBase
end_comment

begin_class
specifier|public
class|class
name|HiveJoinRel
extends|extends
name|JoinRelBase
implements|implements
name|HiveRel
block|{
comment|// NOTE: COMMON_JOIN& SMB_JOIN are Sort Merge Join (in case of COMMON_JOIN
comment|// each parallel computation handles multiple splits where as in case of SMB
comment|// each parallel computation handles one bucket). MAP_JOIN and BUCKET_JOIN is
comment|// hash joins where MAP_JOIN keeps the whole data set of non streaming tables
comment|// in memory where as BUCKET_JOIN keeps only the b
specifier|public
enum|enum
name|JoinAlgorithm
block|{
name|NONE
block|,
name|COMMON_JOIN
block|,
name|MAP_JOIN
block|,
name|BUCKET_JOIN
block|,
name|SMB_JOIN
block|}
specifier|public
enum|enum
name|MapJoinStreamingRelation
block|{
name|NONE
block|,
name|LEFT_RELATION
block|,
name|RIGHT_RELATION
block|}
specifier|private
specifier|final
name|boolean
name|m_leftSemiJoin
decl_stmt|;
specifier|private
specifier|final
name|JoinAlgorithm
name|m_joinAlgorithm
decl_stmt|;
specifier|private
name|MapJoinStreamingRelation
name|m_mapJoinStreamingSide
init|=
name|MapJoinStreamingRelation
operator|.
name|NONE
decl_stmt|;
specifier|public
specifier|static
name|HiveJoinRel
name|getJoin
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|boolean
name|leftSemiJoin
parameter_list|)
block|{
try|try
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|variablesStopped
init|=
name|Collections
operator|.
name|emptySet
argument_list|()
decl_stmt|;
return|return
operator|new
name|HiveJoinRel
argument_list|(
name|cluster
argument_list|,
literal|null
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|joinType
argument_list|,
name|variablesStopped
argument_list|,
name|JoinAlgorithm
operator|.
name|NONE
argument_list|,
literal|null
argument_list|,
name|leftSemiJoin
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidRelException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|HiveJoinRel
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traits
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|variablesStopped
parameter_list|,
name|JoinAlgorithm
name|joinAlgo
parameter_list|,
name|MapJoinStreamingRelation
name|streamingSideForMapJoin
parameter_list|,
name|boolean
name|leftSemiJoin
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
name|getJoinTraitSet
argument_list|(
name|cluster
argument_list|,
name|traits
argument_list|)
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|joinType
argument_list|,
name|variablesStopped
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|leftKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|rightKeys
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
name|Integer
argument_list|>
name|filterNulls
init|=
operator|new
name|LinkedList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|RexNode
name|remaining
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|condition
operator|!=
literal|null
condition|)
block|{
name|remaining
operator|=
name|RelOptUtil
operator|.
name|splitJoinCondition
argument_list|(
name|getSystemFieldList
argument_list|()
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|leftKeys
argument_list|,
name|rightKeys
argument_list|,
name|filterNulls
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|remaining
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InvalidRelException
argument_list|(
literal|"EnumerableJoinRel only supports equi-join"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|m_joinAlgorithm
operator|=
name|joinAlgo
expr_stmt|;
name|m_leftSemiJoin
operator|=
name|leftSemiJoin
expr_stmt|;
block|}
annotation|@
name|Override
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
specifier|final
name|HiveJoinRel
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RexNode
name|conditionExpr
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|boolean
name|semiJoinDone
parameter_list|)
block|{
return|return
name|copy
argument_list|(
name|traitSet
argument_list|,
name|conditionExpr
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|m_joinAlgorithm
argument_list|,
name|m_mapJoinStreamingSide
argument_list|,
name|m_leftSemiJoin
argument_list|)
return|;
block|}
specifier|public
name|HiveJoinRel
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RexNode
name|conditionExpr
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|JoinAlgorithm
name|joinalgo
parameter_list|,
name|MapJoinStreamingRelation
name|streamingSide
parameter_list|,
name|boolean
name|semiJoinDone
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|HiveJoinRel
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|conditionExpr
argument_list|,
name|joinType
argument_list|,
name|variablesStopped
argument_list|,
name|joinalgo
argument_list|,
name|streamingSide
argument_list|,
name|semiJoinDone
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
name|JoinAlgorithm
name|getJoinAlgorithm
parameter_list|()
block|{
return|return
name|m_joinAlgorithm
return|;
block|}
specifier|public
name|boolean
name|isLeftSemiJoin
parameter_list|()
block|{
return|return
name|m_leftSemiJoin
return|;
block|}
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
name|HiveCostUtil
operator|.
name|computCardinalityBasedCost
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * @return returns rowtype representing only the left join input    */
specifier|public
name|RelDataType
name|deriveRowType
parameter_list|()
block|{
if|if
condition|(
name|m_leftSemiJoin
condition|)
block|{
return|return
name|deriveJoinRowType
argument_list|(
name|left
operator|.
name|getRowType
argument_list|()
argument_list|,
literal|null
argument_list|,
name|JoinRelType
operator|.
name|INNER
argument_list|,
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
expr|<
name|RelDataTypeField
operator|>
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|deriveRowType
argument_list|()
return|;
block|}
block|}
end_class

end_unit

