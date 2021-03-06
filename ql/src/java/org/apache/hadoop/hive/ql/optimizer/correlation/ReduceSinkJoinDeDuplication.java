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
name|EnumSet
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
name|ql
operator|.
name|exec
operator|.
name|CommonMergeJoinOperator
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
name|MapJoinOperator
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
name|ForwardWalker
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
name|ReduceSinkDesc
operator|.
name|ReducerTraits
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
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * Optimization to check whether any ReduceSink operator in the plan can be  * simplified so data is not shuffled/sorted if it is already shuffled/sorted.  *  * This optimization is executed after join algorithm selection logic has run,  * and it is intended to optimize new cases that cannot be optimized when  * {@link ReduceSinkDeDuplication} runs because some physical algorithms have  * not been selected. Instead of removing ReduceSink operators from the plan,  * they will be tagged, and then the execution plan compiler might take action,  * e.g., on Tez, ReduceSink operators that just need to forward data will be  * translated into a ONE-TO-ONE edge. The parallelism degree of these ReduceSink  * operators might be adjusted, as a ReduceSink operator that just forwards data  * cannot alter the degree of parallelism of the previous task.  */
end_comment

begin_class
specifier|public
class|class
name|ReduceSinkJoinDeDuplication
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
name|ReduceSinkJoinDeDuplication
operator|.
name|class
argument_list|)
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
name|ReduceSinkJoinDeDuplicateProcCtx
name|cppCtx
init|=
operator|new
name|ReduceSinkJoinDeDuplicateProcCtx
argument_list|(
name|pGraphContext
argument_list|)
decl_stmt|;
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
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|ReduceSinkJoinDeDuplicateProcFactory
operator|.
name|getReducerMapJoinProc
argument_list|()
argument_list|)
expr_stmt|;
name|SemanticDispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|ReduceSinkJoinDeDuplicateProcFactory
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
name|ForwardWalker
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
name|ReduceSinkJoinDeDuplicateProcCtx
extends|extends
name|AbstractCorrelationProcCtx
block|{
specifier|public
name|ReduceSinkJoinDeDuplicateProcCtx
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
name|ReduceSinkJoinDeDuplicateProcFactory
block|{
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getReducerMapJoinProc
parameter_list|()
block|{
return|return
operator|new
name|ReducerProc
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
specifier|static
class|class
name|ReducerProc
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
name|ReduceSinkJoinDeDuplicateProcCtx
name|dedupCtx
init|=
operator|(
name|ReduceSinkJoinDeDuplicateProcCtx
operator|)
name|procCtx
decl_stmt|;
name|ReduceSinkOperator
name|cRS
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|isForwarding
argument_list|()
condition|)
block|{
comment|// Already set
return|return
literal|false
return|;
block|}
if|if
condition|(
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|isOrdering
argument_list|()
condition|)
block|{
comment|// 1-1 edge is skipped for sorted shuffle
return|return
literal|false
return|;
block|}
if|if
condition|(
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Not supported
return|return
literal|false
return|;
block|}
name|boolean
name|onlyPartitioning
init|=
literal|false
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|cRSChild
init|=
name|cRS
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|cRSChild
operator|instanceof
name|MapJoinOperator
operator|||
name|cRSChild
operator|instanceof
name|CommonMergeJoinOperator
condition|)
block|{
comment|// If it is a MapJoin or MergeJoin, we make sure that they are on
comment|// the reduce side, otherwise we bail out
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|cRSChild
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parent
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|// MapJoin and SMBJoin not supported
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|cRSChild
operator|instanceof
name|MapJoinOperator
condition|)
block|{
name|onlyPartitioning
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|int
name|maxNumReducers
init|=
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
decl_stmt|;
name|ReduceSinkOperator
name|pRS
decl_stmt|;
if|if
condition|(
name|onlyPartitioning
condition|)
block|{
name|pRS
operator|=
name|CorrelationUtilities
operator|.
name|findFirstPossibleParent
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
expr_stmt|;
block|}
else|else
block|{
name|pRS
operator|=
name|CorrelationUtilities
operator|.
name|findFirstPossibleParentPreserveSortOrder
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
expr_stmt|;
block|}
if|if
condition|(
name|pRS
operator|!=
literal|null
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|pRSChild
init|=
name|pRS
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|pRSChild
operator|instanceof
name|MapJoinOperator
condition|)
block|{
comment|// Handle MapJoin specially and check for all its children
name|MapJoinOperator
name|pRSChildMJ
init|=
operator|(
name|MapJoinOperator
operator|)
name|pRSChild
decl_stmt|;
comment|// In this case, both should be DHJ operators as pRSChildMJ can only guarantee
comment|// partitioned input, not sorted.
if|if
condition|(
operator|!
name|pRSChildMJ
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
operator|||
operator|!
operator|(
name|cRSChild
operator|instanceof
name|MapJoinOperator
operator|)
operator|||
operator|!
operator|(
operator|(
name|MapJoinOperator
operator|)
name|cRSChild
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|l
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|pRSChild
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parent
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|rsOp
argument_list|)
expr_stmt|;
if|if
condition|(
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
operator|>
name|maxNumReducers
condition|)
block|{
name|maxNumReducers
operator|=
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ReduceSinkDeDuplicationUtils
operator|.
name|strictMerge
argument_list|(
name|cRS
argument_list|,
name|l
operator|.
name|build
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set {} to forward data"
argument_list|,
name|cRS
argument_list|)
expr_stmt|;
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|setForwarding
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|propagateMaxNumReducers
argument_list|(
name|dedupCtx
argument_list|,
name|cRS
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|pRS
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|CommonMergeJoinOperator
condition|)
block|{
comment|// Handle MergeJoin specially and check for all its children
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|l
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|pRSChild
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parent
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|// SMBJoin not supported
return|return
literal|false
return|;
block|}
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parent
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|rsOp
argument_list|)
expr_stmt|;
if|if
condition|(
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
operator|>
name|maxNumReducers
condition|)
block|{
name|maxNumReducers
operator|=
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ReduceSinkDeDuplicationUtils
operator|.
name|strictMerge
argument_list|(
name|cRS
argument_list|,
name|l
operator|.
name|build
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set {} to forward data"
argument_list|,
name|cRS
argument_list|)
expr_stmt|;
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|setForwarding
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|propagateMaxNumReducers
argument_list|(
name|dedupCtx
argument_list|,
name|cRS
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
comment|// Rest of cases
if|if
condition|(
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
operator|>
name|maxNumReducers
condition|)
block|{
name|maxNumReducers
operator|=
name|pRS
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ReduceSinkDeDuplicationUtils
operator|.
name|strictMerge
argument_list|(
name|cRS
argument_list|,
name|pRS
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set {} to forward data"
argument_list|,
name|cRS
argument_list|)
expr_stmt|;
name|cRS
operator|.
name|getConf
argument_list|()
operator|.
name|setForwarding
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|propagateMaxNumReducers
argument_list|(
name|dedupCtx
argument_list|,
name|cRS
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
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
specifier|private
specifier|static
name|void
name|propagateMaxNumReducers
parameter_list|(
name|ReduceSinkJoinDeDuplicateProcCtx
name|dedupCtx
parameter_list|,
name|ReduceSinkOperator
name|rsOp
parameter_list|,
name|int
name|maxNumReducers
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|rsOp
operator|==
literal|null
condition|)
block|{
comment|// Bail out
return|return;
block|}
if|if
condition|(
name|rsOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|MapJoinOperator
operator|||
name|rsOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|CommonMergeJoinOperator
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|p
range|:
name|rsOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|ReduceSinkOperator
name|pRSOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|p
decl_stmt|;
name|pRSOp
operator|.
name|getConf
argument_list|()
operator|.
name|setReducerTraits
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|ReducerTraits
operator|.
name|FIXED
argument_list|)
argument_list|)
expr_stmt|;
name|pRSOp
operator|.
name|getConf
argument_list|()
operator|.
name|setNumReducers
argument_list|(
name|maxNumReducers
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set {} to FIXED parallelism: {}"
argument_list|,
name|pRSOp
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
if|if
condition|(
name|pRSOp
operator|.
name|getConf
argument_list|()
operator|.
name|isForwarding
argument_list|()
condition|)
block|{
name|ReduceSinkOperator
name|newRSOp
init|=
name|CorrelationUtilities
operator|.
name|findFirstPossibleParent
argument_list|(
name|pRSOp
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
name|propagateMaxNumReducers
argument_list|(
name|dedupCtx
argument_list|,
name|newRSOp
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setReducerTraits
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|ReducerTraits
operator|.
name|FIXED
argument_list|)
argument_list|)
expr_stmt|;
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setNumReducers
argument_list|(
name|maxNumReducers
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set {} to FIXED parallelism: {}"
argument_list|,
name|rsOp
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
if|if
condition|(
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|isForwarding
argument_list|()
condition|)
block|{
name|ReduceSinkOperator
name|newRSOp
init|=
name|CorrelationUtilities
operator|.
name|findFirstPossibleParent
argument_list|(
name|rsOp
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
name|propagateMaxNumReducers
argument_list|(
name|dedupCtx
argument_list|,
name|newRSOp
argument_list|,
name|maxNumReducers
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

