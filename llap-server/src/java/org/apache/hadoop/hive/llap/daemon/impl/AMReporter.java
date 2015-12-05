begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|javax
operator|.
name|net
operator|.
name|SocketFactory
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
name|CancellationException
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
name|DelayQueue
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
name|Delayed
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicReference
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|configuration
operator|.
name|LlapConfiguration
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
name|QueryFailedHandler
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
name|hadoop
operator|.
name|io
operator|.
name|retry
operator|.
name|RetryPolicies
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
name|retry
operator|.
name|RetryPolicy
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
name|CallableWithNdc
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
name|records
operator|.
name|TezTaskAttemptID
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
comment|/**  * Responsible for communicating with various AMs.  */
end_comment

begin_class
specifier|public
class|class
name|AMReporter
extends|extends
name|AbstractService
block|{
comment|// TODO In case of a failure to heartbeat, tasks for the specific DAG should ideally be KILLED
comment|/*   registrations and un-registrations will happen as and when tasks are submitted or are removed.   reference counting is likely required.    A connection needs to be established to each app master.    Ignore exceptions when communicating with the AM.   At a later point, report back saying the AM is dead so that tasks can be removed from the running queue.    Use a cachedThreadPool so that a few AMs going down does not affect other AppMasters.    Race: When a task completes - it sends out it's message via the regular TaskReporter. The AM after this may run another DAG,   or may die. This may need to be consolidated with the LlapTaskReporter. Try ensuring there's no race between the two.    Single thread which sends heartbeats to AppMasters as events drain off a queue.    */
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
name|AMReporter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|LlapNodeId
name|nodeId
decl_stmt|;
specifier|private
specifier|final
name|QueryFailedHandler
name|queryFailedHandler
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|queueLookupExecutor
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|RetryPolicy
name|retryPolicy
decl_stmt|;
specifier|private
specifier|final
name|long
name|retryTimeout
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
specifier|private
specifier|final
name|DelayQueue
argument_list|<
name|AMNodeInfo
argument_list|>
name|pendingHeartbeatQueeu
init|=
operator|new
name|DelayQueue
argument_list|<>
argument_list|()
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
name|long
name|heartbeatInterval
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|isShutdown
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Tracks appMasters to which heartbeats are being sent. This should not be used for any other
comment|// messages like taskKilled, etc.
specifier|private
specifier|final
name|Map
argument_list|<
name|LlapNodeId
argument_list|,
name|AMNodeInfo
argument_list|>
name|knownAppMasters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|volatile
name|ListenableFuture
argument_list|<
name|Void
argument_list|>
name|queueLookupFuture
decl_stmt|;
specifier|public
name|AMReporter
parameter_list|(
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|localAddress
parameter_list|,
name|QueryFailedHandler
name|queryFailedHandler
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|AMReporter
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|queryFailedHandler
operator|=
name|queryFailedHandler
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|ExecutorService
name|rawExecutor
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
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
literal|"AMReporter %d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|executor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|rawExecutor
argument_list|)
expr_stmt|;
name|ExecutorService
name|rawExecutor2
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
literal|"AMReporterQueueDrainer"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|queueLookupExecutor
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|rawExecutor2
argument_list|)
expr_stmt|;
name|this
operator|.
name|heartbeatInterval
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_HEARTBEAT_INTERVAL_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryTimeout
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_TIMEOUT_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|long
name|retrySleep
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_SLEEP_BETWEEN_RETRIES_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|this
operator|.
name|retryPolicy
operator|=
name|RetryPolicies
operator|.
name|retryUpToMaximumTimeWithFixedSleep
argument_list|(
name|retryTimeout
argument_list|,
name|retrySleep
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|socketFactory
operator|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting up AMReporter with "
operator|+
literal|"heartbeatInterval(ms)="
operator|+
name|heartbeatInterval
operator|+
literal|", retryTime(ms)="
operator|+
name|retryTimeout
operator|+
literal|", retrySleep(ms)="
operator|+
name|retrySleep
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
name|QueueLookupCallable
name|queueDrainerCallable
init|=
operator|new
name|QueueLookupCallable
argument_list|()
decl_stmt|;
name|queueLookupFuture
operator|=
name|queueLookupExecutor
operator|.
name|submit
argument_list|(
name|queueDrainerCallable
argument_list|)
expr_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|queueLookupFuture
argument_list|,
operator|new
name|FutureCallback
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Void
name|result
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"AMReporter QueueDrainer exited"
argument_list|)
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
if|if
condition|(
name|t
operator|instanceof
name|CancellationException
operator|&&
name|isShutdown
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"AMReporter QueueDrainer exited as a result of a cancellation after shutdown"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"AMReporter QueueDrainer exited with error"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|getDefaultUncaughtExceptionHandler
argument_list|()
operator|.
name|uncaughtException
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|nodeId
operator|=
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
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"AMReporter running with NodeId: {}"
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isShutdown
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
name|queueLookupFuture
operator|!=
literal|null
condition|)
block|{
name|queueLookupFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|queueLookupExecutor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopped service: "
operator|+
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|registerTask
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
name|String
name|queryId
parameter_list|,
name|String
name|dagName
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Registering for heartbeat: "
operator|+
name|amLocation
operator|+
literal|":"
operator|+
name|port
operator|+
literal|" for dagName="
operator|+
name|dagName
argument_list|)
expr_stmt|;
block|}
name|AMNodeInfo
name|amNodeInfo
decl_stmt|;
synchronized|synchronized
init|(
name|knownAppMasters
init|)
block|{
name|LlapNodeId
name|amNodeId
init|=
name|LlapNodeId
operator|.
name|getInstance
argument_list|(
name|amLocation
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|amNodeInfo
operator|=
name|knownAppMasters
operator|.
name|get
argument_list|(
name|amNodeId
argument_list|)
expr_stmt|;
if|if
condition|(
name|amNodeInfo
operator|==
literal|null
condition|)
block|{
name|amNodeInfo
operator|=
operator|new
name|AMNodeInfo
argument_list|(
name|amNodeId
argument_list|,
name|user
argument_list|,
name|jobToken
argument_list|,
name|dagName
argument_list|,
name|retryPolicy
argument_list|,
name|retryTimeout
argument_list|,
name|socketFactory
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|knownAppMasters
operator|.
name|put
argument_list|(
name|amNodeId
argument_list|,
name|amNodeInfo
argument_list|)
expr_stmt|;
comment|// Add to the queue only the first time this is registered, and on
comment|// subsequent instances when it's taken off the queue.
name|amNodeInfo
operator|.
name|setNextHeartbeatTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|heartbeatInterval
argument_list|)
expr_stmt|;
name|pendingHeartbeatQueeu
operator|.
name|add
argument_list|(
name|amNodeInfo
argument_list|)
expr_stmt|;
block|}
name|amNodeInfo
operator|.
name|setCurrentDagName
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|amNodeInfo
operator|.
name|incrementAndGetTaskCount
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|unregisterTask
parameter_list|(
name|String
name|amLocation
parameter_list|,
name|int
name|port
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Un-registering for heartbeat: "
operator|+
name|amLocation
operator|+
literal|":"
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
name|AMNodeInfo
name|amNodeInfo
decl_stmt|;
name|LlapNodeId
name|amNodeId
init|=
name|LlapNodeId
operator|.
name|getInstance
argument_list|(
name|amLocation
argument_list|,
name|port
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|knownAppMasters
init|)
block|{
name|amNodeInfo
operator|=
name|knownAppMasters
operator|.
name|get
argument_list|(
name|amNodeId
argument_list|)
expr_stmt|;
if|if
condition|(
name|amNodeInfo
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
operator|(
literal|"Ignoring duplicate unregisterRequest for am at: "
operator|+
name|amLocation
operator|+
literal|":"
operator|+
name|port
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|amNodeInfo
operator|.
name|decrementAndGetTaskCount
argument_list|()
expr_stmt|;
block|}
comment|// Not removing this here. Will be removed when taken off the queue and discovered to have 0
comment|// pending tasks.
block|}
block|}
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
specifier|final
name|String
name|queryId
parameter_list|,
specifier|final
name|String
name|dagName
parameter_list|,
specifier|final
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
block|{
comment|// Not re-using the connection for the AM heartbeat - which may or may not be open by this point.
comment|// knownAppMasters is used for sending heartbeats for queued tasks. Killed messages use a new connection.
name|LlapNodeId
name|amNodeId
init|=
name|LlapNodeId
operator|.
name|getInstance
argument_list|(
name|amLocation
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|AMNodeInfo
name|amNodeInfo
init|=
operator|new
name|AMNodeInfo
argument_list|(
name|amNodeId
argument_list|,
name|user
argument_list|,
name|jobToken
argument_list|,
name|dagName
argument_list|,
name|retryPolicy
argument_list|,
name|retryTimeout
argument_list|,
name|socketFactory
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Even if the service hasn't started up. It's OK to make this invocation since this will
comment|// only happen after the AtomicReference address has been populated. Not adding an additional check.
name|ListenableFuture
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|KillTaskCallable
argument_list|(
name|taskAttemptId
argument_list|,
name|amNodeInfo
argument_list|)
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
operator|new
name|FutureCallback
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Void
name|result
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sent taskKilled for {}"
argument_list|,
name|taskAttemptId
argument_list|)
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
name|warn
argument_list|(
literal|"Failed to send taskKilled for {}. The attempt will likely time out."
argument_list|,
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|QueueLookupCallable
extends|extends
name|CallableWithNdc
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Void
name|callInternal
parameter_list|()
block|{
while|while
condition|(
operator|!
name|isShutdown
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|isInterrupted
argument_list|()
condition|)
block|{
try|try
block|{
specifier|final
name|AMNodeInfo
name|amNodeInfo
init|=
name|pendingHeartbeatQueeu
operator|.
name|take
argument_list|()
decl_stmt|;
if|if
condition|(
name|amNodeInfo
operator|.
name|getTaskCount
argument_list|()
operator|==
literal|0
operator|||
name|amNodeInfo
operator|.
name|hasAmFailed
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|knownAppMasters
init|)
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
literal|"Removing am {} with last associated dag{} from heartbeat with taskCount={}, amFailed={}"
argument_list|,
name|amNodeInfo
operator|.
name|amNodeId
argument_list|,
name|amNodeInfo
operator|.
name|getCurrentDagName
argument_list|()
argument_list|,
name|amNodeInfo
operator|.
name|getTaskCount
argument_list|()
argument_list|,
name|amNodeInfo
operator|.
name|hasAmFailed
argument_list|()
argument_list|,
name|amNodeInfo
argument_list|)
expr_stmt|;
block|}
name|knownAppMasters
operator|.
name|remove
argument_list|(
name|amNodeInfo
operator|.
name|amNodeId
argument_list|)
expr_stmt|;
block|}
name|amNodeInfo
operator|.
name|stopUmbilical
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Add back to the queue for the next heartbeat, and schedule the actual heartbeat
name|long
name|next
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|heartbeatInterval
decl_stmt|;
name|amNodeInfo
operator|.
name|setNextHeartbeatTime
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|pendingHeartbeatQueeu
operator|.
name|add
argument_list|(
name|amNodeInfo
argument_list|)
expr_stmt|;
name|ListenableFuture
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|AMHeartbeatCallable
argument_list|(
name|amNodeInfo
argument_list|)
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
operator|new
name|FutureCallback
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Void
name|result
parameter_list|)
block|{
comment|// Nothing to do.
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
name|String
name|currentDagName
init|=
name|amNodeInfo
operator|.
name|getCurrentDagName
argument_list|()
decl_stmt|;
name|amNodeInfo
operator|.
name|setAmFailed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Heartbeat failed to AM {}. Killing all other tasks for the query={}"
argument_list|,
name|amNodeInfo
operator|.
name|amNodeId
argument_list|,
name|currentDagName
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|queryFailedHandler
operator|.
name|queryFailed
argument_list|(
literal|null
argument_list|,
name|currentDagName
argument_list|)
expr_stmt|;
block|}
block|}
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
if|if
condition|(
name|isShutdown
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"QueueLookup thread interrupted after shutdown"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received unexpected interrupt while waiting on heartbeat queue"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
class|class
name|KillTaskCallable
extends|extends
name|CallableWithNdc
argument_list|<
name|Void
argument_list|>
block|{
specifier|final
name|AMNodeInfo
name|amNodeInfo
decl_stmt|;
specifier|final
name|TezTaskAttemptID
name|taskAttemptId
decl_stmt|;
specifier|public
name|KillTaskCallable
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|,
name|AMNodeInfo
name|amNodeInfo
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
name|amNodeInfo
operator|=
name|amNodeInfo
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Void
name|callInternal
parameter_list|()
block|{
try|try
block|{
name|amNodeInfo
operator|.
name|getUmbilical
argument_list|()
operator|.
name|taskKilled
argument_list|(
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to send taskKilled message for task {}. Will re-run after it times out"
argument_list|,
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isShutdown
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupted while trying to send taskKilled message for task {}"
argument_list|,
name|taskAttemptId
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
class|class
name|AMHeartbeatCallable
extends|extends
name|CallableWithNdc
argument_list|<
name|Void
argument_list|>
block|{
specifier|final
name|AMNodeInfo
name|amNodeInfo
decl_stmt|;
specifier|public
name|AMHeartbeatCallable
parameter_list|(
name|AMNodeInfo
name|amNodeInfo
parameter_list|)
block|{
name|this
operator|.
name|amNodeInfo
operator|=
name|amNodeInfo
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Void
name|callInternal
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Attempting to heartbeat to AM: "
operator|+
name|amNodeInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|amNodeInfo
operator|.
name|getTaskCount
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"NodeHeartbeat to: "
operator|+
name|amNodeInfo
argument_list|)
expr_stmt|;
block|}
name|amNodeInfo
operator|.
name|getUmbilical
argument_list|()
operator|.
name|nodeHeartbeat
argument_list|(
operator|new
name|Text
argument_list|(
name|nodeId
operator|.
name|getHostname
argument_list|()
argument_list|)
argument_list|,
name|nodeId
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|currentDagName
init|=
name|amNodeInfo
operator|.
name|getCurrentDagName
argument_list|()
decl_stmt|;
name|amNodeInfo
operator|.
name|setAmFailed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to communicated with AM at {}. Killing remaining fragments for query {}"
argument_list|,
name|amNodeInfo
operator|.
name|amNodeId
argument_list|,
name|currentDagName
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|queryFailedHandler
operator|.
name|queryFailed
argument_list|(
literal|null
argument_list|,
name|currentDagName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isShutdown
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while trying to send heartbeat to AM {}"
argument_list|,
name|amNodeInfo
operator|.
name|amNodeId
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Skipping node heartbeat to AM: "
operator|+
name|amNodeInfo
operator|+
literal|", since ref count is 0"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|AMNodeInfo
implements|implements
name|Delayed
block|{
specifier|private
specifier|final
name|AtomicInteger
name|taskCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|user
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapNodeId
name|amNodeId
decl_stmt|;
specifier|private
specifier|final
name|RetryPolicy
name|retryPolicy
decl_stmt|;
specifier|private
specifier|final
name|long
name|timeout
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|amFailed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|String
name|currentDagName
decl_stmt|;
specifier|private
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|private
name|long
name|nextHeartbeatTime
decl_stmt|;
specifier|public
name|AMNodeInfo
parameter_list|(
name|LlapNodeId
name|amNodeId
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
name|String
name|currentDagName
parameter_list|,
name|RetryPolicy
name|retryPolicy
parameter_list|,
name|long
name|timeout
parameter_list|,
name|SocketFactory
name|socketFactory
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|jobToken
operator|=
name|jobToken
expr_stmt|;
name|this
operator|.
name|currentDagName
operator|=
name|currentDagName
expr_stmt|;
name|this
operator|.
name|retryPolicy
operator|=
name|retryPolicy
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|socketFactory
operator|=
name|socketFactory
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|amNodeId
operator|=
name|amNodeId
expr_stmt|;
block|}
specifier|synchronized
name|LlapTaskUmbilicalProtocol
name|getUmbilical
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|umbilical
operator|==
literal|null
condition|)
block|{
specifier|final
name|InetSocketAddress
name|address
init|=
name|NetUtils
operator|.
name|createSocketAddrForHost
argument_list|(
name|amNodeId
operator|.
name|getHostname
argument_list|()
argument_list|,
name|amNodeId
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|SecurityUtil
operator|.
name|setTokenService
argument_list|(
name|this
operator|.
name|jobToken
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|ugi
operator|.
name|addToken
argument_list|(
name|jobToken
argument_list|)
expr_stmt|;
name|umbilical
operator|=
name|ugi
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
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
argument_list|,
name|conf
argument_list|,
name|socketFactory
argument_list|,
operator|(
name|int
operator|)
name|timeout
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|umbilical
return|;
block|}
specifier|synchronized
name|void
name|stopUmbilical
parameter_list|()
block|{
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
name|umbilical
operator|=
literal|null
expr_stmt|;
block|}
name|int
name|incrementAndGetTaskCount
parameter_list|()
block|{
return|return
name|taskCount
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
name|int
name|decrementAndGetTaskCount
parameter_list|()
block|{
return|return
name|taskCount
operator|.
name|decrementAndGet
argument_list|()
return|;
block|}
name|void
name|setAmFailed
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|amFailed
operator|.
name|set
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
name|boolean
name|hasAmFailed
parameter_list|()
block|{
return|return
name|amFailed
operator|.
name|get
argument_list|()
return|;
block|}
name|int
name|getTaskCount
parameter_list|()
block|{
return|return
name|taskCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getCurrentDagName
parameter_list|()
block|{
return|return
name|currentDagName
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setCurrentDagName
parameter_list|(
name|String
name|currentDagName
parameter_list|)
block|{
name|this
operator|.
name|currentDagName
operator|=
name|currentDagName
expr_stmt|;
block|}
specifier|synchronized
name|void
name|setNextHeartbeatTime
parameter_list|(
name|long
name|nextTime
parameter_list|)
block|{
name|nextHeartbeatTime
operator|=
name|nextTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDelay
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|nextHeartbeatTime
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Delayed
name|o
parameter_list|)
block|{
name|AMNodeInfo
name|other
init|=
operator|(
name|AMNodeInfo
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|nextHeartbeatTime
operator|>
name|other
operator|.
name|nextHeartbeatTime
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|nextHeartbeatTime
operator|<
name|other
operator|.
name|nextHeartbeatTime
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
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
literal|"AMInfo: "
operator|+
name|amNodeId
operator|+
literal|", taskCount="
operator|+
name|getTaskCount
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

