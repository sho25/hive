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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|Metrics
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
name|conf
operator|.
name|HiveConf
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
name|CompilationOpContext
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
name|DriverContext
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
name|QueryDisplay
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
name|QueryPlan
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
name|QueryState
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
name|history
operator|.
name|HiveHistory
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
name|lib
operator|.
name|Node
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
name|lockmgr
operator|.
name|HiveTxnManager
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
name|Hive
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
name|OperatorDesc
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
name|api
operator|.
name|StageType
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
operator|.
name|LogHelper
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
name|util
operator|.
name|StringUtils
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
name|IOException
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|LinkedList
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
comment|/**  * Task implementation.  **/
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Task
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
implements|implements
name|Serializable
implements|,
name|Node
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|transient
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|taskCounters
decl_stmt|;
specifier|public
specifier|transient
name|TaskHandle
name|taskHandle
decl_stmt|;
specifier|protected
specifier|transient
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
specifier|transient
name|QueryState
name|queryState
decl_stmt|;
specifier|protected
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|protected
specifier|transient
name|QueryPlan
name|queryPlan
decl_stmt|;
specifier|protected
specifier|transient
name|DriverContext
name|driverContext
decl_stmt|;
specifier|protected
specifier|transient
name|boolean
name|clonedConf
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|transient
name|String
name|jobID
decl_stmt|;
specifier|protected
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|backupTask
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|backupChildrenTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|transient
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Task
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|taskTag
decl_stmt|;
specifier|private
name|boolean
name|isLocalMode
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|retryCmdWhenFail
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|NO_TAG
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|COMMON_JOIN
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|HINTED_MAPJOIN
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|HINTED_MAPJOIN_LOCAL
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|CONVERTED_MAPJOIN
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|CONVERTED_MAPJOIN_LOCAL
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|BACKUP_COMMON_JOIN
init|=
literal|6
decl_stmt|;
comment|// The join task is converted to a mapjoin task. This can only happen if
comment|// hive.auto.convert.join.noconditionaltask is set to true. No conditional task was
comment|// created in case the mapjoin failed.
specifier|public
specifier|static
specifier|final
name|int
name|MAPJOIN_ONLY_NOBACKUP
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|CONVERTED_SORTMERGEJOIN
init|=
literal|8
decl_stmt|;
specifier|public
name|QueryDisplay
name|queryDisplay
init|=
literal|null
decl_stmt|;
comment|// Descendants tasks who subscribe feeds from this task
specifier|protected
specifier|transient
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|feedSubscribers
decl_stmt|;
specifier|protected
name|String
name|id
decl_stmt|;
specifier|protected
name|T
name|work
decl_stmt|;
specifier|private
name|TaskState
name|taskState
init|=
name|TaskState
operator|.
name|CREATED
decl_stmt|;
specifier|private
name|String
name|statusMessage
decl_stmt|;
specifier|private
name|String
name|diagnosticMesg
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|fetchSource
decl_stmt|;
specifier|public
name|void
name|setDiagnosticMessage
parameter_list|(
name|String
name|diagnosticMesg
parameter_list|)
block|{
name|this
operator|.
name|diagnosticMesg
operator|=
name|diagnosticMesg
expr_stmt|;
block|}
specifier|public
name|String
name|getDiagnosticsMessage
parameter_list|()
block|{
return|return
name|diagnosticMesg
return|;
block|}
specifier|public
name|void
name|setStatusMessage
parameter_list|(
name|String
name|statusMessage
parameter_list|)
block|{
name|this
operator|.
name|statusMessage
operator|=
name|statusMessage
expr_stmt|;
name|updateStatusInQueryDisplay
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getStatusMessage
parameter_list|()
block|{
return|return
name|statusMessage
return|;
block|}
specifier|public
enum|enum
name|FeedType
block|{
name|DYNAMIC_PARTITIONS
block|,
comment|// list of dynamic partitions
block|}
comment|/**    * Order of the States here is important as the ordinal values are used    * determine the progression of taskState over its lifeCycle which is then    * used to make some decisions in Driver.execute    */
specifier|public
enum|enum
name|TaskState
block|{
comment|// Task state is unkown
name|UNKNOWN
block|,
comment|// Task is just created
name|CREATED
block|,
comment|// Task data structures have been initialized
name|INITIALIZED
block|,
comment|// Task has been queued for execution by the driver
name|QUEUED
block|,
comment|// Task is currently running
name|RUNNING
block|,
comment|// Task has completed
name|FINISHED
block|}
comment|// Bean methods
specifier|protected
name|boolean
name|rootTask
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childTasks
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentTasks
decl_stmt|;
comment|/**    * this can be set by the Task, to provide more info about the failure in TaskResult    * where the Driver can find it.  This is checked if {@link Task#execute(org.apache.hadoop.hive.ql.DriverContext)}    * returns non-0 code.    */
specifier|private
name|Throwable
name|exception
decl_stmt|;
specifier|public
name|Task
parameter_list|()
block|{
name|this
operator|.
name|taskCounters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|taskTag
operator|=
name|Task
operator|.
name|NO_TAG
expr_stmt|;
block|}
specifier|public
name|TaskHandle
name|getTaskHandle
parameter_list|()
block|{
return|return
name|taskHandle
return|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|this
operator|.
name|queryPlan
operator|=
name|queryPlan
expr_stmt|;
name|setInitialized
argument_list|()
expr_stmt|;
name|this
operator|.
name|queryState
operator|=
name|queryState
expr_stmt|;
if|if
condition|(
literal|null
operator|==
name|this
operator|.
name|conf
condition|)
block|{
name|this
operator|.
name|conf
operator|=
name|queryState
operator|.
name|getConf
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|driverContext
operator|=
name|driverContext
expr_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setQueryDisplay
parameter_list|(
name|QueryDisplay
name|queryDisplay
parameter_list|)
block|{
name|this
operator|.
name|queryDisplay
operator|=
name|queryDisplay
expr_stmt|;
block|}
specifier|protected
name|void
name|updateStatusInQueryDisplay
parameter_list|()
block|{
if|if
condition|(
name|queryDisplay
operator|!=
literal|null
condition|)
block|{
name|queryDisplay
operator|.
name|updateTaskStatus
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|setState
parameter_list|(
name|TaskState
name|state
parameter_list|)
block|{
name|this
operator|.
name|taskState
operator|=
name|state
expr_stmt|;
name|updateStatusInQueryDisplay
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|Hive
name|getHive
parameter_list|()
block|{
try|try
block|{
return|return
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * This method is called in the Driver on every task. It updates counters and calls execute(),    * which is overridden in each task    *    * @return return value of execute()    */
specifier|public
name|int
name|executeTask
parameter_list|(
name|HiveHistory
name|hiveHistory
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|setStarted
argument_list|()
expr_stmt|;
if|if
condition|(
name|hiveHistory
operator|!=
literal|null
condition|)
block|{
name|hiveHistory
operator|.
name|logPlanProgress
argument_list|(
name|queryPlan
argument_list|)
expr_stmt|;
block|}
name|int
name|retval
init|=
name|execute
argument_list|(
name|driverContext
argument_list|)
decl_stmt|;
name|this
operator|.
name|setDone
argument_list|()
expr_stmt|;
if|if
condition|(
name|hiveHistory
operator|!=
literal|null
condition|)
block|{
name|hiveHistory
operator|.
name|logPlanProgress
argument_list|(
name|queryPlan
argument_list|)
expr_stmt|;
block|}
return|return
name|retval
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * This method is overridden in each Task. TODO execute should return a TaskHandle.    *    * @return status of executing the task    */
specifier|protected
specifier|abstract
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
function_decl|;
specifier|public
name|boolean
name|isRootTask
parameter_list|()
block|{
return|return
name|rootTask
return|;
block|}
specifier|public
name|void
name|setRootTask
parameter_list|(
name|boolean
name|rootTask
parameter_list|)
block|{
name|this
operator|.
name|rootTask
operator|=
name|rootTask
expr_stmt|;
block|}
specifier|public
name|void
name|setChildTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childTasks
parameter_list|)
block|{
name|this
operator|.
name|childTasks
operator|=
name|childTasks
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|getChildren
parameter_list|()
block|{
return|return
name|getChildTasks
argument_list|()
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
name|getChildTasks
parameter_list|()
block|{
return|return
name|childTasks
return|;
block|}
specifier|public
name|int
name|getNumChild
parameter_list|()
block|{
return|return
name|childTasks
operator|==
literal|null
condition|?
literal|0
else|:
name|childTasks
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|void
name|setParentTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentTasks
parameter_list|)
block|{
name|this
operator|.
name|parentTasks
operator|=
name|parentTasks
expr_stmt|;
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
name|getParentTasks
parameter_list|()
block|{
return|return
name|parentTasks
return|;
block|}
specifier|public
name|int
name|getNumParent
parameter_list|()
block|{
return|return
name|parentTasks
operator|==
literal|null
condition|?
literal|0
else|:
name|parentTasks
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getBackupTask
parameter_list|()
block|{
return|return
name|backupTask
return|;
block|}
specifier|public
name|void
name|setBackupTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|backupTask
parameter_list|)
block|{
name|this
operator|.
name|backupTask
operator|=
name|backupTask
expr_stmt|;
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
name|getBackupChildrenTasks
parameter_list|()
block|{
return|return
name|backupChildrenTasks
return|;
block|}
specifier|public
name|void
name|setBackupChildrenTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|backupChildrenTasks
parameter_list|)
block|{
name|this
operator|.
name|backupChildrenTasks
operator|=
name|backupChildrenTasks
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getAndInitBackupTask
parameter_list|()
block|{
if|if
condition|(
name|backupTask
operator|!=
literal|null
condition|)
block|{
comment|// first set back the backup task with its children task.
if|if
condition|(
name|backupChildrenTasks
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
name|backupChild
range|:
name|backupChildrenTasks
control|)
block|{
name|backupChild
operator|.
name|getParentTasks
argument_list|()
operator|.
name|add
argument_list|(
name|backupTask
argument_list|)
expr_stmt|;
block|}
block|}
comment|// recursively remove task from its children tasks if this task doesn't have any parent task
name|this
operator|.
name|removeFromChildrenTasks
argument_list|()
expr_stmt|;
block|}
return|return
name|backupTask
return|;
block|}
specifier|public
name|void
name|removeFromChildrenTasks
parameter_list|()
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childrenTasks
init|=
name|this
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|childrenTasks
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTsk
range|:
name|childrenTasks
control|)
block|{
comment|// remove this task from its children tasks
name|childTsk
operator|.
name|getParentTasks
argument_list|()
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// recursively remove non-parent task from its children
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|siblingTasks
init|=
name|childTsk
operator|.
name|getParentTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|siblingTasks
operator|==
literal|null
operator|||
name|siblingTasks
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|childTsk
operator|.
name|removeFromChildrenTasks
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * The default dependent tasks are just child tasks, but different types could implement their own    * (e.g. ConditionalTask will use the listTasks as dependents).    *    * @return a list of tasks that are dependent on this task.    */
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
name|getDependentTasks
parameter_list|()
block|{
return|return
name|getChildTasks
argument_list|()
return|;
block|}
comment|/**    * Add a dependent task on the current task. Return if the dependency already existed or is this a    * new one    *    * @return true if the task got added false if it already existed    */
specifier|public
name|boolean
name|addDependentTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|dependent
parameter_list|)
block|{
name|boolean
name|ret
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|getChildTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
name|setChildTasks
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|getChildTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|dependent
argument_list|)
condition|)
block|{
name|ret
operator|=
literal|true
expr_stmt|;
name|getChildTasks
argument_list|()
operator|.
name|add
argument_list|(
name|dependent
argument_list|)
expr_stmt|;
if|if
condition|(
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
name|dependent
operator|.
name|setParentTasks
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|this
argument_list|)
condition|)
block|{
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"rawtypes"
block|}
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|findLeafs
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
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
name|leafTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|NodeUtils
operator|.
name|iterateTask
argument_list|(
name|rootTasks
argument_list|,
name|Task
operator|.
name|class
argument_list|,
operator|new
name|NodeUtils
operator|.
name|Function
argument_list|<
name|Task
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
name|List
name|dependents
init|=
name|task
operator|.
name|getDependentTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|dependents
operator|==
literal|null
operator|||
name|dependents
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|leafTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|leafTasks
return|;
block|}
comment|/**    * Remove the dependent task.    *    * @param dependent    *          the task to remove    */
specifier|public
name|void
name|removeDependentTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|dependent
parameter_list|)
block|{
if|if
condition|(
operator|(
name|getChildTasks
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|getChildTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|dependent
argument_list|)
operator|)
condition|)
block|{
name|getChildTasks
argument_list|()
operator|.
name|remove
argument_list|(
name|dependent
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|this
argument_list|)
operator|)
condition|)
block|{
name|dependent
operator|.
name|getParentTasks
argument_list|()
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|setStarted
parameter_list|()
block|{
name|setState
argument_list|(
name|TaskState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|started
parameter_list|()
block|{
return|return
name|taskState
operator|==
name|TaskState
operator|.
name|RUNNING
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|done
parameter_list|()
block|{
return|return
name|taskState
operator|==
name|TaskState
operator|.
name|FINISHED
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setDone
parameter_list|()
block|{
name|setState
argument_list|(
name|TaskState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|setQueued
parameter_list|()
block|{
name|setState
argument_list|(
name|TaskState
operator|.
name|QUEUED
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|getQueued
parameter_list|()
block|{
return|return
name|taskState
operator|==
name|TaskState
operator|.
name|QUEUED
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setInitialized
parameter_list|()
block|{
name|setState
argument_list|(
name|TaskState
operator|.
name|INITIALIZED
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|getInitialized
parameter_list|()
block|{
return|return
name|taskState
operator|==
name|TaskState
operator|.
name|INITIALIZED
return|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isNotInitialized
parameter_list|()
block|{
return|return
name|taskState
operator|.
name|ordinal
argument_list|()
operator|<
name|TaskState
operator|.
name|INITIALIZED
operator|.
name|ordinal
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isRunnable
parameter_list|()
block|{
name|boolean
name|isrunnable
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parentTasks
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
name|parent
range|:
name|parentTasks
control|)
block|{
if|if
condition|(
operator|!
name|parent
operator|.
name|done
argument_list|()
condition|)
block|{
name|isrunnable
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|isrunnable
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|void
name|setWork
parameter_list|(
name|T
name|work
parameter_list|)
block|{
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
block|}
specifier|public
name|T
name|getWork
parameter_list|()
block|{
return|return
name|work
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|MapWork
argument_list|>
name|getMapWork
parameter_list|()
block|{
return|return
name|Collections
operator|.
expr|<
name|MapWork
operator|>
name|emptyList
argument_list|()
return|;
block|}
specifier|public
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|String
name|getExternalHandle
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|TaskState
name|getTaskState
parameter_list|()
block|{
return|return
name|taskState
return|;
block|}
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isMapRedLocalTask
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getTopOperators
parameter_list|()
block|{
return|return
operator|new
name|LinkedList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|hasReduce
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getReducer
parameter_list|(
name|MapWork
name|work
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getCounters
parameter_list|()
block|{
return|return
name|taskCounters
return|;
block|}
comment|/**    * Should be overridden to return the type of the specific task among the types in StageType.    *    * @return StageType.* or null if not overridden    */
specifier|public
specifier|abstract
name|StageType
name|getType
parameter_list|()
function_decl|;
comment|/**    * Subscribe the feed of publisher. To prevent cycles, a task can only subscribe to its ancestor.    * Feed is a generic form of execution-time feedback (type, value) pair from one task to another    * task. Examples include dynamic partitions (which are only available at execution time). The    * MoveTask may pass the list of dynamic partitions to the StatsTask since after the MoveTask the    * list of dynamic partitions are lost (MoveTask moves them to the table's destination directory    * which is mixed with old partitions).    *    * @param publisher    *          this feed provider.    */
specifier|public
name|void
name|subscribeFeed
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|publisher
parameter_list|)
block|{
if|if
condition|(
name|publisher
operator|!=
name|this
operator|&&
name|publisher
operator|.
name|ancestorOrSelf
argument_list|(
name|this
argument_list|)
condition|)
block|{
if|if
condition|(
name|publisher
operator|.
name|getFeedSubscribers
argument_list|()
operator|==
literal|null
condition|)
block|{
name|publisher
operator|.
name|setFeedSubscribers
argument_list|(
operator|new
name|LinkedList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|publisher
operator|.
name|getFeedSubscribers
argument_list|()
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|// return true if this task is an ancestor of itself of parameter desc
specifier|private
name|boolean
name|ancestorOrSelf
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|desc
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|desc
condition|)
block|{
return|return
literal|true
return|;
block|}
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|deps
init|=
name|getDependentTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|deps
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
name|d
range|:
name|deps
control|)
block|{
if|if
condition|(
name|d
operator|.
name|ancestorOrSelf
argument_list|(
name|desc
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
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
name|getFeedSubscribers
parameter_list|()
block|{
return|return
name|feedSubscribers
return|;
block|}
specifier|public
name|void
name|setFeedSubscribers
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|s
parameter_list|)
block|{
name|feedSubscribers
operator|=
name|s
expr_stmt|;
block|}
comment|// push the feed to its subscribers
specifier|protected
name|void
name|pushFeed
parameter_list|(
name|FeedType
name|feedType
parameter_list|,
name|Object
name|feedValue
parameter_list|)
block|{
if|if
condition|(
name|feedSubscribers
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
name|s
range|:
name|feedSubscribers
control|)
block|{
name|s
operator|.
name|receiveFeed
argument_list|(
name|feedType
argument_list|,
name|feedValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// a subscriber accept the feed and do something depending on the Task type
specifier|protected
name|void
name|receiveFeed
parameter_list|(
name|FeedType
name|feedType
parameter_list|,
name|Object
name|feedValue
parameter_list|)
block|{   }
specifier|protected
name|void
name|cloneConf
parameter_list|()
block|{
if|if
condition|(
operator|!
name|clonedConf
condition|)
block|{
name|clonedConf
operator|=
literal|true
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Provide metrics on the type and number of tasks executed by the HiveServer    * @param metrics    */
specifier|public
name|void
name|updateTaskMetrics
parameter_list|(
name|Metrics
name|metrics
parameter_list|)
block|{
comment|// no metrics gathered by default
block|}
specifier|public
name|int
name|getTaskTag
parameter_list|()
block|{
return|return
name|taskTag
return|;
block|}
specifier|public
name|void
name|setTaskTag
parameter_list|(
name|int
name|taskTag
parameter_list|)
block|{
name|this
operator|.
name|taskTag
operator|=
name|taskTag
expr_stmt|;
block|}
specifier|public
name|boolean
name|isLocalMode
parameter_list|()
block|{
return|return
name|isLocalMode
return|;
block|}
specifier|public
name|void
name|setLocalMode
parameter_list|(
name|boolean
name|isLocalMode
parameter_list|)
block|{
name|this
operator|.
name|isLocalMode
operator|=
name|isLocalMode
expr_stmt|;
block|}
specifier|public
name|boolean
name|requireLock
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|QueryPlan
name|getQueryPlan
parameter_list|()
block|{
return|return
name|queryPlan
return|;
block|}
specifier|public
name|DriverContext
name|getDriverContext
parameter_list|()
block|{
return|return
name|driverContext
return|;
block|}
specifier|public
name|void
name|setDriverContext
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|this
operator|.
name|driverContext
operator|=
name|driverContext
expr_stmt|;
block|}
specifier|public
name|void
name|setQueryPlan
parameter_list|(
name|QueryPlan
name|queryPlan
parameter_list|)
block|{
name|this
operator|.
name|queryPlan
operator|=
name|queryPlan
expr_stmt|;
block|}
specifier|public
name|String
name|getJobID
parameter_list|()
block|{
return|return
name|jobID
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{   }
name|Throwable
name|getException
parameter_list|()
block|{
return|return
name|exception
return|;
block|}
specifier|protected
name|void
name|setException
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|exception
operator|=
name|ex
expr_stmt|;
block|}
specifier|public
name|void
name|setConsole
parameter_list|(
name|LogHelper
name|console
parameter_list|)
block|{
name|this
operator|.
name|console
operator|=
name|console
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFetchSource
parameter_list|()
block|{
return|return
name|fetchSource
return|;
block|}
specifier|public
name|void
name|setFetchSource
parameter_list|(
name|boolean
name|fetchSource
parameter_list|)
block|{
name|this
operator|.
name|fetchSource
operator|=
name|fetchSource
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|getType
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|canExecuteInParallel
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|QueryState
name|getQueryState
parameter_list|()
block|{
return|return
name|queryState
return|;
block|}
specifier|public
name|HiveTxnManager
name|getTxnMgr
parameter_list|()
block|{
return|return
name|driverContext
operator|.
name|getCtx
argument_list|()
operator|.
name|getHiveTxnManager
argument_list|()
return|;
block|}
block|}
end_class

end_unit

