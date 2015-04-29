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
name|protobuf
operator|.
name|BlockingService
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
name|service
operator|.
name|AbstractService
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
name|ContainerRunner
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

begin_class
specifier|public
class|class
name|LlapDaemonProtocolServerImpl
extends|extends
name|AbstractService
implements|implements
name|LlapDaemonProtocolBlockingPB
block|{
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
name|LlapDaemonProtocolServerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|numHandlers
decl_stmt|;
specifier|private
specifier|final
name|ContainerRunner
name|containerRunner
decl_stmt|;
specifier|private
specifier|final
name|int
name|configuredPort
decl_stmt|;
specifier|private
name|RPC
operator|.
name|Server
name|server
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|bindAddress
decl_stmt|;
specifier|public
name|LlapDaemonProtocolServerImpl
parameter_list|(
name|int
name|numHandlers
parameter_list|,
name|ContainerRunner
name|containerRunner
parameter_list|,
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|address
parameter_list|,
name|int
name|configuredPort
parameter_list|)
block|{
name|super
argument_list|(
literal|"LlapDaemonProtocolServerImpl"
argument_list|)
expr_stmt|;
name|this
operator|.
name|numHandlers
operator|=
name|numHandlers
expr_stmt|;
name|this
operator|.
name|containerRunner
operator|=
name|containerRunner
expr_stmt|;
name|this
operator|.
name|bindAddress
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|configuredPort
operator|=
name|configuredPort
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating: "
operator|+
name|LlapDaemonProtocolServerImpl
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" with port configured to: "
operator|+
name|configuredPort
argument_list|)
expr_stmt|;
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
name|containerRunner
operator|.
name|submitWork
argument_list|(
name|request
argument_list|)
expr_stmt|;
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
return|return
name|SubmitWorkResponseProto
operator|.
name|getDefaultInstance
argument_list|()
return|;
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
name|containerRunner
operator|.
name|sourceStateUpdated
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
name|SourceStateUpdatedResponseProto
operator|.
name|getDefaultInstance
argument_list|()
return|;
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
name|containerRunner
operator|.
name|queryComplete
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
name|QueryCompleteResponseProto
operator|.
name|getDefaultInstance
argument_list|()
return|;
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
name|containerRunner
operator|.
name|terminateFragment
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
name|TerminateFragmentResponseProto
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|getConfig
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|addr
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|configuredPort
argument_list|)
decl_stmt|;
try|try
block|{
name|server
operator|=
name|createServer
argument_list|(
name|LlapDaemonProtocolBlockingPB
operator|.
name|class
argument_list|,
name|addr
argument_list|,
name|conf
argument_list|,
name|numHandlers
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|LlapDaemonProtocol
operator|.
name|newReflectiveBlockingService
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
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
literal|"Failed to run RPC Server on port: "
operator|+
name|configuredPort
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|InetSocketAddress
name|serverBindAddress
init|=
name|NetUtils
operator|.
name|getConnectAddress
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|this
operator|.
name|bindAddress
operator|.
name|set
argument_list|(
name|NetUtils
operator|.
name|createSocketAddrForHost
argument_list|(
name|serverBindAddress
operator|.
name|getAddress
argument_list|()
operator|.
name|getCanonicalHostName
argument_list|()
argument_list|,
name|serverBindAddress
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Instantiated "
operator|+
name|LlapDaemonProtocolBlockingPB
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" at "
operator|+
name|bindAddress
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|VisibleForTesting
name|InetSocketAddress
name|getBindAddress
parameter_list|()
block|{
return|return
name|bindAddress
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
name|RPC
operator|.
name|Server
name|createServer
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|pbProtocol
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|int
name|numHandlers
parameter_list|,
name|BlockingService
name|blockingService
parameter_list|)
throws|throws
name|IOException
block|{
name|RPC
operator|.
name|setProtocolEngine
argument_list|(
name|conf
argument_list|,
name|pbProtocol
argument_list|,
name|ProtobufRpcEngine
operator|.
name|class
argument_list|)
expr_stmt|;
name|RPC
operator|.
name|Server
name|server
init|=
operator|new
name|RPC
operator|.
name|Builder
argument_list|(
name|conf
argument_list|)
operator|.
name|setProtocol
argument_list|(
name|pbProtocol
argument_list|)
operator|.
name|setInstance
argument_list|(
name|blockingService
argument_list|)
operator|.
name|setBindAddress
argument_list|(
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|addr
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|setNumHandlers
argument_list|(
name|numHandlers
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// TODO Add security.
return|return
name|server
return|;
block|}
block|}
end_class

end_unit

