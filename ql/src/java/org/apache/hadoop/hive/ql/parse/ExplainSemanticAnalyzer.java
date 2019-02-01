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
name|parse
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|FileUtils
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
name|StatsSetupConst
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
name|ValidTxnList
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
name|ValidTxnWriteIdList
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
name|FieldSchema
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
name|exec
operator|.
name|TaskFactory
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
name|ExplainConfiguration
operator|.
name|AnalyzeState
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
name|ExplainConfiguration
operator|.
name|VectorizationDetailLevel
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
name|ExplainWork
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
name|StatsAggregator
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
name|StatsCollectionContext
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
name|fs
operator|.
name|FSStatsAggregator
import|;
end_import

begin_comment
comment|/**  * ExplainSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExplainSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldList
decl_stmt|;
name|ExplainConfiguration
name|config
decl_stmt|;
specifier|public
name|ExplainSemanticAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
name|config
operator|=
operator|new
name|ExplainConfiguration
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|int
name|childCount
init|=
name|ast
operator|.
name|getChildCount
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
comment|// Skip TOK_QUERY.
while|while
condition|(
name|i
operator|<
name|childCount
condition|)
block|{
name|int
name|explainOptions
init|=
name|ast
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_FORMATTED
condition|)
block|{
name|config
operator|.
name|setFormatted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_EXTENDED
condition|)
block|{
name|config
operator|.
name|setExtended
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_DEPENDENCY
condition|)
block|{
name|config
operator|.
name|setDependency
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_CBO
condition|)
block|{
name|config
operator|.
name|setCbo
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_COST
condition|)
block|{
name|config
operator|.
name|setCboCost
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_JOINCOST
condition|)
block|{
name|config
operator|.
name|setCboJoinCost
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_LOGICAL
condition|)
block|{
name|config
operator|.
name|setLogical
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_AUTHORIZATION
condition|)
block|{
name|config
operator|.
name|setAuthorize
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_ANALYZE
condition|)
block|{
name|config
operator|.
name|setAnalyze
argument_list|(
name|AnalyzeState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|config
operator|.
name|setExplainRootPath
argument_list|(
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_VECTORIZATION
condition|)
block|{
name|config
operator|.
name|setVectorization
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|+
literal|1
operator|<
name|childCount
condition|)
block|{
name|int
name|vectorizationOption
init|=
name|ast
operator|.
name|getChild
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
comment|// [ONLY]
if|if
condition|(
name|vectorizationOption
operator|==
name|HiveParser
operator|.
name|TOK_ONLY
condition|)
block|{
name|config
operator|.
name|setVectorizationOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
if|if
condition|(
name|i
operator|+
literal|1
operator|>=
name|childCount
condition|)
block|{
break|break;
block|}
name|vectorizationOption
operator|=
name|ast
operator|.
name|getChild
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
expr_stmt|;
block|}
comment|// [SUMMARY|OPERATOR|EXPRESSION|DETAIL]
if|if
condition|(
name|vectorizationOption
operator|==
name|HiveParser
operator|.
name|TOK_SUMMARY
condition|)
block|{
name|config
operator|.
name|setVectorizationDetailLevel
argument_list|(
name|VectorizationDetailLevel
operator|.
name|SUMMARY
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vectorizationOption
operator|==
name|HiveParser
operator|.
name|TOK_OPERATOR
condition|)
block|{
name|config
operator|.
name|setVectorizationDetailLevel
argument_list|(
name|VectorizationDetailLevel
operator|.
name|OPERATOR
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vectorizationOption
operator|==
name|HiveParser
operator|.
name|TOK_EXPRESSION
condition|)
block|{
name|config
operator|.
name|setVectorizationDetailLevel
argument_list|(
name|VectorizationDetailLevel
operator|.
name|EXPRESSION
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vectorizationOption
operator|==
name|HiveParser
operator|.
name|TOK_DETAIL
condition|)
block|{
name|config
operator|.
name|setVectorizationDetailLevel
argument_list|(
name|VectorizationDetailLevel
operator|.
name|DETAIL
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_LOCKS
condition|)
block|{
name|config
operator|.
name|setLocks
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_AST
condition|)
block|{
name|config
operator|.
name|setAst
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_DEBUG
condition|)
block|{
name|config
operator|.
name|setDebug
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// UNDONE: UNKNOWN OPTION?
block|}
name|i
operator|++
expr_stmt|;
block|}
name|ctx
operator|.
name|setExplainConfig
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setExplainPlan
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ASTNode
name|input
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// explain analyze is composed of two steps
comment|// step 1 (ANALYZE_STATE.RUNNING), run the query and collect the runtime #rows
comment|// step 2 (ANALYZE_STATE.ANALYZING), explain the query and provide the runtime #rows collected.
if|if
condition|(
name|config
operator|.
name|getAnalyze
argument_list|()
operator|==
name|AnalyzeState
operator|.
name|RUNNING
condition|)
block|{
name|String
name|query
init|=
name|ctx
operator|.
name|getTokenRewriteStream
argument_list|()
operator|.
name|toString
argument_list|(
name|input
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|input
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Explain analyze (running phase) for query "
operator|+
name|query
argument_list|)
expr_stmt|;
name|conf
operator|.
name|unset
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|)
expr_stmt|;
name|conf
operator|.
name|unset
argument_list|(
name|ValidTxnWriteIdList
operator|.
name|VALID_TABLES_WRITEIDS_KEY
argument_list|)
expr_stmt|;
name|Context
name|runCtx
init|=
literal|null
decl_stmt|;
try|try
block|{
name|runCtx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// runCtx and ctx share the configuration, but not isExplainPlan()
name|runCtx
operator|.
name|setExplainConfig
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|,
name|runCtx
argument_list|,
name|queryState
operator|.
name|getLineageState
argument_list|()
argument_list|)
decl_stmt|;
name|CommandProcessorResponse
name|ret
init|=
name|driver
operator|.
name|run
argument_list|(
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|.
name|getResponseCode
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Note that we need to call getResults for simple fetch optimization.
comment|// However, we need to skip all the results.
while|while
condition|(
name|driver
operator|.
name|getResults
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
condition|)
block|{           }
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ret
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|ret
operator|.
name|getException
argument_list|()
argument_list|)
throw|;
block|}
name|config
operator|.
name|setOpIdToRuntimeNumRows
argument_list|(
name|aggregateStats
argument_list|(
name|config
operator|.
name|getExplainRootPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
name|ctx
operator|.
name|resetOpContext
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|resetStream
argument_list|()
expr_stmt|;
name|TaskFactory
operator|.
name|resetId
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Explain analyze (analyzing phase) for query "
operator|+
name|query
argument_list|)
expr_stmt|;
name|config
operator|.
name|setAnalyze
argument_list|(
name|AnalyzeState
operator|.
name|ANALYZING
argument_list|)
expr_stmt|;
block|}
comment|//Creating new QueryState unfortunately causes all .q.out to change - do this in a separate ticket
comment|//Sharing QueryState between generating the plan and executing the query seems bad
comment|//BaseSemanticAnalyzer sem = SemanticAnalyzerFactory.get(new QueryState(queryState.getConf()), input);
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|sem
operator|.
name|analyze
argument_list|(
name|input
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|sem
operator|.
name|validate
argument_list|()
expr_stmt|;
name|inputs
operator|=
name|sem
operator|.
name|getInputs
argument_list|()
expr_stmt|;
name|outputs
operator|=
name|sem
operator|.
name|getOutputs
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|setResFile
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|tasks
init|=
name|sem
operator|.
name|getAllRootTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|tasks
operator|==
literal|null
condition|)
block|{
name|tasks
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
name|FetchTask
name|fetchTask
init|=
name|sem
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchTask
operator|!=
literal|null
condition|)
block|{
comment|// Initialize fetch work such that operator tree will be constructed.
name|fetchTask
operator|.
name|getWork
argument_list|()
operator|.
name|initializeForFetch
argument_list|(
name|ctx
operator|.
name|getOpContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ParseContext
name|pCtx
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|sem
operator|instanceof
name|SemanticAnalyzer
condition|)
block|{
name|pCtx
operator|=
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|getParseContext
argument_list|()
expr_stmt|;
block|}
name|config
operator|.
name|setUserLevelExplain
argument_list|(
operator|!
name|config
operator|.
name|isExtended
argument_list|()
operator|&&
operator|!
name|config
operator|.
name|isFormatted
argument_list|()
operator|&&
operator|!
name|config
operator|.
name|isDependency
argument_list|()
operator|&&
operator|!
name|config
operator|.
name|isCbo
argument_list|()
operator|&&
operator|!
name|config
operator|.
name|isLogical
argument_list|()
operator|&&
operator|!
name|config
operator|.
name|isAuthorize
argument_list|()
operator|&&
operator|(
operator|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXPLAIN_USER
argument_list|)
operator|&&
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
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
operator|)
operator|||
operator|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SPARK_EXPLAIN_USER
argument_list|)
operator|&&
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
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
operator|)
operator|)
argument_list|)
expr_stmt|;
name|ExplainWork
name|work
init|=
operator|new
name|ExplainWork
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|pCtx
argument_list|,
name|tasks
argument_list|,
name|fetchTask
argument_list|,
name|input
argument_list|,
name|sem
argument_list|,
name|config
argument_list|,
name|ctx
operator|.
name|getCboInfo
argument_list|()
argument_list|,
name|ctx
operator|.
name|getOptimizedSql
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCalcitePlan
argument_list|()
argument_list|)
decl_stmt|;
name|work
operator|.
name|setAppendTaskType
argument_list|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEEXPLAINDEPENDENCYAPPENDTASKTYPES
argument_list|)
argument_list|)
expr_stmt|;
name|ExplainTask
name|explTask
init|=
operator|(
name|ExplainTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|)
decl_stmt|;
name|fieldList
operator|=
name|explTask
operator|.
name|getResultSchema
argument_list|()
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|explTask
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aggregateStats
parameter_list|(
name|Path
name|localTmpPath
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|opIdToRuntimeNumRows
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
comment|// localTmpPath is the root of all the stats.
comment|// Under it, there will be SEL_1/statsfiles, SEL_2/statsfiles etc where SEL_1 and SEL_2 are the op ids.
name|FileSystem
name|fs
decl_stmt|;
name|FileStatus
index|[]
name|statuses
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|localTmpPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|statuses
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|localTmpPath
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
expr_stmt|;
comment|// statuses can be null if it is DDL, etc
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|statuses
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|statuses
control|)
block|{
if|if
condition|(
name|status
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|StatsCollectionContext
name|scc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
index|[]
name|names
init|=
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|opId
init|=
name|names
index|[
name|names
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|scc
operator|.
name|setStatsTmpDir
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|StatsAggregator
name|statsAggregator
init|=
operator|new
name|FSStatsAggregator
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|statsAggregator
operator|.
name|connect
argument_list|(
name|scc
argument_list|)
condition|)
block|{
comment|// -1 means that there is no stats
name|opIdToRuntimeNumRows
operator|.
name|put
argument_list|(
name|opId
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|value
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|""
argument_list|,
name|StatsSetupConst
operator|.
name|RUN_TIME_ROW_COUNT
argument_list|)
decl_stmt|;
name|opIdToRuntimeNumRows
operator|.
name|put
argument_list|(
name|opId
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|statsAggregator
operator|!=
literal|null
condition|)
block|{
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|scc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|opIdToRuntimeNumRows
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getResultSchema
parameter_list|()
block|{
return|return
name|fieldList
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|skipAuthorization
parameter_list|()
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
init|=
name|getRootTasks
argument_list|()
decl_stmt|;
assert|assert
name|rootTasks
operator|!=
literal|null
operator|&&
name|rootTasks
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|Task
name|task
init|=
name|rootTasks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|task
operator|instanceof
name|ExplainTask
operator|&&
operator|(
operator|(
name|ExplainTask
operator|)
name|task
operator|)
operator|.
name|getWork
argument_list|()
operator|.
name|isAuthorize
argument_list|()
return|;
block|}
block|}
end_class

end_unit

