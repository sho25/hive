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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
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
name|LlapDaemonConfiguration
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestLlapDaemonProtocolServerImpl
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|ServiceException
block|{
name|LlapDaemonConfiguration
name|daemonConf
init|=
operator|new
name|LlapDaemonConfiguration
argument_list|()
decl_stmt|;
name|LlapDaemonProtocolServerImpl
name|server
init|=
operator|new
name|LlapDaemonProtocolServerImpl
argument_list|(
name|daemonConf
argument_list|,
name|mock
argument_list|(
name|ContainerRunner
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|server
operator|.
name|init
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|InetSocketAddress
name|serverAddr
init|=
name|server
operator|.
name|getBindAddress
argument_list|()
decl_stmt|;
name|LlapDaemonProtocolBlockingPB
name|client
init|=
operator|new
name|LlapDaemonProtocolClientImpl
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|serverAddr
operator|.
name|getHostName
argument_list|()
argument_list|,
name|serverAddr
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|submitWork
argument_list|(
literal|null
argument_list|,
name|SubmitWorkRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setAmHost
argument_list|(
literal|"amhost"
argument_list|)
operator|.
name|setAmPort
argument_list|(
literal|2000
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

