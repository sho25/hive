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
name|net
operator|.
name|InetAddress
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
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|ScheduledExecutorService
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
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|ServiceInstance
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
name|registry
operator|.
name|client
operator|.
name|api
operator|.
name|RegistryOperationsFactory
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
name|RegistryPathUtils
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
name|impl
operator|.
name|zk
operator|.
name|RegistryOperationsService
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
name|Resource
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
name|conf
operator|.
name|YarnConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|LlapYarnRegistryImpl
implements|implements
name|ServiceRegistry
block|{
comment|/** IPC endpoint names. */
specifier|private
specifier|static
specifier|final
name|String
name|IPC_SERVICES
init|=
literal|"services"
decl_stmt|,
name|IPC_MNG
init|=
literal|"llapmng"
decl_stmt|,
name|IPC_SHUFFLE
init|=
literal|"shuffle"
decl_stmt|,
name|IPC_LLAP
init|=
literal|"llap"
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
name|LlapYarnRegistryImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegistryOperationsService
name|client
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ServiceRecordMarshal
name|encoder
decl_stmt|;
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
specifier|private
specifier|final
name|DynamicServiceInstanceSet
name|instances
init|=
operator|new
name|DynamicServiceInstanceSet
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|UUID
name|uniq
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|UNIQUE_IDENTIFIER
init|=
literal|"llap.unique.id"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SERVICE_CLASS
init|=
literal|"org-apache-hive"
decl_stmt|;
specifier|final
name|ScheduledExecutorService
name|refresher
init|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
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
literal|"LlapYarnRegistryRefresher"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|refreshDelay
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isDaemon
decl_stmt|;
static|static
block|{
name|String
name|localhost
init|=
literal|"localhost"
decl_stmt|;
try|try
block|{
name|localhost
operator|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getCanonicalHostName
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|uhe
parameter_list|)
block|{
comment|// ignore
block|}
name|hostname
operator|=
name|localhost
expr_stmt|;
block|}
specifier|public
name|LlapYarnRegistryImpl
parameter_list|(
name|String
name|instanceName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|isDaemon
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap Registry is enabled with registryid: "
operator|+
name|instanceName
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
name|YarnConfiguration
operator|.
name|YARN_SITE_CONFIGURATION_FILE
argument_list|)
expr_stmt|;
comment|// registry reference
name|client
operator|=
operator|(
name|RegistryOperationsService
operator|)
name|RegistryOperationsFactory
operator|.
name|createInstance
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|encoder
operator|=
operator|new
name|RegistryUtils
operator|.
name|ServiceRecordMarshal
argument_list|()
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|RegistryPathUtils
operator|.
name|join
argument_list|(
name|RegistryUtils
operator|.
name|componentPath
argument_list|(
name|RegistryUtils
operator|.
name|currentUser
argument_list|()
argument_list|,
name|SERVICE_CLASS
argument_list|,
name|instanceName
argument_list|,
literal|"workers"
argument_list|)
argument_list|,
literal|"worker-"
argument_list|)
expr_stmt|;
name|refreshDelay
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_REFRESH_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDaemon
operator|=
name|isDaemon
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|refreshDelay
operator|>
literal|0
argument_list|,
literal|"Refresh delay for registry has to be positive = %d"
argument_list|,
name|refreshDelay
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
specifier|private
specifier|final
name|String
name|getPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|register
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|path
init|=
name|getPath
argument_list|()
decl_stmt|;
name|ServiceRecord
name|srv
init|=
operator|new
name|ServiceRecord
argument_list|()
decl_stmt|;
name|srv
operator|.
name|addInternalEndpoint
argument_list|(
name|getRpcEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|srv
operator|.
name|addInternalEndpoint
argument_list|(
name|getMngEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|srv
operator|.
name|addInternalEndpoint
argument_list|(
name|getShuffleEndpoint
argument_list|()
argument_list|)
expr_stmt|;
name|srv
operator|.
name|addExternalEndpoint
argument_list|(
name|getServicesEndpoint
argument_list|()
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|conf
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
name|srv
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
comment|// restart sensitive instance id
name|srv
operator|.
name|set
argument_list|(
name|UNIQUE_IDENTIFIER
argument_list|,
name|uniq
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|mknode
argument_list|(
name|RegistryPathUtils
operator|.
name|parentOf
argument_list|(
name|path
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// FIXME: YARN registry needs to expose Ephemeral_Seq nodes& return the paths
name|client
operator|.
name|zkCreate
argument_list|(
name|path
argument_list|,
name|CreateMode
operator|.
name|EPHEMERAL_SEQUENTIAL
argument_list|,
name|encoder
operator|.
name|toBytes
argument_list|(
name|srv
argument_list|)
argument_list|,
name|client
operator|.
name|getClientAcls
argument_list|()
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
implements|implements
name|ServiceInstance
block|{
specifier|private
specifier|final
name|ServiceRecord
name|srv
decl_stmt|;
specifier|private
name|boolean
name|alive
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|String
name|host
decl_stmt|;
specifier|private
specifier|final
name|int
name|rpcPort
decl_stmt|;
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
specifier|public
name|DynamicServiceInstance
parameter_list|(
name|ServiceRecord
name|srv
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|srv
operator|=
name|srv
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
name|rpc
init|=
name|srv
operator|.
name|getInternalEndpoint
argument_list|(
name|IPC_LLAP
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
name|this
operator|.
name|host
operator|=
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|rpc
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
name|ADDRESS_HOSTNAME_FIELD
argument_list|)
expr_stmt|;
name|this
operator|.
name|rpcPort
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|RegistryTypeUtils
operator|.
name|getAddressField
argument_list|(
name|rpc
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
name|mngPort
operator|=
name|Integer
operator|.
name|valueOf
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
name|valueOf
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
block|}
annotation|@
name|Override
specifier|public
name|String
name|getWorkerIdentity
parameter_list|()
block|{
return|return
name|srv
operator|.
name|get
argument_list|(
name|UNIQUE_IDENTIFIER
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRpcPort
parameter_list|()
block|{
return|return
name|rpcPort
return|;
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
name|boolean
name|isAlive
parameter_list|()
block|{
return|return
name|alive
return|;
block|}
specifier|public
name|void
name|kill
parameter_list|()
block|{
comment|// May be possible to generate a notification back to the scheduler from here.
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing service instance: "
operator|+
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|alive
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProperties
parameter_list|()
block|{
return|return
name|srv
operator|.
name|attributes
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Resource
name|getResource
parameter_list|()
block|{
name|int
name|memory
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|srv
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|vCores
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|srv
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|Resource
operator|.
name|newInstance
argument_list|(
name|memory
argument_list|,
name|vCores
argument_list|)
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
literal|"DynamicServiceInstance [alive="
operator|+
name|alive
operator|+
literal|", host="
operator|+
name|host
operator|+
literal|":"
operator|+
name|rpcPort
operator|+
literal|" with resources="
operator|+
name|getResource
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
comment|// Relying on the identity hashCode and equality, since refreshing instances retains the old copy
comment|// of an already known instance.
block|}
specifier|private
class|class
name|DynamicServiceInstanceSet
implements|implements
name|ServiceInstanceSet
block|{
comment|// LinkedHashMap to retain iteration order.
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceInstance
argument_list|>
name|instances
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantReadWriteLock
operator|.
name|ReadLock
name|readLock
init|=
name|lock
operator|.
name|readLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantReadWriteLock
operator|.
name|WriteLock
name|writeLock
init|=
name|lock
operator|.
name|writeLock
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceInstance
argument_list|>
name|getAll
parameter_list|()
block|{
comment|// Return a copy. Instances may be modified during a refresh.
name|readLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|instances
argument_list|)
return|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServiceInstance
argument_list|>
name|getAllInstancesOrdered
parameter_list|()
block|{
name|List
argument_list|<
name|ServiceInstance
argument_list|>
name|list
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|readLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|list
operator|.
name|addAll
argument_list|(
name|instances
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
operator|new
name|Comparator
argument_list|<
name|ServiceInstance
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|ServiceInstance
name|o1
parameter_list|,
name|ServiceInstance
name|o2
parameter_list|)
block|{
return|return
name|o2
operator|.
name|getWorkerIdentity
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServiceInstance
name|getInstance
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|readLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|instances
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|refresh
parameter_list|()
throws|throws
name|IOException
block|{
comment|/* call this from wherever */
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceInstance
argument_list|>
name|freshInstances
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ServiceInstance
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|path
init|=
name|getPath
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceRecord
argument_list|>
name|records
init|=
name|RegistryUtils
operator|.
name|listServiceRecords
argument_list|(
name|client
argument_list|,
name|RegistryPathUtils
operator|.
name|parentOf
argument_list|(
name|path
argument_list|)
argument_list|)
decl_stmt|;
comment|// Synchronize after reading the service records from the external service (ZK)
name|writeLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|latestKeys
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting to refresh ServiceInstanceSet "
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ServiceRecord
name|rec
range|:
name|records
operator|.
name|values
argument_list|()
control|)
block|{
name|ServiceInstance
name|instance
init|=
operator|new
name|DynamicServiceInstance
argument_list|(
name|rec
argument_list|)
decl_stmt|;
if|if
condition|(
name|instance
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|instances
operator|!=
literal|null
operator|&&
name|instances
operator|.
name|containsKey
argument_list|(
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// add a new object
name|freshInstances
operator|.
name|put
argument_list|(
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|,
name|instance
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding new worker "
operator|+
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
operator|+
literal|" which mapped to "
operator|+
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
literal|"Retaining running worker "
operator|+
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
operator|+
literal|" which mapped to "
operator|+
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|latestKeys
operator|.
name|add
argument_list|(
name|instance
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|instances
operator|!=
literal|null
condition|)
block|{
comment|// deep-copy before modifying
name|Set
argument_list|<
name|String
argument_list|>
name|oldKeys
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|instances
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldKeys
operator|.
name|removeAll
argument_list|(
name|latestKeys
argument_list|)
condition|)
block|{
comment|// This is all the records which have not checked in, and are effectively dead.
for|for
control|(
name|String
name|k
range|:
name|oldKeys
control|)
block|{
comment|// this is so that people can hold onto ServiceInstance references as placeholders for tasks
specifier|final
name|DynamicServiceInstance
name|dead
init|=
operator|(
name|DynamicServiceInstance
operator|)
name|instances
operator|.
name|get
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|dead
operator|.
name|kill
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting dead worker "
operator|+
name|k
operator|+
literal|" which mapped to "
operator|+
name|dead
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// oldKeys contains the set of dead instances at this point.
name|this
operator|.
name|instances
operator|.
name|keySet
argument_list|()
operator|.
name|removeAll
argument_list|(
name|oldKeys
argument_list|)
expr_stmt|;
name|this
operator|.
name|instances
operator|.
name|putAll
argument_list|(
name|freshInstances
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|instances
operator|.
name|putAll
argument_list|(
name|freshInstances
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|writeLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|ServiceInstance
argument_list|>
name|getByHost
parameter_list|(
name|String
name|host
parameter_list|)
block|{
comment|// TODO Maybe store this as a map which is populated during construction, to avoid walking
comment|// the map on each request.
name|readLock
operator|.
name|lock
argument_list|()
expr_stmt|;
name|Set
argument_list|<
name|ServiceInstance
argument_list|>
name|byHost
init|=
operator|new
name|HashSet
argument_list|<
name|ServiceInstance
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|ServiceInstance
name|i
range|:
name|instances
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|host
operator|.
name|equals
argument_list|(
name|i
operator|.
name|getHost
argument_list|()
argument_list|)
condition|)
block|{
comment|// all hosts in instances should be alive in this impl
name|byHost
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
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
literal|"Locality comparing "
operator|+
name|host
operator|+
literal|" to "
operator|+
name|i
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"Returning "
operator|+
name|byHost
operator|.
name|size
argument_list|()
operator|+
literal|" hosts for locality allocation on "
operator|+
name|host
argument_list|)
expr_stmt|;
block|}
return|return
name|byHost
return|;
block|}
finally|finally
block|{
name|readLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|ServiceInstanceSet
name|getInstances
parameter_list|(
name|String
name|component
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
literal|"LLAP"
operator|.
name|equals
argument_list|(
name|component
argument_list|)
argument_list|)
expr_stmt|;
comment|// right now there is only 1 component
if|if
condition|(
name|this
operator|.
name|client
operator|!=
literal|null
condition|)
block|{
name|instances
operator|.
name|refresh
argument_list|()
expr_stmt|;
return|return
name|instances
return|;
block|}
else|else
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|this
operator|.
name|client
argument_list|,
literal|"Yarn registry client is not intialized"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
return|return;
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|isDaemon
condition|)
return|return;
name|refresher
operator|.
name|scheduleWithFixedDelay
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
try|try
block|{
name|instances
operator|.
name|refresh
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not refresh hosts during scheduled refresh"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
literal|0
argument_list|,
name|refreshDelay
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

