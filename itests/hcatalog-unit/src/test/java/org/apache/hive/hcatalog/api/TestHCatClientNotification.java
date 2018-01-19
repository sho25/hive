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
name|api
package|;
end_package

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
name|assertNotNull
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|metastore
operator|.
name|IMetaStoreClient
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
name|NotificationEvent
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
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
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
name|listener
operator|.
name|DbNotificationListener
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

begin_comment
comment|/**  * This can't use TestHCatClient because it has to have control over certain conf variables when  * the metastore is started.  Plus, we don't need a metastore running in another thread.  The  * local one is fine.  */
end_comment

begin_class
specifier|public
class|class
name|TestHCatClientNotification
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
name|TestHCatClientNotification
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HCatClient
name|hCatClient
decl_stmt|;
specifier|private
specifier|static
name|MessageDeserializer
name|md
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|startTime
decl_stmt|;
specifier|private
name|long
name|firstEventId
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupClient
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_LISTENERS
argument_list|,
name|DbNotificationListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|=
name|HCatClient
operator|.
name|create
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|md
operator|=
name|MessageFactory
operator|.
name|getInstance
argument_list|()
operator|.
name|getDeserializer
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
decl_stmt|;
name|startTime
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|now
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
name|fail
argument_list|(
literal|"Bummer, time has fallen over the edge"
argument_list|)
expr_stmt|;
else|else
name|startTime
operator|=
operator|(
name|int
operator|)
name|now
expr_stmt|;
name|firstEventId
operator|=
name|hCatClient
operator|.
name|getCurrentNotificationEventId
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"myhcatdb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|1
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"myhcatdb"
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|CreateDatabaseMessage
name|createDatabaseMessage
init|=
name|md
operator|.
name|getCreateDatabaseMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"myhcatdb"
argument_list|,
name|createDatabaseMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|dropDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbname
init|=
literal|"hcatdropdb"
decl_stmt|;
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
name|dbname
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|dropDatabase
argument_list|(
name|dbname
argument_list|,
literal|false
argument_list|,
name|HCatClient
operator|.
name|DropDBMode
operator|.
name|RESTRICT
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|2
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dbname
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|DropDatabaseMessage
name|dropDatabaseMessage
init|=
name|md
operator|.
name|getDropDatabaseMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbname
argument_list|,
name|dropDatabaseMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"hcatcreatetable"
decl_stmt|;
name|HCatTable
name|table
init|=
operator|new
name|HCatTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|cols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"onecol"
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createTable
argument_list|(
name|HCatCreateTableDesc
operator|.
name|create
argument_list|(
name|table
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|1
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hcatcreatetable"
argument_list|,
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Parse the message field
name|CreateTableMessage
name|createTableMessage
init|=
name|md
operator|.
name|getCreateTableMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|createTableMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|createTableMessage
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|createTableMessage
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
comment|// fetch the table marked by the message and compare
name|HCatTable
name|createdTable
init|=
name|hCatClient
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|createdTable
operator|.
name|diff
argument_list|(
name|table
argument_list|)
operator|.
name|equals
argument_list|(
name|HCatTable
operator|.
name|NO_DIFF
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// TODO - Currently no way to test alter table, as this interface doesn't support alter table
annotation|@
name|Test
specifier|public
name|void
name|dropTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"hcatdroptable"
decl_stmt|;
name|HCatTable
name|table
init|=
operator|new
name|HCatTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|cols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"onecol"
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createTable
argument_list|(
name|HCatCreateTableDesc
operator|.
name|create
argument_list|(
name|table
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|2
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|DropTableMessage
name|dropTableMessage
init|=
name|md
operator|.
name|getDropTableMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|dropTableMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|dropTableMessage
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|dropTableMessage
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"hcataddparttable"
decl_stmt|;
name|String
name|partColName
init|=
literal|"pc"
decl_stmt|;
name|HCatTable
name|table
init|=
operator|new
name|HCatTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|partCol
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
name|partColName
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|cols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"onecol"
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createTable
argument_list|(
name|HCatCreateTableDesc
operator|.
name|create
argument_list|(
name|table
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|partName
init|=
literal|"testpart"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
name|partSpec
operator|.
name|put
argument_list|(
name|partColName
argument_list|,
name|partName
argument_list|)
expr_stmt|;
name|HCatPartition
name|part
init|=
operator|new
name|HCatPartition
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|hCatClient
operator|.
name|addPartition
argument_list|(
name|HCatAddPartitionDesc
operator|.
name|create
argument_list|(
name|part
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|2
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Parse the message field
name|AddPartitionMessage
name|addPartitionMessage
init|=
name|md
operator|.
name|getAddPartitionMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|addPartitionMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|addPartitionMessage
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|addPartitionMessage
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|ptndescs
init|=
name|addPartitionMessage
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
comment|// fetch the partition referred to by the message and compare
name|HCatPartition
name|addedPart
init|=
name|hCatClient
operator|.
name|getPartition
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|ptndescs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getTableName
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getValues
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getColumns
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getColumns
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getPartColumns
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getPartColumns
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|part
operator|.
name|getLocation
argument_list|()
argument_list|,
name|addedPart
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// TODO - currently no way to test alter partition, as HCatClient doesn't support it.
annotation|@
name|Test
specifier|public
name|void
name|dropPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"hcatdropparttable"
decl_stmt|;
name|String
name|partColName
init|=
literal|"pc"
decl_stmt|;
name|HCatTable
name|table
init|=
operator|new
name|HCatTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|partCol
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
name|partColName
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|cols
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"onecol"
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createTable
argument_list|(
name|HCatCreateTableDesc
operator|.
name|create
argument_list|(
name|table
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|partName
init|=
literal|"testpart"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
name|partSpec
operator|.
name|put
argument_list|(
name|partColName
argument_list|,
name|partName
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|addPartition
argument_list|(
name|HCatAddPartitionDesc
operator|.
name|create
argument_list|(
operator|new
name|HCatPartition
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|dropPartitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HCatNotificationEvent
name|event
init|=
name|events
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|3
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|event
operator|.
name|getEventTime
argument_list|()
operator|>=
name|startTime
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|,
name|event
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Parse the message field
name|DropPartitionMessage
name|dropPartitionMessage
init|=
name|md
operator|.
name|getDropPartitionMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbName
argument_list|,
name|dropPartitionMessage
operator|.
name|getDB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|dropPartitionMessage
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|,
name|dropPartitionMessage
operator|.
name|getTableType
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|droppedPartSpecs
init|=
name|dropPartitionMessage
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|droppedPartSpecs
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|droppedPartSpecs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|partSpec
argument_list|,
name|droppedPartSpecs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getOnlyMaxEvents
parameter_list|()
throws|throws
name|Exception
block|{
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatdb1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatdb2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatdb3"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|1
argument_list|,
name|events
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|2
argument_list|,
name|events
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|filter
parameter_list|()
throws|throws
name|Exception
block|{
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatf1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatf2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|dropDatabase
argument_list|(
literal|"hcatf2"
argument_list|,
literal|false
argument_list|,
name|HCatClient
operator|.
name|DropDBMode
operator|.
name|RESTRICT
argument_list|)
expr_stmt|;
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
init|=
operator|new
name|IMetaStoreClient
operator|.
name|NotificationFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
return|return
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|0
argument_list|,
name|filter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|3
argument_list|,
name|events
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|filterWithMax
parameter_list|()
throws|throws
name|Exception
block|{
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatm1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|createDatabase
argument_list|(
name|HCatCreateDBDesc
operator|.
name|create
argument_list|(
literal|"hcatm2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hCatClient
operator|.
name|dropDatabase
argument_list|(
literal|"hcatm2"
argument_list|,
literal|false
argument_list|,
name|HCatClient
operator|.
name|DropDBMode
operator|.
name|RESTRICT
argument_list|)
expr_stmt|;
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
init|=
operator|new
name|IMetaStoreClient
operator|.
name|NotificationFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
return|return
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|events
init|=
name|hCatClient
operator|.
name|getNextNotification
argument_list|(
name|firstEventId
argument_list|,
literal|1
argument_list|,
name|filter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|events
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|firstEventId
operator|+
literal|1
argument_list|,
name|events
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEventId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

