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
name|List
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
name|counter
operator|.
name|SparkCounters
import|;
end_import

begin_comment
comment|/**  * A handle to a submitted job. Allows for monitoring and controlling of the running remote job.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|JobHandle
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
extends|extends
name|Future
argument_list|<
name|T
argument_list|>
block|{
comment|/**    * The client job ID. This is unrelated to any Spark jobs that might be triggered by the    * submitted job.    */
name|String
name|getClientJobId
parameter_list|()
function_decl|;
comment|/**    * A collection of metrics collected from the Spark jobs triggered by this job.    *    * To collect job metrics on the client, Spark jobs must be registered with JobContext::monitor()    * on the remote end.    */
name|MetricsCollection
name|getMetrics
parameter_list|()
function_decl|;
comment|/**    * Get corresponding spark job IDs for this job.    */
name|List
argument_list|<
name|Integer
argument_list|>
name|getSparkJobIds
parameter_list|()
function_decl|;
comment|/**    * Get the SparkCounters for this job.    */
name|SparkCounters
name|getSparkCounters
parameter_list|()
function_decl|;
comment|/**    * Return the current state of the job.    */
name|State
name|getState
parameter_list|()
function_decl|;
comment|/**    * Return the error if the job has failed.    */
name|Throwable
name|getError
parameter_list|()
function_decl|;
comment|/**    * The current state of the submitted job.    */
specifier|static
enum|enum
name|State
block|{
name|SENT
block|,
name|QUEUED
block|,
name|STARTED
block|,
name|CANCELLED
block|,
name|FAILED
block|,
name|SUCCEEDED
block|;   }
comment|/**    * A listener for monitoring the state of the job in the remote context. Callbacks are called    * when the corresponding state change occurs.    */
specifier|static
interface|interface
name|Listener
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
block|{
name|void
name|onJobQueued
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|)
function_decl|;
name|void
name|onJobStarted
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|)
function_decl|;
name|void
name|onJobCancelled
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|)
function_decl|;
name|void
name|onJobFailed
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|,
name|Throwable
name|cause
parameter_list|)
function_decl|;
name|void
name|onJobSucceeded
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|,
name|T
name|result
parameter_list|)
function_decl|;
comment|/**      * Called when a monitored Spark job is started on the remote context. This callback      * does not indicate a state change in the client job's status.      */
name|void
name|onSparkJobStarted
parameter_list|(
name|JobHandle
argument_list|<
name|T
argument_list|>
name|job
parameter_list|,
name|int
name|sparkJobId
parameter_list|)
function_decl|;
block|}
block|}
end_interface

end_unit

