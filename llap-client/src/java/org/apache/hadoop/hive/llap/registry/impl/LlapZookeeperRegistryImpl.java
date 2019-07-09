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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|TreeMap
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
name|ChildData
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
name|curator
operator|.
name|utils
operator|.
name|CloseableUtils
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
name|io
operator|.
name|api
operator|.
name|LlapProxy
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
name|LlapServiceInstance
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
name|registry
operator|.
name|impl
operator|.
name|ServiceInstanceBase
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
name|impl
operator|.
name|ZkRegistryBase
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
operator|.
name|ServiceRecordMarshal
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
name|AddressTypes
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
name|ProtocolTypes
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
name|ContainerId
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
name|Resource
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
name|LlapZookeeperRegistryImpl
extends|extends
name|ZkRegistryBase
argument_list|<
name|LlapServiceInstance
argument_list|>
implements|implements
name|ServiceRegistry
argument_list|<
name|LlapServiceInstance
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
name|LlapZookeeperRegistryImpl
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * IPC endpoint names.    */
specifier|private
specifier|static
specifier|final
name|String
name|IPC_SERVICES
init|=
literal|"services"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IPC_MNG
init|=
literal|"llapmng"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IPC_SHUFFLE
init|=
literal|"shuffle"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IPC_LLAP
init|=
literal|"llap"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IPC_OUTPUTFORMAT
init|=
literal|"llapoutputformat"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|NAMESPACE_PREFIX
init|=
literal|"llap-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SLOT_PREFIX
init|=
literal|"slot-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SASL_LOGIN_CONTEXT_NAME
init|=
literal|"LlapZooKeeperClient"
decl_stmt|;
specifier|private
name|SlotZnode
name|slotZnode
decl_stmt|;
specifier|private
name|ServiceRecord
name|daemonZkRecord
decl_stmt|;
comment|// to be used by clients of ServiceRegistry TODO: this is unnecessary
specifier|private
name|DynamicServiceInstanceSet
name|instances
decl_stmt|;
specifier|public
name|LlapZookeeperRegistryImpl
parameter_list|(
name|String
name|instanceName
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|instanceName
argument_list|,
name|conf
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZK_REGISTRY_NAMESPACE
argument_list|)
argument_list|,
name|NAMESPACE_PREFIX
argument_list|,
name|USER_SCOPE_PATH_PREFIX
argument_list|,
name|WORKER_PREFIX
argument_list|,
name|WORKER_GROUP
argument_list|,
name|LlapProxy
operator|.
name|isDaemon
argument_list|()
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
name|LLAP_KERBEROS_PRINCIPAL
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
name|LLAP_KERBEROS_KEYTAB_FILE
argument_list|)
argument_list|,
name|ConfVars
operator|.
name|LLAP_VALIDATE_ACLS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap Zookeeper Registry is enabled with registryid: "
operator|+
name|instanceName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Endpoint
name|getRpcEndpoint
parameter_list|()
block|{
specifier|final
name|int
name|rpcPort
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|)
decl_stmt|;
return|return
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
name|IPC_LLAP
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|rpcPort
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Endpoint
name|getShuffleEndpoint
parameter_list|()
block|{
specifier|final
name|int
name|shufflePort
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_SHUFFLE_PORT
argument_list|)
decl_stmt|;
comment|// HTTP today, but might not be
return|return
name|RegistryTypeUtils
operator|.
name|inetAddrEndpoint
argument_list|(
name|IPC_SHUFFLE
argument_list|,
name|ProtocolTypes
operator|.
name|PROTOCOL_TCP
argument_list|,
name|hostname
argument_list|,
name|shufflePort
argument_list|)
return|;
block|}
specifier|public
name|Endpoint
name|getServicesEndpoint
parameter_list|()
block|{
specifier|final
name|int
name|servicePort
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_PORT
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isSSL
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_SSL
argument_list|)
decl_stmt|;
specifier|final
name|String
name|scheme
init|=
name|isSSL
condition|?
literal|"https"
else|:
literal|"http"
decl_stmt|;
specifier|final
name|URL
name|serviceURL
decl_stmt|;
try|try
block|{
name|serviceURL
operator|=
operator|new
name|URL
argument_list|(
name|scheme
argument_list|,
name|hostname
argument_list|,
name|servicePort
argument_list|,
literal|""
argument_list|)
expr_stmt|;
return|return
name|RegistryTypeUtils
operator|.
name|webEndpoint
argument_list|(
name|IPC_SERVICES
argument_list|,
name|serviceURL
operator|.
name|toURI
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
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
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"llap service URI for "
operator|+
name|hostname
operator|+
literal|" is invalid"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Endpoint
name|getMngEndpoint
parameter_list|()
block|{
return|return
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
name|IPC_MNG
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_MANAGEMENT_RPC_PORT
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Endpoint
name|getOutputFormatEndpoint
parameter_list|()
block|{
return|return
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
name|IPC_OUTPUTFORMAT
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|hostname
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_OUTPUT_SERVICE_PORT
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|register
parameter_list|()
throws|throws
name|IOException
block|{
name|daemonZkRecord
operator|=
operator|new
name|ServiceRecord
argument_list|()
expr_stmt|;
name|Endpoint
name|rpcEndpoint
init|=
name|getRpcEndpoint
argument_list|()
decl_stmt|;
name|daemonZkRecord
operator|.
name|addInternalEndpoint
argument_list|(
name|rpcEndpoint
argument_list|)
expr_stmt|;
name|daemonZkRecord
operator|.
name|addInternalEndpoint
argument_list|(
name|getMngEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|daemonZkRecord
operator|.
name|addInternalEndpoint
argument_list|(
name|getShuffleEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|daemonZkRecord
operator|.
name|addExternalEndpoint
argument_list|(
name|getServicesEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|daemonZkRecord
operator|.
name|addInternalEndpoint
argument_list|(
name|getOutputFormatEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|populateConfigValues
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|capacityValues
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|capacityValues
operator|.
name|put
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
argument_list|,
name|HiveConf
operator|.
name|getVarWithoutType
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|)
argument_list|)
expr_stmt|;
name|capacityValues
operator|.
name|put
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLED_WAIT_QUEUE_SIZE
argument_list|,
name|HiveConf
operator|.
name|getVarWithoutType
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|populateConfigValues
argument_list|(
name|capacityValues
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|uniqueId
init|=
name|registerServiceRecord
argument_list|(
name|daemonZkRecord
argument_list|)
decl_stmt|;
name|long
name|znodeCreationTimeout
init|=
literal|120
decl_stmt|;
comment|// Create a znode under the rootNamespace parent for this instance of the server
try|try
block|{
name|slotZnode
operator|=
operator|new
name|SlotZnode
argument_list|(
name|zooKeeperClient
argument_list|,
name|workersPath
argument_list|,
name|SLOT_PREFIX
argument_list|,
name|WORKER_PREFIX
argument_list|,
name|uniqueId
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|slotZnode
operator|.
name|start
argument_list|(
name|znodeCreationTimeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Max znode creation wait time: "
operator|+
name|znodeCreationTimeout
operator|+
literal|"s exhausted"
argument_list|)
throw|;
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
name|error
argument_list|(
literal|"Unable to create a znode for this server instance"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|CloseableUtils
operator|.
name|closeQuietly
argument_list|(
name|slotZnode
argument_list|)
expr_stmt|;
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
throw|throw
operator|(
name|e
operator|instanceof
name|IOException
operator|)
condition|?
operator|(
name|IOException
operator|)
name|e
else|:
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Registered node. Created a znode on ZooKeeper for LLAP instance: rpc: {}, "
operator|+
literal|"shuffle: {}, webui: {}, mgmt: {}, znodePath: {}"
argument_list|,
name|rpcEndpoint
argument_list|,
name|getShuffleEndpoint
argument_list|()
argument_list|,
name|getServicesEndpoint
argument_list|()
argument_list|,
name|getMngEndpoint
argument_list|()
argument_list|,
name|getRegistrationZnodePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|uniqueId
return|;
block|}
specifier|private
name|void
name|populateConfigValues
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
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kv
range|:
name|attributes
control|)
block|{
if|if
condition|(
name|kv
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|HiveConf
operator|.
name|PREFIX_LLAP
argument_list|)
operator|||
name|kv
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|HiveConf
operator|.
name|PREFIX_HIVE_LLAP
argument_list|)
condition|)
block|{
comment|// TODO: read this somewhere useful, like the task scheduler
name|daemonZkRecord
operator|.
name|set
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
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
name|populateConfigValues
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
name|updateServiceRecord
argument_list|(
name|this
operator|.
name|daemonZkRecord
argument_list|,
name|doCheckAcls
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unregister
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing for the zkCreate models
block|}
specifier|private
class|class
name|DynamicServiceInstance
extends|extends
name|ServiceInstanceBase
implements|implements
name|LlapServiceInstance
block|{
specifier|private
specifier|final
name|int
name|mngPort
decl_stmt|;
specifier|private
specifier|final
name|int
name|shufflePort
decl_stmt|;
specifier|private
specifier|final
name|int
name|outputFormatPort
decl_stmt|;
specifier|private
specifier|final
name|String
name|serviceAddress
decl_stmt|;
specifier|private
specifier|final
name|Resource
name|resource
decl_stmt|;
specifier|public
name|DynamicServiceInstance
parameter_list|(
name|ServiceRecord
name|srv
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|srv
argument_list|,
name|IPC_LLAP
argument_list|)
expr_stmt|;
specifier|final
name|Endpoint
name|shuffle
init|=
name|srv
operator|.
name|getInternalEndpoint
argument_list|(
name|IPC_SHUFFLE
argument_list|)
decl_stmt|;
specifier|final
name|Endpoint
name|mng
init|=
name|srv
operator|.
name|getInternalEndpoint
argument_list|(
name|IPC_MNG
argument_list|)
decl_stmt|;
specifier|final
name|Endpoint
name|outputFormat
init|=
name|srv
operator|.
name|getInternalEndpoint
argument_list|(
name|IPC_OUTPUTFORMAT
argument_list|)
decl_stmt|;
specifier|final
name|Endpoint
name|services
init|=
name|srv
operator|.
name|getExternalEndpoint
argument_list|(
name|IPC_SERVICES
argument_list|)
decl_stmt|;
name|this
operator|.
name|mngPort
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|mng
operator|.
name|addresses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|AddressTypes
operator|.
name|ADDRESS_PORT_FIELD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|shufflePort
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|shuffle
operator|.
name|addresses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|AddressTypes
operator|.
name|ADDRESS_PORT_FIELD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|outputFormatPort
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|outputFormat
operator|.
name|addresses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|AddressTypes
operator|.
name|ADDRESS_PORT_FIELD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|serviceAddress
operator|=
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|services
operator|.
name|addresses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|AddressTypes
operator|.
name|ADDRESS_URI
argument_list|)
expr_stmt|;
name|String
name|memStr
init|=
name|srv
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|String
name|coreStr
init|=
name|srv
operator|.
name|get
argument_list|(
name|LlapRegistryService
operator|.
name|LLAP_DAEMON_NUM_ENABLED_EXECUTORS
argument_list|,
literal|""
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|resource
operator|=
name|Resource
operator|.
name|newInstance
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|memStr
argument_list|)
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|coreStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid resource configuration for a LLAP node: memory "
operator|+
name|memStr
operator|+
literal|", vcores "
operator|+
name|coreStr
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getShufflePort
parameter_list|()
block|{
return|return
name|shufflePort
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServicesAddress
parameter_list|()
block|{
return|return
name|serviceAddress
return|;
block|}
annotation|@
name|Override
specifier|public
name|Resource
name|getResource
parameter_list|()
block|{
return|return
name|resource
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
literal|"DynamicServiceInstance [id="
operator|+
name|getWorkerIdentity
argument_list|()
operator|+
literal|", host="
operator|+
name|getHost
argument_list|()
operator|+
literal|":"
operator|+
name|getRpcPort
argument_list|()
operator|+
literal|" with resources="
operator|+
name|getResource
argument_list|()
operator|+
literal|", shufflePort="
operator|+
name|getShufflePort
argument_list|()
operator|+
literal|", servicesAddress="
operator|+
name|getServicesAddress
argument_list|()
operator|+
literal|", mgmtPort="
operator|+
name|getManagementPort
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getManagementPort
parameter_list|()
block|{
return|return
name|mngPort
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputFormatPort
parameter_list|()
block|{
return|return
name|outputFormatPort
return|;
block|}
block|}
comment|// TODO: this class is completely unnecessary... 1-on-1 mapping with parent.
comment|//       Remains here as the legacy of the original higher-level interface (getInstance).
specifier|private
specifier|static
class|class
name|DynamicServiceInstanceSet
implements|implements
name|LlapServiceInstanceSet
block|{
specifier|private
specifier|final
name|PathChildrenCache
name|instancesCache
decl_stmt|;
specifier|private
specifier|final
name|LlapZookeeperRegistryImpl
name|parent
decl_stmt|;
specifier|private
specifier|final
name|ServiceRecordMarshal
name|encoder
decl_stmt|;
specifier|public
name|DynamicServiceInstanceSet
parameter_list|(
name|PathChildrenCache
name|cache
parameter_list|,
name|LlapZookeeperRegistryImpl
name|parent
parameter_list|,
name|ServiceRecordMarshal
name|encoder
parameter_list|)
block|{
name|this
operator|.
name|instancesCache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|encoder
operator|=
name|encoder
expr_stmt|;
name|parent
operator|.
name|populateCache
argument_list|(
name|instancesCache
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getAll
parameter_list|()
block|{
return|return
name|parent
operator|.
name|getAllInternal
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getAllInstancesOrdered
parameter_list|(
name|boolean
name|consistentIndexes
parameter_list|)
block|{
return|return
name|parent
operator|.
name|getAllInstancesOrdered
argument_list|(
name|consistentIndexes
argument_list|,
name|instancesCache
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|LlapServiceInstance
name|getInstance
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|instances
init|=
name|getAll
argument_list|()
decl_stmt|;
for|for
control|(
name|LlapServiceInstance
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
specifier|public
name|Set
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getByHost
parameter_list|(
name|String
name|host
parameter_list|)
block|{
return|return
name|parent
operator|.
name|getByHostInternal
argument_list|(
name|host
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|parent
operator|.
name|sizeInternal
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ApplicationId
name|getApplicationId
parameter_list|()
block|{
for|for
control|(
name|ChildData
name|childData
range|:
name|instancesCache
operator|.
name|getCurrentData
argument_list|()
control|)
block|{
name|byte
index|[]
name|data
init|=
name|getWorkerData
argument_list|(
name|childData
argument_list|,
name|WORKER_PREFIX
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
continue|continue;
name|ServiceRecord
name|sr
init|=
literal|null
decl_stmt|;
try|try
block|{
name|sr
operator|=
name|encoder
operator|.
name|fromBytes
argument_list|(
name|childData
operator|.
name|getPath
argument_list|()
argument_list|,
name|data
argument_list|)
expr_stmt|;
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
literal|"Unable to decode data for zkpath: {}."
operator|+
literal|" Ignoring from current instances list.."
argument_list|,
name|childData
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|String
name|containerStr
init|=
name|sr
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_CONTAINER_ID
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|containerStr
operator|==
literal|null
operator|||
name|containerStr
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
return|return
name|ContainerId
operator|.
name|fromString
argument_list|(
name|containerStr
argument_list|)
operator|.
name|getApplicationAttemptId
argument_list|()
operator|.
name|getApplicationId
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
name|String
name|extractWorkerIdFromSlot
parameter_list|(
name|ChildData
name|childData
parameter_list|)
block|{
return|return
operator|new
name|String
argument_list|(
name|childData
operator|.
name|getData
argument_list|()
argument_list|,
name|SlotZnode
operator|.
name|CHARSET
argument_list|)
return|;
block|}
comment|// The real implementation for the instanceset... instanceset has its own copy of the
comment|// ZK cache yet completely depends on the parent in every other aspect and is thus unneeded.
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getAllInstancesOrdered
parameter_list|(
name|boolean
name|consistentIndexes
parameter_list|,
name|PathChildrenCache
name|instancesCache
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|slotByWorker
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|LlapServiceInstance
argument_list|>
name|unsorted
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|ChildData
name|childData
range|:
name|instancesCache
operator|.
name|getCurrentData
argument_list|()
control|)
block|{
if|if
condition|(
name|childData
operator|==
literal|null
condition|)
continue|continue;
name|byte
index|[]
name|data
init|=
name|childData
operator|.
name|getData
argument_list|()
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
continue|continue;
name|String
name|nodeName
init|=
name|extractNodeName
argument_list|(
name|childData
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeName
operator|.
name|startsWith
argument_list|(
name|WORKER_PREFIX
argument_list|)
condition|)
block|{
name|LlapServiceInstance
name|instances
init|=
name|getInstanceByPath
argument_list|(
name|childData
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|instances
operator|!=
literal|null
condition|)
block|{
name|unsorted
operator|.
name|add
argument_list|(
name|instances
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|nodeName
operator|.
name|startsWith
argument_list|(
name|SLOT_PREFIX
argument_list|)
condition|)
block|{
name|slotByWorker
operator|.
name|put
argument_list|(
name|extractWorkerIdFromSlot
argument_list|(
name|childData
argument_list|)
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|nodeName
operator|.
name|substring
argument_list|(
name|SLOT_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring unknown node {}"
argument_list|,
name|childData
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|LlapServiceInstance
argument_list|>
name|sorted
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|long
name|maxSlot
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|LlapServiceInstance
name|worker
range|:
name|unsorted
control|)
block|{
name|Long
name|slot
init|=
name|slotByWorker
operator|.
name|get
argument_list|(
name|worker
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|slot
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unknown slot for {}"
argument_list|,
name|worker
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|maxSlot
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxSlot
argument_list|,
name|slot
argument_list|)
expr_stmt|;
name|sorted
operator|.
name|put
argument_list|(
name|slot
argument_list|,
name|worker
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|consistentIndexes
condition|)
block|{
comment|// Add dummy instances to all slots where LLAPs are MIA... I can haz insert_iterator?
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|LlapServiceInstance
argument_list|>
name|dummies
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Long
argument_list|>
name|keyIter
init|=
name|sorted
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|long
name|expected
init|=
literal|0
decl_stmt|;
name|Long
name|ts
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|keyIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Long
name|slot
init|=
name|keyIter
operator|.
name|next
argument_list|()
decl_stmt|;
assert|assert
name|slot
operator|>=
name|expected
assert|;
while|while
condition|(
name|slot
operator|>
name|expected
condition|)
block|{
if|if
condition|(
name|ts
operator|==
literal|null
condition|)
block|{
name|ts
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
comment|// Inactive nodes restart every call!
block|}
name|dummies
operator|.
name|put
argument_list|(
name|expected
argument_list|,
operator|new
name|InactiveServiceInstance
argument_list|(
literal|"inactive-"
operator|+
name|expected
operator|+
literal|"-"
operator|+
name|ts
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|expected
expr_stmt|;
block|}
operator|++
name|expected
expr_stmt|;
block|}
name|sorted
operator|.
name|putAll
argument_list|(
name|dummies
argument_list|)
expr_stmt|;
block|}
return|return
name|sorted
operator|.
name|values
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|extractNodeName
parameter_list|(
name|ChildData
name|childData
parameter_list|)
block|{
name|String
name|nodeName
init|=
name|childData
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|int
name|ix
init|=
name|nodeName
operator|.
name|lastIndexOf
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ix
operator|>=
literal|0
condition|)
block|{
name|nodeName
operator|=
name|nodeName
operator|.
name|substring
argument_list|(
name|ix
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|nodeName
return|;
block|}
annotation|@
name|Override
specifier|public
name|LlapServiceInstanceSet
name|getInstances
parameter_list|(
name|String
name|component
parameter_list|,
name|long
name|clusterReadyTimeoutMs
parameter_list|)
throws|throws
name|IOException
block|{
name|PathChildrenCache
name|instancesCache
init|=
name|ensureInstancesCache
argument_list|(
name|clusterReadyTimeoutMs
argument_list|)
decl_stmt|;
comment|// lazily create instances
if|if
condition|(
name|instances
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|instances
operator|=
operator|new
name|DynamicServiceInstanceSet
argument_list|(
name|instancesCache
argument_list|,
name|this
argument_list|,
name|encoder
argument_list|)
expr_stmt|;
block|}
return|return
name|instances
return|;
block|}
annotation|@
name|Override
specifier|public
name|ApplicationId
name|getApplicationId
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getInstances
argument_list|(
literal|"LLAP"
argument_list|,
literal|0
argument_list|)
operator|.
name|getApplicationId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|CloseableUtils
operator|.
name|closeQuietly
argument_list|(
name|slotZnode
argument_list|)
expr_stmt|;
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|LlapServiceInstance
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
name|DynamicServiceInstance
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
comment|// External LLAP clients would need to set LLAP_ZK_REGISTRY_USER to the LLAP daemon user (hive),
comment|// rather than relying on RegistryUtils.currentUser().
return|return
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
name|RegistryUtils
operator|.
name|currentUser
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

