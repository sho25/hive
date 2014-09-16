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
name|api
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
name|common
operator|.
name|type
operator|.
name|Decimal128
import|;
end_import

begin_interface
specifier|public
interface|interface
name|Vector
block|{
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|LONG
argument_list|(
literal|0
argument_list|)
block|,
name|DOUBLE
argument_list|(
literal|1
argument_list|)
block|,
name|DECIMAL
argument_list|(
literal|2
argument_list|)
block|,
name|BINARY
argument_list|(
literal|3
argument_list|)
block|;
specifier|private
name|byte
name|value
decl_stmt|;
specifier|private
name|Type
parameter_list|(
name|int
name|value
parameter_list|)
block|{
assert|assert
name|value
operator|>=
name|Byte
operator|.
name|MIN_VALUE
operator|&&
name|value
operator|<=
name|Byte
operator|.
name|MAX_VALUE
assert|;
name|this
operator|.
name|value
operator|=
operator|(
name|byte
operator|)
name|value
expr_stmt|;
block|}
specifier|public
name|byte
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
comment|/**    * @return Number of columns in this vector.    */
specifier|public
name|int
name|getNumberOfColumns
parameter_list|()
function_decl|;
comment|/**    * @return Number of rows in this vector.    */
specifier|public
name|int
name|getNumberOfRows
parameter_list|()
function_decl|;
comment|/**    * Prepare vector to read values at a particular offset, from particular column.    * @param colIx Column index.    * @param rowOffset Row offset inside vector.    */
specifier|public
name|ColumnReader
name|next
parameter_list|(
name|int
name|colIx
parameter_list|,
name|int
name|rowCount
parameter_list|)
function_decl|;
specifier|public
interface|interface
name|ColumnReader
block|{
comment|/**      * @return Whether there's a run on a single value in a row range for a column.      */
specifier|public
name|boolean
name|isSameValue
parameter_list|()
function_decl|;
comment|/**      * @return Whether there are any nulls in a row range for a column.      */
specifier|public
name|boolean
name|hasNulls
parameter_list|()
function_decl|;
comment|/**      * @return Long value from a specific cell.      */
specifier|public
name|long
name|getLong
parameter_list|()
function_decl|;
comment|/**      * Extracts long values from a row range for a column. Can set any value for nulls.      * @param dest Destination array.      * @param isNulls      * @param offset Offset to write to dest from.      * @param rowCount Row (cell) count to extract.      */
specifier|public
name|void
name|copyLongs
parameter_list|(
name|long
index|[]
name|dest
parameter_list|,
name|boolean
index|[]
name|isNulls
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|copyDoubles
parameter_list|(
name|double
index|[]
name|dest
parameter_list|,
name|boolean
index|[]
name|isNull
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * @return Double value from a specific cell.      */
specifier|public
name|double
name|getDouble
parameter_list|()
function_decl|;
comment|/**      * @return Decimal value from a specific cell.      */
specifier|public
name|Decimal128
name|getDecimal
parameter_list|()
function_decl|;
specifier|public
name|void
name|copyDecimals
parameter_list|(
name|Decimal128
index|[]
name|dest
parameter_list|,
name|boolean
index|[]
name|isNull
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * @return byte[] value from a specific cell.      */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
function_decl|;
comment|/**      * Extracts byte[] values from a row range for a column. Can set any value for nulls.      * @param dest Destination array of base arrays for values.      * @param destStarts Destination array start offsets (to define slices of base arrays).      * @param destLengths Destination array value lengths (to define slices of base arrays).      * @param offset Offset to write to dest from.      * @param rowCount Row (cell) count to extract.      */
specifier|public
name|void
name|copyBytes
parameter_list|(
name|byte
index|[]
index|[]
name|dest
parameter_list|,
name|int
index|[]
name|destStarts
parameter_list|,
name|int
index|[]
name|destLengths
parameter_list|,
name|boolean
index|[]
name|isNull
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_interface

end_unit

