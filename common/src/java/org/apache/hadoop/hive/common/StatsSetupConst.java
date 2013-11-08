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
name|common
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

begin_comment
comment|/**  * A class that defines the constant strings used by the statistics implementation.  */
end_comment

begin_class
specifier|public
class|class
name|StatsSetupConst
block|{
comment|/**    * The value of the user variable "hive.stats.dbclass" to use HBase implementation.    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_IMPL_CLASS_VAL
init|=
literal|"hbase"
decl_stmt|;
comment|/**    * The value of the user variable "hive.stats.dbclass" to use JDBC implementation.    */
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_IMPL_CLASS_VAL
init|=
literal|"jdbc"
decl_stmt|;
comment|// statistics stored in metastore
comment|/**    * The name of the statistic Num Files to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|NUM_FILES
init|=
literal|"numFiles"
decl_stmt|;
comment|/**    * The name of the statistic Num Partitions to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|NUM_PARTITIONS
init|=
literal|"numPartitions"
decl_stmt|;
comment|/**    * The name of the statistic Total Size to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|TOTAL_SIZE
init|=
literal|"totalSize"
decl_stmt|;
comment|/**    * The name of the statistic Row Count to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|ROW_COUNT
init|=
literal|"numRows"
decl_stmt|;
comment|/**    * The name of the statistic Raw Data Size to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|RAW_DATA_SIZE
init|=
literal|"rawDataSize"
decl_stmt|;
comment|/**    * @return List of all supported statistics    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|supportedStats
init|=
operator|new
name|String
index|[]
block|{
name|NUM_FILES
block|,
name|ROW_COUNT
block|,
name|TOTAL_SIZE
block|,
name|RAW_DATA_SIZE
block|}
decl_stmt|;
comment|/**    * @return List of all statistics that need to be collected during query execution. These are    * statistics that inherently require a scan of the data.    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|statsRequireCompute
init|=
operator|new
name|String
index|[]
block|{
name|ROW_COUNT
block|,
name|RAW_DATA_SIZE
block|}
decl_stmt|;
comment|/**    * @return List of statistics that can be collected quickly without requiring a scan of the data.    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|fastStats
init|=
operator|new
name|String
index|[]
block|{
name|NUM_FILES
block|,
name|TOTAL_SIZE
block|}
decl_stmt|;
comment|// This string constant is used by stats task to indicate to AlterHandler that
comment|// alterPartition/alterTable is happening via statsTask.
specifier|public
specifier|static
specifier|final
name|String
name|STATS_GENERATED_VIA_STATS_TASK
init|=
literal|"STATS_GENERATED_VIA_STATS_TASK"
decl_stmt|;
comment|// This string constant will be persisted in metastore to indicate whether corresponding
comment|// table or partition's statistics are accurate or not.
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_STATS_ACCURATE
init|=
literal|"COLUMN_STATS_ACCURATE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TRUE
init|=
literal|"true"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FALSE
init|=
literal|"false"
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|areStatsUptoDate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|String
name|statsAcc
init|=
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
decl_stmt|;
return|return
name|statsAcc
operator|==
literal|null
condition|?
literal|false
else|:
name|statsAcc
operator|.
name|equals
argument_list|(
name|TRUE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

