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
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
operator|.
name|removeComments
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|TestHiveStringUtils
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSplitAndUnEscape
parameter_list|()
throws|throws
name|Exception
block|{
name|splitAndUnEscapeTestCase
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|splitAndUnEscapeTestCase
argument_list|(
literal|"'single element'"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"'single element'"
block|}
argument_list|)
expr_stmt|;
name|splitAndUnEscapeTestCase
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.S"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"yyyy-MM-dd'T'HH:mm:ss"
block|,
literal|"yyyy-MM-dd'T'HH:mm:ss.S"
block|}
argument_list|)
expr_stmt|;
name|splitAndUnEscapeTestCase
argument_list|(
literal|"single\\,element"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"single,element"
block|}
argument_list|)
expr_stmt|;
name|splitAndUnEscapeTestCase
argument_list|(
literal|"element\\,one\\\\,element\\\\two\\\\\\,"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"element,one\\"
block|,
literal|"element\\two\\,"
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|splitAndUnEscapeTestCase
parameter_list|(
name|String
name|testValue
parameter_list|,
name|String
index|[]
name|expectedResults
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|testResults
init|=
name|HiveStringUtils
operator|.
name|splitAndUnEscape
argument_list|(
name|testValue
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|expectedResults
argument_list|)
operator|+
literal|" == "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|testResults
argument_list|)
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|expectedResults
argument_list|,
name|testResults
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStripComments
parameter_list|()
throws|throws
name|Exception
block|{
name|assertNull
argument_list|(
name|removeComments
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertUnchanged
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertUnchanged
argument_list|(
literal|"select 1"
argument_list|)
expr_stmt|;
name|assertUnchanged
argument_list|(
literal|"insert into foo (values('-----')"
argument_list|)
expr_stmt|;
name|assertUnchanged
argument_list|(
literal|"insert into foo (values('abc\n\'xyz')"
argument_list|)
expr_stmt|;
name|assertUnchanged
argument_list|(
literal|"create database if not exists testDB; set hive.cli.print.current.db=true;use\ntestDB;\nuse default;drop if exists testDB;"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|removeComments
argument_list|(
literal|"foo\n"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|removeComments
argument_list|(
literal|"\nfoo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|removeComments
argument_list|(
literal|"\n\nfoo\n\n"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values('-----')"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\ninsert into foo (values('-----')"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values('----''-')"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\ninsert into foo (values('----''-')"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values(\"----''-\")"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\ninsert into foo (values(\"----''-\")"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values(\"----\"\"-\")"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\ninsert into foo (values(\"----\"\"-\")"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values('-\n--\n--')"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\ninsert into foo (values('-\n--\n--')"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values('-\n--\n--')"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\n\ninsert into foo (values('-\n--\n--')"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values(\"-\n--\n--\")"
argument_list|,
name|removeComments
argument_list|(
literal|"--comment\n\ninsert into foo (values(\"-\n--\n--\")"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values(\"-\n--\n--\")"
argument_list|,
name|removeComments
argument_list|(
literal|"\n\n--comment\n\ninsert into foo (values(\"-\n--\n--\")\n\n"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"insert into foo (values('abc');\ninsert into foo (values('def');"
argument_list|,
name|removeComments
argument_list|(
literal|"insert into foo (values('abc');\n--comment\ninsert into foo (values('def');"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLinesEndingWithComments
parameter_list|()
block|{
name|int
index|[]
name|escape
init|=
block|{
operator|-
literal|1
block|}
decl_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|removeComments
argument_list|(
literal|"show tables;"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|removeComments
argument_list|(
literal|"show tables; --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|removeComments
argument_list|(
literal|"show tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|removeComments
argument_list|(
literal|"show tables; -------comments;one;two;three;;;;"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show"
argument_list|,
name|removeComments
argument_list|(
literal|"show-- tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show"
argument_list|,
name|removeComments
argument_list|(
literal|"show --tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"s"
argument_list|,
name|removeComments
argument_list|(
literal|"s--how --tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|removeComments
argument_list|(
literal|"-- show tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"show tables\""
argument_list|,
name|removeComments
argument_list|(
literal|"\"show tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"show --comments tables\""
argument_list|,
name|removeComments
argument_list|(
literal|"\"show --comments tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"'show --comments' tables\""
argument_list|,
name|removeComments
argument_list|(
literal|"\"'show --comments' tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"'show --comments tables'"
argument_list|,
name|removeComments
argument_list|(
literal|"'show --comments tables' --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"'\"show --comments tables\"'"
argument_list|,
name|removeComments
argument_list|(
literal|"'\"show --comments tables\"' --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * check that statement is unchanged after stripping    */
specifier|private
name|void
name|assertUnchanged
parameter_list|(
name|String
name|statement
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"statement should not have been affected by stripping commnents"
argument_list|,
name|statement
argument_list|,
name|removeComments
argument_list|(
name|statement
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

