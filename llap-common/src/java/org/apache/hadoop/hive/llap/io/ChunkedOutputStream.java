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
name|llap
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|io
operator|.
name|OutputStream
import|;
end_import

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

begin_comment
comment|// Writes data out as a series of chunks in the form<chunk size><chunk bytes><chunk size><chunk bytes>
end_comment

begin_comment
comment|// Closing the output stream will send a final 0-length chunk which will indicate end of input.
end_comment

begin_class
specifier|public
class|class
name|ChunkedOutputStream
extends|extends
name|OutputStream
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ChunkedOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|DataOutputStream
name|dout
decl_stmt|;
specifier|private
name|byte
index|[]
name|singleByte
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
specifier|private
name|byte
index|[]
name|buffer
decl_stmt|;
specifier|private
name|int
name|bufPos
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|id
decl_stmt|;
specifier|public
name|ChunkedOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|bufSize
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating chunked input stream: {}"
argument_list|,
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|bufSize
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Positive bufSize required, was "
operator|+
name|bufSize
argument_list|)
throw|;
block|}
name|buffer
operator|=
operator|new
name|byte
index|[
name|bufSize
index|]
expr_stmt|;
name|dout
operator|=
operator|new
name|DataOutputStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|singleByte
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
name|write
argument_list|(
name|singleByte
argument_list|,
literal|0
argument_list|,
literal|1
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
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|bytesWritten
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bytesWritten
operator|<
name|len
condition|)
block|{
comment|// Copy the data to the buffer
name|int
name|bytesToWrite
init|=
name|Math
operator|.
name|min
argument_list|(
name|len
operator|-
name|bytesWritten
argument_list|,
name|buffer
operator|.
name|length
operator|-
name|bufPos
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|off
operator|+
name|bytesWritten
argument_list|,
name|buffer
argument_list|,
name|bufPos
argument_list|,
name|bytesToWrite
argument_list|)
expr_stmt|;
name|bytesWritten
operator|+=
name|bytesToWrite
expr_stmt|;
name|bufPos
operator|+=
name|bytesToWrite
expr_stmt|;
comment|// If we've filled the buffer, write it out
if|if
condition|(
name|bufPos
operator|==
name|buffer
operator|.
name|length
condition|)
block|{
name|writeChunk
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|flush
argument_list|()
expr_stmt|;
comment|// Write final 0-length chunk
name|writeChunk
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"{}: Closing underlying output stream."
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|dout
operator|.
name|close
argument_list|()
expr_stmt|;
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
comment|// Write any remaining bytes to the out stream.
if|if
condition|(
name|bufPos
operator|>
literal|0
condition|)
block|{
name|writeChunk
argument_list|()
expr_stmt|;
name|dout
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeChunk
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{}: Writing chunk of size {}"
argument_list|,
name|id
argument_list|,
name|bufPos
argument_list|)
expr_stmt|;
block|}
comment|// First write chunk length
name|dout
operator|.
name|writeInt
argument_list|(
name|bufPos
argument_list|)
expr_stmt|;
comment|// Then write chunk bytes
name|dout
operator|.
name|write
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|bufPos
argument_list|)
expr_stmt|;
name|bufPos
operator|=
literal|0
expr_stmt|;
comment|// reset buffer
block|}
block|}
end_class

end_unit

