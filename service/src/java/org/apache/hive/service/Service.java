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
name|hive
operator|.
name|service
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
comment|/**  * Service.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|Service
block|{
comment|/**    * Service states    */
specifier|public
enum|enum
name|STATE
block|{
comment|/** Constructed but not initialized */
name|NOTINITED
block|,
comment|/** Initialized but not started or stopped */
name|INITED
block|,
comment|/** started and not stopped */
name|STARTED
block|,
comment|/** stopped. No further state transitions are permitted */
name|STOPPED
block|}
comment|/**    * Initialize the service.    *    * The transition must be from {@link STATE#NOTINITED} to {@link STATE#INITED} unless the    * operation failed and an exception was raised.    *    * @param config    *          the configuration of the service    */
name|void
name|init
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
function_decl|;
comment|/**    * Start the service.    *    * The transition should be from {@link STATE#INITED} to {@link STATE#STARTED} unless the    * operation failed and an exception was raised.    */
name|void
name|start
parameter_list|()
function_decl|;
comment|/**    * Stop the service.    *    * This operation must be designed to complete regardless of the initial state    * of the service, including the state of all its internal fields.    */
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * Register an instance of the service state change events.    *    * @param listener    *          a new listener    */
name|void
name|register
parameter_list|(
name|ServiceStateChangeListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Unregister a previously instance of the service state change events.    *    * @param listener    *          the listener to unregister.    */
name|void
name|unregister
parameter_list|(
name|ServiceStateChangeListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Get the name of this service.    *    * @return the service name    */
name|String
name|getName
parameter_list|()
function_decl|;
comment|/**    * Get the configuration of this service.    * This is normally not a clone and may be manipulated, though there are no    * guarantees as to what the consequences of such actions may be    *    * @return the current configuration, unless a specific implementation chooses    *         otherwise.    */
name|HiveConf
name|getHiveConf
parameter_list|()
function_decl|;
comment|/**    * Get the current service state    *    * @return the state of the service    */
name|STATE
name|getServiceState
parameter_list|()
function_decl|;
comment|/**    * Get the service start time    *    * @return the start time of the service. This will be zero if the service    *         has not yet been started.    */
name|long
name|getStartTime
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

