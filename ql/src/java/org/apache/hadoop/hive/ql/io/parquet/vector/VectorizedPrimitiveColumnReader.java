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
name|vector
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
name|exec
operator|.
name|vector
operator|.
name|BytesColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|ColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|DecimalColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|DoubleColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|TimestampColumnVector
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
name|timestamp
operator|.
name|NanoTime
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
name|timestamp
operator|.
name|NanoTimeUtils
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
name|TypeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|column
operator|.
name|ColumnDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|column
operator|.
name|page
operator|.
name|PageReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|Type
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
name|ByteOrder
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

begin_comment
comment|/**  * It's column level Parquet reader which is used to read a batch of records for a column,  * part of the code is referred from Apache Spark and Apache Parquet.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedPrimitiveColumnReader
extends|extends
name|BaseVectorizedColumnReader
block|{
specifier|public
name|VectorizedPrimitiveColumnReader
parameter_list|(
name|ColumnDescriptor
name|descriptor
parameter_list|,
name|PageReader
name|pageReader
parameter_list|,
name|boolean
name|skipTimestampConversion
parameter_list|,
name|Type
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|descriptor
argument_list|,
name|pageReader
argument_list|,
name|skipTimestampConversion
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readBatch
parameter_list|(
name|int
name|total
parameter_list|,
name|ColumnVector
name|column
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|rowId
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|total
operator|>
literal|0
condition|)
block|{
comment|// Compute the number of values we want to read in this page.
name|int
name|leftInPage
init|=
call|(
name|int
call|)
argument_list|(
name|endOfPageValueCount
operator|-
name|valuesRead
argument_list|)
decl_stmt|;
if|if
condition|(
name|leftInPage
operator|==
literal|0
condition|)
block|{
name|readPage
argument_list|()
expr_stmt|;
name|leftInPage
operator|=
call|(
name|int
call|)
argument_list|(
name|endOfPageValueCount
operator|-
name|valuesRead
argument_list|)
expr_stmt|;
block|}
name|int
name|num
init|=
name|Math
operator|.
name|min
argument_list|(
name|total
argument_list|,
name|leftInPage
argument_list|)
decl_stmt|;
if|if
condition|(
name|isCurrentPageDictionaryEncoded
condition|)
block|{
name|LongColumnVector
name|dictionaryIds
init|=
operator|new
name|LongColumnVector
argument_list|()
decl_stmt|;
comment|// Read and decode dictionary ids.
name|readDictionaryIDs
argument_list|(
name|num
argument_list|,
name|dictionaryIds
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
name|decodeDictionaryIds
argument_list|(
name|rowId
argument_list|,
name|num
argument_list|,
name|column
argument_list|,
name|dictionaryIds
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// assign values in vector
name|readBatchHelper
argument_list|(
name|num
argument_list|,
name|column
argument_list|,
name|columnType
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
block|}
name|rowId
operator|+=
name|num
expr_stmt|;
name|total
operator|-=
name|num
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readBatchHelper
parameter_list|(
name|int
name|num
parameter_list|,
name|ColumnVector
name|column
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|PrimitiveTypeInfo
name|primitiveColumnType
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|columnType
decl_stmt|;
switch|switch
condition|(
name|primitiveColumnType
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
name|readIntegers
argument_list|(
name|num
argument_list|,
operator|(
name|LongColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|LONG
case|:
name|readLongs
argument_list|(
name|num
argument_list|,
operator|(
name|LongColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|readBooleans
argument_list|(
name|num
argument_list|,
operator|(
name|LongColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|readDoubles
argument_list|(
name|num
argument_list|,
operator|(
name|DoubleColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|readBinaries
argument_list|(
name|num
argument_list|,
operator|(
name|BytesColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|readFloats
argument_list|(
name|num
argument_list|,
operator|(
name|DoubleColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|readDecimal
argument_list|(
name|num
argument_list|,
operator|(
name|DecimalColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|readTimestamp
argument_list|(
name|num
argument_list|,
operator|(
name|TimestampColumnVector
operator|)
name|column
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unsupported type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|readDictionaryIDs
parameter_list|(
name|int
name|total
parameter_list|,
name|LongColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readValueDictionaryId
argument_list|()
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readIntegers
parameter_list|(
name|int
name|total
parameter_list|,
name|LongColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readInteger
argument_list|()
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readDoubles
parameter_list|(
name|int
name|total
parameter_list|,
name|DoubleColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readBooleans
parameter_list|(
name|int
name|total
parameter_list|,
name|LongColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readBoolean
argument_list|()
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readLongs
parameter_list|(
name|int
name|total
parameter_list|,
name|LongColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readFloats
parameter_list|(
name|int
name|total
parameter_list|,
name|DoubleColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|=
name|dataColumn
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readDecimal
parameter_list|(
name|int
name|total
parameter_list|,
name|DecimalColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
name|c
operator|.
name|precision
operator|=
operator|(
name|short
operator|)
name|type
operator|.
name|asPrimitiveType
argument_list|()
operator|.
name|getDecimalMetadata
argument_list|()
operator|.
name|getPrecision
argument_list|()
expr_stmt|;
name|c
operator|.
name|scale
operator|=
operator|(
name|short
operator|)
name|type
operator|.
name|asPrimitiveType
argument_list|()
operator|.
name|getDecimalMetadata
argument_list|()
operator|.
name|getScale
argument_list|()
expr_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|.
name|set
argument_list|(
name|dataColumn
operator|.
name|readBytes
argument_list|()
operator|.
name|getBytesUnsafe
argument_list|()
argument_list|,
name|c
operator|.
name|scale
argument_list|)
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
name|c
operator|.
name|vector
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|vector
index|[
name|rowId
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readBinaries
parameter_list|(
name|int
name|total
parameter_list|,
name|BytesColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
name|c
operator|.
name|setVal
argument_list|(
name|rowId
argument_list|,
name|dataColumn
operator|.
name|readBytes
argument_list|()
operator|.
name|getBytesUnsafe
argument_list|()
argument_list|)
expr_stmt|;
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
comment|// TODO figure out a better way to set repeat for Binary type
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readTimestamp
parameter_list|(
name|int
name|total
parameter_list|,
name|TimestampColumnVector
name|c
parameter_list|,
name|int
name|rowId
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|total
decl_stmt|;
while|while
condition|(
name|left
operator|>
literal|0
condition|)
block|{
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
if|if
condition|(
name|definitionLevel
operator|>=
name|maxDefLevel
condition|)
block|{
switch|switch
condition|(
name|descriptor
operator|.
name|getType
argument_list|()
condition|)
block|{
comment|//INT64 is not yet supported
case|case
name|INT96
case|:
name|NanoTime
name|nt
init|=
name|NanoTime
operator|.
name|fromBinary
argument_list|(
name|dataColumn
operator|.
name|readBytes
argument_list|()
argument_list|)
decl_stmt|;
name|Timestamp
name|ts
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt
argument_list|,
name|skipTimestampConversion
argument_list|)
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|rowId
argument_list|,
name|ts
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unsupported parquet logical type: "
operator|+
name|type
operator|.
name|getOriginalType
argument_list|()
operator|+
literal|" for timestamp"
argument_list|)
throw|;
block|}
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
name|c
operator|.
name|isRepeating
operator|&&
operator|(
operator|(
name|c
operator|.
name|time
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|time
index|[
name|rowId
index|]
operator|)
operator|&&
operator|(
name|c
operator|.
name|nanos
index|[
literal|0
index|]
operator|==
name|c
operator|.
name|nanos
index|[
name|rowId
index|]
operator|)
operator|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|isNull
index|[
name|rowId
index|]
operator|=
literal|true
expr_stmt|;
name|c
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|c
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|rowId
operator|++
expr_stmt|;
name|left
operator|--
expr_stmt|;
block|}
block|}
comment|/**    * Reads `num` values into column, decoding the values from `dictionaryIds` and `dictionary`.    */
specifier|private
name|void
name|decodeDictionaryIds
parameter_list|(
name|int
name|rowId
parameter_list|,
name|int
name|num
parameter_list|,
name|ColumnVector
name|column
parameter_list|,
name|LongColumnVector
name|dictionaryIds
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|dictionaryIds
operator|.
name|isNull
argument_list|,
name|rowId
argument_list|,
name|column
operator|.
name|isNull
argument_list|,
name|rowId
argument_list|,
name|num
argument_list|)
expr_stmt|;
if|if
condition|(
name|column
operator|.
name|noNulls
condition|)
block|{
name|column
operator|.
name|noNulls
operator|=
name|dictionaryIds
operator|.
name|noNulls
expr_stmt|;
block|}
name|column
operator|.
name|isRepeating
operator|=
name|column
operator|.
name|isRepeating
operator|&&
name|dictionaryIds
operator|.
name|isRepeating
expr_stmt|;
switch|switch
condition|(
name|descriptor
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|INT32
case|:
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
operator|(
operator|(
name|LongColumnVector
operator|)
name|column
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|dictionary
operator|.
name|decodeToInt
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INT64
case|:
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
operator|(
operator|(
name|LongColumnVector
operator|)
name|column
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|dictionary
operator|.
name|decodeToLong
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|FLOAT
case|:
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|column
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|dictionary
operator|.
name|decodeToFloat
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DOUBLE
case|:
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|column
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|dictionary
operator|.
name|decodeToDouble
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INT96
case|:
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
name|ByteBuffer
name|buf
init|=
name|dictionary
operator|.
name|decodeToBinary
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|.
name|toByteBuffer
argument_list|()
decl_stmt|;
name|buf
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
expr_stmt|;
name|long
name|timeOfDayNanos
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|int
name|julianDay
init|=
name|buf
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|NanoTime
name|nt
init|=
operator|new
name|NanoTime
argument_list|(
name|julianDay
argument_list|,
name|timeOfDayNanos
argument_list|)
decl_stmt|;
name|Timestamp
name|ts
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt
argument_list|,
name|skipTimestampConversion
argument_list|)
decl_stmt|;
operator|(
operator|(
name|TimestampColumnVector
operator|)
name|column
operator|)
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|BINARY
case|:
case|case
name|FIXED_LEN_BYTE_ARRAY
case|:
if|if
condition|(
name|column
operator|instanceof
name|BytesColumnVector
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
operator|(
operator|(
name|BytesColumnVector
operator|)
name|column
operator|)
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|dictionary
operator|.
name|decodeToBinary
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|.
name|getBytesUnsafe
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|DecimalColumnVector
name|decimalColumnVector
init|=
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|column
operator|)
decl_stmt|;
name|decimalColumnVector
operator|.
name|precision
operator|=
operator|(
name|short
operator|)
name|type
operator|.
name|asPrimitiveType
argument_list|()
operator|.
name|getDecimalMetadata
argument_list|()
operator|.
name|getPrecision
argument_list|()
expr_stmt|;
name|decimalColumnVector
operator|.
name|scale
operator|=
operator|(
name|short
operator|)
name|type
operator|.
name|asPrimitiveType
argument_list|()
operator|.
name|getDecimalMetadata
argument_list|()
operator|.
name|getScale
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|rowId
init|;
name|i
operator|<
name|rowId
operator|+
name|num
condition|;
operator|++
name|i
control|)
block|{
name|decimalColumnVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|dictionary
operator|.
name|decodeToBinary
argument_list|(
operator|(
name|int
operator|)
name|dictionaryIds
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|.
name|getBytesUnsafe
argument_list|()
argument_list|,
name|decimalColumnVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported type: "
operator|+
name|descriptor
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

