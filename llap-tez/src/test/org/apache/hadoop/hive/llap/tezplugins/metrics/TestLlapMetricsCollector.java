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
name|collect
operator|.
name|Lists
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
name|RpcController
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
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyLong
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|any
import|;
end_import

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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|MockitoAnnotations
operator|.
name|initMocks
import|;
end_import

begin_class
specifier|public
class|class
name|TestLlapMetricsCollector
block|{
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_TIMEOUT
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TEST_SEQ_VERSION
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_IDENTITY_1
init|=
literal|"testInstance_1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_IDENTITY_2
init|=
literal|"testInstance_2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsResponseProto
name|TEST_RESPONSE
init|=
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsResponseProto
operator|.
name|getDefaultInstance
argument_list|()
decl_stmt|;
specifier|private
name|LlapMetricsCollector
name|collector
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Configuration
name|mockConf
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|LlapManagementProtocolClientImplFactory
name|mockClientFactory
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|LlapManagementProtocolClientImpl
name|mockClient
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|ScheduledExecutorService
name|mockExecutor
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|ServiceException
block|{
name|initMocks
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"30000ms"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|MockListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockClientFactory
operator|.
name|create
argument_list|(
name|any
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockClient
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockClient
operator|.
name|getDaemonMetrics
argument_list|(
name|any
argument_list|(
name|RpcController
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|GetDaemonMetricsRequestProto
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_RESPONSE
argument_list|)
expr_stmt|;
name|collector
operator|=
operator|new
name|LlapMetricsCollector
argument_list|(
name|mockConf
argument_list|,
name|mockExecutor
argument_list|,
name|mockClientFactory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testAddService
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testRemoveService
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|collector
operator|.
name|onRemove
argument_list|(
name|mockService
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testMultipleCollectOnSameInstance
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|LlapMetricsCollector
operator|.
name|LlapMetrics
name|metrics1
init|=
name|collector
operator|.
name|getMetrics
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
decl_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|LlapMetricsCollector
operator|.
name|LlapMetrics
name|metrics2
init|=
name|collector
operator|.
name|getMetrics
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
decl_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|metrics1
argument_list|,
name|metrics2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testCollectOnMultipleInstances
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService1
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService1
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|mockService2
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService2
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_2
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService1
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService2
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testMultipleCollectOnMultipleInstances
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService1
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService1
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|mockService2
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService2
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_2
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService1
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService2
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testStartStartsScheduling
parameter_list|()
block|{
comment|// When
name|collector
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Then
name|verify
argument_list|(
name|mockExecutor
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|scheduleAtFixedRate
argument_list|(
name|any
argument_list|(
name|Runnable
operator|.
name|class
argument_list|)
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|any
argument_list|(
name|TimeUnit
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testShutdown
parameter_list|()
block|{
comment|// When
name|collector
operator|.
name|shutdown
argument_list|()
expr_stmt|;
comment|// Then
name|verify
argument_list|(
name|mockExecutor
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testConsumeInitialInstances
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Given
name|LlapServiceInstance
name|mockService
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|LlapServiceInstanceSet
name|serviceInstances
init|=
name|mock
argument_list|(
name|LlapServiceInstanceSet
operator|.
name|class
argument_list|)
decl_stmt|;
name|LlapRegistryService
name|mockRegistryService
init|=
name|mock
argument_list|(
name|LlapRegistryService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|serviceInstances
operator|.
name|getAll
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|mockService
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockRegistryService
operator|.
name|getInstances
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serviceInstances
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|consumeInitialInstances
argument_list|(
name|mockRegistryService
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|collector
operator|.
name|getMetrics
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testStartWontStartSchedulingIfTheConfigValueIsZeroMs
parameter_list|()
block|{
comment|// Given
name|when
argument_list|(
name|mockConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"0ms"
argument_list|)
expr_stmt|;
name|collector
operator|=
operator|new
name|LlapMetricsCollector
argument_list|(
name|mockConf
argument_list|,
name|mockExecutor
argument_list|,
name|mockClientFactory
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Then
name|verify
argument_list|(
name|mockExecutor
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|scheduleAtFixedRate
argument_list|(
name|any
argument_list|(
name|Runnable
operator|.
name|class
argument_list|)
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|anyLong
argument_list|()
argument_list|,
name|any
argument_list|(
name|TimeUnit
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that the listener is created and called. The default config contains the mock listener.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testListener
parameter_list|()
block|{
comment|// Given
name|LlapServiceInstance
name|mockService1
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService1
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|mockService2
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService2
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_2
argument_list|)
expr_stmt|;
comment|// When
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService1
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService2
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertNotNull
argument_list|(
name|collector
operator|.
name|listener
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|collector
operator|.
name|listener
operator|)
operator|.
name|initCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|collector
operator|.
name|listener
operator|)
operator|.
name|fullMetricsCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|collector
operator|.
name|listener
operator|)
operator|.
name|daemonMetricsCount
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that the collector is working without the listener too.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|DEFAULT_TIMEOUT
argument_list|)
specifier|public
name|void
name|testWithoutListener
parameter_list|()
block|{
comment|// Given
name|when
argument_list|(
name|mockConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|collector
operator|=
operator|new
name|LlapMetricsCollector
argument_list|(
name|mockConf
argument_list|,
name|mockExecutor
argument_list|,
name|mockClientFactory
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|mockService1
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService1
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_1
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|mockService2
init|=
name|mock
argument_list|(
name|LlapServiceInstance
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockService2
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_IDENTITY_2
argument_list|)
expr_stmt|;
comment|// Check that there is no exception with start / create / remove / collect
name|collector
operator|.
name|start
argument_list|()
expr_stmt|;
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService1
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|onCreate
argument_list|(
name|mockService2
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|onRemove
argument_list|(
name|mockService2
argument_list|,
name|TEST_SEQ_VERSION
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collectMetrics
argument_list|()
expr_stmt|;
comment|// Then
name|assertNull
argument_list|(
name|collector
operator|.
name|listener
argument_list|)
expr_stmt|;
block|}
comment|/**    * Just count the calls.    */
specifier|static
class|class
name|MockListener
implements|implements
name|LlapMetricsListener
block|{
name|int
name|initCount
init|=
literal|0
decl_stmt|;
name|int
name|daemonMetricsCount
init|=
literal|0
decl_stmt|;
name|int
name|fullMetricsCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|LlapRegistryService
name|registry
parameter_list|)
block|{
name|initCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|newDaemonMetrics
parameter_list|(
name|String
name|workerIdentity
parameter_list|,
name|LlapMetricsCollector
operator|.
name|LlapMetrics
name|newMetrics
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Init should be called first"
argument_list|,
name|initCount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|daemonMetricsCount
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|newClusterMetrics
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LlapMetricsCollector
operator|.
name|LlapMetrics
argument_list|>
name|newMetrics
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Init should be called first"
argument_list|,
name|initCount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|fullMetricsCount
operator|++
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

