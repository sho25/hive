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
name|*
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
name|Serializable
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
name|Collections
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
name|tez
operator|.
name|client
operator|.
name|TezClient
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

begin_comment
comment|/**  * TezJobMonitor keeps track of a tez job while it's being executed. It will   * print status to the console and retrieve final status of the job after   * completion.  */
end_comment

begin_class
specifier|public
class|class
name|TezJobMonitor
block|{
specifier|static
specifier|final
specifier|private
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
specifier|transient
name|LogHelper
name|console
decl_stmt|;
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
comment|/**    * monitorExecution handles status printing, failures during execution and final    * status retrieval.    *    * @param dagClient client that was used to kick off the job    * @return int 0 - success, 1 - killed, 2 - failed    */
specifier|public
name|int
name|monitorExecution
parameter_list|(
name|DAGClient
name|dagClient
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|DAGStatus
name|status
init|=
literal|null
decl_stmt|;
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
name|checkInterval
init|=
literal|500
decl_stmt|;
name|int
name|printInterval
init|=
literal|3000
decl_stmt|;
name|int
name|maxRetryInterval
init|=
literal|5000
decl_stmt|;
name|int
name|counter
init|=
literal|0
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
name|console
operator|.
name|printInfo
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
operator|++
name|counter
expr_stmt|;
try|try
block|{
name|status
operator|=
name|dagClient
operator|.
name|getDAGStatus
argument_list|()
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
name|failedCounter
operator|=
literal|0
expr_stmt|;
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
name|console
operator|.
name|printInfo
argument_list|(
literal|"Status: Running\n"
argument_list|)
expr_stmt|;
name|printTaskNumbers
argument_list|(
name|progressMap
argument_list|,
name|console
argument_list|)
expr_stmt|;
name|running
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|counter
operator|%
name|printInterval
operator|/
name|checkInterval
operator|==
literal|0
condition|)
block|{
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
block|}
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|failedCounter
operator|%
name|maxRetryInterval
operator|/
name|checkInterval
operator|==
literal|0
condition|)
block|{
try|try
block|{
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
block|}
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
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
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
name|int
name|percentComplete
init|=
call|(
name|int
call|)
argument_list|(
literal|100
operator|*
name|progress
operator|.
name|getSucceededTaskCount
argument_list|()
operator|/
operator|(
name|float
operator|)
name|progress
operator|.
name|getTotalTaskCount
argument_list|()
argument_list|)
decl_stmt|;
name|reportBuffer
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s: %3d%% complete\t"
argument_list|,
name|s
argument_list|,
name|percentComplete
argument_list|)
argument_list|)
expr_stmt|;
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
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
return|return
name|report
return|;
block|}
specifier|private
name|void
name|printTaskNumbers
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
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
name|int
name|numTasks
init|=
name|progress
operator|.
name|getTotalTaskCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|numTasks
operator|==
literal|1
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
literal|"%s:        1 task\t"
argument_list|,
name|s
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
literal|"%s: %7d tasks\t"
argument_list|,
name|s
argument_list|,
name|numTasks
argument_list|)
argument_list|)
expr_stmt|;
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
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

