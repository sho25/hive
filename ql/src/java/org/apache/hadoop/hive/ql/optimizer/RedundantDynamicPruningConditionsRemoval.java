begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|calcite
operator|.
name|util
operator|.
name|Pair
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
name|metadata
operator|.
name|Table
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
name|ExprNodeConstantDesc
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
name|ExprNodeDynamicListDesc
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
name|FilterDesc
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
name|generic
operator|.
name|GenericUDFBaseCompare
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
name|generic
operator|.
name|GenericUDFIn
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
name|Lists
import|;
end_import

begin_comment
comment|/**  * Takes a Filter operator on top of a TableScan and removes dynamic pruning conditions  * if static partition pruning has been triggered already.  *   * This transformation is executed when CBO is on and hence we can guarantee that the filtering  * conditions on the partition columns will be immediately on top of the TableScan operator.  *  */
end_comment

begin_class
specifier|public
class|class
name|RedundantDynamicPruningConditionsRemoval
extends|extends
name|Transform
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
name|RedundantDynamicPruningConditionsRemoval
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Transform the query tree.    *    * @param pctx the current parse context    */
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
comment|// Make sure semijoin is not enabled. If it is, then do not remove the dynamic partition pruning predicates.
if|if
condition|(
operator|!
name|pctx
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
name|TEZ_DYNAMIC_SEMIJOIN_REDUCTION
argument_list|)
condition|)
block|{
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
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|FilterTransformer
argument_list|()
argument_list|)
expr_stmt|;
name|SemanticDispatcher
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
name|SemanticGraphWalker
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
block|}
return|return
name|pctx
return|;
block|}
specifier|private
class|class
name|FilterTransformer
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
name|FilterOperator
name|filter
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|FilterDesc
name|desc
init|=
name|filter
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
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
comment|// collect
name|CollectContext
name|removalContext
init|=
operator|new
name|CollectContext
argument_list|()
decl_stmt|;
name|collect
argument_list|(
name|desc
operator|.
name|getPredicate
argument_list|()
argument_list|,
name|removalContext
argument_list|)
expr_stmt|;
name|CollectContext
name|tsRemovalContext
init|=
operator|new
name|CollectContext
argument_list|()
decl_stmt|;
name|collect
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
argument_list|,
name|tsRemovalContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
name|pair
range|:
name|removalContext
operator|.
name|dynamicListNodes
control|)
block|{
name|ExprNodeDesc
name|child
init|=
name|pair
operator|.
name|left
decl_stmt|;
name|ExprNodeDesc
name|columnDesc
init|=
name|child
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|child
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|ExprNodeDynamicListDesc
assert|;
name|ExprNodeDesc
name|parent
init|=
name|pair
operator|.
name|right
decl_stmt|;
name|String
name|column
init|=
name|ExprNodeDescUtils
operator|.
name|extractColName
argument_list|(
name|columnDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|column
operator|!=
literal|null
condition|)
block|{
name|Table
name|table
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|boolean
name|generate
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
operator|&&
name|table
operator|.
name|isPartitionKey
argument_list|(
name|column
argument_list|)
condition|)
block|{
name|generate
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|filterColumnDesc
range|:
name|removalContext
operator|.
name|comparatorNodes
control|)
block|{
if|if
condition|(
name|columnDesc
operator|.
name|isSame
argument_list|(
name|filterColumnDesc
argument_list|)
condition|)
block|{
name|generate
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|generate
condition|)
block|{
comment|// We can safely remove the condition by replacing it with "true"
name|ExprNodeDesc
name|constNode
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
operator|==
literal|null
condition|)
block|{
name|desc
operator|.
name|setPredicate
argument_list|(
name|constNode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|i
init|=
name|parent
operator|.
name|getChildren
argument_list|()
operator|.
name|indexOf
argument_list|(
name|child
argument_list|)
decl_stmt|;
name|parent
operator|.
name|getChildren
argument_list|()
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|parent
operator|.
name|getChildren
argument_list|()
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|constNode
argument_list|)
expr_stmt|;
block|}
comment|// We remove it from the TS too if it was pushed
for|for
control|(
name|Pair
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
name|tsPair
range|:
name|tsRemovalContext
operator|.
name|dynamicListNodes
control|)
block|{
name|ExprNodeDesc
name|tsChild
init|=
name|tsPair
operator|.
name|left
decl_stmt|;
name|ExprNodeDesc
name|tsParent
init|=
name|tsPair
operator|.
name|right
decl_stmt|;
if|if
condition|(
name|tsChild
operator|.
name|isSame
argument_list|(
name|child
argument_list|)
condition|)
block|{
if|if
condition|(
name|tsParent
operator|==
literal|null
condition|)
block|{
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|setFilterExpr
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|i
init|=
name|tsParent
operator|.
name|getChildren
argument_list|()
operator|.
name|indexOf
argument_list|(
name|tsChild
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|!=
operator|-
literal|1
condition|)
block|{
name|tsParent
operator|.
name|getChildren
argument_list|()
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|tsParent
operator|.
name|getChildren
argument_list|()
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|constNode
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Dynamic pruning condition removed: "
operator|+
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|private
specifier|static
name|void
name|collect
parameter_list|(
name|ExprNodeDesc
name|pred
parameter_list|,
name|CollectContext
name|listContext
parameter_list|)
block|{
name|collect
argument_list|(
literal|null
argument_list|,
name|pred
argument_list|,
name|listContext
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|collect
parameter_list|(
name|ExprNodeDesc
name|parent
parameter_list|,
name|ExprNodeDesc
name|child
parameter_list|,
name|CollectContext
name|listContext
parameter_list|)
block|{
if|if
condition|(
name|child
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|&&
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|child
operator|)
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFIn
condition|)
block|{
if|if
condition|(
name|child
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|ExprNodeDynamicListDesc
condition|)
block|{
name|listContext
operator|.
name|dynamicListNodes
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|(
name|child
argument_list|,
name|parent
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|child
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|&&
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|child
operator|)
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFBaseCompare
operator|&&
name|child
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
name|ExprNodeDesc
name|leftCol
init|=
name|child
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
name|rightCol
init|=
name|child
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|leftColDesc
init|=
name|ExprNodeDescUtils
operator|.
name|getColumnExpr
argument_list|(
name|leftCol
argument_list|)
decl_stmt|;
if|if
condition|(
name|leftColDesc
operator|!=
literal|null
condition|)
block|{
name|boolean
name|rightConstant
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|rightCol
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
name|rightConstant
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rightCol
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|ExprNodeDesc
name|foldedExpr
init|=
name|ConstantPropagateProcFactory
operator|.
name|foldExpr
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|rightCol
argument_list|)
decl_stmt|;
name|rightConstant
operator|=
name|foldedExpr
operator|!=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|rightConstant
condition|)
block|{
name|listContext
operator|.
name|comparatorNodes
operator|.
name|add
argument_list|(
name|leftColDesc
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ExprNodeColumnDesc
name|rightColDesc
init|=
name|ExprNodeDescUtils
operator|.
name|getColumnExpr
argument_list|(
name|rightCol
argument_list|)
decl_stmt|;
if|if
condition|(
name|rightColDesc
operator|!=
literal|null
condition|)
block|{
name|boolean
name|leftConstant
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|leftCol
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
name|leftConstant
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|leftCol
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|ExprNodeDesc
name|foldedExpr
init|=
name|ConstantPropagateProcFactory
operator|.
name|foldExpr
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|leftCol
argument_list|)
decl_stmt|;
name|leftConstant
operator|=
name|foldedExpr
operator|!=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|leftConstant
condition|)
block|{
name|listContext
operator|.
name|comparatorNodes
operator|.
name|add
argument_list|(
name|rightColDesc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return;
block|}
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|child
argument_list|)
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|newChild
range|:
name|child
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|collect
argument_list|(
name|child
argument_list|,
name|newChild
argument_list|,
name|listContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
class|class
name|CollectContext
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|dynamicListNodes
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|comparatorNodes
decl_stmt|;
specifier|public
name|CollectContext
parameter_list|()
block|{
name|this
operator|.
name|dynamicListNodes
operator|=
name|Lists
operator|.
expr|<
name|Pair
argument_list|<
name|ExprNodeDesc
argument_list|,
name|ExprNodeDesc
argument_list|>
operator|>
name|newArrayList
argument_list|()
expr_stmt|;
name|this
operator|.
name|comparatorNodes
operator|=
name|Lists
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|newArrayList
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

