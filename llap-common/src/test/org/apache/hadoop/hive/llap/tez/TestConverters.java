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
name|tez
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertArrayEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
operator|.
name|EntityDescriptorProto
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
name|IOSpecProto
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
name|SignableVertexSpec
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
name|UserPayloadProto
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
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|InputDescriptor
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
name|dag
operator|.
name|api
operator|.
name|OutputDescriptor
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
name|dag
operator|.
name|api
operator|.
name|ProcessorDescriptor
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
name|dag
operator|.
name|api
operator|.
name|UserPayload
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
name|dag
operator|.
name|records
operator|.
name|TezDAGID
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
name|dag
operator|.
name|records
operator|.
name|TezTaskAttemptID
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
name|dag
operator|.
name|records
operator|.
name|TezTaskID
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
name|dag
operator|.
name|records
operator|.
name|TezVertexID
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
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|InputSpec
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
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|OutputSpec
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
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|TaskSpec
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
name|TestConverters
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|5000
argument_list|)
specifier|public
name|void
name|testTaskSpecToFragmentSpec
parameter_list|()
block|{
name|ByteBuffer
name|procBb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|procBb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|UserPayload
name|processorPayload
init|=
name|UserPayload
operator|.
name|create
argument_list|(
name|procBb
argument_list|)
decl_stmt|;
name|ProcessorDescriptor
name|processorDescriptor
init|=
name|ProcessorDescriptor
operator|.
name|create
argument_list|(
literal|"fakeProcessorName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|processorPayload
argument_list|)
decl_stmt|;
name|ByteBuffer
name|input1Bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|input1Bb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|300
argument_list|)
expr_stmt|;
name|UserPayload
name|input1Payload
init|=
name|UserPayload
operator|.
name|create
argument_list|(
name|input1Bb
argument_list|)
decl_stmt|;
name|InputDescriptor
name|id1
init|=
name|InputDescriptor
operator|.
name|create
argument_list|(
literal|"input1ClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|input1Payload
argument_list|)
decl_stmt|;
name|InputSpec
name|inputSpec1
init|=
operator|new
name|InputSpec
argument_list|(
literal|"sourceVertexName1"
argument_list|,
name|id1
argument_list|,
literal|33
argument_list|)
decl_stmt|;
name|InputSpec
name|inputSpec2
init|=
operator|new
name|InputSpec
argument_list|(
literal|"sourceVertexName2"
argument_list|,
name|id1
argument_list|,
literal|44
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSpec
argument_list|>
name|inputSpecList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|inputSpec1
argument_list|,
name|inputSpec2
argument_list|)
decl_stmt|;
name|ByteBuffer
name|output1Bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|output1Bb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|400
argument_list|)
expr_stmt|;
name|UserPayload
name|output1Payload
init|=
name|UserPayload
operator|.
name|create
argument_list|(
name|output1Bb
argument_list|)
decl_stmt|;
name|OutputDescriptor
name|od1
init|=
name|OutputDescriptor
operator|.
name|create
argument_list|(
literal|"output1ClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|output1Payload
argument_list|)
decl_stmt|;
name|OutputSpec
name|outputSpec1
init|=
operator|new
name|OutputSpec
argument_list|(
literal|"destVertexName1"
argument_list|,
name|od1
argument_list|,
literal|55
argument_list|)
decl_stmt|;
name|OutputSpec
name|outputSpec2
init|=
operator|new
name|OutputSpec
argument_list|(
literal|"destVertexName2"
argument_list|,
name|od1
argument_list|,
literal|66
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|OutputSpec
argument_list|>
name|outputSpecList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|outputSpec1
argument_list|,
name|outputSpec2
argument_list|)
decl_stmt|;
name|ApplicationId
name|appId
init|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
literal|1000
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|TezDAGID
name|tezDagId
init|=
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|appId
argument_list|,
literal|300
argument_list|)
decl_stmt|;
name|TezVertexID
name|tezVertexId
init|=
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|tezDagId
argument_list|,
literal|400
argument_list|)
decl_stmt|;
name|TezTaskID
name|tezTaskId
init|=
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|tezVertexId
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|TezTaskAttemptID
name|tezTaskAttemptId
init|=
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|tezTaskId
argument_list|,
literal|600
argument_list|)
decl_stmt|;
name|TaskSpec
name|taskSpec
init|=
operator|new
name|TaskSpec
argument_list|(
name|tezTaskAttemptId
argument_list|,
literal|"dagName"
argument_list|,
literal|"vertexName"
argument_list|,
literal|10
argument_list|,
name|processorDescriptor
argument_list|,
name|inputSpecList
argument_list|,
name|outputSpecList
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SignableVertexSpec
name|vertexProto
init|=
name|Converters
operator|.
name|convertTaskSpecToProto
argument_list|(
name|taskSpec
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"dagName"
argument_list|,
name|vertexProto
operator|.
name|getDagName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"vertexName"
argument_list|,
name|vertexProto
operator|.
name|getVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|appId
operator|.
name|toString
argument_list|()
argument_list|,
name|vertexProto
operator|.
name|getVertexIdentifier
argument_list|()
operator|.
name|getApplicationIdString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tezDagId
operator|.
name|getId
argument_list|()
argument_list|,
name|vertexProto
operator|.
name|getVertexIdentifier
argument_list|()
operator|.
name|getDagId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|processorDescriptor
operator|.
name|getClassName
argument_list|()
argument_list|,
name|vertexProto
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|processorDescriptor
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
argument_list|,
name|vertexProto
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vertexProto
operator|.
name|getInputSpecsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vertexProto
operator|.
name|getOutputSpecsCount
argument_list|()
argument_list|)
expr_stmt|;
name|verifyInputSpecAndProto
argument_list|(
name|inputSpec1
argument_list|,
name|vertexProto
operator|.
name|getInputSpecs
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|verifyInputSpecAndProto
argument_list|(
name|inputSpec2
argument_list|,
name|vertexProto
operator|.
name|getInputSpecs
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|verifyOutputSpecAndProto
argument_list|(
name|outputSpec1
argument_list|,
name|vertexProto
operator|.
name|getOutputSpecs
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|verifyOutputSpecAndProto
argument_list|(
name|outputSpec2
argument_list|,
name|vertexProto
operator|.
name|getOutputSpecs
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|5000
argument_list|)
specifier|public
name|void
name|testFragmentSpecToTaskSpec
parameter_list|()
block|{
name|ByteBuffer
name|procBb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|procBb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|ByteBuffer
name|input1Bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|input1Bb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|300
argument_list|)
expr_stmt|;
name|ByteBuffer
name|output1Bb
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|output1Bb
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
literal|400
argument_list|)
expr_stmt|;
name|ApplicationId
name|appId
init|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
literal|1000
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|TezDAGID
name|tezDagId
init|=
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|appId
argument_list|,
literal|300
argument_list|)
decl_stmt|;
name|TezVertexID
name|tezVertexId
init|=
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|tezDagId
argument_list|,
literal|400
argument_list|)
decl_stmt|;
name|TezTaskID
name|tezTaskId
init|=
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|tezVertexId
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|TezTaskAttemptID
name|tezTaskAttemptId
init|=
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|tezTaskId
argument_list|,
literal|600
argument_list|)
decl_stmt|;
name|SignableVertexSpec
operator|.
name|Builder
name|builder
init|=
name|SignableVertexSpec
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setVertexIdentifier
argument_list|(
name|Converters
operator|.
name|createVertexIdentifier
argument_list|(
name|tezTaskAttemptId
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDagName
argument_list|(
literal|"dagName"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setVertexName
argument_list|(
literal|"vertexName"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setProcessorDescriptor
argument_list|(
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"fakeProcessorName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|procBb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addInputSpecs
argument_list|(
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setConnectedVertexName
argument_list|(
literal|"sourceVertexName1"
argument_list|)
operator|.
name|setPhysicalEdgeCount
argument_list|(
literal|33
argument_list|)
operator|.
name|setIoDescriptor
argument_list|(
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"input1ClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|input1Bb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addInputSpecs
argument_list|(
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setConnectedVertexName
argument_list|(
literal|"sourceVertexName2"
argument_list|)
operator|.
name|setPhysicalEdgeCount
argument_list|(
literal|44
argument_list|)
operator|.
name|setIoDescriptor
argument_list|(
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"input1ClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|input1Bb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addOutputSpecs
argument_list|(
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setConnectedVertexName
argument_list|(
literal|"destVertexName1"
argument_list|)
operator|.
name|setPhysicalEdgeCount
argument_list|(
literal|55
argument_list|)
operator|.
name|setIoDescriptor
argument_list|(
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"outputClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|output1Bb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addOutputSpecs
argument_list|(
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setConnectedVertexName
argument_list|(
literal|"destVertexName2"
argument_list|)
operator|.
name|setPhysicalEdgeCount
argument_list|(
literal|66
argument_list|)
operator|.
name|setIoDescriptor
argument_list|(
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setClassName
argument_list|(
literal|"outputClassName"
argument_list|)
operator|.
name|setUserPayload
argument_list|(
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|output1Bb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|SignableVertexSpec
name|vertexProto
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|TaskSpec
name|taskSpec
init|=
name|Converters
operator|.
name|getTaskSpecfromProto
argument_list|(
name|vertexProto
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"dagName"
argument_list|,
name|taskSpec
operator|.
name|getDAGName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"vertexName"
argument_list|,
name|taskSpec
operator|.
name|getVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tezTaskAttemptId
argument_list|,
name|taskSpec
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fakeProcessorName"
argument_list|,
name|taskSpec
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|serialized
init|=
operator|new
name|byte
index|[
name|taskSpec
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
operator|.
name|remaining
argument_list|()
index|]
decl_stmt|;
name|taskSpec
operator|.
name|getProcessorDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
operator|.
name|get
argument_list|(
name|serialized
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|procBb
operator|.
name|array
argument_list|()
argument_list|,
name|serialized
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|taskSpec
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|taskSpec
operator|.
name|getOutputs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|verifyInputSpecAndProto
argument_list|(
name|taskSpec
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|vertexProto
operator|.
name|getInputSpecs
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|verifyInputSpecAndProto
argument_list|(
name|taskSpec
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|vertexProto
operator|.
name|getInputSpecs
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|verifyOutputSpecAndProto
argument_list|(
name|taskSpec
operator|.
name|getOutputs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|vertexProto
operator|.
name|getOutputSpecs
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|verifyOutputSpecAndProto
argument_list|(
name|taskSpec
operator|.
name|getOutputs
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|vertexProto
operator|.
name|getOutputSpecs
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyInputSpecAndProto
parameter_list|(
name|InputSpec
name|inputSpec
parameter_list|,
name|IOSpecProto
name|inputSpecProto
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|inputSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|,
name|inputSpecProto
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|inputSpec
operator|.
name|getSourceVertexName
argument_list|()
argument_list|,
name|inputSpecProto
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|inputSpec
operator|.
name|getInputDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|,
name|inputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|inputSpec
operator|.
name|getInputDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
argument_list|,
name|inputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyOutputSpecAndProto
parameter_list|(
name|OutputSpec
name|outputSpec
parameter_list|,
name|IOSpecProto
name|outputSpecProto
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|outputSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|,
name|outputSpecProto
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|outputSpec
operator|.
name|getDestinationVertexName
argument_list|()
argument_list|,
name|outputSpecProto
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|outputSpec
operator|.
name|getOutputDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|,
name|outputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|outputSpec
operator|.
name|getOutputDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
argument_list|,
name|outputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

