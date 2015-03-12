begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Order
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
name|SkewedInfo
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TestSharedStorageDescriptor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHBaseStore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|location
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setLocation
argument_list|(
literal|"here"
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|setLocation
argument_list|(
literal|"there"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|changeOnInputFormat
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
literal|"input"
argument_list|)
expr_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|ssd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|setInputFormat
argument_list|(
literal|"different"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"different"
argument_list|,
name|ssd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|changeOnSerde
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|SerDeInfo
name|serde
init|=
operator|new
name|SerDeInfo
argument_list|()
decl_stmt|;
name|serde
operator|.
name|setName
argument_list|(
literal|"serde"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
name|serde
argument_list|)
expr_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"serde"
argument_list|,
name|ssd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setName
argument_list|(
literal|"different"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"serde"
argument_list|,
name|serde
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"different"
argument_list|,
name|ssd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"serde"
argument_list|,
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|multipleChangesDontCauseMultipleCopies
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
literal|"input"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
literal|"output"
argument_list|)
expr_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|ssd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|setInputFormat
argument_list|(
literal|"different"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"different"
argument_list|,
name|ssd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|keep
init|=
name|ssd
operator|.
name|getShared
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setOutputFormat
argument_list|(
literal|"different_output"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"different"
argument_list|,
name|ssd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"different_output"
argument_list|,
name|ssd
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"output"
argument_list|,
name|sd
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|keep
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|changeOrder
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|addToSortCols
argument_list|(
operator|new
name|Order
argument_list|(
literal|"fred"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ssd
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOrder
argument_list|()
argument_list|)
expr_stmt|;
name|ssd
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|setOrder
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ssd
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOrder
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOrder
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|changeOrderList
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|addToSortCols
argument_list|(
operator|new
name|Order
argument_list|(
literal|"fred"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|SharedStorageDescriptor
name|ssd
init|=
operator|new
name|SharedStorageDescriptor
argument_list|()
decl_stmt|;
name|ssd
operator|.
name|setShared
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ssd
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOrder
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Order
argument_list|>
name|list
init|=
name|ssd
operator|.
name|getSortCols
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Order
argument_list|(
literal|"bob"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sd
operator|==
name|ssd
operator|.
name|getShared
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ssd
operator|.
name|getSortColsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd
operator|.
name|getSortColsSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

