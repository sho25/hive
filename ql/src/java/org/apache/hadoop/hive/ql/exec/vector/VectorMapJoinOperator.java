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
name|Arrays
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
name|Collection
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
name|concurrent
operator|.
name|Future
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
name|JoinUtil
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
name|persistence
operator|.
name|HybridHashTableContainer
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
name|MapJoinTableContainer
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
name|MapJoinTableContainer
operator|.
name|ReusableGetAdaptor
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
name|ObjectContainer
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
name|StructObjectInspector
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
name|io
operator|.
name|DataOutputBuffer
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
implements|implements
name|VectorizationContextRegion
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
specifier|private
name|VectorExpression
index|[]
name|keyExpressions
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
name|VectorizationContext
name|vOutContext
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
specifier|private
specifier|transient
name|VectorizedRowBatch
name|outputBatch
decl_stmt|;
specifier|private
specifier|transient
name|VectorizedRowBatch
name|scratchBatch
decl_stmt|;
comment|// holds restored (from disk) big table rows
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
name|VectorAssignRowSameBatch
argument_list|>
name|outputVectorAssignRowMap
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
specifier|private
specifier|transient
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
decl_stmt|;
specifier|private
specifier|transient
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
decl_stmt|;
specifier|private
specifier|transient
name|VectorizedRowBatchCtx
name|vrbCtx
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|int
name|tag
decl_stmt|;
comment|// big table alias
specifier|private
name|VectorExpressionWriter
index|[]
name|rowWriters
decl_stmt|;
comment|// Writer for producing row from input batch
specifier|protected
specifier|transient
name|Object
index|[]
name|singleRow
decl_stmt|;
specifier|public
name|VectorMapJoinOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
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
name|noOuterJoin
operator|=
name|desc
operator|.
name|isNoOuterJoin
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
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|FILTER
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
comment|// We are making a new output vectorized row batch.
name|vOutContext
operator|=
operator|new
name|VectorizationContext
argument_list|(
name|getName
argument_list|()
argument_list|,
name|desc
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Code borrowed from VectorReduceSinkOperator.initializeOp
name|VectorExpressionWriterFactory
operator|.
name|processVectorInspector
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|,
operator|new
name|VectorExpressionWriterFactory
operator|.
name|SingleOIDClosure
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
name|ObjectInspector
name|objectInspector
parameter_list|)
block|{
name|rowWriters
operator|=
name|writers
expr_stmt|;
name|inputObjInspectors
index|[
literal|0
index|]
operator|=
name|objectInspector
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|singleRow
operator|=
operator|new
name|Object
index|[
name|rowWriters
operator|.
name|length
index|]
expr_stmt|;
name|Collection
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|result
init|=
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyDesc
init|=
name|conf
operator|.
name|getKeys
argument_list|()
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
decl_stmt|;
name|keyOutputWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
name|keyDesc
argument_list|)
expr_stmt|;
name|vrbCtx
operator|=
operator|new
name|VectorizedRowBatchCtx
argument_list|()
expr_stmt|;
name|vrbCtx
operator|.
name|init
argument_list|(
name|vOutContext
operator|.
name|getScratchColumnTypeMap
argument_list|()
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|this
operator|.
name|outputObjInspector
argument_list|)
expr_stmt|;
name|outputBatch
operator|=
name|vrbCtx
operator|.
name|createVectorizedRowBatch
argument_list|()
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
name|outputVectorAssignRowMap
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorAssignRowSameBatch
argument_list|>
argument_list|()
expr_stmt|;
return|return
name|result
return|;
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
name|VectorAssignRowSameBatch
name|va
init|=
name|outputVectorAssignRowMap
operator|.
name|get
argument_list|(
name|outputOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|va
operator|==
literal|null
condition|)
block|{
name|va
operator|=
operator|new
name|VectorAssignRowSameBatch
argument_list|()
expr_stmt|;
name|va
operator|.
name|init
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|outputOI
argument_list|,
name|vOutContext
operator|.
name|getProjectedColumns
argument_list|()
argument_list|)
expr_stmt|;
name|va
operator|.
name|setOneBatch
argument_list|(
name|outputBatch
argument_list|)
expr_stmt|;
name|outputVectorAssignRowMap
operator|.
name|put
argument_list|(
name|outputOI
argument_list|,
name|va
argument_list|)
expr_stmt|;
block|}
name|va
operator|.
name|assignRow
argument_list|(
name|outputBatch
operator|.
name|size
argument_list|,
name|values
argument_list|)
expr_stmt|;
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
name|super
operator|.
name|closeOp
argument_list|(
name|aborted
argument_list|)
expr_stmt|;
for|for
control|(
name|MapJoinTableContainer
name|tableContainer
range|:
name|mapJoinTables
control|)
block|{
if|if
condition|(
name|tableContainer
operator|!=
literal|null
condition|)
block|{
name|tableContainer
operator|.
name|dumpMetrics
argument_list|()
expr_stmt|;
block|}
block|}
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
name|JoinUtil
operator|.
name|JoinResult
name|setMapJoinKey
parameter_list|(
name|ReusableGetAdaptor
name|dest
parameter_list|,
name|Object
name|row
parameter_list|,
name|byte
name|alias
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|dest
operator|.
name|setFromVector
argument_list|(
name|keyValues
index|[
name|batchIndex
index|]
argument_list|,
name|keyOutputWriters
argument_list|,
name|keyWrapperBatch
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
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
comment|// Preparation for hybrid grace hash join
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
if|if
condition|(
name|scratchBatch
operator|==
literal|null
condition|)
block|{
name|scratchBatch
operator|=
name|VectorizedBatchUtil
operator|.
name|makeLike
argument_list|(
name|inBatch
argument_list|)
expr_stmt|;
block|}
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
name|process
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
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getOuputVectorizationContext
parameter_list|()
block|{
return|return
name|vOutContext
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|spillBigTableRow
parameter_list|(
name|MapJoinTableContainer
name|hybridHtContainer
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Extract the actual row from row batch
name|VectorizedRowBatch
name|inBatch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|Object
index|[]
name|actualRow
init|=
name|getRowObject
argument_list|(
name|inBatch
argument_list|,
name|batchIndex
argument_list|)
decl_stmt|;
name|super
operator|.
name|spillBigTableRow
argument_list|(
name|hybridHtContainer
argument_list|,
name|actualRow
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reProcessBigTable
parameter_list|(
name|int
name|partitionId
parameter_list|)
throws|throws
name|HiveException
block|{
name|HybridHashTableContainer
operator|.
name|HashPartition
name|partition
init|=
name|firstSmallTable
operator|.
name|getHashPartitions
argument_list|()
index|[
name|partitionId
index|]
decl_stmt|;
name|ObjectContainer
name|bigTable
init|=
name|partition
operator|.
name|getMatchfileObjContainer
argument_list|()
decl_stmt|;
name|DataOutputBuffer
name|dataOutputBuffer
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
while|while
condition|(
name|bigTable
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Object
name|row
init|=
name|bigTable
operator|.
name|next
argument_list|()
decl_stmt|;
name|VectorizedBatchUtil
operator|.
name|addProjectedRowToBatchFrom
argument_list|(
name|row
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|posBigTable
index|]
argument_list|,
name|scratchBatch
operator|.
name|size
argument_list|,
name|scratchBatch
argument_list|,
name|dataOutputBuffer
argument_list|)
expr_stmt|;
name|scratchBatch
operator|.
name|size
operator|++
expr_stmt|;
if|if
condition|(
name|scratchBatch
operator|.
name|size
operator|==
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|)
block|{
name|process
argument_list|(
name|scratchBatch
argument_list|,
name|tag
argument_list|)
expr_stmt|;
comment|// call process once we have a full batch
name|scratchBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|dataOutputBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Process the row batch that has less than DEFAULT_SIZE rows
if|if
condition|(
name|scratchBatch
operator|.
name|size
operator|>
literal|0
condition|)
block|{
name|process
argument_list|(
name|scratchBatch
argument_list|,
name|tag
argument_list|)
expr_stmt|;
name|scratchBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|dataOutputBuffer
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|bigTable
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// Code borrowed from VectorReduceSinkOperator
specifier|private
name|Object
index|[]
name|getRowObject
parameter_list|(
name|VectorizedRowBatch
name|vrb
parameter_list|,
name|int
name|rowIndex
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|batchIndex
init|=
name|rowIndex
decl_stmt|;
if|if
condition|(
name|vrb
operator|.
name|selectedInUse
condition|)
block|{
name|batchIndex
operator|=
name|vrb
operator|.
name|selected
index|[
name|rowIndex
index|]
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
name|vrb
operator|.
name|projectionSize
condition|;
name|i
operator|++
control|)
block|{
name|ColumnVector
name|vectorColumn
init|=
name|vrb
operator|.
name|cols
index|[
name|vrb
operator|.
name|projectedColumns
index|[
name|i
index|]
index|]
decl_stmt|;
if|if
condition|(
name|vectorColumn
operator|!=
literal|null
condition|)
block|{
name|singleRow
index|[
name|i
index|]
operator|=
name|rowWriters
index|[
name|i
index|]
operator|.
name|writeValue
argument_list|(
name|vectorColumn
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Some columns from tables are not used.
name|singleRow
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|singleRow
return|;
block|}
block|}
end_class

end_unit

