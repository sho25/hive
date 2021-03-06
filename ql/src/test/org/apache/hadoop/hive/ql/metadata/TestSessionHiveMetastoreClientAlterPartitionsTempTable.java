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
name|metadata
package|;
end_package

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
name|IMetaStoreClient
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
name|MetaStoreTestUtils
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
name|annotation
operator|.
name|MetastoreCheckinTest
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
name|InvalidOperationException
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
name|metastore
operator|.
name|client
operator|.
name|CustomIgnoreRule
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
name|client
operator|.
name|TestAlterPartitions
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
name|client
operator|.
name|builder
operator|.
name|PartitionBuilder
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
name|client
operator|.
name|builder
operator|.
name|TableBuilder
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
name|minihms
operator|.
name|AbstractMetaStoreService
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
name|thrift
operator|.
name|TException
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
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|assertFalse
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

begin_comment
comment|/**  * Test class for alter/rename partitions related methods on temporary tables.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
name|MetastoreCheckinTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSessionHiveMetastoreClientAlterPartitionsTempTable
extends|extends
name|TestAlterPartitions
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PART_PRIV
init|=
literal|"PARTITION_LEVEL_PRIVILEGE"
decl_stmt|;
specifier|public
name|TestSessionHiveMetastoreClientAlterPartitionsTempTable
parameter_list|(
name|String
name|name
parameter_list|,
name|AbstractMetaStoreService
name|metaStore
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|metaStore
argument_list|)
expr_stmt|;
name|ignoreRule
operator|=
operator|new
name|CustomIgnoreRule
argument_list|()
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
name|initHiveConf
argument_list|()
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setClient
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getMSC
argument_list|()
argument_list|)
expr_stmt|;
name|cleanDB
argument_list|()
expr_stmt|;
name|createDB
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initHiveConf
parameter_list|()
throws|throws
name|HiveException
block|{
name|conf
operator|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
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
name|METASTORE_FASTPATH
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Table
name|createTestTable
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partCols
parameter_list|,
name|boolean
name|setPartitionLevelPrivilages
parameter_list|)
throws|throws
name|Exception
block|{
name|TableBuilder
name|builder
init|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"name"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|setTemporary
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|partCols
operator|.
name|forEach
argument_list|(
name|col
lambda|->
name|builder
operator|.
name|addPartCol
argument_list|(
name|col
argument_list|,
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|builder
operator|.
name|build
argument_list|(
name|getMetaStore
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|setPartitionLevelPrivilages
condition|)
block|{
name|table
operator|.
name|putToParameters
argument_list|(
name|PART_PRIV
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addPartition
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
throws|throws
name|TException
block|{
name|PartitionBuilder
name|builder
init|=
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|inTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|values
operator|.
name|forEach
argument_list|(
name|builder
operator|::
name|addValue
argument_list|)
expr_stmt|;
name|Partition
name|partition
init|=
name|builder
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|getClient
argument_list|()
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addPartitions
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partitions
operator|.
name|add
argument_list|(
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|inTable
argument_list|(
name|table
argument_list|)
operator|.
name|addValue
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setLocation
argument_list|(
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|add_partitions
argument_list|(
name|partitions
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertPartitionUnchanged
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|testValues
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partCols
parameter_list|)
throws|throws
name|Exception
block|{
name|assertFalse
argument_list|(
name|partition
operator|.
name|getParameters
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"hmsTestParam001"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedKVPairs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partCols
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|expectedKVPairs
operator|.
name|add
argument_list|(
name|partCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"="
operator|+
name|testValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
name|getClient
argument_list|()
operator|.
name|getTable
argument_list|(
name|partition
operator|.
name|getDbName
argument_list|()
argument_list|,
name|partition
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|partPath
init|=
name|String
operator|.
name|join
argument_list|(
literal|"/"
argument_list|,
name|expectedKVPairs
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/"
operator|+
name|partPath
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertPartitionChanged
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|testValues
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partCols
parameter_list|)
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|"testValue001"
argument_list|,
name|partition
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"hmsTestParam001"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedKVPairs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partCols
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|expectedKVPairs
operator|.
name|add
argument_list|(
name|partCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"="
operator|+
name|testValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
name|getClient
argument_list|()
operator|.
name|getTable
argument_list|(
name|partition
operator|.
name|getDbName
argument_list|()
argument_list|,
name|partition
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|partPath
init|=
name|String
operator|.
name|join
argument_list|(
literal|"/"
argument_list|,
name|expectedKVPairs
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/"
operator|+
name|partPath
operator|+
literal|"/hh=01"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NEW_CREATE_TIME
argument_list|,
name|partition
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NEW_CREATE_TIME
argument_list|,
name|partition
operator|.
name|getLastAccessTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testRenamePartitionNullNewPart
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testRenamePartitionNullNewPart
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testAlterPartitionsNullPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testAlterPartitionsNullPartition
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testAlterPartitionsWithEnvironmentCtxNullPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testAlterPartitionsWithEnvironmentCtxNullPartition
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testAlterPartitionsNullPartitions
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testAlterPartitionsNullPartitions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testAlterPartitionsWithEnvironmentCtxNullPartitions
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testAlterPartitionsWithEnvironmentCtxNullPartitions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackNullPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|oldParts
init|=
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|assertPartitionRollback
argument_list|(
name|oldParts
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|oldParts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|,
name|oldParts
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackNullPartitions
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|assertPartitionRollback
argument_list|(
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackPartValsNull
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|oldParts
init|=
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Partition
name|partition
init|=
operator|new
name|Partition
argument_list|(
name|oldParts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|partition
operator|.
name|setValues
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertPartitionRollback
argument_list|(
name|oldParts
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|partition
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackUnknownPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|getClient
argument_list|()
operator|.
name|getTable
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Partition
name|newPart1
init|=
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|inTable
argument_list|(
name|table
argument_list|)
operator|.
name|addValue
argument_list|(
literal|"1111"
argument_list|)
operator|.
name|addValue
argument_list|(
literal|"1111"
argument_list|)
operator|.
name|addValue
argument_list|(
literal|"11"
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|oldPartitions
init|=
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Partition
name|newPart2
init|=
operator|new
name|Partition
argument_list|(
name|oldPartitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|makeTestChangesOnPartition
argument_list|(
name|newPart2
argument_list|)
expr_stmt|;
name|assertPartitionRollback
argument_list|(
name|oldPartitions
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|newPart2
argument_list|,
name|newPart1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackChangeDBName
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|oldPartitions
init|=
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Partition
name|newPart1
init|=
operator|new
name|Partition
argument_list|(
name|oldPartitions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|newPart1
operator|.
name|setDbName
argument_list|(
name|DB_NAME
operator|+
literal|"_changed"
argument_list|)
expr_stmt|;
name|assertPartitionRollback
argument_list|(
name|oldPartitions
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|oldPartitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|oldPartitions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|newPart1
argument_list|,
name|oldPartitions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionsCheckRollbackChangeTableName
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|oldPartitions
init|=
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Partition
name|newPart1
init|=
operator|new
name|Partition
argument_list|(
name|oldPartitions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|newPart1
operator|.
name|setTableName
argument_list|(
name|TABLE_NAME
operator|+
literal|"_changed"
argument_list|)
expr_stmt|;
name|assertPartitionRollback
argument_list|(
name|oldPartitions
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|oldPartitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|oldPartitions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|newPart1
argument_list|,
name|oldPartitions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
name|void
name|assertPartitionRollback
parameter_list|(
name|List
argument_list|<
name|Partition
argument_list|>
name|oldParts
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|alterParts
parameter_list|)
throws|throws
name|TException
block|{
try|try
block|{
name|getClient
argument_list|()
operator|.
name|alter_partitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|alterParts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
decl||
name|InvalidOperationException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|oldParts
argument_list|,
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|fail
argument_list|(
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

