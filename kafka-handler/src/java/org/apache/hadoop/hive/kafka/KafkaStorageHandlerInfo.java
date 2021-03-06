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
name|kafka
package|;
end_package

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
name|metadata
operator|.
name|StorageHandlerInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|clients
operator|.
name|CommonClientConfigs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|clients
operator|.
name|consumer
operator|.
name|KafkaConsumer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|common
operator|.
name|PartitionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|common
operator|.
name|TopicPartition
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
comment|/**  * Kafka Storage Handler info.  */
end_comment

begin_class
class|class
name|KafkaStorageHandlerInfo
implements|implements
name|StorageHandlerInfo
block|{
specifier|private
specifier|final
name|String
name|topic
decl_stmt|;
specifier|private
specifier|final
name|Properties
name|consumerProperties
decl_stmt|;
name|KafkaStorageHandlerInfo
parameter_list|(
name|String
name|topic
parameter_list|,
name|Properties
name|consumerProperties
parameter_list|)
block|{
name|this
operator|.
name|topic
operator|=
name|topic
expr_stmt|;
name|this
operator|.
name|consumerProperties
operator|=
name|consumerProperties
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|formatAsText
parameter_list|()
block|{
try|try
init|(
name|KafkaConsumer
name|consumer
init|=
operator|new
name|KafkaConsumer
argument_list|(
name|consumerProperties
argument_list|)
block|{     }
init|)
block|{
comment|//noinspection unchecked
name|List
argument_list|<
name|PartitionInfo
argument_list|>
name|partitionsInfo
init|=
name|consumer
operator|.
name|partitionsFor
argument_list|(
name|topic
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TopicPartition
argument_list|>
name|topicPartitions
init|=
name|partitionsInfo
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|partitionInfo
lambda|->
operator|new
name|TopicPartition
argument_list|(
name|partitionInfo
operator|.
name|topic
argument_list|()
argument_list|,
name|partitionInfo
operator|.
name|partition
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|Map
name|endOffsets
init|=
name|consumer
operator|.
name|endOffsets
argument_list|(
name|topicPartitions
argument_list|)
decl_stmt|;
name|Map
name|startOffsets
init|=
name|consumer
operator|.
name|beginningOffsets
argument_list|(
name|topicPartitions
argument_list|)
decl_stmt|;
return|return
name|partitionsInfo
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|partitionInfo
lambda|->
name|String
operator|.
name|format
argument_list|(
literal|"%s [start offset = [%s], end offset = [%s]]"
argument_list|,
name|partitionInfo
operator|.
name|toString
argument_list|()
argument_list|,
name|startOffsets
operator|.
name|get
argument_list|(
operator|new
name|TopicPartition
argument_list|(
name|partitionInfo
operator|.
name|topic
argument_list|()
argument_list|,
name|partitionInfo
operator|.
name|partition
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|endOffsets
operator|.
name|get
argument_list|(
operator|new
name|TopicPartition
argument_list|(
name|partitionInfo
operator|.
name|topic
argument_list|()
argument_list|,
name|partitionInfo
operator|.
name|partition
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"ERROR fetching metadata for Topic [%s], Connection String [%s], Error [%s]"
argument_list|,
name|topic
argument_list|,
name|consumerProperties
operator|.
name|getProperty
argument_list|(
name|CommonClientConfigs
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
argument_list|)
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

