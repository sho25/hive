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
name|server
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
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
name|MetaStoreTestUtils
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
name|CLIService
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
name|OperationHandle
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
name|SessionHandle
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
name|session
operator|.
name|SessionManager
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
name|TProtocolVersion
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
name|List
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
operator|.
name|HTTP_FORBIDDEN
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
operator|.
name|HTTP_OK
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

begin_comment
comment|/**  * TestHS2HttpServer -- executes tests of HiveServer2 HTTP Server.  */
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
name|CLIService
name|client
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|SessionManager
name|sm
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
specifier|private
specifier|static
name|String
name|apiBaseURL
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
name|MetaStoreTestUtils
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
name|apiBaseURL
operator|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/api/v1"
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
name|Exception
name|hs2Exception
init|=
literal|null
decl_stmt|;
name|boolean
name|hs2Started
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|tryCount
init|=
literal|0
init|;
operator|(
name|tryCount
operator|<
name|MetaStoreTestUtils
operator|.
name|RETRY_COUNT
operator|)
condition|;
name|tryCount
operator|++
control|)
block|{
try|try
block|{
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
name|client
operator|=
name|hiveServer2
operator|.
name|getCliService
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|hs2Started
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
name|HiveConf
operator|.
name|setIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|,
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PORT
argument_list|,
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
argument_list|,
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
argument_list|)
expr_stmt|;
name|webUIPort
operator|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|hs2Started
condition|)
block|{
throw|throw
operator|(
name|hs2Exception
operator|)
throw|;
block|}
name|sm
operator|=
name|hiveServer2
operator|.
name|getCliService
argument_list|()
operator|.
name|getSessionManager
argument_list|()
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
name|testBaseUrlResponseHeader
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
literal|"/"
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
name|String
name|xfoHeader
init|=
name|conn
operator|.
name|getHeaderField
argument_list|(
literal|"X-FRAME-OPTIONS"
argument_list|)
decl_stmt|;
name|String
name|xXSSProtectionHeader
init|=
name|conn
operator|.
name|getHeaderField
argument_list|(
literal|"X-XSS-Protection"
argument_list|)
decl_stmt|;
name|String
name|xContentTypeHeader
init|=
name|conn
operator|.
name|getHeaderField
argument_list|(
literal|"X-Content-Type-Options"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|xfoHeader
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|xXSSProtectionHeader
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|xContentTypeHeader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDirListingDisabledOnStaticServlet
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|url
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/static"
decl_stmt|;
name|getReaderForUrl
argument_list|(
name|url
argument_list|,
name|HTTP_FORBIDDEN
argument_list|)
expr_stmt|;
block|}
specifier|private
name|BufferedReader
name|getReaderForUrl
parameter_list|(
name|String
name|urlString
parameter_list|,
name|int
name|expectedStatus
parameter_list|)
throws|throws
name|Exception
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|urlString
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
name|expectedStatus
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedStatus
operator|!=
name|HTTP_OK
condition|)
block|{
return|return
literal|null
return|;
block|}
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
return|return
name|reader
return|;
block|}
specifier|private
name|String
name|readFromUrl
parameter_list|(
name|String
name|urlString
parameter_list|)
throws|throws
name|Exception
block|{
name|BufferedReader
name|reader
init|=
name|getReaderForUrl
argument_list|(
name|urlString
argument_list|,
name|HTTP_OK
argument_list|)
decl_stmt|;
name|StringBuilder
name|response
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|String
name|inputLine
decl_stmt|;
while|while
condition|(
operator|(
name|inputLine
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
name|response
operator|.
name|append
argument_list|(
name|inputLine
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|response
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|JsonNode
argument_list|>
name|getListOfNodes
parameter_list|(
name|String
name|json
parameter_list|)
throws|throws
name|Exception
block|{
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|JsonNode
name|rootNode
init|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|JsonNode
argument_list|>
name|nodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|rootNode
operator|.
name|isArray
argument_list|()
condition|)
block|{
for|for
control|(
specifier|final
name|JsonNode
name|node
range|:
name|rootNode
control|)
block|{
name|nodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodes
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testApiServletHistoricalQueries
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|historicalQueriesRoute
init|=
literal|"/queries/historical"
decl_stmt|;
specifier|final
name|SessionHandle
name|handle
init|=
name|sm
operator|.
name|openSession
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V9
argument_list|,
literal|"user"
argument_list|,
literal|"passw"
argument_list|,
literal|"127.0.0.1"
argument_list|,
operator|new
name|HashMap
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|queryString
init|=
literal|"SET "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
operator|+
literal|" = false"
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|handle
argument_list|,
name|queryString
argument_list|,
operator|new
name|HashMap
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|opHandle
operator|=
name|client
operator|.
name|executeStatement
argument_list|(
name|handle
argument_list|,
literal|"SELECT 1"
argument_list|,
operator|new
name|HashMap
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|String
name|queriesResponse
init|=
name|readFromUrl
argument_list|(
name|apiBaseURL
operator|+
name|historicalQueriesRoute
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|JsonNode
argument_list|>
name|historicalQueries
init|=
name|getListOfNodes
argument_list|(
name|queriesResponse
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|historicalQueries
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|JsonNode
name|historicalQuery
init|=
name|historicalQueries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|historicalQuery
operator|.
name|path
argument_list|(
literal|"running"
argument_list|)
operator|.
name|asBoolean
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|historicalQuery
operator|.
name|path
argument_list|(
literal|"state"
argument_list|)
operator|.
name|asText
argument_list|()
argument_list|,
literal|"FINISHED"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|historicalQuery
operator|.
name|path
argument_list|(
literal|"runtime"
argument_list|)
operator|.
name|canConvertToInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|historicalQuery
operator|.
name|path
argument_list|(
literal|"queryDisplay"
argument_list|)
operator|.
name|isObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testApiServletActiveSessions
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sessionsRoute
init|=
literal|"/sessions"
decl_stmt|;
name|String
name|initNoSessionsResponse
init|=
name|readFromUrl
argument_list|(
name|apiBaseURL
operator|+
name|sessionsRoute
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"[]"
operator|.
name|equals
argument_list|(
name|initNoSessionsResponse
argument_list|)
argument_list|)
expr_stmt|;
name|SessionHandle
name|handle1
init|=
name|sm
operator|.
name|openSession
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V9
argument_list|,
literal|"user"
argument_list|,
literal|"passw"
argument_list|,
literal|"127.0.0.1"
argument_list|,
operator|new
name|HashMap
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|oneSessionResponse
init|=
name|readFromUrl
argument_list|(
name|apiBaseURL
operator|+
name|sessionsRoute
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|JsonNode
argument_list|>
name|sessionNodes
init|=
name|getListOfNodes
argument_list|(
name|oneSessionResponse
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|sessionNodes
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|JsonNode
name|session
init|=
name|sessionNodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"sessionId"
argument_list|)
operator|.
name|asText
argument_list|()
argument_list|,
name|handle1
operator|.
name|getSessionId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"username"
argument_list|)
operator|.
name|asText
argument_list|()
argument_list|,
literal|"user"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"ipAddress"
argument_list|)
operator|.
name|asText
argument_list|()
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"operationCount"
argument_list|)
operator|.
name|asInt
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"activeTime"
argument_list|)
operator|.
name|canConvertToInt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|session
operator|.
name|path
argument_list|(
literal|"idleTime"
argument_list|)
operator|.
name|canConvertToInt
argument_list|()
argument_list|)
expr_stmt|;
name|SessionHandle
name|handle2
init|=
name|sm
operator|.
name|openSession
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V9
argument_list|,
literal|"user"
argument_list|,
literal|"passw"
argument_list|,
literal|"127.0.0.1"
argument_list|,
operator|new
name|HashMap
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|twoSessionsResponse
init|=
name|readFromUrl
argument_list|(
name|apiBaseURL
operator|+
name|sessionsRoute
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|JsonNode
argument_list|>
name|twoSessionsNodes
init|=
name|getListOfNodes
argument_list|(
name|twoSessionsResponse
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|twoSessionsNodes
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|closeSession
argument_list|(
name|handle1
argument_list|)
expr_stmt|;
name|sm
operator|.
name|closeSession
argument_list|(
name|handle2
argument_list|)
expr_stmt|;
name|String
name|endNoSessionsResponse
init|=
name|readFromUrl
argument_list|(
name|apiBaseURL
operator|+
name|sessionsRoute
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"[]"
operator|.
name|equals
argument_list|(
name|endNoSessionsResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWrongApiVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|wrongApiVersionUrl
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/api/v2"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|wrongApiVersionUrl
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
name|HTTP_BAD_REQUEST
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWrongRoute
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|wrongRouteUrl
init|=
literal|"http://localhost:"
operator|+
name|webUIPort
operator|+
literal|"/api/v1/nonexistingRoute"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|wrongRouteUrl
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
name|HTTP_NOT_FOUND
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
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
literal|"[a-zA-Z]{3} [a-zA-Z]{3} [0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"
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
name|String
name|expected
init|=
name|contextRootContent
operator|.
name|replaceAll
argument_list|(
name|datePattern
argument_list|,
name|dateMask
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|jspContent
operator|.
name|replaceAll
argument_list|(
name|datePattern
argument_list|,
name|dateMask
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
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
literal|"Got an HTTP response code other thank OK."
argument_list|,
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

