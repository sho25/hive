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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * Memory Manager is an accountant over a fixed size of memory.  * It does is the following.  * 1 - tracks the amount of memory (bytes) reserved out of a given maximum size to be shared between IO Threads.  * 2 - when a reservation can not be fulfilled form the current free space it has to notify Evictor to free up some  * space.  *<p>  * Note that it does not know about the actual shape, content or owners of memory, all it cares about is bytes usage.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MemoryManager
block|{
comment|/**    * Signals to the Memory manager the release of some memory bytes that are free to be used.    *    * @param memUsage amount of memory bytes that are released to be added to the ledger as free.    */
name|void
name|releaseMemory
parameter_list|(
name|long
name|memUsage
parameter_list|)
function_decl|;
comment|/**    * Sets the amount of bytes that the memory manager is managing.    *    * @param maxSize total amount of available bytes to be allocated.    */
name|void
name|updateMaxSize
parameter_list|(
name|long
name|maxSize
parameter_list|)
function_decl|;
comment|/**    * Reserves some amount of bytes within the managed pool of memory.    *<p>    * Callers expect that the memory manager will always fulfill the request by notifying the Evictor about how much    * need to be evicted to accommodate the reserve request.    * Note that this method will block until reservation is fulfilled.    *    * @param memoryToReserve Amount of bytes to reserve.    * @param isStopped       Caller state to indicate if it is still running while the memory manager is trying to    *                        allocate the space.    */
name|void
name|reserveMemory
parameter_list|(
name|long
name|memoryToReserve
parameter_list|,
name|AtomicBoolean
name|isStopped
parameter_list|)
function_decl|;
comment|/**    * Request the memory manager to evict more memory, this will be blocking and might return 0 if nothing was evicted.    *    * @param memoryToEvict amount of bytes to evict.    * @return actual amount of evicted bytes.    */
name|long
name|evictMemory
parameter_list|(
name|long
name|memoryToEvict
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

