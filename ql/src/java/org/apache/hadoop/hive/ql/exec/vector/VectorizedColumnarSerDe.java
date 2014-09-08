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
operator|.
name|vector
package|;
end_package

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
name|sql
operator|.
name|Timestamp
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
name|metadata
operator|.
name|HiveException
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
name|SerDe
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
name|SerDeStats
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
name|columnar
operator|.
name|BytesRefArrayWritable
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
name|columnar
operator|.
name|ColumnarSerDe
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
name|DateWritable
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
name|lazy
operator|.
name|LazyDate
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
name|LazyLong
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
name|LazyTimestamp
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
name|LazyUtils
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
name|StructField
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
name|StructObjectInspector
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
name|DataOutputBuffer
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
name|ObjectWritable
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
comment|/**  * VectorizedColumnarSerDe is used by Vectorized query execution engine  * for columnar based storage supported by RCFile.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedColumnarSerDe
extends|extends
name|ColumnarSerDe
implements|implements
name|VectorizedSerde
block|{
specifier|public
name|VectorizedColumnarSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
specifier|private
specifier|final
name|BytesRefArrayWritable
index|[]
name|byteRefArray
init|=
operator|new
name|BytesRefArrayWritable
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|final
name|ObjectWritable
name|ow
init|=
operator|new
name|ObjectWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ByteStream
operator|.
name|Output
name|serializeVectorStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
comment|/**    * Serialize a vectorized row batch    *    * @param vrg    *          Vectorized row batch to serialize    * @param objInspector    *          The ObjectInspector for the row object    * @return The serialized Writable object    * @throws SerDeException    * @see SerDe#serialize(Object, ObjectInspector)    */
annotation|@
name|Override
specifier|public
name|Writable
name|serializeVector
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
comment|// Validate that the OI is of struct type
if|if
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" can only serialize struct types, but we got: "
operator|+
name|objInspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|vrg
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
comment|// Reset the byte buffer
name|serializeVectorStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|int
name|rowIndex
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
comment|// If selectedInUse is true then we need to serialize only
comment|// the selected indexes
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|rowIndex
operator|=
name|batch
operator|.
name|selected
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
name|rowIndex
operator|=
name|i
expr_stmt|;
block|}
name|BytesRefArrayWritable
name|byteRow
init|=
name|byteRefArray
index|[
name|i
index|]
decl_stmt|;
name|int
name|numCols
init|=
name|fields
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|byteRow
operator|==
literal|null
condition|)
block|{
name|byteRow
operator|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|numCols
argument_list|)
expr_stmt|;
name|byteRefArray
index|[
name|i
index|]
operator|=
name|byteRow
expr_stmt|;
block|}
name|byteRow
operator|.
name|resetValid
argument_list|(
name|numCols
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|batch
operator|.
name|projectionSize
condition|;
name|p
operator|++
control|)
block|{
name|int
name|k
init|=
name|batch
operator|.
name|projectedColumns
index|[
name|p
index|]
decl_stmt|;
name|ObjectInspector
name|foi
init|=
name|fields
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|ColumnVector
name|currentColVector
init|=
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
switch|switch
condition|(
name|foi
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|foi
decl_stmt|;
if|if
condition|(
operator|!
name|currentColVector
operator|.
name|noNulls
operator|&&
operator|(
name|currentColVector
operator|.
name|isRepeating
operator|||
name|currentColVector
operator|.
name|isNull
index|[
name|rowIndex
index|]
operator|)
condition|)
block|{
comment|// The column is null hence write null value
name|serializeVectorStream
operator|.
name|write
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// If here then the vector value is not null.
if|if
condition|(
name|currentColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// If the vector has repeating values then set rowindex to zero
name|rowIndex
operator|=
literal|0
expr_stmt|;
block|}
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
comment|// In vectorization true is stored as 1 and false as 0
name|boolean
name|b
init|=
name|lcv
operator|.
name|vector
index|[
name|rowIndex
index|]
operator|==
literal|1
condition|?
literal|true
else|:
literal|false
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|serializeVectorStream
operator|.
name|write
argument_list|(
name|LazyUtils
operator|.
name|trueBytes
argument_list|,
literal|0
argument_list|,
name|LazyUtils
operator|.
name|trueBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serializeVectorStream
operator|.
name|write
argument_list|(
name|LazyUtils
operator|.
name|trueBytes
argument_list|,
literal|0
argument_list|,
name|LazyUtils
operator|.
name|trueBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
name|LazyLong
operator|.
name|writeUTF8
argument_list|(
name|serializeVectorStream
argument_list|,
name|lcv
operator|.
name|vector
index|[
name|rowIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
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
name|dcv
operator|.
name|vector
index|[
name|rowIndex
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|serializeVectorStream
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
case|case
name|BINARY
case|:
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|bcv
operator|.
name|vector
index|[
name|rowIndex
index|]
decl_stmt|;
name|serializeVectorStream
operator|.
name|write
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
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
block|{
comment|// Is it correct to escape CHAR and VARCHAR?
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
name|LazyUtils
operator|.
name|writeEscaped
argument_list|(
name|serializeVectorStream
argument_list|,
name|bcv
operator|.
name|vector
index|[
name|rowIndex
index|]
argument_list|,
name|bcv
operator|.
name|start
index|[
name|rowIndex
index|]
argument_list|,
name|bcv
operator|.
name|length
index|[
name|rowIndex
index|]
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|TIMESTAMP
case|:
name|LongColumnVector
name|tcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
name|long
name|timeInNanoSec
init|=
name|tcv
operator|.
name|vector
index|[
name|rowIndex
index|]
decl_stmt|;
name|Timestamp
name|t
init|=
operator|new
name|Timestamp
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|TimestampUtils
operator|.
name|assignTimeInNanoSec
argument_list|(
name|timeInNanoSec
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|TimestampWritable
name|tw
init|=
operator|new
name|TimestampWritable
argument_list|()
decl_stmt|;
name|tw
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|LazyTimestamp
operator|.
name|writeUTF8
argument_list|(
name|serializeVectorStream
argument_list|,
name|tw
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|LongColumnVector
name|dacv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|k
index|]
decl_stmt|;
name|DateWritable
name|daw
init|=
operator|new
name|DateWritable
argument_list|(
operator|(
name|int
operator|)
name|dacv
operator|.
name|vector
index|[
name|rowIndex
index|]
argument_list|)
decl_stmt|;
name|LazyDate
operator|.
name|writeUTF8
argument_list|(
name|serializeVectorStream
argument_list|,
name|daw
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Vectorizaton is not supported for datatype:"
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
break|break;
block|}
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Vectorizaton is not supported for datatype:"
operator|+
name|foi
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unknown ObjectInspector category!"
argument_list|)
throw|;
block|}
name|byteRow
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|set
argument_list|(
name|serializeVectorStream
operator|.
name|getData
argument_list|()
argument_list|,
name|count
argument_list|,
name|serializeVectorStream
operator|.
name|getLength
argument_list|()
operator|-
name|count
argument_list|)
expr_stmt|;
name|count
operator|=
name|serializeVectorStream
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
block|}
name|ow
operator|.
name|set
argument_list|(
name|byteRefArray
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|ow
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|BytesRefArrayWritable
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Ideally this should throw  UnsupportedOperationException as the serde is
comment|// vectorized serde. But since RC file reader does not support vectorized reading this
comment|// is left as it is. This function will be called from VectorizedRowBatchCtx::addRowToBatch
comment|// to deserialize the row one by one and populate the batch. Once RC file reader supports vectorized
comment|// reading this serde and be standalone serde with no dependency on ColumnarSerDe.
return|return
name|super
operator|.
name|deserialize
argument_list|(
name|blob
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Deserializes the rowBlob into Vectorized row batch    * @param rowBlob    *          rowBlob row batch to deserialize    * @param rowsInBlob    *          Total number of rows in rowBlob to deserialize    * @param reuseBatch    *          VectorizedRowBatch to which the rows should be serialized   *    * @throws SerDeException    */
annotation|@
name|Override
specifier|public
name|void
name|deserializeVector
parameter_list|(
name|Object
name|rowBlob
parameter_list|,
name|int
name|rowsInBlob
parameter_list|,
name|VectorizedRowBatch
name|reuseBatch
parameter_list|)
throws|throws
name|SerDeException
block|{
name|BytesRefArrayWritable
index|[]
name|refArray
init|=
operator|(
name|BytesRefArrayWritable
index|[]
operator|)
name|rowBlob
decl_stmt|;
name|DataOutputBuffer
name|buffer
init|=
operator|new
name|DataOutputBuffer
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
name|rowsInBlob
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|row
init|=
name|deserialize
argument_list|(
name|refArray
index|[
name|i
index|]
argument_list|)
decl_stmt|;
try|try
block|{
name|VectorizedBatchUtil
operator|.
name|addRowToBatch
argument_list|(
name|row
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|cachedObjectInspector
argument_list|,
name|i
argument_list|,
name|reuseBatch
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

