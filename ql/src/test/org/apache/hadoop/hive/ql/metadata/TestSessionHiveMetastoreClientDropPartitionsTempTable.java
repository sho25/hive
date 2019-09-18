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
name|FieldSchema
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
name|TestDropPartitions
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
name|DatabaseBuilder
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

begin_comment
comment|/**  * Test class for delete partitions related methods on temporary tables.  */
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
name|TestSessionHiveMetastoreClientDropPartitionsTempTable
extends|extends
name|TestDropPartitions
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|TestSessionHiveMetastoreClientDropPartitionsTempTable
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
name|getClient
argument_list|()
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|getMetaStore
argument_list|()
operator|.
name|cleanWarehouseDirs
argument_list|()
expr_stmt|;
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|create
argument_list|(
name|getClient
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Create test tables with 3 partitions
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|getYearAndMonthPartCols
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|createPartitions
argument_list|()
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
name|createTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
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
name|DB_NAME
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_id"
argument_list|,
literal|"int"
argument_list|,
literal|"test col id"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_value"
argument_list|,
literal|"string"
argument_list|,
literal|"test col value"
argument_list|)
operator|.
name|setPartCols
argument_list|(
name|partCols
argument_list|)
operator|.
name|setLocation
argument_list|(
name|getMetaStore
argument_list|()
operator|.
name|getWarehouseRoot
argument_list|()
operator|+
literal|"/"
operator|+
name|tableName
argument_list|)
operator|.
name|setTemporary
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableParams
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setTableParams
argument_list|(
name|tableParams
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|create
argument_list|(
name|getClient
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Partition
name|createPartition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|)
throws|throws
name|Exception
block|{
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|setTableName
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setValues
argument_list|(
name|values
argument_list|)
operator|.
name|setCols
argument_list|(
name|partCols
argument_list|)
operator|.
name|addToTable
argument_list|(
name|getClient
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
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
name|partition
init|=
name|getClient
argument_list|()
operator|.
name|getPartition
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|values
argument_list|)
decl_stmt|;
return|return
name|partition
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Partition
name|createPartition
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|location
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partParams
parameter_list|)
throws|throws
name|Exception
block|{
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|setValues
argument_list|(
name|values
argument_list|)
operator|.
name|setCols
argument_list|(
name|partCols
argument_list|)
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
operator|.
name|setPartParams
argument_list|(
name|partParams
argument_list|)
operator|.
name|addToTable
argument_list|(
name|getClient
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Partition
name|partition
init|=
name|getClient
argument_list|()
operator|.
name|getPartition
argument_list|(
name|DB_NAME
argument_list|,
name|tableName
argument_list|,
name|values
argument_list|)
decl_stmt|;
return|return
name|partition
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkPartitionsAfterDelete
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|droppedPartitions
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|existingPartitions
parameter_list|,
name|boolean
name|deleteData
parameter_list|,
name|boolean
name|purge
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
name|getClient
argument_list|()
operator|.
name|listPartitions
argument_list|(
name|DB_NAME
argument_list|,
name|tableName
argument_list|,
name|MAX
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The table "
operator|+
name|tableName
operator|+
literal|" has "
operator|+
name|partitions
operator|.
name|size
argument_list|()
operator|+
literal|" partitions, but it should have "
operator|+
name|existingPartitions
operator|.
name|size
argument_list|()
argument_list|,
name|existingPartitions
operator|.
name|size
argument_list|()
argument_list|,
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|droppedPartition
range|:
name|droppedPartitions
control|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|partitions
operator|.
name|contains
argument_list|(
name|droppedPartition
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|partitionPath
init|=
operator|new
name|Path
argument_list|(
name|droppedPartition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteData
condition|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"The location '"
operator|+
name|partitionPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' should not exist."
argument_list|,
name|getMetaStore
argument_list|()
operator|.
name|isPathExists
argument_list|(
name|partitionPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The location '"
operator|+
name|partitionPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' should exist."
argument_list|,
name|getMetaStore
argument_list|()
operator|.
name|isPathExists
argument_list|(
name|partitionPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Partition
name|existingPartition
range|:
name|existingPartitions
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|partitions
operator|.
name|contains
argument_list|(
name|existingPartition
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|partitionPath
init|=
operator|new
name|Path
argument_list|(
name|existingPartition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The location '"
operator|+
name|partitionPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' should exist."
argument_list|,
name|getMetaStore
argument_list|()
operator|.
name|isPathExists
argument_list|(
name|partitionPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

