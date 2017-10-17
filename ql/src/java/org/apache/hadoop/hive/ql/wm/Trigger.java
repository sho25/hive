begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wm
package|;
end_package

begin_comment
comment|/**  * Trigger interface which gets mapped to CREATE TRIGGER .. queries. A trigger can have a name, expression and action.  * Trigger is a simple expression which gets evaluated during the lifecycle of query and executes an action  * if the expression defined in trigger evaluates to true.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Trigger
block|{
enum|enum
name|Action
block|{
name|KILL_QUERY
argument_list|(
literal|""
argument_list|)
block|,
name|MOVE_TO_POOL
argument_list|(
literal|""
argument_list|)
block|;
name|String
name|poolName
decl_stmt|;
name|Action
parameter_list|(
specifier|final
name|String
name|poolName
parameter_list|)
block|{
name|this
operator|.
name|poolName
operator|=
name|poolName
expr_stmt|;
block|}
specifier|public
name|Action
name|setPoolName
parameter_list|(
specifier|final
name|String
name|poolName
parameter_list|)
block|{
name|this
operator|.
name|poolName
operator|=
name|poolName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getPoolName
parameter_list|()
block|{
return|return
name|poolName
return|;
block|}
block|}
comment|/**    * Based on current value, returns true if trigger is applied else false.    *    * @param current - current value    * @return true if trigger got applied false otherwise    */
name|boolean
name|apply
parameter_list|(
name|long
name|current
parameter_list|)
function_decl|;
comment|/**    * Get trigger expression    *    * @return expression    */
name|Expression
name|getExpression
parameter_list|()
function_decl|;
comment|/**    * Return the name of the trigger    *    * @return trigger name    */
name|String
name|getName
parameter_list|()
function_decl|;
comment|/**    * Return the action that will get executed when trigger expression evaluates to true    *    * @return action    */
name|Action
name|getAction
parameter_list|()
function_decl|;
comment|/**    * Return cloned copy of this trigger    *    * @return clone copy    */
name|Trigger
name|clone
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

