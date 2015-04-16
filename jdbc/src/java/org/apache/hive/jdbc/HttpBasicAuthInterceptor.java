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
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
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
name|auth
operator|.
name|UsernamePasswordCredentials
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
name|client
operator|.
name|CookieStore
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
name|client
operator|.
name|protocol
operator|.
name|ClientContext
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
name|impl
operator|.
name|auth
operator|.
name|AuthSchemeBase
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
name|impl
operator|.
name|auth
operator|.
name|BasicScheme
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
comment|/**  * The class is instantiated with the username and password, it is then  * used to add header with these credentials to HTTP requests  *  */
end_comment

begin_class
specifier|public
class|class
name|HttpBasicAuthInterceptor
implements|implements
name|HttpRequestInterceptor
block|{
name|UsernamePasswordCredentials
name|credentials
decl_stmt|;
name|AuthSchemeBase
name|authScheme
decl_stmt|;
name|CookieStore
name|cookieStore
decl_stmt|;
name|boolean
name|isCookieEnabled
decl_stmt|;
name|String
name|cookieName
decl_stmt|;
name|boolean
name|isSSL
decl_stmt|;
specifier|public
name|HttpBasicAuthInterceptor
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|CookieStore
name|cookieStore
parameter_list|,
name|String
name|cn
parameter_list|,
name|boolean
name|isSSL
parameter_list|)
block|{
if|if
condition|(
name|username
operator|!=
literal|null
condition|)
block|{
name|credentials
operator|=
operator|new
name|UsernamePasswordCredentials
argument_list|(
name|username
argument_list|,
name|password
argument_list|)
expr_stmt|;
block|}
name|authScheme
operator|=
operator|new
name|BasicScheme
argument_list|()
expr_stmt|;
name|this
operator|.
name|cookieStore
operator|=
name|cookieStore
expr_stmt|;
name|isCookieEnabled
operator|=
operator|(
name|cookieStore
operator|!=
literal|null
operator|)
expr_stmt|;
name|cookieName
operator|=
name|cn
expr_stmt|;
name|this
operator|.
name|isSSL
operator|=
name|isSSL
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
if|if
condition|(
name|isCookieEnabled
condition|)
block|{
name|httpContext
operator|.
name|setAttribute
argument_list|(
name|ClientContext
operator|.
name|COOKIE_STORE
argument_list|,
name|cookieStore
argument_list|)
expr_stmt|;
block|}
comment|// Add the authentication details under the following scenarios:
comment|// 1. Cookie Authentication is disabled OR
comment|// 2. The first time when the request is sent OR
comment|// 3. The server returns a 401, which sometimes means the cookie has expired
comment|// 4. The cookie is secured where as the client connect does not use SSL
if|if
condition|(
operator|!
name|isCookieEnabled
operator|||
operator|(
operator|(
name|httpContext
operator|.
name|getAttribute
argument_list|(
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_KEY
argument_list|)
operator|==
literal|null
operator|&&
operator|(
name|cookieStore
operator|==
literal|null
operator|||
operator|(
name|cookieStore
operator|!=
literal|null
operator|&&
name|Utils
operator|.
name|needToSendCredentials
argument_list|(
name|cookieStore
argument_list|,
name|cookieName
argument_list|,
name|isSSL
argument_list|)
operator|)
operator|)
operator|)
operator|||
operator|(
name|httpContext
operator|.
name|getAttribute
argument_list|(
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_KEY
argument_list|)
operator|!=
literal|null
operator|&&
name|httpContext
operator|.
name|getAttribute
argument_list|(
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_KEY
argument_list|)
operator|.
name|equals
argument_list|(
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_TRUE
argument_list|)
operator|)
operator|)
condition|)
block|{
name|Header
name|basicAuthHeader
init|=
name|authScheme
operator|.
name|authenticate
argument_list|(
name|credentials
argument_list|,
name|httpRequest
argument_list|,
name|httpContext
argument_list|)
decl_stmt|;
name|httpRequest
operator|.
name|addHeader
argument_list|(
name|basicAuthHeader
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isCookieEnabled
condition|)
block|{
name|httpContext
operator|.
name|setAttribute
argument_list|(
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_KEY
argument_list|,
name|Utils
operator|.
name|HIVE_SERVER2_RETRY_FALSE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

