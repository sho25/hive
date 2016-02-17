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
name|org
operator|.
name|apache
operator|.
name|hadoop
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
name|util
operator|.
name|StringUtils
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
name|LlapFixedRegistryImpl
implements|implements
name|ServiceRegistry
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
name|LlapFixedRegistryImpl
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|// This is primarily for testing to avoid the host lookup
specifier|public
specifier|static
specifier|final
name|String
name|FIXED_REGISTRY_RESOLVE_HOST_NAMES
init|=
literal|"fixed.registry.resolve.host.names"
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|int
name|shuffle
decl_stmt|;
specifier|private
specifier|final
name|int
name|mngPort
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|hosts
decl_stmt|;
specifier|private
specifier|final
name|int
name|memory
decl_stmt|;
specifier|private
specifier|final
name|int
name|vcores
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|resolveHosts
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|srv
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|LlapFixedRegistryImpl
parameter_list|(
name|String
name|hosts
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|hosts
operator|=
name|hosts
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|this
operator|.
name|port
operator|=
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
expr_stmt|;
name|this
operator|.
name|shuffle
operator|=
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
expr_stmt|;
name|this
operator|.
name|resolveHosts
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|FIXED_REGISTRY_RESOLVE_HOST_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|mngPort
operator|=
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
name|put
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
name|this
operator|.
name|memory
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|)
expr_stmt|;
name|this
operator|.
name|vcores
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// nothing to start
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// nothing to stop
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
comment|// nothing to register
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
comment|// nothing to unregister
block|}
specifier|public
specifier|static
name|String
name|getWorkerIdentity
parameter_list|(
name|String
name|host
parameter_list|)
block|{
comment|// trigger clean errors for anyone who mixes up identity with hosts
return|return
literal|"host-"
operator|+
name|host
return|;
block|}
specifier|private
specifier|final
class|class
name|FixedServiceInstance
implements|implements
name|ServiceInstance
block|{
specifier|private
specifier|final
name|String
name|host
decl_stmt|;
specifier|public
name|FixedServiceInstance
parameter_list|(
name|String
name|host
parameter_list|)
block|{
if|if
condition|(
name|resolveHosts
condition|)
block|{
try|try
block|{
name|InetAddress
name|inetAddress
init|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|NetUtils
operator|.
name|isLocalAddress
argument_list|(
name|inetAddress
argument_list|)
condition|)
block|{
name|InetSocketAddress
name|socketAddress
init|=
operator|new
name|InetSocketAddress
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|socketAddress
operator|=
name|NetUtils
operator|.
name|getConnectAddress
argument_list|(
name|socketAddress
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding host identified as local: "
operator|+
name|host
operator|+
literal|" as "
operator|+
name|socketAddress
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|host
operator|=
name|socketAddress
operator|.
name|getHostName
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring resolution issues for host: "
operator|+
name|host
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|host
operator|=
name|host
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
name|LlapFixedRegistryImpl
operator|.
name|getWorkerIdentity
argument_list|(
name|host
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
comment|// TODO: allow>1 port per host?
return|return
name|LlapFixedRegistryImpl
operator|.
name|this
operator|.
name|port
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
name|LlapFixedRegistryImpl
operator|.
name|this
operator|.
name|mngPort
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
name|LlapFixedRegistryImpl
operator|.
name|this
operator|.
name|shuffle
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
literal|true
return|;
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|srv
argument_list|)
decl_stmt|;
comment|// no worker identity
return|return
name|properties
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
name|Resource
operator|.
name|newInstance
argument_list|(
name|memory
argument_list|,
name|vcores
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
literal|"FixedServiceInstance{"
operator|+
literal|"host="
operator|+
name|host
operator|+
literal|", memory="
operator|+
name|memory
operator|+
literal|", vcores="
operator|+
name|vcores
operator|+
literal|'}'
return|;
block|}
block|}
specifier|private
specifier|final
class|class
name|FixedServiceInstanceSet
implements|implements
name|ServiceInstanceSet
block|{
comment|// LinkedHashMap have a repeatable iteration order.
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
specifier|public
name|FixedServiceInstanceSet
parameter_list|()
block|{
for|for
control|(
name|String
name|host
range|:
name|hosts
control|)
block|{
comment|// trigger bugs in anyone who uses this as a hostname
name|instances
operator|.
name|put
argument_list|(
name|getWorkerIdentity
argument_list|(
name|host
argument_list|)
argument_list|,
operator|new
name|FixedServiceInstance
argument_list|(
name|host
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
return|return
name|instances
return|;
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
return|return
name|instances
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
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
name|ServiceInstance
name|inst
init|=
name|getInstance
argument_list|(
name|getWorkerIdentity
argument_list|(
name|host
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|inst
operator|!=
literal|null
condition|)
block|{
name|byHost
operator|.
name|add
argument_list|(
name|inst
argument_list|)
expr_stmt|;
block|}
return|return
name|byHost
return|;
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
comment|// I will do no such thing
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
return|return
operator|new
name|FixedServiceInstanceSet
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
name|String
operator|.
name|format
argument_list|(
literal|"FixedRegistry hosts=%s"
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|this
operator|.
name|hosts
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

