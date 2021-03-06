begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|hbase
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
name|fs
operator|.
name|FileSystem
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
name|FileUtil
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|hbase
operator|.
name|HConstants
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
name|hbase
operator|.
name|MiniHBaseCluster
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
name|hbase
operator|.
name|TableName
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|hbase
operator|.
name|client
operator|.
name|ConnectionFactory
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
name|hbase
operator|.
name|client
operator|.
name|Table
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|HiveMetaStoreClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|MiniMRCluster
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
name|testutils
operator|.
name|MiniZooKeeperCluster
import|;
end_import

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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_comment
comment|/**  * MiniCluster class composed of a number of Hadoop Minicluster implementations  * and other necessary daemons needed for testing (HBase, Hive MetaStore, Zookeeper, MiniMRCluster)  */
end_comment

begin_class
specifier|public
class|class
name|ManyMiniCluster
block|{
comment|//MR stuff
specifier|private
name|boolean
name|miniMRClusterEnabled
decl_stmt|;
specifier|private
name|MiniMRCluster
name|mrCluster
decl_stmt|;
specifier|private
name|int
name|numTaskTrackers
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
decl_stmt|;
comment|//HBase stuff
specifier|private
name|boolean
name|miniHBaseClusterEnabled
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|hbaseCluster
decl_stmt|;
specifier|private
name|String
name|hbaseRoot
decl_stmt|;
specifier|private
name|Configuration
name|hbaseConf
decl_stmt|;
specifier|private
name|String
name|hbaseDir
decl_stmt|;
comment|//ZK Stuff
specifier|private
name|boolean
name|miniZookeeperClusterEnabled
decl_stmt|;
specifier|private
name|MiniZooKeeperCluster
name|zookeeperCluster
decl_stmt|;
specifier|private
name|int
name|zookeeperPort
decl_stmt|;
specifier|private
name|String
name|zookeeperDir
decl_stmt|;
comment|//DFS Stuff
specifier|private
name|MiniDFSCluster
name|dfsCluster
decl_stmt|;
comment|//Hive Stuff
specifier|private
name|boolean
name|miniHiveMetastoreEnabled
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|hiveMetaStoreClient
decl_stmt|;
specifier|private
specifier|final
name|File
name|workDir
decl_stmt|;
specifier|private
name|boolean
name|started
init|=
literal|false
decl_stmt|;
comment|/**    * create a cluster instance using a builder which will expose configurable options    * @param workDir working directory ManyMiniCluster will use for all of it's *Minicluster instances    * @return a Builder instance    */
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|File
name|workDir
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|workDir
argument_list|)
return|;
block|}
specifier|private
name|ManyMiniCluster
parameter_list|(
name|Builder
name|b
parameter_list|)
block|{
name|workDir
operator|=
name|b
operator|.
name|workDir
expr_stmt|;
name|numTaskTrackers
operator|=
name|b
operator|.
name|numTaskTrackers
expr_stmt|;
name|hiveConf
operator|=
name|b
operator|.
name|hiveConf
expr_stmt|;
name|jobConf
operator|=
name|b
operator|.
name|jobConf
expr_stmt|;
name|hbaseConf
operator|=
name|b
operator|.
name|hbaseConf
expr_stmt|;
name|miniMRClusterEnabled
operator|=
name|b
operator|.
name|miniMRClusterEnabled
expr_stmt|;
name|miniHBaseClusterEnabled
operator|=
name|b
operator|.
name|miniHBaseClusterEnabled
expr_stmt|;
name|miniHiveMetastoreEnabled
operator|=
name|b
operator|.
name|miniHiveMetastoreEnabled
expr_stmt|;
name|miniZookeeperClusterEnabled
operator|=
name|b
operator|.
name|miniZookeeperClusterEnabled
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|started
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
if|if
condition|(
name|miniMRClusterEnabled
condition|)
block|{
name|setupMRCluster
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|miniZookeeperClusterEnabled
operator|||
name|miniHBaseClusterEnabled
condition|)
block|{
name|miniZookeeperClusterEnabled
operator|=
literal|true
expr_stmt|;
name|setupZookeeper
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|miniHBaseClusterEnabled
condition|)
block|{
name|setupHBaseCluster
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|miniHiveMetastoreEnabled
condition|)
block|{
name|setUpMetastore
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to setup cluster"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|hbaseCluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|hbaseCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|hbaseCluster
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|zookeeperCluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|zookeeperCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|zookeeperCluster
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|mrCluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|mrCluster
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|dfsCluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|dfsCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|dfsCluster
operator|=
literal|null
expr_stmt|;
block|}
try|try
block|{
name|FileSystem
operator|.
name|closeAll
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|started
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * @return Configuration of mini HBase cluster    */
specifier|public
name|Configuration
name|getHBaseConf
parameter_list|()
block|{
return|return
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|hbaseConf
argument_list|)
return|;
block|}
comment|/**    * @return Configuration of mini MR cluster    */
specifier|public
name|Configuration
name|getJobConf
parameter_list|()
block|{
return|return
operator|new
name|Configuration
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
comment|/**    * @return Configuration of Hive Metastore, this is a standalone not a daemon    */
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
return|;
block|}
comment|/**    * @return Filesystem used by MiniMRCluster and MiniHBaseCluster    */
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
try|try
block|{
return|return
name|FileSystem
operator|.
name|get
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to get FileSystem"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return Metastore client instance    */
specifier|public
name|HiveMetaStoreClient
name|getHiveMetaStoreClient
parameter_list|()
block|{
return|return
name|hiveMetaStoreClient
return|;
block|}
specifier|private
name|void
name|setupMRCluster
parameter_list|()
block|{
try|try
block|{
specifier|final
name|int
name|jobTrackerPort
init|=
name|findFreePort
argument_list|()
decl_stmt|;
specifier|final
name|int
name|taskTrackerPort
init|=
name|findFreePort
argument_list|()
decl_stmt|;
if|if
condition|(
name|jobConf
operator|==
literal|null
condition|)
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
name|jobConf
operator|.
name|setInt
argument_list|(
literal|"mapred.submit.replication"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.queues"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.default.capacity"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
comment|//conf.set("hadoop.job.history.location",new File(workDir).getAbsolutePath()+"/history");
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"/logs"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|mrCluster
operator|=
operator|new
name|MiniMRCluster
argument_list|(
name|jobTrackerPort
argument_list|,
name|taskTrackerPort
argument_list|,
name|numTaskTrackers
argument_list|,
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|numTaskTrackers
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|jobConf
operator|=
name|mrCluster
operator|.
name|createJobConf
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to Setup MR Cluster"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|setupZookeeper
parameter_list|()
block|{
try|try
block|{
name|zookeeperDir
operator|=
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"zk"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
name|zookeeperPort
operator|=
name|findFreePort
argument_list|()
expr_stmt|;
name|zookeeperCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|()
expr_stmt|;
name|zookeeperCluster
operator|.
name|setDefaultClientPort
argument_list|(
name|zookeeperPort
argument_list|)
expr_stmt|;
name|zookeeperCluster
operator|.
name|startup
argument_list|(
operator|new
name|File
argument_list|(
name|zookeeperDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to Setup Zookeeper Cluster"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|setupHBaseCluster
parameter_list|()
block|{
specifier|final
name|int
name|numRegionServers
init|=
literal|1
decl_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|hbaseDir
operator|=
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"hbase"
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
expr_stmt|;
name|hbaseDir
operator|=
name|hbaseDir
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
name|hbaseRoot
operator|=
literal|"file:///"
operator|+
name|hbaseDir
expr_stmt|;
if|if
condition|(
name|hbaseConf
operator|==
literal|null
condition|)
name|hbaseConf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|hbaseConf
operator|.
name|set
argument_list|(
literal|"hbase.rootdir"
argument_list|,
name|hbaseRoot
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|set
argument_list|(
literal|"hbase.master"
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|zookeeperPort
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.port"
argument_list|,
name|findFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.port"
argument_list|,
name|findFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|hbaseCluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|hbaseConf
argument_list|,
name|numRegionServers
argument_list|)
expr_stmt|;
name|hbaseConf
operator|.
name|set
argument_list|(
literal|"hbase.master"
argument_list|,
name|hbaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
expr_stmt|;
comment|//opening the META table ensures that cluster is running
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|hbaseConf
argument_list|)
expr_stmt|;
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to setup HBase Cluster"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|setUpMetastore
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveConf
operator|==
literal|null
condition|)
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|//The default org.apache.hadoop.hive.ql.hooks.PreExecutePrinter hook
comment|//is present only in the ql/test directory
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
operator|.
name|varname
argument_list|,
literal|"jdbc:derby:"
operator|+
operator|new
name|File
argument_list|(
name|workDir
operator|+
literal|"/metastore_db"
argument_list|)
operator|+
literal|";create=true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"warehouse"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|//set where derby logs
name|File
name|derbyLogFile
init|=
operator|new
name|File
argument_list|(
name|workDir
operator|+
literal|"/derby.log"
argument_list|)
decl_stmt|;
name|derbyLogFile
operator|.
name|createNewFile
argument_list|()
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"derby.stream.error.file"
argument_list|,
name|derbyLogFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|//    Driver driver = new Driver(conf);
comment|//    SessionState.start(new CliSessionState(conf));
name|hiveMetaStoreClient
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|findFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|server
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|server
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
name|server
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|port
return|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|File
name|workDir
decl_stmt|;
specifier|private
name|int
name|numTaskTrackers
init|=
literal|1
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
name|Configuration
name|hbaseConf
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|boolean
name|miniMRClusterEnabled
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|miniHBaseClusterEnabled
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|miniHiveMetastoreEnabled
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|miniZookeeperClusterEnabled
init|=
literal|true
decl_stmt|;
specifier|private
name|Builder
parameter_list|(
name|File
name|workDir
parameter_list|)
block|{
name|this
operator|.
name|workDir
operator|=
name|workDir
expr_stmt|;
block|}
specifier|public
name|Builder
name|numTaskTrackers
parameter_list|(
name|int
name|num
parameter_list|)
block|{
name|numTaskTrackers
operator|=
name|num
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|jobConf
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
block|{
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|hbaseConf
parameter_list|(
name|Configuration
name|hbaseConf
parameter_list|)
block|{
name|this
operator|.
name|hbaseConf
operator|=
name|hbaseConf
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|hiveConf
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|miniMRClusterEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|miniMRClusterEnabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|miniHBaseClusterEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|miniHBaseClusterEnabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|miniZookeeperClusterEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|miniZookeeperClusterEnabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|miniHiveMetastoreEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|miniHiveMetastoreEnabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ManyMiniCluster
name|build
parameter_list|()
block|{
return|return
operator|new
name|ManyMiniCluster
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

