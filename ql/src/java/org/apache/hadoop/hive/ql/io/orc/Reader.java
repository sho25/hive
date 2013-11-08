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
name|List
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
name|ql
operator|.
name|io
operator|.
name|sarg
operator|.
name|SearchArgument
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_comment
comment|/**  * The interface for reading ORC files.  *  * One Reader can support multiple concurrent RecordReader.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Reader
block|{
comment|/**    * Get the number of rows in the file.    * @return the number of rows    */
name|long
name|getNumberOfRows
parameter_list|()
function_decl|;
comment|/**    * Get the deserialized data size of the file    * @return raw data size    */
name|long
name|getRawDataSize
parameter_list|()
function_decl|;
comment|/**    * Get the deserialized data size of the specified columns    * @param colNames    * @return raw data size of columns    */
name|long
name|getRawDataSizeOfColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|)
function_decl|;
comment|/**    * Get the user metadata keys.    * @return the set of metadata keys    */
name|Iterable
argument_list|<
name|String
argument_list|>
name|getMetadataKeys
parameter_list|()
function_decl|;
comment|/**    * Get a user metadata value.    * @param key a key given by the user    * @return the bytes associated with the given key    */
name|ByteBuffer
name|getMetadataValue
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Get the compression kind.    * @return the kind of compression in the file    */
name|CompressionKind
name|getCompression
parameter_list|()
function_decl|;
comment|/**    * Get the buffer size for the compression.    * @return number of bytes to buffer for the compression codec.    */
name|int
name|getCompressionSize
parameter_list|()
function_decl|;
comment|/**    * Get the number of rows per a entry in the row index.    * @return the number of rows per an entry in the row index or 0 if there    * is no row index.    */
name|int
name|getRowIndexStride
parameter_list|()
function_decl|;
comment|/**    * Get the list of stripes.    * @return the information about the stripes in order    */
name|Iterable
argument_list|<
name|StripeInformation
argument_list|>
name|getStripes
parameter_list|()
function_decl|;
comment|/**    * Get the object inspector for looking at the objects.    * @return an object inspector for each row returned    */
name|ObjectInspector
name|getObjectInspector
parameter_list|()
function_decl|;
comment|/**    * Get the length of the file.    * @return the number of bytes in the file    */
name|long
name|getContentLength
parameter_list|()
function_decl|;
comment|/**    * Get the statistics about the columns in the file.    * @return the information about the column    */
name|ColumnStatistics
index|[]
name|getStatistics
parameter_list|()
function_decl|;
comment|/**    * Get the metadata information like stripe level column statistics etc.    * @return the information about the column    * @throws IOException    */
name|Metadata
name|getMetadata
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the list of types contained in the file. The root type is the first    * type in the list.    * @return the list of flattened types    */
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|getTypes
parameter_list|()
function_decl|;
comment|/**    * Create a RecordReader that will scan the entire file.    * @param include true for each column that should be included    * @return A new RecordReader    * @throws IOException    */
name|RecordReader
name|rows
parameter_list|(
name|boolean
index|[]
name|include
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a RecordReader that will start reading at the first stripe after    * offset up to the stripe that starts at offset + length. This is intended    * to work with MapReduce's FileInputFormat where divisions are picked    * blindly, but they must cover all of the rows.    * @param offset a byte offset in the file    * @param length a number of bytes in the file    * @param include true for each column that should be included    * @return a new RecordReader that will read the specified rows.    * @throws IOException    * @deprecated    */
annotation|@
name|Deprecated
name|RecordReader
name|rows
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|,
name|boolean
index|[]
name|include
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a RecordReader that will read a section of a file. It starts reading    * at the first stripe after the offset and continues to the stripe that    * starts at offset + length. It also accepts a list of columns to read and a    * search argument.    * @param offset the minimum offset of the first stripe to read    * @param length the distance from offset of the first address to stop reading    *               at    * @param include true for each column that should be included    * @param sarg a search argument that limits the rows that should be read.    * @param neededColumns the names of the included columns    * @return the record reader for the rows    */
name|RecordReader
name|rows
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|,
name|boolean
index|[]
name|include
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|String
index|[]
name|neededColumns
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

