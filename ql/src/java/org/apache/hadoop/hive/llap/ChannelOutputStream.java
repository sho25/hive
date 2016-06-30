begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBuf
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelFuture
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelFutureListener
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
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
comment|/**  * OutputStream to write to the Netty Channel  */
end_comment

begin_class
specifier|public
class|class
name|ChannelOutputStream
extends|extends
name|OutputStream
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
name|ChannelOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ChannelHandlerContext
name|chc
decl_stmt|;
specifier|private
name|int
name|bufSize
decl_stmt|;
specifier|private
name|String
name|id
decl_stmt|;
specifier|private
name|ByteBuf
name|buf
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
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Object
name|writeMonitor
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxPendingWrites
decl_stmt|;
specifier|private
specifier|volatile
name|int
name|pendingWrites
init|=
literal|0
decl_stmt|;
specifier|private
name|ChannelFutureListener
name|writeListener
init|=
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|future
parameter_list|)
block|{
name|pendingWrites
operator|--
expr_stmt|;
if|if
condition|(
name|future
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Write cancelled on ID "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Write error on ID "
operator|+
name|id
argument_list|,
name|future
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|writeMonitor
init|)
block|{
name|writeMonitor
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
specifier|private
name|ChannelFutureListener
name|closeListener
init|=
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|future
parameter_list|)
block|{
if|if
condition|(
name|future
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Close cancelled on ID "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|future
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Close failed on ID "
operator|+
name|id
argument_list|,
name|future
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
specifier|public
name|ChannelOutputStream
parameter_list|(
name|ChannelHandlerContext
name|chc
parameter_list|,
name|String
name|id
parameter_list|,
name|int
name|bufSize
parameter_list|,
name|int
name|maxOutstandingWrites
parameter_list|)
block|{
name|this
operator|.
name|chc
operator|=
name|chc
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|bufSize
operator|=
name|bufSize
expr_stmt|;
name|this
operator|.
name|buf
operator|=
name|chc
operator|.
name|alloc
argument_list|()
operator|.
name|buffer
argument_list|(
name|bufSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxPendingWrites
operator|=
name|maxOutstandingWrites
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
parameter_list|)
throws|throws
name|IOException
block|{
name|write
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
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
name|currentOffset
init|=
name|off
decl_stmt|;
name|int
name|bytesRemaining
init|=
name|len
decl_stmt|;
while|while
condition|(
name|bytesRemaining
operator|+
name|buf
operator|.
name|readableBytes
argument_list|()
operator|>
name|bufSize
condition|)
block|{
name|int
name|iterationLen
init|=
name|bufSize
operator|-
name|buf
operator|.
name|readableBytes
argument_list|()
decl_stmt|;
name|writeInternal
argument_list|(
name|b
argument_list|,
name|currentOffset
argument_list|,
name|iterationLen
argument_list|)
expr_stmt|;
name|currentOffset
operator|+=
name|iterationLen
expr_stmt|;
name|bytesRemaining
operator|-=
name|iterationLen
expr_stmt|;
block|}
if|if
condition|(
name|bytesRemaining
operator|>
literal|0
condition|)
block|{
name|writeInternal
argument_list|(
name|b
argument_list|,
name|currentOffset
argument_list|,
name|bytesRemaining
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
if|if
condition|(
name|buf
operator|.
name|isReadable
argument_list|()
condition|)
block|{
name|writeToChannel
argument_list|()
expr_stmt|;
block|}
name|chc
operator|.
name|flush
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Already closed: "
operator|+
name|id
argument_list|)
throw|;
block|}
try|try
block|{
name|flush
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error flushing stream before close"
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
comment|// Wait for all writes to finish before we actually close.
name|waitForWritesToFinish
argument_list|(
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
name|chc
operator|.
name|close
argument_list|()
operator|.
name|addListener
argument_list|(
name|closeListener
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|buf
operator|.
name|release
argument_list|()
expr_stmt|;
name|buf
operator|=
literal|null
expr_stmt|;
name|chc
operator|=
literal|null
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|waitForWritesToFinish
parameter_list|(
name|int
name|desiredWriteCount
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|writeMonitor
init|)
block|{
comment|// to prevent spurious wake up
while|while
condition|(
name|pendingWrites
operator|>
name|desiredWriteCount
condition|)
block|{
try|try
block|{
name|writeMonitor
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Interrupted while waiting for write operations to finish for "
operator|+
name|id
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|writeToChannel
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Already closed: "
operator|+
name|id
argument_list|)
throw|;
block|}
comment|// Wait if we have exceeded our max pending write count
name|waitForWritesToFinish
argument_list|(
name|maxPendingWrites
operator|-
literal|1
argument_list|)
expr_stmt|;
name|pendingWrites
operator|++
expr_stmt|;
name|chc
operator|.
name|writeAndFlush
argument_list|(
name|buf
operator|.
name|copy
argument_list|()
argument_list|)
operator|.
name|addListener
argument_list|(
name|writeListener
argument_list|)
expr_stmt|;
name|buf
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeInternal
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
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Already closed: "
operator|+
name|id
argument_list|)
throw|;
block|}
name|buf
operator|.
name|writeBytes
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
if|if
condition|(
name|buf
operator|.
name|readableBytes
argument_list|()
operator|>=
name|bufSize
condition|)
block|{
name|writeToChannel
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

