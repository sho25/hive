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

begin_comment
comment|/**  * The interface for writing ORC files.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Writer
block|{
comment|/**    * Add arbitrary meta-data to the ORC file. This may be called at any point    * until the Writer is closed. If the same key is passed a second time, the    * second value will replace the first.    * @param key a key to label the data with.    * @param value the contents of the metadata.    */
name|void
name|addUserMetadata
parameter_list|(
name|String
name|key
parameter_list|,
name|ByteBuffer
name|value
parameter_list|)
function_decl|;
comment|/**    * Add a row to the ORC file.    * @param row the row to add    * @throws IOException    */
name|void
name|addRow
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Flush all of the buffers and close the file. No methods on this writer    * should be called afterwards.    * @throws IOException    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return the deserialized data size. Raw data size will be compute when    * writing the file footer. Hence raw data size value will be available only    * after closing the writer.    *    * @return raw data size    */
name|long
name|getRawDataSize
parameter_list|()
function_decl|;
comment|/**    * Return the number of rows in file. Row count gets updated when flushing    * the stripes. To get accurate row count this method should be called after    * closing the writer.    *    * @return row count    */
name|long
name|getNumberOfRows
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

