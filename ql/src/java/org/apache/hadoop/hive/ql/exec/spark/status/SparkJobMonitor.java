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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
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

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_class
specifier|abstract
class|class
name|SparkJobMonitor
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkJobMonitor
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
specifier|transient
specifier|final
name|SessionState
operator|.
name|LogHelper
name|console
decl_stmt|;
specifier|protected
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|int
name|checkInterval
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|final
name|long
name|monitorTimeoutInterval
decl_stmt|;
specifier|private
specifier|final
name|InPlaceUpdate
name|inPlaceUpdateFn
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|completed
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|printInterval
init|=
literal|3000
decl_stmt|;
specifier|private
name|long
name|lastPrintTime
decl_stmt|;
specifier|protected
name|long
name|startTime
decl_stmt|;
specifier|protected
enum|enum
name|StageState
block|{
name|PENDING
block|,
name|RUNNING
block|,
name|FINISHED
block|}
specifier|protected
specifier|final
name|boolean
name|inPlaceUpdate
decl_stmt|;
specifier|protected
name|SparkJobMonitor
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|monitorTimeoutInterval
operator|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_JOB_MONITOR_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|inPlaceUpdate
operator|=
name|InPlaceUpdate
operator|.
name|canRenderInPlace
argument_list|(
name|hiveConf
argument_list|)
operator|&&
operator|!
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|getIsSilent
argument_list|()
expr_stmt|;
name|console
operator|=
operator|new
name|SessionState
operator|.
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
name|inPlaceUpdateFn
operator|=
operator|new
name|InPlaceUpdate
argument_list|(
name|SessionState
operator|.
name|LogHelper
operator|.
name|getInfoStream
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|int
name|startMonitor
parameter_list|()
function_decl|;
specifier|private
name|void
name|printStatusInPlace
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|)
block|{
name|inPlaceUpdateFn
operator|.
name|render
argument_list|(
name|getProgressMonitor
argument_list|(
name|progressMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|printStatus
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|,
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|lastProgressMap
parameter_list|)
block|{
comment|// do not print duplicate status while still in middle of print interval.
name|boolean
name|isDuplicateState
init|=
name|isSameAsPreviousProgress
argument_list|(
name|progressMap
argument_list|,
name|lastProgressMap
argument_list|)
decl_stmt|;
name|boolean
name|withinInterval
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<=
name|lastPrintTime
operator|+
name|printInterval
decl_stmt|;
if|if
condition|(
name|isDuplicateState
operator|&&
name|withinInterval
condition|)
block|{
return|return;
block|}
name|String
name|report
init|=
name|getReport
argument_list|(
name|progressMap
argument_list|)
decl_stmt|;
if|if
condition|(
name|inPlaceUpdate
condition|)
block|{
name|printStatusInPlace
argument_list|(
name|progressMap
argument_list|)
expr_stmt|;
name|console
operator|.
name|logInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
name|lastPrintTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|int
name|getTotalTaskCount
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|)
block|{
name|int
name|totalTasks
init|=
literal|0
decl_stmt|;
for|for
control|(
name|SparkStageProgress
name|progress
range|:
name|progressMap
operator|.
name|values
argument_list|()
control|)
block|{
name|totalTasks
operator|+=
name|progress
operator|.
name|getTotalTaskCount
argument_list|()
expr_stmt|;
block|}
return|return
name|totalTasks
return|;
block|}
specifier|protected
name|int
name|getStageMaxTaskCount
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|)
block|{
name|int
name|stageMaxTasks
init|=
literal|0
decl_stmt|;
for|for
control|(
name|SparkStageProgress
name|progress
range|:
name|progressMap
operator|.
name|values
argument_list|()
control|)
block|{
name|int
name|tasks
init|=
name|progress
operator|.
name|getTotalTaskCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|tasks
operator|>
name|stageMaxTasks
condition|)
block|{
name|stageMaxTasks
operator|=
name|tasks
expr_stmt|;
block|}
block|}
return|return
name|stageMaxTasks
return|;
block|}
specifier|private
name|String
name|getReport
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|)
block|{
name|StringBuilder
name|reportBuffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|SimpleDateFormat
name|dt
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss,SSS"
argument_list|)
decl_stmt|;
name|String
name|currentDate
init|=
name|dt
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
decl_stmt|;
name|reportBuffer
operator|.
name|append
argument_list|(
name|currentDate
operator|+
literal|"\t"
argument_list|)
expr_stmt|;
comment|// Num of total and completed tasks
name|int
name|sumTotal
init|=
literal|0
decl_stmt|;
name|int
name|sumComplete
init|=
literal|0
decl_stmt|;
name|SortedSet
argument_list|<
name|SparkStage
argument_list|>
name|keys
init|=
operator|new
name|TreeSet
argument_list|<
name|SparkStage
argument_list|>
argument_list|(
name|progressMap
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|SparkStage
name|stage
range|:
name|keys
control|)
block|{
name|SparkStageProgress
name|progress
init|=
name|progressMap
operator|.
name|get
argument_list|(
name|stage
argument_list|)
decl_stmt|;
specifier|final
name|int
name|complete
init|=
name|progress
operator|.
name|getSucceededTaskCount
argument_list|()
decl_stmt|;
specifier|final
name|int
name|total
init|=
name|progress
operator|.
name|getTotalTaskCount
argument_list|()
decl_stmt|;
specifier|final
name|int
name|running
init|=
name|progress
operator|.
name|getRunningTaskCount
argument_list|()
decl_stmt|;
specifier|final
name|int
name|failed
init|=
name|progress
operator|.
name|getFailedTaskCount
argument_list|()
decl_stmt|;
name|sumTotal
operator|+=
name|total
expr_stmt|;
name|sumComplete
operator|+=
name|complete
expr_stmt|;
name|String
name|s
init|=
name|stage
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|stageName
init|=
literal|"Stage-"
operator|+
name|s
decl_stmt|;
if|if
condition|(
name|total
operator|<=
literal|0
condition|)
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: -/-\t"
argument_list|,
name|stageName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|complete
operator|==
name|total
operator|&&
operator|!
name|completed
operator|.
name|contains
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|completed
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|perfLogger
operator|.
name|startTimeHasMethod
argument_list|(
name|PerfLogger
operator|.
name|SPARK_RUN_STAGE
operator|+
name|s
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_RUN_STAGE
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_RUN_STAGE
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|complete
operator|<
name|total
operator|&&
operator|(
name|complete
operator|>
literal|0
operator|||
name|running
operator|>
literal|0
operator|||
name|failed
operator|>
literal|0
operator|)
condition|)
block|{
comment|/* stage is started, but not complete */
if|if
condition|(
operator|!
name|perfLogger
operator|.
name|startTimeHasMethod
argument_list|(
name|PerfLogger
operator|.
name|SPARK_RUN_STAGE
operator|+
name|s
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_RUN_STAGE
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|failed
operator|>
literal|0
condition|)
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %d(+%d,-%d)/%d\t"
argument_list|,
name|stageName
argument_list|,
name|complete
argument_list|,
name|running
argument_list|,
name|failed
argument_list|,
name|total
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %d(+%d)/%d\t"
argument_list|,
name|stageName
argument_list|,
name|complete
argument_list|,
name|running
argument_list|,
name|total
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|/* stage is waiting for input/slots or complete */
if|if
condition|(
name|failed
operator|>
literal|0
condition|)
block|{
comment|/* tasks finished but some failed */
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %d(-%d)/%d Finished with failed tasks\t"
argument_list|,
name|stageName
argument_list|,
name|complete
argument_list|,
name|failed
argument_list|,
name|total
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|complete
operator|==
name|total
condition|)
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %d/%d Finished\t"
argument_list|,
name|stageName
argument_list|,
name|complete
argument_list|,
name|total
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %d/%d\t"
argument_list|,
name|stageName
argument_list|,
name|complete
argument_list|,
name|total
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|float
name|progress
init|=
operator|(
name|sumTotal
operator|==
literal|0
operator|)
condition|?
literal|1.0f
else|:
operator|(
name|float
operator|)
name|sumComplete
operator|/
operator|(
name|float
operator|)
name|sumTotal
decl_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|updateProgressedPercentage
argument_list|(
name|progress
argument_list|)
expr_stmt|;
block|}
return|return
name|reportBuffer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|isSameAsPreviousProgress
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|,
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|lastProgressMap
parameter_list|)
block|{
if|if
condition|(
name|lastProgressMap
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|progressMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|lastProgressMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
else|else
block|{
if|if
condition|(
name|lastProgressMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
if|if
condition|(
name|progressMap
operator|.
name|size
argument_list|()
operator|!=
name|lastProgressMap
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|SparkStage
name|key
range|:
name|progressMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|lastProgressMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
operator|||
operator|!
name|progressMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|equals
argument_list|(
name|lastProgressMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|SparkProgressMonitor
name|getProgressMonitor
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|)
block|{
return|return
operator|new
name|SparkProgressMonitor
argument_list|(
name|progressMap
argument_list|,
name|startTime
argument_list|)
return|;
block|}
block|}
end_class

end_unit

