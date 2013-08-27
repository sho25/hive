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
operator|.
name|expressions
operator|.
name|aggregates
operator|.
name|gen
package|;
end_package

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
name|util
operator|.
name|JavaDataModel
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
name|ObjectInspectorFactory
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

begin_comment
comment|/** * VectorUDAFStdSampDouble. Vectorized implementation for VARIANCE aggregates.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"stddev_samp"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns the sample standard deviation of a set of numbers (vectorized, double)"
argument_list|)
specifier|public
class|class
name|VectorUDAFStdSampDouble
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
comment|/**      /* class for storing the current aggregate value.      */
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
specifier|transient
specifier|private
name|double
name|sum
decl_stmt|;
specifier|transient
specifier|private
name|long
name|count
decl_stmt|;
specifier|transient
specifier|private
name|double
name|variance
decl_stmt|;
specifier|transient
specifier|private
name|boolean
name|isNull
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|()
block|{
name|isNull
operator|=
literal|false
expr_stmt|;
name|sum
operator|=
literal|0
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
name|variance
operator|=
literal|0
expr_stmt|;
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
block|}
specifier|private
name|VectorExpression
name|inputExpression
decl_stmt|;
specifier|transient
specifier|private
name|LongWritable
name|resultCount
decl_stmt|;
specifier|transient
specifier|private
name|DoubleWritable
name|resultSum
decl_stmt|;
specifier|transient
specifier|private
name|DoubleWritable
name|resultVariance
decl_stmt|;
specifier|transient
specifier|private
name|Object
index|[]
name|partialResult
decl_stmt|;
specifier|transient
specifier|private
name|ObjectInspector
name|soi
decl_stmt|;
specifier|public
name|VectorUDAFStdSampDouble
parameter_list|(
name|VectorExpression
name|inputExpression
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputExpression
operator|=
name|inputExpression
expr_stmt|;
block|}
specifier|public
name|VectorUDAFStdSampDouble
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|partialResult
operator|=
operator|new
name|Object
index|[
literal|3
index|]
expr_stmt|;
name|resultCount
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
name|resultSum
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
name|resultVariance
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
name|partialResult
index|[
literal|0
index|]
operator|=
name|resultCount
expr_stmt|;
name|partialResult
index|[
literal|1
index|]
operator|=
name|resultSum
expr_stmt|;
name|partialResult
index|[
literal|2
index|]
operator|=
name|resultVariance
expr_stmt|;
name|initPartialResultInspector
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initPartialResultInspector
parameter_list|()
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|foi
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fname
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"sum"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"variance"
argument_list|)
expr_stmt|;
name|soi
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fname
argument_list|,
name|foi
argument_list|)
expr_stmt|;
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
name|aggregateIndex
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
name|aggregateIndex
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
name|inputExpression
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|DoubleColumnVector
name|inputVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|inputExpression
operator|.
name|getOutputColumn
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
name|double
index|[]
name|vector
init|=
name|inputVector
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|inputVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputVector
operator|.
name|noNulls
operator|||
operator|!
name|inputVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|iterateRepeatingNoNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|batch
operator|.
name|selectedInUse
operator|&&
name|inputVector
operator|.
name|noNulls
condition|)
block|{
name|iterateNoSelectionNoNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|vector
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
name|iterateNoSelectionHasNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|vector
argument_list|,
name|batchSize
argument_list|,
name|inputVector
operator|.
name|isNull
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inputVector
operator|.
name|noNulls
condition|)
block|{
name|iterateSelectionNoNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|vector
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
name|iterateSelectionHasNullsWithAggregationSelection
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|vector
argument_list|,
name|batchSize
argument_list|,
name|inputVector
operator|.
name|isNull
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
name|iterateRepeatingNoNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|double
name|value
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
name|aggregateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|iterateSelectionHasNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
index|[]
name|isNull
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
name|Aggregation
name|myagg
init|=
name|getCurrentAggregationBuffer
argument_list|(
name|aggregationBufferSets
argument_list|,
name|aggregateIndex
argument_list|,
name|j
argument_list|)
decl_stmt|;
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
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|double
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|iterateSelectionNoNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|double
index|[]
name|vector
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
name|aggregateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|double
name|value
init|=
name|vector
index|[
name|selected
index|[
name|i
index|]
index|]
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|iterateNoSelectionHasNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
index|[]
name|isNull
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
name|aggregateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|double
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|iterateNoSelectionNoNullsWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|double
index|[]
name|vector
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
name|aggregateIndex
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|double
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
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
name|DoubleColumnVector
name|inputVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|inputExpression
operator|.
name|getOutputColumn
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
name|double
index|[]
name|vector
init|=
name|inputVector
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|inputVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputVector
operator|.
name|noNulls
condition|)
block|{
name|iterateRepeatingNoNulls
argument_list|(
name|myagg
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|batch
operator|.
name|selectedInUse
operator|&&
name|inputVector
operator|.
name|noNulls
condition|)
block|{
name|iterateNoSelectionNoNulls
argument_list|(
name|myagg
argument_list|,
name|vector
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
name|vector
argument_list|,
name|batchSize
argument_list|,
name|inputVector
operator|.
name|isNull
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inputVector
operator|.
name|noNulls
condition|)
block|{
name|iterateSelectionNoNulls
argument_list|(
name|myagg
argument_list|,
name|vector
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
name|vector
argument_list|,
name|batchSize
argument_list|,
name|inputVector
operator|.
name|isNull
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
name|iterateRepeatingNoNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|double
name|value
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
comment|// TODO: conjure a formula w/o iterating
comment|//
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
comment|// We pulled out i=0 so we can remove the count> 1 check in the loop
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
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
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
index|[]
name|isNull
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
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|double
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
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
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
index|[]
name|selected
parameter_list|)
block|{
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|double
name|value
init|=
name|vector
index|[
name|selected
index|[
literal|0
index|]
index|]
decl_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
comment|// i=0 was pulled out to remove the count> 1 check in the loop
comment|//
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|value
operator|=
name|vector
index|[
name|selected
index|[
name|i
index|]
index|]
expr_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
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
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
index|[]
name|isNull
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
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|double
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|iterateNoSelectionNoNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|double
index|[]
name|vector
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
name|double
name|value
init|=
name|vector
index|[
literal|0
index|]
decl_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
comment|// i=0 was pulled out to remove count> 1 check
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|batchSize
condition|;
operator|++
name|i
control|)
block|{
name|value
operator|=
name|vector
index|[
name|i
index|]
expr_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
literal|1
expr_stmt|;
name|double
name|t
init|=
name|myagg
operator|.
name|count
operator|*
name|value
operator|-
name|myagg
operator|.
name|sum
decl_stmt|;
name|myagg
operator|.
name|variance
operator|+=
operator|(
name|t
operator|*
name|t
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|myagg
operator|.
name|count
operator|*
operator|(
name|myagg
operator|.
name|count
operator|-
literal|1
operator|)
operator|)
expr_stmt|;
block|}
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
return|return
operator|new
name|Aggregation
argument_list|()
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
name|Aggregation
name|myAgg
init|=
operator|(
name|Aggregation
operator|)
name|agg
decl_stmt|;
name|myAgg
operator|.
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluateOutput
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
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
name|myagg
operator|.
name|isNull
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
assert|assert
operator|(
literal|0
operator|<
name|myagg
operator|.
name|count
operator|)
assert|;
name|resultCount
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|count
argument_list|)
expr_stmt|;
name|resultSum
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|sum
argument_list|)
expr_stmt|;
name|resultVariance
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|variance
argument_list|)
expr_stmt|;
return|return
name|partialResult
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getOutputObjectInspector
parameter_list|()
block|{
return|return
name|soi
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getAggregationBufferFixedSize
parameter_list|()
block|{
name|JavaDataModel
name|model
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|JavaDataModel
operator|.
name|alignUp
argument_list|(
name|model
operator|.
name|object
argument_list|()
operator|+
name|model
operator|.
name|primitive2
argument_list|()
operator|*
literal|3
operator|+
name|model
operator|.
name|primitive1
argument_list|()
argument_list|,
name|model
operator|.
name|memoryAlign
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|AggregationDesc
name|desc
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// No-op
block|}
specifier|public
name|VectorExpression
name|getInputExpression
parameter_list|()
block|{
return|return
name|inputExpression
return|;
block|}
specifier|public
name|void
name|setInputExpression
parameter_list|(
name|VectorExpression
name|inputExpression
parameter_list|)
block|{
name|this
operator|.
name|inputExpression
operator|=
name|inputExpression
expr_stmt|;
block|}
block|}
end_class

end_unit

