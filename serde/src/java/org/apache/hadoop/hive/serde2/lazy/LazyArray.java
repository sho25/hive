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
name|lazy
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
name|objectinspector
operator|.
name|LazyListObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyArray stores an array of Lazy Objects.  *  * LazyArray does not deal with the case of a NULL array. That is handled by the  * parent LazyObject.  */
end_comment

begin_class
specifier|public
class|class
name|LazyArray
extends|extends
name|LazyNonPrimitive
argument_list|<
name|LazyListObjectInspector
argument_list|>
block|{
comment|/**    * Whether the data is already parsed or not.    */
name|boolean
name|parsed
init|=
literal|false
decl_stmt|;
comment|/**    * The length of the array. Only valid when the data is parsed. -1 when the    * array is NULL.    */
name|int
name|arrayLength
init|=
literal|0
decl_stmt|;
comment|/**    * The start positions of array elements. Only valid when the data is parsed.    * Note that startPosition[arrayLength] = begin + length + 1; that makes sure    * we can use the same formula to compute the length of each element of the    * array.    */
name|int
index|[]
name|startPosition
decl_stmt|;
comment|/**    * Whether init() has been called on the element or not.    */
name|boolean
index|[]
name|elementInited
decl_stmt|;
comment|/**    * The elements of the array. Note that we do arrayElements[i]. init(bytes,    * begin, length) only when that element is accessed.    */
name|LazyObject
index|[]
name|arrayElements
decl_stmt|;
comment|/**    * Construct a LazyArray object with the ObjectInspector.    *    * @param oi    *          the oi representing the type of this LazyArray as well as meta    *          information like separator etc.    */
specifier|protected
name|LazyArray
parameter_list|(
name|LazyListObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the row data for this LazyArray.    *    * @see LazyObject#init(ByteArrayRef, int, int)    */
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
name|cachedList
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Enlarge the size of arrays storing information for the elements inside the    * array.    */
specifier|private
name|void
name|enlargeArrays
parameter_list|()
block|{
if|if
condition|(
name|startPosition
operator|==
literal|null
condition|)
block|{
name|int
name|initialSize
init|=
literal|2
decl_stmt|;
name|startPosition
operator|=
operator|new
name|int
index|[
name|initialSize
index|]
expr_stmt|;
name|arrayElements
operator|=
operator|new
name|LazyObject
index|[
name|initialSize
index|]
expr_stmt|;
name|elementInited
operator|=
operator|new
name|boolean
index|[
name|initialSize
index|]
expr_stmt|;
block|}
else|else
block|{
name|startPosition
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|startPosition
argument_list|,
name|startPosition
operator|.
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
name|arrayElements
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|arrayElements
argument_list|,
name|arrayElements
operator|.
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
name|elementInited
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|elementInited
argument_list|,
name|elementInited
operator|.
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Parse the bytes and fill arrayLength and startPosition.    */
specifier|private
name|void
name|parse
parameter_list|()
block|{
name|parsed
operator|=
literal|true
expr_stmt|;
name|byte
name|separator
init|=
name|oi
operator|.
name|getSeparator
argument_list|()
decl_stmt|;
name|boolean
name|isEscaped
init|=
name|oi
operator|.
name|isEscaped
argument_list|()
decl_stmt|;
name|byte
name|escapeChar
init|=
name|oi
operator|.
name|getEscapeChar
argument_list|()
decl_stmt|;
comment|// empty array?
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
name|arrayLength
operator|=
literal|0
expr_stmt|;
return|return;
block|}
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
name|arrayLength
operator|=
literal|0
expr_stmt|;
name|int
name|arrayByteEnd
init|=
name|start
operator|+
name|length
decl_stmt|;
name|int
name|elementByteBegin
init|=
name|start
decl_stmt|;
name|int
name|elementByteEnd
init|=
name|start
decl_stmt|;
comment|// Go through all bytes in the byte[]
while|while
condition|(
name|elementByteEnd
operator|<=
name|arrayByteEnd
condition|)
block|{
comment|// Reached the end of a field?
if|if
condition|(
name|elementByteEnd
operator|==
name|arrayByteEnd
operator|||
name|bytes
index|[
name|elementByteEnd
index|]
operator|==
name|separator
condition|)
block|{
comment|// Array size not big enough?
if|if
condition|(
name|startPosition
operator|==
literal|null
operator|||
name|arrayLength
operator|+
literal|1
operator|==
name|startPosition
operator|.
name|length
condition|)
block|{
name|enlargeArrays
argument_list|()
expr_stmt|;
block|}
name|startPosition
index|[
name|arrayLength
index|]
operator|=
name|elementByteBegin
expr_stmt|;
name|arrayLength
operator|++
expr_stmt|;
name|elementByteBegin
operator|=
name|elementByteEnd
operator|+
literal|1
expr_stmt|;
name|elementByteEnd
operator|++
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|isEscaped
operator|&&
name|bytes
index|[
name|elementByteEnd
index|]
operator|==
name|escapeChar
operator|&&
name|elementByteEnd
operator|+
literal|1
operator|<
name|arrayByteEnd
condition|)
block|{
comment|// ignore the char after escape_char
name|elementByteEnd
operator|+=
literal|2
expr_stmt|;
block|}
else|else
block|{
name|elementByteEnd
operator|++
expr_stmt|;
block|}
block|}
block|}
comment|// Store arrayByteEnd+1 in startPosition[arrayLength]
comment|// so that we can use the same formula to compute the length of
comment|// each element in the array: startPosition[i+1] - startPosition[i] - 1
name|startPosition
index|[
name|arrayLength
index|]
operator|=
name|arrayByteEnd
operator|+
literal|1
expr_stmt|;
if|if
condition|(
name|arrayLength
operator|>
literal|0
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|elementInited
argument_list|,
literal|0
argument_list|,
name|arrayLength
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns the actual primitive object at the index position inside the array    * represented by this LazyObject.    */
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
name|arrayLength
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
comment|/**    * Get the element without checking out-of-bound index.    */
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
name|elementInited
index|[
name|index
index|]
condition|)
block|{
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
name|elementInited
index|[
name|index
index|]
operator|=
literal|true
expr_stmt|;
name|int
name|elementStart
init|=
name|startPosition
index|[
name|index
index|]
decl_stmt|;
name|int
name|elementLength
init|=
name|startPosition
index|[
name|index
operator|+
literal|1
index|]
operator|-
name|elementStart
operator|-
literal|1
decl_stmt|;
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
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|oi
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isNull
argument_list|(
name|oi
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|elementStart
argument_list|,
name|elementLength
argument_list|)
condition|)
block|{
name|arrayElements
index|[
name|index
index|]
operator|.
name|setNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
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
argument_list|,
name|elementLength
argument_list|)
expr_stmt|;
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
comment|/**    * Returns -1 for null array.    */
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
name|arrayLength
return|;
block|}
comment|/**    * cachedList is reused every time getList is called. Different LazyArray    * instances cannot share the same cachedList.    */
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
name|arrayLength
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|cachedList
operator|!=
literal|null
condition|)
block|{
return|return
name|cachedList
return|;
block|}
name|cachedList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|arrayLength
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
name|arrayLength
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

