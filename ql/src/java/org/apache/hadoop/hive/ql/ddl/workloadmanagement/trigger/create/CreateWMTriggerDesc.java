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
name|ddl
operator|.
name|workloadmanagement
operator|.
name|trigger
operator|.
name|create
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
name|ql
operator|.
name|ddl
operator|.
name|DDLDesc
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

begin_comment
comment|/**  * DDL task description for CREATE TRIGGER commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create WM Trigger"
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
name|CreateWMTriggerDesc
implements|implements
name|DDLDesc
implements|,
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
specifier|final
name|String
name|resourcePlanName
decl_stmt|;
specifier|private
specifier|final
name|String
name|triggerName
decl_stmt|;
specifier|private
specifier|final
name|String
name|triggerExpression
decl_stmt|;
specifier|private
specifier|final
name|String
name|actionExpression
decl_stmt|;
specifier|public
name|CreateWMTriggerDesc
parameter_list|(
name|String
name|resourcePlanName
parameter_list|,
name|String
name|triggerName
parameter_list|,
name|String
name|triggerExpression
parameter_list|,
name|String
name|actionExpression
parameter_list|)
block|{
name|this
operator|.
name|resourcePlanName
operator|=
name|resourcePlanName
expr_stmt|;
name|this
operator|.
name|triggerName
operator|=
name|triggerName
expr_stmt|;
name|this
operator|.
name|triggerExpression
operator|=
name|triggerExpression
expr_stmt|;
name|this
operator|.
name|actionExpression
operator|=
name|actionExpression
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Resource plan name"
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
name|getResourcePlanName
parameter_list|()
block|{
return|return
name|resourcePlanName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Trigger name"
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
name|getTriggerName
parameter_list|()
block|{
return|return
name|triggerName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Trigger expression"
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
name|getTriggerExpression
parameter_list|()
block|{
return|return
name|triggerExpression
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Action expression"
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
name|getActionExpression
parameter_list|()
block|{
return|return
name|actionExpression
return|;
block|}
block|}
end_class

end_unit

