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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|net
operator|.
name|URI
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
name|List
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
name|filecache
operator|.
name|DistributedCache
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
name|BlockLocation
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
name|FSDataInputStream
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
name|FileStatus
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
name|PathFilter
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
name|ProxyFileSystem
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
name|hive
operator|.
name|shims
operator|.
name|HadoopShims
operator|.
name|DirectDecompressorShim
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
name|io
operator|.
name|LongWritable
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
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
name|JobTracker
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
name|TaskLogServlet
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
name|WebHCatJTShim20S
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
name|JobStatus
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
name|TaskAttemptContext
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
name|VersionInfo
import|;
end_import

begin_comment
comment|/**  * Implemention of shims against Hadoop 0.20 with Security.  */
end_comment

begin_class
specifier|public
class|class
name|Hadoop20SShims
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
name|TaskLogServlet
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
name|getJobTrackerState
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
name|getJobTrackerState
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|TaskAttemptID
name|newTaskAttemptID
parameter_list|(
name|JobID
name|jobId
parameter_list|,
name|boolean
name|isMap
parameter_list|,
name|int
name|taskId
parameter_list|,
name|int
name|id
parameter_list|)
block|{
return|return
operator|new
name|TaskAttemptID
argument_list|(
name|jobId
operator|.
name|getJtIdentifier
argument_list|()
argument_list|,
name|jobId
operator|.
name|getId
argument_list|()
argument_list|,
name|isMap
argument_list|,
name|taskId
argument_list|,
name|id
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
name|JobContext
name|newJobContext
parameter_list|(
name|Job
name|job
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
name|JobContext
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
name|getJobLauncherRpcAddress
argument_list|(
name|conf
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
literal|"mapred.job.tracker"
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
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
name|val
argument_list|)
expr_stmt|;
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
literal|"mapred.job.tracker.http.address"
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
comment|// older versions of Hadoop don't have a Trash constructor based on the
comment|// Path or FileSystem. So need to achieve this by creating a dummy conf.
comment|// this needs to be filtered out based on version
name|Configuration
name|dupConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
operator|.
name|setDefaultUri
argument_list|(
name|dupConf
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
argument_list|)
expr_stmt|;
name|Trash
name|trash
init|=
operator|new
name|Trash
argument_list|(
name|dupConf
argument_list|)
decl_stmt|;
return|return
name|trash
operator|.
name|moveToTrash
argument_list|(
name|path
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
argument_list|()
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
argument_list|()
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
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|LongWritable
argument_list|>
name|getLongComparator
parameter_list|()
block|{
return|return
operator|new
name|Comparator
argument_list|<
name|LongWritable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|LongWritable
name|o1
parameter_list|,
name|LongWritable
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|compareTo
argument_list|(
name|o2
argument_list|)
return|;
block|}
block|}
return|;
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
annotation|@
name|Override
specifier|public
name|MiniMrShim
name|getMiniTezCluster
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot run tez on current hadoop, Version: "
operator|+
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
throw|;
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
return|return
name|mr
operator|.
name|getJobTrackerPort
argument_list|()
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
name|setJobLauncherRpcAddress
argument_list|(
name|conf
argument_list|,
literal|"localhost:"
operator|+
name|mr
operator|.
name|getJobTrackerPort
argument_list|()
argument_list|)
expr_stmt|;
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
name|HCatHadoopShims20S
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
name|HCatHadoopShims20S
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
argument_list|()
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
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|TaskAttemptID
name|taskId
parameter_list|)
block|{
return|return
operator|new
name|TaskAttemptContext
argument_list|(
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
name|TaskAttemptContext
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
name|Progressable
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
name|conf
argument_list|,
name|taskId
argument_list|,
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
name|JobContext
argument_list|(
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContext
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
name|JobContext
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
name|mapreduce
operator|.
name|JobID
operator|.
name|class
argument_list|,
name|Progressable
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
name|JobContext
operator|)
name|construct
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|,
name|jobId
argument_list|,
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
if|if
condition|(
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|""
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
try|try
block|{
comment|//In local mode, mapreduce will not call OutputCommitter.cleanupJob.
comment|//Calling it from here so that the partition publish happens.
comment|//This call needs to be removed after MAPREDUCE-1447 is fixed.
name|outputFormat
operator|.
name|getOutputCommitter
argument_list|(
name|createTaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|createTaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|commitJob
argument_list|(
name|job
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
name|IOException
argument_list|(
literal|"Failed to cleanup job"
argument_list|,
name|e
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
literal|"Failed to cleanup job"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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
if|if
condition|(
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|""
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
try|try
block|{
comment|// This call needs to be removed after MAPREDUCE-1447 is fixed.
name|outputFormat
operator|.
name|getOutputCommitter
argument_list|(
name|createTaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|abortJob
argument_list|(
name|job
argument_list|,
name|JobStatus
operator|.
name|State
operator|.
name|FAILED
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
name|IOException
argument_list|(
literal|"Failed to abort job"
argument_list|,
name|e
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
literal|"Failed to abort job"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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
return|return
name|JobTracker
operator|.
name|getAddress
argument_list|(
name|conf
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
name|DistributedCache
operator|.
name|CACHE_ARCHIVES
return|;
case|case
name|CACHE_FILES
case|:
return|return
name|DistributedCache
operator|.
name|CACHE_FILES
return|;
case|case
name|CACHE_SYMLINK
case|:
return|return
name|DistributedCache
operator|.
name|CACHE_SYMLINK
return|;
case|case
name|CLASSPATH_ARCHIVES
case|:
return|return
literal|"mapred.job.classpath.archives"
return|;
case|case
name|CLASSPATH_FILES
case|:
return|return
literal|"mapred.job.classpath.files"
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
comment|// In hadoop 1.x.x the file system URI is sufficient to determine the uri of the file
return|return
literal|"hdfs"
operator|.
name|equals
argument_list|(
name|fs
operator|.
name|getUri
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
name|WebHCatJTShim20S
argument_list|(
name|conf
argument_list|,
name|ugi
argument_list|)
return|;
comment|//this has state, so can't be cached
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FileStatus
argument_list|>
name|listLocatedStatus
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|PathFilter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
name|filter
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockLocation
index|[]
name|getLocations
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|status
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|getFileBlockLocations
argument_list|(
name|status
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|hflush
parameter_list|(
name|FSDataOutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|stream
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|createProxyFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|URI
name|uri
parameter_list|)
block|{
return|return
operator|new
name|ProxyFileSystem
argument_list|(
name|fs
argument_list|,
name|uri
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHadoopConfNames
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ret
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
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPFS"
argument_list|,
literal|"fs.default.name"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPMAPFILENAME"
argument_list|,
literal|"map.input.file"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPMAPREDINPUTDIR"
argument_list|,
literal|"mapred.input.dir"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPMAPREDINPUTDIRRECURSIVE"
argument_list|,
literal|"mapred.input.dir.recursive"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDMAXSPLITSIZE"
argument_list|,
literal|"mapred.max.split.size"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDMINSPLITSIZE"
argument_list|,
literal|"mapred.min.split.size"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDMINSPLITSIZEPERNODE"
argument_list|,
literal|"mapred.min.split.size.per.node"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDMINSPLITSIZEPERRACK"
argument_list|,
literal|"mapred.min.split.size.per.rack"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPNUMREDUCERS"
argument_list|,
literal|"mapred.reduce.tasks"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPJOBNAME"
argument_list|,
literal|"mapred.job.name"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"HADOOPSPECULATIVEEXECREDUCERS"
argument_list|,
literal|"mapred.reduce.tasks.speculative.execution"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDSETUPCLEANUPNEEDED"
argument_list|,
literal|"mapred.committer.job.setup.cleanup.needed"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|put
argument_list|(
literal|"MAPREDTASKCLEANUPNEEDED"
argument_list|,
literal|"mapreduce.job.committer.task.cleanup.needed"
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZeroCopyReaderShim
name|getZeroCopyReader
parameter_list|(
name|FSDataInputStream
name|in
parameter_list|,
name|ByteBufferPoolShim
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* not supported */
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|DirectDecompressorShim
name|getDirectDecompressor
parameter_list|(
name|DirectCompressionType
name|codec
parameter_list|)
block|{
comment|/* not supported */
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
name|context
parameter_list|)
block|{
return|return
name|context
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
block|}
end_class

end_unit

