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
name|llap
operator|.
name|metrics
package|;
end_package

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
name|MoreObjects
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
name|metrics2
operator|.
name|MetricsInfo
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Metrics information for llap daemon container.  */
end_comment

begin_enum
specifier|public
enum|enum
name|LlapDaemonExecutorInfo
implements|implements
name|MetricsInfo
block|{
name|ExecutorMetrics
argument_list|(
literal|"Llap daemon cache related metrics"
argument_list|)
block|,
name|ExecutorMaxFreeSlots
argument_list|(
literal|"Sum of wait queue size and number of executors"
argument_list|)
block|,
name|ExecutorNumExecutorsPerInstance
argument_list|(
literal|"Total number of executor threads per node"
argument_list|)
block|,
name|ExecutorNumExecutorsAvailable
argument_list|(
literal|"Total number of executor threads per node that are free"
argument_list|)
block|,
name|ExecutorNumExecutorsAvailableAverage
argument_list|(
literal|"Total number of executor threads per node that are free averaged over time"
argument_list|)
block|,
name|ExecutorAvailableFreeSlots
argument_list|(
literal|"Number of free slots available"
argument_list|)
block|,
name|ExecutorAvailableFreeSlotsPercent
argument_list|(
literal|"Percent of free slots available"
argument_list|)
block|,
name|ExecutorThreadCPUTime
argument_list|(
literal|"Cpu time in nanoseconds"
argument_list|)
block|,
name|ExecutorMemoryPerInstance
argument_list|(
literal|"Total memory for executors per node in bytes"
argument_list|)
block|,
name|ExecutorCacheMemoryPerInstance
argument_list|(
literal|"Total Cache memory per node in bytes"
argument_list|)
block|,
name|ExecutorJvmMaxMemory
argument_list|(
literal|"Max memory available for JVM in bytes"
argument_list|)
block|,
name|ExecutorWaitQueueSize
argument_list|(
literal|"Size of wait queue per node"
argument_list|)
block|,
name|ExecutorThreadUserTime
argument_list|(
literal|"User time in nanoseconds"
argument_list|)
block|,
name|ExecutorTotalRequestsHandled
argument_list|(
literal|"Total number of requests handled by the container"
argument_list|)
block|,
name|ExecutorNumQueuedRequests
argument_list|(
literal|"Number of requests queued by the container for processing"
argument_list|)
block|,
name|ExecutorNumQueuedRequestsAverage
argument_list|(
literal|"Number of requests queued by the container for processing averaged over time"
argument_list|)
block|,
name|ExecutorNumPreemptableRequests
argument_list|(
literal|"Number of queued requests that are pre-emptable"
argument_list|)
block|,
name|ExecutorTotalRejectedRequests
argument_list|(
literal|"Total number of requests rejected as wait queue being full"
argument_list|)
block|,
name|ExecutorTotalSuccess
argument_list|(
literal|"Total number of requests handled by the container that succeeded"
argument_list|)
block|,
name|ExecutorTotalFailed
argument_list|(
literal|"Total number of requests handled by the container that failed execution"
argument_list|)
block|,
name|ExecutorTotalKilled
argument_list|(
literal|"Total number of requests handled by the container that got interrupted"
argument_list|)
block|,
name|ExecutorTotalAskedToDie
argument_list|(
literal|"Total number of requests handled by the container that were asked to die"
argument_list|)
block|,
name|ExecutorTotalPreemptionTimeToKill
argument_list|(
literal|"Total amount of time taken for killing tasks due to pre-emption"
argument_list|)
block|,
name|ExecutorTotalPreemptionTimeLost
argument_list|(
literal|"Total useful cluster time lost because of pre-emption"
argument_list|)
block|,
name|ExecutorPercentileTimeToKill
argument_list|(
literal|"Percentile time to kill for pre-empted tasks"
argument_list|)
block|,
name|ExecutorPercentileTimeLost
argument_list|(
literal|"Percentile cluster time wasted due to pre-emption"
argument_list|)
block|,
name|ExecutorMaxPreemptionTimeToKill
argument_list|(
literal|"Max time for killing pre-empted task"
argument_list|)
block|,
name|ExecutorMaxPreemptionTimeLost
argument_list|(
literal|"Max cluster time lost due to pre-emption"
argument_list|)
block|,
name|ExecutorTotalEvictedFromWaitQueue
argument_list|(
literal|"Total number of tasks evicted from wait queue because of low priority"
argument_list|)
block|,
name|ExecutorFallOffSuccessTimeLost
argument_list|(
literal|"Total time lost in an executor completing after informing the AM - successful fragments"
argument_list|)
block|,
name|ExecutorFallOffSuccessMaxTimeLost
argument_list|(
literal|"Max value of time lost in an executor completing after informing the AM - successful fragments"
argument_list|)
block|,
name|ExecutorFallOffFailedTimeLost
argument_list|(
literal|"Total time lost in an executor completing after informing the AM - failed fragments"
argument_list|)
block|,
name|ExecutorFallOffFailedMaxTimeLost
argument_list|(
literal|"Max value of time lost in an executor completing after informing the AM - failed fragments"
argument_list|)
block|,
name|ExecutorFallOffKilledTimeLost
argument_list|(
literal|"Total time lost in an executor completing after informing the AM - killed fragments"
argument_list|)
block|,
name|ExecutorFallOffKilledMaxTimeLost
argument_list|(
literal|"Max value of time lost in an executor completing after informing the AM - killed fragments"
argument_list|)
block|,
name|ExecutorFallOffNumCompletedFragments
argument_list|(
literal|"Number of completed fragments w.r.t falloff values"
argument_list|)
block|,
name|AverageQueueTime
argument_list|(
literal|"Average queue time for tasks"
argument_list|)
block|,
name|AverageResponseTime
argument_list|(
literal|"Average response time for successful tasks"
argument_list|)
block|,   ;
specifier|private
specifier|final
name|String
name|desc
decl_stmt|;
name|LlapDaemonExecutorInfo
parameter_list|(
name|String
name|desc
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
name|desc
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|MoreObjects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"description"
argument_list|,
name|desc
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_enum

end_unit

