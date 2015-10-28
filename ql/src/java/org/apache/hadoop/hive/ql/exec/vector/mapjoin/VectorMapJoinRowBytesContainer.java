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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|fs
operator|.
name|FileUtil
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
name|Output
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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

begin_comment
comment|/**  * An eager bytes container that puts row bytes to an output stream.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinRowBytesContainer
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorMapJoinRowBytesContainer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|File
name|parentFile
decl_stmt|;
specifier|private
name|File
name|tmpFile
decl_stmt|;
comment|// We buffer in a org.apache.hadoop.hive.serde2.ByteStream.Output since that is what
comment|// is used by VectorSerializeRow / SerializeWrite.  Periodically, we flush this buffer
comment|// to disk.
specifier|private
name|Output
name|output
decl_stmt|;
specifier|private
name|int
name|rowBeginPos
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|OUTPUT_SIZE
init|=
literal|4096
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|THRESHOLD
init|=
literal|8
operator|*
operator|(
name|OUTPUT_SIZE
operator|/
literal|10
operator|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INPUT_SIZE
init|=
literal|4096
decl_stmt|;
specifier|private
name|FileOutputStream
name|fileOutputStream
decl_stmt|;
specifier|private
name|boolean
name|isOpen
decl_stmt|;
specifier|private
name|byte
index|[]
name|readBuffer
decl_stmt|;
specifier|private
name|byte
index|[]
name|largeRowBuffer
decl_stmt|;
specifier|private
name|int
name|readOffset
decl_stmt|;
specifier|private
name|int
name|readLength
decl_stmt|;
specifier|private
name|int
name|readNextCount
decl_stmt|;
specifier|private
name|int
name|readNextIndex
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_READS
init|=
literal|256
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|readNextBytes
decl_stmt|;
specifier|private
name|int
name|readNextOffsets
index|[]
decl_stmt|;
specifier|private
name|int
name|readNextLengths
index|[]
decl_stmt|;
specifier|private
name|byte
index|[]
name|currentBytes
decl_stmt|;
specifier|private
name|int
name|currentOffset
decl_stmt|;
specifier|private
name|int
name|currentLength
decl_stmt|;
specifier|private
name|long
name|totalWriteLength
decl_stmt|;
specifier|private
name|long
name|totalReadLength
decl_stmt|;
specifier|private
name|FileInputStream
name|fileInputStream
decl_stmt|;
specifier|public
name|VectorMapJoinRowBytesContainer
parameter_list|()
block|{
name|output
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|readBuffer
operator|=
operator|new
name|byte
index|[
name|INPUT_SIZE
index|]
expr_stmt|;
name|readNextBytes
operator|=
operator|new
name|byte
index|[
name|MAX_READS
index|]
index|[]
expr_stmt|;
name|readNextOffsets
operator|=
operator|new
name|int
index|[
name|MAX_READS
index|]
expr_stmt|;
name|readNextLengths
operator|=
operator|new
name|int
index|[
name|MAX_READS
index|]
expr_stmt|;
name|isOpen
operator|=
literal|false
expr_stmt|;
name|totalWriteLength
operator|=
literal|0
expr_stmt|;
name|totalReadLength
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|void
name|setupOutputFileStreams
parameter_list|()
throws|throws
name|IOException
block|{
name|parentFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"bytes-container"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentFile
operator|.
name|delete
argument_list|()
operator|&&
name|parentFile
operator|.
name|mkdir
argument_list|()
condition|)
block|{
name|parentFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
name|tmpFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"BytesContainer"
argument_list|,
literal|".tmp"
argument_list|,
name|parentFile
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"BytesContainer created temp file "
operator|+
name|tmpFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|fileOutputStream
operator|=
operator|new
name|FileOutputStream
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initFile
parameter_list|()
block|{
try|try
block|{
name|setupOutputFileStreams
argument_list|()
expr_stmt|;
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
literal|"Failed to create temporary output file on disk"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Output
name|getOuputForRowBytes
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isOpen
condition|)
block|{
name|initFile
argument_list|()
expr_stmt|;
name|isOpen
operator|=
literal|true
expr_stmt|;
block|}
comment|// Reserve space for the int length.
name|output
operator|.
name|reserve
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|rowBeginPos
operator|=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
return|return
name|output
return|;
block|}
specifier|public
name|void
name|finishRow
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|output
operator|.
name|getLength
argument_list|()
operator|-
name|rowBeginPos
decl_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|rowBeginPos
operator|-
literal|4
argument_list|,
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|output
operator|.
name|getLength
argument_list|()
operator|>
name|THRESHOLD
condition|)
block|{
name|fileOutputStream
operator|.
name|write
argument_list|(
name|output
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|totalWriteLength
operator|+=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|prepareForReading
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isOpen
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|output
operator|.
name|getLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|fileOutputStream
operator|.
name|write
argument_list|(
name|output
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|totalWriteLength
operator|+=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|fileOutputStream
operator|.
name|flush
argument_list|()
expr_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fileInputStream
operator|!=
literal|null
condition|)
block|{
name|fileInputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|fileInputStream
operator|=
operator|new
name|FileInputStream
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
name|readNextIndex
operator|=
literal|0
expr_stmt|;
name|readNextCount
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|int
name|readInt
parameter_list|()
block|{
name|int
name|value
init|=
operator|(
operator|(
operator|(
name|readBuffer
index|[
name|readOffset
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
name|readBuffer
index|[
name|readOffset
operator|+
literal|1
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
name|readBuffer
index|[
name|readOffset
operator|+
literal|2
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
name|readBuffer
index|[
name|readOffset
operator|+
literal|3
index|]
operator|&
literal|0xFF
operator|)
operator|)
operator|)
decl_stmt|;
name|readOffset
operator|+=
literal|4
expr_stmt|;
return|return
name|value
return|;
block|}
comment|// Call when nextReadIndex == nextReadCount.
specifier|private
name|void
name|bufferedRead
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Reset for reading.
name|readNextIndex
operator|=
literal|0
expr_stmt|;
comment|// Reset for filling.
name|readNextCount
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|readOffset
operator|<
name|readLength
condition|)
block|{
comment|// Move unprocessed remainder to beginning of buffer.
name|int
name|unprocessLength
init|=
name|readLength
operator|-
name|readOffset
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|readBuffer
argument_list|,
name|readOffset
argument_list|,
name|readBuffer
argument_list|,
literal|0
argument_list|,
name|unprocessLength
argument_list|)
expr_stmt|;
name|int
name|maxReadLength
init|=
name|readBuffer
operator|.
name|length
operator|-
name|unprocessLength
decl_stmt|;
name|int
name|partialReadLength
init|=
name|fileInputStream
operator|.
name|read
argument_list|(
name|readBuffer
argument_list|,
name|unprocessLength
argument_list|,
name|maxReadLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|partialReadLength
operator|==
operator|-
literal|1
condition|)
block|{
name|partialReadLength
operator|=
literal|0
expr_stmt|;
block|}
name|totalReadLength
operator|+=
name|partialReadLength
expr_stmt|;
name|readLength
operator|=
name|unprocessLength
operator|+
name|partialReadLength
expr_stmt|;
name|readOffset
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|readOffset
operator|=
literal|0
expr_stmt|;
name|readLength
operator|=
name|fileInputStream
operator|.
name|read
argument_list|(
name|readBuffer
argument_list|,
literal|0
argument_list|,
name|readBuffer
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|readLength
operator|==
operator|-
literal|1
condition|)
block|{
name|readLength
operator|=
literal|0
expr_stmt|;
block|}
name|totalReadLength
operator|+=
name|readLength
expr_stmt|;
block|}
if|if
condition|(
name|readLength
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|readLength
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Negative read length"
argument_list|)
throw|;
block|}
comment|// Get length word.
if|if
condition|(
name|readLength
operator|<
literal|4
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting 4 byte length"
argument_list|)
throw|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
comment|// Use Input class to read length.
name|int
name|saveReadOffset
init|=
name|readOffset
decl_stmt|;
name|int
name|rowLength
init|=
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|rowLength
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Negative row length"
argument_list|)
throw|;
block|}
name|int
name|remainingLength
init|=
name|readLength
operator|-
name|readOffset
decl_stmt|;
if|if
condition|(
name|remainingLength
operator|<
name|rowLength
condition|)
block|{
if|if
condition|(
name|readNextCount
operator|>
literal|0
condition|)
block|{
comment|// Leave this one for the next round.
name|readOffset
operator|=
name|saveReadOffset
expr_stmt|;
break|break;
block|}
comment|// Buffer needed to bridge.
if|if
condition|(
name|largeRowBuffer
operator|==
literal|null
operator|||
name|largeRowBuffer
operator|.
name|length
operator|<
name|rowLength
condition|)
block|{
name|int
name|newLargeBufferLength
init|=
name|Math
operator|.
name|max
argument_list|(
name|Integer
operator|.
name|highestOneBit
argument_list|(
name|rowLength
argument_list|)
operator|<<
literal|1
argument_list|,
name|INPUT_SIZE
argument_list|)
decl_stmt|;
name|largeRowBuffer
operator|=
operator|new
name|byte
index|[
name|newLargeBufferLength
index|]
expr_stmt|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|readBuffer
argument_list|,
name|readOffset
argument_list|,
name|largeRowBuffer
argument_list|,
literal|0
argument_list|,
name|remainingLength
argument_list|)
expr_stmt|;
name|int
name|expectedPartialLength
init|=
name|rowLength
operator|-
name|remainingLength
decl_stmt|;
name|int
name|partialReadLength
init|=
name|fileInputStream
operator|.
name|read
argument_list|(
name|largeRowBuffer
argument_list|,
name|remainingLength
argument_list|,
name|expectedPartialLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|partialReadLength
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected EOF (total write length "
operator|+
name|totalWriteLength
operator|+
literal|", total read length "
operator|+
name|totalReadLength
operator|+
literal|", read length "
operator|+
name|expectedPartialLength
operator|+
literal|")"
argument_list|)
throw|;
block|}
if|if
condition|(
name|expectedPartialLength
operator|!=
name|partialReadLength
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to read a complete row of length "
operator|+
name|rowLength
operator|+
literal|" (total write length "
operator|+
name|totalWriteLength
operator|+
literal|", total read length "
operator|+
name|totalReadLength
operator|+
literal|", read length "
operator|+
name|expectedPartialLength
operator|+
literal|", actual length "
operator|+
name|partialReadLength
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|totalReadLength
operator|+=
name|partialReadLength
expr_stmt|;
name|readNextBytes
index|[
name|readNextCount
index|]
operator|=
name|largeRowBuffer
expr_stmt|;
name|readNextOffsets
index|[
name|readNextCount
index|]
operator|=
literal|0
expr_stmt|;
name|readNextLengths
index|[
name|readNextCount
index|]
operator|=
name|rowLength
expr_stmt|;
comment|// Indicate we used the last row's bytes for large buffer.
name|readOffset
operator|=
name|readLength
expr_stmt|;
name|readNextCount
operator|++
expr_stmt|;
break|break;
block|}
name|readNextBytes
index|[
name|readNextCount
index|]
operator|=
name|readBuffer
expr_stmt|;
name|readNextOffsets
index|[
name|readNextCount
index|]
operator|=
name|readOffset
expr_stmt|;
name|readNextLengths
index|[
name|readNextCount
index|]
operator|=
name|rowLength
expr_stmt|;
name|readOffset
operator|+=
name|rowLength
expr_stmt|;
name|readNextCount
operator|++
expr_stmt|;
if|if
condition|(
name|readNextCount
operator|>=
name|readNextBytes
operator|.
name|length
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|readLength
operator|-
name|readOffset
operator|<
literal|4
condition|)
block|{
comment|// Handle in next round.
break|break;
block|}
block|}
block|}
specifier|public
name|boolean
name|readNext
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isOpen
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|readNextIndex
operator|>=
name|readNextCount
condition|)
block|{
name|bufferedRead
argument_list|()
expr_stmt|;
comment|// Any more left?
if|if
condition|(
name|readNextIndex
operator|>=
name|readNextCount
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
name|currentBytes
operator|=
name|readNextBytes
index|[
name|readNextIndex
index|]
expr_stmt|;
name|currentOffset
operator|=
name|readNextOffsets
index|[
name|readNextIndex
index|]
expr_stmt|;
name|currentLength
operator|=
name|readNextLengths
index|[
name|readNextIndex
index|]
expr_stmt|;
name|readNextIndex
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|byte
index|[]
name|currentBytes
parameter_list|()
block|{
return|return
name|currentBytes
return|;
block|}
specifier|public
name|int
name|currentOffset
parameter_list|()
block|{
return|return
name|currentOffset
return|;
block|}
specifier|public
name|int
name|currentLength
parameter_list|()
block|{
return|return
name|currentLength
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
if|if
condition|(
name|fileInputStream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|fileInputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{       }
name|fileInputStream
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|fileOutputStream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|fileOutputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{       }
name|fileOutputStream
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|parentFile
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|parentFile
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{       }
block|}
name|parentFile
operator|=
literal|null
expr_stmt|;
name|tmpFile
operator|=
literal|null
expr_stmt|;
name|isOpen
operator|=
literal|false
expr_stmt|;
name|totalWriteLength
operator|=
literal|0
expr_stmt|;
block|}
block|}
end_class

end_unit

