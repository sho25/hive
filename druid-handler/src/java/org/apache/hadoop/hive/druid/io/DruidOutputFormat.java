begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|druid
operator|.
name|io
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
name|base
operator|.
name|Preconditions
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
name|base
operator|.
name|Strings
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
name|Lists
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|DimensionSchema
import|;
end_import

begin_import
import|import
name|io
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
name|io
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
name|io
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
name|io
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
name|io
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
name|io
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
name|io
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
name|Granularity
import|;
end_import

begin_import
import|import
name|io
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
name|io
operator|.
name|druid
operator|.
name|query
operator|.
name|aggregation
operator|.
name|DoubleSumAggregatorFactory
import|;
end_import

begin_import
import|import
name|io
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
name|io
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
name|io
operator|.
name|druid
operator|.
name|segment
operator|.
name|data
operator|.
name|ConciseBitmapSerdeFactory
import|;
end_import

begin_import
import|import
name|io
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
name|io
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
name|io
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
name|io
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|granularity
operator|.
name|GranularitySpec
import|;
end_import

begin_import
import|import
name|io
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
name|io
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
name|io
operator|.
name|druid
operator|.
name|segment
operator|.
name|realtime
operator|.
name|plumber
operator|.
name|CustomVersioningPolicy
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|storage
operator|.
name|hdfs
operator|.
name|HdfsDataSegmentPusher
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|storage
operator|.
name|hdfs
operator|.
name|HdfsDataSegmentPusherConfig
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|serde
operator|.
name|DruidWritable
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
name|FileSinkOperator
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
name|HiveOutputFormat
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|io
operator|.
name|Writable
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
name|JobConf
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
name|RecordWriter
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
name|util
operator|.
name|Progressable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|ArrayList
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
name|Properties
import|;
end_import

begin_import
import|import static
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
operator|.
name|SEGMENTS_DESCRIPTOR_DIR_NAME
import|;
end_import

begin_class
specifier|public
class|class
name|DruidOutputFormat
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|HiveOutputFormat
argument_list|<
name|K
argument_list|,
name|DruidWritable
argument_list|>
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DruidOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|FileSinkOperator
operator|.
name|RecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|Path
name|finalOutPath
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|segmentGranularity
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_GRANULARITY
argument_list|)
operator|!=
literal|null
condition|?
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_GRANULARITY
argument_list|)
else|:
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_INDEXING_GRANULARITY
argument_list|)
decl_stmt|;
specifier|final
name|String
name|dataSource
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|)
decl_stmt|;
specifier|final
name|String
name|segmentDirectory
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_DIRECTORY
argument_list|)
operator|!=
literal|null
condition|?
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_DIRECTORY
argument_list|)
else|:
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DRUID_SEGMENT_DIRECTORY
argument_list|)
decl_stmt|;
specifier|final
name|HdfsDataSegmentPusherConfig
name|hdfsDataSegmentPusherConfig
init|=
operator|new
name|HdfsDataSegmentPusherConfig
argument_list|()
decl_stmt|;
name|hdfsDataSegmentPusherConfig
operator|.
name|setStorageDirectory
argument_list|(
name|segmentDirectory
argument_list|)
expr_stmt|;
specifier|final
name|DataSegmentPusher
name|hdfsDataSegmentPusher
init|=
operator|new
name|HdfsDataSegmentPusher
argument_list|(
name|hdfsDataSegmentPusherConfig
argument_list|,
name|jc
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
argument_list|)
decl_stmt|;
specifier|final
name|GranularitySpec
name|granularitySpec
init|=
operator|new
name|UniformGranularitySpec
argument_list|(
name|Granularity
operator|.
name|fromString
argument_list|(
name|segmentGranularity
argument_list|)
argument_list|,
name|Granularity
operator|.
name|fromString
argument_list|(
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_GRANULARITY
argument_list|)
operator|==
literal|null
condition|?
literal|"NONE"
else|:
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_GRANULARITY
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnNameProperty
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnTypeProperty
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|columnNameProperty
argument_list|)
operator|||
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|columnTypeProperty
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"List of columns names [%s] or columns type [%s] is/are not present"
argument_list|,
name|columnNameProperty
argument_list|,
name|columnTypeProperty
argument_list|)
argument_list|)
throw|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|columnNames
operator|.
name|contains
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Timestamp column (' "
operator|+
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
operator|+
literal|"') not specified in create table; list of columns is : "
operator|+
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
argument_list|)
throw|;
block|}
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
decl_stmt|;
comment|// Default, all columns that are not metrics or timestamp, are treated as dimensions
specifier|final
name|List
argument_list|<
name|DimensionSchema
argument_list|>
name|dimensions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|AggregatorFactory
argument_list|>
name|aggregatorFactoryBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
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
name|columnTypes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|PrimitiveTypeInfo
name|f
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|AggregatorFactory
name|af
decl_stmt|;
switch|switch
condition|(
name|f
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
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
name|af
operator|=
operator|new
name|LongSumAggregatorFactory
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|DECIMAL
case|:
name|af
operator|=
operator|new
name|DoubleSumAggregatorFactory
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|String
name|tColumnName
init|=
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tColumnName
operator|.
name|equals
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
operator|&&
operator|!
name|tColumnName
operator|.
name|equals
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Dimension "
operator|+
name|tColumnName
operator|+
literal|" does not have STRING type: "
operator|+
name|f
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
continue|continue;
default|default:
comment|// Dimension
name|String
name|dColumnName
init|=
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|f
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
operator|!=
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Dimension "
operator|+
name|dColumnName
operator|+
literal|" does not have STRING type: "
operator|+
name|f
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
name|dimensions
operator|.
name|add
argument_list|(
operator|new
name|StringDimensionSchema
argument_list|(
name|dColumnName
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|aggregatorFactoryBuilder
operator|.
name|add
argument_list|(
name|af
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|AggregatorFactory
argument_list|>
name|aggregatorFactories
init|=
name|aggregatorFactoryBuilder
operator|.
name|build
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
name|DruidTable
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
name|dimensions
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|inputParser
init|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|convertValue
argument_list|(
name|inputRowParser
argument_list|,
name|Map
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|DataSchema
name|dataSchema
init|=
operator|new
name|DataSchema
argument_list|(
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dataSource
argument_list|,
literal|"Data source name is null"
argument_list|)
argument_list|,
name|inputParser
argument_list|,
name|aggregatorFactories
operator|.
name|toArray
argument_list|(
operator|new
name|AggregatorFactory
index|[
name|aggregatorFactories
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|granularitySpec
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
argument_list|)
decl_stmt|;
specifier|final
name|String
name|workingPath
init|=
name|jc
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_JOB_WORKING_DIRECTORY
argument_list|)
decl_stmt|;
specifier|final
name|String
name|version
init|=
name|jc
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_VERSION
argument_list|)
decl_stmt|;
name|Integer
name|maxPartitionSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_MAX_PARTITION_SIZE
argument_list|)
decl_stmt|;
name|String
name|basePersistDirectory
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_BASE_PERSIST_DIRECTORY
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|basePersistDirectory
argument_list|)
condition|)
block|{
name|basePersistDirectory
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
expr_stmt|;
block|}
name|Integer
name|maxRowInMemory
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_MAX_ROW_IN_MEMORY
argument_list|)
decl_stmt|;
name|IndexSpec
name|indexSpec
decl_stmt|;
if|if
condition|(
literal|"concise"
operator|.
name|equals
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_BITMAP_FACTORY_TYPE
argument_list|)
argument_list|)
condition|)
block|{
name|indexSpec
operator|=
operator|new
name|IndexSpec
argument_list|(
operator|new
name|ConciseBitmapSerdeFactory
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexSpec
operator|=
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
expr_stmt|;
block|}
name|RealtimeTuningConfig
name|realtimeTuningConfig
init|=
operator|new
name|RealtimeTuningConfig
argument_list|(
name|maxRowInMemory
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|File
argument_list|(
name|basePersistDirectory
argument_list|,
name|dataSource
argument_list|)
argument_list|,
operator|new
name|CustomVersioningPolicy
argument_list|(
name|version
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indexSpec
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"running with Data schema [%s] "
argument_list|,
name|dataSchema
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|DruidRecordWriter
argument_list|(
name|dataSchema
argument_list|,
name|realtimeTuningConfig
argument_list|,
name|hdfsDataSegmentPusher
argument_list|,
name|maxPartitionSize
argument_list|,
operator|new
name|Path
argument_list|(
name|workingPath
argument_list|,
name|SEGMENTS_DESCRIPTOR_DIR_NAME
argument_list|)
argument_list|,
name|finalOutPath
operator|.
name|getFileSystem
argument_list|(
name|jc
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|DruidWritable
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"please implement me !"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// NOOP
block|}
block|}
end_class

end_unit

