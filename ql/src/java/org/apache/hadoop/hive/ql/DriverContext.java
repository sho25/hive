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
name|FileSinkOperator
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
name|ql
operator|.
name|exec
operator|.
name|NodeUtils
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
name|ql
operator|.
name|exec
operator|.
name|NodeUtils
operator|.
name|Function
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
name|ql
operator|.
name|exec
operator|.
name|Operator
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
name|ql
operator|.
name|exec
operator|.
name|StatsTask
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
name|TaskRunner
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
name|ql
operator|.
name|exec
operator|.
name|mr
operator|.
name|MapRedTask
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|plan
operator|.
name|MapWork
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
name|ql
operator|.
name|plan
operator|.
name|ReduceWork
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ConcurrentLinkedQueue
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
name|LinkedBlockingQueue
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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

begin_comment
comment|/**  * DriverContext.  *  */
end_comment

begin_class
specifier|public
class|class
name|DriverContext
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
name|Driver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SessionState
operator|.
name|LogHelper
name|console
init|=
operator|new
name|SessionState
operator|.
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_TIME
init|=
literal|2000
decl_stmt|;
specifier|private
name|Queue
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|runnable
decl_stmt|;
specifier|private
name|Queue
argument_list|<
name|TaskRunner
argument_list|>
name|running
decl_stmt|;
comment|// how many jobs have been started
specifier|private
name|int
name|curJobNo
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|boolean
name|shutdown
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|StatsTask
argument_list|>
name|statsTasks
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|StatsTask
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|DriverContext
parameter_list|()
block|{   }
specifier|public
name|DriverContext
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|runnable
operator|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|running
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|TaskRunner
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isShutdown
parameter_list|()
block|{
return|return
name|shutdown
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
operator|!
name|shutdown
operator|&&
operator|(
operator|!
name|running
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|runnable
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|remove
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
name|runnable
operator|.
name|remove
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|launching
parameter_list|(
name|TaskRunner
name|runner
parameter_list|)
throws|throws
name|HiveException
block|{
name|checkShutdown
argument_list|()
expr_stmt|;
name|running
operator|.
name|add
argument_list|(
name|runner
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getRunnable
parameter_list|(
name|int
name|maxthreads
parameter_list|)
throws|throws
name|HiveException
block|{
name|checkShutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|runnable
operator|.
name|peek
argument_list|()
operator|!=
literal|null
operator|&&
name|running
operator|.
name|size
argument_list|()
operator|<
name|maxthreads
condition|)
block|{
return|return
name|runnable
operator|.
name|remove
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Polls running tasks to see if a task has ended.    *    * @return The result object for any completed/failed task    */
specifier|public
specifier|synchronized
name|TaskRunner
name|pollFinished
parameter_list|()
throws|throws
name|InterruptedException
block|{
while|while
condition|(
operator|!
name|shutdown
condition|)
block|{
name|Iterator
argument_list|<
name|TaskRunner
argument_list|>
name|it
init|=
name|running
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TaskRunner
name|runner
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|runner
operator|!=
literal|null
operator|&&
operator|!
name|runner
operator|.
name|isRunning
argument_list|()
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
return|return
name|runner
return|;
block|}
block|}
name|wait
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|checkShutdown
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
name|shutdown
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"FAILED: Operation cancelled"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Cleans up remaining tasks in case of failure    */
specifier|public
specifier|synchronized
name|void
name|shutdown
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Shutting down query "
operator|+
name|ctx
operator|.
name|getCmd
argument_list|()
argument_list|)
expr_stmt|;
name|shutdown
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|TaskRunner
name|runner
range|:
name|running
control|)
block|{
if|if
condition|(
name|runner
operator|.
name|isRunning
argument_list|()
condition|)
block|{
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
name|runner
operator|.
name|getTask
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Shutting down task : "
operator|+
name|task
argument_list|)
expr_stmt|;
try|try
block|{
name|task
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Exception on shutting down task "
operator|+
name|task
operator|.
name|getId
argument_list|()
operator|+
literal|": "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
name|Thread
name|thread
init|=
name|runner
operator|.
name|getRunner
argument_list|()
decl_stmt|;
if|if
condition|(
name|thread
operator|!=
literal|null
condition|)
block|{
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|running
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Checks if a task can be launched.    *    * @param tsk    *          the task to be checked    * @return true if the task is launchable, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isLaunchable
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
parameter_list|)
block|{
comment|// A launchable task is one that hasn't been queued, hasn't been
comment|// initialized, and is runnable.
return|return
operator|!
name|tsk
operator|.
name|getQueued
argument_list|()
operator|&&
operator|!
name|tsk
operator|.
name|getInitialized
argument_list|()
operator|&&
name|tsk
operator|.
name|isRunnable
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|addToRunnable
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|runnable
operator|.
name|contains
argument_list|(
name|tsk
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|checkShutdown
argument_list|()
expr_stmt|;
name|runnable
operator|.
name|add
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
name|tsk
operator|.
name|setQueued
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|int
name|getCurJobNo
parameter_list|()
block|{
return|return
name|curJobNo
return|;
block|}
specifier|public
name|Context
name|getCtx
parameter_list|()
block|{
return|return
name|ctx
return|;
block|}
specifier|public
name|void
name|incCurJobNo
parameter_list|(
name|int
name|amount
parameter_list|)
block|{
name|this
operator|.
name|curJobNo
operator|=
name|this
operator|.
name|curJobNo
operator|+
name|amount
expr_stmt|;
block|}
specifier|public
name|void
name|prepare
parameter_list|(
name|QueryPlan
name|plan
parameter_list|)
block|{
comment|// extract stats keys from StatsTask
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
init|=
name|plan
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|NodeUtils
operator|.
name|iterateTask
argument_list|(
name|rootTasks
argument_list|,
name|StatsTask
operator|.
name|class
argument_list|,
operator|new
name|Function
argument_list|<
name|StatsTask
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|StatsTask
name|statsTask
parameter_list|)
block|{
name|statsTasks
operator|.
name|put
argument_list|(
name|statsTask
operator|.
name|getWork
argument_list|()
operator|.
name|getAggKey
argument_list|()
argument_list|,
name|statsTask
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|prepare
parameter_list|(
name|TaskRunner
name|runner
parameter_list|)
block|{   }
specifier|public
name|void
name|finished
parameter_list|(
name|TaskRunner
name|runner
parameter_list|)
block|{
if|if
condition|(
name|statsTasks
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
operator|(
name|runner
operator|.
name|getTask
argument_list|()
operator|instanceof
name|MapRedTask
operator|)
condition|)
block|{
return|return;
block|}
name|MapRedTask
name|mapredTask
init|=
operator|(
name|MapRedTask
operator|)
name|runner
operator|.
name|getTask
argument_list|()
decl_stmt|;
name|MapWork
name|mapWork
init|=
name|mapredTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|ReduceWork
name|reduceWork
init|=
name|mapredTask
operator|.
name|getWork
argument_list|()
operator|.
name|getReduceWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|>
name|operators
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|>
argument_list|(
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduceWork
operator|!=
literal|null
condition|)
block|{
name|operators
operator|.
name|add
argument_list|(
name|reduceWork
operator|.
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|statKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|NodeUtils
operator|.
name|iterate
argument_list|(
name|operators
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|,
operator|new
name|Function
argument_list|<
name|FileSinkOperator
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|FileSinkOperator
name|fsOp
parameter_list|)
block|{
if|if
condition|(
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|isGatherStats
argument_list|()
condition|)
block|{
name|statKeys
operator|.
name|add
argument_list|(
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getStatsAggPrefix
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|statKey
range|:
name|statKeys
control|)
block|{
name|statsTasks
operator|.
name|get
argument_list|(
name|statKey
argument_list|)
operator|.
name|getWork
argument_list|()
operator|.
name|setSourceTask
argument_list|(
name|mapredTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

