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
name|daemon
operator|.
name|impl
package|;
end_package

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
name|LlapDaemonInfo
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
name|metrics2
operator|.
name|MetricsSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Captor
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
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|util
operator|.
name|reflection
operator|.
name|Fields
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|util
operator|.
name|reflection
operator|.
name|InstanceField
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Integer
operator|.
name|parseInt
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
name|assertTrue
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
name|MockitoAnnotations
operator|.
name|initMocks
import|;
end_import

begin_class
specifier|public
class|class
name|TestLlapDaemon
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|METRICS_SOURCES
init|=
operator|new
name|String
index|[]
block|{
literal|"JvmMetrics"
block|,
literal|"LlapDaemonExecutorMetrics-"
operator|+
name|MetricsUtils
operator|.
name|getHostName
argument_list|()
block|,
literal|"LlapDaemonJvmMetrics-"
operator|+
name|MetricsUtils
operator|.
name|getHostName
argument_list|()
block|,
name|MetricsUtils
operator|.
name|METRICS_PROCESS_NAME
block|}
decl_stmt|;
specifier|private
name|Configuration
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|LlapRegistryService
name|mockRegistry
decl_stmt|;
annotation|@
name|Captor
specifier|private
name|ArgumentCaptor
argument_list|<
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
name|captor
decl_stmt|;
specifier|private
name|LlapDaemon
name|daemon
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|initMocks
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|,
literal|"@llap"
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|String
index|[]
name|localDirs
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|LlapDaemonInfo
operator|.
name|initialize
argument_list|(
literal|"testDaemon"
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|daemon
operator|=
operator|new
name|LlapDaemon
argument_list|(
name|hiveConf
argument_list|,
literal|1
argument_list|,
name|LlapDaemon
operator|.
name|getTotalHeapSize
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|,
name|localDirs
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|"TestLlapDaemon"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|MetricsSystem
name|ms
init|=
name|LlapMetricsSystem
operator|.
name|instance
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|mSource
range|:
name|METRICS_SOURCES
control|)
block|{
name|ms
operator|.
name|unregisterSource
argument_list|(
name|mSource
argument_list|)
expr_stmt|;
block|}
name|daemon
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateRegistration
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Given
name|int
name|enabledExecutors
init|=
literal|0
decl_stmt|;
name|int
name|enabledQueue
init|=
literal|2
decl_stmt|;
name|trySetMock
argument_list|(
name|daemon
argument_list|,
name|LlapRegistryService
operator|.
name|class
argument_list|,
name|mockRegistry
argument_list|)
expr_stmt|;
comment|// When
name|daemon
operator|.
name|setCapacity
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SetCapacityRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setQueueSize
argument_list|(
name|enabledQueue
argument_list|)
operator|.
name|setExecutorNum
argument_list|(
name|enabledExecutors
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockRegistry
argument_list|)
operator|.
name|updateRegistration
argument_list|(
name|captor
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
comment|// Then
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
init|=
name|StreamSupport
operator|.
name|stream
argument_list|(
name|captor
operator|.
name|getValue
argument_list|()
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
lambda|->
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|attributes
operator|.
name|containsKey
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|attributes
operator|.
name|containsKey
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|enabledQueue
argument_list|,
name|parseInt
argument_list|(
name|attributes
operator|.
name|get
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|enabledExecutors
argument_list|,
name|parseInt
argument_list|(
name|attributes
operator|.
name|get
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
parameter_list|<
name|T
parameter_list|>
name|void
name|trySetMock
parameter_list|(
name|Object
name|o
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|T
name|mock
parameter_list|)
block|{
name|List
argument_list|<
name|InstanceField
argument_list|>
name|instanceFields
init|=
name|Fields
operator|.
name|allDeclaredFieldsOf
argument_list|(
name|o
argument_list|)
operator|.
name|filter
argument_list|(
name|instanceField
lambda|->
operator|!
name|clazz
operator|.
name|isAssignableFrom
argument_list|(
name|instanceField
operator|.
name|jdkField
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
operator|.
name|instanceFields
argument_list|()
decl_stmt|;
if|if
condition|(
name|instanceFields
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Mocking is only supported, if only one field is assignable from the given class."
argument_list|)
throw|;
block|}
name|InstanceField
name|instanceField
init|=
name|instanceFields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|instanceField
operator|.
name|set
argument_list|(
name|mock
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

