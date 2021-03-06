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
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
package|;
end_package

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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|Utilities
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
name|log
operator|.
name|PerfLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|JobExecutionStatus
import|;
end_import

begin_comment
comment|/**  * LocalSparkJobMonitor monitor a single Spark job status in a loop until job finished/failed/killed.  * It print current job status to console and sleep current thread between monitor interval.  */
end_comment

begin_class
specifier|public
class|class
name|LocalSparkJobMonitor
extends|extends
name|SparkJobMonitor
block|{
specifier|private
name|SparkJobStatus
name|sparkJobStatus
decl_stmt|;
specifier|public
name|LocalSparkJobMonitor
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|SparkJobStatus
name|sparkJobStatus
parameter_list|)
block|{
name|super
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|sparkJobStatus
operator|=
name|sparkJobStatus
expr_stmt|;
block|}
specifier|public
name|int
name|startMonitor
parameter_list|()
block|{
name|boolean
name|running
init|=
literal|false
decl_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|int
name|rc
init|=
literal|0
decl_stmt|;
name|JobExecutionStatus
name|lastState
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|lastProgressMap
init|=
literal|null
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_RUN_JOB
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_SUBMIT_TO_RUNNING
argument_list|)
expr_stmt|;
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|JobExecutionStatus
name|state
init|=
name|sparkJobStatus
operator|.
name|getState
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"state = "
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
name|long
name|timeCount
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|timeCount
operator|>
name|monitorTimeoutInterval
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Job hasn't been submitted after "
operator|+
name|timeCount
operator|+
literal|"s. Aborting it."
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Status: "
operator|+
name|state
argument_list|)
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
name|rc
operator|=
literal|2
expr_stmt|;
break|break;
block|}
block|}
elseif|else
if|if
condition|(
name|state
operator|!=
name|lastState
operator|||
name|state
operator|==
name|JobExecutionStatus
operator|.
name|RUNNING
condition|)
block|{
name|lastState
operator|=
name|state
expr_stmt|;
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
init|=
name|sparkJobStatus
operator|.
name|getSparkStageProgress
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|RUNNING
case|:
if|if
condition|(
operator|!
name|running
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_SUBMIT_TO_RUNNING
argument_list|)
expr_stmt|;
comment|// print job stages.
name|console
operator|.
name|printInfo
argument_list|(
literal|"\nQuery Hive on Spark job["
operator|+
name|sparkJobStatus
operator|.
name|getJobId
argument_list|()
operator|+
literal|"] stages:"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|stageId
range|:
name|sparkJobStatus
operator|.
name|getStageIds
argument_list|()
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|stageId
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|"\nStatus: Running (Hive on Spark job["
operator|+
name|sparkJobStatus
operator|.
name|getJobId
argument_list|()
operator|+
literal|"])"
argument_list|)
expr_stmt|;
name|running
operator|=
literal|true
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Job Progress Format\nCurrentTime StageId_StageAttemptId: "
operator|+
literal|"SucceededTasksCount(+RunningTasksCount-FailedTasksCount)/TotalTasksCount [StageCost]"
argument_list|)
expr_stmt|;
block|}
name|updateFunction
operator|.
name|printStatus
argument_list|(
name|progressMap
argument_list|,
name|lastProgressMap
argument_list|)
expr_stmt|;
name|lastProgressMap
operator|=
name|progressMap
expr_stmt|;
break|break;
case|case
name|SUCCEEDED
case|:
name|updateFunction
operator|.
name|printStatus
argument_list|(
name|progressMap
argument_list|,
name|lastProgressMap
argument_list|)
expr_stmt|;
name|lastProgressMap
operator|=
name|progressMap
expr_stmt|;
name|double
name|duration
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000.0
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Finished successfully in "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%.2f seconds"
argument_list|,
name|duration
argument_list|)
argument_list|)
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|FAILED
case|:
name|console
operator|.
name|printError
argument_list|(
literal|"Status: Failed"
argument_list|)
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
name|rc
operator|=
literal|3
expr_stmt|;
break|break;
case|case
name|UNKNOWN
case|:
name|console
operator|.
name|printError
argument_list|(
literal|"Status: Unknown"
argument_list|)
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
name|rc
operator|=
literal|4
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|done
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|checkInterval
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|" with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
name|msg
operator|=
literal|"Failed to monitor Job[ "
operator|+
name|sparkJobStatus
operator|.
name|getJobId
argument_list|()
operator|+
literal|"]"
operator|+
name|msg
expr_stmt|;
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang3.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
name|msg
argument_list|,
literal|"\n"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|rc
operator|=
literal|1
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
name|sparkJobStatus
operator|.
name|setMonitorError
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|done
condition|)
block|{
break|break;
block|}
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_RUN_JOB
argument_list|)
expr_stmt|;
return|return
name|rc
return|;
block|}
block|}
end_class

end_unit

