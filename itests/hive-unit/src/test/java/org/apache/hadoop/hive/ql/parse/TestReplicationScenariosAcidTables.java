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
name|parse
package|;
end_package

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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|AllocateTableWriteIdsRequest
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
name|AllocateTableWriteIdsResponse
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
name|OpenTxnRequest
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
name|OpenTxnsResponse
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
name|shims
operator|.
name|Utils
import|;
end_import

begin_import
import|import static
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
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
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
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|BeforeClass
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

begin_comment
comment|/**  * TestReplicationScenariosAcidTables - test replication for ACID tables  */
end_comment

begin_class
specifier|public
class|class
name|TestReplicationScenariosAcidTables
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestRule
name|replV1BackwardCompat
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReplicationScenarios
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|WarehouseInstance
name|primary
decl_stmt|,
name|replica
decl_stmt|,
name|replicaNonAcid
decl_stmt|;
specifier|private
name|String
name|primaryDbName
decl_stmt|,
name|replicatedDbName
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classLevelSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.use.datanode.hostname"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser."
operator|+
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
operator|+
literal|".hosts"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|miniDFSCluster
init|=
operator|new
name|MiniDFSCluster
operator|.
name|Builder
argument_list|(
name|conf
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|1
argument_list|)
operator|.
name|format
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overridesForHiveConf
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|miniDFSCluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.txn.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.repl.dump.include.acid.tables"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.repl.bootstrap.dump.open.txn.timeout"
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|primary
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|overridesForHiveConf
argument_list|)
expr_stmt|;
name|replica
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|overridesForHiveConf
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overridesForHiveConf1
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|miniDFSCluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.txn.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.repl.dump.include.acid.tables"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|replicaNonAcid
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|overridesForHiveConf1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|classLevelTearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|primary
operator|.
name|close
argument_list|()
expr_stmt|;
name|replica
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Throwable
block|{
name|replV1BackwardCompat
operator|=
name|primary
operator|.
name|getReplivationV1CompatRule
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|primaryDbName
operator|=
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"_"
operator|+
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|replicatedDbName
operator|=
literal|"replicated_"
operator|+
name|primaryDbName
expr_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|primaryDbName
operator|+
literal|" WITH DBPROPERTIES ( '"
operator|+
name|SOURCE_OF_REPLICATION
operator|+
literal|"' = '1,2,3')"
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
name|Throwable
block|{
name|primary
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|primaryDbName
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|replica
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|replicatedDbName
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|replicaNonAcid
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|replicatedDbName
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAcidTablesBootstrap
parameter_list|()
throws|throws
name|Throwable
block|{
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t1 (id int) clustered by(id) into 3 buckets stored as orc "
operator|+
literal|"tblproperties (\"transactional\"=\"true\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t1 values(1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t1 values(2)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t2 (place string) partitioned by (country string) clustered by(place) "
operator|+
literal|"into 3 buckets stored as orc tblproperties (\"transactional\"=\"true\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t2 partition(country='india') values ('bangalore')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t2 partition(country='us') values ('austin')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t2 partition(country='france') values ('paris')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"alter table t2 add partition(country='italy')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t3 (rank int) tblproperties(\"transactional\"=\"true\", "
operator|+
literal|"\"transactional_properties\"=\"insert_only\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t3 values(11)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t3 values(22)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t4 (id int)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t4 values(111), (222)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t5 (id int) stored as orc "
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t5 values(1111), (2222)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"alter table t5 set tblproperties (\"transactional\"=\"true\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t5 values(3333)"
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"t1"
block|,
literal|"t2"
block|,
literal|"t3"
block|,
literal|"t4"
block|,
literal|"t5"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t1 order by id"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"1"
block|,
literal|"2"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select country from t2 order by country"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"france"
block|,
literal|"india"
block|,
literal|"us"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select rank from t3 order by rank"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"11"
block|,
literal|"22"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t4 order by id"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"111"
block|,
literal|"222"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t5 order by id"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"1111"
block|,
literal|"2222"
block|,
literal|"3333"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAcidTablesBootstrapWithOpenTxnsTimeout
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Open 5 txns
name|HiveConf
name|primaryConf
init|=
name|primary
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|TxnStore
name|txnHandler
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|primary
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|OpenTxnsResponse
name|otResp
init|=
name|txnHandler
operator|.
name|openTxns
argument_list|(
operator|new
name|OpenTxnRequest
argument_list|(
literal|5
argument_list|,
literal|"u1"
argument_list|,
literal|"localhost"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|txns
init|=
name|otResp
operator|.
name|getTxn_ids
argument_list|()
decl_stmt|;
name|String
name|txnIdRange
init|=
literal|" txn_id>= "
operator|+
name|txns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|+
literal|" and txn_id<= "
operator|+
name|txns
operator|.
name|get
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select * from TXNS"
argument_list|)
argument_list|,
literal|5
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|primaryConf
argument_list|,
literal|"select count(*) from TXNS where txn_state = 'o' and "
operator|+
name|txnIdRange
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create 2 tables, one partitioned and other not. Also, have both types of full ACID and MM tables.
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t1 (id int) clustered by(id) into 3 buckets stored as orc "
operator|+
literal|"tblproperties (\"transactional\"=\"true\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t1 values(1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t2 (rank int) partitioned by (name string) tblproperties(\"transactional\"=\"true\", "
operator|+
literal|"\"transactional_properties\"=\"insert_only\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t2 partition(name='Bob') values(11)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t2 partition(name='Carl') values(10)"
argument_list|)
expr_stmt|;
comment|// Allocate write ids for both tables t1 and t2 for all txns
comment|// t1=5+1(insert) and t2=5+2(insert)
name|AllocateTableWriteIdsRequest
name|rqst
init|=
operator|new
name|AllocateTableWriteIdsRequest
argument_list|(
name|primaryDbName
argument_list|,
literal|"t1"
argument_list|)
decl_stmt|;
name|rqst
operator|.
name|setTxnIds
argument_list|(
name|txns
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|allocateTableWriteIds
argument_list|(
name|rqst
argument_list|)
expr_stmt|;
name|rqst
operator|.
name|setTableName
argument_list|(
literal|"t2"
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|allocateTableWriteIds
argument_list|(
name|rqst
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select * from TXN_TO_WRITE_ID"
argument_list|)
argument_list|,
literal|6
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|primaryConf
argument_list|,
literal|"select count(*) from TXN_TO_WRITE_ID where t2w_database = '"
operator|+
name|primaryDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and t2w_table = 't1'"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select * from TXN_TO_WRITE_ID"
argument_list|)
argument_list|,
literal|7
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|primaryConf
argument_list|,
literal|"select count(*) from TXN_TO_WRITE_ID where t2w_database = '"
operator|+
name|primaryDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and t2w_table = 't2'"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Bootstrap dump with open txn timeout as 1s.
name|List
argument_list|<
name|String
argument_list|>
name|withConfigs
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"'hive.repl.bootstrap.dump.open.txn.timeout'='1s'"
argument_list|)
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|withConfigs
argument_list|)
decl_stmt|;
comment|// After bootstrap dump, all the opened txns should be aborted. Verify it.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select * from TXNS"
argument_list|)
argument_list|,
literal|0
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|primaryConf
argument_list|,
literal|"select count(*) from TXNS where txn_state = 'o' and "
operator|+
name|txnIdRange
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select * from TXNS"
argument_list|)
argument_list|,
literal|5
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|primaryConf
argument_list|,
literal|"select count(*) from TXNS where txn_state = 'a' and "
operator|+
name|txnIdRange
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify the next write id
name|String
index|[]
name|nextWriteId
init|=
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select nwi_next from NEXT_WRITE_ID where "
operator|+
literal|" nwi_database = '"
operator|+
name|primaryDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and nwi_table = 't1'"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|nextWriteId
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|nextWriteId
operator|=
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|primaryConf
argument_list|,
literal|"select nwi_next from NEXT_WRITE_ID where "
operator|+
literal|" nwi_database = '"
operator|+
name|primaryDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and nwi_table = 't2'"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|nextWriteId
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|,
literal|8L
argument_list|)
expr_stmt|;
comment|// Bootstrap load which should also replicate the aborted write ids on both tables.
name|HiveConf
name|replicaConf
init|=
name|replica
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"t1"
block|,
literal|"t2"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t1"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"1"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select rank from t2 order by rank"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"10"
block|,
literal|"11"
block|}
argument_list|)
expr_stmt|;
comment|// Verify if HWM is properly set after REPL LOAD
name|nextWriteId
operator|=
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select nwi_next from NEXT_WRITE_ID where "
operator|+
literal|" nwi_database = '"
operator|+
name|replicatedDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and nwi_table = 't1'"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|nextWriteId
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|nextWriteId
operator|=
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select nwi_next from NEXT_WRITE_ID where "
operator|+
literal|" nwi_database = '"
operator|+
name|replicatedDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and nwi_table = 't2'"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|nextWriteId
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|,
literal|8L
argument_list|)
expr_stmt|;
comment|// Verify if all the aborted write ids are replicated to the replicated DB
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select * from TXN_TO_WRITE_ID"
argument_list|)
argument_list|,
literal|5
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|replicaConf
argument_list|,
literal|"select count(*) from TXN_TO_WRITE_ID where t2w_database = '"
operator|+
name|replicatedDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and t2w_table = 't1'"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select * from TXN_TO_WRITE_ID"
argument_list|)
argument_list|,
literal|5
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|replicaConf
argument_list|,
literal|"select count(*) from TXN_TO_WRITE_ID where t2w_database = '"
operator|+
name|replicatedDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and t2w_table = 't2'"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify if entries added in COMPACTION_QUEUE for each table/partition
comment|// t1-> 1 entry and t2-> 2 entries (1 per partition)
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select * from COMPACTION_QUEUE"
argument_list|)
argument_list|,
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|replicaConf
argument_list|,
literal|"select count(*) from COMPACTION_QUEUE where cq_database = '"
operator|+
name|replicatedDbName
operator|+
literal|"' and cq_table = 't1'"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|replicaConf
argument_list|,
literal|"select * from COMPACTION_QUEUE"
argument_list|)
argument_list|,
literal|2
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|replicaConf
argument_list|,
literal|"select count(*) from COMPACTION_QUEUE where cq_database = '"
operator|+
name|replicatedDbName
operator|+
literal|"' and cq_table = 't2'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOpenTxnEvent
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|tableName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// create table will start and coomit the transaction
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
literal|" (key int, value int) PARTITIONED BY (load_date date) "
operator|+
literal|"CLUSTERED BY(key) INTO 3 BUCKETS STORED AS ORC TBLPROPERTIES ('transactional'='true')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW TABLES LIKE '"
operator|+
name|tableName
operator|+
literal|"'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|tableName
argument_list|)
operator|.
name|run
argument_list|(
literal|"INSERT INTO "
operator|+
name|tableName
operator|+
literal|" partition (load_date='2016-03-01') VALUES (1, 1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"select key from "
operator|+
name|tableName
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Test the idempotent behavior of Open and Commit Txn
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbortTxnEvent
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|tableName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|String
name|tableNameFail
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"Fail"
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// this should fail
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableNameFail
operator|+
literal|" (key int, value int) PARTITIONED BY (load_date date) "
operator|+
literal|"CLUSTERED BY(key) ('transactional'='true')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW TABLES LIKE '"
operator|+
name|tableNameFail
operator|+
literal|"'"
argument_list|)
operator|.
name|verifyFailure
argument_list|(
operator|new
name|String
index|[]
block|{
name|tableNameFail
block|}
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Test the idempotent behavior of Abort Txn
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxnEventNonAcid
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|tableName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replicaNonAcid
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
literal|" (key int, value int) PARTITIONED BY (load_date date) "
operator|+
literal|"CLUSTERED BY(key) INTO 3 BUCKETS STORED AS ORC TBLPROPERTIES ('transactional'='true')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW TABLES LIKE '"
operator|+
name|tableName
operator|+
literal|"'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|tableName
argument_list|)
operator|.
name|run
argument_list|(
literal|"INSERT INTO "
operator|+
name|tableName
operator|+
literal|" partition (load_date='2016-03-01') VALUES (1, 1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"select key from "
operator|+
name|tableName
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replicaNonAcid
operator|.
name|loadFailure
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

