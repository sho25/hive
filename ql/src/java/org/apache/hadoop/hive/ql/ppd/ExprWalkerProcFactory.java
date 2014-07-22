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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FunctionRegistry
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
name|ExprNodeFieldDesc
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
comment|/**  * Expression factory for predicate pushdown processing. Each processor  * determines whether the expression is a possible candidate for predicate  * pushdown optimization for the given operator  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ExprWalkerProcFactory
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ExprWalkerProcFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * ColumnExprProcessor.    *    */
specifier|public
specifier|static
class|class
name|ColumnExprProcessor
implements|implements
name|NodeProcessor
block|{
comment|/**      * Converts the reference from child row resolver to current row resolver.      */
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
name|ExprNodeColumnDesc
name|colref
init|=
operator|(
name|ExprNodeColumnDesc
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
name|OperatorDesc
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
name|boolean
name|isCandidate
init|=
literal|true
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
name|ExprNodeDesc
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
comment|// means that expression can't be pushed either because it is value in
comment|// group by
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
else|else
block|{
if|if
condition|(
name|exp
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|isCandidate
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|exp
operator|instanceof
name|ExprNodeColumnDesc
operator|&&
name|colAlias
operator|==
literal|null
condition|)
block|{
name|ExprNodeColumnDesc
name|column
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|exp
decl_stmt|;
name|colAlias
operator|=
operator|new
name|String
index|[]
block|{
name|column
operator|.
name|getTabAlias
argument_list|()
block|,
name|column
operator|.
name|getColumn
argument_list|()
block|}
expr_stmt|;
block|}
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
name|isCandidate
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
block|{
return|return
literal|false
return|;
block|}
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
name|isCandidate
argument_list|)
expr_stmt|;
return|return
name|isCandidate
return|;
block|}
block|}
comment|/**    * FieldExprProcessor.    *    */
specifier|public
specifier|static
class|class
name|FieldExprProcessor
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
name|ExprNodeFieldDesc
name|expr
init|=
operator|(
name|ExprNodeFieldDesc
operator|)
name|nd
decl_stmt|;
name|boolean
name|isCandidate
init|=
literal|true
decl_stmt|;
assert|assert
operator|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
assert|;
name|ExprNodeDesc
name|ch
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nd
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeDesc
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
name|setDesc
argument_list|(
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
comment|// need to iterate through all children even if one is found to be not a
comment|// candidate
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
comment|/**    * If all children are candidates and refer only to one table alias then this    * expr is a candidate else it is not a candidate but its children could be    * final candidates.    */
specifier|public
specifier|static
class|class
name|GenericFuncExprProcessor
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
name|ExprNodeGenericFuncDesc
name|expr
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|nd
decl_stmt|;
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|expr
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
condition|)
block|{
comment|// this GenericUDF can't be pushed down
name|ctx
operator|.
name|setIsCandidate
argument_list|(
name|expr
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setDeterministic
argument_list|(
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
name|ExprNodeDesc
name|ch
init|=
operator|(
name|ExprNodeDesc
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
name|ExprNodeDesc
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
name|getChildren
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
comment|// need to iterate through all children even if one is found to be not a
comment|// candidate
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
block|{
break|break;
block|}
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
comment|/**    * For constants and null expressions.    */
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
name|ExprNodeDesc
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
name|getGenericFuncProcessor
parameter_list|()
block|{
return|return
operator|new
name|GenericFuncExprProcessor
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
specifier|private
specifier|static
name|NodeProcessor
name|getFieldProcessor
parameter_list|()
block|{
return|return
operator|new
name|FieldExprProcessor
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
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|ExprNodeDesc
name|pred
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|preds
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
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
comment|/**    * Extracts pushdown predicates from the given list of predicate expression.    *    * @param opContext    *          operator context used for resolving column references    * @param op    *          operator of the predicates being processed    * @param preds    * @return The expression walker information    * @throws SemanticException    */
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
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
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
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
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
name|ExprNodeColumnDesc
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
name|ExprNodeFieldDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getFieldProcessor
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
name|ExprNodeGenericFuncDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getGenericFuncProcessor
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
name|ExprNodeDesc
argument_list|>
name|clonedPreds
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|node
range|:
name|preds
control|)
block|{
name|ExprNodeDesc
name|clone
init|=
name|node
operator|.
name|clone
argument_list|()
decl_stmt|;
name|clonedPreds
operator|.
name|add
argument_list|(
name|clone
argument_list|)
expr_stmt|;
name|exprContext
operator|.
name|getNewToOldExprMap
argument_list|()
operator|.
name|put
argument_list|(
name|clone
argument_list|,
name|node
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
name|HiveConf
name|conf
init|=
name|opContext
operator|.
name|getParseContext
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// check the root expression for final candidates
for|for
control|(
name|ExprNodeDesc
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
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|exprContext
return|;
block|}
comment|/**    * Walks through the top AND nodes and determine which of them are final    * candidates.    */
specifier|private
specifier|static
name|void
name|extractFinalCandidates
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|ExprWalkerInfo
name|ctx
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
comment|// We decompose an AND expression into its parts before checking if the
comment|// entire expression is a candidate because each part may be a candidate
comment|// for replicating transitively over an equijoin condition.
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|expr
argument_list|)
condition|)
block|{
comment|// If the operator is AND, we need to determine if any of the children are
comment|// final candidates.
comment|// For the children, we populate the NewToOldExprMap to keep track of
comment|// the original condition before rewriting it for this operator
assert|assert
name|ctx
operator|.
name|getNewToOldExprMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|expr
argument_list|)
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expr
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
name|ctx
operator|.
name|getNewToOldExprMap
argument_list|()
operator|.
name|put
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ctx
operator|.
name|getNewToOldExprMap
argument_list|()
operator|.
name|get
argument_list|(
name|expr
argument_list|)
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|extractFinalCandidates
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ctx
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
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
elseif|else
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|expr
argument_list|)
operator|&&
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
name|HIVEPPDREMOVEDUPLICATEFILTERS
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|addNonFinalCandidate
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ExprWalkerProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

