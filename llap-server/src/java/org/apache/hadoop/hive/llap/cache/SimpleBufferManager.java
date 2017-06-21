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
name|List
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
name|Allocator
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
name|DataCache
operator|.
name|BooleanRef
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
name|DataCache
operator|.
name|DiskRangeListFactory
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
name|DiskRange
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
name|DiskRangeList
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

begin_class
specifier|public
class|class
name|SimpleBufferManager
implements|implements
name|BufferUsageManager
implements|,
name|LowLevelCache
block|{
specifier|private
specifier|final
name|Allocator
name|allocator
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonCacheMetrics
name|metrics
decl_stmt|;
specifier|public
name|SimpleBufferManager
parameter_list|(
name|Allocator
name|allocator
parameter_list|,
name|LlapDaemonCacheMetrics
name|metrics
parameter_list|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Simple buffer manager"
argument_list|)
expr_stmt|;
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
specifier|private
name|boolean
name|lockBuffer
parameter_list|(
name|LlapAllocatorBuffer
name|buffer
parameter_list|)
block|{
name|int
name|rc
init|=
name|buffer
operator|.
name|incRef
argument_list|()
decl_stmt|;
if|if
condition|(
name|rc
operator|<=
literal|0
condition|)
return|return
literal|false
return|;
name|metrics
operator|.
name|incrCacheNumLockedBuffers
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|unlockBuffer
parameter_list|(
name|LlapAllocatorBuffer
name|buffer
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|.
name|decRef
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|LlapIoImpl
operator|.
name|CACHE_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|CACHE_LOGGER
operator|.
name|trace
argument_list|(
literal|"Deallocating {} that was not cached"
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
name|allocator
operator|.
name|deallocate
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|metrics
operator|.
name|decrCacheNumLockedBuffers
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decRefBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
name|unlockBuffer
argument_list|(
operator|(
name|LlapAllocatorBuffer
operator|)
name|buffer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decRefBuffers
parameter_list|(
name|List
argument_list|<
name|MemoryBuffer
argument_list|>
name|cacheBuffers
parameter_list|)
block|{
for|for
control|(
name|MemoryBuffer
name|b
range|:
name|cacheBuffers
control|)
block|{
name|unlockBuffer
argument_list|(
operator|(
name|LlapAllocatorBuffer
operator|)
name|b
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|incRefBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
return|return
name|lockBuffer
argument_list|(
operator|(
name|LlapAllocatorBuffer
operator|)
name|buffer
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Allocator
name|getAllocator
parameter_list|()
block|{
return|return
name|allocator
return|;
block|}
annotation|@
name|Override
specifier|public
name|DiskRangeList
name|getFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRangeList
name|range
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|DiskRangeListFactory
name|factory
parameter_list|,
name|LowLevelCacheCounters
name|qfCounters
parameter_list|,
name|BooleanRef
name|gotAllData
parameter_list|)
block|{
return|return
name|range
return|;
comment|// Nothing changes - no cache.
block|}
annotation|@
name|Override
specifier|public
name|long
index|[]
name|putFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRange
index|[]
name|ranges
parameter_list|,
name|MemoryBuffer
index|[]
name|chunks
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|Priority
name|priority
parameter_list|,
name|LowLevelCacheCounters
name|qfCounters
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
name|chunks
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|LlapAllocatorBuffer
name|buffer
init|=
operator|(
name|LlapAllocatorBuffer
operator|)
name|chunks
index|[
name|i
index|]
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
literal|"Locking {} at put time (no cache)"
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
name|boolean
name|canLock
init|=
name|lockBuffer
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
assert|assert
name|canLock
assert|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Buffer manager doesn't have cache"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|debugDumpForOom
parameter_list|()
block|{
return|return
literal|""
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
comment|// TODO Auto-generated method stub
block|}
block|}
end_class

end_unit

