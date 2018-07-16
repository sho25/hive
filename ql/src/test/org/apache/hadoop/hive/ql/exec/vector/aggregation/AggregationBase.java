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
name|exec
operator|.
name|vector
operator|.
name|aggregation
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|ImmutablePair
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
name|common
operator|.
name|type
operator|.
name|DataTypePhysicalVariation
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
name|vector
operator|.
name|LongColumnVector
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
name|VectorAggregationBufferRow
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
name|VectorAggregationDesc
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
name|VectorExtractRow
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
name|VectorRandomBatchSource
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
name|VectorRandomRowSource
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
name|VectorizationContext
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
name|VectorizedRowBatch
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
name|VectorizedRowBatchCtx
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
name|expressions
operator|.
name|aggregates
operator|.
name|VectorAggregateExpression
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
name|HiveException
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
name|optimizer
operator|.
name|physical
operator|.
name|Vectorizer
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
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
name|GenericUDAFResolver
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
name|GenericUDAFEvaluator
operator|.
name|AggregationBuffer
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
name|io
operator|.
name|ShortWritable
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|PrimitiveTypeInfo
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
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_class
specifier|public
class|class
name|AggregationBase
block|{
specifier|public
enum|enum
name|AggregationTestMode
block|{
name|ROW_MODE
block|,
name|VECTOR_EXPRESSION
block|;
specifier|static
specifier|final
name|int
name|count
init|=
name|values
argument_list|()
operator|.
name|length
decl_stmt|;
block|}
specifier|public
specifier|static
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|String
name|aggregationFunctionName
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenericUDAFResolver
name|resolver
init|=
name|FunctionRegistry
operator|.
name|getGenericUDAFResolver
argument_list|(
name|aggregationFunctionName
argument_list|)
decl_stmt|;
name|TypeInfo
index|[]
name|parameters
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|typeInfo
block|}
decl_stmt|;
name|GenericUDAFEvaluator
name|evaluator
init|=
name|resolver
operator|.
name|getEvaluator
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
return|return
name|evaluator
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|doRowTest
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|GenericUDAFEvaluator
name|evaluator
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|GenericUDAFEvaluator
operator|.
name|Mode
name|udafEvaluatorMode
parameter_list|,
name|int
name|maxKeyCount
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|Exception
block|{
comment|/*     System.out.println(         "*DEBUG* typeInfo " + typeInfo.toString() +         " aggregationTestMode ROW_MODE" +         " outputTypeInfo " + outputTypeInfo.toString());     */
comment|// Last entry is for a NULL key.
name|AggregationBuffer
index|[]
name|aggregationBuffers
init|=
operator|new
name|AggregationBuffer
index|[
name|maxKeyCount
operator|+
literal|1
index|]
decl_stmt|;
name|ObjectInspector
name|objectInspector
init|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|outputTypeInfo
argument_list|)
decl_stmt|;
name|Object
index|[]
name|parameterArray
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|int
name|rowCount
init|=
name|randomRows
operator|.
name|length
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
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|randomRows
index|[
name|i
index|]
decl_stmt|;
name|ShortWritable
name|shortWritable
init|=
operator|(
name|ShortWritable
operator|)
name|row
index|[
literal|0
index|]
decl_stmt|;
specifier|final
name|int
name|key
decl_stmt|;
if|if
condition|(
name|shortWritable
operator|==
literal|null
condition|)
block|{
name|key
operator|=
name|maxKeyCount
expr_stmt|;
block|}
else|else
block|{
name|key
operator|=
name|shortWritable
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|AggregationBuffer
name|aggregationBuffer
init|=
name|aggregationBuffers
index|[
name|key
index|]
decl_stmt|;
if|if
condition|(
name|aggregationBuffer
operator|==
literal|null
condition|)
block|{
name|aggregationBuffer
operator|=
name|evaluator
operator|.
name|getNewAggregationBuffer
argument_list|()
expr_stmt|;
name|aggregationBuffers
index|[
name|key
index|]
operator|=
name|aggregationBuffer
expr_stmt|;
block|}
name|parameterArray
index|[
literal|0
index|]
operator|=
name|row
index|[
literal|1
index|]
expr_stmt|;
name|evaluator
operator|.
name|aggregate
argument_list|(
name|aggregationBuffer
argument_list|,
name|parameterArray
argument_list|)
expr_stmt|;
block|}
specifier|final
name|boolean
name|isPrimitive
init|=
operator|(
name|outputTypeInfo
operator|instanceof
name|PrimitiveTypeInfo
operator|)
decl_stmt|;
specifier|final
name|boolean
name|isPartial
init|=
operator|(
name|udafEvaluatorMode
operator|==
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|PARTIAL1
operator|||
name|udafEvaluatorMode
operator|==
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|PARTIAL2
operator|)
decl_stmt|;
for|for
control|(
name|short
name|key
init|=
literal|0
init|;
name|key
operator|<
name|maxKeyCount
operator|+
literal|1
condition|;
name|key
operator|++
control|)
block|{
name|AggregationBuffer
name|aggregationBuffer
init|=
name|aggregationBuffers
index|[
name|key
index|]
decl_stmt|;
if|if
condition|(
name|aggregationBuffer
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Object
name|result
decl_stmt|;
if|if
condition|(
name|isPartial
condition|)
block|{
name|result
operator|=
name|evaluator
operator|.
name|terminatePartial
argument_list|(
name|aggregationBuffer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|evaluator
operator|.
name|terminate
argument_list|(
name|aggregationBuffer
argument_list|)
expr_stmt|;
block|}
name|Object
name|copyResult
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|copyResult
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isPrimitive
condition|)
block|{
name|copyResult
operator|=
name|VectorRandomRowSource
operator|.
name|getWritablePrimitiveObject
argument_list|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|outputTypeInfo
argument_list|,
name|objectInspector
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copyResult
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|result
argument_list|,
name|objectInspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
name|results
index|[
name|key
index|]
operator|=
name|copyResult
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|void
name|extractResultObjects
parameter_list|(
name|VectorizedRowBatch
name|outputBatch
parameter_list|,
name|short
index|[]
name|keys
parameter_list|,
name|VectorExtractRow
name|resultVectorExtractRow
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|Object
index|[]
name|scrqtchRow
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
block|{
specifier|final
name|boolean
name|isPrimitive
init|=
operator|(
name|outputTypeInfo
operator|instanceof
name|PrimitiveTypeInfo
operator|)
decl_stmt|;
name|ObjectInspector
name|objectInspector
decl_stmt|;
if|if
condition|(
name|isPrimitive
condition|)
block|{
name|objectInspector
operator|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|objectInspector
operator|=
literal|null
expr_stmt|;
block|}
for|for
control|(
name|int
name|batchIndex
init|=
literal|0
init|;
name|batchIndex
operator|<
name|outputBatch
operator|.
name|size
condition|;
name|batchIndex
operator|++
control|)
block|{
name|resultVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|outputBatch
argument_list|,
name|batchIndex
argument_list|,
name|scrqtchRow
argument_list|)
expr_stmt|;
if|if
condition|(
name|isPrimitive
condition|)
block|{
name|Object
name|copyResult
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|scrqtchRow
index|[
literal|0
index|]
argument_list|,
name|objectInspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|results
index|[
name|keys
index|[
name|batchIndex
index|]
index|]
operator|=
name|copyResult
expr_stmt|;
block|}
else|else
block|{
name|results
index|[
name|keys
index|[
name|batchIndex
index|]
index|]
operator|=
name|scrqtchRow
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|static
name|boolean
name|doVectorTest
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|GenericUDAFEvaluator
name|evaluator
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|GenericUDAFEvaluator
operator|.
name|Mode
name|udafEvaluatorMode
parameter_list|,
name|int
name|maxKeyCount
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|DataTypePhysicalVariation
index|[]
name|dataTypePhysicalVariations
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parameterList
parameter_list|,
name|VectorRandomBatchSource
name|batchSource
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|VectorizationContext
name|vectorizationContext
init|=
operator|new
name|VectorizationContext
argument_list|(
literal|"name"
argument_list|,
name|columns
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|typeInfos
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|dataTypePhysicalVariations
argument_list|)
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|ImmutablePair
argument_list|<
name|VectorAggregationDesc
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|Vectorizer
operator|.
name|getVectorAggregationDesc
argument_list|(
name|aggregationName
argument_list|,
name|parameterList
argument_list|,
name|evaluator
argument_list|,
name|outputTypeInfo
argument_list|,
name|udafEvaluatorMode
argument_list|,
name|vectorizationContext
argument_list|)
decl_stmt|;
name|VectorAggregationDesc
name|vecAggrDesc
init|=
name|pair
operator|.
name|left
decl_stmt|;
if|if
condition|(
name|vecAggrDesc
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"No vector aggregation expression found for aggregationName "
operator|+
name|aggregationName
operator|+
literal|" udafEvaluatorMode "
operator|+
name|udafEvaluatorMode
operator|+
literal|" parameterList "
operator|+
name|parameterList
operator|+
literal|" outputTypeInfo "
operator|+
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|VectorAggregateExpression
argument_list|>
name|vecAggrClass
init|=
name|vecAggrDesc
operator|.
name|getVecAggrClass
argument_list|()
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|VectorAggregateExpression
argument_list|>
name|ctor
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ctor
operator|=
name|vecAggrClass
operator|.
name|getConstructor
argument_list|(
name|VectorAggregationDesc
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Constructor "
operator|+
name|vecAggrClass
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"(VectorAggregationDesc) not available"
argument_list|)
throw|;
block|}
name|VectorAggregateExpression
name|vecAggrExpr
init|=
literal|null
decl_stmt|;
try|try
block|{
name|vecAggrExpr
operator|=
name|ctor
operator|.
name|newInstance
argument_list|(
name|vecAggrDesc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to create "
operator|+
name|vecAggrClass
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"(VectorAggregationDesc) object "
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|vecAggrExpr
operator|.
name|getInputExpression
argument_list|()
argument_list|)
expr_stmt|;
comment|/*     System.out.println(         "*DEBUG* typeInfo " + typeInfo.toString() +         " aggregationTestMode VECTOR_MODE" +         " vecAggrExpr " + vecAggrExpr.getClass().getSimpleName());     */
name|VectorRandomRowSource
name|rowSource
init|=
name|batchSource
operator|.
name|getRowSource
argument_list|()
decl_stmt|;
name|VectorizedRowBatchCtx
name|batchContext
init|=
operator|new
name|VectorizedRowBatchCtx
argument_list|(
name|columnNames
argument_list|,
name|rowSource
operator|.
name|typeInfos
argument_list|()
argument_list|,
name|rowSource
operator|.
name|dataTypePhysicalVariations
argument_list|()
argument_list|,
comment|/* dataColumnNums */
literal|null
argument_list|,
comment|/* partitionColumnCount */
literal|0
argument_list|,
comment|/* virtualColumnCount */
literal|0
argument_list|,
comment|/* neededVirtualColumns */
literal|null
argument_list|,
name|vectorizationContext
operator|.
name|getScratchColumnTypeNames
argument_list|()
argument_list|,
name|vectorizationContext
operator|.
name|getScratchDataTypePhysicalVariations
argument_list|()
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|batchContext
operator|.
name|createVectorizedRowBatch
argument_list|()
decl_stmt|;
comment|// Last entry is for a NULL key.
name|VectorAggregationBufferRow
index|[]
name|vectorAggregationBufferRows
init|=
operator|new
name|VectorAggregationBufferRow
index|[
name|maxKeyCount
operator|+
literal|1
index|]
decl_stmt|;
name|VectorAggregationBufferRow
index|[]
name|batchBufferRows
decl_stmt|;
name|batchSource
operator|.
name|resetBatchIteration
argument_list|()
expr_stmt|;
name|int
name|rowIndex
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|!
name|batchSource
operator|.
name|fillNextBatch
argument_list|(
name|batch
argument_list|)
condition|)
block|{
break|break;
block|}
name|LongColumnVector
name|keyLongColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|0
index|]
decl_stmt|;
name|batchBufferRows
operator|=
operator|new
name|VectorAggregationBufferRow
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
specifier|final
name|int
name|size
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|boolean
name|selectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
for|for
control|(
name|int
name|logical
init|=
literal|0
init|;
name|logical
operator|<
name|size
condition|;
name|logical
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logical
index|]
else|:
name|logical
operator|)
decl_stmt|;
specifier|final
name|int
name|keyAdjustedBatchIndex
decl_stmt|;
if|if
condition|(
name|keyLongColVector
operator|.
name|isRepeating
condition|)
block|{
name|keyAdjustedBatchIndex
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|keyAdjustedBatchIndex
operator|=
name|batchIndex
expr_stmt|;
block|}
specifier|final
name|short
name|key
decl_stmt|;
if|if
condition|(
name|keyLongColVector
operator|.
name|noNulls
operator|||
operator|!
name|keyLongColVector
operator|.
name|isNull
index|[
name|keyAdjustedBatchIndex
index|]
condition|)
block|{
name|key
operator|=
operator|(
name|short
operator|)
name|keyLongColVector
operator|.
name|vector
index|[
name|keyAdjustedBatchIndex
index|]
expr_stmt|;
block|}
else|else
block|{
name|key
operator|=
operator|(
name|short
operator|)
name|maxKeyCount
expr_stmt|;
block|}
name|VectorAggregationBufferRow
name|bufferRow
init|=
name|vectorAggregationBufferRows
index|[
name|key
index|]
decl_stmt|;
if|if
condition|(
name|bufferRow
operator|==
literal|null
condition|)
block|{
name|VectorAggregateExpression
operator|.
name|AggregationBuffer
name|aggregationBuffer
init|=
name|vecAggrExpr
operator|.
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
name|aggregationBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|VectorAggregateExpression
operator|.
name|AggregationBuffer
index|[]
name|aggregationBuffers
init|=
operator|new
name|VectorAggregateExpression
operator|.
name|AggregationBuffer
index|[]
block|{
name|aggregationBuffer
block|}
decl_stmt|;
name|bufferRow
operator|=
operator|new
name|VectorAggregationBufferRow
argument_list|(
name|aggregationBuffers
argument_list|)
expr_stmt|;
name|vectorAggregationBufferRows
index|[
name|key
index|]
operator|=
name|bufferRow
expr_stmt|;
block|}
name|batchBufferRows
index|[
name|logical
index|]
operator|=
name|bufferRow
expr_stmt|;
block|}
name|vecAggrExpr
operator|.
name|aggregateInputSelection
argument_list|(
name|batchBufferRows
argument_list|,
literal|0
argument_list|,
name|batch
argument_list|)
expr_stmt|;
name|rowIndex
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
name|String
index|[]
name|outputColumnNames
init|=
operator|new
name|String
index|[]
block|{
literal|"output"
block|}
decl_stmt|;
name|TypeInfo
index|[]
name|outputTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|outputTypeInfo
block|}
decl_stmt|;
name|VectorizedRowBatchCtx
name|outputBatchContext
init|=
operator|new
name|VectorizedRowBatchCtx
argument_list|(
name|outputColumnNames
argument_list|,
name|outputTypeInfos
argument_list|,
literal|null
argument_list|,
comment|/* dataColumnNums */
literal|null
argument_list|,
comment|/* partitionColumnCount */
literal|0
argument_list|,
comment|/* virtualColumnCount */
literal|0
argument_list|,
comment|/* neededVirtualColumns */
literal|null
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|,
operator|new
name|DataTypePhysicalVariation
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|outputBatch
init|=
name|outputBatchContext
operator|.
name|createVectorizedRowBatch
argument_list|()
decl_stmt|;
name|short
index|[]
name|keys
init|=
operator|new
name|short
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
name|VectorExtractRow
name|resultVectorExtractRow
init|=
operator|new
name|VectorExtractRow
argument_list|()
decl_stmt|;
name|resultVectorExtractRow
operator|.
name|init
argument_list|(
operator|new
name|TypeInfo
index|[]
block|{
name|outputTypeInfo
block|}
argument_list|,
operator|new
name|int
index|[]
block|{
literal|0
block|}
argument_list|)
expr_stmt|;
name|Object
index|[]
name|scrqtchRow
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
for|for
control|(
name|short
name|key
init|=
literal|0
init|;
name|key
operator|<
name|maxKeyCount
operator|+
literal|1
condition|;
name|key
operator|++
control|)
block|{
name|VectorAggregationBufferRow
name|vectorAggregationBufferRow
init|=
name|vectorAggregationBufferRows
index|[
name|key
index|]
decl_stmt|;
if|if
condition|(
name|vectorAggregationBufferRow
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|outputBatch
operator|.
name|size
operator|==
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|)
block|{
name|extractResultObjects
argument_list|(
name|outputBatch
argument_list|,
name|keys
argument_list|,
name|resultVectorExtractRow
argument_list|,
name|outputTypeInfo
argument_list|,
name|scrqtchRow
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|outputBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|keys
index|[
name|outputBatch
operator|.
name|size
index|]
operator|=
name|key
expr_stmt|;
name|VectorAggregateExpression
operator|.
name|AggregationBuffer
name|aggregationBuffer
init|=
name|vectorAggregationBufferRow
operator|.
name|getAggregationBuffer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|vecAggrExpr
operator|.
name|assignRowColumn
argument_list|(
name|outputBatch
argument_list|,
name|outputBatch
operator|.
name|size
operator|++
argument_list|,
literal|0
argument_list|,
name|aggregationBuffer
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|outputBatch
operator|.
name|size
operator|>
literal|0
condition|)
block|{
name|extractResultObjects
argument_list|(
name|outputBatch
argument_list|,
name|keys
argument_list|,
name|resultVectorExtractRow
argument_list|,
name|outputTypeInfo
argument_list|,
name|scrqtchRow
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|compareObjects
parameter_list|(
name|Object
name|object1
parameter_list|,
name|Object
name|object2
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
block|{
if|if
condition|(
name|typeInfo
operator|instanceof
name|PrimitiveTypeInfo
condition|)
block|{
return|return
name|VectorRandomRowSource
operator|.
name|getWritablePrimitiveObject
argument_list|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
argument_list|,
name|objectInspector
argument_list|,
name|object1
argument_list|)
operator|.
name|equals
argument_list|(
name|VectorRandomRowSource
operator|.
name|getWritablePrimitiveObject
argument_list|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
argument_list|,
name|objectInspector
argument_list|,
name|object2
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|object1
operator|.
name|equals
argument_list|(
name|object2
argument_list|)
return|;
block|}
block|}
specifier|protected
name|void
name|executeAggregationTests
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|GenericUDAFEvaluator
name|evaluator
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|GenericUDAFEvaluator
operator|.
name|Mode
name|udafEvaluatorMode
parameter_list|,
name|int
name|maxKeyCount
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parameters
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|VectorRandomRowSource
name|rowSource
parameter_list|,
name|VectorRandomBatchSource
name|batchSource
parameter_list|,
name|Object
index|[]
name|resultsArray
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|AggregationTestMode
operator|.
name|count
condition|;
name|i
operator|++
control|)
block|{
comment|// Last entry is for a NULL key.
name|Object
index|[]
name|results
init|=
operator|new
name|Object
index|[
name|maxKeyCount
operator|+
literal|1
index|]
decl_stmt|;
name|resultsArray
index|[
name|i
index|]
operator|=
name|results
expr_stmt|;
name|AggregationTestMode
name|aggregationTestMode
init|=
name|AggregationTestMode
operator|.
name|values
argument_list|()
index|[
name|i
index|]
decl_stmt|;
switch|switch
condition|(
name|aggregationTestMode
condition|)
block|{
case|case
name|ROW_MODE
case|:
if|if
condition|(
operator|!
name|doRowTest
argument_list|(
name|typeInfo
argument_list|,
name|evaluator
argument_list|,
name|outputTypeInfo
argument_list|,
name|udafEvaluatorMode
argument_list|,
name|maxKeyCount
argument_list|,
name|columns
argument_list|,
name|parameters
argument_list|,
name|randomRows
argument_list|,
name|rowSource
operator|.
name|rowStructObjectInspector
argument_list|()
argument_list|,
name|results
argument_list|)
condition|)
block|{
return|return;
block|}
break|break;
case|case
name|VECTOR_EXPRESSION
case|:
if|if
condition|(
operator|!
name|doVectorTest
argument_list|(
name|aggregationName
argument_list|,
name|typeInfo
argument_list|,
name|evaluator
argument_list|,
name|outputTypeInfo
argument_list|,
name|udafEvaluatorMode
argument_list|,
name|maxKeyCount
argument_list|,
name|columns
argument_list|,
name|columnNames
argument_list|,
name|rowSource
operator|.
name|typeInfos
argument_list|()
argument_list|,
name|rowSource
operator|.
name|dataTypePhysicalVariations
argument_list|()
argument_list|,
name|parameters
argument_list|,
name|batchSource
argument_list|,
name|results
argument_list|)
condition|)
block|{
return|return;
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected Hash Aggregation test mode "
operator|+
name|aggregationTestMode
argument_list|)
throw|;
block|}
block|}
block|}
specifier|protected
name|void
name|verifyAggregationResults
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|int
name|maxKeyCount
parameter_list|,
name|GenericUDAFEvaluator
operator|.
name|Mode
name|udafEvaluatorMode
parameter_list|,
name|Object
index|[]
name|resultsArray
parameter_list|)
block|{
comment|// Row-mode is the expected results.
name|Object
index|[]
name|expectedResults
init|=
operator|(
name|Object
index|[]
operator|)
name|resultsArray
index|[
literal|0
index|]
decl_stmt|;
name|ObjectInspector
name|objectInspector
init|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|outputTypeInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|v
init|=
literal|1
init|;
name|v
operator|<
name|AggregationTestMode
operator|.
name|count
condition|;
name|v
operator|++
control|)
block|{
name|Object
index|[]
name|vectorResults
init|=
operator|(
name|Object
index|[]
operator|)
name|resultsArray
index|[
name|v
index|]
decl_stmt|;
for|for
control|(
name|short
name|key
init|=
literal|0
init|;
name|key
operator|<
name|maxKeyCount
operator|+
literal|1
condition|;
name|key
operator|++
control|)
block|{
name|Object
name|expectedResult
init|=
name|expectedResults
index|[
name|key
index|]
decl_stmt|;
name|Object
name|vectorResult
init|=
name|vectorResults
index|[
name|key
index|]
decl_stmt|;
if|if
condition|(
name|expectedResult
operator|==
literal|null
operator|||
name|vectorResult
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|expectedResult
operator|!=
literal|null
operator|||
name|vectorResult
operator|!=
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Key "
operator|+
name|key
operator|+
literal|" typeName "
operator|+
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" outputTypeName "
operator|+
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" "
operator|+
name|AggregationTestMode
operator|.
name|values
argument_list|()
index|[
name|v
index|]
operator|+
literal|" result is NULL "
operator|+
operator|(
name|vectorResult
operator|==
literal|null
condition|?
literal|"YES"
else|:
literal|"NO result "
operator|+
name|vectorResult
operator|.
name|toString
argument_list|()
operator|)
operator|+
literal|" does not match row-mode expected result is NULL "
operator|+
operator|(
name|expectedResult
operator|==
literal|null
condition|?
literal|"YES"
else|:
literal|"NO result "
operator|+
name|expectedResult
operator|.
name|toString
argument_list|()
operator|)
operator|+
literal|" udafEvaluatorMode "
operator|+
name|udafEvaluatorMode
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|compareObjects
argument_list|(
name|expectedResult
argument_list|,
name|vectorResult
argument_list|,
name|outputTypeInfo
argument_list|,
name|objectInspector
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Key "
operator|+
name|key
operator|+
literal|" typeName "
operator|+
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" outputTypeName "
operator|+
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" "
operator|+
name|AggregationTestMode
operator|.
name|values
argument_list|()
index|[
name|v
index|]
operator|+
literal|" result "
operator|+
name|vectorResult
operator|.
name|toString
argument_list|()
operator|+
literal|" ("
operator|+
name|vectorResult
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|")"
operator|+
literal|" does not match row-mode expected result "
operator|+
name|expectedResult
operator|.
name|toString
argument_list|()
operator|+
literal|" ("
operator|+
name|expectedResult
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|")"
operator|+
literal|" udafEvaluatorMode "
operator|+
name|udafEvaluatorMode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

