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
name|exprNodeFuncDesc
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
comment|/** Information maintained for an expr while walking an expr tree */
specifier|private
specifier|static
class|class
name|ExprInfo
block|{
comment|/** true if expr rooted at this node doesn't contain more than one table alias */
specifier|public
name|boolean
name|isCandidate
init|=
literal|false
decl_stmt|;
comment|/** alias that this expression refers to */
specifier|public
name|String
name|alias
init|=
literal|null
decl_stmt|;
comment|/** new expr for this expression.*/
specifier|public
name|exprNodeDesc
name|convertedExpr
init|=
literal|null
decl_stmt|;
specifier|public
name|ExprInfo
parameter_list|()
block|{}
specifier|public
name|ExprInfo
parameter_list|(
name|boolean
name|isCandidate
parameter_list|,
name|String
name|alias
parameter_list|,
name|exprNodeDesc
name|replacedNode
parameter_list|)
block|{
name|this
operator|.
name|isCandidate
operator|=
name|isCandidate
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|convertedExpr
operator|=
name|replacedNode
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|OpProcFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
empty_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
literal|null
decl_stmt|;
specifier|private
name|RowResolver
name|toRR
init|=
literal|null
decl_stmt|;
comment|/**    *  this map contains a expr infos. Each key is a node in the expression tree and the     * information for each node is the value which is used while walking the tree by     * its parent    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|>
name|pushdownPreds
decl_stmt|;
comment|/**    *  Values the expression sub-trees (predicates) that can be pushed down for root     * expression tree. Since there can be more than one alias in an expression tree,     * this is a map from the alias to predicates.    */
specifier|private
name|Map
argument_list|<
name|exprNodeDesc
argument_list|,
name|ExprInfo
argument_list|>
name|exprInfoMap
decl_stmt|;
specifier|public
name|ExprWalkerInfo
parameter_list|()
block|{
name|this
operator|.
name|pushdownPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|exprInfoMap
operator|=
operator|new
name|HashMap
argument_list|<
name|exprNodeDesc
argument_list|,
name|ExprInfo
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
name|Serializable
argument_list|>
name|op
parameter_list|,
specifier|final
name|RowResolver
name|toRR
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
name|this
operator|.
name|toRR
operator|=
name|toRR
expr_stmt|;
name|this
operator|.
name|pushdownPreds
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|exprInfoMap
operator|=
operator|new
name|HashMap
argument_list|<
name|exprNodeDesc
argument_list|,
name|ExprInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the op of this expression    */
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getOp
parameter_list|()
block|{
return|return
name|op
return|;
block|}
comment|/**    * @return the row resolver of the operator of this expression    */
specifier|public
name|RowResolver
name|getToRR
parameter_list|()
block|{
return|return
name|toRR
return|;
block|}
comment|/**    * @return converted expression for give node. If there is none then returns null.    */
specifier|public
name|exprNodeDesc
name|getConvertedNode
parameter_list|(
name|Node
name|nd
parameter_list|)
block|{
name|ExprInfo
name|ei
init|=
name|exprInfoMap
operator|.
name|get
argument_list|(
name|nd
argument_list|)
decl_stmt|;
if|if
condition|(
name|ei
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|ei
operator|.
name|convertedExpr
return|;
block|}
comment|/**    * adds a replacement node for this expression    * @param oldNode original node    * @param newNode new node    */
specifier|public
name|void
name|addConvertedNode
parameter_list|(
name|exprNodeDesc
name|oldNode
parameter_list|,
name|exprNodeDesc
name|newNode
parameter_list|)
block|{
name|ExprInfo
name|ei
init|=
name|exprInfoMap
operator|.
name|get
argument_list|(
name|oldNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|ei
operator|==
literal|null
condition|)
block|{
name|ei
operator|=
operator|new
name|ExprInfo
argument_list|()
expr_stmt|;
name|exprInfoMap
operator|.
name|put
argument_list|(
name|oldNode
argument_list|,
name|ei
argument_list|)
expr_stmt|;
block|}
name|ei
operator|.
name|convertedExpr
operator|=
name|newNode
expr_stmt|;
name|exprInfoMap
operator|.
name|put
argument_list|(
name|newNode
argument_list|,
operator|new
name|ExprInfo
argument_list|(
name|ei
operator|.
name|isCandidate
argument_list|,
name|ei
operator|.
name|alias
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns true if the specified expression is pushdown candidate else false    * @param expr    * @return    */
specifier|public
name|boolean
name|isCandidate
parameter_list|(
name|exprNodeDesc
name|expr
parameter_list|)
block|{
name|ExprInfo
name|ei
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
name|ei
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|ei
operator|.
name|isCandidate
return|;
block|}
comment|/**    * Marks the specified expr to the specified value    * @param expr    * @param b can    */
specifier|public
name|void
name|setIsCandidate
parameter_list|(
name|exprNodeDesc
name|expr
parameter_list|,
name|boolean
name|b
parameter_list|)
block|{
name|ExprInfo
name|ei
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
name|ei
operator|==
literal|null
condition|)
block|{
name|ei
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
name|ei
argument_list|)
expr_stmt|;
block|}
name|ei
operator|.
name|isCandidate
operator|=
name|b
expr_stmt|;
block|}
comment|/**    * Returns the alias of the specified expr    * @param expr    * @return    */
specifier|public
name|String
name|getAlias
parameter_list|(
name|exprNodeDesc
name|expr
parameter_list|)
block|{
name|ExprInfo
name|ei
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
name|ei
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|ei
operator|.
name|alias
return|;
block|}
comment|/**    * Adds the specified alias to the specified expr    * @param expr    * @param alias    */
specifier|public
name|void
name|addAlias
parameter_list|(
name|exprNodeDesc
name|expr
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
if|if
condition|(
name|alias
operator|==
literal|null
condition|)
return|return;
name|ExprInfo
name|ei
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
name|ei
operator|==
literal|null
condition|)
block|{
name|ei
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
name|ei
argument_list|)
expr_stmt|;
block|}
name|ei
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
block|}
comment|/**    * Adds the specified expr as the top-most pushdown expr (ie all its children can be pushed)    * @param expr    */
specifier|public
name|void
name|addFinalCandidate
parameter_list|(
name|exprNodeFuncDesc
name|expr
parameter_list|)
block|{
name|String
name|alias
init|=
name|this
operator|.
name|getAlias
argument_list|(
name|expr
argument_list|)
decl_stmt|;
if|if
condition|(
name|pushdownPreds
operator|.
name|get
argument_list|(
name|alias
argument_list|)
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
operator|new
name|ArrayList
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|pushdownPreds
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|add
argument_list|(
operator|(
name|exprNodeFuncDesc
operator|)
name|expr
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the list of pushdown expressions for each alias that appear in the current operator's    * RowResolver. The exprs in each list can be combined using conjunction (AND)    * @return the map of alias to a list of pushdown predicates    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
argument_list|>
argument_list|>
name|getFinalCandidates
parameter_list|()
block|{
return|return
name|pushdownPreds
return|;
block|}
comment|/**    * Merges the specified pushdown predicates with the current class    * @param ewi ExpressionWalkerInfo    */
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
return|return;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeFuncDesc
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
name|exprNodeFuncDesc
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
block|}
block|}
end_class

end_unit

