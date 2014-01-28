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
name|io
operator|.
name|Serializable
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
name|Task
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
name|parse
operator|.
name|BaseSemanticAnalyzer
operator|.
name|tableSpec
import|;
end_import

begin_comment
comment|/**  * ConditionalStats.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Stats-Aggr Operator"
argument_list|)
specifier|public
class|class
name|StatsWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|tableSpec
name|tableSpecs
decl_stmt|;
comment|// source table spec -- for TableScanOperator
specifier|private
name|LoadTableDesc
name|loadTableDesc
decl_stmt|;
comment|// same as MoveWork.loadTableDesc -- for FileSinkOperator
specifier|private
name|LoadFileDesc
name|loadFileDesc
decl_stmt|;
comment|// same as MoveWork.loadFileDesc -- for FileSinkOperator
specifier|private
name|String
name|aggKey
decl_stmt|;
comment|// aggregation key prefix
specifier|private
name|boolean
name|statsReliable
decl_stmt|;
comment|// are stats completely reliable
comment|// If stats aggregator is not present, clear the current aggregator stats.
comment|// For eg. if a merge is being performed, stats already collected by aggregator (numrows etc.)
comment|// are still valid. However, if a load file is being performed, the old stats collected by
comment|// aggregator are not valid. It might be a good idea to clear them instead of leaving wrong
comment|// and old stats.
specifier|private
name|boolean
name|clearAggregatorStats
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|noStatsAggregator
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isNoScanAnalyzeCommand
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isPartialScanAnalyzeCommand
init|=
literal|false
decl_stmt|;
comment|// sourceTask for TS is not changed (currently) but that of FS might be changed
comment|// by various optimizers (auto.convert.join, for example)
comment|// so this is set by DriverContext in runtime
specifier|private
specifier|transient
name|Task
name|sourceTask
decl_stmt|;
specifier|public
name|StatsWork
parameter_list|()
block|{   }
specifier|public
name|StatsWork
parameter_list|(
name|tableSpec
name|tableSpecs
parameter_list|)
block|{
name|this
operator|.
name|tableSpecs
operator|=
name|tableSpecs
expr_stmt|;
block|}
specifier|public
name|StatsWork
parameter_list|(
name|LoadTableDesc
name|loadTableDesc
parameter_list|)
block|{
name|this
operator|.
name|loadTableDesc
operator|=
name|loadTableDesc
expr_stmt|;
block|}
specifier|public
name|StatsWork
parameter_list|(
name|LoadFileDesc
name|loadFileDesc
parameter_list|)
block|{
name|this
operator|.
name|loadFileDesc
operator|=
name|loadFileDesc
expr_stmt|;
block|}
specifier|public
name|StatsWork
parameter_list|(
name|boolean
name|statsReliable
parameter_list|)
block|{
name|this
operator|.
name|statsReliable
operator|=
name|statsReliable
expr_stmt|;
block|}
specifier|public
name|tableSpec
name|getTableSpecs
parameter_list|()
block|{
return|return
name|tableSpecs
return|;
block|}
specifier|public
name|LoadTableDesc
name|getLoadTableDesc
parameter_list|()
block|{
return|return
name|loadTableDesc
return|;
block|}
specifier|public
name|LoadFileDesc
name|getLoadFileDesc
parameter_list|()
block|{
return|return
name|loadFileDesc
return|;
block|}
specifier|public
name|void
name|setAggKey
parameter_list|(
name|String
name|aggK
parameter_list|)
block|{
name|aggKey
operator|=
name|aggK
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Stats Aggregation Key Prefix"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|String
name|getAggKey
parameter_list|()
block|{
return|return
name|aggKey
return|;
block|}
specifier|public
name|boolean
name|getNoStatsAggregator
parameter_list|()
block|{
return|return
name|noStatsAggregator
return|;
block|}
specifier|public
name|void
name|setNoStatsAggregator
parameter_list|(
name|boolean
name|noStatsAggregator
parameter_list|)
block|{
name|this
operator|.
name|noStatsAggregator
operator|=
name|noStatsAggregator
expr_stmt|;
block|}
specifier|public
name|boolean
name|isStatsReliable
parameter_list|()
block|{
return|return
name|statsReliable
return|;
block|}
specifier|public
name|void
name|setStatsReliable
parameter_list|(
name|boolean
name|statsReliable
parameter_list|)
block|{
name|this
operator|.
name|statsReliable
operator|=
name|statsReliable
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClearAggregatorStats
parameter_list|()
block|{
return|return
name|clearAggregatorStats
return|;
block|}
specifier|public
name|void
name|setClearAggregatorStats
parameter_list|(
name|boolean
name|clearAggregatorStats
parameter_list|)
block|{
name|this
operator|.
name|clearAggregatorStats
operator|=
name|clearAggregatorStats
expr_stmt|;
block|}
comment|/**    * @return the isNoScanAnalyzeCommand    */
specifier|public
name|boolean
name|isNoScanAnalyzeCommand
parameter_list|()
block|{
return|return
name|isNoScanAnalyzeCommand
return|;
block|}
comment|/**    * @param isNoScanAnalyzeCommand the isNoScanAnalyzeCommand to set    */
specifier|public
name|void
name|setNoScanAnalyzeCommand
parameter_list|(
name|boolean
name|isNoScanAnalyzeCommand
parameter_list|)
block|{
name|this
operator|.
name|isNoScanAnalyzeCommand
operator|=
name|isNoScanAnalyzeCommand
expr_stmt|;
block|}
comment|/**    * @return the isPartialScanAnalyzeCommand    */
specifier|public
name|boolean
name|isPartialScanAnalyzeCommand
parameter_list|()
block|{
return|return
name|isPartialScanAnalyzeCommand
return|;
block|}
comment|/**    * @param isPartialScanAnalyzeCommand the isPartialScanAnalyzeCommand to set    */
specifier|public
name|void
name|setPartialScanAnalyzeCommand
parameter_list|(
name|boolean
name|isPartialScanAnalyzeCommand
parameter_list|)
block|{
name|this
operator|.
name|isPartialScanAnalyzeCommand
operator|=
name|isPartialScanAnalyzeCommand
expr_stmt|;
block|}
specifier|public
name|Task
name|getSourceTask
parameter_list|()
block|{
return|return
name|sourceTask
return|;
block|}
specifier|public
name|void
name|setSourceTask
parameter_list|(
name|Task
name|sourceTask
parameter_list|)
block|{
name|this
operator|.
name|sourceTask
operator|=
name|sourceTask
expr_stmt|;
block|}
block|}
end_class

end_unit

