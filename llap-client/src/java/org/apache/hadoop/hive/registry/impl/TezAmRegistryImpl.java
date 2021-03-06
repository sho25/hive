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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|recipes
operator|.
name|cache
operator|.
name|PathChildrenCache
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
name|impl
operator|.
name|LlapRegistryService
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
name|RegistryTypeUtils
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
name|registry
operator|.
name|client
operator|.
name|types
operator|.
name|Endpoint
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
name|types
operator|.
name|ServiceRecord
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
name|TezAmRegistryImpl
extends|extends
name|ZkRegistryBase
argument_list|<
name|TezAmInstance
argument_list|>
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
name|TezAmRegistryImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|IPC_TEZCLIENT
init|=
literal|"tez-client"
decl_stmt|;
specifier|static
specifier|final
name|String
name|IPC_PLUGIN
init|=
literal|"llap-plugin"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AM_SESSION_ID
init|=
literal|"am.session.id"
decl_stmt|,
name|AM_PLUGIN_TOKEN
init|=
literal|"am.plugin.token"
decl_stmt|,
name|AM_PLUGIN_JOBID
init|=
literal|"am.plugin.jobid"
decl_stmt|,
name|AM_GUARANTEED_COUNT
init|=
literal|"am.guaranteed.count"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|NAMESPACE_PREFIX
init|=
literal|"tez-am-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SASL_LOGIN_CONTEXT_NAME
init|=
literal|"TezAmZooKeeperClient"
decl_stmt|;
specifier|private
specifier|final
name|String
name|registryName
decl_stmt|;
specifier|private
name|ServiceRecord
name|srv
decl_stmt|;
specifier|public
specifier|static
name|TezAmRegistryImpl
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|useSecureZk
parameter_list|)
block|{
name|String
name|amRegistryName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_NAME
argument_list|)
decl_stmt|;
return|return
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|amRegistryName
argument_list|)
condition|?
literal|null
else|:
operator|new
name|TezAmRegistryImpl
argument_list|(
name|amRegistryName
argument_list|,
name|conf
argument_list|,
name|useSecureZk
argument_list|)
return|;
block|}
specifier|private
name|TezAmRegistryImpl
parameter_list|(
name|String
name|instanceName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|useSecureZk
parameter_list|)
block|{
name|super
argument_list|(
name|instanceName
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
name|NAMESPACE_PREFIX
argument_list|,
name|USER_SCOPE_PATH_PREFIX
argument_list|,
name|WORKER_PREFIX
argument_list|,
name|WORKER_GROUP
argument_list|,
name|useSecureZk
condition|?
name|SASL_LOGIN_CONTEXT_NAME
else|:
literal|null
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_PRINCIPAL
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_KEYTAB_FILE
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Always validate ACLs
name|this
operator|.
name|registryName
operator|=
name|instanceName
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"AM Zookeeper Registry is enabled with registryid: "
operator|+
name|instanceName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initializeWithoutRegistering
parameter_list|()
throws|throws
name|IOException
block|{
name|initializeWithoutRegisteringInternal
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|populateCache
parameter_list|(
name|boolean
name|doInvokeListeners
parameter_list|)
throws|throws
name|IOException
block|{
name|PathChildrenCache
name|pcc
init|=
name|ensureInstancesCache
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|populateCache
argument_list|(
name|pcc
argument_list|,
name|doInvokeListeners
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|register
parameter_list|(
name|int
name|amPort
parameter_list|,
name|int
name|pluginPort
parameter_list|,
name|String
name|sessionId
parameter_list|,
name|String
name|serializedToken
parameter_list|,
name|String
name|jobIdForToken
parameter_list|,
name|int
name|guaranteedCount
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|srv
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Already registered with "
operator|+
name|srv
argument_list|)
throw|;
block|}
name|srv
operator|=
operator|new
name|ServiceRecord
argument_list|()
expr_stmt|;
name|Endpoint
name|rpcEndpoint
init|=
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
name|IPC_TEZCLIENT
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|amPort
argument_list|)
argument_list|)
decl_stmt|;
name|srv
operator|.
name|addInternalEndpoint
argument_list|(
name|rpcEndpoint
argument_list|)
expr_stmt|;
name|Endpoint
name|pluginEndpoint
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|pluginPort
operator|>=
literal|0
condition|)
block|{
name|pluginEndpoint
operator|=
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
name|IPC_PLUGIN
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|pluginPort
argument_list|)
argument_list|)
expr_stmt|;
name|srv
operator|.
name|addInternalEndpoint
argument_list|(
name|pluginEndpoint
argument_list|)
expr_stmt|;
block|}
name|srv
operator|.
name|set
argument_list|(
name|AM_SESSION_ID
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|boolean
name|hasToken
init|=
name|serializedToken
operator|!=
literal|null
decl_stmt|;
name|srv
operator|.
name|set
argument_list|(
name|AM_PLUGIN_TOKEN
argument_list|,
name|hasToken
condition|?
name|serializedToken
else|:
literal|""
argument_list|)
expr_stmt|;
name|srv
operator|.
name|set
argument_list|(
name|AM_PLUGIN_JOBID
argument_list|,
name|jobIdForToken
operator|!=
literal|null
condition|?
name|jobIdForToken
else|:
literal|""
argument_list|)
expr_stmt|;
name|srv
operator|.
name|set
argument_list|(
name|AM_GUARANTEED_COUNT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|guaranteedCount
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|uniqueId
init|=
name|registerServiceRecord
argument_list|(
name|srv
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Registered this AM: rpc: {}, plugin: {}, sessionId: {}, token: {}, znodePath: {}"
argument_list|,
name|rpcEndpoint
argument_list|,
name|pluginEndpoint
argument_list|,
name|sessionId
argument_list|,
name|hasToken
argument_list|,
name|getRegistrationZnodePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|uniqueId
return|;
block|}
specifier|public
name|void
name|updateGuaranteed
parameter_list|(
name|int
name|guaranteedCount
parameter_list|)
throws|throws
name|IOException
block|{
name|srv
operator|.
name|set
argument_list|(
name|AM_GUARANTEED_COUNT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|guaranteedCount
argument_list|)
argument_list|)
expr_stmt|;
name|updateServiceRecord
argument_list|(
name|srv
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TezAmInstance
name|getInstance
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Collection
argument_list|<
name|TezAmInstance
argument_list|>
name|instances
init|=
name|getAllInternal
argument_list|()
decl_stmt|;
for|for
control|(
name|TezAmInstance
name|instance
range|:
name|instances
control|)
block|{
if|if
condition|(
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|instance
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|TezAmInstance
name|createServiceInstance
parameter_list|(
name|ServiceRecord
name|srv
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TezAmInstance
argument_list|(
name|srv
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getZkPathUser
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// We assume that AMs and HS2 run under the same user.
return|return
name|LlapRegistryService
operator|.
name|currentUser
argument_list|()
return|;
block|}
specifier|public
name|String
name|getRegistryName
parameter_list|()
block|{
return|return
name|registryName
return|;
block|}
block|}
end_class

end_unit

