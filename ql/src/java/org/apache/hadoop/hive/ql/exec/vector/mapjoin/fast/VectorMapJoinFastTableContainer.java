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
name|conf
operator|.
name|HiveConf
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
name|HashMapWrapper
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
name|MapJoinObjectSerDeContext
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
name|NonMatchedSmallTableIterator
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
name|MatchTracker
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
name|VectorMapJoinHashTable
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
name|VectorMapJoinTableContainer
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
name|HashTableImplementationType
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
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * HashTableLoader for Tez constructs the hashtable from records read from  * a broadcast edge.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastTableContainer
implements|implements
name|VectorMapJoinTableContainer
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
name|VectorMapJoinFastTableContainer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MapJoinDesc
name|desc
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|hconf
decl_stmt|;
specifier|private
specifier|final
name|float
name|keyCountAdj
decl_stmt|;
specifier|private
specifier|final
name|int
name|threshold
decl_stmt|;
specifier|private
specifier|final
name|float
name|loadFactor
decl_stmt|;
specifier|private
specifier|final
name|int
name|wbSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|estimatedKeyCount
decl_stmt|;
specifier|private
specifier|final
name|VectorMapJoinFastHashTable
name|vectorMapJoinFastHashTable
decl_stmt|;
specifier|private
name|String
name|key
decl_stmt|;
specifier|public
name|VectorMapJoinFastTableContainer
parameter_list|(
name|MapJoinDesc
name|desc
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|long
name|estimatedKeyCount
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|keyCountAdj
operator|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEKEYCOUNTADJUSTMENT
argument_list|)
expr_stmt|;
name|threshold
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLETHRESHOLD
argument_list|)
expr_stmt|;
name|loadFactor
operator|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLELOADFACTOR
argument_list|)
expr_stmt|;
name|wbSize
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEWBSIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|estimatedKeyCount
operator|=
name|estimatedKeyCount
expr_stmt|;
name|int
name|newThreshold
init|=
name|HashMapWrapper
operator|.
name|calculateTableSize
argument_list|(
name|keyCountAdj
argument_list|,
name|threshold
argument_list|,
name|loadFactor
argument_list|,
name|estimatedKeyCount
argument_list|)
decl_stmt|;
comment|// LOG.debug("VectorMapJoinFastTableContainer load newThreshold " + newThreshold);
name|vectorMapJoinFastHashTable
operator|=
name|createHashTable
argument_list|(
name|newThreshold
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashTable
name|vectorMapJoinHashTable
parameter_list|()
block|{
return|return
name|vectorMapJoinFastHashTable
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setKey
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|private
name|VectorMapJoinFastHashTable
name|createHashTable
parameter_list|(
name|int
name|newThreshold
parameter_list|)
block|{
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
name|HashTableImplementationType
name|hashTableImplementationType
init|=
name|vectorDesc
operator|.
name|getHashTableImplementationType
argument_list|()
decl_stmt|;
name|HashTableKind
name|hashTableKind
init|=
name|vectorDesc
operator|.
name|getHashTableKind
argument_list|()
decl_stmt|;
name|HashTableKeyType
name|hashTableKeyType
init|=
name|vectorDesc
operator|.
name|getHashTableKeyType
argument_list|()
decl_stmt|;
name|boolean
name|isFullOuter
init|=
name|vectorDesc
operator|.
name|getIsFullOuter
argument_list|()
decl_stmt|;
name|boolean
name|minMaxEnabled
init|=
name|vectorDesc
operator|.
name|getMinMaxEnabled
argument_list|()
decl_stmt|;
name|int
name|writeBufferSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEWBSIZE
argument_list|)
decl_stmt|;
name|VectorMapJoinFastHashTable
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
name|VectorMapJoinFastLongHashMap
argument_list|(
name|isFullOuter
argument_list|,
name|minMaxEnabled
argument_list|,
name|hashTableKeyType
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastLongHashMultiSet
argument_list|(
name|isFullOuter
argument_list|,
name|minMaxEnabled
argument_list|,
name|hashTableKeyType
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastLongHashSet
argument_list|(
name|isFullOuter
argument_list|,
name|minMaxEnabled
argument_list|,
name|hashTableKeyType
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
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
name|VectorMapJoinFastStringHashMap
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastStringHashMultiSet
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastStringHashSet
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|,
name|desc
operator|.
name|getKeyTblDesc
argument_list|()
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
name|VectorMapJoinFastMultiKeyHashMap
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_MULTISET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastMultiKeyHashMultiSet
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
argument_list|)
expr_stmt|;
break|break;
case|case
name|HASH_SET
case|:
name|hashTable
operator|=
operator|new
name|VectorMapJoinFastMultiKeyHashSet
argument_list|(
name|isFullOuter
argument_list|,
name|newThreshold
argument_list|,
name|loadFactor
argument_list|,
name|writeBufferSize
argument_list|,
name|estimatedKeyCount
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
annotation|@
name|Override
specifier|public
name|MapJoinKey
name|putRow
parameter_list|(
name|Writable
name|currentKey
parameter_list|,
name|Writable
name|currentValue
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|HiveException
throws|,
name|IOException
block|{
comment|// We are not using the key and value contexts, nor do we support a MapJoinKey.
name|vectorMapJoinFastHashTable
operator|.
name|putRow
argument_list|(
operator|(
name|BytesWritable
operator|)
name|currentKey
argument_list|,
operator|(
name|BytesWritable
operator|)
name|currentValue
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|seal
parameter_list|()
block|{
comment|// Do nothing
block|}
annotation|@
name|Override
specifier|public
name|ReusableGetAdaptor
name|createGetter
parameter_list|(
name|MapJoinKey
name|keyTypeFromLoader
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not applicable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|NonMatchedSmallTableIterator
name|createNonMatchedSmallTableIterator
parameter_list|(
name|MatchTracker
name|matchTracker
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not applicable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
comment|// Do nothing
block|}
annotation|@
name|Override
specifier|public
name|MapJoinKey
name|getAnyKey
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not applicable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dumpMetrics
parameter_list|()
block|{
comment|// TODO
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasSpill
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|vectorMapJoinFastHashTable
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEstimatedMemorySize
parameter_list|()
block|{
name|JavaDataModel
name|jdm
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
name|long
name|size
init|=
literal|0
decl_stmt|;
name|size
operator|+=
name|vectorMapJoinFastHashTable
operator|.
name|getEstimatedMemorySize
argument_list|()
expr_stmt|;
name|size
operator|+=
operator|(
literal|4
operator|*
name|jdm
operator|.
name|primitive1
argument_list|()
operator|)
expr_stmt|;
name|size
operator|+=
operator|(
literal|2
operator|*
name|jdm
operator|.
name|object
argument_list|()
operator|)
expr_stmt|;
name|size
operator|+=
operator|(
name|jdm
operator|.
name|primitive2
argument_list|()
operator|)
expr_stmt|;
return|return
name|size
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSerde
parameter_list|(
name|MapJoinObjectSerDeContext
name|keyCtx
parameter_list|,
name|MapJoinObjectSerDeContext
name|valCtx
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Do nothing in this case.
block|}
block|}
end_class

end_unit

