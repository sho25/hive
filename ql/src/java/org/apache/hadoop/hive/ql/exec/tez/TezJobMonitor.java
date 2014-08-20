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
name|tez
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
name|util
operator|.
name|Collections
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|Heartbeater
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
name|lockmgr
operator|.
name|HiveTxnManager
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

begin_comment
comment|/**  * TezJobMonitor keeps track of a tez job while it's being executed. It will  * print status to the console and retrieve final status of the job after  * completion.  */
end_comment

begin_class
specifier|public
class|class
name|TezJobMonitor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TezJobMonitor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
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
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|checkInterval
init|=
literal|200
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRetryInterval
init|=
literal|2500
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
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|completed
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
static|static
block|{
name|shutdownList
operator|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|LinkedList
argument_list|<
name|DAGClient
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
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
try|try
block|{
for|for
control|(
name|TezSessionState
name|s
range|:
name|TezSessionState
operator|.
name|getOpenSessions
argument_list|()
control|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Shutting down tez session."
argument_list|)
expr_stmt|;
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|close
argument_list|(
name|s
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
comment|// ignore
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TezJobMonitor
parameter_list|()
block|{
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
comment|/**    * monitorExecution handles status printing, failures during execution and final    * status retrieval.    *    * @param dagClient client that was used to kick off the job    * @param txnMgr transaction manager for this operation    * @param conf configuration file for this operation    * @return int 0 - success, 1 - killed, 2 - failed    */
specifier|public
name|int
name|monitorExecution
parameter_list|(
specifier|final
name|DAGClient
name|dagClient
parameter_list|,
name|HiveTxnManager
name|txnMgr
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|DAGStatus
name|status
init|=
literal|null
decl_stmt|;
name|completed
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
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
name|failedCounter
init|=
literal|0
decl_stmt|;
name|int
name|rc
init|=
literal|0
decl_stmt|;
name|DAGStatus
operator|.
name|State
name|lastState
init|=
literal|null
decl_stmt|;
name|String
name|lastReport
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|StatusGetOpts
argument_list|>
name|opts
init|=
operator|new
name|HashSet
argument_list|<
name|StatusGetOpts
argument_list|>
argument_list|()
decl_stmt|;
name|Heartbeater
name|heartbeater
init|=
operator|new
name|Heartbeater
argument_list|(
name|txnMgr
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|shutdownList
operator|.
name|add
argument_list|(
name|dagClient
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"\n"
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
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|status
operator|=
name|dagClient
operator|.
name|getDAGStatus
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
init|=
name|status
operator|.
name|getVertexProgress
argument_list|()
decl_stmt|;
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
name|heartbeater
operator|.
name|heartbeat
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
literal|"Status: Running (application id: "
operator|+
name|dagClient
operator|.
name|getExecutionContext
argument_list|()
operator|+
literal|")\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|progressMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_RUN_VERTEX
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
name|running
operator|=
literal|true
expr_stmt|;
block|}
name|lastReport
operator|=
name|printStatus
argument_list|(
name|progressMap
argument_list|,
name|lastReport
argument_list|,
name|console
argument_list|)
expr_stmt|;
break|break;
case|case
name|SUCCEEDED
case|:
name|lastReport
operator|=
name|printStatus
argument_list|(
name|progressMap
argument_list|,
name|lastReport
argument_list|,
name|console
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Finished successfully"
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
name|KILLED
case|:
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
if|if
condition|(
operator|++
name|failedCounter
operator|%
name|maxRetryInterval
operator|/
name|checkInterval
operator|==
literal|0
operator|||
name|e
operator|instanceof
name|InterruptedException
condition|)
block|{
try|try
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Killing DAG..."
argument_list|)
expr_stmt|;
name|dagClient
operator|.
name|tryKillDAG
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|io
parameter_list|)
block|{
comment|// best effort
block|}
catch|catch
parameter_list|(
name|TezException
name|te
parameter_list|)
block|{
comment|// best effort
block|}
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Execution has failed."
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
block|}
block|}
name|shutdownList
operator|.
name|remove
argument_list|(
name|dagClient
argument_list|)
expr_stmt|;
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
return|return
name|rc
return|;
block|}
specifier|private
name|String
name|printStatus
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|,
name|String
name|lastReport
parameter_list|,
name|LogHelper
name|console
parameter_list|)
block|{
name|StringBuffer
name|reportBuffer
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
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
name|Progress
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
name|s
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
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_RUN_VERTEX
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
comment|/* vertex is started, but not complete */
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
name|s
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
name|s
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
comment|/* vertex is waiting for input/slots or complete */
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
literal|"%s: %d(-%d)/%d\t"
argument_list|,
name|s
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
name|s
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
name|String
name|report
init|=
name|reportBuffer
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|report
operator|.
name|equals
argument_list|(
name|lastReport
argument_list|)
operator|||
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|>=
name|lastPrintTime
operator|+
name|printInterval
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
name|lastPrintTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
return|return
name|report
return|;
block|}
block|}
end_class

end_unit

