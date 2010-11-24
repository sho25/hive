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
comment|/**  *  * ExecuteWithHookContext is a new interface that the Pre/Post Execute Hook can run with the HookContext.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|ExecuteWithHookContext
extends|extends
name|Hook
block|{
comment|/**    *    * @param hookContext    *     The hook context passed to each hooks.    * @throws Exception    */
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
end_interface

end_unit

