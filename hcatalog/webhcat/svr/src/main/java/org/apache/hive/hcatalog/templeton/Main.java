begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|api
operator|.
name|core
operator|.
name|PackagesResourceConfig
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|spi
operator|.
name|container
operator|.
name|servlet
operator|.
name|ServletContainer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|HashMap
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
name|hadoop
operator|.
name|hdfs
operator|.
name|web
operator|.
name|AuthFilter
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
name|util
operator|.
name|GenericOptionsParser
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
name|rewrite
operator|.
name|handler
operator|.
name|RedirectPatternRule
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
name|rewrite
operator|.
name|handler
operator|.
name|RewriteHandler
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
name|Handler
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
name|HandlerList
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
name|ServletHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|bridge
operator|.
name|SLF4JBridgeHandler
import|;
end_import

begin_comment
comment|/**  * The main executable that starts up and runs the Server.  */
end_comment

begin_class
specifier|public
class|class
name|Main
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SERVLET_PATH
init|=
literal|"templeton"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Main
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_PORT
init|=
literal|8080
decl_stmt|;
specifier|private
name|Server
name|server
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|AppConfig
name|conf
decl_stmt|;
comment|/**      * Retrieve the config singleton.      */
specifier|public
specifier|static
specifier|synchronized
name|AppConfig
name|getAppConfigInstance
parameter_list|()
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
name|LOG
operator|.
name|error
argument_list|(
literal|"Bug: configuration not yet loaded"
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
name|Main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|init
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|initLogger
argument_list|()
expr_stmt|;
name|conf
operator|=
name|loadConfig
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|conf
operator|.
name|startCleanup
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loaded conf "
operator|+
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// Jersey uses java.util.logging - bridge to slf4
specifier|private
name|void
name|initLogger
parameter_list|()
block|{
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
name|rootLogger
init|=
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|LogManager
operator|.
name|getLogManager
argument_list|()
operator|.
name|getLogger
argument_list|(
literal|""
argument_list|)
decl_stmt|;
for|for
control|(
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Handler
name|h
range|:
name|rootLogger
operator|.
name|getHandlers
argument_list|()
control|)
name|rootLogger
operator|.
name|removeHandler
argument_list|(
name|h
argument_list|)
expr_stmt|;
name|SLF4JBridgeHandler
operator|.
name|install
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AppConfig
name|loadConfig
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|AppConfig
name|cf
init|=
operator|new
name|AppConfig
argument_list|()
decl_stmt|;
try|try
block|{
name|GenericOptionsParser
name|parser
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|cf
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|getRemainingArgs
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
name|usage
argument_list|()
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
literal|"Unable to parse options: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|usage
argument_list|()
expr_stmt|;
block|}
return|return
name|cf
return|;
block|}
specifier|public
name|void
name|usage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"usage: templeton [-Dtempleton.port=N] [-D...]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|int
name|port
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|AppConfig
operator|.
name|PORT
argument_list|,
name|DEFAULT_PORT
argument_list|)
decl_stmt|;
try|try
block|{
name|checkEnv
argument_list|()
expr_stmt|;
name|runServer
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"templeton: listening on port "
operator|+
name|port
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Templeton listening on port "
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"templeton: Server failed to start: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Server failed to start: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to stop jetty.Server"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|checkEnv
parameter_list|()
block|{
name|checkCurrentDirPermissions
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|checkCurrentDirPermissions
parameter_list|()
block|{
comment|//org.apache.commons.exec.DefaultExecutor requires
comment|// that current directory exists
name|File
name|pwd
init|=
operator|new
name|File
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|pwd
operator|.
name|exists
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"Server failed to start: templeton: Current working directory '.' does not exist!"
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Server
name|runServer
parameter_list|(
name|int
name|port
parameter_list|)
throws|throws
name|Exception
block|{
comment|//Authenticate using keytab
if|if
condition|(
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|UserGroupInformation
operator|.
name|loginUserFromKeytab
argument_list|(
name|conf
operator|.
name|kerberosPrincipal
argument_list|()
argument_list|,
name|conf
operator|.
name|kerberosKeytab
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Create the Jetty server
name|Server
name|server
init|=
operator|new
name|Server
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|ServletContextHandler
name|root
init|=
operator|new
name|ServletContextHandler
argument_list|(
name|server
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
comment|// Add the Auth filter
name|FilterHolder
name|fHolder
init|=
name|makeAuthFilter
argument_list|()
decl_stmt|;
comment|/*           * We add filters for each of the URIs supported by templeton.          * If we added the entire sub-structure using '/*', the mapreduce           * notification cannot give the callback to templeton in secure mode.          * This is because mapreduce does not use secure credentials for           * callbacks. So jetty would fail the request as unauthorized.          */
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/ddl/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/pig/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/hive/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/queue/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/mapreduce/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/status/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
name|root
operator|.
name|addFilter
argument_list|(
name|fHolder
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/v1/version/*"
argument_list|,
name|FilterMapping
operator|.
name|REQUEST
argument_list|)
expr_stmt|;
comment|// Connect Jersey
name|ServletHolder
name|h
init|=
operator|new
name|ServletHolder
argument_list|(
operator|new
name|ServletContainer
argument_list|(
name|makeJerseyConfig
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|root
operator|.
name|addServlet
argument_list|(
name|h
argument_list|,
literal|"/"
operator|+
name|SERVLET_PATH
operator|+
literal|"/*"
argument_list|)
expr_stmt|;
comment|// Add any redirects
name|addRedirects
argument_list|(
name|server
argument_list|)
expr_stmt|;
comment|// Start the server
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
return|return
name|server
return|;
block|}
comment|// Configure the AuthFilter with the Kerberos params iff security
comment|// is enabled.
specifier|public
name|FilterHolder
name|makeAuthFilter
parameter_list|()
block|{
name|FilterHolder
name|authFilter
init|=
operator|new
name|FilterHolder
argument_list|(
name|AuthFilter
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|authFilter
operator|.
name|setInitParameter
argument_list|(
literal|"dfs.web.authentication.signature.secret"
argument_list|,
name|conf
operator|.
name|kerberosSecret
argument_list|()
argument_list|)
expr_stmt|;
name|authFilter
operator|.
name|setInitParameter
argument_list|(
literal|"dfs.web.authentication.kerberos.principal"
argument_list|,
name|conf
operator|.
name|kerberosPrincipal
argument_list|()
argument_list|)
expr_stmt|;
name|authFilter
operator|.
name|setInitParameter
argument_list|(
literal|"dfs.web.authentication.kerberos.keytab"
argument_list|,
name|conf
operator|.
name|kerberosKeytab
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|authFilter
return|;
block|}
specifier|public
name|PackagesResourceConfig
name|makeJerseyConfig
parameter_list|()
block|{
name|PackagesResourceConfig
name|rc
init|=
operator|new
name|PackagesResourceConfig
argument_list|(
literal|"org.apache.hive.hcatalog.templeton"
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
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
name|props
operator|.
name|put
argument_list|(
literal|"com.sun.jersey.api.json.POJOMappingFeature"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"com.sun.jersey.config.property.WadlGeneratorConfig"
argument_list|,
literal|"org.apache.hive.hcatalog.templeton.WadlConfig"
argument_list|)
expr_stmt|;
name|rc
operator|.
name|setPropertiesAndFeatures
argument_list|(
name|props
argument_list|)
expr_stmt|;
return|return
name|rc
return|;
block|}
specifier|public
name|void
name|addRedirects
parameter_list|(
name|Server
name|server
parameter_list|)
block|{
name|RewriteHandler
name|rewrite
init|=
operator|new
name|RewriteHandler
argument_list|()
decl_stmt|;
name|RedirectPatternRule
name|redirect
init|=
operator|new
name|RedirectPatternRule
argument_list|()
decl_stmt|;
name|redirect
operator|.
name|setPattern
argument_list|(
literal|"/templeton/v1/application.wadl"
argument_list|)
expr_stmt|;
name|redirect
operator|.
name|setLocation
argument_list|(
literal|"/templeton/application.wadl"
argument_list|)
expr_stmt|;
name|rewrite
operator|.
name|addRule
argument_list|(
name|redirect
argument_list|)
expr_stmt|;
name|HandlerList
name|handlerlist
init|=
operator|new
name|HandlerList
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Handler
argument_list|>
name|handlers
init|=
operator|new
name|ArrayList
argument_list|<
name|Handler
argument_list|>
argument_list|()
decl_stmt|;
comment|// Any redirect handlers need to be added first
name|handlers
operator|.
name|add
argument_list|(
name|rewrite
argument_list|)
expr_stmt|;
comment|// Now add all the default handlers
for|for
control|(
name|Handler
name|handler
range|:
name|server
operator|.
name|getHandlers
argument_list|()
control|)
block|{
name|handlers
operator|.
name|add
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
name|Handler
index|[]
name|newlist
init|=
operator|new
name|Handler
index|[
name|handlers
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|handlerlist
operator|.
name|setHandlers
argument_list|(
name|handlers
operator|.
name|toArray
argument_list|(
name|newlist
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|setHandler
argument_list|(
name|handlerlist
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Main
name|templeton
init|=
operator|new
name|Main
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|templeton
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

