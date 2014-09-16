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
name|ql
operator|.
name|io
operator|.
name|orc
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_interface
interface|interface
name|CompressionCodec
block|{
specifier|public
enum|enum
name|Modifier
block|{
comment|/* speed/compression tradeoffs */
name|FASTEST
block|,
name|FAST
block|,
name|DEFAULT
block|,
comment|/* data sensitivity modifiers */
name|TEXT
block|,
name|BINARY
block|}
empty_stmt|;
comment|/**    * Compress the in buffer to the out buffer.    * @param in the bytes to compress    * @param out the uncompressed bytes    * @param overflow put any additional bytes here    * @return true if the output is smaller than input    * @throws IOException    */
name|boolean
name|compress
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|ByteBuffer
name|out
parameter_list|,
name|ByteBuffer
name|overflow
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Decompress the in buffer to the out buffer.    * @param in the bytes to decompress    * @param out the decompressed bytes    * @throws IOException    */
name|void
name|decompress
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|ByteBuffer
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Produce a modified compression codec if the underlying algorithm allows    * modification.    *    * This does not modify the current object, but returns a new object if    * modifications are possible. Returns the same object if no modifications    * are possible.    * @param modifiers compression modifiers    * @return codec for use after optional modification    */
name|CompressionCodec
name|modify
parameter_list|(
name|EnumSet
argument_list|<
name|Modifier
argument_list|>
name|modifiers
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

