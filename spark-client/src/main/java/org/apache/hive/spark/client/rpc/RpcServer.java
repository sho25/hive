begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|rpc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
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
name|Random
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
name|ConcurrentMap
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
name|TimeoutException
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|callback
operator|.
name|Callback
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
name|callback
operator|.
name|CallbackHandler
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
name|callback
operator|.
name|NameCallback
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
name|callback
operator|.
name|PasswordCallback
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthorizeCallback
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|RealmCallback
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|Sasl
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslServer
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|bootstrap
operator|.
name|ServerBootstrap
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelFuture
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelInitializer
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelOption
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|SocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioServerSocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|GenericFutureListener
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Promise
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledFuture
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|ApplicationReport
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
name|YarnApplicationState
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
name|client
operator|.
name|api
operator|.
name|YarnClient
import|;
end_import

begin_comment
comment|/**  * An RPC server. The server matches remote clients based on a secret that is generated on  * the server - the secret needs to be given to the client through some other mechanism for  * this to work.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RpcServer
implements|implements
name|Closeable
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
name|RpcServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SecureRandom
name|RND
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|address
decl_stmt|;
specifier|private
specifier|final
name|Channel
name|channel
decl_stmt|;
specifier|private
specifier|final
name|EventLoopGroup
name|group
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ClientInfo
argument_list|>
name|pendingClients
decl_stmt|;
specifier|private
specifier|final
name|RpcConfiguration
name|config
decl_stmt|;
specifier|private
name|String
name|applicationId
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
name|RpcServer
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapConf
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
operator|.
name|config
operator|=
operator|new
name|RpcConfiguration
argument_list|(
name|mapConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|group
operator|=
operator|new
name|NioEventLoopGroup
argument_list|(
name|this
operator|.
name|config
operator|.
name|getRpcThreadCount
argument_list|()
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"Spark-Driver-RPC-Handler-%d"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ServerBootstrap
name|serverBootstrap
init|=
operator|new
name|ServerBootstrap
argument_list|()
operator|.
name|group
argument_list|(
name|group
argument_list|)
operator|.
name|channel
argument_list|(
name|NioServerSocketChannel
operator|.
name|class
argument_list|)
operator|.
name|childHandler
argument_list|(
operator|new
name|ChannelInitializer
argument_list|<
name|SocketChannel
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|initChannel
parameter_list|(
name|SocketChannel
name|ch
parameter_list|)
throws|throws
name|Exception
block|{
name|SaslServerHandler
name|saslHandler
init|=
operator|new
name|SaslServerHandler
argument_list|(
name|config
argument_list|)
decl_stmt|;
specifier|final
name|Rpc
name|newRpc
init|=
name|Rpc
operator|.
name|createServer
argument_list|(
name|saslHandler
argument_list|,
name|config
argument_list|,
name|ch
argument_list|,
name|group
argument_list|)
decl_stmt|;
name|saslHandler
operator|.
name|rpc
operator|=
name|newRpc
expr_stmt|;
name|Runnable
name|cancelTask
init|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Timed out waiting for the completion of SASL negotiation "
operator|+
literal|"between HiveServer2 and the Remote Spark Driver."
argument_list|)
expr_stmt|;
name|newRpc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|saslHandler
operator|.
name|cancelTask
operator|=
name|group
operator|.
name|schedule
argument_list|(
name|cancelTask
argument_list|,
name|RpcServer
operator|.
name|this
operator|.
name|config
operator|.
name|getConnectTimeoutMs
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|SO_REUSEADDR
argument_list|,
literal|true
argument_list|)
operator|.
name|childOption
argument_list|(
name|ChannelOption
operator|.
name|SO_KEEPALIVE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|this
operator|.
name|channel
operator|=
name|bindServerPort
argument_list|(
name|serverBootstrap
argument_list|)
operator|.
name|channel
argument_list|()
expr_stmt|;
name|this
operator|.
name|port
operator|=
operator|(
operator|(
name|InetSocketAddress
operator|)
name|channel
operator|.
name|localAddress
argument_list|()
operator|)
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|this
operator|.
name|pendingClients
operator|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|address
operator|=
name|this
operator|.
name|config
operator|.
name|getServerAddress
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully created Remote Spark Driver RPC Server with address {}:{}"
argument_list|,
name|this
operator|.
name|address
argument_list|,
name|this
operator|.
name|port
argument_list|)
expr_stmt|;
block|}
comment|/**    * Retry the list of configured ports until one is found    * @param serverBootstrap    * @return    * @throws InterruptedException    * @throws IOException    */
specifier|private
name|ChannelFuture
name|bindServerPort
parameter_list|(
name|ServerBootstrap
name|serverBootstrap
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|ports
init|=
name|config
operator|.
name|getServerPorts
argument_list|()
decl_stmt|;
if|if
condition|(
name|ports
operator|.
name|contains
argument_list|(
literal|0
argument_list|)
condition|)
block|{
return|return
name|serverBootstrap
operator|.
name|bind
argument_list|(
literal|0
argument_list|)
operator|.
name|sync
argument_list|()
return|;
block|}
else|else
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|ports
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
name|index
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|ports
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|ports
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|ports
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|serverBootstrap
operator|.
name|bind
argument_list|(
name|port
argument_list|)
operator|.
name|sync
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Retry the next port
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Remote Spark Driver RPC Server cannot bind to any of the configured ports: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|config
operator|.
name|getServerPorts
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Tells the RPC server to expect a connection from a new client.    *    * @param clientId An identifier for the client. Must be unique.    * @param secret The secret the client will send to the server to identify itself.    * @param serverDispatcher The dispatcher to use when setting up the RPC instance.    * @return A future that can be used to wait for the client connection, which also provides the    *         secret needed for the client to connect.    */
specifier|public
name|Future
argument_list|<
name|Rpc
argument_list|>
name|registerClient
parameter_list|(
specifier|final
name|String
name|clientId
parameter_list|,
name|String
name|secret
parameter_list|,
name|RpcDispatcher
name|serverDispatcher
parameter_list|)
block|{
return|return
name|registerClient
argument_list|(
name|clientId
argument_list|,
name|secret
argument_list|,
name|serverDispatcher
argument_list|,
name|config
operator|.
name|getServerConnectTimeoutMs
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|setApplicationId
parameter_list|(
name|String
name|applicationId
parameter_list|)
block|{
name|this
operator|.
name|applicationId
operator|=
name|applicationId
expr_stmt|;
block|}
comment|/**    * This function converts an application in form of a String into a ApplicationId.    *    * @param appIDStr The application id in form of a string    * @return the application id as an instance of ApplicationId class.    */
specifier|private
specifier|static
name|ApplicationId
name|getApplicationIDFromString
parameter_list|(
name|String
name|appIDStr
parameter_list|)
block|{
name|String
index|[]
name|parts
init|=
name|appIDStr
operator|.
name|split
argument_list|(
literal|"_"
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|3
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"the application id found is not valid. application id: "
operator|+
name|appIDStr
argument_list|)
throw|;
block|}
name|long
name|timestamp
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|int
name|id
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|2
index|]
argument_list|)
decl_stmt|;
return|return
name|ApplicationId
operator|.
name|newInstance
argument_list|(
name|timestamp
argument_list|,
name|id
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isApplicationAccepted
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|applicationId
parameter_list|)
block|{
if|if
condition|(
name|applicationId
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|YarnClient
name|yarnClient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ApplicationId
name|appId
init|=
name|getApplicationIDFromString
argument_list|(
name|applicationId
argument_list|)
decl_stmt|;
name|yarnClient
operator|=
name|YarnClient
operator|.
name|createYarnClient
argument_list|()
expr_stmt|;
name|yarnClient
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|yarnClient
operator|.
name|start
argument_list|()
expr_stmt|;
name|ApplicationReport
name|appReport
init|=
name|yarnClient
operator|.
name|getApplicationReport
argument_list|(
name|appId
argument_list|)
decl_stmt|;
return|return
name|appReport
operator|!=
literal|null
operator|&&
name|appReport
operator|.
name|getYarnApplicationState
argument_list|()
operator|==
name|YarnApplicationState
operator|.
name|ACCEPTED
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed getting application status for: "
operator|+
name|applicationId
operator|+
literal|": "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|yarnClient
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|yarnClient
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
name|error
argument_list|(
literal|"Failed to stop yarn client: "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|static
class|class
name|YarnApplicationStateFinder
block|{
specifier|public
name|boolean
name|isApplicationAccepted
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|applicationId
parameter_list|)
block|{
if|if
condition|(
name|applicationId
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|YarnClient
name|yarnClient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to find "
operator|+
name|applicationId
argument_list|)
expr_stmt|;
name|ApplicationId
name|appId
init|=
name|getApplicationIDFromString
argument_list|(
name|applicationId
argument_list|)
decl_stmt|;
name|yarnClient
operator|=
name|YarnClient
operator|.
name|createYarnClient
argument_list|()
expr_stmt|;
name|yarnClient
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|yarnClient
operator|.
name|start
argument_list|()
expr_stmt|;
name|ApplicationReport
name|appReport
init|=
name|yarnClient
operator|.
name|getApplicationReport
argument_list|(
name|appId
argument_list|)
decl_stmt|;
return|return
name|appReport
operator|!=
literal|null
operator|&&
name|appReport
operator|.
name|getYarnApplicationState
argument_list|()
operator|==
name|YarnApplicationState
operator|.
name|ACCEPTED
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed getting application status for: "
operator|+
name|applicationId
operator|+
literal|": "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|yarnClient
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|yarnClient
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
name|error
argument_list|(
literal|"Failed to stop yarn client: "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
name|Future
argument_list|<
name|Rpc
argument_list|>
name|registerClient
parameter_list|(
specifier|final
name|String
name|clientId
parameter_list|,
name|String
name|secret
parameter_list|,
name|RpcDispatcher
name|serverDispatcher
parameter_list|,
specifier|final
name|long
name|clientTimeoutMs
parameter_list|)
block|{
return|return
name|registerClient
argument_list|(
name|clientId
argument_list|,
name|secret
argument_list|,
name|serverDispatcher
argument_list|,
name|clientTimeoutMs
argument_list|,
operator|new
name|YarnApplicationStateFinder
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|Future
argument_list|<
name|Rpc
argument_list|>
name|registerClient
parameter_list|(
specifier|final
name|String
name|clientId
parameter_list|,
name|String
name|secret
parameter_list|,
name|RpcDispatcher
name|serverDispatcher
parameter_list|,
name|long
name|clientTimeoutMs
parameter_list|,
name|YarnApplicationStateFinder
name|yarnApplicationStateFinder
parameter_list|)
block|{
specifier|final
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
init|=
name|group
operator|.
name|next
argument_list|()
operator|.
name|newPromise
argument_list|()
decl_stmt|;
name|Runnable
name|timeout
init|=
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// check to see if application is in ACCEPTED state, if so, don't set failure
comment|// if applicationId is not null
comment|//   do yarn application -status $applicationId
comment|//   if state == ACCEPTED
comment|//     reschedule timeout runnable
comment|//   else
comment|//    set failure as below
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to find "
operator|+
name|applicationId
argument_list|)
expr_stmt|;
if|if
condition|(
name|yarnApplicationStateFinder
operator|.
name|isApplicationAccepted
argument_list|(
name|hiveConf
argument_list|,
name|applicationId
argument_list|)
condition|)
block|{
specifier|final
name|ClientInfo
name|client
init|=
name|pendingClients
operator|.
name|get
argument_list|(
name|clientId
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Extending timeout for client "
operator|+
name|clientId
argument_list|)
expr_stmt|;
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|oldTimeoutFuture
init|=
name|client
operator|.
name|timeoutFuture
decl_stmt|;
name|client
operator|.
name|timeoutFuture
operator|=
name|group
operator|.
name|schedule
argument_list|(
name|this
argument_list|,
name|clientTimeoutMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|oldTimeoutFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|promise
operator|.
name|setFailure
argument_list|(
operator|new
name|TimeoutException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Client '%s' timed out waiting for connection from the Remote Spark"
operator|+
literal|" Driver"
argument_list|,
name|clientId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutFuture
init|=
name|group
operator|.
name|schedule
argument_list|(
name|timeout
argument_list|,
name|clientTimeoutMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
specifier|final
name|ClientInfo
name|client
init|=
operator|new
name|ClientInfo
argument_list|(
name|clientId
argument_list|,
name|promise
argument_list|,
name|secret
argument_list|,
name|serverDispatcher
argument_list|,
name|timeoutFuture
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingClients
operator|.
name|putIfAbsent
argument_list|(
name|clientId
argument_list|,
name|client
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Remote Spark Driver with client ID '%s' already registered"
argument_list|,
name|clientId
argument_list|)
argument_list|)
throw|;
block|}
name|promise
operator|.
name|addListener
argument_list|(
operator|new
name|GenericFutureListener
argument_list|<
name|Promise
argument_list|<
name|Rpc
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|p
parameter_list|)
block|{
if|if
condition|(
operator|!
name|p
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|pendingClients
operator|.
name|remove
argument_list|(
name|clientId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|promise
return|;
block|}
comment|/**    * Tells the RPC server to cancel the connection from an existing pending client.    *    * @param clientId The identifier for the client    * @param failure The error about why the connection should be canceled    */
specifier|public
name|void
name|cancelClient
parameter_list|(
specifier|final
name|String
name|clientId
parameter_list|,
specifier|final
name|Throwable
name|failure
parameter_list|)
block|{
specifier|final
name|ClientInfo
name|cinfo
init|=
name|pendingClients
operator|.
name|remove
argument_list|(
name|clientId
argument_list|)
decl_stmt|;
if|if
condition|(
name|cinfo
operator|==
literal|null
condition|)
block|{
comment|// Nothing to be done here.
return|return;
block|}
name|cinfo
operator|.
name|timeoutFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|cinfo
operator|.
name|promise
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|cinfo
operator|.
name|promise
operator|.
name|setFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Tells the RPC server to cancel the connection from an existing pending client.    *    * @param clientId The identifier for the client    * @param msg The error message about why the connection should be canceled    */
specifier|public
name|void
name|cancelClient
parameter_list|(
specifier|final
name|String
name|clientId
parameter_list|,
specifier|final
name|String
name|msg
parameter_list|)
block|{
name|cancelClient
argument_list|(
name|clientId
argument_list|,
operator|new
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Cancelling Remote Spark Driver client connection '%s' with error: "
operator|+
name|msg
argument_list|,
name|clientId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a secret for identifying a client connection.    */
specifier|public
name|String
name|createSecret
parameter_list|()
block|{
name|byte
index|[]
name|secret
init|=
operator|new
name|byte
index|[
name|config
operator|.
name|getSecretBits
argument_list|()
operator|/
literal|8
index|]
decl_stmt|;
name|RND
operator|.
name|nextBytes
argument_list|(
name|secret
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
name|b
range|:
name|secret
control|)
block|{
if|if
condition|(
name|b
operator|<
literal|10
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"0"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|Integer
operator|.
name|toHexString
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getAddress
parameter_list|()
block|{
return|return
name|address
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
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|channel
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|ClientInfo
name|client
range|:
name|pendingClients
operator|.
name|values
argument_list|()
control|)
block|{
name|client
operator|.
name|promise
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|pendingClients
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|group
operator|.
name|shutdownGracefully
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|SaslServerHandler
extends|extends
name|SaslHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|SaslServer
name|server
decl_stmt|;
specifier|private
name|Rpc
name|rpc
decl_stmt|;
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|cancelTask
decl_stmt|;
specifier|private
name|String
name|clientId
decl_stmt|;
specifier|private
name|ClientInfo
name|client
decl_stmt|;
name|SaslServerHandler
parameter_list|(
name|RpcConfiguration
name|config
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|Sasl
operator|.
name|createSaslServer
argument_list|(
name|config
operator|.
name|getSaslMechanism
argument_list|()
argument_list|,
name|Rpc
operator|.
name|SASL_PROTOCOL
argument_list|,
name|Rpc
operator|.
name|SASL_REALM
argument_list|,
name|config
operator|.
name|getSaslOptions
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|server
operator|.
name|isComplete
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getNegotiatedProperty
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|(
name|String
operator|)
name|server
operator|.
name|getNegotiatedProperty
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Rpc
operator|.
name|SaslMessage
name|update
parameter_list|(
name|Rpc
operator|.
name|SaslMessage
name|challenge
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|clientId
operator|==
literal|null
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|challenge
operator|.
name|clientId
operator|!=
literal|null
argument_list|,
literal|"Missing client ID in SASL handshake."
argument_list|)
expr_stmt|;
name|clientId
operator|=
name|challenge
operator|.
name|clientId
expr_stmt|;
name|client
operator|=
name|pendingClients
operator|.
name|get
argument_list|(
name|clientId
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|client
operator|!=
literal|null
argument_list|,
literal|"Unexpected client ID '%s' in SASL handshake."
argument_list|,
name|clientId
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Rpc
operator|.
name|SaslMessage
argument_list|(
name|server
operator|.
name|evaluateResponse
argument_list|(
name|challenge
operator|.
name|payload
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|wrap
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|server
operator|.
name|wrap
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|unwrap
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|server
operator|.
name|unwrap
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispose
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|server
operator|.
name|isComplete
argument_list|()
condition|)
block|{
name|onError
argument_list|(
operator|new
name|SaslException
argument_list|(
literal|"Server closed before SASL negotiation finished."
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|server
operator|.
name|dispose
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|onComplete
parameter_list|()
throws|throws
name|Exception
block|{
name|cancelTask
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|timeoutFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rpc
operator|.
name|setDispatcher
argument_list|(
name|client
operator|.
name|dispatcher
argument_list|)
expr_stmt|;
name|client
operator|.
name|promise
operator|.
name|setSuccess
argument_list|(
name|rpc
argument_list|)
expr_stmt|;
name|pendingClients
operator|.
name|remove
argument_list|(
name|client
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
name|cancelTask
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|timeoutFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|client
operator|.
name|promise
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|client
operator|.
name|promise
operator|.
name|setFailure
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Callback
index|[]
name|callbacks
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|client
operator|!=
literal|null
argument_list|,
literal|"Handshake not initialized yet."
argument_list|)
expr_stmt|;
for|for
control|(
name|Callback
name|cb
range|:
name|callbacks
control|)
block|{
if|if
condition|(
name|cb
operator|instanceof
name|NameCallback
condition|)
block|{
operator|(
operator|(
name|NameCallback
operator|)
name|cb
operator|)
operator|.
name|setName
argument_list|(
name|clientId
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cb
operator|instanceof
name|PasswordCallback
condition|)
block|{
operator|(
operator|(
name|PasswordCallback
operator|)
name|cb
operator|)
operator|.
name|setPassword
argument_list|(
name|client
operator|.
name|secret
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cb
operator|instanceof
name|AuthorizeCallback
condition|)
block|{
operator|(
operator|(
name|AuthorizeCallback
operator|)
name|cb
operator|)
operator|.
name|setAuthorized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cb
operator|instanceof
name|RealmCallback
condition|)
block|{
name|RealmCallback
name|rb
init|=
operator|(
name|RealmCallback
operator|)
name|cb
decl_stmt|;
name|rb
operator|.
name|setText
argument_list|(
name|rb
operator|.
name|getDefaultText
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|ClientInfo
block|{
specifier|final
name|String
name|id
decl_stmt|;
specifier|final
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
decl_stmt|;
specifier|final
name|String
name|secret
decl_stmt|;
specifier|final
name|RpcDispatcher
name|dispatcher
decl_stmt|;
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutFuture
decl_stmt|;
specifier|private
name|ClientInfo
parameter_list|(
name|String
name|id
parameter_list|,
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
parameter_list|,
name|String
name|secret
parameter_list|,
name|RpcDispatcher
name|dispatcher
parameter_list|,
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutFuture
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|promise
operator|=
name|promise
expr_stmt|;
name|this
operator|.
name|secret
operator|=
name|secret
expr_stmt|;
name|this
operator|.
name|dispatcher
operator|=
name|dispatcher
expr_stmt|;
name|this
operator|.
name|timeoutFuture
operator|=
name|timeoutFuture
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

