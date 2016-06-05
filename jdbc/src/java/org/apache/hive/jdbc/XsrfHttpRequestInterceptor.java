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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
specifier|public
class|class
name|XsrfHttpRequestInterceptor
implements|implements
name|HttpRequestInterceptor
block|{
comment|// Note : This implements HttpRequestInterceptor rather than extending
comment|// HttpRequestInterceptorBase, because that class is an auth-specific
comment|// class and refactoring would kludge too many things that are potentially
comment|// public api.
comment|//
comment|// At the base, though, what we do is a very simple thing to protect
comment|// against CSRF attacks, and that is to simply add another header. If
comment|// HS2 is running with an XSRF filter enabled, then it will reject all
comment|// requests that do not contain this. Thus, we add this in here on the
comment|// client-side. This simple check prevents random other websites from
comment|// redirecting a browser that has login credentials from making a
comment|// request to HS2 on their behalf.
specifier|private
specifier|static
name|boolean
name|injectHeader
init|=
literal|true
decl_stmt|;
specifier|public
specifier|static
name|void
name|enableHeaderInjection
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|injectHeader
operator|=
name|enabled
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
name|injectHeader
condition|)
block|{
name|httpRequest
operator|.
name|addHeader
argument_list|(
literal|"X-XSRF-HEADER"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

