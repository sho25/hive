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
name|lazy
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|OutputStream
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
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
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
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|SerDeException
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
name|lazy
operator|.
name|LazySimpleSerDe
operator|.
name|SerDeParameters
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
name|primitive
operator|.
name|HiveDecimalObjectInspector
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
name|BinaryObjectInspector
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
name|BooleanObjectInspector
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
name|ByteObjectInspector
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
name|DoubleObjectInspector
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
name|FloatObjectInspector
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
name|IntObjectInspector
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
name|LongObjectInspector
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
name|ShortObjectInspector
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
name|StringObjectInspector
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
name|DateObjectInspector
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
name|TimestampObjectInspector
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
name|TypeInfoUtils
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
name|Text
import|;
end_import

begin_comment
comment|/**  * LazyUtils.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyUtils
block|{
comment|/**    * Returns the digit represented by character b.    *    * @param b    *          The ascii code of the character    * @param radix    *          The radix    * @return -1 if it's invalid    */
specifier|public
specifier|static
name|int
name|digit
parameter_list|(
name|int
name|b
parameter_list|,
name|int
name|radix
parameter_list|)
block|{
name|int
name|r
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|b
operator|>=
literal|'0'
operator|&&
name|b
operator|<=
literal|'9'
condition|)
block|{
name|r
operator|=
name|b
operator|-
literal|'0'
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|b
operator|>=
literal|'A'
operator|&&
name|b
operator|<=
literal|'Z'
condition|)
block|{
name|r
operator|=
name|b
operator|-
literal|'A'
operator|+
literal|10
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|b
operator|>=
literal|'a'
operator|&&
name|b
operator|<=
literal|'z'
condition|)
block|{
name|r
operator|=
name|b
operator|-
literal|'a'
operator|+
literal|10
expr_stmt|;
block|}
if|if
condition|(
name|r
operator|>=
name|radix
condition|)
block|{
name|r
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
comment|/**    * Returns -1 if the first byte sequence is lexicographically less than the    * second; returns +1 if the second byte sequence is lexicographically less    * than the first; otherwise return 0.    */
specifier|public
specifier|static
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|b1
parameter_list|,
name|int
name|start1
parameter_list|,
name|int
name|length1
parameter_list|,
name|byte
index|[]
name|b2
parameter_list|,
name|int
name|start2
parameter_list|,
name|int
name|length2
parameter_list|)
block|{
name|int
name|min
init|=
name|Math
operator|.
name|min
argument_list|(
name|length1
argument_list|,
name|length2
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
name|min
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|b1
index|[
name|start1
operator|+
name|i
index|]
operator|==
name|b2
index|[
name|start2
operator|+
name|i
index|]
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|b1
index|[
name|start1
operator|+
name|i
index|]
operator|<
name|b2
index|[
name|start2
operator|+
name|i
index|]
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|1
return|;
block|}
block|}
if|if
condition|(
name|length1
operator|<
name|length2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|length1
operator|>
name|length2
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * Convert a UTF-8 byte array to String.    *    * @param bytes    *          The byte[] containing the UTF-8 String.    * @param start    *          The start position inside the bytes.    * @param length    *          The length of the data, starting from "start"    * @return The unicode String    */
specifier|public
specifier|static
name|String
name|convertToString
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
try|try
block|{
return|return
name|Text
operator|.
name|decode
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|byte
index|[]
name|trueBytes
init|=
block|{
operator|(
name|byte
operator|)
literal|'t'
block|,
literal|'r'
block|,
literal|'u'
block|,
literal|'e'
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|falseBytes
init|=
block|{
operator|(
name|byte
operator|)
literal|'f'
block|,
literal|'a'
block|,
literal|'l'
block|,
literal|'s'
block|,
literal|'e'
block|}
decl_stmt|;
comment|/**    * Write the bytes with special characters escaped.    *    * @param escaped    *          Whether the data should be written out in an escaped way.    * @param escapeChar    *          if escaped, the char for prefixing special characters.    * @param needsEscape    *          if escaped, whether a specific character needs escaping. This    *          array should have size of 128.    */
specifier|private
specifier|static
name|void
name|writeEscaped
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|boolean
index|[]
name|needsEscape
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|escaped
condition|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|len
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<=
name|end
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|end
operator|||
operator|(
name|bytes
index|[
name|i
index|]
operator|>=
literal|0
operator|&&
name|needsEscape
index|[
name|bytes
index|[
name|i
index|]
index|]
operator|)
condition|)
block|{
if|if
condition|(
name|i
operator|>
name|start
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|i
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
name|start
operator|=
name|i
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|len
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|escapeChar
argument_list|)
expr_stmt|;
comment|// the current char will be written out later.
block|}
block|}
block|}
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write out the text representation of a Primitive Object to a UTF8 byte    * stream.    *    * @param out    *          The UTF8 byte OutputStream    * @param o    *          The primitive Object    * @param needsEscape    *          Whether a character needs escaping. This array should have size of    *          128.    */
specifier|public
specifier|static
name|void
name|writePrimitiveUTF8
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|Object
name|o
parameter_list|,
name|PrimitiveObjectInspector
name|oi
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|boolean
index|[]
name|needsEscape
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|oi
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
name|b
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|trueBytes
argument_list|,
literal|0
argument_list|,
name|trueBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
name|falseBytes
argument_list|,
literal|0
argument_list|,
name|falseBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|BYTE
case|:
block|{
name|LazyInteger
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|SHORT
case|:
block|{
name|LazyInteger
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INT
case|:
block|{
name|LazyInteger
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|IntObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|LONG
case|:
block|{
name|LazyLong
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|FLOAT
case|:
block|{
name|float
name|f
init|=
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|ByteBuffer
name|b
init|=
name|Text
operator|.
name|encode
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DOUBLE
case|:
block|{
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|ByteBuffer
name|b
init|=
name|Text
operator|.
name|encode
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|d
argument_list|)
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|STRING
case|:
block|{
name|Text
name|t
init|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|writeEscaped
argument_list|(
name|out
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|BINARY
case|:
block|{
name|BytesWritable
name|bw
init|=
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|byte
index|[]
name|toEncode
init|=
operator|new
name|byte
index|[
name|bw
operator|.
name|getLength
argument_list|()
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|toEncode
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|toWrite
init|=
name|Base64
operator|.
name|encodeBase64
argument_list|(
name|toEncode
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|toWrite
argument_list|,
literal|0
argument_list|,
name|toWrite
operator|.
name|length
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DATE
case|:
block|{
name|LazyDate
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|DateObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|TIMESTAMP
case|:
block|{
name|LazyTimestamp
operator|.
name|writeUTF8
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DECIMAL
case|:
block|{
name|HiveDecimal
name|bd
init|=
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|ByteBuffer
name|b
init|=
name|Text
operator|.
name|encode
argument_list|(
name|bd
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error."
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Write out a binary representation of a PrimitiveObject to a byte stream.    *    * @param out ByteStream.Output, an unsynchronized version of ByteArrayOutputStream, used as a    *            backing buffer for the the DataOutputStream    * @param o the PrimitiveObject    * @param oi the PrimitiveObjectInspector    * @throws IOException on error during the write operation    */
specifier|public
specifier|static
name|void
name|writePrimitive
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|Object
name|o
parameter_list|,
name|PrimitiveObjectInspector
name|oi
parameter_list|)
throws|throws
name|IOException
block|{
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|out
argument_list|)
decl_stmt|;
try|try
block|{
switch|switch
condition|(
name|oi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|boolean
name|b
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeBoolean
argument_list|(
name|b
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|byte
name|bt
init|=
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeByte
argument_list|(
name|bt
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|short
name|s
init|=
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeShort
argument_list|(
name|s
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|int
name|i
init|=
operator|(
operator|(
name|IntObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|long
name|l
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeLong
argument_list|(
name|l
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|float
name|f
init|=
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeFloat
argument_list|(
name|f
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|dos
operator|.
name|writeDouble
argument_list|(
name|d
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error."
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
comment|// closing the underlying ByteStream should have no effect, the data should still be
comment|// accessible
name|dos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|int
name|hashBytes
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|hash
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|hash
operator|=
operator|(
literal|31
operator|*
name|hash
operator|)
operator|+
name|data
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
specifier|public
specifier|static
name|void
name|extractColumnInfo
parameter_list|(
name|Properties
name|tbl
parameter_list|,
name|SerDeParameters
name|serdeParams
parameter_list|,
name|String
name|serdeName
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Read the configuration parameters
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
comment|// NOTE: if "columns.types" is missing, all columns will be of String type
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
comment|// Parse the configuration parameters
if|if
condition|(
name|columnNameProperty
operator|!=
literal|null
operator|&&
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serdeParams
operator|.
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serdeParams
operator|.
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|columnTypeProperty
operator|==
literal|null
condition|)
block|{
comment|// Default type: all string
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
name|columnTypeProperty
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|serdeParams
operator|.
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
if|if
condition|(
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|serdeParams
operator|.
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|serdeName
operator|+
literal|": columns has "
operator|+
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
operator|+
literal|" elements while columns.types has "
operator|+
name|serdeParams
operator|.
name|columnTypes
operator|.
name|size
argument_list|()
operator|+
literal|" elements!"
argument_list|)
throw|;
block|}
block|}
comment|/**    * gets a byte[] with copy of data from source BytesWritable    * @param sourceBw - source BytesWritable    */
specifier|public
specifier|static
name|byte
index|[]
name|createByteArray
parameter_list|(
name|BytesWritable
name|sourceBw
parameter_list|)
block|{
comment|//TODO should replace with BytesWritable.copyData() once Hive
comment|//removes support for the Hadoop 0.20 series.
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|sourceBw
operator|.
name|getBytes
argument_list|()
argument_list|,
name|sourceBw
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Utility function to get separator for current level used in serialization.    * Used to get a better log message when out of bound lookup happens    * @param separators - array of separators byte, byte at index x indicates    *  separator used at that level    * @param level - nesting level    * @return separator at given level    * @throws SerDeException    */
specifier|static
name|byte
name|getSeparator
parameter_list|(
name|byte
index|[]
name|separators
parameter_list|,
name|int
name|level
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
return|return
name|separators
index|[
name|level
index|]
return|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Number of levels of nesting supported for "
operator|+
literal|"LazySimpleSerde is "
operator|+
operator|(
name|separators
operator|.
name|length
operator|-
literal|1
operator|)
operator|+
literal|" Unable to work with level "
operator|+
name|level
decl_stmt|;
if|if
condition|(
name|separators
operator|.
name|length
operator|<
literal|9
condition|)
block|{
name|msg
operator|+=
literal|". Use "
operator|+
name|LazySimpleSerDe
operator|.
name|SERIALIZATION_EXTEND_NESTING_LEVELS
operator|+
literal|" serde property for tables using LazySimpleSerde."
expr_stmt|;
block|}
throw|throw
operator|new
name|SerDeException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|LazyUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

