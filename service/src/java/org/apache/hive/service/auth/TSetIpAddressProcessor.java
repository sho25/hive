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
name|TException
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
name|protocol
operator|.
name|TProtocol
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

begin_comment
comment|/**  * This class is responsible for setting the ipAddress for operations executed via HiveServer2.  *<br>  *<ul>  *<li>IP address is only set for operations that calls listeners with hookContext</li>  *<li>IP address is only set if the underlying transport mechanism is socket</li>  *</ul>  *<br>  *  * @see org.apache.hadoop.hive.ql.hooks.ExecuteWithHookContext  */
end_comment

begin_class
specifier|public
class|class
name|TSetIpAddressProcessor
parameter_list|<
name|I
extends|extends
name|Iface
parameter_list|>
extends|extends
name|TCLIService
operator|.
name|Processor
argument_list|<
name|Iface
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TSetIpAddressProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|TSetIpAddressProcessor
parameter_list|(
name|Iface
name|iface
parameter_list|)
block|{
name|super
argument_list|(
name|iface
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|process
parameter_list|(
specifier|final
name|TProtocol
name|in
parameter_list|,
specifier|final
name|TProtocol
name|out
parameter_list|)
throws|throws
name|TException
block|{
name|setIpAddress
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|setUserName
argument_list|(
name|in
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|super
operator|.
name|process
argument_list|(
name|in
argument_list|,
name|out
argument_list|)
return|;
block|}
finally|finally
block|{
name|THREAD_LOCAL_USER_NAME
operator|.
name|remove
argument_list|()
expr_stmt|;
name|THREAD_LOCAL_IP_ADDRESS
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setUserName
parameter_list|(
specifier|final
name|TProtocol
name|in
parameter_list|)
block|{
name|TTransport
name|transport
init|=
name|in
operator|.
name|getTransport
argument_list|()
decl_stmt|;
if|if
condition|(
name|transport
operator|instanceof
name|TSaslServerTransport
condition|)
block|{
name|String
name|userName
init|=
operator|(
operator|(
name|TSaslServerTransport
operator|)
name|transport
operator|)
operator|.
name|getSaslServer
argument_list|()
operator|.
name|getAuthorizationID
argument_list|()
decl_stmt|;
name|THREAD_LOCAL_USER_NAME
operator|.
name|set
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|setIpAddress
parameter_list|(
specifier|final
name|TProtocol
name|in
parameter_list|)
block|{
name|TTransport
name|transport
init|=
name|in
operator|.
name|getTransport
argument_list|()
decl_stmt|;
name|TSocket
name|tSocket
init|=
name|getUnderlyingSocketFromTransport
argument_list|(
name|transport
argument_list|)
decl_stmt|;
if|if
condition|(
name|tSocket
operator|==
literal|null
condition|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Unknown Transport, cannot determine ipAddress"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|THREAD_LOCAL_IP_ADDRESS
operator|.
name|set
argument_list|(
name|tSocket
operator|.
name|getSocket
argument_list|()
operator|.
name|getInetAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TSocket
name|getUnderlyingSocketFromTransport
parameter_list|(
name|TTransport
name|transport
parameter_list|)
block|{
while|while
condition|(
name|transport
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|transport
operator|instanceof
name|TSaslServerTransport
condition|)
block|{
name|transport
operator|=
operator|(
operator|(
name|TSaslServerTransport
operator|)
name|transport
operator|)
operator|.
name|getUnderlyingTransport
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|transport
operator|instanceof
name|TSaslClientTransport
condition|)
block|{
name|transport
operator|=
operator|(
operator|(
name|TSaslClientTransport
operator|)
name|transport
operator|)
operator|.
name|getUnderlyingTransport
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|transport
operator|instanceof
name|TSocket
condition|)
block|{
return|return
operator|(
name|TSocket
operator|)
name|transport
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|THREAD_LOCAL_IP_ADDRESS
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|String
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|THREAD_LOCAL_USER_NAME
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|String
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|String
name|getUserIpAddress
parameter_list|()
block|{
return|return
name|THREAD_LOCAL_IP_ADDRESS
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|THREAD_LOCAL_USER_NAME
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

