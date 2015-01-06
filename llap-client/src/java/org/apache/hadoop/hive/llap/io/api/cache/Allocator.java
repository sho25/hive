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
name|io
operator|.
name|api
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  * Allocator, separate from cache for now. Depending on the scheme we choose (buddy allocator +  * every (col x rg) separately in cache, or cache of large pages with many (col x rg)), this will  * either do buddy allocation or hide the management of BufferInProgress/etc. from prototype.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Allocator
block|{
comment|// TODO: subject to change depending on memory/cache design
specifier|public
specifier|static
class|class
name|LlapBuffer
block|{
specifier|public
name|LlapBuffer
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|byteBuffer
operator|=
name|byteBuffer
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
specifier|public
name|ByteBuffer
name|byteBuffer
decl_stmt|;
specifier|public
name|int
name|offset
decl_stmt|;
specifier|public
name|int
name|length
decl_stmt|;
block|}
name|LlapBuffer
name|allocateMemory
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
name|void
name|deallocate
parameter_list|(
name|LlapBuffer
name|columnData
parameter_list|)
function_decl|;
block|}
end_interface

begin_comment
comment|/*   protected final BufferInProgress prepareReusableBuffer(       HashSet<WeakBuffer> resultBuffers) throws InterruptedException {     while (true) {       BufferInProgress buf = reusableBuffers.poll();       if (buf == null) {         WeakBuffer newWb = bufferPool.allocateBuffer();         if (!resultBuffers.add(newWb)) {           throw new AssertionError("Cannot add new buffer to resultBuffers");         }         return new BufferInProgress(newWb);       }       if (resultBuffers.add(buf.buffer)) {         if (!buf.buffer.lock(true)) {           resultBuffers.remove(buf.buffer);           continue;  // Buffer was evicted.         }         if (DebugUtils.isTraceLockingEnabled()) {           Llap.LOG.info("Locked " + buf.buffer + " due to reuse");         }       } else if (!buf.buffer.isLocked()) {         throw new AssertionError(buf.buffer + " is in resultBuffers, but is not locked");       }     }   }    protected final void returnReusableBuffer(BufferInProgress colBuffer) {     // Check space - 16 is chunk header plus one segment header, minimum required space.     // This should be extremely rare.     // TODO: use different value that makes some sense     // TODO: with large enough stripes it might be better not to split every stripe into two     //       buffers but instead not reuse the buffer if e.g. 1Mb/15Mb is left.     if (colBuffer.getSpaceLeft()< 16) return;     reusableBuffers.add(colBuffer);   } */
end_comment

end_unit

