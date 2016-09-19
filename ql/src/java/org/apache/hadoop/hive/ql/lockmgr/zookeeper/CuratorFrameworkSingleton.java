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
name|lockmgr
operator|.
name|zookeeper
package|;
end_package

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
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|ExponentialBackoffRetry
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
name|util
operator|.
name|ZooKeeperHiveHelper
import|;
end_import

begin_class
specifier|public
class|class
name|CuratorFrameworkSingleton
block|{
specifier|private
specifier|static
name|HiveConf
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|CuratorFramework
name|sharedClient
init|=
literal|null
decl_stmt|;
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"CuratorFrameworkSingleton"
argument_list|)
decl_stmt|;
static|static
block|{
comment|// Add shutdown hook.
name|ShutdownHookManager
operator|.
name|addShutdownHook
argument_list|(
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
name|closeAndReleaseInstance
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|CuratorFramework
name|getInstance
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|sharedClient
operator|==
literal|null
condition|)
block|{
comment|// Create a client instance
if|if
condition|(
name|hiveConf
operator|==
literal|null
condition|)
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|conf
operator|=
name|hiveConf
expr_stmt|;
block|}
name|int
name|sessionTimeout
init|=
operator|(
name|int
operator|)
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|int
name|baseSleepTime
init|=
operator|(
name|int
operator|)
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_BASESLEEPTIME
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|int
name|maxRetries
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_MAX_RETRIES
argument_list|)
decl_stmt|;
name|String
name|quorumServers
init|=
name|ZooKeeperHiveHelper
operator|.
name|getQuorumServers
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|sharedClient
operator|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
name|quorumServers
argument_list|)
operator|.
name|sessionTimeoutMs
argument_list|(
name|sessionTimeout
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|ExponentialBackoffRetry
argument_list|(
name|baseSleepTime
argument_list|,
name|maxRetries
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|sharedClient
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
return|return
name|sharedClient
return|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|void
name|closeAndReleaseInstance
parameter_list|()
block|{
if|if
condition|(
name|sharedClient
operator|!=
literal|null
condition|)
block|{
name|sharedClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|sharedClient
operator|=
literal|null
expr_stmt|;
name|String
name|shutdownMsg
init|=
literal|"Closing ZooKeeper client."
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|shutdownMsg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

