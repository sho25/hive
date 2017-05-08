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
name|session
package|;
end_package

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
name|ql
operator|.
name|QueryDisplay
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
name|ql
operator|.
name|QueryInfo
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
name|ql
operator|.
name|plan
operator|.
name|api
operator|.
name|StageType
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|hive
operator|.
name|tmpl
operator|.
name|QueryProfileTmpl
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
name|Test
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

begin_comment
comment|/**  * Test QueryDisplay and its consumers like WebUI.  */
end_comment

begin_class
specifier|public
class|class
name|TestQueryDisplay
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|SessionManager
name|sessionManager
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|HiveServer2
name|dummyHs2
init|=
operator|new
name|HiveServer2
argument_list|()
decl_stmt|;
name|sessionManager
operator|=
operator|new
name|SessionManager
argument_list|(
name|dummyHs2
argument_list|)
expr_stmt|;
name|sessionManager
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test if query display captures information on current/historic SQL operations.    */
annotation|@
name|Test
specifier|public
name|void
name|testQueryDisplay
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveSession
name|session
init|=
name|sessionManager
operator|.
name|createSession
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V8
argument_list|)
argument_list|,
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V8
argument_list|,
literal|"testuser"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|OperationHandle
name|opHandle1
init|=
name|session
operator|.
name|executeStatement
argument_list|(
literal|"show databases"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|OperationHandle
name|opHandle2
init|=
name|session
operator|.
name|executeStatement
argument_list|(
literal|"show tables"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|QueryInfo
argument_list|>
name|liveSqlOperations
decl_stmt|,
name|historicSqlOperations
decl_stmt|;
name|liveSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getLiveQueryInfos
argument_list|()
expr_stmt|;
name|historicSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getHistoricalQueryInfos
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|liveSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|historicSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|liveSqlOperations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"show databases"
argument_list|,
name|opHandle1
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|liveSqlOperations
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"show tables"
argument_list|,
name|opHandle2
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|session
operator|.
name|closeOperation
argument_list|(
name|opHandle1
argument_list|)
expr_stmt|;
name|liveSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getLiveQueryInfos
argument_list|()
expr_stmt|;
name|historicSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getHistoricalQueryInfos
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|liveSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|historicSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|historicSqlOperations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"show databases"
argument_list|,
name|opHandle1
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|liveSqlOperations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"show tables"
argument_list|,
name|opHandle2
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|session
operator|.
name|closeOperation
argument_list|(
name|opHandle2
argument_list|)
expr_stmt|;
name|liveSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getLiveQueryInfos
argument_list|()
expr_stmt|;
name|historicSqlOperations
operator|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getHistoricalQueryInfos
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|liveSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|historicSqlOperations
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|historicSqlOperations
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"show databases"
argument_list|,
name|opHandle1
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyDDL
argument_list|(
name|historicSqlOperations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"show tables"
argument_list|,
name|opHandle2
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|session
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test if webui captures information on current/historic SQL operations.    */
annotation|@
name|Test
specifier|public
name|void
name|testWebUI
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveSession
name|session
init|=
name|sessionManager
operator|.
name|createSession
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V8
argument_list|)
argument_list|,
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V8
argument_list|,
literal|"testuser"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|OperationHandle
name|opHandle1
init|=
name|session
operator|.
name|executeStatement
argument_list|(
literal|"show databases"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|OperationHandle
name|opHandle2
init|=
name|session
operator|.
name|executeStatement
argument_list|(
literal|"show tables"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|verifyDDLHtml
argument_list|(
literal|"show databases"
argument_list|,
name|opHandle1
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|verifyDDLHtml
argument_list|(
literal|"show tables"
argument_list|,
name|opHandle2
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|session
operator|.
name|closeOperation
argument_list|(
name|opHandle1
argument_list|)
expr_stmt|;
name|session
operator|.
name|closeOperation
argument_list|(
name|opHandle2
argument_list|)
expr_stmt|;
name|verifyDDLHtml
argument_list|(
literal|"show databases"
argument_list|,
name|opHandle1
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|verifyDDLHtml
argument_list|(
literal|"show tables"
argument_list|,
name|opHandle2
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|session
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|verifyDDL
parameter_list|(
name|QueryInfo
name|queryInfo
parameter_list|,
name|String
name|stmt
parameter_list|,
name|String
name|handle
parameter_list|,
name|boolean
name|finished
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|queryInfo
operator|.
name|getUserName
argument_list|()
argument_list|,
literal|"testuser"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|queryInfo
operator|.
name|getExecutionEngine
argument_list|()
argument_list|,
literal|"mr"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|queryInfo
operator|.
name|getOperationId
argument_list|()
argument_list|,
name|handle
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|queryInfo
operator|.
name|getBeginTime
argument_list|()
operator|>
literal|0
operator|&&
name|queryInfo
operator|.
name|getBeginTime
argument_list|()
operator|<=
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|finished
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|queryInfo
operator|.
name|getEndTime
argument_list|()
operator|>
literal|0
operator|&&
name|queryInfo
operator|.
name|getEndTime
argument_list|()
operator|>=
name|queryInfo
operator|.
name|getBeginTime
argument_list|()
operator|&&
name|queryInfo
operator|.
name|getEndTime
argument_list|()
operator|<=
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|queryInfo
operator|.
name|getRuntime
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|queryInfo
operator|.
name|getEndTime
argument_list|()
argument_list|)
expr_stmt|;
comment|//For runtime, query may have finished.
block|}
name|QueryDisplay
name|qDisplay1
init|=
name|queryInfo
operator|.
name|getQueryDisplay
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|qDisplay1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|qDisplay1
operator|.
name|getQueryString
argument_list|()
argument_list|,
name|stmt
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|qDisplay1
operator|.
name|getExplainPlan
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|qDisplay1
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getHmsTimings
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|COMPILATION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getHmsTimings
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|EXECUTION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getPerfLogStarts
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|COMPILATION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getPerfLogEnds
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|COMPILATION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getPerfLogStarts
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|COMPILATION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|qDisplay1
operator|.
name|getPerfLogEnds
argument_list|(
name|QueryDisplay
operator|.
name|Phase
operator|.
name|COMPILATION
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|qDisplay1
operator|.
name|getTaskDisplays
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|QueryDisplay
operator|.
name|TaskDisplay
name|tInfo1
init|=
name|qDisplay1
operator|.
name|getTaskDisplays
argument_list|()
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
name|tInfo1
operator|.
name|getTaskId
argument_list|()
argument_list|,
literal|"Stage-0"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tInfo1
operator|.
name|getTaskType
argument_list|()
argument_list|,
name|StageType
operator|.
name|DDL
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tInfo1
operator|.
name|getBeginTime
argument_list|()
operator|>
literal|0
operator|&&
name|tInfo1
operator|.
name|getBeginTime
argument_list|()
operator|<=
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tInfo1
operator|.
name|getEndTime
argument_list|()
operator|>
literal|0
operator|&&
name|tInfo1
operator|.
name|getEndTime
argument_list|()
operator|>=
name|tInfo1
operator|.
name|getBeginTime
argument_list|()
operator|&&
name|tInfo1
operator|.
name|getEndTime
argument_list|()
operator|<=
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tInfo1
operator|.
name|getStatus
argument_list|()
argument_list|,
literal|"Success, ReturnVal 0"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sanity check if basic information is delivered in this html.  Let's not go too crazy and    * assert each element, to make it easier to add UI improvements.    */
specifier|private
name|void
name|verifyDDLHtml
parameter_list|(
name|String
name|stmt
parameter_list|,
name|String
name|opHandle
parameter_list|)
throws|throws
name|Exception
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|QueryInfo
name|queryInfo
init|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getQueryInfo
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
operator|new
name|QueryProfileTmpl
argument_list|()
operator|.
name|render
argument_list|(
name|sw
argument_list|,
name|queryInfo
argument_list|)
expr_stmt|;
name|String
name|html
init|=
name|sw
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|html
operator|.
name|contains
argument_list|(
name|stmt
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|html
operator|.
name|contains
argument_list|(
literal|"testuser"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

