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
name|AtomicLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|MemoryBuffer
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

begin_comment
comment|/**  * We want to have cacheable and non-allocator buffers, as well as allocator buffers with no  * cache dependency, and also ones that are both. Alas, we could only achieve this if we were  * using a real programming language.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LlapAllocatorBuffer
extends|extends
name|LlapCacheableBuffer
implements|implements
name|MemoryBuffer
block|{
specifier|private
specifier|final
name|AtomicLong
name|state
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|ByteBuffer
name|byteBuffer
decl_stmt|;
comment|/** Allocator uses this to remember the allocation size. Somewhat redundant with header. */
specifier|public
name|int
name|allocSize
decl_stmt|;
specifier|public
name|void
name|initialize
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
name|allocSize
operator|=
name|length
expr_stmt|;
block|}
specifier|public
name|void
name|initializeWithExistingSlice
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|int
name|allocSize
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
name|allocSize
operator|=
name|allocSize
expr_stmt|;
block|}
specifier|public
name|void
name|setNewAllocLocation
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|int
name|headerIx
parameter_list|)
block|{
assert|assert
name|state
operator|.
name|get
argument_list|()
operator|==
literal|0
operator|:
literal|"New buffer state is not 0 "
operator|+
name|this
assert|;
name|long
name|newState
init|=
name|State
operator|.
name|setFlag
argument_list|(
name|State
operator|.
name|setLocation
argument_list|(
literal|0
argument_list|,
name|arenaIx
argument_list|,
name|headerIx
argument_list|)
argument_list|,
name|State
operator|.
name|FLAG_NEW_ALLOC
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
literal|0
argument_list|,
name|newState
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Contention on the new buffer "
operator|+
name|this
argument_list|)
throw|;
block|}
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
specifier|public
name|int
name|incRef
parameter_list|()
block|{
return|return
name|incRefInternal
argument_list|(
literal|true
argument_list|)
return|;
block|}
name|int
name|tryIncRef
parameter_list|()
block|{
return|return
name|incRefInternal
argument_list|(
literal|false
argument_list|)
return|;
block|}
specifier|static
specifier|final
name|int
name|INCREF_EVICTED
init|=
operator|-
literal|1
decl_stmt|,
name|INCREF_FAILED
init|=
operator|-
literal|2
decl_stmt|;
specifier|private
name|int
name|incRefInternal
parameter_list|(
name|boolean
name|doWait
parameter_list|)
block|{
name|long
name|newValue
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|oldValue
init|=
name|state
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
return|return
name|INCREF_EVICTED
return|;
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|doWait
operator|||
operator|!
name|waitForState
argument_list|()
condition|)
return|return
name|INCREF_FAILED
return|;
comment|// Thread is being interrupted.
continue|continue;
block|}
name|int
name|oldRefCount
init|=
name|State
operator|.
name|getRefCount
argument_list|(
name|oldValue
argument_list|)
decl_stmt|;
assert|assert
name|oldRefCount
operator|>=
literal|0
operator|:
literal|"oldValue is "
operator|+
name|oldValue
operator|+
literal|" "
operator|+
name|this
assert|;
if|if
condition|(
name|oldRefCount
operator|==
name|State
operator|.
name|MAX_REFCOUNT
condition|)
throw|throw
operator|new
name|AssertionError
argument_list|(
name|this
argument_list|)
throw|;
name|newValue
operator|=
name|State
operator|.
name|incRefCount
argument_list|(
name|oldValue
argument_list|)
expr_stmt|;
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_NEW_ALLOC
argument_list|)
condition|)
block|{
comment|// Remove new-alloc flag on first use. Full unlock after that would imply force-discarding
comment|// this buffer is acceptable. This is kind of an ugly compact between the cache and us.
name|newValue
operator|=
name|State
operator|.
name|switchFlag
argument_list|(
name|newValue
argument_list|,
name|State
operator|.
name|FLAG_NEW_ALLOC
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
break|break;
block|}
name|int
name|newRefCount
init|=
name|State
operator|.
name|getRefCount
argument_list|(
name|newValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Locked {}; new ref count {}"
argument_list|,
name|this
argument_list|,
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
name|State
operator|.
name|getRefCount
argument_list|(
name|state
operator|.
name|get
argument_list|()
argument_list|)
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
name|State
operator|.
name|getRefCount
argument_list|(
name|state
operator|.
name|get
argument_list|()
argument_list|)
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
name|State
operator|.
name|hasFlags
argument_list|(
name|state
operator|.
name|get
argument_list|()
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
return|;
block|}
specifier|public
name|int
name|decRef
parameter_list|()
block|{
name|long
name|newState
decl_stmt|,
name|oldState
decl_stmt|;
do|do
block|{
name|oldState
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// We have to check it here since invalid decref will overflow.
name|int
name|oldRefCount
init|=
name|State
operator|.
name|getRefCount
argument_list|(
name|oldState
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldRefCount
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Invalid decRef when refCount is 0: "
operator|+
name|this
argument_list|)
throw|;
block|}
name|newState
operator|=
name|State
operator|.
name|decRefCount
argument_list|(
name|oldState
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldState
argument_list|,
name|newState
argument_list|)
condition|)
do|;
name|int
name|newRefCount
init|=
name|State
operator|.
name|getRefCount
argument_list|(
name|newState
argument_list|)
decl_stmt|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Unlocked {}; refcount {}"
argument_list|,
name|this
argument_list|,
name|newRefCount
argument_list|)
expr_stmt|;
block|}
return|return
name|newRefCount
return|;
block|}
comment|/**    * Invalidates the cached buffer. The memory allocation in memory manager is managed by the    * caller, and therefore we mark the buffer as having released the memory too.    * @return Whether the we can invalidate; false if locked or already evicted.    */
annotation|@
name|Override
specifier|public
name|int
name|invalidate
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|oldValue
init|=
name|state
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|State
operator|.
name|getRefCount
argument_list|(
name|oldValue
argument_list|)
operator|!=
literal|0
condition|)
return|return
name|INVALIDATE_FAILED
return|;
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
return|return
name|INVALIDATE_ALREADY_INVALID
return|;
name|long
name|newValue
init|=
name|State
operator|.
name|setFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
operator||
name|State
operator|.
name|FLAG_MEM_RELEASED
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
break|break;
block|}
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Invalidated {} due to eviction"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
return|return
name|INVALIDATE_OK
return|;
block|}
comment|/**    * Invalidates the uncached buffer. The memory allocation in memory manager is managed by the    * allocator; we will only mark it as released if there are no concurrent moves. In that case,    * the caller of this will release the memory; otherwise, the end of move will release.    * @return Arena index, if the buffer memory can be released; -1 otherwise.    */
specifier|public
name|int
name|invalidateAndRelease
parameter_list|()
block|{
name|boolean
name|result
decl_stmt|;
name|long
name|oldValue
decl_stmt|,
name|newValue
decl_stmt|;
do|do
block|{
name|result
operator|=
literal|false
expr_stmt|;
name|oldValue
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|State
operator|.
name|getRefCount
argument_list|(
name|oldValue
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Refcount is "
operator|+
name|State
operator|.
name|getRefCount
argument_list|(
name|oldValue
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
block|{
return|return
operator|-
literal|1
return|;
comment|// Concurrent force-eviction - ignore.
block|}
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
condition|)
block|{
comment|// No move pending, the allocator can release.
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|newValue
argument_list|,
name|State
operator|.
name|FLAG_REMOVED
operator||
name|State
operator|.
name|FLAG_MEM_RELEASED
argument_list|)
expr_stmt|;
name|result
operator|=
literal|true
expr_stmt|;
block|}
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
do|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Invalidated {} due to direct deallocation"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
comment|// Arena cannot change after we have marked it as released.
return|return
name|result
condition|?
name|State
operator|.
name|getArena
argument_list|(
name|oldValue
argument_list|)
else|:
operator|-
literal|1
return|;
block|}
comment|/**    * Marks previously invalidated buffer as released.    * @return Whether the buffer memory can be released.    */
specifier|public
name|int
name|releaseInvalidated
parameter_list|()
block|{
name|long
name|oldValue
decl_stmt|,
name|newValue
decl_stmt|;
do|do
block|{
name|oldValue
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Not invalidated"
argument_list|)
throw|;
block|}
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
operator||
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
condition|)
return|return
operator|-
literal|1
return|;
comment|// No move pending and no intervening discard, the allocator can release.
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
do|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Removed {}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
comment|// Arena cannot change after we have marked it as released.
return|return
name|State
operator|.
name|getArena
argument_list|(
name|oldValue
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|waitForState
parameter_list|()
block|{
synchronized|synchronized
init|(
name|state
init|)
block|{
try|try
block|{
name|state
operator|.
name|wait
argument_list|(
literal|10
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Buffer incRef is deffering an interrupt"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|long
name|state
init|=
name|this
operator|.
name|state
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|flags
init|=
name|State
operator|.
name|getAllFlags
argument_list|(
name|state
argument_list|)
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
name|State
operator|.
name|getRefCount
argument_list|(
name|state
argument_list|)
operator|+
operator|(
name|flags
operator|==
literal|0
condition|?
literal|""
else|:
operator|(
literal|", "
operator|+
name|State
operator|.
name|toFlagString
argument_list|(
name|flags
argument_list|)
operator|)
operator|)
operator|+
literal|")"
return|;
block|}
specifier|public
name|String
name|toDebugString
parameter_list|()
block|{
return|return
name|toDebugString
argument_list|(
name|state
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|String
name|toDebugString
parameter_list|(
name|long
name|state
parameter_list|)
block|{
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
name|State
operator|.
name|getArena
argument_list|(
name|state
argument_list|)
operator|+
literal|":"
operator|+
name|State
operator|.
name|getHeader
argument_list|(
name|state
argument_list|)
operator|+
literal|"; "
operator|+
name|allocSize
operator|+
literal|"; "
operator|+
name|State
operator|.
name|toFlagString
argument_list|(
name|State
operator|.
name|getAllFlags
argument_list|(
name|state
argument_list|)
argument_list|)
operator|+
literal|")"
return|;
block|}
specifier|public
name|boolean
name|startMoveOrDiscard
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|int
name|headerIx
parameter_list|,
name|boolean
name|isForceDiscard
parameter_list|)
block|{
name|long
name|oldValue
decl_stmt|,
name|newValue
decl_stmt|;
do|do
block|{
name|oldValue
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|State
operator|.
name|getRefCount
argument_list|(
name|oldValue
argument_list|)
operator|!=
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|flags
init|=
name|State
operator|.
name|getAllFlags
argument_list|(
name|oldValue
argument_list|)
decl_stmt|;
comment|// The only allowed flag is new-alloc, and that only if we are not discarding.
if|if
condition|(
name|flags
operator|!=
literal|0
operator|&&
operator|(
name|isForceDiscard
operator|||
name|flags
operator|!=
name|State
operator|.
name|FLAG_NEW_ALLOC
operator|)
condition|)
block|{
return|return
literal|false
return|;
comment|// We could start a move if it's being evicted, but let's not do it for now.
block|}
if|if
condition|(
name|State
operator|.
name|getArena
argument_list|(
name|oldValue
argument_list|)
operator|!=
name|arenaIx
operator|||
name|State
operator|.
name|getHeader
argument_list|(
name|oldValue
argument_list|)
operator|!=
name|headerIx
condition|)
block|{
return|return
literal|false
return|;
comment|// The caller could re-check the location, but would probably find it locked.
block|}
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
do|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Locked {} in preparation for a move"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * @return null if no action is required; otherwise, the buffer should be deallocated and,    *         if the value is true, its memory should be released to the memory manager.    */
specifier|public
name|Boolean
name|cancelDiscard
parameter_list|()
block|{
name|long
name|oldValue
decl_stmt|,
name|newValue
decl_stmt|;
name|Boolean
name|result
decl_stmt|;
do|do
block|{
name|oldValue
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
assert|assert
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
operator|:
name|this
operator|.
name|toDebugString
argument_list|()
assert|;
name|newValue
operator|=
name|State
operator|.
name|switchFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
expr_stmt|;
name|result
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
block|{
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Removed during the move "
operator|+
name|this
argument_list|)
throw|;
block|}
name|result
operator|=
operator|!
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MEM_RELEASED
argument_list|)
expr_stmt|;
comment|// Not necessary here cause noone will be looking at these after us; set them for clarity.
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|newValue
argument_list|,
name|State
operator|.
name|FLAG_MEM_RELEASED
operator||
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
do|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Move ended for {}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|state
init|)
block|{
name|state
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return null if no action is required; otherwise, the buffer should be deallocated and,    *         if the value is true, its memory should be released to the memory manager.    */
specifier|public
name|Boolean
name|endDiscard
parameter_list|()
block|{
name|long
name|oldValue
decl_stmt|,
name|newValue
decl_stmt|;
name|Boolean
name|result
decl_stmt|;
do|do
block|{
name|oldValue
operator|=
name|state
operator|.
name|get
argument_list|()
expr_stmt|;
assert|assert
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
assert|;
name|newValue
operator|=
name|State
operator|.
name|switchFlag
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MOVING
argument_list|)
expr_stmt|;
name|newValue
operator|=
name|State
operator|.
name|setFlag
argument_list|(
name|newValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
operator||
name|State
operator|.
name|FLAG_MEM_RELEASED
operator||
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
expr_stmt|;
name|result
operator|=
literal|null
expr_stmt|;
comment|// See if someone else evicted this in parallel.
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_EVICTED
argument_list|)
condition|)
block|{
if|if
condition|(
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_REMOVED
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Removed during the move "
operator|+
name|this
argument_list|)
throw|;
block|}
name|result
operator|=
operator|!
name|State
operator|.
name|hasFlags
argument_list|(
name|oldValue
argument_list|,
name|State
operator|.
name|FLAG_MEM_RELEASED
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
operator|!
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldValue
argument_list|,
name|newValue
argument_list|)
condition|)
do|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOCKING_LOGGER
operator|.
name|trace
argument_list|(
literal|"Discared {}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|state
init|)
block|{
name|state
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|State
block|{
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_MOVING
init|=
literal|0b00001
decl_stmt|,
comment|// Locked by someone to move or force-evict.
name|FLAG_EVICTED
init|=
literal|0b00010
decl_stmt|,
comment|// Evicted. This is cache-specific.
name|FLAG_REMOVED
init|=
literal|0b00100
decl_stmt|,
comment|// Removed from allocator structures. The final state.
name|FLAG_MEM_RELEASED
init|=
literal|0b01000
decl_stmt|,
comment|// The memory was released to memory manager.
name|FLAG_NEW_ALLOC
init|=
literal|0b10000
decl_stmt|;
comment|// New allocation before the first use; cannot force-evict.
specifier|private
specifier|static
specifier|final
name|int
name|FLAGS_WIDTH
init|=
literal|5
decl_stmt|,
name|REFCOUNT_WIDTH
init|=
literal|19
decl_stmt|,
name|ARENA_WIDTH
init|=
literal|16
decl_stmt|,
name|HEADER_WIDTH
init|=
literal|24
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|MAX_REFCOUNT
init|=
operator|(
literal|1
operator|<<
name|REFCOUNT_WIDTH
operator|)
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REFCOUNT_SHIFT
init|=
name|FLAGS_WIDTH
decl_stmt|,
name|ARENA_SHIFT
init|=
name|REFCOUNT_SHIFT
operator|+
name|REFCOUNT_WIDTH
decl_stmt|,
name|HEADER_SHIFT
init|=
name|ARENA_SHIFT
operator|+
name|ARENA_WIDTH
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|FLAGS_MASK
init|=
operator|(
literal|1L
operator|<<
name|FLAGS_WIDTH
operator|)
operator|-
literal|1
decl_stmt|,
name|REFCOUNT_MASK
init|=
operator|(
operator|(
literal|1L
operator|<<
name|REFCOUNT_WIDTH
operator|)
operator|-
literal|1
operator|)
operator|<<
name|REFCOUNT_SHIFT
decl_stmt|,
name|ARENA_MASK
init|=
operator|(
operator|(
literal|1L
operator|<<
name|ARENA_WIDTH
operator|)
operator|-
literal|1
operator|)
operator|<<
name|ARENA_SHIFT
decl_stmt|,
name|HEADER_MASK
init|=
operator|(
operator|(
literal|1L
operator|<<
name|HEADER_WIDTH
operator|)
operator|-
literal|1
operator|)
operator|<<
name|HEADER_SHIFT
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|hasFlags
parameter_list|(
name|long
name|value
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
return|return
operator|(
name|value
operator|&
name|flags
operator|)
operator|!=
literal|0
return|;
block|}
specifier|public
specifier|static
name|int
name|getAllFlags
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|state
operator|&
name|FLAGS_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|getRefCount
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
operator|(
name|state
operator|&
name|REFCOUNT_MASK
operator|)
operator|>>>
name|REFCOUNT_SHIFT
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|getArena
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
operator|(
name|state
operator|&
name|ARENA_MASK
operator|)
operator|>>>
name|ARENA_SHIFT
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|getHeader
parameter_list|(
name|long
name|state
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
operator|(
name|state
operator|&
name|HEADER_MASK
operator|)
operator|>>>
name|HEADER_SHIFT
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|incRefCount
parameter_list|(
name|long
name|state
parameter_list|)
block|{
comment|// Note: doesn't check for overflow. Could AND with max refcount mask but the caller checks.
return|return
name|state
operator|+
operator|(
literal|1
operator|<<
name|REFCOUNT_SHIFT
operator|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|decRefCount
parameter_list|(
name|long
name|state
parameter_list|)
block|{
comment|// Note: doesn't check for overflow. Could AND with max refcount mask but the caller checks.
return|return
name|state
operator|-
operator|(
literal|1
operator|<<
name|REFCOUNT_SHIFT
operator|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|setLocation
parameter_list|(
name|long
name|state
parameter_list|,
name|int
name|arenaIx
parameter_list|,
name|int
name|headerIx
parameter_list|)
block|{
name|long
name|arenaVal
init|=
operator|(
operator|(
name|long
operator|)
name|arenaIx
operator|)
operator|<<
name|ARENA_SHIFT
decl_stmt|,
name|arenaWMask
init|=
name|arenaVal
operator|&
name|ARENA_MASK
decl_stmt|;
name|long
name|headerVal
init|=
operator|(
operator|(
name|long
operator|)
name|headerIx
operator|)
operator|<<
name|HEADER_SHIFT
decl_stmt|,
name|headerWMask
init|=
name|headerVal
operator|&
name|HEADER_MASK
decl_stmt|;
assert|assert
name|arenaVal
operator|==
name|arenaWMask
operator|:
literal|"Arena "
operator|+
name|arenaIx
operator|+
literal|" is wider than "
operator|+
name|ARENA_WIDTH
assert|;
assert|assert
name|headerVal
operator|==
name|headerWMask
operator|:
literal|"Header "
operator|+
name|headerIx
operator|+
literal|" is wider than "
operator|+
name|HEADER_WIDTH
assert|;
return|return
operator|(
name|state
operator|&
operator|~
operator|(
name|ARENA_MASK
operator||
name|HEADER_MASK
operator|)
operator|)
operator||
name|arenaWMask
operator||
name|headerWMask
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|setFlag
parameter_list|(
name|long
name|state
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
assert|assert
name|flags
operator|<=
name|FLAGS_MASK
assert|;
return|return
name|state
operator||
name|flags
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|switchFlag
parameter_list|(
name|long
name|state
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
assert|assert
name|flags
operator|<=
name|FLAGS_MASK
assert|;
return|return
name|state
operator|^
name|flags
return|;
block|}
specifier|public
specifier|static
name|String
name|toFlagString
parameter_list|(
name|int
name|state
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|leftPad
argument_list|(
name|Integer
operator|.
name|toBinaryString
argument_list|(
name|state
argument_list|)
argument_list|,
name|REFCOUNT_SHIFT
argument_list|,
literal|'0'
argument_list|)
return|;
block|}
block|}
annotation|@
name|VisibleForTesting
name|int
name|getArenaIndex
parameter_list|()
block|{
return|return
name|State
operator|.
name|getArena
argument_list|(
name|state
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

