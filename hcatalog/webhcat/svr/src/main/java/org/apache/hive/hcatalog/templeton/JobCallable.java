begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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

begin_class
specifier|public
specifier|abstract
class|class
name|JobCallable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Callable
argument_list|<
name|T
argument_list|>
block|{
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
name|JobCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|public
enum|enum
name|JobState
block|{
name|STARTED
block|,
name|FAILED
block|,
name|COMPLETED
block|}
comment|/*    * Job state of job request. Changes to the state are synchronized using    * setStateAndResult. This is required due to two different threads,    * main thread and job execute thread, tries to change state and organize    * clean up tasks.    */
specifier|private
name|JobState
name|jobState
init|=
name|JobState
operator|.
name|STARTED
decl_stmt|;
comment|/*    * Result of JobCallable task after successful task completion. This is    * expected to be set by the thread which executes JobCallable task.    */
specifier|public
name|T
name|returnResult
init|=
literal|null
decl_stmt|;
comment|/*    * Sets the job state to FAILED. Returns true if FAILED status is set.    * Otherwise, it returns false.    */
specifier|public
name|boolean
name|setJobStateFailed
parameter_list|()
block|{
return|return
name|setStateAndResult
argument_list|(
name|JobState
operator|.
name|FAILED
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/*    * Sets the job state to COMPLETED and also sets the results value. Returns true    * if COMPLETED status is set. Otherwise, it returns false.    */
specifier|public
name|boolean
name|setJobStateCompleted
parameter_list|(
name|T
name|result
parameter_list|)
block|{
return|return
name|setStateAndResult
argument_list|(
name|JobState
operator|.
name|COMPLETED
argument_list|,
name|result
argument_list|)
return|;
block|}
comment|/*    * Sets the job state and result. Returns true if status and result are set.    * Otherwise, it returns false.    */
specifier|private
specifier|synchronized
name|boolean
name|setStateAndResult
parameter_list|(
name|JobState
name|jobState
parameter_list|,
name|T
name|result
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|jobState
operator|==
name|JobState
operator|.
name|STARTED
condition|)
block|{
name|this
operator|.
name|jobState
operator|=
name|jobState
expr_stmt|;
name|this
operator|.
name|returnResult
operator|=
name|result
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to set job state to "
operator|+
name|jobState
operator|+
literal|" due to job state "
operator|+
name|this
operator|.
name|jobState
operator|+
literal|". Expected state is "
operator|+
name|JobState
operator|.
name|STARTED
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/*    * Executes the callable task with help of execute() call and gets the result    * of the task. It also sets job status as COMPLETED if state is not already    * set to FAILED and returns result to future.    */
specifier|public
name|T
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|/*      * Don't catch any execution exceptions here and let the caller catch it.      */
name|T
name|result
init|=
name|this
operator|.
name|execute
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|setJobStateCompleted
argument_list|(
name|result
argument_list|)
condition|)
block|{
comment|/*       * Failed to set job status as COMPLETED which mean the main thread would have       * exited and not waiting for the result. Call cleanup() to execute any cleanup.       */
name|cleanup
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|this
operator|.
name|returnResult
return|;
block|}
comment|/*    * Abstract method to be overridden for task execution.    */
specifier|public
specifier|abstract
name|T
name|execute
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/*    * Cleanup method called to run cleanup tasks if job state is FAILED. By default,    * no cleanup is provided.    */
specifier|public
name|void
name|cleanup
parameter_list|()
block|{}
block|}
end_class

end_unit

