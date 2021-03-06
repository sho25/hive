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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TJobExecutionStatus
import|;
end_import

begin_class
specifier|public
class|class
name|TezProgressMonitorStatusMapper
implements|implements
name|ProgressMonitorStatusMapper
block|{
comment|/**    * These states are taken form DAGStatus.State, could not use that here directly as it was    * optional dependency and did not want to include it just for the enum.    */
enum|enum
name|TezStatus
block|{
name|SUBMITTED
block|,
name|INITING
block|,
name|RUNNING
block|,
name|SUCCEEDED
block|,
name|KILLED
block|,
name|FAILED
block|,
name|ERROR
block|}
annotation|@
name|Override
specifier|public
name|TJobExecutionStatus
name|forStatus
parameter_list|(
name|String
name|status
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|status
argument_list|)
condition|)
block|{
return|return
name|TJobExecutionStatus
operator|.
name|NOT_AVAILABLE
return|;
block|}
name|TezStatus
name|tezStatus
init|=
name|TezStatus
operator|.
name|valueOf
argument_list|(
name|status
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|tezStatus
condition|)
block|{
case|case
name|SUBMITTED
case|:
case|case
name|INITING
case|:
case|case
name|RUNNING
case|:
return|return
name|TJobExecutionStatus
operator|.
name|IN_PROGRESS
return|;
default|default:
return|return
name|TJobExecutionStatus
operator|.
name|COMPLETE
return|;
block|}
block|}
block|}
end_class

end_unit

