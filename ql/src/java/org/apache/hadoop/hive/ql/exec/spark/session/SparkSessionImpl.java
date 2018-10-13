begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|session
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
name|concurrent
operator|.
name|TimeoutException
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
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
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
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|Maps
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
name|collect
operator|.
name|Sets
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
name|ql
operator|.
name|ErrorMsg
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
name|hive
operator|.
name|common
operator|.
name|ObjectPair
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
name|spark
operator|.
name|HiveSparkClient
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
name|HiveSparkClientFactory
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
name|SparkWork
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
name|util
operator|.
name|Utils
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
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Implementation of {@link SparkSession} that treats each Spark session as a separate Spark  * application.  *  *<p>  *   It uses a {@link HiveSparkClient} to submit a Spark application and to submit Spark jobs to  *   the Spark app.  *</p>  *  *<p>  *   This class contains logic to trigger a timeout of this {@link SparkSession} if certain  *   conditions are met (e.g. a job hasn't been submitted in the past "x" seconds). Since we use  *   a threadpool to schedule a task that regularly checks if a session has timed out, we need to  *   properly synchronize the {@link #open(HiveConf)} and {@link #close()} methods. We use a  *   series of volatile variables and read-write locks to ensure this.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|SparkSessionImpl
implements|implements
name|SparkSession
block|{
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
name|SparkSession
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPARK_DIR
init|=
literal|"_spark_session_dir"
decl_stmt|;
comment|/** Regex for different Spark session error messages */
specifier|private
specifier|static
specifier|final
name|String
name|AM_TIMEOUT_ERR
init|=
literal|".*ApplicationMaster for attempt.*timed out.*"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|UNKNOWN_QUEUE_ERR
init|=
literal|"(submitted by user.*to unknown queue:.*)\n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|STOPPED_QUEUE_ERR
init|=
literal|"(Queue.*is STOPPED)"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FULL_QUEUE_ERR
init|=
literal|"(Queue.*already has.*applications)"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INVALILD_MEM_ERR
init|=
literal|"(Required executor memory.*is above the max threshold.*) of this"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INVALID_CORE_ERR
init|=
literal|"(initial executor number.*must between min executor.*and max executor number.*)\n"
decl_stmt|;
comment|/** Pre-compiled error patterns. Shared between all Spark sessions */
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Pattern
argument_list|>
name|errorPatterns
decl_stmt|;
comment|// Several of the following variables need to be volatile so they can be accessed by the timeout
comment|// thread
specifier|private
specifier|volatile
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|isOpen
decl_stmt|;
specifier|private
specifier|final
name|String
name|sessionId
decl_stmt|;
specifier|private
specifier|volatile
name|HiveSparkClient
name|hiveSparkClient
decl_stmt|;
specifier|private
specifier|volatile
name|Path
name|scratchDir
decl_stmt|;
comment|/**    * The timestamp of the last completed Spark job.    */
specifier|private
specifier|volatile
name|long
name|lastSparkJobCompletionTime
decl_stmt|;
comment|/**    * A {@link Set} of currently running queries. Each job is identified by its query id.    */
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|activeJobs
init|=
name|Sets
operator|.
name|newConcurrentHashSet
argument_list|()
decl_stmt|;
comment|/**    * True if at least a single query has been run by this session, false otherwise.    */
specifier|private
specifier|volatile
name|boolean
name|queryCompleted
decl_stmt|;
specifier|private
name|ReadWriteLock
name|closeLock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
name|SparkSessionImpl
parameter_list|(
name|String
name|sessionId
parameter_list|)
block|{
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
name|initErrorPatterns
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to open Hive on Spark session {}"
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|isOpen
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|hiveSparkClient
operator|=
name|HiveSparkClientFactory
operator|.
name|createHiveSparkClient
argument_list|(
name|conf
argument_list|,
name|sessionId
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
comment|// It's possible that user session is closed while creating Spark client.
name|HiveException
name|he
decl_stmt|;
if|if
condition|(
name|isOpen
condition|)
block|{
name|he
operator|=
name|getHiveException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|he
operator|=
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_CLOSED_SESSION
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
block|}
throw|throw
name|he
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Hive on Spark session {} successfully opened"
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SparkJobRef
name|submit
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|isOpen
argument_list|,
literal|"Hive on Spark session is not open. Can't submit jobs."
argument_list|)
expr_stmt|;
return|return
name|hiveSparkClient
operator|.
name|execute
argument_list|(
name|driverContext
argument_list|,
name|sparkWork
argument_list|)
return|;
block|}
finally|finally
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|getMemoryAndCores
parameter_list|()
throws|throws
name|Exception
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|SparkConf
name|sparkConf
init|=
name|hiveSparkClient
operator|.
name|getSparkConf
argument_list|()
decl_stmt|;
name|int
name|numExecutors
init|=
name|hiveSparkClient
operator|.
name|getExecutorCount
argument_list|()
decl_stmt|;
comment|// at start-up, we may be unable to get number of executors
if|if
condition|(
name|numExecutors
operator|<=
literal|0
condition|)
block|{
return|return
operator|new
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
operator|-
literal|1L
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
name|int
name|executorMemoryInMB
init|=
name|Utils
operator|.
name|memoryStringToMb
argument_list|(
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.executor.memory"
argument_list|,
literal|"512m"
argument_list|)
argument_list|)
decl_stmt|;
name|double
name|memoryFraction
init|=
literal|1.0
operator|-
name|sparkConf
operator|.
name|getDouble
argument_list|(
literal|"spark.storage.memoryFraction"
argument_list|,
literal|0.6
argument_list|)
decl_stmt|;
name|long
name|totalMemory
init|=
call|(
name|long
call|)
argument_list|(
name|numExecutors
operator|*
name|executorMemoryInMB
operator|*
name|memoryFraction
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|int
name|totalCores
decl_stmt|;
name|String
name|masterURL
init|=
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
if|if
condition|(
name|masterURL
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
operator|||
name|masterURL
operator|.
name|startsWith
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
name|totalCores
operator|=
name|sparkConf
operator|.
name|contains
argument_list|(
literal|"spark.default.parallelism"
argument_list|)
condition|?
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.default.parallelism"
argument_list|,
literal|1
argument_list|)
else|:
name|hiveSparkClient
operator|.
name|getDefaultParallelism
argument_list|()
expr_stmt|;
name|totalCores
operator|=
name|Math
operator|.
name|max
argument_list|(
name|totalCores
argument_list|,
name|numExecutors
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|coresPerExecutor
init|=
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.executor.cores"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|totalCores
operator|=
name|numExecutors
operator|*
name|coresPerExecutor
expr_stmt|;
block|}
name|totalCores
operator|=
name|totalCores
operator|/
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.task.cpus"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|long
name|memoryPerTaskInBytes
init|=
name|totalMemory
operator|/
name|totalCores
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Hive on Spark application currently has number of executors: "
operator|+
name|numExecutors
operator|+
literal|", total cores: "
operator|+
name|totalCores
operator|+
literal|", memory per executor: "
operator|+
name|executorMemoryInMB
operator|+
literal|" mb, memoryFraction: "
operator|+
name|memoryFraction
argument_list|)
expr_stmt|;
return|return
operator|new
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|memoryPerTaskInBytes
argument_list|)
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|totalCores
argument_list|)
argument_list|)
return|;
block|}
finally|finally
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|isOpen
return|;
block|}
finally|finally
block|{
name|closeLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
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
name|isOpen
condition|)
block|{
name|closeLock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|isOpen
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to close Hive on Spark session {}"
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|isOpen
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|hiveSparkClient
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|hiveSparkClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Hive on Spark session {} successfully closed"
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|cleanScratchDir
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close Hive on Spark session ("
operator|+
name|sessionId
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|hiveSparkClient
operator|=
literal|null
expr_stmt|;
name|queryCompleted
operator|=
literal|false
expr_stmt|;
name|lastSparkJobCompletionTime
operator|=
literal|0
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|closeLock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Path
name|createScratchDir
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|parent
init|=
operator|new
name|Path
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHdfsScratchDirURIString
argument_list|()
argument_list|,
name|SPARK_DIR
argument_list|)
decl_stmt|;
name|Path
name|sparkDir
init|=
operator|new
name|Path
argument_list|(
name|parent
argument_list|,
name|sessionId
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|sparkDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FsPermission
name|fsPermission
init|=
operator|new
name|FsPermission
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIRPERMISSION
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|sparkDir
argument_list|,
name|fsPermission
argument_list|)
expr_stmt|;
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|sparkDir
argument_list|)
expr_stmt|;
return|return
name|sparkDir
return|;
block|}
specifier|private
specifier|static
name|void
name|initErrorPatterns
parameter_list|()
block|{
name|errorPatterns
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|(
operator|new
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Pattern
argument_list|>
argument_list|()
operator|.
name|put
argument_list|(
name|AM_TIMEOUT_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|AM_TIMEOUT_ERR
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|UNKNOWN_QUEUE_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|UNKNOWN_QUEUE_ERR
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|STOPPED_QUEUE_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|STOPPED_QUEUE_ERR
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|FULL_QUEUE_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|FULL_QUEUE_ERR
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|INVALILD_MEM_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|INVALILD_MEM_ERR
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|INVALID_CORE_ERR
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
name|INVALID_CORE_ERR
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|HiveException
name|getHiveException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|Throwable
name|oe
init|=
name|e
decl_stmt|;
name|StringBuilder
name|matchedString
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|TimeoutException
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_TIMEOUT
argument_list|,
name|sessionId
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|InterruptedException
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_INTERRUPTED
argument_list|,
name|sessionId
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|RuntimeException
condition|)
block|{
name|String
name|sts
init|=
name|Throwables
operator|.
name|getStackTraceAsString
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|matches
argument_list|(
name|sts
argument_list|,
name|AM_TIMEOUT_ERR
argument_list|,
name|matchedString
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_TIMEOUT
argument_list|,
name|sessionId
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|matches
argument_list|(
name|sts
argument_list|,
name|UNKNOWN_QUEUE_ERR
argument_list|,
name|matchedString
argument_list|)
operator|||
name|matches
argument_list|(
name|sts
argument_list|,
name|STOPPED_QUEUE_ERR
argument_list|,
name|matchedString
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_INVALID_QUEUE
argument_list|,
name|sessionId
argument_list|,
name|matchedString
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|matches
argument_list|(
name|sts
argument_list|,
name|FULL_QUEUE_ERR
argument_list|,
name|matchedString
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_QUEUE_FULL
argument_list|,
name|sessionId
argument_list|,
name|matchedString
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|matches
argument_list|(
name|sts
argument_list|,
name|INVALILD_MEM_ERR
argument_list|,
name|matchedString
argument_list|)
operator|||
name|matches
argument_list|(
name|sts
argument_list|,
name|INVALID_CORE_ERR
argument_list|,
name|matchedString
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_INVALID_RESOURCE_REQUEST
argument_list|,
name|sessionId
argument_list|,
name|matchedString
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_ERROR
argument_list|,
name|sessionId
argument_list|,
name|Throwables
operator|.
name|getRootCause
argument_list|(
name|e
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
name|e
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|HiveException
argument_list|(
name|oe
argument_list|,
name|ErrorMsg
operator|.
name|SPARK_CREATE_CLIENT_ERROR
argument_list|,
name|sessionId
argument_list|,
name|Throwables
operator|.
name|getRootCause
argument_list|(
name|oe
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|matches
parameter_list|(
name|String
name|input
parameter_list|,
name|String
name|regex
parameter_list|,
name|StringBuilder
name|matchedString
parameter_list|)
block|{
if|if
condition|(
operator|!
name|errorPatterns
operator|.
name|containsKey
argument_list|(
name|regex
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No error pattern found for regex: {}"
argument_list|,
name|regex
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Pattern
name|p
init|=
name|errorPatterns
operator|.
name|get
argument_list|(
name|regex
argument_list|)
decl_stmt|;
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|m
operator|.
name|find
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|&&
name|m
operator|.
name|groupCount
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// assume matchedString is empty
name|matchedString
operator|.
name|append
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|//This method is not thread safe
specifier|private
name|void
name|cleanScratchDir
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|scratchDir
operator|!=
literal|null
condition|)
block|{
name|FileSystem
name|fs
init|=
name|scratchDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|scratchDir
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Create scratch directory for spark session if it does not exist.    * This method is not thread safe.    * @return Path to Spark session scratch directory.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|Path
name|getHDFSSessionDir
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|scratchDir
operator|==
literal|null
condition|)
block|{
name|scratchDir
operator|=
name|createScratchDir
argument_list|()
expr_stmt|;
block|}
return|return
name|scratchDir
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onQuerySubmission
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|activeJobs
operator|.
name|add
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check if a session has timed out, and if it has close the session.    */
annotation|@
name|Override
specifier|public
name|boolean
name|triggerTimeout
parameter_list|(
name|long
name|sessionTimeout
parameter_list|)
block|{
if|if
condition|(
name|hasTimedOut
argument_list|(
name|queryCompleted
argument_list|,
name|activeJobs
argument_list|,
name|lastSparkJobCompletionTime
argument_list|,
name|sessionTimeout
argument_list|)
condition|)
block|{
name|closeLock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|hasTimedOut
argument_list|(
name|queryCompleted
argument_list|,
name|activeJobs
argument_list|,
name|lastSparkJobCompletionTime
argument_list|,
name|sessionTimeout
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Closing Spark session "
operator|+
name|getSessionId
argument_list|()
operator|+
literal|" because a Spark job has not "
operator|+
literal|"been run in the past "
operator|+
name|sessionTimeout
operator|/
literal|1000
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
finally|finally
block|{
name|closeLock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Returns true if a session has timed out, false otherwise. The following conditions must be met    * in order to consider a session as timed out: (1) the session must have run at least one    * query, (2) there can be no actively running Spark jobs, and (3) the last completed Spark job    * must have been more than sessionTimeout seconds ago.    */
specifier|private
specifier|static
name|boolean
name|hasTimedOut
parameter_list|(
name|boolean
name|queryCompleted
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|activeJobs
parameter_list|,
name|long
name|lastSparkJobCompletionTime
parameter_list|,
name|long
name|sessionTimeout
parameter_list|)
block|{
return|return
name|queryCompleted
operator|&&
name|activeJobs
operator|.
name|isEmpty
argument_list|()
operator|&&
name|lastSparkJobCompletionTime
operator|>
literal|0
operator|&&
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastSparkJobCompletionTime
operator|)
operator|>
name|sessionTimeout
return|;
block|}
comment|/**    * When this session completes the execution of a query, set the {@link #queryCompleted} flag    * to true if it hasn't already been set, remove the query from the list of actively running jobs,    * and set the {@link #lastSparkJobCompletionTime} to the current timestamp.    */
annotation|@
name|Override
specifier|public
name|void
name|onQueryCompletion
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
if|if
condition|(
operator|!
name|queryCompleted
condition|)
block|{
name|queryCompleted
operator|=
literal|true
expr_stmt|;
block|}
name|activeJobs
operator|.
name|remove
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
name|lastSparkJobCompletionTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|HiveSparkClient
name|getHiveSparkClient
parameter_list|()
block|{
return|return
name|hiveSparkClient
return|;
block|}
block|}
end_class

end_unit

