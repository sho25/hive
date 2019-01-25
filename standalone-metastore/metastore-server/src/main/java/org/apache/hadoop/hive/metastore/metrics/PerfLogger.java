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
name|metastore
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
import|;
end_import

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
name|util
operator|.
name|HashMap
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

begin_comment
comment|/**  * PerfLogger.  *  * Can be used to measure and log the time spent by a piece of code.  */
end_comment

begin_class
specifier|public
class|class
name|PerfLogger
block|{
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|startTimes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|endTimes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PerfLogger
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|PerfLogger
argument_list|>
name|perfLogger
init|=
operator|new
name|ThreadLocal
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|PerfLogger
parameter_list|()
block|{
comment|// Use getPerfLogger to get an instance of PerfLogger
block|}
comment|/**    * Get the singleton PerfLogger instance.    * @param resetPerfLogger if false, get the current PerfLogger, or create a new one if a    *                        current one does not exist.  If true, a new instance of PerfLogger    *                        will be returned rather than the existing one.  Note that the    *                        existing PerfLogger is not shutdown and any object which already has a    *                        reference to it may continue to use it. But all future calls to this    *                        method with this set to false will get the new PerfLogger instance.    * @return a PerfLogger    */
specifier|public
specifier|static
name|PerfLogger
name|getPerfLogger
parameter_list|(
name|boolean
name|resetPerfLogger
parameter_list|)
block|{
name|PerfLogger
name|result
init|=
name|perfLogger
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|resetPerfLogger
operator|||
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|PerfLogger
argument_list|()
expr_stmt|;
name|perfLogger
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|void
name|setPerfLogger
parameter_list|(
name|PerfLogger
name|resetPerfLogger
parameter_list|)
block|{
name|perfLogger
operator|.
name|set
argument_list|(
name|resetPerfLogger
argument_list|)
expr_stmt|;
block|}
comment|/**    * Call this function when you start to measure time spent by a piece of code.    * @param callerName the logging object to be used.    * @param method method or ID that identifies this perf log element.    */
specifier|public
name|void
name|PerfLogBegin
parameter_list|(
name|String
name|callerName
parameter_list|,
name|String
name|method
parameter_list|)
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|startTimes
operator|.
name|put
argument_list|(
name|method
argument_list|,
operator|new
name|Long
argument_list|(
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"<PERFLOG method="
operator|+
name|method
operator|+
literal|" from="
operator|+
name|callerName
operator|+
literal|">"
argument_list|)
expr_stmt|;
block|}
name|beginMetrics
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
comment|/**    * Call this function in correspondence of PerfLogBegin to mark the end of the measurement.    * @param callerName    * @param method    * @return long duration  the difference between now and startTime, or -1 if startTime is null    */
specifier|public
name|long
name|PerfLogEnd
parameter_list|(
name|String
name|callerName
parameter_list|,
name|String
name|method
parameter_list|)
block|{
return|return
name|PerfLogEnd
argument_list|(
name|callerName
argument_list|,
name|method
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Call this function in correspondence of PerfLogBegin to mark the end of the measurement.    * @param callerName    * @param method    * @return long duration  the difference between now and startTime, or -1 if startTime is null    */
specifier|public
name|long
name|PerfLogEnd
parameter_list|(
name|String
name|callerName
parameter_list|,
name|String
name|method
parameter_list|,
name|String
name|additionalInfo
parameter_list|)
block|{
name|Long
name|startTime
init|=
name|startTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
decl_stmt|;
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|endTimes
operator|.
name|put
argument_list|(
name|method
argument_list|,
operator|new
name|Long
argument_list|(
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|duration
init|=
name|startTime
operator|==
literal|null
condition|?
operator|-
literal|1
else|:
name|endTime
operator|-
name|startTime
operator|.
name|longValue
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"</PERFLOG method="
argument_list|)
operator|.
name|append
argument_list|(
name|method
argument_list|)
decl_stmt|;
if|if
condition|(
name|startTime
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" start="
argument_list|)
operator|.
name|append
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" end="
argument_list|)
operator|.
name|append
argument_list|(
name|endTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|startTime
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" duration="
argument_list|)
operator|.
name|append
argument_list|(
name|duration
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" from="
argument_list|)
operator|.
name|append
argument_list|(
name|callerName
argument_list|)
expr_stmt|;
if|if
condition|(
name|additionalInfo
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|additionalInfo
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|">"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|endMetrics
argument_list|(
name|method
argument_list|)
expr_stmt|;
return|return
name|duration
return|;
block|}
specifier|public
name|Long
name|getStartTime
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|long
name|startTime
init|=
literal|0L
decl_stmt|;
if|if
condition|(
name|startTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|startTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
return|return
name|startTime
return|;
block|}
specifier|public
name|Long
name|getEndTime
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|long
name|endTime
init|=
literal|0L
decl_stmt|;
if|if
condition|(
name|endTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|endTime
operator|=
name|endTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
return|return
name|endTime
return|;
block|}
specifier|public
name|boolean
name|startTimeHasMethod
parameter_list|(
name|String
name|method
parameter_list|)
block|{
return|return
name|startTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|endTimeHasMethod
parameter_list|(
name|String
name|method
parameter_list|)
block|{
return|return
name|endTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
return|;
block|}
specifier|public
name|Long
name|getDuration
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|long
name|duration
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|startTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
operator|&&
name|endTimes
operator|.
name|containsKey
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|duration
operator|=
name|endTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
operator|-
name|startTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
return|return
name|duration
return|;
block|}
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getStartTimes
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|startTimes
argument_list|)
return|;
block|}
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getEndTimes
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|endTimes
argument_list|)
return|;
block|}
comment|// Methods for metrics integration.  Each thread-local PerfLogger will open/close scope during each perf-log method.
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|Timer
operator|.
name|Context
argument_list|>
name|timerContexts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|Timer
operator|.
name|Context
name|totalApiCallsTimerContext
init|=
literal|null
decl_stmt|;
specifier|private
name|void
name|beginMetrics
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|Timer
name|timer
init|=
name|Metrics
operator|.
name|getOrCreateTimer
argument_list|(
name|MetricsConstants
operator|.
name|API_PREFIX
operator|+
name|method
argument_list|)
decl_stmt|;
if|if
condition|(
name|timer
operator|!=
literal|null
condition|)
block|{
name|timerContexts
operator|.
name|put
argument_list|(
name|method
argument_list|,
name|timer
operator|.
name|time
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|timer
operator|=
name|Metrics
operator|.
name|getOrCreateTimer
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_API_CALLS
argument_list|)
expr_stmt|;
if|if
condition|(
name|timer
operator|!=
literal|null
condition|)
block|{
name|totalApiCallsTimerContext
operator|=
name|timer
operator|.
name|time
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|endMetrics
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|Timer
operator|.
name|Context
name|context
init|=
name|timerContexts
operator|.
name|remove
argument_list|(
name|method
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|totalApiCallsTimerContext
operator|!=
literal|null
condition|)
block|{
name|totalApiCallsTimerContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Cleans up any dangling perfLog metric call scopes.    */
specifier|public
name|void
name|cleanupPerfLogMetrics
parameter_list|()
block|{
for|for
control|(
name|Timer
operator|.
name|Context
name|context
range|:
name|timerContexts
operator|.
name|values
argument_list|()
control|)
block|{
name|context
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|timerContexts
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalApiCallsTimerContext
operator|!=
literal|null
condition|)
block|{
name|totalApiCallsTimerContext
operator|.
name|close
argument_list|()
expr_stmt|;
name|totalApiCallsTimerContext
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

