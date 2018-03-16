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
name|hive
operator|.
name|jdbc
package|;
end_package

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
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|impl
operator|.
name|ZkRegistryBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|server
operator|.
name|HS2ActivePassiveHARegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|server
operator|.
name|HS2ActivePassiveHARegistryClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|server
operator|.
name|HiveServer2Instance
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|servlet
operator|.
name|HS2Peers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
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
name|AfterClass
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
name|BeforeClass
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

begin_class
specifier|public
class|class
name|TestActivePassiveHA
block|{
specifier|private
name|MiniHS2
name|miniHS2_1
init|=
literal|null
decl_stmt|;
specifier|private
name|MiniHS2
name|miniHS2_2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|TestingServer
name|zkServer
decl_stmt|;
specifier|private
name|Connection
name|hs2Conn
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf1
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf2
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHS2
operator|.
name|cleanupLocalDir
argument_list|()
expr_stmt|;
name|zkServer
operator|=
operator|new
name|TestingServer
argument_list|()
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTest
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|zkServer
operator|!=
literal|null
condition|)
block|{
name|zkServer
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkServer
operator|=
literal|null
expr_stmt|;
block|}
name|MiniHS2
operator|.
name|cleanupLocalDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf1
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf1
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Set up zookeeper dynamic service discovery configs
name|setHAConfigs
argument_list|(
name|hiveConf1
argument_list|)
expr_stmt|;
name|miniHS2_1
operator|=
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf1
argument_list|)
operator|.
name|cleanupLocalDirOnStartup
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|hiveConf2
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf2
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Set up zookeeper dynamic service discovery configs
name|setHAConfigs
argument_list|(
name|hiveConf2
argument_list|)
expr_stmt|;
name|miniHS2_2
operator|=
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf2
argument_list|)
operator|.
name|cleanupLocalDirOnStartup
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hs2Conn
operator|!=
literal|null
condition|)
block|{
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|miniHS2_1
operator|!=
literal|null
operator|)
operator|&&
name|miniHS2_1
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2_1
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|miniHS2_2
operator|!=
literal|null
operator|)
operator|&&
name|miniHS2_2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2_2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|setHAConfigs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SUPPORT_DYNAMIC_SERVICE_DISCOVERY
operator|.
name|varname
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
operator|.
name|varname
argument_list|,
name|zkServer
operator|.
name|getConnectString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|zkRootNamespace
init|=
literal|"hs2test"
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ZOOKEEPER_NAMESPACE
operator|.
name|varname
argument_list|,
name|zkRootNamespace
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ACTIVE_PASSIVE_HA_ENABLE
operator|.
name|varname
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeDuration
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_TIMEOUT
operator|.
name|varname
argument_list|,
literal|2
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeDuration
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_BASESLEEPTIME
operator|.
name|varname
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_MAX_RETRIES
operator|.
name|varname
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testActivePassive
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|hiveConf1
operator|.
name|set
argument_list|(
name|ZkRegistryBase
operator|.
name|UNIQUE_IDENTIFIER
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|miniHS2_1
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|miniHS2_1
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|hiveConf2
operator|.
name|set
argument_list|(
name|ZkRegistryBase
operator|.
name|UNIQUE_IDENTIFIER
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|miniHS2_2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|miniHS2_2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|miniHS2_1
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|url
init|=
literal|"http://localhost:"
operator|+
name|hiveConf1
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/leader"
decl_stmt|;
name|assertEquals
argument_list|(
literal|"true"
argument_list|,
name|sendGet
argument_list|(
name|url
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|miniHS2_2
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf2
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/leader"
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|sendGet
argument_list|(
name|url
argument_list|)
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf1
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/peers"
expr_stmt|;
name|String
name|resp
init|=
name|sendGet
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|HS2Peers
operator|.
name|HS2Instances
name|hs2Peers
init|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|resp
argument_list|,
name|HS2Peers
operator|.
name|HS2Instances
operator|.
name|class
argument_list|)
decl_stmt|;
name|int
name|port1
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|hiveConf1
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
operator|.
name|varname
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveServer2Instance
name|hsi
range|:
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
control|)
block|{
if|if
condition|(
name|hsi
operator|.
name|getRpcPort
argument_list|()
operator|==
name|port1
condition|)
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|setHAConfigs
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HS2ActivePassiveHARegistry
name|client
init|=
name|HS2ActivePassiveHARegistryClient
operator|.
name|getClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveServer2Instance
argument_list|>
name|hs2Instances
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|client
operator|.
name|getAll
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|hs2Instances
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveServer2Instance
argument_list|>
name|leaders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HiveServer2Instance
argument_list|>
name|standby
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|HiveServer2Instance
name|instance
range|:
name|hs2Instances
control|)
block|{
if|if
condition|(
name|instance
operator|.
name|isLeader
argument_list|()
condition|)
block|{
name|leaders
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|standby
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|leaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|standby
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|miniHS2_1
operator|.
name|stop
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|miniHS2_2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|miniHS2_2
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf2
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/leader"
expr_stmt|;
name|assertEquals
argument_list|(
literal|"true"
argument_list|,
name|sendGet
argument_list|(
name|url
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
operator|.
name|getAll
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
name|HS2ActivePassiveHARegistryClient
operator|.
name|getClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|hs2Instances
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|client
operator|.
name|getAll
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|hs2Instances
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|leaders
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|standby
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|HiveServer2Instance
name|instance
range|:
name|hs2Instances
control|)
block|{
if|if
condition|(
name|instance
operator|.
name|isLeader
argument_list|()
condition|)
block|{
name|leaders
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|standby
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|leaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|standby
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf2
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/peers"
expr_stmt|;
name|resp
operator|=
name|sendGet
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|objectMapper
operator|=
operator|new
name|ObjectMapper
argument_list|()
expr_stmt|;
name|hs2Peers
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|resp
argument_list|,
name|HS2Peers
operator|.
name|HS2Instances
operator|.
name|class
argument_list|)
expr_stmt|;
name|int
name|port2
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|hiveConf2
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
operator|.
name|varname
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveServer2Instance
name|hsi
range|:
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
control|)
block|{
if|if
condition|(
name|hsi
operator|.
name|getRpcPort
argument_list|()
operator|==
name|port2
condition|)
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// start 1st server again
name|hiveConf1
operator|.
name|set
argument_list|(
name|ZkRegistryBase
operator|.
name|UNIQUE_IDENTIFIER
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|miniHS2_1
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|miniHS2_1
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|miniHS2_1
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf1
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/leader"
expr_stmt|;
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|sendGet
argument_list|(
name|url
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
operator|.
name|getAll
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
name|HS2ActivePassiveHARegistryClient
operator|.
name|getClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|hs2Instances
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|client
operator|.
name|getAll
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|hs2Instances
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|leaders
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|standby
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|HiveServer2Instance
name|instance
range|:
name|hs2Instances
control|)
block|{
if|if
condition|(
name|instance
operator|.
name|isLeader
argument_list|()
condition|)
block|{
name|leaders
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|standby
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|leaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|standby
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"http://localhost:"
operator|+
name|hiveConf1
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|)
operator|+
literal|"/peers"
expr_stmt|;
name|resp
operator|=
name|sendGet
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|objectMapper
operator|=
operator|new
name|ObjectMapper
argument_list|()
expr_stmt|;
name|hs2Peers
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|resp
argument_list|,
name|HS2Peers
operator|.
name|HS2Instances
operator|.
name|class
argument_list|)
expr_stmt|;
name|port2
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|hiveConf2
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveServer2Instance
name|hsi
range|:
name|hs2Peers
operator|.
name|getHiveServer2Instances
argument_list|()
control|)
block|{
if|if
condition|(
name|hsi
operator|.
name|getRpcPort
argument_list|()
operator|==
name|port2
condition|)
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|hsi
operator|.
name|isLeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|String
name|sendGet
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|Exception
block|{
name|URL
name|obj
init|=
operator|new
name|URL
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|con
init|=
operator|(
name|HttpURLConnection
operator|)
name|obj
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|con
operator|.
name|setRequestMethod
argument_list|(
literal|"GET"
argument_list|)
expr_stmt|;
name|BufferedReader
name|in
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|con
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|inputLine
decl_stmt|;
name|StringBuilder
name|response
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|inputLine
operator|=
name|in
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|response
operator|.
name|append
argument_list|(
name|inputLine
argument_list|)
expr_stmt|;
block|}
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|response
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

