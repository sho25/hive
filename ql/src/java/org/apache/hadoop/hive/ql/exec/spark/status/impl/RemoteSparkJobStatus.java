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
name|metadata
operator|.
name|HiveException
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
name|Job
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
name|JobContext
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
name|JobHandle
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
name|SparkClient
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
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|concurrent
operator|.
name|Future
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
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Used with remove spark client.  */
end_comment

begin_class
specifier|public
class|class
name|RemoteSparkJobStatus
implements|implements
name|SparkJobStatus
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
name|RemoteSparkJobStatus
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SparkClient
name|sparkClient
decl_stmt|;
specifier|private
specifier|final
name|JobHandle
argument_list|<
name|Serializable
argument_list|>
name|jobHandle
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|long
name|sparkClientTimeoutInSeconds
decl_stmt|;
specifier|public
name|RemoteSparkJobStatus
parameter_list|(
name|SparkClient
name|sparkClient
parameter_list|,
name|JobHandle
argument_list|<
name|Serializable
argument_list|>
name|jobHandle
parameter_list|,
name|long
name|timeoutInSeconds
parameter_list|)
block|{
name|this
operator|.
name|sparkClient
operator|=
name|sparkClient
expr_stmt|;
name|this
operator|.
name|jobHandle
operator|=
name|jobHandle
expr_stmt|;
name|this
operator|.
name|sparkClientTimeoutInSeconds
operator|=
name|timeoutInSeconds
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getJobId
parameter_list|()
block|{
return|return
name|jobHandle
operator|.
name|getSparkJobIds
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|jobHandle
operator|.
name|getSparkJobIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|JobExecutionStatus
name|getState
parameter_list|()
throws|throws
name|HiveException
block|{
name|SparkJobInfo
name|sparkJobInfo
init|=
name|getSparkJobInfo
argument_list|()
decl_stmt|;
return|return
name|sparkJobInfo
operator|!=
literal|null
condition|?
name|sparkJobInfo
operator|.
name|status
argument_list|()
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
index|[]
name|getStageIds
parameter_list|()
throws|throws
name|HiveException
block|{
name|SparkJobInfo
name|sparkJobInfo
init|=
name|getSparkJobInfo
argument_list|()
decl_stmt|;
return|return
name|sparkJobInfo
operator|!=
literal|null
condition|?
name|sparkJobInfo
operator|.
name|stageIds
argument_list|()
else|:
operator|new
name|int
index|[
literal|0
index|]
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
throws|throws
name|HiveException
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
name|getSparkStageInfo
argument_list|(
name|stageId
argument_list|)
decl_stmt|;
if|if
condition|(
name|sparkStageInfo
operator|!=
literal|null
operator|&&
name|sparkStageInfo
operator|.
name|name
argument_list|()
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
name|jobHandle
operator|.
name|getSparkCounters
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkStatistics
name|getSparkStatistics
parameter_list|()
block|{
name|MetricsCollection
name|metricsCollection
init|=
name|jobHandle
operator|.
name|getMetrics
argument_list|()
decl_stmt|;
if|if
condition|(
name|metricsCollection
operator|==
literal|null
operator|||
name|getCounter
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
comment|// add spark job metrics.
name|String
name|jobIdentifier
init|=
literal|"Spark Job["
operator|+
name|jobHandle
operator|.
name|getClientJobId
argument_list|()
operator|+
literal|"] Metrics"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|flatJobMetric
init|=
name|extractMetrics
argument_list|(
name|metricsCollection
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
block|{    }
specifier|private
name|SparkJobInfo
name|getSparkJobInfo
parameter_list|()
throws|throws
name|HiveException
block|{
name|Integer
name|sparkJobId
init|=
name|jobHandle
operator|.
name|getSparkJobIds
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|jobHandle
operator|.
name|getSparkJobIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|sparkJobId
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Future
argument_list|<
name|SparkJobInfo
argument_list|>
name|getJobInfo
init|=
name|sparkClient
operator|.
name|run
argument_list|(
operator|new
name|GetJobInfoJob
argument_list|(
name|jobHandle
operator|.
name|getClientJobId
argument_list|()
argument_list|,
name|sparkJobId
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|getJobInfo
operator|.
name|get
argument_list|(
name|sparkClientTimeoutInSeconds
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get job info."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|SparkStageInfo
name|getSparkStageInfo
parameter_list|(
name|int
name|stageId
parameter_list|)
block|{
name|Future
argument_list|<
name|SparkStageInfo
argument_list|>
name|getStageInfo
init|=
name|sparkClient
operator|.
name|run
argument_list|(
operator|new
name|GetStageInfoJob
argument_list|(
name|stageId
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|getStageInfo
operator|.
name|get
argument_list|(
name|sparkClientTimeoutInSeconds
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error getting stage info"
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|JobHandle
operator|.
name|State
name|getRemoteJobState
parameter_list|()
block|{
return|return
name|jobHandle
operator|.
name|getState
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|GetJobInfoJob
implements|implements
name|Job
argument_list|<
name|SparkJobInfo
argument_list|>
block|{
specifier|private
specifier|final
name|String
name|clientJobId
decl_stmt|;
specifier|private
specifier|final
name|int
name|sparkJobId
decl_stmt|;
specifier|private
name|GetJobInfoJob
parameter_list|()
block|{
comment|// For serialization.
name|this
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|GetJobInfoJob
parameter_list|(
name|String
name|clientJobId
parameter_list|,
name|int
name|sparkJobId
parameter_list|)
block|{
name|this
operator|.
name|clientJobId
operator|=
name|clientJobId
expr_stmt|;
name|this
operator|.
name|sparkJobId
operator|=
name|sparkJobId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SparkJobInfo
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|SparkJobInfo
name|jobInfo
init|=
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|statusTracker
argument_list|()
operator|.
name|getJobInfo
argument_list|(
name|sparkJobId
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobInfo
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
argument_list|>
name|list
init|=
name|jc
operator|.
name|getMonitoredJobs
argument_list|()
operator|.
name|get
argument_list|(
name|clientJobId
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|!=
literal|null
operator|&&
name|list
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
name|futureAction
init|=
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|futureAction
operator|.
name|isDone
argument_list|()
condition|)
block|{
name|jobInfo
operator|=
name|getDefaultJobInfo
argument_list|(
name|sparkJobId
argument_list|,
name|JobExecutionStatus
operator|.
name|SUCCEEDED
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|jobInfo
operator|==
literal|null
condition|)
block|{
name|jobInfo
operator|=
name|getDefaultJobInfo
argument_list|(
name|sparkJobId
argument_list|,
name|JobExecutionStatus
operator|.
name|UNKNOWN
argument_list|)
expr_stmt|;
block|}
return|return
name|jobInfo
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|GetStageInfoJob
implements|implements
name|Job
argument_list|<
name|SparkStageInfo
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|stageId
decl_stmt|;
specifier|private
name|GetStageInfoJob
parameter_list|()
block|{
comment|// For serialization.
name|this
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|GetStageInfoJob
parameter_list|(
name|int
name|stageId
parameter_list|)
block|{
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
name|SparkStageInfo
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|jc
operator|.
name|sc
argument_list|()
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
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|extractMetrics
parameter_list|(
name|MetricsCollection
name|metricsCollection
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|results
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Metrics
name|allMetrics
init|=
name|metricsCollection
operator|.
name|getAllMetrics
argument_list|()
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"EexcutorDeserializeTime"
argument_list|,
name|allMetrics
operator|.
name|executorDeserializeTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"ExecutorRunTime"
argument_list|,
name|allMetrics
operator|.
name|executorRunTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"ResultSize"
argument_list|,
name|allMetrics
operator|.
name|resultSize
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"JvmGCTime"
argument_list|,
name|allMetrics
operator|.
name|jvmGCTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"ResultSerializationTime"
argument_list|,
name|allMetrics
operator|.
name|resultSerializationTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"MemoryBytesSpilled"
argument_list|,
name|allMetrics
operator|.
name|memoryBytesSpilled
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"DiskBytesSpilled"
argument_list|,
name|allMetrics
operator|.
name|diskBytesSpilled
argument_list|)
expr_stmt|;
if|if
condition|(
name|allMetrics
operator|.
name|inputMetrics
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
literal|"BytesRead"
argument_list|,
name|allMetrics
operator|.
name|inputMetrics
operator|.
name|bytesRead
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allMetrics
operator|.
name|shuffleReadMetrics
operator|!=
literal|null
condition|)
block|{
name|ShuffleReadMetrics
name|shuffleReadMetrics
init|=
name|allMetrics
operator|.
name|shuffleReadMetrics
decl_stmt|;
name|long
name|rbf
init|=
name|shuffleReadMetrics
operator|.
name|remoteBlocksFetched
decl_stmt|;
name|long
name|lbf
init|=
name|shuffleReadMetrics
operator|.
name|localBlocksFetched
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"RemoteBlocksFetched"
argument_list|,
name|rbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"LocalBlocksFetched"
argument_list|,
name|lbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"TotalBlocksFetched"
argument_list|,
name|lbf
operator|+
name|rbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"FetchWaitTime"
argument_list|,
name|shuffleReadMetrics
operator|.
name|fetchWaitTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"RemoteBytesRead"
argument_list|,
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
literal|"ShuffleBytesWritten"
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleBytesWritten
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
literal|"ShuffleWriteTime"
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleWriteTime
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
specifier|private
specifier|static
name|SparkJobInfo
name|getDefaultJobInfo
parameter_list|(
specifier|final
name|Integer
name|jobId
parameter_list|,
specifier|final
name|JobExecutionStatus
name|status
parameter_list|)
block|{
return|return
operator|new
name|SparkJobInfo
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|jobId
parameter_list|()
block|{
return|return
name|jobId
operator|==
literal|null
condition|?
operator|-
literal|1
else|:
name|jobId
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
index|[]
name|stageIds
parameter_list|()
block|{
return|return
operator|new
name|int
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|JobExecutionStatus
name|status
parameter_list|()
block|{
return|return
name|status
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

