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
name|LinkedHashSet
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

begin_class
specifier|public
class|class
name|LowLevelFifoCachePolicy
extends|extends
name|LowLevelCachePolicyBase
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
name|LinkedHashSet
argument_list|<
name|LlapCacheableBuffer
argument_list|>
name|buffers
decl_stmt|;
specifier|public
name|LowLevelFifoCachePolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
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
argument_list|)
expr_stmt|;
name|int
name|expectedBufferSize
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
name|int
name|expectedBuffers
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
name|expectedBufferSize
argument_list|)
decl_stmt|;
name|buffers
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|LlapCacheableBuffer
argument_list|>
argument_list|(
call|(
name|int
call|)
argument_list|(
name|expectedBuffers
operator|/
literal|0.75f
argument_list|)
argument_list|)
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
parameter_list|)
block|{
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
name|candidate
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|candidate
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
name|candidate
operator|.
name|length
expr_stmt|;
name|listener
operator|.
name|notifyEvicted
argument_list|(
name|candidate
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
block|}
end_class

end_unit

