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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|stats
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

begin_comment
comment|/**  * An interface for any possible implementation for publishing statics.  */
end_comment

begin_interface
specifier|public
interface|interface
name|StatsPublisher
block|{
comment|/**    * This method does the necessary one-time initializations, possibly creating the tables and    * database (if not exist).    * This method is usually called in the Hive client side rather than by the mappers/reducers    * so that it is initialized only once.    * @param hconf HiveConf that contains the configurations parameters used to connect to    * intermediate stats database.    * @return true if initialization is successful, false otherwise.    */
specifier|public
name|boolean
name|init
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
function_decl|;
comment|/**    * This method connects to the intermediate statistics database.    * @param hconf HiveConf that contains the connection parameters.    * @return true if connection is successful, false otherwise.    */
specifier|public
name|boolean
name|connect
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
function_decl|;
comment|/**    * This method publishes a given statistic into a disk storage, possibly HBase or MySQL.    *    * fileID   : a string identification the statistics to be published by all mappers/reducers    *            and then gathered. The statID is unique per output partition per task, e.g.,:    *                 the output directory name (uniq per FileSinkOperator) +    *                 the partition specs (only for dynamic partitions) +    *                 taskID (last component of task file)    *    * statType : a string noting the key to be published. Ex: "numRows".    *    * value    : an integer noting the value of the published key.    */
specifier|public
name|boolean
name|publishStat
parameter_list|(
name|String
name|fileID
parameter_list|,
name|String
name|statType
parameter_list|,
name|String
name|value
parameter_list|)
function_decl|;
comment|/**    * This method closes the connection to the temporary storage.    */
specifier|public
name|boolean
name|closeConnection
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

