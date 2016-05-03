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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|TezDAGID
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
name|dag
operator|.
name|records
operator|.
name|TezTaskID
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
name|TezVertexID
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
name|hadoop
operator|.
name|shim
operator|.
name|DefaultHadoopShim
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
name|EndReason
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
name|TaskRunner2Result
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
name|TaskExecutorTestHelpers
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
name|TestTaskExecutorService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|MockRequest
name|createMockRequest
parameter_list|(
name|int
name|fragmentNum
parameter_list|,
name|int
name|parallelism
parameter_list|,
name|long
name|startTime
parameter_list|,
name|boolean
name|canFinish
parameter_list|,
name|long
name|workTime
parameter_list|)
block|{
name|SubmitWorkRequestProto
name|requestProto
init|=
name|createSubmitWorkRequestProto
argument_list|(
name|fragmentNum
argument_list|,
name|parallelism
argument_list|,
name|startTime
argument_list|)
decl_stmt|;
name|QueryFragmentInfo
name|queryFragmentInfo
init|=
name|createQueryFragmentInfo
argument_list|(
name|requestProto
operator|.
name|getFragmentSpec
argument_list|()
argument_list|)
decl_stmt|;
name|MockRequest
name|mockRequest
init|=
operator|new
name|MockRequest
argument_list|(
name|requestProto
argument_list|,
name|queryFragmentInfo
argument_list|,
name|canFinish
argument_list|,
name|workTime
argument_list|)
decl_stmt|;
return|return
name|mockRequest
return|;
block|}
specifier|public
specifier|static
name|TaskExecutorService
operator|.
name|TaskWrapper
name|createTaskWrapper
parameter_list|(
name|SubmitWorkRequestProto
name|request
parameter_list|,
name|boolean
name|canFinish
parameter_list|,
name|int
name|workTime
parameter_list|)
block|{
name|QueryFragmentInfo
name|queryFragmentInfo
init|=
name|createQueryFragmentInfo
argument_list|(
name|request
operator|.
name|getFragmentSpec
argument_list|()
argument_list|)
decl_stmt|;
name|MockRequest
name|mockRequest
init|=
operator|new
name|MockRequest
argument_list|(
name|request
argument_list|,
name|queryFragmentInfo
argument_list|,
name|canFinish
argument_list|,
name|workTime
argument_list|)
decl_stmt|;
name|TaskExecutorService
operator|.
name|TaskWrapper
name|taskWrapper
init|=
operator|new
name|TaskExecutorService
operator|.
name|TaskWrapper
argument_list|(
name|mockRequest
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|taskWrapper
return|;
block|}
specifier|public
specifier|static
name|QueryFragmentInfo
name|createQueryFragmentInfo
parameter_list|(
name|FragmentSpecProto
name|fragmentSpecProto
parameter_list|)
block|{
name|QueryInfo
name|queryInfo
init|=
name|createQueryInfo
argument_list|()
decl_stmt|;
name|QueryFragmentInfo
name|fragmentInfo
init|=
operator|new
name|QueryFragmentInfo
argument_list|(
name|queryInfo
argument_list|,
literal|"fakeVertexName"
argument_list|,
name|fragmentSpecProto
operator|.
name|getFragmentNumber
argument_list|()
argument_list|,
literal|0
argument_list|,
name|fragmentSpecProto
argument_list|)
decl_stmt|;
return|return
name|fragmentInfo
return|;
block|}
specifier|public
specifier|static
name|QueryInfo
name|createQueryInfo
parameter_list|()
block|{
name|QueryIdentifier
name|queryIdentifier
init|=
operator|new
name|QueryIdentifier
argument_list|(
literal|"fake_app_id_string"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|QueryInfo
name|queryInfo
init|=
operator|new
name|QueryInfo
argument_list|(
name|queryIdentifier
argument_list|,
literal|"fake_app_id_string"
argument_list|,
literal|"fake_dag_name"
argument_list|,
literal|1
argument_list|,
literal|"fakeUser"
argument_list|,
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
argument_list|>
argument_list|()
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|queryInfo
return|;
block|}
specifier|public
specifier|static
name|SubmitWorkRequestProto
name|createSubmitWorkRequestProto
parameter_list|(
name|int
name|fragmentNumber
parameter_list|,
name|int
name|selfAndUpstreamParallelism
parameter_list|,
name|long
name|attemptStartTime
parameter_list|)
block|{
return|return
name|createSubmitWorkRequestProto
argument_list|(
name|fragmentNumber
argument_list|,
name|selfAndUpstreamParallelism
argument_list|,
literal|0
argument_list|,
name|attemptStartTime
argument_list|,
literal|1
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SubmitWorkRequestProto
name|createSubmitWorkRequestProto
parameter_list|(
name|int
name|fragmentNumber
parameter_list|,
name|int
name|selfAndUpstreamParallelism
parameter_list|,
name|int
name|selfAndUpstreamComplete
parameter_list|,
name|long
name|attemptStartTime
parameter_list|,
name|int
name|withinDagPriority
parameter_list|)
block|{
name|ApplicationId
name|appId
init|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
literal|9999
argument_list|,
literal|72
argument_list|)
decl_stmt|;
name|TezDAGID
name|dagId
init|=
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|appId
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|TezVertexID
name|vId
init|=
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|dagId
argument_list|,
literal|35
argument_list|)
decl_stmt|;
name|TezTaskID
name|tId
init|=
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|vId
argument_list|,
literal|389
argument_list|)
decl_stmt|;
name|TezTaskAttemptID
name|taId
init|=
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|tId
argument_list|,
name|fragmentNumber
argument_list|)
decl_stmt|;
return|return
name|SubmitWorkRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFragmentSpec
argument_list|(
name|FragmentSpecProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setAttemptNumber
argument_list|(
literal|0
argument_list|)
operator|.
name|setDagName
argument_list|(
literal|"MockDag"
argument_list|)
operator|.
name|setFragmentNumber
argument_list|(
name|fragmentNumber
argument_list|)
operator|.
name|setVertexName
argument_list|(
literal|"MockVertex"
argument_list|)
operator|.
name|setProcessorDescriptor
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"MockProcessor"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setFragmentIdentifierString
argument_list|(
name|taId
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setAmHost
argument_list|(
literal|"localhost"
argument_list|)
operator|.
name|setAmPort
argument_list|(
literal|12345
argument_list|)
operator|.
name|setAppAttemptNumber
argument_list|(
literal|0
argument_list|)
operator|.
name|setApplicationIdString
argument_list|(
literal|"MockApp_1"
argument_list|)
operator|.
name|setContainerIdString
argument_list|(
literal|"MockContainer_1"
argument_list|)
operator|.
name|setUser
argument_list|(
literal|"MockUser"
argument_list|)
operator|.
name|setTokenIdentifier
argument_list|(
literal|"MockToken_1"
argument_list|)
operator|.
name|setFragmentRuntimeInfo
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|FragmentRuntimeInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFirstAttemptStartTime
argument_list|(
name|attemptStartTime
argument_list|)
operator|.
name|setNumSelfAndUpstreamTasks
argument_list|(
name|selfAndUpstreamParallelism
argument_list|)
operator|.
name|setNumSelfAndUpstreamCompletedTasks
argument_list|(
name|selfAndUpstreamComplete
argument_list|)
operator|.
name|setWithinDagPriority
argument_list|(
name|withinDagPriority
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|MockRequest
extends|extends
name|TaskRunnerCallable
block|{
specifier|private
specifier|final
name|long
name|workTime
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|canFinish
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|isStarted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|isFinished
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|wasKilled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|wasInterrupted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
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
name|startedCondition
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|sleepCondition
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|shouldSleep
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|Condition
name|finishedCondition
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
specifier|public
name|MockRequest
parameter_list|(
name|SubmitWorkRequestProto
name|requestProto
parameter_list|,
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|,
name|boolean
name|canFinish
parameter_list|,
name|long
name|workTime
parameter_list|)
block|{
name|super
argument_list|(
name|requestProto
argument_list|,
name|fragmentInfo
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|ExecutionContextImpl
argument_list|(
literal|"localhost"
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|new
name|Credentials
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|mock
argument_list|(
name|LlapDaemonExecutorMetrics
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|KilledTaskHandler
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|FragmentCompletionHandler
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|DefaultHadoopShim
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|workTime
operator|=
name|workTime
expr_stmt|;
name|this
operator|.
name|canFinish
operator|=
name|canFinish
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|TaskRunner2Result
name|callInternal
parameter_list|()
block|{
try|try
block|{
name|logInfo
argument_list|(
name|super
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" is executing.."
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|isStarted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|startedCondition
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
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|shouldSleep
condition|)
block|{
name|sleepCondition
operator|.
name|await
argument_list|(
name|workTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
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
name|wasInterrupted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|new
name|TaskRunner2Result
argument_list|(
name|EndReason
operator|.
name|KILL_REQUESTED
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|wasKilled
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
operator|new
name|TaskRunner2Result
argument_list|(
name|EndReason
operator|.
name|KILL_REQUESTED
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|TaskRunner2Result
argument_list|(
name|EndReason
operator|.
name|SUCCESS
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|isFinished
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|finishedCondition
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|killTask
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|wasKilled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|shouldSleep
operator|=
literal|false
expr_stmt|;
name|sleepCondition
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
name|boolean
name|hasStarted
parameter_list|()
block|{
return|return
name|isStarted
operator|.
name|get
argument_list|()
return|;
block|}
name|boolean
name|hasFinished
parameter_list|()
block|{
return|return
name|isFinished
operator|.
name|get
argument_list|()
return|;
block|}
name|boolean
name|wasPreempted
parameter_list|()
block|{
return|return
name|wasKilled
operator|.
name|get
argument_list|()
return|;
block|}
name|void
name|complete
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|shouldSleep
operator|=
literal|false
expr_stmt|;
name|sleepCondition
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
name|void
name|awaitStart
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|isStarted
operator|.
name|get
argument_list|()
condition|)
block|{
name|startedCondition
operator|.
name|await
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
name|void
name|awaitEnd
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|isFinished
operator|.
name|get
argument_list|()
condition|)
block|{
name|finishedCondition
operator|.
name|await
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
annotation|@
name|Override
specifier|public
name|boolean
name|canFinish
parameter_list|()
block|{
return|return
name|canFinish
return|;
block|}
block|}
specifier|private
specifier|static
name|void
name|logInfo
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|message
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|logInfo
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|logInfo
argument_list|(
name|message
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

