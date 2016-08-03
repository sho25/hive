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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinBytesHashTable
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
name|serde2
operator|.
name|WriteBuffers
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
comment|/*  * An single byte array value hash map optimized for vector map join.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastBytesHashTable
extends|extends
name|VectorMapJoinFastHashTable
implements|implements
name|VectorMapJoinBytesHashTable
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
name|VectorMapJoinFastBytesHashTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isLogDebugEnabled
init|=
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
decl_stmt|;
specifier|protected
name|VectorMapJoinFastKeyStore
name|keyStore
decl_stmt|;
specifier|protected
name|BytesWritable
name|testKeyBytesWritable
decl_stmt|;
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
comment|// No deserialization of key(s) here -- just get reference to bytes.
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
name|add
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|,
name|currentValue
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
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|tripleIndex
init|=
literal|3
operator|*
name|slot
decl_stmt|;
if|if
condition|(
name|slotTriples
index|[
name|tripleIndex
index|]
operator|==
literal|0
condition|)
block|{
comment|// LOG.debug("VectorMapJoinFastBytesHashMap findWriteSlot slot " + slot + " tripleIndex " + tripleIndex + " empty");
name|isNewKey
operator|=
literal|true
expr_stmt|;
empty_stmt|;
break|break;
block|}
if|if
condition|(
name|hashCode
operator|==
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|1
index|]
operator|&&
name|keyStore
operator|.
name|unsafeEqualKey
argument_list|(
name|slotTriples
index|[
name|tripleIndex
index|]
argument_list|,
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
condition|)
block|{
comment|// LOG.debug("VectorMapJoinFastBytesHashMap findWriteSlot slot " + slot + " tripleIndex " + tripleIndex + " existing");
name|isNewKey
operator|=
literal|false
expr_stmt|;
break|break;
block|}
comment|// TODO
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
name|assignSlot
argument_list|(
name|slot
argument_list|,
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|,
name|hashCode
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
name|newSlotTripleArraySize
init|=
name|newLogicalHashBucketCount
operator|*
literal|3
decl_stmt|;
name|long
index|[]
name|newSlotTriples
init|=
operator|new
name|long
index|[
name|newSlotTripleArraySize
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
name|tripleIndex
init|=
name|slot
operator|*
literal|3
decl_stmt|;
name|long
name|keyRef
init|=
name|slotTriples
index|[
name|tripleIndex
index|]
decl_stmt|;
if|if
condition|(
name|keyRef
operator|!=
literal|0
condition|)
block|{
name|long
name|hashCode
init|=
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|1
index|]
decl_stmt|;
name|long
name|valueRef
init|=
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|2
index|]
decl_stmt|;
comment|// Copy to new slot table.
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
name|newTripleIndex
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
name|newTripleIndex
operator|=
name|newSlot
operator|*
literal|3
expr_stmt|;
name|long
name|newKeyRef
init|=
name|newSlotTriples
index|[
name|newTripleIndex
index|]
decl_stmt|;
if|if
condition|(
name|newKeyRef
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
name|newSlotTriples
index|[
name|newTripleIndex
index|]
operator|=
name|keyRef
expr_stmt|;
name|newSlotTriples
index|[
name|newTripleIndex
operator|+
literal|1
index|]
operator|=
name|hashCode
expr_stmt|;
name|newSlotTriples
index|[
name|newTripleIndex
operator|+
literal|2
index|]
operator|=
name|valueRef
expr_stmt|;
block|}
block|}
name|slotTriples
operator|=
name|newSlotTriples
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
specifier|final
name|long
name|findReadSlot
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
name|WriteBuffers
operator|.
name|Position
name|readPos
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
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|tripleIndex
init|=
name|slot
operator|*
literal|3
decl_stmt|;
comment|// LOG.debug("VectorMapJoinFastBytesHashMap findReadSlot slot keyRefWord " + Long.toHexString(slotTriples[tripleIndex]) + " hashCode " + Long.toHexString(hashCode) + " entry hashCode " + Long.toHexString(slotTriples[tripleIndex + 1]) + " valueRefWord " + Long.toHexString(slotTriples[tripleIndex + 2]));
if|if
condition|(
name|slotTriples
index|[
name|tripleIndex
index|]
operator|!=
literal|0
operator|&&
name|hashCode
operator|==
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|1
index|]
condition|)
block|{
comment|// Finally, verify the key bytes match.
if|if
condition|(
name|keyStore
operator|.
name|equalKey
argument_list|(
name|slotTriples
index|[
name|tripleIndex
index|]
argument_list|,
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|,
name|readPos
argument_list|)
condition|)
block|{
return|return
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|2
index|]
return|;
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
comment|/*    * The hash table slots.  For a bytes key hash table, each slot is 3 longs and the array is    * 3X sized.    *    * The slot triple is 1) a non-zero reference word to the key bytes, 2) the key hash code, and    * 3) a non-zero reference word to the first value bytes.    */
specifier|protected
name|long
index|[]
name|slotTriples
decl_stmt|;
specifier|private
name|void
name|allocateBucketArray
parameter_list|()
block|{
name|int
name|slotTripleArraySize
init|=
literal|3
operator|*
name|logicalHashBucketCount
decl_stmt|;
name|slotTriples
operator|=
operator|new
name|long
index|[
name|slotTripleArraySize
index|]
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinFastBytesHashTable
parameter_list|(
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
name|allocateBucketArray
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

