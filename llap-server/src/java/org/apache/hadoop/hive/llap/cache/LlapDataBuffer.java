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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|io
operator|.
name|api
operator|.
name|cache
operator|.
name|LlapMemoryBuffer
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
name|io
operator|.
name|api
operator|.
name|impl
operator|.
name|LlapIoImpl
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|LlapDataBuffer
extends|extends
name|LlapCacheableBuffer
implements|implements
name|LlapMemoryBuffer
block|{
comment|// For now, we don't track refcount for metadata blocks, don't clear them, don't reuse them and
comment|// basically rely on GC to remove them. So, refcount only applies to data blocks. If that
comment|// changes, refcount management should be move to LlapCacheableBuffer to be shared.
specifier|private
specifier|static
specifier|final
name|int
name|EVICTED_REFCOUNT
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
specifier|final
name|AtomicInteger
name|refCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|ByteBuffer
name|byteBuffer
decl_stmt|;
comment|/** Allocator uses this to remember which arena to alloc from.    * TODO Could wrap ByteBuffer instead? This needs reference anyway. */
specifier|public
name|int
name|arenaIndex
init|=
operator|-
literal|1
decl_stmt|;
comment|/** ORC cache uses this to store compressed length; buffer is cached uncompressed, but    * the lookup is on compressed ranges, so we need to know this. */
specifier|public
name|int
name|declaredLength
decl_stmt|;
specifier|public
name|int
name|allocSize
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|int
name|arenaIndex
parameter_list|,
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
operator|.
name|slice
argument_list|()
expr_stmt|;
name|this
operator|.
name|byteBuffer
operator|.
name|position
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|this
operator|.
name|byteBuffer
operator|.
name|limit
argument_list|(
name|offset
operator|+
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|arenaIndex
operator|=
name|arenaIndex
expr_stmt|;
name|this
operator|.
name|allocSize
operator|=
name|length
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getByteBufferDup
parameter_list|()
block|{
return|return
name|byteBuffer
operator|.
name|duplicate
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getByteBufferRaw
parameter_list|()
block|{
return|return
name|byteBuffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemoryUsage
parameter_list|()
block|{
return|return
name|allocSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|EvictionDispatcher
name|evictionDispatcher
parameter_list|)
block|{
name|evictionDispatcher
operator|.
name|notifyEvicted
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
name|int
name|incRef
parameter_list|()
block|{
name|int
name|newRefCount
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|oldRefCount
init|=
name|refCount
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldRefCount
operator|==
name|EVICTED_REFCOUNT
condition|)
return|return
operator|-
literal|1
return|;
assert|assert
name|oldRefCount
operator|>=
literal|0
operator|:
literal|"oldRefCount is "
operator|+
name|oldRefCount
operator|+
literal|" "
operator|+
name|this
assert|;
name|newRefCount
operator|=
name|oldRefCount
operator|+
literal|1
expr_stmt|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|oldRefCount
argument_list|,
name|newRefCount
argument_list|)
condition|)
break|break;
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Locked "
operator|+
name|this
operator|+
literal|"; new ref count "
operator|+
name|newRefCount
argument_list|)
expr_stmt|;
block|}
return|return
name|newRefCount
return|;
block|}
annotation|@
name|VisibleForTesting
name|int
name|getRefCount
parameter_list|()
block|{
return|return
name|refCount
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|boolean
name|isLocked
parameter_list|()
block|{
comment|// Best-effort check. We cannot do a good check against caller thread, since
comment|// refCount could still be> 0 if someone else locked. This is used for asserts and logs.
return|return
name|refCount
operator|.
name|get
argument_list|()
operator|>
literal|0
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|boolean
name|isInvalid
parameter_list|()
block|{
return|return
name|refCount
operator|.
name|get
argument_list|()
operator|==
name|EVICTED_REFCOUNT
return|;
block|}
name|int
name|decRef
parameter_list|()
block|{
name|int
name|newRefCount
init|=
name|refCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Unlocked "
operator|+
name|this
operator|+
literal|"; refcount "
operator|+
name|newRefCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newRefCount
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected refCount "
operator|+
name|newRefCount
operator|+
literal|": "
operator|+
name|this
argument_list|)
throw|;
block|}
return|return
name|newRefCount
return|;
block|}
comment|/**    * @return Whether the we can invalidate; false if locked or already evicted.    */
annotation|@
name|Override
specifier|public
name|boolean
name|invalidate
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|value
init|=
name|refCount
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|value
argument_list|,
name|EVICTED_REFCOUNT
argument_list|)
condition|)
break|break;
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Invalidated "
operator|+
name|this
operator|+
literal|" due to eviction"
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|int
name|refCount
init|=
name|this
operator|.
name|refCount
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
literal|"0x"
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
argument_list|)
operator|+
literal|"("
operator|+
name|refCount
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

