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
name|druid
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
name|java
operator|.
name|util
operator|.
name|http
operator|.
name|client
operator|.
name|Request
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
name|BaseQuery
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
name|LocatedSegmentDescriptor
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
name|Query
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
name|SegmentDescriptor
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
name|scan
operator|.
name|ScanQuery
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
name|select
operator|.
name|PagingSpec
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
name|select
operator|.
name|SelectQuery
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
name|spec
operator|.
name|MultipleSpecificSegmentSpec
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
name|lang3
operator|.
name|StringEscapeUtils
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
name|lang3
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
name|serde
operator|.
name|DruidGroupByQueryRecordReader
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
name|DruidQueryRecordReader
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
name|DruidScanQueryRecordReader
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
name|DruidSelectQueryRecordReader
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
name|DruidTimeseriesQueryRecordReader
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
name|DruidTopNQueryRecordReader
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
name|Utilities
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
name|VectorizedInputFormatInterface
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
name|VectorizedSupport
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
name|shims
operator|.
name|ShimLoader
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
name|NullWritable
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
name|hadoop
operator|.
name|mapreduce
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|RecordReader
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpMethod
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Druid query based input format.  *  * Given a query and the Druid broker address, it will send it, and retrieve  * and parse the results.  */
end_comment

begin_class
specifier|public
class|class
name|DruidQueryBasedInputFormat
extends|extends
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|DruidWritable
argument_list|>
implements|implements
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|DruidWritable
argument_list|>
implements|,
name|VectorizedInputFormatInterface
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DruidQueryBasedInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|DruidQueryRecordReader
name|getDruidQueryReader
parameter_list|(
name|String
name|druidQueryType
parameter_list|)
block|{
switch|switch
condition|(
name|druidQueryType
condition|)
block|{
case|case
name|Query
operator|.
name|TIMESERIES
case|:
return|return
operator|new
name|DruidTimeseriesQueryRecordReader
argument_list|()
return|;
case|case
name|Query
operator|.
name|TOPN
case|:
return|return
operator|new
name|DruidTopNQueryRecordReader
argument_list|()
return|;
case|case
name|Query
operator|.
name|GROUP_BY
case|:
return|return
operator|new
name|DruidGroupByQueryRecordReader
argument_list|()
return|;
case|case
name|Query
operator|.
name|SELECT
case|:
return|return
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
return|;
case|case
name|Query
operator|.
name|SCAN
case|:
return|return
operator|new
name|DruidScanQueryRecordReader
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Druid query type "
operator|+
name|druidQueryType
operator|+
literal|" not recognized"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getInputSplits
argument_list|(
name|job
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|getInputSplits
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|HiveDruidSplit
index|[]
name|getInputSplits
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|address
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_BROKER_DEFAULT_ADDRESS
argument_list|)
decl_stmt|;
name|String
name|queryId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|address
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid broker address not specified in configuration"
argument_list|)
throw|;
block|}
name|String
name|druidQuery
init|=
name|StringEscapeUtils
operator|.
name|unescapeJava
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_JSON
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|druidQueryType
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|druidQuery
argument_list|)
condition|)
block|{
comment|// Empty, maybe because CBO did not run; we fall back to
comment|// full Select query
if|if
condition|(
name|LOG
operator|.
name|isWarnEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Druid query is empty; creating Select query"
argument_list|)
expr_stmt|;
block|}
name|String
name|dataSource
init|=
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataSource
operator|==
literal|null
operator|||
name|dataSource
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid data source cannot be empty or null"
argument_list|)
throw|;
block|}
name|druidQuery
operator|=
name|DruidStorageHandlerUtils
operator|.
name|createScanAllQuery
argument_list|(
name|dataSource
argument_list|,
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|druidQueryType
operator|=
name|Query
operator|.
name|SCAN
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|,
name|druidQueryType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|druidQueryType
operator|=
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|druidQueryType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid query type not recognized"
argument_list|)
throw|;
block|}
block|}
comment|// Add Hive Query ID to Druid Query
if|if
condition|(
name|queryId
operator|!=
literal|null
condition|)
block|{
name|druidQuery
operator|=
name|withQueryId
argument_list|(
name|druidQuery
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
block|}
comment|// hive depends on FileSplits
name|Job
name|job
init|=
name|Job
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|JobContext
name|jobContext
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|newJobContext
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Path
index|[]
name|paths
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
comment|// We need to deserialize and serialize query so intervals are written in the JSON
comment|// Druid query with user timezone, as this is default Hive time semantics.
comment|// Then, create splits with the Druid queries.
switch|switch
condition|(
name|druidQueryType
condition|)
block|{
case|case
name|Query
operator|.
name|TIMESERIES
case|:
case|case
name|Query
operator|.
name|TOPN
case|:
case|case
name|Query
operator|.
name|GROUP_BY
case|:
return|return
operator|new
name|HiveDruidSplit
index|[]
block|{
operator|new
name|HiveDruidSplit
argument_list|(
name|druidQuery
argument_list|,
name|paths
index|[
literal|0
index|]
argument_list|,
operator|new
name|String
index|[]
block|{
name|address
block|}
argument_list|)
block|}
return|;
case|case
name|Query
operator|.
name|SELECT
case|:
name|SelectQuery
name|selectQuery
init|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|druidQuery
argument_list|,
name|SelectQuery
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|distributeSelectQuery
argument_list|(
name|address
argument_list|,
name|selectQuery
argument_list|,
name|paths
index|[
literal|0
index|]
argument_list|)
return|;
case|case
name|Query
operator|.
name|SCAN
case|:
name|ScanQuery
name|scanQuery
init|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|druidQuery
argument_list|,
name|ScanQuery
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|distributeScanQuery
argument_list|(
name|address
argument_list|,
name|scanQuery
argument_list|,
name|paths
index|[
literal|0
index|]
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid query type not recognized"
argument_list|)
throw|;
block|}
block|}
comment|/* New method that distributes the Select query by creating splits containing    * information about different Druid nodes that have the data for the given    * query. */
specifier|private
specifier|static
name|HiveDruidSplit
index|[]
name|distributeSelectQuery
parameter_list|(
name|String
name|address
parameter_list|,
name|SelectQuery
name|query
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If it has a limit, we use it and we do not distribute the query
specifier|final
name|boolean
name|isFetch
init|=
name|query
operator|.
name|getContextBoolean
argument_list|(
name|DruidConstants
operator|.
name|DRUID_QUERY_FETCH
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|isFetch
condition|)
block|{
return|return
operator|new
name|HiveDruidSplit
index|[]
block|{
operator|new
name|HiveDruidSplit
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|query
argument_list|)
argument_list|,
name|dummyPath
argument_list|,
operator|new
name|String
index|[]
block|{
name|address
block|}
argument_list|)
block|}
return|;
block|}
specifier|final
name|List
argument_list|<
name|LocatedSegmentDescriptor
argument_list|>
name|segmentDescriptors
init|=
name|fetchLocatedSegmentDescriptors
argument_list|(
name|address
argument_list|,
name|query
argument_list|)
decl_stmt|;
comment|// Create one input split for each segment
specifier|final
name|int
name|numSplits
init|=
name|segmentDescriptors
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|HiveDruidSplit
index|[]
name|splits
init|=
operator|new
name|HiveDruidSplit
index|[
name|segmentDescriptors
operator|.
name|size
argument_list|()
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
name|numSplits
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|LocatedSegmentDescriptor
name|locatedSD
init|=
name|segmentDescriptors
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|String
index|[]
name|hosts
init|=
operator|new
name|String
index|[
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|hosts
index|[
name|j
index|]
operator|=
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getHost
argument_list|()
expr_stmt|;
block|}
comment|// Create partial Select query
specifier|final
name|SegmentDescriptor
name|newSD
init|=
operator|new
name|SegmentDescriptor
argument_list|(
name|locatedSD
operator|.
name|getInterval
argument_list|()
argument_list|,
name|locatedSD
operator|.
name|getVersion
argument_list|()
argument_list|,
name|locatedSD
operator|.
name|getPartitionNumber
argument_list|()
argument_list|)
decl_stmt|;
comment|//@TODO This is fetching all the rows at once from broker or multiple historical nodes
comment|// Move to use scan query to avoid GC back pressure on the nodes
comment|// https://issues.apache.org/jira/browse/HIVE-17627
specifier|final
name|SelectQuery
name|partialQuery
init|=
name|query
operator|.
name|withQuerySegmentSpec
argument_list|(
operator|new
name|MultipleSpecificSegmentSpec
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|newSD
argument_list|)
argument_list|)
argument_list|)
operator|.
name|withPagingSpec
argument_list|(
name|PagingSpec
operator|.
name|newSpec
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|HiveDruidSplit
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|partialQuery
argument_list|)
argument_list|,
name|dummyPath
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
block|}
return|return
name|splits
return|;
block|}
comment|/* New method that distributes the Scan query by creating splits containing    * information about different Druid nodes that have the data for the given    * query. */
specifier|private
specifier|static
name|HiveDruidSplit
index|[]
name|distributeScanQuery
parameter_list|(
name|String
name|address
parameter_list|,
name|ScanQuery
name|query
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If it has a limit, we use it and we do not distribute the query
specifier|final
name|boolean
name|isFetch
init|=
name|query
operator|.
name|getLimit
argument_list|()
operator|<
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
if|if
condition|(
name|isFetch
condition|)
block|{
return|return
operator|new
name|HiveDruidSplit
index|[]
block|{
operator|new
name|HiveDruidSplit
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|query
argument_list|)
argument_list|,
name|dummyPath
argument_list|,
operator|new
name|String
index|[]
block|{
name|address
block|}
argument_list|)
block|}
return|;
block|}
specifier|final
name|List
argument_list|<
name|LocatedSegmentDescriptor
argument_list|>
name|segmentDescriptors
init|=
name|fetchLocatedSegmentDescriptors
argument_list|(
name|address
argument_list|,
name|query
argument_list|)
decl_stmt|;
comment|// Create one input split for each segment
specifier|final
name|int
name|numSplits
init|=
name|segmentDescriptors
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|HiveDruidSplit
index|[]
name|splits
init|=
operator|new
name|HiveDruidSplit
index|[
name|segmentDescriptors
operator|.
name|size
argument_list|()
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
name|numSplits
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|LocatedSegmentDescriptor
name|locatedSD
init|=
name|segmentDescriptors
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|String
index|[]
name|hosts
init|=
operator|new
name|String
index|[
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|1
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|hosts
index|[
name|j
index|]
operator|=
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getHost
argument_list|()
expr_stmt|;
block|}
comment|// Default to broker if all other hosts fail.
name|hosts
index|[
name|locatedSD
operator|.
name|getLocations
argument_list|()
operator|.
name|size
argument_list|()
index|]
operator|=
name|address
expr_stmt|;
comment|// Create partial Select query
specifier|final
name|SegmentDescriptor
name|newSD
init|=
operator|new
name|SegmentDescriptor
argument_list|(
name|locatedSD
operator|.
name|getInterval
argument_list|()
argument_list|,
name|locatedSD
operator|.
name|getVersion
argument_list|()
argument_list|,
name|locatedSD
operator|.
name|getPartitionNumber
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Query
name|partialQuery
init|=
name|query
operator|.
name|withQuerySegmentSpec
argument_list|(
operator|new
name|MultipleSpecificSegmentSpec
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|newSD
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|HiveDruidSplit
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|partialQuery
argument_list|)
argument_list|,
name|dummyPath
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
block|}
return|return
name|splits
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|LocatedSegmentDescriptor
argument_list|>
name|fetchLocatedSegmentDescriptors
parameter_list|(
name|String
name|address
parameter_list|,
name|BaseQuery
name|query
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|intervals
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|query
operator|.
name|getIntervals
argument_list|()
argument_list|,
literal|","
argument_list|)
decl_stmt|;
comment|// Comma-separated intervals without brackets
specifier|final
name|String
name|request
init|=
name|String
operator|.
name|format
argument_list|(
literal|"http://%s/druid/v2/datasources/%s/candidates?intervals=%s"
argument_list|,
name|address
argument_list|,
name|query
operator|.
name|getDataSource
argument_list|()
operator|.
name|getNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|URLEncoder
operator|.
name|encode
argument_list|(
name|intervals
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"sending request {} to query for segments"
argument_list|,
name|request
argument_list|)
expr_stmt|;
specifier|final
name|InputStream
name|response
decl_stmt|;
try|try
block|{
name|response
operator|=
name|DruidStorageHandlerUtils
operator|.
name|submitRequest
argument_list|(
name|DruidStorageHandler
operator|.
name|getHttpClient
argument_list|()
argument_list|,
operator|new
name|Request
argument_list|(
name|HttpMethod
operator|.
name|GET
argument_list|,
operator|new
name|URL
argument_list|(
name|request
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
comment|// Retrieve results
specifier|final
name|List
argument_list|<
name|LocatedSegmentDescriptor
argument_list|>
name|segmentDescriptors
decl_stmt|;
try|try
block|{
name|segmentDescriptors
operator|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|response
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|LocatedSegmentDescriptor
argument_list|>
argument_list|>
argument_list|()
block|{           }
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|response
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|segmentDescriptors
return|;
block|}
specifier|private
specifier|static
name|String
name|withQueryId
parameter_list|(
name|String
name|druidQuery
parameter_list|,
name|String
name|queryId
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
argument_list|<
name|?
argument_list|>
name|queryWithId
init|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|druidQuery
argument_list|,
name|BaseQuery
operator|.
name|class
argument_list|)
operator|.
name|withId
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
return|return
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|queryWithId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|DruidWritable
argument_list|>
name|getRecordReader
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We need to provide a different record reader for every type of Druid query.
comment|// The reason is that Druid results format is different for each type.
specifier|final
name|DruidQueryRecordReader
argument_list|<
name|?
argument_list|>
name|reader
decl_stmt|;
comment|// By default, we use druid scan query as fallback.
specifier|final
name|String
name|druidQueryType
init|=
name|job
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|,
name|Query
operator|.
name|SCAN
argument_list|)
decl_stmt|;
name|reader
operator|=
name|getDruidQueryReader
argument_list|(
name|druidQueryType
argument_list|)
expr_stmt|;
name|reader
operator|.
name|initialize
argument_list|(
operator|(
name|HiveDruidSplit
operator|)
name|split
argument_list|,
name|job
argument_list|)
expr_stmt|;
if|if
condition|(
name|Utilities
operator|.
name|getIsVectorized
argument_list|(
name|job
argument_list|)
condition|)
block|{
comment|//noinspection unchecked
return|return
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
operator|)
operator|new
name|DruidVectorizedWrapper
argument_list|(
name|reader
argument_list|,
name|job
argument_list|)
return|;
block|}
return|return
name|reader
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|DruidWritable
argument_list|>
name|createRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// By default, we use druid scan query as fallback.
specifier|final
name|String
name|druidQueryType
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|,
name|Query
operator|.
name|SCAN
argument_list|)
decl_stmt|;
comment|// We need to provide a different record reader for every type of Druid query.
comment|// The reason is that Druid results format is different for each type.
comment|//noinspection unchecked
return|return
name|getDruidQueryReader
argument_list|(
name|druidQueryType
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizedSupport
operator|.
name|Support
index|[]
name|getSupportedFeatures
parameter_list|()
block|{
return|return
operator|new
name|VectorizedSupport
operator|.
name|Support
index|[
literal|0
index|]
return|;
block|}
block|}
end_class

end_unit

