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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Represents the mapping from logical resource allocations to queries from WM, to actual physical  * allocations performed using some implementation of a scheduler.  */
end_comment

begin_interface
interface|interface
name|QueryAllocationManager
block|{
name|void
name|start
parameter_list|()
function_decl|;
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * Updates the session allocations asynchronously.    * @param totalMaxAlloc The total maximum fraction of the cluster to allocate. Used to    *                      avoid various artifacts, esp. with small numbers and double weirdness.    *                      Null means the total is unknown.    * @param sessions Sessions to update based on their allocation fraction.    */
name|void
name|updateSessionsAsync
parameter_list|(
name|Double
name|totalMaxAlloc
parameter_list|,
name|List
argument_list|<
name|WmTezSession
argument_list|>
name|sessions
parameter_list|)
function_decl|;
comment|/**    * Sets a callback to be invoked on cluster changes relevant to resource allocation.    */
name|void
name|setClusterChangedCallback
parameter_list|(
name|Runnable
name|clusterChangedCallback
parameter_list|)
function_decl|;
comment|/**    * Updates the session asynchronously with the existing allocation.    */
name|void
name|updateSessionAsync
parameter_list|(
name|WmTezSession
name|session
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

