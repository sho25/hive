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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_class
specifier|public
class|class
name|DropWMPoolDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|2608462103392563252L
decl_stmt|;
specifier|private
name|String
name|resourcePlanName
decl_stmt|;
specifier|private
name|String
name|poolPath
decl_stmt|;
specifier|public
name|DropWMPoolDesc
parameter_list|()
block|{}
specifier|public
name|DropWMPoolDesc
parameter_list|(
name|String
name|resourcePlanName
parameter_list|,
name|String
name|poolPath
parameter_list|)
block|{
name|this
operator|.
name|resourcePlanName
operator|=
name|resourcePlanName
expr_stmt|;
name|this
operator|.
name|poolPath
operator|=
name|poolPath
expr_stmt|;
block|}
specifier|public
name|String
name|getResourcePlanName
parameter_list|()
block|{
return|return
name|resourcePlanName
return|;
block|}
specifier|public
name|void
name|setResourcePlanName
parameter_list|(
name|String
name|resourcePlanName
parameter_list|)
block|{
name|this
operator|.
name|resourcePlanName
operator|=
name|resourcePlanName
expr_stmt|;
block|}
specifier|public
name|String
name|getPoolPath
parameter_list|()
block|{
return|return
name|poolPath
return|;
block|}
specifier|public
name|void
name|setPoolPath
parameter_list|(
name|String
name|poolPath
parameter_list|)
block|{
name|this
operator|.
name|poolPath
operator|=
name|poolPath
expr_stmt|;
block|}
block|}
end_class

end_unit

