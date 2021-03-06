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
name|spark
operator|.
name|status
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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
name|spark
operator|.
name|executor
operator|.
name|TaskMetrics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|scheduler
operator|.
name|SparkListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|scheduler
operator|.
name|SparkListenerJobStart
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|scheduler
operator|.
name|SparkListenerTaskEnd
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|scheduler
operator|.
name|TaskInfo
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

begin_class
specifier|public
class|class
name|JobMetricsListener
extends|extends
name|SparkListener
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
name|JobMetricsListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|int
index|[]
argument_list|>
name|jobIdToStageId
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|stageIdToJobId
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|TaskMetrics
argument_list|,
name|TaskInfo
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|allJobMetrics
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onTaskEnd
parameter_list|(
name|SparkListenerTaskEnd
name|taskEnd
parameter_list|)
block|{
name|int
name|stageId
init|=
name|taskEnd
operator|.
name|stageId
argument_list|()
decl_stmt|;
name|Integer
name|jobId
init|=
name|stageIdToJobId
operator|.
name|get
argument_list|(
name|stageId
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobId
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can not find job id for stage["
operator|+
name|stageId
operator|+
literal|"]."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|TaskMetrics
argument_list|,
name|TaskInfo
argument_list|>
argument_list|>
argument_list|>
name|jobMetrics
init|=
name|allJobMetrics
operator|.
name|computeIfAbsent
argument_list|(
name|jobId
argument_list|,
name|k
lambda|->
name|Maps
operator|.
name|newHashMap
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|TaskMetrics
argument_list|,
name|TaskInfo
argument_list|>
argument_list|>
name|stageMetrics
init|=
name|jobMetrics
operator|.
name|computeIfAbsent
argument_list|(
name|stageId
argument_list|,
name|k
lambda|->
name|Lists
operator|.
name|newLinkedList
argument_list|()
argument_list|)
decl_stmt|;
name|stageMetrics
operator|.
name|add
argument_list|(
operator|new
name|AbstractMap
operator|.
name|SimpleEntry
argument_list|<>
argument_list|(
name|taskEnd
operator|.
name|taskMetrics
argument_list|()
argument_list|,
name|taskEnd
operator|.
name|taskInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onJobStart
parameter_list|(
name|SparkListenerJobStart
name|jobStart
parameter_list|)
block|{
name|int
name|jobId
init|=
name|jobStart
operator|.
name|jobId
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|jobStart
operator|.
name|stageIds
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
index|[]
name|intStageIds
init|=
operator|new
name|int
index|[
name|size
index|]
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Integer
name|stageId
init|=
operator|(
name|Integer
operator|)
name|jobStart
operator|.
name|stageIds
argument_list|()
operator|.
name|apply
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|intStageIds
index|[
name|i
index|]
operator|=
name|stageId
expr_stmt|;
name|stageIdToJobId
operator|.
name|put
argument_list|(
name|stageId
argument_list|,
name|jobId
argument_list|)
expr_stmt|;
block|}
name|jobIdToStageId
operator|.
name|put
argument_list|(
name|jobId
argument_list|,
name|intStageIds
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|TaskMetrics
argument_list|,
name|TaskInfo
argument_list|>
argument_list|>
argument_list|>
name|getJobMetric
parameter_list|(
name|int
name|jobId
parameter_list|)
block|{
return|return
name|allJobMetrics
operator|.
name|get
argument_list|(
name|jobId
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|cleanup
parameter_list|(
name|int
name|jobId
parameter_list|)
block|{
name|allJobMetrics
operator|.
name|remove
argument_list|(
name|jobId
argument_list|)
expr_stmt|;
name|jobIdToStageId
operator|.
name|remove
argument_list|(
name|jobId
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|iterator
init|=
name|stageIdToJobId
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|jobId
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

