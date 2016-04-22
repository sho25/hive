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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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
name|thrift
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TBoolValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TByteValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TColumnValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TDoubleValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI16Value
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI32Value
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI64Value
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TStringValue
import|;
end_import

begin_comment
comment|/**  * Protocols before HIVE_CLI_SERVICE_PROTOCOL_V6 (used by RowBasedSet)  *  */
end_comment

begin_class
specifier|public
class|class
name|ColumnValue
block|{
specifier|private
specifier|static
name|TColumnValue
name|booleanValue
parameter_list|(
name|Boolean
name|value
parameter_list|)
block|{
name|TBoolValue
name|tBoolValue
init|=
operator|new
name|TBoolValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tBoolValue
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|boolVal
argument_list|(
name|tBoolValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|byteValue
parameter_list|(
name|Byte
name|value
parameter_list|)
block|{
name|TByteValue
name|tByteValue
init|=
operator|new
name|TByteValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tByteValue
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|byteVal
argument_list|(
name|tByteValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|shortValue
parameter_list|(
name|Short
name|value
parameter_list|)
block|{
name|TI16Value
name|tI16Value
init|=
operator|new
name|TI16Value
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tI16Value
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|i16Val
argument_list|(
name|tI16Value
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|intValue
parameter_list|(
name|Integer
name|value
parameter_list|)
block|{
name|TI32Value
name|tI32Value
init|=
operator|new
name|TI32Value
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tI32Value
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|i32Val
argument_list|(
name|tI32Value
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|longValue
parameter_list|(
name|Long
name|value
parameter_list|)
block|{
name|TI64Value
name|tI64Value
init|=
operator|new
name|TI64Value
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tI64Value
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|i64Val
argument_list|(
name|tI64Value
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|floatValue
parameter_list|(
name|Float
name|value
parameter_list|)
block|{
name|TDoubleValue
name|tDoubleValue
init|=
operator|new
name|TDoubleValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tDoubleValue
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|doubleVal
argument_list|(
name|tDoubleValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|doubleValue
parameter_list|(
name|Double
name|value
parameter_list|)
block|{
name|TDoubleValue
name|tDoubleValue
init|=
operator|new
name|TDoubleValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tDoubleValue
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|doubleVal
argument_list|(
name|tDoubleValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|TStringValue
name|tStringValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStringValue
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStringValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|HiveChar
name|value
parameter_list|)
block|{
name|TStringValue
name|tStringValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStringValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStringValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|HiveVarchar
name|value
parameter_list|)
block|{
name|TStringValue
name|tStringValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStringValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStringValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|dateValue
parameter_list|(
name|Date
name|value
parameter_list|)
block|{
name|TStringValue
name|tStringValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStringValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TColumnValue
argument_list|(
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStringValue
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|timestampValue
parameter_list|(
name|Timestamp
name|value
parameter_list|)
block|{
name|TStringValue
name|tStringValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStringValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStringValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|HiveDecimal
name|value
parameter_list|,
name|TypeDescriptor
name|typeDescriptor
parameter_list|)
block|{
name|TStringValue
name|tStrValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|int
name|scale
init|=
name|typeDescriptor
operator|.
name|getDecimalDigits
argument_list|()
decl_stmt|;
name|tStrValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toFormatString
argument_list|(
name|scale
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStrValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|HiveIntervalYearMonth
name|value
parameter_list|)
block|{
name|TStringValue
name|tStrValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStrValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStrValue
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TColumnValue
name|stringValue
parameter_list|(
name|HiveIntervalDayTime
name|value
parameter_list|)
block|{
name|TStringValue
name|tStrValue
init|=
operator|new
name|TStringValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|tStrValue
operator|.
name|setValue
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|TColumnValue
operator|.
name|stringVal
argument_list|(
name|tStrValue
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TColumnValue
name|toTColumnValue
parameter_list|(
name|TypeDescriptor
name|typeDescriptor
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|Type
name|type
init|=
name|typeDescriptor
operator|.
name|getType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
return|return
name|booleanValue
argument_list|(
operator|(
name|Boolean
operator|)
name|value
argument_list|)
return|;
case|case
name|TINYINT_TYPE
case|:
return|return
name|byteValue
argument_list|(
operator|(
name|Byte
operator|)
name|value
argument_list|)
return|;
case|case
name|SMALLINT_TYPE
case|:
return|return
name|shortValue
argument_list|(
operator|(
name|Short
operator|)
name|value
argument_list|)
return|;
case|case
name|INT_TYPE
case|:
return|return
name|intValue
argument_list|(
operator|(
name|Integer
operator|)
name|value
argument_list|)
return|;
case|case
name|BIGINT_TYPE
case|:
return|return
name|longValue
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
return|;
case|case
name|FLOAT_TYPE
case|:
return|return
name|floatValue
argument_list|(
operator|(
name|Float
operator|)
name|value
argument_list|)
return|;
case|case
name|DOUBLE_TYPE
case|:
return|return
name|doubleValue
argument_list|(
operator|(
name|Double
operator|)
name|value
argument_list|)
return|;
case|case
name|STRING_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
return|;
case|case
name|CHAR_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|HiveChar
operator|)
name|value
argument_list|)
return|;
case|case
name|VARCHAR_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|HiveVarchar
operator|)
name|value
argument_list|)
return|;
case|case
name|DATE_TYPE
case|:
return|return
name|dateValue
argument_list|(
operator|(
name|Date
operator|)
name|value
argument_list|)
return|;
case|case
name|TIMESTAMP_TYPE
case|:
return|return
name|timestampValue
argument_list|(
operator|(
name|Timestamp
operator|)
name|value
argument_list|)
return|;
case|case
name|INTERVAL_YEAR_MONTH_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|HiveIntervalYearMonth
operator|)
name|value
argument_list|)
return|;
case|case
name|INTERVAL_DAY_TIME_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|HiveIntervalDayTime
operator|)
name|value
argument_list|)
return|;
case|case
name|DECIMAL_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|HiveDecimal
operator|)
name|value
argument_list|,
name|typeDescriptor
argument_list|)
return|;
case|case
name|BINARY_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
return|;
case|case
name|ARRAY_TYPE
case|:
case|case
name|MAP_TYPE
case|:
case|case
name|STRUCT_TYPE
case|:
case|case
name|UNION_TYPE
case|:
case|case
name|USER_DEFINED_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
return|;
case|case
name|NULL_TYPE
case|:
return|return
name|stringValue
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|Boolean
name|getBooleanValue
parameter_list|(
name|TBoolValue
name|tBoolValue
parameter_list|)
block|{
if|if
condition|(
name|tBoolValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tBoolValue
operator|.
name|isValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Byte
name|getByteValue
parameter_list|(
name|TByteValue
name|tByteValue
parameter_list|)
block|{
if|if
condition|(
name|tByteValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tByteValue
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Short
name|getShortValue
parameter_list|(
name|TI16Value
name|tI16Value
parameter_list|)
block|{
if|if
condition|(
name|tI16Value
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tI16Value
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Integer
name|getIntegerValue
parameter_list|(
name|TI32Value
name|tI32Value
parameter_list|)
block|{
if|if
condition|(
name|tI32Value
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tI32Value
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Long
name|getLongValue
parameter_list|(
name|TI64Value
name|tI64Value
parameter_list|)
block|{
if|if
condition|(
name|tI64Value
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tI64Value
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Double
name|getDoubleValue
parameter_list|(
name|TDoubleValue
name|tDoubleValue
parameter_list|)
block|{
if|if
condition|(
name|tDoubleValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tDoubleValue
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|String
name|getStringValue
parameter_list|(
name|TStringValue
name|tStringValue
parameter_list|)
block|{
if|if
condition|(
name|tStringValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tStringValue
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Date
name|getDateValue
parameter_list|(
name|TStringValue
name|tStringValue
parameter_list|)
block|{
if|if
condition|(
name|tStringValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|Date
operator|.
name|valueOf
argument_list|(
name|tStringValue
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Timestamp
name|getTimestampValue
parameter_list|(
name|TStringValue
name|tStringValue
parameter_list|)
block|{
if|if
condition|(
name|tStringValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|tStringValue
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|getBinaryValue
parameter_list|(
name|TStringValue
name|tString
parameter_list|)
block|{
if|if
condition|(
name|tString
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
name|tString
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|BigDecimal
name|getBigDecimalValue
parameter_list|(
name|TStringValue
name|tStringValue
parameter_list|)
block|{
if|if
condition|(
name|tStringValue
operator|.
name|isSetValue
argument_list|()
condition|)
block|{
return|return
operator|new
name|BigDecimal
argument_list|(
name|tStringValue
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|Object
name|toColumnValue
parameter_list|(
name|TColumnValue
name|value
parameter_list|)
block|{
name|TColumnValue
operator|.
name|_Fields
name|field
init|=
name|value
operator|.
name|getSetField
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|BOOL_VAL
case|:
return|return
name|getBooleanValue
argument_list|(
name|value
operator|.
name|getBoolVal
argument_list|()
argument_list|)
return|;
case|case
name|BYTE_VAL
case|:
return|return
name|getByteValue
argument_list|(
name|value
operator|.
name|getByteVal
argument_list|()
argument_list|)
return|;
case|case
name|I16_VAL
case|:
return|return
name|getShortValue
argument_list|(
name|value
operator|.
name|getI16Val
argument_list|()
argument_list|)
return|;
case|case
name|I32_VAL
case|:
return|return
name|getIntegerValue
argument_list|(
name|value
operator|.
name|getI32Val
argument_list|()
argument_list|)
return|;
case|case
name|I64_VAL
case|:
return|return
name|getLongValue
argument_list|(
name|value
operator|.
name|getI64Val
argument_list|()
argument_list|)
return|;
case|case
name|DOUBLE_VAL
case|:
return|return
name|getDoubleValue
argument_list|(
name|value
operator|.
name|getDoubleVal
argument_list|()
argument_list|)
return|;
case|case
name|STRING_VAL
case|:
return|return
name|getStringValue
argument_list|(
name|value
operator|.
name|getStringVal
argument_list|()
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"never"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

