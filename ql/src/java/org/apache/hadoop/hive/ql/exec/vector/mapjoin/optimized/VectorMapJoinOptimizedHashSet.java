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
name|BytesBytesMultiHashMap
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
name|VectorMapJoinBytesHashSet
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
name|VectorMapJoinHashTableResult
import|;
end_import

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedHashSet
extends|extends
name|VectorMapJoinOptimizedHashTable
implements|implements
name|VectorMapJoinBytesHashSet
block|{
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashSetResult
name|createHashSetResult
parameter_list|()
block|{
return|return
operator|new
name|HashSetResult
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|HashSetResult
extends|extends
name|VectorMapJoinHashSetResult
block|{
specifier|private
name|BytesBytesMultiHashMap
operator|.
name|Result
name|bytesBytesMultiHashMapResult
decl_stmt|;
specifier|public
name|HashSetResult
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|bytesBytesMultiHashMapResult
operator|=
operator|new
name|BytesBytesMultiHashMap
operator|.
name|Result
argument_list|()
expr_stmt|;
block|}
specifier|public
name|BytesBytesMultiHashMap
operator|.
name|Result
name|bytesBytesMultiHashMapResult
parameter_list|()
block|{
return|return
name|bytesBytesMultiHashMapResult
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|forget
parameter_list|()
block|{
name|bytesBytesMultiHashMapResult
operator|.
name|forget
argument_list|()
expr_stmt|;
name|super
operator|.
name|forget
argument_list|()
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
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyOffset
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|VectorMapJoinHashSetResult
name|hashSetResult
parameter_list|)
throws|throws
name|IOException
block|{
name|HashSetResult
name|implementationHashSetResult
init|=
operator|(
name|HashSetResult
operator|)
name|hashSetResult
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
init|=
name|doLookup
argument_list|(
name|keyBytes
argument_list|,
name|keyOffset
argument_list|,
name|keyLength
argument_list|,
name|implementationHashSetResult
operator|.
name|bytesBytesMultiHashMapResult
argument_list|()
argument_list|,
operator|(
name|VectorMapJoinHashTableResult
operator|)
name|hashSetResult
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|joinResult
return|;
block|}
specifier|public
name|VectorMapJoinOptimizedHashSet
parameter_list|(
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
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEstimatedMemorySize
parameter_list|()
block|{
return|return
name|super
operator|.
name|getEstimatedMemorySize
argument_list|()
return|;
block|}
block|}
end_class

end_unit

