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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Lock
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
name|hive
operator|.
name|llap
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

begin_class
specifier|public
class|class
name|LowLevelFifoCachePolicy
implements|implements
name|LowLevelCachePolicy
block|{
specifier|private
specifier|final
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|LlapCacheableBuffer
argument_list|>
name|buffers
decl_stmt|;
specifier|private
name|EvictionListener
name|evictionListener
decl_stmt|;
specifier|private
name|LlapOomDebugDump
name|parentDebugDump
decl_stmt|;
specifier|public
name|LowLevelFifoCachePolicy
parameter_list|()
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"FIFO cache policy"
argument_list|)
expr_stmt|;
name|buffers
operator|=
operator|new
name|LinkedList
argument_list|<
name|LlapCacheableBuffer
argument_list|>
argument_list|()
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
name|pri
parameter_list|)
block|{
comment|// Ignore priority.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|buffers
operator|.
name|add
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
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
comment|// FIFO policy doesn't care.
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
comment|// FIFO policy doesn't care.
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
name|void
name|setParentDebugDumper
parameter_list|(
name|LlapOomDebugDump
name|dumper
parameter_list|)
block|{
name|this
operator|.
name|parentDebugDump
operator|=
name|dumper
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
return|return
name|evictInternal
argument_list|(
name|memoryToReserve
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|private
name|long
name|evictInternal
parameter_list|(
name|long
name|memoryToReserve
parameter_list|,
name|int
name|minSize
parameter_list|)
block|{
name|long
name|evicted
init|=
literal|0
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|LlapCacheableBuffer
argument_list|>
name|iter
init|=
name|buffers
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|evicted
operator|<
name|memoryToReserve
operator|&&
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LlapCacheableBuffer
name|buffer
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|long
name|memUsage
init|=
name|buffer
operator|.
name|getMemoryUsage
argument_list|()
decl_stmt|;
if|if
condition|(
name|memUsage
operator|<
name|minSize
operator|||
operator|(
name|minSize
operator|>
literal|0
operator|&&
operator|!
operator|(
name|buffer
operator|instanceof
name|LlapAllocatorBuffer
operator|)
operator|)
condition|)
continue|continue;
if|if
condition|(
name|LlapCacheableBuffer
operator|.
name|INVALIDATE_OK
operator|==
name|buffer
operator|.
name|invalidate
argument_list|()
condition|)
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
name|evicted
operator|+=
name|memUsage
expr_stmt|;
name|evictionListener
operator|.
name|notifyEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|evicted
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|debugDumpForOom
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"FIFO eviction list: "
argument_list|)
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
name|buffers
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" elements): "
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|LlapCacheableBuffer
argument_list|>
name|iter
init|=
name|buffers
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|iter
operator|.
name|next
argument_list|()
operator|.
name|toStringForCache
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|",\n"
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|parentDebugDump
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|append
argument_list|(
name|parentDebugDump
operator|.
name|debugDumpForOom
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debugDumpShort
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\nFIFO eviction list: "
argument_list|)
expr_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
name|buffers
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" elements)"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|parentDebugDump
operator|!=
literal|null
condition|)
block|{
name|parentDebugDump
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

