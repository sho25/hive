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
name|server
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|io
operator|.
name|IOUtils
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|Assert
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
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
name|assertNotNull
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
name|HttpEntity
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
name|methods
operator|.
name|CloseableHttpResponse
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
name|methods
operator|.
name|HttpGet
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
name|CloseableHttpClient
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
name|HttpClients
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
name|util
operator|.
name|EntityUtils
import|;
end_import

begin_comment
comment|/**  * TestHS2HttpServer -- executes tests of HiveServer2 HTTP Server  */
end_comment

begin_class
specifier|public
class|class
name|TestHS2HttpServer
block|{
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|String
name|metastorePasswd
init|=
literal|"61ecbc41cdae3e6b32712a06c73606fa"
decl_stmt|;
comment|//random md5
specifier|private
specifier|static
name|Integer
name|webUIPort
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTests
parameter_list|()
throws|throws
name|Exception
block|{
name|webUIPort
operator|=
name|MetaStoreUtils
operator|.
name|findFreePortExcepting
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|getDefaultValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|,
name|metastorePasswd
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|,
name|webUIPort
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStackServlet
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|baseURL
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/stacks"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseURL
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_OK
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|conn
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|contents
init|=
literal|false
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"Process Thread Dump:"
argument_list|)
condition|)
block|{
name|contents
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|contents
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testContextRootUrlRewrite
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|datePattern
init|=
literal|"[a-zA-Z]{3} [a-zA-Z]{3} [0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]+\\[[0-9]+]"
decl_stmt|;
name|String
name|dateMask
init|=
literal|"xxxMasked_DateTime_xxx"
decl_stmt|;
name|String
name|baseURL
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/"
decl_stmt|;
name|String
name|contextRootContent
init|=
name|getURLResponseAsString
argument_list|(
name|baseURL
argument_list|)
decl_stmt|;
name|String
name|jspUrl
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/hiveserver2.jsp"
decl_stmt|;
name|String
name|jspContent
init|=
name|getURLResponseAsString
argument_list|(
name|jspUrl
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|contextRootContent
operator|.
name|replaceAll
argument_list|(
name|datePattern
argument_list|,
name|dateMask
argument_list|)
argument_list|,
name|jspContent
operator|.
name|replaceAll
argument_list|(
name|datePattern
argument_list|,
name|dateMask
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConfStrippedFromWebUI
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|pwdValFound
init|=
literal|null
decl_stmt|;
name|String
name|pwdKeyFound
init|=
literal|null
decl_stmt|;
name|CloseableHttpClient
name|httpclient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|httpclient
operator|=
name|HttpClients
operator|.
name|createDefault
argument_list|()
expr_stmt|;
name|HttpGet
name|httpGet
init|=
operator|new
name|HttpGet
argument_list|(
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/conf"
argument_list|)
decl_stmt|;
name|CloseableHttpResponse
name|response1
init|=
name|httpclient
operator|.
name|execute
argument_list|(
name|httpGet
argument_list|)
decl_stmt|;
try|try
block|{
name|HttpEntity
name|entity1
init|=
name|response1
operator|.
name|getEntity
argument_list|()
decl_stmt|;
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|entity1
operator|.
name|getContent
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|br
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
name|metastorePasswd
argument_list|)
condition|)
block|{
name|pwdValFound
operator|=
name|line
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
condition|)
block|{
name|pwdKeyFound
operator|=
name|line
expr_stmt|;
block|}
block|}
name|EntityUtils
operator|.
name|consume
argument_list|(
name|entity1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|response1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|httpclient
operator|!=
literal|null
condition|)
block|{
name|httpclient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|pwdKeyFound
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|pwdValFound
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getURLResponseAsString
parameter_list|(
name|String
name|baseURL
parameter_list|)
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseURL
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_OK
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|IOUtils
operator|.
name|copy
argument_list|(
name|conn
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|writer
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTests
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

