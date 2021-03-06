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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimerTask
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * AutoProgressor periodically sends updates to the job tracker so that it  * doesn't consider this task attempt dead if there is a long period of  * inactivity. This can be configured with a timeout so that it doesn't run  * indefinitely.  */
end_comment

begin_class
specifier|public
class|class
name|AutoProgressor
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Timer that reports every 5 minutes to the jobtracker. This ensures that
comment|// even if the operator returning rows for greater than that
comment|// duration, a progress report is sent to the tracker so that the tracker
comment|// does not think that the job is dead.
name|Timer
name|rpTimer
init|=
literal|null
decl_stmt|;
comment|// Timer that tops rpTimer after a long timeout, e.g. 1 hr
name|Timer
name|srpTimer
init|=
literal|null
decl_stmt|;
comment|// Name of the class to report for
name|String
name|logClassName
init|=
literal|null
decl_stmt|;
name|int
name|notificationInterval
decl_stmt|;
name|long
name|timeout
decl_stmt|;
name|Reporter
name|reporter
decl_stmt|;
class|class
name|ReporterTask
extends|extends
name|TimerTask
block|{
comment|/**      * Reporter to report progress to the jobtracker.      */
specifier|private
name|Reporter
name|rp
decl_stmt|;
comment|/**      * Constructor.      */
specifier|public
name|ReporterTask
parameter_list|(
name|Reporter
name|rp
parameter_list|)
block|{
if|if
condition|(
name|rp
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|rp
operator|=
name|rp
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|rp
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ReporterTask calling reporter.progress() for "
operator|+
name|logClassName
argument_list|)
expr_stmt|;
name|rp
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
block|}
block|}
class|class
name|StopReporterTimerTask
extends|extends
name|TimerTask
block|{
comment|/**      * Task to stop the reporter timer once we hit the timeout      */
specifier|private
specifier|final
name|ReporterTask
name|rt
decl_stmt|;
specifier|public
name|StopReporterTimerTask
parameter_list|(
name|ReporterTask
name|rp
parameter_list|)
block|{
name|this
operator|.
name|rt
operator|=
name|rp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|rt
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping reporter timer for "
operator|+
name|logClassName
argument_list|)
expr_stmt|;
name|rt
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    *    * @param logClassName    * @param reporter    * @param notificationInterval - interval for reporter updates (in ms)    */
name|AutoProgressor
parameter_list|(
name|String
name|logClassName
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|int
name|notificationInterval
parameter_list|)
block|{
name|this
operator|.
name|logClassName
operator|=
name|logClassName
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
name|this
operator|.
name|notificationInterval
operator|=
name|notificationInterval
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    *    * @param logClassName    * @param reporter    * @param notificationInterval - interval for reporter updates (in ms)    * @param timeout - when the autoprogressor should stop reporting (in ms)    */
name|AutoProgressor
parameter_list|(
name|String
name|logClassName
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|int
name|notificationInterval
parameter_list|,
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|logClassName
operator|=
name|logClassName
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
name|this
operator|.
name|notificationInterval
operator|=
name|notificationInterval
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
specifier|public
name|void
name|go
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running ReporterTask every "
operator|+
name|notificationInterval
operator|+
literal|" miliseconds."
argument_list|)
expr_stmt|;
name|rpTimer
operator|=
operator|new
name|Timer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ReporterTask
name|rt
init|=
operator|new
name|ReporterTask
argument_list|(
name|reporter
argument_list|)
decl_stmt|;
name|rpTimer
operator|.
name|scheduleAtFixedRate
argument_list|(
name|rt
argument_list|,
literal|0
argument_list|,
name|notificationInterval
argument_list|)
expr_stmt|;
if|if
condition|(
name|timeout
operator|>
literal|0
condition|)
block|{
name|srpTimer
operator|=
operator|new
name|Timer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|StopReporterTimerTask
name|srt
init|=
operator|new
name|StopReporterTimerTask
argument_list|(
name|rt
argument_list|)
decl_stmt|;
name|srpTimer
operator|.
name|schedule
argument_list|(
name|srt
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

