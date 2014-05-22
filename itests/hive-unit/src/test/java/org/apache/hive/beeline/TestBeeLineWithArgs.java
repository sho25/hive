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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|assertNotNull
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
name|fail
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
name|FileOutputStream
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
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
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
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|List
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
name|fs
operator|.
name|Path
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
specifier|private
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
specifier|final
name|String
name|tableName
init|=
literal|"TestBeelineTable1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tableComment
init|=
literal|"Test table comment"
decl_stmt|;
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getBaseArgs
parameter_list|(
name|String
name|jdbcUrl
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-d"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|BeeLine
operator|.
name|BEELINE_DEFAULT_JDBC_DRIVER
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-u"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|jdbcUrl
argument_list|)
expr_stmt|;
return|return
name|argList
return|;
block|}
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
comment|// Set to non-zk lock manager to prevent HS2 from trying to connect
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.EmbeddedLockManager"
argument_list|)
expr_stmt|;
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
name|createTable
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create table for use by tests    * @throws ClassNotFoundException    * @throws SQLException    */
specifier|private
specifier|static
name|void
name|createTable
parameter_list|()
throws|throws
name|ClassNotFoundException
throws|,
name|SQLException
block|{
name|Class
operator|.
name|forName
argument_list|(
name|BeeLine
operator|.
name|BEELINE_DEFAULT_JDBC_DRIVER
argument_list|)
expr_stmt|;
name|Connection
name|con
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|JDBC_URL
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Connection is null"
argument_list|,
name|con
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Connection should not be closed"
argument_list|,
name|con
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|Statement
name|stmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Statement is null"
argument_list|,
name|stmt
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"set hive.support.concurrency = false"
argument_list|)
expr_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|String
name|dataFileDir
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Path
name|dataFilePath
init|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
decl_stmt|;
comment|// drop table. ignore error.
try|try
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|fail
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// create table
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (under_col int comment 'the under column', value string) comment '"
operator|+
name|tableComment
operator|+
literal|"'"
argument_list|)
expr_stmt|;
comment|// load data
name|stmt
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
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
name|List
argument_list|<
name|String
argument_list|>
name|argList
parameter_list|)
throws|throws
name|Throwable
block|{
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
name|String
index|[]
name|args
init|=
name|argList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|argList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
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
comment|/**    * Attempt to execute a simple script file with the -f option to BeeLine    * Test for presence of an expected pattern    * in the output (stdout or stderr), fail if not found    * Print PASSED or FAILED    * @paramm testName Name of test to print    * @param expectedPattern Text to look for in command output/error    * @param shouldMatch true if the pattern should be found, false if it should not    * @throws Exception on command execution error    */
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
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|argList
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
name|argList
operator|.
name|add
argument_list|(
literal|"-f"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|scriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|testCommandLineScript
argument_list|(
name|argList
argument_list|)
decl_stmt|;
name|boolean
name|matches
init|=
name|output
operator|.
name|contains
argument_list|(
name|expectedPattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|shouldMatch
operator|!=
name|matches
condition|)
block|{
comment|//failed
name|fail
argument_list|(
name|testName
operator|+
literal|": Output"
operator|+
name|output
operator|+
literal|" should"
operator|+
operator|(
name|shouldMatch
condition|?
literal|""
else|:
literal|" not"
operator|)
operator|+
literal|" contain "
operator|+
name|expectedPattern
argument_list|)
expr_stmt|;
block|}
name|scriptFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that BeeLine will read comment lines that start with whitespace    * @throws Throwable    */
annotation|@
name|Test
specifier|public
name|void
name|testWhitespaceBeforeCommentScriptFile
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testWhitespaceBeforeCommentScriptFile"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|" 	 	-- comment has spaces and tabs before it\n 	 	# comment has spaces and tabs before it\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"cannot recognize input near '<EOF>'"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
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
argument_list|,
name|argList
argument_list|)
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
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test Beeline -hivevar option. User can specify --hivevar name=value on Beeline command line.    * In the script, user should be able to use it in the form of ${name}, which will be substituted with    * the value.    * @throws Throwable    */
annotation|@
name|Test
specifier|public
name|void
name|testBeelineHiveVariable
parameter_list|()
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hivevar"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"DUMMY_TBL=dummy"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testHiveCommandLineHiveVariable"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"create table ${DUMMY_TBL} (d int);\nshow tables;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"dummy"
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineHiveConfVariable
parameter_list|()
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hiveconf"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"hive.table.name=dummy"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testBeelineHiveConfVariable"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"create table ${hiveconf:hive.table.name} (d int);\nshow tables;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"dummy"
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test Beeline -hivevar option. User can specify --hivevar name=value on Beeline command line.    * This test defines multiple variables using repeated --hivevar or --hiveconf flags.    * @throws Throwable    */
annotation|@
name|Test
specifier|public
name|void
name|testBeelineMultiHiveVariable
parameter_list|()
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hivevar"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"TABLE_NAME=dummy2"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hiveconf"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"COLUMN_NAME=d"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hivevar"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"COMMAND=create"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hivevar"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"OBJECT=table"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hiveconf"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"COLUMN_TYPE=int"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testHiveCommandLineHiveVariable"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"${COMMAND} ${OBJECT} ${TABLE_NAME} (${hiveconf:COLUMN_NAME} ${hiveconf:COLUMN_TYPE});\nshow tables;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"dummy2"
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
argument_list|,
name|argList
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
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Select null from table , check how null is printed    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testNullDefault
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testNullDefault"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set hive.support.concurrency = false;\n"
operator|+
literal|"select null from "
operator|+
name|tableName
operator|+
literal|" limit 1 ;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"NULL"
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
argument_list|,
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Select null from table , check if default null is printed differently    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testNullNonEmpty
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testNullNonDefault"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set hive.support.concurrency = false;\n"
operator|+
literal|"!set nullemptystring false\n select null from "
operator|+
name|tableName
operator|+
literal|" limit 1 ;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"NULL"
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
argument_list|,
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetVariableValue
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testGetVariableValue"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set env:TERM;"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"env:TERM"
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
argument_list|,
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Select null from table , check if setting null to empty string works.    * Original beeline/sqlline used to print nulls as empty strings    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testNullEmpty
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testNullNonDefault"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set hive.support.concurrency = false;\n"
operator|+
literal|"!set nullemptystring true\n select 'abc',null,'def' from "
operator|+
name|tableName
operator|+
literal|" limit 1 ;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"'abc','','def'"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--outputformat=csv"
argument_list|)
expr_stmt|;
name|testScriptFile
argument_list|(
name|TEST_NAME
argument_list|,
name|SCRIPT_TEXT
argument_list|,
name|EXPECTED_PATTERN
argument_list|,
literal|true
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Select null from table , check if setting null to empty string works - Using beeling cmd line    *  argument.    * Original beeline/sqlline used to print nulls as empty strings    * Print PASSED or FAILED    */
annotation|@
name|Test
specifier|public
name|void
name|testNullEmptyCmdArg
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testNullNonDefault"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set hive.support.concurrency = false;\n"
operator|+
literal|"select 'abc',null,'def' from "
operator|+
name|tableName
operator|+
literal|" limit 1 ;\n"
decl_stmt|;
comment|//final String EXPECTED_PATTERN = "| abc  |      | def  |";
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"'abc','','def'"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--nullemptystring=true"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--outputformat=csv"
argument_list|)
expr_stmt|;
name|testScriptFile
argument_list|(
name|TEST_NAME
argument_list|,
name|SCRIPT_TEXT
argument_list|,
name|EXPECTED_PATTERN
argument_list|,
literal|true
argument_list|,
name|argList
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
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-f"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|scriptFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|output
init|=
name|testCommandLineScript
argument_list|(
name|argList
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
comment|/**    * HIVE-4566    * @throws UnsupportedEncodingException    */
annotation|@
name|Test
specifier|public
name|void
name|testNPE
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
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
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!typeinfo"
block|}
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
name|Assert
operator|.
name|assertFalse
argument_list|(
name|output
operator|.
name|contains
argument_list|(
literal|"java.lang.NullPointerException"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|output
operator|.
name|contains
argument_list|(
literal|"No current connection"
argument_list|)
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!nativesql"
block|}
argument_list|)
expr_stmt|;
name|output
operator|=
name|os
operator|.
name|toString
argument_list|(
literal|"UTF8"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|output
operator|.
name|contains
argument_list|(
literal|"java.lang.NullPointerException"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|output
operator|.
name|contains
argument_list|(
literal|"No current connection"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> PASSED "
operator|+
literal|"testNPE"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHiveVarSubstitution
parameter_list|()
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|JDBC_URL
operator|+
literal|"#D_TBL=dummy_t"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testHiveVarSubstitution"
decl_stmt|;
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"create table ${D_TBL} (d int);\nshow tables;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"dummy_t"
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmbeddedBeelineConnection
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|embeddedJdbcURL
init|=
name|BeeLine
operator|.
name|BEELINE_DEFAULT_JDBC_URL
operator|+
literal|"/Default"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
name|getBaseArgs
argument_list|(
name|embeddedJdbcURL
argument_list|)
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"--hivevar"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"DUMMY_TBL=embedded_table"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|TEST_NAME
init|=
literal|"testEmbeddedBeelineConnection"
decl_stmt|;
comment|// Set to non-zk lock manager to avoid trying to connect to zookeeper
specifier|final
name|String
name|SCRIPT_TEXT
init|=
literal|"set hive.lock.manager=org.apache.hadoop.hive.ql.lockmgr.EmbeddedLockManager;\n"
operator|+
literal|"create table ${DUMMY_TBL} (d int);\nshow tables;\n"
decl_stmt|;
specifier|final
name|String
name|EXPECTED_PATTERN
init|=
literal|"embedded_table"
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
argument_list|,
name|argList
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

