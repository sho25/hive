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
name|serde2
operator|.
name|teradata
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
name|io
operator|.
name|BaseEncoding
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
name|type
operator|.
name|Date
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
name|serde
operator|.
name|serdeConstants
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
name|io
operator|.
name|DateWritableV2
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
name|io
operator|.
name|BytesWritable
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
name|List
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

begin_comment
comment|/**  * Test the data type DATE for Teradata binary format.  */
end_comment

begin_class
specifier|public
class|class
name|TestTeradataBinarySerdeForDate
block|{
specifier|private
specifier|final
name|TeradataBinarySerde
name|serde
init|=
operator|new
name|TeradataBinarySerde
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"TD_DATE"
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"date"
argument_list|)
expr_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|props
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampBefore1900
parameter_list|()
throws|throws
name|Exception
block|{
comment|//0060-01-01
name|BytesWritable
name|in
init|=
operator|new
name|BytesWritable
argument_list|(
name|BaseEncoding
operator|.
name|base16
argument_list|()
operator|.
name|lowerCase
argument_list|()
operator|.
name|decode
argument_list|(
literal|"00653de7fe"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|Date
name|ts
init|=
operator|(
operator|(
name|DateWritableV2
operator|)
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getYear
argument_list|()
argument_list|,
literal|60
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getMonth
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getDay
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|BytesWritable
name|res
init|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|in
operator|.
name|copyBytes
argument_list|()
argument_list|,
name|res
operator|.
name|copyBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampAfter1900
parameter_list|()
throws|throws
name|Exception
block|{
comment|//9999-01-01
name|BytesWritable
name|in
init|=
operator|new
name|BytesWritable
argument_list|(
name|BaseEncoding
operator|.
name|base16
argument_list|()
operator|.
name|lowerCase
argument_list|()
operator|.
name|decode
argument_list|(
literal|"0095cfd304"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|Date
name|ts
init|=
operator|(
operator|(
name|DateWritableV2
operator|)
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getYear
argument_list|()
argument_list|,
literal|9999
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getMonth
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts
operator|.
name|getDay
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|BytesWritable
name|res
init|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|in
operator|.
name|copyBytes
argument_list|()
argument_list|,
name|res
operator|.
name|copyBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

