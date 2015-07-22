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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|NullWritable
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

begin_comment
comment|/**  * This class supports string and binary data by value reference -- i.e. each field is  * explicitly present, as opposed to provided by a dictionary reference.  * In some cases, all the values will be in the same byte array to begin with,  * but this need not be the case. If each value is in a separate byte  * array to start with, or not all of the values are in the same original  * byte array, you can still assign data by reference into this column vector.  * This gives flexibility to use this in multiple situations.  *<p>  * When setting data by reference, the caller  * is responsible for allocating the byte arrays used to hold the data.  * You can also set data by value, as long as you call the initBuffer() method first.  * You can mix "by value" and "by reference" in the same column vector,  * though that use is probably not typical.  */
end_comment

begin_class
specifier|public
class|class
name|BytesColumnVector
extends|extends
name|ColumnVector
block|{
specifier|public
name|byte
index|[]
index|[]
name|vector
decl_stmt|;
specifier|public
name|int
index|[]
name|start
decl_stmt|;
comment|// start offset of each field
comment|/*    * The length of each field. If the value repeats for every entry, then it is stored    * in vector[0] and isRepeating from the superclass is set to true.    */
specifier|public
name|int
index|[]
name|length
decl_stmt|;
specifier|private
name|byte
index|[]
name|buffer
decl_stmt|;
comment|// optional buffer to use when actually copying in data
specifier|private
name|int
name|nextFree
decl_stmt|;
comment|// next free position in buffer
comment|// Reusable text object
specifier|private
specifier|final
name|Text
name|textObject
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
comment|// Estimate that there will be 16 bytes per entry
specifier|static
specifier|final
name|int
name|DEFAULT_BUFFER_SIZE
init|=
literal|16
operator|*
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
decl_stmt|;
comment|// Proportion of extra space to provide when allocating more buffer space.
specifier|static
specifier|final
name|float
name|EXTRA_SPACE_FACTOR
init|=
operator|(
name|float
operator|)
literal|1.2
decl_stmt|;
comment|/**    * Use this constructor for normal operation.    * All column vectors should be the default size normally.    */
specifier|public
name|BytesColumnVector
parameter_list|()
block|{
name|this
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Don't call this constructor except for testing purposes.    *    * @param size  number of elements in the column vector    */
specifier|public
name|BytesColumnVector
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|vector
operator|=
operator|new
name|byte
index|[
name|size
index|]
index|[]
expr_stmt|;
name|start
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
name|length
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
block|}
comment|/**    * Additional reset work for BytesColumnVector (releasing scratch bytes for by value strings).    */
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|initBuffer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/** Set a field by reference.    *    * @param elementNum index within column vector to set    * @param sourceBuf container of source data    * @param start start byte position within source    * @param length  length of source byte sequence    */
specifier|public
name|void
name|setRef
parameter_list|(
name|int
name|elementNum
parameter_list|,
name|byte
index|[]
name|sourceBuf
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|vector
index|[
name|elementNum
index|]
operator|=
name|sourceBuf
expr_stmt|;
name|this
operator|.
name|start
index|[
name|elementNum
index|]
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|length
index|[
name|elementNum
index|]
operator|=
name|length
expr_stmt|;
block|}
comment|/**    * You must call initBuffer first before using setVal().    * Provide the estimated number of bytes needed to hold    * a full column vector worth of byte string data.    *    * @param estimatedValueSize  Estimated size of buffer space needed    */
specifier|public
name|void
name|initBuffer
parameter_list|(
name|int
name|estimatedValueSize
parameter_list|)
block|{
name|nextFree
operator|=
literal|0
expr_stmt|;
comment|// if buffer is already allocated, keep using it, don't re-allocate
if|if
condition|(
name|buffer
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
comment|// allocate a little extra space to limit need to re-allocate
name|int
name|bufferSize
init|=
name|this
operator|.
name|vector
operator|.
name|length
operator|*
call|(
name|int
call|)
argument_list|(
name|estimatedValueSize
operator|*
name|EXTRA_SPACE_FACTOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|bufferSize
operator|<
name|DEFAULT_BUFFER_SIZE
condition|)
block|{
name|bufferSize
operator|=
name|DEFAULT_BUFFER_SIZE
expr_stmt|;
block|}
name|buffer
operator|=
operator|new
name|byte
index|[
name|bufferSize
index|]
expr_stmt|;
block|}
comment|/**    * Initialize buffer to default size.    */
specifier|public
name|void
name|initBuffer
parameter_list|()
block|{
name|initBuffer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return amount of buffer space currently allocated    */
specifier|public
name|int
name|bufferSize
parameter_list|()
block|{
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|buffer
operator|.
name|length
return|;
block|}
comment|/**    * Set a field by actually copying in to a local buffer.    * If you must actually copy data in to the array, use this method.    * DO NOT USE this method unless it's not practical to set data by reference with setRef().    * Setting data by reference tends to run a lot faster than copying data in.    *    * @param elementNum index within column vector to set    * @param sourceBuf container of source data    * @param start start byte position within source    * @param length  length of source byte sequence    */
specifier|public
name|void
name|setVal
parameter_list|(
name|int
name|elementNum
parameter_list|,
name|byte
index|[]
name|sourceBuf
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
operator|(
name|nextFree
operator|+
name|length
operator|)
operator|>
name|buffer
operator|.
name|length
condition|)
block|{
name|increaseBufferSpace
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|sourceBuf
argument_list|,
name|start
argument_list|,
name|buffer
argument_list|,
name|nextFree
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|vector
index|[
name|elementNum
index|]
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|start
index|[
name|elementNum
index|]
operator|=
name|nextFree
expr_stmt|;
name|this
operator|.
name|length
index|[
name|elementNum
index|]
operator|=
name|length
expr_stmt|;
name|nextFree
operator|+=
name|length
expr_stmt|;
block|}
comment|/**    * Set a field to the concatenation of two string values. Result data is copied    * into the internal buffer.    *    * @param elementNum index within column vector to set    * @param leftSourceBuf container of left argument    * @param leftStart start of left argument    * @param leftLen length of left argument    * @param rightSourceBuf container of right argument    * @param rightStart start of right argument    * @param rightLen length of right arugment    */
specifier|public
name|void
name|setConcat
parameter_list|(
name|int
name|elementNum
parameter_list|,
name|byte
index|[]
name|leftSourceBuf
parameter_list|,
name|int
name|leftStart
parameter_list|,
name|int
name|leftLen
parameter_list|,
name|byte
index|[]
name|rightSourceBuf
parameter_list|,
name|int
name|rightStart
parameter_list|,
name|int
name|rightLen
parameter_list|)
block|{
name|int
name|newLen
init|=
name|leftLen
operator|+
name|rightLen
decl_stmt|;
if|if
condition|(
operator|(
name|nextFree
operator|+
name|newLen
operator|)
operator|>
name|buffer
operator|.
name|length
condition|)
block|{
name|increaseBufferSpace
argument_list|(
name|newLen
argument_list|)
expr_stmt|;
block|}
name|vector
index|[
name|elementNum
index|]
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|start
index|[
name|elementNum
index|]
operator|=
name|nextFree
expr_stmt|;
name|this
operator|.
name|length
index|[
name|elementNum
index|]
operator|=
name|newLen
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|leftSourceBuf
argument_list|,
name|leftStart
argument_list|,
name|buffer
argument_list|,
name|nextFree
argument_list|,
name|leftLen
argument_list|)
expr_stmt|;
name|nextFree
operator|+=
name|leftLen
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|rightSourceBuf
argument_list|,
name|rightStart
argument_list|,
name|buffer
argument_list|,
name|nextFree
argument_list|,
name|rightLen
argument_list|)
expr_stmt|;
name|nextFree
operator|+=
name|rightLen
expr_stmt|;
block|}
comment|/**    * Increase buffer space enough to accommodate next element.    * This uses an exponential increase mechanism to rapidly    * increase buffer size to enough to hold all data.    * As batches get re-loaded, buffer space allocated will quickly    * stabilize.    *    * @param nextElemLength size of next element to be added    */
specifier|public
name|void
name|increaseBufferSpace
parameter_list|(
name|int
name|nextElemLength
parameter_list|)
block|{
comment|// Keep doubling buffer size until there will be enough space for next element.
name|int
name|newLength
init|=
literal|2
operator|*
name|buffer
operator|.
name|length
decl_stmt|;
while|while
condition|(
operator|(
name|nextFree
operator|+
name|nextElemLength
operator|)
operator|>
name|newLength
condition|)
block|{
name|newLength
operator|*=
literal|2
expr_stmt|;
block|}
comment|// Allocate new buffer, copy data to it, and set buffer to new buffer.
name|byte
index|[]
name|newBuffer
init|=
operator|new
name|byte
index|[
name|newLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|newBuffer
argument_list|,
literal|0
argument_list|,
name|nextFree
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|newBuffer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getWritableObject
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|isRepeating
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
block|}
name|Writable
name|result
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|isNull
index|[
name|index
index|]
operator|&&
name|vector
index|[
name|index
index|]
operator|!=
literal|null
condition|)
block|{
name|textObject
operator|.
name|clear
argument_list|()
expr_stmt|;
name|textObject
operator|.
name|append
argument_list|(
name|vector
index|[
name|index
index|]
argument_list|,
name|start
index|[
name|index
index|]
argument_list|,
name|length
index|[
name|index
index|]
argument_list|)
expr_stmt|;
name|result
operator|=
name|textObject
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|NullWritable
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/** Copy the current object contents into the output. Only copy selected entries,     * as indicated by selectedInUse and the sel array.     */
specifier|public
name|void
name|copySelected
parameter_list|(
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|,
name|BytesColumnVector
name|output
parameter_list|)
block|{
comment|// Output has nulls if and only if input has nulls.
name|output
operator|.
name|noNulls
operator|=
name|noNulls
expr_stmt|;
name|output
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// Handle repeating case
if|if
condition|(
name|isRepeating
condition|)
block|{
name|output
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
name|isNull
index|[
literal|0
index|]
expr_stmt|;
name|output
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// Handle normal case
comment|// Copy data values over
if|if
condition|(
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|output
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|output
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Copy nulls over if needed
if|if
condition|(
operator|!
name|noNulls
condition|)
block|{
if|if
condition|(
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|isNull
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** Simplify vector by brute-force flattening noNulls and isRepeating     * This can be used to reduce combinatorial explosion of code paths in VectorExpressions     * with many arguments, at the expense of loss of some performance.     */
specifier|public
name|void
name|flatten
parameter_list|(
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|flattenPush
argument_list|()
expr_stmt|;
if|if
condition|(
name|isRepeating
condition|)
block|{
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// setRef is used below and this is safe, because the reference
comment|// is to data owned by this column vector. If this column vector
comment|// gets re-used, the whole thing is re-used together so there
comment|// is no danger of a dangling reference.
comment|// Only copy data values if entry is not null. The string value
comment|// at position 0 is undefined if the position 0 value is null.
if|if
condition|(
name|noNulls
operator|||
operator|(
operator|!
name|noNulls
operator|&&
operator|!
name|isNull
index|[
literal|0
index|]
operator|)
condition|)
block|{
comment|// loops start at position 1 because position 0 is already set
if|if
condition|(
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|this
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|flattenRepeatingNulls
argument_list|(
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|flattenNoNulls
argument_list|(
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
comment|// Fill the all the vector entries with provided value
specifier|public
name|void
name|fill
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|noNulls
operator|=
literal|true
expr_stmt|;
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|setRef
argument_list|(
literal|0
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setElement
parameter_list|(
name|int
name|outElementNum
parameter_list|,
name|int
name|inputElementNum
parameter_list|,
name|ColumnVector
name|inputVector
parameter_list|)
block|{
name|BytesColumnVector
name|in
init|=
operator|(
name|BytesColumnVector
operator|)
name|inputVector
decl_stmt|;
name|setVal
argument_list|(
name|outElementNum
argument_list|,
name|in
operator|.
name|vector
index|[
name|inputElementNum
index|]
argument_list|,
name|in
operator|.
name|start
index|[
name|inputElementNum
index|]
argument_list|,
name|in
operator|.
name|length
index|[
name|inputElementNum
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|initBuffer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stringifyValue
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|row
parameter_list|)
block|{
if|if
condition|(
name|isRepeating
condition|)
block|{
name|row
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|noNulls
operator|||
operator|!
name|isNull
index|[
name|row
index|]
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
operator|new
name|String
argument_list|(
name|this
operator|.
name|buffer
argument_list|,
name|start
index|[
name|row
index|]
argument_list|,
name|length
index|[
name|row
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

