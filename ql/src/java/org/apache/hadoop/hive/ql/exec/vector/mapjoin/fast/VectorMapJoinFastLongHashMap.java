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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_comment
comment|/*  * An single long value map optimized for vector map join.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastLongHashMap
extends|extends
name|VectorMapJoinFastLongHashTable
implements|implements
name|VectorMapJoinLongHashMap
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
name|VectorMapJoinFastLongHashMap
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
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
name|long
name|key
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
name|pairIndex
init|=
literal|2
operator|*
name|slot
decl_stmt|;
if|if
condition|(
name|isNewKey
condition|)
block|{
comment|// First entry.
name|slotPairs
index|[
name|pairIndex
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
name|slotPairs
index|[
name|pairIndex
operator|+
literal|1
index|]
operator|=
name|key
expr_stmt|;
block|}
else|else
block|{
comment|// Add another value.
name|slotPairs
index|[
name|pairIndex
index|]
operator|=
name|valueStore
operator|.
name|addMore
argument_list|(
name|slotPairs
index|[
name|pairIndex
index|]
argument_list|,
name|valueBytes
argument_list|,
literal|0
argument_list|,
name|valueLength
argument_list|)
expr_stmt|;
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
name|long
name|key
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
name|VectorMapJoinFastLongHashUtil
operator|.
name|hashKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// LOG.debug("VectorMapJoinFastLongHashMap lookup " + key + " hashCode " + hashCode);
name|long
name|valueRef
init|=
name|findReadSlot
argument_list|(
name|key
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
name|valueRef
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
name|optimizedHashMapResult
operator|.
name|set
argument_list|(
name|valueStore
argument_list|,
name|valueRef
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
name|VectorMapJoinFastLongHashMap
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
name|minMaxEnabled
argument_list|,
name|isOuterJoin
argument_list|,
name|hashTableKeyType
argument_list|,
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|writeBuffersSize
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
block|}
block|}
end_class

end_unit

