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
name|LinkedHashMap
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
name|SparkStatisticGroup
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
name|SparkStatisticsNames
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

begin_class
specifier|public
specifier|final
class|class
name|SparkMetricsUtils
block|{
specifier|private
name|SparkMetricsUtils
parameter_list|()
block|{}
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|collectMetrics
parameter_list|(
name|Metrics
name|allMetrics
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
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|TASK_DURATION_TIME
argument_list|,
name|allMetrics
operator|.
name|taskDurationTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|EXECUTOR_CPU_TIME
argument_list|,
name|allMetrics
operator|.
name|executorCpuTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|EXECUTOR_RUN_TIME
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
name|SparkStatisticsNames
operator|.
name|JVM_GC_TIME
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
name|SparkStatisticsNames
operator|.
name|MEMORY_BYTES_SPILLED
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
name|SparkStatisticsNames
operator|.
name|DISK_BYTES_SPILLED
argument_list|,
name|allMetrics
operator|.
name|diskBytesSpilled
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|EXECUTOR_DESERIALIZE_TIME
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
name|SparkStatisticsNames
operator|.
name|EXECUTOR_DESERIALIZE_CPU_TIME
argument_list|,
name|allMetrics
operator|.
name|executorDeserializeCpuTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|RESULT_SIZE
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
name|SparkStatisticsNames
operator|.
name|RESULT_SERIALIZATION_TIME
argument_list|,
name|allMetrics
operator|.
name|resultSerializationTime
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
name|SparkStatisticsNames
operator|.
name|BYTES_READ
argument_list|,
name|allMetrics
operator|.
name|inputMetrics
operator|.
name|bytesRead
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|RECORDS_READ
argument_list|,
name|allMetrics
operator|.
name|inputMetrics
operator|.
name|recordsRead
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
name|SparkStatisticsNames
operator|.
name|SHUFFLE_TOTAL_BYTES_READ
argument_list|,
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
operator|+
name|shuffleReadMetrics
operator|.
name|localBytesRead
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_REMOTE_BYTES_READ
argument_list|,
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_LOCAL_BYTES_READ
argument_list|,
name|shuffleReadMetrics
operator|.
name|localBytesRead
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_REMOTE_BYTES_READ_TO_DISK
argument_list|,
name|shuffleReadMetrics
operator|.
name|remoteBytesReadToDisk
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_RECORDS_READ
argument_list|,
name|shuffleReadMetrics
operator|.
name|recordsRead
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_TOTAL_BLOCKS_FETCHED
argument_list|,
name|rbf
operator|+
name|lbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_REMOTE_BLOCKS_FETCHED
argument_list|,
name|rbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_LOCAL_BLOCKS_FETCHED
argument_list|,
name|lbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_FETCH_WAIT_TIME
argument_list|,
name|shuffleReadMetrics
operator|.
name|fetchWaitTime
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
name|SparkStatisticsNames
operator|.
name|SHUFFLE_BYTES_WRITTEN
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
name|SparkStatisticsNames
operator|.
name|SHUFFLE_RECORDS_WRITTEN
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleRecordsWritten
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|SHUFFLE_WRITE_TIME
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleWriteTime
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allMetrics
operator|.
name|outputMetrics
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|BYTES_WRITTEN
argument_list|,
name|allMetrics
operator|.
name|outputMetrics
operator|.
name|bytesWritten
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SparkStatisticsNames
operator|.
name|RECORDS_WRITTEN
argument_list|,
name|allMetrics
operator|.
name|outputMetrics
operator|.
name|recordsWritten
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
specifier|public
specifier|static
name|long
name|getSparkStatisticAsLong
parameter_list|(
name|SparkStatisticGroup
name|group
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|group
operator|.
name|getSparkStatistic
argument_list|(
name|name
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

