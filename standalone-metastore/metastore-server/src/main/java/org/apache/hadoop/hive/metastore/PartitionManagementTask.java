begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashSet
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
name|concurrent
operator|.
name|CountDownLatch
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
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
name|conf
operator|.
name|Configuration
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
name|repl
operator|.
name|ReplConst
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
name|metastore
operator|.
name|api
operator|.
name|TableMeta
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|TimeValidator
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
name|annotations
operator|.
name|VisibleForTesting
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
comment|/**  * Partition management task is primarily responsible for partition retention and discovery based on table properties.  *  * Partition Retention - If "partition.retention.period" table property is set with retention interval, when this  * metastore task runs periodically, it will drop partitions with age (creation time) greater than retention period.  * Dropping partitions after retention period will also delete the data in that partition.  *  * Partition Discovery - If "discover.partitions" table property is set, this metastore task monitors table location  * for newly added partition directories and create partition objects if it does not exist. Also, if partition object  * exist and if corresponding directory does not exists under table location then the partition object will be dropped.  *  */
end_comment

begin_class
specifier|public
class|class
name|PartitionManagementTask
implements|implements
name|MetastoreTaskThread
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PartitionManagementTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DISCOVER_PARTITIONS_TBLPROPERTY
init|=
literal|"discover.partitions"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PARTITION_RETENTION_PERIOD_TBLPROPERTY
init|=
literal|"partition.retention.period"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
comment|// these are just for testing
specifier|private
specifier|static
name|int
name|completedAttempts
decl_stmt|;
specifier|private
specifier|static
name|int
name|skippedAttempts
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|long
name|runFrequency
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_TASK_FREQUENCY
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
comment|// we modify conf in setupConf(), so we make a copy
name|conf
operator|=
operator|new
name|Configuration
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|private
specifier|static
name|boolean
name|partitionDiscoveryEnabled
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
return|return
name|params
operator|!=
literal|null
operator|&&
name|params
operator|.
name|containsKey
argument_list|(
name|DISCOVER_PARTITIONS_TBLPROPERTY
argument_list|)
operator|&&
name|params
operator|.
name|get
argument_list|(
name|DISCOVER_PARTITIONS_TBLPROPERTY
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|tblBeingReplicatedInto
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
return|return
name|params
operator|!=
literal|null
operator|&&
name|params
operator|.
name|containsKey
argument_list|(
name|ReplConst
operator|.
name|REPL_TARGET_TABLE_PROPERTY
argument_list|)
operator|&&
operator|!
name|params
operator|.
name|get
argument_list|(
name|ReplConst
operator|.
name|REPL_TARGET_TABLE_PROPERTY
argument_list|)
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
name|skippedAttempts
operator|=
literal|0
expr_stmt|;
name|String
name|qualifiedTableName
init|=
literal|null
decl_stmt|;
name|IMetaStoreClient
name|msc
init|=
literal|null
decl_stmt|;
try|try
block|{
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Table
argument_list|>
name|candidateTables
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|catalogName
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_CATALOG_NAME
argument_list|)
decl_stmt|;
name|String
name|dbPattern
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_DATABASE_PATTERN
argument_list|)
decl_stmt|;
name|String
name|tablePattern
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_TABLE_PATTERN
argument_list|)
decl_stmt|;
name|String
name|tableTypes
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_TABLE_TYPES
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|tableTypesSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tableTypesList
decl_stmt|;
comment|// if tableTypes is empty, then a list with single empty string has to specified to scan no tables.
comment|// specifying empty here is equivalent to disabling the partition discovery altogether as it scans no tables.
if|if
condition|(
name|tableTypes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableTypesList
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|String
name|type
range|:
name|tableTypes
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
try|try
block|{
name|tableTypesSet
operator|.
name|add
argument_list|(
name|TableType
operator|.
name|valueOf
argument_list|(
name|type
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// ignore
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unknown table type: {}"
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
block|}
name|tableTypesList
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|tableTypesSet
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|TableMeta
argument_list|>
name|foundTableMetas
init|=
name|msc
operator|.
name|getTableMeta
argument_list|(
name|catalogName
argument_list|,
name|dbPattern
argument_list|,
name|tablePattern
argument_list|,
name|tableTypesList
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Looking for tables using catalog: {} dbPattern: {} tablePattern: {} found: {}"
argument_list|,
name|catalogName
argument_list|,
name|dbPattern
argument_list|,
name|tablePattern
argument_list|,
name|foundTableMetas
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TableMeta
name|tableMeta
range|:
name|foundTableMetas
control|)
block|{
name|Table
name|table
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|tableMeta
operator|.
name|getCatName
argument_list|()
argument_list|,
name|tableMeta
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tableMeta
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionDiscoveryEnabled
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|&&
operator|!
name|tblBeingReplicatedInto
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
name|candidateTables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|candidateTables
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// TODO: Msck creates MetastoreClient (MSC) on its own. MSC creation is expensive. Sharing MSC also
comment|// will not be safe unless synchronized MSC is used. Using synchronized MSC in multi-threaded context also
comment|// defeats the purpose of thread pooled msck repair.
name|int
name|threadPoolSize
init|=
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_MANAGEMENT_TASK_THREAD_POOL_SIZE
argument_list|)
decl_stmt|;
specifier|final
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|candidateTables
operator|.
name|size
argument_list|()
argument_list|,
name|threadPoolSize
argument_list|)
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
literal|"PartitionDiscoveryTask-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|CountDownLatch
name|countDownLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|candidateTables
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Found {} candidate tables for partition discovery"
argument_list|,
name|candidateTables
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|setupMsckConf
argument_list|()
expr_stmt|;
for|for
control|(
name|Table
name|table
range|:
name|candidateTables
control|)
block|{
name|qualifiedTableName
operator|=
name|Warehouse
operator|.
name|getCatalogQualifiedTableName
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|long
name|retentionSeconds
init|=
name|getRetentionPeriodInSeconds
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running partition discovery for table {} retentionPeriod: {}s"
argument_list|,
name|qualifiedTableName
argument_list|,
name|retentionSeconds
argument_list|)
expr_stmt|;
comment|// this always runs in 'sync' mode where partitions can be added and dropped
name|MsckInfo
name|msckInfo
init|=
operator|new
name|MsckInfo
argument_list|(
name|table
operator|.
name|getCatName
argument_list|()
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|retentionSeconds
argument_list|)
decl_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|MsckThread
argument_list|(
name|msckInfo
argument_list|,
name|conf
argument_list|,
name|qualifiedTableName
argument_list|,
name|countDownLatch
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|countDownLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception while running partition discovery task for table: "
operator|+
name|qualifiedTableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|msc
operator|!=
literal|null
condition|)
block|{
name|msc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|completedAttempts
operator|++
expr_stmt|;
block|}
else|else
block|{
name|skippedAttempts
operator|++
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Lock is held by some other partition discovery task. Skipping this attempt..#{}"
argument_list|,
name|skippedAttempts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|long
name|getRetentionPeriodInSeconds
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
block|{
name|String
name|retentionPeriod
decl_stmt|;
name|long
name|retentionSeconds
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getParameters
argument_list|()
operator|!=
literal|null
operator|&&
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|containsKey
argument_list|(
name|PARTITION_RETENTION_PERIOD_TBLPROPERTY
argument_list|)
condition|)
block|{
name|retentionPeriod
operator|=
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|PARTITION_RETENTION_PERIOD_TBLPROPERTY
argument_list|)
expr_stmt|;
if|if
condition|(
name|retentionPeriod
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"'{}' table property is defined but empty. Skipping retention period.."
argument_list|,
name|PARTITION_RETENTION_PERIOD_TBLPROPERTY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|TimeValidator
name|timeValidator
init|=
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|timeValidator
operator|.
name|validate
argument_list|(
name|retentionPeriod
argument_list|)
expr_stmt|;
name|retentionSeconds
operator|=
name|MetastoreConf
operator|.
name|convertTimeStr
argument_list|(
name|retentionPeriod
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"'{}' retentionPeriod value is invalid. Skipping retention period.."
argument_list|,
name|retentionPeriod
argument_list|)
expr_stmt|;
comment|// will return -1
block|}
block|}
block|}
return|return
name|retentionSeconds
return|;
block|}
specifier|private
name|void
name|setupMsckConf
parameter_list|()
block|{
comment|// if invalid partition directory appears, we just skip and move on. We don't want partition management to throw
comment|// when invalid path is encountered as these are background threads. We just want to skip and move on. Users will
comment|// have to fix the invalid paths via external means.
name|conf
operator|.
name|set
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|MSCK_PATH_VALIDATION
operator|.
name|getVarname
argument_list|()
argument_list|,
literal|"skip"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|MsckThread
implements|implements
name|Runnable
block|{
specifier|private
name|MsckInfo
name|msckInfo
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|qualifiedTableName
decl_stmt|;
specifier|private
name|CountDownLatch
name|countDownLatch
decl_stmt|;
name|MsckThread
parameter_list|(
name|MsckInfo
name|msckInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|qualifiedTableName
parameter_list|,
name|CountDownLatch
name|countDownLatch
parameter_list|)
block|{
name|this
operator|.
name|msckInfo
operator|=
name|msckInfo
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|qualifiedTableName
operator|=
name|qualifiedTableName
expr_stmt|;
name|this
operator|.
name|countDownLatch
operator|=
name|countDownLatch
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Msck
name|msck
init|=
operator|new
name|Msck
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|msck
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|msck
operator|.
name|repair
argument_list|(
name|msckInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception while running partition discovery task for table: "
operator|+
name|qualifiedTableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// there is no recovery from exception, so we always count down and retry in next attempt
name|countDownLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|int
name|getSkippedAttempts
parameter_list|()
block|{
return|return
name|skippedAttempts
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|int
name|getCompletedAttempts
parameter_list|()
block|{
return|return
name|completedAttempts
return|;
block|}
block|}
end_class

end_unit

