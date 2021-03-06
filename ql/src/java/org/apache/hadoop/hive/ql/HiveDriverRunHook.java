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
name|hooks
operator|.
name|Hook
import|;
end_import

begin_comment
comment|/**  * HiveDriverRunHook allows Hive to be extended with custom  * logic for processing commands.  *  *<p>  *  * Note that the lifetime of an instantiated hook object is scoped to  * the analysis of a single statement; hook instances are never reused.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|HiveDriverRunHook
extends|extends
name|Hook
block|{
comment|/**    * Invoked before Hive begins any processing of a command in the Driver,    * notably before compilation and any customizable performance logging.    */
specifier|public
name|void
name|preDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**    * Invoked after Hive performs any processing of a command, just before a    * response is returned to the entity calling the Driver.    */
specifier|public
name|void
name|postDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
end_interface

end_unit

