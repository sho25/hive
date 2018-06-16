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
name|TimeZone
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
name|cache
operator|.
name|BufferUsageManager
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
name|SerDeLowLevelCacheImpl
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
name|encoded
operator|.
name|SerDeEncodedDataReader
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
name|ConsumerFileMetadata
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
name|ConsumerStripeMetadata
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
name|LlapDaemonCacheMetrics
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
name|IoTrace
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
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|PartitionDesc
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
name|Deserializer
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
name|SerDeException
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
name|InputFormat
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
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|FixedSizedObjectPool
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
name|CompressionKind
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
name|OrcFile
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
name|ColumnEncoding
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
name|RowIndex
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
name|orc
operator|.
name|TypeDescription
import|;
end_import

begin_class
specifier|public
class|class
name|GenericColumnVectorProducer
implements|implements
name|ColumnVectorProducer
block|{
specifier|private
specifier|final
name|SerDeLowLevelCacheImpl
name|cache
decl_stmt|;
specifier|private
specifier|final
name|BufferUsageManager
name|bufferManager
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonCacheMetrics
name|cacheMetrics
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonIOMetrics
name|ioMetrics
decl_stmt|;
specifier|private
specifier|final
name|FixedSizedObjectPool
argument_list|<
name|IoTrace
argument_list|>
name|tracePool
decl_stmt|;
specifier|public
name|GenericColumnVectorProducer
parameter_list|(
name|SerDeLowLevelCacheImpl
name|serdeCache
parameter_list|,
name|BufferUsageManager
name|bufferManager
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|LlapDaemonCacheMetrics
name|cacheMetrics
parameter_list|,
name|LlapDaemonIOMetrics
name|ioMetrics
parameter_list|,
name|FixedSizedObjectPool
argument_list|<
name|IoTrace
argument_list|>
name|tracePool
parameter_list|)
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
name|this
operator|.
name|cache
operator|=
name|serdeCache
expr_stmt|;
name|this
operator|.
name|bufferManager
operator|=
name|bufferManager
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cacheMetrics
operator|=
name|cacheMetrics
expr_stmt|;
name|this
operator|.
name|ioMetrics
operator|=
name|ioMetrics
expr_stmt|;
name|this
operator|.
name|tracePool
operator|=
name|tracePool
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReadPipeline
name|createReadPipeline
parameter_list|(
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|consumer
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|Includes
name|includes
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|QueryFragmentCounters
name|counters
parameter_list|,
name|SchemaEvolutionFactory
name|sef
parameter_list|,
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sourceInputFormat
parameter_list|,
name|Deserializer
name|sourceSerDe
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|parts
parameter_list|)
throws|throws
name|IOException
block|{
name|cacheMetrics
operator|.
name|incrCacheReadRequests
argument_list|()
expr_stmt|;
name|OrcEncodedDataConsumer
name|edc
init|=
operator|new
name|OrcEncodedDataConsumer
argument_list|(
name|consumer
argument_list|,
name|includes
argument_list|,
literal|false
argument_list|,
name|counters
argument_list|,
name|ioMetrics
argument_list|)
decl_stmt|;
name|SerDeFileMetadata
name|fm
decl_stmt|;
try|try
block|{
name|fm
operator|=
operator|new
name|SerDeFileMetadata
argument_list|(
name|sourceSerDe
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|edc
operator|.
name|setFileMetadata
argument_list|(
name|fm
argument_list|)
expr_stmt|;
comment|// Note that we pass job config to the record reader, but use global config for LLAP IO.
comment|// TODO: add tracing to serde reader
name|SerDeEncodedDataReader
name|reader
init|=
operator|new
name|SerDeEncodedDataReader
argument_list|(
name|cache
argument_list|,
name|bufferManager
argument_list|,
name|conf
argument_list|,
name|split
argument_list|,
name|includes
operator|.
name|getPhysicalColumnIds
argument_list|()
argument_list|,
name|edc
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|,
name|sourceInputFormat
argument_list|,
name|sourceSerDe
argument_list|,
name|counters
argument_list|,
name|fm
operator|.
name|getSchema
argument_list|()
argument_list|,
name|parts
argument_list|)
decl_stmt|;
name|edc
operator|.
name|init
argument_list|(
name|reader
argument_list|,
name|reader
argument_list|,
operator|new
name|IoTrace
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|edc
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|SerDeStripeMetadata
implements|implements
name|ConsumerStripeMetadata
block|{
comment|// The writer is local to the process.
specifier|private
specifier|final
name|String
name|writerTimezone
init|=
name|TimeZone
operator|.
name|getDefault
argument_list|()
operator|.
name|getID
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
decl_stmt|;
specifier|private
specifier|final
name|int
name|stripeIx
decl_stmt|;
specifier|private
name|long
name|rowCount
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|SerDeStripeMetadata
parameter_list|(
name|int
name|stripeIx
parameter_list|)
block|{
name|this
operator|.
name|stripeIx
operator|=
name|stripeIx
expr_stmt|;
block|}
annotation|@
name|Override
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
name|int
name|getStripeIx
parameter_list|()
block|{
return|return
name|stripeIx
return|;
block|}
annotation|@
name|Override
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
name|Override
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|setEncodings
parameter_list|(
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
parameter_list|)
block|{
name|this
operator|.
name|encodings
operator|=
name|encodings
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RowIndex
index|[]
name|getRowIndexes
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsRowIndexes
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|setRowCount
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|rowCount
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[stripeIx="
operator|+
name|stripeIx
operator|+
literal|", rowCount="
operator|+
name|rowCount
operator|+
literal|", encodings="
operator|+
name|encodings
operator|+
literal|"]"
operator|.
name|replace
argument_list|(
literal|'\n'
argument_list|,
literal|' '
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|SerDeFileMetadata
implements|implements
name|ConsumerFileMetadata
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Type
argument_list|>
name|orcTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|TypeDescription
name|schema
decl_stmt|;
specifier|public
name|SerDeFileMetadata
parameter_list|(
name|Deserializer
name|sourceSerDe
parameter_list|)
throws|throws
name|SerDeException
block|{
name|TypeDescription
name|schema
init|=
name|OrcInputFormat
operator|.
name|convertTypeInfo
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|sourceSerDe
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|addTypesFromSchema
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
comment|// Copied here until a utility version of this released in ORC.
specifier|public
specifier|static
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|setTypeBuilderFromSchema
parameter_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Builder
name|type
parameter_list|,
name|TypeDescription
name|schema
parameter_list|)
block|{
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|schema
operator|.
name|getChildren
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|schema
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BYTE
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|SHORT
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LONG
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRING
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|schema
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|VARCHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|schema
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BINARY
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DECIMAL
argument_list|)
expr_stmt|;
name|type
operator|.
name|setPrecision
argument_list|(
name|schema
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|type
operator|.
name|setScale
argument_list|(
name|schema
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LIST
argument_list|)
expr_stmt|;
name|type
operator|.
name|addSubtypes
argument_list|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|MAP
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|STRUCT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRUCT
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|field
range|:
name|schema
operator|.
name|getFieldNames
argument_list|()
control|)
block|{
name|type
operator|.
name|addFieldNames
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|UNION
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown category: "
operator|+
name|schema
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|children
return|;
block|}
specifier|private
name|void
name|addTypesFromSchema
parameter_list|(
name|TypeDescription
name|schema
parameter_list|)
block|{
comment|// The same thing that WriterImpl does when writing the footer, but w/o the footer.
name|OrcProto
operator|.
name|Type
operator|.
name|Builder
name|type
init|=
name|OrcProto
operator|.
name|Type
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|setTypeBuilderFromSchema
argument_list|(
name|type
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|orcTypes
operator|.
name|add
argument_list|(
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|addTypesFromSchema
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Type
argument_list|>
name|getTypes
parameter_list|()
block|{
return|return
name|orcTypes
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStripeCount
parameter_list|()
block|{
return|return
literal|1
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
name|CompressionKind
operator|.
name|NONE
return|;
block|}
annotation|@
name|Override
specifier|public
name|TypeDescription
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
annotation|@
name|Override
specifier|public
name|OrcFile
operator|.
name|Version
name|getFileVersion
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

