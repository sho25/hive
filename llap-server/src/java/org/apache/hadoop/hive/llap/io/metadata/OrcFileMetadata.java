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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|CompressionKind
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
name|FileMetadata
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
name|BucketStatistics
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
name|ColumnStatistics
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
name|StringStatistics
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
name|StripeStatistics
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
name|ReaderImpl
operator|.
name|StripeInformationImpl
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

begin_comment
comment|/** ORC file metadata. Currently contains some duplicate info due to how different parts  * of ORC use different info. Ideally we would get rid of protobuf structs in code beyond reading,  * or instead use protobuf structs everywhere instead of the mix of things like now.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|OrcFileMetadata
extends|extends
name|LlapCacheableBuffer
implements|implements
name|FileMetadata
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|stripes
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|versionList
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|StripeStatistics
argument_list|>
name|stripeStats
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Type
argument_list|>
name|types
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|fileStats
decl_stmt|;
specifier|private
specifier|final
name|long
name|fileId
decl_stmt|;
specifier|private
specifier|final
name|CompressionKind
name|compressionKind
decl_stmt|;
specifier|private
specifier|final
name|int
name|rowIndexStride
decl_stmt|;
specifier|private
specifier|final
name|int
name|compressionBufferSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|metadataSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|writerVersionNum
decl_stmt|;
specifier|private
specifier|final
name|long
name|contentLength
decl_stmt|;
specifier|private
specifier|final
name|long
name|numberOfRows
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isOriginalFormat
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
name|OrcFileMetadata
name|ofm
init|=
name|createDummy
argument_list|()
decl_stmt|;
name|SIZE_ESTIMATORS
operator|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimator
argument_list|(
name|ofm
argument_list|)
expr_stmt|;
name|addLbsEstimator
argument_list|(
name|SIZE_ESTIMATORS
argument_list|)
expr_stmt|;
name|SIZE_ESTIMATOR
operator|=
name|SIZE_ESTIMATORS
operator|.
name|get
argument_list|(
name|OrcFileMetadata
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|addLbsEstimator
parameter_list|(
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|ObjectEstimator
argument_list|>
name|sizeEstimators
parameter_list|)
block|{
comment|// Create estimator for LiteralByteString for the thing to work.
name|Class
argument_list|<
name|?
argument_list|>
name|lbsClass
init|=
literal|null
decl_stmt|;
try|try
block|{
name|lbsClass
operator|=
name|Class
operator|.
name|forName
argument_list|(
literal|"com.google.protobuf.LiteralByteString"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// Ignore and hope for the best.
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot find LiteralByteString"
argument_list|)
expr_stmt|;
block|}
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimator
argument_list|(
name|lbsClass
argument_list|,
name|sizeEstimators
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|OrcFileMetadata
name|createDummy
parameter_list|()
block|{
name|OrcFileMetadata
name|ofm
init|=
operator|new
name|OrcFileMetadata
argument_list|()
decl_stmt|;
name|ofm
operator|.
name|stripes
operator|.
name|add
argument_list|(
operator|new
name|StripeInformationImpl
argument_list|(
name|OrcProto
operator|.
name|StripeInformation
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ofm
operator|.
name|fileStats
operator|.
name|add
argument_list|(
name|ColumnStatistics
operator|.
name|getDefaultInstance
argument_list|()
argument_list|)
expr_stmt|;
name|ofm
operator|.
name|stripeStats
operator|.
name|add
argument_list|(
name|StripeStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|addColStats
argument_list|(
name|createStatsDummy
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ofm
operator|.
name|types
operator|.
name|add
argument_list|(
name|Type
operator|.
name|newBuilder
argument_list|()
operator|.
name|addFieldNames
argument_list|(
literal|"a"
argument_list|)
operator|.
name|addSubtypes
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ofm
operator|.
name|versionList
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|ofm
return|;
block|}
specifier|static
name|ColumnStatistics
operator|.
name|Builder
name|createStatsDummy
parameter_list|()
block|{
return|return
name|ColumnStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setBucketStatistics
argument_list|(
name|BucketStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|addCount
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|setStringStatistics
argument_list|(
name|StringStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMaximum
argument_list|(
literal|"zzz"
argument_list|)
argument_list|)
return|;
block|}
comment|// Ctor for memory estimation
specifier|private
name|OrcFileMetadata
parameter_list|()
block|{
name|stripes
operator|=
operator|new
name|ArrayList
argument_list|<
name|StripeInformation
argument_list|>
argument_list|()
expr_stmt|;
name|versionList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|fileStats
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|stripeStats
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|types
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|fileId
operator|=
name|writerVersionNum
operator|=
name|metadataSize
operator|=
name|compressionBufferSize
operator|=
name|rowIndexStride
operator|=
literal|0
expr_stmt|;
name|contentLength
operator|=
name|numberOfRows
operator|=
literal|0
expr_stmt|;
name|estimatedMemUsage
operator|=
literal|0
expr_stmt|;
name|isOriginalFormat
operator|=
literal|false
expr_stmt|;
name|compressionKind
operator|=
name|CompressionKind
operator|.
name|NONE
expr_stmt|;
block|}
specifier|public
name|OrcFileMetadata
parameter_list|(
name|long
name|fileId
parameter_list|,
name|Reader
name|reader
parameter_list|)
block|{
name|this
operator|.
name|fileId
operator|=
name|fileId
expr_stmt|;
name|this
operator|.
name|stripeStats
operator|=
name|reader
operator|.
name|getOrcProtoStripeStatistics
argument_list|()
expr_stmt|;
name|this
operator|.
name|compressionKind
operator|=
name|reader
operator|.
name|getCompression
argument_list|()
expr_stmt|;
name|this
operator|.
name|compressionBufferSize
operator|=
name|reader
operator|.
name|getCompressionSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|stripes
operator|=
name|reader
operator|.
name|getStripes
argument_list|()
expr_stmt|;
name|this
operator|.
name|isOriginalFormat
operator|=
name|OrcInputFormat
operator|.
name|isOriginal
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|this
operator|.
name|writerVersionNum
operator|=
name|reader
operator|.
name|getWriterVersion
argument_list|()
operator|.
name|getId
argument_list|()
expr_stmt|;
name|this
operator|.
name|versionList
operator|=
name|reader
operator|.
name|getVersionList
argument_list|()
expr_stmt|;
name|this
operator|.
name|metadataSize
operator|=
name|reader
operator|.
name|getMetadataSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|reader
operator|.
name|getTypes
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowIndexStride
operator|=
name|reader
operator|.
name|getRowIndexStride
argument_list|()
expr_stmt|;
name|this
operator|.
name|contentLength
operator|=
name|reader
operator|.
name|getContentLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|numberOfRows
operator|=
name|reader
operator|.
name|getNumberOfRows
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileStats
operator|=
name|reader
operator|.
name|getOrcProtoFileStatistics
argument_list|()
expr_stmt|;
name|this
operator|.
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
comment|// LlapCacheableBuffer
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
comment|// relies on GC, so it can always be evicted now.
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
specifier|protected
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|// FileMetadata
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|getTypes
parameter_list|()
block|{
return|return
name|types
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOriginalFormat
parameter_list|()
block|{
return|return
name|isOriginalFormat
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|getStripes
parameter_list|()
block|{
return|return
name|stripes
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompressionKind
name|getCompressionKind
parameter_list|()
block|{
return|return
name|compressionKind
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCompressionBufferSize
parameter_list|()
block|{
return|return
name|compressionBufferSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowIndexStride
parameter_list|()
block|{
return|return
name|rowIndexStride
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getColumnCount
parameter_list|()
block|{
return|return
name|types
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFlattenedColumnCount
parameter_list|()
block|{
return|return
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSubtypesCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFileId
parameter_list|()
block|{
return|return
name|fileId
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getVersionList
parameter_list|()
block|{
return|return
name|versionList
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMetadataSize
parameter_list|()
block|{
return|return
name|metadataSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getWriterVersionNum
parameter_list|()
block|{
return|return
name|writerVersionNum
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|StripeStatistics
argument_list|>
name|getStripeStats
parameter_list|()
block|{
return|return
name|stripeStats
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getContentLength
parameter_list|()
block|{
return|return
name|contentLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumberOfRows
parameter_list|()
block|{
return|return
name|numberOfRows
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|getFileStats
parameter_list|()
block|{
return|return
name|fileStats
return|;
block|}
block|}
end_class

end_unit

