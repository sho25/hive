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
name|BufferedReader
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
name|conf
operator|.
name|Configured
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
name|mapred
operator|.
name|JobClient
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|DelegationTokenIdentifier
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|Tool
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
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * A Map Reduce job that will start another job.  *  * We have a single Mapper job that starts a child MR job.  The parent  * monitors the child child job and ends when the child job exits.  In  * addition, we  *  * - write out the parent job id so the caller can record it.  * - run a keep alive thread so the job doesn't end.  * - Optionally, store the stdout, stderr, and exit value of the child  *   in hdfs files.  */
end_comment

begin_class
specifier|public
class|class
name|TempletonControllerJob
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|public
specifier|static
specifier|final
name|String
name|COPY_NAME
init|=
literal|"templeton.copy"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STATUSDIR_NAME
init|=
literal|"templeton.statusdir"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAR_ARGS_NAME
init|=
literal|"templeton.args"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERRIDE_CLASSPATH
init|=
literal|"templeton.override-classpath"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STDOUT_FNAME
init|=
literal|"stdout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STDERR_FNAME
init|=
literal|"stderr"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXIT_FNAME
init|=
literal|"exit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WATCHER_TIMEOUT_SECS
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|KEEP_ALIVE_MSEC
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TOKEN_FILE_ARG_PLACEHOLDER
init|=
literal|"__WEBHCAT_TOKEN_FILE_LOCATION__"
decl_stmt|;
specifier|private
specifier|static
name|TrivialExecService
name|execService
init|=
name|TrivialExecService
operator|.
name|getInstance
argument_list|()
decl_stmt|;
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
name|TempletonControllerJob
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
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
block|{
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
name|removeEnv
operator|.
name|add
argument_list|(
literal|"HADOOP_ROOT_LOGGER"
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
name|String
name|tokenArg
init|=
literal|"mapreduce.job.credentials.binary="
operator|+
name|tokenFile
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
name|TOKEN_FILE_ARG_PLACEHOLDER
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
name|TOKEN_FILE_ARG_PLACEHOLDER
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
return|return
name|execService
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
name|System
operator|.
name|err
operator|.
name|println
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
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
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
name|proc
operator|.
name|exitValue
argument_list|()
operator|!=
literal|0
condition|)
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"templeton: job failed with exit code "
operator|+
name|proc
operator|.
name|exitValue
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"templeton: job completed with exit code 0"
argument_list|)
expr_stmt|;
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
name|cnt
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
name|cnt
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
name|System
operator|.
name|err
operator|.
name|println
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
block|}
specifier|private
specifier|static
class|class
name|Watcher
implements|implements
name|Runnable
block|{
specifier|private
name|InputStream
name|in
decl_stmt|;
specifier|private
name|OutputStream
name|out
decl_stmt|;
specifier|private
name|JobID
name|jobid
decl_stmt|;
specifier|private
name|Configuration
name|conf
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
name|out
operator|=
name|System
operator|.
name|err
expr_stmt|;
else|else
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
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
name|System
operator|.
name|err
operator|.
name|println
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
name|PrintWriter
name|writer
init|=
operator|new
name|PrintWriter
argument_list|(
name|out
argument_list|)
decl_stmt|;
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
name|state
operator|.
name|setPercentComplete
argument_list|(
name|percent
argument_list|)
expr_stmt|;
name|state
operator|.
name|setChildId
argument_list|(
name|childid
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"templeton: state error: "
operator|+
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
block|{               }
block|}
block|}
block|}
name|writer
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"templeton: execute error: "
operator|+
name|e
argument_list|)
expr_stmt|;
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
name|Mapper
operator|.
name|Context
name|cnt
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|sendReport
decl_stmt|;
specifier|public
name|KeepAlive
parameter_list|(
name|Mapper
operator|.
name|Context
name|cnt
parameter_list|)
block|{
name|this
operator|.
name|cnt
operator|=
name|cnt
expr_stmt|;
name|this
operator|.
name|sendReport
operator|=
literal|true
expr_stmt|;
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
while|while
condition|(
name|sendReport
condition|)
block|{
name|cnt
operator|.
name|progress
argument_list|()
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
specifier|private
name|JobID
name|submittedJobId
decl_stmt|;
specifier|public
name|String
name|getSubmittedId
parameter_list|()
block|{
if|if
condition|(
name|submittedJobId
operator|==
literal|null
condition|)
return|return
literal|null
return|;
else|else
return|return
name|submittedJobId
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Enqueue the job and print out the job id for later collection.    */
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|JAR_ARGS_NAME
argument_list|,
name|TempletonUtils
operator|.
name|encodeArray
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"user.name"
argument_list|,
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|TempletonControllerJob
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJobName
argument_list|(
literal|"TempletonControllerJob"
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|LaunchMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|SingleInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|NullOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
name|of
init|=
operator|new
name|NullOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
argument_list|()
decl_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|of
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|JobClient
name|jc
init|=
operator|new
name|JobClient
argument_list|(
operator|new
name|JobConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|mrdt
init|=
name|jc
operator|.
name|getDelegationToken
argument_list|(
operator|new
name|Text
argument_list|(
literal|"mr token"
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
operator|new
name|Text
argument_list|(
literal|"mr token"
argument_list|)
argument_list|,
name|mrdt
argument_list|)
expr_stmt|;
name|job
operator|.
name|submit
argument_list|()
expr_stmt|;
name|submittedJobId
operator|=
name|job
operator|.
name|getJobID
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|TempletonControllerJob
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"TempletonControllerJob failed!"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

