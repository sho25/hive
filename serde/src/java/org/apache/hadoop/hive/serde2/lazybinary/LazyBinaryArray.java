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
name|serde2
operator|.
name|lazybinary
package|;
end_package

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
name|lazy
operator|.
name|ByteArrayRef
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
name|lazy
operator|.
name|LazyObject
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
operator|.
name|RecordInfo
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
operator|.
name|VInt
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
name|objectinspector
operator|.
name|LazyBinaryListObjectInspector
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
name|objectinspector
operator|.
name|ListObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyBinaryArray is serialized as follows: start A b b b b b b end bytes[] ->  * |--------|---|---|---|---| ... |---|---|  *   * Section A is the null-bytes. Suppose the list has N elements, then there are  * (N+7)/8 bytes used as null-bytes. Each bit corresponds to an element and it  * indicates whether that element is null (0) or not null (1).  *   * After A, all b(s) represent the elements of the list. Each of them is again a  * LazyBinaryObject.  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryArray
extends|extends
name|LazyBinaryNonPrimitive
argument_list|<
name|LazyBinaryListObjectInspector
argument_list|>
block|{
comment|/**    * Whether the data is already parsed or not.    */
name|boolean
name|parsed
init|=
literal|false
decl_stmt|;
comment|/**    * The length of the array. Only valid when the data is parsed.    */
name|int
name|arraySize
init|=
literal|0
decl_stmt|;
comment|/**    * The start positions and lengths of array elements. Only valid when the data    * is parsed.    */
name|int
index|[]
name|elementStart
decl_stmt|;
name|int
index|[]
name|elementLength
decl_stmt|;
comment|/**    * Whether an element is initialized or not.    */
name|boolean
index|[]
name|elementInited
decl_stmt|;
comment|/**    * Whether an element is null or not. Because length is 0 does not means the    * field is null. In particular, a 0-length string is not null.    */
name|boolean
index|[]
name|elementIsNull
decl_stmt|;
comment|/**    * The elements of the array. Note that we call arrayElements[i].init(bytes,    * begin, length) only when that element is accessed.    */
name|LazyBinaryObject
index|[]
name|arrayElements
decl_stmt|;
comment|/**    * Construct a LazyBinaryArray object with the ObjectInspector.    *     * @param oi    *          the oi representing the type of this LazyBinaryArray    */
specifier|protected
name|LazyBinaryArray
parameter_list|(
name|LazyBinaryListObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the row data for this LazyBinaryArray.    *     * @see LazyObject#init(ByteArrayRef, int, int)    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|parsed
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Enlarge the size of arrays storing information for the elements inside the    * array.    */
specifier|private
name|void
name|adjustArraySize
parameter_list|(
name|int
name|newSize
parameter_list|)
block|{
if|if
condition|(
name|elementStart
operator|==
literal|null
operator|||
name|elementStart
operator|.
name|length
operator|<
name|newSize
condition|)
block|{
name|elementStart
operator|=
operator|new
name|int
index|[
name|newSize
index|]
expr_stmt|;
name|elementLength
operator|=
operator|new
name|int
index|[
name|newSize
index|]
expr_stmt|;
name|elementInited
operator|=
operator|new
name|boolean
index|[
name|newSize
index|]
expr_stmt|;
name|elementIsNull
operator|=
operator|new
name|boolean
index|[
name|newSize
index|]
expr_stmt|;
name|arrayElements
operator|=
operator|new
name|LazyBinaryObject
index|[
name|newSize
index|]
expr_stmt|;
block|}
block|}
name|VInt
name|vInt
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|VInt
argument_list|()
decl_stmt|;
name|RecordInfo
name|recordInfo
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|RecordInfo
argument_list|()
decl_stmt|;
comment|/**    * Parse the bytes and fill elementStart, elementLength, elementInited and    * elementIsNull.    */
specifier|private
name|void
name|parse
parameter_list|()
block|{
name|byte
index|[]
name|bytes
init|=
name|this
operator|.
name|bytes
operator|.
name|getData
argument_list|()
decl_stmt|;
comment|// get the vlong that represents the map size
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|arraySize
operator|=
name|vInt
operator|.
name|value
expr_stmt|;
if|if
condition|(
literal|0
operator|==
name|arraySize
condition|)
block|{
name|parsed
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// adjust arrays
name|adjustArraySize
argument_list|(
name|arraySize
argument_list|)
expr_stmt|;
comment|// find out the null-bytes
name|int
name|arryByteStart
init|=
name|start
operator|+
name|vInt
operator|.
name|length
decl_stmt|;
name|int
name|nullByteCur
init|=
name|arryByteStart
decl_stmt|;
name|int
name|nullByteEnd
init|=
name|arryByteStart
operator|+
operator|(
name|arraySize
operator|+
literal|7
operator|)
operator|/
literal|8
decl_stmt|;
comment|// the begin the real elements
name|int
name|lastElementByteEnd
init|=
name|nullByteEnd
decl_stmt|;
comment|// the list element object inspector
name|ObjectInspector
name|listEleObjectInspector
init|=
operator|(
operator|(
name|ListObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
comment|// parsing elements one by one
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arraySize
condition|;
name|i
operator|++
control|)
block|{
name|elementIsNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|(
name|bytes
index|[
name|nullByteCur
index|]
operator|&
operator|(
literal|1
operator|<<
operator|(
name|i
operator|%
literal|8
operator|)
operator|)
operator|)
operator|!=
literal|0
condition|)
block|{
name|elementIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|LazyBinaryUtils
operator|.
name|checkObjectByteInfo
argument_list|(
name|listEleObjectInspector
argument_list|,
name|bytes
argument_list|,
name|lastElementByteEnd
argument_list|,
name|recordInfo
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|elementStart
index|[
name|i
index|]
operator|=
name|lastElementByteEnd
operator|+
name|recordInfo
operator|.
name|elementOffset
expr_stmt|;
name|elementLength
index|[
name|i
index|]
operator|=
name|recordInfo
operator|.
name|elementSize
expr_stmt|;
name|lastElementByteEnd
operator|=
name|elementStart
index|[
name|i
index|]
operator|+
name|elementLength
index|[
name|i
index|]
expr_stmt|;
block|}
comment|// move onto the next null byte
if|if
condition|(
literal|7
operator|==
operator|(
name|i
operator|%
literal|8
operator|)
condition|)
block|{
name|nullByteCur
operator|++
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|elementInited
argument_list|,
literal|0
argument_list|,
name|arraySize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|parsed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Returns the actual primitive object at the index position inside the array    * represented by this LazyBinaryObject.    */
specifier|public
name|Object
name|getListElementObject
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|index
operator|>=
name|arraySize
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|uncheckedGetElement
argument_list|(
name|index
argument_list|)
return|;
block|}
comment|/**    * Get the element without checking out-of-bound index.    *     * @param index    *          index to the array element    */
specifier|private
name|Object
name|uncheckedGetElement
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|elementIsNull
index|[
name|index
index|]
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|elementInited
index|[
name|index
index|]
condition|)
block|{
name|elementInited
index|[
name|index
index|]
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|arrayElements
index|[
name|index
index|]
operator|==
literal|null
condition|)
block|{
name|arrayElements
index|[
name|index
index|]
operator|=
name|LazyBinaryFactory
operator|.
name|createLazyBinaryObject
argument_list|(
operator|(
name|oi
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|arrayElements
index|[
name|index
index|]
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|elementStart
index|[
name|index
index|]
argument_list|,
name|elementLength
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|arrayElements
index|[
name|index
index|]
operator|.
name|getObject
argument_list|()
return|;
block|}
comment|/**    * Returns the array size.    */
specifier|public
name|int
name|getListLength
parameter_list|()
block|{
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
return|return
name|arraySize
return|;
block|}
comment|/**    * cachedList is reused every time getList is called. Different    * LazyBianryArray instances cannot share the same cachedList.    */
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cachedList
decl_stmt|;
comment|/**    * Returns the List of actual primitive objects. Returns null for null array.    */
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getList
parameter_list|()
block|{
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cachedList
operator|==
literal|null
condition|)
block|{
name|cachedList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|arraySize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cachedList
operator|.
name|clear
argument_list|()
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
name|arraySize
condition|;
name|index
operator|++
control|)
block|{
name|cachedList
operator|.
name|add
argument_list|(
name|uncheckedGetElement
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|cachedList
return|;
block|}
block|}
end_class

end_unit

