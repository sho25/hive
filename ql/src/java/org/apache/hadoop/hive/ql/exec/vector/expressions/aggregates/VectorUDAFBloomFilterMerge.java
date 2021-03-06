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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|aggregates
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
name|VectorAggregationBufferRow
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
name|VectorAggregationDesc
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
name|VectorizedRowBatch
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
name|VectorExpression
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
name|VectorAggregateExpression
operator|.
name|AggregationBuffer
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
name|plan
operator|.
name|AggregationDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFBloomFilter
operator|.
name|GenericUDAFBloomFilterEvaluator
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
operator|.
name|Mode
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|BloomKFilter
import|;
end_import

begin_class
specifier|public
class|class
name|VectorUDAFBloomFilterMerge
extends|extends
name|VectorAggregateExpression
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|long
name|expectedEntries
init|=
operator|-
literal|1
decl_stmt|;
specifier|transient
specifier|private
name|int
name|aggBufferSize
decl_stmt|;
comment|/**    * class for storing the current aggregate value.    */
specifier|private
specifier|static
specifier|final
class|class
name|Aggregation
implements|implements
name|AggregationBuffer
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|byte
index|[]
name|bfBytes
decl_stmt|;
specifier|public
name|Aggregation
parameter_list|(
name|long
name|expectedEntries
parameter_list|)
block|{
name|ByteArrayOutputStream
name|bytesOut
init|=
literal|null
decl_stmt|;
try|try
block|{
name|BloomKFilter
name|bf
init|=
operator|new
name|BloomKFilter
argument_list|(
name|expectedEntries
argument_list|)
decl_stmt|;
name|bytesOut
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|BloomKFilter
operator|.
name|serialize
argument_list|(
name|bytesOut
argument_list|,
name|bf
argument_list|)
expr_stmt|;
name|bfBytes
operator|=
name|bytesOut
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Error creating aggregation buffer"
argument_list|,
name|err
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|bytesOut
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getVariableSize
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// Do not change the initial bytes which contain NumHashFunctions/NumBits!
name|Arrays
operator|.
name|fill
argument_list|(
name|bfBytes
argument_list|,
name|BloomKFilter
operator|.
name|START_OF_SERIALIZED_LONGS
argument_list|,
name|bfBytes
operator|.
name|length
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|// This constructor is used to momentarily create the object so match can be called.
specifier|public
name|VectorUDAFBloomFilterMerge
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDAFBloomFilterMerge
parameter_list|(
name|VectorAggregationDesc
name|vecAggrDesc
parameter_list|)
block|{
name|super
argument_list|(
name|vecAggrDesc
argument_list|)
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
name|GenericUDAFBloomFilterEvaluator
name|udafBloomFilter
init|=
operator|(
name|GenericUDAFBloomFilterEvaluator
operator|)
name|vecAggrDesc
operator|.
name|getEvaluator
argument_list|()
decl_stmt|;
name|expectedEntries
operator|=
name|udafBloomFilter
operator|.
name|getExpectedEntries
argument_list|()
expr_stmt|;
name|aggBufferSize
operator|=
operator|-
literal|1
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
literal|"expectedEntries not initialized"
argument_list|)
throw|;
block|}
return|return
operator|new
name|Aggregation
argument_list|(
name|expectedEntries
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|aggregateInput
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|inputExpression
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|ColumnVector
name|inputColumn
init|=
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|inputExpression
operator|.
name|getOutputColumnNum
argument_list|()
index|]
decl_stmt|;
name|int
name|batchSize
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|batchSize
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|Aggregation
name|myagg
init|=
operator|(
name|Aggregation
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|inputColumn
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputColumn
operator|.
name|noNulls
operator|||
operator|!
name|inputColumn
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
operator|!
name|batch
operator|.
name|selectedInUse
operator|&&
name|inputColumn
operator|.
name|noNulls
condition|)
block|{
name|iterateNoSelectionNoNulls
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|iterateNoSelectionHasNulls
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inputColumn
operator|.
name|noNulls
condition|)
block|{
name|iterateSelectionNoNulls
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|,
name|batch
operator|.
name|selected
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|iterateSelectionHasNulls
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|,
name|batch
operator|.
name|selected
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateNoSelectionNoNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateNoSelectionHasNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|inputColumn
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|iterateSelectionNoNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
index|[]
name|selected
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|j
control|)
block|{
name|int
name|i
init|=
name|selected
index|[
name|j
index|]
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateSelectionHasNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
index|[]
name|selected
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|j
control|)
block|{
name|int
name|i
init|=
name|selected
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|inputColumn
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|aggregateInputSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|batchSize
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|batchSize
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|inputExpression
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|ColumnVector
name|inputColumn
init|=
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|inputExpression
operator|.
name|getOutputColumnNum
argument_list|()
index|]
decl_stmt|;
if|if
condition|(
name|inputColumn
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColumn
operator|.
name|isRepeating
condition|)
block|{
name|iterateNoNullsRepeatingWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|iterateNoNullsSelectionWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|iterateNoNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColumn
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|!
name|inputColumn
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|iterateNoNullsRepeatingWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|iterateHasNullsSelectionWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|,
name|batch
operator|.
name|selected
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|iterateHasNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|inputColumn
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|iterateNoNullsRepeatingWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregrateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateNoNullsSelectionWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
index|[]
name|selection
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|int
name|row
init|=
name|selection
index|[
name|i
index|]
decl_stmt|;
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregrateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateNoNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregrateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateHasNullsSelectionWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
index|[]
name|selection
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|int
name|row
init|=
name|selection
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|inputColumn
operator|.
name|isNull
index|[
name|row
index|]
condition|)
block|{
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregrateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|iterateHasNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|ColumnVector
name|inputColumn
parameter_list|,
name|int
name|batchSize
parameter_list|)
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
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|inputColumn
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregrateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|processValue
argument_list|(
name|myagg
argument_list|,
name|inputColumn
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Aggregation
name|getCurrentAggregationBuffer
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregrateIndex
parameter_list|,
name|int
name|row
parameter_list|)
block|{
name|VectorAggregationBufferRow
name|mySet
init|=
name|aggregationBufferSets
index|[
name|row
index|]
decl_stmt|;
name|Aggregation
name|myagg
init|=
operator|(
name|Aggregation
operator|)
name|mySet
operator|.
name|getAggregationBuffer
argument_list|(
name|aggregrateIndex
argument_list|)
decl_stmt|;
return|return
name|myagg
return|;
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
name|agg
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAggregationBufferFixedSize
parameter_list|()
block|{
if|if
condition|(
name|aggBufferSize
operator|<
literal|0
condition|)
block|{
comment|// Not pretty, but we need a way to get the size
try|try
block|{
name|Aggregation
name|agg
init|=
operator|(
name|Aggregation
operator|)
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
name|aggBufferSize
operator|=
name|agg
operator|.
name|bfBytes
operator|.
name|length
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
literal|"Unexpected error while creating AggregationBuffer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|aggBufferSize
return|;
block|}
name|void
name|processValue
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
comment|// columnVector entry is byte array representing serialized BloomFilter.
comment|// BloomFilter.mergeBloomFilterBytes() does a simple byte ORing
comment|// which should be faster than deserialize/merge.
name|BytesColumnVector
name|inputColumn
init|=
operator|(
name|BytesColumnVector
operator|)
name|columnVector
decl_stmt|;
name|BloomKFilter
operator|.
name|mergeBloomFilterBytes
argument_list|(
name|myagg
operator|.
name|bfBytes
argument_list|,
literal|0
argument_list|,
name|myagg
operator|.
name|bfBytes
operator|.
name|length
argument_list|,
name|inputColumn
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputColumn
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputColumn
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|String
name|name
parameter_list|,
name|ColumnVector
operator|.
name|Type
name|inputColVectorType
parameter_list|,
name|ColumnVector
operator|.
name|Type
name|outputColVectorType
parameter_list|,
name|Mode
name|mode
parameter_list|)
block|{
comment|/*      * Bloom filter merge input and output are BYTES.      *      * Just modes (PARTIAL2, FINAL).      */
return|return
name|name
operator|.
name|equals
argument_list|(
literal|"bloom_filter"
argument_list|)
operator|&&
name|inputColVectorType
operator|==
name|ColumnVector
operator|.
name|Type
operator|.
name|BYTES
operator|&&
name|outputColVectorType
operator|==
name|ColumnVector
operator|.
name|Type
operator|.
name|BYTES
operator|&&
operator|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL2
operator|||
name|mode
operator|==
name|Mode
operator|.
name|FINAL
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|assignRowColumn
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|,
name|int
name|columnNum
parameter_list|,
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|BytesColumnVector
name|outputColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|columnNum
index|]
decl_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|Aggregation
name|bfAgg
init|=
operator|(
name|Aggregation
operator|)
name|agg
decl_stmt|;
name|outputColVector
operator|.
name|setVal
argument_list|(
name|batchIndex
argument_list|,
name|bfAgg
operator|.
name|bfBytes
argument_list|,
literal|0
argument_list|,
name|bfAgg
operator|.
name|bfBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

