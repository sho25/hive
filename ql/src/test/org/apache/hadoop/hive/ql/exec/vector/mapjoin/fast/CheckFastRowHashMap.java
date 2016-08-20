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
name|vector
operator|.
name|mapjoin
operator|.
name|fast
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|exec
operator|.
name|JoinUtil
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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashMapResult
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
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableKeyType
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
name|ByteWritable
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
name|ShortWritable
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
name|fast
operator|.
name|LazyBinaryDeserializeRead
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|BooleanWritable
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|IntWritable
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
name|LongWritable
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|CheckFastRowHashMap
extends|extends
name|CheckFastHashTable
block|{
specifier|public
specifier|static
name|void
name|verifyHashMapRows
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rows
parameter_list|,
name|int
index|[]
name|actualToValueMap
parameter_list|,
name|VectorMapJoinHashMapResult
name|hashMapResult
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|count
init|=
name|rows
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|int
name|columnCount
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
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
for|for
control|(
name|int
name|a
init|=
literal|0
init|;
name|a
operator|<
name|count
condition|;
name|a
operator|++
control|)
block|{
name|int
name|valueIndex
init|=
name|actualToValueMap
index|[
name|a
index|]
decl_stmt|;
name|Object
index|[]
name|row
init|=
name|rows
operator|.
name|get
argument_list|(
name|valueIndex
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ref
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|offset
init|=
operator|(
name|int
operator|)
name|ref
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|ref
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|typeInfos
argument_list|)
decl_stmt|;
name|lazyBinaryDeserializeRead
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|columnCount
condition|;
name|index
operator|++
control|)
block|{
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|VerifyFastRow
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfos
index|[
name|index
index|]
argument_list|,
name|writable
argument_list|)
expr_stmt|;
block|}
name|lazyBinaryDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondConfiguredFieldsWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|bufferRangeHasExtraDataWarned
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
if|if
condition|(
name|a
operator|==
name|count
operator|-
literal|1
condition|)
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|ref
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|ref
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|String
name|debugDetailedReadPositionString
decl_stmt|;
specifier|private
specifier|static
name|String
name|debugDetailedHashMapResultPositionString
decl_stmt|;
specifier|private
specifier|static
name|String
name|debugExceptionMessage
decl_stmt|;
specifier|private
specifier|static
name|StackTraceElement
index|[]
name|debugStackTrace
decl_stmt|;
specifier|public
specifier|static
name|void
name|verifyHashMapRowsMore
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rows
parameter_list|,
name|int
index|[]
name|actualToValueMap
parameter_list|,
name|VectorMapJoinHashMapResult
name|hashMapResult
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|int
name|clipIndex
parameter_list|,
name|boolean
name|useExactBytes
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|count
init|=
name|rows
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|int
name|columnCount
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
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
for|for
control|(
name|int
name|a
init|=
literal|0
init|;
name|a
operator|<
name|count
condition|;
name|a
operator|++
control|)
block|{
name|int
name|valueIndex
init|=
name|actualToValueMap
index|[
name|a
index|]
decl_stmt|;
name|Object
index|[]
name|row
init|=
name|rows
operator|.
name|get
argument_list|(
name|valueIndex
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ref
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|offset
init|=
operator|(
name|int
operator|)
name|ref
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|ref
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|a
operator|==
name|clipIndex
condition|)
block|{
name|length
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|useExactBytes
condition|)
block|{
comment|// Use exact byte array which might generate array out of bounds...
name|bytes
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
expr_stmt|;
name|offset
operator|=
literal|0
expr_stmt|;
block|}
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|typeInfos
argument_list|)
decl_stmt|;
name|lazyBinaryDeserializeRead
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|boolean
name|thrown
init|=
literal|false
decl_stmt|;
name|Exception
name|saveException
init|=
literal|null
decl_stmt|;
name|boolean
name|notExpected
init|=
literal|false
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|index
operator|=
literal|0
init|;
name|index
operator|<
name|columnCount
condition|;
name|index
operator|++
control|)
block|{
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|VerifyFastRow
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfos
index|[
name|index
index|]
argument_list|,
name|writable
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|thrown
operator|=
literal|true
expr_stmt|;
name|saveException
operator|=
name|e
expr_stmt|;
name|debugDetailedReadPositionString
operator|=
name|lazyBinaryDeserializeRead
operator|.
name|getDetailedReadPositionString
argument_list|()
expr_stmt|;
name|debugDetailedHashMapResultPositionString
operator|=
name|hashMapResult
operator|.
name|getDetailedHashMapResultPositionString
argument_list|()
expr_stmt|;
name|debugExceptionMessage
operator|=
name|saveException
operator|.
name|getMessage
argument_list|()
expr_stmt|;
name|debugStackTrace
operator|=
name|saveException
operator|.
name|getStackTrace
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|a
operator|==
name|clipIndex
condition|)
block|{
if|if
condition|(
operator|!
name|thrown
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Expecting an exception to be thrown for the clipped case..."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|saveException
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|saveException
operator|instanceof
name|EOFException
condition|)
block|{
comment|// This is the one we are expecting.
block|}
elseif|else
if|if
condition|(
name|saveException
operator|instanceof
name|ArrayIndexOutOfBoundsException
condition|)
block|{
name|notExpected
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Expecting an EOFException to be thrown for the clipped case..."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|thrown
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Not expecting an exception to be thrown for the non-clipped case..."
argument_list|)
expr_stmt|;
block|}
name|lazyBinaryDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondConfiguredFieldsWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|bufferRangeHasExtraDataWarned
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ref
operator|=
name|hashMapResult
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|a
operator|==
name|count
operator|-
literal|1
condition|)
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|ref
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|ref
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Element for Key: row and byte[] x Hash Table: HashMap    */
specifier|public
specifier|static
class|class
name|FastRowHashMapElement
block|{
specifier|private
name|byte
index|[]
name|key
decl_stmt|;
specifier|private
name|Object
index|[]
name|keyRow
decl_stmt|;
specifier|private
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|valueRows
decl_stmt|;
specifier|public
name|FastRowHashMapElement
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|Object
index|[]
name|keyRow
parameter_list|,
name|byte
index|[]
name|firstValue
parameter_list|,
name|Object
index|[]
name|valueRow
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|keyRow
operator|=
name|keyRow
expr_stmt|;
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|firstValue
argument_list|)
expr_stmt|;
name|valueRows
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|valueRows
operator|.
name|add
argument_list|(
name|valueRow
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|public
name|Object
index|[]
name|getKeyRow
parameter_list|()
block|{
return|return
name|keyRow
return|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|values
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|values
return|;
block|}
specifier|public
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|getValueRows
parameter_list|()
block|{
return|return
name|valueRows
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|Object
index|[]
name|valueRow
parameter_list|)
block|{
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|valueRows
operator|.
name|add
argument_list|(
name|valueRow
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Verify table for Key: row and byte[] x Hash Table: HashMap    */
specifier|public
specifier|static
class|class
name|VerifyFastRowHashMap
block|{
specifier|private
name|int
name|count
decl_stmt|;
specifier|private
name|FastRowHashMapElement
index|[]
name|array
decl_stmt|;
specifier|private
name|TreeMap
argument_list|<
name|BytesWritable
argument_list|,
name|Integer
argument_list|>
name|keyValueMap
decl_stmt|;
specifier|public
name|VerifyFastRowHashMap
parameter_list|()
block|{
name|count
operator|=
literal|0
expr_stmt|;
name|array
operator|=
operator|new
name|FastRowHashMapElement
index|[
literal|50
index|]
expr_stmt|;
comment|// We use BytesWritable because it supports Comparable for our TreeMap.
name|keyValueMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|BytesWritable
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|BytesWritable
name|keyBytesWritable
init|=
operator|new
name|BytesWritable
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|length
argument_list|)
decl_stmt|;
return|return
name|keyValueMap
operator|.
name|containsKey
argument_list|(
name|keyBytesWritable
argument_list|)
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|Object
index|[]
name|keyRow
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Object
index|[]
name|valueRow
parameter_list|)
block|{
name|BytesWritable
name|keyBytesWritable
init|=
operator|new
name|BytesWritable
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyValueMap
operator|.
name|containsKey
argument_list|(
name|keyBytesWritable
argument_list|)
condition|)
block|{
name|int
name|index
init|=
name|keyValueMap
operator|.
name|get
argument_list|(
name|keyBytesWritable
argument_list|)
decl_stmt|;
name|array
index|[
name|index
index|]
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|valueRow
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|count
operator|>=
name|array
operator|.
name|length
condition|)
block|{
comment|// Grow.
name|FastRowHashMapElement
index|[]
name|newArray
init|=
operator|new
name|FastRowHashMapElement
index|[
name|array
operator|.
name|length
operator|*
literal|2
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
literal|0
argument_list|,
name|newArray
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|array
operator|=
name|newArray
expr_stmt|;
block|}
name|array
index|[
name|count
index|]
operator|=
operator|new
name|FastRowHashMapElement
argument_list|(
name|key
argument_list|,
name|keyRow
argument_list|,
name|value
argument_list|,
name|valueRow
argument_list|)
expr_stmt|;
name|keyValueMap
operator|.
name|put
argument_list|(
name|keyBytesWritable
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
specifier|public
name|byte
index|[]
name|addRandomExisting
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|Object
index|[]
name|valueRow
parameter_list|,
name|Random
name|r
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|count
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|index
init|=
name|r
operator|.
name|nextInt
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|array
index|[
name|index
index|]
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|valueRow
argument_list|)
expr_stmt|;
return|return
name|array
index|[
name|index
index|]
operator|.
name|getKey
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getKey
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|array
index|[
name|index
index|]
operator|.
name|getKey
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|getValues
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|array
index|[
name|index
index|]
operator|.
name|getValues
argument_list|()
return|;
block|}
specifier|public
name|void
name|verify
parameter_list|(
name|VectorMapJoinFastHashTable
name|map
parameter_list|,
name|HashTableKeyType
name|hashTableKeyType
parameter_list|,
name|PrimitiveTypeInfo
index|[]
name|valuePrimitiveTypeInfos
parameter_list|,
name|boolean
name|doClipping
parameter_list|,
name|boolean
name|useExactBytes
parameter_list|,
name|Random
name|random
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|mapSize
init|=
name|map
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapSize
operator|!=
name|count
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"map.size() does not match expected count"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|count
condition|;
name|index
operator|++
control|)
block|{
name|FastRowHashMapElement
name|element
init|=
name|array
index|[
name|index
index|]
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
init|=
name|element
operator|.
name|getValues
argument_list|()
decl_stmt|;
name|VectorMapJoinHashMapResult
name|hashMapResult
init|=
literal|null
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
init|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
decl_stmt|;
switch|switch
condition|(
name|hashTableKeyType
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
block|{
name|Object
index|[]
name|keyRow
init|=
name|element
operator|.
name|getKeyRow
argument_list|()
decl_stmt|;
name|Object
name|keyObject
init|=
name|keyRow
index|[
literal|0
index|]
decl_stmt|;
name|VectorMapJoinFastLongHashMap
name|longHashMap
init|=
operator|(
name|VectorMapJoinFastLongHashMap
operator|)
name|map
decl_stmt|;
name|hashMapResult
operator|=
name|longHashMap
operator|.
name|createHashMapResult
argument_list|()
expr_stmt|;
name|long
name|longKey
decl_stmt|;
switch|switch
condition|(
name|hashTableKeyType
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|longKey
operator|=
operator|(
operator|(
name|BooleanWritable
operator|)
name|keyObject
operator|)
operator|.
name|get
argument_list|()
condition|?
literal|1
else|:
literal|0
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|longKey
operator|=
operator|(
operator|(
name|ByteWritable
operator|)
name|keyObject
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|longKey
operator|=
operator|(
operator|(
name|ShortWritable
operator|)
name|keyObject
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|longKey
operator|=
operator|(
operator|(
name|IntWritable
operator|)
name|keyObject
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|longKey
operator|=
operator|(
operator|(
name|LongWritable
operator|)
name|keyObject
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected hash table key type "
operator|+
name|hashTableKeyType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
name|joinResult
operator|=
name|longHashMap
operator|.
name|lookup
argument_list|(
name|longKey
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
if|if
condition|(
name|joinResult
operator|!=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|)
block|{
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|STRING
case|:
block|{
name|Object
index|[]
name|keyRow
init|=
name|element
operator|.
name|getKeyRow
argument_list|()
decl_stmt|;
name|Object
name|keyObject
init|=
name|keyRow
index|[
literal|0
index|]
decl_stmt|;
name|VectorMapJoinFastStringHashMap
name|stringHashMap
init|=
operator|(
name|VectorMapJoinFastStringHashMap
operator|)
name|map
decl_stmt|;
name|hashMapResult
operator|=
name|stringHashMap
operator|.
name|createHashMapResult
argument_list|()
expr_stmt|;
name|Text
name|text
init|=
operator|(
name|Text
operator|)
name|keyObject
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|text
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|text
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|joinResult
operator|=
name|stringHashMap
operator|.
name|lookup
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|length
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
if|if
condition|(
name|joinResult
operator|!=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|)
block|{
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|MULTI_KEY
case|:
block|{
name|byte
index|[]
name|keyBytes
init|=
name|element
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|VectorMapJoinFastMultiKeyHashMap
name|stringHashMap
init|=
operator|(
name|VectorMapJoinFastMultiKeyHashMap
operator|)
name|map
decl_stmt|;
name|hashMapResult
operator|=
name|stringHashMap
operator|.
name|createHashMapResult
argument_list|()
expr_stmt|;
name|joinResult
operator|=
name|stringHashMap
operator|.
name|lookup
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyBytes
operator|.
name|length
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
if|if
condition|(
name|joinResult
operator|!=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|)
block|{
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected hash table key type "
operator|+
name|hashTableKeyType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
name|int
index|[]
name|actualToValueMap
init|=
name|verifyHashMapValues
argument_list|(
name|hashMapResult
argument_list|,
name|values
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rows
init|=
name|element
operator|.
name|getValueRows
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|doClipping
operator|&&
operator|!
name|useExactBytes
condition|)
block|{
name|verifyHashMapRows
argument_list|(
name|rows
argument_list|,
name|actualToValueMap
argument_list|,
name|hashMapResult
argument_list|,
name|valuePrimitiveTypeInfos
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|clipIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|rows
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|verifyHashMapRowsMore
argument_list|(
name|rows
argument_list|,
name|actualToValueMap
argument_list|,
name|hashMapResult
argument_list|,
name|valuePrimitiveTypeInfos
argument_list|,
name|clipIndex
argument_list|,
name|useExactBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

