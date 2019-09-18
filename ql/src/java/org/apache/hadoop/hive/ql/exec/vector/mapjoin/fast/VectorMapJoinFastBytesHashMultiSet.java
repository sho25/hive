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
name|mapjoin
operator|.
name|fast
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinBytesHashMultiSet
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
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashMultiSetResult
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HashCodeUtil
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
comment|/*  * An bytes key hash multi-set optimized for vector map join.  *  * This is the abstract base for the multi-key and string bytes key hash multi-set implementations.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastBytesHashMultiSet
extends|extends
name|VectorMapJoinFastBytesHashTable
implements|implements
name|VectorMapJoinBytesHashMultiSet
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
name|VectorMapJoinFastBytesHashMultiSet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|VectorMapJoinFastBytesHashMultiSetStore
name|hashMultiSetStore
decl_stmt|;
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashMultiSetResult
name|createHashMultiSetResult
parameter_list|()
block|{
return|return
operator|new
name|VectorMapJoinFastBytesHashMultiSetStore
operator|.
name|HashMultiSetResult
argument_list|()
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
block|{
if|if
condition|(
name|checkResize
argument_list|()
condition|)
block|{
name|expandAndRehash
argument_list|()
expr_stmt|;
block|}
name|long
name|hashCode
init|=
name|HashCodeUtil
operator|.
name|murmurHash
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
decl_stmt|;
name|int
name|intHashCode
init|=
operator|(
name|int
operator|)
name|hashCode
decl_stmt|;
name|int
name|slot
init|=
operator|(
name|intHashCode
operator|&
name|logicalHashBucketMask
operator|)
decl_stmt|;
name|long
name|probeSlot
init|=
name|slot
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|boolean
name|isNewKey
decl_stmt|;
name|long
name|refWord
decl_stmt|;
specifier|final
name|long
name|partialHashCode
init|=
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|extractPartialHashCode
argument_list|(
name|hashCode
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|refWord
operator|=
name|slots
index|[
name|slot
index|]
expr_stmt|;
if|if
condition|(
name|refWord
operator|==
literal|0
condition|)
block|{
name|isNewKey
operator|=
literal|true
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|getPartialHashCodeFromRefWord
argument_list|(
name|refWord
argument_list|)
operator|==
name|partialHashCode
operator|&&
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|equalKey
argument_list|(
name|refWord
argument_list|,
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|,
name|writeBuffers
argument_list|,
name|unsafeReadPos
argument_list|)
condition|)
block|{
name|isNewKey
operator|=
literal|false
expr_stmt|;
break|break;
block|}
operator|++
name|metricPutConflict
expr_stmt|;
comment|// Some other key (collision) - keep probing.
name|probeSlot
operator|+=
operator|(
operator|++
name|i
operator|)
expr_stmt|;
name|slot
operator|=
call|(
name|int
call|)
argument_list|(
name|probeSlot
operator|&
name|logicalHashBucketMask
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|largestNumberOfSteps
operator|<
name|i
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
literal|"Probed "
operator|+
name|i
operator|+
literal|" slots (the longest so far) to find space"
argument_list|)
expr_stmt|;
block|}
name|largestNumberOfSteps
operator|=
name|i
expr_stmt|;
comment|// debugDumpKeyProbe(keyOffset, keyLength, hashCode, slot);
block|}
if|if
condition|(
name|isNewKey
condition|)
block|{
name|slots
index|[
name|slot
index|]
operator|=
name|hashMultiSetStore
operator|.
name|addFirst
argument_list|(
name|partialHashCode
argument_list|,
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|keysAssigned
operator|++
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|long
name|newRefWord
init|=
name|hashMultiSetStore
operator|.
name|bumpCount
argument_list|(
name|refWord
argument_list|,
name|unsafeReadPos
argument_list|)
decl_stmt|;
if|if
condition|(
name|newRefWord
operator|!=
name|refWord
condition|)
block|{
name|slots
index|[
name|slot
index|]
operator|=
name|newRefWord
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|JoinUtil
operator|.
name|JoinResult
name|contains
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|VectorMapJoinHashMultiSetResult
name|hashMultiSetResult
parameter_list|)
block|{
name|VectorMapJoinFastBytesHashMultiSetStore
operator|.
name|HashMultiSetResult
name|fastHashMultiSetResult
init|=
operator|(
name|VectorMapJoinFastBytesHashMultiSetStore
operator|.
name|HashMultiSetResult
operator|)
name|hashMultiSetResult
decl_stmt|;
name|fastHashMultiSetResult
operator|.
name|forget
argument_list|()
expr_stmt|;
name|long
name|hashCode
init|=
name|HashCodeUtil
operator|.
name|murmurHash
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
decl_stmt|;
name|doHashMultiSetContains
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|,
name|hashCode
argument_list|,
name|fastHashMultiSetResult
argument_list|)
expr_stmt|;
return|return
name|fastHashMultiSetResult
operator|.
name|joinResult
argument_list|()
return|;
block|}
specifier|protected
specifier|final
name|void
name|doHashMultiSetContains
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|long
name|hashCode
parameter_list|,
name|VectorMapJoinFastBytesHashMultiSetStore
operator|.
name|HashMultiSetResult
name|fastHashMultiSetResult
parameter_list|)
block|{
name|int
name|intHashCode
init|=
operator|(
name|int
operator|)
name|hashCode
decl_stmt|;
name|int
name|slot
init|=
operator|(
name|intHashCode
operator|&
name|logicalHashBucketMask
operator|)
decl_stmt|;
name|long
name|probeSlot
init|=
name|slot
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
specifier|final
name|long
name|partialHashCode
init|=
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|extractPartialHashCode
argument_list|(
name|hashCode
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
specifier|final
name|long
name|refWord
init|=
name|slots
index|[
name|slot
index|]
decl_stmt|;
if|if
condition|(
name|refWord
operator|==
literal|0
condition|)
block|{
comment|// Given that we do not delete, an empty slot means no match.
return|return;
block|}
elseif|else
if|if
condition|(
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|getPartialHashCodeFromRefWord
argument_list|(
name|refWord
argument_list|)
operator|==
name|partialHashCode
condition|)
block|{
comment|// Finally, verify the key bytes match and remember the set membership count in
comment|// fastHashMultiSetResult.
name|fastHashMultiSetResult
operator|.
name|setKey
argument_list|(
name|hashMultiSetStore
argument_list|,
name|refWord
argument_list|)
expr_stmt|;
if|if
condition|(
name|fastHashMultiSetResult
operator|.
name|equalKey
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
condition|)
block|{
name|fastHashMultiSetResult
operator|.
name|setContains
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
comment|// Some other key (collision) - keep probing.
name|probeSlot
operator|+=
operator|(
operator|++
name|i
operator|)
expr_stmt|;
if|if
condition|(
name|i
operator|>
name|largestNumberOfSteps
condition|)
block|{
comment|// We know we never went that far when we were inserting.
return|return;
block|}
name|slot
operator|=
call|(
name|int
call|)
argument_list|(
name|probeSlot
operator|&
name|logicalHashBucketMask
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|VectorMapJoinFastBytesHashMultiSet
parameter_list|(
name|boolean
name|isFullOuter
parameter_list|,
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|int
name|writeBuffersSize
parameter_list|,
name|long
name|estimatedKeyCount
parameter_list|)
block|{
name|super
argument_list|(
name|isFullOuter
argument_list|,
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|writeBuffersSize
argument_list|,
name|estimatedKeyCount
argument_list|)
expr_stmt|;
name|hashMultiSetStore
operator|=
operator|new
name|VectorMapJoinFastBytesHashMultiSetStore
argument_list|(
name|writeBuffersSize
argument_list|)
expr_stmt|;
name|writeBuffers
operator|=
name|hashMultiSetStore
operator|.
name|getWriteBuffers
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEstimatedMemorySize
parameter_list|()
block|{
name|long
name|size
init|=
name|super
operator|.
name|getEstimatedMemorySize
argument_list|()
decl_stmt|;
name|size
operator|+=
name|hashMultiSetStore
operator|.
name|getEstimatedMemorySize
argument_list|()
expr_stmt|;
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

