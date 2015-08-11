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
name|common
operator|.
name|io
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
name|encoded
operator|.
name|MemoryBuffer
import|;
end_import

begin_comment
comment|/** An allocator provided externally to storage classes to allocate MemoryBuffer-s. */
end_comment

begin_interface
specifier|public
interface|interface
name|Allocator
block|{
specifier|public
specifier|static
class|class
name|AllocatorOutOfMemoryException
extends|extends
name|RuntimeException
block|{
specifier|public
name|AllocatorOutOfMemoryException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|268124648177151761L
decl_stmt|;
block|}
comment|/**    * Allocates multiple buffers of a given size.    * @param dest Array where buffers are placed. Objects are reused if already there    *             (see createUnallocated), created otherwise.    * @param size Allocation size.    * @throws AllocatorOutOfMemoryException Cannot allocate.    */
name|void
name|allocateMultiple
parameter_list|(
name|MemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|AllocatorOutOfMemoryException
function_decl|;
comment|/**    * Creates an unallocated memory buffer object. This object can be passed to allocateMultiple    * to allocate; this is useful if data structures are created for separate buffers that can    * later be allocated together.    */
name|MemoryBuffer
name|createUnallocated
parameter_list|()
function_decl|;
comment|/** Deallocates a memory buffer. */
name|void
name|deallocate
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/** Whether the allocator uses direct buffers. */
name|boolean
name|isDirectAlloc
parameter_list|()
function_decl|;
comment|/** Maximum allocation size supported by this allocator. */
name|int
name|getMaxAllocation
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

