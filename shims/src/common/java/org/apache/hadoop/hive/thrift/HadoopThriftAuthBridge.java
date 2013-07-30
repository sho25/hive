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
comment|/**   * This class is only overridden by the secure hadoop shim. It allows   * the Thrift SASL support to bridge to Hadoop's UserGroupInformation   *& DelegationToken infrastructure.   */
end_comment

begin_class
specifier|public
class|class
name|HadoopThriftAuthBridge
block|{
specifier|public
name|Client
name|createClient
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The current version of Hadoop does not support Authentication"
argument_list|)
throw|;
block|}
specifier|public
name|Client
name|createClientWithConf
parameter_list|(
name|String
name|authType
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The current version of Hadoop does not support Authentication"
argument_list|)
throw|;
block|}
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The current version of Hadoop does not support Authentication"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|Client
block|{
comment|/**     *     * @param principalConfig In the case of Kerberos authentication this will     * be the kerberos principal name, for DIGEST-MD5 (delegation token) based     * authentication this will be null     * @param host The metastore server host name     * @param methodStr "KERBEROS" or "DIGEST"     * @param tokenStrForm This is url encoded string form of     * org.apache.hadoop.security.token.     * @param underlyingTransport the underlying transport     * @return the transport     * @throws IOException     */
specifier|public
specifier|abstract
name|TTransport
name|createClientTransport
parameter_list|(
name|String
name|principalConfig
parameter_list|,
name|String
name|host
parameter_list|,
name|String
name|methodStr
parameter_list|,
name|String
name|tokenStrForm
parameter_list|,
name|TTransport
name|underlyingTransport
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|Server
block|{
specifier|public
specifier|abstract
name|TTransportFactory
name|createTransportFactory
parameter_list|()
throws|throws
name|TTransportException
function_decl|;
specifier|public
specifier|abstract
name|TProcessor
name|wrapProcessor
parameter_list|(
name|TProcessor
name|processor
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|TProcessor
name|wrapNonAssumingProcessor
parameter_list|(
name|TProcessor
name|processor
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|InetAddress
name|getRemoteAddress
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|void
name|startDelegationTokenSecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Object
name|hmsHandler
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|String
name|getRemoteUser
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
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
name|IOException
throws|,
name|InterruptedException
function_decl|;
specifier|public
specifier|abstract
name|long
name|renewDelegationToken
parameter_list|(
name|String
name|tokenStrForm
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|abstract
name|void
name|cancelDelegationToken
parameter_list|(
name|String
name|tokenStrForm
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

