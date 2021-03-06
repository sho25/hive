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
name|CompilationOpContext
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
name|SMBMapJoinOperator
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
name|exec
operator|.
name|vector
operator|.
name|wrapper
operator|.
name|VectorHashKeyWrapperBase
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
name|wrapper
operator|.
name|VectorHashKeyWrapperBatch
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
name|SMBJoinDesc
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
name|VectorDesc
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
name|VectorMapJoinDesc
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
name|VectorSMBJoinDesc
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
name|objectinspector
operator|.
name|StructObjectInspector
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * VectorSMBJoinOperator.  * Implements the vectorized SMB join operator. The implementation relies on the row-mode SMB join operator.  * It accepts a vectorized batch input from the big table and iterates over the batch, calling the parent row-mode  * implementation for each row in the batch.  */
end_comment

begin_class
specifier|public
class|class
name|VectorSMBMapJoinOperator
extends|extends
name|SMBMapJoinOperator
implements|implements
name|VectorizationOperator
implements|,
name|VectorizationContextRegion
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
name|VectorSMBMapJoinOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorizationContext
name|vContext
decl_stmt|;
specifier|private
name|VectorSMBJoinDesc
name|vectorDesc
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|bigTableValueExpressions
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|bigTableFilterExpressions
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|keyExpressions
decl_stmt|;
specifier|private
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
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
name|VectorizedRowBatchCtx
name|vrbCtx
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
decl_stmt|;
specifier|private
specifier|transient
name|Map
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorAssignRow
argument_list|>
name|outputVectorAssignRowMap
decl_stmt|;
specifier|private
specifier|transient
name|int
name|batchIndex
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|VectorHashKeyWrapperBase
index|[]
name|keyValues
decl_stmt|;
specifier|private
specifier|transient
name|SMBJoinKeyEvaluator
name|keyEvaluator
decl_stmt|;
specifier|private
specifier|transient
name|VectorExpressionWriter
index|[]
name|valueWriters
decl_stmt|;
specifier|private
interface|interface
name|SMBJoinKeyEvaluator
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|evaluate
parameter_list|(
name|VectorHashKeyWrapperBase
name|kw
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorSMBMapJoinOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorSMBMapJoinOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorSMBMapJoinOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|SMBJoinDesc
name|desc
init|=
operator|(
name|SMBJoinDesc
operator|)
name|conf
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|desc
expr_stmt|;
name|this
operator|.
name|vContext
operator|=
name|vContext
expr_stmt|;
name|this
operator|.
name|vectorDesc
operator|=
operator|(
name|VectorSMBJoinDesc
operator|)
name|vectorDesc
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
comment|// Must obtain vectorized equivalents for filter and value expressions
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
name|keyOutputWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
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
argument_list|,
comment|/* vContextEnvironment */
name|vContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getInputVectorizationContext
parameter_list|()
block|{
return|return
name|vContext
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Object
argument_list|>
name|smbJoinComputeKeys
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
if|if
condition|(
name|alias
operator|==
name|this
operator|.
name|posBigTable
condition|)
block|{
comment|// The keyEvaluate reuses storage.  That doesn't work with SMB MapJoin because it
comment|// holds references to keys as it is merging.
name|List
argument_list|<
name|Object
argument_list|>
name|singletonListAndObjects
init|=
name|keyEvaluator
operator|.
name|evaluate
argument_list|(
name|keyValues
index|[
name|batchIndex
index|]
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|singletonListAndObjects
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
name|singletonListAndObjects
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|singletonListAndObjects
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|joinKeysObjectInspectors
index|[
name|alias
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|smbJoinComputeKeys
argument_list|(
name|row
argument_list|,
name|alias
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
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
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|bigTableFilterExpressions
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|keyExpressions
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|bigTableValueExpressions
argument_list|,
name|hconf
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
operator|(
name|StructObjectInspector
operator|)
name|this
operator|.
name|outputObjInspector
argument_list|,
name|vOutContext
operator|.
name|getScratchColumnTypeNames
argument_list|()
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
name|outputVectorAssignRowMap
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorAssignRow
argument_list|>
argument_list|()
expr_stmt|;
comment|// This key evaluator translates from the vectorized VectorHashKeyWrapper format
comment|// into the row-mode MapJoinKey
name|keyEvaluator
operator|=
operator|new
name|SMBJoinKeyEvaluator
argument_list|()
block|{
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|key
decl_stmt|;
specifier|public
name|SMBJoinKeyEvaluator
name|init
parameter_list|()
block|{
name|key
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
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
name|key
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|evaluate
parameter_list|(
name|VectorHashKeyWrapperBase
name|kw
parameter_list|)
throws|throws
name|HiveException
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
name|keyExpressions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|key
operator|.
name|set
argument_list|(
name|i
argument_list|,
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
comment|// We're hijacking the big table evaluators and replacing them with our own custom ones
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
argument_list|,
name|hconf
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
name|getOutputColumnNum
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
if|if
condition|(
name|alias
operator|!=
name|this
operator|.
name|posBigTable
condition|)
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
else|else
block|{
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
for|for
control|(
name|VectorExpression
name|ve
range|:
name|keyExpressions
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
comment|// Since the JOIN operator is not fully vectorized anyway at the moment
comment|// (due to the use of row-mode small-tables) this is a reasonable trade-off.
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
name|VectorAssignRow
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
name|VectorAssignRow
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
argument_list|,
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
name|vectorForward
argument_list|(
name|outputBatch
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
name|VectorizationContext
name|getOutputVectorizationContext
parameter_list|()
block|{
return|return
name|vOutContext
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorDesc
name|getVectorDesc
parameter_list|()
block|{
return|return
name|vectorDesc
return|;
block|}
block|}
end_class

end_unit

