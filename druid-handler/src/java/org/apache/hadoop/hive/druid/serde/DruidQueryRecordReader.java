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
operator|.
name|serde
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
name|collect
operator|.
name|Iterators
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
name|BaseQuery
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
name|joda
operator|.
name|time
operator|.
name|Period
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
name|List
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
name|T
extends|extends
name|BaseQuery
parameter_list|<
name|R
parameter_list|>
parameter_list|,
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
name|T
name|query
decl_stmt|;
comment|/**    * Query results.    */
specifier|protected
name|Iterator
argument_list|<
name|R
argument_list|>
name|results
init|=
name|Iterators
operator|.
name|emptyIterator
argument_list|()
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
name|Configuration
name|conf
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
comment|// Create query
name|query
operator|=
name|createQuery
argument_list|(
name|hiveDruidSplit
operator|.
name|getDruidQuery
argument_list|()
argument_list|)
expr_stmt|;
comment|// Execute query
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Retrieving from druid using query:\n "
operator|+
name|query
argument_list|)
expr_stmt|;
block|}
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
name|DruidStorageHandlerUtils
operator|.
name|createRequest
argument_list|(
name|hiveDruidSplit
operator|.
name|getLocations
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|query
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
name|R
argument_list|>
name|resultsList
decl_stmt|;
try|try
block|{
name|resultsList
operator|=
name|createResultsList
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|response
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
if|if
condition|(
name|resultsList
operator|==
literal|null
operator|||
name|resultsList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|results
operator|=
name|resultsList
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
name|createQuery
parameter_list|(
name|String
name|content
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|R
argument_list|>
name|createResultsList
parameter_list|(
name|InputStream
name|content
parameter_list|)
throws|throws
name|IOException
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
argument_list|()
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
return|return
literal|0
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
comment|// TODO: we could generate vector row batches so that vectorized execution may get triggered
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
comment|// Nothing to do
block|}
block|}
end_class

end_unit

