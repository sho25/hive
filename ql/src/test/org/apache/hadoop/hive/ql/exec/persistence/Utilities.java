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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|SerDeUtils
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
name|BytesWritable
import|;
end_import

begin_class
class|class
name|Utilities
block|{
specifier|static
name|void
name|testEquality
parameter_list|(
name|MapJoinKeyObject
name|key1
parameter_list|,
name|MapJoinKeyObject
name|key2
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|key1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|key2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|key1
argument_list|,
name|key2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|key1
operator|.
name|getKeyLength
argument_list|()
argument_list|,
name|key2
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|key1
operator|.
name|equals
argument_list|(
name|key2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
name|MapJoinKeyObject
name|serde
parameter_list|(
name|MapJoinKeyObject
name|key
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|types
parameter_list|)
throws|throws
name|Exception
block|{
name|MapJoinKeyObject
name|result
init|=
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|ByteArrayInputStream
name|bais
decl_stmt|;
name|ObjectInputStream
name|in
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ObjectOutputStream
name|out
init|=
operator|new
name|ObjectOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|LazyBinarySerDe
name|serde
init|=
operator|new
name|LazyBinarySerDe
argument_list|()
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|types
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
literal|null
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|MapJoinObjectSerDeContext
name|context
init|=
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|serde
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|key
operator|.
name|write
argument_list|(
name|context
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|bais
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|bais
argument_list|)
expr_stmt|;
name|result
operator|.
name|read
argument_list|(
name|context
argument_list|,
name|in
argument_list|,
operator|new
name|BytesWritable
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|static
name|void
name|testEquality
parameter_list|(
name|MapJoinRowContainer
name|container1
parameter_list|,
name|MapJoinRowContainer
name|container2
parameter_list|)
throws|throws
name|HiveException
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|container1
operator|.
name|rowCount
argument_list|()
argument_list|,
name|container2
operator|.
name|rowCount
argument_list|()
argument_list|)
expr_stmt|;
name|AbstractRowContainer
operator|.
name|RowIterator
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|iter1
init|=
name|container1
operator|.
name|rowIter
argument_list|()
decl_stmt|,
name|iter2
init|=
name|container2
operator|.
name|rowIter
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Object
argument_list|>
name|row1
init|=
name|iter1
operator|.
name|first
argument_list|()
init|,
name|row2
init|=
name|iter2
operator|.
name|first
argument_list|()
init|;
name|row1
operator|!=
literal|null
operator|&&
name|row2
operator|!=
literal|null
condition|;
name|row1
operator|=
name|iter1
operator|.
name|next
argument_list|()
operator|,
name|row2
operator|=
name|iter2
operator|.
name|next
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|row1
argument_list|,
name|row2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|MapJoinEagerRowContainer
name|serde
parameter_list|(
name|MapJoinRowContainer
name|container
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|types
parameter_list|)
throws|throws
name|Exception
block|{
name|MapJoinEagerRowContainer
name|result
init|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
decl_stmt|;
name|ByteArrayInputStream
name|bais
decl_stmt|;
name|ObjectInputStream
name|in
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ObjectOutputStream
name|out
init|=
operator|new
name|ObjectOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|LazyBinarySerDe
name|serde
init|=
operator|new
name|LazyBinarySerDe
argument_list|()
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|types
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
literal|null
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|MapJoinObjectSerDeContext
name|context
init|=
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|serde
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|container
operator|.
name|write
argument_list|(
name|context
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|bais
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|bais
argument_list|)
expr_stmt|;
name|result
operator|.
name|read
argument_list|(
name|context
argument_list|,
name|in
argument_list|,
operator|new
name|BytesWritable
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

