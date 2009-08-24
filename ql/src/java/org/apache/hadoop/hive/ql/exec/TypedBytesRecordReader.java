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
name|ql
operator|.
name|exec
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
name|io
operator|.
name|InputStream
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
name|util
operator|.
name|ArrayList
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
name|util
operator|.
name|typedbytes
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|util
operator|.
name|typedbytes
operator|.
name|TypedBytesWritableInput
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
name|util
operator|.
name|typedbytes
operator|.
name|TypedBytesWritableOutput
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
name|NonSyncDataOutputBuffer
name|barrStr
init|=
operator|new
name|NonSyncDataOutputBuffer
argument_list|()
decl_stmt|;
name|TypedBytesWritableOutput
name|tbOut
init|=
operator|new
name|TypedBytesWritableOutput
argument_list|(
name|barrStr
argument_list|)
decl_stmt|;
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
specifier|public
name|void
name|initialize
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Configuration
name|conf
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
return|return
operator|-
literal|1
return|;
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
name|row
operator|.
name|add
argument_list|(
name|wrt
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BYTE
case|:
block|{
name|ByteWritable
name|bw
init|=
operator|(
name|ByteWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readByte
argument_list|(
name|bw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeByte
argument_list|(
name|bw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|BOOL
case|:
block|{
name|BooleanWritable
name|bw
init|=
operator|(
name|BooleanWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readBoolean
argument_list|(
name|bw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeBoolean
argument_list|(
name|bw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INT
case|:
block|{
name|IntWritable
name|iw
init|=
operator|(
name|IntWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readInt
argument_list|(
name|iw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeInt
argument_list|(
name|iw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|SHORT
case|:
block|{
name|ShortWritable
name|sw
init|=
operator|(
name|ShortWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readShort
argument_list|(
name|sw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeShort
argument_list|(
name|sw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|LONG
case|:
block|{
name|LongWritable
name|lw
init|=
operator|(
name|LongWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readLong
argument_list|(
name|lw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeLong
argument_list|(
name|lw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|FLOAT
case|:
block|{
name|FloatWritable
name|fw
init|=
operator|(
name|FloatWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readFloat
argument_list|(
name|fw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeFloat
argument_list|(
name|fw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DOUBLE
case|:
block|{
name|DoubleWritable
name|dw
init|=
operator|(
name|DoubleWritable
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readDouble
argument_list|(
name|dw
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeDouble
argument_list|(
name|dw
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|STRING
case|:
block|{
name|Text
name|txt
init|=
operator|(
name|Text
operator|)
name|row
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|tbIn
operator|.
name|readText
argument_list|(
name|txt
argument_list|)
expr_stmt|;
name|tbOut
operator|.
name|writeText
argument_list|(
name|txt
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
assert|assert
literal|false
assert|;
comment|// should never come here
block|}
name|pos
operator|++
expr_stmt|;
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
name|din
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

