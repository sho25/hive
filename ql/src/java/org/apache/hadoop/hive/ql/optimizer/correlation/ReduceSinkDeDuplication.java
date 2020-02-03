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
name|correlation
package|;
end_package

begin_import
import|import static
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
operator|.
name|HIVECONVERTJOIN
import|;
end_import

begin_import
import|import static
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
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASK
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
name|LinkedHashMap
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
name|Stack
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
name|exec
operator|.
name|GroupByOperator
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
name|JoinOperator
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
name|ReduceSinkOperator
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
name|SelectOperator
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
name|DefaultRuleDispatcher
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
name|SemanticDispatcher
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
name|SemanticGraphWalker
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
name|SemanticNodeProcessor
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
name|NodeProcessorCtx
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
name|SemanticRule
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
name|RuleRegExp
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
name|optimizer
operator|.
name|Transform
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
comment|/**  * If two reducer sink operators share the same partition/sort columns and order,  * they can be merged. This should happen after map join optimization because map  * join optimization will remove reduce sink operators.  *  * This optimizer removes/replaces child-RS (not parent) which is safer way for DefaultGraphWalker.  */
end_comment

begin_class
specifier|public
class|class
name|ReduceSinkDeDuplication
extends|extends
name|Transform
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReduceSinkDeDuplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|RS
init|=
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GBY
init|=
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JOIN
init|=
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
specifier|protected
name|ParseContext
name|pGraphContext
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|pGraphContext
operator|=
name|pctx
expr_stmt|;
comment|// generate pruned column list for all relevant operators
name|ReduceSinkDeduplicateProcCtx
name|cppCtx
init|=
operator|new
name|ReduceSinkDeduplicateProcCtx
argument_list|(
name|pGraphContext
argument_list|)
decl_stmt|;
comment|// for auto convert map-joins, it not safe to dedup in here (todo)
name|boolean
name|mergeJoins
init|=
operator|!
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HIVECONVERTJOIN
argument_list|)
operator|&&
operator|!
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HIVECONVERTJOINNOCONDITIONALTASK
argument_list|)
operator|&&
operator|!
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONVERT_JOIN_BUCKET_MAPJOIN_TEZ
argument_list|)
operator|&&
operator|!
name|pctx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVEDYNAMICPARTITIONHASHJOIN
argument_list|)
decl_stmt|;
comment|// If multiple rules can be matched with same cost, last rule will be choosen as a processor
comment|// see DefaultRuleDispatcher#dispatch()
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|RS
operator|+
literal|"%.*%"
operator|+
name|RS
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ReduceSinkDeduplicateProcFactory
operator|.
name|getReducerReducerProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|RS
operator|+
literal|"%"
operator|+
name|GBY
operator|+
literal|"%.*%"
operator|+
name|RS
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ReduceSinkDeduplicateProcFactory
operator|.
name|getGroupbyReducerProc
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeJoins
condition|)
block|{
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R3"
argument_list|,
name|JOIN
operator|+
literal|"%.*%"
operator|+
name|RS
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ReduceSinkDeduplicateProcFactory
operator|.
name|getJoinReducerProc
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// TODO RS+JOIN
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|SemanticDispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|ReduceSinkDeduplicateProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|cppCtx
argument_list|)
decl_stmt|;
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
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
name|pGraphContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
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
name|pGraphContext
return|;
block|}
specifier|protected
class|class
name|ReduceSinkDeduplicateProcCtx
extends|extends
name|AbstractCorrelationProcCtx
block|{
specifier|public
name|ReduceSinkDeduplicateProcCtx
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|super
argument_list|(
name|pctx
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|ReduceSinkDeduplicateProcFactory
block|{
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getReducerReducerProc
parameter_list|()
block|{
return|return
operator|new
name|ReducerReducerProc
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getGroupbyReducerProc
parameter_list|()
block|{
return|return
operator|new
name|GroupbyReducerProc
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getJoinReducerProc
parameter_list|()
block|{
return|return
operator|new
name|JoinReducerProc
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultProc
argument_list|()
return|;
block|}
block|}
comment|/*    * do nothing.    */
specifier|static
class|class
name|DefaultProc
implements|implements
name|SemanticNodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|abstract
specifier|static
class|class
name|AbsctractReducerReducerProc
implements|implements
name|SemanticNodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
init|=
operator|(
name|ReduceSinkDeduplicateProcCtx
operator|)
name|procCtx
decl_stmt|;
if|if
condition|(
name|dedupCtx
operator|.
name|hasBeenRemoved
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
argument_list|>
operator|)
name|nd
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ReduceSinkOperator
name|cRS
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|child
init|=
name|CorrelationUtilities
operator|.
name|getSingleChild
argument_list|(
name|cRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|JoinOperator
condition|)
block|{
return|return
literal|false
return|;
comment|// not supported
block|}
if|if
condition|(
name|child
operator|instanceof
name|GroupByOperator
condition|)
block|{
name|GroupByOperator
name|cGBY
init|=
operator|(
name|GroupByOperator
operator|)
name|child
decl_stmt|;
if|if
condition|(
operator|!
name|CorrelationUtilities
operator|.
name|hasGroupingSet
argument_list|(
name|cRS
argument_list|)
operator|&&
operator|!
name|cGBY
operator|.
name|getConf
argument_list|()
operator|.
name|isGroupingSetsPresent
argument_list|()
condition|)
block|{
return|return
name|process
argument_list|(
name|cRS
argument_list|,
name|cGBY
argument_list|,
name|dedupCtx
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
if|if
condition|(
name|child
operator|instanceof
name|SelectOperator
condition|)
block|{
return|return
name|process
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|protected
specifier|abstract
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|GroupByOperator
name|cGBY
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
block|}
specifier|static
class|class
name|GroupbyReducerProc
extends|extends
name|AbsctractReducerReducerProc
block|{
comment|// given a group by operator this determines if that group by belongs to semi-join branch
comment|// note that this works only for second last group by in semi-join branch (X-GB-RS-GB-RS)
specifier|private
name|boolean
name|isSemiJoinBranch
parameter_list|(
specifier|final
name|GroupByOperator
name|gOp
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|gOp
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|gOp
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|gOp
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|dedupCtx
operator|.
name|getPctx
argument_list|()
operator|.
name|getRsToSemiJoinBranchInfo
argument_list|()
operator|.
name|containsKey
argument_list|(
name|rsOp
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|// pRS-pGBY-cRS
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GroupByOperator
name|pGBY
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|cRS
argument_list|,
name|GroupByOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pGBY
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|isSemiJoinBranch
argument_list|(
name|pGBY
argument_list|,
name|dedupCtx
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|pGBY
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
operator|&&
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
name|CorrelationUtilities
operator|.
name|replaceReduceSinkWithSelectOperator
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|// pRS-pGBY-cRS-cGBY
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|GroupByOperator
name|cGBY
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|start
init|=
name|CorrelationUtilities
operator|.
name|getStartForGroupBy
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
argument_list|)
decl_stmt|;
name|GroupByOperator
name|pGBY
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|start
argument_list|,
name|GroupByOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pGBY
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|isSemiJoinBranch
argument_list|(
name|cGBY
argument_list|,
name|dedupCtx
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|getSingleParent
argument_list|(
name|pGBY
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
operator|&&
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
name|CorrelationUtilities
operator|.
name|removeReduceSinkForGroupBy
argument_list|(
name|cRS
argument_list|,
name|cGBY
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|static
class|class
name|JoinReducerProc
extends|extends
name|AbsctractReducerReducerProc
block|{
comment|// pRS-pJOIN-cRS
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|JoinOperator
name|pJoin
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|cRS
argument_list|,
name|JoinOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pJoin
operator|!=
literal|null
operator|&&
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pJoin
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
name|pJoin
operator|.
name|getConf
argument_list|()
operator|.
name|setFixedAsSorted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|CorrelationUtilities
operator|.
name|replaceReduceSinkWithSelectOperator
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|pJoin
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
condition|)
block|{
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|// pRS-pJOIN-cRS-cGBY
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|GroupByOperator
name|cGBY
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|start
init|=
name|CorrelationUtilities
operator|.
name|getStartForGroupBy
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
argument_list|)
decl_stmt|;
name|JoinOperator
name|pJoin
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|start
argument_list|,
name|JoinOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pJoin
operator|!=
literal|null
operator|&&
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pJoin
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
name|pJoin
operator|.
name|getConf
argument_list|()
operator|.
name|setFixedAsSorted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|CorrelationUtilities
operator|.
name|removeReduceSinkForGroupBy
argument_list|(
name|cRS
argument_list|,
name|cGBY
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|pJoin
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
condition|)
block|{
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|static
class|class
name|ReducerReducerProc
extends|extends
name|AbsctractReducerReducerProc
block|{
comment|// pRS-cRS
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|cRS
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
condition|)
block|{
comment|// Try extended deduplication
if|if
condition|(
name|ReduceSinkDeDuplicationUtils
operator|.
name|aggressiveDedup
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|,
name|dedupCtx
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Normal deduplication
if|if
condition|(
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
name|CorrelationUtilities
operator|.
name|replaceReduceSinkWithSelectOperator
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|// pRS-cRS-cGBY
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|ReduceSinkOperator
name|cRS
parameter_list|,
name|GroupByOperator
name|cGBY
parameter_list|,
name|ReduceSinkDeduplicateProcCtx
name|dedupCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|start
init|=
name|CorrelationUtilities
operator|.
name|getStartForGroupBy
argument_list|(
name|cRS
argument_list|,
name|dedupCtx
argument_list|)
decl_stmt|;
name|ReduceSinkOperator
name|pRS
init|=
name|CorrelationUtilities
operator|.
name|findPossibleParent
argument_list|(
name|start
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
name|dedupCtx
operator|.
name|trustScript
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRS
operator|!=
literal|null
operator|&&
name|ReduceSinkDeDuplicationUtils
operator|.
name|merge
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|,
name|dedupCtx
operator|.
name|minReducer
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|dedupCtx
operator|.
name|getPctx
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
name|HIVEGROUPBYSKEW
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|CorrelationUtilities
operator|.
name|removeReduceSinkForGroupBy
argument_list|(
name|cRS
argument_list|,
name|cGBY
argument_list|,
name|dedupCtx
operator|.
name|getPctx
argument_list|()
argument_list|,
name|dedupCtx
argument_list|)
expr_stmt|;
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|setDeduplicated
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

