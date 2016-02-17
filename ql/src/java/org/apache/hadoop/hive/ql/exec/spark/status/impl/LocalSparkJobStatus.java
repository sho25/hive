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
name|java
operator|.
name|util
operator|.
name|Set
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|Statistic
operator|.
name|SparkStatistics
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
name|spark
operator|.
name|Statistic
operator|.
name|SparkStatisticsBuilder
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
name|spark
operator|.
name|status
operator|.
name|SparkJobStatus
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
name|spark
operator|.
name|status
operator|.
name|SparkStageProgress
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
name|MetricsCollection
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
name|counter
operator|.
name|SparkCounters
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
name|JobExecutionStatus
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
name|SparkJobInfo
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
name|SparkStageInfo
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
name|api
operator|.
name|java
operator|.
name|JavaFutureAction
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
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
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
name|ShuffleReadMetrics
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
name|ShuffleWriteMetrics
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
name|scala
operator|.
name|Option
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
name|LocalSparkJobStatus
implements|implements
name|SparkJobStatus
block|{
specifier|private
specifier|final
name|JavaSparkContext
name|sparkContext
decl_stmt|;
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
name|LocalSparkJobStatus
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|int
name|jobId
decl_stmt|;
comment|// After SPARK-2321, we only use JobMetricsListener to get job metrics
comment|// TODO: remove it when the new API provides equivalent functionality
specifier|private
name|JobMetricsListener
name|jobMetricsListener
decl_stmt|;
specifier|private
name|SparkCounters
name|sparkCounters
decl_stmt|;
specifier|private
name|JavaFutureAction
argument_list|<
name|Void
argument_list|>
name|future
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
decl_stmt|;
specifier|public
name|LocalSparkJobStatus
parameter_list|(
name|JavaSparkContext
name|sparkContext
parameter_list|,
name|int
name|jobId
parameter_list|,
name|JobMetricsListener
name|jobMetricsListener
parameter_list|,
name|SparkCounters
name|sparkCounters
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
parameter_list|,
name|JavaFutureAction
argument_list|<
name|Void
argument_list|>
name|future
parameter_list|)
block|{
name|this
operator|.
name|sparkContext
operator|=
name|sparkContext
expr_stmt|;
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|jobMetricsListener
operator|=
name|jobMetricsListener
expr_stmt|;
name|this
operator|.
name|sparkCounters
operator|=
name|sparkCounters
expr_stmt|;
name|this
operator|.
name|cachedRDDIds
operator|=
name|cachedRDDIds
expr_stmt|;
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getAppID
parameter_list|()
block|{
return|return
name|sparkContext
operator|.
name|sc
argument_list|()
operator|.
name|applicationId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getJobId
parameter_list|()
block|{
return|return
name|jobId
return|;
block|}
annotation|@
name|Override
specifier|public
name|JobExecutionStatus
name|getState
parameter_list|()
block|{
name|SparkJobInfo
name|sparkJobInfo
init|=
name|getJobInfo
argument_list|()
decl_stmt|;
comment|// For spark job with empty source data, it's not submitted actually, so we would never
comment|// receive JobStart/JobEnd event in JobStateListener, use JavaFutureAction to get current
comment|// job state.
if|if
condition|(
name|sparkJobInfo
operator|==
literal|null
operator|&&
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
try|try
block|{
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to run job "
operator|+
name|jobId
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|JobExecutionStatus
operator|.
name|FAILED
return|;
block|}
return|return
name|JobExecutionStatus
operator|.
name|SUCCEEDED
return|;
block|}
return|return
name|sparkJobInfo
operator|==
literal|null
condition|?
literal|null
else|:
name|sparkJobInfo
operator|.
name|status
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
index|[]
name|getStageIds
parameter_list|()
block|{
name|SparkJobInfo
name|sparkJobInfo
init|=
name|getJobInfo
argument_list|()
decl_stmt|;
return|return
name|sparkJobInfo
operator|==
literal|null
condition|?
operator|new
name|int
index|[
literal|0
index|]
else|:
name|sparkJobInfo
operator|.
name|stageIds
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|SparkStageProgress
argument_list|>
name|getSparkStageProgress
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|SparkStageProgress
argument_list|>
name|stageProgresses
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SparkStageProgress
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|stageId
range|:
name|getStageIds
argument_list|()
control|)
block|{
name|SparkStageInfo
name|sparkStageInfo
init|=
name|getStageInfo
argument_list|(
name|stageId
argument_list|)
decl_stmt|;
if|if
condition|(
name|sparkStageInfo
operator|!=
literal|null
condition|)
block|{
name|int
name|runningTaskCount
init|=
name|sparkStageInfo
operator|.
name|numActiveTasks
argument_list|()
decl_stmt|;
name|int
name|completedTaskCount
init|=
name|sparkStageInfo
operator|.
name|numCompletedTasks
argument_list|()
decl_stmt|;
name|int
name|failedTaskCount
init|=
name|sparkStageInfo
operator|.
name|numFailedTasks
argument_list|()
decl_stmt|;
name|int
name|totalTaskCount
init|=
name|sparkStageInfo
operator|.
name|numTasks
argument_list|()
decl_stmt|;
name|SparkStageProgress
name|sparkStageProgress
init|=
operator|new
name|SparkStageProgress
argument_list|(
name|totalTaskCount
argument_list|,
name|completedTaskCount
argument_list|,
name|runningTaskCount
argument_list|,
name|failedTaskCount
argument_list|)
decl_stmt|;
name|stageProgresses
operator|.
name|put
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|sparkStageInfo
operator|.
name|stageId
argument_list|()
argument_list|)
operator|+
literal|"_"
operator|+
name|sparkStageInfo
operator|.
name|currentAttemptId
argument_list|()
argument_list|,
name|sparkStageProgress
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|stageProgresses
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkCounters
name|getCounter
parameter_list|()
block|{
return|return
name|sparkCounters
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkStatistics
name|getSparkStatistics
parameter_list|()
block|{
name|SparkStatisticsBuilder
name|sparkStatisticsBuilder
init|=
operator|new
name|SparkStatisticsBuilder
argument_list|()
decl_stmt|;
comment|// add Hive operator level statistics.
name|sparkStatisticsBuilder
operator|.
name|add
argument_list|(
name|sparkCounters
argument_list|)
expr_stmt|;
comment|// add spark job metrics.
name|String
name|jobIdentifier
init|=
literal|"Spark Job["
operator|+
name|jobId
operator|+
literal|"] Metrics"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|TaskMetrics
argument_list|>
argument_list|>
name|jobMetric
init|=
name|jobMetricsListener
operator|.
name|getJobMetric
argument_list|(
name|jobId
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobMetric
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MetricsCollection
name|metricsCollection
init|=
operator|new
name|MetricsCollection
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|stageIds
init|=
name|jobMetric
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|stageId
range|:
name|stageIds
control|)
block|{
name|List
argument_list|<
name|TaskMetrics
argument_list|>
name|taskMetrics
init|=
name|jobMetric
operator|.
name|get
argument_list|(
name|stageId
argument_list|)
decl_stmt|;
for|for
control|(
name|TaskMetrics
name|taskMetric
range|:
name|taskMetrics
control|)
block|{
name|Metrics
name|metrics
init|=
operator|new
name|Metrics
argument_list|(
name|taskMetric
argument_list|)
decl_stmt|;
name|metricsCollection
operator|.
name|addMetrics
argument_list|(
name|jobId
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|stageId
argument_list|)
argument_list|,
literal|0
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
block|}
block|}
name|SparkJobUtils
name|sparkJobUtils
init|=
operator|new
name|SparkJobUtils
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|flatJobMetric
init|=
name|sparkJobUtils
operator|.
name|collectMetrics
argument_list|(
name|metricsCollection
operator|.
name|getAllMetrics
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|flatJobMetric
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sparkStatisticsBuilder
operator|.
name|add
argument_list|(
name|jobIdentifier
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sparkStatisticsBuilder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
name|jobMetricsListener
operator|.
name|cleanup
argument_list|(
name|jobId
argument_list|)
expr_stmt|;
if|if
condition|(
name|cachedRDDIds
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Integer
name|cachedRDDId
range|:
name|cachedRDDIds
control|)
block|{
name|sparkContext
operator|.
name|sc
argument_list|()
operator|.
name|unpersistRDD
argument_list|(
name|cachedRDDId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|SparkJobInfo
name|getJobInfo
parameter_list|()
block|{
return|return
name|sparkContext
operator|.
name|statusTracker
argument_list|()
operator|.
name|getJobInfo
argument_list|(
name|jobId
argument_list|)
return|;
block|}
specifier|private
name|SparkStageInfo
name|getStageInfo
parameter_list|(
name|int
name|stageId
parameter_list|)
block|{
return|return
name|sparkContext
operator|.
name|statusTracker
argument_list|()
operator|.
name|getStageInfo
argument_list|(
name|stageId
argument_list|)
return|;
block|}
block|}
end_class

end_unit

