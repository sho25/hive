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
name|columnar
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|WritableComparator
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
name|WritableFactories
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
name|WritableFactory
import|;
end_import

begin_comment
comment|/**  *<tt>BytesRefWritable</tt> referenced a section of byte array. It can be used  * to avoid unnecessary byte copy.  */
end_comment

begin_class
specifier|public
class|class
name|BytesRefWritable
implements|implements
name|Writable
implements|,
name|Comparable
argument_list|<
name|BytesRefWritable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_BYTES
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|public
specifier|static
name|BytesRefWritable
name|ZeroBytesRefWritable
init|=
operator|new
name|BytesRefWritable
argument_list|()
decl_stmt|;
name|int
name|start
init|=
literal|0
decl_stmt|;
name|int
name|length
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
literal|null
decl_stmt|;
name|LazyDecompressionCallback
name|lazyDecompressObj
decl_stmt|;
comment|/**    * Create a zero-size bytes.    */
specifier|public
name|BytesRefWritable
parameter_list|()
block|{
name|this
argument_list|(
name|EMPTY_BYTES
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a BytesRefWritable with<tt>length</tt> bytes.    */
specifier|public
name|BytesRefWritable
parameter_list|(
name|int
name|length
parameter_list|)
block|{
assert|assert
name|length
operator|>
literal|0
assert|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|bytes
operator|=
operator|new
name|byte
index|[
name|this
operator|.
name|length
index|]
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Create a BytesRefWritable referenced to the given bytes.    */
specifier|public
name|BytesRefWritable
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|length
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Create a BytesRefWritable referenced to one section of the given bytes. The    * section is determined by argument<tt>offset</tt> and<tt>len</tt>.    */
specifier|public
name|BytesRefWritable
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|bytes
operator|=
name|data
expr_stmt|;
name|start
operator|=
name|offset
expr_stmt|;
name|length
operator|=
name|len
expr_stmt|;
block|}
comment|/**    * Create a BytesRefWritable referenced to one section of the given bytes. The    * argument<tt>lazyDecompressData</tt> refers to a LazyDecompressionCallback    * object. The arguments<tt>offset</tt> and<tt>len</tt> are referred to    * uncompressed bytes of<tt>lazyDecompressData</tt>. Use<tt>offset</tt> and    *<tt>len</tt> after uncompressing the data.    */
specifier|public
name|BytesRefWritable
parameter_list|(
name|LazyDecompressionCallback
name|lazyDecompressData
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|lazyDecompressObj
operator|=
name|lazyDecompressData
expr_stmt|;
name|start
operator|=
name|offset
expr_stmt|;
name|length
operator|=
name|len
expr_stmt|;
block|}
specifier|private
name|void
name|lazyDecompress
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|&&
name|lazyDecompressObj
operator|!=
literal|null
condition|)
block|{
name|bytes
operator|=
name|lazyDecompressObj
operator|.
name|decompress
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Returns a copy of the underlying bytes referenced by this instance.    *     * @return a new copied byte array    * @throws IOException    */
specifier|public
name|byte
index|[]
name|getBytesCopy
parameter_list|()
throws|throws
name|IOException
block|{
name|lazyDecompress
argument_list|()
expr_stmt|;
name|byte
index|[]
name|bb
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|bb
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|bb
return|;
block|}
comment|/**    * Returns the underlying bytes.    *     * @throws IOException    */
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
throws|throws
name|IOException
block|{
name|lazyDecompress
argument_list|()
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * readFields() will corrupt the array. So use the set method whenever    * possible.    *     * @see #readFields(DataInput)    */
specifier|public
name|void
name|set
parameter_list|(
name|byte
index|[]
name|newData
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|bytes
operator|=
name|newData
expr_stmt|;
name|start
operator|=
name|offset
expr_stmt|;
name|length
operator|=
name|len
expr_stmt|;
name|lazyDecompressObj
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * readFields() will corrupt the array. So use the set method whenever    * possible.    *     * @see #readFields(DataInput)    */
specifier|public
name|void
name|set
parameter_list|(
name|LazyDecompressionCallback
name|newData
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|bytes
operator|=
literal|null
expr_stmt|;
name|start
operator|=
name|offset
expr_stmt|;
name|length
operator|=
name|len
expr_stmt|;
name|lazyDecompressObj
operator|=
name|newData
expr_stmt|;
block|}
specifier|public
name|void
name|writeDataTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|lazyDecompress
argument_list|()
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Always reuse the bytes array if length of bytes array is equal or greater    * to the current record, otherwise create a new one. readFields will corrupt    * the array. Please use set() whenever possible.    *     * @see #set(byte[], int, int)    */
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|>
name|bytes
operator|.
name|length
condition|)
block|{
name|bytes
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
block|}
name|start
operator|=
literal|0
expr_stmt|;
name|length
operator|=
name|len
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|lazyDecompress
argument_list|()
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|super
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|(
literal|3
operator|*
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
name|start
init|;
name|idx
operator|<
name|length
condition|;
name|idx
operator|++
control|)
block|{
comment|// if not the first, put a blank separator in
if|if
condition|(
name|idx
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|String
name|num
init|=
name|Integer
operator|.
name|toHexString
argument_list|(
literal|0xff
operator|&
name|bytes
index|[
name|idx
index|]
argument_list|)
decl_stmt|;
comment|// if it is only one digit, add a leading 0.
if|if
condition|(
name|num
operator|.
name|length
argument_list|()
operator|<
literal|2
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|BytesRefWritable
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Argument can not be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|0
return|;
block|}
try|try
block|{
return|return
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|getLength
argument_list|()
argument_list|,
name|other
operator|.
name|getData
argument_list|()
argument_list|,
name|other
operator|.
name|start
argument_list|,
name|other
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|right_obj
parameter_list|)
block|{
if|if
condition|(
name|right_obj
operator|==
literal|null
operator|||
operator|!
operator|(
name|right_obj
operator|instanceof
name|BytesRefWritable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|compareTo
argument_list|(
operator|(
name|BytesRefWritable
operator|)
name|right_obj
argument_list|)
operator|==
literal|0
return|;
block|}
static|static
block|{
name|WritableFactories
operator|.
name|setFactory
argument_list|(
name|BytesRefWritable
operator|.
name|class
argument_list|,
operator|new
name|WritableFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Writable
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BytesRefWritable
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|length
return|;
block|}
specifier|public
name|int
name|getStart
parameter_list|()
block|{
return|return
name|start
return|;
block|}
block|}
end_class

end_unit

