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
name|reexec
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|Driver
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
name|mapper
operator|.
name|PlanMapper
import|;
end_import

begin_comment
comment|/**  * Defines an interface for re-execution logics.  *  * FIXME: rethink methods.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|IReExecutionPlugin
block|{
comment|/**    * Called when the {@link Driver} is being initialized    *    * The plugin may add hooks/etc to tap into the system.    */
name|void
name|initialize
parameter_list|(
name|Driver
name|driver
parameter_list|)
function_decl|;
comment|/**    * Called before executing the query.    */
name|void
name|beforeExecute
parameter_list|(
name|int
name|executionIndex
parameter_list|,
name|boolean
name|explainReOptimization
parameter_list|)
function_decl|;
comment|/**    * The query have failed, does this plugin advises to re-execute it again?    */
name|boolean
name|shouldReExecute
parameter_list|(
name|int
name|executionNum
parameter_list|)
function_decl|;
comment|/**    * The plugin should prepare for the re-compilaton of the query.    */
name|void
name|prepareToReExecute
parameter_list|()
function_decl|;
comment|/**    * The query have failed; and have been recompiled - does this plugin advises to re-execute it again?    */
name|boolean
name|shouldReExecute
parameter_list|(
name|int
name|executionNum
parameter_list|,
name|PlanMapper
name|oldPlanMapper
parameter_list|,
name|PlanMapper
name|newPlanMapper
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

