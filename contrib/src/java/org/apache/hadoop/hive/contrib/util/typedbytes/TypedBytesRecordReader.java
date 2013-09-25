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
name|contrib
operator|.
name|util
operator|.
name|typedbytes
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|Configuration
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
name|exec
operator|.
name|RecordReader
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
name|NonSyncDataOutputBuffer
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
name|serde
operator|.
name|serdeConstants
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
name|ByteWritable
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
name|DoubleWritable
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
name|ShortWritable
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
name|ObjectInspectorConverters
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveTypeEntry
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
name|ParameterizedPrimitiveTypeUtils
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
name|BooleanWritable
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
name|BytesWritable
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
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * TypedBytesRecordReader.  *  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesRecordReader
implements|implements
name|RecordReader
block|{
specifier|private
name|DataInputStream
name|din
decl_stmt|;
specifier|private
name|TypedBytesWritableInput
name|tbIn
decl_stmt|;
specifier|private
name|NonSyncDataOutputBuffer
name|barrStr
init|=
operator|new
name|NonSyncDataOutputBuffer
argument_list|()
decl_stmt|;
specifier|private
name|TypedBytesWritableOutput
name|tbOut
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Writable
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|String
argument_list|>
name|rowTypeName
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|srcOIns
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|dstOIns
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Converter
argument_list|>
name|converters
init|=
operator|new
name|ArrayList
argument_list|<
name|Converter
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Type
argument_list|,
name|String
argument_list|>
name|typedBytesToTypeName
init|=
operator|new
name|HashMap
argument_list|<
name|Type
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|2
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|3
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|4
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|5
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|6
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|7
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
name|typedBytesToTypeName
operator|.
name|put
argument_list|(
name|getType
argument_list|(
literal|11
argument_list|)
argument_list|,
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|IOException
block|{
name|din
operator|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|tbIn
operator|=
operator|new
name|TypedBytesWritableInput
argument_list|(
name|din
argument_list|)
expr_stmt|;
name|tbOut
operator|=
operator|new
name|TypedBytesWritableOutput
argument_list|(
name|barrStr
argument_list|)
expr_stmt|;
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
name|columnTypes
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnTypeProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|columnType
range|:
name|columnTypes
control|)
block|{
name|PrimitiveTypeEntry
name|dstTypeEntry
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromTypeName
argument_list|(
name|columnType
argument_list|)
decl_stmt|;
name|dstOIns
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|dstTypeEntry
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Writable
name|createRow
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesWritable
name|retWrit
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
return|return
name|retWrit
return|;
block|}
specifier|private
name|Writable
name|allocateWritable
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BYTE
case|:
return|return
operator|new
name|ByteWritable
argument_list|()
return|;
case|case
name|BOOL
case|:
return|return
operator|new
name|BooleanWritable
argument_list|()
return|;
case|case
name|INT
case|:
return|return
operator|new
name|IntWritable
argument_list|()
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|ShortWritable
argument_list|()
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|LongWritable
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|FloatWritable
argument_list|()
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleWritable
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|Text
argument_list|()
return|;
default|default:
assert|assert
literal|false
assert|;
comment|// not supported
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|int
name|next
parameter_list|(
name|Writable
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|barrStr
operator|.
name|reset
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Type
name|type
init|=
name|tbIn
operator|.
name|readTypeCode
argument_list|()
decl_stmt|;
comment|// it was a empty stream
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|ENDOFRECORD
condition|)
block|{
name|tbOut
operator|.
name|writeEndOfRecord
argument_list|()
expr_stmt|;
if|if
condition|(
name|barrStr
operator|.
name|getLength
argument_list|()
operator|>
literal|0
condition|)
block|{
operator|(
operator|(
name|BytesWritable
operator|)
name|data
operator|)
operator|.
name|set
argument_list|(
name|barrStr
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|barrStr
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|barrStr
operator|.
name|getLength
argument_list|()
return|;
block|}
if|if
condition|(
name|pos
operator|>=
name|row
operator|.
name|size
argument_list|()
condition|)
block|{
name|Writable
name|wrt
init|=
name|allocateWritable
argument_list|(
name|type
argument_list|)
decl_stmt|;
assert|assert
name|pos
operator|==
name|row
operator|.
name|size
argument_list|()
assert|;
assert|assert
name|pos
operator|==
name|rowTypeName
operator|.
name|size
argument_list|()
assert|;
name|row
operator|.
name|add
argument_list|(
name|wrt
argument_list|)
expr_stmt|;
name|rowTypeName
operator|.
name|add
argument_list|(
name|type
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|typeName
init|=
name|typedBytesToTypeName
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|PrimitiveTypeEntry
name|srcTypeEntry
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromTypeName
argument_list|(
name|typeName
argument_list|)
decl_stmt|;
name|srcOIns
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|srcTypeEntry
argument_list|)
argument_list|)
expr_stmt|;
name|converters
operator|.
name|add
argument_list|(
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|srcOIns
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|,
name|dstOIns
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|rowTypeName
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|equals
argument_list|(
name|type
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"datatype of row changed from "
operator|+
name|rowTypeName
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|+
literal|" to "
operator|+
name|type
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|Writable
name|w
init|=
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BYTE
case|:
name|tbIn
operator|.
name|readByte
argument_list|(
operator|(
name|ByteWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|BOOL
case|:
name|tbIn
operator|.
name|readBoolean
argument_list|(
operator|(
name|BooleanWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|tbIn
operator|.
name|readInt
argument_list|(
operator|(
name|IntWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|tbIn
operator|.
name|readShort
argument_list|(
operator|(
name|ShortWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|tbIn
operator|.
name|readLong
argument_list|(
operator|(
name|LongWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|tbIn
operator|.
name|readFloat
argument_list|(
operator|(
name|FloatWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|tbIn
operator|.
name|readDouble
argument_list|(
operator|(
name|DoubleWritable
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|tbIn
operator|.
name|readText
argument_list|(
operator|(
name|Text
operator|)
name|w
argument_list|)
expr_stmt|;
break|break;
default|default:
assert|assert
literal|false
assert|;
comment|// should never come here
block|}
name|write
argument_list|(
name|pos
argument_list|,
name|w
argument_list|)
expr_stmt|;
name|pos
operator|++
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|write
parameter_list|(
name|int
name|pos
parameter_list|,
name|Writable
name|inpw
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|typ
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|Writable
name|w
init|=
operator|(
name|Writable
operator|)
name|converters
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|convert
argument_list|(
name|inpw
argument_list|)
decl_stmt|;
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeBoolean
argument_list|(
operator|(
name|BooleanWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeByte
argument_list|(
operator|(
name|ByteWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeShort
argument_list|(
operator|(
name|ShortWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeInt
argument_list|(
operator|(
name|IntWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeLong
argument_list|(
operator|(
name|LongWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeFloat
argument_list|(
operator|(
name|FloatWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeDouble
argument_list|(
operator|(
name|DoubleWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|typ
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
name|tbOut
operator|.
name|writeText
argument_list|(
operator|(
name|Text
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
literal|false
assert|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|din
operator|!=
literal|null
condition|)
block|{
name|din
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|Type
name|getType
parameter_list|(
name|int
name|code
parameter_list|)
block|{
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|type
operator|.
name|code
operator|==
name|code
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

