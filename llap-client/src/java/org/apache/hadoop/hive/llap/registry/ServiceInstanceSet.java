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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_interface
specifier|public
interface|interface
name|ServiceInstanceSet
block|{
comment|/**    * Get an instance mapping which map worker identity to each instance.    *     * The worker identity does not collide between restarts, so each restart will have a unique id,    * while having the same host/ip pair.    *     * @return    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ServiceInstance
argument_list|>
name|getAll
parameter_list|()
function_decl|;
comment|/**    * Get an instance by worker identity.    *     * @param name    * @return    */
specifier|public
name|ServiceInstance
name|getInstance
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**    * Get a list of service instances for a given host.    *     * The list could include dead and alive instances.    *     * @param host    * @return    */
specifier|public
name|Set
argument_list|<
name|ServiceInstance
argument_list|>
name|getByHost
parameter_list|(
name|String
name|host
parameter_list|)
function_decl|;
comment|/**    * Refresh the instance set from registry backing store.    *     * @throws IOException    */
specifier|public
name|void
name|refresh
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

