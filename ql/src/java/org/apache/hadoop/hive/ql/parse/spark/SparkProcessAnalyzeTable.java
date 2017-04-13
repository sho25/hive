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
name|parse
operator|.
name|spark
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|MapredParquetInputFormat
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
name|GenMapRedUtils
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
name|MapWork
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
name|SparkWork
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * ProcessAnalyzeTable sets up work for the several variants of analyze table  * (normal, no scan, partial scan.) The plan at this point will be a single  * table scan operator.  *  * Cloned from Tez ProcessAnalyzeTable.  */
end_comment

begin_class
specifier|public
class|class
name|SparkProcessAnalyzeTable
implements|implements
name|NodeProcessor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkProcessAnalyzeTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// shared plan utils for spark
specifier|private
name|GenSparkUtils
name|utils
init|=
literal|null
decl_stmt|;
comment|/**    * Injecting the utils in the constructor facilitates testing.    */
specifier|public
name|SparkProcessAnalyzeTable
parameter_list|(
name|GenSparkUtils
name|utils
parameter_list|)
block|{
name|this
operator|.
name|utils
operator|=
name|utils
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|procContext
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenSparkProcContext
name|context
init|=
operator|(
name|GenSparkProcContext
operator|)
name|procContext
decl_stmt|;
name|TableScanOperator
name|tableScan
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|parseContext
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormat
init|=
name|tableScan
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
if|if
condition|(
name|parseContext
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isAnalyzeCommand
argument_list|()
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|tableScan
operator|.
name|getChildOperators
argument_list|()
operator|==
literal|null
operator|||
name|tableScan
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|,
literal|"AssertionError: expected tableScan.getChildOperators() to be null, "
operator|+
literal|"or tableScan.getChildOperators().size() to be 0"
argument_list|)
expr_stmt|;
name|String
name|alias
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|a
range|:
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|tableScan
operator|==
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|get
argument_list|(
name|a
argument_list|)
condition|)
block|{
name|alias
operator|=
name|a
expr_stmt|;
block|}
block|}
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|alias
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected alias to be not null"
argument_list|)
expr_stmt|;
name|SparkWork
name|sparkWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|boolean
name|partialScan
init|=
name|parseContext
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
name|parseContext
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isNoScanAnalyzeCommand
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|OrcInputFormat
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|inputFormat
argument_list|)
operator|||
name|MapredParquetInputFormat
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|inputFormat
argument_list|)
operator|)
operator|&&
operator|(
name|noScan
operator|||
name|partialScan
operator|)
condition|)
block|{
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS partialscan;
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS noscan;
comment|// There will not be any Spark job above this task
name|StatsNoJobWork
name|snjWork
init|=
operator|new
name|StatsNoJobWork
argument_list|(
name|tableScan
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
name|parseContext
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
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|snjTask
operator|.
name|setParentTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|context
operator|.
name|rootTasks
operator|.
name|remove
argument_list|(
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
name|context
operator|.
name|rootTasks
operator|.
name|add
argument_list|(
name|snjTask
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS;
comment|// The plan consists of a simple SparkTask followed by a StatsTask.
comment|// The Spark task is just a simple TableScanOperator
name|StatsWork
name|statsWork
init|=
operator|new
name|StatsWork
argument_list|(
name|tableScan
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
name|tableScan
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
name|tableScan
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
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setStatsReliable
argument_list|(
name|parseContext
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
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|currentTask
operator|.
name|addDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
comment|// ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS noscan;
comment|// The plan consists of a StatsTask only.
if|if
condition|(
name|parseContext
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isNoScanAnalyzeCommand
argument_list|()
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
name|context
operator|.
name|rootTasks
operator|.
name|remove
argument_list|(
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
name|context
operator|.
name|rootTasks
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
name|parseContext
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isPartialScanAnalyzeCommand
argument_list|()
condition|)
block|{
name|handlePartialScanCommand
argument_list|(
name|tableScan
argument_list|,
name|parseContext
argument_list|,
name|statsWork
argument_list|,
name|context
argument_list|,
name|statsTask
argument_list|)
expr_stmt|;
block|}
comment|// NOTE: here we should use the new partition predicate pushdown API to get a list of pruned list,
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
name|tableScan
argument_list|)
decl_stmt|;
name|PrunedPartitionList
name|partitions
init|=
literal|null
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
name|tableScan
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
name|tableScan
argument_list|)
decl_stmt|;
name|partitions
operator|=
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
expr_stmt|;
block|}
name|MapWork
name|w
init|=
name|utils
operator|.
name|createMapWork
argument_list|(
name|context
argument_list|,
name|tableScan
argument_list|,
name|sparkWork
argument_list|,
name|partitions
argument_list|)
decl_stmt|;
name|w
operator|.
name|setGatheringStats
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * handle partial scan command.    *    * It is composed of PartialScanTask followed by StatsTask.    */
specifier|private
name|void
name|handlePartialScanCommand
parameter_list|(
name|TableScanOperator
name|tableScan
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|,
name|StatsWork
name|statsWork
parameter_list|,
name|GenSparkProcContext
name|context
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
name|tableScan
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
name|tableScan
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
name|tableScan
operator|.
name|getConf
argument_list|()
operator|.
name|getTmpStatsDir
argument_list|()
argument_list|,
name|parseContext
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Task
argument_list|<
name|PartialScanWork
argument_list|>
name|partialScanTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|scanWork
argument_list|,
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|partialScanTask
operator|.
name|initialize
argument_list|(
name|parseContext
operator|.
name|getQueryState
argument_list|()
argument_list|,
literal|null
argument_list|,
name|driverCxt
argument_list|,
name|tableScan
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|)
expr_stmt|;
name|partialScanTask
operator|.
name|setWork
argument_list|(
name|scanWork
argument_list|)
expr_stmt|;
name|statsWork
operator|.
name|setSourceTask
argument_list|(
name|partialScanTask
argument_list|)
expr_stmt|;
comment|// task dependency
name|context
operator|.
name|rootTasks
operator|.
name|remove
argument_list|(
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
name|context
operator|.
name|rootTasks
operator|.
name|add
argument_list|(
name|partialScanTask
argument_list|)
expr_stmt|;
name|partialScanTask
operator|.
name|addDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

