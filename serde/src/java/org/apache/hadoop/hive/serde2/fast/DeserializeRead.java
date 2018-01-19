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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|DataTypePhysicalVariation
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|StructTypeInfo
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
name|UnionTypeInfo
import|;
end_import

begin_comment
comment|/*  * Directly deserialize with the caller reading field-by-field a serialization format.  *  * The caller is responsible for calling the read method for the right type of each field  * (after calling readNextField).  *  * Reading some fields require a results object to receive value information.  A separate  * results object is created by the caller at initialization per different field even for the same  * type.  *  * Some type values are by reference to either bytes in the deserialization buffer or to  * other type specific buffers.  So, those references are only valid until the next time set is  * called.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DeserializeRead
block|{
specifier|protected
specifier|final
name|TypeInfo
index|[]
name|typeInfos
decl_stmt|;
comment|// NOTE: Currently, read variations only apply to top level data types...
specifier|protected
name|DataTypePhysicalVariation
index|[]
name|dataTypePhysicalVariations
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|useExternalBuffer
decl_stmt|;
specifier|protected
specifier|final
name|Category
index|[]
name|categories
decl_stmt|;
specifier|protected
specifier|final
name|PrimitiveCategory
index|[]
name|primitiveCategories
decl_stmt|;
comment|/*    * This class is used to read one field at a time.  Simple fields like long, double, int are read    * into to primitive current* members; the non-simple field types like Date, Timestamp, etc, are    * read into a current object that this method will allocate.    *    * This method handles complex type fields by recursively calling this method.    */
specifier|private
name|void
name|allocateCurrentWritable
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
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
break|break;
case|case
name|LIST
case|:
name|allocateCurrentWritable
argument_list|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|allocateCurrentWritable
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|allocateCurrentWritable
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
for|for
control|(
name|TypeInfo
name|fieldTypeInfo
range|:
operator|(
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
control|)
block|{
name|allocateCurrentWritable
argument_list|(
name|fieldTypeInfo
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
for|for
control|(
name|TypeInfo
name|fieldTypeInfo
range|:
operator|(
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
control|)
block|{
name|allocateCurrentWritable
argument_list|(
name|fieldTypeInfo
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected category "
operator|+
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Constructor.    *    * When useExternalBuffer is specified true and readNextField reads a string/char/varchar/binary    * field, it will request an external buffer to receive the data of format conversion.    *    * if (deserializeRead.readNextField()) {    *   if (deserializeRead.currentExternalBufferNeeded) {    *&lt;Ensure external buffer is as least deserializeRead.currentExternalBufferNeededLen bytes&gt;    *     deserializeRead.copyToExternalBuffer(externalBuffer, externalBufferStart);    *   } else {    *&lt;Otherwise, field data is available in the currentBytes, currentBytesStart, and    *      currentBytesLength of deserializeRead&gt;    *   }    *    * @param typeInfos    * @param dataTypePhysicalVariations    *                            Specify for each corresponding TypeInfo a read variation. Can be    *                            null.  dataTypePhysicalVariation.NONE is then assumed.    * @param useExternalBuffer   Specify true when the caller is prepared to provide a bytes buffer    *                            to receive a string/char/varchar/binary field that needs format    *                            conversion.    */
specifier|public
name|DeserializeRead
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|DataTypePhysicalVariation
index|[]
name|dataTypePhysicalVariations
parameter_list|,
name|boolean
name|useExternalBuffer
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
if|if
condition|(
name|dataTypePhysicalVariations
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|dataTypePhysicalVariations
operator|=
name|dataTypePhysicalVariations
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|dataTypePhysicalVariations
operator|=
operator|new
name|DataTypePhysicalVariation
index|[
name|count
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|this
operator|.
name|dataTypePhysicalVariations
argument_list|,
name|DataTypePhysicalVariation
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
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
block|}
name|allocateCurrentWritable
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|useExternalBuffer
operator|=
name|useExternalBuffer
expr_stmt|;
block|}
specifier|public
name|DeserializeRead
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|boolean
name|useExternalBuffer
parameter_list|)
block|{
name|this
argument_list|(
name|typeInfos
argument_list|,
literal|null
argument_list|,
name|useExternalBuffer
argument_list|)
expr_stmt|;
block|}
comment|// Don't allow for public.
specifier|protected
name|DeserializeRead
parameter_list|()
block|{
comment|// Initialize to satisfy compiler finals.
name|typeInfos
operator|=
literal|null
expr_stmt|;
name|useExternalBuffer
operator|=
literal|false
expr_stmt|;
name|categories
operator|=
literal|null
expr_stmt|;
name|primitiveCategories
operator|=
literal|null
expr_stmt|;
block|}
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
comment|/*    * Get optional read variations for fields.    */
specifier|public
name|DataTypePhysicalVariation
index|[]
name|getDataTypePhysicalVariations
parameter_list|()
block|{
return|return
name|dataTypePhysicalVariations
return|;
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
comment|/*    * Reads the the next field.    *    * Afterwards, reading is positioned to the next field.    *    * @return  Return true when the field was not null and data is put in the appropriate    *          current* member.    *          Otherwise, false when the field is null.    *    */
specifier|public
specifier|abstract
name|boolean
name|readNextField
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Reads through an undesired field.    *    * No data values are valid after this call.    * Designed for skipping columns that are not included.    */
specifier|public
specifier|abstract
name|void
name|skipNextField
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Returns true if the readField method is supported;    */
specifier|public
name|boolean
name|isReadFieldSupported
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/*    * When supported, read a field by field number (i.e. random access).    *    * Currently, only LazySimpleDeserializeRead supports this.    *    * @return  Return true when the field was not null and data is put in the appropriate    *          current* member.    *          Otherwise, false when the field is null.    */
specifier|public
name|boolean
name|readField
parameter_list|(
name|int
name|fieldIndex
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
comment|/*    * Tests whether there is another List element or another Map key/value pair.    */
specifier|public
specifier|abstract
name|boolean
name|isNextComplexMultiValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Read a field that is under a complex type.  It may be a primitive type or deeper complex type.    */
specifier|public
specifier|abstract
name|boolean
name|readComplexField
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Used by Struct and Union complex type readers to indicate the (final) field has been fully    * read and the current complex type is finished.    */
specifier|public
specifier|abstract
name|void
name|finishComplexVariableFieldsType
parameter_list|()
function_decl|;
comment|/*    * Call this method may be called after all the all fields have been read to check    * for unread fields.    *    * Note that when optimizing reading to stop reading unneeded include columns, worrying    * about whether all data is consumed is not appropriate (often we aren't reading it all by    * design).    *    * Since LazySimpleDeserializeRead parses the line through the last desired column it does    * support this function.    */
specifier|public
specifier|abstract
name|boolean
name|isEndOfInputReached
parameter_list|()
function_decl|;
comment|/*    * Get detailed read position information to help diagnose exceptions.    */
specifier|public
specifier|abstract
name|String
name|getDetailedReadPositionString
parameter_list|()
function_decl|;
comment|/*    * These members hold the current value that was read when readNextField return false.    */
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
comment|/*    * STRING, CHAR, VARCHAR, and BINARY.    *    * For CHAR and VARCHAR when the caller takes responsibility for    * truncation/padding issues.    *    * When currentExternalBufferNeeded is true, conversion is needed into an external buffer of    * at least currentExternalBufferNeededLen bytes.  Use copyToExternalBuffer to get the result.    *    * Otherwise, currentBytes, currentBytesStart, and currentBytesLength are the result.    */
specifier|public
name|boolean
name|currentExternalBufferNeeded
decl_stmt|;
specifier|public
name|int
name|currentExternalBufferNeededLen
decl_stmt|;
specifier|public
name|void
name|copyToExternalBuffer
parameter_list|(
name|byte
index|[]
name|externalBuffer
parameter_list|,
name|int
name|externalBufferStart
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
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
comment|/*    * DECIMAL_64.    */
specifier|public
name|long
name|currentDecimal64
decl_stmt|;
block|}
end_class

end_unit

