begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|llap
operator|.
name|daemon
package|;
end_package

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

begin_class
specifier|public
class|class
name|HistoryLogger
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_EVENT_TYPE
init|=
literal|"Event"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_APPLICATION_ID
init|=
literal|"ApplicationId"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_CONTAINER_ID
init|=
literal|"ContainerId"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_SUBMIT_TIME
init|=
literal|"SubmitTime"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_START_TIME
init|=
literal|"StartTime"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_END_TIME
init|=
literal|"EndTime"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_DAG_NAME
init|=
literal|"DagName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_VERTEX_NAME
init|=
literal|"VertexName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_TASK_ID
init|=
literal|"TaskId"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_ATTEMPT_ID
init|=
literal|"TaskAttemptId"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_THREAD_NAME
init|=
literal|"ThreadName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_HOSTNAME
init|=
literal|"HostName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HISTORY_SUCCEEDED
init|=
literal|"Succeeded"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EVENT_TYPE_FRAGMENT_START
init|=
literal|"FRAGMENT_START"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EVENT_TYPE_FRAGMENT_END
init|=
literal|"FRAGMENT_END"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|HISTORY_LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HistoryLogger
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|logFragmentStart
parameter_list|(
name|String
name|applicationIdStr
parameter_list|,
name|String
name|containerIdStr
parameter_list|,
name|String
name|hostname
parameter_list|,
name|String
name|dagName
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|taskId
parameter_list|,
name|int
name|attemptId
parameter_list|)
block|{
name|HISTORY_LOGGER
operator|.
name|info
argument_list|(
name|constructFragmentStartString
argument_list|(
name|applicationIdStr
argument_list|,
name|containerIdStr
argument_list|,
name|hostname
argument_list|,
name|dagName
argument_list|,
name|vertexName
argument_list|,
name|taskId
argument_list|,
name|attemptId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|logFragmentEnd
parameter_list|(
name|String
name|applicationIdStr
parameter_list|,
name|String
name|containerIdStr
parameter_list|,
name|String
name|hostname
parameter_list|,
name|String
name|dagName
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|taskId
parameter_list|,
name|int
name|attemptId
parameter_list|,
name|String
name|threadName
parameter_list|,
name|long
name|startTime
parameter_list|,
name|boolean
name|failed
parameter_list|)
block|{
name|HISTORY_LOGGER
operator|.
name|info
argument_list|(
name|constructFragmentEndString
argument_list|(
name|applicationIdStr
argument_list|,
name|containerIdStr
argument_list|,
name|hostname
argument_list|,
name|dagName
argument_list|,
name|vertexName
argument_list|,
name|taskId
argument_list|,
name|attemptId
argument_list|,
name|threadName
argument_list|,
name|startTime
argument_list|,
name|failed
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|constructFragmentStartString
parameter_list|(
name|String
name|applicationIdStr
parameter_list|,
name|String
name|containerIdStr
parameter_list|,
name|String
name|hostname
parameter_list|,
name|String
name|dagName
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|taskId
parameter_list|,
name|int
name|attemptId
parameter_list|)
block|{
name|HistoryLineBuilder
name|lb
init|=
operator|new
name|HistoryLineBuilder
argument_list|(
name|EVENT_TYPE_FRAGMENT_START
argument_list|)
decl_stmt|;
name|lb
operator|.
name|addHostName
argument_list|(
name|hostname
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addAppid
argument_list|(
name|applicationIdStr
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addContainerId
argument_list|(
name|containerIdStr
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addDagName
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addVertexName
argument_list|(
name|vertexName
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTaskId
argument_list|(
name|taskId
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTaskAttemptId
argument_list|(
name|attemptId
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTime
argument_list|(
name|HISTORY_SUBMIT_TIME
argument_list|)
expr_stmt|;
return|return
name|lb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|constructFragmentEndString
parameter_list|(
name|String
name|applicationIdStr
parameter_list|,
name|String
name|containerIdStr
parameter_list|,
name|String
name|hostname
parameter_list|,
name|String
name|dagName
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|taskId
parameter_list|,
name|int
name|attemptId
parameter_list|,
name|String
name|threadName
parameter_list|,
name|long
name|startTime
parameter_list|,
name|boolean
name|succeeded
parameter_list|)
block|{
name|HistoryLineBuilder
name|lb
init|=
operator|new
name|HistoryLineBuilder
argument_list|(
name|EVENT_TYPE_FRAGMENT_END
argument_list|)
decl_stmt|;
name|lb
operator|.
name|addHostName
argument_list|(
name|hostname
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addAppid
argument_list|(
name|applicationIdStr
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addContainerId
argument_list|(
name|containerIdStr
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addDagName
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addVertexName
argument_list|(
name|vertexName
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTaskId
argument_list|(
name|taskId
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTaskAttemptId
argument_list|(
name|attemptId
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addThreadName
argument_list|(
name|threadName
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addSuccessStatus
argument_list|(
name|succeeded
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTime
argument_list|(
name|HISTORY_START_TIME
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
name|lb
operator|.
name|addTime
argument_list|(
name|HISTORY_END_TIME
argument_list|)
expr_stmt|;
return|return
name|lb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|HistoryLineBuilder
block|{
specifier|private
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|HistoryLineBuilder
parameter_list|(
name|String
name|eventType
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|HISTORY_EVENT_TYPE
argument_list|)
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
operator|.
name|append
argument_list|(
name|eventType
argument_list|)
expr_stmt|;
block|}
name|HistoryLineBuilder
name|addHostName
parameter_list|(
name|String
name|hostname
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_HOSTNAME
argument_list|,
name|hostname
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addAppid
parameter_list|(
name|String
name|appId
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_APPLICATION_ID
argument_list|,
name|appId
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addContainerId
parameter_list|(
name|String
name|containerId
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_CONTAINER_ID
argument_list|,
name|containerId
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addDagName
parameter_list|(
name|String
name|dagName
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_DAG_NAME
argument_list|,
name|dagName
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addVertexName
parameter_list|(
name|String
name|vertexName
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_VERTEX_NAME
argument_list|,
name|vertexName
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addTaskId
parameter_list|(
name|int
name|taskId
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_TASK_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|taskId
argument_list|)
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addTaskAttemptId
parameter_list|(
name|int
name|attemptId
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_ATTEMPT_ID
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|attemptId
argument_list|)
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addThreadName
parameter_list|(
name|String
name|threadName
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_THREAD_NAME
argument_list|,
name|threadName
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addTime
parameter_list|(
name|String
name|timeParam
parameter_list|,
name|long
name|millis
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|timeParam
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|millis
argument_list|)
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addTime
parameter_list|(
name|String
name|timeParam
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|timeParam
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
name|HistoryLineBuilder
name|addSuccessStatus
parameter_list|(
name|boolean
name|status
parameter_list|)
block|{
return|return
name|setKeyValue
argument_list|(
name|HISTORY_SUCCEEDED
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|status
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HistoryLineBuilder
name|setKeyValue
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
operator|.
name|append
argument_list|(
name|key
argument_list|)
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

