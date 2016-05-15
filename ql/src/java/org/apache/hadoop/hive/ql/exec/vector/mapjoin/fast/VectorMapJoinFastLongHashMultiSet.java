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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinLongHashMultiSet
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
comment|/*  * An single LONG key hash multi-set optimized for vector map join.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastLongHashMultiSet
extends|extends
name|VectorMapJoinFastLongHashTable
implements|implements
name|VectorMapJoinLongHashMultiSet
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
name|VectorMapJoinFastLongHashMultiSet
operator|.
name|class
argument_list|)
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
name|VectorMapJoinFastHashMultiSet
operator|.
name|HashMultiSetResult
argument_list|()
return|;
block|}
comment|/*    * A Unit Test convenience method for putting the key into the hash table using the    * actual type.    */
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|testPutRow
parameter_list|(
name|long
name|currentKey
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|add
argument_list|(
name|currentKey
argument_list|,
literal|null
argument_list|)
expr_stmt|;
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
literal|1
expr_stmt|;
comment|// Count.
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
operator|++
expr_stmt|;
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
name|long
name|key
parameter_list|,
name|VectorMapJoinHashMultiSetResult
name|hashMultiSetResult
parameter_list|)
block|{
name|VectorMapJoinFastHashMultiSet
operator|.
name|HashMultiSetResult
name|optimizedHashMultiSetResult
init|=
operator|(
name|VectorMapJoinFastHashMultiSet
operator|.
name|HashMultiSetResult
operator|)
name|hashMultiSetResult
decl_stmt|;
name|optimizedHashMultiSetResult
operator|.
name|forget
argument_list|()
expr_stmt|;
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
name|long
name|count
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
name|count
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
name|optimizedHashMultiSetResult
operator|.
name|set
argument_list|(
name|count
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
name|optimizedHashMultiSetResult
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
name|VectorMapJoinFastLongHashMultiSet
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
block|}
block|}
end_class

end_unit

