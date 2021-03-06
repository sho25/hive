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
name|TaskAttemptID
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
name|task
operator|.
name|TaskAttemptContextImpl
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
name|ConsumerConfig
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
name|ConsumerRecord
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
name|clients
operator|.
name|producer
operator|.
name|KafkaProducer
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
name|producer
operator|.
name|ProducerConfig
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
name|producer
operator|.
name|ProducerRecord
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
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|common
operator|.
name|serialization
operator|.
name|ByteArrayDeserializer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
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
name|UUID
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_comment
comment|/**  * Kafka Iterator Tests.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|KafkaRecordIteratorTest
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
name|KafkaRecordIteratorTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_NUMBER
init|=
literal|19384
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TOPIC
init|=
literal|"my_test_topic"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TX_TOPIC
init|=
literal|"tx_test_topic"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|KEY_BYTES
init|=
literal|"KEY"
operator|.
name|getBytes
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KafkaBrokerResource
name|BROKER_RESOURCE
init|=
operator|new
name|KafkaBrokerResource
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|RECORDS
init|=
name|getRecords
argument_list|(
name|TOPIC
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|TX_RECORDS
init|=
name|getRecords
argument_list|(
name|TX_TOPIC
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|POLL_TIMEOUT_MS
init|=
literal|900L
decl_stmt|;
specifier|private
specifier|static
name|KafkaProducer
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|producer
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
name|TOPIC
block|,
literal|true
block|,
name|RECORDS
block|}
block|,
block|{
name|TX_TOPIC
block|,
literal|false
block|,
name|TX_RECORDS
block|}
block|}
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|getRecords
parameter_list|(
name|String
name|topic
parameter_list|)
block|{
return|return
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|RECORD_NUMBER
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|number
lambda|->
block|{
specifier|final
name|byte
index|[]
name|value
init|=
operator|(
literal|"VALUE-"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|number
argument_list|)
operator|)
operator|.
name|getBytes
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ConsumerRecord
argument_list|<>
argument_list|(
name|topic
argument_list|,
literal|0
argument_list|,
operator|(
name|long
operator|)
name|number
argument_list|,
literal|0L
argument_list|,
literal|null
argument_list|,
literal|0L
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|KEY_BYTES
argument_list|,
name|value
argument_list|)
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|String
name|currentTopic
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|readUncommitted
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|expectedRecords
decl_stmt|;
specifier|private
specifier|final
name|TopicPartition
name|topicPartition
decl_stmt|;
specifier|private
name|KafkaConsumer
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|consumer
init|=
literal|null
decl_stmt|;
specifier|private
name|KafkaRecordIterator
name|kafkaRecordIterator
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|public
name|KafkaRecordIteratorTest
parameter_list|(
name|String
name|currentTopic
parameter_list|,
name|boolean
name|readUncommitted
parameter_list|,
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|expectedRecords
parameter_list|)
block|{
name|this
operator|.
name|currentTopic
operator|=
name|currentTopic
expr_stmt|;
comment|// when true means the the topic is not Transactional topic
name|this
operator|.
name|readUncommitted
operator|=
name|readUncommitted
expr_stmt|;
name|this
operator|.
name|expectedRecords
operator|=
name|expectedRecords
expr_stmt|;
name|this
operator|.
name|topicPartition
operator|=
operator|new
name|TopicPartition
argument_list|(
name|currentTopic
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Throwable
block|{
name|BROKER_RESOURCE
operator|.
name|before
argument_list|()
expr_stmt|;
name|sendData
argument_list|(
name|RECORDS
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|sendData
argument_list|(
name|TX_RECORDS
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"setting up consumer"
argument_list|)
expr_stmt|;
name|setupConsumer
argument_list|()
expr_stmt|;
name|this
operator|.
name|kafkaRecordIterator
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextAbsoluteStartEnd
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|0L
argument_list|,
operator|(
name|long
operator|)
name|expectedRecords
operator|.
name|size
argument_list|()
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextGivenStartEnd
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|2L
argument_list|,
literal|4L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
literal|2L
operator|&&
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|<
literal|4L
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextNoOffsets
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextLastRecord
parameter_list|()
block|{
name|long
name|startOffset
init|=
call|(
name|long
call|)
argument_list|(
name|expectedRecords
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|long
name|lastOffset
init|=
operator|(
name|long
operator|)
name|expectedRecords
operator|.
name|size
argument_list|()
decl_stmt|;
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
name|startOffset
argument_list|,
name|lastOffset
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
name|startOffset
operator|&&
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|<
name|lastOffset
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextFirstRecord
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|0L
argument_list|,
literal|1L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
literal|0L
operator|&&
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|<
literal|1L
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextNoStart
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|null
argument_list|,
literal|10L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
literal|0L
operator|&&
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|<
literal|10L
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNextNoEnd
parameter_list|()
block|{
name|long
name|lastOffset
init|=
operator|(
name|long
operator|)
name|expectedRecords
operator|.
name|size
argument_list|()
decl_stmt|;
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|5L
argument_list|,
literal|null
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
literal|5L
operator|&&
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|<
name|lastOffset
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRecordReader
parameter_list|()
block|{
name|List
argument_list|<
name|KafkaWritable
argument_list|>
name|serRecords
init|=
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
operator|new
name|KafkaWritable
argument_list|(
name|consumerRecord
operator|.
name|partition
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|offset
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|timestamp
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|value
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|key
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
name|KafkaRecordReader
name|recordReader
init|=
operator|new
name|KafkaRecordReader
argument_list|()
decl_stmt|;
name|TaskAttemptContext
name|context
init|=
operator|new
name|TaskAttemptContextImpl
argument_list|(
name|this
operator|.
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|recordReader
operator|.
name|initialize
argument_list|(
operator|new
name|KafkaInputSplit
argument_list|(
name|currentTopic
argument_list|,
literal|0
argument_list|,
literal|50L
argument_list|,
literal|100L
argument_list|,
literal|null
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|50
init|;
name|i
operator|<
literal|100
condition|;
operator|++
name|i
control|)
block|{
name|KafkaWritable
name|record
init|=
operator|new
name|KafkaWritable
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|recordReader
operator|.
name|next
argument_list|(
literal|null
argument_list|,
name|record
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serRecords
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|KafkaRecordIterator
operator|.
name|PollTimeoutException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testPullingBeyondLimit
parameter_list|()
block|{
comment|//FYI In the Tx world Commits can introduce offset gaps therefore
comment|//thus(RECORD_NUMBER + 1) as beyond limit offset is only true if the topic has not Tx or any Control msg.
name|long
name|increment
init|=
name|readUncommitted
condition|?
literal|1
else|:
literal|2
decl_stmt|;
name|long
name|requestedEnd
init|=
name|expectedRecords
operator|.
name|size
argument_list|()
operator|+
name|increment
decl_stmt|;
name|long
name|requestedStart
init|=
name|expectedRecords
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
name|requestedStart
argument_list|,
name|requestedEnd
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|consumerRecord
parameter_list|)
lambda|->
name|consumerRecord
operator|.
name|offset
argument_list|()
operator|>=
name|requestedStart
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalStateException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testPullingStartGreaterThanEnd
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|10L
argument_list|,
literal|1L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|KafkaRecordIterator
operator|.
name|PollTimeoutException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testPullingFromEmptyTopic
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
operator|new
name|TopicPartition
argument_list|(
literal|"noHere"
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|0L
argument_list|,
literal|100L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|KafkaRecordIterator
operator|.
name|PollTimeoutException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testPullingFromEmptyPartition
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
operator|new
name|TopicPartition
argument_list|(
name|currentTopic
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|0L
argument_list|,
literal|100L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|expectedRecords
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartIsEqualEnd
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|10L
argument_list|,
literal|10L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartIsTheLastOffset
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
operator|(
name|long
operator|)
name|expectedRecords
operator|.
name|size
argument_list|()
argument_list|,
operator|(
name|long
operator|)
name|expectedRecords
operator|.
name|size
argument_list|()
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartIsTheFirstOffset
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
operator|new
name|KafkaRecordIterator
argument_list|(
name|this
operator|.
name|consumer
argument_list|,
name|topicPartition
argument_list|,
literal|0L
argument_list|,
literal|0L
argument_list|,
name|POLL_TIMEOUT_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|compareIterator
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|()
argument_list|,
name|this
operator|.
name|kafkaRecordIterator
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|compareIterator
parameter_list|(
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|expected
parameter_list|,
name|Iterator
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|kafkaRecordIterator
parameter_list|)
block|{
name|expected
operator|.
name|forEach
argument_list|(
parameter_list|(
name|expectedRecord
parameter_list|)
lambda|->
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Record with offset is missing"
operator|+
name|expectedRecord
operator|.
name|offset
argument_list|()
argument_list|,
name|kafkaRecordIterator
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|ConsumerRecord
name|record
init|=
name|kafkaRecordIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedRecord
operator|.
name|topic
argument_list|()
argument_list|,
name|record
operator|.
name|topic
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedRecord
operator|.
name|partition
argument_list|()
argument_list|,
name|record
operator|.
name|partition
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Offsets not matching"
argument_list|,
name|expectedRecord
operator|.
name|offset
argument_list|()
argument_list|,
name|record
operator|.
name|offset
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|binaryExceptedValue
init|=
name|expectedRecord
operator|.
name|value
argument_list|()
decl_stmt|;
name|byte
index|[]
name|binaryExceptedKey
init|=
name|expectedRecord
operator|.
name|key
argument_list|()
decl_stmt|;
name|byte
index|[]
name|binaryValue
init|=
operator|(
name|byte
index|[]
operator|)
name|record
operator|.
name|value
argument_list|()
decl_stmt|;
name|byte
index|[]
name|binaryKey
init|=
operator|(
name|byte
index|[]
operator|)
name|record
operator|.
name|key
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
literal|"Values not matching"
argument_list|,
name|binaryExceptedValue
argument_list|,
name|binaryValue
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
literal|"Keys not matching"
argument_list|,
name|binaryExceptedKey
argument_list|,
name|binaryKey
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|kafkaRecordIterator
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupConsumer
parameter_list|()
block|{
name|Properties
name|consumerProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"enable.auto.commit"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"auto.offset.reset"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"bootstrap.servers"
argument_list|,
name|KafkaBrokerResource
operator|.
name|BROKER_IP_PORT
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"kafka.bootstrap.servers"
argument_list|,
name|KafkaBrokerResource
operator|.
name|BROKER_IP_PORT
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|KafkaTableProperties
operator|.
name|KAFKA_POLL_TIMEOUT
operator|.
name|getName
argument_list|()
argument_list|,
name|KafkaTableProperties
operator|.
name|KAFKA_POLL_TIMEOUT
operator|.
name|getDefaultValue
argument_list|()
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"key.deserializer"
argument_list|,
name|ByteArrayDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"value.deserializer"
argument_list|,
name|ByteArrayDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"request.timeout.ms"
argument_list|,
literal|"3002"
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"fetch.max.wait.ms"
argument_list|,
literal|"3001"
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"session.timeout.ms"
argument_list|,
literal|"3001"
argument_list|)
expr_stmt|;
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"metadata.max.age.ms"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|readUncommitted
condition|)
block|{
name|consumerProps
operator|.
name|setProperty
argument_list|(
name|ConsumerConfig
operator|.
name|ISOLATION_LEVEL_CONFIG
argument_list|,
literal|"read_committed"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"kafka.consumer.isolation.level"
argument_list|,
literal|"read_committed"
argument_list|)
expr_stmt|;
block|}
name|consumerProps
operator|.
name|setProperty
argument_list|(
literal|"max.poll.records"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|RECORD_NUMBER
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|consumer
operator|=
operator|new
name|KafkaConsumer
argument_list|<>
argument_list|(
name|consumerProps
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|sendData
parameter_list|(
name|List
argument_list|<
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|recordList
parameter_list|,
annotation|@
name|Nullable
name|String
name|txId
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting up kafka producer"
argument_list|)
expr_stmt|;
name|Properties
name|producerProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|producerProps
operator|.
name|setProperty
argument_list|(
literal|"bootstrap.servers"
argument_list|,
name|KafkaBrokerResource
operator|.
name|BROKER_IP_PORT
argument_list|)
expr_stmt|;
name|producerProps
operator|.
name|setProperty
argument_list|(
literal|"key.serializer"
argument_list|,
literal|"org.apache.kafka.common.serialization.ByteArraySerializer"
argument_list|)
expr_stmt|;
name|producerProps
operator|.
name|setProperty
argument_list|(
literal|"value.serializer"
argument_list|,
literal|"org.apache.kafka.common.serialization.ByteArraySerializer"
argument_list|)
expr_stmt|;
name|producerProps
operator|.
name|setProperty
argument_list|(
literal|"max.block.ms"
argument_list|,
literal|"10000"
argument_list|)
expr_stmt|;
if|if
condition|(
name|txId
operator|!=
literal|null
condition|)
block|{
name|producerProps
operator|.
name|setProperty
argument_list|(
name|ProducerConfig
operator|.
name|TRANSACTIONAL_ID_CONFIG
argument_list|,
name|txId
argument_list|)
expr_stmt|;
block|}
name|producer
operator|=
operator|new
name|KafkaProducer
argument_list|<>
argument_list|(
name|producerProps
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"kafka producer started"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sending [{}] records"
argument_list|,
name|RECORDS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|txId
operator|!=
literal|null
condition|)
block|{
name|producer
operator|.
name|initTransactions
argument_list|()
expr_stmt|;
name|producer
operator|.
name|beginTransaction
argument_list|()
expr_stmt|;
block|}
name|recordList
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|consumerRecord
lambda|->
operator|new
name|ProducerRecord
argument_list|<>
argument_list|(
name|consumerRecord
operator|.
name|topic
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|partition
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|timestamp
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|key
argument_list|()
argument_list|,
name|consumerRecord
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|producerRecord
lambda|->
name|producer
operator|.
name|send
argument_list|(
name|producerRecord
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|txId
operator|!=
literal|null
condition|)
block|{
name|producer
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
name|producer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|this
operator|.
name|kafkaRecordIterator
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|consumer
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|consumer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownCluster
parameter_list|()
block|{
name|BROKER_RESOURCE
operator|.
name|after
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

