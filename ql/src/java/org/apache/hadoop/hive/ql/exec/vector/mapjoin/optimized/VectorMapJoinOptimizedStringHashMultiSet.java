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
name|VectorMapJoinBytesHashMultiSet
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

begin_comment
comment|/*  * An multi-key hash map based on the BytesBytesMultiHashMultiSet.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedStringHashMultiSet
extends|extends
name|VectorMapJoinOptimizedHashMultiSet
implements|implements
name|VectorMapJoinBytesHashMultiSet
block|{
specifier|private
name|VectorMapJoinOptimizedStringCommon
name|stringCommon
decl_stmt|;
comment|/*   @Override   public void putRow(BytesWritable currentKey, BytesWritable currentValue)       throws SerDeException, HiveException, IOException {      stringCommon.adaptPutRow((VectorMapJoinOptimizedHashTable) this, currentKey, currentValue);   }   */
annotation|@
name|Override
specifier|public
name|JoinResult
name|contains
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
name|VectorMapJoinHashMultiSetResult
name|hashMultiSetResult
parameter_list|)
throws|throws
name|IOException
block|{
name|SerializedBytes
name|serializedBytes
init|=
name|stringCommon
operator|.
name|serialize
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
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
name|hashMultiSetResult
argument_list|)
return|;
block|}
specifier|public
name|VectorMapJoinOptimizedStringHashMultiSet
parameter_list|(
name|boolean
name|isOuterJoin
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
name|stringCommon
operator|=
operator|new
name|VectorMapJoinOptimizedStringCommon
argument_list|(
name|isOuterJoin
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

