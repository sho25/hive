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
name|HiveJoinRel
operator|.
name|JoinAlgorithm
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
name|HiveProjectRel
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
name|rules
operator|.
name|SwapJoinRule
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

begin_class
specifier|public
class|class
name|HiveSwapJoinRule
extends|extends
name|SwapJoinRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveSwapJoinRule
name|INSTANCE
init|=
operator|new
name|HiveSwapJoinRule
argument_list|()
decl_stmt|;
specifier|private
name|HiveSwapJoinRule
parameter_list|()
block|{
name|super
argument_list|(
name|HiveJoinRel
operator|.
name|class
argument_list|,
name|HiveProjectRel
operator|.
name|DEFAULT_PROJECT_FACTORY
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
if|if
condition|(
name|call
operator|.
expr|<
name|HiveJoinRel
operator|>
name|rel
argument_list|(
literal|0
argument_list|)
operator|.
name|isLeftSemiJoin
argument_list|()
condition|)
return|return
literal|false
return|;
else|else
return|return
name|super
operator|.
name|matches
argument_list|(
name|call
argument_list|)
operator|&&
name|call
operator|.
expr|<
name|HiveJoinRel
operator|>
name|rel
argument_list|(
literal|0
argument_list|)
operator|.
name|getJoinAlgorithm
argument_list|()
operator|==
name|JoinAlgorithm
operator|.
name|NONE
return|;
block|}
block|}
end_class

end_unit

