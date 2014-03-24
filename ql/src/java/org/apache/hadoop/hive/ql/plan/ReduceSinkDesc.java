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
name|plan
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

begin_comment
comment|/**  * ReduceSinkDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Reduce Output Operator"
argument_list|)
specifier|public
class|class
name|ReduceSinkDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Key columns are passed to reducer in the "key".    */
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputKeyColumnNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColumnIndices
decl_stmt|;
comment|/**    * Value columns are passed to reducer in the "value".    */
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputValueColumnNames
decl_stmt|;
comment|/**    * Describe how to serialize the key.    */
specifier|private
name|TableDesc
name|keySerializeInfo
decl_stmt|;
comment|/**    * Describe how to serialize the value.    */
specifier|private
name|TableDesc
name|valueSerializeInfo
decl_stmt|;
comment|/**    * The tag for this reducesink descriptor.    */
specifier|private
name|int
name|tag
decl_stmt|;
comment|/**    * Number of distribution keys.    */
specifier|private
name|int
name|numDistributionKeys
decl_stmt|;
comment|/**    * Used in tez. Holds the name of the output    * that this reduce sink is writing to.    */
specifier|private
name|String
name|outputName
decl_stmt|;
comment|/**    * The partition columns (CLUSTER BY or DISTRIBUTE BY in Hive language).    * Partition columns decide the reducer that the current row goes to.    * Partition columns are not passed to reducer.    */
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
decl_stmt|;
specifier|private
name|int
name|numReducers
decl_stmt|;
comment|/**    * Bucket information    */
specifier|private
name|int
name|numBuckets
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|bucketCols
decl_stmt|;
specifier|private
name|int
name|topN
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|float
name|topNMemoryUsage
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|mapGroupBy
decl_stmt|;
comment|// for group-by, values with same key on top-K should be forwarded
specifier|public
name|ReduceSinkDesc
parameter_list|()
block|{   }
specifier|public
name|ReduceSinkDesc
parameter_list|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|int
name|numDistributionKeys
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputKeyColumnNames
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColumnIndices
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputValueColumnNames
parameter_list|,
name|int
name|tag
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
parameter_list|,
name|int
name|numReducers
parameter_list|,
specifier|final
name|TableDesc
name|keySerializeInfo
parameter_list|,
specifier|final
name|TableDesc
name|valueSerializeInfo
parameter_list|)
block|{
name|this
operator|.
name|keyCols
operator|=
name|keyCols
expr_stmt|;
name|this
operator|.
name|numDistributionKeys
operator|=
name|numDistributionKeys
expr_stmt|;
name|this
operator|.
name|valueCols
operator|=
name|valueCols
expr_stmt|;
name|this
operator|.
name|outputKeyColumnNames
operator|=
name|outputKeyColumnNames
expr_stmt|;
name|this
operator|.
name|outputValueColumnNames
operator|=
name|outputValueColumnNames
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
name|this
operator|.
name|numReducers
operator|=
name|numReducers
expr_stmt|;
name|this
operator|.
name|partitionCols
operator|=
name|partitionCols
expr_stmt|;
name|this
operator|.
name|keySerializeInfo
operator|=
name|keySerializeInfo
expr_stmt|;
name|this
operator|.
name|valueSerializeInfo
operator|=
name|valueSerializeInfo
expr_stmt|;
name|this
operator|.
name|distinctColumnIndices
operator|=
name|distinctColumnIndices
expr_stmt|;
name|this
operator|.
name|setNumBuckets
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|setBucketCols
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|ReduceSinkDesc
name|desc
init|=
operator|new
name|ReduceSinkDesc
argument_list|()
decl_stmt|;
name|desc
operator|.
name|setKeyCols
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
operator|)
name|getKeyCols
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setValueCols
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
operator|)
name|getValueCols
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setOutputKeyColumnNames
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|String
argument_list|>
operator|)
name|getOutputKeyColumnNames
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColumnIndicesClone
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Integer
argument_list|>
name|distinctColumnIndex
range|:
name|getDistinctColumnIndices
argument_list|()
control|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|tmp
operator|.
name|addAll
argument_list|(
name|distinctColumnIndex
argument_list|)
expr_stmt|;
name|distinctColumnIndicesClone
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
block|}
name|desc
operator|.
name|setDistinctColumnIndices
argument_list|(
name|distinctColumnIndicesClone
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setOutputValueColumnNames
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|String
argument_list|>
operator|)
name|getOutputValueColumnNames
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumDistributionKeys
argument_list|(
name|getNumDistributionKeys
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setTag
argument_list|(
name|getTag
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumReducers
argument_list|(
name|getNumReducers
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setPartitionCols
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
operator|)
name|getPartitionCols
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setKeySerializeInfo
argument_list|(
operator|(
name|TableDesc
operator|)
name|getKeySerializeInfo
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setValueSerializeInfo
argument_list|(
operator|(
name|TableDesc
operator|)
name|getValueSerializeInfo
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumBuckets
argument_list|(
name|numBuckets
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setBucketCols
argument_list|(
name|bucketCols
argument_list|)
expr_stmt|;
return|return
name|desc
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getOutputKeyColumnNames
parameter_list|()
block|{
return|return
name|outputKeyColumnNames
return|;
block|}
specifier|public
name|void
name|setOutputKeyColumnNames
parameter_list|(
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputKeyColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputKeyColumnNames
operator|=
name|outputKeyColumnNames
expr_stmt|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getOutputValueColumnNames
parameter_list|()
block|{
return|return
name|outputValueColumnNames
return|;
block|}
specifier|public
name|void
name|setOutputValueColumnNames
parameter_list|(
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputValueColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputValueColumnNames
operator|=
name|outputValueColumnNames
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"key expressions"
argument_list|)
specifier|public
name|String
name|getKeyColString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|keyCols
argument_list|)
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getKeyCols
parameter_list|()
block|{
return|return
name|keyCols
return|;
block|}
specifier|public
name|void
name|setKeyCols
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|)
block|{
name|this
operator|.
name|keyCols
operator|=
name|keyCols
expr_stmt|;
block|}
specifier|public
name|int
name|getNumDistributionKeys
parameter_list|()
block|{
return|return
name|this
operator|.
name|numDistributionKeys
return|;
block|}
specifier|public
name|void
name|setNumDistributionKeys
parameter_list|(
name|int
name|numKeys
parameter_list|)
block|{
name|this
operator|.
name|numDistributionKeys
operator|=
name|numKeys
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"value expressions"
argument_list|)
specifier|public
name|String
name|getValueColsString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|valueCols
argument_list|)
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getValueCols
parameter_list|()
block|{
return|return
name|valueCols
return|;
block|}
specifier|public
name|void
name|setValueCols
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
parameter_list|)
block|{
name|this
operator|.
name|valueCols
operator|=
name|valueCols
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map-reduce partition columns"
argument_list|)
specifier|public
name|String
name|getParitionColsString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|partitionCols
argument_list|)
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getPartitionCols
parameter_list|()
block|{
return|return
name|partitionCols
return|;
block|}
specifier|public
name|void
name|setPartitionCols
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
parameter_list|)
block|{
name|this
operator|.
name|partitionCols
operator|=
name|partitionCols
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"tag"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|int
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
specifier|public
name|void
name|setTag
parameter_list|(
name|int
name|tag
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
block|}
specifier|public
name|int
name|getTopN
parameter_list|()
block|{
return|return
name|topN
return|;
block|}
specifier|public
name|void
name|setTopN
parameter_list|(
name|int
name|topN
parameter_list|)
block|{
name|this
operator|.
name|topN
operator|=
name|topN
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"TopN"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|Integer
name|getTopNExplain
parameter_list|()
block|{
return|return
name|topN
operator|>
literal|0
condition|?
name|topN
else|:
literal|null
return|;
block|}
specifier|public
name|float
name|getTopNMemoryUsage
parameter_list|()
block|{
return|return
name|topNMemoryUsage
return|;
block|}
specifier|public
name|void
name|setTopNMemoryUsage
parameter_list|(
name|float
name|topNMemoryUsage
parameter_list|)
block|{
name|this
operator|.
name|topNMemoryUsage
operator|=
name|topNMemoryUsage
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"TopN Hash Memory Usage"
argument_list|)
specifier|public
name|Float
name|getTopNMemoryUsageExplain
parameter_list|()
block|{
return|return
name|topN
operator|>
literal|0
operator|&&
name|topNMemoryUsage
operator|>
literal|0
condition|?
name|topNMemoryUsage
else|:
literal|null
return|;
block|}
specifier|public
name|boolean
name|isMapGroupBy
parameter_list|()
block|{
return|return
name|mapGroupBy
return|;
block|}
specifier|public
name|void
name|setMapGroupBy
parameter_list|(
name|boolean
name|mapGroupBy
parameter_list|)
block|{
name|this
operator|.
name|mapGroupBy
operator|=
name|mapGroupBy
expr_stmt|;
block|}
comment|/**    * Returns the number of reducers for the map-reduce job. -1 means to decide    * the number of reducers at runtime. This enables Hive to estimate the number    * of reducers based on the map-reduce input data size, which is only    * available right before we start the map-reduce job.    */
specifier|public
name|int
name|getNumReducers
parameter_list|()
block|{
return|return
name|numReducers
return|;
block|}
specifier|public
name|void
name|setNumReducers
parameter_list|(
name|int
name|numReducers
parameter_list|)
block|{
name|this
operator|.
name|numReducers
operator|=
name|numReducers
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getKeySerializeInfo
parameter_list|()
block|{
return|return
name|keySerializeInfo
return|;
block|}
specifier|public
name|void
name|setKeySerializeInfo
parameter_list|(
name|TableDesc
name|keySerializeInfo
parameter_list|)
block|{
name|this
operator|.
name|keySerializeInfo
operator|=
name|keySerializeInfo
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getValueSerializeInfo
parameter_list|()
block|{
return|return
name|valueSerializeInfo
return|;
block|}
specifier|public
name|void
name|setValueSerializeInfo
parameter_list|(
name|TableDesc
name|valueSerializeInfo
parameter_list|)
block|{
name|this
operator|.
name|valueSerializeInfo
operator|=
name|valueSerializeInfo
expr_stmt|;
block|}
comment|/**    * Returns the sort order of the key columns.    *    * @return null, which means ascending order for all key columns, or a String    *         of the same length as key columns, that consists of only "+"    *         (ascending order) and "-" (descending order).    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sort order"
argument_list|)
specifier|public
name|String
name|getOrder
parameter_list|()
block|{
return|return
name|keySerializeInfo
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|)
return|;
block|}
specifier|public
name|void
name|setOrder
parameter_list|(
name|String
name|orderStr
parameter_list|)
block|{
name|keySerializeInfo
operator|.
name|getProperties
argument_list|()
operator|.
name|setProperty
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|,
name|orderStr
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|getDistinctColumnIndices
parameter_list|()
block|{
return|return
name|distinctColumnIndices
return|;
block|}
specifier|public
name|void
name|setDistinctColumnIndices
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColumnIndices
parameter_list|)
block|{
name|this
operator|.
name|distinctColumnIndices
operator|=
name|distinctColumnIndices
expr_stmt|;
block|}
specifier|public
name|String
name|getOutputName
parameter_list|()
block|{
return|return
name|outputName
return|;
block|}
specifier|public
name|void
name|setOutputName
parameter_list|(
name|String
name|outputName
parameter_list|)
block|{
name|this
operator|.
name|outputName
operator|=
name|outputName
expr_stmt|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|bucketCols
return|;
block|}
specifier|public
name|void
name|setBucketCols
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|bucketCols
parameter_list|)
block|{
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
block|}
block|}
end_class

end_unit

