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
name|security
operator|.
name|Security
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|callback
operator|.
name|Callback
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
name|callback
operator|.
name|CallbackHandler
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
name|callback
operator|.
name|NameCallback
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
name|callback
operator|.
name|PasswordCallback
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
name|callback
operator|.
name|UnsupportedCallbackException
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
name|AuthenticationException
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
name|AuthorizeCallback
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
name|SaslException
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
name|auth
operator|.
name|AuthenticationProviderFactory
operator|.
name|AuthMethods
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
name|auth
operator|.
name|PlainSaslServer
operator|.
name|SaslPlainProvider
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
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TCLIService
operator|.
name|Iface
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
name|TProcessor
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
name|TSaslClientTransport
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
name|TTransportFactory
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

begin_class
specifier|public
specifier|final
class|class
name|PlainSaslHelper
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
name|PlainSaslHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|TProcessorFactory
name|getPlainProcessorFactory
parameter_list|(
name|ThriftCLIService
name|service
parameter_list|)
block|{
return|return
operator|new
name|SQLPlainProcessorFactory
argument_list|(
name|service
argument_list|)
return|;
block|}
comment|// Register Plain SASL server provider
static|static
block|{
name|Security
operator|.
name|addProvider
argument_list|(
operator|new
name|SaslPlainProvider
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|TTransportFactory
name|getPlainTransportFactory
parameter_list|(
name|String
name|authTypeStr
parameter_list|)
throws|throws
name|LoginException
block|{
name|TSaslServerTransport
operator|.
name|Factory
name|saslFactory
init|=
operator|new
name|TSaslServerTransport
operator|.
name|Factory
argument_list|()
decl_stmt|;
try|try
block|{
name|saslFactory
operator|.
name|addServerDefinition
argument_list|(
literal|"PLAIN"
argument_list|,
name|authTypeStr
argument_list|,
literal|null
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|PlainServerCallbackHandler
argument_list|(
name|authTypeStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthenticationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LoginException
argument_list|(
literal|"Error setting callback handler"
operator|+
name|e
argument_list|)
throw|;
block|}
return|return
name|saslFactory
return|;
block|}
specifier|static
name|TTransportFactory
name|getDualPlainTransportFactory
parameter_list|(
name|TTransportFactory
name|otherTrans
parameter_list|,
name|String
name|trustedDomain
parameter_list|)
throws|throws
name|LoginException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Created additional transport factory for skipping authentication when client "
operator|+
literal|"connection is from the same domain."
argument_list|)
expr_stmt|;
return|return
operator|new
name|DualSaslTransportFactory
argument_list|(
name|otherTrans
argument_list|,
name|trustedDomain
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TTransport
name|getPlainTransport
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|TTransport
name|underlyingTransport
parameter_list|)
throws|throws
name|SaslException
block|{
return|return
operator|new
name|TSaslClientTransport
argument_list|(
literal|"PLAIN"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|PlainCallbackHandler
argument_list|(
name|username
argument_list|,
name|password
argument_list|)
argument_list|,
name|underlyingTransport
argument_list|)
return|;
block|}
comment|// Return true if the remote host is from the trusted domain, i.e. host URL has the same
comment|// suffix as the trusted domain.
specifier|static
specifier|public
name|boolean
name|isHostFromTrustedDomain
parameter_list|(
name|String
name|remoteHost
parameter_list|,
name|String
name|trustedDomain
parameter_list|)
block|{
return|return
name|remoteHost
operator|.
name|endsWith
argument_list|(
name|trustedDomain
argument_list|)
return|;
block|}
specifier|private
name|PlainSaslHelper
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't initialize class"
argument_list|)
throw|;
block|}
specifier|static
specifier|final
class|class
name|DualSaslTransportFactory
extends|extends
name|TTransportFactory
block|{
name|TTransportFactory
name|otherFactory
decl_stmt|;
name|TTransportFactory
name|noAuthFactory
decl_stmt|;
name|String
name|trustedDomain
decl_stmt|;
name|DualSaslTransportFactory
parameter_list|(
name|TTransportFactory
name|otherFactory
parameter_list|,
name|String
name|trustedDomain
parameter_list|)
throws|throws
name|LoginException
block|{
name|this
operator|.
name|noAuthFactory
operator|=
name|getPlainTransportFactory
argument_list|(
name|AuthMethods
operator|.
name|NONE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|otherFactory
operator|=
name|otherFactory
expr_stmt|;
name|this
operator|.
name|trustedDomain
operator|=
name|trustedDomain
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TTransport
name|getTransport
parameter_list|(
specifier|final
name|TTransport
name|trans
parameter_list|)
block|{
name|TSocket
name|tSocket
init|=
literal|null
decl_stmt|;
comment|// Attempt to avoid authentication if only we can fetch the client IP address and it
comment|// happens to be from the same domain as the server.
if|if
condition|(
name|trans
operator|instanceof
name|TSocket
condition|)
block|{
name|tSocket
operator|=
operator|(
name|TSocket
operator|)
name|trans
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|trans
operator|instanceof
name|TSaslServerTransport
condition|)
block|{
name|TSaslServerTransport
name|saslTrans
init|=
operator|(
name|TSaslServerTransport
operator|)
name|trans
decl_stmt|;
name|tSocket
operator|=
call|(
name|TSocket
call|)
argument_list|(
name|saslTrans
operator|.
name|getUnderlyingTransport
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|remoteHost
init|=
name|tSocket
operator|!=
literal|null
condition|?
name|tSocket
operator|.
name|getSocket
argument_list|()
operator|.
name|getInetAddress
argument_list|()
operator|.
name|getCanonicalHostName
argument_list|()
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|remoteHost
operator|!=
literal|null
operator|&&
name|isHostFromTrustedDomain
argument_list|(
name|remoteHost
argument_list|,
name|trustedDomain
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No authentication performed because the connecting host "
operator|+
name|remoteHost
operator|+
literal|" is "
operator|+
literal|"from the trusted domain "
operator|+
name|trustedDomain
argument_list|)
expr_stmt|;
return|return
name|noAuthFactory
operator|.
name|getTransport
argument_list|(
name|trans
argument_list|)
return|;
block|}
return|return
name|otherFactory
operator|.
name|getTransport
argument_list|(
name|trans
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|PlainServerCallbackHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|AuthMethods
name|authMethod
decl_stmt|;
name|PlainServerCallbackHandler
parameter_list|(
name|String
name|authMethodStr
parameter_list|)
throws|throws
name|AuthenticationException
block|{
name|authMethod
operator|=
name|AuthMethods
operator|.
name|getValidAuthMethod
argument_list|(
name|authMethodStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Callback
index|[]
name|callbacks
parameter_list|)
throws|throws
name|IOException
throws|,
name|UnsupportedCallbackException
block|{
name|String
name|username
init|=
literal|null
decl_stmt|;
name|String
name|password
init|=
literal|null
decl_stmt|;
name|AuthorizeCallback
name|ac
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Callback
name|callback
range|:
name|callbacks
control|)
block|{
if|if
condition|(
name|callback
operator|instanceof
name|NameCallback
condition|)
block|{
name|NameCallback
name|nc
init|=
operator|(
name|NameCallback
operator|)
name|callback
decl_stmt|;
name|username
operator|=
name|nc
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|PasswordCallback
condition|)
block|{
name|PasswordCallback
name|pc
init|=
operator|(
name|PasswordCallback
operator|)
name|callback
decl_stmt|;
name|password
operator|=
operator|new
name|String
argument_list|(
name|pc
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|AuthorizeCallback
condition|)
block|{
name|ac
operator|=
operator|(
name|AuthorizeCallback
operator|)
name|callback
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedCallbackException
argument_list|(
name|callback
argument_list|)
throw|;
block|}
block|}
name|PasswdAuthenticationProvider
name|provider
init|=
name|AuthenticationProviderFactory
operator|.
name|getAuthenticationProvider
argument_list|(
name|authMethod
argument_list|)
decl_stmt|;
name|provider
operator|.
name|Authenticate
argument_list|(
name|username
argument_list|,
name|password
argument_list|)
expr_stmt|;
if|if
condition|(
name|ac
operator|!=
literal|null
condition|)
block|{
name|ac
operator|.
name|setAuthorized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|PlainCallbackHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|String
name|username
decl_stmt|;
specifier|private
specifier|final
name|String
name|password
decl_stmt|;
specifier|public
name|PlainCallbackHandler
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|)
block|{
name|this
operator|.
name|username
operator|=
name|username
expr_stmt|;
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Callback
index|[]
name|callbacks
parameter_list|)
throws|throws
name|IOException
throws|,
name|UnsupportedCallbackException
block|{
for|for
control|(
name|Callback
name|callback
range|:
name|callbacks
control|)
block|{
if|if
condition|(
name|callback
operator|instanceof
name|NameCallback
condition|)
block|{
name|NameCallback
name|nameCallback
init|=
operator|(
name|NameCallback
operator|)
name|callback
decl_stmt|;
name|nameCallback
operator|.
name|setName
argument_list|(
name|username
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|callback
operator|instanceof
name|PasswordCallback
condition|)
block|{
name|PasswordCallback
name|passCallback
init|=
operator|(
name|PasswordCallback
operator|)
name|callback
decl_stmt|;
name|passCallback
operator|.
name|setPassword
argument_list|(
name|password
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedCallbackException
argument_list|(
name|callback
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|SQLPlainProcessorFactory
extends|extends
name|TProcessorFactory
block|{
specifier|private
specifier|final
name|ThriftCLIService
name|service
decl_stmt|;
name|SQLPlainProcessorFactory
parameter_list|(
name|ThriftCLIService
name|service
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TProcessor
name|getProcessor
parameter_list|(
name|TTransport
name|trans
parameter_list|)
block|{
return|return
operator|new
name|TSetIpAddressProcessor
argument_list|<
name|Iface
argument_list|>
argument_list|(
name|service
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

