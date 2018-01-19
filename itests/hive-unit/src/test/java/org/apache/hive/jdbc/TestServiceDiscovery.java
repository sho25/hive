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
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|Predicate
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
name|Collections2
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
name|framework
operator|.
name|recipes
operator|.
name|nodes
operator|.
name|PersistentEphemeralNode
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
name|ql
operator|.
name|util
operator|.
name|ZooKeeperHiveHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|Assert
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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
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
comment|/**  * Test Hive dynamic service discovery.  */
end_comment

begin_class
specifier|public
class|class
name|TestServiceDiscovery
block|{
specifier|private
specifier|static
name|TestingServer
name|server
decl_stmt|;
specifier|private
specifier|static
name|CuratorFramework
name|client
decl_stmt|;
specifier|private
specifier|static
name|String
name|rootNamespace
init|=
literal|"hiveserver2"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|server
operator|=
operator|new
name|TestingServer
argument_list|()
expr_stmt|;
name|CuratorFrameworkFactory
operator|.
name|Builder
name|builder
init|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
decl_stmt|;
name|client
operator|=
name|builder
operator|.
name|connectString
argument_list|(
name|server
operator|.
name|getConnectString
argument_list|()
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|RetryOneTime
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnect
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
name|confs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.bind.host"
argument_list|,
literal|"host-1"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.transport.mode"
argument_list|,
literal|"binary"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.port"
argument_list|,
literal|"8000"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.authentication"
argument_list|,
literal|"PLAIN"
argument_list|)
expr_stmt|;
name|publishConfsToZk
argument_list|(
name|confs
argument_list|,
literal|"uri1"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.bind.host"
argument_list|,
literal|"host-2"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.transport.mode"
argument_list|,
literal|"binary"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.port"
argument_list|,
literal|"9000"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.authentication"
argument_list|,
literal|"PLAIN"
argument_list|)
expr_stmt|;
name|publishConfsToZk
argument_list|(
name|confs
argument_list|,
literal|"uri2"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.bind.host"
argument_list|,
literal|"host-3"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.transport.mode"
argument_list|,
literal|"binary"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.thrift.port"
argument_list|,
literal|"10000"
argument_list|)
expr_stmt|;
name|confs
operator|.
name|put
argument_list|(
literal|"hive.server2.authentication"
argument_list|,
literal|"PLAIN"
argument_list|)
expr_stmt|;
name|publishConfsToZk
argument_list|(
name|confs
argument_list|,
literal|"uri3"
argument_list|)
expr_stmt|;
name|Utils
operator|.
name|JdbcConnectionParams
name|connParams
init|=
operator|new
name|Utils
operator|.
name|JdbcConnectionParams
argument_list|()
decl_stmt|;
name|connParams
operator|.
name|setZooKeeperEnsemble
argument_list|(
name|server
operator|.
name|getConnectString
argument_list|()
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|Utils
operator|.
name|JdbcConnectionParams
operator|.
name|ZOOKEEPER_NAMESPACE
argument_list|,
literal|"hiveserver2"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ConnParamInfo
argument_list|>
name|allConnectParams
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|//Reject all paths to force it to continue.  When no more paths, should throw an exception.
try|try
block|{
name|ZooKeeperHiveClientHelper
operator|.
name|configureConnParams
argument_list|(
name|connParams
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperHiveClientException
name|e
parameter_list|)
block|{
break|break;
block|}
name|connParams
operator|.
name|getRejectedHostZnodePaths
argument_list|()
operator|.
name|add
argument_list|(
name|connParams
operator|.
name|getCurrentHostZnodePath
argument_list|()
argument_list|)
expr_stmt|;
name|allConnectParams
operator|.
name|add
argument_list|(
operator|new
name|ConnParamInfo
argument_list|(
name|connParams
operator|.
name|getHost
argument_list|()
argument_list|,
name|connParams
operator|.
name|getPort
argument_list|()
argument_list|,
name|connParams
operator|.
name|getCurrentHostZnodePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//Make sure it itereated through all possible ConnParams
name|Collection
argument_list|<
name|ConnParamInfo
argument_list|>
name|cp1
init|=
name|Collections2
operator|.
name|filter
argument_list|(
name|allConnectParams
argument_list|,
operator|new
name|ConnParamInfoPred
argument_list|(
literal|"host-1"
argument_list|,
literal|8000
argument_list|,
literal|"serverUri=uri1"
argument_list|)
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|ConnParamInfo
argument_list|>
name|cp2
init|=
name|Collections2
operator|.
name|filter
argument_list|(
name|allConnectParams
argument_list|,
operator|new
name|ConnParamInfoPred
argument_list|(
literal|"host-2"
argument_list|,
literal|9000
argument_list|,
literal|"serverUri=uri2"
argument_list|)
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|ConnParamInfo
argument_list|>
name|cp3
init|=
name|Collections2
operator|.
name|filter
argument_list|(
name|allConnectParams
argument_list|,
operator|new
name|ConnParamInfoPred
argument_list|(
literal|"host-3"
argument_list|,
literal|10000
argument_list|,
literal|"serverUri=uri3"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|cp1
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|cp2
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|cp3
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|//Helper classes for ConnParam comparison logics.
specifier|private
class|class
name|ConnParamInfo
block|{
name|String
name|host
decl_stmt|;
name|int
name|port
decl_stmt|;
name|String
name|path
decl_stmt|;
specifier|public
name|ConnParamInfo
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|ConnParamInfoPred
implements|implements
name|Predicate
argument_list|<
name|ConnParamInfo
argument_list|>
block|{
name|String
name|host
decl_stmt|;
name|int
name|port
decl_stmt|;
name|String
name|pathPrefix
decl_stmt|;
name|ConnParamInfoPred
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|pathPrefix
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|pathPrefix
operator|=
name|pathPrefix
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|ConnParamInfo
name|inputParam
parameter_list|)
block|{
return|return
name|inputParam
operator|.
name|host
operator|.
name|equals
argument_list|(
name|host
argument_list|)
operator|&&
name|inputParam
operator|.
name|port
operator|==
name|port
operator|&&
name|inputParam
operator|.
name|path
operator|.
name|startsWith
argument_list|(
name|pathPrefix
argument_list|)
return|;
block|}
block|}
comment|//Mocks HS2 publishing logic.
specifier|private
name|void
name|publishConfsToZk
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confs
parameter_list|,
name|String
name|uri
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|create
argument_list|()
operator|.
name|creatingParentsIfNeeded
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
operator|.
name|forPath
argument_list|(
name|ZooKeeperHiveHelper
operator|.
name|ZOOKEEPER_PATH_SEPARATOR
operator|+
name|rootNamespace
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|code
argument_list|()
operator|==
name|KeeperException
operator|.
name|Code
operator|.
name|NODEEXISTS
argument_list|)
expr_stmt|;
block|}
name|String
name|pathPrefix
init|=
name|ZooKeeperHiveHelper
operator|.
name|ZOOKEEPER_PATH_SEPARATOR
operator|+
name|rootNamespace
operator|+
name|ZooKeeperHiveHelper
operator|.
name|ZOOKEEPER_PATH_SEPARATOR
operator|+
literal|"serverUri="
operator|+
name|uri
operator|+
literal|";"
operator|+
literal|"sequence="
decl_stmt|;
name|String
name|znodeData
init|=
literal|""
decl_stmt|;
comment|// Publish configs for this instance as the data on the node
name|znodeData
operator|=
name|Joiner
operator|.
name|on
argument_list|(
literal|';'
argument_list|)
operator|.
name|withKeyValueSeparator
argument_list|(
literal|"="
argument_list|)
operator|.
name|join
argument_list|(
name|confs
argument_list|)
expr_stmt|;
name|byte
index|[]
name|znodeDataUTF8
init|=
name|znodeData
operator|.
name|getBytes
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|PersistentEphemeralNode
name|znode
init|=
operator|new
name|PersistentEphemeralNode
argument_list|(
name|client
argument_list|,
name|PersistentEphemeralNode
operator|.
name|Mode
operator|.
name|EPHEMERAL_SEQUENTIAL
argument_list|,
name|pathPrefix
argument_list|,
name|znodeDataUTF8
argument_list|)
decl_stmt|;
name|znode
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// We'll wait for 120s for node creation
name|long
name|znodeCreationTimeout
init|=
literal|120
decl_stmt|;
if|if
condition|(
operator|!
name|znode
operator|.
name|waitForInitialCreate
argument_list|(
name|znodeCreationTimeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Max znode creation wait time: "
operator|+
name|znodeCreationTimeout
operator|+
literal|"s exhausted"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

