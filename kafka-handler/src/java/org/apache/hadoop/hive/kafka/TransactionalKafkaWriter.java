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
name|annotations
operator|.
name|VisibleForTesting
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
name|collect
operator|.
name|ImmutableMap
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
name|tuple
operator|.
name|Pair
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
name|FSDataInputStream
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
name|FSDataOutputStream
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
name|FileStatus
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
name|FileSystem
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
name|ql
operator|.
name|exec
operator|.
name|FileSinkOperator
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
name|BytesWritable
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
name|Writable
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
name|RecordWriter
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
name|common
operator|.
name|KafkaException
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
name|AuthenticationException
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
name|OutOfOrderSequenceException
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
name|apache
operator|.
name|kafka
operator|.
name|common
operator|.
name|errors
operator|.
name|TimeoutException
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
name|ByteArraySerializer
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
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|Set
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
name|TimeUnit
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
name|atomic
operator|.
name|AtomicReference
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
comment|/**  * Transactional Kafka Record Writer used to achieve Exactly once semantic.  */
end_comment

begin_class
class|class
name|TransactionalKafkaWriter
implements|implements
name|FileSinkOperator
operator|.
name|RecordWriter
implements|,
name|RecordWriter
argument_list|<
name|BytesWritable
argument_list|,
name|KafkaWritable
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
name|TransactionalKafkaWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TRANSACTION_DIR
init|=
literal|"transaction_states"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Duration
name|DURATION_0
init|=
name|Duration
operator|.
name|ofMillis
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|topic
decl_stmt|;
specifier|private
specifier|final
name|HiveKafkaProducer
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|producer
decl_stmt|;
specifier|private
specifier|final
name|Callback
name|callback
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|sendExceptionRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Path
name|openTxFileName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|optimisticCommit
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fileSystem
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TopicPartition
argument_list|,
name|Long
argument_list|>
name|offsets
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|writerIdTopicId
decl_stmt|;
specifier|private
specifier|final
name|long
name|producerId
decl_stmt|;
specifier|private
specifier|final
name|short
name|producerEpoch
decl_stmt|;
specifier|private
name|long
name|sentRecords
init|=
literal|0L
decl_stmt|;
comment|/**    *  @param topic Kafka topic.    * @param producerProperties kafka producer properties.    * @param queryWorkingPath the Query working directory as, table_directory/hive_query_id.  *                         Used to store the state of the transaction and/or log sent records and partitions.  *                         for more information see:  *                         {@link KafkaStorageHandler#getQueryWorkingDir(org.apache.hadoop.hive.metastore.api.Table)}    * @param fileSystem file system handler.    * @param optimisticCommit if true the commit will happen at the task level otherwise will be delegated to HS2.    */
name|TransactionalKafkaWriter
parameter_list|(
name|String
name|topic
parameter_list|,
name|Properties
name|producerProperties
parameter_list|,
name|Path
name|queryWorkingPath
parameter_list|,
name|FileSystem
name|fileSystem
parameter_list|,
annotation|@
name|Nullable
name|Boolean
name|optimisticCommit
parameter_list|)
block|{
name|this
operator|.
name|fileSystem
operator|=
name|fileSystem
expr_stmt|;
name|this
operator|.
name|topic
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|topic
argument_list|,
literal|"NULL topic !!"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|producerProperties
operator|.
name|getProperty
argument_list|(
name|ProducerConfig
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
argument_list|)
operator|!=
literal|null
argument_list|,
literal|"set ["
operator|+
name|ProducerConfig
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
operator|+
literal|"] property"
argument_list|)
expr_stmt|;
name|producerProperties
operator|.
name|setProperty
argument_list|(
name|ProducerConfig
operator|.
name|VALUE_SERIALIZER_CLASS_CONFIG
argument_list|,
name|ByteArraySerializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|producerProperties
operator|.
name|setProperty
argument_list|(
name|ProducerConfig
operator|.
name|KEY_SERIALIZER_CLASS_CONFIG
argument_list|,
name|ByteArraySerializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|producer
operator|=
operator|new
name|HiveKafkaProducer
argument_list|<>
argument_list|(
name|producerProperties
argument_list|)
expr_stmt|;
name|this
operator|.
name|optimisticCommit
operator|=
name|optimisticCommit
operator|==
literal|null
condition|?
literal|true
else|:
name|optimisticCommit
expr_stmt|;
name|this
operator|.
name|callback
operator|=
parameter_list|(
name|metadata
parameter_list|,
name|exception
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
name|sendExceptionRef
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//According to https://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
comment|//Callbacks form the same TopicPartition will return in order thus this will keep track of most recent offset.
specifier|final
name|TopicPartition
name|tp
init|=
operator|new
name|TopicPartition
argument_list|(
name|metadata
operator|.
name|topic
argument_list|()
argument_list|,
name|metadata
operator|.
name|partition
argument_list|()
argument_list|)
decl_stmt|;
name|offsets
operator|.
name|put
argument_list|(
name|tp
argument_list|,
name|metadata
operator|.
name|offset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
comment|// Start Tx
assert|assert
name|producer
operator|.
name|getTransactionalId
argument_list|()
operator|!=
literal|null
assert|;
try|try
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
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|logHints
argument_list|(
name|exception
argument_list|)
expr_stmt|;
if|if
condition|(
name|tryToAbortTx
argument_list|(
name|exception
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Aborting Transaction [{}] cause by ERROR [{}]"
argument_list|,
name|producer
operator|.
name|getTransactionalId
argument_list|()
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|producer
operator|.
name|abortTransaction
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Closing writer [{}] caused by ERROR [{}]"
argument_list|,
name|producer
operator|.
name|getTransactionalId
argument_list|()
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|producer
operator|.
name|close
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
throw|throw
name|exception
throw|;
block|}
name|writerIdTopicId
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"WriterId [%s], Kafka Topic [%s]"
argument_list|,
name|producer
operator|.
name|getTransactionalId
argument_list|()
argument_list|,
name|topic
argument_list|)
expr_stmt|;
name|producerEpoch
operator|=
name|this
operator|.
name|optimisticCommit
condition|?
operator|-
literal|1
else|:
name|producer
operator|.
name|getEpoch
argument_list|()
expr_stmt|;
name|producerId
operator|=
name|this
operator|.
name|optimisticCommit
condition|?
operator|-
literal|1
else|:
name|producer
operator|.
name|getProducerId
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"DONE with Initialization of {}, Epoch[{}], internal ID[{}]"
argument_list|,
name|writerIdTopicId
argument_list|,
name|producerEpoch
argument_list|,
name|producerId
argument_list|)
expr_stmt|;
comment|//Writer base working directory
name|openTxFileName
operator|=
name|this
operator|.
name|optimisticCommit
condition|?
literal|null
else|:
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|queryWorkingPath
argument_list|,
name|TRANSACTION_DIR
argument_list|)
argument_list|,
name|producer
operator|.
name|getTransactionalId
argument_list|()
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|producerEpoch
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
name|checkExceptions
argument_list|()
expr_stmt|;
try|try
block|{
name|sentRecords
operator|++
expr_stmt|;
name|producer
operator|.
name|send
argument_list|(
name|KafkaUtils
operator|.
name|toProducerRecord
argument_list|(
name|topic
argument_list|,
operator|(
name|KafkaWritable
operator|)
name|w
argument_list|)
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|tryToAbortTx
argument_list|(
name|e
argument_list|)
condition|)
block|{
comment|// producer.send() may throw a KafkaException which wraps a FencedException re throw its wrapped inner cause.
name|producer
operator|.
name|abortTransaction
argument_list|()
expr_stmt|;
block|}
name|producer
operator|.
name|close
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|sendExceptionRef
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|checkExceptions
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logHints
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|TimeoutException
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Maybe Try to increase [`retry.backoff.ms`] to avoid this error [{}]."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * The non Abort Close method can be split into 2 parts.    * Part one is to Flush to Kafka all the buffered Records then Log (Topic-Partition, Offset).    * Part two is To either commit the TX or Save the state of the TX to WAL and let HS2 do the commit.    *    * @param abort if set to true will abort flush and exit    * @throws IOException exception causing the failure    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|abort
condition|)
block|{
comment|// Case Abort, try to AbortTransaction -> Close producer ASAP -> Exit;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Aborting Transaction and Sending from {}"
argument_list|,
name|writerIdTopicId
argument_list|)
expr_stmt|;
try|try
block|{
name|producer
operator|.
name|abortTransaction
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Aborting Transaction {} failed due to [{}]"
argument_list|,
name|writerIdTopicId
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|producer
operator|.
name|close
argument_list|(
name|DURATION_0
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Normal Case ->  lOG and Commit then Close
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing Kafka buffer of writerId {}"
argument_list|,
name|writerIdTopicId
argument_list|)
expr_stmt|;
name|producer
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// No exception good let's log to a file whatever Flushed.
name|String
name|formattedMsg
init|=
literal|"Topic[%s] Partition [%s] -> Last offset [%s]"
decl_stmt|;
name|String
name|flushedOffsetMsg
init|=
name|offsets
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|topicPartitionLongEntry
lambda|->
name|String
operator|.
name|format
argument_list|(
name|formattedMsg
argument_list|,
name|topicPartitionLongEntry
operator|.
name|getKey
argument_list|()
operator|.
name|topic
argument_list|()
argument_list|,
name|topicPartitionLongEntry
operator|.
name|getKey
argument_list|()
operator|.
name|partition
argument_list|()
argument_list|,
name|topicPartitionLongEntry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"WriterId {} flushed the following [{}] "
argument_list|,
name|writerIdTopicId
argument_list|,
name|flushedOffsetMsg
argument_list|)
expr_stmt|;
comment|// OPTIMISTIC COMMIT OR PERSIST STATE OF THE TX_WAL
name|checkExceptions
argument_list|()
expr_stmt|;
if|if
condition|(
name|optimisticCommit
condition|)
block|{
comment|// Case Commit at the task level
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Case delegate TX commit to HS2
name|persistTxState
argument_list|()
expr_stmt|;
block|}
name|checkExceptions
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Closed writerId [{}], Sent [{}] records to Topic [{}]"
argument_list|,
name|producer
operator|.
name|getTransactionalId
argument_list|()
argument_list|,
name|sentRecords
argument_list|,
name|topic
argument_list|)
expr_stmt|;
name|producer
operator|.
name|close
argument_list|(
name|Duration
operator|.
name|ZERO
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|commitTransaction
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting Optimistic commit by {}"
argument_list|,
name|writerIdTopicId
argument_list|)
expr_stmt|;
try|try
block|{
name|producer
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|sendExceptionRef
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write the Kafka Consumer PID and Epoch to checkpoint file {@link TransactionalKafkaWriter#openTxFileName}.    */
specifier|private
name|void
name|persistTxState
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Committing state to path [{}] by [{}]"
argument_list|,
name|openTxFileName
operator|.
name|toString
argument_list|()
argument_list|,
name|writerIdTopicId
argument_list|)
expr_stmt|;
try|try
init|(
name|FSDataOutputStream
name|outStream
init|=
name|fileSystem
operator|.
name|create
argument_list|(
name|openTxFileName
argument_list|)
init|)
block|{
name|outStream
operator|.
name|writeLong
argument_list|(
name|producerId
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|writeShort
argument_list|(
name|producerEpoch
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|sendExceptionRef
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|BytesWritable
name|bytesWritable
parameter_list|,
name|KafkaWritable
name|kafkaWritable
parameter_list|)
throws|throws
name|IOException
block|{
name|write
argument_list|(
name|kafkaWritable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getSentRecords
parameter_list|()
block|{
return|return
name|sentRecords
return|;
block|}
annotation|@
name|VisibleForTesting
name|short
name|getProducerEpoch
parameter_list|()
block|{
return|return
name|producerEpoch
return|;
block|}
annotation|@
name|VisibleForTesting
name|long
name|getProducerId
parameter_list|()
block|{
return|return
name|producerId
return|;
block|}
comment|/**    * Checks for existing exception. In case of exception will close consumer and rethrow as IOException    * @throws IOException abort if possible, close consumer then rethrow exception.    */
specifier|private
name|void
name|checkExceptions
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|sendExceptionRef
operator|.
name|get
argument_list|()
operator|!=
literal|null
operator|&&
name|sendExceptionRef
operator|.
name|get
argument_list|()
operator|instanceof
name|KafkaException
operator|&&
name|sendExceptionRef
operator|.
name|get
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ProducerFencedException
condition|)
block|{
comment|// producer.send() may throw a KafkaException which wraps a FencedException re throw its wrapped inner cause.
name|sendExceptionRef
operator|.
name|updateAndGet
argument_list|(
name|e
lambda|->
operator|(
name|KafkaException
operator|)
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sendExceptionRef
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Exception
name|exception
init|=
name|sendExceptionRef
operator|.
name|get
argument_list|()
decl_stmt|;
name|logHints
argument_list|(
name|exception
argument_list|)
expr_stmt|;
if|if
condition|(
name|tryToAbortTx
argument_list|(
name|exception
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Aborting Transaction [{}] cause by ERROR [{}]"
argument_list|,
name|writerIdTopicId
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|producer
operator|.
name|abortTransaction
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Closing writer [{}] caused by ERROR [{}]"
argument_list|,
name|writerIdTopicId
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|producer
operator|.
name|close
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|exception
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|tryToAbortTx
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
comment|// According to https://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
comment|// We can't recover from these exceptions, so our only option is to close the producer and exit.
name|boolean
name|isNotFencedOut
init|=
operator|!
operator|(
name|e
operator|instanceof
name|ProducerFencedException
operator|)
operator|&&
operator|!
operator|(
name|e
operator|instanceof
name|OutOfOrderSequenceException
operator|)
operator|&&
operator|!
operator|(
name|e
operator|instanceof
name|AuthenticationException
operator|)
decl_stmt|;
comment|// producer.send() may throw a KafkaException which wraps a FencedException therefore check inner cause.
name|boolean
name|causeIsNotFencedOut
init|=
operator|!
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ProducerFencedException
operator|)
decl_stmt|;
return|return
name|isNotFencedOut
operator|&&
name|causeIsNotFencedOut
return|;
block|}
comment|/**    * Given a query workingDirectory as table_directory/hive_query_id/ will fetch the open transaction states.    * Table directory is {@link org.apache.hadoop.hive.metastore.api.Table#getSd()#getLocation()}.    * Hive Query ID is inferred from the JobConf see {@link KafkaStorageHandler#getQueryId()}.    *    * The path to a transaction state is as follow.    * .../{@code queryWorkingDir}/{@code TRANSACTION_DIR}/{@code writerId}/{@code producerEpoch}    *    * The actual state is stored in the file {@code producerEpoch}.    * The file contains a {@link Long} as internal producer Id and a {@link Short} as the producer epoch.    * According to Kafka API, highest epoch corresponds to the active Producer, therefore if there is multiple    * {@code producerEpoch} files will pick the maximum based on {@link Short::compareTo}.    *    * @param fs File system handler.    * @param queryWorkingDir Query working Directory, see:    *                        {@link KafkaStorageHandler#getQueryWorkingDir(org.apache.hadoop.hive.metastore.api.Table)}.    * @return Map of Transaction Ids to Pair of Kafka Producer internal ID (Long) and producer epoch (short)    * @throws IOException if any of the IO operations fail.    */
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|Long
argument_list|,
name|Short
argument_list|>
argument_list|>
name|getTransactionsState
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|queryWorkingDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|//list all current Dir
specifier|final
name|Path
name|transactionWorkingDir
init|=
operator|new
name|Path
argument_list|(
name|queryWorkingDir
argument_list|,
name|TRANSACTION_DIR
argument_list|)
decl_stmt|;
specifier|final
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|transactionWorkingDir
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|FileStatus
argument_list|>
name|transactionSet
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|files
argument_list|)
operator|.
name|filter
argument_list|(
name|FileStatus
operator|::
name|isDirectory
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Path
argument_list|>
name|setOfTxPath
init|=
name|transactionSet
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|FileStatus
operator|::
name|getPath
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|Long
argument_list|,
name|Short
argument_list|>
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|setOfTxPath
operator|.
name|forEach
argument_list|(
name|path
lambda|->
block|{
specifier|final
name|String
name|txId
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
try|try
block|{
name|FileStatus
index|[]
name|epochFiles
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
comment|// List all the Epoch if any and select the max.
comment|// According to Kafka API recent venison of Producer with the same TxID will have greater epoch and same PID.
name|Optional
argument_list|<
name|Short
argument_list|>
name|maxEpoch
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|epochFiles
argument_list|)
operator|.
name|filter
argument_list|(
name|FileStatus
operator|::
name|isFile
argument_list|)
operator|.
name|map
argument_list|(
name|fileStatus
lambda|->
name|Short
operator|.
name|valueOf
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|max
argument_list|(
name|Short
operator|::
name|compareTo
argument_list|)
decl_stmt|;
name|short
name|epoch
init|=
name|maxEpoch
operator|.
name|orElseThrow
argument_list|(
parameter_list|()
lambda|->
operator|new
name|RuntimeException
argument_list|(
literal|"Missing sub directory epoch from directory ["
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|openTxFileName
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|epoch
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|internalId
decl_stmt|;
try|try
init|(
name|FSDataInputStream
name|inStream
init|=
name|fs
operator|.
name|open
argument_list|(
name|openTxFileName
argument_list|)
init|)
block|{
name|internalId
operator|=
name|inStream
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|short
name|fileEpoch
init|=
name|inStream
operator|.
name|readShort
argument_list|()
decl_stmt|;
if|if
condition|(
name|epoch
operator|!=
name|fileEpoch
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Was expecting [%s] but got [%s] from path [%s]"
argument_list|,
name|epoch
argument_list|,
name|fileEpoch
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|builder
operator|.
name|put
argument_list|(
name|txId
argument_list|,
name|Pair
operator|.
name|of
argument_list|(
name|internalId
argument_list|,
name|epoch
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

