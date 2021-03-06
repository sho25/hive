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
name|cli
package|;
end_package

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
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveSQLException
block|{
comment|/**    * Tests the conversion from a regular exception to the TStatus object    */
annotation|@
name|Test
specifier|public
name|void
name|testExceptionToTStatus
parameter_list|()
block|{
name|Exception
name|ex1
init|=
name|createException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createSimpleCause
argument_list|()
argument_list|)
expr_stmt|;
name|TStatus
name|status
init|=
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|ex1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|,
name|status
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ex1
operator|.
name|getMessage
argument_list|()
argument_list|,
name|status
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
argument_list|,
name|status
operator|.
name|getInfoMessages
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion from a HiveSQLException exception to the TStatus object    */
annotation|@
name|Test
specifier|public
name|void
name|testHiveSQLExceptionToTStatus
parameter_list|()
block|{
name|String
name|expectedMessage
init|=
literal|"reason"
decl_stmt|;
name|String
name|expectedSqlState
init|=
literal|"sqlState"
decl_stmt|;
name|int
name|expectedVendorCode
init|=
literal|10
decl_stmt|;
name|Exception
name|ex1
init|=
operator|new
name|HiveSQLException
argument_list|(
name|expectedMessage
argument_list|,
name|expectedSqlState
argument_list|,
name|expectedVendorCode
argument_list|,
name|createSimpleCause
argument_list|()
argument_list|)
decl_stmt|;
name|TStatus
name|status
init|=
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|ex1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|,
name|status
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedSqlState
argument_list|,
name|status
operator|.
name|getSqlState
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedMessage
argument_list|,
name|status
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
argument_list|,
name|status
operator|.
name|getInfoMessages
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion between the exception text with the simple cause and the    * Throwable object    */
annotation|@
name|Test
specifier|public
name|void
name|testExceptionMarshalling
parameter_list|()
throws|throws
name|Exception
block|{
name|Exception
name|ex1
init|=
name|createException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createSimpleCause
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|ex
init|=
name|HiveSQLException
operator|.
name|toCause
argument_list|(
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception1"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception2"
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion between the exception text with nested cause and    * the Throwable object    */
annotation|@
name|Test
specifier|public
name|void
name|testNestedException
parameter_list|()
block|{
name|Exception
name|ex1
init|=
name|createException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createNestedCause
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|ex
init|=
name|HiveSQLException
operator|.
name|toCause
argument_list|(
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception1"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception2"
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|Exception
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception3"
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion of the exception with unknown source    */
annotation|@
name|Test
specifier|public
name|void
name|testExceptionWithUnknownSource
parameter_list|()
block|{
name|Exception
name|ex1
init|=
name|createException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createSimpleCause
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|details
init|=
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
decl_stmt|;
comment|// Simulate the unknown source
name|String
index|[]
name|tokens
init|=
name|details
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|tokens
index|[
literal|2
index|]
operator|=
literal|null
expr_stmt|;
name|tokens
index|[
literal|3
index|]
operator|=
literal|"-1"
expr_stmt|;
name|details
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
name|tokens
argument_list|,
literal|":"
argument_list|)
argument_list|)
expr_stmt|;
name|Throwable
name|ex
init|=
name|HiveSQLException
operator|.
name|toCause
argument_list|(
name|details
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception1"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception2"
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion of the exception that the class type of one of the causes    * doesn't exist. The stack trace text is generated on the server and passed to JDBC    * client. It's possible that some cause types don't exist on the client and HiveSQLException    * can't convert them and use RunTimeException instead.    */
annotation|@
name|Test
specifier|public
name|void
name|testExceptionWithMissingTypeOnClient
parameter_list|()
block|{
name|Exception
name|ex1
init|=
operator|new
name|UnsupportedOperationException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createSimpleCause
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|details
init|=
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
decl_stmt|;
comment|// Simulate an unknown type
name|String
index|[]
name|tokens
init|=
name|details
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|tokens
index|[
literal|0
index|]
operator|=
literal|"*DummyException"
expr_stmt|;
name|details
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
name|tokens
argument_list|,
literal|":"
argument_list|)
argument_list|)
expr_stmt|;
name|Throwable
name|ex
init|=
name|HiveSQLException
operator|.
name|toCause
argument_list|(
name|details
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests the conversion of the exception from anonymous class    */
annotation|@
name|Test
specifier|public
name|void
name|testExceptionFromAnonymousClass
parameter_list|()
block|{
name|Dummy
name|d
init|=
operator|new
name|Dummy
argument_list|()
block|{
specifier|public
name|void
name|testExceptionConversion
parameter_list|()
block|{
name|Exception
name|ex1
init|=
name|createException
argument_list|()
decl_stmt|;
name|ex1
operator|.
name|initCause
argument_list|(
name|createSimpleCause
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|ex
init|=
name|HiveSQLException
operator|.
name|toCause
argument_list|(
name|HiveSQLException
operator|.
name|toString
argument_list|(
name|ex1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception1"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertSame
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"exception2"
argument_list|,
name|ex
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|d
operator|.
name|testExceptionConversion
argument_list|()
expr_stmt|;
block|}
interface|interface
name|Dummy
block|{
name|void
name|testExceptionConversion
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|static
name|Exception
name|createException
parameter_list|()
block|{
return|return
operator|new
name|RuntimeException
argument_list|(
literal|"exception1"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Exception
name|createSimpleCause
parameter_list|()
block|{
return|return
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"exception2"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Exception
name|createNestedCause
parameter_list|()
block|{
return|return
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"exception2"
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"exception3"
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

