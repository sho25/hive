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
name|ArrayList
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
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|metastore
operator|.
name|api
operator|.
name|Schema
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
name|QueryDisplay
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
name|QueryInfo
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
name|ql
operator|.
name|exec
operator|.
name|ExplainTask
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
name|FetchTask
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
name|parse
operator|.
name|ASTNode
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
name|parse
operator|.
name|HiveParser
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
name|parse
operator|.
name|HiveSemanticAnalyzerHook
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
name|parse
operator|.
name|HiveSemanticAnalyzerHookContext
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
name|parse
operator|.
name|SemanticException
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
name|processors
operator|.
name|CommandProcessorResponse
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

begin_comment
comment|/**  * Enables to use re-execution logics.  *  * Covers the IDriver interface, handles query re-execution; and asks clear questions from the underlying re-execution plugins.  */
end_comment

begin_class
specifier|public
class|class
name|ReExecDriver
implements|implements
name|IDriver
block|{
specifier|private
class|class
name|HandleReOptimizationExplain
implements|implements
name|HiveSemanticAnalyzerHook
block|{
annotation|@
name|Override
specifier|public
name|ASTNode
name|preAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|ast
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_EXPLAIN
condition|)
block|{
name|int
name|childCount
init|=
name|ast
operator|.
name|getChildCount
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|childCount
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|ast
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|KW_REOPTIMIZATION
condition|)
block|{
name|explainReOptimization
operator|=
literal|true
expr_stmt|;
name|ast
operator|.
name|deleteChild
argument_list|(
name|i
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|explainReOptimization
operator|&&
name|firstExecution
argument_list|()
condition|)
block|{
name|Tree
name|execTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|execTree
operator|.
name|setParent
argument_list|(
name|ast
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|.
name|getParent
argument_list|()
operator|.
name|setChild
argument_list|(
literal|0
argument_list|,
name|execTree
argument_list|)
expr_stmt|;
return|return
operator|(
name|ASTNode
operator|)
name|execTree
return|;
block|}
block|}
return|return
name|ast
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{     }
block|}
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
name|ReExecDriver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|explainReOptimization
decl_stmt|;
specifier|private
name|Driver
name|coreDriver
decl_stmt|;
specifier|private
name|QueryState
name|queryState
decl_stmt|;
specifier|private
name|String
name|currentQuery
decl_stmt|;
specifier|private
name|int
name|executionIndex
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|IReExecutionPlugin
argument_list|>
name|plugins
decl_stmt|;
annotation|@
name|Override
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|queryState
operator|.
name|getConf
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|firstExecution
parameter_list|()
block|{
return|return
name|executionIndex
operator|==
literal|0
return|;
block|}
specifier|public
name|ReExecDriver
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryInfo
name|queryInfo
parameter_list|,
name|ArrayList
argument_list|<
name|IReExecutionPlugin
argument_list|>
name|plugins
parameter_list|)
block|{
name|this
operator|.
name|queryState
operator|=
name|queryState
expr_stmt|;
name|coreDriver
operator|=
operator|new
name|Driver
argument_list|(
name|queryState
argument_list|,
name|queryInfo
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|coreDriver
operator|.
name|getHookRunner
argument_list|()
operator|.
name|addSemanticAnalyzerHook
argument_list|(
operator|new
name|HandleReOptimizationExplain
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|plugins
operator|=
name|plugins
expr_stmt|;
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|p
operator|.
name|initialize
argument_list|(
name|coreDriver
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|compile
parameter_list|(
name|String
name|command
parameter_list|,
name|boolean
name|resetTaskIds
parameter_list|)
block|{
return|return
name|coreDriver
operator|.
name|compile
argument_list|(
name|command
argument_list|,
name|resetTaskIds
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|compileAndRespond
parameter_list|(
name|String
name|statement
parameter_list|)
throws|throws
name|CommandProcessorException
block|{
name|currentQuery
operator|=
name|statement
expr_stmt|;
return|return
name|coreDriver
operator|.
name|compileAndRespond
argument_list|(
name|statement
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|QueryPlan
name|getPlan
parameter_list|()
block|{
return|return
name|coreDriver
operator|.
name|getPlan
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|QueryState
name|getQueryState
parameter_list|()
block|{
return|return
name|queryState
return|;
block|}
annotation|@
name|Override
specifier|public
name|QueryDisplay
name|getQueryDisplay
parameter_list|()
block|{
return|return
name|coreDriver
operator|.
name|getQueryDisplay
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOperationId
parameter_list|(
name|String
name|operationId
parameter_list|)
block|{
name|coreDriver
operator|.
name|setOperationId
argument_list|(
name|operationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|()
throws|throws
name|CommandProcessorException
block|{
name|executionIndex
operator|=
literal|0
expr_stmt|;
name|int
name|maxExecutuions
init|=
literal|1
operator|+
name|coreDriver
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_QUERY_MAX_REEXECUTION_COUNT
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|executionIndex
operator|++
expr_stmt|;
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|p
operator|.
name|beforeExecute
argument_list|(
name|executionIndex
argument_list|,
name|explainReOptimization
argument_list|)
expr_stmt|;
block|}
name|coreDriver
operator|.
name|getContext
argument_list|()
operator|.
name|setExecutionIndex
argument_list|(
name|executionIndex
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Execution #{} of query"
argument_list|,
name|executionIndex
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|cpr
init|=
literal|null
decl_stmt|;
name|CommandProcessorException
name|cpe
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cpr
operator|=
name|coreDriver
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|cpe
operator|=
name|e
expr_stmt|;
block|}
name|PlanMapper
name|oldPlanMapper
init|=
name|coreDriver
operator|.
name|getPlanMapper
argument_list|()
decl_stmt|;
name|afterExecute
argument_list|(
name|oldPlanMapper
argument_list|,
name|cpr
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|boolean
name|shouldReExecute
init|=
name|explainReOptimization
operator|&&
name|executionIndex
operator|==
literal|1
decl_stmt|;
name|shouldReExecute
operator||=
name|cpr
operator|==
literal|null
operator|&&
name|shouldReExecute
argument_list|()
expr_stmt|;
if|if
condition|(
name|executionIndex
operator|>=
name|maxExecutuions
operator|||
operator|!
name|shouldReExecute
condition|)
block|{
if|if
condition|(
name|cpr
operator|!=
literal|null
condition|)
block|{
return|return
name|cpr
return|;
block|}
else|else
block|{
throw|throw
name|cpe
throw|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Preparing to re-execute query"
argument_list|)
expr_stmt|;
name|prepareToReExecute
argument_list|()
expr_stmt|;
try|try
block|{
name|coreDriver
operator|.
name|compileAndRespond
argument_list|(
name|currentQuery
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Recompilation of the query failed; this is unexpected."
argument_list|)
expr_stmt|;
comment|// FIXME: somehow place pointers that re-execution compilation have failed; the query have been successfully compiled before?
throw|throw
name|e
throw|;
block|}
name|PlanMapper
name|newPlanMapper
init|=
name|coreDriver
operator|.
name|getPlanMapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|explainReOptimization
operator|&&
operator|!
name|shouldReExecuteAfterCompile
argument_list|(
name|oldPlanMapper
argument_list|,
name|newPlanMapper
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"re-running the query would probably not yield better results; returning with last error"
argument_list|)
expr_stmt|;
comment|// FIXME: retain old error; or create a new one?
return|return
name|cpr
return|;
block|}
block|}
block|}
specifier|private
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
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|p
operator|.
name|afterExecute
argument_list|(
name|planMapper
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|shouldReExecuteAfterCompile
parameter_list|(
name|PlanMapper
name|oldPlanMapper
parameter_list|,
name|PlanMapper
name|newPlanMapper
parameter_list|)
block|{
name|boolean
name|ret
init|=
literal|false
decl_stmt|;
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|boolean
name|shouldReExecute
init|=
name|p
operator|.
name|shouldReExecute
argument_list|(
name|executionIndex
argument_list|,
name|oldPlanMapper
argument_list|,
name|newPlanMapper
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"{}.shouldReExecuteAfterCompile = {}"
argument_list|,
name|p
argument_list|,
name|shouldReExecute
argument_list|)
expr_stmt|;
name|ret
operator||=
name|shouldReExecute
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|boolean
name|shouldReExecute
parameter_list|()
block|{
name|boolean
name|ret
init|=
literal|false
decl_stmt|;
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|boolean
name|shouldReExecute
init|=
name|p
operator|.
name|shouldReExecute
argument_list|(
name|executionIndex
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"{}.shouldReExecute = {}"
argument_list|,
name|p
argument_list|,
name|shouldReExecute
argument_list|)
expr_stmt|;
name|ret
operator||=
name|shouldReExecute
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|CommandProcessorException
block|{
name|compileAndRespond
argument_list|(
name|command
argument_list|)
expr_stmt|;
return|return
name|run
argument_list|()
return|;
block|}
specifier|private
name|void
name|prepareToReExecute
parameter_list|()
block|{
for|for
control|(
name|IReExecutionPlugin
name|p
range|:
name|plugins
control|)
block|{
name|p
operator|.
name|prepareToReExecute
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getResults
parameter_list|(
name|List
name|res
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|coreDriver
operator|.
name|getResults
argument_list|(
name|res
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxRows
parameter_list|(
name|int
name|maxRows
parameter_list|)
block|{
name|coreDriver
operator|.
name|setMaxRows
argument_list|(
name|maxRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FetchTask
name|getFetchTask
parameter_list|()
block|{
return|return
name|coreDriver
operator|.
name|getFetchTask
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Schema
name|getSchema
parameter_list|()
block|{
if|if
condition|(
name|explainReOptimization
condition|)
block|{
return|return
operator|new
name|Schema
argument_list|(
name|ExplainTask
operator|.
name|getResultSchema
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
return|return
name|coreDriver
operator|.
name|getSchema
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFetchingTable
parameter_list|()
block|{
return|return
name|coreDriver
operator|.
name|isFetchingTable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetFetch
parameter_list|()
throws|throws
name|IOException
block|{
name|coreDriver
operator|.
name|resetFetch
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|coreDriver
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|destroy
parameter_list|()
block|{
name|coreDriver
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Context
name|getContext
parameter_list|()
block|{
return|return
name|coreDriver
operator|.
name|getContext
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|setStatsSource
parameter_list|(
name|StatsSource
name|statsSource
parameter_list|)
block|{
name|coreDriver
operator|.
name|setStatsSource
argument_list|(
name|statsSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasResultSet
parameter_list|()
block|{
return|return
name|explainReOptimization
operator|||
name|coreDriver
operator|.
name|hasResultSet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

