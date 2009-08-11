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
name|HashSet
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
name|TableScanOperator
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
name|filterDesc
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
name|joinCond
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
name|joinDesc
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * Operator factory for predicate pushdown processing of operator graph  * Each operator determines the pushdown predicates by walking the expression tree.  * Each operator merges its own pushdown predicates with those of its children  * Finally the TableScan operator gathers all the predicates and inserts a filter operator   * after itself.  * TODO: Further optimizations  *   1) Multi-insert case  *   2) Create a filter operator for those predicates that couldn't be pushed to the previous   *      operators in the data flow  *   3) Merge multiple sequential filter predicates into so that plans are more readable  *   4) Remove predicates from filter operators that have been pushed. Currently these pushed  *      predicates are evaluated twice.  */
end_comment

begin_class
specifier|public
class|class
name|OpProcFactory
block|{
comment|/**    * Processor for Script Operator    * Prevents any predicates being pushed    */
specifier|public
specifier|static
class|class
name|ScriptPPD
extends|extends
name|DefaultPPD
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
comment|// script operator is a black-box to hive so no optimization here
comment|// assuming that nothing can be pushed above the script op
comment|// same with LIMIT op
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Combines predicates of its child into a single expression and adds a filter op as new child    */
specifier|public
specifier|static
class|class
name|TableScanPPD
extends|extends
name|DefaultPPD
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|OpWalkerInfo
name|owi
init|=
operator|(
name|OpWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|RowResolver
name|inputRR
init|=
name|owi
operator|.
name|getRowResolver
argument_list|(
name|nd
argument_list|)
decl_stmt|;
name|TableScanOperator
name|tsOp
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|mergeWithChildrenPred
argument_list|(
name|tsOp
argument_list|,
name|owi
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ExprWalkerInfo
name|pushDownPreds
init|=
name|owi
operator|.
name|getPrunedPreds
argument_list|(
name|tsOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|pushDownPreds
operator|==
literal|null
operator|||
name|pushDownPreds
operator|.
name|getFinalCandidates
argument_list|()
operator|==
literal|null
operator|||
name|pushDownPreds
operator|.
name|getFinalCandidates
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// combine all predicates into a single expression
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
name|preds
init|=
literal|null
decl_stmt|;
name|exprNodeDesc
name|condn
init|=
literal|null
decl_stmt|;
name|Iterator
argument_list|<
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|iterator
init|=
name|pushDownPreds
operator|.
name|getFinalCandidates
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|preds
operator|=
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|condn
operator|==
literal|null
condition|)
block|{
name|condn
operator|=
name|preds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
for|for
control|(
init|;
name|i
operator|<
name|preds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|condn
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
operator|(
name|exprNodeDesc
operator|)
name|preds
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|condn
operator|=
operator|new
name|exprNodeFuncDesc
argument_list|(
literal|"AND"
argument_list|,
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|getUDFClass
argument_list|(
literal|"AND"
argument_list|)
argument_list|,
name|FunctionRegistry
operator|.
name|getUDFMethod
argument_list|(
literal|"AND"
argument_list|,
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|condn
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// add new filter op
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|originalChilren
init|=
name|tsOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|tsOp
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|filterDesc
argument_list|>
name|output
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|new
name|filterDesc
argument_list|(
name|condn
argument_list|,
literal|false
argument_list|)
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
name|tsOp
argument_list|)
decl_stmt|;
name|output
operator|.
name|setChildOperators
argument_list|(
name|originalChilren
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|ch
range|:
name|originalChilren
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOperators
init|=
name|ch
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|parentOperators
operator|.
name|indexOf
argument_list|(
name|tsOp
argument_list|)
decl_stmt|;
assert|assert
name|pos
operator|!=
operator|-
literal|1
assert|;
name|parentOperators
operator|.
name|remove
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|parentOperators
operator|.
name|add
argument_list|(
name|pos
argument_list|,
name|output
argument_list|)
expr_stmt|;
comment|// add the new op as the old
block|}
name|OpParseContext
name|ctx
init|=
operator|new
name|OpParseContext
argument_list|(
name|inputRR
argument_list|)
decl_stmt|;
name|owi
operator|.
name|put
argument_list|(
name|output
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
block|}
comment|/**    * Determines the push down predicates in its where expression and then combines it with    * the push down predicates that are passed from its children    */
specifier|public
specifier|static
class|class
name|FilterPPD
extends|extends
name|DefaultPPD
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|OpWalkerInfo
name|owi
init|=
operator|(
name|OpWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
name|exprNodeDesc
name|predicate
init|=
operator|(
operator|(
operator|(
name|FilterOperator
operator|)
name|nd
operator|)
operator|.
name|getConf
argument_list|()
operator|)
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
comment|// get pushdown predicates for this operato's predicate
name|ExprWalkerInfo
name|ewi
init|=
name|ExprWalkerProcFactory
operator|.
name|extractPushdownPreds
argument_list|(
name|owi
argument_list|,
name|op
argument_list|,
name|predicate
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ewi
operator|.
name|isDeterministic
argument_list|()
condition|)
block|{
comment|/* predicate is not deterministic */
return|return
literal|null
return|;
block|}
name|logExpr
argument_list|(
name|nd
argument_list|,
name|ewi
argument_list|)
expr_stmt|;
name|owi
operator|.
name|putPrunedPreds
argument_list|(
name|op
argument_list|,
name|ewi
argument_list|)
expr_stmt|;
comment|// merge it with children predicates
name|mergeWithChildrenPred
argument_list|(
name|op
argument_list|,
name|owi
argument_list|,
name|ewi
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Determines predicates for which alias can be pushed to it's parents.    * See the comments for getQualifiedAliases function    */
specifier|public
specifier|static
class|class
name|JoinPPD
extends|extends
name|DefaultPPD
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|OpWalkerInfo
name|owi
init|=
operator|(
name|OpWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|getQualifiedAliases
argument_list|(
operator|(
name|JoinOperator
operator|)
name|nd
argument_list|,
name|owi
operator|.
name|getRowResolver
argument_list|(
name|nd
argument_list|)
argument_list|)
decl_stmt|;
name|mergeWithChildrenPred
argument_list|(
name|nd
argument_list|,
name|owi
argument_list|,
literal|null
argument_list|,
name|aliases
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**      * Figures out the aliases for whom it is safe to push predicates based on ANSI SQL semantics      * For inner join, all predicates for all aliases can be pushed      * For full outer join, none of the predicates can be pushed as that would limit the number of       *   rows for join      * For left outer join, all the predicates on the left side aliases can be pushed up      * For right outer join, all the predicates on the right side aliases can be pushed up      * Joins chain containing both left and right outer joins are treated as full outer join.      * TODO: further optimization opportunity for the case a.c1 = b.c1 and b.c2 = c.c2      *   a and b are first joined and then the result with c. But the second join op currently       *   treats a and b as separate aliases and thus disallowing predicate expr containing both       *   tables a and b (such as a.c3 +  a.c4> 20). Such predicates also can be pushed just above      *   the second join and below the first join      *        * @param op Join Operator      * @param rr Row resolver      * @return set of qualified aliases       */
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getQualifiedAliases
parameter_list|(
name|JoinOperator
name|op
parameter_list|,
name|RowResolver
name|rr
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|loj
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
name|roj
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|oj
init|=
literal|false
decl_stmt|;
name|joinCond
index|[]
name|conds
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|posToAliasMap
init|=
name|op
operator|.
name|getPosToAliasMap
argument_list|()
decl_stmt|;
for|for
control|(
name|joinCond
name|jc
range|:
name|conds
control|)
block|{
if|if
condition|(
name|jc
operator|.
name|getType
argument_list|()
operator|==
name|joinDesc
operator|.
name|FULL_OUTER_JOIN
condition|)
block|{
name|oj
operator|=
literal|true
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|jc
operator|.
name|getType
argument_list|()
operator|==
name|joinDesc
operator|.
name|LEFT_OUTER_JOIN
condition|)
block|{
if|if
condition|(
name|jc
operator|.
name|getLeft
argument_list|()
operator|<
name|loj
condition|)
name|loj
operator|=
name|jc
operator|.
name|getLeft
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|jc
operator|.
name|getType
argument_list|()
operator|==
name|joinDesc
operator|.
name|RIGHT_OUTER_JOIN
condition|)
block|{
if|if
condition|(
name|jc
operator|.
name|getRight
argument_list|()
operator|>
name|roj
condition|)
name|roj
operator|=
name|jc
operator|.
name|getRight
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|oj
operator|||
operator|(
name|loj
operator|!=
name|Integer
operator|.
name|MAX_VALUE
operator|&&
name|roj
operator|!=
operator|-
literal|1
operator|)
condition|)
return|return
name|aliases
return|;
for|for
control|(
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|pa
range|:
name|posToAliasMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|loj
operator|!=
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
if|if
condition|(
name|pa
operator|.
name|getKey
argument_list|()
operator|<=
name|loj
condition|)
name|aliases
operator|.
name|addAll
argument_list|(
name|pa
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|roj
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|pa
operator|.
name|getKey
argument_list|()
operator|>=
name|roj
condition|)
name|aliases
operator|.
name|addAll
argument_list|(
name|pa
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|aliases
operator|.
name|addAll
argument_list|(
name|pa
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|aliases2
init|=
name|rr
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
name|aliases
operator|.
name|retainAll
argument_list|(
name|aliases2
argument_list|)
expr_stmt|;
return|return
name|aliases
return|;
block|}
block|}
comment|/**    * Processor for ReduceSink operator.    *    */
specifier|public
specifier|static
class|class
name|ReduceSinkPPD
extends|extends
name|DefaultPPD
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|OpWalkerInfo
name|owi
init|=
operator|(
name|OpWalkerInfo
operator|)
name|procCtx
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|owi
operator|.
name|getRowResolver
argument_list|(
name|nd
argument_list|)
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
name|boolean
name|ignoreAliases
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|aliases
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|aliases
operator|.
name|contains
argument_list|(
literal|""
argument_list|)
condition|)
block|{
comment|// Reduce sink of group by operator
name|ignoreAliases
operator|=
literal|true
expr_stmt|;
block|}
name|mergeWithChildrenPred
argument_list|(
name|nd
argument_list|,
name|owi
argument_list|,
literal|null
argument_list|,
name|aliases
argument_list|,
name|ignoreAliases
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Default processor which just merges its children    */
specifier|public
specifier|static
class|class
name|DefaultPPD
implements|implements
name|NodeProcessor
block|{
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing for "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|"("
operator|+
operator|(
operator|(
name|Operator
operator|)
name|nd
operator|)
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|mergeWithChildrenPred
argument_list|(
name|nd
argument_list|,
operator|(
name|OpWalkerInfo
operator|)
name|procCtx
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**      * @param nd      * @param ewi      */
specifier|protected
name|void
name|logExpr
parameter_list|(
name|Node
name|nd
parameter_list|,
name|ExprWalkerInfo
name|ewi
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Pushdown Predicates of "
operator|+
name|nd
operator|.
name|getName
argument_list|()
operator|+
literal|" For Alias : "
operator|+
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|exprNodeDesc
name|n
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"\t"
operator|+
name|n
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Take current operators pushdown predicates and merges them with children's pushdown predicates      * @param nd current operator      * @param owi operator context during this walk      * @param ewi pushdown predicates (part of expression walker info)      * @param aliases aliases that this operator can pushdown. null means that all aliases can be pushed down      * @param ignoreAliases       * @throws SemanticException       */
specifier|protected
name|void
name|mergeWithChildrenPred
parameter_list|(
name|Node
name|nd
parameter_list|,
name|OpWalkerInfo
name|owi
parameter_list|,
name|ExprWalkerInfo
name|ewi
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
parameter_list|,
name|boolean
name|ignoreAliases
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
operator|||
name|nd
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|// ppd for multi-insert query is not yet implemented
comment|// no-op for leafs
return|return;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
name|ExprWalkerInfo
name|childPreds
init|=
name|owi
operator|.
name|getPrunedPreds
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
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
argument_list|)
decl_stmt|;
if|if
condition|(
name|childPreds
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|ewi
operator|==
literal|null
condition|)
block|{
name|ewi
operator|=
operator|new
name|ExprWalkerInfo
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|e
range|:
name|childPreds
operator|.
name|getFinalCandidates
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|ignoreAliases
operator|||
name|aliases
operator|==
literal|null
operator|||
name|aliases
operator|.
name|contains
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
operator|||
name|e
operator|.
name|getKey
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// e.getKey() (alias) can be null in case of constant expressions. see input8.q
name|ExprWalkerInfo
name|extractPushdownPreds
init|=
name|ExprWalkerProcFactory
operator|.
name|extractPushdownPreds
argument_list|(
name|owi
argument_list|,
name|op
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|ewi
operator|.
name|merge
argument_list|(
name|extractPushdownPreds
argument_list|)
expr_stmt|;
name|logExpr
argument_list|(
name|nd
argument_list|,
name|extractPushdownPreds
argument_list|)
expr_stmt|;
block|}
block|}
name|owi
operator|.
name|putPrunedPreds
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|,
name|ewi
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|FilterPPD
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getJoinProc
parameter_list|()
block|{
return|return
operator|new
name|JoinPPD
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getRSProc
parameter_list|()
block|{
return|return
operator|new
name|ReduceSinkPPD
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getTSProc
parameter_list|()
block|{
return|return
operator|new
name|TableScanPPD
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
name|DefaultPPD
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getSCRProc
parameter_list|()
block|{
return|return
operator|new
name|ScriptPPD
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getLIMProc
parameter_list|()
block|{
return|return
operator|new
name|ScriptPPD
argument_list|()
return|;
block|}
block|}
end_class

end_unit

