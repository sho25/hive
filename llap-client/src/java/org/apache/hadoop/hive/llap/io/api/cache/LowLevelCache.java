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
name|io
operator|.
name|api
operator|.
name|cache
package|;
end_package

begin_interface
specifier|public
interface|interface
name|LowLevelCache
block|{
comment|/**    * Gets file data for particular offsets. Null entries mean no data.    * @param file File name; MUST be interned.    */
name|LlapMemoryBuffer
index|[]
name|getFileData
parameter_list|(
name|String
name|fileName
parameter_list|,
name|long
index|[]
name|offsets
parameter_list|)
function_decl|;
comment|/**    * Puts file data into cache.    * @param file File name; MUST be interned.    * @return null if all data was put; bitmask indicating which chunks were not put otherwise;    *         the replacement chunks from cache are updated directly in the array.    */
name|long
index|[]
name|putFileData
parameter_list|(
name|String
name|file
parameter_list|,
name|long
index|[]
name|offsets
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|chunks
parameter_list|)
function_decl|;
comment|/**    * Releases the buffer returned by getFileData or allocateMultiple.    */
name|void
name|releaseBuffer
parameter_list|(
name|LlapMemoryBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Allocate dest.length new blocks of size into dest.    */
name|void
name|allocateMultiple
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|)
function_decl|;
name|void
name|releaseBuffers
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|cacheBuffers
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

