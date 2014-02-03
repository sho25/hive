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
name|Set
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
comment|/**  * MapWork represents all the information used to run a map task on the cluster.  * It is first used when the query planner breaks the logical plan into tasks and  * used throughout physical optimization to track map-side operator plans, input  * paths, aliases, etc.  *  * ExecDriver will serialize the contents of this class and make sure it is  * distributed on the cluster. The ExecMapper will ultimately deserialize this  * class on the data nodes and setup it's operator pipeline accordingly.  *  * This class is also used in the explain command any property with the  * appropriate annotation will be displayed in the explain output.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"serial"
block|,
literal|"deprecation"
block|}
argument_list|)
specifier|public
class|class
name|MapWork
extends|extends
name|BaseWork
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
name|MapWork
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|hadoopSupportsSplittable
decl_stmt|;
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
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
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
init|=
operator|new
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
argument_list|()
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartnInfo
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
argument_list|()
decl_stmt|;
comment|// If this map task has a FileSinkOperator, and bucketing/sorting metadata can be
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
specifier|private
name|MapredLocalWork
name|mapLocalWork
decl_stmt|;
specifier|private
name|Path
name|tmpHDFSPath
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
comment|//use sampled partitioning
specifier|private
name|int
name|samplingType
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SAMPLING_ON_PREV_MR
init|=
literal|1
decl_stmt|;
comment|// todo HIVE-3841
specifier|public
specifier|static
specifier|final
name|int
name|SAMPLING_ON_START
init|=
literal|2
decl_stmt|;
comment|// sampling on task running
comment|// the following two are used for join processing
specifier|private
name|QBJoinTree
name|joinTree
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
name|boolean
name|useBucketizedHiveInputFormat
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|scratchColumnVectorTypes
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|scratchColumnMap
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|vectorMode
init|=
literal|false
decl_stmt|;
specifier|public
name|MapWork
parameter_list|()
block|{}
specifier|public
name|MapWork
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
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
comment|/**    * This is used to display and verify output of "Path -> Alias" in test framework.    *    * QTestUtil masks "Path -> Alias" and makes verification impossible.    * By keeping "Path -> Alias" intact and adding a new display name which is not    * masked by QTestUtil by removing prefix.    *    * Notes: we would still be masking for intermediate directories.    *    * @return    */
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Split Sample"
argument_list|,
name|normalExplain
operator|=
literal|false
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
comment|/**    * For each map side operator - stores the alias the operator is working on    * behalf of in the operator runtime state. This is used by reduce sink    * operator - but could be useful for debugging as well.    */
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Execution mode"
argument_list|)
specifier|public
name|String
name|getVectorModeOn
parameter_list|()
block|{
return|return
name|vectorMode
condition|?
literal|"vectorized"
else|:
literal|null
return|;
block|}
annotation|@
name|Override
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map Operator Tree"
argument_list|)
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllRootOperators
parameter_list|()
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
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
name|opSet
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
return|return
name|opSet
return|;
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
specifier|public
name|void
name|initialize
parameter_list|()
block|{
name|setAliases
argument_list|()
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
name|String
name|getIndexIntermediateFile
parameter_list|()
block|{
return|return
name|indexIntermediateFile
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getAliases
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|aliasToWork
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getWorks
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|aliasToWork
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getPaths
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|pathToAliases
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|getPartitionDescs
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
argument_list|(
name|aliasToPartnInfo
operator|.
name|values
argument_list|()
argument_list|)
return|;
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
name|Path
name|getTmpHDFSPath
parameter_list|()
block|{
return|return
name|tmpHDFSPath
return|;
block|}
specifier|public
name|void
name|setTmpHDFSPath
parameter_list|(
name|Path
name|tmpHDFSPath
parameter_list|)
block|{
name|this
operator|.
name|tmpHDFSPath
operator|=
name|tmpHDFSPath
expr_stmt|;
block|}
specifier|public
name|void
name|mergingInto
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
comment|// currently, this is sole field affecting mergee task
name|mapWork
operator|.
name|useBucketizedHiveInputFormat
operator||=
name|useBucketizedHiveInputFormat
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
name|int
name|getSamplingType
parameter_list|()
block|{
return|return
name|samplingType
return|;
block|}
specifier|public
name|void
name|setSamplingType
parameter_list|(
name|int
name|samplingType
parameter_list|)
block|{
name|this
operator|.
name|samplingType
operator|=
name|samplingType
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Sampling"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|String
name|getSamplingTypeString
parameter_list|()
block|{
return|return
name|samplingType
operator|==
literal|1
condition|?
literal|"SAMPLING_ON_PREV_MR"
else|:
name|samplingType
operator|==
literal|2
condition|?
literal|"SAMPLING_ON_START"
else|:
literal|null
return|;
block|}
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|job
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
name|job
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
name|job
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|getScratchColumnVectorTypes
parameter_list|()
block|{
return|return
name|scratchColumnVectorTypes
return|;
block|}
specifier|public
name|void
name|setScratchColumnVectorTypes
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|scratchColumnVectorTypes
parameter_list|)
block|{
name|this
operator|.
name|scratchColumnVectorTypes
operator|=
name|scratchColumnVectorTypes
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|getScratchColumnMap
parameter_list|()
block|{
return|return
name|scratchColumnMap
return|;
block|}
specifier|public
name|void
name|setScratchColumnMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|scratchColumnMap
parameter_list|)
block|{
name|this
operator|.
name|scratchColumnMap
operator|=
name|scratchColumnMap
expr_stmt|;
block|}
specifier|public
name|boolean
name|getVectorMode
parameter_list|()
block|{
return|return
name|vectorMode
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setVectorMode
parameter_list|(
name|boolean
name|vectorMode
parameter_list|)
block|{
name|this
operator|.
name|vectorMode
operator|=
name|vectorMode
expr_stmt|;
block|}
block|}
end_class

end_unit

