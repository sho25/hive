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
name|ConditionalResolver
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
name|ConditionalWork
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

begin_comment
comment|/**  * Conditional Task implementation.  */
end_comment

begin_class
specifier|public
class|class
name|ConditionalTask
extends|extends
name|Task
argument_list|<
name|ConditionalWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
decl_stmt|;
specifier|private
name|boolean
name|resolved
init|=
literal|false
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|resTasks
decl_stmt|;
specifier|private
name|ConditionalResolver
name|resolver
decl_stmt|;
specifier|private
name|Object
name|resolverCtx
decl_stmt|;
specifier|public
name|ConditionalTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|listTasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canExecuteInParallel
parameter_list|()
block|{
return|return
name|isMapRedTask
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasReduce
parameter_list|()
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|listTasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|hasReduce
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|resTasks
operator|=
name|resolver
operator|.
name|getTasks
argument_list|(
name|conf
argument_list|,
name|resolverCtx
argument_list|)
expr_stmt|;
name|resolved
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|resolveTask
argument_list|(
name|driverContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|resolveTask
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|getListTasks
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|resTasks
operator|.
name|contains
argument_list|(
name|tsk
argument_list|)
condition|)
block|{
name|driverContext
operator|.
name|remove
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|tsk
operator|.
name|getId
argument_list|()
operator|+
literal|" is filtered out by condition resolver."
argument_list|)
expr_stmt|;
if|if
condition|(
name|tsk
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
name|driverContext
operator|.
name|incCurJobNo
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|//recursively remove this task from its children's parent task
name|tsk
operator|.
name|removeFromChildrenTasks
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|getParentTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// This makes it so that we can go back up the tree later
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|getParentTasks
argument_list|()
control|)
block|{
name|task
operator|.
name|addDependentTask
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
comment|// resolved task
if|if
condition|(
name|driverContext
operator|.
name|addToRunnable
argument_list|(
name|tsk
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|tsk
operator|.
name|getId
argument_list|()
operator|+
literal|" is selected by condition resolver."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * @return the resolver    */
specifier|public
name|ConditionalResolver
name|getResolver
parameter_list|()
block|{
return|return
name|resolver
return|;
block|}
comment|/**    * @param resolver    *          the resolver to set    */
specifier|public
name|void
name|setResolver
parameter_list|(
name|ConditionalResolver
name|resolver
parameter_list|)
block|{
name|this
operator|.
name|resolver
operator|=
name|resolver
expr_stmt|;
block|}
comment|/**    * @return the resolverCtx    */
specifier|public
name|Object
name|getResolverCtx
parameter_list|()
block|{
return|return
name|resolverCtx
return|;
block|}
comment|// used to determine whether child tasks can be run.
annotation|@
name|Override
specifier|public
name|boolean
name|done
parameter_list|()
block|{
name|boolean
name|ret
init|=
literal|true
decl_stmt|;
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
init|=
name|getParentTasks
argument_list|()
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
name|par
range|:
name|parentTasks
control|)
block|{
name|ret
operator|=
name|ret
operator|&&
name|par
operator|.
name|done
argument_list|()
expr_stmt|;
block|}
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
name|retTasks
decl_stmt|;
if|if
condition|(
name|resolved
condition|)
block|{
name|retTasks
operator|=
name|resTasks
expr_stmt|;
block|}
else|else
block|{
name|retTasks
operator|=
name|getListTasks
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ret
operator|&&
name|retTasks
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
name|tsk
range|:
name|retTasks
control|)
block|{
name|ret
operator|=
name|ret
operator|&&
name|tsk
operator|.
name|done
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
comment|/**    * @param resolverCtx    *          the resolverCtx to set    */
specifier|public
name|void
name|setResolverCtx
parameter_list|(
name|Object
name|resolverCtx
parameter_list|)
block|{
name|this
operator|.
name|resolverCtx
operator|=
name|resolverCtx
expr_stmt|;
block|}
comment|/**    * @return the listTasks    */
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
name|getListTasks
parameter_list|()
block|{
return|return
name|listTasks
return|;
block|}
comment|/**    * @param listTasks    *          the listTasks to set    */
specifier|public
name|void
name|setListTasks
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
name|listTasks
parameter_list|)
block|{
name|this
operator|.
name|listTasks
operator|=
name|listTasks
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|CONDITIONAL
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"CONDITION"
return|;
block|}
comment|/**    * Add a dependent task on the current conditional task. The task will not be a direct child of    * conditional task. Actually it will be added as child task of associated tasks.    *    * @return true if the task got added false if it already existed    */
annotation|@
name|Override
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
name|getListTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ret
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|getListTasks
argument_list|()
control|)
block|{
name|ret
operator|=
name|ret
operator|&
name|tsk
operator|.
name|addDependentTask
argument_list|(
name|dependent
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
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
name|listTasks
return|;
block|}
block|}
end_class

end_unit

