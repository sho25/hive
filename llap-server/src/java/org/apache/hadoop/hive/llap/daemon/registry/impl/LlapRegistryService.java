begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|daemon
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
name|Inet4Address
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
name|UnknownHostException
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
name|llap
operator|.
name|daemon
operator|.
name|LlapDaemonConfiguration
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
name|LlapDaemon
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
name|net
operator|.
name|NetUtils
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
name|log4j
operator|.
name|Logger
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
name|Logger
operator|.
name|getLogger
argument_list|(
name|LlapRegistryService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|SERVICE_CLASS
init|=
literal|"org-apache-hive"
decl_stmt|;
specifier|private
name|RegistryOperationsService
name|client
decl_stmt|;
specifier|private
name|String
name|instanceName
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ServiceRecordMarshal
name|encoder
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|hostname
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
name|LlapRegistryService
parameter_list|()
block|{
name|super
argument_list|(
literal|"LlapRegistryService"
argument_list|)
expr_stmt|;
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
name|registryId
init|=
name|conf
operator|.
name|getTrimmed
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|registryId
operator|.
name|startsWith
argument_list|(
literal|"@"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap Registry is enabled with registryid: "
operator|+
name|registryId
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
name|instanceName
operator|=
name|registryId
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
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
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap Registry is disabled"
argument_list|)
expr_stmt|;
block|}
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
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
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
specifier|public
name|Endpoint
name|getRpcEndpoint
parameter_list|()
block|{
specifier|final
name|int
name|rpcPort
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_PORT_DEFAULT
argument_list|)
decl_stmt|;
return|return
name|RegistryTypeUtils
operator|.
name|ipcEndpoint
argument_list|(
literal|"llap"
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
name|conf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_YARN_SHUFFLE_PORT
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_YARN_SHUFFLE_PORT_DEFAULT
argument_list|)
decl_stmt|;
comment|// HTTP today, but might not be
return|return
name|RegistryTypeUtils
operator|.
name|inetAddrEndpoint
argument_list|(
literal|"shuffle"
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
specifier|private
specifier|final
name|String
name|getPath
parameter_list|()
block|{
return|return
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
return|;
block|}
specifier|public
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
name|client
operator|!=
literal|null
condition|)
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
name|getShuffleEndpoint
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
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_PREFIX
argument_list|)
condition|)
block|{
comment|// TODO: read this somewhere useful, like the allocator
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
block|}
specifier|public
name|void
name|unregisterWorker
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|client
operator|!=
literal|null
condition|)
block|{
comment|// with ephemeral nodes, there's nothing to do here
comment|// because the create didn't return paths
block|}
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceRecord
argument_list|>
name|getWorkers
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|client
operator|!=
literal|null
condition|)
block|{
name|String
name|path
init|=
name|getPath
argument_list|()
decl_stmt|;
return|return
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
block|}
end_class

end_unit

