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
package|;
end_package

begin_comment
comment|/**  * The class is synchronized, as WebUI may access information about a running query.  */
end_comment

begin_class
specifier|public
class|class
name|QueryInfo
block|{
specifier|private
specifier|final
name|String
name|userName
decl_stmt|;
specifier|private
specifier|final
name|String
name|executionEngine
decl_stmt|;
specifier|private
specifier|final
name|long
name|beginTime
decl_stmt|;
specifier|private
specifier|final
name|String
name|operationId
decl_stmt|;
specifier|private
name|Long
name|runtime
decl_stmt|;
comment|// tracks only running portion of the query.
specifier|private
name|Long
name|endTime
decl_stmt|;
specifier|private
name|String
name|state
decl_stmt|;
specifier|private
name|QueryDisplay
name|queryDisplay
decl_stmt|;
specifier|public
name|QueryInfo
parameter_list|(
name|String
name|state
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|executionEngine
parameter_list|,
name|String
name|operationId
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
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|executionEngine
operator|=
name|executionEngine
expr_stmt|;
name|this
operator|.
name|beginTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|operationId
operator|=
name|operationId
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|long
name|getElapsedTime
parameter_list|()
block|{
if|if
condition|(
name|isRunning
argument_list|()
condition|)
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|beginTime
return|;
block|}
else|else
block|{
return|return
name|endTime
operator|-
name|beginTime
return|;
block|}
block|}
specifier|public
specifier|synchronized
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
name|endTime
operator|==
literal|null
return|;
block|}
specifier|public
specifier|synchronized
name|QueryDisplay
name|getQueryDisplay
parameter_list|()
block|{
return|return
name|queryDisplay
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setQueryDisplay
parameter_list|(
name|QueryDisplay
name|queryDisplay
parameter_list|)
block|{
name|this
operator|.
name|queryDisplay
operator|=
name|queryDisplay
expr_stmt|;
block|}
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|userName
return|;
block|}
specifier|public
name|String
name|getExecutionEngine
parameter_list|()
block|{
return|return
name|executionEngine
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|long
name|getBeginTime
parameter_list|()
block|{
return|return
name|beginTime
return|;
block|}
specifier|public
specifier|synchronized
name|Long
name|getEndTime
parameter_list|()
block|{
return|return
name|endTime
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|updateState
parameter_list|(
name|String
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
specifier|public
name|String
name|getOperationId
parameter_list|()
block|{
return|return
name|operationId
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setEndTime
parameter_list|()
block|{
name|this
operator|.
name|endTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|setRuntime
parameter_list|(
name|long
name|runtime
parameter_list|)
block|{
name|this
operator|.
name|runtime
operator|=
name|runtime
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Long
name|getRuntime
parameter_list|()
block|{
return|return
name|runtime
return|;
block|}
block|}
end_class

end_unit

