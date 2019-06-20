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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|MetastoreConf
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
name|security
operator|.
name|HadoopThriftAuthBridge
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
name|StatsUpdaterThread
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
name|txn
operator|.
name|compactor
operator|.
name|Cleaner
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
name|txn
operator|.
name|compactor
operator|.
name|Initiator
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
name|txn
operator|.
name|compactor
operator|.
name|Worker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Base class for HMS leader config testing.  */
end_comment

begin_class
class|class
name|MetastoreHousekeepingLeaderTestBase
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
name|MetastoreHousekeepingLeaderTestBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|client
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Warehouse
name|warehouse
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isServerStarted
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|int
name|port
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|miniDFS
decl_stmt|;
comment|// How long should we wait for the housekeeping threads to start in ms.
specifier|private
specifier|static
specifier|final
name|long
name|SLEEP_INTERVAL_FOR_THREADS_TO_START
init|=
literal|10000
decl_stmt|;
comment|// Threads using ThreadPool will start after the configured interval. So, start them some time
comment|// before we check the existence of threads.
specifier|private
specifier|static
specifier|final
name|long
name|REMOTE_TASKS_INTERVAL
init|=
name|SLEEP_INTERVAL_FOR_THREADS_TO_START
operator|-
literal|3000
decl_stmt|;
specifier|static
specifier|final
name|String
name|METASTORE_THREAD_TASK_FREQ_CONF
init|=
literal|"metastore.leader.test.task.freq"
decl_stmt|;
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|threadNames
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
name|Map
argument_list|<
name|Class
argument_list|,
name|Boolean
argument_list|>
name|threadClasses
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|void
name|internalSetup
parameter_list|(
specifier|final
name|String
name|leaderHostName
parameter_list|)
throws|throws
name|Exception
block|{
name|MetaStoreTestUtils
operator|.
name|setConfForStandloneMode
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_BIND_HOST
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|METASTORE_HOUSEKEEPING_LEADER_HOSTNAME
argument_list|,
name|leaderHostName
argument_list|)
expr_stmt|;
name|addHouseKeepingThreadConfigs
argument_list|()
expr_stmt|;
name|warehouse
operator|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|isServerStarted
condition|)
block|{
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Unable to connect to the MetaStore server"
argument_list|,
name|client
argument_list|)
expr_stmt|;
return|return;
block|}
name|port
operator|=
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|(
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting MetaStore Server on port "
operator|+
name|port
argument_list|)
expr_stmt|;
name|isServerStarted
operator|=
literal|true
expr_stmt|;
comment|// If the client connects the metastore service has started. This is used as a signal to
comment|// start tests.
name|client
operator|=
name|createClient
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HiveMetaStoreClient
name|createClient
parameter_list|()
throws|throws
name|Exception
block|{
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_URIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|EXECUTE_SET_UGI
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
return|;
block|}
specifier|private
name|void
name|addHouseKeepingThreadConfigs
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setTimeDuration
argument_list|(
name|METASTORE_THREAD_TASK_FREQ_CONF
argument_list|,
name|REMOTE_TASKS_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|addStatsUpdaterThreadConfigs
argument_list|()
expr_stmt|;
name|addReplChangeManagerConfigs
argument_list|()
expr_stmt|;
name|addCompactorConfigs
argument_list|()
expr_stmt|;
name|long
name|numTasks
init|=
name|addRemoteOnlyTasksConfigs
argument_list|()
decl_stmt|;
name|numTasks
operator|=
name|numTasks
operator|+
name|addAlwaysTasksConfigs
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THREAD_POOL_SIZE
argument_list|,
name|numTasks
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addStatsUpdaterThreadConfigs
parameter_list|()
block|{
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|STATS_AUTO_UPDATE_WORKER_COUNT
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|STATS_AUTO_UPDATE
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
name|threadClasses
operator|.
name|put
argument_list|(
name|StatsUpdaterThread
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|put
argument_list|(
name|StatsUpdaterThread
operator|.
name|WORKER_NAME_PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addReplChangeManagerConfigs
parameter_list|()
throws|throws
name|Exception
block|{
name|miniDFS
operator|=
operator|new
name|MiniDFSCluster
operator|.
name|Builder
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|1
argument_list|)
operator|.
name|format
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|cmroot
init|=
literal|"hdfs://"
operator|+
name|miniDFS
operator|.
name|getNameNode
argument_list|()
operator|.
name|getHostAndPort
argument_list|()
operator|+
literal|"/cmroot"
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMDIR
argument_list|,
name|cmroot
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|put
argument_list|(
name|ReplChangeManager
operator|.
name|CM_THREAD_NAME_PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addCompactorConfigs
parameter_list|()
block|{
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|COMPACTOR_INITIATOR_ON
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_METASTORE_RUNWORKER_IN
argument_list|,
literal|"metastore"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|COMPACTOR_WORKER_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|threadClasses
operator|.
name|put
argument_list|(
name|Initiator
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|threadClasses
operator|.
name|put
argument_list|(
name|Worker
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|threadClasses
operator|.
name|put
argument_list|(
name|Cleaner
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|addRemoteOnlyTasksConfigs
parameter_list|()
block|{
name|String
name|remoteTaskClassPaths
init|=
name|RemoteMetastoreTaskThreadTestImpl1
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|","
operator|+
name|RemoteMetastoreTaskThreadTestImpl2
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|TASK_THREADS_REMOTE_ONLY
argument_list|,
name|remoteTaskClassPaths
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|put
argument_list|(
name|RemoteMetastoreTaskThreadTestImpl1
operator|.
name|TASK_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|put
argument_list|(
name|RemoteMetastoreTaskThreadTestImpl2
operator|.
name|TASK_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|2
return|;
block|}
specifier|private
name|long
name|addAlwaysTasksConfigs
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|alwaysTaskClassPaths
init|=
name|MetastoreTaskThreadAlwaysTestImpl
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|TASK_THREADS_ALWAYS
argument_list|,
name|alwaysTaskClassPaths
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|put
argument_list|(
name|MetastoreTaskThreadAlwaysTestImpl
operator|.
name|TASK_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
specifier|private
specifier|static
name|String
name|getAllThreadsAsString
parameter_list|()
block|{
name|Map
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|threadStacks
init|=
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|entry
range|:
name|threadStacks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Thread
name|t
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Name: "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" State: "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getState
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" Class name: "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|void
name|searchHousekeepingThreads
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Client has been created so the metastore has started serving. Sleep for few seconds for
comment|// the housekeeping threads to start.
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL_FOR_THREADS_TO_START
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|getAllThreadsAsString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if all the housekeeping threads have been started.
name|Set
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
comment|// All house keeping threads should be alive.
if|if
condition|(
operator|!
name|thread
operator|.
name|isAlive
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Account for the threads identifiable by their classes.
if|if
condition|(
name|threadClasses
operator|.
name|get
argument_list|(
name|thread
operator|.
name|getClass
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|threadClasses
operator|.
name|put
argument_list|(
name|thread
operator|.
name|getClass
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Account for the threads identifiable by their names
name|String
name|threadName
init|=
name|thread
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|threadName
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|threadName
operator|.
name|startsWith
argument_list|(
name|StatsUpdaterThread
operator|.
name|WORKER_NAME_PREFIX
argument_list|)
condition|)
block|{
name|threadNames
operator|.
name|put
argument_list|(
name|StatsUpdaterThread
operator|.
name|WORKER_NAME_PREFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|threadName
operator|.
name|startsWith
argument_list|(
name|ReplChangeManager
operator|.
name|CM_THREAD_NAME_PREFIX
argument_list|)
condition|)
block|{
name|threadNames
operator|.
name|put
argument_list|(
name|ReplChangeManager
operator|.
name|CM_THREAD_NAME_PREFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|threadNames
operator|.
name|get
argument_list|(
name|threadName
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|threadNames
operator|.
name|put
argument_list|(
name|threadName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

