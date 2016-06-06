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
name|hive
operator|.
name|hcatalog
operator|.
name|listener
package|;
end_package

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
name|MetaStoreEventListener
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
name|RawStore
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
name|RawStoreProxy
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
name|InsertEvent
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
name|messaging
operator|.
name|MessageFactory
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

begin_comment
comment|/**  * An implementation of {@link org.apache.hadoop.hive.metastore.MetaStoreEventListener} that  * stores events in the database.  *  * Design overview:  This listener takes any event, builds a NotificationEventResponse,  * and puts it on a queue.  There is a dedicated thread that reads entries from the queue and  * places them in the database.  The reason for doing it in a separate thread is that we want to  * avoid slowing down other metadata operations with the work of putting the notification into  * the database.  Also, occasionally the thread needs to clean the database of old records.  We  * definitely don't want to do that as part of another metadata operation.  */
end_comment

begin_class
specifier|public
class|class
name|DbNotificationListener
extends|extends
name|MetaStoreEventListener
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
name|DbNotificationListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|CleanerThread
name|cleaner
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Object
name|NOTIFICATION_TBL_LOCK
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|// This is the same object as super.conf, but it's convenient to keep a copy of it as a
comment|// HiveConf rather than a Configuration.
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|MessageFactory
name|msgFactory
decl_stmt|;
specifier|private
name|RawStore
name|rs
decl_stmt|;
specifier|private
specifier|synchronized
name|void
name|init
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
try|try
block|{
name|rs
operator|=
name|RawStoreProxy
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|conf
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_RAW_STORE_IMPL
argument_list|)
argument_list|,
literal|999999
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to connect to raw store, notifications will not be tracked"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|rs
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|cleaner
operator|==
literal|null
operator|&&
name|rs
operator|!=
literal|null
condition|)
block|{
name|cleaner
operator|=
operator|new
name|CleanerThread
argument_list|(
name|conf
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|DbNotificationListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
comment|// The code in MetastoreUtils.getMetaStoreListeners() that calls this looks for a constructor
comment|// with a Configuration parameter, so we have to declare config as Configuration.  But it
comment|// actually passes a HiveConf, which we need.  So we'll do this ugly down cast.
name|hiveConf
operator|=
operator|(
name|HiveConf
operator|)
name|config
expr_stmt|;
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|msgFactory
operator|=
name|MessageFactory
operator|.
name|getInstance
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param tableEvent table event.    * @throws org.apache.hadoop.hive.metastore.api.MetaException    */
specifier|public
name|void
name|onConfigChange
parameter_list|(
name|ConfigChangeEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|key
init|=
name|tableEvent
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_DB_LISTENER_TTL
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
comment|// This weirdness of setting it in our hiveConf and then reading back does two things.
comment|// One, it handles the conversion of the TimeUnit.  Two, it keeps the value around for
comment|// later in case we need it again.
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_DB_LISTENER_TTL
operator|.
name|name
argument_list|()
argument_list|,
name|tableEvent
operator|.
name|getNewValue
argument_list|()
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setTimeToLive
argument_list|(
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_DB_LISTENER_TTL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param tableEvent table event.    * @throws MetaException    */
specifier|public
name|void
name|onCreateTable
parameter_list|(
name|CreateTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|t
init|=
name|tableEvent
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildCreateTableMessage
argument_list|(
name|t
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableEvent table event.    * @throws MetaException    */
specifier|public
name|void
name|onDropTable
parameter_list|(
name|DropTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|t
init|=
name|tableEvent
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildDropTableMessage
argument_list|(
name|t
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableEvent alter table event    * @throws MetaException    */
specifier|public
name|void
name|onAlterTable
parameter_list|(
name|AlterTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|before
init|=
name|tableEvent
operator|.
name|getOldTable
argument_list|()
decl_stmt|;
name|Table
name|after
init|=
name|tableEvent
operator|.
name|getNewTable
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ALTER_TABLE_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildAlterTableMessage
argument_list|(
name|before
argument_list|,
name|after
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|after
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|after
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param partitionEvent partition event    * @throws MetaException    */
specifier|public
name|void
name|onAddPartition
parameter_list|(
name|AddPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|t
init|=
name|partitionEvent
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildAddPartitionMessage
argument_list|(
name|t
argument_list|,
name|partitionEvent
operator|.
name|getPartitionIterator
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param partitionEvent partition event    * @throws MetaException    */
specifier|public
name|void
name|onDropPartition
parameter_list|(
name|DropPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|t
init|=
name|partitionEvent
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildDropPartitionMessage
argument_list|(
name|t
argument_list|,
name|partitionEvent
operator|.
name|getPartitionIterator
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param partitionEvent partition event    * @throws MetaException    */
specifier|public
name|void
name|onAlterPartition
parameter_list|(
name|AlterPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Partition
name|before
init|=
name|partitionEvent
operator|.
name|getOldPartition
argument_list|()
decl_stmt|;
name|Partition
name|after
init|=
name|partitionEvent
operator|.
name|getNewPartition
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_ALTER_PARTITION_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildAlterPartitionMessage
argument_list|(
name|partitionEvent
operator|.
name|getTable
argument_list|()
argument_list|,
name|before
argument_list|,
name|after
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|before
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|before
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param dbEvent database event    * @throws MetaException    */
specifier|public
name|void
name|onCreateDatabase
parameter_list|(
name|CreateDatabaseEvent
name|dbEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Database
name|db
init|=
name|dbEvent
operator|.
name|getDatabase
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildCreateDatabaseMessage
argument_list|(
name|db
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param dbEvent database event    * @throws MetaException    */
specifier|public
name|void
name|onDropDatabase
parameter_list|(
name|DropDatabaseEvent
name|dbEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|Database
name|db
init|=
name|dbEvent
operator|.
name|getDatabase
argument_list|()
decl_stmt|;
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildDropDatabaseMessage
argument_list|(
name|db
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onInsert
parameter_list|(
name|InsertEvent
name|insertEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|NotificationEvent
name|event
init|=
operator|new
name|NotificationEvent
argument_list|(
literal|0
argument_list|,
name|now
argument_list|()
argument_list|,
name|HCatConstants
operator|.
name|HCAT_INSERT_EVENT
argument_list|,
name|msgFactory
operator|.
name|buildInsertMessage
argument_list|(
name|insertEvent
operator|.
name|getDb
argument_list|()
argument_list|,
name|insertEvent
operator|.
name|getTable
argument_list|()
argument_list|,
name|insertEvent
operator|.
name|getPartitionKeyValues
argument_list|()
argument_list|,
name|insertEvent
operator|.
name|getFiles
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|event
operator|.
name|setDbName
argument_list|(
name|insertEvent
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
name|event
operator|.
name|setTableName
argument_list|(
name|insertEvent
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|enqueue
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param partSetDoneEvent    * @throws MetaException    */
specifier|public
name|void
name|onLoadPartitionDone
parameter_list|(
name|LoadPartitionDoneEvent
name|partSetDoneEvent
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// TODO, we don't support this, but we should, since users may create an empty partition and
comment|// then load data into it.
block|}
specifier|private
name|int
name|now
parameter_list|()
block|{
name|long
name|millis
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|millis
operator|/=
literal|1000
expr_stmt|;
if|if
condition|(
name|millis
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"We've passed max int value in seconds since the epoch, "
operator|+
literal|"all notification times will be the same!"
argument_list|)
expr_stmt|;
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
return|return
operator|(
name|int
operator|)
name|millis
return|;
block|}
specifier|private
name|void
name|enqueue
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|rs
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|NOTIFICATION_TBL_LOCK
init|)
block|{
name|rs
operator|.
name|addNotificationEvent
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Dropping event "
operator|+
name|event
operator|+
literal|" since notification is not running."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|CleanerThread
extends|extends
name|Thread
block|{
specifier|private
name|RawStore
name|rs
decl_stmt|;
specifier|private
name|int
name|ttl
decl_stmt|;
name|CleanerThread
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|RawStore
name|rs
parameter_list|)
block|{
name|super
argument_list|(
literal|"CleanerThread"
argument_list|)
expr_stmt|;
name|this
operator|.
name|rs
operator|=
name|rs
expr_stmt|;
name|setTimeToLive
argument_list|(
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_DB_LISTENER_TTL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
synchronized|synchronized
init|(
name|NOTIFICATION_TBL_LOCK
init|)
block|{
name|rs
operator|.
name|cleanNotificationEvents
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaner thread sleep interupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setTimeToLive
parameter_list|(
name|long
name|configTtl
parameter_list|)
block|{
if|if
condition|(
name|configTtl
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
name|ttl
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
else|else
name|ttl
operator|=
operator|(
name|int
operator|)
name|configTtl
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

