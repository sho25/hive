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
name|log
package|;
end_package

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
name|ql
operator|.
name|QueryPlan
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

begin_comment
comment|/**  * PerfLogger.  *  * Can be used to measure and log the time spent by a piece of code.  */
end_comment

begin_class
specifier|public
class|class
name|PerfLogger
block|{
specifier|public
specifier|static
specifier|final
name|String
name|ACQUIRE_READ_WRITE_LOCKS
init|=
literal|"acquireReadWriteLocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COMPILE
init|=
literal|"compile"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PARSE
init|=
literal|"parse"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ANALYZE
init|=
literal|"semanticAnalyze"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DO_AUTHORIZATION
init|=
literal|"doAuthorization"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRIVER_EXECUTE
init|=
literal|"Driver.execute"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INPUT_SUMMARY
init|=
literal|"getInputSummary"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|GET_SPLITS
init|=
literal|"getSplits"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RUN_TASKS
init|=
literal|"runTasks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZE_PLAN
init|=
literal|"serializePlan"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DESERIALIZE_PLAN
init|=
literal|"deserializePlan"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CLONE_PLAN
init|=
literal|"clonePlan"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TASK
init|=
literal|"task."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RELEASE_LOCKS
init|=
literal|"releaseLocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PRUNE_LISTING
init|=
literal|"prune-listing"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PARTITION_RETRIEVING
init|=
literal|"partition-retrieving"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PRE_HOOK
init|=
literal|"PreHook."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|POST_HOOK
init|=
literal|"PostHook."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FAILURE_HOOK
init|=
literal|"FailureHook."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRIVER_RUN
init|=
literal|"Driver.run"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TIME_TO_SUBMIT
init|=
literal|"TimeToSubmit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_SUBMIT_TO_RUNNING
init|=
literal|"TezSubmitToRunningDag"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_BUILD_DAG
init|=
literal|"TezBuildDag"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_SUBMIT_DAG
init|=
literal|"TezSubmitDag"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_RUN_DAG
init|=
literal|"TezRunDag"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_CREATE_VERTEX
init|=
literal|"TezCreateVertex."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_RUN_VERTEX
init|=
literal|"TezRunVertex."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_INITIALIZE_PROCESSOR
init|=
literal|"TezInitializeProcessor"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_RUN_PROCESSOR
init|=
literal|"TezRunProcessor"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEZ_INIT_OPERATORS
init|=
literal|"TezInitializeOperators"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LOAD_HASHTABLE
init|=
literal|"LoadHashtable"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INIT_ORC_RECORD_READER
init|=
literal|"OrcRecordReaderInit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_ORC_SPLITS
init|=
literal|"OrcCreateSplits"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ORC_GET_SPLITS
init|=
literal|"OrcGetSplits"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ORC_GET_BLOCK_LOCATIONS
init|=
literal|"OrcGetBlockLocations"
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
argument_list|<
name|PerfLogger
argument_list|>
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
name|startTimes
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
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
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
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
name|PerfLogger
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|PerfLogger
parameter_list|()
block|{
comment|// Use getPerfLogger to get an instance of PerfLogger
block|}
specifier|public
specifier|static
name|PerfLogger
name|getPerfLogger
parameter_list|()
block|{
return|return
name|getPerfLogger
argument_list|(
literal|false
argument_list|)
return|;
block|}
comment|/**    * Call this function to get an instance of PerfLogger.    *    * Use resetPerfLogger to require a new instance.  Useful at the beginning of execution.    *    * @return Session perflogger if there's a sessionstate, otherwise return the thread local instance    */
specifier|public
specifier|static
name|PerfLogger
name|getPerfLogger
parameter_list|(
name|boolean
name|resetPerfLogger
parameter_list|)
block|{
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|perfLogger
operator|.
name|get
argument_list|()
operator|==
literal|null
operator|||
name|resetPerfLogger
condition|)
block|{
name|perfLogger
operator|.
name|set
argument_list|(
operator|new
name|PerfLogger
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|perfLogger
operator|.
name|get
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getPerfLogger
argument_list|(
name|resetPerfLogger
argument_list|)
return|;
block|}
block|}
comment|/**    * Call this function when you start to measure time spent by a piece of code.    * @param _log the logging object to be used.    * @param method method or ID that identifies this perf log element.    */
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
name|LOG
operator|.
name|info
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
block|}
comment|/**    * Call this function in correspondence of PerfLogBegin to mark the end of the measurement.    * @param _log    * @param method    * @return long duration  the difference between now and startTime, or -1 if startTime is null    */
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
name|long
name|duration
init|=
operator|-
literal|1
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
name|duration
operator|=
name|endTime
operator|-
name|startTime
operator|.
name|longValue
argument_list|()
expr_stmt|;
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
operator|.
name|append
argument_list|(
literal|">"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|sb
argument_list|)
expr_stmt|;
return|return
name|duration
return|;
block|}
comment|/**    * Call this function at the end of processing a query (any time after the last call to PerfLogEnd    * for a given query) to run any cleanup/final steps that need to be run    * @param _log    */
specifier|public
name|void
name|close
parameter_list|(
name|Log
name|_log
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|)
block|{    }
specifier|public
name|Long
name|getStartTime
parameter_list|(
name|String
name|method
parameter_list|)
block|{
return|return
name|startTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
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
return|return
name|endTimes
operator|.
name|get
argument_list|(
name|method
argument_list|)
return|;
block|}
block|}
end_class

end_unit

