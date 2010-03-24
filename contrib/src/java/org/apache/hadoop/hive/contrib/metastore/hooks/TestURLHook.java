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
name|contrib
operator|.
name|metastore
operator|.
name|hooks
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|hooks
operator|.
name|JDOConnectionURLHook
import|;
end_import

begin_comment
comment|/**  * First returns a url for a blank DB, then returns a URL for the original DB.  * For testing the feature in url_hook.q  */
end_comment

begin_class
specifier|public
class|class
name|TestURLHook
implements|implements
name|JDOConnectionURLHook
block|{
specifier|static
name|String
name|originalUrl
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getJdoConnectionUrl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|originalUrl
operator|==
literal|null
condition|)
block|{
name|originalUrl
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
return|return
literal|"jdbc:derby:;databaseName=../build/test/junit_metastore_db_blank;create=true"
return|;
block|}
else|else
block|{
return|return
name|originalUrl
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyBadConnectionUrl
parameter_list|(
name|String
name|url
parameter_list|)
block|{    }
block|}
end_class

end_unit

