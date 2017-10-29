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
name|io
operator|.
name|DataOutputBuffer
import|;
end_import

begin_comment
comment|/**  * The *NON-NATIVE* base vector map join operator class used by VectorMapJoinOperator and  * VectorMapJoinOuterFilteredOperator.  *  * It has common variables and code for the output batch, Hybrid Grace spill batch, and more.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinBaseOperator
extends|extends
name|MapJoinOperator
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
name|VectorMapJoinBaseOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|VectorizationContext
name|vContext
decl_stmt|;
specifier|protected
name|VectorMapJoinDesc
name|vectorDesc
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|VectorizationContext
name|vOutContext
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
specifier|protected
specifier|transient
name|VectorizedRowBatch
name|outputBatch
decl_stmt|;
specifier|protected
specifier|transient
name|VectorizedRowBatch
name|scratchBatch
decl_stmt|;
comment|// holds restored (from disk) big table rows
specifier|protected
specifier|transient
name|Map
argument_list|<
name|ObjectInspector
argument_list|,
name|VectorAssignRow
argument_list|>
name|outputVectorAssignRowMap
decl_stmt|;
specifier|protected
specifier|transient
name|VectorizedRowBatchCtx
name|vrbCtx
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|tag
decl_stmt|;
comment|// big table alias
comment|/** Kryo ctor. */
specifier|protected
name|VectorMapJoinBaseOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinBaseOperator
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
name|VectorMapJoinBaseOperator
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
name|super
argument_list|(
name|ctx
argument_list|)
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
name|VectorMapJoinDesc
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
name|vOutContext
operator|.
name|setInitialTypeInfos
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|getOutputTypeInfos
argument_list|(
name|desc
argument_list|)
argument_list|)
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
specifier|public
specifier|static
name|TypeInfo
index|[]
name|getOutputTypeInfos
parameter_list|(
name|MapJoinDesc
name|desc
parameter_list|)
block|{
specifier|final
name|byte
name|posBigTable
init|=
operator|(
name|byte
operator|)
name|desc
operator|.
name|getPosBigTable
argument_list|()
decl_stmt|;
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
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|bigTableExprs
init|=
name|desc
operator|.
name|getExprs
argument_list|()
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
decl_stmt|;
name|Byte
index|[]
name|order
init|=
name|desc
operator|.
name|getTagOrder
argument_list|()
decl_stmt|;
name|Byte
name|posSingleVectorMapJoinSmallTable
init|=
operator|(
name|order
index|[
literal|0
index|]
operator|==
name|posBigTable
condition|?
name|order
index|[
literal|1
index|]
else|:
name|order
index|[
literal|0
index|]
operator|)
decl_stmt|;
specifier|final
name|int
name|outputColumnCount
init|=
name|desc
operator|.
name|getOutputColumnNames
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|TypeInfo
index|[]
name|outputTypeInfos
init|=
operator|new
name|TypeInfo
index|[
name|outputColumnCount
index|]
decl_stmt|;
comment|/*      * Gather up big and small table output result information from the MapJoinDesc.      */
name|List
argument_list|<
name|Integer
argument_list|>
name|bigTableRetainList
init|=
name|desc
operator|.
name|getRetainList
argument_list|()
operator|.
name|get
argument_list|(
name|posBigTable
argument_list|)
decl_stmt|;
specifier|final
name|int
name|bigTableRetainSize
init|=
name|bigTableRetainList
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
index|[]
name|smallTableIndices
decl_stmt|;
name|int
name|smallTableIndicesSize
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|smallTableExprs
init|=
name|desc
operator|.
name|getExprs
argument_list|()
operator|.
name|get
argument_list|(
name|posSingleVectorMapJoinSmallTable
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getValueIndices
argument_list|()
operator|!=
literal|null
operator|&&
name|desc
operator|.
name|getValueIndices
argument_list|()
operator|.
name|get
argument_list|(
name|posSingleVectorMapJoinSmallTable
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|smallTableIndices
operator|=
name|desc
operator|.
name|getValueIndices
argument_list|()
operator|.
name|get
argument_list|(
name|posSingleVectorMapJoinSmallTable
argument_list|)
expr_stmt|;
name|smallTableIndicesSize
operator|=
name|smallTableIndices
operator|.
name|length
expr_stmt|;
block|}
else|else
block|{
name|smallTableIndices
operator|=
literal|null
expr_stmt|;
name|smallTableIndicesSize
operator|=
literal|0
expr_stmt|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|smallTableRetainList
init|=
name|desc
operator|.
name|getRetainList
argument_list|()
operator|.
name|get
argument_list|(
name|posSingleVectorMapJoinSmallTable
argument_list|)
decl_stmt|;
specifier|final
name|int
name|smallTableRetainSize
init|=
name|smallTableRetainList
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|smallTableResultSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|smallTableIndicesSize
operator|>
literal|0
condition|)
block|{
name|smallTableResultSize
operator|=
name|smallTableIndicesSize
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|smallTableRetainSize
operator|>
literal|0
condition|)
block|{
name|smallTableResultSize
operator|=
name|smallTableRetainSize
expr_stmt|;
block|}
comment|/*      * Determine the big table retained mapping first so we can optimize out (with      * projection) copying inner join big table keys in the subsequent small table results section.      */
name|int
name|nextOutputColumn
init|=
operator|(
name|order
index|[
literal|0
index|]
operator|==
name|posBigTable
condition|?
literal|0
else|:
name|smallTableResultSize
operator|)
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
name|bigTableRetainSize
condition|;
name|i
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|bigTableExprs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|outputTypeInfos
index|[
name|nextOutputColumn
index|]
operator|=
name|typeInfo
expr_stmt|;
name|nextOutputColumn
operator|++
expr_stmt|;
block|}
comment|/*      * Now determine the small table results.      */
name|int
name|firstSmallTableOutputColumn
decl_stmt|;
name|firstSmallTableOutputColumn
operator|=
operator|(
name|order
index|[
literal|0
index|]
operator|==
name|posBigTable
condition|?
name|bigTableRetainSize
else|:
literal|0
operator|)
expr_stmt|;
name|int
name|smallTableOutputCount
init|=
literal|0
decl_stmt|;
name|nextOutputColumn
operator|=
name|firstSmallTableOutputColumn
expr_stmt|;
comment|// Small table indices has more information (i.e. keys) than retain, so use it if it exists...
if|if
condition|(
name|smallTableIndicesSize
operator|>
literal|0
condition|)
block|{
name|smallTableOutputCount
operator|=
name|smallTableIndicesSize
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
name|smallTableIndicesSize
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|smallTableIndices
index|[
name|i
index|]
operator|>=
literal|0
condition|)
block|{
comment|// Zero and above numbers indicate a big table key is needed for
comment|// small table result "area".
name|int
name|keyIndex
init|=
name|smallTableIndices
index|[
name|i
index|]
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|keyDesc
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|outputTypeInfos
index|[
name|nextOutputColumn
index|]
operator|=
name|typeInfo
expr_stmt|;
block|}
else|else
block|{
comment|// Negative numbers indicate a column to be (deserialize) read from the small table's
comment|// LazyBinary value row.
name|int
name|smallTableValueIndex
init|=
operator|-
name|smallTableIndices
index|[
name|i
index|]
operator|-
literal|1
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|smallTableExprs
operator|.
name|get
argument_list|(
name|smallTableValueIndex
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|outputTypeInfos
index|[
name|nextOutputColumn
index|]
operator|=
name|typeInfo
expr_stmt|;
block|}
name|nextOutputColumn
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|smallTableRetainSize
operator|>
literal|0
condition|)
block|{
name|smallTableOutputCount
operator|=
name|smallTableRetainSize
expr_stmt|;
comment|// Only small table values appear in join output result.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|smallTableRetainSize
condition|;
name|i
operator|++
control|)
block|{
name|int
name|smallTableValueIndex
init|=
name|smallTableRetainList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|smallTableExprs
operator|.
name|get
argument_list|(
name|smallTableValueIndex
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|outputTypeInfos
index|[
name|nextOutputColumn
index|]
operator|=
name|typeInfo
expr_stmt|;
name|nextOutputColumn
operator|++
expr_stmt|;
block|}
block|}
return|return
name|outputTypeInfos
return|;
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
argument_list|,
name|vOutContext
operator|.
name|getScratchDataTypePhysicalVariations
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
name|forward
argument_list|(
name|outputBatch
argument_list|,
literal|null
argument_list|,
literal|true
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
comment|/**    * For a vectorized row batch from the rows feed from the super MapJoinOperator.    */
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
if|if
condition|(
name|scratchBatch
operator|==
literal|null
condition|)
block|{
comment|// The process method was not called -- no big table rows.
return|return;
block|}
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

