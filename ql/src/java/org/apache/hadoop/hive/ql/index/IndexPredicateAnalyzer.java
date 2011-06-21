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
name|index
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
name|GenericUDFBridge
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

begin_comment
comment|/**  * IndexPredicateAnalyzer decomposes predicates, separating the parts  * which can be satisfied by an index from the parts which cannot.  * Currently, it only supports pure conjunctions over binary expressions  * comparing a column reference with a constant value.  It is assumed  * that all column aliases encountered refer to the same table.  */
end_comment

begin_class
specifier|public
class|class
name|IndexPredicateAnalyzer
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
name|IndexPredicateAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|udfNames
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|allowedColumnNames
decl_stmt|;
specifier|public
name|IndexPredicateAnalyzer
parameter_list|()
block|{
name|udfNames
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Registers a comparison operator as one which can be satisfied    * by an index search.  Unless this is called, analyzePredicate    * will never find any indexable conditions.    *    * @param udfName name of comparison operator as returned    * by either {@link GenericUDFBridge#getUdfName} (for simple UDF's)    * or udf.getClass().getName() (for generic UDF's).    */
specifier|public
name|void
name|addComparisonOp
parameter_list|(
name|String
name|udfName
parameter_list|)
block|{
name|udfNames
operator|.
name|add
argument_list|(
name|udfName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Clears the set of column names allowed in comparisons.  (Initially, all    * column names are allowed.)    */
specifier|public
name|void
name|clearAllowedColumnNames
parameter_list|()
block|{
name|allowedColumnNames
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Adds a column name to the set of column names allowed.    *    * @param columnName name of column to be allowed    */
specifier|public
name|void
name|allowColumnName
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
if|if
condition|(
name|allowedColumnNames
operator|==
literal|null
condition|)
block|{
name|clearAllowedColumnNames
argument_list|()
expr_stmt|;
block|}
name|allowedColumnNames
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Analyzes a predicate.    *    * @param predicate predicate to be analyzed    *    * @param searchConditions receives conditions produced by analysis    *    * @return residual predicate which could not be translated to    * searchConditions    */
specifier|public
name|ExprNodeDesc
name|analyzePredicate
parameter_list|(
name|ExprNodeDesc
name|predicate
parameter_list|,
specifier|final
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
parameter_list|)
block|{
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
name|NodeProcessor
name|nodeProcessor
init|=
operator|new
name|NodeProcessor
argument_list|()
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
comment|// We can only push down stuff which appears as part of
comment|// a pure conjunction:  reject OR, CASE, etc.
for|for
control|(
name|Node
name|ancestor
range|:
name|stack
control|)
block|{
if|if
condition|(
name|nd
operator|==
name|ancestor
condition|)
block|{
break|break;
block|}
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|ancestor
argument_list|)
condition|)
block|{
return|return
name|nd
return|;
block|}
block|}
return|return
name|analyzeExpr
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|nd
argument_list|,
name|searchConditions
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|nodeProcessor
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
name|add
argument_list|(
name|predicate
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
name|ExprNodeDesc
name|residualPredicate
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nodeOutput
operator|.
name|get
argument_list|(
name|predicate
argument_list|)
decl_stmt|;
return|return
name|residualPredicate
return|;
block|}
specifier|private
name|ExprNodeDesc
name|analyzeExpr
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|)
condition|)
block|{
return|return
name|expr
return|;
block|}
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
assert|assert
operator|(
name|nodeOutputs
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|ExprNodeDesc
name|residual1
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nodeOutputs
index|[
literal|0
index|]
decl_stmt|;
name|ExprNodeDesc
name|residual2
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nodeOutputs
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|residual1
operator|==
literal|null
condition|)
block|{
return|return
name|residual2
return|;
block|}
if|if
condition|(
name|residual2
operator|==
literal|null
condition|)
block|{
return|return
name|residual1
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|residuals
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|residuals
operator|.
name|add
argument_list|(
name|residual1
argument_list|)
expr_stmt|;
name|residuals
operator|.
name|add
argument_list|(
name|residual2
argument_list|)
expr_stmt|;
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|getGenericUDFForAnd
argument_list|()
argument_list|,
name|residuals
argument_list|)
return|;
block|}
name|String
name|udfName
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|funcDesc
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
decl_stmt|;
if|if
condition|(
name|funcDesc
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFBridge
condition|)
block|{
name|GenericUDFBridge
name|func
init|=
operator|(
name|GenericUDFBridge
operator|)
name|funcDesc
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
name|udfName
operator|=
name|func
operator|.
name|getUdfName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|udfName
operator|=
name|funcDesc
operator|.
name|getGenericUDF
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|udfNames
operator|.
name|contains
argument_list|(
name|udfName
argument_list|)
condition|)
block|{
return|return
name|expr
return|;
block|}
name|ExprNodeDesc
name|child1
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nodeOutputs
index|[
literal|0
index|]
decl_stmt|;
name|ExprNodeDesc
name|child2
init|=
operator|(
name|ExprNodeDesc
operator|)
name|nodeOutputs
index|[
literal|1
index|]
decl_stmt|;
name|ExprNodeColumnDesc
name|columnDesc
init|=
literal|null
decl_stmt|;
name|ExprNodeConstantDesc
name|constantDesc
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|child1
operator|instanceof
name|ExprNodeColumnDesc
operator|)
operator|&&
operator|(
name|child2
operator|instanceof
name|ExprNodeConstantDesc
operator|)
condition|)
block|{
comment|// COL<op> CONSTANT
name|columnDesc
operator|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|child1
expr_stmt|;
name|constantDesc
operator|=
operator|(
name|ExprNodeConstantDesc
operator|)
name|child2
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|child2
operator|instanceof
name|ExprNodeColumnDesc
operator|)
operator|&&
operator|(
name|child1
operator|instanceof
name|ExprNodeConstantDesc
operator|)
condition|)
block|{
comment|// CONSTANT<op> COL
name|columnDesc
operator|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|child2
expr_stmt|;
name|constantDesc
operator|=
operator|(
name|ExprNodeConstantDesc
operator|)
name|child1
expr_stmt|;
block|}
if|if
condition|(
name|columnDesc
operator|==
literal|null
condition|)
block|{
return|return
name|expr
return|;
block|}
if|if
condition|(
name|allowedColumnNames
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|allowedColumnNames
operator|.
name|contains
argument_list|(
name|columnDesc
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|expr
return|;
block|}
block|}
name|searchConditions
operator|.
name|add
argument_list|(
operator|new
name|IndexSearchCondition
argument_list|(
name|columnDesc
argument_list|,
name|udfName
argument_list|,
name|constantDesc
argument_list|,
name|expr
argument_list|)
argument_list|)
expr_stmt|;
comment|// we converted the expression to a search condition, so
comment|// remove it from the residual predicate
return|return
literal|null
return|;
block|}
comment|/**    * Translates search conditions back to ExprNodeDesc form (as    * a left-deep conjunction).    *    * @param searchConditions (typically produced by analyzePredicate)    *    * @return ExprNodeDesc form of search conditions    */
specifier|public
name|ExprNodeDesc
name|translateSearchConditions
parameter_list|(
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
parameter_list|)
block|{
name|ExprNodeDesc
name|expr
init|=
literal|null
decl_stmt|;
for|for
control|(
name|IndexSearchCondition
name|searchCondition
range|:
name|searchConditions
control|)
block|{
if|if
condition|(
name|expr
operator|==
literal|null
condition|)
block|{
name|expr
operator|=
name|searchCondition
operator|.
name|getComparisonExpr
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|searchCondition
operator|.
name|getComparisonExpr
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|getGenericUDFForAnd
argument_list|()
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
return|return
name|expr
return|;
block|}
block|}
end_class

end_unit

