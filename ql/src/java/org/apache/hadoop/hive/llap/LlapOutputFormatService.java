begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
package|;
end_package

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
name|HashMap
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
name|mapred
operator|.
name|RecordWriter
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
name|StringUtils
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|LlapOutputSocketInitMessage
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
name|llap
operator|.
name|security
operator|.
name|SecretManager
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
name|SimpleChannelInboundHandler
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
name|handler
operator|.
name|codec
operator|.
name|protobuf
operator|.
name|ProtobufDecoder
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
name|codec
operator|.
name|protobuf
operator|.
name|ProtobufVarint32FrameDecoder
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
name|codec
operator|.
name|string
operator|.
name|StringEncoder
import|;
end_import

begin_comment
comment|/**  * Responsible for sending back result set data to the connections  * made by external clients via the LLAP input format.  */
end_comment

begin_class
specifier|public
class|class
name|LlapOutputFormatService
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
name|LlapOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|started
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|initing
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|LlapOutputFormatService
name|INSTANCE
decl_stmt|;
comment|// TODO: the global lock might be to coarse here.
specifier|private
specifier|final
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RecordWriter
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|writers
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|RecordWriter
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|errors
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
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|WAIT_TIME
init|=
literal|5
decl_stmt|;
specifier|private
name|EventLoopGroup
name|eventLoopGroup
decl_stmt|;
specifier|private
name|ServerBootstrap
name|serverBootstrap
decl_stmt|;
specifier|private
name|ChannelFuture
name|listeningChannelFuture
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|SecretManager
name|sm
decl_stmt|;
specifier|private
specifier|final
name|long
name|writerTimeoutMs
decl_stmt|;
specifier|private
name|LlapOutputFormatService
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SecretManager
name|sm
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|sm
operator|=
name|sm
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|writerTimeoutMs
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_OUTPUT_STREAM_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|initializeAndStart
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|SecretManager
name|sm
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|initing
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
condition|)
block|{
name|INSTANCE
operator|=
operator|new
name|LlapOutputFormatService
argument_list|(
name|conf
argument_list|,
name|sm
argument_list|)
expr_stmt|;
name|INSTANCE
operator|.
name|start
argument_list|()
expr_stmt|;
name|started
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|LlapOutputFormatService
name|get
parameter_list|()
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|started
operator|.
name|get
argument_list|()
argument_list|,
literal|"LlapOutputFormatService must be started before invoking get"
argument_list|)
expr_stmt|;
return|return
name|INSTANCE
return|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting LlapOutputFormatService"
argument_list|)
expr_stmt|;
name|int
name|portFromConf
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_OUTPUT_SERVICE_PORT
argument_list|)
decl_stmt|;
name|int
name|sendBufferSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_OUTPUT_SERVICE_SEND_BUFFER_SIZE
argument_list|)
decl_stmt|;
name|eventLoopGroup
operator|=
operator|new
name|NioEventLoopGroup
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|serverBootstrap
operator|=
operator|new
name|ServerBootstrap
argument_list|()
expr_stmt|;
name|serverBootstrap
operator|.
name|group
argument_list|(
name|eventLoopGroup
argument_list|)
expr_stmt|;
name|serverBootstrap
operator|.
name|channel
argument_list|(
name|NioServerSocketChannel
operator|.
name|class
argument_list|)
expr_stmt|;
name|serverBootstrap
operator|.
name|childHandler
argument_list|(
operator|new
name|LlapOutputFormatServiceChannelHandler
argument_list|(
name|sendBufferSize
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|listeningChannelFuture
operator|=
name|serverBootstrap
operator|.
name|bind
argument_list|(
name|portFromConf
argument_list|)
operator|.
name|sync
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
name|listeningChannelFuture
operator|.
name|channel
argument_list|()
operator|.
name|localAddress
argument_list|()
operator|)
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"LlapOutputFormatService: Binding to port: {} with send buffer size: {} "
argument_list|,
name|this
operator|.
name|port
argument_list|,
name|sendBufferSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"LlapOutputFormatService: Error binding to port "
operator|+
name|portFromConf
argument_list|,
name|err
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping LlapOutputFormatService"
argument_list|)
expr_stmt|;
if|if
condition|(
name|listeningChannelFuture
operator|!=
literal|null
condition|)
block|{
name|listeningChannelFuture
operator|.
name|channel
argument_list|()
operator|.
name|close
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
name|listeningChannelFuture
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"LlapOutputFormatService does not appear to have a listening port to close."
argument_list|)
expr_stmt|;
block|}
name|eventLoopGroup
operator|.
name|shutdownGracefully
argument_list|(
literal|1
argument_list|,
name|WAIT_TIME
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|getWriter
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|RecordWriter
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|writer
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
while|while
condition|(
operator|(
name|writer
operator|=
name|writers
operator|.
name|get
argument_list|(
name|id
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|String
name|error
init|=
name|errors
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|error
argument_list|)
throw|;
block|}
if|if
condition|(
name|isFirst
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for writer for "
operator|+
name|id
argument_list|)
expr_stmt|;
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000000
operator|)
operator|>
name|writerTimeoutMs
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The writer for "
operator|+
name|id
operator|+
literal|" has timed out after "
operator|+
name|writerTimeoutMs
operator|+
literal|"ms"
argument_list|)
throw|;
block|}
name|lock
operator|.
name|wait
argument_list|(
name|writerTimeoutMs
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Returning writer for: "
operator|+
name|id
argument_list|)
expr_stmt|;
return|return
operator|(
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|writer
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
specifier|protected
class|class
name|LlapOutputFormatServiceHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|LlapOutputSocketInitMessage
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|sendBufferSize
decl_stmt|;
specifier|public
name|LlapOutputFormatServiceHandler
parameter_list|(
specifier|final
name|int
name|sendBufferSize
parameter_list|)
block|{
name|this
operator|.
name|sendBufferSize
operator|=
name|sendBufferSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|channelRead0
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|LlapOutputSocketInitMessage
name|msg
parameter_list|)
block|{
name|String
name|id
init|=
name|msg
operator|.
name|getFragmentId
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tokenBytes
init|=
name|msg
operator|.
name|hasToken
argument_list|()
condition|?
name|msg
operator|.
name|getToken
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
decl_stmt|;
try|try
block|{
name|registerReader
argument_list|(
name|ctx
argument_list|,
name|id
argument_list|,
name|tokenBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Make sure we fail the channel if something goes wrong.
comment|// We internally handle all the "expected" exceptions, so log a lot of information here.
name|failChannel
argument_list|(
name|ctx
argument_list|,
name|id
argument_list|,
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|registerReader
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|tokenBytes
parameter_list|)
block|{
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sm
operator|.
name|verifyToken
argument_list|(
name|tokenBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
decl||
name|IOException
name|ex
parameter_list|)
block|{
name|failChannel
argument_list|(
name|ctx
argument_list|,
name|id
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"registering socket for: "
operator|+
name|id
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|LlapRecordWriter
name|writer
init|=
operator|new
name|LlapRecordWriter
argument_list|(
operator|new
name|ChannelOutputStream
argument_list|(
name|ctx
argument_list|,
name|id
argument_list|,
name|sendBufferSize
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|isFailed
init|=
literal|true
decl_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
if|if
condition|(
operator|!
name|writers
operator|.
name|containsKey
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|isFailed
operator|=
literal|false
expr_stmt|;
name|writers
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|writer
argument_list|)
expr_stmt|;
comment|// Add listener to handle any cleanup for when the connection is closed
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|closeFuture
argument_list|()
operator|.
name|addListener
argument_list|(
operator|new
name|LlapOutputFormatChannelCloseListener
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|lock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isFailed
condition|)
block|{
name|failChannel
argument_list|(
name|ctx
argument_list|,
name|id
argument_list|,
literal|"Writer already registered for "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Do not call under lock. */
specifier|private
name|void
name|failChannel
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|error
parameter_list|)
block|{
comment|// TODO: write error to the channel? there's no mechanism for that now.
name|ctx
operator|.
name|close
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|errors
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|lock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
class|class
name|LlapOutputFormatChannelCloseListener
implements|implements
name|ChannelFutureListener
block|{
specifier|private
name|String
name|id
decl_stmt|;
name|LlapOutputFormatChannelCloseListener
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|operationComplete
parameter_list|(
name|ChannelFuture
name|future
parameter_list|)
throws|throws
name|Exception
block|{
name|RecordWriter
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|writer
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|INSTANCE
init|)
block|{
name|writer
operator|=
name|writers
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Did not find a writer for ID "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
class|class
name|LlapOutputFormatServiceChannelHandler
extends|extends
name|ChannelInitializer
argument_list|<
name|SocketChannel
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|sendBufferSize
decl_stmt|;
specifier|public
name|LlapOutputFormatServiceChannelHandler
parameter_list|(
specifier|final
name|int
name|sendBufferSize
parameter_list|)
block|{
name|this
operator|.
name|sendBufferSize
operator|=
name|sendBufferSize
expr_stmt|;
block|}
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
name|ch
operator|.
name|pipeline
argument_list|()
operator|.
name|addLast
argument_list|(
operator|new
name|ProtobufVarint32FrameDecoder
argument_list|()
argument_list|,
operator|new
name|ProtobufDecoder
argument_list|(
name|LlapOutputSocketInitMessage
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
argument_list|,
operator|new
name|StringEncoder
argument_list|()
argument_list|,
operator|new
name|LlapOutputFormatServiceHandler
argument_list|(
name|sendBufferSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

