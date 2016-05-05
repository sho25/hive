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
name|tez
operator|.
name|dag
operator|.
name|api
package|;
end_package

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
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|EventMetaData
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

begin_comment
comment|// Proxy class within the tez.api package to access package private methods.
end_comment

begin_class
specifier|public
class|class
name|TaskSpecBuilder
block|{
specifier|public
name|TaskSpec
name|constructTaskSpec
parameter_list|(
name|DAG
name|dag
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|numSplits
parameter_list|,
name|ApplicationId
name|appId
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|Vertex
name|vertex
init|=
name|dag
operator|.
name|getVertex
argument_list|(
name|vertexName
argument_list|)
decl_stmt|;
name|ProcessorDescriptor
name|processorDescriptor
init|=
name|vertex
operator|.
name|getProcessorDescriptor
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RootInputLeafOutput
argument_list|<
name|InputDescriptor
argument_list|,
name|InputInitializerDescriptor
argument_list|>
argument_list|>
name|inputs
init|=
name|vertex
operator|.
name|getInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RootInputLeafOutput
argument_list|<
name|OutputDescriptor
argument_list|,
name|OutputCommitterDescriptor
argument_list|>
argument_list|>
name|outputs
init|=
name|vertex
operator|.
name|getOutputs
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|inputs
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|outputs
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|InputSpec
argument_list|>
name|inputSpecs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RootInputLeafOutput
argument_list|<
name|InputDescriptor
argument_list|,
name|InputInitializerDescriptor
argument_list|>
name|input
range|:
name|inputs
control|)
block|{
name|InputSpec
name|inputSpec
init|=
operator|new
name|InputSpec
argument_list|(
name|input
operator|.
name|getName
argument_list|()
argument_list|,
name|input
operator|.
name|getIODescriptor
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|inputSpecs
operator|.
name|add
argument_list|(
name|inputSpec
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|OutputSpec
argument_list|>
name|outputSpecs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RootInputLeafOutput
argument_list|<
name|OutputDescriptor
argument_list|,
name|OutputCommitterDescriptor
argument_list|>
name|output
range|:
name|outputs
control|)
block|{
name|OutputSpec
name|outputSpec
init|=
operator|new
name|OutputSpec
argument_list|(
name|output
operator|.
name|getName
argument_list|()
argument_list|,
name|output
operator|.
name|getIODescriptor
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|outputSpecs
operator|.
name|add
argument_list|(
name|outputSpec
argument_list|)
expr_stmt|;
block|}
name|TezDAGID
name|dagId
init|=
name|TezDAGID
operator|.
name|getInstance
argument_list|(
name|appId
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|TezVertexID
name|vertexId
init|=
name|TezVertexID
operator|.
name|getInstance
argument_list|(
name|dagId
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|TezTaskID
name|taskId
init|=
name|TezTaskID
operator|.
name|getInstance
argument_list|(
name|vertexId
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|TezTaskAttemptID
name|taskAttemptId
init|=
name|TezTaskAttemptID
operator|.
name|getInstance
argument_list|(
name|taskId
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|TaskSpec
argument_list|(
name|taskAttemptId
argument_list|,
name|dag
operator|.
name|getName
argument_list|()
argument_list|,
name|vertexName
argument_list|,
name|numSplits
argument_list|,
name|processorDescriptor
argument_list|,
name|inputSpecs
argument_list|,
name|outputSpecs
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|EventMetaData
name|getDestingationMetaData
parameter_list|(
name|Vertex
name|vertex
parameter_list|)
block|{
name|List
argument_list|<
name|RootInputLeafOutput
argument_list|<
name|InputDescriptor
argument_list|,
name|InputInitializerDescriptor
argument_list|>
argument_list|>
name|inputs
init|=
name|vertex
operator|.
name|getInputs
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|inputs
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|String
name|inputName
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
name|EventMetaData
name|destMeta
init|=
operator|new
name|EventMetaData
argument_list|(
name|EventMetaData
operator|.
name|EventProducerConsumerType
operator|.
name|INPUT
argument_list|,
name|vertex
operator|.
name|getName
argument_list|()
argument_list|,
name|inputName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|destMeta
return|;
block|}
block|}
end_class

end_unit

