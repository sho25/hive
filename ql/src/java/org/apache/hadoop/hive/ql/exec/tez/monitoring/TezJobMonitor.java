begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at<p>   http://www.apache.org/licenses/LICENSE-2.0<p>   Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|tez
operator|.
name|monitoring
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGStatus
operator|.
name|State
operator|.
name|RUNNING
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
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|LinkedList
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
name|Set
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
name|commons
operator|.
name|lang3
operator|.
name|exception
operator|.
name|ExceptionUtils
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
name|common
operator|.
name|log
operator|.
name|ProgressMonitor
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Context
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
name|exec
operator|.
name|tez
operator|.
name|TezSessionPoolManager
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
name|plan
operator|.
name|BaseWork
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
operator|.
name|LogHelper
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
name|wm
operator|.
name|TimeCounterLimit
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
name|wm
operator|.
name|WmContext
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
name|wm
operator|.
name|VertexCounterLimit
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
name|common
operator|.
name|util
operator|.
name|ShutdownHookManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|counters
operator|.
name|CounterGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|counters
operator|.
name|TezCounter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|counters
operator|.
name|TezCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|DAG
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|Progress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|StatusGetOpts
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|util
operator|.
name|StopWatch
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * TezJobMonitor keeps track of a tez job while it's being executed. It will  * print status to the console and retrieve final status of the job after  * completion.  */
end_comment

begin_class
specifier|public
class|class
name|TezJobMonitor
block|{
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|TezJobMonitor
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_CHECK_INTERVAL
init|=
literal|200
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CHECK_INTERVAL
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_RETRY_INTERVAL
init|=
literal|2500
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_RETRY_FAILURES
init|=
operator|(
name|MAX_RETRY_INTERVAL
operator|/
name|MAX_CHECK_INTERVAL
operator|)
operator|+
literal|1
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|DAGClient
argument_list|>
name|shutdownList
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|BaseWork
argument_list|>
name|topSortedWorks
decl_stmt|;
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|StringWriter
name|diagnostics
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
static|static
block|{
name|shutdownList
operator|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
expr_stmt|;
name|ShutdownHookManager
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|TezJobMonitor
operator|.
name|killRunningJobs
argument_list|()
expr_stmt|;
try|try
block|{
comment|// TODO: why does this only kill non-default sessions?
comment|// Nothing for workload management since that only deals with default ones.
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|closeNonDefaultSessions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|initShutdownHook
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|shutdownList
argument_list|,
literal|"Shutdown hook was not properly initialized"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
name|DAGClient
name|dagClient
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|DAG
name|dag
decl_stmt|;
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
name|long
name|executionStartTime
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|RenderStrategy
operator|.
name|UpdateFunction
name|updateFunction
decl_stmt|;
specifier|public
name|TezJobMonitor
parameter_list|(
name|List
argument_list|<
name|BaseWork
argument_list|>
name|topSortedWorks
parameter_list|,
specifier|final
name|DAGClient
name|dagClient
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|DAG
name|dag
parameter_list|,
name|Context
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|topSortedWorks
operator|=
name|topSortedWorks
expr_stmt|;
name|this
operator|.
name|dagClient
operator|=
name|dagClient
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|dag
operator|=
name|dag
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|ctx
expr_stmt|;
name|console
operator|=
name|SessionState
operator|.
name|getConsole
argument_list|()
expr_stmt|;
name|updateFunction
operator|=
name|updateFunction
argument_list|()
expr_stmt|;
block|}
specifier|private
name|RenderStrategy
operator|.
name|UpdateFunction
name|updateFunction
parameter_list|()
block|{
return|return
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
operator|&&
operator|!
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|isHiveServerQuery
argument_list|()
condition|?
operator|new
name|RenderStrategy
operator|.
name|InPlaceUpdateFunction
argument_list|(
name|this
argument_list|)
else|:
operator|new
name|RenderStrategy
operator|.
name|LogToFileFunction
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isProfilingEnabled
parameter_list|()
block|{
return|return
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|)
operator|||
name|Utilities
operator|.
name|isPerfOrAboveLogging
argument_list|(
name|hiveConf
argument_list|)
return|;
block|}
specifier|public
name|int
name|monitorExecution
parameter_list|()
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|int
name|failedCounter
init|=
literal|0
decl_stmt|;
specifier|final
name|StopWatch
name|failureTimer
init|=
operator|new
name|StopWatch
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
literal|0
decl_stmt|;
name|DAGStatus
name|status
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|vertexProgressMap
init|=
literal|null
decl_stmt|;
name|long
name|monitorStartTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|shutdownList
init|)
block|{
name|shutdownList
operator|.
name|add
argument_list|(
name|dagClient
argument_list|)
expr_stmt|;
block|}
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_RUN_DAG
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
name|TEZ_SUBMIT_TO_RUNNING
argument_list|)
expr_stmt|;
name|DAGStatus
operator|.
name|State
name|lastState
init|=
literal|null
decl_stmt|;
name|boolean
name|running
init|=
literal|false
decl_stmt|;
name|long
name|checkInterval
init|=
name|MIN_CHECK_INTERVAL
decl_stmt|;
name|WmContext
name|wmContext
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|checkHeartbeaterLockException
argument_list|()
expr_stmt|;
block|}
name|status
operator|=
name|dagClient
operator|.
name|getDAGStatus
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|StatusGetOpts
operator|.
name|GET_COUNTERS
argument_list|)
argument_list|,
name|checkInterval
argument_list|)
expr_stmt|;
name|TezCounters
name|dagCounters
init|=
name|status
operator|.
name|getDAGCounters
argument_list|()
decl_stmt|;
name|vertexProgressMap
operator|=
name|status
operator|.
name|getVertexProgress
argument_list|()
expr_stmt|;
name|wmContext
operator|=
name|context
operator|.
name|getWmContext
argument_list|()
expr_stmt|;
if|if
condition|(
name|dagCounters
operator|!=
literal|null
operator|&&
name|wmContext
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|desiredCounters
init|=
name|wmContext
operator|.
name|getSubscribedCounters
argument_list|()
decl_stmt|;
if|if
condition|(
name|desiredCounters
operator|!=
literal|null
operator|&&
operator|!
name|desiredCounters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|currentCounters
init|=
name|getCounterValues
argument_list|(
name|dagCounters
argument_list|,
name|vertexProgressMap
argument_list|,
name|desiredCounters
argument_list|,
name|done
argument_list|)
decl_stmt|;
name|wmContext
operator|.
name|setCurrentCounters
argument_list|(
name|currentCounters
argument_list|)
expr_stmt|;
block|}
block|}
name|DAGStatus
operator|.
name|State
name|state
init|=
name|status
operator|.
name|getState
argument_list|()
decl_stmt|;
name|failedCounter
operator|=
literal|0
expr_stmt|;
comment|// AM is responsive again (recovery?)
name|failureTimer
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|state
operator|!=
name|lastState
operator|||
name|state
operator|==
name|RUNNING
condition|)
block|{
name|lastState
operator|=
name|state
expr_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|SUBMITTED
case|:
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Submitted"
argument_list|)
expr_stmt|;
break|break;
case|case
name|INITING
case|:
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Initializing"
argument_list|)
expr_stmt|;
name|this
operator|.
name|executionStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
break|break;
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
name|TEZ_SUBMIT_TO_RUNNING
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Running ("
operator|+
name|dagClient
operator|.
name|getExecutionContext
argument_list|()
operator|+
literal|")\n"
argument_list|)
expr_stmt|;
name|this
operator|.
name|executionStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|running
operator|=
literal|true
expr_stmt|;
comment|// from running -> failed/succeeded, the AM breaks out of timeouts
name|checkInterval
operator|=
name|MAX_CHECK_INTERVAL
expr_stmt|;
block|}
name|updateFunction
operator|.
name|update
argument_list|(
name|status
argument_list|,
name|vertexProgressMap
argument_list|)
expr_stmt|;
break|break;
case|case
name|SUCCEEDED
case|:
if|if
condition|(
operator|!
name|running
condition|)
block|{
name|this
operator|.
name|executionStartTime
operator|=
name|monitorStartTime
expr_stmt|;
block|}
name|updateFunction
operator|.
name|update
argument_list|(
name|status
argument_list|,
name|vertexProgressMap
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
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
name|KILLED
case|:
if|if
condition|(
operator|!
name|running
condition|)
block|{
name|this
operator|.
name|executionStartTime
operator|=
name|monitorStartTime
expr_stmt|;
block|}
name|updateFunction
operator|.
name|update
argument_list|(
name|status
argument_list|,
name|vertexProgressMap
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Killed"
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
literal|1
expr_stmt|;
break|break;
case|case
name|FAILED
case|:
case|case
name|ERROR
case|:
if|if
condition|(
operator|!
name|running
condition|)
block|{
name|this
operator|.
name|executionStartTime
operator|=
name|monitorStartTime
expr_stmt|;
block|}
name|updateFunction
operator|.
name|update
argument_list|(
name|status
argument_list|,
name|vertexProgressMap
argument_list|)
expr_stmt|;
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
literal|2
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|wmContext
operator|!=
literal|null
operator|&&
name|done
condition|)
block|{
name|wmContext
operator|.
name|setQueryCompleted
argument_list|(
literal|true
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
name|console
operator|.
name|printInfo
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|isInterrupted
init|=
name|hasInterruptedException
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|failedCounter
operator|==
literal|0
condition|)
block|{
name|failureTimer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|failureTimer
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isInterrupted
operator|||
operator|(
operator|++
name|failedCounter
operator|>=
name|MAX_RETRY_FAILURES
operator|&&
name|failureTimer
operator|.
name|now
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|>
name|MAX_RETRY_INTERVAL
operator|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|isInterrupted
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Killing DAG..."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Killing DAG... after %d seconds"
argument_list|,
name|failureTimer
operator|.
name|now
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|dagClient
operator|.
name|tryKillDAG
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|TezException
name|tezException
parameter_list|)
block|{
comment|// best effort
block|}
name|console
operator|.
name|printError
argument_list|(
literal|"Execution has failed. stack trace: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
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
block|}
else|else
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Retrying..."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|wmContext
operator|!=
literal|null
operator|&&
name|done
condition|)
block|{
name|wmContext
operator|.
name|setQueryCompleted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|done
condition|)
block|{
if|if
condition|(
name|wmContext
operator|!=
literal|null
operator|&&
name|done
condition|)
block|{
name|wmContext
operator|.
name|setQueryCompleted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rc
operator|!=
literal|0
operator|&&
name|status
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|diag
range|:
name|status
operator|.
name|getDiagnostics
argument_list|()
control|)
block|{
name|console
operator|.
name|printError
argument_list|(
name|diag
argument_list|)
expr_stmt|;
name|diagnostics
operator|.
name|append
argument_list|(
name|diag
argument_list|)
expr_stmt|;
block|}
block|}
synchronized|synchronized
init|(
name|shutdownList
init|)
block|{
name|shutdownList
operator|.
name|remove
argument_list|(
name|dagClient
argument_list|)
expr_stmt|;
block|}
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
name|TEZ_RUN_DAG
argument_list|)
expr_stmt|;
name|printSummary
argument_list|(
name|success
argument_list|,
name|vertexProgressMap
argument_list|)
expr_stmt|;
return|return
name|rc
return|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getCounterValues
parameter_list|(
specifier|final
name|TezCounters
name|dagCounters
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|vertexProgressMap
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|desiredCounters
parameter_list|,
specifier|final
name|boolean
name|done
parameter_list|)
block|{
comment|// DAG specific counters
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|updatedCounters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|CounterGroup
name|counterGroup
range|:
name|dagCounters
control|)
block|{
for|for
control|(
name|TezCounter
name|tezCounter
range|:
name|counterGroup
control|)
block|{
name|String
name|counterName
init|=
name|tezCounter
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|desiredCounters
operator|.
name|contains
argument_list|(
name|counterName
argument_list|)
condition|)
block|{
name|updatedCounters
operator|.
name|put
argument_list|(
name|counterName
argument_list|,
name|tezCounter
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Process per vertex counters.
name|String
name|counterName
init|=
name|VertexCounterLimit
operator|.
name|VertexCounter
operator|.
name|TOTAL_TASKS
operator|.
name|name
argument_list|()
decl_stmt|;
if|if
condition|(
name|desiredCounters
operator|.
name|contains
argument_list|(
name|counterName
argument_list|)
operator|&&
name|vertexProgressMap
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|entry
range|:
name|vertexProgressMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// TOTAL_TASKS counter is per vertex counter, but triggers are validated at query level
comment|// looking for query level violations. So we always choose max TOTAL_TASKS among all vertices.
comment|// Publishing TOTAL_TASKS for all vertices is not really useful from the context of triggers.
name|long
name|currentMax
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|updatedCounters
operator|.
name|containsKey
argument_list|(
name|counterName
argument_list|)
condition|)
block|{
name|currentMax
operator|=
name|updatedCounters
operator|.
name|get
argument_list|(
name|counterName
argument_list|)
expr_stmt|;
block|}
name|long
name|totalTasks
init|=
name|Math
operator|.
name|max
argument_list|(
name|currentMax
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getTotalTaskCount
argument_list|()
argument_list|)
decl_stmt|;
name|updatedCounters
operator|.
name|put
argument_list|(
name|counterName
argument_list|,
name|totalTasks
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Time based counters. If DAG is done already don't update these counters.
if|if
condition|(
operator|!
name|done
condition|)
block|{
name|counterName
operator|=
name|TimeCounterLimit
operator|.
name|TimeCounter
operator|.
name|ELAPSED_TIME
operator|.
name|name
argument_list|()
expr_stmt|;
if|if
condition|(
name|desiredCounters
operator|.
name|contains
argument_list|(
name|counterName
argument_list|)
condition|)
block|{
name|updatedCounters
operator|.
name|put
argument_list|(
name|counterName
argument_list|,
name|context
operator|.
name|getWmContext
argument_list|()
operator|.
name|getElapsedTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|counterName
operator|=
name|TimeCounterLimit
operator|.
name|TimeCounter
operator|.
name|EXECUTION_TIME
operator|.
name|name
argument_list|()
expr_stmt|;
if|if
condition|(
name|desiredCounters
operator|.
name|contains
argument_list|(
name|counterName
argument_list|)
operator|&&
name|executionStartTime
operator|>
literal|0
condition|)
block|{
name|updatedCounters
operator|.
name|put
argument_list|(
name|counterName
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|executionStartTime
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|updatedCounters
return|;
block|}
specifier|private
name|void
name|printSummary
parameter_list|(
name|boolean
name|success
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|)
block|{
if|if
condition|(
name|isProfilingEnabled
argument_list|()
operator|&&
name|success
operator|&&
name|progressMap
operator|!=
literal|null
condition|)
block|{
name|double
name|duration
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|this
operator|.
name|executionStartTime
operator|)
operator|/
literal|1000.0
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: DAG finished successfully in "
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
name|console
operator|.
name|printInfo
argument_list|(
literal|""
argument_list|)
expr_stmt|;
operator|new
name|QueryExecutionBreakdownSummary
argument_list|(
name|perfLogger
argument_list|)
operator|.
name|print
argument_list|(
name|console
argument_list|)
expr_stmt|;
operator|new
name|DAGSummary
argument_list|(
name|progressMap
argument_list|,
name|hiveConf
argument_list|,
name|dagClient
argument_list|,
name|dag
argument_list|,
name|perfLogger
argument_list|)
operator|.
name|print
argument_list|(
name|console
argument_list|)
expr_stmt|;
comment|//llap IO summary
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_ENABLED
argument_list|,
literal|false
argument_list|)
condition|)
block|{
operator|new
name|LLAPioSummary
argument_list|(
name|progressMap
argument_list|,
name|dagClient
argument_list|)
operator|.
name|print
argument_list|(
name|console
argument_list|)
expr_stmt|;
operator|new
name|FSCountersSummary
argument_list|(
name|progressMap
argument_list|,
name|dagClient
argument_list|)
operator|.
name|print
argument_list|(
name|console
argument_list|)
expr_stmt|;
block|}
name|String
name|wmQueue
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_INTERACTIVE_QUEUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|wmQueue
operator|!=
literal|null
operator|&&
operator|!
name|wmQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
operator|new
name|LlapWmSummary
argument_list|(
name|progressMap
argument_list|,
name|dagClient
argument_list|)
operator|.
name|print
argument_list|(
name|console
argument_list|)
expr_stmt|;
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|hasInterruptedException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
comment|// Hadoop IPC wraps InterruptedException. GRRR.
while|while
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|InterruptedException
operator|||
name|e
operator|instanceof
name|InterruptedIOException
condition|)
block|{
return|return
literal|true
return|;
block|}
name|e
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * killRunningJobs tries to terminate execution of all    * currently running tez queries. No guarantees, best effort only.    *    * {@link org.apache.hadoop.hive.ql.exec.tez.TezJobExecHelper#killRunningJobs()} makes use of    * this method via reflection.    */
specifier|public
specifier|static
name|void
name|killRunningJobs
parameter_list|()
block|{
synchronized|synchronized
init|(
name|shutdownList
init|)
block|{
for|for
control|(
name|DAGClient
name|c
range|:
name|shutdownList
control|)
block|{
try|try
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Trying to shutdown DAG"
argument_list|)
expr_stmt|;
name|c
operator|.
name|tryKillDAG
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
specifier|static
name|long
name|getCounterValueByGroupName
parameter_list|(
name|TezCounters
name|vertexCounters
parameter_list|,
name|String
name|groupNamePattern
parameter_list|,
name|String
name|counterName
parameter_list|)
block|{
name|TezCounter
name|tezCounter
init|=
name|vertexCounters
operator|.
name|getGroup
argument_list|(
name|groupNamePattern
argument_list|)
operator|.
name|findCounter
argument_list|(
name|counterName
argument_list|)
decl_stmt|;
return|return
operator|(
name|tezCounter
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|tezCounter
operator|.
name|getValue
argument_list|()
return|;
block|}
specifier|public
name|String
name|getDiagnostics
parameter_list|()
block|{
return|return
name|diagnostics
operator|.
name|toString
argument_list|()
return|;
block|}
name|ProgressMonitor
name|progressMonitor
parameter_list|(
name|DAGStatus
name|status
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|TezProgressMonitor
argument_list|(
name|dagClient
argument_list|,
name|status
argument_list|,
name|topSortedWorks
argument_list|,
name|progressMap
argument_list|,
name|console
argument_list|,
name|executionStartTime
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|TezException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Getting  Progress Information: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" stack trace: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|TezProgressMonitor
operator|.
name|NULL
return|;
block|}
block|}
end_class

end_unit

