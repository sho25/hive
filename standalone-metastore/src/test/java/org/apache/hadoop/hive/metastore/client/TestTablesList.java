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
name|metastore
operator|.
name|client
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
name|api
operator|.
name|UnknownDBException
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
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
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
name|transport
operator|.
name|TTransportException
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
name|AfterClass
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * Test class for IMetaStoreClient API. Testing the Table related functions for metadata  * querying like getting one, or multiple tables, and table name lists.  */
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
name|TestTablesList
block|{
comment|// Needed until there is no junit release with @BeforeParam, @AfterParam (junit 4.13)
comment|// https://github.com/junit-team/junit4/commit/1bf8438b65858565dbb64736bfe13aae9cfc1b5a
comment|// Then we should remove our own copy
specifier|private
specifier|static
name|Set
argument_list|<
name|AbstractMetaStoreService
argument_list|>
name|metaStoreServices
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_DATABASE
init|=
literal|"default"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OTHER_DATABASE
init|=
literal|"dummy"
decl_stmt|;
specifier|private
specifier|final
name|AbstractMetaStoreService
name|metaStore
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|client
decl_stmt|;
specifier|private
name|Table
index|[]
name|testTables
init|=
operator|new
name|Table
index|[
literal|7
index|]
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
argument_list|(
name|name
operator|=
literal|"{0}"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|getMetaStoreToTest
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|result
init|=
name|MetaStoreFactoryForTests
operator|.
name|getMetaStores
argument_list|()
decl_stmt|;
name|metaStoreServices
operator|=
name|result
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|test
lambda|->
operator|(
name|AbstractMetaStoreService
operator|)
name|test
index|[
literal|1
index|]
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|TestTablesList
parameter_list|(
name|String
name|name
parameter_list|,
name|AbstractMetaStoreService
name|metaStore
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|metaStore
operator|=
name|metaStore
expr_stmt|;
name|this
operator|.
name|metaStore
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// Needed until there is no junit release with @BeforeParam, @AfterParam (junit 4.13)
comment|// https://github.com/junit-team/junit4/commit/1bf8438b65858565dbb64736bfe13aae9cfc1b5a
comment|// Then we should move this to @AfterParam
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopMetaStores
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|AbstractMetaStoreService
name|metaStoreService
range|:
name|metaStoreServices
control|)
block|{
name|metaStoreService
operator|.
name|stop
argument_list|()
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
comment|// Get new client
name|client
operator|=
name|metaStore
operator|.
name|getClient
argument_list|()
expr_stmt|;
comment|// Clean up the database
name|client
operator|.
name|dropDatabase
argument_list|(
name|OTHER_DATABASE
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Drop every table in the default database
for|for
control|(
name|String
name|tableName
range|:
name|client
operator|.
name|getAllTables
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
control|)
block|{
name|client
operator|.
name|dropTable
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Clean up trash
name|metaStore
operator|.
name|cleanWarehouseDirs
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|0
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_0"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Owner1"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|1000
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|1
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_1"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Owner1"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|2000
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|2
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_2"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Owner2"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|1000
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|3
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_3"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Owner3"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|3000
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|4
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_4"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Tester"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|2500
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value4"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testTables
index|[
literal|5
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|DEFAULT_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_5"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|OTHER_DATABASE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|testTables
index|[
literal|6
index|]
operator|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|OTHER_DATABASE
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"filter_test_table_0"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"test_col"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"Owner1"
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|1000
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// Create the tables in the MetaStore
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|testTables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|createTable
argument_list|(
name|testTables
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Reload tables from the MetaStore
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|testTables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|testTables
index|[
name|i
index|]
operator|=
name|client
operator|.
name|getTable
argument_list|(
name|testTables
index|[
name|i
index|]
operator|.
name|getDbName
argument_list|()
argument_list|,
name|testTables
index|[
name|i
index|]
operator|.
name|getTableName
argument_list|()
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
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|client
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckOwner
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_OWNER
operator|+
literal|"=\"Owner1\""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|2
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckLastAccess
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_LAST_ACCESS
operator|+
literal|"=1000"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|2
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckParameter
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_PARAMS
operator|+
literal|"param1=\"value2\""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|3
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|3
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckLike
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_OWNER
operator|+
literal|" LIKE \"Owner.*\""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|4
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|3
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckLessOrEquals
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_LAST_ACCESS
operator|+
literal|"<=2000"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|3
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckNotEquals
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_PARAMS
operator|+
literal|"param1<>\"value2\""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|2
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|4
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckCombined
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Combined: last_access<=3000 and (Owner="Tester" or param1="param2")
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_LAST_ACCESS
operator|+
literal|"<3000 and ("
operator|+
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_OWNER
operator|+
literal|"=\"Tester\" or "
operator|+
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_PARAMS
operator|+
literal|"param1=\"value2\")"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|3
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|4
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckLimit
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Check the limit
name|String
name|filter
init|=
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_OWNER
operator|+
literal|" LIKE \"Owner.*\""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
name|filter
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|1
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|0
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|||
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|1
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|||
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|2
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|||
name|tableNames
operator|.
name|contains
argument_list|(
name|testTables
index|[
literal|3
index|]
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListTableNamesByFilterCheckNoSuchDatabase
parameter_list|()
throws|throws
name|Exception
block|{
comment|// No such database
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
literal|"no_such_database"
argument_list|,
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_LAST_ACCESS
operator|+
literal|">2000"
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Found tables"
argument_list|,
literal|0
argument_list|,
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|UnknownDBException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListTableNamesByFilterNullDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
literal|null
argument_list|,
name|hive_metastoreConstants
operator|.
name|HIVE_FILTER_FIELD_LAST_ACCESS
operator|+
literal|">2000"
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
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
name|testListTableNamesByFilterNullFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListTableNamesByFilterInvalidFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|listTableNamesByFilter
argument_list|(
name|DEFAULT_DATABASE
argument_list|,
literal|"invalid filter"
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

