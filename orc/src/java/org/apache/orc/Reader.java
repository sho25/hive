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
comment|/**    * Get the deserialized data size of the specified columns ids    * @param colIds - internal column id (check orcfiledump for column ids)    * @return raw data size of columns    */
name|long
name|getRawDataSizeFromColIndices
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|colIds
parameter_list|)
function_decl|;
comment|/**    * Get the user metadata keys.    * @return the set of metadata keys    */
name|List
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
comment|/**    * Did the user set the given metadata value.    * @param key the key to check    * @return true if the metadata value was set    */
name|boolean
name|hasMetadataValue
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Get the compression kind.    * @return the kind of compression in the file    */
name|CompressionKind
name|getCompressionKind
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
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|getStripes
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
comment|/**    * Get the type of rows in this ORC file.    */
name|TypeDescription
name|getSchema
parameter_list|()
function_decl|;
comment|/**    * Get the list of types contained in the file. The root type is the first    * type in the list.    * @return the list of flattened types    * @deprecated use getSchema instead    */
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|getTypes
parameter_list|()
function_decl|;
comment|/**    * Get the file format version.    */
name|OrcFile
operator|.
name|Version
name|getFileVersion
parameter_list|()
function_decl|;
comment|/**    * Get the version of the writer of this file.    */
name|OrcFile
operator|.
name|WriterVersion
name|getWriterVersion
parameter_list|()
function_decl|;
comment|/**    * Options for creating a RecordReader.    */
specifier|public
specifier|static
class|class
name|Options
block|{
specifier|private
name|boolean
index|[]
name|include
decl_stmt|;
specifier|private
name|long
name|offset
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|length
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|SearchArgument
name|sarg
init|=
literal|null
decl_stmt|;
specifier|private
name|String
index|[]
name|columnNames
init|=
literal|null
decl_stmt|;
specifier|private
name|Boolean
name|useZeroCopy
init|=
literal|null
decl_stmt|;
specifier|private
name|Boolean
name|skipCorruptRecords
init|=
literal|null
decl_stmt|;
specifier|private
name|TypeDescription
name|schema
init|=
literal|null
decl_stmt|;
specifier|private
name|DataReader
name|dataReader
init|=
literal|null
decl_stmt|;
comment|/**      * Set the list of columns to read.      * @param include a list of columns to read      * @return this      */
specifier|public
name|Options
name|include
parameter_list|(
name|boolean
index|[]
name|include
parameter_list|)
block|{
name|this
operator|.
name|include
operator|=
name|include
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the range of bytes to read      * @param offset the starting byte offset      * @param length the number of bytes to read      * @return this      */
specifier|public
name|Options
name|range
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the schema on read type description.      */
specifier|public
name|Options
name|schema
parameter_list|(
name|TypeDescription
name|schema
parameter_list|)
block|{
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set search argument for predicate push down.      * @param sarg the search argument      * @param columnNames the column names for      * @return this      */
specifier|public
name|Options
name|searchArgument
parameter_list|(
name|SearchArgument
name|sarg
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|)
block|{
name|this
operator|.
name|sarg
operator|=
name|sarg
expr_stmt|;
name|this
operator|.
name|columnNames
operator|=
name|columnNames
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set whether to use zero copy from HDFS.      * @param value the new zero copy flag      * @return this      */
specifier|public
name|Options
name|useZeroCopy
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|useZeroCopy
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Options
name|dataReader
parameter_list|(
name|DataReader
name|value
parameter_list|)
block|{
name|this
operator|.
name|dataReader
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set whether to skip corrupt records.      * @param value the new skip corrupt records flag      * @return this      */
specifier|public
name|Options
name|skipCorruptRecords
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|skipCorruptRecords
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|boolean
index|[]
name|getInclude
parameter_list|()
block|{
return|return
name|include
return|;
block|}
specifier|public
name|long
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|length
return|;
block|}
specifier|public
name|TypeDescription
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
specifier|public
name|SearchArgument
name|getSearchArgument
parameter_list|()
block|{
return|return
name|sarg
return|;
block|}
specifier|public
name|String
index|[]
name|getColumnNames
parameter_list|()
block|{
return|return
name|columnNames
return|;
block|}
specifier|public
name|long
name|getMaxOffset
parameter_list|()
block|{
name|long
name|result
init|=
name|offset
operator|+
name|length
decl_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
name|result
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|Boolean
name|getUseZeroCopy
parameter_list|()
block|{
return|return
name|useZeroCopy
return|;
block|}
specifier|public
name|Boolean
name|getSkipCorruptRecords
parameter_list|()
block|{
return|return
name|skipCorruptRecords
return|;
block|}
specifier|public
name|DataReader
name|getDataReader
parameter_list|()
block|{
return|return
name|dataReader
return|;
block|}
specifier|public
name|Options
name|clone
parameter_list|()
block|{
name|Options
name|result
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|result
operator|.
name|include
operator|=
name|include
expr_stmt|;
name|result
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|result
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|result
operator|.
name|sarg
operator|=
name|sarg
expr_stmt|;
name|result
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|result
operator|.
name|columnNames
operator|=
name|columnNames
expr_stmt|;
name|result
operator|.
name|useZeroCopy
operator|=
name|useZeroCopy
expr_stmt|;
name|result
operator|.
name|skipCorruptRecords
operator|=
name|skipCorruptRecords
expr_stmt|;
name|result
operator|.
name|dataReader
operator|=
name|dataReader
operator|==
literal|null
condition|?
literal|null
else|:
name|dataReader
operator|.
name|clone
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"{include: "
argument_list|)
expr_stmt|;
if|if
condition|(
name|include
operator|==
literal|null
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|include
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
name|include
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|", offset: "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", length: "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|sarg
operator|!=
literal|null
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", sarg: "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|sarg
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", columns: ["
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnNames
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|columnNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", schema: "
argument_list|)
expr_stmt|;
name|schema
operator|.
name|printToBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * Create a RecordReader that reads everything with the default options.    * @return a new RecordReader    * @throws IOException    */
name|RecordReader
name|rows
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a RecordReader that uses the options given.    * This method can't be named rows, because many callers used rows(null)    * before the rows() method was introduced.    * @param options the options to read with    * @return a new RecordReader    * @throws IOException    */
name|RecordReader
name|rows
parameter_list|(
name|Options
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return List of integers representing version of the file, in order from major to minor.    */
name|List
argument_list|<
name|Integer
argument_list|>
name|getVersionList
parameter_list|()
function_decl|;
comment|/**    * @return Gets the size of metadata, in bytes.    */
name|int
name|getMetadataSize
parameter_list|()
function_decl|;
comment|/**    * @return Stripe statistics, in original protobuf form.    */
name|List
argument_list|<
name|OrcProto
operator|.
name|StripeStatistics
argument_list|>
name|getOrcProtoStripeStatistics
parameter_list|()
function_decl|;
comment|/**    * @return Stripe statistics.    */
name|List
argument_list|<
name|StripeStatistics
argument_list|>
name|getStripeStatistics
parameter_list|()
function_decl|;
comment|/**    * @return File statistics, in original protobuf form.    */
name|List
argument_list|<
name|OrcProto
operator|.
name|ColumnStatistics
argument_list|>
name|getOrcProtoFileStatistics
parameter_list|()
function_decl|;
comment|/**    * @return Serialized file metadata read from disk for the purposes of caching, etc.    */
name|ByteBuffer
name|getSerializedFileFooter
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

