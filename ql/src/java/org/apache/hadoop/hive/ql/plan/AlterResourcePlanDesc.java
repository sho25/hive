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
name|plan
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|WMResourcePlanStatus
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
name|Explain
operator|.
name|Level
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Resource plans"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|AlterResourcePlanDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|3514685833183437279L
decl_stmt|;
specifier|private
name|String
name|rpName
decl_stmt|;
specifier|private
name|String
name|newName
decl_stmt|;
specifier|private
name|Integer
name|queryParallelism
decl_stmt|;
specifier|private
name|WMResourcePlanStatus
name|status
decl_stmt|;
specifier|private
name|boolean
name|validate
decl_stmt|;
specifier|private
name|String
name|defaultPoolPath
decl_stmt|;
specifier|private
name|boolean
name|isEnableActivate
decl_stmt|;
specifier|public
name|AlterResourcePlanDesc
parameter_list|()
block|{}
specifier|private
name|AlterResourcePlanDesc
parameter_list|(
name|String
name|rpName
parameter_list|,
name|String
name|newName
parameter_list|,
name|Integer
name|queryParallelism
parameter_list|,
name|WMResourcePlanStatus
name|status
parameter_list|,
name|boolean
name|validate
parameter_list|,
name|String
name|defaultPoolPath
parameter_list|)
block|{
name|this
operator|.
name|rpName
operator|=
name|rpName
expr_stmt|;
name|this
operator|.
name|newName
operator|=
name|newName
expr_stmt|;
name|this
operator|.
name|queryParallelism
operator|=
name|queryParallelism
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|validate
operator|=
name|validate
expr_stmt|;
name|this
operator|.
name|defaultPoolPath
operator|=
name|defaultPoolPath
expr_stmt|;
block|}
specifier|public
specifier|static
name|AlterResourcePlanDesc
name|createSet
parameter_list|(
name|String
name|rpName
parameter_list|)
block|{
return|return
operator|new
name|AlterResourcePlanDesc
argument_list|(
name|rpName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AlterResourcePlanDesc
name|createChangeStatus
parameter_list|(
name|String
name|rpName
parameter_list|,
name|WMResourcePlanStatus
name|status
parameter_list|)
block|{
return|return
operator|new
name|AlterResourcePlanDesc
argument_list|(
name|rpName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|status
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|AlterResourcePlanDesc
name|createValidatePlan
parameter_list|(
name|String
name|rpName
parameter_list|)
block|{
return|return
operator|new
name|AlterResourcePlanDesc
argument_list|(
name|rpName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"resourcePlanName"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getRpName
parameter_list|()
block|{
return|return
name|rpName
return|;
block|}
specifier|public
name|void
name|setRpName
parameter_list|(
name|String
name|rpName
parameter_list|)
block|{
name|this
operator|.
name|rpName
operator|=
name|rpName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"newResourcePlanName"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getNewName
parameter_list|()
block|{
return|return
name|newName
return|;
block|}
specifier|public
name|void
name|setNewName
parameter_list|(
name|String
name|newName
parameter_list|)
block|{
name|this
operator|.
name|newName
operator|=
name|newName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Default pool"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getDefaultPoolPath
parameter_list|()
block|{
return|return
name|defaultPoolPath
return|;
block|}
specifier|public
name|void
name|setDefaultPoolPath
parameter_list|(
name|String
name|defaultPoolPath
parameter_list|)
block|{
name|this
operator|.
name|defaultPoolPath
operator|=
name|defaultPoolPath
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"queryParallelism"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Integer
name|getQueryParallelism
parameter_list|()
block|{
return|return
name|queryParallelism
return|;
block|}
specifier|public
name|void
name|setQueryParallelism
parameter_list|(
name|Integer
name|queryParallelism
parameter_list|)
block|{
name|this
operator|.
name|queryParallelism
operator|=
name|queryParallelism
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"status"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|WMResourcePlanStatus
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
specifier|public
name|void
name|setStatus
parameter_list|(
name|WMResourcePlanStatus
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"shouldValidate"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|shouldValidate
parameter_list|()
block|{
return|return
name|validate
return|;
block|}
specifier|public
name|void
name|setValidate
parameter_list|(
name|boolean
name|validate
parameter_list|)
block|{
name|this
operator|.
name|validate
operator|=
name|validate
expr_stmt|;
block|}
specifier|public
name|void
name|setIsEnableActivate
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|this
operator|.
name|isEnableActivate
operator|=
name|b
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEnableActivate
parameter_list|()
block|{
return|return
name|isEnableActivate
return|;
block|}
block|}
end_class

end_unit

