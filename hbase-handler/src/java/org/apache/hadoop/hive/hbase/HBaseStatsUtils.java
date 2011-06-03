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
name|hbase
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
name|Collection
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|stats
operator|.
name|StatsSetupConst
import|;
end_import

begin_class
specifier|public
class|class
name|HBaseStatsUtils
block|{
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|supportedStats
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|columnNameMapping
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
comment|// supported statistics
name|supportedStats
operator|.
name|add
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|supportedStats
operator|.
name|add
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
expr_stmt|;
comment|// row count statistics
name|columnNameMapping
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|,
name|HBaseStatsSetupConstants
operator|.
name|PART_STAT_ROW_COUNT_COLUMN_NAME
argument_list|)
expr_stmt|;
comment|// raw data size
name|columnNameMapping
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|HBaseStatsSetupConstants
operator|.
name|PART_STAT_RAW_DATA_SIZE_COLUMN_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the set of supported statistics    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getSupportedStatistics
parameter_list|()
block|{
return|return
name|supportedStats
return|;
block|}
comment|/**    * Retrieves the value for a particular stat from the published map.    *    * @param statType    *          - statistic type to be retrieved from the map    * @param stats    *          - stats map    * @return value for the given statistic as string, "0" if the statistic is not present    */
specifier|public
specifier|static
name|String
name|getStatFromMap
parameter_list|(
name|String
name|statType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|stats
parameter_list|)
block|{
name|String
name|value
init|=
name|stats
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|"0"
return|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * Check if the set to be published is within the supported statistics.    * It must also contain at least the basic statistics (used for comparison).    *    * @param stats    *          - stats to be published    * @return true if is a valid statistic set, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isValidStatisticSet
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|stats
parameter_list|)
block|{
if|if
condition|(
operator|!
name|stats
operator|.
name|contains
argument_list|(
name|getBasicStat
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|String
name|stat
range|:
name|stats
control|)
block|{
if|if
condition|(
operator|!
name|supportedStats
operator|.
name|contains
argument_list|(
name|stat
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Check if a particular statistic type is supported    *    * @param statType    *          - statistic to be published    * @return true if statType is supported, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isValidStatistic
parameter_list|(
name|String
name|statType
parameter_list|)
block|{
return|return
name|supportedStats
operator|.
name|contains
argument_list|(
name|statType
argument_list|)
return|;
block|}
comment|/**    * Returns the HBase column where the statistics for the given type are stored.    *    * @param statType    *          - supported statistic.    * @return column name for the given statistic.    */
specifier|public
specifier|static
name|byte
index|[]
name|getColumnName
parameter_list|(
name|String
name|statType
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnNameMapping
operator|.
name|get
argument_list|(
name|statType
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns the family name for stored statistics.    */
specifier|public
specifier|static
name|byte
index|[]
name|getFamilyName
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HBaseStatsSetupConstants
operator|.
name|PART_STAT_COLUMN_FAMILY
argument_list|)
return|;
block|}
comment|/**    * Returns the basic type of the supported statistics.    * It is used to determine which statistics are fresher.    */
specifier|public
specifier|static
name|String
name|getBasicStat
parameter_list|()
block|{
return|return
name|supportedStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

