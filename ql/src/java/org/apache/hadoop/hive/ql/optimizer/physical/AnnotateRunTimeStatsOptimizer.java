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
name|optimizer
operator|.
name|physical
package|;
end_package

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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Stack
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
name|ql
operator|.
name|ErrorMsg
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
name|mr
operator|.
name|MapRedTask
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
name|spark
operator|.
name|SparkTask
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|Rule
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
name|metadata
operator|.
name|HiveException
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
name|ParseContext
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
name|plan
operator|.
name|OperatorDesc
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
name|SparkWork
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
name|TezWork
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
name|StatsPublisher
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
name|FSStatsPublisher
import|;
end_import

begin_class
specifier|public
class|class
name|AnnotateRunTimeStatsOptimizer
implements|implements
name|PhysicalPlanResolver
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
name|AnnotateRunTimeStatsOptimizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
class|class
name|AnnotateRunTimeStatsDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
specifier|final
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|public
name|AnnotateRunTimeStatsDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|,
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|rules
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|ops
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|currTask
operator|instanceof
name|MapRedTask
condition|)
block|{
name|MapRedTask
name|mr
init|=
operator|(
name|MapRedTask
operator|)
name|currTask
decl_stmt|;
name|ops
operator|.
name|addAll
argument_list|(
name|mr
operator|.
name|getWork
argument_list|()
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currTask
operator|instanceof
name|TezTask
condition|)
block|{
name|TezWork
name|work
init|=
operator|(
operator|(
name|TezTask
operator|)
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|ops
operator|.
name|addAll
argument_list|(
name|w
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currTask
operator|instanceof
name|SparkTask
condition|)
block|{
name|SparkWork
name|sparkWork
init|=
operator|(
name|SparkWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|ops
operator|.
name|addAll
argument_list|(
name|w
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|setOrAnnotateStats
argument_list|(
name|ops
argument_list|,
name|physicalContext
operator|.
name|getParseContext
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|setOrAnnotateStats
parameter_list|(
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|ops
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|ops
control|)
block|{
if|if
condition|(
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplainAnalyze
argument_list|()
operator|==
name|AnalyzeState
operator|.
name|RUNNING
condition|)
block|{
name|setRuntimeStatsDir
argument_list|(
name|op
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplainAnalyze
argument_list|()
operator|==
name|AnalyzeState
operator|.
name|ANALYZING
condition|)
block|{
name|annotateRuntimeStats
argument_list|(
name|op
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unexpected stats in AnnotateWithRunTimeStatistics."
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|setRuntimeStatsDir
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|OperatorDesc
name|conf
init|=
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"setRuntimeStatsDir for "
operator|+
name|op
operator|.
name|getOperatorId
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|path
init|=
operator|new
name|Path
argument_list|(
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplainConfig
argument_list|()
operator|.
name|getExplainRootPath
argument_list|()
argument_list|,
name|op
operator|.
name|getOperatorId
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|StatsPublisher
name|statsPublisher
init|=
operator|new
name|FSStatsPublisher
argument_list|()
decl_stmt|;
name|StatsCollectionContext
name|runtimeStatsContext
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|runtimeStatsContext
operator|.
name|setStatsTmpDir
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|init
argument_list|(
name|runtimeStatsContext
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"StatsPublishing error: StatsPublisher is not initialized."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_NOT_OBTAINED
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
name|conf
operator|.
name|setRuntimeStatsTmpDir
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"skip setRuntimeStatsDir for "
operator|+
name|op
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" because OperatorDesc is null"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|annotateRuntimeStats
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
block|{
name|Long
name|runTimeNumRows
init|=
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplainConfig
argument_list|()
operator|.
name|getOpIdToRuntimeNumRows
argument_list|()
operator|.
name|get
argument_list|(
name|op
operator|.
name|getOperatorId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
operator|&&
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getStatistics
argument_list|()
operator|!=
literal|null
operator|&&
name|runTimeNumRows
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"annotateRuntimeStats for "
operator|+
name|op
operator|.
name|getOperatorId
argument_list|()
argument_list|)
expr_stmt|;
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getStatistics
argument_list|()
operator|.
name|setRunTimeNumRows
argument_list|(
name|runTimeNumRows
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"skip annotateRuntimeStats for "
operator|+
name|op
operator|.
name|getOperatorId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|AnnotateRunTimeStatsDispatcher
argument_list|(
name|pctx
argument_list|,
name|opRules
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
specifier|public
name|void
name|resolve
parameter_list|(
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|getAllOperatorsForSimpleFetch
argument_list|(
name|opSet
argument_list|)
decl_stmt|;
name|setOrAnnotateStats
argument_list|(
name|ops
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllOperatorsForSimpleFetch
parameter_list|(
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
parameter_list|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnSet
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opStack
init|=
operator|new
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// add all children
name|opStack
operator|.
name|addAll
argument_list|(
name|opSet
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|opStack
operator|.
name|empty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|opStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|returnSet
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opStack
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|returnSet
return|;
block|}
block|}
end_class

end_unit

