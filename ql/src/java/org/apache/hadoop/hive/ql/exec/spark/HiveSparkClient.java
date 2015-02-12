begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|plan
operator|.
name|SparkWork
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
name|SparkConf
import|;
end_import

begin_interface
specifier|public
interface|interface
name|HiveSparkClient
extends|extends
name|Serializable
extends|,
name|Closeable
block|{
comment|/**    * HiveSparkClient should generate Spark RDD graph by given sparkWork and driverContext,    * and submit RDD graph to Spark cluster.    * @param driverContext    * @param sparkWork    * @return SparkJobRef could be used to track spark job progress and metrics.    * @throws Exception    */
name|SparkJobRef
name|execute
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
comment|/**    * @return spark configuration    */
name|SparkConf
name|getSparkConf
parameter_list|()
function_decl|;
comment|/**    * @return the number of executors    */
name|int
name|getExecutorCount
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/**    * For standalone mode, this can be used to get total number of cores.    * @return  default parallelism.    */
name|int
name|getDefaultParallelism
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
end_interface

end_unit

