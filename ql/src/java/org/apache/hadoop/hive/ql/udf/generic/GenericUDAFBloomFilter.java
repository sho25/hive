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
name|ql
operator|.
name|udf
operator|.
name|generic
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
name|common
operator|.
name|io
operator|.
name|NonSyncByteArrayInputStream
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|common
operator|.
name|type
operator|.
name|Timestamp
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
name|Description
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
name|Operator
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
name|SelectOperator
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
name|UDFArgumentTypeException
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
name|VectorizedUDAFs
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
name|expressions
operator|.
name|aggregates
operator|.
name|*
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
name|ql
operator|.
name|parse
operator|.
name|SemanticException
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
name|plan
operator|.
name|ColStatistics
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
name|plan
operator|.
name|ExprNodeColumnDesc
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
name|plan
operator|.
name|ExprNodeDescUtils
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
name|plan
operator|.
name|Statistics
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
name|DateWritableV2
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
name|HiveDecimalWritable
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
name|primitive
operator|.
name|*
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
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|BloomKFilter
import|;
end_import

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
name|ByteArrayOutputStream
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
name|List
import|;
end_import

begin_comment
comment|/**  * Generic UDF to generate Bloom Filter  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"bloom_filter"
argument_list|)
specifier|public
class|class
name|GenericUDAFBloomFilter
implements|implements
name|GenericUDAFResolver2
block|{
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|GenericUDAFParameterInfo
name|info
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
operator|new
name|GenericUDAFBloomFilterEvaluator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
operator|new
name|GenericUDAFBloomFilterEvaluator
argument_list|()
return|;
block|}
comment|/**    * GenericUDAFBloomFilterEvaluator - Evaluator class for BloomFilter    */
annotation|@
name|VectorizedUDAFs
argument_list|(
block|{
name|VectorUDAFBloomFilter
operator|.
name|class
block|,
name|VectorUDAFBloomFilterMerge
operator|.
name|class
block|}
argument_list|)
specifier|public
specifier|static
class|class
name|GenericUDAFBloomFilterEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// Source operator to get the number of entries
specifier|private
name|SelectOperator
name|sourceOperator
decl_stmt|;
specifier|private
name|long
name|hintEntries
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|maxEntries
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|minEntries
init|=
literal|0
decl_stmt|;
specifier|private
name|float
name|factor
init|=
literal|1
decl_stmt|;
comment|// ObjectInspector for input data.
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
comment|// Bloom filter rest
specifier|private
specifier|final
name|ByteArrayOutputStream
name|result
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|byte
index|[]
name|scratchBuffer
init|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|init
parameter_list|(
name|Mode
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
comment|// Initialize input
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
comment|// Do nothing for other modes
block|}
comment|// Output will be same in both partial or full aggregation modes.
comment|// It will be a BloomFilter in ByteWritable
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
name|long
name|entries
init|=
name|Math
operator|.
name|min
argument_list|(
name|getExpectedEntries
argument_list|()
argument_list|,
name|maxEntries
argument_list|)
decl_stmt|;
name|long
name|numBits
init|=
call|(
name|long
call|)
argument_list|(
operator|-
name|entries
operator|*
name|Math
operator|.
name|log
argument_list|(
name|BloomKFilter
operator|.
name|DEFAULT_FPP
argument_list|)
operator|/
operator|(
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|*
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|)
argument_list|)
decl_stmt|;
name|int
name|nLongs
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|double
operator|)
name|numBits
operator|/
operator|(
name|double
operator|)
name|Long
operator|.
name|SIZE
argument_list|)
decl_stmt|;
comment|// additional bits to pad long array to block size
name|int
name|padLongs
init|=
literal|8
operator|-
name|nLongs
operator|%
literal|8
decl_stmt|;
return|return
operator|(
name|nLongs
operator|+
name|padLongs
operator|)
operator|*
name|Long
operator|.
name|SIZE
operator|/
literal|8
return|;
block|}
comment|/**      * Class for storing the BloomFilter      */
annotation|@
name|AggregationType
argument_list|(
name|estimable
operator|=
literal|true
argument_list|)
specifier|static
class|class
name|BloomFilterBuf
extends|extends
name|AbstractAggregationBuffer
block|{
name|BloomKFilter
name|bloomFilter
decl_stmt|;
specifier|public
name|BloomFilterBuf
parameter_list|(
name|long
name|expectedEntries
parameter_list|,
name|long
name|maxEntries
parameter_list|)
block|{
if|if
condition|(
name|expectedEntries
operator|>
name|maxEntries
condition|)
block|{
name|bloomFilter
operator|=
operator|new
name|BloomKFilter
argument_list|(
name|maxEntries
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bloomFilter
operator|=
operator|new
name|BloomKFilter
argument_list|(
name|expectedEntries
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|bloomFilter
operator|.
name|sizeInBytes
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
operator|(
operator|(
name|BloomFilterBuf
operator|)
name|agg
operator|)
operator|.
name|bloomFilter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|long
name|expectedEntries
init|=
name|getExpectedEntries
argument_list|()
decl_stmt|;
if|if
condition|(
name|expectedEntries
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"BloomFilter expectedEntries not initialized"
argument_list|)
throw|;
block|}
name|BloomFilterBuf
name|buf
init|=
operator|new
name|BloomFilterBuf
argument_list|(
name|expectedEntries
argument_list|,
name|maxEntries
argument_list|)
decl_stmt|;
name|reset
argument_list|(
name|buf
argument_list|)
expr_stmt|;
return|return
name|buf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|parameters
operator|==
literal|null
operator|||
name|parameters
index|[
literal|0
index|]
operator|==
literal|null
condition|)
block|{
comment|// 2nd condition occurs when the input has 0 rows (possible due to
comment|// filtering, joins etc).
return|return;
block|}
name|BloomKFilter
name|bf
init|=
operator|(
operator|(
name|BloomFilterBuf
operator|)
name|agg
operator|)
operator|.
name|bloomFilter
decl_stmt|;
comment|// Add the expression into the BloomFilter
switch|switch
condition|(
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|boolean
name|vBoolean
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vBoolean
condition|?
literal|1
else|:
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|byte
name|vByte
init|=
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vByte
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|short
name|vShort
init|=
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vShort
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|int
name|vInt
init|=
operator|(
operator|(
name|IntObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vInt
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|long
name|vLong
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vLong
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|float
name|vFloat
init|=
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addDouble
argument_list|(
name|vFloat
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|double
name|vDouble
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addDouble
argument_list|(
name|vDouble
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|HiveDecimalWritable
name|vDecimal
init|=
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
name|startIdx
init|=
name|vDecimal
operator|.
name|toBytes
argument_list|(
name|scratchBuffer
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addBytes
argument_list|(
name|scratchBuffer
argument_list|,
name|startIdx
argument_list|,
name|scratchBuffer
operator|.
name|length
operator|-
name|startIdx
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|DateWritableV2
name|vDate
init|=
operator|(
operator|(
name|DateObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vDate
operator|.
name|getDays
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|Timestamp
name|vTimeStamp
init|=
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addLong
argument_list|(
name|vTimeStamp
operator|.
name|toEpochMilli
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|Text
name|vChar
init|=
operator|(
operator|(
name|HiveCharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
operator|.
name|getStrippedValue
argument_list|()
decl_stmt|;
name|bf
operator|.
name|addBytes
argument_list|(
name|vChar
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|vChar
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|Text
name|vVarChar
init|=
operator|(
operator|(
name|HiveVarcharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
name|bf
operator|.
name|addBytes
argument_list|(
name|vVarChar
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|vVarChar
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|Text
name|vString
init|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addBytes
argument_list|(
name|vString
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|vString
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|BytesWritable
name|vBytes
init|=
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|bf
operator|.
name|addBytes
argument_list|(
name|vBytes
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|vBytes
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Bad primitive category "
operator|+
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
name|partial
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|partial
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|BytesWritable
name|bytes
init|=
operator|(
name|BytesWritable
operator|)
name|partial
decl_stmt|;
name|ByteArrayInputStream
name|in
init|=
operator|new
name|NonSyncByteArrayInputStream
argument_list|(
name|bytes
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
comment|// Deserialize the bloom filter
try|try
block|{
name|BloomKFilter
name|bf
init|=
name|BloomKFilter
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
operator|(
operator|(
name|BloomFilterBuf
operator|)
name|agg
operator|)
operator|.
name|bloomFilter
operator|.
name|merge
argument_list|(
name|bf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|result
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|BloomKFilter
operator|.
name|serialize
argument_list|(
name|result
argument_list|,
operator|(
operator|(
name|BloomFilterBuf
operator|)
name|agg
operator|)
operator|.
name|bloomFilter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|BytesWritable
argument_list|(
name|result
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|terminate
argument_list|(
name|agg
argument_list|)
return|;
block|}
specifier|public
name|long
name|getExpectedEntries
parameter_list|()
block|{
comment|// If hint is provided use that size.
if|if
condition|(
name|hintEntries
operator|>
literal|0
condition|)
return|return
name|hintEntries
return|;
name|long
name|expectedEntries
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|sourceOperator
operator|!=
literal|null
operator|&&
name|sourceOperator
operator|.
name|getStatistics
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Statistics
name|stats
init|=
name|sourceOperator
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
name|expectedEntries
operator|=
name|stats
operator|.
name|getNumRows
argument_list|()
expr_stmt|;
comment|// Use NumDistinctValues if possible
switch|switch
condition|(
name|stats
operator|.
name|getColumnStatsState
argument_list|()
condition|)
block|{
case|case
name|COMPLETE
case|:
case|case
name|PARTIAL
case|:
comment|// There should only be column in sourceOperator
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
init|=
name|stats
operator|.
name|getColumnStats
argument_list|()
decl_stmt|;
name|ExprNodeColumnDesc
name|colExpr
init|=
name|ExprNodeDescUtils
operator|.
name|getColumnExpr
argument_list|(
name|sourceOperator
operator|.
name|getConf
argument_list|()
operator|.
name|getColList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|colExpr
operator|!=
literal|null
operator|&&
name|stats
operator|.
name|getColumnStatisticsFromColName
argument_list|(
name|colExpr
operator|.
name|getColumn
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|long
name|ndv
init|=
name|stats
operator|.
name|getColumnStatisticsFromColName
argument_list|(
name|colExpr
operator|.
name|getColumn
argument_list|()
argument_list|)
operator|.
name|getCountDistint
argument_list|()
decl_stmt|;
if|if
condition|(
name|ndv
operator|>
literal|0
condition|)
block|{
name|expectedEntries
operator|=
name|ndv
expr_stmt|;
block|}
block|}
break|break;
default|default:
break|break;
block|}
block|}
comment|// Update expectedEntries based on factor and minEntries configurations
name|expectedEntries
operator|=
call|(
name|long
call|)
argument_list|(
name|expectedEntries
operator|*
name|factor
argument_list|)
expr_stmt|;
name|expectedEntries
operator|=
name|expectedEntries
operator|>
name|minEntries
condition|?
name|expectedEntries
else|:
name|minEntries
expr_stmt|;
return|return
name|expectedEntries
return|;
block|}
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getSourceOperator
parameter_list|()
block|{
return|return
name|sourceOperator
return|;
block|}
specifier|public
name|void
name|setSourceOperator
parameter_list|(
name|SelectOperator
name|sourceOperator
parameter_list|)
block|{
name|this
operator|.
name|sourceOperator
operator|=
name|sourceOperator
expr_stmt|;
block|}
specifier|public
name|void
name|setHintEntries
parameter_list|(
name|long
name|hintEntries
parameter_list|)
block|{
name|this
operator|.
name|hintEntries
operator|=
name|hintEntries
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasHintEntries
parameter_list|()
block|{
return|return
name|hintEntries
operator|!=
operator|-
literal|1
return|;
block|}
specifier|public
name|void
name|setMaxEntries
parameter_list|(
name|long
name|maxEntries
parameter_list|)
block|{
name|this
operator|.
name|maxEntries
operator|=
name|maxEntries
expr_stmt|;
block|}
specifier|public
name|void
name|setMinEntries
parameter_list|(
name|long
name|minEntries
parameter_list|)
block|{
name|this
operator|.
name|minEntries
operator|=
name|minEntries
expr_stmt|;
block|}
specifier|public
name|long
name|getMinEntries
parameter_list|()
block|{
return|return
name|minEntries
return|;
block|}
specifier|public
name|void
name|setFactor
parameter_list|(
name|float
name|factor
parameter_list|)
block|{
name|this
operator|.
name|factor
operator|=
name|factor
expr_stmt|;
block|}
specifier|public
name|float
name|getFactor
parameter_list|()
block|{
return|return
name|factor
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
return|return
literal|"expectedEntries="
operator|+
name|getExpectedEntries
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

