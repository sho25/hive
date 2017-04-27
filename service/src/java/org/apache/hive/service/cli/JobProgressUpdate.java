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
name|log
operator|.
name|ProgressMonitor
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

begin_class
specifier|public
class|class
name|JobProgressUpdate
block|{
specifier|public
specifier|final
name|double
name|progressedPercentage
decl_stmt|;
specifier|public
specifier|final
name|String
name|footerSummary
decl_stmt|;
specifier|public
specifier|final
name|long
name|startTimeMillis
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|headers
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|rows
decl_stmt|;
specifier|public
specifier|final
name|String
name|status
decl_stmt|;
name|JobProgressUpdate
parameter_list|(
name|ProgressMonitor
name|monitor
parameter_list|)
block|{
name|this
argument_list|(
name|monitor
operator|.
name|headers
argument_list|()
argument_list|,
name|monitor
operator|.
name|rows
argument_list|()
argument_list|,
name|monitor
operator|.
name|footerSummary
argument_list|()
argument_list|,
name|monitor
operator|.
name|progressedPercentage
argument_list|()
argument_list|,
name|monitor
operator|.
name|startTime
argument_list|()
argument_list|,
name|monitor
operator|.
name|executionStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|JobProgressUpdate
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|rows
parameter_list|,
name|String
name|footerSummary
parameter_list|,
name|double
name|progressedPercentage
parameter_list|,
name|long
name|startTimeMillis
parameter_list|,
name|String
name|status
parameter_list|)
block|{
name|this
operator|.
name|progressedPercentage
operator|=
name|progressedPercentage
expr_stmt|;
name|this
operator|.
name|footerSummary
operator|=
name|footerSummary
expr_stmt|;
name|this
operator|.
name|startTimeMillis
operator|=
name|startTimeMillis
expr_stmt|;
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
name|this
operator|.
name|rows
operator|=
name|rows
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|()
block|{
return|return
name|headers
return|;
block|}
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|rows
parameter_list|()
block|{
return|return
name|rows
return|;
block|}
block|}
end_class

end_unit

