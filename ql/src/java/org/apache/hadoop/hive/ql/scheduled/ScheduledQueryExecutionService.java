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
name|scheduled
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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
name|Executors
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
name|Constants
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
name|metastore
operator|.
name|api
operator|.
name|QueryState
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
name|metastore
operator|.
name|api
operator|.
name|ScheduledQueryKey
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
name|metastore
operator|.
name|api
operator|.
name|ScheduledQueryPollResponse
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
name|metastore
operator|.
name|api
operator|.
name|ScheduledQueryProgressInfo
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
name|DriverFactory
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
name|IDriver
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
name|processors
operator|.
name|CommandProcessorException
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
name|security
operator|.
name|SessionStateUserAuthenticator
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_class
specifier|public
class|class
name|ScheduledQueryExecutionService
implements|implements
name|Closeable
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ScheduledQueryExecutionService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ScheduledQueryExecutionContext
name|context
decl_stmt|;
specifier|private
name|ScheduledQueryExecutor
name|worker
decl_stmt|;
specifier|public
specifier|static
name|ScheduledQueryExecutionService
name|startScheduledQueryExecutorService
parameter_list|(
name|HiveConf
name|conf0
parameter_list|)
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|conf0
argument_list|)
decl_stmt|;
name|MetastoreBasedScheduledQueryService
name|qService
init|=
operator|new
name|MetastoreBasedScheduledQueryService
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"Scheduled Query Thread %d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|ScheduledQueryExecutionContext
name|ctx
init|=
operator|new
name|ScheduledQueryExecutionContext
argument_list|(
name|executor
argument_list|,
name|conf
argument_list|,
name|qService
argument_list|)
decl_stmt|;
return|return
operator|new
name|ScheduledQueryExecutionService
argument_list|(
name|ctx
argument_list|)
return|;
block|}
specifier|public
name|ScheduledQueryExecutionService
parameter_list|(
name|ScheduledQueryExecutionContext
name|ctx
parameter_list|)
block|{
name|context
operator|=
name|ctx
expr_stmt|;
name|ctx
operator|.
name|executor
operator|.
name|submit
argument_list|(
name|worker
operator|=
operator|new
name|ScheduledQueryExecutor
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|ProgressReporter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|boolean
name|isTerminalState
parameter_list|(
name|QueryState
name|state
parameter_list|)
block|{
return|return
name|state
operator|==
name|QueryState
operator|.
name|FINISHED
operator|||
name|state
operator|==
name|QueryState
operator|.
name|ERRORED
return|;
block|}
class|class
name|ScheduledQueryExecutor
implements|implements
name|Runnable
block|{
specifier|private
name|ScheduledQueryProgressInfo
name|info
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|ScheduledQueryPollResponse
name|q
init|=
name|context
operator|.
name|schedulerService
operator|.
name|scheduledQueryPoll
argument_list|()
decl_stmt|;
if|if
condition|(
name|q
operator|.
name|isSetExecutionId
argument_list|()
condition|)
block|{
try|try
block|{
name|processQuery
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected exception during scheduled query processing"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|context
operator|.
name|getIdleSleepTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"interrupted"
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|reportQueryProgress
parameter_list|()
block|{
if|if
condition|(
name|info
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Reporting query progress of {} as {} err:{}"
argument_list|,
name|info
operator|.
name|getScheduledExecutionId
argument_list|()
argument_list|,
name|info
operator|.
name|getState
argument_list|()
argument_list|,
name|info
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|schedulerService
operator|.
name|scheduledQueryProgress
argument_list|(
name|info
argument_list|)
expr_stmt|;
if|if
condition|(
name|isTerminalState
argument_list|(
name|info
operator|.
name|getState
argument_list|()
argument_list|)
condition|)
block|{
name|info
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|processQuery
parameter_list|(
name|ScheduledQueryPollResponse
name|q
parameter_list|)
block|{
name|SessionState
name|state
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|context
operator|.
name|conf
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|HIVE_QUERY_EXCLUSIVE_LOCK
argument_list|,
name|lockNameFor
argument_list|(
name|q
operator|.
name|getScheduleKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHENTICATOR_MANAGER
argument_list|,
name|SessionStateUserAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|unset
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONID
operator|.
name|varname
argument_list|)
expr_stmt|;
name|state
operator|=
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|,
name|q
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|info
operator|=
operator|new
name|ScheduledQueryProgressInfo
argument_list|()
expr_stmt|;
name|info
operator|.
name|setScheduledExecutionId
argument_list|(
name|q
operator|.
name|getExecutionId
argument_list|()
argument_list|)
expr_stmt|;
name|info
operator|.
name|setState
argument_list|(
name|QueryState
operator|.
name|EXECUTING
argument_list|)
expr_stmt|;
name|reportQueryProgress
argument_list|()
expr_stmt|;
try|try
init|(
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|DriverFactory
operator|.
name|getNewQueryState
argument_list|(
name|conf
argument_list|)
argument_list|,
name|q
operator|.
name|getUser
argument_list|()
argument_list|,
literal|null
argument_list|)
init|)
block|{
name|info
operator|.
name|setExecutorQueryId
argument_list|(
name|driver
operator|.
name|getQueryState
argument_list|()
operator|.
name|getQueryId
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|q
operator|.
name|getQuery
argument_list|()
argument_list|)
expr_stmt|;
name|info
operator|.
name|setState
argument_list|(
name|QueryState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|info
operator|.
name|setErrorMessage
argument_list|(
name|getErrorStringForException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
name|info
operator|.
name|setState
argument_list|(
name|QueryState
operator|.
name|ERRORED
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|state
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{           }
block|}
name|reportQueryProgress
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|lockNameFor
parameter_list|(
name|ScheduledQueryKey
name|scheduleKey
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"scheduled_query_%s_%s"
argument_list|,
name|scheduleKey
operator|.
name|getClusterNamespace
argument_list|()
argument_list|,
name|scheduleKey
operator|.
name|getScheduleName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|String
name|getErrorStringForException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|CommandProcessorException
condition|)
block|{
name|CommandProcessorException
name|cpr
init|=
operator|(
name|CommandProcessorException
operator|)
name|t
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s"
argument_list|,
name|cpr
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s: %s"
argument_list|,
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
class|class
name|ProgressReporter
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|context
operator|.
name|getProgressReporterSleepTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|worker
operator|.
name|reportQueryProgress
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|context
operator|.
name|executor
operator|.
name|awaitTermination
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|context
operator|.
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

