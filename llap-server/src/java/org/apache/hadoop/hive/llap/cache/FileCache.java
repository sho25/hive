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
name|llap
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|atomic
operator|.
name|AtomicInteger
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
name|base
operator|.
name|Function
import|;
end_import

begin_comment
comment|/** Class used for a single file in LowLevelCacheImpl, etc. */
end_comment

begin_class
class|class
name|FileCache
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
specifier|static
specifier|final
name|int
name|EVICTED_REFCOUNT
init|=
operator|-
literal|1
decl_stmt|,
name|EVICTING_REFCOUNT
init|=
operator|-
literal|2
decl_stmt|;
specifier|private
specifier|final
name|T
name|cache
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|refCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|FileCache
parameter_list|(
name|T
name|value
parameter_list|)
block|{
name|this
operator|.
name|cache
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|T
name|getCache
parameter_list|()
block|{
return|return
name|cache
return|;
block|}
name|boolean
name|incRef
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|value
init|=
name|refCount
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
name|EVICTED_REFCOUNT
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|value
operator|==
name|EVICTING_REFCOUNT
condition|)
continue|continue;
comment|// spin until it resolves; extremely rare
assert|assert
name|value
operator|>=
literal|0
assert|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|value
argument_list|,
name|value
operator|+
literal|1
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
block|}
name|void
name|decRef
parameter_list|()
block|{
name|int
name|value
init|=
name|refCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected refCount "
operator|+
name|value
argument_list|)
throw|;
block|}
block|}
name|boolean
name|startEvicting
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|value
init|=
name|refCount
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|1
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|value
argument_list|,
name|EVICTING_REFCOUNT
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
block|}
name|void
name|commitEvicting
parameter_list|()
block|{
name|boolean
name|result
init|=
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|EVICTING_REFCOUNT
argument_list|,
name|EVICTED_REFCOUNT
argument_list|)
decl_stmt|;
assert|assert
name|result
assert|;
block|}
name|void
name|abortEvicting
parameter_list|()
block|{
name|boolean
name|result
init|=
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|EVICTING_REFCOUNT
argument_list|,
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|result
assert|;
block|}
comment|/**    * All this mess is necessary because we want to be able to remove sub-caches for fully    * evicted files. It may actually be better to have non-nested map with object keys?    */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|FileCache
argument_list|<
name|T
argument_list|>
name|getOrAddFileSubCache
parameter_list|(
name|ConcurrentHashMap
argument_list|<
name|Object
argument_list|,
name|FileCache
argument_list|<
name|T
argument_list|>
argument_list|>
name|cache
parameter_list|,
name|Object
name|fileKey
parameter_list|,
name|Function
argument_list|<
name|Void
argument_list|,
name|T
argument_list|>
name|createFunc
parameter_list|)
block|{
name|FileCache
argument_list|<
name|T
argument_list|>
name|newSubCache
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|// Overwhelmingly executes once.
name|FileCache
argument_list|<
name|T
argument_list|>
name|subCache
init|=
name|cache
operator|.
name|get
argument_list|(
name|fileKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|subCache
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|subCache
operator|.
name|incRef
argument_list|()
condition|)
return|return
name|subCache
return|;
comment|// Main path - found it, incRef-ed it.
if|if
condition|(
name|newSubCache
operator|==
literal|null
condition|)
block|{
name|newSubCache
operator|=
operator|new
name|FileCache
argument_list|<
name|T
argument_list|>
argument_list|(
name|createFunc
operator|.
name|apply
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|newSubCache
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
comment|// Found a stale value we cannot incRef; try to replace it with new value.
if|if
condition|(
name|cache
operator|.
name|replace
argument_list|(
name|fileKey
argument_list|,
name|subCache
argument_list|,
name|newSubCache
argument_list|)
condition|)
return|return
name|newSubCache
return|;
continue|continue;
comment|// Someone else replaced/removed a stale value, try again.
block|}
comment|// No value found.
if|if
condition|(
name|newSubCache
operator|==
literal|null
condition|)
block|{
name|newSubCache
operator|=
operator|new
name|FileCache
argument_list|<
name|T
argument_list|>
argument_list|(
name|createFunc
operator|.
name|apply
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|newSubCache
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
name|FileCache
argument_list|<
name|T
argument_list|>
name|oldSubCache
init|=
name|cache
operator|.
name|putIfAbsent
argument_list|(
name|fileKey
argument_list|,
name|newSubCache
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldSubCache
operator|==
literal|null
condition|)
return|return
name|newSubCache
return|;
comment|// Main path 2 - created a new file cache.
if|if
condition|(
name|oldSubCache
operator|.
name|incRef
argument_list|()
condition|)
return|return
name|oldSubCache
return|;
comment|// Someone created one in parallel.
comment|// Someone created one in parallel and then it went stale.
if|if
condition|(
name|cache
operator|.
name|replace
argument_list|(
name|fileKey
argument_list|,
name|oldSubCache
argument_list|,
name|newSubCache
argument_list|)
condition|)
return|return
name|newSubCache
return|;
comment|// Someone else replaced/removed a parallel-added stale value, try again. Max confusion.
block|}
block|}
block|}
end_class

end_unit

