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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|util
operator|.
name|JavaDataModel
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
specifier|protected
name|WriteBuffers
name|writeBuffers
decl_stmt|;
specifier|protected
name|WriteBuffers
operator|.
name|Position
name|unsafeReadPos
decl_stmt|;
comment|// Thread-unsafe position used at write time.
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
specifier|public
specifier|abstract
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
function_decl|;
specifier|protected
name|void
name|expandAndRehash
parameter_list|()
block|{
comment|// We cannot go above highest Integer power of 2.
if|if
condition|(
name|logicalHashBucketCount
operator|>
name|HIGHEST_INT_POWER_OF_2
condition|)
block|{
name|throwExpandError
argument_list|(
name|HIGHEST_INT_POWER_OF_2
argument_list|,
literal|"Bytes"
argument_list|)
expr_stmt|;
block|}
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
name|long
index|[]
name|newSlots
init|=
operator|new
name|long
index|[
name|newLogicalHashBucketCount
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
operator|!=
literal|0
condition|)
block|{
specifier|final
name|long
name|hashCode
init|=
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|calculateHashCode
argument_list|(
name|refWord
argument_list|,
name|writeBuffers
argument_list|,
name|unsafeReadPos
argument_list|)
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
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|newSlots
index|[
name|newSlot
index|]
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
name|newLargestNumberOfSteps
operator|=
name|i
expr_stmt|;
comment|// debugDumpKeyProbe(keyOffset, keyLength, hashCode, slot);
block|}
comment|// Use old reference word.
name|newSlots
index|[
name|newSlot
index|]
operator|=
name|refWord
expr_stmt|;
block|}
block|}
name|slots
operator|=
name|newSlots
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
block|}
comment|/*    * The hash table slots for fast HashMap.    */
specifier|protected
name|long
index|[]
name|slots
decl_stmt|;
specifier|private
name|void
name|allocateBucketArray
parameter_list|()
block|{
comment|// We cannot go above highest Integer power of 2.
if|if
condition|(
name|logicalHashBucketCount
operator|>
name|HIGHEST_INT_POWER_OF_2
condition|)
block|{
name|throwExpandError
argument_list|(
name|HIGHEST_INT_POWER_OF_2
argument_list|,
literal|"Bytes"
argument_list|)
expr_stmt|;
block|}
name|slots
operator|=
operator|new
name|long
index|[
name|logicalHashBucketCount
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
parameter_list|,
name|long
name|estimatedKeyCount
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|writeBuffersSize
argument_list|,
name|estimatedKeyCount
argument_list|)
expr_stmt|;
name|unsafeReadPos
operator|=
operator|new
name|WriteBuffers
operator|.
name|Position
argument_list|()
expr_stmt|;
name|allocateBucketArray
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
literal|0
decl_stmt|;
name|size
operator|+=
name|super
operator|.
name|getEstimatedMemorySize
argument_list|()
expr_stmt|;
name|size
operator|+=
name|unsafeReadPos
operator|==
literal|null
condition|?
literal|0
else|:
name|unsafeReadPos
operator|.
name|getEstimatedMemorySize
argument_list|()
expr_stmt|;
name|size
operator|+=
name|JavaDataModel
operator|.
name|get
argument_list|()
operator|.
name|lengthForLongArrayOfSize
argument_list|(
name|slots
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

