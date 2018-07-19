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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|primitives
operator|.
name|Ints
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
name|TopNKeyOperator
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
name|TopNKeyDesc
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
name|VectorTopNKeyDesc
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
name|serde
operator|.
name|serdeConstants
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
name|SerDeException
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
name|binarysortable
operator|.
name|BinarySortableSerDe
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
name|ObjectInspectorFactory
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
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
name|WritableUtils
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
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|PriorityQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import static
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
operator|.
name|TOPNKEY
import|;
end_import

begin_comment
comment|/**  * VectorTopNKeyOperator passes rows that contains top N keys only.  */
end_comment

begin_class
specifier|public
class|class
name|VectorTopNKeyOperator
extends|extends
name|Operator
argument_list|<
name|TopNKeyDesc
argument_list|>
implements|implements
name|VectorizationOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorTopNKeyDesc
name|vectorDesc
decl_stmt|;
specifier|private
name|VectorizationContext
name|vContext
decl_stmt|;
comment|// Key column info
specifier|private
name|int
index|[]
name|keyColumnNums
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|keyTypeInfos
decl_stmt|;
comment|// Extract row
specifier|private
specifier|transient
name|Object
index|[]
name|singleRow
decl_stmt|;
specifier|private
specifier|transient
name|VectorExtractRow
name|vectorExtractRow
decl_stmt|;
comment|// Serialization
specifier|private
specifier|transient
name|BinarySortableSerDe
name|binarySortableSerDe
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
name|keyObjectInspector
decl_stmt|;
comment|// Batch processing
specifier|private
specifier|transient
name|boolean
name|firstBatch
decl_stmt|;
specifier|private
specifier|transient
name|PriorityQueue
argument_list|<
name|Writable
argument_list|>
name|priorityQueue
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|temporarySelected
decl_stmt|;
specifier|public
name|VectorTopNKeyOperator
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
block|{
name|this
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|TopNKeyDesc
operator|)
name|conf
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
name|VectorTopNKeyDesc
operator|)
name|vectorDesc
expr_stmt|;
name|VectorExpression
index|[]
name|keyExpressions
init|=
name|this
operator|.
name|vectorDesc
operator|.
name|getKeyExpressions
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numKeys
init|=
name|keyExpressions
operator|.
name|length
decl_stmt|;
name|keyColumnNums
operator|=
operator|new
name|int
index|[
name|numKeys
index|]
expr_stmt|;
name|keyTypeInfos
operator|=
operator|new
name|TypeInfo
index|[
name|numKeys
index|]
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
name|numKeys
condition|;
name|i
operator|++
control|)
block|{
name|keyColumnNums
index|[
name|i
index|]
operator|=
name|keyExpressions
index|[
name|i
index|]
operator|.
name|getOutputColumnNum
argument_list|()
expr_stmt|;
name|keyTypeInfos
index|[
name|i
index|]
operator|=
name|keyExpressions
index|[
name|i
index|]
operator|.
name|getOutputTypeInfo
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorTopNKeyOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorTopNKeyOperator
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
name|vectorDesc
operator|.
name|getKeyExpressions
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|VectorExpression
name|keyExpression
range|:
name|vectorDesc
operator|.
name|getKeyExpressions
argument_list|()
control|)
block|{
name|keyExpression
operator|.
name|init
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|firstBatch
operator|=
literal|true
expr_stmt|;
name|VectorExpression
index|[]
name|keyExpressions
init|=
name|vectorDesc
operator|.
name|getKeyExpressions
argument_list|()
decl_stmt|;
specifier|final
name|int
name|size
init|=
name|keyExpressions
operator|.
name|length
decl_stmt|;
name|ObjectInspector
index|[]
name|fieldObjectInspectors
init|=
operator|new
name|ObjectInspector
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|VectorExpression
name|keyExpression
init|=
name|keyExpressions
index|[
name|i
index|]
decl_stmt|;
name|fieldObjectInspectors
index|[
name|i
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|keyExpression
operator|.
name|getOutputTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|keyObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|this
operator|.
name|conf
operator|.
name|getKeyColumnNames
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|fieldObjectInspectors
argument_list|)
argument_list|)
expr_stmt|;
name|temporarySelected
operator|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|data
decl_stmt|;
comment|// The selected vector represents selected rows.
comment|// Clone the selected vector
name|System
operator|.
name|arraycopy
argument_list|(
name|batch
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|temporarySelected
argument_list|,
literal|0
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|int
index|[]
name|selectedBackup
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|batch
operator|.
name|selected
operator|=
name|temporarySelected
expr_stmt|;
name|int
name|sizeBackup
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|boolean
name|selectedInUseBackup
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
for|for
control|(
name|VectorExpression
name|keyExpression
range|:
name|vectorDesc
operator|.
name|getKeyExpressions
argument_list|()
control|)
block|{
name|keyExpression
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|firstBatch
condition|)
block|{
name|vectorExtractRow
operator|=
operator|new
name|VectorExtractRow
argument_list|()
expr_stmt|;
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|keyObjectInspector
argument_list|,
name|Ints
operator|.
name|asList
argument_list|(
name|keyColumnNums
argument_list|)
argument_list|)
expr_stmt|;
name|singleRow
operator|=
operator|new
name|Object
index|[
name|vectorExtractRow
operator|.
name|getCount
argument_list|()
index|]
expr_stmt|;
name|Comparator
name|comparator
init|=
name|Comparator
operator|.
name|reverseOrder
argument_list|()
decl_stmt|;
name|priorityQueue
operator|=
operator|new
name|PriorityQueue
argument_list|<
name|Writable
argument_list|>
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
try|try
block|{
name|binarySortableSerDe
operator|=
operator|new
name|BinarySortableSerDe
argument_list|()
expr_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Joiner
name|joiner
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|joiner
operator|.
name|join
argument_list|(
name|conf
operator|.
name|getKeyColumnNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|joiner
operator|.
name|join
argument_list|(
name|keyTypeInfos
argument_list|)
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|,
name|conf
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|)
expr_stmt|;
name|binarySortableSerDe
operator|.
name|initialize
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|properties
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|firstBatch
operator|=
literal|false
expr_stmt|;
block|}
comment|// Clear the priority queue
name|priorityQueue
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Get top n keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
comment|// Get keys
name|int
name|j
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|j
operator|=
name|batch
operator|.
name|selected
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
name|j
operator|=
name|i
expr_stmt|;
block|}
name|vectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|j
argument_list|,
name|singleRow
argument_list|)
expr_stmt|;
name|Writable
name|keysWritable
decl_stmt|;
try|try
block|{
name|keysWritable
operator|=
name|binarySortableSerDe
operator|.
name|serialize
argument_list|(
name|singleRow
argument_list|,
name|keyObjectInspector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Put the copied keys into the priority queue
if|if
condition|(
operator|!
name|priorityQueue
operator|.
name|contains
argument_list|(
name|keysWritable
argument_list|)
condition|)
block|{
name|priorityQueue
operator|.
name|offer
argument_list|(
name|WritableUtils
operator|.
name|clone
argument_list|(
name|keysWritable
argument_list|,
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Limit the queue size
if|if
condition|(
name|priorityQueue
operator|.
name|size
argument_list|()
operator|>
name|conf
operator|.
name|getTopN
argument_list|()
condition|)
block|{
name|priorityQueue
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Filter rows with top n keys
name|int
name|size
init|=
literal|0
decl_stmt|;
name|int
index|[]
name|selected
init|=
operator|new
name|int
index|[
name|batch
operator|.
name|selected
operator|.
name|length
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|j
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|j
operator|=
name|batch
operator|.
name|selected
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
name|j
operator|=
name|i
expr_stmt|;
block|}
comment|// Get keys
name|vectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|j
argument_list|,
name|singleRow
argument_list|)
expr_stmt|;
name|Writable
name|keysWritable
decl_stmt|;
try|try
block|{
name|keysWritable
operator|=
name|binarySortableSerDe
operator|.
name|serialize
argument_list|(
name|singleRow
argument_list|,
name|keyObjectInspector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Select a row in the priority queue
if|if
condition|(
name|priorityQueue
operator|.
name|contains
argument_list|(
name|keysWritable
argument_list|)
condition|)
block|{
name|selected
index|[
name|size
operator|++
index|]
operator|=
name|j
expr_stmt|;
block|}
block|}
comment|// Apply selection to batch
if|if
condition|(
name|batch
operator|.
name|size
operator|!=
name|size
condition|)
block|{
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|selected
operator|=
name|selected
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
comment|// Forward the result
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|forward
argument_list|(
name|batch
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Restore the original selected vector
name|batch
operator|.
name|selected
operator|=
name|selectedBackup
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|sizeBackup
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
name|selectedInUseBackup
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|TopNKeyOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|TOPNKEY
return|;
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
specifier|public
name|VectorDesc
name|getVectorDesc
parameter_list|()
block|{
return|return
name|vectorDesc
return|;
block|}
comment|// Because a TopNKeyOperator works like a FilterOperator with top n key condition, its properties
comment|// for optimizers has same values. Following methods are same with FilterOperator;
comment|// supportSkewJoinOptimization, columnNamesRowResolvedCanBeObtained,
comment|// supportAutomaticSortMergeJoin, and supportUnionRemoveOptimization.
annotation|@
name|Override
specifier|public
name|boolean
name|supportSkewJoinOptimization
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|columnNamesRowResolvedCanBeObtained
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportAutomaticSortMergeJoin
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportUnionRemoveOptimization
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|// Must send on to VectorPTFOperator...
annotation|@
name|Override
specifier|public
name|void
name|setNextVectorBatchGroupStatus
parameter_list|(
name|boolean
name|isLastGroupBatch
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|setNextVectorBatchGroupStatus
argument_list|(
name|isLastGroupBatch
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

