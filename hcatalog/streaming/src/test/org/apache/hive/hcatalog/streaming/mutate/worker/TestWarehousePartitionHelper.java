begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|assertThat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|conf
operator|.
name|Configuration
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestWarehousePartitionHelper
block|{
specifier|private
specifier|static
specifier|final
name|Configuration
name|CONFIGURATION
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|TABLE_PATH
init|=
operator|new
name|Path
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|UNPARTITIONED_COLUMNS
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|UNPARTITIONED_VALUES
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|PARTITIONED_COLUMNS
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"A"
argument_list|,
literal|"B"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|PARTITIONED_VALUES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|PartitionHelper
name|unpartitionedHelper
decl_stmt|;
specifier|private
specifier|final
name|PartitionHelper
name|partitionedHelper
decl_stmt|;
specifier|public
name|TestWarehousePartitionHelper
parameter_list|()
throws|throws
name|Exception
block|{
name|unpartitionedHelper
operator|=
operator|new
name|WarehousePartitionHelper
argument_list|(
name|CONFIGURATION
argument_list|,
name|TABLE_PATH
argument_list|,
name|UNPARTITIONED_COLUMNS
argument_list|)
expr_stmt|;
name|partitionedHelper
operator|=
operator|new
name|WarehousePartitionHelper
argument_list|(
name|CONFIGURATION
argument_list|,
name|TABLE_PATH
argument_list|,
name|PARTITIONED_COLUMNS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|UnsupportedOperationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|createNotSupported
parameter_list|()
throws|throws
name|Exception
block|{
name|unpartitionedHelper
operator|.
name|createPartitionIfNotExists
argument_list|(
name|UNPARTITIONED_VALUES
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getPathForUnpartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|path
init|=
name|unpartitionedHelper
operator|.
name|getPathForPartition
argument_list|(
name|UNPARTITIONED_VALUES
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|path
argument_list|,
name|is
argument_list|(
name|TABLE_PATH
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getPathForPartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|path
init|=
name|partitionedHelper
operator|.
name|getPathForPartition
argument_list|(
name|PARTITIONED_VALUES
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|path
argument_list|,
name|is
argument_list|(
operator|new
name|Path
argument_list|(
name|TABLE_PATH
argument_list|,
literal|"A=1/B=2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|closeSucceeds
parameter_list|()
throws|throws
name|IOException
block|{
name|partitionedHelper
operator|.
name|close
argument_list|()
expr_stmt|;
name|unpartitionedHelper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

