begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|fs
operator|.
name|Path
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
operator|.
name|MiniClusterType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|StartMiniHS2Cluster
block|{
specifier|private
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
comment|/**    * Not a unit test - this simply runs a MiniHS2 cluster, which can be used for manual testing.    */
annotation|@
name|Test
specifier|public
name|void
name|testRunCluster
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"miniHS2.run"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
condition|)
block|{
return|return;
block|}
name|MiniClusterType
name|clusterType
init|=
name|MiniClusterType
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"miniHS2.clusterType"
argument_list|,
literal|"MR"
argument_list|)
operator|.
name|toUpperCase
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|confFilesProperty
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"miniHS2.conf"
argument_list|,
literal|"../../data/conf/hive-site.xml"
argument_list|)
decl_stmt|;
name|boolean
name|usePortsFromConf
init|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"miniHS2.usePortsFromConf"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Load conf files
name|String
index|[]
name|confFiles
init|=
name|confFilesProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|int
name|idx
decl_stmt|;
for|for
control|(
name|idx
operator|=
literal|0
init|;
name|idx
operator|<
name|confFiles
operator|.
name|length
condition|;
operator|++
name|idx
control|)
block|{
name|String
name|confFile
init|=
name|confFiles
index|[
name|idx
index|]
decl_stmt|;
if|if
condition|(
name|confFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confFile
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_RPC_QUERY_PLAN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
init|;
name|idx
operator|<
name|confFiles
operator|.
name|length
condition|;
operator|++
name|idx
control|)
block|{
name|String
name|confFile
init|=
name|confFiles
index|[
name|idx
index|]
decl_stmt|;
if|if
condition|(
name|confFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confFile
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|,
name|clusterType
argument_list|,
name|usePortsFromConf
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|getDFS
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/apps_staging_dir/anonymous"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"JDBC URL avaailable at "
operator|+
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|)
expr_stmt|;
comment|// MiniHS2 cluster is up .. let it run until someone kills the test
while|while
condition|(
literal|true
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

