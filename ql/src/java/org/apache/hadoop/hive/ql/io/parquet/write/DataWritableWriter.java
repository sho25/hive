begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
operator|.
name|write
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|writable
operator|.
name|BigDecimalWritable
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
name|parquet
operator|.
name|writable
operator|.
name|BinaryWritable
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
name|Writable
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|ParquetEncodingException
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|RecordConsumer
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|Type
import|;
end_import

begin_comment
comment|/**  *  * DataWritableWriter is a writer,  * that will read an ArrayWritable and give the data to parquet  * with the expected schema  *  */
end_comment

begin_class
specifier|public
class|class
name|DataWritableWriter
block|{
specifier|private
specifier|final
name|RecordConsumer
name|recordConsumer
decl_stmt|;
specifier|private
specifier|final
name|GroupType
name|schema
decl_stmt|;
specifier|public
name|DataWritableWriter
parameter_list|(
specifier|final
name|RecordConsumer
name|recordConsumer
parameter_list|,
specifier|final
name|GroupType
name|schema
parameter_list|)
block|{
name|this
operator|.
name|recordConsumer
operator|=
name|recordConsumer
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|ArrayWritable
name|arr
parameter_list|)
block|{
if|if
condition|(
name|arr
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|recordConsumer
operator|.
name|startMessage
argument_list|()
expr_stmt|;
name|writeData
argument_list|(
name|arr
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|recordConsumer
operator|.
name|endMessage
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeData
parameter_list|(
specifier|final
name|ArrayWritable
name|arr
parameter_list|,
specifier|final
name|GroupType
name|type
parameter_list|)
block|{
if|if
condition|(
name|arr
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|int
name|fieldCount
init|=
name|type
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
name|Writable
index|[]
name|values
init|=
name|arr
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|field
init|=
literal|0
init|;
name|field
operator|<
name|fieldCount
condition|;
operator|++
name|field
control|)
block|{
specifier|final
name|Type
name|fieldType
init|=
name|type
operator|.
name|getType
argument_list|(
name|field
argument_list|)
decl_stmt|;
specifier|final
name|String
name|fieldName
init|=
name|fieldType
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|final
name|Writable
name|value
init|=
name|values
index|[
name|field
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|recordConsumer
operator|.
name|startField
argument_list|(
name|fieldName
argument_list|,
name|field
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldType
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
name|writePrimitive
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|recordConsumer
operator|.
name|startGroup
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|ArrayWritable
condition|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|asGroupType
argument_list|()
operator|.
name|getRepetition
argument_list|()
operator|.
name|equals
argument_list|(
name|Type
operator|.
name|Repetition
operator|.
name|REPEATED
argument_list|)
condition|)
block|{
name|writeArray
argument_list|(
operator|(
name|ArrayWritable
operator|)
name|value
argument_list|,
name|fieldType
operator|.
name|asGroupType
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writeData
argument_list|(
operator|(
name|ArrayWritable
operator|)
name|value
argument_list|,
name|fieldType
operator|.
name|asGroupType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParquetEncodingException
argument_list|(
literal|"This should be an ArrayWritable or MapWritable: "
operator|+
name|value
argument_list|)
throw|;
block|}
name|recordConsumer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
name|recordConsumer
operator|.
name|endField
argument_list|(
name|fieldName
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeArray
parameter_list|(
specifier|final
name|ArrayWritable
name|array
parameter_list|,
specifier|final
name|GroupType
name|type
parameter_list|)
block|{
if|if
condition|(
name|array
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|Writable
index|[]
name|subValues
init|=
name|array
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|int
name|fieldCount
init|=
name|type
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|field
init|=
literal|0
init|;
name|field
operator|<
name|fieldCount
condition|;
operator|++
name|field
control|)
block|{
specifier|final
name|Type
name|subType
init|=
name|type
operator|.
name|getType
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|recordConsumer
operator|.
name|startField
argument_list|(
name|subType
operator|.
name|getName
argument_list|()
argument_list|,
name|field
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
name|subValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Writable
name|subValue
init|=
name|subValues
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|subValue
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|subType
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
if|if
condition|(
name|subValue
operator|instanceof
name|ArrayWritable
condition|)
block|{
name|writePrimitive
argument_list|(
operator|(
operator|(
name|ArrayWritable
operator|)
name|subValue
operator|)
operator|.
name|get
argument_list|()
index|[
name|field
index|]
argument_list|)
expr_stmt|;
comment|// 0 ?
block|}
else|else
block|{
name|writePrimitive
argument_list|(
name|subValue
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
operator|(
name|subValue
operator|instanceof
name|ArrayWritable
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"This should be a ArrayWritable: "
operator|+
name|subValue
argument_list|)
throw|;
block|}
else|else
block|{
name|recordConsumer
operator|.
name|startGroup
argument_list|()
expr_stmt|;
name|writeData
argument_list|(
operator|(
name|ArrayWritable
operator|)
name|subValue
argument_list|,
name|subType
operator|.
name|asGroupType
argument_list|()
argument_list|)
expr_stmt|;
name|recordConsumer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|recordConsumer
operator|.
name|endField
argument_list|(
name|subType
operator|.
name|getName
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writePrimitive
parameter_list|(
specifier|final
name|Writable
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|value
operator|instanceof
name|DoubleWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addDouble
argument_list|(
operator|(
operator|(
name|DoubleWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|BooleanWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addBoolean
argument_list|(
operator|(
operator|(
name|BooleanWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|FloatWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addFloat
argument_list|(
operator|(
operator|(
name|FloatWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|IntWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addInteger
argument_list|(
operator|(
operator|(
name|IntWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|LongWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addLong
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|ShortWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addInteger
argument_list|(
operator|(
operator|(
name|ShortWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|ByteWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addInteger
argument_list|(
operator|(
operator|(
name|ByteWritable
operator|)
name|value
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|BigDecimalWritable
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"BigDecimal writing not implemented"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|BinaryWritable
condition|)
block|{
name|recordConsumer
operator|.
name|addBinary
argument_list|(
operator|(
operator|(
name|BinaryWritable
operator|)
name|value
operator|)
operator|.
name|getBinary
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown value type: "
operator|+
name|value
operator|+
literal|" "
operator|+
name|value
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

