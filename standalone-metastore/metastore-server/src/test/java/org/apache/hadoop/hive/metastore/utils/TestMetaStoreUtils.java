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
name|utils
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
name|ImmutableMap
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|common
operator|.
name|StatsSetupConst
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
name|MetastoreUnitTest
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
name|EnvironmentContext
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
name|Assert
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
name|Collections
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
name|common
operator|.
name|StatsSetupConst
operator|.
name|COLUMN_STATS_ACCURATE
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
name|common
operator|.
name|StatsSetupConst
operator|.
name|NUM_FILES
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
name|common
operator|.
name|StatsSetupConst
operator|.
name|NUM_ERASURE_CODED_FILES
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
name|common
operator|.
name|StatsSetupConst
operator|.
name|STATS_GENERATED
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
name|common
operator|.
name|StatsSetupConst
operator|.
name|TOTAL_SIZE
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
name|utils
operator|.
name|MetaStoreServerUtils
operator|.
name|updateTableStatsSlow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|Is
operator|.
name|is
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
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetaStoreUtils
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DB_NAME
init|=
literal|"db1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"tbl1"
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|paramsWithStats
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|NUM_FILES
argument_list|,
literal|"1"
argument_list|,
name|TOTAL_SIZE
argument_list|,
literal|"2"
argument_list|,
name|NUM_ERASURE_CODED_FILES
argument_list|,
literal|"0"
argument_list|)
decl_stmt|;
specifier|private
name|Database
name|db
decl_stmt|;
specifier|public
name|TestMetaStoreUtils
parameter_list|()
block|{
try|try
block|{
name|db
operator|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrimMapNullsXform
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"akey"
argument_list|,
literal|"aval"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"blank"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expected
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"akey"
argument_list|,
literal|"aval"
argument_list|,
literal|"blank"
argument_list|,
literal|""
argument_list|,
literal|"null"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|xformed
init|=
name|MetaStoreServerUtils
operator|.
name|trimMapNulls
argument_list|(
name|m
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|xformed
argument_list|,
name|is
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrimMapNullsPrune
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"akey"
argument_list|,
literal|"aval"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"blank"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expected
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"akey"
argument_list|,
literal|"aval"
argument_list|,
literal|"blank"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pruned
init|=
name|MetaStoreServerUtils
operator|.
name|trimMapNulls
argument_list|(
name|m
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pruned
argument_list|,
name|is
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testcolumnsIncludedByNameType
parameter_list|()
block|{
name|FieldSchema
name|col1
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
literal|"string"
argument_list|,
literal|"col1 comment"
argument_list|)
decl_stmt|;
name|FieldSchema
name|col1a
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
literal|"string"
argument_list|,
literal|"col1 but with a different comment"
argument_list|)
decl_stmt|;
name|FieldSchema
name|col2
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col2"
argument_list|,
literal|"string"
argument_list|,
literal|"col2 comment"
argument_list|)
decl_stmt|;
name|FieldSchema
name|col3
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col3"
argument_list|,
literal|"string"
argument_list|,
literal|"col3 comment"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col1a
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col2
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|,
name|col3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col3
argument_list|,
name|col2
argument_list|,
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|MetaStoreServerUtils
operator|.
name|columnsIncludedByNameType
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that updateTableStatsSlow really updates table statistics.    * The test does the following:    *<ol>    *<li>Create database</li>    *<li>Create unpartitioned table</li>    *<li>Create unpartitioned table which has params</li>    *<li>Call updateTableStatsSlow with arguments which should cause stats calculation</li>    *<li>Verify table statistics using mocked warehouse</li>    *<li>Create table which already have stats</li>    *<li>Call updateTableStatsSlow forcing stats recompute</li>    *<li>Verify table statistics using mocked warehouse</li>    *<li>Verifies behavior when STATS_GENERATED is set in environment context</li>    *</ol>    */
annotation|@
name|Test
specifier|public
name|void
name|testUpdateTableStatsSlow_statsUpdated
parameter_list|()
throws|throws
name|TException
block|{
name|long
name|fileLength
init|=
literal|5
decl_stmt|;
comment|// Create database and table
name|Table
name|tbl
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
comment|// Set up mock warehouse
name|FileStatus
name|fs1
init|=
name|getFileStatus
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|"/tmp/0"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|FileStatus
name|fs2
init|=
name|getFileStatus
argument_list|(
name|fileLength
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|"/tmp/1"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|FileStatus
name|fs3
init|=
name|getFileStatus
argument_list|(
name|fileLength
argument_list|,
literal|false
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|"/tmp/1"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|fileStatus
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|fs1
argument_list|,
name|fs2
argument_list|,
name|fs3
argument_list|)
decl_stmt|;
name|Warehouse
name|wh
init|=
name|mock
argument_list|(
name|Warehouse
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|wh
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fileStatus
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expected
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|NUM_FILES
argument_list|,
literal|"2"
argument_list|,
name|TOTAL_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|2
operator|*
name|fileLength
argument_list|)
argument_list|,
name|NUM_ERASURE_CODED_FILES
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify that when stats are already present and forceRecompute is specified they are recomputed
name|Table
name|tbl1
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|NUM_FILES
argument_list|,
literal|"0"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|TOTAL_SIZE
argument_list|,
literal|"0"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|wh
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fileStatus
argument_list|)
expr_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tbl1
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify that COLUMN_STATS_ACCURATE is removed from params
name|Table
name|tbl2
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|wh
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl2
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fileStatus
argument_list|)
expr_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl2
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tbl2
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
name|EnvironmentContext
name|context
init|=
operator|new
name|EnvironmentContext
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|STATS_GENERATED
argument_list|,
name|StatsSetupConst
operator|.
name|TASK
argument_list|)
argument_list|)
decl_stmt|;
comment|// Verify that if environment context has STATS_GENERATED set to task,
comment|// COLUMN_STATS_ACCURATE in params is set to correct value
name|Table
name|tbl3
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
literal|"foo"
argument_list|)
comment|// The value doesn't matter
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|wh
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl3
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fileStatus
argument_list|)
expr_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl3
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expected1
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|NUM_FILES
argument_list|,
literal|"2"
argument_list|,
name|TOTAL_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|2
operator|*
name|fileLength
argument_list|)
argument_list|,
name|NUM_ERASURE_CODED_FILES
argument_list|,
literal|"1"
argument_list|,
name|COLUMN_STATS_ACCURATE
argument_list|,
literal|"{\"BASIC_STATS\":\"true\"}"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tbl3
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|expected1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the call to updateTableStatsSlow() removes DO_NOT_UPDATE_STATS from table params.    */
annotation|@
name|Test
specifier|public
name|void
name|testUpdateTableStatsSlow_removesDoNotUpdateStats
parameter_list|()
throws|throws
name|TException
block|{
comment|// Create database and table
name|Table
name|tbl
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|StatsSetupConst
operator|.
name|DO_NOT_UPDATE_STATS
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Table
name|tbl1
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addTableParam
argument_list|(
name|StatsSetupConst
operator|.
name|DO_NOT_UPDATE_STATS
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Warehouse
name|wh
init|=
name|mock
argument_list|(
name|Warehouse
operator|.
name|class
argument_list|)
decl_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|wh
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|,
name|wh
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|,
name|is
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|wh
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that updateTableStatsSlow() does not calculate table statistics when    *<ol>    *<li>newDir is true</li>    *<li>Table is partitioned</li>    *<li>Stats are already present and forceRecompute isn't set</li>    *</ol>    */
annotation|@
name|Test
specifier|public
name|void
name|testUpdateTableStatsSlow_doesNotUpdateStats
parameter_list|()
throws|throws
name|TException
block|{
comment|// Create database and table
name|FieldSchema
name|fs
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"date"
argument_list|,
literal|"string"
argument_list|,
literal|"date column"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|Table
name|tbl
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Warehouse
name|wh
init|=
name|mock
argument_list|(
name|Warehouse
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// newDir(true) => stats not updated
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|,
name|wh
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|wh
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// partitioned table => stats not updated
name|Table
name|tbl1
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setPartCols
argument_list|(
name|cols
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|wh
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl1
argument_list|)
expr_stmt|;
comment|// Already contains stats => stats not updated when forceRecompute isn't set
name|Table
name|tbl2
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
name|TABLE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|setTableParams
argument_list|(
name|paramsWithStats
argument_list|)
operator|.
name|build
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|updateTableStatsSlow
argument_list|(
name|db
argument_list|,
name|tbl2
argument_list|,
name|wh
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|wh
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|getFileStatusesForUnpartitionedTable
argument_list|(
name|db
argument_list|,
name|tbl2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Build a FileStatus object.    */
specifier|private
specifier|static
name|FileStatus
name|getFileStatus
parameter_list|(
name|long
name|fileLength
parameter_list|,
name|boolean
name|isdir
parameter_list|,
name|int
name|blockReplication
parameter_list|,
name|int
name|blockSize
parameter_list|,
name|int
name|modificationTime
parameter_list|,
name|String
name|pathString
parameter_list|,
name|boolean
name|isErasureCoded
parameter_list|)
block|{
return|return
operator|new
name|FileStatus
argument_list|(
name|fileLength
argument_list|,
name|isdir
argument_list|,
name|blockReplication
argument_list|,
name|blockSize
argument_list|,
name|modificationTime
argument_list|,
literal|0L
argument_list|,
operator|(
name|FsPermission
operator|)
literal|null
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|Path
argument_list|(
name|pathString
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|isErasureCoded
argument_list|)
return|;
block|}
block|}
end_class

end_unit

