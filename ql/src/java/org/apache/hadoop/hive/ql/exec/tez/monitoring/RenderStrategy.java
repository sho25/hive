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
name|tez
operator|.
name|monitoring
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
name|StringWriter
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

begin_class
class|class
name|RenderStrategy
block|{
interface|interface
name|UpdateFunction
block|{
name|void
name|update
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
name|vertexProgressMap
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|abstract
specifier|static
class|class
name|BaseUpdateFunction
implements|implements
name|UpdateFunction
block|{
specifier|private
specifier|static
specifier|final
name|int
name|PRINT_INTERVAL
init|=
literal|3000
decl_stmt|;
specifier|final
name|TezJobMonitor
name|monitor
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
decl_stmt|;
specifier|private
name|long
name|lastPrintTime
init|=
literal|0L
decl_stmt|;
specifier|private
name|String
name|lastReport
init|=
literal|null
decl_stmt|;
name|BaseUpdateFunction
parameter_list|(
name|TezJobMonitor
name|monitor
parameter_list|)
block|{
name|this
operator|.
name|monitor
operator|=
name|monitor
expr_stmt|;
name|perfLogger
operator|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
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
name|vertexProgressMap
parameter_list|)
block|{
name|renderProgress
argument_list|(
name|monitor
operator|.
name|progressMonitor
argument_list|(
name|status
argument_list|,
name|vertexProgressMap
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|report
init|=
name|getReport
argument_list|(
name|vertexProgressMap
argument_list|)
decl_stmt|;
if|if
condition|(
name|showReport
argument_list|(
name|report
argument_list|)
condition|)
block|{
name|renderReport
argument_list|(
name|report
argument_list|)
expr_stmt|;
name|lastReport
operator|=
name|report
expr_stmt|;
name|lastPrintTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|showReport
parameter_list|(
name|String
name|report
parameter_list|)
block|{
return|return
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
name|PRINT_INTERVAL
return|;
block|}
comment|/*        This is used to print the progress information as pure text , a sample is as below:           Map 1: 0/1	Reducer 2: 0/1           Map 1: 0(+1)/1	Reducer 2: 0/1           Map 1: 1/1	Reducer 2: 0(+1)/1           Map 1: 1/1	Reducer 2: 1/1      */
specifier|private
name|String
name|getReport
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|)
block|{
name|StringWriter
name|reportBuffer
init|=
operator|new
name|StringWriter
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
argument_list|<>
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
name|getFailedTaskAttemptCount
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
comment|/*            * We may have missed the start of the vertex due to the 3 seconds interval            */
if|if
condition|(
operator|!
name|perfLogger
operator|.
name|startTimeHasMethod
argument_list|(
name|PerfLogger
operator|.
name|TEZ_RUN_VERTEX
operator|+
name|s
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|TezJobMonitor
operator|.
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
operator|!
name|perfLogger
operator|.
name|endTimeHasMethod
argument_list|(
name|PerfLogger
operator|.
name|TEZ_RUN_VERTEX
operator|+
name|s
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|TezJobMonitor
operator|.
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
if|if
condition|(
operator|!
name|perfLogger
operator|.
name|startTimeHasMethod
argument_list|(
name|PerfLogger
operator|.
name|TEZ_RUN_VERTEX
operator|+
name|s
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|TezJobMonitor
operator|.
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
return|return
name|reportBuffer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|abstract
name|void
name|renderProgress
parameter_list|(
name|ProgressMonitor
name|progressMonitor
parameter_list|)
function_decl|;
specifier|abstract
name|void
name|renderReport
parameter_list|(
name|String
name|report
parameter_list|)
function_decl|;
block|}
comment|/**    * this adds the required progress update to the session state that is used by HS2 to send the    * same information to beeline client when requested.    */
specifier|static
class|class
name|LogToFileFunction
extends|extends
name|BaseUpdateFunction
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LogToFileFunction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|hiveServer2InPlaceProgressEnabled
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_INPLACE_PROGRESS
argument_list|)
decl_stmt|;
name|LogToFileFunction
parameter_list|(
name|TezJobMonitor
name|monitor
parameter_list|)
block|{
name|super
argument_list|(
name|monitor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|renderProgress
parameter_list|(
name|ProgressMonitor
name|progressMonitor
parameter_list|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|updateProgressMonitor
argument_list|(
name|progressMonitor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|renderReport
parameter_list|(
name|String
name|report
parameter_list|)
block|{
if|if
condition|(
name|hiveServer2InPlaceProgressEnabled
condition|)
block|{
name|LOGGER
operator|.
name|info
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|monitor
operator|.
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This used when we want the progress update to printed in the same process typically used via    * hive-cli mode.    */
specifier|static
class|class
name|InPlaceUpdateFunction
extends|extends
name|BaseUpdateFunction
block|{
comment|/**      * Have to use the same instance to render else the number lines printed earlier is lost and the      * screen will print the table again and again.      */
specifier|private
specifier|final
name|InPlaceUpdate
name|inPlaceUpdate
decl_stmt|;
name|InPlaceUpdateFunction
parameter_list|(
name|TezJobMonitor
name|monitor
parameter_list|)
block|{
name|super
argument_list|(
name|monitor
argument_list|)
expr_stmt|;
name|inPlaceUpdate
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
annotation|@
name|Override
specifier|public
name|void
name|renderProgress
parameter_list|(
name|ProgressMonitor
name|progressMonitor
parameter_list|)
block|{
name|inPlaceUpdate
operator|.
name|render
argument_list|(
name|progressMonitor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|renderReport
parameter_list|(
name|String
name|report
parameter_list|)
block|{
name|monitor
operator|.
name|console
operator|.
name|logInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

