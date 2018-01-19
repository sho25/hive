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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|io
operator|.
name|HiveKey
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_class
specifier|public
class|class
name|PTFTopNHash
extends|extends
name|TopNHash
block|{
specifier|protected
name|float
name|memUsage
decl_stmt|;
specifier|protected
name|boolean
name|isMapGroupBy
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Key
argument_list|,
name|TopNHash
argument_list|>
name|partitionHeaps
decl_stmt|;
specifier|private
name|TopNHash
name|largestPartition
decl_stmt|;
specifier|private
name|boolean
name|prevIndexPartIsNull
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Integer
argument_list|>
name|indexesWithNullPartKey
decl_stmt|;
specifier|private
name|OperatorDesc
name|conf
decl_stmt|;
specifier|private
name|Configuration
name|hconf
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|int
name|topN
parameter_list|,
name|float
name|memUsage
parameter_list|,
name|boolean
name|isMapGroupBy
parameter_list|,
name|BinaryCollector
name|collector
parameter_list|,
specifier|final
name|OperatorDesc
name|conf
parameter_list|,
specifier|final
name|Configuration
name|hconf
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|topN
argument_list|,
name|memUsage
argument_list|,
name|isMapGroupBy
argument_list|,
name|collector
argument_list|,
name|conf
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|this
operator|.
name|isMapGroupBy
operator|=
name|isMapGroupBy
expr_stmt|;
name|this
operator|.
name|memUsage
operator|=
name|memUsage
expr_stmt|;
name|partitionHeaps
operator|=
operator|new
name|HashMap
argument_list|<
name|Key
argument_list|,
name|TopNHash
argument_list|>
argument_list|()
expr_stmt|;
name|indexesWithNullPartKey
operator|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|tryStoreKey
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|boolean
name|partColsIsNull
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|prevIndexPartIsNull
operator|=
name|partColsIsNull
expr_stmt|;
return|return
name|_tryStoreKey
argument_list|(
name|key
argument_list|,
name|partColsIsNull
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|private
name|void
name|updateLargest
parameter_list|(
name|TopNHash
name|p
parameter_list|)
block|{
if|if
condition|(
name|largestPartition
operator|==
literal|null
operator|||
name|largestPartition
operator|.
name|usage
operator|<
name|p
operator|.
name|usage
condition|)
block|{
name|largestPartition
operator|=
name|p
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|findLargest
parameter_list|()
block|{
for|for
control|(
name|TopNHash
name|p
range|:
name|partitionHeaps
operator|.
name|values
argument_list|()
control|)
block|{
name|updateLargest
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|_tryStoreKey
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|boolean
name|partColsIsNull
parameter_list|,
name|int
name|batchIndex
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|isEnabled
condition|)
block|{
return|return
name|FORWARD
return|;
comment|// short-circuit quickly - forward all rows
block|}
if|if
condition|(
name|topN
operator|==
literal|0
condition|)
block|{
return|return
name|EXCLUDE
return|;
comment|// short-circuit quickly - eat all rows
block|}
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|partColsIsNull
argument_list|,
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
if|if
condition|(
name|partHeap
operator|==
literal|null
condition|)
block|{
name|partHeap
operator|=
operator|new
name|TopNHash
argument_list|()
expr_stmt|;
name|partHeap
operator|.
name|initialize
argument_list|(
name|topN
argument_list|,
name|memUsage
argument_list|,
name|isMapGroupBy
argument_list|,
name|collector
argument_list|,
name|conf
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
if|if
condition|(
name|batchIndex
operator|>=
literal|0
condition|)
block|{
name|partHeap
operator|.
name|startVectorizedBatch
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|partitionHeaps
operator|.
name|put
argument_list|(
name|pk
argument_list|,
name|partHeap
argument_list|)
expr_stmt|;
block|}
name|usage
operator|=
name|usage
operator|-
name|partHeap
operator|.
name|usage
expr_stmt|;
name|int
name|r
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|batchIndex
operator|>=
literal|0
condition|)
block|{
name|partHeap
operator|.
name|tryStoreVectorizedKey
argument_list|(
name|key
argument_list|,
literal|false
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|r
operator|=
name|partHeap
operator|.
name|tryStoreKey
argument_list|(
name|key
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|usage
operator|=
name|usage
operator|+
name|partHeap
operator|.
name|usage
expr_stmt|;
name|updateLargest
argument_list|(
name|partHeap
argument_list|)
expr_stmt|;
if|if
condition|(
name|usage
operator|>
name|threshold
condition|)
block|{
name|usage
operator|-=
name|largestPartition
operator|.
name|usage
expr_stmt|;
name|largestPartition
operator|.
name|flush
argument_list|()
expr_stmt|;
name|usage
operator|+=
name|largestPartition
operator|.
name|usage
expr_stmt|;
name|largestPartition
operator|=
literal|null
expr_stmt|;
name|findLargest
argument_list|()
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
specifier|public
name|void
name|storeValue
parameter_list|(
name|int
name|index
parameter_list|,
name|int
name|hashCode
parameter_list|,
name|BytesWritable
name|value
parameter_list|,
name|boolean
name|vectorized
parameter_list|)
block|{
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|prevIndexPartIsNull
argument_list|,
name|hashCode
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
name|usage
operator|=
name|usage
operator|-
name|partHeap
operator|.
name|usage
expr_stmt|;
name|partHeap
operator|.
name|storeValue
argument_list|(
name|index
argument_list|,
name|hashCode
argument_list|,
name|value
argument_list|,
name|vectorized
argument_list|)
expr_stmt|;
name|usage
operator|=
name|usage
operator|+
name|partHeap
operator|.
name|usage
expr_stmt|;
name|updateLargest
argument_list|(
name|partHeap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isEnabled
operator|||
operator|(
name|topN
operator|==
literal|0
operator|)
condition|)
return|return;
for|for
control|(
name|TopNHash
name|partHash
range|:
name|partitionHeaps
operator|.
name|values
argument_list|()
control|)
block|{
name|partHash
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|startVectorizedBatch
parameter_list|(
name|int
name|size
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
operator|!
name|isEnabled
condition|)
block|{
return|return
name|FORWARD
return|;
comment|// short-circuit quickly - forward all rows
block|}
elseif|else
if|if
condition|(
name|topN
operator|==
literal|0
condition|)
block|{
return|return
name|EXCLUDE
return|;
comment|// short-circuit quickly - eat all rows
block|}
for|for
control|(
name|TopNHash
name|partHash
range|:
name|partitionHeaps
operator|.
name|values
argument_list|()
control|)
block|{
name|usage
operator|=
name|usage
operator|-
name|partHash
operator|.
name|usage
expr_stmt|;
name|partHash
operator|.
name|startVectorizedBatch
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|usage
operator|=
name|usage
operator|+
name|partHash
operator|.
name|usage
expr_stmt|;
name|updateLargest
argument_list|(
name|partHash
argument_list|)
expr_stmt|;
block|}
name|batchSize
operator|=
name|size
expr_stmt|;
if|if
condition|(
name|batchIndexToResult
operator|==
literal|null
operator|||
name|batchIndexToResult
operator|.
name|length
operator|<
name|batchSize
condition|)
block|{
name|batchIndexToResult
operator|=
operator|new
name|int
index|[
name|Math
operator|.
name|max
argument_list|(
name|batchSize
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
index|]
expr_stmt|;
block|}
name|indexesWithNullPartKey
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|public
name|void
name|tryStoreVectorizedKey
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|boolean
name|partColsIsNull
parameter_list|,
name|int
name|batchIndex
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|_tryStoreKey
argument_list|(
name|key
argument_list|,
name|partColsIsNull
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|partColsIsNull
condition|)
block|{
name|indexesWithNullPartKey
operator|.
name|add
argument_list|(
name|batchIndex
argument_list|)
expr_stmt|;
block|}
name|batchIndexToResult
index|[
name|batchIndex
index|]
operator|=
name|key
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getVectorizedBatchResult
parameter_list|(
name|int
name|batchIndex
parameter_list|)
block|{
name|prevIndexPartIsNull
operator|=
name|indexesWithNullPartKey
operator|.
name|contains
argument_list|(
name|batchIndex
argument_list|)
expr_stmt|;
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|prevIndexPartIsNull
argument_list|,
name|batchIndexToResult
index|[
name|batchIndex
index|]
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
return|return
name|partHeap
operator|.
name|getVectorizedBatchResult
argument_list|(
name|batchIndex
argument_list|)
return|;
block|}
specifier|public
name|HiveKey
name|getVectorizedKeyToForward
parameter_list|(
name|int
name|batchIndex
parameter_list|)
block|{
name|prevIndexPartIsNull
operator|=
name|indexesWithNullPartKey
operator|.
name|contains
argument_list|(
name|batchIndex
argument_list|)
expr_stmt|;
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|prevIndexPartIsNull
argument_list|,
name|batchIndexToResult
index|[
name|batchIndex
index|]
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
return|return
name|partHeap
operator|.
name|getVectorizedKeyToForward
argument_list|(
name|batchIndex
argument_list|)
return|;
block|}
specifier|public
name|int
name|getVectorizedKeyDistLength
parameter_list|(
name|int
name|batchIndex
parameter_list|)
block|{
name|prevIndexPartIsNull
operator|=
name|indexesWithNullPartKey
operator|.
name|contains
argument_list|(
name|batchIndex
argument_list|)
expr_stmt|;
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|prevIndexPartIsNull
argument_list|,
name|batchIndexToResult
index|[
name|batchIndex
index|]
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
return|return
name|partHeap
operator|.
name|getVectorizedKeyDistLength
argument_list|(
name|batchIndex
argument_list|)
return|;
block|}
specifier|public
name|int
name|getVectorizedKeyHashCode
parameter_list|(
name|int
name|batchIndex
parameter_list|)
block|{
name|prevIndexPartIsNull
operator|=
name|indexesWithNullPartKey
operator|.
name|contains
argument_list|(
name|batchIndex
argument_list|)
expr_stmt|;
name|Key
name|pk
init|=
operator|new
name|Key
argument_list|(
name|prevIndexPartIsNull
argument_list|,
name|batchIndexToResult
index|[
name|batchIndex
index|]
argument_list|)
decl_stmt|;
name|TopNHash
name|partHeap
init|=
name|partitionHeaps
operator|.
name|get
argument_list|(
name|pk
argument_list|)
decl_stmt|;
return|return
name|partHeap
operator|.
name|getVectorizedKeyHashCode
argument_list|(
name|batchIndex
argument_list|)
return|;
block|}
specifier|static
class|class
name|Key
block|{
name|boolean
name|isNull
decl_stmt|;
name|int
name|hashCode
decl_stmt|;
specifier|public
name|Key
parameter_list|(
name|boolean
name|isNull
parameter_list|,
name|int
name|hashCode
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|isNull
operator|=
name|isNull
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|hashCode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Key
name|other
init|=
operator|(
name|Key
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|hashCode
operator|!=
name|other
operator|.
name|hashCode
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|isNull
operator|!=
name|other
operator|.
name|isNull
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|hashCode
operator|+
literal|","
operator|+
name|isNull
return|;
block|}
block|}
block|}
end_class

end_unit

