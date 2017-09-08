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
name|util
operator|.
name|Collection
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
name|registry
operator|.
name|ServiceInstanceSet
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
name|ApplicationId
import|;
end_import

begin_interface
specifier|public
interface|interface
name|LlapServiceInstanceSet
extends|extends
name|ServiceInstanceSet
argument_list|<
name|LlapServiceInstance
argument_list|>
block|{
comment|/**    * Gets a list containing all the instances. This list has the same iteration order across    * different processes, assuming the list of registry entries is the same.    * @param consistentIndexes if true, also try to maintain the same exact index for each node    *                          across calls, by inserting inactive instances to replace the    *                          removed ones.    */
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getAllInstancesOrdered
parameter_list|(
name|boolean
name|consistentIndexes
parameter_list|)
function_decl|;
comment|/** LLAP application ID */
name|ApplicationId
name|getApplicationId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

