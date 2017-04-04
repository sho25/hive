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
name|metastore
operator|.
name|messaging
operator|.
name|event
operator|.
name|filters
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
name|metastore
operator|.
name|IMetaStoreClient
operator|.
name|NotificationFilter
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
name|metastore
operator|.
name|api
operator|.
name|NotificationEvent
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|BasicFilter
implements|implements
name|NotificationFilter
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
specifier|final
name|NotificationEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
comment|// get rid of trivial case first, so that we can safely assume non-null
block|}
return|return
name|shouldAccept
argument_list|(
name|event
argument_list|)
return|;
block|}
specifier|abstract
name|boolean
name|shouldAccept
parameter_list|(
specifier|final
name|NotificationEvent
name|event
parameter_list|)
function_decl|;
block|}
end_class

end_unit

