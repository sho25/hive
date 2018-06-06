begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|status
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|Statistic
operator|.
name|SparkStatistics
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
name|JobExecutionStatus
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
comment|/**  * SparkJobStatus identify what Hive want to know about the status of a Spark job.  */
end_comment

begin_interface
specifier|public
interface|interface
name|SparkJobStatus
block|{
name|String
name|getAppID
parameter_list|()
function_decl|;
name|int
name|getJobId
parameter_list|()
function_decl|;
name|JobExecutionStatus
name|getState
parameter_list|()
throws|throws
name|HiveException
function_decl|;
name|int
index|[]
name|getStageIds
parameter_list|()
throws|throws
name|HiveException
function_decl|;
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|getSparkStageProgress
parameter_list|()
throws|throws
name|HiveException
function_decl|;
name|SparkCounters
name|getCounter
parameter_list|()
function_decl|;
name|SparkStatistics
name|getSparkStatistics
parameter_list|()
function_decl|;
name|String
name|getWebUIURL
parameter_list|()
function_decl|;
name|void
name|cleanup
parameter_list|()
function_decl|;
name|Throwable
name|getMonitorError
parameter_list|()
function_decl|;
name|void
name|setMonitorError
parameter_list|(
name|Throwable
name|e
parameter_list|)
function_decl|;
name|Throwable
name|getSparkJobException
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

