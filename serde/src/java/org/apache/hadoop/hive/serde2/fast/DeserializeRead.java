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
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|HiveDecimal
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
name|HiveIntervalDayTime
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
name|HiveIntervalYearMonth
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
name|HiveVarchar
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
name|HiveCharWritable
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
name|HiveVarcharWritable
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
name|typeinfo
operator|.
name|CharTypeInfo
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
name|DecimalTypeInfo
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
name|VarcharTypeInfo
import|;
end_import

begin_comment
comment|/*  * Directly deserialize with the caller reading field-by-field a serialization format.  *   * The caller is responsible for calling the read method for the right type of each field  * (after calling readCheckNull).  *   * Reading some fields require a results object to receive value information.  A separate  * results object is created by the caller at initialization per different field even for the same  * type.  *  * Some type values are by reference to either bytes in the deserialization buffer or to  * other type specific buffers.  So, those references are only valid until the next time set is  * called.  */
end_comment

begin_interface
specifier|public
interface|interface
name|DeserializeRead
block|{
comment|/*    * The primitive type information for all fields.    */
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
parameter_list|()
function_decl|;
comment|/*    * Set the range of bytes to be deserialized.    */
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
name|boolean
name|readCheckNull
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * Call this method after all fields have been read to check for extra fields.    */
name|void
name|extraFieldsCheck
parameter_list|()
function_decl|;
comment|/*    * Read integrity warning flags.    */
name|boolean
name|readBeyondConfiguredFieldsWarned
parameter_list|()
function_decl|;
name|boolean
name|readBeyondBufferRangeWarned
parameter_list|()
function_decl|;
name|boolean
name|bufferRangeHasExtraDataWarned
parameter_list|()
function_decl|;
comment|/*    * BOOLEAN.    */
name|boolean
name|readBoolean
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * BYTE.    */
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * SHORT.    */
name|short
name|readShort
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * INT.    */
name|int
name|readInt
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * LONG.    */
name|long
name|readLong
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * FLOAT.    */
name|float
name|readFloat
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * DOUBLE.    */
name|double
name|readDouble
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/*    * This class is the base abstract read bytes results for STRING, CHAR, VARCHAR, and BINARY.    */
specifier|public
specifier|abstract
class|class
name|ReadBytesResults
block|{
specifier|public
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|public
name|int
name|start
decl_stmt|;
specifier|public
name|int
name|length
decl_stmt|;
specifier|public
name|ReadBytesResults
parameter_list|()
block|{
name|bytes
operator|=
literal|null
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|/*    * STRING.    *    * Can be used to read CHAR and VARCHAR when the caller takes responsibility for    * truncation/padding issues.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadStringResults
extends|extends
name|ReadBytesResults
block|{
specifier|public
name|ReadStringResults
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Reading a STRING field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different bytes field.
name|ReadStringResults
name|createReadStringResults
parameter_list|()
function_decl|;
name|void
name|readString
parameter_list|(
name|ReadStringResults
name|readStringResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * CHAR.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadHiveCharResults
extends|extends
name|ReadBytesResults
block|{
specifier|private
name|CharTypeInfo
name|charTypeInfo
decl_stmt|;
specifier|private
name|int
name|maxLength
decl_stmt|;
specifier|protected
name|HiveCharWritable
name|hiveCharWritable
decl_stmt|;
specifier|public
name|ReadHiveCharResults
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|CharTypeInfo
name|charTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|charTypeInfo
operator|=
name|charTypeInfo
expr_stmt|;
name|this
operator|.
name|maxLength
operator|=
name|charTypeInfo
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|hiveCharWritable
operator|=
operator|new
name|HiveCharWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isInit
parameter_list|()
block|{
return|return
operator|(
name|charTypeInfo
operator|!=
literal|null
operator|)
return|;
block|}
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
return|return
name|maxLength
return|;
block|}
specifier|public
name|HiveChar
name|getHiveChar
parameter_list|()
block|{
return|return
name|hiveCharWritable
operator|.
name|getHiveChar
argument_list|()
return|;
block|}
block|}
comment|// Reading a CHAR field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different CHAR field.
name|ReadHiveCharResults
name|createReadHiveCharResults
parameter_list|()
function_decl|;
name|void
name|readHiveChar
parameter_list|(
name|ReadHiveCharResults
name|readHiveCharResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * VARCHAR.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadHiveVarcharResults
extends|extends
name|ReadBytesResults
block|{
specifier|private
name|VarcharTypeInfo
name|varcharTypeInfo
decl_stmt|;
specifier|private
name|int
name|maxLength
decl_stmt|;
specifier|protected
name|HiveVarcharWritable
name|hiveVarcharWritable
decl_stmt|;
specifier|public
name|ReadHiveVarcharResults
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|VarcharTypeInfo
name|varcharTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|varcharTypeInfo
operator|=
name|varcharTypeInfo
expr_stmt|;
name|this
operator|.
name|maxLength
operator|=
name|varcharTypeInfo
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|hiveVarcharWritable
operator|=
operator|new
name|HiveVarcharWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isInit
parameter_list|()
block|{
return|return
operator|(
name|varcharTypeInfo
operator|!=
literal|null
operator|)
return|;
block|}
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
return|return
name|maxLength
return|;
block|}
specifier|public
name|HiveVarchar
name|getHiveVarchar
parameter_list|()
block|{
return|return
name|hiveVarcharWritable
operator|.
name|getHiveVarchar
argument_list|()
return|;
block|}
block|}
comment|// Reading a VARCHAR field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different VARCHAR field.
name|ReadHiveVarcharResults
name|createReadHiveVarcharResults
parameter_list|()
function_decl|;
name|void
name|readHiveVarchar
parameter_list|(
name|ReadHiveVarcharResults
name|readHiveVarcharResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * BINARY.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadBinaryResults
extends|extends
name|ReadBytesResults
block|{
specifier|public
name|ReadBinaryResults
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Reading a BINARY field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different bytes field.
name|ReadBinaryResults
name|createReadBinaryResults
parameter_list|()
function_decl|;
name|void
name|readBinary
parameter_list|(
name|ReadBinaryResults
name|readBinaryResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * DATE.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadDateResults
block|{
specifier|protected
name|DateWritable
name|dateWritable
decl_stmt|;
specifier|public
name|ReadDateResults
parameter_list|()
block|{
name|dateWritable
operator|=
operator|new
name|DateWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Date
name|getDate
parameter_list|()
block|{
return|return
name|dateWritable
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|int
name|getDays
parameter_list|()
block|{
return|return
name|dateWritable
operator|.
name|getDays
argument_list|()
return|;
block|}
block|}
comment|// Reading a DATE field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different DATE field.
name|ReadDateResults
name|createReadDateResults
parameter_list|()
function_decl|;
name|void
name|readDate
parameter_list|(
name|ReadDateResults
name|readDateResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * TIMESTAMP.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadTimestampResults
block|{
specifier|protected
name|TimestampWritable
name|timestampWritable
decl_stmt|;
specifier|public
name|ReadTimestampResults
parameter_list|()
block|{
name|timestampWritable
operator|=
operator|new
name|TimestampWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestampWritable
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
block|}
comment|// Reading a TIMESTAMP field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different TIMESTAMP field.
name|ReadTimestampResults
name|createReadTimestampResults
parameter_list|()
function_decl|;
name|void
name|readTimestamp
parameter_list|(
name|ReadTimestampResults
name|readTimestampResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * INTERVAL_YEAR_MONTH.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadIntervalYearMonthResults
block|{
specifier|protected
name|HiveIntervalYearMonthWritable
name|hiveIntervalYearMonthWritable
decl_stmt|;
specifier|public
name|ReadIntervalYearMonthResults
parameter_list|()
block|{
name|hiveIntervalYearMonthWritable
operator|=
operator|new
name|HiveIntervalYearMonthWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonth
name|getHiveIntervalYearMonth
parameter_list|()
block|{
return|return
name|hiveIntervalYearMonthWritable
operator|.
name|getHiveIntervalYearMonth
argument_list|()
return|;
block|}
block|}
comment|// Reading a INTERVAL_YEAR_MONTH field require a results object to receive value information.
comment|// A separate results object is created at initialization per different INTERVAL_YEAR_MONTH field.
name|ReadIntervalYearMonthResults
name|createReadIntervalYearMonthResults
parameter_list|()
function_decl|;
name|void
name|readIntervalYearMonth
parameter_list|(
name|ReadIntervalYearMonthResults
name|readIntervalYearMonthResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * INTERVAL_DAY_TIME.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadIntervalDayTimeResults
block|{
specifier|protected
name|HiveIntervalDayTimeWritable
name|hiveIntervalDayTimeWritable
decl_stmt|;
specifier|public
name|ReadIntervalDayTimeResults
parameter_list|()
block|{
name|hiveIntervalDayTimeWritable
operator|=
operator|new
name|HiveIntervalDayTimeWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
name|getHiveIntervalDayTime
parameter_list|()
block|{
return|return
name|hiveIntervalDayTimeWritable
operator|.
name|getHiveIntervalDayTime
argument_list|()
return|;
block|}
block|}
comment|// Reading a INTERVAL_DAY_TIME field require a results object to receive value information.
comment|// A separate results object is created at initialization per different INTERVAL_DAY_TIME field.
name|ReadIntervalDayTimeResults
name|createReadIntervalDayTimeResults
parameter_list|()
function_decl|;
name|void
name|readIntervalDayTime
parameter_list|(
name|ReadIntervalDayTimeResults
name|readIntervalDayTimeResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/*    * DECIMAL.    */
comment|// This class is for abstract since each format may need its own specialization.
specifier|public
specifier|abstract
class|class
name|ReadDecimalResults
block|{
specifier|protected
name|DecimalTypeInfo
name|decimalTypeInfo
decl_stmt|;
specifier|public
name|ReadDecimalResults
parameter_list|()
block|{     }
specifier|public
name|void
name|init
parameter_list|(
name|DecimalTypeInfo
name|decimalTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|decimalTypeInfo
operator|=
name|decimalTypeInfo
expr_stmt|;
block|}
specifier|public
name|boolean
name|isInit
parameter_list|()
block|{
return|return
operator|(
name|decimalTypeInfo
operator|!=
literal|null
operator|)
return|;
block|}
specifier|public
specifier|abstract
name|HiveDecimal
name|getHiveDecimal
parameter_list|()
function_decl|;
block|}
comment|// Reading a DECIMAL field require a results object to receive value information.  A separate
comment|// results object is created at initialization per different DECIMAL field.
name|ReadDecimalResults
name|createReadDecimalResults
parameter_list|()
function_decl|;
name|void
name|readHiveDecimal
parameter_list|(
name|ReadDecimalResults
name|readDecimalResults
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

