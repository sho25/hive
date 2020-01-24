begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|dbinstall
operator|.
name|rules
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
name|InputStreamReader
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
name|List
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
name|lang3
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
name|hive
operator|.
name|metastore
operator|.
name|tools
operator|.
name|schematool
operator|.
name|MetastoreSchemaTool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExternalResource
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

begin_comment
comment|/**  * Abstract JUnit TestRule for different RDMBS types.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DatabaseRule
extends|extends
name|ExternalResource
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
name|DatabaseRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|HIVE_USER
init|=
literal|"hiveuser"
decl_stmt|;
comment|// used in most of the RDBMS configs, except MSSQL
specifier|protected
specifier|static
specifier|final
name|String
name|HIVE_PASSWORD
init|=
literal|"hivepassword"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|HIVE_DB
init|=
literal|"hivedb"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_STARTUP_WAIT
init|=
literal|5
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|public
specifier|abstract
name|String
name|getHivePassword
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getDockerImageName
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
index|[]
name|getDockerAdditionalArgs
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getDbType
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getDbRootUser
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getDbRootPassword
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getJdbcDriver
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|String
name|getJdbcUrl
parameter_list|()
function_decl|;
specifier|private
name|boolean
name|verbose
decl_stmt|;
specifier|public
name|DatabaseRule
name|setVerbose
parameter_list|(
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
return|return
name|this
return|;
block|}
empty_stmt|;
specifier|public
name|String
name|getDb
parameter_list|()
block|{
return|return
name|HIVE_DB
return|;
block|}
empty_stmt|;
comment|/**    * URL to use when connecting as root rather than Hive    *    * @return URL    */
specifier|public
specifier|abstract
name|String
name|getInitialJdbcUrl
parameter_list|()
function_decl|;
comment|/**    * Determine if the docker container is ready to use.    *    * @param logOutput output of docker logs command    * @return true if ready, false otherwise    */
specifier|public
specifier|abstract
name|boolean
name|isContainerReady
parameter_list|(
name|String
name|logOutput
parameter_list|)
function_decl|;
specifier|protected
name|String
index|[]
name|buildArray
parameter_list|(
name|String
modifier|...
name|strs
parameter_list|)
block|{
return|return
name|strs
return|;
block|}
specifier|private
specifier|static
class|class
name|ProcessResults
block|{
specifier|final
name|String
name|stdout
decl_stmt|;
specifier|final
name|String
name|stderr
decl_stmt|;
specifier|final
name|int
name|rc
decl_stmt|;
specifier|public
name|ProcessResults
parameter_list|(
name|String
name|stdout
parameter_list|,
name|String
name|stderr
parameter_list|,
name|int
name|rc
parameter_list|)
block|{
name|this
operator|.
name|stdout
operator|=
name|stdout
expr_stmt|;
name|this
operator|.
name|stderr
operator|=
name|stderr
expr_stmt|;
name|this
operator|.
name|rc
operator|=
name|rc
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
comment|//runDockerContainer
if|if
condition|(
name|runCmdAndPrintStreams
argument_list|(
name|buildRunCmd
argument_list|()
argument_list|,
literal|600
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to start docker container"
argument_list|)
throw|;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ProcessResults
name|pr
decl_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|pr
operator|=
name|runCmd
argument_list|(
name|buildLogCmd
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
if|if
condition|(
name|pr
operator|.
name|rc
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to get docker logs"
argument_list|)
throw|;
block|}
block|}
do|while
condition|(
name|startTime
operator|+
name|MAX_STARTUP_WAIT
operator|>=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|&&
operator|!
name|isContainerReady
argument_list|(
name|pr
operator|.
name|stdout
argument_list|)
condition|)
do|;
if|if
condition|(
name|startTime
operator|+
name|MAX_STARTUP_WAIT
operator|<
name|System
operator|.
name|currentTimeMillis
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Container failed to be ready in "
operator|+
name|MAX_STARTUP_WAIT
operator|/
literal|1000
operator|+
literal|" seconds"
argument_list|)
throw|;
block|}
name|MetastoreSchemaTool
operator|.
name|setHomeDirForTesting
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|after
parameter_list|()
block|{
comment|// stopAndRmDockerContainer
if|if
condition|(
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"metastore.itest.no.stop.container"
argument_list|)
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not stopping container "
operator|+
name|getDockerContainerName
argument_list|()
operator|+
literal|" at user request, please "
operator|+
literal|"be sure to shut it down before rerunning the test."
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
if|if
condition|(
name|runCmdAndPrintStreams
argument_list|(
name|buildStopCmd
argument_list|()
argument_list|,
literal|60
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to stop docker container"
argument_list|)
throw|;
block|}
if|if
condition|(
name|runCmdAndPrintStreams
argument_list|(
name|buildRmCmd
argument_list|()
argument_list|,
literal|15
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to remove docker container"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|getDockerContainerName
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"metastore-test-%s-install"
argument_list|,
name|getDbType
argument_list|()
argument_list|)
return|;
block|}
empty_stmt|;
specifier|private
name|ProcessResults
name|runCmd
parameter_list|(
name|String
index|[]
name|cmd
parameter_list|,
name|long
name|secondsToWait
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to run: "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|cmd
argument_list|,
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|Process
name|proc
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|proc
operator|.
name|waitFor
argument_list|(
name|secondsToWait
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Process "
operator|+
name|cmd
index|[
literal|0
index|]
operator|+
literal|" failed to run in "
operator|+
name|secondsToWait
operator|+
literal|" seconds"
argument_list|)
throw|;
block|}
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|proc
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|StringBuilder
name|lines
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|reader
operator|.
name|lines
argument_list|()
operator|.
name|forEach
argument_list|(
name|s
lambda|->
name|lines
operator|.
name|append
argument_list|(
name|s
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|proc
operator|.
name|getErrorStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|StringBuilder
name|errLines
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|reader
operator|.
name|lines
argument_list|()
operator|.
name|forEach
argument_list|(
name|s
lambda|->
name|errLines
operator|.
name|append
argument_list|(
name|s
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ProcessResults
argument_list|(
name|lines
operator|.
name|toString
argument_list|()
argument_list|,
name|errLines
operator|.
name|toString
argument_list|()
argument_list|,
name|proc
operator|.
name|exitValue
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|int
name|runCmdAndPrintStreams
parameter_list|(
name|String
index|[]
name|cmd
parameter_list|,
name|long
name|secondsToWait
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|ProcessResults
name|results
init|=
name|runCmd
argument_list|(
name|cmd
argument_list|,
name|secondsToWait
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stdout from proc: "
operator|+
name|results
operator|.
name|stdout
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stderr from proc: "
operator|+
name|results
operator|.
name|stderr
argument_list|)
expr_stmt|;
return|return
name|results
operator|.
name|rc
return|;
block|}
specifier|private
name|String
index|[]
name|buildRunCmd
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cmd
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|4
operator|+
name|getDockerAdditionalArgs
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|cmd
operator|.
name|add
argument_list|(
literal|"docker"
argument_list|)
expr_stmt|;
name|cmd
operator|.
name|add
argument_list|(
literal|"run"
argument_list|)
expr_stmt|;
name|cmd
operator|.
name|add
argument_list|(
literal|"--name"
argument_list|)
expr_stmt|;
name|cmd
operator|.
name|add
argument_list|(
name|getDockerContainerName
argument_list|()
argument_list|)
expr_stmt|;
name|cmd
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|getDockerAdditionalArgs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|cmd
operator|.
name|add
argument_list|(
name|getDockerImageName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cmd
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|cmd
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|private
name|String
index|[]
name|buildStopCmd
parameter_list|()
block|{
return|return
name|buildArray
argument_list|(
literal|"docker"
argument_list|,
literal|"stop"
argument_list|,
name|getDockerContainerName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|String
index|[]
name|buildRmCmd
parameter_list|()
block|{
return|return
name|buildArray
argument_list|(
literal|"docker"
argument_list|,
literal|"rm"
argument_list|,
name|getDockerContainerName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|String
index|[]
name|buildLogCmd
parameter_list|()
block|{
return|return
name|buildArray
argument_list|(
literal|"docker"
argument_list|,
literal|"logs"
argument_list|,
name|getDockerContainerName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|String
name|getHiveUser
parameter_list|()
block|{
return|return
name|HIVE_USER
return|;
block|}
specifier|public
name|int
name|createUser
parameter_list|()
block|{
return|return
operator|new
name|MetastoreSchemaTool
argument_list|()
operator|.
name|setVerbose
argument_list|(
name|verbose
argument_list|)
operator|.
name|run
argument_list|(
name|buildArray
argument_list|(
literal|"-createUser"
argument_list|,
literal|"-dbType"
argument_list|,
name|getDbType
argument_list|()
argument_list|,
literal|"-userName"
argument_list|,
name|getDbRootUser
argument_list|()
argument_list|,
literal|"-passWord"
argument_list|,
name|getDbRootPassword
argument_list|()
argument_list|,
literal|"-hiveUser"
argument_list|,
name|getHiveUser
argument_list|()
argument_list|,
literal|"-hivePassword"
argument_list|,
name|getHivePassword
argument_list|()
argument_list|,
literal|"-hiveDb"
argument_list|,
name|getDb
argument_list|()
argument_list|,
literal|"-url"
argument_list|,
name|getInitialJdbcUrl
argument_list|()
argument_list|,
literal|"-driver"
argument_list|,
name|getJdbcDriver
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|installLatest
parameter_list|()
block|{
return|return
operator|new
name|MetastoreSchemaTool
argument_list|()
operator|.
name|setVerbose
argument_list|(
name|verbose
argument_list|)
operator|.
name|run
argument_list|(
name|buildArray
argument_list|(
literal|"-initSchema"
argument_list|,
literal|"-dbType"
argument_list|,
name|getDbType
argument_list|()
argument_list|,
literal|"-userName"
argument_list|,
name|getHiveUser
argument_list|()
argument_list|,
literal|"-passWord"
argument_list|,
name|getHivePassword
argument_list|()
argument_list|,
literal|"-url"
argument_list|,
name|getJdbcUrl
argument_list|()
argument_list|,
literal|"-driver"
argument_list|,
name|getJdbcDriver
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|installAVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
return|return
operator|new
name|MetastoreSchemaTool
argument_list|()
operator|.
name|setVerbose
argument_list|(
name|verbose
argument_list|)
operator|.
name|run
argument_list|(
name|buildArray
argument_list|(
literal|"-initSchemaTo"
argument_list|,
name|version
argument_list|,
literal|"-dbType"
argument_list|,
name|getDbType
argument_list|()
argument_list|,
literal|"-userName"
argument_list|,
name|getHiveUser
argument_list|()
argument_list|,
literal|"-passWord"
argument_list|,
name|getHivePassword
argument_list|()
argument_list|,
literal|"-url"
argument_list|,
name|getJdbcUrl
argument_list|()
argument_list|,
literal|"-driver"
argument_list|,
name|getJdbcDriver
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|upgradeToLatest
parameter_list|()
block|{
return|return
operator|new
name|MetastoreSchemaTool
argument_list|()
operator|.
name|setVerbose
argument_list|(
name|verbose
argument_list|)
operator|.
name|run
argument_list|(
name|buildArray
argument_list|(
literal|"-upgradeSchema"
argument_list|,
literal|"-dbType"
argument_list|,
name|getDbType
argument_list|()
argument_list|,
literal|"-userName"
argument_list|,
name|HIVE_USER
argument_list|,
literal|"-passWord"
argument_list|,
name|getHivePassword
argument_list|()
argument_list|,
literal|"-url"
argument_list|,
name|getJdbcUrl
argument_list|()
argument_list|,
literal|"-driver"
argument_list|,
name|getJdbcDriver
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|install
parameter_list|()
block|{
name|createUser
argument_list|()
expr_stmt|;
name|installLatest
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

