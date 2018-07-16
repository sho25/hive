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
name|Decimal64ColumnVector
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
name|typeinfo
operator|.
name|DecimalTypeInfo
import|;
end_import

begin_comment
comment|/** * VectorUDAFSumLong. Vectorized implementation for SUM aggregates. */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"sum"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr) - Returns the sum value of expr (vectorized, type: decimal64 -> decimal64)"
argument_list|)
specifier|public
class|class
name|VectorUDAFSumDecimal64
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
specifier|transient
specifier|private
name|long
name|sum
decl_stmt|;
comment|/**     * Value is explicitly (re)initialized in reset()     */
specifier|transient
specifier|private
name|boolean
name|isNull
init|=
literal|true
decl_stmt|;
specifier|transient
specifier|private
name|long
name|outputDecimal64AbsMax
decl_stmt|;
specifier|transient
specifier|private
name|boolean
name|isOverflowed
init|=
literal|false
decl_stmt|;
specifier|public
name|Aggregation
parameter_list|(
name|long
name|outputDecimal64AbsMax
parameter_list|)
block|{
name|this
operator|.
name|outputDecimal64AbsMax
operator|=
name|outputDecimal64AbsMax
expr_stmt|;
block|}
specifier|public
name|void
name|sumValue
parameter_list|(
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
name|isOverflowed
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|isNull
condition|)
block|{
name|sum
operator|=
name|value
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sum
operator|+=
name|value
expr_stmt|;
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
name|sum
argument_list|)
operator|>
name|outputDecimal64AbsMax
condition|)
block|{
name|isOverflowed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
comment|// The isNull check and work has already been performed.
specifier|public
name|void
name|sumValueNoNullCheck
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|sum
operator|+=
name|value
expr_stmt|;
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
name|sum
argument_list|)
operator|>
name|outputDecimal64AbsMax
condition|)
block|{
name|isOverflowed
operator|=
literal|true
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
name|isNull
operator|=
literal|true
expr_stmt|;
name|isOverflowed
operator|=
literal|false
expr_stmt|;
name|sum
operator|=
literal|0
expr_stmt|;
empty_stmt|;
block|}
block|}
specifier|private
name|DecimalTypeInfo
name|outputDecimalTypeInfo
decl_stmt|;
specifier|private
name|long
name|outputDecimal64AbsMax
decl_stmt|;
comment|// This constructor is used to momentarily create the object so match can be called.
specifier|public
name|VectorUDAFSumDecimal64
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDAFSumDecimal64
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
name|outputDecimalTypeInfo
operator|=
operator|(
name|DecimalTypeInfo
operator|)
name|outputTypeInfo
expr_stmt|;
name|outputDecimal64AbsMax
operator|=
name|HiveDecimalWritable
operator|.
name|getDecimal64AbsMax
argument_list|(
name|outputDecimalTypeInfo
operator|.
name|getPrecision
argument_list|()
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
name|Decimal64ColumnVector
name|inputVector
init|=
operator|(
name|Decimal64ColumnVector
operator|)
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
name|long
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
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputVector
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
name|vector
index|[
literal|0
index|]
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
name|vector
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
name|vector
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
name|inputVector
operator|.
name|isRepeating
condition|)
block|{
name|iterateHasNullsRepeatingWithAggregationSelection
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
argument_list|,
name|inputVector
operator|.
name|isNull
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
name|iterateHasNullsSelectionWithAggregationSelection
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
argument_list|,
name|inputVector
operator|.
name|isNull
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
name|aggregateIndex
parameter_list|,
name|long
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|value
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
name|aggregateIndex
parameter_list|,
name|long
index|[]
name|values
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|values
index|[
name|selection
index|[
name|i
index|]
index|]
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
name|aggregateIndex
parameter_list|,
name|long
index|[]
name|values
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|iterateHasNullsRepeatingWithAggregationSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|long
name|value
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
index|[]
name|isNull
parameter_list|)
block|{
if|if
condition|(
name|isNull
index|[
literal|0
index|]
condition|)
block|{
return|return;
block|}
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|value
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
name|aggregateIndex
parameter_list|,
name|long
index|[]
name|values
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
index|[]
name|selection
parameter_list|,
name|boolean
index|[]
name|isNull
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
name|selection
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|values
index|[
name|i
index|]
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
name|aggregateIndex
parameter_list|,
name|long
index|[]
name|values
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
name|myagg
operator|.
name|sumValue
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
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
name|Decimal64ColumnVector
name|inputVector
init|=
operator|(
name|Decimal64ColumnVector
operator|)
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
name|long
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
if|if
condition|(
name|myagg
operator|.
name|isNull
condition|)
block|{
name|myagg
operator|.
name|isNull
operator|=
literal|false
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
literal|0
expr_stmt|;
block|}
name|myagg
operator|.
name|sumValueNoNullCheck
argument_list|(
name|vector
index|[
literal|0
index|]
operator|*
name|batchSize
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
name|iterateSelectionHasNulls
parameter_list|(
name|Aggregation
name|myagg
parameter_list|,
name|long
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
name|long
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
name|isNull
operator|=
literal|false
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
literal|0
expr_stmt|;
block|}
name|myagg
operator|.
name|sumValueNoNullCheck
argument_list|(
name|value
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
name|long
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
name|sum
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
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
name|long
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
name|myagg
operator|.
name|sumValueNoNullCheck
argument_list|(
name|value
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
name|long
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
name|long
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
name|sum
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
name|myagg
operator|.
name|sumValueNoNullCheck
argument_list|(
name|value
argument_list|)
expr_stmt|;
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
name|long
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
name|sum
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
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
name|long
name|value
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
name|myagg
operator|.
name|sumValueNoNullCheck
argument_list|(
name|value
argument_list|)
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
argument_list|(
name|outputDecimal64AbsMax
argument_list|)
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
comment|/*      * Sum input and output are DECIMAL_64.      *      * Any mode (PARTIAL1, PARTIAL2, FINAL, COMPLETE).      */
return|return
name|name
operator|.
name|equals
argument_list|(
literal|"sum"
argument_list|)
operator|&&
name|inputColVectorType
operator|==
name|ColumnVector
operator|.
name|Type
operator|.
name|DECIMAL_64
operator|&&
name|outputColVectorType
operator|==
name|ColumnVector
operator|.
name|Type
operator|.
name|DECIMAL_64
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
name|Decimal64ColumnVector
name|outputColVector
init|=
operator|(
name|Decimal64ColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|columnNum
index|]
decl_stmt|;
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
operator|||
name|myagg
operator|.
name|isOverflowed
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
name|myagg
operator|.
name|sum
expr_stmt|;
block|}
block|}
end_class

end_unit

