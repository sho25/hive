begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Defines the API for the Spark remote client.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SparkClient
extends|extends
name|Serializable
block|{
comment|/**    * Submits a job for asynchronous execution.    *    * @param job The job to execute.    * @return A handle that be used to monitor the job.    */
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|JobHandle
argument_list|<
name|T
argument_list|>
name|submit
parameter_list|(
name|Job
argument_list|<
name|T
argument_list|>
name|job
parameter_list|)
function_decl|;
comment|/**    * Stops the remote context.    *    * Any pending jobs will be cancelled, and the remote context will be torn down.    */
name|void
name|stop
parameter_list|()
function_decl|;
comment|/**    * Adds a jar file to the running remote context.    *    * Note that the URL should be reachable by the Spark driver process. If running the driver    * in cluster mode, it may reside on a different host, meaning "file:" URLs have to exist    * on that node (and not on the client machine).    *    * @param url The location of the jar file.    * @return A future that can be used to monitor the operation.    */
name|Future
argument_list|<
name|?
argument_list|>
name|addJar
parameter_list|(
name|URL
name|url
parameter_list|)
function_decl|;
comment|/**    * Adds a file to the running remote context.    *    * Note that the URL should be reachable by the Spark driver process. If running the driver    * in cluster mode, it may reside on a different host, meaning "file:" URLs have to exist    * on that node (and not on the client machine).    *    * @param url The location of the file.    * @return A future that can be used to monitor the operation.    */
name|Future
argument_list|<
name|?
argument_list|>
name|addFile
parameter_list|(
name|URL
name|url
parameter_list|)
function_decl|;
comment|/**    * Get the count of executors    */
name|Future
argument_list|<
name|Integer
argument_list|>
name|getExecutorCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

