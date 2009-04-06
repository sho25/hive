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
name|ppd
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
name|exprNodeColumnDesc
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
name|exprNodeDesc
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
name|exprNodeFieldDesc
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
name|exprNodeFuncDesc
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
name|exprNodeIndexDesc
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
name|udf
operator|.
name|UDFOPAnd
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
name|udf
operator|.
name|UDFType
import|;
end_import

begin_comment
comment|/**  * Expression factory for predicate pushdown processing.   * Each processor determines whether the expression is a possible candidate  * for predicate pushdown optimization for the given operator  */
end_comment

begin_class
specifier|public
class|class
name|ExprWalkerProcFactory
block|{
specifier|public
specifier|static
class|class
name|ColumnExprProcessor
implements|implements
name|NodeProcessor
block|{
comment|/**      * Converts the reference from child row resolver to current row resolver      */
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
name|ExprWalkerInfo
name|ctx
init|=
operator|(
name|ExprWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|exprNodeColumnDesc
name|colref
init|=
operator|(
name|exprNodeColumnDesc
operator|)
name|nd
decl_stmt|;
name|RowResolver
name|toRR
init|=
name|ctx
operator|.
name|getToRR
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
name|ctx
operator|.
name|getOp
argument_list|()
decl_stmt|;
name|String
index|[]
name|colAlias
init|=
name|toRR
operator|.
name|reverseLookup
argument_list|(
name|colref
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// replace the output expression with the input expression so that
comment|// parent op can understand this expression
name|exprNodeDesc
name|exp
init|=
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|get
argument_list|(
name|colref
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|exp
operator|==
literal|null
condition|)
block|{
comment|// means that expression can't be pushed either because it is value in group by
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|colref
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|ctx
operator|.
name|addConvertedNode
argument_list|(
name|colref
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|exp
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addAlias
argument_list|(
name|exp
argument_list|,
name|colAlias
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|colAlias
operator|==
literal|null
condition|)
assert|assert
literal|false
assert|;
name|ctx
operator|.
name|addAlias
argument_list|(
name|colref
argument_list|,
name|colAlias
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|colref
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * If all children are candidates and refer only to one table alias then this expr is a candidate    * else it is not a candidate but its children could be final candidates    */
specifier|public
specifier|static
class|class
name|FuncExprProcessor
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
name|ExprWalkerInfo
name|ctx
init|=
operator|(
name|ExprWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|String
name|alias
init|=
literal|null
decl_stmt|;
name|exprNodeFuncDesc
name|expr
init|=
operator|(
name|exprNodeFuncDesc
operator|)
name|nd
decl_stmt|;
name|UDFType
name|note
init|=
name|expr
operator|.
name|getUDFClass
argument_list|()
operator|.
name|getAnnotation
argument_list|(
name|UDFType
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|note
operator|!=
literal|null
operator|&&
operator|!
name|note
operator|.
name|deterministic
argument_list|()
condition|)
block|{
comment|// this UDF can't be pushed down
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|boolean
name|isCandidate
init|=
literal|true
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
name|nd
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
name|exprNodeDesc
name|ch
init|=
operator|(
name|exprNodeDesc
operator|)
name|nd
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|newCh
init|=
name|ctx
operator|.
name|getConvertedNode
argument_list|(
name|ch
argument_list|)
decl_stmt|;
if|if
condition|(
name|newCh
operator|!=
literal|null
condition|)
block|{
name|expr
operator|.
name|getChildExprs
argument_list|()
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|newCh
argument_list|)
expr_stmt|;
name|ch
operator|=
name|newCh
expr_stmt|;
block|}
name|String
name|chAlias
init|=
name|ctx
operator|.
name|getAlias
argument_list|(
name|ch
argument_list|)
decl_stmt|;
name|isCandidate
operator|=
name|isCandidate
operator|&&
name|ctx
operator|.
name|isCandidate
argument_list|(
name|ch
argument_list|)
expr_stmt|;
comment|// need to iterate through all children even if one is found to be not a candidate
comment|// in case if the other children could be individually pushed up
if|if
condition|(
name|isCandidate
operator|&&
name|chAlias
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|alias
operator|==
literal|null
condition|)
block|{
name|alias
operator|=
name|chAlias
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|chAlias
operator|.
name|equalsIgnoreCase
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|isCandidate
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|isCandidate
condition|)
break|break;
block|}
name|ctx
operator|.
name|addAlias
argument_list|(
name|expr
argument_list|,
name|alias
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
name|isCandidate
argument_list|)
expr_stmt|;
return|return
name|isCandidate
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|IndexExprProcessor
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
name|ExprWalkerInfo
name|ctx
init|=
operator|(
name|ExprWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|exprNodeIndexDesc
name|expr
init|=
operator|(
name|exprNodeIndexDesc
operator|)
name|nd
decl_stmt|;
comment|// process the base array expr(or map)
name|exprNodeDesc
name|desc
init|=
name|expr
operator|.
name|getDesc
argument_list|()
decl_stmt|;
name|exprNodeDesc
name|index
init|=
name|expr
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|exprNodeDesc
name|newDesc
init|=
name|ctx
operator|.
name|getConvertedNode
argument_list|(
name|desc
argument_list|)
decl_stmt|;
if|if
condition|(
name|newDesc
operator|!=
literal|null
condition|)
block|{
name|expr
operator|.
name|setDesc
argument_list|(
name|newDesc
argument_list|)
expr_stmt|;
name|desc
operator|=
name|newDesc
expr_stmt|;
block|}
name|exprNodeDesc
name|newIndex
init|=
name|ctx
operator|.
name|getConvertedNode
argument_list|(
name|desc
argument_list|)
decl_stmt|;
if|if
condition|(
name|newIndex
operator|!=
literal|null
condition|)
block|{
name|expr
operator|.
name|setIndex
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
name|index
operator|=
name|newIndex
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ctx
operator|.
name|isCandidate
argument_list|(
name|desc
argument_list|)
operator|||
operator|!
name|ctx
operator|.
name|isCandidate
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|descAlias
init|=
name|ctx
operator|.
name|getAlias
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|String
name|indexAlias
init|=
name|ctx
operator|.
name|getAlias
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|descAlias
operator|!=
literal|null
operator|&&
name|indexAlias
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|descAlias
operator|.
name|equals
argument_list|(
name|indexAlias
argument_list|)
operator|)
condition|)
block|{
comment|// aliases don't match
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|alias
init|=
name|descAlias
operator|!=
literal|null
condition|?
name|descAlias
else|:
name|indexAlias
decl_stmt|;
name|ctx
operator|.
name|addAlias
argument_list|(
name|expr
argument_list|,
name|alias
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * For constants and null expressions    */
specifier|public
specifier|static
class|class
name|DefaultExprProcessor
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
name|ExprWalkerInfo
name|ctx
init|=
operator|(
name|ExprWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|ctx
operator|.
name|setIsCandidate
argument_list|(
operator|(
name|exprNodeDesc
operator|)
name|nd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultExprProcessor
parameter_list|()
block|{
return|return
operator|new
name|DefaultExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFuncProcessor
parameter_list|()
block|{
return|return
operator|new
name|FuncExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getIndexProcessor
parameter_list|()
block|{
return|return
operator|new
name|IndexExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getColumnProcessor
parameter_list|()
block|{
return|return
operator|new
name|ColumnExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ExprWalkerInfo
name|extractPushdownPreds
parameter_list|(
name|OpWalkerInfo
name|opContext
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|,
name|exprNodeFuncDesc
name|pred
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
name|preds
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|()
decl_stmt|;
name|preds
operator|.
name|add
argument_list|(
name|pred
argument_list|)
expr_stmt|;
return|return
name|extractPushdownPreds
argument_list|(
name|opContext
argument_list|,
name|op
argument_list|,
name|preds
argument_list|)
return|;
block|}
comment|/**    * Extracts pushdown predicates from the given list of predicate expression    * @param opContext operator context used for resolving column references    * @param op operator of the predicates being processed    * @param preds    * @return    * @throws SemanticException    */
specifier|public
specifier|static
name|ExprWalkerInfo
name|extractPushdownPreds
parameter_list|(
name|OpWalkerInfo
name|opContext
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
name|preds
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the walker, the rules dispatcher and the context.
name|ExprWalkerInfo
name|exprContext
init|=
operator|new
name|ExprWalkerInfo
argument_list|(
name|op
argument_list|,
name|opContext
operator|.
name|getRowResolver
argument_list|(
name|op
argument_list|)
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining the operator stack. The dispatcher
comment|// generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|exprRules
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
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|exprNodeColumnDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getColumnProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|exprNodeFieldDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getFuncProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R3"
argument_list|,
name|exprNodeFuncDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getFuncProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R4"
argument_list|,
name|exprNodeIndexDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getIndexProcessor
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultExprProcessor
argument_list|()
argument_list|,
name|exprRules
argument_list|,
name|exprContext
argument_list|)
decl_stmt|;
name|GraphWalker
name|egw
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
name|startNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
name|clonedPreds
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeFuncDesc
name|node
range|:
name|preds
control|)
block|{
name|clonedPreds
operator|.
name|add
argument_list|(
operator|(
name|exprNodeFuncDesc
operator|)
name|node
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|startNodes
operator|.
name|addAll
argument_list|(
name|clonedPreds
argument_list|)
expr_stmt|;
name|egw
operator|.
name|startWalking
argument_list|(
name|startNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// check the root expression for final candidates
for|for
control|(
name|exprNodeFuncDesc
name|pred
range|:
name|clonedPreds
control|)
block|{
name|extractFinalCandidates
argument_list|(
name|pred
argument_list|,
name|exprContext
argument_list|)
expr_stmt|;
block|}
return|return
name|exprContext
return|;
block|}
comment|/**    * Walks through the top AND nodes and determine which of them are final candidates    */
specifier|private
specifier|static
name|void
name|extractFinalCandidates
parameter_list|(
name|exprNodeFuncDesc
name|expr
parameter_list|,
name|ExprWalkerInfo
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|ctx
operator|.
name|isCandidate
argument_list|(
name|expr
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|addFinalCandidate
argument_list|(
name|expr
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|UDFOPAnd
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|expr
operator|.
name|getUDFClass
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// now determine if any of the children are final candidates
for|for
control|(
name|Node
name|ch
range|:
name|expr
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
name|ch
operator|instanceof
name|exprNodeFuncDesc
condition|)
name|extractFinalCandidates
argument_list|(
operator|(
name|exprNodeFuncDesc
operator|)
name|ch
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

