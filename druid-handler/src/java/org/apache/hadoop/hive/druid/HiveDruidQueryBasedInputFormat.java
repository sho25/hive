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
name|druid
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
name|io
operator|.
name|InputStream
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
name|Arrays
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
name|Map
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
name|optimizer
operator|.
name|calcite
operator|.
name|druid
operator|.
name|DruidIntervalUtils
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
name|optimizer
operator|.
name|calcite
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
name|metamx
operator|.
name|common
operator|.
name|lifecycle
operator|.
name|Lifecycle
import|;
end_import

begin_import
import|import
name|com
operator|.
name|metamx
operator|.
name|http
operator|.
name|client
operator|.
name|HttpClient
import|;
end_import

begin_import
import|import
name|com
operator|.
name|metamx
operator|.
name|http
operator|.
name|client
operator|.
name|HttpClientConfig
import|;
end_import

begin_import
import|import
name|com
operator|.
name|metamx
operator|.
name|http
operator|.
name|client
operator|.
name|HttpClientInit
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
name|Druids
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
name|Druids
operator|.
name|SegmentMetadataQueryBuilder
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
name|Druids
operator|.
name|SelectQueryBuilder
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
name|Druids
operator|.
name|TimeBoundaryQueryBuilder
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
name|Query
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
name|Result
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
name|metadata
operator|.
name|metadata
operator|.
name|SegmentAnalysis
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
name|metadata
operator|.
name|metadata
operator|.
name|SegmentMetadataQuery
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
name|select
operator|.
name|PagingSpec
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
name|select
operator|.
name|SelectQuery
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
name|spec
operator|.
name|MultipleIntervalSegmentSpec
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
name|timeboundary
operator|.
name|TimeBoundaryQuery
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
name|timeboundary
operator|.
name|TimeBoundaryResultValue
import|;
end_import

begin_comment
comment|/**  * Druid query based input format.  *   * Given a query and the Druid broker address, it will send it, and retrieve  * and parse the results.  */
end_comment

begin_class
specifier|public
class|class
name|HiveDruidQueryBasedInputFormat
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
name|HiveDruidQueryBasedInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
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
expr|<
name|InputSplit
operator|>
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
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
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid data source cannot be empty"
argument_list|)
throw|;
block|}
name|druidQuery
operator|=
name|createSelectStarQuery
argument_list|(
name|address
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
name|druidQueryType
operator|=
name|Query
operator|.
name|SELECT
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
comment|// hive depends on FileSplits
name|Job
name|job
init|=
operator|new
name|Job
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
name|address
argument_list|,
name|druidQuery
argument_list|,
name|paths
index|[
literal|0
index|]
argument_list|)
block|}
return|;
case|case
name|Query
operator|.
name|SELECT
case|:
return|return
name|splitSelectQuery
argument_list|(
name|conf
argument_list|,
name|address
argument_list|,
name|druidQuery
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
specifier|private
specifier|static
name|String
name|createSelectStarQuery
parameter_list|(
name|String
name|address
parameter_list|,
name|String
name|dataSource
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create Select query
name|SelectQueryBuilder
name|builder
init|=
operator|new
name|Druids
operator|.
name|SelectQueryBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|dataSource
argument_list|(
name|dataSource
argument_list|)
expr_stmt|;
name|builder
operator|.
name|intervals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_INTERVAL
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|pagingSpec
argument_list|(
name|PagingSpec
operator|.
name|newSpec
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|context
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|context
operator|.
name|put
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_FETCH
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builder
operator|.
name|context
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
comment|/* Method that splits Select query depending on the threshold so read can be    * parallelized */
specifier|private
specifier|static
name|HiveDruidSplit
index|[]
name|splitSelectQuery
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|address
parameter_list|,
name|String
name|druidQuery
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|selectThreshold
init|=
operator|(
name|int
operator|)
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_SELECT_THRESHOLD
argument_list|)
decl_stmt|;
name|SelectQuery
name|query
decl_stmt|;
try|try
block|{
name|query
operator|=
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
name|e
argument_list|)
throw|;
block|}
specifier|final
name|boolean
name|isFetch
init|=
name|query
operator|.
name|getContextBoolean
argument_list|(
name|Constants
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
comment|// If it has a limit, we use it and we do not split the query
return|return
operator|new
name|HiveDruidSplit
index|[]
block|{
operator|new
name|HiveDruidSplit
argument_list|(
name|address
argument_list|,
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
argument_list|)
block|}
return|;
block|}
comment|// We do not have the number of rows, thus we need to execute a
comment|// Segment Metadata query to obtain number of rows
name|SegmentMetadataQueryBuilder
name|metadataBuilder
init|=
operator|new
name|Druids
operator|.
name|SegmentMetadataQueryBuilder
argument_list|()
decl_stmt|;
name|metadataBuilder
operator|.
name|dataSource
argument_list|(
name|query
operator|.
name|getDataSource
argument_list|()
argument_list|)
expr_stmt|;
name|metadataBuilder
operator|.
name|intervals
argument_list|(
name|query
operator|.
name|getIntervals
argument_list|()
argument_list|)
expr_stmt|;
name|metadataBuilder
operator|.
name|merge
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|metadataBuilder
operator|.
name|analysisTypes
argument_list|()
expr_stmt|;
name|SegmentMetadataQuery
name|metadataQuery
init|=
name|metadataBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|HttpClient
name|client
init|=
name|HttpClientInit
operator|.
name|createClient
argument_list|(
name|HttpClientConfig
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|Lifecycle
argument_list|()
argument_list|)
decl_stmt|;
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
name|client
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|createRequest
argument_list|(
name|address
argument_list|,
name|metadataQuery
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
name|List
argument_list|<
name|SegmentAnalysis
argument_list|>
name|metadataList
decl_stmt|;
try|try
block|{
name|metadataList
operator|=
name|DruidStorageHandlerUtils
operator|.
name|SMILE_MAPPER
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
name|SegmentAnalysis
argument_list|>
argument_list|>
argument_list|()
block|{}
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
if|if
condition|(
name|metadataList
operator|==
literal|null
operator|||
name|metadataList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Connected to Druid but could not retrieve datasource information"
argument_list|)
throw|;
block|}
if|if
condition|(
name|metadataList
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Information about segments should have been merged"
argument_list|)
throw|;
block|}
specifier|final
name|long
name|numRows
init|=
name|metadataList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getNumRows
argument_list|()
decl_stmt|;
name|query
operator|=
name|query
operator|.
name|withPagingSpec
argument_list|(
name|PagingSpec
operator|.
name|newSpec
argument_list|(
name|selectThreshold
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|numRows
operator|<=
name|selectThreshold
condition|)
block|{
comment|// We are not going to split it
return|return
operator|new
name|HiveDruidSplit
index|[]
block|{
operator|new
name|HiveDruidSplit
argument_list|(
name|address
argument_list|,
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
argument_list|)
block|}
return|;
block|}
comment|// If the query does not specify a timestamp, we obtain the total time using
comment|// a Time Boundary query. Then, we use the information to split the query
comment|// following the Select threshold configuration property
specifier|final
name|List
argument_list|<
name|Interval
argument_list|>
name|intervals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|.
name|getIntervals
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|query
operator|.
name|getIntervals
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_INTERVAL
argument_list|)
condition|)
block|{
comment|// Default max and min, we should execute a time boundary query to get a
comment|// more precise range
name|TimeBoundaryQueryBuilder
name|timeBuilder
init|=
operator|new
name|Druids
operator|.
name|TimeBoundaryQueryBuilder
argument_list|()
decl_stmt|;
name|timeBuilder
operator|.
name|dataSource
argument_list|(
name|query
operator|.
name|getDataSource
argument_list|()
argument_list|)
expr_stmt|;
name|TimeBoundaryQuery
name|timeQuery
init|=
name|timeBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|response
operator|=
name|DruidStorageHandlerUtils
operator|.
name|submitRequest
argument_list|(
name|client
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|createRequest
argument_list|(
name|address
argument_list|,
name|timeQuery
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
name|List
argument_list|<
name|Result
argument_list|<
name|TimeBoundaryResultValue
argument_list|>
argument_list|>
name|timeList
decl_stmt|;
try|try
block|{
name|timeList
operator|=
name|DruidStorageHandlerUtils
operator|.
name|SMILE_MAPPER
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
name|Result
argument_list|<
name|TimeBoundaryResultValue
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{}
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
if|if
condition|(
name|timeList
operator|==
literal|null
operator|||
name|timeList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Connected to Druid but could not retrieve time boundary information"
argument_list|)
throw|;
block|}
if|if
condition|(
name|timeList
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"We should obtain a single time boundary"
argument_list|)
throw|;
block|}
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
name|timeList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getMinTime
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|,
name|timeList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|getMaxTime
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|intervals
operator|.
name|addAll
argument_list|(
name|query
operator|.
name|getIntervals
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Create (numRows/default threshold) input splits
name|int
name|numSplits
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|double
operator|)
name|numRows
operator|/
name|selectThreshold
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
name|newIntervals
init|=
name|createSplitsIntervals
argument_list|(
name|intervals
argument_list|,
name|numSplits
argument_list|)
decl_stmt|;
name|HiveDruidSplit
index|[]
name|splits
init|=
operator|new
name|HiveDruidSplit
index|[
name|numSplits
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
comment|// Create partial Select query
specifier|final
name|SelectQuery
name|partialQuery
init|=
name|query
operator|.
name|withQuerySegmentSpec
argument_list|(
operator|new
name|MultipleIntervalSegmentSpec
argument_list|(
name|newIntervals
operator|.
name|get
argument_list|(
name|i
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
name|address
argument_list|,
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
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
name|createSplitsIntervals
parameter_list|(
name|List
argument_list|<
name|Interval
argument_list|>
name|intervals
parameter_list|,
name|int
name|numSplits
parameter_list|)
block|{
specifier|final
name|long
name|totalTime
init|=
name|DruidIntervalUtils
operator|.
name|extractTotalTime
argument_list|(
name|intervals
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|intervals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStartMillis
argument_list|()
decl_stmt|;
name|long
name|endTime
init|=
name|startTime
decl_stmt|;
name|long
name|currTime
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
name|newIntervals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|posIntervals
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
name|long
name|rangeSize
init|=
name|Math
operator|.
name|round
argument_list|(
call|(
name|double
call|)
argument_list|(
name|totalTime
operator|*
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
operator|/
name|numSplits
argument_list|)
operator|-
name|Math
operator|.
name|round
argument_list|(
call|(
name|double
call|)
argument_list|(
name|totalTime
operator|*
name|i
argument_list|)
operator|/
name|numSplits
argument_list|)
decl_stmt|;
comment|// Create the new interval(s)
name|List
argument_list|<
name|Interval
argument_list|>
name|currentIntervals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|posIntervals
operator|<
name|intervals
operator|.
name|size
argument_list|()
condition|)
block|{
specifier|final
name|Interval
name|interval
init|=
name|intervals
operator|.
name|get
argument_list|(
name|posIntervals
argument_list|)
decl_stmt|;
specifier|final
name|long
name|expectedRange
init|=
name|rangeSize
operator|-
name|currTime
decl_stmt|;
if|if
condition|(
name|interval
operator|.
name|getEndMillis
argument_list|()
operator|-
name|startTime
operator|>=
name|expectedRange
condition|)
block|{
name|endTime
operator|=
name|startTime
operator|+
name|expectedRange
expr_stmt|;
name|currentIntervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
name|startTime
operator|=
name|endTime
expr_stmt|;
name|currTime
operator|=
literal|0
expr_stmt|;
break|break;
block|}
name|endTime
operator|=
name|interval
operator|.
name|getEndMillis
argument_list|()
expr_stmt|;
name|currentIntervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
argument_list|)
expr_stmt|;
name|currTime
operator|+=
operator|(
name|endTime
operator|-
name|startTime
operator|)
expr_stmt|;
name|startTime
operator|=
name|intervals
operator|.
name|get
argument_list|(
operator|++
name|posIntervals
argument_list|)
operator|.
name|getStartMillis
argument_list|()
expr_stmt|;
block|}
name|newIntervals
operator|.
name|add
argument_list|(
name|currentIntervals
argument_list|)
expr_stmt|;
block|}
assert|assert
name|endTime
operator|==
name|intervals
operator|.
name|get
argument_list|(
name|intervals
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getEndMillis
argument_list|()
assert|;
return|return
name|newIntervals
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
argument_list|,
name|?
argument_list|>
name|reader
decl_stmt|;
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
argument_list|)
decl_stmt|;
if|if
condition|(
name|druidQueryType
operator|==
literal|null
condition|)
block|{
name|reader
operator|=
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
expr_stmt|;
comment|// By default
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
return|return
name|reader
return|;
block|}
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
name|reader
operator|=
operator|new
name|DruidTimeseriesQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|TOPN
case|:
name|reader
operator|=
operator|new
name|DruidTopNQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|GROUP_BY
case|:
name|reader
operator|=
operator|new
name|DruidGroupByQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|SELECT
case|:
name|reader
operator|=
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid query type not recognized"
argument_list|)
throw|;
block|}
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
comment|// We need to provide a different record reader for every type of Druid query.
comment|// The reason is that Druid results format is different for each type.
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
argument_list|)
decl_stmt|;
if|if
condition|(
name|druidQueryType
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
return|;
comment|// By default
block|}
specifier|final
name|DruidQueryRecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|reader
decl_stmt|;
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
name|reader
operator|=
operator|new
name|DruidTimeseriesQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|TOPN
case|:
name|reader
operator|=
operator|new
name|DruidTopNQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|GROUP_BY
case|:
name|reader
operator|=
operator|new
name|DruidGroupByQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|SELECT
case|:
name|reader
operator|=
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Druid query type not recognized"
argument_list|)
throw|;
block|}
return|return
name|reader
return|;
block|}
block|}
end_class

end_unit

