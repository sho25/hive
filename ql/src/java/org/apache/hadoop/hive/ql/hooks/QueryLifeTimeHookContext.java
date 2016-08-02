begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  **/
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

begin_comment
comment|/**  * Hook context for {@link QueryLifeTimeHook}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|QueryLifeTimeHookContext
block|{
comment|/**    * Get the current Hive configuration    *    * @return the Hive configuration being used    */
name|HiveConf
name|getHiveConf
parameter_list|()
function_decl|;
comment|/**    * Set Hive configuration    */
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
function_decl|;
comment|/**    * Get the current command.    *    * @return the current query command    */
name|String
name|getCommand
parameter_list|()
function_decl|;
comment|/**    * Set the current command    *    * @param command the query command to set    */
name|void
name|setCommand
parameter_list|(
name|String
name|command
parameter_list|)
function_decl|;
comment|/**    * Get the hook context for query execution.    * Note: this result value is null during query compilation phase.    *    * @return a {@link HookContext} instance containing information such as query    * plan, list of tasks, etc.    */
name|HookContext
name|getHookContext
parameter_list|()
function_decl|;
comment|/**    * Set the hook context    *    * @param hc a {@link HookContext} containing information for the current query.    */
name|void
name|setHookContext
parameter_list|(
name|HookContext
name|hc
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

