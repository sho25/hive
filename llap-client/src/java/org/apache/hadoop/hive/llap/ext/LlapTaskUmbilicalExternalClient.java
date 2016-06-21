begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ext
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|InetSocketAddress
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|ScheduledThreadPoolExecutor
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
name|protobuf
operator|.
name|InvalidProtocolBufferException
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
name|collections4
operator|.
name|ListUtils
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
name|conf
operator|.
name|HiveConf
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
name|QueryIdentifierProto
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
name|SignableVertexSpec
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
name|SubmissionStateProto
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
name|SubmitWorkResponseProto
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
name|VertexOrBinary
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
name|security
operator|.
name|LlapTokenIdentifier
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
name|tez
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
name|hive
operator|.
name|llap
operator|.
name|tez
operator|.
name|LlapProtocolClientProxy
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
name|helpers
operator|.
name|LlapTaskUmbilicalServer
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
name|ipc
operator|.
name|ProtocolSignature
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
name|EventType
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
name|TezEvent
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
name|TezHeartbeatRequest
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
name|TezHeartbeatResponse
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
specifier|public
class|class
name|LlapTaskUmbilicalExternalClient
extends|extends
name|AbstractService
implements|implements
name|Closeable
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
name|LlapTaskUmbilicalExternalClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapProtocolClientProxy
name|communicator
decl_stmt|;
specifier|private
specifier|volatile
name|LlapTaskUmbilicalServer
name|llapTaskUmbilicalServer
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|protected
specifier|final
name|String
name|tokenIdentifier
decl_stmt|;
specifier|protected
specifier|final
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|sessionToken
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|PendingEventData
argument_list|>
name|pendingEvents
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|TaskHeartbeatInfo
argument_list|>
name|registeredTasks
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|TaskHeartbeatInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|LlapTaskUmbilicalExternalResponder
name|responder
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|ScheduledThreadPoolExecutor
name|timer
decl_stmt|;
specifier|private
specifier|final
name|long
name|connectionTimeout
decl_stmt|;
specifier|private
specifier|static
class|class
name|TaskHeartbeatInfo
block|{
specifier|final
name|String
name|taskAttemptId
decl_stmt|;
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|final
name|int
name|port
decl_stmt|;
specifier|final
name|AtomicLong
name|lastHeartbeat
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|public
name|TaskHeartbeatInfo
parameter_list|(
name|String
name|taskAttemptId
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|taskAttemptId
operator|=
name|taskAttemptId
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|lastHeartbeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|PendingEventData
block|{
specifier|final
name|TaskHeartbeatInfo
name|heartbeatInfo
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TezEvent
argument_list|>
name|tezEvents
decl_stmt|;
specifier|public
name|PendingEventData
parameter_list|(
name|TaskHeartbeatInfo
name|heartbeatInfo
parameter_list|,
name|List
argument_list|<
name|TezEvent
argument_list|>
name|tezEvents
parameter_list|)
block|{
name|this
operator|.
name|heartbeatInfo
operator|=
name|heartbeatInfo
expr_stmt|;
name|this
operator|.
name|tezEvents
operator|=
name|tezEvents
expr_stmt|;
block|}
block|}
specifier|public
name|LlapTaskUmbilicalExternalClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tokenIdentifier
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|sessionToken
parameter_list|,
name|LlapTaskUmbilicalExternalResponder
name|responder
parameter_list|,
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|llapToken
parameter_list|)
block|{
name|super
argument_list|(
name|LlapTaskUmbilicalExternalClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|umbilical
operator|=
operator|new
name|LlapTaskUmbilicalExternalImpl
argument_list|()
expr_stmt|;
name|this
operator|.
name|tokenIdentifier
operator|=
name|tokenIdentifier
expr_stmt|;
name|this
operator|.
name|sessionToken
operator|=
name|sessionToken
expr_stmt|;
name|this
operator|.
name|responder
operator|=
name|responder
expr_stmt|;
name|this
operator|.
name|timer
operator|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectionTimeout
operator|=
literal|3
operator|*
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_TIMEOUT_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
comment|// Add support for configurable threads, however 1 should always be enough.
name|this
operator|.
name|communicator
operator|=
operator|new
name|LlapProtocolClientProxy
argument_list|(
literal|1
argument_list|,
name|conf
argument_list|,
name|llapToken
argument_list|)
expr_stmt|;
name|this
operator|.
name|communicator
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
throws|throws
name|IOException
block|{
comment|// If we use a single server for multiple external clients, then consider using more than one handler.
name|int
name|numHandlers
init|=
literal|1
decl_stmt|;
name|llapTaskUmbilicalServer
operator|=
operator|new
name|LlapTaskUmbilicalServer
argument_list|(
name|conf
argument_list|,
name|umbilical
argument_list|,
name|numHandlers
argument_list|,
name|tokenIdentifier
argument_list|,
name|sessionToken
argument_list|)
expr_stmt|;
name|communicator
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
block|{
name|llapTaskUmbilicalServer
operator|.
name|shutdownServer
argument_list|()
expr_stmt|;
name|timer
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|communicator
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|communicator
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|InetSocketAddress
name|getAddress
parameter_list|()
block|{
return|return
name|llapTaskUmbilicalServer
operator|.
name|getAddress
argument_list|()
return|;
block|}
comment|/**    * Submit the work for actual execution.    * @throws InvalidProtocolBufferException     */
specifier|public
name|void
name|submitWork
parameter_list|(
name|SubmitWorkRequestProto
name|request
parameter_list|,
name|String
name|llapHost
parameter_list|,
name|int
name|llapPort
parameter_list|)
block|{
comment|// Register the pending events to be sent for this spec.
name|VertexOrBinary
name|vob
init|=
name|request
operator|.
name|getWorkSpec
argument_list|()
decl_stmt|;
assert|assert
name|vob
operator|.
name|hasVertexBinary
argument_list|()
operator|!=
name|vob
operator|.
name|hasVertex
argument_list|()
assert|;
name|SignableVertexSpec
name|vertex
init|=
literal|null
decl_stmt|;
try|try
block|{
name|vertex
operator|=
name|vob
operator|.
name|hasVertex
argument_list|()
condition|?
name|vob
operator|.
name|getVertex
argument_list|()
else|:
name|SignableVertexSpec
operator|.
name|parseFrom
argument_list|(
name|vob
operator|.
name|getVertexBinary
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|QueryIdentifierProto
name|queryIdentifierProto
init|=
name|vertex
operator|.
name|getQueryIdentifier
argument_list|()
decl_stmt|;
name|TezTaskAttemptID
name|attemptId
init|=
name|Converters
operator|.
name|createTaskAttemptId
argument_list|(
name|queryIdentifierProto
argument_list|,
name|vertex
operator|.
name|getVertexIndex
argument_list|()
argument_list|,
name|request
operator|.
name|getFragmentNumber
argument_list|()
argument_list|,
name|request
operator|.
name|getAttemptNumber
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|fragmentId
init|=
name|attemptId
operator|.
name|toString
argument_list|()
decl_stmt|;
name|pendingEvents
operator|.
name|putIfAbsent
argument_list|(
name|fragmentId
argument_list|,
operator|new
name|PendingEventData
argument_list|(
operator|new
name|TaskHeartbeatInfo
argument_list|(
name|fragmentId
argument_list|,
name|llapHost
argument_list|,
name|llapPort
argument_list|)
argument_list|,
name|Lists
operator|.
expr|<
name|TezEvent
operator|>
name|newArrayList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Setup timer task to check for hearbeat timeouts
name|timer
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|HeartbeatCheckTask
argument_list|()
argument_list|,
name|connectionTimeout
argument_list|,
name|connectionTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
comment|// Send out the actual SubmitWorkRequest
name|communicator
operator|.
name|sendSubmitWork
argument_list|(
name|request
argument_list|,
name|llapHost
argument_list|,
name|llapPort
argument_list|,
operator|new
name|LlapProtocolClientProxy
operator|.
name|ExecuteRequestCallback
argument_list|<
name|SubmitWorkResponseProto
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|setResponse
parameter_list|(
name|SubmitWorkResponseProto
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|hasSubmissionState
argument_list|()
condition|)
block|{
if|if
condition|(
name|response
operator|.
name|getSubmissionState
argument_list|()
operator|.
name|equals
argument_list|(
name|SubmissionStateProto
operator|.
name|REJECTED
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Fragment: "
operator|+
name|fragmentId
operator|+
literal|" rejected. Server Busy."
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
if|if
condition|(
name|responder
operator|!=
literal|null
condition|)
block|{
name|Throwable
name|err
init|=
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|responder
operator|.
name|submissionFailed
argument_list|(
name|fragmentId
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|indicateError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Failed to submit: "
operator|+
name|fragmentId
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|Throwable
name|err
init|=
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
decl_stmt|;
name|responder
operator|.
name|submissionFailed
argument_list|(
name|fragmentId
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateHeartbeatInfo
parameter_list|(
name|String
name|taskAttemptId
parameter_list|)
block|{
name|int
name|updateCount
init|=
literal|0
decl_stmt|;
name|PendingEventData
name|pendingEventData
init|=
name|pendingEvents
operator|.
name|get
argument_list|(
name|taskAttemptId
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingEventData
operator|!=
literal|null
condition|)
block|{
name|pendingEventData
operator|.
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|updateCount
operator|++
expr_stmt|;
block|}
name|TaskHeartbeatInfo
name|heartbeatInfo
init|=
name|registeredTasks
operator|.
name|get
argument_list|(
name|taskAttemptId
argument_list|)
decl_stmt|;
if|if
condition|(
name|heartbeatInfo
operator|!=
literal|null
condition|)
block|{
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|updateCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|updateCount
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No tasks found for heartbeat from taskAttemptId "
operator|+
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|updateHeartbeatInfo
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|int
name|updateCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|pendingEvents
operator|.
name|keySet
argument_list|()
control|)
block|{
name|PendingEventData
name|pendingEventData
init|=
name|pendingEvents
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingEventData
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|pendingEventData
operator|.
name|heartbeatInfo
operator|.
name|hostname
operator|.
name|equals
argument_list|(
name|hostname
argument_list|)
operator|&&
name|pendingEventData
operator|.
name|heartbeatInfo
operator|.
name|port
operator|==
name|port
condition|)
block|{
name|pendingEventData
operator|.
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|updateCount
operator|++
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|key
range|:
name|registeredTasks
operator|.
name|keySet
argument_list|()
control|)
block|{
name|TaskHeartbeatInfo
name|heartbeatInfo
init|=
name|registeredTasks
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|heartbeatInfo
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|heartbeatInfo
operator|.
name|hostname
operator|.
name|equals
argument_list|(
name|hostname
argument_list|)
operator|&&
name|heartbeatInfo
operator|.
name|port
operator|==
name|port
condition|)
block|{
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|updateCount
operator|++
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|updateCount
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No tasks found for heartbeat from hostname "
operator|+
name|hostname
operator|+
literal|", port "
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|HeartbeatCheckTask
implements|implements
name|Runnable
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|timedOutTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Check both pending and registered tasks for timeouts
for|for
control|(
name|String
name|key
range|:
name|pendingEvents
operator|.
name|keySet
argument_list|()
control|)
block|{
name|PendingEventData
name|pendingEventData
init|=
name|pendingEvents
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingEventData
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|currentTime
operator|-
name|pendingEventData
operator|.
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|get
argument_list|()
operator|>=
name|connectionTimeout
condition|)
block|{
name|timedOutTasks
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|timedOutTask
range|:
name|timedOutTasks
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Pending taskAttemptId "
operator|+
name|timedOutTask
operator|+
literal|" timed out"
argument_list|)
expr_stmt|;
name|responder
operator|.
name|heartbeatTimeout
argument_list|(
name|timedOutTask
argument_list|)
expr_stmt|;
name|pendingEvents
operator|.
name|remove
argument_list|(
name|timedOutTask
argument_list|)
expr_stmt|;
block|}
name|timedOutTasks
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|key
range|:
name|registeredTasks
operator|.
name|keySet
argument_list|()
control|)
block|{
name|TaskHeartbeatInfo
name|heartbeatInfo
init|=
name|registeredTasks
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|heartbeatInfo
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|currentTime
operator|-
name|heartbeatInfo
operator|.
name|lastHeartbeat
operator|.
name|get
argument_list|()
operator|>=
name|connectionTimeout
condition|)
block|{
name|timedOutTasks
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|timedOutTask
range|:
name|timedOutTasks
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running taskAttemptId "
operator|+
name|timedOutTask
operator|+
literal|" timed out"
argument_list|)
expr_stmt|;
name|responder
operator|.
name|heartbeatTimeout
argument_list|(
name|timedOutTask
argument_list|)
expr_stmt|;
name|registeredTasks
operator|.
name|remove
argument_list|(
name|timedOutTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
interface|interface
name|LlapTaskUmbilicalExternalResponder
block|{
name|void
name|submissionFailed
parameter_list|(
name|String
name|fragmentId
parameter_list|,
name|Throwable
name|throwable
parameter_list|)
function_decl|;
name|void
name|heartbeat
parameter_list|(
name|TezHeartbeatRequest
name|request
parameter_list|)
function_decl|;
name|void
name|taskKilled
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
function_decl|;
name|void
name|heartbeatTimeout
parameter_list|(
name|String
name|fragmentId
parameter_list|)
function_decl|;
block|}
comment|// Ideally, the server should be shared across all client sessions running on the same node.
specifier|private
class|class
name|LlapTaskUmbilicalExternalImpl
implements|implements
name|LlapTaskUmbilicalProtocol
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|canCommit
parameter_list|(
name|TezTaskAttemptID
name|taskid
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Expecting only a single instance of a task to be running.
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|TezHeartbeatResponse
name|heartbeat
parameter_list|(
name|TezHeartbeatRequest
name|request
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
comment|// Keep-alive information. The client should be informed and will have to take care of re-submitting the work.
comment|// Some parts of fault tolerance go here.
comment|// This also provides completion information, and a possible notification when task actually starts running (first heartbeat)
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
literal|"Received heartbeat from container, request="
operator|+
name|request
argument_list|)
expr_stmt|;
block|}
comment|// Incoming events can be ignored until the point when shuffle needs to be handled, instead of just scans.
name|TezHeartbeatResponse
name|response
init|=
operator|new
name|TezHeartbeatResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|setLastRequestId
argument_list|(
name|request
operator|.
name|getRequestId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assuming TaskAttemptId and FragmentIdentifierString are the same. Verify this.
name|TezTaskAttemptID
name|taskAttemptId
init|=
name|request
operator|.
name|getCurrentTaskAttemptID
argument_list|()
decl_stmt|;
name|String
name|taskAttemptIdString
init|=
name|taskAttemptId
operator|.
name|toString
argument_list|()
decl_stmt|;
name|updateHeartbeatInfo
argument_list|(
name|taskAttemptIdString
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TezEvent
argument_list|>
name|tezEvents
init|=
literal|null
decl_stmt|;
name|PendingEventData
name|pendingEventData
init|=
name|pendingEvents
operator|.
name|remove
argument_list|(
name|taskAttemptIdString
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingEventData
operator|==
literal|null
condition|)
block|{
name|tezEvents
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
comment|// If this heartbeat was not from a pending event and it's not in our list of registered tasks,
if|if
condition|(
operator|!
name|registeredTasks
operator|.
name|containsKey
argument_list|(
name|taskAttemptIdString
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unexpected heartbeat from "
operator|+
name|taskAttemptIdString
argument_list|)
expr_stmt|;
name|response
operator|.
name|setShouldDie
argument_list|()
expr_stmt|;
comment|// Do any of the other fields need to be set?
return|return
name|response
return|;
block|}
block|}
else|else
block|{
name|tezEvents
operator|=
name|pendingEventData
operator|.
name|tezEvents
expr_stmt|;
comment|// Tasks removed from the pending list should then be added to the registered list.
name|registeredTasks
operator|.
name|put
argument_list|(
name|taskAttemptIdString
argument_list|,
name|pendingEventData
operator|.
name|heartbeatInfo
argument_list|)
expr_stmt|;
block|}
name|response
operator|.
name|setLastRequestId
argument_list|(
name|request
operator|.
name|getRequestId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Irrelevant from eventIds. This can be tracked in the AM itself, instead of polluting the task.
comment|// Also since we have all the MRInput events here - they'll all be sent in together.
name|response
operator|.
name|setNextFromEventId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Irrelevant. See comment above.
name|response
operator|.
name|setNextPreRoutedEventId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|//Irrelevant. See comment above.
name|response
operator|.
name|setEvents
argument_list|(
name|tezEvents
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TezEvent
argument_list|>
name|inEvents
init|=
name|request
operator|.
name|getEvents
argument_list|()
decl_stmt|;
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
literal|"Heartbeat from "
operator|+
name|taskAttemptIdString
operator|+
literal|" events: "
operator|+
operator|(
name|inEvents
operator|!=
literal|null
condition|?
name|inEvents
operator|.
name|size
argument_list|()
else|:
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TezEvent
name|tezEvent
range|:
name|ListUtils
operator|.
name|emptyIfNull
argument_list|(
name|inEvents
argument_list|)
control|)
block|{
name|EventType
name|eventType
init|=
name|tezEvent
operator|.
name|getEventType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|eventType
condition|)
block|{
case|case
name|TASK_ATTEMPT_COMPLETED_EVENT
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"Task completed event for "
operator|+
name|taskAttemptIdString
argument_list|)
expr_stmt|;
name|registeredTasks
operator|.
name|remove
argument_list|(
name|taskAttemptIdString
argument_list|)
expr_stmt|;
break|break;
case|case
name|TASK_ATTEMPT_FAILED_EVENT
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"Task failed event for "
operator|+
name|taskAttemptIdString
argument_list|)
expr_stmt|;
name|registeredTasks
operator|.
name|remove
argument_list|(
name|taskAttemptIdString
argument_list|)
expr_stmt|;
break|break;
case|case
name|TASK_STATUS_UPDATE_EVENT
case|:
comment|// If we want to handle counters
name|LOG
operator|.
name|debug
argument_list|(
literal|"Task update event for "
operator|+
name|taskAttemptIdString
argument_list|)
expr_stmt|;
break|break;
default|default:
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unhandled event type "
operator|+
name|eventType
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
comment|// Pass the request on to the responder
try|try
block|{
if|if
condition|(
name|responder
operator|!=
literal|null
condition|)
block|{
name|responder
operator|.
name|heartbeat
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error during responder execution"
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeHeartbeat
parameter_list|(
name|Text
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|updateHeartbeatInfo
argument_list|(
name|hostname
operator|.
name|toString
argument_list|()
argument_list|,
name|port
argument_list|)
expr_stmt|;
comment|// No need to propagate to this to the responder
block|}
annotation|@
name|Override
specifier|public
name|void
name|taskKilled
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|taskAttemptIdString
init|=
name|taskAttemptId
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Task killed - "
operator|+
name|taskAttemptIdString
argument_list|)
expr_stmt|;
name|registeredTasks
operator|.
name|remove
argument_list|(
name|taskAttemptIdString
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|responder
operator|!=
literal|null
condition|)
block|{
name|responder
operator|.
name|taskKilled
argument_list|(
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error during responder execution"
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|int
name|clientMethodsHash
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ProtocolSignature
operator|.
name|getProtocolSignature
argument_list|(
name|this
argument_list|,
name|protocol
argument_list|,
name|clientVersion
argument_list|,
name|clientMethodsHash
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

