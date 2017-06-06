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
name|metadata
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|HashMap
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
name|llap
operator|.
name|IncrementalObjectSizeEstimator
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
name|IncrementalObjectSizeEstimator
operator|.
name|ObjectEstimator
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
name|EvictionDispatcher
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
name|LlapCacheableBuffer
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
name|SyntheticFileId
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
name|orc
operator|.
name|DataReader
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

begin_import
import|import
name|org
operator|.
name|apache
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
name|OrcIndex
import|;
end_import

begin_class
specifier|public
class|class
name|OrcStripeMetadata
extends|extends
name|LlapCacheableBuffer
implements|implements
name|ConsumerStripeMetadata
block|{
specifier|private
specifier|final
name|TypeDescription
name|schema
decl_stmt|;
specifier|private
specifier|final
name|OrcBatchKey
name|stripeKey
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|OrcProto
operator|.
name|ColumnEncoding
argument_list|>
name|encodings
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|OrcProto
operator|.
name|Stream
argument_list|>
name|streams
decl_stmt|;
specifier|private
specifier|final
name|String
name|writerTimezone
decl_stmt|;
specifier|private
specifier|final
name|long
name|rowCount
decl_stmt|;
specifier|private
name|OrcIndex
name|rowIndex
decl_stmt|;
specifier|private
name|OrcFile
operator|.
name|WriterVersion
name|writerVersion
decl_stmt|;
specifier|private
specifier|final
name|int
name|estimatedMemUsage
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|ObjectEstimator
argument_list|>
name|SIZE_ESTIMATORS
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|ObjectEstimator
name|SIZE_ESTIMATOR
decl_stmt|;
static|static
block|{
name|OrcStripeMetadata
name|osm
init|=
name|createDummy
argument_list|(
operator|new
name|SyntheticFileId
argument_list|()
argument_list|)
decl_stmt|;
name|SIZE_ESTIMATORS
operator|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|osm
argument_list|)
expr_stmt|;
name|IncrementalObjectSizeEstimator
operator|.
name|addEstimator
argument_list|(
literal|"com.google.protobuf.LiteralByteString"
argument_list|,
name|SIZE_ESTIMATORS
argument_list|)
expr_stmt|;
comment|// Add long for the regular file ID estimation.
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|Long
operator|.
name|class
argument_list|,
name|SIZE_ESTIMATORS
argument_list|)
expr_stmt|;
name|SIZE_ESTIMATOR
operator|=
name|SIZE_ESTIMATORS
operator|.
name|get
argument_list|(
name|OrcStripeMetadata
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OrcStripeMetadata
parameter_list|(
name|OrcBatchKey
name|stripeKey
parameter_list|,
name|DataReader
name|mr
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|,
name|TypeDescription
name|schema
parameter_list|,
name|OrcFile
operator|.
name|WriterVersion
name|writerVersion
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|stripeKey
operator|=
name|stripeKey
expr_stmt|;
name|OrcProto
operator|.
name|StripeFooter
name|footer
init|=
name|mr
operator|.
name|readStripeFooter
argument_list|(
name|stripe
argument_list|)
decl_stmt|;
name|streams
operator|=
name|footer
operator|.
name|getStreamsList
argument_list|()
expr_stmt|;
name|encodings
operator|=
name|footer
operator|.
name|getColumnsList
argument_list|()
expr_stmt|;
name|writerTimezone
operator|=
name|footer
operator|.
name|getWriterTimezone
argument_list|()
expr_stmt|;
name|rowCount
operator|=
name|stripe
operator|.
name|getNumberOfRows
argument_list|()
expr_stmt|;
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
name|schema
argument_list|,
name|footer
argument_list|,
literal|true
argument_list|,
name|includes
argument_list|,
literal|null
argument_list|,
name|sargColumns
argument_list|,
name|writerVersion
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|estimatedMemUsage
operator|=
name|SIZE_ESTIMATOR
operator|.
name|estimate
argument_list|(
name|this
argument_list|,
name|SIZE_ESTIMATORS
argument_list|)
expr_stmt|;
name|this
operator|.
name|writerVersion
operator|=
name|writerVersion
expr_stmt|;
block|}
specifier|private
name|OrcStripeMetadata
parameter_list|(
name|Object
name|id
parameter_list|)
block|{
name|stripeKey
operator|=
operator|new
name|OrcBatchKey
argument_list|(
name|id
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|encodings
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|streams
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|writerTimezone
operator|=
literal|""
expr_stmt|;
name|schema
operator|=
name|TypeDescription
operator|.
name|fromString
argument_list|(
literal|"struct<x:int>"
argument_list|)
expr_stmt|;
name|rowCount
operator|=
name|estimatedMemUsage
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|OrcStripeMetadata
name|createDummy
parameter_list|(
name|Object
name|id
parameter_list|)
block|{
name|OrcStripeMetadata
name|dummy
init|=
operator|new
name|OrcStripeMetadata
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|dummy
operator|.
name|encodings
operator|.
name|add
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|dummy
operator|.
name|streams
operator|.
name|add
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|RowIndex
name|ri
init|=
name|OrcProto
operator|.
name|RowIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|addEntry
argument_list|(
name|OrcProto
operator|.
name|RowIndexEntry
operator|.
name|newBuilder
argument_list|()
operator|.
name|addPositions
argument_list|(
literal|1
argument_list|)
operator|.
name|setStatistics
argument_list|(
name|OrcFileMetadata
operator|.
name|createStatsDummy
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|BloomFilterIndex
name|bfi
init|=
name|OrcProto
operator|.
name|BloomFilterIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|addBloomFilter
argument_list|(
name|OrcProto
operator|.
name|BloomFilter
operator|.
name|newBuilder
argument_list|()
operator|.
name|addBitset
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|dummy
operator|.
name|rowIndex
operator|=
operator|new
name|OrcIndex
argument_list|(
operator|new
name|OrcProto
operator|.
name|RowIndex
index|[]
block|{
name|ri
block|}
argument_list|,
operator|new
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
index|[]
block|{
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|BLOOM_FILTER_UTF8
block|}
argument_list|,
operator|new
name|OrcProto
operator|.
name|BloomFilterIndex
index|[]
block|{
name|bfi
block|}
argument_list|)
expr_stmt|;
return|return
name|dummy
return|;
block|}
specifier|public
name|boolean
name|hasAllIndexes
parameter_list|(
name|boolean
index|[]
name|includes
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
name|includes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|includes
index|[
name|i
index|]
operator|&&
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
index|[
name|i
index|]
operator|==
literal|null
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|loadMissingIndexes
parameter_list|(
name|DataReader
name|mr
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Do not lose the old indexes. Create a super set includes
name|OrcProto
operator|.
name|RowIndex
index|[]
name|existing
init|=
name|getRowIndexes
argument_list|()
decl_stmt|;
name|boolean
name|superset
index|[]
init|=
operator|new
name|boolean
index|[
name|Math
operator|.
name|max
argument_list|(
name|existing
operator|.
name|length
argument_list|,
name|includes
operator|.
name|length
argument_list|)
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
name|includes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|superset
index|[
name|i
index|]
operator|=
name|includes
index|[
name|i
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|existing
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|superset
index|[
name|i
index|]
operator|=
name|superset
index|[
name|i
index|]
operator|||
operator|(
name|existing
index|[
name|i
index|]
operator|!=
literal|null
operator|)
expr_stmt|;
block|}
comment|// TODO: should we save footer to avoid a read here?
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
name|schema
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
name|superset
argument_list|,
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
argument_list|,
name|sargColumns
argument_list|,
name|writerVersion
argument_list|,
name|rowIndex
operator|.
name|getBloomFilterKinds
argument_list|()
argument_list|,
name|rowIndex
operator|.
name|getBloomFilterIndex
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: theoretically, we should re-estimate memory usage here and update memory manager
block|}
specifier|public
name|int
name|getStripeIx
parameter_list|()
block|{
return|return
name|stripeKey
operator|.
name|stripeIx
return|;
block|}
specifier|public
name|OrcProto
operator|.
name|RowIndex
index|[]
name|getRowIndexes
parameter_list|()
block|{
return|return
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
return|;
block|}
specifier|public
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
index|[]
name|getBloomFilterKinds
parameter_list|()
block|{
return|return
name|rowIndex
operator|.
name|getBloomFilterKinds
argument_list|()
return|;
block|}
specifier|public
name|OrcProto
operator|.
name|BloomFilterIndex
index|[]
name|getBloomFilterIndexes
parameter_list|()
block|{
return|return
name|rowIndex
operator|.
name|getBloomFilterIndex
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|OrcProto
operator|.
name|ColumnEncoding
argument_list|>
name|getEncodings
parameter_list|()
block|{
return|return
name|encodings
return|;
block|}
specifier|public
name|List
argument_list|<
name|OrcProto
operator|.
name|Stream
argument_list|>
name|getStreams
parameter_list|()
block|{
return|return
name|streams
return|;
block|}
specifier|public
name|String
name|getWriterTimezone
parameter_list|()
block|{
return|return
name|writerTimezone
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemoryUsage
parameter_list|()
block|{
return|return
name|estimatedMemUsage
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|EvictionDispatcher
name|evictionDispatcher
parameter_list|)
block|{
name|evictionDispatcher
operator|.
name|notifyEvicted
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|invalidate
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|OrcBatchKey
name|getKey
parameter_list|()
block|{
return|return
name|stripeKey
return|;
block|}
specifier|public
name|long
name|getRowCount
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|resetRowIndex
parameter_list|()
block|{
name|rowIndex
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RowIndexEntry
name|getRowIndexEntry
parameter_list|(
name|int
name|colIx
parameter_list|,
name|int
name|rgIx
parameter_list|)
block|{
return|return
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
index|[
name|colIx
index|]
operator|.
name|getEntry
argument_list|(
name|rgIx
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsRowIndexes
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

