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
name|CopyOnWriteArrayList
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
name|ExecutionException
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
name|TimeoutException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Promise
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

begin_class
class|class
name|JobHandleImpl
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
implements|implements
name|JobHandle
argument_list|<
name|T
argument_list|>
block|{
specifier|private
specifier|final
name|AtomicBoolean
name|cancelled
decl_stmt|;
specifier|private
specifier|final
name|SparkClientImpl
name|client
decl_stmt|;
specifier|private
specifier|final
name|String
name|jobId
decl_stmt|;
specifier|private
specifier|final
name|MetricsCollection
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|Promise
argument_list|<
name|T
argument_list|>
name|promise
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|sparkJobIds
decl_stmt|;
specifier|private
specifier|volatile
name|SparkCounters
name|sparkCounters
decl_stmt|;
name|JobHandleImpl
parameter_list|(
name|SparkClientImpl
name|client
parameter_list|,
name|Promise
argument_list|<
name|T
argument_list|>
name|promise
parameter_list|,
name|String
name|jobId
parameter_list|)
block|{
name|this
operator|.
name|cancelled
operator|=
operator|new
name|AtomicBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|promise
operator|=
name|promise
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
operator|new
name|MetricsCollection
argument_list|()
expr_stmt|;
name|this
operator|.
name|sparkJobIds
operator|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|sparkCounters
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Requests a running job to be cancelled. */
annotation|@
name|Override
specifier|public
name|boolean
name|cancel
parameter_list|(
name|boolean
name|mayInterrupt
parameter_list|)
block|{
if|if
condition|(
name|cancelled
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|client
operator|.
name|cancel
argument_list|(
name|jobId
argument_list|)
expr_stmt|;
name|promise
operator|.
name|cancel
argument_list|(
name|mayInterrupt
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|get
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
return|return
name|promise
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|get
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
throws|,
name|TimeoutException
block|{
return|return
name|promise
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCancelled
parameter_list|()
block|{
return|return
name|promise
operator|.
name|isCancelled
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|promise
operator|.
name|isDone
argument_list|()
return|;
block|}
comment|/**    * The client job ID. This is unrelated to any Spark jobs that might be triggered by the    * submitted job.    */
annotation|@
name|Override
specifier|public
name|String
name|getClientJobId
parameter_list|()
block|{
return|return
name|jobId
return|;
block|}
comment|/**    * A collection of metrics collected from the Spark jobs triggered by this job.    *    * To collect job metrics on the client, Spark jobs must be registered with JobContext::monitor()    * on the remote end.    */
annotation|@
name|Override
specifier|public
name|MetricsCollection
name|getMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getSparkJobIds
parameter_list|()
block|{
return|return
name|sparkJobIds
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkCounters
name|getSparkCounters
parameter_list|()
block|{
return|return
name|sparkCounters
return|;
block|}
specifier|public
name|void
name|setSparkCounters
parameter_list|(
name|SparkCounters
name|sparkCounters
parameter_list|)
block|{
name|this
operator|.
name|sparkCounters
operator|=
name|sparkCounters
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|void
name|setSuccess
parameter_list|(
name|Object
name|result
parameter_list|)
block|{
name|promise
operator|.
name|setSuccess
argument_list|(
operator|(
name|T
operator|)
name|result
argument_list|)
expr_stmt|;
block|}
name|void
name|setFailure
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
name|promise
operator|.
name|setFailure
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
comment|/** Last attempt resort at preventing stray jobs from accumulating in SparkClientImpl. */
annotation|@
name|Override
specifier|protected
name|void
name|finalize
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isDone
argument_list|()
condition|)
block|{
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

