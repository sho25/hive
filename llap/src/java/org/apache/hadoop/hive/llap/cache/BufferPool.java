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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|cache
operator|.
name|BufferPool
operator|.
name|WeakBuffer
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
class|class
name|BufferPool
block|{
comment|// TODO: we should keep evicted buffers for reuse. Perhaps that too should be factored out.
specifier|private
specifier|final
name|CachePolicy
name|cachePolicy
decl_stmt|;
specifier|private
specifier|final
name|Object
name|evictionNotifyObj
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|int
name|evictionIsWaiting
decl_stmt|;
comment|// best effort flag
specifier|private
specifier|final
name|long
name|maxCacheSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
name|EvictionListener
name|evictionListener
decl_stmt|;
specifier|public
name|BufferPool
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|EvictionListener
name|evictionListener
parameter_list|)
block|{
name|this
operator|.
name|evictionListener
operator|=
name|evictionListener
expr_stmt|;
name|this
operator|.
name|maxCacheSize
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_CACHE_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_BUFFER_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|cachePolicy
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_USE_LRFU
argument_list|)
condition|?
operator|new
name|LrfuCachePolicy
argument_list|(
name|conf
argument_list|,
name|bufferSize
argument_list|,
name|maxCacheSize
argument_list|)
else|:
operator|new
name|FifoCachePolicy
argument_list|(
name|bufferSize
argument_list|,
name|maxCacheSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Allocates a new buffer. Buffer starts out locked (assumption is that caller is going to    * write to it immediately and then unlock it; future writers/readers will lock and unlock).    * @return Buffer.    */
specifier|public
name|WeakBuffer
name|allocateBuffer
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// TODO: for now, dumb byte arrays. Should be off-heap.
name|ByteBuffer
name|newBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|bufferSize
argument_list|)
decl_stmt|;
name|WeakBuffer
name|wb
init|=
operator|new
name|WeakBuffer
argument_list|(
name|this
argument_list|,
name|newBuffer
argument_list|)
decl_stmt|;
comment|// Don't touch the buffer - it's not in cache yet. cache() will set the initial priority.
if|if
condition|(
operator|!
name|wb
operator|.
name|lock
argument_list|(
literal|false
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cannot lock a new buffer"
argument_list|)
throw|;
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
name|wb
operator|+
literal|" after creation"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|hasWaited
init|=
literal|false
decl_stmt|;
name|WeakBuffer
name|evicted
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|evicted
operator|=
name|cachePolicy
operator|.
name|cache
argument_list|(
name|wb
argument_list|)
expr_stmt|;
if|if
condition|(
name|evicted
operator|!=
name|CachePolicy
operator|.
name|CANNOT_EVICT
condition|)
break|break;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceCachingEnabled
argument_list|()
operator|&&
operator|!
name|hasWaited
condition|)
block|{
name|Llap
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to add a new block to cache; waiting for blocks to be unlocked"
argument_list|)
expr_stmt|;
name|hasWaited
operator|=
literal|true
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|evictionNotifyObj
init|)
block|{
operator|++
name|evictionIsWaiting
expr_stmt|;
name|evictionNotifyObj
operator|.
name|wait
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
operator|--
name|evictionIsWaiting
expr_stmt|;
block|}
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceCachingEnabled
argument_list|()
operator|&&
name|hasWaited
condition|)
block|{
name|Llap
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Eviction is done waiting"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|evicted
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|evictionListener
operator|!=
literal|null
condition|)
block|{
name|evictionListener
operator|.
name|evictionNotice
argument_list|(
name|evicted
argument_list|)
expr_stmt|;
block|}
comment|// After eviction notice, the contents can be reset.
name|evicted
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|wb
return|;
block|}
specifier|private
specifier|final
name|void
name|unblockEviction
parameter_list|()
block|{
if|if
condition|(
name|evictionIsWaiting
operator|<=
literal|0
condition|)
return|return;
synchronized|synchronized
init|(
name|evictionNotifyObj
init|)
block|{
if|if
condition|(
name|evictionIsWaiting
operator|<=
literal|0
condition|)
return|return;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceCachingEnabled
argument_list|()
condition|)
block|{
name|Llap
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Notifying eviction that some block has been unlocked"
argument_list|)
expr_stmt|;
block|}
name|evictionNotifyObj
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|WeakBuffer
name|allocateFake
parameter_list|()
block|{
return|return
operator|new
name|WeakBuffer
argument_list|(
literal|null
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * This class serves 3 purposes:    * 1) it implements BufferPool-specific hashCode and equals (ByteBuffer ones are content-based);    * 2) it contains the refCount;    * 3) by extension from (2), it can be held while it is evicted; when locking before the usage,    *    the fact that the data has been evicted will be discovered (similar to weak_ptr).    * Note: not static because when we wait for something to become evict-able,    * we need to receive notifications from unlock (see unlock). Otherwise could be static.    */
specifier|public
specifier|static
specifier|final
class|class
name|WeakBuffer
block|{
specifier|private
specifier|static
specifier|final
name|int
name|EVICTED_REFCOUNT
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|BufferPool
name|parent
decl_stmt|;
specifier|private
name|ByteBuffer
name|contents
decl_stmt|;
specifier|private
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
comment|// TODO: Fields pertaining to cache policy. Perhaps they should live in separate object.
specifier|public
name|double
name|priority
decl_stmt|;
specifier|public
name|long
name|lastUpdate
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|int
name|indexInHeap
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|boolean
name|isLockedInHeap
init|=
literal|false
decl_stmt|;
specifier|private
name|WeakBuffer
parameter_list|(
name|BufferPool
name|parent
parameter_list|,
name|ByteBuffer
name|contents
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|contents
operator|=
name|contents
expr_stmt|;
block|}
specifier|public
name|ByteBuffer
name|getContents
parameter_list|()
block|{
assert|assert
name|isLocked
argument_list|()
operator|:
literal|"Cannot get contents with refCount "
operator|+
name|refCount
operator|.
name|get
argument_list|()
assert|;
return|return
name|contents
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
name|contents
operator|==
literal|null
condition|)
return|return
literal|0
return|;
return|return
name|System
operator|.
name|identityHashCode
argument_list|(
name|contents
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|WeakBuffer
operator|)
condition|)
return|return
literal|false
return|;
comment|// We only compare objects, and not contents of the ByteBuffer.
comment|// One ByteBuffer is never put in multiple WeakBuffer-s (that is the invariant).
return|return
name|contents
operator|==
operator|(
operator|(
name|WeakBuffer
operator|)
name|obj
operator|)
operator|.
name|contents
return|;
block|}
specifier|public
name|boolean
name|lock
parameter_list|(
name|boolean
name|doTouch
parameter_list|)
block|{
name|int
name|oldRefCount
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|oldRefCount
operator|=
name|refCount
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|oldRefCount
operator|==
name|EVICTED_REFCOUNT
condition|)
return|return
literal|false
return|;
assert|assert
name|oldRefCount
operator|>=
literal|0
assert|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|oldRefCount
argument_list|,
name|oldRefCount
operator|+
literal|1
argument_list|)
condition|)
break|break;
block|}
if|if
condition|(
name|doTouch
operator|&&
name|oldRefCount
operator|==
literal|0
operator|&&
name|parent
operator|!=
literal|null
condition|)
block|{
name|parent
operator|.
name|cachePolicy
operator|.
name|notifyLock
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|isLocked
parameter_list|()
block|{
comment|// Best-effort check. We cannot do a good check against caller thread, since
comment|// refCount could still be> 0 if someone else locked. This is used for asserts.
return|return
name|refCount
operator|.
name|get
argument_list|()
operator|>
literal|0
return|;
block|}
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
specifier|public
name|boolean
name|isCleared
parameter_list|()
block|{
return|return
name|contents
operator|==
literal|null
return|;
block|}
specifier|public
name|void
name|unlock
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
argument_list|)
throw|;
block|}
comment|// If this block became eligible, see if we need to unblock the eviction.
if|if
condition|(
name|newRefCount
operator|==
literal|0
operator|&&
name|parent
operator|!=
literal|null
condition|)
block|{
name|parent
operator|.
name|cachePolicy
operator|.
name|notifyUnlock
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|parent
operator|.
name|unblockEviction
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"0x"
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * @return Whether the we can invalidate; false if locked or already evicted.      */
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
name|Llap
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
name|ByteBuffer
name|clear
parameter_list|()
block|{
assert|assert
name|refCount
operator|.
name|get
argument_list|()
operator|==
name|EVICTED_REFCOUNT
assert|;
name|ByteBuffer
name|result
init|=
name|contents
decl_stmt|;
name|contents
operator|=
literal|null
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|String
name|toStringForCache
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|hashCode
argument_list|()
argument_list|)
operator|+
literal|" "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$.2f"
argument_list|,
name|priority
argument_list|)
operator|+
literal|" "
operator|+
name|lastUpdate
operator|+
literal|" "
operator|+
operator|(
name|isLocked
argument_list|()
condition|?
literal|"!"
else|:
literal|"."
operator|)
operator|+
literal|"]"
return|;
block|}
block|}
block|}
end_class

end_unit

