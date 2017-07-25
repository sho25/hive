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
name|merge
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
name|Date
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
name|DateColumnStatsData
import|;
end_import

begin_class
specifier|public
class|class
name|DateColumnStatsMerger
extends|extends
name|ColumnStatsMerger
block|{
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|ColumnStatisticsObj
name|aggregateColStats
parameter_list|,
name|ColumnStatisticsObj
name|newColStats
parameter_list|)
block|{
name|DateColumnStatsData
name|aggregateData
init|=
name|aggregateColStats
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDateStats
argument_list|()
decl_stmt|;
name|DateColumnStatsData
name|newData
init|=
name|newColStats
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDateStats
argument_list|()
decl_stmt|;
name|Date
name|lowValue
init|=
name|aggregateData
operator|.
name|getLowValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|newData
operator|.
name|getLowValue
argument_list|()
argument_list|)
operator|<
literal|0
condition|?
name|aggregateData
operator|.
name|getLowValue
argument_list|()
else|:
name|newData
operator|.
name|getLowValue
argument_list|()
decl_stmt|;
name|aggregateData
operator|.
name|setLowValue
argument_list|(
name|lowValue
argument_list|)
expr_stmt|;
name|Date
name|highValue
init|=
name|aggregateData
operator|.
name|getHighValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|newData
operator|.
name|getHighValue
argument_list|()
argument_list|)
operator|>=
literal|0
condition|?
name|aggregateData
operator|.
name|getHighValue
argument_list|()
else|:
name|newData
operator|.
name|getHighValue
argument_list|()
decl_stmt|;
name|aggregateData
operator|.
name|setHighValue
argument_list|(
name|highValue
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
operator|!
name|aggregateData
operator|.
name|isSetBitVectors
argument_list|()
operator|||
name|aggregateData
operator|.
name|getBitVectors
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
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
name|NumDistinctValueEstimator
name|oldEst
init|=
name|NumDistinctValueEstimatorFactory
operator|.
name|getNumDistinctValueEstimator
argument_list|(
name|aggregateData
operator|.
name|getBitVectors
argument_list|()
argument_list|)
decl_stmt|;
name|NumDistinctValueEstimator
name|newEst
init|=
name|NumDistinctValueEstimatorFactory
operator|.
name|getNumDistinctValueEstimator
argument_list|(
name|newData
operator|.
name|getBitVectors
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|ndv
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|oldEst
operator|.
name|canMerge
argument_list|(
name|newEst
argument_list|)
condition|)
block|{
name|oldEst
operator|.
name|mergeEstimators
argument_list|(
name|newEst
argument_list|)
expr_stmt|;
name|ndv
operator|=
name|oldEst
operator|.
name|estimateNumDistinctValues
argument_list|()
expr_stmt|;
name|aggregateData
operator|.
name|setBitVectors
argument_list|(
name|oldEst
operator|.
name|serialize
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ndv
operator|=
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
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Use bitvector to merge column "
operator|+
name|aggregateColStats
operator|.
name|getColName
argument_list|()
operator|+
literal|"'s ndvs of "
operator|+
name|aggregateData
operator|.
name|getNumDVs
argument_list|()
operator|+
literal|" and "
operator|+
name|newData
operator|.
name|getNumDVs
argument_list|()
operator|+
literal|" to be "
operator|+
name|ndv
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumDVs
argument_list|(
name|ndv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

