begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|registry
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * ServiceRegistry interface for switching between fixed host and dynamic registry implementations.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ServiceRegistry
block|{
comment|/**    * Start the service registry    *    * @throws IOException    */
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Stop the service registry    *    * @throws IOException    */
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Register the current instance - the implementation takes care of the endpoints to register.    *    * @return self identifying name    *     * @throws IOException    */
specifier|public
name|String
name|register
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Remove the current registration cleanly (implementation defined cleanup)    *    * @throws IOException    */
specifier|public
name|void
name|unregister
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Client API to get the list of instances registered via the current registry key.    *    * @param component    * @return    * @throws IOException    */
specifier|public
name|ServiceInstanceSet
name|getInstances
parameter_list|(
name|String
name|component
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds state change listeners for service instances.    *    * @param listener - state change listener    * @throws IOException    */
specifier|public
name|void
name|registerStateChangeListener
parameter_list|(
name|ServiceInstanceStateChangeListener
name|listener
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

