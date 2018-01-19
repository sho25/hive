begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
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
name|common
operator|.
name|JvmMetricsInfo
operator|.
name|*
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
name|log
operator|.
name|metrics
operator|.
name|EventCounter
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
name|lib
operator|.
name|DefaultMetricsSystem
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
name|Interns
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
name|GarbageCollectorMXBean
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
name|MemoryMXBean
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
name|MemoryUsage
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
name|ConcurrentHashMap
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

begin_comment
comment|/**  * JVM and logging related metrics. Ported from Hadoop JvmMetrics.  * Mostly used by various servers as a part of the metrics they export.  */
end_comment

begin_class
specifier|public
class|class
name|JvmMetrics
implements|implements
name|MetricsSource
block|{
enum|enum
name|Singleton
block|{
name|INSTANCE
block|;
name|JvmMetrics
name|impl
decl_stmt|;
specifier|synchronized
name|JvmMetrics
name|init
parameter_list|(
name|String
name|processName
parameter_list|,
name|String
name|sessionId
parameter_list|)
block|{
if|if
condition|(
name|impl
operator|==
literal|null
condition|)
block|{
name|impl
operator|=
name|create
argument_list|(
name|processName
argument_list|,
name|sessionId
argument_list|,
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|impl
return|;
block|}
block|}
specifier|static
specifier|final
name|float
name|M
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|final
name|MemoryMXBean
name|memoryMXBean
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|GarbageCollectorMXBean
argument_list|>
name|gcBeans
init|=
name|ManagementFactory
operator|.
name|getGarbageCollectorMXBeans
argument_list|()
decl_stmt|;
specifier|final
name|ThreadMXBean
name|threadMXBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
specifier|final
name|String
name|processName
decl_stmt|,
name|sessionId
decl_stmt|;
specifier|private
name|JvmPauseMonitor
name|pauseMonitor
init|=
literal|null
decl_stmt|;
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsInfo
index|[]
argument_list|>
name|gcInfoCache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MetricsInfo
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|JvmMetrics
parameter_list|(
name|String
name|processName
parameter_list|,
name|String
name|sessionId
parameter_list|)
block|{
name|this
operator|.
name|processName
operator|=
name|processName
expr_stmt|;
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
block|}
specifier|public
name|void
name|setPauseMonitor
parameter_list|(
specifier|final
name|JvmPauseMonitor
name|pauseMonitor
parameter_list|)
block|{
name|this
operator|.
name|pauseMonitor
operator|=
name|pauseMonitor
expr_stmt|;
block|}
specifier|public
specifier|static
name|JvmMetrics
name|create
parameter_list|(
name|String
name|processName
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|MetricsSystem
name|ms
parameter_list|)
block|{
return|return
name|ms
operator|.
name|register
argument_list|(
name|JvmMetrics
operator|.
name|name
argument_list|()
argument_list|,
name|JvmMetrics
operator|.
name|description
argument_list|()
argument_list|,
operator|new
name|JvmMetrics
argument_list|(
name|processName
argument_list|,
name|sessionId
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|JvmMetrics
name|initSingleton
parameter_list|(
name|String
name|processName
parameter_list|,
name|String
name|sessionId
parameter_list|)
block|{
return|return
name|Singleton
operator|.
name|INSTANCE
operator|.
name|init
argument_list|(
name|processName
argument_list|,
name|sessionId
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
name|all
parameter_list|)
block|{
name|MetricsRecordBuilder
name|rb
init|=
name|collector
operator|.
name|addRecord
argument_list|(
name|JvmMetrics
argument_list|)
operator|.
name|setContext
argument_list|(
literal|"jvm"
argument_list|)
operator|.
name|tag
argument_list|(
name|ProcessName
argument_list|,
name|processName
argument_list|)
operator|.
name|tag
argument_list|(
name|SessionId
argument_list|,
name|sessionId
argument_list|)
decl_stmt|;
name|getMemoryUsage
argument_list|(
name|rb
argument_list|)
expr_stmt|;
name|getGcUsage
argument_list|(
name|rb
argument_list|)
expr_stmt|;
name|getThreadUsage
argument_list|(
name|rb
argument_list|)
expr_stmt|;
name|getEventCounters
argument_list|(
name|rb
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getMemoryUsage
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|MemoryUsage
name|memNonHeap
init|=
name|memoryMXBean
operator|.
name|getNonHeapMemoryUsage
argument_list|()
decl_stmt|;
name|MemoryUsage
name|memHeap
init|=
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
decl_stmt|;
name|Runtime
name|runtime
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
decl_stmt|;
name|rb
operator|.
name|addGauge
argument_list|(
name|MemNonHeapUsedM
argument_list|,
name|memNonHeap
operator|.
name|getUsed
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemNonHeapCommittedM
argument_list|,
name|memNonHeap
operator|.
name|getCommitted
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemNonHeapMaxM
argument_list|,
name|memNonHeap
operator|.
name|getMax
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemHeapUsedM
argument_list|,
name|memHeap
operator|.
name|getUsed
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemHeapCommittedM
argument_list|,
name|memHeap
operator|.
name|getCommitted
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemHeapMaxM
argument_list|,
name|memHeap
operator|.
name|getMax
argument_list|()
operator|/
name|M
argument_list|)
operator|.
name|addGauge
argument_list|(
name|MemMaxM
argument_list|,
name|runtime
operator|.
name|maxMemory
argument_list|()
operator|/
name|M
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getGcUsage
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|long
name|count
init|=
literal|0
decl_stmt|;
name|long
name|timeMillis
init|=
literal|0
decl_stmt|;
for|for
control|(
name|GarbageCollectorMXBean
name|gcBean
range|:
name|gcBeans
control|)
block|{
name|long
name|c
init|=
name|gcBean
operator|.
name|getCollectionCount
argument_list|()
decl_stmt|;
name|long
name|t
init|=
name|gcBean
operator|.
name|getCollectionTime
argument_list|()
decl_stmt|;
name|MetricsInfo
index|[]
name|gcInfo
init|=
name|getGcInfo
argument_list|(
name|gcBean
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|rb
operator|.
name|addCounter
argument_list|(
name|gcInfo
index|[
literal|0
index|]
argument_list|,
name|c
argument_list|)
operator|.
name|addCounter
argument_list|(
name|gcInfo
index|[
literal|1
index|]
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|count
operator|+=
name|c
expr_stmt|;
name|timeMillis
operator|+=
name|t
expr_stmt|;
block|}
name|rb
operator|.
name|addCounter
argument_list|(
name|GcCount
argument_list|,
name|count
argument_list|)
operator|.
name|addCounter
argument_list|(
name|GcTimeMillis
argument_list|,
name|timeMillis
argument_list|)
expr_stmt|;
if|if
condition|(
name|pauseMonitor
operator|!=
literal|null
condition|)
block|{
name|rb
operator|.
name|addCounter
argument_list|(
name|GcNumWarnThresholdExceeded
argument_list|,
name|pauseMonitor
operator|.
name|getNumGcWarnThreadholdExceeded
argument_list|()
argument_list|)
expr_stmt|;
name|rb
operator|.
name|addCounter
argument_list|(
name|GcNumInfoThresholdExceeded
argument_list|,
name|pauseMonitor
operator|.
name|getNumGcInfoThresholdExceeded
argument_list|()
argument_list|)
expr_stmt|;
name|rb
operator|.
name|addCounter
argument_list|(
name|GcTotalExtraSleepTime
argument_list|,
name|pauseMonitor
operator|.
name|getTotalGcExtraSleepTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|MetricsInfo
index|[]
name|getGcInfo
parameter_list|(
name|String
name|gcName
parameter_list|)
block|{
name|MetricsInfo
index|[]
name|gcInfo
init|=
name|gcInfoCache
operator|.
name|get
argument_list|(
name|gcName
argument_list|)
decl_stmt|;
if|if
condition|(
name|gcInfo
operator|==
literal|null
condition|)
block|{
name|gcInfo
operator|=
operator|new
name|MetricsInfo
index|[
literal|2
index|]
expr_stmt|;
name|gcInfo
index|[
literal|0
index|]
operator|=
name|Interns
operator|.
name|info
argument_list|(
literal|"GcCount"
operator|+
name|gcName
argument_list|,
literal|"GC Count for "
operator|+
name|gcName
argument_list|)
expr_stmt|;
name|gcInfo
index|[
literal|1
index|]
operator|=
name|Interns
operator|.
name|info
argument_list|(
literal|"GcTimeMillis"
operator|+
name|gcName
argument_list|,
literal|"GC Time for "
operator|+
name|gcName
argument_list|)
expr_stmt|;
name|MetricsInfo
index|[]
name|previousGcInfo
init|=
name|gcInfoCache
operator|.
name|putIfAbsent
argument_list|(
name|gcName
argument_list|,
name|gcInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousGcInfo
operator|!=
literal|null
condition|)
block|{
return|return
name|previousGcInfo
return|;
block|}
block|}
return|return
name|gcInfo
return|;
block|}
specifier|private
name|void
name|getThreadUsage
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|int
name|threadsNew
init|=
literal|0
decl_stmt|;
name|int
name|threadsRunnable
init|=
literal|0
decl_stmt|;
name|int
name|threadsBlocked
init|=
literal|0
decl_stmt|;
name|int
name|threadsWaiting
init|=
literal|0
decl_stmt|;
name|int
name|threadsTimedWaiting
init|=
literal|0
decl_stmt|;
name|int
name|threadsTerminated
init|=
literal|0
decl_stmt|;
name|long
name|threadIds
index|[]
init|=
name|threadMXBean
operator|.
name|getAllThreadIds
argument_list|()
decl_stmt|;
for|for
control|(
name|ThreadInfo
name|threadInfo
range|:
name|threadMXBean
operator|.
name|getThreadInfo
argument_list|(
name|threadIds
argument_list|,
literal|0
argument_list|)
control|)
block|{
if|if
condition|(
name|threadInfo
operator|==
literal|null
condition|)
continue|continue;
comment|// race protection
switch|switch
condition|(
name|threadInfo
operator|.
name|getThreadState
argument_list|()
condition|)
block|{
case|case
name|NEW
case|:
name|threadsNew
operator|++
expr_stmt|;
break|break;
case|case
name|RUNNABLE
case|:
name|threadsRunnable
operator|++
expr_stmt|;
break|break;
case|case
name|BLOCKED
case|:
name|threadsBlocked
operator|++
expr_stmt|;
break|break;
case|case
name|WAITING
case|:
name|threadsWaiting
operator|++
expr_stmt|;
break|break;
case|case
name|TIMED_WAITING
case|:
name|threadsTimedWaiting
operator|++
expr_stmt|;
break|break;
case|case
name|TERMINATED
case|:
name|threadsTerminated
operator|++
expr_stmt|;
break|break;
block|}
block|}
name|rb
operator|.
name|addGauge
argument_list|(
name|ThreadsNew
argument_list|,
name|threadsNew
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ThreadsRunnable
argument_list|,
name|threadsRunnable
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ThreadsBlocked
argument_list|,
name|threadsBlocked
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ThreadsWaiting
argument_list|,
name|threadsWaiting
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ThreadsTimedWaiting
argument_list|,
name|threadsTimedWaiting
argument_list|)
operator|.
name|addGauge
argument_list|(
name|ThreadsTerminated
argument_list|,
name|threadsTerminated
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getEventCounters
parameter_list|(
name|MetricsRecordBuilder
name|rb
parameter_list|)
block|{
name|rb
operator|.
name|addCounter
argument_list|(
name|LogFatal
argument_list|,
name|EventCounter
operator|.
name|getFatal
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|LogError
argument_list|,
name|EventCounter
operator|.
name|getError
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|LogWarn
argument_list|,
name|EventCounter
operator|.
name|getWarn
argument_list|()
argument_list|)
operator|.
name|addCounter
argument_list|(
name|LogInfo
argument_list|,
name|EventCounter
operator|.
name|getInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

