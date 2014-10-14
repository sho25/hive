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
name|hive
operator|.
name|service
operator|.
name|auth
package|;
end_package

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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|Sasl
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
name|shims
operator|.
name|HadoopShims
operator|.
name|KerberosNameShim
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
name|shims
operator|.
name|ShimLoader
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|cli
operator|.
name|thrift
operator|.
name|ThriftCLIService
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
name|TProcessorFactory
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
name|TSSLTransportFactory
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
name|TServerSocket
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
name|TSocket
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
name|TTransport
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

begin_comment
comment|/**  * This class helps in some aspects of authentication. It creates the proper Thrift classes for the  * given configuration as well as helps with authenticating requests.  */
end_comment

begin_class
specifier|public
class|class
name|HiveAuthFactory
block|{
specifier|public
enum|enum
name|AuthTypes
block|{
name|NOSASL
argument_list|(
literal|"NOSASL"
argument_list|)
block|,
name|NONE
argument_list|(
literal|"NONE"
argument_list|)
block|,
name|LDAP
argument_list|(
literal|"LDAP"
argument_list|)
block|,
name|KERBEROS
argument_list|(
literal|"KERBEROS"
argument_list|)
block|,
name|CUSTOM
argument_list|(
literal|"CUSTOM"
argument_list|)
block|,
name|PAM
argument_list|(
literal|"PAM"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|authType
decl_stmt|;
name|AuthTypes
parameter_list|(
name|String
name|authType
parameter_list|)
block|{
name|this
operator|.
name|authType
operator|=
name|authType
expr_stmt|;
block|}
specifier|public
name|String
name|getAuthName
parameter_list|()
block|{
return|return
name|authType
return|;
block|}
block|}
specifier|private
name|HadoopThriftAuthBridge
operator|.
name|Server
name|saslServer
decl_stmt|;
specifier|private
name|String
name|authTypeStr
decl_stmt|;
specifier|private
specifier|final
name|String
name|transportMode
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HS2_PROXY_USER
init|=
literal|"hive.server2.proxy.user"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HS2_CLIENT_TOKEN
init|=
literal|"hiveserver2ClientToken"
decl_stmt|;
specifier|public
name|HiveAuthFactory
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|TTransportException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|transportMode
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|)
expr_stmt|;
name|authTypeStr
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|)
expr_stmt|;
comment|// In http mode we use NOSASL as the default auth type
if|if
condition|(
literal|"http"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transportMode
argument_list|)
condition|)
block|{
if|if
condition|(
name|authTypeStr
operator|==
literal|null
condition|)
block|{
name|authTypeStr
operator|=
name|AuthTypes
operator|.
name|NOSASL
operator|.
name|getAuthName
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|authTypeStr
operator|==
literal|null
condition|)
block|{
name|authTypeStr
operator|=
name|AuthTypes
operator|.
name|NONE
operator|.
name|getAuthName
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|getAuthName
argument_list|()
argument_list|)
operator|&&
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isSecureShimImpl
argument_list|()
condition|)
block|{
name|saslServer
operator|=
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
operator|.
name|createServer
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_KEYTAB
argument_list|)
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|)
argument_list|)
expr_stmt|;
comment|// start delegation token manager
try|try
block|{
name|saslServer
operator|.
name|startDelegationTokenSecretManager
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TTransportException
argument_list|(
literal|"Failed to start token manager"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSaslProperties
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslProps
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
name|SaslQOP
name|saslQOP
init|=
name|SaslQOP
operator|.
name|fromString
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_SASL_QOP
argument_list|)
argument_list|)
decl_stmt|;
name|saslProps
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|,
name|saslQOP
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|saslProps
operator|.
name|put
argument_list|(
name|Sasl
operator|.
name|SERVER_AUTH
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
return|return
name|saslProps
return|;
block|}
specifier|public
name|TTransportFactory
name|getAuthTransFactory
parameter_list|()
throws|throws
name|LoginException
block|{
name|TTransportFactory
name|transportFactory
decl_stmt|;
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|transportFactory
operator|=
name|saslServer
operator|.
name|createTransportFactory
argument_list|(
name|getSaslProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LoginException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|NONE
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
name|transportFactory
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransportFactory
argument_list|(
name|authTypeStr
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|LDAP
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
name|transportFactory
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransportFactory
argument_list|(
name|authTypeStr
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|PAM
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
name|transportFactory
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransportFactory
argument_list|(
name|authTypeStr
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|NOSASL
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
name|transportFactory
operator|=
operator|new
name|TTransportFactory
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|CUSTOM
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
name|transportFactory
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransportFactory
argument_list|(
name|authTypeStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|LoginException
argument_list|(
literal|"Unsupported authentication type "
operator|+
name|authTypeStr
argument_list|)
throw|;
block|}
return|return
name|transportFactory
return|;
block|}
comment|/**    * Returns the thrift processor factory for HiveServer2 running in binary mode    * @param service    * @return    * @throws LoginException    */
specifier|public
name|TProcessorFactory
name|getAuthProcFactory
parameter_list|(
name|ThriftCLIService
name|service
parameter_list|)
throws|throws
name|LoginException
block|{
if|if
condition|(
name|authTypeStr
operator|.
name|equalsIgnoreCase
argument_list|(
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|getAuthName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|KerberosSaslHelper
operator|.
name|getKerberosProcessorFactory
argument_list|(
name|saslServer
argument_list|,
name|service
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|PlainSaslHelper
operator|.
name|getPlainProcessorFactory
argument_list|(
name|service
argument_list|)
return|;
block|}
block|}
specifier|public
name|String
name|getRemoteUser
parameter_list|()
block|{
return|return
name|saslServer
operator|==
literal|null
condition|?
literal|null
else|:
name|saslServer
operator|.
name|getRemoteUser
argument_list|()
return|;
block|}
specifier|public
name|String
name|getIpAddress
parameter_list|()
block|{
if|if
condition|(
name|saslServer
operator|==
literal|null
operator|||
name|saslServer
operator|.
name|getRemoteAddress
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|saslServer
operator|.
name|getRemoteAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
return|;
block|}
block|}
comment|// Perform kerberos login using the hadoop shim API if the configuration is available
specifier|public
specifier|static
name|void
name|loginFromKeytab
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|principal
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|keyTabFile
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_KEYTAB
argument_list|)
decl_stmt|;
if|if
condition|(
name|principal
operator|.
name|isEmpty
argument_list|()
operator|||
name|keyTabFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HiveServer2 Kerberos principal or keytab is not correctly configured"
argument_list|)
throw|;
block|}
else|else
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|loginUserFromKeytab
argument_list|(
name|principal
argument_list|,
name|keyTabFile
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform SPNEGO login using the hadoop shim API if the configuration is available
specifier|public
specifier|static
name|UserGroupInformation
name|loginFromSpnegoKeytabAndReturnUGI
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|principal
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SPNEGO_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|keyTabFile
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SPNEGO_KEYTAB
argument_list|)
decl_stmt|;
if|if
condition|(
name|principal
operator|.
name|isEmpty
argument_list|()
operator|||
name|keyTabFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HiveServer2 SPNEGO principal or keytab is not correctly configured"
argument_list|)
throw|;
block|}
else|else
block|{
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|loginUserFromKeytabAndReturnUGI
argument_list|(
name|principal
argument_list|,
name|keyTabFile
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|TTransport
name|getSocketTransport
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|int
name|loginTimeout
parameter_list|)
block|{
return|return
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|loginTimeout
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TTransport
name|getSSLSocket
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|int
name|loginTimeout
parameter_list|)
throws|throws
name|TTransportException
block|{
return|return
name|TSSLTransportFactory
operator|.
name|getClientSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|loginTimeout
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TTransport
name|getSSLSocket
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|int
name|loginTimeout
parameter_list|,
name|String
name|trustStorePath
parameter_list|,
name|String
name|trustStorePassWord
parameter_list|)
throws|throws
name|TTransportException
block|{
name|TSSLTransportFactory
operator|.
name|TSSLTransportParameters
name|params
init|=
operator|new
name|TSSLTransportFactory
operator|.
name|TSSLTransportParameters
argument_list|()
decl_stmt|;
name|params
operator|.
name|setTrustStore
argument_list|(
name|trustStorePath
argument_list|,
name|trustStorePassWord
argument_list|)
expr_stmt|;
name|params
operator|.
name|requireClientAuth
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|TSSLTransportFactory
operator|.
name|getClientSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|loginTimeout
argument_list|,
name|params
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TServerSocket
name|getServerSocket
parameter_list|(
name|String
name|hiveHost
parameter_list|,
name|int
name|portNum
parameter_list|)
throws|throws
name|TTransportException
block|{
name|InetSocketAddress
name|serverAddress
decl_stmt|;
if|if
condition|(
name|hiveHost
operator|==
literal|null
operator|||
name|hiveHost
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|serverAddress
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|portNum
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serverAddress
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|hiveHost
argument_list|,
name|portNum
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TServerSocket
argument_list|(
name|serverAddress
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TServerSocket
name|getServerSSLSocket
parameter_list|(
name|String
name|hiveHost
parameter_list|,
name|int
name|portNum
parameter_list|,
name|String
name|keyStorePath
parameter_list|,
name|String
name|keyStorePassWord
parameter_list|)
throws|throws
name|TTransportException
throws|,
name|UnknownHostException
block|{
name|TSSLTransportFactory
operator|.
name|TSSLTransportParameters
name|params
init|=
operator|new
name|TSSLTransportFactory
operator|.
name|TSSLTransportParameters
argument_list|()
decl_stmt|;
name|params
operator|.
name|setKeyStore
argument_list|(
name|keyStorePath
argument_list|,
name|keyStorePassWord
argument_list|)
expr_stmt|;
name|InetAddress
name|serverAddress
decl_stmt|;
if|if
condition|(
name|hiveHost
operator|==
literal|null
operator|||
name|hiveHost
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|serverAddress
operator|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|serverAddress
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|hiveHost
argument_list|)
expr_stmt|;
block|}
return|return
name|TSSLTransportFactory
operator|.
name|getServerSocket
argument_list|(
name|portNum
argument_list|,
literal|0
argument_list|,
name|serverAddress
argument_list|,
name|params
argument_list|)
return|;
block|}
comment|// retrieve delegation token for the given user
specifier|public
name|String
name|getDelegationToken
parameter_list|(
name|String
name|owner
parameter_list|,
name|String
name|renewer
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|saslServer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Delegation token only supported over kerberos authentication"
argument_list|)
throw|;
block|}
try|try
block|{
name|String
name|tokenStr
init|=
name|saslServer
operator|.
name|getDelegationTokenWithService
argument_list|(
name|owner
argument_list|,
name|renewer
argument_list|,
name|HS2_CLIENT_TOKEN
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenStr
operator|==
literal|null
operator|||
name|tokenStr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Received empty retrieving delegation token for user "
operator|+
name|owner
argument_list|)
throw|;
block|}
return|return
name|tokenStr
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error retrieving delegation token for user "
operator|+
name|owner
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"delegation token retrieval interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// cancel given delegation token
specifier|public
name|void
name|cancelDelegationToken
parameter_list|(
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|saslServer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Delegation token only supported over kerberos authentication"
argument_list|)
throw|;
block|}
try|try
block|{
name|saslServer
operator|.
name|cancelDelegationToken
argument_list|(
name|delegationToken
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error canceling delegation token "
operator|+
name|delegationToken
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|renewDelegationToken
parameter_list|(
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|saslServer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Delegation token only supported over kerberos authentication"
argument_list|)
throw|;
block|}
try|try
block|{
name|saslServer
operator|.
name|renewDelegationToken
argument_list|(
name|delegationToken
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error renewing delegation token "
operator|+
name|delegationToken
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getUserFromToken
parameter_list|(
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|saslServer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Delegation token only supported over kerberos authentication"
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|saslServer
operator|.
name|getUserFromToken
argument_list|(
name|delegationToken
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error extracting user from delegation token "
operator|+
name|delegationToken
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|verifyProxyAccess
parameter_list|(
name|String
name|realUser
parameter_list|,
name|String
name|proxyUser
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|UserGroupInformation
name|sessionUgi
decl_stmt|;
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|KerberosNameShim
name|kerbName
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getKerberosNameShim
argument_list|(
name|realUser
argument_list|)
decl_stmt|;
name|String
name|shortPrincipalName
init|=
name|kerbName
operator|.
name|getServiceName
argument_list|()
decl_stmt|;
name|sessionUgi
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|createProxyUser
argument_list|(
name|shortPrincipalName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sessionUgi
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|createRemoteUser
argument_list|(
name|realUser
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|proxyUser
operator|.
name|equalsIgnoreCase
argument_list|(
name|realUser
argument_list|)
condition|)
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|authorizeProxyAccess
argument_list|(
name|proxyUser
argument_list|,
name|sessionUgi
argument_list|,
name|ipAddress
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failed to validate proxy privilege of "
operator|+
name|realUser
operator|+
literal|" for "
operator|+
name|proxyUser
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

