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
name|stats
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
name|Collection
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|FileStatus
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
name|common
operator|.
name|StatsSetupConst
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Warehouse
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
name|EnvironmentContext
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
name|utils
operator|.
name|MetaStoreUtils
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
name|CompilationOpContext
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
name|ErrorMsg
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
name|Utilities
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
name|Hive
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
name|HiveException
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
name|parse
operator|.
name|BaseSemanticAnalyzer
operator|.
name|TableSpec
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
name|BasicStatsWork
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
name|DynamicPartitionCtx
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
name|LoadTableDesc
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
name|api
operator|.
name|StageType
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|util
operator|.
name|StringUtils
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * StatsTask implementation. StatsTask mainly deals with "collectable" stats. These are  * stats that require data scanning and are collected during query execution (unless the user  * explicitly requests data scanning just for the purpose of stats computation using the "ANALYZE"  * command. All other stats are computed directly by the MetaStore. The rationale being that the  * MetaStore layer covers all Thrift calls and provides better guarantees about the accuracy of  * those stats.  **/
end_comment

begin_class
specifier|public
class|class
name|BasicStatsTask
implements|implements
name|Serializable
implements|,
name|IStatsProcessor
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
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BasicStatsTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|Partition
argument_list|>
name|dpPartSpecs
decl_stmt|;
specifier|public
name|boolean
name|followedColStats
decl_stmt|;
specifier|private
name|BasicStatsWork
name|work
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|public
name|BasicStatsTask
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|BasicStatsWork
name|work
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|dpPartSpecs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|process
parameter_list|(
name|Hive
name|db
parameter_list|,
name|Table
name|tbl
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing stats task"
argument_list|)
expr_stmt|;
name|table
operator|=
name|tbl
expr_stmt|;
return|return
name|aggregateStats
argument_list|(
name|db
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|CompilationOpContext
name|opContext
parameter_list|)
block|{   }
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|STATS
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"STATS"
return|;
block|}
specifier|private
specifier|static
class|class
name|BasicStatsProcessor
block|{
specifier|private
name|Partish
name|partish
decl_stmt|;
specifier|private
name|FileStatus
index|[]
name|partfileStatus
decl_stmt|;
specifier|private
name|BasicStatsWork
name|work
decl_stmt|;
specifier|private
name|boolean
name|followedColStats1
decl_stmt|;
specifier|public
name|BasicStatsProcessor
parameter_list|(
name|Partish
name|partish
parameter_list|,
name|BasicStatsWork
name|work
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|boolean
name|followedColStats2
parameter_list|)
block|{
name|this
operator|.
name|partish
operator|=
name|partish
expr_stmt|;
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
name|followedColStats1
operator|=
name|followedColStats2
expr_stmt|;
block|}
specifier|public
name|Object
name|process
parameter_list|(
name|StatsAggregator
name|statsAggregator
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
block|{
name|Partish
name|p
init|=
name|partish
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|p
operator|.
name|getPartParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|p
operator|.
name|isAcid
argument_list|()
condition|)
block|{
name|StatsSetupConst
operator|.
name|setBasicStatsState
argument_list|(
name|parameters
argument_list|,
name|StatsSetupConst
operator|.
name|FALSE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|work
operator|.
name|isTargetRewritten
argument_list|()
condition|)
block|{
name|StatsSetupConst
operator|.
name|setBasicStatsState
argument_list|(
name|parameters
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
comment|// work.getTableSpecs() == null means it is not analyze command
comment|// and then if it is not followed by column stats, we should clean
comment|// column stats
comment|// FIXME: move this to ColStat related part
if|if
condition|(
operator|!
name|work
operator|.
name|isExplicitAnalyze
argument_list|()
operator|&&
operator|!
name|followedColStats1
condition|)
block|{
name|StatsSetupConst
operator|.
name|clearColumnStatsState
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partfileStatus
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Partition/partfiles is null for: "
operator|+
name|partish
operator|.
name|getPartition
argument_list|()
operator|.
name|getSpec
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// The collectable stats for the aggregator needs to be cleared.
comment|// For eg. if a file is being loaded, the old number of rows are not valid
comment|// XXX: makes no sense for me... possibly not needed anymore
if|if
condition|(
name|work
operator|.
name|isClearAggregatorStats
argument_list|()
condition|)
block|{
comment|// we choose to keep the invalid stats and only change the setting.
name|StatsSetupConst
operator|.
name|setBasicStatsState
argument_list|(
name|parameters
argument_list|,
name|StatsSetupConst
operator|.
name|FALSE
argument_list|)
expr_stmt|;
block|}
name|updateQuickStats
argument_list|(
name|parameters
argument_list|,
name|partfileStatus
argument_list|)
expr_stmt|;
if|if
condition|(
name|StatsSetupConst
operator|.
name|areBasicStatsUptoDate
argument_list|(
name|parameters
argument_list|)
condition|)
block|{
if|if
condition|(
name|statsAggregator
operator|!=
literal|null
condition|)
block|{
name|String
name|prefix
init|=
name|getAggregationPrefix
argument_list|(
name|p
operator|.
name|getTable
argument_list|()
argument_list|,
name|p
operator|.
name|getPartition
argument_list|()
argument_list|)
decl_stmt|;
name|updateStats
argument_list|(
name|statsAggregator
argument_list|,
name|parameters
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|p
operator|.
name|getOutput
argument_list|()
return|;
block|}
specifier|public
name|void
name|collectFileStatus
parameter_list|(
name|Warehouse
name|wh
parameter_list|)
throws|throws
name|MetaException
block|{
name|partfileStatus
operator|=
name|wh
operator|.
name|getFileStatusesForSD
argument_list|(
name|partish
operator|.
name|getPartSd
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|updateQuickStats
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
name|FileStatus
index|[]
name|partfileStatus
parameter_list|)
throws|throws
name|MetaException
block|{
name|MetaStoreUtils
operator|.
name|populateQuickStats
argument_list|(
name|partfileStatus
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getAggregationPrefix
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|prefix
init|=
name|getAggregationPrefix0
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
decl_stmt|;
name|String
name|aggKey
init|=
name|prefix
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|?
name|prefix
else|:
name|prefix
operator|+
name|Path
operator|.
name|SEPARATOR
decl_stmt|;
return|return
name|aggKey
return|;
block|}
specifier|private
name|String
name|getAggregationPrefix0
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// prefix is of the form dbName.tblName
name|String
name|prefix
init|=
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|MetaStoreUtils
operator|.
name|encodeTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
comment|// FIXME: this is a secret contract; reusein getAggrKey() creates a more closer relation to the StatsGatherer
comment|// prefix = work.getAggKey();
name|prefix
operator|=
name|prefix
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
name|partition
operator|!=
literal|null
condition|)
block|{
return|return
name|Utilities
operator|.
name|join
argument_list|(
name|prefix
argument_list|,
name|Warehouse
operator|.
name|makePartPath
argument_list|(
name|partition
operator|.
name|getSpec
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
return|return
name|prefix
return|;
block|}
specifier|private
name|void
name|updateStats
parameter_list|(
name|StatsAggregator
name|statsAggregator
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
name|String
name|aggKey
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|String
name|statType
range|:
name|StatsSetupConst
operator|.
name|statsRequireCompute
control|)
block|{
name|String
name|value
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
name|aggKey
argument_list|,
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
operator|&&
operator|!
name|value
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|longValue
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|work
operator|.
name|isTargetRewritten
argument_list|()
condition|)
block|{
name|String
name|originalValue
init|=
name|parameters
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|originalValue
operator|!=
literal|null
condition|)
block|{
name|longValue
operator|+=
name|Long
operator|.
name|parseLong
argument_list|(
name|originalValue
argument_list|)
expr_stmt|;
comment|// todo: invalid + valid = invalid
block|}
block|}
name|parameters
operator|.
name|put
argument_list|(
name|statType
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|longValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|int
name|aggregateStats
parameter_list|(
name|Hive
name|db
parameter_list|)
block|{
name|StatsAggregator
name|statsAggregator
init|=
literal|null
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
name|StatsCollectionContext
name|scc
init|=
literal|null
decl_stmt|;
name|EnvironmentContext
name|environmentContext
init|=
literal|null
decl_stmt|;
name|environmentContext
operator|=
operator|new
name|EnvironmentContext
argument_list|()
expr_stmt|;
name|environmentContext
operator|.
name|putToProperties
argument_list|(
name|StatsSetupConst
operator|.
name|DO_NOT_UPDATE_STATS
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Stats setup:
specifier|final
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|getWork
argument_list|()
operator|.
name|getNoStatsAggregator
argument_list|()
operator|&&
operator|!
name|getWork
argument_list|()
operator|.
name|isNoScanAnalyzeCommand
argument_list|()
condition|)
block|{
try|try
block|{
name|scc
operator|=
name|getContext
argument_list|()
expr_stmt|;
name|statsAggregator
operator|=
name|createStatsAggregator
argument_list|(
name|scc
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_RELIABLE
argument_list|)
condition|)
block|{
throw|throw
name|e
throw|;
block|}
name|console
operator|.
name|printError
argument_list|(
name|ErrorMsg
operator|.
name|STATS_SKIPPING_BY_ERROR
operator|.
name|getErrorCodedMsg
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|getPartitionsList
argument_list|(
name|db
argument_list|)
decl_stmt|;
name|String
name|tableFullName
init|=
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|table
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Partish
argument_list|>
name|partishes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitions
operator|==
literal|null
condition|)
block|{
name|Partish
name|p
decl_stmt|;
name|partishes
operator|.
name|add
argument_list|(
name|p
operator|=
operator|new
name|Partish
operator|.
name|PTable
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|BasicStatsProcessor
name|basicStatsProcessor
init|=
operator|new
name|BasicStatsProcessor
argument_list|(
name|p
argument_list|,
name|work
argument_list|,
name|conf
argument_list|,
name|followedColStats
argument_list|)
decl_stmt|;
name|basicStatsProcessor
operator|.
name|collectFileStatus
argument_list|(
name|wh
argument_list|)
expr_stmt|;
name|Table
name|res
init|=
operator|(
name|Table
operator|)
name|basicStatsProcessor
operator|.
name|process
argument_list|(
name|statsAggregator
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// Stats task should not set creation signature
name|res
operator|.
name|getTTable
argument_list|()
operator|.
name|unsetCreationMetadata
argument_list|()
expr_stmt|;
name|db
operator|.
name|alterTable
argument_list|(
name|tableFullName
argument_list|,
name|res
argument_list|,
name|environmentContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Table "
operator|+
name|tableFullName
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|p
operator|.
name|getPartParameters
argument_list|()
argument_list|)
operator|+
literal|']'
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableFullName
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|p
operator|.
name|getPartParameters
argument_list|()
argument_list|)
operator|+
literal|']'
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Partitioned table:
comment|// Need to get the old stats of the partition
comment|// and update the table stats based on the old and new stats.
name|List
argument_list|<
name|Partition
argument_list|>
name|updates
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|ExecutorService
name|pool
init|=
name|buildBasicStatsExecutor
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Future
argument_list|<
name|Void
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BasicStatsProcessor
argument_list|>
name|processors
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
specifier|final
name|Partition
name|partn
range|:
name|partitions
control|)
block|{
name|Partish
name|p
decl_stmt|;
name|BasicStatsProcessor
name|bsp
init|=
operator|new
name|BasicStatsProcessor
argument_list|(
name|p
operator|=
operator|new
name|Partish
operator|.
name|PPart
argument_list|(
name|table
argument_list|,
name|partn
argument_list|)
argument_list|,
name|work
argument_list|,
name|conf
argument_list|,
name|followedColStats
argument_list|)
decl_stmt|;
name|processors
operator|.
name|add
argument_list|(
name|bsp
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|pool
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|bsp
operator|.
name|collectFileStatus
argument_list|(
name|wh
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
for|for
control|(
name|Future
argument_list|<
name|Void
argument_list|>
name|future
range|:
name|futures
control|)
block|{
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cancelling "
operator|+
name|futures
operator|.
name|size
argument_list|()
operator|+
literal|" file stats lookup tasks"
argument_list|)
expr_stmt|;
comment|//cancel other futures
for|for
control|(
name|Future
name|future
range|:
name|futures
control|)
block|{
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Fail the query if the stats are supposed to be reliable
if|if
condition|(
name|work
operator|.
name|isStatsReliable
argument_list|()
condition|)
block|{
name|ret
operator|=
literal|1
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished getting file stats of all partitions!"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|BasicStatsProcessor
name|basicStatsProcessor
range|:
name|processors
control|)
block|{
name|Object
name|res
init|=
name|basicStatsProcessor
operator|.
name|process
argument_list|(
name|statsAggregator
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Partition "
operator|+
name|basicStatsProcessor
operator|.
name|partish
operator|.
name|getPartition
argument_list|()
operator|.
name|getSpec
argument_list|()
operator|+
literal|" stats: [0]"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|updates
operator|.
name|add
argument_list|(
operator|(
name|Partition
operator|)
name|res
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Partition "
operator|+
name|basicStatsProcessor
operator|.
name|partish
operator|.
name|getPartition
argument_list|()
operator|.
name|getSpec
argument_list|()
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|basicStatsProcessor
operator|.
name|partish
operator|.
name|getPartParameters
argument_list|()
argument_list|)
operator|+
literal|']'
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Partition "
operator|+
name|basicStatsProcessor
operator|.
name|partish
operator|.
name|getPartition
argument_list|()
operator|.
name|getSpec
argument_list|()
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|basicStatsProcessor
operator|.
name|partish
operator|.
name|getPartParameters
argument_list|()
argument_list|)
operator|+
literal|']'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|updates
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|db
operator|.
name|alterPartitions
argument_list|(
name|tableFullName
argument_list|,
name|updates
argument_list|,
name|environmentContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|work
operator|.
name|isStatsReliable
argument_list|()
operator|&&
name|updates
operator|.
name|size
argument_list|()
operator|!=
name|processors
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stats should be reliadble...however seems like there were some issue.. => ret 1"
argument_list|)
expr_stmt|;
name|ret
operator|=
literal|1
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"[Warning] could not update stats."
argument_list|,
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
comment|// Fail the query if the stats are supposed to be reliable
if|if
condition|(
name|work
operator|.
name|isStatsReliable
argument_list|()
condition|)
block|{
name|ret
operator|=
literal|1
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|statsAggregator
operator|!=
literal|null
condition|)
block|{
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|scc
argument_list|)
expr_stmt|;
block|}
block|}
comment|// The return value of 0 indicates success,
comment|// anything else indicates failure
return|return
name|ret
return|;
block|}
specifier|private
name|BasicStatsWork
name|getWork
parameter_list|()
block|{
return|return
name|work
return|;
block|}
specifier|private
name|ExecutorService
name|buildBasicStatsExecutor
parameter_list|()
block|{
comment|//Get the file status up-front for all partitions. Beneficial in cases of blob storage systems
name|int
name|poolSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|ConfVars
operator|.
name|HIVE_MOVE_FILES_THREAD_COUNT
operator|.
name|varname
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// In case thread count is set to 0, use single thread.
name|poolSize
operator|=
name|Math
operator|.
name|max
argument_list|(
name|poolSize
argument_list|,
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|ExecutorService
name|pool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|poolSize
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"stats-updater-thread-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Getting file stats of all partitions. threadpool size:"
operator|+
name|poolSize
argument_list|)
expr_stmt|;
return|return
name|pool
return|;
block|}
specifier|private
name|StatsAggregator
name|createStatsAggregator
parameter_list|(
name|StatsCollectionContext
name|scc
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|statsImpl
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTATSDBCLASS
argument_list|)
decl_stmt|;
name|StatsFactory
name|factory
init|=
name|StatsFactory
operator|.
name|newFactory
argument_list|(
name|statsImpl
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|factory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_NOT_OBTAINED
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
comment|// initialize stats publishing table for noscan which has only stats task
comment|// the rest of MR task following stats task initializes it in ExecDriver.java
name|StatsPublisher
name|statsPublisher
init|=
name|factory
operator|.
name|getStatsPublisher
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|init
argument_list|(
name|scc
argument_list|)
condition|)
block|{
comment|// creating stats table if not exists
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_INITIALIZATION_ERROR
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
comment|// manufacture a StatsAggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|statsAggregator
operator|.
name|connect
argument_list|(
name|scc
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSAGGREGATOR_CONNECTION_ERROR
operator|.
name|getErrorCodedMsg
argument_list|(
name|statsImpl
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|statsAggregator
return|;
block|}
specifier|private
name|StatsCollectionContext
name|getContext
parameter_list|()
throws|throws
name|HiveException
block|{
name|StatsCollectionContext
name|scc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Task
name|sourceTask
init|=
name|getWork
argument_list|()
operator|.
name|getSourceTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceTask
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSAGGREGATOR_SOURCETASK_NULL
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
name|scc
operator|.
name|setTask
argument_list|(
name|sourceTask
argument_list|)
expr_stmt|;
name|scc
operator|.
name|setStatsTmpDir
argument_list|(
name|this
operator|.
name|getWork
argument_list|()
operator|.
name|getStatsTmpDir
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|scc
return|;
block|}
specifier|private
name|String
name|toString
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|statType
range|:
name|StatsSetupConst
operator|.
name|supportedStats
control|)
block|{
name|String
name|value
init|=
name|parameters
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|statType
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Get the list of partitions that need to update statistics.    * TODO: we should reuse the Partitions generated at compile time    * since getting the list of partitions is quite expensive.    *    * @return a list of partitions that need to update statistics.    * @throws HiveException    */
specifier|private
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsList
parameter_list|(
name|Hive
name|db
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|work
operator|.
name|getLoadFileDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
comment|//we are in CTAS, so we know there are no partitions
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getTableSpecs
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// ANALYZE command
name|TableSpec
name|tblSpec
init|=
name|work
operator|.
name|getTableSpecs
argument_list|()
decl_stmt|;
name|table
operator|=
name|tblSpec
operator|.
name|tableHandle
expr_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// get all partitions that matches with the partition spec
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|tblSpec
operator|.
name|partitions
decl_stmt|;
if|if
condition|(
name|partitions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|partn
range|:
name|partitions
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|partn
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|.
name|getLoadTableDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// INSERT OVERWRITE command
name|LoadTableDesc
name|tbd
init|=
name|work
operator|.
name|getLoadTableDesc
argument_list|()
decl_stmt|;
name|table
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|DynamicPartitionCtx
name|dpCtx
init|=
name|tbd
operator|.
name|getDPCtx
argument_list|()
decl_stmt|;
if|if
condition|(
name|dpCtx
operator|!=
literal|null
operator|&&
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// dynamic partitions
comment|// If no dynamic partitions are generated, dpPartSpecs may not be initialized
if|if
condition|(
name|dpPartSpecs
operator|!=
literal|null
condition|)
block|{
comment|// load the list of DP partitions and return the list of partition specs
name|list
operator|.
name|addAll
argument_list|(
name|dpPartSpecs
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// static partition
name|Partition
name|partn
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|partn
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|Partition
argument_list|>
name|getDpPartSpecs
parameter_list|()
block|{
return|return
name|dpPartSpecs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDpPartSpecs
parameter_list|(
name|Collection
argument_list|<
name|Partition
argument_list|>
name|dpPartSpecs
parameter_list|)
block|{
name|this
operator|.
name|dpPartSpecs
operator|=
name|dpPartSpecs
expr_stmt|;
block|}
block|}
end_class

end_unit

