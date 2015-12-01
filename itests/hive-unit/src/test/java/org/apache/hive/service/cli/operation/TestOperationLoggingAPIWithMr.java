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
name|operation
package|;
end_package

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
name|RowSet
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

begin_comment
comment|/**  * TestOperationLoggingAPIWithMr  * Test the FetchResults of TFetchType.LOG in thrift level in MR mode.  */
end_comment

begin_class
specifier|public
class|class
name|TestOperationLoggingAPIWithMr
extends|extends
name|OperationLoggingAPITestBase
block|{
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
name|tableName
operator|=
literal|"testOperationLoggingAPIWithMr_table"
expr_stmt|;
name|expectedLogsVerbose
operator|=
operator|new
name|String
index|[]
block|{
literal|"Parsing command"
block|,
literal|"Parse Completed"
block|,
literal|"Starting Semantic Analysis"
block|,     }
expr_stmt|;
name|expectedLogsExecution
operator|=
operator|new
name|String
index|[]
block|{
literal|"Total jobs"
block|,
literal|"Starting command"
block|,
literal|"Semantic Analysis Completed"
block|,
literal|"Number of reduce tasks determined at compile time"
block|,
literal|"number of splits"
block|,
literal|"Submitting tokens for job"
block|,
literal|"Ended Job"
block|}
expr_stmt|;
name|expectedLogsPerformance
operator|=
operator|new
name|String
index|[]
block|{
literal|"<PERFLOG method=compile from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"<PERFLOG method=parse from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"<PERFLOG method=Driver.run from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"<PERFLOG method=runTasks from=org.apache.hadoop.hive.ql.Driver>"
block|}
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
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
operator|.
name|varname
argument_list|,
literal|"verbose"
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|confOverlay
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLog
parameter_list|()
throws|throws
name|Exception
block|{
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogAsync
parameter_list|()
throws|throws
name|Exception
block|{
comment|// verify whether the sql operation log is generated and fetch correctly in async mode.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatementAsync
argument_list|(
name|sessionHandle
argument_list|,
name|sql
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Poll on the operation status till the query is completed
name|boolean
name|isQueryRunning
init|=
literal|true
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
name|OperationStatus
name|opStatus
decl_stmt|;
name|OperationState
name|state
init|=
literal|null
decl_stmt|;
name|RowSet
name|rowSetAccumulated
init|=
literal|null
decl_stmt|;
name|StringBuilder
name|logs
init|=
operator|new
name|StringBuilder
argument_list|()
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
break|break;
block|}
name|opStatus
operator|=
name|client
operator|.
name|getOperationStatus
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|opStatus
argument_list|)
expr_stmt|;
name|state
operator|=
name|opStatus
operator|.
name|getState
argument_list|()
expr_stmt|;
name|rowSetAccumulated
operator|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_NEXT
argument_list|,
literal|2000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
index|[]
name|row
range|:
name|rowSetAccumulated
control|)
block|{
name|logs
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
if|if
condition|(
name|state
operator|==
name|OperationState
operator|.
name|CANCELED
operator|||
name|state
operator|==
name|OperationState
operator|.
name|CLOSED
operator|||
name|state
operator|==
name|OperationState
operator|.
name|FINISHED
operator|||
name|state
operator|==
name|OperationState
operator|.
name|ERROR
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
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// The sql should be completed now.
name|Assert
operator|.
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
comment|// Verify the accumulated logs
name|verifyFetchedLogPost
argument_list|(
name|logs
operator|.
name|toString
argument_list|()
argument_list|,
name|expectedLogsVerbose
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Verify the fetched logs from the beginning of the log file
name|RowSet
name|rowSet
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
literal|2000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
name|verifyFetchedLog
argument_list|(
name|rowSet
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFetchResultsOfLogWithOrientation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// (FETCH_FIRST) execute a sql, and fetch its sql operation log as expected value
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
name|int
name|expectedLogLength
init|=
name|rowSetLog
operator|.
name|numRows
argument_list|()
decl_stmt|;
comment|// (FETCH_NEXT) execute the same sql again,
comment|// and fetch the sql operation log with FETCH_NEXT orientation
name|OperationHandle
name|operationHandleWithOrientation
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
name|rowSetLogWithOrientation
decl_stmt|;
name|int
name|logLength
init|=
literal|0
decl_stmt|;
name|int
name|maxRows
init|=
name|calculateProperMaxRows
argument_list|(
name|expectedLogLength
argument_list|)
decl_stmt|;
do|do
block|{
name|rowSetLogWithOrientation
operator|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandleWithOrientation
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_NEXT
argument_list|,
name|maxRows
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
expr_stmt|;
name|logLength
operator|+=
name|rowSetLogWithOrientation
operator|.
name|numRows
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|rowSetLogWithOrientation
operator|.
name|numRows
argument_list|()
operator|==
name|maxRows
condition|)
do|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedLogLength
argument_list|,
name|logLength
argument_list|)
expr_stmt|;
comment|// (FETCH_FIRST) fetch again from the same operation handle with FETCH_FIRST orientation
name|rowSetLogWithOrientation
operator|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandleWithOrientation
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
name|verifyFetchedLog
argument_list|(
name|rowSetLogWithOrientation
argument_list|,
name|expectedLogsVerbose
argument_list|)
expr_stmt|;
block|}
comment|// Since the log length of the sql operation may vary during HIVE dev, calculate a proper maxRows.
specifier|private
name|int
name|calculateProperMaxRows
parameter_list|(
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<
literal|10
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|len
operator|<
literal|100
condition|)
block|{
return|return
literal|10
return|;
block|}
else|else
block|{
return|return
literal|100
return|;
block|}
block|}
block|}
end_class

end_unit

