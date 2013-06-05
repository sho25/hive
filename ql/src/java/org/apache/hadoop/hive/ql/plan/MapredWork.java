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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|FileSinkOperator
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
name|Operator
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
name|OperatorUtils
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
name|optimizer
operator|.
name|physical
operator|.
name|BucketingSortingCtx
operator|.
name|BucketCol
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
name|optimizer
operator|.
name|physical
operator|.
name|BucketingSortingCtx
operator|.
name|SortCol
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
name|parse
operator|.
name|OpParseContext
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
name|parse
operator|.
name|QBJoinTree
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
name|parse
operator|.
name|SplitSample
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

begin_comment
comment|/**  * MapredWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map Reduce"
argument_list|)
specifier|public
class|class
name|MapredWork
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|command
decl_stmt|;
comment|// map side work
comment|// use LinkedHashMap to make sure the iteration order is
comment|// deterministic, to ease testing
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartnInfo
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
decl_stmt|;
comment|// map<->reduce interface
comment|// schema of the map-reduce 'key' object - this is homogeneous
specifier|private
name|TableDesc
name|keyDesc
decl_stmt|;
comment|// schema of the map-reduce 'val' object - this is heterogeneous
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|Integer
name|numReduceTasks
decl_stmt|;
specifier|private
name|Integer
name|numMapTasks
decl_stmt|;
specifier|private
name|Long
name|maxSplitSize
decl_stmt|;
specifier|private
name|Long
name|minSplitSize
decl_stmt|;
specifier|private
name|Long
name|minSplitSizePerNode
decl_stmt|;
specifier|private
name|Long
name|minSplitSizePerRack
decl_stmt|;
specifier|private
name|boolean
name|needsTagging
decl_stmt|;
specifier|private
name|boolean
name|hadoopSupportsSplittable
decl_stmt|;
specifier|private
name|MapredLocalWork
name|mapLocalWork
decl_stmt|;
specifier|private
name|String
name|inputformat
decl_stmt|;
specifier|private
name|String
name|indexIntermediateFile
decl_stmt|;
specifier|private
name|boolean
name|gatheringStats
decl_stmt|;
specifier|private
name|String
name|tmpHDFSFileURI
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opParseCtxMap
decl_stmt|;
specifier|private
name|QBJoinTree
name|joinTree
decl_stmt|;
specifier|private
name|boolean
name|mapperCannotSpanPartns
decl_stmt|;
comment|// used to indicate the input is sorted, and so a BinarySearchRecordReader shoudl be used
specifier|private
name|boolean
name|inputFormatSorted
init|=
literal|false
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|useBucketizedHiveInputFormat
decl_stmt|;
comment|// if this is true, this means that this is the map reduce task which writes the final data,
comment|// ignoring the optional merge task
specifier|private
name|boolean
name|finalMapRed
init|=
literal|false
decl_stmt|;
comment|// If this map reduce task has a FileSinkOperator, and bucketing/sorting metadata can be
comment|// inferred about the data being written by that operator, these are mappings from the directory
comment|// that operator writes into to the bucket/sort columns for that data.
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|bucketedColsByDirectory
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|sortedColsByDirectory
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|MapredWork
parameter_list|()
block|{
name|aliasToPartnInfo
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MapredWork
parameter_list|(
specifier|final
name|String
name|command
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|,
specifier|final
name|TableDesc
name|keyDesc
parameter_list|,
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
parameter_list|,
specifier|final
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|,
specifier|final
name|Integer
name|numReduceTasks
parameter_list|,
specifier|final
name|MapredLocalWork
name|mapLocalWork
parameter_list|,
specifier|final
name|boolean
name|hadoopSupportsSplittable
parameter_list|)
block|{
name|this
operator|.
name|command
operator|=
name|command
expr_stmt|;
name|this
operator|.
name|pathToAliases
operator|=
name|pathToAliases
expr_stmt|;
name|this
operator|.
name|pathToPartitionInfo
operator|=
name|pathToPartitionInfo
expr_stmt|;
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
name|this
operator|.
name|keyDesc
operator|=
name|keyDesc
expr_stmt|;
name|this
operator|.
name|tagToValueDesc
operator|=
name|tagToValueDesc
expr_stmt|;
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
name|this
operator|.
name|numReduceTasks
operator|=
name|numReduceTasks
expr_stmt|;
name|this
operator|.
name|mapLocalWork
operator|=
name|mapLocalWork
expr_stmt|;
name|aliasToPartnInfo
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|hadoopSupportsSplittable
operator|=
name|hadoopSupportsSplittable
expr_stmt|;
name|maxSplitSize
operator|=
literal|null
expr_stmt|;
name|minSplitSize
operator|=
literal|null
expr_stmt|;
name|minSplitSizePerNode
operator|=
literal|null
expr_stmt|;
name|minSplitSizePerRack
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|String
name|getCommand
parameter_list|()
block|{
return|return
name|command
return|;
block|}
specifier|public
name|void
name|setCommand
parameter_list|(
specifier|final
name|String
name|command
parameter_list|)
block|{
name|this
operator|.
name|command
operator|=
name|command
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Alias"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|getPathToAliases
parameter_list|()
block|{
return|return
name|pathToAliases
return|;
block|}
specifier|public
name|void
name|setPathToAliases
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|)
block|{
name|this
operator|.
name|pathToAliases
operator|=
name|pathToAliases
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Truncated Path -> Alias"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
comment|/**    * This is used to display and verify output of "Path -> Alias" in test framework.    *    * {@link QTestUtil} masks "Path -> Alias" and makes verification impossible.    * By keeping "Path -> Alias" intact and adding a new display name which is not    * masked by {@link QTestUtil} by removing prefix.    *    * Notes: we would still be masking for intermediate directories.    *    * @return    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTruncatedPathToAliases
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|trunPathToAliases
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|itr
init|=
name|this
operator|.
name|pathToAliases
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|origiKey
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|newKey
init|=
name|PlanUtils
operator|.
name|removePrefixFromWarehouseConfig
argument_list|(
name|origiKey
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|trunPathToAliases
operator|.
name|put
argument_list|(
name|newKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|trunPathToAliases
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Partition"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|getPathToPartitionInfo
parameter_list|()
block|{
return|return
name|pathToPartitionInfo
return|;
block|}
specifier|public
name|void
name|setPathToPartitionInfo
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|)
block|{
name|this
operator|.
name|pathToPartitionInfo
operator|=
name|pathToPartitionInfo
expr_stmt|;
block|}
comment|/**    * @return the aliasToPartnInfo    */
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|getAliasToPartnInfo
parameter_list|()
block|{
return|return
name|aliasToPartnInfo
return|;
block|}
comment|/**    * @param aliasToPartnInfo    *          the aliasToPartnInfo to set    */
specifier|public
name|void
name|setAliasToPartnInfo
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartnInfo
parameter_list|)
block|{
name|this
operator|.
name|aliasToPartnInfo
operator|=
name|aliasToPartnInfo
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Operator Tree"
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getAliasToWork
parameter_list|()
block|{
return|return
name|aliasToWork
return|;
block|}
specifier|public
name|void
name|setAliasToWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
block|}
specifier|public
name|void
name|mergeAliasedInput
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|pathDir
parameter_list|,
name|PartitionDesc
name|partitionInfo
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|pathToAliases
operator|.
name|get
argument_list|(
name|pathDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliases
operator|==
literal|null
condition|)
block|{
name|aliases
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
name|pathDir
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
name|pathDir
argument_list|,
name|partitionInfo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|aliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return the mapredLocalWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Local Work"
argument_list|)
specifier|public
name|MapredLocalWork
name|getMapLocalWork
parameter_list|()
block|{
return|return
name|mapLocalWork
return|;
block|}
comment|/**    * @param mapLocalWork    *          the mapredLocalWork to set    */
specifier|public
name|void
name|setMapLocalWork
parameter_list|(
specifier|final
name|MapredLocalWork
name|mapLocalWork
parameter_list|)
block|{
name|this
operator|.
name|mapLocalWork
operator|=
name|mapLocalWork
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getKeyDesc
parameter_list|()
block|{
return|return
name|keyDesc
return|;
block|}
comment|/**    * If the plan has a reducer and correspondingly a reduce-sink, then store the TableDesc pointing    * to keySerializeInfo of the ReduceSink    *    * @param keyDesc    */
specifier|public
name|void
name|setKeyDesc
parameter_list|(
specifier|final
name|TableDesc
name|keyDesc
parameter_list|)
block|{
name|this
operator|.
name|keyDesc
operator|=
name|keyDesc
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getTagToValueDesc
parameter_list|()
block|{
return|return
name|tagToValueDesc
return|;
block|}
specifier|public
name|void
name|setTagToValueDesc
parameter_list|(
specifier|final
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
parameter_list|)
block|{
name|this
operator|.
name|tagToValueDesc
operator|=
name|tagToValueDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Reduce Operator Tree"
argument_list|)
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getReducer
parameter_list|()
block|{
return|return
name|reducer
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Split Sample"
argument_list|)
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|getNameToSplitSample
parameter_list|()
block|{
return|return
name|nameToSplitSample
return|;
block|}
specifier|public
name|void
name|setNameToSplitSample
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
parameter_list|)
block|{
name|this
operator|.
name|nameToSplitSample
operator|=
name|nameToSplitSample
expr_stmt|;
block|}
specifier|public
name|void
name|setReducer
parameter_list|(
specifier|final
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|)
block|{
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
block|}
specifier|public
name|Integer
name|getNumMapTasks
parameter_list|()
block|{
return|return
name|numMapTasks
return|;
block|}
specifier|public
name|void
name|setNumMapTasks
parameter_list|(
name|Integer
name|numMapTasks
parameter_list|)
block|{
name|this
operator|.
name|numMapTasks
operator|=
name|numMapTasks
expr_stmt|;
block|}
comment|/**    * If the number of reducers is -1, the runtime will automatically figure it    * out by input data size.    *    * The number of reducers will be a positive number only in case the target    * table is bucketed into N buckets (through CREATE TABLE). This feature is    * not supported yet, so the number of reducers will always be -1 for now.    */
specifier|public
name|Integer
name|getNumReduceTasks
parameter_list|()
block|{
return|return
name|numReduceTasks
return|;
block|}
specifier|public
name|void
name|setNumReduceTasks
parameter_list|(
specifier|final
name|Integer
name|numReduceTasks
parameter_list|)
block|{
name|this
operator|.
name|numReduceTasks
operator|=
name|numReduceTasks
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Bucketed Columns"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|getBucketedColsByDirectory
parameter_list|()
block|{
return|return
name|bucketedColsByDirectory
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Sorted Columns"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|getSortedColsByDirectory
parameter_list|()
block|{
return|return
name|sortedColsByDirectory
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|addMapWork
parameter_list|(
name|String
name|path
parameter_list|,
name|String
name|alias
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|work
parameter_list|,
name|PartitionDesc
name|pd
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|curAliases
init|=
name|pathToAliases
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|curAliases
operator|==
literal|null
condition|)
block|{
assert|assert
operator|(
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|==
literal|null
operator|)
assert|;
name|curAliases
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|curAliases
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|pd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
operator|(
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|!=
literal|null
operator|)
assert|;
block|}
for|for
control|(
name|String
name|oneAlias
range|:
name|curAliases
control|)
block|{
if|if
condition|(
name|oneAlias
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Multiple aliases named: "
operator|+
name|alias
operator|+
literal|" for path: "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|curAliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
if|if
condition|(
name|aliasToWork
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Existing work for alias: "
operator|+
name|alias
argument_list|)
throw|;
block|}
name|aliasToWork
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|String
name|isInvalid
parameter_list|()
block|{
if|if
condition|(
operator|(
name|getNumReduceTasks
argument_list|()
operator|>=
literal|1
operator|)
operator|&&
operator|(
name|getReducer
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|"Reducers> 0 but no reduce operator"
return|;
block|}
if|if
condition|(
operator|(
name|getNumReduceTasks
argument_list|()
operator|==
literal|0
operator|)
operator|&&
operator|(
name|getReducer
argument_list|()
operator|!=
literal|null
operator|)
condition|)
block|{
return|return
literal|"Reducers == 0 but reduce operator specified"
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|toXML
parameter_list|()
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|serializeMapRedWork
argument_list|(
name|this
argument_list|,
name|baos
argument_list|)
expr_stmt|;
return|return
operator|(
name|baos
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
comment|// non bean
comment|/**    * For each map side operator - stores the alias the operator is working on    * behalf of in the operator runtime state. This is used by reducesink    * operator - but could be useful for debugging as well.    */
specifier|private
name|void
name|setAliases
parameter_list|()
block|{
if|if
condition|(
name|aliasToWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|String
name|oneAlias
range|:
name|aliasToWork
operator|.
name|keySet
argument_list|()
control|)
block|{
name|aliasToWork
operator|.
name|get
argument_list|(
name|oneAlias
argument_list|)
operator|.
name|setAlias
argument_list|(
name|oneAlias
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Derive additional attributes to be rendered by EXPLAIN.    */
specifier|public
name|void
name|deriveExplainAttributes
parameter_list|()
block|{
if|if
condition|(
name|pathToPartitionInfo
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|pathToPartitionInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|deriveBaseFileName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|mapLocalWork
operator|!=
literal|null
condition|)
block|{
name|mapLocalWork
operator|.
name|deriveExplainAttributes
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|()
block|{
name|setAliases
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Needs Tagging"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|boolean
name|getNeedsTagging
parameter_list|()
block|{
return|return
name|needsTagging
return|;
block|}
specifier|public
name|void
name|setNeedsTagging
parameter_list|(
name|boolean
name|needsTagging
parameter_list|)
block|{
name|this
operator|.
name|needsTagging
operator|=
name|needsTagging
expr_stmt|;
block|}
specifier|public
name|boolean
name|getHadoopSupportsSplittable
parameter_list|()
block|{
return|return
name|hadoopSupportsSplittable
return|;
block|}
specifier|public
name|void
name|setHadoopSupportsSplittable
parameter_list|(
name|boolean
name|hadoopSupportsSplittable
parameter_list|)
block|{
name|this
operator|.
name|hadoopSupportsSplittable
operator|=
name|hadoopSupportsSplittable
expr_stmt|;
block|}
specifier|public
name|Long
name|getMaxSplitSize
parameter_list|()
block|{
return|return
name|maxSplitSize
return|;
block|}
specifier|public
name|void
name|setMaxSplitSize
parameter_list|(
name|Long
name|maxSplitSize
parameter_list|)
block|{
name|this
operator|.
name|maxSplitSize
operator|=
name|maxSplitSize
expr_stmt|;
block|}
specifier|public
name|Long
name|getMinSplitSize
parameter_list|()
block|{
return|return
name|minSplitSize
return|;
block|}
specifier|public
name|void
name|setMinSplitSize
parameter_list|(
name|Long
name|minSplitSize
parameter_list|)
block|{
name|this
operator|.
name|minSplitSize
operator|=
name|minSplitSize
expr_stmt|;
block|}
specifier|public
name|Long
name|getMinSplitSizePerNode
parameter_list|()
block|{
return|return
name|minSplitSizePerNode
return|;
block|}
specifier|public
name|void
name|setMinSplitSizePerNode
parameter_list|(
name|Long
name|minSplitSizePerNode
parameter_list|)
block|{
name|this
operator|.
name|minSplitSizePerNode
operator|=
name|minSplitSizePerNode
expr_stmt|;
block|}
specifier|public
name|Long
name|getMinSplitSizePerRack
parameter_list|()
block|{
return|return
name|minSplitSizePerRack
return|;
block|}
specifier|public
name|void
name|setMinSplitSizePerRack
parameter_list|(
name|Long
name|minSplitSizePerRack
parameter_list|)
block|{
name|this
operator|.
name|minSplitSizePerRack
operator|=
name|minSplitSizePerRack
expr_stmt|;
block|}
specifier|public
name|String
name|getInputformat
parameter_list|()
block|{
return|return
name|inputformat
return|;
block|}
specifier|public
name|void
name|setInputformat
parameter_list|(
name|String
name|inputformat
parameter_list|)
block|{
name|this
operator|.
name|inputformat
operator|=
name|inputformat
expr_stmt|;
block|}
specifier|public
name|String
name|getIndexIntermediateFile
parameter_list|()
block|{
return|return
name|indexIntermediateFile
return|;
block|}
specifier|public
name|void
name|addIndexIntermediateFile
parameter_list|(
name|String
name|fileName
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|indexIntermediateFile
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|indexIntermediateFile
operator|=
name|fileName
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|indexIntermediateFile
operator|+=
literal|","
operator|+
name|fileName
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setGatheringStats
parameter_list|(
name|boolean
name|gatherStats
parameter_list|)
block|{
name|this
operator|.
name|gatheringStats
operator|=
name|gatherStats
expr_stmt|;
block|}
specifier|public
name|boolean
name|isGatheringStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|gatheringStats
return|;
block|}
specifier|public
name|void
name|setMapperCannotSpanPartns
parameter_list|(
name|boolean
name|mapperCannotSpanPartns
parameter_list|)
block|{
name|this
operator|.
name|mapperCannotSpanPartns
operator|=
name|mapperCannotSpanPartns
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMapperCannotSpanPartns
parameter_list|()
block|{
return|return
name|this
operator|.
name|mapperCannotSpanPartns
return|;
block|}
specifier|public
name|String
name|getTmpHDFSFileURI
parameter_list|()
block|{
return|return
name|tmpHDFSFileURI
return|;
block|}
specifier|public
name|void
name|setTmpHDFSFileURI
parameter_list|(
name|String
name|tmpHDFSFileURI
parameter_list|)
block|{
name|this
operator|.
name|tmpHDFSFileURI
operator|=
name|tmpHDFSFileURI
expr_stmt|;
block|}
specifier|public
name|QBJoinTree
name|getJoinTree
parameter_list|()
block|{
return|return
name|joinTree
return|;
block|}
specifier|public
name|void
name|setJoinTree
parameter_list|(
name|QBJoinTree
name|joinTree
parameter_list|)
block|{
name|this
operator|.
name|joinTree
operator|=
name|joinTree
expr_stmt|;
block|}
specifier|public
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|getOpParseCtxMap
parameter_list|()
block|{
return|return
name|opParseCtxMap
return|;
block|}
specifier|public
name|void
name|setOpParseCtxMap
parameter_list|(
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opParseCtxMap
parameter_list|)
block|{
name|this
operator|.
name|opParseCtxMap
operator|=
name|opParseCtxMap
expr_stmt|;
block|}
specifier|public
name|boolean
name|isInputFormatSorted
parameter_list|()
block|{
return|return
name|inputFormatSorted
return|;
block|}
specifier|public
name|void
name|setInputFormatSorted
parameter_list|(
name|boolean
name|inputFormatSorted
parameter_list|)
block|{
name|this
operator|.
name|inputFormatSorted
operator|=
name|inputFormatSorted
expr_stmt|;
block|}
specifier|public
name|void
name|resolveDynamicPartitionStoredAsSubDirsMerge
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Path
name|path
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
parameter_list|,
name|PartitionDesc
name|partDesc
parameter_list|)
block|{
name|pathToAliases
operator|.
name|put
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllOperators
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|getReducer
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opList
operator|.
name|add
argument_list|(
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pa
init|=
name|getPathToAliases
argument_list|()
decl_stmt|;
if|if
condition|(
name|pa
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|ls
range|:
name|pa
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|a
range|:
name|ls
control|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|a
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|!=
literal|null
condition|)
block|{
name|opList
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|//recursively add all children
while|while
condition|(
operator|!
name|opList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|opList
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opList
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|returnList
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
return|return
name|returnList
return|;
block|}
specifier|public
name|boolean
name|isUseBucketizedHiveInputFormat
parameter_list|()
block|{
return|return
name|useBucketizedHiveInputFormat
return|;
block|}
specifier|public
name|void
name|setUseBucketizedHiveInputFormat
parameter_list|(
name|boolean
name|useBucketizedHiveInputFormat
parameter_list|)
block|{
name|this
operator|.
name|useBucketizedHiveInputFormat
operator|=
name|useBucketizedHiveInputFormat
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFinalMapRed
parameter_list|()
block|{
return|return
name|finalMapRed
return|;
block|}
specifier|public
name|void
name|setFinalMapRed
parameter_list|(
name|boolean
name|finalMapRed
parameter_list|)
block|{
name|this
operator|.
name|finalMapRed
operator|=
name|finalMapRed
expr_stmt|;
block|}
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
block|{
for|for
control|(
name|PartitionDesc
name|partition
range|:
name|aliasToPartnInfo
operator|.
name|values
argument_list|()
control|)
block|{
name|PlanUtils
operator|.
name|configureJobConf
argument_list|(
name|partition
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|mappers
init|=
name|aliasToWork
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|FileSinkOperator
name|fs
range|:
name|OperatorUtils
operator|.
name|findOperators
argument_list|(
name|mappers
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|PlanUtils
operator|.
name|configureJobConf
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getTableInfo
argument_list|()
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reducer
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileSinkOperator
name|fs
range|:
name|OperatorUtils
operator|.
name|findOperators
argument_list|(
name|reducer
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|PlanUtils
operator|.
name|configureJobConf
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getTableInfo
argument_list|()
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

