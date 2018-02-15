begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|EdgeManagerPlugin
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
name|EdgeManagerPluginContext
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
name|events
operator|.
name|DataMovementEvent
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
name|events
operator|.
name|InputReadErrorEvent
import|;
end_import

begin_class
specifier|public
class|class
name|CustomPartitionEdge
extends|extends
name|EdgeManagerPlugin
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
name|CustomPartitionEdge
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|CustomEdgeConfiguration
name|conf
init|=
literal|null
decl_stmt|;
specifier|final
name|EdgeManagerPluginContext
name|context
decl_stmt|;
comment|// used by the framework at runtime. initialize is the real initializer at runtime
specifier|public
name|CustomPartitionEdge
parameter_list|(
name|EdgeManagerPluginContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumDestinationTaskPhysicalInputs
parameter_list|(
name|int
name|destinationTaskIndex
parameter_list|)
block|{
return|return
name|context
operator|.
name|getSourceVertexNumTasks
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumSourceTaskPhysicalOutputs
parameter_list|(
name|int
name|sourceTaskIndex
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getNumBuckets
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumDestinationConsumerTasks
parameter_list|(
name|int
name|sourceTaskIndex
parameter_list|)
block|{
return|return
name|context
operator|.
name|getDestinationVertexNumTasks
argument_list|()
return|;
block|}
comment|// called at runtime to initialize the custom edge.
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|()
block|{
name|ByteBuffer
name|payload
init|=
name|context
operator|.
name|getUserPayload
argument_list|()
operator|.
name|getPayload
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing the edge, payload: "
operator|+
name|payload
argument_list|)
expr_stmt|;
if|if
condition|(
name|payload
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid payload"
argument_list|)
throw|;
block|}
comment|// De-serialization code
name|DataInputByteBuffer
name|dibb
init|=
operator|new
name|DataInputByteBuffer
argument_list|()
decl_stmt|;
name|dibb
operator|.
name|reset
argument_list|(
name|payload
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|CustomEdgeConfiguration
argument_list|()
expr_stmt|;
try|try
block|{
name|conf
operator|.
name|readFields
argument_list|(
name|dibb
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Routing table: "
operator|+
name|conf
operator|.
name|getRoutingTable
argument_list|()
operator|+
literal|" num Buckets: "
operator|+
name|conf
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|routeDataMovementEventToDestination
parameter_list|(
name|DataMovementEvent
name|event
parameter_list|,
name|int
name|sourceTaskIndex
parameter_list|,
name|int
name|sourceOutputIndex
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|mapDestTaskIndices
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|get
argument_list|(
name|sourceOutputIndex
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// No task for given input, return empty list with -1 as index
name|mapDestTaskIndices
operator|.
name|put
argument_list|(
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Normal case.
name|List
argument_list|<
name|Integer
argument_list|>
name|outputIndices
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|sourceTaskIndex
argument_list|)
decl_stmt|;
for|for
control|(
name|Integer
name|destIndex
range|:
name|conf
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|get
argument_list|(
name|sourceOutputIndex
argument_list|)
control|)
block|{
name|mapDestTaskIndices
operator|.
name|put
argument_list|(
name|destIndex
argument_list|,
name|outputIndices
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|routeInputSourceTaskFailedEventToDestination
parameter_list|(
name|int
name|sourceTaskIndex
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|mapDestTaskIndices
parameter_list|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|outputIndices
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|sourceTaskIndex
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|context
operator|.
name|getDestinationVertexNumTasks
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|mapDestTaskIndices
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|outputIndices
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|routeInputErrorEventToSource
parameter_list|(
name|InputReadErrorEvent
name|event
parameter_list|,
name|int
name|destinationTaskIndex
parameter_list|,
name|int
name|destinationFailedInputIndex
parameter_list|)
block|{
return|return
name|event
operator|.
name|getIndex
argument_list|()
return|;
block|}
block|}
end_class

end_unit

