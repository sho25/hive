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
name|jdbc
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_interface
specifier|public
interface|interface
name|InPlaceUpdateStream
block|{
name|void
name|update
parameter_list|(
name|TProgressUpdateResp
name|response
parameter_list|)
function_decl|;
name|InPlaceUpdateStream
name|NO_OP
init|=
operator|new
name|InPlaceUpdateStream
argument_list|()
block|{
specifier|private
specifier|final
name|EventNotifier
name|eventNotifier
init|=
operator|new
name|EventNotifier
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|TProgressUpdateResp
name|response
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|EventNotifier
name|getEventNotifier
parameter_list|()
block|{
return|return
name|eventNotifier
return|;
block|}
block|}
decl_stmt|;
name|EventNotifier
name|getEventNotifier
parameter_list|()
function_decl|;
class|class
name|EventNotifier
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|EventNotifier
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|isComplete
init|=
literal|false
decl_stmt|;
name|boolean
name|isOperationLogUpdatedOnceAtLeast
init|=
literal|false
decl_stmt|;
specifier|public
specifier|synchronized
name|void
name|progressBarCompleted
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"progress bar is complete"
argument_list|)
expr_stmt|;
name|this
operator|.
name|isComplete
operator|=
literal|true
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|boolean
name|isProgressBarComplete
parameter_list|()
block|{
return|return
name|isComplete
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|operationLogShowedToUser
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"operations log is shown to the user"
argument_list|)
expr_stmt|;
name|isOperationLogUpdatedOnceAtLeast
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isOperationLogUpdatedAtLeastOnce
parameter_list|()
block|{
return|return
name|isOperationLogUpdatedOnceAtLeast
return|;
block|}
specifier|public
name|boolean
name|canOutputOperationLogs
parameter_list|()
block|{
return|return
operator|!
name|isOperationLogUpdatedAtLeastOnce
argument_list|()
operator|||
name|isProgressBarComplete
argument_list|()
return|;
block|}
block|}
block|}
end_interface

end_unit

