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
name|assertEquals
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
name|fail
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|Service
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
name|HiveAuthConstants
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
name|OperationState
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
name|OperationStatus
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
name|server
operator|.
name|HiveServer2
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
comment|/**  * ThriftCLIServiceTestWithCookie.  */
end_comment

begin_class
specifier|public
class|class
name|ThriftCliServiceTestWithCookie
block|{
specifier|protected
specifier|static
name|int
name|port
decl_stmt|;
specifier|protected
specifier|static
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
specifier|protected
specifier|static
name|HiveServer2
name|hiveServer2
decl_stmt|;
specifier|protected
specifier|static
name|ThriftCLIServiceClient
name|client
decl_stmt|;
specifier|protected
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
specifier|static
name|String
name|USERNAME
init|=
literal|"anonymous"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|PASSWORD
init|=
literal|"anonymous"
decl_stmt|;
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
comment|// Find a free port
name|port
operator|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_AUTH_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Set the cookie max age to a very low value so that
comment|// the server sends 401 very frequently
name|hiveConf
operator|.
name|setTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_MAX_AGE
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
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
literal|"http"
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
literal|"cliservice"
argument_list|)
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
name|HiveAuthConstants
operator|.
name|AuthTypes
operator|.
name|NOSASL
operator|.
name|toString
argument_list|()
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
name|stopHiveServer2
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|startHiveServer2WithConf
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
name|Exception
name|HS2Exception
init|=
literal|null
decl_stmt|;
name|boolean
name|HS2Started
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
name|tryCount
operator|<
name|MetaStoreTestUtils
operator|.
name|RETRY_COUNT
condition|;
name|tryCount
operator|++
control|)
block|{
try|try
block|{
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
name|HS2Started
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
name|HS2Exception
operator|=
name|t
expr_stmt|;
name|port
operator|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
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
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|HS2Started
condition|)
block|{
name|HS2Exception
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
comment|// Wait for startup to complete
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HiveServer2 started on port "
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|stopHiveServer2
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveServer2
operator|!=
literal|null
condition|)
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|ThriftCLIServiceClient
name|getServiceClientInternal
parameter_list|()
block|{
for|for
control|(
name|Service
name|service
range|:
name|hiveServer2
operator|.
name|getServices
argument_list|()
control|)
block|{
if|if
condition|(
name|service
operator|instanceof
name|ThriftBinaryCLIService
condition|)
block|{
return|return
operator|new
name|ThriftCLIServiceClient
argument_list|(
operator|(
name|ThriftBinaryCLIService
operator|)
name|service
argument_list|)
return|;
block|}
if|if
condition|(
name|service
operator|instanceof
name|ThriftHttpCLIService
condition|)
block|{
return|return
operator|new
name|ThriftCLIServiceClient
argument_list|(
operator|(
name|ThriftHttpCLIService
operator|)
name|service
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"HiveServer2 not running Thrift service"
argument_list|)
throw|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{   }
comment|/**    * @throws java.lang.Exception    */
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
specifier|public
name|void
name|testOpenSession
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Open a new client session
name|SessionHandle
name|sessHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
name|USERNAME
argument_list|,
name|PASSWORD
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
comment|// Session handle should not be null
name|assertNotNull
argument_list|(
literal|"Session handle should not be null"
argument_list|,
name|sessHandle
argument_list|)
expr_stmt|;
comment|// Close client session
name|client
operator|.
name|closeSession
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetFunctions
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionHandle
name|sessHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
name|USERNAME
argument_list|,
name|PASSWORD
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Session handle should not be null"
argument_list|,
name|sessHandle
argument_list|)
expr_stmt|;
name|String
name|catalogName
init|=
literal|null
decl_stmt|;
name|String
name|schemaName
init|=
literal|null
decl_stmt|;
name|String
name|functionName
init|=
literal|"*"
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|client
operator|.
name|getFunctions
argument_list|(
name|sessHandle
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Operation handle should not be null"
argument_list|,
name|opHandle
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test synchronous query execution    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testExecuteStatement
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|opConf
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
comment|// Open a new client session
name|SessionHandle
name|sessHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
name|USERNAME
argument_list|,
name|PASSWORD
argument_list|,
name|opConf
argument_list|)
decl_stmt|;
comment|// Session handle should not be null
name|assertNotNull
argument_list|(
literal|"Session handle should not be null"
argument_list|,
name|sessHandle
argument_list|)
expr_stmt|;
comment|// Change lock manager to embedded mode
name|String
name|queryString
init|=
literal|"SET hive.lock.manager="
operator|+
literal|"org.apache.hadoop.hive.ql.lockmgr.EmbeddedLockManager"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
name|queryString
argument_list|,
name|opConf
argument_list|)
expr_stmt|;
comment|// Drop the table if it exists
name|queryString
operator|=
literal|"DROP TABLE IF EXISTS TEST_EXEC_THRIFT"
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
name|queryString
argument_list|,
name|opConf
argument_list|)
expr_stmt|;
comment|// Create a test table
name|queryString
operator|=
literal|"CREATE TABLE TEST_EXEC_THRIFT(ID STRING)"
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
name|queryString
argument_list|,
name|opConf
argument_list|)
expr_stmt|;
comment|// Execute another query
name|queryString
operator|=
literal|"SELECT ID+1 FROM TEST_EXEC_THRIFT"
expr_stmt|;
name|OperationHandle
name|opHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
name|queryString
argument_list|,
name|opConf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|OperationStatus
name|opStatus
init|=
name|client
operator|.
name|getOperationStatus
argument_list|(
name|opHandle
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|opStatus
argument_list|)
expr_stmt|;
name|OperationState
name|state
init|=
name|opStatus
operator|.
name|getState
argument_list|()
decl_stmt|;
comment|// Expect query to be completed now
name|assertEquals
argument_list|(
literal|"Query should be finished"
argument_list|,
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|state
argument_list|)
expr_stmt|;
comment|// Cleanup
name|queryString
operator|=
literal|"DROP TABLE TEST_EXEC_THRIFT"
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
name|queryString
argument_list|,
name|opConf
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

