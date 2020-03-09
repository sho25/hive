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
name|conf
operator|.
name|MetastoreConf
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
name|shims
operator|.
name|Utils
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
name|AfterClass
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
name|HashMap
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
name|List
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
name|Map
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

begin_comment
comment|/**  * TestReplicationScenariosAcidTablesBase - base class for replication for ACID tables tests  */
end_comment

begin_class
specifier|public
class|class
name|BaseReplicationScenariosAcidTables
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
specifier|static
name|WarehouseInstance
name|primary
decl_stmt|;
specifier|static
name|WarehouseInstance
name|replica
decl_stmt|,
name|replicaNonAcid
decl_stmt|;
specifier|static
name|HiveConf
name|conf
decl_stmt|;
name|String
name|primaryDbName
decl_stmt|,
name|replicatedDbName
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|acidTableNames
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|nonAcidTableNames
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
name|void
name|internalBeforeClassSetup
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overrides
parameter_list|,
name|Class
name|clazz
parameter_list|)
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
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
name|acidEnableConf
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
name|put
argument_list|(
literal|"hive.strict.checks.bucketing"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.mapred.mode"
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.disallow.incompatible.col.type.changes"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.in.repl.test"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|acidEnableConf
operator|.
name|putAll
argument_list|(
name|overrides
argument_list|)
expr_stmt|;
name|primary
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|acidEnableConf
argument_list|)
expr_stmt|;
name|acidEnableConf
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
operator|.
name|getHiveName
argument_list|()
argument_list|,
name|primary
operator|.
name|repldDir
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
name|acidEnableConf
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
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|overridesForHiveConf1
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
operator|.
name|getHiveName
argument_list|()
argument_list|,
name|primary
operator|.
name|repldDir
argument_list|)
expr_stmt|;
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
name|String
name|primaryDbNameExtra
init|=
name|primaryDbName
operator|+
literal|"_extra"
decl_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|primaryDbNameExtra
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
name|primary
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|primaryDbName
operator|+
literal|"_extra cascade"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|prepareAcidData
parameter_list|(
name|String
name|primaryDbName
parameter_list|)
throws|throws
name|Throwable
block|{
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
literal|"create table t5 (id int) stored as orc tblproperties (\"transactional\"=\"true\")"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t5 values(1111), (2222), (3333)"
argument_list|)
expr_stmt|;
name|acidTableNames
operator|.
name|add
argument_list|(
literal|"t1"
argument_list|)
expr_stmt|;
name|acidTableNames
operator|.
name|add
argument_list|(
literal|"t2"
argument_list|)
expr_stmt|;
name|acidTableNames
operator|.
name|add
argument_list|(
literal|"t3"
argument_list|)
expr_stmt|;
name|acidTableNames
operator|.
name|add
argument_list|(
literal|"t5"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|prepareNonAcidData
parameter_list|(
name|String
name|primaryDbName
parameter_list|)
throws|throws
name|Throwable
block|{
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
literal|"create table t4 (id int)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t4 values(111), (222)"
argument_list|)
expr_stmt|;
name|nonAcidTableNames
operator|.
name|add
argument_list|(
literal|"t4"
argument_list|)
expr_stmt|;
block|}
name|WarehouseInstance
operator|.
name|Tuple
name|prepareDataAndDump
parameter_list|(
name|String
name|primaryDbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|withClause
parameter_list|)
throws|throws
name|Throwable
block|{
name|prepareAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
return|return
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
name|withClause
operator|!=
literal|null
condition|?
name|withClause
else|:
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|verifyNonAcidTableLoad
parameter_list|(
name|String
name|replicatedDbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
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
expr_stmt|;
block|}
specifier|private
name|void
name|verifyAcidTableLoad
parameter_list|(
name|String
name|replicatedDbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
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
name|void
name|verifyLoadExecution
parameter_list|(
name|String
name|replicatedDbName
parameter_list|,
name|String
name|lastReplId
parameter_list|,
name|boolean
name|includeAcid
parameter_list|)
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|(
name|nonAcidTableNames
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeAcid
condition|)
block|{
name|tableNames
operator|.
name|addAll
argument_list|(
name|acidTableNames
argument_list|)
expr_stmt|;
block|}
name|replica
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
name|tableNames
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
name|lastReplId
argument_list|)
operator|.
name|verifyReplTargetProperty
argument_list|(
name|replicatedDbName
argument_list|)
expr_stmt|;
name|verifyNonAcidTableLoad
argument_list|(
name|replicatedDbName
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeAcid
condition|)
block|{
name|verifyAcidTableLoad
argument_list|(
name|replicatedDbName
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|prepareIncAcidData
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t6 stored as orc tblproperties (\"transactional\"=\"true\")"
operator|+
literal|" as select * from t1"
argument_list|)
operator|.
name|run
argument_list|(
literal|"alter table t2 add columns (placetype string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"update t2 set placetype = 'city'"
argument_list|)
expr_stmt|;
name|acidTableNames
operator|.
name|add
argument_list|(
literal|"t6"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyIncAcidLoad
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t6 order by id"
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
literal|"select distinct placetype from t2"
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|"city"
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
specifier|private
name|void
name|runUsingDriver
parameter_list|(
name|IDriver
name|driver
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|Throwable
block|{
name|driver
operator|.
name|run
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
name|void
name|prepareInc2AcidData
parameter_list|(
name|String
name|dbName
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Throwable
block|{
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"insert into t1 values (3)"
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"insert into t5 values (4444)"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyInc2AcidLoad
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"select id from t6 order by id"
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
literal|"select distinct placetype from t2"
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|"city"
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
block|,
literal|"3"
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
block|,
literal|"4444"
block|}
argument_list|)
expr_stmt|;
block|}
name|void
name|prepareIncNonAcidData
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t4 values (333)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t7 (str string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t7 values ('aaa')"
argument_list|)
expr_stmt|;
name|nonAcidTableNames
operator|.
name|add
argument_list|(
literal|"t7"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyIncNonAcidLoad
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"select * from t4 order by id"
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
block|,
literal|"333"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select * from t7"
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|"aaa"
argument_list|)
expr_stmt|;
block|}
name|void
name|prepareInc2NonAcidData
parameter_list|(
name|String
name|dbName
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Throwable
block|{
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"insert into t4 values (444)"
argument_list|)
expr_stmt|;
name|runUsingDriver
argument_list|(
name|driver
argument_list|,
literal|"insert into t7 values ('bbb')"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyInc2NonAcidLoad
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"select * from t4 order by id"
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
block|,
literal|"333"
block|,
literal|"444"
block|}
argument_list|)
operator|.
name|run
argument_list|(
literal|"select * from t7"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"aaa"
block|,
literal|"bbb"
block|}
argument_list|)
expr_stmt|;
block|}
name|void
name|verifyIncLoad
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|lastReplId
parameter_list|)
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|(
name|nonAcidTableNames
argument_list|)
decl_stmt|;
name|tableNames
operator|.
name|addAll
argument_list|(
name|acidTableNames
argument_list|)
expr_stmt|;
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
operator|.
name|verifyResults
argument_list|(
name|tableNames
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|dbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|lastReplId
argument_list|)
operator|.
name|verifyReplTargetProperty
argument_list|(
name|replicatedDbName
argument_list|)
expr_stmt|;
name|verifyIncNonAcidLoad
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|verifyIncAcidLoad
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
name|void
name|verifyInc2Load
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|lastReplId
parameter_list|)
throws|throws
name|Throwable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|(
name|nonAcidTableNames
argument_list|)
decl_stmt|;
name|tableNames
operator|.
name|addAll
argument_list|(
name|acidTableNames
argument_list|)
expr_stmt|;
name|replica
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
operator|.
name|verifyResults
argument_list|(
name|tableNames
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|dbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|lastReplId
argument_list|)
operator|.
name|verifyReplTargetProperty
argument_list|(
name|replicatedDbName
argument_list|)
expr_stmt|;
name|verifyInc2NonAcidLoad
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|verifyInc2AcidLoad
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Long
argument_list|>
name|openTxns
parameter_list|(
name|int
name|numTxns
parameter_list|,
name|TxnStore
name|txnHandler
parameter_list|,
name|HiveConf
name|primaryConf
parameter_list|)
throws|throws
name|Throwable
block|{
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
name|numTxns
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
name|numTxns
operator|-
literal|1
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
name|numTxns
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
return|return
name|txns
return|;
block|}
name|void
name|allocateWriteIdsForTables
parameter_list|(
name|String
name|primaryDbName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tables
parameter_list|,
name|TxnStore
name|txnHandler
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|txns
parameter_list|,
name|HiveConf
name|primaryConf
parameter_list|)
throws|throws
name|Throwable
block|{
name|AllocateTableWriteIdsRequest
name|rqst
init|=
operator|new
name|AllocateTableWriteIdsRequest
argument_list|()
decl_stmt|;
name|rqst
operator|.
name|setDbName
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|tables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|rqst
operator|.
name|setTableName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
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
block|}
name|verifyWriteIdsForTables
argument_list|(
name|tables
argument_list|,
name|primaryConf
argument_list|,
name|primaryDbName
argument_list|)
expr_stmt|;
block|}
name|void
name|verifyWriteIdsForTables
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tables
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|)
throws|throws
name|Throwable
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|tables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|conf
argument_list|,
literal|"select * from TXN_TO_WRITE_ID"
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|conf
argument_list|,
literal|"select count(*) from TXN_TO_WRITE_ID where t2w_database = '"
operator|+
name|dbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and t2w_table = '"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|verifyAllOpenTxnsAborted
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|txns
parameter_list|,
name|HiveConf
name|primaryConf
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|numTxns
init|=
name|txns
operator|.
name|size
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
name|numTxns
operator|-
literal|1
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
name|numTxns
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
block|}
name|void
name|verifyNextId
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tables
parameter_list|,
name|String
name|dbName
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// Verify the next write id
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|tables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
index|[]
name|nextWriteId
init|=
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|conf
argument_list|,
literal|"select nwi_next from NEXT_WRITE_ID where  nwi_database = '"
operator|+
name|dbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' and nwi_table = '"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"'"
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
name|entry
operator|.
name|getValue
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|verifyCompactionQueue
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tables
parameter_list|,
name|String
name|dbName
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Throwable
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|tables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnDbUtil
operator|.
name|queryToString
argument_list|(
name|conf
argument_list|,
literal|"select * from COMPACTION_QUEUE"
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|TxnDbUtil
operator|.
name|countQueryAgent
argument_list|(
name|conf
argument_list|,
literal|"select count(*) from COMPACTION_QUEUE where cq_database = '"
operator|+
name|dbName
operator|+
literal|"' and cq_table = '"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

