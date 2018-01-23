begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hooks
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|llap
operator|.
name|counters
operator|.
name|LlapIOCounters
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
name|HiveInputCounters
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
name|FileSystemCounter
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
name|TezTask
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

begin_comment
comment|/**  * Post execution hook to print hive tez counters to console error stream.  */
end_comment

begin_class
specifier|public
class|class
name|PostExecTezSummaryPrinter
implements|implements
name|ExecuteWithHookContext
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
name|PostExecTezSummaryPrinter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
assert|assert
operator|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookContext
operator|.
name|HookType
operator|.
name|POST_EXEC_HOOK
operator|)
assert|;
name|HiveConf
name|conf
init|=
name|hookContext
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
literal|"tez"
operator|.
name|equals
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
argument_list|)
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing post execution hook to print tez summary.."
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|SessionState
operator|.
name|LogHelper
name|console
init|=
name|ss
operator|.
name|getConsole
argument_list|()
decl_stmt|;
name|QueryPlan
name|plan
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
decl_stmt|;
if|if
condition|(
name|plan
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|TezTask
argument_list|>
name|rootTasks
init|=
name|Utilities
operator|.
name|getTezTasks
argument_list|(
name|plan
operator|.
name|getRootTasks
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TezTask
name|tezTask
range|:
name|rootTasks
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Printing summary for tez task: "
operator|+
name|tezTask
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TezCounters
name|counters
init|=
name|tezTask
operator|.
name|getTezCounters
argument_list|()
decl_stmt|;
if|if
condition|(
name|counters
operator|!=
literal|null
condition|)
block|{
name|String
name|hiveCountersGroup
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECOUNTERGROUP
argument_list|)
decl_stmt|;
for|for
control|(
name|CounterGroup
name|group
range|:
name|counters
control|)
block|{
if|if
condition|(
name|hiveCountersGroup
operator|.
name|equals
argument_list|(
name|group
operator|.
name|getDisplayName
argument_list|()
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|tezTask
operator|.
name|getId
argument_list|()
operator|+
literal|" HIVE COUNTERS:"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|TezCounter
name|counter
range|:
name|group
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"   "
operator|+
name|counter
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|": "
operator|+
name|counter
operator|.
name|getValue
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|group
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HiveInputCounters
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|tezTask
operator|.
name|getId
argument_list|()
operator|+
literal|" INPUT COUNTERS:"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|TezCounter
name|counter
range|:
name|group
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"   "
operator|+
name|counter
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|": "
operator|+
name|counter
operator|.
name|getValue
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|group
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|FileSystemCounter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|tezTask
operator|.
name|getId
argument_list|()
operator|+
literal|" FILE SYSTEM COUNTERS:"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|TezCounter
name|counter
range|:
name|group
control|)
block|{
comment|// HDFS counters should be relatively consistent across test runs when compared to
comment|// local file system counters
if|if
condition|(
name|counter
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"HDFS"
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"   "
operator|+
name|counter
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|": "
operator|+
name|counter
operator|.
name|getValue
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|group
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|LlapIOCounters
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|tezTask
operator|.
name|getId
argument_list|()
operator|+
literal|" LLAP IO COUNTERS:"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|testSafeCounters
init|=
name|LlapIOCounters
operator|.
name|testSafeCounterNames
argument_list|()
decl_stmt|;
for|for
control|(
name|TezCounter
name|counter
range|:
name|group
control|)
block|{
if|if
condition|(
name|testSafeCounters
operator|.
name|contains
argument_list|(
name|counter
operator|.
name|getDisplayName
argument_list|()
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"   "
operator|+
name|counter
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|": "
operator|+
name|counter
operator|.
name|getValue
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

