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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|LogUtils
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
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|ql
operator|.
name|exec
operator|.
name|TaskResult
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
name|ql
operator|.
name|plan
operator|.
name|api
operator|.
name|StageType
import|;
end_import

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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|Counters
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
name|RunningJob
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnoreProperties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonWriteNullProperties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_comment
comment|/**  * Some limited query information to save for WebUI.  *  * The class is synchronized, as WebUI may access information about a running query.  */
end_comment

begin_class
specifier|public
class|class
name|QueryDisplay
block|{
comment|// Member variables
specifier|private
name|String
name|queryStr
decl_stmt|;
specifier|private
name|String
name|explainPlan
decl_stmt|;
specifier|private
name|String
name|errorMessage
decl_stmt|;
specifier|private
name|String
name|queryId
decl_stmt|;
specifier|private
name|long
name|queryStartTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|hmsTimingMap
init|=
operator|new
name|HashMap
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|perfLogStartMap
init|=
operator|new
name|HashMap
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
name|perfLogEndMap
init|=
operator|new
name|HashMap
argument_list|<
name|Phase
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|TaskDisplay
argument_list|>
name|tasks
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|TaskDisplay
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|synchronized
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|void
name|updateTaskStatus
parameter_list|(
name|Task
argument_list|<
name|T
argument_list|>
name|tTask
parameter_list|)
block|{
if|if
condition|(
operator|!
name|tasks
operator|.
name|containsKey
argument_list|(
name|tTask
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
name|tasks
operator|.
name|put
argument_list|(
name|tTask
operator|.
name|getId
argument_list|()
argument_list|,
operator|new
name|TaskDisplay
argument_list|(
name|tTask
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tasks
operator|.
name|get
argument_list|(
name|tTask
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|updateStatus
argument_list|(
name|tTask
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|void
name|updateTaskStatistics
parameter_list|(
name|MapRedStats
name|mapRedStats
parameter_list|,
name|RunningJob
name|rj
parameter_list|,
name|String
name|taskId
parameter_list|)
throws|throws
name|IOException
throws|,
name|JSONException
block|{
if|if
condition|(
name|tasks
operator|.
name|containsKey
argument_list|(
name|taskId
argument_list|)
condition|)
block|{
name|tasks
operator|.
name|get
argument_list|(
name|taskId
argument_list|)
operator|.
name|updateMapRedStatsJson
argument_list|(
name|mapRedStats
argument_list|,
name|rj
argument_list|)
expr_stmt|;
block|}
block|}
comment|//Inner classes
specifier|public
enum|enum
name|Phase
block|{
name|COMPILATION
block|,
name|EXECUTION
block|,   }
specifier|public
name|String
name|getFullLogLocation
parameter_list|()
block|{
return|return
name|LogUtils
operator|.
name|getLogFilePath
argument_list|()
return|;
block|}
annotation|@
name|JsonWriteNullProperties
argument_list|(
literal|false
argument_list|)
annotation|@
name|JsonIgnoreProperties
argument_list|(
name|ignoreUnknown
operator|=
literal|true
argument_list|)
specifier|public
specifier|static
class|class
name|TaskDisplay
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NUMBER_OF_MAPPERS
init|=
literal|"Number of Mappers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NUMBER_OF_REDUCERS
init|=
literal|"Number of Reducers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COUNTERS
init|=
literal|"Counters"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JOB_ID
init|=
literal|"Job Id"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JOB_FILE
init|=
literal|"Job File"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TRACKING_URL
init|=
literal|"Tracking URL"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAP_PROGRESS
init|=
literal|"Map Progress (%)"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REDUCE_PROGRESS
init|=
literal|"Reduce Progress (%)"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CLEANUP_PROGRESS
init|=
literal|"Cleanup Progress (%)"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SETUP_PROGRESS
init|=
literal|"Setup Progress (%)"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COMPLETE
init|=
literal|"Complete"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SUCCESSFUL
init|=
literal|"Successful"
decl_stmt|;
specifier|private
name|Integer
name|returnValue
decl_stmt|;
comment|//if set, determines that task is complete.
specifier|private
name|String
name|errorMsg
decl_stmt|;
specifier|private
name|Long
name|beginTime
decl_stmt|;
specifier|private
name|Long
name|endTime
decl_stmt|;
specifier|private
name|String
name|taskId
decl_stmt|;
specifier|private
name|String
name|externalHandle
decl_stmt|;
specifier|public
name|Task
operator|.
name|TaskState
name|taskState
decl_stmt|;
specifier|private
name|StageType
name|taskType
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|boolean
name|requireLock
decl_stmt|;
specifier|private
name|String
name|statusMessage
decl_stmt|;
specifier|private
name|JSONObject
name|statsJSON
decl_stmt|;
comment|// required for jackson
specifier|public
name|TaskDisplay
parameter_list|()
block|{      }
specifier|public
name|TaskDisplay
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
name|taskId
operator|=
name|task
operator|.
name|getId
argument_list|()
expr_stmt|;
name|externalHandle
operator|=
name|task
operator|.
name|getExternalHandle
argument_list|()
expr_stmt|;
name|taskType
operator|=
name|task
operator|.
name|getType
argument_list|()
expr_stmt|;
name|name
operator|=
name|task
operator|.
name|getName
argument_list|()
expr_stmt|;
name|requireLock
operator|=
name|task
operator|.
name|requireLock
argument_list|()
expr_stmt|;
block|}
annotation|@
name|JsonIgnore
specifier|public
specifier|synchronized
name|String
name|getStatus
parameter_list|()
block|{
if|if
condition|(
name|returnValue
operator|==
literal|null
condition|)
block|{
return|return
literal|"Running"
return|;
block|}
elseif|else
if|if
condition|(
name|returnValue
operator|==
literal|0
condition|)
block|{
return|return
literal|"Success, ReturnVal 0"
return|;
block|}
else|else
block|{
return|return
literal|"Failure, ReturnVal "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|returnValue
argument_list|)
return|;
block|}
block|}
specifier|private
name|void
name|updateMapRedStatsJson
parameter_list|(
name|MapRedStats
name|stats
parameter_list|,
name|RunningJob
name|rj
parameter_list|)
throws|throws
name|IOException
throws|,
name|JSONException
block|{
if|if
condition|(
name|statsJSON
operator|==
literal|null
condition|)
block|{
name|statsJSON
operator|=
operator|new
name|JSONObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|stats
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|stats
operator|.
name|getNumMap
argument_list|()
operator|>=
literal|0
condition|)
block|{
name|statsJSON
operator|.
name|put
argument_list|(
name|NUMBER_OF_MAPPERS
argument_list|,
name|stats
operator|.
name|getNumMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stats
operator|.
name|getNumReduce
argument_list|()
operator|>=
literal|0
condition|)
block|{
name|statsJSON
operator|.
name|put
argument_list|(
name|NUMBER_OF_REDUCERS
argument_list|,
name|stats
operator|.
name|getNumReduce
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stats
operator|.
name|getCounters
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|statsJSON
operator|.
name|put
argument_list|(
name|COUNTERS
argument_list|,
name|getCountersJson
argument_list|(
name|stats
operator|.
name|getCounters
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|rj
operator|!=
literal|null
condition|)
block|{
name|statsJSON
operator|.
name|put
argument_list|(
name|JOB_ID
argument_list|,
name|rj
operator|.
name|getID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|JOB_FILE
argument_list|,
name|rj
operator|.
name|getJobFile
argument_list|()
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|TRACKING_URL
argument_list|,
name|rj
operator|.
name|getTrackingURL
argument_list|()
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|MAP_PROGRESS
argument_list|,
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|mapProgress
argument_list|()
operator|*
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|REDUCE_PROGRESS
argument_list|,
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|reduceProgress
argument_list|()
operator|*
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|CLEANUP_PROGRESS
argument_list|,
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|cleanupProgress
argument_list|()
operator|*
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|SETUP_PROGRESS
argument_list|,
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|setupProgress
argument_list|()
operator|*
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|COMPLETE
argument_list|,
name|rj
operator|.
name|isComplete
argument_list|()
argument_list|)
expr_stmt|;
name|statsJSON
operator|.
name|put
argument_list|(
name|SUCCESSFUL
argument_list|,
name|rj
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|String
name|getStatsJsonString
parameter_list|()
block|{
if|if
condition|(
name|statsJSON
operator|!=
literal|null
condition|)
block|{
return|return
name|statsJSON
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|JSONObject
name|getCountersJson
parameter_list|(
name|Counters
name|ctrs
parameter_list|)
throws|throws
name|JSONException
block|{
name|JSONObject
name|countersJson
init|=
operator|new
name|JSONObject
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Counters
operator|.
name|Group
argument_list|>
name|iterator
init|=
name|ctrs
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Counters
operator|.
name|Group
name|group
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Counters
operator|.
name|Counter
argument_list|>
name|groupIterator
init|=
name|group
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|JSONObject
name|groupJson
init|=
operator|new
name|JSONObject
argument_list|()
decl_stmt|;
while|while
condition|(
name|groupIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Counters
operator|.
name|Counter
name|counter
init|=
name|groupIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|groupJson
operator|.
name|put
argument_list|(
name|counter
operator|.
name|getDisplayName
argument_list|()
argument_list|,
name|counter
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|countersJson
operator|.
name|put
argument_list|(
name|group
operator|.
name|getDisplayName
argument_list|()
argument_list|,
name|groupJson
argument_list|)
expr_stmt|;
block|}
return|return
name|countersJson
return|;
block|}
specifier|public
specifier|synchronized
name|Long
name|getElapsedTime
parameter_list|()
block|{
if|if
condition|(
name|endTime
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|beginTime
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|Integer
name|getReturnValue
parameter_list|()
block|{
return|return
name|returnValue
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getErrorMsg
parameter_list|()
block|{
return|return
name|errorMsg
return|;
block|}
specifier|public
specifier|synchronized
name|Long
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
name|String
name|getTaskId
parameter_list|()
block|{
return|return
name|taskId
return|;
block|}
specifier|public
specifier|synchronized
name|StageType
name|getTaskType
parameter_list|()
block|{
return|return
name|taskType
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|JsonIgnore
specifier|public
specifier|synchronized
name|boolean
name|isRequireLock
parameter_list|()
block|{
return|return
name|requireLock
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getExternalHandle
parameter_list|()
block|{
return|return
name|externalHandle
return|;
block|}
specifier|public
specifier|synchronized
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|void
name|updateStatus
parameter_list|(
name|Task
argument_list|<
name|T
argument_list|>
name|tTask
parameter_list|)
block|{
name|this
operator|.
name|taskState
operator|=
name|tTask
operator|.
name|getTaskState
argument_list|()
expr_stmt|;
if|if
condition|(
name|externalHandle
operator|==
literal|null
operator|&&
name|tTask
operator|.
name|getExternalHandle
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|externalHandle
operator|=
name|tTask
operator|.
name|getExternalHandle
argument_list|()
expr_stmt|;
block|}
name|setStatusMessage
argument_list|(
name|tTask
operator|.
name|getStatusMessage
argument_list|()
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|taskState
condition|)
block|{
case|case
name|RUNNING
case|:
if|if
condition|(
name|beginTime
operator|==
literal|null
condition|)
block|{
name|beginTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|FINISHED
case|:
if|if
condition|(
name|endTime
operator|==
literal|null
condition|)
block|{
name|endTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
break|break;
block|}
block|}
specifier|public
specifier|synchronized
name|String
name|getStatusMessage
parameter_list|()
block|{
return|return
name|statusMessage
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setStatusMessage
parameter_list|(
name|String
name|statusMessage
parameter_list|)
block|{
name|this
operator|.
name|statusMessage
operator|=
name|statusMessage
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|setTaskResult
parameter_list|(
name|String
name|taskId
parameter_list|,
name|TaskResult
name|result
parameter_list|)
block|{
name|TaskDisplay
name|taskDisplay
init|=
name|tasks
operator|.
name|get
argument_list|(
name|taskId
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskDisplay
operator|!=
literal|null
condition|)
block|{
name|taskDisplay
operator|.
name|returnValue
operator|=
name|result
operator|.
name|getExitVal
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|.
name|getTaskError
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|taskDisplay
operator|.
name|errorMsg
operator|=
name|result
operator|.
name|getTaskError
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|synchronized
name|List
argument_list|<
name|TaskDisplay
argument_list|>
name|getTaskDisplays
parameter_list|()
block|{
name|List
argument_list|<
name|TaskDisplay
argument_list|>
name|taskDisplays
init|=
operator|new
name|ArrayList
argument_list|<
name|TaskDisplay
argument_list|>
argument_list|()
decl_stmt|;
name|taskDisplays
operator|.
name|addAll
argument_list|(
name|tasks
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|taskDisplays
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setQueryStr
parameter_list|(
name|String
name|queryStr
parameter_list|)
block|{
name|this
operator|.
name|queryStr
operator|=
name|queryStr
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|String
name|getQueryString
parameter_list|()
block|{
return|return
name|returnStringOrUnknown
argument_list|(
name|queryStr
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getExplainPlan
parameter_list|()
block|{
return|return
name|returnStringOrUnknown
argument_list|(
name|explainPlan
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setExplainPlan
parameter_list|(
name|String
name|explainPlan
parameter_list|)
block|{
name|this
operator|.
name|explainPlan
operator|=
name|explainPlan
expr_stmt|;
block|}
comment|/**    * @param phase phase of query    * @return map of HMS Client method-calls and duration in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getHmsTimings
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
return|return
name|hmsTimingMap
operator|.
name|get
argument_list|(
name|phase
argument_list|)
return|;
block|}
comment|/**    * @param phase phase of query    * @param hmsTimings map of HMS Client method-calls and duration in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|void
name|setHmsTimings
parameter_list|(
name|Phase
name|phase
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|hmsTimings
parameter_list|)
block|{
name|hmsTimingMap
operator|.
name|put
argument_list|(
name|phase
argument_list|,
name|hmsTimings
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param phase phase of query    * @return map of PerfLogger call-trace name and start time in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getPerfLogStarts
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
return|return
name|perfLogStartMap
operator|.
name|get
argument_list|(
name|phase
argument_list|)
return|;
block|}
comment|/**    * @param phase phase of query    * @param perfLogStarts map of PerfLogger call-trace name and start time in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|void
name|setPerfLogStarts
parameter_list|(
name|Phase
name|phase
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|perfLogStarts
parameter_list|)
block|{
name|perfLogStartMap
operator|.
name|put
argument_list|(
name|phase
argument_list|,
name|perfLogStarts
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param phase phase of query    * @return map of PerfLogger call-trace name and end time in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getPerfLogEnds
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
return|return
name|perfLogEndMap
operator|.
name|get
argument_list|(
name|phase
argument_list|)
return|;
block|}
comment|/**    * @param phase phase of query    * @param perfLogEnds map of PerfLogger call-trace name and end time in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|void
name|setPerfLogEnds
parameter_list|(
name|Phase
name|phase
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|perfLogEnds
parameter_list|)
block|{
name|perfLogEndMap
operator|.
name|put
argument_list|(
name|phase
argument_list|,
name|perfLogEnds
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param phase phase of query    * @return map of PerfLogger call-trace name and duration in miliseconds, during given phase.    */
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getPerfLogTimes
parameter_list|(
name|Phase
name|phase
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|times
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|startTimes
init|=
name|perfLogStartMap
operator|.
name|get
argument_list|(
name|phase
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|endTimes
init|=
name|perfLogEndMap
operator|.
name|get
argument_list|(
name|phase
argument_list|)
decl_stmt|;
if|if
condition|(
name|endTimes
operator|!=
literal|null
operator|&&
name|startTimes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|timeKey
range|:
name|endTimes
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Long
name|endTime
init|=
name|endTimes
operator|.
name|get
argument_list|(
name|timeKey
argument_list|)
decl_stmt|;
name|Long
name|startTime
init|=
name|startTimes
operator|.
name|get
argument_list|(
name|timeKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|startTime
operator|!=
literal|null
condition|)
block|{
name|times
operator|.
name|put
argument_list|(
name|timeKey
argument_list|,
name|endTime
operator|-
name|startTime
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|times
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getErrorMessage
parameter_list|()
block|{
return|return
name|errorMessage
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setErrorMessage
parameter_list|(
name|String
name|errorMessage
parameter_list|)
block|{
name|this
operator|.
name|errorMessage
operator|=
name|errorMessage
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|String
name|getQueryId
parameter_list|()
block|{
return|return
name|returnStringOrUnknown
argument_list|(
name|queryId
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setQueryId
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
block|}
specifier|private
name|String
name|returnStringOrUnknown
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|s
operator|==
literal|null
condition|?
literal|"UNKNOWN"
else|:
name|s
return|;
block|}
specifier|public
name|long
name|getQueryStartTime
parameter_list|()
block|{
return|return
name|queryStartTime
return|;
block|}
block|}
end_class

end_unit

