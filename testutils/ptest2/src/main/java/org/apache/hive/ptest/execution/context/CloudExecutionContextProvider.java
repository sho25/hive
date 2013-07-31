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
operator|.
name|context
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|RandomAccessFile
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
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|conf
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
name|ssh
operator|.
name|SSHCommand
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
name|jclouds
operator|.
name|compute
operator|.
name|RunNodesException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|NodeMetadata
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
name|Iterables
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

begin_class
specifier|public
class|class
name|CloudExecutionContextProvider
implements|implements
name|ExecutionContextProvider
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
name|CloudExecutionContextProvider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATA_DIR
init|=
literal|"dataDir"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|API_KEY
init|=
literal|"apiKey"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ACCESS_KEY
init|=
literal|"accessKey"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NUM_HOSTS
init|=
literal|"numHosts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|GROUP_NAME
init|=
literal|"groupName"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IMAGE_ID
init|=
literal|"imageId"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KEY_PAIR
init|=
literal|"keyPair"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SECURITY_GROUP
init|=
literal|"securityGroup"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_BID
init|=
literal|"maxBid"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SLAVE_LOCAL_DIRECTORIES
init|=
literal|"localDirs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USERNAME
init|=
literal|"user"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSTANCE_TYPE
init|=
literal|"instanceType"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NUM_THREADS
init|=
literal|"numThreads"
decl_stmt|;
specifier|private
specifier|final
name|RandomAccessFile
name|mHostLog
decl_stmt|;
specifier|private
specifier|final
name|String
name|mPrivateKey
decl_stmt|;
specifier|private
specifier|final
name|String
name|mUser
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|mSlaveLocalDirs
decl_stmt|;
specifier|private
specifier|final
name|int
name|mNumThreads
decl_stmt|;
specifier|private
specifier|final
name|int
name|mNumHosts
decl_stmt|;
specifier|private
specifier|final
name|int
name|mNumRetries
decl_stmt|;
specifier|private
specifier|final
name|long
name|mRetrySleepInterval
decl_stmt|;
specifier|private
specifier|final
name|CloudComputeService
name|mCloudComputeService
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|mTerminatedHosts
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|mTerminationExecutor
decl_stmt|;
specifier|private
specifier|final
name|File
name|mWorkingDir
decl_stmt|;
specifier|private
specifier|final
name|SSHCommandExecutor
name|mSSHCommandExecutor
decl_stmt|;
annotation|@
name|VisibleForTesting
name|CloudExecutionContextProvider
parameter_list|(
name|String
name|dataDir
parameter_list|,
name|int
name|numHosts
parameter_list|,
name|CloudComputeService
name|cloudComputeService
parameter_list|,
name|SSHCommandExecutor
name|sshCommandExecutor
parameter_list|,
name|String
name|workingDirectory
parameter_list|,
name|String
name|privateKey
parameter_list|,
name|String
name|user
parameter_list|,
name|String
index|[]
name|slaveLocalDirs
parameter_list|,
name|int
name|numThreads
parameter_list|,
name|int
name|numRetries
parameter_list|,
name|long
name|retrySleepInterval
parameter_list|)
throws|throws
name|IOException
block|{
name|mNumHosts
operator|=
name|numHosts
expr_stmt|;
name|mCloudComputeService
operator|=
name|cloudComputeService
expr_stmt|;
name|mPrivateKey
operator|=
name|privateKey
expr_stmt|;
name|mUser
operator|=
name|user
expr_stmt|;
name|mSlaveLocalDirs
operator|=
name|slaveLocalDirs
expr_stmt|;
name|mNumThreads
operator|=
name|numThreads
expr_stmt|;
name|mNumRetries
operator|=
name|numRetries
expr_stmt|;
name|mRetrySleepInterval
operator|=
name|retrySleepInterval
expr_stmt|;
name|mSSHCommandExecutor
operator|=
name|sshCommandExecutor
expr_stmt|;
name|mWorkingDir
operator|=
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|workingDirectory
argument_list|,
literal|"working"
argument_list|)
argument_list|)
expr_stmt|;
name|mTerminatedHosts
operator|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
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
name|boolean
name|removeEldestEntry
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
parameter_list|)
block|{
return|return
name|size
argument_list|()
operator|>
literal|100
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|mTerminationExecutor
operator|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
expr_stmt|;
name|mHostLog
operator|=
operator|new
name|RandomAccessFile
argument_list|(
operator|new
name|File
argument_list|(
name|dataDir
argument_list|,
literal|"hosts"
argument_list|)
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
name|initialize
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hosts
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|String
name|host
init|=
literal|null
decl_stmt|;
name|mHostLog
operator|.
name|seek
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// should already be true
while|while
condition|(
operator|(
name|host
operator|=
name|mHostLog
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|hosts
operator|.
name|add
argument_list|(
name|host
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|terminate
argument_list|(
name|hosts
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|mHostLog
operator|.
name|seek
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|mHostLog
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|sleep
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|performBackgroundWork
argument_list|()
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
literal|"Unexpected error in background worker"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-BackgroundWorker"
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|terminate
parameter_list|(
name|ExecutionContext
name|executionContext
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hostsToTerminate
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Host
name|host
range|:
name|executionContext
operator|.
name|getHosts
argument_list|()
control|)
block|{
name|hostsToTerminate
operator|.
name|add
argument_list|(
name|host
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|terminate
argument_list|(
name|hostsToTerminate
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|replaceBadHosts
parameter_list|(
name|ExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|CreateHostsFailedException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hostsToTerminate
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Host
argument_list|>
name|hostsNotRemoved
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Host
name|host
range|:
name|executionContext
operator|.
name|getBadHosts
argument_list|()
control|)
block|{
name|hostsToTerminate
operator|.
name|add
argument_list|(
name|host
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|executionContext
operator|.
name|removeHost
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|hostsNotRemoved
operator|.
name|add
argument_list|(
name|host
argument_list|)
expr_stmt|;
block|}
block|}
name|executionContext
operator|.
name|clearBadHosts
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|hostsToTerminate
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Replacing "
operator|+
name|hostsToTerminate
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|hostsToTerminate
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|nodes
init|=
name|createNodes
argument_list|(
name|hostsToTerminate
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|NodeMetadata
name|node
range|:
name|nodes
control|)
block|{
name|executionContext
operator|.
name|addHost
argument_list|(
operator|new
name|Host
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|,
name|mUser
argument_list|,
name|mSlaveLocalDirs
argument_list|,
name|mNumThreads
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Preconditions
operator|.
name|checkState
argument_list|(
name|hostsNotRemoved
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"Host "
operator|+
name|hostsNotRemoved
operator|+
literal|" was in bad hosts but could not be removed"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|ExecutionContext
name|createExecutionContext
parameter_list|()
throws|throws
name|CreateHostsFailedException
throws|,
name|ServiceNotAvailableException
block|{
try|try
block|{
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|nodes
init|=
name|createNodes
argument_list|(
name|mNumHosts
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Host
argument_list|>
name|hosts
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeMetadata
name|node
range|:
name|nodes
control|)
block|{
name|hosts
operator|.
name|add
argument_list|(
operator|new
name|Host
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|,
name|mUser
argument_list|,
name|mSlaveLocalDirs
argument_list|,
name|mNumThreads
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ExecutionContext
argument_list|(
name|this
argument_list|,
name|hosts
argument_list|,
name|mWorkingDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|mPrivateKey
argument_list|)
return|;
block|}
finally|finally
block|{
name|syncLog
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|createNodes
parameter_list|(
name|int
name|numHosts
parameter_list|)
throws|throws
name|CreateHostsFailedException
block|{
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|result
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|int
name|attempts
init|=
literal|0
decl_stmt|;
name|int
name|numRequired
init|=
name|numHosts
decl_stmt|;
name|RunNodesException
name|exception
init|=
literal|null
decl_stmt|;
do|do
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to create "
operator|+
name|numRequired
operator|+
literal|" nodes"
argument_list|)
expr_stmt|;
try|try
block|{
name|result
operator|.
name|addAll
argument_list|(
name|verifyHosts
argument_list|(
name|mCloudComputeService
operator|.
name|createNodes
argument_list|(
name|numRequired
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RunNodesException
name|e
parameter_list|)
block|{
name|exception
operator|=
name|e
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error creating nodes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|terminateInternal
argument_list|(
name|e
operator|.
name|getNodeErrors
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|addAll
argument_list|(
name|verifyHosts
argument_list|(
name|e
operator|.
name|getSuccessfulNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully created "
operator|+
name|result
operator|.
name|size
argument_list|()
operator|+
literal|" nodes"
argument_list|)
expr_stmt|;
name|numRequired
operator|=
name|numHosts
operator|-
name|result
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|numRequired
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
name|mRetrySleepInterval
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|CreateHostsFailedException
argument_list|(
literal|"Interrupted while trying to create hosts"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
do|while
condition|(
name|attempts
operator|++
operator|<
name|mNumRetries
operator|&&
name|numRequired
operator|>
literal|0
condition|)
do|;
if|if
condition|(
name|result
operator|.
name|size
argument_list|()
operator|<
name|numHosts
condition|)
block|{
throw|throw
operator|new
name|CreateHostsFailedException
argument_list|(
literal|"Error creating nodes"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down TerminationExecutor"
argument_list|)
expr_stmt|;
name|mTerminationExecutor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing CloudComputeService"
argument_list|)
expr_stmt|;
name|mCloudComputeService
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// I don't entirely believe that everything is cleaned up
comment|// when close is called based on watching the TRACE logs
try|try
block|{
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore, shutting down anyway
block|}
block|}
specifier|private
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|verifyHosts
parameter_list|(
name|Set
argument_list|<
name|?
extends|extends
name|NodeMetadata
argument_list|>
name|hosts
parameter_list|)
throws|throws
name|CreateHostsFailedException
block|{
name|persistHostnamesToLog
argument_list|(
name|hosts
argument_list|)
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|result
init|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|NodeMetadata
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|hosts
operator|.
name|size
argument_list|()
argument_list|,
literal|25
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
specifier|final
name|NodeMetadata
name|node
range|:
name|hosts
control|)
block|{
name|executorService
operator|.
name|submit
argument_list|(
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
name|SSHCommand
name|command
init|=
operator|new
name|SSHCommand
argument_list|(
name|mSSHCommandExecutor
argument_list|,
name|mPrivateKey
argument_list|,
name|mUser
argument_list|,
name|node
operator|.
name|getHostname
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|"pkill -f java"
argument_list|)
decl_stmt|;
name|mSSHCommandExecutor
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
if|if
condition|(
name|command
operator|.
name|getExitCode
argument_list|()
operator|==
name|Constants
operator|.
name|EXIT_CODE_UNKNOWN
operator|||
name|command
operator|.
name|getException
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|command
operator|.
name|getException
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Node "
operator|+
name|node
operator|.
name|getHostname
argument_list|()
operator|+
literal|" is bad on startup"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Node "
operator|+
name|node
operator|.
name|getHostname
argument_list|()
operator|+
literal|" is bad on startup"
argument_list|,
name|command
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|terminateInternal
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|executorService
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Verify command still executing on a host after 10 minutes"
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
throw|throw
operator|new
name|CreateHostsFailedException
argument_list|(
literal|"Interrupted while trying to create hosts"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|executorService
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|performBackgroundWork
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing background work"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|terminatedHosts
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|mTerminatedHosts
init|)
block|{
name|terminatedHosts
operator|.
name|putAll
argument_list|(
name|mTerminatedHosts
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|NodeMetadata
name|node
range|:
name|getRunningNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|terminatedHosts
operator|.
name|containsKey
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
name|terminateInternal
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found zombie node: "
operator|+
name|node
operator|+
literal|" previously terminated at "
operator|+
operator|new
name|Date
argument_list|(
name|terminatedHosts
operator|.
name|get
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|getRunningNodes
parameter_list|()
block|{
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|result
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|computes
init|=
name|mCloudComputeService
operator|.
name|listRunningNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeMetadata
name|node
range|:
name|computes
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|terminateInternal
parameter_list|(
name|Set
argument_list|<
name|?
extends|extends
name|NodeMetadata
argument_list|>
name|nodes
parameter_list|)
block|{
for|for
control|(
name|NodeMetadata
name|node
range|:
name|nodes
control|)
block|{
name|terminateInternal
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|terminateInternal
parameter_list|(
specifier|final
name|NodeMetadata
name|node
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Submitting termination for "
operator|+
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|mTerminationExecutor
operator|.
name|submit
argument_list|(
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Terminating "
operator|+
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|mCloudComputeService
operator|.
name|destroyNode
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mTerminatedHosts
operator|.
name|containsKey
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
name|mTerminatedHosts
operator|.
name|put
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|error
argument_list|(
literal|"Error attempting to terminate host "
operator|+
name|node
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|persistHostnamesToLog
parameter_list|(
name|Set
argument_list|<
name|?
extends|extends
name|NodeMetadata
argument_list|>
name|nodes
parameter_list|)
block|{
for|for
control|(
name|NodeMetadata
name|node
range|:
name|nodes
control|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mHostLog
operator|.
name|writeBytes
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
operator|+
literal|"\n"
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
specifier|private
name|void
name|syncLog
parameter_list|()
block|{
try|try
block|{
name|mHostLog
operator|.
name|getFD
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|terminate
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|hosts
parameter_list|,
name|boolean
name|warnIfHostsNotFound
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Requesting termination of "
operator|+
name|hosts
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|nodesToTerminate
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeMetadata
name|node
range|:
name|getRunningNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|hosts
operator|.
name|contains
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
condition|)
block|{
name|nodesToTerminate
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
name|terminateInternal
argument_list|(
name|nodesToTerminate
argument_list|)
expr_stmt|;
if|if
condition|(
name|warnIfHostsNotFound
operator|&&
name|nodesToTerminate
operator|.
name|size
argument_list|()
operator|!=
name|hosts
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Requested termination of "
operator|+
name|hosts
operator|.
name|size
argument_list|()
operator|+
literal|" but found only "
operator|+
name|nodesToTerminate
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|Builder
implements|implements
name|ExecutionContextProvider
operator|.
name|Builder
block|{
annotation|@
name|Override
specifier|public
name|ExecutionContextProvider
name|build
parameter_list|(
name|Context
name|context
parameter_list|,
name|String
name|workingDirectory
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|create
argument_list|(
name|context
argument_list|,
name|workingDirectory
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|CloudExecutionContextProvider
name|create
parameter_list|(
name|Context
name|context
parameter_list|,
name|String
name|workingDirectory
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|dataDir
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|DATA_DIR
argument_list|)
argument_list|,
name|DATA_DIR
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|String
name|apiKey
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|API_KEY
argument_list|)
argument_list|,
name|API_KEY
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|String
name|accessKey
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|ACCESS_KEY
argument_list|)
argument_list|,
name|ACCESS_KEY
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|Integer
name|numHosts
init|=
name|context
operator|.
name|getInteger
argument_list|(
name|NUM_HOSTS
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numHosts
operator|>
literal|0
argument_list|,
name|NUM_HOSTS
operator|+
literal|" must be greater than zero"
argument_list|)
expr_stmt|;
name|String
name|groupName
init|=
name|context
operator|.
name|getString
argument_list|(
name|GROUP_NAME
argument_list|,
literal|"hive-ptest-slaves"
argument_list|)
decl_stmt|;
name|String
name|imageId
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|IMAGE_ID
argument_list|)
argument_list|,
name|IMAGE_ID
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|String
name|keyPair
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|KEY_PAIR
argument_list|)
argument_list|,
name|KEY_PAIR
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|String
name|securityGroup
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|SECURITY_GROUP
argument_list|)
argument_list|,
name|SECURITY_GROUP
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|Float
name|maxBid
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getFloat
argument_list|(
name|MAX_BID
argument_list|)
argument_list|,
name|MAX_BID
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|maxBid
operator|>
literal|0
argument_list|,
name|MAX_BID
operator|+
literal|" must be greater than zero"
argument_list|)
expr_stmt|;
name|String
name|privateKey
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|PRIVATE_KEY
argument_list|)
argument_list|,
name|PRIVATE_KEY
operator|+
literal|" is required"
argument_list|)
decl_stmt|;
name|String
name|user
init|=
name|context
operator|.
name|getString
argument_list|(
name|USERNAME
argument_list|,
literal|"hiveptest"
argument_list|)
decl_stmt|;
name|String
index|[]
name|localDirs
init|=
name|Iterables
operator|.
name|toArray
argument_list|(
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|trimResults
argument_list|()
operator|.
name|split
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|SLAVE_LOCAL_DIRECTORIES
argument_list|,
literal|"/home/hiveptest/"
argument_list|)
argument_list|)
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|Integer
name|numThreads
init|=
name|context
operator|.
name|getInteger
argument_list|(
name|NUM_THREADS
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|String
name|instanceType
init|=
name|context
operator|.
name|getString
argument_list|(
name|INSTANCE_TYPE
argument_list|,
literal|"c1.xlarge"
argument_list|)
decl_stmt|;
name|CloudComputeService
name|cloudComputeService
init|=
operator|new
name|CloudComputeService
argument_list|(
name|apiKey
argument_list|,
name|accessKey
argument_list|,
name|instanceType
argument_list|,
name|groupName
argument_list|,
name|imageId
argument_list|,
name|keyPair
argument_list|,
name|securityGroup
argument_list|,
name|maxBid
argument_list|)
decl_stmt|;
name|CloudExecutionContextProvider
name|service
init|=
operator|new
name|CloudExecutionContextProvider
argument_list|(
name|dataDir
argument_list|,
name|numHosts
argument_list|,
name|cloudComputeService
argument_list|,
operator|new
name|SSHCommandExecutor
argument_list|(
name|LOG
argument_list|)
argument_list|,
name|workingDirectory
argument_list|,
name|privateKey
argument_list|,
name|user
argument_list|,
name|localDirs
argument_list|,
name|numThreads
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
decl_stmt|;
return|return
name|service
return|;
block|}
block|}
end_class

end_unit

