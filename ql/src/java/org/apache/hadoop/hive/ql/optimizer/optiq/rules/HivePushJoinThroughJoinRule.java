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
name|rules
operator|.
name|PushJoinThroughJoinRule
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

begin_class
specifier|public
class|class
name|HivePushJoinThroughJoinRule
extends|extends
name|PushJoinThroughJoinRule
block|{
specifier|public
specifier|static
specifier|final
name|RelOptRule
name|RIGHT
init|=
operator|new
name|HivePushJoinThroughJoinRule
argument_list|(
literal|"Hive PushJoinThroughJoinRule:right"
argument_list|,
literal|true
argument_list|,
name|HiveJoinRel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelOptRule
name|LEFT
init|=
operator|new
name|HivePushJoinThroughJoinRule
argument_list|(
literal|"Hive PushJoinThroughJoinRule:left"
argument_list|,
literal|false
argument_list|,
name|HiveJoinRel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HivePushJoinThroughJoinRule
parameter_list|(
name|String
name|description
parameter_list|,
name|boolean
name|right
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|JoinRelBase
argument_list|>
name|clazz
parameter_list|)
block|{
name|super
argument_list|(
name|description
argument_list|,
name|right
argument_list|,
name|clazz
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
name|boolean
name|isAMatch
init|=
literal|false
decl_stmt|;
specifier|final
name|HiveJoinRel
name|topJoin
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveJoinRel
name|bottomJoin
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|topJoin
operator|.
name|getJoinAlgorithm
argument_list|()
operator|==
name|JoinAlgorithm
operator|.
name|NONE
operator|&&
name|bottomJoin
operator|.
name|getJoinAlgorithm
argument_list|()
operator|==
name|JoinAlgorithm
operator|.
name|NONE
condition|)
block|{
name|isAMatch
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|isAMatch
return|;
block|}
block|}
end_class

end_unit

