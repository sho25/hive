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
name|exec
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
name|ConcurrentMap
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FileSystem
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
name|fs
operator|.
name|PathFilter
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
name|api
operator|.
name|InvalidOperationException
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
name|QueryPlan
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
name|StatsProvidingRecordReader
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
name|shims
operator|.
name|ShimLoader
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
name|FileSplit
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
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
name|JobConf
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
name|Reporter
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
name|ReflectionUtils
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
name|collect
operator|.
name|MapMaker
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
comment|/**  * StatsNoJobTask is used in cases where stats collection is the only task for the given query (no  * parent MR or Tez job). It is used in the following cases 1) ANALYZE with partialscan/noscan for  * file formats that implement StatsProvidingRecordReader interface: ORC format (implements  * StatsProvidingRecordReader) stores column statistics for all columns in the file footer. Its much  * faster to compute the table/partition statistics by reading the footer than scanning all the  * rows. This task can be used for computing basic stats like numFiles, numRows, fileSize,  * rawDataSize from ORC footer.  **/
end_comment

begin_class
specifier|public
class|class
name|StatsNoJobTask
extends|extends
name|Task
argument_list|<
name|StatsNoJobWork
argument_list|>
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
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|StatsNoJobTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Partition
argument_list|>
name|partUpdates
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
name|String
name|tableFullName
decl_stmt|;
specifier|private
specifier|static
name|JobConf
name|jc
init|=
literal|null
decl_stmt|;
specifier|public
name|StatsNoJobTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|,
name|driverContext
argument_list|)
expr_stmt|;
name|jc
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing stats (no job) task"
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|""
decl_stmt|;
name|ExecutorService
name|threadPool
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tableName
operator|=
name|work
operator|.
name|getTableSpecs
argument_list|()
operator|.
name|tableName
expr_stmt|;
name|table
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|int
name|numThreads
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STATS_GATHER_NUM_THREADS
argument_list|)
decl_stmt|;
name|tableFullName
operator|=
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
expr_stmt|;
name|threadPool
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numThreads
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
literal|"StatsNoJobTask-Thread-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|partUpdates
operator|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
name|numThreads
argument_list|)
operator|.
name|makeMap
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized threadpool for stats computation with "
operator|+
name|numThreads
operator|+
literal|" threads"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot get table "
operator|+
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Cannot get table "
operator|+
name|tableName
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|aggregateStats
argument_list|(
name|threadPool
argument_list|)
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"STATS-NO-JOB"
return|;
block|}
class|class
name|StatsCollection
implements|implements
name|Runnable
block|{
specifier|private
name|Partition
name|partn
decl_stmt|;
specifier|public
name|StatsCollection
parameter_list|(
name|Partition
name|part
parameter_list|)
block|{
name|this
operator|.
name|partn
operator|=
name|part
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// get the list of partitions
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
name|Partition
name|tPart
init|=
name|partn
operator|.
name|getTPartition
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|tPart
operator|.
name|getParameters
argument_list|()
decl_stmt|;
try|try
block|{
name|Path
name|dir
init|=
operator|new
name|Path
argument_list|(
name|tPart
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|numRows
init|=
literal|0
decl_stmt|;
name|long
name|rawDataSize
init|=
literal|0
decl_stmt|;
name|long
name|fileSize
init|=
literal|0
decl_stmt|;
name|long
name|numFiles
init|=
literal|0
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|fileList
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|listLocatedStatus
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|hiddenFileFilter
argument_list|)
decl_stmt|;
name|boolean
name|statsAvailable
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fileList
control|)
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|inputFormat
init|=
operator|(
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|partn
operator|.
name|getInputFormatClass
argument_list|()
argument_list|,
name|jc
argument_list|)
decl_stmt|;
name|InputSplit
name|dummySplit
init|=
operator|new
name|FileSplit
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|new
name|String
index|[]
block|{
name|partn
operator|.
name|getLocation
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|recordReader
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|dummySplit
argument_list|,
name|jc
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
decl_stmt|;
name|StatsProvidingRecordReader
name|statsRR
decl_stmt|;
if|if
condition|(
name|recordReader
operator|instanceof
name|StatsProvidingRecordReader
condition|)
block|{
name|statsRR
operator|=
operator|(
name|StatsProvidingRecordReader
operator|)
name|recordReader
expr_stmt|;
name|rawDataSize
operator|+=
name|statsRR
operator|.
name|getStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
expr_stmt|;
name|numRows
operator|+=
name|statsRR
operator|.
name|getStats
argument_list|()
operator|.
name|getRowCount
argument_list|()
expr_stmt|;
name|fileSize
operator|+=
name|file
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|numFiles
operator|+=
literal|1
expr_stmt|;
name|statsAvailable
operator|=
literal|true
expr_stmt|;
block|}
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|statsAvailable
condition|)
block|{
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numRows
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|rawDataSize
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|TOTAL_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|fileSize
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|NUM_FILES
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numFiles
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_GENERATED_VIA_STATS_TASK
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|partUpdates
operator|.
name|put
argument_list|(
name|tPart
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
operator|new
name|Partition
argument_list|(
name|table
argument_list|,
name|tPart
argument_list|)
argument_list|)
expr_stmt|;
comment|// printout console and debug logs
name|String
name|threadName
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|msg
init|=
literal|"Partition "
operator|+
name|tableFullName
operator|+
name|partn
operator|.
name|getSpec
argument_list|()
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|parameters
argument_list|)
operator|+
literal|']'
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|threadName
operator|+
literal|": "
operator|+
name|msg
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|threadName
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|msg
init|=
literal|"Partition "
operator|+
name|tableFullName
operator|+
name|partn
operator|.
name|getSpec
argument_list|()
operator|+
literal|" does not provide stats."
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|threadName
operator|+
literal|": "
operator|+
name|msg
argument_list|)
expr_stmt|;
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
literal|"[Warning] could not update stats for "
operator|+
name|tableFullName
operator|+
name|partn
operator|.
name|getSpec
argument_list|()
operator|+
literal|"."
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
comment|// Before updating the partition params, if any partition params is null
comment|// and if statsReliable is true then updatePartition() function  will fail
comment|// the task by returning 1
if|if
condition|(
name|work
operator|.
name|isStatsReliable
argument_list|()
condition|)
block|{
name|partUpdates
operator|.
name|put
argument_list|(
name|tPart
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
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
block|}
specifier|private
name|int
name|aggregateStats
parameter_list|(
name|ExecutorService
name|threadPool
parameter_list|)
block|{
name|int
name|ret
init|=
literal|0
decl_stmt|;
try|try
block|{
name|Collection
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getPrunedPartitionList
argument_list|()
operator|==
literal|null
condition|)
block|{
name|partitions
operator|=
name|getPartitionsList
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|partitions
operator|=
name|work
operator|.
name|getPrunedPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
expr_stmt|;
block|}
comment|// non-partitioned table
if|if
condition|(
name|partitions
operator|==
literal|null
condition|)
block|{
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
name|Table
name|tTable
init|=
name|table
operator|.
name|getTTable
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|tTable
operator|.
name|getParameters
argument_list|()
decl_stmt|;
try|try
block|{
name|Path
name|dir
init|=
operator|new
name|Path
argument_list|(
name|tTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|numRows
init|=
literal|0
decl_stmt|;
name|long
name|rawDataSize
init|=
literal|0
decl_stmt|;
name|long
name|fileSize
init|=
literal|0
decl_stmt|;
name|long
name|numFiles
init|=
literal|0
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|fileList
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|listLocatedStatus
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|hiddenFileFilter
argument_list|)
decl_stmt|;
name|boolean
name|statsAvailable
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fileList
control|)
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|inputFormat
init|=
operator|(
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|table
operator|.
name|getInputFormatClass
argument_list|()
argument_list|,
name|jc
argument_list|)
decl_stmt|;
name|InputSplit
name|dummySplit
init|=
operator|new
name|FileSplit
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|new
name|String
index|[]
block|{
name|table
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|recordReader
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|dummySplit
argument_list|,
name|jc
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
decl_stmt|;
name|StatsProvidingRecordReader
name|statsRR
decl_stmt|;
if|if
condition|(
name|recordReader
operator|instanceof
name|StatsProvidingRecordReader
condition|)
block|{
name|statsRR
operator|=
operator|(
name|StatsProvidingRecordReader
operator|)
name|recordReader
expr_stmt|;
name|numRows
operator|+=
name|statsRR
operator|.
name|getStats
argument_list|()
operator|.
name|getRowCount
argument_list|()
expr_stmt|;
name|rawDataSize
operator|+=
name|statsRR
operator|.
name|getStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
expr_stmt|;
name|fileSize
operator|+=
name|file
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|numFiles
operator|+=
literal|1
expr_stmt|;
name|statsAvailable
operator|=
literal|true
expr_stmt|;
block|}
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|statsAvailable
condition|)
block|{
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numRows
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|rawDataSize
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|TOTAL_SIZE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|fileSize
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|NUM_FILES
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|numFiles
argument_list|)
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_GENERATED_VIA_STATS_TASK
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterTable
argument_list|(
name|tableFullName
argument_list|,
operator|new
name|Table
argument_list|(
name|tTable
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"Table "
operator|+
name|tableFullName
operator|+
literal|" stats: ["
operator|+
name|toString
argument_list|(
name|parameters
argument_list|)
operator|+
literal|']'
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|msg
init|=
literal|"Table "
operator|+
name|tableFullName
operator|+
literal|" does not provide stats."
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
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
literal|"[Warning] could not update stats for "
operator|+
name|tableFullName
operator|+
literal|"."
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
block|}
block|}
else|else
block|{
comment|// Partitioned table
for|for
control|(
name|Partition
name|partn
range|:
name|partitions
control|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|StatsCollection
argument_list|(
name|partn
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stats collection waiting for threadpool to shutdown.."
argument_list|)
expr_stmt|;
name|shutdownAndAwaitTermination
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stats collection threadpool shutdown successful."
argument_list|)
expr_stmt|;
name|ret
operator|=
name|updatePartitions
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
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
operator|-
literal|1
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
name|int
name|updatePartitions
parameter_list|()
throws|throws
name|InvalidOperationException
throws|,
name|HiveException
block|{
if|if
condition|(
operator|!
name|partUpdates
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|updatedParts
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|partUpdates
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|updatedParts
operator|.
name|contains
argument_list|(
literal|null
argument_list|)
operator|&&
name|work
operator|.
name|isStatsReliable
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stats requested to be reliable. Empty stats found and hence failing the task."
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Bulk updating partitions.."
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterPartitions
argument_list|(
name|tableFullName
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|partUpdates
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Bulk updated "
operator|+
name|partUpdates
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" partitions."
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|shutdownAndAwaitTermination
parameter_list|(
name|ExecutorService
name|threadPool
parameter_list|)
block|{
comment|// Disable new tasks from being submitted
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Wait a while for existing tasks to terminate
if|if
condition|(
operator|!
name|threadPool
operator|.
name|awaitTermination
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
comment|// Cancel currently executing tasks
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
comment|// Wait a while for tasks to respond to being cancelled
if|if
condition|(
operator|!
name|threadPool
operator|.
name|awaitTermination
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stats collection thread pool did not terminate"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// Cancel again if current thread also interrupted
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
comment|// Preserve interrupt status
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|PathFilter
name|hiddenFileFilter
init|=
operator|new
name|PathFilter
argument_list|()
block|{
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
decl_stmt|;
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
specifier|private
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsList
parameter_list|()
throws|throws
name|HiveException
block|{
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
else|else
block|{
return|return
name|tblSpec
operator|.
name|partitions
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

