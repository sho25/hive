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
name|common
operator|.
name|metrics
operator|.
name|metrics2
package|;
end_package

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|ConsoleReporter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Counter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|ExponentiallyDecayingReservoir
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Gauge
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|JmxReporter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Metric
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricRegistry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|json
operator|.
name|MetricsModule
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|jvm
operator|.
name|BufferPoolMetricSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|jvm
operator|.
name|ClassLoadingGaugeSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|jvm
operator|.
name|GarbageCollectorMetricSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|jvm
operator|.
name|MemoryUsageGaugeSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|jvm
operator|.
name|ThreadStatesGaugeSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|joshelser
operator|.
name|dropwizard
operator|.
name|metrics
operator|.
name|hadoop
operator|.
name|HadoopMetrics2Reporter
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
name|base
operator|.
name|Splitter
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
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
name|fs
operator|.
name|Path
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
name|permission
operator|.
name|FsPermission
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsScope
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsVariable
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
name|java
operator|.
name|io
operator|.
name|BufferedWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|OutputStreamWriter
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
name|net
operator|.
name|URI
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimerTask
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
name|ExecutionException
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
name|locks
operator|.
name|Lock
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

begin_comment
comment|/**  * Codahale-backed Metrics implementation.  */
end_comment

begin_class
specifier|public
class|class
name|CodahaleMetrics
implements|implements
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
name|metrics
operator|.
name|common
operator|.
name|Metrics
block|{
specifier|public
specifier|static
specifier|final
name|String
name|API_PREFIX
init|=
literal|"api_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ACTIVE_CALLS
init|=
literal|"active_calls_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CodahaleMetrics
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|MetricRegistry
name|metricRegistry
init|=
operator|new
name|MetricRegistry
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Lock
name|timersLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Lock
name|countersLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Lock
name|gaugesLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|Timer
argument_list|>
name|timers
decl_stmt|;
specifier|private
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|Counter
argument_list|>
name|counters
decl_stmt|;
specifier|private
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Gauge
argument_list|>
name|gauges
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|Closeable
argument_list|>
name|reporters
init|=
operator|new
name|HashSet
argument_list|<
name|Closeable
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|CodahaleMetricsScope
argument_list|>
argument_list|>
name|threadLocalScopes
init|=
operator|new
name|ThreadLocal
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|CodahaleMetricsScope
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|CodahaleMetricsScope
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|CodahaleMetricsScope
argument_list|>
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|public
class|class
name|CodahaleMetricsScope
implements|implements
name|MetricsScope
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|Timer
name|timer
decl_stmt|;
specifier|private
name|Timer
operator|.
name|Context
name|timerContext
decl_stmt|;
specifier|private
name|boolean
name|isOpen
init|=
literal|false
decl_stmt|;
comment|/**      * Instantiates a named scope - intended to only be called by Metrics, so locally scoped.      * @param name - name of the variable      */
specifier|private
name|CodahaleMetricsScope
parameter_list|(
name|String
name|name
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
name|timer
operator|=
name|CodahaleMetrics
operator|.
name|this
operator|.
name|getTimer
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|open
argument_list|()
expr_stmt|;
block|}
comment|/**      * Opens scope, and makes note of the time started, increments run counter      *      */
specifier|public
name|void
name|open
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isOpen
condition|)
block|{
name|isOpen
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|timerContext
operator|=
name|timer
operator|.
name|time
argument_list|()
expr_stmt|;
name|CodahaleMetrics
operator|.
name|this
operator|.
name|incrementCounter
argument_list|(
name|ACTIVE_CALLS
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Scope named "
operator|+
name|name
operator|+
literal|" is not closed, cannot be opened."
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Closes scope, and records the time taken      */
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|isOpen
condition|)
block|{
name|timerContext
operator|.
name|close
argument_list|()
expr_stmt|;
name|CodahaleMetrics
operator|.
name|this
operator|.
name|decrementCounter
argument_list|(
name|ACTIVE_CALLS
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Scope named "
operator|+
name|name
operator|+
literal|" is not open, cannot be closed."
argument_list|)
expr_stmt|;
block|}
name|isOpen
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|public
name|CodahaleMetrics
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|//Codahale artifacts are lazily-created.
name|timers
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
name|load
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Timer
name|timer
init|=
operator|new
name|Timer
argument_list|(
operator|new
name|ExponentiallyDecayingReservoir
argument_list|()
argument_list|)
decl_stmt|;
name|metricRegistry
operator|.
name|register
argument_list|(
name|key
argument_list|,
name|timer
argument_list|)
expr_stmt|;
return|return
name|timer
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|counters
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|Counter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Counter
name|load
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Counter
name|counter
init|=
operator|new
name|Counter
argument_list|()
decl_stmt|;
name|metricRegistry
operator|.
name|register
argument_list|(
name|key
argument_list|,
name|counter
argument_list|)
expr_stmt|;
return|return
name|counter
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|gauges
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Gauge
argument_list|>
argument_list|()
expr_stmt|;
comment|//register JVM metrics
name|registerAll
argument_list|(
literal|"gc"
argument_list|,
operator|new
name|GarbageCollectorMetricSet
argument_list|()
argument_list|)
expr_stmt|;
name|registerAll
argument_list|(
literal|"buffers"
argument_list|,
operator|new
name|BufferPoolMetricSet
argument_list|(
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|registerAll
argument_list|(
literal|"memory"
argument_list|,
operator|new
name|MemoryUsageGaugeSet
argument_list|()
argument_list|)
expr_stmt|;
name|registerAll
argument_list|(
literal|"threads"
argument_list|,
operator|new
name|ThreadStatesGaugeSet
argument_list|()
argument_list|)
expr_stmt|;
name|registerAll
argument_list|(
literal|"classLoading"
argument_list|,
operator|new
name|ClassLoadingGaugeSet
argument_list|()
argument_list|)
expr_stmt|;
comment|//Metrics reporter
name|Set
argument_list|<
name|MetricsReporting
argument_list|>
name|finalReporterList
init|=
operator|new
name|HashSet
argument_list|<
name|MetricsReporting
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|metricsReporterNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|trimResults
argument_list|()
operator|.
name|omitEmptyStrings
argument_list|()
operator|.
name|split
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_REPORTER
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricsReporterNames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|metricsReportingName
range|:
name|metricsReporterNames
control|)
block|{
try|try
block|{
name|MetricsReporting
name|reporter
init|=
name|MetricsReporting
operator|.
name|valueOf
argument_list|(
name|metricsReportingName
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
argument_list|)
decl_stmt|;
name|finalReporterList
operator|.
name|add
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Metrics reporter skipped due to invalid configured reporter: "
operator|+
name|metricsReportingName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|initReporting
argument_list|(
name|finalReporterList
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|reporters
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Closeable
name|reporter
range|:
name|reporters
control|)
block|{
name|reporter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Metric
argument_list|>
name|metric
range|:
name|metricRegistry
operator|.
name|getMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|metricRegistry
operator|.
name|remove
argument_list|(
name|metric
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|timers
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
name|counters
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startStoredScope
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|name
operator|=
name|API_PREFIX
operator|+
name|name
expr_stmt|;
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|CodahaleMetricsScope
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|endStoredScope
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|name
operator|=
name|API_PREFIX
operator|+
name|name
expr_stmt|;
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|MetricsScope
name|getStoredScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No metrics scope named "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
specifier|public
name|MetricsScope
name|createScope
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|name
operator|=
name|API_PREFIX
operator|+
name|name
expr_stmt|;
return|return
operator|new
name|CodahaleMetricsScope
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|endScope
parameter_list|(
name|MetricsScope
name|scope
parameter_list|)
block|{
operator|(
operator|(
name|CodahaleMetricsScope
operator|)
name|scope
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|incrementCounter
argument_list|(
name|name
argument_list|,
literal|1L
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|increment
parameter_list|)
block|{
name|String
name|key
init|=
name|name
decl_stmt|;
try|try
block|{
name|countersLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|counters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|inc
argument_list|(
name|increment
argument_list|)
expr_stmt|;
return|return
name|counters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|getCount
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Error retrieving counter from the metric registry "
argument_list|,
name|ee
argument_list|)
throw|;
block|}
finally|finally
block|{
name|countersLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Long
name|decrementCounter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|decrementCounter
argument_list|(
name|name
argument_list|,
literal|1L
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|decrementCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|decrement
parameter_list|)
block|{
name|String
name|key
init|=
name|name
decl_stmt|;
try|try
block|{
name|countersLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|counters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|dec
argument_list|(
name|decrement
argument_list|)
expr_stmt|;
return|return
name|counters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|getCount
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Error retrieving counter from the metric registry "
argument_list|,
name|ee
argument_list|)
throw|;
block|}
finally|finally
block|{
name|countersLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addGauge
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|MetricsVariable
name|variable
parameter_list|)
block|{
name|Gauge
name|gauge
init|=
operator|new
name|Gauge
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
return|return
name|variable
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|gaugesLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|gauges
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|gauge
argument_list|)
expr_stmt|;
comment|// Metrics throws an Exception if we don't do this when the key already exists
if|if
condition|(
name|metricRegistry
operator|.
name|getGauges
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"A Gauge with name ["
operator|+
name|name
operator|+
literal|"] already exists. "
operator|+
literal|" The old gauge will be overwritten, but this is not recommended"
argument_list|)
expr_stmt|;
name|metricRegistry
operator|.
name|remove
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|metricRegistry
operator|.
name|register
argument_list|(
name|name
argument_list|,
name|gauge
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|gaugesLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// This method is necessary to synchronize lazy-creation to the timers.
specifier|private
name|Timer
name|getTimer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|key
init|=
name|name
decl_stmt|;
try|try
block|{
name|timersLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|Timer
name|timer
init|=
name|timers
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|timer
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Error retrieving timer from the metric registry "
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|timersLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|registerAll
parameter_list|(
name|String
name|prefix
parameter_list|,
name|MetricSet
name|metricSet
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Metric
argument_list|>
name|entry
range|:
name|metricSet
operator|.
name|getMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|MetricSet
condition|)
block|{
name|registerAll
argument_list|(
name|prefix
operator|+
literal|"."
operator|+
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|MetricSet
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|metricRegistry
operator|.
name|register
argument_list|(
name|prefix
operator|+
literal|"."
operator|+
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MetricRegistry
name|getMetricRegistry
parameter_list|()
block|{
return|return
name|metricRegistry
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|String
name|dumpJson
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectMapper
name|jsonMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
operator|.
name|registerModule
argument_list|(
operator|new
name|MetricsModule
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|jsonMapper
operator|.
name|writerWithDefaultPrettyPrinter
argument_list|()
operator|.
name|writeValueAsString
argument_list|(
name|metricRegistry
argument_list|)
return|;
block|}
comment|/**    * Should be only called once to initialize the reporters    */
specifier|private
name|void
name|initReporting
parameter_list|(
name|Set
argument_list|<
name|MetricsReporting
argument_list|>
name|reportingSet
parameter_list|)
block|{
for|for
control|(
name|MetricsReporting
name|reporting
range|:
name|reportingSet
control|)
block|{
switch|switch
condition|(
name|reporting
condition|)
block|{
case|case
name|CONSOLE
case|:
specifier|final
name|ConsoleReporter
name|consoleReporter
init|=
name|ConsoleReporter
operator|.
name|forRegistry
argument_list|(
name|metricRegistry
argument_list|)
operator|.
name|convertRatesTo
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|convertDurationsTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|consoleReporter
operator|.
name|start
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|reporters
operator|.
name|add
argument_list|(
name|consoleReporter
argument_list|)
expr_stmt|;
break|break;
case|case
name|JMX
case|:
specifier|final
name|JmxReporter
name|jmxReporter
init|=
name|JmxReporter
operator|.
name|forRegistry
argument_list|(
name|metricRegistry
argument_list|)
operator|.
name|convertRatesTo
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|convertDurationsTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|jmxReporter
operator|.
name|start
argument_list|()
expr_stmt|;
name|reporters
operator|.
name|add
argument_list|(
name|jmxReporter
argument_list|)
expr_stmt|;
break|break;
case|case
name|JSON_FILE
case|:
specifier|final
name|JsonFileReporter
name|jsonFileReporter
init|=
operator|new
name|JsonFileReporter
argument_list|()
decl_stmt|;
name|jsonFileReporter
operator|.
name|start
argument_list|()
expr_stmt|;
name|reporters
operator|.
name|add
argument_list|(
name|jsonFileReporter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HADOOP2
case|:
name|String
name|applicationName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_HADOOP2_COMPONENT_NAME
operator|.
name|varname
argument_list|)
decl_stmt|;
name|long
name|reportingInterval
init|=
name|HiveConf
operator|.
name|toTime
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_HADOOP2_INTERVAL
operator|.
name|varname
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
specifier|final
name|HadoopMetrics2Reporter
name|metrics2Reporter
init|=
name|HadoopMetrics2Reporter
operator|.
name|forRegistry
argument_list|(
name|metricRegistry
argument_list|)
operator|.
name|convertRatesTo
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|convertDurationsTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|build
argument_list|(
name|DefaultMetricsSystem
operator|.
name|initialize
argument_list|(
name|applicationName
argument_list|)
argument_list|,
comment|// The application-level name
name|applicationName
argument_list|,
comment|// Component name
name|applicationName
argument_list|,
comment|// Component description
literal|"General"
argument_list|)
decl_stmt|;
comment|// Name for each metric record
name|metrics2Reporter
operator|.
name|start
argument_list|(
name|reportingInterval
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
class|class
name|JsonFileReporter
implements|implements
name|Closeable
block|{
specifier|private
name|ObjectMapper
name|jsonMapper
init|=
literal|null
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|Timer
name|timer
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|jsonMapper
operator|=
operator|new
name|ObjectMapper
argument_list|()
operator|.
name|registerModule
argument_list|(
operator|new
name|MetricsModule
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|timer
operator|=
operator|new
name|java
operator|.
name|util
operator|.
name|Timer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|long
name|time
init|=
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_JSON_FILE_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|pathString
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_JSON_FILE_LOCATION
argument_list|)
decl_stmt|;
name|timer
operator|.
name|schedule
argument_list|(
operator|new
name|TimerTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|BufferedWriter
name|bw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|json
init|=
name|jsonMapper
operator|.
name|writerWithDefaultPrettyPrinter
argument_list|()
operator|.
name|writeValueAsString
argument_list|(
name|metricRegistry
argument_list|)
decl_stmt|;
name|Path
name|tmpPath
init|=
operator|new
name|Path
argument_list|(
name|pathString
operator|+
literal|".tmp"
argument_list|)
decl_stmt|;
name|URI
name|tmpPathURI
init|=
name|tmpPath
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tmpPathURI
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
operator|&&
name|tmpPathURI
operator|.
name|getAuthority
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|//default local
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|tmpPathURI
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|tmpPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|bw
operator|=
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
name|tmpPath
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|bw
operator|.
name|write
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|bw
operator|.
name|close
argument_list|()
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|tmpPath
argument_list|,
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0644
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|pathString
argument_list|)
decl_stmt|;
name|fs
operator|.
name|rename
argument_list|(
name|tmpPath
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|path
argument_list|,
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0644
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Error writing JSON Metrics to file"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|bw
operator|!=
literal|null
condition|)
block|{
name|bw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|//Ignore.
block|}
block|}
block|}
block|}
argument_list|,
literal|0
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|timer
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|timer
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

