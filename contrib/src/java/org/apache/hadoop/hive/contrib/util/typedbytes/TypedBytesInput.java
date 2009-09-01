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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|TreeMap
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|record
operator|.
name|Buffer
import|;
end_import

begin_comment
comment|/**  * Provides functionality for reading typed bytes.  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesInput
block|{
specifier|private
name|DataInput
name|in
decl_stmt|;
specifier|private
name|TypedBytesInput
parameter_list|()
block|{}
specifier|private
name|void
name|setDataInput
parameter_list|(
name|DataInput
name|in
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
block|}
specifier|private
specifier|static
name|ThreadLocal
name|tbIn
init|=
operator|new
name|ThreadLocal
argument_list|()
block|{
specifier|protected
specifier|synchronized
name|Object
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|TypedBytesInput
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Get a thread-local typed bytes input for the supplied {@link DataInput}.    * @param in data input object    * @return typed bytes input corresponding to the supplied {@link DataInput}.    */
specifier|public
specifier|static
name|TypedBytesInput
name|get
parameter_list|(
name|DataInput
name|in
parameter_list|)
block|{
name|TypedBytesInput
name|bin
init|=
operator|(
name|TypedBytesInput
operator|)
name|tbIn
operator|.
name|get
argument_list|()
decl_stmt|;
name|bin
operator|.
name|setDataInput
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|bin
return|;
block|}
comment|/** Creates a new instance of TypedBytesInput. */
specifier|public
name|TypedBytesInput
parameter_list|(
name|DataInput
name|in
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
block|}
comment|/**    * Reads a typed bytes sequence and converts it to a Java object. The first     * byte is interpreted as a type code, and then the right number of     * subsequent bytes are read depending on the obtained type.    * @return the obtained object or null when the end of the file is reached    * @throws IOException    */
specifier|public
name|Object
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|code
init|=
literal|1
decl_stmt|;
try|try
block|{
name|code
operator|=
name|in
operator|.
name|readUnsignedByte
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BYTES
operator|.
name|code
condition|)
block|{
return|return
operator|new
name|Buffer
argument_list|(
name|readBytes
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BYTE
operator|.
name|code
condition|)
block|{
return|return
name|readByte
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BOOL
operator|.
name|code
condition|)
block|{
return|return
name|readBool
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|INT
operator|.
name|code
condition|)
block|{
return|return
name|readInt
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|SHORT
operator|.
name|code
condition|)
block|{
return|return
name|readShort
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|LONG
operator|.
name|code
condition|)
block|{
return|return
name|readLong
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|FLOAT
operator|.
name|code
condition|)
block|{
return|return
name|readFloat
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|DOUBLE
operator|.
name|code
condition|)
block|{
return|return
name|readDouble
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|STRING
operator|.
name|code
condition|)
block|{
return|return
name|readString
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|VECTOR
operator|.
name|code
condition|)
block|{
return|return
name|readVector
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|LIST
operator|.
name|code
condition|)
block|{
return|return
name|readList
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|MAP
operator|.
name|code
condition|)
block|{
return|return
name|readMap
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|MARKER
operator|.
name|code
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
literal|50
operator|<=
name|code
operator|&&
name|code
operator|<=
literal|200
condition|)
block|{
comment|// application-specific typecodes
return|return
operator|new
name|Buffer
argument_list|(
name|readBytes
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unknown type"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Reads a typed bytes sequence. The first byte is interpreted as a type code,    * and then the right number of subsequent bytes are read depending on the    * obtained type.    *     * @return the obtained typed bytes sequence or null when the end of the file    *         is reached    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRaw
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|code
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|code
operator|=
name|in
operator|.
name|readUnsignedByte
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BYTES
operator|.
name|code
condition|)
block|{
return|return
name|readRawBytes
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BYTE
operator|.
name|code
condition|)
block|{
return|return
name|readRawByte
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|BOOL
operator|.
name|code
condition|)
block|{
return|return
name|readRawBool
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|INT
operator|.
name|code
condition|)
block|{
return|return
name|readRawInt
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|LONG
operator|.
name|code
condition|)
block|{
return|return
name|readRawLong
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|FLOAT
operator|.
name|code
condition|)
block|{
return|return
name|readRawFloat
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|DOUBLE
operator|.
name|code
condition|)
block|{
return|return
name|readRawDouble
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|STRING
operator|.
name|code
condition|)
block|{
return|return
name|readRawString
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|VECTOR
operator|.
name|code
condition|)
block|{
return|return
name|readRawVector
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|LIST
operator|.
name|code
condition|)
block|{
return|return
name|readRawList
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|MAP
operator|.
name|code
condition|)
block|{
return|return
name|readRawMap
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|code
operator|==
name|Type
operator|.
name|MARKER
operator|.
name|code
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
literal|50
operator|<=
name|code
operator|&&
name|code
operator|<=
literal|200
condition|)
block|{
comment|// application-specific typecodes
return|return
name|readRawBytes
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unknown type"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Reads a type byte and returns the corresponding {@link Type}.    * @return the obtained Type or null when the end of the file is reached    * @throws IOException    */
specifier|public
name|Type
name|readType
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|code
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|code
operator|=
name|in
operator|.
name|readUnsignedByte
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
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
comment|/**    * Skips a type byte.    * @return true iff the end of the file was not reached    * @throws IOException    */
specifier|public
name|boolean
name|skipType
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Reads the bytes following a<code>Type.BYTES</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readBytes
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.BYTES</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawBytes
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|5
operator|+
name|length
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|BYTES
operator|.
name|code
expr_stmt|;
name|bytes
index|[
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|24
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|16
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|8
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|4
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|length
argument_list|)
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|5
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the byte following a<code>Type.BYTE</code> code.    * @return the obtained byte    * @throws IOException    */
specifier|public
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readByte
argument_list|()
return|;
block|}
comment|/**    * Reads the raw byte following a<code>Type.BYTE</code> code.    * @return the obtained byte    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawByte
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|2
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|BYTE
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the boolean following a<code>Type.BOOL</code> code.    * @return the obtained boolean    * @throws IOException    */
specifier|public
name|boolean
name|readBool
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readBoolean
argument_list|()
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.BOOL</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawBool
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|2
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|BOOL
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the integer following a<code>Type.INT</code> code.    * @return the obtained integer    * @throws IOException    */
specifier|public
name|int
name|readInt
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readInt
argument_list|()
return|;
block|}
comment|/**    * Reads the short following a<code>Type.SHORT</code> code.    * @return the obtained short    * @throws IOException    */
specifier|public
name|short
name|readShort
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readShort
argument_list|()
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.INT</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawInt
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|5
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|INT
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the long following a<code>Type.LONG</code> code.    * @return the obtained long    * @throws IOException    */
specifier|public
name|long
name|readLong
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readLong
argument_list|()
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.LONG</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawLong
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|9
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|LONG
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the float following a<code>Type.FLOAT</code> code.    * @return the obtained float    * @throws IOException    */
specifier|public
name|float
name|readFloat
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readFloat
argument_list|()
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.FLOAT</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawFloat
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|5
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|FLOAT
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the double following a<code>Type.DOUBLE</code> code.    * @return the obtained double    * @throws IOException    */
specifier|public
name|double
name|readDouble
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readDouble
argument_list|()
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.DOUBLE</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawDouble
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|9
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|DOUBLE
operator|.
name|code
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the string following a<code>Type.STRING</code> code.    * @return the obtained string    * @throws IOException    */
specifier|public
name|String
name|readString
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|WritableUtils
operator|.
name|readString
argument_list|(
name|in
argument_list|)
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.STRING</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawString
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
literal|5
operator|+
name|length
index|]
decl_stmt|;
name|bytes
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|Type
operator|.
name|STRING
operator|.
name|code
expr_stmt|;
name|bytes
index|[
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|24
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|16
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|8
operator|)
argument_list|)
expr_stmt|;
name|bytes
index|[
literal|4
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|length
argument_list|)
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|bytes
argument_list|,
literal|5
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**    * Reads the vector following a<code>Type.VECTOR</code> code.    * @return the obtained vector    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|ArrayList
name|readVector
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|readVectorHeader
argument_list|()
decl_stmt|;
name|ArrayList
name|result
init|=
operator|new
name|ArrayList
argument_list|(
name|length
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|read
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.VECTOR</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawVector
parameter_list|()
throws|throws
name|IOException
block|{
name|Buffer
name|buffer
init|=
operator|new
name|Buffer
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|readVectorHeader
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|Type
operator|.
name|VECTOR
operator|.
name|code
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|24
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|16
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|8
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|length
argument_list|)
block|}
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|readRaw
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Reads the header following a<code>Type.VECTOR</code> code.    * @return the number of elements in the vector    * @throws IOException    */
specifier|public
name|int
name|readVectorHeader
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readInt
argument_list|()
return|;
block|}
comment|/**    * Reads the list following a<code>Type.LIST</code> code.    * @return the obtained list    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|List
name|readList
parameter_list|()
throws|throws
name|IOException
block|{
name|List
name|list
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
name|Object
name|obj
init|=
name|read
argument_list|()
decl_stmt|;
while|while
condition|(
name|obj
operator|!=
literal|null
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|obj
operator|=
name|read
argument_list|()
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.LIST</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawList
parameter_list|()
throws|throws
name|IOException
block|{
name|Buffer
name|buffer
init|=
operator|new
name|Buffer
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|Type
operator|.
name|LIST
operator|.
name|code
block|}
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|readRaw
argument_list|()
decl_stmt|;
while|while
condition|(
name|bytes
operator|!=
literal|null
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|readRaw
argument_list|()
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|Type
operator|.
name|MARKER
operator|.
name|code
block|}
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Reads the map following a<code>Type.MAP</code> code.    * @return the obtained map    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|TreeMap
name|readMap
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|readMapHeader
argument_list|()
decl_stmt|;
name|TreeMap
name|result
init|=
operator|new
name|TreeMap
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|key
init|=
name|read
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
name|read
argument_list|()
decl_stmt|;
name|result
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Reads the raw bytes following a<code>Type.MAP</code> code.    * @return the obtained bytes sequence    * @throws IOException    */
specifier|public
name|byte
index|[]
name|readRawMap
parameter_list|()
throws|throws
name|IOException
block|{
name|Buffer
name|buffer
init|=
operator|new
name|Buffer
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|readMapHeader
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|Type
operator|.
name|MAP
operator|.
name|code
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|24
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|16
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
operator|(
name|length
operator|>>
literal|8
operator|)
argument_list|)
block|,
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|length
argument_list|)
block|}
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|readRaw
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|readRaw
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Reads the header following a<code>Type.MAP</code> code.    * @return the number of key-value pairs in the map    * @throws IOException    */
specifier|public
name|int
name|readMapHeader
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readInt
argument_list|()
return|;
block|}
block|}
end_class

end_unit

