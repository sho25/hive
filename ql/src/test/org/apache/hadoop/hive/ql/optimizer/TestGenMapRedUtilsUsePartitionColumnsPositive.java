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
name|optimizer
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
name|Properties
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
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestGenMapRedUtilsUsePartitionColumnsPositive
block|{
annotation|@
name|Parameterized
operator|.
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: updatePartitions({2})"
argument_list|)
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|testCases
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|,
literal|"p2"
block|,
literal|"p2"
block|,
literal|"t2"
block|}
block|,
block|{
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|,
literal|"p2,p3"
block|,
literal|"p2/p3"
block|,
literal|"t2:t3"
block|}
block|,
block|{
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|,
literal|"p1,p2,p3"
block|,
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|}
block|,
block|{
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|,
literal|"p1,p3"
block|,
literal|"p1/p3"
block|,
literal|"t1:t3"
block|}
block|,
block|{
literal|"p1"
block|,
literal|"t1"
block|,
literal|"p1"
block|,
literal|"p1"
block|,
literal|"t1"
block|}
block|,
block|{
literal|"p1/p2/p3"
block|,
literal|"t1:t2:t3"
block|,
literal|"p3,p2,p1"
block|,
literal|"p3/p2/p1"
block|,
literal|"t3:t2:t1"
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|String
name|inPartColNames
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|1
argument_list|)
specifier|public
name|String
name|inPartColTypes
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|2
argument_list|)
specifier|public
name|String
name|partNamesToRetain
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|3
argument_list|)
specifier|public
name|String
name|expectedPartColNames
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameter
argument_list|(
literal|4
argument_list|)
specifier|public
name|String
name|expectedPartColTypes
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testUsePartitionColumns
parameter_list|()
block|{
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_PARTITION_COLUMNS
argument_list|,
name|inPartColNames
argument_list|)
expr_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_PARTITION_COLUMN_TYPES
argument_list|,
name|inPartColTypes
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|usePartitionColumns
argument_list|(
name|p
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|partNamesToRetain
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|actualNames
init|=
name|p
operator|.
name|getProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_PARTITION_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|actualTypes
init|=
name|p
operator|.
name|getProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_PARTITION_COLUMN_TYPES
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedPartColNames
argument_list|,
name|actualNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedPartColTypes
argument_list|,
name|actualTypes
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

