begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|ServiceInstanceSet
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
name|service
operator|.
name|AbstractService
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
name|name
init|=
name|hosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|yarnRegistries
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|registry
operator|=
name|yarnRegistries
operator|.
name|get
argument_list|(
name|name
argument_list|)
expr_stmt|;
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
name|yarnRegistries
operator|.
name|put
argument_list|(
name|name
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
name|LlapYarnRegistryImpl
argument_list|(
name|hosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
name|conf
argument_list|,
name|isDaemon
argument_list|)
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
name|ServiceInstanceSet
name|getInstances
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|registry
operator|.
name|getInstances
argument_list|(
literal|"LLAP"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

