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
name|api
operator|.
name|server
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
name|PrintStream
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
name|BlockingQueue
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
name|hive
operator|.
name|ptest
operator|.
name|api
operator|.
name|Status
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
name|api
operator|.
name|request
operator|.
name|TestStartRequest
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
name|Constants
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
name|Dirs
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
name|LocalCommandFactory
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
name|LogDirectoryCleaner
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
name|PTest
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
name|context
operator|.
name|CreateHostsFailedException
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
name|context
operator|.
name|ServiceNotAvailableException
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
comment|/**  * Executes parallel test in a single thread since the slaves  * will be fully utilized by the test environment.  */
end_comment

begin_class
specifier|public
class|class
name|TestExecutor
extends|extends
name|Thread
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
name|TestExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContextConfiguration
name|mExecutionContextConfiguration
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContextProvider
name|mExecutionContextProvider
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|Test
argument_list|>
name|mTestQueue
decl_stmt|;
specifier|private
specifier|final
name|PTest
operator|.
name|Builder
name|mPTestBuilder
decl_stmt|;
specifier|private
name|ExecutionContext
name|mExecutionContext
decl_stmt|;
specifier|private
name|boolean
name|execute
decl_stmt|;
specifier|public
name|TestExecutor
parameter_list|(
name|ExecutionContextConfiguration
name|executionContextConfiguration
parameter_list|,
name|ExecutionContextProvider
name|executionContextProvider
parameter_list|,
name|BlockingQueue
argument_list|<
name|Test
argument_list|>
name|testQueue
parameter_list|,
name|PTest
operator|.
name|Builder
name|pTestBuilder
parameter_list|)
block|{
name|mExecutionContextConfiguration
operator|=
name|executionContextConfiguration
expr_stmt|;
name|mExecutionContextProvider
operator|=
name|executionContextProvider
expr_stmt|;
name|mTestQueue
operator|=
name|testQueue
expr_stmt|;
name|mPTestBuilder
operator|=
name|pTestBuilder
expr_stmt|;
name|execute
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
while|while
condition|(
name|execute
condition|)
block|{
name|Test
name|test
init|=
literal|null
decl_stmt|;
name|PrintStream
name|logStream
init|=
literal|null
decl_stmt|;
name|Logger
name|logger
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// start a log cleaner at the start of each test
name|LogDirectoryCleaner
name|cleaner
init|=
operator|new
name|LogDirectoryCleaner
argument_list|(
operator|new
name|File
argument_list|(
name|mExecutionContextConfiguration
operator|.
name|getGlobalLogDirectory
argument_list|()
argument_list|)
argument_list|,
name|mExecutionContextConfiguration
operator|.
name|getMaxLogDirectoriesPerProfile
argument_list|()
argument_list|)
decl_stmt|;
name|cleaner
operator|.
name|setName
argument_list|(
literal|"LogCleaner-"
operator|+
name|mExecutionContextConfiguration
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
name|test
operator|=
name|mTestQueue
operator|.
name|poll
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|execute
condition|)
block|{
name|terminateExecutionContext
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|test
operator|==
literal|null
condition|)
block|{
name|terminateExecutionContext
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|test
operator|.
name|setStatus
argument_list|(
name|Status
operator|.
name|inProgress
argument_list|()
argument_list|)
expr_stmt|;
name|test
operator|.
name|setDequeueTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|mExecutionContext
operator|==
literal|null
condition|)
block|{
name|mExecutionContext
operator|=
name|createExceutionContext
argument_list|()
expr_stmt|;
block|}
name|test
operator|.
name|setExecutionStartTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|TestStartRequest
name|startRequest
init|=
name|test
operator|.
name|getStartRequest
argument_list|()
decl_stmt|;
name|String
name|profile
init|=
name|startRequest
operator|.
name|getProfile
argument_list|()
decl_stmt|;
name|File
name|profileConfFile
init|=
operator|new
name|File
argument_list|(
name|mExecutionContextConfiguration
operator|.
name|getProfileDirectory
argument_list|()
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%s.properties"
argument_list|,
name|profile
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|profileConfFile
operator|.
name|isFile
argument_list|()
condition|)
block|{
name|test
operator|.
name|setStatus
argument_list|(
name|Status
operator|.
name|illegalArgument
argument_list|(
literal|"Profile "
operator|+
name|profile
operator|+
literal|" not found in directory "
operator|+
name|mExecutionContextConfiguration
operator|.
name|getProfileDirectory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|test
operator|.
name|setExecutionFinishTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|mExecutionContextConfiguration
operator|.
name|getGlobalLogDirectory
argument_list|()
argument_list|,
name|test
operator|.
name|getStartRequest
argument_list|()
operator|.
name|getTestHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|File
name|logFile
init|=
operator|new
name|File
argument_list|(
name|logDir
argument_list|,
literal|"execution.txt"
argument_list|)
decl_stmt|;
name|test
operator|.
name|setOutputFile
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
name|logStream
operator|=
operator|new
name|PrintStream
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
name|logger
operator|=
operator|new
name|TestLogger
argument_list|(
name|logStream
argument_list|,
name|TestLogger
operator|.
name|LEVEL
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
name|TestConfiguration
name|testConfiguration
init|=
name|TestConfiguration
operator|.
name|fromFile
argument_list|(
name|profileConfFile
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|testConfiguration
operator|.
name|setPatch
argument_list|(
name|startRequest
operator|.
name|getPatchURL
argument_list|()
argument_list|)
expr_stmt|;
name|testConfiguration
operator|.
name|setJiraName
argument_list|(
name|startRequest
operator|.
name|getJiraName
argument_list|()
argument_list|)
expr_stmt|;
name|testConfiguration
operator|.
name|setClearLibraryCache
argument_list|(
name|startRequest
operator|.
name|isClearLibraryCache
argument_list|()
argument_list|)
expr_stmt|;
name|LocalCommandFactory
name|localCommandFactory
init|=
operator|new
name|LocalCommandFactory
argument_list|(
name|logger
argument_list|)
decl_stmt|;
name|PTest
name|ptest
init|=
name|mPTestBuilder
operator|.
name|build
argument_list|(
name|testConfiguration
argument_list|,
name|mExecutionContext
argument_list|,
name|test
operator|.
name|getStartRequest
argument_list|()
operator|.
name|getTestHandle
argument_list|()
argument_list|,
name|logDir
argument_list|,
name|localCommandFactory
argument_list|,
operator|new
name|SSHCommandExecutor
argument_list|(
name|logger
argument_list|)
argument_list|,
operator|new
name|RSyncCommandExecutor
argument_list|(
name|logger
argument_list|,
name|mExecutionContextConfiguration
operator|.
name|getMaxRsyncThreads
argument_list|()
argument_list|,
name|localCommandFactory
argument_list|)
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|int
name|result
init|=
name|ptest
operator|.
name|run
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
name|Constants
operator|.
name|EXIT_CODE_SUCCESS
condition|)
block|{
name|test
operator|.
name|setStatus
argument_list|(
name|Status
operator|.
name|ok
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|test
operator|.
name|setStatus
argument_list|(
name|Status
operator|.
name|failed
argument_list|(
literal|"Tests failed with exit code "
operator|+
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logStream
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// if all drones where abandoned on a host, try replacing them.
name|mExecutionContext
operator|.
name|replaceBadHosts
argument_list|()
expr_stmt|;
block|}
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
name|error
argument_list|(
literal|"Unxpected Error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|test
operator|!=
literal|null
condition|)
block|{
name|test
operator|.
name|setStatus
argument_list|(
name|Status
operator|.
name|failed
argument_list|(
literal|"Tests failed with exception "
operator|+
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|!=
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"Error executing "
operator|+
name|test
operator|.
name|getStartRequest
argument_list|()
operator|.
name|getTestHandle
argument_list|()
decl_stmt|;
name|logger
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if we died for any reason lets get a new set of hosts
name|terminateExecutionContext
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|test
operator|!=
literal|null
condition|)
block|{
name|test
operator|.
name|setExecutionFinishTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logStream
operator|!=
literal|null
condition|)
block|{
name|logStream
operator|.
name|flush
argument_list|()
expr_stmt|;
name|logStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|terminateExecutionContext
parameter_list|()
block|{
if|if
condition|(
name|mExecutionContext
operator|!=
literal|null
condition|)
block|{
name|mExecutionContext
operator|.
name|terminate
argument_list|()
expr_stmt|;
name|mExecutionContext
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|ExecutionContext
name|createExceutionContext
parameter_list|()
throws|throws
name|ServiceNotAvailableException
throws|,
name|InterruptedException
throws|,
name|CreateHostsFailedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to create a new execution context"
argument_list|)
expr_stmt|;
name|ExecutionContext
name|result
init|=
name|mExecutionContextProvider
operator|.
name|createExecutionContext
argument_list|()
decl_stmt|;
name|long
name|elapsedTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Context Creation time: "
operator|+
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|convert
argument_list|(
name|elapsedTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|execute
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

