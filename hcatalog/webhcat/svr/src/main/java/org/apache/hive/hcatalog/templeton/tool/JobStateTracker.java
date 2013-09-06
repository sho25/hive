begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|ZooKeeper
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_class
specifier|public
class|class
name|JobStateTracker
block|{
comment|// The path to the tracking root
specifier|private
name|String
name|job_trackingroot
init|=
literal|null
decl_stmt|;
comment|// The zookeeper connection to use
specifier|private
name|ZooKeeper
name|zk
decl_stmt|;
comment|// The id of the tracking node -- must be a SEQUENTIAL node
specifier|private
name|String
name|trackingnode
decl_stmt|;
comment|// The id of the job this tracking node represents
specifier|private
name|String
name|jobid
decl_stmt|;
comment|// The logger
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|JobStateTracker
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Constructor for a new node -- takes the jobid of an existing job    *    */
specifier|public
name|JobStateTracker
parameter_list|(
name|String
name|node
parameter_list|,
name|ZooKeeper
name|zk
parameter_list|,
name|boolean
name|nodeIsTracker
parameter_list|,
name|String
name|job_trackingpath
parameter_list|)
block|{
name|this
operator|.
name|zk
operator|=
name|zk
expr_stmt|;
if|if
condition|(
name|nodeIsTracker
condition|)
block|{
name|trackingnode
operator|=
name|node
expr_stmt|;
block|}
else|else
block|{
name|jobid
operator|=
name|node
expr_stmt|;
block|}
name|job_trackingroot
operator|=
name|job_trackingpath
expr_stmt|;
block|}
comment|/**    * Create the parent znode for this job state.    */
specifier|public
name|void
name|create
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|paths
init|=
name|ZooKeeperStorage
operator|.
name|getPaths
argument_list|(
name|job_trackingroot
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|znode
range|:
name|paths
control|)
block|{
try|try
block|{
name|zk
operator|.
name|create
argument_list|(
name|znode
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
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
block|{       }
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to create parent nodes"
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|trackingnode
operator|=
name|zk
operator|.
name|create
argument_list|(
name|makeTrackingZnode
argument_list|()
argument_list|,
name|jobid
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT_SEQUENTIAL
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
name|IOException
argument_list|(
literal|"Unable to create "
operator|+
name|makeTrackingZnode
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|zk
operator|.
name|delete
argument_list|(
name|makeTrackingJobZnode
argument_list|(
name|trackingnode
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Might have been deleted already
name|LOG
operator|.
name|info
argument_list|(
literal|"Couldn't delete "
operator|+
name|makeTrackingJobZnode
argument_list|(
name|trackingnode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the jobid for this tracking node    * @throws IOException    */
specifier|public
name|String
name|getJobID
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
operator|new
name|String
argument_list|(
name|zk
operator|.
name|getData
argument_list|(
name|makeTrackingJobZnode
argument_list|(
name|trackingnode
argument_list|)
argument_list|,
literal|false
argument_list|,
operator|new
name|Stat
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// It was deleted during the transaction
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Node already deleted "
operator|+
name|trackingnode
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't read node "
operator|+
name|trackingnode
argument_list|)
throw|;
block|}
block|}
comment|/**    * Make a ZK path to a new tracking node    */
specifier|public
name|String
name|makeTrackingZnode
parameter_list|()
block|{
return|return
name|job_trackingroot
operator|+
literal|"/"
return|;
block|}
comment|/**    * Make a ZK path to an existing tracking node    */
specifier|public
name|String
name|makeTrackingJobZnode
parameter_list|(
name|String
name|nodename
parameter_list|)
block|{
return|return
name|job_trackingroot
operator|+
literal|"/"
operator|+
name|nodename
return|;
block|}
comment|/*    * Get the list of tracking jobs.  These can be used to determine which jobs have    * expired.    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getTrackingJobs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeper
name|zk
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|jobs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|String
name|myid
range|:
name|zk
operator|.
name|getChildren
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_ROOT
argument_list|)
operator|+
name|ZooKeeperStorage
operator|.
name|TRACKINGDIR
argument_list|,
literal|false
argument_list|)
control|)
block|{
name|jobs
operator|.
name|add
argument_list|(
name|myid
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
name|IOException
argument_list|(
literal|"Can't get tracking children"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|jobs
return|;
block|}
block|}
end_class

end_unit

