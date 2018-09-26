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
name|kafka
operator|.
name|admin
operator|.
name|RackAwareMode
import|;
end_import

begin_import
import|import
name|kafka
operator|.
name|server
operator|.
name|KafkaConfig
import|;
end_import

begin_import
import|import
name|kafka
operator|.
name|server
operator|.
name|KafkaServer
import|;
end_import

begin_import
import|import
name|kafka
operator|.
name|utils
operator|.
name|TestUtils
import|;
end_import

begin_import
import|import
name|kafka
operator|.
name|zk
operator|.
name|AdminZkClient
import|;
end_import

begin_import
import|import
name|kafka
operator|.
name|zk
operator|.
name|EmbeddedZookeeper
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
name|io
operator|.
name|FileUtils
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
name|utils
operator|.
name|Time
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExternalResource
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
name|File
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
comment|/**  * Test Helper Class to start and stop a kafka broker.  */
end_comment

begin_class
class|class
name|KafkaBrokerResource
extends|extends
name|ExternalResource
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
name|KafkaBrokerResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TOPIC
init|=
literal|"TEST-CREATE_TOPIC"
decl_stmt|;
specifier|static
specifier|final
name|String
name|BROKER_IP_PORT
init|=
literal|"127.0.0.1:9092"
decl_stmt|;
specifier|private
name|EmbeddedZookeeper
name|zkServer
decl_stmt|;
specifier|private
name|KafkaServer
name|kafkaServer
decl_stmt|;
specifier|private
name|AdminZkClient
name|adminZkClient
decl_stmt|;
specifier|private
name|Path
name|tmpLogDir
decl_stmt|;
comment|/**    * Override to set up your specific external resource.    *    * @throws Throwable if setup fails (which will disable {@code after}    */
annotation|@
name|Override
specifier|protected
name|void
name|before
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Start the ZK and the Broker
name|LOG
operator|.
name|info
argument_list|(
literal|"init embedded Zookeeper"
argument_list|)
expr_stmt|;
name|zkServer
operator|=
operator|new
name|EmbeddedZookeeper
argument_list|()
expr_stmt|;
name|tmpLogDir
operator|=
name|Files
operator|.
name|createTempDirectory
argument_list|(
literal|"kafka-log-dir-"
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
expr_stmt|;
name|String
name|zkConnect
init|=
literal|"127.0.0.1:"
operator|+
name|zkServer
operator|.
name|port
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"init kafka broker"
argument_list|)
expr_stmt|;
name|Properties
name|brokerProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"zookeeper.connect"
argument_list|,
name|zkConnect
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"broker.id"
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"log.dir"
argument_list|,
name|tmpLogDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"listeners"
argument_list|,
literal|"PLAINTEXT://"
operator|+
name|BROKER_IP_PORT
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"offsets.topic.replication.factor"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"transaction.state.log.replication.factor"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|brokerProps
operator|.
name|setProperty
argument_list|(
literal|"transaction.state.log.min.isr"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|KafkaConfig
name|config
init|=
operator|new
name|KafkaConfig
argument_list|(
name|brokerProps
argument_list|)
decl_stmt|;
name|kafkaServer
operator|=
name|TestUtils
operator|.
name|createServer
argument_list|(
name|config
argument_list|,
name|Time
operator|.
name|SYSTEM
argument_list|)
expr_stmt|;
name|kafkaServer
operator|.
name|startup
argument_list|()
expr_stmt|;
name|kafkaServer
operator|.
name|zkClient
argument_list|()
expr_stmt|;
name|adminZkClient
operator|=
operator|new
name|AdminZkClient
argument_list|(
name|kafkaServer
operator|.
name|zkClient
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating kafka TOPIC [{}]"
argument_list|,
name|TOPIC
argument_list|)
expr_stmt|;
name|adminZkClient
operator|.
name|createTopic
argument_list|(
name|TOPIC
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|,
name|RackAwareMode
operator|.
name|Disabled$
operator|.
name|MODULE$
argument_list|)
expr_stmt|;
block|}
comment|/**    * Override to tear down your specific external resource.    */
annotation|@
name|Override
specifier|protected
name|void
name|after
parameter_list|()
block|{
name|super
operator|.
name|after
argument_list|()
expr_stmt|;
try|try
block|{
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|tmpLogDir
operator|.
name|toString
argument_list|()
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error cleaning "
operator|+
name|tmpLogDir
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|kafkaServer
operator|!=
literal|null
condition|)
block|{
name|kafkaServer
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|kafkaServer
operator|.
name|awaitShutdown
argument_list|()
expr_stmt|;
block|}
name|zkServer
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|void
name|deleteTopic
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"SameParameterValue"
argument_list|)
name|String
name|topic
parameter_list|)
block|{
name|adminZkClient
operator|.
name|deleteTopic
argument_list|(
name|topic
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

