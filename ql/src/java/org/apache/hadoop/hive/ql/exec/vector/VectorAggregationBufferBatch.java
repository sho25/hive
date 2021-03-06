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
package|;
end_package

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
name|util
operator|.
name|JavaDataModel
import|;
end_import

begin_comment
comment|/**  * This maps a batch to the aggregation buffers sets to use for each row (key)  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorAggregationBufferBatch
block|{
comment|/**    * Batch sized array of aggregation buffer sets.    * The array is preallocated and is reused for each batch, but the individual entries    * will reference different aggregation buffer set from batch to batch.    * the array is not reset between batches, content past this.index will be stale.    */
specifier|private
specifier|final
name|VectorAggregationBufferRow
index|[]
name|aggregationBuffers
decl_stmt|;
comment|/**    * Same as aggregationBuffers but only distinct buffers    */
specifier|private
specifier|final
name|VectorAggregationBufferRow
index|[]
name|distinctAggregationBuffers
decl_stmt|;
comment|/**    * versioning number gets incremented on each batch. This allows us to cache the selection    * mapping info in the aggregation buffer set themselves while still being able to    * detect stale info.    */
specifier|private
name|int
name|version
decl_stmt|;
comment|/**    * Get the number of distinct aggregation buffer sets (ie. keys) used in current batch.    */
specifier|private
name|int
name|distinctCount
decl_stmt|;
comment|/**    * Memory consumed by a set of aggregation buffers    */
specifier|private
name|long
name|aggregatorsFixedSize
decl_stmt|;
comment|/**    * Array of indexes for aggregators that have variable size    */
specifier|private
name|int
index|[]
name|variableSizeAggregators
decl_stmt|;
empty_stmt|;
comment|/**    * returns True if any of the aggregators has a variable size    * @return    */
specifier|public
name|boolean
name|getHasVariableSize
parameter_list|()
block|{
return|return
name|variableSizeAggregators
operator|.
name|length
operator|>
literal|0
return|;
block|}
comment|/**    * Returns the fixed size consumed by the aggregation buffers    * @return    */
specifier|public
name|long
name|getAggregatorsFixedSize
parameter_list|()
block|{
return|return
name|aggregatorsFixedSize
return|;
block|}
comment|/**    * the array of aggregation buffers for the current batch.    * content past the {@link #getDistinctBufferSetCount()} index    * is stale from previous batches.    * @return    */
specifier|public
name|VectorAggregationBufferRow
index|[]
name|getAggregationBuffers
parameter_list|()
block|{
return|return
name|aggregationBuffers
return|;
block|}
comment|/**    * number of distinct aggregation buffer sets (ie. keys) in the current batch.    * @return    */
specifier|public
name|int
name|getDistinctBufferSetCount
parameter_list|()
block|{
return|return
name|distinctCount
return|;
block|}
specifier|public
name|VectorAggregationBufferBatch
parameter_list|()
block|{
name|aggregationBuffers
operator|=
operator|new
name|VectorAggregationBufferRow
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
name|distinctAggregationBuffers
operator|=
operator|new
name|VectorAggregationBufferRow
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
block|}
comment|/**    * resets the internal aggregation buffers sets index and increments the versioning    * used to optimize the selection vector population.    */
specifier|public
name|void
name|startBatch
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
name|distinctCount
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * assigns the given aggregation buffer set to a given batch row (by row number).    * populates the selection vector appropriately. This is where the versioning numbers    * play a role in determining if the index cached on the aggregation buffer set is stale.    */
specifier|public
name|void
name|mapAggregationBufferSet
parameter_list|(
name|VectorAggregationBufferRow
name|bufferSet
parameter_list|,
name|int
name|row
parameter_list|)
block|{
if|if
condition|(
name|version
operator|!=
name|bufferSet
operator|.
name|getVersion
argument_list|()
condition|)
block|{
name|bufferSet
operator|.
name|setVersionAndIndex
argument_list|(
name|version
argument_list|,
name|distinctCount
argument_list|)
expr_stmt|;
name|distinctAggregationBuffers
index|[
name|distinctCount
index|]
operator|=
name|bufferSet
expr_stmt|;
operator|++
name|distinctCount
expr_stmt|;
block|}
name|aggregationBuffers
index|[
name|row
index|]
operator|=
name|bufferSet
expr_stmt|;
block|}
specifier|public
name|void
name|compileAggregationBatchInfo
parameter_list|(
name|VectorAggregateExpression
index|[]
name|aggregators
parameter_list|)
block|{
name|JavaDataModel
name|model
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
index|[]
name|variableSizeAggregators
init|=
operator|new
name|int
index|[
name|aggregators
operator|.
name|length
index|]
decl_stmt|;
name|int
name|indexVariableSizes
init|=
literal|0
decl_stmt|;
name|aggregatorsFixedSize
operator|=
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
name|primitive1
argument_list|()
operator|*
literal|2
operator|+
name|model
operator|.
name|ref
argument_list|()
argument_list|,
name|model
operator|.
name|memoryAlign
argument_list|()
argument_list|)
expr_stmt|;
name|aggregatorsFixedSize
operator|+=
name|model
operator|.
name|lengthForObjectArrayOfSize
argument_list|(
name|aggregators
operator|.
name|length
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
name|aggregators
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|VectorAggregateExpression
name|aggregator
init|=
name|aggregators
index|[
name|i
index|]
decl_stmt|;
name|aggregatorsFixedSize
operator|+=
name|aggregator
operator|.
name|getAggregationBufferFixedSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|aggregator
operator|.
name|hasVariableSize
argument_list|()
condition|)
block|{
name|variableSizeAggregators
index|[
name|indexVariableSizes
index|]
operator|=
name|i
expr_stmt|;
operator|++
name|indexVariableSizes
expr_stmt|;
block|}
block|}
name|this
operator|.
name|variableSizeAggregators
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|variableSizeAggregators
argument_list|,
literal|0
argument_list|,
name|indexVariableSizes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getVariableSize
parameter_list|(
name|int
name|batchSize
parameter_list|)
block|{
name|int
name|variableSize
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
name|variableSizeAggregators
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|distinctCount
condition|;
operator|++
name|r
control|)
block|{
name|VectorAggregationBufferRow
name|buf
init|=
name|distinctAggregationBuffers
index|[
name|r
index|]
decl_stmt|;
name|variableSize
operator|+=
name|buf
operator|.
name|getAggregationBuffer
argument_list|(
name|variableSizeAggregators
index|[
name|i
index|]
argument_list|)
operator|.
name|getVariableSize
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|(
name|variableSize
operator|*
name|batchSize
operator|)
operator|/
name|distinctCount
return|;
block|}
block|}
end_class

end_unit

