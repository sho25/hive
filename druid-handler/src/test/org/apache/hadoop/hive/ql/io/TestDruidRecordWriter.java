begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|type
operator|.
name|TypeReference
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
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
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|Firehose
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|InputRow
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|DimensionsSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|InputRowParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|MapInputRowParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|StringDimensionSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|TimeAndDimsParseSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|TimestampSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|java
operator|.
name|util
operator|.
name|common
operator|.
name|granularity
operator|.
name|Granularities
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|query
operator|.
name|aggregation
operator|.
name|AggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|query
operator|.
name|aggregation
operator|.
name|LongSumAggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|query
operator|.
name|aggregation
operator|.
name|hyperloglog
operator|.
name|HyperUniquesAggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|IndexSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|QueryableIndex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|QueryableIndexStorageAdapter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|data
operator|.
name|RoaringBitmapSerdeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|DataSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|RealtimeTuningConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|granularity
operator|.
name|UniformGranularitySpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|DataSegmentPusher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|LocalDataSegmentPuller
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|LocalDataSegmentPusher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|LocalDataSegmentPusherConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|SegmentLoadingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|realtime
operator|.
name|firehose
operator|.
name|IngestSegmentFirehose
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|realtime
operator|.
name|firehose
operator|.
name|WindowedStorageAdapter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|timeline
operator|.
name|DataSegment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidTable
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
name|LocalFileSystem
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
name|conf
operator|.
name|Constants
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
name|druid
operator|.
name|DruidStorageHandler
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
name|druid
operator|.
name|DruidStorageHandlerUtils
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
name|druid
operator|.
name|conf
operator|.
name|DruidConstants
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
name|druid
operator|.
name|io
operator|.
name|DruidRecordWriter
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
name|druid
operator|.
name|serde
operator|.
name|DruidWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Interval
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TemporaryFolder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|List
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * Test Class for Druid Record Writer.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"ConstantConditions"
argument_list|)
specifier|public
class|class
name|TestDruidRecordWriter
block|{
specifier|private
specifier|final
name|ObjectMapper
name|objectMapper
init|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Interval
name|INTERVAL_FULL
init|=
operator|new
name|Interval
argument_list|(
literal|"2014-10-22T00:00:00Z/P1D"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TemporaryFolder
name|temporaryFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedRows
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|ImmutableMap
operator|.
name|of
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-10-22T00:00:00.000Z"
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|,
literal|"host"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"a.example.com"
argument_list|)
argument_list|,
literal|"visited_sum"
argument_list|,
literal|190L
argument_list|,
literal|"unique_hosts"
argument_list|,
literal|1.0d
argument_list|)
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-10-22T01:00:00.000Z"
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|,
literal|"host"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"b.example.com"
argument_list|)
argument_list|,
literal|"visited_sum"
argument_list|,
literal|175L
argument_list|,
literal|"unique_hosts"
argument_list|,
literal|1.0d
argument_list|)
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-10-22T02:00:00.000Z"
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|,
literal|"host"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"c.example.com"
argument_list|)
argument_list|,
literal|"visited_sum"
argument_list|,
literal|270L
argument_list|,
literal|"unique_hosts"
argument_list|,
literal|1.0d
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testTimeStampColumnName
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Time column name need to match to ensure serdeser compatibility"
argument_list|,
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
expr_stmt|;
block|}
comment|//Test is failing due to Guava dependency, Druid 0.13.0 should have less dependency on Guava
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testWrite
parameter_list|()
throws|throws
name|IOException
throws|,
name|SegmentLoadingException
block|{
specifier|final
name|String
name|dataSourceName
init|=
literal|"testDataSource"
decl_stmt|;
specifier|final
name|File
name|segmentOutputDir
init|=
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
decl_stmt|;
specifier|final
name|File
name|workingDir
init|=
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
decl_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|final
name|InputRowParser
name|inputRowParser
init|=
operator|new
name|MapInputRowParser
argument_list|(
operator|new
name|TimeAndDimsParseSpec
argument_list|(
operator|new
name|TimestampSpec
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
literal|"auto"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|DimensionsSpec
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
operator|new
name|StringDimensionSchema
argument_list|(
literal|"host"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|parserMap
init|=
name|objectMapper
operator|.
name|convertValue
argument_list|(
name|inputRowParser
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
block|{         }
argument_list|)
decl_stmt|;
name|DataSchema
name|dataSchema
init|=
operator|new
name|DataSchema
argument_list|(
name|dataSourceName
argument_list|,
name|parserMap
argument_list|,
operator|new
name|AggregatorFactory
index|[]
block|{
operator|new
name|LongSumAggregatorFactory
argument_list|(
literal|"visited_sum"
argument_list|,
literal|"visited_sum"
argument_list|)
block|,
operator|new
name|HyperUniquesAggregatorFactory
argument_list|(
literal|"unique_hosts"
argument_list|,
literal|"unique_hosts"
argument_list|)
block|}
argument_list|,
operator|new
name|UniformGranularitySpec
argument_list|(
name|Granularities
operator|.
name|DAY
argument_list|,
name|Granularities
operator|.
name|NONE
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|INTERVAL_FULL
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|,
name|objectMapper
argument_list|)
decl_stmt|;
name|IndexSpec
name|indexSpec
init|=
operator|new
name|IndexSpec
argument_list|(
operator|new
name|RoaringBitmapSerdeFactory
argument_list|(
literal|true
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RealtimeTuningConfig
name|tuningConfig
init|=
operator|new
name|RealtimeTuningConfig
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indexSpec
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|0L
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LocalFileSystem
name|localFileSystem
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|DataSegmentPusher
name|dataSegmentPusher
init|=
operator|new
name|LocalDataSegmentPusher
argument_list|(
operator|new
name|LocalDataSegmentPusherConfig
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|File
name|getStorageDirectory
parameter_list|()
block|{
return|return
name|segmentOutputDir
return|;
block|}
block|}
argument_list|,
name|objectMapper
argument_list|)
decl_stmt|;
name|Path
name|segmentDescriptorPath
init|=
operator|new
name|Path
argument_list|(
name|workingDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|DruidStorageHandler
operator|.
name|SEGMENTS_DESCRIPTOR_DIR_NAME
argument_list|)
decl_stmt|;
name|DruidRecordWriter
name|druidRecordWriter
init|=
operator|new
name|DruidRecordWriter
argument_list|(
name|dataSchema
argument_list|,
name|tuningConfig
argument_list|,
name|dataSegmentPusher
argument_list|,
literal|20
argument_list|,
name|segmentDescriptorPath
argument_list|,
name|localFileSystem
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DruidWritable
argument_list|>
name|druidWritables
init|=
name|expectedRows
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|input
lambda|->
operator|new
name|DruidWritable
argument_list|(
name|ImmutableMap
operator|.
block|<String
argument_list|,
name|Object
operator|>
name|builder
argument_list|()
operator|.
name|putAll
argument_list|(
name|input
argument_list|)
operator|.
name|put
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|,
name|Granularities
operator|.
name|DAY
operator|.
name|bucketStart
argument_list|(
operator|new
name|DateTime
argument_list|(
operator|(
name|long
operator|)
name|input
operator|.
name|get
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
argument_list|)
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
init|)
decl|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|DruidWritable
name|druidWritable
range|:
name|druidWritables
control|)
block|{
name|druidRecordWriter
operator|.
name|write
argument_list|(
name|druidWritable
argument_list|)
expr_stmt|;
block|}
name|druidRecordWriter
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DataSegment
argument_list|>
name|dataSegmentList
init|=
name|DruidStorageHandlerUtils
operator|.
name|getCreatedSegments
argument_list|(
name|segmentDescriptorPath
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|dataSegmentList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|tmpUnzippedSegmentDir
init|=
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
decl_stmt|;
operator|new
name|LocalDataSegmentPuller
argument_list|()
operator|.
name|getSegmentFiles
argument_list|(
name|dataSegmentList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tmpUnzippedSegmentDir
argument_list|)
expr_stmt|;
specifier|final
name|QueryableIndex
name|queryableIndex
init|=
name|DruidStorageHandlerUtils
operator|.
name|INDEX_IO
operator|.
name|loadIndex
argument_list|(
name|tmpUnzippedSegmentDir
argument_list|)
decl_stmt|;
name|QueryableIndexStorageAdapter
name|adapter
init|=
operator|new
name|QueryableIndexStorageAdapter
argument_list|(
name|queryableIndex
argument_list|)
decl_stmt|;
name|Firehose
name|firehose
init|=
operator|new
name|IngestSegmentFirehose
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
operator|new
name|WindowedStorageAdapter
argument_list|(
name|adapter
argument_list|,
name|adapter
operator|.
name|getInterval
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"visited_sum"
argument_list|,
literal|"unique_hosts"
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputRow
argument_list|>
name|rows
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
while|while
condition|(
name|firehose
operator|.
name|hasMore
argument_list|()
condition|)
block|{
name|rows
operator|.
name|add
argument_list|(
name|firehose
operator|.
name|nextRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|verifyRows
argument_list|(
name|expectedRows
argument_list|,
name|rows
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyRows
parameter_list|(
name|List
argument_list|<
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedRows
parameter_list|,
name|List
argument_list|<
name|InputRow
argument_list|>
name|actualRows
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actualRows = "
operator|+
name|actualRows
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedRows
operator|.
name|size
argument_list|()
argument_list|,
name|actualRows
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
literal|0
init|;
name|i
operator|<
name|expectedRows
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
name|expectedRows
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|InputRow
name|actual
init|=
name|actualRows
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|actual
operator|.
name|getDimensions
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|DruidConstants
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
argument_list|,
name|actual
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|actual
operator|.
name|getDimension
argument_list|(
literal|"host"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
literal|"visited_sum"
argument_list|)
argument_list|,
name|actual
operator|.
name|getMetric
argument_list|(
literal|"visited_sum"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
name|Double
operator|)
name|expected
operator|.
name|get
argument_list|(
literal|"unique_hosts"
argument_list|)
argument_list|,
operator|(
name|Double
operator|)
name|HyperUniquesAggregatorFactory
operator|.
name|estimateCardinality
argument_list|(
name|actual
operator|.
name|getRaw
argument_list|(
literal|"unique_hosts"
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerDesr
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|segment
init|=
literal|"{\"dataSource\":\"datasource2015\",\"interval\":\"2015-06-01T00:00:00.000-04:00/"
operator|+
literal|"2015-06-02T00:00:00.000-04:00\""
operator|+
literal|",\"version\":\"2016-11-04T19:24:01.732-04:00\",\"loadSpec\":{\"type\":\"hdfs\","
operator|+
literal|"\"path\":\"hdfs://cn105-10.l42scl.hortonworks.com:8020/apps/hive/warehouse/druid.db/"
operator|+
literal|".hive-staging_hive_2016-11-04_19-23-50_168_1550339856804207572-1/_task_tmp.-ext-10002/_tmp.000000_0/"
operator|+
literal|"datasource2015/20150601T000000.000-0400_20150602T000000.000-0400/2016-11-04T19_24_01.732-04_00/0/"
operator|+
literal|"index.zip\"},\"dimensions\":\"dimension1\",\"metrics\":\"bigint\",\"shardSpec\":{\"type\":\"linear\","
operator|+
literal|"\"partitionNum\":0},\"binaryVersion\":9,\"size\":1765,\"identifier\":\"datasource2015_2015-06-01"
operator|+
literal|"T00:00:00.000-04:00_2015-06-02T00:00:00.000-04:00_2016-11-04T19:24:01.732-04:00\"}"
decl_stmt|;
name|DataSegment
name|dataSegment
init|=
name|objectMapper
operator|.
name|readerFor
argument_list|(
name|DataSegment
operator|.
name|class
argument_list|)
operator|.
name|readValue
argument_list|(
name|segment
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"datasource2015"
argument_list|,
name|dataSegment
operator|.
name|getDataSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

