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
name|javax
operator|.
name|management
operator|.
name|MXBean
import|;
end_import

begin_comment
comment|/**  * MXbean to expose cache allocator related information through JMX.  */
end_comment

begin_interface
annotation|@
name|MXBean
specifier|public
interface|interface
name|BuddyAllocatorMXBean
block|{
comment|/**    * Gets if bytebuffers are allocated directly offheap.    *    * @return gets if direct bytebuffer allocation    */
name|boolean
name|getIsDirect
parameter_list|()
function_decl|;
comment|/**    * Gets minimum allocation size of allocator.    *    * @return minimum allocation size    */
name|int
name|getMinAllocation
parameter_list|()
function_decl|;
comment|/**    * Gets maximum allocation size of allocator.    *    * @return maximum allocation size    */
name|int
name|getMaxAllocation
parameter_list|()
function_decl|;
comment|/**    * Gets the arena size.    *    * @return arena size    */
name|int
name|getArenaSize
parameter_list|()
function_decl|;
comment|/**    * Gets the maximum cache size.    *    * @return max cache size    */
name|long
name|getMaxCacheSize
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

