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
name|reexec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Driver
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
name|Operator
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
name|mapjoin
operator|.
name|MapJoinMemoryExhaustionError
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
name|hooks
operator|.
name|ExecuteWithHookContext
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
name|hooks
operator|.
name|HookContext
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
name|hooks
operator|.
name|HookContext
operator|.
name|HookType
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
name|mapper
operator|.
name|PlanMapper
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
name|mapper
operator|.
name|StatsSource
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
name|mapper
operator|.
name|StatsSources
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
name|stats
operator|.
name|OperatorStatsReaderHook
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
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|ReOptimizePlugin
implements|implements
name|IReExecutionPlugin
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
name|ReOptimizePlugin
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|retryPossible
decl_stmt|;
specifier|private
name|Driver
name|coreDriver
decl_stmt|;
specifier|private
name|OperatorStatsReaderHook
name|statsReaderHook
decl_stmt|;
specifier|private
name|boolean
name|alwaysCollectStats
decl_stmt|;
class|class
name|LocalHook
implements|implements
name|ExecuteWithHookContext
block|{
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
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookType
operator|.
name|ON_FAILURE_HOOK
condition|)
block|{
name|Throwable
name|exception
init|=
name|hookContext
operator|.
name|getException
argument_list|()
decl_stmt|;
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
block|{
name|String
name|message
init|=
name|exception
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|message
operator|!=
literal|null
condition|)
block|{
name|boolean
name|isOOM
init|=
name|message
operator|.
name|contains
argument_list|(
name|MapJoinMemoryExhaustionError
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|message
operator|.
name|contains
argument_list|(
name|OutOfMemoryError
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|message
operator|.
name|contains
argument_list|(
literal|"Vertex failed,"
argument_list|)
operator|&&
name|isOOM
condition|)
block|{
name|retryPossible
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"ReOptimization: retryPossible: {}"
argument_list|,
name|retryPossible
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Driver
name|driver
parameter_list|)
block|{
name|coreDriver
operator|=
name|driver
expr_stmt|;
name|coreDriver
operator|.
name|getHookRunner
argument_list|()
operator|.
name|addOnFailureHook
argument_list|(
operator|new
name|LocalHook
argument_list|()
argument_list|)
expr_stmt|;
name|statsReaderHook
operator|=
operator|new
name|OperatorStatsReaderHook
argument_list|()
expr_stmt|;
name|coreDriver
operator|.
name|getHookRunner
argument_list|()
operator|.
name|addOnFailureHook
argument_list|(
name|statsReaderHook
argument_list|)
expr_stmt|;
name|coreDriver
operator|.
name|getHookRunner
argument_list|()
operator|.
name|addPostHook
argument_list|(
name|statsReaderHook
argument_list|)
expr_stmt|;
name|alwaysCollectStats
operator|=
name|driver
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_QUERY_REEXECUTION_ALWAYS_COLLECT_OPERATOR_STATS
argument_list|)
expr_stmt|;
name|statsReaderHook
operator|.
name|setCollectOnSuccess
argument_list|(
name|alwaysCollectStats
argument_list|)
expr_stmt|;
name|coreDriver
operator|.
name|setStatsSource
argument_list|(
name|getStatsSource
argument_list|(
name|driver
operator|.
name|getConf
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
enum|enum
name|StatsSourceMode
block|{
name|query
block|,
name|hiveserver
block|;   }
specifier|private
name|StatsSource
name|getStatsSource
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|StatsSourceMode
name|mode
init|=
name|StatsSourceMode
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_QUERY_REEXECUTION_STATS_PERSISTENCE
argument_list|)
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|mode
condition|)
block|{
case|case
name|query
case|:
return|return
operator|new
name|StatsSources
operator|.
name|MapBackedStatsSource
argument_list|()
return|;
case|case
name|hiveserver
case|:
return|return
name|StatsSources
operator|.
name|globalStatsSource
argument_list|(
name|conf
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown StatsSource setting: "
operator|+
name|mode
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldReExecute
parameter_list|(
name|int
name|executionNum
parameter_list|)
block|{
return|return
name|retryPossible
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareToReExecute
parameter_list|()
block|{
name|statsReaderHook
operator|.
name|setCollectOnSuccess
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|retryPossible
operator|=
literal|false
expr_stmt|;
name|coreDriver
operator|.
name|setStatsSource
argument_list|(
name|StatsSources
operator|.
name|getStatsSourceContaining
argument_list|(
name|coreDriver
operator|.
name|getStatsSource
argument_list|()
argument_list|,
name|coreDriver
operator|.
name|getPlanMapper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldReExecute
parameter_list|(
name|int
name|executionNum
parameter_list|,
name|PlanMapper
name|oldPlanMapper
parameter_list|,
name|PlanMapper
name|newPlanMapper
parameter_list|)
block|{
name|boolean
name|planDidChange
init|=
operator|!
name|planEquals
argument_list|(
name|oldPlanMapper
argument_list|,
name|newPlanMapper
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"planDidChange: {}"
argument_list|,
name|planDidChange
argument_list|)
expr_stmt|;
return|return
name|planDidChange
return|;
block|}
specifier|private
name|boolean
name|planEquals
parameter_list|(
name|PlanMapper
name|pmL
parameter_list|,
name|PlanMapper
name|pmR
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|>
name|opsL
init|=
name|getRootOps
argument_list|(
name|pmL
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|>
name|opsR
init|=
name|getRootOps
argument_list|(
name|pmR
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Operator
argument_list|>
name|itL
init|=
name|opsL
operator|.
name|iterator
argument_list|()
init|;
name|itL
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|opL
init|=
name|itL
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Operator
argument_list|>
name|itR
init|=
name|opsR
operator|.
name|iterator
argument_list|()
init|;
name|itR
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|opR
init|=
name|itR
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|opL
operator|.
name|logicalEqualsTree
argument_list|(
name|opR
argument_list|)
condition|)
block|{
name|itL
operator|.
name|remove
argument_list|()
expr_stmt|;
name|itR
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|opsL
operator|.
name|isEmpty
argument_list|()
operator|&&
name|opsR
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|Operator
argument_list|>
name|getRootOps
parameter_list|(
name|PlanMapper
name|pmL
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|>
name|ops
init|=
name|pmL
operator|.
name|getAll
argument_list|(
name|Operator
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Operator
argument_list|>
name|iterator
init|=
name|ops
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Operator
name|o
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|o
operator|.
name|getNumChild
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|ops
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeExecute
parameter_list|(
name|int
name|executionIndex
parameter_list|,
name|boolean
name|explainReOptimization
parameter_list|)
block|{
if|if
condition|(
name|explainReOptimization
condition|)
block|{
name|statsReaderHook
operator|.
name|setCollectOnSuccess
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterExecute
parameter_list|(
name|PlanMapper
name|planMapper
parameter_list|,
name|boolean
name|success
parameter_list|)
block|{
if|if
condition|(
name|alwaysCollectStats
condition|)
block|{
name|coreDriver
operator|.
name|setStatsSource
argument_list|(
name|StatsSources
operator|.
name|getStatsSourceContaining
argument_list|(
name|coreDriver
operator|.
name|getStatsSource
argument_list|()
argument_list|,
name|planMapper
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

