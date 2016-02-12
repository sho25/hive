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
name|metastore
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
name|fail
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
name|hive
operator|.
name|UtilsForTest
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
name|api
operator|.
name|Database
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
name|Index
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
name|api
operator|.
name|NoSuchObjectException
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
name|Partition
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
name|PartitionSpec
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
name|Driver
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
name|ShimLoader
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestFilterHooks
block|{
specifier|public
specifier|static
class|class
name|DummyMetaStoreFilterHookImpl
extends|extends
name|DefaultMetaStoreFilterHookImpl
block|{
specifier|public
specifier|static
name|boolean
name|blockResults
init|=
literal|false
decl_stmt|;
specifier|public
name|DummyMetaStoreFilterHookImpl
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterDatabases
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dbList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterDatabases
argument_list|(
name|dbList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Database
name|filterDatabase
parameter_list|(
name|Database
name|dataBase
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
literal|"Blocked access"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|filterDatabase
argument_list|(
name|dataBase
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterTableNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterTableNames
argument_list|(
name|dbName
argument_list|,
name|tableList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|filterTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
literal|"Blocked access"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|filterTable
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Table
argument_list|>
name|filterTables
parameter_list|(
name|List
argument_list|<
name|Table
argument_list|>
name|tableList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Table
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterTables
argument_list|(
name|tableList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|filterPartitions
parameter_list|(
name|List
argument_list|<
name|Partition
argument_list|>
name|partitionList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterPartitions
argument_list|(
name|partitionList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|filterPartitionSpecs
parameter_list|(
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|PartitionSpec
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterPartitionSpecs
argument_list|(
name|partitionSpecList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|filterPartition
parameter_list|(
name|Partition
name|partition
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
literal|"Blocked access"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|filterPartition
argument_list|(
name|partition
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterPartitionNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterPartitionNames
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|partitionNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Index
name|filterIndex
parameter_list|(
name|Index
name|index
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
literal|"Blocked access"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|filterIndex
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterIndexNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indexList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterIndexNames
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|indexList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Index
argument_list|>
name|filterIndexes
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexeList
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|blockResults
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Index
argument_list|>
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|filterIndexes
argument_list|(
name|indexeList
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|String
name|DBNAME1
init|=
literal|"testdb1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DBNAME2
init|=
literal|"testdb2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TAB1
init|=
literal|"tab1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TAB2
init|=
literal|"tab2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INDEX1
init|=
literal|"idx1"
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|static
name|Driver
name|driver
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|false
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestFilterHooks
operator|.
name|class
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
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
name|setVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_FILTER_HOOK
argument_list|,
name|DummyMetaStoreFilterHookImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UtilsForTest
operator|.
name|setNewDerbyDbLocation
argument_list|(
name|hiveConf
argument_list|,
name|TestFilterHooks
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
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
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|DBNAME1
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|DBNAME2
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|DBNAME1
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|DBNAME2
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|DBNAME1
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table "
operator|+
name|DBNAME1
operator|+
literal|"."
operator|+
name|TAB1
operator|+
literal|" (id int, name string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table "
operator|+
name|TAB2
operator|+
literal|" (id int) partitioned by (name string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"ALTER TABLE "
operator|+
name|TAB2
operator|+
literal|" ADD PARTITION (name='value1')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"ALTER TABLE "
operator|+
name|TAB2
operator|+
literal|" ADD PARTITION (name='value2')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"CREATE INDEX "
operator|+
name|INDEX1
operator|+
literal|" on table "
operator|+
name|TAB1
operator|+
literal|"(id) AS 'COMPACT' WITH DEFERRED REBUILD"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|false
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|DBNAME1
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|DBNAME2
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
name|driver
operator|.
name|destroy
argument_list|()
expr_stmt|;
name|msc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getTable
argument_list|(
name|DBNAME1
argument_list|,
name|TAB1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|msc
operator|.
name|getTables
argument_list|(
name|DBNAME1
argument_list|,
literal|"*"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|msc
operator|.
name|getAllTables
argument_list|(
name|DBNAME1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|msc
operator|.
name|getTables
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getAllTables
argument_list|(
name|DBNAME2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getDatabase
argument_list|(
name|DBNAME1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|msc
operator|.
name|getDatabases
argument_list|(
literal|"*"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|msc
operator|.
name|getAllDatabases
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|msc
operator|.
name|getDatabases
argument_list|(
name|DBNAME1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getPartition
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|,
literal|"name=value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|msc
operator|.
name|getPartitionsByNames
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"name=value1"
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getIndex
argument_list|(
name|DBNAME1
argument_list|,
name|TAB1
argument_list|,
name|INDEX1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDummyFilterForTables
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|msc
operator|.
name|getTable
argument_list|(
name|DBNAME1
argument_list|,
name|TAB1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getTable() should fail with blocking mode"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// Excepted
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getTables
argument_list|(
name|DBNAME1
argument_list|,
literal|"*"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getAllTables
argument_list|(
name|DBNAME1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getTables
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDummyFilterForDb
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getDatabase
argument_list|(
name|DBNAME1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getDatabase() should fail with blocking mode"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// Excepted
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getDatabases
argument_list|(
literal|"*"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getAllDatabases
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getDatabases
argument_list|(
name|DBNAME1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDummyFilterForPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getPartition
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|,
literal|"name=value1"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getPartition() should fail with blocking mode"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// Excepted
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|msc
operator|.
name|getPartitionsByNames
argument_list|(
name|DBNAME1
argument_list|,
name|TAB2
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"name=value1"
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDummyFilterForIndex
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMetaStoreFilterHookImpl
operator|.
name|blockResults
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|assertNotNull
argument_list|(
name|msc
operator|.
name|getIndex
argument_list|(
name|DBNAME1
argument_list|,
name|TAB1
argument_list|,
name|INDEX1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getPartition() should fail with blocking mode"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// Excepted
block|}
block|}
block|}
end_class

end_unit

