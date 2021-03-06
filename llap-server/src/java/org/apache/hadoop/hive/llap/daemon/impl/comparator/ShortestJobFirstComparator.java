begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|daemon
operator|.
name|impl
operator|.
name|comparator
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
name|hive
operator|.
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|TaskRunnerCallable
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
import|;
end_import

begin_comment
comment|// if map tasks and reduce tasks are in finishable state then priority is given to the task
end_comment

begin_comment
comment|// that has less number of pending tasks (shortest job)
end_comment

begin_class
specifier|public
class|class
name|ShortestJobFirstComparator
extends|extends
name|LlapQueueComparatorBase
block|{
annotation|@
name|Override
specifier|public
name|int
name|compareInternal
parameter_list|(
name|TaskRunnerCallable
name|o1
parameter_list|,
name|TaskRunnerCallable
name|o2
parameter_list|)
block|{
name|LlapDaemonProtocolProtos
operator|.
name|FragmentRuntimeInfo
name|fri1
init|=
name|o1
operator|.
name|getFragmentRuntimeInfo
argument_list|()
decl_stmt|;
name|LlapDaemonProtocolProtos
operator|.
name|FragmentRuntimeInfo
name|fri2
init|=
name|o2
operator|.
name|getFragmentRuntimeInfo
argument_list|()
decl_stmt|;
comment|// Check if these belong to the same task, and work with withinDagPriority
if|if
condition|(
name|o1
operator|.
name|getQueryId
argument_list|()
operator|.
name|equals
argument_list|(
name|o2
operator|.
name|getQueryId
argument_list|()
argument_list|)
condition|)
block|{
comment|// Same Query
if|if
condition|(
name|fri1
operator|.
name|getWithinDagPriority
argument_list|()
operator|==
name|fri2
operator|.
name|getWithinDagPriority
argument_list|()
condition|)
block|{
comment|// task_attempt within same vertex.
comment|// Choose the attempt that was started earlier
return|return
name|Long
operator|.
name|compare
argument_list|(
name|fri1
operator|.
name|getCurrentAttemptStartTime
argument_list|()
argument_list|,
name|fri2
operator|.
name|getCurrentAttemptStartTime
argument_list|()
argument_list|)
return|;
block|}
comment|// Within dag priority - lower values indicate higher priority.
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|fri1
operator|.
name|getWithinDagPriority
argument_list|()
argument_list|,
name|fri2
operator|.
name|getWithinDagPriority
argument_list|()
argument_list|)
return|;
block|}
comment|// Compute knownPending tasks. selfAndUpstream indicates task counts for current vertex and
comment|// it's parent hierarchy. selfAndUpstreamComplete indicates how many of these have completed.
name|int
name|knownPending1
init|=
name|fri1
operator|.
name|getNumSelfAndUpstreamTasks
argument_list|()
operator|-
name|fri1
operator|.
name|getNumSelfAndUpstreamCompletedTasks
argument_list|()
decl_stmt|;
name|int
name|knownPending2
init|=
name|fri2
operator|.
name|getNumSelfAndUpstreamTasks
argument_list|()
operator|-
name|fri2
operator|.
name|getNumSelfAndUpstreamCompletedTasks
argument_list|()
decl_stmt|;
comment|// longer the wait time for an attempt wrt to its start time, higher the priority it gets
name|long
name|waitTime1
init|=
name|fri1
operator|.
name|getCurrentAttemptStartTime
argument_list|()
operator|-
name|fri1
operator|.
name|getFirstAttemptStartTime
argument_list|()
decl_stmt|;
name|long
name|waitTime2
init|=
name|fri2
operator|.
name|getCurrentAttemptStartTime
argument_list|()
operator|-
name|fri2
operator|.
name|getFirstAttemptStartTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|waitTime1
operator|==
literal|0
operator|||
name|waitTime2
operator|==
literal|0
condition|)
block|{
return|return
name|knownPending1
operator|-
name|knownPending2
return|;
block|}
name|double
name|ratio1
init|=
operator|(
name|double
operator|)
name|knownPending1
operator|/
operator|(
name|double
operator|)
name|waitTime1
decl_stmt|;
name|double
name|ratio2
init|=
operator|(
name|double
operator|)
name|knownPending2
operator|/
operator|(
name|double
operator|)
name|waitTime2
decl_stmt|;
if|if
condition|(
name|ratio1
operator|<
name|ratio2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|ratio1
operator|>
name|ratio2
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

