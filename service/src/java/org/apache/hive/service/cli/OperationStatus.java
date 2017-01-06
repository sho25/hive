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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_comment
comment|/**  * OperationStatus  *  */
end_comment

begin_class
specifier|public
class|class
name|OperationStatus
block|{
specifier|private
specifier|final
name|OperationState
name|state
decl_stmt|;
specifier|private
specifier|final
name|String
name|taskStatus
decl_stmt|;
specifier|private
specifier|final
name|long
name|operationStarted
decl_stmt|;
specifier|private
specifier|final
name|long
name|operationCompleted
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hasResultSet
decl_stmt|;
specifier|private
specifier|final
name|HiveSQLException
name|operationException
decl_stmt|;
specifier|private
name|JobProgressUpdate
name|jobProgressUpdate
decl_stmt|;
specifier|public
name|OperationStatus
parameter_list|(
name|OperationState
name|state
parameter_list|,
name|String
name|taskStatus
parameter_list|,
name|long
name|operationStarted
parameter_list|,
name|long
name|operationCompleted
parameter_list|,
name|boolean
name|hasResultSet
parameter_list|,
name|HiveSQLException
name|operationException
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|taskStatus
operator|=
name|taskStatus
expr_stmt|;
name|this
operator|.
name|operationStarted
operator|=
name|operationStarted
expr_stmt|;
name|this
operator|.
name|operationCompleted
operator|=
name|operationCompleted
expr_stmt|;
name|this
operator|.
name|hasResultSet
operator|=
name|hasResultSet
expr_stmt|;
name|this
operator|.
name|operationException
operator|=
name|operationException
expr_stmt|;
block|}
specifier|public
name|OperationState
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|String
name|getTaskStatus
parameter_list|()
block|{
return|return
name|taskStatus
return|;
block|}
specifier|public
name|long
name|getOperationStarted
parameter_list|()
block|{
return|return
name|operationStarted
return|;
block|}
specifier|public
name|long
name|getOperationCompleted
parameter_list|()
block|{
return|return
name|operationCompleted
return|;
block|}
specifier|public
name|boolean
name|getHasResultSet
parameter_list|()
block|{
return|return
name|hasResultSet
return|;
block|}
specifier|public
name|HiveSQLException
name|getOperationException
parameter_list|()
block|{
return|return
name|operationException
return|;
block|}
name|void
name|setJobProgressUpdate
parameter_list|(
name|JobProgressUpdate
name|jobProgressUpdate
parameter_list|)
block|{
name|this
operator|.
name|jobProgressUpdate
operator|=
name|jobProgressUpdate
expr_stmt|;
block|}
specifier|public
name|JobProgressUpdate
name|jobProgressUpdate
parameter_list|()
block|{
return|return
name|jobProgressUpdate
return|;
block|}
block|}
end_class

end_unit

