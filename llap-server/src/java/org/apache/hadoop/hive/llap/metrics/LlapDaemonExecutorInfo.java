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
comment|/**  * Metrics information for llap daemon container.  */
end_comment

begin_enum
specifier|public
enum|enum
name|LlapDaemonExecutorInfo
implements|implements
name|MetricsInfo
block|{
name|LLAP_DAEMON_EXECUTOR_METRICS
argument_list|(
literal|"Llap daemon cache related metrics"
argument_list|)
block|,
name|EXECUTOR_THREAD_CPU_TIME
argument_list|(
literal|"Cpu time in nanoseconds"
argument_list|)
block|,
name|EXECUTOR_THREAD_USER_TIME
argument_list|(
literal|"User time in nanoseconds"
argument_list|)
block|,
name|EXECUTOR_THREAD_SYSTEM_TIME
argument_list|(
literal|"System time in nanoseconds"
argument_list|)
block|,
name|EXECUTOR_TOTAL_REQUESTS_HANDLED
argument_list|(
literal|"Total number of requests handled by the container"
argument_list|)
block|,
name|EXECUTOR_NUM_QUEUED_REQUESTS
argument_list|(
literal|"Number of requests queued by the container for processing"
argument_list|)
block|,
name|EXECUTOR_TOTAL_SUCCESS
argument_list|(
literal|"Total number of requests handled by the container that succeeded"
argument_list|)
block|,
name|EXECUTOR_TOTAL_EXECUTION_FAILURE
argument_list|(
literal|"Total number of requests handled by the container that failed execution"
argument_list|)
block|,
name|EXECUTOR_TOTAL_INTERRUPTED
argument_list|(
literal|"Total number of requests handled by the container that got interrupted"
argument_list|)
block|,
name|EXECUTOR_TOTAL_ASKED_TO_DIE
argument_list|(
literal|"Total number of requests handled by the container that were asked to die"
argument_list|)
block|;
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

