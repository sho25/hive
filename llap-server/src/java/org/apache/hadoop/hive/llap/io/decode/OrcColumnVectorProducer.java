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
name|concurrent
operator|.
name|ExecutorService
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|BinaryStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|BooleanStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|ByteStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|CharacterStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|DateStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|DecimalStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|DoubleStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|FloatStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|IntStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|LongStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|ShortStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|StringStreamReader
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
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
operator|.
name|TimestampStreamReader
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
name|encoded
operator|.
name|EncodedDataProducer
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
name|encoded
operator|.
name|OrcEncodedDataProducer
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

begin_class
specifier|public
class|class
name|OrcColumnVectorProducer
extends|extends
name|ColumnVectorProducer
argument_list|<
name|OrcBatchKey
argument_list|>
block|{
specifier|private
specifier|final
name|OrcEncodedDataProducer
name|edp
decl_stmt|;
specifier|private
specifier|final
name|OrcMetadataCache
name|metadataCache
decl_stmt|;
specifier|private
name|boolean
name|skipCorrupt
decl_stmt|;
specifier|public
name|OrcColumnVectorProducer
parameter_list|(
name|ExecutorService
name|executor
parameter_list|,
name|OrcEncodedDataProducer
name|edp
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|executor
argument_list|)
expr_stmt|;
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOGL
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
literal|"Initializing ORC column vector producer"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|edp
operator|=
name|edp
expr_stmt|;
name|this
operator|.
name|metadataCache
operator|=
name|OrcMetadataCache
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|this
operator|.
name|skipCorrupt
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
name|HIVE_ORC_SKIP_CORRUPT_DATA
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|EncodedDataProducer
argument_list|<
name|OrcBatchKey
argument_list|>
name|getEncodedDataProducer
parameter_list|()
block|{
return|return
name|edp
return|;
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
name|String
name|fileName
init|=
name|batch
operator|.
name|batchKey
operator|.
name|file
decl_stmt|;
comment|// OrcEncodedDataProducer should have just loaded cache entries from this file.
comment|// The default LRU algorithm shouldn't have dropped the entries. To make it
comment|// safe, untie the code from EDP into separate class and make use of loading cache. The current
comment|// assumption is that entries for the current file exists in metadata cache.
try|try
block|{
name|OrcFileMetadata
name|fileMetadata
init|=
name|metadataCache
operator|.
name|getFileMetadata
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|OrcBatchKey
name|stripeKey
init|=
name|batch
operator|.
name|batchKey
operator|.
name|clone
argument_list|()
decl_stmt|;
comment|// To get stripe metadata we only need to know the stripe number. Oddly, stripe metadata
comment|// accepts BatchKey as key. We need to keep to row group index in batch key the same to
comment|// retrieve the stripe metadata properly. To make sure we get the correct stripe
comment|// metadata, set row group index to 0. That's how it is cached. See OrcEncodedDataProducer
name|stripeKey
operator|.
name|rgIx
operator|=
literal|0
expr_stmt|;
name|OrcStripeMetadata
name|stripeMetadata
init|=
name|metadataCache
operator|.
name|getStripeMetadata
argument_list|(
name|stripeKey
argument_list|)
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
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|columnReaders
init|=
name|createTreeReaders
argument_list|(
name|numCols
argument_list|,
name|batch
argument_list|,
name|fileMetadata
argument_list|,
name|stripeMetadata
argument_list|)
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
name|maxBatchesRG
condition|;
name|i
operator|++
control|)
block|{
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
name|cvb
operator|.
name|size
operator|=
name|batchSize
expr_stmt|;
block|}
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
block|}
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
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
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|createTreeReaders
parameter_list|(
name|int
name|numCols
parameter_list|,
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
name|batch
parameter_list|,
name|OrcFileMetadata
name|fileMetadata
parameter_list|,
name|OrcStripeMetadata
name|stripeMetadata
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|file
init|=
name|batch
operator|.
name|batchKey
operator|.
name|file
decl_stmt|;
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|treeReaders
init|=
operator|new
name|RecordReaderImpl
operator|.
name|TreeReader
index|[
name|numCols
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
name|Type
name|columnType
init|=
name|fileMetadata
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|columnIndex
argument_list|)
decl_stmt|;
comment|// EncodedColumnBatch is already decompressed, we don't really need to pass codec.
comment|// But we need to know if the original data is compressed or not. This is used to skip
comment|// positions in row index properly. If the file is originally compressed,
comment|// then 1st position (compressed offset) in row index should be skipped to get
comment|// uncompressed offset, else 1st position should not be skipped.
name|CompressionCodec
name|codec
init|=
name|fileMetadata
operator|.
name|getCompressionCodec
argument_list|()
decl_stmt|;
name|int
name|bufferSize
init|=
name|fileMetadata
operator|.
name|getCompressionBufferSize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|ColumnEncoding
name|columnEncoding
init|=
name|stripeMetadata
operator|.
name|getEncodings
argument_list|()
operator|.
name|get
argument_list|(
name|columnIndex
argument_list|)
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
comment|// stream buffers are arranged in enum order of stream kind
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|present
init|=
literal|null
decl_stmt|;
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|data
init|=
literal|null
decl_stmt|;
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dictionary
init|=
literal|null
decl_stmt|;
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|lengths
init|=
literal|null
decl_stmt|;
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|secondary
init|=
literal|null
decl_stmt|;
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|streamBuffer
range|:
name|streamBuffers
control|)
block|{
switch|switch
condition|(
name|streamBuffer
operator|.
name|streamKind
condition|)
block|{
case|case
literal|0
case|:
comment|// PRESENT stream
name|present
operator|=
name|streamBuffer
expr_stmt|;
break|break;
case|case
literal|1
case|:
comment|// DATA stream
name|data
operator|=
name|streamBuffer
expr_stmt|;
break|break;
case|case
literal|2
case|:
comment|// LENGTH stream
name|lengths
operator|=
name|streamBuffer
expr_stmt|;
break|break;
case|case
literal|3
case|:
comment|// DICTIONARY_DATA stream
name|dictionary
operator|=
name|streamBuffer
expr_stmt|;
break|break;
case|case
literal|5
case|:
comment|// SECONDARY stream
name|secondary
operator|=
name|streamBuffer
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected stream kind: "
operator|+
name|streamBuffer
operator|.
name|streamKind
argument_list|)
throw|;
block|}
block|}
switch|switch
condition|(
name|columnType
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|BINARY
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|BinaryStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setLengthStream
argument_list|(
name|lengths
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|BooleanStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|ByteStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|ShortStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|IntStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|LongStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|skipCorrupt
argument_list|(
name|skipCorrupt
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|FloatStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|DoubleStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|CharacterStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setMaxLength
argument_list|(
name|columnType
operator|.
name|getMaximumLength
argument_list|()
argument_list|)
operator|.
name|setCharacterType
argument_list|(
name|columnType
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setLengthStream
argument_list|(
name|lengths
argument_list|)
operator|.
name|setDictionaryStream
argument_list|(
name|dictionary
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|StringStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setLengthStream
argument_list|(
name|lengths
argument_list|)
operator|.
name|setDictionaryStream
argument_list|(
name|dictionary
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|DecimalStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPrecision
argument_list|(
name|columnType
operator|.
name|getPrecision
argument_list|()
argument_list|)
operator|.
name|setScale
argument_list|(
name|columnType
operator|.
name|getScale
argument_list|()
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setValueStream
argument_list|(
name|data
argument_list|)
operator|.
name|setScaleStream
argument_list|(
name|secondary
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|TimestampStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setSecondsStream
argument_list|(
name|data
argument_list|)
operator|.
name|setNanosStream
argument_list|(
name|secondary
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|skipCorrupt
argument_list|(
name|skipCorrupt
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|treeReaders
index|[
name|i
index|]
operator|=
name|DateStreamReader
operator|.
name|builder
argument_list|()
operator|.
name|setFileName
argument_list|(
name|file
argument_list|)
operator|.
name|setColumnIndex
argument_list|(
name|columnIndex
argument_list|)
operator|.
name|setPresentStream
argument_list|(
name|present
argument_list|)
operator|.
name|setDataStream
argument_list|(
name|data
argument_list|)
operator|.
name|setCompressionCodec
argument_list|(
name|codec
argument_list|)
operator|.
name|setBufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|setRowIndex
argument_list|(
name|rowIndexEntry
argument_list|)
operator|.
name|setColumnEncoding
argument_list|(
name|columnEncoding
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Data type not supported yet! "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
return|return
name|treeReaders
return|;
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

