begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

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
name|FileOutputStream
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
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
name|Writer
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
name|Properties
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|akka
operator|.
name|actor
operator|.
name|ActorRef
import|;
end_import

begin_import
import|import
name|akka
operator|.
name|actor
operator|.
name|ActorSelection
import|;
end_import

begin_import
import|import
name|akka
operator|.
name|actor
operator|.
name|Props
import|;
end_import

begin_import
import|import
name|akka
operator|.
name|actor
operator|.
name|UntypedActor
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
name|Charsets
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
name|Joiner
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
name|Optional
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
name|Lists
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
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkContext
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
name|SparkException
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

begin_class
class|class
name|SparkClientImpl
implements|implements
name|SparkClient
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkClientImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_CONNECTION_TIMEOUT
init|=
literal|"60"
decl_stmt|;
comment|// In seconds
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|childIdGenerator
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|ActorRef
name|clientRef
decl_stmt|;
specifier|private
specifier|final
name|Thread
name|driverThread
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|JobHandleImpl
argument_list|<
name|?
argument_list|>
argument_list|>
name|jobs
decl_stmt|;
specifier|private
specifier|volatile
name|ActorSelection
name|remoteRef
decl_stmt|;
name|SparkClientImpl
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SparkException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|childIdGenerator
operator|=
operator|new
name|AtomicInteger
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
literal|"SparkClient-"
operator|+
name|ClientUtils
operator|.
name|randomName
argument_list|()
expr_stmt|;
name|this
operator|.
name|clientRef
operator|=
name|bind
argument_list|(
name|Props
operator|.
name|create
argument_list|(
name|ClientActor
operator|.
name|class
argument_list|,
name|this
argument_list|)
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|jobs
operator|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|driverThread
operator|=
name|startDriver
argument_list|()
expr_stmt|;
name|long
name|connectTimeout
init|=
literal|1000
operator|*
name|Integer
operator|.
name|parseInt
argument_list|(
name|Optional
operator|.
name|fromNullable
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"spark.client.connectTimeout"
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|DEFAULT_CONNECTION_TIMEOUT
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|connectTimeout
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
while|while
condition|(
name|remoteRef
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|wait
argument_list|(
name|connectTimeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|SparkException
argument_list|(
literal|"Interrupted."
argument_list|,
name|ie
argument_list|)
throw|;
block|}
name|connectTimeout
operator|=
name|endTime
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|remoteRef
operator|==
literal|null
operator|&&
name|connectTimeout
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|SparkException
argument_list|(
literal|"Timed out waiting for remote driver to connect."
argument_list|)
throw|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|JobHandle
argument_list|<
name|T
argument_list|>
name|submit
parameter_list|(
name|Job
argument_list|<
name|T
argument_list|>
name|job
parameter_list|)
block|{
name|String
name|jobId
init|=
name|ClientUtils
operator|.
name|randomName
argument_list|()
decl_stmt|;
name|remoteRef
operator|.
name|tell
argument_list|(
operator|new
name|Protocol
operator|.
name|JobRequest
argument_list|(
name|jobId
argument_list|,
name|job
argument_list|)
argument_list|,
name|clientRef
argument_list|)
expr_stmt|;
name|JobHandleImpl
argument_list|<
name|T
argument_list|>
name|handle
init|=
operator|new
name|JobHandleImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|this
argument_list|,
name|jobId
argument_list|)
decl_stmt|;
name|jobs
operator|.
name|put
argument_list|(
name|jobId
argument_list|,
name|handle
argument_list|)
expr_stmt|;
return|return
name|handle
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|remoteRef
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sending EndSession to remote actor."
argument_list|)
expr_stmt|;
name|remoteRef
operator|.
name|tell
argument_list|(
operator|new
name|Protocol
operator|.
name|EndSession
argument_list|()
argument_list|,
name|clientRef
argument_list|)
expr_stmt|;
block|}
name|unbind
argument_list|(
name|clientRef
argument_list|)
expr_stmt|;
try|try
block|{
name|driverThread
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// TODO: timeout?
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupted before driver thread was finished."
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Future
argument_list|<
name|?
argument_list|>
name|addJar
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
return|return
name|submit
argument_list|(
operator|new
name|AddJarJob
argument_list|(
name|url
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Future
argument_list|<
name|?
argument_list|>
name|addFile
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
return|return
name|submit
argument_list|(
operator|new
name|AddFileJob
argument_list|(
name|url
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Future
argument_list|<
name|Integer
argument_list|>
name|getExecutorCount
parameter_list|()
block|{
return|return
name|submit
argument_list|(
operator|new
name|GetExecutorCountJob
argument_list|()
argument_list|)
return|;
block|}
name|void
name|cancel
parameter_list|(
name|String
name|jobId
parameter_list|)
block|{
name|remoteRef
operator|.
name|tell
argument_list|(
operator|new
name|Protocol
operator|.
name|CancelJob
argument_list|(
name|jobId
argument_list|)
argument_list|,
name|clientRef
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Thread
name|startDriver
parameter_list|()
throws|throws
name|IOException
block|{
name|Runnable
name|runnable
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|containsKey
argument_list|(
name|ClientUtils
operator|.
name|CONF_KEY_IN_PROCESS
argument_list|)
condition|)
block|{
comment|// Mostly for testing things quickly. Do not do this in production.
name|LOG
operator|.
name|warn
argument_list|(
literal|"!!!! Running remote driver in-process. !!!!"
argument_list|)
expr_stmt|;
name|runnable
operator|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"--remote"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s/%s"
argument_list|,
name|SparkClientFactory
operator|.
name|akkaUrl
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"--secret"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|SparkClientFactory
operator|.
name|secret
argument_list|)
expr_stmt|;
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
name|e
range|:
name|conf
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"--conf"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s=%s"
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RemoteDriver
operator|.
name|main
argument_list|(
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error running driver."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
comment|// Create a file with all the job properties to be read by spark-submit. Change the
comment|// file's permissions so that only the owner can read it. This avoid having the
comment|// connection secret show up in the child process's command line.
name|File
name|properties
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"spark-submit."
argument_list|,
literal|".properties"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|properties
operator|.
name|setReadable
argument_list|(
literal|false
argument_list|)
operator|||
operator|!
name|properties
operator|.
name|setReadable
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot change permissions of job properties file."
argument_list|)
throw|;
block|}
name|Properties
name|allProps
init|=
operator|new
name|Properties
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
name|e
range|:
name|conf
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|allProps
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|allProps
operator|.
name|put
argument_list|(
name|ClientUtils
operator|.
name|CONF_KEY_SECRET
argument_list|,
name|SparkClientFactory
operator|.
name|secret
argument_list|)
expr_stmt|;
name|Writer
name|writer
init|=
operator|new
name|OutputStreamWriter
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|properties
argument_list|)
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
try|try
block|{
name|allProps
operator|.
name|store
argument_list|(
name|writer
argument_list|,
literal|"Spark Context configuration"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Define how to pass options to the child process. If launching in client (or local)
comment|// mode, the driver options need to be passed directly on the command line. Otherwise,
comment|// SparkSubmit will take care of that for us.
name|String
name|master
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|master
operator|!=
literal|null
argument_list|,
literal|"spark.master is not defined."
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|argv
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// If a Spark installation is provided, use the spark-submit script. Otherwise, call the
comment|// SparkSubmit class directly, which has some caveats (like having to provide a proper
comment|// version of Guava on the classpath depending on the deploy mode).
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
literal|"spark.home"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|argv
operator|.
name|add
argument_list|(
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"spark.home"
argument_list|)
argument_list|,
literal|"bin/spark-submit"
argument_list|)
operator|.
name|getAbsolutePath
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
literal|"No spark.home provided, calling SparkSubmit directly."
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.home"
argument_list|)
argument_list|,
literal|"bin/java"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|master
operator|.
name|startsWith
argument_list|(
literal|"local"
argument_list|)
operator|||
name|master
operator|.
name|startsWith
argument_list|(
literal|"mesos"
argument_list|)
operator|||
name|master
operator|.
name|endsWith
argument_list|(
literal|"-client"
argument_list|)
operator|||
name|master
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
name|String
name|mem
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.driver.memory"
argument_list|)
decl_stmt|;
if|if
condition|(
name|mem
operator|!=
literal|null
condition|)
block|{
name|argv
operator|.
name|add
argument_list|(
literal|"-Xms"
operator|+
name|mem
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
literal|"-Xmx"
operator|+
name|mem
argument_list|)
expr_stmt|;
block|}
name|String
name|cp
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.driver.extraClassPath"
argument_list|)
decl_stmt|;
if|if
condition|(
name|cp
operator|!=
literal|null
condition|)
block|{
name|argv
operator|.
name|add
argument_list|(
literal|"-classpath"
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|cp
argument_list|)
expr_stmt|;
block|}
name|String
name|libPath
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.driver.extraLibPath"
argument_list|)
decl_stmt|;
if|if
condition|(
name|libPath
operator|!=
literal|null
condition|)
block|{
name|argv
operator|.
name|add
argument_list|(
literal|"-Djava.library.path="
operator|+
name|libPath
argument_list|)
expr_stmt|;
block|}
name|String
name|extra
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.driver.extraJavaOptions"
argument_list|)
decl_stmt|;
if|if
condition|(
name|extra
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|opt
range|:
name|extra
operator|.
name|split
argument_list|(
literal|"[ ]"
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|opt
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|argv
operator|.
name|add
argument_list|(
name|opt
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|argv
operator|.
name|add
argument_list|(
literal|"org.apache.spark.deploy.SparkSubmit"
argument_list|)
expr_stmt|;
block|}
name|argv
operator|.
name|add
argument_list|(
literal|"--properties-file"
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|properties
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
literal|"--class"
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|RemoteDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|jar
init|=
literal|"spark-internal"
decl_stmt|;
if|if
condition|(
name|SparkContext
operator|.
name|jarOfClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|isDefined
argument_list|()
condition|)
block|{
name|jar
operator|=
name|SparkContext
operator|.
name|jarOfClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|argv
operator|.
name|add
argument_list|(
name|jar
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
literal|"--remote"
argument_list|)
expr_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s/%s"
argument_list|,
name|SparkClientFactory
operator|.
name|akkaUrl
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running client driver with argv: {}"
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|" "
argument_list|)
operator|.
name|join
argument_list|(
name|argv
argument_list|)
argument_list|)
expr_stmt|;
name|ProcessBuilder
name|pb
init|=
operator|new
name|ProcessBuilder
argument_list|(
name|argv
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|argv
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|pb
operator|.
name|environment
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
specifier|final
name|Process
name|child
init|=
name|pb
operator|.
name|start
argument_list|()
decl_stmt|;
name|int
name|childId
init|=
name|childIdGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|redirect
argument_list|(
literal|"stdout-redir-"
operator|+
name|childId
argument_list|,
name|child
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|System
operator|.
name|out
argument_list|)
expr_stmt|;
name|redirect
argument_list|(
literal|"stderr-redir-"
operator|+
name|childId
argument_list|,
name|child
operator|.
name|getErrorStream
argument_list|()
argument_list|,
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
name|runnable
operator|=
operator|new
name|Runnable
argument_list|()
block|{
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
name|exitCode
init|=
name|child
operator|.
name|waitFor
argument_list|()
decl_stmt|;
if|if
condition|(
name|exitCode
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Child process exited with code {}."
argument_list|,
name|exitCode
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while waiting for child process."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
block|}
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
name|runnable
argument_list|)
decl_stmt|;
name|thread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|thread
operator|.
name|setName
argument_list|(
literal|"Driver"
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|thread
return|;
block|}
specifier|private
name|void
name|redirect
parameter_list|(
name|String
name|name
parameter_list|,
name|InputStream
name|in
parameter_list|,
name|OutputStream
name|out
parameter_list|)
block|{
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Redirector
argument_list|(
name|in
argument_list|,
name|out
argument_list|)
argument_list|)
decl_stmt|;
name|thread
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|thread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ActorRef
name|bind
parameter_list|(
name|Props
name|props
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|SparkClientFactory
operator|.
name|actorSystem
operator|.
name|actorOf
argument_list|(
name|props
argument_list|,
name|name
argument_list|)
return|;
block|}
specifier|private
name|void
name|unbind
parameter_list|(
name|ActorRef
name|actor
parameter_list|)
block|{
name|SparkClientFactory
operator|.
name|actorSystem
operator|.
name|stop
argument_list|(
name|actor
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ActorSelection
name|select
parameter_list|(
name|String
name|url
parameter_list|)
block|{
return|return
name|SparkClientFactory
operator|.
name|actorSystem
operator|.
name|actorSelection
argument_list|(
name|url
argument_list|)
return|;
block|}
specifier|private
class|class
name|ClientActor
extends|extends
name|UntypedActor
block|{
annotation|@
name|Override
specifier|public
name|void
name|onReceive
parameter_list|(
name|Object
name|message
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|message
operator|instanceof
name|Protocol
operator|.
name|Error
condition|)
block|{
name|Protocol
operator|.
name|Error
name|e
init|=
operator|(
name|Protocol
operator|.
name|Error
operator|)
name|message
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Error report from remote driver."
argument_list|,
name|e
operator|.
name|cause
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|message
operator|instanceof
name|Protocol
operator|.
name|Hello
condition|)
block|{
name|Protocol
operator|.
name|Hello
name|hello
init|=
operator|(
name|Protocol
operator|.
name|Hello
operator|)
name|message
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Received hello from {}"
argument_list|,
name|hello
operator|.
name|remoteUrl
argument_list|)
expr_stmt|;
name|remoteRef
operator|=
name|select
argument_list|(
name|hello
operator|.
name|remoteUrl
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|SparkClientImpl
operator|.
name|this
init|)
block|{
name|SparkClientImpl
operator|.
name|this
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|message
operator|instanceof
name|Protocol
operator|.
name|JobMetrics
condition|)
block|{
name|Protocol
operator|.
name|JobMetrics
name|jm
init|=
operator|(
name|Protocol
operator|.
name|JobMetrics
operator|)
name|message
decl_stmt|;
name|JobHandleImpl
argument_list|<
name|?
argument_list|>
name|handle
init|=
name|jobs
operator|.
name|get
argument_list|(
name|jm
operator|.
name|jobId
argument_list|)
decl_stmt|;
if|if
condition|(
name|handle
operator|!=
literal|null
condition|)
block|{
name|handle
operator|.
name|getMetrics
argument_list|()
operator|.
name|addMetrics
argument_list|(
name|jm
operator|.
name|sparkJobId
argument_list|,
name|jm
operator|.
name|stageId
argument_list|,
name|jm
operator|.
name|taskId
argument_list|,
name|jm
operator|.
name|metrics
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received metrics for unknown job {}"
argument_list|,
name|jm
operator|.
name|jobId
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|message
operator|instanceof
name|Protocol
operator|.
name|JobResult
condition|)
block|{
name|Protocol
operator|.
name|JobResult
name|jr
init|=
operator|(
name|Protocol
operator|.
name|JobResult
operator|)
name|message
decl_stmt|;
name|JobHandleImpl
argument_list|<
name|?
argument_list|>
name|handle
init|=
name|jobs
operator|.
name|remove
argument_list|(
name|jr
operator|.
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|handle
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received result for {}"
argument_list|,
name|jr
operator|.
name|id
argument_list|)
expr_stmt|;
name|handle
operator|.
name|setSparkCounters
argument_list|(
name|jr
operator|.
name|sparkCounters
argument_list|)
expr_stmt|;
name|handle
operator|.
name|complete
argument_list|(
name|jr
operator|.
name|result
argument_list|,
name|jr
operator|.
name|error
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received result for unknown job {}"
argument_list|,
name|jr
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|message
operator|instanceof
name|Protocol
operator|.
name|JobSubmitted
condition|)
block|{
name|Protocol
operator|.
name|JobSubmitted
name|jobSubmitted
init|=
operator|(
name|Protocol
operator|.
name|JobSubmitted
operator|)
name|message
decl_stmt|;
name|JobHandleImpl
argument_list|<
name|?
argument_list|>
name|handle
init|=
name|jobs
operator|.
name|get
argument_list|(
name|jobSubmitted
operator|.
name|clientJobId
argument_list|)
decl_stmt|;
if|if
condition|(
name|handle
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received spark job ID: {} for {}"
argument_list|,
name|jobSubmitted
operator|.
name|sparkJobId
argument_list|,
name|jobSubmitted
operator|.
name|clientJobId
argument_list|)
expr_stmt|;
name|handle
operator|.
name|getSparkJobIds
argument_list|()
operator|.
name|add
argument_list|(
name|jobSubmitted
operator|.
name|sparkJobId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received spark job ID: {} for unknown job {}"
argument_list|,
name|jobSubmitted
operator|.
name|sparkJobId
argument_list|,
name|jobSubmitted
operator|.
name|clientJobId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
class|class
name|Redirector
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|InputStream
name|in
decl_stmt|;
specifier|private
specifier|final
name|OutputStream
name|out
decl_stmt|;
name|Redirector
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|OutputStream
name|out
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
name|this
operator|.
name|out
operator|=
name|out
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
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|read
argument_list|(
name|buf
argument_list|)
decl_stmt|;
while|while
condition|(
name|len
operator|!=
operator|-
literal|1
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|len
operator|=
name|in
operator|.
name|read
argument_list|(
name|buf
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error in redirector thread."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|AddJarJob
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
name|String
name|path
decl_stmt|;
name|AddJarJob
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
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
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|addJar
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|AddFileJob
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
name|String
name|path
decl_stmt|;
name|AddFileJob
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
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
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|addFile
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|GetExecutorCountJob
implements|implements
name|Job
argument_list|<
name|Integer
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
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|count
init|=
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|sc
argument_list|()
operator|.
name|getExecutorMemoryStatus
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|count
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

