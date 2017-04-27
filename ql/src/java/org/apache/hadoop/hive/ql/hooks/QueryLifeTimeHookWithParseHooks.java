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

begin_comment
comment|/**  * Extension of {@link QueryLifeTimeHook} that has hooks for pre and post parsing of a query.  */
end_comment

begin_interface
specifier|public
interface|interface
name|QueryLifeTimeHookWithParseHooks
extends|extends
name|QueryLifeTimeHook
block|{
comment|/**    * Invoked before a query enters the parse phase.    *    * @param ctx the context for the hook    */
name|void
name|beforeParse
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|)
function_decl|;
comment|/**    * Invoked after a query parsing. Note: if 'hasError' is true,    * the query won't enter the following compilation phase.    *    * @param ctx the context for the hook    * @param hasError whether any error occurred during compilation.    */
name|void
name|afterParse
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

