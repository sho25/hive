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
name|hadoop
operator|.
name|hive
operator|.
name|shims
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
name|Arrays
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|AppConfigurationEntry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|AppConfigurationEntry
operator|.
name|LoginModuleControlFlag
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterChain
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterConfig
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletResponse
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
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
name|security
operator|.
name|SecurityUtil
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
name|security
operator|.
name|UserGroupInformation
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
name|security
operator|.
name|authentication
operator|.
name|util
operator|.
name|KerberosUtil
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
name|client
operator|.
name|ZooKeeperSaslClient
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
name|Utils
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
name|Utils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|IBM_JAVA
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.vendor"
argument_list|)
operator|.
name|contains
argument_list|(
literal|"IBM"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|UserGroupInformation
name|getUGI
parameter_list|()
throws|throws
name|LoginException
throws|,
name|IOException
block|{
name|String
name|doAs
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_USER_NAME"
argument_list|)
decl_stmt|;
if|if
condition|(
name|doAs
operator|!=
literal|null
operator|&&
name|doAs
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|/*       * this allows doAs (proxy user) to be passed along across process boundary where       * delegation tokens are not supported.  For example, a DDL stmt via WebHCat with       * a doAs parameter, forks to 'hcat' which needs to start a Session that       * proxies the end user       */
return|return
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|doAs
argument_list|,
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
argument_list|)
return|;
block|}
return|return
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
return|;
block|}
comment|/**    * Dynamically sets up the JAAS configuration that uses kerberos    * @param principal    * @param keyTabFile    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setZookeeperClientKerberosJaasConfig
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|keyTabFile
parameter_list|)
throws|throws
name|IOException
block|{
comment|// ZooKeeper property name to pick the correct JAAS conf section
specifier|final
name|String
name|SASL_LOGIN_CONTEXT_NAME
init|=
literal|"HiveZooKeeperClient"
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|ZooKeeperSaslClient
operator|.
name|LOGIN_CONTEXT_NAME_KEY
argument_list|,
name|SASL_LOGIN_CONTEXT_NAME
argument_list|)
expr_stmt|;
name|principal
operator|=
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|principal
argument_list|,
literal|"0.0.0.0"
argument_list|)
expr_stmt|;
name|JaasConfiguration
name|jaasConf
init|=
operator|new
name|JaasConfiguration
argument_list|(
name|SASL_LOGIN_CONTEXT_NAME
argument_list|,
name|principal
argument_list|,
name|keyTabFile
argument_list|)
decl_stmt|;
comment|// Install the Configuration in the runtime.
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
operator|.
name|setConfiguration
argument_list|(
name|jaasConf
argument_list|)
expr_stmt|;
block|}
comment|/**    * A JAAS configuration for ZooKeeper clients intended to use for SASL    * Kerberos.    */
specifier|private
specifier|static
class|class
name|JaasConfiguration
extends|extends
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
block|{
comment|// Current installed Configuration
specifier|private
specifier|static
specifier|final
name|boolean
name|IBM_JAVA
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.vendor"
argument_list|)
operator|.
name|contains
argument_list|(
literal|"IBM"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
name|baseConfig
init|=
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|loginContextName
decl_stmt|;
specifier|private
specifier|final
name|String
name|principal
decl_stmt|;
specifier|private
specifier|final
name|String
name|keyTabFile
decl_stmt|;
specifier|public
name|JaasConfiguration
parameter_list|(
name|String
name|hiveLoginContextName
parameter_list|,
name|String
name|principal
parameter_list|,
name|String
name|keyTabFile
parameter_list|)
block|{
name|this
operator|.
name|loginContextName
operator|=
name|hiveLoginContextName
expr_stmt|;
name|this
operator|.
name|principal
operator|=
name|principal
expr_stmt|;
name|this
operator|.
name|keyTabFile
operator|=
name|keyTabFile
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AppConfigurationEntry
index|[]
name|getAppConfigurationEntry
parameter_list|(
name|String
name|appName
parameter_list|)
block|{
if|if
condition|(
name|loginContextName
operator|.
name|equals
argument_list|(
name|appName
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|krbOptions
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
if|if
condition|(
name|IBM_JAVA
condition|)
block|{
name|krbOptions
operator|.
name|put
argument_list|(
literal|"credsType"
argument_list|,
literal|"both"
argument_list|)
expr_stmt|;
name|krbOptions
operator|.
name|put
argument_list|(
literal|"useKeytab"
argument_list|,
name|keyTabFile
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|krbOptions
operator|.
name|put
argument_list|(
literal|"doNotPrompt"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|krbOptions
operator|.
name|put
argument_list|(
literal|"storeKey"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|krbOptions
operator|.
name|put
argument_list|(
literal|"useKeyTab"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|krbOptions
operator|.
name|put
argument_list|(
literal|"keyTab"
argument_list|,
name|keyTabFile
argument_list|)
expr_stmt|;
block|}
name|krbOptions
operator|.
name|put
argument_list|(
literal|"principal"
argument_list|,
name|principal
argument_list|)
expr_stmt|;
name|krbOptions
operator|.
name|put
argument_list|(
literal|"refreshKrb5Config"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|AppConfigurationEntry
name|hiveZooKeeperClientEntry
init|=
operator|new
name|AppConfigurationEntry
argument_list|(
name|KerberosUtil
operator|.
name|getKrb5LoginModuleName
argument_list|()
argument_list|,
name|LoginModuleControlFlag
operator|.
name|REQUIRED
argument_list|,
name|krbOptions
argument_list|)
decl_stmt|;
return|return
operator|new
name|AppConfigurationEntry
index|[]
block|{
name|hiveZooKeeperClientEntry
block|}
return|;
block|}
comment|// Try the base config
if|if
condition|(
name|baseConfig
operator|!=
literal|null
condition|)
block|{
return|return
name|baseConfig
operator|.
name|getAppConfigurationEntry
argument_list|(
name|appName
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
name|String
name|XSRF_CUSTOM_HEADER_PARAM
init|=
literal|"custom-header"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|XSRF_CUSTOM_METHODS_TO_IGNORE_PARAM
init|=
literal|"methods-to-ignore"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|XSRF_HEADER_DEFAULT
init|=
literal|"X-XSRF-HEADER"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|XSRF_METHODS_TO_IGNORE_DEFAULT
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"GET"
argument_list|,
literal|"OPTIONS"
argument_list|,
literal|"HEAD"
argument_list|,
literal|"TRACE"
argument_list|)
argument_list|)
decl_stmt|;
comment|/*    * Return Hadoop-native RestCsrfPreventionFilter if it is available.    * Otherwise, construct our own copy of its logic.    */
specifier|public
specifier|static
name|Filter
name|getXSRFFilter
parameter_list|()
block|{
name|String
name|filterClass
init|=
literal|"org.apache.hadoop.security.http.RestCsrfPreventionFilter"
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|klass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|filterClass
argument_list|)
decl_stmt|;
name|Filter
name|f
init|=
name|klass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter {} found, using as-is."
argument_list|,
name|filterClass
argument_list|)
expr_stmt|;
return|return
name|f
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ClassNotFoundException, InstantiationException, IllegalAccessException
comment|// Class could not be init-ed, use our local copy
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unable to use {}, got exception {}. Using internal shims impl of filter."
argument_list|,
name|filterClass
argument_list|,
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|Utils
operator|.
name|constructXSRFFilter
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|Filter
name|constructXSRFFilter
parameter_list|()
block|{
comment|// Note Hadoop 2.7.1 onwards includes a RestCsrfPreventionFilter class that is
comment|// usable as-is. However, since we have to work on a multitude of hadoop versions
comment|// including very old ones, we either duplicate their code here, or not support
comment|// an XSRFFilter on older versions of hadoop So, we duplicate to minimize evil(ugh).
comment|// See HADOOP-12691 for details of what this is doing.
comment|// This method should never be called if Hadoop 2.7+ is available.
return|return
operator|new
name|Filter
argument_list|()
block|{
specifier|private
name|String
name|headerName
init|=
name|XSRF_HEADER_DEFAULT
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|methodsToIgnore
init|=
name|XSRF_METHODS_TO_IGNORE_DEFAULT
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FilterConfig
name|filterConfig
parameter_list|)
throws|throws
name|ServletException
block|{
name|String
name|customHeader
init|=
name|filterConfig
operator|.
name|getInitParameter
argument_list|(
name|XSRF_CUSTOM_HEADER_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|customHeader
operator|!=
literal|null
condition|)
block|{
name|headerName
operator|=
name|customHeader
expr_stmt|;
block|}
name|String
name|customMethodsToIgnore
init|=
name|filterConfig
operator|.
name|getInitParameter
argument_list|(
name|XSRF_CUSTOM_METHODS_TO_IGNORE_PARAM
argument_list|)
decl_stmt|;
if|if
condition|(
name|customMethodsToIgnore
operator|!=
literal|null
condition|)
block|{
name|parseMethodsToIgnore
argument_list|(
name|customMethodsToIgnore
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|parseMethodsToIgnore
parameter_list|(
name|String
name|mti
parameter_list|)
block|{
name|String
index|[]
name|methods
init|=
name|mti
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|methodsToIgnore
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|methods
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|methodsToIgnore
operator|.
name|add
argument_list|(
name|methods
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|doFilter
parameter_list|(
name|ServletRequest
name|request
parameter_list|,
name|ServletResponse
name|response
parameter_list|,
name|FilterChain
name|chain
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
if|if
condition|(
name|doXsrfFilter
argument_list|(
name|request
argument_list|,
name|response
argument_list|,
name|methodsToIgnore
argument_list|,
name|headerName
argument_list|)
condition|)
block|{
name|chain
operator|.
name|doFilter
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|destroy
parameter_list|()
block|{
comment|// do nothing
block|}
block|}
return|;
block|}
comment|// Method that provides similar filter functionality to filter-holder above, useful when
comment|// calling from code that does not use filters as-is.
specifier|public
specifier|static
name|boolean
name|doXsrfFilter
parameter_list|(
name|ServletRequest
name|request
parameter_list|,
name|ServletResponse
name|response
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|methodsToIgnore
parameter_list|,
name|String
name|headerName
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServletException
block|{
name|HttpServletRequest
name|httpRequest
init|=
operator|(
name|HttpServletRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|methodsToIgnore
operator|==
literal|null
condition|)
block|{
name|methodsToIgnore
operator|=
name|XSRF_METHODS_TO_IGNORE_DEFAULT
expr_stmt|;
block|}
if|if
condition|(
name|headerName
operator|==
literal|null
condition|)
block|{
name|headerName
operator|=
name|XSRF_HEADER_DEFAULT
expr_stmt|;
block|}
if|if
condition|(
name|methodsToIgnore
operator|.
name|contains
argument_list|(
name|httpRequest
operator|.
name|getMethod
argument_list|()
argument_list|)
operator|||
name|httpRequest
operator|.
name|getHeader
argument_list|(
name|headerName
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
operator|(
operator|(
name|HttpServletResponse
operator|)
name|response
operator|)
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_BAD_REQUEST
argument_list|,
literal|"Missing Required Header for Vulnerability Protection"
argument_list|)
expr_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|println
argument_list|(
literal|"XSRF filter denial, requests must contain header : "
operator|+
name|headerName
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

