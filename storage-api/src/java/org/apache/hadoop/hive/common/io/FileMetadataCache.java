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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|MemoryBufferOrBuffers
import|;
end_import

begin_interface
specifier|public
interface|interface
name|FileMetadataCache
block|{
comment|/**    * @return Metadata for a given file (ORC or Parquet footer).    *         The caller must decref this buffer when done.    */
name|MemoryBufferOrBuffers
name|getFileMetadata
parameter_list|(
name|Object
name|fileKey
parameter_list|)
function_decl|;
comment|// TODO: add BB put method(s) when merging with ORC off-heap metadata cache
comment|/**    * Puts the metadata for a given file (e.g. a footer buffer into cache).    * @param fileKey The file key.    * @param length The footer length.    * @param is The stream to read the footer from.    * @return The buffer or buffers representing the cached footer.    *         The caller must decref this buffer when done.    */
name|MemoryBufferOrBuffers
name|putFileMetadata
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|int
name|length
parameter_list|,
name|InputStream
name|is
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases the buffer returned from getFileMetadata or putFileMetadata method.    * @param buffer The buffer to release.    */
name|void
name|decRefBuffer
parameter_list|(
name|MemoryBufferOrBuffers
name|buffer
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

