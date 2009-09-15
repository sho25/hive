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
comment|/**  * Conditional Task implementation  **/
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
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|resTask
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
if|if
condition|(
name|task
operator|.
name|isMapRedTask
argument_list|()
condition|)
return|return
literal|true
return|;
return|return
literal|false
return|;
block|}
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
if|if
condition|(
name|task
operator|.
name|hasReduce
argument_list|()
condition|)
return|return
literal|true
return|;
return|return
literal|false
return|;
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
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|)
expr_stmt|;
name|resTask
operator|=
name|listTasks
operator|.
name|get
argument_list|(
name|resolver
operator|.
name|getTaskId
argument_list|(
name|conf
argument_list|,
name|resolverCtx
argument_list|)
argument_list|)
expr_stmt|;
name|resTask
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
block|{
return|return
name|resTask
operator|.
name|executeTask
argument_list|()
return|;
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
comment|/**    * @param resolver the resolver to set    */
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
comment|/**    * @param resolverCtx the resolverCtx to set    */
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
comment|/**    * @param listTasks the listTasks to set    */
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
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|CONDITIONAL
return|;
block|}
block|}
end_class

end_unit

