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
name|io
operator|.
name|orc
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
name|io
operator|.
name|InputStream
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

begin_class
specifier|abstract
class|class
name|InStream
extends|extends
name|InputStream
block|{
specifier|private
specifier|static
class|class
name|UncompressedStream
extends|extends
name|InStream
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|ByteBuffer
index|[]
name|bytes
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|offsets
decl_stmt|;
specifier|private
specifier|final
name|long
name|length
decl_stmt|;
specifier|private
name|long
name|currentOffset
decl_stmt|;
specifier|private
name|ByteBuffer
name|range
decl_stmt|;
specifier|private
name|int
name|currentRange
decl_stmt|;
specifier|public
name|UncompressedStream
parameter_list|(
name|String
name|name
parameter_list|,
name|ByteBuffer
index|[]
name|input
parameter_list|,
name|long
index|[]
name|offsets
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|bytes
operator|=
name|input
expr_stmt|;
name|this
operator|.
name|offsets
operator|=
name|offsets
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|currentRange
operator|=
literal|0
expr_stmt|;
name|currentOffset
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
block|{
if|if
condition|(
name|range
operator|==
literal|null
operator|||
name|range
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|currentOffset
operator|==
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|seek
argument_list|(
name|currentOffset
argument_list|)
expr_stmt|;
block|}
name|currentOffset
operator|+=
literal|1
expr_stmt|;
return|return
literal|0xff
operator|&
name|range
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|range
operator|==
literal|null
operator|||
name|range
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|currentOffset
operator|==
name|this
operator|.
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|seek
argument_list|(
name|currentOffset
argument_list|)
expr_stmt|;
block|}
name|int
name|actualLength
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
argument_list|,
name|range
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|range
operator|.
name|get
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|actualLength
argument_list|)
expr_stmt|;
name|currentOffset
operator|+=
name|actualLength
expr_stmt|;
return|return
name|actualLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
block|{
if|if
condition|(
name|range
operator|!=
literal|null
operator|&&
name|range
operator|.
name|remaining
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|range
operator|.
name|remaining
argument_list|()
return|;
block|}
return|return
call|(
name|int
call|)
argument_list|(
name|length
operator|-
name|currentOffset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|currentRange
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
name|currentOffset
operator|=
name|length
expr_stmt|;
comment|// explicit de-ref of bytes[]
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|bytes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|bytes
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|seek
argument_list|(
name|index
operator|.
name|getNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|seek
parameter_list|(
name|long
name|desired
parameter_list|)
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
name|bytes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|offsets
index|[
name|i
index|]
operator|<=
name|desired
operator|&&
name|desired
operator|-
name|offsets
index|[
name|i
index|]
operator|<
name|bytes
index|[
name|i
index|]
operator|.
name|remaining
argument_list|()
condition|)
block|{
name|currentOffset
operator|=
name|desired
expr_stmt|;
name|currentRange
operator|=
name|i
expr_stmt|;
name|this
operator|.
name|range
operator|=
name|bytes
index|[
name|i
index|]
operator|.
name|duplicate
argument_list|()
expr_stmt|;
name|int
name|pos
init|=
name|range
operator|.
name|position
argument_list|()
decl_stmt|;
name|pos
operator|+=
call|(
name|int
call|)
argument_list|(
name|desired
operator|-
name|offsets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
comment|// this is why we duplicate
name|this
operator|.
name|range
operator|.
name|position
argument_list|(
name|pos
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Seek in "
operator|+
name|name
operator|+
literal|" to "
operator|+
name|desired
operator|+
literal|" is outside of the data"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"uncompressed stream "
operator|+
name|name
operator|+
literal|" position: "
operator|+
name|currentOffset
operator|+
literal|" length: "
operator|+
name|length
operator|+
literal|" range: "
operator|+
name|currentRange
operator|+
literal|" offset: "
operator|+
operator|(
name|range
operator|==
literal|null
condition|?
literal|0
else|:
name|range
operator|.
name|position
argument_list|()
operator|)
operator|+
literal|" limit: "
operator|+
operator|(
name|range
operator|==
literal|null
condition|?
literal|0
else|:
name|range
operator|.
name|limit
argument_list|()
operator|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|CompressedStream
extends|extends
name|InStream
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|ByteBuffer
index|[]
name|bytes
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|offsets
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|length
decl_stmt|;
specifier|private
name|ByteBuffer
name|uncompressed
decl_stmt|;
specifier|private
specifier|final
name|CompressionCodec
name|codec
decl_stmt|;
specifier|private
name|ByteBuffer
name|compressed
decl_stmt|;
specifier|private
name|long
name|currentOffset
decl_stmt|;
specifier|private
name|int
name|currentRange
decl_stmt|;
specifier|private
name|boolean
name|isUncompressedOriginal
decl_stmt|;
specifier|private
name|boolean
name|isDirect
init|=
literal|false
decl_stmt|;
specifier|public
name|CompressedStream
parameter_list|(
name|String
name|name
parameter_list|,
name|ByteBuffer
index|[]
name|input
parameter_list|,
name|long
index|[]
name|offsets
parameter_list|,
name|long
name|length
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|int
name|bufferSize
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|input
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|codec
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|isDirect
operator|=
name|this
operator|.
name|bytes
index|[
literal|0
index|]
operator|.
name|isDirect
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|offsets
operator|=
name|offsets
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|currentOffset
operator|=
literal|0
expr_stmt|;
name|currentRange
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|ByteBuffer
name|allocateBuffer
parameter_list|(
name|int
name|size
parameter_list|)
block|{
comment|// TODO: use the same pool as the ORC readers
if|if
condition|(
name|isDirect
operator|==
literal|true
condition|)
block|{
return|return
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|size
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
argument_list|)
return|;
block|}
block|}
specifier|private
name|void
name|readHeader
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|compressed
operator|==
literal|null
operator|||
name|compressed
operator|.
name|remaining
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|seek
argument_list|(
name|currentOffset
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compressed
operator|.
name|remaining
argument_list|()
operator|>
name|OutStream
operator|.
name|HEADER_SIZE
condition|)
block|{
name|int
name|b0
init|=
name|compressed
operator|.
name|get
argument_list|()
operator|&
literal|0xff
decl_stmt|;
name|int
name|b1
init|=
name|compressed
operator|.
name|get
argument_list|()
operator|&
literal|0xff
decl_stmt|;
name|int
name|b2
init|=
name|compressed
operator|.
name|get
argument_list|()
operator|&
literal|0xff
decl_stmt|;
name|boolean
name|isOriginal
init|=
operator|(
name|b0
operator|&
literal|0x01
operator|)
operator|==
literal|1
decl_stmt|;
name|int
name|chunkLength
init|=
operator|(
name|b2
operator|<<
literal|15
operator|)
operator||
operator|(
name|b1
operator|<<
literal|7
operator|)
operator||
operator|(
name|b0
operator|>>
literal|1
operator|)
decl_stmt|;
if|if
condition|(
name|chunkLength
operator|>
name|bufferSize
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Buffer size too small. size = "
operator|+
name|bufferSize
operator|+
literal|" needed = "
operator|+
name|chunkLength
argument_list|)
throw|;
block|}
comment|// read 3 bytes, which should be equal to OutStream.HEADER_SIZE always
assert|assert
name|OutStream
operator|.
name|HEADER_SIZE
operator|==
literal|3
operator|:
literal|"The Orc HEADER_SIZE must be the same in OutStream and InStream"
assert|;
name|currentOffset
operator|+=
name|OutStream
operator|.
name|HEADER_SIZE
expr_stmt|;
name|ByteBuffer
name|slice
init|=
name|this
operator|.
name|slice
argument_list|(
name|chunkLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|isOriginal
condition|)
block|{
name|uncompressed
operator|=
name|slice
expr_stmt|;
name|isUncompressedOriginal
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|isUncompressedOriginal
condition|)
block|{
name|uncompressed
operator|=
name|allocateBuffer
argument_list|(
name|bufferSize
argument_list|)
expr_stmt|;
name|isUncompressedOriginal
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|uncompressed
operator|==
literal|null
condition|)
block|{
name|uncompressed
operator|=
name|allocateBuffer
argument_list|(
name|bufferSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|uncompressed
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|codec
operator|.
name|decompress
argument_list|(
name|slice
argument_list|,
name|uncompressed
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can't read header at "
operator|+
name|this
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|uncompressed
operator|==
literal|null
operator|||
name|uncompressed
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|currentOffset
operator|==
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|readHeader
argument_list|()
expr_stmt|;
block|}
return|return
literal|0xff
operator|&
name|uncompressed
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|uncompressed
operator|==
literal|null
operator|||
name|uncompressed
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|currentOffset
operator|==
name|this
operator|.
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|readHeader
argument_list|()
expr_stmt|;
block|}
name|int
name|actualLength
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
argument_list|,
name|uncompressed
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|uncompressed
operator|.
name|get
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|actualLength
argument_list|)
expr_stmt|;
return|return
name|actualLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|uncompressed
operator|==
literal|null
operator|||
name|uncompressed
operator|.
name|remaining
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|currentOffset
operator|==
name|length
condition|)
block|{
return|return
literal|0
return|;
block|}
name|readHeader
argument_list|()
expr_stmt|;
block|}
return|return
name|uncompressed
operator|.
name|remaining
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|uncompressed
operator|=
literal|null
expr_stmt|;
name|compressed
operator|=
literal|null
expr_stmt|;
name|currentRange
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
name|currentOffset
operator|=
name|length
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
name|bytes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|bytes
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|seek
argument_list|(
name|index
operator|.
name|getNext
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|uncompressedBytes
init|=
name|index
operator|.
name|getNext
argument_list|()
decl_stmt|;
if|if
condition|(
name|uncompressedBytes
operator|!=
literal|0
condition|)
block|{
name|readHeader
argument_list|()
expr_stmt|;
name|uncompressed
operator|.
name|position
argument_list|(
name|uncompressed
operator|.
name|position
argument_list|()
operator|+
operator|(
name|int
operator|)
name|uncompressedBytes
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|uncompressed
operator|!=
literal|null
condition|)
block|{
comment|// mark the uncompressed buffer as done
name|uncompressed
operator|.
name|position
argument_list|(
name|uncompressed
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* slices a read only contigous buffer of chunkLength */
specifier|private
name|ByteBuffer
name|slice
parameter_list|(
name|int
name|chunkLength
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|len
init|=
name|chunkLength
decl_stmt|;
specifier|final
name|long
name|oldOffset
init|=
name|currentOffset
decl_stmt|;
name|ByteBuffer
name|slice
decl_stmt|;
if|if
condition|(
name|compressed
operator|.
name|remaining
argument_list|()
operator|>=
name|len
condition|)
block|{
name|slice
operator|=
name|compressed
operator|.
name|slice
argument_list|()
expr_stmt|;
comment|// simple case
name|slice
operator|.
name|limit
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|currentOffset
operator|+=
name|len
expr_stmt|;
name|compressed
operator|.
name|position
argument_list|(
name|compressed
operator|.
name|position
argument_list|()
operator|+
name|len
argument_list|)
expr_stmt|;
return|return
name|slice
return|;
block|}
elseif|else
if|if
condition|(
name|currentRange
operator|>=
operator|(
name|bytes
operator|.
name|length
operator|-
literal|1
operator|)
condition|)
block|{
comment|// nothing has been modified yet
throw|throw
operator|new
name|IOException
argument_list|(
literal|"EOF in "
operator|+
name|this
operator|+
literal|" while trying to read "
operator|+
name|chunkLength
operator|+
literal|" bytes"
argument_list|)
throw|;
block|}
comment|// we need to consolidate 2 or more buffers into 1
comment|// first clear out compressed buffers
name|ByteBuffer
name|copy
init|=
name|allocateBuffer
argument_list|(
name|chunkLength
argument_list|)
decl_stmt|;
name|currentOffset
operator|+=
name|compressed
operator|.
name|remaining
argument_list|()
expr_stmt|;
name|len
operator|-=
name|compressed
operator|.
name|remaining
argument_list|()
expr_stmt|;
name|copy
operator|.
name|put
argument_list|(
name|compressed
argument_list|)
expr_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
operator|&&
operator|(
operator|++
name|currentRange
operator|)
operator|<
name|bytes
operator|.
name|length
condition|)
block|{
name|compressed
operator|=
name|bytes
index|[
name|currentRange
index|]
operator|.
name|duplicate
argument_list|()
expr_stmt|;
if|if
condition|(
name|compressed
operator|.
name|remaining
argument_list|()
operator|>=
name|len
condition|)
block|{
name|slice
operator|=
name|compressed
operator|.
name|slice
argument_list|()
expr_stmt|;
name|slice
operator|.
name|limit
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|copy
operator|.
name|put
argument_list|(
name|slice
argument_list|)
expr_stmt|;
name|currentOffset
operator|+=
name|len
expr_stmt|;
name|compressed
operator|.
name|position
argument_list|(
name|compressed
operator|.
name|position
argument_list|()
operator|+
name|len
argument_list|)
expr_stmt|;
return|return
name|copy
return|;
block|}
name|currentOffset
operator|+=
name|compressed
operator|.
name|remaining
argument_list|()
expr_stmt|;
name|len
operator|-=
name|compressed
operator|.
name|remaining
argument_list|()
expr_stmt|;
name|copy
operator|.
name|put
argument_list|(
name|compressed
argument_list|)
expr_stmt|;
block|}
comment|// restore offsets for exception clarity
name|seek
argument_list|(
name|oldOffset
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"EOF in "
operator|+
name|this
operator|+
literal|" while trying to read "
operator|+
name|chunkLength
operator|+
literal|" bytes"
argument_list|)
throw|;
block|}
specifier|private
name|void
name|seek
parameter_list|(
name|long
name|desired
parameter_list|)
throws|throws
name|IOException
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
name|bytes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|offsets
index|[
name|i
index|]
operator|<=
name|desired
operator|&&
name|desired
operator|-
name|offsets
index|[
name|i
index|]
operator|<
name|bytes
index|[
name|i
index|]
operator|.
name|remaining
argument_list|()
condition|)
block|{
name|currentRange
operator|=
name|i
expr_stmt|;
name|compressed
operator|=
name|bytes
index|[
name|i
index|]
operator|.
name|duplicate
argument_list|()
expr_stmt|;
name|int
name|pos
init|=
name|compressed
operator|.
name|position
argument_list|()
decl_stmt|;
name|pos
operator|+=
call|(
name|int
call|)
argument_list|(
name|desired
operator|-
name|offsets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|compressed
operator|.
name|position
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|currentOffset
operator|=
name|desired
expr_stmt|;
return|return;
block|}
block|}
comment|// if they are seeking to the precise end, go ahead and let them go there
name|int
name|segments
init|=
name|bytes
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|segments
operator|!=
literal|0
operator|&&
name|desired
operator|==
name|offsets
index|[
name|segments
operator|-
literal|1
index|]
operator|+
name|bytes
index|[
name|segments
operator|-
literal|1
index|]
operator|.
name|remaining
argument_list|()
condition|)
block|{
name|currentRange
operator|=
name|segments
operator|-
literal|1
expr_stmt|;
name|compressed
operator|=
name|bytes
index|[
name|currentRange
index|]
operator|.
name|duplicate
argument_list|()
expr_stmt|;
name|compressed
operator|.
name|position
argument_list|(
name|compressed
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|currentOffset
operator|=
name|desired
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Seek outside of data in "
operator|+
name|this
operator|+
literal|" to "
operator|+
name|desired
argument_list|)
throw|;
block|}
specifier|private
name|String
name|rangeString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
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
name|offsets
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|" range "
operator|+
name|i
operator|+
literal|" = "
operator|+
name|offsets
index|[
name|i
index|]
operator|+
literal|" to "
operator|+
name|bytes
index|[
name|i
index|]
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"compressed stream "
operator|+
name|name
operator|+
literal|" position: "
operator|+
name|currentOffset
operator|+
literal|" length: "
operator|+
name|length
operator|+
literal|" range: "
operator|+
name|currentRange
operator|+
literal|" offset: "
operator|+
operator|(
name|compressed
operator|==
literal|null
condition|?
literal|0
else|:
name|compressed
operator|.
name|position
argument_list|()
operator|)
operator|+
literal|" limit: "
operator|+
operator|(
name|compressed
operator|==
literal|null
condition|?
literal|0
else|:
name|compressed
operator|.
name|limit
argument_list|()
operator|)
operator|+
name|rangeString
argument_list|()
operator|+
operator|(
name|uncompressed
operator|==
literal|null
condition|?
literal|""
else|:
literal|" uncompressed: "
operator|+
name|uncompressed
operator|.
name|position
argument_list|()
operator|+
literal|" to "
operator|+
name|uncompressed
operator|.
name|limit
argument_list|()
operator|)
return|;
block|}
block|}
specifier|public
specifier|abstract
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create an input stream from a list of buffers.    * @param name the name of the stream    * @param input the list of ranges of bytes for the stream    * @param offsets a list of offsets (the same length as input) that must    *                contain the first offset of the each set of bytes in input    * @param length the length in bytes of the stream    * @param codec the compression codec    * @param bufferSize the compression buffer size    * @return an input stream    * @throws IOException    */
specifier|public
specifier|static
name|InStream
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|ByteBuffer
index|[]
name|input
parameter_list|,
name|long
index|[]
name|offsets
parameter_list|,
name|long
name|length
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|UncompressedStream
argument_list|(
name|name
argument_list|,
name|input
argument_list|,
name|offsets
argument_list|,
name|length
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|CompressedStream
argument_list|(
name|name
argument_list|,
name|input
argument_list|,
name|offsets
argument_list|,
name|length
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

