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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|lang
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|LowLevelCache
operator|.
name|Priority
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

begin_comment
comment|/**  * Implementation of the algorithm from "On the Existence of a Spectrum of Policies  * that Subsumes the Least Recently Used (LRU) and Least Frequently Used (LFU) Policies".  * Additionally, buffer locking has to be handled (locked buffer cannot be evicted).  */
end_comment

begin_class
specifier|public
class|class
name|LowLevelLrfuCachePolicy
implements|implements
name|LowLevelCachePolicy
block|{
specifier|private
specifier|final
name|double
name|lambda
decl_stmt|;
specifier|private
specifier|final
name|double
name|f
parameter_list|(
name|long
name|x
parameter_list|)
block|{
return|return
name|Math
operator|.
name|pow
argument_list|(
literal|0.5
argument_list|,
name|lambda
operator|*
name|x
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|double
name|F0
init|=
literal|1
decl_stmt|;
comment|// f(0) is always 1
specifier|private
specifier|final
name|double
name|touchPriority
parameter_list|(
name|long
name|time
parameter_list|,
name|long
name|lastAccess
parameter_list|,
name|double
name|previous
parameter_list|)
block|{
return|return
name|F0
operator|+
name|f
argument_list|(
name|time
operator|-
name|lastAccess
argument_list|)
operator|*
name|previous
return|;
block|}
specifier|private
specifier|final
name|double
name|expirePriority
parameter_list|(
name|long
name|time
parameter_list|,
name|long
name|lastAccess
parameter_list|,
name|double
name|previous
parameter_list|)
block|{
return|return
name|f
argument_list|(
name|time
operator|-
name|lastAccess
argument_list|)
operator|*
name|previous
return|;
block|}
specifier|private
specifier|final
name|AtomicLong
name|timer
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**    * The heap and list. Currently synchronized on the object, which is not good. If this becomes    * a problem (which it probably will), we can partition the cache policy, or use some better    * structure. Heap should not be locked while holding the lock on list.    * As of now, eviction in most cases will only need the list; locking doesn't do anything;    * unlocking actually places item in evictable cache - unlocking is done after processing,    * so this most expensive part (and only access to heap in most cases) will not affect it.    * Perhaps we should use ConcurrentDoubleLinkedList (in public domain).    * ONLY LIST REMOVAL is allowed under list lock.    */
specifier|private
specifier|final
name|LlapCacheableBuffer
index|[]
name|heap
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|listLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
name|LlapCacheableBuffer
name|listHead
decl_stmt|,
name|listTail
decl_stmt|;
comment|/** Number of elements. */
specifier|private
name|int
name|heapSize
init|=
literal|0
decl_stmt|;
specifier|private
name|EvictionListener
name|evictionListener
decl_stmt|;
specifier|public
name|LowLevelLrfuCachePolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|long
name|maxSize
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MAX_SIZE
argument_list|)
decl_stmt|;
name|int
name|minBufferSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MIN_ALLOC
argument_list|)
decl_stmt|;
name|lambda
operator|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_LRFU_LAMBDA
argument_list|)
expr_stmt|;
name|int
name|maxBuffers
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|maxSize
operator|*
literal|1.0
operator|)
operator|/
name|minBufferSize
argument_list|)
decl_stmt|;
name|int
name|maxHeapSize
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|lambda
operator|==
literal|0
condition|)
block|{
name|maxHeapSize
operator|=
name|maxBuffers
expr_stmt|;
comment|// lrfuThreshold is +inf in this case
block|}
else|else
block|{
name|int
name|lrfuThreshold
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|Math
operator|.
name|log
argument_list|(
literal|1
operator|-
name|Math
operator|.
name|pow
argument_list|(
literal|0.5
argument_list|,
name|lambda
argument_list|)
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|0.5
argument_list|)
operator|)
operator|/
name|lambda
argument_list|)
decl_stmt|;
name|maxHeapSize
operator|=
name|Math
operator|.
name|min
argument_list|(
name|lrfuThreshold
argument_list|,
name|maxBuffers
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOGL
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"LRFU cache policy with min buffer size "
operator|+
name|minBufferSize
operator|+
literal|" and lambda "
operator|+
name|lambda
operator|+
literal|" (heap size "
operator|+
name|maxHeapSize
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|heap
operator|=
operator|new
name|LlapCacheableBuffer
index|[
name|maxHeapSize
index|]
expr_stmt|;
name|listHead
operator|=
name|listTail
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cache
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|,
name|Priority
name|priority
parameter_list|)
block|{
comment|// LRFU cache policy doesn't store locked blocks. When we cache, the block is locked, so
comment|// we simply do nothing here. The fact that it was never updated will allow us to add it
comment|// properly on the first notifyUnlock.
comment|// We'll do is set priority, to account for the inbound one. No lock - not in heap.
assert|assert
name|buffer
operator|.
name|lastUpdate
operator|==
operator|-
literal|1
assert|;
name|long
name|time
init|=
name|timer
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|priority
operator|=
name|F0
expr_stmt|;
name|buffer
operator|.
name|lastUpdate
operator|=
name|time
expr_stmt|;
if|if
condition|(
name|priority
operator|==
name|Priority
operator|.
name|HIGH
condition|)
block|{
name|buffer
operator|.
name|priority
operator|*=
literal|8
expr_stmt|;
comment|// this is arbitrary
block|}
else|else
block|{
assert|assert
name|priority
operator|==
name|Priority
operator|.
name|NORMAL
assert|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
comment|// We do not proactively remove locked items from the heap, and opportunistically try to
comment|// remove from the list (since eviction is mostly from the list). If eviction stumbles upon
comment|// a locked item in either, it will remove it from cache; when we unlock, we are going to
comment|// put it back or update it, depending on whether this has happened. This should cause
comment|// most of the expensive cache update work to happen in unlock, not blocking processing.
if|if
condition|(
name|buffer
operator|.
name|indexInHeap
operator|!=
name|LlapCacheableBuffer
operator|.
name|IN_LIST
condition|)
return|return;
if|if
condition|(
operator|!
name|listLock
operator|.
name|tryLock
argument_list|()
condition|)
return|return;
name|removeFromListAndUnlock
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyUnlock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
name|long
name|time
init|=
name|timer
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceCachingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Touching "
operator|+
name|buffer
operator|+
literal|" at "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|heap
init|)
block|{
comment|// First, update buffer priority - we have just been using it.
name|buffer
operator|.
name|priority
operator|=
operator|(
name|buffer
operator|.
name|lastUpdate
operator|==
operator|-
literal|1
operator|)
condition|?
name|F0
else|:
name|touchPriority
argument_list|(
name|time
argument_list|,
name|buffer
operator|.
name|lastUpdate
argument_list|,
name|buffer
operator|.
name|priority
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|lastUpdate
operator|=
name|time
expr_stmt|;
comment|// Then, if the buffer was in the list, remove it.
if|if
condition|(
name|buffer
operator|.
name|indexInHeap
operator|==
name|LlapCacheableBuffer
operator|.
name|IN_LIST
condition|)
block|{
name|listLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|removeFromListAndUnlock
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
comment|// The only concurrent change that can happen when we hold the heap lock is list removal;
comment|// we have just ensured the item is not in the list, so we have a definite state now.
if|if
condition|(
name|buffer
operator|.
name|indexInHeap
operator|>=
literal|0
condition|)
block|{
comment|// The buffer has lived in the heap all along. Restore heap property.
name|heapifyDownUnderLock
argument_list|(
name|buffer
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|heapSize
operator|==
name|heap
operator|.
name|length
condition|)
block|{
comment|// The buffer is not in the (full) heap. Demote the top item of the heap into the list.
name|LlapCacheableBuffer
name|demoted
init|=
name|heap
index|[
literal|0
index|]
decl_stmt|;
synchronized|synchronized
init|(
name|listLock
init|)
block|{
name|demoted
operator|.
name|indexInHeap
operator|=
name|LlapCacheableBuffer
operator|.
name|IN_LIST
expr_stmt|;
name|demoted
operator|.
name|prev
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|listHead
operator|!=
literal|null
condition|)
block|{
name|demoted
operator|.
name|next
operator|=
name|listHead
expr_stmt|;
name|listHead
operator|.
name|prev
operator|=
name|demoted
expr_stmt|;
name|listHead
operator|=
name|demoted
expr_stmt|;
block|}
else|else
block|{
name|listHead
operator|=
name|listTail
operator|=
name|demoted
expr_stmt|;
name|demoted
operator|.
name|next
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|// Now insert the buffer in its place and restore heap property.
name|buffer
operator|.
name|indexInHeap
operator|=
literal|0
expr_stmt|;
name|heapifyDownUnderLock
argument_list|(
name|buffer
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Heap is not full, add the buffer to the heap and restore heap property up.
assert|assert
name|heapSize
operator|<
name|heap
operator|.
name|length
operator|:
name|heap
operator|.
name|length
operator|+
literal|"< "
operator|+
name|heapSize
assert|;
name|buffer
operator|.
name|indexInHeap
operator|=
name|heapSize
expr_stmt|;
name|heapifyUpUnderLock
argument_list|(
name|buffer
argument_list|,
name|time
argument_list|)
expr_stmt|;
operator|++
name|heapSize
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setEvictionListener
parameter_list|(
name|EvictionListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|evictionListener
operator|=
name|listener
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|evictSomeBlocks
parameter_list|(
name|long
name|memoryToReserve
parameter_list|)
block|{
name|long
name|evicted
init|=
literal|0
decl_stmt|;
comment|// In normal case, we evict the items from the list.
name|LlapCacheableBuffer
name|nextCandidate
decl_stmt|,
name|firstCandidate
decl_stmt|;
name|listLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|nextCandidate
operator|=
name|firstCandidate
operator|=
name|listTail
expr_stmt|;
while|while
condition|(
name|evicted
operator|<
name|memoryToReserve
operator|&&
name|nextCandidate
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|nextCandidate
operator|.
name|invalidate
argument_list|()
condition|)
block|{
comment|// Locked buffer was in the list - just drop it; will be re-added on unlock.
name|LlapCacheableBuffer
name|lockedBuffer
init|=
name|nextCandidate
decl_stmt|;
if|if
condition|(
name|firstCandidate
operator|==
name|nextCandidate
condition|)
block|{
name|firstCandidate
operator|=
name|nextCandidate
operator|.
name|prev
expr_stmt|;
block|}
name|nextCandidate
operator|=
name|nextCandidate
operator|.
name|prev
expr_stmt|;
name|removeFromListUnderLock
argument_list|(
name|lockedBuffer
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Update the state to removed-from-list, so that parallel notifyUnlock doesn't modify us.
name|nextCandidate
operator|.
name|indexInHeap
operator|=
name|LlapCacheableBuffer
operator|.
name|NOT_IN_CACHE
expr_stmt|;
name|evicted
operator|+=
name|nextCandidate
operator|.
name|getMemoryUsage
argument_list|()
expr_stmt|;
name|nextCandidate
operator|=
name|nextCandidate
operator|.
name|prev
expr_stmt|;
block|}
if|if
condition|(
name|firstCandidate
operator|!=
name|nextCandidate
condition|)
block|{
if|if
condition|(
name|nextCandidate
operator|==
literal|null
condition|)
block|{
name|listHead
operator|=
name|listTail
operator|=
literal|null
expr_stmt|;
comment|// We have evicted the entire list.
block|}
else|else
block|{
comment|// Splice the section that we have evicted out of the list.
name|removeFromListUnderLock
argument_list|(
name|nextCandidate
operator|.
name|next
argument_list|,
name|firstCandidate
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|listLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
while|while
condition|(
name|firstCandidate
operator|!=
name|nextCandidate
condition|)
block|{
name|evictionListener
operator|.
name|notifyEvicted
argument_list|(
name|firstCandidate
argument_list|)
expr_stmt|;
name|firstCandidate
operator|=
name|firstCandidate
operator|.
name|prev
expr_stmt|;
block|}
if|if
condition|(
name|evicted
operator|>=
name|memoryToReserve
condition|)
return|return
name|evicted
return|;
comment|// This should not happen unless we are evicting a lot at once, or buffers are large (so
comment|// there's a small number of buffers and they all live in the heap).
name|long
name|time
init|=
name|timer
operator|.
name|get
argument_list|()
decl_stmt|;
while|while
condition|(
name|evicted
operator|<
name|memoryToReserve
condition|)
block|{
name|LlapCacheableBuffer
name|buffer
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|heap
init|)
block|{
name|buffer
operator|=
name|evictFromHeapUnderLock
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
return|return
name|evicted
return|;
name|evicted
operator|+=
name|buffer
operator|.
name|getMemoryUsage
argument_list|()
expr_stmt|;
name|evictionListener
operator|.
name|notifyEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
return|return
name|evicted
return|;
block|}
specifier|private
name|void
name|heapifyUpUnderLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|,
name|long
name|time
parameter_list|)
block|{
comment|// See heapifyDown comment.
name|int
name|ix
init|=
name|buffer
operator|.
name|indexInHeap
decl_stmt|;
name|double
name|priority
init|=
name|buffer
operator|.
name|priority
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|ix
operator|==
literal|0
condition|)
break|break;
comment|// Buffer is at the top of the heap.
name|int
name|parentIx
init|=
operator|(
name|ix
operator|-
literal|1
operator|)
operator|>>>
literal|1
decl_stmt|;
name|LlapCacheableBuffer
name|parent
init|=
name|heap
index|[
name|parentIx
index|]
decl_stmt|;
name|double
name|parentPri
init|=
name|getHeapifyPriority
argument_list|(
name|parent
argument_list|,
name|time
argument_list|)
decl_stmt|;
if|if
condition|(
name|priority
operator|>=
name|parentPri
condition|)
break|break;
name|heap
index|[
name|ix
index|]
operator|=
name|parent
expr_stmt|;
name|parent
operator|.
name|indexInHeap
operator|=
name|ix
expr_stmt|;
name|ix
operator|=
name|parentIx
expr_stmt|;
block|}
name|buffer
operator|.
name|indexInHeap
operator|=
name|ix
expr_stmt|;
name|heap
index|[
name|ix
index|]
operator|=
name|buffer
expr_stmt|;
block|}
comment|// Note: almost never called (unless buffers are very large or we evict a lot).
specifier|private
name|LlapCacheableBuffer
name|evictFromHeapUnderLock
parameter_list|(
name|long
name|time
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|heapSize
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|LlapCacheableBuffer
name|result
init|=
name|heap
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceCachingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Evicting "
operator|+
name|result
operator|+
literal|" at "
operator|+
name|time
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|indexInHeap
operator|=
operator|-
literal|1
expr_stmt|;
operator|--
name|heapSize
expr_stmt|;
name|boolean
name|canEvict
init|=
name|result
operator|.
name|invalidate
argument_list|()
decl_stmt|;
if|if
condition|(
name|heapSize
operator|>
literal|0
condition|)
block|{
name|LlapCacheableBuffer
name|newRoot
init|=
name|heap
index|[
name|heapSize
index|]
decl_stmt|;
name|newRoot
operator|.
name|indexInHeap
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|newRoot
operator|.
name|lastUpdate
operator|!=
name|time
condition|)
block|{
name|newRoot
operator|.
name|priority
operator|=
name|expirePriority
argument_list|(
name|time
argument_list|,
name|newRoot
operator|.
name|lastUpdate
argument_list|,
name|newRoot
operator|.
name|priority
argument_list|)
expr_stmt|;
name|newRoot
operator|.
name|lastUpdate
operator|=
name|time
expr_stmt|;
block|}
name|heapifyDownUnderLock
argument_list|(
name|newRoot
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|canEvict
condition|)
return|return
name|result
return|;
comment|// Otherwise we just removed a locked item from heap; unlock will re-add it, we continue.
block|}
block|}
specifier|private
name|void
name|heapifyDownUnderLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|,
name|long
name|time
parameter_list|)
block|{
comment|// Relative positions of the blocks don't change over time; priorities we expire can only
comment|// decrease; we only have one block that could have broken heap rule and we always move it
comment|// down; therefore, we can update priorities of other blocks as we go for part of the heap -
comment|// we correct any discrepancy w/the parent after expiring priority, and any block we expire
comment|// the priority for already has lower priority than that of its children.
comment|// TODO: avoid expiring priorities if times are close? might be needlessly expensive.
name|int
name|ix
init|=
name|buffer
operator|.
name|indexInHeap
decl_stmt|;
name|double
name|priority
init|=
name|buffer
operator|.
name|priority
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|newIx
init|=
name|moveMinChildUp
argument_list|(
name|ix
argument_list|,
name|time
argument_list|,
name|priority
argument_list|)
decl_stmt|;
if|if
condition|(
name|newIx
operator|==
operator|-
literal|1
condition|)
break|break;
name|ix
operator|=
name|newIx
expr_stmt|;
block|}
name|buffer
operator|.
name|indexInHeap
operator|=
name|ix
expr_stmt|;
name|heap
index|[
name|ix
index|]
operator|=
name|buffer
expr_stmt|;
block|}
comment|/**    * Moves the minimum child of targetPos block up to targetPos; optionally compares priorities    * and terminates if targetPos element has lesser value than either of its children.    * @return the index of the child that was moved up; -1 if nothing was moved due to absence    *         of the children, or a failed priority check.    */
specifier|private
name|int
name|moveMinChildUp
parameter_list|(
name|int
name|targetPos
parameter_list|,
name|long
name|time
parameter_list|,
name|double
name|comparePri
parameter_list|)
block|{
name|int
name|leftIx
init|=
operator|(
name|targetPos
operator|<<
literal|1
operator|)
operator|+
literal|1
decl_stmt|,
name|rightIx
init|=
name|leftIx
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|leftIx
operator|>=
name|heapSize
condition|)
return|return
operator|-
literal|1
return|;
comment|// Buffer is at the leaf node.
name|LlapCacheableBuffer
name|left
init|=
name|heap
index|[
name|leftIx
index|]
decl_stmt|,
name|right
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|rightIx
operator|<
name|heapSize
condition|)
block|{
name|right
operator|=
name|heap
index|[
name|rightIx
index|]
expr_stmt|;
block|}
name|double
name|leftPri
init|=
name|getHeapifyPriority
argument_list|(
name|left
argument_list|,
name|time
argument_list|)
decl_stmt|,
name|rightPri
init|=
name|getHeapifyPriority
argument_list|(
name|right
argument_list|,
name|time
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparePri
operator|>=
literal|0
operator|&&
name|comparePri
operator|<=
name|leftPri
operator|&&
name|comparePri
operator|<=
name|rightPri
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|leftPri
operator|<=
name|rightPri
condition|)
block|{
comment|// prefer left, cause right might be missing
name|heap
index|[
name|targetPos
index|]
operator|=
name|left
expr_stmt|;
name|left
operator|.
name|indexInHeap
operator|=
name|targetPos
expr_stmt|;
return|return
name|leftIx
return|;
block|}
else|else
block|{
name|heap
index|[
name|targetPos
index|]
operator|=
name|right
expr_stmt|;
name|right
operator|.
name|indexInHeap
operator|=
name|targetPos
expr_stmt|;
return|return
name|rightIx
return|;
block|}
block|}
specifier|private
name|double
name|getHeapifyPriority
parameter_list|(
name|LlapCacheableBuffer
name|buf
parameter_list|,
name|long
name|time
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|==
literal|null
condition|)
return|return
name|Double
operator|.
name|MAX_VALUE
return|;
if|if
condition|(
name|buf
operator|.
name|lastUpdate
operator|!=
name|time
operator|&&
name|time
operator|>=
literal|0
condition|)
block|{
name|buf
operator|.
name|priority
operator|=
name|expirePriority
argument_list|(
name|time
argument_list|,
name|buf
operator|.
name|lastUpdate
argument_list|,
name|buf
operator|.
name|priority
argument_list|)
expr_stmt|;
name|buf
operator|.
name|lastUpdate
operator|=
name|time
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|priority
return|;
block|}
specifier|private
name|void
name|removeFromListAndUnlock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|buffer
operator|.
name|indexInHeap
operator|==
name|LlapCacheableBuffer
operator|.
name|IN_LIST
condition|)
return|return;
name|removeFromListUnderLock
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|indexInHeap
operator|=
name|LlapCacheableBuffer
operator|.
name|NOT_IN_CACHE
expr_stmt|;
block|}
finally|finally
block|{
name|listLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|removeFromListUnderLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|==
name|listTail
condition|)
block|{
name|listTail
operator|=
name|buffer
operator|.
name|prev
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|next
operator|.
name|prev
operator|=
name|buffer
operator|.
name|prev
expr_stmt|;
block|}
if|if
condition|(
name|buffer
operator|==
name|listHead
condition|)
block|{
name|listHead
operator|=
name|buffer
operator|.
name|next
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|prev
operator|.
name|next
operator|=
name|buffer
operator|.
name|next
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|removeFromListUnderLock
parameter_list|(
name|LlapCacheableBuffer
name|from
parameter_list|,
name|LlapCacheableBuffer
name|to
parameter_list|)
block|{
if|if
condition|(
name|to
operator|==
name|listTail
condition|)
block|{
name|listTail
operator|=
name|from
operator|.
name|prev
expr_stmt|;
block|}
else|else
block|{
name|to
operator|.
name|next
operator|.
name|prev
operator|=
name|from
operator|.
name|prev
expr_stmt|;
block|}
if|if
condition|(
name|from
operator|==
name|listHead
condition|)
block|{
name|listHead
operator|=
name|to
operator|.
name|next
expr_stmt|;
block|}
else|else
block|{
name|from
operator|.
name|prev
operator|.
name|next
operator|=
name|to
operator|.
name|next
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|debugDumpHeap
parameter_list|()
block|{
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"List: "
argument_list|)
decl_stmt|;
if|if
condition|(
name|listHead
operator|==
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"<empty>"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LlapCacheableBuffer
name|listItem
init|=
name|listHead
decl_stmt|;
while|while
condition|(
name|listItem
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|listItem
operator|.
name|toStringForCache
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" -> "
argument_list|)
expr_stmt|;
name|listItem
operator|=
name|listItem
operator|.
name|next
expr_stmt|;
block|}
block|}
name|result
operator|.
name|append
argument_list|(
literal|"\nHeap:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|heapSize
operator|==
literal|0
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"<empty>\n"
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|int
name|levels
init|=
literal|32
operator|-
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|heapSize
argument_list|)
decl_stmt|;
name|int
name|ix
init|=
literal|0
decl_stmt|;
name|int
name|spacesCount
init|=
name|heap
index|[
literal|0
index|]
operator|.
name|toStringForCache
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|3
decl_stmt|;
name|String
name|full
init|=
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|" "
argument_list|,
name|spacesCount
argument_list|)
decl_stmt|,
name|half
init|=
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|" "
argument_list|,
name|spacesCount
operator|/
literal|2
argument_list|)
decl_stmt|;
name|int
name|maxWidth
init|=
literal|1
operator|<<
operator|(
name|levels
operator|-
literal|1
operator|)
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
name|levels
condition|;
operator|++
name|i
control|)
block|{
name|int
name|width
init|=
literal|1
operator|<<
name|i
decl_stmt|;
name|int
name|middleGap
init|=
operator|(
name|maxWidth
operator|-
name|width
operator|)
operator|/
name|width
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
operator|(
name|middleGap
operator|>>>
literal|1
operator|)
condition|;
operator|++
name|j
control|)
block|{
name|result
operator|.
name|append
argument_list|(
name|full
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|middleGap
operator|&
literal|1
operator|)
operator|==
literal|1
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|half
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|width
operator|&&
name|ix
operator|<
name|heapSize
condition|;
operator|++
name|j
operator|,
operator|++
name|ix
control|)
block|{
if|if
condition|(
name|j
operator|!=
literal|0
condition|)
block|{
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|middleGap
condition|;
operator|++
name|k
control|)
block|{
name|result
operator|.
name|append
argument_list|(
name|full
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|middleGap
operator|==
literal|0
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
name|j
operator|&
literal|1
operator|)
operator|==
literal|0
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
name|heap
index|[
name|ix
index|]
operator|.
name|toStringForCache
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|j
operator|&
literal|1
operator|)
operator|==
literal|1
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

