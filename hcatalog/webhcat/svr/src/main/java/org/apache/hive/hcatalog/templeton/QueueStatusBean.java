begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobStatus
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
name|mapred
operator|.
name|JobProfile
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
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|JobState
import|;
end_import

begin_comment
comment|/**  * QueueStatusBean - The results of an exec call.  */
end_comment

begin_class
specifier|public
class|class
name|QueueStatusBean
block|{
specifier|public
name|JobStatus
name|status
decl_stmt|;
specifier|public
name|JobProfile
name|profile
decl_stmt|;
specifier|public
specifier|final
name|String
name|id
decl_stmt|;
specifier|public
name|String
name|parentId
decl_stmt|;
specifier|public
name|String
name|percentComplete
decl_stmt|;
specifier|public
name|Long
name|exitValue
decl_stmt|;
specifier|public
name|String
name|user
decl_stmt|;
specifier|public
name|String
name|callback
decl_stmt|;
specifier|public
name|String
name|completed
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|userargs
decl_stmt|;
specifier|public
name|String
name|msg
decl_stmt|;
specifier|public
name|QueueStatusBean
parameter_list|(
name|String
name|jobId
parameter_list|,
name|String
name|errMsg
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|msg
operator|=
name|errMsg
expr_stmt|;
block|}
comment|/**    * Create a new QueueStatusBean    *    * @param state      store job state    * @param status     job status    * @param profile    job profile    */
specifier|public
name|QueueStatusBean
parameter_list|(
name|JobState
name|state
parameter_list|,
name|JobStatus
name|status
parameter_list|,
name|JobProfile
name|profile
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|profile
operator|=
name|profile
expr_stmt|;
name|id
operator|=
name|profile
operator|.
name|getJobID
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|parentId
operator|=
name|state
operator|.
name|getParent
argument_list|()
expr_stmt|;
name|percentComplete
operator|=
name|state
operator|.
name|getPercentComplete
argument_list|()
expr_stmt|;
name|exitValue
operator|=
name|state
operator|.
name|getExitValue
argument_list|()
expr_stmt|;
name|user
operator|=
name|profile
operator|.
name|getUser
argument_list|()
expr_stmt|;
name|callback
operator|=
name|state
operator|.
name|getCallback
argument_list|()
expr_stmt|;
name|completed
operator|=
name|state
operator|.
name|getCompleteStatus
argument_list|()
expr_stmt|;
name|userargs
operator|=
name|state
operator|.
name|getUserArgs
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

