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
name|common
operator|.
name|io
operator|.
name|storage_api
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
name|storage_api
operator|.
name|MemoryBuffer
import|;
end_import

begin_comment
comment|/**  * An allocator that has additional, internal-only call to deallocate evicted buffer.  * When we evict buffers, we do not release memory to the system; that is because we want it for  * ourselves, so we set the value atomically to account for both eviction and the new demand.  */
end_comment

begin_interface
specifier|public
interface|interface
name|EvictionAwareAllocator
extends|extends
name|Allocator
block|{
name|void
name|deallocateEvicted
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

