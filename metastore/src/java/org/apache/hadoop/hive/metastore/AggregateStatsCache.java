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
package|;
end_package

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
name|ColumnStatisticsObj
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|BloomFilter
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|ConcurrentHashMap
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
name|atomic
operator|.
name|AtomicInteger
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
name|atomic
operator|.
name|AtomicLong
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
name|ReadWriteLock
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
name|ReentrantReadWriteLock
import|;
end_import

begin_class
specifier|public
class|class
name|AggregateStatsCache
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
name|AggregateStatsCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AggregateStatsCache
name|self
init|=
literal|null
decl_stmt|;
comment|// Backing store for this cache
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Key
argument_list|,
name|AggrColStatsList
argument_list|>
name|cacheStore
decl_stmt|;
comment|// Cache size
specifier|private
specifier|final
name|int
name|maxCacheNodes
decl_stmt|;
comment|// Current nodes in the cache
specifier|private
specifier|final
name|AtomicInteger
name|currentNodes
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Run the cleaner thread when the cache is maxFull% full
specifier|private
specifier|final
name|float
name|maxFull
decl_stmt|;
comment|// Run the cleaner thread until cache is cleanUntil% occupied
specifier|private
specifier|final
name|float
name|cleanUntil
decl_stmt|;
comment|// Nodes go stale after this
specifier|private
specifier|final
name|long
name|timeToLiveMs
decl_stmt|;
comment|// Max time when waiting for write locks on node list
specifier|private
specifier|final
name|long
name|maxWriterWaitTime
decl_stmt|;
comment|// Max time when waiting for read locks on node list
specifier|private
specifier|final
name|long
name|maxReaderWaitTime
decl_stmt|;
comment|// Maximum number of paritions aggregated per cache node
specifier|private
specifier|final
name|int
name|maxPartsPerCacheNode
decl_stmt|;
comment|// Bloom filter false positive probability
specifier|private
specifier|final
name|float
name|falsePositiveProbability
decl_stmt|;
comment|// Max tolerable variance for matches
specifier|private
specifier|final
name|float
name|maxVariance
decl_stmt|;
comment|// Used to determine if cleaner thread is already running
specifier|private
name|boolean
name|isCleaning
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|cacheHits
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|cacheMisses
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// To track cleaner metrics
name|int
name|numRemovedTTL
init|=
literal|0
decl_stmt|,
name|numRemovedLRU
init|=
literal|0
decl_stmt|;
specifier|private
name|AggregateStatsCache
parameter_list|(
name|int
name|maxCacheNodes
parameter_list|,
name|int
name|maxPartsPerCacheNode
parameter_list|,
name|long
name|timeToLiveMs
parameter_list|,
name|float
name|falsePositiveProbability
parameter_list|,
name|float
name|maxVariance
parameter_list|,
name|long
name|maxWriterWaitTime
parameter_list|,
name|long
name|maxReaderWaitTime
parameter_list|,
name|float
name|maxFull
parameter_list|,
name|float
name|cleanUntil
parameter_list|)
block|{
name|this
operator|.
name|maxCacheNodes
operator|=
name|maxCacheNodes
expr_stmt|;
name|this
operator|.
name|maxPartsPerCacheNode
operator|=
name|maxPartsPerCacheNode
expr_stmt|;
name|this
operator|.
name|timeToLiveMs
operator|=
name|timeToLiveMs
expr_stmt|;
name|this
operator|.
name|falsePositiveProbability
operator|=
name|falsePositiveProbability
expr_stmt|;
name|this
operator|.
name|maxVariance
operator|=
name|maxVariance
expr_stmt|;
name|this
operator|.
name|maxWriterWaitTime
operator|=
name|maxWriterWaitTime
expr_stmt|;
name|this
operator|.
name|maxReaderWaitTime
operator|=
name|maxReaderWaitTime
expr_stmt|;
name|this
operator|.
name|maxFull
operator|=
name|maxFull
expr_stmt|;
name|this
operator|.
name|cleanUntil
operator|=
name|cleanUntil
expr_stmt|;
name|this
operator|.
name|cacheStore
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Key
argument_list|,
name|AggrColStatsList
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|AggregateStatsCache
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
name|int
name|maxCacheNodes
init|=
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
name|METASTORE_AGGREGATE_STATS_CACHE_SIZE
argument_list|)
decl_stmt|;
comment|// The number of partitions aggregated per cache node
comment|// If the number of partitions requested is> this value, we'll fetch directly from Metastore
name|int
name|maxPartitionsPerCacheNode
init|=
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
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_PARTITIONS
argument_list|)
decl_stmt|;
name|long
name|timeToLiveMs
init|=
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
name|METASTORE_AGGREGATE_STATS_CACHE_TTL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|*
literal|1000
decl_stmt|;
comment|// False positives probability we are ready to tolerate for the underlying bloom filter
name|float
name|falsePositiveProbability
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_FPP
argument_list|)
decl_stmt|;
comment|// Maximum tolerable variance in number of partitions between cached node and our request
name|float
name|maxVariance
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_VARIANCE
argument_list|)
decl_stmt|;
name|long
name|maxWriterWaitTime
init|=
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
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_WRITER_WAIT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|long
name|maxReaderWaitTime
init|=
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
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_READER_WAIT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|float
name|maxFull
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_FULL
argument_list|)
decl_stmt|;
name|float
name|cleanUntil
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_CLEAN_UNTIL
argument_list|)
decl_stmt|;
name|self
operator|=
operator|new
name|AggregateStatsCache
argument_list|(
name|maxCacheNodes
argument_list|,
name|maxPartitionsPerCacheNode
argument_list|,
name|timeToLiveMs
argument_list|,
name|falsePositiveProbability
argument_list|,
name|maxVariance
argument_list|,
name|maxWriterWaitTime
argument_list|,
name|maxReaderWaitTime
argument_list|,
name|maxFull
argument_list|,
name|cleanUntil
argument_list|)
expr_stmt|;
block|}
return|return
name|self
return|;
block|}
specifier|public
name|int
name|getMaxCacheNodes
parameter_list|()
block|{
return|return
name|maxCacheNodes
return|;
block|}
specifier|public
name|int
name|getCurrentNodes
parameter_list|()
block|{
return|return
name|currentNodes
operator|.
name|intValue
argument_list|()
return|;
block|}
specifier|public
name|float
name|getFullPercent
parameter_list|()
block|{
return|return
operator|(
name|currentNodes
operator|.
name|intValue
argument_list|()
operator|/
operator|(
name|float
operator|)
name|maxCacheNodes
operator|)
operator|*
literal|100
return|;
block|}
specifier|public
name|int
name|getMaxPartsPerCacheNode
parameter_list|()
block|{
return|return
name|maxPartsPerCacheNode
return|;
block|}
specifier|public
name|float
name|getFalsePositiveProbability
parameter_list|()
block|{
return|return
name|falsePositiveProbability
return|;
block|}
specifier|public
name|Float
name|getHitRatio
parameter_list|()
block|{
if|if
condition|(
name|cacheHits
operator|.
name|longValue
argument_list|()
operator|+
name|cacheMisses
operator|.
name|longValue
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
call|(
name|float
call|)
argument_list|(
name|cacheHits
operator|.
name|longValue
argument_list|()
argument_list|)
operator|/
operator|(
name|cacheHits
operator|.
name|longValue
argument_list|()
operator|+
name|cacheMisses
operator|.
name|longValue
argument_list|()
operator|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return aggregate stats for a column from the cache or null.    * While reading from the nodelist for a key, we wait maxReaderWaitTime to acquire the lock,    * failing which we return a cache miss (i.e. null)    *    * @param dbName    * @param tblName    * @param colName    * @param partNames    * @return    */
specifier|public
name|AggrColStats
name|get
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|colName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
block|{
comment|// Cache key
name|Key
name|key
init|=
operator|new
name|Key
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|)
decl_stmt|;
name|AggrColStatsList
name|candidateList
init|=
name|cacheStore
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// No key, or no nodes in candidate list
if|if
condition|(
operator|(
name|candidateList
operator|==
literal|null
operator|)
operator|||
operator|(
name|candidateList
operator|.
name|nodes
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No aggregate stats cached for "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// Find the value object
comment|// Update the timestamp of the key,value if value matches the criteria
comment|// Return the value
name|AggrColStats
name|match
init|=
literal|null
decl_stmt|;
name|boolean
name|isLocked
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Try to readlock the candidateList; timeout after maxReaderWaitTime
name|isLocked
operator|=
name|candidateList
operator|.
name|readLock
operator|.
name|tryLock
argument_list|(
name|maxReaderWaitTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocked
condition|)
block|{
name|match
operator|=
name|findBestMatch
argument_list|(
name|partNames
argument_list|,
name|candidateList
operator|.
name|nodes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|match
operator|!=
literal|null
condition|)
block|{
comment|// Ok to not lock the list for this and use a volatile lastAccessTime instead
name|candidateList
operator|.
name|updateLastAccessTime
argument_list|()
expr_stmt|;
name|cacheHits
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Returning aggregate stats from the cache; total hits: "
operator|+
name|cacheHits
operator|.
name|longValue
argument_list|()
operator|+
literal|", total misses: "
operator|+
name|cacheMisses
operator|.
name|longValue
argument_list|()
operator|+
literal|", hit ratio: "
operator|+
name|getHitRatio
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cacheMisses
operator|.
name|incrementAndGet
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
literal|"Interrupted Exception ignored "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|isLocked
condition|)
block|{
name|candidateList
operator|.
name|readLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|match
return|;
block|}
comment|/**    * Find the best match using the configurable error tolerance and time to live value    *    * @param partNames    * @param candidates    * @return best matched node or null    */
specifier|private
name|AggrColStats
name|findBestMatch
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|List
argument_list|<
name|AggrColStats
argument_list|>
name|candidates
parameter_list|)
block|{
comment|// Hits, misses tracked for a candidate node
name|MatchStats
name|matchStats
decl_stmt|;
comment|// MatchStats for each candidate
name|Map
argument_list|<
name|AggrColStats
argument_list|,
name|MatchStats
argument_list|>
name|candidateMatchStats
init|=
operator|new
name|HashMap
argument_list|<
name|AggrColStats
argument_list|,
name|MatchStats
argument_list|>
argument_list|()
decl_stmt|;
comment|// The final match we intend to return
name|AggrColStats
name|bestMatch
init|=
literal|null
decl_stmt|;
comment|// To compare among potentially multiple matches
name|int
name|bestMatchHits
init|=
literal|0
decl_stmt|;
name|int
name|numPartsRequested
init|=
name|partNames
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// 1st pass at marking invalid candidates
comment|// Checks based on variance and TTL
comment|// Note: we're not creating a copy of the list for saving memory
for|for
control|(
name|AggrColStats
name|candidate
range|:
name|candidates
control|)
block|{
comment|// Variance check
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|candidate
operator|.
name|getNumPartsCached
argument_list|()
operator|-
name|numPartsRequested
operator|)
operator|/
name|numPartsRequested
argument_list|)
operator|>
name|maxVariance
condition|)
block|{
continue|continue;
block|}
comment|// TTL check
if|if
condition|(
name|isExpired
argument_list|(
name|candidate
argument_list|)
condition|)
block|{
continue|continue;
block|}
else|else
block|{
name|candidateMatchStats
operator|.
name|put
argument_list|(
name|candidate
argument_list|,
operator|new
name|MatchStats
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// We'll count misses as we iterate
name|int
name|maxMisses
init|=
operator|(
name|int
operator|)
name|maxVariance
operator|*
name|numPartsRequested
decl_stmt|;
for|for
control|(
name|String
name|partName
range|:
name|partNames
control|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|AggrColStats
argument_list|,
name|MatchStats
argument_list|>
argument_list|>
name|iterator
init|=
name|candidateMatchStats
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|AggrColStats
argument_list|,
name|MatchStats
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|AggrColStats
name|candidate
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|matchStats
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|candidate
operator|.
name|getBloomFilter
argument_list|()
operator|.
name|test
argument_list|(
name|partName
operator|.
name|getBytes
argument_list|()
argument_list|)
condition|)
block|{
operator|++
name|matchStats
operator|.
name|hits
expr_stmt|;
block|}
else|else
block|{
operator|++
name|matchStats
operator|.
name|misses
expr_stmt|;
block|}
comment|// 2nd pass at removing invalid candidates
comment|// If misses so far exceed max tolerable misses
if|if
condition|(
name|matchStats
operator|.
name|misses
operator|>
name|maxMisses
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
continue|continue;
block|}
comment|// Check if this is the best match so far
if|if
condition|(
name|matchStats
operator|.
name|hits
operator|>
name|bestMatchHits
condition|)
block|{
name|bestMatch
operator|=
name|candidate
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|bestMatch
operator|!=
literal|null
condition|)
block|{
comment|// Update the last access time for this node
name|bestMatch
operator|.
name|updateLastAccessTime
argument_list|()
expr_stmt|;
block|}
return|return
name|bestMatch
return|;
block|}
comment|/**    * Add a new node to the cache; may trigger the cleaner thread if the cache is near full capacity.    * We'll however add the node even if we temporaily exceed maxCacheNodes, because the cleaner    * will eventually create space from expired nodes or by removing LRU nodes.    *    * @param dbName    * @param tblName    * @param colName    * @param numPartsCached    * @param colStats    * @param bloomFilter    */
comment|// TODO: make add asynchronous: add shouldn't block the higher level calls
specifier|public
name|void
name|add
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|colName
parameter_list|,
name|long
name|numPartsCached
parameter_list|,
name|ColumnStatisticsObj
name|colStats
parameter_list|,
name|BloomFilter
name|bloomFilter
parameter_list|)
block|{
comment|// If we have no space in the cache, run cleaner thread
if|if
condition|(
name|getCurrentNodes
argument_list|()
operator|/
name|maxCacheNodes
operator|>
name|maxFull
condition|)
block|{
name|spawnCleaner
argument_list|()
expr_stmt|;
block|}
comment|// Cache key
name|Key
name|key
init|=
operator|new
name|Key
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|)
decl_stmt|;
comment|// Add new node to the cache
name|AggrColStats
name|node
init|=
operator|new
name|AggrColStats
argument_list|(
name|numPartsCached
argument_list|,
name|bloomFilter
argument_list|,
name|colStats
argument_list|)
decl_stmt|;
name|AggrColStatsList
name|nodeList
decl_stmt|;
name|AggrColStatsList
name|newNodeList
init|=
operator|new
name|AggrColStatsList
argument_list|()
decl_stmt|;
name|newNodeList
operator|.
name|nodes
operator|=
operator|new
name|ArrayList
argument_list|<
name|AggrColStats
argument_list|>
argument_list|()
expr_stmt|;
name|nodeList
operator|=
name|cacheStore
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|newNodeList
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeList
operator|==
literal|null
condition|)
block|{
name|nodeList
operator|=
name|newNodeList
expr_stmt|;
block|}
name|boolean
name|isLocked
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isLocked
operator|=
name|nodeList
operator|.
name|writeLock
operator|.
name|tryLock
argument_list|(
name|maxWriterWaitTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocked
condition|)
block|{
name|nodeList
operator|.
name|nodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|node
operator|.
name|updateLastAccessTime
argument_list|()
expr_stmt|;
name|nodeList
operator|.
name|updateLastAccessTime
argument_list|()
expr_stmt|;
name|currentNodes
operator|.
name|getAndIncrement
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
literal|"Interrupted Exception ignored "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|isLocked
condition|)
block|{
name|nodeList
operator|.
name|writeLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Cleans the expired nodes or removes LRU nodes of the cache,    * until the cache size reduces to cleanUntil% full.    */
specifier|private
name|void
name|spawnCleaner
parameter_list|()
block|{
comment|// This spawns a separate thread to walk through the cache and removes expired nodes.
comment|// Only one cleaner thread should be running at any point.
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|isCleaning
condition|)
block|{
return|return;
block|}
name|isCleaning
operator|=
literal|true
expr_stmt|;
block|}
name|Thread
name|cleaner
init|=
operator|new
name|Thread
argument_list|(
literal|"AggregateStatsCache-CleanerThread"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|numRemovedTTL
operator|=
literal|0
expr_stmt|;
name|numRemovedLRU
operator|=
literal|0
expr_stmt|;
name|long
name|cleanerStartTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"AggregateStatsCache is "
operator|+
name|getFullPercent
argument_list|()
operator|+
literal|"% full, with "
operator|+
name|getCurrentNodes
argument_list|()
operator|+
literal|" nodes; starting cleaner thread"
argument_list|)
expr_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Key
argument_list|,
name|AggrColStatsList
argument_list|>
argument_list|>
name|mapIterator
init|=
name|cacheStore
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|mapIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Key
argument_list|,
name|AggrColStatsList
argument_list|>
name|pair
init|=
name|mapIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|AggrColStats
name|node
decl_stmt|;
name|AggrColStatsList
name|candidateList
init|=
name|pair
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AggrColStats
argument_list|>
name|nodes
init|=
name|candidateList
operator|.
name|nodes
decl_stmt|;
if|if
condition|(
name|nodes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|mapIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|boolean
name|isLocked
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isLocked
operator|=
name|candidateList
operator|.
name|writeLock
operator|.
name|tryLock
argument_list|(
name|maxWriterWaitTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocked
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|AggrColStats
argument_list|>
name|listIterator
init|=
name|nodes
operator|.
name|iterator
argument_list|()
init|;
name|listIterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|node
operator|=
name|listIterator
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// Remove the node if it has expired
if|if
condition|(
name|isExpired
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|listIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
name|numRemovedTTL
operator|++
expr_stmt|;
name|currentNodes
operator|.
name|getAndDecrement
argument_list|()
expr_stmt|;
block|}
block|}
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
literal|"Interrupted Exception ignored "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|isLocked
condition|)
block|{
name|candidateList
operator|.
name|writeLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|// We want to make sure this runs at a low priority in the background
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
block|}
comment|// If the expired nodes did not result in cache being cleanUntil% in size,
comment|// start removing LRU nodes
while|while
condition|(
name|getCurrentNodes
argument_list|()
operator|/
name|maxCacheNodes
operator|>
name|cleanUntil
condition|)
block|{
name|evictOneNode
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|isCleaning
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping cleaner thread; AggregateStatsCache is now "
operator|+
name|getFullPercent
argument_list|()
operator|+
literal|"% full, with "
operator|+
name|getCurrentNodes
argument_list|()
operator|+
literal|" nodes"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of expired nodes removed: "
operator|+
name|numRemovedTTL
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of LRU nodes removed: "
operator|+
name|numRemovedLRU
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaner ran for: "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|cleanerStartTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|cleaner
operator|.
name|setPriority
argument_list|(
name|Thread
operator|.
name|MIN_PRIORITY
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Evict an LRU node or expired node whichever we find first    */
specifier|private
name|void
name|evictOneNode
parameter_list|()
block|{
comment|// Get the LRU key, value
name|Key
name|lruKey
init|=
literal|null
decl_stmt|;
name|AggrColStatsList
name|lruValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Key
argument_list|,
name|AggrColStatsList
argument_list|>
name|entry
range|:
name|cacheStore
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Key
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|AggrColStatsList
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|lruKey
operator|==
literal|null
condition|)
block|{
name|lruKey
operator|=
name|key
expr_stmt|;
name|lruValue
operator|=
name|value
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|(
name|value
operator|.
name|lastAccessTime
operator|<
name|lruValue
operator|.
name|lastAccessTime
operator|)
operator|&&
operator|!
operator|(
name|value
operator|.
name|nodes
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|lruKey
operator|=
name|key
expr_stmt|;
name|lruValue
operator|=
name|value
expr_stmt|;
block|}
block|}
comment|// Now delete a node for this key's list
name|AggrColStatsList
name|candidateList
init|=
name|cacheStore
operator|.
name|get
argument_list|(
name|lruKey
argument_list|)
decl_stmt|;
name|boolean
name|isLocked
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isLocked
operator|=
name|candidateList
operator|.
name|writeLock
operator|.
name|tryLock
argument_list|(
name|maxWriterWaitTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocked
condition|)
block|{
name|AggrColStats
name|candidate
decl_stmt|;
name|AggrColStats
name|lruNode
init|=
literal|null
decl_stmt|;
name|int
name|currentIndex
init|=
literal|0
decl_stmt|;
name|int
name|deleteIndex
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|AggrColStats
argument_list|>
name|iterator
init|=
name|candidateList
operator|.
name|nodes
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|candidate
operator|=
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// Since we have to create space for 1, if we find an expired node we will remove it&
comment|// return
if|if
condition|(
name|isExpired
argument_list|(
name|candidate
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
name|currentNodes
operator|.
name|getAndDecrement
argument_list|()
expr_stmt|;
name|numRemovedTTL
operator|++
expr_stmt|;
return|return;
block|}
comment|// Sorry, too many ifs but this form looks optimal
comment|// Update the LRU node from what we've seen so far
if|if
condition|(
name|lruNode
operator|==
literal|null
condition|)
block|{
name|lruNode
operator|=
name|candidate
expr_stmt|;
operator|++
name|currentIndex
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|lruNode
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|candidate
operator|.
name|lastAccessTime
operator|<
name|lruNode
operator|.
name|lastAccessTime
condition|)
block|{
name|lruNode
operator|=
name|candidate
expr_stmt|;
name|deleteIndex
operator|=
name|currentIndex
expr_stmt|;
block|}
block|}
block|}
name|candidateList
operator|.
name|nodes
operator|.
name|remove
argument_list|(
name|deleteIndex
argument_list|)
expr_stmt|;
name|currentNodes
operator|.
name|getAndDecrement
argument_list|()
expr_stmt|;
name|numRemovedLRU
operator|++
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
literal|"Interrupted Exception ignored "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|isLocked
condition|)
block|{
name|candidateList
operator|.
name|writeLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|boolean
name|isExpired
parameter_list|(
name|AggrColStats
name|aggrColStats
parameter_list|)
block|{
return|return
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|aggrColStats
operator|.
name|lastAccessTime
operator|)
operator|>
name|timeToLiveMs
return|;
block|}
comment|/**    * Key object for the stats cache hashtable    */
specifier|static
class|class
name|Key
block|{
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblName
decl_stmt|;
specifier|private
specifier|final
name|String
name|colName
decl_stmt|;
name|Key
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|col
parameter_list|)
block|{
comment|// Don't construct an illegal cache key
if|if
condition|(
operator|(
name|db
operator|==
literal|null
operator|)
operator|||
operator|(
name|table
operator|==
literal|null
operator|)
operator|||
operator|(
name|col
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"dbName, tblName, colName can't be null"
argument_list|)
throw|;
block|}
name|dbName
operator|=
name|db
expr_stmt|;
name|tblName
operator|=
name|table
expr_stmt|;
name|colName
operator|=
name|col
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
operator|(
name|other
operator|==
literal|null
operator|)
operator|||
operator|!
operator|(
name|other
operator|instanceof
name|Key
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Key
name|that
init|=
operator|(
name|Key
operator|)
name|other
decl_stmt|;
return|return
name|dbName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|dbName
argument_list|)
operator|&&
name|tblName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|tblName
argument_list|)
operator|&&
name|colName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|colName
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
name|dbName
operator|.
name|hashCode
argument_list|()
operator|*
literal|31
operator|+
name|tblName
operator|.
name|hashCode
argument_list|()
operator|*
literal|31
operator|+
name|colName
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"database:"
operator|+
name|dbName
operator|+
literal|", table:"
operator|+
name|tblName
operator|+
literal|", column:"
operator|+
name|colName
return|;
block|}
block|}
specifier|static
class|class
name|AggrColStatsList
block|{
comment|// TODO: figure out a better data structure for node list(?)
specifier|private
name|List
argument_list|<
name|AggrColStats
argument_list|>
name|nodes
init|=
operator|new
name|ArrayList
argument_list|<
name|AggrColStats
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
comment|// Read lock for get operation
specifier|private
specifier|final
name|Lock
name|readLock
init|=
name|lock
operator|.
name|readLock
argument_list|()
decl_stmt|;
comment|// Write lock for add, evict and clean operation
specifier|private
specifier|final
name|Lock
name|writeLock
init|=
name|lock
operator|.
name|writeLock
argument_list|()
decl_stmt|;
comment|// Using volatile instead of locking updates to this variable,
comment|// since we can rely on approx lastAccessTime but don't want a performance hit
specifier|private
specifier|volatile
name|long
name|lastAccessTime
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|AggrColStats
argument_list|>
name|getNodes
parameter_list|()
block|{
return|return
name|nodes
return|;
block|}
name|void
name|updateLastAccessTime
parameter_list|()
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|AggrColStats
block|{
specifier|private
specifier|final
name|long
name|numPartsCached
decl_stmt|;
specifier|private
specifier|final
name|BloomFilter
name|bloomFilter
decl_stmt|;
specifier|private
specifier|final
name|ColumnStatisticsObj
name|colStats
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|lastAccessTime
decl_stmt|;
specifier|public
name|AggrColStats
parameter_list|(
name|long
name|numPartsCached
parameter_list|,
name|BloomFilter
name|bloomFilter
parameter_list|,
name|ColumnStatisticsObj
name|colStats
parameter_list|)
block|{
name|this
operator|.
name|numPartsCached
operator|=
name|numPartsCached
expr_stmt|;
name|this
operator|.
name|bloomFilter
operator|=
name|bloomFilter
expr_stmt|;
name|this
operator|.
name|colStats
operator|=
name|colStats
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getNumPartsCached
parameter_list|()
block|{
return|return
name|numPartsCached
return|;
block|}
specifier|public
name|ColumnStatisticsObj
name|getColStats
parameter_list|()
block|{
return|return
name|colStats
return|;
block|}
specifier|public
name|BloomFilter
name|getBloomFilter
parameter_list|()
block|{
return|return
name|bloomFilter
return|;
block|}
name|void
name|updateLastAccessTime
parameter_list|()
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Intermediate object, used to collect hits& misses for each cache node that is evaluate for an    * incoming request    */
specifier|private
specifier|static
class|class
name|MatchStats
block|{
specifier|private
name|int
name|hits
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|misses
init|=
literal|0
decl_stmt|;
name|MatchStats
parameter_list|(
name|int
name|hits
parameter_list|,
name|int
name|misses
parameter_list|)
block|{
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
name|this
operator|.
name|misses
operator|=
name|misses
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

