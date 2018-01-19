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
operator|.
name|ByteSegmentRef
import|;
end_import

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedHashMap
extends|extends
name|VectorMapJoinOptimizedHashTable
implements|implements
name|VectorMapJoinBytesHashMap
block|{
annotation|@
name|Override
specifier|public
name|VectorMapJoinHashMapResult
name|createHashMapResult
parameter_list|()
block|{
return|return
operator|new
name|HashMapResult
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|HashMapResult
extends|extends
name|VectorMapJoinHashMapResult
block|{
specifier|private
name|BytesBytesMultiHashMap
operator|.
name|Result
name|bytesBytesMultiHashMapResult
decl_stmt|;
specifier|public
name|HashMapResult
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
name|boolean
name|hasRows
parameter_list|()
block|{
return|return
operator|(
name|joinResult
argument_list|()
operator|==
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSingleRow
parameter_list|()
block|{
if|if
condition|(
name|joinResult
argument_list|()
operator|!=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HashMapResult is not a match"
argument_list|)
throw|;
block|}
return|return
name|bytesBytesMultiHashMapResult
operator|.
name|isSingleRow
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCappedCountAvailable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|cappedCount
parameter_list|()
block|{
comment|// the return values are capped to return ==0, ==1 and>= 2
return|return
name|hasRows
argument_list|()
condition|?
operator|(
name|isSingleRow
argument_list|()
condition|?
literal|1
else|:
literal|2
operator|)
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteSegmentRef
name|first
parameter_list|()
block|{
if|if
condition|(
name|joinResult
argument_list|()
operator|!=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"HashMapResult is not a match"
argument_list|)
throw|;
block|}
return|return
name|bytesBytesMultiHashMapResult
operator|.
name|first
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteSegmentRef
name|next
parameter_list|()
block|{
return|return
name|bytesBytesMultiHashMapResult
operator|.
name|next
argument_list|()
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"isSingleRow "
operator|+
operator|(
name|joinResult
argument_list|()
operator|==
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
condition|?
name|isSingleRow
argument_list|()
else|:
literal|"<none>"
operator|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDetailedHashMapResultPositionString
parameter_list|()
block|{
return|return
literal|"(Not supported yet)"
return|;
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
name|keyOffset
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|VectorMapJoinHashMapResult
name|hashMapResult
parameter_list|)
throws|throws
name|IOException
block|{
name|HashMapResult
name|implementationHashMapResult
init|=
operator|(
name|HashMapResult
operator|)
name|hashMapResult
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
name|implementationHashMapResult
operator|.
name|bytesBytesMultiHashMapResult
argument_list|()
argument_list|,
operator|(
name|VectorMapJoinHashTableResult
operator|)
name|hashMapResult
argument_list|)
decl_stmt|;
return|return
name|joinResult
return|;
block|}
specifier|public
name|VectorMapJoinOptimizedHashMap
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
block|}
end_class

end_unit

