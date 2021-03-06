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
name|io
operator|.
name|FileOutputStream
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
name|io
operator|.
name|OutputStreamWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|FSDataOutputStream
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

begin_comment
comment|/**  * This class builds a single instance of itself with the Singleton  * design pattern. While building the single instance, it sets up a  * mini cluster that actually consists of a mini DFS cluster and a  * mini MapReduce cluster on the local machine and also sets up the  * environment for Pig to run on top of the mini cluster.  */
end_comment

begin_class
specifier|public
class|class
name|MiniCluster
block|{
specifier|private
name|MiniDFSCluster
name|m_dfs
init|=
literal|null
decl_stmt|;
specifier|private
name|MiniMRCluster
name|m_mr
init|=
literal|null
decl_stmt|;
specifier|private
name|FileSystem
name|m_fileSys
init|=
literal|null
decl_stmt|;
specifier|private
name|JobConf
name|m_conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|MiniCluster
name|INSTANCE
init|=
operator|new
name|MiniCluster
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isSetup
init|=
literal|true
decl_stmt|;
specifier|private
name|MiniCluster
parameter_list|()
block|{
name|setupMiniDfsAndMrClusters
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setupMiniDfsAndMrClusters
parameter_list|()
block|{
try|try
block|{
specifier|final
name|int
name|dataNodes
init|=
literal|1
decl_stmt|;
comment|// There will be 4 data nodes
specifier|final
name|int
name|taskTrackers
init|=
literal|1
decl_stmt|;
comment|// There will be 4 task tracker nodes
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
comment|// Builds and starts the mini dfs and mapreduce clusters
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
literal|"target/tmp/logs/"
argument_list|)
expr_stmt|;
block|}
name|m_dfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|config
argument_list|,
name|dataNodes
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|m_fileSys
operator|=
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|m_mr
operator|=
operator|new
name|MiniMRCluster
argument_list|(
name|taskTrackers
argument_list|,
name|m_fileSys
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Create the configuration hadoop-site.xml file
name|File
name|conf_dir
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.home"
argument_list|)
argument_list|,
literal|"pigtest/conf/"
argument_list|)
decl_stmt|;
name|conf_dir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|File
name|conf_file
init|=
operator|new
name|File
argument_list|(
name|conf_dir
argument_list|,
literal|"hadoop-site.xml"
argument_list|)
decl_stmt|;
comment|// Write the necessary config info to hadoop-site.xml
name|m_conf
operator|=
name|m_mr
operator|.
name|createJobConf
argument_list|()
expr_stmt|;
name|m_conf
operator|.
name|setInt
argument_list|(
literal|"mapred.submit.replication"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|m_conf
operator|.
name|set
argument_list|(
literal|"dfs.datanode.address"
argument_list|,
literal|"0.0.0.0:0"
argument_list|)
expr_stmt|;
name|m_conf
operator|.
name|set
argument_list|(
literal|"dfs.datanode.http.address"
argument_list|,
literal|"0.0.0.0:0"
argument_list|)
expr_stmt|;
name|m_conf
operator|.
name|writeXml
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|conf_file
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set the system properties needed by Pig
name|System
operator|.
name|setProperty
argument_list|(
literal|"cluster"
argument_list|,
name|m_conf
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"namenode"
argument_list|,
name|m_conf
operator|.
name|get
argument_list|(
literal|"fs.default.name"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"junit.hadoop.conf"
argument_list|,
name|conf_dir
operator|.
name|getPath
argument_list|()
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Returns the single instance of class MiniClusterBuilder that    * represents the resouces for a mini dfs cluster and a mini    * mapreduce cluster.    */
specifier|public
specifier|static
name|MiniCluster
name|buildCluster
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isSetup
condition|)
block|{
name|INSTANCE
operator|.
name|setupMiniDfsAndMrClusters
argument_list|()
expr_stmt|;
name|isSetup
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|INSTANCE
return|;
block|}
specifier|public
name|void
name|shutDown
parameter_list|()
block|{
name|INSTANCE
operator|.
name|shutdownMiniDfsAndMrClusters
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finalize
parameter_list|()
block|{
name|shutdownMiniDfsAndMrClusters
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|shutdownMiniDfsAndMrClusters
parameter_list|()
block|{
name|isSetup
operator|=
literal|false
expr_stmt|;
try|try
block|{
if|if
condition|(
name|m_fileSys
operator|!=
literal|null
condition|)
block|{
name|m_fileSys
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
name|m_dfs
operator|!=
literal|null
condition|)
block|{
name|m_dfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|m_mr
operator|!=
literal|null
condition|)
block|{
name|m_mr
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|m_fileSys
operator|=
literal|null
expr_stmt|;
name|m_dfs
operator|=
literal|null
expr_stmt|;
name|m_mr
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|Properties
name|getProperties
parameter_list|()
block|{
name|errorIfNotSetup
argument_list|()
expr_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
assert|assert
name|m_conf
operator|!=
literal|null
assert|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|m_conf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|properties
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|properties
return|;
block|}
specifier|public
name|void
name|setProperty
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|errorIfNotSetup
argument_list|()
expr_stmt|;
name|m_conf
operator|.
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
name|errorIfNotSetup
argument_list|()
expr_stmt|;
return|return
name|m_fileSys
return|;
block|}
comment|/**    * Throw RunTimeException if isSetup is false    */
specifier|private
name|void
name|errorIfNotSetup
parameter_list|()
block|{
if|if
condition|(
name|isSetup
condition|)
block|{
return|return;
block|}
name|String
name|msg
init|=
literal|"function called on MiniCluster that has been shutdown"
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
specifier|static
specifier|public
name|void
name|createInputFile
parameter_list|(
name|MiniCluster
name|miniCluster
parameter_list|,
name|String
name|fileName
parameter_list|,
name|String
index|[]
name|inputData
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|miniCluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|createInputFile
argument_list|(
name|fs
argument_list|,
name|fileName
argument_list|,
name|inputData
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|public
name|void
name|createInputFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|fileName
parameter_list|,
name|String
index|[]
name|inputData
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File "
operator|+
name|fileName
operator|+
literal|" already exists on the minicluster"
argument_list|)
throw|;
block|}
name|FSDataOutputStream
name|stream
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|stream
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inputData
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|pw
operator|.
name|println
argument_list|(
name|inputData
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|pw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Helper to remove a dfs file from the minicluster DFS    *    * @param miniCluster reference to the Minicluster where the file should be deleted    * @param fileName pathname of the file to be deleted    * @throws IOException    */
specifier|static
specifier|public
name|void
name|deleteFile
parameter_list|(
name|MiniCluster
name|miniCluster
parameter_list|,
name|String
name|fileName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|miniCluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|fileName
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

