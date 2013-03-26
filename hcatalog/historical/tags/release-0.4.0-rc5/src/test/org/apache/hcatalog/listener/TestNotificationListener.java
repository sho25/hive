begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|MapMessage
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
name|ObjectMessage
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
name|api
operator|.
name|Database
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
name|InvalidPartitionException
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|metastore
operator|.
name|api
operator|.
name|UnknownDBException
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
name|UnknownPartitionException
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
name|UnknownTableException
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
name|CommandNeedRetryException
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
name|thrift
operator|.
name|TException
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

begin_class
specifier|public
class|class
name|TestNotificationListener
extends|extends
name|TestCase
implements|implements
name|MessageListener
block|{
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Driver
name|driver
decl_stmt|;
specifier|private
name|AtomicInteger
name|cntInvocation
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
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
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
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
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|cntInvocation
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testAMQListener
parameter_list|()
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|NoSuchObjectException
throws|,
name|CommandNeedRetryException
throws|,
name|UnknownDBException
throws|,
name|InvalidPartitionException
throws|,
name|UnknownPartitionException
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
name|HiveMetaStoreClient
name|msc
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
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
name|msc
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
literal|"alter table mytbl drop partition(b='2011')"
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
name|cntInvocation
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|event
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ADD_DATABASE_EVENT
argument_list|)
condition|)
block|{
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
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
operator|(
call|(
name|Database
call|)
argument_list|(
operator|(
name|ObjectMessage
operator|)
name|msg
argument_list|)
operator|.
name|getObject
argument_list|()
operator|)
operator|.
name|getName
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
name|HCAT_ADD_TABLE_EVENT
argument_list|)
condition|)
block|{
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
name|Table
name|tbl
init|=
call|(
name|Table
call|)
argument_list|(
operator|(
operator|(
name|ObjectMessage
operator|)
name|msg
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tbl
operator|.
name|getPartitionKeysSize
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
name|Partition
name|part
init|=
call|(
name|Partition
call|)
argument_list|(
operator|(
operator|(
name|ObjectMessage
operator|)
name|msg
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|part
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|part
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|vals
operator|.
name|add
argument_list|(
literal|"2011"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|vals
argument_list|,
name|part
operator|.
name|getValues
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
name|HCAT_DROP_PARTITION_EVENT
argument_list|)
condition|)
block|{
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
name|Partition
name|part
init|=
call|(
name|Partition
call|)
argument_list|(
operator|(
operator|(
name|ObjectMessage
operator|)
name|msg
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|part
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|part
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|vals
operator|.
name|add
argument_list|(
literal|"2011"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|vals
argument_list|,
name|part
operator|.
name|getValues
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
name|HCAT_DROP_TABLE_EVENT
argument_list|)
condition|)
block|{
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
name|Table
name|tbl
init|=
call|(
name|Table
call|)
argument_list|(
operator|(
operator|(
name|ObjectMessage
operator|)
name|msg
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"mytbl"
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tbl
operator|.
name|getPartitionKeysSize
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
name|assertEquals
argument_list|(
literal|"mydb"
argument_list|,
operator|(
call|(
name|Database
call|)
argument_list|(
operator|(
name|ObjectMessage
operator|)
name|msg
argument_list|)
operator|.
name|getObject
argument_list|()
operator|)
operator|.
name|getName
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
name|MapMessage
name|mapMsg
init|=
operator|(
name|MapMessage
operator|)
name|msg
decl_stmt|;
assert|assert
name|mapMsg
operator|.
name|getString
argument_list|(
literal|"b"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"2011"
argument_list|)
assert|;
block|}
else|else
assert|assert
literal|false
assert|;
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
block|}
block|}
end_class

end_unit

