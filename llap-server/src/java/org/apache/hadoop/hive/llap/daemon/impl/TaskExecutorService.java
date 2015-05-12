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
name|util
operator|.
name|Comparator
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
name|PriorityBlockingQueue
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
name|SynchronousQueue
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
name|ThreadPoolExecutor
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
comment|/**  * Task executor service provides method for scheduling tasks. Tasks submitted to executor service  * are submitted to wait queue for scheduling. Wait queue tasks are ordered based on the priority  * of the task. The internal wait queue scheduler moves tasks from wait queue when executor slots  * are available or when a higher priority task arrives and will schedule it for execution.  * When pre-emption is enabled, the tasks from wait queue can replace(pre-empt) a running task.  * The pre-empted task is reported back to the Application Master(AM) for it to be rescheduled.  *<p/>  * Because of the concurrent nature of task submission, the position of the task in wait queue is  * held as long the scheduling of the task from wait queue (with or without pre-emption) is complete.  * The order of pre-emption is based on the ordering in the pre-emption queue. All tasks that cannot  * run to completion immediately (canFinish = false) are added to pre-emption queue.  *<p/>  * When all the executor threads are occupied and wait queue is full, the task scheduler will  * throw RejectedExecutionException.  *<p/>  * Task executor service can be shut down which will terminated all running tasks and reject all  * new tasks. Shutting down of the task executor service can be done gracefully or immediately.  */
end_comment

begin_class
specifier|public
class|class
name|TaskExecutorService
implements|implements
name|Scheduler
argument_list|<
name|TaskRunnerCallable
argument_list|>
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
name|TaskExecutorService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|isInfoEnabled
init|=
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|isDebugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|isTraceEnabled
init|=
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TASK_EXECUTOR_THREAD_NAME_FORMAT
init|=
literal|"Task-Executor-%d"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|WAIT_QUEUE_SCHEDULER_THREAD_NAME_FORMAT
init|=
literal|"Wait-Queue-Scheduler-%d"
decl_stmt|;
comment|// some object to lock upon. Used by task scheduler to notify wait queue scheduler of new items
comment|// to wait queue
specifier|private
specifier|final
name|Object
name|waitLock
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|executorService
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|TaskRunnerCallable
argument_list|>
name|waitQueue
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|waitQueueExecutorService
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TaskRunnerCallable
argument_list|>
name|idToTaskMap
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TaskRunnerCallable
argument_list|,
name|ListenableFuture
argument_list|<
name|?
argument_list|>
argument_list|>
name|preemptionMap
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|TaskRunnerCallable
argument_list|>
name|preemptionQueue
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|enablePreemption
decl_stmt|;
specifier|private
specifier|final
name|ThreadPoolExecutor
name|threadPoolExecutor
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|numSlotsAvailable
decl_stmt|;
specifier|public
name|TaskExecutorService
parameter_list|(
name|int
name|numExecutors
parameter_list|,
name|int
name|waitQueueSize
parameter_list|,
name|boolean
name|enablePreemption
parameter_list|)
block|{
name|this
operator|.
name|waitLock
operator|=
operator|new
name|Object
argument_list|()
expr_stmt|;
name|this
operator|.
name|waitQueue
operator|=
operator|new
name|BoundedPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|WaitQueueComparator
argument_list|()
argument_list|,
name|waitQueueSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPoolExecutor
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|numExecutors
argument_list|,
comment|// core pool size
name|numExecutors
argument_list|,
comment|// max pool size
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
comment|// direct hand-off
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
name|TASK_EXECUTOR_THREAD_NAME_FORMAT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorService
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|threadPoolExecutor
argument_list|)
expr_stmt|;
name|this
operator|.
name|idToTaskMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|preemptionMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|preemptionQueue
operator|=
operator|new
name|PriorityBlockingQueue
argument_list|<>
argument_list|(
name|numExecutors
argument_list|,
operator|new
name|PreemptionQueueComparator
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|enablePreemption
operator|=
name|enablePreemption
expr_stmt|;
name|this
operator|.
name|numSlotsAvailable
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|numExecutors
argument_list|)
expr_stmt|;
comment|// single threaded scheduler for tasks from wait queue to executor threads
name|ExecutorService
name|wes
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
name|setNameFormat
argument_list|(
name|WAIT_QUEUE_SCHEDULER_THREAD_NAME_FORMAT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|waitQueueExecutorService
operator|=
name|MoreExecutors
operator|.
name|listeningDecorator
argument_list|(
name|wes
argument_list|)
expr_stmt|;
name|ListenableFuture
argument_list|<
name|?
argument_list|>
name|future
init|=
name|waitQueueExecutorService
operator|.
name|submit
argument_list|(
operator|new
name|WaitQueueWorker
argument_list|()
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
operator|new
name|WaitQueueWorkerCallback
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Worker that takes tasks from wait queue and schedule it for execution.    */
specifier|private
specifier|final
class|class
name|WaitQueueWorker
implements|implements
name|Runnable
block|{
name|TaskRunnerCallable
name|task
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|waitQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|waitLock
init|)
block|{
name|waitLock
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Since schedule() can be called from multiple threads, we peek the wait queue,
comment|// try scheduling the task and then remove the task if scheduling is successful.
comment|// This will make sure the task's place in the wait queue is held until it gets scheduled.
while|while
condition|(
operator|(
name|task
operator|=
name|waitQueue
operator|.
name|peek
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// if the task cannot finish and if no slots are available then don't schedule it.
comment|// TODO: Event notifications that change canFinish state should notify waitLock
synchronized|synchronized
init|(
name|waitLock
init|)
block|{
if|if
condition|(
operator|!
name|task
operator|.
name|canFinish
argument_list|()
operator|&&
name|numSlotsAvailable
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
name|waitLock
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
name|boolean
name|scheduled
init|=
name|trySchedule
argument_list|(
name|task
argument_list|)
decl_stmt|;
if|if
condition|(
name|scheduled
condition|)
block|{
comment|// wait queue could have been re-ordered in the mean time because of concurrent task
comment|// submission. So remove the specific task instead of the head task.
name|waitQueue
operator|.
name|remove
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|waitQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|waitLock
init|)
block|{
name|waitLock
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Executor service will create new thread if the current thread gets interrupted. We don't
comment|// need to do anything with the exception.
name|LOG
operator|.
name|info
argument_list|(
name|WAIT_QUEUE_SCHEDULER_THREAD_NAME_FORMAT
operator|+
literal|" thread has been interrupted."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|WaitQueueWorkerCallback
implements|implements
name|FutureCallback
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Object
name|result
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Wait queue scheduler worker exited with success!"
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
name|error
argument_list|(
literal|"Wait queue scheduler worker exited with failure!"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|schedule
parameter_list|(
name|TaskRunnerCallable
name|task
parameter_list|)
throws|throws
name|RejectedExecutionException
block|{
if|if
condition|(
name|waitQueue
operator|.
name|offer
argument_list|(
name|task
argument_list|)
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" added to wait queue."
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|waitLock
init|)
block|{
name|waitLock
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|RejectedExecutionException
argument_list|(
literal|"Queues are full. Rejecting request."
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|trySchedule
parameter_list|(
specifier|final
name|TaskRunnerCallable
name|task
parameter_list|)
block|{
name|boolean
name|scheduled
init|=
literal|false
decl_stmt|;
try|try
block|{
name|ListenableFuture
argument_list|<
name|TaskRunner2Result
argument_list|>
name|future
init|=
name|executorService
operator|.
name|submit
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|FutureCallback
argument_list|<
name|TaskRunner2Result
argument_list|>
name|wrappedCallback
init|=
operator|new
name|InternalCompletionListener
argument_list|(
name|task
operator|.
name|getCallback
argument_list|()
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
name|wrappedCallback
argument_list|)
expr_stmt|;
if|if
condition|(
name|isInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" scheduled for execution."
argument_list|)
expr_stmt|;
block|}
comment|// only tasks that cannot finish immediately are pre-emptable. In other words, if all inputs
comment|// to the tasks are not ready yet, the task is eligible for pre-emptable.
if|if
condition|(
name|enablePreemption
operator|&&
operator|!
name|task
operator|.
name|canFinish
argument_list|()
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" is not finishable and pre-emption is enabled."
operator|+
literal|"Adding it to pre-emption queue."
argument_list|)
expr_stmt|;
block|}
name|addTaskToPreemptionList
argument_list|(
name|task
argument_list|,
name|future
argument_list|)
expr_stmt|;
block|}
name|numSlotsAvailable
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
name|scheduled
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|enablePreemption
operator|&&
name|task
operator|.
name|canFinish
argument_list|()
operator|&&
operator|!
name|preemptionQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|isTraceEnabled
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"idToTaskMap: "
operator|+
name|idToTaskMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"preemptionMap: "
operator|+
name|preemptionMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"preemptionQueue: "
operator|+
name|preemptionQueue
argument_list|)
expr_stmt|;
block|}
name|TaskRunnerCallable
name|pRequest
init|=
name|preemptionQueue
operator|.
name|remove
argument_list|()
decl_stmt|;
comment|// if some task completes, it will remove itself from pre-emptions lists make this null.
comment|// if it happens bail out and schedule it again as a free slot will be available.
if|if
condition|(
name|pRequest
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|pRequest
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" is chosen for pre-emption."
argument_list|)
expr_stmt|;
block|}
name|ListenableFuture
argument_list|<
name|?
argument_list|>
name|pFuture
init|=
name|preemptionMap
operator|.
name|get
argument_list|(
name|pRequest
argument_list|)
decl_stmt|;
comment|// if pFuture is null, then it must have been completed and be removed from preemption map
if|if
condition|(
name|pFuture
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pre-emption invoked for "
operator|+
name|pRequest
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" by interrupting the thread."
argument_list|)
expr_stmt|;
block|}
name|pRequest
operator|.
name|killTask
argument_list|()
expr_stmt|;
comment|// TODO. Ideally, should wait for the thread to complete and fall off before assuming the
comment|// slot is available for the next task.
name|removeTaskFromPreemptionList
argument_list|(
name|pRequest
argument_list|,
name|pRequest
operator|.
name|getRequestId
argument_list|()
argument_list|)
expr_stmt|;
comment|// future is cancelled or completed normally, in which case schedule the new request
if|if
condition|(
name|pFuture
operator|.
name|isDone
argument_list|()
operator|&&
name|pFuture
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|pRequest
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" request preempted by "
operator|+
name|task
operator|.
name|getRequestId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// try to submit the task from wait queue to executor service. If it gets rejected the
comment|// task from wait queue will hold on to its position for next try.
try|try
block|{
name|ListenableFuture
argument_list|<
name|TaskRunner2Result
argument_list|>
name|future
init|=
name|executorService
operator|.
name|submit
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|FutureCallback
argument_list|<
name|TaskRunner2Result
argument_list|>
name|wrappedCallback
init|=
operator|new
name|InternalCompletionListener
argument_list|(
name|task
operator|.
name|getCallback
argument_list|()
argument_list|)
decl_stmt|;
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
name|wrappedCallback
argument_list|)
expr_stmt|;
name|numSlotsAvailable
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
name|scheduled
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Request "
operator|+
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" from wait queue submitted"
operator|+
literal|" to executor service."
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|e1
parameter_list|)
block|{
comment|// This should not happen as we just freed a slot from executor service by pre-emption,
comment|// which cannot be claimed by other tasks as trySchedule() is serially executed.
name|scheduled
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Request "
operator|+
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" from wait queue rejected by"
operator|+
literal|" executor service."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|scheduled
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|removeTaskFromPreemptionList
parameter_list|(
name|TaskRunnerCallable
name|pRequest
parameter_list|,
name|String
name|requestId
parameter_list|)
block|{
name|idToTaskMap
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
name|preemptionMap
operator|.
name|remove
argument_list|(
name|pRequest
argument_list|)
expr_stmt|;
name|preemptionQueue
operator|.
name|remove
argument_list|(
name|pRequest
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|void
name|addTaskToPreemptionList
parameter_list|(
name|TaskRunnerCallable
name|task
parameter_list|,
name|ListenableFuture
argument_list|<
name|TaskRunner2Result
argument_list|>
name|future
parameter_list|)
block|{
name|idToTaskMap
operator|.
name|put
argument_list|(
name|task
operator|.
name|getRequestId
argument_list|()
argument_list|,
name|task
argument_list|)
expr_stmt|;
name|preemptionMap
operator|.
name|put
argument_list|(
name|task
argument_list|,
name|future
argument_list|)
expr_stmt|;
name|preemptionQueue
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
class|class
name|InternalCompletionListener
implements|implements
name|FutureCallback
argument_list|<
name|TaskRunner2Result
argument_list|>
block|{
specifier|private
name|TaskRunnerCallable
operator|.
name|TaskRunnerCallback
name|wrappedCallback
decl_stmt|;
specifier|public
name|InternalCompletionListener
parameter_list|(
name|TaskRunnerCallable
operator|.
name|TaskRunnerCallback
name|wrappedCallback
parameter_list|)
block|{
name|this
operator|.
name|wrappedCallback
operator|=
name|wrappedCallback
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|TaskRunner2Result
name|result
parameter_list|)
block|{
name|wrappedCallback
operator|.
name|onSuccess
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|updatePreemptionListAndNotify
argument_list|(
literal|true
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
name|wrappedCallback
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|updatePreemptionListAndNotify
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updatePreemptionListAndNotify
parameter_list|(
name|boolean
name|success
parameter_list|)
block|{
comment|// if this task was added to pre-emption list, remove it
name|String
name|taskId
init|=
name|wrappedCallback
operator|.
name|getRequestId
argument_list|()
decl_stmt|;
name|TaskRunnerCallable
name|task
init|=
name|idToTaskMap
operator|.
name|get
argument_list|(
name|taskId
argument_list|)
decl_stmt|;
name|String
name|state
init|=
name|success
condition|?
literal|"succeeded"
else|:
literal|"failed"
decl_stmt|;
if|if
condition|(
name|enablePreemption
operator|&&
name|task
operator|!=
literal|null
condition|)
block|{
name|removeTaskFromPreemptionList
argument_list|(
name|task
argument_list|,
name|taskId
argument_list|)
expr_stmt|;
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|task
operator|.
name|getRequestId
argument_list|()
operator|+
literal|" request "
operator|+
name|state
operator|+
literal|"! Removed from preemption list."
argument_list|)
expr_stmt|;
block|}
block|}
name|numSlotsAvailable
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|waitQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|waitLock
init|)
block|{
name|waitLock
operator|.
name|notify
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// TODO: llap daemon should call this to gracefully shutdown the task executor service
specifier|public
name|void
name|shutDown
parameter_list|(
name|boolean
name|awaitTermination
parameter_list|)
block|{
if|if
condition|(
name|awaitTermination
condition|)
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"awaitTermination: "
operator|+
name|awaitTermination
operator|+
literal|" shutting down task executor"
operator|+
literal|" service gracefully"
argument_list|)
expr_stmt|;
block|}
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|executorService
operator|.
name|awaitTermination
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
condition|)
block|{
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
name|waitQueueExecutorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|waitQueueExecutorService
operator|.
name|awaitTermination
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
condition|)
block|{
name|waitQueueExecutorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|waitQueueExecutorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"awaitTermination: "
operator|+
name|awaitTermination
operator|+
literal|" shutting down task executor"
operator|+
literal|" service immediately"
argument_list|)
expr_stmt|;
block|}
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|waitQueueExecutorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|getPreemptionListSize
parameter_list|()
block|{
return|return
name|preemptionMap
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|TaskRunnerCallable
name|getPreemptionTask
parameter_list|()
block|{
return|return
name|preemptionQueue
operator|.
name|peek
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
class|class
name|WaitQueueComparator
implements|implements
name|Comparator
argument_list|<
name|TaskRunnerCallable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|TaskRunnerCallable
name|o1
parameter_list|,
name|TaskRunnerCallable
name|o2
parameter_list|)
block|{
name|boolean
name|newCanFinish
init|=
name|o1
operator|.
name|canFinish
argument_list|()
decl_stmt|;
name|boolean
name|oldCanFinish
init|=
name|o2
operator|.
name|canFinish
argument_list|()
decl_stmt|;
if|if
condition|(
name|newCanFinish
operator|==
literal|true
operator|&&
name|oldCanFinish
operator|==
literal|false
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|newCanFinish
operator|==
literal|false
operator|&&
name|oldCanFinish
operator|==
literal|true
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o1
operator|.
name|getVertexParallelism
argument_list|()
operator|>
name|o2
operator|.
name|getVertexParallelism
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|o1
operator|.
name|getVertexParallelism
argument_list|()
operator|<
name|o2
operator|.
name|getVertexParallelism
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
class|class
name|PreemptionQueueComparator
implements|implements
name|Comparator
argument_list|<
name|TaskRunnerCallable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|TaskRunnerCallable
name|o1
parameter_list|,
name|TaskRunnerCallable
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|.
name|getVertexParallelism
argument_list|()
operator|>
name|o2
operator|.
name|getVertexParallelism
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|o1
operator|.
name|getVertexParallelism
argument_list|()
operator|<
name|o2
operator|.
name|getVertexParallelism
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

