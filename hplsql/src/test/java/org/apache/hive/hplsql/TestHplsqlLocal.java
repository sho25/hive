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
name|hplsql
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
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
name|io
operator|.
name|FileUtils
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
name|Test
import|;
end_import

begin_comment
comment|/**  * Unit tests for HPL/SQL (no Hive connection required)  */
end_comment

begin_class
specifier|public
class|class
name|TestHplsqlLocal
block|{
specifier|private
specifier|final
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"add"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAssign
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"assign"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBoolExpr
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"bool_expr"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBreak
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"break"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCase
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"case"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCast
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"cast"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChar
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"char"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCoalesce
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"coalesce"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcat
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"concat"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateFunction
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"create_function"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateFunction2
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"create_function2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateProcedure
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"create_procedure"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDate
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"date"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDbmsOutput
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"dbms_output"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeclare
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"declare"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeclareCondition
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"declare_condition"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeclareCondition2
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"declare_condition2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecode
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"decode"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEqual
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"equal"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testException
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"exception"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExit
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"exit"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpr
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"expr"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testForRange
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"for_range"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIf
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"if"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInstr
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"instr"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInterval
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"interval"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLang
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"lang"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLeave
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"leave"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLength
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"length"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLen
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"len"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLower
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"lower"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNvl
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"nvl"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNvl2
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"nvl2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrint
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"print"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturn
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"return"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetError
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"seterror"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSub
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"sub"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubstring
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"substring"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubstr
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"substr"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampIso
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"timestamp_iso"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"timestamp"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToChar
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"to_char"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"to_timestamp"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrim
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"trim"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTwoPipes
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"twopipes"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpper
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"upper"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testValuesInto
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"values_into"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWhile
parameter_list|()
throws|throws
name|Exception
block|{
name|run
argument_list|(
literal|"while"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run a test file    */
name|void
name|run
parameter_list|(
name|String
name|testFile
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|Exec
name|exec
init|=
operator|new
name|Exec
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-f"
block|,
literal|"src/test/queries/local/"
operator|+
name|testFile
operator|+
literal|".sql"
block|,
literal|"-trace"
block|}
decl_stmt|;
name|exec
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|String
name|s
init|=
name|getTestOutput
argument_list|(
name|out
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|FileUtils
operator|.
name|writeStringToFile
argument_list|(
operator|new
name|java
operator|.
name|io
operator|.
name|File
argument_list|(
literal|"target/tmp/log/"
operator|+
name|testFile
operator|+
literal|".out.txt"
argument_list|)
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|String
name|t
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
operator|new
name|java
operator|.
name|io
operator|.
name|File
argument_list|(
literal|"src/test/results/local/"
operator|+
name|testFile
operator|+
literal|".out.txt"
argument_list|)
argument_list|,
literal|"utf-8"
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|s
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get test output    */
name|String
name|getTestOutput
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|Exception
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|line
operator|.
name|startsWith
argument_list|(
literal|"log4j:"
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

