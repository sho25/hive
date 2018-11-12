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
name|JavaType
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
name|MapBasedRow
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
name|Row
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
name|io
operator|.
name|NullWritable
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Record reader for results for Druid GroupByQuery.  */
end_comment

begin_class
specifier|public
class|class
name|DruidGroupByQueryRecordReader
extends|extends
name|DruidQueryRecordReader
argument_list|<
name|Row
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|TypeReference
argument_list|<
name|Row
argument_list|>
name|TYPE_REFERENCE
init|=
operator|new
name|TypeReference
argument_list|<
name|Row
argument_list|>
argument_list|()
block|{   }
decl_stmt|;
specifier|private
name|MapBasedRow
name|currentRow
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|currentEvent
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|JavaType
name|getResultTypeDef
parameter_list|()
block|{
return|return
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|getTypeFactory
argument_list|()
operator|.
name|constructType
argument_list|(
name|TYPE_REFERENCE
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
comment|// Results
if|if
condition|(
name|queryResultsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|Row
name|row
init|=
name|queryResultsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// currently druid supports only MapBasedRow as Jackson SerDe so it should safe to cast without check
name|currentRow
operator|=
operator|(
name|MapBasedRow
operator|)
name|row
expr_stmt|;
name|currentEvent
operator|=
name|currentRow
operator|.
name|getEvent
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
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// 1) The timestamp column
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|put
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|EVENT_TIMESTAMP_COLUMN
argument_list|,
name|currentRow
operator|.
name|getTimestamp
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|currentRow
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
comment|// 2) The dimension columns
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|putAll
argument_list|(
name|currentEvent
argument_list|)
expr_stmt|;
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
comment|// 1) The timestamp column
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|put
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|EVENT_TIMESTAMP_COLUMN
argument_list|,
name|currentRow
operator|.
name|getTimestamp
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|currentRow
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
comment|// 2) The dimension columns
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|putAll
argument_list|(
name|currentEvent
argument_list|)
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
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|queryResultsIterator
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

