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
name|serde
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
name|JsonParser
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
name|JsonToken
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
name|ObjectCodec
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
name|JavaType
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
name|Throwables
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
name|IAE
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
name|RE
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
name|guava
operator|.
name|CloseQuietly
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
name|HttpClient
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
name|java
operator|.
name|util
operator|.
name|http
operator|.
name|client
operator|.
name|response
operator|.
name|InputStreamResponseHandler
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
name|QueryInterruptedException
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
name|io
operator|.
name|HiveDruidSplit
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
name|parquet
operator|.
name|Strings
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
name|Closeable
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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_comment
comment|/**  * Base record reader for given a Druid query. This class contains the logic to  * send the query to the broker and retrieve the results. The transformation to  * emit records needs to be done by the classes that extend the reader.  *  * The key for each record will be a NullWritable, while the value will be a  * DruidWritable containing the timestamp as well as all values resulting from  * the query.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DruidQueryRecordReader
parameter_list|<
name|R
extends|extends
name|Comparable
parameter_list|<
name|R
parameter_list|>
parameter_list|>
extends|extends
name|RecordReader
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
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|DruidWritable
argument_list|>
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
name|DruidQueryRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Query that Druid executes.    */
specifier|protected
name|Query
name|query
decl_stmt|;
comment|/**    * Query results as a streaming iterator.    */
name|JsonParserIterator
argument_list|<
name|R
argument_list|>
name|queryResultsIterator
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|initialize
argument_list|(
name|split
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|ObjectMapper
name|mapper
parameter_list|,
name|ObjectMapper
name|smileMapper
parameter_list|,
name|HttpClient
name|httpClient
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveDruidSplit
name|hiveDruidSplit
init|=
operator|(
name|HiveDruidSplit
operator|)
name|split
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hiveDruidSplit
argument_list|,
literal|"input split is null ???"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|httpClient
argument_list|,
literal|"need Http Client can not be null"
argument_list|)
expr_stmt|;
name|ObjectMapper
name|objectMapper
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|mapper
argument_list|,
literal|"object Mapper can not be null"
argument_list|)
decl_stmt|;
comment|// Smile mapper is used to read query results that are serialized as binary instead of json
comment|// Smile mapper is used to read query results that are serialized as binary instead of json
name|ObjectMapper
name|smileObjectMapper
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|smileMapper
argument_list|,
literal|"Smile Mapper can not be null"
argument_list|)
decl_stmt|;
comment|// Create query
name|this
operator|.
name|query
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hiveDruidSplit
operator|.
name|getDruidQuery
argument_list|()
argument_list|)
argument_list|,
name|Query
operator|.
name|class
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|query
argument_list|)
expr_stmt|;
comment|/*       Result type definition used to read the rows, this is query dependent.      */
name|JavaType
name|resultsType
init|=
name|getResultTypeDef
argument_list|()
decl_stmt|;
specifier|final
name|String
index|[]
name|locations
init|=
name|hiveDruidSplit
operator|.
name|getLocations
argument_list|()
decl_stmt|;
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
name|int
name|currentLocationIndex
init|=
literal|0
decl_stmt|;
name|Exception
name|ex
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|initialized
operator|&&
name|currentLocationIndex
operator|<
name|locations
operator|.
name|length
condition|)
block|{
name|String
name|address
init|=
name|locations
index|[
name|currentLocationIndex
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|address
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"can not fetch results from empty or null host value"
argument_list|)
throw|;
block|}
comment|// Execute query
name|LOG
operator|.
name|debug
argument_list|(
literal|"Retrieving data from druid location[{}] using query:[{}] "
argument_list|,
name|address
argument_list|,
name|query
argument_list|)
expr_stmt|;
try|try
block|{
name|Request
name|request
init|=
name|DruidStorageHandlerUtils
operator|.
name|createSmileRequest
argument_list|(
name|address
argument_list|,
name|query
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|InputStream
argument_list|>
name|inputStreamFuture
init|=
name|httpClient
operator|.
name|go
argument_list|(
name|request
argument_list|,
operator|new
name|InputStreamResponseHandler
argument_list|()
argument_list|)
decl_stmt|;
comment|//noinspection unchecked
name|queryResultsIterator
operator|=
operator|new
name|JsonParserIterator
argument_list|(
name|smileObjectMapper
argument_list|,
name|resultsType
argument_list|,
name|inputStreamFuture
argument_list|,
name|request
operator|.
name|getUrl
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|queryResultsIterator
operator|.
name|init
argument_list|()
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|ExecutionException
decl||
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|queryResultsIterator
operator|!=
literal|null
condition|)
block|{
comment|// We got exception while querying results from this host.
name|queryResultsIterator
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failure getting results for query[{}] from host[{}] because of [{}]"
argument_list|,
name|query
argument_list|,
name|address
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ex
operator|==
literal|null
condition|)
block|{
name|ex
operator|=
name|e
expr_stmt|;
block|}
else|else
block|{
name|ex
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
throw|throw
operator|new
name|RE
argument_list|(
name|ex
argument_list|,
literal|"Failure getting results for query[%s] from locations[%s] because of [%s]"
argument_list|,
name|query
argument_list|,
name|locations
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|ex
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|initialize
argument_list|(
name|split
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|SMILE_MAPPER
argument_list|,
name|DruidStorageHandler
operator|.
name|getHttpClient
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|JavaType
name|getResultTypeDef
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|DruidWritable
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|DruidWritable
argument_list|(
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|DruidWritable
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
block|{
comment|// HiveContextAwareRecordReader uses this position to track the block position and check
comment|// whether to skip header and footer. return -1 to since we need not skip any header and
comment|// footer rows for druid.
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|NullWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|DruidWritable
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|CloseQuietly
operator|.
name|close
argument_list|(
name|queryResultsIterator
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is a helper wrapper class used to create an iterator of druid rows out of InputStream.    * The type of the rows is defined by    * org.apache.hadoop.hive.druid.serde.DruidQueryRecordReader.JsonParserIterator#typeRef    *    * @param<R> druid Row type returned as result    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"TypeParameterHidesVisibleType"
argument_list|)
specifier|protected
class|class
name|JsonParserIterator
parameter_list|<
name|R
extends|extends
name|Comparable
parameter_list|<
name|R
parameter_list|>
parameter_list|>
implements|implements
name|Iterator
argument_list|<
name|R
argument_list|>
implements|,
name|Closeable
block|{
specifier|private
name|JsonParser
name|jp
decl_stmt|;
specifier|private
name|ObjectCodec
name|objectCodec
decl_stmt|;
specifier|private
specifier|final
name|ObjectMapper
name|mapper
decl_stmt|;
specifier|private
specifier|final
name|JavaType
name|typeRef
decl_stmt|;
specifier|private
specifier|final
name|Future
argument_list|<
name|InputStream
argument_list|>
name|future
decl_stmt|;
specifier|private
specifier|final
name|Query
name|query
decl_stmt|;
specifier|private
specifier|final
name|String
name|url
decl_stmt|;
comment|/**      * @param mapper mapper used to deserialize the stream of data (we use smile factory)      * @param typeRef Type definition of the results objects      * @param future Future holding the input stream (the input stream is not owned but it will be closed      *               when org.apache.hadoop.hive.druid.serde.DruidQueryRecordReader.JsonParserIterator#close() is called      *               or reach the end of the steam)      * @param url URL used to fetch the data, used mostly as message with exception stack to identify the faulty stream,      *           thus this can be empty string.      * @param query Query used to fetch the data, used mostly as message with exception stack, thus can be empty string.      */
name|JsonParserIterator
parameter_list|(
name|ObjectMapper
name|mapper
parameter_list|,
name|JavaType
name|typeRef
parameter_list|,
name|Future
argument_list|<
name|InputStream
argument_list|>
name|future
parameter_list|,
name|String
name|url
parameter_list|,
name|Query
name|query
parameter_list|)
block|{
name|this
operator|.
name|typeRef
operator|=
name|typeRef
expr_stmt|;
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
name|this
operator|.
name|url
operator|=
name|url
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
name|this
operator|.
name|mapper
operator|=
name|mapper
expr_stmt|;
name|jp
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|jp
operator|.
name|isClosed
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|END_ARRAY
condition|)
block|{
name|CloseQuietly
operator|.
name|close
argument_list|(
name|jp
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|R
name|next
parameter_list|()
block|{
try|try
block|{
specifier|final
name|R
name|retVal
init|=
name|objectCodec
operator|.
name|readValue
argument_list|(
name|jp
argument_list|,
name|typeRef
argument_list|)
decl_stmt|;
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|retVal
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|IOException
throws|,
name|ExecutionException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|jp
operator|==
literal|null
condition|)
block|{
name|InputStream
name|is
init|=
name|future
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|is
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"query[%s] url[%s] timed out"
argument_list|,
name|query
argument_list|,
name|url
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
name|jp
operator|=
name|mapper
operator|.
name|getFactory
argument_list|()
operator|.
name|createParser
argument_list|(
name|is
argument_list|)
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|AUTO_CLOSE_SOURCE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|final
name|JsonToken
name|nextToken
init|=
name|jp
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextToken
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
name|QueryInterruptedException
name|cause
init|=
name|jp
operator|.
name|getCodec
argument_list|()
operator|.
name|readValue
argument_list|(
name|jp
argument_list|,
name|QueryInterruptedException
operator|.
name|class
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|QueryInterruptedException
argument_list|(
name|cause
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|nextToken
operator|!=
name|JsonToken
operator|.
name|START_ARRAY
condition|)
block|{
throw|throw
operator|new
name|IAE
argument_list|(
literal|"Next token wasn't a START_ARRAY, was[%s] from url [%s]"
argument_list|,
name|jp
operator|.
name|getCurrentToken
argument_list|()
argument_list|,
name|url
argument_list|)
throw|;
block|}
else|else
block|{
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|objectCodec
operator|=
name|jp
operator|.
name|getCodec
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|CloseQuietly
operator|.
name|close
argument_list|(
name|jp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

