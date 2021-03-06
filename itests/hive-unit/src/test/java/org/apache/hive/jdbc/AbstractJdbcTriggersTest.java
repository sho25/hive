begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNull
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
name|assertTrue
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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|llap
operator|.
name|LlapBaseInputFormat
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
name|WMTrigger
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
name|wm
operator|.
name|Trigger
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
operator|.
name|MiniClusterType
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
name|AfterClass
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
name|BeforeClass
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractJdbcTriggersTest
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
name|String
name|dataFileDir
decl_stmt|;
specifier|static
name|Path
name|kvDataFilePath
decl_stmt|;
specifier|protected
specifier|static
name|String
name|tableName
init|=
literal|"testtab1"
decl_stmt|;
specifier|protected
specifier|static
name|HiveConf
name|conf
init|=
literal|null
decl_stmt|;
specifier|protected
name|Connection
name|hs2Conn
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|confDir
init|=
literal|"../../data/conf/llap/"
decl_stmt|;
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/hive-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Setting hive-site: "
operator|+
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_TRIGGER_VALIDATION_INTERVAL
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_INITIALIZE_DEFAULT_SESSIONS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MODE
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/tez-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|,
name|MiniClusterType
operator|.
name|LLAP
argument_list|)
expr_stmt|;
name|dataFileDir
operator|=
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
expr_stmt|;
name|kvDataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|getDFS
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/apps_staging_dir/anonymous"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hs2Conn
operator|=
name|BaseJdbcWithMiniLlap
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|LlapBaseInputFormat
operator|.
name|closeAll
argument_list|()
expr_stmt|;
name|hs2Conn
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
name|afterTest
parameter_list|()
block|{
if|if
condition|(
name|miniHS2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|abstract
name|String
name|getTestName
parameter_list|()
function_decl|;
specifier|private
name|void
name|createSleepUDF
parameter_list|()
throws|throws
name|SQLException
block|{
name|String
name|udfName
init|=
name|TestJdbcWithMiniHS2
operator|.
name|SleepMsUDF
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Connection
name|con
init|=
name|hs2Conn
decl_stmt|;
name|Statement
name|stmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create temporary function sleep as '"
operator|+
name|udfName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|void
name|runQueryWithTrigger
parameter_list|(
specifier|final
name|String
name|query
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
parameter_list|,
specifier|final
name|String
name|expect
parameter_list|,
specifier|final
name|int
name|queryTimeoutSecs
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|testName
init|=
name|getTestName
argument_list|()
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start of test: {}"
argument_list|,
name|testName
argument_list|)
expr_stmt|;
name|Connection
name|con
init|=
name|hs2Conn
decl_stmt|;
name|BaseJdbcWithMiniLlap
operator|.
name|createTestTable
argument_list|(
name|con
argument_list|,
literal|null
argument_list|,
name|tableName
argument_list|,
name|kvDataFilePath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|createSleepUDF
argument_list|()
expr_stmt|;
specifier|final
name|Statement
name|selStmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|Throwable
name|throwable
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|queryTimeoutSecs
operator|>
literal|0
condition|)
block|{
name|selStmt
operator|.
name|setQueryTimeout
argument_list|(
name|queryTimeoutSecs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|setCmds
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|setCmd
range|:
name|setCmds
control|)
block|{
name|selStmt
operator|.
name|execute
argument_list|(
name|setCmd
argument_list|)
expr_stmt|;
block|}
block|}
name|selStmt
operator|.
name|execute
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|throwable
operator|=
name|e
expr_stmt|;
block|}
name|selStmt
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|expect
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
literal|"Expected query to succeed"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
literal|"Expected non-null throwable"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SQLException
operator|.
name|class
argument_list|,
name|throwable
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expect
operator|+
literal|" is not contained in "
operator|+
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|expect
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End of test: {} time: {} ms"
argument_list|,
name|testName
argument_list|,
operator|(
name|end
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
name|void
name|runQueryWithTrigger
parameter_list|(
specifier|final
name|String
name|query
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
parameter_list|,
specifier|final
name|String
name|expect
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|errCaptureExpect
parameter_list|)
throws|throws
name|Exception
block|{
name|Connection
name|con
init|=
name|hs2Conn
decl_stmt|;
name|BaseJdbcWithMiniLlap
operator|.
name|createTestTable
argument_list|(
name|con
argument_list|,
literal|null
argument_list|,
name|tableName
argument_list|,
name|kvDataFilePath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|createSleepUDF
argument_list|()
expr_stmt|;
specifier|final
name|ByteArrayOutputStream
name|baos
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
name|baos
argument_list|)
argument_list|)
expr_stmt|;
comment|// capture stderr
specifier|final
name|Statement
name|selStmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|Throwable
name|throwable
init|=
literal|null
decl_stmt|;
try|try
block|{
try|try
block|{
if|if
condition|(
name|setCmds
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|setCmd
range|:
name|setCmds
control|)
block|{
name|selStmt
operator|.
name|execute
argument_list|(
name|setCmd
argument_list|)
expr_stmt|;
block|}
block|}
name|selStmt
operator|.
name|execute
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|throwable
operator|=
name|e
expr_stmt|;
block|}
name|selStmt
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|expect
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
literal|"Expected query to succeed"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
literal|"Expected non-null throwable"
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SQLException
operator|.
name|class
argument_list|,
name|throwable
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expect
operator|+
literal|" is not contained in "
operator|+
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|expect
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|errCaptureExpect
operator|!=
literal|null
operator|&&
operator|!
name|errCaptureExpect
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// failure hooks are run after HiveStatement is closed. wait sometime for failure hook to execute
name|String
name|stdErrStr
init|=
literal|""
decl_stmt|;
while|while
condition|(
operator|!
name|stdErrStr
operator|.
name|contains
argument_list|(
name|errCaptureExpect
operator|.
name|get
argument_list|(
name|errCaptureExpect
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
condition|)
block|{
name|baos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|stdErrStr
operator|=
name|baos
operator|.
name|toString
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|errExpect
range|:
name|errCaptureExpect
control|)
block|{
name|assertTrue
argument_list|(
literal|"'"
operator|+
name|errExpect
operator|+
literal|"' expected in STDERR capture, but not found."
argument_list|,
name|stdErrStr
operator|.
name|contains
argument_list|(
name|errExpect
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|abstract
name|void
name|setupTriggers
parameter_list|(
specifier|final
name|List
argument_list|<
name|Trigger
argument_list|>
name|triggers
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|List
argument_list|<
name|String
argument_list|>
name|getConfigs
parameter_list|(
name|String
modifier|...
name|more
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set mapred.min.split.size=200"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set mapred.max.split.size=200"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set tez.grouping.min-size=200"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set tez.grouping.max-size=200"
argument_list|)
expr_stmt|;
if|if
condition|(
name|more
operator|!=
literal|null
condition|)
block|{
name|setCmds
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|more
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|setCmds
return|;
block|}
name|WMTrigger
name|wmTriggerFromTrigger
parameter_list|(
name|Trigger
name|trigger
parameter_list|)
block|{
name|WMTrigger
name|result
init|=
operator|new
name|WMTrigger
argument_list|(
literal|"rp"
argument_list|,
name|trigger
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|.
name|setTriggerExpression
argument_list|(
name|trigger
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|setActionExpression
argument_list|(
name|trigger
operator|.
name|getAction
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

