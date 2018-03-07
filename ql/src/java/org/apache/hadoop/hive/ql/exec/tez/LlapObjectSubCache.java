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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|SoftReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_comment
comment|/**  * LlapObjectSubCache. A subcache which lives inside the LlapObjectCache.  * The subcache maintains two lists  * 1. List of softreference to the objects  * 2. List of locks to access the objects.  */
end_comment

begin_class
specifier|public
class|class
name|LlapObjectSubCache
parameter_list|<
name|T
parameter_list|>
block|{
comment|// List of softreferences
specifier|private
name|Object
index|[]
name|softReferenceList
decl_stmt|;
comment|// List of locks to protect the above list
specifier|private
name|List
argument_list|<
name|ReentrantLock
argument_list|>
name|locks
decl_stmt|;
comment|// Function to create subCache
specifier|private
name|Object
index|[]
name|createSubCache
parameter_list|(
name|int
name|numEntries
parameter_list|)
block|{
return|return
operator|new
name|Object
index|[
name|numEntries
index|]
return|;
block|}
comment|// Function to setup locks
specifier|private
name|List
argument_list|<
name|ReentrantLock
argument_list|>
name|createSubCacheLocks
parameter_list|(
name|int
name|numEntries
parameter_list|)
block|{
name|List
argument_list|<
name|ReentrantLock
argument_list|>
name|lockList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numEntries
condition|;
name|i
operator|++
control|)
block|{
name|lockList
operator|.
name|add
argument_list|(
name|i
argument_list|,
operator|new
name|ReentrantLock
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|lockList
return|;
block|}
specifier|public
name|LlapObjectSubCache
parameter_list|(
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
name|ObjectCache
name|cache
parameter_list|,
name|String
name|subCacheKey
parameter_list|,
specifier|final
name|int
name|numEntries
parameter_list|)
throws|throws
name|HiveException
block|{
name|softReferenceList
operator|=
name|cache
operator|.
name|retrieve
argument_list|(
name|subCacheKey
operator|+
literal|"_main"
argument_list|,
parameter_list|()
lambda|->
name|createSubCache
argument_list|(
name|numEntries
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|cache
operator|.
name|retrieve
argument_list|(
name|subCacheKey
operator|+
literal|"_locks"
argument_list|,
parameter_list|()
lambda|->
name|createSubCacheLocks
argument_list|(
name|numEntries
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|lock
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
name|locks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|lock
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|unlock
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
name|locks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|T
name|get
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
comment|// Must be held by same thread
name|Preconditions
operator|.
name|checkState
argument_list|(
name|locks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|isHeldByCurrentThread
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|softReferenceList
index|[
name|index
index|]
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
call|(
name|SoftReference
argument_list|<
name|T
argument_list|>
call|)
argument_list|(
name|softReferenceList
index|[
name|index
index|]
argument_list|)
operator|)
operator|.
name|get
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|T
name|value
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
comment|// Must be held by same thread
name|Preconditions
operator|.
name|checkState
argument_list|(
name|locks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|isHeldByCurrentThread
argument_list|()
argument_list|)
expr_stmt|;
name|softReferenceList
index|[
name|index
index|]
operator|=
operator|new
name|SoftReference
argument_list|<>
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

