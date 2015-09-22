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
name|Charset
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
name|Random
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|zookeeper
operator|.
name|Watcher
import|;
end_import

begin_class
class|class
name|ZooKeeperHiveClientHelper
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZooKeeperHiveClientHelper
operator|.
name|class
operator|.
name|getName
argument_list|()
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
comment|/**    * A no-op watcher class    */
specifier|static
class|class
name|DummyWatcher
implements|implements
name|Watcher
block|{
specifier|public
name|void
name|process
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
name|event
parameter_list|)
block|{     }
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
name|String
name|zooKeeperEnsemble
init|=
name|connParams
operator|.
name|getZooKeeperEnsemble
argument_list|()
decl_stmt|;
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
name|zooKeeperNamespace
operator|=
name|JdbcConnectionParams
operator|.
name|ZOOKEEPER_DEFAULT_NAMESPACE
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|serverHosts
decl_stmt|;
name|Random
name|randomizer
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|String
name|serverNode
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
try|try
block|{
name|zooKeeperClient
operator|.
name|start
argument_list|()
expr_stmt|;
name|serverHosts
operator|=
name|zooKeeperClient
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
literal|"/"
operator|+
name|zooKeeperNamespace
argument_list|)
expr_stmt|;
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
literal|"Tried all existing HiveServer2 uris from ZooKeeper."
argument_list|)
throw|;
block|}
comment|// Now pick a server node randomly
name|serverNode
operator|=
name|serverHosts
operator|.
name|get
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|(
name|serverHosts
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setCurrentHostZnodePath
argument_list|(
name|serverNode
argument_list|)
expr_stmt|;
comment|// Read config string from the znode for this server node
name|String
name|serverConfStr
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
literal|"/"
operator|+
name|zooKeeperNamespace
operator|+
literal|"/"
operator|+
name|serverNode
argument_list|)
argument_list|,
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|applyConfs
argument_list|(
name|serverConfStr
argument_list|,
name|connParams
argument_list|)
expr_stmt|;
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
comment|// Close the client connection with ZooKeeper
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
literal|"hive.server2.thrift.bind.host"
argument_list|)
operator|)
operator|&&
operator|(
name|connParams
operator|.
name|getHost
argument_list|()
operator|==
literal|null
operator|)
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
literal|"hive.server2.thrift.port"
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
literal|"/"
operator|+
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
literal|2
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

