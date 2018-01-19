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
name|Collections
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
name|LinkedHashSet
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
name|Objects
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
name|ConcurrentHashMap
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
name|mapred
operator|.
name|split
operator|.
name|TezGroupedSplit
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
name|TaskLocationHint
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
comment|/**  * SplitGrouper is used to combine splits based on head room and locality. It  * also enforces restrictions around schema, file format and bucketing.  */
end_comment

begin_class
specifier|public
class|class
name|SplitGrouper
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
name|SplitGrouper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// TODO This needs to be looked at. Map of Map to Map... Made concurrent for now since split generation
comment|// can happen in parallel.
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|,
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|TezMapredSplitsGrouper
name|tezGrouper
init|=
operator|new
name|TezMapredSplitsGrouper
argument_list|()
decl_stmt|;
comment|/**    * group splits for each bucket separately - while evenly filling all the    * available slots with tasks    */
specifier|public
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|group
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|bucketSplitMultimap
parameter_list|,
name|int
name|availableSlots
parameter_list|,
name|float
name|waves
parameter_list|,
name|SplitLocationProvider
name|splitLocationProvider
parameter_list|)
throws|throws
name|IOException
block|{
comment|// figure out how many tasks we want for each bucket
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|bucketTaskMap
init|=
name|estimateBucketSizes
argument_list|(
name|availableSlots
argument_list|,
name|waves
argument_list|,
name|bucketSplitMultimap
operator|.
name|asMap
argument_list|()
argument_list|)
decl_stmt|;
comment|// allocate map bucket id to grouped splits
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|InputSplit
argument_list|>
name|bucketGroupedSplitMultimap
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
comment|// use the tez grouper to combine splits once per bucket
for|for
control|(
name|int
name|bucketId
range|:
name|bucketSplitMultimap
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
name|bucketSplitMultimap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
decl_stmt|;
name|InputSplit
index|[]
name|rawSplits
init|=
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
decl_stmt|;
name|InputSplit
index|[]
name|groupedSplits
init|=
name|tezGrouper
operator|.
name|getGroupedSplits
argument_list|(
name|conf
argument_list|,
name|rawSplits
argument_list|,
name|bucketTaskMap
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
argument_list|,
operator|new
name|ColumnarSplitSizeEstimator
argument_list|()
argument_list|,
name|splitLocationProvider
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Original split count is "
operator|+
name|rawSplits
operator|.
name|length
operator|+
literal|" grouped split count is "
operator|+
name|groupedSplits
operator|.
name|length
operator|+
literal|", for bucket: "
operator|+
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
name|bucketGroupedSplitMultimap
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
return|return
name|bucketGroupedSplitMultimap
return|;
block|}
comment|/**    * Create task location hints from a set of input splits    * @param splits the actual splits    * @param consistentLocations whether to re-order locations for each split, if it's a file split    * @return taskLocationHints - 1 per input split specified    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|TaskLocationHint
argument_list|>
name|createTaskLocationHints
parameter_list|(
name|InputSplit
index|[]
name|splits
parameter_list|,
name|boolean
name|consistentLocations
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|TaskLocationHint
argument_list|>
name|locationHints
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|splits
operator|.
name|length
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
name|String
name|rack
init|=
operator|(
name|split
operator|instanceof
name|TezGroupedSplit
operator|)
condition|?
operator|(
operator|(
name|TezGroupedSplit
operator|)
name|split
operator|)
operator|.
name|getRack
argument_list|()
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|rack
operator|==
literal|null
condition|)
block|{
name|String
index|[]
name|locations
init|=
name|split
operator|.
name|getLocations
argument_list|()
decl_stmt|;
if|if
condition|(
name|locations
operator|!=
literal|null
operator|&&
name|locations
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// Worthwhile only if more than 1 split, consistentGroupingEnabled and is a FileSplit
if|if
condition|(
name|consistentLocations
operator|&&
name|locations
operator|.
name|length
operator|>
literal|1
operator|&&
name|split
operator|instanceof
name|FileSplit
condition|)
block|{
name|Arrays
operator|.
name|sort
argument_list|(
name|locations
argument_list|)
expr_stmt|;
name|FileSplit
name|fileSplit
init|=
operator|(
name|FileSplit
operator|)
name|split
decl_stmt|;
name|Path
name|path
init|=
name|fileSplit
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|long
name|startLocation
init|=
name|fileSplit
operator|.
name|getStart
argument_list|()
decl_stmt|;
name|int
name|hashCode
init|=
name|Objects
operator|.
name|hash
argument_list|(
name|path
argument_list|,
name|startLocation
argument_list|)
decl_stmt|;
name|int
name|startIndex
init|=
name|hashCode
operator|%
name|locations
operator|.
name|length
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
name|locationSet
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|(
name|locations
operator|.
name|length
argument_list|)
decl_stmt|;
comment|// Set up the locations starting from startIndex, and wrapping around the sorted array.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|index
init|=
operator|(
name|startIndex
operator|+
name|i
operator|)
operator|%
name|locations
operator|.
name|length
decl_stmt|;
name|locationSet
operator|.
name|add
argument_list|(
name|locations
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
name|locationHints
operator|.
name|add
argument_list|(
name|TaskLocationHint
operator|.
name|createTaskLocationHint
argument_list|(
name|locationSet
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|locationHints
operator|.
name|add
argument_list|(
name|TaskLocationHint
operator|.
name|createTaskLocationHint
argument_list|(
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|split
operator|.
name|getLocations
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|locationHints
operator|.
name|add
argument_list|(
name|TaskLocationHint
operator|.
name|createTaskLocationHint
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|locationHints
operator|.
name|add
argument_list|(
name|TaskLocationHint
operator|.
name|createTaskLocationHint
argument_list|(
literal|null
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|rack
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|locationHints
return|;
block|}
comment|/** Generate groups of splits, separated by schema evolution boundaries */
specifier|public
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
parameter_list|,
name|SplitLocationProvider
name|locationProvider
parameter_list|)
throws|throws
name|Exception
block|{
return|return
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
literal|null
argument_list|,
literal|true
argument_list|,
name|locationProvider
argument_list|)
return|;
block|}
comment|/** Generate groups of splits, separated by schema evolution boundaries */
specifier|public
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
parameter_list|,
name|String
name|inputName
parameter_list|,
name|boolean
name|groupAcrossFiles
parameter_list|,
name|SplitLocationProvider
name|locationProvider
parameter_list|)
throws|throws
name|Exception
block|{
name|MapWork
name|work
init|=
name|populateMapWork
argument_list|(
name|jobConf
argument_list|,
name|inputName
argument_list|)
decl_stmt|;
comment|// ArrayListMultimap is important here to retain the ordering for the splits.
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
name|int
name|i
init|=
literal|0
decl_stmt|;
name|InputSplit
name|prevSplit
init|=
literal|null
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
if|if
condition|(
name|schemaEvolved
argument_list|(
name|s
argument_list|,
name|prevSplit
argument_list|,
name|groupAcrossFiles
argument_list|,
name|work
argument_list|)
condition|)
block|{
operator|++
name|i
expr_stmt|;
name|prevSplit
operator|=
name|s
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
name|this
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
argument_list|,
name|locationProvider
argument_list|)
decl_stmt|;
return|return
name|groupedSplits
return|;
block|}
comment|/**    * get the size estimates for each bucket in tasks. This is used to make sure    * we allocate the head room evenly    */
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|estimateBucketSizes
parameter_list|(
name|int
name|availableSlots
parameter_list|,
name|float
name|waves
parameter_list|,
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
parameter_list|)
block|{
comment|// mapping of bucket id to size of all splits in bucket in bytes
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
comment|// mapping of bucket id to number of required tasks to run
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|bucketTaskMap
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
comment|// TODO HIVE-12255. Make use of SplitSizeEstimator.
comment|// The actual task computation needs to be looked at as well.
comment|// compute the total size per bucket
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|boolean
name|earlyExit
init|=
literal|false
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
name|long
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InputSplit
name|s
range|:
name|bucketSplitMap
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
control|)
block|{
comment|// the incoming split may not be a file split when we are re-grouping TezGroupedSplits in
comment|// the case of SMB join. So in this case, we can do an early exit by not doing the
comment|// calculation for bucketSizeMap. Each bucket will assume it can fill availableSlots * waves
comment|// (preset to 0.5) for SMB join.
if|if
condition|(
operator|!
operator|(
name|s
operator|instanceof
name|FileSplit
operator|)
condition|)
block|{
name|bucketTaskMap
operator|.
name|put
argument_list|(
name|bucketId
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
expr_stmt|;
name|earlyExit
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
name|FileSplit
name|fsplit
init|=
operator|(
name|FileSplit
operator|)
name|s
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
if|if
condition|(
name|earlyExit
condition|)
block|{
return|return
name|bucketTaskMap
return|;
block|}
comment|// compute the number of tasks
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
comment|// availableSlots * waves => desired slots to fill
comment|// sizePerBucket/totalSize => weight for particular bucket. weights add
comment|// up to 1.
name|numEstimatedTasks
operator|=
call|(
name|int
call|)
argument_list|(
name|availableSlots
operator|*
name|waves
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
name|bucketTaskMap
operator|.
name|put
argument_list|(
name|bucketId
argument_list|,
name|numEstimatedTasks
argument_list|)
expr_stmt|;
block|}
return|return
name|bucketTaskMap
return|;
block|}
specifier|private
specifier|static
name|MapWork
name|populateMapWork
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|String
name|inputName
parameter_list|)
block|{
name|MapWork
name|work
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|inputName
operator|!=
literal|null
condition|)
block|{
name|work
operator|=
operator|(
name|MapWork
operator|)
name|Utilities
operator|.
name|getMergeWork
argument_list|(
name|jobConf
argument_list|,
name|inputName
argument_list|)
expr_stmt|;
comment|// work can still be null if there is no merge work for this input
block|}
if|if
condition|(
name|work
operator|==
literal|null
condition|)
block|{
name|work
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
return|return
name|work
return|;
block|}
specifier|private
name|boolean
name|schemaEvolved
parameter_list|(
name|InputSplit
name|s
parameter_list|,
name|InputSplit
name|prevSplit
parameter_list|,
name|boolean
name|groupAcrossFiles
parameter_list|,
name|MapWork
name|work
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|retval
init|=
literal|false
decl_stmt|;
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
name|getFromPathRecursively
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
if|if
condition|(
name|prevSplit
operator|!=
literal|null
condition|)
block|{
name|Path
name|prevPath
init|=
operator|(
operator|(
name|FileSplit
operator|)
name|prevSplit
operator|)
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|groupAcrossFiles
condition|)
block|{
return|return
operator|!
name|path
operator|.
name|equals
argument_list|(
name|prevPath
argument_list|)
return|;
block|}
name|PartitionDesc
name|prevPD
init|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
argument_list|,
name|prevPath
argument_list|,
name|cache
argument_list|)
decl_stmt|;
name|previousDeserializerClass
operator|=
name|prevPD
operator|.
name|getDeserializerClassName
argument_list|()
expr_stmt|;
name|previousInputFormatClass
operator|=
name|prevPD
operator|.
name|getInputFileFormatClass
argument_list|()
expr_stmt|;
block|}
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
name|retval
operator|=
literal|true
expr_stmt|;
block|}
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
literal|" to src new group? "
operator|+
name|retval
argument_list|)
expr_stmt|;
block|}
return|return
name|retval
return|;
block|}
block|}
end_class

end_unit

