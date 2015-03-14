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
name|MetadataReader
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
name|BloomFilter
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
name|BloomFilterIndex
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
name|OrcProto
operator|.
name|StripeFooter
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
name|StripeInformation
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
specifier|public
class|class
name|OrcStripeMetadata
extends|extends
name|LlapCacheableBuffer
block|{
specifier|private
specifier|final
name|OrcBatchKey
name|stripeKey
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Stream
argument_list|>
name|streams
decl_stmt|;
specifier|private
name|RecordReaderImpl
operator|.
name|Index
name|rowIndex
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
argument_list|()
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
name|MetadataReader
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
name|this
operator|.
name|stripeKey
operator|=
name|stripeKey
expr_stmt|;
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
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
name|footer
argument_list|,
name|includes
argument_list|,
literal|null
argument_list|,
name|sargColumns
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
block|}
specifier|public
name|OrcStripeMetadata
parameter_list|()
block|{
name|stripeKey
operator|=
literal|null
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
parameter_list|()
block|{
name|OrcStripeMetadata
name|dummy
init|=
operator|new
name|OrcStripeMetadata
argument_list|()
decl_stmt|;
name|dummy
operator|.
name|encodings
operator|.
name|add
argument_list|(
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
name|Stream
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|RowIndex
name|ri
init|=
name|RowIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|addEntry
argument_list|(
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
name|BloomFilterIndex
name|bfi
init|=
name|BloomFilterIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|addBloomFilter
argument_list|(
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
name|RecordReaderImpl
operator|.
name|Index
argument_list|(
operator|new
name|RowIndex
index|[]
block|{
name|ri
block|}
argument_list|,
operator|new
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
name|MetadataReader
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
comment|// TODO: should we save footer to avoid a read here?
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
literal|null
argument_list|,
name|includes
argument_list|,
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
argument_list|,
name|sargColumns
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
name|List
argument_list|<
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
name|Stream
argument_list|>
name|getStreams
parameter_list|()
block|{
return|return
name|streams
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
block|}
end_class

end_unit

