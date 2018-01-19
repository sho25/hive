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
name|optimized
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

begin_comment
comment|/*  * An single long value hash map based on the BytesBytesMultiHashSet.  *  * We serialize the long key into BinarySortable format into an output buffer accepted by  * BytesBytesMultiHashSet.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedLongHashSet
extends|extends
name|VectorMapJoinOptimizedHashSet
implements|implements
name|VectorMapJoinLongHashSet
block|{
specifier|private
name|VectorMapJoinOptimizedLongCommon
name|longCommon
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|useMinMax
parameter_list|()
block|{
return|return
name|longCommon
operator|.
name|useMinMax
argument_list|()
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
name|longCommon
operator|.
name|min
argument_list|()
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
name|longCommon
operator|.
name|max
argument_list|()
return|;
block|}
comment|/*   @Override   public void putRow(BytesWritable currentKey, BytesWritable currentValue)       throws SerDeException, HiveException, IOException {      longCommon.adaptPutRow((VectorMapJoinOptimizedHashTable) this, currentKey, currentValue);   }   */
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
throws|throws
name|IOException
block|{
name|SerializedBytes
name|serializedBytes
init|=
name|longCommon
operator|.
name|serialize
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|super
operator|.
name|contains
argument_list|(
name|serializedBytes
operator|.
name|bytes
argument_list|,
name|serializedBytes
operator|.
name|offset
argument_list|,
name|serializedBytes
operator|.
name|length
argument_list|,
name|hashSetResult
argument_list|)
return|;
block|}
specifier|public
name|VectorMapJoinOptimizedLongHashSet
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
name|MapJoinTableContainer
name|originalTableContainer
parameter_list|,
name|ReusableGetAdaptor
name|hashMapRowGetter
parameter_list|)
block|{
name|super
argument_list|(
name|originalTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
name|longCommon
operator|=
operator|new
name|VectorMapJoinOptimizedLongCommon
argument_list|(
name|minMaxEnabled
argument_list|,
name|isOuterJoin
argument_list|,
name|hashTableKeyType
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

