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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|HashSet
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
name|Random
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
name|ByteStream
operator|.
name|RandomAccessOutput
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
name|LazyBinaryUtils
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
name|SerDeException
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
name|WriteBuffers
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
specifier|public
class|class
name|TestBytesBytesMultiHashMap
block|{
specifier|private
specifier|static
specifier|final
name|float
name|LOAD_FACTOR
init|=
literal|0.75f
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|CAPACITY
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|WB_SIZE
init|=
literal|128
decl_stmt|;
comment|// Make sure we cross some buffer boundaries...
annotation|@
name|Test
specifier|public
name|void
name|testCapacityValidation
parameter_list|()
block|{
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|CAPACITY
argument_list|,
name|map
operator|.
name|getCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
literal|9
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|16
argument_list|,
name|map
operator|.
name|getCapacity
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the scenario when maxProbeSize is a very small value, it doesn't fail
name|BytesBytesMultiHashMap
name|map1
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
literal|1024
argument_list|,
operator|(
name|float
operator|)
literal|0.75
argument_list|,
literal|524288
argument_list|,
literal|1
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutGetOne
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|RandomKvSource
name|kv
init|=
operator|new
name|RandomKvSource
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv
operator|.
name|getLastKey
argument_list|()
argument_list|,
name|kv
operator|.
name|getLastValue
argument_list|()
argument_list|)
expr_stmt|;
name|kv
operator|=
operator|new
name|RandomKvSource
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv
operator|.
name|getLastKey
argument_list|()
argument_list|,
name|kv
operator|.
name|getLastValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutGetMultiple
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|RandomKvSource
name|kv
init|=
operator|new
name|RandomKvSource
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv
operator|.
name|getLastKey
argument_list|()
argument_list|,
name|kv
operator|.
name|getLastValue
argument_list|()
argument_list|)
expr_stmt|;
name|FixedKeyKvSource
name|kv2
init|=
operator|new
name|FixedKeyKvSource
argument_list|(
name|kv
operator|.
name|getLastKey
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|kv2
operator|.
name|values
operator|.
name|add
argument_list|(
name|kv
operator|.
name|getLastValue
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
literal|3
condition|;
operator|++
name|i
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|kv2
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv2
operator|.
name|key
argument_list|,
name|kv2
operator|.
name|values
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|kv2
operator|.
name|values
operator|.
name|size
argument_list|()
index|]
index|[]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetNonExistent
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|RandomKvSource
name|kv
init|=
operator|new
name|RandomKvSource
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|key
init|=
name|kv
operator|.
name|getLastKey
argument_list|()
decl_stmt|;
name|key
index|[
literal|0
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|key
index|[
literal|0
index|]
operator|+
literal|1
argument_list|)
expr_stmt|;
name|FixedKeyKvSource
name|kv2
init|=
operator|new
name|FixedKeyKvSource
argument_list|(
name|kv
operator|.
name|getLastKey
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|kv2
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|key
index|[
literal|0
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|key
index|[
literal|0
index|]
operator|+
literal|1
argument_list|)
expr_stmt|;
name|BytesBytesMultiHashMap
operator|.
name|Result
name|hashMapResult
init|=
operator|new
name|BytesBytesMultiHashMap
operator|.
name|Result
argument_list|()
decl_stmt|;
name|map
operator|.
name|getValueResult
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|hashMapResult
operator|.
name|hasRows
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|getValueResult
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|hashMapResult
operator|.
name|hasRows
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPutWithFullMap
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Make sure the map does not expand; should be able to find space.
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
name|CAPACITY
argument_list|,
literal|1f
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|UniqueKeysKvSource
name|kv
init|=
operator|new
name|UniqueKeysKvSource
argument_list|()
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
name|CAPACITY
condition|;
operator|++
name|i
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
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
name|kv
operator|.
name|keys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv
operator|.
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|kv
operator|.
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|CAPACITY
argument_list|,
name|map
operator|.
name|getCapacity
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get of non-existent key should terminate..
name|BytesBytesMultiHashMap
operator|.
name|Result
name|hashMapResult
init|=
operator|new
name|BytesBytesMultiHashMap
operator|.
name|Result
argument_list|()
decl_stmt|;
name|map
operator|.
name|getValueResult
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpand
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start with capacity 1; make sure we expand on every put.
name|BytesBytesMultiHashMap
name|map
init|=
operator|new
name|BytesBytesMultiHashMap
argument_list|(
literal|1
argument_list|,
literal|0.0000001f
argument_list|,
name|WB_SIZE
argument_list|)
decl_stmt|;
name|UniqueKeysKvSource
name|kv
init|=
operator|new
name|UniqueKeysKvSource
argument_list|()
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
literal|18
condition|;
operator|++
name|i
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|kv
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<=
name|i
condition|;
operator|++
name|j
control|)
block|{
name|verifyHashMapResult
argument_list|(
name|map
argument_list|,
name|kv
operator|.
name|keys
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|,
name|kv
operator|.
name|values
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
operator|<<
literal|18
argument_list|,
name|map
operator|.
name|getCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyHashMapResult
parameter_list|(
name|BytesBytesMultiHashMap
name|map
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
modifier|...
name|values
parameter_list|)
block|{
name|BytesBytesMultiHashMap
operator|.
name|Result
name|hashMapResult
init|=
operator|new
name|BytesBytesMultiHashMap
operator|.
name|Result
argument_list|()
decl_stmt|;
name|byte
name|state
init|=
name|map
operator|.
name|getValueResult
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|,
name|hashMapResult
argument_list|)
decl_stmt|;
name|HashSet
argument_list|<
name|ByteBuffer
argument_list|>
name|hs
init|=
operator|new
name|HashSet
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|hashMapResult
operator|.
name|hasRows
argument_list|()
condition|)
block|{
name|WriteBuffers
operator|.
name|ByteSegmentRef
name|ref
init|=
name|hashMapResult
operator|.
name|first
argument_list|()
decl_stmt|;
while|while
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
name|hs
operator|.
name|add
argument_list|(
name|ref
operator|.
name|copy
argument_list|()
argument_list|)
expr_stmt|;
name|ref
operator|=
name|hashMapResult
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|state
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|values
operator|.
name|length
argument_list|,
name|count
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
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|hs
operator|.
name|contains
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FixedKeyKvSource
extends|extends
name|RandomKvSource
block|{
specifier|private
name|byte
index|[]
name|key
decl_stmt|;
specifier|public
name|FixedKeyKvSource
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|minLength
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|super
argument_list|(
name|minLength
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeKey
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|dest
operator|.
name|write
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Thrown "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|UniqueKeysKvSource
extends|extends
name|RandomKvSource
block|{
specifier|private
name|long
name|lastKey
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
literal|9
index|]
decl_stmt|;
specifier|private
name|byte
index|[]
name|lastBuffer
decl_stmt|;
specifier|public
name|UniqueKeysKvSource
parameter_list|()
block|{
name|super
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeKey
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
throws|throws
name|SerDeException
block|{
name|lastKey
operator|+=
literal|465623573
expr_stmt|;
name|int
name|len
init|=
name|LazyBinaryUtils
operator|.
name|writeVLongToByteArray
argument_list|(
name|buffer
argument_list|,
name|lastKey
argument_list|)
decl_stmt|;
name|lastBuffer
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buffer
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|lastBuffer
argument_list|)
expr_stmt|;
name|writeLastBuffer
argument_list|(
name|dest
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeLastBuffer
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
block|{
try|try
block|{
name|dest
operator|.
name|write
argument_list|(
name|lastBuffer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Thrown "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeValue
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Assumes value is written after key.
name|values
operator|.
name|add
argument_list|(
name|lastBuffer
argument_list|)
expr_stmt|;
name|writeLastBuffer
argument_list|(
name|dest
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|RandomKvSource
implements|implements
name|BytesBytesMultiHashMap
operator|.
name|KvSource
block|{
specifier|private
name|int
name|minLength
decl_stmt|,
name|maxLength
decl_stmt|;
specifier|private
specifier|final
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|(
literal|43
argument_list|)
decl_stmt|;
specifier|public
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|,
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|RandomKvSource
parameter_list|(
name|int
name|minLength
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|this
operator|.
name|minLength
operator|=
name|minLength
expr_stmt|;
name|this
operator|.
name|maxLength
operator|=
name|maxLength
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getLastValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|values
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|getLastKey
parameter_list|()
block|{
return|return
name|keys
operator|.
name|get
argument_list|(
name|keys
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeKey
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
throws|throws
name|SerDeException
block|{
name|keys
operator|.
name|add
argument_list|(
name|write
argument_list|(
name|dest
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeValue
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
throws|throws
name|SerDeException
block|{
name|values
operator|.
name|add
argument_list|(
name|write
argument_list|(
name|dest
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|byte
index|[]
name|write
parameter_list|(
name|RandomAccessOutput
name|dest
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|minLength
operator|+
name|rdm
operator|.
name|nextInt
argument_list|(
name|maxLength
operator|-
name|minLength
operator|+
literal|1
argument_list|)
index|]
decl_stmt|;
name|rdm
operator|.
name|nextBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
try|try
block|{
name|dest
operator|.
name|write
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Thrown "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|updateStateByte
parameter_list|(
name|Byte
name|previousValue
parameter_list|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
name|previousValue
operator|==
literal|null
condition|?
literal|1
else|:
name|previousValue
operator|+
literal|1
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

