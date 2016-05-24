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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsData
import|;
end_import

begin_interface
specifier|public
interface|interface
name|IExtrapolatePartStatus
block|{
comment|// The following function will extrapolate the stats when the column stats of
comment|// some partitions are missing.
comment|/**    * @param extrapolateData    *          it will carry back the specific stats, e.g., DOUBLE_STATS or    *          LONG_STATS    * @param numParts    *          the total number of partitions    * @param numPartsWithStats    *          the number of partitions that have stats    * @param adjustedIndexMap    *          the partition name to index map    * @param adjustedStatsMap    *          the partition name to its stats map    * @param densityAvg    *          the average of ndv density, which is useful when    *          useDensityFunctionForNDVEstimation is true.    */
specifier|public
specifier|abstract
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
function_decl|;
block|}
end_interface

end_unit

