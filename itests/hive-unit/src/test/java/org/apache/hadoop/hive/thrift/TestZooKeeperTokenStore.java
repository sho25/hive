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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|retry
operator|.
name|ExponentialBackoffRetry
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
name|hbase
operator|.
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|thrift
operator|.
name|HadoopThriftAuthBridge
operator|.
name|Server
operator|.
name|ServerMode
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
name|io
operator|.
name|Text
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|AbstractDelegationTokenSecretManager
operator|.
name|DelegationTokenInformation
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|HiveDelegationTokenSupport
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
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|ACL
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

begin_class
specifier|public
class|class
name|TestZooKeeperTokenStore
extends|extends
name|TestCase
block|{
specifier|private
name|MiniZooKeeperCluster
name|zkCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|CuratorFramework
name|zkClient
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|zkPort
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|ZooKeeperTokenStore
name|ts
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|zkDataDir
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|zkCluster
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cluster already running"
argument_list|)
throw|;
block|}
name|this
operator|.
name|zkCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|()
expr_stmt|;
name|this
operator|.
name|zkPort
operator|=
name|this
operator|.
name|zkCluster
operator|.
name|startup
argument_list|(
name|zkDataDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|zkClient
operator|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
literal|"localhost:"
operator|+
name|zkPort
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|ExponentialBackoffRetry
argument_list|(
literal|1000
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|zkClient
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|zkClient
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|ts
operator|!=
literal|null
condition|)
block|{
name|ts
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|zkCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|this
operator|.
name|zkCluster
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|Configuration
name|createConf
parameter_list|(
name|String
name|zkPath
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_STR
argument_list|,
literal|"localhost:"
operator|+
name|this
operator|.
name|zkPort
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ZNODE
argument_list|,
name|zkPath
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|testTokenStorage
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|ZK_PATH
init|=
literal|"/zktokenstore-testTokenStorage"
decl_stmt|;
name|ts
operator|=
operator|new
name|ZooKeeperTokenStore
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
name|ZK_PATH
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|,
literal|"world:anyone:cdrwa"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ts
operator|.
name|init
argument_list|(
literal|null
argument_list|,
name|ServerMode
operator|.
name|METASTORE
argument_list|)
expr_stmt|;
name|String
name|metastore_zk_path
init|=
name|ZK_PATH
operator|+
name|ServerMode
operator|.
name|METASTORE
decl_stmt|;
name|int
name|keySeq
init|=
name|ts
operator|.
name|addMasterKey
argument_list|(
literal|"key1Data"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|zkClient
operator|.
name|getData
argument_list|()
operator|.
name|forPath
argument_list|(
name|metastore_zk_path
operator|+
literal|"/keys/"
operator|+
name|String
operator|.
name|format
argument_list|(
name|ZooKeeperTokenStore
operator|.
name|ZK_SEQ_FORMAT
argument_list|,
name|keySeq
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|keyBytes
argument_list|)
argument_list|,
literal|"key1Data"
argument_list|)
expr_stmt|;
name|int
name|keySeq2
init|=
name|ts
operator|.
name|addMasterKey
argument_list|(
literal|"key2Data"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"keys sequential"
argument_list|,
name|keySeq
operator|+
literal|1
argument_list|,
name|keySeq2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"expected number keys"
argument_list|,
literal|2
argument_list|,
name|ts
operator|.
name|getMasterKeys
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ts
operator|.
name|removeMasterKey
argument_list|(
name|keySeq
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"expected number keys"
argument_list|,
literal|1
argument_list|,
name|ts
operator|.
name|getMasterKeys
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// tokens
name|DelegationTokenIdentifier
name|tokenId
init|=
operator|new
name|DelegationTokenIdentifier
argument_list|(
operator|new
name|Text
argument_list|(
literal|"owner"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"renewer"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"realUser"
argument_list|)
argument_list|)
decl_stmt|;
name|DelegationTokenInformation
name|tokenInfo
init|=
operator|new
name|DelegationTokenInformation
argument_list|(
literal|99
argument_list|,
literal|"password"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|ts
operator|.
name|addToken
argument_list|(
name|tokenId
argument_list|,
name|tokenInfo
argument_list|)
expr_stmt|;
name|DelegationTokenInformation
name|tokenInfoRead
init|=
name|ts
operator|.
name|getToken
argument_list|(
name|tokenId
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tokenInfo
operator|.
name|getRenewDate
argument_list|()
argument_list|,
name|tokenInfoRead
operator|.
name|getRenewDate
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|tokenInfo
argument_list|,
name|tokenInfoRead
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|HiveDelegationTokenSupport
operator|.
name|encodeDelegationTokenInformation
argument_list|(
name|tokenInfo
argument_list|)
argument_list|,
name|HiveDelegationTokenSupport
operator|.
name|encodeDelegationTokenInformation
argument_list|(
name|tokenInfoRead
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|allIds
init|=
name|ts
operator|.
name|getAllDelegationTokenIdentifiers
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|allIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TokenStoreDelegationTokenSecretManager
operator|.
name|encodeWritable
argument_list|(
name|tokenId
argument_list|)
argument_list|,
name|TokenStoreDelegationTokenSecretManager
operator|.
name|encodeWritable
argument_list|(
name|allIds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ts
operator|.
name|removeToken
argument_list|(
name|tokenId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ts
operator|.
name|getAllDelegationTokenIdentifiers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testAclNoAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|ZK_PATH
init|=
literal|"/zktokenstore-testAclNoAuth"
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
name|ZK_PATH
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|,
literal|"ip:127.0.0.1:r"
argument_list|)
expr_stmt|;
name|ts
operator|=
operator|new
name|ZooKeeperTokenStore
argument_list|()
expr_stmt|;
try|try
block|{
name|ts
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ts
operator|.
name|init
argument_list|(
literal|null
argument_list|,
name|ServerMode
operator|.
name|METASTORE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected ACL exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DelegationTokenStore
operator|.
name|TokenStoreException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|KeeperException
operator|.
name|NoAuthException
operator|.
name|class
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testAclInvalid
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|ZK_PATH
init|=
literal|"/zktokenstore-testAclInvalid"
decl_stmt|;
name|String
name|aclString
init|=
literal|"sasl:hive/host@TEST.DOMAIN:cdrwa, fail-parse-ignored"
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
name|ZK_PATH
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|,
name|aclString
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ACL
argument_list|>
name|aclList
init|=
name|ZooKeeperTokenStore
operator|.
name|parseACLs
argument_list|(
name|aclString
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aclList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|ts
operator|=
operator|new
name|ZooKeeperTokenStore
argument_list|()
expr_stmt|;
try|try
block|{
name|ts
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ts
operator|.
name|init
argument_list|(
literal|null
argument_list|,
name|ServerMode
operator|.
name|METASTORE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected ACL exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DelegationTokenStore
operator|.
name|TokenStoreException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|KeeperException
operator|.
name|InvalidACLException
operator|.
name|class
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testAclPositive
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|ZK_PATH
init|=
literal|"/zktokenstore-testAcl"
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
name|ZK_PATH
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|,
literal|"ip:127.0.0.1:cdrwa,world:anyone:cdrwa"
argument_list|)
expr_stmt|;
name|ts
operator|=
operator|new
name|ZooKeeperTokenStore
argument_list|()
expr_stmt|;
name|ts
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ts
operator|.
name|init
argument_list|(
literal|null
argument_list|,
name|ServerMode
operator|.
name|METASTORE
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
init|=
name|zkClient
operator|.
name|getACL
argument_list|()
operator|.
name|forPath
argument_list|(
name|ZK_PATH
operator|+
name|ServerMode
operator|.
name|METASTORE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|acl
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

