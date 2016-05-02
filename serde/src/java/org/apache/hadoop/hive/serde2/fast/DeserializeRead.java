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
name|serde2
operator|.
name|fast
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
name|serde2
operator|.
name|io
operator|.
name|DateWritable
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
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|HiveIntervalDayTimeWritable
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
name|io
operator|.
name|HiveIntervalYearMonthWritable
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
name|io
operator|.
name|TimestampWritable
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
operator|.
name|Category
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_comment
comment|/*  * Directly deserialize with the caller reading field-by-field a serialization format.  *  * The caller is responsible for calling the read method for the right type of each field  * (after calling readCheckNull).  *  * Reading some fields require a results object to receive value information.  A separate  * results object is created by the caller at initialization per different field even for the same  * type.  *  * Some type values are by reference to either bytes in the deserialization buffer or to  * other type specific buffers.  So, those references are only valid until the next time set is  * called.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DeserializeRead
block|{
specifier|protected
name|TypeInfo
index|[]
name|typeInfos
decl_stmt|;
specifier|protected
name|boolean
index|[]
name|columnsToInclude
decl_stmt|;
specifier|protected
name|Category
index|[]
name|categories
decl_stmt|;
specifier|protected
name|PrimitiveCategory
index|[]
name|primitiveCategories
decl_stmt|;
specifier|public
name|DeserializeRead
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|)
block|{
name|this
operator|.
name|typeInfos
operator|=
name|typeInfos
expr_stmt|;
specifier|final
name|int
name|count
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|categories
operator|=
operator|new
name|Category
index|[
name|count
index|]
expr_stmt|;
name|primitiveCategories
operator|=
operator|new
name|PrimitiveCategory
index|[
name|count
index|]
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|typeInfos
index|[
name|i
index|]
decl_stmt|;
name|Category
name|category
init|=
name|typeInfo
operator|.
name|getCategory
argument_list|()
decl_stmt|;
name|categories
index|[
name|i
index|]
operator|=
name|category
expr_stmt|;
if|if
condition|(
name|category
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|PrimitiveCategory
name|primitiveCategory
init|=
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|primitiveCategories
index|[
name|i
index|]
operator|=
name|primitiveCategory
expr_stmt|;
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
case|case
name|DATE
case|:
if|if
condition|(
name|currentDateWritable
operator|==
literal|null
condition|)
block|{
name|currentDateWritable
operator|=
operator|new
name|DateWritable
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|TIMESTAMP
case|:
if|if
condition|(
name|currentTimestampWritable
operator|==
literal|null
condition|)
block|{
name|currentTimestampWritable
operator|=
operator|new
name|TimestampWritable
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
if|if
condition|(
name|currentHiveIntervalYearMonthWritable
operator|==
literal|null
condition|)
block|{
name|currentHiveIntervalYearMonthWritable
operator|=
operator|new
name|HiveIntervalYearMonthWritable
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
if|if
condition|(
name|currentHiveIntervalDayTimeWritable
operator|==
literal|null
condition|)
block|{
name|currentHiveIntervalDayTimeWritable
operator|=
operator|new
name|HiveIntervalDayTimeWritable
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL
case|:
if|if
condition|(
name|currentHiveDecimalWritable
operator|==
literal|null
condition|)
block|{
name|currentHiveDecimalWritable
operator|=
operator|new
name|HiveDecimalWritable
argument_list|()
expr_stmt|;
block|}
break|break;
default|default:
comment|// No writable needed for this data type.
block|}
block|}
block|}
name|columnsToInclude
operator|=
literal|null
expr_stmt|;
block|}
comment|// Don't allow for public.
specifier|protected
name|DeserializeRead
parameter_list|()
block|{   }
comment|/*    * The type information for all fields.    */
specifier|public
name|TypeInfo
index|[]
name|typeInfos
parameter_list|()
block|{
return|return
name|typeInfos
return|;
block|}
comment|/*    * If some fields are are not going to be used by the query, use this routine to specify    * the columns to return.  The readCheckNull method will automatically return NULL for the    * other columns.    */
specifier|public
name|void
name|setColumnsToInclude
parameter_list|(
name|boolean
index|[]
name|columnsToInclude
parameter_list|)
block|{
name|this
operator|.
name|columnsToInclude
operator|=
name|columnsToInclude
expr_stmt|;
block|}
comment|/*    * Set the range of bytes to be deserialized.    */
specifier|public
specifier|abstract
name|void
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/*    * Reads the NULL information for a field.    *    * @return Return true when the field is NULL; reading is positioned to the next field.    *         Otherwise, false when the field is NOT NULL; reading is positioned to the field data.    */
specifier|public
specifier|abstract
name|boolean
name|readCheckNull
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Call this method after all fields have been read to check for extra fields.    */
specifier|public
specifier|abstract
name|void
name|extraFieldsCheck
parameter_list|()
function_decl|;
comment|/*    * Read integrity warning flags.    */
specifier|public
specifier|abstract
name|boolean
name|readBeyondConfiguredFieldsWarned
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|readBeyondBufferRangeWarned
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|bufferRangeHasExtraDataWarned
parameter_list|()
function_decl|;
comment|/*    * These members hold the current value that was read when readCheckNull return false.    */
comment|/*    * BOOLEAN.    */
specifier|public
name|boolean
name|currentBoolean
decl_stmt|;
comment|/*    * BYTE.    */
specifier|public
name|byte
name|currentByte
decl_stmt|;
comment|/*    * SHORT.    */
specifier|public
name|short
name|currentShort
decl_stmt|;
comment|/*    * INT.    */
specifier|public
name|int
name|currentInt
decl_stmt|;
comment|/*    * LONG.    */
specifier|public
name|long
name|currentLong
decl_stmt|;
comment|/*    * FLOAT.    */
specifier|public
name|float
name|currentFloat
decl_stmt|;
comment|/*    * DOUBLE.    */
specifier|public
name|double
name|currentDouble
decl_stmt|;
comment|/*    * STRING, CHAR, VARCHAR, and BINARY.    *    * For CHAR and VARCHAR when the caller takes responsibility for    * truncation/padding issues.    */
specifier|public
name|byte
index|[]
name|currentBytes
decl_stmt|;
specifier|public
name|int
name|currentBytesStart
decl_stmt|;
specifier|public
name|int
name|currentBytesLength
decl_stmt|;
comment|/*    * DATE.    */
specifier|public
name|DateWritable
name|currentDateWritable
decl_stmt|;
comment|/*    * TIMESTAMP.    */
specifier|public
name|TimestampWritable
name|currentTimestampWritable
decl_stmt|;
comment|/*    * INTERVAL_YEAR_MONTH.    */
specifier|public
name|HiveIntervalYearMonthWritable
name|currentHiveIntervalYearMonthWritable
decl_stmt|;
comment|/*    * INTERVAL_DAY_TIME.    */
specifier|public
name|HiveIntervalDayTimeWritable
name|currentHiveIntervalDayTimeWritable
decl_stmt|;
comment|/*    * DECIMAL.    */
specifier|public
name|HiveDecimalWritable
name|currentHiveDecimalWritable
decl_stmt|;
block|}
end_class

end_unit

