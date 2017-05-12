begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|cli
package|;
end_package

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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|BufferedWriter
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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

begin_class
specifier|public
class|class
name|TestHiveCli
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHiveCli
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ERRNO_OK
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ERRNO_ARGS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ERRNO_OTHER
init|=
literal|2
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SOURCE_CONTEXT
init|=
literal|"create table if not exists test.testSrcTbl(sc1 string);"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SOURCE_CONTEXT2
init|=
literal|"create table if not exists test.testSrcTbl2(sc2 string);"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SOURCE_CONTEXT3
init|=
literal|"create table if not exists test.testSrcTbl3(sc3 string);"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SOURCE_CONTEXT4
init|=
literal|"show tables;!ls;show tables;\nquit;"
decl_stmt|;
specifier|final
specifier|static
name|String
name|CMD
init|=
literal|"create database if not exists test;\ncreate table if not exists test.testTbl(a string, b "
operator|+
literal|"string);\n"
decl_stmt|;
specifier|private
name|HiveCli
name|cli
decl_stmt|;
specifier|private
name|OutputStream
name|os
decl_stmt|;
specifier|private
name|PrintStream
name|ps
decl_stmt|;
specifier|private
name|OutputStream
name|errS
decl_stmt|;
specifier|private
name|PrintStream
name|errPs
decl_stmt|;
specifier|private
name|File
name|tmp
init|=
literal|null
decl_stmt|;
specifier|private
name|void
name|executeCMD
parameter_list|(
name|String
index|[]
name|args
parameter_list|,
name|String
name|input
parameter_list|,
name|int
name|retCode
parameter_list|)
block|{
name|InputStream
name|inputStream
init|=
literal|null
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
try|try
block|{
if|if
condition|(
name|input
operator|!=
literal|null
condition|)
block|{
name|inputStream
operator|=
name|IOUtils
operator|.
name|toInputStream
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
name|ret
operator|=
name|cli
operator|.
name|runWithArgs
argument_list|(
name|args
argument_list|,
name|inputStream
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to execute command due to the error: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|retCode
operator|!=
name|ret
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed due to the error:"
operator|+
name|errS
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Supported return code is "
operator|+
name|retCode
operator|+
literal|" while the actual is "
operator|+
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This method is used for verifying CMD to see whether the output contains the keywords provided.    *    * @param CMD    * @param keywords    * @param os    * @param options    * @param retCode    * @param contains    */
specifier|private
name|void
name|verifyCMD
parameter_list|(
name|String
name|CMD
parameter_list|,
name|String
name|keywords
parameter_list|,
name|OutputStream
name|os
parameter_list|,
name|String
index|[]
name|options
parameter_list|,
name|int
name|retCode
parameter_list|,
name|boolean
name|contains
parameter_list|)
block|{
name|executeCMD
argument_list|(
name|options
argument_list|,
name|CMD
argument_list|,
name|retCode
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|os
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|output
argument_list|)
expr_stmt|;
if|if
condition|(
name|contains
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The expected keyword \""
operator|+
name|keywords
operator|+
literal|"\" occur in the output: "
operator|+
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
name|keywords
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"The expected keyword \""
operator|+
name|keywords
operator|+
literal|"\" should be excluded occurred in the output: "
operator|+
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
name|keywords
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInValidCmd
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"!lss\n"
argument_list|,
literal|"Failed to execute lss"
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCmd
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"show tables;!ls;show tables;\n"
argument_list|,
literal|"src"
argument_list|,
name|os
argument_list|,
literal|null
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetPromptValue
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"set hive.cli.prompt=MYCLI;SHOW\nTABLES;"
argument_list|,
literal|"MYCLI> "
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetHeaderValue
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"create database if not exists test;\ncreate table if not exists test.testTbl(a string, b string);\nset hive.cli.print.header=true;\n select * from test.testTbl;\n"
argument_list|,
literal|"testtbl.a testtbl.b"
argument_list|,
name|os
argument_list|,
literal|null
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHelp
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|"usage: hive"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-H"
block|}
argument_list|,
name|ERRNO_ARGS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidDatabaseOptions
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"\nshow tables;\nquit;\n"
argument_list|,
literal|"Database does not exist: invalidDB"
argument_list|,
name|errS
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"invalidDB"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDatabaseOptions
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"\nshow tables;\nquit;"
argument_list|,
literal|"testtbl"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSourceCmd
parameter_list|()
block|{
name|File
name|f
init|=
name|generateTmpFile
argument_list|(
name|SOURCE_CONTEXT
argument_list|)
decl_stmt|;
name|verifyCMD
argument_list|(
literal|"source "
operator|+
name|f
operator|.
name|getPath
argument_list|()
operator|+
literal|";"
operator|+
literal|"desc testSrcTbl;\nquit;\n"
argument_list|,
literal|"sc1"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSourceCmd2
parameter_list|()
block|{
name|File
name|f
init|=
name|generateTmpFile
argument_list|(
name|SOURCE_CONTEXT3
argument_list|)
decl_stmt|;
name|verifyCMD
argument_list|(
literal|"source "
operator|+
name|f
operator|.
name|getPath
argument_list|()
operator|+
literal|";"
operator|+
literal|"desc testSrcTbl3;\nquit;\n"
argument_list|,
literal|"sc3"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSourceCmd3
parameter_list|()
block|{
name|File
name|f
init|=
name|generateTmpFile
argument_list|(
name|SOURCE_CONTEXT4
argument_list|)
decl_stmt|;
name|verifyCMD
argument_list|(
literal|"source "
operator|+
name|f
operator|.
name|getPath
argument_list|()
operator|+
literal|";"
operator|+
literal|"desc testSrcTbl4;\nquit;\n"
argument_list|,
literal|"src"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSqlFromCmd
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|""
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show databases;"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSqlFromCmdWithDBName
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|"testtbl"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|,
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidOptions
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|"The '-e' and '-f' options cannot be specified simultaneously"
argument_list|,
name|errS
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|,
literal|"-f"
block|,
literal|"path/to/file"
block|}
argument_list|,
name|ERRNO_ARGS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidOptions2
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|"Unrecognized option: -k"
argument_list|,
name|errS
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-k"
block|}
argument_list|,
name|ERRNO_ARGS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVariables
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"set system:xxx=5;\nset system:yyy=${system:xxx};\nset system:yyy;"
argument_list|,
literal|""
argument_list|,
name|os
argument_list|,
literal|null
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVariablesForSource
parameter_list|()
block|{
name|File
name|f
init|=
name|generateTmpFile
argument_list|(
name|SOURCE_CONTEXT2
argument_list|)
decl_stmt|;
name|verifyCMD
argument_list|(
literal|"set hiveconf:zzz="
operator|+
name|f
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|";\nsource ${hiveconf:zzz};\ndesc testSrcTbl2;"
argument_list|,
literal|"sc2"
argument_list|,
name|os
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--database"
block|,
literal|"test"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrOutput
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"show tables;set system:xxx=5;set system:yyy=${system:xxx};\nlss;"
argument_list|,
literal|"cannot recognize input near 'lss' '<EOF>' '<EOF>'"
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUseCurrentDB1
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"create database if not exists testDB; set hive.cli.print.current.db=true;use testDB;\n"
operator|+
literal|"use default;drop if exists testDB;"
argument_list|,
literal|"hive (testDB)>"
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUseCurrentDB2
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"create database if not exists testDB; set hive.cli.print.current.db=true;use\ntestDB;\nuse default;drop if exists testDB;"
argument_list|,
literal|"hive (testDB)>"
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUseCurrentDB3
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"create database if not exists testDB; set hive.cli.print.current.db=true;use  testDB;\n"
operator|+
literal|"use default;drop if exists testDB;"
argument_list|,
literal|"hive (testDB)>"
argument_list|,
name|errS
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUseInvalidDB
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|"set hive.cli.print.current.db=true;use invalidDB;"
argument_list|,
literal|"hive (invalidDB)>"
argument_list|,
name|os
argument_list|,
literal|null
argument_list|,
name|ERRNO_OTHER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoErrorDB
parameter_list|()
block|{
name|verifyCMD
argument_list|(
literal|null
argument_list|,
literal|"Error: Method not supported (state=,code=0)"
argument_list|,
name|errS
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|}
argument_list|,
name|ERRNO_OK
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|redirectOutputStream
parameter_list|()
block|{
comment|// Setup output stream to redirect output to
name|os
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|ps
operator|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|errS
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|errPs
operator|=
operator|new
name|PrintStream
argument_list|(
name|errS
argument_list|)
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|ps
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|errPs
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initFromFile
parameter_list|()
block|{
name|tmp
operator|=
name|generateTmpFile
argument_list|(
name|CMD
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Fail to create the initial file"
argument_list|)
expr_stmt|;
block|}
name|executeCMD
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-f"
block|,
literal|"\""
operator|+
name|tmp
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"\""
block|}
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|File
name|generateTmpFile
parameter_list|(
name|String
name|context
parameter_list|)
block|{
name|File
name|file
init|=
literal|null
decl_stmt|;
name|BufferedWriter
name|bw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|file
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|".sql"
argument_list|)
expr_stmt|;
name|bw
operator|=
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|FileWriter
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|bw
operator|.
name|write
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to write tmp file due to the exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|bw
argument_list|)
expr_stmt|;
block|}
return|return
name|file
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"datanucleus.schema.autoCreateAll"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|cli
operator|=
operator|new
name|HiveCli
argument_list|()
expr_stmt|;
name|redirectOutputStream
argument_list|()
expr_stmt|;
name|initFromFile
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|tmp
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

