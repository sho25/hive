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
name|lazybinary
package|;
end_package

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ByteStream
operator|.
name|RandomAccessOutput
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
name|lazybinary
operator|.
name|objectinspector
operator|.
name|LazyBinaryObjectInspectorFactory
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
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * LazyBinaryUtils.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyBinaryUtils
block|{
comment|/**    * Convert the byte array to an int starting from the given offset. Refer to    * code by aeden on DZone Snippets:    *    * @param b    *          the byte array    * @param offset    *          the array offset    * @return the integer    */
specifier|public
specifier|static
name|int
name|byteArrayToInt
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|int
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|int
name|shift
init|=
operator|(
literal|4
operator|-
literal|1
operator|-
name|i
operator|)
operator|*
literal|8
decl_stmt|;
name|value
operator|+=
operator|(
name|b
index|[
name|i
operator|+
name|offset
index|]
operator|&
literal|0x000000FF
operator|)
operator|<<
name|shift
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * Convert the byte array to a long starting from the given offset.    *    * @param b    *          the byte array    * @param offset    *          the array offset    * @return the long    */
specifier|public
specifier|static
name|long
name|byteArrayToLong
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|int
name|shift
init|=
operator|(
literal|8
operator|-
literal|1
operator|-
name|i
operator|)
operator|*
literal|8
decl_stmt|;
name|value
operator|+=
operator|(
call|(
name|long
call|)
argument_list|(
name|b
index|[
name|i
operator|+
name|offset
index|]
operator|&
literal|0x00000000000000FF
argument_list|)
operator|)
operator|<<
name|shift
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * Convert the byte array to a short starting from the given offset.    *    * @param b    *          the byte array    * @param offset    *          the array offset    * @return the short    */
specifier|public
specifier|static
name|short
name|byteArrayToShort
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|short
name|value
init|=
literal|0
decl_stmt|;
name|value
operator|+=
operator|(
name|b
index|[
name|offset
index|]
operator|&
literal|0x000000FF
operator|)
operator|<<
literal|8
expr_stmt|;
name|value
operator|+=
operator|(
name|b
index|[
name|offset
operator|+
literal|1
index|]
operator|&
literal|0x000000FF
operator|)
expr_stmt|;
return|return
name|value
return|;
block|}
comment|/**    * Record is the unit that data is serialized in. A record includes two parts.    * The first part stores the size of the element and the second part stores    * the real element. size element record -> |----|-------------------------|    *    * A RecordInfo stores two information of a record, the size of the "size"    * part which is the element offset and the size of the element part which is    * element size.    */
specifier|public
specifier|static
class|class
name|RecordInfo
block|{
specifier|public
name|RecordInfo
parameter_list|()
block|{
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|elementSize
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|byte
name|elementOffset
decl_stmt|;
specifier|public
name|int
name|elementSize
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"("
operator|+
name|elementOffset
operator|+
literal|", "
operator|+
name|elementSize
operator|+
literal|")"
return|;
block|}
block|}
comment|/**    * Check a particular field and set its size and offset in bytes based on the    * field type and the bytes arrays.    *    * For void, boolean, byte, short, int, long, float and double, there is no    * offset and the size is fixed. For string, map, list, struct, the first four    * bytes are used to store the size. So the offset is 4 and the size is    * computed by concating the first four bytes together. The first four bytes    * are defined with respect to the offset in the bytes arrays.    * For timestamp, if the first bit is 0, the record length is 4, otherwise    * a VInt begins at the 5th byte and its length is added to 4.    *    * @param objectInspector    *          object inspector of the field    * @param bytes    *          bytes arrays store the table row    * @param offset    *          offset of this field    * @param recordInfo    *          modify this byteinfo object and return it    */
specifier|public
specifier|static
name|void
name|checkObjectByteInfo
parameter_list|(
name|ObjectInspector
name|objectInspector
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|RecordInfo
name|recordInfo
parameter_list|,
name|VInt
name|vInt
parameter_list|)
block|{
name|Category
name|category
init|=
name|objectInspector
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|PrimitiveCategory
name|primitiveCategory
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|objectInspector
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
case|case
name|VOID
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
literal|0
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
literal|1
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
literal|2
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
literal|4
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
literal|8
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
comment|// using vint instead of 4 bytes
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|recordInfo
operator|.
name|elementOffset
operator|=
name|vInt
operator|.
name|length
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|vInt
operator|.
name|value
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|recordInfo
operator|.
name|elementOffset
operator|=
name|vInt
operator|.
name|length
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|vInt
operator|.
name|value
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
comment|// using vint instead of 4 bytes
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|recordInfo
operator|.
name|elementOffset
operator|=
name|vInt
operator|.
name|length
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|vInt
operator|.
name|value
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|TimestampWritable
operator|.
name|getTotalLength
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|int
name|secondsSize
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
decl_stmt|;
name|int
name|nanosSize
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
operator|+
name|secondsSize
index|]
argument_list|)
decl_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|secondsSize
operator|+
name|nanosSize
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
comment|// using vint instead of 4 bytes
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|0
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|vInt
operator|.
name|length
expr_stmt|;
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|vInt
operator|.
name|length
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|+=
name|vInt
operator|.
name|length
operator|+
name|vInt
operator|.
name|value
expr_stmt|;
break|break;
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized primitive type: "
operator|+
name|primitiveCategory
argument_list|)
throw|;
block|}
block|}
break|break;
case|case
name|LIST
case|:
case|case
name|MAP
case|:
case|case
name|STRUCT
case|:
case|case
name|UNION
case|:
name|recordInfo
operator|.
name|elementOffset
operator|=
literal|4
expr_stmt|;
name|recordInfo
operator|.
name|elementSize
operator|=
name|LazyBinaryUtils
operator|.
name|byteArrayToInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
expr_stmt|;
break|break;
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized non-primitive type: "
operator|+
name|category
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * A zero-compressed encoded long.    */
specifier|public
specifier|static
class|class
name|VLong
block|{
specifier|public
name|VLong
parameter_list|()
block|{
name|value
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|long
name|value
decl_stmt|;
specifier|public
name|byte
name|length
decl_stmt|;
block|}
empty_stmt|;
comment|/**    * Reads a zero-compressed encoded long from a byte array and returns it.    *    * @param bytes    *          the byte array    * @param offset    *          offset of the array to read from    * @param vlong    *          storing the deserialized long and its size in byte    */
specifier|public
specifier|static
name|void
name|readVLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|VLong
name|vlong
parameter_list|)
block|{
name|byte
name|firstByte
init|=
name|bytes
index|[
name|offset
index|]
decl_stmt|;
name|vlong
operator|.
name|length
operator|=
operator|(
name|byte
operator|)
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
if|if
condition|(
name|vlong
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|vlong
operator|.
name|value
operator|=
name|firstByte
expr_stmt|;
return|return;
block|}
name|long
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|vlong
operator|.
name|length
operator|-
literal|1
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
index|[
name|offset
operator|+
literal|1
operator|+
name|idx
index|]
decl_stmt|;
name|i
operator|=
name|i
operator|<<
literal|8
expr_stmt|;
name|i
operator|=
name|i
operator||
operator|(
name|b
operator|&
literal|0xFF
operator|)
expr_stmt|;
block|}
name|vlong
operator|.
name|value
operator|=
operator|(
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|firstByte
argument_list|)
condition|?
operator|(
name|i
operator|^
operator|-
literal|1L
operator|)
else|:
name|i
operator|)
expr_stmt|;
block|}
comment|/**    * A zero-compressed encoded integer.    */
specifier|public
specifier|static
class|class
name|VInt
block|{
specifier|public
name|VInt
parameter_list|()
block|{
name|value
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|int
name|value
decl_stmt|;
specifier|public
name|byte
name|length
decl_stmt|;
block|}
empty_stmt|;
specifier|public
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|VInt
argument_list|>
name|threadLocalVInt
init|=
operator|new
name|ThreadLocal
argument_list|<
name|VInt
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|VInt
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|VInt
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Reads a zero-compressed encoded int from a byte array and returns it.    *    * @param bytes    *          the byte array    * @param offset    *          offset of the array to read from    * @param vInt    *          storing the deserialized int and its size in byte    */
specifier|public
specifier|static
name|void
name|readVInt
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|VInt
name|vInt
parameter_list|)
block|{
name|byte
name|firstByte
init|=
name|bytes
index|[
name|offset
index|]
decl_stmt|;
name|vInt
operator|.
name|length
operator|=
operator|(
name|byte
operator|)
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
if|if
condition|(
name|vInt
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|vInt
operator|.
name|value
operator|=
name|firstByte
expr_stmt|;
return|return;
block|}
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|vInt
operator|.
name|length
operator|-
literal|1
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
index|[
name|offset
operator|+
literal|1
operator|+
name|idx
index|]
decl_stmt|;
name|i
operator|=
name|i
operator|<<
literal|8
expr_stmt|;
name|i
operator|=
name|i
operator||
operator|(
name|b
operator|&
literal|0xFF
operator|)
expr_stmt|;
block|}
name|vInt
operator|.
name|value
operator|=
operator|(
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|firstByte
argument_list|)
condition|?
operator|(
name|i
operator|^
operator|-
literal|1
operator|)
else|:
name|i
operator|)
expr_stmt|;
block|}
comment|/**    * Writes a zero-compressed encoded int to a byte array.    *    * @param byteStream    *          the byte array/stream    * @param i    *          the int    */
specifier|public
specifier|static
name|void
name|writeVInt
parameter_list|(
name|RandomAccessOutput
name|byteStream
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|writeVLong
argument_list|(
name|byteStream
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read a zero-compressed encoded long from a byte array.    *    * @param bytes the byte array    * @param offset the offset in the byte array where the VLong is stored    * @return the long    */
specifier|public
specifier|static
name|long
name|readVLongFromByteArray
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|byte
name|firstByte
init|=
name|bytes
index|[
name|offset
operator|++
index|]
decl_stmt|;
name|int
name|len
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|firstByte
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|==
literal|1
condition|)
block|{
return|return
name|firstByte
return|;
block|}
name|long
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|len
operator|-
literal|1
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
index|[
name|offset
operator|++
index|]
decl_stmt|;
name|i
operator|=
name|i
operator|<<
literal|8
expr_stmt|;
name|i
operator|=
name|i
operator||
operator|(
name|b
operator|&
literal|0xFF
operator|)
expr_stmt|;
block|}
return|return
operator|(
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|firstByte
argument_list|)
condition|?
operator|~
name|i
else|:
name|i
operator|)
return|;
block|}
comment|/**    * Write a zero-compressed encoded long to a byte array.    *    * @param bytes    *          the byte array/stream    * @param l    *          the long    */
specifier|public
specifier|static
name|int
name|writeVLongToByteArray
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|long
name|l
parameter_list|)
block|{
return|return
name|LazyBinaryUtils
operator|.
name|writeVLongToByteArray
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|l
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|writeVLongToByteArray
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|long
name|l
parameter_list|)
block|{
if|if
condition|(
name|l
operator|>=
operator|-
literal|112
operator|&&
name|l
operator|<=
literal|127
condition|)
block|{
name|bytes
index|[
name|offset
index|]
operator|=
operator|(
name|byte
operator|)
name|l
expr_stmt|;
return|return
literal|1
return|;
block|}
name|int
name|len
init|=
operator|-
literal|112
decl_stmt|;
if|if
condition|(
name|l
operator|<
literal|0
condition|)
block|{
name|l
operator|^=
operator|-
literal|1L
expr_stmt|;
comment|// take one's complement'
name|len
operator|=
operator|-
literal|120
expr_stmt|;
block|}
name|long
name|tmp
init|=
name|l
decl_stmt|;
while|while
condition|(
name|tmp
operator|!=
literal|0
condition|)
block|{
name|tmp
operator|=
name|tmp
operator|>>
literal|8
expr_stmt|;
name|len
operator|--
expr_stmt|;
block|}
name|bytes
index|[
name|offset
index|]
operator|=
operator|(
name|byte
operator|)
name|len
expr_stmt|;
name|len
operator|=
operator|(
name|len
operator|<
operator|-
literal|120
operator|)
condition|?
operator|-
operator|(
name|len
operator|+
literal|120
operator|)
else|:
operator|-
operator|(
name|len
operator|+
literal|112
operator|)
expr_stmt|;
for|for
control|(
name|int
name|idx
init|=
name|len
init|;
name|idx
operator|!=
literal|0
condition|;
name|idx
operator|--
control|)
block|{
name|int
name|shiftbits
init|=
operator|(
name|idx
operator|-
literal|1
operator|)
operator|*
literal|8
decl_stmt|;
name|long
name|mask
init|=
literal|0xFFL
operator|<<
name|shiftbits
decl_stmt|;
name|bytes
index|[
name|offset
operator|+
literal|1
operator|-
operator|(
name|idx
operator|-
name|len
operator|)
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|l
operator|&
name|mask
operator|)
operator|>>
name|shiftbits
argument_list|)
expr_stmt|;
block|}
return|return
literal|1
operator|+
name|len
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|VLONG_BYTES_LEN
init|=
literal|9
decl_stmt|;
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|byte
index|[]
argument_list|>
name|vLongBytesThreadLocal
init|=
operator|new
name|ThreadLocal
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|byte
index|[
name|VLONG_BYTES_LEN
index|]
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|writeVLong
parameter_list|(
name|RandomAccessOutput
name|byteStream
parameter_list|,
name|long
name|l
parameter_list|)
block|{
name|byte
index|[]
name|vLongBytes
init|=
name|vLongBytesThreadLocal
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|LazyBinaryUtils
operator|.
name|writeVLongToByteArray
argument_list|(
name|vLongBytes
argument_list|,
name|l
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
name|vLongBytes
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|writeDouble
parameter_list|(
name|RandomAccessOutput
name|byteStream
parameter_list|,
name|double
name|d
parameter_list|)
block|{
name|long
name|v
init|=
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|d
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|56
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|48
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|40
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|32
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|24
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
name|ConcurrentHashMap
argument_list|<
name|TypeInfo
argument_list|,
name|ObjectInspector
argument_list|>
name|cachedLazyBinaryObjectInspector
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|TypeInfo
argument_list|,
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Returns the lazy binary object inspector that can be used to inspect an    * lazy binary object of that typeInfo    *    * For primitive types, we use the standard writable object inspector.    */
specifier|public
specifier|static
name|ObjectInspector
name|getLazyBinaryObjectInspectorFromTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|ObjectInspector
name|result
init|=
name|cachedLazyBinaryObjectInspector
operator|.
name|get
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
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
block|{
name|result
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|LIST
case|:
block|{
name|ObjectInspector
name|elementObjectInspector
init|=
name|getLazyBinaryObjectInspectorFromTypeInfo
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
decl_stmt|;
name|result
operator|=
name|LazyBinaryObjectInspectorFactory
operator|.
name|getLazyBinaryListObjectInspector
argument_list|(
name|elementObjectInspector
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|MAP
case|:
block|{
name|MapTypeInfo
name|mapTypeInfo
init|=
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|ObjectInspector
name|keyObjectInspector
init|=
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|mapTypeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueObjectInspector
init|=
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|mapTypeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
name|LazyBinaryObjectInspectorFactory
operator|.
name|getLazyBinaryMapObjectInspector
argument_list|(
name|keyObjectInspector
argument_list|,
name|valueObjectInspector
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|STRUCT
case|:
block|{
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fieldTypeInfos
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldTypeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldObjectInspectors
operator|.
name|add
argument_list|(
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|LazyBinaryObjectInspectorFactory
operator|.
name|getLazyBinaryStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldObjectInspectors
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|UNION
case|:
block|{
name|UnionTypeInfo
name|unionTypeInfo
init|=
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|unionTypeInfo
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fieldTypeInfos
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldTypeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldObjectInspectors
operator|.
name|add
argument_list|(
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|LazyBinaryObjectInspectorFactory
operator|.
name|getLazyBinaryUnionObjectInspector
argument_list|(
name|fieldObjectInspectors
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
block|{
name|result
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|ObjectInspector
name|prev
init|=
name|cachedLazyBinaryObjectInspector
operator|.
name|putIfAbsent
argument_list|(
name|typeInfo
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
name|LazyBinaryUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

