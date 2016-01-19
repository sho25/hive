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
name|optimizer
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
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
name|fs
operator|.
name|Path
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
name|conf
operator|.
name|HiveConf
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
name|DriverContext
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
name|Operator
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
name|TableScanOperator
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
name|exec
operator|.
name|TaskFactory
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
name|mr
operator|.
name|MapRedTask
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
name|io
operator|.
name|orc
operator|.
name|OrcInputFormat
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
name|io
operator|.
name|rcfile
operator|.
name|stats
operator|.
name|PartialScanWork
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|optimizer
operator|.
name|GenMRProcContext
operator|.
name|GenMapRedCtx
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
name|ParseContext
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
name|PrunedPartitionList
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
name|SemanticException
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
name|plan
operator|.
name|MapredWork
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
name|plan
operator|.
name|OperatorDesc
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
name|plan
operator|.
name|StatsNoJobWork
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
name|plan
operator|.
name|StatsWork
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
name|mapred
operator|.
name|InputFormat
import|;
end_import

begin_comment
comment|/**  * Processor for the rule - table scan.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRTableScan1
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRTableScan1
parameter_list|()
block|{   }
comment|/**    * Table Sink encountered.    * @param nd    *          the table sink operator encountered    * @param opProcCtx    *          context    */
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|opProcCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableScanOperator
name|op
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormat
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
operator|.
name|getInputFormatClass
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
comment|// create a dummy MapReduce task
name|MapredWork
name|currWork
init|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|(
name|parseCtx
argument_list|)
decl_stmt|;
name|MapRedTask
name|currTask
init|=
operator|(
name|MapRedTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|currWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
name|op
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|parseCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currOp
init|=
name|parseCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|currOp
operator|==
name|op
condition|)
block|{
name|String
name|currAliasId
init|=
name|alias
decl_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|op
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|currTask
argument_list|,
name|currAliasId
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|parseCtx
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isAnalyzeCommand
argument_list|()
condition|)
block|{
name|boolean
name|partialScan
init|=
name|parseCtx
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isPartialScanAnalyzeCommand
argument_list|()
decl_stmt|;
name|boolean
name|noScan
init|=
name|parseCtx
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isNoScanAnalyzeCommand
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputFormat
operator|.
name|equals
argument_list|(
name|OrcInputFormat
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// For ORC, all the following statements are the same
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS partialscan;
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS noscan;
comment|// There will not be any MR or Tez job above this task
name|StatsNoJobWork
name|snjWork
init|=
operator|new
name|StatsNoJobWork
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
operator|.
name|getTableSpec
argument_list|()
argument_list|)
decl_stmt|;
name|snjWork
operator|.
name|setStatsReliable
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_RELIABLE
argument_list|)
argument_list|)
expr_stmt|;
comment|// If partition is specified, get pruned partition list
name|Set
argument_list|<
name|Partition
argument_list|>
name|confirmedParts
init|=
name|GenMapRedUtils
operator|.
name|getConfirmedPartitionsForScan
argument_list|(
name|op
argument_list|)
decl_stmt|;
if|if
condition|(
name|confirmedParts
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Table
name|source
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partCols
init|=
name|GenMapRedUtils
operator|.
name|getPartitionColumns
argument_list|(
name|op
argument_list|)
decl_stmt|;
name|PrunedPartitionList
name|partList
init|=
operator|new
name|PrunedPartitionList
argument_list|(
name|source
argument_list|,
name|confirmedParts
argument_list|,
name|partCols
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|snjWork
operator|.
name|setPrunedPartitionList
argument_list|(
name|partList
argument_list|)
expr_stmt|;
block|}
name|Task
argument_list|<
name|StatsNoJobWork
argument_list|>
name|snjTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|snjWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|snjTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|snjTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS;
comment|// The plan consists of a simple MapRedTask followed by a StatsTask.
comment|// The MR task is just a simple TableScanOperator
name|StatsWork
name|statsWork
init|=
operator|new
name|StatsWork
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
operator|.
name|getTableSpec
argument_list|()
argument_list|)
decl_stmt|;
name|statsWork
operator|.
name|setAggKey
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getStatsAggPrefix
argument_list|()
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setStatsTmpDir
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTmpStatsDir
argument_list|()
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setSourceTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setStatsReliable
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_RELIABLE
argument_list|)
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|StatsWork
argument_list|>
name|statsTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|statsWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|currTask
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS noscan;
comment|// The plan consists of a StatsTask only.
if|if
condition|(
name|noScan
condition|)
block|{
name|statsTask
operator|.
name|setParentTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setNoScanAnalyzeCommand
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|remove
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
block|}
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS partialscan;
if|if
condition|(
name|partialScan
condition|)
block|{
name|handlePartialScanCommand
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|,
name|parseCtx
argument_list|,
name|currTask
argument_list|,
name|statsWork
argument_list|,
name|statsTask
argument_list|)
expr_stmt|;
block|}
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setGatheringStats
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|currWork
operator|.
name|getReduceWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|currWork
operator|.
name|getReduceWork
argument_list|()
operator|.
name|setGatheringStats
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// NOTE: here we should use the new partition predicate pushdown API to get a list of
comment|// pruned list,
comment|// and pass it to setTaskPlan as the last parameter
name|Set
argument_list|<
name|Partition
argument_list|>
name|confirmedPartns
init|=
name|GenMapRedUtils
operator|.
name|getConfirmedPartitionsForScan
argument_list|(
name|op
argument_list|)
decl_stmt|;
if|if
condition|(
name|confirmedPartns
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Table
name|source
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partCols
init|=
name|GenMapRedUtils
operator|.
name|getPartitionColumns
argument_list|(
name|op
argument_list|)
decl_stmt|;
name|PrunedPartitionList
name|partList
init|=
operator|new
name|PrunedPartitionList
argument_list|(
name|source
argument_list|,
name|confirmedPartns
argument_list|,
name|partCols
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|op
argument_list|,
name|currTask
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|,
name|partList
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// non-partitioned table
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|op
argument_list|,
name|currTask
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
assert|assert
literal|false
assert|;
return|return
literal|null
return|;
block|}
comment|/**    * handle partial scan command. It is composed of PartialScanTask followed by StatsTask .    * @param op    * @param ctx    * @param parseCtx    * @param currTask    * @param parseInfo    * @param statsWork    * @param statsTask    * @throws SemanticException    */
specifier|private
name|void
name|handlePartialScanCommand
parameter_list|(
name|TableScanOperator
name|op
parameter_list|,
name|GenMRProcContext
name|ctx
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|,
name|StatsWork
name|statsWork
parameter_list|,
name|Task
argument_list|<
name|StatsWork
argument_list|>
name|statsTask
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|aggregationKey
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getStatsAggPrefix
argument_list|()
decl_stmt|;
name|StringBuilder
name|aggregationKeyBuffer
init|=
operator|new
name|StringBuilder
argument_list|(
name|aggregationKey
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
init|=
name|GenMapRedUtils
operator|.
name|getInputPathsForPartialScan
argument_list|(
name|op
argument_list|,
name|aggregationKeyBuffer
argument_list|)
decl_stmt|;
name|aggregationKey
operator|=
name|aggregationKeyBuffer
operator|.
name|toString
argument_list|()
expr_stmt|;
comment|// scan work
name|PartialScanWork
name|scanWork
init|=
operator|new
name|PartialScanWork
argument_list|(
name|inputPaths
argument_list|)
decl_stmt|;
name|scanWork
operator|.
name|setMapperCannotSpanPartns
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scanWork
operator|.
name|setAggKey
argument_list|(
name|aggregationKey
argument_list|)
expr_stmt|;
name|scanWork
operator|.
name|setStatsTmpDir
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getTmpStatsDir
argument_list|()
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
comment|// stats work
name|statsWork
operator|.
name|setPartialScanAnalyzeCommand
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// partial scan task
name|DriverContext
name|driverCxt
init|=
operator|new
name|DriverContext
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|PartialScanWork
argument_list|>
name|psTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|scanWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|psTask
operator|.
name|initialize
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
literal|null
argument_list|,
name|driverCxt
argument_list|,
name|op
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|)
expr_stmt|;
name|psTask
operator|.
name|setWork
argument_list|(
name|scanWork
argument_list|)
expr_stmt|;
comment|// task dependency
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|remove
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|psTask
argument_list|)
expr_stmt|;
name|psTask
operator|.
name|addDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|parentTasks
operator|.
name|add
argument_list|(
name|psTask
argument_list|)
expr_stmt|;
name|statsTask
operator|.
name|setParentTasks
argument_list|(
name|parentTasks
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

