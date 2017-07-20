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
name|plan
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|lang
operator|.
name|ArrayUtils
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
name|vector
operator|.
name|ColumnVector
operator|.
name|Type
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorBase
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorCount
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorCountStar
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalAvg
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalFirstValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalLastValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalMax
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalMin
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDecimalSum
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDenseRank
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleAvg
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleFirstValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleLastValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleMax
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleMin
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorDoubleSum
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongAvg
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongFirstValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongLastValue
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongMax
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongMin
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorLongSum
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorRank
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorRowNumber
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
name|ptf
operator|.
name|WindowFrameDef
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * VectorPTFDesc.  *  * Extra parameters beyond PTFDesc just for the VectorPTFOperator.  *  * We don't extend PTFDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorPTFDesc
extends|extends
name|AbstractVectorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|SupportedFunctionType
block|{
name|ROW_NUMBER
block|,
name|RANK
block|,
name|DENSE_RANK
block|,
name|MIN
block|,
name|MAX
block|,
name|SUM
block|,
name|AVG
block|,
name|FIRST_VALUE
block|,
name|LAST_VALUE
block|,
name|COUNT
block|}
specifier|public
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|SupportedFunctionType
argument_list|>
name|supportedFunctionsMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SupportedFunctionType
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"row_number"
argument_list|,
name|SupportedFunctionType
operator|.
name|ROW_NUMBER
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"rank"
argument_list|,
name|SupportedFunctionType
operator|.
name|RANK
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"dense_rank"
argument_list|,
name|SupportedFunctionType
operator|.
name|DENSE_RANK
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"min"
argument_list|,
name|SupportedFunctionType
operator|.
name|MIN
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"max"
argument_list|,
name|SupportedFunctionType
operator|.
name|MAX
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"sum"
argument_list|,
name|SupportedFunctionType
operator|.
name|SUM
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"avg"
argument_list|,
name|SupportedFunctionType
operator|.
name|AVG
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"first_value"
argument_list|,
name|SupportedFunctionType
operator|.
name|FIRST_VALUE
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"last_value"
argument_list|,
name|SupportedFunctionType
operator|.
name|LAST_VALUE
argument_list|)
expr_stmt|;
name|supportedFunctionsMap
operator|.
name|put
argument_list|(
literal|"count"
argument_list|,
name|SupportedFunctionType
operator|.
name|COUNT
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|supportedFunctionNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|TreeSet
argument_list|<
name|String
argument_list|>
name|treeSet
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|treeSet
operator|.
name|addAll
argument_list|(
name|supportedFunctionsMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|supportedFunctionNames
operator|.
name|addAll
argument_list|(
name|treeSet
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isPartitionOrderBy
decl_stmt|;
specifier|private
name|String
index|[]
name|evaluatorFunctionNames
decl_stmt|;
specifier|private
name|WindowFrameDef
index|[]
name|evaluatorWindowFrameDefs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
index|[]
name|evaluatorInputExprNodeDescLists
decl_stmt|;
specifier|private
name|ExprNodeDesc
index|[]
name|orderExprNodeDescs
decl_stmt|;
specifier|private
name|ExprNodeDesc
index|[]
name|partitionExprNodeDescs
decl_stmt|;
specifier|private
name|String
index|[]
name|outputColumnNames
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|outputTypeInfos
decl_stmt|;
specifier|private
name|VectorPTFInfo
name|vectorPTFInfo
decl_stmt|;
specifier|public
name|VectorPTFDesc
parameter_list|()
block|{
name|isPartitionOrderBy
operator|=
literal|false
expr_stmt|;
name|evaluatorFunctionNames
operator|=
literal|null
expr_stmt|;
name|evaluatorInputExprNodeDescLists
operator|=
literal|null
expr_stmt|;
name|orderExprNodeDescs
operator|=
literal|null
expr_stmt|;
name|partitionExprNodeDescs
operator|=
literal|null
expr_stmt|;
name|outputColumnNames
operator|=
literal|null
expr_stmt|;
name|outputTypeInfos
operator|=
literal|null
expr_stmt|;
block|}
comment|// We provide this public method to help EXPLAIN VECTORIZATION show the evaluator classes.
specifier|public
specifier|static
name|VectorPTFEvaluatorBase
name|getEvaluator
parameter_list|(
name|SupportedFunctionType
name|functionType
parameter_list|,
name|WindowFrameDef
name|windowFrameDef
parameter_list|,
name|Type
name|columnVectorType
parameter_list|,
name|VectorExpression
name|inputVectorExpression
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|VectorPTFEvaluatorBase
name|evaluator
decl_stmt|;
switch|switch
condition|(
name|functionType
condition|)
block|{
case|case
name|ROW_NUMBER
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorRowNumber
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|RANK
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorRank
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DENSE_RANK
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDenseRank
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|MIN
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongMin
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleMin
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalMin
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|MAX
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongMax
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleMax
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalMax
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|SUM
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongSum
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleSum
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalSum
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|AVG
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongAvg
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleAvg
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalAvg
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|FIRST_VALUE
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongFirstValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleFirstValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalFirstValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|LAST_VALUE
case|:
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorLongLastValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDoubleLastValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorDecimalLastValue
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
operator|+
literal|" for "
operator|+
name|functionType
argument_list|)
throw|;
block|}
break|break;
case|case
name|COUNT
case|:
if|if
condition|(
name|inputVectorExpression
operator|==
literal|null
condition|)
block|{
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorCountStar
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|evaluator
operator|=
operator|new
name|VectorPTFEvaluatorCount
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected function type "
operator|+
name|functionType
argument_list|)
throw|;
block|}
return|return
name|evaluator
return|;
block|}
specifier|public
specifier|static
name|VectorPTFEvaluatorBase
index|[]
name|getEvaluators
parameter_list|(
name|VectorPTFDesc
name|vectorPTFDesc
parameter_list|,
name|VectorPTFInfo
name|vectorPTFInfo
parameter_list|)
block|{
name|String
index|[]
name|evaluatorFunctionNames
init|=
name|vectorPTFDesc
operator|.
name|getEvaluatorFunctionNames
argument_list|()
decl_stmt|;
name|int
name|evaluatorCount
init|=
name|evaluatorFunctionNames
operator|.
name|length
decl_stmt|;
name|WindowFrameDef
index|[]
name|evaluatorWindowFrameDefs
init|=
name|vectorPTFDesc
operator|.
name|getEvaluatorWindowFrameDefs
argument_list|()
decl_stmt|;
name|VectorExpression
index|[]
name|evaluatorInputExpressions
init|=
name|vectorPTFInfo
operator|.
name|getEvaluatorInputExpressions
argument_list|()
decl_stmt|;
name|Type
index|[]
name|evaluatorInputColumnVectorTypes
init|=
name|vectorPTFInfo
operator|.
name|getEvaluatorInputColumnVectorTypes
argument_list|()
decl_stmt|;
name|int
index|[]
name|outputColumnMap
init|=
name|vectorPTFInfo
operator|.
name|getOutputColumnMap
argument_list|()
decl_stmt|;
name|VectorPTFEvaluatorBase
index|[]
name|evaluators
init|=
operator|new
name|VectorPTFEvaluatorBase
index|[
name|evaluatorCount
index|]
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
name|evaluatorCount
condition|;
name|i
operator|++
control|)
block|{
name|String
name|functionName
init|=
name|evaluatorFunctionNames
index|[
name|i
index|]
decl_stmt|;
name|WindowFrameDef
name|windowFrameDef
init|=
name|evaluatorWindowFrameDefs
index|[
name|i
index|]
decl_stmt|;
name|SupportedFunctionType
name|functionType
init|=
name|VectorPTFDesc
operator|.
name|supportedFunctionsMap
operator|.
name|get
argument_list|(
name|functionName
argument_list|)
decl_stmt|;
name|VectorExpression
name|inputVectorExpression
init|=
name|evaluatorInputExpressions
index|[
name|i
index|]
decl_stmt|;
specifier|final
name|Type
name|columnVectorType
init|=
name|evaluatorInputColumnVectorTypes
index|[
name|i
index|]
decl_stmt|;
comment|// The output* arrays start at index 0 for output evaluator aggregations.
specifier|final
name|int
name|outputColumnNum
init|=
name|outputColumnMap
index|[
name|i
index|]
decl_stmt|;
name|VectorPTFEvaluatorBase
name|evaluator
init|=
name|VectorPTFDesc
operator|.
name|getEvaluator
argument_list|(
name|functionType
argument_list|,
name|windowFrameDef
argument_list|,
name|columnVectorType
argument_list|,
name|inputVectorExpression
argument_list|,
name|outputColumnNum
argument_list|)
decl_stmt|;
name|evaluators
index|[
name|i
index|]
operator|=
name|evaluator
expr_stmt|;
block|}
return|return
name|evaluators
return|;
block|}
specifier|public
specifier|static
name|int
index|[]
name|getStreamingColumnMap
parameter_list|(
name|VectorPTFEvaluatorBase
index|[]
name|evaluators
parameter_list|)
block|{
specifier|final
name|int
name|evaluatorCount
init|=
name|evaluators
operator|.
name|length
decl_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|streamingColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
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
name|evaluatorCount
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|VectorPTFEvaluatorBase
name|evaluator
init|=
name|evaluators
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|evaluator
operator|.
name|streamsResult
argument_list|()
condition|)
block|{
name|streamingColumns
operator|.
name|add
argument_list|(
name|evaluator
operator|.
name|getOutputColumnNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ArrayUtils
operator|.
name|toPrimitive
argument_list|(
name|streamingColumns
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getIsPartitionOrderBy
parameter_list|()
block|{
return|return
name|isPartitionOrderBy
return|;
block|}
specifier|public
name|void
name|setIsPartitionOrderBy
parameter_list|(
name|boolean
name|isPartitionOrderBy
parameter_list|)
block|{
name|this
operator|.
name|isPartitionOrderBy
operator|=
name|isPartitionOrderBy
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getEvaluatorFunctionNames
parameter_list|()
block|{
return|return
name|evaluatorFunctionNames
return|;
block|}
specifier|public
name|void
name|setEvaluatorFunctionNames
parameter_list|(
name|String
index|[]
name|evaluatorFunctionNames
parameter_list|)
block|{
name|this
operator|.
name|evaluatorFunctionNames
operator|=
name|evaluatorFunctionNames
expr_stmt|;
block|}
specifier|public
name|WindowFrameDef
index|[]
name|getEvaluatorWindowFrameDefs
parameter_list|()
block|{
return|return
name|evaluatorWindowFrameDefs
return|;
block|}
specifier|public
name|void
name|setEvaluatorWindowFrameDefs
parameter_list|(
name|WindowFrameDef
index|[]
name|evaluatorWindowFrameDefs
parameter_list|)
block|{
name|this
operator|.
name|evaluatorWindowFrameDefs
operator|=
name|evaluatorWindowFrameDefs
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
index|[]
name|getEvaluatorInputExprNodeDescLists
parameter_list|()
block|{
return|return
name|evaluatorInputExprNodeDescLists
return|;
block|}
specifier|public
name|void
name|setEvaluatorInputExprNodeDescLists
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
index|[]
name|evaluatorInputExprNodeDescLists
parameter_list|)
block|{
name|this
operator|.
name|evaluatorInputExprNodeDescLists
operator|=
name|evaluatorInputExprNodeDescLists
expr_stmt|;
block|}
specifier|public
name|ExprNodeDesc
index|[]
name|getOrderExprNodeDescs
parameter_list|()
block|{
return|return
name|orderExprNodeDescs
return|;
block|}
specifier|public
name|void
name|setOrderExprNodeDescs
parameter_list|(
name|ExprNodeDesc
index|[]
name|orderExprNodeDescs
parameter_list|)
block|{
name|this
operator|.
name|orderExprNodeDescs
operator|=
name|orderExprNodeDescs
expr_stmt|;
block|}
specifier|public
name|ExprNodeDesc
index|[]
name|getPartitionExprNodeDescs
parameter_list|()
block|{
return|return
name|partitionExprNodeDescs
return|;
block|}
specifier|public
name|void
name|setPartitionExprNodeDescs
parameter_list|(
name|ExprNodeDesc
index|[]
name|partitionExprNodeDescs
parameter_list|)
block|{
name|this
operator|.
name|partitionExprNodeDescs
operator|=
name|partitionExprNodeDescs
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
specifier|public
name|void
name|setOutputColumnNames
parameter_list|(
name|String
index|[]
name|outputColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getOutputTypeInfos
parameter_list|()
block|{
return|return
name|outputTypeInfos
return|;
block|}
specifier|public
name|void
name|setOutputTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|outputTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|outputTypeInfos
operator|=
name|outputTypeInfos
expr_stmt|;
block|}
specifier|public
name|void
name|setVectorPTFInfo
parameter_list|(
name|VectorPTFInfo
name|vectorPTFInfo
parameter_list|)
block|{
name|this
operator|.
name|vectorPTFInfo
operator|=
name|vectorPTFInfo
expr_stmt|;
block|}
specifier|public
name|VectorPTFInfo
name|getVectorPTFInfo
parameter_list|()
block|{
return|return
name|vectorPTFInfo
return|;
block|}
block|}
end_class

end_unit

