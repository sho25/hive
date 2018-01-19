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
name|hadoop
operator|.
name|hive
operator|.
name|cli
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyBoolean
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
name|anyInt
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
name|anyString
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
name|eq
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
name|doThrow
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
name|mock
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
name|never
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
name|FileWriter
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Permission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Collections
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
name|jline
operator|.
name|console
operator|.
name|ConsoleReader
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|ArgumentCompleter
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|Completer
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
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|Schema
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
name|CommandNeedRetryException
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
name|IDriver
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|util
operator|.
name|Shell
import|;
end_import

begin_comment
comment|// Cannot call class TestCliDriver since that's the name of the generated
end_comment

begin_comment
comment|// code for the script-based testing
end_comment

begin_class
specifier|public
class|class
name|TestCliDriverMethods
extends|extends
name|TestCase
block|{
name|SecurityManager
name|securityManager
decl_stmt|;
comment|// Some of these tests require intercepting System.exit() using the SecurityManager.
comment|// It is safer to  register/unregister our SecurityManager during setup/teardown instead
comment|// of doing it within the individual test cases.
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|securityManager
operator|=
name|System
operator|.
name|getSecurityManager
argument_list|()
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|NoExitSecurityManager
argument_list|(
name|securityManager
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|System
operator|.
name|setSecurityManager
argument_list|(
name|securityManager
argument_list|)
expr_stmt|;
block|}
comment|// If the command has an associated schema, make sure it gets printed to use
specifier|public
name|void
name|testThatCliDriverPrintsHeaderForCommandsWithSchema
parameter_list|()
throws|throws
name|CommandNeedRetryException
block|{
name|Schema
name|mockSchema
init|=
name|mock
argument_list|(
name|Schema
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
literal|"FlightOfTheConchords"
decl_stmt|;
name|fieldSchemas
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|fieldName
argument_list|,
literal|"type"
argument_list|,
literal|"comment"
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSchema
operator|.
name|getFieldSchemas
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fieldSchemas
argument_list|)
expr_stmt|;
name|PrintStream
name|mockOut
init|=
name|headerPrintingTestDriver
argument_list|(
name|mockSchema
argument_list|)
decl_stmt|;
comment|// Should have printed out the header for the field schema
name|verify
argument_list|(
name|mockOut
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|print
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
comment|// If the command has no schema, make sure nothing is printed
specifier|public
name|void
name|testThatCliDriverPrintsNoHeaderForCommandsWithNoSchema
parameter_list|()
throws|throws
name|CommandNeedRetryException
block|{
name|Schema
name|mockSchema
init|=
name|mock
argument_list|(
name|Schema
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockSchema
operator|.
name|getFieldSchemas
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|PrintStream
name|mockOut
init|=
name|headerPrintingTestDriver
argument_list|(
name|mockSchema
argument_list|)
decl_stmt|;
comment|// Should not have tried to print any thing.
name|verify
argument_list|(
name|mockOut
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|print
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Test that CliDriver does not strip comments starting with '--'
specifier|public
name|void
name|testThatCliDriverDoesNotStripComments
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We need to overwrite System.out and System.err as that is what is used in ShellCmdExecutor
comment|// So save old values...
name|PrintStream
name|oldOut
init|=
name|System
operator|.
name|out
decl_stmt|;
name|PrintStream
name|oldErr
init|=
name|System
operator|.
name|err
decl_stmt|;
comment|// Capture stdout and stderr
name|ByteArrayOutputStream
name|dataOut
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|out
init|=
operator|new
name|PrintStream
argument_list|(
name|dataOut
argument_list|)
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|dataErr
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|err
init|=
operator|new
name|PrintStream
argument_list|(
name|dataErr
argument_list|)
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|err
argument_list|)
expr_stmt|;
name|CliSessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|ss
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|ss
operator|.
name|err
operator|=
name|err
expr_stmt|;
comment|// Save output as yo cannot print it while System.out and System.err are weird
name|String
name|message
decl_stmt|;
name|String
name|errors
decl_stmt|;
name|int
name|ret
decl_stmt|;
try|try
block|{
name|CliSessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
comment|// issue a command with bad options
name|ret
operator|=
name|cliDriver
operator|.
name|processCmd
argument_list|(
literal|"!ls --abcdefghijklmnopqrstuvwxyz123456789"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// restore System.out and System.err
name|System
operator|.
name|setOut
argument_list|(
name|oldOut
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|oldErr
argument_list|)
expr_stmt|;
block|}
name|message
operator|=
name|dataOut
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|errors
operator|=
name|dataErr
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Comments with '--; should not have been stripped,"
operator|+
literal|" so command should fail"
argument_list|,
name|ret
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Comments with '--; should not have been stripped,"
operator|+
literal|" so we should have got an error in the output: '"
operator|+
name|errors
operator|+
literal|"'."
argument_list|,
name|errors
operator|.
name|contains
argument_list|(
literal|"option"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|message
argument_list|)
expr_stmt|;
comment|// message kept around in for debugging
block|}
comment|/**    * Do the actual testing against a mocked CliDriver based on what type of schema    *    * @param mockSchema    *          Schema to throw against test    * @return Output that would have been sent to the user    * @throws CommandNeedRetryException    *           won't actually be thrown    */
specifier|private
name|PrintStream
name|headerPrintingTestDriver
parameter_list|(
name|Schema
name|mockSchema
parameter_list|)
throws|throws
name|CommandNeedRetryException
block|{
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
comment|// We want the driver to try to print the header...
name|Configuration
name|conf
init|=
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|eq
argument_list|(
name|ConfVars
operator|.
name|HIVE_CLI_PRINT_HEADER
operator|.
name|varname
argument_list|)
argument_list|,
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cliDriver
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|IDriver
name|proc
init|=
name|mock
argument_list|(
name|IDriver
operator|.
name|class
argument_list|)
decl_stmt|;
name|CommandProcessorResponse
name|cpr
init|=
name|mock
argument_list|(
name|CommandProcessorResponse
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|proc
operator|.
name|run
argument_list|(
name|anyString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|cpr
argument_list|)
expr_stmt|;
comment|// and then see what happens based on the provided schema
name|when
argument_list|(
name|proc
operator|.
name|getSchema
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockSchema
argument_list|)
expr_stmt|;
name|CliSessionState
name|mockSS
init|=
name|mock
argument_list|(
name|CliSessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|PrintStream
name|mockOut
init|=
name|mock
argument_list|(
name|PrintStream
operator|.
name|class
argument_list|)
decl_stmt|;
name|mockSS
operator|.
name|out
operator|=
name|mockOut
expr_stmt|;
name|cliDriver
operator|.
name|processLocalCmd
argument_list|(
literal|"use default;"
argument_list|,
name|proc
argument_list|,
name|mockSS
argument_list|)
expr_stmt|;
return|return
name|mockOut
return|;
block|}
specifier|public
name|void
name|testGetCommandCompletor
parameter_list|()
block|{
name|Completer
index|[]
name|completors
init|=
name|CliDriver
operator|.
name|getCommandCompleter
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|completors
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|completors
index|[
literal|0
index|]
operator|instanceof
name|ArgumentCompleter
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|completors
index|[
literal|1
index|]
operator|instanceof
name|Completer
argument_list|)
expr_stmt|;
comment|//comletor add space after last delimeter
name|List
argument_list|<
name|CharSequence
argument_list|>
name|testList
init|=
operator|new
name|ArrayList
argument_list|<
name|CharSequence
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|")"
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|completors
index|[
literal|1
index|]
operator|.
name|complete
argument_list|(
literal|"fdsdfsdf"
argument_list|,
literal|0
argument_list|,
name|testList
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|") "
argument_list|,
name|testList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|testList
operator|=
operator|new
name|ArrayList
argument_list|<
name|CharSequence
argument_list|>
argument_list|()
expr_stmt|;
name|completors
index|[
literal|1
index|]
operator|.
name|complete
argument_list|(
literal|"len"
argument_list|,
literal|0
argument_list|,
name|testList
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|testList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"length("
argument_list|)
argument_list|)
expr_stmt|;
name|testList
operator|=
operator|new
name|ArrayList
argument_list|<
name|CharSequence
argument_list|>
argument_list|()
expr_stmt|;
name|completors
index|[
literal|0
index|]
operator|.
name|complete
argument_list|(
literal|"set f"
argument_list|,
literal|0
argument_list|,
name|testList
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"set"
argument_list|,
name|testList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRun
parameter_list|()
throws|throws
name|Exception
block|{
comment|// clean history
name|String
name|historyDirectory
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.home"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
operator|new
name|File
argument_list|(
name|historyDirectory
argument_list|)
operator|)
operator|.
name|exists
argument_list|()
condition|)
block|{
name|File
name|historyFile
init|=
operator|new
name|File
argument_list|(
name|historyDirectory
operator|+
name|File
operator|.
name|separator
operator|+
literal|".hivehistory"
argument_list|)
decl_stmt|;
name|historyFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
name|HiveConf
name|configuration
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SESSION_HISTORY_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|PrintStream
name|oldOut
init|=
name|System
operator|.
name|out
decl_stmt|;
name|ByteArrayOutputStream
name|dataOut
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|dataOut
argument_list|)
argument_list|)
expr_stmt|;
name|PrintStream
name|oldErr
init|=
name|System
operator|.
name|err
decl_stmt|;
name|ByteArrayOutputStream
name|dataErr
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|dataErr
argument_list|)
argument_list|)
expr_stmt|;
name|CliSessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|CliSessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
block|{}
decl_stmt|;
try|try
block|{
operator|new
name|FakeCliDriver
argument_list|()
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
argument_list|,
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"test message"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataErr
operator|.
name|toString
argument_list|()
argument_list|,
name|dataErr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Hive history file="
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataErr
operator|.
name|toString
argument_list|()
argument_list|,
name|dataErr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"File: fakeFile is not a file."
argument_list|)
argument_list|)
expr_stmt|;
name|dataOut
operator|.
name|reset
argument_list|()
expr_stmt|;
name|dataErr
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|setOut
argument_list|(
name|oldOut
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|oldErr
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test commands exit and quit    */
specifier|public
name|void
name|testQuit
parameter_list|()
throws|throws
name|Exception
block|{
name|CliSessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|ss
operator|.
name|err
operator|=
name|System
operator|.
name|err
expr_stmt|;
name|ss
operator|.
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
try|try
block|{
name|CliSessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
name|cliDriver
operator|.
name|processCmd
argument_list|(
literal|"quit"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
try|try
block|{
name|CliSessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
name|cliDriver
operator|.
name|processCmd
argument_list|(
literal|"exit"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testProcessSelectDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|CliSessionState
name|sessinState
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|CliSessionState
operator|.
name|start
argument_list|(
name|sessinState
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|sessinState
operator|.
name|err
operator|=
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|sessinState
operator|.
name|database
operator|=
literal|"database"
expr_stmt|;
name|CliDriver
name|driver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
try|try
block|{
name|driver
operator|.
name|processSelectDatabase
argument_list|(
name|sessinState
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shuld be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|40000
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"FAILED: ParseException line 1:4 cannot recognize input near 'database'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testprocessInitFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|oldHiveHome
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
decl_stmt|;
name|String
name|oldHiveConfDir
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|)
decl_stmt|;
name|File
name|homeFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|"hive"
argument_list|)
decl_stmt|;
name|String
name|tmpDir
init|=
name|homeFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsoluteFile
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"TestCliDriverMethods"
decl_stmt|;
name|homeFile
operator|.
name|delete
argument_list|()
expr_stmt|;
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|tmpDir
argument_list|)
argument_list|)
expr_stmt|;
name|homeFile
operator|=
operator|new
name|File
argument_list|(
name|tmpDir
operator|+
name|File
operator|.
name|separator
operator|+
literal|"bin"
operator|+
name|File
operator|.
name|separator
operator|+
name|CliDriver
operator|.
name|HIVERCFILE
argument_list|)
expr_stmt|;
name|homeFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|homeFile
operator|.
name|createNewFile
argument_list|()
expr_stmt|;
name|FileUtils
operator|.
name|write
argument_list|(
name|homeFile
argument_list|,
literal|"-- init hive file for test "
argument_list|)
expr_stmt|;
name|setEnv
argument_list|(
literal|"HIVE_HOME"
argument_list|,
name|homeFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|setEnv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|,
name|homeFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|CliSessionState
name|sessionState
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|sessionState
operator|.
name|err
operator|=
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|sessionState
operator|.
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
try|try
block|{
name|CliSessionState
operator|.
name|start
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
name|cliDriver
operator|.
name|processInitFiles
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Putting the global hiverc in $HIVE_HOME/bin/.hiverc is deprecated. "
operator|+
literal|"Please use $HIVE_CONF_DIR/.hiverc instead."
argument_list|)
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|write
argument_list|(
name|homeFile
argument_list|,
literal|"bla bla bla"
argument_list|)
expr_stmt|;
comment|// if init file contains incorrect row
try|try
block|{
name|cliDriver
operator|.
name|processInitFiles
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|40000
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|setEnv
argument_list|(
literal|"HIVE_HOME"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|cliDriver
operator|.
name|processInitFiles
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|40000
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// restore data
name|setEnv
argument_list|(
literal|"HIVE_HOME"
argument_list|,
name|oldHiveHome
argument_list|)
expr_stmt|;
name|setEnv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|,
name|oldHiveConfDir
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|tmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|File
name|f
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive"
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|write
argument_list|(
name|f
argument_list|,
literal|"bla bla bla"
argument_list|)
expr_stmt|;
try|try
block|{
name|sessionState
operator|.
name|initFiles
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
name|f
operator|.
name|getAbsolutePath
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|CliDriver
name|cliDriver
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
name|cliDriver
operator|.
name|processInitFiles
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExitException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|40000
argument_list|,
name|e
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"cannot recognize input near 'bla' 'bla' 'bla'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|setEnv
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
index|[]
name|classes
init|=
name|Collections
operator|.
name|class
operator|.
name|getDeclaredClasses
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
name|System
operator|.
name|getenv
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
name|cl
range|:
name|classes
control|)
block|{
if|if
condition|(
literal|"java.util.Collections$UnmodifiableMap"
operator|.
name|equals
argument_list|(
name|cl
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|Field
name|field
init|=
name|cl
operator|.
name|getDeclaredField
argument_list|(
literal|"m"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|obj
init|=
name|field
operator|.
name|get
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|FakeCliDriver
extends|extends
name|CliDriver
block|{
annotation|@
name|Override
specifier|protected
name|void
name|setupConsoleReader
parameter_list|()
throws|throws
name|IOException
block|{
name|reader
operator|=
operator|new
name|FakeConsoleReader
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FakeConsoleReader
extends|extends
name|ConsoleReader
block|{
specifier|private
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|File
name|temp
init|=
literal|null
decl_stmt|;
specifier|public
name|FakeConsoleReader
parameter_list|()
throws|throws
name|IOException
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|readLine
parameter_list|(
name|String
name|prompt
parameter_list|)
throws|throws
name|IOException
block|{
name|FileWriter
name|writer
decl_stmt|;
switch|switch
condition|(
name|counter
operator|++
condition|)
block|{
case|case
literal|0
case|:
return|return
literal|"!echo test message;"
return|;
case|case
literal|1
case|:
name|temp
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|temp
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
return|return
literal|"source  "
operator|+
name|temp
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|";"
return|;
case|case
literal|2
case|:
name|temp
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|temp
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|writer
operator|=
operator|new
name|FileWriter
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
literal|"bla bla bla"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|"list file file://"
operator|+
name|temp
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|";"
return|;
case|case
literal|3
case|:
return|return
literal|"!echo "
return|;
case|case
literal|4
case|:
return|return
literal|"test message;"
return|;
case|case
literal|5
case|:
return|return
literal|"source  fakeFile;"
return|;
case|case
literal|6
case|:
name|temp
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive"
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|temp
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|writer
operator|=
operator|new
name|FileWriter
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
literal|"source  fakeFile;"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|"list file file://"
operator|+
name|temp
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|";"
return|;
comment|// drop table over10k;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|NoExitSecurityManager
extends|extends
name|SecurityManager
block|{
specifier|public
name|SecurityManager
name|parentSecurityManager
decl_stmt|;
specifier|public
name|NoExitSecurityManager
parameter_list|(
name|SecurityManager
name|parent
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|parentSecurityManager
operator|=
name|parent
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|,
name|Object
name|context
parameter_list|)
block|{
if|if
condition|(
name|parentSecurityManager
operator|!=
literal|null
condition|)
block|{
name|parentSecurityManager
operator|.
name|checkPermission
argument_list|(
name|perm
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{
if|if
condition|(
name|parentSecurityManager
operator|!=
literal|null
condition|)
block|{
name|parentSecurityManager
operator|.
name|checkPermission
argument_list|(
name|perm
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkExit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
throw|throw
operator|new
name|ExitException
argument_list|(
name|status
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ExitException
extends|extends
name|RuntimeException
block|{
name|int
name|status
decl_stmt|;
specifier|public
name|ExitException
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|public
name|int
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
block|}
block|}
end_class

end_unit

