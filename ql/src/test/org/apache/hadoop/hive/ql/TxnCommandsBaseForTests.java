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
name|FileUtil
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
name|LocatedFileStatus
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
name|fs
operator|.
name|RemoteIterator
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|txn
operator|.
name|TxnDbUtil
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
name|HiveInputFormat
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
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|HashSet
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
name|Set
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|TxnCommandsBaseForTests
block|{
comment|//bucket count for test tables; set it to 1 for easier debugging
specifier|final
specifier|static
name|int
name|BUCKET_COUNT
init|=
literal|2
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
name|HiveConf
name|hiveConf
decl_stmt|;
name|Driver
name|d
decl_stmt|;
enum|enum
name|Table
block|{
name|ACIDTBL
argument_list|(
literal|"acidTbl"
argument_list|)
block|,
name|ACIDTBLPART
argument_list|(
literal|"acidTblPart"
argument_list|)
block|,
name|ACIDTBL2
argument_list|(
literal|"acidTbl2"
argument_list|)
block|,
name|NONACIDORCTBL
argument_list|(
literal|"nonAcidOrcTbl"
argument_list|)
block|,
name|NONACIDORCTBL2
argument_list|(
literal|"nonAcidOrcTbl2"
argument_list|)
block|,
name|NONACIDNONBUCKET
argument_list|(
literal|"nonAcidNonBucket"
argument_list|)
block|;
specifier|final
name|String
name|name
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
name|Table
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
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
name|setUpInternal
argument_list|()
expr_stmt|;
block|}
name|void
name|initHiveConf
parameter_list|()
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|void
name|setUpInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|initHiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|getWarehouseDir
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|,
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MERGE_CARDINALITY_VIOLATION_CHECK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTATSCOLAUTOGATHER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|prepDb
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|getWarehouseDir
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|getWarehouseDir
argument_list|()
argument_list|)
operator|.
name|mkdirs
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not create "
operator|+
name|getWarehouseDir
argument_list|()
argument_list|)
throw|;
block|}
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|start
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ss
operator|.
name|applyAuthorizationPolicy
argument_list|()
expr_stmt|;
name|d
operator|=
operator|new
name|Driver
argument_list|(
operator|new
name|QueryState
operator|.
name|Builder
argument_list|()
operator|.
name|withHiveConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|nonIsolated
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|d
operator|.
name|setMaxRows
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|dropTables
argument_list|()
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a int, b int) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|"(a int, b int) partitioned by (p string) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a int, b int) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDORCTBL2
operator|+
literal|"(a int, b int) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create temporary  table "
operator|+
name|Table
operator|.
name|ACIDTBL2
operator|+
literal|"(a int, b int, c int) clustered by (c) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDNONBUCKET
operator|+
literal|"(a int, b int) stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|dropTables
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|TxnCommandsBaseForTests
operator|.
name|Table
name|t
range|:
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|values
argument_list|()
control|)
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists "
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
if|if
condition|(
name|d
operator|!=
literal|null
condition|)
block|{
name|dropTables
argument_list|()
expr_stmt|;
name|d
operator|.
name|close
argument_list|()
expr_stmt|;
name|d
operator|.
name|destroy
argument_list|()
expr_stmt|;
name|d
operator|=
literal|null
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|getTestDataDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|getWarehouseDir
parameter_list|()
block|{
return|return
name|getTestDataDir
argument_list|()
operator|+
literal|"/warehouse"
return|;
block|}
specifier|abstract
name|String
name|getTestDataDir
parameter_list|()
function_decl|;
comment|/**    * takes raw data and turns it into a string as if from Driver.getResults()    * sorts rows in dictionary order    */
name|List
argument_list|<
name|String
argument_list|>
name|stringifyValues
parameter_list|(
name|int
index|[]
index|[]
name|rowsIn
parameter_list|)
block|{
return|return
name|TestTxnCommands2
operator|.
name|stringifyValues
argument_list|(
name|rowsIn
argument_list|)
return|;
block|}
name|String
name|makeValuesClause
parameter_list|(
name|int
index|[]
index|[]
name|rows
parameter_list|)
block|{
return|return
name|TestTxnCommands2
operator|.
name|makeValuesClause
argument_list|(
name|rows
argument_list|)
return|;
block|}
name|void
name|runWorker
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|MetaException
block|{
name|TestTxnCommands2
operator|.
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
name|void
name|runCleaner
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|MetaException
block|{
name|TestTxnCommands2
operator|.
name|runCleaner
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|runStatementOnDriver
parameter_list|(
name|String
name|stmt
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|cpr
init|=
name|d
operator|.
name|run
argument_list|(
name|stmt
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|stmt
operator|+
literal|" failed: "
operator|+
name|cpr
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|d
operator|.
name|getResults
argument_list|(
name|rs
argument_list|)
expr_stmt|;
return|return
name|rs
return|;
block|}
name|CommandProcessorResponse
name|runStatementOnDriverNegative
parameter_list|(
name|String
name|stmt
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|cpr
init|=
name|d
operator|.
name|run
argument_list|(
name|stmt
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
return|return
name|cpr
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Didn't get expected failure!"
argument_list|)
throw|;
block|}
comment|/**    * Runs Vectorized Explain on the query and checks if the plan is vectorized as expected    * @param vectorized {@code true} - assert that it's vectorized    */
name|void
name|assertVectorized
parameter_list|(
name|boolean
name|vectorized
parameter_list|,
name|String
name|query
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"EXPLAIN VECTORIZATION DETAIL "
operator|+
name|query
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|line
range|:
name|rs
control|)
block|{
if|if
condition|(
name|line
operator|!=
literal|null
operator|&&
name|line
operator|.
name|contains
argument_list|(
literal|"Execution mode: vectorized"
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Was vectorized when it wasn't expected"
argument_list|,
name|vectorized
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Din't find expected 'vectorized' in plan"
argument_list|,
operator|!
name|vectorized
argument_list|)
expr_stmt|;
block|}
comment|/**    * Will assert that actual files match expected.    * @param expectedFiles - suffixes of expected Paths.  Must be the same length    * @param rootPath - table or patition root where to start looking for actual files, recursively    */
name|void
name|assertExpectedFileSet
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|expectedFiles
parameter_list|,
name|String
name|rootPath
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|suffixLength
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|expectedFiles
control|)
block|{
if|if
condition|(
name|suffixLength
operator|>
literal|0
condition|)
block|{
assert|assert
name|suffixLength
operator|==
name|s
operator|.
name|length
argument_list|()
operator|:
literal|"all entries must be the same length. current: "
operator|+
name|s
assert|;
block|}
name|suffixLength
operator|=
name|s
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|actualFiles
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|remoteIterator
init|=
name|fs
operator|.
name|listFiles
argument_list|(
operator|new
name|Path
argument_list|(
name|rootPath
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|remoteIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LocatedFileStatus
name|lfs
init|=
name|remoteIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|lfs
operator|.
name|isDirectory
argument_list|()
operator|&&
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
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
operator|.
name|accept
argument_list|(
name|lfs
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|p
init|=
name|lfs
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|actualFiles
operator|.
name|add
argument_list|(
name|p
operator|.
name|substring
argument_list|(
name|p
operator|.
name|length
argument_list|()
operator|-
name|suffixLength
argument_list|,
name|p
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Unexpected file list"
argument_list|,
name|expectedFiles
argument_list|,
name|actualFiles
argument_list|)
expr_stmt|;
block|}
name|void
name|checkExpected
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|rs
parameter_list|,
name|String
index|[]
index|[]
name|expected
parameter_list|,
name|String
name|msg
parameter_list|,
name|Logger
name|LOG
parameter_list|,
name|boolean
name|checkFileName
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": read data("
operator|+
name|msg
operator|+
literal|"): "
argument_list|)
expr_stmt|;
name|logResult
argument_list|(
name|LOG
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": "
operator|+
name|msg
operator|+
literal|"; "
operator|+
name|rs
argument_list|,
name|expected
operator|.
name|length
argument_list|,
name|rs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|//verify data and layout
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Actual line (data) "
operator|+
name|i
operator|+
literal|" data: "
operator|+
name|rs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|rs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|startsWith
argument_list|(
name|expected
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkFileName
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Actual line(file) "
operator|+
name|i
operator|+
literal|" file: "
operator|+
name|rs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|rs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|endsWith
argument_list|(
name|expected
index|[
name|i
index|]
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|void
name|logResult
parameter_list|(
name|Logger
name|LOG
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|rs
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|rs
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|s
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * We have to use a different query to check results for Vectorized tests because to get the    * file name info we need to use {@link org.apache.hadoop.hive.ql.metadata.VirtualColumn#FILENAME}    * which will currently make the query non-vectorizable.  This means we can't check the file name    * for vectorized version of the test.    */
name|void
name|checkResult
parameter_list|(
name|String
index|[]
index|[]
name|expectedResult
parameter_list|,
name|String
name|query
parameter_list|,
name|boolean
name|isVectorized
parameter_list|,
name|String
name|msg
parameter_list|,
name|Logger
name|LOG
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|checkExpected
argument_list|(
name|rs
argument_list|,
name|expectedResult
argument_list|,
name|msg
operator|+
operator|(
name|isVectorized
condition|?
literal|" vect"
else|:
literal|""
operator|)
argument_list|,
name|LOG
argument_list|,
operator|!
name|isVectorized
argument_list|)
expr_stmt|;
name|assertVectorized
argument_list|(
name|isVectorized
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

