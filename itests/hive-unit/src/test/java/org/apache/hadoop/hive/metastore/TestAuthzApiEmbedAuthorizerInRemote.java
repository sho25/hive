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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_comment
comment|/**  * Test {@link TestAuthorizationApiAuthorizer} in remote mode of metastore  */
end_comment

begin_class
specifier|public
class|class
name|TestAuthzApiEmbedAuthorizerInRemote
extends|extends
name|TestAuthorizationApiAuthorizer
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|isRemoteMetastoreMode
operator|=
literal|true
expr_stmt|;
comment|// remote metastore mode
name|TestAuthorizationApiAuthorizer
operator|.
name|setup
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

