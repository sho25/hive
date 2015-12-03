begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|operation
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|miniHS2
operator|.
name|MiniHS2
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
name|CLIServiceClient
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
name|FetchOrientation
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
name|FetchType
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
name|HiveSQLException
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
name|RowSet
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
name|Assert
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
name|Ignore
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
comment|/**  * OperationLoggingAPITestBase  * Test the FetchResults of TFetchType.LOG in thrift level.  * This is the base class.  */
end_comment

begin_class
annotation|@
name|Ignore
specifier|public
specifier|abstract
class|class
name|OperationLoggingAPITestBase
block|{
specifier|protected
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
specifier|static
name|String
name|tableName
decl_stmt|;
specifier|private
name|File
name|dataFile
decl_stmt|;
specifier|protected
name|CLIServiceClient
name|client
decl_stmt|;
specifier|protected
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
decl_stmt|;
specifier|protected
name|SessionHandle
name|sessionHandle
decl_stmt|;
specifier|protected
specifier|final
name|String
name|sql
init|=
literal|"select * from "
operator|+
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|sqlCntStar
init|=
literal|"select count(*) from "
operator|+
name|tableName
decl_stmt|;
specifier|protected
specifier|static
name|String
index|[]
name|expectedLogsVerbose
decl_stmt|;
specifier|protected
specifier|static
name|String
index|[]
name|expectedLogsExecution
decl_stmt|;
specifier|protected
specifier|static
name|String
index|[]
name|expectedLogsPerformance
decl_stmt|;
comment|/**    * Open a session, and create a table for cases usage    * @throws Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|dataFile
operator|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|client
operator|=
name|miniHS2
operator|.
name|getServiceClient
argument_list|()
expr_stmt|;
name|sessionHandle
operator|=
name|setupSession
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Cleanup
name|String
name|queryString
init|=
literal|"DROP TABLE "
operator|+
name|tableName
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
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
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogWithVerboseMode
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|queryString
init|=
literal|"set hive.server2.logging.operation.level=verbose"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// verify whether the sql operation log is generated and fetch correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
comment|// Verbose Logs should contain everything, including execution and performance
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsExecution
argument_list|)
expr_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsPerformance
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogWithPerformanceMode
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|queryString
init|=
literal|"set hive.server2.logging.operation.level=performance"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// verify whether the sql operation log is generated and fetch correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
comment|// rowSetLog should contain execution as well as performance logs
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsExecution
argument_list|)
expr_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsPerformance
argument_list|)
expr_stmt|;
name|verifyMissingContentsInFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Restore everything to default setup to avoid discrepancy between junit test runs
name|String
name|queryString2
init|=
literal|"set hive.server2.logging.operation.level=verbose"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogWithExecutionMode
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|queryString
init|=
literal|"set hive.server2.logging.operation.level=execution"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// verify whether the sql operation log is generated and fetch correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsExecution
argument_list|)
expr_stmt|;
name|verifyMissingContentsInFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsPerformance
argument_list|)
expr_stmt|;
name|verifyMissingContentsInFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Restore everything to default setup to avoid discrepancy between junit test runs
name|String
name|queryString2
init|=
literal|"set hive.server2.logging.operation.level=verbose"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogWithNoneMode
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|queryString
init|=
literal|"set hive.server2.logging.operation.level=none"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// verify whether the sql operation log is generated and fetch correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
comment|// We should not get any rows.
assert|assert
operator|(
name|rowSetLog
operator|.
name|numRows
argument_list|()
operator|==
literal|0
operator|)
assert|;
block|}
finally|finally
block|{
comment|// Restore everything to default setup to avoid discrepancy between junit test runs
name|String
name|queryString2
init|=
literal|"set hive.server2.logging.operation.level=verbose"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogCleanup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Verify cleanup functionality.
comment|// Open a new session, since this case needs to close the session in the end.
name|SessionHandle
name|sessionHandleCleanup
init|=
name|setupSession
argument_list|()
decl_stmt|;
comment|// prepare
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandleCleanup
argument_list|,
name|sql
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSetLog
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
name|File
name|sessionLogDir
init|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LOG_LOCATION
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|sessionHandleCleanup
operator|.
name|getHandleIdentifier
argument_list|()
argument_list|)
decl_stmt|;
name|File
name|operationLogFile
init|=
operator|new
name|File
argument_list|(
name|sessionLogDir
argument_list|,
name|operationHandle
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// check whether exception is thrown when fetching log from a closed operation.
name|client
operator|.
name|closeOperation
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Fetch should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid OperationHandle:"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// check whether operation log file is deleted.
if|if
condition|(
name|operationLogFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Operation log file should be deleted."
argument_list|)
expr_stmt|;
block|}
comment|// check whether session log dir is deleted after session is closed.
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandleCleanup
argument_list|)
expr_stmt|;
if|if
condition|(
name|sessionLogDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Session log dir should be deleted."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SessionHandle
name|setupSession
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Open a session
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
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
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Drop the table if it exists
name|queryString
operator|=
literal|"DROP TABLE IF EXISTS "
operator|+
name|tableName
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Create a test table
name|queryString
operator|=
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (key int, value string)"
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Load data
name|queryString
operator|=
literal|"load data local inpath '"
operator|+
name|dataFile
operator|+
literal|"' into table "
operator|+
name|tableName
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Precondition check: verify whether the table is created and data is fetched correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sql
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetResult
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|500
argument_list|,
name|rowSetResult
operator|.
name|numRows
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|238
argument_list|,
name|rowSetResult
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"val_238"
argument_list|,
name|rowSetResult
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
block|}
specifier|private
name|String
name|verifyFetchedLogPre
parameter_list|(
name|RowSet
name|rowSet
parameter_list|,
name|String
index|[]
name|el
parameter_list|)
block|{
name|StringBuilder
name|stringBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
index|[]
name|row
range|:
name|rowSet
control|)
block|{
name|stringBuilder
operator|.
name|append
argument_list|(
name|row
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|stringBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|protected
name|void
name|verifyFetchedLog
parameter_list|(
name|RowSet
name|rowSet
parameter_list|,
name|String
index|[]
name|el
parameter_list|)
block|{
name|String
name|logs
init|=
name|verifyFetchedLogPre
argument_list|(
name|rowSet
argument_list|,
name|el
argument_list|)
decl_stmt|;
name|verifyFetchedLogPost
argument_list|(
name|logs
argument_list|,
name|el
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyMissingContentsInFetchedLog
parameter_list|(
name|RowSet
name|rowSet
parameter_list|,
name|String
index|[]
name|el
parameter_list|)
block|{
name|String
name|logs
init|=
name|verifyFetchedLogPre
argument_list|(
name|rowSet
argument_list|,
name|el
argument_list|)
decl_stmt|;
name|verifyFetchedLogPost
argument_list|(
name|logs
argument_list|,
name|el
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|verifyFetchedLogPost
parameter_list|(
name|String
name|logs
parameter_list|,
name|String
index|[]
name|el
parameter_list|,
name|boolean
name|contains
parameter_list|)
block|{
for|for
control|(
name|String
name|log
range|:
name|el
control|)
block|{
if|if
condition|(
name|contains
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Checking for presence of "
operator|+
name|log
argument_list|,
name|logs
operator|.
name|contains
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Checking for absence of "
operator|+
name|log
argument_list|,
name|logs
operator|.
name|contains
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

