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
name|api
operator|.
name|*
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
name|mockito
operator|.
name|Mockito
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

begin_class
specifier|public
class|class
name|TestHiveAlterHandler
block|{
annotation|@
name|Test
specifier|public
name|void
name|testAlterTableAddColNotUpdateStats
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
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
name|FieldSchema
name|col4
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col4"
argument_list|,
literal|"string"
argument_list|,
literal|"col4 comment"
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|oldSd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|oldSd
operator|.
name|setCols
argument_list|(
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
expr_stmt|;
name|Table
name|oldTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|oldTable
operator|.
name|setDbName
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setTableName
argument_list|(
literal|"test_table"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setSd
argument_list|(
name|oldSd
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|newSd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|oldSd
argument_list|)
decl_stmt|;
name|newSd
operator|.
name|setCols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|,
name|col3
argument_list|,
name|col4
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|newTable
init|=
operator|new
name|Table
argument_list|(
name|oldTable
argument_list|)
decl_stmt|;
name|newTable
operator|.
name|setSd
argument_list|(
name|newSd
argument_list|)
expr_stmt|;
name|RawStore
name|msdb
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RawStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"shouldn't be called"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|msdb
argument_list|)
operator|.
name|getTableColumnStatistics
argument_list|(
name|oldTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|oldTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
argument_list|)
expr_stmt|;
name|HiveAlterHandler
name|handler
init|=
operator|new
name|HiveAlterHandler
argument_list|()
decl_stmt|;
name|handler
operator|.
name|alterTableUpdateTableColumnStats
argument_list|(
name|msdb
argument_list|,
name|oldTable
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterTableDelColUpdateStats
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
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
name|FieldSchema
name|col4
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col4"
argument_list|,
literal|"string"
argument_list|,
literal|"col4 comment"
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|oldSd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|oldSd
operator|.
name|setCols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|,
name|col3
argument_list|,
name|col4
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|oldTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|oldTable
operator|.
name|setDbName
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setTableName
argument_list|(
literal|"test_table"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setSd
argument_list|(
name|oldSd
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|newSd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|oldSd
argument_list|)
decl_stmt|;
name|newSd
operator|.
name|setCols
argument_list|(
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
expr_stmt|;
name|Table
name|newTable
init|=
operator|new
name|Table
argument_list|(
name|oldTable
argument_list|)
decl_stmt|;
name|newTable
operator|.
name|setSd
argument_list|(
name|newSd
argument_list|)
expr_stmt|;
name|RawStore
name|msdb
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RawStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveAlterHandler
name|handler
init|=
operator|new
name|HiveAlterHandler
argument_list|()
decl_stmt|;
name|handler
operator|.
name|alterTableUpdateTableColumnStats
argument_list|(
name|msdb
argument_list|,
name|oldTable
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|msdb
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|getTableColumnStatistics
argument_list|(
name|oldTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|oldTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|,
literal|"col4"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterTableChangePosNotUpdateStats
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
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
name|FieldSchema
name|col4
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"col4"
argument_list|,
literal|"string"
argument_list|,
literal|"col4 comment"
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|oldSd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|oldSd
operator|.
name|setCols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col2
argument_list|,
name|col3
argument_list|,
name|col4
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|oldTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|oldTable
operator|.
name|setDbName
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setTableName
argument_list|(
literal|"test_table"
argument_list|)
expr_stmt|;
name|oldTable
operator|.
name|setSd
argument_list|(
name|oldSd
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|newSd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|oldSd
argument_list|)
decl_stmt|;
name|newSd
operator|.
name|setCols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|col1
argument_list|,
name|col4
argument_list|,
name|col2
argument_list|,
name|col3
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|newTable
init|=
operator|new
name|Table
argument_list|(
name|oldTable
argument_list|)
decl_stmt|;
name|newTable
operator|.
name|setSd
argument_list|(
name|newSd
argument_list|)
expr_stmt|;
name|RawStore
name|msdb
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RawStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"shouldn't be called"
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|msdb
argument_list|)
operator|.
name|getTableColumnStatistics
argument_list|(
name|oldTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|oldTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|,
literal|"col4"
argument_list|)
argument_list|)
expr_stmt|;
name|HiveAlterHandler
name|handler
init|=
operator|new
name|HiveAlterHandler
argument_list|()
decl_stmt|;
name|handler
operator|.
name|alterTableUpdateTableColumnStats
argument_list|(
name|msdb
argument_list|,
name|oldTable
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

