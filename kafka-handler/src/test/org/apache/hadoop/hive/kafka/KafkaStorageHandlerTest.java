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
name|metastore
operator|.
name|TableType
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|plan
operator|.
name|TableDesc
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
name|serde2
operator|.
name|avro
operator|.
name|AvroSerDe
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
name|producer
operator|.
name|ProducerConfig
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
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
name|Properties
import|;
end_import

begin_comment
comment|/**  * Test class for properties setting.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaStorageHandlerTest
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_TOPIC
init|=
literal|"test-topic"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LOCALHOST_9291
init|=
literal|"localhost:9291"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|configureJobPropertiesWithDefaultValues
parameter_list|()
throws|throws
name|MetaException
block|{
name|KafkaStorageHandler
name|kafkaStorageHandler
init|=
operator|new
name|KafkaStorageHandler
argument_list|()
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TableDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Table
name|preCreateTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|kafkaStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|preCreateTable
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|getParameters
argument_list|()
operator|.
name|forEach
argument_list|(
name|properties
operator|::
name|setProperty
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|kafkaStorageHandler
operator|.
name|configureInputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
name|kafkaStorageHandler
operator|.
name|configureOutputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|stream
argument_list|(
name|KafkaTableProperties
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|key
lambda|->
operator|!
name|key
operator|.
name|isMandatory
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
parameter_list|(
name|key
parameter_list|)
lambda|->
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Wrong match for key "
operator|+
name|key
operator|.
name|getName
argument_list|()
argument_list|,
name|key
operator|.
name|getDefaultValue
argument_list|()
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|key
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|configureInputJobProperties
parameter_list|()
throws|throws
name|MetaException
block|{
name|KafkaStorageHandler
name|kafkaStorageHandler
init|=
operator|new
name|KafkaStorageHandler
argument_list|()
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TableDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// set the mandatory properties
name|Table
name|preCreateTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|preCreateTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|kafkaStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|preCreateTable
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|getParameters
argument_list|()
operator|.
name|forEach
argument_list|(
name|properties
operator|::
name|setProperty
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|KafkaTableProperties
operator|.
name|WRITE_SEMANTIC_PROPERTY
operator|.
name|getName
argument_list|()
argument_list|,
name|KafkaOutputFormat
operator|.
name|WriteSemantic
operator|.
name|EXACTLY_ONCE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|KafkaTableProperties
operator|.
name|SERDE_CLASS_NAME
operator|.
name|getName
argument_list|()
argument_list|,
name|AvroSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|KafkaTableProperties
operator|.
name|KAFKA_POLL_TIMEOUT
operator|.
name|getName
argument_list|()
argument_list|,
literal|"7000"
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|kafkaStorageHandler
operator|.
name|configureInputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|AvroSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|SERDE_CLASS_NAME
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"7000"
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|KAFKA_POLL_TIMEOUT
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|jobProperties
operator|.
name|forEach
argument_list|(
name|jobConf
operator|::
name|set
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
literal|"task_id_test_0001"
argument_list|)
expr_stmt|;
name|Properties
name|kafkaProperties
init|=
name|KafkaUtils
operator|.
name|consumerProperties
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LOCALHOST_9291
argument_list|,
name|kafkaProperties
operator|.
name|get
argument_list|(
name|CommonClientConfigs
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"read_committed"
argument_list|,
name|kafkaProperties
operator|.
name|get
argument_list|(
name|ConsumerConfig
operator|.
name|ISOLATION_LEVEL_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|kafkaProperties
operator|.
name|get
argument_list|(
name|ConsumerConfig
operator|.
name|ENABLE_AUTO_COMMIT_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"none"
argument_list|,
name|kafkaProperties
operator|.
name|get
argument_list|(
name|ConsumerConfig
operator|.
name|AUTO_OFFSET_RESET_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|jobConf
argument_list|)
argument_list|,
name|kafkaProperties
operator|.
name|get
argument_list|(
name|CommonClientConfigs
operator|.
name|CLIENT_ID_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|configureOutJobProperties
parameter_list|()
throws|throws
name|MetaException
block|{
name|KafkaStorageHandler
name|kafkaStorageHandler
init|=
operator|new
name|KafkaStorageHandler
argument_list|()
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TableDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// set the mandatory properties
name|Table
name|preCreateTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|putToParameters
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|kafkaStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|preCreateTable
argument_list|)
expr_stmt|;
name|preCreateTable
operator|.
name|getParameters
argument_list|()
operator|.
name|forEach
argument_list|(
name|properties
operator|::
name|setProperty
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|KafkaTableProperties
operator|.
name|WRITE_SEMANTIC_PROPERTY
operator|.
name|getName
argument_list|()
argument_list|,
name|KafkaOutputFormat
operator|.
name|WriteSemantic
operator|.
name|EXACTLY_ONCE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_OPTIMISTIC_COMMIT
operator|.
name|getName
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|kafkaStorageHandler
operator|.
name|configureOutputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"false"
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_OPTIMISTIC_COMMIT
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_TOPIC
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|TEST_TOPIC
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|LOCALHOST_9291
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|KafkaOutputFormat
operator|.
name|WriteSemantic
operator|.
name|EXACTLY_ONCE
operator|.
name|name
argument_list|()
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|WRITE_SEMANTIC_PROPERTY
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaUtils
operator|.
name|CONSUMER_CONFIGURATION_PREFIX
operator|+
literal|"."
operator|+
name|ConsumerConfig
operator|.
name|ISOLATION_LEVEL_CONFIG
argument_list|)
argument_list|,
literal|"read_committed"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|KafkaOutputFormat
operator|.
name|WriteSemantic
operator|.
name|EXACTLY_ONCE
operator|.
name|name
argument_list|()
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaTableProperties
operator|.
name|WRITE_SEMANTIC_PROPERTY
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|jobProperties
operator|.
name|forEach
argument_list|(
name|jobConf
operator|::
name|set
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
literal|"task_id_test"
argument_list|)
expr_stmt|;
name|Properties
name|producerProperties
init|=
name|KafkaUtils
operator|.
name|producerProperties
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LOCALHOST_9291
argument_list|,
name|producerProperties
operator|.
name|get
argument_list|(
name|CommonClientConfigs
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"task_id_test"
argument_list|,
name|producerProperties
operator|.
name|get
argument_list|(
name|CommonClientConfigs
operator|.
name|CLIENT_ID_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"all"
argument_list|,
name|producerProperties
operator|.
name|get
argument_list|(
name|ProducerConfig
operator|.
name|ACKS_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|producerProperties
operator|.
name|get
argument_list|(
name|ProducerConfig
operator|.
name|RETRIES_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"true"
argument_list|,
name|producerProperties
operator|.
name|get
argument_list|(
name|ProducerConfig
operator|.
name|ENABLE_IDEMPOTENCE_CONFIG
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

