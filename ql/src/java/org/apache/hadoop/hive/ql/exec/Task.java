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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|session
operator|.
name|SessionState
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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * Task implementation  **/
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
specifier|transient
specifier|protected
name|boolean
name|started
decl_stmt|;
specifier|transient
specifier|protected
name|boolean
name|initialized
decl_stmt|;
specifier|transient
specifier|protected
name|boolean
name|isdone
decl_stmt|;
specifier|transient
specifier|protected
name|boolean
name|queued
decl_stmt|;
specifier|transient
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
specifier|transient
specifier|protected
name|Hive
name|db
decl_stmt|;
specifier|transient
specifier|protected
name|Log
name|LOG
decl_stmt|;
specifier|transient
specifier|protected
name|LogHelper
name|console
decl_stmt|;
specifier|transient
specifier|protected
name|QueryPlan
name|queryPlan
decl_stmt|;
specifier|transient
specifier|protected
name|TaskHandle
name|taskHandle
decl_stmt|;
specifier|transient
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|taskCounters
decl_stmt|;
specifier|transient
specifier|protected
name|DriverContext
name|driverContext
decl_stmt|;
comment|// Bean methods
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
specifier|public
name|Task
parameter_list|()
block|{
name|isdone
operator|=
literal|false
expr_stmt|;
name|started
operator|=
literal|false
expr_stmt|;
name|initialized
operator|=
literal|false
expr_stmt|;
name|queued
operator|=
literal|false
expr_stmt|;
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
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
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|this
operator|.
name|queryPlan
operator|=
name|queryPlan
expr_stmt|;
name|isdone
operator|=
literal|false
expr_stmt|;
name|started
operator|=
literal|false
expr_stmt|;
name|setInitialized
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
try|try
block|{
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// Bail out ungracefully - we should never hit
comment|// this here - but would have hit it in SemanticAnalyzer
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
comment|/**    * This method is called in the Driver on every task. It updates counters    * and calls execute(), which is overridden in each task    * @return return value of execute()    */
specifier|public
name|int
name|executeTask
parameter_list|()
block|{
try|try
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|this
operator|.
name|setStarted
argument_list|()
expr_stmt|;
if|if
condition|(
name|ss
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|getHiveHistory
argument_list|()
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
argument_list|()
decl_stmt|;
name|this
operator|.
name|setDone
argument_list|()
expr_stmt|;
if|if
condition|(
name|ss
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|getHiveHistory
argument_list|()
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * This method is overridden in each Task.    * TODO execute should return a TaskHandle.    * @return status of executing the task    */
specifier|protected
specifier|abstract
name|int
name|execute
parameter_list|()
function_decl|;
comment|/**    * Update the progress of the task within taskHandle and also    * dump the progress information to the history file    * @param taskHandle task handle returned by execute    * @throws IOException     */
specifier|public
name|void
name|progress
parameter_list|(
name|TaskHandle
name|taskHandle
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do nothing by default
block|}
comment|// dummy method - FetchTask overwrites this
specifier|public
name|boolean
name|fetch
parameter_list|(
name|Vector
argument_list|<
name|String
argument_list|>
name|res
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
literal|false
assert|;
return|return
literal|false
return|;
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
comment|/**    * Add a dependent task on the current task. Return if the dependency already existed or is this a new one    * @return true if the task got added false if it already existed    */
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
comment|/**    * remove the dependent task    * @param dependent the task to remove    */
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
specifier|public
name|void
name|setStarted
parameter_list|()
block|{
name|this
operator|.
name|started
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|started
parameter_list|()
block|{
return|return
name|started
return|;
block|}
specifier|public
name|boolean
name|done
parameter_list|()
block|{
return|return
name|isdone
return|;
block|}
specifier|public
name|void
name|setDone
parameter_list|()
block|{
name|isdone
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|setQueued
parameter_list|()
block|{
name|queued
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|getQueued
parameter_list|()
block|{
return|return
name|queued
return|;
block|}
specifier|public
name|void
name|setInitialized
parameter_list|()
block|{
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|getInitialized
parameter_list|()
block|{
return|return
name|initialized
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
specifier|protected
name|String
name|id
decl_stmt|;
specifier|protected
name|T
name|work
decl_stmt|;
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
name|hasReduce
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|updateCounters
parameter_list|(
name|TaskHandle
name|th
parameter_list|)
throws|throws
name|IOException
block|{
comment|// default, do nothing
block|}
specifier|public
name|Map
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
comment|/**    * Should be overridden to return the type of the specific task among    * the types in TaskType    *     * @return TaskTypeType.* or -1 if not overridden    */
specifier|public
name|int
name|getType
parameter_list|()
block|{
assert|assert
literal|false
assert|;
return|return
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

