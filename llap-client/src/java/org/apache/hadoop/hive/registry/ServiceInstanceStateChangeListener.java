begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|registry
package|;
end_package

begin_comment
comment|/**  * Callback listener for instance state change events  */
end_comment

begin_interface
specifier|public
interface|interface
name|ServiceInstanceStateChangeListener
parameter_list|<
name|InstanceType
extends|extends
name|ServiceInstance
parameter_list|>
block|{
comment|/**    * Called when new {@link ServiceInstance} is created.    *    * @param serviceInstance - created service instance    */
name|void
name|onCreate
parameter_list|(
name|InstanceType
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
function_decl|;
comment|/**    * Called when an existing {@link ServiceInstance} is updated.    *    * @param serviceInstance - updated service instance    */
name|void
name|onUpdate
parameter_list|(
name|InstanceType
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
function_decl|;
comment|/**    * Called when an existing {@link ServiceInstance} is removed.    *    * @param serviceInstance - removed service instance    */
name|void
name|onRemove
parameter_list|(
name|InstanceType
name|serviceInstance
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

