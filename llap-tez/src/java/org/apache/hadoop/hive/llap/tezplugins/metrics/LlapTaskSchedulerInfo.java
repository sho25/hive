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
name|llap
operator|.
name|tezplugins
operator|.
name|metrics
package|;
end_package

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
comment|/**  * Metrics information for llap task scheduler.  */
end_comment

begin_enum
specifier|public
enum|enum
name|LlapTaskSchedulerInfo
implements|implements
name|MetricsInfo
block|{
name|SchedulerMetrics
argument_list|(
literal|"Llap task scheduler related metrics"
argument_list|)
block|,
name|SchedulerClusterNodeCount
argument_list|(
literal|"Number of nodes in the cluster"
argument_list|)
block|,
name|SchedulerExecutorsPerInstance
argument_list|(
literal|"Total number of executor threads per node"
argument_list|)
block|,
name|SchedulerMemoryPerInstance
argument_list|(
literal|"Total memory for executors per node in bytes"
argument_list|)
block|,
name|SchedulerCpuCoresPerInstance
argument_list|(
literal|"Total CPU vCores per node"
argument_list|)
block|,
name|SchedulerDisabledNodeCount
argument_list|(
literal|"Number of nodes disabled temporarily"
argument_list|)
block|,
name|SchedulerPendingTaskCount
argument_list|(
literal|"Number of pending tasks"
argument_list|)
block|,
name|SchedulerSchedulableTaskCount
argument_list|(
literal|"Current slots available for scheduling tasks"
argument_list|)
block|,
name|SchedulerSuccessfulTaskCount
argument_list|(
literal|"Total number of successful tasks"
argument_list|)
block|,
name|SchedulerRunningTaskCount
argument_list|(
literal|"Total number of running tasks"
argument_list|)
block|,
name|SchedulerPendingPreemptionTaskCount
argument_list|(
literal|"Total number of tasks pending for pre-emption"
argument_list|)
block|,
name|SchedulerPreemptedTaskCount
argument_list|(
literal|"Total number of tasks pre-empted"
argument_list|)
block|,
name|SchedulerCompletedDagCount
argument_list|(
literal|"Number of DAGs completed"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|desc
decl_stmt|;
name|LlapTaskSchedulerInfo
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
name|Objects
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

