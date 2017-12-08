begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|druid
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
name|base
operator|.
name|Strings
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
name|Lists
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
name|net
operator|.
name|URLClassLoader
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
specifier|public
class|class
name|ForkingDruidNode
extends|extends
name|DruidNode
block|{
specifier|private
specifier|final
specifier|static
name|String
name|DEFAULT_JAVA_CMD
init|=
literal|"java"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ForkingDruidNode
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|classpath
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|jvmArgs
decl_stmt|;
specifier|private
specifier|final
name|File
name|logLocation
decl_stmt|;
specifier|private
specifier|final
name|File
name|logFile
decl_stmt|;
specifier|private
specifier|final
name|String
name|javaCmd
decl_stmt|;
specifier|private
specifier|final
name|ProcessBuilder
name|processBuilder
init|=
operator|new
name|ProcessBuilder
argument_list|()
decl_stmt|;
specifier|private
name|Process
name|druidProcess
init|=
literal|null
decl_stmt|;
specifier|private
name|Boolean
name|started
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|allowedPrefixes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"com.metamx"
argument_list|,
literal|"druid"
argument_list|,
literal|"io.druid"
argument_list|,
literal|"java.io.tmpdir"
argument_list|,
literal|"hadoop"
argument_list|)
decl_stmt|;
specifier|public
name|ForkingDruidNode
parameter_list|(
name|String
name|nodeType
parameter_list|,
name|String
name|extraClasspath
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|jvmArgs
parameter_list|,
name|File
name|logLocation
parameter_list|,
name|String
name|javaCmd
parameter_list|)
block|{
name|super
argument_list|(
name|nodeType
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|command
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|this
operator|.
name|classpath
operator|=
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|extraClasspath
argument_list|)
condition|?
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.class.path"
argument_list|)
else|:
name|extraClasspath
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
operator|==
literal|null
condition|?
operator|new
name|HashMap
argument_list|<>
argument_list|()
else|:
name|properties
expr_stmt|;
name|this
operator|.
name|jvmArgs
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|jvmArgs
argument_list|)
expr_stmt|;
name|this
operator|.
name|logLocation
operator|=
name|logLocation
operator|==
literal|null
condition|?
operator|new
name|File
argument_list|(
literal|"/tmp/druid"
argument_list|)
else|:
name|logLocation
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|logLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
name|this
operator|.
name|logLocation
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|javaCmd
operator|=
name|javaCmd
operator|==
literal|null
condition|?
name|DEFAULT_JAVA_CMD
else|:
name|javaCmd
expr_stmt|;
name|logFile
operator|=
operator|new
name|File
argument_list|(
name|this
operator|.
name|logLocation
argument_list|,
name|getNodeType
argument_list|()
operator|+
literal|".log"
argument_list|)
expr_stmt|;
comment|// set the log stream
name|processBuilder
operator|.
name|redirectErrorStream
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|processBuilder
operator|.
name|redirectOutput
argument_list|(
name|ProcessBuilder
operator|.
name|Redirect
operator|.
name|appendTo
argument_list|(
name|logFile
argument_list|)
argument_list|)
expr_stmt|;
name|command
operator|.
name|add
argument_list|(
name|this
operator|.
name|javaCmd
argument_list|)
expr_stmt|;
name|command
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|jvmArgs
argument_list|)
expr_stmt|;
name|command
operator|.
name|add
argument_list|(
literal|"-server"
argument_list|)
expr_stmt|;
name|command
operator|.
name|add
argument_list|(
literal|"-cp"
argument_list|)
expr_stmt|;
name|command
operator|.
name|add
argument_list|(
name|classpath
argument_list|)
expr_stmt|;
comment|// inject properties from the main App that matches allowedPrefix
for|for
control|(
name|String
name|propName
range|:
name|System
operator|.
name|getProperties
argument_list|()
operator|.
name|stringPropertyNames
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|allowedPrefix
range|:
name|allowedPrefixes
control|)
block|{
if|if
condition|(
name|propName
operator|.
name|startsWith
argument_list|(
name|allowedPrefix
argument_list|)
condition|)
block|{
name|command
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"-D%s=%s"
argument_list|,
name|propName
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
name|propName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|this
operator|.
name|properties
operator|.
name|forEach
argument_list|(
parameter_list|(
name|key
parameter_list|,
name|value
parameter_list|)
lambda|->
name|command
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"-D%s=%s"
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|command
operator|.
name|addAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"io.druid.cli.Main"
argument_list|,
literal|"server"
argument_list|,
name|getNodeType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|processBuilder
operator|.
name|command
argument_list|(
name|command
argument_list|)
expr_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"Creating forking druid node with "
operator|+
name|String
operator|.
name|join
argument_list|(
literal|" "
argument_list|,
name|processBuilder
operator|.
name|command
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|started
init|)
block|{
if|if
condition|(
name|started
operator|==
literal|false
condition|)
block|{
name|druidProcess
operator|=
name|processBuilder
operator|.
name|start
argument_list|()
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|getNodeType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAlive
parameter_list|()
block|{
synchronized|synchronized
init|(
name|started
init|)
block|{
return|return
name|started
operator|&&
name|druidProcess
operator|!=
literal|null
operator|&&
name|druidProcess
operator|.
name|isAlive
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|started
init|)
block|{
if|if
condition|(
name|druidProcess
operator|!=
literal|null
operator|&&
name|druidProcess
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|druidProcess
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|log
operator|.
name|info
argument_list|(
literal|"Waiting for "
operator|+
name|getNodeType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|druidProcess
operator|.
name|waitFor
argument_list|(
literal|5000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
block|{
name|log
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Shutdown completed for node [%s]"
argument_list|,
name|getNodeType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|log
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Waiting to shutdown node [%s] exhausted shutting down forcibly"
argument_list|,
name|getNodeType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|druidProcess
operator|.
name|destroyForcibly
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

