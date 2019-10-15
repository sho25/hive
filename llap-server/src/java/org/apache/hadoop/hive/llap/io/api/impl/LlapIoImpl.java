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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|impl
package|;
end_package

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
name|LinkedBlockingQueue
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
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|io
operator|.
name|CacheTag
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|StatsRecordingThreadPool
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
name|io
operator|.
name|Allocator
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
name|io
operator|.
name|DataCache
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
name|io
operator|.
name|DiskRange
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
name|io
operator|.
name|DiskRangeList
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
name|io
operator|.
name|FileMetadataCache
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
name|io
operator|.
name|Allocator
operator|.
name|BufferObjectFactory
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
name|io
operator|.
name|encoded
operator|.
name|MemoryBuffer
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
name|llap
operator|.
name|cache
operator|.
name|BuddyAllocator
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
name|llap
operator|.
name|cache
operator|.
name|BufferUsageManager
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
name|llap
operator|.
name|cache
operator|.
name|CacheContentsTracker
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
name|llap
operator|.
name|cache
operator|.
name|EvictionDispatcher
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
name|llap
operator|.
name|cache
operator|.
name|LlapDataBuffer
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
name|llap
operator|.
name|cache
operator|.
name|LlapIoDebugDump
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelCache
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelCacheImpl
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelCacheMemoryManager
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelCachePolicy
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelFifoCachePolicy
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelLrfuCachePolicy
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
name|llap
operator|.
name|cache
operator|.
name|SerDeLowLevelCacheImpl
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
name|llap
operator|.
name|cache
operator|.
name|SimpleAllocator
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
name|llap
operator|.
name|cache
operator|.
name|SimpleBufferManager
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
name|llap
operator|.
name|cache
operator|.
name|LowLevelCache
operator|.
name|Priority
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapIo
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|ColumnVectorProducer
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|GenericColumnVectorProducer
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|OrcColumnVectorProducer
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
name|llap
operator|.
name|io
operator|.
name|metadata
operator|.
name|MetadataCache
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
name|llap
operator|.
name|metrics
operator|.
name|LlapDaemonCacheMetrics
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
name|llap
operator|.
name|metrics
operator|.
name|LlapDaemonIOMetrics
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
name|llap
operator|.
name|metrics
operator|.
name|MetricsUtils
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|LlapCacheOnlyInputFormatInterface
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
name|encoded
operator|.
name|IoTrace
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
name|serde2
operator|.
name|Deserializer
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
name|io
operator|.
name|NullWritable
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
name|metrics2
operator|.
name|util
operator|.
name|MBeans
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
name|FixedSizedObjectPool
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
name|primitives
operator|.
name|Ints
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

begin_class
specifier|public
class|class
name|LlapIoImpl
implements|implements
name|LlapIo
argument_list|<
name|VectorizedRowBatch
argument_list|>
implements|,
name|LlapIoDebugDump
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"LlapIoImpl"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|ORC_LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"LlapIoOrc"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|CACHE_LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"LlapIoCache"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOCKING_LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"LlapIoLocking"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MODE_CACHE
init|=
literal|"cache"
decl_stmt|;
comment|// TODO: later, we may have a map
specifier|private
specifier|final
name|ColumnVectorProducer
name|orcCvp
decl_stmt|,
name|genericCvp
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonCacheMetrics
name|cacheMetrics
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonIOMetrics
name|ioMetrics
decl_stmt|;
specifier|private
name|ObjectName
name|buddyAllocatorMXBean
decl_stmt|;
specifier|private
specifier|final
name|Allocator
name|allocator
decl_stmt|;
specifier|private
specifier|final
name|FileMetadataCache
name|fileMetadataCache
decl_stmt|;
specifier|private
specifier|final
name|LowLevelCache
name|dataCache
decl_stmt|;
specifier|private
specifier|final
name|BufferUsageManager
name|bufferManager
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|daemonConf
decl_stmt|;
specifier|private
specifier|final
name|LowLevelCacheMemoryManager
name|memoryManager
decl_stmt|;
specifier|private
name|List
argument_list|<
name|LlapIoDebugDump
argument_list|>
name|debugDumpComponents
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|LlapIoImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|daemonConf
operator|=
name|conf
expr_stmt|;
name|String
name|ioMode
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
name|LLAP_IO_MEMORY_MODE
argument_list|)
decl_stmt|;
name|boolean
name|useLowLevelCache
init|=
name|LlapIoImpl
operator|.
name|MODE_CACHE
operator|.
name|equalsIgnoreCase
argument_list|(
name|ioMode
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing LLAP IO in {} mode"
argument_list|,
name|useLowLevelCache
condition|?
name|LlapIoImpl
operator|.
name|MODE_CACHE
else|:
literal|"none"
argument_list|)
expr_stmt|;
name|String
name|displayName
init|=
literal|"LlapDaemonCacheMetrics-"
operator|+
name|MetricsUtils
operator|.
name|getHostName
argument_list|()
decl_stmt|;
name|String
name|sessionId
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"llap.daemon.metrics.sessionid"
argument_list|)
decl_stmt|;
name|this
operator|.
name|cacheMetrics
operator|=
name|LlapDaemonCacheMetrics
operator|.
name|create
argument_list|(
name|displayName
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|displayName
operator|=
literal|"LlapDaemonIOMetrics-"
operator|+
name|MetricsUtils
operator|.
name|getHostName
argument_list|()
expr_stmt|;
name|String
index|[]
name|strIntervals
init|=
name|HiveConf
operator|.
name|getTrimmedStringsVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_DECODING_METRICS_PERCENTILE_INTERVALS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|intervalList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|strIntervals
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|strInterval
range|:
name|strIntervals
control|)
block|{
try|try
block|{
name|intervalList
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|strInterval
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring IO decoding metrics interval {} from {} as it is invalid"
argument_list|,
name|strInterval
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|strIntervals
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|this
operator|.
name|ioMetrics
operator|=
name|LlapDaemonIOMetrics
operator|.
name|create
argument_list|(
name|displayName
argument_list|,
name|sessionId
argument_list|,
name|Ints
operator|.
name|toArray
argument_list|(
name|intervalList
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started llap daemon metrics with displayName: {} sessionId: {}"
argument_list|,
name|displayName
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|MetadataCache
name|metadataCache
init|=
literal|null
decl_stmt|;
name|SerDeLowLevelCacheImpl
name|serdeCache
init|=
literal|null
decl_stmt|;
comment|// TODO: extract interface when needed
name|BufferUsageManager
name|bufferManagerOrc
init|=
literal|null
decl_stmt|,
name|bufferManagerGeneric
init|=
literal|null
decl_stmt|;
name|boolean
name|isEncodeEnabled
init|=
name|useLowLevelCache
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_IO_ENCODE_ENABLED
argument_list|)
decl_stmt|;
if|if
condition|(
name|useLowLevelCache
condition|)
block|{
comment|// Memory manager uses cache policy to trigger evictions, so create the policy first.
name|boolean
name|useLrfu
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
name|LLAP_USE_LRFU
argument_list|)
decl_stmt|;
name|long
name|totalMemorySize
init|=
name|HiveConf
operator|.
name|getSizeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
argument_list|)
decl_stmt|;
name|int
name|minAllocSize
init|=
operator|(
name|int
operator|)
name|HiveConf
operator|.
name|getSizeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_MIN_ALLOC
argument_list|)
decl_stmt|;
name|LowLevelCachePolicy
name|realCachePolicy
init|=
name|useLrfu
condition|?
operator|new
name|LowLevelLrfuCachePolicy
argument_list|(
name|minAllocSize
argument_list|,
name|totalMemorySize
argument_list|,
name|conf
argument_list|)
else|:
operator|new
name|LowLevelFifoCachePolicy
argument_list|()
decl_stmt|;
name|boolean
name|trackUsage
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
name|LLAP_TRACK_CACHE_USAGE
argument_list|)
decl_stmt|;
name|LowLevelCachePolicy
name|cachePolicyWrapper
decl_stmt|;
if|if
condition|(
name|trackUsage
condition|)
block|{
name|cachePolicyWrapper
operator|=
operator|new
name|CacheContentsTracker
argument_list|(
name|realCachePolicy
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cachePolicyWrapper
operator|=
name|realCachePolicy
expr_stmt|;
block|}
comment|// Allocator uses memory manager to request memory, so create the manager next.
name|this
operator|.
name|memoryManager
operator|=
operator|new
name|LowLevelCacheMemoryManager
argument_list|(
name|totalMemorySize
argument_list|,
name|cachePolicyWrapper
argument_list|,
name|cacheMetrics
argument_list|)
expr_stmt|;
name|cacheMetrics
operator|.
name|setCacheCapacityTotal
argument_list|(
name|totalMemorySize
argument_list|)
expr_stmt|;
comment|// Cache uses allocator to allocate and deallocate, create allocator and then caches.
name|BuddyAllocator
name|allocator
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
name|memoryManager
argument_list|,
name|cacheMetrics
argument_list|)
decl_stmt|;
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
name|LowLevelCacheImpl
name|cacheImpl
init|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|cacheMetrics
argument_list|,
name|cachePolicyWrapper
argument_list|,
name|allocator
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|dataCache
operator|=
name|cacheImpl
expr_stmt|;
if|if
condition|(
name|isEncodeEnabled
condition|)
block|{
name|SerDeLowLevelCacheImpl
name|serdeCacheImpl
init|=
operator|new
name|SerDeLowLevelCacheImpl
argument_list|(
name|cacheMetrics
argument_list|,
name|cachePolicyWrapper
argument_list|,
name|allocator
argument_list|)
decl_stmt|;
name|serdeCache
operator|=
name|serdeCacheImpl
expr_stmt|;
name|serdeCacheImpl
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|boolean
name|useGapCache
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_CACHE_ENABLE_ORC_GAP_CACHE
argument_list|)
decl_stmt|;
name|metadataCache
operator|=
operator|new
name|MetadataCache
argument_list|(
name|allocator
argument_list|,
name|memoryManager
argument_list|,
name|cachePolicyWrapper
argument_list|,
name|useGapCache
argument_list|,
name|cacheMetrics
argument_list|)
expr_stmt|;
name|fileMetadataCache
operator|=
name|metadataCache
expr_stmt|;
comment|// And finally cache policy uses cache to notify it of eviction. The cycle is complete!
name|EvictionDispatcher
name|e
init|=
operator|new
name|EvictionDispatcher
argument_list|(
name|dataCache
argument_list|,
name|serdeCache
argument_list|,
name|metadataCache
argument_list|,
name|allocator
argument_list|)
decl_stmt|;
name|cachePolicyWrapper
operator|.
name|setEvictionListener
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|cacheImpl
operator|.
name|startThreads
argument_list|()
expr_stmt|;
comment|// Start the cache threads.
name|bufferManager
operator|=
name|bufferManagerOrc
operator|=
name|cacheImpl
expr_stmt|;
comment|// Cache also serves as buffer manager.
name|bufferManagerGeneric
operator|=
name|serdeCache
expr_stmt|;
if|if
condition|(
name|trackUsage
condition|)
block|{
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|cachePolicyWrapper
argument_list|)
expr_stmt|;
comment|// Cache contents tracker.
block|}
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|realCachePolicy
argument_list|)
expr_stmt|;
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|cacheImpl
argument_list|)
expr_stmt|;
if|if
condition|(
name|serdeCache
operator|!=
literal|null
condition|)
block|{
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|serdeCache
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metadataCache
operator|!=
literal|null
condition|)
block|{
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|metadataCache
argument_list|)
expr_stmt|;
block|}
name|debugDumpComponents
operator|.
name|add
argument_list|(
name|allocator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|allocator
operator|=
operator|new
name|SimpleAllocator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fileMetadataCache
operator|=
literal|null
expr_stmt|;
name|SimpleBufferManager
name|sbm
init|=
operator|new
name|SimpleBufferManager
argument_list|(
name|allocator
argument_list|,
name|cacheMetrics
argument_list|)
decl_stmt|;
name|bufferManager
operator|=
name|bufferManagerOrc
operator|=
name|bufferManagerGeneric
operator|=
name|sbm
expr_stmt|;
name|dataCache
operator|=
name|sbm
expr_stmt|;
name|this
operator|.
name|memoryManager
operator|=
literal|null
expr_stmt|;
name|debugDumpComponents
operator|.
name|add
argument_list|(
operator|new
name|LlapIoDebugDump
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|debugDumpShort
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"LLAP IO allocator is not in use!"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|// IO thread pool. Listening is used for unhandled errors for now (TODO: remove?)
name|int
name|numThreads
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
name|LLAP_IO_THREADPOOL_SIZE
argument_list|)
decl_stmt|;
name|executor
operator|=
operator|new
name|StatsRecordingThreadPool
argument_list|(
name|numThreads
argument_list|,
name|numThreads
argument_list|,
literal|0L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"IO-Elevator-Thread-%d"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|FixedSizedObjectPool
argument_list|<
name|IoTrace
argument_list|>
name|tracePool
init|=
name|IoTrace
operator|.
name|createTracePool
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// TODO: this should depends on input format and be in a map, or something.
name|this
operator|.
name|orcCvp
operator|=
operator|new
name|OrcColumnVectorProducer
argument_list|(
name|metadataCache
argument_list|,
name|dataCache
argument_list|,
name|bufferManagerOrc
argument_list|,
name|conf
argument_list|,
name|cacheMetrics
argument_list|,
name|ioMetrics
argument_list|,
name|tracePool
argument_list|)
expr_stmt|;
name|this
operator|.
name|genericCvp
operator|=
name|isEncodeEnabled
condition|?
operator|new
name|GenericColumnVectorProducer
argument_list|(
name|serdeCache
argument_list|,
name|bufferManagerGeneric
argument_list|,
name|conf
argument_list|,
name|cacheMetrics
argument_list|,
name|ioMetrics
argument_list|,
name|tracePool
argument_list|)
else|:
literal|null
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"LLAP IO initialized"
argument_list|)
expr_stmt|;
name|registerMXBeans
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|registerMXBeans
parameter_list|()
block|{
name|buddyAllocatorMXBean
operator|=
name|MBeans
operator|.
name|register
argument_list|(
literal|"LlapDaemon"
argument_list|,
literal|"BuddyAllocatorInfo"
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMemoryInfo
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|purge
parameter_list|()
block|{
if|if
condition|(
name|memoryManager
operator|!=
literal|null
condition|)
block|{
return|return
name|memoryManager
operator|.
name|purge
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getInputFormat
parameter_list|(
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sourceInputFormat
parameter_list|,
name|Deserializer
name|sourceSerDe
parameter_list|)
block|{
name|ColumnVectorProducer
name|cvp
init|=
name|genericCvp
decl_stmt|;
if|if
condition|(
name|sourceInputFormat
operator|instanceof
name|OrcInputFormat
condition|)
block|{
name|cvp
operator|=
name|orcCvp
expr_stmt|;
comment|// Special-case for ORC.
block|}
elseif|else
if|if
condition|(
name|cvp
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"LLAP encode is disabled; cannot use for "
operator|+
name|sourceInputFormat
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
operator|new
name|LlapInputFormat
argument_list|(
name|sourceInputFormat
argument_list|,
name|sourceSerDe
argument_list|,
name|cvp
argument_list|,
name|executor
argument_list|,
name|daemonConf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing LlapIoImpl.."
argument_list|)
expr_stmt|;
if|if
condition|(
name|buddyAllocatorMXBean
operator|!=
literal|null
condition|)
block|{
name|MBeans
operator|.
name|unregister
argument_list|(
name|buddyAllocatorMXBean
argument_list|)
expr_stmt|;
name|buddyAllocatorMXBean
operator|=
literal|null
expr_stmt|;
block|}
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initCacheOnlyInputFormat
parameter_list|(
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|inputFormat
parameter_list|)
block|{
name|LlapCacheOnlyInputFormatInterface
name|cacheIf
init|=
operator|(
name|LlapCacheOnlyInputFormatInterface
operator|)
name|inputFormat
decl_stmt|;
name|cacheIf
operator|.
name|injectCaches
argument_list|(
name|fileMetadataCache
argument_list|,
operator|new
name|GenericDataCache
argument_list|(
name|dataCache
argument_list|,
name|bufferManager
argument_list|)
argument_list|,
name|daemonConf
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|GenericDataCache
implements|implements
name|DataCache
implements|,
name|BufferObjectFactory
block|{
specifier|private
specifier|final
name|LowLevelCache
name|lowLevelCache
decl_stmt|;
specifier|private
specifier|final
name|BufferUsageManager
name|bufferManager
decl_stmt|;
specifier|public
name|GenericDataCache
parameter_list|(
name|LowLevelCache
name|lowLevelCache
parameter_list|,
name|BufferUsageManager
name|bufferManager
parameter_list|)
block|{
name|this
operator|.
name|lowLevelCache
operator|=
name|lowLevelCache
expr_stmt|;
name|this
operator|.
name|bufferManager
operator|=
name|bufferManager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DiskRangeList
name|getFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRangeList
name|range
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|DiskRangeListFactory
name|factory
parameter_list|,
name|BooleanRef
name|gotAllData
parameter_list|)
block|{
comment|// TODO: we currently pass null counters because this doesn't use LlapRecordReader.
comment|//       Create counters for non-elevator-using fragments also?
return|return
name|lowLevelCache
operator|.
name|getFileData
argument_list|(
name|fileKey
argument_list|,
name|range
argument_list|,
name|baseOffset
argument_list|,
name|factory
argument_list|,
literal|null
argument_list|,
name|gotAllData
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
index|[]
name|putFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRange
index|[]
name|ranges
parameter_list|,
name|MemoryBuffer
index|[]
name|data
parameter_list|,
name|long
name|baseOffset
parameter_list|)
block|{
return|return
name|putFileData
argument_list|(
name|fileKey
argument_list|,
name|ranges
argument_list|,
name|data
argument_list|,
name|baseOffset
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
index|[]
name|putFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRange
index|[]
name|ranges
parameter_list|,
name|MemoryBuffer
index|[]
name|data
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|CacheTag
name|tag
parameter_list|)
block|{
return|return
name|lowLevelCache
operator|.
name|putFileData
argument_list|(
name|fileKey
argument_list|,
name|ranges
argument_list|,
name|data
argument_list|,
name|baseOffset
argument_list|,
name|Priority
operator|.
name|NORMAL
argument_list|,
literal|null
argument_list|,
name|tag
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
name|bufferManager
operator|.
name|decRefBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reuseBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
name|boolean
name|isReused
init|=
name|bufferManager
operator|.
name|incRefBuffer
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
assert|assert
name|isReused
assert|;
block|}
annotation|@
name|Override
specifier|public
name|Allocator
name|getAllocator
parameter_list|()
block|{
return|return
name|bufferManager
operator|.
name|getAllocator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BufferObjectFactory
name|getDataBufferFactory
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|MemoryBuffer
name|create
parameter_list|()
block|{
return|return
operator|new
name|LlapDataBuffer
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|debugDumpShort
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
for|for
control|(
name|LlapIoDebugDump
name|child
range|:
name|debugDumpComponents
control|)
block|{
name|child
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

