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
name|io
operator|.
name|NullWritable
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
name|topn
operator|.
name|DimensionAndMetricValueExtractor
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
name|topn
operator|.
name|TopNQuery
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
name|topn
operator|.
name|TopNResultValue
import|;
end_import

begin_comment
comment|/**  * Record reader for results for Druid TopNQuery.  */
end_comment

begin_class
specifier|public
class|class
name|DruidTopNQueryRecordReader
extends|extends
name|DruidQueryRecordReader
argument_list|<
name|TopNQuery
argument_list|,
name|Result
argument_list|<
name|TopNResultValue
argument_list|>
argument_list|>
block|{
specifier|private
name|Result
argument_list|<
name|TopNResultValue
argument_list|>
name|current
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|DimensionAndMetricValueExtractor
argument_list|>
name|values
init|=
name|Iterators
operator|.
name|emptyIterator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|TopNQuery
name|createQuery
parameter_list|(
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|content
argument_list|,
name|TopNQuery
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Result
argument_list|<
name|TopNResultValue
argument_list|>
argument_list|>
name|createResultsList
parameter_list|(
name|InputStream
name|content
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DruidStorageHandlerUtils
operator|.
name|SMILE_MAPPER
operator|.
name|readValue
argument_list|(
name|content
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|Result
argument_list|<
name|TopNResultValue
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
block|{
if|if
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|results
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|results
operator|.
name|next
argument_list|()
expr_stmt|;
name|values
operator|=
name|current
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
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
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Create new value
name|DruidWritable
name|value
init|=
operator|new
name|DruidWritable
argument_list|()
decl_stmt|;
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|put
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|current
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|putAll
argument_list|(
name|values
operator|.
name|next
argument_list|()
operator|.
name|getBaseObject
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|value
return|;
block|}
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|DruidWritable
name|value
parameter_list|)
block|{
if|if
condition|(
name|nextKeyValue
argument_list|()
condition|)
block|{
comment|// Update value
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|put
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|current
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|putAll
argument_list|(
name|values
operator|.
name|next
argument_list|()
operator|.
name|getBaseObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
return|return
name|results
operator|.
name|hasNext
argument_list|()
operator|||
name|values
operator|.
name|hasNext
argument_list|()
condition|?
literal|0
else|:
literal|1
return|;
block|}
block|}
end_class

end_unit

