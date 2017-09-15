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
name|SerDeLowLevelCacheImpl
operator|.
name|LlapSerDeDataBuffer
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
name|metadata
operator|.
name|OrcFileEstimateErrors
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
name|metadata
operator|.
name|MetadataCache
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
name|metadata
operator|.
name|MetadataCache
operator|.
name|LlapMetadataBuffer
import|;
end_import

begin_comment
comment|/**  * Eviction dispatcher - uses double dispatch to route eviction notifications to correct caches.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|EvictionDispatcher
implements|implements
name|EvictionListener
implements|,
name|LlapOomDebugDump
block|{
specifier|private
specifier|final
name|LowLevelCache
name|dataCache
decl_stmt|;
specifier|private
specifier|final
name|SerDeLowLevelCacheImpl
name|serdeCache
decl_stmt|;
specifier|private
specifier|final
name|MetadataCache
name|metadataCache
decl_stmt|;
specifier|private
specifier|final
name|EvictionAwareAllocator
name|allocator
decl_stmt|;
specifier|public
name|EvictionDispatcher
parameter_list|(
name|LowLevelCache
name|dataCache
parameter_list|,
name|SerDeLowLevelCacheImpl
name|serdeCache
parameter_list|,
name|MetadataCache
name|metadataCache
parameter_list|,
name|EvictionAwareAllocator
name|allocator
parameter_list|)
block|{
name|this
operator|.
name|dataCache
operator|=
name|dataCache
expr_stmt|;
name|this
operator|.
name|metadataCache
operator|=
name|metadataCache
expr_stmt|;
name|this
operator|.
name|serdeCache
operator|=
name|serdeCache
expr_stmt|;
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{
name|buffer
operator|.
name|notifyEvicted
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// This will call one of the specific notifyEvicted overloads.
block|}
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|LlapSerDeDataBuffer
name|buffer
parameter_list|)
block|{
name|serdeCache
operator|.
name|notifyEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|allocator
operator|.
name|deallocateEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|LlapDataBuffer
name|buffer
parameter_list|)
block|{
name|dataCache
operator|.
name|notifyEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|allocator
operator|.
name|deallocateEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|LlapMetadataBuffer
argument_list|<
name|?
argument_list|>
name|buffer
parameter_list|)
block|{
name|metadataCache
operator|.
name|notifyEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
comment|// Note: the metadata cache may deallocate additional buffers, but not this one.
name|allocator
operator|.
name|deallocateEvicted
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|OrcFileEstimateErrors
name|buffer
parameter_list|)
block|{
name|metadataCache
operator|.
name|notifyEvicted
argument_list|(
name|buffer
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|dataCache
operator|.
name|debugDumpForOom
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|serdeCache
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|serdeCache
operator|.
name|debugDumpForOom
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metadataCache
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|metadataCache
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
name|dataCache
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
if|if
condition|(
name|serdeCache
operator|!=
literal|null
condition|)
block|{
name|serdeCache
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metadataCache
operator|!=
literal|null
condition|)
block|{
name|metadataCache
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

