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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|fast
operator|.
name|DeserializeRead
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
name|fast
operator|.
name|SerializeWrite
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * TestBinarySortableSerDe.  *  */
end_comment

begin_class
specifier|public
class|class
name|VerifyFast
block|{
specifier|public
specifier|static
name|void
name|verifyDeserializeRead
parameter_list|(
name|DeserializeRead
name|deserializeRead
parameter_list|,
name|PrimitiveTypeInfo
name|primitiveTypeInfo
parameter_list|,
name|Object
name|object
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isNull
decl_stmt|;
name|isNull
operator|=
name|deserializeRead
operator|.
name|readCheckNull
argument_list|()
expr_stmt|;
if|if
condition|(
name|isNull
condition|)
block|{
if|if
condition|(
name|object
operator|!=
literal|null
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Field reports null but object is not null"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
elseif|else
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Field report not null but object is null"
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|boolean
name|value
init|=
name|deserializeRead
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Boolean
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Boolean expected object not Boolean"
argument_list|)
expr_stmt|;
block|}
name|Boolean
name|expected
init|=
operator|(
name|Boolean
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Boolean field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|BYTE
case|:
block|{
name|byte
name|value
init|=
name|deserializeRead
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Byte
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Byte expected object not Byte"
argument_list|)
expr_stmt|;
block|}
name|Byte
name|expected
init|=
operator|(
name|Byte
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Byte field mismatch (expected "
operator|+
operator|(
name|int
operator|)
name|expected
operator|+
literal|" found "
operator|+
operator|(
name|int
operator|)
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|SHORT
case|:
block|{
name|short
name|value
init|=
name|deserializeRead
operator|.
name|readShort
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Short
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Short expected object not Short"
argument_list|)
expr_stmt|;
block|}
name|Short
name|expected
init|=
operator|(
name|Short
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Short field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INT
case|:
block|{
name|int
name|value
init|=
name|deserializeRead
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Integer
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Integer expected object not Integer"
argument_list|)
expr_stmt|;
block|}
name|Integer
name|expected
init|=
operator|(
name|Integer
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Int field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|LONG
case|:
block|{
name|long
name|value
init|=
name|deserializeRead
operator|.
name|readLong
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Long
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Long expected object not Long"
argument_list|)
expr_stmt|;
block|}
name|Long
name|expected
init|=
operator|(
name|Long
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Long field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|FLOAT
case|:
block|{
name|float
name|value
init|=
name|deserializeRead
operator|.
name|readFloat
argument_list|()
decl_stmt|;
name|Float
name|expected
init|=
operator|(
name|Float
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Float
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Float expected object not Float"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Float field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DOUBLE
case|:
block|{
name|double
name|value
init|=
name|deserializeRead
operator|.
name|readDouble
argument_list|()
decl_stmt|;
name|Double
name|expected
init|=
operator|(
name|Double
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|object
operator|instanceof
name|Double
operator|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Double expected object not Double"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
name|expected
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Double field mismatch (expected "
operator|+
name|expected
operator|+
literal|" found "
operator|+
name|value
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|STRING
case|:
block|{
name|DeserializeRead
operator|.
name|ReadStringResults
name|readStringResults
init|=
name|deserializeRead
operator|.
name|createReadStringResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readString
argument_list|(
name|readStringResults
argument_list|)
expr_stmt|;
name|byte
index|[]
name|stringBytes
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|readStringResults
operator|.
name|bytes
argument_list|,
name|readStringResults
operator|.
name|start
argument_list|,
name|readStringResults
operator|.
name|start
operator|+
name|readStringResults
operator|.
name|length
argument_list|)
decl_stmt|;
name|Text
name|text
init|=
operator|new
name|Text
argument_list|(
name|stringBytes
argument_list|)
decl_stmt|;
name|String
name|string
init|=
name|text
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|expected
init|=
operator|(
name|String
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|string
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"String field mismatch (expected '"
operator|+
name|expected
operator|+
literal|"' found '"
operator|+
name|string
operator|+
literal|"')"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|CHAR
case|:
block|{
name|DeserializeRead
operator|.
name|ReadHiveCharResults
name|readHiveCharResults
init|=
name|deserializeRead
operator|.
name|createReadHiveCharResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readHiveChar
argument_list|(
name|readHiveCharResults
argument_list|)
expr_stmt|;
name|HiveChar
name|hiveChar
init|=
name|readHiveCharResults
operator|.
name|getHiveChar
argument_list|()
decl_stmt|;
name|HiveChar
name|expected
init|=
operator|(
name|HiveChar
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|hiveChar
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Char field mismatch (expected '"
operator|+
name|expected
operator|+
literal|"' found '"
operator|+
name|hiveChar
operator|+
literal|"')"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|VARCHAR
case|:
block|{
name|DeserializeRead
operator|.
name|ReadHiveVarcharResults
name|readHiveVarcharResults
init|=
name|deserializeRead
operator|.
name|createReadHiveVarcharResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readHiveVarchar
argument_list|(
name|readHiveVarcharResults
argument_list|)
expr_stmt|;
name|HiveVarchar
name|hiveVarchar
init|=
name|readHiveVarcharResults
operator|.
name|getHiveVarchar
argument_list|()
decl_stmt|;
name|HiveVarchar
name|expected
init|=
operator|(
name|HiveVarchar
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|hiveVarchar
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Varchar field mismatch (expected '"
operator|+
name|expected
operator|+
literal|"' found '"
operator|+
name|hiveVarchar
operator|+
literal|"')"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DECIMAL
case|:
block|{
name|DeserializeRead
operator|.
name|ReadDecimalResults
name|readDecimalResults
init|=
name|deserializeRead
operator|.
name|createReadDecimalResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readHiveDecimal
argument_list|(
name|readDecimalResults
argument_list|)
expr_stmt|;
name|HiveDecimal
name|value
init|=
name|readDecimalResults
operator|.
name|getHiveDecimal
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Decimal field evaluated to NULL"
argument_list|)
expr_stmt|;
block|}
name|HiveDecimal
name|expected
init|=
operator|(
name|HiveDecimal
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|DecimalTypeInfo
name|decimalTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|primitiveTypeInfo
decl_stmt|;
name|int
name|precision
init|=
name|decimalTypeInfo
operator|.
name|getPrecision
argument_list|()
decl_stmt|;
name|int
name|scale
init|=
name|decimalTypeInfo
operator|.
name|getScale
argument_list|()
decl_stmt|;
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Decimal field mismatch (expected "
operator|+
name|expected
operator|.
name|toString
argument_list|()
operator|+
literal|" found "
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|") precision "
operator|+
name|precision
operator|+
literal|", scale "
operator|+
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DATE
case|:
block|{
name|DeserializeRead
operator|.
name|ReadDateResults
name|readDateResults
init|=
name|deserializeRead
operator|.
name|createReadDateResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readDate
argument_list|(
name|readDateResults
argument_list|)
expr_stmt|;
name|Date
name|value
init|=
name|readDateResults
operator|.
name|getDate
argument_list|()
decl_stmt|;
name|Date
name|expected
init|=
operator|(
name|Date
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Date field mismatch (expected "
operator|+
name|expected
operator|.
name|toString
argument_list|()
operator|+
literal|" found "
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|TIMESTAMP
case|:
block|{
name|DeserializeRead
operator|.
name|ReadTimestampResults
name|readTimestampResults
init|=
name|deserializeRead
operator|.
name|createReadTimestampResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readTimestamp
argument_list|(
name|readTimestampResults
argument_list|)
expr_stmt|;
name|Timestamp
name|value
init|=
name|readTimestampResults
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|Timestamp
name|expected
init|=
operator|(
name|Timestamp
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Timestamp field mismatch (expected "
operator|+
name|expected
operator|.
name|toString
argument_list|()
operator|+
literal|" found "
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
block|{
name|DeserializeRead
operator|.
name|ReadIntervalYearMonthResults
name|readIntervalYearMonthResults
init|=
name|deserializeRead
operator|.
name|createReadIntervalYearMonthResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readIntervalYearMonth
argument_list|(
name|readIntervalYearMonthResults
argument_list|)
expr_stmt|;
name|HiveIntervalYearMonth
name|value
init|=
name|readIntervalYearMonthResults
operator|.
name|getHiveIntervalYearMonth
argument_list|()
decl_stmt|;
name|HiveIntervalYearMonth
name|expected
init|=
operator|(
name|HiveIntervalYearMonth
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"HiveIntervalYearMonth field mismatch (expected "
operator|+
name|expected
operator|.
name|toString
argument_list|()
operator|+
literal|" found "
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
block|{
name|DeserializeRead
operator|.
name|ReadIntervalDayTimeResults
name|readIntervalDayTimeResults
init|=
name|deserializeRead
operator|.
name|createReadIntervalDayTimeResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readIntervalDayTime
argument_list|(
name|readIntervalDayTimeResults
argument_list|)
expr_stmt|;
name|HiveIntervalDayTime
name|value
init|=
name|readIntervalDayTimeResults
operator|.
name|getHiveIntervalDayTime
argument_list|()
decl_stmt|;
name|HiveIntervalDayTime
name|expected
init|=
operator|(
name|HiveIntervalDayTime
operator|)
name|object
decl_stmt|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"HiveIntervalDayTime field mismatch (expected "
operator|+
name|expected
operator|.
name|toString
argument_list|()
operator|+
literal|" found "
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|BINARY
case|:
block|{
name|DeserializeRead
operator|.
name|ReadBinaryResults
name|readBinaryResults
init|=
name|deserializeRead
operator|.
name|createReadBinaryResults
argument_list|()
decl_stmt|;
name|deserializeRead
operator|.
name|readBinary
argument_list|(
name|readBinaryResults
argument_list|)
expr_stmt|;
name|byte
index|[]
name|byteArray
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|readBinaryResults
operator|.
name|bytes
argument_list|,
name|readBinaryResults
operator|.
name|start
argument_list|,
name|readBinaryResults
operator|.
name|start
operator|+
name|readBinaryResults
operator|.
name|length
argument_list|)
decl_stmt|;
name|byte
index|[]
name|expected
init|=
operator|(
name|byte
index|[]
operator|)
name|object
decl_stmt|;
if|if
condition|(
name|byteArray
operator|.
name|length
operator|!=
name|expected
operator|.
name|length
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Byte Array field mismatch (expected "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|" found "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|byteArray
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|b
init|=
literal|0
init|;
name|b
operator|<
name|byteArray
operator|.
name|length
condition|;
name|b
operator|++
control|)
block|{
if|if
condition|(
name|byteArray
index|[
name|b
index|]
operator|!=
name|expected
index|[
name|b
index|]
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Byte Array field mismatch (expected "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|" found "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|byteArray
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unknown primitive category "
operator|+
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|serializeWrite
parameter_list|(
name|SerializeWrite
name|serializeWrite
parameter_list|,
name|PrimitiveTypeInfo
name|primitiveTypeInfo
parameter_list|,
name|Object
name|object
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
name|serializeWrite
operator|.
name|writeNull
argument_list|()
expr_stmt|;
return|return;
block|}
switch|switch
condition|(
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|boolean
name|value
init|=
operator|(
name|Boolean
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeBoolean
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|BYTE
case|:
block|{
name|byte
name|value
init|=
operator|(
name|Byte
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeByte
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|SHORT
case|:
block|{
name|short
name|value
init|=
operator|(
name|Short
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeShort
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INT
case|:
block|{
name|int
name|value
init|=
operator|(
name|Integer
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeInt
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|LONG
case|:
block|{
name|long
name|value
init|=
operator|(
name|Long
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|FLOAT
case|:
block|{
name|float
name|value
init|=
operator|(
name|Float
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeFloat
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DOUBLE
case|:
block|{
name|double
name|value
init|=
operator|(
name|Double
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeDouble
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|STRING
case|:
block|{
name|String
name|value
init|=
operator|(
name|String
operator|)
name|object
decl_stmt|;
name|byte
index|[]
name|stringBytes
init|=
name|value
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|stringLength
init|=
name|stringBytes
operator|.
name|length
decl_stmt|;
name|serializeWrite
operator|.
name|writeString
argument_list|(
name|stringBytes
argument_list|,
literal|0
argument_list|,
name|stringLength
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|CHAR
case|:
block|{
name|HiveChar
name|value
init|=
operator|(
name|HiveChar
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveChar
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|VARCHAR
case|:
block|{
name|HiveVarchar
name|value
init|=
operator|(
name|HiveVarchar
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveVarchar
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL
case|:
block|{
name|HiveDecimal
name|value
init|=
operator|(
name|HiveDecimal
operator|)
name|object
decl_stmt|;
name|DecimalTypeInfo
name|decTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|primitiveTypeInfo
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveDecimal
argument_list|(
name|value
argument_list|,
name|decTypeInfo
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DATE
case|:
block|{
name|Date
name|value
init|=
operator|(
name|Date
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeDate
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|TIMESTAMP
case|:
block|{
name|Timestamp
name|value
init|=
operator|(
name|Timestamp
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeTimestamp
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
block|{
name|HiveIntervalYearMonth
name|value
init|=
operator|(
name|HiveIntervalYearMonth
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveIntervalYearMonth
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
block|{
name|HiveIntervalDayTime
name|value
init|=
operator|(
name|HiveIntervalDayTime
operator|)
name|object
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveIntervalDayTime
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|BINARY
case|:
block|{
name|byte
index|[]
name|binaryBytes
init|=
operator|(
name|byte
index|[]
operator|)
name|object
decl_stmt|;
name|int
name|length
init|=
name|binaryBytes
operator|.
name|length
decl_stmt|;
name|serializeWrite
operator|.
name|writeBinary
argument_list|(
name|binaryBytes
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unknown primitive category "
operator|+
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

