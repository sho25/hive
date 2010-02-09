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
name|LinkedList
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

begin_comment
comment|/**  * DriverContext.  *  */
end_comment

begin_class
specifier|public
class|class
name|DriverContext
block|{
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
init|=
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
decl_stmt|;
comment|// how many jobs have been started
name|int
name|curJobNo
decl_stmt|;
specifier|public
name|DriverContext
parameter_list|(
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
parameter_list|)
block|{
name|this
operator|.
name|runnable
operator|=
name|runnable
expr_stmt|;
block|}
specifier|public
name|Queue
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getRunnable
parameter_list|()
block|{
return|return
name|runnable
return|;
block|}
comment|/**    * Checks if a task can be launched.    *     * @param tsk    *          the task to be checked    * @return true if the task is launchable, false otherwise    */
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
name|void
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
block|{
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
block|}
end_class

end_unit

