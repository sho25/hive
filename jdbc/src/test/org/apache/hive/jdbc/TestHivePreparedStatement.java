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
name|jdbc
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
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
operator|.
name|Iface
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
name|TExecuteStatementReq
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
name|TExecuteStatementResp
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
name|TGetOperationStatusReq
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
name|TGetOperationStatusResp
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
name|TOperationHandle
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
name|TOperationState
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
name|TSessionHandle
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
name|TStatus
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
name|TStatusCode
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
name|TCloseOperationResp
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
name|TCloseOperationReq
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
name|org
operator|.
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|MockitoAnnotations
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_class
specifier|public
class|class
name|TestHivePreparedStatement
block|{
annotation|@
name|Mock
specifier|private
name|HiveConnection
name|connection
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Iface
name|client
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TSessionHandle
name|sessHandle
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TExecuteStatementResp
name|tExecStatementResp
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TGetOperationStatusResp
name|tGetOperationStatusResp
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TCloseOperationResp
name|tCloseOperationResp
decl_stmt|;
specifier|private
name|TStatus
name|tStatusSuccess
init|=
operator|new
name|TStatus
argument_list|(
name|TStatusCode
operator|.
name|SUCCESS_STATUS
argument_list|)
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|TOperationHandle
name|tOperationHandle
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|MockitoAnnotations
operator|.
name|initMocks
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tExecStatementResp
operator|.
name|getStatus
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tStatusSuccess
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tExecStatementResp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tOperationHandle
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tGetOperationStatusResp
operator|.
name|getStatus
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tStatusSuccess
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tGetOperationStatusResp
operator|.
name|getOperationState
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TOperationState
operator|.
name|FINISHED_STATE
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tGetOperationStatusResp
operator|.
name|isSetOperationState
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tGetOperationStatusResp
operator|.
name|isSetOperationCompleted
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|tCloseOperationResp
operator|.
name|getStatus
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tStatusSuccess
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|GetOperationStatus
argument_list|(
name|any
argument_list|(
name|TGetOperationStatusReq
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tGetOperationStatusResp
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|CloseOperation
argument_list|(
name|any
argument_list|(
name|TCloseOperationReq
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tCloseOperationResp
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|client
operator|.
name|ExecuteStatement
argument_list|(
name|any
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tExecStatementResp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testNonParameterized
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 1"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|unusedArgument
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"asd"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SQLException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|unsetArgument
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1 from x where a=?"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|oneArgument
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1 from x where a=?"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"asd"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 1 from x where a='asd'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|escapingOfStringArgument
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1 from x where a=?"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"a'\"d"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 1 from x where a='a\\'\"d'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|pastingIntoQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1 from x where a='e' || ?"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"v"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 1 from x where a='e' || 'v'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// HIVE-13625
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|pastingIntoEscapedQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select 1 from x where a='\\044e' || ?"
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"v"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 1 from x where a='\\044e' || 'v'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleQuoteSetString
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select * from table where value=?"
decl_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"anyValue\\' or 1=1 --"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select * from table where value='anyValue\\' or 1=1 --'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
name|ps
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
literal|"anyValue\\\\' or 1=1 --"
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select * from table where value='anyValue\\\\\\' or 1=1 --'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleQuoteSetBinaryStream
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sql
init|=
literal|"select * from table where value=?"
decl_stmt|;
name|ArgumentCaptor
argument_list|<
name|TExecuteStatementReq
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|TExecuteStatementReq
operator|.
name|class
argument_list|)
decl_stmt|;
name|HivePreparedStatement
name|ps
init|=
operator|new
name|HivePreparedStatement
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|ps
operator|.
name|setBinaryStream
argument_list|(
literal|1
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
literal|"'anyValue' or 1=1"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select * from table where value='\\'anyValue\\' or 1=1'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
name|ps
operator|.
name|setBinaryStream
argument_list|(
literal|1
argument_list|,
operator|new
name|ByteArrayInputStream
argument_list|(
literal|"\\'anyValue\\' or 1=1"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|client
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|ExecuteStatement
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select * from table where value='\\'anyValue\\' or 1=1'"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
operator|.
name|getStatement
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

