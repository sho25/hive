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
name|assertFalse
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
name|assertTrue
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|PlainSaslHelper
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
comment|/**  * ThriftCLIServiceTest.  * This is the abstract class that tests ThriftCLIService.  * Subclass this to test more specific behaviour.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ThriftCLIServiceTest
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
name|TCLIService
operator|.
name|Client
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
name|anonymousUser
init|=
literal|"anonymous"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|anonymousPasswd
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
name|MetaStoreUtils
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
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
comment|// Start HiveServer2 with given config
comment|// Fail if server doesn't start
try|try
block|{
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
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
name|TTransport
name|createBinaryTransport
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|PlainSaslHelper
operator|.
name|getPlainTransport
argument_list|(
name|anonymousUser
argument_list|,
name|anonymousPasswd
argument_list|,
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|void
name|initClient
parameter_list|(
name|TTransport
name|transport
parameter_list|)
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
name|client
operator|=
operator|new
name|TCLIService
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOpenSession
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create a new request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
comment|// Get the response; ignore exception if any
name|TOpenSessionResp
name|openResp
init|=
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Response should not be null"
argument_list|,
name|openResp
argument_list|)
expr_stmt|;
name|TSessionHandle
name|sessHandle
init|=
name|openResp
operator|.
name|getSessionHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Session handle should not be null"
argument_list|,
name|sessHandle
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|openResp
operator|.
name|getStatus
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|TStatusCode
operator|.
name|SUCCESS_STATUS
argument_list|)
expr_stmt|;
comment|// Close the session; ignore exception if any
name|TCloseSessionReq
name|closeReq
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessHandle
argument_list|)
decl_stmt|;
name|client
operator|.
name|CloseSession
argument_list|(
name|closeReq
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
comment|// Create a new open session request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|TSessionHandle
name|sessHandle
init|=
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
name|TGetFunctionsReq
name|funcReq
init|=
operator|new
name|TGetFunctionsReq
argument_list|()
decl_stmt|;
name|funcReq
operator|.
name|setSessionHandle
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
name|funcReq
operator|.
name|setFunctionName
argument_list|(
literal|"*"
argument_list|)
expr_stmt|;
name|funcReq
operator|.
name|setCatalogName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|funcReq
operator|.
name|setSchemaName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|TGetFunctionsResp
name|funcResp
init|=
name|client
operator|.
name|GetFunctions
argument_list|(
name|funcReq
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|funcResp
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|funcResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|funcResp
operator|.
name|getStatus
argument_list|()
operator|.
name|getStatusCode
argument_list|()
operator|==
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|)
expr_stmt|;
comment|// Close the session; ignore exception if any
name|TCloseSessionReq
name|closeReq
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessHandle
argument_list|)
decl_stmt|;
name|client
operator|.
name|CloseSession
argument_list|(
name|closeReq
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
comment|// Create a new request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|TSessionHandle
name|sessHandle
init|=
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
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
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Drop the table if it exists
name|queryString
operator|=
literal|"DROP TABLE IF EXISTS TEST_EXEC_THRIFT"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Create a test table
name|queryString
operator|=
literal|"CREATE TABLE TEST_EXEC_THRIFT(ID STRING)"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Execute another query
name|queryString
operator|=
literal|"SELECT ID FROM TEST_EXEC_THRIFT"
expr_stmt|;
name|TExecuteStatementResp
name|execResp
init|=
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TOperationHandle
name|operationHandle
init|=
name|execResp
operator|.
name|getOperationHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|TGetOperationStatusReq
name|opStatusReq
init|=
operator|new
name|TGetOperationStatusReq
argument_list|()
decl_stmt|;
name|opStatusReq
operator|.
name|setOperationHandle
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|opStatusReq
argument_list|)
expr_stmt|;
name|TGetOperationStatusResp
name|opStatusResp
init|=
name|client
operator|.
name|GetOperationStatus
argument_list|(
name|opStatusReq
argument_list|)
decl_stmt|;
name|TOperationState
name|state
init|=
name|opStatusResp
operator|.
name|getOperationState
argument_list|()
decl_stmt|;
comment|// Expect query to be completed now
name|assertEquals
argument_list|(
literal|"Query should be finished"
argument_list|,
name|TOperationState
operator|.
name|FINISHED_STATE
argument_list|,
name|state
argument_list|)
expr_stmt|;
comment|// Cleanup
name|queryString
operator|=
literal|"DROP TABLE TEST_EXEC_THRIFT"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Close the session; ignore exception if any
name|TCloseSessionReq
name|closeReq
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessHandle
argument_list|)
decl_stmt|;
name|client
operator|.
name|CloseSession
argument_list|(
name|closeReq
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test asynchronous query execution and error message reporting to the client    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testExecuteStatementAsync
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create a new request object
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|TSessionHandle
name|sessHandle
init|=
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
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
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Drop the table if it exists
name|queryString
operator|=
literal|"DROP TABLE IF EXISTS TEST_EXEC_ASYNC_THRIFT"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Create a test table
name|queryString
operator|=
literal|"CREATE TABLE TEST_EXEC_ASYNC_THRIFT(ID STRING)"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Execute another query
name|queryString
operator|=
literal|"SELECT ID FROM TEST_EXEC_ASYNC_THRIFT"
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Will attempt to execute: "
operator|+
name|queryString
argument_list|)
expr_stmt|;
name|TExecuteStatementResp
name|execResp
init|=
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|TOperationHandle
name|operationHandle
init|=
name|execResp
operator|.
name|getOperationHandle
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
comment|// Poll on the operation status till the query is completed
name|boolean
name|isQueryRunning
init|=
literal|true
decl_stmt|;
name|TGetOperationStatusReq
name|opStatusReq
decl_stmt|;
name|TGetOperationStatusResp
name|opStatusResp
init|=
literal|null
decl_stmt|;
name|TOperationState
name|state
init|=
literal|null
decl_stmt|;
name|long
name|pollTimeout
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|100000
decl_stmt|;
while|while
condition|(
name|isQueryRunning
condition|)
block|{
comment|// Break if polling times out
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|pollTimeout
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Polling timed out"
argument_list|)
expr_stmt|;
break|break;
block|}
name|opStatusReq
operator|=
operator|new
name|TGetOperationStatusReq
argument_list|()
expr_stmt|;
name|opStatusReq
operator|.
name|setOperationHandle
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|opStatusReq
argument_list|)
expr_stmt|;
name|opStatusResp
operator|=
name|client
operator|.
name|GetOperationStatus
argument_list|(
name|opStatusReq
argument_list|)
expr_stmt|;
name|state
operator|=
name|opStatusResp
operator|.
name|getOperationState
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Current state: "
operator|+
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|==
name|TOperationState
operator|.
name|CANCELED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|CLOSED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|FINISHED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|ERROR_STATE
condition|)
block|{
name|isQueryRunning
operator|=
literal|false
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|// Expect query to be successfully completed now
name|assertEquals
argument_list|(
literal|"Query should be finished"
argument_list|,
name|TOperationState
operator|.
name|FINISHED_STATE
argument_list|,
name|state
argument_list|)
expr_stmt|;
comment|// Execute a malformed query
comment|// This query will give a runtime error
name|queryString
operator|=
literal|"CREATE TABLE NON_EXISTING_TAB (ID STRING) location 'hdfs://localhost:10000/a/b/c'"
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Will attempt to execute: "
operator|+
name|queryString
argument_list|)
expr_stmt|;
name|execResp
operator|=
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|operationHandle
operator|=
name|execResp
operator|.
name|getOperationHandle
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|isQueryRunning
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|isQueryRunning
condition|)
block|{
comment|// Break if polling times out
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>
name|pollTimeout
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Polling timed out"
argument_list|)
expr_stmt|;
break|break;
block|}
name|opStatusReq
operator|=
operator|new
name|TGetOperationStatusReq
argument_list|()
expr_stmt|;
name|opStatusReq
operator|.
name|setOperationHandle
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|opStatusReq
argument_list|)
expr_stmt|;
name|opStatusResp
operator|=
name|client
operator|.
name|GetOperationStatus
argument_list|(
name|opStatusReq
argument_list|)
expr_stmt|;
name|state
operator|=
name|opStatusResp
operator|.
name|getOperationState
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Current state: "
operator|+
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|==
name|TOperationState
operator|.
name|CANCELED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|CLOSED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|FINISHED_STATE
operator|||
name|state
operator|==
name|TOperationState
operator|.
name|ERROR_STATE
condition|)
block|{
name|isQueryRunning
operator|=
literal|false
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|// Expect query to return an error state
name|assertEquals
argument_list|(
literal|"Operation should be in error state"
argument_list|,
name|TOperationState
operator|.
name|ERROR_STATE
argument_list|,
name|state
argument_list|)
expr_stmt|;
comment|// sqlState, errorCode should be set to appropriate values
name|assertEquals
argument_list|(
name|opStatusResp
operator|.
name|getSqlState
argument_list|()
argument_list|,
literal|"08S01"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|opStatusResp
operator|.
name|getErrorCode
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Cleanup
name|queryString
operator|=
literal|"DROP TABLE TEST_EXEC_ASYNC_THRIFT"
expr_stmt|;
name|executeQuery
argument_list|(
name|queryString
argument_list|,
name|sessHandle
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Close the session; ignore exception if any
name|TCloseSessionReq
name|closeReq
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessHandle
argument_list|)
decl_stmt|;
name|client
operator|.
name|CloseSession
argument_list|(
name|closeReq
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TExecuteStatementResp
name|executeQuery
parameter_list|(
name|String
name|queryString
parameter_list|,
name|TSessionHandle
name|sessHandle
parameter_list|,
name|boolean
name|runAsync
parameter_list|)
throws|throws
name|Exception
block|{
name|TExecuteStatementReq
name|execReq
init|=
operator|new
name|TExecuteStatementReq
argument_list|()
decl_stmt|;
name|execReq
operator|.
name|setSessionHandle
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
name|execReq
operator|.
name|setStatement
argument_list|(
name|queryString
argument_list|)
expr_stmt|;
name|execReq
operator|.
name|setRunAsync
argument_list|(
name|runAsync
argument_list|)
expr_stmt|;
name|TExecuteStatementResp
name|execResp
init|=
name|client
operator|.
name|ExecuteStatement
argument_list|(
name|execReq
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|execResp
argument_list|)
expr_stmt|;
return|return
name|execResp
return|;
block|}
specifier|protected
name|void
name|testOpenSessionExpectedException
parameter_list|()
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
block|}
end_class

end_unit

