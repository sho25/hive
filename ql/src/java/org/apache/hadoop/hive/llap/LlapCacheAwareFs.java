begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|AtomicLong
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|fs
operator|.
name|FSDataInputStream
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
name|FSDataOutputStream
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
name|FileSystem
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
name|fs
operator|.
name|PositionedReadable
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
name|Seekable
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
name|permission
operator|.
name|FsPermission
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
name|DataCache
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
name|DiskRange
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
name|DiskRangeList
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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|encoded
operator|.
name|CacheChunk
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
name|util
operator|.
name|Progressable
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
name|RecordReaderUtils
import|;
end_import

begin_comment
comment|/**  * This is currently only used by Parquet; however, a generally applicable approach is used -  * you pass in a set of offset pairs for a file, and the file is cached with these boundaries.  * Don't add anything format specific here.  */
end_comment

begin_class
specifier|public
class|class
name|LlapCacheAwareFs
extends|extends
name|FileSystem
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SCHEME
init|=
literal|"llapcache"
decl_stmt|;
specifier|private
specifier|static
name|AtomicLong
name|currentSplitId
init|=
operator|new
name|AtomicLong
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|URI
name|uri
decl_stmt|;
comment|// We store the chunk indices by split file; that way if several callers are reading
comment|// the same file they can separately store and remove the relevant parts of the index.
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|CacheAwareInputStream
argument_list|>
name|files
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Path
name|registerFile
parameter_list|(
name|DataCache
name|cache
parameter_list|,
name|Path
name|path
parameter_list|,
name|Object
name|fileKey
parameter_list|,
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|index
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|splitId
init|=
name|currentSplitId
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|CacheAwareInputStream
name|stream
init|=
operator|new
name|CacheAwareInputStream
argument_list|(
name|cache
argument_list|,
name|conf
argument_list|,
name|index
argument_list|,
name|path
argument_list|,
name|fileKey
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|.
name|putIfAbsent
argument_list|(
name|splitId
argument_list|,
name|stream
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Record already exists for "
operator|+
name|splitId
argument_list|)
throw|;
block|}
name|conf
operator|.
name|set
argument_list|(
literal|"fs."
operator|+
name|LlapCacheAwareFs
operator|.
name|SCHEME
operator|+
literal|".impl"
argument_list|,
name|LlapCacheAwareFs
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|SCHEME
operator|+
literal|"://"
operator|+
name|SCHEME
operator|+
literal|"/"
operator|+
name|splitId
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|unregisterFile
parameter_list|(
name|Path
name|cachePath
parameter_list|)
block|{
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
literal|"Unregistering "
operator|+
name|cachePath
argument_list|)
expr_stmt|;
block|}
name|files
operator|.
name|remove
argument_list|(
name|extractSplitId
argument_list|(
name|cachePath
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|URI
name|uri
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|uri
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|uri
operator|=
name|URI
operator|.
name|create
argument_list|(
name|SCHEME
operator|+
literal|"://"
operator|+
name|SCHEME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
name|Path
name|path
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSDataInputStream
argument_list|(
name|getCtx
argument_list|(
name|path
argument_list|)
operator|.
name|cloneWithBufferSize
argument_list|(
name|bufferSize
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|getCtx
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|files
operator|.
name|get
argument_list|(
name|extractSplitId
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|extractSplitId
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|pathOnly
init|=
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathOnly
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
name|pathOnly
operator|=
name|pathOnly
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|pathOnly
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|URI
name|getUri
parameter_list|()
block|{
return|return
name|uri
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getWorkingDirectory
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWorkingDirectory
parameter_list|(
name|Path
name|arg0
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|append
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|int
name|arg1
parameter_list|,
name|Progressable
name|arg2
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|append
argument_list|(
name|ctx
operator|.
name|path
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|create
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|FsPermission
name|arg1
parameter_list|,
name|boolean
name|arg2
parameter_list|,
name|int
name|arg3
parameter_list|,
name|short
name|arg4
parameter_list|,
name|long
name|arg5
parameter_list|,
name|Progressable
name|arg6
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|create
argument_list|(
name|ctx
operator|.
name|path
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|,
name|arg3
argument_list|,
name|arg4
argument_list|,
name|arg5
argument_list|,
name|arg6
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|boolean
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|delete
argument_list|(
name|ctx
operator|.
name|path
argument_list|,
name|arg1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|(
name|Path
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|getFileStatus
argument_list|(
name|ctx
operator|.
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
index|[]
name|listStatus
parameter_list|(
name|Path
name|arg0
parameter_list|)
throws|throws
name|FileNotFoundException
throws|,
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|listStatus
argument_list|(
name|ctx
operator|.
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mkdirs
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|FsPermission
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|;
return|return
name|ctx
operator|.
name|getFs
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|ctx
operator|.
name|path
argument_list|,
name|arg1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|rename
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|Path
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|ctx1
init|=
name|getCtx
argument_list|(
name|arg0
argument_list|)
decl_stmt|,
name|ctx2
init|=
name|getCtx
argument_list|(
name|arg1
argument_list|)
decl_stmt|;
return|return
name|ctx1
operator|.
name|getFs
argument_list|()
operator|.
name|rename
argument_list|(
name|ctx1
operator|.
name|path
argument_list|,
name|ctx2
operator|.
name|path
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|CacheAwareInputStream
extends|extends
name|InputStream
implements|implements
name|Seekable
implements|,
name|PositionedReadable
block|{
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|chunkIndex
decl_stmt|;
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|private
specifier|final
name|Object
name|fileKey
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|DataCache
name|cache
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
name|long
name|position
init|=
literal|0
decl_stmt|;
specifier|public
name|CacheAwareInputStream
parameter_list|(
name|DataCache
name|cache
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TreeMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|chunkIndex
parameter_list|,
name|Path
name|path
parameter_list|,
name|Object
name|fileKey
parameter_list|,
name|int
name|bufferSize
parameter_list|)
block|{
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|fileKey
operator|=
name|fileKey
expr_stmt|;
name|this
operator|.
name|chunkIndex
operator|=
name|chunkIndex
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
block|}
specifier|public
name|LlapCacheAwareFs
operator|.
name|CacheAwareInputStream
name|cloneWithBufferSize
parameter_list|(
name|int
name|bufferSize
parameter_list|)
block|{
return|return
operator|new
name|CacheAwareInputStream
argument_list|(
name|cache
argument_list|,
name|conf
argument_list|,
name|chunkIndex
argument_list|,
name|path
argument_list|,
name|fileKey
argument_list|,
name|bufferSize
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
comment|// This is not called by ConsecutiveChunk stuff in Parquet.
comment|// If this were used, it might make sense to make it faster.
name|byte
index|[]
name|theByte
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|int
name|result
init|=
name|read
argument_list|(
name|theByte
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|1
condition|)
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
return|return
name|theByte
index|[
literal|0
index|]
operator|&
literal|0xFF
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|array
parameter_list|,
specifier|final
name|int
name|arrayOffset
parameter_list|,
specifier|final
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|readStartPos
init|=
name|position
decl_stmt|;
name|DiskRangeList
name|drl
init|=
operator|new
name|DiskRangeList
argument_list|(
name|readStartPos
argument_list|,
name|readStartPos
operator|+
name|len
argument_list|)
decl_stmt|;
name|DataCache
operator|.
name|BooleanRef
name|gotAllData
init|=
operator|new
name|DataCache
operator|.
name|BooleanRef
argument_list|()
decl_stmt|;
name|drl
operator|=
name|cache
operator|.
name|getFileData
argument_list|(
name|fileKey
argument_list|,
name|drl
argument_list|,
literal|0
argument_list|,
operator|new
name|DataCache
operator|.
name|DiskRangeListFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiskRangeList
name|createCacheChunk
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|,
name|long
name|startOffset
parameter_list|,
name|long
name|endOffset
parameter_list|)
block|{
return|return
operator|new
name|CacheChunk
argument_list|(
name|buffer
argument_list|,
name|startOffset
argument_list|,
name|endOffset
argument_list|)
return|;
block|}
block|}
argument_list|,
name|gotAllData
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Buffers after cache "
operator|+
name|RecordReaderUtils
operator|.
name|stringifyDiskRanges
argument_list|(
name|drl
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gotAllData
operator|.
name|value
condition|)
block|{
name|long
name|sizeRead
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|drl
operator|!=
literal|null
condition|)
block|{
assert|assert
name|drl
operator|.
name|hasData
argument_list|()
assert|;
name|long
name|from
init|=
name|drl
operator|.
name|getOffset
argument_list|()
decl_stmt|,
name|to
init|=
name|drl
operator|.
name|getEnd
argument_list|()
decl_stmt|;
name|int
name|offsetFromReadStart
init|=
call|(
name|int
call|)
argument_list|(
name|from
operator|-
name|readStartPos
argument_list|)
decl_stmt|,
name|candidateSize
init|=
call|(
name|int
call|)
argument_list|(
name|to
operator|-
name|from
argument_list|)
decl_stmt|;
name|ByteBuffer
name|data
init|=
name|drl
operator|.
name|getData
argument_list|()
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|data
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|arrayOffset
operator|+
name|offsetFromReadStart
argument_list|,
name|candidateSize
argument_list|)
expr_stmt|;
name|sizeRead
operator|+=
name|candidateSize
expr_stmt|;
name|drl
operator|=
name|drl
operator|.
name|next
expr_stmt|;
block|}
name|validateAndUpdatePosition
argument_list|(
name|len
argument_list|,
name|sizeRead
argument_list|)
expr_stmt|;
return|return
name|len
return|;
block|}
name|int
name|maxAlloc
init|=
name|cache
operator|.
name|getAllocator
argument_list|()
operator|.
name|getMaxAllocation
argument_list|()
decl_stmt|;
comment|// We have some disk data. Separate it by column chunk and put into cache.
comment|// We started with a single DRL, so we assume there will be no consecutive missing blocks
comment|// after the cache has inserted cache data. We also assume all the missing parts will
comment|// represent one or several column chunks, since we always cache on column chunk boundaries.
name|DiskRangeList
name|current
init|=
name|drl
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSDataInputStream
name|is
init|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|,
name|bufferSize
argument_list|)
decl_stmt|;
name|Allocator
name|allocator
init|=
name|cache
operator|.
name|getAllocator
argument_list|()
decl_stmt|;
name|long
name|sizeRead
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|DiskRangeList
name|candidate
init|=
name|current
decl_stmt|;
name|current
operator|=
name|current
operator|.
name|next
expr_stmt|;
name|long
name|from
init|=
name|candidate
operator|.
name|getOffset
argument_list|()
decl_stmt|,
name|to
init|=
name|candidate
operator|.
name|getEnd
argument_list|()
decl_stmt|;
comment|// The offset in the destination array for the beginning of this missing range.
name|int
name|offsetFromReadStart
init|=
call|(
name|int
call|)
argument_list|(
name|from
operator|-
name|readStartPos
argument_list|)
decl_stmt|,
name|candidateSize
init|=
call|(
name|int
call|)
argument_list|(
name|to
operator|-
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|candidate
operator|.
name|hasData
argument_list|()
condition|)
block|{
name|ByteBuffer
name|data
init|=
name|candidate
operator|.
name|getData
argument_list|()
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|data
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|arrayOffset
operator|+
name|offsetFromReadStart
argument_list|,
name|candidateSize
argument_list|)
expr_stmt|;
name|sizeRead
operator|+=
name|candidateSize
expr_stmt|;
continue|continue;
block|}
comment|// The data is not in cache.
comment|// Account for potential partial chunks.
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|chunksInThisRead
init|=
name|getAndValidateMissingChunks
argument_list|(
name|maxAlloc
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
decl_stmt|;
name|is
operator|.
name|seek
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|is
operator|.
name|readFully
argument_list|(
name|array
argument_list|,
name|arrayOffset
operator|+
name|offsetFromReadStart
argument_list|,
name|candidateSize
argument_list|)
expr_stmt|;
name|sizeRead
operator|+=
name|candidateSize
expr_stmt|;
comment|// Now copy missing chunks (and parts of chunks) into cache buffers.
if|if
condition|(
name|fileKey
operator|==
literal|null
operator|||
name|cache
operator|==
literal|null
condition|)
continue|continue;
name|int
name|extraDiskDataOffset
init|=
literal|0
decl_stmt|;
comment|// TODO: should we try to make a giant array for one cache call to avoid overhead?
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|missingChunk
range|:
name|chunksInThisRead
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|long
name|chunkFrom
init|=
name|Math
operator|.
name|max
argument_list|(
name|from
argument_list|,
name|missingChunk
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|,
name|chunkTo
init|=
name|Math
operator|.
name|min
argument_list|(
name|to
argument_list|,
name|missingChunk
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|,
name|chunkLength
init|=
name|chunkTo
operator|-
name|chunkFrom
decl_stmt|;
name|MemoryBuffer
index|[]
name|largeBuffers
init|=
literal|null
decl_stmt|,
name|smallBuffer
init|=
literal|null
decl_stmt|,
name|newCacheData
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|largeBufCount
init|=
call|(
name|int
call|)
argument_list|(
name|chunkLength
operator|/
name|maxAlloc
argument_list|)
decl_stmt|;
name|int
name|smallSize
init|=
call|(
name|int
call|)
argument_list|(
name|chunkLength
operator|%
name|maxAlloc
argument_list|)
decl_stmt|;
name|int
name|chunkPartCount
init|=
name|largeBufCount
operator|+
operator|(
operator|(
name|smallSize
operator|>
literal|0
operator|)
condition|?
literal|1
else|:
literal|0
operator|)
decl_stmt|;
name|DiskRange
index|[]
name|cacheRanges
init|=
operator|new
name|DiskRange
index|[
name|chunkPartCount
index|]
decl_stmt|;
name|int
name|extraOffsetInChunk
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|maxAlloc
operator|<
name|chunkLength
condition|)
block|{
name|largeBuffers
operator|=
operator|new
name|MemoryBuffer
index|[
name|largeBufCount
index|]
expr_stmt|;
name|allocator
operator|.
name|allocateMultiple
argument_list|(
name|largeBuffers
argument_list|,
name|maxAlloc
argument_list|,
name|cache
operator|.
name|getDataBufferFactory
argument_list|()
argument_list|)
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
name|largeBuffers
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// By definition here we copy up to the limit of the buffer.
name|ByteBuffer
name|bb
init|=
name|largeBuffers
index|[
name|i
index|]
operator|.
name|getByteBufferRaw
argument_list|()
decl_stmt|;
name|int
name|remaining
init|=
name|bb
operator|.
name|remaining
argument_list|()
decl_stmt|;
assert|assert
name|remaining
operator|==
name|maxAlloc
assert|;
name|copyDiskDataToCacheBuffer
argument_list|(
name|array
argument_list|,
name|arrayOffset
operator|+
name|offsetFromReadStart
operator|+
name|extraDiskDataOffset
argument_list|,
name|remaining
argument_list|,
name|bb
argument_list|,
name|cacheRanges
argument_list|,
name|i
argument_list|,
name|chunkFrom
operator|+
name|extraOffsetInChunk
argument_list|)
expr_stmt|;
name|extraDiskDataOffset
operator|+=
name|remaining
expr_stmt|;
name|extraOffsetInChunk
operator|+=
name|remaining
expr_stmt|;
block|}
block|}
name|newCacheData
operator|=
name|largeBuffers
expr_stmt|;
name|largeBuffers
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|smallSize
operator|>
literal|0
condition|)
block|{
name|smallBuffer
operator|=
operator|new
name|MemoryBuffer
index|[
literal|1
index|]
expr_stmt|;
name|allocator
operator|.
name|allocateMultiple
argument_list|(
name|smallBuffer
argument_list|,
name|smallSize
argument_list|,
name|cache
operator|.
name|getDataBufferFactory
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBuffer
name|bb
init|=
name|smallBuffer
index|[
literal|0
index|]
operator|.
name|getByteBufferRaw
argument_list|()
decl_stmt|;
name|copyDiskDataToCacheBuffer
argument_list|(
name|array
argument_list|,
name|arrayOffset
operator|+
name|offsetFromReadStart
operator|+
name|extraDiskDataOffset
argument_list|,
name|smallSize
argument_list|,
name|bb
argument_list|,
name|cacheRanges
argument_list|,
name|largeBufCount
argument_list|,
name|chunkFrom
operator|+
name|extraOffsetInChunk
argument_list|)
expr_stmt|;
name|extraDiskDataOffset
operator|+=
name|smallSize
expr_stmt|;
name|extraOffsetInChunk
operator|+=
name|smallSize
expr_stmt|;
comment|// Not strictly necessary, noone will look at it.
if|if
condition|(
name|newCacheData
operator|==
literal|null
condition|)
block|{
name|newCacheData
operator|=
name|smallBuffer
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: add allocate overload with an offset and length
name|MemoryBuffer
index|[]
name|combinedCacheData
init|=
operator|new
name|MemoryBuffer
index|[
name|largeBufCount
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|newCacheData
argument_list|,
literal|0
argument_list|,
name|combinedCacheData
argument_list|,
literal|0
argument_list|,
name|largeBufCount
argument_list|)
expr_stmt|;
name|newCacheData
operator|=
name|combinedCacheData
expr_stmt|;
name|newCacheData
index|[
name|largeBufCount
index|]
operator|=
name|smallBuffer
index|[
literal|0
index|]
expr_stmt|;
block|}
name|smallBuffer
operator|=
literal|null
expr_stmt|;
block|}
name|cache
operator|.
name|putFileData
argument_list|(
name|fileKey
argument_list|,
name|cacheRanges
argument_list|,
name|newCacheData
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// We do not use the new cache buffers for the actual read, given the way read() API is.
comment|// Therefore, we don't need to handle cache collisions - just decref all the buffers.
if|if
condition|(
name|newCacheData
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|MemoryBuffer
name|buffer
range|:
name|newCacheData
control|)
block|{
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
continue|continue;
name|cache
operator|.
name|releaseBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If we have failed before building newCacheData, deallocate other the allocated.
if|if
condition|(
name|largeBuffers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|MemoryBuffer
name|buffer
range|:
name|largeBuffers
control|)
block|{
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
continue|continue;
name|allocator
operator|.
name|deallocate
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|smallBuffer
operator|!=
literal|null
operator|&&
name|smallBuffer
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
name|allocator
operator|.
name|deallocate
argument_list|(
name|smallBuffer
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|validateAndUpdatePosition
argument_list|(
name|len
argument_list|,
name|sizeRead
argument_list|)
expr_stmt|;
return|return
name|len
return|;
block|}
specifier|private
name|void
name|validateAndUpdatePosition
parameter_list|(
name|int
name|len
parameter_list|,
name|long
name|sizeRead
parameter_list|)
block|{
if|if
condition|(
name|sizeRead
operator|!=
name|len
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Reading at "
operator|+
name|position
operator|+
literal|" for "
operator|+
name|len
operator|+
literal|": "
operator|+
name|sizeRead
operator|+
literal|" bytes copied"
argument_list|)
throw|;
block|}
name|position
operator|+=
name|len
expr_stmt|;
block|}
specifier|private
name|void
name|copyDiskDataToCacheBuffer
parameter_list|(
name|byte
index|[]
name|diskData
parameter_list|,
name|int
name|offsetInDiskData
parameter_list|,
name|int
name|sizeToCopy
parameter_list|,
name|ByteBuffer
name|cacheBuffer
parameter_list|,
name|DiskRange
index|[]
name|cacheRanges
parameter_list|,
name|int
name|cacheRangeIx
parameter_list|,
name|long
name|cacheRangeStart
parameter_list|)
block|{
name|int
name|bbPos
init|=
name|cacheBuffer
operator|.
name|position
argument_list|()
decl_stmt|;
name|long
name|cacheRangeEnd
init|=
name|cacheRangeStart
operator|+
name|sizeToCopy
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Caching ["
operator|+
name|cacheRangeStart
operator|+
literal|", "
operator|+
name|cacheRangeEnd
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|cacheRanges
index|[
name|cacheRangeIx
index|]
operator|=
operator|new
name|DiskRange
argument_list|(
name|cacheRangeStart
argument_list|,
name|cacheRangeEnd
argument_list|)
expr_stmt|;
name|cacheBuffer
operator|.
name|put
argument_list|(
name|diskData
argument_list|,
name|offsetInDiskData
argument_list|,
name|sizeToCopy
argument_list|)
expr_stmt|;
name|cacheBuffer
operator|.
name|position
argument_list|(
name|bbPos
argument_list|)
expr_stmt|;
block|}
specifier|private
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|getAndValidateMissingChunks
parameter_list|(
name|int
name|maxAlloc
parameter_list|,
name|long
name|from
parameter_list|,
name|long
name|to
parameter_list|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|firstMissing
init|=
name|chunkIndex
operator|.
name|floorEntry
argument_list|(
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstMissing
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No lower bound for offset "
operator|+
name|from
argument_list|)
throw|;
block|}
if|if
condition|(
name|firstMissing
operator|.
name|getValue
argument_list|()
operator|<=
name|from
operator|||
operator|(
operator|(
name|from
operator|-
name|firstMissing
operator|.
name|getKey
argument_list|()
operator|)
operator|%
name|maxAlloc
operator|)
operator|!=
literal|0
condition|)
block|{
comment|// The data does not belong to a recognized chunk, or is split wrong.
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Lower bound for offset "
operator|+
name|from
operator|+
literal|" is ["
operator|+
name|firstMissing
operator|.
name|getKey
argument_list|()
operator|+
literal|", "
operator|+
name|firstMissing
operator|.
name|getValue
argument_list|()
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|missingChunks
init|=
name|chunkIndex
operator|.
name|subMap
argument_list|(
name|firstMissing
operator|.
name|getKey
argument_list|()
argument_list|,
name|to
argument_list|)
decl_stmt|;
if|if
condition|(
name|missingChunks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No chunks for ["
operator|+
name|from
operator|+
literal|", "
operator|+
name|to
operator|+
literal|")"
argument_list|)
throw|;
block|}
name|long
name|lastMissingOffset
init|=
name|missingChunks
operator|.
name|lastKey
argument_list|()
decl_stmt|,
name|lastMissingEnd
init|=
name|missingChunks
operator|.
name|get
argument_list|(
name|lastMissingOffset
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastMissingEnd
operator|<
name|to
operator|||
operator|(
name|to
operator|!=
name|lastMissingEnd
operator|&&
operator|(
operator|(
name|to
operator|-
name|lastMissingOffset
operator|)
operator|%
name|maxAlloc
operator|)
operator|!=
literal|0
operator|)
condition|)
block|{
comment|// The data does not belong to a recognized chunk, or is split wrong.
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Lower bound for offset "
operator|+
name|to
operator|+
literal|" is ["
operator|+
name|lastMissingOffset
operator|+
literal|", "
operator|+
name|lastMissingEnd
operator|+
literal|")"
argument_list|)
throw|;
block|}
return|return
name|missingChunks
return|;
block|}
specifier|public
name|FileSystem
name|getFs
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|position
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Private
specifier|public
name|boolean
name|seekToNewSource
parameter_list|(
name|long
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|long
name|arg0
parameter_list|,
name|byte
index|[]
name|arg1
parameter_list|,
name|int
name|arg2
parameter_list|,
name|int
name|arg3
parameter_list|)
throws|throws
name|IOException
block|{
name|seek
argument_list|(
name|arg0
argument_list|)
expr_stmt|;
return|return
name|read
argument_list|(
name|arg1
argument_list|,
name|arg2
argument_list|,
name|arg3
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFully
parameter_list|(
name|long
name|arg0
parameter_list|,
name|byte
index|[]
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|read
argument_list|(
name|arg0
argument_list|,
name|arg1
argument_list|,
literal|0
argument_list|,
name|arg1
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFully
parameter_list|(
name|long
name|arg0
parameter_list|,
name|byte
index|[]
name|arg1
parameter_list|,
name|int
name|arg2
parameter_list|,
name|int
name|arg3
parameter_list|)
throws|throws
name|IOException
block|{
name|read
argument_list|(
name|arg0
argument_list|,
name|arg1
argument_list|,
literal|0
argument_list|,
name|arg1
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

