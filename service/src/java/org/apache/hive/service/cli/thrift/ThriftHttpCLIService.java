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
name|service
operator|.
name|cli
operator|.
name|thrift
package|;
end_package

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
name|concurrent
operator|.
name|ExecutorService
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
name|SynchronousQueue
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
name|ThreadPoolExecutor
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
name|shims
operator|.
name|ShimLoader
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
name|Shell
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
name|auth
operator|.
name|HiveAuthFactory
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
name|CLIService
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
name|TCLIService
operator|.
name|Iface
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
name|ThreadFactoryWithGarbageCleanup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServlet
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
name|ExecutorThreadPool
import|;
end_import

begin_class
specifier|public
class|class
name|ThriftHttpCLIService
extends|extends
name|ThriftCLIService
block|{
specifier|private
specifier|final
name|Runnable
name|oomHook
decl_stmt|;
specifier|public
name|ThriftHttpCLIService
parameter_list|(
name|CLIService
name|cliService
parameter_list|,
name|Runnable
name|oomHook
parameter_list|)
block|{
name|super
argument_list|(
name|cliService
argument_list|,
name|ThriftHttpCLIService
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|oomHook
operator|=
name|oomHook
expr_stmt|;
block|}
comment|/**    * Configure Jetty to serve http requests. Example of a client connection URL:    * http://localhost:10000/servlets/thrifths2/ A gateway may cause actual target URL to differ,    * e.g. http://gateway:port/hive2/servlets/thrifths2/    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// HTTP Server
name|httpServer
operator|=
operator|new
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|Server
argument_list|()
expr_stmt|;
comment|// Server thread pool
comment|// Start with minWorkerThreads, expand till maxWorkerThreads and reject subsequent requests
name|String
name|threadPoolName
init|=
literal|"HiveServer2-HttpHandler-Pool"
decl_stmt|;
name|ExecutorService
name|executorService
init|=
operator|new
name|ThreadPoolExecutorWithOomHook
argument_list|(
name|minWorkerThreads
argument_list|,
name|maxWorkerThreads
argument_list|,
name|workerKeepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ThreadFactoryWithGarbageCleanup
argument_list|(
name|threadPoolName
argument_list|)
argument_list|,
name|oomHook
argument_list|)
decl_stmt|;
name|ExecutorThreadPool
name|threadPool
init|=
operator|new
name|ExecutorThreadPool
argument_list|(
name|executorService
argument_list|)
decl_stmt|;
name|httpServer
operator|.
name|setThreadPool
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
comment|// Connector configs
name|SelectChannelConnector
name|connector
init|=
operator|new
name|SelectChannelConnector
argument_list|()
decl_stmt|;
name|boolean
name|useSsl
init|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_USE_SSL
argument_list|)
decl_stmt|;
name|String
name|schemeName
init|=
name|useSsl
condition|?
literal|"https"
else|:
literal|"http"
decl_stmt|;
comment|// Change connector if SSL is used
if|if
condition|(
name|useSsl
condition|)
block|{
name|String
name|keyStorePath
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PATH
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
name|keyStorePassword
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getPassword
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyStorePath
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PATH
operator|.
name|varname
operator|+
literal|" Not configured for SSL connection"
argument_list|)
throw|;
block|}
name|SslContextFactory
name|sslContextFactory
init|=
operator|new
name|SslContextFactory
argument_list|()
decl_stmt|;
name|String
index|[]
name|excludedProtocols
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SSL_PROTOCOL_BLACKLIST
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HTTP Server SSL: adding excluded protocols: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|excludedProtocols
argument_list|)
argument_list|)
expr_stmt|;
name|sslContextFactory
operator|.
name|addExcludeProtocols
argument_list|(
name|excludedProtocols
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HTTP Server SSL: SslContextFactory.getExcludeProtocols = "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|sslContextFactory
operator|.
name|getExcludeProtocols
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sslContextFactory
operator|.
name|setKeyStorePath
argument_list|(
name|keyStorePath
argument_list|)
expr_stmt|;
name|sslContextFactory
operator|.
name|setKeyStorePassword
argument_list|(
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
name|setPort
argument_list|(
name|portNum
argument_list|)
expr_stmt|;
comment|// Linux:yes, Windows:no
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
name|int
name|maxIdleTime
init|=
operator|(
name|int
operator|)
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_MAX_IDLE_TIME
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|connector
operator|.
name|setMaxIdleTime
argument_list|(
name|maxIdleTime
argument_list|)
expr_stmt|;
name|httpServer
operator|.
name|addConnector
argument_list|(
name|connector
argument_list|)
expr_stmt|;
comment|// Thrift configs
name|hiveAuthFactory
operator|=
operator|new
name|HiveAuthFactory
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|TProcessor
name|processor
init|=
operator|new
name|TCLIService
operator|.
name|Processor
argument_list|<
name|Iface
argument_list|>
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|TProtocolFactory
name|protocolFactory
init|=
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
decl_stmt|;
comment|// Set during the init phase of HiveServer2 if auth mode is kerberos
comment|// UGI for the hive/_HOST (kerberos) principal
name|UserGroupInformation
name|serviceUGI
init|=
name|cliService
operator|.
name|getServiceUGI
argument_list|()
decl_stmt|;
comment|// UGI for the http/_HOST (SPNego) principal
name|UserGroupInformation
name|httpUGI
init|=
name|cliService
operator|.
name|getHttpUGI
argument_list|()
decl_stmt|;
name|String
name|authType
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|)
decl_stmt|;
name|TServlet
name|thriftHttpServlet
init|=
operator|new
name|ThriftHttpServlet
argument_list|(
name|processor
argument_list|,
name|protocolFactory
argument_list|,
name|authType
argument_list|,
name|serviceUGI
argument_list|,
name|httpUGI
argument_list|)
decl_stmt|;
comment|// Context handler
specifier|final
name|ServletContextHandler
name|context
init|=
operator|new
name|ServletContextHandler
argument_list|(
name|ServletContextHandler
operator|.
name|SESSIONS
argument_list|)
decl_stmt|;
name|context
operator|.
name|setContextPath
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|String
name|httpPath
init|=
name|getHttpPath
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PATH
argument_list|)
argument_list|)
decl_stmt|;
name|httpServer
operator|.
name|setHandler
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
operator|new
name|ServletHolder
argument_list|(
name|thriftHttpServlet
argument_list|)
argument_list|,
name|httpPath
argument_list|)
expr_stmt|;
comment|// TODO: check defaults: maxTimeout, keepalive, maxBodySize, bodyRecieveDuration, etc.
comment|// Finally, start the server
name|httpServer
operator|.
name|start
argument_list|()
expr_stmt|;
name|String
name|msg
init|=
literal|"Started "
operator|+
name|ThriftHttpCLIService
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" in "
operator|+
name|schemeName
operator|+
literal|" mode on port "
operator|+
name|portNum
operator|+
literal|" path="
operator|+
name|httpPath
operator|+
literal|" with "
operator|+
name|minWorkerThreads
operator|+
literal|"..."
operator|+
name|maxWorkerThreads
operator|+
literal|" worker threads"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|httpServer
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Error starting HiveServer2: could not start "
operator|+
name|ThriftHttpCLIService
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * The config parameter can be like "path", "/path", "/path/", "path/*", "/path1/path2/*" and so on.    * httpPath should end up as "/*", "/path/*" or "/path1/../pathN/*"    * @param httpPath    * @return    */
specifier|private
name|String
name|getHttpPath
parameter_list|(
name|String
name|httpPath
parameter_list|)
block|{
if|if
condition|(
name|httpPath
operator|==
literal|null
operator|||
name|httpPath
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|httpPath
operator|=
literal|"/*"
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|httpPath
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
name|httpPath
operator|=
literal|"/"
operator|+
name|httpPath
expr_stmt|;
block|}
if|if
condition|(
name|httpPath
operator|.
name|endsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
name|httpPath
operator|=
name|httpPath
operator|+
literal|"*"
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|httpPath
operator|.
name|endsWith
argument_list|(
literal|"/*"
argument_list|)
condition|)
block|{
name|httpPath
operator|=
name|httpPath
operator|+
literal|"/*"
expr_stmt|;
block|}
block|}
return|return
name|httpPath
return|;
block|}
block|}
end_class

end_unit

