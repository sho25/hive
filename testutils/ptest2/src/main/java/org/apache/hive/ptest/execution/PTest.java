begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
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
name|Collections
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|ConcurrentHashMap
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
name|CopyOnWriteArrayList
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
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|CommandLineParser
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
name|cli
operator|.
name|GnuParser
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
name|cli
operator|.
name|Options
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|ExecutionContextConfiguration
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|Host
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|TestConfiguration
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|TestParser
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
name|ptest
operator|.
name|execution
operator|.
name|context
operator|.
name|ExecutionContext
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
name|ptest
operator|.
name|execution
operator|.
name|context
operator|.
name|ExecutionContextProvider
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
name|ptest
operator|.
name|execution
operator|.
name|ssh
operator|.
name|RSyncCommandExecutor
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
name|ptest
operator|.
name|execution
operator|.
name|ssh
operator|.
name|SSHCommandExecutor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|app
operator|.
name|Velocity
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Files
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
name|io
operator|.
name|Resources
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
name|util
operator|.
name|concurrent
operator|.
name|ListeningExecutorService
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
name|util
operator|.
name|concurrent
operator|.
name|MoreExecutors
import|;
end_import

begin_class
specifier|public
class|class
name|PTest
block|{
static|static
block|{
name|Velocity
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
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
name|PTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TestConfiguration
name|mConfiguration
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|mExecutor
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|mExecutedTests
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|mFailedTests
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Phase
argument_list|>
name|mPhases
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContext
name|mExecutionContext
decl_stmt|;
specifier|private
specifier|final
name|Logger
name|mLogger
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|mHostExecutors
decl_stmt|;
specifier|private
specifier|final
name|String
name|mBuildTag
decl_stmt|;
specifier|public
name|PTest
parameter_list|(
specifier|final
name|TestConfiguration
name|configuration
parameter_list|,
specifier|final
name|ExecutionContext
name|executionContext
parameter_list|,
specifier|final
name|String
name|buildTag
parameter_list|,
specifier|final
name|File
name|logDir
parameter_list|,
specifier|final
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
specifier|final
name|SSHCommandExecutor
name|sshCommandExecutor
parameter_list|,
specifier|final
name|RSyncCommandExecutor
name|rsyncCommandExecutor
parameter_list|,
specifier|final
name|Logger
name|logger
parameter_list|)
throws|throws
name|Exception
block|{
name|mConfiguration
operator|=
name|configuration
expr_stmt|;
name|mLogger
operator|=
name|logger
expr_stmt|;
name|mBuildTag
operator|=
name|buildTag
expr_stmt|;
name|mExecutedTests
operator|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|mFailedTests
operator|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|mExecutionContext
operator|=
name|executionContext
expr_stmt|;
name|mExecutor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|File
name|failedLogDir
init|=
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|logDir
argument_list|,
literal|"failed"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|File
name|succeededLogDir
init|=
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|logDir
argument_list|,
literal|"succeeded"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|File
name|scratchDir
init|=
name|Dirs
operator|.
name|createEmpty
argument_list|(
operator|new
name|File
argument_list|(
name|mExecutionContext
operator|.
name|getLocalWorkingDirectory
argument_list|()
argument_list|,
literal|"scratch"
argument_list|)
argument_list|)
decl_stmt|;
name|File
name|patchDir
init|=
name|Dirs
operator|.
name|createEmpty
argument_list|(
operator|new
name|File
argument_list|(
name|logDir
argument_list|,
literal|"patches"
argument_list|)
argument_list|)
decl_stmt|;
name|File
name|patchFile
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|configuration
operator|.
name|getPatch
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|patchFile
operator|=
operator|new
name|File
argument_list|(
name|patchDir
argument_list|,
name|buildTag
operator|+
literal|".patch"
argument_list|)
expr_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|Resources
operator|.
name|toByteArray
argument_list|(
operator|new
name|URL
argument_list|(
name|configuration
operator|.
name|getPatch
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|patchFile
argument_list|)
expr_stmt|;
block|}
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaultsBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|templateDefaultsBuilder
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
name|configuration
operator|.
name|getRepository
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
name|configuration
operator|.
name|getRepositoryName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"repositoryType"
argument_list|,
name|configuration
operator|.
name|getRepositoryType
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
name|configuration
operator|.
name|getBranch
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|configuration
operator|.
name|isClearLibraryCache
argument_list|()
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
name|mExecutionContext
operator|.
name|getLocalWorkingDirectory
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
name|configuration
operator|.
name|getAntArgs
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"antTestArgs"
argument_list|,
name|configuration
operator|.
name|getAntTestArgs
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
name|buildTag
argument_list|)
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
name|logDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
name|configuration
operator|.
name|getJavaHome
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"javaHomeForTests"
argument_list|,
name|configuration
operator|.
name|getJavaHomeForTests
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
name|configuration
operator|.
name|getAntEnvOpts
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"antTestTarget"
argument_list|,
name|configuration
operator|.
name|getAntTestTarget
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
init|=
name|templateDefaultsBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|TestParser
name|testParser
init|=
operator|new
name|TestParser
argument_list|(
name|configuration
operator|.
name|getContext
argument_list|()
argument_list|,
operator|new
name|File
argument_list|(
name|mExecutionContext
operator|.
name|getLocalWorkingDirectory
argument_list|()
argument_list|,
name|configuration
operator|.
name|getRepositoryName
argument_list|()
operator|+
literal|"-source"
argument_list|)
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|HostExecutorBuilder
name|hostExecutorBuilder
init|=
operator|new
name|HostExecutorBuilder
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HostExecutor
name|build
parameter_list|(
name|Host
name|host
parameter_list|)
block|{
return|return
operator|new
name|HostExecutor
argument_list|(
name|host
argument_list|,
name|executionContext
operator|.
name|getPrivateKey
argument_list|()
argument_list|,
name|mExecutor
argument_list|,
name|sshCommandExecutor
argument_list|,
name|rsyncCommandExecutor
argument_list|,
name|templateDefaults
argument_list|,
name|scratchDir
argument_list|,
name|succeededLogDir
argument_list|,
name|failedLogDir
argument_list|,
literal|10
argument_list|,
name|logger
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
init|=
operator|new
name|ArrayList
argument_list|<
name|HostExecutor
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Host
name|host
range|:
name|mExecutionContext
operator|.
name|getHosts
argument_list|()
control|)
block|{
name|hostExecutors
operator|.
name|add
argument_list|(
name|hostExecutorBuilder
operator|.
name|build
argument_list|(
name|host
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|mHostExecutors
operator|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|HostExecutor
argument_list|>
argument_list|(
name|hostExecutors
argument_list|)
expr_stmt|;
name|mPhases
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|mPhases
operator|.
name|add
argument_list|(
operator|new
name|PrepPhase
argument_list|(
name|mHostExecutors
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|scratchDir
argument_list|,
name|patchFile
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
name|mPhases
operator|.
name|add
argument_list|(
operator|new
name|ExecutionPhase
argument_list|(
name|mHostExecutors
argument_list|,
name|mExecutionContext
argument_list|,
name|hostExecutorBuilder
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|succeededLogDir
argument_list|,
name|failedLogDir
argument_list|,
name|testParser
operator|.
name|parse
argument_list|()
argument_list|,
name|mExecutedTests
argument_list|,
name|mFailedTests
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
name|mPhases
operator|.
name|add
argument_list|(
operator|new
name|ReportingPhase
argument_list|(
name|mHostExecutors
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|run
parameter_list|()
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
name|boolean
name|error
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|elapsedTimes
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
try|try
block|{
name|mLogger
operator|.
name|info
argument_list|(
literal|"Running tests with "
operator|+
name|mConfiguration
argument_list|)
expr_stmt|;
for|for
control|(
name|Phase
name|phase
range|:
name|mPhases
control|)
block|{
name|String
name|msg
init|=
literal|"Executing "
operator|+
name|phase
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|mLogger
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|messages
operator|.
name|add
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|phase
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|long
name|elapsedTime
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|convert
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|elapsedTimes
operator|.
name|put
argument_list|(
name|phase
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|elapsedTime
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|mFailedTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|TestsFailedException
argument_list|(
name|mFailedTests
operator|.
name|size
argument_list|()
operator|+
literal|" tests failed"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|mLogger
operator|.
name|error
argument_list|(
literal|"Test run exited with an unexpected error"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
name|messages
operator|.
name|add
argument_list|(
literal|"Tests failed with: "
operator|+
name|throwable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": "
operator|+
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|error
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|mHostExecutors
control|)
block|{
if|if
condition|(
name|hostExecutor
operator|.
name|isBad
argument_list|()
condition|)
block|{
name|mExecutionContext
operator|.
name|addBadHost
argument_list|(
name|hostExecutor
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|mExecutor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|mFailedTests
argument_list|)
decl_stmt|;
if|if
condition|(
name|failedTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d failed tests"
argument_list|,
name|failedTests
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mLogger
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d failed tests"
argument_list|,
name|failedTests
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|failingTestName
range|:
name|failedTests
control|)
block|{
name|mLogger
operator|.
name|warn
argument_list|(
name|failingTestName
argument_list|)
expr_stmt|;
block|}
name|mLogger
operator|.
name|info
argument_list|(
literal|"Executed "
operator|+
name|mExecutedTests
operator|.
name|size
argument_list|()
operator|+
literal|" tests"
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
name|Long
argument_list|>
name|entry
range|:
name|elapsedTimes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"PERF: Phase %s took %d minutes"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|publishJiraComment
argument_list|(
name|error
argument_list|,
name|messages
argument_list|,
name|failedTests
argument_list|)
expr_stmt|;
if|if
condition|(
name|error
operator|||
operator|!
name|mFailedTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|=
literal|1
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|publishJiraComment
parameter_list|(
name|boolean
name|error
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|messages
parameter_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
parameter_list|)
block|{
if|if
condition|(
name|mConfiguration
operator|.
name|getJiraName
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
literal|"Skipping JIRA comment as name is empty."
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|mConfiguration
operator|.
name|getJiraUrl
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
literal|"Skipping JIRA comment as URL is empty."
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|mConfiguration
operator|.
name|getJiraUser
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
literal|"Skipping JIRA comment as user is empty."
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|mConfiguration
operator|.
name|getJiraPassword
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mLogger
operator|.
name|info
argument_list|(
literal|"Skipping JIRA comment as password is empty."
argument_list|)
expr_stmt|;
return|return;
block|}
name|JIRAService
name|jira
init|=
operator|new
name|JIRAService
argument_list|(
name|mLogger
argument_list|,
name|mConfiguration
argument_list|,
name|mBuildTag
argument_list|)
decl_stmt|;
name|jira
operator|.
name|postComment
argument_list|(
name|error
argument_list|,
name|mExecutedTests
operator|.
name|size
argument_list|()
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|public
name|PTest
name|build
parameter_list|(
name|TestConfiguration
name|configuration
parameter_list|,
name|ExecutionContext
name|executionContext
parameter_list|,
name|String
name|buildTag
parameter_list|,
name|File
name|logDir
parameter_list|,
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
name|SSHCommandExecutor
name|sshCommandExecutor
parameter_list|,
name|RSyncCommandExecutor
name|rsyncCommandExecutor
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|PTest
argument_list|(
name|configuration
argument_list|,
name|executionContext
argument_list|,
name|buildTag
argument_list|,
name|logDir
argument_list|,
name|localCommandFactory
argument_list|,
name|sshCommandExecutor
argument_list|,
name|rsyncCommandExecutor
argument_list|,
name|logger
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|String
name|PROPERTIES
init|=
literal|"properties"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REPOSITORY
init|=
name|TestConfiguration
operator|.
name|REPOSITORY
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REPOSITORY_NAME
init|=
name|TestConfiguration
operator|.
name|REPOSITORY_NAME
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BRANCH
init|=
name|TestConfiguration
operator|.
name|BRANCH
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PATCH
init|=
literal|"patch"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JAVA_HOME
init|=
name|TestConfiguration
operator|.
name|JAVA_HOME
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JAVA_HOME_TEST
init|=
name|TestConfiguration
operator|.
name|JAVA_HOME_TEST
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANT_TEST_ARGS
init|=
name|TestConfiguration
operator|.
name|ANT_TEST_ARGS
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANT_ENV_OPTS
init|=
name|TestConfiguration
operator|.
name|ANT_ENV_OPTS
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANT_TEST_TARGET
init|=
name|TestConfiguration
operator|.
name|ANT_TEST_TARGET
decl_stmt|;
comment|/**    * All args override properties file settings except    * for this one which is additive.    */
specifier|private
specifier|static
specifier|final
name|String
name|ANT_ARG
init|=
literal|"D"
decl_stmt|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Args "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|PROPERTIES
argument_list|,
literal|true
argument_list|,
literal|"properties file"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|REPOSITORY
argument_list|,
literal|true
argument_list|,
literal|"Overrides git repository in properties file"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|REPOSITORY_NAME
argument_list|,
literal|true
argument_list|,
literal|"Overrides git repository *name* in properties file"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|BRANCH
argument_list|,
literal|true
argument_list|,
literal|"Overrides git branch in properties file"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|PATCH
argument_list|,
literal|true
argument_list|,
literal|"URI to patch, either file:/// or http(s)://"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|ANT_ARG
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|"Supplemntal ant arguments"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|JAVA_HOME
argument_list|,
literal|true
argument_list|,
literal|"Java Home for compiling and running tests (unless "
operator|+
name|JAVA_HOME_TEST
operator|+
literal|" is specified)"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|JAVA_HOME_TEST
argument_list|,
literal|true
argument_list|,
literal|"Java Home for running tests (optional)"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|ANT_TEST_ARGS
argument_list|,
literal|true
argument_list|,
literal|"Arguments to ant test on slave nodes only"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|null
argument_list|,
name|ANT_ENV_OPTS
argument_list|,
literal|true
argument_list|,
literal|"ANT_OPTS environemnt variable setting"
argument_list|)
expr_stmt|;
name|CommandLine
name|commandLine
init|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|commandLine
operator|.
name|hasOption
argument_list|(
name|PROPERTIES
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|" "
argument_list|)
operator|.
name|join
argument_list|(
name|PTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|"--"
operator|+
name|PROPERTIES
argument_list|,
literal|"config.properties"
argument_list|)
argument_list|)
throw|;
block|}
name|String
name|testConfigurationFile
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|PROPERTIES
argument_list|)
decl_stmt|;
name|ExecutionContextConfiguration
name|executionContextConfiguration
init|=
name|ExecutionContextConfiguration
operator|.
name|fromFile
argument_list|(
name|testConfigurationFile
argument_list|)
decl_stmt|;
name|String
name|buildTag
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"BUILD_TAG"
argument_list|)
operator|==
literal|null
condition|?
literal|"undefined-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
else|:
name|System
operator|.
name|getenv
argument_list|(
literal|"BUILD_TAG"
argument_list|)
decl_stmt|;
name|File
name|logDir
init|=
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|executionContextConfiguration
operator|.
name|getGlobalLogDirectory
argument_list|()
argument_list|,
name|buildTag
argument_list|)
argument_list|)
decl_stmt|;
name|LogDirectoryCleaner
name|cleaner
init|=
operator|new
name|LogDirectoryCleaner
argument_list|(
operator|new
name|File
argument_list|(
name|executionContextConfiguration
operator|.
name|getGlobalLogDirectory
argument_list|()
argument_list|)
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|cleaner
operator|.
name|setName
argument_list|(
literal|"LogCleaner-"
operator|+
name|executionContextConfiguration
operator|.
name|getGlobalLogDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
name|TestConfiguration
name|conf
init|=
name|TestConfiguration
operator|.
name|fromFile
argument_list|(
name|testConfigurationFile
argument_list|,
name|LOG
argument_list|)
decl_stmt|;
name|String
name|repository
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|REPOSITORY
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|repository
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setRepository
argument_list|(
name|repository
argument_list|)
expr_stmt|;
block|}
name|String
name|repositoryName
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|REPOSITORY_NAME
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|repositoryName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setRepositoryName
argument_list|(
name|repositoryName
argument_list|)
expr_stmt|;
block|}
name|String
name|branch
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|BRANCH
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|branch
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setBranch
argument_list|(
name|branch
argument_list|)
expr_stmt|;
block|}
name|String
name|patch
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|PATCH
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|patch
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setPatch
argument_list|(
name|patch
argument_list|)
expr_stmt|;
block|}
name|String
name|javaHome
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|JAVA_HOME
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|javaHome
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setJavaHome
argument_list|(
name|javaHome
argument_list|)
expr_stmt|;
block|}
name|String
name|javaHomeForTests
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|JAVA_HOME_TEST
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|javaHomeForTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setJavaHomeForTests
argument_list|(
name|javaHomeForTests
argument_list|)
expr_stmt|;
block|}
name|String
name|antTestArgs
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|ANT_TEST_ARGS
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|antTestArgs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setAntTestArgs
argument_list|(
name|antTestArgs
argument_list|)
expr_stmt|;
block|}
name|String
name|antEnvOpts
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|ANT_ENV_OPTS
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|antEnvOpts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setAntEnvOpts
argument_list|(
name|antEnvOpts
argument_list|)
expr_stmt|;
block|}
name|String
name|antTestTarget
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
name|ANT_TEST_TARGET
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|antTestTarget
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|setAntTestTarget
argument_list|(
name|antTestTarget
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|supplementalAntArgs
init|=
name|commandLine
operator|.
name|getOptionValues
argument_list|(
name|ANT_ARG
argument_list|)
decl_stmt|;
if|if
condition|(
name|supplementalAntArgs
operator|!=
literal|null
operator|&&
name|supplementalAntArgs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|String
name|antArgs
init|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|conf
operator|.
name|getAntArgs
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|antArgs
operator|.
name|isEmpty
argument_list|()
operator|||
name|antArgs
operator|.
name|endsWith
argument_list|(
literal|" "
argument_list|)
operator|)
condition|)
block|{
name|antArgs
operator|+=
literal|" "
expr_stmt|;
block|}
name|antArgs
operator|+=
literal|"-"
operator|+
name|ANT_ARG
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|" -"
operator|+
name|ANT_ARG
argument_list|)
operator|.
name|join
argument_list|(
name|supplementalAntArgs
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setAntArgs
argument_list|(
name|antArgs
argument_list|)
expr_stmt|;
block|}
name|ExecutionContextProvider
name|executionContextProvider
init|=
literal|null
decl_stmt|;
name|ExecutionContext
name|executionContext
init|=
literal|null
decl_stmt|;
name|int
name|exitCode
init|=
literal|0
decl_stmt|;
try|try
block|{
name|executionContextProvider
operator|=
name|executionContextConfiguration
operator|.
name|getExecutionContextProvider
argument_list|()
expr_stmt|;
name|executionContext
operator|=
name|executionContextProvider
operator|.
name|createExecutionContext
argument_list|()
expr_stmt|;
name|PTest
name|ptest
init|=
operator|new
name|PTest
argument_list|(
name|conf
argument_list|,
name|executionContext
argument_list|,
name|buildTag
argument_list|,
name|logDir
argument_list|,
operator|new
name|LocalCommandFactory
argument_list|(
name|LOG
argument_list|)
argument_list|,
operator|new
name|SSHCommandExecutor
argument_list|(
name|LOG
argument_list|)
argument_list|,
operator|new
name|RSyncCommandExecutor
argument_list|(
name|LOG
argument_list|)
argument_list|,
name|LOG
argument_list|)
decl_stmt|;
name|exitCode
operator|=
name|ptest
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|executionContext
operator|!=
literal|null
condition|)
block|{
name|executionContext
operator|.
name|terminate
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|executionContextProvider
operator|!=
literal|null
condition|)
block|{
name|executionContextProvider
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

