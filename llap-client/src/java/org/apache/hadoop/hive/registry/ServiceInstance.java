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
name|registry
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_interface
specifier|public
interface|interface
name|ServiceInstance
block|{
comment|/**    * Worker identity is a UUID (unique across restarts), to identify a node which died&amp; was brought    * back on the same host/port    */
name|String
name|getWorkerIdentity
parameter_list|()
function_decl|;
comment|/**    * Hostname of the service instance    *     * @return service hostname    */
name|String
name|getHost
parameter_list|()
function_decl|;
comment|/**    * RPC Endpoint for service instance    *    * @return rpc port    */
name|int
name|getRpcPort
parameter_list|()
function_decl|;
comment|/**    * Config properties of the Service Instance (llap.daemon.*)    *    * @return properties    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProperties
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

