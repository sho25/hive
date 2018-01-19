begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Buffer that can be managed by LowLevelEvictionPolicy.  * We want to have cacheable and non-allocator buffers, as well as allocator buffers with no  * cache dependency, and also ones that are both. Alas, we could only achieve this if we were  * using a real programming language.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LlapCacheableBuffer
block|{
specifier|protected
specifier|static
specifier|final
name|int
name|IN_LIST
init|=
operator|-
literal|2
decl_stmt|,
name|NOT_IN_CACHE
init|=
operator|-
literal|1
decl_stmt|;
comment|/** Priority for cache policy (should be pretty universal). */
specifier|public
name|double
name|priority
decl_stmt|;
comment|/** Last priority update time for cache policy (should be pretty universal). */
specifier|public
name|long
name|lastUpdate
init|=
operator|-
literal|1
decl_stmt|;
comment|// TODO: remove some of these fields as needed?
comment|/** Linked list pointers for LRFU/LRU cache policies. Given that each block is in cache    * that might be better than external linked list. Or not, since this is not concurrent. */
specifier|public
name|LlapCacheableBuffer
name|prev
init|=
literal|null
decl_stmt|;
comment|/** Linked list pointers for LRFU/LRU cache policies. Given that each block is in cache    * that might be better than external linked list. Or not, since this is not concurrent. */
specifier|public
name|LlapCacheableBuffer
name|next
init|=
literal|null
decl_stmt|;
comment|/** Index in heap for LRFU/LFU cache policies. */
specifier|public
name|int
name|indexInHeap
init|=
name|NOT_IN_CACHE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|INVALIDATE_OK
init|=
literal|0
decl_stmt|,
name|INVALIDATE_FAILED
init|=
literal|1
decl_stmt|,
name|INVALIDATE_ALREADY_INVALID
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|abstract
name|int
name|invalidate
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|long
name|getMemoryUsage
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|void
name|notifyEvicted
parameter_list|(
name|EvictionDispatcher
name|evictionDispatcher
parameter_list|)
function_decl|;
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
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
argument_list|)
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
specifier|protected
specifier|abstract
name|boolean
name|isLocked
parameter_list|()
function_decl|;
block|}
end_class

end_unit

