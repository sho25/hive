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
name|metastore
operator|.
name|HiveMetaHook
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
name|metadata
operator|.
name|HiveStorageHandler
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|DefaultHiveAuthorizationProvider
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|AbstractSerDe
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
name|OutputFormat
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
name|lib
operator|.
name|NullOutputFormat
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
comment|/**  * Hive Kafka storage handler to allow user querying Stream of tuples from a Kafka queue.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaStorageHandler
implements|implements
name|HiveStorageHandler
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
name|KafkaStorageHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KAFKA_STORAGE_HANDLER
init|=
literal|"org.apache.hadoop.hive.kafka.KafkaStorageHandler"
decl_stmt|;
specifier|private
name|Configuration
name|configuration
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
block|{
return|return
name|KafkaPullerInputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFormatClass
parameter_list|()
block|{
return|return
name|NullOutputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|AbstractSerDe
argument_list|>
name|getSerDeClass
parameter_list|()
block|{
return|return
name|GenericKafkaSerDe
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveMetaHook
name|getMetaHook
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveAuthorizationProvider
name|getAuthorizationProvider
parameter_list|()
block|{
return|return
operator|new
name|DefaultHiveAuthorizationProvider
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureInputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
name|String
name|topic
init|=
name|tableDesc
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_TOPIC
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|topic
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Kafka topic missing set table property->"
operator|+
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_TOPIC
argument_list|)
throw|;
block|}
name|jobProperties
operator|.
name|put
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_TOPIC
argument_list|,
name|topic
argument_list|)
expr_stmt|;
name|String
name|brokerString
init|=
name|tableDesc
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|brokerString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Broker address missing set table property->"
operator|+
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
argument_list|)
throw|;
block|}
name|jobProperties
operator|.
name|put
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
argument_list|,
name|brokerString
argument_list|)
expr_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|KafkaStreamingUtils
operator|.
name|SERDE_CLASS_NAME
argument_list|,
name|tableDesc
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|KafkaStreamingUtils
operator|.
name|SERDE_CLASS_NAME
argument_list|,
name|KafkaJsonSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table properties: SerDe class name {}"
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|KafkaStreamingUtils
operator|.
name|SERDE_CLASS_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|//set extra properties
name|tableDesc
operator|.
name|getProperties
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|objectObjectEntry
lambda|->
name|objectObjectEntry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|KafkaStreamingUtils
operator|.
name|CONSUMER_CONFIGURATION_PREFIX
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
name|String
name|key
operator|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|substring
argument_list|(
name|KafkaStreamingUtils
operator|.
name|CONSUMER_CONFIGURATION_PREFIX
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|;           if
operator|(
name|KafkaStreamingUtils
operator|.
name|FORBIDDEN_PROPERTIES
operator|.
name|contains
argument_list|(
name|key
argument_list|)
operator|)
block|{
throw|throw
argument_list|new
name|IllegalArgumentException
argument_list|(
literal|"Not suppose to set Kafka Property "
operator|+
name|key
argument_list|)
block|;           }
name|String
name|value
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|;
name|jobProperties
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting extra job properties: key [{}] -> value [{}]"
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
argument_list|;
block|}
block|)
class|;
end_class

begin_function
unit|}    @
name|Override
specifier|public
name|void
name|configureInputJobCredentials
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|secrets
parameter_list|)
block|{    }
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|configureOutputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{    }
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|configureTableJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
name|configureInputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|configureInputJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|properties
operator|.
name|forEach
argument_list|(
name|jobConf
operator|::
name|set
argument_list|)
expr_stmt|;
try|try
block|{
name|KafkaStreamingUtils
operator|.
name|copyDependencyJars
argument_list|(
name|jobConf
argument_list|,
name|KafkaStorageHandler
operator|.
name|class
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
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|KAFKA_STORAGE_HANDLER
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|StorageHandlerInfo
name|getStorageHandlerInfo
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|topic
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_TOPIC
argument_list|)
decl_stmt|;
if|if
condition|(
name|topic
operator|==
literal|null
operator|||
name|topic
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"topic is null or empty"
argument_list|)
throw|;
block|}
name|String
name|brokers
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|KafkaStreamingUtils
operator|.
name|HIVE_KAFKA_BOOTSTRAP_SERVERS
argument_list|)
decl_stmt|;
if|if
condition|(
name|brokers
operator|==
literal|null
operator|||
name|brokers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"kafka brokers string is null or empty"
argument_list|)
throw|;
block|}
specifier|final
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|ConsumerConfig
operator|.
name|KEY_DESERIALIZER_CLASS_CONFIG
argument_list|,
name|ByteArrayDeserializer
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
name|ConsumerConfig
operator|.
name|VALUE_DESERIALIZER_CLASS_CONFIG
argument_list|,
name|ByteArrayDeserializer
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
name|CommonClientConfigs
operator|.
name|BOOTSTRAP_SERVERS_CONFIG
argument_list|,
name|brokers
argument_list|)
expr_stmt|;
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|objectObjectEntry
lambda|->
name|objectObjectEntry
operator|.
name|getKey
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|KafkaStreamingUtils
operator|.
name|CONSUMER_CONFIGURATION_PREFIX
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
name|String
name|key
operator|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
name|KafkaStreamingUtils
operator|.
name|CONSUMER_CONFIGURATION_PREFIX
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|;           if
operator|(
name|KafkaStreamingUtils
operator|.
name|FORBIDDEN_PROPERTIES
operator|.
name|contains
argument_list|(
name|key
argument_list|)
operator|)
block|{
throw|throw
argument_list|new
name|IllegalArgumentException
argument_list|(
literal|"Not suppose to set Kafka Property "
operator|+
name|key
argument_list|)
block|;           }
name|properties
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_return
return|return
operator|new
name|KafkaStorageHandlerInfo
argument_list|(
name|topic
argument_list|,
name|properties
argument_list|)
return|;
end_return

unit|} }
end_unit

