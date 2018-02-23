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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|TableType
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
name|CreationMetadata
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
name|TableMeta
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
name|TException
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
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toSet
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
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * API tests for HMS client's getTableMeta method.  */
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
name|TestGetTableMeta
extends|extends
name|MetaStoreClientTest
block|{
specifier|private
name|AbstractMetaStoreService
name|metaStore
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|client
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DB_NAME
init|=
literal|"testpartdb"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"testparttable"
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableMeta
argument_list|>
name|expectedMetas
init|=
literal|null
decl_stmt|;
specifier|public
name|TestGetTableMeta
parameter_list|(
name|String
name|name
parameter_list|,
name|AbstractMetaStoreService
name|metaStore
parameter_list|)
block|{
name|this
operator|.
name|metaStore
operator|=
name|metaStore
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
comment|// Get new client
name|client
operator|=
name|metaStore
operator|.
name|getClient
argument_list|()
expr_stmt|;
comment|// Clean up
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
operator|+
literal|"_one"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
operator|+
literal|"_two"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|metaStore
operator|.
name|cleanWarehouseDirs
argument_list|()
expr_stmt|;
comment|//Create test dbs and tables
name|expectedMetas
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|String
name|dbName
init|=
name|DB_NAME
operator|+
literal|"_one"
decl_stmt|;
name|createDB
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|expectedMetas
operator|.
name|add
argument_list|(
name|createTestTable
argument_list|(
name|dbName
argument_list|,
name|TABLE_NAME
operator|+
literal|"_one"
argument_list|,
name|TableType
operator|.
name|EXTERNAL_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|expectedMetas
operator|.
name|add
argument_list|(
name|createTestTable
argument_list|(
name|dbName
argument_list|,
name|TABLE_NAME
operator|+
literal|""
argument_list|,
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|,
literal|"cmT"
argument_list|)
argument_list|)
expr_stmt|;
name|expectedMetas
operator|.
name|add
argument_list|(
name|createTestTable
argument_list|(
name|dbName
argument_list|,
literal|"v"
operator|+
name|TABLE_NAME
argument_list|,
name|TableType
operator|.
name|VIRTUAL_VIEW
argument_list|)
argument_list|)
expr_stmt|;
name|dbName
operator|=
name|DB_NAME
operator|+
literal|"_two"
expr_stmt|;
name|createDB
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|expectedMetas
operator|.
name|add
argument_list|(
name|createTestTable
argument_list|(
name|dbName
argument_list|,
name|TABLE_NAME
operator|+
literal|"_one"
argument_list|,
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|expectedMetas
operator|.
name|add
argument_list|(
name|createTestTable
argument_list|(
name|dbName
argument_list|,
literal|"v"
operator|+
name|TABLE_NAME
argument_list|,
name|TableType
operator|.
name|MATERIALIZED_VIEW
argument_list|,
literal|""
argument_list|)
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
specifier|private
name|Database
name|createDB
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|TException
block|{
name|Database
name|db
init|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
return|return
name|db
return|;
block|}
specifier|private
name|Table
name|createTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|TableType
name|type
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
name|setType
argument_list|(
name|type
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|TableType
operator|.
name|MATERIALIZED_VIEW
condition|)
block|{
name|CreationMetadata
name|cm
init|=
operator|new
name|CreationMetadata
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|.
name|setCreationMetadata
argument_list|(
name|cm
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
name|TableType
operator|.
name|EXTERNAL_TABLE
condition|)
block|{
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
specifier|private
name|TableMeta
name|createTestTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|TableType
name|type
parameter_list|,
name|String
name|comment
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|createTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"comment"
argument_list|,
name|comment
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|TableMeta
name|tableMeta
init|=
operator|new
name|TableMeta
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|type
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|tableMeta
operator|.
name|setComments
argument_list|(
name|comment
argument_list|)
expr_stmt|;
return|return
name|tableMeta
return|;
block|}
specifier|private
name|TableMeta
name|createTestTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|TableType
name|type
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|createTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
operator|new
name|TableMeta
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|type
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|assertTableMetas
parameter_list|(
name|int
index|[]
name|expected
parameter_list|,
name|List
argument_list|<
name|TableMeta
argument_list|>
name|actualTableMetas
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|length
operator|+
literal|" but have "
operator|+
name|actualTableMetas
operator|.
name|size
argument_list|()
operator|+
literal|" tableMeta(s)"
argument_list|,
name|expected
operator|.
name|length
argument_list|,
name|actualTableMetas
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|TableMeta
argument_list|>
name|metas
init|=
name|actualTableMetas
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|expected
control|)
block|{
name|assertTrue
argument_list|(
literal|"Missing "
operator|+
name|expectedMetas
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|metas
operator|.
name|remove
argument_list|(
name|expectedMetas
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Unexpected tableMeta(s): "
operator|+
name|metas
argument_list|,
name|metas
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Testing getTableMeta(String,String,List(String)) ->    *         get_table_meta(String,String,List(String)).    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMeta
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"asdf"
argument_list|,
literal|"qwerty"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"zxcv"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"testpartdb_two"
argument_list|,
literal|"vtestparttable"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"***"
argument_list|,
literal|"**"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*one"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*one*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"testpartdb_two"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"testpartdb_two*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"testpartdb*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"testpartdb*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|,
name|TableType
operator|.
name|MATERIALIZED_VIEW
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*one"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"*TABLE"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*one"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaCaseSensitive
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*tWo"
argument_list|,
literal|"tEsT*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|3
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"mAnAGeD_tABlE"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaNullOrEmptyDb
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|null
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|""
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaNullOrEmptyTbl
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|null
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|""
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaNullOrEmptyTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
name|tableMetas
operator|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaNullNoDbNoTbl
parameter_list|()
throws|throws
name|Exception
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
operator|+
literal|"_one"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
operator|+
literal|"_two"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMetas
init|=
name|client
operator|.
name|getTableMeta
argument_list|(
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
decl_stmt|;
name|assertTableMetas
argument_list|(
operator|new
name|int
index|[]
block|{}
argument_list|,
name|tableMetas
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

