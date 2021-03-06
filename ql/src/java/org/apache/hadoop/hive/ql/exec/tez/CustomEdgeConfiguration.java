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
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|LinkedListMultimap
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
name|collect
operator|.
name|Multimap
import|;
end_import

begin_class
class|class
name|CustomEdgeConfiguration
implements|implements
name|Writable
block|{
name|boolean
name|vertexInited
init|=
literal|false
decl_stmt|;
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|bucketToTaskMap
init|=
literal|null
decl_stmt|;
specifier|public
name|CustomEdgeConfiguration
parameter_list|()
block|{   }
specifier|public
name|CustomEdgeConfiguration
parameter_list|(
name|int
name|numBuckets
parameter_list|,
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|routingTable
parameter_list|)
block|{
name|this
operator|.
name|bucketToTaskMap
operator|=
name|routingTable
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
if|if
condition|(
name|routingTable
operator|!=
literal|null
condition|)
block|{
name|vertexInited
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|vertexInited
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numBuckets
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketToTaskMap
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|bucketToTaskMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Collection
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|entry
range|:
name|bucketToTaskMap
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|bucketNum
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|taskId
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|bucketNum
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|taskId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|vertexInited
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|vertexInited
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|int
name|count
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|bucketToTaskMap
operator|=
name|LinkedListMultimap
operator|.
name|create
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|bucketToTaskMap
operator|.
name|put
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|,
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|count
operator|!=
name|bucketToTaskMap
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Was not a clean translation. Some records are missing"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|getRoutingTable
parameter_list|()
block|{
return|return
name|bucketToTaskMap
return|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
block|}
end_class

end_unit

