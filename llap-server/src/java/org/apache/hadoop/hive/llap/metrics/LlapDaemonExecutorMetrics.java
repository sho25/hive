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
name|metrics
package|;
end_package

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorCacheMemoryPerInstance
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorJvmMaxMemory
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorMemoryPerInstance
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorNumQueuedRequests
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorRpcNumHandlers
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorThreadCPUTime
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorThreadCountPerInstance
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorThreadUserTime
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorTotalAskedToDie
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorTotalExecutionFailure
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorTotalInterrupted
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorTotalRequestsHandled
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorTotalSuccess
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorMetrics
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|ExecutorWaitQueueSize
import|;
end_import

begin_import
import|import static
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
name|LlapDaemonExecutorInfo
operator|.
name|PreemptionTimeLost
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|metrics2
operator|.
name|impl
operator|.
name|MsInfo
operator|.
name|ProcessName
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|metrics2
operator|.
name|impl
operator|.
name|MsInfo
operator|.
name|SessionId
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
name|ThreadInfo
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
name|ConcurrentHashMap
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
name|impl
operator|.
name|ContainerRunnerImpl
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
name|metrics2
operator|.
name|MetricsCollector
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
name|metrics2
operator|.
name|MetricsInfo
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
name|metrics2
operator|.
name|MetricsRecordBuilder
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
name|metrics2
operator|.
name|MetricsSource
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
name|metrics2
operator|.
name|MetricsSystem
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
name|metrics2
operator|.
name|annotation
operator|.
name|Metric
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
name|metrics2
operator|.
name|annotation
operator|.
name|Metrics
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
name|metrics2
operator|.
name|lib
operator|.
name|MetricsRegistry
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableCounterLong
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableGaugeInt
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableGaugeLong
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
name|metrics2
operator|.
name|source
operator|.
name|JvmMetrics
import|;
end_import

begin_comment
comment|/**  * Metrics about the llap daemon executors.  */
end_comment

begin_class
annotation|@
name|Metrics
argument_list|(
name|about
operator|=
literal|"LlapDaemon Executor Metrics"
argument_list|,
name|context
operator|=
literal|"executors"
argument_list|)
specifier|public
class|class
name|LlapDaemonExecutorMetrics
implements|implements
name|MetricsSource
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|JvmMetrics
name|jvmMetrics
decl_stmt|;
specifier|private
specifier|final
name|String
name|sessionId
decl_stmt|;
specifier|private
specifier|final
name|MetricsRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|int
name|numExecutors
decl_stmt|;
specifier|private
specifier|final
name|ThreadMXBean
name|threadMXBean
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|MetricsInfo
argument_list|>
name|cpuMetricsInfoMap
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|MetricsInfo
argument_list|>
name|userMetricsInfoMap
decl_stmt|;
specifier|final
name|MutableGaugeLong
index|[]
name|executorThreadCpuTime
decl_stmt|;
specifier|final
name|MutableGaugeLong
index|[]
name|executorThreadUserTime
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|executorTotalRequestHandled
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|executorNumQueuedRequests
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|executorTotalSuccess
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|executorTotalIKilled
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|executorTotalExecutionFailed
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|preemptionTimeLost
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|cacheMemoryPerInstance
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|memoryPerInstance
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|jvmMaxMemory
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|waitQueueSize
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|rpcNumHandlers
decl_stmt|;
specifier|private
name|LlapDaemonExecutorMetrics
parameter_list|(
name|String
name|displayName
parameter_list|,
name|JvmMetrics
name|jm
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|int
name|numExecutors
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|displayName
expr_stmt|;
name|this
operator|.
name|jvmMetrics
operator|=
name|jm
expr_stmt|;
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
name|this
operator|.
name|registry
operator|=
operator|new
name|MetricsRegistry
argument_list|(
literal|"LlapDaemonExecutorRegistry"
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|.
name|tag
argument_list|(
name|ProcessName
argument_list|,
name|MetricsUtils
operator|.
name|METRICS_PROCESS_NAME
argument_list|)
operator|.
name|tag
argument_list|(
name|SessionId
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|numExecutors
operator|=
name|numExecutors
expr_stmt|;
name|this
operator|.
name|threadMXBean
operator|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
expr_stmt|;
name|this
operator|.
name|executorThreadCpuTime
operator|=
operator|new
name|MutableGaugeLong
index|[
name|numExecutors
index|]
expr_stmt|;
name|this
operator|.
name|executorThreadUserTime
operator|=
operator|new
name|MutableGaugeLong
index|[
name|numExecutors
index|]
expr_stmt|;
name|this
operator|.
name|cpuMetricsInfoMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|userMetricsInfoMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numExecutors
condition|;
name|i
operator|++
control|)
block|{
name|MetricsInfo
name|mic
init|=
operator|new
name|LlapDaemonCustomMetricsInfo
argument_list|(
name|ExecutorThreadCPUTime
operator|.
name|name
argument_list|()
operator|+
literal|"_"
operator|+
name|i
argument_list|,
name|ExecutorThreadCPUTime
operator|.
name|description
argument_list|()
argument_list|)
decl_stmt|;
name|MetricsInfo
name|miu
init|=
operator|new
name|LlapDaemonCustomMetricsInfo
argument_list|(
name|ExecutorThreadUserTime
operator|.
name|name
argument_list|()
operator|+
literal|"_"
operator|+
name|i
argument_list|,
name|ExecutorThreadUserTime
operator|.
name|description
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|cpuMetricsInfoMap
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|mic
argument_list|)
expr_stmt|;
name|this
operator|.
name|userMetricsInfoMap
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|miu
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorThreadCpuTime
index|[
name|i
index|]
operator|=
name|registry
operator|.
name|newGauge
argument_list|(
name|mic
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorThreadUserTime
index|[
name|i
index|]
operator|=
name|registry
operator|.
name|newGauge
argument_list|(
name|miu
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|LlapDaemonExecutorMetrics
name|create
parameter_list|(
name|String
name|displayName
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|int
name|numExecutors
parameter_list|)
block|{
name|MetricsSystem
name|ms
init|=
name|LlapMetricsSystem
operator|.
name|instance
argument_list|()
decl_stmt|;
name|JvmMetrics
name|jm
init|=
name|JvmMetrics
operator|.
name|create
argument_list|(
name|MetricsUtils
operator|.
name|METRICS_PROCESS_NAME
argument_list|,
name|sessionId
argument_list|,
name|ms
argument_list|)
decl_stmt|;
return|return
name|ms
operator|.
name|register
argument_list|(
name|displayName
argument_list|,
literal|"LlapDaemon Executor Metrics"
argument_list|,
operator|new
name|LlapDaemonExecutorMetrics
argument_list|(
name|displayName
argument_list|,
name|jm
argument_list|,
name|sessionId
argument_list|,
name|numExecutors
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getMetrics
parameter_list|(
name|MetricsCollector
name|collector
parameter_list|,
name|boolean
name|b
parameter_list|)
block|{
name|MetricsRecordBuilder
name|rb
init|=
name|collector
operator|.
name|addRecord
argument_list|(
name|ExecutorMetrics
argument_list|)
operator|.
name|setContext
argument_list|(
literal|"executors"
argument_list|)
operator|.
name|tag
argument_list|(
name|ProcessName
argument_list|,
name|MetricsUtils
operator|.
name|METRICS_PROCESS_NAME
argument_list|)
operator|.
name|tag
argument_list|(
name|SessionId
argument_list|,
name|sessionId
argument_list|)
decl_stmt|;
name|getExecutorStats
argument_list|(
name|rb
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrExecutorTotalRequestsHandled
parameter_list|()
block|{
name|executorTotalRequestHandled
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrExecutorNumQueuedRequests
parameter_list|()
block|{
name|executorNumQueuedRequests
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrExecutorNumQueuedRequests
parameter_list|()
block|{
name|executorNumQueuedRequests
operator|.
name|incr
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrExecutorTotalSuccess
parameter_list|()
block|{
name|executorTotalSuccess
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrExecutorTotalExecutionFailed
parameter_list|()
block|{
name|executorTotalExecutionFailed
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrPreemptionTimeLost
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|preemptionTimeLost
operator|.
name|incr
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrExecutorTotalKilled
parameter_list|()
block|{
name|executorTotalIKilled
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setCacheMemoryPerInstance
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|cacheMemoryPerInstance
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setMemoryPerInstance
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|memoryPerInstance
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setJvmMaxMemory
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|jvmMaxMemory
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setWaitQueueSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|waitQueueSize
operator|.
name|set
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRpcNumHandlers
parameter_list|(
name|int
name|numHandlers
parameter_list|)
block|{
name|rpcNumHandlers
operator|.
name|set
argument_list|(
name|numHandlers
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getExecutorStats
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|updateThreadMetrics
argument_list|(
name|rb
argument_list|)
expr_stmt|;
name|rb
operator|.
name|addCounter
argument_list|(
name|ExecutorTotalRequestsHandled
argument_list|,
name|executorTotalRequestHandled
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|ExecutorNumQueuedRequests
argument_list|,
name|executorNumQueuedRequests
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|ExecutorTotalSuccess
argument_list|,
name|executorTotalSuccess
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|ExecutorTotalExecutionFailure
argument_list|,
name|executorTotalExecutionFailed
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|ExecutorTotalInterrupted
argument_list|,
name|executorTotalIKilled
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|PreemptionTimeLost
argument_list|,
name|preemptionTimeLost
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorThreadCountPerInstance
argument_list|,
name|numExecutors
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorMemoryPerInstance
argument_list|,
name|memoryPerInstance
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorCacheMemoryPerInstance
argument_list|,
name|cacheMemoryPerInstance
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorJvmMaxMemory
argument_list|,
name|jvmMaxMemory
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorWaitQueueSize
argument_list|,
name|waitQueueSize
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ExecutorRpcNumHandlers
argument_list|,
name|rpcNumHandlers
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateThreadMetrics
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
if|if
condition|(
name|threadMXBean
operator|.
name|isThreadCpuTimeSupported
argument_list|()
operator|&&
name|threadMXBean
operator|.
name|isThreadCpuTimeEnabled
argument_list|()
condition|)
block|{
specifier|final
name|long
index|[]
name|ids
init|=
name|threadMXBean
operator|.
name|getAllThreadIds
argument_list|()
decl_stmt|;
specifier|final
name|ThreadInfo
index|[]
name|infos
init|=
name|threadMXBean
operator|.
name|getThreadInfo
argument_list|(
name|ids
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ids
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ThreadInfo
name|threadInfo
init|=
name|infos
index|[
name|i
index|]
decl_stmt|;
name|String
name|threadName
init|=
name|threadInfo
operator|.
name|getThreadName
argument_list|()
decl_stmt|;
name|long
name|threadId
init|=
name|ids
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numExecutors
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|threadName
operator|.
name|equals
argument_list|(
name|ContainerRunnerImpl
operator|.
name|THREAD_NAME_FORMAT_PREFIX
operator|+
name|j
argument_list|)
condition|)
block|{
name|executorThreadCpuTime
index|[
name|j
index|]
operator|.
name|set
argument_list|(
name|threadMXBean
operator|.
name|getThreadCpuTime
argument_list|(
name|threadId
argument_list|)
argument_list|)
expr_stmt|;
name|executorThreadUserTime
index|[
name|j
index|]
operator|.
name|set
argument_list|(
name|threadMXBean
operator|.
name|getThreadUserTime
argument_list|(
name|threadId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numExecutors
condition|;
name|i
operator|++
control|)
block|{
name|rb
operator|.
name|addGauge
argument_list|(
name|cpuMetricsInfoMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|executorThreadCpuTime
index|[
name|i
index|]
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|rb
operator|.
name|addGauge
argument_list|(
name|userMetricsInfoMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|executorThreadUserTime
index|[
name|i
index|]
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|JvmMetrics
name|getJvmMetrics
parameter_list|()
block|{
return|return
name|jvmMetrics
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
end_class

end_unit

