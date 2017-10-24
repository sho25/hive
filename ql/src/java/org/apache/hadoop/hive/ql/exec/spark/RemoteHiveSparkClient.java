begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
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
name|base
operator|.
name|Strings
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
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|hive
operator|.
name|common
operator|.
name|FileUtils
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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConfUtil
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
name|ql
operator|.
name|Context
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
name|ql
operator|.
name|DriverContext
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
name|ql
operator|.
name|exec
operator|.
name|DagUtils
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|SparkJobRef
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|impl
operator|.
name|RemoteSparkJobRef
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|impl
operator|.
name|RemoteSparkJobStatus
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
name|ql
operator|.
name|io
operator|.
name|HiveKey
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|plan
operator|.
name|BaseWork
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
name|ql
operator|.
name|plan
operator|.
name|SparkWork
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|BytesWritable
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
name|hive
operator|.
name|spark
operator|.
name|client
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
name|hive
operator|.
name|spark
operator|.
name|client
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
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|JobHandle
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
name|spark
operator|.
name|client
operator|.
name|SparkClient
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
name|spark
operator|.
name|client
operator|.
name|SparkClientFactory
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
name|spark
operator|.
name|client
operator|.
name|SparkClientUtilities
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
name|spark
operator|.
name|counter
operator|.
name|SparkCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaFutureAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_comment
comment|/**  * RemoteSparkClient is a wrapper of {@link org.apache.hive.spark.client.SparkClient}, which  * wrap a spark job request and send to an remote SparkContext.  */
end_comment

begin_class
specifier|public
class|class
name|RemoteHiveSparkClient
implements|implements
name|HiveSparkClient
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MR_JAR_PROPERTY
init|=
literal|"tmpjars"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MR_CREDENTIALS_LOCATION_PROPERTY
init|=
literal|"mapreduce.job.credentials.binary"
decl_stmt|;
specifier|private
specifier|static
specifier|final
specifier|transient
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RemoteHiveSparkClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
specifier|transient
name|Splitter
name|CSV_SPLITTER
init|=
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|omitEmptyStrings
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
decl_stmt|;
specifier|private
specifier|transient
name|SparkClient
name|remoteClient
decl_stmt|;
specifier|private
specifier|transient
name|SparkConf
name|sparkConf
decl_stmt|;
specifier|private
specifier|transient
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|URI
argument_list|>
name|localJars
init|=
operator|new
name|ArrayList
argument_list|<
name|URI
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|URI
argument_list|>
name|localFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|URI
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|long
name|sparkClientTimtout
decl_stmt|;
name|RemoteHiveSparkClient
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|sparkClientTimtout
operator|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_CLIENT_FUTURE_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|sparkConf
operator|=
name|HiveSparkClientFactory
operator|.
name|generateSparkConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|createRemoteClient
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|createRemoteClient
parameter_list|()
throws|throws
name|Exception
block|{
name|remoteClient
operator|=
name|SparkClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_ENABLED
argument_list|)
operator|&&
operator|(
name|SparkClientUtilities
operator|.
name|isYarnMaster
argument_list|(
name|hiveConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
argument_list|)
operator|||
name|SparkClientUtilities
operator|.
name|isLocalMaster
argument_list|(
name|hiveConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
argument_list|)
operator|)
condition|)
block|{
name|int
name|minExecutors
init|=
name|getExecutorsToWarm
argument_list|()
decl_stmt|;
if|if
condition|(
name|minExecutors
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Prewarm Spark executors. The minimum number of executors to warm is "
operator|+
name|minExecutors
argument_list|)
expr_stmt|;
comment|// Spend at most HIVE_PREWARM_SPARK_TIMEOUT to wait for executors to come up.
name|int
name|curExecutors
init|=
literal|0
decl_stmt|;
name|long
name|maxPrewarmTime
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_SPARK_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
do|do
block|{
try|try
block|{
name|curExecutors
operator|=
name|getExecutorCount
argument_list|(
name|maxPrewarmTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
comment|// let's don't fail on future timeout since we have a timeout for pre-warm
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timed out getting executor count."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curExecutors
operator|>=
name|minExecutors
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished prewarming Spark executors. The current number of executors is "
operator|+
name|curExecutors
argument_list|)
expr_stmt|;
return|return;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
comment|// sleep half a second
block|}
do|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|ts
operator|<
name|maxPrewarmTime
condition|)
do|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Timeout ("
operator|+
name|maxPrewarmTime
operator|/
literal|1000
operator|+
literal|"s) occurred while prewarming executors. "
operator|+
literal|"The current number of executors is "
operator|+
name|curExecutors
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Please note that the method is very tied with Spark documentation 1.4.1 regarding    * dynamic allocation, such as default values.    * @return    */
specifier|private
name|int
name|getExecutorsToWarm
parameter_list|()
block|{
name|int
name|minExecutors
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PREWARM_NUM_CONTAINERS
argument_list|)
decl_stmt|;
name|boolean
name|dynamicAllocation
init|=
name|hiveConf
operator|.
name|getBoolean
argument_list|(
literal|"spark.dynamicAllocation.enabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|dynamicAllocation
condition|)
block|{
name|int
name|min
init|=
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.dynamicAllocation.minExecutors"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|int
name|initExecutors
init|=
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.dynamicAllocation.initialExecutors"
argument_list|,
name|min
argument_list|)
decl_stmt|;
name|minExecutors
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minExecutors
argument_list|,
name|initExecutors
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|execInstances
init|=
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.executor.instances"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|minExecutors
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minExecutors
argument_list|,
name|execInstances
argument_list|)
expr_stmt|;
block|}
return|return
name|minExecutors
return|;
block|}
specifier|private
name|int
name|getExecutorCount
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|Exception
block|{
name|Future
argument_list|<
name|Integer
argument_list|>
name|handler
init|=
name|remoteClient
operator|.
name|getExecutorCount
argument_list|()
decl_stmt|;
return|return
name|handler
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkConf
name|getSparkConf
parameter_list|()
block|{
return|return
name|sparkConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getExecutorCount
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|getExecutorCount
argument_list|(
name|sparkClientTimtout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getDefaultParallelism
parameter_list|()
throws|throws
name|Exception
block|{
name|Future
argument_list|<
name|Integer
argument_list|>
name|handler
init|=
name|remoteClient
operator|.
name|getDefaultParallelism
argument_list|()
decl_stmt|;
return|return
name|handler
operator|.
name|get
argument_list|(
name|sparkClientTimtout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkJobRef
name|execute
parameter_list|(
specifier|final
name|DriverContext
name|driverContext
parameter_list|,
specifier|final
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|SparkClientUtilities
operator|.
name|isYarnMaster
argument_list|(
name|hiveConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
argument_list|)
operator|&&
operator|!
name|remoteClient
operator|.
name|isActive
argument_list|()
condition|)
block|{
comment|// Re-create the remote client if not active any more
name|close
argument_list|()
expr_stmt|;
name|createRemoteClient
argument_list|()
expr_stmt|;
block|}
try|try
block|{
return|return
name|submit
argument_list|(
name|driverContext
argument_list|,
name|sparkWork
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|cause
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Failed to submit Spark work, please retry later"
argument_list|,
name|cause
argument_list|)
throw|;
block|}
block|}
specifier|private
name|SparkJobRef
name|submit
parameter_list|(
specifier|final
name|DriverContext
name|driverContext
parameter_list|,
specifier|final
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
decl_stmt|;
specifier|final
name|HiveConf
name|hiveConf
init|=
operator|(
name|HiveConf
operator|)
name|ctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|refreshLocalResources
argument_list|(
name|sparkWork
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
specifier|final
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|//update the credential provider location in the jobConf
name|HiveConfUtil
operator|.
name|updateJobCredentialProviders
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
comment|// Create temporary scratch dir
specifier|final
name|Path
name|emptyScratchDir
init|=
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|emptyScratchDir
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|emptyScratchDir
argument_list|)
expr_stmt|;
name|byte
index|[]
name|jobConfBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|scratchDirBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|emptyScratchDir
argument_list|)
decl_stmt|;
name|byte
index|[]
name|sparkWorkBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|sparkWork
argument_list|)
decl_stmt|;
name|JobStatusJob
name|job
init|=
operator|new
name|JobStatusJob
argument_list|(
name|jobConfBytes
argument_list|,
name|scratchDirBytes
argument_list|,
name|sparkWorkBytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|driverContext
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Operation is cancelled."
argument_list|)
throw|;
block|}
name|JobHandle
argument_list|<
name|Serializable
argument_list|>
name|jobHandle
init|=
name|remoteClient
operator|.
name|submit
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|RemoteSparkJobStatus
name|sparkJobStatus
init|=
operator|new
name|RemoteSparkJobStatus
argument_list|(
name|remoteClient
argument_list|,
name|jobHandle
argument_list|,
name|sparkClientTimtout
argument_list|)
decl_stmt|;
return|return
operator|new
name|RemoteSparkJobRef
argument_list|(
name|hiveConf
argument_list|,
name|jobHandle
argument_list|,
name|sparkJobStatus
argument_list|)
return|;
block|}
specifier|private
name|void
name|refreshLocalResources
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// add hive-exec jar
name|addJars
argument_list|(
operator|(
operator|new
name|JobConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|)
operator|.
name|getJar
argument_list|()
argument_list|)
expr_stmt|;
comment|// add aux jars
name|addJars
argument_list|(
name|conf
operator|.
name|getAuxJars
argument_list|()
argument_list|)
expr_stmt|;
name|addJars
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getReloadableAuxJars
argument_list|()
argument_list|)
expr_stmt|;
comment|// add added jars
name|String
name|addedJars
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDJARS
argument_list|,
name|addedJars
argument_list|)
expr_stmt|;
name|addJars
argument_list|(
name|addedJars
argument_list|)
expr_stmt|;
comment|// add plugin module jars on demand
comment|// jobConf will hold all the configuration for hadoop, tez, and hive
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|MR_JAR_PROPERTY
argument_list|,
literal|""
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|work
operator|.
name|configureJobConf
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|addJars
argument_list|(
name|jobConf
operator|.
name|get
argument_list|(
name|MR_JAR_PROPERTY
argument_list|)
argument_list|)
expr_stmt|;
comment|// remove the location of container tokens
name|conf
operator|.
name|unset
argument_list|(
name|MR_CREDENTIALS_LOCATION_PROPERTY
argument_list|)
expr_stmt|;
comment|// add added files
name|String
name|addedFiles
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|FILE
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDFILES
argument_list|,
name|addedFiles
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedFiles
argument_list|)
expr_stmt|;
comment|// add added archives
name|String
name|addedArchives
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|ARCHIVE
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDARCHIVES
argument_list|,
name|addedArchives
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedArchives
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addResources
parameter_list|(
name|String
name|addedFiles
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|addedFile
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|addedFiles
argument_list|)
argument_list|)
control|)
block|{
try|try
block|{
name|URI
name|fileUri
init|=
name|FileUtils
operator|.
name|getURI
argument_list|(
name|addedFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileUri
operator|!=
literal|null
operator|&&
operator|!
name|localFiles
operator|.
name|contains
argument_list|(
name|fileUri
argument_list|)
condition|)
block|{
name|localFiles
operator|.
name|add
argument_list|(
name|fileUri
argument_list|)
expr_stmt|;
if|if
condition|(
name|SparkUtilities
operator|.
name|needUploadToHDFS
argument_list|(
name|fileUri
argument_list|,
name|sparkConf
argument_list|)
condition|)
block|{
name|fileUri
operator|=
name|SparkUtilities
operator|.
name|uploadToHDFS
argument_list|(
name|fileUri
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
name|remoteClient
operator|.
name|addFile
argument_list|(
name|fileUri
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to add file:"
operator|+
name|addedFile
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|addJars
parameter_list|(
name|String
name|addedJars
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|addedJar
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|addedJars
argument_list|)
argument_list|)
control|)
block|{
try|try
block|{
name|URI
name|jarUri
init|=
name|FileUtils
operator|.
name|getURI
argument_list|(
name|addedJar
argument_list|)
decl_stmt|;
if|if
condition|(
name|jarUri
operator|!=
literal|null
operator|&&
operator|!
name|localJars
operator|.
name|contains
argument_list|(
name|jarUri
argument_list|)
condition|)
block|{
name|localJars
operator|.
name|add
argument_list|(
name|jarUri
argument_list|)
expr_stmt|;
if|if
condition|(
name|SparkUtilities
operator|.
name|needUploadToHDFS
argument_list|(
name|jarUri
argument_list|,
name|sparkConf
argument_list|)
condition|)
block|{
name|jarUri
operator|=
name|SparkUtilities
operator|.
name|uploadToHDFS
argument_list|(
name|jarUri
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
name|remoteClient
operator|.
name|addJar
argument_list|(
name|jarUri
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to add jar:"
operator|+
name|addedJar
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|remoteClient
operator|!=
literal|null
condition|)
block|{
name|remoteClient
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|localFiles
operator|.
name|clear
argument_list|()
expr_stmt|;
name|localJars
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|JobStatusJob
implements|implements
name|Job
argument_list|<
name|Serializable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|jobConfBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|scratchDirBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|sparkWorkBytes
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|JobStatusJob
parameter_list|()
block|{
comment|// For deserialization.
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|JobStatusJob
parameter_list|(
name|byte
index|[]
name|jobConfBytes
parameter_list|,
name|byte
index|[]
name|scratchDirBytes
parameter_list|,
name|byte
index|[]
name|sparkWorkBytes
parameter_list|)
block|{
name|this
operator|.
name|jobConfBytes
operator|=
name|jobConfBytes
expr_stmt|;
name|this
operator|.
name|scratchDirBytes
operator|=
name|scratchDirBytes
expr_stmt|;
name|this
operator|.
name|sparkWorkBytes
operator|=
name|sparkWorkBytes
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Serializable
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|localJobConf
init|=
name|KryoSerializer
operator|.
name|deserializeJobConf
argument_list|(
name|jobConfBytes
argument_list|)
decl_stmt|;
comment|// Add jar to current thread class loader dynamically, and add jar paths to JobConf as Spark
comment|// may need to load classes from this jar in other threads.
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|addedJars
init|=
name|jc
operator|.
name|getAddedJars
argument_list|()
decl_stmt|;
if|if
condition|(
name|addedJars
operator|!=
literal|null
operator|&&
operator|!
name|addedJars
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|localAddedJars
init|=
name|SparkClientUtilities
operator|.
name|addToClassPath
argument_list|(
name|addedJars
argument_list|,
name|localJobConf
argument_list|,
name|jc
operator|.
name|getLocalTmpDir
argument_list|()
argument_list|)
decl_stmt|;
name|localJobConf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|HIVE_ADDED_JARS
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
name|localAddedJars
argument_list|,
literal|";"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Path
name|localScratchDir
init|=
name|KryoSerializer
operator|.
name|deserialize
argument_list|(
name|scratchDirBytes
argument_list|,
name|Path
operator|.
name|class
argument_list|)
decl_stmt|;
name|SparkWork
name|localSparkWork
init|=
name|KryoSerializer
operator|.
name|deserialize
argument_list|(
name|sparkWorkBytes
argument_list|,
name|SparkWork
operator|.
name|class
argument_list|)
decl_stmt|;
name|logConfigurations
argument_list|(
name|localJobConf
argument_list|)
expr_stmt|;
name|SparkCounters
name|sparkCounters
init|=
operator|new
name|SparkCounters
argument_list|(
name|jc
operator|.
name|sc
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|prefixes
init|=
name|localSparkWork
operator|.
name|getRequiredCounterPrefix
argument_list|()
decl_stmt|;
if|if
condition|(
name|prefixes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|prefixes
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|counterName
range|:
name|prefixes
operator|.
name|get
argument_list|(
name|group
argument_list|)
control|)
block|{
name|sparkCounters
operator|.
name|createCounter
argument_list|(
name|group
argument_list|,
name|counterName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|SparkReporter
name|sparkReporter
init|=
operator|new
name|SparkReporter
argument_list|(
name|sparkCounters
argument_list|)
decl_stmt|;
comment|// Generate Spark plan
name|SparkPlanGenerator
name|gen
init|=
operator|new
name|SparkPlanGenerator
argument_list|(
name|jc
operator|.
name|sc
argument_list|()
argument_list|,
literal|null
argument_list|,
name|localJobConf
argument_list|,
name|localScratchDir
argument_list|,
name|sparkReporter
argument_list|)
decl_stmt|;
name|SparkPlan
name|plan
init|=
name|gen
operator|.
name|generate
argument_list|(
name|localSparkWork
argument_list|)
decl_stmt|;
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|setJobGroup
argument_list|(
literal|"queryId = "
operator|+
name|localSparkWork
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|DagUtils
operator|.
name|getQueryName
argument_list|(
name|localJobConf
argument_list|)
argument_list|)
expr_stmt|;
comment|// Execute generated plan.
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|finalRDD
init|=
name|plan
operator|.
name|generateGraph
argument_list|()
decl_stmt|;
comment|// We use Spark RDD async action to submit job as it's the only way to get jobId now.
name|JavaFutureAction
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|finalRDD
operator|.
name|foreachAsync
argument_list|(
name|HiveVoidFunction
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|jc
operator|.
name|monitor
argument_list|(
name|future
argument_list|,
name|sparkCounters
argument_list|,
name|plan
operator|.
name|getCachedRDDIds
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|logConfigurations
parameter_list|(
name|JobConf
name|localJobConf
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Logging job configuration: "
argument_list|)
expr_stmt|;
name|StringBuilder
name|outWriter
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|// redact sensitive information before logging
name|HiveConfUtil
operator|.
name|dumpConfig
argument_list|(
name|localJobConf
argument_list|,
name|outWriter
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|outWriter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

