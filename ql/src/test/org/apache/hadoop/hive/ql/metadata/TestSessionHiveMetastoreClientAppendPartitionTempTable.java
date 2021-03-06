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
name|StorageDescriptor
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
name|TestAppendPartitions
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
comment|/**  * Test class for append partitions related methods on temporary tables.  */
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
name|TestSessionHiveMetastoreClientAppendPartitionTempTable
extends|extends
name|TestAppendPartitions
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|TestSessionHiveMetastoreClientAppendPartitionTempTable
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
name|cleanUpDatabase
argument_list|()
expr_stmt|;
name|createTables
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
parameter_list|,
name|String
name|tableType
parameter_list|,
name|String
name|location
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
name|setType
argument_list|(
name|tableType
argument_list|)
operator|.
name|setLocation
argument_list|(
name|location
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
name|builder
operator|.
name|create
argument_list|(
name|getClient
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|getClient
argument_list|()
operator|.
name|getTable
argument_list|(
name|DB_NAME
argument_list|,
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|verifyPartition
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|expectedPartValues
parameter_list|,
name|String
name|partitionName
parameter_list|)
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|partition
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedPartValues
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotEquals
argument_list|(
literal|0
argument_list|,
name|partition
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|partition
operator|.
name|getParameters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|partitionSD
init|=
name|partition
operator|.
name|getSd
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
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
name|partitionName
argument_list|,
name|partitionSD
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|getMetaStore
argument_list|()
operator|.
name|isPathExists
argument_list|(
operator|new
name|Path
argument_list|(
name|partitionSD
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

