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
name|templeton
operator|.
name|tool
package|;
end_package

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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|ExponentialBackoffRetry
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
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Ids
import|;
end_import

begin_comment
comment|/**  * A storage implementation based on storing everything in ZooKeeper.  * This keeps everything in a central location that is guaranteed  * to be available and accessible.  *  * Data is stored with each key/value pair being a node in ZooKeeper.  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperStorage
implements|implements
name|TempletonStorage
block|{
specifier|public
specifier|static
specifier|final
name|String
name|TRACKINGDIR
init|=
literal|"/created"
decl_stmt|;
comment|// Locations for each of the storage types
specifier|public
name|String
name|storage_root
init|=
literal|null
decl_stmt|;
specifier|public
name|String
name|job_path
init|=
literal|null
decl_stmt|;
specifier|public
name|String
name|job_trackingpath
init|=
literal|null
decl_stmt|;
specifier|public
name|String
name|overhead_path
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZK_HOSTS
init|=
literal|"templeton.zookeeper.hosts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZK_SESSION_TIMEOUT
init|=
literal|"templeton.zookeeper.session-timeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ENCODING
init|=
literal|"UTF-8"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ZooKeeperStorage
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CuratorFramework
name|zk
decl_stmt|;
comment|/**    * Open a ZooKeeper connection for the JobState.    */
specifier|public
specifier|static
name|CuratorFramework
name|zkOpen
parameter_list|(
name|String
name|zkHosts
parameter_list|,
name|int
name|zkSessionTimeoutMs
parameter_list|)
throws|throws
name|IOException
block|{
comment|//do we need to add a connection status listener?  What will that do?
name|ExponentialBackoffRetry
name|retryPolicy
init|=
operator|new
name|ExponentialBackoffRetry
argument_list|(
literal|1000
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|CuratorFramework
name|zk
init|=
name|CuratorFrameworkFactory
operator|.
name|newClient
argument_list|(
name|zkHosts
argument_list|,
name|zkSessionTimeoutMs
argument_list|,
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|getConnectionTimeoutMs
argument_list|()
argument_list|,
name|retryPolicy
argument_list|)
decl_stmt|;
name|zk
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|zk
return|;
block|}
comment|/**    * Open a ZooKeeper connection for the JobState.    */
specifier|public
specifier|static
name|CuratorFramework
name|zkOpen
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|/*the silly looking call to Builder below is to get the default value of session timeout     from Curator which itself exposes it as system property*/
return|return
name|zkOpen
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ZK_HOSTS
argument_list|)
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|ZK_SESSION_TIMEOUT
argument_list|,
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|getSessionTimeoutMs
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|ZooKeeperStorage
parameter_list|()
block|{
comment|// No-op -- this is needed to be able to instantiate the
comment|// class from the name.
block|}
comment|/**    * Close this ZK connection.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|zk
operator|!=
literal|null
condition|)
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|startCleanup
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
try|try
block|{
name|ZooKeeperCleanup
operator|.
name|startInstance
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cleanup instance didn't start."
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Create a node in ZooKeeper    */
specifier|public
name|void
name|create
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|wasCreated
init|=
literal|false
decl_stmt|;
try|try
block|{
name|zk
operator|.
name|create
argument_list|()
operator|.
name|creatingParentsIfNeeded
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
operator|.
name|withACL
argument_list|(
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|)
operator|.
name|forPath
argument_list|(
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|wasCreated
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|ex
parameter_list|)
block|{
comment|//we just created top level node for this jobId
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error creating "
operator|+
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|,
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|wasCreated
condition|)
block|{
try|try
block|{
comment|// Really not sure if this should go here.  Will have
comment|// to see how the storage mechanism evolves.
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|Type
operator|.
name|JOB
argument_list|)
condition|)
block|{
name|JobStateTracker
name|jt
init|=
operator|new
name|JobStateTracker
argument_list|(
name|id
argument_list|,
name|zk
argument_list|,
literal|false
argument_list|,
name|job_trackingpath
argument_list|)
decl_stmt|;
name|jt
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error tracking (jobId="
operator|+
name|id
operator|+
literal|"): "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// If we couldn't create the tracker node, don't create the main node.
try|try
block|{
name|zk
operator|.
name|delete
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
expr_stmt|;
comment|//default version is -1
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|//EK: it's not obvious that this is the right logic, if we don't record the 'callback'
comment|//for example and never notify the client of job completion
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to delete "
operator|+
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
operator|+
literal|":"
operator|+
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
try|try
block|{
if|if
condition|(
name|zk
operator|.
name|checkExists
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to create "
operator|+
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|wasCreated
condition|)
block|{
try|try
block|{
name|saveField
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
literal|"created"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotFoundException
name|nfe
parameter_list|)
block|{
comment|// Wow, something's really wrong.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't write to node "
operator|+
name|id
argument_list|,
name|nfe
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Get the path based on the job type.    *    * @param type    */
specifier|public
name|String
name|getPath
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|String
name|typepath
init|=
name|overhead_path
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|JOB
case|:
name|typepath
operator|=
name|job_path
expr_stmt|;
break|break;
case|case
name|JOBTRACKING
case|:
name|typepath
operator|=
name|job_trackingpath
expr_stmt|;
break|break;
block|}
return|return
name|typepath
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|getPaths
parameter_list|(
name|String
name|fullpath
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|fullpath
operator|.
name|length
argument_list|()
operator|<
literal|2
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|fullpath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|location
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|location
operator|=
name|fullpath
operator|.
name|indexOf
argument_list|(
literal|"/"
argument_list|,
name|location
operator|+
literal|1
argument_list|)
operator|)
operator|>
literal|0
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|fullpath
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|location
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|paths
operator|.
name|add
argument_list|(
name|fullpath
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|strings
init|=
operator|new
name|String
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
return|return
name|paths
operator|.
name|toArray
argument_list|(
name|strings
argument_list|)
return|;
block|}
comment|/**    * A helper method that sets a field value.    * @throws java.lang.Exception    */
specifier|private
name|void
name|setFieldData
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|val
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|zk
operator|.
name|create
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
operator|.
name|withACL
argument_list|(
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|)
operator|.
name|forPath
argument_list|(
name|makeFieldZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|)
argument_list|,
name|val
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|e
parameter_list|)
block|{
name|zk
operator|.
name|setData
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeFieldZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|)
argument_list|,
name|val
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Make a ZK path to the named field.    */
specifier|public
name|String
name|makeFieldZnode
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
operator|+
literal|"/"
operator|+
name|name
return|;
block|}
comment|/**    * Make a ZK path to job    */
specifier|public
name|String
name|makeZnode
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|getPath
argument_list|(
name|type
argument_list|)
operator|+
literal|"/"
operator|+
name|id
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|saveField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|val
parameter_list|)
throws|throws
name|NotFoundException
block|{
try|try
block|{
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|create
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|setFieldData
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
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
name|NotFoundException
argument_list|(
literal|"Writing "
operator|+
name|key
operator|+
literal|": "
operator|+
name|val
operator|+
literal|", "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|)
block|{
try|try
block|{
name|byte
index|[]
name|b
init|=
name|zk
operator|.
name|getData
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeFieldZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|key
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|String
argument_list|(
name|b
argument_list|,
name|ENCODING
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|NotFoundException
block|{
try|try
block|{
for|for
control|(
name|String
name|child
range|:
name|zk
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
control|)
block|{
try|try
block|{
name|zk
operator|.
name|delete
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeFieldZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|child
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
comment|// Other nodes may be trying to delete this at the same time,
comment|// so just log errors and skip them.
throw|throw
operator|new
name|NotFoundException
argument_list|(
literal|"Couldn't delete "
operator|+
name|makeFieldZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|child
argument_list|)
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|zk
operator|.
name|delete
argument_list|()
operator|.
name|forPath
argument_list|(
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
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
comment|// Same thing -- might be deleted by other nodes, so just go on.
throw|throw
operator|new
name|NotFoundException
argument_list|(
literal|"Couldn't delete "
operator|+
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Error getting children of node -- probably node has been deleted
throw|throw
operator|new
name|NotFoundException
argument_list|(
literal|"Couldn't get children of "
operator|+
name|makeZnode
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllForType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
try|try
block|{
return|return
name|zk
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|openStorage
parameter_list|(
name|Configuration
name|config
parameter_list|)
throws|throws
name|IOException
block|{
name|storage_root
operator|=
name|config
operator|.
name|get
argument_list|(
name|STORAGE_ROOT
argument_list|)
expr_stmt|;
name|job_path
operator|=
name|storage_root
operator|+
literal|"/jobs"
expr_stmt|;
name|job_trackingpath
operator|=
name|storage_root
operator|+
name|TRACKINGDIR
expr_stmt|;
name|overhead_path
operator|=
name|storage_root
operator|+
literal|"/overhead"
expr_stmt|;
if|if
condition|(
name|zk
operator|==
literal|null
condition|)
block|{
name|zk
operator|=
name|zkOpen
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeStorage
parameter_list|()
throws|throws
name|IOException
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

