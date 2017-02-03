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
name|optimized
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
name|persistence
operator|.
name|MapJoinKey
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
name|persistence
operator|.
name|MapJoinTableContainer
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
name|persistence
operator|.
name|MapJoinTableContainer
operator|.
name|ReusableGetAdaptor
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
name|MapJoinDesc
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
name|ql
operator|.
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableKind
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedCreateHashTable
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
name|VectorMapJoinOptimizedCreateHashTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|VectorMapJoinOptimizedHashTable
name|createHashTable
parameter_list|(
name|MapJoinDesc
name|desc
parameter_list|,
name|MapJoinTableContainer
name|mapJoinTableContainer
parameter_list|)
block|{
name|MapJoinKey
name|refKey
init|=
name|mapJoinTableContainer
operator|.
name|getAnyKey
argument_list|()
decl_stmt|;
name|ReusableGetAdaptor
name|hashMapRowGetter
init|=
name|mapJoinTableContainer
operator|.
name|createGetter
argument_list|(
name|refKey
argument_list|)
decl_stmt|;
name|boolean
name|isOuterJoin
init|=
operator|!
name|desc
operator|.
name|isNoOuterJoin
argument_list|()
decl_stmt|;
name|VectorMapJoinDesc
name|vectorDesc
init|=
operator|(
name|VectorMapJoinDesc
operator|)
name|desc
operator|.
name|getVectorDesc
argument_list|()
decl_stmt|;
name|HashTableKind
name|hashTableKind
init|=
name|vectorDesc
operator|.
name|hashTableKind
argument_list|()
decl_stmt|;
name|HashTableKeyType
name|hashTableKeyType
init|=
name|vectorDesc
operator|.
name|hashTableKeyType
argument_list|()
decl_stmt|;
name|boolean
name|minMaxEnabled
init|=
name|vectorDesc
operator|.
name|minMaxEnabled
argument_list|()
decl_stmt|;
name|VectorMapJoinOptimizedHashTable
name|hashTable
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|hashTableKeyType
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
switch|switch
condition|(
name|hashTableKind
condition|)
block|{
case|case
name|HASH_MAP
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedLongHashMap
argument_list|(
name|minMaxEnabled
argument_list|,
name|isOuterJoin
argument_list|,
name|hashTableKeyType
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedLongHashMultiSet
argument_list|(
name|minMaxEnabled
argument_list|,
name|isOuterJoin
argument_list|,
name|hashTableKeyType
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedLongHashSet
argument_list|(
name|minMaxEnabled
argument_list|,
name|isOuterJoin
argument_list|,
name|hashTableKeyType
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
block|}
break|break;
case|case
name|STRING
case|:
switch|switch
condition|(
name|hashTableKind
condition|)
block|{
case|case
name|HASH_MAP
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedStringHashMap
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedStringHashMultiSet
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedStringHashSet
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
block|}
break|break;
case|case
name|MULTI_KEY
case|:
switch|switch
condition|(
name|hashTableKind
condition|)
block|{
case|case
name|HASH_MAP
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedMultiKeyHashMap
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedMultiKeyHashMultiSet
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinOptimizedMultiKeyHashSet
argument_list|(
name|isOuterJoin
argument_list|,
name|mapJoinTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
break|break;
block|}
break|break;
block|}
return|return
name|hashTable
return|;
block|}
comment|/*   @Override   public com.esotericsoftware.kryo.io.Output getHybridBigTableSpillOutput(int partitionId) {      HybridHashTableContainer ht = (HybridHashTableContainer) mapJoinTableContainer;      HashPartition hp = ht.getHashPartitions()[partitionId];      return hp.getMatchfileOutput();   }   */
block|}
end_class

end_unit

