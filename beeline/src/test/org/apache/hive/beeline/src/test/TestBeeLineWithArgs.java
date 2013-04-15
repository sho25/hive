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
operator|.
name|src
operator|.
name|test
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
name|io
operator|.
name|IOException
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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|parse
operator|.
name|SemanticException
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
name|beeline
operator|.
name|BeeLine
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
import|;
end_import

begin_comment
comment|/**  * TestBeeLineWithArgs - executes tests of the command-line arguments to BeeLine  *  */
end_comment

begin_comment
comment|//public class TestBeeLineWithArgs extends TestCase {
end_comment

begin_class
specifier|public
class|class
name|TestBeeLineWithArgs
block|{
comment|// Default location of HiveServer2
specifier|final
specifier|static
name|String
name|JDBC_URL
init|=
name|BeeLine
operator|.
name|BEELINE_DEFAULT_JDBC_URL
operator|+
literal|"localhost:10000"
decl_stmt|;
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
decl_stmt|;
comment|/**    * Start up a local Hive Server 2 for these tests    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|preTests
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|//  hiveConf.logVars(System.err);
comment|// System.err.flush();
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Starting HiveServer2..."
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shut down a local Hive Server 2 for these tests    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|postTests
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|hiveServer2
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Stopping HiveServer2..."
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
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
block|}
block|}
comment|/**    * Execute a script with "beeline -f"    * @param scriptFileName The name of the script to execute    * @throws Any exception while executing    * @return The stderr and stdout from running the script    */
specifier|private
name|String
name|testCommandLineScript
parameter_list|(
name|String
name|scriptFileName
parameter_list|)
throws|throws
name|Throwable
block|{
name|String
index|[]
name|args
init|=
block|{
literal|"-d"
block|,
name|BeeLine
operator|.
name|BEELINE_DEFAULT_JDBC_DRIVER
block|,
literal|"-u"
block|,
name|JDBC_URL
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
decl_stmt|;
name|BeeLine
name|beeLine
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|beelineOutputStream
init|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
decl_stmt|;
name|beeLine
operator|.
name|setOutputStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setErrorStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|begin
argument_list|(
name|args
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|os
operator|.
name|toString
argument_list|(
literal|"UTF8"
argument_list|)
decl_stmt|;
return|return
name|output
return|;
block|}
comment|/**    * Attempt to execute a simple script file with the -f option to BeeLine    * Test for presence of an expected pattern    * in the output (stdout or stderr), fail if not found    * Print PASSED or FAILED    * @paramm testName Name of test to print    * @param expecttedPattern Text to look for in command output    * @param shouldMatch true if the pattern should be found, false if it should not    * @throws Exception on command execution error    */
specifier|private
name|void
name|testScriptFile
parameter_list|(
name|String
name|testName
parameter_list|,
name|String
name|scriptText
parameter_list|,
name|String
name|expectedPattern
parameter_list|,
name|boolean
name|shouldMatch
parameter_list|)
throws|throws
name|Throwable
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> STARTED "
operator|+
name|testName
argument_list|)
expr_stmt|;
comment|// Put the script content in a temp file
name|File
name|scriptFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
name|testName
argument_list|,
literal|"temp"
argument_list|)
decl_stmt|;
name|scriptFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|PrintStream
name|os
init|=
operator|new
name|PrintStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|scriptFile
argument_list|)
argument_list|)
decl_stmt|;
name|os
operator|.
name|print
argument_list|(
name|scriptText
argument_list|)
expr_stmt|;
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|shouldMatch
condition|)
block|{
try|try
block|{
name|String
name|output
init|=
name|testCommandLineScript
argument_list|(
name|scriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|elapsedTime
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
decl_stmt|;
name|String
name|time
init|=
literal|"("
operator|+
name|elapsedTime
operator|+
literal|"s)"
decl_stmt|;
if|if
condition|(
name|output
operator|.
name|contains
argument_list|(
name|expectedPattern
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> PASSED "
operator|+
name|testName
operator|+
literal|" "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Output: "
operator|+
name|output
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|">>> FAILED "
operator|+
name|testName
operator|+
literal|" (ERROR) "
operator|+
name|time
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|testName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
else|else
block|{
try|try
block|{
name|String
name|output
init|=
name|testCommandLineScript
argument_list|(
name|scriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|elapsedTime
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
decl_stmt|;
name|String
name|time
init|=
literal|"("
operator|+
name|elapsedTime
operator|+
literal|"s)"
decl_stmt|;
if|if
condition|(
name|output
operator|.
name|contains
argument_list|(
name|expectedPattern
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Output: "
operator|+
name|output
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|">>> FAILED "
operator|+
name|testName
operator|+
literal|" (ERROR) "
operator|+
name|time
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|testName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> PASSED "
operator|+
name|testName
operator|+
literal|" "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
name|scriptFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * Attempt to execute a simple script file with the -f option to BeeLine    * Test for presence of an expected pattern    * in the output (stdout or stderr), fail if not found    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testPositiveScriptFile
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testPositiveScriptFile"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"show databases;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|" default "
decl_stmt|;
name|testScriptFile
argument_list|(
name|TEST_NAME
argument_list|,
name|SCRIPT_TEXT
argument_list|,
name|EXPECTED_PATTERN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Attempt to execute a simple script file with the -f option to BeeLine    * The first command should fail and the second command should not execute    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testBreakOnErrorScriptFile
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testBreakOnErrorScriptFile"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"select * from abcdefg01;\nshow databases;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|" default "
decl_stmt|;
name|testScriptFile
argument_list|(
name|TEST_NAME
argument_list|,
name|SCRIPT_TEXT
argument_list|,
name|EXPECTED_PATTERN
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Attempt to execute a missing script file with the -f option to BeeLine    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testNegativeScriptFile
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testNegativeScriptFile"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|" default "
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> STARTED "
operator|+
name|TEST_NAME
argument_list|)
expr_stmt|;
comment|// Create and delete a temp file
name|File
name|scriptFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"beelinenegative"
argument_list|,
literal|"temp"
argument_list|)
decl_stmt|;
name|scriptFile
operator|.
name|delete
argument_list|()
expr_stmt|;
try|try
block|{
name|String
name|output
init|=
name|testCommandLineScript
argument_list|(
name|scriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|elapsedTime
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
decl_stmt|;
name|String
name|time
init|=
literal|"("
operator|+
name|elapsedTime
operator|+
literal|"s)"
decl_stmt|;
if|if
condition|(
name|output
operator|.
name|contains
argument_list|(
name|EXPECTED_PATTERN
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Output: "
operator|+
name|output
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|">>> FAILED "
operator|+
name|TEST_NAME
operator|+
literal|" (ERROR) "
operator|+
name|time
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|TEST_NAME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> PASSED "
operator|+
name|TEST_NAME
operator|+
literal|" "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

