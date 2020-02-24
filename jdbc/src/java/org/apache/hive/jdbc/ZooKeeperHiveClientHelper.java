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
name|hive
operator|.
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

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
name|curator
operator|.
name|utils
operator|.
name|ZKPaths
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
name|hive
operator|.
name|jdbc
operator|.
name|Utils
operator|.
name|JdbcConnectionParams
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
name|service
operator|.
name|server
operator|.
name|HS2ActivePassiveHARegistry
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
name|service
operator|.
name|server
operator|.
name|HS2ActivePassiveHARegistryClient
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
name|service
operator|.
name|server
operator|.
name|HiveServer2Instance
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
name|Joiner
import|;
end_import

begin_class
class|class
name|ZooKeeperHiveClientHelper
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ZooKeeperHiveClientHelper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Pattern for key1=value1;key2=value2
specifier|private
specifier|static
specifier|final
name|Pattern
name|kvPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^=;]*)=([^;]*)[;]?"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|getZooKeeperNamespace
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
block|{
name|String
name|zooKeeperNamespace
init|=
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|ZOOKEEPER_NAMESPACE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|zooKeeperNamespace
operator|==
literal|null
operator|)
operator|||
operator|(
name|zooKeeperNamespace
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// if active passive HA enabled, use default HA namespace
if|if
condition|(
name|isZkHADynamicDiscoveryMode
argument_list|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
argument_list|)
condition|)
block|{
name|zooKeeperNamespace
operator|=
name|JdbcConnectionParams
operator|.
name|ZOOKEEPER_ACTIVE_PASSIVE_HA_DEFAULT_NAMESPACE
expr_stmt|;
block|}
else|else
block|{
name|zooKeeperNamespace
operator|=
name|JdbcConnectionParams
operator|.
name|ZOOKEEPER_DEFAULT_NAMESPACE
expr_stmt|;
block|}
block|}
return|return
name|zooKeeperNamespace
return|;
block|}
comment|/**    * Returns true is only if HA service discovery mode is enabled    *    * @param sessionConf - session configuration    * @return true if serviceDiscoveryMode=zooKeeperHA is specified in JDBC URI    */
specifier|public
specifier|static
name|boolean
name|isZkHADynamicDiscoveryMode
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionConf
parameter_list|)
block|{
specifier|final
name|String
name|discoveryMode
init|=
name|sessionConf
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|SERVICE_DISCOVERY_MODE
argument_list|)
decl_stmt|;
return|return
operator|(
name|discoveryMode
operator|!=
literal|null
operator|)
operator|&&
name|JdbcConnectionParams
operator|.
name|SERVICE_DISCOVERY_MODE_ZOOKEEPER_HA
operator|.
name|equalsIgnoreCase
argument_list|(
name|discoveryMode
argument_list|)
return|;
block|}
comment|/**    * Returns true is any service discovery mode is enabled (HA or non-HA)    *    * @param sessionConf - session configuration    * @return true if serviceDiscoveryMode is specified in JDBC URI    */
specifier|public
specifier|static
name|boolean
name|isZkDynamicDiscoveryMode
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionConf
parameter_list|)
block|{
specifier|final
name|String
name|discoveryMode
init|=
name|sessionConf
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|SERVICE_DISCOVERY_MODE
argument_list|)
decl_stmt|;
return|return
operator|(
name|discoveryMode
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|JdbcConnectionParams
operator|.
name|SERVICE_DISCOVERY_MODE_ZOOKEEPER
operator|.
name|equalsIgnoreCase
argument_list|(
name|discoveryMode
argument_list|)
operator|||
name|JdbcConnectionParams
operator|.
name|SERVICE_DISCOVERY_MODE_ZOOKEEPER_HA
operator|.
name|equalsIgnoreCase
argument_list|(
name|discoveryMode
argument_list|)
operator|)
return|;
block|}
specifier|private
specifier|static
name|CuratorFramework
name|getZkClient
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|zooKeeperEnsemble
init|=
name|connParams
operator|.
name|getZooKeeperEnsemble
argument_list|()
decl_stmt|;
name|CuratorFramework
name|zooKeeperClient
init|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
name|zooKeeperEnsemble
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|ExponentialBackoffRetry
argument_list|(
literal|1000
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|zooKeeperClient
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|zooKeeperClient
return|;
block|}
comment|/**    * Get a list of all HiveServer2 server hosts.    *    * @param connParams The current JDBC connection parameters    * @param zooKeeperClient The client to use to connect to ZooKeeper    * @return A list of HiveServer2 hosts    * @throws ZooKeeperHiveClientException Failed to communicate to ZooKeeper    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getServerHosts
parameter_list|(
specifier|final
name|JdbcConnectionParams
name|connParams
parameter_list|,
specifier|final
name|CuratorFramework
name|zooKeeperClient
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
specifier|final
name|String
name|zookeeperNamespace
init|=
name|getZooKeeperNamespace
argument_list|(
name|connParams
argument_list|)
decl_stmt|;
specifier|final
name|String
name|zkPath
init|=
name|ZKPaths
operator|.
name|makePath
argument_list|(
literal|null
argument_list|,
name|zookeeperNamespace
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|serverHosts
init|=
name|zooKeeperClient
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
name|zkPath
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Discovered HiveServer2 hosts in ZooKeeper [{}]: {}"
argument_list|,
name|zkPath
argument_list|,
name|serverHosts
argument_list|)
expr_stmt|;
if|if
condition|(
name|serverHosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Did not find any HiveServer2 hosts in ZooKeeper [{}]. "
operator|+
literal|"Check that the Hive ZooKeeper namespace is configured correctly."
argument_list|,
name|zkPath
argument_list|)
expr_stmt|;
block|}
comment|// Remove the znodes we've already tried from this list
name|serverHosts
operator|.
name|removeAll
argument_list|(
name|connParams
operator|.
name|getRejectedHostZnodePaths
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Servers in ZooKeeper after removing rejected: {}"
argument_list|,
name|serverHosts
argument_list|)
expr_stmt|;
return|return
name|serverHosts
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to retrive HS2 host information from ZooKeeper"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|updateParamsWithZKServerNode
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|,
name|CuratorFramework
name|zooKeeperClient
parameter_list|,
name|String
name|serverNode
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|String
name|zooKeeperNamespace
init|=
name|getZooKeeperNamespace
argument_list|(
name|connParams
argument_list|)
decl_stmt|;
name|connParams
operator|.
name|setCurrentHostZnodePath
argument_list|(
name|serverNode
argument_list|)
expr_stmt|;
comment|// Read data from the znode for this server node
comment|// This data could be either config string (new releases) or server end
comment|// point (old releases)
name|String
name|dataStr
init|=
operator|new
name|String
argument_list|(
name|zooKeeperClient
operator|.
name|getData
argument_list|()
operator|.
name|forPath
argument_list|(
name|ZKPaths
operator|.
name|makePath
argument_list|(
literal|null
argument_list|,
name|zooKeeperNamespace
argument_list|,
name|serverNode
argument_list|)
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
comment|// If dataStr is not null and dataStr is not a KV pattern,
comment|// it must be the server uri added by an older version HS2
name|Matcher
name|matcher
init|=
name|kvPattern
operator|.
name|matcher
argument_list|(
name|dataStr
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|dataStr
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|matcher
operator|.
name|find
argument_list|()
operator|)
condition|)
block|{
name|String
index|[]
name|split
init|=
name|dataStr
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to parse HiveServer2 URI from ZooKeeper data: "
operator|+
name|dataStr
argument_list|)
throw|;
block|}
name|connParams
operator|.
name|setHost
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setPort
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|split
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|applyConfs
argument_list|(
name|dataStr
argument_list|,
name|connParams
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|configureConnParams
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
if|if
condition|(
name|isZkHADynamicDiscoveryMode
argument_list|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
argument_list|)
condition|)
block|{
name|configureConnParamsHA
argument_list|(
name|connParams
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CuratorFramework
name|zooKeeperClient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zooKeeperClient
operator|=
name|getZkClient
argument_list|(
name|connParams
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|serverHosts
init|=
name|getServerHosts
argument_list|(
name|connParams
argument_list|,
name|zooKeeperClient
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverHosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"No more HiveServer2 URIs from ZooKeeper to attempt"
argument_list|)
throw|;
block|}
comment|// Pick a server node randomly
specifier|final
name|String
name|serverNode
init|=
name|serverHosts
operator|.
name|get
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|serverHosts
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|updateParamsWithZKServerNode
argument_list|(
name|connParams
argument_list|,
name|zooKeeperClient
argument_list|,
name|serverNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperHiveClientException
name|zkhce
parameter_list|)
block|{
throw|throw
name|zkhce
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to read HiveServer2 configs from ZooKeeper"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|zooKeeperClient
operator|!=
literal|null
condition|)
block|{
name|zooKeeperClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|configureConnParamsHA
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
try|try
block|{
name|Configuration
name|registryConf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|registryConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
operator|.
name|varname
argument_list|,
name|connParams
operator|.
name|getZooKeeperEnsemble
argument_list|()
argument_list|)
expr_stmt|;
name|registryConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ACTIVE_PASSIVE_HA_REGISTRY_NAMESPACE
operator|.
name|varname
argument_list|,
name|getZooKeeperNamespace
argument_list|(
name|connParams
argument_list|)
argument_list|)
expr_stmt|;
name|HS2ActivePassiveHARegistry
name|haRegistryClient
init|=
name|HS2ActivePassiveHARegistryClient
operator|.
name|getClient
argument_list|(
name|registryConf
argument_list|)
decl_stmt|;
name|boolean
name|foundLeader
init|=
literal|false
decl_stmt|;
name|String
name|maxRetriesConf
init|=
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|RETRIES
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxRetries
init|=
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|maxRetriesConf
argument_list|)
condition|?
literal|5
else|:
name|Integer
operator|.
name|parseInt
argument_list|(
name|maxRetriesConf
argument_list|)
decl_stmt|;
name|int
name|retries
init|=
literal|0
decl_stmt|;
name|int
name|sleepMs
init|=
literal|1000
decl_stmt|;
while|while
condition|(
operator|!
name|foundLeader
operator|&&
name|retries
operator|<
name|maxRetries
condition|)
block|{
for|for
control|(
name|HiveServer2Instance
name|hiveServer2Instance
range|:
name|haRegistryClient
operator|.
name|getAll
argument_list|()
control|)
block|{
if|if
condition|(
name|hiveServer2Instance
operator|.
name|isLeader
argument_list|()
condition|)
block|{
name|foundLeader
operator|=
literal|true
expr_stmt|;
name|connParams
operator|.
name|setHost
argument_list|(
name|hiveServer2Instance
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setPort
argument_list|(
name|hiveServer2Instance
operator|.
name|getRpcPort
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|mode
init|=
name|hiveServer2Instance
operator|.
name|getTransportMode
argument_list|()
operator|.
name|equals
argument_list|(
literal|"http"
argument_list|)
condition|?
literal|"http:/"
operator|+
name|hiveServer2Instance
operator|.
name|getHttpEndpoint
argument_list|()
else|:
name|hiveServer2Instance
operator|.
name|getTransportMode
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Found HS2 Active Host: {} Port: {} Identity: {} Mode: {}"
argument_list|,
name|hiveServer2Instance
operator|.
name|getHost
argument_list|()
argument_list|,
name|hiveServer2Instance
operator|.
name|getRpcPort
argument_list|()
argument_list|,
name|hiveServer2Instance
operator|.
name|getWorkerIdentity
argument_list|()
argument_list|,
name|mode
argument_list|)
expr_stmt|;
comment|// configurations are always published to ServiceRecord. Read/apply configs to JDBC connection params
name|String
name|serverConfStr
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|';'
argument_list|)
operator|.
name|withKeyValueSeparator
argument_list|(
literal|"="
argument_list|)
operator|.
name|join
argument_list|(
name|hiveServer2Instance
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"Configurations applied to JDBC connection params. {}"
argument_list|,
name|hiveServer2Instance
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|applyConfs
argument_list|(
name|serverConfStr
argument_list|,
name|connParams
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|foundLeader
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to connect to HS2 Active Host (No Leader Found!). Retrying after {} ms. retries: {}"
argument_list|,
name|sleepMs
argument_list|,
name|retries
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepMs
argument_list|)
expr_stmt|;
name|retries
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|foundLeader
condition|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to connect to HiveServer2 Active host (No leader found!) after"
operator|+
literal|" "
operator|+
name|maxRetries
operator|+
literal|" retries."
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
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to read HiveServer2 configs from ZooKeeper"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|static
name|List
argument_list|<
name|JdbcConnectionParams
argument_list|>
name|getDirectParamsList
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
name|CuratorFramework
name|zooKeeperClient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zooKeeperClient
operator|=
name|getZkClient
argument_list|(
name|connParams
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|serverHosts
init|=
name|getServerHosts
argument_list|(
name|connParams
argument_list|,
name|zooKeeperClient
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverHosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"No more HiveServer2 URIs from ZooKeeper to attempt"
argument_list|)
throw|;
block|}
specifier|final
name|List
argument_list|<
name|JdbcConnectionParams
argument_list|>
name|directParamsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|serverHosts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|serverNode
range|:
name|serverHosts
control|)
block|{
name|JdbcConnectionParams
name|directConnParams
init|=
operator|new
name|JdbcConnectionParams
argument_list|(
name|connParams
argument_list|)
decl_stmt|;
name|directParamsList
operator|.
name|add
argument_list|(
name|directConnParams
argument_list|)
expr_stmt|;
name|updateParamsWithZKServerNode
argument_list|(
name|directConnParams
argument_list|,
name|zooKeeperClient
argument_list|,
name|serverNode
argument_list|)
expr_stmt|;
block|}
return|return
name|directParamsList
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
literal|"Unable to read HiveServer2 configs from ZooKeeper"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|zooKeeperClient
operator|!=
literal|null
condition|)
block|{
name|zooKeeperClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Apply configs published by the server. Configs specified from client's JDBC URI override    * configs published by the server.    *    * @param serverConfStr    * @param connParams    * @throws Exception    */
specifier|private
specifier|static
name|void
name|applyConfs
parameter_list|(
name|String
name|serverConfStr
parameter_list|,
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|Exception
block|{
name|Matcher
name|matcher
init|=
name|kvPattern
operator|.
name|matcher
argument_list|(
name|serverConfStr
argument_list|)
decl_stmt|;
while|while
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
comment|// Have to use this if-else since switch-case on String is supported Java 7 onwards
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|!=
literal|null
operator|)
condition|)
block|{
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Null config value for: "
operator|+
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|+
literal|" published by the server."
argument_list|)
throw|;
block|}
comment|// Set host
if|if
condition|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.thrift.bind.host"
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|setHost
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set transportMode
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.transport.mode"
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|TRANSPORT_MODE
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|TRANSPORT_MODE
argument_list|,
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set port
if|if
condition|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.thrift.port"
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|setPort
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.thrift.http.port"
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getPort
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|connParams
operator|.
name|setPort
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set sasl qop
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.thrift.sasl.qop"
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_QOP
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_QOP
argument_list|,
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set http path
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.thrift.http.path"
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|HTTP_PATH
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|HTTP_PATH
argument_list|,
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set SSL
if|if
condition|(
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.use.SSL"
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|USE_SSL
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|USE_SSL
argument_list|,
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**          * Note: this is pretty messy, but sticking to the current implementation.          * Set authentication configs. Note that in JDBC driver, we have 3 auth modes: NOSASL,          * Kerberos (including delegation token mechanism) and password based.          * The use of JdbcConnectionParams.AUTH_TYPE==JdbcConnectionParams.AUTH_SIMPLE picks NOSASL.          * The presence of JdbcConnectionParams.AUTH_PRINCIPAL==<principal> picks Kerberos.          * If principal is absent, the presence of          * JdbcConnectionParams.AUTH_TYPE==JdbcConnectionParams.AUTH_TOKEN uses delegation token.          * Otherwise password based (which includes NONE, PAM, LDAP, CUSTOM)          */
if|if
condition|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hive.server2.authentication"
argument_list|)
condition|)
block|{
comment|// NOSASL
if|if
condition|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NOSASL"
argument_list|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TYPE
argument_list|)
operator|&&
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TYPE
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_SIMPLE
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TYPE
argument_list|,
name|JdbcConnectionParams
operator|.
name|AUTH_SIMPLE
argument_list|)
expr_stmt|;
block|}
block|}
comment|// KERBEROS
comment|// If delegation token is passed from the client side, do not set the principal
if|if
condition|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"hive.server2.authentication.kerberos.principal"
argument_list|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TYPE
argument_list|)
operator|&&
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|get
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TYPE
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_TOKEN
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|containsKey
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_PRINCIPAL
argument_list|)
operator|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|JdbcConnectionParams
operator|.
name|AUTH_PRINCIPAL
argument_list|,
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

