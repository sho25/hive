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
name|VectorMapJoinHashMap
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
name|VectorMapJoinLongHashMap
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
name|VectorMapJoinLongHashTable
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
name|VectorMapJoinDesc
operator|.
name|HashTableKeyType
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
name|BinarySortableDeserializeRead
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
name|TypeInfoFactory
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
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|KeyValueReader
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
comment|/*  * An single long value map optimized for vector map join.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastLongHashTable
extends|extends
name|VectorMapJoinFastHashTable
implements|implements
name|VectorMapJoinLongHashTable
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorMapJoinFastLongHashTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|boolean
name|isLogDebugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
specifier|private
name|HashTableKeyType
name|hashTableKeyType
decl_stmt|;
specifier|private
name|boolean
name|isOuterJoin
decl_stmt|;
specifier|private
name|BinarySortableDeserializeRead
name|keyBinarySortableDeserializeRead
decl_stmt|;
specifier|private
name|boolean
name|useMinMax
decl_stmt|;
specifier|private
name|long
name|min
decl_stmt|;
specifier|private
name|long
name|max
decl_stmt|;
specifier|private
name|BytesWritable
name|testValueBytesWritable
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|useMinMax
parameter_list|()
block|{
return|return
name|useMinMax
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|min
parameter_list|()
block|{
return|return
name|min
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|max
parameter_list|()
block|{
return|return
name|max
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putRow
parameter_list|(
name|BytesWritable
name|currentKey
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|byte
index|[]
name|keyBytes
init|=
name|currentKey
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|currentKey
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|keyBinarySortableDeserializeRead
operator|.
name|set
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyBinarySortableDeserializeRead
operator|.
name|readCheckNull
argument_list|()
condition|)
block|{
if|if
condition|(
name|isOuterJoin
condition|)
block|{
return|return;
block|}
else|else
block|{
comment|// For inner join, we expect all NULL values to have been filtered out before now.
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unexpected NULL in map join small table"
argument_list|)
throw|;
block|}
block|}
name|long
name|key
init|=
name|VectorMapJoinFastLongHashUtil
operator|.
name|deserializeLongKey
argument_list|(
name|keyBinarySortableDeserializeRead
argument_list|,
name|hashTableKeyType
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|key
argument_list|,
name|currentValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|putRow
parameter_list|(
name|long
name|currentKey
parameter_list|,
name|byte
index|[]
name|currentValue
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
if|if
condition|(
name|testValueBytesWritable
operator|==
literal|null
condition|)
block|{
name|testValueBytesWritable
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
block|}
name|testValueBytesWritable
operator|.
name|set
argument_list|(
name|currentValue
argument_list|,
literal|0
argument_list|,
name|currentValue
operator|.
name|length
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|currentKey
argument_list|,
name|testValueBytesWritable
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|void
name|assignSlot
parameter_list|(
name|int
name|slot
parameter_list|,
name|long
name|key
parameter_list|,
name|boolean
name|isNewKey
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
function_decl|;
specifier|public
name|void
name|add
parameter_list|(
name|long
name|key
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
block|{
if|if
condition|(
name|resizeThreshold
operator|<=
name|keysAssigned
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
name|calculateLongHashCode
argument_list|(
name|key
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
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|pairIndex
init|=
literal|2
operator|*
name|slot
decl_stmt|;
name|long
name|valueRef
init|=
name|slotPairs
index|[
name|pairIndex
index|]
decl_stmt|;
if|if
condition|(
name|valueRef
operator|==
literal|0
condition|)
block|{
comment|// LOG.debug("VectorMapJoinFastLongHashTable add key " + key + " slot " + slot + " pairIndex " + pairIndex + " empty slot (i = " + i + ")");
name|isNewKey
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|long
name|tableKey
init|=
name|slotPairs
index|[
name|pairIndex
operator|+
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|key
operator|==
name|tableKey
condition|)
block|{
comment|// LOG.debug("VectorMapJoinFastLongHashTable add key " + key + " slot " + slot + " pairIndex " + pairIndex + " found key (i = " + i + ")");
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
name|isLogDebugEnabled
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
comment|// LOG.debug("VectorMapJoinFastLongHashTable add slot " + slot + " hashCode " + Long.toHexString(hashCode));
name|assignSlot
argument_list|(
name|slot
argument_list|,
name|key
argument_list|,
name|isNewKey
argument_list|,
name|currentValue
argument_list|)
expr_stmt|;
if|if
condition|(
name|isNewKey
condition|)
block|{
name|keysAssigned
operator|++
expr_stmt|;
if|if
condition|(
name|useMinMax
condition|)
block|{
if|if
condition|(
name|key
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|key
expr_stmt|;
block|}
if|if
condition|(
name|key
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|key
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|expandAndRehash
parameter_list|()
block|{
name|int
name|newLogicalHashBucketCount
init|=
name|logicalHashBucketCount
operator|*
literal|2
decl_stmt|;
name|int
name|newLogicalHashBucketMask
init|=
name|newLogicalHashBucketCount
operator|-
literal|1
decl_stmt|;
name|int
name|newMetricPutConflict
init|=
literal|0
decl_stmt|;
name|int
name|newLargestNumberOfSteps
init|=
literal|0
decl_stmt|;
name|int
name|newSlotPairArraySize
init|=
name|newLogicalHashBucketCount
operator|*
literal|2
decl_stmt|;
name|long
index|[]
name|newSlotPairs
init|=
operator|new
name|long
index|[
name|newSlotPairArraySize
index|]
decl_stmt|;
for|for
control|(
name|int
name|slot
init|=
literal|0
init|;
name|slot
operator|<
name|logicalHashBucketCount
condition|;
name|slot
operator|++
control|)
block|{
name|int
name|pairIndex
init|=
name|slot
operator|*
literal|2
decl_stmt|;
name|long
name|valueRef
init|=
name|slotPairs
index|[
name|pairIndex
index|]
decl_stmt|;
if|if
condition|(
name|valueRef
operator|!=
literal|0
condition|)
block|{
name|long
name|tableKey
init|=
name|slotPairs
index|[
name|pairIndex
operator|+
literal|1
index|]
decl_stmt|;
comment|// Copy to new slot table.
name|long
name|hashCode
init|=
name|HashCodeUtil
operator|.
name|calculateLongHashCode
argument_list|(
name|tableKey
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
name|newSlot
init|=
name|intHashCode
operator|&
name|newLogicalHashBucketMask
decl_stmt|;
name|long
name|newProbeSlot
init|=
name|newSlot
decl_stmt|;
name|int
name|newPairIndex
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|newPairIndex
operator|=
name|newSlot
operator|*
literal|2
expr_stmt|;
name|long
name|newValueRef
init|=
name|newSlotPairs
index|[
name|newPairIndex
index|]
decl_stmt|;
if|if
condition|(
name|newValueRef
operator|==
literal|0
condition|)
block|{
break|break;
block|}
operator|++
name|newMetricPutConflict
expr_stmt|;
comment|// Some other key (collision) - keep probing.
name|newProbeSlot
operator|+=
operator|(
operator|++
name|i
operator|)
expr_stmt|;
name|newSlot
operator|=
call|(
name|int
call|)
argument_list|(
name|newProbeSlot
operator|&
name|newLogicalHashBucketMask
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newLargestNumberOfSteps
operator|<
name|i
condition|)
block|{
if|if
condition|(
name|isLogDebugEnabled
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
name|newLargestNumberOfSteps
operator|=
name|i
expr_stmt|;
comment|// debugDumpKeyProbe(keyOffset, keyLength, hashCode, slot);
block|}
comment|// Use old value reference word.
comment|// LOG.debug("VectorMapJoinFastLongHashTable expandAndRehash key " + tableKey + " slot " + newSlot + " newPairIndex " + newPairIndex + " empty slot (i = " + i + ")");
name|newSlotPairs
index|[
name|newPairIndex
index|]
operator|=
name|valueRef
expr_stmt|;
name|newSlotPairs
index|[
name|newPairIndex
operator|+
literal|1
index|]
operator|=
name|tableKey
expr_stmt|;
block|}
block|}
name|slotPairs
operator|=
name|newSlotPairs
expr_stmt|;
name|logicalHashBucketCount
operator|=
name|newLogicalHashBucketCount
expr_stmt|;
name|logicalHashBucketMask
operator|=
name|newLogicalHashBucketMask
expr_stmt|;
name|metricPutConflict
operator|=
name|newMetricPutConflict
expr_stmt|;
name|largestNumberOfSteps
operator|=
name|newLargestNumberOfSteps
expr_stmt|;
name|resizeThreshold
operator|=
call|(
name|int
call|)
argument_list|(
name|logicalHashBucketCount
operator|*
name|loadFactor
argument_list|)
expr_stmt|;
name|metricExpands
operator|++
expr_stmt|;
comment|// LOG.debug("VectorMapJoinFastLongHashTable expandAndRehash new logicalHashBucketCount " + logicalHashBucketCount + " resizeThreshold " + resizeThreshold + " metricExpands " + metricExpands);
block|}
specifier|protected
name|long
name|findReadSlot
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|hashCode
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
name|intHashCode
operator|&
name|logicalHashBucketMask
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
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|pairIndex
init|=
literal|2
operator|*
name|slot
decl_stmt|;
name|long
name|valueRef
init|=
name|slotPairs
index|[
name|pairIndex
index|]
decl_stmt|;
if|if
condition|(
name|valueRef
operator|==
literal|0
condition|)
block|{
comment|// Given that we do not delete, an empty slot means no match.
comment|// LOG.debug("VectorMapJoinFastLongHashTable findReadSlot key " + key + " slot " + slot + " pairIndex " + pairIndex + " empty slot (i = " + i + ")");
return|return
operator|-
literal|1
return|;
block|}
name|long
name|tableKey
init|=
name|slotPairs
index|[
name|pairIndex
operator|+
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|key
operator|==
name|tableKey
condition|)
block|{
comment|// LOG.debug("VectorMapJoinFastLongHashTable findReadSlot key " + key + " slot " + slot + " pairIndex " + pairIndex + " found key (i = " + i + ")");
return|return
name|slotPairs
index|[
name|pairIndex
index|]
return|;
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
comment|// LOG.debug("VectorMapJoinFastLongHashTable findReadSlot returning not found");
comment|// We know we never went that far when we were inserting.
comment|// LOG.debug("VectorMapJoinFastLongHashTable findReadSlot key " + key + " slot " + slot + " pairIndex " + pairIndex + " largestNumberOfSteps " + largestNumberOfSteps + " (i = " + i + ")");
return|return
operator|-
literal|1
return|;
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
comment|/*    * The hash table slots.  For a long key hash table, each slot is 2 longs and the array is    * 2X sized.    *    * The slot pair is 1) a non-zero reference word to the first value bytes and 2) the long value.    */
specifier|protected
name|long
index|[]
name|slotPairs
decl_stmt|;
specifier|private
name|void
name|allocateBucketArray
parameter_list|()
block|{
name|int
name|slotPairArraySize
init|=
literal|2
operator|*
name|logicalHashBucketCount
decl_stmt|;
name|slotPairs
operator|=
operator|new
name|long
index|[
name|slotPairArraySize
index|]
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinFastLongHashTable
parameter_list|(
name|boolean
name|minMaxEnabled
parameter_list|,
name|boolean
name|isOuterJoin
parameter_list|,
name|HashTableKeyType
name|hashTableKeyType
parameter_list|,
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|int
name|writeBuffersSize
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|writeBuffersSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|isOuterJoin
operator|=
name|isOuterJoin
expr_stmt|;
name|this
operator|.
name|hashTableKeyType
operator|=
name|hashTableKeyType
expr_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
block|{
name|TypeInfoFactory
operator|.
name|longTypeInfo
block|}
decl_stmt|;
name|keyBinarySortableDeserializeRead
operator|=
operator|new
name|BinarySortableDeserializeRead
argument_list|(
name|primitiveTypeInfos
argument_list|)
expr_stmt|;
name|allocateBucketArray
argument_list|()
expr_stmt|;
name|useMinMax
operator|=
name|minMaxEnabled
expr_stmt|;
name|min
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|max
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
block|}
block|}
end_class

end_unit

