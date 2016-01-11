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
name|cli
operator|.
name|thrift
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|hive
operator|.
name|jdbc
operator|.
name|HttpBasicAuthInterceptor
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
name|HiveAuthFactory
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
name|HiveAuthFactory
operator|.
name|AuthTypes
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
name|TOpenSessionReq
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
name|impl
operator|.
name|client
operator|.
name|DefaultHttpClient
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
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
name|THttpClient
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
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  *  * TestThriftHttpCLIService.  * This tests ThriftCLIService started in http mode.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestThriftHttpCLIService
extends|extends
name|ThriftCLIServiceTest
block|{
specifier|private
specifier|static
name|String
name|transportMode
init|=
literal|"http"
decl_stmt|;
specifier|private
specifier|static
name|String
name|thriftHttpPath
init|=
literal|"cliservice"
decl_stmt|;
comment|/**    *  HttpBasicAuthInterceptorWithLogging    *  This adds httpRequestHeaders to the BasicAuthInterceptor    */
specifier|public
class|class
name|HttpBasicAuthInterceptorWithLogging
extends|extends
name|HttpBasicAuthInterceptor
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|requestHeaders
decl_stmt|;
specifier|public
name|HttpBasicAuthInterceptorWithLogging
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
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|additionalHeaders
parameter_list|)
block|{
name|super
argument_list|(
name|username
argument_list|,
name|password
argument_list|,
name|cookieStore
argument_list|,
name|cn
argument_list|,
name|isSSL
argument_list|,
name|additionalHeaders
argument_list|)
expr_stmt|;
name|requestHeaders
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
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
name|super
operator|.
name|process
argument_list|(
name|httpRequest
argument_list|,
name|httpContext
argument_list|)
expr_stmt|;
name|String
name|currHeaders
init|=
literal|""
decl_stmt|;
for|for
control|(
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
name|h
range|:
name|httpRequest
operator|.
name|getAllHeaders
argument_list|()
control|)
block|{
name|currHeaders
operator|+=
name|h
operator|.
name|getName
argument_list|()
operator|+
literal|":"
operator|+
name|h
operator|.
name|getValue
argument_list|()
operator|+
literal|" "
expr_stmt|;
block|}
name|requestHeaders
operator|.
name|add
argument_list|(
name|currHeaders
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getRequestHeaders
parameter_list|()
block|{
return|return
name|requestHeaders
return|;
block|}
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Set up the base class
name|ThriftCLIServiceTest
operator|.
name|setUpBeforeClass
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|hiveServer2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PORT
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|,
name|AuthTypes
operator|.
name|NOSASL
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|,
name|transportMode
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PATH
argument_list|,
name|thriftHttpPath
argument_list|)
expr_stmt|;
name|startHiveServer2WithConf
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|client
operator|=
name|getServiceClientInternal
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|ThriftCLIServiceTest
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Override
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{    }
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Override
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{    }
annotation|@
name|Test
comment|/**    * Tests calls from a raw (NOSASL) binary client,    * to a HiveServer2 running in http mode.    * This should throw an expected exception due to incompatibility.    * @throws Exception    */
specifier|public
name|void
name|testBinaryClientHttpServer
parameter_list|()
throws|throws
name|Exception
block|{
name|TTransport
name|transport
init|=
name|getRawBinaryTransport
argument_list|()
decl_stmt|;
name|TCLIService
operator|.
name|Client
name|rawBinaryClient
init|=
name|getClient
argument_list|(
name|transport
argument_list|)
decl_stmt|;
comment|// This will throw an expected exception since client-server modes are incompatible
name|testOpenSessionExpectedException
argument_list|(
name|rawBinaryClient
argument_list|)
expr_stmt|;
block|}
comment|/**    * Configure a wrong service endpoint for the client transport,    * and test for error.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testIncorrectHttpPath
parameter_list|()
throws|throws
name|Exception
block|{
name|thriftHttpPath
operator|=
literal|"wrongPath"
expr_stmt|;
name|TTransport
name|transport
init|=
name|getHttpTransport
argument_list|()
decl_stmt|;
name|TCLIService
operator|.
name|Client
name|httpClient
init|=
name|getClient
argument_list|(
name|transport
argument_list|)
decl_stmt|;
comment|// This will throw an expected exception since
comment|// client is communicating with the wrong http service endpoint
name|testOpenSessionExpectedException
argument_list|(
name|httpClient
argument_list|)
expr_stmt|;
comment|// Reset to correct http path
name|thriftHttpPath
operator|=
literal|"cliservice"
expr_stmt|;
block|}
specifier|private
name|void
name|testOpenSessionExpectedException
parameter_list|(
name|TCLIService
operator|.
name|Client
name|client
parameter_list|)
block|{
name|boolean
name|caughtEx
init|=
literal|false
decl_stmt|;
comment|// Create a new open session request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
try|try
block|{
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|caughtEx
operator|=
literal|true
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Exception expected: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Exception expected"
argument_list|,
name|caughtEx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TCLIService
operator|.
name|Client
name|getClient
parameter_list|(
name|TTransport
name|transport
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Create the corresponding client
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
return|return
operator|new
name|TCLIService
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
return|;
block|}
specifier|private
name|TTransport
name|getRawBinaryTransport
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|HiveAuthFactory
operator|.
name|getSocketTransport
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TTransport
name|getHttpTransport
parameter_list|()
throws|throws
name|Exception
block|{
name|DefaultHttpClient
name|httpClient
init|=
operator|new
name|DefaultHttpClient
argument_list|()
decl_stmt|;
name|String
name|httpUrl
init|=
name|transportMode
operator|+
literal|"://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/"
operator|+
name|thriftHttpPath
operator|+
literal|"/"
decl_stmt|;
name|httpClient
operator|.
name|addRequestInterceptor
argument_list|(
operator|new
name|HttpBasicAuthInterceptor
argument_list|(
name|USERNAME
argument_list|,
name|PASSWORD
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|THttpClient
argument_list|(
name|httpUrl
argument_list|,
name|httpClient
argument_list|)
return|;
block|}
comment|/**    * Test additional http headers passed to request interceptor.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testAdditionalHttpHeaders
parameter_list|()
throws|throws
name|Exception
block|{
name|TTransport
name|transport
decl_stmt|;
name|DefaultHttpClient
name|hClient
init|=
operator|new
name|DefaultHttpClient
argument_list|()
decl_stmt|;
name|String
name|httpUrl
init|=
name|transportMode
operator|+
literal|"://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/"
operator|+
name|thriftHttpPath
operator|+
literal|"/"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|additionalHeaders
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
name|additionalHeaders
operator|.
name|put
argument_list|(
literal|"key1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|additionalHeaders
operator|.
name|put
argument_list|(
literal|"key2"
argument_list|,
literal|"value2"
argument_list|)
expr_stmt|;
name|HttpBasicAuthInterceptorWithLogging
name|authInt
init|=
operator|new
name|HttpBasicAuthInterceptorWithLogging
argument_list|(
name|USERNAME
argument_list|,
name|PASSWORD
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|additionalHeaders
argument_list|)
decl_stmt|;
name|hClient
operator|.
name|addRequestInterceptor
argument_list|(
name|authInt
argument_list|)
expr_stmt|;
name|transport
operator|=
operator|new
name|THttpClient
argument_list|(
name|httpUrl
argument_list|,
name|hClient
argument_list|)
expr_stmt|;
name|TCLIService
operator|.
name|Client
name|httpClient
init|=
name|getClient
argument_list|(
name|transport
argument_list|)
decl_stmt|;
comment|// Create a new open session request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|httpClient
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|headers
init|=
name|authInt
operator|.
name|getRequestHeaders
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|h
range|:
name|headers
control|)
block|{
name|assertTrue
argument_list|(
name|h
operator|.
name|contains
argument_list|(
literal|"key1:value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|h
operator|.
name|contains
argument_list|(
literal|"key2:value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

