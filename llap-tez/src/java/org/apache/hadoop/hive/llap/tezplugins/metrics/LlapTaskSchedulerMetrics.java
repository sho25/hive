begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|tezplugins
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerClusterNodeCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerCompletedDagCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerCpuCoresPerInstance
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerDisabledNodeCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerExecutorsPerInstance
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerMemoryPerInstance
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerMetrics
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerPendingPreemptionTaskCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerPendingTaskCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerPreemptedTaskCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerRunningTaskCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerSchedulableTaskCount
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
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapTaskSchedulerInfo
operator|.
name|SchedulerSuccessfulTaskCount
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
name|LlapMetricsSystem
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
name|MetricsUtils
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
name|MutableCounterInt
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
comment|/**  * Metrics about the llap daemon task scheduler.  */
end_comment

begin_class
annotation|@
name|Metrics
argument_list|(
name|about
operator|=
literal|"LlapDaemon Task Scheduler Metrics"
argument_list|,
name|context
operator|=
literal|"scheduler"
argument_list|)
specifier|public
class|class
name|LlapTaskSchedulerMetrics
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
annotation|@
name|Metric
name|MutableGaugeInt
name|numExecutors
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|memoryPerInstance
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|cpuCoresPerInstance
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|clusterNodeCount
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|disabledNodeCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|pendingTasksCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|schedulableTasksCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|runningTasksCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|successfulTasksCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|preemptedTasksCount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|completedDagcount
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterInt
name|pendingPreemptionTasksCount
decl_stmt|;
specifier|private
name|LlapTaskSchedulerMetrics
parameter_list|(
name|String
name|displayName
parameter_list|,
name|JvmMetrics
name|jm
parameter_list|,
name|String
name|sessionId
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
literal|"LlapTaskSchedulerMetricsRegistry"
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
block|}
specifier|public
specifier|static
name|LlapTaskSchedulerMetrics
name|create
parameter_list|(
name|String
name|displayName
parameter_list|,
name|String
name|sessionId
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
literal|"Llap Task Scheduler Metrics"
argument_list|,
operator|new
name|LlapTaskSchedulerMetrics
argument_list|(
name|displayName
argument_list|,
name|jm
argument_list|,
name|sessionId
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
name|SchedulerMetrics
argument_list|)
operator|.
name|setContext
argument_list|(
literal|"scheduler"
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
name|getTaskSchedulerStats
argument_list|(
name|rb
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setNumExecutors
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|numExecutors
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
name|setCpuCoresPerInstance
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|cpuCoresPerInstance
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setClusterNodeCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|clusterNodeCount
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDisabledNodeCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|disabledNodeCount
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrPendingTasksCount
parameter_list|()
block|{
name|pendingTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrPendingTasksCount
parameter_list|()
block|{
name|pendingTasksCount
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
name|incrSchedulableTasksCount
parameter_list|(
name|int
name|delta
parameter_list|)
block|{
name|schedulableTasksCount
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrSchedulableTasksCount
parameter_list|()
block|{
name|schedulableTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrSchedulableTasksCount
parameter_list|()
block|{
name|schedulableTasksCount
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
name|incrSuccessfulTasksCount
parameter_list|()
block|{
name|successfulTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrRunningTasksCount
parameter_list|()
block|{
name|runningTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrRunningTasksCount
parameter_list|()
block|{
name|runningTasksCount
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
name|incrPreemptedTasksCount
parameter_list|()
block|{
name|preemptedTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrCompletedDagCount
parameter_list|()
block|{
name|completedDagcount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrPendingPreemptionTasksCount
parameter_list|()
block|{
name|pendingPreemptionTasksCount
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrPendingPreemptionTasksCount
parameter_list|()
block|{
name|pendingPreemptionTasksCount
operator|.
name|incr
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getTaskSchedulerStats
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|rb
operator|.
name|addGauge
argument_list|(
name|SchedulerClusterNodeCount
argument_list|,
name|clusterNodeCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|SchedulerExecutorsPerInstance
argument_list|,
name|numExecutors
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|SchedulerMemoryPerInstance
argument_list|,
name|memoryPerInstance
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|SchedulerCpuCoresPerInstance
argument_list|,
name|cpuCoresPerInstance
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|SchedulerDisabledNodeCount
argument_list|,
name|disabledNodeCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerPendingTaskCount
argument_list|,
name|pendingTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerSchedulableTaskCount
argument_list|,
name|schedulableTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerRunningTaskCount
argument_list|,
name|runningTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerSuccessfulTaskCount
argument_list|,
name|successfulTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerPendingPreemptionTaskCount
argument_list|,
name|pendingPreemptionTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerPreemptedTaskCount
argument_list|,
name|preemptedTasksCount
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|SchedulerCompletedDagCount
argument_list|,
name|completedDagcount
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
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

