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
name|parquet
operator|.
name|vector
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|bytes
operator|.
name|ByteBufferInputStream
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
name|type
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|column
operator|.
name|Dictionary
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * The interface to wrap the underlying Parquet dictionary and non dictionary encoded page reader.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ParquetDataColumnReader
block|{
comment|/**    * Initialize the reader by page data.    * @param valueCount value count    * @param in page data    * @throws IOException    */
name|void
name|initFromPage
parameter_list|(
name|int
name|valueCount
parameter_list|,
name|ByteBufferInputStream
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return the next Dictionary ID from the page    */
name|int
name|readValueDictionaryId
parameter_list|()
function_decl|;
comment|/**    * @return the next Long from the page    */
name|long
name|readLong
parameter_list|()
function_decl|;
comment|/**    * @return the next Integer from the page    * Though the function is looking for an integer, it will return the value through long.    * The type of data saved as long can be changed to be int or smallint or tinyint.  In that case    * the value returned to the user will depend on the data.  If the data value is within the valid    * range accommodated by the read type, the data will be returned as is.  When data is not within    * the valid range, a NULL will be returned.  A long value saved in parquet files will be    * returned asis to facilitate the validity check.  Also, the vectorized representation uses    * a LongColumnVector to store integer values.    */
name|long
name|readInteger
parameter_list|()
function_decl|;
comment|/**    * @return the next SmallInt from the page    */
name|long
name|readSmallInt
parameter_list|()
function_decl|;
comment|/**    * @return the next TinyInt from the page    */
name|long
name|readTinyInt
parameter_list|()
function_decl|;
comment|/**    * @return the next Float from the page    */
name|float
name|readFloat
parameter_list|()
function_decl|;
comment|/**    * @return the next Boolean from the page    */
name|boolean
name|readBoolean
parameter_list|()
function_decl|;
comment|/**    * @return the next String from the page    */
name|byte
index|[]
name|readString
parameter_list|()
function_decl|;
comment|/**    * @return the next Varchar from the page    */
name|byte
index|[]
name|readVarchar
parameter_list|()
function_decl|;
comment|/**    * @return the next Char from the page    */
name|byte
index|[]
name|readChar
parameter_list|()
function_decl|;
comment|/**    * @return the next Bytes from the page    */
name|byte
index|[]
name|readBytes
parameter_list|()
function_decl|;
comment|/**    * @return the next Decimal from the page    */
name|byte
index|[]
name|readDecimal
parameter_list|()
function_decl|;
comment|/**    * @return the next Double from the page    */
name|double
name|readDouble
parameter_list|()
function_decl|;
comment|/**    * @return the next Timestamp from the page    */
name|Timestamp
name|readTimestamp
parameter_list|()
function_decl|;
comment|/**    * @return is data valid    */
name|boolean
name|isValid
parameter_list|()
function_decl|;
comment|/**    * @return the underlying dictionary if current reader is dictionary encoded    */
name|Dictionary
name|getDictionary
parameter_list|()
function_decl|;
comment|/**    * @param id in dictionary    * @return the Bytes from the dictionary by id    */
name|byte
index|[]
name|readBytes
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Float from the dictionary by id    */
name|float
name|readFloat
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Double from the dictionary by id    */
name|double
name|readDouble
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Integer from the dictionary by id    * Though the function is looking for an integer, it will return the value through long.    * The type of data saved as long can be changed to be int or smallint or tinyint.  In that case    * the value returned to the user will depend on the data.  If the data value is within the valid    * range accommodated by the read type, the data will be returned as is.  When data is not within    * the valid range, a NULL will be returned.  A long value saved in parquet files will be    * returned asis to facilitate the validity check.  Also, the vectorized representation uses    * a LongColumnVector to store integer values.    */
name|long
name|readInteger
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Long from the dictionary by id    */
name|long
name|readLong
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Small Int from the dictionary by id    */
name|long
name|readSmallInt
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the tiny int from the dictionary by id    */
name|long
name|readTinyInt
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Boolean from the dictionary by id    */
name|boolean
name|readBoolean
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Decimal from the dictionary by id    */
name|byte
index|[]
name|readDecimal
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Timestamp from the dictionary by id    */
name|Timestamp
name|readTimestamp
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the String from the dictionary by id    */
name|byte
index|[]
name|readString
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Varchar from the dictionary by id    */
name|byte
index|[]
name|readVarchar
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
comment|/**    * @param id in dictionary    * @return the Char from the dictionary by id    */
name|byte
index|[]
name|readChar
parameter_list|(
name|int
name|id
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

