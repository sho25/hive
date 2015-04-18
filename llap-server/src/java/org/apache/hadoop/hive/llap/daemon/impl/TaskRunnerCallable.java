begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|impl
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|ConcurrentMap
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
name|atomic
operator|.
name|AtomicLong
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
name|hive
operator|.
name|common
operator|.
name|CallableWithNdc
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
name|llap
operator|.
name|daemon
operator|.
name|HistoryLogger
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
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
name|llap
operator|.
name|metrics
operator|.
name|LlapDaemonExecutorMetrics
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
name|llap
operator|.
name|protocol
operator|.
name|LlapTaskUmbilicalProtocol
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
name|llap
operator|.
name|tezplugins
operator|.
name|Converters
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
name|ipc
operator|.
name|RPC
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
name|net
operator|.
name|NetUtils
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
name|Credentials
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
name|SecurityUtil
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
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|TezCommonUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|JobTokenIdentifier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|TokenCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|input
operator|.
name|MRInputLegacy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
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
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|InputSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|TaskSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|common
operator|.
name|objectregistry
operator|.
name|ObjectRegistryImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|internals
operator|.
name|api
operator|.
name|TaskReporterInterface
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|task
operator|.
name|TezChild
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|task
operator|.
name|TezTaskRunner
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
name|Stopwatch
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
name|HashMultimap
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
name|Multimap
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
name|FutureCallback
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
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TaskRunnerCallable
extends|extends
name|CallableWithNdc
argument_list|<
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|TaskRunnerCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|localDirs
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|envMap
decl_stmt|;
specifier|private
specifier|final
name|String
name|pid
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|ObjectRegistryImpl
name|objectRegistry
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContext
name|executionContext
decl_stmt|;
specifier|private
specifier|final
name|Credentials
name|credentials
decl_stmt|;
specifier|private
specifier|final
name|long
name|memoryAvailable
decl_stmt|;
specifier|private
specifier|final
name|ConfParams
name|confParams
decl_stmt|;
specifier|private
specifier|final
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|jobToken
decl_stmt|;
specifier|private
specifier|final
name|AMReporter
name|amReporter
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
argument_list|>
name|sourceCompletionMap
decl_stmt|;
specifier|private
specifier|final
name|TaskSpec
name|taskSpec
decl_stmt|;
specifier|private
specifier|volatile
name|TezTaskRunner
name|taskRunner
decl_stmt|;
specifier|private
specifier|volatile
name|TaskReporterInterface
name|taskReporter
decl_stmt|;
specifier|private
specifier|volatile
name|ListeningExecutorService
name|executor
decl_stmt|;
specifier|private
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|startTime
decl_stmt|;
specifier|private
specifier|volatile
name|String
name|threadName
decl_stmt|;
specifier|private
name|LlapDaemonExecutorMetrics
name|metrics
decl_stmt|;
specifier|protected
name|String
name|requestId
decl_stmt|;
name|TaskRunnerCallable
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ExecutionContext
name|executionContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|envMap
parameter_list|,
name|String
index|[]
name|localDirs
parameter_list|,
name|Credentials
name|credentials
parameter_list|,
name|long
name|memoryAvailable
parameter_list|,
name|AMReporter
name|amReporter
parameter_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
argument_list|>
name|sourceCompletionMap
parameter_list|,
name|ConfParams
name|confParams
parameter_list|,
name|LlapDaemonExecutorMetrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|executionContext
operator|=
name|executionContext
expr_stmt|;
name|this
operator|.
name|envMap
operator|=
name|envMap
expr_stmt|;
name|this
operator|.
name|localDirs
operator|=
name|localDirs
expr_stmt|;
name|this
operator|.
name|objectRegistry
operator|=
operator|new
name|ObjectRegistryImpl
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceCompletionMap
operator|=
name|sourceCompletionMap
expr_stmt|;
name|this
operator|.
name|credentials
operator|=
name|credentials
expr_stmt|;
name|this
operator|.
name|memoryAvailable
operator|=
name|memoryAvailable
expr_stmt|;
name|this
operator|.
name|confParams
operator|=
name|confParams
expr_stmt|;
name|this
operator|.
name|jobToken
operator|=
name|TokenCache
operator|.
name|getSessionToken
argument_list|(
name|credentials
argument_list|)
expr_stmt|;
name|this
operator|.
name|taskSpec
operator|=
name|Converters
operator|.
name|getTaskSpecfromProto
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|amReporter
operator|=
name|amReporter
expr_stmt|;
comment|// Register with the AMReporter when the callable is setup. Unregister once it starts running.
if|if
condition|(
name|jobToken
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|amReporter
operator|.
name|registerTask
argument_list|(
name|request
operator|.
name|getAmHost
argument_list|()
argument_list|,
name|request
operator|.
name|getAmPort
argument_list|()
argument_list|,
name|request
operator|.
name|getUser
argument_list|()
argument_list|,
name|jobToken
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|requestId
operator|=
name|getTaskAttemptId
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|TezChild
operator|.
name|ContainerExecutionResult
name|callInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|threadName
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"canFinish: "
operator|+
name|taskSpec
operator|.
name|getTaskAttemptID
argument_list|()
operator|+
literal|": "
operator|+
name|canFinish
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Unregister from the AMReporter, since the task is now running.
name|this
operator|.
name|amReporter
operator|.
name|unregisterTask
argument_list|(
name|request
operator|.
name|getAmHost
argument_list|()
argument_list|,
name|request
operator|.
name|getAmPort
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO This executor seems unnecessary. Here and TezChild
name|ExecutorService
name|executorReal
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"TezTaskRunner_"
operator|+
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getTaskAttemptIdString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|executor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|executorReal
argument_list|)
expr_stmt|;
comment|// TODO Consolidate this code with TezChild.
name|Stopwatch
name|sw
init|=
operator|new
name|Stopwatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|UserGroupInformation
name|taskUgi
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|request
operator|.
name|getUser
argument_list|()
argument_list|)
decl_stmt|;
name|taskUgi
operator|.
name|addCredentials
argument_list|(
name|credentials
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ByteBuffer
argument_list|>
name|serviceConsumerMetadata
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|serviceConsumerMetadata
operator|.
name|put
argument_list|(
name|TezConstants
operator|.
name|TEZ_SHUFFLE_HANDLER_SERVICE_ID
argument_list|,
name|TezCommonUtils
operator|.
name|convertJobTokenToBytes
argument_list|(
name|jobToken
argument_list|)
argument_list|)
expr_stmt|;
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|startedInputsMap
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|UserGroupInformation
name|taskOwner
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|request
operator|.
name|getTokenIdentifier
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|InetSocketAddress
name|address
init|=
name|NetUtils
operator|.
name|createSocketAddrForHost
argument_list|(
name|request
operator|.
name|getAmHost
argument_list|()
argument_list|,
name|request
operator|.
name|getAmPort
argument_list|()
argument_list|)
decl_stmt|;
name|SecurityUtil
operator|.
name|setTokenService
argument_list|(
name|jobToken
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|taskOwner
operator|.
name|addToken
argument_list|(
name|jobToken
argument_list|)
expr_stmt|;
name|umbilical
operator|=
name|taskOwner
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|LlapTaskUmbilicalProtocol
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LlapTaskUmbilicalProtocol
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|RPC
operator|.
name|getProxy
argument_list|(
name|LlapTaskUmbilicalProtocol
operator|.
name|class
argument_list|,
name|LlapTaskUmbilicalProtocol
operator|.
name|versionID
argument_list|,
name|address
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|taskReporter
operator|=
operator|new
name|LlapTaskReporter
argument_list|(
name|umbilical
argument_list|,
name|confParams
operator|.
name|amHeartbeatIntervalMsMax
argument_list|,
name|confParams
operator|.
name|amCounterHeartbeatInterval
argument_list|,
name|confParams
operator|.
name|amMaxEventsPerHeartbeat
argument_list|,
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
argument_list|,
name|request
operator|.
name|getContainerIdString
argument_list|()
argument_list|)
expr_stmt|;
name|taskRunner
operator|=
operator|new
name|TezTaskRunner
argument_list|(
name|conf
argument_list|,
name|taskUgi
argument_list|,
name|localDirs
argument_list|,
name|taskSpec
argument_list|,
name|request
operator|.
name|getAppAttemptNumber
argument_list|()
argument_list|,
name|serviceConsumerMetadata
argument_list|,
name|envMap
argument_list|,
name|startedInputsMap
argument_list|,
name|taskReporter
argument_list|,
name|executor
argument_list|,
name|objectRegistry
argument_list|,
name|pid
argument_list|,
name|executionContext
argument_list|,
name|memoryAvailable
argument_list|)
expr_stmt|;
name|boolean
name|shouldDie
decl_stmt|;
try|try
block|{
name|shouldDie
operator|=
operator|!
name|taskRunner
operator|.
name|run
argument_list|()
expr_stmt|;
if|if
condition|(
name|shouldDie
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got a shouldDie notification via heartbeats. Shutting down"
argument_list|)
expr_stmt|;
return|return
operator|new
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|(
name|TezChild
operator|.
name|ContainerExecutionResult
operator|.
name|ExitStatus
operator|.
name|SUCCESS
argument_list|,
literal|null
argument_list|,
literal|"Asked to die by the AM"
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
operator|new
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|(
name|TezChild
operator|.
name|ContainerExecutionResult
operator|.
name|ExitStatus
operator|.
name|EXECUTION_FAILURE
argument_list|,
name|e
argument_list|,
literal|"TaskExecutionFailure: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TezException
name|e
parameter_list|)
block|{
return|return
operator|new
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|(
name|TezChild
operator|.
name|ContainerExecutionResult
operator|.
name|ExitStatus
operator|.
name|EXECUTION_FAILURE
argument_list|,
name|e
argument_list|,
literal|"TaskExecutionFailure: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
comment|// TODO Fix UGI and FS Handling. Closing UGI here causes some errors right now.
comment|//        FileSystem.closeAllForUGI(taskUgi);
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"ExecutionTime for Container: "
operator|+
name|request
operator|.
name|getContainerIdString
argument_list|()
operator|+
literal|"="
operator|+
name|sw
operator|.
name|stop
argument_list|()
operator|.
name|elapsedMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"canFinish post completion: "
operator|+
name|taskSpec
operator|.
name|getTaskAttemptID
argument_list|()
operator|+
literal|": "
operator|+
name|canFinish
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|(
name|TezChild
operator|.
name|ContainerExecutionResult
operator|.
name|ExitStatus
operator|.
name|SUCCESS
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Check whether a task can run to completion or may end up blocking on it's sources.    * This currently happens via looking up source state.    * TODO: Eventually, this should lookup the Hive Processor to figure out whether    * it's reached a state where it can finish - especially in cases of failures    * after data has been fetched.    *    * @return    */
specifier|public
name|boolean
name|canFinish
parameter_list|()
block|{
name|List
argument_list|<
name|InputSpec
argument_list|>
name|inputSpecList
init|=
name|taskSpec
operator|.
name|getInputs
argument_list|()
decl_stmt|;
name|boolean
name|canFinish
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|inputSpecList
operator|!=
literal|null
operator|&&
operator|!
name|inputSpecList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|InputSpec
name|inputSpec
range|:
name|inputSpecList
control|)
block|{
if|if
condition|(
name|isSourceOfInterest
argument_list|(
name|inputSpec
argument_list|)
condition|)
block|{
comment|// Lookup the state in the map.
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
name|state
init|=
name|sourceCompletionMap
operator|.
name|get
argument_list|(
name|inputSpec
operator|.
name|getSourceVertexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
operator|&&
name|state
operator|==
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
operator|.
name|S_SUCCEEDED
condition|)
block|{
continue|continue;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cannot finish due to source: "
operator|+
name|inputSpec
operator|.
name|getSourceVertexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|canFinish
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
return|return
name|canFinish
return|;
block|}
specifier|private
name|boolean
name|isSourceOfInterest
parameter_list|(
name|InputSpec
name|inputSpec
parameter_list|)
block|{
name|String
name|inputClassName
init|=
name|inputSpec
operator|.
name|getInputDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
decl_stmt|;
comment|// MRInput is not of interest since it'll always be ready.
return|return
operator|!
name|inputClassName
operator|.
name|equals
argument_list|(
name|MRInputLegacy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|executor
operator|!=
literal|null
condition|)
block|{
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|taskReporter
operator|!=
literal|null
condition|)
block|{
name|taskReporter
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|umbilical
operator|!=
literal|null
condition|)
block|{
name|RPC
operator|.
name|stopProxy
argument_list|(
name|umbilical
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|requestId
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|requestId
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|TaskRunnerCallable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|requestId
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|TaskRunnerCallable
operator|)
name|obj
operator|)
operator|.
name|getRequestId
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|getVertexParallelism
parameter_list|()
block|{
return|return
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getVertexParallelism
argument_list|()
return|;
block|}
specifier|public
name|String
name|getRequestId
parameter_list|()
block|{
return|return
name|requestId
return|;
block|}
specifier|public
name|TaskRunnerCallback
name|getCallback
parameter_list|()
block|{
return|return
operator|new
name|TaskRunnerCallback
argument_list|(
name|request
argument_list|,
name|this
argument_list|)
return|;
block|}
specifier|final
class|class
name|TaskRunnerCallback
implements|implements
name|FutureCallback
argument_list|<
name|TezChild
operator|.
name|ContainerExecutionResult
argument_list|>
block|{
specifier|private
specifier|final
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
decl_stmt|;
specifier|private
specifier|final
name|TaskRunnerCallable
name|taskRunnerCallable
decl_stmt|;
specifier|private
specifier|final
name|String
name|requestId
decl_stmt|;
name|TaskRunnerCallback
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
parameter_list|,
name|TaskRunnerCallable
name|taskRunnerCallable
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|taskRunnerCallable
operator|=
name|taskRunnerCallable
expr_stmt|;
name|this
operator|.
name|requestId
operator|=
name|getTaskIdentifierString
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getRequestId
parameter_list|()
block|{
return|return
name|requestId
return|;
block|}
comment|// TODO Slightly more useful error handling
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|TezChild
operator|.
name|ContainerExecutionResult
name|result
parameter_list|)
block|{
switch|switch
condition|(
name|result
operator|.
name|getExitStatus
argument_list|()
condition|)
block|{
case|case
name|SUCCESS
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully finished: "
operator|+
name|requestId
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrExecutorTotalSuccess
argument_list|()
expr_stmt|;
break|break;
case|case
name|EXECUTION_FAILURE
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to run: "
operator|+
name|requestId
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrExecutorTotalExecutionFailed
argument_list|()
expr_stmt|;
break|break;
case|case
name|INTERRUPTED
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupted while running: "
operator|+
name|requestId
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrExecutorTotalInterrupted
argument_list|()
expr_stmt|;
break|break;
case|case
name|ASKED_TO_DIE
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Asked to die while running: "
operator|+
name|requestId
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|incrExecutorTotalAskedToDie
argument_list|()
expr_stmt|;
break|break;
block|}
name|taskRunnerCallable
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|HistoryLogger
operator|.
name|logFragmentEnd
argument_list|(
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|,
name|request
operator|.
name|getContainerIdString
argument_list|()
argument_list|,
name|executionContext
operator|.
name|getHostName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getDagName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getVertexName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentNumber
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getAttemptNumber
argument_list|()
argument_list|,
name|taskRunnerCallable
operator|.
name|threadName
argument_list|,
name|taskRunnerCallable
operator|.
name|startTime
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|decrExecutorNumQueuedRequests
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"TezTaskRunner execution failed for : "
operator|+
name|getTaskIdentifierString
argument_list|(
name|request
argument_list|)
argument_list|,
name|t
argument_list|)
expr_stmt|;
comment|// TODO HIVE-10236 Report a fatal error over the umbilical
name|taskRunnerCallable
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|HistoryLogger
operator|.
name|logFragmentEnd
argument_list|(
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|,
name|request
operator|.
name|getContainerIdString
argument_list|()
argument_list|,
name|executionContext
operator|.
name|getHostName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getDagName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getVertexName
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentNumber
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getAttemptNumber
argument_list|()
argument_list|,
name|taskRunnerCallable
operator|.
name|threadName
argument_list|,
name|taskRunnerCallable
operator|.
name|startTime
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|decrExecutorNumQueuedRequests
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ConfParams
block|{
specifier|final
name|int
name|amHeartbeatIntervalMsMax
decl_stmt|;
specifier|final
name|long
name|amCounterHeartbeatInterval
decl_stmt|;
specifier|final
name|int
name|amMaxEventsPerHeartbeat
decl_stmt|;
specifier|public
name|ConfParams
parameter_list|(
name|int
name|amHeartbeatIntervalMsMax
parameter_list|,
name|long
name|amCounterHeartbeatInterval
parameter_list|,
name|int
name|amMaxEventsPerHeartbeat
parameter_list|)
block|{
name|this
operator|.
name|amHeartbeatIntervalMsMax
operator|=
name|amHeartbeatIntervalMsMax
expr_stmt|;
name|this
operator|.
name|amCounterHeartbeatInterval
operator|=
name|amCounterHeartbeatInterval
expr_stmt|;
name|this
operator|.
name|amMaxEventsPerHeartbeat
operator|=
name|amMaxEventsPerHeartbeat
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getTaskIdentifierString
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"AppId="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", containerId="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getContainerIdString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", Dag="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getDagName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", Vertex="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getVertexName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", FragmentNum="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentNumber
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", Attempt="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getAttemptNumber
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|String
name|getTaskAttemptId
parameter_list|(
name|SubmitWorkRequestProto
name|request
parameter_list|)
block|{
return|return
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getTaskAttemptIdString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

