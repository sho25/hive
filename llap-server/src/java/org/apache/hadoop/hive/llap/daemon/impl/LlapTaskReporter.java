begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
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
name|LinkedBlockingQueue
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|ReentrantLock
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
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|exception
operator|.
name|ExceptionUtils
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
name|tez
operator|.
name|common
operator|.
name|TezTaskUmbilicalProtocol
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
name|counters
operator|.
name|TezCounters
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
name|RuntimeTask
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
name|events
operator|.
name|TaskAttemptCompletedEvent
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
name|events
operator|.
name|TaskAttemptFailedEvent
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
name|events
operator|.
name|TaskStatusUpdateEvent
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
name|EventMetaData
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
name|EventMetaData
operator|.
name|EventProducerConsumerType
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
name|ErrorReporter
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
comment|/**  * Responsible for communication between tasks running in a Container and the ApplicationMaster.  * Takes care of sending heartbeats (regular and OOB) to the AM - to send generated events, and to  * retrieve events specific to this task.  *  */
end_comment

begin_class
specifier|public
class|class
name|LlapTaskReporter
implements|implements
name|TaskReporterInterface
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
name|LlapTaskReporter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|private
specifier|final
name|long
name|pollInterval
decl_stmt|;
specifier|private
specifier|final
name|long
name|sendCounterInterval
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxEventsToGet
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|requestCounter
decl_stmt|;
specifier|private
specifier|final
name|String
name|containerIdStr
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|heartbeatExecutor
decl_stmt|;
annotation|@
name|VisibleForTesting
name|HeartbeatCallable
name|currentCallable
decl_stmt|;
specifier|public
name|LlapTaskReporter
parameter_list|(
name|LlapTaskUmbilicalProtocol
name|umbilical
parameter_list|,
name|long
name|amPollInterval
parameter_list|,
name|long
name|sendCounterInterval
parameter_list|,
name|int
name|maxEventsToGet
parameter_list|,
name|AtomicLong
name|requestCounter
parameter_list|,
name|String
name|containerIdStr
parameter_list|)
block|{
name|this
operator|.
name|umbilical
operator|=
name|umbilical
expr_stmt|;
name|this
operator|.
name|pollInterval
operator|=
name|amPollInterval
expr_stmt|;
name|this
operator|.
name|sendCounterInterval
operator|=
name|sendCounterInterval
expr_stmt|;
name|this
operator|.
name|maxEventsToGet
operator|=
name|maxEventsToGet
expr_stmt|;
name|this
operator|.
name|requestCounter
operator|=
name|requestCounter
expr_stmt|;
name|this
operator|.
name|containerIdStr
operator|=
name|containerIdStr
expr_stmt|;
name|ExecutorService
name|executor
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
literal|"TaskHeartbeatThread"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|heartbeatExecutor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Register a task to be tracked. Heartbeats will be sent out for this task to fetch events, etc.    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|registerTask
parameter_list|(
name|RuntimeTask
name|task
parameter_list|,
name|ErrorReporter
name|errorReporter
parameter_list|)
block|{
name|currentCallable
operator|=
operator|new
name|HeartbeatCallable
argument_list|(
name|task
argument_list|,
name|umbilical
argument_list|,
name|pollInterval
argument_list|,
name|sendCounterInterval
argument_list|,
name|maxEventsToGet
argument_list|,
name|requestCounter
argument_list|,
name|containerIdStr
argument_list|)
expr_stmt|;
name|ListenableFuture
argument_list|<
name|Boolean
argument_list|>
name|future
init|=
name|heartbeatExecutor
operator|.
name|submit
argument_list|(
name|currentCallable
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
operator|new
name|HeartbeatCallback
argument_list|(
name|errorReporter
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method should always be invoked before setting up heartbeats for another task running in    * the same container.    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|unregisterTask
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|)
block|{
name|currentCallable
operator|.
name|markComplete
argument_list|()
expr_stmt|;
name|currentCallable
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|heartbeatExecutor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
class|class
name|HeartbeatCallable
implements|implements
name|Callable
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|int
name|LOG_COUNTER_START_INTERVAL
init|=
literal|5000
decl_stmt|;
comment|// 5 seconds
specifier|private
specifier|static
specifier|final
name|float
name|LOG_COUNTER_BACKOFF
init|=
literal|1.3f
decl_stmt|;
specifier|private
specifier|final
name|RuntimeTask
name|task
decl_stmt|;
specifier|private
name|EventMetaData
name|updateEventMetadata
decl_stmt|;
specifier|private
specifier|final
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|private
specifier|final
name|long
name|pollInterval
decl_stmt|;
specifier|private
specifier|final
name|long
name|sendCounterInterval
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxEventsToGet
decl_stmt|;
specifier|private
specifier|final
name|String
name|containerIdStr
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|requestCounter
decl_stmt|;
specifier|private
name|LinkedBlockingQueue
argument_list|<
name|TezEvent
argument_list|>
name|eventsToSend
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|TezEvent
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|condition
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
comment|/*      * Keeps track of regular timed heartbeats. Is primarily used as a timing mechanism to send /      * log counters.      */
specifier|private
name|AtomicInteger
name|nonOobHeartbeatCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|int
name|nextHeartbeatNumToLog
init|=
literal|0
decl_stmt|;
comment|/*      * Tracks the last non-OOB heartbeat number at which counters were sent to the AM.       */
specifier|private
name|int
name|prevCounterSendHeartbeatNum
init|=
literal|0
decl_stmt|;
specifier|public
name|HeartbeatCallable
parameter_list|(
name|RuntimeTask
name|task
parameter_list|,
name|LlapTaskUmbilicalProtocol
name|umbilical
parameter_list|,
name|long
name|amPollInterval
parameter_list|,
name|long
name|sendCounterInterval
parameter_list|,
name|int
name|maxEventsToGet
parameter_list|,
name|AtomicLong
name|requestCounter
parameter_list|,
name|String
name|containerIdStr
parameter_list|)
block|{
name|this
operator|.
name|pollInterval
operator|=
name|amPollInterval
expr_stmt|;
name|this
operator|.
name|sendCounterInterval
operator|=
name|sendCounterInterval
expr_stmt|;
name|this
operator|.
name|maxEventsToGet
operator|=
name|maxEventsToGet
expr_stmt|;
name|this
operator|.
name|requestCounter
operator|=
name|requestCounter
expr_stmt|;
name|this
operator|.
name|containerIdStr
operator|=
name|containerIdStr
expr_stmt|;
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
name|this
operator|.
name|umbilical
operator|=
name|umbilical
expr_stmt|;
name|this
operator|.
name|updateEventMetadata
operator|=
operator|new
name|EventMetaData
argument_list|(
name|EventProducerConsumerType
operator|.
name|SYSTEM
argument_list|,
name|task
operator|.
name|getVertexName
argument_list|()
argument_list|,
literal|""
argument_list|,
name|task
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|)
expr_stmt|;
name|nextHeartbeatNumToLog
operator|=
operator|(
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
call|(
name|int
call|)
argument_list|(
name|LOG_COUNTER_START_INTERVAL
operator|/
operator|(
name|amPollInterval
operator|==
literal|0
condition|?
literal|0.000001f
else|:
operator|(
name|float
operator|)
name|amPollInterval
operator|)
argument_list|)
argument_list|)
operator|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Heartbeat only for active tasks. Errors, etc will be reported directly.
while|while
condition|(
operator|!
name|task
operator|.
name|isTaskDone
argument_list|()
operator|&&
operator|!
name|task
operator|.
name|hadFatalError
argument_list|()
condition|)
block|{
name|ResponseWrapper
name|response
init|=
name|heartbeat
argument_list|(
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|shouldDie
condition|)
block|{
comment|// AM sent a shouldDie=true
name|LOG
operator|.
name|info
argument_list|(
literal|"Asked to die via task heartbeat"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
if|if
condition|(
name|response
operator|.
name|numEvents
operator|<
name|maxEventsToGet
condition|)
block|{
comment|// Wait before sending another heartbeat. Otherwise consider as an OOB heartbeat
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|boolean
name|interrupted
init|=
name|condition
operator|.
name|await
argument_list|(
name|pollInterval
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|interrupted
condition|)
block|{
name|nonOobHeartbeatCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|int
name|pendingEventCount
init|=
name|eventsToSend
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|pendingEventCount
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exiting TaskReporter thread with pending queue size="
operator|+
name|pendingEventCount
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * @param eventsArg      * @return      * @throws IOException      *           indicates an RPC communication failure.      * @throws TezException      *           indicates an exception somewhere in the AM.      */
specifier|private
specifier|synchronized
name|ResponseWrapper
name|heartbeat
parameter_list|(
name|Collection
argument_list|<
name|TezEvent
argument_list|>
name|eventsArg
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
if|if
condition|(
name|eventsArg
operator|!=
literal|null
condition|)
block|{
name|eventsToSend
operator|.
name|addAll
argument_list|(
name|eventsArg
argument_list|)
expr_stmt|;
block|}
name|TezEvent
name|updateEvent
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|TezEvent
argument_list|>
name|events
init|=
operator|new
name|ArrayList
argument_list|<
name|TezEvent
argument_list|>
argument_list|()
decl_stmt|;
name|eventsToSend
operator|.
name|drainTo
argument_list|(
name|events
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|task
operator|.
name|isTaskDone
argument_list|()
operator|&&
operator|!
name|task
operator|.
name|hadFatalError
argument_list|()
condition|)
block|{
name|boolean
name|sendCounters
init|=
literal|false
decl_stmt|;
comment|/**          * Increasing the heartbeat interval can delay the delivery of events. Sending just updated          * records would save CPU in DAG AM, but certain counters are updated very frequently. Until          * real time decisions are made based on these counters, it can be sent once per second.          */
comment|// Not completely accurate, since OOB heartbeats could go out.
if|if
condition|(
operator|(
name|nonOobHeartbeatCounter
operator|.
name|get
argument_list|()
operator|-
name|prevCounterSendHeartbeatNum
operator|)
operator|*
name|pollInterval
operator|>=
name|sendCounterInterval
condition|)
block|{
name|sendCounters
operator|=
literal|true
expr_stmt|;
name|prevCounterSendHeartbeatNum
operator|=
name|nonOobHeartbeatCounter
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|updateEvent
operator|=
operator|new
name|TezEvent
argument_list|(
name|getStatusUpdateEvent
argument_list|(
name|sendCounters
argument_list|)
argument_list|,
name|updateEventMetadata
argument_list|)
expr_stmt|;
name|events
operator|.
name|add
argument_list|(
name|updateEvent
argument_list|)
expr_stmt|;
block|}
name|long
name|requestId
init|=
name|requestCounter
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|TezHeartbeatRequest
name|request
init|=
operator|new
name|TezHeartbeatRequest
argument_list|(
name|requestId
argument_list|,
name|events
argument_list|,
name|containerIdStr
argument_list|,
name|task
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|,
name|task
operator|.
name|getEventCounter
argument_list|()
argument_list|,
name|maxEventsToGet
argument_list|)
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
literal|"Sending heartbeat to AM, request="
operator|+
name|request
argument_list|)
expr_stmt|;
block|}
name|maybeLogCounters
argument_list|()
expr_stmt|;
name|TezHeartbeatResponse
name|response
init|=
name|umbilical
operator|.
name|heartbeat
argument_list|(
name|request
argument_list|)
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
literal|"Received heartbeat response from AM, response="
operator|+
name|response
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|response
operator|.
name|shouldDie
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received should die response from AM"
argument_list|)
expr_stmt|;
return|return
operator|new
name|ResponseWrapper
argument_list|(
literal|true
argument_list|,
literal|1
argument_list|)
return|;
block|}
if|if
condition|(
name|response
operator|.
name|getLastRequestId
argument_list|()
operator|!=
name|requestId
condition|)
block|{
throw|throw
operator|new
name|TezException
argument_list|(
literal|"AM and Task out of sync"
operator|+
literal|", responseReqId="
operator|+
name|response
operator|.
name|getLastRequestId
argument_list|()
operator|+
literal|", expectedReqId="
operator|+
name|requestId
argument_list|)
throw|;
block|}
comment|// The same umbilical is used by multiple tasks. Problematic in the case where multiple tasks
comment|// are running using the same umbilical.
name|int
name|numEventsReceived
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|isTaskDone
argument_list|()
operator|||
name|task
operator|.
name|hadFatalError
argument_list|()
condition|)
block|{
if|if
condition|(
name|response
operator|.
name|getEvents
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|response
operator|.
name|getEvents
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Current task already complete, Ignoring all event in"
operator|+
literal|" heartbeat response, eventCount="
operator|+
name|response
operator|.
name|getEvents
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|response
operator|.
name|getEvents
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|response
operator|.
name|getEvents
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
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
literal|"Routing events from heartbeat response to task"
operator|+
literal|", currentTaskAttemptId="
operator|+
name|task
operator|.
name|getTaskAttemptID
argument_list|()
operator|+
literal|", eventCount="
operator|+
name|response
operator|.
name|getEvents
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// This should ideally happen in a separate thread
name|numEventsReceived
operator|=
name|response
operator|.
name|getEvents
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|task
operator|.
name|handleEvents
argument_list|(
name|response
operator|.
name|getEvents
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ResponseWrapper
argument_list|(
literal|false
argument_list|,
name|numEventsReceived
argument_list|)
return|;
block|}
specifier|public
name|void
name|markComplete
parameter_list|()
block|{
comment|// Notify to clear pending events, if any.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|condition
operator|.
name|signal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|maybeLogCounters
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|nonOobHeartbeatCounter
operator|.
name|get
argument_list|()
operator|==
name|nextHeartbeatNumToLog
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Counters: "
operator|+
name|task
operator|.
name|getCounters
argument_list|()
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
name|nextHeartbeatNumToLog
operator|=
call|(
name|int
call|)
argument_list|(
name|nextHeartbeatNumToLog
operator|*
operator|(
name|LOG_COUNTER_BACKOFF
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Sends out final events for task success.      * @param taskAttemptID      * @return      * @throws IOException      *           indicates an RPC communication failure.      * @throws TezException      *           indicates an exception somewhere in the AM.      */
specifier|private
name|boolean
name|taskSucceeded
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
name|TezEvent
name|statusUpdateEvent
init|=
operator|new
name|TezEvent
argument_list|(
name|getStatusUpdateEvent
argument_list|(
literal|true
argument_list|)
argument_list|,
name|updateEventMetadata
argument_list|)
decl_stmt|;
name|TezEvent
name|taskCompletedEvent
init|=
operator|new
name|TezEvent
argument_list|(
operator|new
name|TaskAttemptCompletedEvent
argument_list|()
argument_list|,
name|updateEventMetadata
argument_list|)
decl_stmt|;
return|return
operator|!
name|heartbeat
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|statusUpdateEvent
argument_list|,
name|taskCompletedEvent
argument_list|)
argument_list|)
operator|.
name|shouldDie
return|;
block|}
specifier|private
name|TaskStatusUpdateEvent
name|getStatusUpdateEvent
parameter_list|(
name|boolean
name|sendCounters
parameter_list|)
block|{
return|return
operator|new
name|TaskStatusUpdateEvent
argument_list|(
operator|(
name|sendCounters
condition|?
name|task
operator|.
name|getCounters
argument_list|()
else|:
literal|null
operator|)
argument_list|,
name|task
operator|.
name|getProgress
argument_list|()
argument_list|,
name|task
operator|.
name|getTaskStatistics
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Sends out final events for task failure.      * @param taskAttemptID      * @param t      * @param diagnostics      * @param srcMeta      * @return      * @throws IOException      *           indicates an RPC communication failure.      * @throws TezException      *           indicates an exception somewhere in the AM.      */
specifier|private
name|boolean
name|taskFailed
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|,
name|Throwable
name|t
parameter_list|,
name|String
name|diagnostics
parameter_list|,
name|EventMetaData
name|srcMeta
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
name|TezEvent
name|statusUpdateEvent
init|=
operator|new
name|TezEvent
argument_list|(
name|getStatusUpdateEvent
argument_list|(
literal|true
argument_list|)
argument_list|,
name|updateEventMetadata
argument_list|)
decl_stmt|;
if|if
condition|(
name|diagnostics
operator|==
literal|null
condition|)
block|{
name|diagnostics
operator|=
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|diagnostics
operator|=
name|diagnostics
operator|+
literal|":"
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|TezEvent
name|taskAttemptFailedEvent
init|=
operator|new
name|TezEvent
argument_list|(
operator|new
name|TaskAttemptFailedEvent
argument_list|(
name|diagnostics
argument_list|)
argument_list|,
name|srcMeta
operator|==
literal|null
condition|?
name|updateEventMetadata
else|:
name|srcMeta
argument_list|)
decl_stmt|;
return|return
operator|!
name|heartbeat
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|statusUpdateEvent
argument_list|,
name|taskAttemptFailedEvent
argument_list|)
argument_list|)
operator|.
name|shouldDie
return|;
block|}
specifier|private
name|void
name|addEvents
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|,
name|Collection
argument_list|<
name|TezEvent
argument_list|>
name|events
parameter_list|)
block|{
if|if
condition|(
name|events
operator|!=
literal|null
operator|&&
operator|!
name|events
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|eventsToSend
operator|.
name|addAll
argument_list|(
name|events
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|HeartbeatCallback
implements|implements
name|FutureCallback
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|final
name|ErrorReporter
name|errorReporter
decl_stmt|;
name|HeartbeatCallback
parameter_list|(
name|ErrorReporter
name|errorReporter
parameter_list|)
block|{
name|this
operator|.
name|errorReporter
operator|=
name|errorReporter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Boolean
name|result
parameter_list|)
block|{
if|if
condition|(
name|result
operator|==
literal|false
condition|)
block|{
name|errorReporter
operator|.
name|shutdownRequested
argument_list|()
expr_stmt|;
block|}
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
name|errorReporter
operator|.
name|reportError
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|boolean
name|taskSucceeded
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
return|return
name|currentCallable
operator|.
name|taskSucceeded
argument_list|(
name|taskAttemptID
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|taskFailed
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|,
name|Throwable
name|t
parameter_list|,
name|String
name|diagnostics
parameter_list|,
name|EventMetaData
name|srcMeta
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
return|return
name|currentCallable
operator|.
name|taskFailed
argument_list|(
name|taskAttemptID
argument_list|,
name|t
argument_list|,
name|diagnostics
argument_list|,
name|srcMeta
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|addEvents
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|,
name|Collection
argument_list|<
name|TezEvent
argument_list|>
name|events
parameter_list|)
block|{
name|currentCallable
operator|.
name|addEvents
argument_list|(
name|taskAttemptID
argument_list|,
name|events
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canCommit
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptID
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|umbilical
operator|.
name|canCommit
argument_list|(
name|taskAttemptID
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|ResponseWrapper
block|{
name|boolean
name|shouldDie
decl_stmt|;
name|int
name|numEvents
decl_stmt|;
specifier|private
name|ResponseWrapper
parameter_list|(
name|boolean
name|shouldDie
parameter_list|,
name|int
name|numEvents
parameter_list|)
block|{
name|this
operator|.
name|shouldDie
operator|=
name|shouldDie
expr_stmt|;
name|this
operator|.
name|numEvents
operator|=
name|numEvents
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

