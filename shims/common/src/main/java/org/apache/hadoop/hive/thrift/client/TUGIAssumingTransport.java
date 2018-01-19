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
name|thrift
operator|.
name|client
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|TFilterTransport
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

begin_comment
comment|/**   * The Thrift SASL transports call Sasl.createSaslServer and Sasl.createSaslClient   * inside open(). So, we need to assume the correct UGI when the transport is opened   * so that the SASL mechanisms have access to the right principal. This transport   * wraps the Sasl transports to set up the right UGI context for open().   *   * This is used on the client side, where the API explicitly opens a transport to   * the server.   */
end_comment

begin_class
specifier|public
class|class
name|TUGIAssumingTransport
extends|extends
name|TFilterTransport
block|{
specifier|protected
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|public
name|TUGIAssumingTransport
parameter_list|(
name|TTransport
name|wrapped
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
name|super
argument_list|(
name|wrapped
argument_list|)
expr_stmt|;
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|TTransportException
block|{
try|try
block|{
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|run
parameter_list|()
block|{
try|try
block|{
name|wrapped
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|tte
parameter_list|)
block|{
comment|// Wrap the transport exception in an RTE, since UGI.doAs() then goes
comment|// and unwraps this for us out of the doAs block. We then unwrap one
comment|// more time in our catch clause to get back the TTE. (ugh)
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|tte
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Received an ioe we never threw!"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Received an ie we never threw!"
argument_list|,
name|ie
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|rte
parameter_list|)
block|{
if|if
condition|(
name|rte
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TTransportException
condition|)
block|{
throw|throw
operator|(
name|TTransportException
operator|)
name|rte
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
name|rte
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

