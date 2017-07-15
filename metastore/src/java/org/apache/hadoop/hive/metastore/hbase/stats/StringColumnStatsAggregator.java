begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|hbase
operator|.
name|stats
package|;
end_package

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
name|common
operator|.
name|ndv
operator|.
name|NumDistinctValueEstimator
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
name|ndv
operator|.
name|NumDistinctValueEstimatorFactory
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatistics
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsData
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsObj
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|StringColumnStatsData
import|;
end_import

begin_class
specifier|public
class|class
name|StringColumnStatsAggregator
extends|extends
name|ColumnStatsAggregator
block|{
annotation|@
name|Override
specifier|public
name|ColumnStatisticsObj
name|aggregate
parameter_list|(
name|String
name|colName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|css
parameter_list|)
throws|throws
name|MetaException
block|{
name|ColumnStatisticsObj
name|statsObj
init|=
literal|null
decl_stmt|;
comment|// check if all the ColumnStatisticsObjs contain stats and all the ndv are
comment|// bitvectors. Only when both of the conditions are true, we merge bit
comment|// vectors. Otherwise, just use the maximum function.
name|boolean
name|doAllPartitionContainStats
init|=
name|partNames
operator|.
name|size
argument_list|()
operator|==
name|css
operator|.
name|size
argument_list|()
decl_stmt|;
name|NumDistinctValueEstimator
name|ndvEstimator
init|=
literal|null
decl_stmt|;
name|String
name|colType
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ColumnStatistics
name|cs
range|:
name|css
control|)
block|{
if|if
condition|(
name|cs
operator|.
name|getStatsObjSize
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The number of columns should be exactly one in aggrStats, but found "
operator|+
name|cs
operator|.
name|getStatsObjSize
argument_list|()
argument_list|)
throw|;
block|}
name|ColumnStatisticsObj
name|cso
init|=
name|cs
operator|.
name|getStatsObjIterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|statsObj
operator|==
literal|null
condition|)
block|{
name|colType
operator|=
name|cso
operator|.
name|getColType
argument_list|()
expr_stmt|;
name|statsObj
operator|=
name|ColumnStatsAggregatorFactory
operator|.
name|newColumnStaticsObj
argument_list|(
name|colName
argument_list|,
name|colType
argument_list|,
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getSetField
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|isSetBitVectors
argument_list|()
operator|||
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|getBitVectors
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
break|break;
block|}
else|else
block|{
comment|// check if all of the bit vectors can merge
name|NumDistinctValueEstimator
name|estimator
init|=
name|NumDistinctValueEstimatorFactory
operator|.
name|getNumDistinctValueEstimator
argument_list|(
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|getBitVectors
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ndvEstimator
operator|==
literal|null
condition|)
block|{
name|ndvEstimator
operator|=
name|estimator
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|ndvEstimator
operator|.
name|canMerge
argument_list|(
name|estimator
argument_list|)
condition|)
block|{
continue|continue;
block|}
else|else
block|{
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|ndvEstimator
operator|=
name|NumDistinctValueEstimatorFactory
operator|.
name|getEmptyNumDistinctValueEstimator
argument_list|(
name|ndvEstimator
argument_list|)
expr_stmt|;
block|}
name|ColumnStatisticsData
name|columnStatisticsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
if|if
condition|(
name|doAllPartitionContainStats
operator|&&
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|StringColumnStatsData
name|aggregateData
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ColumnStatistics
name|cs
range|:
name|css
control|)
block|{
name|ColumnStatisticsObj
name|cso
init|=
name|cs
operator|.
name|getStatsObjIterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|StringColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
name|ndvEstimator
operator|.
name|mergeEstimators
argument_list|(
name|NumDistinctValueEstimatorFactory
operator|.
name|getNumDistinctValueEstimator
argument_list|(
name|newData
operator|.
name|getBitVectors
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|aggregateData
operator|==
literal|null
condition|)
block|{
name|aggregateData
operator|=
name|newData
operator|.
name|deepCopy
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|aggregateData
operator|.
name|setMaxColLen
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getMaxColLen
argument_list|()
argument_list|,
name|newData
operator|.
name|getMaxColLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setAvgColLen
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getAvgColLen
argument_list|()
argument_list|,
name|newData
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumNulls
argument_list|(
name|aggregateData
operator|.
name|getNumNulls
argument_list|()
operator|+
name|newData
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|aggregateData
operator|.
name|setNumDVs
argument_list|(
name|ndvEstimator
operator|.
name|estimateNumDistinctValues
argument_list|()
argument_list|)
expr_stmt|;
name|columnStatisticsData
operator|.
name|setStringStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|StringColumnStatsData
name|aggregateData
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ColumnStatistics
name|cs
range|:
name|css
control|)
block|{
name|ColumnStatisticsObj
name|cso
init|=
name|cs
operator|.
name|getStatsObjIterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|StringColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|aggregateData
operator|==
literal|null
condition|)
block|{
name|aggregateData
operator|=
name|newData
operator|.
name|deepCopy
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|aggregateData
operator|.
name|setMaxColLen
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getMaxColLen
argument_list|()
argument_list|,
name|newData
operator|.
name|getMaxColLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setAvgColLen
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getAvgColLen
argument_list|()
argument_list|,
name|newData
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumNulls
argument_list|(
name|aggregateData
operator|.
name|getNumNulls
argument_list|()
operator|+
name|newData
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumDVs
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getNumDVs
argument_list|()
argument_list|,
name|newData
operator|.
name|getNumDVs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|columnStatisticsData
operator|.
name|setStringStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
block|}
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|columnStatisticsData
argument_list|)
expr_stmt|;
return|return
name|statsObj
return|;
block|}
block|}
end_class

end_unit

