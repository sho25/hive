begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|shims
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
name|lang
operator|.
name|Integer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
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
name|Map
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
name|lang
operator|.
name|StringUtils
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
name|fs
operator|.
name|Trash
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
name|MiniMRCluster
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
name|ClusterStatus
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
name|Reporter
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
name|WebHCatJTShim23
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|JobID
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
name|mapreduce
operator|.
name|MRJobConfig
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
name|mapreduce
operator|.
name|OutputFormat
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
name|mapreduce
operator|.
name|TaskAttemptID
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
name|mapreduce
operator|.
name|TaskID
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
name|mapreduce
operator|.
name|TaskType
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
name|mapreduce
operator|.
name|task
operator|.
name|JobContextImpl
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
name|mapreduce
operator|.
name|task
operator|.
name|TaskAttemptContextImpl
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
name|mapreduce
operator|.
name|util
operator|.
name|HostUtil
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
name|net
operator|.
name|NetUtils
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
name|util
operator|.
name|Progressable
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
name|lib
operator|.
name|TotalOrderPartitioner
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Implemention of shims against Hadoop 0.23.0.  */
end_comment

begin_class
specifier|public
class|class
name|Hadoop23Shims
extends|extends
name|HadoopShimsSecure
block|{
annotation|@
name|Override
specifier|public
name|String
name|getTaskAttemptLogUrl
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|String
name|taskTrackerHttpAddress
parameter_list|,
name|String
name|taskAttemptId
parameter_list|)
throws|throws
name|MalformedURLException
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
literal|"mapreduce.framework.name"
argument_list|)
operator|!=
literal|null
operator|&&
name|conf
operator|.
name|get
argument_list|(
literal|"mapreduce.framework.name"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"yarn"
argument_list|)
condition|)
block|{
comment|// if the cluster is running in MR2 mode, return null
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't fetch tasklog: TaskLogServlet is not supported in MR2 mode."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
else|else
block|{
comment|// if the cluster is running in MR1 mode, using HostUtil to construct TaskLogURL
name|URL
name|taskTrackerHttpURL
init|=
operator|new
name|URL
argument_list|(
name|taskTrackerHttpAddress
argument_list|)
decl_stmt|;
return|return
name|HostUtil
operator|.
name|getTaskLogUrl
argument_list|(
name|taskTrackerHttpURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|taskTrackerHttpURL
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|,
name|taskAttemptId
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JobTrackerState
name|getJobTrackerState
parameter_list|(
name|ClusterStatus
name|clusterStatus
parameter_list|)
throws|throws
name|Exception
block|{
name|JobTrackerState
name|state
decl_stmt|;
switch|switch
condition|(
name|clusterStatus
operator|.
name|getJobTrackerStatus
argument_list|()
condition|)
block|{
case|case
name|INITIALIZING
case|:
return|return
name|JobTrackerState
operator|.
name|INITIALIZING
return|;
case|case
name|RUNNING
case|:
return|return
name|JobTrackerState
operator|.
name|RUNNING
return|;
default|default:
name|String
name|errorMsg
init|=
literal|"Unrecognized JobTracker state: "
operator|+
name|clusterStatus
operator|.
name|getJobTrackerStatus
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
name|newTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Progressable
name|progressable
parameter_list|)
block|{
return|return
operator|new
name|TaskAttemptContextImpl
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|progress
parameter_list|()
block|{
name|progressable
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
name|newJobContext
parameter_list|(
name|Job
name|job
parameter_list|)
block|{
return|return
operator|new
name|JobContextImpl
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|job
operator|.
name|getJobID
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isLocalMode
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
literal|"local"
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"mapreduce.framework.name"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getJobLauncherRpcAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
literal|"yarn.resourcemanager.address"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setJobLauncherRpcAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|val
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
comment|// LocalClientProtocolProvider expects both parameters to be 'local'.
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.framework.name"
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.jobtracker.address"
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.framework.name"
argument_list|,
literal|"yarn"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.resourcemanager.address"
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getJobLauncherHttpAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
literal|"yarn.resourcemanager.webapp.address"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDefaultBlockSize
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
block|{
return|return
name|fs
operator|.
name|getDefaultBlockSize
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getDefaultReplication
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
block|{
return|return
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|moveToAppropriateTrash
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Trash
operator|.
name|moveToAppropriateTrash
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTotalOrderPartitionFile
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|partitionFile
parameter_list|)
block|{
name|TotalOrderPartitioner
operator|.
name|setPartitionFile
argument_list|(
name|jobConf
argument_list|,
name|partitionFile
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns a shim to wrap MiniMrCluster    */
specifier|public
name|MiniMrShim
name|getMiniMrCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numberOfTaskTrackers
parameter_list|,
name|String
name|nameNode
parameter_list|,
name|int
name|numDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MiniMrShim
argument_list|(
name|conf
argument_list|,
name|numberOfTaskTrackers
argument_list|,
name|nameNode
argument_list|,
name|numDir
argument_list|)
return|;
block|}
comment|/**    * Shim for MiniMrCluster    */
specifier|public
class|class
name|MiniMrShim
implements|implements
name|HadoopShims
operator|.
name|MiniMrShim
block|{
specifier|private
specifier|final
name|MiniMRCluster
name|mr
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|MiniMrShim
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numberOfTaskTrackers
parameter_list|,
name|String
name|nameNode
parameter_list|,
name|int
name|numDir
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|JobConf
name|jConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jConf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.queues"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|jConf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.default.capacity"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
name|mr
operator|=
operator|new
name|MiniMRCluster
argument_list|(
name|numberOfTaskTrackers
argument_list|,
name|nameNode
argument_list|,
name|numDir
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|jConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getJobTrackerPort
parameter_list|()
throws|throws
name|UnsupportedOperationException
block|{
name|String
name|address
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"yarn.resourcemanager.address"
argument_list|)
decl_stmt|;
name|address
operator|=
name|StringUtils
operator|.
name|substringAfterLast
argument_list|(
name|address
argument_list|,
literal|":"
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|address
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid YARN resource manager port."
argument_list|)
throw|;
block|}
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|address
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|mr
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|JobConf
name|jConf
init|=
name|mr
operator|.
name|createJobConf
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
range|:
name|jConf
control|)
block|{
comment|//System.out.println("XXX Var: "+pair.getKey() +"="+pair.getValue());
comment|//if (conf.get(pair.getKey()) == null) {
name|conf
operator|.
name|set
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|//}
block|}
block|}
block|}
comment|// Don't move this code to the parent class. There's a binary
comment|// incompatibility between hadoop 1 and 2 wrt MiniDFSCluster and we
comment|// need to have two different shim classes even though they are
comment|// exactly the same.
specifier|public
name|HadoopShims
operator|.
name|MiniDFSShim
name|getMiniDfs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numDataNodes
parameter_list|,
name|boolean
name|format
parameter_list|,
name|String
index|[]
name|racks
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MiniDFSShim
argument_list|(
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
name|numDataNodes
argument_list|,
name|format
argument_list|,
name|racks
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * MiniDFSShim.    *    */
specifier|public
class|class
name|MiniDFSShim
implements|implements
name|HadoopShims
operator|.
name|MiniDFSShim
block|{
specifier|private
specifier|final
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|public
name|MiniDFSShim
parameter_list|(
name|MiniDFSCluster
name|cluster
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|cluster
operator|.
name|getFileSystem
argument_list|()
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|volatile
name|HCatHadoopShims
name|hcatShimInstance
decl_stmt|;
annotation|@
name|Override
specifier|public
name|HCatHadoopShims
name|getHCatShim
parameter_list|()
block|{
if|if
condition|(
name|hcatShimInstance
operator|==
literal|null
condition|)
block|{
name|hcatShimInstance
operator|=
operator|new
name|HCatHadoopShims23
argument_list|()
expr_stmt|;
block|}
return|return
name|hcatShimInstance
return|;
block|}
specifier|private
specifier|final
class|class
name|HCatHadoopShims23
implements|implements
name|HCatHadoopShims
block|{
annotation|@
name|Override
specifier|public
name|TaskID
name|createTaskID
parameter_list|()
block|{
return|return
operator|new
name|TaskID
argument_list|(
literal|""
argument_list|,
literal|0
argument_list|,
name|TaskType
operator|.
name|MAP
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TaskAttemptID
name|createTaskAttemptID
parameter_list|()
block|{
return|return
operator|new
name|TaskAttemptID
argument_list|(
literal|""
argument_list|,
literal|0
argument_list|,
name|TaskType
operator|.
name|MAP
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptID
name|taskId
parameter_list|)
block|{
return|return
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|task
operator|.
name|TaskAttemptContextImpl
argument_list|(
name|conf
operator|instanceof
name|JobConf
condition|?
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
else|:
name|conf
argument_list|,
name|taskId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
name|conf
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptID
name|taskId
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
name|newContext
init|=
literal|null
decl_stmt|;
try|try
block|{
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
name|construct
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContextImpl
operator|.
name|class
operator|.
name|getDeclaredConstructor
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
operator|.
name|class
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptID
operator|.
name|class
argument_list|,
name|Reporter
operator|.
name|class
argument_list|)
decl_stmt|;
name|construct
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|newContext
operator|=
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
operator|)
name|construct
operator|.
name|newInstance
argument_list|(
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
argument_list|,
name|taskId
argument_list|,
operator|(
name|Reporter
operator|)
name|progressable
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|newContext
return|;
block|}
annotation|@
name|Override
specifier|public
name|JobContext
name|createJobContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|JobID
name|jobId
parameter_list|)
block|{
return|return
operator|new
name|JobContextImpl
argument_list|(
name|conf
operator|instanceof
name|JobConf
condition|?
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
else|:
name|conf
argument_list|,
name|jobId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContext
name|createJobContext
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
name|conf
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobID
name|jobId
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
block|{
return|return
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContextImpl
argument_list|(
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
argument_list|,
name|jobId
argument_list|,
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|Reporter
operator|)
name|progressable
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitJob
parameter_list|(
name|OutputFormat
name|outputFormat
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Do nothing as this was fixed by MAPREDUCE-1447.
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortJob
parameter_list|(
name|OutputFormat
name|outputFormat
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Do nothing as this was fixed by MAPREDUCE-1447.
block|}
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|getResourceManagerAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|addr
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"yarn.resourcemanager.address"
argument_list|,
literal|"localhost:8032"
argument_list|)
decl_stmt|;
return|return
name|NetUtils
operator|.
name|createSocketAddr
argument_list|(
name|addr
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPropertyName
parameter_list|(
name|PropertyName
name|name
parameter_list|)
block|{
switch|switch
condition|(
name|name
condition|)
block|{
case|case
name|CACHE_ARCHIVES
case|:
return|return
name|MRJobConfig
operator|.
name|CACHE_ARCHIVES
return|;
case|case
name|CACHE_FILES
case|:
return|return
name|MRJobConfig
operator|.
name|CACHE_FILES
return|;
case|case
name|CACHE_SYMLINK
case|:
return|return
name|MRJobConfig
operator|.
name|CACHE_SYMLINK
return|;
block|}
return|return
literal|""
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFileInHDFS
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// In case of viewfs we need to lookup where the actual file is to know the filesystem in use.
comment|// resolvePath is a sure shot way of knowing which file system the file is.
return|return
literal|"hdfs"
operator|.
name|equals
argument_list|(
name|fs
operator|.
name|resolvePath
argument_list|(
name|path
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|WebHCatJTShim
name|getWebHCatShim
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|WebHCatJTShim23
argument_list|(
name|conf
argument_list|,
name|ugi
argument_list|)
return|;
comment|//this has state, so can't be cached
block|}
block|}
end_class

end_unit

