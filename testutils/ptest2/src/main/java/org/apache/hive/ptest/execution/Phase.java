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
name|IOException
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
name|Callable
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
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|LocalCommand
operator|.
name|CollectLogPolicy
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
name|NonZeroExitCodeException
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
name|RemoteCommandResult
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
name|SSHExecutionException
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
name|SSHResult
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
name|util
operator|.
name|concurrent
operator|.
name|Futures
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
name|ListenableFuture
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
specifier|abstract
class|class
name|Phase
block|{
specifier|protected
specifier|final
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
decl_stmt|;
specifier|private
specifier|final
name|LocalCommandFactory
name|localCommandFactory
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
decl_stmt|;
specifier|protected
specifier|final
name|Logger
name|logger
decl_stmt|;
specifier|public
name|Phase
parameter_list|(
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
parameter_list|,
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|hostExecutors
operator|=
name|hostExecutors
expr_stmt|;
name|this
operator|.
name|localCommandFactory
operator|=
name|localCommandFactory
expr_stmt|;
name|this
operator|.
name|templateDefaults
operator|=
name|templateDefaults
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|execute
parameter_list|()
throws|throws
name|Throwable
function_decl|;
comment|// clean prep
specifier|protected
name|void
name|execLocally
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|NonZeroExitCodeException
block|{
name|CollectLogPolicy
name|localCollector
init|=
operator|new
name|CollectLogPolicy
argument_list|(
name|logger
argument_list|)
decl_stmt|;
name|command
operator|=
name|Templates
operator|.
name|getTemplateResult
argument_list|(
name|command
argument_list|,
name|templateDefaults
argument_list|)
expr_stmt|;
name|LocalCommand
name|localCmd
init|=
name|localCommandFactory
operator|.
name|create
argument_list|(
name|localCollector
argument_list|,
name|command
argument_list|)
decl_stmt|;
if|if
condition|(
name|localCmd
operator|.
name|getExitCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|NonZeroExitCodeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Command '%s' failed with exit status %d and output '%s'"
argument_list|,
name|command
argument_list|,
name|localCmd
operator|.
name|getExitCode
argument_list|()
argument_list|,
name|localCollector
operator|.
name|getOutput
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// prep
specifier|protected
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|rsyncFromLocalToRemoteInstances
parameter_list|(
name|String
name|localFile
parameter_list|,
name|String
name|remoteFile
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|hostExecutors
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|rsyncFromLocalToRemoteInstances
argument_list|(
name|localFile
argument_list|,
name|remoteFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|flatten
argument_list|(
name|futures
argument_list|)
return|;
block|}
comment|// clean
specifier|protected
name|List
argument_list|<
name|SSHResult
argument_list|>
name|execHosts
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|SSHResult
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|hostExecutors
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|exec
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|toListOfResults
argument_list|(
name|futures
argument_list|)
return|;
block|}
comment|// clean prep
specifier|protected
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|execInstances
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|hostExecutors
control|)
block|{
name|futures
operator|.
name|addAll
argument_list|(
name|hostExecutor
operator|.
name|execInstances
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|toListOfResults
argument_list|(
name|futures
argument_list|)
return|;
block|}
specifier|protected
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|initalizeHosts
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|ListeningExecutorService
name|executor
init|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|hostExecutors
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
specifier|final
name|HostExecutor
name|hostExecutor
range|:
name|hostExecutors
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|initalizeHost
argument_list|(
name|hostExecutor
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
name|future
range|:
name|futures
control|)
block|{
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|result
init|=
name|future
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|addAll
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
return|return
name|results
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|executor
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|initalizeHost
parameter_list|(
name|HostExecutor
name|hostExecutor
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|RemoteCommandResult
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|exec
argument_list|(
literal|"killall -q -9 -f java || true"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// order matters in all of these so block
name|results
operator|.
name|addAll
argument_list|(
name|toListOfResults
argument_list|(
name|hostExecutor
operator|.
name|execInstances
argument_list|(
literal|"rm -rf $localDir/$instanceName/scratch $localDir/$instanceName/logs"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|addAll
argument_list|(
name|toListOfResults
argument_list|(
name|hostExecutor
operator|.
name|execInstances
argument_list|(
literal|"mkdir -p $localDir/$instanceName/logs "
operator|+
literal|"$localDir/$instanceName/maven "
operator|+
literal|"$localDir/$instanceName/scratch "
operator|+
literal|"$localDir/$instanceName/ivy "
operator|+
literal|"$localDir/$instanceName/${repositoryName}-source"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// order does not matter below, so go wide
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|RemoteCommandResult
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|rsyncFromLocalToRemoteInstances
argument_list|(
literal|"$workingDir/${repositoryName}-source"
argument_list|,
literal|"$localDir/$instanceName/"
argument_list|)
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|rsyncFromLocalToRemoteInstances
argument_list|(
literal|"$workingDir/maven"
argument_list|,
literal|"$localDir/$instanceName/"
argument_list|)
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|rsyncFromLocalToRemoteInstances
argument_list|(
literal|"$workingDir/ivy"
argument_list|,
literal|"$localDir/$instanceName/"
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|.
name|addAll
argument_list|(
name|flatten
argument_list|(
name|futures
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
specifier|private
parameter_list|<
name|T
extends|extends
name|RemoteCommandResult
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|flatten
parameter_list|(
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|futures
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|T
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|>
name|future
range|:
name|futures
control|)
block|{
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
name|result
init|=
name|future
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|addAll
argument_list|(
name|toListOfResults
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
specifier|private
parameter_list|<
name|T
extends|extends
name|RemoteCommandResult
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|toListOfResults
parameter_list|(
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|T
argument_list|>
argument_list|>
name|futures
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|T
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|T
name|result
range|:
name|Futures
operator|.
name|allAsList
argument_list|(
name|futures
argument_list|)
operator|.
name|get
argument_list|()
control|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|result
operator|.
name|getException
argument_list|()
operator|!=
literal|null
operator|||
name|result
operator|.
name|getExitCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|SSHExecutionException
argument_list|(
name|result
argument_list|)
throw|;
block|}
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
specifier|protected
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getTemplateDefaults
parameter_list|()
block|{
return|return
name|templateDefaults
return|;
block|}
block|}
end_class

end_unit

