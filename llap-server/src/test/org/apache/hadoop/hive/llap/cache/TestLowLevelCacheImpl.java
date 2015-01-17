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
name|Callable
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
name|Executor
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
name|javax
operator|.
name|management
operator|.
name|RuntimeErrorException
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
name|Assume
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
name|TestLowLevelCacheImpl
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
name|TestLowLevelCacheImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|DummyAllocator
implements|implements
name|Allocator
block|{
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
name|LlapCacheableBuffer
name|buf
init|=
operator|new
name|LlapCacheableBuffer
argument_list|()
decl_stmt|;
name|buf
operator|.
name|initialize
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|dest
index|[
name|i
index|]
operator|=
name|buf
expr_stmt|;
block|}
return|return
literal|true
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
block|{     }
block|}
specifier|private
specifier|static
class|class
name|DummyCachePolicy
extends|extends
name|LowLevelCachePolicyBase
block|{
specifier|public
name|DummyCachePolicy
parameter_list|(
name|long
name|maxSize
parameter_list|)
block|{
name|super
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|cache
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{     }
specifier|public
name|void
name|notifyLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{     }
specifier|public
name|void
name|notifyUnlock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{     }
specifier|protected
name|long
name|evictSomeBlocks
parameter_list|(
name|long
name|memoryToReserve
parameter_list|,
name|EvictionListener
name|listener
parameter_list|)
block|{
return|return
name|memoryToReserve
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetPut
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|createConf
argument_list|()
decl_stmt|;
name|LowLevelCacheImpl
name|cache
init|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyCachePolicy
argument_list|(
literal|10
argument_list|)
argument_list|,
operator|new
name|DummyAllocator
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// no cleanup thread
name|String
name|fn1
init|=
literal|"file1"
operator|.
name|intern
argument_list|()
decl_stmt|,
name|fn2
init|=
literal|"file2"
operator|.
name|intern
argument_list|()
decl_stmt|;
name|LlapMemoryBuffer
index|[]
name|fakes
init|=
operator|new
name|LlapMemoryBuffer
index|[]
block|{
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|}
decl_stmt|;
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|2
block|,
literal|3
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|LlapMemoryBuffer
index|[]
name|bufsDiff
init|=
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|long
index|[]
name|mask
init|=
name|cache
operator|.
name|putFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|3
block|,
literal|1
block|}
argument_list|,
name|bufsDiff
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mask
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|mask
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// 2nd bit set - element 2 was already in cache.
name|assertSame
argument_list|(
name|fakes
index|[
literal|0
index|]
argument_list|,
name|bufsDiff
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|// Should have been replaced
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
literal|4
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStaleValueGet
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|createConf
argument_list|()
decl_stmt|;
name|LowLevelCacheImpl
name|cache
init|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyCachePolicy
argument_list|(
literal|10
argument_list|)
argument_list|,
operator|new
name|DummyAllocator
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// no cleanup thread
name|String
name|fn1
init|=
literal|"file1"
operator|.
name|intern
argument_list|()
decl_stmt|,
name|fn2
init|=
literal|"file2"
operator|.
name|intern
argument_list|()
decl_stmt|;
name|LlapMemoryBuffer
index|[]
name|fakes
init|=
operator|new
name|LlapMemoryBuffer
index|[]
block|{
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|}
decl_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|evict
argument_list|(
name|cache
argument_list|,
name|fakes
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|evict
argument_list|(
name|cache
argument_list|,
name|fakes
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
operator|-
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|getFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|verifyRefcount
argument_list|(
name|fakes
argument_list|,
operator|-
literal|1
argument_list|,
literal|3
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStaleValueReplace
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|createConf
argument_list|()
decl_stmt|;
name|LowLevelCacheImpl
name|cache
init|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyCachePolicy
argument_list|(
literal|10
argument_list|)
argument_list|,
operator|new
name|DummyAllocator
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// no cleanup thread
name|String
name|fn1
init|=
literal|"file1"
operator|.
name|intern
argument_list|()
decl_stmt|,
name|fn2
init|=
literal|"file2"
operator|.
name|intern
argument_list|()
decl_stmt|;
name|LlapMemoryBuffer
index|[]
name|fakes
init|=
operator|new
name|LlapMemoryBuffer
index|[]
block|{
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|,
name|fb
argument_list|()
block|}
decl_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|evict
argument_list|(
name|cache
argument_list|,
name|fakes
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|evict
argument_list|(
name|cache
argument_list|,
name|fakes
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|long
index|[]
name|mask
init|=
name|cache
operator|.
name|putFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|6
argument_list|,
literal|7
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mask
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|mask
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// Buffers at offset 2& 3 exist; 1 exists and is stale; 4 doesn't
name|assertNull
argument_list|(
name|cache
operator|.
name|putFileData
argument_list|(
name|fn2
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|}
argument_list|,
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|fbs
argument_list|(
name|fakes
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|7
argument_list|)
argument_list|,
name|cache
operator|.
name|getFileData
argument_list|(
name|fn1
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTTWithCleanup
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|createConf
argument_list|()
decl_stmt|;
specifier|final
name|LowLevelCacheImpl
name|cache
init|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|conf
argument_list|,
operator|new
name|DummyCachePolicy
argument_list|(
literal|10
argument_list|)
argument_list|,
operator|new
name|DummyAllocator
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|String
name|fn1
init|=
literal|"file1"
operator|.
name|intern
argument_list|()
decl_stmt|,
name|fn2
init|=
literal|"file2"
operator|.
name|intern
argument_list|()
decl_stmt|;
specifier|final
name|int
name|offsetsToUse
init|=
literal|8
decl_stmt|;
specifier|final
name|CountDownLatch
name|cdlIn
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|4
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
specifier|final
name|AtomicInteger
name|rdmsDone
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Callable
argument_list|<
name|Long
argument_list|>
name|rdmCall
init|=
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
specifier|public
name|Long
name|call
parameter_list|()
block|{
name|int
name|gets
init|=
literal|0
decl_stmt|,
name|puts
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|(
literal|1234
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
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
literal|0
init|;
name|i
operator|<
literal|20000
condition|;
operator|++
name|i
control|)
block|{
name|boolean
name|isGet
init|=
name|rdm
operator|.
name|nextBoolean
argument_list|()
decl_stmt|,
name|isFn1
init|=
name|rdm
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|String
name|fileName
init|=
name|isFn1
condition|?
name|fn1
else|:
name|fn2
decl_stmt|;
name|int
name|fileIndex
init|=
name|isFn1
condition|?
literal|1
else|:
literal|2
decl_stmt|;
name|int
name|count
init|=
name|rdm
operator|.
name|nextInt
argument_list|(
name|offsetsToUse
argument_list|)
decl_stmt|;
name|long
index|[]
name|offsets
init|=
operator|new
name|long
index|[
name|count
index|]
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
name|offsets
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|offsets
index|[
name|j
index|]
operator|=
name|rdm
operator|.
name|nextInt
argument_list|(
name|offsetsToUse
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isGet
condition|)
block|{
name|LlapMemoryBuffer
index|[]
name|results
init|=
name|cache
operator|.
name|getFileData
argument_list|(
name|fileName
argument_list|,
name|offsets
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|==
literal|null
condition|)
continue|continue;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|offsets
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
if|if
condition|(
name|results
index|[
name|j
index|]
operator|==
literal|null
condition|)
continue|continue;
operator|++
name|gets
expr_stmt|;
name|LlapCacheableBuffer
name|result
init|=
call|(
name|LlapCacheableBuffer
call|)
argument_list|(
name|results
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|makeFakeArenaIndex
argument_list|(
name|fileIndex
argument_list|,
name|offsets
index|[
name|j
index|]
argument_list|)
argument_list|,
name|result
operator|.
name|arenaIndex
argument_list|)
expr_stmt|;
name|result
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|LlapMemoryBuffer
index|[]
name|buffers
init|=
operator|new
name|LlapMemoryBuffer
index|[
name|count
index|]
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
name|offsets
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|LlapCacheableBuffer
name|buf
init|=
name|LowLevelCacheImpl
operator|.
name|allocateFake
argument_list|()
decl_stmt|;
name|buf
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|buf
operator|.
name|arenaIndex
operator|=
name|makeFakeArenaIndex
argument_list|(
name|fileIndex
argument_list|,
name|offsets
index|[
name|j
index|]
argument_list|)
expr_stmt|;
name|buffers
index|[
name|j
index|]
operator|=
name|buf
expr_stmt|;
block|}
name|long
index|[]
name|mask
init|=
name|cache
operator|.
name|putFileData
argument_list|(
name|fileName
argument_list|,
name|offsets
argument_list|,
name|buffers
argument_list|)
decl_stmt|;
name|puts
operator|+=
name|buffers
operator|.
name|length
expr_stmt|;
name|long
name|maskVal
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|mask
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mask
operator|.
name|length
argument_list|)
expr_stmt|;
name|maskVal
operator|=
name|mask
index|[
literal|0
index|]
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
name|offsets
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|LlapCacheableBuffer
name|buf
init|=
call|(
name|LlapCacheableBuffer
call|)
argument_list|(
name|buffers
index|[
name|j
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|maskVal
operator|&
literal|1
operator|)
operator|==
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
name|makeFakeArenaIndex
argument_list|(
name|fileIndex
argument_list|,
name|offsets
index|[
name|j
index|]
argument_list|)
argument_list|,
name|buf
operator|.
name|arenaIndex
argument_list|)
expr_stmt|;
block|}
name|maskVal
operator|>>=
literal|1
expr_stmt|;
name|buf
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|rdmsDone
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
operator|(
operator|(
name|long
operator|)
name|gets
operator|)
operator|<<
literal|32
operator|)
operator||
name|puts
return|;
block|}
specifier|private
name|int
name|makeFakeArenaIndex
parameter_list|(
name|int
name|fileIndex
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
operator|(
name|fileIndex
operator|<<
literal|16
operator|)
operator|+
name|offset
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|FutureTask
argument_list|<
name|Integer
argument_list|>
name|evictionTask
init|=
operator|new
name|FutureTask
argument_list|<
name|Integer
argument_list|>
argument_list|(
operator|new
name|Callable
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
specifier|public
name|Integer
name|call
parameter_list|()
block|{
name|boolean
name|isFirstFile
init|=
literal|false
decl_stmt|;
name|long
index|[]
name|offsets
init|=
operator|new
name|long
index|[
name|offsetsToUse
index|]
decl_stmt|;
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|(
literal|1234
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
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
name|offsetsToUse
condition|;
operator|++
name|i
control|)
block|{
name|offsets
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|int
name|evictions
init|=
literal|0
decl_stmt|;
name|syncThreadStart
argument_list|(
name|cdlIn
argument_list|,
name|cdlOut
argument_list|)
expr_stmt|;
while|while
condition|(
name|rdmsDone
operator|.
name|get
argument_list|()
operator|<
literal|3
condition|)
block|{
name|isFirstFile
operator|=
operator|!
name|isFirstFile
expr_stmt|;
name|String
name|fileName
init|=
name|isFirstFile
condition|?
name|fn1
else|:
name|fn2
decl_stmt|;
name|LlapMemoryBuffer
index|[]
name|results
init|=
name|cache
operator|.
name|getFileData
argument_list|(
name|fileName
argument_list|,
name|offsets
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|==
literal|null
condition|)
continue|continue;
name|int
name|startIndex
init|=
name|rdm
operator|.
name|nextInt
argument_list|(
name|results
operator|.
name|length
argument_list|)
decl_stmt|,
name|index
init|=
name|startIndex
decl_stmt|;
name|LlapCacheableBuffer
name|victim
init|=
literal|null
decl_stmt|;
do|do
block|{
if|if
condition|(
name|results
index|[
name|index
index|]
operator|!=
literal|null
condition|)
block|{
name|LlapCacheableBuffer
name|result
init|=
operator|(
name|LlapCacheableBuffer
operator|)
name|results
index|[
name|index
index|]
decl_stmt|;
name|result
operator|.
name|decRef
argument_list|()
expr_stmt|;
if|if
condition|(
name|victim
operator|==
literal|null
operator|&&
name|result
operator|.
name|invalidate
argument_list|()
condition|)
block|{
operator|++
name|evictions
expr_stmt|;
name|victim
operator|=
name|result
expr_stmt|;
block|}
block|}
operator|++
name|index
expr_stmt|;
if|if
condition|(
name|index
operator|==
name|results
operator|.
name|length
condition|)
name|index
operator|=
literal|0
expr_stmt|;
block|}
do|while
condition|(
name|index
operator|!=
name|startIndex
condition|)
do|;
if|if
condition|(
name|victim
operator|==
literal|null
condition|)
continue|continue;
name|cache
operator|.
name|notifyEvicted
argument_list|(
name|victim
argument_list|)
expr_stmt|;
block|}
return|return
name|evictions
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|FutureTask
argument_list|<
name|Long
argument_list|>
name|rdmTask1
init|=
operator|new
name|FutureTask
argument_list|<
name|Long
argument_list|>
argument_list|(
name|rdmCall
argument_list|)
decl_stmt|,
name|rdmTask2
init|=
operator|new
name|FutureTask
argument_list|<
name|Long
argument_list|>
argument_list|(
name|rdmCall
argument_list|)
decl_stmt|,
name|rdmTask3
init|=
operator|new
name|FutureTask
argument_list|<
name|Long
argument_list|>
argument_list|(
name|rdmCall
argument_list|)
decl_stmt|;
name|Executor
name|threadPool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
name|rdmTask1
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
name|rdmTask2
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
name|rdmTask3
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
name|evictionTask
argument_list|)
expr_stmt|;
try|try
block|{
name|cdlIn
operator|.
name|await
argument_list|()
expr_stmt|;
name|cdlOut
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|long
name|result1
init|=
name|rdmTask1
operator|.
name|get
argument_list|()
decl_stmt|,
name|result2
init|=
name|rdmTask2
operator|.
name|get
argument_list|()
decl_stmt|,
name|result3
init|=
name|rdmTask3
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|evictions
init|=
name|evictionTask
operator|.
name|get
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"MTT test: task 1: "
operator|+
name|descRdmTask
argument_list|(
name|result1
argument_list|)
operator|+
literal|", task 2: "
operator|+
name|descRdmTask
argument_list|(
name|result2
argument_list|)
operator|+
literal|", task 3: "
operator|+
name|descRdmTask
argument_list|(
name|result3
argument_list|)
operator|+
literal|"; "
operator|+
name|evictions
operator|+
literal|" evictions"
argument_list|)
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
name|String
name|descRdmTask
parameter_list|(
name|long
name|result
parameter_list|)
block|{
return|return
operator|(
name|result
operator|>>>
literal|32
operator|)
operator|+
literal|" successful gets, "
operator|+
operator|(
name|result
operator|&
operator|(
operator|(
literal|1L
operator|<<
literal|32
operator|)
operator|-
literal|1
operator|)
operator|)
operator|+
literal|" puts"
return|;
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
name|evict
parameter_list|(
name|LowLevelCacheImpl
name|cache
parameter_list|,
name|LlapMemoryBuffer
name|fake
parameter_list|)
block|{
name|LlapCacheableBuffer
name|victimBuffer
init|=
operator|(
name|LlapCacheableBuffer
operator|)
name|fake
decl_stmt|;
name|int
name|refCount
init|=
name|victimBuffer
operator|.
name|getRefCount
argument_list|()
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
name|refCount
condition|;
operator|++
name|i
control|)
block|{
name|victimBuffer
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|victimBuffer
operator|.
name|invalidate
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|notifyEvicted
argument_list|(
name|victimBuffer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyRefcount
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|fakes
parameter_list|,
name|int
modifier|...
name|refCounts
parameter_list|)
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
name|refCounts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|refCounts
index|[
name|i
index|]
argument_list|,
operator|(
operator|(
name|LlapCacheableBuffer
operator|)
name|fakes
index|[
name|i
index|]
operator|)
operator|.
name|getRefCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|LlapMemoryBuffer
index|[]
name|fbs
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|fakes
parameter_list|,
name|int
modifier|...
name|indexes
parameter_list|)
block|{
name|LlapMemoryBuffer
index|[]
name|rv
init|=
operator|new
name|LlapMemoryBuffer
index|[
name|indexes
operator|.
name|length
index|]
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
name|indexes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|rv
index|[
name|i
index|]
operator|=
operator|(
name|indexes
index|[
name|i
index|]
operator|==
operator|-
literal|1
operator|)
condition|?
literal|null
else|:
name|fakes
index|[
name|indexes
index|[
name|i
index|]
index|]
expr_stmt|;
block|}
return|return
name|rv
return|;
block|}
specifier|private
name|LlapCacheableBuffer
name|fb
parameter_list|()
block|{
name|LlapCacheableBuffer
name|fake
init|=
name|LowLevelCacheImpl
operator|.
name|allocateFake
argument_list|()
decl_stmt|;
name|fake
operator|.
name|incRef
argument_list|()
expr_stmt|;
return|return
name|fake
return|;
block|}
specifier|private
name|Configuration
name|createConf
parameter_list|()
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
literal|3
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
literal|8
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
literal|8
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
literal|8
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

