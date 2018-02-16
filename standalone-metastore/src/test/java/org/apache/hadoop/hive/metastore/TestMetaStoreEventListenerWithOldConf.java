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
name|metastore
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
name|Lists
import|;
end_import

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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Map
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
name|hive
operator|.
name|metastore
operator|.
name|annotation
operator|.
name|MetastoreUnitTest
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
name|FieldSchema
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
name|Index
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
name|client
operator|.
name|builder
operator|.
name|DatabaseBuilder
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
name|client
operator|.
name|builder
operator|.
name|IndexBuilder
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
name|client
operator|.
name|builder
operator|.
name|PartitionBuilder
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
name|client
operator|.
name|builder
operator|.
name|TableBuilder
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|MetastoreConf
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
name|events
operator|.
name|AddIndexEvent
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
name|events
operator|.
name|AddPartitionEvent
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
name|events
operator|.
name|AlterIndexEvent
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
name|events
operator|.
name|AlterPartitionEvent
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
name|events
operator|.
name|AlterTableEvent
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
name|events
operator|.
name|ConfigChangeEvent
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
name|events
operator|.
name|CreateDatabaseEvent
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
name|events
operator|.
name|CreateTableEvent
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
name|events
operator|.
name|DropDatabaseEvent
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
name|events
operator|.
name|DropIndexEvent
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
name|events
operator|.
name|DropPartitionEvent
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
name|events
operator|.
name|DropTableEvent
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
name|events
operator|.
name|ListenerEvent
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
name|events
operator|.
name|LoadPartitionDoneEvent
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
name|events
operator|.
name|PreAddIndexEvent
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
name|events
operator|.
name|PreAddPartitionEvent
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
name|events
operator|.
name|PreAlterIndexEvent
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
name|events
operator|.
name|PreAlterPartitionEvent
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
name|events
operator|.
name|PreAlterTableEvent
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
name|events
operator|.
name|PreCreateDatabaseEvent
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
name|events
operator|.
name|PreCreateTableEvent
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
name|events
operator|.
name|PreDropDatabaseEvent
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
name|events
operator|.
name|PreDropIndexEvent
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
name|events
operator|.
name|PreDropPartitionEvent
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
name|events
operator|.
name|PreDropTableEvent
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
name|events
operator|.
name|PreEventContext
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
name|events
operator|.
name|PreLoadPartitionDoneEvent
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
name|security
operator|.
name|HadoopThriftAuthBridge
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotSame
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Mostly same tests as TestMetaStoreEventListener, but using old hive conf values instead of new  * metastore conf values.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetaStoreEventListenerWithOldConf
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|metaConfKey
init|=
literal|"hive.metastore.partition.name.whitelist.pattern"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|metaConfVal
init|=
literal|""
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
literal|"hive.metastore.event.listeners"
argument_list|,
name|DummyListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.pre.event.listeners"
argument_list|,
name|DummyPreListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|PARTITION_NAME_WHITELIST_PATTERN
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_URIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_CONNECTION_RETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|setConfForStandloneMode
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|DummyListener
operator|.
name|notifyList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|DummyPreListener
operator|.
name|notifyList
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaConfNotifyListenersClosingClient
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStoreClient
name|closingClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|closingClient
operator|.
name|setMetaConf
argument_list|(
name|metaConfKey
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|ConfigChangeEvent
name|event
init|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getOldValue
argument_list|()
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getNewValue
argument_list|()
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|closingClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|event
operator|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getOldValue
argument_list|()
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getNewValue
argument_list|()
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaConfNotifyListenersNonClosingClient
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStoreClient
name|nonClosingClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|nonClosingClient
operator|.
name|setMetaConf
argument_list|(
name|metaConfKey
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|ConfigChangeEvent
name|event
init|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getOldValue
argument_list|()
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getNewValue
argument_list|()
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
comment|// This should also trigger meta listener notification via TServerEventHandler#deleteContext
name|nonClosingClient
operator|.
name|getTTransport
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|event
operator|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getOldValue
argument_list|()
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|event
operator|.
name|getNewValue
argument_list|()
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaConfDuplicateNotification
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStoreClient
name|closingClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|closingClient
operator|.
name|setMetaConf
argument_list|(
name|metaConfKey
argument_list|,
name|metaConfVal
argument_list|)
expr_stmt|;
name|int
name|beforeCloseNotificationEventCounts
init|=
name|DummyListener
operator|.
name|notifyList
operator|.
name|size
argument_list|()
decl_stmt|;
name|closingClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|int
name|afterCloseNotificationEventCounts
init|=
name|DummyListener
operator|.
name|notifyList
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Setting key to same value, should not trigger configChange event during shutdown
name|assertEquals
argument_list|(
name|beforeCloseNotificationEventCounts
argument_list|,
name|afterCloseNotificationEventCounts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaConfSameHandler
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStoreClient
name|closingClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|closingClient
operator|.
name|setMetaConf
argument_list|(
name|metaConfKey
argument_list|,
literal|"[test pattern modified]"
argument_list|)
expr_stmt|;
name|ConfigChangeEvent
name|event
init|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
decl_stmt|;
name|int
name|beforeCloseNotificationEventCounts
init|=
name|DummyListener
operator|.
name|notifyList
operator|.
name|size
argument_list|()
decl_stmt|;
name|IHMSHandler
name|beforeHandler
init|=
name|event
operator|.
name|getHandler
argument_list|()
decl_stmt|;
name|closingClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|event
operator|=
operator|(
name|ConfigChangeEvent
operator|)
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
expr_stmt|;
name|int
name|afterCloseNotificationEventCounts
init|=
name|DummyListener
operator|.
name|notifyList
operator|.
name|size
argument_list|()
decl_stmt|;
name|IHMSHandler
name|afterHandler
init|=
name|event
operator|.
name|getHandler
argument_list|()
decl_stmt|;
comment|// Meta-conf cleanup should trigger an event to listener
name|assertNotSame
argument_list|(
name|beforeCloseNotificationEventCounts
argument_list|,
name|afterCloseNotificationEventCounts
argument_list|)
expr_stmt|;
comment|// Both the handlers should be same
name|assertEquals
argument_list|(
name|beforeHandler
argument_list|,
name|afterHandler
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

