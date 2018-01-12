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
name|repl
operator|.
name|bootstrap
operator|.
name|load
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
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|ArrayList
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

begin_comment
comment|/**  * This class will be responsible to track how many tasks have been created,  * organization of tasks such that after the number of tasks for next execution are created  * we create a dependency collection task(DCT) -> another bootstrap task,  * and then add DCT as dependent to all existing tasks that are created so the cycle can continue.  */
end_comment

begin_class
specifier|public
class|class
name|TaskTracker
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TaskTracker
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * used to identify the list of tasks at root level for a given level like table /  db / partition.    * this does not include the task dependency notion of "table tasks< ---- partition task"    */
specifier|private
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|ReplicationState
name|replicationState
init|=
literal|null
decl_stmt|;
comment|// since tasks themselves can be graphs we want to limit the number of created
comment|// tasks including all of dependencies.
specifier|private
name|int
name|numberOfTasks
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxTasksAllowed
decl_stmt|;
specifier|public
name|TaskTracker
parameter_list|(
name|int
name|defaultMaxTasks
parameter_list|)
block|{
name|maxTasksAllowed
operator|=
name|defaultMaxTasks
expr_stmt|;
block|}
specifier|public
name|TaskTracker
parameter_list|(
name|TaskTracker
name|existing
parameter_list|)
block|{
name|maxTasksAllowed
operator|=
name|existing
operator|.
name|maxTasksAllowed
operator|-
name|existing
operator|.
name|numberOfTasks
expr_stmt|;
block|}
comment|/**    * this method is used to identify all the tasks in a graph.    * the graph however might get created in a disjoint fashion, in which case we can just update    * the number of tasks using the "update" method.    */
specifier|public
name|void
name|addTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
name|tasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|visited
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|updateTaskCount
argument_list|(
name|task
argument_list|,
name|visited
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateTaskCount
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|visited
parameter_list|)
block|{
name|numberOfTasks
operator|+=
literal|1
expr_stmt|;
name|visited
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
if|if
condition|(
name|task
operator|.
name|getChildTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|visited
operator|.
name|contains
argument_list|(
name|childTask
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|updateTaskCount
argument_list|(
name|childTask
argument_list|,
name|visited
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|boolean
name|canAddMoreTasks
parameter_list|()
block|{
return|return
name|numberOfTasks
operator|<
name|maxTasksAllowed
return|;
block|}
specifier|public
name|boolean
name|hasTasks
parameter_list|()
block|{
return|return
name|numberOfTasks
operator|!=
literal|0
return|;
block|}
specifier|public
name|void
name|update
parameter_list|(
name|TaskTracker
name|withAnother
parameter_list|)
block|{
name|numberOfTasks
operator|+=
name|withAnother
operator|.
name|numberOfTasks
expr_stmt|;
if|if
condition|(
name|withAnother
operator|.
name|hasReplicationState
argument_list|()
condition|)
block|{
name|this
operator|.
name|replicationState
operator|=
name|withAnother
operator|.
name|replicationState
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setReplicationState
parameter_list|(
name|ReplicationState
name|state
parameter_list|)
block|{
name|this
operator|.
name|replicationState
operator|=
name|state
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasReplicationState
parameter_list|()
block|{
return|return
name|replicationState
operator|!=
literal|null
return|;
block|}
specifier|public
name|ReplicationState
name|replicationState
parameter_list|()
block|{
return|return
name|replicationState
return|;
block|}
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
parameter_list|()
block|{
return|return
name|tasks
return|;
block|}
specifier|public
name|void
name|debugLog
parameter_list|(
name|String
name|forEventType
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"{} event with total / root number of tasks:{}/{}"
argument_list|,
name|forEventType
argument_list|,
name|numberOfTasks
argument_list|,
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|numberOfTasks
parameter_list|()
block|{
return|return
name|numberOfTasks
return|;
block|}
block|}
end_class

end_unit

