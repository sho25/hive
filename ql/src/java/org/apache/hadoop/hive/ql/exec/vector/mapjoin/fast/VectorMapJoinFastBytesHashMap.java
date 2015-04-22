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
name|VectorMapJoinBytesHashMap
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
name|VectorMapJoinHashMapResult
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

begin_comment
comment|/*  * An single byte array value hash map optimized for vector map join.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastBytesHashMap
extends|extends
name|VectorMapJoinFastBytesHashTable
implements|implements
name|VectorMapJoinBytesHashMap
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
name|VectorMapJoinFastBytesHashMap
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|VectorMapJoinFastValueStore
name|valueStore
decl_stmt|;
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashMapResult
name|createHashMapResult
parameter_list|()
block|{
return|return
operator|new
name|VectorMapJoinFastValueStore
operator|.
name|HashMapResult
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
name|byte
index|[]
name|valueBytes
init|=
name|currentValue
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|valueLength
init|=
name|currentValue
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|int
name|tripleIndex
init|=
literal|3
operator|*
name|slot
decl_stmt|;
if|if
condition|(
name|isNewKey
condition|)
block|{
comment|// First entry.
name|slotTriples
index|[
name|tripleIndex
index|]
operator|=
name|keyStore
operator|.
name|add
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|1
index|]
operator|=
name|hashCode
expr_stmt|;
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|2
index|]
operator|=
name|valueStore
operator|.
name|addFirst
argument_list|(
name|valueBytes
argument_list|,
literal|0
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
comment|// LOG.info("VectorMapJoinFastBytesHashMap add first keyRefWord " + Long.toHexString(slotTriples[tripleIndex]) + " hashCode " + Long.toHexString(slotTriples[tripleIndex + 1]) + " valueRefWord " + Long.toHexString(slotTriples[tripleIndex + 2]));
name|keysAssigned
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// Add another value.
comment|// LOG.info("VectorMapJoinFastBytesHashMap add more keyRefWord " + Long.toHexString(slotTriples[tripleIndex]) + " hashCode " + Long.toHexString(slotTriples[tripleIndex + 1]) + " valueRefWord " + Long.toHexString(slotTriples[tripleIndex + 2]));
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|2
index|]
operator|=
name|valueStore
operator|.
name|addMore
argument_list|(
name|slotTriples
index|[
name|tripleIndex
operator|+
literal|2
index|]
argument_list|,
name|valueBytes
argument_list|,
literal|0
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
comment|// LOG.info("VectorMapJoinFastBytesHashMap add more new valueRefWord " + Long.toHexString(slotTriples[tripleIndex + 2]));
block|}
block|}
annotation|@
name|Override
specifier|public
name|JoinUtil
operator|.
name|JoinResult
name|lookup
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
name|VectorMapJoinHashMapResult
name|hashMapResult
parameter_list|)
block|{
name|VectorMapJoinFastValueStore
operator|.
name|HashMapResult
name|optimizedHashMapResult
init|=
operator|(
name|VectorMapJoinFastValueStore
operator|.
name|HashMapResult
operator|)
name|hashMapResult
decl_stmt|;
name|optimizedHashMapResult
operator|.
name|forget
argument_list|()
expr_stmt|;
name|long
name|hashCode
init|=
name|VectorMapJoinFastBytesHashUtil
operator|.
name|hashKey
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
decl_stmt|;
name|long
name|valueRefWord
init|=
name|findReadSlot
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|,
name|hashCode
argument_list|)
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
decl_stmt|;
if|if
condition|(
name|valueRefWord
operator|==
operator|-
literal|1
condition|)
block|{
name|joinResult
operator|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
expr_stmt|;
block|}
else|else
block|{
comment|// LOG.info("VectorMapJoinFastBytesHashMap lookup hashCode " + Long.toHexString(hashCode) + " valueRefWord " + Long.toHexString(valueRefWord) + " (valueStore != null) " + (valueStore != null));
name|optimizedHashMapResult
operator|.
name|set
argument_list|(
name|valueStore
argument_list|,
name|valueRefWord
argument_list|)
expr_stmt|;
name|joinResult
operator|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
expr_stmt|;
block|}
name|optimizedHashMapResult
operator|.
name|setJoinResult
argument_list|(
name|joinResult
argument_list|)
expr_stmt|;
return|return
name|joinResult
return|;
block|}
specifier|public
name|VectorMapJoinFastBytesHashMap
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
name|memUsage
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
name|memUsage
argument_list|)
expr_stmt|;
name|valueStore
operator|=
operator|new
name|VectorMapJoinFastValueStore
argument_list|(
name|writeBuffersSize
argument_list|)
expr_stmt|;
comment|// Share the same write buffers with our value store.
name|keyStore
operator|=
operator|new
name|VectorMapJoinFastKeyStore
argument_list|(
name|valueStore
operator|.
name|writeBuffers
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

