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
name|io
operator|.
name|orc
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
name|List
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|Path
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
name|shims
operator|.
name|HadoopShims
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|OrcTail
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|Weigher
import|;
end_import

begin_class
class|class
name|LocalCache
implements|implements
name|OrcInputFormat
operator|.
name|FooterCache
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
name|LocalCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CACHE_INITIAL_CAPACITY
init|=
literal|1024
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|TailAndFileData
block|{
specifier|public
name|TailAndFileData
parameter_list|(
name|long
name|fileLength
parameter_list|,
name|long
name|fileModificationTime
parameter_list|,
name|ByteBuffer
name|bb
parameter_list|)
block|{
name|this
operator|.
name|fileLength
operator|=
name|fileLength
expr_stmt|;
name|this
operator|.
name|fileModTime
operator|=
name|fileModificationTime
expr_stmt|;
name|this
operator|.
name|bb
operator|=
name|bb
expr_stmt|;
block|}
specifier|public
name|ByteBuffer
name|bb
decl_stmt|;
specifier|public
name|long
name|fileLength
decl_stmt|,
name|fileModTime
decl_stmt|;
specifier|public
name|int
name|getMemoryUsage
parameter_list|()
block|{
return|return
name|bb
operator|.
name|capacity
argument_list|()
operator|+
literal|100
return|;
comment|// 100 is for 2 longs, BB and java overheads (semi-arbitrary).
block|}
block|}
specifier|private
specifier|final
name|Cache
argument_list|<
name|Path
argument_list|,
name|TailAndFileData
argument_list|>
name|cache
decl_stmt|;
name|LocalCache
parameter_list|(
name|int
name|numThreads
parameter_list|,
name|long
name|cacheMemSize
parameter_list|,
name|boolean
name|useSoftRef
parameter_list|)
block|{
name|CacheBuilder
argument_list|<
name|Path
argument_list|,
name|TailAndFileData
argument_list|>
name|builder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|initialCapacity
argument_list|(
name|DEFAULT_CACHE_INITIAL_CAPACITY
argument_list|)
operator|.
name|concurrencyLevel
argument_list|(
name|numThreads
argument_list|)
operator|.
name|maximumWeight
argument_list|(
name|cacheMemSize
argument_list|)
operator|.
name|weigher
argument_list|(
operator|new
name|Weigher
argument_list|<
name|Path
argument_list|,
name|TailAndFileData
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|weigh
parameter_list|(
name|Path
name|key
parameter_list|,
name|TailAndFileData
name|value
parameter_list|)
block|{
return|return
name|value
operator|.
name|getMemoryUsage
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|useSoftRef
condition|)
block|{
name|builder
operator|=
name|builder
operator|.
name|softValues
argument_list|()
expr_stmt|;
block|}
name|cache
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|cache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
name|cache
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|Path
name|path
parameter_list|,
name|OrcTail
name|tail
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|tail
operator|.
name|getSerializedTail
argument_list|()
decl_stmt|;
if|if
condition|(
name|bb
operator|.
name|capacity
argument_list|()
operator|!=
name|bb
operator|.
name|remaining
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Bytebuffer allocated for path: "
operator|+
name|path
operator|+
literal|" has remaining: "
operator|+
name|bb
operator|.
name|remaining
argument_list|()
operator|+
literal|" != capacity: "
operator|+
name|bb
operator|.
name|capacity
argument_list|()
argument_list|)
throw|;
block|}
name|cache
operator|.
name|put
argument_list|(
name|path
argument_list|,
operator|new
name|TailAndFileData
argument_list|(
name|tail
operator|.
name|getFileTail
argument_list|()
operator|.
name|getFileLength
argument_list|()
argument_list|,
name|tail
operator|.
name|getFileModificationTime
argument_list|()
argument_list|,
name|bb
operator|.
name|duplicate
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getAndValidate
parameter_list|(
specifier|final
name|List
argument_list|<
name|HadoopShims
operator|.
name|HdfsFileStatusWithId
argument_list|>
name|files
parameter_list|,
specifier|final
name|boolean
name|isOriginal
parameter_list|,
specifier|final
name|OrcTail
index|[]
name|result
parameter_list|,
specifier|final
name|ByteBuffer
index|[]
name|ppdResult
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
comment|// TODO: should local cache also be by fileId? Preserve the original logic for now.
assert|assert
name|result
operator|.
name|length
operator|==
name|files
operator|.
name|size
argument_list|()
assert|;
name|int
name|i
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|HadoopShims
operator|.
name|HdfsFileStatusWithId
name|fileWithId
range|:
name|files
control|)
block|{
operator|++
name|i
expr_stmt|;
name|FileStatus
name|file
init|=
name|fileWithId
operator|.
name|getFileStatus
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
name|file
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|TailAndFileData
name|tfd
init|=
name|cache
operator|.
name|getIfPresent
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Serialized tail "
operator|+
operator|(
name|tfd
operator|==
literal|null
condition|?
literal|"not "
else|:
literal|""
operator|)
operator|+
literal|"cached for path: "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tfd
operator|==
literal|null
condition|)
continue|continue;
if|if
condition|(
name|file
operator|.
name|getLen
argument_list|()
operator|==
name|tfd
operator|.
name|fileLength
operator|&&
name|file
operator|.
name|getModificationTime
argument_list|()
operator|==
name|tfd
operator|.
name|fileModTime
condition|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|ReaderImpl
operator|.
name|extractFileTail
argument_list|(
name|tfd
operator|.
name|bb
operator|.
name|duplicate
argument_list|()
argument_list|,
name|tfd
operator|.
name|fileLength
argument_list|,
name|tfd
operator|.
name|fileModTime
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Invalidate
name|cache
operator|.
name|invalidate
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Meta-Info for : "
operator|+
name|path
operator|+
literal|" changed. CachedModificationTime: "
operator|+
name|tfd
operator|.
name|fileModTime
operator|+
literal|", CurrentModificationTime: "
operator|+
name|file
operator|.
name|getModificationTime
argument_list|()
operator|+
literal|", CachedLength: "
operator|+
name|tfd
operator|.
name|fileLength
operator|+
literal|", CurrentLength: "
operator|+
name|file
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPpd
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBlocking
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
specifier|final
name|OrcInputFormat
operator|.
name|FooterCacheKey
name|cacheKey
parameter_list|,
specifier|final
name|OrcTail
name|orcTail
parameter_list|)
throws|throws
name|IOException
block|{
name|put
argument_list|(
name|cacheKey
operator|.
name|getPath
argument_list|()
argument_list|,
name|orcTail
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

