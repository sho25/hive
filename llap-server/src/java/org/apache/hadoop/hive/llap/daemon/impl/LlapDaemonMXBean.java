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
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MXBean
import|;
end_import

begin_comment
comment|/**  * MXbean to expose llap daemon related information through JMX.  */
end_comment

begin_interface
annotation|@
name|MXBean
specifier|public
interface|interface
name|LlapDaemonMXBean
block|{
comment|/**    * Gets the rpc port.    * @return the rpc port    */
specifier|public
name|int
name|getRpcPort
parameter_list|()
function_decl|;
comment|/**    * Gets the number of executors.    * @return number of executors    */
specifier|public
name|int
name|getNumExecutors
parameter_list|()
function_decl|;
comment|/**    * Gets the number of active executors.    * @return number of active executors    */
specifier|public
name|int
name|getNumActive
parameter_list|()
function_decl|;
comment|/**    * Gets the shuffle port.    * @return the shuffle port    */
specifier|public
name|int
name|getShufflePort
parameter_list|()
function_decl|;
comment|/**    * CSV list of local directories    * @return local dirs    */
specifier|public
name|String
name|getLocalDirs
parameter_list|()
function_decl|;
comment|/**    * Executor states.    * @return Executor states.    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getExecutorsStatus
parameter_list|()
function_decl|;
comment|/**    * Gets llap daemon configured executor memory per instance.    * @return memory per instance    */
specifier|public
name|long
name|getExecutorMemoryPerInstance
parameter_list|()
function_decl|;
comment|/**    * Gets llap daemon configured io memory per instance.    * @return memory per instance    */
specifier|public
name|long
name|getIoMemoryPerInstance
parameter_list|()
function_decl|;
comment|/**    * Checks if Llap IO is enabled    * @return true if enabled, false if not    */
specifier|public
name|boolean
name|isIoEnabled
parameter_list|()
function_decl|;
comment|/**    * Gets max available jvm memory.    * @return max jvm memory    */
specifier|public
name|long
name|getMaxJvmMemory
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

