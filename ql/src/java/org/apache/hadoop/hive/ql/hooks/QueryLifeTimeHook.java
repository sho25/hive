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

begin_comment
comment|/**  * A type of hook which triggers before query compilation and after query execution.  */
end_comment

begin_interface
specifier|public
interface|interface
name|QueryLifeTimeHook
extends|extends
name|Hook
block|{
comment|/**    * Invoked before a query enters the compilation phase.    *    * @param ctx the context for the hook    */
name|void
name|beforeCompile
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|)
function_decl|;
comment|/**    * Invoked after a query compilation. Note: if 'hasError' is true,    * the query won't enter the following execution phase.    *    * @param ctx the context for the hook    * @param hasError whether any error occurred during compilation.    */
name|void
name|afterCompile
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|,
name|boolean
name|hasError
parameter_list|)
function_decl|;
comment|/**    * Invoked before a query enters the execution phase.    *    * @param ctx the context for the hook    */
name|void
name|beforeExecution
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|)
function_decl|;
comment|/**    * Invoked after a query finishes its execution.    *    * @param ctx the context for the hook    * @param hasError whether any error occurred during query execution.    */
name|void
name|afterExecution
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|,
name|boolean
name|hasError
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

