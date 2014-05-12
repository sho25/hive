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
name|classification
operator|.
name|InterfaceAudience
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
name|NullWritable
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
name|Text
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
name|Mapper
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
name|Shell
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
name|StringUtils
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
name|hcatalog
operator|.
name|templeton
operator|.
name|BadParam
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
name|hcatalog
operator|.
name|templeton
operator|.
name|LauncherDelegator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|Arrays
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
name|LinkedList
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
name|ExecutorService
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
name|Executors
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

begin_comment
comment|/**  * Note that this class is used in a different JVM than WebHCat server.  Thus it should not call   * any classes not available on every node in the cluster (outside webhcat jar).  * TempletonControllerJob#run() calls Job.setJarByClass(LaunchMapper.class) which  * causes webhcat jar to be shipped to target node, but not it's transitive closure.  * Long term we need to clean up this separation and create a separate jar to ship so that the  * dependencies are clear.  (This used to be an inner class of TempletonControllerJob)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LaunchMapper
extends|extends
name|Mapper
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|,
name|Text
argument_list|,
name|Text
argument_list|>
implements|implements
name|JobSubmissionConstants
block|{
comment|/**    * This class currently sends everything to stderr, but it should probably use Log4J -     * it will end up in 'syslog' of this Map task.  For example, look for KeepAlive heartbeat msgs.    */
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
name|LaunchMapper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * When a Pig job is submitted and it uses HCat, WebHCat may be configured to ship hive tar    * to the target node.  Pig on the target node needs some env vars configured.    */
specifier|private
specifier|static
name|void
name|handlePigEnvVars
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|HIVE_HOME
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|env
operator|.
name|put
argument_list|(
name|PigConstants
operator|.
name|HIVE_HOME
argument_list|,
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|HIVE_HOME
argument_list|)
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|HCAT_HOME
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|env
operator|.
name|put
argument_list|(
name|PigConstants
operator|.
name|HCAT_HOME
argument_list|,
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|HCAT_HOME
argument_list|)
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|PIG_OPTS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|StringBuilder
name|pigOpts
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|prop
range|:
name|StringUtils
operator|.
name|split
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|PigConstants
operator|.
name|PIG_OPTS
argument_list|)
argument_list|)
control|)
block|{
name|pigOpts
operator|.
name|append
argument_list|(
literal|"-D"
argument_list|)
operator|.
name|append
argument_list|(
name|TempletonUtils
operator|.
name|unEscape
argument_list|(
name|prop
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|put
argument_list|(
name|PigConstants
operator|.
name|PIG_OPTS
argument_list|,
name|pigOpts
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|Process
name|startJob
parameter_list|(
name|Context
name|context
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|overrideClasspath
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|copyLocal
argument_list|(
name|COPY_NAME
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|String
index|[]
name|jarArgs
init|=
name|TempletonUtils
operator|.
name|decodeArray
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|JAR_ARGS_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|removeEnv
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|//todo: we really need some comments to explain exactly why each of these is removed
name|removeEnv
operator|.
name|add
argument_list|(
literal|"HADOOP_ROOT_LOGGER"
argument_list|)
expr_stmt|;
name|removeEnv
operator|.
name|add
argument_list|(
literal|"hadoop-command"
argument_list|)
expr_stmt|;
name|removeEnv
operator|.
name|add
argument_list|(
literal|"CLASS"
argument_list|)
expr_stmt|;
name|removeEnv
operator|.
name|add
argument_list|(
literal|"mapredcommand"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
name|TempletonUtils
operator|.
name|hadoopUserEnv
argument_list|(
name|user
argument_list|,
name|overrideClasspath
argument_list|)
decl_stmt|;
name|handlePigEnvVars
argument_list|(
name|conf
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jarArgsList
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|jarArgs
argument_list|)
argument_list|)
decl_stmt|;
name|handleTokenFile
argument_list|(
name|jarArgsList
argument_list|,
name|JobSubmissionConstants
operator|.
name|TOKEN_FILE_ARG_PLACEHOLDER
argument_list|,
literal|"mapreduce.job.credentials.binary"
argument_list|)
expr_stmt|;
name|handleTokenFile
argument_list|(
name|jarArgsList
argument_list|,
name|JobSubmissionConstants
operator|.
name|TOKEN_FILE_ARG_PLACEHOLDER_TEZ
argument_list|,
literal|"tez.credentials.path"
argument_list|)
expr_stmt|;
return|return
name|TrivialExecService
operator|.
name|getInstance
argument_list|()
operator|.
name|run
argument_list|(
name|jarArgsList
argument_list|,
name|removeEnv
argument_list|,
name|env
argument_list|)
return|;
block|}
comment|/**    * Replace placeholder with actual "prop=file".  This is done multiple times (possibly) since    * Tez and MR use different property names    */
specifier|private
specifier|static
name|void
name|handleTokenFile
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|jarArgsList
parameter_list|,
name|String
name|tokenPlaceHolder
parameter_list|,
name|String
name|tokenProperty
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|tokenFile
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_TOKEN_FILE_LOCATION"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenFile
operator|!=
literal|null
condition|)
block|{
comment|//Token is available, so replace the placeholder
name|tokenFile
operator|=
name|tokenFile
operator|.
name|replaceAll
argument_list|(
literal|"\""
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|tokenArg
init|=
name|tokenProperty
operator|+
literal|"="
operator|+
name|tokenFile
decl_stmt|;
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
try|try
block|{
name|tokenArg
operator|=
name|TempletonUtils
operator|.
name|quoteForWindows
argument_list|(
name|tokenArg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadParam
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"cannot pass "
operator|+
name|tokenFile
operator|+
literal|" to "
operator|+
name|tokenProperty
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|jarArgsList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|newArg
init|=
name|jarArgsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|replace
argument_list|(
name|tokenPlaceHolder
argument_list|,
name|tokenArg
argument_list|)
decl_stmt|;
name|jarArgsList
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|newArg
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|//No token, so remove the placeholder arg
name|Iterator
argument_list|<
name|String
argument_list|>
name|it
init|=
name|jarArgsList
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|arg
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|arg
operator|.
name|contains
argument_list|(
name|tokenPlaceHolder
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|copyLocal
parameter_list|(
name|String
name|var
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|filenames
init|=
name|TempletonUtils
operator|.
name|decodeArray
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|var
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|filenames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|filename
range|:
name|filenames
control|)
block|{
name|Path
name|src
init|=
operator|new
name|Path
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|Path
name|dst
init|=
operator|new
name|Path
argument_list|(
name|src
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|src
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: copy "
operator|+
name|src
operator|+
literal|" => "
operator|+
name|dst
argument_list|)
expr_stmt|;
name|fs
operator|.
name|copyToLocalFile
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Process
name|proc
init|=
name|startJob
argument_list|(
name|context
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|OVERRIDE_CLASSPATH
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|statusdir
init|=
name|conf
operator|.
name|get
argument_list|(
name|STATUSDIR_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|statusdir
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|statusdir
operator|=
name|TempletonUtils
operator|.
name|addUserHomeDirectoryIfApplicable
argument_list|(
name|statusdir
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Invalid status dir URI"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|Boolean
name|enablelog
init|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ENABLE_LOG
argument_list|)
argument_list|)
decl_stmt|;
name|LauncherDelegator
operator|.
name|JobType
name|jobType
init|=
name|LauncherDelegator
operator|.
name|JobType
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|JOB_TYPE
argument_list|)
argument_list|)
decl_stmt|;
name|ExecutorService
name|pool
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
decl_stmt|;
name|executeWatcher
argument_list|(
name|pool
argument_list|,
name|conf
argument_list|,
name|context
operator|.
name|getJobID
argument_list|()
argument_list|,
name|proc
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|statusdir
argument_list|,
name|STDOUT_FNAME
argument_list|)
expr_stmt|;
name|executeWatcher
argument_list|(
name|pool
argument_list|,
name|conf
argument_list|,
name|context
operator|.
name|getJobID
argument_list|()
argument_list|,
name|proc
operator|.
name|getErrorStream
argument_list|()
argument_list|,
name|statusdir
argument_list|,
name|STDERR_FNAME
argument_list|)
expr_stmt|;
name|KeepAlive
name|keepAlive
init|=
name|startCounterKeepAlive
argument_list|(
name|pool
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|proc
operator|.
name|waitFor
argument_list|()
expr_stmt|;
name|keepAlive
operator|.
name|sendReport
operator|=
literal|false
expr_stmt|;
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|pool
operator|.
name|awaitTermination
argument_list|(
name|WATCHER_TIMEOUT_SECS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
name|writeExitValue
argument_list|(
name|conf
argument_list|,
name|proc
operator|.
name|exitValue
argument_list|()
argument_list|,
name|statusdir
argument_list|)
expr_stmt|;
name|JobState
name|state
init|=
operator|new
name|JobState
argument_list|(
name|context
operator|.
name|getJobID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|state
operator|.
name|setExitValue
argument_list|(
name|proc
operator|.
name|exitValue
argument_list|()
argument_list|)
expr_stmt|;
name|state
operator|.
name|setCompleteStatus
argument_list|(
literal|"done"
argument_list|)
expr_stmt|;
name|state
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|enablelog
operator|&&
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|statusdir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: collecting logs for "
operator|+
name|context
operator|.
name|getJobID
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" to "
operator|+
name|statusdir
operator|+
literal|"/logs"
argument_list|)
expr_stmt|;
name|LogRetriever
name|logRetriever
init|=
operator|new
name|LogRetriever
argument_list|(
name|statusdir
argument_list|,
name|jobType
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|logRetriever
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|.
name|exitValue
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: job failed with exit code "
operator|+
name|proc
operator|.
name|exitValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: job completed with exit code 0"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|executeWatcher
parameter_list|(
name|ExecutorService
name|pool
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|JobID
name|jobid
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Watcher
name|w
init|=
operator|new
name|Watcher
argument_list|(
name|conf
argument_list|,
name|jobid
argument_list|,
name|in
argument_list|,
name|statusdir
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|pool
operator|.
name|execute
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
specifier|private
name|KeepAlive
name|startCounterKeepAlive
parameter_list|(
name|ExecutorService
name|pool
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|KeepAlive
name|k
init|=
operator|new
name|KeepAlive
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|pool
operator|.
name|execute
argument_list|(
name|k
argument_list|)
expr_stmt|;
return|return
name|k
return|;
block|}
specifier|private
name|void
name|writeExitValue
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|exitValue
parameter_list|,
name|String
name|statusdir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|statusdir
argument_list|)
condition|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|statusdir
argument_list|,
name|EXIT_FNAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|OutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: Writing exit value "
operator|+
name|exitValue
operator|+
literal|" to "
operator|+
name|p
argument_list|)
expr_stmt|;
name|PrintWriter
name|writer
init|=
operator|new
name|PrintWriter
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|writer
operator|.
name|println
argument_list|(
name|exitValue
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Watcher
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|InputStream
name|in
decl_stmt|;
specifier|private
name|OutputStream
name|out
decl_stmt|;
specifier|private
specifier|final
name|JobID
name|jobid
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
name|boolean
name|needCloseOutput
init|=
literal|false
decl_stmt|;
specifier|public
name|Watcher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|JobID
name|jobid
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|String
name|name
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
name|this
operator|.
name|jobid
operator|=
name|jobid
expr_stmt|;
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|STDERR_FNAME
argument_list|)
condition|)
block|{
name|out
operator|=
name|System
operator|.
name|err
expr_stmt|;
block|}
else|else
block|{
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|statusdir
argument_list|)
condition|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|statusdir
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|out
operator|=
name|fs
operator|.
name|create
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|needCloseOutput
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"templeton: Writing status to "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|PrintWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|InputStreamReader
name|isr
init|=
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
name|isr
argument_list|)
decl_stmt|;
name|writer
operator|=
operator|new
name|PrintWriter
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|JobState
name|state
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|percent
init|=
name|TempletonUtils
operator|.
name|extractPercentComplete
argument_list|(
name|line
argument_list|)
decl_stmt|;
name|String
name|childid
init|=
name|TempletonUtils
operator|.
name|extractChildJobId
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|percent
operator|!=
literal|null
operator|||
name|childid
operator|!=
literal|null
condition|)
block|{
name|state
operator|=
operator|new
name|JobState
argument_list|(
name|jobid
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|percent
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|setPercentComplete
argument_list|(
name|percent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|childid
operator|!=
literal|null
condition|)
block|{
name|JobState
name|childState
init|=
operator|new
name|JobState
argument_list|(
name|childid
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|childState
operator|.
name|setParent
argument_list|(
name|jobid
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|state
operator|.
name|addChild
argument_list|(
name|childid
argument_list|)
expr_stmt|;
name|state
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
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
literal|"templeton: state error: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|state
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
name|LOG
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|writer
operator|.
name|flush
argument_list|()
expr_stmt|;
if|if
condition|(
name|out
operator|!=
name|System
operator|.
name|err
operator|&&
name|out
operator|!=
name|System
operator|.
name|out
condition|)
block|{
comment|//depending on FileSystem implementation flush() may or may not do anything
name|writer
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
name|LOG
operator|.
name|error
argument_list|(
literal|"templeton: execute error: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Need to close() because in some FileSystem
comment|// implementations flush() is no-op.
comment|// Close the file handle if it is a hdfs file.
comment|// But if it is stderr/stdout, skip it since
comment|// WebHCat is not supposed to close it
if|if
condition|(
name|needCloseOutput
operator|&&
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|KeepAlive
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|sendReport
decl_stmt|;
specifier|public
name|KeepAlive
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|this
operator|.
name|sendReport
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
specifier|private
specifier|static
name|StringBuilder
name|makeDots
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|sendReport
condition|)
block|{
comment|// Periodically report progress on the Context object
comment|// to prevent TaskTracker from killing the Templeton
comment|// Controller task
name|context
operator|.
name|progress
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
name|String
name|msg
init|=
literal|"KeepAlive Heart beat"
operator|+
name|makeDots
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|KEEP_ALIVE_MSEC
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Ok to be interrupted
block|}
block|}
block|}
block|}
end_class

end_unit

