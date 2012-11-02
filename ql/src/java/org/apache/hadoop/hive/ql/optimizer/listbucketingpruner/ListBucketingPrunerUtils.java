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
operator|.
name|listbucketingpruner
package|;
end_package

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
name|metadata
operator|.
name|Partition
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqual
import|;
end_import

begin_comment
comment|/**  * Utility for list bucketing prune.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ListBucketingPrunerUtils
block|{
comment|/**    * Decide if pruner skips the skewed directory    * Input: if the skewed value matches the expression tree    * Ouput: if pruner should skip the directory represented by the skewed value    * If match result is unknown(null) or true, pruner doesn't skip the directory    * If match result is false, pruner skips the dir.    * @param bool    *          if the skewed value matches the expression tree    * @return    */
specifier|public
specifier|static
name|boolean
name|skipSkewedDirectory
parameter_list|(
name|Boolean
name|bool
parameter_list|)
block|{
if|if
condition|(
name|bool
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
name|bool
operator|.
name|booleanValue
argument_list|()
return|;
block|}
comment|/**    * or 2 Boolean operands in the context of pruning match    *    * Operand one|Operand another | or result    * unknown | T | T    * unknown | F | unknown    * unknown | unknown | unknown    * T | T | T    * T | F | T    * T | unknown | unknown    * F | T | T    * F | F | F    * F | unknown | unknown    */
specifier|public
specifier|static
name|Boolean
name|orBoolOperand
parameter_list|(
name|Boolean
name|o
parameter_list|,
name|Boolean
name|a
parameter_list|)
block|{
comment|// pick up unknown case
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|a
operator|==
literal|null
operator|)
operator|||
operator|!
name|a
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|a
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
name|o
operator|||
name|a
operator|)
return|;
block|}
comment|/**    * And 2 Boolean operands in the context of pruning match    *    * Operand one|Operand another | And result    * unknown | T | unknown    * unknown | F | F    * unknown | unknown | unknown    * T | T | T    * T | F | F    * T | unknown | unknown    * F | T | F    * F | F | F    * F | unknown | F    * @param o    *          one operand    * @param a    *          another operand    * @return result    */
specifier|public
specifier|static
name|Boolean
name|andBoolOperand
parameter_list|(
name|Boolean
name|o
parameter_list|,
name|Boolean
name|a
parameter_list|)
block|{
comment|// pick up unknown case and let and operator handle the rest
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|a
operator|==
literal|null
operator|)
operator|||
name|a
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|a
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
return|return
name|o
condition|?
literal|null
else|:
name|Boolean
operator|.
name|FALSE
return|;
block|}
return|return
operator|(
name|o
operator|&&
name|a
operator|)
return|;
block|}
comment|/**    * Not a Boolean operand in the context of pruning match    *    * Operand | Not    * T | F    * F | T    * unknown | unknown    * @param input    *          match result    * @return    */
specifier|public
specifier|static
name|Boolean
name|notBoolOperand
parameter_list|(
name|Boolean
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|input
condition|?
name|Boolean
operator|.
name|FALSE
else|:
name|Boolean
operator|.
name|TRUE
return|;
block|}
comment|/**    * 1. Walk through the tree to decide value    * 1.1 true means the element matches the expression tree    * 1.2 false means the element doesn't match the expression tree    * 1.3 unknown means not sure if the element matches the expression tree    *    * Example:    * skewed column: C1, C2    * cell: (1,a) , (1,b) , (1,c) , (1,other), (2,a), (2,b) , (2,c), (2,other), (other,a), (other,b),    * (other,c), (other,other)    *    * * Expression Tree : ((c1=1) and (c2=a)) or ( (c1=3) or (c2=b))    *    * or    * / \    * and or    * / \ / \    * c1=1 c2=a c1=3 c2=b    * @throws SemanticException    *    */
specifier|static
name|Boolean
name|evaluateExprOnCell
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
name|ExprNodeDesc
name|pruner
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|recursiveExpr
argument_list|(
name|pruner
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
return|;
block|}
comment|/**    * Walk through expression tree recursively to evaluate.    *    *    * @param node    * @param skewedCols    * @param cell    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|Boolean
name|recursiveExpr
parameter_list|(
specifier|final
name|ExprNodeDesc
name|node
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|isUnknownState
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|node
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|node
operator|)
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFOPEqual
condition|)
block|{
return|return
name|evaluateEqualNd
argument_list|(
name|node
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
name|evaluateAndNode
argument_list|(
name|node
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpOr
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
name|evaluateOrNode
argument_list|(
name|node
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpNot
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
name|evaluateNotNode
argument_list|(
name|node
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Evaluate equal node.    *    *    * @param node    * @param skewedCols    * @param cell    * @param uniqSkewedValues    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|Boolean
name|evaluateEqualNd
parameter_list|(
specifier|final
name|ExprNodeDesc
name|node
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Boolean
name|result
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|node
operator|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
assert|assert
operator|(
operator|(
name|children
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|children
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|)
operator|)
operator|:
literal|"GenericUDFOPEqual should have 2 "
operator|+
literal|"ExprNodeDesc. Node name : "
operator|+
name|node
operator|.
name|getName
argument_list|()
assert|;
name|ExprNodeDesc
name|left
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|right
init|=
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|left
operator|instanceof
name|ExprNodeColumnDesc
operator|&&
name|right
operator|instanceof
name|ExprNodeConstantDesc
operator|)
operator|:
literal|"GenericUDFOPEqual should have 2 children: "
operator|+
literal|" the first is ExprNodeColumnDesc and the second is ExprNodeConstantDesc. "
operator|+
literal|"But this one, the first one is "
operator|+
name|left
operator|.
name|getName
argument_list|()
operator|+
literal|" and the second is "
operator|+
name|right
operator|.
name|getName
argument_list|()
assert|;
name|result
operator|=
name|startComparisonInEqualNode
argument_list|(
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|,
name|result
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Comparison in equal node    *    * @param skewedCols    * @param cell    * @param uniqSkewedValues    * @param result    * @param left    * @param right    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|Boolean
name|startComparisonInEqualNode
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|,
name|Boolean
name|result
parameter_list|,
name|ExprNodeDesc
name|left
parameter_list|,
name|ExprNodeDesc
name|right
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|columnNameInFilter
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|left
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|String
name|constantValueInFilter
init|=
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|right
operator|)
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|skewedCols
operator|.
name|contains
argument_list|(
name|columnNameInFilter
argument_list|)
operator|)
operator|:
literal|"List bucketing pruner has a column name "
operator|+
name|columnNameInFilter
operator|+
literal|" which is not found in the partiton's skewed column list"
assert|;
name|int
name|index
init|=
name|skewedCols
operator|.
name|indexOf
argument_list|(
name|columnNameInFilter
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|index
operator|<
name|cell
operator|.
name|size
argument_list|()
operator|)
operator|:
literal|"GenericUDFOPEqual has a ExprNodeColumnDesc ("
operator|+
name|columnNameInFilter
operator|+
literal|") which is "
operator|+
name|index
operator|+
literal|"th"
operator|+
literal|"skewed column. "
operator|+
literal|" But it can't find the matching part in cell."
operator|+
literal|" Because the cell size is "
operator|+
name|cell
operator|.
name|size
argument_list|()
assert|;
name|String
name|cellValueInPosition
init|=
name|cell
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|index
operator|<
name|uniqSkewedValues
operator|.
name|size
argument_list|()
operator|)
operator|:
literal|"GenericUDFOPEqual has a ExprNodeColumnDesc ("
operator|+
name|columnNameInFilter
operator|+
literal|") which is "
operator|+
name|index
operator|+
literal|"th"
operator|+
literal|"skewed column. "
operator|+
literal|" But it can't find the matching part in uniq skewed value list."
operator|+
literal|" Because the cell size is "
operator|+
name|uniqSkewedValues
operator|.
name|size
argument_list|()
assert|;
name|List
argument_list|<
name|String
argument_list|>
name|uniqSkewedValuesInPosition
init|=
name|uniqSkewedValues
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|result
operator|=
name|coreComparisonInEqualNode
argument_list|(
name|constantValueInFilter
argument_list|,
name|cellValueInPosition
argument_list|,
name|uniqSkewedValuesInPosition
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Compare    * @param constantValueInFilter    * @param cellValueInPosition    * @param uniqSkewedValuesInPosition    * @return    */
specifier|private
specifier|static
name|Boolean
name|coreComparisonInEqualNode
parameter_list|(
name|String
name|constantValueInFilter
parameter_list|,
name|String
name|cellValueInPosition
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|uniqSkewedValuesInPosition
parameter_list|)
block|{
name|Boolean
name|result
decl_stmt|;
comment|// Compare cell value with constant value in filter
comment|// 1 if they match and cell value isn't other, return true
comment|// 2 if they don't match but cell is other and value in filter is not skewed value,
comment|//   return unknown. why not true? true is not enough. since not true is false,
comment|//   but not unknown is unknown.
comment|//   For example, skewed column C, skewed value 1, 2. clause: where not ( c =3)
comment|//   cell is other, evaluate (not(c=3)).
comment|//   other to (c=3), if ture. not(c=3) will be false. but it is wrong skip default dir
comment|//   but, if unknown. not(c=3) will be unknown. we will choose default dir.
comment|// 3 all others, return false
if|if
condition|(
name|cellValueInPosition
operator|.
name|equals
argument_list|(
name|constantValueInFilter
argument_list|)
operator|&&
operator|!
name|cellValueInPosition
operator|.
name|equals
argument_list|(
name|ListBucketingPruner
operator|.
name|DEFAULT_SKEWED_KEY
argument_list|)
condition|)
block|{
name|result
operator|=
name|Boolean
operator|.
name|TRUE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cellValueInPosition
operator|.
name|equals
argument_list|(
name|ListBucketingPruner
operator|.
name|DEFAULT_SKEWED_KEY
argument_list|)
operator|&&
operator|!
name|uniqSkewedValuesInPosition
operator|.
name|contains
argument_list|(
name|constantValueInFilter
argument_list|)
condition|)
block|{
name|result
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|Boolean
operator|.
name|FALSE
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|Boolean
name|evaluateNotNode
parameter_list|(
specifier|final
name|ExprNodeDesc
name|node
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|node
operator|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|children
operator|==
literal|null
operator|)
operator|||
operator|(
name|children
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"GenericUDFOPNot should have 1 ExprNodeDesc. Node name : "
operator|+
name|node
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|ExprNodeDesc
name|child
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|notBoolOperand
argument_list|(
name|recursiveExpr
argument_list|(
name|child
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Boolean
name|evaluateOrNode
parameter_list|(
specifier|final
name|ExprNodeDesc
name|node
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|node
operator|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|children
operator|==
literal|null
operator|)
operator|||
operator|(
name|children
operator|.
name|size
argument_list|()
operator|!=
literal|2
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"GenericUDFOPOr should have 2 ExprNodeDesc. Node name : "
operator|+
name|node
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|ExprNodeDesc
name|left
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|right
init|=
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
name|orBoolOperand
argument_list|(
name|recursiveExpr
argument_list|(
name|left
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
argument_list|,
name|recursiveExpr
argument_list|(
name|right
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Boolean
name|evaluateAndNode
parameter_list|(
specifier|final
name|ExprNodeDesc
name|node
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|cell
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|node
operator|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|children
operator|==
literal|null
operator|)
operator|||
operator|(
name|children
operator|.
name|size
argument_list|()
operator|!=
literal|2
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"GenericUDFOPAnd should have 2 ExprNodeDesc. Node name : "
operator|+
name|node
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|ExprNodeDesc
name|left
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|right
init|=
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
name|andBoolOperand
argument_list|(
name|recursiveExpr
argument_list|(
name|left
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
argument_list|,
name|recursiveExpr
argument_list|(
name|right
argument_list|,
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|uniqSkewedValues
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check if the node is unknown    *    *    * unknown is marked in {@link #transform(ParseContext)}<blockquote>    *    *<pre>    * newcd = new ExprNodeConstantDesc(cd.getTypeInfo(), null)    *</pre>    *    * like    *    * 1. non-skewed column    *    * 2. non and/or/not ...    *    *    * @param descNd    * @return    */
specifier|static
name|boolean
name|isUnknownState
parameter_list|(
name|ExprNodeDesc
name|descNd
parameter_list|)
block|{
name|boolean
name|unknown
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|(
name|descNd
operator|==
literal|null
operator|)
operator|||
operator|(
name|descNd
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|descNd
operator|)
operator|.
name|getValue
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
name|unknown
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|unknown
return|;
block|}
comment|/**    * check if the partition is list bucketing    *    * @param part    * @return    */
specifier|public
specifier|static
name|boolean
name|isListBucketingPart
parameter_list|(
name|Partition
name|part
parameter_list|)
block|{
return|return
operator|(
name|part
operator|.
name|getSkewedColNames
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColValues
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColValues
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColValueLocationMaps
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColValueLocationMaps
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit

