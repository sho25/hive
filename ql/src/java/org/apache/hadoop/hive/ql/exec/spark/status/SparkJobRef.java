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
operator|.
name|status
package|;
end_package

begin_class
specifier|public
class|class
name|SparkJobRef
block|{
specifier|private
name|String
name|jobId
decl_stmt|;
specifier|private
name|SparkJobStatus
name|sparkJobStatus
decl_stmt|;
specifier|public
name|SparkJobRef
parameter_list|()
block|{}
specifier|public
name|SparkJobRef
parameter_list|(
name|String
name|jobId
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
block|}
specifier|public
name|SparkJobRef
parameter_list|(
name|String
name|jobId
parameter_list|,
name|SparkJobStatus
name|sparkJobStatus
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
name|this
operator|.
name|sparkJobStatus
operator|=
name|sparkJobStatus
expr_stmt|;
block|}
specifier|public
name|String
name|getJobId
parameter_list|()
block|{
return|return
name|jobId
return|;
block|}
specifier|public
name|void
name|setJobId
parameter_list|(
name|String
name|jobId
parameter_list|)
block|{
name|this
operator|.
name|jobId
operator|=
name|jobId
expr_stmt|;
block|}
specifier|public
name|SparkJobStatus
name|getSparkJobStatus
parameter_list|()
block|{
return|return
name|sparkJobStatus
return|;
block|}
specifier|public
name|void
name|setSparkJobStatus
parameter_list|(
name|SparkJobStatus
name|sparkJobStatus
parameter_list|)
block|{
name|this
operator|.
name|sparkJobStatus
operator|=
name|sparkJobStatus
expr_stmt|;
block|}
block|}
end_class

end_unit

