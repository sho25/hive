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
name|ql
operator|.
name|exec
operator|.
name|InPlaceUpdates
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
name|fusesource
operator|.
name|jansi
operator|.
name|Ansi
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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DecimalFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
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

begin_import
import|import static
name|org
operator|.
name|fusesource
operator|.
name|jansi
operator|.
name|Ansi
operator|.
name|ansi
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
comment|// in-place progress update related variables
specifier|protected
specifier|final
name|boolean
name|inPlaceUpdate
decl_stmt|;
specifier|private
name|int
name|lines
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|PrintStream
name|out
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COLUMN_1_WIDTH
init|=
literal|16
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HEADER_FORMAT
init|=
literal|"%16s%10s %13s  %5s  %9s  %7s  %7s  %6s  "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|STAGE_FORMAT
init|=
literal|"%-16s%10s %13s  %5s  %9s  %7s  %7s  %6s  "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HEADER
init|=
name|String
operator|.
name|format
argument_list|(
name|HEADER_FORMAT
argument_list|,
literal|"STAGES"
argument_list|,
literal|"ATTEMPT"
argument_list|,
literal|"STATUS"
argument_list|,
literal|"TOTAL"
argument_list|,
literal|"COMPLETED"
argument_list|,
literal|"RUNNING"
argument_list|,
literal|"PENDING"
argument_list|,
literal|"FAILED"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SEPARATOR_WIDTH
init|=
literal|86
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SEPARATOR
init|=
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
name|SEPARATOR_WIDTH
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"-"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FOOTER_FORMAT
init|=
literal|"%-15s  %-30s %-4s  %-25s"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|progressBarChars
init|=
literal|30
decl_stmt|;
specifier|private
specifier|final
name|NumberFormat
name|secondsFormat
init|=
operator|new
name|DecimalFormat
argument_list|(
literal|"#0.00"
argument_list|)
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
name|InPlaceUpdates
operator|.
name|inPlaceEligible
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|console
operator|=
name|SessionState
operator|.
name|getConsole
argument_list|()
expr_stmt|;
name|out
operator|=
name|SessionState
operator|.
name|LogHelper
operator|.
name|getInfoStream
argument_list|()
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
name|String
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
comment|// position the cursor to line 0
name|repositionCursor
argument_list|()
expr_stmt|;
comment|// header
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|reprintLineWithColorAsBold
argument_list|(
name|HEADER
argument_list|,
name|Ansi
operator|.
name|Color
operator|.
name|CYAN
argument_list|)
expr_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|progressMap
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
specifier|final
name|int
name|numKey
init|=
name|keys
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|s
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
name|s
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
name|StageState
name|state
init|=
name|total
operator|>
literal|0
condition|?
name|StageState
operator|.
name|PENDING
else|:
name|StageState
operator|.
name|FINISHED
decl_stmt|;
if|if
condition|(
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
condition|)
block|{
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
name|complete
operator|<
name|total
condition|)
block|{
name|state
operator|=
name|StageState
operator|.
name|RUNNING
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|StageState
operator|.
name|FINISHED
expr_stmt|;
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
name|completed
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|div
init|=
name|s
operator|.
name|indexOf
argument_list|(
literal|'_'
argument_list|)
decl_stmt|;
name|String
name|attempt
init|=
name|div
operator|>
literal|0
condition|?
name|s
operator|.
name|substring
argument_list|(
name|div
operator|+
literal|1
argument_list|)
else|:
literal|"-"
decl_stmt|;
name|String
name|stageName
init|=
literal|"Stage-"
operator|+
operator|(
name|div
operator|>
literal|0
condition|?
name|s
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|div
argument_list|)
else|:
name|s
operator|)
decl_stmt|;
name|String
name|nameWithProgress
init|=
name|getNameWithProgress
argument_list|(
name|stageName
argument_list|,
name|complete
argument_list|,
name|total
argument_list|)
decl_stmt|;
specifier|final
name|int
name|pending
init|=
name|total
operator|-
name|complete
operator|-
name|running
decl_stmt|;
name|String
name|stageStr
init|=
name|String
operator|.
name|format
argument_list|(
name|STAGE_FORMAT
argument_list|,
name|nameWithProgress
argument_list|,
name|attempt
argument_list|,
name|state
argument_list|,
name|total
argument_list|,
name|complete
argument_list|,
name|running
argument_list|,
name|pending
argument_list|,
name|failed
argument_list|)
decl_stmt|;
name|reportBuffer
operator|.
name|append
argument_list|(
name|stageStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|idx
operator|++
operator|!=
name|numKey
operator|-
literal|1
condition|)
block|{
name|reportBuffer
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
name|reprintMultiLine
argument_list|(
name|reportBuffer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
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
name|String
name|footer
init|=
name|getFooter
argument_list|(
name|numKey
argument_list|,
name|completed
operator|.
name|size
argument_list|()
argument_list|,
name|progress
argument_list|,
name|startTime
argument_list|)
decl_stmt|;
name|reprintLineWithColorAsBold
argument_list|(
name|footer
argument_list|,
name|Ansi
operator|.
name|Color
operator|.
name|RED
argument_list|)
expr_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|printStatus
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|,
name|Map
argument_list|<
name|String
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
specifier|private
name|String
name|getReport
parameter_list|(
name|Map
argument_list|<
name|String
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
name|SortedSet
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|TreeSet
argument_list|<
name|String
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
name|String
name|s
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
name|s
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
name|String
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|,
name|Map
argument_list|<
name|String
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
name|String
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
name|void
name|repositionCursor
parameter_list|()
block|{
if|if
condition|(
name|lines
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|cursorUp
argument_list|(
name|lines
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|=
literal|0
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|reprintLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|InPlaceUpdates
operator|.
name|reprintLine
argument_list|(
name|out
argument_list|,
name|line
argument_list|)
expr_stmt|;
name|lines
operator|++
expr_stmt|;
block|}
specifier|private
name|void
name|reprintLineWithColorAsBold
parameter_list|(
name|String
name|line
parameter_list|,
name|Ansi
operator|.
name|Color
name|color
parameter_list|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|eraseLine
argument_list|(
name|Ansi
operator|.
name|Erase
operator|.
name|ALL
argument_list|)
operator|.
name|fg
argument_list|(
name|color
argument_list|)
operator|.
name|bold
argument_list|()
operator|.
name|a
argument_list|(
name|line
argument_list|)
operator|.
name|a
argument_list|(
literal|'\n'
argument_list|)
operator|.
name|boldOff
argument_list|()
operator|.
name|reset
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|++
expr_stmt|;
block|}
specifier|private
name|String
name|getNameWithProgress
parameter_list|(
name|String
name|s
parameter_list|,
name|int
name|complete
parameter_list|,
name|int
name|total
parameter_list|)
block|{
name|String
name|result
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
name|float
name|percent
init|=
name|total
operator|==
literal|0
condition|?
literal|1.0f
else|:
operator|(
name|float
operator|)
name|complete
operator|/
operator|(
name|float
operator|)
name|total
decl_stmt|;
comment|// lets use the remaining space in column 1 as progress bar
name|int
name|spaceRemaining
init|=
name|COLUMN_1_WIDTH
operator|-
name|s
operator|.
name|length
argument_list|()
operator|-
literal|1
decl_stmt|;
name|String
name|trimmedVName
init|=
name|s
decl_stmt|;
comment|// if the vertex name is longer than column 1 width, trim it down
if|if
condition|(
name|s
operator|.
name|length
argument_list|()
operator|>
name|COLUMN_1_WIDTH
condition|)
block|{
name|trimmedVName
operator|=
name|s
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|COLUMN_1_WIDTH
operator|-
literal|2
argument_list|)
expr_stmt|;
name|result
operator|=
name|trimmedVName
operator|+
literal|".."
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|trimmedVName
operator|+
literal|" "
expr_stmt|;
block|}
name|int
name|toFill
init|=
call|(
name|int
call|)
argument_list|(
name|spaceRemaining
operator|*
name|percent
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|toFill
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|+=
literal|"."
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|// STAGES: 03/04            [==================>>-----] 86%  ELAPSED TIME: 1.71 s
specifier|private
name|String
name|getFooter
parameter_list|(
name|int
name|keySize
parameter_list|,
name|int
name|completedSize
parameter_list|,
name|float
name|progress
parameter_list|,
name|long
name|startTime
parameter_list|)
block|{
name|String
name|verticesSummary
init|=
name|String
operator|.
name|format
argument_list|(
literal|"STAGES: %02d/%02d"
argument_list|,
name|completedSize
argument_list|,
name|keySize
argument_list|)
decl_stmt|;
name|String
name|progressBar
init|=
name|getInPlaceProgressBar
argument_list|(
name|progress
argument_list|)
decl_stmt|;
specifier|final
name|int
name|progressPercent
init|=
call|(
name|int
call|)
argument_list|(
name|progress
operator|*
literal|100
argument_list|)
decl_stmt|;
name|String
name|progressStr
init|=
literal|""
operator|+
name|progressPercent
operator|+
literal|"%"
decl_stmt|;
name|float
name|et
init|=
call|(
name|float
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
argument_list|)
operator|/
operator|(
name|float
operator|)
literal|1000
decl_stmt|;
name|String
name|elapsedTime
init|=
literal|"ELAPSED TIME: "
operator|+
name|secondsFormat
operator|.
name|format
argument_list|(
name|et
argument_list|)
operator|+
literal|" s"
decl_stmt|;
name|String
name|footer
init|=
name|String
operator|.
name|format
argument_list|(
name|FOOTER_FORMAT
argument_list|,
name|verticesSummary
argument_list|,
name|progressBar
argument_list|,
name|progressStr
argument_list|,
name|elapsedTime
argument_list|)
decl_stmt|;
return|return
name|footer
return|;
block|}
comment|// [==================>>-----]
specifier|private
name|String
name|getInPlaceProgressBar
parameter_list|(
name|float
name|percent
parameter_list|)
block|{
name|StringBuilder
name|bar
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"["
argument_list|)
decl_stmt|;
name|int
name|remainingChars
init|=
name|progressBarChars
operator|-
literal|4
decl_stmt|;
name|int
name|completed
init|=
call|(
name|int
call|)
argument_list|(
name|remainingChars
operator|*
name|percent
argument_list|)
decl_stmt|;
name|int
name|pending
init|=
name|remainingChars
operator|-
name|completed
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|completed
condition|;
name|i
operator|++
control|)
block|{
name|bar
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
block|}
name|bar
operator|.
name|append
argument_list|(
literal|">>"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pending
condition|;
name|i
operator|++
control|)
block|{
name|bar
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
block|}
name|bar
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|bar
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|reprintMultiLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|int
name|numLines
init|=
name|line
operator|.
name|split
argument_list|(
literal|"\r\n|\r|\n"
argument_list|)
operator|.
name|length
decl_stmt|;
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|eraseLine
argument_list|(
name|Ansi
operator|.
name|Erase
operator|.
name|ALL
argument_list|)
operator|.
name|a
argument_list|(
name|line
argument_list|)
operator|.
name|a
argument_list|(
literal|'\n'
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|+=
name|numLines
expr_stmt|;
block|}
block|}
end_class

end_unit

