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
name|util
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
name|ConditionalTask
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
comment|/**  * The dag traversal done here is written to be not recursion based as large DAG's will lead to  * stack overflow's, hence iteration based.  */
end_comment

begin_class
specifier|public
class|class
name|DAGTraversal
block|{
specifier|public
specifier|static
name|void
name|traverse
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
name|tasks
parameter_list|,
name|Function
name|function
parameter_list|)
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
name|listOfTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tasks
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|listOfTasks
operator|.
name|isEmpty
argument_list|()
condition|)
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
name|children
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
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
name|listOfTasks
control|)
block|{
comment|// skip processing has to be done first before continuing
if|if
condition|(
name|function
operator|.
name|skipProcessing
argument_list|(
name|task
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// Add list tasks from conditional tasks
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
condition|)
block|{
name|children
operator|.
name|addAll
argument_list|(
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|task
operator|.
name|getDependentTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|children
operator|.
name|addAll
argument_list|(
name|task
operator|.
name|getDependentTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|function
operator|.
name|process
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
name|listOfTasks
operator|=
name|children
expr_stmt|;
block|}
block|}
specifier|public
interface|interface
name|Function
block|{
name|void
name|process
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
function_decl|;
name|boolean
name|skipProcessing
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

