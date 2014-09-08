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
name|exec
operator|.
name|Utilities
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
name|HiveFileFormatUtils
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
name|plan
operator|.
name|MapWork
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
name|plan
operator|.
name|PartitionDesc
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
name|shims
operator|.
name|ShimLoader
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
name|InputFormat
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
name|JobConf
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
name|mapreduce
operator|.
name|split
operator|.
name|TezMapReduceSplitsGrouper
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
name|util
operator|.
name|ReflectionUtils
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
name|TezUtils
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
name|mapreduce
operator|.
name|hadoop
operator|.
name|InputSplitInfoMem
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
name|MRInputHelpers
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
name|mapreduce
operator|.
name|protos
operator|.
name|MRRuntimeProtos
operator|.
name|MRSplitsProto
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
name|InputInitializer
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
name|InputInitializerContext
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
name|InputSpecUpdate
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
name|InputConfigureVertexTasksEvent
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
name|InputDataInformationEvent
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
name|InputInitializerEvent
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
name|Multimap
import|;
end_import

begin_comment
comment|/**  * This class is used to generate splits inside the AM on the cluster. It  * optionally groups together splits based on available head room as well as  * making sure that splits from different partitions are only grouped if they  * are of the same schema, format and serde  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|HiveSplitGenerator
extends|extends
name|InputInitializer
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
name|HiveSplitGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SplitGrouper
name|grouper
init|=
operator|new
name|SplitGrouper
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DynamicPartitionPruner
name|pruner
init|=
operator|new
name|DynamicPartitionPruner
argument_list|()
decl_stmt|;
specifier|private
name|InputInitializerContext
name|context
decl_stmt|;
specifier|public
name|HiveSplitGenerator
parameter_list|(
name|InputInitializerContext
name|initializerContext
parameter_list|)
block|{
name|super
argument_list|(
name|initializerContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Event
argument_list|>
name|initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|InputInitializerContext
name|rootInputContext
init|=
name|getContext
argument_list|()
decl_stmt|;
name|context
operator|=
name|rootInputContext
expr_stmt|;
name|MRInputUserPayloadProto
name|userPayloadProto
init|=
name|MRInputHelpers
operator|.
name|parseMRInputPayload
argument_list|(
name|rootInputContext
operator|.
name|getInputUserPayload
argument_list|()
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|TezUtils
operator|.
name|createConfFromByteString
argument_list|(
name|userPayloadProto
operator|.
name|getConfigurationBytes
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|sendSerializedEvents
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"mapreduce.tez.input.initializer.serialize.event.payload"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Read all credentials into the credentials instance stored in JobConf.
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getMergedCredentials
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|MapWork
name|work
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
comment|// perform dynamic partition pruning
name|pruner
operator|.
name|prune
argument_list|(
name|work
argument_list|,
name|jobConf
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|InputSplitInfoMem
name|inputSplitInfo
init|=
literal|null
decl_stmt|;
name|String
name|realInputFormatName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"mapred.input.format.class"
argument_list|)
decl_stmt|;
name|boolean
name|groupingEnabled
init|=
name|userPayloadProto
operator|.
name|getGroupingEnabled
argument_list|()
decl_stmt|;
if|if
condition|(
name|groupingEnabled
condition|)
block|{
comment|// Need to instantiate the realInputFormat
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|inputFormat
init|=
operator|(
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|realInputFormatName
argument_list|)
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
name|int
name|totalResource
init|=
name|rootInputContext
operator|.
name|getTotalAvailableResource
argument_list|()
operator|.
name|getMemory
argument_list|()
decl_stmt|;
name|int
name|taskResource
init|=
name|rootInputContext
operator|.
name|getVertexTaskResource
argument_list|()
operator|.
name|getMemory
argument_list|()
decl_stmt|;
name|int
name|availableSlots
init|=
name|totalResource
operator|/
name|taskResource
decl_stmt|;
comment|// Create the un-grouped splits
name|float
name|waves
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|TezMapReduceSplitsGrouper
operator|.
name|TEZ_GROUPING_SPLIT_WAVES
argument_list|,
name|TezMapReduceSplitsGrouper
operator|.
name|TEZ_GROUPING_SPLIT_WAVES_DEFAULT
argument_list|)
decl_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|jobConf
argument_list|,
call|(
name|int
call|)
argument_list|(
name|availableSlots
operator|*
name|waves
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of input splits: "
operator|+
name|splits
operator|.
name|length
operator|+
literal|". "
operator|+
name|availableSlots
operator|+
literal|" available slots, "
operator|+
name|waves
operator|+
literal|" waves. Input format is: "
operator|+
name|realInputFormatName
argument_list|)
expr_stmt|;
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|groupedSplits
init|=
name|generateGroupedSplits
argument_list|(
name|jobConf
argument_list|,
name|conf
argument_list|,
name|splits
argument_list|,
name|waves
argument_list|,
name|availableSlots
argument_list|)
decl_stmt|;
comment|// And finally return them in a flat array
name|InputSplit
index|[]
name|flatSplits
init|=
name|groupedSplits
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of grouped splits: "
operator|+
name|flatSplits
operator|.
name|length
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TaskLocationHint
argument_list|>
name|locationHints
init|=
name|grouper
operator|.
name|createTaskLocationHints
argument_list|(
name|flatSplits
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|clearWork
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|inputSplitInfo
operator|=
operator|new
name|InputSplitInfoMem
argument_list|(
name|flatSplits
argument_list|,
name|locationHints
argument_list|,
name|flatSplits
operator|.
name|length
argument_list|,
literal|null
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// no need for grouping and the target #of tasks.
comment|// This code path should never be triggered at the moment. If grouping is disabled,
comment|// DAGUtils uses MRInputAMSplitGenerator.
comment|// If this is used in the future - make sure to disable grouping in the payload, if it isn't already disabled
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HiveInputFormat does not support non-grouped splits, InputFormatName is: "
operator|+
name|realInputFormatName
argument_list|)
throw|;
comment|// inputSplitInfo = MRInputHelpers.generateInputSplitsToMem(jobConf, false, 0);
block|}
return|return
name|createEventList
argument_list|(
name|sendSerializedEvents
argument_list|,
name|inputSplitInfo
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|generateGroupedSplits
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|InputSplit
index|[]
name|splits
parameter_list|,
name|float
name|waves
parameter_list|,
name|int
name|availableSlots
parameter_list|)
throws|throws
name|Exception
block|{
name|MapWork
name|work
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|bucketSplitMultiMap
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
name|Class
argument_list|<
name|?
argument_list|>
name|previousInputFormatClass
init|=
literal|null
decl_stmt|;
name|String
name|previousDeserializerClass
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InputSplit
name|s
range|:
name|splits
control|)
block|{
comment|// this is the bit where we make sure we don't group across partition
comment|// schema boundaries
name|Path
name|path
init|=
operator|(
operator|(
name|FileSplit
operator|)
name|s
operator|)
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|PartitionDesc
name|pd
init|=
name|HiveFileFormatUtils
operator|.
name|getPartitionDescFromPathRecursively
argument_list|(
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
argument_list|,
name|path
argument_list|,
name|cache
argument_list|)
decl_stmt|;
name|String
name|currentDeserializerClass
init|=
name|pd
operator|.
name|getDeserializerClassName
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|currentInputFormatClass
init|=
name|pd
operator|.
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|currentInputFormatClass
operator|!=
name|previousInputFormatClass
operator|)
operator|||
operator|(
operator|!
name|currentDeserializerClass
operator|.
name|equals
argument_list|(
name|previousDeserializerClass
argument_list|)
operator|)
condition|)
block|{
operator|++
name|i
expr_stmt|;
block|}
name|previousInputFormatClass
operator|=
name|currentInputFormatClass
expr_stmt|;
name|previousDeserializerClass
operator|=
name|currentDeserializerClass
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding split "
operator|+
name|path
operator|+
literal|" to src group "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|bucketSplitMultiMap
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"# Src groups for split generation: "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
comment|// group them into the chunks we want
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|groupedSplits
init|=
name|grouper
operator|.
name|group
argument_list|(
name|jobConf
argument_list|,
name|bucketSplitMultiMap
argument_list|,
name|availableSlots
argument_list|,
name|waves
argument_list|)
decl_stmt|;
return|return
name|groupedSplits
return|;
block|}
specifier|private
name|List
argument_list|<
name|Event
argument_list|>
name|createEventList
parameter_list|(
name|boolean
name|sendSerializedEvents
parameter_list|,
name|InputSplitInfoMem
name|inputSplitInfo
parameter_list|)
block|{
name|List
argument_list|<
name|Event
argument_list|>
name|events
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|inputSplitInfo
operator|.
name|getNumTasks
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|InputConfigureVertexTasksEvent
name|configureVertexEvent
init|=
name|InputConfigureVertexTasksEvent
operator|.
name|create
argument_list|(
name|inputSplitInfo
operator|.
name|getNumTasks
argument_list|()
argument_list|,
name|VertexLocationHint
operator|.
name|create
argument_list|(
name|inputSplitInfo
operator|.
name|getTaskLocationHints
argument_list|()
argument_list|)
argument_list|,
name|InputSpecUpdate
operator|.
name|getDefaultSinglePhysicalInputSpecUpdate
argument_list|()
argument_list|)
decl_stmt|;
name|events
operator|.
name|add
argument_list|(
name|configureVertexEvent
argument_list|)
expr_stmt|;
if|if
condition|(
name|sendSerializedEvents
condition|)
block|{
name|MRSplitsProto
name|splitsProto
init|=
name|inputSplitInfo
operator|.
name|getSplitsProto
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MRSplitProto
name|mrSplit
range|:
name|splitsProto
operator|.
name|getSplitsList
argument_list|()
control|)
block|{
name|InputDataInformationEvent
name|diEvent
init|=
name|InputDataInformationEvent
operator|.
name|createWithSerializedPayload
argument_list|(
name|count
operator|++
argument_list|,
name|mrSplit
operator|.
name|toByteString
argument_list|()
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|events
operator|.
name|add
argument_list|(
name|diEvent
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|split
range|:
name|inputSplitInfo
operator|.
name|getOldFormatSplits
argument_list|()
control|)
block|{
name|InputDataInformationEvent
name|diEvent
init|=
name|InputDataInformationEvent
operator|.
name|createWithObjectPayload
argument_list|(
name|count
operator|++
argument_list|,
name|split
argument_list|)
decl_stmt|;
name|events
operator|.
name|add
argument_list|(
name|diEvent
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|events
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleInputInitializerEvent
parameter_list|(
name|List
argument_list|<
name|InputInitializerEvent
argument_list|>
name|events
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|InputInitializerEvent
name|e
range|:
name|events
control|)
block|{
name|pruner
operator|.
name|getQueue
argument_list|()
operator|.
name|put
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

