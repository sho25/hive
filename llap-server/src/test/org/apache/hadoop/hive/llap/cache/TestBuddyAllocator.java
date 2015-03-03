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
name|Random
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
name|CountDownLatch
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
name|ExecutorService
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
name|Executors
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
name|FutureTask
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestBuddyAllocator
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestBuddyAllocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|(
literal|2284
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|DummyMemoryManager
implements|implements
name|MemoryManager
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|reserveMemory
parameter_list|(
name|long
name|memoryToReserve
parameter_list|,
name|boolean
name|waitForEviction
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVariableSizeAllocs
parameter_list|()
block|{
name|testVariableSizeInternal
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVariableSizeMultiAllocs
parameter_list|()
block|{
name|testVariableSizeInternal
argument_list|(
literal|3
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|testVariableSizeInternal
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSameSizes
parameter_list|()
block|{
name|int
name|min
init|=
literal|3
decl_stmt|,
name|max
init|=
literal|8
decl_stmt|,
name|maxAlloc
init|=
literal|1
operator|<<
name|max
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
literal|1
operator|<<
name|min
argument_list|,
name|maxAlloc
argument_list|,
name|maxAlloc
argument_list|,
name|maxAlloc
argument_list|)
decl_stmt|;
name|BuddyAllocator
name|a
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyMemoryManager
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|max
init|;
name|i
operator|>=
name|min
condition|;
operator|--
name|i
control|)
block|{
name|allocSameSize
argument_list|(
name|a
argument_list|,
literal|1
operator|<<
operator|(
name|max
operator|-
name|i
operator|)
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleArenas
parameter_list|()
block|{
name|int
name|max
init|=
literal|8
decl_stmt|,
name|maxAlloc
init|=
literal|1
operator|<<
name|max
decl_stmt|,
name|allocLog2
init|=
name|max
operator|-
literal|1
decl_stmt|,
name|arenaCount
init|=
literal|5
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
literal|1
operator|<<
literal|3
argument_list|,
name|maxAlloc
argument_list|,
name|maxAlloc
argument_list|,
name|maxAlloc
operator|*
name|arenaCount
argument_list|)
decl_stmt|;
name|BuddyAllocator
name|a
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyMemoryManager
argument_list|()
argument_list|)
decl_stmt|;
name|allocSameSize
argument_list|(
name|a
argument_list|,
name|arenaCount
operator|*
literal|2
argument_list|,
name|allocLog2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT
parameter_list|()
block|{
specifier|final
name|int
name|min
init|=
literal|3
decl_stmt|,
name|max
init|=
literal|8
decl_stmt|,
name|maxAlloc
init|=
literal|1
operator|<<
name|max
decl_stmt|,
name|allocsPerSize
init|=
literal|3
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
literal|1
operator|<<
name|min
argument_list|,
name|maxAlloc
argument_list|,
name|maxAlloc
operator|*
literal|8
argument_list|,
name|maxAlloc
operator|*
literal|24
argument_list|)
decl_stmt|;
specifier|final
name|BuddyAllocator
name|a
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyMemoryManager
argument_list|()
argument_list|)
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|cdlIn
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
decl_stmt|,
name|cdlOut
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|FutureTask
argument_list|<
name|Object
argument_list|>
name|upTask
init|=
operator|new
name|FutureTask
argument_list|<
name|Object
argument_list|>
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|syncThreadStart
argument_list|(
name|cdlIn
argument_list|,
name|cdlOut
argument_list|)
expr_stmt|;
name|allocateUp
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocsPerSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|allocateUp
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocsPerSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
literal|null
argument_list|)
decl_stmt|,
name|downTask
init|=
operator|new
name|FutureTask
argument_list|<
name|Object
argument_list|>
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|syncThreadStart
argument_list|(
name|cdlIn
argument_list|,
name|cdlOut
argument_list|)
expr_stmt|;
name|allocateDown
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocsPerSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|allocateDown
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocsPerSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
literal|null
argument_list|)
decl_stmt|,
name|sameTask
init|=
operator|new
name|FutureTask
argument_list|<
name|Object
argument_list|>
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|syncThreadStart
argument_list|(
name|cdlIn
argument_list|,
name|cdlOut
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|min
init|;
name|i
operator|<=
name|max
condition|;
operator|++
name|i
control|)
block|{
name|allocSameSize
argument_list|(
name|a
argument_list|,
operator|(
literal|1
operator|<<
operator|(
name|max
operator|-
name|i
operator|)
operator|)
operator|*
name|allocsPerSize
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|sameTask
argument_list|)
expr_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|upTask
argument_list|)
expr_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|downTask
argument_list|)
expr_stmt|;
try|try
block|{
name|cdlIn
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Wait for all threads to be ready.
name|cdlOut
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// Release them at the same time.
name|upTask
operator|.
name|get
argument_list|()
expr_stmt|;
name|downTask
operator|.
name|get
argument_list|()
expr_stmt|;
name|sameTask
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|syncThreadStart
parameter_list|(
specifier|final
name|CountDownLatch
name|cdlIn
parameter_list|,
specifier|final
name|CountDownLatch
name|cdlOut
parameter_list|)
block|{
name|cdlIn
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|cdlOut
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|testVariableSizeInternal
parameter_list|(
name|int
name|allocCount
parameter_list|,
name|int
name|arenaSizeMult
parameter_list|,
name|int
name|arenaCount
parameter_list|)
block|{
name|int
name|min
init|=
literal|3
decl_stmt|,
name|max
init|=
literal|8
decl_stmt|,
name|maxAlloc
init|=
literal|1
operator|<<
name|max
decl_stmt|,
name|arenaSize
init|=
name|maxAlloc
operator|*
name|arenaSizeMult
decl_stmt|;
name|Configuration
name|conf
init|=
name|createConf
argument_list|(
literal|1
operator|<<
name|min
argument_list|,
name|maxAlloc
argument_list|,
name|arenaSize
argument_list|,
name|arenaSize
operator|*
name|arenaCount
argument_list|)
decl_stmt|;
name|BuddyAllocator
name|a
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyMemoryManager
argument_list|()
argument_list|)
decl_stmt|;
name|allocateUp
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|allocateDown
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|allocateDown
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|allocateUp
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|allocateUp
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|allocateDown
argument_list|(
name|a
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|allocCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|allocSameSize
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|int
name|allocCount
parameter_list|,
name|int
name|sizeLog2
parameter_list|)
block|{
name|LlapMemoryBuffer
index|[]
index|[]
name|allocs
init|=
operator|new
name|LlapMemoryBuffer
index|[
name|allocCount
index|]
index|[]
decl_stmt|;
name|long
index|[]
index|[]
name|testValues
init|=
operator|new
name|long
index|[
name|allocCount
index|]
index|[]
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
name|allocCount
condition|;
operator|++
name|j
control|)
block|{
name|allocateAndUseBuffer
argument_list|(
name|a
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|,
literal|1
argument_list|,
name|j
argument_list|,
name|sizeLog2
argument_list|)
expr_stmt|;
block|}
name|deallocUpOrDown
argument_list|(
name|a
argument_list|,
literal|false
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|allocateUp
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|int
name|allocPerSize
parameter_list|,
name|boolean
name|isSameOrderDealloc
parameter_list|)
block|{
name|int
name|sizes
init|=
name|max
operator|-
name|min
operator|+
literal|1
decl_stmt|;
name|LlapMemoryBuffer
index|[]
index|[]
name|allocs
init|=
operator|new
name|LlapMemoryBuffer
index|[
name|sizes
index|]
index|[]
decl_stmt|;
comment|// Put in the beginning; relies on the knowledge of internal implementation. Pave?
name|long
index|[]
index|[]
name|testValues
init|=
operator|new
name|long
index|[
name|sizes
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|min
init|;
name|i
operator|<=
name|max
condition|;
operator|++
name|i
control|)
block|{
name|allocateAndUseBuffer
argument_list|(
name|a
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|,
name|allocPerSize
argument_list|,
name|i
operator|-
name|min
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|deallocUpOrDown
argument_list|(
name|a
argument_list|,
name|isSameOrderDealloc
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|allocateDown
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|int
name|allocPerSize
parameter_list|,
name|boolean
name|isSameOrderDealloc
parameter_list|)
block|{
name|int
name|sizes
init|=
name|max
operator|-
name|min
operator|+
literal|1
decl_stmt|;
name|LlapMemoryBuffer
index|[]
index|[]
name|allocs
init|=
operator|new
name|LlapMemoryBuffer
index|[
name|sizes
index|]
index|[]
decl_stmt|;
comment|// Put in the beginning; relies on the knowledge of internal implementation. Pave?
name|long
index|[]
index|[]
name|testValues
init|=
operator|new
name|long
index|[
name|sizes
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|max
init|;
name|i
operator|>=
name|min
condition|;
operator|--
name|i
control|)
block|{
name|allocateAndUseBuffer
argument_list|(
name|a
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|,
name|allocPerSize
argument_list|,
name|i
operator|-
name|min
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|deallocUpOrDown
argument_list|(
name|a
argument_list|,
name|isSameOrderDealloc
argument_list|,
name|allocs
argument_list|,
name|testValues
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|allocateAndUseBuffer
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|LlapMemoryBuffer
index|[]
index|[]
name|allocs
parameter_list|,
name|long
index|[]
index|[]
name|testValues
parameter_list|,
name|int
name|allocCount
parameter_list|,
name|int
name|index
parameter_list|,
name|int
name|sizeLog2
parameter_list|)
block|{
name|allocs
index|[
name|index
index|]
operator|=
operator|new
name|LlapMemoryBuffer
index|[
name|allocCount
index|]
expr_stmt|;
name|testValues
index|[
name|index
index|]
operator|=
operator|new
name|long
index|[
name|allocCount
index|]
expr_stmt|;
name|int
name|size
init|=
operator|(
literal|1
operator|<<
name|sizeLog2
operator|)
operator|-
literal|1
decl_stmt|;
if|if
condition|(
operator|!
name|a
operator|.
name|allocateMultiple
argument_list|(
name|allocs
index|[
name|index
index|]
argument_list|,
name|size
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to allocate "
operator|+
name|allocCount
operator|+
literal|" of "
operator|+
name|size
operator|+
literal|"; "
operator|+
name|a
operator|.
name|debugDump
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
comment|// LOG.info("Allocated " + allocCount + " of " + size + "; " + a.debugDump());
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|allocCount
condition|;
operator|++
name|j
control|)
block|{
name|LlapMemoryBuffer
name|mem
init|=
name|allocs
index|[
name|index
index|]
index|[
name|j
index|]
decl_stmt|;
name|long
name|testValue
init|=
name|testValues
index|[
name|index
index|]
index|[
name|j
index|]
operator|=
name|rdm
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|mem
operator|.
name|byteBuffer
operator|.
name|position
argument_list|()
decl_stmt|;
name|mem
operator|.
name|byteBuffer
operator|.
name|putLong
argument_list|(
name|pos
argument_list|,
name|testValue
argument_list|)
expr_stmt|;
name|int
name|halfLength
init|=
name|mem
operator|.
name|byteBuffer
operator|.
name|remaining
argument_list|()
operator|>>
literal|1
decl_stmt|;
if|if
condition|(
name|halfLength
operator|+
literal|8
operator|<=
name|mem
operator|.
name|byteBuffer
operator|.
name|remaining
argument_list|()
condition|)
block|{
name|mem
operator|.
name|byteBuffer
operator|.
name|putLong
argument_list|(
name|pos
operator|+
name|halfLength
argument_list|,
name|testValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|deallocUpOrDown
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|boolean
name|isSameOrderDealloc
parameter_list|,
name|LlapMemoryBuffer
index|[]
index|[]
name|allocs
parameter_list|,
name|long
index|[]
index|[]
name|testValues
parameter_list|)
block|{
if|if
condition|(
name|isSameOrderDealloc
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|allocs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|deallocBuffers
argument_list|(
name|a
argument_list|,
name|allocs
index|[
name|i
index|]
argument_list|,
name|testValues
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
name|allocs
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|deallocBuffers
argument_list|(
name|a
argument_list|,
name|allocs
index|[
name|i
index|]
argument_list|,
name|testValues
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|deallocBuffers
parameter_list|(
name|BuddyAllocator
name|a
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|allocs
parameter_list|,
name|long
index|[]
name|testValues
parameter_list|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|allocs
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|LlapCacheableBuffer
name|mem
init|=
operator|(
name|LlapCacheableBuffer
operator|)
name|allocs
index|[
name|j
index|]
decl_stmt|;
name|int
name|pos
init|=
name|mem
operator|.
name|byteBuffer
operator|.
name|position
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to match ("
operator|+
name|pos
operator|+
literal|") on "
operator|+
name|j
operator|+
literal|"/"
operator|+
name|allocs
operator|.
name|length
argument_list|,
name|testValues
index|[
name|j
index|]
argument_list|,
name|mem
operator|.
name|byteBuffer
operator|.
name|getLong
argument_list|(
name|pos
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|halfLength
init|=
name|mem
operator|.
name|byteBuffer
operator|.
name|remaining
argument_list|()
operator|>>
literal|1
decl_stmt|;
if|if
condition|(
name|halfLength
operator|+
literal|8
operator|<=
name|mem
operator|.
name|byteBuffer
operator|.
name|remaining
argument_list|()
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Failed to match half ("
operator|+
operator|(
name|pos
operator|+
name|halfLength
operator|)
operator|+
literal|") on "
operator|+
name|j
operator|+
literal|"/"
operator|+
name|allocs
operator|.
name|length
argument_list|,
name|testValues
index|[
name|j
index|]
argument_list|,
name|mem
operator|.
name|byteBuffer
operator|.
name|getLong
argument_list|(
name|pos
operator|+
name|halfLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|a
operator|.
name|deallocate
argument_list|(
name|mem
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Configuration
name|createConf
parameter_list|(
name|int
name|min
parameter_list|,
name|int
name|max
parameter_list|,
name|int
name|arena
parameter_list|,
name|int
name|total
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MIN_ALLOC
operator|.
name|varname
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MAX_ALLOC
operator|.
name|varname
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_ARENA_SIZE
operator|.
name|varname
argument_list|,
name|arena
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MAX_SIZE
operator|.
name|varname
argument_list|,
name|total
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

