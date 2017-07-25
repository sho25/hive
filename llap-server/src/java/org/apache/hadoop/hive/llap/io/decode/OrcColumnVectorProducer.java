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
name|java
operator|.
name|util
operator|.
name|Map
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
name|common
operator|.
name|Pool
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|LowLevelCache
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
name|OrcEncodedDataReader
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|OrcConf
import|;
end_import

begin_class
specifier|public
class|class
name|OrcColumnVectorProducer
implements|implements
name|ColumnVectorProducer
block|{
specifier|private
specifier|final
name|OrcMetadataCache
name|metadataCache
decl_stmt|;
specifier|private
specifier|final
name|LowLevelCache
name|lowLevelCache
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
name|boolean
name|_skipCorrupt
decl_stmt|;
comment|// TODO: get rid of this
specifier|private
name|LlapDaemonCacheMetrics
name|cacheMetrics
decl_stmt|;
specifier|private
name|LlapDaemonIOMetrics
name|ioMetrics
decl_stmt|;
comment|// TODO: if using in multiple places, e.g. SerDe cache, pass this in.
comment|// TODO: should this rather use a threadlocal for NUMA affinity?
specifier|private
specifier|final
name|FixedSizedObjectPool
argument_list|<
name|IoTrace
argument_list|>
name|tracePool
decl_stmt|;
specifier|public
name|OrcColumnVectorProducer
parameter_list|(
name|OrcMetadataCache
name|metadataCache
parameter_list|,
name|LowLevelCache
name|lowLevelCache
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
name|metadataCache
operator|=
name|metadataCache
expr_stmt|;
name|this
operator|.
name|lowLevelCache
operator|=
name|lowLevelCache
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
name|_skipCorrupt
operator|=
name|OrcConf
operator|.
name|SKIP_CORRUPT_DATA
operator|.
name|getBoolean
argument_list|(
name|conf
argument_list|)
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
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|,
name|QueryFragmentCounters
name|counters
parameter_list|,
name|TypeDescription
name|readerSchema
parameter_list|,
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|unused0
parameter_list|,
name|Deserializer
name|unused1
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
name|unused2
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
name|columnIds
operator|.
name|size
argument_list|()
argument_list|,
name|_skipCorrupt
argument_list|,
name|counters
argument_list|,
name|ioMetrics
argument_list|)
decl_stmt|;
name|OrcEncodedDataReader
name|reader
init|=
operator|new
name|OrcEncodedDataReader
argument_list|(
name|lowLevelCache
argument_list|,
name|bufferManager
argument_list|,
name|metadataCache
argument_list|,
name|conf
argument_list|,
name|job
argument_list|,
name|split
argument_list|,
name|columnIds
argument_list|,
name|sarg
argument_list|,
name|columnNames
argument_list|,
name|edc
argument_list|,
name|counters
argument_list|,
name|readerSchema
argument_list|,
name|tracePool
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
name|reader
operator|.
name|getTrace
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|edc
return|;
block|}
block|}
end_class

end_unit

