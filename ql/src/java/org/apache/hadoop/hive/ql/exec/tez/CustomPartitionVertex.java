begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|fs
operator|.
name|Path
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
name|ql
operator|.
name|io
operator|.
name|HiveInputFormat
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
name|DataOutputBuffer
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
name|serializer
operator|.
name|SerializationFactory
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
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
name|mapred
operator|.
name|split
operator|.
name|TezGroupedSplitsInputFormat
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
name|mapred
operator|.
name|split
operator|.
name|TezMapredSplitsGrouper
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
name|EdgeManagerDescriptor
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
name|EdgeProperty
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
name|EdgeProperty
operator|.
name|DataMovementType
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
name|VertexLocationHint
operator|.
name|TaskLocationHint
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
name|TezConfiguration
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
name|VertexLocationHint
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
name|VertexManagerPlugin
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
name|VertexManagerPluginContext
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
name|mapreduce
operator|.
name|hadoop
operator|.
name|MRHelpers
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
name|mapreduce
operator|.
name|protos
operator|.
name|MRRuntimeProtos
operator|.
name|MRInputUserPayloadProto
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
name|mapreduce
operator|.
name|protos
operator|.
name|MRRuntimeProtos
operator|.
name|MRSplitProto
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
name|Event
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
name|RootInputConfigureVertexTasksEvent
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
name|RootInputDataInformationEvent
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
name|RootInputUpdatePayloadEvent
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
name|VertexManagerEvent
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
name|Function
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
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
name|HashMultimap
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
name|Iterables
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
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|Multimap
import|;
end_import

begin_comment
comment|/*  * Only works with old mapred API  * Will only work with a single MRInput for now.  */
end_comment

begin_class
specifier|public
class|class
name|CustomPartitionVertex
implements|implements
name|VertexManagerPlugin
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
name|CustomPartitionVertex
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|VertexManagerPluginContext
name|context
decl_stmt|;
specifier|private
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|bucketToTaskMap
init|=
name|HashMultimap
operator|.
expr|<
name|Integer
decl_stmt|,
name|Integer
decl|>
name|create
argument_list|()
decl_stmt|;
specifier|private
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|bucketToInitialSplitMap
init|=
name|ArrayListMultimap
operator|.
expr|<
name|Integer
decl_stmt|,
name|InputSplit
decl|>
name|create
argument_list|()
decl_stmt|;
specifier|private
name|RootInputConfigureVertexTasksEvent
name|configureVertexTaskEvent
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RootInputDataInformationEvent
argument_list|>
name|dataInformationEvents
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|FileSplit
argument_list|>
argument_list|>
name|pathFileSplitsMap
init|=
operator|new
name|TreeMap
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|FileSplit
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|rootVertexInitialized
init|=
literal|false
decl_stmt|;
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|bucketToGroupedSplitMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|bucketToNumTaskMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|CustomPartitionVertex
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|VertexManagerPluginContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|ByteBuffer
name|byteBuf
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|context
operator|.
name|getUserPayload
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|byteBuf
operator|.
name|getInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onVertexStarted
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|completions
parameter_list|)
block|{
name|int
name|numTasks
init|=
name|context
operator|.
name|getVertexNumTasks
argument_list|(
name|context
operator|.
name|getVertexName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|scheduledTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|numTasks
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
name|numTasks
condition|;
operator|++
name|i
control|)
block|{
name|scheduledTasks
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|scheduleVertexTasks
argument_list|(
name|scheduledTasks
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onSourceTaskCompleted
parameter_list|(
name|String
name|srcVertexName
parameter_list|,
name|Integer
name|attemptId
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|onVertexManagerEventReceived
parameter_list|(
name|VertexManagerEvent
name|vmEvent
parameter_list|)
block|{   }
comment|// One call per root Input - and for now only one is handled.
annotation|@
name|Override
specifier|public
name|void
name|onRootVertexInitialized
parameter_list|(
name|String
name|inputName
parameter_list|,
name|InputDescriptor
name|inputDescriptor
parameter_list|,
name|List
argument_list|<
name|Event
argument_list|>
name|events
parameter_list|)
block|{
comment|// Ideally, since there's only 1 Input expected at the moment -
comment|// ensure this method is called only once. Tez will call it once per Root Input.
name|Preconditions
operator|.
name|checkState
argument_list|(
name|rootVertexInitialized
operator|==
literal|false
argument_list|)
expr_stmt|;
name|rootVertexInitialized
operator|=
literal|true
expr_stmt|;
try|try
block|{
comment|// This is using the payload from the RootVertexInitializer corresponding
comment|// to InputName. Ideally it should be using it's own configuration class - but that
comment|// means serializing another instance.
name|MRInputUserPayloadProto
name|protoPayload
init|=
name|MRHelpers
operator|.
name|parseMRInputPayload
argument_list|(
name|inputDescriptor
operator|.
name|getUserPayload
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|MRHelpers
operator|.
name|createConfFromByteString
argument_list|(
name|protoPayload
operator|.
name|getConfigurationBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|/*        * Currently in tez, the flow of events is thus: "Generate Splits -> Initialize Vertex"        * (with parallelism info obtained from the generate splits phase). The generate splits        * phase groups splits using the TezGroupedSplitsInputFormat. However, for bucket map joins        * the grouping done by this input format results in incorrect results as the grouper has no        * knowledge of buckets. So, we initially set the input format to be HiveInputFormat        * (in DagUtils) for the case of bucket map joins so as to obtain un-grouped splits.        * We then group the splits corresponding to buckets using the tez grouper which returns        * TezGroupedSplits.        */
comment|// This assumes that Grouping will always be used.
comment|// Changing the InputFormat - so that the correct one is initialized in MRInput.
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.input.format.class"
argument_list|,
name|TezGroupedSplitsInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|MRInputUserPayloadProto
name|updatedPayload
init|=
name|MRInputUserPayloadProto
operator|.
name|newBuilder
argument_list|(
name|protoPayload
argument_list|)
operator|.
name|setConfigurationBytes
argument_list|(
name|MRHelpers
operator|.
name|createByteStringFromConf
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|inputDescriptor
operator|.
name|setUserPayload
argument_list|(
name|updatedPayload
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|boolean
name|dataInformationEventSeen
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Event
name|event
range|:
name|events
control|)
block|{
if|if
condition|(
name|event
operator|instanceof
name|RootInputConfigureVertexTasksEvent
condition|)
block|{
comment|// No tasks should have been started yet. Checked by initial state check.
name|Preconditions
operator|.
name|checkState
argument_list|(
name|dataInformationEventSeen
operator|==
literal|false
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|context
operator|.
name|getVertexNumTasks
argument_list|(
name|context
operator|.
name|getVertexName
argument_list|()
argument_list|)
operator|==
operator|-
literal|1
argument_list|,
literal|"Parallelism for the vertex should be set to -1 if the InputInitializer is setting parallelism"
argument_list|)
expr_stmt|;
name|RootInputConfigureVertexTasksEvent
name|cEvent
init|=
operator|(
name|RootInputConfigureVertexTasksEvent
operator|)
name|event
decl_stmt|;
comment|// The vertex cannot be configured until all DataEvents are seen - to build the routing table.
name|configureVertexTaskEvent
operator|=
name|cEvent
expr_stmt|;
name|dataInformationEvents
operator|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|configureVertexTaskEvent
operator|.
name|getNumTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|event
operator|instanceof
name|RootInputUpdatePayloadEvent
condition|)
block|{
comment|// this event can never occur. If it does, fail.
name|Preconditions
operator|.
name|checkState
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|instanceof
name|RootInputDataInformationEvent
condition|)
block|{
name|dataInformationEventSeen
operator|=
literal|true
expr_stmt|;
name|RootInputDataInformationEvent
name|diEvent
init|=
operator|(
name|RootInputDataInformationEvent
operator|)
name|event
decl_stmt|;
name|dataInformationEvents
operator|.
name|add
argument_list|(
name|diEvent
argument_list|)
expr_stmt|;
name|FileSplit
name|fileSplit
decl_stmt|;
try|try
block|{
name|fileSplit
operator|=
name|getFileSplitFromEvent
argument_list|(
name|diEvent
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
literal|"Failed to get file split for event: "
operator|+
name|diEvent
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|FileSplit
argument_list|>
name|fsList
init|=
name|pathFileSplitsMap
operator|.
name|get
argument_list|(
name|fileSplit
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fsList
operator|==
literal|null
condition|)
block|{
name|fsList
operator|=
operator|new
name|ArrayList
argument_list|<
name|FileSplit
argument_list|>
argument_list|()
expr_stmt|;
name|pathFileSplitsMap
operator|.
name|put
argument_list|(
name|fileSplit
operator|.
name|getPath
argument_list|()
argument_list|,
name|fsList
argument_list|)
expr_stmt|;
block|}
name|fsList
operator|.
name|add
argument_list|(
name|fileSplit
argument_list|)
expr_stmt|;
block|}
block|}
name|setBucketNumForPath
argument_list|(
name|pathFileSplitsMap
argument_list|)
expr_stmt|;
try|try
block|{
name|groupSplits
argument_list|()
expr_stmt|;
name|processAllEvents
argument_list|(
name|inputName
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
block|}
specifier|private
name|void
name|processAllEvents
parameter_list|(
name|String
name|inputName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|InputSplit
argument_list|>
name|finalSplits
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|int
name|taskCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Collection
argument_list|<
name|InputSplit
argument_list|>
argument_list|>
name|entry
range|:
name|bucketToGroupedSplitMap
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|bucketNum
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|InputSplit
argument_list|>
name|initialSplits
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|finalSplits
operator|.
name|addAll
argument_list|(
name|initialSplits
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|initialSplits
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|bucketToTaskMap
operator|.
name|put
argument_list|(
name|bucketNum
argument_list|,
name|taskCount
argument_list|)
expr_stmt|;
name|taskCount
operator|++
expr_stmt|;
block|}
block|}
comment|// Construct the EdgeManager descriptor to be used by all edges which need the routing table.
name|EdgeManagerDescriptor
name|hiveEdgeManagerDesc
init|=
operator|new
name|EdgeManagerDescriptor
argument_list|(
name|CustomPartitionEdge
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|payload
init|=
name|getBytePayload
argument_list|(
name|bucketToTaskMap
argument_list|)
decl_stmt|;
name|hiveEdgeManagerDesc
operator|.
name|setUserPayload
argument_list|(
name|payload
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|EdgeManagerDescriptor
argument_list|>
name|emMap
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
comment|// Replace the edge manager for all vertices which have routing type custom.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|EdgeProperty
argument_list|>
name|edgeEntry
range|:
name|context
operator|.
name|getInputVertexEdgeProperties
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|edgeEntry
operator|.
name|getValue
argument_list|()
operator|.
name|getDataMovementType
argument_list|()
operator|==
name|DataMovementType
operator|.
name|CUSTOM
operator|&&
name|edgeEntry
operator|.
name|getValue
argument_list|()
operator|.
name|getEdgeManagerDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
operator|.
name|equals
argument_list|(
name|CustomPartitionEdge
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|emMap
operator|.
name|put
argument_list|(
name|edgeEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|hiveEdgeManagerDesc
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Task count is "
operator|+
name|taskCount
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RootInputDataInformationEvent
argument_list|>
name|taskEvents
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|finalSplits
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// Re-serialize the splits after grouping.
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InputSplit
name|inputSplit
range|:
name|finalSplits
control|)
block|{
name|MRSplitProto
name|serializedSplit
init|=
name|MRHelpers
operator|.
name|createSplitProto
argument_list|(
name|inputSplit
argument_list|)
decl_stmt|;
name|RootInputDataInformationEvent
name|diEvent
init|=
operator|new
name|RootInputDataInformationEvent
argument_list|(
name|count
argument_list|,
name|serializedSplit
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|diEvent
operator|.
name|setTargetIndex
argument_list|(
name|count
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
name|taskEvents
operator|.
name|add
argument_list|(
name|diEvent
argument_list|)
expr_stmt|;
block|}
comment|// Replace the Edge Managers
name|context
operator|.
name|setVertexParallelism
argument_list|(
name|taskCount
argument_list|,
operator|new
name|VertexLocationHint
argument_list|(
name|createTaskLocationHintsFromSplits
argument_list|(
name|finalSplits
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
name|finalSplits
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|emMap
argument_list|)
expr_stmt|;
comment|// Set the actual events for the tasks.
name|context
operator|.
name|addRootInputEvents
argument_list|(
name|inputName
argument_list|,
name|taskEvents
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|getBytePayload
parameter_list|(
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|routingTable
parameter_list|)
throws|throws
name|IOException
block|{
name|CustomEdgeConfiguration
name|edgeConf
init|=
operator|new
name|CustomEdgeConfiguration
argument_list|(
name|routingTable
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|routingTable
argument_list|)
decl_stmt|;
name|DataOutputBuffer
name|dob
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|edgeConf
operator|.
name|write
argument_list|(
name|dob
argument_list|)
expr_stmt|;
name|byte
index|[]
name|serialized
init|=
name|dob
operator|.
name|getData
argument_list|()
decl_stmt|;
return|return
name|serialized
return|;
block|}
specifier|private
name|FileSplit
name|getFileSplitFromEvent
parameter_list|(
name|RootInputDataInformationEvent
name|event
parameter_list|)
throws|throws
name|IOException
block|{
name|InputSplit
name|inputSplit
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|event
operator|.
name|getDeserializedUserPayload
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|inputSplit
operator|=
operator|(
name|InputSplit
operator|)
name|event
operator|.
name|getDeserializedUserPayload
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|MRSplitProto
name|splitProto
init|=
name|MRSplitProto
operator|.
name|parseFrom
argument_list|(
name|event
operator|.
name|getUserPayload
argument_list|()
argument_list|)
decl_stmt|;
name|SerializationFactory
name|serializationFactory
init|=
operator|new
name|SerializationFactory
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|inputSplit
operator|=
name|MRHelpers
operator|.
name|createOldFormatSplitFromUserPayload
argument_list|(
name|splitProto
argument_list|,
name|serializationFactory
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|inputSplit
operator|instanceof
name|FileSplit
operator|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Cannot handle splits other than FileSplit for the moment"
argument_list|)
throw|;
block|}
return|return
operator|(
name|FileSplit
operator|)
name|inputSplit
return|;
block|}
comment|/*    * This method generates the map of bucket to file splits.    */
specifier|private
name|void
name|setBucketNumForPath
parameter_list|(
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|FileSplit
argument_list|>
argument_list|>
name|pathFileSplitsMap
parameter_list|)
block|{
name|int
name|bucketNum
init|=
literal|0
decl_stmt|;
name|int
name|fsCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|FileSplit
argument_list|>
argument_list|>
name|entry
range|:
name|pathFileSplitsMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|bucketId
init|=
name|bucketNum
operator|%
name|numBuckets
decl_stmt|;
for|for
control|(
name|FileSplit
name|fsplit
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|fsCount
operator|++
expr_stmt|;
name|bucketToInitialSplitMap
operator|.
name|put
argument_list|(
name|bucketId
argument_list|,
name|fsplit
argument_list|)
expr_stmt|;
block|}
name|bucketNum
operator|++
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Total number of splits counted: "
operator|+
name|fsCount
operator|+
literal|" and total files encountered: "
operator|+
name|pathFileSplitsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|groupSplits
parameter_list|()
throws|throws
name|IOException
block|{
name|estimateBucketSizes
argument_list|()
expr_stmt|;
name|bucketToGroupedSplitMap
operator|=
name|ArrayListMultimap
operator|.
expr|<
name|Integer
operator|,
name|InputSplit
operator|>
name|create
argument_list|(
name|bucketToInitialSplitMap
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Collection
argument_list|<
name|InputSplit
argument_list|>
argument_list|>
name|bucketSplitMap
init|=
name|bucketToInitialSplitMap
operator|.
name|asMap
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|bucketId
range|:
name|bucketSplitMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Collection
argument_list|<
name|InputSplit
argument_list|>
name|inputSplitCollection
init|=
name|bucketSplitMap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
decl_stmt|;
name|TezMapredSplitsGrouper
name|grouper
init|=
operator|new
name|TezMapredSplitsGrouper
argument_list|()
decl_stmt|;
name|InputSplit
index|[]
name|groupedSplits
init|=
name|grouper
operator|.
name|getGroupedSplits
argument_list|(
name|conf
argument_list|,
name|inputSplitCollection
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|bucketToNumTaskMap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
argument_list|,
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Original split size is "
operator|+
name|inputSplitCollection
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
literal|0
index|]
argument_list|)
operator|.
name|length
operator|+
literal|" grouped split size is "
operator|+
name|groupedSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|bucketToGroupedSplitMap
operator|.
name|removeAll
argument_list|(
name|bucketId
argument_list|)
expr_stmt|;
for|for
control|(
name|InputSplit
name|inSplit
range|:
name|groupedSplits
control|)
block|{
name|bucketToGroupedSplitMap
operator|.
name|put
argument_list|(
name|bucketId
argument_list|,
name|inSplit
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|estimateBucketSizes
parameter_list|()
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|bucketSizeMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Collection
argument_list|<
name|InputSplit
argument_list|>
argument_list|>
name|bucketSplitMap
init|=
name|bucketToInitialSplitMap
operator|.
name|asMap
argument_list|()
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|bucketId
range|:
name|bucketSplitMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Long
name|size
init|=
literal|0L
decl_stmt|;
name|Collection
argument_list|<
name|InputSplit
argument_list|>
name|inputSplitCollection
init|=
name|bucketSplitMap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|InputSplit
argument_list|>
name|iter
init|=
name|inputSplitCollection
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FileSplit
name|fsplit
init|=
operator|(
name|FileSplit
operator|)
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|size
operator|+=
name|fsplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|totalSize
operator|+=
name|fsplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|bucketSizeMap
operator|.
name|put
argument_list|(
name|bucketId
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|int
name|totalResource
init|=
name|context
operator|.
name|getTotalAVailableResource
argument_list|()
operator|.
name|getMemory
argument_list|()
decl_stmt|;
name|int
name|taskResource
init|=
name|context
operator|.
name|getVertexTaskResource
argument_list|()
operator|.
name|getMemory
argument_list|()
decl_stmt|;
name|float
name|waves
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_AM_GROUPING_SPLIT_WAVES
argument_list|,
name|TezConfiguration
operator|.
name|TEZ_AM_GROUPING_SPLIT_WAVES_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|numTasks
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|totalResource
operator|*
name|waves
operator|)
operator|/
name|taskResource
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Total resource: "
operator|+
name|totalResource
operator|+
literal|" Task Resource: "
operator|+
name|taskResource
operator|+
literal|" waves: "
operator|+
name|waves
operator|+
literal|" total size of splits: "
operator|+
name|totalSize
operator|+
literal|" total number of tasks: "
operator|+
name|numTasks
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|bucketId
range|:
name|bucketSizeMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|int
name|numEstimatedTasks
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|totalSize
operator|!=
literal|0
condition|)
block|{
name|numEstimatedTasks
operator|=
call|(
name|int
call|)
argument_list|(
name|numTasks
operator|*
name|bucketSizeMap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
operator|/
name|totalSize
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated number of tasks: "
operator|+
name|numEstimatedTasks
operator|+
literal|" for bucket "
operator|+
name|bucketId
argument_list|)
expr_stmt|;
if|if
condition|(
name|numEstimatedTasks
operator|==
literal|0
condition|)
block|{
name|numEstimatedTasks
operator|=
literal|1
expr_stmt|;
block|}
name|bucketToNumTaskMap
operator|.
name|put
argument_list|(
name|bucketId
argument_list|,
name|numEstimatedTasks
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|TaskLocationHint
argument_list|>
name|createTaskLocationHintsFromSplits
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
index|[]
name|oldFormatSplits
parameter_list|)
block|{
name|Iterable
argument_list|<
name|TaskLocationHint
argument_list|>
name|iterable
init|=
name|Iterables
operator|.
name|transform
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|oldFormatSplits
argument_list|)
argument_list|,
operator|new
name|Function
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
argument_list|,
name|TaskLocationHint
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TaskLocationHint
name|apply
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|input
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|input
operator|.
name|getLocations
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|TaskLocationHint
argument_list|(
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|input
operator|.
name|getLocations
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"NULL Location: returning an empty location hint"
argument_list|)
expr_stmt|;
return|return
operator|new
name|TaskLocationHint
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
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
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|iterable
argument_list|)
return|;
block|}
block|}
end_class

end_unit

