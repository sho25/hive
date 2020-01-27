begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertEquals
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
name|TestCommands
block|{
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
comment|/**    * Test the commands directly call from beeline.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testBeelineCommands
parameter_list|()
throws|throws
name|IOException
block|{
comment|// avoid System.exit() call in beeline which causes JVM to exit and fails the test
name|System
operator|.
name|setProperty
argument_list|(
name|BeeLineOpts
operator|.
name|PROPERTY_NAME_EXIT
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
comment|// Verify the command without ';' at the end also works fine
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"jdbc:hive2://"
block|,
literal|"-e"
block|,
literal|"select 3"
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"jdbc:hive2://"
block|,
literal|"-e"
block|,
literal|"create table t1(x int); show tables"
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test {@link Commands#getCmdList(String, boolean)} with various nesting of special characters:    * apostrophe, quotation mark, newline, comment start, semicolon.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testGetCmdList
parameter_list|()
throws|throws
name|Exception
block|{
name|BeeLine
name|beeline
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
name|Commands
name|commands
init|=
operator|new
name|Commands
argument_list|(
name|beeline
argument_list|)
decl_stmt|;
try|try
block|{
comment|// COMMANDS, WHITE SPACES
comment|// trivial
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" ;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|" "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"; "
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" "
argument_list|,
literal|" "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" ; "
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" ; "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" \\; "
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// add whitespace
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" \n select \n 1 \n "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" \n select \n 1 \n ;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// add whitespace after semicolon
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" \n select 1 \n "
argument_list|,
literal|" \n "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" \n select 1 \n ; \n "
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// second command
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|"select 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1;select 2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// second command, no ending semicolon
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|"select 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1;select 2"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// three commands with whitespaces
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|" \n select \t 1"
argument_list|,
literal|"\tselect\n2\r"
argument_list|,
literal|" select\n3"
argument_list|,
literal|"   "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|" \n select \t 1;\tselect\n2\r; select\n3;   "
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// ADD STRINGS
comment|// trivial string
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"foo\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"foo\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo'"
argument_list|,
literal|" select 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo'; select 2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"foo\""
argument_list|,
literal|" select 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"foo\"; select 2"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select ''"
argument_list|,
literal|" select \"\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select ''; select \"\""
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// string containing delimiter of other string
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo\"bar'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo\"bar';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"foo'bar\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"foo'bar\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo\"bar'"
argument_list|,
literal|" select 'foo\"bar'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo\"bar'; select 'foo\"bar';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"foo'bar\""
argument_list|,
literal|" select \"foo'bar\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"foo'bar\"; select \"foo'bar\""
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select '\"' "
argument_list|,
literal|" select \"'\" "
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select '\"' ; select \"'\" ;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// string containing semicolon
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo;bar'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo;bar';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"foo;bar\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"foo;bar\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// two selects of strings vs. one select containing semicolon
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select '\"foobar'"
argument_list|,
literal|" select 'foobar\"'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select '\"foobar'; select 'foobar\"';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"'foobar'; select 'foobar'\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"'foobar'; select 'foobar'\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// newline within strings
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'multi\nline\nstring'"
argument_list|,
literal|" select 'allowed'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'multi\nline\nstring'; select 'allowed';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"multi\nline\nstring\""
argument_list|,
literal|" select \"allowed\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"multi\nline\nstring\"; select \"allowed\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select ';\nselect 1;\n'"
argument_list|,
literal|" select 'sql within string'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select ';\nselect 1;\n'; select 'sql within string';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// escaped quotation marks in strings
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'fo\\'o'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'fo\\'o';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"fo\\\"o\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"fo\\\"o\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'fo\\\"o'"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'fo\\\"o';"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select \"fo\\'o\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select \"fo\\'o\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// strings ending with backslash
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 'foo\\\\'"
argument_list|,
literal|" select \"bar\\\\\""
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 'foo\\\\'; select \"bar\\\\\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// ADD LINE COMMENTS
comment|// line comments
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" -- comment\nselect 2"
argument_list|,
literal|" -- comment\n"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; -- comment\nselect 2; -- comment\n"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select -- comment\n1"
argument_list|,
literal|" select -- comment\n2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select -- comment\n1; select -- comment\n2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select -- comment 1; select -- comment 2;"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select -- comment 1; select -- comment 2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select -- comment\\\n1"
argument_list|,
literal|" select -- comment\\\n2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select -- comment\\\n1; select -- comment\\\n2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// line comments with semicolons
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 -- invalid;\nselect 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 -- invalid;\nselect 2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 -- valid\n"
argument_list|,
literal|"select 2"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 -- valid\n;select 2;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// line comments with quotation marks
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 -- v'lid\n"
argument_list|,
literal|"select 2"
argument_list|,
literal|"select 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 -- v'lid\n;select 2;select 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 -- v\"lid\n"
argument_list|,
literal|"select 2"
argument_list|,
literal|"select 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 -- v\"lid\n;select 2;select 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|"select 1 -- '\n"
argument_list|,
literal|"select \"'\""
argument_list|,
literal|"select 3 -- \"\n"
argument_list|,
literal|"?"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|";select 1 -- '\n;select \"'\";select 3 -- \"\n;?"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|"select 1 -- ';select \"'\"\n"
argument_list|,
literal|"select 3 -- \"\n"
argument_list|,
literal|"?"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|";select 1 -- ';select \"'\"\n;select 3 -- \"\n;?"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// ADD BLOCK COMMENTS
comment|// block comments with semicolons
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" select /* */ 2"
argument_list|,
literal|" select /* */ 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; select /* */ 2; select /* */ 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" select /* ; */ 2"
argument_list|,
literal|" select /* ; */ 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; select /* ; */ 2; select /* ; */ 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 /* c1; */"
argument_list|,
literal|" /**/ select 2 /*/ c3; /*/"
argument_list|,
literal|" select 3"
argument_list|,
literal|" /* c4 */"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 /* c1; */; /**/ select 2 /*/ c3; /*/; select 3; /* c4 */"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// block comments with line comments
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 --lc /* fake bc\n"
argument_list|,
literal|"select 2 --lc */\n"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 --lc /* fake bc\n;select 2 --lc */\n;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 /*bc -- fake lc\n;select 2 --lc */\n"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 /*bc -- fake lc\n;select 2 --lc */\n;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// block comments with quotation marks
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 /* v'lid */"
argument_list|,
literal|"select 2"
argument_list|,
literal|"select 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 /* v'lid */;select 2;select 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1 /* v\"lid */"
argument_list|,
literal|"select 2"
argument_list|,
literal|"select 3"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1 /* v\"lid */;select 2;select 3;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|"select 1 /* ' */"
argument_list|,
literal|"select \"'\""
argument_list|,
literal|"select 3 /* \" */"
argument_list|,
literal|"?"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|";select 1 /* ' */;select \"'\";select 3 /* \" */;?"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|""
argument_list|,
literal|"select 1 /*/ ' ;select \"'\" /*/"
argument_list|,
literal|"select 3 /* \" */"
argument_list|,
literal|"?"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|";select 1 /*/ ' ;select \"'\" /*/;select 3 /* \" */;?"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// UNTERMINATED STRING, COMMENT
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" -- ;\\';\\\";--;  ;/*;*/; '; ';\";\";"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; -- ;\\';\\\";--;  ;/*;*/; '; ';\";\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" /* ;\\';\\\";--;\n;/*;  ; '; ';\";\";"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; /* ;\\';\\\";--;\n;/*;  ; '; ';\";\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" '  ;\\';\\\";--;\n;/*;*/;  ;  ;\";\";"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; '  ;\\';\\\";--;\n;/*;*/;  ;  ;\";\";"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"select 1"
argument_list|,
literal|" \" ;\\';\\\";--;\n;/*;*/; '; ';  ;  ;"
argument_list|)
argument_list|,
name|commands
operator|.
name|getCmdList
argument_list|(
literal|"select 1; \" ;\\';\\\";--;\n;/*;*/; '; ';  ;  ;"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|beeline
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

