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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|net
operator|.
name|URISyntaxException
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
name|Iterator
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
name|InputSplit
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HadoopShims
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|String
name|getInputFormatClassName
parameter_list|()
function_decl|;
name|int
name|createHadoopArchive
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|parentDir
parameter_list|,
name|Path
name|destDir
parameter_list|,
name|String
name|archiveName
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|public
name|URI
name|getHarUri
parameter_list|(
name|URI
name|original
parameter_list|,
name|URI
name|base
parameter_list|,
name|URI
name|originalBase
parameter_list|)
throws|throws
name|URISyntaxException
function_decl|;
comment|/**    * Hive uses side effect files exclusively for it's output. It also manages    * the setup/cleanup/commit of output from the hive client. As a result it does    * not need support for the same inside the MR framework    *    * This routine sets the appropriate options related to bypass setup/cleanup/commit    * support in the MR framework, but does not set the OutputFormat class.    */
name|void
name|prepareJobOutput
parameter_list|(
name|JobConf
name|conf
parameter_list|)
function_decl|;
comment|/**    * Used by TaskLogProcessor to Remove HTML quoting from a string    * @param item the string to unquote    * @return the unquoted string    *    */
specifier|public
name|String
name|unquoteHtmlChars
parameter_list|(
name|String
name|item
parameter_list|)
function_decl|;
specifier|public
name|void
name|closeAllForUGI
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
function_decl|;
comment|/**    * Get the UGI that the given job configuration will run as.    *    * In secure versions of Hadoop, this simply returns the current    * access control context's user, ignoring the configuration.    */
specifier|public
name|UserGroupInformation
name|getUGIForConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|LoginException
throws|,
name|IOException
function_decl|;
comment|/**    * Used by metastore server to perform requested rpc in client context.    * @param<T>    * @param ugi    * @param pvea    * @throws IOException    * @throws InterruptedException    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|doAs
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|,
name|PrivilegedExceptionAction
argument_list|<
name|T
argument_list|>
name|pvea
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Once a delegation token is stored in a file, the location is specified    * for a child process that runs hadoop operations, using an environment    * variable .    * @return Return the name of environment variable used by hadoop to find    *  location of token file    */
specifier|public
name|String
name|getTokenFileLocEnvName
parameter_list|()
function_decl|;
comment|/**    * Get delegation token from filesystem and write the token along with    * metastore tokens into a file    * @param conf    * @return Path of the file with token credential    * @throws IOException    */
specifier|public
name|Path
name|createDelegationTokenFile
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Used by metastore server to creates UGI object for a remote user.    * @param userName remote User Name    * @param groupNames group names associated with remote user name    * @return UGI created for the remote user.    */
specifier|public
name|UserGroupInformation
name|createRemoteUser
parameter_list|(
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
function_decl|;
comment|/**    * Get the short name corresponding to the subject in the passed UGI    *    * In secure versions of Hadoop, this returns the short name (after    * undergoing the translation in the kerberos name rule mapping).    * In unsecure versions of Hadoop, this returns the name of the subject    */
specifier|public
name|String
name|getShortUserName
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
function_decl|;
comment|/**    * Return true if the Shim is based on Hadoop Security APIs.    */
specifier|public
name|boolean
name|isSecureShimImpl
parameter_list|()
function_decl|;
comment|/**    * Return true if the hadoop configuration has security enabled    * @return    */
specifier|public
name|boolean
name|isSecurityEnabled
parameter_list|()
function_decl|;
comment|/**    * Get the string form of the token given a token signature.    * The signature is used as the value of the "service" field in the token for lookup.    * Ref: AbstractDelegationTokenSelector in Hadoop. If there exists such a token    * in the token cache (credential store) of the job, the lookup returns that.    * This is relevant only when running against a "secure" hadoop release    * The method gets hold of the tokens if they are set up by hadoop - this should    * happen on the map/reduce tasks if the client added the tokens into hadoop's    * credential store in the front end during job submission. The method will    * select the hive delegation token among the set of tokens and return the string    * form of it    * @param tokenSignature    * @return the string form of the token found    * @throws IOException    */
name|String
name|getTokenStrForm
parameter_list|(
name|String
name|tokenSignature
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Add a delegation token to the given ugi    * @param ugi    * @param tokenStr    * @param tokenService    * @throws IOException    */
name|void
name|setTokenStr
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|,
name|String
name|tokenStr
parameter_list|,
name|String
name|tokenService
parameter_list|)
throws|throws
name|IOException
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
comment|/**    *  Perform kerberos login using the given principal and keytab    * @throws IOException    */
specifier|public
name|void
name|loginUserFromKeytab
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|keytabFile
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Perform kerberos re-login using the given principal and keytab, to renew    * the credentials    * @throws IOException    */
specifier|public
name|void
name|reLoginUserFromKeytab
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/***    * Check if the current UGI is keytab based    * @return    * @throws IOException    */
specifier|public
name|boolean
name|isLoginKeytabBased
parameter_list|()
throws|throws
name|IOException
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
comment|/**    * Create the proxy ugi for the given userid    * @param userName    * @return    */
name|UserGroupInformation
name|createProxyUser
parameter_list|(
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
comment|/**    * InputSplitShim.    *    */
specifier|public
interface|interface
name|InputSplitShim
extends|extends
name|InputSplit
block|{
name|JobConf
name|getJob
parameter_list|()
function_decl|;
name|long
name|getLength
parameter_list|()
function_decl|;
comment|/** Returns an array containing the startoffsets of the files in the split. */
name|long
index|[]
name|getStartOffsets
parameter_list|()
function_decl|;
comment|/** Returns an array containing the lengths of the files in the split. */
name|long
index|[]
name|getLengths
parameter_list|()
function_decl|;
comment|/** Returns the start offset of the i<sup>th</sup> Path. */
name|long
name|getOffset
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/** Returns the length of the i<sup>th</sup> Path. */
name|long
name|getLength
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/** Returns the number of Paths in the split. */
name|int
name|getNumPaths
parameter_list|()
function_decl|;
comment|/** Returns the i<sup>th</sup> Path. */
name|Path
name|getPath
parameter_list|(
name|int
name|i
parameter_list|)
function_decl|;
comment|/** Returns all the Paths in the split. */
name|Path
index|[]
name|getPaths
parameter_list|()
function_decl|;
comment|/** Returns all the Paths where this input-split resides. */
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|void
name|shrinkSplit
parameter_list|(
name|long
name|length
parameter_list|)
function_decl|;
name|String
name|toString
parameter_list|()
function_decl|;
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
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
name|InputSplitShim
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
name|InputSplitShim
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
name|InputSplitShim
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
comment|/**    * Get the block locations for the given directory.    * @param fs the file system    * @param path the directory name to get the status and block locations    * @param filter a filter that needs to accept the file (or null)    * @return an iterator for the located file status objects    * @throws IOException    */
name|Iterator
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
comment|/**    * Get configuration from JobContext    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|(
name|JobContext
name|context
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

