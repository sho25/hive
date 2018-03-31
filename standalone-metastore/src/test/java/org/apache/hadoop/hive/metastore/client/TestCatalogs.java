begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|Warehouse
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
name|Catalog
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
name|client
operator|.
name|builder
operator|.
name|CatalogBuilder
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
name|Ignore
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
name|File
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
name|Comparator
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
import|;
end_import

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TestCatalogs
extends|extends
name|MetaStoreClientTest
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestCatalogs
operator|.
name|class
argument_list|)
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
specifier|public
name|TestCatalogs
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
comment|// Drop any left over catalogs
name|List
argument_list|<
name|String
argument_list|>
name|catalogs
init|=
name|client
operator|.
name|getCatalogs
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|catName
range|:
name|catalogs
control|)
block|{
if|if
condition|(
operator|!
name|catName
operator|.
name|equalsIgnoreCase
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
argument_list|)
condition|)
block|{
comment|// First drop any databases in catalog
name|List
argument_list|<
name|String
argument_list|>
name|databases
init|=
name|client
operator|.
name|getAllDatabases
argument_list|(
name|catName
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|db
range|:
name|databases
control|)
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|catName
argument_list|,
name|db
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|dropCatalog
argument_list|(
name|catName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|databases
init|=
name|client
operator|.
name|getAllDatabases
argument_list|(
name|catName
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|db
range|:
name|databases
control|)
block|{
if|if
condition|(
operator|!
name|db
operator|.
name|equalsIgnoreCase
argument_list|(
name|DEFAULT_DATABASE_NAME
argument_list|)
condition|)
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|catName
argument_list|,
name|db
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|catalogOperations
parameter_list|()
throws|throws
name|TException
block|{
name|String
index|[]
name|catNames
init|=
block|{
literal|"cat1"
block|,
literal|"cat2"
block|,
literal|"ADifferentName"
block|}
decl_stmt|;
name|String
index|[]
name|description
init|=
block|{
literal|"a description"
block|,
literal|"super descriptive"
block|,
literal|null
block|}
decl_stmt|;
name|String
index|[]
name|location
init|=
block|{
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
literal|"cat1"
argument_list|)
block|,
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
literal|"cat2"
argument_list|)
block|,
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
literal|"different"
argument_list|)
block|}
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
name|catNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Catalog
name|cat
init|=
operator|new
name|CatalogBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|catNames
index|[
name|i
index|]
argument_list|)
operator|.
name|setLocation
argument_list|(
name|location
index|[
name|i
index|]
argument_list|)
operator|.
name|setDescription
argument_list|(
name|description
index|[
name|i
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createCatalog
argument_list|(
name|cat
argument_list|)
expr_stmt|;
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
name|cat
operator|.
name|getLocationUri
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|dir
operator|.
name|exists
argument_list|()
operator|&&
name|dir
operator|.
name|isDirectory
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|catNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Catalog
name|cat
init|=
name|client
operator|.
name|getCatalog
argument_list|(
name|catNames
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|catNames
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
name|cat
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|description
index|[
name|i
index|]
argument_list|,
name|cat
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|location
index|[
name|i
index|]
argument_list|,
name|cat
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
name|cat
operator|.
name|getLocationUri
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|dir
operator|.
name|exists
argument_list|()
operator|&&
name|dir
operator|.
name|isDirectory
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure there's a default database associated with each catalog
name|Database
name|db
init|=
name|client
operator|.
name|getDatabase
argument_list|(
name|catNames
index|[
name|i
index|]
argument_list|,
name|DEFAULT_DATABASE_NAME
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"file:"
operator|+
name|cat
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|db
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|catalogs
init|=
name|client
operator|.
name|getCatalogs
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|catalogs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|catalogs
operator|.
name|sort
argument_list|(
name|Comparator
operator|.
name|naturalOrder
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|catNames
operator|.
name|length
operator|+
literal|1
argument_list|)
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
argument_list|)
expr_stmt|;
name|expected
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|catNames
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|sort
argument_list|(
name|Comparator
operator|.
name|naturalOrder
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|catalogs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|" actual "
operator|+
name|catalogs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|catalogs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|catNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|dropCatalog
argument_list|(
name|catNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
name|location
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|dir
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|catalogs
operator|=
name|client
operator|.
name|getCatalogs
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|catalogs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|catalogs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NoSuchObjectException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|getNonExistentCatalog
parameter_list|()
throws|throws
name|TException
block|{
name|client
operator|.
name|getCatalog
argument_list|(
literal|"noSuchCatalog"
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
annotation|@
name|Ignore
comment|// TODO This test passes fine locally but fails on Linux, not sure why
specifier|public
name|void
name|createCatalogWithBadLocation
parameter_list|()
throws|throws
name|TException
block|{
name|Catalog
name|cat
init|=
operator|new
name|CatalogBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"goodluck"
argument_list|)
operator|.
name|setLocation
argument_list|(
literal|"/nosuch/nosuch"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createCatalog
argument_list|(
name|cat
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NoSuchObjectException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|dropNonExistentCatalog
parameter_list|()
throws|throws
name|TException
block|{
name|client
operator|.
name|dropCatalog
argument_list|(
literal|"noSuchCatalog"
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
name|dropHiveCatalog
parameter_list|()
throws|throws
name|TException
block|{
name|client
operator|.
name|dropCatalog
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
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
name|dropNonEmptyCatalog
parameter_list|()
throws|throws
name|TException
block|{
name|String
name|catName
init|=
literal|"toBeDropped"
decl_stmt|;
name|Catalog
name|cat
init|=
operator|new
name|CatalogBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|catName
argument_list|)
operator|.
name|setLocation
argument_list|(
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
name|catName
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createCatalog
argument_list|(
name|cat
argument_list|)
expr_stmt|;
name|String
name|dbName
init|=
literal|"dontDropMe"
decl_stmt|;
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setCatalogName
argument_list|(
name|catName
argument_list|)
operator|.
name|create
argument_list|(
name|client
argument_list|,
name|metaStore
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropCatalog
argument_list|(
name|catName
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
name|dropCatalogWithNonEmptyDefaultDb
parameter_list|()
throws|throws
name|TException
block|{
name|String
name|catName
init|=
literal|"toBeDropped2"
decl_stmt|;
operator|new
name|CatalogBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|catName
argument_list|)
operator|.
name|setLocation
argument_list|(
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
name|catName
argument_list|)
argument_list|)
operator|.
name|create
argument_list|(
name|client
argument_list|)
expr_stmt|;
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
literal|"not_droppable"
argument_list|)
operator|.
name|setCatName
argument_list|(
name|catName
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"cola1"
argument_list|,
literal|"bigint"
argument_list|)
operator|.
name|create
argument_list|(
name|client
argument_list|,
name|metaStore
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropCatalog
argument_list|(
name|catName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

