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
name|jdbc
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
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|HttpAuthUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpRequestInterceptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|protocol
operator|.
name|HttpContext
import|;
end_import

begin_comment
comment|/**  *  * Authentication interceptor which adds Base64 encoded payload,  * containing the username and kerberos service ticket,  * to the outgoing http request header.  *  */
end_comment

begin_class
specifier|public
class|class
name|HttpKerberosRequestInterceptor
implements|implements
name|HttpRequestInterceptor
block|{
name|String
name|principal
decl_stmt|;
name|String
name|host
decl_stmt|;
name|String
name|serverHttpUrl
decl_stmt|;
comment|// A fair reentrant lock
specifier|private
specifier|static
name|ReentrantLock
name|kerberosLock
init|=
operator|new
name|ReentrantLock
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|public
name|HttpKerberosRequestInterceptor
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|host
parameter_list|,
name|String
name|serverHttpUrl
parameter_list|)
block|{
name|this
operator|.
name|principal
operator|=
name|principal
expr_stmt|;
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
name|this
operator|.
name|serverHttpUrl
operator|=
name|serverHttpUrl
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|HttpRequest
name|httpRequest
parameter_list|,
name|HttpContext
name|httpContext
parameter_list|)
throws|throws
name|HttpException
throws|,
name|IOException
block|{
name|String
name|kerberosAuthHeader
decl_stmt|;
try|try
block|{
comment|// Generate the service ticket for sending to the server.
comment|// Locking ensures the tokens are unique in case of concurrent requests
name|kerberosLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|kerberosAuthHeader
operator|=
name|HttpAuthUtils
operator|.
name|getKerberosServiceTicket
argument_list|(
name|principal
argument_list|,
name|host
argument_list|,
name|serverHttpUrl
argument_list|)
expr_stmt|;
comment|// Set the session key token (Base64 encoded) in the headers
name|httpRequest
operator|.
name|addHeader
argument_list|(
name|HttpAuthUtils
operator|.
name|AUTHORIZATION
operator|+
literal|": "
operator|+
name|HttpAuthUtils
operator|.
name|NEGOTIATE
operator|+
literal|" "
argument_list|,
name|kerberosAuthHeader
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HttpException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|kerberosLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

