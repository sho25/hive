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
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|counter
operator|.
name|SparkCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaFutureAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
import|;
end_import

begin_comment
comment|/**  * Holds runtime information about the job execution context.  *  * An instance of this class is kept on the node hosting a remote Spark context and is made  * available to jobs being executed via RemoteSparkContext#submit().  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|JobContext
block|{
comment|/** The shared SparkContext instance. */
name|JavaSparkContext
name|sc
parameter_list|()
function_decl|;
comment|/**    * Monitor a job. This allows job-related information (such as metrics) to be communicated    * back to the client.    *    * @return The job (unmodified).    */
parameter_list|<
name|T
parameter_list|>
name|JavaFutureAction
argument_list|<
name|T
argument_list|>
name|monitor
parameter_list|(
name|JavaFutureAction
argument_list|<
name|T
argument_list|>
name|job
parameter_list|,
name|SparkCounters
name|sparkCounters
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
parameter_list|)
function_decl|;
comment|/**    * Return a map from client job Id to corresponding JavaFutureActions.    */
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|getMonitoredJobs
parameter_list|()
function_decl|;
comment|/**    * Return all added jar path and timestamp which added through AddJarJob.    */
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getAddedJars
parameter_list|()
function_decl|;
comment|/**    * Returns a local tmp dir specific to the context    */
name|File
name|getLocalTmpDir
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

