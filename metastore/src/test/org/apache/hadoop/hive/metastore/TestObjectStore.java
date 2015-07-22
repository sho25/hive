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
name|InvalidInputException
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
name|InvalidObjectException
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
name|PrincipalType
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
name|Role
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
name|SerDeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestObjectStore
block|{
specifier|private
name|ObjectStore
name|objectStore
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DB1
init|=
literal|"testobjectstoredb1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DB2
init|=
literal|"testobjectstoredb2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE1
init|=
literal|"testobjectstoretable1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEY1
init|=
literal|"testobjectstorekey1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEY2
init|=
literal|"testobjectstorekey2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OWNER
init|=
literal|"testobjectstoreowner"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USER1
init|=
literal|"testobjectstoreuser1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROLE1
init|=
literal|"testobjectstorerole1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROLE2
init|=
literal|"testobjectstorerole2"
decl_stmt|;
specifier|public
specifier|static
class|class
name|MockPartitionExpressionProxy
implements|implements
name|PartitionExpressionProxy
block|{
annotation|@
name|Override
specifier|public
name|String
name|convertExprToFilter
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterPartitionsByExpr
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partColumnNames
parameter_list|,
name|List
argument_list|<
name|PrimitiveTypeInfo
argument_list|>
name|partColumnTypeInfos
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
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
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EXPRESSION_PROXY_CLASS
argument_list|,
name|MockPartitionExpressionProxy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|objectStore
operator|=
operator|new
name|ObjectStore
argument_list|()
expr_stmt|;
name|objectStore
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Deadline
operator|.
name|registerIfNot
argument_list|(
literal|100000
argument_list|)
expr_stmt|;
try|try
block|{
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
try|try
block|{
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{   }
comment|/**    * Test database operations    */
annotation|@
name|Test
specifier|public
name|void
name|testDatabaseOps
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
block|{
name|Database
name|db1
init|=
operator|new
name|Database
argument_list|(
name|DB1
argument_list|,
literal|"description"
argument_list|,
literal|"locationurl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Database
name|db2
init|=
operator|new
name|Database
argument_list|(
name|DB2
argument_list|,
literal|"description"
argument_list|,
literal|"locationurl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|createDatabase
argument_list|(
name|db1
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|createDatabase
argument_list|(
name|db2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|databases
init|=
name|objectStore
operator|.
name|getAllDatabases
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|databases
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DB1
argument_list|,
name|databases
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DB2
argument_list|,
name|databases
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB1
argument_list|)
expr_stmt|;
name|databases
operator|=
name|objectStore
operator|.
name|getAllDatabases
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|databases
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DB2
argument_list|,
name|databases
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test table operations    */
annotation|@
name|Test
specifier|public
name|void
name|testTableOps
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidInputException
block|{
name|Database
name|db1
init|=
operator|new
name|Database
argument_list|(
name|DB1
argument_list|,
literal|"description"
argument_list|,
literal|"locationurl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|createDatabase
argument_list|(
name|db1
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
literal|null
argument_list|,
literal|"location"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
operator|new
name|SerDeInfo
argument_list|(
literal|"SerDeName"
argument_list|,
literal|"serializationLib"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HashMap
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
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|Table
name|tbl1
init|=
operator|new
name|Table
argument_list|(
name|TABLE1
argument_list|,
name|DB1
argument_list|,
literal|"owner"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
name|sd
argument_list|,
literal|null
argument_list|,
name|params
argument_list|,
literal|"viewOriginalText"
argument_list|,
literal|"viewExpandedText"
argument_list|,
literal|"MANAGED_TABLE"
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|createTable
argument_list|(
name|tbl1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|objectStore
operator|.
name|getAllTables
argument_list|(
name|DB1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tables
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TABLE1
argument_list|,
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|newTbl1
init|=
operator|new
name|Table
argument_list|(
literal|"new"
operator|+
name|TABLE1
argument_list|,
name|DB1
argument_list|,
literal|"owner"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
name|sd
argument_list|,
literal|null
argument_list|,
name|params
argument_list|,
literal|"viewOriginalText"
argument_list|,
literal|"viewExpandedText"
argument_list|,
literal|"MANAGED_TABLE"
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|alterTable
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|,
name|newTbl1
argument_list|)
expr_stmt|;
name|tables
operator|=
name|objectStore
operator|.
name|getTables
argument_list|(
name|DB1
argument_list|,
literal|"new*"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tables
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"new"
operator|+
name|TABLE1
argument_list|,
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropTable
argument_list|(
name|DB1
argument_list|,
literal|"new"
operator|+
name|TABLE1
argument_list|)
expr_stmt|;
name|tables
operator|=
name|objectStore
operator|.
name|getAllTables
argument_list|(
name|DB1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tables
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests partition operations    */
annotation|@
name|Test
specifier|public
name|void
name|testPartitionOps
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidInputException
block|{
name|Database
name|db1
init|=
operator|new
name|Database
argument_list|(
name|DB1
argument_list|,
literal|"description"
argument_list|,
literal|"locationurl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|createDatabase
argument_list|(
name|db1
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
literal|null
argument_list|,
literal|"location"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
operator|new
name|SerDeInfo
argument_list|(
literal|"SerDeName"
argument_list|,
literal|"serializationLib"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|tableParams
operator|.
name|put
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|FieldSchema
name|partitionKey1
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"Country"
argument_list|,
literal|"String"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|FieldSchema
name|partitionKey2
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"State"
argument_list|,
literal|"String"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Table
name|tbl1
init|=
operator|new
name|Table
argument_list|(
name|TABLE1
argument_list|,
name|DB1
argument_list|,
literal|"owner"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
name|sd
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|partitionKey1
argument_list|,
name|partitionKey2
argument_list|)
argument_list|,
name|tableParams
argument_list|,
literal|"viewOriginalText"
argument_list|,
literal|"viewExpandedText"
argument_list|,
literal|"MANAGED_TABLE"
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|createTable
argument_list|(
name|tbl1
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionParams
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|partitionParams
operator|.
name|put
argument_list|(
literal|"PARTITION_LEVEL_PRIVILEGE"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|value1
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"US"
argument_list|,
literal|"CA"
argument_list|)
decl_stmt|;
name|Partition
name|part1
init|=
operator|new
name|Partition
argument_list|(
name|value1
argument_list|,
name|DB1
argument_list|,
name|TABLE1
argument_list|,
literal|111
argument_list|,
literal|111
argument_list|,
name|sd
argument_list|,
name|partitionParams
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|addPartition
argument_list|(
name|part1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|value2
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"US"
argument_list|,
literal|"MA"
argument_list|)
decl_stmt|;
name|Partition
name|part2
init|=
operator|new
name|Partition
argument_list|(
name|value2
argument_list|,
name|DB1
argument_list|,
name|TABLE1
argument_list|,
literal|222
argument_list|,
literal|222
argument_list|,
name|sd
argument_list|,
name|partitionParams
argument_list|)
decl_stmt|;
name|objectStore
operator|.
name|addPartition
argument_list|(
name|part2
argument_list|)
expr_stmt|;
name|Deadline
operator|.
name|startTimer
argument_list|(
literal|"getPartition"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|objectStore
operator|.
name|getPartitions
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|111
argument_list|,
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|222
argument_list|,
name|partitions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropPartition
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|partitions
operator|=
name|objectStore
operator|.
name|getPartitions
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|222
argument_list|,
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropPartition
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropTable
argument_list|(
name|DB1
argument_list|,
name|TABLE1
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|dropDatabase
argument_list|(
name|DB1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test master keys operation    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterKeyOps
parameter_list|()
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|int
name|id1
init|=
name|objectStore
operator|.
name|addMasterKey
argument_list|(
name|KEY1
argument_list|)
decl_stmt|;
name|int
name|id2
init|=
name|objectStore
operator|.
name|addMasterKey
argument_list|(
name|KEY2
argument_list|)
decl_stmt|;
name|String
index|[]
name|keys
init|=
name|objectStore
operator|.
name|getMasterKeys
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|keys
operator|.
name|length
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|KEY1
argument_list|,
name|keys
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|KEY2
argument_list|,
name|keys
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|updateMasterKey
argument_list|(
name|id1
argument_list|,
literal|"new"
operator|+
name|KEY1
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|updateMasterKey
argument_list|(
name|id2
argument_list|,
literal|"new"
operator|+
name|KEY2
argument_list|)
expr_stmt|;
name|keys
operator|=
name|objectStore
operator|.
name|getMasterKeys
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|keys
operator|.
name|length
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"new"
operator|+
name|KEY1
argument_list|,
name|keys
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"new"
operator|+
name|KEY2
argument_list|,
name|keys
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|removeMasterKey
argument_list|(
name|id1
argument_list|)
expr_stmt|;
name|keys
operator|=
name|objectStore
operator|.
name|getMasterKeys
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|keys
operator|.
name|length
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"new"
operator|+
name|KEY2
argument_list|,
name|keys
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|removeMasterKey
argument_list|(
name|id2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test role operation    */
annotation|@
name|Test
specifier|public
name|void
name|testRoleOps
parameter_list|()
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|objectStore
operator|.
name|addRole
argument_list|(
name|ROLE1
argument_list|,
name|OWNER
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|addRole
argument_list|(
name|ROLE2
argument_list|,
name|OWNER
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|roles
init|=
name|objectStore
operator|.
name|listRoleNames
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|roles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ROLE2
argument_list|,
name|roles
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Role
name|role1
init|=
name|objectStore
operator|.
name|getRole
argument_list|(
name|ROLE1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|OWNER
argument_list|,
name|role1
operator|.
name|getOwnerName
argument_list|()
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|grantRole
argument_list|(
name|role1
argument_list|,
name|USER1
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
name|OWNER
argument_list|,
name|PrincipalType
operator|.
name|ROLE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|revokeRole
argument_list|(
name|role1
argument_list|,
name|USER1
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|removeRole
argument_list|(
name|ROLE1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

