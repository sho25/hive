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
name|ql
operator|.
name|stats
operator|.
name|fs
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|ql
operator|.
name|exec
operator|.
name|SerializationUtilities
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
name|stats
operator|.
name|StatsAggregator
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
name|stats
operator|.
name|StatsCollectionContext
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
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Kryo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Input
import|;
end_import

begin_class
specifier|public
class|class
name|FSStatsAggregator
implements|implements
name|StatsAggregator
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
name|statsList
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|connect
parameter_list|(
name|StatsCollectionContext
name|scc
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|statsDirs
init|=
name|scc
operator|.
name|getStatsTmpDirs
argument_list|()
decl_stmt|;
assert|assert
name|statsDirs
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|:
literal|"Found multiple stats dirs: "
operator|+
name|statsDirs
assert|;
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|statsDirs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"About to read stats from {}"
argument_list|,
name|statsDir
argument_list|)
expr_stmt|;
name|int
name|poolSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|scc
operator|.
name|getHiveConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_MOVE_FILES_THREAD_COUNT
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
empty_stmt|;
specifier|final
name|List
argument_list|<
name|Future
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|futureList
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|statsDir
operator|.
name|getFileSystem
argument_list|(
name|scc
operator|.
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
name|statsList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|FileStatus
index|[]
name|status
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|statsDir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
return|return
name|file
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_FILE_PREFIX
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|statsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|FileStatus
name|file
range|:
name|status
control|)
block|{
name|futureList
operator|.
name|add
argument_list|(
name|pool
operator|.
name|submit
argument_list|(
parameter_list|()
lambda|->
block|{
name|Kryo
name|kryo
init|=
literal|null
decl_stmt|;
try|try
init|(
name|Input
name|in
init|=
operator|new
name|Input
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
init|)
block|{
name|kryo
operator|=
name|SerializationUtilities
operator|.
name|borrowKryo
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|stats
init|=
name|kryo
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
name|statsMap
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"Read stats {}"
argument_list|,
name|stats
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
finally|finally
block|{
name|SerializationUtilities
operator|.
name|releaseKryo
argument_list|(
name|kryo
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Future
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
name|future
range|:
name|futureList
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|stats
init|=
name|future
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|!=
literal|null
condition|)
block|{
name|statsList
operator|.
name|add
argument_list|(
name|stats
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|error
argument_list|(
literal|"Failed to read stats from filesystem "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cancelRunningTasks
argument_list|(
name|futureList
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|cancelRunningTasks
argument_list|(
name|futureList
argument_list|)
expr_stmt|;
comment|//reset interrupt state
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|cancelRunningTasks
parameter_list|(
name|List
argument_list|<
name|Future
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|tasks
parameter_list|)
block|{
for|for
control|(
name|Future
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
name|task
range|:
name|tasks
control|)
block|{
name|task
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|aggregateStats
parameter_list|(
name|String
name|partID
parameter_list|,
name|String
name|statType
parameter_list|)
block|{
name|long
name|counter
init|=
literal|0
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|debug
argument_list|(
literal|"Part ID: {}, {}"
argument_list|,
name|partID
argument_list|,
name|statType
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|statsMap
range|:
name|statsList
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partStat
init|=
name|statsMap
operator|.
name|get
argument_list|(
name|partID
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|partStat
condition|)
block|{
comment|// not all partitions are scanned in all mappers, so this could be null.
continue|continue;
block|}
name|String
name|statVal
init|=
name|partStat
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|statVal
condition|)
block|{
comment|// partition was found, but was empty.
continue|continue;
block|}
name|counter
operator|+=
name|Long
operator|.
name|parseLong
argument_list|(
name|statVal
argument_list|)
expr_stmt|;
block|}
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|info
argument_list|(
literal|"Read stats for {}, {}, {}: "
argument_list|,
name|partID
argument_list|,
name|statType
argument_list|,
name|counter
argument_list|)
expr_stmt|;
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|counter
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|closeConnection
parameter_list|(
name|StatsCollectionContext
name|scc
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|statsDirs
init|=
name|scc
operator|.
name|getStatsTmpDirs
argument_list|()
decl_stmt|;
assert|assert
name|statsDirs
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|:
literal|"Found multiple stats dirs: "
operator|+
name|statsDirs
assert|;
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|statsDirs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"About to delete stats tmp dir :"
operator|+
name|statsDir
argument_list|)
expr_stmt|;
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|statsDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to delete stats dir"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

