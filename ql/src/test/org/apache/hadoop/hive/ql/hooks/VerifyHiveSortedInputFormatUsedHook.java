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
name|hooks
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|hooks
operator|.
name|HookContext
operator|.
name|HookType
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
name|MapredWork
import|;
end_import

begin_class
specifier|public
class|class
name|VerifyHiveSortedInputFormatUsedHook
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|.
name|equals
argument_list|(
name|HookType
operator|.
name|POST_EXEC_HOOK
argument_list|)
condition|)
block|{
comment|// Go through the root tasks, and verify the input format of the map reduce task(s) is
comment|// HiveSortedInputFormat
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
operator|.
name|getRootTasks
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
name|rootTask
range|:
name|rootTasks
control|)
block|{
if|if
condition|(
name|rootTask
operator|.
name|getWork
argument_list|()
operator|instanceof
name|MapredWork
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The root map reduce task's input was not marked as sorted."
argument_list|,
operator|(
operator|(
name|MapredWork
operator|)
name|rootTask
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|isInputFormatSorted
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

