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
name|io
operator|.
name|encoded
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|cache
operator|.
name|Cache
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
name|EncodedColumn
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|cache
operator|.
name|Allocator
operator|.
name|LlapBuffer
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|orc
operator|.
name|OrcCacheKey
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
name|metadata
operator|.
name|OrcMetadataCache
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
name|OrcFile
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
name|OrcInputFormat
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
name|Type
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
name|Reader
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
name|RecordReader
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
name|StripeInformation
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
name|sarg
operator|.
name|SearchArgument
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_class
specifier|public
class|class
name|OrcEncodedDataProducer
implements|implements
name|EncodedDataProducer
argument_list|<
name|OrcBatchKey
argument_list|>
block|{
specifier|private
name|FileSystem
name|cachedFs
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|OrcMetadataCache
name|metadataCache
decl_stmt|;
specifier|private
specifier|final
name|Allocator
name|allocator
decl_stmt|;
specifier|private
specifier|final
name|Cache
argument_list|<
name|OrcCacheKey
argument_list|>
name|cache
decl_stmt|;
specifier|private
class|class
name|OrcEncodedDataReader
implements|implements
name|EncodedDataReader
argument_list|<
name|OrcBatchKey
argument_list|>
implements|,
name|Consumer
argument_list|<
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
block|{
specifier|private
specifier|final
name|FileSplit
name|split
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
decl_stmt|;
specifier|private
specifier|final
name|SearchArgument
name|sarg
decl_stmt|;
specifier|private
specifier|final
name|Consumer
argument_list|<
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
name|consumer
decl_stmt|;
comment|// Read state.
specifier|private
name|int
name|stripeIxFrom
decl_stmt|,
name|stripeIxTo
decl_stmt|;
specifier|private
name|Reader
name|orcReader
decl_stmt|;
specifier|private
specifier|final
name|String
name|internedFilePath
decl_stmt|;
comment|/**      * readState[stripeIx'][colIx'] - bitmask (as long array) of rg-s that are done.      * Bitmasks are all well-known size so we don't bother with BitSets and such.      * Each long has natural bit indexes used, so rightmost bits are filled first.      */
specifier|private
name|long
index|[]
index|[]
index|[]
name|readState
decl_stmt|;
specifier|private
name|int
index|[]
name|rgsPerStripe
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
specifier|public
name|OrcEncodedDataReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|Consumer
argument_list|<
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
name|consumer
parameter_list|)
block|{
name|this
operator|.
name|split
operator|=
operator|(
name|FileSplit
operator|)
name|split
expr_stmt|;
name|this
operator|.
name|internedFilePath
operator|=
name|this
operator|.
name|split
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|intern
argument_list|()
expr_stmt|;
name|this
operator|.
name|columnIds
operator|=
name|columnIds
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|columnIds
operator|!=
literal|null
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|this
operator|.
name|columnIds
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|sarg
operator|=
name|sarg
expr_stmt|;
name|this
operator|.
name|consumer
operator|=
name|consumer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|isStopped
operator|=
literal|true
expr_stmt|;
comment|// TODO: stop fetching if still in progress
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing split for "
operator|+
name|internedFilePath
argument_list|)
expr_stmt|;
if|if
condition|(
name|isStopped
condition|)
return|return;
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|stripes
init|=
name|metadataCache
operator|.
name|getStripes
argument_list|(
name|internedFilePath
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Type
argument_list|>
name|types
init|=
name|metadataCache
operator|.
name|getTypes
argument_list|(
name|internedFilePath
argument_list|)
decl_stmt|;
name|orcReader
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|stripes
operator|==
literal|null
operator|||
name|types
operator|==
literal|null
condition|)
block|{
name|orcReader
operator|=
name|createOrcReader
argument_list|(
name|split
argument_list|)
expr_stmt|;
name|stripes
operator|=
name|metadataCache
operator|.
name|getStripes
argument_list|(
name|internedFilePath
argument_list|)
expr_stmt|;
name|types
operator|=
name|metadataCache
operator|.
name|getTypes
argument_list|(
name|internedFilePath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnIds
operator|==
literal|null
condition|)
block|{
name|columnIds
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|types
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|types
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|columnIds
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|determineWhatToRead
argument_list|(
name|stripes
argument_list|)
expr_stmt|;
if|if
condition|(
name|isStopped
condition|)
return|return;
name|List
argument_list|<
name|Integer
argument_list|>
index|[]
name|stripeColumnsToRead
init|=
name|produceDataFromCache
argument_list|()
decl_stmt|;
comment|// readState now contains some 1s for column x rgs that were fetched from cache.
comment|// TODO: I/O threadpool would be here; for now, linear and inefficient
for|for
control|(
name|int
name|stripeIxMod
init|=
literal|0
init|;
name|stripeIxMod
operator|<
name|readState
operator|.
name|length
condition|;
operator|++
name|stripeIxMod
control|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|colsToRead
init|=
name|stripeColumnsToRead
index|[
name|stripeIxMod
index|]
decl_stmt|;
name|long
index|[]
index|[]
name|colRgs
init|=
name|readState
index|[
name|stripeIxMod
index|]
decl_stmt|;
if|if
condition|(
name|colsToRead
operator|==
literal|null
condition|)
block|{
name|colsToRead
operator|=
name|columnIds
expr_stmt|;
block|}
if|if
condition|(
name|colsToRead
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
comment|// All the data for this stripe was in cache.
if|if
condition|(
name|colsToRead
operator|.
name|size
argument_list|()
operator|!=
name|colRgs
operator|.
name|length
condition|)
block|{
comment|// We are reading subset of the original columns, remove unnecessary bitmasks.
name|long
index|[]
index|[]
name|colRgs2
init|=
operator|new
name|long
index|[
name|colsToRead
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|i2
init|=
operator|-
literal|1
init|;
name|i
operator|<
name|colRgs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|colRgs
index|[
name|i
index|]
operator|==
literal|null
condition|)
continue|continue;
name|colRgs2
index|[
operator|++
name|i2
index|]
operator|=
name|colRgs
index|[
name|i
index|]
expr_stmt|;
block|}
name|colRgs
operator|=
name|colRgs2
expr_stmt|;
block|}
name|int
name|stripeIx
init|=
name|stripeIxFrom
operator|+
name|stripeIxMod
decl_stmt|;
name|StripeInformation
name|si
init|=
name|stripes
operator|.
name|get
argument_list|(
name|stripeIx
argument_list|)
decl_stmt|;
name|int
name|rgCount
init|=
name|rgsPerStripe
index|[
name|stripeIxMod
index|]
decl_stmt|;
name|boolean
index|[]
name|includes
init|=
name|OrcInputFormat
operator|.
name|genIncludedColumns
argument_list|(
name|types
argument_list|,
name|colsToRead
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|orcReader
operator|==
literal|null
condition|)
block|{
name|orcReader
operator|=
name|createOrcReader
argument_list|(
name|split
argument_list|)
expr_stmt|;
block|}
name|RecordReader
name|stripeReader
init|=
name|orcReader
operator|.
name|rows
argument_list|(
name|si
operator|.
name|getOffset
argument_list|()
argument_list|,
name|si
operator|.
name|getLength
argument_list|()
argument_list|,
name|includes
argument_list|)
decl_stmt|;
comment|// We pass in the already-filtered RGs, as well as sarg. ORC can apply additional filtering.
name|stripeReader
operator|.
name|readEncodedColumns
argument_list|(
name|colRgs
argument_list|,
name|rgCount
argument_list|,
name|sarg
argument_list|,
name|this
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
name|stripeReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|consumer
operator|.
name|setDone
argument_list|()
expr_stmt|;
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceMttEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"done processing "
operator|+
name|split
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|returnData
parameter_list|(
name|LlapBuffer
name|data
parameter_list|)
block|{
comment|// TODO#: return the data to cache (unlock)
block|}
specifier|private
name|void
name|determineWhatToRead
parameter_list|(
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|stripes
parameter_list|)
block|{
comment|// The unit of caching for ORC is (stripe x column) (see OrcBatchKey). Note that we do not use
comment|// SARG anywhere, because file-level filtering on sarg is already performed during split
comment|// generation, and stripe-level filtering to get row groups is not very helpful right now.
name|long
name|offset
init|=
name|split
operator|.
name|getStart
argument_list|()
decl_stmt|,
name|maxOffset
init|=
name|offset
operator|+
name|split
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|stripeIxFrom
operator|=
name|stripeIxTo
operator|=
operator|-
literal|1
expr_stmt|;
name|int
name|stripeIx
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|String
name|tmp
init|=
literal|"FileSplit {"
operator|+
name|split
operator|.
name|getStart
argument_list|()
operator|+
literal|", "
operator|+
name|split
operator|.
name|getLength
argument_list|()
operator|+
literal|"}; stripes "
decl_stmt|;
for|for
control|(
name|StripeInformation
name|stripe
range|:
name|stripes
control|)
block|{
name|tmp
operator|+=
literal|"{"
operator|+
name|stripe
operator|.
name|getOffset
argument_list|()
operator|+
literal|", "
operator|+
name|stripe
operator|.
name|getLength
argument_list|()
operator|+
literal|"}, "
expr_stmt|;
block|}
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|debug
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|stripeRgCounts
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|stripes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|StripeInformation
name|stripe
range|:
name|stripes
control|)
block|{
name|long
name|stripeStart
init|=
name|stripe
operator|.
name|getOffset
argument_list|()
decl_stmt|;
if|if
condition|(
name|offset
operator|>
name|stripeStart
condition|)
continue|continue;
if|if
condition|(
name|stripeIxFrom
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Including from "
operator|+
name|stripeIx
operator|+
literal|" ("
operator|+
name|stripeStart
operator|+
literal|">= "
operator|+
name|offset
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|stripeIxFrom
operator|=
name|stripeIx
expr_stmt|;
block|}
if|if
condition|(
name|stripeStart
operator|>=
name|maxOffset
condition|)
block|{
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Including until "
operator|+
name|stripeIxTo
operator|+
literal|" ("
operator|+
name|stripeStart
operator|+
literal|">= "
operator|+
name|maxOffset
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|stripeIxTo
operator|=
name|stripeIx
expr_stmt|;
break|break;
block|}
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
name|orcReader
operator|.
name|getRowIndexStride
argument_list|()
argument_list|)
decl_stmt|;
name|stripeRgCounts
operator|.
name|add
argument_list|(
name|rgCount
argument_list|)
expr_stmt|;
operator|++
name|stripeIx
expr_stmt|;
block|}
if|if
condition|(
name|stripeIxTo
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|DebugUtils
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Including until "
operator|+
name|stripeIx
operator|+
literal|" (end of file)"
argument_list|)
expr_stmt|;
block|}
name|stripeIxTo
operator|=
name|stripeIx
expr_stmt|;
block|}
name|readState
operator|=
operator|new
name|long
index|[
name|stripeRgCounts
operator|.
name|size
argument_list|()
index|]
index|[]
index|[]
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
name|stripeRgCounts
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|int
name|bitmaskSize
init|=
name|align64
argument_list|(
name|stripeRgCounts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|>>>
literal|6
decl_stmt|;
name|readState
index|[
name|i
index|]
operator|=
operator|new
name|long
index|[
name|columnIds
operator|.
name|size
argument_list|()
index|]
index|[]
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|columnIds
operator|.
name|size
argument_list|()
condition|;
operator|++
name|j
control|)
block|{
name|readState
index|[
name|i
index|]
index|[
name|j
index|]
operator|=
operator|new
name|long
index|[
name|bitmaskSize
index|]
expr_stmt|;
block|}
block|}
name|rgsPerStripe
operator|=
operator|new
name|int
index|[
name|stripeRgCounts
operator|.
name|size
argument_list|()
index|]
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
name|rgsPerStripe
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|rgsPerStripe
index|[
name|i
index|]
operator|=
name|stripeRgCounts
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO: split by stripe? we do everything by stripe, and it might be faster
comment|// TODO: return type provisional depending on ORC API
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
index|[]
name|produceDataFromCache
parameter_list|()
block|{
comment|// Assumes none of the columns are fetched, because we always do this before reading.
name|OrcCacheKey
name|key
init|=
operator|new
name|OrcCacheKey
argument_list|(
name|internedFilePath
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// Grr, no generics arrays, "J" in "Java" stands for "joke".
name|List
argument_list|<
name|Integer
argument_list|>
index|[]
name|stripeColsNotInCache
init|=
operator|new
name|List
index|[
name|readState
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|stripeIxMod
init|=
literal|0
init|;
name|stripeIxMod
operator|<
name|readState
operator|.
name|length
condition|;
operator|++
name|stripeIxMod
control|)
block|{
name|key
operator|.
name|stripeIx
operator|=
name|stripeIxFrom
operator|+
name|stripeIxMod
expr_stmt|;
name|long
index|[]
index|[]
name|cols
init|=
name|readState
index|[
name|stripeIxMod
index|]
decl_stmt|;
name|int
name|rgCount
init|=
name|rgsPerStripe
index|[
name|stripeIxMod
index|]
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
name|cols
operator|.
name|length
condition|;
operator|++
name|colIxMod
control|)
block|{
name|key
operator|.
name|colIx
operator|=
name|columnIds
operator|.
name|get
argument_list|(
name|colIxMod
argument_list|)
expr_stmt|;
name|long
index|[]
name|doneMask
init|=
name|cols
index|[
name|colIxMod
index|]
decl_stmt|;
name|boolean
name|areAllRgsInCache
init|=
literal|true
decl_stmt|;
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
name|key
operator|.
name|rgIx
operator|=
name|rgIx
expr_stmt|;
name|LlapBuffer
name|cached
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cached
operator|==
literal|null
condition|)
block|{
name|areAllRgsInCache
operator|=
literal|false
expr_stmt|;
continue|continue;
block|}
comment|// TODO: pool of EncodedColumn-s objects. Someone will need to return them though.
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
name|col
init|=
operator|new
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|(
name|key
operator|.
name|copyToPureBatchKey
argument_list|()
argument_list|,
name|key
operator|.
name|colIx
argument_list|,
name|cached
argument_list|)
decl_stmt|;
name|consumer
operator|.
name|consumeData
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|doneMask
index|[
name|rgIx
operator|>>>
literal|6
index|]
operator||=
literal|1
operator|<<
operator|(
name|rgIx
operator|&
literal|63
operator|)
expr_stmt|;
block|}
name|boolean
name|hasFetchList
init|=
name|stripeColsNotInCache
index|[
name|stripeIxMod
index|]
operator|!=
literal|null
decl_stmt|;
if|if
condition|(
name|areAllRgsInCache
condition|)
block|{
name|cols
index|[
name|colIxMod
index|]
operator|=
literal|null
expr_stmt|;
comment|// No need for bitmask, all rgs are done.
if|if
condition|(
operator|!
name|hasFetchList
condition|)
block|{
comment|// All rgs for this stripe x column were fetched from cache. If this is the first
comment|// such column, create custom, smaller list of columns to fetch later for this
comment|// stripe (default is all the columns originally requested). Add all previous
comment|// columns, need to fetch them since this is the first column.
name|stripeColsNotInCache
index|[
name|stripeIxMod
index|]
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|cols
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|stripeIxMod
operator|>
literal|0
condition|)
block|{
name|stripeColsNotInCache
index|[
name|stripeIxMod
index|]
operator|.
name|addAll
argument_list|(
name|columnIds
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|colIxMod
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|hasFetchList
condition|)
block|{
comment|// Only a subset of original columnIds need to be fetched for this stripe;
comment|// add the current one to this sublist.
name|stripeColsNotInCache
index|[
name|stripeIxMod
index|]
operator|.
name|add
argument_list|(
name|columnIds
operator|.
name|get
argument_list|(
name|colIxMod
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|stripeColsNotInCache
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDone
parameter_list|()
block|{
name|consumer
operator|.
name|setDone
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeData
parameter_list|(
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
name|data
parameter_list|)
block|{
comment|// Store object in cache; create new key object - cannot be reused.
name|OrcCacheKey
name|key
init|=
operator|new
name|OrcCacheKey
argument_list|(
name|data
operator|.
name|batchKey
argument_list|,
name|data
operator|.
name|columnIndex
argument_list|)
decl_stmt|;
name|LlapBuffer
name|cached
init|=
name|cache
operator|.
name|cacheOrGet
argument_list|(
name|key
argument_list|,
name|data
operator|.
name|columnData
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|columnData
operator|!=
name|cached
condition|)
block|{
name|allocator
operator|.
name|deallocate
argument_list|(
name|data
operator|.
name|columnData
argument_list|)
expr_stmt|;
name|data
operator|.
name|columnData
operator|=
name|cached
expr_stmt|;
block|}
name|consumer
operator|.
name|consumeData
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|consumer
operator|.
name|setError
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Reader
name|createOrcReader
parameter_list|(
name|FileSplit
name|fileSplit
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|cachedFs
decl_stmt|;
name|Path
name|path
init|=
name|fileSplit
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"pfile"
operator|.
name|equals
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
condition|)
block|{
name|fs
operator|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Cannot use cached FS due to hive tests' proxy FS.
block|}
if|if
condition|(
name|metadataCache
operator|==
literal|null
condition|)
block|{
name|metadataCache
operator|=
operator|new
name|OrcMetadataCache
argument_list|(
name|cachedFs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|OrcFile
operator|.
name|createReader
argument_list|(
name|path
argument_list|,
name|OrcFile
operator|.
name|readerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|align64
parameter_list|(
name|int
name|number
parameter_list|)
block|{
name|int
name|rem
init|=
name|number
operator|&
literal|63
decl_stmt|;
return|return
name|number
operator|-
name|rem
operator|+
operator|(
name|rem
operator|==
literal|0
condition|?
literal|0
else|:
literal|64
operator|)
return|;
block|}
specifier|public
name|OrcEncodedDataProducer
parameter_list|(
name|Allocator
name|allocator
parameter_list|,
name|Cache
argument_list|<
name|OrcCacheKey
argument_list|>
name|cache
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We assume all splits will come from the same FS.
name|this
operator|.
name|cachedFs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|metadataCache
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|EncodedDataReader
argument_list|<
name|OrcBatchKey
argument_list|>
name|getReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|Consumer
argument_list|<
name|EncodedColumn
argument_list|<
name|OrcBatchKey
argument_list|>
argument_list|>
name|consumer
parameter_list|)
block|{
return|return
operator|new
name|OrcEncodedDataReader
argument_list|(
name|split
argument_list|,
name|columnIds
argument_list|,
name|sarg
argument_list|,
name|consumer
argument_list|)
return|;
block|}
block|}
end_class

end_unit

