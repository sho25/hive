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
name|exec
operator|.
name|vector
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
name|Map
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
name|conf
operator|.
name|Configuration
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
name|ExprNodeEvaluator
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
name|MapJoinOperator
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
name|Utilities
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
name|persistence
operator|.
name|MapJoinKey
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
name|VectorExpressionWriter
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
name|VectorExpressionWriterFactory
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
name|MapJoinDesc
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
name|api
operator|.
name|OperatorType
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

begin_comment
comment|/**  * The vectorized version of the MapJoinOperator.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOperator
extends|extends
name|MapJoinOperator
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
name|VectorMapJoinOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    *    */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Vectorizaiton context key    * Used to retrieve column map from the MapTask scratch    */
specifier|private
name|String
name|fileKey
decl_stmt|;
specifier|private
name|int
name|tagLen
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|keyExpressions
decl_stmt|;
specifier|private
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
decl_stmt|;
specifier|private
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|bigTableFilterExpressions
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|bigTableValueExpressions
decl_stmt|;
specifier|private
specifier|transient
name|VectorizedRowBatch
name|outputBatch
decl_stmt|;
specifier|private
specifier|transient
name|MapJoinKeyEvaluator
name|keyEvaluator
decl_stmt|;
specifier|private
specifier|transient
name|VectorExpressionWriter
index|[]
name|valueWriters
decl_stmt|;
specifier|private
specifier|transient
name|Map
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorColumnAssign
index|[]
argument_list|>
name|outputVectorAssigners
decl_stmt|;
comment|// These members are used as out-of-band params
comment|// for the inner-loop supper.processOp callbacks
comment|//
specifier|private
specifier|transient
name|int
name|batchIndex
decl_stmt|;
specifier|private
specifier|transient
name|VectorHashKeyWrapper
index|[]
name|keyValues
decl_stmt|;
specifier|public
name|VectorMapJoinOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|private
interface|interface
name|MapJoinKeyEvaluator
block|{
name|MapJoinKey
name|evaluate
parameter_list|(
name|VectorHashKeyWrapper
name|kw
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
specifier|public
name|VectorMapJoinOperator
parameter_list|(
name|VectorizationContext
name|vContext
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|()
expr_stmt|;
name|MapJoinDesc
name|desc
init|=
operator|(
name|MapJoinDesc
operator|)
name|conf
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|desc
expr_stmt|;
name|order
operator|=
name|desc
operator|.
name|getTagOrder
argument_list|()
expr_stmt|;
name|numAliases
operator|=
name|desc
operator|.
name|getExprs
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|posBigTable
operator|=
operator|(
name|byte
operator|)
name|desc
operator|.
name|getPosBigTable
argument_list|()
expr_stmt|;
name|filterMaps
operator|=
name|desc
operator|.
name|getFilterMap
argument_list|()
expr_stmt|;
name|tagLen
operator|=
name|desc
operator|.
name|getTagLength
argument_list|()
expr_stmt|;
name|noOuterJoin
operator|=
name|desc
operator|.
name|isNoOuterJoin
argument_list|()
expr_stmt|;
name|vContext
operator|.
name|setOperatorType
argument_list|(
name|OperatorType
operator|.
name|FILTER
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filterExpressions
init|=
name|desc
operator|.
name|getFilters
argument_list|()
decl_stmt|;
name|bigTableFilterExpressions
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|filterExpressions
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
argument_list|)
expr_stmt|;
name|vContext
operator|.
name|setOperatorType
argument_list|(
name|OperatorType
operator|.
name|MAPJOIN
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyDesc
init|=
name|desc
operator|.
name|getKeys
argument_list|()
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
decl_stmt|;
name|keyExpressions
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|keyDesc
argument_list|)
expr_stmt|;
name|keyOutputWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
name|keyDesc
argument_list|)
expr_stmt|;
comment|// We're only going to evaluate the big table vectorized expressions,
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
init|=
name|desc
operator|.
name|getExprs
argument_list|()
decl_stmt|;
name|bigTableValueExpressions
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|exprs
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|outColNames
init|=
name|desc
operator|.
name|getOutputColumnNames
argument_list|()
decl_stmt|;
name|int
name|outputColumnIndex
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
name|alias
range|:
name|order
control|)
block|{
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|exprs
operator|.
name|get
argument_list|(
name|alias
argument_list|)
control|)
block|{
name|String
name|columnName
init|=
name|outColNames
operator|.
name|get
argument_list|(
name|outputColumnIndex
argument_list|)
decl_stmt|;
name|vContext
operator|.
name|addOutputColumn
argument_list|(
name|columnName
argument_list|,
name|expr
operator|.
name|getTypeString
argument_list|()
argument_list|)
expr_stmt|;
operator|++
name|outputColumnIndex
expr_stmt|;
block|}
block|}
name|this
operator|.
name|fileKey
operator|=
name|vContext
operator|.
name|getFileKey
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|allTypeMaps
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|hconf
argument_list|)
operator|.
name|getMapWork
argument_list|()
operator|.
name|getScratchColumnVectorTypes
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|typeMap
init|=
name|allTypeMaps
operator|.
name|get
argument_list|(
name|fileKey
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|allColumnMaps
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|hconf
argument_list|)
operator|.
name|getMapWork
argument_list|()
operator|.
name|getScratchColumnMap
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|columnMap
init|=
name|allColumnMaps
operator|.
name|get
argument_list|(
name|fileKey
argument_list|)
decl_stmt|;
name|outputBatch
operator|=
name|VectorizedRowBatch
operator|.
name|buildBatch
argument_list|(
name|typeMap
argument_list|,
name|columnMap
argument_list|)
expr_stmt|;
name|keyWrapperBatch
operator|=
name|VectorHashKeyWrapperBatch
operator|.
name|compileKeyWrapperBatch
argument_list|(
name|keyExpressions
argument_list|)
expr_stmt|;
comment|// This key evaluator translates from the vectorized VectorHashKeyWrapper format
comment|// into the row-mode MapJoinKey
name|keyEvaluator
operator|=
operator|new
name|MapJoinKeyEvaluator
argument_list|()
block|{
specifier|private
name|MapJoinKey
name|key
decl_stmt|;
specifier|public
name|MapJoinKeyEvaluator
name|init
parameter_list|()
block|{
name|key
operator|=
operator|new
name|MapJoinKey
argument_list|(
operator|new
name|Object
index|[
name|keyExpressions
operator|.
name|length
index|]
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinKey
name|evaluate
parameter_list|(
name|VectorHashKeyWrapper
name|kw
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
index|[]
name|keyValues
init|=
name|key
operator|.
name|getKey
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
name|keyExpressions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|keyValues
index|[
name|i
index|]
operator|=
name|keyWrapperBatch
operator|.
name|getWritableKeyValue
argument_list|(
name|kw
argument_list|,
name|i
argument_list|,
name|keyOutputWriters
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|key
return|;
block|}
empty_stmt|;
block|}
operator|.
name|init
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|valueExpressions
init|=
name|conf
operator|.
name|getExprs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|bigTableExpressions
init|=
name|valueExpressions
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
decl_stmt|;
name|VectorExpressionWriterFactory
operator|.
name|processVectorExpressions
argument_list|(
name|bigTableExpressions
argument_list|,
operator|new
name|VectorExpressionWriterFactory
operator|.
name|ListOIDClosure
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|VectorExpressionWriter
index|[]
name|writers
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|oids
parameter_list|)
block|{
name|valueWriters
operator|=
name|writers
expr_stmt|;
name|joinValuesObjectInspectors
index|[
name|posBigTable
index|]
operator|=
name|oids
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// We're hijacking the big table evaluators an replace them with our own custom ones
comment|// which are going to return values from the input batch vector expressions
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|vectorNodeEvaluators
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|(
name|bigTableExpressions
operator|.
name|size
argument_list|()
argument_list|)
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
name|bigTableExpressions
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|ExprNodeDesc
name|desc
init|=
name|bigTableExpressions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|VectorExpression
name|vectorExpr
init|=
name|bigTableValueExpressions
index|[
name|i
index|]
decl_stmt|;
comment|// This is a vectorized aware evaluator
name|ExprNodeEvaluator
name|eval
init|=
operator|new
name|ExprNodeEvaluator
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
name|desc
argument_list|)
block|{
name|int
name|columnIndex
decl_stmt|;
empty_stmt|;
name|int
name|writerIndex
decl_stmt|;
specifier|public
name|ExprNodeEvaluator
name|initVectorExpr
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|int
name|writerIndex
parameter_list|)
block|{
name|this
operator|.
name|columnIndex
operator|=
name|columnIndex
expr_stmt|;
name|this
operator|.
name|writerIndex
operator|=
name|writerIndex
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"should never reach here"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|_evaluate
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|version
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|inBatch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|int
name|rowIndex
init|=
name|inBatch
operator|.
name|selectedInUse
condition|?
name|inBatch
operator|.
name|selected
index|[
name|batchIndex
index|]
else|:
name|batchIndex
decl_stmt|;
return|return
name|valueWriters
index|[
name|writerIndex
index|]
operator|.
name|writeValue
argument_list|(
name|inBatch
operator|.
name|cols
index|[
name|columnIndex
index|]
argument_list|,
name|rowIndex
argument_list|)
return|;
block|}
block|}
operator|.
name|initVectorExpr
argument_list|(
name|vectorExpr
operator|.
name|getOutputColumn
argument_list|()
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|vectorNodeEvaluators
operator|.
name|add
argument_list|(
name|eval
argument_list|)
expr_stmt|;
block|}
comment|// Now replace the old evaluators with our own
name|joinValues
index|[
name|posBigTable
index|]
operator|=
name|vectorNodeEvaluators
expr_stmt|;
comment|// Filtering is handled in the input batch processing
name|filterMaps
index|[
name|posBigTable
index|]
operator|=
literal|null
expr_stmt|;
name|outputVectorAssigners
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorColumnAssign
index|[]
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * 'forwards' the (row-mode) record into the (vectorized) output batch    */
annotation|@
name|Override
specifier|protected
name|void
name|internalForward
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|outputOI
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
index|[]
name|values
init|=
operator|(
name|Object
index|[]
operator|)
name|row
decl_stmt|;
name|VectorColumnAssign
index|[]
name|vcas
init|=
name|outputVectorAssigners
operator|.
name|get
argument_list|(
name|outputOI
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|vcas
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|allColumnMaps
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|hconf
argument_list|)
operator|.
name|getMapWork
argument_list|()
operator|.
name|getScratchColumnMap
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|columnMap
init|=
name|allColumnMaps
operator|.
name|get
argument_list|(
name|fileKey
argument_list|)
decl_stmt|;
name|vcas
operator|=
name|VectorColumnAssignFactory
operator|.
name|buildAssigners
argument_list|(
name|outputBatch
argument_list|,
name|outputOI
argument_list|,
name|columnMap
argument_list|,
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|)
expr_stmt|;
name|outputVectorAssigners
operator|.
name|put
argument_list|(
name|outputOI
argument_list|,
name|vcas
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|vcas
index|[
name|i
index|]
operator|.
name|assignObjectValue
argument_list|(
name|values
index|[
name|i
index|]
argument_list|,
name|outputBatch
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
operator|++
name|outputBatch
operator|.
name|size
expr_stmt|;
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
name|flushOutput
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|flushOutput
parameter_list|()
throws|throws
name|HiveException
block|{
name|forward
argument_list|(
name|outputBatch
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|outputBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|aborted
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|aborted
operator|&&
literal|0
operator|<
name|outputBatch
operator|.
name|size
condition|)
block|{
name|flushOutput
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|MapJoinKey
name|computeMapJoinKey
parameter_list|(
name|Object
name|row
parameter_list|,
name|byte
name|alias
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|inBatch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
return|return
name|keyEvaluator
operator|.
name|evaluate
argument_list|(
name|keyValues
index|[
name|batchIndex
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|byte
name|alias
init|=
operator|(
name|byte
operator|)
name|tag
decl_stmt|;
name|VectorizedRowBatch
name|inBatch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|bigTableFilterExpressions
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|bigTableFilterExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|inBatch
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
literal|null
operator|!=
name|bigTableValueExpressions
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|bigTableValueExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|inBatch
argument_list|)
expr_stmt|;
block|}
block|}
name|keyWrapperBatch
operator|.
name|evaluateBatch
argument_list|(
name|inBatch
argument_list|)
expr_stmt|;
name|keyValues
operator|=
name|keyWrapperBatch
operator|.
name|getVectorHashKeyWrappers
argument_list|()
expr_stmt|;
comment|// This implementation of vectorized JOIN is delegating all the work
comment|// to the row-mode implementation by hijacking the big table node evaluators
comment|// and calling the row-mode join processOp for each row in the input batch.
comment|// Since the JOIN operator is not fully vectorized anyway atm (due to the use
comment|// of row-mode small-tables) this is a reasonable trade-off.
comment|//
for|for
control|(
name|batchIndex
operator|=
literal|0
init|;
name|batchIndex
operator|<
name|inBatch
operator|.
name|size
condition|;
operator|++
name|batchIndex
control|)
block|{
name|super
operator|.
name|processOp
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
comment|// Set these two to invalid values so any attempt to use them
comment|// outside the inner loop results in NPE/OutOfBounds errors
name|batchIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|keyValues
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

