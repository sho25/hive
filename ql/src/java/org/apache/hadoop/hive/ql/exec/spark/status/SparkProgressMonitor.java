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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * This class defines various parts of the progress update bar.  * Progressbar is displayed in hive-cli and typically rendered using InPlaceUpdate.  */
end_comment

begin_class
class|class
name|SparkProgressMonitor
implements|implements
name|ProgressMonitor
block|{
specifier|private
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
decl_stmt|;
specifier|private
name|long
name|startTime
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COLUMN_1_WIDTH
init|=
literal|16
decl_stmt|;
name|SparkProgressMonitor
parameter_list|(
name|Map
argument_list|<
name|SparkStage
argument_list|,
name|SparkStageProgress
argument_list|>
name|progressMap
parameter_list|,
name|long
name|startTime
parameter_list|)
block|{
name|this
operator|.
name|progressMap
operator|=
name|progressMap
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
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
name|Arrays
operator|.
name|asList
argument_list|(
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
argument_list|,
literal|""
argument_list|)
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
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|progressRows
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|SparkJobMonitor
operator|.
name|StageState
name|state
init|=
name|total
operator|>
literal|0
condition|?
name|SparkJobMonitor
operator|.
name|StageState
operator|.
name|PENDING
else|:
name|SparkJobMonitor
operator|.
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
name|complete
operator|<
name|total
condition|)
block|{
name|state
operator|=
name|SparkJobMonitor
operator|.
name|StageState
operator|.
name|RUNNING
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|SparkJobMonitor
operator|.
name|StageState
operator|.
name|FINISHED
expr_stmt|;
block|}
block|}
name|String
name|attempt
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|stage
operator|.
name|getAttemptId
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|stageName
init|=
literal|"Stage-"
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|stage
operator|.
name|getStageId
argument_list|()
argument_list|)
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
name|progressRows
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|nameWithProgress
argument_list|,
name|attempt
argument_list|,
name|state
operator|.
name|toString
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|total
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|complete
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|running
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|pending
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|failed
argument_list|)
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|progressRows
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
name|String
operator|.
name|format
argument_list|(
literal|"STAGES: %02d/%02d"
argument_list|,
name|getCompletedStages
argument_list|()
argument_list|,
name|progressMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
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
name|startTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executionStatus
parameter_list|()
block|{
if|if
condition|(
name|getCompletedStages
argument_list|()
operator|==
name|progressMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|SparkJobMonitor
operator|.
name|StageState
operator|.
name|FINISHED
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|SparkJobMonitor
operator|.
name|StageState
operator|.
name|RUNNING
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|double
name|progressedPercentage
parameter_list|()
block|{
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
name|sumTotal
operator|+=
name|total
expr_stmt|;
name|sumComplete
operator|+=
name|complete
expr_stmt|;
block|}
name|double
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
return|return
name|progress
return|;
block|}
specifier|private
name|int
name|getCompletedStages
parameter_list|()
block|{
name|int
name|completed
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
if|if
condition|(
name|total
operator|>
literal|0
operator|&&
name|complete
operator|==
name|total
condition|)
block|{
name|completed
operator|++
expr_stmt|;
block|}
block|}
return|return
name|completed
return|;
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
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
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
name|trimmedVName
operator|+=
literal|".."
expr_stmt|;
block|}
else|else
block|{
name|trimmedVName
operator|+=
literal|" "
expr_stmt|;
block|}
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|(
name|trimmedVName
argument_list|)
decl_stmt|;
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
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
block|}
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

