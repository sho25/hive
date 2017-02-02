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
operator|.
name|encoded
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/** Abstract interface for any class wrapping a ByteBuffer. */
end_comment

begin_interface
specifier|public
interface|interface
name|MemoryBuffer
block|{
comment|/**    * Get the raw byte buffer that backs this buffer.    * Note - raw buffer should not be modified.    * @return the shared byte buffer    */
specifier|public
name|ByteBuffer
name|getByteBufferRaw
parameter_list|()
function_decl|;
specifier|public
name|ByteBuffer
name|getByteBufferDup
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

