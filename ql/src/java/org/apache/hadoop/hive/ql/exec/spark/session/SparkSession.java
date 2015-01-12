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
name|exec
operator|.
name|spark
operator|.
name|session
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
name|ql
operator|.
name|DriverContext
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|SparkJobRef
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|plan
operator|.
name|SparkWork
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_interface
specifier|public
interface|interface
name|SparkSession
block|{
comment|/**    * Initializes a Spark session for DAG execution.    * @param conf Hive configuration.    */
name|void
name|open
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Submit given<i>sparkWork</i> to SparkClient.    * @param driverContext    * @param sparkWork    * @return SparkJobRef    */
name|SparkJobRef
name|submit
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**    * Get Spark shuffle memory per task, and total number of cores. This    * information can be used to estimate how many reducers a task can have.    *    * @return a tuple, the first element is the shuffle memory per task in bytes,    *  the second element is the number of total cores usable by the client    */
name|Tuple2
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|getMemoryAndCores
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/**    * @return true if the session is open and ready to submit jobs.    */
name|boolean
name|isOpen
parameter_list|()
function_decl|;
comment|/**    * @return configuration.    */
name|HiveConf
name|getConf
parameter_list|()
function_decl|;
comment|/**    * @return session id.    */
name|String
name|getSessionId
parameter_list|()
function_decl|;
comment|/**    * Close session and release resources.    */
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

