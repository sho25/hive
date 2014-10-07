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
name|net
operator|.
name|URI
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
name|sql
operator|.
name|SQLException
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
name|Arrays
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|cli
operator|.
name|thrift
operator|.
name|TStatus
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
name|cli
operator|.
name|thrift
operator|.
name|TStatusCode
import|;
end_import

begin_class
specifier|public
class|class
name|Utils
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Utils
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**     * The required prefix for the connection URL.     */
specifier|public
specifier|static
specifier|final
name|String
name|URL_PREFIX
init|=
literal|"jdbc:hive2://"
decl_stmt|;
comment|/**     * If host is provided, without a port.     */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_PORT
init|=
literal|"10000"
decl_stmt|;
comment|/**    * Hive's default database name    */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_DATABASE
init|=
literal|"default"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|URI_JDBC_PREFIX
init|=
literal|"jdbc:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|URI_HIVE_PREFIX
init|=
literal|"hive2:"
decl_stmt|;
specifier|public
specifier|static
class|class
name|JdbcConnectionParams
block|{
comment|// Note on client side parameter naming convention:
comment|// Prefer using a shorter camelCase param name instead of using the same name as the
comment|// corresponding
comment|// HiveServer2 config.
comment|// For a jdbc url: jdbc:hive2://<host>:<port>/dbName;sess_var_list?hive_conf_list#hive_var_list,
comment|// client side params are specified in sess_var_list
comment|// Client param names:
specifier|static
specifier|final
name|String
name|AUTH_TYPE
init|=
literal|"auth"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_QOP
init|=
literal|"sasl.qop"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_SIMPLE
init|=
literal|"noSasl"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_TOKEN
init|=
literal|"delegationToken"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_USER
init|=
literal|"user"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_PRINCIPAL
init|=
literal|"principal"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_PASSWD
init|=
literal|"password"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_KERBEROS_AUTH_TYPE
init|=
literal|"kerberosAuthType"
decl_stmt|;
specifier|static
specifier|final
name|String
name|AUTH_KERBEROS_AUTH_TYPE_FROM_SUBJECT
init|=
literal|"fromSubject"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ANONYMOUS_USER
init|=
literal|"anonymous"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ANONYMOUS_PASSWD
init|=
literal|"anonymous"
decl_stmt|;
specifier|static
specifier|final
name|String
name|USE_SSL
init|=
literal|"ssl"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SSL_TRUST_STORE
init|=
literal|"sslTrustStore"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SSL_TRUST_STORE_PASSWORD
init|=
literal|"trustStorePassword"
decl_stmt|;
specifier|static
specifier|final
name|String
name|TRANSPORT_MODE
init|=
literal|"hive.server2.transport.mode"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HTTP_PATH
init|=
literal|"hive.server2.thrift.http.path"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SERVICE_DISCOVERY_MODE
init|=
literal|"serviceDiscoveryMode"
decl_stmt|;
comment|// Don't use dynamic serice discovery
specifier|static
specifier|final
name|String
name|SERVICE_DISCOVERY_MODE_NONE
init|=
literal|"none"
decl_stmt|;
comment|// Use ZooKeeper for indirection while using dynamic service discovery
specifier|static
specifier|final
name|String
name|SERVICE_DISCOVERY_MODE_ZOOKEEPER
init|=
literal|"zooKeeper"
decl_stmt|;
specifier|static
specifier|final
name|String
name|ZOOKEEPER_NAMESPACE
init|=
literal|"zooKeeperNamespace"
decl_stmt|;
comment|// Default namespace value on ZooKeeper.
comment|// This value is used if the param "zooKeeperNamespace" is not specified in the JDBC Uri.
specifier|static
specifier|final
name|String
name|ZOOKEEPER_DEFAULT_NAMESPACE
init|=
literal|"hiveserver2"
decl_stmt|;
comment|// Non-configurable params:
comment|// ZOOKEEPER_SESSION_TIMEOUT is not exposed as client configurable
specifier|static
specifier|final
name|int
name|ZOOKEEPER_SESSION_TIMEOUT
init|=
literal|600
operator|*
literal|1000
decl_stmt|;
comment|// Currently supports JKS keystore format
specifier|static
specifier|final
name|String
name|SSL_TRUST_STORE_TYPE
init|=
literal|"JKS"
decl_stmt|;
specifier|private
name|String
name|host
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|String
name|jdbcUriString
decl_stmt|;
specifier|private
name|String
name|dbName
init|=
name|DEFAULT_DATABASE
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfs
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVars
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionVars
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|isEmbeddedMode
init|=
literal|false
decl_stmt|;
specifier|private
name|String
index|[]
name|authorityList
decl_stmt|;
specifier|private
name|String
name|zooKeeperEnsemble
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|currentHostZnodePath
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|rejectedHostZnodePaths
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|JdbcConnectionParams
parameter_list|()
block|{     }
specifier|public
name|String
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|port
return|;
block|}
specifier|public
name|String
name|getJdbcUriString
parameter_list|()
block|{
return|return
name|jdbcUriString
return|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveConfs
parameter_list|()
block|{
return|return
name|hiveConfs
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVars
parameter_list|()
block|{
return|return
name|hiveVars
return|;
block|}
specifier|public
name|boolean
name|isEmbeddedMode
parameter_list|()
block|{
return|return
name|isEmbeddedMode
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSessionVars
parameter_list|()
block|{
return|return
name|sessionVars
return|;
block|}
specifier|public
name|String
index|[]
name|getAuthorityList
parameter_list|()
block|{
return|return
name|authorityList
return|;
block|}
specifier|public
name|String
name|getZooKeeperEnsemble
parameter_list|()
block|{
return|return
name|zooKeeperEnsemble
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getRejectedHostZnodePaths
parameter_list|()
block|{
return|return
name|rejectedHostZnodePaths
return|;
block|}
specifier|public
name|String
name|getCurrentHostZnodePath
parameter_list|()
block|{
return|return
name|currentHostZnodePath
return|;
block|}
specifier|public
name|void
name|setHost
parameter_list|(
name|String
name|host
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
block|}
specifier|public
name|void
name|setPort
parameter_list|(
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
block|}
specifier|public
name|void
name|setJdbcUriString
parameter_list|(
name|String
name|jdbcUriString
parameter_list|)
block|{
name|this
operator|.
name|jdbcUriString
operator|=
name|jdbcUriString
expr_stmt|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
specifier|public
name|void
name|setHiveConfs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfs
parameter_list|)
block|{
name|this
operator|.
name|hiveConfs
operator|=
name|hiveConfs
expr_stmt|;
block|}
specifier|public
name|void
name|setHiveVars
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVars
parameter_list|)
block|{
name|this
operator|.
name|hiveVars
operator|=
name|hiveVars
expr_stmt|;
block|}
specifier|public
name|void
name|setEmbeddedMode
parameter_list|(
name|boolean
name|embeddedMode
parameter_list|)
block|{
name|this
operator|.
name|isEmbeddedMode
operator|=
name|embeddedMode
expr_stmt|;
block|}
specifier|public
name|void
name|setSessionVars
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionVars
parameter_list|)
block|{
name|this
operator|.
name|sessionVars
operator|=
name|sessionVars
expr_stmt|;
block|}
specifier|public
name|void
name|setSuppliedAuthorityList
parameter_list|(
name|String
index|[]
name|authorityList
parameter_list|)
block|{
name|this
operator|.
name|authorityList
operator|=
name|authorityList
expr_stmt|;
block|}
specifier|public
name|void
name|setZooKeeperEnsemble
parameter_list|(
name|String
name|zooKeeperEnsemble
parameter_list|)
block|{
name|this
operator|.
name|zooKeeperEnsemble
operator|=
name|zooKeeperEnsemble
expr_stmt|;
block|}
specifier|public
name|void
name|setCurrentHostZnodePath
parameter_list|(
name|String
name|currentHostZnodePath
parameter_list|)
block|{
name|this
operator|.
name|currentHostZnodePath
operator|=
name|currentHostZnodePath
expr_stmt|;
block|}
block|}
comment|// Verify success or success_with_info status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccessWithInfo
parameter_list|(
name|TStatus
name|status
parameter_list|)
throws|throws
name|SQLException
block|{
name|verifySuccess
argument_list|(
name|status
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Verify success status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccess
parameter_list|(
name|TStatus
name|status
parameter_list|)
throws|throws
name|SQLException
block|{
name|verifySuccess
argument_list|(
name|status
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Verify success and optionally with_info status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccess
parameter_list|(
name|TStatus
name|status
parameter_list|,
name|boolean
name|withInfo
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
operator|(
name|status
operator|.
name|getStatusCode
argument_list|()
operator|!=
name|TStatusCode
operator|.
name|SUCCESS_STATUS
operator|)
operator|&&
operator|(
name|withInfo
operator|&&
operator|(
name|status
operator|.
name|getStatusCode
argument_list|()
operator|!=
name|TStatusCode
operator|.
name|SUCCESS_WITH_INFO_STATUS
operator|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|status
argument_list|)
throw|;
block|}
block|}
comment|/**    * Parse JDBC connection URL    * The new format of the URL is:    * jdbc:hive2://<host1>:<port1>,<host2>:<port2>/dbName;sess_var_list?hive_conf_list#hive_var_list    * where the optional sess, conf and var lists are semicolon separated<key>=<val> pairs.    * For utilizing dynamic service discovery with HiveServer2 multiple comma separated host:port pairs can    * be specified as shown above.    * The JDBC driver resolves the list of uris and picks a specific server instance to connect to.    * Currently, dynamic service discovery using ZooKeeper is supported, in which case the host:port pairs represent a ZooKeeper ensemble.    *    * As before, if the host/port is not specified, it the driver runs an embedded hive.    * examples -    *  jdbc:hive2://ubuntu:11000/db2?hive.cli.conf.printheader=true;hive.exec.mode.local.auto.inputbytes.max=9999#stab=salesTable;icol=customerID    *  jdbc:hive2://?hive.cli.conf.printheader=true;hive.exec.mode.local.auto.inputbytes.max=9999#stab=salesTable;icol=customerID    *  jdbc:hive2://ubuntu:11000/db2;user=foo;password=bar    *    *  Connect to http://server:10001/hs2, with specified basicAuth credentials and initial database:    *  jdbc:hive2://server:10001/db;user=foo;password=bar?hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2    *    * @param uri    * @return    * @throws SQLException    */
specifier|public
specifier|static
name|JdbcConnectionParams
name|parseURL
parameter_list|(
name|String
name|uri
parameter_list|)
throws|throws
name|JdbcUriParseException
throws|,
name|SQLException
throws|,
name|ZooKeeperHiveClientException
block|{
name|JdbcConnectionParams
name|connParams
init|=
operator|new
name|JdbcConnectionParams
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|uri
operator|.
name|startsWith
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|JdbcUriParseException
argument_list|(
literal|"Bad URL format: Missing prefix "
operator|+
name|URL_PREFIX
argument_list|)
throw|;
block|}
comment|// For URLs with no other configuration
comment|// Don't parse them, but set embedded mode as true
if|if
condition|(
name|uri
operator|.
name|equalsIgnoreCase
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|setEmbeddedMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|connParams
return|;
block|}
comment|// The JDBC URI now supports specifying multiple host:port if dynamic service discovery is
comment|// configured on HiveServer2 (like: host1:port1,host2:port2,host3:port3)
comment|// We'll extract the authorities (host:port combo) from the URI, extract session vars, hive
comment|// confs& hive vars by parsing it as a Java URI.
comment|// To parse the intermediate URI as a Java URI, we'll give a dummy authority(dummy:00000).
comment|// Later, we'll substitute the dummy authority for a resolved authority.
name|String
name|dummyAuthorityString
init|=
literal|"dummyhost:00000"
decl_stmt|;
name|String
name|suppliedAuthorities
init|=
name|getAuthorities
argument_list|(
name|uri
argument_list|,
name|connParams
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|suppliedAuthorities
operator|==
literal|null
operator|)
operator|||
operator|(
name|suppliedAuthorities
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// Given uri of the form:
comment|// jdbc:hive2:///dbName;sess_var_list?hive_conf_list#hive_var_list
name|connParams
operator|.
name|setEmbeddedMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Supplied authorities: "
operator|+
name|suppliedAuthorities
argument_list|)
expr_stmt|;
name|String
index|[]
name|authorityList
init|=
name|suppliedAuthorities
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|connParams
operator|.
name|setSuppliedAuthorityList
argument_list|(
name|authorityList
argument_list|)
expr_stmt|;
name|uri
operator|=
name|uri
operator|.
name|replace
argument_list|(
name|suppliedAuthorities
argument_list|,
name|dummyAuthorityString
argument_list|)
expr_stmt|;
block|}
comment|// Now parse the connection uri with dummy authority
name|URI
name|jdbcURI
init|=
name|URI
operator|.
name|create
argument_list|(
name|uri
operator|.
name|substring
argument_list|(
name|URI_JDBC_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// key=value pattern
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^;]*)=([^;]*)[;]?"
argument_list|)
decl_stmt|;
comment|// dbname and session settings
name|String
name|sessVars
init|=
name|jdbcURI
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|sessVars
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|sessVars
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|dbName
init|=
literal|""
decl_stmt|;
comment|// removing leading '/' returned by getPath()
name|sessVars
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|sessVars
operator|.
name|contains
argument_list|(
literal|";"
argument_list|)
condition|)
block|{
comment|// only dbname is provided
name|dbName
operator|=
name|sessVars
expr_stmt|;
block|}
else|else
block|{
comment|// we have dbname followed by session parameters
name|dbName
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sessVars
operator|.
name|indexOf
argument_list|(
literal|';'
argument_list|)
argument_list|)
expr_stmt|;
name|sessVars
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
name|sessVars
operator|.
name|indexOf
argument_list|(
literal|';'
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|sessVars
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|sessMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|sessVars
argument_list|)
decl_stmt|;
while|while
condition|(
name|sessMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
if|if
condition|(
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|sessMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|sessMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|JdbcUriParseException
argument_list|(
literal|"Bad URL format: Multiple values for property "
operator|+
name|sessMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|dbName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// parse hive conf settings
name|String
name|confStr
init|=
name|jdbcURI
operator|.
name|getQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|confStr
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|confMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|confStr
argument_list|)
decl_stmt|;
while|while
condition|(
name|confMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|getHiveConfs
argument_list|()
operator|.
name|put
argument_list|(
name|confMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|confMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// parse hive var settings
name|String
name|varStr
init|=
name|jdbcURI
operator|.
name|getFragment
argument_list|()
decl_stmt|;
if|if
condition|(
name|varStr
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|varMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|varStr
argument_list|)
decl_stmt|;
while|while
condition|(
name|varMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|getHiveVars
argument_list|()
operator|.
name|put
argument_list|(
name|varMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|varMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Extract host, port
if|if
condition|(
name|connParams
operator|.
name|isEmbeddedMode
argument_list|()
condition|)
block|{
comment|// In case of embedded mode we were supplied with an empty authority.
comment|// So we never substituted the authority with a dummy one.
name|connParams
operator|.
name|setHost
argument_list|(
name|jdbcURI
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setPort
argument_list|(
name|jdbcURI
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Else substitute the dummy authority with a resolved one.
comment|// In case of dynamic service discovery using ZooKeeper, it picks a server uri from ZooKeeper
name|String
name|resolvedAuthorityString
init|=
name|resolveAuthority
argument_list|(
name|connParams
argument_list|)
decl_stmt|;
name|uri
operator|=
name|uri
operator|.
name|replace
argument_list|(
name|dummyAuthorityString
argument_list|,
name|resolvedAuthorityString
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setJdbcUriString
argument_list|(
name|uri
argument_list|)
expr_stmt|;
comment|// Create a Java URI from the resolved URI for extracting the host/port
name|URI
name|resolvedAuthorityURI
init|=
literal|null
decl_stmt|;
try|try
block|{
name|resolvedAuthorityURI
operator|=
operator|new
name|URI
argument_list|(
literal|null
argument_list|,
name|resolvedAuthorityString
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|JdbcUriParseException
argument_list|(
literal|"Bad URL format: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|connParams
operator|.
name|setHost
argument_list|(
name|resolvedAuthorityURI
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setPort
argument_list|(
name|resolvedAuthorityURI
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|connParams
return|;
block|}
comment|/**    * Get the authority string from the supplied uri, which could potentially contain multiple    * host:port pairs.    *    * @param uri    * @param connParams    * @return    * @throws JdbcUriParseException    */
specifier|private
specifier|static
name|String
name|getAuthorities
parameter_list|(
name|String
name|uri
parameter_list|,
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|JdbcUriParseException
block|{
name|String
name|authorities
decl_stmt|;
comment|/**      * For a jdbc uri like:      * jdbc:hive2://<host1>:<port1>,<host2>:<port2>/dbName;sess_var_list?conf_list#var_list      * Extract the uri host:port list starting after "jdbc:hive2://",      * till the 1st "/" or "?" or "#" whichever comes first& in the given order      * Examples:      * jdbc:hive2://host1:port1,host2:port2,host3:port3/db;k1=v1?k2=v2#k3=v3      * jdbc:hive2://host1:port1,host2:port2,host3:port3/;k1=v1?k2=v2#k3=v3      * jdbc:hive2://host1:port1,host2:port2,host3:port3?k2=v2#k3=v3      * jdbc:hive2://host1:port1,host2:port2,host3:port3#k3=v3      */
name|int
name|fromIndex
init|=
name|Utils
operator|.
name|URL_PREFIX
operator|.
name|length
argument_list|()
decl_stmt|;
name|int
name|toIndex
init|=
operator|-
literal|1
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|toIndexChars
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"/"
argument_list|,
literal|"?"
argument_list|,
literal|"#"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|toIndexChar
range|:
name|toIndexChars
control|)
block|{
name|toIndex
operator|=
name|uri
operator|.
name|indexOf
argument_list|(
name|toIndexChar
argument_list|,
name|fromIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|toIndex
operator|>
literal|0
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
name|toIndex
operator|<
literal|0
condition|)
block|{
name|authorities
operator|=
name|uri
operator|.
name|substring
argument_list|(
name|fromIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authorities
operator|=
name|uri
operator|.
name|substring
argument_list|(
name|fromIndex
argument_list|,
name|toIndex
argument_list|)
expr_stmt|;
block|}
return|return
name|authorities
return|;
block|}
comment|/**    * Get a string representing a specific host:port    * @param connParams    * @return    * @throws JdbcUriParseException    * @throws ZooKeeperHiveClientException    */
specifier|private
specifier|static
name|String
name|resolveAuthority
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|JdbcUriParseException
throws|,
name|ZooKeeperHiveClientException
block|{
name|String
name|serviceDiscoveryMode
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
name|SERVICE_DISCOVERY_MODE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|serviceDiscoveryMode
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
name|serviceDiscoveryMode
argument_list|)
operator|)
condition|)
block|{
comment|// Resolve using ZooKeeper
return|return
name|resolveAuthorityUsingZooKeeper
argument_list|(
name|connParams
argument_list|)
return|;
block|}
else|else
block|{
name|String
name|authority
init|=
name|connParams
operator|.
name|getAuthorityList
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|URI
name|jdbcURI
init|=
name|URI
operator|.
name|create
argument_list|(
name|URI_HIVE_PREFIX
operator|+
literal|"//"
operator|+
name|authority
argument_list|)
decl_stmt|;
comment|// Check to prevent unintentional use of embedded mode. A missing "/"
comment|// to separate the 'path' portion of URI can result in this.
comment|// The missing "/" common typo while using secure mode, eg of such url -
comment|// jdbc:hive2://localhost:10000;principal=hive/HiveServer2Host@YOUR-REALM.COM
if|if
condition|(
operator|(
name|jdbcURI
operator|.
name|getAuthority
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|jdbcURI
operator|.
name|getHost
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|JdbcUriParseException
argument_list|(
literal|"Bad URL format. Hostname not found "
operator|+
literal|" in authority part of the url: "
operator|+
name|jdbcURI
operator|.
name|getAuthority
argument_list|()
operator|+
literal|". Are you missing a '/' after the hostname ?"
argument_list|)
throw|;
block|}
comment|// Return the 1st element of the array
return|return
name|jdbcURI
operator|.
name|getAuthority
argument_list|()
return|;
block|}
block|}
comment|/**    * Read a specific host:port from ZooKeeper    * @param connParams    * @return    * @throws ZooKeeperHiveClientException    */
specifier|private
specifier|static
name|String
name|resolveAuthorityUsingZooKeeper
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
comment|// Set ZooKeeper ensemble in connParams for later use
name|connParams
operator|.
name|setZooKeeperEnsemble
argument_list|(
name|joinStringArray
argument_list|(
name|connParams
operator|.
name|getAuthorityList
argument_list|()
argument_list|,
literal|","
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ZooKeeperHiveClientHelper
operator|.
name|getNextServerUriFromZooKeeper
argument_list|(
name|connParams
argument_list|)
return|;
block|}
comment|/**    * Read the next server coordinates (host:port combo) from ZooKeeper. Ignore the znodes already    * explored. Also update the host, port, jdbcUriString fields of connParams.    *    * @param connParams    * @throws ZooKeeperHiveClientException    */
specifier|static
name|void
name|updateConnParamsFromZooKeeper
parameter_list|(
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|ZooKeeperHiveClientException
block|{
comment|// Add current host to the rejected list
name|connParams
operator|.
name|getRejectedHostZnodePaths
argument_list|()
operator|.
name|add
argument_list|(
name|connParams
operator|.
name|getCurrentHostZnodePath
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get another HiveServer2 uri from ZooKeeper
name|String
name|serverUriString
init|=
name|ZooKeeperHiveClientHelper
operator|.
name|getNextServerUriFromZooKeeper
argument_list|(
name|connParams
argument_list|)
decl_stmt|;
comment|// Parse serverUri to a java URI and extract host, port
name|URI
name|serverUri
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Note URL_PREFIX is not a valid scheme format, therefore leaving it null in the constructor
comment|// to construct a valid URI
name|serverUri
operator|=
operator|new
name|URI
argument_list|(
literal|null
argument_list|,
name|serverUriString
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ZooKeeperHiveClientException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|String
name|oldServerHost
init|=
name|connParams
operator|.
name|getHost
argument_list|()
decl_stmt|;
name|int
name|oldServerPort
init|=
name|connParams
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|String
name|newServerHost
init|=
name|serverUri
operator|.
name|getHost
argument_list|()
decl_stmt|;
name|int
name|newServerPort
init|=
name|serverUri
operator|.
name|getPort
argument_list|()
decl_stmt|;
name|connParams
operator|.
name|setHost
argument_list|(
name|newServerHost
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setPort
argument_list|(
name|newServerPort
argument_list|)
expr_stmt|;
name|connParams
operator|.
name|setJdbcUriString
argument_list|(
name|connParams
operator|.
name|getJdbcUriString
argument_list|()
operator|.
name|replace
argument_list|(
name|oldServerHost
operator|+
literal|":"
operator|+
name|oldServerPort
argument_list|,
name|newServerHost
operator|+
literal|":"
operator|+
name|newServerPort
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|joinStringArray
parameter_list|(
name|String
index|[]
name|stringArray
parameter_list|,
name|String
name|seperator
parameter_list|)
block|{
name|StringBuilder
name|stringBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|cur
init|=
literal|0
init|,
name|end
init|=
name|stringArray
operator|.
name|length
init|;
name|cur
operator|<
name|end
condition|;
name|cur
operator|++
control|)
block|{
if|if
condition|(
name|cur
operator|>
literal|0
condition|)
block|{
name|stringBuilder
operator|.
name|append
argument_list|(
name|seperator
argument_list|)
expr_stmt|;
block|}
name|stringBuilder
operator|.
name|append
argument_list|(
name|stringArray
index|[
name|cur
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|stringBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Takes a version string delimited by '.' and '-' characters    * and returns a partial version.    *    * @param fullVersion    *          version string.    * @param position    *          position of version string to get starting at 0. eg, for a X.x.xxx    *          string, 0 will return the major version, 1 will return minor    *          version.    * @return version part, or -1 if version string was malformed.    */
specifier|static
name|int
name|getVersionPart
parameter_list|(
name|String
name|fullVersion
parameter_list|,
name|int
name|position
parameter_list|)
block|{
name|int
name|version
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|String
index|[]
name|tokens
init|=
name|fullVersion
operator|.
name|split
argument_list|(
literal|"[\\.-]"
argument_list|)
decl_stmt|;
comment|//$NON-NLS-1$
if|if
condition|(
name|tokens
operator|!=
literal|null
operator|&&
name|tokens
operator|.
name|length
operator|>
literal|1
operator|&&
name|tokens
index|[
name|position
index|]
operator|!=
literal|null
condition|)
block|{
name|version
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|tokens
index|[
name|position
index|]
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|version
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|version
return|;
block|}
block|}
end_class

end_unit

