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
name|ByteArrayInputStream
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|io
operator|.
name|ArrayWritable
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
name|IOUtils
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
name|MapWritable
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
name|NullWritable
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
name|SortedMapWritable
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
name|VIntWritable
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
name|VLongWritable
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
name|WritableComparable
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Provides functionality for reading typed bytes as Writable objects.  *  * @see TypedBytesInput  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesWritableInput
implements|implements
name|Configurable
block|{
specifier|private
name|TypedBytesInput
name|in
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|TypedBytesWritableInput
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setTypedBytesInput
parameter_list|(
name|TypedBytesInput
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
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|Object
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|TypedBytesWritableInput
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Get a thread-local typed bytes writable input for the supplied    * {@link TypedBytesInput}.    *    * @param in    *          typed bytes input object    * @return typed bytes writable input corresponding to the supplied    *         {@link TypedBytesInput}.    */
specifier|public
specifier|static
name|TypedBytesWritableInput
name|get
parameter_list|(
name|TypedBytesInput
name|in
parameter_list|)
block|{
name|TypedBytesWritableInput
name|bin
init|=
operator|(
name|TypedBytesWritableInput
operator|)
name|tbIn
operator|.
name|get
argument_list|()
decl_stmt|;
name|bin
operator|.
name|setTypedBytesInput
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|bin
return|;
block|}
comment|/**    * Get a thread-local typed bytes writable input for the supplied    * {@link DataInput}.    *    * @param in    *          data input object    * @return typed bytes writable input corresponding to the supplied    *         {@link DataInput}.    */
specifier|public
specifier|static
name|TypedBytesWritableInput
name|get
parameter_list|(
name|DataInput
name|in
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|TypedBytesInput
operator|.
name|get
argument_list|(
name|in
argument_list|)
argument_list|)
return|;
block|}
comment|/** Creates a new instance of TypedBytesWritableInput. */
specifier|public
name|TypedBytesWritableInput
parameter_list|(
name|TypedBytesInput
name|in
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
block|}
comment|/** Creates a new instance of TypedBytesWritableInput. */
specifier|public
name|TypedBytesWritableInput
parameter_list|(
name|DataInput
name|din
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|TypedBytesInput
argument_list|(
name|din
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Writable
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|Type
name|type
init|=
name|in
operator|.
name|readType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BYTES
case|:
return|return
name|readBytes
argument_list|()
return|;
case|case
name|BYTE
case|:
return|return
name|readByte
argument_list|()
return|;
case|case
name|BOOL
case|:
return|return
name|readBoolean
argument_list|()
return|;
case|case
name|INT
case|:
return|return
name|readVInt
argument_list|()
return|;
case|case
name|SHORT
case|:
return|return
name|readShort
argument_list|()
return|;
case|case
name|LONG
case|:
return|return
name|readVLong
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
name|readFloat
argument_list|()
return|;
case|case
name|DOUBLE
case|:
return|return
name|readDouble
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
name|readText
argument_list|()
return|;
case|case
name|VECTOR
case|:
return|return
name|readArray
argument_list|()
return|;
case|case
name|MAP
case|:
return|return
name|readMap
argument_list|()
return|;
case|case
name|WRITABLE
case|:
return|return
name|readWritable
argument_list|()
return|;
case|case
name|ENDOFRECORD
case|:
return|return
literal|null
return|;
case|case
name|NULL
case|:
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unknown type"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Type
name|readTypeCode
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readType
argument_list|()
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|readType
parameter_list|()
throws|throws
name|IOException
block|{
name|Type
name|type
init|=
name|in
operator|.
name|readType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BYTES
case|:
return|return
name|BytesWritable
operator|.
name|class
return|;
case|case
name|BYTE
case|:
return|return
name|ByteWritable
operator|.
name|class
return|;
case|case
name|BOOL
case|:
return|return
name|BooleanWritable
operator|.
name|class
return|;
case|case
name|INT
case|:
return|return
name|VIntWritable
operator|.
name|class
return|;
case|case
name|LONG
case|:
return|return
name|VLongWritable
operator|.
name|class
return|;
case|case
name|FLOAT
case|:
return|return
name|FloatWritable
operator|.
name|class
return|;
case|case
name|SHORT
case|:
return|return
name|ShortWritable
operator|.
name|class
return|;
case|case
name|DOUBLE
case|:
return|return
name|DoubleWritable
operator|.
name|class
return|;
case|case
name|STRING
case|:
return|return
name|Text
operator|.
name|class
return|;
case|case
name|VECTOR
case|:
return|return
name|ArrayWritable
operator|.
name|class
return|;
case|case
name|MAP
case|:
return|return
name|MapWritable
operator|.
name|class
return|;
case|case
name|WRITABLE
case|:
return|return
name|Writable
operator|.
name|class
return|;
case|case
name|ENDOFRECORD
case|:
return|return
literal|null
return|;
case|case
name|NULL
case|:
return|return
name|NullWritable
operator|.
name|class
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unknown type"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|BytesWritable
name|readBytes
parameter_list|(
name|BytesWritable
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|in
operator|.
name|readBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|bw
operator|==
literal|null
condition|)
block|{
name|bw
operator|=
operator|new
name|BytesWritable
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bw
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|bw
return|;
block|}
specifier|public
name|BytesWritable
name|readBytes
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readBytes
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|ByteWritable
name|readByte
parameter_list|(
name|ByteWritable
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bw
operator|==
literal|null
condition|)
block|{
name|bw
operator|=
operator|new
name|ByteWritable
argument_list|()
expr_stmt|;
block|}
name|bw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|bw
return|;
block|}
specifier|public
name|ByteWritable
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readByte
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|BooleanWritable
name|readBoolean
parameter_list|(
name|BooleanWritable
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bw
operator|==
literal|null
condition|)
block|{
name|bw
operator|=
operator|new
name|BooleanWritable
argument_list|()
expr_stmt|;
block|}
name|bw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readBool
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|bw
return|;
block|}
specifier|public
name|BooleanWritable
name|readBoolean
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readBoolean
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|IntWritable
name|readInt
parameter_list|(
name|IntWritable
name|iw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|iw
operator|==
literal|null
condition|)
block|{
name|iw
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
block|}
name|iw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|iw
return|;
block|}
specifier|public
name|IntWritable
name|readInt
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readInt
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|ShortWritable
name|readShort
parameter_list|(
name|ShortWritable
name|sw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|sw
operator|==
literal|null
condition|)
block|{
name|sw
operator|=
operator|new
name|ShortWritable
argument_list|()
expr_stmt|;
block|}
name|sw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readShort
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sw
return|;
block|}
specifier|public
name|ShortWritable
name|readShort
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readShort
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|VIntWritable
name|readVInt
parameter_list|(
name|VIntWritable
name|iw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|iw
operator|==
literal|null
condition|)
block|{
name|iw
operator|=
operator|new
name|VIntWritable
argument_list|()
expr_stmt|;
block|}
name|iw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|iw
return|;
block|}
specifier|public
name|VIntWritable
name|readVInt
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readVInt
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|LongWritable
name|readLong
parameter_list|(
name|LongWritable
name|lw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lw
operator|==
literal|null
condition|)
block|{
name|lw
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
block|}
name|lw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|lw
return|;
block|}
specifier|public
name|LongWritable
name|readLong
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readLong
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|VLongWritable
name|readVLong
parameter_list|(
name|VLongWritable
name|lw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lw
operator|==
literal|null
condition|)
block|{
name|lw
operator|=
operator|new
name|VLongWritable
argument_list|()
expr_stmt|;
block|}
name|lw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|lw
return|;
block|}
specifier|public
name|VLongWritable
name|readVLong
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readVLong
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|FloatWritable
name|readFloat
parameter_list|(
name|FloatWritable
name|fw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fw
operator|==
literal|null
condition|)
block|{
name|fw
operator|=
operator|new
name|FloatWritable
argument_list|()
expr_stmt|;
block|}
name|fw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|fw
return|;
block|}
specifier|public
name|FloatWritable
name|readFloat
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readFloat
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|DoubleWritable
name|readDouble
parameter_list|(
name|DoubleWritable
name|dw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dw
operator|==
literal|null
condition|)
block|{
name|dw
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
block|}
name|dw
operator|.
name|set
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|dw
return|;
block|}
specifier|public
name|DoubleWritable
name|readDouble
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readDouble
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|Text
name|readText
parameter_list|(
name|Text
name|t
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
name|t
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
block|}
name|t
operator|.
name|set
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
specifier|public
name|Text
name|readText
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readText
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|ArrayWritable
name|readArray
parameter_list|(
name|ArrayWritable
name|aw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|aw
operator|==
literal|null
condition|)
block|{
name|aw
operator|=
operator|new
name|ArrayWritable
argument_list|(
name|TypedBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|aw
operator|.
name|getValueClass
argument_list|()
operator|.
name|equals
argument_list|(
name|TypedBytesWritable
operator|.
name|class
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"value class has to be TypedBytesWritable"
argument_list|)
throw|;
block|}
name|int
name|length
init|=
name|in
operator|.
name|readVectorHeader
argument_list|()
decl_stmt|;
name|Writable
index|[]
name|writables
init|=
operator|new
name|Writable
index|[
name|length
index|]
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
name|writables
index|[
name|i
index|]
operator|=
operator|new
name|TypedBytesWritable
argument_list|(
name|in
operator|.
name|readRaw
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|aw
operator|.
name|set
argument_list|(
name|writables
argument_list|)
expr_stmt|;
return|return
name|aw
return|;
block|}
specifier|public
name|ArrayWritable
name|readArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readArray
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|MapWritable
name|readMap
parameter_list|(
name|MapWritable
name|mw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|mw
operator|==
literal|null
condition|)
block|{
name|mw
operator|=
operator|new
name|MapWritable
argument_list|()
expr_stmt|;
block|}
name|int
name|length
init|=
name|in
operator|.
name|readMapHeader
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
name|Writable
name|key
init|=
name|read
argument_list|()
decl_stmt|;
name|Writable
name|value
init|=
name|read
argument_list|()
decl_stmt|;
name|mw
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
name|mw
return|;
block|}
specifier|public
name|MapWritable
name|readMap
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readMap
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|SortedMapWritable
name|readSortedMap
parameter_list|(
name|SortedMapWritable
name|mw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|mw
operator|==
literal|null
condition|)
block|{
name|mw
operator|=
operator|new
name|SortedMapWritable
argument_list|()
expr_stmt|;
block|}
name|int
name|length
init|=
name|in
operator|.
name|readMapHeader
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
name|WritableComparable
name|key
init|=
operator|(
name|WritableComparable
operator|)
name|read
argument_list|()
decl_stmt|;
name|Writable
name|value
init|=
name|read
argument_list|()
decl_stmt|;
name|mw
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
name|mw
return|;
block|}
specifier|public
name|SortedMapWritable
name|readSortedMap
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readSortedMap
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|Writable
name|readWritable
parameter_list|(
name|Writable
name|writable
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|dis
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|in
operator|.
name|readBytes
argument_list|()
argument_list|)
decl_stmt|;
name|dis
operator|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
expr_stmt|;
name|String
name|className
init|=
name|WritableUtils
operator|.
name|readString
argument_list|(
name|dis
argument_list|)
decl_stmt|;
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|cls
init|=
name|conf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|Writable
operator|.
name|class
argument_list|)
decl_stmt|;
name|writable
operator|=
operator|(
name|Writable
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|writable
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|className
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"wrong Writable class given"
argument_list|)
throw|;
block|}
name|writable
operator|.
name|readFields
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
name|dis
operator|=
literal|null
expr_stmt|;
return|return
name|writable
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|dis
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Writable
name|readWritable
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|readWritable
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
block|}
end_class

end_unit

