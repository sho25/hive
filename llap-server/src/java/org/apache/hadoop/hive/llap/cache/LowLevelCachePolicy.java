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

begin_comment
comment|/**  * Actor managing the eviction requests.  * Cache policy relies notifications from the actual {@link LowLevelCache} to keep track of buffer access.  */
end_comment

begin_interface
specifier|public
interface|interface
name|LowLevelCachePolicy
extends|extends
name|LlapIoDebugDump
block|{
comment|/**    * Signals to the policy the addition of a new page to the cache directory.    *    * @param buffer   buffer to be cached    * @param priority the priority of cached element    */
name|void
name|cache
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|,
name|Priority
name|priority
parameter_list|)
function_decl|;
comment|/**    * Notifies the policy that this buffer is locked, thus take it out of the free list.    * Note that this notification is a hint and can not be the source of truth about what can be evicted    * currently the source of truth is the counter of reference to the buffer see {@link LlapCacheableBuffer#isLocked()}.    *    * @param buffer buffer to be locked.    */
name|void
name|notifyLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Notifies the policy that a buffer is unlocked after been used. This notification signals to the policy that an    * access to this page occurred thus can be used to track what page got a read request    *    * @param buffer buffer that just got unlocked    */
name|void
name|notifyUnlock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Signals to the policy that it has to evict some pages to make room incoming buffers.    * Policy has to at least evict the amount requested.    * Policy does not now about the shape of evicted buffers and only can reason about total size.    * Not that is method will block until at least {@code memoryToReserve} bytes are evicted.    *    * @param memoryToReserve amount of bytes to be evicted    * @return actual amount of evicted bytes.    */
name|long
name|evictSomeBlocks
parameter_list|(
name|long
name|memoryToReserve
parameter_list|)
function_decl|;
comment|/**    * Sets the eviction listener dispatcher.    *    * @param listener eviction listener actor    */
name|void
name|setEvictionListener
parameter_list|(
name|EvictionListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Signals to the policy to evict all the unlocked used buffers.    *    * @return amount (bytes) of memory evicted.    */
name|long
name|purge
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

