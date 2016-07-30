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
name|decode
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|EncodedColumnBatch
operator|.
name|ColumnStreamData
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
name|counters
operator|.
name|LlapIOCounters
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
name|counters
operator|.
name|QueryFragmentCounters
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
name|ColumnVectorBatch
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
name|OrcFileMetadata
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
name|OrcStripeMetadata
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
name|metrics
operator|.
name|LlapDaemonIOMetrics
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
name|BytesColumnVector
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
name|ColumnVector
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
name|DecimalColumnVector
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
name|DoubleColumnVector
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
name|ListColumnVector
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
name|LongColumnVector
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
name|MapColumnVector
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
name|StructColumnVector
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
name|TimestampColumnVector
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
name|UnionColumnVector
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
name|VectorizedRowBatch
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
name|CompressionCodec
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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|encoded
operator|.
name|EncodedTreeReaderFactory
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
name|EncodedTreeReaderFactory
operator|.
name|SettableTreeReader
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
name|encoded
operator|.
name|Reader
operator|.
name|OrcEncodedColumnBatch
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
name|OrcUtils
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
name|TypeDescription
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
name|TreeReaderFactory
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
name|WriterImpl
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
name|OrcProto
import|;
end_import

begin_class
specifier|public
class|class
name|OrcEncodedDataConsumer
extends|extends
name|EncodedDataConsumer
argument_list|<
name|OrcBatchKey
argument_list|,
name|OrcEncodedColumnBatch
argument_list|>
block|{
specifier|private
name|TreeReaderFactory
operator|.
name|TreeReader
index|[]
name|columnReaders
decl_stmt|;
specifier|private
name|int
name|previousStripeIndex
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|OrcFileMetadata
name|fileMetadata
decl_stmt|;
comment|// We assume one request is only for one file.
specifier|private
name|CompressionCodec
name|codec
decl_stmt|;
specifier|private
name|OrcStripeMetadata
index|[]
name|stripes
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|skipCorrupt
decl_stmt|;
comment|// TODO: get rid of this
specifier|private
specifier|final
name|QueryFragmentCounters
name|counters
decl_stmt|;
specifier|private
name|boolean
index|[]
name|includedColumns
decl_stmt|;
specifier|public
name|OrcEncodedDataConsumer
parameter_list|(
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|consumer
parameter_list|,
name|int
name|colCount
parameter_list|,
name|boolean
name|skipCorrupt
parameter_list|,
name|QueryFragmentCounters
name|counters
parameter_list|,
name|LlapDaemonIOMetrics
name|ioMetrics
parameter_list|)
block|{
name|super
argument_list|(
name|consumer
argument_list|,
name|colCount
argument_list|,
name|ioMetrics
argument_list|)
expr_stmt|;
comment|// TODO: get rid of this
name|this
operator|.
name|skipCorrupt
operator|=
name|skipCorrupt
expr_stmt|;
name|this
operator|.
name|counters
operator|=
name|counters
expr_stmt|;
block|}
specifier|public
name|void
name|setFileMetadata
parameter_list|(
name|OrcFileMetadata
name|f
parameter_list|)
block|{
assert|assert
name|fileMetadata
operator|==
literal|null
assert|;
name|fileMetadata
operator|=
name|f
expr_stmt|;
name|stripes
operator|=
operator|new
name|OrcStripeMetadata
index|[
name|f
operator|.
name|getStripes
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|codec
operator|=
name|WriterImpl
operator|.
name|createCodec
argument_list|(
name|fileMetadata
operator|.
name|getCompressionKind
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setStripeMetadata
parameter_list|(
name|OrcStripeMetadata
name|m
parameter_list|)
block|{
assert|assert
name|stripes
operator|!=
literal|null
assert|;
name|stripes
index|[
name|m
operator|.
name|getStripeIx
argument_list|()
index|]
operator|=
name|m
expr_stmt|;
block|}
specifier|private
specifier|static
name|ColumnVector
name|createColumn
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
specifier|final
name|int
name|columnId
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
name|OrcProto
operator|.
name|Type
name|type
init|=
name|types
operator|.
name|get
argument_list|(
name|columnId
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|type
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|DATE
case|:
return|return
operator|new
name|LongColumnVector
argument_list|(
name|batchSize
argument_list|)
return|;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleColumnVector
argument_list|(
name|batchSize
argument_list|)
return|;
case|case
name|BINARY
case|:
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
return|return
operator|new
name|BytesColumnVector
argument_list|(
name|batchSize
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|TimestampColumnVector
argument_list|(
name|batchSize
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|DecimalColumnVector
argument_list|(
name|batchSize
argument_list|,
name|type
operator|.
name|getPrecision
argument_list|()
argument_list|,
name|type
operator|.
name|getScale
argument_list|()
argument_list|)
return|;
case|case
name|STRUCT
case|:
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|subtypeIdxs
init|=
name|type
operator|.
name|getSubtypesList
argument_list|()
decl_stmt|;
name|ColumnVector
index|[]
name|fieldVector
init|=
operator|new
name|ColumnVector
index|[
name|subtypeIdxs
operator|.
name|size
argument_list|()
index|]
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
name|fieldVector
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldVector
index|[
name|i
index|]
operator|=
name|createColumn
argument_list|(
name|types
argument_list|,
name|subtypeIdxs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|StructColumnVector
argument_list|(
name|batchSize
argument_list|,
name|fieldVector
argument_list|)
return|;
block|}
case|case
name|UNION
case|:
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|subtypeIdxs
init|=
name|type
operator|.
name|getSubtypesList
argument_list|()
decl_stmt|;
name|ColumnVector
index|[]
name|fieldVector
init|=
operator|new
name|ColumnVector
index|[
name|subtypeIdxs
operator|.
name|size
argument_list|()
index|]
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
name|fieldVector
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldVector
index|[
name|i
index|]
operator|=
name|createColumn
argument_list|(
name|types
argument_list|,
name|subtypeIdxs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|UnionColumnVector
argument_list|(
name|batchSize
argument_list|,
name|fieldVector
argument_list|)
return|;
block|}
case|case
name|LIST
case|:
return|return
operator|new
name|ListColumnVector
argument_list|(
name|batchSize
argument_list|,
name|createColumn
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|0
argument_list|)
argument_list|,
name|batchSize
argument_list|)
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
operator|new
name|MapColumnVector
argument_list|(
name|batchSize
argument_list|,
name|createColumn
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|0
argument_list|)
argument_list|,
name|batchSize
argument_list|)
argument_list|,
name|createColumn
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|batchSize
argument_list|)
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"LLAP does not support "
operator|+
name|type
operator|.
name|getKind
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|decodeBatch
parameter_list|(
name|OrcEncodedColumnBatch
name|batch
parameter_list|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|downstreamConsumer
parameter_list|)
block|{
name|long
name|startTime
init|=
name|counters
operator|.
name|startTimeCounter
argument_list|()
decl_stmt|;
name|int
name|currentStripeIndex
init|=
name|batch
operator|.
name|getBatchKey
argument_list|()
operator|.
name|stripeIx
decl_stmt|;
name|boolean
name|sameStripe
init|=
name|currentStripeIndex
operator|==
name|previousStripeIndex
decl_stmt|;
try|try
block|{
name|OrcStripeMetadata
name|stripeMetadata
init|=
name|stripes
index|[
name|currentStripeIndex
index|]
decl_stmt|;
comment|// Get non null row count from root column, to get max vector batches
name|int
name|rgIdx
init|=
name|batch
operator|.
name|getBatchKey
argument_list|()
operator|.
name|rgIx
decl_stmt|;
name|long
name|nonNullRowCount
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|rgIdx
operator|==
name|OrcEncodedColumnBatch
operator|.
name|ALL_RGS
condition|)
block|{
name|nonNullRowCount
operator|=
name|stripeMetadata
operator|.
name|getRowCount
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
init|=
name|stripeMetadata
operator|.
name|getRowIndexes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getEntry
argument_list|(
name|rgIdx
argument_list|)
decl_stmt|;
name|nonNullRowCount
operator|=
name|getRowCount
argument_list|(
name|rowIndex
argument_list|)
expr_stmt|;
block|}
name|int
name|maxBatchesRG
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|nonNullRowCount
operator|/
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
operator|)
operator|+
literal|1
argument_list|)
decl_stmt|;
name|int
name|batchSize
init|=
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
decl_stmt|;
name|int
name|numCols
init|=
name|batch
operator|.
name|getColumnIxs
argument_list|()
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|columnReaders
operator|==
literal|null
operator|||
operator|!
name|sameStripe
condition|)
block|{
name|this
operator|.
name|columnReaders
operator|=
name|EncodedTreeReaderFactory
operator|.
name|createEncodedTreeReader
argument_list|(
name|numCols
argument_list|,
name|fileMetadata
operator|.
name|getTypes
argument_list|()
argument_list|,
name|stripeMetadata
operator|.
name|getEncodings
argument_list|()
argument_list|,
name|batch
argument_list|,
name|codec
argument_list|,
name|skipCorrupt
argument_list|)
expr_stmt|;
name|positionInStreams
argument_list|(
name|columnReaders
argument_list|,
name|batch
argument_list|,
name|numCols
argument_list|,
name|stripeMetadata
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|repositionInStreams
argument_list|(
name|this
operator|.
name|columnReaders
argument_list|,
name|batch
argument_list|,
name|sameStripe
argument_list|,
name|numCols
argument_list|,
name|stripeMetadata
argument_list|)
expr_stmt|;
block|}
name|previousStripeIndex
operator|=
name|currentStripeIndex
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
name|maxBatchesRG
condition|;
name|i
operator|++
control|)
block|{
comment|// for last batch in row group, adjust the batch size
if|if
condition|(
name|i
operator|==
name|maxBatchesRG
operator|-
literal|1
condition|)
block|{
name|batchSize
operator|=
call|(
name|int
call|)
argument_list|(
name|nonNullRowCount
operator|%
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
if|if
condition|(
name|batchSize
operator|==
literal|0
condition|)
break|break;
block|}
name|ColumnVectorBatch
name|cvb
init|=
name|cvbPool
operator|.
name|take
argument_list|()
decl_stmt|;
assert|assert
name|cvb
operator|.
name|cols
operator|.
name|length
operator|==
name|batch
operator|.
name|getColumnIxs
argument_list|()
operator|.
name|length
assert|;
comment|// Must be constant per split.
name|cvb
operator|.
name|size
operator|=
name|batchSize
expr_stmt|;
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
init|=
name|fileMetadata
operator|.
name|getTypes
argument_list|()
decl_stmt|;
name|int
index|[]
name|columnMapping
init|=
name|batch
operator|.
name|getColumnIxs
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|batch
operator|.
name|getColumnIxs
argument_list|()
operator|.
name|length
condition|;
name|idx
operator|++
control|)
block|{
if|if
condition|(
name|cvb
operator|.
name|cols
index|[
name|idx
index|]
operator|==
literal|null
condition|)
block|{
comment|// Orc store rows inside a root struct (hive writes it this way).
comment|// When we populate column vectors we skip over the root struct.
name|cvb
operator|.
name|cols
index|[
name|idx
index|]
operator|=
name|createColumn
argument_list|(
name|types
argument_list|,
name|columnMapping
index|[
name|idx
index|]
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|cvb
operator|.
name|cols
index|[
name|idx
index|]
operator|.
name|ensureSize
argument_list|(
name|batchSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|columnReaders
index|[
name|idx
index|]
operator|.
name|nextVector
argument_list|(
name|cvb
operator|.
name|cols
index|[
name|idx
index|]
argument_list|,
literal|null
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
comment|// we are done reading a batch, send it to consumer for processing
name|downstreamConsumer
operator|.
name|consumeData
argument_list|(
name|cvb
argument_list|)
expr_stmt|;
name|counters
operator|.
name|incrCounter
argument_list|(
name|LlapIOCounters
operator|.
name|ROWS_EMITTED
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|counters
operator|.
name|incrTimeCounter
argument_list|(
name|LlapIOCounters
operator|.
name|DECODE_TIME_NS
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
name|counters
operator|.
name|incrCounter
argument_list|(
name|LlapIOCounters
operator|.
name|NUM_VECTOR_BATCHES
argument_list|,
name|maxBatchesRG
argument_list|)
expr_stmt|;
name|counters
operator|.
name|incrCounter
argument_list|(
name|LlapIOCounters
operator|.
name|NUM_DECODED_BATCHES
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Caller will return the batch.
name|downstreamConsumer
operator|.
name|setError
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|positionInStreams
parameter_list|(
name|TreeReaderFactory
operator|.
name|TreeReader
index|[]
name|columnReaders
parameter_list|,
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
name|batch
parameter_list|,
name|int
name|numCols
parameter_list|,
name|OrcStripeMetadata
name|stripeMetadata
parameter_list|)
throws|throws
name|IOException
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|int
name|columnIndex
init|=
name|batch
operator|.
name|getColumnIxs
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|rowGroupIndex
init|=
name|batch
operator|.
name|getBatchKey
argument_list|()
operator|.
name|rgIx
decl_stmt|;
name|OrcProto
operator|.
name|RowIndex
name|rowIndex
init|=
name|stripeMetadata
operator|.
name|getRowIndexes
argument_list|()
index|[
name|columnIndex
index|]
decl_stmt|;
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndexEntry
init|=
name|rowIndex
operator|.
name|getEntry
argument_list|(
name|rowGroupIndex
argument_list|)
decl_stmt|;
name|columnReaders
index|[
name|i
index|]
operator|.
name|seek
argument_list|(
operator|new
name|RecordReaderImpl
operator|.
name|PositionProviderImpl
argument_list|(
name|rowIndexEntry
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|repositionInStreams
parameter_list|(
name|TreeReaderFactory
operator|.
name|TreeReader
index|[]
name|columnReaders
parameter_list|,
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
name|batch
parameter_list|,
name|boolean
name|sameStripe
parameter_list|,
name|int
name|numCols
parameter_list|,
name|OrcStripeMetadata
name|stripeMetadata
parameter_list|)
throws|throws
name|IOException
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|int
name|columnIndex
init|=
name|batch
operator|.
name|getColumnIxs
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|rowGroupIndex
init|=
name|batch
operator|.
name|getBatchKey
argument_list|()
operator|.
name|rgIx
decl_stmt|;
name|ColumnStreamData
index|[]
name|streamBuffers
init|=
name|batch
operator|.
name|getColumnData
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|OrcProto
operator|.
name|RowIndex
name|rowIndex
init|=
name|stripeMetadata
operator|.
name|getRowIndexes
argument_list|()
index|[
name|columnIndex
index|]
decl_stmt|;
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndexEntry
init|=
name|rowIndex
operator|.
name|getEntry
argument_list|(
name|rowGroupIndex
argument_list|)
decl_stmt|;
operator|(
operator|(
name|SettableTreeReader
operator|)
name|columnReaders
index|[
name|i
index|]
operator|)
operator|.
name|setBuffers
argument_list|(
name|streamBuffers
argument_list|,
name|sameStripe
argument_list|)
expr_stmt|;
name|columnReaders
index|[
name|i
index|]
operator|.
name|seek
argument_list|(
operator|new
name|RecordReaderImpl
operator|.
name|PositionProviderImpl
argument_list|(
name|rowIndexEntry
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|getRowCount
parameter_list|(
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndexEntry
parameter_list|)
block|{
return|return
name|rowIndexEntry
operator|.
name|getStatistics
argument_list|()
operator|.
name|getNumberOfValues
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TypeDescription
name|getFileSchema
parameter_list|()
block|{
return|return
name|OrcUtils
operator|.
name|convertTypeFromProtobuf
argument_list|(
name|fileMetadata
operator|.
name|getTypes
argument_list|()
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
index|[]
name|getIncludedColumns
parameter_list|()
block|{
return|return
name|includedColumns
return|;
block|}
specifier|public
name|void
name|setIncludedColumns
parameter_list|(
specifier|final
name|boolean
index|[]
name|includedColumns
parameter_list|)
block|{
name|this
operator|.
name|includedColumns
operator|=
name|includedColumns
expr_stmt|;
block|}
block|}
end_class

end_unit

