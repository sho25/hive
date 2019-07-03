begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|tezplugins
operator|.
name|metrics
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
name|conf
operator|.
name|Configuration
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
name|llap
operator|.
name|registry
operator|.
name|impl
operator|.
name|LlapRegistryService
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
name|llap
operator|.
name|tezplugins
operator|.
name|metrics
operator|.
name|LlapMetricsCollector
operator|.
name|LlapMetrics
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

begin_comment
comment|/**  * Interface to handle Llap Daemon metrics changes.  */
end_comment

begin_interface
specifier|public
interface|interface
name|LlapMetricsListener
block|{
comment|/**    * Initializing the listener with the current configuration.    * @param conf The configuration    * @param registry The Llap registry service to access the Llap Daemons    */
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LlapRegistryService
name|registry
parameter_list|)
function_decl|;
comment|/**    * Handler will be called when new Llap Daemon metrics data is arrived.    * @param workerIdentity The worker identity of the Llap Daemon    * @param newMetrics The new metrics object    */
name|void
name|newDaemonMetrics
parameter_list|(
name|String
name|workerIdentity
parameter_list|,
name|LlapMetrics
name|newMetrics
parameter_list|)
function_decl|;
comment|/**    * Handler will be called when new data is arrived for every active Llap Daemon in the cluster.    * @param newMetrics The map of the worker indentity -> metrics    */
name|void
name|newClusterMetrics
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LlapMetrics
argument_list|>
name|newMetrics
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

