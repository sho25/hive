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
name|Set
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
name|fs
operator|.
name|ContentSummary
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
name|shims
operator|.
name|Utils
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Hook Context keeps all the necessary information for all the hooks.  * New implemented hook can get the query plan, job conf and the list of all completed tasks from this hook context  */
end_comment

begin_class
specifier|public
class|class
name|HookContext
block|{
specifier|static
specifier|public
enum|enum
name|HookType
block|{
name|PRE_EXEC_HOOK
block|,
name|POST_EXEC_HOOK
block|,
name|ON_FAILURE_HOOK
block|}
specifier|private
name|QueryPlan
name|queryPlan
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TaskRunner
argument_list|>
name|completeTaskList
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|private
name|LineageInfo
name|linfo
decl_stmt|;
specifier|private
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|private
name|HookType
name|hookType
decl_stmt|;
specifier|final
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
name|inputPathToContentSummary
decl_stmt|;
specifier|private
specifier|final
name|String
name|ipAddress
decl_stmt|;
specifier|private
specifier|final
name|String
name|userName
decl_stmt|;
comment|// unique id set for operation when run from HS2, base64 encoded value of
comment|// TExecuteStatementResp.TOperationHandle.THandleIdentifier.guid
specifier|private
specifier|final
name|String
name|operationId
decl_stmt|;
specifier|public
name|HookContext
parameter_list|(
name|QueryPlan
name|queryPlan
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
name|inputPathToContentSummary
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|String
name|operationId
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|queryPlan
operator|=
name|queryPlan
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|inputPathToContentSummary
operator|=
name|inputPathToContentSummary
expr_stmt|;
name|completeTaskList
operator|=
operator|new
name|ArrayList
argument_list|<
name|TaskRunner
argument_list|>
argument_list|()
expr_stmt|;
name|inputs
operator|=
name|queryPlan
operator|.
name|getInputs
argument_list|()
expr_stmt|;
name|outputs
operator|=
name|queryPlan
operator|.
name|getOutputs
argument_list|()
expr_stmt|;
name|ugi
operator|=
name|Utils
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|linfo
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|linfo
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getLineageState
argument_list|()
operator|.
name|getLineageInfo
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|ipAddress
operator|=
name|ipAddress
expr_stmt|;
name|this
operator|.
name|operationId
operator|=
name|operationId
expr_stmt|;
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
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
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
name|List
argument_list|<
name|TaskRunner
argument_list|>
name|getCompleteTaskList
parameter_list|()
block|{
return|return
name|completeTaskList
return|;
block|}
specifier|public
name|void
name|setCompleteTaskList
parameter_list|(
name|List
argument_list|<
name|TaskRunner
argument_list|>
name|completeTaskList
parameter_list|)
block|{
name|this
operator|.
name|completeTaskList
operator|=
name|completeTaskList
expr_stmt|;
block|}
specifier|public
name|void
name|addCompleteTask
parameter_list|(
name|TaskRunner
name|completeTaskRunner
parameter_list|)
block|{
name|completeTaskList
operator|.
name|add
argument_list|(
name|completeTaskRunner
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
specifier|public
name|void
name|setInputs
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
specifier|public
name|void
name|setOutputs
parameter_list|(
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
specifier|public
name|LineageInfo
name|getLinfo
parameter_list|()
block|{
return|return
name|linfo
return|;
block|}
specifier|public
name|void
name|setLinfo
parameter_list|(
name|LineageInfo
name|linfo
parameter_list|)
block|{
name|this
operator|.
name|linfo
operator|=
name|linfo
expr_stmt|;
block|}
specifier|public
name|UserGroupInformation
name|getUgi
parameter_list|()
block|{
return|return
name|ugi
return|;
block|}
specifier|public
name|void
name|setUgi
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
name|getInputPathToContentSummary
parameter_list|()
block|{
return|return
name|inputPathToContentSummary
return|;
block|}
specifier|public
name|HookType
name|getHookType
parameter_list|()
block|{
return|return
name|hookType
return|;
block|}
specifier|public
name|void
name|setHookType
parameter_list|(
name|HookType
name|hookType
parameter_list|)
block|{
name|this
operator|.
name|hookType
operator|=
name|hookType
expr_stmt|;
block|}
specifier|public
name|String
name|getIpAddress
parameter_list|()
block|{
return|return
name|this
operator|.
name|ipAddress
return|;
block|}
specifier|public
name|String
name|getOperationName
parameter_list|()
block|{
return|return
name|queryPlan
operator|.
name|getOperationName
argument_list|()
return|;
block|}
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|this
operator|.
name|userName
return|;
block|}
specifier|public
name|String
name|getOperationId
parameter_list|()
block|{
return|return
name|operationId
return|;
block|}
block|}
end_class

end_unit

