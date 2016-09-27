begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

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
name|NoSuchElementException
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
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|DataReadMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|InputMetrics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|Metrics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|ShuffleReadMetrics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|ShuffleWriteMetrics
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
name|Predicate
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
name|Predicates
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
name|Collections2
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
name|Sets
import|;
end_import

begin_comment
comment|/**  * Provides metrics collected for a submitted job.  *  * The collected metrics can be analysed at different levels of granularity:  * - Global (all Spark jobs triggered by client job)  * - Spark job  * - Stage  * - Task  *  * Only successful, non-speculative tasks are considered. Metrics are updated as tasks finish,  * so snapshots can be retrieved before the whole job completes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsCollection
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|TaskInfo
argument_list|>
name|taskMetrics
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|public
name|Metrics
name|getAllMetrics
parameter_list|()
block|{
return|return
name|aggregate
argument_list|(
name|Predicates
operator|.
expr|<
name|TaskInfo
operator|>
name|alwaysTrue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getJobIds
parameter_list|()
block|{
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Integer
argument_list|>
name|fun
init|=
operator|new
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|jobId
return|;
block|}
block|}
decl_stmt|;
return|return
name|transform
argument_list|(
name|Predicates
operator|.
expr|<
name|TaskInfo
operator|>
name|alwaysTrue
argument_list|()
argument_list|,
name|fun
argument_list|)
return|;
block|}
specifier|public
name|Metrics
name|getJobMetrics
parameter_list|(
name|int
name|jobId
parameter_list|)
block|{
return|return
name|aggregate
argument_list|(
operator|new
name|JobFilter
argument_list|(
name|jobId
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getStageIds
parameter_list|(
name|int
name|jobId
parameter_list|)
block|{
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Integer
argument_list|>
name|fun
init|=
operator|new
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|stageId
return|;
block|}
block|}
decl_stmt|;
return|return
name|transform
argument_list|(
operator|new
name|JobFilter
argument_list|(
name|jobId
argument_list|)
argument_list|,
name|fun
argument_list|)
return|;
block|}
specifier|public
name|Metrics
name|getStageMetrics
parameter_list|(
specifier|final
name|int
name|jobId
parameter_list|,
specifier|final
name|int
name|stageId
parameter_list|)
block|{
return|return
name|aggregate
argument_list|(
operator|new
name|StageFilter
argument_list|(
name|jobId
argument_list|,
name|stageId
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Long
argument_list|>
name|getTaskIds
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|)
block|{
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Long
argument_list|>
name|fun
init|=
operator|new
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|taskId
return|;
block|}
block|}
decl_stmt|;
return|return
name|transform
argument_list|(
operator|new
name|StageFilter
argument_list|(
name|jobId
argument_list|,
name|stageId
argument_list|)
argument_list|,
name|fun
argument_list|)
return|;
block|}
specifier|public
name|Metrics
name|getTaskMetrics
parameter_list|(
specifier|final
name|int
name|jobId
parameter_list|,
specifier|final
name|int
name|stageId
parameter_list|,
specifier|final
name|long
name|taskId
parameter_list|)
block|{
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
name|filter
init|=
operator|new
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|jobId
operator|==
name|input
operator|.
name|jobId
operator|&&
name|stageId
operator|==
name|input
operator|.
name|stageId
operator|&&
name|taskId
operator|==
name|input
operator|.
name|taskId
return|;
block|}
block|}
decl_stmt|;
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|TaskInfo
argument_list|>
name|it
init|=
name|Collections2
operator|.
name|filter
argument_list|(
name|taskMetrics
argument_list|,
name|filter
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
name|it
operator|.
name|next
argument_list|()
operator|.
name|metrics
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"Task not found."
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addMetrics
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|,
name|long
name|taskId
parameter_list|,
name|Metrics
name|metrics
parameter_list|)
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|taskMetrics
operator|.
name|add
argument_list|(
operator|new
name|TaskInfo
argument_list|(
name|jobId
argument_list|,
name|stageId
argument_list|,
name|taskId
argument_list|,
name|metrics
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|transform
parameter_list|(
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
name|filter
parameter_list|,
name|Function
argument_list|<
name|TaskInfo
argument_list|,
name|T
argument_list|>
name|fun
parameter_list|)
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|Collection
argument_list|<
name|TaskInfo
argument_list|>
name|filtered
init|=
name|Collections2
operator|.
name|filter
argument_list|(
name|taskMetrics
argument_list|,
name|filter
argument_list|)
decl_stmt|;
return|return
name|Sets
operator|.
name|newHashSet
argument_list|(
name|Collections2
operator|.
name|transform
argument_list|(
name|filtered
argument_list|,
name|fun
argument_list|)
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Metrics
name|aggregate
parameter_list|(
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
name|filter
parameter_list|)
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Task metrics.
name|long
name|executorDeserializeTime
init|=
literal|0L
decl_stmt|;
name|long
name|executorRunTime
init|=
literal|0L
decl_stmt|;
name|long
name|resultSize
init|=
literal|0L
decl_stmt|;
name|long
name|jvmGCTime
init|=
literal|0L
decl_stmt|;
name|long
name|resultSerializationTime
init|=
literal|0L
decl_stmt|;
name|long
name|memoryBytesSpilled
init|=
literal|0L
decl_stmt|;
name|long
name|diskBytesSpilled
init|=
literal|0L
decl_stmt|;
comment|// Input metrics.
name|boolean
name|hasInputMetrics
init|=
literal|false
decl_stmt|;
name|long
name|bytesRead
init|=
literal|0L
decl_stmt|;
comment|// Shuffle read metrics.
name|boolean
name|hasShuffleReadMetrics
init|=
literal|false
decl_stmt|;
name|int
name|remoteBlocksFetched
init|=
literal|0
decl_stmt|;
name|int
name|localBlocksFetched
init|=
literal|0
decl_stmt|;
name|long
name|fetchWaitTime
init|=
literal|0L
decl_stmt|;
name|long
name|remoteBytesRead
init|=
literal|0L
decl_stmt|;
comment|// Shuffle write metrics.
name|long
name|shuffleBytesWritten
init|=
literal|0L
decl_stmt|;
name|long
name|shuffleWriteTime
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|TaskInfo
name|info
range|:
name|Collections2
operator|.
name|filter
argument_list|(
name|taskMetrics
argument_list|,
name|filter
argument_list|)
control|)
block|{
name|Metrics
name|m
init|=
name|info
operator|.
name|metrics
decl_stmt|;
name|executorDeserializeTime
operator|+=
name|m
operator|.
name|executorDeserializeTime
expr_stmt|;
name|executorRunTime
operator|+=
name|m
operator|.
name|executorRunTime
expr_stmt|;
name|resultSize
operator|+=
name|m
operator|.
name|resultSize
expr_stmt|;
name|jvmGCTime
operator|+=
name|m
operator|.
name|jvmGCTime
expr_stmt|;
name|resultSerializationTime
operator|+=
name|m
operator|.
name|resultSerializationTime
expr_stmt|;
name|memoryBytesSpilled
operator|+=
name|m
operator|.
name|memoryBytesSpilled
expr_stmt|;
name|diskBytesSpilled
operator|+=
name|m
operator|.
name|diskBytesSpilled
expr_stmt|;
if|if
condition|(
name|m
operator|.
name|inputMetrics
operator|!=
literal|null
condition|)
block|{
name|hasInputMetrics
operator|=
literal|true
expr_stmt|;
name|bytesRead
operator|+=
name|m
operator|.
name|inputMetrics
operator|.
name|bytesRead
expr_stmt|;
block|}
if|if
condition|(
name|m
operator|.
name|shuffleReadMetrics
operator|!=
literal|null
condition|)
block|{
name|hasShuffleReadMetrics
operator|=
literal|true
expr_stmt|;
name|remoteBlocksFetched
operator|+=
name|m
operator|.
name|shuffleReadMetrics
operator|.
name|remoteBlocksFetched
expr_stmt|;
name|localBlocksFetched
operator|+=
name|m
operator|.
name|shuffleReadMetrics
operator|.
name|localBlocksFetched
expr_stmt|;
name|fetchWaitTime
operator|+=
name|m
operator|.
name|shuffleReadMetrics
operator|.
name|fetchWaitTime
expr_stmt|;
name|remoteBytesRead
operator|+=
name|m
operator|.
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
expr_stmt|;
block|}
if|if
condition|(
name|m
operator|.
name|shuffleWriteMetrics
operator|!=
literal|null
condition|)
block|{
name|shuffleBytesWritten
operator|+=
name|m
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleBytesWritten
expr_stmt|;
name|shuffleWriteTime
operator|+=
name|m
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleWriteTime
expr_stmt|;
block|}
block|}
name|InputMetrics
name|inputMetrics
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasInputMetrics
condition|)
block|{
name|inputMetrics
operator|=
operator|new
name|InputMetrics
argument_list|(
name|bytesRead
argument_list|)
expr_stmt|;
block|}
name|ShuffleReadMetrics
name|shuffleReadMetrics
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasShuffleReadMetrics
condition|)
block|{
name|shuffleReadMetrics
operator|=
operator|new
name|ShuffleReadMetrics
argument_list|(
name|remoteBlocksFetched
argument_list|,
name|localBlocksFetched
argument_list|,
name|fetchWaitTime
argument_list|,
name|remoteBytesRead
argument_list|)
expr_stmt|;
block|}
name|ShuffleWriteMetrics
name|shuffleWriteMetrics
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasShuffleReadMetrics
condition|)
block|{
name|shuffleWriteMetrics
operator|=
operator|new
name|ShuffleWriteMetrics
argument_list|(
name|shuffleBytesWritten
argument_list|,
name|shuffleWriteTime
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Metrics
argument_list|(
name|executorDeserializeTime
argument_list|,
name|executorRunTime
argument_list|,
name|resultSize
argument_list|,
name|jvmGCTime
argument_list|,
name|resultSerializationTime
argument_list|,
name|memoryBytesSpilled
argument_list|,
name|diskBytesSpilled
argument_list|,
name|inputMetrics
argument_list|,
name|shuffleReadMetrics
argument_list|,
name|shuffleWriteMetrics
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TaskInfo
block|{
specifier|final
name|int
name|jobId
decl_stmt|;
specifier|final
name|int
name|stageId
decl_stmt|;
specifier|final
name|long
name|taskId
decl_stmt|;
specifier|final
name|Metrics
name|metrics
decl_stmt|;
name|TaskInfo
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|,
name|long
name|taskId
parameter_list|,
name|Metrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|stageId
operator|=
name|stageId
expr_stmt|;
name|this
operator|.
name|taskId
operator|=
name|taskId
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|JobFilter
implements|implements
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|jobId
decl_stmt|;
name|JobFilter
parameter_list|(
name|int
name|jobId
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|jobId
operator|==
name|input
operator|.
name|jobId
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|StageFilter
implements|implements
name|Predicate
argument_list|<
name|TaskInfo
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|jobId
decl_stmt|;
specifier|private
specifier|final
name|int
name|stageId
decl_stmt|;
name|StageFilter
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|stageId
operator|=
name|stageId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|TaskInfo
name|input
parameter_list|)
block|{
return|return
name|jobId
operator|==
name|input
operator|.
name|jobId
operator|&&
name|stageId
operator|==
name|input
operator|.
name|stageId
return|;
block|}
block|}
block|}
end_class

end_unit

