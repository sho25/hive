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
name|llap
operator|.
name|daemon
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|RejectedExecutionException
import|;
end_import

begin_comment
comment|/**  * Task scheduler interface  */
end_comment

begin_interface
specifier|public
interface|interface
name|Scheduler
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * Schedule the task or throw RejectedExecutionException if queues are full    * @param t - task to schedule    * @throws RejectedExecutionException    */
name|void
name|schedule
parameter_list|(
name|T
name|t
parameter_list|)
throws|throws
name|RejectedExecutionException
function_decl|;
comment|/**    * Attempt to kill the fragment with the specified fragmentId    * @param fragmentId    */
name|void
name|killFragment
parameter_list|(
name|String
name|fragmentId
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

