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
name|util
operator|.
name|Collection
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|atomic
operator|.
name|AtomicLong
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
name|atomic
operator|.
name|AtomicReference
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
name|SaslClient
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
name|base
operator|.
name|Throwables
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
name|Lists
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
name|Bootstrap
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
name|ChannelFutureListener
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
name|ChannelHandlerContext
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
name|ChannelInboundHandlerAdapter
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
name|embedded
operator|.
name|EmbeddedChannel
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
name|NioSocketChannel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|logging
operator|.
name|LoggingHandler
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|logging
operator|.
name|LogLevel
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
name|EventExecutorGroup
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
name|ImmediateEventExecutor
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

begin_comment
comment|/**  * Encapsulates the RPC functionality. Provides higher-level methods to talk to the remote  * endpoint.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Rpc
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
name|Rpc
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|SASL_REALM
init|=
literal|"rsc"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SASL_USER
init|=
literal|"rsc"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SASL_PROTOCOL
init|=
literal|"rsc"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SASL_AUTH_CONF
init|=
literal|"auth-conf"
decl_stmt|;
comment|/**    * Creates an RPC client for a server running on the given remote host and port.    *    * @param config RPC configuration data.    * @param eloop Event loop for managing the connection.    * @param host Host name or IP address to connect to.    * @param port Port where server is listening.    * @param clientId The client ID that identifies the connection.    * @param secret Secret for authenticating the client with the server.    * @param dispatcher Dispatcher used to handle RPC calls.    * @return A future that can be used to monitor the creation of the RPC object.    */
specifier|public
specifier|static
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|createClient
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|,
specifier|final
name|NioEventLoopGroup
name|eloop
parameter_list|,
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
specifier|final
name|String
name|clientId
parameter_list|,
specifier|final
name|String
name|secret
parameter_list|,
specifier|final
name|RpcDispatcher
name|dispatcher
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|RpcConfiguration
name|rpcConf
init|=
operator|new
name|RpcConfiguration
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|int
name|connectTimeoutMs
init|=
operator|(
name|int
operator|)
name|rpcConf
operator|.
name|getConnectTimeoutMs
argument_list|()
decl_stmt|;
specifier|final
name|ChannelFuture
name|cf
init|=
operator|new
name|Bootstrap
argument_list|()
operator|.
name|group
argument_list|(
name|eloop
argument_list|)
operator|.
name|handler
argument_list|(
operator|new
name|ChannelInboundHandlerAdapter
argument_list|()
block|{ }
argument_list|)
operator|.
name|channel
argument_list|(
name|NioSocketChannel
operator|.
name|class
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|SO_KEEPALIVE
argument_list|,
literal|true
argument_list|)
operator|.
name|option
argument_list|(
name|ChannelOption
operator|.
name|CONNECT_TIMEOUT_MILLIS
argument_list|,
name|connectTimeoutMs
argument_list|)
operator|.
name|connect
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
decl_stmt|;
specifier|final
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
init|=
name|eloop
operator|.
name|next
argument_list|()
operator|.
name|newPromise
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Rpc
argument_list|>
name|rpc
init|=
operator|new
name|AtomicReference
argument_list|<
name|Rpc
argument_list|>
argument_list|()
decl_stmt|;
comment|// Set up a timeout to undo everything.
specifier|final
name|Runnable
name|timeoutTask
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
name|promise
operator|.
name|setFailure
argument_list|(
operator|new
name|TimeoutException
argument_list|(
literal|"Timed out waiting for RPC server connection."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|final
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutFuture
init|=
name|eloop
operator|.
name|schedule
argument_list|(
name|timeoutTask
argument_list|,
name|connectTimeoutMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
comment|// The channel listener instantiates the Rpc instance when the connection is established,
comment|// and initiates the SASL handshake.
name|cf
operator|.
name|addListener
argument_list|(
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|cf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|cf
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|SaslClientHandler
name|saslHandler
init|=
operator|new
name|SaslClientHandler
argument_list|(
name|rpcConf
argument_list|,
name|clientId
argument_list|,
name|promise
argument_list|,
name|timeoutFuture
argument_list|,
name|secret
argument_list|,
name|dispatcher
argument_list|)
decl_stmt|;
name|Rpc
name|rpc
init|=
name|createRpc
argument_list|(
name|rpcConf
argument_list|,
name|saslHandler
argument_list|,
operator|(
name|SocketChannel
operator|)
name|cf
operator|.
name|channel
argument_list|()
argument_list|,
name|eloop
argument_list|)
decl_stmt|;
name|saslHandler
operator|.
name|rpc
operator|=
name|rpc
expr_stmt|;
name|saslHandler
operator|.
name|sendHello
argument_list|(
name|cf
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|promise
operator|.
name|setFailure
argument_list|(
name|cf
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Handle cancellation of the promise.
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
name|p
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
name|cf
operator|.
name|cancel
argument_list|(
literal|true
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
specifier|static
name|Rpc
name|createServer
parameter_list|(
name|SaslHandler
name|saslHandler
parameter_list|,
name|RpcConfiguration
name|config
parameter_list|,
name|SocketChannel
name|channel
parameter_list|,
name|EventExecutorGroup
name|egroup
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createRpc
argument_list|(
name|config
argument_list|,
name|saslHandler
argument_list|,
name|channel
argument_list|,
name|egroup
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Rpc
name|createRpc
parameter_list|(
name|RpcConfiguration
name|config
parameter_list|,
name|SaslHandler
name|saslHandler
parameter_list|,
name|SocketChannel
name|client
parameter_list|,
name|EventExecutorGroup
name|egroup
parameter_list|)
throws|throws
name|IOException
block|{
name|LogLevel
name|logLevel
init|=
name|LogLevel
operator|.
name|TRACE
decl_stmt|;
if|if
condition|(
name|config
operator|.
name|getRpcChannelLogLevel
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|logLevel
operator|=
name|LogLevel
operator|.
name|valueOf
argument_list|(
name|config
operator|.
name|getRpcChannelLogLevel
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid log level {}, reverting to default."
argument_list|,
name|config
operator|.
name|getRpcChannelLogLevel
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|boolean
name|logEnabled
init|=
literal|false
decl_stmt|;
switch|switch
condition|(
name|logLevel
condition|)
block|{
case|case
name|DEBUG
case|:
name|logEnabled
operator|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
expr_stmt|;
break|break;
case|case
name|ERROR
case|:
name|logEnabled
operator|=
name|LOG
operator|.
name|isErrorEnabled
argument_list|()
expr_stmt|;
break|break;
case|case
name|INFO
case|:
name|logEnabled
operator|=
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
expr_stmt|;
break|break;
case|case
name|TRACE
case|:
name|logEnabled
operator|=
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
expr_stmt|;
break|break;
case|case
name|WARN
case|:
name|logEnabled
operator|=
name|LOG
operator|.
name|isWarnEnabled
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|logEnabled
condition|)
block|{
name|client
operator|.
name|pipeline
argument_list|()
operator|.
name|addLast
argument_list|(
literal|"logger"
argument_list|,
operator|new
name|LoggingHandler
argument_list|(
name|Rpc
operator|.
name|class
argument_list|,
name|logLevel
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|KryoMessageCodec
name|kryo
init|=
operator|new
name|KryoMessageCodec
argument_list|(
name|config
operator|.
name|getMaxMessageSize
argument_list|()
argument_list|,
name|MessageHeader
operator|.
name|class
argument_list|,
name|NullMessage
operator|.
name|class
argument_list|,
name|SaslMessage
operator|.
name|class
argument_list|)
decl_stmt|;
name|saslHandler
operator|.
name|setKryoMessageCodec
argument_list|(
name|kryo
argument_list|)
expr_stmt|;
name|client
operator|.
name|pipeline
argument_list|()
operator|.
name|addLast
argument_list|(
literal|"codec"
argument_list|,
name|kryo
argument_list|)
operator|.
name|addLast
argument_list|(
literal|"sasl"
argument_list|,
name|saslHandler
argument_list|)
expr_stmt|;
return|return
operator|new
name|Rpc
argument_list|(
name|config
argument_list|,
name|client
argument_list|,
name|egroup
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|Rpc
name|createEmbedded
parameter_list|(
name|RpcDispatcher
name|dispatcher
parameter_list|)
block|{
name|EmbeddedChannel
name|c
init|=
operator|new
name|EmbeddedChannel
argument_list|(
operator|new
name|LoggingHandler
argument_list|(
name|Rpc
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|KryoMessageCodec
argument_list|(
literal|0
argument_list|,
name|MessageHeader
operator|.
name|class
argument_list|,
name|NullMessage
operator|.
name|class
argument_list|)
argument_list|,
name|dispatcher
argument_list|)
decl_stmt|;
name|Rpc
name|rpc
init|=
operator|new
name|Rpc
argument_list|(
operator|new
name|RpcConfiguration
argument_list|(
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|c
argument_list|,
name|ImmediateEventExecutor
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|rpc
operator|.
name|dispatcher
operator|=
name|dispatcher
expr_stmt|;
return|return
name|rpc
return|;
block|}
specifier|private
specifier|final
name|RpcConfiguration
name|config
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|rpcClosed
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|rpcId
decl_stmt|;
specifier|private
specifier|final
name|Channel
name|channel
decl_stmt|;
specifier|private
specifier|final
name|Collection
argument_list|<
name|Listener
argument_list|>
name|listeners
decl_stmt|;
specifier|private
specifier|final
name|EventExecutorGroup
name|egroup
decl_stmt|;
specifier|private
specifier|volatile
name|RpcDispatcher
name|dispatcher
decl_stmt|;
specifier|private
name|Rpc
parameter_list|(
name|RpcConfiguration
name|config
parameter_list|,
name|Channel
name|channel
parameter_list|,
name|EventExecutorGroup
name|egroup
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|channel
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|egroup
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|config
operator|=
name|config
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
name|this
operator|.
name|dispatcher
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|egroup
operator|=
name|egroup
expr_stmt|;
name|this
operator|.
name|listeners
operator|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcClosed
operator|=
operator|new
name|AtomicBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcId
operator|=
operator|new
name|AtomicLong
argument_list|()
expr_stmt|;
comment|// Note: this does not work for embedded channels.
name|channel
operator|.
name|pipeline
argument_list|()
operator|.
name|addLast
argument_list|(
literal|"monitor"
argument_list|,
operator|new
name|ChannelInboundHandlerAdapter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|channelInactive
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
name|close
argument_list|()
expr_stmt|;
name|super
operator|.
name|channelInactive
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|l
parameter_list|)
block|{
synchronized|synchronized
init|(
name|listeners
init|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Send an RPC call to the remote endpoint and returns a future that can be used to monitor the    * operation.    */
specifier|public
name|Future
argument_list|<
name|Void
argument_list|>
name|call
parameter_list|(
name|Object
name|msg
parameter_list|)
block|{
return|return
name|call
argument_list|(
name|msg
argument_list|,
name|Void
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isActive
parameter_list|()
block|{
return|return
name|channel
operator|.
name|isActive
argument_list|()
return|;
block|}
comment|/**    * Send an RPC call to the remote endpoint and returns a future that can be used to monitor the    * operation.    *    * @param msg RPC call to send.    * @param retType Type of expected reply.    * @return A future used to monitor the operation.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|call
parameter_list|(
specifier|final
name|Object
name|msg
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|retType
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|msg
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|channel
operator|.
name|isActive
argument_list|()
argument_list|,
literal|"RPC channel is closed."
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|long
name|id
init|=
name|rpcId
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
specifier|final
name|Promise
argument_list|<
name|T
argument_list|>
name|promise
init|=
name|createPromise
argument_list|()
decl_stmt|;
specifier|final
name|ChannelFutureListener
name|listener
init|=
operator|new
name|ChannelFutureListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|cf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|cf
operator|.
name|isSuccess
argument_list|()
operator|&&
operator|!
name|promise
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to send RPC, closing connection."
argument_list|,
name|cf
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
name|promise
operator|.
name|setFailure
argument_list|(
name|cf
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
name|dispatcher
operator|.
name|discardRpc
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|dispatcher
operator|.
name|registerRpc
argument_list|(
name|id
argument_list|,
name|promise
argument_list|,
name|msg
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|channel
operator|.
name|eventLoop
argument_list|()
operator|.
name|submit
argument_list|(
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
name|channel
operator|.
name|write
argument_list|(
operator|new
name|MessageHeader
argument_list|(
name|id
argument_list|,
name|Rpc
operator|.
name|MessageType
operator|.
name|CALL
argument_list|)
argument_list|)
operator|.
name|addListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|channel
operator|.
name|writeAndFlush
argument_list|(
name|msg
argument_list|)
operator|.
name|addListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|promise
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Creates a promise backed by this RPC's event loop.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Promise
argument_list|<
name|T
argument_list|>
name|createPromise
parameter_list|()
block|{
return|return
name|egroup
operator|.
name|next
argument_list|()
operator|.
name|newPromise
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|Channel
name|getChannel
parameter_list|()
block|{
return|return
name|channel
return|;
block|}
name|void
name|setDispatcher
parameter_list|(
name|RpcDispatcher
name|dispatcher
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dispatcher
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|this
operator|.
name|dispatcher
operator|==
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|dispatcher
operator|=
name|dispatcher
expr_stmt|;
name|channel
operator|.
name|pipeline
argument_list|()
operator|.
name|addLast
argument_list|(
literal|"dispatcher"
argument_list|,
name|dispatcher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|rpcClosed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
return|return;
block|}
try|try
block|{
name|channel
operator|.
name|close
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
synchronized|synchronized
init|(
name|listeners
init|)
block|{
for|for
control|(
name|Listener
name|l
range|:
name|listeners
control|)
block|{
try|try
block|{
name|l
operator|.
name|rpcClosed
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error caught in Rpc.Listener invocation."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|public
interface|interface
name|Listener
block|{
name|void
name|rpcClosed
parameter_list|(
name|Rpc
name|rpc
parameter_list|)
function_decl|;
block|}
specifier|static
enum|enum
name|MessageType
block|{
name|CALL
block|,
name|REPLY
block|,
name|ERROR
block|;   }
specifier|static
class|class
name|MessageHeader
block|{
specifier|final
name|long
name|id
decl_stmt|;
specifier|final
name|MessageType
name|type
decl_stmt|;
name|MessageHeader
parameter_list|()
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|MessageHeader
parameter_list|(
name|long
name|id
parameter_list|,
name|MessageType
name|type
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
name|type
operator|=
name|type
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|NullMessage
block|{    }
specifier|static
class|class
name|SaslMessage
block|{
specifier|final
name|String
name|clientId
decl_stmt|;
specifier|final
name|byte
index|[]
name|payload
decl_stmt|;
name|SaslMessage
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|SaslMessage
parameter_list|(
name|byte
index|[]
name|payload
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|payload
argument_list|)
expr_stmt|;
block|}
name|SaslMessage
parameter_list|(
name|String
name|clientId
parameter_list|,
name|byte
index|[]
name|payload
parameter_list|)
block|{
name|this
operator|.
name|clientId
operator|=
name|clientId
expr_stmt|;
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|SaslClientHandler
extends|extends
name|SaslHandler
implements|implements
name|CallbackHandler
block|{
specifier|private
specifier|final
name|SaslClient
name|client
decl_stmt|;
specifier|private
specifier|final
name|String
name|clientId
decl_stmt|;
specifier|private
specifier|final
name|String
name|secret
decl_stmt|;
specifier|private
specifier|final
name|RpcDispatcher
name|dispatcher
decl_stmt|;
specifier|private
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
decl_stmt|;
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeout
decl_stmt|;
comment|// Can't be set in constructor due to circular dependency.
specifier|private
name|Rpc
name|rpc
decl_stmt|;
name|SaslClientHandler
parameter_list|(
name|RpcConfiguration
name|config
parameter_list|,
name|String
name|clientId
parameter_list|,
name|Promise
argument_list|<
name|Rpc
argument_list|>
name|promise
parameter_list|,
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeout
parameter_list|,
name|String
name|secret
parameter_list|,
name|RpcDispatcher
name|dispatcher
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
name|clientId
operator|=
name|clientId
expr_stmt|;
name|this
operator|.
name|promise
operator|=
name|promise
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
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
name|client
operator|=
name|Sasl
operator|.
name|createSaslClient
argument_list|(
operator|new
name|String
index|[]
block|{
name|config
operator|.
name|getSaslMechanism
argument_list|()
block|}
argument_list|,
literal|null
argument_list|,
name|SASL_PROTOCOL
argument_list|,
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
name|client
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
name|client
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
name|SaslMessage
name|update
parameter_list|(
name|SaslMessage
name|challenge
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|response
init|=
name|client
operator|.
name|evaluateChallenge
argument_list|(
name|challenge
operator|.
name|payload
argument_list|)
decl_stmt|;
return|return
name|response
operator|!=
literal|null
condition|?
operator|new
name|SaslMessage
argument_list|(
name|response
argument_list|)
else|:
literal|null
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
name|client
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
name|client
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
name|client
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
literal|"Client closed before SASL negotiation finished."
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
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
name|timeout
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
name|dispatcher
argument_list|)
expr_stmt|;
name|promise
operator|.
name|setSuccess
argument_list|(
name|rpc
argument_list|)
expr_stmt|;
name|timeout
operator|=
literal|null
expr_stmt|;
name|promise
operator|=
literal|null
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
name|timeout
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|promise
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|promise
operator|.
name|setFailure
argument_list|(
name|error
argument_list|)
expr_stmt|;
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
name|void
name|sendHello
parameter_list|(
name|Channel
name|c
parameter_list|)
throws|throws
name|Exception
block|{
name|byte
index|[]
name|hello
init|=
name|client
operator|.
name|hasInitialResponse
argument_list|()
condition|?
name|client
operator|.
name|evaluateChallenge
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
else|:
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|c
operator|.
name|writeAndFlush
argument_list|(
operator|new
name|SaslMessage
argument_list|(
name|clientId
argument_list|,
name|hello
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

