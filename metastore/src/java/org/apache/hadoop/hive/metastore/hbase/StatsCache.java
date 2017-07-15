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
name|hbase
package|;
end_package

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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|HiveStatsUtils
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
name|metastore
operator|.
name|api
operator|.
name|AggrStats
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
name|ColumnStatistics
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
name|hbase
operator|.
name|stats
operator|.
name|ColumnStatsAggregator
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
name|hbase
operator|.
name|stats
operator|.
name|ColumnStatsAggregatorFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|concurrent
operator|.
name|ExecutionException
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

begin_comment
comment|/**  * A cache for stats.  This is only intended for use by  * {@link org.apache.hadoop.hive.metastore.hbase.HBaseReadWrite} and should not be used outside  * that class.  */
end_comment

begin_class
class|class
name|StatsCache
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
name|StatsCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|StatsCache
name|self
init|=
literal|null
decl_stmt|;
specifier|private
name|LoadingCache
argument_list|<
name|StatsCacheKey
argument_list|,
name|AggrStats
argument_list|>
name|cache
decl_stmt|;
specifier|private
name|Invalidator
name|invalidator
decl_stmt|;
specifier|private
name|long
name|runInvalidatorEvery
decl_stmt|;
specifier|private
name|long
name|maxTimeInCache
decl_stmt|;
specifier|private
name|boolean
name|invalidatorHasRun
decl_stmt|;
annotation|@
name|VisibleForTesting
name|Counter
name|misses
decl_stmt|;
annotation|@
name|VisibleForTesting
name|Counter
name|hbaseHits
decl_stmt|;
annotation|@
name|VisibleForTesting
name|Counter
name|totalGets
decl_stmt|;
specifier|static
specifier|synchronized
name|StatsCache
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|self
operator|==
literal|null
condition|)
block|{
name|self
operator|=
operator|new
name|StatsCache
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|self
return|;
block|}
specifier|private
name|StatsCache
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
specifier|final
name|StatsCache
name|me
init|=
name|this
decl_stmt|;
name|cache
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|maximumSize
argument_list|(
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_AGGR_STATS_CACHE_ENTRIES
argument_list|)
argument_list|)
operator|.
name|expireAfterWrite
argument_list|(
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_AGGR_STATS_MEMORY_TTL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|StatsCacheKey
argument_list|,
name|AggrStats
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|AggrStats
name|load
parameter_list|(
name|StatsCacheKey
name|key
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|useDensityFunctionForNDVEstimation
init|=
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
name|HIVE_METASTORE_STATS_NDV_DENSITY_FUNCTION
argument_list|)
decl_stmt|;
name|HBaseReadWrite
name|hrw
init|=
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|AggrStats
name|aggrStats
init|=
name|hrw
operator|.
name|getAggregatedStats
argument_list|(
name|key
operator|.
name|hashed
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggrStats
operator|==
literal|null
condition|)
block|{
name|misses
operator|.
name|incr
argument_list|()
expr_stmt|;
name|ColumnStatsAggregator
name|aggregator
init|=
literal|null
decl_stmt|;
name|aggrStats
operator|=
operator|new
name|AggrStats
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unable to find aggregated stats for "
operator|+
name|key
operator|.
name|colName
operator|+
literal|", aggregating"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|css
init|=
name|hrw
operator|.
name|getPartitionStatistics
argument_list|(
name|key
operator|.
name|dbName
argument_list|,
name|key
operator|.
name|tableName
argument_list|,
name|key
operator|.
name|partNames
argument_list|,
name|HBaseStore
operator|.
name|partNameListToValsList
argument_list|(
name|key
operator|.
name|partNames
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|key
operator|.
name|colName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|css
operator|!=
literal|null
operator|&&
name|css
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|aggrStats
operator|.
name|setPartsFound
argument_list|(
name|css
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|aggregator
operator|==
literal|null
condition|)
block|{
name|aggregator
operator|=
name|ColumnStatsAggregatorFactory
operator|.
name|getColumnStatsAggregator
argument_list|(
name|css
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getStatsObj
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getStatsData
argument_list|()
operator|.
name|getSetField
argument_list|()
argument_list|,
name|useDensityFunctionForNDVEstimation
argument_list|)
expr_stmt|;
block|}
name|ColumnStatisticsObj
name|statsObj
init|=
name|aggregator
operator|.
name|aggregate
argument_list|(
name|key
operator|.
name|colName
argument_list|,
name|key
operator|.
name|partNames
argument_list|,
name|css
argument_list|)
decl_stmt|;
name|aggrStats
operator|.
name|addToColStats
argument_list|(
name|statsObj
argument_list|)
expr_stmt|;
name|me
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|aggrStats
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|hbaseHits
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
return|return
name|aggrStats
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|misses
operator|=
operator|new
name|Counter
argument_list|(
literal|"Stats cache table misses"
argument_list|)
expr_stmt|;
name|hbaseHits
operator|=
operator|new
name|Counter
argument_list|(
literal|"Stats cache table hits"
argument_list|)
expr_stmt|;
name|totalGets
operator|=
operator|new
name|Counter
argument_list|(
literal|"Total get calls to the stats cache"
argument_list|)
expr_stmt|;
name|maxTimeInCache
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_AGGR_STATS_HBASE_TTL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// We want runEvery in milliseconds, even though we give the default value in the conf in
comment|// seconds.
name|runInvalidatorEvery
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_AGGR_STATS_INVALIDATOR_FREQUENCY
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|invalidator
operator|=
operator|new
name|Invalidator
argument_list|()
expr_stmt|;
name|invalidator
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|invalidator
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add an object to the cache.    * @param key Key for this entry    * @param aggrStats stats    * @throws java.io.IOException    */
name|void
name|put
parameter_list|(
name|StatsCacheKey
name|key
parameter_list|,
name|AggrStats
name|aggrStats
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|()
operator|.
name|putAggregatedStats
argument_list|(
name|key
operator|.
name|hashed
argument_list|,
name|key
operator|.
name|dbName
argument_list|,
name|key
operator|.
name|tableName
argument_list|,
name|key
operator|.
name|partNames
argument_list|,
name|key
operator|.
name|colName
argument_list|,
name|aggrStats
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|aggrStats
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get partition level statistics    * @param dbName name of database table is in    * @param tableName name of table    * @param partNames names of the partitions    * @param colName of column to get stats for    * @return stats object for this column, or null if none cached    * @throws java.io.IOException    */
name|AggrStats
name|get
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|String
name|colName
parameter_list|)
throws|throws
name|IOException
block|{
name|totalGets
operator|.
name|incr
argument_list|()
expr_stmt|;
name|StatsCacheKey
name|key
init|=
operator|new
name|StatsCacheKey
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partNames
argument_list|,
name|colName
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Remove all entries that are related to a particular set of partitions.  This should be    * called when partitions are deleted or stats are updated.    * @param dbName name of database table is in    * @param tableName name of table    * @param partName name of the partition    * @throws IOException    */
name|void
name|invalidate
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|)
throws|throws
name|IOException
block|{
name|invalidator
operator|.
name|addToQueue
argument_list|(
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
operator|.
name|Entry
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|dbName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setTableName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setPartName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|partName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|void
name|dumpCounters
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|misses
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|hbaseHits
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|totalGets
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Completely dump the cache from memory, used to test that we can access stats from HBase itself.    * @throws IOException    */
annotation|@
name|VisibleForTesting
name|void
name|flushMemory
parameter_list|()
throws|throws
name|IOException
block|{
name|cache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|resetCounters
parameter_list|()
block|{
name|misses
operator|.
name|clear
argument_list|()
expr_stmt|;
name|hbaseHits
operator|.
name|clear
argument_list|()
expr_stmt|;
name|totalGets
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setRunInvalidatorEvery
parameter_list|(
name|long
name|runEvery
parameter_list|)
block|{
name|runInvalidatorEvery
operator|=
name|runEvery
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setMaxTimeInCache
parameter_list|(
name|long
name|maxTime
parameter_list|)
block|{
name|maxTimeInCache
operator|=
name|maxTime
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|wakeInvalidator
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|invalidatorHasRun
operator|=
literal|false
expr_stmt|;
comment|// Wait through 2 cycles so we're sure our entry won't be picked as too new.
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
name|runInvalidatorEvery
argument_list|)
expr_stmt|;
name|invalidator
operator|.
name|interrupt
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|invalidatorHasRun
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|StatsCacheKey
block|{
specifier|final
name|byte
index|[]
name|hashed
decl_stmt|;
name|String
name|dbName
decl_stmt|;
name|String
name|tableName
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partNames
decl_stmt|;
name|String
name|colName
decl_stmt|;
specifier|private
name|MessageDigest
name|md
decl_stmt|;
name|StatsCacheKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|hashed
operator|=
name|key
expr_stmt|;
block|}
name|StatsCacheKey
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|String
name|colName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partNames
operator|=
name|partNames
expr_stmt|;
name|this
operator|.
name|colName
operator|=
name|colName
expr_stmt|;
try|try
block|{
name|md
operator|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|md
operator|.
name|update
argument_list|(
name|dbName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|this
operator|.
name|partNames
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|partNames
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|s
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|update
argument_list|(
name|colName
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|hashed
operator|=
name|md
operator|.
name|digest
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
operator|||
operator|!
operator|(
name|other
operator|instanceof
name|StatsCacheKey
operator|)
condition|)
return|return
literal|false
return|;
name|StatsCacheKey
name|that
init|=
operator|(
name|StatsCacheKey
operator|)
name|other
decl_stmt|;
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|hashed
argument_list|,
name|that
operator|.
name|hashed
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|hashCode
argument_list|(
name|hashed
argument_list|)
return|;
block|}
block|}
specifier|private
class|class
name|Invalidator
extends|extends
name|Thread
block|{
specifier|private
name|List
argument_list|<
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
operator|.
name|Entry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
name|void
name|addToQueue
parameter_list|(
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
operator|.
name|Entry
name|entry
parameter_list|)
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|entries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|startedAt
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
operator|.
name|Entry
argument_list|>
name|thisRun
init|=
literal|null
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|entries
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|thisRun
operator|=
name|entries
expr_stmt|;
name|entries
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|thisRun
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
name|filter
init|=
name|HbaseMetastoreProto
operator|.
name|AggrStatsInvalidatorFilter
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRunEvery
argument_list|(
name|runInvalidatorEvery
argument_list|)
operator|.
name|setMaxCacheEntryLife
argument_list|(
name|maxTimeInCache
argument_list|)
operator|.
name|addAllToInvalidate
argument_list|(
name|thisRun
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StatsCacheKey
argument_list|>
name|keys
init|=
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|()
operator|.
name|invalidateAggregatedStats
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|cache
operator|.
name|invalidateAll
argument_list|(
name|keys
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Not a lot I can do here
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught error while invalidating entries in the cache"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|invalidatorHasRun
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|sleep
argument_list|(
name|runInvalidatorEvery
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startedAt
operator|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interupted while sleeping"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

