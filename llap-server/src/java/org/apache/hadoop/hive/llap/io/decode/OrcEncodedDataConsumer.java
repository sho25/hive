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
name|LlapDaemonQueueMetrics
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
name|VectorizedRowBatch
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
name|OrcProto
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

begin_class
specifier|public
class|class
name|OrcEncodedDataConsumer
extends|extends
name|EncodedDataConsumer
argument_list|<
name|OrcBatchKey
argument_list|>
block|{
specifier|private
name|EncodedTreeReaderFactory
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
name|LlapDaemonQueueMetrics
name|queueMetrics
parameter_list|)
block|{
name|super
argument_list|(
name|consumer
argument_list|,
name|colCount
argument_list|,
name|queueMetrics
argument_list|)
expr_stmt|;
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
comment|// TODO: get rid of this
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
annotation|@
name|Override
specifier|protected
name|void
name|decodeBatch
parameter_list|(
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
name|batch
parameter_list|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|downstreamConsumer
parameter_list|)
block|{
name|int
name|currentStripeIndex
init|=
name|batch
operator|.
name|batchKey
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
name|batchKey
operator|.
name|rgIx
decl_stmt|;
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
name|long
name|nonNullRowCount
init|=
name|getRowCount
argument_list|(
name|rowIndex
argument_list|)
decl_stmt|;
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
name|columnIxs
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
operator|new
name|ColumnVectorBatch
argument_list|(
name|batch
operator|.
name|columnIxs
operator|.
name|length
argument_list|)
decl_stmt|;
name|cvb
operator|.
name|size
operator|=
name|batchSize
expr_stmt|;
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
name|columnIxs
operator|.
name|length
condition|;
name|idx
operator|++
control|)
block|{
name|cvb
operator|.
name|cols
index|[
name|idx
index|]
operator|=
operator|(
name|ColumnVector
operator|)
name|columnReaders
index|[
name|idx
index|]
operator|.
name|nextVector
argument_list|(
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
name|QueryFragmentCounters
operator|.
name|Counter
operator|.
name|ROWS_EMITTED
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|counters
operator|.
name|incrCounter
argument_list|(
name|QueryFragmentCounters
operator|.
name|Counter
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
name|QueryFragmentCounters
operator|.
name|Counter
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
name|EncodedTreeReaderFactory
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
name|columnIxs
index|[
name|i
index|]
decl_stmt|;
name|int
name|rowGroupIndex
init|=
name|batch
operator|.
name|batchKey
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
name|EncodedTreeReaderFactory
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
name|columnIxs
index|[
name|i
index|]
decl_stmt|;
name|int
name|rowGroupIndex
init|=
name|batch
operator|.
name|batchKey
operator|.
name|rgIx
decl_stmt|;
name|EncodedColumnBatch
operator|.
name|StreamBuffer
index|[]
name|streamBuffers
init|=
name|batch
operator|.
name|columnData
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
name|columnReaders
index|[
name|i
index|]
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
block|}
end_class

end_unit

