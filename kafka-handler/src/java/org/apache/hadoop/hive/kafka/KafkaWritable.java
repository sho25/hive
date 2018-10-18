begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|kafka
package|;
end_package

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
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|clients
operator|.
name|consumer
operator|.
name|ConsumerRecord
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Writable implementation of Kafka ConsumerRecord.  * Serialized in the form:  * {@code timestamp} long| {@code partition} (int) | {@code offset} (long) | {@code value.size()} (int) |  * {@code value} (byte []) | {@code recordKey.size()}| {@code recordKey (byte [])}  */
end_comment

begin_class
specifier|public
class|class
name|KafkaWritable
implements|implements
name|Writable
block|{
specifier|private
name|int
name|partition
decl_stmt|;
specifier|private
name|long
name|offset
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
name|byte
index|[]
name|recordKey
decl_stmt|;
name|void
name|set
parameter_list|(
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|consumerRecord
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|consumerRecord
operator|.
name|partition
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|consumerRecord
operator|.
name|timestamp
argument_list|()
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|consumerRecord
operator|.
name|offset
argument_list|()
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|consumerRecord
operator|.
name|value
argument_list|()
expr_stmt|;
name|this
operator|.
name|recordKey
operator|=
name|consumerRecord
operator|.
name|key
argument_list|()
expr_stmt|;
block|}
name|KafkaWritable
parameter_list|(
name|int
name|partition
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
annotation|@
name|Nullable
name|byte
index|[]
name|recordKey
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|recordKey
operator|=
name|recordKey
expr_stmt|;
block|}
name|KafkaWritable
parameter_list|(
name|int
name|partition
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
annotation|@
name|Nullable
name|byte
index|[]
name|recordKey
parameter_list|)
block|{
name|this
argument_list|(
name|partition
argument_list|,
operator|-
literal|1
argument_list|,
name|timestamp
argument_list|,
name|value
argument_list|,
name|recordKey
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"WeakerAccess"
argument_list|)
specifier|public
name|KafkaWritable
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
name|dataOutput
operator|.
name|writeLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|partition
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|writeLong
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|recordKey
operator|!=
literal|null
condition|)
block|{
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|recordKey
operator|.
name|length
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|recordKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataOutput
operator|.
name|writeInt
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
name|timestamp
operator|=
name|dataInput
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|partition
operator|=
name|dataInput
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|offset
operator|=
name|dataInput
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|int
name|dataSize
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|dataSize
operator|>
literal|0
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|dataSize
index|]
expr_stmt|;
name|dataInput
operator|.
name|readFully
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
block|}
name|int
name|keyArraySize
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyArraySize
operator|>
operator|-
literal|1
condition|)
block|{
name|recordKey
operator|=
operator|new
name|byte
index|[
name|keyArraySize
index|]
expr_stmt|;
name|dataInput
operator|.
name|readFully
argument_list|(
name|recordKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|recordKey
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|int
name|getPartition
parameter_list|()
block|{
return|return
name|partition
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"WeakerAccess"
argument_list|)
name|long
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Nullable
name|byte
index|[]
name|getRecordKey
parameter_list|()
block|{
return|return
name|recordKey
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|KafkaWritable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|KafkaWritable
name|writable
init|=
operator|(
name|KafkaWritable
operator|)
name|o
decl_stmt|;
return|return
name|partition
operator|==
name|writable
operator|.
name|partition
operator|&&
name|offset
operator|==
name|writable
operator|.
name|offset
operator|&&
name|timestamp
operator|==
name|writable
operator|.
name|timestamp
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|writable
operator|.
name|value
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|recordKey
argument_list|,
name|writable
operator|.
name|recordKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|Objects
operator|.
name|hash
argument_list|(
name|partition
argument_list|,
name|offset
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|recordKey
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"KafkaWritable{"
operator|+
literal|"partition="
operator|+
name|partition
operator|+
literal|", offset="
operator|+
name|offset
operator|+
literal|", timestamp="
operator|+
name|timestamp
operator|+
literal|", value="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|value
argument_list|)
operator|+
literal|", recordKey="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|recordKey
argument_list|)
operator|+
literal|'}'
return|;
block|}
name|Writable
name|getHiveWritable
parameter_list|(
name|MetadataColumn
name|metadataColumn
parameter_list|)
block|{
switch|switch
condition|(
name|metadataColumn
condition|)
block|{
case|case
name|OFFSET
case|:
return|return
operator|new
name|LongWritable
argument_list|(
name|getOffset
argument_list|()
argument_list|)
return|;
case|case
name|PARTITION
case|:
return|return
operator|new
name|IntWritable
argument_list|(
name|getPartition
argument_list|()
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|LongWritable
argument_list|(
name|getTimestamp
argument_list|()
argument_list|)
return|;
case|case
name|KEY
case|:
return|return
name|getRecordKey
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BytesWritable
argument_list|(
name|getRecordKey
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown metadata column ["
operator|+
name|metadataColumn
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

