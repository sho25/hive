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
operator|.
name|parse
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
name|*
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
name|Map
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|FSDataInputStream
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
name|hive_metastoreConstants
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
name|Context
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
name|QueryPlan
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
name|exec
operator|.
name|ExplainTask
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
name|orc
operator|.
name|OrcInputFormat
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
name|orc
operator|.
name|OrcOutputFormat
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
name|metadata
operator|.
name|HiveException
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
name|Table
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
name|ExplainWork
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
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|TestUpdateDeleteSemanticAnalyzer
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestUpdateDeleteSemanticAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Hive
name|db
decl_stmt|;
comment|// All of the insert, update, and delete tests assume two tables, T and U, each with columns a,
comment|// and b.  U it partitioned by an additional column ds.  These are created by parseAndAnalyze
comment|// and removed by cleanupTables().
annotation|@
name|Test
specifier|public
name|void
name|testInsertSelect
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"insert into table T select a, b from U"
argument_list|,
literal|"testInsertSelect"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteAllNonPartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from T"
argument_list|,
literal|"testDeleteAllNonPartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteWhereNoPartition
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from T where a> 5"
argument_list|,
literal|"testDeleteWhereNoPartition"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteAllPartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from U"
argument_list|,
literal|"testDeleteAllPartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteAllWherePartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from U where a> 5"
argument_list|,
literal|"testDeleteAllWherePartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteOnePartition
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from U where ds = 'today'"
argument_list|,
literal|"testDeleteFromPartitionOnly"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteOnePartitionWhere
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"delete from U where ds = 'today' and a> 5"
argument_list|,
literal|"testDeletePartitionWhere"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateAllNonPartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update T set b = 5"
argument_list|,
literal|"testUpdateAllNonPartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateAllNonPartitionedWhere
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update T set b = 5 where b> 5"
argument_list|,
literal|"testUpdateAllNonPartitionedWhere"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateAllPartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update U set b = 5"
argument_list|,
literal|"testUpdateAllPartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateAllPartitionedWhere
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update U set b = 5 where b> 5"
argument_list|,
literal|"testUpdateAllPartitionedWhere"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateOnePartition
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update U set b = 5 where ds = 'today'"
argument_list|,
literal|"testUpdateOnePartition"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateOnePartitionWhere
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"update U set b = 5 where ds = 'today' and b> 5"
argument_list|,
literal|"testUpdateOnePartitionWhere"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertValues
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"insert into table T values ('abc', 3), ('ghi', null)"
argument_list|,
literal|"testInsertValues"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertValuesPartitioned
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ReturnInfo
name|rc
init|=
name|parseAndAnalyze
argument_list|(
literal|"insert into table U partition (ds) values "
operator|+
literal|"('abc', 3, 'today'), ('ghi', 5, 'tomorrow')"
argument_list|,
literal|"testInsertValuesPartitioned"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|explain
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|rc
operator|.
name|sem
argument_list|,
name|rc
operator|.
name|plan
argument_list|,
name|rc
operator|.
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupTables
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONINGMODE
argument_list|,
literal|"nonstrict"
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
name|HIVE_TXN_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|cleanupTables
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
name|db
operator|!=
literal|null
condition|)
block|{
name|db
operator|.
name|dropTable
argument_list|(
literal|"T"
argument_list|)
expr_stmt|;
name|db
operator|.
name|dropTable
argument_list|(
literal|"U"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|ReturnInfo
block|{
name|ASTNode
name|ast
decl_stmt|;
name|BaseSemanticAnalyzer
name|sem
decl_stmt|;
name|QueryPlan
name|plan
decl_stmt|;
name|ReturnInfo
parameter_list|(
name|ASTNode
name|a
parameter_list|,
name|BaseSemanticAnalyzer
name|s
parameter_list|,
name|QueryPlan
name|p
parameter_list|)
block|{
name|ast
operator|=
name|a
expr_stmt|;
name|sem
operator|=
name|s
expr_stmt|;
name|plan
operator|=
name|p
expr_stmt|;
block|}
block|}
specifier|private
name|ReturnInfo
name|parseAndAnalyze
parameter_list|(
name|String
name|query
parameter_list|,
name|String
name|testName
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParseException
throws|,
name|HiveException
block|{
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Context
name|ctx
init|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCmd
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setHDFSCleanup
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|tree
init|=
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|tree
operator|=
name|ParseUtils
operator|.
name|findRootNonNullToken
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|tree
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|initTxnMgr
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|db
operator|=
name|sem
operator|.
name|getDb
argument_list|()
expr_stmt|;
comment|// I have to create the tables here (rather than in setup()) because I need the Hive
comment|// connection, which is conveniently created by the semantic analyzer.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|db
operator|.
name|createTable
argument_list|(
literal|"T"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
argument_list|,
literal|null
argument_list|,
name|OrcInputFormat
operator|.
name|class
argument_list|,
name|OrcOutputFormat
operator|.
name|class
argument_list|,
literal|2
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|db
operator|.
name|createTable
argument_list|(
literal|"U"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"ds"
argument_list|)
argument_list|,
name|OrcInputFormat
operator|.
name|class
argument_list|,
name|OrcOutputFormat
operator|.
name|class
argument_list|,
literal|2
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|Table
name|u
init|=
name|db
operator|.
name|getTable
argument_list|(
literal|"U"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partVals
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|partVals
operator|.
name|put
argument_list|(
literal|"ds"
argument_list|,
literal|"yesterday"
argument_list|)
expr_stmt|;
name|db
operator|.
name|createPartition
argument_list|(
name|u
argument_list|,
name|partVals
argument_list|)
expr_stmt|;
name|partVals
operator|.
name|clear
argument_list|()
expr_stmt|;
name|partVals
operator|.
name|put
argument_list|(
literal|"ds"
argument_list|,
literal|"today"
argument_list|)
expr_stmt|;
name|db
operator|.
name|createPartition
argument_list|(
name|u
argument_list|,
name|partVals
argument_list|)
expr_stmt|;
name|sem
operator|.
name|analyze
argument_list|(
name|tree
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
comment|// validate the plan
name|sem
operator|.
name|validate
argument_list|()
expr_stmt|;
name|QueryPlan
name|plan
init|=
operator|new
name|QueryPlan
argument_list|(
name|query
argument_list|,
name|sem
argument_list|,
literal|0L
argument_list|,
name|testName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
operator|new
name|ReturnInfo
argument_list|(
name|tree
argument_list|,
name|sem
argument_list|,
name|plan
argument_list|)
return|;
block|}
specifier|private
name|String
name|explain
parameter_list|(
name|SemanticAnalyzer
name|sem
parameter_list|,
name|QueryPlan
name|plan
parameter_list|,
name|String
name|astStringTree
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|File
name|f
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"TestSemanticAnalyzer"
argument_list|,
literal|"explain"
argument_list|)
decl_stmt|;
name|Path
name|tmp
init|=
operator|new
name|Path
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|create
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
name|ExplainWork
name|work
init|=
operator|new
name|ExplainWork
argument_list|(
name|tmp
argument_list|,
name|sem
operator|.
name|getParseContext
argument_list|()
argument_list|,
name|sem
operator|.
name|getRootTasks
argument_list|()
argument_list|,
name|sem
operator|.
name|getFetchTask
argument_list|()
argument_list|,
name|astStringTree
argument_list|,
name|sem
argument_list|,
literal|true
argument_list|,
literal|false
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
argument_list|)
decl_stmt|;
name|ExplainTask
name|task
init|=
operator|new
name|ExplainTask
argument_list|()
decl_stmt|;
name|task
operator|.
name|setWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|task
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|plan
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|task
operator|.
name|execute
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|tmp
argument_list|)
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|final
name|int
name|bufSz
init|=
literal|4096
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|bufSz
index|]
decl_stmt|;
name|long
name|pos
init|=
literal|0L
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|bytesRead
init|=
name|in
operator|.
name|read
argument_list|(
name|pos
argument_list|,
name|buf
argument_list|,
literal|0
argument_list|,
name|bufSz
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytesRead
operator|>
literal|0
condition|)
block|{
name|pos
operator|+=
name|bytesRead
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
operator|new
name|String
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|bytesRead
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Reached end of file
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"pfile:/.*\n"
argument_list|,
literal|"pfile:MASKED-OUT\n"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"location file:/.*\n"
argument_list|,
literal|"location file:MASKED-OUT\n"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"file:/.*\n"
argument_list|,
literal|"file:MASKED-OUT\n"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"transient_lastDdlTime.*\n"
argument_list|,
literal|"transient_lastDdlTime MASKED-OUT\n"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

