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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hive
operator|.
name|common
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
name|DiskRangeList
operator|.
name|DiskRangeListCreateHelper
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
name|DiskRangeList
operator|.
name|DiskRangeListMutateHelper
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
name|Consumer
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
name|EncodedColumnBatch
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
name|EncodedColumnBatch
operator|.
name|StreamBuffer
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
name|cache
operator|.
name|LowLevelCache
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
name|orc
operator|.
name|OrcBatchKey
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
name|OrcProto
operator|.
name|ColumnEncoding
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
name|OrcProto
operator|.
name|RowIndex
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
name|OrcProto
operator|.
name|RowIndexEntry
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
name|OrcProto
operator|.
name|Stream
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
name|RecordReaderImpl
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|RecordReaderUtils
operator|.
name|ByteBufferAllocatorPool
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
operator|.
name|ZeroCopyReaderShim
import|;
end_import

begin_class
specifier|public
class|class
name|EncodedReaderImpl
implements|implements
name|EncodedReader
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|EncodedReaderImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|fileId
decl_stmt|;
specifier|private
specifier|final
name|FSDataInputStream
name|file
decl_stmt|;
specifier|private
specifier|final
name|CompressionCodec
name|codec
decl_stmt|;
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
decl_stmt|;
specifier|private
specifier|final
name|ZeroCopyReaderShim
name|zcr
decl_stmt|;
specifier|private
specifier|final
name|long
name|rowIndexStride
decl_stmt|;
specifier|private
specifier|final
name|LowLevelCache
name|cache
decl_stmt|;
specifier|private
specifier|final
name|ByteBufferAllocatorPool
name|pool
decl_stmt|;
comment|// For now, one consumer for all calls.
specifier|private
specifier|final
name|Consumer
argument_list|<
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
name|consumer
decl_stmt|;
specifier|public
name|EncodedReaderImpl
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|useZeroCopy
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|long
name|strideRate
parameter_list|,
name|LowLevelCache
name|cache
parameter_list|,
name|Consumer
argument_list|<
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
name|consumer
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fileId
operator|=
name|RecordReaderUtils
operator|.
name|getFileId
argument_list|(
name|fileSystem
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|file
operator|=
name|fileSystem
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|codec
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|useZeroCopy
condition|?
operator|new
name|ByteBufferAllocatorPool
argument_list|()
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|zcr
operator|=
name|useZeroCopy
condition|?
name|RecordReaderUtils
operator|.
name|createZeroCopyShim
argument_list|(
name|file
argument_list|,
name|codec
argument_list|,
name|pool
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|rowIndexStride
operator|=
name|strideRate
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|consumer
operator|=
name|consumer
expr_stmt|;
if|if
condition|(
name|zcr
operator|!=
literal|null
operator|&&
operator|!
name|cache
operator|.
name|isDirectAlloc
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Cannot use zero-copy reader with non-direct cache "
operator|+
literal|"buffers; either disable zero-copy or enable direct cache allocation"
argument_list|)
throw|;
block|}
block|}
comment|/** Helper context for each column being read */
specifier|private
specifier|static
specifier|final
class|class
name|ColumnReadContext
block|{
specifier|public
name|ColumnReadContext
parameter_list|(
name|int
name|colIx
parameter_list|,
name|ColumnEncoding
name|encoding
parameter_list|,
name|RowIndex
name|rowIndex
parameter_list|)
block|{
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
name|this
operator|.
name|rowIndex
operator|=
name|rowIndex
expr_stmt|;
name|this
operator|.
name|colIx
operator|=
name|colIx
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|MAX_STREAMS
init|=
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|ROW_INDEX_VALUE
decl_stmt|;
comment|/** The number of streams that are part of this column. */
name|int
name|streamCount
init|=
literal|0
decl_stmt|;
specifier|final
name|StreamContext
index|[]
name|streams
init|=
operator|new
name|StreamContext
index|[
name|MAX_STREAMS
index|]
decl_stmt|;
comment|/** Column encoding. */
specifier|final
name|ColumnEncoding
name|encoding
decl_stmt|;
comment|/** Column rowindex. */
specifier|final
name|OrcProto
operator|.
name|RowIndex
name|rowIndex
decl_stmt|;
comment|/** Column index in the file. */
specifier|final
name|int
name|colIx
decl_stmt|;
specifier|public
name|void
name|addStream
parameter_list|(
name|long
name|offset
parameter_list|,
name|OrcProto
operator|.
name|Stream
name|stream
parameter_list|,
name|int
name|indexIx
parameter_list|)
block|{
name|streams
index|[
name|streamCount
operator|++
index|]
operator|=
operator|new
name|StreamContext
argument_list|(
name|stream
argument_list|,
name|offset
argument_list|,
name|indexIx
argument_list|)
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
literal|" column_index: "
argument_list|)
operator|.
name|append
argument_list|(
name|colIx
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" encoding: "
argument_list|)
operator|.
name|append
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" stream_count: "
argument_list|)
operator|.
name|append
argument_list|(
name|streamCount
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StreamContext
name|sc
range|:
name|streams
control|)
block|{
if|if
condition|(
name|sc
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" stream_"
argument_list|)
operator|.
name|append
argument_list|(
name|i
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|sc
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|StreamContext
block|{
specifier|public
name|StreamContext
parameter_list|(
name|OrcProto
operator|.
name|Stream
name|stream
parameter_list|,
name|long
name|streamOffset
parameter_list|,
name|int
name|streamIndexOffset
parameter_list|)
block|{
name|this
operator|.
name|kind
operator|=
name|stream
operator|.
name|getKind
argument_list|()
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|stream
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|streamOffset
expr_stmt|;
name|this
operator|.
name|streamIndexOffset
operator|=
name|streamIndexOffset
expr_stmt|;
block|}
comment|/** Offsets of each stream in the column. */
specifier|public
specifier|final
name|long
name|offset
decl_stmt|,
name|length
decl_stmt|;
specifier|public
specifier|final
name|int
name|streamIndexOffset
decl_stmt|;
specifier|public
specifier|final
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
name|kind
decl_stmt|;
comment|/** Iterators for the buffers; used to maintain position in per-rg reading. */
name|DiskRangeList
name|bufferIter
decl_stmt|;
comment|/** Saved stripe-level stream, to reuse for each RG (e.g. dictionaries). */
name|StreamBuffer
name|stripeLevelStream
decl_stmt|;
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
literal|" kind: "
argument_list|)
operator|.
name|append
argument_list|(
name|kind
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" offset: "
argument_list|)
operator|.
name|append
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" length: "
argument_list|)
operator|.
name|append
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" index_offset: "
argument_list|)
operator|.
name|append
argument_list|(
name|streamIndexOffset
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readEncodedColumns
parameter_list|(
name|int
name|stripeIx
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|RowIndex
index|[]
name|indexes
parameter_list|,
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
parameter_list|,
name|List
argument_list|<
name|Stream
argument_list|>
name|streamList
parameter_list|,
name|boolean
index|[]
name|included
parameter_list|,
name|boolean
index|[]
index|[]
name|colRgs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Note: for now we don't have to setError here, caller will setError if we throw.
comment|// We are also not supposed to call setDone, since we are only part of the operation.
name|long
name|stripeOffset
init|=
name|stripe
operator|.
name|getOffset
argument_list|()
decl_stmt|;
comment|// 1. Figure out what we have to read.
name|long
name|offset
init|=
literal|0
decl_stmt|;
comment|// Stream offset in relation to the stripe.
comment|// 1.1. Figure out which columns have a present stream
name|boolean
index|[]
name|hasNull
init|=
name|RecordReaderUtils
operator|.
name|findPresentStreamsByColumn
argument_list|(
name|streamList
argument_list|,
name|types
argument_list|)
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The following columns have PRESENT streams: "
operator|+
name|DebugUtils
operator|.
name|toString
argument_list|(
name|hasNull
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// We assume stream list is sorted by column and that non-data
comment|// streams do not interleave data streams for the same column.
comment|// 1.2. With that in mind, determine disk ranges to read/get from cache (not by stream).
name|int
name|colRgIx
init|=
operator|-
literal|1
decl_stmt|,
name|lastColIx
init|=
operator|-
literal|1
decl_stmt|;
name|ColumnReadContext
index|[]
name|colCtxs
init|=
operator|new
name|ColumnReadContext
index|[
name|colRgs
operator|.
name|length
index|]
decl_stmt|;
name|boolean
index|[]
name|includedRgs
init|=
literal|null
decl_stmt|;
name|boolean
name|isCompressed
init|=
operator|(
name|codec
operator|!=
literal|null
operator|)
decl_stmt|;
name|DiskRangeListCreateHelper
name|listToRead
init|=
operator|new
name|DiskRangeListCreateHelper
argument_list|()
decl_stmt|;
for|for
control|(
name|OrcProto
operator|.
name|Stream
name|stream
range|:
name|streamList
control|)
block|{
name|long
name|length
init|=
name|stream
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|int
name|colIx
init|=
name|stream
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
name|streamKind
init|=
name|stream
operator|.
name|getKind
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|included
index|[
name|colIx
index|]
operator|||
name|StreamName
operator|.
name|getArea
argument_list|(
name|streamKind
argument_list|)
operator|!=
name|StreamName
operator|.
name|Area
operator|.
name|DATA
condition|)
block|{
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping stream: "
operator|+
name|streamKind
operator|+
literal|" at "
operator|+
name|offset
operator|+
literal|", "
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
name|offset
operator|+=
name|length
expr_stmt|;
continue|continue;
block|}
name|ColumnReadContext
name|ctx
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|lastColIx
operator|!=
name|colIx
condition|)
block|{
operator|++
name|colRgIx
expr_stmt|;
assert|assert
name|colCtxs
index|[
name|colRgIx
index|]
operator|==
literal|null
assert|;
name|lastColIx
operator|=
name|colIx
expr_stmt|;
name|includedRgs
operator|=
name|colRgs
index|[
name|colRgIx
index|]
expr_stmt|;
name|ctx
operator|=
name|colCtxs
index|[
name|colRgIx
index|]
operator|=
operator|new
name|ColumnReadContext
argument_list|(
name|colIx
argument_list|,
name|encodings
operator|.
name|get
argument_list|(
name|colIx
argument_list|)
argument_list|,
name|indexes
index|[
name|colIx
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating context "
operator|+
name|colRgIx
operator|+
literal|" for column "
operator|+
name|colIx
operator|+
literal|":"
operator|+
name|ctx
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ctx
operator|=
name|colCtxs
index|[
name|colRgIx
index|]
expr_stmt|;
assert|assert
name|ctx
operator|!=
literal|null
assert|;
block|}
name|int
name|indexIx
init|=
name|RecordReaderUtils
operator|.
name|getIndexPosition
argument_list|(
name|ctx
operator|.
name|encoding
operator|.
name|getKind
argument_list|()
argument_list|,
name|types
operator|.
name|get
argument_list|(
name|colIx
argument_list|)
operator|.
name|getKind
argument_list|()
argument_list|,
name|streamKind
argument_list|,
name|isCompressed
argument_list|,
name|hasNull
index|[
name|colIx
index|]
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|addStream
argument_list|(
name|offset
argument_list|,
name|stream
argument_list|,
name|indexIx
argument_list|)
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding stream for column "
operator|+
name|colIx
operator|+
literal|": "
operator|+
name|streamKind
operator|+
literal|" at "
operator|+
name|offset
operator|+
literal|", "
operator|+
name|length
operator|+
literal|", index position "
operator|+
name|indexIx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includedRgs
operator|==
literal|null
operator|||
name|RecordReaderUtils
operator|.
name|isDictionary
argument_list|(
name|streamKind
argument_list|,
name|encodings
operator|.
name|get
argument_list|(
name|colIx
argument_list|)
argument_list|)
condition|)
block|{
name|RecordReaderUtils
operator|.
name|addEntireStreamToRanges
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|listToRead
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Will read whole stream "
operator|+
name|streamKind
operator|+
literal|"; added to "
operator|+
name|listToRead
operator|.
name|getTail
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|RecordReaderUtils
operator|.
name|addRgFilteredStreamToRanges
argument_list|(
name|stream
argument_list|,
name|includedRgs
argument_list|,
name|codec
operator|!=
literal|null
argument_list|,
name|indexes
index|[
name|colIx
index|]
argument_list|,
name|encodings
operator|.
name|get
argument_list|(
name|colIx
argument_list|)
argument_list|,
name|types
operator|.
name|get
argument_list|(
name|colIx
argument_list|)
argument_list|,
name|bufferSize
argument_list|,
name|hasNull
index|[
name|colIx
index|]
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|listToRead
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|offset
operator|+=
name|length
expr_stmt|;
block|}
comment|// 2. Now, read all of the ranges from cache or disk.
name|DiskRangeListMutateHelper
name|toRead
init|=
operator|new
name|DiskRangeListMutateHelper
argument_list|(
name|listToRead
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Resulting disk ranges to read: "
operator|+
name|RecordReaderUtils
operator|.
name|stringifyDiskRanges
argument_list|(
name|toRead
operator|.
name|next
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cache
operator|!=
literal|null
condition|)
block|{
name|cache
operator|.
name|getFileData
argument_list|(
name|fileId
argument_list|,
name|toRead
operator|.
name|next
argument_list|,
name|stripeOffset
argument_list|)
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Disk ranges after cache (base offset "
operator|+
name|stripeOffset
operator|+
literal|"): "
operator|+
name|RecordReaderUtils
operator|.
name|stringifyDiskRanges
argument_list|(
name|toRead
operator|.
name|next
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Force direct buffers if we will be decompressing to direct cache.
name|RecordReaderUtils
operator|.
name|readDiskRanges
argument_list|(
name|file
argument_list|,
name|zcr
argument_list|,
name|stripeOffset
argument_list|,
name|toRead
operator|.
name|next
argument_list|,
name|cache
operator|.
name|isDirectAlloc
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Disk ranges after disk read  (base offset "
operator|+
name|stripeOffset
operator|+
literal|"): "
operator|+
name|RecordReaderUtils
operator|.
name|stringifyDiskRanges
argument_list|(
name|toRead
operator|.
name|next
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// 3. Finally, decompress data, map per RG, and return to caller.
comment|// We go by RG and not by column because that is how data is processed.
name|int
name|rgCount
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|double
operator|)
name|stripe
operator|.
name|getNumberOfRows
argument_list|()
operator|/
name|rowIndexStride
argument_list|)
decl_stmt|;
name|DiskRangeList
name|iter
init|=
name|toRead
operator|.
name|next
decl_stmt|;
comment|// Keep "toRead" list for future use, don't extract().
for|for
control|(
name|int
name|rgIx
init|=
literal|0
init|;
name|rgIx
operator|<
name|rgCount
condition|;
operator|++
name|rgIx
control|)
block|{
name|boolean
name|isLastRg
init|=
name|rgIx
operator|==
name|rgCount
operator|-
literal|1
decl_stmt|;
comment|// Create the batch we will use to return data for this RG.
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
name|ecb
init|=
operator|new
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|(
operator|new
name|OrcBatchKey
argument_list|(
name|fileId
argument_list|,
name|stripeIx
argument_list|,
name|rgIx
argument_list|)
argument_list|,
name|colRgs
operator|.
name|length
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|boolean
name|isRGSelected
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|colIxMod
init|=
literal|0
init|;
name|colIxMod
operator|<
name|colRgs
operator|.
name|length
condition|;
operator|++
name|colIxMod
control|)
block|{
if|if
condition|(
name|colRgs
index|[
name|colIxMod
index|]
operator|!=
literal|null
operator|&&
operator|!
name|colRgs
index|[
name|colIxMod
index|]
index|[
name|rgIx
index|]
condition|)
block|{
comment|// RG x col filtered.
name|isRGSelected
operator|=
literal|false
expr_stmt|;
continue|continue;
comment|// TODO#: this would be invalid with HL cache, where RG x col can be excluded.
block|}
name|ColumnReadContext
name|ctx
init|=
name|colCtxs
index|[
name|colIxMod
index|]
decl_stmt|;
name|RowIndexEntry
name|index
init|=
name|ctx
operator|.
name|rowIndex
operator|.
name|getEntry
argument_list|(
name|rgIx
argument_list|)
decl_stmt|,
name|nextIndex
init|=
name|isLastRg
condition|?
literal|null
else|:
name|ctx
operator|.
name|rowIndex
operator|.
name|getEntry
argument_list|(
name|rgIx
operator|+
literal|1
argument_list|)
decl_stmt|;
name|ecb
operator|.
name|initColumn
argument_list|(
name|colIxMod
argument_list|,
name|ctx
operator|.
name|colIx
argument_list|,
name|ctx
operator|.
name|streamCount
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|streamIx
init|=
literal|0
init|;
name|streamIx
operator|<
name|ctx
operator|.
name|streamCount
condition|;
operator|++
name|streamIx
control|)
block|{
name|StreamContext
name|sctx
init|=
name|ctx
operator|.
name|streams
index|[
name|streamIx
index|]
decl_stmt|;
name|StreamBuffer
name|cb
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|RecordReaderUtils
operator|.
name|isDictionary
argument_list|(
name|sctx
operator|.
name|kind
argument_list|,
name|ctx
operator|.
name|encoding
argument_list|)
condition|)
block|{
comment|// This stream is for entire stripe and needed for every RG; uncompress once and reuse.
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Getting stripe-level stream ["
operator|+
name|sctx
operator|.
name|kind
operator|+
literal|", "
operator|+
name|ctx
operator|.
name|encoding
operator|+
literal|"] for"
operator|+
literal|" column "
operator|+
name|ctx
operator|.
name|colIx
operator|+
literal|" RG "
operator|+
name|rgIx
operator|+
literal|" at "
operator|+
name|sctx
operator|.
name|offset
operator|+
literal|", "
operator|+
name|sctx
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sctx
operator|.
name|stripeLevelStream
operator|==
literal|null
condition|)
block|{
name|sctx
operator|.
name|stripeLevelStream
operator|=
operator|new
name|StreamBuffer
argument_list|(
name|sctx
operator|.
name|kind
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
comment|// We will be using this for each RG while also sending RGs to processing.
comment|// To avoid buffers being unlocked, run refcount one ahead; we will not increase
comment|// it when building the last RG, so each RG processing will decref once, and the
comment|// last one will unlock the buffers.
name|sctx
operator|.
name|stripeLevelStream
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|DiskRangeList
name|lastCached
init|=
name|InStream
operator|.
name|uncompressStream
argument_list|(
name|fileId
argument_list|,
name|stripeOffset
argument_list|,
name|iter
argument_list|,
name|sctx
operator|.
name|offset
argument_list|,
name|sctx
operator|.
name|offset
operator|+
name|sctx
operator|.
name|length
argument_list|,
name|zcr
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|,
name|cache
argument_list|,
name|sctx
operator|.
name|stripeLevelStream
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastCached
operator|!=
literal|null
condition|)
block|{
name|iter
operator|=
name|lastCached
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|isLastRg
condition|)
block|{
name|sctx
operator|.
name|stripeLevelStream
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
name|cb
operator|=
name|sctx
operator|.
name|stripeLevelStream
expr_stmt|;
block|}
else|else
block|{
comment|// This stream can be separated by RG using index. Let's do that.
name|long
name|cOffset
init|=
name|index
operator|.
name|getPositions
argument_list|(
name|sctx
operator|.
name|streamIndexOffset
argument_list|)
operator|+
name|sctx
operator|.
name|offset
decl_stmt|,
name|nextCOffset
init|=
name|isLastRg
condition|?
name|sctx
operator|.
name|length
else|:
name|nextIndex
operator|.
name|getPositions
argument_list|(
name|sctx
operator|.
name|streamIndexOffset
argument_list|)
decl_stmt|,
name|endCOffset
init|=
name|RecordReaderUtils
operator|.
name|estimateRgEndOffset
argument_list|(
name|isCompressed
argument_list|,
name|isLastRg
argument_list|,
name|nextCOffset
argument_list|,
name|sctx
operator|.
name|length
argument_list|,
name|bufferSize
argument_list|)
operator|+
name|sctx
operator|.
name|offset
decl_stmt|;
name|cb
operator|=
operator|new
name|StreamBuffer
argument_list|(
name|sctx
operator|.
name|kind
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
name|cb
operator|.
name|incRef
argument_list|()
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Getting data for column "
operator|+
name|ctx
operator|.
name|colIx
operator|+
literal|" "
operator|+
operator|(
name|isLastRg
condition|?
literal|"last "
else|:
literal|""
operator|)
operator|+
literal|"RG "
operator|+
name|rgIx
operator|+
literal|" stream "
operator|+
name|sctx
operator|.
name|kind
operator|+
literal|" at "
operator|+
name|sctx
operator|.
name|offset
operator|+
literal|", "
operator|+
name|sctx
operator|.
name|length
operator|+
literal|" index position "
operator|+
name|sctx
operator|.
name|streamIndexOffset
operator|+
literal|": compressed ["
operator|+
name|cOffset
operator|+
literal|", "
operator|+
name|endCOffset
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isStartOfStream
init|=
name|sctx
operator|.
name|bufferIter
operator|==
literal|null
decl_stmt|;
name|DiskRangeList
name|range
init|=
name|isStartOfStream
condition|?
name|iter
else|:
name|sctx
operator|.
name|bufferIter
decl_stmt|;
name|DiskRangeList
name|lastCached
init|=
name|InStream
operator|.
name|uncompressStream
argument_list|(
name|fileId
argument_list|,
name|stripeOffset
argument_list|,
name|range
argument_list|,
name|cOffset
argument_list|,
name|endCOffset
argument_list|,
name|zcr
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|,
name|cache
argument_list|,
name|cb
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastCached
operator|!=
literal|null
condition|)
block|{
name|sctx
operator|.
name|bufferIter
operator|=
name|iter
operator|=
name|lastCached
expr_stmt|;
comment|// Reset iter just to ensure it's valid
block|}
block|}
name|ecb
operator|.
name|setStreamData
argument_list|(
name|colIxMod
argument_list|,
name|streamIx
argument_list|,
name|cb
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isRGSelected
condition|)
block|{
name|consumer
operator|.
name|consumeData
argument_list|(
name|ecb
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceOrcEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Disk ranges after processing all the data "
operator|+
name|RecordReaderUtils
operator|.
name|stringifyDiskRanges
argument_list|(
name|toRead
operator|.
name|next
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// TODO: this is not good; we hold all the blocks until we send them all.
comment|//       Hard to avoid due to sharing by RGs... perhaps we can still do better.
name|DiskRangeList
name|toFree
init|=
name|toRead
operator|.
name|next
decl_stmt|;
while|while
condition|(
name|toFree
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|toFree
operator|instanceof
name|CacheChunk
condition|)
block|{
name|LlapMemoryBuffer
name|buffer
init|=
operator|(
operator|(
name|CacheChunk
operator|)
name|toFree
operator|)
operator|.
name|buffer
decl_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceLockingEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unlocking "
operator|+
name|buffer
operator|+
literal|" at the end of readEncodedColumns"
argument_list|)
expr_stmt|;
block|}
name|cache
operator|.
name|releaseBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|toFree
operator|=
name|toFree
operator|.
name|next
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|file
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|pool
operator|!=
literal|null
condition|)
block|{
name|pool
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

