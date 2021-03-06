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

begin_comment
comment|/**  * Post execution (success or failure) hook to print hive workload manager events summary.  */
end_comment

begin_class
specifier|public
class|class
name|PostExecWMEventsSummaryPrinter
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
name|PostExecWMEventsSummaryPrinter
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
operator|||
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookContext
operator|.
name|HookType
operator|.
name|ON_FAILURE_HOOK
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
literal|"Executing post execution hook to print workload manager events summary.."
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|LogHelper
name|console
init|=
name|SessionState
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
name|WmContext
name|wmContext
init|=
name|tezTask
operator|.
name|getContext
argument_list|()
operator|.
name|getWmContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|wmContext
operator|!=
literal|null
condition|)
block|{
name|wmContext
operator|.
name|printJson
argument_list|(
name|console
argument_list|)
expr_stmt|;
name|wmContext
operator|.
name|shortPrint
argument_list|(
name|console
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

