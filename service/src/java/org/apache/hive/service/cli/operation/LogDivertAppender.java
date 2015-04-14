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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|CharArrayWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|OperationLog
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
name|OperationLog
operator|.
name|LoggingLevel
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
name|cli
operator|.
name|CLIServiceUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|ConsoleAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Layout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|WriterAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|spi
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|spi
operator|.
name|LoggingEvent
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
name|base
operator|.
name|Joiner
import|;
end_import

begin_comment
comment|/**  * An Appender to divert logs from individual threads to the LogObject they belong to.  */
end_comment

begin_class
specifier|public
class|class
name|LogDivertAppender
extends|extends
name|WriterAppender
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|LogDivertAppender
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|OperationManager
name|operationManager
decl_stmt|;
specifier|private
name|boolean
name|isVerbose
decl_stmt|;
specifier|private
name|Layout
name|verboseLayout
decl_stmt|;
comment|/**    * A log filter that filters messages coming from the logger with the given names.    * It be used as a white list filter or a black list filter.    * We apply black list filter on the Loggers used by the log diversion stuff, so that    * they don't generate more logs for themselves when they process logs.    * White list filter is used for less verbose log collection    */
specifier|private
specifier|static
class|class
name|NameFilter
extends|extends
name|Filter
block|{
specifier|private
name|Pattern
name|namePattern
decl_stmt|;
specifier|private
name|LoggingLevel
name|loggingMode
decl_stmt|;
specifier|private
name|OperationManager
name|operationManager
decl_stmt|;
comment|/* Patterns that are excluded in verbose logging level.      * Filter out messages coming from log processing classes, or we'll run an infinite loop.      */
specifier|private
specifier|static
specifier|final
name|Pattern
name|verboseExcludeNamePattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|"|"
argument_list|)
operator|.
name|join
argument_list|(
operator|new
name|String
index|[]
block|{
name|LOG
operator|.
name|getName
argument_list|()
block|,
name|OperationLog
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
name|OperationManager
operator|.
name|class
operator|.
name|getName
argument_list|()
block|}
argument_list|)
argument_list|)
decl_stmt|;
comment|/* Patterns that are included in execution logging level.      * In execution mode, show only select logger messages.      */
specifier|private
specifier|static
specifier|final
name|Pattern
name|executionIncludeNamePattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|"|"
argument_list|)
operator|.
name|join
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"org.apache.hadoop.mapreduce.JobSubmitter"
block|,
literal|"org.apache.hadoop.mapreduce.Job"
block|,
literal|"SessionState"
block|,
name|Task
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
literal|"org.apache.hadoop.hive.ql.exec.spark.status.SparkJobMonitor"
block|}
argument_list|)
argument_list|)
decl_stmt|;
comment|/* Patterns that are included in performance logging level.      * In performance mode, show execution and performance logger messages.      */
specifier|private
specifier|static
specifier|final
name|Pattern
name|performanceIncludeNamePattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|executionIncludeNamePattern
operator|.
name|pattern
argument_list|()
operator|+
literal|"|"
operator|+
name|PerfLogger
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|void
name|setCurrentNamePattern
parameter_list|(
name|OperationLog
operator|.
name|LoggingLevel
name|mode
parameter_list|)
block|{
if|if
condition|(
name|mode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|VERBOSE
condition|)
block|{
name|this
operator|.
name|namePattern
operator|=
name|verboseExcludeNamePattern
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|EXECUTION
condition|)
block|{
name|this
operator|.
name|namePattern
operator|=
name|executionIncludeNamePattern
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|PERFORMANCE
condition|)
block|{
name|this
operator|.
name|namePattern
operator|=
name|performanceIncludeNamePattern
expr_stmt|;
block|}
block|}
specifier|public
name|NameFilter
parameter_list|(
name|OperationLog
operator|.
name|LoggingLevel
name|loggingMode
parameter_list|,
name|OperationManager
name|op
parameter_list|)
block|{
name|this
operator|.
name|operationManager
operator|=
name|op
expr_stmt|;
name|this
operator|.
name|loggingMode
operator|=
name|loggingMode
expr_stmt|;
name|setCurrentNamePattern
argument_list|(
name|loggingMode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|decide
parameter_list|(
name|LoggingEvent
name|ev
parameter_list|)
block|{
name|OperationLog
name|log
init|=
name|operationManager
operator|.
name|getOperationLogByThread
argument_list|()
decl_stmt|;
name|boolean
name|excludeMatches
init|=
operator|(
name|loggingMode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|VERBOSE
operator|)
decl_stmt|;
if|if
condition|(
name|log
operator|==
literal|null
condition|)
block|{
return|return
name|Filter
operator|.
name|DENY
return|;
block|}
name|OperationLog
operator|.
name|LoggingLevel
name|currentLoggingMode
init|=
name|log
operator|.
name|getOpLoggingLevel
argument_list|()
decl_stmt|;
comment|// If logging is disabled, deny everything.
if|if
condition|(
name|currentLoggingMode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|NONE
condition|)
block|{
return|return
name|Filter
operator|.
name|DENY
return|;
block|}
comment|// Look at the current session's setting
comment|// and set the pattern and excludeMatches accordingly.
if|if
condition|(
name|currentLoggingMode
operator|!=
name|loggingMode
condition|)
block|{
name|loggingMode
operator|=
name|currentLoggingMode
expr_stmt|;
name|setCurrentNamePattern
argument_list|(
name|loggingMode
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isMatch
init|=
name|namePattern
operator|.
name|matcher
argument_list|(
name|ev
operator|.
name|getLoggerName
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
decl_stmt|;
if|if
condition|(
name|excludeMatches
operator|==
name|isMatch
condition|)
block|{
comment|// Deny if this is black-list filter (excludeMatches = true) and it
comment|// matched
comment|// or if this is whitelist filter and it didn't match
return|return
name|Filter
operator|.
name|DENY
return|;
block|}
return|return
name|Filter
operator|.
name|NEUTRAL
return|;
block|}
block|}
comment|/** This is where the log message will go to */
specifier|private
specifier|final
name|CharArrayWriter
name|writer
init|=
operator|new
name|CharArrayWriter
argument_list|()
decl_stmt|;
specifier|private
name|void
name|setLayout
parameter_list|(
name|boolean
name|isVerbose
parameter_list|,
name|Layout
name|lo
parameter_list|)
block|{
if|if
condition|(
name|isVerbose
condition|)
block|{
if|if
condition|(
name|lo
operator|==
literal|null
condition|)
block|{
name|lo
operator|=
name|CLIServiceUtils
operator|.
name|verboseLayout
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot find a Layout from a ConsoleAppender. Using default Layout pattern."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|lo
operator|=
name|CLIServiceUtils
operator|.
name|nonVerboseLayout
expr_stmt|;
block|}
name|setLayout
argument_list|(
name|lo
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initLayout
parameter_list|(
name|boolean
name|isVerbose
parameter_list|)
block|{
comment|// There should be a ConsoleAppender. Copy its Layout.
name|Logger
name|root
init|=
name|Logger
operator|.
name|getRootLogger
argument_list|()
decl_stmt|;
name|Layout
name|layout
init|=
literal|null
decl_stmt|;
name|Enumeration
argument_list|<
name|?
argument_list|>
name|appenders
init|=
name|root
operator|.
name|getAllAppenders
argument_list|()
decl_stmt|;
while|while
condition|(
name|appenders
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|Appender
name|ap
init|=
operator|(
name|Appender
operator|)
name|appenders
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|ap
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|ConsoleAppender
operator|.
name|class
argument_list|)
condition|)
block|{
name|layout
operator|=
name|ap
operator|.
name|getLayout
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|setLayout
argument_list|(
name|isVerbose
argument_list|,
name|layout
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LogDivertAppender
parameter_list|(
name|OperationManager
name|operationManager
parameter_list|,
name|OperationLog
operator|.
name|LoggingLevel
name|loggingMode
parameter_list|)
block|{
name|isVerbose
operator|=
operator|(
name|loggingMode
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|VERBOSE
operator|)
expr_stmt|;
name|initLayout
argument_list|(
name|isVerbose
argument_list|)
expr_stmt|;
name|setWriter
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|setName
argument_list|(
literal|"LogDivertAppender"
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationManager
operator|=
name|operationManager
expr_stmt|;
name|this
operator|.
name|verboseLayout
operator|=
name|isVerbose
condition|?
name|layout
else|:
name|CLIServiceUtils
operator|.
name|verboseLayout
expr_stmt|;
name|addFilter
argument_list|(
operator|new
name|NameFilter
argument_list|(
name|loggingMode
argument_list|,
name|operationManager
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doAppend
parameter_list|(
name|LoggingEvent
name|event
parameter_list|)
block|{
name|OperationLog
name|log
init|=
name|operationManager
operator|.
name|getOperationLogByThread
argument_list|()
decl_stmt|;
comment|// Set current layout depending on the verbose/non-verbose mode.
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|boolean
name|isCurrModeVerbose
init|=
operator|(
name|log
operator|.
name|getOpLoggingLevel
argument_list|()
operator|==
name|OperationLog
operator|.
name|LoggingLevel
operator|.
name|VERBOSE
operator|)
decl_stmt|;
comment|// If there is a logging level change from verbose->non-verbose or vice-versa since
comment|// the last subAppend call, change the layout to preserve consistency.
if|if
condition|(
name|isCurrModeVerbose
operator|!=
name|isVerbose
condition|)
block|{
name|isVerbose
operator|=
name|isCurrModeVerbose
expr_stmt|;
name|setLayout
argument_list|(
name|isVerbose
argument_list|,
name|verboseLayout
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|doAppend
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * Overrides WriterAppender.subAppend(), which does the real logging. No need    * to worry about concurrency since log4j calls this synchronously.    */
annotation|@
name|Override
specifier|protected
name|void
name|subAppend
parameter_list|(
name|LoggingEvent
name|event
parameter_list|)
block|{
name|super
operator|.
name|subAppend
argument_list|(
name|event
argument_list|)
expr_stmt|;
comment|// That should've gone into our writer. Notify the LogContext.
name|String
name|logOutput
init|=
name|writer
operator|.
name|toString
argument_list|()
decl_stmt|;
name|writer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|OperationLog
name|log
init|=
name|operationManager
operator|.
name|getOperationLogByThread
argument_list|()
decl_stmt|;
if|if
condition|(
name|log
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|" ---+++=== Dropped log event from thread "
operator|+
name|event
operator|.
name|getThreadName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|log
operator|.
name|writeOperationLog
argument_list|(
name|logOutput
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

