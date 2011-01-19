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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
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
block|}
block|}
specifier|private
specifier|static
specifier|final
name|int
name|port
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|TestHadoop20SAuthBridge
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
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
name|setBoolean
argument_list|(
literal|"hive.metastore.local"
argument_list|,
literal|false
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
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HiveMetaStore
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
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|thread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
name|loopUntilHMSReady
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
comment|//obtain a token by directly invoking the metastore operation(without going
comment|//through the thrift interface). Obtaining a token makes the secret manager
comment|//aware of the user and that it gave the token to the user
name|String
name|tokenStrForm
decl_stmt|;
if|if
condition|(
name|tokenSig
operator|==
literal|null
condition|)
block|{
name|tokenStrForm
operator|=
name|HiveMetaStore
operator|.
name|getDelegationToken
argument_list|(
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tokenStrForm
operator|=
name|HiveMetaStore
operator|.
name|getDelegationToken
argument_list|(
name|clientUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|tokenSig
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.metastore.token.signature"
argument_list|,
name|tokenSig
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * A simple connect test to make sure that the metastore is up    * @throws Exception    */
specifier|private
name|void
name|loopUntilHMSReady
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|retries
init|=
literal|0
decl_stmt|;
name|Exception
name|exc
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|()
decl_stmt|;
name|socket
operator|.
name|connect
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|port
argument_list|)
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|retries
operator|++
operator|>
literal|6
condition|)
block|{
comment|//give up
name|exc
operator|=
name|e
expr_stmt|;
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
block|}
throw|throw
name|exc
throw|;
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
block|}
end_class

end_unit

