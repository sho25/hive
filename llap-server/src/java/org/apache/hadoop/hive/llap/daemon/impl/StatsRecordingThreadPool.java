begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|Stack
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
name|FutureTask
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
name|RunnableFuture
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
name|ThreadFactory
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
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
name|LlapUtil
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
name|counters
operator|.
name|LlapIOCounters
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
name|io
operator|.
name|encoded
operator|.
name|TezCounterSource
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
name|counters
operator|.
name|FileSystemCounter
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
name|runtime
operator|.
name|task
operator|.
name|TaskRunner2Callable
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
name|org
operator|.
name|slf4j
operator|.
name|MDC
import|;
end_import

begin_comment
comment|/**  * Custom thread pool implementation that records per thread file system statistics in TezCounters.  * The way it works is we capture before and after snapshots of file system thread statistics,  * compute the delta difference in statistics and update them in tez task counters.  */
end_comment

begin_class
specifier|public
class|class
name|StatsRecordingThreadPool
extends|extends
name|ThreadPoolExecutor
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
name|StatsRecordingThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// uncaught exception handler that will be set for all threads before execution
specifier|private
name|Thread
operator|.
name|UncaughtExceptionHandler
name|uncaughtExceptionHandler
decl_stmt|;
specifier|private
specifier|final
name|ThreadMXBean
name|mxBean
decl_stmt|;
specifier|public
name|StatsRecordingThreadPool
parameter_list|(
specifier|final
name|int
name|corePoolSize
parameter_list|,
specifier|final
name|int
name|maximumPoolSize
parameter_list|,
specifier|final
name|long
name|keepAliveTime
parameter_list|,
specifier|final
name|TimeUnit
name|unit
parameter_list|,
specifier|final
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
specifier|final
name|ThreadFactory
name|threadFactory
parameter_list|)
block|{
name|this
argument_list|(
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|,
name|threadFactory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StatsRecordingThreadPool
parameter_list|(
specifier|final
name|int
name|corePoolSize
parameter_list|,
specifier|final
name|int
name|maximumPoolSize
parameter_list|,
specifier|final
name|long
name|keepAliveTime
parameter_list|,
specifier|final
name|TimeUnit
name|unit
parameter_list|,
specifier|final
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
specifier|final
name|ThreadFactory
name|threadFactory
parameter_list|,
name|Thread
operator|.
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|,
name|threadFactory
argument_list|)
expr_stmt|;
name|this
operator|.
name|uncaughtExceptionHandler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|mxBean
operator|=
name|LlapUtil
operator|.
name|initThreadMxBean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
parameter_list|<
name|T
parameter_list|>
name|RunnableFuture
argument_list|<
name|T
argument_list|>
name|newTaskFor
parameter_list|(
specifier|final
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
return|return
operator|new
name|FutureTask
argument_list|(
operator|new
name|WrappedCallable
argument_list|(
name|callable
argument_list|,
name|uncaughtExceptionHandler
argument_list|,
name|mxBean
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|setUncaughtExceptionHandler
parameter_list|(
name|Thread
operator|.
name|UncaughtExceptionHandler
name|handler
parameter_list|)
block|{
name|this
operator|.
name|uncaughtExceptionHandler
operator|=
name|handler
expr_stmt|;
block|}
comment|/**    * Callable that wraps the actual callable submitted to the thread pool and invokes completion    * listener in finally block.    *    * @param<V> - actual callable    */
specifier|private
specifier|static
class|class
name|WrappedCallable
parameter_list|<
name|V
parameter_list|>
implements|implements
name|Callable
argument_list|<
name|V
argument_list|>
block|{
specifier|private
name|Callable
argument_list|<
name|V
argument_list|>
name|actualCallable
decl_stmt|;
specifier|private
name|Thread
operator|.
name|UncaughtExceptionHandler
name|uncaughtExceptionHandler
decl_stmt|;
specifier|private
name|ThreadMXBean
name|mxBean
decl_stmt|;
name|WrappedCallable
parameter_list|(
specifier|final
name|Callable
argument_list|<
name|V
argument_list|>
name|callable
parameter_list|,
specifier|final
name|Thread
operator|.
name|UncaughtExceptionHandler
name|uncaughtExceptionHandler
parameter_list|,
name|ThreadMXBean
name|mxBean
parameter_list|)
block|{
name|this
operator|.
name|actualCallable
operator|=
name|callable
expr_stmt|;
name|this
operator|.
name|uncaughtExceptionHandler
operator|=
name|uncaughtExceptionHandler
expr_stmt|;
name|this
operator|.
name|mxBean
operator|=
name|mxBean
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Thread
name|thread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
comment|// setup uncaught exception handler for the current thread
if|if
condition|(
name|uncaughtExceptionHandler
operator|!=
literal|null
condition|)
block|{
name|thread
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|uncaughtExceptionHandler
argument_list|)
expr_stmt|;
block|}
comment|// clone thread local file system statistics
name|List
argument_list|<
name|LlapUtil
operator|.
name|StatisticsData
argument_list|>
name|statsBefore
init|=
name|LlapUtil
operator|.
name|cloneThreadLocalFileSystemStatistics
argument_list|()
decl_stmt|;
name|long
name|cpuTime
init|=
name|mxBean
operator|==
literal|null
condition|?
operator|-
literal|1
else|:
name|mxBean
operator|.
name|getCurrentThreadCpuTime
argument_list|()
decl_stmt|,
name|userTime
init|=
name|mxBean
operator|==
literal|null
condition|?
operator|-
literal|1
else|:
name|mxBean
operator|.
name|getCurrentThreadUserTime
argument_list|()
decl_stmt|;
name|setupMDCFromNDC
argument_list|(
name|actualCallable
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|actualCallable
operator|.
name|call
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|mxBean
operator|!=
literal|null
condition|)
block|{
name|cpuTime
operator|=
name|mxBean
operator|.
name|getCurrentThreadCpuTime
argument_list|()
operator|-
name|cpuTime
expr_stmt|;
name|userTime
operator|=
name|mxBean
operator|.
name|getCurrentThreadUserTime
argument_list|()
operator|-
name|userTime
expr_stmt|;
block|}
name|updateCounters
argument_list|(
name|statsBefore
argument_list|,
name|actualCallable
argument_list|,
name|cpuTime
argument_list|,
name|userTime
argument_list|)
expr_stmt|;
name|MDC
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setupMDCFromNDC
parameter_list|(
specifier|final
name|Callable
argument_list|<
name|V
argument_list|>
name|actualCallable
parameter_list|)
block|{
if|if
condition|(
name|actualCallable
operator|instanceof
name|CallableWithNdc
condition|)
block|{
name|CallableWithNdc
name|callableWithNdc
init|=
operator|(
name|CallableWithNdc
operator|)
name|actualCallable
decl_stmt|;
try|try
block|{
comment|// CallableWithNdc inherits from NDC only when call() is invoked. CallableWithNdc has to
comment|// extended to provide access to its ndcStack that is cloned during creation. Until, then
comment|// we will use reflection to access the private field.
comment|// FIXME: HIVE-14243 follow to remove this reflection
name|Field
name|field
init|=
name|callableWithNdc
operator|.
name|getClass
argument_list|()
operator|.
name|getSuperclass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"ndcStack"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Stack
name|ndcStack
init|=
operator|(
name|Stack
operator|)
name|field
operator|.
name|get
argument_list|(
name|callableWithNdc
argument_list|)
decl_stmt|;
specifier|final
name|Stack
name|clonedStack
init|=
operator|(
name|Stack
operator|)
name|ndcStack
operator|.
name|clone
argument_list|()
decl_stmt|;
specifier|final
name|String
name|fragmentId
init|=
operator|(
name|String
operator|)
name|clonedStack
operator|.
name|pop
argument_list|()
decl_stmt|;
specifier|final
name|String
name|queryId
init|=
operator|(
name|String
operator|)
name|clonedStack
operator|.
name|pop
argument_list|()
decl_stmt|;
specifier|final
name|String
name|dagId
init|=
operator|(
name|String
operator|)
name|clonedStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|MDC
operator|.
name|put
argument_list|(
literal|"dagId"
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
name|MDC
operator|.
name|put
argument_list|(
literal|"queryId"
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|MDC
operator|.
name|put
argument_list|(
literal|"fragmentId"
argument_list|,
name|fragmentId
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
literal|"Received dagId: {} queryId: {} instanceType: {}"
argument_list|,
name|dagId
argument_list|,
name|queryId
argument_list|,
name|actualCallable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
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
name|warn
argument_list|(
literal|"Not setting up MDC as NDC stack cannot be accessed reflectively for"
operator|+
literal|" instance type: {} exception type: {}"
argument_list|,
name|actualCallable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not setting up MDC as unknown callable instance type received: {}"
argument_list|,
name|actualCallable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * LLAP IO related counters.      */
specifier|public
enum|enum
name|LlapExecutorCounters
block|{
name|EXECUTOR_CPU_NS
block|,
name|EXECUTOR_USER_NS
block|;      }
specifier|private
name|void
name|updateCounters
parameter_list|(
specifier|final
name|List
argument_list|<
name|LlapUtil
operator|.
name|StatisticsData
argument_list|>
name|statsBefore
parameter_list|,
specifier|final
name|Callable
argument_list|<
name|V
argument_list|>
name|actualCallable
parameter_list|,
name|long
name|cpuTime
parameter_list|,
name|long
name|userTime
parameter_list|)
block|{
name|Thread
name|thread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|TezCounters
name|tezCounters
init|=
literal|null
decl_stmt|;
comment|// add tez counters for task execution and llap io
if|if
condition|(
name|actualCallable
operator|instanceof
name|TaskRunner2Callable
condition|)
block|{
name|TaskRunner2Callable
name|taskRunner2Callable
init|=
operator|(
name|TaskRunner2Callable
operator|)
name|actualCallable
decl_stmt|;
comment|// counters for task execution side
name|tezCounters
operator|=
name|taskRunner2Callable
operator|.
name|addAndGetTezCounter
argument_list|(
name|FileSystemCounter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|actualCallable
operator|instanceof
name|TezCounterSource
condition|)
block|{
comment|// Other counter sources (currently used in LLAP IO).
name|tezCounters
operator|=
operator|(
operator|(
name|TezCounterSource
operator|)
name|actualCallable
operator|)
operator|.
name|getTezCounters
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected callable {}; cannot get counters"
argument_list|,
name|actualCallable
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tezCounters
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cpuTime
operator|>=
literal|0
operator|&&
name|userTime
operator|>=
literal|0
condition|)
block|{
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|LlapExecutorCounters
operator|.
name|EXECUTOR_CPU_NS
argument_list|)
operator|.
name|increment
argument_list|(
name|cpuTime
argument_list|)
expr_stmt|;
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|LlapExecutorCounters
operator|.
name|EXECUTOR_USER_NS
argument_list|)
operator|.
name|increment
argument_list|(
name|userTime
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|statsBefore
operator|!=
literal|null
condition|)
block|{
comment|// if there are multiple stats for the same scheme (from different NameNode), this
comment|// method will squash them together
name|Map
argument_list|<
name|String
argument_list|,
name|FileSystem
operator|.
name|Statistics
argument_list|>
name|schemeToStats
init|=
name|LlapUtil
operator|.
name|getCombinedFileSystemStatistics
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FileSystem
operator|.
name|Statistics
argument_list|>
name|entry
range|:
name|schemeToStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|String
name|scheme
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FileSystem
operator|.
name|Statistics
name|statistics
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|FileSystem
operator|.
name|Statistics
operator|.
name|StatisticsData
name|threadFSStats
init|=
name|statistics
operator|.
name|getThreadStatistics
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|LlapUtil
operator|.
name|StatisticsData
argument_list|>
name|allStatsBefore
init|=
name|LlapUtil
operator|.
name|getStatisticsForScheme
argument_list|(
name|scheme
argument_list|,
name|statsBefore
argument_list|)
decl_stmt|;
name|long
name|bytesReadDelta
init|=
literal|0
decl_stmt|;
name|long
name|bytesWrittenDelta
init|=
literal|0
decl_stmt|;
name|long
name|readOpsDelta
init|=
literal|0
decl_stmt|;
name|long
name|largeReadOpsDelta
init|=
literal|0
decl_stmt|;
name|long
name|writeOpsDelta
init|=
literal|0
decl_stmt|;
comment|// there could be more scheme after execution as execution might be accessing a
comment|// different filesystem. So if we don't find a matching scheme before execution we
comment|// just use the after execution values directly without computing delta difference
if|if
condition|(
name|allStatsBefore
operator|!=
literal|null
operator|&&
operator|!
name|allStatsBefore
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|LlapUtil
operator|.
name|StatisticsData
name|sb
range|:
name|allStatsBefore
control|)
block|{
name|bytesReadDelta
operator|+=
name|threadFSStats
operator|.
name|getBytesRead
argument_list|()
operator|-
name|sb
operator|.
name|getBytesRead
argument_list|()
expr_stmt|;
name|bytesWrittenDelta
operator|+=
name|threadFSStats
operator|.
name|getBytesWritten
argument_list|()
operator|-
name|sb
operator|.
name|getBytesWritten
argument_list|()
expr_stmt|;
name|readOpsDelta
operator|+=
name|threadFSStats
operator|.
name|getReadOps
argument_list|()
operator|-
name|sb
operator|.
name|getReadOps
argument_list|()
expr_stmt|;
name|largeReadOpsDelta
operator|+=
name|threadFSStats
operator|.
name|getLargeReadOps
argument_list|()
operator|-
name|sb
operator|.
name|getLargeReadOps
argument_list|()
expr_stmt|;
name|writeOpsDelta
operator|+=
name|threadFSStats
operator|.
name|getWriteOps
argument_list|()
operator|-
name|sb
operator|.
name|getWriteOps
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|bytesReadDelta
operator|=
name|threadFSStats
operator|.
name|getBytesRead
argument_list|()
expr_stmt|;
name|bytesWrittenDelta
operator|=
name|threadFSStats
operator|.
name|getBytesWritten
argument_list|()
expr_stmt|;
name|readOpsDelta
operator|=
name|threadFSStats
operator|.
name|getReadOps
argument_list|()
expr_stmt|;
name|largeReadOpsDelta
operator|=
name|threadFSStats
operator|.
name|getLargeReadOps
argument_list|()
expr_stmt|;
name|writeOpsDelta
operator|=
name|threadFSStats
operator|.
name|getWriteOps
argument_list|()
expr_stmt|;
block|}
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|scheme
argument_list|,
name|FileSystemCounter
operator|.
name|BYTES_READ
argument_list|)
operator|.
name|increment
argument_list|(
name|bytesReadDelta
argument_list|)
expr_stmt|;
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|scheme
argument_list|,
name|FileSystemCounter
operator|.
name|BYTES_WRITTEN
argument_list|)
operator|.
name|increment
argument_list|(
name|bytesWrittenDelta
argument_list|)
expr_stmt|;
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|scheme
argument_list|,
name|FileSystemCounter
operator|.
name|READ_OPS
argument_list|)
operator|.
name|increment
argument_list|(
name|readOpsDelta
argument_list|)
expr_stmt|;
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|scheme
argument_list|,
name|FileSystemCounter
operator|.
name|LARGE_READ_OPS
argument_list|)
operator|.
name|increment
argument_list|(
name|largeReadOpsDelta
argument_list|)
expr_stmt|;
name|tezCounters
operator|.
name|findCounter
argument_list|(
name|scheme
argument_list|,
name|FileSystemCounter
operator|.
name|WRITE_OPS
argument_list|)
operator|.
name|increment
argument_list|(
name|writeOpsDelta
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
literal|"Updated stats: instance: {} thread name: {} thread id: {} scheme: {} "
operator|+
literal|"bytesRead: {} bytesWritten: {} readOps: {} largeReadOps: {} writeOps: {}"
argument_list|,
name|actualCallable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|thread
operator|.
name|getName
argument_list|()
argument_list|,
name|thread
operator|.
name|getId
argument_list|()
argument_list|,
name|scheme
argument_list|,
name|bytesReadDelta
argument_list|,
name|bytesWrittenDelta
argument_list|,
name|readOpsDelta
argument_list|,
name|largeReadOpsDelta
argument_list|,
name|writeOpsDelta
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"File system statistics snapshot before execution of thread is null."
operator|+
literal|"Thread name: {} id: {} allStats: {}"
argument_list|,
name|thread
operator|.
name|getName
argument_list|()
argument_list|,
name|thread
operator|.
name|getId
argument_list|()
argument_list|,
name|statsBefore
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"TezCounters is null for callable type: {}"
argument_list|,
name|actualCallable
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

