begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|services
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapIo
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapProxy
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
name|http
operator|.
name|HttpServer
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|LlapIoMemoryServlet
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LlapIoMemoryServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|ACCESS_CONTROL_ALLOW_METHODS
init|=
literal|"Access-Control-Allow-Methods"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ACCESS_CONTROL_ALLOW_ORIGIN
init|=
literal|"Access-Control-Allow-Origin"
decl_stmt|;
comment|/**    * Initialize this servlet.    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|ServletException
block|{   }
comment|/**    * Process a GET request for the specified resource.    *    * @param request    *          The servlet request we are processing    * @param response    *          The servlet response we are creating    */
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|isInstrumentationAccessAllowed
argument_list|(
name|getServletContext
argument_list|()
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
condition|)
block|{
return|return;
block|}
name|PrintWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/plain; charset=utf8"
argument_list|)
expr_stmt|;
name|response
operator|.
name|setHeader
argument_list|(
name|ACCESS_CONTROL_ALLOW_METHODS
argument_list|,
literal|"GET"
argument_list|)
expr_stmt|;
name|response
operator|.
name|setHeader
argument_list|(
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|response
operator|.
name|setHeader
argument_list|(
literal|"Cache-Control"
argument_list|,
literal|"no-transform,public,max-age=60,s-maxage=60"
argument_list|)
expr_stmt|;
name|writer
operator|=
name|response
operator|.
name|getWriter
argument_list|()
expr_stmt|;
name|LlapIo
argument_list|<
name|?
argument_list|>
name|llapIo
init|=
name|LlapProxy
operator|.
name|getIo
argument_list|()
decl_stmt|;
if|if
condition|(
name|llapIo
operator|==
literal|null
condition|)
block|{
name|writer
operator|.
name|write
argument_list|(
literal|"LLAP IO not found"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|write
argument_list|(
name|llapIo
operator|.
name|getMemoryInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught exception while processing llap status request"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

