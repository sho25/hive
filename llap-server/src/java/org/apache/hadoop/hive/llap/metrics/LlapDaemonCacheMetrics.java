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
name|LlapDaemonCacheInfo
operator|.
name|CacheAllocatedArena
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
name|LlapDaemonCacheInfo
operator|.
name|CacheCapacityRemaining
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
name|LlapDaemonCacheInfo
operator|.
name|CacheCapacityRemainingPercentage
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
name|LlapDaemonCacheInfo
operator|.
name|CacheCapacityTotal
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
name|LlapDaemonCacheInfo
operator|.
name|CacheCapacityUsed
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
name|LlapDaemonCacheInfo
operator|.
name|CacheHitBytes
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
name|LlapDaemonCacheInfo
operator|.
name|CacheHitRatio
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
name|LlapDaemonCacheInfo
operator|.
name|CacheMetrics
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
name|LlapDaemonCacheInfo
operator|.
name|CacheNumLockedBuffers
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
name|LlapDaemonCacheInfo
operator|.
name|CacheReadRequests
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
name|LlapDaemonCacheInfo
operator|.
name|CacheRequestedBytes
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
name|MutableGaugeLong
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

begin_comment
comment|/**  * Llap daemon cache metrics source.  */
end_comment

begin_class
annotation|@
name|Metrics
argument_list|(
name|about
operator|=
literal|"LlapDaemon Cache Metrics"
argument_list|,
name|context
operator|=
literal|"cache"
argument_list|)
specifier|public
class|class
name|LlapDaemonCacheMetrics
implements|implements
name|MetricsSource
block|{
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
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
name|MutableCounterLong
name|cacheReadRequests
decl_stmt|;
annotation|@
name|Metric
name|MutableGaugeLong
name|cacheCapacityTotal
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|cacheCapacityUsed
decl_stmt|;
comment|// Not using the gauge to avoid races.
annotation|@
name|Metric
name|MutableCounterLong
name|cacheRequestedBytes
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|cacheHitBytes
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|cacheAllocatedArena
decl_stmt|;
annotation|@
name|Metric
name|MutableCounterLong
name|cacheNumLockedBuffers
decl_stmt|;
specifier|private
name|LlapDaemonCacheMetrics
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|sessionId
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
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
literal|"LlapDaemonCacheRegistry"
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
name|LlapDaemonCacheMetrics
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
name|LlapDaemonCacheMetrics
argument_list|(
name|displayName
argument_list|,
name|sessionId
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|setCacheCapacityTotal
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|cacheCapacityTotal
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrCacheCapacityUsed
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|cacheCapacityUsed
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrCacheRequestedBytes
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|cacheRequestedBytes
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrCacheHitBytes
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|cacheHitBytes
operator|.
name|incr
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrCacheReadRequests
parameter_list|()
block|{
name|cacheReadRequests
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrAllocatedArena
parameter_list|()
block|{
name|cacheAllocatedArena
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrCacheNumLockedBuffers
parameter_list|()
block|{
name|cacheNumLockedBuffers
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decrCacheNumLockedBuffers
parameter_list|()
block|{
name|cacheNumLockedBuffers
operator|.
name|incr
argument_list|(
operator|-
literal|1
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
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getCacheRequestedBytes
parameter_list|()
block|{
return|return
name|cacheRequestedBytes
operator|.
name|value
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getCacheHitBytes
parameter_list|()
block|{
return|return
name|cacheHitBytes
operator|.
name|value
argument_list|()
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
name|CacheMetrics
argument_list|)
operator|.
name|setContext
argument_list|(
literal|"cache"
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
name|getCacheStats
argument_list|(
name|rb
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getCacheStats
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|float
name|cacheHitRatio
init|=
name|cacheRequestedBytes
operator|.
name|value
argument_list|()
operator|==
literal|0
condition|?
literal|0.0f
else|:
operator|(
name|float
operator|)
name|cacheHitBytes
operator|.
name|value
argument_list|()
operator|/
operator|(
name|float
operator|)
name|cacheRequestedBytes
operator|.
name|value
argument_list|()
decl_stmt|;
name|long
name|cacheCapacityRemaining
init|=
name|cacheCapacityTotal
operator|.
name|value
argument_list|()
operator|-
name|cacheCapacityUsed
operator|.
name|value
argument_list|()
decl_stmt|;
name|float
name|cacheRemainingPercent
init|=
name|cacheCapacityTotal
operator|.
name|value
argument_list|()
operator|==
literal|0
condition|?
literal|0.0f
else|:
operator|(
name|float
operator|)
name|cacheCapacityRemaining
operator|/
operator|(
name|float
operator|)
name|cacheCapacityTotal
operator|.
name|value
argument_list|()
decl_stmt|;
name|rb
operator|.
name|addCounter
argument_list|(
name|CacheCapacityRemaining
argument_list|,
name|cacheCapacityRemaining
argument_list|)
operator|.
name|addGauge
argument_list|(
name|CacheCapacityRemainingPercentage
argument_list|,
name|cacheRemainingPercent
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheCapacityTotal
argument_list|,
name|cacheCapacityTotal
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheCapacityUsed
argument_list|,
name|cacheCapacityUsed
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheReadRequests
argument_list|,
name|cacheReadRequests
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheRequestedBytes
argument_list|,
name|cacheRequestedBytes
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheHitBytes
argument_list|,
name|cacheHitBytes
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheAllocatedArena
argument_list|,
name|cacheAllocatedArena
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|CacheNumLockedBuffers
argument_list|,
name|cacheNumLockedBuffers
operator|.
name|value
argument_list|()
argument_list|)
operator|.
name|addGauge
argument_list|(
name|CacheHitRatio
argument_list|,
name|cacheHitRatio
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

