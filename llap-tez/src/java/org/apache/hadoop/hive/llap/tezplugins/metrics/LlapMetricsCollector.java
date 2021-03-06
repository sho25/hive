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
name|llap
operator|.
name|tezplugins
operator|.
name|metrics
package|;
end_package

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
name|ThreadFactoryBuilder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|impl
operator|.
name|LlapManagementProtocolClientImpl
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
name|registry
operator|.
name|LlapServiceInstance
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
name|registry
operator|.
name|LlapServiceInstanceSet
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
name|registry
operator|.
name|impl
operator|.
name|LlapRegistryService
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
name|registry
operator|.
name|ServiceInstanceStateChangeListener
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
name|Service
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
name|ServiceStateChangeListener
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|ScheduledExecutorService
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

begin_comment
comment|/**  * Collect metrics from the llap daemons in every given milliseconds.  */
end_comment

begin_class
specifier|public
class|class
name|LlapMetricsCollector
implements|implements
name|ServiceStateChangeListener
implements|,
name|ServiceInstanceStateChangeListener
argument_list|<
name|LlapServiceInstance
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
name|LlapMetricsCollector
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|THREAD_NAME
init|=
literal|"LlapTaskSchedulerMetricsCollectorThread"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|INITIAL_DELAY_MSEC
init|=
literal|10000L
decl_stmt|;
specifier|private
specifier|final
name|ScheduledExecutorService
name|scheduledMetricsExecutor
decl_stmt|;
specifier|private
specifier|final
name|LlapManagementProtocolClientImplFactory
name|clientFactory
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LlapManagementProtocolClientImpl
argument_list|>
name|llapClients
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LlapMetrics
argument_list|>
name|instanceStatisticsMap
decl_stmt|;
specifier|private
specifier|final
name|long
name|metricsCollectionMs
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|final
name|LlapMetricsListener
name|listener
decl_stmt|;
specifier|public
name|LlapMetricsCollector
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LlapRegistryService
name|registry
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
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
name|THREAD_NAME
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|,
name|LlapManagementProtocolClientImplFactory
operator|.
name|basicInstance
argument_list|(
name|conf
argument_list|)
argument_list|,
name|registry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|LlapMetricsCollector
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ScheduledExecutorService
name|scheduledMetricsExecutor
parameter_list|,
name|LlapManagementProtocolClientImplFactory
name|clientFactory
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|scheduledMetricsExecutor
argument_list|,
name|clientFactory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|LlapMetricsCollector
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ScheduledExecutorService
name|scheduledMetricsExecutor
parameter_list|,
name|LlapManagementProtocolClientImplFactory
name|clientFactory
parameter_list|,
name|LlapRegistryService
name|registry
parameter_list|)
block|{
name|this
operator|.
name|scheduledMetricsExecutor
operator|=
name|scheduledMetricsExecutor
expr_stmt|;
name|this
operator|.
name|clientFactory
operator|=
name|clientFactory
expr_stmt|;
name|this
operator|.
name|llapClients
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|instanceStatisticsMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|metricsCollectionMs
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|String
name|listenerClass
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
argument_list|)
decl_stmt|;
if|if
condition|(
name|listenerClass
operator|==
literal|null
operator|||
name|listenerClass
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listener
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|listener
operator|=
operator|(
name|LlapMetricsListener
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|listenerClass
operator|.
name|trim
argument_list|()
argument_list|)
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|listener
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|registry
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wrong configuration for "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
operator|+
literal|" "
operator|+
name|listenerClass
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|metricsCollectionMs
operator|>
literal|0
condition|)
block|{
name|scheduledMetricsExecutor
operator|.
name|scheduleAtFixedRate
argument_list|(
parameter_list|()
lambda|->
block|{
name|collectMetrics
argument_list|()
expr_stmt|;
block|}
argument_list|,
name|INITIAL_DELAY_MSEC
argument_list|,
name|metricsCollectionMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|scheduledMetricsExecutor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|collectMetrics
parameter_list|()
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|LlapManagementProtocolClientImpl
argument_list|>
name|entry
range|:
name|llapClients
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|identity
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|LlapManagementProtocolClientImpl
name|client
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsResponseProto
name|metrics
init|=
name|client
operator|.
name|getDaemonMetrics
argument_list|(
literal|null
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|LlapMetrics
name|newMetrics
init|=
operator|new
name|LlapMetrics
argument_list|(
name|metrics
argument_list|)
decl_stmt|;
name|instanceStatisticsMap
operator|.
name|put
argument_list|(
name|identity
argument_list|,
name|newMetrics
argument_list|)
expr_stmt|;
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|listener
operator|.
name|newDaemonMetrics
argument_list|(
name|identity
argument_list|,
name|newMetrics
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"LlapMetricsListener thrown an unexpected exception"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|ServiceException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|instanceStatisticsMap
operator|.
name|remove
argument_list|(
name|identity
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|listener
operator|.
name|newClusterMetrics
argument_list|(
name|getMetrics
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"LlapMetricsListener thrown an unexpected exception"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stateChanged
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
if|if
condition|(
name|service
operator|.
name|getServiceState
argument_list|()
operator|==
name|Service
operator|.
name|STATE
operator|.
name|STARTED
condition|)
block|{
if|if
condition|(
name|service
operator|instanceof
name|LlapRegistryService
condition|)
block|{
name|setupLlapRegistryService
argument_list|(
operator|(
name|LlapRegistryService
operator|)
name|service
argument_list|)
expr_stmt|;
block|}
name|start
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|service
operator|.
name|getServiceState
argument_list|()
operator|==
name|Service
operator|.
name|STATE
operator|.
name|STOPPED
condition|)
block|{
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setupLlapRegistryService
parameter_list|(
name|LlapRegistryService
name|service
parameter_list|)
block|{
try|try
block|{
name|consumeInitialInstances
argument_list|(
name|service
argument_list|)
expr_stmt|;
name|service
operator|.
name|registerStateChangeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
name|void
name|consumeInitialInstances
parameter_list|(
name|LlapRegistryService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapServiceInstanceSet
name|serviceInstances
init|=
name|service
operator|.
name|getInstances
argument_list|()
decl_stmt|;
for|for
control|(
name|LlapServiceInstance
name|serviceInstance
range|:
name|serviceInstances
operator|.
name|getAll
argument_list|()
control|)
block|{
name|onCreate
argument_list|(
name|serviceInstance
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onCreate
parameter_list|(
name|LlapServiceInstance
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
block|{
name|LlapManagementProtocolClientImpl
name|client
init|=
name|clientFactory
operator|.
name|create
argument_list|(
name|serviceInstance
argument_list|)
decl_stmt|;
name|llapClients
operator|.
name|put
argument_list|(
name|serviceInstance
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onUpdate
parameter_list|(
name|LlapServiceInstance
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
block|{
comment|//NOOP
block|}
annotation|@
name|Override
specifier|public
name|void
name|onRemove
parameter_list|(
name|LlapServiceInstance
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
block|{
name|String
name|workerIdentity
init|=
name|serviceInstance
operator|.
name|getWorkerIdentity
argument_list|()
decl_stmt|;
name|llapClients
operator|.
name|remove
argument_list|(
name|workerIdentity
argument_list|)
expr_stmt|;
name|instanceStatisticsMap
operator|.
name|remove
argument_list|(
name|workerIdentity
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LlapMetrics
name|getMetrics
parameter_list|(
name|String
name|workerIdentity
parameter_list|)
block|{
return|return
name|instanceStatisticsMap
operator|.
name|get
argument_list|(
name|workerIdentity
argument_list|)
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|LlapMetrics
argument_list|>
name|getMetrics
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|instanceStatisticsMap
argument_list|)
return|;
block|}
comment|/**    * Stores the metrics retrieved from the llap daemons, along with the retrieval timestamp.    */
specifier|public
specifier|static
class|class
name|LlapMetrics
block|{
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|metrics
decl_stmt|;
annotation|@
name|VisibleForTesting
name|LlapMetrics
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
specifier|public
name|LlapMetrics
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsResponseProto
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|(
name|metrics
operator|.
name|getMetricsCount
argument_list|()
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|getMetricsList
argument_list|()
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
name|this
operator|.
name|metrics
operator|.
name|put
argument_list|(
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
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**      * The metric values in the map. The keys are the enum names (See: LlapDaemonExecutorInfo), and      * the values are the actual values.      * @return The metric map      */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
block|}
block|}
end_class

end_unit

