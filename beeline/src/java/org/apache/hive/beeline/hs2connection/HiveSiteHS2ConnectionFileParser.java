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
name|beeline
operator|.
name|hs2connection
package|;
end_package

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
name|Properties
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
name|common
operator|.
name|ServerUtils
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/*  * Looks for a hive-site.xml from the classpath. If found this class parses the hive-site.xml  * to return a set of connection properties which can be used to construct the connection url  * for Beeline connection  */
end_comment

begin_class
specifier|public
class|class
name|HiveSiteHS2ConnectionFileParser
implements|implements
name|HS2ConnectionFileParser
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|URL
name|hiveSiteURI
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TRUSTSTORE_PASS_PROP
init|=
literal|"javax.net.ssl.trustStorePassword"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TRUSTSTORE_PROP
init|=
literal|"javax.net.ssl.trustStore"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveSiteHS2ConnectionFileParser
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HiveSiteHS2ConnectionFileParser
parameter_list|()
block|{
name|hiveSiteURI
operator|=
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
if|if
condition|(
name|hiveSiteURI
operator|==
literal|null
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"hive-site.xml not found for constructing the connection URL"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|log
operator|.
name|info
argument_list|(
literal|"Using hive-site.xml at "
operator|+
name|hiveSiteURI
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
name|hiveSiteURI
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|VisibleForTesting
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|hiveConf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Properties
name|getConnectionProperties
parameter_list|()
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|configExists
argument_list|()
condition|)
block|{
return|return
name|props
return|;
block|}
name|props
operator|.
name|setProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|URL_PREFIX_PROPERTY_KEY
argument_list|,
literal|"jdbc:hive2://"
argument_list|)
expr_stmt|;
name|addHosts
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|addSSL
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|addKerberos
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|addHttp
argument_list|(
name|props
argument_list|)
expr_stmt|;
return|return
name|props
return|;
block|}
specifier|private
name|void
name|addSSL
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
if|if
condition|(
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_USE_SSL
argument_list|)
condition|)
block|{
return|return;
block|}
else|else
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"ssl"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|String
name|truststore
init|=
name|System
operator|.
name|getenv
argument_list|(
name|TRUSTSTORE_PROP
argument_list|)
decl_stmt|;
if|if
condition|(
name|truststore
operator|!=
literal|null
operator|&&
name|truststore
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"sslTruststore"
argument_list|,
name|truststore
argument_list|)
expr_stmt|;
block|}
name|String
name|trustStorePassword
init|=
name|System
operator|.
name|getenv
argument_list|(
name|TRUSTSTORE_PASS_PROP
argument_list|)
decl_stmt|;
if|if
condition|(
name|trustStorePassword
operator|!=
literal|null
operator|&&
operator|!
name|trustStorePassword
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"trustStorePassword"
argument_list|,
name|trustStorePassword
argument_list|)
expr_stmt|;
block|}
name|String
name|saslQop
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_SASL_QOP
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
literal|"auth"
operator|.
name|equalsIgnoreCase
argument_list|(
name|saslQop
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"sasl.qop"
argument_list|,
name|saslQop
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addKerberos
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
if|if
condition|(
literal|"KERBEROS"
operator|.
name|equals
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|)
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"principal"
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addHttp
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
if|if
condition|(
literal|"http"
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|)
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"transportMode"
argument_list|,
literal|"http"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
name|props
operator|.
name|setProperty
argument_list|(
literal|"httpPath"
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PATH
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addHosts
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
comment|// if zk HA is enabled get hosts property
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SUPPORT_DYNAMIC_SERVICE_DISCOVERY
argument_list|)
condition|)
block|{
name|addZKServiceDiscoveryHosts
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addDefaultHS2Hosts
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addZKServiceDiscoveryHosts
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
name|props
operator|.
name|setProperty
argument_list|(
literal|"serviceDiscoveryMode"
argument_list|,
literal|"zooKeeper"
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
literal|"zooKeeperNamespace"
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_ZOOKEEPER_NAMESPACE
argument_list|)
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
literal|"hosts"
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addDefaultHS2Hosts
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
name|String
name|hiveHost
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_SERVER2_THRIFT_BIND_HOST"
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveHost
operator|==
literal|null
condition|)
block|{
name|hiveHost
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|)
expr_stmt|;
block|}
name|InetAddress
name|serverIPAddress
decl_stmt|;
try|try
block|{
name|serverIPAddress
operator|=
name|ServerUtils
operator|.
name|getHostAddress
argument_list|(
name|hiveHost
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BeelineHS2ConnectionFileParseException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|int
name|portNum
init|=
name|getPortNum
argument_list|(
literal|"http"
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
literal|"hosts"
argument_list|,
name|serverIPAddress
operator|.
name|getHostName
argument_list|()
operator|+
literal|":"
operator|+
name|portNum
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getPortNum
parameter_list|(
name|boolean
name|isHttp
parameter_list|)
block|{
name|String
name|portString
decl_stmt|;
name|int
name|portNum
decl_stmt|;
if|if
condition|(
name|isHttp
condition|)
block|{
name|portString
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_SERVER2_THRIFT_HTTP_PORT"
argument_list|)
expr_stmt|;
if|if
condition|(
name|portString
operator|!=
literal|null
condition|)
block|{
name|portNum
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|portNum
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PORT
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|portString
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_SERVER2_THRIFT_PORT"
argument_list|)
expr_stmt|;
if|if
condition|(
name|portString
operator|!=
literal|null
condition|)
block|{
name|portNum
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|portNum
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|portNum
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|configExists
parameter_list|()
block|{
return|return
operator|(
name|hiveSiteURI
operator|!=
literal|null
operator|)
return|;
block|}
block|}
end_class

end_unit

