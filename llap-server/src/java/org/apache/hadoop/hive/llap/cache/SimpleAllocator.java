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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

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
name|common
operator|.
name|io
operator|.
name|Allocator
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|MemoryBuffer
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

begin_import
import|import
name|sun
operator|.
name|misc
operator|.
name|Cleaner
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|SimpleAllocator
implements|implements
name|Allocator
implements|,
name|BuddyAllocatorMXBean
block|{
specifier|private
specifier|final
name|boolean
name|isDirect
decl_stmt|;
specifier|private
specifier|static
name|Field
name|cleanerField
decl_stmt|;
static|static
block|{
try|try
block|{
comment|// TODO: To make it work for JDK9 use CleanerUtil from https://issues.apache.org/jira/browse/HADOOP-12760
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|dbClazz
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"java.nio.DirectByteBuffer"
argument_list|)
decl_stmt|;
name|cleanerField
operator|=
name|dbClazz
operator|.
name|getDeclaredField
argument_list|(
literal|"cleaner"
argument_list|)
expr_stmt|;
name|cleanerField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot initialize DirectByteBuffer cleaner"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|cleanerField
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|SimpleAllocator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|isDirect
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
argument_list|)
expr_stmt|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Simple allocator with "
operator|+
operator|(
name|isDirect
condition|?
literal|"direct"
else|:
literal|"byte"
operator|)
operator|+
literal|" buffers"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|void
name|allocateMultiple
parameter_list|(
name|MemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|allocateMultiple
argument_list|(
name|dest
argument_list|,
name|size
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|allocateMultiple
parameter_list|(
name|MemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|,
name|BufferObjectFactory
name|factory
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dest
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|LlapAllocatorBuffer
name|buf
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dest
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
comment|// Note: this is backward compat only. Should be removed with createUnallocated.
name|dest
index|[
name|i
index|]
operator|=
name|buf
operator|=
operator|(
name|factory
operator|!=
literal|null
operator|)
condition|?
operator|(
name|LlapAllocatorBuffer
operator|)
name|factory
operator|.
name|create
argument_list|()
else|:
name|createUnallocated
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|=
operator|(
name|LlapAllocatorBuffer
operator|)
name|dest
index|[
name|i
index|]
expr_stmt|;
block|}
name|ByteBuffer
name|bb
init|=
name|isDirect
condition|?
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|size
argument_list|)
else|:
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|buf
operator|.
name|initialize
argument_list|(
name|bb
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|deallocate
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
block|{
name|LlapAllocatorBuffer
name|buf
init|=
operator|(
name|LlapAllocatorBuffer
operator|)
name|buffer
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|buf
operator|.
name|byteBuffer
decl_stmt|;
name|buf
operator|.
name|byteBuffer
operator|=
literal|null
expr_stmt|;
if|if
condition|(
operator|!
name|bb
operator|.
name|isDirect
argument_list|()
condition|)
return|return;
name|Field
name|field
init|=
name|cleanerField
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
return|return;
try|try
block|{
operator|(
operator|(
name|Cleaner
operator|)
name|field
operator|.
name|get
argument_list|(
name|bb
argument_list|)
operator|)
operator|.
name|clean
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error using DirectByteBuffer cleaner; stopping its use"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|cleanerField
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDirectAlloc
parameter_list|()
block|{
return|return
name|isDirect
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|LlapAllocatorBuffer
name|createUnallocated
parameter_list|()
block|{
return|return
operator|new
name|LlapDataBuffer
argument_list|()
return|;
block|}
comment|// BuddyAllocatorMXBean
annotation|@
name|Override
specifier|public
name|boolean
name|getIsDirect
parameter_list|()
block|{
return|return
name|isDirect
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMinAllocation
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxAllocation
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getArenaSize
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxCacheSize
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
block|}
end_class

end_unit

