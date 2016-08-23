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
name|hadoop
operator|.
name|hive
operator|.
name|ql
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
name|hive
operator|.
name|common
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
name|ShowCompactRequest
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
name|ShowCompactResponse
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
name|metastore
operator|.
name|txn
operator|.
name|TxnStore
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
name|TxnUtils
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|txn
operator|.
name|compactor
operator|.
name|Cleaner
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
name|txn
operator|.
name|compactor
operator|.
name|Worker
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
name|Test
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

begin_comment
comment|/**  * This class resides in itests to facilitate running query using Tez engine, since the jars are  * fully loaded here, which is not the case if it stays in ql.  */
end_comment

begin_class
specifier|public
class|class
name|TestAcidOnTez
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestAcidOnTez
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_WAREHOUSE_DIR
init|=
name|TEST_DATA_DIR
operator|+
literal|"/warehouse"
decl_stmt|;
comment|//bucket count for test tables; set it to 1 for easier debugging
specifier|private
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
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Driver
name|d
decl_stmt|;
specifier|private
specifier|static
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
name|NONACIDORCTBL
argument_list|(
literal|"nonAcidOrcTbl"
argument_list|)
block|,
name|NONACIDPART
argument_list|(
literal|"nonAcidPart"
argument_list|)
block|;
specifier|private
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
name|tearDown
argument_list|()
expr_stmt|;
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
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
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
name|TEST_WAREHOUSE_DIR
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
name|HIVEMAPREDMODE
argument_list|,
literal|"nonstrict"
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
argument_list|()
expr_stmt|;
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
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
name|TEST_WAREHOUSE_DIR
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
name|TEST_WAREHOUSE_DIR
argument_list|)
throw|;
block|}
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|SessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
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
literal|" buckets stored as orc "
operator|+
name|getTblProperties
argument_list|()
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
literal|" buckets stored as orc "
operator|+
name|getTblProperties
argument_list|()
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
literal|" buckets stored as orc "
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|"(a int, b int) partitioned by (p string) stored as orc "
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) values(1,2)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) values(3,4)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) values(5,6)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) values(7,8)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) values(9,10)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a,b) values(1,2),(3,4),(5,6),(7,8),(9,10)"
argument_list|)
expr_stmt|;
block|}
comment|/**    * this is to test differety types of Acid tables    */
name|String
name|getTblProperties
parameter_list|()
block|{
return|return
literal|"TBLPROPERTIES ('transactional'='true')"
return|;
block|}
specifier|private
name|void
name|dropTables
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Table
name|t
range|:
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
name|destroy
argument_list|()
expr_stmt|;
name|d
operator|.
name|close
argument_list|()
expr_stmt|;
name|d
operator|=
literal|null
expr_stmt|;
block|}
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeJoinOnMR
parameter_list|()
throws|throws
name|Exception
block|{
name|testJoin
argument_list|(
literal|"mr"
argument_list|,
literal|"MergeJoin"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapJoinOnMR
parameter_list|()
throws|throws
name|Exception
block|{
name|testJoin
argument_list|(
literal|"mr"
argument_list|,
literal|"MapJoin"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeJoinOnTez
parameter_list|()
throws|throws
name|Exception
block|{
name|testJoin
argument_list|(
literal|"tez"
argument_list|,
literal|"MergeJoin"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapJoinOnTez
parameter_list|()
throws|throws
name|Exception
block|{
name|testJoin
argument_list|(
literal|"tez"
argument_list|,
literal|"MapJoin"
argument_list|)
expr_stmt|;
block|}
comment|// Ideally test like this should be a qfile test. However, the explain output from qfile is always
comment|// slightly different depending on where the test is run, specifically due to file size estimation
specifier|private
name|void
name|testJoin
parameter_list|(
name|String
name|engine
parameter_list|,
name|String
name|joinType
parameter_list|)
throws|throws
name|Exception
block|{
name|HiveConf
name|confForTez
init|=
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|// make a clone of existing hive conf
name|HiveConf
name|confForMR
init|=
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|// make a clone of existing hive conf
if|if
condition|(
name|engine
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
name|setupTez
argument_list|(
name|confForTez
argument_list|)
expr_stmt|;
comment|// one-time setup to make query able to run with Tez
block|}
if|if
condition|(
name|joinType
operator|.
name|equals
argument_list|(
literal|"MapJoin"
argument_list|)
condition|)
block|{
name|setupMapJoin
argument_list|(
name|confForTez
argument_list|)
expr_stmt|;
name|setupMapJoin
argument_list|(
name|confForMR
argument_list|)
expr_stmt|;
block|}
name|runQueries
argument_list|(
name|engine
argument_list|,
name|joinType
argument_list|,
name|confForTez
argument_list|,
name|confForMR
argument_list|)
expr_stmt|;
comment|// Perform compaction. Join result after compaction should still be the same
name|runStatementOnDriver
argument_list|(
literal|"alter table "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" compact 'MAJOR'"
argument_list|)
expr_stmt|;
name|TestTxnCommands2
operator|.
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|TxnStore
name|txnHandler
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ShowCompactResponse
name|resp
init|=
name|txnHandler
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Unexpected number of compactions in history"
argument_list|,
literal|1
argument_list|,
name|resp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Unexpected 0 compaction state"
argument_list|,
name|TxnStore
operator|.
name|CLEANING_RESPONSE
argument_list|,
name|resp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|TestTxnCommands2
operator|.
name|runCleaner
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|runQueries
argument_list|(
name|engine
argument_list|,
name|joinType
argument_list|,
name|confForTez
argument_list|,
name|confForMR
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runQueries
parameter_list|(
name|String
name|engine
parameter_list|,
name|String
name|joinType
parameter_list|,
name|HiveConf
name|confForTez
parameter_list|,
name|HiveConf
name|confForMR
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
literal|"select count(*) from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" t1 join "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" t2 on t1.a=t2.a"
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
literal|"select count(*) from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" t1 join "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" t2 on t1.a=t2.a"
argument_list|)
expr_stmt|;
comment|// more queries can be added here in the future to test acid joins
name|List
argument_list|<
name|String
argument_list|>
name|explain
decl_stmt|;
comment|// stores Explain output
name|int
index|[]
index|[]
name|expected
init|=
block|{
block|{
literal|5
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|query
range|:
name|queries
control|)
block|{
if|if
condition|(
name|engine
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
name|explain
operator|=
name|runStatementOnDriver
argument_list|(
literal|"explain "
operator|+
name|query
argument_list|,
name|confForTez
argument_list|)
expr_stmt|;
if|if
condition|(
name|joinType
operator|.
name|equals
argument_list|(
literal|"MergeJoin"
argument_list|)
condition|)
block|{
name|TestTxnCommands2
operator|.
name|assertExplainHasString
argument_list|(
literal|"Merge Join Operator"
argument_list|,
name|explain
argument_list|,
literal|"Didn't find "
operator|+
name|joinType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// MapJoin
name|TestTxnCommands2
operator|.
name|assertExplainHasString
argument_list|(
literal|"Map Join Operator"
argument_list|,
name|explain
argument_list|,
literal|"Didn't find "
operator|+
name|joinType
argument_list|)
expr_stmt|;
block|}
name|rs
operator|=
name|runStatementOnDriver
argument_list|(
name|query
argument_list|,
name|confForTez
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// mr
name|explain
operator|=
name|runStatementOnDriver
argument_list|(
literal|"explain "
operator|+
name|query
argument_list|,
name|confForMR
argument_list|)
expr_stmt|;
if|if
condition|(
name|joinType
operator|.
name|equals
argument_list|(
literal|"MergeJoin"
argument_list|)
condition|)
block|{
name|TestTxnCommands2
operator|.
name|assertExplainHasString
argument_list|(
literal|"  Join Operator"
argument_list|,
name|explain
argument_list|,
literal|"Didn't find "
operator|+
name|joinType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// MapJoin
name|TestTxnCommands2
operator|.
name|assertExplainHasString
argument_list|(
literal|"Map Join Operator"
argument_list|,
name|explain
argument_list|,
literal|"Didn't find "
operator|+
name|joinType
argument_list|)
expr_stmt|;
block|}
name|rs
operator|=
name|runStatementOnDriver
argument_list|(
name|query
argument_list|,
name|confForMR
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Join result incorrect"
argument_list|,
name|TestTxnCommands2
operator|.
name|stringifyValues
argument_list|(
name|expected
argument_list|)
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setupTez
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|,
literal|"tez"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_USER_INSTALL_DIR
argument_list|,
name|TEST_DATA_DIR
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"tez.local.mode"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
literal|"file:///"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"tez.runtime.optimize.local.fetch"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"tez.staging-dir"
argument_list|,
name|TEST_DATA_DIR
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"tez.ignore.lib.uris"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupMapJoin
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOIN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
block|}
specifier|private
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
comment|/**    * Run statement with customized hive conf    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|runStatementOnDriver
parameter_list|(
name|String
name|stmt
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|CommandProcessorResponse
name|cpr
init|=
name|driver
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
name|driver
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
block|}
end_class

end_unit

