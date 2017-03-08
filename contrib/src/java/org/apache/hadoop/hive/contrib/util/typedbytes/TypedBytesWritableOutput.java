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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_comment
comment|/**  * Provides functionality for writing Writable objects as typed bytes.  *  * @see TypedBytesOutput  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesWritableOutput
block|{
specifier|private
name|TypedBytesOutput
name|out
decl_stmt|;
specifier|private
name|TypedBytesWritableOutput
parameter_list|()
block|{   }
specifier|private
name|void
name|setTypedBytesOutput
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
specifier|private
specifier|static
name|ThreadLocal
name|tbOut
init|=
operator|new
name|ThreadLocal
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Object
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|TypedBytesWritableOutput
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Get a thread-local typed bytes writable input for the supplied    * {@link TypedBytesOutput}.    *    * @param out    *          typed bytes output object    * @return typed bytes writable output corresponding to the supplied    *         {@link TypedBytesOutput}.    */
specifier|public
specifier|static
name|TypedBytesWritableOutput
name|get
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|TypedBytesWritableOutput
name|bout
init|=
operator|(
name|TypedBytesWritableOutput
operator|)
name|tbOut
operator|.
name|get
argument_list|()
decl_stmt|;
name|bout
operator|.
name|setTypedBytesOutput
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return
name|bout
return|;
block|}
comment|/**    * Get a thread-local typed bytes writable output for the supplied    * {@link DataOutput}.    *    * @param out    *          data output object    * @return typed bytes writable output corresponding to the supplied    *         {@link DataOutput}.    */
specifier|public
specifier|static
name|TypedBytesWritableOutput
name|get
parameter_list|(
name|DataOutput
name|out
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|TypedBytesOutput
operator|.
name|get
argument_list|(
name|out
argument_list|)
argument_list|)
return|;
block|}
comment|/** Creates a new instance of TypedBytesWritableOutput. */
specifier|public
name|TypedBytesWritableOutput
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
comment|/** Creates a new instance of TypedBytesWritableOutput. */
specifier|public
name|TypedBytesWritableOutput
parameter_list|(
name|DataOutput
name|dout
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|TypedBytesOutput
argument_list|(
name|dout
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|w
operator|instanceof
name|TypedBytesWritable
condition|)
block|{
name|writeTypedBytes
argument_list|(
operator|(
name|TypedBytesWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|BytesWritable
condition|)
block|{
name|writeBytes
argument_list|(
operator|(
name|BytesWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|ByteWritable
condition|)
block|{
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
name|w
operator|instanceof
name|BooleanWritable
condition|)
block|{
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
name|w
operator|instanceof
name|IntWritable
condition|)
block|{
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
name|w
operator|instanceof
name|VIntWritable
condition|)
block|{
name|writeVInt
argument_list|(
operator|(
name|VIntWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|LongWritable
condition|)
block|{
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
name|w
operator|instanceof
name|VLongWritable
condition|)
block|{
name|writeVLong
argument_list|(
operator|(
name|VLongWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|FloatWritable
condition|)
block|{
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
name|w
operator|instanceof
name|DoubleWritable
condition|)
block|{
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
name|w
operator|instanceof
name|Text
condition|)
block|{
name|writeText
argument_list|(
operator|(
name|Text
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|ShortWritable
condition|)
block|{
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
name|w
operator|instanceof
name|ArrayWritable
condition|)
block|{
name|writeArray
argument_list|(
operator|(
name|ArrayWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|MapWritable
condition|)
block|{
name|writeMap
argument_list|(
operator|(
name|MapWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|SortedMapWritable
condition|)
block|{
name|writeSortedMap
argument_list|(
operator|(
name|SortedMapWritable
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|NullWritable
operator|||
name|w
operator|==
literal|null
condition|)
block|{
name|writeNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|writeWritable
argument_list|(
name|w
argument_list|)
expr_stmt|;
comment|// last resort
block|}
block|}
specifier|public
name|void
name|writeTypedBytes
parameter_list|(
name|TypedBytesWritable
name|tbw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeRaw
argument_list|(
name|tbw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|tbw
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeBytes
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
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeByte
parameter_list|(
name|ByteWritable
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeByte
argument_list|(
name|bw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeBoolean
parameter_list|(
name|BooleanWritable
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBool
argument_list|(
name|bw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeInt
parameter_list|(
name|IntWritable
name|iw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|iw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeVInt
parameter_list|(
name|VIntWritable
name|viw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|viw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeLong
parameter_list|(
name|LongWritable
name|lw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|lw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeVLong
parameter_list|(
name|VLongWritable
name|vlw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|vlw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeFloat
parameter_list|(
name|FloatWritable
name|fw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeFloat
argument_list|(
name|fw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeDouble
parameter_list|(
name|DoubleWritable
name|dw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|dw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeShort
parameter_list|(
name|ShortWritable
name|sw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeShort
argument_list|(
name|sw
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeText
parameter_list|(
name|Text
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|t
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeArray
parameter_list|(
name|ArrayWritable
name|aw
parameter_list|)
throws|throws
name|IOException
block|{
name|Writable
index|[]
name|writables
init|=
name|aw
operator|.
name|get
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeVectorHeader
argument_list|(
name|writables
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Writable
name|writable
range|:
name|writables
control|)
block|{
name|write
argument_list|(
name|writable
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|writeMap
parameter_list|(
name|MapWritable
name|mw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeMapHeader
argument_list|(
name|mw
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|>
name|entry
range|:
name|mw
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|write
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|writeSortedMap
parameter_list|(
name|SortedMapWritable
name|smw
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeMapHeader
argument_list|(
name|smw
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure it compiles with both Hadoop 2 and Hadoop 3.
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
argument_list|>
name|entrySet
init|=
call|(
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
argument_list|>
call|)
argument_list|(
operator|(
name|Object
operator|)
name|smw
operator|.
name|entrySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|entry
range|:
name|entrySet
control|)
block|{
name|write
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|writeNull
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|writeWritable
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
name|DataOutputStream
name|dos
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|dos
operator|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|dos
argument_list|,
name|w
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|w
operator|.
name|write
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|Type
operator|.
name|WRITABLE
operator|.
name|code
argument_list|)
expr_stmt|;
name|dos
operator|.
name|close
argument_list|()
expr_stmt|;
name|dos
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|dos
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|writeEndOfRecord
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeEndOfRecord
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

