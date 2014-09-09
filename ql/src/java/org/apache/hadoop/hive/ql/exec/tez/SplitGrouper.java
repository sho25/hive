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
name|HashSet
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SplitGrouper
operator|.
name|class
argument_list|)
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
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Original split size is "
operator|+
name|rawSplits
operator|.
name|length
operator|+
literal|" grouped split size is "
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
comment|// compute the total size per bucket
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
if|if
condition|(
name|split
operator|.
name|getLocations
argument_list|()
operator|!=
literal|null
condition|)
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
name|HashSet
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
block|}
end_class

end_unit

