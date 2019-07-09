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
name|registry
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
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|RetryOneTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|test
operator|.
name|TestingServer
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
name|registry
operator|.
name|ServiceInstanceSet
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
name|Collection
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

begin_class
specifier|public
class|class
name|TestLlapZookeeperRegistryImpl
block|{
specifier|private
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
name|LlapZookeeperRegistryImpl
name|registry
decl_stmt|;
specifier|private
name|CuratorFramework
name|curatorFramework
decl_stmt|;
specifier|private
name|TestingServer
name|server
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|registry
operator|=
operator|new
name|LlapZookeeperRegistryImpl
argument_list|(
literal|"TestLlapZookeeperRegistryImpl"
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|server
operator|=
operator|new
name|TestingServer
argument_list|()
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|curatorFramework
operator|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
name|server
operator|.
name|getConnectString
argument_list|()
argument_list|)
operator|.
name|sessionTimeoutMs
argument_list|(
literal|1000
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|RetryOneTime
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|curatorFramework
operator|.
name|start
argument_list|()
expr_stmt|;
name|trySetMock
argument_list|(
name|registry
argument_list|,
name|CuratorFramework
operator|.
name|class
argument_list|,
name|curatorFramework
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|curatorFramework
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegister
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Given
name|int
name|expectedExecutorCount
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|)
decl_stmt|;
name|int
name|expectedQueueSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
argument_list|)
decl_stmt|;
comment|// When
name|registry
operator|.
name|register
argument_list|()
expr_stmt|;
name|ServiceInstanceSet
argument_list|<
name|LlapServiceInstance
argument_list|>
name|serviceInstanceSet
init|=
name|registry
operator|.
name|getInstances
argument_list|(
literal|"LLAP"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
comment|// Then
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|llaps
init|=
name|serviceInstanceSet
operator|.
name|getAll
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|llaps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|serviceInstance
init|=
name|llaps
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
init|=
name|serviceInstance
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedQueueSize
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
name|expectedExecutorCount
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
annotation|@
name|Test
specifier|public
name|void
name|testUpdate
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Given
name|String
name|expectedExecutorCount
init|=
literal|"2"
decl_stmt|;
name|String
name|expectedQueueSize
init|=
literal|"20"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|capacityValues
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|capacityValues
operator|.
name|put
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
argument_list|,
name|expectedExecutorCount
argument_list|)
expr_stmt|;
name|capacityValues
operator|.
name|put
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
argument_list|,
name|expectedQueueSize
argument_list|)
expr_stmt|;
comment|// When
name|registry
operator|.
name|register
argument_list|()
expr_stmt|;
name|registry
operator|.
name|updateRegistration
argument_list|(
name|capacityValues
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
name|ServiceInstanceSet
argument_list|<
name|LlapServiceInstance
argument_list|>
name|serviceInstanceSet
init|=
name|registry
operator|.
name|getInstances
argument_list|(
literal|"LLAP"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
comment|// Then
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|llaps
init|=
name|serviceInstanceSet
operator|.
name|getAll
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|llaps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LlapServiceInstance
name|serviceInstance
init|=
name|llaps
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
init|=
name|serviceInstance
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedQueueSize
argument_list|,
name|attributes
operator|.
name|get
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedExecutorCount
argument_list|,
name|attributes
operator|.
name|get
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
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

