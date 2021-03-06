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
name|beeline
operator|.
name|logs
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
name|InPlaceUpdate
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
name|common
operator|.
name|log
operator|.
name|ProgressMonitor
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
name|jdbc
operator|.
name|logs
operator|.
name|InPlaceUpdateStream
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
name|TProgressUpdateResp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|BeelineInPlaceUpdateStream
implements|implements
name|InPlaceUpdateStream
block|{
specifier|private
name|InPlaceUpdate
name|inPlaceUpdate
decl_stmt|;
specifier|private
name|EventNotifier
name|notifier
decl_stmt|;
specifier|public
name|BeelineInPlaceUpdateStream
parameter_list|(
name|PrintStream
name|out
parameter_list|,
name|InPlaceUpdateStream
operator|.
name|EventNotifier
name|notifier
parameter_list|)
block|{
name|this
operator|.
name|inPlaceUpdate
operator|=
operator|new
name|InPlaceUpdate
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|notifier
operator|=
name|notifier
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|TProgressUpdateResp
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|==
literal|null
operator|||
name|response
operator|.
name|getStatus
argument_list|()
operator|.
name|equals
argument_list|(
name|TJobExecutionStatus
operator|.
name|NOT_AVAILABLE
argument_list|)
condition|)
block|{
comment|/*         we set it to completed if there is nothing the server has to report         for example, DDL statements       */
name|notifier
operator|.
name|progressBarCompleted
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|notifier
operator|.
name|isOperationLogUpdatedAtLeastOnce
argument_list|()
condition|)
block|{
comment|/*         try to render in place update progress bar only if the operations logs is update at least once         as this will hopefully allow printing the metadata information like query id, application id         etc. have to remove these notifiers when the operation logs get merged into GetOperationStatus       */
name|inPlaceUpdate
operator|.
name|render
argument_list|(
operator|new
name|ProgressMonitorWrapper
argument_list|(
name|response
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|EventNotifier
name|getEventNotifier
parameter_list|()
block|{
return|return
name|notifier
return|;
block|}
specifier|static
class|class
name|ProgressMonitorWrapper
implements|implements
name|ProgressMonitor
block|{
specifier|private
name|TProgressUpdateResp
name|response
decl_stmt|;
name|ProgressMonitorWrapper
parameter_list|(
name|TProgressUpdateResp
name|response
parameter_list|)
block|{
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|()
block|{
return|return
name|response
operator|.
name|getHeaderNames
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|response
operator|.
name|getRows
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|footerSummary
parameter_list|()
block|{
return|return
name|response
operator|.
name|getFooterSummary
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|response
operator|.
name|getStartTime
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executionStatus
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This should never be used for anything. All the required data is available via other methods"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|progressedPercentage
parameter_list|()
block|{
return|return
name|response
operator|.
name|getProgressedPercentage
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

