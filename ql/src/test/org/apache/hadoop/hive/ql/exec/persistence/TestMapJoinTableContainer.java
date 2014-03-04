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
name|ql
operator|.
name|exec
operator|.
name|persistence
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|Text
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
name|TestMapJoinTableContainer
block|{
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|KEY
init|=
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"key"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|VALUE
init|=
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"value"
argument_list|)
block|}
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|baos
decl_stmt|;
specifier|private
name|ObjectOutputStream
name|out
decl_stmt|;
specifier|private
name|ObjectInputStream
name|in
decl_stmt|;
specifier|private
name|MapJoinTableContainer
name|container
decl_stmt|;
specifier|private
name|MapJoinTableContainerSerDe
name|containerSerde
decl_stmt|;
specifier|private
name|MapJoinKeyObject
name|key
decl_stmt|;
specifier|private
name|MapJoinRowContainer
name|rowContainer
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|key
operator|=
operator|new
name|MapJoinKeyObject
argument_list|(
name|KEY
argument_list|)
expr_stmt|;
name|rowContainer
operator|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
expr_stmt|;
name|rowContainer
operator|.
name|addRow
argument_list|(
name|VALUE
argument_list|)
expr_stmt|;
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|out
operator|=
operator|new
name|ObjectOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|LazyBinarySerDe
name|keySerde
init|=
operator|new
name|LazyBinarySerDe
argument_list|()
decl_stmt|;
name|Properties
name|keyProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|keyProps
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"v1"
argument_list|)
expr_stmt|;
name|keyProps
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|keySerde
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|keyProps
argument_list|)
expr_stmt|;
name|LazyBinarySerDe
name|valueSerde
init|=
operator|new
name|LazyBinarySerDe
argument_list|()
decl_stmt|;
name|Properties
name|valueProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|valueProps
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"v1"
argument_list|)
expr_stmt|;
name|valueProps
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|valueSerde
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|keyProps
argument_list|)
expr_stmt|;
name|containerSerde
operator|=
operator|new
name|MapJoinTableContainerSerDe
argument_list|(
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|keySerde
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|valueSerde
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|container
operator|=
operator|new
name|HashMapWrapper
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|container
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
name|containerSerde
operator|.
name|persist
argument_list|(
name|out
argument_list|,
name|container
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|container
operator|=
name|containerSerde
operator|.
name|load
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|rowContainer
argument_list|,
name|container
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDummyContainer
parameter_list|()
throws|throws
name|Exception
block|{
name|MapJoinTableContainerSerDe
operator|.
name|persistDummyTable
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|container
operator|=
name|containerSerde
operator|.
name|load
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|container
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|container
operator|.
name|entrySet
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

