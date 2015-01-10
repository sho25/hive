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
name|llap
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|DebugUtils
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|cache
operator|.
name|LlapMemoryBuffer
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|impl
operator|.
name|LlapIoImpl
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|LlapCacheableBuffer
extends|extends
name|LlapMemoryBuffer
block|{
specifier|public
name|LlapCacheableBuffer
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|byteBuffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toStringForCache
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|hashCode
argument_list|()
argument_list|)
operator|+
literal|" "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$.2f"
argument_list|,
name|priority
argument_list|)
operator|+
literal|" "
operator|+
name|lastUpdate
operator|+
literal|" "
operator|+
operator|(
name|isLocked
argument_list|()
condition|?
literal|"!"
else|:
literal|"."
operator|)
operator|+
literal|"]"
return|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|EVICTED_REFCOUNT
init|=
operator|-
literal|1
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
comment|// TODO: Fields pertaining to cache policy. Perhaps they should live in separate object.
specifier|public
name|double
name|priority
decl_stmt|;
specifier|public
name|long
name|lastUpdate
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|int
name|indexInHeap
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|boolean
name|isLockedInHeap
decl_stmt|;
comment|// TODO#: this flag is invalid and not thread safe
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|byteBuffer
operator|==
literal|null
condition|)
return|return
literal|0
return|;
return|return
operator|(
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
operator|.
name|byteBuffer
argument_list|)
operator|*
literal|37
operator|+
name|offset
operator|)
operator|*
literal|37
operator|+
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|LlapCacheableBuffer
operator|)
condition|)
return|return
literal|false
return|;
name|LlapCacheableBuffer
name|other
init|=
operator|(
name|LlapCacheableBuffer
operator|)
name|obj
decl_stmt|;
comment|// We only compare objects, and not contents of the ByteBuffer.
return|return
name|byteBuffer
operator|==
name|other
operator|.
name|byteBuffer
operator|&&
name|this
operator|.
name|offset
operator|==
name|other
operator|.
name|offset
operator|&&
name|this
operator|.
name|length
operator|==
name|other
operator|.
name|length
return|;
block|}
name|int
name|lock
parameter_list|()
block|{
name|int
name|oldRefCount
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|oldRefCount
operator|=
name|refCount
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|oldRefCount
operator|==
name|EVICTED_REFCOUNT
condition|)
return|return
operator|-
literal|1
return|;
assert|assert
name|oldRefCount
operator|>=
literal|0
assert|;
if|if
condition|(
name|refCount
operator|.
name|compareAndSet
argument_list|(
name|oldRefCount
argument_list|,
name|oldRefCount
operator|+
literal|1
argument_list|)
condition|)
break|break;
block|}
return|return
name|oldRefCount
return|;
block|}
specifier|public
name|boolean
name|isLocked
parameter_list|()
block|{
comment|// Best-effort check. We cannot do a good check against caller thread, since
comment|// refCount could still be> 0 if someone else locked. This is used for asserts.
return|return
name|refCount
operator|.
name|get
argument_list|()
operator|>
literal|0
return|;
block|}
specifier|public
name|boolean
name|isInvalid
parameter_list|()
block|{
return|return
name|refCount
operator|.
name|get
argument_list|()
operator|==
name|EVICTED_REFCOUNT
return|;
block|}
name|int
name|unlock
parameter_list|()
block|{
name|int
name|newRefCount
init|=
name|refCount
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|newRefCount
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
name|newRefCount
argument_list|)
throw|;
block|}
return|return
name|newRefCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"0x"
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return Whether the we can invalidate; false if locked or already evicted.    */
name|boolean
name|invalidate
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
literal|0
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
name|EVICTED_REFCOUNT
argument_list|)
condition|)
break|break;
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Invalidated "
operator|+
name|this
operator|+
literal|" due to eviction"
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

