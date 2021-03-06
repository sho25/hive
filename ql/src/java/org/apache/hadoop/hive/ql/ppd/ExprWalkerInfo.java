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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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
name|Map
operator|.
name|Entry
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

begin_comment
comment|/**  * Context for Expression Walker for determining predicate pushdown candidates  * It contains a ExprInfo object for each expression that is processed.  */
end_comment

begin_class
specifier|public
class|class
name|ExprWalkerInfo
implements|implements
name|NodeProcessorCtx
block|{
comment|/** Information maintained for an expr while walking an expr tree. */
specifier|protected
class|class
name|ExprInfo
block|{
comment|/**      * true if expr rooted at this node doesn't contain more than one table.      * alias      */
specifier|protected
name|boolean
name|isCandidate
init|=
literal|false
decl_stmt|;
comment|/** alias that this expression refers to. */
specifier|protected
name|String
name|alias
init|=
literal|null
decl_stmt|;
comment|/** new expr for this expression. */
specifier|protected
name|ExprNodeDesc
name|convertedExpr
init|=
literal|null
decl_stmt|;
block|}
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
name|OpProcFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
literal|null
decl_stmt|;
comment|/**    * Values the expression sub-trees (predicates) that can be pushed down for    * root expression tree. Since there can be more than one alias in an    * expression tree, this is a map from the alias to predicates.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|pushdownPreds
decl_stmt|;
comment|/**    * Values the expression sub-trees (predicates) that can not be pushed down for    * root expression tree. Since there can be more than one alias in an    * expression tree, this is a map from the alias to predicates.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|nonFinalPreds
decl_stmt|;
comment|/**    * this map contains a expr infos. Each key is a node in the expression tree    * and the information for each node is the value which is used while walking    * the tree by its parent.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprInfo
argument_list|>
name|exprInfoMap
decl_stmt|;
comment|/**    * This is a map from a new pushdown expressions generated by the ExprWalker    * to the old pushdown expression that it originated from. For example, if    * an output column of the current operator is _col0, which comes from an    * input column _col1, this would map the filter "Column[_col1]=2" to    * "Column[_col0]=2" ("Column[_col1]=2" is new because we move from children    * operators to parents in PPD)    */
specifier|private
specifier|final
name|Map
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
name|newToOldExprMap
decl_stmt|;
specifier|private
name|boolean
name|isDeterministic
init|=
literal|true
decl_stmt|;
specifier|public
name|ExprWalkerInfo
parameter_list|()
block|{
name|pushdownPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|nonFinalPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|exprInfoMap
operator|=
operator|new
name|IdentityHashMap
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprInfo
argument_list|>
argument_list|()
expr_stmt|;
name|newToOldExprMap
operator|=
operator|new
name|IdentityHashMap
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ExprWalkerInfo
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
name|pushdownPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|exprInfoMap
operator|=
operator|new
name|IdentityHashMap
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprInfo
argument_list|>
argument_list|()
expr_stmt|;
name|nonFinalPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|newToOldExprMap
operator|=
operator|new
name|IdentityHashMap
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the op of this expression.    */
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getOp
parameter_list|()
block|{
return|return
name|op
return|;
block|}
comment|/**    * @return the new expression to old expression map    */
specifier|public
name|Map
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
name|getNewToOldExprMap
parameter_list|()
block|{
return|return
name|newToOldExprMap
return|;
block|}
comment|/**    * Get additional info for a given expression node    */
specifier|public
name|ExprInfo
name|getExprInfo
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
return|return
name|exprInfoMap
operator|.
name|get
argument_list|(
name|expr
argument_list|)
return|;
block|}
comment|/**    * Get additional info for a given expression node if it    * exists, or create a new one and store it if it does not    */
specifier|public
name|ExprInfo
name|addExprInfo
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
name|ExprInfo
name|exprInfo
init|=
operator|new
name|ExprInfo
argument_list|()
decl_stmt|;
name|exprInfoMap
operator|.
name|put
argument_list|(
name|expr
argument_list|,
name|exprInfo
argument_list|)
expr_stmt|;
return|return
name|exprInfo
return|;
block|}
comment|/**    * Get additional info for a given expression node if it    * exists, or create a new one and store it if it does not    */
specifier|public
name|ExprInfo
name|addOrGetExprInfo
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
name|ExprInfo
name|exprInfo
init|=
name|exprInfoMap
operator|.
name|get
argument_list|(
name|expr
argument_list|)
decl_stmt|;
if|if
condition|(
name|exprInfo
operator|==
literal|null
condition|)
block|{
name|exprInfo
operator|=
operator|new
name|ExprInfo
argument_list|()
expr_stmt|;
name|exprInfoMap
operator|.
name|put
argument_list|(
name|expr
argument_list|,
name|exprInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|exprInfo
return|;
block|}
specifier|public
name|void
name|addFinalCandidate
parameter_list|(
name|String
name|alias
parameter_list|,
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|predicates
init|=
name|getPushdownPreds
argument_list|(
name|alias
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|curPred
range|:
name|predicates
control|)
block|{
if|if
condition|(
name|curPred
operator|.
name|isSame
argument_list|(
name|expr
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
name|predicates
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds the passed list of pushDowns for the alias.    *    * @param alias    * @param pushDowns    */
specifier|public
name|void
name|addPushDowns
parameter_list|(
name|String
name|alias
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|pushDowns
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|predicates
init|=
name|getPushdownPreds
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|boolean
name|isNew
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|newPred
range|:
name|pushDowns
control|)
block|{
name|isNew
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|curPred
range|:
name|predicates
control|)
block|{
if|if
condition|(
name|curPred
operator|.
name|isSame
argument_list|(
name|newPred
argument_list|)
condition|)
block|{
name|isNew
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|isNew
condition|)
block|{
name|predicates
operator|.
name|add
argument_list|(
name|newPred
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Returns the list of pushdown expressions for each alias that appear in the    * current operator's RowResolver. The exprs in each list can be combined    * using conjunction (AND).    *    * @return the map of alias to a list of pushdown predicates    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getFinalCandidates
parameter_list|()
block|{
return|return
name|pushdownPreds
return|;
block|}
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getPushdownPreds
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|predicates
init|=
name|pushdownPreds
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|predicates
operator|==
literal|null
condition|)
block|{
name|pushdownPreds
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|predicates
operator|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|predicates
return|;
block|}
specifier|public
name|boolean
name|hasAnyCandidates
parameter_list|()
block|{
if|if
condition|(
name|pushdownPreds
operator|==
literal|null
operator|||
name|pushdownPreds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
range|:
name|pushdownPreds
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|exprs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|hasNonFinalCandidates
parameter_list|()
block|{
if|if
condition|(
name|nonFinalPreds
operator|==
literal|null
operator|||
name|nonFinalPreds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
range|:
name|nonFinalPreds
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|exprs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Adds the specified expr as a non-final candidate    *    * @param expr    */
specifier|public
name|void
name|addNonFinalCandidate
parameter_list|(
name|String
name|alias
parameter_list|,
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
if|if
condition|(
name|nonFinalPreds
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|==
literal|null
condition|)
block|{
name|nonFinalPreds
operator|.
name|put
argument_list|(
name|alias
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|nonFinalPreds
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns list of non-final candidate predicate for each map.    *    * @return list of non-final candidate predicates    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getNonFinalCandidates
parameter_list|()
block|{
return|return
name|nonFinalPreds
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getResidualPredicates
parameter_list|(
name|boolean
name|clear
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|oldExprs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|entry
range|:
name|nonFinalPreds
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|converted
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
name|newExpr
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|converted
operator|.
name|add
argument_list|(
name|newToOldExprMap
operator|.
name|get
argument_list|(
name|newExpr
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|oldExprs
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|converted
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|clear
condition|)
block|{
name|nonFinalPreds
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|oldExprs
return|;
block|}
comment|/**    * Merges the specified pushdown predicates with the current class.    *    * @param ewi    *          ExpressionWalkerInfo    */
specifier|public
name|void
name|merge
parameter_list|(
name|ExprWalkerInfo
name|ewi
parameter_list|)
block|{
if|if
condition|(
name|ewi
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|e
range|:
name|ewi
operator|.
name|getFinalCandidates
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|predList
init|=
name|pushdownPreds
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|predList
operator|!=
literal|null
condition|)
block|{
name|predList
operator|.
name|addAll
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pushdownPreds
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|e
range|:
name|ewi
operator|.
name|getNonFinalCandidates
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|predList
init|=
name|nonFinalPreds
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|predList
operator|!=
literal|null
condition|)
block|{
name|predList
operator|.
name|addAll
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nonFinalPreds
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|newToOldExprMap
operator|.
name|putAll
argument_list|(
name|ewi
operator|.
name|getNewToOldExprMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * sets the deterministic flag for this expression.    *    * @param b    *          deterministic or not    */
specifier|public
name|void
name|setDeterministic
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|isDeterministic
operator|=
name|b
expr_stmt|;
block|}
comment|/**    * @return whether this expression is deterministic or not.    */
specifier|public
name|boolean
name|isDeterministic
parameter_list|()
block|{
return|return
name|isDeterministic
return|;
block|}
block|}
end_class

end_unit

