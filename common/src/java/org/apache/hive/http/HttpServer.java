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
name|http
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|URL
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
name|javax
operator|.
name|servlet
operator|.
name|ServletContext
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
name|HttpServlet
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|math3
operator|.
name|util
operator|.
name|Pair
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
name|fs
operator|.
name|CommonConfigurationKeys
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
name|server
operator|.
name|AuthenticationFilter
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
name|authorize
operator|.
name|AccessControlList
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
name|Shell
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
name|logging
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
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
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|LoggerContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|appender
operator|.
name|AbstractOutputStreamAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|appender
operator|.
name|FileManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|appender
operator|.
name|OutputStreamManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|Connector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|Server
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|handler
operator|.
name|ContextHandler
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|handler
operator|.
name|ContextHandlerCollection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|nio
operator|.
name|SelectChannelConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|ssl
operator|.
name|SslSelectChannelConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|DefaultServlet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|FilterHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|FilterMapping
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|ServletContextHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|ServletHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|ServletHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|util
operator|.
name|ssl
operator|.
name|SslContextFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|util
operator|.
name|thread
operator|.
name|QueuedThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|webapp
operator|.
name|WebAppContext
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
name|Splitter
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
name|Strings
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
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * A simple embedded Jetty server to serve as HS2/HMS web UI.  */
end_comment

begin_class
specifier|public
class|class
name|HttpServer
block|{
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|slf4j
operator|.
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONF_CONTEXT_ATTRIBUTE
init|=
literal|"hive.conf"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADMINS_ACL
init|=
literal|"admins.acl"
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|String
name|appDir
decl_stmt|;
specifier|private
specifier|final
name|WebAppContext
name|webAppContext
decl_stmt|;
specifier|private
specifier|final
name|Server
name|webServer
decl_stmt|;
comment|/**    * Create a status server on the given port.    */
specifier|private
name|HttpServer
parameter_list|(
specifier|final
name|Builder
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|name
operator|=
name|b
operator|.
name|name
expr_stmt|;
name|webServer
operator|=
operator|new
name|Server
argument_list|()
expr_stmt|;
name|appDir
operator|=
name|getWebAppsPath
argument_list|(
name|b
operator|.
name|name
argument_list|)
expr_stmt|;
name|webAppContext
operator|=
name|createWebAppContext
argument_list|(
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|b
operator|.
name|useSPNEGO
condition|)
block|{
comment|// Secure the web server with kerberos
name|setupSpnegoFilter
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|initializeWebServer
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|host
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|int
name|maxThreads
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|contextAttrs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|String
name|keyStorePassword
decl_stmt|;
specifier|private
name|String
name|keyStorePath
decl_stmt|;
specifier|private
name|String
name|spnegoPrincipal
decl_stmt|;
specifier|private
name|String
name|spnegoKeytab
decl_stmt|;
specifier|private
name|boolean
name|useSPNEGO
decl_stmt|;
specifier|private
name|boolean
name|useSSL
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
argument_list|>
argument_list|>
name|servlets
init|=
operator|new
name|LinkedList
argument_list|<
name|Pair
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|Builder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|name
operator|!=
literal|null
operator|&&
operator|!
name|name
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"Name must be specified"
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|HttpServer
name|build
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|HttpServer
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|Builder
name|setConf
parameter_list|(
name|HiveConf
name|origConf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|origConf
argument_list|)
expr_stmt|;
name|origConf
operator|.
name|stripHiddenConfigurations
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setContextAttribute
argument_list|(
name|CONF_CONTEXT_ATTRIBUTE
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
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
return|return
name|this
return|;
block|}
specifier|public
name|Builder
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
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setMaxThreads
parameter_list|(
name|int
name|maxThreads
parameter_list|)
block|{
name|this
operator|.
name|maxThreads
operator|=
name|maxThreads
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setAdmins
parameter_list|(
name|String
name|admins
parameter_list|)
block|{
if|if
condition|(
name|admins
operator|!=
literal|null
condition|)
block|{
name|setContextAttribute
argument_list|(
name|ADMINS_ACL
argument_list|,
operator|new
name|AccessControlList
argument_list|(
name|admins
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setKeyStorePassword
parameter_list|(
name|String
name|keyStorePassword
parameter_list|)
block|{
name|this
operator|.
name|keyStorePassword
operator|=
name|keyStorePassword
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setKeyStorePath
parameter_list|(
name|String
name|keyStorePath
parameter_list|)
block|{
name|this
operator|.
name|keyStorePath
operator|=
name|keyStorePath
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setUseSSL
parameter_list|(
name|boolean
name|useSSL
parameter_list|)
block|{
name|this
operator|.
name|useSSL
operator|=
name|useSSL
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setUseSPNEGO
parameter_list|(
name|boolean
name|useSPNEGO
parameter_list|)
block|{
name|this
operator|.
name|useSPNEGO
operator|=
name|useSPNEGO
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setSPNEGOPrincipal
parameter_list|(
name|String
name|principal
parameter_list|)
block|{
name|this
operator|.
name|spnegoPrincipal
operator|=
name|principal
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setSPNEGOKeytab
parameter_list|(
name|String
name|keytab
parameter_list|)
block|{
name|this
operator|.
name|spnegoKeytab
operator|=
name|keytab
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setContextAttribute
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|contextAttrs
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|addServlet
parameter_list|(
name|String
name|endpoint
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
name|servlet
parameter_list|)
block|{
name|servlets
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
argument_list|>
argument_list|(
name|endpoint
argument_list|,
name|servlet
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|Exception
block|{
name|webServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started HttpServer[{}] on port {}"
argument_list|,
name|name
argument_list|,
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|Exception
block|{
name|webServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|webServer
operator|.
name|getConnectors
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLocalPort
argument_list|()
return|;
block|}
comment|/**    * Checks the user has privileges to access to instrumentation servlets.    *<p/>    * If<code>hadoop.security.instrumentation.requires.admin</code> is set to FALSE    * (default value) it always returns TRUE.    *<p/>    * If<code>hadoop.security.instrumentation.requires.admin</code> is set to TRUE    * it will check if the current user is in the admin ACLS. If the user is    * in the admin ACLs it returns TRUE, otherwise it returns FALSE.    *    * @param servletContext the servlet context.    * @param request the servlet request.    * @param response the servlet response.    * @return TRUE/FALSE based on the logic described above.    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
literal|"hive"
argument_list|)
specifier|public
specifier|static
name|boolean
name|isInstrumentationAccessAllowed
parameter_list|(
name|ServletContext
name|servletContext
parameter_list|,
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|(
name|Configuration
operator|)
name|servletContext
operator|.
name|getAttribute
argument_list|(
name|CONF_CONTEXT_ATTRIBUTE
argument_list|)
decl_stmt|;
name|boolean
name|access
init|=
literal|true
decl_stmt|;
name|boolean
name|adminAccess
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CommonConfigurationKeys
operator|.
name|HADOOP_SECURITY_INSTRUMENTATION_REQUIRES_ADMIN
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|adminAccess
condition|)
block|{
name|access
operator|=
name|hasAdministratorAccess
argument_list|(
name|servletContext
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
return|return
name|access
return|;
block|}
comment|/**    * Does the user sending the HttpServletRequest have the administrator ACLs? If    * it isn't the case, response will be modified to send an error to the user.    *    * @param servletContext    * @param request    * @param response used to send the error response if user does not have admin access.    * @return true if admin-authorized, false otherwise    * @throws IOException    */
specifier|static
name|boolean
name|hasAdministratorAccess
parameter_list|(
name|ServletContext
name|servletContext
parameter_list|,
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|(
name|Configuration
operator|)
name|servletContext
operator|.
name|getAttribute
argument_list|(
name|CONF_CONTEXT_ATTRIBUTE
argument_list|)
decl_stmt|;
comment|// If there is no authorization, anybody has administrator access.
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|CommonConfigurationKeys
operator|.
name|HADOOP_SECURITY_AUTHORIZATION
argument_list|,
literal|false
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|remoteUser
init|=
name|request
operator|.
name|getRemoteUser
argument_list|()
decl_stmt|;
if|if
condition|(
name|remoteUser
operator|==
literal|null
condition|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_UNAUTHORIZED
argument_list|,
literal|"Unauthenticated users are not "
operator|+
literal|"authorized to access this page."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|servletContext
operator|.
name|getAttribute
argument_list|(
name|ADMINS_ACL
argument_list|)
operator|!=
literal|null
operator|&&
operator|!
name|userHasAdministratorAccess
argument_list|(
name|servletContext
argument_list|,
name|remoteUser
argument_list|)
condition|)
block|{
name|response
operator|.
name|sendError
argument_list|(
name|HttpServletResponse
operator|.
name|SC_UNAUTHORIZED
argument_list|,
literal|"User "
operator|+
name|remoteUser
operator|+
literal|" is unauthorized to access this page."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Get the admin ACLs from the given ServletContext and check if the given    * user is in the ACL.    *    * @param servletContext the context containing the admin ACL.    * @param remoteUser the remote user to check for.    * @return true if the user is present in the ACL, false if no ACL is set or    *         the user is not present    */
specifier|static
name|boolean
name|userHasAdministratorAccess
parameter_list|(
name|ServletContext
name|servletContext
parameter_list|,
name|String
name|remoteUser
parameter_list|)
block|{
name|AccessControlList
name|adminsAcl
init|=
operator|(
name|AccessControlList
operator|)
name|servletContext
operator|.
name|getAttribute
argument_list|(
name|ADMINS_ACL
argument_list|)
decl_stmt|;
name|UserGroupInformation
name|remoteUserUGI
init|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|remoteUser
argument_list|)
decl_stmt|;
return|return
name|adminsAcl
operator|!=
literal|null
operator|&&
name|adminsAcl
operator|.
name|isUserAllowed
argument_list|(
name|remoteUserUGI
argument_list|)
return|;
block|}
comment|/**    * Create the web context for the application of specified name    */
name|WebAppContext
name|createWebAppContext
parameter_list|(
name|Builder
name|b
parameter_list|)
block|{
name|WebAppContext
name|ctx
init|=
operator|new
name|WebAppContext
argument_list|()
decl_stmt|;
name|setContextAttributes
argument_list|(
name|ctx
operator|.
name|getServletContext
argument_list|()
argument_list|,
name|b
operator|.
name|contextAttrs
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setDisplayName
argument_list|(
name|b
operator|.
name|name
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setContextPath
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setWar
argument_list|(
name|appDir
operator|+
literal|"/"
operator|+
name|b
operator|.
name|name
argument_list|)
expr_stmt|;
return|return
name|ctx
return|;
block|}
comment|/**    * Secure the web server with kerberos (AuthenticationFilter).    */
name|void
name|setupSpnegoFilter
parameter_list|(
name|Builder
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
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
name|params
operator|.
name|put
argument_list|(
literal|"kerberos.principal"
argument_list|,
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|b
operator|.
name|spnegoPrincipal
argument_list|,
name|b
operator|.
name|host
argument_list|)
argument_list|)
expr_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"kerberos.keytab"
argument_list|,
name|b
operator|.
name|spnegoKeytab
argument_list|)
expr_stmt|;
name|params
operator|.
name|put
argument_list|(
name|AuthenticationFilter
operator|.
name|AUTH_TYPE
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|FilterHolder
name|holder
init|=
operator|new
name|FilterHolder
argument_list|()
decl_stmt|;
name|holder
operator|.
name|setClassName
argument_list|(
name|AuthenticationFilter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|holder
operator|.
name|setInitParameters
argument_list|(
name|params
argument_list|)
expr_stmt|;
name|ServletHandler
name|handler
init|=
name|webAppContext
operator|.
name|getServletHandler
argument_list|()
decl_stmt|;
name|handler
operator|.
name|addFilterWithMapping
argument_list|(
name|holder
argument_list|,
literal|"/*"
argument_list|,
name|FilterMapping
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a channel connector for "http/https" requests    */
name|Connector
name|createChannelConnector
parameter_list|(
name|int
name|queueSize
parameter_list|,
name|Builder
name|b
parameter_list|)
block|{
name|SelectChannelConnector
name|connector
decl_stmt|;
if|if
condition|(
operator|!
name|b
operator|.
name|useSSL
condition|)
block|{
name|connector
operator|=
operator|new
name|SelectChannelConnector
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|SslContextFactory
name|sslContextFactory
init|=
operator|new
name|SslContextFactory
argument_list|()
decl_stmt|;
name|sslContextFactory
operator|.
name|setKeyStorePath
argument_list|(
name|b
operator|.
name|keyStorePath
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|excludedSSLProtocols
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|trimResults
argument_list|()
operator|.
name|omitEmptyStrings
argument_list|()
operator|.
name|split
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|b
operator|.
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SSL_PROTOCOL_BLACKLIST
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|sslContextFactory
operator|.
name|addExcludeProtocols
argument_list|(
name|excludedSSLProtocols
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|excludedSSLProtocols
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|sslContextFactory
operator|.
name|setKeyStorePassword
argument_list|(
name|b
operator|.
name|keyStorePassword
argument_list|)
expr_stmt|;
name|connector
operator|=
operator|new
name|SslSelectChannelConnector
argument_list|(
name|sslContextFactory
argument_list|)
expr_stmt|;
block|}
name|connector
operator|.
name|setLowResourcesMaxIdleTime
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setAcceptQueueSize
argument_list|(
name|queueSize
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setResolveNames
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setUseDirectBuffers
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setReuseAddress
argument_list|(
operator|!
name|Shell
operator|.
name|WINDOWS
argument_list|)
expr_stmt|;
return|return
name|connector
return|;
block|}
comment|/**    * Set servlet context attributes that can be used in jsp.    */
name|void
name|setContextAttributes
parameter_list|(
name|Context
name|ctx
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|contextAttrs
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
name|Object
argument_list|>
name|e
range|:
name|contextAttrs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ctx
operator|.
name|setAttribute
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|initializeWebServer
parameter_list|(
name|Builder
name|b
parameter_list|)
block|{
comment|// Create the thread pool for the web server to handle HTTP requests
name|QueuedThreadPool
name|threadPool
init|=
operator|new
name|QueuedThreadPool
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
operator|.
name|maxThreads
operator|>
literal|0
condition|)
block|{
name|threadPool
operator|.
name|setMaxThreads
argument_list|(
name|b
operator|.
name|maxThreads
argument_list|)
expr_stmt|;
block|}
name|threadPool
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|setName
argument_list|(
name|b
operator|.
name|name
operator|+
literal|"-web"
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|setThreadPool
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
comment|// Create the channel connector for the web server
name|Connector
name|connector
init|=
name|createChannelConnector
argument_list|(
name|threadPool
operator|.
name|getMaxThreads
argument_list|()
argument_list|,
name|b
argument_list|)
decl_stmt|;
name|connector
operator|.
name|setHost
argument_list|(
name|b
operator|.
name|host
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setPort
argument_list|(
name|b
operator|.
name|port
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addConnector
argument_list|(
name|connector
argument_list|)
expr_stmt|;
comment|// Configure web application contexts for the web server
name|ContextHandlerCollection
name|contexts
init|=
operator|new
name|ContextHandlerCollection
argument_list|()
decl_stmt|;
name|contexts
operator|.
name|addHandler
argument_list|(
name|webAppContext
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|setHandler
argument_list|(
name|contexts
argument_list|)
expr_stmt|;
name|addServlet
argument_list|(
literal|"jmx"
argument_list|,
literal|"/jmx"
argument_list|,
name|JMXJsonServlet
operator|.
name|class
argument_list|)
expr_stmt|;
name|addServlet
argument_list|(
literal|"conf"
argument_list|,
literal|"/conf"
argument_list|,
name|ConfServlet
operator|.
name|class
argument_list|)
expr_stmt|;
name|addServlet
argument_list|(
literal|"stacks"
argument_list|,
literal|"/stacks"
argument_list|,
name|StackServlet
operator|.
name|class
argument_list|)
expr_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
argument_list|>
name|p
range|:
name|b
operator|.
name|servlets
control|)
block|{
name|addServlet
argument_list|(
name|p
operator|.
name|getFirst
argument_list|()
argument_list|,
literal|"/"
operator|+
name|p
operator|.
name|getFirst
argument_list|()
argument_list|,
name|p
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ServletContextHandler
name|staticCtx
init|=
operator|new
name|ServletContextHandler
argument_list|(
name|contexts
argument_list|,
literal|"/static"
argument_list|)
decl_stmt|;
name|staticCtx
operator|.
name|setResourceBase
argument_list|(
name|appDir
operator|+
literal|"/static"
argument_list|)
expr_stmt|;
name|staticCtx
operator|.
name|addServlet
argument_list|(
name|DefaultServlet
operator|.
name|class
argument_list|,
literal|"/*"
argument_list|)
expr_stmt|;
name|staticCtx
operator|.
name|setDisplayName
argument_list|(
literal|"static"
argument_list|)
expr_stmt|;
name|String
name|logDir
init|=
name|getLogDir
argument_list|(
name|b
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|logDir
operator|!=
literal|null
condition|)
block|{
name|ServletContextHandler
name|logCtx
init|=
operator|new
name|ServletContextHandler
argument_list|(
name|contexts
argument_list|,
literal|"/logs"
argument_list|)
decl_stmt|;
name|setContextAttributes
argument_list|(
name|logCtx
operator|.
name|getServletContext
argument_list|()
argument_list|,
name|b
operator|.
name|contextAttrs
argument_list|)
expr_stmt|;
name|logCtx
operator|.
name|addServlet
argument_list|(
name|AdminAuthorizedServlet
operator|.
name|class
argument_list|,
literal|"/*"
argument_list|)
expr_stmt|;
name|logCtx
operator|.
name|setResourceBase
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
name|logCtx
operator|.
name|setDisplayName
argument_list|(
literal|"logs"
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|getLogDir
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|logDir
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hive.log.dir"
argument_list|)
decl_stmt|;
if|if
condition|(
name|logDir
operator|==
literal|null
condition|)
block|{
name|logDir
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hive.log.dir"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logDir
operator|!=
literal|null
condition|)
block|{
return|return
name|logDir
return|;
block|}
name|LoggerContext
name|context
init|=
operator|(
name|LoggerContext
operator|)
name|LogManager
operator|.
name|getContext
argument_list|(
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|Logger
name|logger
range|:
name|context
operator|.
name|getLoggers
argument_list|()
control|)
block|{
for|for
control|(
name|Appender
name|appender
range|:
name|logger
operator|.
name|getAppenders
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|appender
operator|instanceof
name|AbstractOutputStreamAppender
condition|)
block|{
name|OutputStreamManager
name|manager
init|=
operator|(
operator|(
name|AbstractOutputStreamAppender
argument_list|<
name|?
argument_list|>
operator|)
name|appender
operator|)
operator|.
name|getManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|manager
operator|instanceof
name|FileManager
condition|)
block|{
name|String
name|fileName
init|=
operator|(
operator|(
name|FileManager
operator|)
name|manager
operator|)
operator|.
name|getFileName
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileName
operator|!=
literal|null
condition|)
block|{
return|return
name|fileName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|fileName
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
name|String
name|getWebAppsPath
parameter_list|(
name|String
name|appName
parameter_list|)
throws|throws
name|FileNotFoundException
block|{
name|String
name|relativePath
init|=
literal|"hive-webapps/"
operator|+
name|appName
decl_stmt|;
name|URL
name|url
init|=
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
name|relativePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|url
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|relativePath
operator|+
literal|" not found in CLASSPATH"
argument_list|)
throw|;
block|}
name|String
name|urlString
init|=
name|url
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|urlString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|urlString
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Add a servlet in the server.    * @param name The name of the servlet (can be passed as null)    * @param pathSpec The path spec for the servlet    * @param clazz The servlet class    */
specifier|public
name|void
name|addServlet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|pathSpec
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HttpServlet
argument_list|>
name|clazz
parameter_list|)
block|{
name|ServletHolder
name|holder
init|=
operator|new
name|ServletHolder
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|holder
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|webAppContext
operator|.
name|addServlet
argument_list|(
name|holder
argument_list|,
name|pathSpec
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

