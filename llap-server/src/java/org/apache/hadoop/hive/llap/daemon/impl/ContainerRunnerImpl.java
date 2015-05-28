begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RejectedExecutionException
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
name|AtomicReference
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
name|llap
operator|.
name|LlapNodeId
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
name|ContainerRunner
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
name|FragmentCompletionHandler
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
name|KilledTaskHandler
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
name|FragmentRuntimeInfo
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
name|FragmentSpecProto
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
name|GroupInputSpecProto
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
name|IOSpecProto
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
name|QueryCompleteRequestProto
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
name|SourceStateUpdatedRequestProto
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
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|TerminateFragmentRequestProto
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
name|shufflehandler
operator|.
name|ShuffleHandler
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
name|DataInputBuffer
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
name|service
operator|.
name|AbstractService
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
name|yarn
operator|.
name|api
operator|.
name|ApplicationConstants
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
name|yarn
operator|.
name|util
operator|.
name|AuxiliaryServiceHelper
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
name|NDC
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
name|TezConfiguration
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
name|records
operator|.
name|TezTaskAttemptID
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
name|ExecutionContextImpl
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
comment|// TODO Convert this to a CompositeService
end_comment

begin_class
specifier|public
class|class
name|ContainerRunnerImpl
extends|extends
name|AbstractService
implements|implements
name|ContainerRunner
implements|,
name|FragmentCompletionHandler
block|{
comment|// TODO Setup a set of threads to process incoming requests.
comment|// Make sure requests for a single dag/query are handled by the same thread
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
name|ContainerRunnerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|THREAD_NAME_FORMAT_PREFIX
init|=
literal|"ContainerExecutor "
decl_stmt|;
specifier|private
specifier|volatile
name|AMReporter
name|amReporter
decl_stmt|;
specifier|private
specifier|final
name|QueryTracker
name|queryTracker
decl_stmt|;
specifier|private
specifier|final
name|Scheduler
argument_list|<
name|TaskRunnerCallable
argument_list|>
name|executorService
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|localAddress
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|localEnv
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|long
name|memoryPerExecutor
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonExecutorMetrics
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|TaskRunnerCallable
operator|.
name|ConfParams
name|confParams
decl_stmt|;
specifier|private
specifier|final
name|KilledTaskHandler
name|killedTaskHandler
init|=
operator|new
name|KilledTaskHandlerImpl
argument_list|()
decl_stmt|;
specifier|public
name|ContainerRunnerImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numExecutors
parameter_list|,
name|int
name|waitQueueSize
parameter_list|,
name|boolean
name|enablePreemption
parameter_list|,
name|String
index|[]
name|localDirsBase
parameter_list|,
name|int
name|localShufflePort
parameter_list|,
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|localAddress
parameter_list|,
name|long
name|totalMemoryAvailableBytes
parameter_list|,
name|LlapDaemonExecutorMetrics
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
literal|"ContainerRunnerImpl"
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|numExecutors
operator|>
literal|0
argument_list|,
literal|"Invalid number of executors: "
operator|+
name|numExecutors
operator|+
literal|". Must be> 0"
argument_list|)
expr_stmt|;
name|this
operator|.
name|localAddress
operator|=
name|localAddress
expr_stmt|;
name|this
operator|.
name|queryTracker
operator|=
operator|new
name|QueryTracker
argument_list|(
name|conf
argument_list|,
name|localDirsBase
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorService
operator|=
operator|new
name|TaskExecutorService
argument_list|(
name|numExecutors
argument_list|,
name|waitQueueSize
argument_list|,
name|enablePreemption
argument_list|)
expr_stmt|;
name|AuxiliaryServiceHelper
operator|.
name|setServiceDataIntoEnv
argument_list|(
name|TezConstants
operator|.
name|TEZ_SHUFFLE_HANDLER_SERVICE_ID
argument_list|,
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
operator|.
name|putInt
argument_list|(
name|localShufflePort
argument_list|)
argument_list|,
name|localEnv
argument_list|)
expr_stmt|;
comment|// 80% of memory considered for accounted buffers. Rest for objects.
comment|// TODO Tune this based on the available size.
name|this
operator|.
name|memoryPerExecutor
operator|=
call|(
name|long
call|)
argument_list|(
name|totalMemoryAvailableBytes
operator|*
literal|0.8
operator|/
operator|(
name|float
operator|)
name|numExecutors
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|confParams
operator|=
operator|new
name|TaskRunnerCallable
operator|.
name|ConfParams
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_TASK_AM_HEARTBEAT_INTERVAL_MS
argument_list|,
name|TezConfiguration
operator|.
name|TEZ_TASK_AM_HEARTBEAT_INTERVAL_MS_DEFAULT
argument_list|)
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_TASK_AM_HEARTBEAT_COUNTER_INTERVAL_MS
argument_list|,
name|TezConfiguration
operator|.
name|TEZ_TASK_AM_HEARTBEAT_COUNTER_INTERVAL_MS_DEFAULT
argument_list|)
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_TASK_MAX_EVENTS_PER_HEARTBEAT
argument_list|,
name|TezConfiguration
operator|.
name|TEZ_TASK_MAX_EVENTS_PER_HEARTBEAT_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ContainerRunnerImpl config: "
operator|+
literal|"memoryPerExecutorDerviced="
operator|+
name|memoryPerExecutor
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|serviceInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|queryTracker
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
block|{
comment|// The node id will only be available at this point, since the server has been started in LlapDaemon
name|queryTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|LlapNodeId
name|llapNodeId
init|=
name|LlapNodeId
operator|.
name|getInstance
argument_list|(
name|localAddress
operator|.
name|get
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|localAddress
operator|.
name|get
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|amReporter
operator|=
operator|new
name|AMReporter
argument_list|(
name|llapNodeId
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|amReporter
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|amReporter
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|amReporter
operator|!=
literal|null
condition|)
block|{
name|amReporter
operator|.
name|stop
argument_list|()
expr_stmt|;
name|amReporter
operator|=
literal|null
expr_stmt|;
block|}
name|queryTracker
operator|.
name|stop
argument_list|()
expr_stmt|;
name|super
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|submitWork
parameter_list|(
name|SubmitWorkRequestProto
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|HistoryLogger
operator|.
name|logFragmentStart
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
name|localAddress
operator|.
name|get
argument_list|()
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
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Queueing container for execution: "
operator|+
name|stringifySubmitRequest
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
comment|// This is the start of container-annotated logging.
comment|// TODO Reduce the length of this string. Way too verbose at the moment.
name|String
name|ndcContextString
init|=
name|request
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentIdentifierString
argument_list|()
decl_stmt|;
name|NDC
operator|.
name|push
argument_list|(
name|ndcContextString
argument_list|)
expr_stmt|;
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// TODO What else is required in this environment map.
name|env
operator|.
name|putAll
argument_list|(
name|localEnv
argument_list|)
expr_stmt|;
name|env
operator|.
name|put
argument_list|(
name|ApplicationConstants
operator|.
name|Environment
operator|.
name|USER
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|FragmentSpecProto
name|fragmentSpec
init|=
name|request
operator|.
name|getFragmentSpec
argument_list|()
decl_stmt|;
name|TezTaskAttemptID
name|taskAttemptId
init|=
name|TezTaskAttemptID
operator|.
name|fromString
argument_list|(
name|fragmentSpec
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|dagIdentifier
init|=
name|taskAttemptId
operator|.
name|getTaskID
argument_list|()
operator|.
name|getVertexID
argument_list|()
operator|.
name|getDAGId
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
name|QueryFragmentInfo
name|fragmentInfo
init|=
name|queryTracker
operator|.
name|registerFragment
argument_list|(
literal|null
argument_list|,
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|,
name|fragmentSpec
operator|.
name|getDagName
argument_list|()
argument_list|,
name|dagIdentifier
argument_list|,
name|fragmentSpec
operator|.
name|getVertexName
argument_list|()
argument_list|,
name|fragmentSpec
operator|.
name|getFragmentNumber
argument_list|()
argument_list|,
name|fragmentSpec
operator|.
name|getAttemptNumber
argument_list|()
argument_list|,
name|request
operator|.
name|getUser
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentSpec
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|localDirs
init|=
name|fragmentInfo
operator|.
name|getLocalDirs
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|localDirs
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
literal|"Dirs are: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|localDirs
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// May need to setup localDir for re-localization, which is usually setup as Environment.PWD.
comment|// Used for re-localization, to add the user specified configuration (conf_pb_binary_stream)
name|Credentials
name|credentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|DataInputBuffer
name|dib
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tokenBytes
init|=
name|request
operator|.
name|getCredentialsBinary
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|tokenBytes
argument_list|,
name|tokenBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|credentials
operator|.
name|readTokenStorageStream
argument_list|(
name|dib
argument_list|)
expr_stmt|;
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|jobToken
init|=
name|TokenCache
operator|.
name|getSessionToken
argument_list|(
name|credentials
argument_list|)
decl_stmt|;
comment|// TODO Unregistering does not happen at the moment, since there's no signals on when an app completes.
name|LOG
operator|.
name|info
argument_list|(
literal|"DEBUG: Registering request with the ShuffleHandler"
argument_list|)
expr_stmt|;
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|registerDag
argument_list|(
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|,
name|dagIdentifier
argument_list|,
name|jobToken
argument_list|,
name|request
operator|.
name|getUser
argument_list|()
argument_list|,
name|localDirs
argument_list|)
expr_stmt|;
name|TaskRunnerCallable
name|callable
init|=
operator|new
name|TaskRunnerCallable
argument_list|(
name|request
argument_list|,
name|fragmentInfo
argument_list|,
operator|new
name|Configuration
argument_list|(
name|getConfig
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ExecutionContextImpl
argument_list|(
name|localAddress
operator|.
name|get
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
argument_list|,
name|env
argument_list|,
name|credentials
argument_list|,
name|memoryPerExecutor
argument_list|,
name|amReporter
argument_list|,
name|confParams
argument_list|,
name|metrics
argument_list|,
name|killedTaskHandler
argument_list|,
name|this
argument_list|)
decl_stmt|;
try|try
block|{
name|executorService
operator|.
name|schedule
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e
parameter_list|)
block|{
comment|// Stop tracking the fragment and re-throw the error.
name|fragmentComplete
argument_list|(
name|fragmentInfo
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
name|metrics
operator|.
name|incrExecutorTotalRequestsHandled
argument_list|()
expr_stmt|;
name|metrics
operator|.
name|incrExecutorNumQueuedRequests
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|NDC
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sourceStateUpdated
parameter_list|(
name|SourceStateUpdatedRequestProto
name|request
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing state update: "
operator|+
name|stringifySourceStateUpdateRequest
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|queryTracker
operator|.
name|registerSourceStateChange
argument_list|(
name|request
operator|.
name|getDagName
argument_list|()
argument_list|,
name|request
operator|.
name|getSrcName
argument_list|()
argument_list|,
name|request
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|queryComplete
parameter_list|(
name|QueryCompleteRequestProto
name|request
parameter_list|)
block|{
name|queryTracker
operator|.
name|queryComplete
argument_list|(
literal|null
argument_list|,
name|request
operator|.
name|getDagName
argument_list|()
argument_list|,
name|request
operator|.
name|getDeleteDelay
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|terminateFragment
parameter_list|(
name|TerminateFragmentRequestProto
name|request
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"DBG: Received terminateFragment request for {}"
argument_list|,
name|request
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|killFragment
argument_list|(
name|request
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|stringifySourceStateUpdateRequest
parameter_list|(
name|SourceStateUpdatedRequestProto
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
literal|"dagName="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getDagName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
literal|"sourceName="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getSrcName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
literal|"state="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getState
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
specifier|public
specifier|static
name|String
name|stringifySubmitRequest
parameter_list|(
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
name|FragmentSpecProto
name|fragmentSpec
init|=
name|request
operator|.
name|getFragmentSpec
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"am_details="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getAmHost
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getAmPort
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", taskInfo="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", user="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", appIdString="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getApplicationIdString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", appAttemptNum="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getAppAttemptNumber
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", containerIdString="
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|getContainerIdString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", dagName="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getDagName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", vertexName="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", processor="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", numInputs="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getInputSpecsCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", numOutputs="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getOutputSpecsCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", numGroupedInputs="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentSpec
operator|.
name|getGroupedInputSpecsCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", Inputs={"
argument_list|)
expr_stmt|;
if|if
condition|(
name|fragmentSpec
operator|.
name|getInputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|ioSpec
range|:
name|fragmentSpec
operator|.
name|getInputSpecsList
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", Outputs={"
argument_list|)
expr_stmt|;
if|if
condition|(
name|fragmentSpec
operator|.
name|getOutputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|ioSpec
range|:
name|fragmentSpec
operator|.
name|getOutputSpecsList
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|ioSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", GroupedInputs={"
argument_list|)
expr_stmt|;
if|if
condition|(
name|fragmentSpec
operator|.
name|getGroupedInputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|GroupInputSpecProto
name|group
range|:
name|fragmentSpec
operator|.
name|getGroupedInputSpecsList
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
literal|"groupName="
argument_list|)
operator|.
name|append
argument_list|(
name|group
operator|.
name|getGroupName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", elements="
argument_list|)
operator|.
name|append
argument_list|(
name|group
operator|.
name|getGroupVerticesList
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|group
operator|.
name|getGroupVerticesList
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|FragmentRuntimeInfo
name|fragmentRuntimeInfo
init|=
name|request
operator|.
name|getFragmentRuntimeInfo
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", FragmentRuntimeInfo={"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"taskCount="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentRuntimeInfo
operator|.
name|getNumSelfAndUpstreamTasks
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", completedTaskCount="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentRuntimeInfo
operator|.
name|getNumSelfAndUpstreamCompletedTasks
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", dagStartTime="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentRuntimeInfo
operator|.
name|getDagStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", firstAttemptStartTime="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentRuntimeInfo
operator|.
name|getFirstAttemptStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", currentAttemptStartTime="
argument_list|)
operator|.
name|append
argument_list|(
name|fragmentRuntimeInfo
operator|.
name|getCurrentAttemptStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|fragmentComplete
parameter_list|(
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|)
block|{
name|queryTracker
operator|.
name|fragmentComplete
argument_list|(
name|fragmentInfo
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|KilledTaskHandlerImpl
implements|implements
name|KilledTaskHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|taskKilled
parameter_list|(
name|String
name|amLocation
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|user
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|jobToken
parameter_list|,
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
block|{
name|amReporter
operator|.
name|taskKilled
argument_list|(
name|amLocation
argument_list|,
name|port
argument_list|,
name|user
argument_list|,
name|jobToken
argument_list|,
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

