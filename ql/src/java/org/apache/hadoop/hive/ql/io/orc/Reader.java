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
extends|extends
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|Reader
block|{
comment|/**    * Get the object inspector for looking at the objects.    * @return an object inspector for each row returned    */
name|ObjectInspector
name|getObjectInspector
parameter_list|()
function_decl|;
comment|/**    * Get the Compression kind in the compatibility mode.    */
name|CompressionKind
name|getCompression
parameter_list|()
function_decl|;
comment|/**    * Create a RecordReader that reads everything with the default options.    * @return a new RecordReader    * @throws IOException    */
name|RecordReader
name|rows
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a RecordReader that reads everything with the given options.    * @param options the options to use    * @return a new RecordReader    * @throws IOException    */
name|RecordReader
name|rowsOptions
parameter_list|(
name|Options
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a RecordReader that will scan the entire file.    * This is a legacy method and rowsOptions is preferred.    * @param include true for each column that should be included    * @return A new RecordReader    * @throws IOException    */
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
comment|/**    * Create a RecordReader that will start reading at the first stripe after    * offset up to the stripe that starts at offset + length. This is intended    * to work with MapReduce's FileInputFormat where divisions are picked    * blindly, but they must cover all of the rows.    * This is a legacy method and rowsOptions is preferred.    * @param offset a byte offset in the file    * @param length a number of bytes in the file    * @param include true for each column that should be included    * @return a new RecordReader that will read the specified rows.    * @throws IOException    */
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
comment|/**    * Create a RecordReader that will read a section of a file. It starts reading    * at the first stripe after the offset and continues to the stripe that    * starts at offset + length. It also accepts a list of columns to read and a    * search argument.    * This is a legacy method and rowsOptions is preferred.    * @param offset the minimum offset of the first stripe to read    * @param length the distance from offset of the first address to stop reading    *               at    * @param include true for each column that should be included    * @param sarg a search argument that limits the rows that should be read.    * @param neededColumns the names of the included columns    * @return the record reader for the rows    */
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

