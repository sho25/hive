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
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|hdfs
operator|.
name|DFSConfigKeys
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
name|common
operator|.
name|JavaUtils
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
name|serde2
operator|.
name|SerDeException
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
name|mapred
operator|.
name|split
operator|.
name|SplitLocationProvider
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
name|event
operator|.
name|VertexStateUpdate
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
specifier|public
class|class
name|HiveSplitGenerator
extends|extends
name|InputInitializer
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
name|HiveSplitGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|DynamicPartitionPruner
name|pruner
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|MRInputUserPayloadProto
name|userPayloadProto
decl_stmt|;
specifier|private
specifier|final
name|MapWork
name|work
decl_stmt|;
specifier|private
specifier|final
name|SplitGrouper
name|splitGrouper
init|=
operator|new
name|SplitGrouper
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|SplitLocationProvider
name|splitLocationProvider
decl_stmt|;
specifier|public
name|HiveSplitGenerator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|MapWork
name|work
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Assuming grouping enabled always.
name|userPayloadProto
operator|=
name|MRInputUserPayloadProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setGroupingEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|splitLocationProvider
operator|=
name|Utils
operator|.
name|getSplitLocationProvider
argument_list|(
name|conf
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLocationProvider: "
operator|+
name|splitLocationProvider
argument_list|)
expr_stmt|;
comment|// Read all credentials into the credentials instance stored in JobConf.
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
comment|// Events can start coming in the moment the InputInitializer is created. The pruner
comment|// must be setup and initialized here so that it sets up it's structures to start accepting events.
comment|// Setting it up in initialize leads to a window where events may come in before the pruner is
comment|// initialized, which may cause it to drop events.
comment|// No dynamic partition pruning
name|pruner
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|HiveSplitGenerator
parameter_list|(
name|InputInitializerContext
name|initializerContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|super
argument_list|(
name|initializerContext
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|initializerContext
argument_list|)
expr_stmt|;
name|userPayloadProto
operator|=
name|MRInputHelpers
operator|.
name|parseMRInputPayload
argument_list|(
name|initializerContext
operator|.
name|getInputUserPayload
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|TezUtils
operator|.
name|createConfFromByteString
argument_list|(
name|userPayloadProto
operator|.
name|getConfigurationBytes
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|splitLocationProvider
operator|=
name|Utils
operator|.
name|getSplitLocationProvider
argument_list|(
name|conf
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"SplitLocationProvider: "
operator|+
name|splitLocationProvider
argument_list|)
expr_stmt|;
comment|// Read all credentials into the credentials instance stored in JobConf.
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
name|this
operator|.
name|work
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
comment|// Events can start coming in the moment the InputInitializer is created. The pruner
comment|// must be setup and initialized here so that it sets up it's structures to start accepting events.
comment|// Setting it up in initialize leads to a window where events may come in before the pruner is
comment|// initialized, which may cause it to drop events.
name|pruner
operator|=
operator|new
name|DynamicPartitionPruner
argument_list|(
name|initializerContext
argument_list|,
name|work
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
comment|// Setup the map work for this thread. Pruning modified the work instance to potentially remove
comment|// partitions. The same work instance must be used when generating splits.
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jobConf
argument_list|,
name|work
argument_list|)
expr_stmt|;
try|try
block|{
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
comment|// perform dynamic partition pruning
if|if
condition|(
name|pruner
operator|!=
literal|null
condition|)
block|{
name|pruner
operator|.
name|prune
argument_list|()
expr_stmt|;
block|}
name|InputSplitInfoMem
name|inputSplitInfo
init|=
literal|null
decl_stmt|;
name|boolean
name|generateConsistentSplits
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TEZ_GENERATE_CONSISTENT_SPLITS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"GenerateConsistentSplitsInHive="
operator|+
name|generateConsistentSplits
argument_list|)
expr_stmt|;
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
name|JavaUtils
operator|.
name|loadClass
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
literal|0
decl_stmt|;
name|int
name|taskResource
init|=
literal|0
decl_stmt|;
name|int
name|availableSlots
init|=
literal|0
decl_stmt|;
comment|// FIXME. Do the right thing Luke.
if|if
condition|(
name|getContext
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// for now, totalResource = taskResource for llap
name|availableSlots
operator|=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|getContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|totalResource
operator|=
name|getContext
argument_list|()
operator|.
name|getTotalAvailableResource
argument_list|()
operator|.
name|getMemory
argument_list|()
expr_stmt|;
name|taskResource
operator|=
name|getContext
argument_list|()
operator|.
name|getVertexTaskResource
argument_list|()
operator|.
name|getMemory
argument_list|()
expr_stmt|;
name|availableSlots
operator|=
name|totalResource
operator|/
name|taskResource
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZE
argument_list|,
literal|1
argument_list|)
operator|<=
literal|1
condition|)
block|{
comment|// broken configuration from mapred-default.xml
specifier|final
name|long
name|blockSize
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_BLOCK_SIZE_KEY
argument_list|,
name|DFSConfigKeys
operator|.
name|DFS_BLOCK_SIZE_DEFAULT
argument_list|)
decl_stmt|;
specifier|final
name|long
name|minGrouping
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|TezMapReduceSplitsGrouper
operator|.
name|TEZ_GROUPING_SPLIT_MIN_SIZE
argument_list|,
name|TezMapReduceSplitsGrouper
operator|.
name|TEZ_GROUPING_SPLIT_MIN_SIZE_DEFAULT
argument_list|)
decl_stmt|;
specifier|final
name|long
name|preferredSplitSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|blockSize
operator|/
literal|2
argument_list|,
name|minGrouping
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZE
argument_list|,
name|preferredSplitSize
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The preferred split size is "
operator|+
name|preferredSplitSize
argument_list|)
expr_stmt|;
block|}
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
comment|// Raw splits
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
comment|// Sort the splits, so that subsequent grouping is consistent.
name|Arrays
operator|.
name|sort
argument_list|(
name|splits
argument_list|,
operator|new
name|InputSplitComparator
argument_list|()
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|work
operator|.
name|getIncludedBuckets
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|splits
operator|=
name|pruneBuckets
argument_list|(
name|work
argument_list|,
name|splits
argument_list|)
expr_stmt|;
block|}
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|groupedSplits
init|=
name|splitGrouper
operator|.
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
argument_list|,
name|splitLocationProvider
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
literal|"Number of split groups: "
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
name|splitGrouper
operator|.
name|createTaskLocationHints
argument_list|(
name|flatSplits
argument_list|,
name|generateConsistentSplits
argument_list|)
decl_stmt|;
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
finally|finally
block|{
name|Utilities
operator|.
name|clearWork
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|InputSplit
index|[]
name|pruneBuckets
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|InputSplit
index|[]
name|splits
parameter_list|)
block|{
specifier|final
name|BitSet
name|buckets
init|=
name|work
operator|.
name|getIncludedBuckets
argument_list|()
decl_stmt|;
specifier|final
name|String
name|bucketIn
init|=
name|buckets
operator|.
name|toString
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|filteredSplits
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|(
name|splits
operator|.
name|length
operator|/
literal|2
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|splits
control|)
block|{
specifier|final
name|int
name|bucket
init|=
name|Utilities
operator|.
name|parseSplitBucket
argument_list|(
name|split
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucket
operator|<
literal|0
operator|||
name|buckets
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
condition|)
block|{
comment|// match or UNKNOWN
name|filteredSplits
operator|.
name|add
argument_list|(
name|split
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Pruning with IN ({}) - removing {}"
argument_list|,
name|bucketIn
argument_list|,
name|split
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|filteredSplits
operator|.
name|size
argument_list|()
operator|<
name|splits
operator|.
name|length
condition|)
block|{
comment|// reallocate only if any filters pruned
name|splits
operator|=
name|filteredSplits
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
name|filteredSplits
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|splits
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
name|onVertexStateUpdated
parameter_list|(
name|VertexStateUpdate
name|stateUpdate
parameter_list|)
block|{
name|pruner
operator|.
name|processVertex
argument_list|(
name|stateUpdate
operator|.
name|getVertexName
argument_list|()
argument_list|)
expr_stmt|;
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
name|addEvent
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Descending sort based on split size| Followed by file name. Followed by startPosition.
specifier|static
class|class
name|InputSplitComparator
implements|implements
name|Comparator
argument_list|<
name|InputSplit
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|InputSplit
name|o1
parameter_list|,
name|InputSplit
name|o2
parameter_list|)
block|{
try|try
block|{
name|long
name|len1
init|=
name|o1
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|long
name|len2
init|=
name|o2
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|len1
operator|<
name|len2
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|len1
operator|==
name|len2
condition|)
block|{
comment|// If the same size. Sort on file name followed by startPosition.
if|if
condition|(
name|o1
operator|instanceof
name|FileSplit
operator|&&
name|o2
operator|instanceof
name|FileSplit
condition|)
block|{
name|FileSplit
name|fs1
init|=
operator|(
name|FileSplit
operator|)
name|o1
decl_stmt|;
name|FileSplit
name|fs2
init|=
operator|(
name|FileSplit
operator|)
name|o2
decl_stmt|;
if|if
condition|(
name|fs1
operator|.
name|getPath
argument_list|()
operator|!=
literal|null
operator|&&
name|fs2
operator|.
name|getPath
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|int
name|pathComp
init|=
operator|(
name|fs1
operator|.
name|getPath
argument_list|()
operator|.
name|compareTo
argument_list|(
name|fs2
operator|.
name|getPath
argument_list|()
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|pathComp
operator|==
literal|0
condition|)
block|{
comment|// Compare start Position
name|long
name|startPos1
init|=
name|fs1
operator|.
name|getStart
argument_list|()
decl_stmt|;
name|long
name|startPos2
init|=
name|fs2
operator|.
name|getStart
argument_list|()
decl_stmt|;
if|if
condition|(
name|startPos1
operator|>
name|startPos2
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|startPos1
operator|<
name|startPos2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
else|else
block|{
return|return
name|pathComp
return|;
block|}
block|}
block|}
comment|// No further checks if not a file split. Return equality.
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1
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
literal|"Problem getting input split size"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

