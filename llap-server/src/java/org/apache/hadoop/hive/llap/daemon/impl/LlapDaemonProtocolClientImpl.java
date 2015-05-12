begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|daemon
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|NetUtil
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
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
name|QueryCompleteRequestProto
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
name|QueryCompleteResponseProto
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
name|SourceStateUpdatedRequestProto
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
name|SourceStateUpdatedResponseProto
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
name|SubmitWorkRequestProto
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
name|SubmitWorkResponseProto
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
name|TerminateFragmentRequestProto
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
name|TerminateFragmentResponseProto
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicies
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicy
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
name|ipc
operator|.
name|ProtobufRpcEngine
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
name|ipc
operator|.
name|ProtocolProxy
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
name|ipc
operator|.
name|RPC
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
name|net
operator|.
name|NetUtils
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
name|LlapDaemonProtocolBlockingPB
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

begin_comment
comment|// TODO Change all this to be based on a regular interface instead of relying on the Proto service - Exception signatures cannot be controlled without this for the moment.
end_comment

begin_class
specifier|public
class|class
name|LlapDaemonProtocolClientImpl
implements|implements
name|LlapDaemonProtocolBlockingPB
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|InetSocketAddress
name|serverAddr
decl_stmt|;
specifier|private
specifier|final
name|RetryPolicy
name|retryPolicy
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
name|LlapDaemonProtocolBlockingPB
name|proxy
decl_stmt|;
specifier|public
name|LlapDaemonProtocolClientImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|,
annotation|@
name|Nullable
name|RetryPolicy
name|retryPolicy
parameter_list|,
annotation|@
name|Nullable
name|SocketFactory
name|socketFactory
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|serverAddr
operator|=
name|NetUtils
operator|.
name|createSocketAddr
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryPolicy
operator|=
name|retryPolicy
expr_stmt|;
if|if
condition|(
name|socketFactory
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|socketFactory
operator|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|socketFactory
operator|=
name|socketFactory
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SubmitWorkResponseProto
name|submitWork
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SubmitWorkRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
return|return
name|getProxy
argument_list|()
operator|.
name|submitWork
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SourceStateUpdatedResponseProto
name|sourceStateUpdated
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SourceStateUpdatedRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
return|return
name|getProxy
argument_list|()
operator|.
name|sourceStateUpdated
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|QueryCompleteResponseProto
name|queryComplete
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|QueryCompleteRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
return|return
name|getProxy
argument_list|()
operator|.
name|queryComplete
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|TerminateFragmentResponseProto
name|terminateFragment
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|TerminateFragmentRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
return|return
name|getProxy
argument_list|()
operator|.
name|terminateFragment
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|LlapDaemonProtocolBlockingPB
name|getProxy
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|proxy
operator|==
literal|null
condition|)
block|{
name|proxy
operator|=
name|createProxy
argument_list|()
expr_stmt|;
block|}
return|return
name|proxy
return|;
block|}
specifier|public
name|LlapDaemonProtocolBlockingPB
name|createProxy
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO Fix security
name|RPC
operator|.
name|setProtocolEngine
argument_list|(
name|conf
argument_list|,
name|LlapDaemonProtocolBlockingPB
operator|.
name|class
argument_list|,
name|ProtobufRpcEngine
operator|.
name|class
argument_list|)
expr_stmt|;
name|ProtocolProxy
argument_list|<
name|LlapDaemonProtocolBlockingPB
argument_list|>
name|proxy
init|=
name|RPC
operator|.
name|getProtocolProxy
argument_list|(
name|LlapDaemonProtocolBlockingPB
operator|.
name|class
argument_list|,
literal|0
argument_list|,
name|serverAddr
argument_list|,
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
argument_list|,
name|conf
argument_list|,
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|0
argument_list|,
name|retryPolicy
argument_list|)
decl_stmt|;
return|return
name|proxy
operator|.
name|getProxy
argument_list|()
return|;
block|}
block|}
end_class

end_unit

