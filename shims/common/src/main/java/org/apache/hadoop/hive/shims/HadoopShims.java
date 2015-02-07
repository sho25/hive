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
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessControlException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|permission
operator|.
name|FsAction
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
name|permission
operator|.
name|FsPermission
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
name|StoragePolicyValue
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
name|JobProfile
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
name|mapred
operator|.
name|RecordReader
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
name|lib
operator|.
name|CombineFileSplit
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
name|Progressable
import|;
end_import

begin_comment
comment|/**  * In order to be compatible with multiple versions of Hadoop, all parts  * of the Hadoop interface that are not cross-version compatible are  * encapsulated in an implementation of this class. Users should use  * the ShimLoader class as a factory to obtain an implementation of  * HadoopShims corresponding to the version of Hadoop currently on the  * classpath.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HadoopShims
block|{
comment|/**    * Constructs and Returns TaskAttempt Log Url    * or null if the TaskLogServlet is not available    *    *  @return TaskAttempt Log Url    */
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
function_decl|;
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
function_decl|;
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
parameter_list|,
name|boolean
name|local
parameter_list|,
name|String
name|tezDir
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|MiniMrShim
name|getMiniSparkCluster
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
function_decl|;
comment|/**    * Shim for MiniMrCluster    */
specifier|public
interface|interface
name|MiniMrShim
block|{
specifier|public
name|int
name|getJobTrackerPort
parameter_list|()
throws|throws
name|UnsupportedOperationException
function_decl|;
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|setupConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
block|}
comment|/**    * Returns a shim to wrap MiniDFSCluster. This is necessary since this class    * was moved from org.apache.hadoop.dfs to org.apache.hadoop.hdfs    */
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
function_decl|;
comment|/**    * Shim around the functions in MiniDFSCluster that Hive uses.    */
specifier|public
interface|interface
name|MiniDFSShim
block|{
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
name|CombineFileInputFormatShim
name|getCombineFileInputFormat
parameter_list|()
function_decl|;
enum|enum
name|JobTrackerState
block|{
name|INITIALIZING
block|,
name|RUNNING
block|}
empty_stmt|;
comment|/**    * Convert the ClusterStatus to its Thrift equivalent: JobTrackerState.    * See MAPREDUCE-2455 for why this is a part of the shim.    * @param clusterStatus    * @return the matching JobTrackerState    * @throws Exception if no equivalent JobTrackerState exists    */
specifier|public
name|JobTrackerState
name|getJobTrackerState
parameter_list|(
name|ClusterStatus
name|clusterStatus
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|public
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
function_decl|;
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
function_decl|;
specifier|public
name|JobContext
name|newJobContext
parameter_list|(
name|Job
name|job
parameter_list|)
function_decl|;
comment|/**    * Check wether MR is configured to run in local-mode    * @param conf    * @return    */
specifier|public
name|boolean
name|isLocalMode
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * All retrieval of jobtracker/resource manager rpc address    * in the configuration should be done through this shim    * @param conf    * @return    */
specifier|public
name|String
name|getJobLauncherRpcAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * All updates to jobtracker/resource manager rpc address    * in the configuration should be done through this shim    * @param conf    * @return    */
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
function_decl|;
comment|/**    * All references to jobtracker/resource manager http address    * in the configuration should be done through this shim    * @param conf    * @return    */
specifier|public
name|String
name|getJobLauncherHttpAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * Move the directory/file to trash. In case of the symlinks or mount points, the file is    * moved to the trashbin in the actual volume of the path p being deleted    * @param fs    * @param path    * @param conf    * @return false if the item is already in the trash or trash is disabled    * @throws IOException    */
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
function_decl|;
comment|/**    * Get the default block size for the path. FileSystem alone is not sufficient to    * determine the same, as in case of CSMT the underlying file system determines that.    * @param fs    * @param path    * @return    */
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
function_decl|;
comment|/**    * Get the default replication for a path. In case of CSMT the given path will be used to    * locate the actual filesystem.    * @param fs    * @param path    * @return    */
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
function_decl|;
comment|/**    * Reset the default fair scheduler queue mapping to end user.    *    * @param conf    * @param userName end user name    */
specifier|public
name|void
name|refreshDefaultQueue
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The method sets to set the partition file has a different signature between    * hadoop versions.    * @param jobConf    * @param partition    */
name|void
name|setTotalOrderPartitionFile
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|partition
parameter_list|)
function_decl|;
name|Comparator
argument_list|<
name|LongWritable
argument_list|>
name|getLongComparator
parameter_list|()
function_decl|;
comment|/**    * CombineFileInputFormatShim.    *    * @param<K>    * @param<V>    */
interface|interface
name|CombineFileInputFormatShim
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
name|Path
index|[]
name|getInputPathsShim
parameter_list|(
name|JobConf
name|conf
parameter_list|)
function_decl|;
name|void
name|createPool
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|PathFilter
modifier|...
name|filters
parameter_list|)
function_decl|;
name|CombineFileSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|CombineFileSplit
name|getInputSplitShim
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|RecordReader
name|getRecordReader
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|CombineFileSplit
name|split
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|Class
argument_list|<
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|rrClass
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Get the block locations for the given directory.    * @param fs the file system    * @param path the directory name to get the status and block locations    * @param filter a filter that needs to accept the file (or null)    * @return an list for the located file status objects    * @throws IOException    */
name|List
argument_list|<
name|FileStatus
argument_list|>
name|listLocatedStatus
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|PathFilter
name|filter
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For file status returned by listLocatedStatus, convert them into a list    * of block locations.    * @param fs the file system    * @param status the file information    * @return the block locations of the file    * @throws IOException    */
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
function_decl|;
comment|/**    * For the block locations returned by getLocations() convert them into a Treemap    *<Offset,blockLocation> by iterating over the list of blockLocation.    * Using TreeMap from offset to blockLocation, makes it O(logn) to get a particular    * block based upon offset.    * @param fs the file system    * @param status the file information    * @return TreeMap<Long, BlockLocation>    * @throws IOException    */
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|BlockLocation
argument_list|>
name|getLocationsWithOffset
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|status
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Flush and make visible to other users the changes to the given stream.    * @param stream the stream to hflush.    * @throws IOException    */
specifier|public
name|void
name|hflush
parameter_list|(
name|FSDataOutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For a given file, return a file status    * @param conf    * @param fs    * @param file    * @return    * @throws IOException    */
specifier|public
name|HdfsFileStatus
name|getFullFileStatus
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For a given file, set a given file status.    * @param conf    * @param sourceStatus    * @param fs    * @param target    * @throws IOException    */
specifier|public
name|void
name|setFullFileStatus
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HdfsFileStatus
name|sourceStatus
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|target
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Includes the vanilla FileStatus, and AclStatus if it applies to this version of hadoop.    */
specifier|public
interface|interface
name|HdfsFileStatus
block|{
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|()
function_decl|;
specifier|public
name|void
name|debugLog
parameter_list|()
function_decl|;
block|}
specifier|public
name|HCatHadoopShims
name|getHCatShim
parameter_list|()
function_decl|;
specifier|public
interface|interface
name|HCatHadoopShims
block|{
enum|enum
name|PropertyName
block|{
name|CACHE_ARCHIVES
block|,
name|CACHE_FILES
block|,
name|CACHE_SYMLINK
block|,
name|CLASSPATH_ARCHIVES
block|,
name|CLASSPATH_FILES
block|}
specifier|public
name|TaskID
name|createTaskID
parameter_list|()
function_decl|;
specifier|public
name|TaskAttemptID
name|createTaskAttemptID
parameter_list|()
function_decl|;
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
name|TaskAttemptID
name|taskId
parameter_list|)
function_decl|;
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
function_decl|;
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
function_decl|;
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
name|JobConf
name|conf
parameter_list|,
name|JobID
name|jobId
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
function_decl|;
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
function_decl|;
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
function_decl|;
comment|/* Referring to job tracker in 0.20 and resource manager in 0.23 */
specifier|public
name|InetSocketAddress
name|getResourceManagerAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
specifier|public
name|String
name|getPropertyName
parameter_list|(
name|PropertyName
name|name
parameter_list|)
function_decl|;
comment|/**      * Checks if file is in HDFS filesystem.      *      * @param fs      * @param path      * @return true if the file is in HDFS, false if the file is in other file systems.      */
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
function_decl|;
block|}
comment|/**    * Provides a Hadoop JobTracker shim.    * @param conf not {@code null}    */
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
function_decl|;
specifier|public
interface|interface
name|WebHCatJTShim
block|{
comment|/**      * Grab a handle to a job that is already known to the JobTracker.      *      * @return Profile of the job, or null if not found.      */
specifier|public
name|JobProfile
name|getJobProfile
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Grab a handle to a job that is already known to the JobTracker.      *      * @return Status of the job, or null if not found.      */
specifier|public
name|JobStatus
name|getJobStatus
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Kill a job.      */
specifier|public
name|void
name|killJob
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Get all the jobs submitted.      */
specifier|public
name|JobStatus
index|[]
name|getAllJobs
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Close the connection to the Job Tracker.      */
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
comment|/**      * Does exactly what org.apache.hadoop.mapreduce.Job#addCacheFile(URI) in Hadoop 2.      * Assumes that both parameters are not {@code null}.      */
specifier|public
name|void
name|addCacheFile
parameter_list|(
name|URI
name|uri
parameter_list|,
name|Job
name|job
parameter_list|)
function_decl|;
comment|/**      * Kills all jobs tagged with the given tag that have been started after the      * given timestamp.      */
specifier|public
name|void
name|killJobs
parameter_list|(
name|String
name|tag
parameter_list|,
name|long
name|timestamp
parameter_list|)
function_decl|;
block|}
comment|/**    * Create a proxy file system that can serve a given scheme/authority using some    * other file system.    */
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
function_decl|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHadoopConfNames
parameter_list|()
function_decl|;
comment|/**    * Create a shim for DFS storage policy.    */
specifier|public
enum|enum
name|StoragePolicyValue
block|{
name|MEMORY
block|,
comment|/* 1-replica memory */
name|SSD
block|,
comment|/* 3-replica ssd */
name|DEFAULT
comment|/* system defaults (usually 3-replica disk) */
block|;
specifier|public
specifier|static
name|StoragePolicyValue
name|lookup
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
return|return
name|DEFAULT
return|;
block|}
return|return
name|StoragePolicyValue
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|toUpperCase
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|)
return|;
block|}
block|}
empty_stmt|;
specifier|public
interface|interface
name|StoragePolicyShim
block|{
name|void
name|setStoragePolicy
parameter_list|(
name|Path
name|path
parameter_list|,
name|StoragePolicyValue
name|policy
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    *  obtain a storage policy shim associated with the filesystem.    *  Returns null when the filesystem has no storage policies.    */
specifier|public
name|StoragePolicyShim
name|getStoragePolicyShim
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
function_decl|;
comment|/**    * a hadoop.io ByteBufferPool shim.    */
specifier|public
interface|interface
name|ByteBufferPoolShim
block|{
comment|/**      * Get a new ByteBuffer from the pool.  The pool can provide this from      * removing a buffer from its internal cache, or by allocating a      * new buffer.      *      * @param direct     Whether the buffer should be direct.      * @param length     The minimum length the buffer will have.      * @return           A new ByteBuffer. Its capacity can be less      *                   than what was requested, but must be at      *                   least 1 byte.      */
name|ByteBuffer
name|getBuffer
parameter_list|(
name|boolean
name|direct
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**      * Release a buffer back to the pool.      * The pool may choose to put this buffer into its cache/free it.      *      * @param buffer    a direct bytebuffer      */
name|void
name|putBuffer
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
function_decl|;
block|}
comment|/**    * Provides an HDFS ZeroCopyReader shim.    * @param in FSDataInputStream to read from (where the cached/mmap buffers are tied to)    * @param in ByteBufferPoolShim to allocate fallback buffers with    *    * @return returns null if not supported    */
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
function_decl|;
specifier|public
interface|interface
name|ZeroCopyReaderShim
block|{
comment|/**      * Get a ByteBuffer from the FSDataInputStream - this can be either a HeapByteBuffer or an MappedByteBuffer.      * Also move the in stream by that amount. The data read can be small than maxLength.      *      * @return ByteBuffer read from the stream,      */
specifier|public
name|ByteBuffer
name|readBuffer
parameter_list|(
name|int
name|maxLength
parameter_list|,
name|boolean
name|verifyChecksums
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Release a ByteBuffer obtained from a read on the      * Also move the in stream by that amount. The data read can be small than maxLength.      *      */
specifier|public
name|void
name|releaseBuffer
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
function_decl|;
block|}
specifier|public
enum|enum
name|DirectCompressionType
block|{
name|NONE
block|,
name|ZLIB_NOHEADER
block|,
name|ZLIB
block|,
name|SNAPPY
block|,   }
empty_stmt|;
specifier|public
interface|interface
name|DirectDecompressorShim
block|{
specifier|public
name|void
name|decompress
parameter_list|(
name|ByteBuffer
name|src
parameter_list|,
name|ByteBuffer
name|dst
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
name|DirectDecompressorShim
name|getDirectDecompressor
parameter_list|(
name|DirectCompressionType
name|codec
parameter_list|)
function_decl|;
comment|/**    * Get configuration from JobContext    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|(
name|JobContext
name|context
parameter_list|)
function_decl|;
comment|/**    * Get job conf from the old style JobContext.    * @param context job context    * @return job conf    */
specifier|public
name|JobConf
name|getJobConf
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContext
name|context
parameter_list|)
function_decl|;
specifier|public
name|FileSystem
name|getNonCachedFileSystem
parameter_list|(
name|URI
name|uri
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|getMergedCredentials
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|mergeCredentials
parameter_list|(
name|JobConf
name|dest
parameter_list|,
name|JobConf
name|src
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Check if the configured UGI has access to the path for the given file system action.    * Method will return successfully if action is permitted. AccessControlExceptoin will    * be thrown if user does not have access to perform the action. Other exceptions may    * be thrown for non-access related errors.    * @param fs    * @param status    * @param action    * @throws IOException    * @throws AccessControlException    * @throws Exception    */
specifier|public
name|void
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|status
parameter_list|,
name|FsAction
name|action
parameter_list|)
throws|throws
name|IOException
throws|,
name|AccessControlException
throws|,
name|Exception
function_decl|;
comment|/**    * Use password API (if available) to fetch credentials/password    * @param conf    * @param name    * @return    */
specifier|public
name|String
name|getPassword
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * check whether current hadoop supports sticky bit    * @return    */
name|boolean
name|supportStickyBit
parameter_list|()
function_decl|;
comment|/**    * Check stick bit in the permission    * @param permission    * @return sticky bit    */
name|boolean
name|hasStickyBit
parameter_list|(
name|FsPermission
name|permission
parameter_list|)
function_decl|;
comment|/**    * @return True if the current hadoop supports trash feature.    */
name|boolean
name|supportTrashFeature
parameter_list|()
function_decl|;
comment|/**    * @return Path to HDFS trash, if current hadoop supports trash feature.  Null otherwise.    */
name|Path
name|getCurrentTrashPath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
function_decl|;
comment|/**    * Check whether file is directory.    */
name|boolean
name|isDirectory
parameter_list|(
name|FileStatus
name|fileStatus
parameter_list|)
function_decl|;
comment|/**    * Returns a shim to wrap KerberosName    */
specifier|public
name|KerberosNameShim
name|getKerberosNameShim
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Shim for KerberosName    */
specifier|public
interface|interface
name|KerberosNameShim
block|{
specifier|public
name|String
name|getDefaultRealm
parameter_list|()
function_decl|;
specifier|public
name|String
name|getServiceName
parameter_list|()
function_decl|;
specifier|public
name|String
name|getHostName
parameter_list|()
function_decl|;
specifier|public
name|String
name|getRealm
parameter_list|()
function_decl|;
specifier|public
name|String
name|getShortName
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Copies a source dir/file to a destination by orchestrating the copy between hdfs nodes.    * This distributed process is meant to copy huge files that could take some time if a single    * copy is done.    *    * @param src Path to the source file or directory to copy    * @param dst Path to the destination file or directory    * @param conf The hadoop configuration object    * @return True if it is successfull; False otherwise.    */
specifier|public
name|boolean
name|runDistCp
parameter_list|(
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * This interface encapsulates methods used to get encryption information from    * HDFS paths.    */
specifier|public
interface|interface
name|HdfsEncryptionShim
block|{
comment|/**      * Checks if a given HDFS path is encrypted.      *      * @param path Path to HDFS file system      * @return True if it is encrypted; False otherwise.      * @throws IOException If an error occurred attempting to get encryption information      */
specifier|public
name|boolean
name|isPathEncrypted
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Checks if two HDFS paths are on the same encrypted or unencrypted zone.      *      * @param path1 Path to HDFS file system      * @param path2 Path to HDFS file system      * @return True if both paths are in the same zone; False otherwise.      * @throws IOException If an error occurred attempting to get encryption information      */
specifier|public
name|boolean
name|arePathsOnSameEncryptionZone
parameter_list|(
name|Path
name|path1
parameter_list|,
name|Path
name|path2
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Compares two encrypted path strengths.      *      * @param path1 HDFS path to compare.      * @param path2 HDFS path to compare.      * @return 1 if path1 is stronger; 0 if paths are equals; -1 if path1 is weaker.      * @throws IOException If an error occurred attempting to get encryption/key metadata      */
specifier|public
name|int
name|comparePathKeyStrength
parameter_list|(
name|Path
name|path1
parameter_list|,
name|Path
name|path2
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * create encryption zone by path and keyname      * @param path HDFS path to create encryption zone      * @param keyName keyname      * @throws IOException      */
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|createEncryptionZone
parameter_list|(
name|Path
name|path
parameter_list|,
name|String
name|keyName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Creates an encryption key.      *      * @param keyName Name of the key      * @param bitLength Key encryption length in bits (128 or 256).      * @throws IOException If an error occurs while creating the encryption key      * @throws NoSuchAlgorithmException If cipher algorithm is invalid.      */
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|createKey
parameter_list|(
name|String
name|keyName
parameter_list|,
name|int
name|bitLength
parameter_list|)
throws|throws
name|IOException
throws|,
name|NoSuchAlgorithmException
function_decl|;
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|deleteKey
parameter_list|(
name|String
name|keyName
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|VisibleForTesting
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * This is a dummy class used when the hadoop version does not support hdfs encryption.    */
specifier|public
specifier|static
class|class
name|NoopHdfsEncryptionShim
implements|implements
name|HdfsEncryptionShim
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isPathEncrypted
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* not supported */
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|arePathsOnSameEncryptionZone
parameter_list|(
name|Path
name|path1
parameter_list|,
name|Path
name|path2
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* not supported */
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|comparePathKeyStrength
parameter_list|(
name|Path
name|path1
parameter_list|,
name|Path
name|path2
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* not supported */
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createEncryptionZone
parameter_list|(
name|Path
name|path
parameter_list|,
name|String
name|keyName
parameter_list|)
block|{
comment|/* not supported */
block|}
annotation|@
name|Override
specifier|public
name|void
name|createKey
parameter_list|(
name|String
name|keyName
parameter_list|,
name|int
name|bitLength
parameter_list|)
block|{
comment|/* not supported */
block|}
annotation|@
name|Override
specifier|public
name|void
name|deleteKey
parameter_list|(
name|String
name|keyName
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* not supported */
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getKeys
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* not supported */
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Returns a new instance of the HdfsEncryption shim.    *    * @param fs A FileSystem object to HDFS    * @param conf A Configuration object    * @return A new instance of the HdfsEncryption shim.    * @throws IOException If an error occurred while creating the instance.    */
specifier|public
name|HdfsEncryptionShim
name|createHdfsEncryptionShim
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|Path
name|getPathWithoutSchemeAndAuthority
parameter_list|(
name|Path
name|path
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

