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
name|llap
operator|.
name|loader
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
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentLinkedQueue
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
name|llap
operator|.
name|DebugUtils
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
name|llap
operator|.
name|api
operator|.
name|Llap
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
name|llap
operator|.
name|api
operator|.
name|Vector
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
name|llap
operator|.
name|api
operator|.
name|impl
operator|.
name|RequestImpl
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
name|llap
operator|.
name|api
operator|.
name|impl
operator|.
name|VectorImpl
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
name|llap
operator|.
name|cache
operator|.
name|BufferPool
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
name|llap
operator|.
name|cache
operator|.
name|BufferPool
operator|.
name|WeakBuffer
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
name|llap
operator|.
name|chunk
operator|.
name|ChunkWriterImpl
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
name|llap
operator|.
name|loader
operator|.
name|ChunkPool
operator|.
name|Chunk
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
name|llap
operator|.
name|processor
operator|.
name|ChunkConsumer
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
name|llap
operator|.
name|processor
operator|.
name|ChunkProducerFeedback
import|;
end_import

begin_comment
comment|// TODO: write unit tests if this class becomes less primitive.
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Loader
block|{
comment|// For now, we have one buffer pool. Add bufferpool per load when needed.
specifier|private
specifier|final
name|BufferPool
name|bufferPool
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentLinkedQueue
argument_list|<
name|BufferInProgress
argument_list|>
name|reusableBuffers
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|BufferInProgress
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|ChunkWriterImpl
name|writer
decl_stmt|;
specifier|public
name|Loader
parameter_list|(
name|BufferPool
name|bufferPool
parameter_list|)
block|{
name|this
operator|.
name|bufferPool
operator|=
name|bufferPool
expr_stmt|;
name|this
operator|.
name|writer
operator|=
operator|new
name|ChunkWriterImpl
argument_list|()
expr_stmt|;
block|}
specifier|protected
class|class
name|LoadContext
implements|implements
name|ChunkProducerFeedback
block|{
specifier|public
specifier|volatile
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|returnCompleteVector
parameter_list|(
name|Vector
name|vector
parameter_list|)
block|{
name|Loader
operator|.
name|this
operator|.
name|returnCompleteVector
argument_list|(
name|vector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|isStopped
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|final
name|void
name|load
parameter_list|(
name|RequestImpl
name|request
parameter_list|,
name|ChunkConsumer
name|consumer
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// TODO: this API is subject to change, just a placeholder. Ideally we want to refactor
comment|//       so that working with cache and buffer allocation/locking would be here, but right
comment|//       now it depends on OrcLoader (esp. locking is hard to pull out).
name|LoadContext
name|context
init|=
operator|new
name|LoadContext
argument_list|()
decl_stmt|;
name|consumer
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
comment|// passed as ChunkProducerFeedback
name|loadInternal
argument_list|(
name|request
argument_list|,
name|consumer
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|returnCompleteVector
parameter_list|(
name|Vector
name|vector
parameter_list|)
block|{
name|VectorImpl
name|vectorImpl
init|=
operator|(
name|VectorImpl
operator|)
name|vector
decl_stmt|;
for|for
control|(
name|BufferPool
operator|.
name|WeakBuffer
name|buffer
range|:
name|vectorImpl
operator|.
name|getCacheBuffers
argument_list|()
control|)
block|{
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|Llap
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Unlocking "
operator|+
name|buffer
operator|+
literal|" because reader is done"
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// TODO: this API is subject to change, just a placeholder.
specifier|protected
specifier|abstract
name|void
name|loadInternal
parameter_list|(
name|RequestImpl
name|request
parameter_list|,
name|ChunkConsumer
name|consumer
parameter_list|,
name|LoadContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
specifier|protected
specifier|final
name|BufferInProgress
name|prepareReusableBuffer
parameter_list|(
name|HashSet
argument_list|<
name|WeakBuffer
argument_list|>
name|resultBuffers
parameter_list|)
throws|throws
name|InterruptedException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|BufferInProgress
name|buf
init|=
name|reusableBuffers
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|buf
operator|==
literal|null
condition|)
block|{
name|WeakBuffer
name|newWb
init|=
name|bufferPool
operator|.
name|allocateBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|resultBuffers
operator|.
name|add
argument_list|(
name|newWb
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cannot add new buffer to resultBuffers"
argument_list|)
throw|;
block|}
return|return
operator|new
name|BufferInProgress
argument_list|(
name|newWb
argument_list|)
return|;
block|}
if|if
condition|(
name|resultBuffers
operator|.
name|add
argument_list|(
name|buf
operator|.
name|buffer
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|buf
operator|.
name|buffer
operator|.
name|lock
argument_list|(
literal|true
argument_list|)
condition|)
block|{
name|resultBuffers
operator|.
name|remove
argument_list|(
name|buf
operator|.
name|buffer
argument_list|)
expr_stmt|;
continue|continue;
comment|// Buffer was evicted.
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|Llap
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Locked "
operator|+
name|buf
operator|.
name|buffer
operator|+
literal|" due to reuse"
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|buf
operator|.
name|buffer
operator|.
name|isLocked
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|buf
operator|.
name|buffer
operator|+
literal|" is in resultBuffers, but is not locked"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|protected
specifier|final
name|void
name|returnReusableBuffer
parameter_list|(
name|BufferInProgress
name|colBuffer
parameter_list|)
block|{
comment|// Check space - 16 is chunk header plus one segment header, minimum required space.
comment|// This should be extremely rare.
comment|// TODO: use different value that makes some sense
comment|// TODO: with large enough stripes it might be better not to split every stripe into two
comment|//       buffers but instead not reuse the buffer if e.g. 1Mb/15Mb is left.
if|if
condition|(
name|colBuffer
operator|.
name|getSpaceLeft
argument_list|()
operator|<
literal|16
condition|)
return|return;
name|reusableBuffers
operator|.
name|add
argument_list|(
name|colBuffer
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Chunk
name|mergeResultChunks
parameter_list|(
name|BufferInProgress
name|colBuffer
parameter_list|,
name|Chunk
name|existingResult
parameter_list|,
name|boolean
name|finalCheck
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Both should be extracted in one method, but it's too painful to do in Java.
name|int
name|rowCount
init|=
name|colBuffer
operator|.
name|getChunkInProgressRows
argument_list|()
decl_stmt|;
name|Chunk
name|chunk
init|=
name|colBuffer
operator|.
name|extractChunk
argument_list|()
decl_stmt|;
if|if
condition|(
name|rowCount
operator|<=
literal|0
condition|)
block|{
if|if
condition|(
name|finalCheck
operator|&&
name|existingResult
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No rows written for column"
argument_list|)
throw|;
block|}
return|return
name|existingResult
return|;
block|}
name|writer
operator|.
name|finishChunk
argument_list|(
name|chunk
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
return|return
operator|(
name|existingResult
operator|==
literal|null
operator|)
condition|?
name|chunk
else|:
name|existingResult
operator|.
name|addChunk
argument_list|(
name|chunk
argument_list|)
return|;
block|}
block|}
end_class

end_unit

