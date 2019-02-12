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
name|tezplugins
operator|.
name|endpoint
package|;
end_package

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
name|LlapUtil
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
name|plugin
operator|.
name|rpc
operator|.
name|LlapPluginProtocolProtos
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
name|plugin
operator|.
name|rpc
operator|.
name|LlapPluginProtocolProtos
operator|.
name|UpdateQueryRequestProto
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
name|plugin
operator|.
name|rpc
operator|.
name|LlapPluginProtocolProtos
operator|.
name|UpdateQueryResponseProto
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
name|protocol
operator|.
name|LlapPluginProtocolPB
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
name|tezplugins
operator|.
name|LlapTaskSchedulerService
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
name|security
operator|.
name|token
operator|.
name|SecretManager
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
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|JobTokenIdentifier
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
name|LlapPluginServerImpl
extends|extends
name|AbstractService
implements|implements
name|LlapPluginProtocolPB
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
name|LlapPluginServerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RPC
operator|.
name|Server
name|server
decl_stmt|;
specifier|private
specifier|final
name|SecretManager
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|secretManager
decl_stmt|;
specifier|private
specifier|final
name|int
name|numHandlers
decl_stmt|;
specifier|private
specifier|final
name|LlapTaskSchedulerService
name|parent
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|bindAddress
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|public
name|LlapPluginServerImpl
parameter_list|(
name|SecretManager
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|secretManager
parameter_list|,
name|int
name|numHandlers
parameter_list|,
name|LlapTaskSchedulerService
name|parent
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|super
argument_list|(
literal|"LlapPluginServerImpl"
argument_list|)
expr_stmt|;
name|this
operator|.
name|secretManager
operator|=
name|secretManager
expr_stmt|;
name|this
operator|.
name|numHandlers
operator|=
name|numHandlers
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
operator|<=
literal|0
condition|?
literal|0
else|:
name|port
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap plugin server using port: {} #handlers: {}"
argument_list|,
name|port
argument_list|,
name|numHandlers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|UpdateQueryResponseProto
name|updateQuery
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|UpdateQueryRequestProto
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|parent
operator|.
name|updateQuery
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
name|UpdateQueryResponseProto
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
specifier|final
name|Configuration
name|conf
init|=
name|getConfig
argument_list|()
decl_stmt|;
specifier|final
name|BlockingService
name|daemonImpl
init|=
name|LlapPluginProtocolProtos
operator|.
name|LlapPluginProtocol
operator|.
name|newReflectiveBlockingService
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|server
operator|=
name|LlapUtil
operator|.
name|startProtocolServer
argument_list|(
name|port
argument_list|,
name|numHandlers
argument_list|,
name|bindAddress
argument_list|,
name|conf
argument_list|,
name|daemonImpl
argument_list|,
name|LlapPluginProtocolPB
operator|.
name|class
argument_list|,
name|secretManager
argument_list|,
operator|new
name|LlapPluginPolicyProvider
argument_list|()
argument_list|,
name|ConfVars
operator|.
name|LLAP_PLUGIN_ACL
argument_list|,
name|ConfVars
operator|.
name|LLAP_PLUGIN_ACL_DENY
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting the plugin endpoint on port "
operator|+
name|bindAddress
operator|.
name|get
argument_list|()
operator|.
name|getPort
argument_list|()
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
specifier|public
name|int
name|getActualPort
parameter_list|()
block|{
name|InetSocketAddress
name|bindAddress
init|=
name|this
operator|.
name|bindAddress
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|bindAddress
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get port before the service is started"
argument_list|)
throw|;
block|}
return|return
name|bindAddress
operator|.
name|getPort
argument_list|()
return|;
block|}
block|}
end_class

end_unit

