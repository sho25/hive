begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
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
name|DataOutput
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
operator|.
name|Entry
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

begin_class
specifier|public
specifier|abstract
class|class
name|ReaderWriter
block|{
specifier|private
specifier|static
specifier|final
name|String
name|UTF8
init|=
literal|"UTF-8"
decl_stmt|;
specifier|public
specifier|static
name|Object
name|readDatum
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|type
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DataType
operator|.
name|STRING
case|:
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|in
operator|.
name|readInt
argument_list|()
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|buffer
argument_list|,
name|UTF8
argument_list|)
return|;
case|case
name|DataType
operator|.
name|INTEGER
case|:
name|VIntWritable
name|vint
init|=
operator|new
name|VIntWritable
argument_list|()
decl_stmt|;
name|vint
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|vint
operator|.
name|get
argument_list|()
return|;
case|case
name|DataType
operator|.
name|LONG
case|:
name|VLongWritable
name|vlong
init|=
operator|new
name|VLongWritable
argument_list|()
decl_stmt|;
name|vlong
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|vlong
operator|.
name|get
argument_list|()
return|;
case|case
name|DataType
operator|.
name|FLOAT
case|:
return|return
name|in
operator|.
name|readFloat
argument_list|()
return|;
case|case
name|DataType
operator|.
name|DOUBLE
case|:
return|return
name|in
operator|.
name|readDouble
argument_list|()
return|;
case|case
name|DataType
operator|.
name|BOOLEAN
case|:
return|return
name|in
operator|.
name|readBoolean
argument_list|()
return|;
case|case
name|DataType
operator|.
name|BYTE
case|:
return|return
name|in
operator|.
name|readByte
argument_list|()
return|;
case|case
name|DataType
operator|.
name|SHORT
case|:
return|return
name|in
operator|.
name|readShort
argument_list|()
return|;
case|case
name|DataType
operator|.
name|NULL
case|:
return|return
literal|null
return|;
case|case
name|DataType
operator|.
name|BINARY
case|:
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|ba
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|ba
argument_list|)
expr_stmt|;
return|return
name|ba
return|;
case|case
name|DataType
operator|.
name|MAP
case|:
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|m
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|(
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|m
operator|.
name|put
argument_list|(
name|readDatum
argument_list|(
name|in
argument_list|)
argument_list|,
name|readDatum
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|m
return|;
case|case
name|DataType
operator|.
name|LIST
case|:
name|int
name|sz
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|sz
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
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|readDatum
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected data type "
operator|+
name|type
operator|+
literal|" found in stream."
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|writeDatum
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|Object
name|val
parameter_list|)
throws|throws
name|IOException
block|{
comment|// write the data type
name|byte
name|type
init|=
name|DataType
operator|.
name|findType
argument_list|(
name|val
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DataType
operator|.
name|LIST
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|LIST
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|val
decl_stmt|;
name|int
name|sz
init|=
name|list
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|sz
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
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|writeDatum
argument_list|(
name|out
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return;
case|case
name|DataType
operator|.
name|MAP
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|MAP
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|m
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|val
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|m
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|?
argument_list|>
name|i
init|=
name|m
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|i
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
init|=
operator|(
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|writeDatum
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|writeDatum
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return;
case|case
name|DataType
operator|.
name|INTEGER
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
operator|new
name|VIntWritable
argument_list|(
operator|(
name|Integer
operator|)
name|val
argument_list|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|LONG
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|LONG
argument_list|)
expr_stmt|;
operator|new
name|VLongWritable
argument_list|(
operator|(
name|Long
operator|)
name|val
argument_list|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|FLOAT
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
operator|(
name|Float
operator|)
name|val
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|DOUBLE
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
operator|(
name|Double
operator|)
name|val
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|BOOLEAN
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
operator|(
name|Boolean
operator|)
name|val
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|BYTE
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|BYTE
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|Byte
operator|)
name|val
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|SHORT
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|SHORT
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeShort
argument_list|(
operator|(
name|Short
operator|)
name|val
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|STRING
case|:
name|String
name|s
init|=
operator|(
name|String
operator|)
name|val
decl_stmt|;
name|byte
index|[]
name|utfBytes
init|=
name|s
operator|.
name|getBytes
argument_list|(
name|ReaderWriter
operator|.
name|UTF8
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|STRING
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|utfBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|utfBytes
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|BINARY
case|:
name|byte
index|[]
name|ba
init|=
operator|(
name|byte
index|[]
operator|)
name|val
decl_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|BINARY
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|ba
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|ba
argument_list|)
expr_stmt|;
return|return;
case|case
name|DataType
operator|.
name|NULL
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|DataType
operator|.
name|NULL
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected data type "
operator|+
name|type
operator|+
literal|" found in stream."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

