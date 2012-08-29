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
name|optimizer
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
name|HashMap
import|;
end_import

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
name|LinkedHashMap
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
name|ql
operator|.
name|exec
operator|.
name|ColumnInfo
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
name|ExtractOperator
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
name|FilterOperator
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
name|ForwardOperator
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
name|OperatorFactory
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
name|RowSchema
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
name|ScriptOperator
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
name|parse
operator|.
name|OpParseContext
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
name|RowResolver
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
name|ExprNodeColumnDesc
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
name|ExprNodeDesc
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
name|ReduceSinkDesc
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
name|SelectDesc
import|;
end_import

begin_comment
comment|/**  * If two reducer sink operators share the same partition/sort columns, we  * should merge them. This should happen after map join optimization because map  * join optimization will remove reduce sink operators.  */
end_comment

begin_class
specifier|public
class|class
name|ReduceSinkDeDuplication
implements|implements
name|Transform
block|{
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
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
literal|"RS%.*RS%"
argument_list|)
argument_list|,
name|ReduceSinkDeduplicateProcFactory
operator|.
name|getReducerReducerProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
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
name|GraphWalker
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
class|class
name|ReduceSinkDeduplicateProcCtx
implements|implements
name|NodeProcessorCtx
block|{
name|ParseContext
name|pctx
decl_stmt|;
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|rejectedRSList
decl_stmt|;
specifier|public
name|ReduceSinkDeduplicateProcCtx
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|rejectedRSList
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|ReduceSinkOperator
name|rsOp
parameter_list|)
block|{
return|return
name|rejectedRSList
operator|.
name|contains
argument_list|(
name|rsOp
argument_list|)
return|;
block|}
specifier|public
name|void
name|addRejectedReduceSinkOperator
parameter_list|(
name|ReduceSinkOperator
name|rsOp
parameter_list|)
block|{
if|if
condition|(
operator|!
name|rejectedRSList
operator|.
name|contains
argument_list|(
name|rsOp
argument_list|)
condition|)
block|{
name|rejectedRSList
operator|.
name|add
argument_list|(
name|rsOp
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ParseContext
name|getPctx
parameter_list|()
block|{
return|return
name|pctx
return|;
block|}
specifier|public
name|void
name|setPctx
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|ReduceSinkDeduplicateProcFactory
block|{
specifier|public
specifier|static
name|NodeProcessor
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
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultProc
argument_list|()
return|;
block|}
comment|/*      * do nothing.      */
specifier|static
class|class
name|DefaultProc
implements|implements
name|NodeProcessor
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
name|ReducerReducerProc
implements|implements
name|NodeProcessor
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
name|ctx
init|=
operator|(
name|ReduceSinkDeduplicateProcCtx
operator|)
name|procCtx
decl_stmt|;
name|ReduceSinkOperator
name|childReduceSink
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|contains
argument_list|(
name|childReduceSink
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOp
init|=
name|childReduceSink
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|childOp
operator|!=
literal|null
operator|&&
name|childOp
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|child
init|=
name|childOp
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|GroupByOperator
operator|||
name|child
operator|instanceof
name|JoinOperator
condition|)
block|{
name|ctx
operator|.
name|addRejectedReduceSinkOperator
argument_list|(
name|childReduceSink
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
name|ParseContext
name|pGraphContext
init|=
name|ctx
operator|.
name|getPctx
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|childColumnMapping
init|=
name|getPartitionAndKeyColumnMapping
argument_list|(
name|childReduceSink
argument_list|)
decl_stmt|;
name|ReduceSinkOperator
name|parentRS
init|=
literal|null
decl_stmt|;
name|parentRS
operator|=
name|findSingleParentReduceSink
argument_list|(
name|childReduceSink
argument_list|,
name|pGraphContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentRS
operator|==
literal|null
condition|)
block|{
name|ctx
operator|.
name|addRejectedReduceSinkOperator
argument_list|(
name|childReduceSink
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parentColumnMapping
init|=
name|getPartitionAndKeyColumnMapping
argument_list|(
name|parentRS
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|stopBacktrackFlagOp
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
operator|||
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|stopBacktrackFlagOp
operator|=
name|parentRS
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|stopBacktrackFlagOp
operator|=
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|boolean
name|succeed
init|=
name|backTrackColumnNames
argument_list|(
name|childColumnMapping
argument_list|,
name|childReduceSink
argument_list|,
name|stopBacktrackFlagOp
argument_list|,
name|pGraphContext
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|succeed
condition|)
block|{
return|return
literal|null
return|;
block|}
name|succeed
operator|=
name|backTrackColumnNames
argument_list|(
name|parentColumnMapping
argument_list|,
name|parentRS
argument_list|,
name|stopBacktrackFlagOp
argument_list|,
name|pGraphContext
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|succeed
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
name|same
init|=
name|compareReduceSink
argument_list|(
name|childReduceSink
argument_list|,
name|parentRS
argument_list|,
name|childColumnMapping
argument_list|,
name|parentColumnMapping
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|same
condition|)
block|{
return|return
literal|null
return|;
block|}
name|replaceReduceSinkWithSelectOperator
argument_list|(
name|childReduceSink
argument_list|,
name|pGraphContext
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|replaceReduceSinkWithSelectOperator
parameter_list|(
name|ReduceSinkOperator
name|childReduceSink
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parentOp
init|=
name|childReduceSink
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOp
init|=
name|childReduceSink
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|oldParent
init|=
name|childReduceSink
decl_stmt|;
if|if
condition|(
name|childOp
operator|!=
literal|null
operator|&&
name|childOp
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
operator|(
operator|(
name|childOp
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|instanceof
name|ExtractOperator
operator|)
condition|)
block|{
name|oldParent
operator|=
name|childOp
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|childOp
operator|=
name|childOp
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getChildOperators
argument_list|()
expr_stmt|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|input
init|=
name|parentOp
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|input
operator|.
name|getChildOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|RowResolver
name|inputRR
init|=
name|pGraphContext
operator|.
name|getOpParseCtx
argument_list|()
operator|.
name|get
argument_list|(
name|input
argument_list|)
operator|.
name|getRowResolver
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|outputCols
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputValueColumnNames
argument_list|()
decl_stmt|;
name|RowResolver
name|outputRS
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|outputCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|internalName
init|=
name|outputCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
index|[]
name|nm
init|=
name|inputRR
operator|.
name|reverseLookup
argument_list|(
name|internalName
argument_list|)
decl_stmt|;
name|ColumnInfo
name|valueInfo
init|=
name|inputRR
operator|.
name|get
argument_list|(
name|nm
index|[
literal|0
index|]
argument_list|,
name|nm
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|colDesc
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getValueCols
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|exprs
operator|.
name|add
argument_list|(
name|colDesc
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|add
argument_list|(
name|internalName
argument_list|)
expr_stmt|;
name|outputRS
operator|.
name|put
argument_list|(
name|nm
index|[
literal|0
index|]
argument_list|,
name|nm
index|[
literal|1
index|]
argument_list|,
operator|new
name|ColumnInfo
argument_list|(
name|internalName
argument_list|,
name|valueInfo
operator|.
name|getType
argument_list|()
argument_list|,
name|nm
index|[
literal|0
index|]
argument_list|,
name|valueInfo
operator|.
name|getIsVirtualCol
argument_list|()
argument_list|,
name|valueInfo
operator|.
name|isHiddenVirtualCol
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|colExprMap
operator|.
name|put
argument_list|(
name|internalName
argument_list|,
name|colDesc
argument_list|)
expr_stmt|;
block|}
name|SelectDesc
name|select
init|=
operator|new
name|SelectDesc
argument_list|(
name|exprs
argument_list|,
name|outputs
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|SelectOperator
name|sel
init|=
operator|(
name|SelectOperator
operator|)
name|putOpInsertMap
argument_list|(
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|select
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|inputRR
operator|.
name|getColumnInfos
argument_list|()
argument_list|)
argument_list|,
name|input
argument_list|)
argument_list|,
name|inputRR
argument_list|,
name|pGraphContext
argument_list|)
decl_stmt|;
name|sel
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
comment|// Insert the select operator in between.
name|sel
operator|.
name|setChildOperators
argument_list|(
name|childOp
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|ch
range|:
name|childOp
control|)
block|{
name|ch
operator|.
name|replaceParent
argument_list|(
name|oldParent
argument_list|,
name|sel
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|putOpInsertMap
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|RowResolver
name|rr
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|OpParseContext
name|ctx
init|=
operator|new
name|OpParseContext
argument_list|(
name|rr
argument_list|)
decl_stmt|;
name|pGraphContext
operator|.
name|getOpParseCtx
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|op
return|;
block|}
specifier|private
name|boolean
name|compareReduceSink
parameter_list|(
name|ReduceSinkOperator
name|childReduceSink
parameter_list|,
name|ReduceSinkOperator
name|parentRS
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|childColumnMapping
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parentColumnMapping
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|childPartitionCols
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getPartitionCols
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parentPartitionCols
init|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getPartitionCols
argument_list|()
decl_stmt|;
name|boolean
name|ret
init|=
name|compareExprNodes
argument_list|(
name|childColumnMapping
argument_list|,
name|parentColumnMapping
argument_list|,
name|childPartitionCols
argument_list|,
name|parentPartitionCols
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ret
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|childReduceKeyCols
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parentReduceKeyCols
init|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
name|ret
operator|=
name|compareExprNodes
argument_list|(
name|childColumnMapping
argument_list|,
name|parentColumnMapping
argument_list|,
name|childReduceKeyCols
argument_list|,
name|parentReduceKeyCols
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|ret
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|childRSOrder
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getOrder
argument_list|()
decl_stmt|;
name|String
name|parentRSOrder
init|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getOrder
argument_list|()
decl_stmt|;
name|boolean
name|moveChildRSOrderToParent
init|=
literal|false
decl_stmt|;
comment|//move child reduce sink's order to the parent reduce sink operator.
if|if
condition|(
name|childRSOrder
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|childRSOrder
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|parentRSOrder
operator|==
literal|null
operator|||
operator|!
name|childRSOrder
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
name|parentRSOrder
operator|.
name|trim
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|parentRSOrder
operator|==
literal|null
operator|||
name|parentRSOrder
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|moveChildRSOrderToParent
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|int
name|childNumReducers
init|=
name|childReduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
decl_stmt|;
name|int
name|parentNumReducers
init|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
decl_stmt|;
name|boolean
name|moveChildReducerNumToParent
init|=
literal|false
decl_stmt|;
comment|//move child reduce sink's number reducers to the parent reduce sink operator.
if|if
condition|(
name|childNumReducers
operator|!=
name|parentNumReducers
condition|)
block|{
if|if
condition|(
name|childNumReducers
operator|==
operator|-
literal|1
condition|)
block|{
comment|//do nothing.
block|}
elseif|else
if|if
condition|(
name|parentNumReducers
operator|==
operator|-
literal|1
condition|)
block|{
comment|//set childNumReducers in the parent reduce sink operator.
name|moveChildReducerNumToParent
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|moveChildRSOrderToParent
condition|)
block|{
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|setOrder
argument_list|(
name|childRSOrder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|moveChildReducerNumToParent
condition|)
block|{
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|setNumReducers
argument_list|(
name|childNumReducers
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|compareExprNodes
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|childColumnMapping
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parentColumnMapping
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|childColExprs
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parentColExprs
parameter_list|)
block|{
name|boolean
name|childEmpty
init|=
name|childColExprs
operator|==
literal|null
operator|||
name|childColExprs
operator|.
name|size
argument_list|()
operator|==
literal|0
decl_stmt|;
name|boolean
name|parentEmpty
init|=
name|parentColExprs
operator|==
literal|null
operator|||
name|parentColExprs
operator|.
name|size
argument_list|()
operator|==
literal|0
decl_stmt|;
if|if
condition|(
name|childEmpty
condition|)
block|{
comment|//both empty
return|return
literal|true
return|;
block|}
comment|//child not empty here
if|if
condition|(
name|parentEmpty
condition|)
block|{
comment|// child not empty, but parent empty
return|return
literal|false
return|;
block|}
if|if
condition|(
name|childColExprs
operator|.
name|size
argument_list|()
operator|!=
name|parentColExprs
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|childColExprs
operator|.
name|size
argument_list|()
condition|)
block|{
name|ExprNodeDesc
name|childExpr
init|=
name|childColExprs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|parentExpr
init|=
name|parentColExprs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|childExpr
operator|instanceof
name|ExprNodeColumnDesc
operator|)
operator|&&
operator|(
name|parentExpr
operator|instanceof
name|ExprNodeColumnDesc
operator|)
condition|)
block|{
name|String
name|childCol
init|=
name|childColumnMapping
operator|.
name|get
argument_list|(
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|childExpr
operator|)
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|parentCol
init|=
name|parentColumnMapping
operator|.
name|get
argument_list|(
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|childExpr
operator|)
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|childCol
operator|.
name|equals
argument_list|(
name|parentCol
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
name|i
operator|++
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/*        * back track column names to find their corresponding original column        * names. Only allow simple operators like 'select column' or filter.        */
specifier|private
name|boolean
name|backTrackColumnNames
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|columnMapping
parameter_list|,
name|ReduceSinkOperator
name|reduceSink
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|stopBacktrackFlagOp
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|startOperator
init|=
name|reduceSink
decl_stmt|;
while|while
condition|(
name|startOperator
operator|!=
literal|null
operator|&&
name|startOperator
operator|!=
name|stopBacktrackFlagOp
condition|)
block|{
name|startOperator
operator|=
name|startOperator
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
init|=
name|startOperator
operator|.
name|getColumnExprMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|colExprMap
operator|==
literal|null
operator|||
name|colExprMap
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|Iterator
argument_list|<
name|String
argument_list|>
name|keyIter
init|=
name|columnMapping
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|keyIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|key
init|=
name|keyIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|oldCol
init|=
name|columnMapping
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|exprNode
init|=
name|colExprMap
operator|.
name|get
argument_list|(
name|oldCol
argument_list|)
decl_stmt|;
if|if
condition|(
name|exprNode
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|String
name|col
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|exprNode
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|columnMapping
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionAndKeyColumnMapping
parameter_list|(
name|ReduceSinkOperator
name|reduceSink
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|columnMapping
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ReduceSinkDesc
name|reduceSinkDesc
init|=
name|reduceSink
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
init|=
name|reduceSinkDesc
operator|.
name|getPartitionCols
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|reduceKeyCols
init|=
name|reduceSinkDesc
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionCols
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|desc
range|:
name|partitionCols
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|desc
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|cols
control|)
block|{
name|columnMapping
operator|.
name|put
argument_list|(
name|col
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|reduceKeyCols
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|desc
range|:
name|reduceKeyCols
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|desc
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|cols
control|)
block|{
name|columnMapping
operator|.
name|put
argument_list|(
name|col
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|columnMapping
return|;
block|}
specifier|private
name|ReduceSinkOperator
name|findSingleParentReduceSink
parameter_list|(
name|ReduceSinkOperator
name|childReduceSink
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|start
init|=
name|childReduceSink
decl_stmt|;
while|while
condition|(
name|start
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|start
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
operator|||
name|start
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
comment|// this potentially is a join operator
return|return
literal|null
return|;
block|}
name|boolean
name|allowed
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|(
name|start
operator|instanceof
name|SelectOperator
operator|)
operator|||
operator|(
name|start
operator|instanceof
name|FilterOperator
operator|)
operator|||
operator|(
name|start
operator|instanceof
name|ExtractOperator
operator|)
operator|||
operator|(
name|start
operator|instanceof
name|ForwardOperator
operator|)
operator|||
operator|(
name|start
operator|instanceof
name|ScriptOperator
operator|)
operator|||
operator|(
name|start
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|allowed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|allowed
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|(
name|start
operator|instanceof
name|ScriptOperator
operator|)
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|pGraphContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESCRIPTOPERATORTRUST
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|start
operator|=
name|start
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|start
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
return|return
operator|(
name|ReduceSinkOperator
operator|)
name|start
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

