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
name|ql
operator|.
name|history
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
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
name|cli
operator|.
name|CliSessionState
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
name|common
operator|.
name|LogUtils
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
name|common
operator|.
name|LogUtils
operator|.
name|LogInitializationException
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
name|common
operator|.
name|io
operator|.
name|SessionStream
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
name|Warehouse
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
name|DriverFactory
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
name|history
operator|.
name|HiveHistory
operator|.
name|Keys
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
name|history
operator|.
name|HiveHistory
operator|.
name|QueryInfo
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
name|history
operator|.
name|HiveHistory
operator|.
name|TaskInfo
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
name|io
operator|.
name|IgnoreKeyTextOutputFormat
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
name|metadata
operator|.
name|Hive
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
name|plan
operator|.
name|LoadTableDesc
operator|.
name|LoadFileType
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
name|session
operator|.
name|SessionState
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
name|tools
operator|.
name|LineageInfo
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
name|mapred
operator|.
name|TextInputFormat
import|;
end_import

begin_comment
comment|/**  * TestHiveHistory.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveHistory
extends|extends
name|TestCase
block|{
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
name|String
name|tmpdir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Path
name|tmppath
init|=
operator|new
name|Path
argument_list|(
name|tmpdir
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Hive
name|db
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
comment|/*    * intialize the tables    */
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
block|{
try|try
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|HiveHistory
operator|.
name|class
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmppath
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|tmppath
argument_list|)
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|tmpdir
operator|+
literal|" exists but is not a directory"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tmppath
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tmppath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not make scratch directory "
operator|+
name|tmpdir
argument_list|)
throw|;
block|}
block|}
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// copy the test files into hadoop if required.
name|int
name|i
init|=
literal|0
decl_stmt|;
name|Path
index|[]
name|hadoopDataFile
init|=
operator|new
name|Path
index|[
literal|2
index|]
decl_stmt|;
name|String
index|[]
name|testFiles
init|=
block|{
literal|"kv1.txt"
block|,
literal|"kv2.txt"
block|}
decl_stmt|;
name|String
name|testFileDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|oneFile
range|:
name|testFiles
control|)
block|{
name|Path
name|localDataFile
init|=
operator|new
name|Path
argument_list|(
name|testFileDir
argument_list|,
name|oneFile
argument_list|)
decl_stmt|;
name|hadoopDataFile
index|[
name|i
index|]
operator|=
operator|new
name|Path
argument_list|(
name|tmppath
argument_list|,
name|oneFile
argument_list|)
expr_stmt|;
name|fs
operator|.
name|copyFromLocalFile
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
name|localDataFile
argument_list|,
name|hadoopDataFile
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
comment|// load the test files into tables
name|i
operator|=
literal|0
expr_stmt|;
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
index|[]
name|srctables
init|=
block|{
literal|"src"
block|,
literal|"src2"
block|}
decl_stmt|;
name|LinkedList
argument_list|<
name|String
argument_list|>
name|cols
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"key"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"value"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|src
range|:
name|srctables
control|)
block|{
name|db
operator|.
name|dropTable
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|src
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|db
operator|.
name|createTable
argument_list|(
name|src
argument_list|,
name|cols
argument_list|,
literal|null
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|db
operator|.
name|loadTable
argument_list|(
name|hadoopDataFile
index|[
name|i
index|]
argument_list|,
name|src
argument_list|,
name|LoadFileType
operator|.
name|KEEP_EXISTING
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|i
operator|++
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
operator|new
name|RuntimeException
argument_list|(
literal|"Encountered throwable"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check history file output for this query.    */
specifier|public
name|void
name|testSimpleQuery
parameter_list|()
block|{
operator|new
name|LineageInfo
argument_list|()
expr_stmt|;
try|try
block|{
comment|// NOTE: It is critical to do this here so that log4j is reinitialized
comment|// before any of the other core hive classes are loaded
try|try
block|{
name|LogUtils
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LogInitializationException
name|e
parameter_list|)
block|{       }
name|HiveConf
name|hconf
init|=
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|hconf
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
name|CliSessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|ss
operator|.
name|in
operator|=
name|System
operator|.
name|in
expr_stmt|;
try|try
block|{
name|ss
operator|.
name|out
operator|=
operator|new
name|SessionStream
argument_list|(
name|System
operator|.
name|out
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|ss
operator|.
name|err
operator|=
operator|new
name|SessionStream
argument_list|(
name|System
operator|.
name|err
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|String
name|cmd
init|=
literal|"select a.key+1 from src a"
decl_stmt|;
name|IDriver
name|d
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
name|d
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
name|fail
argument_list|(
literal|"Failed"
argument_list|)
expr_stmt|;
block|}
name|HiveHistoryViewer
name|hv
init|=
operator|new
name|HiveHistoryViewer
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|getHistFileName
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
name|jobInfoMap
init|=
name|hv
operator|.
name|getJobInfoMap
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
name|taskInfoMap
init|=
name|hv
operator|.
name|getTaskInfoMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|jobInfoMap
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"jobInfo Map size not 1"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|taskInfoMap
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"jobInfo Map size not 1"
argument_list|)
expr_stmt|;
block|}
name|cmd
operator|=
operator|(
name|String
operator|)
name|jobInfoMap
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
name|QueryInfo
name|ji
init|=
name|jobInfoMap
operator|.
name|get
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ji
operator|.
name|hm
operator|.
name|get
argument_list|(
name|Keys
operator|.
name|QUERY_NUM_TASKS
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"1"
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"Wrong number of tasks"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Failed"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testQueryloglocParentDirNotExist
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|parentTmpDir
init|=
name|tmpdir
operator|+
literal|"/HIVE2654"
decl_stmt|;
name|Path
name|parentDirPath
init|=
operator|new
name|Path
argument_list|(
name|parentTmpDir
argument_list|)
decl_stmt|;
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|parentDirPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
try|try
block|{
name|String
name|actualDir
init|=
name|parentTmpDir
operator|+
literal|"/test"
decl_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHISTORYFILELOC
operator|.
name|toString
argument_list|()
argument_list|,
name|actualDir
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HiveHistory
name|hiveHistory
init|=
operator|new
name|HiveHistoryImpl
argument_list|(
name|ss
argument_list|)
decl_stmt|;
name|Path
name|actualPath
init|=
operator|new
name|Path
argument_list|(
name|actualDir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|actualPath
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"Query location path is not exist :"
operator|+
name|actualPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|parentDirPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{       }
block|}
block|}
comment|/**    * Check if HiveHistoryImpl class is returned when hive history is enabled    * @throws Exception    */
specifier|public
name|void
name|testHiveHistoryConfigEnabled
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
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
name|SessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|HiveHistory
name|hHistory
init|=
name|ss
operator|.
name|getHiveHistory
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"checking hive history class when history is enabled"
argument_list|,
name|hHistory
operator|.
name|getClass
argument_list|()
argument_list|,
name|HiveHistoryImpl
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check if HiveHistory class is a Proxy class when hive history is disabled    * @throws Exception    */
specifier|public
name|void
name|testHiveHistoryConfigDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SESSION_HISTORY_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|HiveHistory
name|hHistory
init|=
name|ss
operator|.
name|getHiveHistory
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"checking hive history class when history is disabled"
argument_list|,
name|hHistory
operator|.
name|getClass
argument_list|()
operator|!=
name|HiveHistoryImpl
operator|.
name|class
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"hHistory.getClass"
operator|+
name|hHistory
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"verifying proxy class is used when history is disabled"
argument_list|,
name|Proxy
operator|.
name|isProxyClass
argument_list|(
name|hHistory
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

