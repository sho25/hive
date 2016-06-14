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
name|ArrayList
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
name|GroupInputSpecProto
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
name|SourceStateProto
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
name|VertexIdentifier
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
name|util
operator|.
name|ConverterUtils
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
name|TezCommonUtils
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
name|EntityDescriptor
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
name|TezUncheckedException
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
name|api
operator|.
name|event
operator|.
name|VertexState
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
name|TaskContext
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
name|GroupInputSpec
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

begin_class
specifier|public
class|class
name|Converters
block|{
specifier|public
specifier|static
name|TaskSpec
name|getTaskSpecfromProto
parameter_list|(
name|SignableVertexSpec
name|vectorProto
parameter_list|,
name|int
name|fragmentNum
parameter_list|,
name|int
name|attemptNum
parameter_list|,
name|TezTaskAttemptID
name|attemptId
parameter_list|)
block|{
name|VertexIdentifier
name|vertexId
init|=
name|vectorProto
operator|.
name|getVertexIdentifier
argument_list|()
decl_stmt|;
name|TezTaskAttemptID
name|taskAttemptID
init|=
name|attemptId
operator|!=
literal|null
condition|?
name|attemptId
else|:
name|createTaskAttemptId
argument_list|(
name|vertexId
argument_list|,
name|fragmentNum
argument_list|,
name|attemptNum
argument_list|)
decl_stmt|;
name|ProcessorDescriptor
name|processorDescriptor
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|vectorProto
operator|.
name|hasProcessorDescriptor
argument_list|()
condition|)
block|{
name|processorDescriptor
operator|=
name|convertProcessorDescriptorFromProto
argument_list|(
name|vectorProto
operator|.
name|getProcessorDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|InputSpec
argument_list|>
name|inputSpecList
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSpec
argument_list|>
argument_list|(
name|vectorProto
operator|.
name|getInputSpecsCount
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vectorProto
operator|.
name|getInputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|inputSpecProto
range|:
name|vectorProto
operator|.
name|getInputSpecsList
argument_list|()
control|)
block|{
name|inputSpecList
operator|.
name|add
argument_list|(
name|getInputSpecFromProto
argument_list|(
name|inputSpecProto
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|OutputSpec
argument_list|>
name|outputSpecList
init|=
operator|new
name|ArrayList
argument_list|<
name|OutputSpec
argument_list|>
argument_list|(
name|vectorProto
operator|.
name|getOutputSpecsCount
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vectorProto
operator|.
name|getOutputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|outputSpecProto
range|:
name|vectorProto
operator|.
name|getOutputSpecsList
argument_list|()
control|)
block|{
name|outputSpecList
operator|.
name|add
argument_list|(
name|getOutputSpecFromProto
argument_list|(
name|outputSpecProto
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|GroupInputSpec
argument_list|>
name|groupInputSpecs
init|=
operator|new
name|ArrayList
argument_list|<
name|GroupInputSpec
argument_list|>
argument_list|(
name|vectorProto
operator|.
name|getGroupedInputSpecsCount
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vectorProto
operator|.
name|getGroupedInputSpecsCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|GroupInputSpecProto
name|groupInputSpecProto
range|:
name|vectorProto
operator|.
name|getGroupedInputSpecsList
argument_list|()
control|)
block|{
name|groupInputSpecs
operator|.
name|add
argument_list|(
name|getGroupInputSpecFromProto
argument_list|(
name|groupInputSpecProto
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|TaskSpec
name|taskSpec
init|=
operator|new
name|TaskSpec
argument_list|(
name|taskAttemptID
argument_list|,
name|vectorProto
operator|.
name|getDagName
argument_list|()
argument_list|,
name|vectorProto
operator|.
name|getVertexName
argument_list|()
argument_list|,
name|vectorProto
operator|.
name|getVertexParallelism
argument_list|()
argument_list|,
name|processorDescriptor
argument_list|,
name|inputSpecList
argument_list|,
name|outputSpecList
argument_list|,
name|groupInputSpecs
argument_list|)
decl_stmt|;
return|return
name|taskSpec
return|;
block|}
specifier|public
specifier|static
name|TezTaskAttemptID
name|createTaskAttemptId
parameter_list|(
name|VertexIdentifier
name|vertexId
parameter_list|,
name|int
name|fragmentNum
parameter_list|,
name|int
name|attemptNum
parameter_list|)
block|{
comment|// Come ride the API roller-coaster!
return|return
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|ConverterUtils
operator|.
name|toApplicationId
argument_list|(
name|vertexId
operator|.
name|getApplicationIdString
argument_list|()
argument_list|)
argument_list|,
name|vertexId
operator|.
name|getDagId
argument_list|()
argument_list|)
argument_list|,
name|vertexId
operator|.
name|getVertexId
argument_list|()
argument_list|)
argument_list|,
name|fragmentNum
argument_list|)
argument_list|,
name|attemptNum
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TezTaskAttemptID
name|createTaskAttemptId
parameter_list|(
name|TaskContext
name|ctx
parameter_list|)
block|{
comment|// Come ride the API roller-coaster #2! The best part is that ctx has TezTaskAttemptID inside.
return|return
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|ctx
operator|.
name|getApplicationId
argument_list|()
argument_list|,
name|ctx
operator|.
name|getDagIdentifier
argument_list|()
argument_list|)
argument_list|,
name|ctx
operator|.
name|getTaskVertexIndex
argument_list|()
argument_list|)
argument_list|,
name|ctx
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
argument_list|,
name|ctx
operator|.
name|getTaskAttemptNumber
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VertexIdentifier
name|createVertexIdentifier
parameter_list|(
name|TezTaskAttemptID
name|taId
parameter_list|,
name|int
name|appAttemptId
parameter_list|)
block|{
name|VertexIdentifier
operator|.
name|Builder
name|idBuilder
init|=
name|VertexIdentifier
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|idBuilder
operator|.
name|setApplicationIdString
argument_list|(
name|taId
operator|.
name|getTaskID
argument_list|()
operator|.
name|getVertexID
argument_list|()
operator|.
name|getDAGId
argument_list|()
operator|.
name|getApplicationId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|idBuilder
operator|.
name|setAppAttemptNumber
argument_list|(
name|appAttemptId
argument_list|)
expr_stmt|;
name|idBuilder
operator|.
name|setDagId
argument_list|(
name|taId
operator|.
name|getTaskID
argument_list|()
operator|.
name|getVertexID
argument_list|()
operator|.
name|getDAGId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|idBuilder
operator|.
name|setVertexId
argument_list|(
name|taId
operator|.
name|getTaskID
argument_list|()
operator|.
name|getVertexID
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|idBuilder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SignableVertexSpec
operator|.
name|Builder
name|convertTaskSpecToProto
parameter_list|(
name|TaskSpec
name|taskSpec
parameter_list|,
name|int
name|appAttemptId
parameter_list|,
name|String
name|tokenIdentifier
parameter_list|,
name|String
name|user
parameter_list|)
block|{
name|TezTaskAttemptID
name|tId
init|=
name|taskSpec
operator|.
name|getTaskAttemptID
argument_list|()
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
name|createVertexIdentifier
argument_list|(
name|tId
argument_list|,
name|appAttemptId
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDagName
argument_list|(
name|taskSpec
operator|.
name|getDAGName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setVertexName
argument_list|(
name|taskSpec
operator|.
name|getVertexName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setVertexParallelism
argument_list|(
name|taskSpec
operator|.
name|getVertexParallelism
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setTokenIdentifier
argument_list|(
name|tokenIdentifier
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setUser
argument_list|(
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|taskSpec
operator|.
name|getProcessorDescriptor
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setProcessorDescriptor
argument_list|(
name|convertToProto
argument_list|(
name|taskSpec
operator|.
name|getProcessorDescriptor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|taskSpec
operator|.
name|getInputs
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|taskSpec
operator|.
name|getInputs
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|InputSpec
name|inputSpec
range|:
name|taskSpec
operator|.
name|getInputs
argument_list|()
control|)
block|{
name|builder
operator|.
name|addInputSpecs
argument_list|(
name|convertInputSpecToProto
argument_list|(
name|inputSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|taskSpec
operator|.
name|getOutputs
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|taskSpec
operator|.
name|getOutputs
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|OutputSpec
name|outputSpec
range|:
name|taskSpec
operator|.
name|getOutputs
argument_list|()
control|)
block|{
name|builder
operator|.
name|addOutputSpecs
argument_list|(
name|convertOutputSpecToProto
argument_list|(
name|outputSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|taskSpec
operator|.
name|getGroupInputs
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|taskSpec
operator|.
name|getGroupInputs
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|GroupInputSpec
name|groupInputSpec
range|:
name|taskSpec
operator|.
name|getGroupInputs
argument_list|()
control|)
block|{
name|builder
operator|.
name|addGroupedInputSpecs
argument_list|(
name|convertGroupInputSpecToProto
argument_list|(
name|groupInputSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
specifier|private
specifier|static
name|ProcessorDescriptor
name|convertProcessorDescriptorFromProto
parameter_list|(
name|EntityDescriptorProto
name|proto
parameter_list|)
block|{
name|String
name|className
init|=
name|proto
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|UserPayload
name|payload
init|=
name|convertPayloadFromProto
argument_list|(
name|proto
argument_list|)
decl_stmt|;
name|ProcessorDescriptor
name|pd
init|=
name|ProcessorDescriptor
operator|.
name|create
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|setUserPayload
argument_list|(
name|pd
argument_list|,
name|payload
argument_list|)
expr_stmt|;
return|return
name|pd
return|;
block|}
specifier|private
specifier|static
name|EntityDescriptorProto
name|convertToProto
parameter_list|(
name|EntityDescriptor
argument_list|<
name|?
argument_list|>
name|descriptor
parameter_list|)
block|{
name|EntityDescriptorProto
operator|.
name|Builder
name|builder
init|=
name|EntityDescriptorProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setClassName
argument_list|(
name|descriptor
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
name|UserPayload
name|userPayload
init|=
name|descriptor
operator|.
name|getUserPayload
argument_list|()
decl_stmt|;
if|if
condition|(
name|userPayload
operator|!=
literal|null
condition|)
block|{
name|UserPayloadProto
operator|.
name|Builder
name|payloadBuilder
init|=
name|UserPayloadProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|userPayload
operator|.
name|hasPayload
argument_list|()
condition|)
block|{
name|payloadBuilder
operator|.
name|setUserPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|userPayload
operator|.
name|getPayload
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|payloadBuilder
operator|.
name|setVersion
argument_list|(
name|userPayload
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setUserPayload
argument_list|(
name|payloadBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|descriptor
operator|.
name|getHistoryText
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|builder
operator|.
name|setHistoryText
argument_list|(
name|TezCommonUtils
operator|.
name|compressByteArrayToByteString
argument_list|(
name|descriptor
operator|.
name|getHistoryText
argument_list|()
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
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
name|TezUncheckedException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|InputSpec
name|getInputSpecFromProto
parameter_list|(
name|IOSpecProto
name|inputSpecProto
parameter_list|)
block|{
name|InputDescriptor
name|inputDescriptor
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|inputSpecProto
operator|.
name|hasIoDescriptor
argument_list|()
condition|)
block|{
name|inputDescriptor
operator|=
name|convertInputDescriptorFromProto
argument_list|(
name|inputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|InputSpec
name|inputSpec
init|=
operator|new
name|InputSpec
argument_list|(
name|inputSpecProto
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|,
name|inputDescriptor
argument_list|,
name|inputSpecProto
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|inputSpec
return|;
block|}
specifier|private
specifier|static
name|InputDescriptor
name|convertInputDescriptorFromProto
parameter_list|(
name|EntityDescriptorProto
name|proto
parameter_list|)
block|{
name|String
name|className
init|=
name|proto
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|UserPayload
name|payload
init|=
name|convertPayloadFromProto
argument_list|(
name|proto
argument_list|)
decl_stmt|;
name|InputDescriptor
name|id
init|=
name|InputDescriptor
operator|.
name|create
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|setUserPayload
argument_list|(
name|id
argument_list|,
name|payload
argument_list|)
expr_stmt|;
return|return
name|id
return|;
block|}
specifier|private
specifier|static
name|OutputDescriptor
name|convertOutputDescriptorFromProto
parameter_list|(
name|EntityDescriptorProto
name|proto
parameter_list|)
block|{
name|String
name|className
init|=
name|proto
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|UserPayload
name|payload
init|=
name|convertPayloadFromProto
argument_list|(
name|proto
argument_list|)
decl_stmt|;
name|OutputDescriptor
name|od
init|=
name|OutputDescriptor
operator|.
name|create
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|setUserPayload
argument_list|(
name|od
argument_list|,
name|payload
argument_list|)
expr_stmt|;
return|return
name|od
return|;
block|}
specifier|private
specifier|static
name|IOSpecProto
name|convertInputSpecToProto
parameter_list|(
name|InputSpec
name|inputSpec
parameter_list|)
block|{
name|IOSpecProto
operator|.
name|Builder
name|builder
init|=
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputSpec
operator|.
name|getSourceVertexName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setConnectedVertexName
argument_list|(
name|inputSpec
operator|.
name|getSourceVertexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|inputSpec
operator|.
name|getInputDescriptor
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setIoDescriptor
argument_list|(
name|convertToProto
argument_list|(
name|inputSpec
operator|.
name|getInputDescriptor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setPhysicalEdgeCount
argument_list|(
name|inputSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|OutputSpec
name|getOutputSpecFromProto
parameter_list|(
name|IOSpecProto
name|outputSpecProto
parameter_list|)
block|{
name|OutputDescriptor
name|outputDescriptor
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|outputSpecProto
operator|.
name|hasIoDescriptor
argument_list|()
condition|)
block|{
name|outputDescriptor
operator|=
name|convertOutputDescriptorFromProto
argument_list|(
name|outputSpecProto
operator|.
name|getIoDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|OutputSpec
name|outputSpec
init|=
operator|new
name|OutputSpec
argument_list|(
name|outputSpecProto
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|,
name|outputDescriptor
argument_list|,
name|outputSpecProto
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|outputSpec
return|;
block|}
specifier|public
specifier|static
name|IOSpecProto
name|convertOutputSpecToProto
parameter_list|(
name|OutputSpec
name|outputSpec
parameter_list|)
block|{
name|IOSpecProto
operator|.
name|Builder
name|builder
init|=
name|IOSpecProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputSpec
operator|.
name|getDestinationVertexName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setConnectedVertexName
argument_list|(
name|outputSpec
operator|.
name|getDestinationVertexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|outputSpec
operator|.
name|getOutputDescriptor
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setIoDescriptor
argument_list|(
name|convertToProto
argument_list|(
name|outputSpec
operator|.
name|getOutputDescriptor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setPhysicalEdgeCount
argument_list|(
name|outputSpec
operator|.
name|getPhysicalEdgeCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|GroupInputSpec
name|getGroupInputSpecFromProto
parameter_list|(
name|GroupInputSpecProto
name|groupInputSpecProto
parameter_list|)
block|{
name|GroupInputSpec
name|groupSpec
init|=
operator|new
name|GroupInputSpec
argument_list|(
name|groupInputSpecProto
operator|.
name|getGroupName
argument_list|()
argument_list|,
name|groupInputSpecProto
operator|.
name|getGroupVerticesList
argument_list|()
argument_list|,
name|convertInputDescriptorFromProto
argument_list|(
name|groupInputSpecProto
operator|.
name|getMergedInputDescriptor
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|groupSpec
return|;
block|}
specifier|private
specifier|static
name|GroupInputSpecProto
name|convertGroupInputSpecToProto
parameter_list|(
name|GroupInputSpec
name|groupInputSpec
parameter_list|)
block|{
name|GroupInputSpecProto
operator|.
name|Builder
name|builder
init|=
name|GroupInputSpecProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setGroupName
argument_list|(
name|groupInputSpec
operator|.
name|getGroupName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addAllGroupVertices
argument_list|(
name|groupInputSpec
operator|.
name|getGroupVertices
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMergedInputDescriptor
argument_list|(
name|convertToProto
argument_list|(
name|groupInputSpec
operator|.
name|getMergedInputDescriptor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|setUserPayload
parameter_list|(
name|EntityDescriptor
argument_list|<
name|?
argument_list|>
name|entity
parameter_list|,
name|UserPayload
name|payload
parameter_list|)
block|{
if|if
condition|(
name|payload
operator|!=
literal|null
condition|)
block|{
name|entity
operator|.
name|setUserPayload
argument_list|(
name|payload
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|UserPayload
name|convertPayloadFromProto
parameter_list|(
name|EntityDescriptorProto
name|proto
parameter_list|)
block|{
name|UserPayload
name|userPayload
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasUserPayload
argument_list|()
condition|)
block|{
if|if
condition|(
name|proto
operator|.
name|getUserPayload
argument_list|()
operator|.
name|hasUserPayload
argument_list|()
condition|)
block|{
name|userPayload
operator|=
name|UserPayload
operator|.
name|create
argument_list|(
name|proto
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getUserPayload
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|,
name|proto
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|userPayload
operator|=
name|UserPayload
operator|.
name|create
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|userPayload
return|;
block|}
specifier|public
specifier|static
name|SourceStateProto
name|fromVertexState
parameter_list|(
name|VertexState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|SUCCEEDED
case|:
return|return
name|SourceStateProto
operator|.
name|S_SUCCEEDED
return|;
case|case
name|RUNNING
case|:
return|return
name|SourceStateProto
operator|.
name|S_RUNNING
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected state: "
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

