begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|listener
package|;
end_package

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
name|Vector
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
name|CountDownLatch
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
name|javax
operator|.
name|jms
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|ConnectionFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|Destination
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|JMSException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|TextMessage
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|MessageConsumer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|MessageListener
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|Session
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|activemq
operator|.
name|ActiveMQConnectionFactory
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
name|cli
operator|.
name|CliSessionState
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|HiveMetaStoreClient
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
name|PartitionEventType
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
name|DriverFactory
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatBaseTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|AddPartitionMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|AlterPartitionMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|AlterTableMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|CreateDatabaseMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|CreateTableMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|DropDatabaseMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|DropPartitionMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|DropTableMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|HCatEventMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|MessageDeserializer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|MessageFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|jms
operator|.
name|MessagingUtils
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestNotificationListener
extends|extends
name|HCatBaseTest
implements|implements
name|MessageListener
block|{
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|actualMessages
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MSG_RECEIVED_TIMEOUT
init|=
literal|30
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|expectedMessages
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ALTER_PARTITION_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ALTER_TABLE_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|CountDownLatch
name|messageReceivedSignal
init|=
operator|new
name|CountDownLatch
argument_list|(
name|expectedMessages
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.naming.factory.initial"
argument_list|,
literal|"org.apache.activemq.jndi.ActiveMQInitialContextFactory"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.naming.provider.url"
argument_list|,
literal|"vm://localhost?broker.persistent=false"
argument_list|)
expr_stmt|;
name|ConnectionFactory
name|connFac
init|=
operator|new
name|ActiveMQConnectionFactory
argument_list|(
literal|"vm://localhost?broker.persistent=false"
argument_list|)
decl_stmt|;
name|Connection
name|conn
init|=
name|connFac
operator|.
name|createConnection
argument_list|()
decl_stmt|;
name|conn
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// We want message to be sent when session commits, thus we run in
comment|// transacted mode.
name|Session
name|session
init|=
name|conn
operator|.
name|createSession
argument_list|(
literal|true
argument_list|,
name|Session
operator|.
name|SESSION_TRANSACTED
argument_list|)
decl_stmt|;
name|Destination
name|hcatTopic
init|=
name|session
operator|.
name|createTopic
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
argument_list|)
decl_stmt|;
name|MessageConsumer
name|consumer1
init|=
name|session
operator|.
name|createConsumer
argument_list|(
name|hcatTopic
argument_list|)
decl_stmt|;
name|consumer1
operator|.
name|setMessageListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|Destination
name|tblTopic
init|=
name|session
operator|.
name|createTopic
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|".mydb.mytbl"
argument_list|)
decl_stmt|;
name|MessageConsumer
name|consumer2
init|=
name|session
operator|.
name|createConsumer
argument_list|(
name|tblTopic
argument_list|)
decl_stmt|;
name|consumer2
operator|.
name|setMessageListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|Destination
name|dbTopic
init|=
name|session
operator|.
name|createTopic
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|".mydb"
argument_list|)
decl_stmt|;
name|MessageConsumer
name|consumer3
init|=
name|session
operator|.
name|createConsumer
argument_list|(
name|dbTopic
argument_list|)
decl_stmt|;
name|consumer3
operator|.
name|setMessageListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|setUpHiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTORE_EVENT_LISTENERS
operator|.
name|varname
argument_list|,
name|NotificationListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedMessages
argument_list|,
name|actualMessages
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAMQListener
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create database mydb"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use mydb"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table mytbl (a string) partitioned by (b string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table mytbl add partition(b='2011')"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kvs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|put
argument_list|(
literal|"b"
argument_list|,
literal|"2011"
argument_list|)
expr_stmt|;
name|client
operator|.
name|markPartitionForEvent
argument_list|(
literal|"mydb"
argument_list|,
literal|"mytbl"
argument_list|,
name|kvs
argument_list|,
name|PartitionEventType
operator|.
name|LOAD_DONE
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table mytbl partition (b='2011') set fileformat orc"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table mytbl drop partition(b='2011')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table mytbl add columns (c int comment 'this is an int', d decimal(3,2))"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table mytbl"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database mydb"
argument_list|)
expr_stmt|;
comment|// Wait until either all messages are processed or a maximum time limit is reached.
name|messageReceivedSignal
operator|.
name|await
argument_list|(
name|MSG_RECEIVED_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMessage
parameter_list|(
name|Message
name|msg
parameter_list|)
block|{
name|String
name|event
decl_stmt|;
try|try
block|{
name|event
operator|=
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_EVENT
argument_list|)
expr_stmt|;
name|String
name|format
init|=
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_MESSAGE_FORMAT
argument_list|)
decl_stmt|;
name|String
name|version
init|=
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_MESSAGE_VERSION
argument_list|)
decl_stmt|;
name|String
name|messageBody
init|=
operator|(
operator|(
name|TextMessage
operator|)
name|msg
operator|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|actualMessages
operator|.
name|add
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|MessageDeserializer
name|deserializer
init|=
name|MessageFactory
operator|.
name|getDeserializer
argument_list|(
name|format
argument_list|,
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://"
operator|+
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|CreateDatabaseMessage
name|message
init|=
name|deserializer
operator|.
name|getCreateDatabaseMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|CreateDatabaseMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|CreateTableMessage
name|message
init|=
name|deserializer
operator|.
name|getCreateTableMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|CreateTableMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|CreateTableMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb.mytbl"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|AddPartitionMessage
name|message
init|=
name|deserializer
operator|.
name|getAddPartitionMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|message
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"2011"
argument_list|,
name|message
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|AddPartitionMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|AddPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|AddPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"2011"
argument_list|,
operator|(
operator|(
name|AddPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ALTER_PARTITION_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb.mytbl"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// for alter partition events
name|AlterPartitionMessage
name|message
init|=
name|deserializer
operator|.
name|getAlterPartitionMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|message
operator|.
name|getKeyValues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|message
operator|.
name|getKeyValues
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|contains
argument_list|(
literal|"2011"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|AlterPartitionMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|AlterPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|AlterPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getKeyValues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
operator|(
name|AlterPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getKeyValues
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|contains
argument_list|(
literal|"2011"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb.mytbl"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|DropPartitionMessage
name|message
init|=
name|deserializer
operator|.
name|getDropPartitionMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|message
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"2011"
argument_list|,
name|message
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|DropPartitionMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|DropPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|DropPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"2011"
argument_list|,
operator|(
operator|(
name|DropPartitionMessage
operator|)
name|message2
operator|)
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|DropTableMessage
name|message
init|=
name|deserializer
operator|.
name|getDropTableMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|DropTableMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|DropTableMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://"
operator|+
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|DropDatabaseMessage
name|message
init|=
name|deserializer
operator|.
name|getDropDatabaseMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|DropDatabaseMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ALTER_TABLE_EVENT
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"topic://hcat.mydb"
argument_list|,
name|msg
operator|.
name|getJMSDestination
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|AlterTableMessage
name|message
init|=
name|deserializer
operator|.
name|getAlterTableMessage
argument_list|(
name|messageBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|message
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|message
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|HCatEventMessage
name|message2
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected message-type."
argument_list|,
name|message2
operator|instanceof
name|AlterTableMessage
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|message2
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
operator|(
operator|(
name|AlterTableMessage
operator|)
name|message2
operator|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PARTITION_DONE_EVENT
argument_list|)
condition|)
block|{
comment|// TODO: Fill in when PARTITION_DONE_EVENT is supported.
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected: HCAT_PARTITION_DONE_EVENT not supported (yet)."
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected event-type: "
operator|+
name|event
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|JMSException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
finally|finally
block|{
name|messageReceivedSignal
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

