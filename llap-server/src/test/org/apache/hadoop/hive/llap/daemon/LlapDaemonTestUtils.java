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
name|ByteString
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
name|tezplugins
operator|.
name|LlapTezUtils
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
name|Credentials
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

begin_comment
comment|/**  * Utils class for testing Llap Daemon.  */
end_comment

begin_class
specifier|public
class|class
name|LlapDaemonTestUtils
block|{
specifier|private
name|LlapDaemonTestUtils
parameter_list|()
block|{}
specifier|public
specifier|static
name|SubmitWorkRequestProto
name|buildSubmitProtoRequest
parameter_list|(
name|int
name|fragmentNumber
parameter_list|,
name|String
name|appId
parameter_list|,
name|int
name|dagId
parameter_list|,
name|int
name|vId
parameter_list|,
name|String
name|dagName
parameter_list|,
name|int
name|dagStartTime
parameter_list|,
name|int
name|attemptStartTime
parameter_list|,
name|int
name|numSelfAndUpstreamTasks
parameter_list|,
name|int
name|numSelfAndUpstreamComplete
parameter_list|,
name|int
name|withinDagPriority
parameter_list|,
name|Credentials
name|credentials
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|SubmitWorkRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setAttemptNumber
argument_list|(
literal|0
argument_list|)
operator|.
name|setFragmentNumber
argument_list|(
name|fragmentNumber
argument_list|)
operator|.
name|setWorkSpec
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|VertexOrBinary
operator|.
name|newBuilder
argument_list|()
operator|.
name|setVertex
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SignableVertexSpec
operator|.
name|newBuilder
argument_list|()
operator|.
name|setQueryIdentifier
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|QueryIdentifierProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setApplicationIdString
argument_list|(
name|appId
argument_list|)
operator|.
name|setAppAttemptNumber
argument_list|(
literal|0
argument_list|)
operator|.
name|setDagIndex
argument_list|(
name|dagId
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setVertexIndex
argument_list|(
name|vId
argument_list|)
operator|.
name|setDagName
argument_list|(
name|dagName
argument_list|)
operator|.
name|setHiveQueryId
argument_list|(
name|dagName
argument_list|)
operator|.
name|setVertexName
argument_list|(
literal|"MockVertex"
argument_list|)
operator|.
name|setUser
argument_list|(
literal|"MockUser"
argument_list|)
operator|.
name|setTokenIdentifier
argument_list|(
literal|"MockToken_1"
argument_list|)
operator|.
name|setProcessorDescriptor
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"MockProcessor"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setAmHost
argument_list|(
literal|"localhost"
argument_list|)
operator|.
name|setAmPort
argument_list|(
literal|12345
argument_list|)
operator|.
name|setCredentialsBinary
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|LlapTezUtils
operator|.
name|serializeCredentials
argument_list|(
name|credentials
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setContainerIdString
argument_list|(
literal|"MockContainer_1"
argument_list|)
operator|.
name|setFragmentRuntimeInfo
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|FragmentRuntimeInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDagStartTime
argument_list|(
name|dagStartTime
argument_list|)
operator|.
name|setFirstAttemptStartTime
argument_list|(
name|attemptStartTime
argument_list|)
operator|.
name|setNumSelfAndUpstreamTasks
argument_list|(
name|numSelfAndUpstreamTasks
argument_list|)
operator|.
name|setNumSelfAndUpstreamCompletedTasks
argument_list|(
name|numSelfAndUpstreamComplete
argument_list|)
operator|.
name|setWithinDagPriority
argument_list|(
name|withinDagPriority
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

