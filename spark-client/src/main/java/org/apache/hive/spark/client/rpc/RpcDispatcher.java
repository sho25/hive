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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|Iterator
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
name|ConcurrentLinkedQueue
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
name|Maps
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
name|SimpleChannelInboundHandler
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
comment|/**  * An implementation of ChannelInboundHandler that dispatches incoming messages to an instance  * method based on the method signature.  *<p/>  * A handler's signature must be of the form:  *<p/>  *<blockquote><tt>protected void handle(ChannelHandlerContext, MessageType)</tt></blockquote>  *<p/>  * Where "MessageType" must match exactly the type of the message to handle. Polymorphism is not  * supported. Handlers can return a value, which becomes the RPC reply; if a null is returned, then  * a reply is still sent, with an empty payload.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|RpcDispatcher
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|Object
argument_list|>
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
name|RpcDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Method
argument_list|>
name|handlers
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Collection
argument_list|<
name|OutstandingRpc
argument_list|>
name|rpcs
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|OutstandingRpc
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|Rpc
operator|.
name|MessageHeader
name|lastHeader
decl_stmt|;
comment|/** Override this to add a name to the dispatcher, for debugging purposes. */
specifier|protected
name|String
name|name
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
specifier|final
name|void
name|channelRead0
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|lastHeader
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|msg
operator|instanceof
name|Rpc
operator|.
name|MessageHeader
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"[{}] Expected RPC header, got {} instead."
argument_list|,
name|name
argument_list|()
argument_list|,
name|msg
operator|!=
literal|null
condition|?
name|msg
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
name|lastHeader
operator|=
operator|(
name|Rpc
operator|.
name|MessageHeader
operator|)
name|msg
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"[{}] Received RPC message: type={} id={} payload={}"
argument_list|,
name|name
argument_list|()
argument_list|,
name|lastHeader
operator|.
name|type
argument_list|,
name|lastHeader
operator|.
name|id
argument_list|,
name|msg
operator|!=
literal|null
condition|?
name|msg
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
switch|switch
condition|(
name|lastHeader
operator|.
name|type
condition|)
block|{
case|case
name|CALL
case|:
name|handleCall
argument_list|(
name|ctx
argument_list|,
name|msg
argument_list|)
expr_stmt|;
break|break;
case|case
name|REPLY
case|:
name|handleReply
argument_list|(
name|ctx
argument_list|,
name|msg
argument_list|,
name|findRpc
argument_list|(
name|lastHeader
operator|.
name|id
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|ERROR
case|:
name|handleError
argument_list|(
name|ctx
argument_list|,
name|msg
argument_list|,
name|findRpc
argument_list|(
name|lastHeader
operator|.
name|id
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown RPC message type: "
operator|+
name|lastHeader
operator|.
name|type
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|lastHeader
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|OutstandingRpc
name|findRpc
parameter_list|(
name|long
name|id
parameter_list|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|OutstandingRpc
argument_list|>
name|it
init|=
name|rpcs
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|OutstandingRpc
name|rpc
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|rpc
operator|.
name|id
operator|==
name|id
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
return|return
name|rpc
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Received RPC reply for unknown RPC (%d)."
argument_list|,
name|id
argument_list|)
argument_list|)
throw|;
block|}
specifier|private
name|void
name|handleCall
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|Method
name|handler
init|=
name|handlers
operator|.
name|get
argument_list|(
name|msg
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|==
literal|null
condition|)
block|{
name|handler
operator|=
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"handle"
argument_list|,
name|ChannelHandlerContext
operator|.
name|class
argument_list|,
name|msg
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|handler
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|handlers
operator|.
name|put
argument_list|(
name|msg
operator|.
name|getClass
argument_list|()
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
name|Rpc
operator|.
name|MessageType
name|replyType
decl_stmt|;
name|Object
name|replyPayload
decl_stmt|;
try|try
block|{
name|replyPayload
operator|=
name|handler
operator|.
name|invoke
argument_list|(
name|this
argument_list|,
name|ctx
argument_list|,
name|msg
argument_list|)
expr_stmt|;
if|if
condition|(
name|replyPayload
operator|==
literal|null
condition|)
block|{
name|replyPayload
operator|=
operator|new
name|Rpc
operator|.
name|NullMessage
argument_list|()
expr_stmt|;
block|}
name|replyType
operator|=
name|Rpc
operator|.
name|MessageType
operator|.
name|REPLY
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"[%s] Error in RPC handler."
argument_list|,
name|name
argument_list|()
argument_list|)
argument_list|,
name|ite
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
name|replyPayload
operator|=
name|Throwables
operator|.
name|getStackTraceAsString
argument_list|(
name|ite
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
name|replyType
operator|=
name|Rpc
operator|.
name|MessageType
operator|.
name|ERROR
expr_stmt|;
block|}
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|write
argument_list|(
operator|new
name|Rpc
operator|.
name|MessageHeader
argument_list|(
name|lastHeader
operator|.
name|id
argument_list|,
name|replyType
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|writeAndFlush
argument_list|(
name|replyPayload
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|handleReply
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|,
name|OutstandingRpc
name|rpc
parameter_list|)
throws|throws
name|Exception
block|{
name|rpc
operator|.
name|future
operator|.
name|setSuccess
argument_list|(
name|msg
operator|instanceof
name|Rpc
operator|.
name|NullMessage
condition|?
literal|null
else|:
name|msg
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|handleError
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|,
name|OutstandingRpc
name|rpc
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|msg
operator|instanceof
name|String
condition|)
block|{
name|rpc
operator|.
name|future
operator|.
name|setFailure
argument_list|(
operator|new
name|RpcException
argument_list|(
operator|(
name|String
operator|)
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|error
init|=
name|String
operator|.
name|format
argument_list|(
literal|"Received error with unexpected payload (%s)."
argument_list|,
name|msg
operator|!=
literal|null
condition|?
name|msg
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
else|:
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"[%s] %s"
argument_list|,
name|name
argument_list|()
argument_list|,
name|error
argument_list|)
argument_list|)
expr_stmt|;
name|rpc
operator|.
name|future
operator|.
name|setFailure
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"[%s] Caught exception in channel pipeline."
argument_list|,
name|name
argument_list|()
argument_list|)
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"[{}] Closing channel due to exception in pipeline ({})."
argument_list|,
name|name
argument_list|()
argument_list|,
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastHeader
operator|!=
literal|null
condition|)
block|{
comment|// There's an RPC waiting for a reply. Exception was most probably caught while processing
comment|// the RPC, so send an error.
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|write
argument_list|(
operator|new
name|Rpc
operator|.
name|MessageHeader
argument_list|(
name|lastHeader
operator|.
name|id
argument_list|,
name|Rpc
operator|.
name|MessageType
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|writeAndFlush
argument_list|(
name|Throwables
operator|.
name|getStackTraceAsString
argument_list|(
name|cause
argument_list|)
argument_list|)
expr_stmt|;
name|lastHeader
operator|=
literal|null
expr_stmt|;
block|}
name|ctx
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|void
name|channelInactive
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|rpcs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"[{}] Closing RPC channel with {} outstanding RPCs."
argument_list|,
name|name
argument_list|()
argument_list|,
name|rpcs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|OutstandingRpc
name|rpc
range|:
name|rpcs
control|)
block|{
name|rpc
operator|.
name|future
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|channelInactive
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
name|void
name|registerRpc
parameter_list|(
name|long
name|id
parameter_list|,
name|Promise
name|promise
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"[{}] Registered outstanding rpc {} ({})."
argument_list|,
name|name
argument_list|()
argument_list|,
name|id
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|rpcs
operator|.
name|add
argument_list|(
operator|new
name|OutstandingRpc
argument_list|(
name|id
argument_list|,
name|promise
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|discardRpc
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"[{}] Discarding failed RPC {}."
argument_list|,
name|name
argument_list|()
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|findRpc
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|OutstandingRpc
block|{
specifier|final
name|long
name|id
decl_stmt|;
specifier|final
name|Promise
name|future
decl_stmt|;
name|OutstandingRpc
parameter_list|(
name|long
name|id
parameter_list|,
name|Promise
name|future
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
name|future
operator|=
name|future
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

