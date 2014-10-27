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
comment|/**  * A handle to a submitted job. Allows for monitoring and controlling of the running remote job.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|JobHandle
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
extends|extends
name|Future
argument_list|<
name|T
argument_list|>
block|{
comment|/**    * The client job ID. This is unrelated to any Spark jobs that might be triggered by the    * submitted job.    */
name|String
name|getClientJobId
parameter_list|()
function_decl|;
comment|/**    * A collection of metrics collected from the Spark jobs triggered by this job.    *    * To collect job metrics on the client, Spark jobs must be registered with JobContext::monitor()    * on the remote end.    */
name|MetricsCollection
name|getMetrics
parameter_list|()
function_decl|;
comment|// TODO: expose job status?
block|}
end_interface

end_unit

