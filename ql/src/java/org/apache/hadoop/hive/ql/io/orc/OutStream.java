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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_class
class|class
name|OutStream
extends|extends
name|PositionedOutputStream
block|{
interface|interface
name|OutputReceiver
block|{
comment|/**      * Output the given buffer to the final destination      * @param buffer the buffer to output      * @throws IOException      */
name|void
name|output
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|static
specifier|final
name|int
name|HEADER_SIZE
init|=
literal|3
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|OutputReceiver
name|receiver
decl_stmt|;
comment|/**    * Stores the uncompressed bytes that have been serialized, but not    * compressed yet. When this fills, we compress the entire buffer.    */
specifier|private
name|ByteBuffer
name|current
init|=
literal|null
decl_stmt|;
comment|/**    * Stores the compressed bytes until we have a full buffer and then outputs    * them to the receiver. If no compression is being done, this (and overflow)    * will always be null and the current buffer will be sent directly to the    * receiver.    */
specifier|private
name|ByteBuffer
name|compressed
init|=
literal|null
decl_stmt|;
comment|/**    * Since the compressed buffer may start with contents from previous    * compression blocks, we allocate an overflow buffer so that the    * output of the codec can be split between the two buffers. After the    * compressed buffer is sent to the receiver, the overflow buffer becomes    * the new compressed buffer.    */
specifier|private
name|ByteBuffer
name|overflow
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
specifier|final
name|CompressionCodec
name|codec
decl_stmt|;
specifier|private
name|long
name|compressedBytes
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|uncompressedBytes
init|=
literal|0
decl_stmt|;
name|OutStream
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|OutputReceiver
name|receiver
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|codec
expr_stmt|;
name|this
operator|.
name|receiver
operator|=
name|receiver
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
throws|throws
name|IOException
block|{
name|uncompressedBytes
operator|=
literal|0
expr_stmt|;
name|compressedBytes
operator|=
literal|0
expr_stmt|;
name|compressed
operator|=
literal|null
expr_stmt|;
name|overflow
operator|=
literal|null
expr_stmt|;
name|current
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Write the length of the compressed bytes. Life is much easier if the    * header is constant length, so just use 3 bytes. Considering most of the    * codecs want between 32k (snappy) and 256k (lzo, zlib), 3 bytes should    * be plenty. We also use the low bit for whether it is the original or    * compressed bytes.    * @param buffer the buffer to write the header to    * @param position the position in the buffer to write at    * @param val the size in the file    * @param original is it uncompressed    */
specifier|private
specifier|static
name|void
name|writeHeader
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|int
name|position
parameter_list|,
name|int
name|val
parameter_list|,
name|boolean
name|original
parameter_list|)
block|{
name|buffer
operator|.
name|put
argument_list|(
name|position
argument_list|,
call|(
name|byte
call|)
argument_list|(
operator|(
name|val
operator|<<
literal|1
operator|)
operator|+
operator|(
name|original
condition|?
literal|1
else|:
literal|0
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|put
argument_list|(
name|position
operator|+
literal|1
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|val
operator|>>
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|put
argument_list|(
name|position
operator|+
literal|2
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|val
operator|>>
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getNewInputBuffer
parameter_list|()
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
name|current
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|bufferSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|bufferSize
operator|+
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|writeHeader
argument_list|(
name|current
argument_list|,
literal|0
argument_list|,
name|bufferSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|current
operator|.
name|position
argument_list|(
name|HEADER_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Allocate a new output buffer if we are compressing.    */
specifier|private
name|ByteBuffer
name|getNewOutputBuffer
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|bufferSize
operator|+
name|HEADER_SIZE
argument_list|)
return|;
block|}
specifier|private
name|void
name|flip
parameter_list|()
throws|throws
name|IOException
block|{
name|current
operator|.
name|limit
argument_list|(
name|current
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|current
operator|.
name|position
argument_list|(
name|codec
operator|==
literal|null
condition|?
literal|0
else|:
name|HEADER_SIZE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
name|getNewInputBuffer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|current
operator|.
name|remaining
argument_list|()
operator|<
literal|1
condition|)
block|{
name|spill
argument_list|()
expr_stmt|;
block|}
name|uncompressedBytes
operator|+=
literal|1
expr_stmt|;
name|current
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|bytes
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
name|current
operator|==
literal|null
condition|)
block|{
name|getNewInputBuffer
argument_list|()
expr_stmt|;
block|}
name|int
name|remaining
init|=
name|Math
operator|.
name|min
argument_list|(
name|current
operator|.
name|remaining
argument_list|()
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|current
operator|.
name|put
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|remaining
argument_list|)
expr_stmt|;
name|uncompressedBytes
operator|+=
name|remaining
expr_stmt|;
name|length
operator|-=
name|remaining
expr_stmt|;
while|while
condition|(
name|length
operator|!=
literal|0
condition|)
block|{
name|spill
argument_list|()
expr_stmt|;
name|offset
operator|+=
name|remaining
expr_stmt|;
name|remaining
operator|=
name|Math
operator|.
name|min
argument_list|(
name|current
operator|.
name|remaining
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|current
operator|.
name|put
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|remaining
argument_list|)
expr_stmt|;
name|uncompressedBytes
operator|+=
name|remaining
expr_stmt|;
name|length
operator|-=
name|remaining
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|spill
parameter_list|()
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
comment|// if there isn't anything in the current buffer, don't spill
if|if
condition|(
name|current
operator|==
literal|null
operator|||
name|current
operator|.
name|position
argument_list|()
operator|==
operator|(
name|codec
operator|==
literal|null
condition|?
literal|0
else|:
name|HEADER_SIZE
operator|)
condition|)
block|{
return|return;
block|}
name|flip
argument_list|()
expr_stmt|;
if|if
condition|(
name|codec
operator|==
literal|null
condition|)
block|{
name|receiver
operator|.
name|output
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|getNewInputBuffer
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|compressed
operator|==
literal|null
condition|)
block|{
name|compressed
operator|=
name|getNewOutputBuffer
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|overflow
operator|==
literal|null
condition|)
block|{
name|overflow
operator|=
name|getNewOutputBuffer
argument_list|()
expr_stmt|;
block|}
name|int
name|sizePosn
init|=
name|compressed
operator|.
name|position
argument_list|()
decl_stmt|;
name|compressed
operator|.
name|position
argument_list|(
name|compressed
operator|.
name|position
argument_list|()
operator|+
name|HEADER_SIZE
argument_list|)
expr_stmt|;
if|if
condition|(
name|codec
operator|.
name|compress
argument_list|(
name|current
argument_list|,
name|compressed
argument_list|,
name|overflow
argument_list|)
condition|)
block|{
name|uncompressedBytes
operator|=
literal|0
expr_stmt|;
comment|// move position back to after the header
name|current
operator|.
name|position
argument_list|(
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|current
operator|.
name|limit
argument_list|(
name|current
operator|.
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
comment|// find the total bytes in the chunk
name|int
name|totalBytes
init|=
name|compressed
operator|.
name|position
argument_list|()
operator|-
name|sizePosn
operator|-
name|HEADER_SIZE
decl_stmt|;
if|if
condition|(
name|overflow
operator|!=
literal|null
condition|)
block|{
name|totalBytes
operator|+=
name|overflow
operator|.
name|position
argument_list|()
expr_stmt|;
block|}
name|compressedBytes
operator|+=
name|totalBytes
operator|+
name|HEADER_SIZE
expr_stmt|;
name|writeHeader
argument_list|(
name|compressed
argument_list|,
name|sizePosn
argument_list|,
name|totalBytes
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// if we have less than the next header left, spill it.
if|if
condition|(
name|compressed
operator|.
name|remaining
argument_list|()
operator|<
name|HEADER_SIZE
condition|)
block|{
name|compressed
operator|.
name|flip
argument_list|()
expr_stmt|;
name|receiver
operator|.
name|output
argument_list|(
name|compressed
argument_list|)
expr_stmt|;
name|compressed
operator|=
name|overflow
expr_stmt|;
name|overflow
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|compressedBytes
operator|+=
name|uncompressedBytes
operator|+
name|HEADER_SIZE
expr_stmt|;
name|uncompressedBytes
operator|=
literal|0
expr_stmt|;
comment|// we are using the original, but need to spill the current
comment|// compressed buffer first. So back up to where we started,
comment|// flip it and add it to done.
if|if
condition|(
name|sizePosn
operator|!=
literal|0
condition|)
block|{
name|compressed
operator|.
name|position
argument_list|(
name|sizePosn
argument_list|)
expr_stmt|;
name|compressed
operator|.
name|flip
argument_list|()
expr_stmt|;
name|receiver
operator|.
name|output
argument_list|(
name|compressed
argument_list|)
expr_stmt|;
name|compressed
operator|=
literal|null
expr_stmt|;
comment|// if we have an overflow, clear it and make it the new compress
comment|// buffer
if|if
condition|(
name|overflow
operator|!=
literal|null
condition|)
block|{
name|overflow
operator|.
name|clear
argument_list|()
expr_stmt|;
name|compressed
operator|=
name|overflow
expr_stmt|;
name|overflow
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|compressed
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|overflow
operator|!=
literal|null
condition|)
block|{
name|overflow
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|// now add the current buffer into the done list and get a new one.
name|current
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// update the header with the current length
name|writeHeader
argument_list|(
name|current
argument_list|,
literal|0
argument_list|,
name|current
operator|.
name|limit
argument_list|()
operator|-
name|HEADER_SIZE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|receiver
operator|.
name|output
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|getNewInputBuffer
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|void
name|getPosition
parameter_list|(
name|PositionRecorder
name|recorder
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
name|recorder
operator|.
name|addPosition
argument_list|(
name|uncompressedBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|recorder
operator|.
name|addPosition
argument_list|(
name|compressedBytes
argument_list|)
expr_stmt|;
name|recorder
operator|.
name|addPosition
argument_list|(
name|uncompressedBytes
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|spill
argument_list|()
expr_stmt|;
if|if
condition|(
name|compressed
operator|!=
literal|null
operator|&&
name|compressed
operator|.
name|position
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|compressed
operator|.
name|flip
argument_list|()
expr_stmt|;
name|receiver
operator|.
name|output
argument_list|(
name|compressed
argument_list|)
expr_stmt|;
name|compressed
operator|=
literal|null
expr_stmt|;
block|}
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBufferSize
parameter_list|()
block|{
name|long
name|result
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|result
operator|+=
name|current
operator|.
name|capacity
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|compressed
operator|!=
literal|null
condition|)
block|{
name|result
operator|+=
name|compressed
operator|.
name|capacity
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|overflow
operator|!=
literal|null
condition|)
block|{
name|result
operator|+=
name|overflow
operator|.
name|capacity
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

