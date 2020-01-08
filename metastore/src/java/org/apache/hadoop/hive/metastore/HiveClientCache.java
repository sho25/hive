begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ScheduledFuture
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
name|ThreadFactory
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|lang3
operator|.
name|builder
operator|.
name|EqualsBuilder
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
name|lang3
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|classification
operator|.
name|InterfaceAudience
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
name|annotation
operator|.
name|NoReconnect
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
name|shims
operator|.
name|Utils
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
name|security
operator|.
name|UserGroupInformation
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
name|ShutdownHookManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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
name|cache
operator|.
name|Cache
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
name|RemovalListener
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
name|RemovalNotification
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
comment|/**  * A thread safe time expired cache for HiveMetaStoreClient  */
end_comment

begin_class
class|class
name|HiveClientCache
block|{
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_HIVE_CACHE_EXPIRY_TIME_SECONDS
init|=
literal|2
operator|*
literal|60
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_HIVE_CACHE_INITIAL_CAPACITY
init|=
literal|50
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_HIVE_CACHE_MAX_CAPACITY
init|=
literal|50
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|boolean
name|DEFAULT_HIVE_CLIENT_CACHE_STATS_ENABLED
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Cache
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
name|hiveCache
decl_stmt|;
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
name|HiveClientCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|timeout
decl_stmt|;
comment|// This lock is used to make sure removalListener won't close a client that is being contemplated for returning by get()
specifier|private
specifier|final
name|Object
name|CACHE_TEARDOWN_LOCK
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|nextId
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|cleanupHandle
decl_stmt|;
comment|// used to cleanup cache
specifier|private
name|boolean
name|enableStats
decl_stmt|;
comment|// Since HiveMetaStoreClient is not threadsafe, hive clients are not  shared across threads.
comment|// Thread local variable containing each thread's unique ID, is used as one of the keys for the cache
comment|// causing each thread to get a different client even if the conf is same.
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|Integer
argument_list|>
name|threadId
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Integer
name|initialValue
parameter_list|()
block|{
return|return
name|nextId
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|private
name|int
name|getThreadId
parameter_list|()
block|{
return|return
name|threadId
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|IMetaStoreClient
name|getNonCachedHiveMetastoreClient
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|RetryingMetaStoreClient
operator|.
name|getProxy
argument_list|(
name|hiveConf
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|HiveClientCache
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
argument_list|(
operator|(
name|int
operator|)
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CACHE_EXPIRY_TIME
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CACHE_INITIAL_CAPACITY
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CACHE_MAX_CAPACITY
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CACHE_STATS_ENABLED
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated This constructor will be made private or removed as more configuration properties are required.    */
annotation|@
name|Deprecated
specifier|public
name|HiveClientCache
parameter_list|(
specifier|final
name|int
name|timeout
parameter_list|)
block|{
name|this
argument_list|(
name|timeout
argument_list|,
name|DEFAULT_HIVE_CACHE_INITIAL_CAPACITY
argument_list|,
name|DEFAULT_HIVE_CACHE_MAX_CAPACITY
argument_list|,
name|DEFAULT_HIVE_CLIENT_CACHE_STATS_ENABLED
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param timeout the length of time in seconds after a client is created that it should be automatically removed    */
specifier|private
name|HiveClientCache
parameter_list|(
specifier|final
name|int
name|timeout
parameter_list|,
specifier|final
name|int
name|initialCapacity
parameter_list|,
specifier|final
name|int
name|maxCapacity
parameter_list|,
specifier|final
name|boolean
name|enableStats
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|enableStats
operator|=
name|enableStats
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing cache: eviction-timeout="
operator|+
name|timeout
operator|+
literal|" initial-capacity="
operator|+
name|initialCapacity
operator|+
literal|" maximum-capacity="
operator|+
name|maxCapacity
argument_list|)
expr_stmt|;
name|CacheBuilder
name|builder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|initialCapacity
argument_list|(
name|initialCapacity
argument_list|)
operator|.
name|maximumSize
argument_list|(
name|maxCapacity
argument_list|)
operator|.
name|expireAfterAccess
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|removalListener
argument_list|(
name|createRemovalListener
argument_list|()
argument_list|)
decl_stmt|;
comment|/*      * Guava versions<12.0 have stats collection enabled by default and do not expose a recordStats method.      * Check for newer versions of the library and ensure that stats collection is enabled by default.      */
try|try
block|{
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
name|m
init|=
name|builder
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"recordStats"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|m
operator|.
name|invoke
argument_list|(
name|builder
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using a version of guava<12.0. Stats collection is enabled by default."
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
name|warn
argument_list|(
literal|"Unable to invoke recordStats method."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|hiveCache
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
comment|/*      * We need to use a cleanup interval, which is how often the cleanup thread will kick in      * and go do a check to see if any of the connections can be expired. We don't want to      * do this too often, because it'd be like having a mini-GC going off every so often,      * so we limit it to a minimum of DEFAULT_HIVE_CACHE_EXPIRY_TIME_SECONDS. If the client      * has explicitly set a larger timeout on the cache, though, we respect that, and use that      */
name|long
name|cleanupInterval
init|=
name|timeout
operator|>
name|DEFAULT_HIVE_CACHE_EXPIRY_TIME_SECONDS
condition|?
name|timeout
else|:
name|DEFAULT_HIVE_CACHE_EXPIRY_TIME_SECONDS
decl_stmt|;
name|this
operator|.
name|cleanupHandle
operator|=
name|createCleanupThread
argument_list|(
name|cleanupInterval
argument_list|)
expr_stmt|;
name|createShutdownHook
argument_list|()
expr_stmt|;
block|}
specifier|private
name|RemovalListener
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
name|createRemovalListener
parameter_list|()
block|{
name|RemovalListener
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
name|listener
init|=
operator|new
name|RemovalListener
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
name|notification
parameter_list|)
block|{
name|ICacheableMetaStoreClient
name|hiveMetaStoreClient
init|=
name|notification
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|hiveMetaStoreClient
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Evicting client: "
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|System
operator|.
name|identityHashCode
argument_list|(
name|hiveMetaStoreClient
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// TODO: This global lock may not be necessary as all concurrent methods in ICacheableMetaStoreClient
comment|// are synchronized.
synchronized|synchronized
init|(
name|CACHE_TEARDOWN_LOCK
init|)
block|{
name|hiveMetaStoreClient
operator|.
name|setExpiredFromCache
argument_list|()
expr_stmt|;
name|hiveMetaStoreClient
operator|.
name|tearDownIfUnused
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
return|return
name|listener
return|;
block|}
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|createCleanupThread
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
comment|// Add a maintenance thread that will attempt to trigger a cache clean continuously
name|Runnable
name|cleanupThread
init|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|/**      * Create the cleanup handle. In addition to cleaning up every cleanupInterval, we add      * a slight offset, so that the very first time it runs, it runs with a slight delay, so      * as to catch any other connections that were closed when the first timeout happened.      * As a result, the time we can expect an unused connection to be reaped is      * 5 seconds after the first timeout, and then after that, it'll check for whether or not      * it can be cleaned every max(DEFAULT_HIVE_CACHE_EXPIRY_TIME_SECONDS,timeout) seconds      */
name|ThreadFactory
name|daemonThreadFactory
init|=
operator|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"HiveClientCache-cleaner-%d"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
name|daemonThreadFactory
argument_list|)
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|cleanupThread
argument_list|,
name|timeout
operator|+
literal|5
argument_list|,
name|interval
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
specifier|private
name|void
name|createShutdownHook
parameter_list|()
block|{
comment|// Add a shutdown hook for cleanup, if there are elements remaining in the cache which were not cleaned up.
comment|// This is the best effort approach. Ignore any error while doing so. Notice that most of the clients
comment|// would get cleaned up via either the removalListener or the close() call, only the active clients
comment|// that are in the cache or expired but being used in other threads wont get cleaned. The following code will only
comment|// clean the active cache ones. The ones expired from cache but being hold by other threads are in the mercy
comment|// of finalize() being called.
name|Thread
name|cleanupHiveClientShutdownThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cleaning up hive client cache in ShutDown hook"
argument_list|)
expr_stmt|;
name|cleanupHandle
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Cancel the maintenance thread.
name|closeAllClientsQuietly
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ShutdownHookManager
operator|.
name|addShutdownHook
argument_list|(
name|cleanupHiveClientShutdownThread
argument_list|)
expr_stmt|;
block|}
comment|/**    * Note: This doesn't check if they are being used or not, meant only to be called during shutdown etc.    */
name|void
name|closeAllClientsQuietly
parameter_list|()
block|{
try|try
block|{
name|ConcurrentMap
argument_list|<
name|HiveClientCacheKey
argument_list|,
name|ICacheableMetaStoreClient
argument_list|>
name|elements
init|=
name|hiveCache
operator|.
name|asMap
argument_list|()
decl_stmt|;
for|for
control|(
name|ICacheableMetaStoreClient
name|cacheableHiveMetaStoreClient
range|:
name|elements
operator|.
name|values
argument_list|()
control|)
block|{
name|cacheableHiveMetaStoreClient
operator|.
name|tearDown
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Clean up of hive clients in the cache failed. Ignored"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|enableStats
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cache statistics after shutdown: size="
operator|+
name|hiveCache
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|hiveCache
operator|.
name|stats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
comment|// TODO: periodically reload a new HiveConf to check if stats reporting is enabled.
name|hiveCache
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
if|if
condition|(
name|enableStats
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cache statistics after cleanup: size="
operator|+
name|hiveCache
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|hiveCache
operator|.
name|stats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns a cached client if exists or else creates one, caches and returns it. It also checks that the client is    * healthy and can be reused    * @param hiveConf    * @return the hive client    * @throws MetaException    * @throws IOException    * @throws LoginException    */
specifier|public
name|IMetaStoreClient
name|get
parameter_list|(
specifier|final
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|MetaException
throws|,
name|IOException
throws|,
name|LoginException
block|{
specifier|final
name|HiveClientCacheKey
name|cacheKey
init|=
name|HiveClientCacheKey
operator|.
name|fromHiveConf
argument_list|(
name|hiveConf
argument_list|,
name|getThreadId
argument_list|()
argument_list|)
decl_stmt|;
name|ICacheableMetaStoreClient
name|cacheableHiveMetaStoreClient
init|=
literal|null
decl_stmt|;
comment|// the hmsc is not shared across threads. So the only way it could get closed while we are doing healthcheck
comment|// is if removalListener closes it. The synchronization takes care that removalListener won't do it
synchronized|synchronized
init|(
name|CACHE_TEARDOWN_LOCK
init|)
block|{
name|cacheableHiveMetaStoreClient
operator|=
name|getOrCreate
argument_list|(
name|cacheKey
argument_list|)
expr_stmt|;
name|cacheableHiveMetaStoreClient
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|cacheableHiveMetaStoreClient
operator|.
name|isOpen
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|CACHE_TEARDOWN_LOCK
init|)
block|{
name|hiveCache
operator|.
name|invalidate
argument_list|(
name|cacheKey
argument_list|)
expr_stmt|;
name|cacheableHiveMetaStoreClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|cacheableHiveMetaStoreClient
operator|=
name|getOrCreate
argument_list|(
name|cacheKey
argument_list|)
expr_stmt|;
name|cacheableHiveMetaStoreClient
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|cacheableHiveMetaStoreClient
return|;
block|}
comment|/**    * Return from cache if exists else create/cache and return    * @param cacheKey    * @return    * @throws IOException    * @throws MetaException    * @throws LoginException    */
specifier|private
name|ICacheableMetaStoreClient
name|getOrCreate
parameter_list|(
specifier|final
name|HiveClientCacheKey
name|cacheKey
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|LoginException
block|{
try|try
block|{
return|return
name|hiveCache
operator|.
name|get
argument_list|(
name|cacheKey
argument_list|,
operator|new
name|Callable
argument_list|<
name|ICacheableMetaStoreClient
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ICacheableMetaStoreClient
name|call
parameter_list|()
throws|throws
name|MetaException
block|{
comment|// This is called from HCat, so always allow embedded metastore (as was the default).
return|return
operator|(
name|ICacheableMetaStoreClient
operator|)
name|RetryingMetaStoreClient
operator|.
name|getProxy
argument_list|(
name|cacheKey
operator|.
name|getHiveConf
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|HiveConf
operator|.
name|class
operator|,
name|Integer
operator|.
name|class
operator|,
name|Boolean
operator|.
name|class
block|}
operator|,
operator|new
name|Object
index|[]
block|{
name|cacheKey
operator|.
name|getHiveConf
argument_list|()
block|,
name|timeout
block|,
literal|true
block|}
operator|,
name|CacheableHiveMetaStoreClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
block|)
class|;
end_class

begin_expr_stmt
unit|} catch
operator|(
name|ExecutionException
name|e
operator|)
block|{
name|Throwable
name|t
operator|=
name|e
operator|.
name|getCause
argument_list|()
block|;
if|if
condition|(
name|t
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|t
throw|;
block|}
end_expr_stmt

begin_elseif
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|MetaException
condition|)
block|{
throw|throw
operator|(
name|MetaException
operator|)
name|t
throw|;
block|}
end_elseif

begin_elseif
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|LoginException
condition|)
block|{
throw|throw
operator|(
name|LoginException
operator|)
name|t
throw|;
block|}
end_elseif

begin_else
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error creating hiveMetaStoreClient"
argument_list|,
name|t
argument_list|)
throw|;
block|}
end_else

begin_comment
unit|}   }
comment|/**    * A class to wrap HiveConf and expose equality based only on UserGroupInformation and the metaStoreURIs.    * This becomes the key for the cache and this way the same HiveMetaStoreClient would be returned if    * UserGroupInformation and metaStoreURIs are same. This function can evolve to express    * the cases when HiveConf is different but the same hiveMetaStoreClient can be used    */
end_comment

begin_class
specifier|static
class|class
name|HiveClientCacheKey
block|{
specifier|final
specifier|private
name|String
name|metaStoreURIs
decl_stmt|;
specifier|final
specifier|private
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|final
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|final
specifier|private
name|int
name|threadId
decl_stmt|;
specifier|private
name|HiveClientCacheKey
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
specifier|final
name|int
name|threadId
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
name|this
operator|.
name|metaStoreURIs
operator|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|)
expr_stmt|;
name|ugi
operator|=
name|Utils
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|threadId
operator|=
name|threadId
expr_stmt|;
block|}
specifier|public
specifier|static
name|HiveClientCacheKey
name|fromHiveConf
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
specifier|final
name|int
name|threadId
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
return|return
operator|new
name|HiveClientCacheKey
argument_list|(
name|hiveConf
argument_list|,
name|threadId
argument_list|)
return|;
block|}
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|HiveClientCacheKey
name|that
init|=
operator|(
name|HiveClientCacheKey
operator|)
name|o
decl_stmt|;
return|return
operator|new
name|EqualsBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|this
operator|.
name|metaStoreURIs
argument_list|,
name|that
operator|.
name|metaStoreURIs
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|ugi
argument_list|,
name|that
operator|.
name|ugi
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|threadId
argument_list|,
name|that
operator|.
name|threadId
argument_list|)
operator|.
name|isEquals
argument_list|()
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
operator|new
name|HashCodeBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|metaStoreURIs
argument_list|)
operator|.
name|append
argument_list|(
name|ugi
argument_list|)
operator|.
name|append
argument_list|(
name|threadId
argument_list|)
operator|.
name|toHashCode
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
literal|"HiveClientCacheKey: uri="
operator|+
name|this
operator|.
name|metaStoreURIs
operator|+
literal|" ugi="
operator|+
name|this
operator|.
name|ugi
operator|+
literal|" thread="
operator|+
name|this
operator|.
name|threadId
return|;
block|}
block|}
end_class

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ICacheableMetaStoreClient
extends|extends
name|IMetaStoreClient
block|{
annotation|@
name|NoReconnect
name|void
name|acquire
parameter_list|()
function_decl|;
annotation|@
name|NoReconnect
name|void
name|setExpiredFromCache
parameter_list|()
function_decl|;
annotation|@
name|NoReconnect
name|AtomicInteger
name|getUsers
parameter_list|()
function_decl|;
annotation|@
name|NoReconnect
name|boolean
name|isClosed
parameter_list|()
function_decl|;
comment|/**      * @deprecated This method is not used internally and should not be visible through HCatClient.create.      */
annotation|@
name|Deprecated
annotation|@
name|NoReconnect
name|boolean
name|isOpen
parameter_list|()
function_decl|;
annotation|@
name|NoReconnect
name|void
name|tearDownIfUnused
parameter_list|()
function_decl|;
annotation|@
name|NoReconnect
name|void
name|tearDown
parameter_list|()
function_decl|;
block|}
end_interface

begin_comment
comment|/**    * Add # of current users on HiveMetaStoreClient, so that the client can be cleaned when no one is using it.    */
end_comment

begin_class
specifier|static
class|class
name|CacheableHiveMetaStoreClient
extends|extends
name|HiveMetaStoreClient
implements|implements
name|ICacheableMetaStoreClient
block|{
specifier|private
specifier|final
name|AtomicInteger
name|users
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|expiredFromCache
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
name|CacheableHiveMetaStoreClient
parameter_list|(
specifier|final
name|HiveConf
name|conf
parameter_list|,
specifier|final
name|Integer
name|timeout
parameter_list|,
name|Boolean
name|allowEmbedded
parameter_list|)
throws|throws
name|MetaException
block|{
name|super
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
name|allowEmbedded
argument_list|)
expr_stmt|;
block|}
comment|/**      * Increments the user count and optionally renews the expiration time.      *<code>renew</code> should correspond with the expiration policy of the cache.      * When the policy is<code>expireAfterAccess</code>, the expiration time should be extended.      * When the policy is<code>expireAfterWrite</code>, the expiration time should not be extended.      * A mismatch with the policy will lead to closing the connection unnecessarily after the initial      * expiration time is generated.      */
specifier|public
specifier|synchronized
name|void
name|acquire
parameter_list|()
block|{
name|users
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|>
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected increment of user count beyond one: "
operator|+
name|users
operator|.
name|get
argument_list|()
operator|+
literal|" "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Decrements the user count.      */
specifier|private
name|void
name|release
parameter_list|()
block|{
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|users
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected attempt to decrement user count of zero: "
operator|+
name|users
operator|.
name|get
argument_list|()
operator|+
literal|" "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Communicate to the client that it is no longer in the cache.      * The expiration time should be voided to allow the connection to be closed at the first opportunity.      */
specifier|public
specifier|synchronized
name|void
name|setExpiredFromCache
parameter_list|()
block|{
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Evicted client has non-zero user count: "
operator|+
name|users
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|expiredFromCache
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|isClosed
return|;
block|}
comment|/*      * Used only for Debugging or testing purposes      */
specifier|public
name|AtomicInteger
name|getUsers
parameter_list|()
block|{
return|return
name|users
return|;
block|}
comment|/**      * Make a call to hive meta store and see if the client is still usable. Some calls where the user provides      * invalid data renders the client unusable for future use (example: create a table with very long table name)      * @return      */
annotation|@
name|Deprecated
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
try|try
block|{
comment|// Look for an unlikely database name and see if either MetaException or TException is thrown
name|super
operator|.
name|getDatabases
argument_list|(
literal|"NonExistentDatabaseUsedForHealthCheck"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Decrement the user count and piggyback this to set expiry flag as well, then  teardown(), if conditions are met.      * This *MUST* be called by anyone who uses this client.      */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
block|{
name|release
argument_list|()
expr_stmt|;
name|tearDownIfUnused
argument_list|()
expr_stmt|;
block|}
comment|/**      * Attempt to tear down the client connection.      * The connection will be closed if the following conditions hold:      *  1. There are no active user holding the client.      *  2. The client has been evicted from the cache.      */
specifier|public
specifier|synchronized
name|void
name|tearDownIfUnused
parameter_list|()
block|{
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Non-zero user count preventing client tear down: users="
operator|+
name|users
operator|.
name|get
argument_list|()
operator|+
literal|" expired="
operator|+
name|expiredFromCache
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|==
literal|0
operator|&&
name|expiredFromCache
condition|)
block|{
name|this
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Close the underlying objects irrespective of whether they are in use or not.      */
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|isClosed
condition|)
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|isClosed
operator|=
literal|true
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
name|warn
argument_list|(
literal|"Error closing hive metastore client. Ignored."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HCatClient: thread: "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
operator|+
literal|" users="
operator|+
name|users
operator|.
name|get
argument_list|()
operator|+
literal|" expired="
operator|+
name|expiredFromCache
operator|+
literal|" closed="
operator|+
name|isClosed
return|;
block|}
comment|/**      * GC is attempting to destroy the object.      * No one references this client anymore, so it can be torn down without worrying about user counts.      * @throws Throwable      */
annotation|@
name|Override
specifier|protected
name|void
name|finalize
parameter_list|()
throws|throws
name|Throwable
block|{
if|if
condition|(
name|users
operator|.
name|get
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Closing client with non-zero user count: users="
operator|+
name|users
operator|.
name|get
argument_list|()
operator|+
literal|" expired="
operator|+
name|expiredFromCache
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|super
operator|.
name|finalize
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

unit|}
end_unit

