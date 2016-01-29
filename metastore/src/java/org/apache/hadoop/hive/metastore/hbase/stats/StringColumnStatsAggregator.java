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
name|void
name|aggregate
parameter_list|(
name|ColumnStatisticsObj
name|aggregateColStats
parameter_list|,
name|ColumnStatisticsObj
name|newColStats
parameter_list|)
block|{
name|StringColumnStatsData
name|aggregateData
init|=
name|aggregateColStats
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
name|StringColumnStatsData
name|newData
init|=
name|newColStats
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|ndvEstimator
operator|==
literal|null
operator|||
operator|!
name|newData
operator|.
name|isSetBitVectors
argument_list|()
operator|||
name|newData
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
else|else
block|{
name|ndvEstimator
operator|.
name|mergeEstimators
argument_list|(
operator|new
name|NumDistinctValueEstimator
argument_list|(
name|newData
operator|.
name|getBitVectors
argument_list|()
argument_list|,
name|ndvEstimator
operator|.
name|getnumBitVectors
argument_list|()
argument_list|)
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
name|aggregateData
operator|.
name|setBitVectors
argument_list|(
name|ndvEstimator
operator|.
name|serialize
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

