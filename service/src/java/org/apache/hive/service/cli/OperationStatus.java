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
name|HiveSQLException
name|operationException
decl_stmt|;
specifier|public
name|OperationStatus
parameter_list|(
name|OperationState
name|state
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
name|HiveSQLException
name|getOperationException
parameter_list|()
block|{
return|return
name|operationException
return|;
block|}
block|}
end_class

end_unit

