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
name|JoinUtil
operator|.
name|JoinResult
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
name|VectorMapJoinHashSetResult
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
name|VectorMapJoinLongHashSet
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
comment|/*  * An single long value multi-set optimized for vector map join.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastLongHashSet
extends|extends
name|VectorMapJoinFastLongHashTable
implements|implements
name|VectorMapJoinLongHashSet
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VectorMapJoinFastLongHashSet
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashSetResult
name|createHashSetResult
parameter_list|()
block|{
return|return
operator|new
name|VectorMapJoinFastHashSet
operator|.
name|HashSetResult
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
comment|// Existence.
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
block|}
annotation|@
name|Override
specifier|public
name|JoinResult
name|contains
parameter_list|(
name|long
name|key
parameter_list|,
name|VectorMapJoinHashSetResult
name|hashSetResult
parameter_list|)
block|{
name|VectorMapJoinFastHashSet
operator|.
name|HashSetResult
name|optimizedHashSetResult
init|=
operator|(
name|VectorMapJoinFastHashSet
operator|.
name|HashSetResult
operator|)
name|hashSetResult
decl_stmt|;
name|optimizedHashSetResult
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
name|long
name|existance
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
name|existance
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
name|joinResult
operator|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
expr_stmt|;
block|}
name|optimizedHashSetResult
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
name|VectorMapJoinFastLongHashSet
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

