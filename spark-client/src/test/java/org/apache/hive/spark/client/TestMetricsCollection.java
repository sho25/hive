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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
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
name|org
operator|.
name|junit
operator|.
name|Test
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
name|ImmutableSet
import|;
end_import

begin_class
specifier|public
class|class
name|TestMetricsCollection
block|{
annotation|@
name|Test
specifier|public
name|void
name|testMetricsAggregation
parameter_list|()
block|{
name|MetricsCollection
name|collection
init|=
operator|new
name|MetricsCollection
argument_list|()
decl_stmt|;
comment|// 2 jobs, 2 stages per job, 2 tasks per stage.
for|for
control|(
name|int
name|i
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
control|)
block|{
for|for
control|(
name|int
name|j
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
control|)
block|{
for|for
control|(
name|long
name|k
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|1L
argument_list|,
literal|2L
argument_list|)
control|)
block|{
name|collection
operator|.
name|addMetrics
argument_list|(
name|i
argument_list|,
name|j
argument_list|,
name|k
argument_list|,
name|makeMetrics
argument_list|(
name|i
argument_list|,
name|j
argument_list|,
name|k
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertEquals
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|,
name|collection
operator|.
name|getJobIds
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|,
name|collection
operator|.
name|getStageIds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|1L
argument_list|,
literal|2L
argument_list|)
argument_list|,
name|collection
operator|.
name|getTaskIds
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Metrics
name|task112
init|=
name|collection
operator|.
name|getTaskMetrics
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|checkMetrics
argument_list|(
name|task112
argument_list|,
name|taskValue
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Metrics
name|stage21
init|=
name|collection
operator|.
name|getStageMetrics
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|checkMetrics
argument_list|(
name|stage21
argument_list|,
name|stageValue
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Metrics
name|job1
init|=
name|collection
operator|.
name|getJobMetrics
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|checkMetrics
argument_list|(
name|job1
argument_list|,
name|jobValue
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Metrics
name|global
init|=
name|collection
operator|.
name|getAllMetrics
argument_list|()
decl_stmt|;
name|checkMetrics
argument_list|(
name|global
argument_list|,
name|globalValue
argument_list|(
literal|2
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOptionalMetrics
parameter_list|()
block|{
name|long
name|value
init|=
name|taskValue
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|1L
argument_list|)
decl_stmt|;
name|Metrics
name|metrics
init|=
operator|new
name|Metrics
argument_list|(
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|MetricsCollection
name|collection
init|=
operator|new
name|MetricsCollection
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
control|)
block|{
name|collection
operator|.
name|addMetrics
argument_list|(
name|i
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
block|}
name|Metrics
name|global
init|=
name|collection
operator|.
name|getAllMetrics
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|global
operator|.
name|inputMetrics
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|global
operator|.
name|shuffleReadMetrics
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|global
operator|.
name|shuffleWriteMetrics
argument_list|)
expr_stmt|;
name|collection
operator|.
name|addMetrics
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|makeMetrics
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Metrics
name|global2
init|=
name|collection
operator|.
name|getAllMetrics
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|global2
operator|.
name|inputMetrics
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|taskValue
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|,
name|global2
operator|.
name|inputMetrics
operator|.
name|bytesRead
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|global2
operator|.
name|shuffleReadMetrics
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|global2
operator|.
name|shuffleWriteMetrics
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInputReadMethodAggregation
parameter_list|()
block|{
name|MetricsCollection
name|collection
init|=
operator|new
name|MetricsCollection
argument_list|()
decl_stmt|;
name|long
name|value
init|=
name|taskValue
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|Metrics
name|metrics1
init|=
operator|new
name|Metrics
argument_list|(
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
operator|new
name|InputMetrics
argument_list|(
name|DataReadMethod
operator|.
name|Memory
argument_list|,
name|value
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Metrics
name|metrics2
init|=
operator|new
name|Metrics
argument_list|(
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
operator|new
name|InputMetrics
argument_list|(
name|DataReadMethod
operator|.
name|Disk
argument_list|,
name|value
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|collection
operator|.
name|addMetrics
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|metrics1
argument_list|)
expr_stmt|;
name|collection
operator|.
name|addMetrics
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|metrics2
argument_list|)
expr_stmt|;
name|Metrics
name|global
init|=
name|collection
operator|.
name|getAllMetrics
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|global
operator|.
name|inputMetrics
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataReadMethod
operator|.
name|Multiple
argument_list|,
name|global
operator|.
name|inputMetrics
operator|.
name|readMethod
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Metrics
name|makeMetrics
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|,
name|long
name|taskId
parameter_list|)
block|{
name|long
name|value
init|=
literal|1000000
operator|*
name|jobId
operator|+
literal|1000
operator|*
name|stageId
operator|+
name|taskId
decl_stmt|;
return|return
operator|new
name|Metrics
argument_list|(
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|,
operator|new
name|InputMetrics
argument_list|(
name|DataReadMethod
operator|.
name|Memory
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ShuffleReadMetrics
argument_list|(
operator|(
name|int
operator|)
name|value
argument_list|,
operator|(
name|int
operator|)
name|value
argument_list|,
name|value
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ShuffleWriteMetrics
argument_list|(
name|value
argument_list|,
name|value
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * The metric values will all be the same. This makes it easy to calculate the aggregated values    * of jobs and stages without fancy math.    */
specifier|private
name|long
name|taskValue
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|,
name|long
name|taskId
parameter_list|)
block|{
return|return
literal|1000000
operator|*
name|jobId
operator|+
literal|1000
operator|*
name|stageId
operator|+
name|taskId
return|;
block|}
specifier|private
name|long
name|stageValue
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageId
parameter_list|,
name|int
name|taskCount
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|taskCount
condition|;
name|i
operator|++
control|)
block|{
name|value
operator|+=
name|taskValue
argument_list|(
name|jobId
argument_list|,
name|stageId
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
specifier|private
name|long
name|jobValue
parameter_list|(
name|int
name|jobId
parameter_list|,
name|int
name|stageCount
parameter_list|,
name|int
name|tasksPerStage
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|stageCount
condition|;
name|i
operator|++
control|)
block|{
name|value
operator|+=
name|stageValue
argument_list|(
name|jobId
argument_list|,
name|i
argument_list|,
name|tasksPerStage
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
specifier|private
name|long
name|globalValue
parameter_list|(
name|int
name|jobCount
parameter_list|,
name|int
name|stagesPerJob
parameter_list|,
name|int
name|tasksPerStage
parameter_list|)
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|jobCount
condition|;
name|i
operator|++
control|)
block|{
name|value
operator|+=
name|jobValue
argument_list|(
name|i
argument_list|,
name|stagesPerJob
argument_list|,
name|tasksPerStage
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
specifier|private
name|void
name|checkMetrics
parameter_list|(
name|Metrics
name|metrics
parameter_list|,
name|long
name|expected
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|executorDeserializeTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|executorRunTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|resultSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|jvmGCTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|resultSerializationTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|memoryBytesSpilled
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|diskBytesSpilled
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataReadMethod
operator|.
name|Memory
argument_list|,
name|metrics
operator|.
name|inputMetrics
operator|.
name|readMethod
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|inputMetrics
operator|.
name|bytesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
operator|.
name|remoteBlocksFetched
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
operator|.
name|localBlocksFetched
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
operator|.
name|fetchWaitTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleBytesWritten
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|metrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleWriteTime
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

