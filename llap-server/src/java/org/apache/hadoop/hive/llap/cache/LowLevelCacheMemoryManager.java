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
name|impl
operator|.
name|LlapIoImpl
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
name|metrics
operator|.
name|LlapDaemonCacheMetrics
import|;
end_import

begin_comment
comment|/**  * Implementation of memory manager for low level cache. Note that memory is released during  * reserve most of the time, by calling the evictor to evict some memory. releaseMemory is  * called rarely.  */
end_comment

begin_class
specifier|public
class|class
name|LowLevelCacheMemoryManager
implements|implements
name|MemoryManager
block|{
specifier|private
specifier|final
name|AtomicLong
name|usedMemory
decl_stmt|;
specifier|private
specifier|final
name|LowLevelCachePolicy
name|evictor
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonCacheMetrics
name|metrics
decl_stmt|;
specifier|private
name|long
name|maxSize
decl_stmt|;
specifier|public
name|LowLevelCacheMemoryManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LowLevelCachePolicy
name|evictor
parameter_list|,
name|LlapDaemonCacheMetrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
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
name|this
operator|.
name|evictor
operator|=
name|evictor
expr_stmt|;
name|this
operator|.
name|usedMemory
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|metrics
operator|.
name|setCacheCapacityTotal
argument_list|(
name|maxSize
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
literal|"Cache memory manager initialized with max size "
operator|+
name|maxSize
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reserveMemory
parameter_list|(
specifier|final
name|long
name|memoryToReserve
parameter_list|,
name|boolean
name|waitForEviction
parameter_list|)
block|{
comment|// TODO: if this cannot evict enough, it will spin infinitely. Terminate at some point?
name|int
name|badCallCount
init|=
literal|0
decl_stmt|;
name|int
name|nextLog
init|=
literal|4
decl_stmt|;
name|long
name|evictedTotalMetric
init|=
literal|0
decl_stmt|,
name|reservedTotalMetric
init|=
literal|0
decl_stmt|,
name|remainingToReserve
init|=
name|memoryToReserve
decl_stmt|;
name|boolean
name|result
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|remainingToReserve
operator|>
literal|0
condition|)
block|{
name|long
name|usedMem
init|=
name|usedMemory
operator|.
name|get
argument_list|()
decl_stmt|,
name|newUsedMem
init|=
name|usedMem
operator|+
name|remainingToReserve
decl_stmt|;
if|if
condition|(
name|newUsedMem
operator|<=
name|maxSize
condition|)
block|{
if|if
condition|(
name|usedMemory
operator|.
name|compareAndSet
argument_list|(
name|usedMem
argument_list|,
name|newUsedMem
argument_list|)
condition|)
block|{
name|reservedTotalMetric
operator|+=
name|remainingToReserve
expr_stmt|;
break|break;
block|}
continue|continue;
block|}
comment|// TODO: for one-block case, we could move notification for the last block out of the loop.
name|long
name|evicted
init|=
name|evictor
operator|.
name|evictSomeBlocks
argument_list|(
name|remainingToReserve
argument_list|)
decl_stmt|;
if|if
condition|(
name|evicted
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|waitForEviction
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
break|break;
block|}
operator|++
name|badCallCount
expr_stmt|;
if|if
condition|(
name|badCallCount
operator|==
name|nextLog
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot evict blocks for "
operator|+
name|badCallCount
operator|+
literal|" calls; cache full?"
argument_list|)
expr_stmt|;
name|nextLog
operator|<<=
literal|1
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|Math
operator|.
name|min
argument_list|(
literal|1000
argument_list|,
name|nextLog
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|result
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
continue|continue;
block|}
name|evictedTotalMetric
operator|+=
name|evicted
expr_stmt|;
name|badCallCount
operator|=
literal|0
expr_stmt|;
comment|// Adjust the memory - we have to account for what we have just evicted.
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|availableToReserveAfterEvict
init|=
name|maxSize
operator|-
name|usedMem
operator|+
name|evicted
decl_stmt|;
name|long
name|reservedAfterEvict
init|=
name|Math
operator|.
name|min
argument_list|(
name|remainingToReserve
argument_list|,
name|availableToReserveAfterEvict
argument_list|)
decl_stmt|;
if|if
condition|(
name|usedMemory
operator|.
name|compareAndSet
argument_list|(
name|usedMem
argument_list|,
name|usedMem
operator|-
name|evicted
operator|+
name|reservedAfterEvict
argument_list|)
condition|)
block|{
name|remainingToReserve
operator|-=
name|reservedAfterEvict
expr_stmt|;
name|reservedTotalMetric
operator|+=
name|reservedAfterEvict
expr_stmt|;
break|break;
block|}
name|usedMem
operator|=
name|usedMemory
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
name|metrics
operator|.
name|incrCacheCapacityUsed
argument_list|(
name|reservedTotalMetric
operator|-
name|evictedTotalMetric
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|forceReservedMemory
parameter_list|(
name|int
name|memoryToEvict
parameter_list|)
block|{
while|while
condition|(
name|memoryToEvict
operator|>
literal|0
condition|)
block|{
name|long
name|evicted
init|=
name|evictor
operator|.
name|evictSomeBlocks
argument_list|(
name|memoryToEvict
argument_list|)
decl_stmt|;
if|if
condition|(
name|evicted
operator|==
literal|0
condition|)
return|return;
name|memoryToEvict
operator|-=
name|evicted
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseMemory
parameter_list|(
specifier|final
name|long
name|memoryToRelease
parameter_list|)
block|{
name|long
name|oldV
decl_stmt|;
do|do
block|{
name|oldV
operator|=
name|usedMemory
operator|.
name|get
argument_list|()
expr_stmt|;
assert|assert
name|oldV
operator|>=
name|memoryToRelease
assert|;
block|}
do|while
condition|(
operator|!
name|usedMemory
operator|.
name|compareAndSet
argument_list|(
name|oldV
argument_list|,
name|oldV
operator|-
name|memoryToRelease
argument_list|)
condition|)
do|;
name|metrics
operator|.
name|incrCacheCapacityUsed
argument_list|(
operator|-
name|memoryToRelease
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|debugDumpForOom
parameter_list|()
block|{
return|return
literal|"cache state\n"
operator|+
name|evictor
operator|.
name|debugDumpForOom
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateMaxSize
parameter_list|(
name|long
name|maxSize
parameter_list|)
block|{
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
block|}
block|}
end_class

end_unit

