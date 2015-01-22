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
name|Collection
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
name|ExprNodeDescUtils
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
name|ExprNodeGenericFuncDesc
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

begin_comment
comment|/**  * merges SEL-SEL or FIL-FIL into single operator  */
end_comment

begin_class
specifier|public
class|class
name|NonBlockingOpDeDupProc
implements|implements
name|Transform
block|{
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
name|String
name|SEL
init|=
name|SelectOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
name|String
name|FIL
init|=
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
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
name|SEL
operator|+
literal|"%"
operator|+
name|SEL
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|SelectDedup
argument_list|(
name|pctx
argument_list|)
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
name|FIL
operator|+
literal|"%"
operator|+
name|FIL
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|FilterDedup
argument_list|()
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
literal|null
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
name|List
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
name|pctx
return|;
block|}
specifier|private
class|class
name|SelectDedup
implements|implements
name|NodeProcessor
block|{
specifier|private
name|ParseContext
name|pctx
decl_stmt|;
specifier|public
name|SelectDedup
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
name|SelectOperator
name|cSEL
init|=
operator|(
name|SelectOperator
operator|)
name|nd
decl_stmt|;
name|SelectOperator
name|pSEL
init|=
operator|(
name|SelectOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|pSEL
operator|.
name|getNumChild
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
literal|null
return|;
comment|// possible if all children have same expressions, but not likely.
block|}
if|if
condition|(
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
comment|// SEL(no-compute)-SEL. never seen this condition
comment|// and also, removing parent is not safe in current graph walker
return|return
literal|null
return|;
block|}
comment|// For SEL-SEL(compute) case, move column exprs/names of child to parent.
if|if
condition|(
operator|!
name|cSEL
operator|.
name|getConf
argument_list|()
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|funcOutputs
init|=
name|getFunctionOutputs
argument_list|(
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|,
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|getColList
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|cSELColList
init|=
name|cSEL
operator|.
name|getConf
argument_list|()
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cSELOutputColumnNames
init|=
name|cSEL
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputColumnNames
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|funcOutputs
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|checkReferences
argument_list|(
name|cSELColList
argument_list|,
name|funcOutputs
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|cSEL
operator|.
name|getColumnExprMap
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// If the child SelectOperator does not have the ColumnExprMap,
comment|// we do not need to update the ColumnExprMap in the parent SelectOperator.
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|setColList
argument_list|(
name|ExprNodeDescUtils
operator|.
name|backtrack
argument_list|(
name|cSELColList
argument_list|,
name|cSEL
argument_list|,
name|pSEL
argument_list|)
argument_list|)
expr_stmt|;
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputColumnNames
argument_list|(
name|cSELOutputColumnNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// If the child SelectOperator has the ColumnExprMap,
comment|// we need to update the ColumnExprMap in the parent SelectOperator.
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|newPSELColList
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|newPSELOutputColumnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|cSELOutputColumnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|outputColumnName
init|=
name|cSELOutputColumnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|cSELExprNodeDesc
init|=
name|cSELColList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|newPSELExprNodeDesc
init|=
name|ExprNodeDescUtils
operator|.
name|backtrack
argument_list|(
name|cSELExprNodeDesc
argument_list|,
name|cSEL
argument_list|,
name|pSEL
argument_list|)
decl_stmt|;
name|newPSELColList
operator|.
name|add
argument_list|(
name|newPSELExprNodeDesc
argument_list|)
expr_stmt|;
name|newPSELOutputColumnNames
operator|.
name|add
argument_list|(
name|outputColumnName
argument_list|)
expr_stmt|;
name|colExprMap
operator|.
name|put
argument_list|(
name|outputColumnName
argument_list|,
name|newPSELExprNodeDesc
argument_list|)
expr_stmt|;
block|}
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|setColList
argument_list|(
name|newPSELColList
argument_list|)
expr_stmt|;
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputColumnNames
argument_list|(
name|newPSELOutputColumnNames
argument_list|)
expr_stmt|;
name|pSEL
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
block|}
name|pSEL
operator|.
name|setSchema
argument_list|(
name|cSEL
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|pSEL
operator|.
name|getConf
argument_list|()
operator|.
name|setSelectStar
argument_list|(
name|cSEL
operator|.
name|getConf
argument_list|()
operator|.
name|isSelectStar
argument_list|()
argument_list|)
expr_stmt|;
comment|// We need to use the OpParseContext of the child SelectOperator to replace the
comment|// the OpParseContext of the parent SelectOperator.
name|pctx
operator|.
name|updateOpParseCtx
argument_list|(
name|pSEL
argument_list|,
name|pctx
operator|.
name|removeOpParseCtx
argument_list|(
name|cSEL
argument_list|)
argument_list|)
expr_stmt|;
name|pSEL
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|cSEL
argument_list|)
expr_stmt|;
name|cSEL
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|cSEL
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fixContextReferences
argument_list|(
name|cSEL
argument_list|,
name|pSEL
argument_list|)
expr_stmt|;
name|cSEL
operator|=
literal|null
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// collect name of output columns which is result of function
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getFunctionOutputs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|targets
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|functionOutputs
init|=
operator|new
name|HashSet
argument_list|<
name|String
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
name|targets
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
name|targets
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|functionOutputs
operator|.
name|add
argument_list|(
name|colNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|functionOutputs
return|;
block|}
comment|// if any expression of child is referencing parent column which is result of function
comment|// twice or more, skip dedup.
specifier|private
name|boolean
name|checkReferences
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sources
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|funcOutputs
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|ref
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|source
range|:
name|sources
control|)
block|{
if|if
condition|(
operator|!
name|checkReferences
argument_list|(
name|source
argument_list|,
name|funcOutputs
argument_list|,
name|ref
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|checkReferences
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|funcOutputs
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|ref
parameter_list|)
block|{
if|if
condition|(
name|expr
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
name|expr
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
if|if
condition|(
name|funcOutputs
operator|.
name|contains
argument_list|(
name|col
argument_list|)
operator|&&
operator|!
name|ref
operator|.
name|add
argument_list|(
name|col
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|expr
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|child
range|:
name|expr
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|checkReferences
argument_list|(
name|child
argument_list|,
name|funcOutputs
argument_list|,
name|ref
argument_list|)
condition|)
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
comment|/**      * Change existing references in the context to point from child to parent operator.      * @param cSEL child operator (to be removed, and merged into parent)      * @param pSEL parent operator      */
specifier|private
name|void
name|fixContextReferences
parameter_list|(
name|SelectOperator
name|cSEL
parameter_list|,
name|SelectOperator
name|pSEL
parameter_list|)
block|{
name|Collection
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|mapsAliasToOpInfo
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|JoinOperator
name|joinOp
range|:
name|pctx
operator|.
name|getJoinOps
argument_list|()
control|)
block|{
if|if
condition|(
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasToOpInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|mapsAliasToOpInfo
operator|.
name|add
argument_list|(
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasToOpInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|MapJoinOperator
name|mapJoinOp
range|:
name|pctx
operator|.
name|getMapJoinOps
argument_list|()
control|)
block|{
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasToOpInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|mapsAliasToOpInfo
operator|.
name|add
argument_list|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasToOpInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToOpInfo
range|:
name|mapsAliasToOpInfo
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|entry
range|:
name|aliasToOpInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|cSEL
condition|)
block|{
name|aliasToOpInfo
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|pSEL
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
class|class
name|FilterDedup
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
name|FilterOperator
name|cFIL
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|FilterOperator
name|pFIL
init|=
operator|(
name|FilterOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
comment|// Sampling predicates can be merged with predicates from children because PPD/PPR is
comment|// already applied. But to clarify the intention of sampling, just skips merging.
if|if
condition|(
name|pFIL
operator|.
name|getConf
argument_list|()
operator|.
name|getIsSamplingPred
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeDescUtils
operator|.
name|split
argument_list|(
name|cFIL
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
argument_list|,
name|splits
argument_list|)
expr_stmt|;
name|ExprNodeDescUtils
operator|.
name|split
argument_list|(
name|pFIL
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
argument_list|,
name|splits
argument_list|)
expr_stmt|;
name|pFIL
operator|.
name|getConf
argument_list|()
operator|.
name|setPredicate
argument_list|(
name|ExprNodeDescUtils
operator|.
name|mergePredicates
argument_list|(
name|splits
argument_list|)
argument_list|)
expr_stmt|;
comment|// if any of filter is sorted filter, it's sorted filter
name|boolean
name|sortedFilter
init|=
name|pFIL
operator|.
name|getConf
argument_list|()
operator|.
name|isSortedFilter
argument_list|()
operator|||
name|cFIL
operator|.
name|getConf
argument_list|()
operator|.
name|isSortedFilter
argument_list|()
decl_stmt|;
name|pFIL
operator|.
name|getConf
argument_list|()
operator|.
name|setSortedFilter
argument_list|(
name|sortedFilter
argument_list|)
expr_stmt|;
name|pFIL
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|cFIL
argument_list|)
expr_stmt|;
name|cFIL
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|cFIL
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|cFIL
operator|=
literal|null
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

