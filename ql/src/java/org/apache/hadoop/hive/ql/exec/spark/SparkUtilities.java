begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
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
name|TaskContext
import|;
end_import

begin_comment
comment|/**  * Contains utilities methods used as part of Spark tasks  */
end_comment

begin_class
specifier|public
class|class
name|SparkUtilities
block|{
specifier|public
specifier|static
name|void
name|setTaskInfoInJobConf
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|TaskContext
name|taskContext
parameter_list|)
block|{
comment|// Set mapred.task.partition in executor side.
name|jobConf
operator|.
name|setInt
argument_list|(
literal|"mapred.task.partition"
argument_list|,
name|taskContext
operator|.
name|getPartitionId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set mapred.task.id as taskId_attemptId. The taskId is 6 digits in length (prefixed with 0 if
comment|// necessary). Similarly attemptId is two digits in length.
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"%06d_%02d"
argument_list|,
name|taskContext
operator|.
name|getPartitionId
argument_list|()
argument_list|,
name|taskContext
operator|.
name|getAttemptId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

