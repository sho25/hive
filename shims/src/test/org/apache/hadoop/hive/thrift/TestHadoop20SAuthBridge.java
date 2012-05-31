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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|NetworkInterface
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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
name|metastore
operator|.
name|HiveMetaStore
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|SaslRpcServer
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
name|SaslRpcServer
operator|.
name|AuthMethod
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
name|UserGroupInformation
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
name|UserGroupInformation
operator|.
name|AuthenticationMethod
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
name|authorize
operator|.
name|AuthorizationException
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
name|authorize
operator|.
name|ProxyUsers
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
name|SecretManager
operator|.
name|InvalidToken
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
name|Token
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
name|DelegationKey
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSaslServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestHadoop20SAuthBridge
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
class|class
name|MyHadoopThriftAuthBridge20S
extends|extends
name|HadoopThriftAuthBridge20S
block|{
annotation|@
name|Override
specifier|public
name|Server
name|createServer
parameter_list|(
name|String
name|keytabFile
parameter_list|,
name|String
name|principalConf
parameter_list|)
throws|throws
name|TTransportException
block|{
comment|//Create a Server that doesn't interpret any Kerberos stuff
return|return
operator|new
name|Server
argument_list|()
return|;
block|}
specifier|static
class|class
name|Server
extends|extends
name|HadoopThriftAuthBridge20S
operator|.
name|Server
block|{
specifier|public
name|Server
parameter_list|()
throws|throws
name|TTransportException
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TTransportFactory
name|createTransportFactory
parameter_list|()
throws|throws
name|TTransportException
block|{
name|TSaslServerTransport
operator|.
name|Factory
name|transFactory
init|=
operator|new
name|TSaslServerTransport
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|transFactory
operator|.
name|addServerDefinition
argument_list|(
name|AuthMethod
operator|.
name|DIGEST
operator|.
name|getMechanismName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|SaslRpcServer
operator|.
name|SASL_DEFAULT_REALM
argument_list|,
name|SaslRpcServer
operator|.
name|SASL_PROPS
argument_list|,
operator|new
name|SaslDigestCallbackHandler
argument_list|(
name|secretManager
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|TUGIAssumingTransportFactory
argument_list|(
name|transFactory
argument_list|,
name|realUgi
argument_list|)
return|;
block|}
specifier|static
name|DelegationTokenStore
name|TOKEN_STORE
init|=
operator|new
name|MemoryTokenStore
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|DelegationTokenStore
name|getTokenStore
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TOKEN_STORE
return|;
block|}
block|}
block|}
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|void
name|configureSuperUserIPAddresses
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|superUserShortName
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|ipList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Enumeration
argument_list|<
name|NetworkInterface
argument_list|>
name|netInterfaceList
init|=
name|NetworkInterface
operator|.
name|getNetworkInterfaces
argument_list|()
decl_stmt|;
while|while
condition|(
name|netInterfaceList
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|NetworkInterface
name|inf
init|=
name|netInterfaceList
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|Enumeration
argument_list|<
name|InetAddress
argument_list|>
name|addrList
init|=
name|inf
operator|.
name|getInetAddresses
argument_list|()
decl_stmt|;
while|while
condition|(
name|addrList
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|InetAddress
name|addr
init|=
name|addrList
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|ipList
operator|.
name|add
argument_list|(
name|addr
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|ip
range|:
name|ipList
control|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|ip
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"127.0.1.1,"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getCanonicalHostName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|ProxyUsers
operator|.
name|getProxySuperuserIpConfKey
argument_list|(
name|superUserShortName
argument_list|)
argument_list|,
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|port
init|=
name|findFreePort
argument_list|()
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_USE_THRIFT_SASL
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
operator|.
name|varname
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.data"
argument_list|,
literal|"/tmp"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestHadoop20SAuthBridge
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_MODE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
operator|new
name|MyHadoopThriftAuthBridge20S
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test delegation token store/load from shared store.    * @throws Exception    */
specifier|public
name|void
name|testDelegationTokenSharedStore
parameter_list|()
throws|throws
name|Exception
block|{
name|UserGroupInformation
name|clientUgi
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|TokenStoreDelegationTokenSecretManager
name|tokenManager
init|=
operator|new
name|TokenStoreDelegationTokenSecretManager
argument_list|(
literal|0
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|0
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
argument_list|)
decl_stmt|;
comment|// initializes current key
name|tokenManager
operator|.
name|startThreads
argument_list|()
expr_stmt|;
name|tokenManager
operator|.
name|stopThreads
argument_list|()
expr_stmt|;
name|String
name|tokenStrForm
init|=
name|tokenManager
operator|.
name|getDelegationToken
argument_list|(
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
decl_stmt|;
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|t
init|=
operator|new
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
argument_list|()
decl_stmt|;
name|t
operator|.
name|decodeFromUrlString
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
comment|//check whether the username in the token is what we expect
name|DelegationTokenIdentifier
name|d
init|=
operator|new
name|DelegationTokenIdentifier
argument_list|()
decl_stmt|;
name|d
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|t
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Usernames don't match"
argument_list|,
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
operator|.
name|equals
argument_list|(
name|d
operator|.
name|getUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|DelegationTokenInformation
name|tokenInfo
init|=
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|getToken
argument_list|(
name|d
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"token not in store"
argument_list|,
name|tokenInfo
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"duplicate token add"
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|addToken
argument_list|(
name|d
argument_list|,
name|tokenInfo
argument_list|)
argument_list|)
expr_stmt|;
comment|// check keys are copied from token store when token is loaded
name|TokenStoreDelegationTokenSecretManager
name|anotherManager
init|=
operator|new
name|TokenStoreDelegationTokenSecretManager
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"master keys empty on init"
argument_list|,
literal|0
argument_list|,
name|anotherManager
operator|.
name|getAllKeys
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"token loaded"
argument_list|,
name|anotherManager
operator|.
name|retrievePassword
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
name|anotherManager
operator|.
name|renewToken
argument_list|(
name|t
argument_list|,
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"master keys not loaded from store"
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|getMasterKeys
argument_list|()
operator|.
name|length
argument_list|,
name|anotherManager
operator|.
name|getAllKeys
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// cancel the delegation token
name|tokenManager
operator|.
name|cancelDelegationToken
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"token not removed from store after cancel"
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|getToken
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"token removed (again)"
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|removeToken
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|anotherManager
operator|.
name|retrievePassword
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"InvalidToken expected after cancel"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidToken
name|ex
parameter_list|)
block|{
comment|// expected
block|}
comment|// token expiration
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|addToken
argument_list|(
name|d
argument_list|,
operator|new
name|DelegationTokenInformation
argument_list|(
literal|0
argument_list|,
name|t
operator|.
name|getPassword
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|getToken
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
name|anotherManager
operator|.
name|removeExpiredTokens
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
literal|"Expired token not removed"
argument_list|,
name|MyHadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|TOKEN_STORE
operator|.
name|getToken
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
comment|// key expiration - create an already expired key
name|anotherManager
operator|.
name|startThreads
argument_list|()
expr_stmt|;
comment|// generates initial key
name|anotherManager
operator|.
name|stopThreads
argument_list|()
expr_stmt|;
name|DelegationKey
name|expiredKey
init|=
operator|new
name|DelegationKey
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
name|anotherManager
operator|.
name|getAllKeys
argument_list|()
index|[
literal|0
index|]
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|anotherManager
operator|.
name|logUpdateMasterKey
argument_list|(
name|expiredKey
argument_list|)
expr_stmt|;
comment|// updates key with sequence number
name|assertTrue
argument_list|(
literal|"expired key not in allKeys"
argument_list|,
name|anotherManager
operator|.
name|reloadKeys
argument_list|()
operator|.
name|containsKey
argument_list|(
name|expiredKey
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|anotherManager
operator|.
name|rollMasterKeyExt
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Expired key not removed"
argument_list|,
name|anotherManager
operator|.
name|reloadKeys
argument_list|()
operator|.
name|containsKey
argument_list|(
name|expiredKey
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSaslWithHiveMetaStore
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|()
expr_stmt|;
name|UserGroupInformation
name|clientUgi
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|obtainTokenAndAddIntoUGI
argument_list|(
name|clientUgi
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|obtainTokenAndAddIntoUGI
argument_list|(
name|clientUgi
argument_list|,
literal|"tokenForFooTablePartition"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testMetastoreProxyUser
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|()
expr_stmt|;
specifier|final
name|String
name|proxyUserName
init|=
literal|"proxyUser"
decl_stmt|;
comment|//set the configuration up such that proxyUser can act on
comment|//behalf of all users belonging to the group foo_bar_group (
comment|//a dummy group)
name|String
index|[]
name|groupNames
init|=
operator|new
name|String
index|[]
block|{
literal|"foo_bar_group"
block|}
decl_stmt|;
name|setGroupsInConf
argument_list|(
name|groupNames
argument_list|,
name|proxyUserName
argument_list|)
expr_stmt|;
specifier|final
name|UserGroupInformation
name|delegationTokenUser
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
specifier|final
name|UserGroupInformation
name|proxyUserUgi
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|proxyUserName
argument_list|)
decl_stmt|;
name|String
name|tokenStrForm
init|=
name|proxyUserUgi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|//Since the user running the test won't belong to a non-existent group
comment|//foo_bar_group, the call to getDelegationTokenStr will fail
return|return
name|getDelegationTokenStr
argument_list|(
name|delegationTokenUser
argument_list|,
name|proxyUserUgi
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|ae
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected the getDelegationToken call to fail"
argument_list|,
name|tokenStrForm
operator|==
literal|null
argument_list|)
expr_stmt|;
comment|//set the configuration up such that proxyUser can act on
comment|//behalf of all users belonging to the real group(s) that the
comment|//user running the test belongs to
name|setGroupsInConf
argument_list|(
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|,
name|proxyUserName
argument_list|)
expr_stmt|;
name|tokenStrForm
operator|=
name|proxyUserUgi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|//Since the user running the test belongs to the group
comment|//obtained above the call to getDelegationTokenStr will succeed
return|return
name|getDelegationTokenStr
argument_list|(
name|delegationTokenUser
argument_list|,
name|proxyUserUgi
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|ae
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected the getDelegationToken call to not fail"
argument_list|,
name|tokenStrForm
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|t
init|=
operator|new
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
argument_list|()
decl_stmt|;
name|t
operator|.
name|decodeFromUrlString
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
comment|//check whether the username in the token is what we expect
name|DelegationTokenIdentifier
name|d
init|=
operator|new
name|DelegationTokenIdentifier
argument_list|()
decl_stmt|;
name|d
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|t
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Usernames don't match"
argument_list|,
name|delegationTokenUser
operator|.
name|getShortUserName
argument_list|()
operator|.
name|equals
argument_list|(
name|d
operator|.
name|getUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setGroupsInConf
parameter_list|(
name|String
index|[]
name|groupNames
parameter_list|,
name|String
name|proxyUserName
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|set
argument_list|(
name|ProxyUsers
operator|.
name|getProxySuperuserGroupConfKey
argument_list|(
name|proxyUserName
argument_list|)
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|groupNames
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|configureSuperUserIPAddresses
argument_list|(
name|conf
argument_list|,
name|proxyUserName
argument_list|)
expr_stmt|;
name|ProxyUsers
operator|.
name|refreshSuperUserGroupsConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getDelegationTokenStr
parameter_list|(
name|UserGroupInformation
name|ownerUgi
parameter_list|,
name|UserGroupInformation
name|realUgi
parameter_list|)
throws|throws
name|Exception
block|{
comment|//obtain a token by directly invoking the metastore operation(without going
comment|//through the thrift interface). Obtaining a token makes the secret manager
comment|//aware of the user and that it gave the token to the user
comment|//also set the authentication method explicitly to KERBEROS. Since the
comment|//metastore checks whether the authentication method is KERBEROS or not
comment|//for getDelegationToken, and the testcases don't use
comment|//kerberos, this needs to be done
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|authenticationMethod
operator|.
name|set
argument_list|(
name|AuthenticationMethod
operator|.
name|KERBEROS
argument_list|)
expr_stmt|;
name|HadoopThriftAuthBridge20S
operator|.
name|Server
operator|.
name|remoteAddress
operator|.
name|set
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|HiveMetaStore
operator|.
name|getDelegationToken
argument_list|(
name|ownerUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|realUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|obtainTokenAndAddIntoUGI
parameter_list|(
name|UserGroupInformation
name|clientUgi
parameter_list|,
name|String
name|tokenSig
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|tokenStrForm
init|=
name|getDelegationTokenStr
argument_list|(
name|clientUgi
argument_list|,
name|clientUgi
argument_list|)
decl_stmt|;
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|t
init|=
operator|new
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
argument_list|()
decl_stmt|;
name|t
operator|.
name|decodeFromUrlString
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
comment|//check whether the username in the token is what we expect
name|DelegationTokenIdentifier
name|d
init|=
operator|new
name|DelegationTokenIdentifier
argument_list|()
decl_stmt|;
name|d
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|t
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Usernames don't match"
argument_list|,
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
operator|.
name|equals
argument_list|(
name|d
operator|.
name|getUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tokenSig
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"hive.metastore.token.signature"
argument_list|,
name|tokenSig
argument_list|)
expr_stmt|;
name|t
operator|.
name|setService
argument_list|(
operator|new
name|Text
argument_list|(
name|tokenSig
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//add the token to the clientUgi for securely talking to the metastore
name|clientUgi
operator|.
name|addToken
argument_list|(
name|t
argument_list|)
expr_stmt|;
comment|//Create the metastore client as the clientUgi. Doing so this
comment|//way will give the client access to the token that was added earlier
comment|//in the clientUgi
name|HiveMetaStoreClient
name|hiveClient
init|=
name|clientUgi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|HiveMetaStoreClient
argument_list|>
argument_list|()
block|{
specifier|public
name|HiveMetaStoreClient
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStoreClient
name|hiveClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|hiveClient
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Couldn't connect to metastore"
argument_list|,
name|hiveClient
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|//try out some metastore operations
name|createDBAndVerifyExistence
argument_list|(
name|hiveClient
argument_list|)
expr_stmt|;
comment|//check that getDelegationToken fails since we are not authenticating
comment|//over kerberos
name|boolean
name|pass
init|=
literal|false
decl_stmt|;
try|try
block|{
name|hiveClient
operator|.
name|getDelegationToken
argument_list|(
name|clientUgi
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
name|pass
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Expected the getDelegationToken call to fail"
argument_list|,
name|pass
operator|==
literal|true
argument_list|)
expr_stmt|;
name|hiveClient
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//Now cancel the delegation token
name|HiveMetaStore
operator|.
name|cancelDelegationToken
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
comment|//now metastore connection should fail
name|hiveClient
operator|=
name|clientUgi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|HiveMetaStoreClient
argument_list|>
argument_list|()
block|{
specifier|public
name|HiveMetaStoreClient
name|run
parameter_list|()
block|{
try|try
block|{
name|HiveMetaStoreClient
name|hiveClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|hiveClient
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected metastore operations to fail"
argument_list|,
name|hiveClient
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createDBAndVerifyExistence
parameter_list|(
name|HiveMetaStoreClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"simpdb"
decl_stmt|;
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|Database
name|db1
init|=
name|client
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Databases do not match"
argument_list|,
name|db1
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|findFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|socket
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|port
return|;
block|}
block|}
end_class

end_unit

