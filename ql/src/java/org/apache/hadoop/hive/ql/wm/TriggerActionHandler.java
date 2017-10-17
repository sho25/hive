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
name|tez
operator|.
name|TezSessionState
import|;
end_import

begin_comment
comment|/**  * Interface for handling rule violations by queries and for performing actions defined in the rules.  */
end_comment

begin_interface
specifier|public
interface|interface
name|TriggerActionHandler
block|{
comment|/**    * Applies the action defined in the rule for the specified queries    *    * @param queriesViolated - violated queries and the rule it violated    */
name|void
name|applyAction
parameter_list|(
name|Map
argument_list|<
name|TezSessionState
argument_list|,
name|Trigger
operator|.
name|Action
argument_list|>
name|queriesViolated
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

