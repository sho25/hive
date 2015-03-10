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
name|LlapDaemonQueueInfo
operator|.
name|MaxProcessingTime
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
name|LlapDaemonQueueInfo
operator|.
name|QueueMetrics
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
name|LlapDaemonQueueInfo
operator|.
name|QueueSize
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
name|lib
operator|.
name|MutableQuantiles
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
name|MutableRate
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|Metrics
argument_list|(
name|about
operator|=
literal|"LlapDaemon Queue Metrics"
argument_list|,
name|context
operator|=
name|MetricsUtils
operator|.
name|METRICS_CONTEXT
argument_list|)
specifier|public
class|class
name|LlapDaemonQueueMetrics
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
name|String
name|sessionId
decl_stmt|;
specifier|private
specifier|final
name|MetricsRegistry
name|registry
decl_stmt|;
specifier|private
name|long
name|maxTime
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeInt
name|queueSize
decl_stmt|;
annotation|@
name|Metric
name|MutableRate
name|rateOfProcessing
decl_stmt|;
specifier|final
name|MutableQuantiles
index|[]
name|processingTimes
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|maxProcessingTime
decl_stmt|;
specifier|private
name|LlapDaemonQueueMetrics
parameter_list|(
name|String
name|displayName
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|int
index|[]
name|intervals
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
literal|"LlapDaemonQueueRegistry"
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
specifier|final
name|int
name|len
init|=
name|intervals
operator|==
literal|null
condition|?
literal|0
else|:
name|intervals
operator|.
name|length
decl_stmt|;
name|this
operator|.
name|processingTimes
operator|=
operator|new
name|MutableQuantiles
index|[
name|len
index|]
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
name|len
condition|;
name|i
operator|++
control|)
block|{
name|int
name|interval
init|=
name|intervals
index|[
name|i
index|]
decl_stmt|;
name|processingTimes
index|[
name|i
index|]
operator|=
name|registry
operator|.
name|newQuantiles
argument_list|(
name|LlapDaemonQueueInfo
operator|.
name|PercentileProcessingTime
operator|.
name|name
argument_list|()
operator|+
literal|"_"
operator|+
name|interval
operator|+
literal|"s"
argument_list|,
name|LlapDaemonQueueInfo
operator|.
name|PercentileProcessingTime
operator|.
name|description
argument_list|()
argument_list|,
literal|"ops"
argument_list|,
literal|"latency"
argument_list|,
name|interval
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|LlapDaemonQueueMetrics
name|create
parameter_list|(
name|String
name|displayName
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|int
index|[]
name|intervals
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
return|return
name|ms
operator|.
name|register
argument_list|(
name|displayName
argument_list|,
literal|null
argument_list|,
operator|new
name|LlapDaemonQueueMetrics
argument_list|(
name|displayName
argument_list|,
name|sessionId
argument_list|,
name|intervals
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
name|QueueMetrics
argument_list|)
operator|.
name|setContext
argument_list|(
name|MetricsUtils
operator|.
name|METRICS_CONTEXT
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
name|getQueueStats
argument_list|(
name|rb
argument_list|)
expr_stmt|;
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
specifier|public
name|void
name|setQueueSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|queueSize
operator|.
name|set
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addProcessingTime
parameter_list|(
name|long
name|latency
parameter_list|)
block|{
name|rateOfProcessing
operator|.
name|add
argument_list|(
name|latency
argument_list|)
expr_stmt|;
if|if
condition|(
name|latency
operator|>
name|maxTime
condition|)
block|{
name|maxTime
operator|=
name|latency
expr_stmt|;
name|maxProcessingTime
operator|.
name|set
argument_list|(
name|maxTime
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|MutableQuantiles
name|q
range|:
name|processingTimes
control|)
block|{
name|q
operator|.
name|add
argument_list|(
name|latency
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getQueueStats
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|rb
operator|.
name|addGauge
argument_list|(
name|QueueSize
argument_list|,
name|queueSize
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MaxProcessingTime
argument_list|,
name|maxProcessingTime
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MaxProcessingTime
argument_list|,
name|maxProcessingTime
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|rateOfProcessing
operator|.
name|snapshot
argument_list|(
name|rb
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|MutableQuantiles
name|q
range|:
name|processingTimes
control|)
block|{
name|q
operator|.
name|snapshot
argument_list|(
name|rb
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

