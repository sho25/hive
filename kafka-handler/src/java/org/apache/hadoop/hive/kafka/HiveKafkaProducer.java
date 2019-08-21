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
name|base
operator|.
name|Preconditions
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
name|OffsetAndMetadata
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
name|Callback
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
name|Producer
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
name|clients
operator|.
name|producer
operator|.
name|RecordMetadata
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
name|internals
operator|.
name|TransactionalRequestResult
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
name|Metric
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
name|MetricName
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
name|org
operator|.
name|apache
operator|.
name|kafka
operator|.
name|common
operator|.
name|errors
operator|.
name|ProducerFencedException
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|Duration
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
name|concurrent
operator|.
name|Future
import|;
end_import

begin_comment
comment|/**  * Kafka Producer with public methods to extract the producer state then resuming transaction in another process.  * This Producer is to be used only if you need to extract the transaction state and resume it from a different process.  * Class is mostly taken from Apache Flink Project:  * org.apache.flink.streaming.connectors.kafka.internal.FlinkKafkaProducer  *  * @param<K> key serializer class.  * @param<V> value serializer class.  */
end_comment

begin_class
class|class
name|HiveKafkaProducer
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Producer
argument_list|<
name|K
argument_list|,
name|V
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
name|HiveKafkaProducer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|KafkaProducer
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|kafkaProducer
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|String
name|transactionalId
decl_stmt|;
name|HiveKafkaProducer
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
name|transactionalId
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
name|ProducerConfig
operator|.
name|TRANSACTIONAL_ID_CONFIG
argument_list|)
expr_stmt|;
name|kafkaProducer
operator|=
operator|new
name|KafkaProducer
argument_list|<>
argument_list|(
name|properties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initTransactions
parameter_list|()
block|{
name|kafkaProducer
operator|.
name|initTransactions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beginTransaction
parameter_list|()
throws|throws
name|ProducerFencedException
block|{
name|kafkaProducer
operator|.
name|beginTransaction
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTransaction
parameter_list|()
throws|throws
name|ProducerFencedException
block|{
name|kafkaProducer
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortTransaction
parameter_list|()
throws|throws
name|ProducerFencedException
block|{
name|kafkaProducer
operator|.
name|abortTransaction
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendOffsetsToTransaction
parameter_list|(
name|Map
argument_list|<
name|TopicPartition
argument_list|,
name|OffsetAndMetadata
argument_list|>
name|offsets
parameter_list|,
name|String
name|consumerGroupId
parameter_list|)
throws|throws
name|ProducerFencedException
block|{
name|kafkaProducer
operator|.
name|sendOffsetsToTransaction
argument_list|(
name|offsets
argument_list|,
name|consumerGroupId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Future
argument_list|<
name|RecordMetadata
argument_list|>
name|send
parameter_list|(
name|ProducerRecord
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|record
parameter_list|)
block|{
return|return
name|kafkaProducer
operator|.
name|send
argument_list|(
name|record
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Future
argument_list|<
name|RecordMetadata
argument_list|>
name|send
parameter_list|(
name|ProducerRecord
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|record
parameter_list|,
name|Callback
name|callback
parameter_list|)
block|{
return|return
name|kafkaProducer
operator|.
name|send
argument_list|(
name|record
argument_list|,
name|callback
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|PartitionInfo
argument_list|>
name|partitionsFor
parameter_list|(
name|String
name|topic
parameter_list|)
block|{
return|return
name|kafkaProducer
operator|.
name|partitionsFor
argument_list|(
name|topic
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|MetricName
argument_list|,
name|?
extends|extends
name|Metric
argument_list|>
name|metrics
parameter_list|()
block|{
return|return
name|kafkaProducer
operator|.
name|metrics
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|kafkaProducer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|Duration
name|duration
parameter_list|)
block|{
name|kafkaProducer
operator|.
name|close
argument_list|(
name|duration
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
block|{
name|kafkaProducer
operator|.
name|flush
argument_list|()
expr_stmt|;
if|if
condition|(
name|transactionalId
operator|!=
literal|null
condition|)
block|{
name|flushNewPartitions
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Instead of obtaining producerId and epoch from the transaction coordinator, re-use previously obtained ones,    * so that we can resume transaction after a restart. Implementation of this method is based on    * {@link org.apache.kafka.clients.producer.KafkaProducer#initTransactions}.    */
specifier|synchronized
name|void
name|resumeTransaction
parameter_list|(
name|long
name|producerId
parameter_list|,
name|short
name|epoch
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|producerId
operator|>=
literal|0
operator|&&
name|epoch
operator|>=
literal|0
argument_list|,
literal|"Incorrect values for producerId {} and epoch {}"
argument_list|,
name|producerId
argument_list|,
name|epoch
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to resume transaction {} with producerId {} and epoch {}"
argument_list|,
name|transactionalId
argument_list|,
name|producerId
argument_list|,
name|epoch
argument_list|)
expr_stmt|;
name|Object
name|transactionManager
init|=
name|getValue
argument_list|(
name|kafkaProducer
argument_list|,
literal|"transactionManager"
argument_list|)
decl_stmt|;
name|Object
name|topicPartitionBookkeeper
init|=
name|getValue
argument_list|(
name|transactionManager
argument_list|,
literal|"topicPartitionBookkeeper"
argument_list|)
decl_stmt|;
name|invoke
argument_list|(
name|transactionManager
argument_list|,
literal|"transitionTo"
argument_list|,
name|getEnum
argument_list|(
literal|"org.apache.kafka.clients.producer.internals.TransactionManager$State.INITIALIZING"
argument_list|)
argument_list|)
expr_stmt|;
name|invoke
argument_list|(
name|topicPartitionBookkeeper
argument_list|,
literal|"reset"
argument_list|)
expr_stmt|;
name|Object
name|producerIdAndEpoch
init|=
name|getValue
argument_list|(
name|transactionManager
argument_list|,
literal|"producerIdAndEpoch"
argument_list|)
decl_stmt|;
name|setValue
argument_list|(
name|producerIdAndEpoch
argument_list|,
literal|"producerId"
argument_list|,
name|producerId
argument_list|)
expr_stmt|;
name|setValue
argument_list|(
name|producerIdAndEpoch
argument_list|,
literal|"epoch"
argument_list|,
name|epoch
argument_list|)
expr_stmt|;
name|invoke
argument_list|(
name|transactionManager
argument_list|,
literal|"transitionTo"
argument_list|,
name|getEnum
argument_list|(
literal|"org.apache.kafka.clients.producer.internals.TransactionManager$State.READY"
argument_list|)
argument_list|)
expr_stmt|;
name|invoke
argument_list|(
name|transactionManager
argument_list|,
literal|"transitionTo"
argument_list|,
name|getEnum
argument_list|(
literal|"org.apache.kafka.clients.producer.internals.TransactionManager$State.IN_TRANSACTION"
argument_list|)
argument_list|)
expr_stmt|;
name|setValue
argument_list|(
name|transactionManager
argument_list|,
literal|"transactionStarted"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Nullable
name|String
name|getTransactionalId
parameter_list|()
block|{
return|return
name|transactionalId
return|;
block|}
name|long
name|getProducerId
parameter_list|()
block|{
name|Object
name|transactionManager
init|=
name|getValue
argument_list|(
name|kafkaProducer
argument_list|,
literal|"transactionManager"
argument_list|)
decl_stmt|;
name|Object
name|producerIdAndEpoch
init|=
name|getValue
argument_list|(
name|transactionManager
argument_list|,
literal|"producerIdAndEpoch"
argument_list|)
decl_stmt|;
return|return
operator|(
name|long
operator|)
name|getValue
argument_list|(
name|producerIdAndEpoch
argument_list|,
literal|"producerId"
argument_list|)
return|;
block|}
name|short
name|getEpoch
parameter_list|()
block|{
name|Object
name|transactionManager
init|=
name|getValue
argument_list|(
name|kafkaProducer
argument_list|,
literal|"transactionManager"
argument_list|)
decl_stmt|;
name|Object
name|producerIdAndEpoch
init|=
name|getValue
argument_list|(
name|transactionManager
argument_list|,
literal|"producerIdAndEpoch"
argument_list|)
decl_stmt|;
return|return
operator|(
name|short
operator|)
name|getValue
argument_list|(
name|producerIdAndEpoch
argument_list|,
literal|"epoch"
argument_list|)
return|;
block|}
comment|/**    * Besides committing {@link org.apache.kafka.clients.producer.KafkaProducer#commitTransaction} is also adding new    * partitions to the transaction. flushNewPartitions method is moving this logic to pre-commit/flush, to make    * resumeTransaction simpler.    * Otherwise resumeTransaction would require to restore state of the not yet added/"in-flight" partitions.    */
specifier|private
name|void
name|flushNewPartitions
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing new partitions"
argument_list|)
expr_stmt|;
name|TransactionalRequestResult
name|result
init|=
name|enqueueNewPartitions
argument_list|()
decl_stmt|;
name|Object
name|sender
init|=
name|getValue
argument_list|(
name|kafkaProducer
argument_list|,
literal|"sender"
argument_list|)
decl_stmt|;
name|invoke
argument_list|(
name|sender
argument_list|,
literal|"wakeup"
argument_list|)
expr_stmt|;
name|result
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|TransactionalRequestResult
name|enqueueNewPartitions
parameter_list|()
block|{
name|Object
name|transactionManager
init|=
name|getValue
argument_list|(
name|kafkaProducer
argument_list|,
literal|"transactionManager"
argument_list|)
decl_stmt|;
name|Object
name|txnRequestHandler
init|=
name|invoke
argument_list|(
name|transactionManager
argument_list|,
literal|"addPartitionsToTransactionHandler"
argument_list|)
decl_stmt|;
name|invoke
argument_list|(
name|transactionManager
argument_list|,
literal|"enqueueRequest"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|txnRequestHandler
operator|.
name|getClass
argument_list|()
operator|.
name|getSuperclass
argument_list|()
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|txnRequestHandler
block|}
argument_list|)
expr_stmt|;
return|return
operator|(
name|TransactionalRequestResult
operator|)
name|getValue
argument_list|(
name|txnRequestHandler
argument_list|,
name|txnRequestHandler
operator|.
name|getClass
argument_list|()
operator|.
name|getSuperclass
argument_list|()
argument_list|,
literal|"result"
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Enum
argument_list|<
name|?
argument_list|>
name|getEnum
parameter_list|(
name|String
name|enumFullName
parameter_list|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"RegExpRedundantEscape"
argument_list|)
name|String
index|[]
name|x
init|=
name|enumFullName
operator|.
name|split
argument_list|(
literal|"\\.(?=[^\\.]+$)"
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|String
name|enumClassName
init|=
name|x
index|[
literal|0
index|]
decl_stmt|;
name|String
name|enumName
init|=
name|x
index|[
literal|1
index|]
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|Enum
argument_list|>
name|cl
init|=
operator|(
name|Class
argument_list|<
name|Enum
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|enumClassName
argument_list|)
decl_stmt|;
return|return
name|Enum
operator|.
name|valueOf
argument_list|(
name|cl
argument_list|,
name|enumName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incompatible KafkaProducer version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|Object
name|invoke
parameter_list|(
name|Object
name|object
parameter_list|,
name|String
name|methodName
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|argTypes
init|=
operator|new
name|Class
index|[
name|args
operator|.
name|length
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
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|argTypes
index|[
name|i
index|]
operator|=
name|args
index|[
name|i
index|]
operator|.
name|getClass
argument_list|()
expr_stmt|;
block|}
return|return
name|invoke
argument_list|(
name|object
argument_list|,
name|methodName
argument_list|,
name|argTypes
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Object
name|invoke
parameter_list|(
name|Object
name|object
parameter_list|,
name|String
name|methodName
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|argTypes
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
block|{
try|try
block|{
name|Method
name|method
init|=
name|object
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
name|methodName
argument_list|,
name|argTypes
argument_list|)
decl_stmt|;
name|method
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|method
operator|.
name|invoke
argument_list|(
name|object
argument_list|,
name|args
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|InvocationTargetException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incompatible KafkaProducer version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|Object
name|getValue
parameter_list|(
name|Object
name|object
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|getValue
argument_list|(
name|object
argument_list|,
name|object
operator|.
name|getClass
argument_list|()
argument_list|,
name|fieldName
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Object
name|getValue
parameter_list|(
name|Object
name|object
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
try|try
block|{
name|Field
name|field
init|=
name|clazz
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|field
operator|.
name|get
argument_list|(
name|object
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incompatible KafkaProducer version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|setValue
parameter_list|(
name|Object
name|object
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
try|try
block|{
name|Field
name|field
init|=
name|object
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field
operator|.
name|set
argument_list|(
name|object
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incompatible KafkaProducer version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

