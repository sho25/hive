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
name|registry
operator|.
name|impl
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
name|hive
operator|.
name|llap
operator|.
name|registry
operator|.
name|LlapServiceInstance
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
name|base
operator|.
name|Preconditions
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
name|registry
operator|.
name|LlapServiceInstanceSet
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
name|registry
operator|.
name|ServiceRegistry
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
name|registry
operator|.
name|impl
operator|.
name|LlapZookeeperRegistryImpl
operator|.
name|ConfigChangeLockResult
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
name|registry
operator|.
name|ServiceInstanceStateChangeListener
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
name|registry
operator|.
name|client
operator|.
name|binding
operator|.
name|RegistryUtils
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
name|hadoop
operator|.
name|service
operator|.
name|AbstractService
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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

begin_class
specifier|public
class|class
name|LlapRegistryService
extends|extends
name|AbstractService
block|{
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
init|=
literal|"hive.llap.daemon.task.scheduler.enabled.wait.queue.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
init|=
literal|"hive.llap.daemon.num.enabled.executors"
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
name|LlapRegistryService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ServiceRegistry
argument_list|<
name|LlapServiceInstance
argument_list|>
name|registry
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isDaemon
decl_stmt|;
specifier|private
name|boolean
name|isDynamic
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|identity
init|=
literal|"(pending)"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LlapRegistryService
argument_list|>
name|yarnRegistries
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|LlapRegistryService
parameter_list|(
name|boolean
name|isDaemon
parameter_list|)
block|{
name|super
argument_list|(
literal|"LlapRegistryService"
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDaemon
operator|=
name|isDaemon
expr_stmt|;
block|}
comment|/**    * Helper method to get a ServiceRegistry instance to read from the registry.    * This should not be used by LLAP daemons.    *    * @param conf {@link Configuration} instance which contains service registry information.    * @return    */
specifier|public
specifier|static
specifier|synchronized
name|LlapRegistryService
name|getClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|hosts
init|=
name|HiveConf
operator|.
name|getTrimmedVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hosts
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
operator|.
name|toString
argument_list|()
operator|+
literal|" must be defined"
argument_list|)
expr_stmt|;
name|LlapRegistryService
name|registry
decl_stmt|;
if|if
condition|(
name|hosts
operator|.
name|startsWith
argument_list|(
literal|"@"
argument_list|)
condition|)
block|{
comment|// Caching instances only in case of the YARN registry. Each host based list will get it's own copy.
name|String
name|appName
init|=
name|hosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|userName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZK_REGISTRY_USER
argument_list|,
name|currentUser
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|appName
operator|+
literal|"-"
operator|+
name|userName
decl_stmt|;
name|registry
operator|=
name|yarnRegistries
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|registry
operator|==
literal|null
operator|||
operator|!
name|registry
operator|.
name|isInState
argument_list|(
name|STATE
operator|.
name|STARTED
argument_list|)
condition|)
block|{
name|registry
operator|=
operator|new
name|LlapRegistryService
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|registry
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|registry
operator|.
name|start
argument_list|()
expr_stmt|;
name|yarnRegistries
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|registry
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|registry
operator|=
operator|new
name|LlapRegistryService
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|registry
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|registry
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Using LLAP registry (client) type: "
operator|+
name|registry
argument_list|)
expr_stmt|;
return|return
name|registry
return|;
block|}
specifier|public
specifier|static
name|String
name|currentUser
parameter_list|()
block|{
try|try
block|{
return|return
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|hosts
init|=
name|HiveConf
operator|.
name|getTrimmedVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|hosts
operator|.
name|startsWith
argument_list|(
literal|"@"
argument_list|)
condition|)
block|{
name|registry
operator|=
operator|new
name|LlapZookeeperRegistryImpl
argument_list|(
name|hosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDynamic
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|registry
operator|=
operator|new
name|LlapFixedRegistryImpl
argument_list|(
name|hosts
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDynamic
operator|=
literal|false
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Using LLAP registry type "
operator|+
name|registry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|registry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|registry
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isDaemon
condition|)
block|{
name|registerWorker
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|isDaemon
condition|)
block|{
name|unregisterWorker
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|registry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|registry
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Stopping non-existent registry service"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|registerWorker
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|registry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|identity
operator|=
name|this
operator|.
name|registry
operator|.
name|register
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unregisterWorker
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|registry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|registry
operator|.
name|unregister
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateRegistration
parameter_list|(
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|attributes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isDaemon
operator|&&
name|this
operator|.
name|registry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|registry
operator|.
name|updateRegistration
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Locks the Llap Cluster for configuration change for the given time window.    * @param windowStart The beginning of the time window when no other configuration change is allowed.    * @param windowEnd The end of the time window when no other configuration change is allowed.    * @return The result of the change (success if the lock is succeeded, and the next possible    * configuration change time    */
specifier|public
name|ConfigChangeLockResult
name|lockForConfigChange
parameter_list|(
name|long
name|windowStart
parameter_list|,
name|long
name|windowEnd
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|registry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not allowed to call lockForConfigChange before serviceInit"
argument_list|)
throw|;
block|}
if|if
condition|(
name|isDynamic
condition|)
block|{
name|LlapZookeeperRegistryImpl
name|zkRegisty
init|=
operator|(
name|LlapZookeeperRegistryImpl
operator|)
name|registry
decl_stmt|;
return|return
name|zkRegisty
operator|.
name|lockForConfigChange
argument_list|(
name|windowStart
argument_list|,
name|windowEnd
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Acquiring config lock is only allowed for dynamic registries"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|LlapServiceInstanceSet
name|getInstances
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getInstances
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|public
name|LlapServiceInstanceSet
name|getInstances
parameter_list|(
name|long
name|clusterReadyTimeoutMs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|LlapServiceInstanceSet
operator|)
name|this
operator|.
name|registry
operator|.
name|getInstances
argument_list|(
literal|"LLAP"
argument_list|,
name|clusterReadyTimeoutMs
argument_list|)
return|;
block|}
specifier|public
name|void
name|registerStateChangeListener
parameter_list|(
name|ServiceInstanceStateChangeListener
argument_list|<
name|LlapServiceInstance
argument_list|>
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|registry
operator|.
name|registerStateChangeListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
comment|// is the registry dynamic (i.e refreshes?)
specifier|public
name|boolean
name|isDynamic
parameter_list|()
block|{
return|return
name|isDynamic
return|;
block|}
comment|// this is only useful for the daemons to know themselves
specifier|public
name|String
name|getWorkerIdentity
parameter_list|()
block|{
return|return
name|identity
return|;
block|}
specifier|public
name|ApplicationId
name|getApplicationId
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|registry
operator|.
name|getApplicationId
argument_list|()
return|;
block|}
block|}
end_class

end_unit

