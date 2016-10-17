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
name|beeline
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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

begin_comment
comment|/**  * TestBeeLineHistory - executes tests of the !history command of BeeLine  */
end_comment

begin_class
specifier|public
class|class
name|TestBeeLineHistory
block|{
specifier|private
specifier|static
specifier|final
name|String
name|fileName
init|=
literal|"history"
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
name|PrintWriter
name|writer
init|=
operator|new
name|PrintWriter
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 1;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 2;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 3;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 4;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 5;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 6;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 7;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 8;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 9;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|println
argument_list|(
literal|"select 10;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNumHistories
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|ops
init|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
decl_stmt|;
name|BeeLine
name|beeline
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
name|beeline
operator|.
name|getOpts
argument_list|()
operator|.
name|setHistoryFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|setOutputStream
argument_list|(
name|ops
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|initializeConsoleReader
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|dispatch
argument_list|(
literal|"!history"
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|os
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|int
name|numHistories
init|=
name|output
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|length
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|numHistories
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHistory
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|ops
init|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
decl_stmt|;
name|BeeLine
name|beeline
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
name|beeline
operator|.
name|getOpts
argument_list|()
operator|.
name|setHistoryFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|setOutputStream
argument_list|(
name|ops
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|initializeConsoleReader
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|dispatch
argument_list|(
literal|"!history"
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|os
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|String
index|[]
name|tmp
init|=
name|output
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tmp
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"1     : select 1;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tmp
index|[
literal|9
index|]
operator|.
name|equals
argument_list|(
literal|"10    : select 10;"
argument_list|)
argument_list|)
expr_stmt|;
name|beeline
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

