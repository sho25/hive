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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/*  * Encapsulates statistics about the duration of all reduce tasks  * corresponding to a specific JobId.  * The stats are computed in the HadoopJobExecHelper when the  * job completes and then populated inside the QueryPlan for  * each job, from where it can be later on accessed.  * The reducer statistics consist of minimum/maximum/mean/stdv of the  * run times of all the reduce tasks for a job. All the Run times are  * in Milliseconds.  */
end_comment

begin_class
specifier|public
class|class
name|ReducerTimeStatsPerJob
block|{
comment|// stores the JobId of the job
specifier|private
specifier|final
name|String
name|jobId
decl_stmt|;
comment|// Stores the temporal statistics in milliseconds for reducers
comment|// specific to a Job
specifier|private
specifier|final
name|long
name|minimumTime
decl_stmt|;
specifier|private
specifier|final
name|long
name|maximumTime
decl_stmt|;
specifier|private
specifier|final
name|double
name|meanTime
decl_stmt|;
specifier|private
specifier|final
name|double
name|standardDeviationTime
decl_stmt|;
comment|/*    * Computes the temporal run time statistics of the reducers    * for a specific JobId.    */
specifier|public
name|ReducerTimeStatsPerJob
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|reducersRunTimes
parameter_list|,
name|String
name|jobId
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
comment|// If no Run times present, then set -1, indicating no values
if|if
condition|(
operator|!
name|reducersRunTimes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|minimumTime
init|=
name|reducersRunTimes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|maximumTime
init|=
name|reducersRunTimes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|totalTime
init|=
name|reducersRunTimes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|double
name|standardDeviationTime
init|=
literal|0.0
decl_stmt|;
name|double
name|meanTime
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|reducersRunTimes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|<
name|minimumTime
condition|)
block|{
name|minimumTime
operator|=
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|>
name|maximumTime
condition|)
block|{
name|maximumTime
operator|=
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|+=
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|meanTime
operator|=
operator|(
name|double
operator|)
name|totalTime
operator|/
name|reducersRunTimes
operator|.
name|size
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|reducersRunTimes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|standardDeviationTime
operator|+=
name|Math
operator|.
name|pow
argument_list|(
name|meanTime
operator|-
name|reducersRunTimes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
name|standardDeviationTime
operator|/=
name|reducersRunTimes
operator|.
name|size
argument_list|()
expr_stmt|;
name|standardDeviationTime
operator|=
name|Math
operator|.
name|sqrt
argument_list|(
name|standardDeviationTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|minimumTime
operator|=
name|minimumTime
expr_stmt|;
name|this
operator|.
name|maximumTime
operator|=
name|maximumTime
expr_stmt|;
name|this
operator|.
name|meanTime
operator|=
name|meanTime
expr_stmt|;
name|this
operator|.
name|standardDeviationTime
operator|=
name|standardDeviationTime
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|minimumTime
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|maximumTime
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|meanTime
operator|=
operator|-
literal|1.0
expr_stmt|;
name|this
operator|.
name|standardDeviationTime
operator|=
operator|-
literal|1.0
expr_stmt|;
return|return;
block|}
specifier|public
name|long
name|getMinimumTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|minimumTime
return|;
block|}
specifier|public
name|long
name|getMaximumTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|maximumTime
return|;
block|}
specifier|public
name|double
name|getMeanTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|meanTime
return|;
block|}
specifier|public
name|double
name|getStandardDeviationTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|standardDeviationTime
return|;
block|}
specifier|public
name|String
name|getJobId
parameter_list|()
block|{
return|return
name|this
operator|.
name|jobId
return|;
block|}
block|}
end_class

end_unit

