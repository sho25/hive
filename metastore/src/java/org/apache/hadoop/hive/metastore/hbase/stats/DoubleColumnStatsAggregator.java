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
name|columnstats
operator|.
name|aggr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|DoubleColumnStatsData
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|DoubleColumnStatsAggregator
extends|extends
name|ColumnStatsAggregator
implements|implements
name|IExtrapolatePartStatus
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LongColumnStatsAggregator
operator|.
name|class
argument_list|)
decl_stmt|;
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
comment|// bitvectors
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"doAllPartitionContainStats for "
operator|+
name|colName
operator|+
literal|" is "
operator|+
name|doAllPartitionContainStats
argument_list|)
expr_stmt|;
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
name|getDoubleStats
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
name|getDoubleStats
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
name|getDoubleStats
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"all of the bit vectors can merge for "
operator|+
name|colName
operator|+
literal|" is "
operator|+
operator|(
name|ndvEstimator
operator|!=
literal|null
operator|)
argument_list|)
expr_stmt|;
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
operator|||
name|css
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
name|DoubleColumnStatsData
name|aggregateData
init|=
literal|null
decl_stmt|;
name|long
name|lowerBound
init|=
literal|0
decl_stmt|;
name|long
name|higherBound
init|=
literal|0
decl_stmt|;
name|double
name|densityAvgSum
init|=
literal|0.0
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
name|DoubleColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
decl_stmt|;
name|lowerBound
operator|=
name|Math
operator|.
name|max
argument_list|(
name|lowerBound
argument_list|,
name|newData
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
name|higherBound
operator|+=
name|newData
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
name|densityAvgSum
operator|+=
operator|(
name|newData
operator|.
name|getHighValue
argument_list|()
operator|-
name|newData
operator|.
name|getLowValue
argument_list|()
operator|)
operator|/
name|newData
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
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
block|}
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
name|setLowValue
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|aggregateData
operator|.
name|getLowValue
argument_list|()
argument_list|,
name|newData
operator|.
name|getLowValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setHighValue
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getHighValue
argument_list|()
argument_list|,
name|newData
operator|.
name|getHighValue
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
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
comment|// if all the ColumnStatisticsObjs contain bitvectors, we do not need to
comment|// use uniform distribution assumption because we can merge bitvectors
comment|// to get a good estimation.
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
block|}
else|else
block|{
name|long
name|estimation
decl_stmt|;
if|if
condition|(
name|useDensityFunctionForNDVEstimation
condition|)
block|{
comment|// We have estimation, lowerbound and higherbound. We use estimation
comment|// if it is between lowerbound and higherbound.
name|double
name|densityAvg
init|=
name|densityAvgSum
operator|/
name|partNames
operator|.
name|size
argument_list|()
decl_stmt|;
name|estimation
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|aggregateData
operator|.
name|getHighValue
argument_list|()
operator|-
name|aggregateData
operator|.
name|getLowValue
argument_list|()
operator|)
operator|/
name|densityAvg
argument_list|)
expr_stmt|;
if|if
condition|(
name|estimation
operator|<
name|lowerBound
condition|)
block|{
name|estimation
operator|=
name|lowerBound
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|estimation
operator|>
name|higherBound
condition|)
block|{
name|estimation
operator|=
name|higherBound
expr_stmt|;
block|}
block|}
else|else
block|{
name|estimation
operator|=
call|(
name|long
call|)
argument_list|(
name|lowerBound
operator|+
operator|(
name|higherBound
operator|-
name|lowerBound
operator|)
operator|*
name|ndvTuner
argument_list|)
expr_stmt|;
block|}
name|aggregateData
operator|.
name|setNumDVs
argument_list|(
name|estimation
argument_list|)
expr_stmt|;
block|}
name|columnStatisticsData
operator|.
name|setDoubleStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we need extrapolation
name|LOG
operator|.
name|debug
argument_list|(
literal|"start extrapolation for "
operator|+
name|colName
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|indexMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|partNames
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|indexMap
operator|.
name|put
argument_list|(
name|partNames
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|adjustedIndexMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnStatisticsData
argument_list|>
name|adjustedStatsMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ColumnStatisticsData
argument_list|>
argument_list|()
decl_stmt|;
comment|// while we scan the css, we also get the densityAvg, lowerbound and
comment|// higerbound when useDensityFunctionForNDVEstimation is true.
name|double
name|densityAvgSum
init|=
literal|0.0
decl_stmt|;
if|if
condition|(
name|ndvEstimator
operator|==
literal|null
condition|)
block|{
comment|// if not every partition uses bitvector for ndv, we just fall back to
comment|// the traditional extrapolation methods.
for|for
control|(
name|ColumnStatistics
name|cs
range|:
name|css
control|)
block|{
name|String
name|partName
init|=
name|cs
operator|.
name|getStatsDesc
argument_list|()
operator|.
name|getPartName
argument_list|()
decl_stmt|;
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
name|DoubleColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|useDensityFunctionForNDVEstimation
condition|)
block|{
name|densityAvgSum
operator|+=
operator|(
name|newData
operator|.
name|getHighValue
argument_list|()
operator|-
name|newData
operator|.
name|getLowValue
argument_list|()
operator|)
operator|/
name|newData
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
block|}
name|adjustedIndexMap
operator|.
name|put
argument_list|(
name|partName
argument_list|,
operator|(
name|double
operator|)
name|indexMap
operator|.
name|get
argument_list|(
name|partName
argument_list|)
argument_list|)
expr_stmt|;
name|adjustedStatsMap
operator|.
name|put
argument_list|(
name|partName
argument_list|,
name|cso
operator|.
name|getStatsData
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// we first merge all the adjacent bitvectors that we could merge and
comment|// derive new partition names and index.
name|StringBuilder
name|pseudoPartName
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|double
name|pseudoIndexSum
init|=
literal|0
decl_stmt|;
name|int
name|length
init|=
literal|0
decl_stmt|;
name|int
name|curIndex
init|=
operator|-
literal|1
decl_stmt|;
name|DoubleColumnStatsData
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
name|String
name|partName
init|=
name|cs
operator|.
name|getStatsDesc
argument_list|()
operator|.
name|getPartName
argument_list|()
decl_stmt|;
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
name|DoubleColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
decl_stmt|;
comment|// newData.isSetBitVectors() should be true for sure because we
comment|// already checked it before.
if|if
condition|(
name|indexMap
operator|.
name|get
argument_list|(
name|partName
argument_list|)
operator|!=
name|curIndex
condition|)
block|{
comment|// There is bitvector, but it is not adjacent to the previous ones.
if|if
condition|(
name|length
operator|>
literal|0
condition|)
block|{
comment|// we have to set ndv
name|adjustedIndexMap
operator|.
name|put
argument_list|(
name|pseudoPartName
operator|.
name|toString
argument_list|()
argument_list|,
name|pseudoIndexSum
operator|/
name|length
argument_list|)
expr_stmt|;
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
name|ColumnStatisticsData
name|csd
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|csd
operator|.
name|setDoubleStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
name|adjustedStatsMap
operator|.
name|put
argument_list|(
name|pseudoPartName
operator|.
name|toString
argument_list|()
argument_list|,
name|csd
argument_list|)
expr_stmt|;
if|if
condition|(
name|useDensityFunctionForNDVEstimation
condition|)
block|{
name|densityAvgSum
operator|+=
operator|(
name|aggregateData
operator|.
name|getHighValue
argument_list|()
operator|-
name|aggregateData
operator|.
name|getLowValue
argument_list|()
operator|)
operator|/
name|aggregateData
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
block|}
comment|// reset everything
name|pseudoPartName
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
name|pseudoIndexSum
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
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
name|aggregateData
operator|=
literal|null
expr_stmt|;
block|}
name|curIndex
operator|=
name|indexMap
operator|.
name|get
argument_list|(
name|partName
argument_list|)
expr_stmt|;
name|pseudoPartName
operator|.
name|append
argument_list|(
name|partName
argument_list|)
expr_stmt|;
name|pseudoIndexSum
operator|+=
name|curIndex
expr_stmt|;
name|length
operator|++
expr_stmt|;
name|curIndex
operator|++
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
name|setLowValue
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|aggregateData
operator|.
name|getLowValue
argument_list|()
argument_list|,
name|newData
operator|.
name|getLowValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setHighValue
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|aggregateData
operator|.
name|getHighValue
argument_list|()
argument_list|,
name|newData
operator|.
name|getHighValue
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
block|}
if|if
condition|(
name|length
operator|>
literal|0
condition|)
block|{
comment|// we have to set ndv
name|adjustedIndexMap
operator|.
name|put
argument_list|(
name|pseudoPartName
operator|.
name|toString
argument_list|()
argument_list|,
name|pseudoIndexSum
operator|/
name|length
argument_list|)
expr_stmt|;
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
name|ColumnStatisticsData
name|csd
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|csd
operator|.
name|setDoubleStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
name|adjustedStatsMap
operator|.
name|put
argument_list|(
name|pseudoPartName
operator|.
name|toString
argument_list|()
argument_list|,
name|csd
argument_list|)
expr_stmt|;
if|if
condition|(
name|useDensityFunctionForNDVEstimation
condition|)
block|{
name|densityAvgSum
operator|+=
operator|(
name|aggregateData
operator|.
name|getHighValue
argument_list|()
operator|-
name|aggregateData
operator|.
name|getLowValue
argument_list|()
operator|)
operator|/
name|aggregateData
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|extrapolate
argument_list|(
name|columnStatisticsData
argument_list|,
name|partNames
operator|.
name|size
argument_list|()
argument_list|,
name|css
operator|.
name|size
argument_list|()
argument_list|,
name|adjustedIndexMap
argument_list|,
name|adjustedStatsMap
argument_list|,
name|densityAvgSum
operator|/
name|adjustedStatsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ndv estimatation for "
operator|+
name|colName
operator|+
literal|" is "
operator|+
name|columnStatisticsData
operator|.
name|getDoubleStats
argument_list|()
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|void
name|extrapolate
parameter_list|(
name|ColumnStatisticsData
name|extrapolateData
parameter_list|,
name|int
name|numParts
parameter_list|,
name|int
name|numPartsWithStats
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|adjustedIndexMap
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnStatisticsData
argument_list|>
name|adjustedStatsMap
parameter_list|,
name|double
name|densityAvg
parameter_list|)
block|{
name|int
name|rightBorderInd
init|=
name|numParts
decl_stmt|;
name|DoubleColumnStatsData
name|extrapolateDoubleData
init|=
operator|new
name|DoubleColumnStatsData
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|extractedAdjustedStatsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ColumnStatisticsData
argument_list|>
name|entry
range|:
name|adjustedStatsMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|extractedAdjustedStatsMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|LinkedList
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
argument_list|>
argument_list|(
name|extractedAdjustedStatsMap
operator|.
name|entrySet
argument_list|()
argument_list|)
decl_stmt|;
comment|// get the lowValue
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o1
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getValue
argument_list|()
operator|.
name|getLowValue
argument_list|()
operator|<
name|o2
operator|.
name|getValue
argument_list|()
operator|.
name|getLowValue
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|double
name|minInd
init|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|double
name|maxInd
init|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|double
name|lowValue
init|=
literal|0
decl_stmt|;
name|double
name|min
init|=
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getLowValue
argument_list|()
decl_stmt|;
name|double
name|max
init|=
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getLowValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|minInd
operator|==
name|maxInd
condition|)
block|{
name|lowValue
operator|=
name|min
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minInd
operator|<
name|maxInd
condition|)
block|{
comment|// left border is the min
name|lowValue
operator|=
operator|(
name|max
operator|-
operator|(
name|max
operator|-
name|min
operator|)
operator|*
name|maxInd
operator|/
operator|(
name|maxInd
operator|-
name|minInd
operator|)
operator|)
expr_stmt|;
block|}
else|else
block|{
comment|// right border is the min
name|lowValue
operator|=
operator|(
name|max
operator|-
operator|(
name|max
operator|-
name|min
operator|)
operator|*
operator|(
name|rightBorderInd
operator|-
name|maxInd
operator|)
operator|/
operator|(
name|minInd
operator|-
name|maxInd
operator|)
operator|)
expr_stmt|;
block|}
comment|// get the highValue
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o1
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getValue
argument_list|()
operator|.
name|getHighValue
argument_list|()
operator|<
name|o2
operator|.
name|getValue
argument_list|()
operator|.
name|getHighValue
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|minInd
operator|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|maxInd
operator|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|double
name|highValue
init|=
literal|0
decl_stmt|;
name|min
operator|=
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getHighValue
argument_list|()
expr_stmt|;
name|max
operator|=
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getHighValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|minInd
operator|==
name|maxInd
condition|)
block|{
name|highValue
operator|=
name|min
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minInd
operator|<
name|maxInd
condition|)
block|{
comment|// right border is the max
name|highValue
operator|=
operator|(
name|min
operator|+
operator|(
name|max
operator|-
name|min
operator|)
operator|*
operator|(
name|rightBorderInd
operator|-
name|minInd
operator|)
operator|/
operator|(
name|maxInd
operator|-
name|minInd
operator|)
operator|)
expr_stmt|;
block|}
else|else
block|{
comment|// left border is the max
name|highValue
operator|=
operator|(
name|min
operator|+
operator|(
name|max
operator|-
name|min
operator|)
operator|*
name|minInd
operator|/
operator|(
name|minInd
operator|-
name|maxInd
operator|)
operator|)
expr_stmt|;
block|}
comment|// get the #nulls
name|long
name|numNulls
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|entry
range|:
name|extractedAdjustedStatsMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|numNulls
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getNumNulls
argument_list|()
expr_stmt|;
block|}
comment|// we scale up sumNulls based on the number of partitions
name|numNulls
operator|=
name|numNulls
operator|*
name|numParts
operator|/
name|numPartsWithStats
expr_stmt|;
comment|// get the ndv
name|long
name|ndv
init|=
literal|0
decl_stmt|;
name|long
name|ndvMin
init|=
literal|0
decl_stmt|;
name|long
name|ndvMax
init|=
literal|0
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o1
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
operator|<
name|o2
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|long
name|lowerBound
init|=
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
decl_stmt|;
name|long
name|higherBound
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|DoubleColumnStatsData
argument_list|>
name|entry
range|:
name|list
control|)
block|{
name|higherBound
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|useDensityFunctionForNDVEstimation
operator|&&
name|densityAvg
operator|!=
literal|0.0
condition|)
block|{
name|ndv
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|highValue
operator|-
name|lowValue
operator|)
operator|/
name|densityAvg
argument_list|)
expr_stmt|;
if|if
condition|(
name|ndv
operator|<
name|lowerBound
condition|)
block|{
name|ndv
operator|=
name|lowerBound
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ndv
operator|>
name|higherBound
condition|)
block|{
name|ndv
operator|=
name|higherBound
expr_stmt|;
block|}
block|}
else|else
block|{
name|minInd
operator|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|maxInd
operator|=
name|adjustedIndexMap
operator|.
name|get
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|ndvMin
operator|=
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
name|ndvMax
operator|=
name|list
operator|.
name|get
argument_list|(
name|list
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getNumDVs
argument_list|()
expr_stmt|;
if|if
condition|(
name|minInd
operator|==
name|maxInd
condition|)
block|{
name|ndv
operator|=
name|ndvMin
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minInd
operator|<
name|maxInd
condition|)
block|{
comment|// right border is the max
name|ndv
operator|=
call|(
name|long
call|)
argument_list|(
name|ndvMin
operator|+
operator|(
name|ndvMax
operator|-
name|ndvMin
operator|)
operator|*
operator|(
name|rightBorderInd
operator|-
name|minInd
operator|)
operator|/
operator|(
name|maxInd
operator|-
name|minInd
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// left border is the max
name|ndv
operator|=
call|(
name|long
call|)
argument_list|(
name|ndvMin
operator|+
operator|(
name|ndvMax
operator|-
name|ndvMin
operator|)
operator|*
name|minInd
operator|/
operator|(
name|minInd
operator|-
name|maxInd
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
name|extrapolateDoubleData
operator|.
name|setLowValue
argument_list|(
name|lowValue
argument_list|)
expr_stmt|;
name|extrapolateDoubleData
operator|.
name|setHighValue
argument_list|(
name|highValue
argument_list|)
expr_stmt|;
name|extrapolateDoubleData
operator|.
name|setNumNulls
argument_list|(
name|numNulls
argument_list|)
expr_stmt|;
name|extrapolateDoubleData
operator|.
name|setNumDVs
argument_list|(
name|ndv
argument_list|)
expr_stmt|;
name|extrapolateData
operator|.
name|setDoubleStats
argument_list|(
name|extrapolateDoubleData
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

