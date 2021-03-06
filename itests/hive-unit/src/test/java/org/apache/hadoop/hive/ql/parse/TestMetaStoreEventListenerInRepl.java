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
name|events
operator|.
name|AlterDatabaseEvent
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
name|events
operator|.
name|AlterTableEvent
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
name|events
operator|.
name|CreateDatabaseEvent
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
name|events
operator|.
name|CreateTableEvent
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
name|events
operator|.
name|DropTableEvent
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
name|HashSet
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
name|Set
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
comment|/**  * TestMetaStoreEventListenerInRepl - Test metastore events created by replication.  */
end_comment

begin_class
specifier|public
class|class
name|TestMetaStoreEventListenerInRepl
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
name|TestMetaStoreEventListenerInRepl
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
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|internalBeforeClassSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|TestMetaStoreEventListenerInRepl
operator|.
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestMetaStoreEventListenerInRepl
operator|.
name|class
argument_list|)
expr_stmt|;
name|TestMetaStoreEventListenerInRepl
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.use.datanode.hostname"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|TestMetaStoreEventListenerInRepl
operator|.
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
name|TestMetaStoreEventListenerInRepl
operator|.
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
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
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|EVENT_LISTENERS
operator|.
name|getVarname
argument_list|()
argument_list|,
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|conf
argument_list|)
expr_stmt|;
name|conf
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
name|conf
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
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|prepareBootstrapData
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
literal|"create table t4 (id int)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t4 values(111), (222)"
argument_list|)
expr_stmt|;
comment|// Add expected events with associated tables, if any.
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|eventsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|CreateDatabaseEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Replication causes many implicit alter database operations, so metastore will see some
comment|// alter table events as well.
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterDatabaseEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|CreateTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t1"
argument_list|,
literal|"t2"
argument_list|,
literal|"t4"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t1"
argument_list|,
literal|"t2"
argument_list|,
literal|"t4"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|eventsMap
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|prepareIncData
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
operator|.
name|run
argument_list|(
literal|"insert into t1 values (3)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"drop table t2"
argument_list|)
operator|.
name|run
argument_list|(
literal|"alter database "
operator|+
name|dbName
operator|+
literal|" set dbproperties('some.useless.property'='1')"
argument_list|)
expr_stmt|;
comment|// Add expected events with associated tables, if any.
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|eventsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Replication causes many implicit alter database operations, so metastore will see some
comment|// alter table events as well.
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterDatabaseEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|CreateTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t6"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t1"
argument_list|,
literal|"t2"
argument_list|,
literal|"t6"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|DropTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|eventsMap
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|prepareInc2Data
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
operator|.
name|run
argument_list|(
literal|"drop table t1"
argument_list|)
expr_stmt|;
comment|// Add expected events with associated tables, if any.
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|eventsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Replication causes many implicit alter database operations, so metastore will see some
comment|// alter table events as well.
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterDatabaseEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|CreateTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t7"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|AlterTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t4"
argument_list|,
literal|"t7"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventsMap
operator|.
name|put
argument_list|(
name|DropTableEvent
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"t1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|eventsMap
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplEvents
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|eventsMap
init|=
name|prepareBootstrapData
argument_list|(
name|primaryDbName
argument_list|)
decl_stmt|;
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
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|primaryDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|checkEventSanity
argument_list|(
name|eventsMap
argument_list|,
name|replicatedDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|clearSanityData
argument_list|()
expr_stmt|;
name|eventsMap
operator|=
name|prepareIncData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": first incremental dump and load."
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
name|dump
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|primaryDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|checkEventSanity
argument_list|(
name|eventsMap
argument_list|,
name|replicatedDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|clearSanityData
argument_list|()
expr_stmt|;
comment|// Second incremental, after bootstrap
name|eventsMap
operator|=
name|prepareInc2Data
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": second incremental dump and load."
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
name|dump
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|primaryDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|checkEventSanity
argument_list|(
name|eventsMap
argument_list|,
name|replicatedDbName
argument_list|)
expr_stmt|;
name|ReplMetaStoreEventListenerTestImpl
operator|.
name|clearSanityData
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

