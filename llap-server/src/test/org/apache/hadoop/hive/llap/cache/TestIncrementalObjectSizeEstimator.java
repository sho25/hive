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
name|cache
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|LinkedHashSet
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
name|BinaryStatistics
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
name|ColumnEncoding
operator|.
name|Kind
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
name|DateStatistics
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
name|DecimalStatistics
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
name|DoubleStatistics
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
name|IntegerStatistics
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
name|OrcProto
operator|.
name|TimestampStatistics
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
name|RecordReaderImpl
operator|.
name|Index
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
name|util
operator|.
name|JavaDataModel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
import|;
end_import

begin_class
specifier|public
class|class
name|TestIncrementalObjectSizeEstimator
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
name|TestIncrementalObjectSizeEstimator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|DummyMetadataReader
implements|implements
name|MetadataReader
block|{
specifier|public
name|boolean
name|doStreamStep
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|isEmpty
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Index
name|readRowIndex
parameter_list|(
name|StripeInformation
name|stripe
parameter_list|,
name|StripeFooter
name|footer
parameter_list|,
name|boolean
index|[]
name|included
parameter_list|,
name|RowIndex
index|[]
name|indexes
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|,
name|BloomFilterIndex
index|[]
name|bloomFilterIndices
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmpty
condition|)
block|{
return|return
operator|new
name|RecordReaderImpl
operator|.
name|Index
argument_list|(
operator|new
name|RowIndex
index|[]
block|{ }
argument_list|,
operator|new
name|BloomFilterIndex
index|[]
block|{ }
argument_list|)
return|;
block|}
name|ColumnStatistics
name|cs
init|=
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
operator|.
name|setMinimum
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setBinaryStatistics
argument_list|(
name|BinaryStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSum
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|setDateStatistics
argument_list|(
name|DateStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMinimum
argument_list|(
literal|4545
argument_list|)
operator|.
name|setMaximum
argument_list|(
literal|6656
argument_list|)
argument_list|)
operator|.
name|setDecimalStatistics
argument_list|(
name|DecimalStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMaximum
argument_list|(
literal|"zzz"
argument_list|)
operator|.
name|setMinimum
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
operator|.
name|setDoubleStatistics
argument_list|(
name|DoubleStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMinimum
argument_list|(
literal|0.5
argument_list|)
operator|.
name|setMaximum
argument_list|(
literal|1.5
argument_list|)
argument_list|)
operator|.
name|setIntStatistics
argument_list|(
name|IntegerStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMaximum
argument_list|(
literal|10
argument_list|)
operator|.
name|setMinimum
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|setTimestampStatistics
argument_list|(
name|TimestampStatistics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setMaximum
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
argument_list|)
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
literal|0
argument_list|)
operator|.
name|addPositions
argument_list|(
literal|2
argument_list|)
operator|.
name|setStatistics
argument_list|(
name|cs
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RowIndex
name|ri2
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
literal|3
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
operator|.
name|addBitset
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|doStreamStep
condition|)
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|CodedOutputStream
name|cos
init|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|ri
operator|.
name|writeTo
argument_list|(
name|cos
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ri
operator|=
name|RowIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|cos
operator|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|ri2
operator|.
name|writeTo
argument_list|(
name|cos
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ri2
operator|=
name|RowIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|cos
operator|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|bfi
operator|.
name|writeTo
argument_list|(
name|cos
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|bfi
operator|=
name|BloomFilterIndex
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
return|return
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
block|,
name|ri2
block|}
argument_list|,
operator|new
name|BloomFilterIndex
index|[]
block|{
name|bfi
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StripeFooter
name|readStripeFooter
parameter_list|(
name|StripeInformation
name|stripe
parameter_list|)
throws|throws
name|IOException
block|{
name|StripeFooter
operator|.
name|Builder
name|fb
init|=
name|StripeFooter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isEmpty
condition|)
block|{
name|fb
operator|.
name|addStreams
argument_list|(
name|Stream
operator|.
name|newBuilder
argument_list|()
operator|.
name|setColumn
argument_list|(
literal|0
argument_list|)
operator|.
name|setLength
argument_list|(
literal|20
argument_list|)
operator|.
name|setKind
argument_list|(
name|Stream
operator|.
name|Kind
operator|.
name|LENGTH
argument_list|)
argument_list|)
operator|.
name|addStreams
argument_list|(
name|Stream
operator|.
name|newBuilder
argument_list|()
operator|.
name|setColumn
argument_list|(
literal|0
argument_list|)
operator|.
name|setLength
argument_list|(
literal|40
argument_list|)
operator|.
name|setKind
argument_list|(
name|Stream
operator|.
name|Kind
operator|.
name|DATA
argument_list|)
argument_list|)
operator|.
name|addColumns
argument_list|(
name|ColumnEncoding
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDictionarySize
argument_list|(
literal|10
argument_list|)
operator|.
name|setKind
argument_list|(
name|Kind
operator|.
name|DIRECT_V2
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StripeFooter
name|footer
init|=
name|fb
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|doStreamStep
condition|)
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|CodedOutputStream
name|cos
init|=
name|CodedOutputStream
operator|.
name|newInstance
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|footer
operator|.
name|writeTo
argument_list|(
name|cos
argument_list|)
expr_stmt|;
name|cos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|footer
operator|=
name|StripeFooter
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
return|return
name|footer
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetadata
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Mostly tests that it doesn't crash.
name|OrcStripeMetadata
name|osm
init|=
name|OrcStripeMetadata
operator|.
name|createDummy
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|ObjectEstimator
argument_list|>
name|map
init|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|osm
argument_list|)
decl_stmt|;
name|IncrementalObjectSizeEstimator
operator|.
name|addEstimator
argument_list|(
literal|"com.google.protobuf.LiteralByteString"
argument_list|,
name|map
argument_list|)
expr_stmt|;
name|ObjectEstimator
name|root
init|=
name|map
operator|.
name|get
argument_list|(
name|OrcStripeMetadata
operator|.
name|class
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a dummy OSM"
argument_list|)
expr_stmt|;
name|OrcBatchKey
name|stripeKey
init|=
literal|null
decl_stmt|;
name|DummyMetadataReader
name|mr
init|=
operator|new
name|DummyMetadataReader
argument_list|()
decl_stmt|;
name|mr
operator|.
name|doStreamStep
operator|=
literal|false
expr_stmt|;
name|mr
operator|.
name|isEmpty
operator|=
literal|true
expr_stmt|;
name|StripeInformation
name|si
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|StripeInformation
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|si
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
name|osm
operator|=
operator|new
name|OrcStripeMetadata
argument_list|(
name|stripeKey
argument_list|,
name|mr
argument_list|,
name|si
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for an empty OSM"
argument_list|)
expr_stmt|;
name|mr
operator|.
name|doStreamStep
operator|=
literal|true
expr_stmt|;
name|osm
operator|=
operator|new
name|OrcStripeMetadata
argument_list|(
name|stripeKey
argument_list|,
name|mr
argument_list|,
name|si
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for an empty OSM after serde"
argument_list|)
expr_stmt|;
name|mr
operator|.
name|isEmpty
operator|=
literal|false
expr_stmt|;
name|stripeKey
operator|=
operator|new
name|OrcBatchKey
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|osm
operator|=
operator|new
name|OrcStripeMetadata
argument_list|(
name|stripeKey
argument_list|,
name|mr
argument_list|,
name|si
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a test OSM"
argument_list|)
expr_stmt|;
name|osm
operator|.
name|resetRowIndex
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a test OSM w/o row index"
argument_list|)
expr_stmt|;
name|mr
operator|.
name|doStreamStep
operator|=
literal|true
expr_stmt|;
name|osm
operator|=
operator|new
name|OrcStripeMetadata
argument_list|(
name|stripeKey
argument_list|,
name|mr
argument_list|,
name|si
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a test OSM after serde"
argument_list|)
expr_stmt|;
name|osm
operator|.
name|resetRowIndex
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|osm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a test OSM w/o row index after serde"
argument_list|)
expr_stmt|;
name|OrcFileMetadata
name|ofm
init|=
name|OrcFileMetadata
operator|.
name|createDummy
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|map
operator|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|ofm
argument_list|)
expr_stmt|;
name|IncrementalObjectSizeEstimator
operator|.
name|addEstimator
argument_list|(
literal|"com.google.protobuf.LiteralByteString"
argument_list|,
name|map
argument_list|)
expr_stmt|;
name|root
operator|=
name|map
operator|.
name|get
argument_list|(
name|OrcFileMetadata
operator|.
name|class
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|root
operator|.
name|estimate
argument_list|(
name|ofm
argument_list|,
name|map
argument_list|)
operator|+
literal|" for a dummy OFM"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|Struct
block|{
name|Integer
name|i
decl_stmt|;
name|int
name|j
init|=
literal|0
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|Object
argument_list|>
name|list2
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
decl_stmt|;
block|}
specifier|private
specifier|static
class|class
name|Struct2
block|{
name|Struct2
name|next
decl_stmt|;
name|Struct2
name|prev
decl_stmt|;
name|Struct2
name|top
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleTypes
parameter_list|()
block|{
name|JavaDataModel
name|memModel
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|intSize
init|=
name|runEstimate
argument_list|(
operator|new
name|Integer
argument_list|(
literal|0
argument_list|)
argument_list|,
name|memModel
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|runEstimate
argument_list|(
operator|new
name|String
argument_list|(
literal|""
argument_list|)
argument_list|,
name|memModel
argument_list|,
literal|"empty string"
argument_list|)
expr_stmt|;
name|runEstimate
argument_list|(
operator|new
name|String
argument_list|(
literal|"foobarzzzzzzzzzzzzzz"
argument_list|)
argument_list|,
name|memModel
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|runEstimate
argument_list|(
name|list
argument_list|,
name|memModel
argument_list|,
literal|"empty ArrayList"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"zzz"
argument_list|)
argument_list|)
expr_stmt|;
name|runEstimate
argument_list|(
name|list
argument_list|,
name|memModel
argument_list|,
literal|"ArrayList - one string"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|arrayListSize
init|=
name|runEstimate
argument_list|(
name|list
argument_list|,
name|memModel
argument_list|,
literal|"ArrayList - 3 elements"
argument_list|)
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|Object
argument_list|>
name|list2
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|runEstimate
argument_list|(
name|list2
argument_list|,
name|memModel
argument_list|,
literal|"empty LinkedHashSet"
argument_list|)
expr_stmt|;
name|list2
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"zzzz"
argument_list|)
argument_list|)
expr_stmt|;
name|runEstimate
argument_list|(
name|list2
argument_list|,
name|memModel
argument_list|,
literal|"LinkedHashSet - one string"
argument_list|)
expr_stmt|;
name|list2
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|list2
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|lhsSize
init|=
name|runEstimate
argument_list|(
name|list2
argument_list|,
name|memModel
argument_list|,
literal|"LinkedHashSet - 3 elements"
argument_list|)
decl_stmt|;
name|Struct
name|struct
init|=
operator|new
name|Struct
argument_list|()
decl_stmt|;
name|int
name|structSize
init|=
name|runEstimate
argument_list|(
name|struct
argument_list|,
name|memModel
argument_list|,
literal|"Struct - empty"
argument_list|)
decl_stmt|;
name|struct
operator|.
name|i
operator|=
literal|10
expr_stmt|;
name|int
name|structSize2
init|=
name|runEstimate
argument_list|(
name|struct
argument_list|,
name|memModel
argument_list|,
literal|"Struct - one reference"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|intSize
operator|+
name|structSize
argument_list|,
name|structSize2
argument_list|)
expr_stmt|;
name|struct
operator|.
name|list
operator|=
name|list
expr_stmt|;
name|int
name|structSize3
init|=
name|runEstimate
argument_list|(
name|struct
argument_list|,
name|memModel
argument_list|,
literal|"Struct - with ArrayList"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|arrayListSize
operator|+
name|structSize2
argument_list|,
name|structSize3
argument_list|)
expr_stmt|;
name|struct
operator|.
name|list2
operator|=
name|list2
expr_stmt|;
name|int
name|structSize4
init|=
name|runEstimate
argument_list|(
name|struct
argument_list|,
name|memModel
argument_list|,
literal|"Struct - with LinkedHashSet"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|lhsSize
operator|+
name|structSize3
argument_list|,
name|structSize4
argument_list|)
expr_stmt|;
name|Struct2
name|struct2
init|=
operator|new
name|Struct2
argument_list|()
decl_stmt|;
name|int
name|recSize1
init|=
name|runEstimate
argument_list|(
name|struct2
argument_list|,
name|memModel
argument_list|,
literal|"recursive struct - empty"
argument_list|)
decl_stmt|;
name|struct2
operator|.
name|next
operator|=
operator|new
name|Struct2
argument_list|()
expr_stmt|;
name|struct2
operator|.
name|top
operator|=
operator|new
name|Struct2
argument_list|()
expr_stmt|;
name|int
name|recSize2
init|=
name|runEstimate
argument_list|(
name|struct2
argument_list|,
name|memModel
argument_list|,
literal|"recursive struct - no ring"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|recSize1
operator|*
literal|3
argument_list|,
name|recSize2
argument_list|)
expr_stmt|;
name|struct2
operator|.
name|next
operator|.
name|prev
operator|=
name|struct2
expr_stmt|;
name|int
name|recSize3
init|=
name|runEstimate
argument_list|(
name|struct2
argument_list|,
name|memModel
argument_list|,
literal|"recursive struct - ring added"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|recSize2
argument_list|,
name|recSize3
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|runEstimate
parameter_list|(
name|Object
name|obj
parameter_list|,
name|JavaDataModel
name|memModel
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|ObjectEstimator
argument_list|>
name|map
init|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|ObjectEstimator
name|root
init|=
name|map
operator|.
name|get
argument_list|(
name|obj
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|estimate
init|=
name|root
operator|.
name|estimate
argument_list|(
name|obj
argument_list|,
name|map
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated "
operator|+
name|estimate
operator|+
literal|" for "
operator|+
operator|(
name|desc
operator|==
literal|null
condition|?
name|obj
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
else|:
name|desc
operator|)
argument_list|)
expr_stmt|;
return|return
name|estimate
return|;
block|}
block|}
end_class

end_unit

