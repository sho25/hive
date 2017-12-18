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
name|ListColumnVector
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
name|typeinfo
operator|.
name|ListTypeInfo
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

begin_comment
comment|/**  * It's column level Parquet reader which is used to read a batch of records for a list column.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedListColumnReader
extends|extends
name|BaseVectorizedColumnReader
block|{
comment|// The value read in last time
specifier|private
name|Object
name|lastValue
decl_stmt|;
comment|// flag to indicate if there is no data in parquet data page
specifier|private
name|boolean
name|eof
init|=
literal|false
decl_stmt|;
comment|// flag to indicate if it's the first time to read parquet data page with this instance
name|boolean
name|isFirstRow
init|=
literal|true
decl_stmt|;
specifier|public
name|VectorizedListColumnReader
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
name|ListColumnVector
name|lcv
init|=
operator|(
name|ListColumnVector
operator|)
name|column
decl_stmt|;
comment|// Because the length of ListColumnVector.child can't be known now,
comment|// the valueList will save all data for ListColumnVector temporary.
name|List
argument_list|<
name|Object
argument_list|>
name|valueList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
init|=
operator|(
call|(
name|PrimitiveTypeInfo
call|)
argument_list|(
operator|(
name|ListTypeInfo
operator|)
name|columnType
argument_list|)
operator|.
name|getListElementTypeInfo
argument_list|()
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
comment|// read the first row in parquet data page, this will be only happened once for this instance
if|if
condition|(
name|isFirstRow
condition|)
block|{
if|if
condition|(
operator|!
name|fetchNextValue
argument_list|(
name|category
argument_list|)
condition|)
block|{
return|return;
block|}
name|isFirstRow
operator|=
literal|false
expr_stmt|;
block|}
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|eof
operator|&&
name|index
operator|<
name|total
condition|)
block|{
comment|// add element to ListColumnVector one by one
name|addElement
argument_list|(
name|lcv
argument_list|,
name|valueList
argument_list|,
name|category
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
comment|// Decode the value if necessary
if|if
condition|(
name|isCurrentPageDictionaryEncoded
condition|)
block|{
name|valueList
operator|=
name|decodeDictionaryIds
argument_list|(
name|valueList
argument_list|)
expr_stmt|;
block|}
comment|// Convert valueList to array for the ListColumnVector.child
name|convertValueListToListColumnVector
argument_list|(
name|category
argument_list|,
name|lcv
argument_list|,
name|valueList
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|readPageIfNeed
parameter_list|()
throws|throws
name|IOException
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
comment|// no data left in current page, load data from new page
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
return|return
name|leftInPage
return|;
block|}
specifier|private
name|boolean
name|fetchNextValue
parameter_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|left
init|=
name|readPageIfNeed
argument_list|()
decl_stmt|;
if|if
condition|(
name|left
operator|>
literal|0
condition|)
block|{
comment|// get the values of repetition and definitionLevel
name|readRepetitionAndDefinitionLevels
argument_list|()
expr_stmt|;
comment|// read the data if it isn't null
if|if
condition|(
name|definitionLevel
operator|==
name|maxDefLevel
condition|)
block|{
if|if
condition|(
name|isCurrentPageDictionaryEncoded
condition|)
block|{
name|lastValue
operator|=
name|dataColumn
operator|.
name|readValueDictionaryId
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|lastValue
operator|=
name|readPrimitiveTypedRow
argument_list|(
name|category
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
else|else
block|{
name|eof
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
comment|/**    * The function will set all data from parquet data page for an element in ListColumnVector    */
specifier|private
name|void
name|addElement
parameter_list|(
name|ListColumnVector
name|lcv
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|elements
parameter_list|,
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|lcv
operator|.
name|offsets
index|[
name|index
index|]
operator|=
name|elements
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// Return directly if last value is null
if|if
condition|(
name|definitionLevel
operator|<
name|maxDefLevel
condition|)
block|{
name|lcv
operator|.
name|isNull
index|[
name|index
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|lengths
index|[
name|index
index|]
operator|=
literal|0
expr_stmt|;
comment|// fetch the data from parquet data page for next call
name|fetchNextValue
argument_list|(
name|category
argument_list|)
expr_stmt|;
return|return;
block|}
do|do
block|{
comment|// add all data for an element in ListColumnVector, get out the loop if there is no data or the data is for new element
name|elements
operator|.
name|add
argument_list|(
name|lastValue
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|fetchNextValue
argument_list|(
name|category
argument_list|)
operator|&&
operator|(
name|repetitionLevel
operator|!=
literal|0
operator|)
condition|)
do|;
name|lcv
operator|.
name|isNull
index|[
name|index
index|]
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|lengths
index|[
name|index
index|]
operator|=
name|elements
operator|.
name|size
argument_list|()
operator|-
name|lcv
operator|.
name|offsets
index|[
name|index
index|]
expr_stmt|;
block|}
specifier|private
name|Object
name|readPrimitiveTypedRow
parameter_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
parameter_list|)
block|{
switch|switch
condition|(
name|category
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
return|return
name|dataColumn
operator|.
name|readInteger
argument_list|()
return|;
case|case
name|DATE
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|LONG
case|:
return|return
name|dataColumn
operator|.
name|readLong
argument_list|()
return|;
case|case
name|BOOLEAN
case|:
return|return
name|dataColumn
operator|.
name|readBoolean
argument_list|()
condition|?
literal|1
else|:
literal|0
return|;
case|case
name|DOUBLE
case|:
return|return
name|dataColumn
operator|.
name|readDouble
argument_list|()
return|;
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
return|return
name|dataColumn
operator|.
name|readBytes
argument_list|()
operator|.
name|getBytesUnsafe
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
name|dataColumn
operator|.
name|readFloat
argument_list|()
return|;
case|case
name|DECIMAL
case|:
return|return
name|dataColumn
operator|.
name|readBytes
argument_list|()
operator|.
name|getBytesUnsafe
argument_list|()
return|;
case|case
name|INTERVAL_DAY_TIME
case|:
case|case
name|TIMESTAMP
case|:
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported type in the list: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
name|decodeDictionaryIds
parameter_list|(
name|List
name|valueList
parameter_list|)
block|{
name|int
name|total
init|=
name|valueList
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
name|resultList
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|intList
init|=
operator|(
name|List
argument_list|<
name|Integer
argument_list|>
operator|)
name|valueList
decl_stmt|;
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
name|resultList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|total
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
name|total
condition|;
operator|++
name|i
control|)
block|{
name|resultList
operator|.
name|add
argument_list|(
name|dictionary
operator|.
name|decodeToInt
argument_list|(
name|intList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INT64
case|:
name|resultList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|(
name|total
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
name|total
condition|;
operator|++
name|i
control|)
block|{
name|resultList
operator|.
name|add
argument_list|(
name|dictionary
operator|.
name|decodeToLong
argument_list|(
name|intList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|FLOAT
case|:
name|resultList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Float
argument_list|>
argument_list|(
name|total
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
name|total
condition|;
operator|++
name|i
control|)
block|{
name|resultList
operator|.
name|add
argument_list|(
name|dictionary
operator|.
name|decodeToFloat
argument_list|(
name|intList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DOUBLE
case|:
name|resultList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Double
argument_list|>
argument_list|(
name|total
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
name|total
condition|;
operator|++
name|i
control|)
block|{
name|resultList
operator|.
name|add
argument_list|(
name|dictionary
operator|.
name|decodeToDouble
argument_list|(
name|intList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
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
name|resultList
operator|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|total
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
name|total
condition|;
operator|++
name|i
control|)
block|{
name|resultList
operator|.
name|add
argument_list|(
name|dictionary
operator|.
name|decodeToBinary
argument_list|(
name|intList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|getBytesUnsafe
argument_list|()
argument_list|)
expr_stmt|;
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
return|return
name|resultList
return|;
block|}
comment|/**    * The lengths& offsets will be initialized as default size (1024),    * it should be set to the actual size according to the element number.    */
specifier|private
name|void
name|setChildrenInfo
parameter_list|(
name|ListColumnVector
name|lcv
parameter_list|,
name|int
name|itemNum
parameter_list|,
name|int
name|elementNum
parameter_list|)
block|{
name|lcv
operator|.
name|childCount
operator|=
name|itemNum
expr_stmt|;
name|long
index|[]
name|lcvLength
init|=
operator|new
name|long
index|[
name|elementNum
index|]
decl_stmt|;
name|long
index|[]
name|lcvOffset
init|=
operator|new
name|long
index|[
name|elementNum
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|lcv
operator|.
name|lengths
argument_list|,
literal|0
argument_list|,
name|lcvLength
argument_list|,
literal|0
argument_list|,
name|elementNum
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|lcv
operator|.
name|offsets
argument_list|,
literal|0
argument_list|,
name|lcvOffset
argument_list|,
literal|0
argument_list|,
name|elementNum
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|lengths
operator|=
name|lcvLength
expr_stmt|;
name|lcv
operator|.
name|offsets
operator|=
name|lcvOffset
expr_stmt|;
block|}
specifier|private
name|void
name|fillColumnVector
parameter_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
parameter_list|,
name|ListColumnVector
name|lcv
parameter_list|,
name|List
name|valueList
parameter_list|,
name|int
name|elementNum
parameter_list|)
block|{
name|int
name|total
init|=
name|valueList
operator|.
name|size
argument_list|()
decl_stmt|;
name|setChildrenInfo
argument_list|(
name|lcv
argument_list|,
name|total
argument_list|,
name|elementNum
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|category
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
case|case
name|BOOLEAN
case|:
name|lcv
operator|.
name|child
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|total
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|LongColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|List
argument_list|<
name|Integer
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
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
name|lcv
operator|.
name|child
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|total
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|LongColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|List
argument_list|<
name|Long
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DOUBLE
case|:
name|lcv
operator|.
name|child
operator|=
operator|new
name|DoubleColumnVector
argument_list|(
name|total
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|List
argument_list|<
name|Double
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
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
name|lcv
operator|.
name|child
operator|=
operator|new
name|BytesColumnVector
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|child
operator|.
name|init
argument_list|()
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|src
init|=
operator|(
operator|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
operator|(
operator|(
name|BytesColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|src
argument_list|,
literal|0
argument_list|,
name|src
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|FLOAT
case|:
name|lcv
operator|.
name|child
operator|=
operator|new
name|DoubleColumnVector
argument_list|(
name|total
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|List
argument_list|<
name|Float
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL
case|:
name|int
name|precision
init|=
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
decl_stmt|;
name|int
name|scale
init|=
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
decl_stmt|;
name|lcv
operator|.
name|child
operator|=
operator|new
name|DecimalColumnVector
argument_list|(
name|total
argument_list|,
name|precision
argument_list|,
name|scale
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
name|valueList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|set
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
operator|)
name|valueList
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
case|case
name|TIMESTAMP
case|:
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported type in the list: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
comment|/**    * Finish the result ListColumnVector with all collected information.    */
specifier|private
name|void
name|convertValueListToListColumnVector
parameter_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|category
parameter_list|,
name|ListColumnVector
name|lcv
parameter_list|,
name|List
name|valueList
parameter_list|,
name|int
name|elementNum
parameter_list|)
block|{
comment|// Fill the child of ListColumnVector with valueList
name|fillColumnVector
argument_list|(
name|category
argument_list|,
name|lcv
argument_list|,
name|valueList
argument_list|,
name|elementNum
argument_list|)
expr_stmt|;
name|setIsRepeating
argument_list|(
name|lcv
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setIsRepeating
parameter_list|(
name|ListColumnVector
name|lcv
parameter_list|)
block|{
name|ColumnVector
name|child0
init|=
name|getChildData
argument_list|(
name|lcv
argument_list|,
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|lcv
operator|.
name|offsets
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ColumnVector
name|currentChild
init|=
name|getChildData
argument_list|(
name|lcv
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|compareColumnVector
argument_list|(
name|child0
argument_list|,
name|currentChild
argument_list|)
condition|)
block|{
name|lcv
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
return|return;
block|}
block|}
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Get the child ColumnVector of ListColumnVector    */
specifier|private
name|ColumnVector
name|getChildData
parameter_list|(
name|ListColumnVector
name|lcv
parameter_list|,
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|lcv
operator|.
name|offsets
index|[
name|index
index|]
operator|>
name|Integer
operator|.
name|MAX_VALUE
operator|||
name|lcv
operator|.
name|lengths
index|[
name|index
index|]
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"The element number in list is out of scope."
argument_list|)
throw|;
block|}
if|if
condition|(
name|lcv
operator|.
name|isNull
index|[
name|index
index|]
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|start
init|=
operator|(
name|int
operator|)
name|lcv
operator|.
name|offsets
index|[
name|index
index|]
decl_stmt|;
name|int
name|length
init|=
operator|(
name|int
operator|)
name|lcv
operator|.
name|lengths
index|[
name|index
index|]
decl_stmt|;
name|ColumnVector
name|child
init|=
name|lcv
operator|.
name|child
decl_stmt|;
name|ColumnVector
name|resultCV
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|LongColumnVector
condition|)
block|{
name|resultCV
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|length
argument_list|)
expr_stmt|;
try|try
block|{
name|System
operator|.
name|arraycopy
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
argument_list|,
name|start
argument_list|,
operator|(
operator|(
name|LongColumnVector
operator|)
name|resultCV
operator|)
operator|.
name|vector
argument_list|,
literal|0
argument_list|,
name|length
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
name|RuntimeException
argument_list|(
literal|"colinmjj:index:"
operator|+
name|index
operator|+
literal|", start:"
operator|+
name|start
operator|+
literal|",length:"
operator|+
name|length
operator|+
literal|",vec len:"
operator|+
operator|(
operator|(
name|LongColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
operator|.
name|length
operator|+
literal|", offset len:"
operator|+
name|lcv
operator|.
name|offsets
operator|.
name|length
operator|+
literal|", len len:"
operator|+
name|lcv
operator|.
name|lengths
operator|.
name|length
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|child
operator|instanceof
name|DoubleColumnVector
condition|)
block|{
name|resultCV
operator|=
operator|new
name|DoubleColumnVector
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
argument_list|,
name|start
argument_list|,
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|resultCV
operator|)
operator|.
name|vector
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|child
operator|instanceof
name|BytesColumnVector
condition|)
block|{
name|resultCV
operator|=
operator|new
name|BytesColumnVector
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
operator|(
operator|(
name|BytesColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
argument_list|,
name|start
argument_list|,
operator|(
operator|(
name|BytesColumnVector
operator|)
name|resultCV
operator|)
operator|.
name|vector
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|child
operator|instanceof
name|DecimalColumnVector
condition|)
block|{
name|resultCV
operator|=
operator|new
name|DecimalColumnVector
argument_list|(
name|length
argument_list|,
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|child
operator|)
operator|.
name|precision
argument_list|,
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|child
operator|)
operator|.
name|scale
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|lcv
operator|.
name|child
operator|)
operator|.
name|vector
argument_list|,
name|start
argument_list|,
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|resultCV
operator|)
operator|.
name|vector
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|resultCV
return|;
block|}
specifier|private
name|boolean
name|compareColumnVector
parameter_list|(
name|ColumnVector
name|cv1
parameter_list|,
name|ColumnVector
name|cv2
parameter_list|)
block|{
if|if
condition|(
name|cv1
operator|==
literal|null
operator|&&
name|cv2
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
if|if
condition|(
name|cv1
operator|!=
literal|null
operator|&&
name|cv2
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cv1
operator|instanceof
name|LongColumnVector
operator|&&
name|cv2
operator|instanceof
name|LongColumnVector
condition|)
block|{
return|return
name|compareLongColumnVector
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|cv1
argument_list|,
operator|(
name|LongColumnVector
operator|)
name|cv2
argument_list|)
return|;
block|}
if|if
condition|(
name|cv1
operator|instanceof
name|DoubleColumnVector
operator|&&
name|cv2
operator|instanceof
name|DoubleColumnVector
condition|)
block|{
return|return
name|compareDoubleColumnVector
argument_list|(
operator|(
name|DoubleColumnVector
operator|)
name|cv1
argument_list|,
operator|(
name|DoubleColumnVector
operator|)
name|cv2
argument_list|)
return|;
block|}
if|if
condition|(
name|cv1
operator|instanceof
name|BytesColumnVector
operator|&&
name|cv2
operator|instanceof
name|BytesColumnVector
condition|)
block|{
return|return
name|compareBytesColumnVector
argument_list|(
operator|(
name|BytesColumnVector
operator|)
name|cv1
argument_list|,
operator|(
name|BytesColumnVector
operator|)
name|cv2
argument_list|)
return|;
block|}
if|if
condition|(
name|cv1
operator|instanceof
name|DecimalColumnVector
operator|&&
name|cv2
operator|instanceof
name|DecimalColumnVector
condition|)
block|{
return|return
name|compareDecimalColumnVector
argument_list|(
operator|(
name|DecimalColumnVector
operator|)
name|cv1
argument_list|,
operator|(
name|DecimalColumnVector
operator|)
name|cv2
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported ColumnVector comparision between "
operator|+
name|cv1
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" and "
operator|+
name|cv2
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
specifier|private
name|boolean
name|compareLongColumnVector
parameter_list|(
name|LongColumnVector
name|cv1
parameter_list|,
name|LongColumnVector
name|cv2
parameter_list|)
block|{
name|int
name|length1
init|=
name|cv1
operator|.
name|vector
operator|.
name|length
decl_stmt|;
name|int
name|length2
init|=
name|cv2
operator|.
name|vector
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|length1
operator|==
name|length2
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|!=
name|cv2
operator|.
name|vector
index|[
name|i
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|compareDoubleColumnVector
parameter_list|(
name|DoubleColumnVector
name|cv1
parameter_list|,
name|DoubleColumnVector
name|cv2
parameter_list|)
block|{
name|int
name|length1
init|=
name|cv1
operator|.
name|vector
operator|.
name|length
decl_stmt|;
name|int
name|length2
init|=
name|cv2
operator|.
name|vector
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|length1
operator|==
name|length2
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|!=
name|cv2
operator|.
name|vector
index|[
name|i
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|compareDecimalColumnVector
parameter_list|(
name|DecimalColumnVector
name|cv1
parameter_list|,
name|DecimalColumnVector
name|cv2
parameter_list|)
block|{
name|int
name|length1
init|=
name|cv1
operator|.
name|vector
operator|.
name|length
decl_stmt|;
name|int
name|length2
init|=
name|cv2
operator|.
name|vector
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|length1
operator|==
name|length2
operator|&&
name|cv1
operator|.
name|scale
operator|==
name|cv2
operator|.
name|scale
operator|&&
name|cv1
operator|.
name|precision
operator|==
name|cv2
operator|.
name|precision
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|!=
literal|null
operator|&&
name|cv2
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|null
operator|||
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|null
operator|&&
name|cv2
operator|.
name|vector
index|[
name|i
index|]
operator|!=
literal|null
operator|||
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|!=
literal|null
operator|&&
name|cv2
operator|.
name|vector
index|[
name|i
index|]
operator|!=
literal|null
operator|&&
operator|!
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|cv2
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|compareBytesColumnVector
parameter_list|(
name|BytesColumnVector
name|cv1
parameter_list|,
name|BytesColumnVector
name|cv2
parameter_list|)
block|{
name|int
name|length1
init|=
name|cv1
operator|.
name|vector
operator|.
name|length
decl_stmt|;
name|int
name|length2
init|=
name|cv2
operator|.
name|vector
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|length1
operator|==
name|length2
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length1
condition|;
name|i
operator|++
control|)
block|{
name|int
name|innerLen1
init|=
name|cv1
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|length
decl_stmt|;
name|int
name|innerLen2
init|=
name|cv2
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|innerLen1
operator|==
name|innerLen2
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|innerLen1
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|cv1
operator|.
name|vector
index|[
name|i
index|]
index|[
name|j
index|]
operator|!=
name|cv2
operator|.
name|vector
index|[
name|i
index|]
index|[
name|j
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

