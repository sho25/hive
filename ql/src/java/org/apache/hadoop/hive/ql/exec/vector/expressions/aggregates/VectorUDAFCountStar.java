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

begin_comment
comment|/**  * VectorUDAFCountStar. Vectorized implementation for COUNT(*) aggregates.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"count"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr) - Returns count(*) (vectorized)"
argument_list|)
specifier|public
class|class
name|VectorUDAFCountStar
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
comment|/**      * class for storing the current aggregate value.      */
specifier|static
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
name|count
decl_stmt|;
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
name|count
operator|=
literal|0L
expr_stmt|;
block|}
block|}
comment|// This constructor is used to momentarily create the object so match can be called.
specifier|public
name|VectorUDAFCountStar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDAFCountStar
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
block|{     }
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
comment|// count(*) cares not about NULLs nor selection
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
name|myAgg
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
operator|++
name|myAgg
operator|.
name|count
expr_stmt|;
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
name|myagg
operator|.
name|count
operator|+=
name|batchSize
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
operator|+
name|model
operator|.
name|primitive2
argument_list|()
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
comment|/*      * Count null input which is for COUNT(*) and output is LONG.      *      * Just modes (PARTIAL1, COMPLETE).      */
return|return
name|name
operator|.
name|equals
argument_list|(
literal|"count"
argument_list|)
operator|&&
name|inputColVectorType
operator|==
literal|null
operator|&&
name|outputColVectorType
operator|==
name|ColumnVector
operator|.
name|Type
operator|.
name|LONG
operator|&&
operator|(
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
name|LongColumnVector
name|outputColVector
init|=
operator|(
name|LongColumnVector
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
name|count
expr_stmt|;
block|}
block|}
end_class

end_unit

