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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|registry
operator|.
name|ServiceInstance
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|Resource
import|;
end_import

begin_interface
specifier|public
interface|interface
name|LlapServiceInstance
extends|extends
name|ServiceInstance
block|{
comment|/**    * Management endpoint for service instance    *    * @return    */
specifier|public
name|int
name|getManagementPort
parameter_list|()
function_decl|;
comment|/**    * Shuffle Endpoint for service instance    *     * @return    */
specifier|public
name|int
name|getShufflePort
parameter_list|()
function_decl|;
comment|/**    * Address for services hosted on http    * @return    */
specifier|public
name|String
name|getServicesAddress
parameter_list|()
function_decl|;
comment|/**    * OutputFormat endpoint for service instance    *    * @return    */
specifier|public
name|int
name|getOutputFormatPort
parameter_list|()
function_decl|;
comment|/**    * Memory and Executors available for the LLAP tasks    *     * This does not include the size of the cache or the actual vCores allocated via YARN.    *     * @return    */
specifier|public
name|Resource
name|getResource
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

