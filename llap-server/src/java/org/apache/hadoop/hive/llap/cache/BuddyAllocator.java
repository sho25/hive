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

begin_class
specifier|public
specifier|final
class|class
name|BuddyAllocator
implements|implements
name|Allocator
block|{
specifier|private
specifier|final
name|Arena
index|[]
name|arenas
decl_stmt|;
specifier|private
name|AtomicInteger
name|allocatedArenas
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MemoryManager
name|memoryManager
decl_stmt|;
comment|// Config settings
specifier|private
specifier|final
name|int
name|minAllocLog2
decl_stmt|,
name|maxAllocLog2
decl_stmt|,
name|arenaSizeLog2
decl_stmt|,
name|maxArenas
decl_stmt|;
specifier|private
specifier|final
name|int
name|minAllocation
decl_stmt|,
name|maxAllocation
decl_stmt|,
name|arenaSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxSize
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isDirect
decl_stmt|;
specifier|public
name|BuddyAllocator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|MemoryManager
name|memoryManager
parameter_list|)
block|{
name|isDirect
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_ALLOCATE_DIRECT
argument_list|)
expr_stmt|;
name|minAllocation
operator|=
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
expr_stmt|;
name|maxAllocation
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MAX_ALLOC
argument_list|)
expr_stmt|;
name|arenaSize
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_ARENA_SIZE
argument_list|)
expr_stmt|;
name|maxSize
operator|=
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
expr_stmt|;
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
literal|"Buddy allocator with "
operator|+
operator|(
name|isDirect
condition|?
literal|"direct"
else|:
literal|"byte"
operator|)
operator|+
literal|" buffers; allocation sizes "
operator|+
name|minAllocation
operator|+
literal|" - "
operator|+
name|maxAllocation
operator|+
literal|", arena size "
operator|+
name|arenaSize
operator|+
literal|". total size "
operator|+
name|maxSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minAllocation
operator|<
literal|8
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Min allocation must be at least 8: "
operator|+
name|minAllocation
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxSize
operator|<
name|arenaSize
operator|||
name|arenaSize
operator|<
name|maxAllocation
operator|||
name|maxAllocation
operator|<
name|minAllocation
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Inconsistent sizes of cache, arena and allocations: "
operator|+
name|minAllocation
operator|+
literal|", "
operator|+
name|maxAllocation
operator|+
literal|", "
operator|+
name|arenaSize
operator|+
literal|", "
operator|+
name|maxSize
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|Integer
operator|.
name|bitCount
argument_list|(
name|minAllocation
argument_list|)
operator|!=
literal|1
operator|)
operator|||
operator|(
name|Integer
operator|.
name|bitCount
argument_list|(
name|maxAllocation
argument_list|)
operator|!=
literal|1
operator|)
operator|||
operator|(
name|Long
operator|.
name|bitCount
argument_list|(
name|arenaSize
argument_list|)
operator|!=
literal|1
operator|)
condition|)
block|{
comment|// Technically, arena size only needs to be divisible by maxAlloc
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Allocation and arena sizes must be powers of two: "
operator|+
name|minAllocation
operator|+
literal|", "
operator|+
name|maxAllocation
operator|+
literal|", "
operator|+
name|arenaSize
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|maxSize
operator|%
name|arenaSize
operator|)
operator|>
literal|0
operator|||
operator|(
name|maxSize
operator|/
name|arenaSize
operator|)
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cache size not consistent with arena size: "
operator|+
name|arenaSize
operator|+
literal|","
operator|+
name|maxSize
argument_list|)
throw|;
block|}
name|minAllocLog2
operator|=
literal|31
operator|-
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|minAllocation
argument_list|)
expr_stmt|;
name|maxAllocLog2
operator|=
literal|31
operator|-
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|maxAllocation
argument_list|)
expr_stmt|;
name|arenaSizeLog2
operator|=
literal|63
operator|-
name|Long
operator|.
name|numberOfLeadingZeros
argument_list|(
name|arenaSize
argument_list|)
expr_stmt|;
name|maxArenas
operator|=
call|(
name|int
call|)
argument_list|(
name|maxSize
operator|/
name|arenaSize
argument_list|)
expr_stmt|;
name|arenas
operator|=
operator|new
name|Arena
index|[
name|maxArenas
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxArenas
condition|;
operator|++
name|i
control|)
block|{
name|arenas
index|[
name|i
index|]
operator|=
operator|new
name|Arena
argument_list|()
expr_stmt|;
block|}
name|arenas
index|[
literal|0
index|]
operator|.
name|init
argument_list|()
expr_stmt|;
name|allocatedArenas
operator|.
name|set
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|memoryManager
operator|=
name|memoryManager
expr_stmt|;
block|}
comment|// TODO: would it make sense to return buffers asynchronously?
annotation|@
name|Override
specifier|public
name|boolean
name|allocateMultiple
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|)
block|{
assert|assert
name|size
operator|>
literal|0
operator|:
literal|"size is "
operator|+
name|size
assert|;
if|if
condition|(
name|size
operator|>
name|maxAllocation
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Trying to allocate "
operator|+
name|size
operator|+
literal|"; max is "
operator|+
name|maxAllocation
argument_list|)
throw|;
block|}
name|int
name|freeListIx
init|=
literal|31
operator|-
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|!=
operator|(
literal|1
operator|<<
name|freeListIx
operator|)
condition|)
operator|++
name|freeListIx
expr_stmt|;
comment|// not a power of two, add one more
name|freeListIx
operator|=
name|Math
operator|.
name|max
argument_list|(
name|freeListIx
operator|-
name|minAllocLog2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|int
name|allocLog2
init|=
name|freeListIx
operator|+
name|minAllocLog2
decl_stmt|;
name|int
name|allocationSize
init|=
literal|1
operator|<<
name|allocLog2
decl_stmt|;
comment|// TODO: reserving the entire thing is not ideal before we alloc anything. Interleave?
name|memoryManager
operator|.
name|reserveMemory
argument_list|(
name|dest
operator|.
name|length
operator|<<
name|allocLog2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|int
name|ix
init|=
literal|0
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
name|dest
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|dest
index|[
name|i
index|]
operator|!=
literal|null
condition|)
continue|continue;
name|dest
index|[
name|i
index|]
operator|=
operator|new
name|LlapCacheableBuffer
argument_list|()
expr_stmt|;
comment|// TODO: pool of objects?
block|}
comment|// First try to quickly lock some of the correct-sized free lists and allocate from them.
name|int
name|arenaCount
init|=
name|allocatedArenas
operator|.
name|get
argument_list|()
decl_stmt|;
name|long
name|threadId
init|=
name|arenaCount
operator|>
literal|1
condition|?
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
else|:
literal|0
decl_stmt|;
block|{
name|int
name|startIndex
init|=
call|(
name|int
call|)
argument_list|(
name|threadId
operator|%
name|arenaCount
argument_list|)
decl_stmt|,
name|index
init|=
name|startIndex
decl_stmt|;
do|do
block|{
name|int
name|newIx
init|=
name|arenas
index|[
name|index
index|]
operator|.
name|allocateFast
argument_list|(
name|index
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|allocationSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|newIx
operator|==
name|dest
operator|.
name|length
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|newIx
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// Shouldn't happen.
name|ix
operator|=
name|newIx
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|++
name|index
operator|)
operator|==
name|arenaCount
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
block|}
block|}
do|while
condition|(
name|index
operator|!=
name|startIndex
condition|)
do|;
block|}
comment|// Then try to split bigger blocks. TODO: again, ideally we would tryLock at least once
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arenaCount
condition|;
operator|++
name|i
control|)
block|{
name|int
name|newIx
init|=
name|arenas
index|[
name|i
index|]
operator|.
name|allocateWithSplit
argument_list|(
name|i
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|allocationSize
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
comment|// Shouldn't happen.
if|if
condition|(
name|newIx
operator|==
name|dest
operator|.
name|length
condition|)
return|return
literal|true
return|;
name|ix
operator|=
name|newIx
expr_stmt|;
block|}
comment|// Then try to allocate memory if we haven't allocated all the way to maxSize yet; very rare.
for|for
control|(
name|int
name|i
init|=
name|arenaCount
init|;
name|i
operator|<
name|arenas
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|ix
operator|=
name|arenas
index|[
name|i
index|]
operator|.
name|allocateWithExpand
argument_list|(
name|i
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|allocationSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|ix
operator|==
name|dest
operator|.
name|length
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deallocate
parameter_list|(
name|LlapMemoryBuffer
name|buffer
parameter_list|)
block|{
name|LlapCacheableBuffer
name|buf
init|=
operator|(
name|LlapCacheableBuffer
operator|)
name|buffer
decl_stmt|;
name|arenas
index|[
name|buf
operator|.
name|arenaIndex
index|]
operator|.
name|deallocate
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDirectAlloc
parameter_list|()
block|{
return|return
name|isDirect
return|;
block|}
specifier|public
name|String
name|debugDump
parameter_list|()
block|{
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"NOTE: with multiple threads the dump is not guaranteed to be consistent"
argument_list|)
decl_stmt|;
for|for
control|(
name|Arena
name|arena
range|:
name|arenas
control|)
block|{
name|arena
operator|.
name|debugDump
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
class|class
name|Arena
block|{
specifier|private
name|ByteBuffer
name|data
decl_stmt|;
comment|// Avoid storing headers with data since we expect binary size allocations.
comment|// Each headers[i] is a "virtual" byte at i * minAllocation.
specifier|private
name|byte
index|[]
name|headers
decl_stmt|;
specifier|private
name|FreeList
index|[]
name|freeLists
decl_stmt|;
name|void
name|init
parameter_list|()
block|{
name|data
operator|=
name|isDirect
condition|?
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|arenaSize
argument_list|)
else|:
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|arenaSize
argument_list|)
expr_stmt|;
name|int
name|maxMinAllocs
init|=
literal|1
operator|<<
operator|(
name|arenaSizeLog2
operator|-
name|minAllocLog2
operator|)
decl_stmt|;
name|headers
operator|=
operator|new
name|byte
index|[
name|maxMinAllocs
index|]
expr_stmt|;
name|int
name|allocLog2Diff
init|=
name|maxAllocLog2
operator|-
name|minAllocLog2
decl_stmt|,
name|freeListCount
init|=
name|allocLog2Diff
operator|+
literal|1
decl_stmt|;
name|freeLists
operator|=
operator|new
name|FreeList
index|[
name|freeListCount
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|freeListCount
condition|;
operator|++
name|i
control|)
block|{
name|freeLists
index|[
name|i
index|]
operator|=
operator|new
name|FreeList
argument_list|()
expr_stmt|;
block|}
name|int
name|maxMaxAllocs
init|=
literal|1
operator|<<
operator|(
name|arenaSizeLog2
operator|-
name|maxAllocLog2
operator|)
decl_stmt|,
name|headerIndex
init|=
literal|0
decl_stmt|,
name|headerStep
init|=
literal|1
operator|<<
name|allocLog2Diff
decl_stmt|;
name|freeLists
index|[
name|allocLog2Diff
index|]
operator|.
name|listHead
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|offset
init|=
literal|0
init|;
name|i
operator|<
name|maxMaxAllocs
condition|;
operator|++
name|i
operator|,
name|offset
operator|+=
name|maxAllocation
control|)
block|{
comment|// TODO: will this cause bugs on large numbers due to some Java sign bit stupidity?
name|headers
index|[
name|headerIndex
index|]
operator|=
name|makeHeader
argument_list|(
name|allocLog2Diff
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|data
operator|.
name|putInt
argument_list|(
name|offset
argument_list|,
operator|(
name|i
operator|==
literal|0
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
name|headerIndex
operator|-
name|headerStep
operator|)
argument_list|)
expr_stmt|;
name|data
operator|.
name|putInt
argument_list|(
name|offset
operator|+
literal|4
argument_list|,
operator|(
name|i
operator|==
name|maxMaxAllocs
operator|-
literal|1
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
name|headerIndex
operator|+
name|headerStep
operator|)
argument_list|)
expr_stmt|;
name|headerIndex
operator|+=
name|headerStep
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|debugDump
parameter_list|(
name|StringBuilder
name|result
parameter_list|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"\nArena: "
argument_list|)
expr_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|" not allocated"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Try to get as consistent view as we can; make copy of the headers.
name|byte
index|[]
name|headers
init|=
operator|new
name|byte
index|[
name|this
operator|.
name|headers
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|headers
argument_list|,
literal|0
argument_list|,
name|headers
argument_list|,
literal|0
argument_list|,
name|headers
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|headers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|byte
name|header
init|=
name|headers
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|header
operator|==
literal|0
condition|)
continue|continue;
name|int
name|freeListIx
init|=
operator|(
name|header
operator|>>
literal|1
operator|)
operator|-
literal|1
decl_stmt|,
name|offset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|boolean
name|isFree
init|=
operator|(
name|header
operator|&
literal|1
operator|)
operator|==
literal|0
decl_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|"\n  block "
operator|+
name|i
operator|+
literal|" at "
operator|+
name|offset
operator|+
literal|": size "
operator|+
operator|(
literal|1
operator|<<
operator|(
name|freeListIx
operator|+
name|minAllocLog2
operator|)
operator|)
operator|+
literal|", "
operator|+
operator|(
name|isFree
condition|?
literal|"free"
else|:
literal|"allocated"
operator|)
argument_list|)
expr_stmt|;
block|}
name|int
name|allocSize
init|=
name|minAllocation
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
name|freeLists
operator|.
name|length
condition|;
operator|++
name|i
operator|,
name|allocSize
operator|<<=
literal|1
control|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"\n  free list for size "
operator|+
name|allocSize
operator|+
literal|": "
argument_list|)
expr_stmt|;
name|FreeList
name|freeList
init|=
name|freeLists
index|[
name|i
index|]
decl_stmt|;
name|freeList
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|nextHeaderIx
init|=
name|freeList
operator|.
name|listHead
decl_stmt|;
while|while
condition|(
name|nextHeaderIx
operator|>=
literal|0
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|nextHeaderIx
operator|+
literal|", "
argument_list|)
expr_stmt|;
name|nextHeaderIx
operator|=
name|data
operator|.
name|getInt
argument_list|(
name|offsetFromHeaderIndex
argument_list|(
name|nextHeaderIx
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|freeList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|int
name|allocateFast
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|int
name|freeListIx
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|ix
parameter_list|,
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
comment|// not allocated yet
name|FreeList
name|freeList
init|=
name|freeLists
index|[
name|freeListIx
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|freeList
operator|.
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
return|return
name|ix
return|;
try|try
block|{
return|return
name|allocateFromFreeListUnderLock
argument_list|(
name|arenaIx
argument_list|,
name|freeList
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|size
argument_list|)
return|;
block|}
finally|finally
block|{
name|freeList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|allocateWithSplit
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|int
name|freeListIx
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|ix
parameter_list|,
name|int
name|allocationSize
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
comment|// not allocated yet
name|FreeList
name|freeList
init|=
name|freeLists
index|[
name|freeListIx
index|]
decl_stmt|;
name|int
name|remaining
init|=
operator|-
literal|1
decl_stmt|;
name|freeList
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|ix
operator|=
name|allocateFromFreeListUnderLock
argument_list|(
name|arenaIx
argument_list|,
name|freeList
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|allocationSize
argument_list|)
expr_stmt|;
name|remaining
operator|=
name|dest
operator|.
name|length
operator|-
name|ix
expr_stmt|;
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
return|return
name|ix
return|;
block|}
finally|finally
block|{
name|freeList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|byte
name|headerData
init|=
name|makeHeader
argument_list|(
name|freeListIx
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|int
name|headerStep
init|=
literal|1
operator|<<
name|freeListIx
decl_stmt|;
name|int
name|splitListIx
init|=
name|freeListIx
operator|+
literal|1
decl_stmt|;
while|while
condition|(
name|remaining
operator|>
literal|0
operator|&&
name|splitListIx
operator|<
name|freeLists
operator|.
name|length
condition|)
block|{
name|int
name|splitWays
init|=
literal|1
operator|<<
operator|(
name|splitListIx
operator|-
name|freeListIx
operator|)
decl_stmt|;
name|int
name|lastSplitBlocksRemaining
init|=
operator|-
literal|1
decl_stmt|,
name|lastSplitNextHeader
init|=
operator|-
literal|1
decl_stmt|;
name|FreeList
name|splitList
init|=
name|freeLists
index|[
name|splitListIx
index|]
decl_stmt|;
name|splitList
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|headerIx
init|=
name|splitList
operator|.
name|listHead
decl_stmt|;
while|while
condition|(
name|headerIx
operator|>=
literal|0
operator|&&
name|remaining
operator|>
literal|0
condition|)
block|{
name|int
name|origOffset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|headerIx
argument_list|)
decl_stmt|,
name|offset
init|=
name|origOffset
decl_stmt|;
name|int
name|toTake
init|=
name|Math
operator|.
name|min
argument_list|(
name|splitWays
argument_list|,
name|remaining
argument_list|)
decl_stmt|;
name|remaining
operator|-=
name|toTake
expr_stmt|;
name|lastSplitBlocksRemaining
operator|=
name|splitWays
operator|-
name|toTake
expr_stmt|;
for|for
control|(
init|;
name|toTake
operator|>
literal|0
condition|;
operator|++
name|ix
operator|,
operator|--
name|toTake
operator|,
name|headerIx
operator|+=
name|headerStep
operator|,
name|offset
operator|+=
name|allocationSize
control|)
block|{
name|headers
index|[
name|headerIx
index|]
operator|=
name|headerData
expr_stmt|;
operator|(
operator|(
name|LlapCacheableBuffer
operator|)
name|dest
index|[
name|ix
index|]
operator|)
operator|.
name|initialize
argument_list|(
name|arenaIx
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|allocationSize
argument_list|)
expr_stmt|;
block|}
name|lastSplitNextHeader
operator|=
name|headerIx
expr_stmt|;
name|headerIx
operator|=
name|data
operator|.
name|getInt
argument_list|(
name|origOffset
operator|+
literal|4
argument_list|)
expr_stmt|;
block|}
name|replaceListHeadUnderLock
argument_list|(
name|splitList
argument_list|,
name|headerIx
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|splitList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
comment|// We have just obtained all we needed by splitting at lastSplitBlockOffset; now
comment|// we need to put the space remaining from that block into lower free lists.
name|int
name|newListIndex
init|=
name|freeListIx
decl_stmt|;
while|while
condition|(
name|lastSplitBlocksRemaining
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|(
name|lastSplitBlocksRemaining
operator|&
literal|1
operator|)
operator|==
literal|1
condition|)
block|{
name|FreeList
name|newFreeList
init|=
name|freeLists
index|[
name|newListIndex
index|]
decl_stmt|;
name|newFreeList
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|headers
index|[
name|lastSplitNextHeader
index|]
operator|=
name|makeHeader
argument_list|(
name|newListIndex
argument_list|,
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|addBlockToFreeListUnderLock
argument_list|(
name|newFreeList
argument_list|,
name|lastSplitNextHeader
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|newFreeList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|lastSplitNextHeader
operator|+=
operator|(
literal|1
operator|<<
name|newListIndex
operator|)
expr_stmt|;
block|}
name|lastSplitBlocksRemaining
operator|>>>=
literal|1
expr_stmt|;
operator|++
name|newListIndex
expr_stmt|;
continue|continue;
block|}
block|}
operator|++
name|splitListIx
expr_stmt|;
block|}
return|return
name|ix
return|;
block|}
specifier|private
name|void
name|replaceListHeadUnderLock
parameter_list|(
name|FreeList
name|freeList
parameter_list|,
name|int
name|headerIx
parameter_list|)
block|{
if|if
condition|(
name|headerIx
operator|==
name|freeList
operator|.
name|listHead
condition|)
return|return;
if|if
condition|(
name|headerIx
operator|>=
literal|0
condition|)
block|{
name|int
name|newHeadOffset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|headerIx
argument_list|)
decl_stmt|;
name|data
operator|.
name|putInt
argument_list|(
name|newHeadOffset
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// Remove backlink.
block|}
name|freeList
operator|.
name|listHead
operator|=
name|headerIx
expr_stmt|;
block|}
specifier|private
name|int
name|allocateWithExpand
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|int
name|freeListIx
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|ix
parameter_list|,
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// Never goes from non-null to null, so this is the only place we need sync.
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|init
argument_list|()
expr_stmt|;
name|allocatedArenas
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|allocateWithSplit
argument_list|(
name|arenaIx
argument_list|,
name|freeListIx
argument_list|,
name|dest
argument_list|,
name|ix
argument_list|,
name|size
argument_list|)
return|;
block|}
specifier|public
name|int
name|offsetFromHeaderIndex
parameter_list|(
name|int
name|lastSplitNextHeader
parameter_list|)
block|{
return|return
name|lastSplitNextHeader
operator|<<
name|minAllocLog2
return|;
block|}
specifier|public
name|int
name|allocateFromFreeListUnderLock
parameter_list|(
name|int
name|arenaIx
parameter_list|,
name|FreeList
name|freeList
parameter_list|,
name|int
name|freeListIx
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|ix
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|int
name|current
init|=
name|freeList
operator|.
name|listHead
decl_stmt|;
while|while
condition|(
name|current
operator|>=
literal|0
operator|&&
name|ix
operator|<
name|dest
operator|.
name|length
condition|)
block|{
name|int
name|offset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|current
argument_list|)
decl_stmt|;
comment|// Noone else has this either allocated or in a different free list; no sync needed.
name|headers
index|[
name|current
index|]
operator|=
name|makeHeader
argument_list|(
name|freeListIx
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|current
operator|=
name|data
operator|.
name|getInt
argument_list|(
name|offset
operator|+
literal|4
argument_list|)
expr_stmt|;
operator|(
operator|(
name|LlapCacheableBuffer
operator|)
name|dest
index|[
name|ix
index|]
operator|)
operator|.
name|initialize
argument_list|(
name|arenaIx
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|size
argument_list|)
expr_stmt|;
operator|++
name|ix
expr_stmt|;
block|}
name|replaceListHeadUnderLock
argument_list|(
name|freeList
argument_list|,
name|current
argument_list|)
expr_stmt|;
return|return
name|ix
return|;
block|}
specifier|private
name|byte
name|makeHeader
parameter_list|(
name|int
name|freeListIx
parameter_list|,
name|boolean
name|isInUse
parameter_list|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
operator|(
operator|(
name|freeListIx
operator|+
literal|1
operator|)
operator|<<
literal|1
operator|)
operator||
operator|(
name|isInUse
condition|?
literal|1
else|:
literal|0
operator|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|deallocate
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
assert|assert
name|data
operator|!=
literal|null
assert|;
name|int
name|freeListIx
init|=
literal|31
operator|-
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|buffer
operator|.
name|byteBuffer
operator|.
name|remaining
argument_list|()
argument_list|)
operator|-
name|minAllocLog2
decl_stmt|,
name|headerIx
init|=
name|buffer
operator|.
name|byteBuffer
operator|.
name|position
argument_list|()
operator|>>>
name|minAllocLog2
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|FreeList
name|freeList
init|=
name|freeLists
index|[
name|freeListIx
index|]
decl_stmt|;
name|int
name|bHeaderIx
init|=
name|headerIx
operator|^
operator|(
literal|1
operator|<<
name|freeListIx
operator|)
decl_stmt|;
name|freeList
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|freeListIx
operator|==
name|freeLists
operator|.
name|length
operator|-
literal|1
operator|)
operator|||
name|headers
index|[
name|bHeaderIx
index|]
operator|!=
name|makeHeader
argument_list|(
name|freeListIx
argument_list|,
literal|false
argument_list|)
condition|)
block|{
comment|// Buddy block is allocated, or it is on higher level of allocation than we are, or we
comment|// have reached the top level. Add whatever we have got to the current free list.
name|addBlockToFreeListUnderLock
argument_list|(
name|freeList
argument_list|,
name|headerIx
argument_list|)
expr_stmt|;
name|headers
index|[
name|headerIx
index|]
operator|=
name|makeHeader
argument_list|(
name|freeListIx
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
block|}
comment|// Buddy block is free and in the same free list we have locked. Take it out for merge.
name|removeBlockFromFreeList
argument_list|(
name|freeList
argument_list|,
name|bHeaderIx
argument_list|)
expr_stmt|;
name|headers
index|[
name|bHeaderIx
index|]
operator|=
name|headers
index|[
name|headerIx
index|]
operator|=
literal|0
expr_stmt|;
comment|// Erase both headers of the blocks to merge.
block|}
finally|finally
block|{
name|freeList
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
operator|++
name|freeListIx
expr_stmt|;
name|headerIx
operator|=
name|Math
operator|.
name|min
argument_list|(
name|headerIx
argument_list|,
name|bHeaderIx
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addBlockToFreeListUnderLock
parameter_list|(
name|FreeList
name|freeList
parameter_list|,
name|int
name|headerIx
parameter_list|)
block|{
if|if
condition|(
name|freeList
operator|.
name|listHead
operator|>=
literal|0
condition|)
block|{
name|int
name|oldHeadOffset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|freeList
operator|.
name|listHead
argument_list|)
decl_stmt|;
assert|assert
name|data
operator|.
name|getInt
argument_list|(
name|oldHeadOffset
argument_list|)
operator|==
operator|-
literal|1
assert|;
name|data
operator|.
name|putInt
argument_list|(
name|oldHeadOffset
argument_list|,
name|headerIx
argument_list|)
expr_stmt|;
block|}
name|int
name|offset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|headerIx
argument_list|)
decl_stmt|;
name|data
operator|.
name|putInt
argument_list|(
name|offset
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|data
operator|.
name|putInt
argument_list|(
name|offset
operator|+
literal|4
argument_list|,
name|freeList
operator|.
name|listHead
argument_list|)
expr_stmt|;
name|freeList
operator|.
name|listHead
operator|=
name|headerIx
expr_stmt|;
block|}
specifier|private
name|void
name|removeBlockFromFreeList
parameter_list|(
name|FreeList
name|freeList
parameter_list|,
name|int
name|headerIx
parameter_list|)
block|{
name|int
name|bOffset
init|=
name|offsetFromHeaderIndex
argument_list|(
name|headerIx
argument_list|)
decl_stmt|,
name|bpHeaderIx
init|=
name|data
operator|.
name|getInt
argument_list|(
name|bOffset
argument_list|)
decl_stmt|,
name|bnHeaderIx
init|=
name|data
operator|.
name|getInt
argument_list|(
name|bOffset
operator|+
literal|4
argument_list|)
decl_stmt|;
if|if
condition|(
name|freeList
operator|.
name|listHead
operator|==
name|headerIx
condition|)
block|{
assert|assert
name|bpHeaderIx
operator|==
operator|-
literal|1
assert|;
name|freeList
operator|.
name|listHead
operator|=
name|bnHeaderIx
expr_stmt|;
block|}
if|if
condition|(
name|bpHeaderIx
operator|!=
operator|-
literal|1
condition|)
block|{
name|data
operator|.
name|putInt
argument_list|(
name|offsetFromHeaderIndex
argument_list|(
name|bpHeaderIx
argument_list|)
operator|+
literal|4
argument_list|,
name|bnHeaderIx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bnHeaderIx
operator|!=
operator|-
literal|1
condition|)
block|{
name|data
operator|.
name|putInt
argument_list|(
name|offsetFromHeaderIndex
argument_list|(
name|bnHeaderIx
argument_list|)
argument_list|,
name|bpHeaderIx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|FreeList
block|{
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|int
name|listHead
init|=
operator|-
literal|1
decl_stmt|;
comment|// Index of where the buffer is; in minAllocation units
comment|// TODO: One possible improvement - store blocks arriving left over from splits, and
comment|//       blocks requested, to be able to wait for pending splits and reduce fragmentation.
comment|//       However, we are trying to increase fragmentation now, since we cater to single-size.
block|}
block|}
end_class

end_unit

