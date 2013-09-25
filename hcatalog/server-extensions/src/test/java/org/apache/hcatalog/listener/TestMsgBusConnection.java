begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|listener
package|;
end_package

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
name|TextMessage
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|activemq
operator|.
name|broker
operator|.
name|BrokerService
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
name|api
operator|.
name|AlreadyExistsException
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
name|NoSuchObjectException
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
name|Driver
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
name|hcatalog
operator|.
name|messaging
operator|.
name|jms
operator|.
name|MessagingUtils
import|;
end_import

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.listener.TestMsgBusConnection} instead  */
end_comment

begin_class
specifier|public
class|class
name|TestMsgBusConnection
extends|extends
name|TestCase
block|{
specifier|private
name|Driver
name|driver
decl_stmt|;
specifier|private
name|BrokerService
name|broker
decl_stmt|;
specifier|private
name|MessageConsumer
name|consumer
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|broker
operator|=
operator|new
name|BrokerService
argument_list|()
expr_stmt|;
comment|// configure the broker
name|broker
operator|.
name|addConnector
argument_list|(
literal|"tcp://localhost:61616?broker.persistent=false"
argument_list|)
expr_stmt|;
name|broker
operator|.
name|start
argument_list|()
expr_stmt|;
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
literal|"tcp://localhost:61616"
argument_list|)
expr_stmt|;
name|connectClient
argument_list|()
expr_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
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
name|set
argument_list|(
literal|"hive.metastore.local"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_MSGBUS_TOPIC_PREFIX
argument_list|,
literal|"planetlab.hcat"
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
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|connectClient
parameter_list|()
throws|throws
name|JMSException
block|{
name|ConnectionFactory
name|connFac
init|=
operator|new
name|ActiveMQConnectionFactory
argument_list|(
literal|"tcp://localhost:61616"
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
literal|"planetlab.hcat"
argument_list|)
decl_stmt|;
name|consumer
operator|=
name|session
operator|.
name|createConsumer
argument_list|(
name|hcatTopic
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testConnection
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create database testconndb"
argument_list|)
expr_stmt|;
name|Message
name|msg
init|=
name|consumer
operator|.
name|receive
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected TextMessage"
argument_list|,
name|msg
operator|instanceof
name|TextMessage
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|,
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_EVENT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"topic://planetlab.hcat"
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
name|HCatEventMessage
name|messageObject
init|=
name|MessagingUtils
operator|.
name|getMessage
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"testconndb"
argument_list|,
name|messageObject
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|broker
operator|.
name|stop
argument_list|()
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database testconndb cascade"
argument_list|)
expr_stmt|;
name|broker
operator|.
name|start
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|connectClient
argument_list|()
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database testconndb"
argument_list|)
expr_stmt|;
name|msg
operator|=
name|consumer
operator|.
name|receive
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|,
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_EVENT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"topic://planetlab.hcat"
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
name|assertEquals
argument_list|(
literal|"testconndb"
argument_list|,
name|messageObject
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database testconndb cascade"
argument_list|)
expr_stmt|;
name|msg
operator|=
name|consumer
operator|.
name|receive
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|,
name|msg
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_EVENT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"topic://planetlab.hcat"
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
name|assertEquals
argument_list|(
literal|"testconndb"
argument_list|,
name|messageObject
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|nsoe
parameter_list|)
block|{
name|nsoe
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
catch|catch
parameter_list|(
name|AlreadyExistsException
name|aee
parameter_list|)
block|{
name|aee
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
block|}
block|}
end_class

end_unit

