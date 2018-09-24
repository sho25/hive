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
name|reducesink
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiFunction
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
name|VectorSerializeRow
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
name|expressions
operator|.
name|BucketNumExpression
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
name|serde2
operator|.
name|ByteStream
operator|.
name|Output
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
name|fast
operator|.
name|BinarySortableSerializeWrite
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * This class is the object hash (not Uniform Hash) operator class for native vectorized reduce sink.  * It takes the "object" hash code of bucket and/or partition keys (which are often subsets of the  * reduce key).  If the bucket and partition keys are empty, the hash will be a random number.  */
end_comment

begin_class
specifier|public
class|class
name|VectorReduceSinkObjectHashOperator
extends|extends
name|VectorReduceSinkCommonOperator
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
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorReduceSinkObjectHashOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
name|boolean
name|isEmptyBuckets
decl_stmt|;
specifier|protected
name|int
index|[]
name|reduceSinkBucketColumnMap
decl_stmt|;
specifier|protected
name|TypeInfo
index|[]
name|reduceSinkBucketTypeInfos
decl_stmt|;
specifier|protected
name|VectorExpression
index|[]
name|reduceSinkBucketExpressions
decl_stmt|;
specifier|protected
name|boolean
name|isEmptyPartitions
decl_stmt|;
specifier|protected
name|int
index|[]
name|reduceSinkPartitionColumnMap
decl_stmt|;
specifier|protected
name|TypeInfo
index|[]
name|reduceSinkPartitionTypeInfos
decl_stmt|;
specifier|protected
name|VectorExpression
index|[]
name|reduceSinkPartitionExpressions
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
specifier|private
specifier|transient
name|boolean
name|isKeyInitialized
decl_stmt|;
specifier|protected
specifier|transient
name|Output
name|keyOutput
decl_stmt|;
specifier|protected
specifier|transient
name|VectorSerializeRow
argument_list|<
name|BinarySortableSerializeWrite
argument_list|>
name|keyVectorSerializeRow
decl_stmt|;
specifier|private
specifier|transient
name|int
name|numBuckets
decl_stmt|;
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|bucketObjectInspectors
decl_stmt|;
specifier|private
specifier|transient
name|VectorExtractRow
name|bucketVectorExtractRow
decl_stmt|;
specifier|private
specifier|transient
name|Object
index|[]
name|bucketFieldValues
decl_stmt|;
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|partitionObjectInspectors
decl_stmt|;
specifier|private
specifier|transient
name|VectorExtractRow
name|partitionVectorExtractRow
decl_stmt|;
specifier|private
specifier|transient
name|Object
index|[]
name|partitionFieldValues
decl_stmt|;
specifier|private
specifier|transient
name|Random
name|nonPartitionRandom
decl_stmt|;
specifier|private
specifier|transient
name|BiFunction
argument_list|<
name|Object
index|[]
argument_list|,
name|ObjectInspector
index|[]
argument_list|,
name|Integer
argument_list|>
name|hashFunc
decl_stmt|;
specifier|private
specifier|transient
name|BucketNumExpression
name|bucketExpr
init|=
literal|null
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|VectorReduceSinkObjectHashOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkObjectHashOperator
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
name|VectorReduceSinkObjectHashOperator
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
argument_list|,
name|conf
argument_list|,
name|vContext
argument_list|,
name|vectorDesc
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"VectorReduceSinkObjectHashOperator constructor vectorReduceSinkInfo "
operator|+
name|vectorReduceSinkInfo
argument_list|)
expr_stmt|;
comment|// This the is Object Hash class variation.
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|vectorReduceSinkInfo
operator|.
name|getUseUniformHash
argument_list|()
argument_list|)
expr_stmt|;
name|isEmptyBuckets
operator|=
name|this
operator|.
name|vectorDesc
operator|.
name|getIsEmptyBuckets
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isEmptyBuckets
condition|)
block|{
name|reduceSinkBucketColumnMap
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkBucketColumnMap
argument_list|()
expr_stmt|;
name|reduceSinkBucketTypeInfos
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkBucketTypeInfos
argument_list|()
expr_stmt|;
name|reduceSinkBucketExpressions
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkBucketExpressions
argument_list|()
expr_stmt|;
block|}
name|isEmptyPartitions
operator|=
name|this
operator|.
name|vectorDesc
operator|.
name|getIsEmptyPartitions
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isEmptyPartitions
condition|)
block|{
name|reduceSinkPartitionColumnMap
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkPartitionColumnMap
argument_list|()
expr_stmt|;
name|reduceSinkPartitionTypeInfos
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkPartitionTypeInfos
argument_list|()
expr_stmt|;
name|reduceSinkPartitionExpressions
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkPartitionExpressions
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|ObjectInspector
index|[]
name|getObjectInspectorArray
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|ObjectInspector
index|[]
name|objectInspectors
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
name|TypeInfo
name|typeInfo
init|=
name|typeInfos
index|[
name|i
index|]
decl_stmt|;
name|ObjectInspector
name|standardWritableObjectInspector
init|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
name|objectInspectors
index|[
name|i
index|]
operator|=
name|standardWritableObjectInspector
expr_stmt|;
block|}
return|return
name|objectInspectors
return|;
block|}
specifier|private
name|void
name|evaluateBucketExpr
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|rowNum
parameter_list|,
name|int
name|bucketNum
parameter_list|)
throws|throws
name|HiveException
block|{
name|bucketExpr
operator|.
name|setRowNum
argument_list|(
name|rowNum
argument_list|)
expr_stmt|;
name|bucketExpr
operator|.
name|setBucketNum
argument_list|(
name|bucketNum
argument_list|)
expr_stmt|;
name|bucketExpr
operator|.
name|evaluate
argument_list|(
name|batch
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
name|reduceSinkBucketExpressions
argument_list|)
expr_stmt|;
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|reduceSinkPartitionExpressions
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isEmptyKey
condition|)
block|{
comment|// For this variation, we serialize the key without caring if it single Long,
comment|// single String, multi-key, etc.
name|keyOutput
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|keyBinarySortableSerializeWrite
operator|.
name|set
argument_list|(
name|keyOutput
argument_list|)
expr_stmt|;
name|keyVectorSerializeRow
operator|=
operator|new
name|VectorSerializeRow
argument_list|<
name|BinarySortableSerializeWrite
argument_list|>
argument_list|(
name|keyBinarySortableSerializeWrite
argument_list|)
expr_stmt|;
name|keyVectorSerializeRow
operator|.
name|init
argument_list|(
name|reduceSinkKeyTypeInfos
argument_list|,
name|reduceSinkKeyColumnMap
argument_list|)
expr_stmt|;
block|}
comment|// Object Hash.
if|if
condition|(
name|isEmptyBuckets
condition|)
block|{
name|numBuckets
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|numBuckets
operator|=
name|conf
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
name|bucketObjectInspectors
operator|=
name|getObjectInspectorArray
argument_list|(
name|reduceSinkBucketTypeInfos
argument_list|)
expr_stmt|;
name|bucketVectorExtractRow
operator|=
operator|new
name|VectorExtractRow
argument_list|()
expr_stmt|;
name|bucketVectorExtractRow
operator|.
name|init
argument_list|(
name|reduceSinkBucketTypeInfos
argument_list|,
name|reduceSinkBucketColumnMap
argument_list|)
expr_stmt|;
name|bucketFieldValues
operator|=
operator|new
name|Object
index|[
name|reduceSinkBucketTypeInfos
operator|.
name|length
index|]
expr_stmt|;
block|}
if|if
condition|(
name|isEmptyPartitions
condition|)
block|{
name|nonPartitionRandom
operator|=
operator|new
name|Random
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partitionObjectInspectors
operator|=
name|getObjectInspectorArray
argument_list|(
name|reduceSinkPartitionTypeInfos
argument_list|)
expr_stmt|;
name|partitionVectorExtractRow
operator|=
operator|new
name|VectorExtractRow
argument_list|()
expr_stmt|;
name|partitionVectorExtractRow
operator|.
name|init
argument_list|(
name|reduceSinkPartitionTypeInfos
argument_list|,
name|reduceSinkPartitionColumnMap
argument_list|)
expr_stmt|;
name|partitionFieldValues
operator|=
operator|new
name|Object
index|[
name|reduceSinkPartitionTypeInfos
operator|.
name|length
index|]
expr_stmt|;
block|}
comment|// Set hashFunc
name|hashFunc
operator|=
name|bucketingVersion
operator|==
literal|2
operator|&&
operator|!
name|vectorDesc
operator|.
name|getIsAcidChange
argument_list|()
condition|?
name|ObjectInspectorUtils
operator|::
name|getBucketHashCode
else|:
name|ObjectInspectorUtils
operator|::
name|getBucketHashCodeOld
expr_stmt|;
comment|// Set function to evaluate _bucket_number if needed.
if|if
condition|(
name|reduceSinkKeyExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkKeyExpressions
control|)
block|{
if|if
condition|(
name|ve
operator|instanceof
name|BucketNumExpression
condition|)
block|{
name|bucketExpr
operator|=
operator|(
name|BucketNumExpression
operator|)
name|ve
expr_stmt|;
break|break;
block|}
block|}
block|}
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
try|try
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|batchCounter
operator|++
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" empty"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
operator|!
name|isKeyInitialized
condition|)
block|{
name|isKeyInitialized
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|isEmptyKey
condition|)
block|{
name|initializeEmptyKey
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform any key expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkKeyExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkKeyExpressions
control|)
block|{
comment|// Handle _bucket_number
if|if
condition|(
name|ve
operator|instanceof
name|BucketNumExpression
condition|)
block|{
continue|continue;
comment|// Evaluate per row
block|}
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform any value expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkValueExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkValueExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform any bucket expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkBucketExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkBucketExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform any partition expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkPartitionExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkPartitionExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
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
specifier|final
name|int
name|size
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|isEmptyBuckets
condition|)
block|{
comment|// EmptyBuckets = true
if|if
condition|(
name|isEmptyPartitions
condition|)
block|{
comment|// isEmptyPartition = true
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
name|hashCode
init|=
name|nonPartitionRandom
operator|.
name|nextInt
argument_list|()
decl_stmt|;
name|postProcess
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|tag
argument_list|,
name|hashCode
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// isEmptyPartition = false
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
name|partitionVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|partitionFieldValues
argument_list|)
expr_stmt|;
specifier|final
name|int
name|hashCode
init|=
name|hashFunc
operator|.
name|apply
argument_list|(
name|partitionFieldValues
argument_list|,
name|partitionObjectInspectors
argument_list|)
decl_stmt|;
name|postProcess
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|tag
argument_list|,
name|hashCode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// EmptyBuckets = false
if|if
condition|(
name|isEmptyPartitions
condition|)
block|{
comment|// isEmptyPartition = true
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
name|bucketVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|bucketFieldValues
argument_list|)
expr_stmt|;
specifier|final
name|int
name|bucketNum
init|=
name|ObjectInspectorUtils
operator|.
name|getBucketNumber
argument_list|(
name|hashFunc
operator|.
name|apply
argument_list|(
name|bucketFieldValues
argument_list|,
name|bucketObjectInspectors
argument_list|)
argument_list|,
name|numBuckets
argument_list|)
decl_stmt|;
specifier|final
name|int
name|hashCode
init|=
name|nonPartitionRandom
operator|.
name|nextInt
argument_list|()
operator|*
literal|31
operator|+
name|bucketNum
decl_stmt|;
if|if
condition|(
name|bucketExpr
operator|!=
literal|null
condition|)
block|{
name|evaluateBucketExpr
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|bucketNum
argument_list|)
expr_stmt|;
block|}
name|postProcess
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|tag
argument_list|,
name|hashCode
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// isEmptyPartition = false
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
name|partitionVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|partitionFieldValues
argument_list|)
expr_stmt|;
name|bucketVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|bucketFieldValues
argument_list|)
expr_stmt|;
specifier|final
name|int
name|bucketNum
init|=
name|ObjectInspectorUtils
operator|.
name|getBucketNumber
argument_list|(
name|hashFunc
operator|.
name|apply
argument_list|(
name|bucketFieldValues
argument_list|,
name|bucketObjectInspectors
argument_list|)
argument_list|,
name|numBuckets
argument_list|)
decl_stmt|;
specifier|final
name|int
name|hashCode
init|=
name|hashFunc
operator|.
name|apply
argument_list|(
name|partitionFieldValues
argument_list|,
name|partitionObjectInspectors
argument_list|)
operator|*
literal|31
operator|+
name|bucketNum
decl_stmt|;
if|if
condition|(
name|bucketExpr
operator|!=
literal|null
condition|)
block|{
name|evaluateBucketExpr
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|bucketNum
argument_list|)
expr_stmt|;
block|}
name|postProcess
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|tag
argument_list|,
name|hashCode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|processKey
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|isEmptyKey
condition|)
return|return;
try|try
block|{
name|keyBinarySortableSerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
name|keyVectorSerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
comment|// One serialized key for 1 or more rows for the duplicate keys.
specifier|final
name|int
name|keyLength
init|=
name|keyOutput
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
operator|||
name|reduceSkipTag
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|keyOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyWritable
operator|.
name|setSize
argument_list|(
name|keyLength
operator|+
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|keyOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|keyLength
index|]
operator|=
name|reduceTagByte
expr_stmt|;
block|}
name|keyWritable
operator|.
name|setDistKeyLength
argument_list|(
name|keyLength
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|processValue
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|isEmptyValue
condition|)
return|return;
try|try
block|{
name|valueLazyBinarySerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
name|valueBytesWritable
operator|.
name|set
argument_list|(
name|valueOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueOutput
operator|.
name|getLength
argument_list|()
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|postProcess
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|,
name|int
name|tag
parameter_list|,
name|int
name|hashCode
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|processKey
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|tag
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|hashCode
argument_list|)
expr_stmt|;
name|processValue
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueBytesWritable
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
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

