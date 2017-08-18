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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|EnvironmentContext
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
name|events
operator|.
name|AddForeignKeyEvent
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
name|AddNotNullConstraintEvent
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
name|AddPrimaryKeyEvent
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
name|AddUniqueConstraintEvent
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
name|CreateFunctionEvent
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
name|DropFunctionEvent
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
name|ListenerEvent
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
import|import static
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
name|MetaStoreEventListenerConstants
operator|.
name|HIVE_METASTORE_TRANSACTION_ACTIVE
import|;
end_import

begin_import
import|import static
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
name|messaging
operator|.
name|EventMessage
operator|.
name|EventType
import|;
end_import

begin_comment
comment|/**  * This class is used to notify a list of listeners about specific MetaStore events.  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|MetaStoreListenerNotifier
block|{
specifier|private
interface|interface
name|EventNotifier
block|{
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
function_decl|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|EventType
argument_list|,
name|EventNotifier
argument_list|>
name|notificationEvents
init|=
name|Maps
operator|.
name|newHashMap
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|EventType
argument_list|,
name|EventNotifier
operator|>
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|CREATE_DATABASE
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onCreateDatabase
argument_list|(
operator|(
name|CreateDatabaseEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|DROP_DATABASE
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onDropDatabase
argument_list|(
operator|(
name|DropDatabaseEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|CREATE_TABLE
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onCreateTable
argument_list|(
operator|(
name|CreateTableEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|DROP_TABLE
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onDropTable
argument_list|(
operator|(
name|DropTableEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ADD_PARTITION
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddPartition
argument_list|(
operator|(
name|AddPartitionEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|DROP_PARTITION
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onDropPartition
argument_list|(
operator|(
name|DropPartitionEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ALTER_TABLE
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAlterTable
argument_list|(
operator|(
name|AlterTableEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ALTER_PARTITION
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAlterPartition
argument_list|(
operator|(
name|AlterPartitionEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|INSERT
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onInsert
argument_list|(
operator|(
name|InsertEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|CREATE_FUNCTION
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onCreateFunction
argument_list|(
operator|(
name|CreateFunctionEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|DROP_FUNCTION
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onDropFunction
argument_list|(
operator|(
name|DropFunctionEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|CREATE_INDEX
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddIndex
argument_list|(
operator|(
name|AddIndexEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|DROP_INDEX
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onDropIndex
argument_list|(
operator|(
name|DropIndexEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ALTER_INDEX
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAlterIndex
argument_list|(
operator|(
name|AlterIndexEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ADD_PRIMARYKEY
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddPrimaryKey
argument_list|(
operator|(
name|AddPrimaryKeyEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ADD_FOREIGNKEY
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddForeignKey
argument_list|(
operator|(
name|AddForeignKeyEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ADD_UNIQUECONSTRAINT
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddUniqueConstraint
argument_list|(
operator|(
name|AddUniqueConstraintEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|put
argument_list|(
name|EventType
operator|.
name|ADD_NOTNULLCONSTRAINT
argument_list|,
operator|new
name|EventNotifier
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|notify
parameter_list|(
name|MetaStoreEventListener
name|listener
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|listener
operator|.
name|onAddNotNullConstraint
argument_list|(
operator|(
name|AddNotNullConstraintEvent
operator|)
name|event
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Notify a list of listeners about a specific metastore event. Each listener notified might update    * the (ListenerEvent) event by setting a parameter key/value pair. These updated parameters will    * be returned to the caller.    *    * @param listeners List of MetaStoreEventListener listeners.    * @param eventType Type of the notification event.    * @param event The ListenerEvent with information about the event.    * @return A list of key/value pair parameters that the listeners set. The returned object will return an empty    *         map if no parameters were updated or if no listeners were notified.    * @throws MetaException If an error occurred while calling the listeners.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|notifyEvent
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|MetaStoreEventListener
argument_list|>
name|listeners
parameter_list|,
name|EventType
name|eventType
parameter_list|,
name|ListenerEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|listeners
argument_list|,
literal|"Listeners must not be null."
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|event
argument_list|,
literal|"The event must not be null."
argument_list|)
expr_stmt|;
for|for
control|(
name|MetaStoreEventListener
name|listener
range|:
name|listeners
control|)
block|{
name|notificationEvents
operator|.
name|get
argument_list|(
name|eventType
argument_list|)
operator|.
name|notify
argument_list|(
name|listener
argument_list|,
name|event
argument_list|)
expr_stmt|;
block|}
comment|// Each listener called above might set a different parameter on the event.
comment|// This write permission is allowed on the listener side to avoid breaking compatibility if we change the API
comment|// method calls.
return|return
name|event
operator|.
name|getParameters
argument_list|()
return|;
block|}
comment|/**    * Notify a list of listeners about a specific metastore event. Each listener notified might update    * the (ListenerEvent) event by setting a parameter key/value pair. These updated parameters will    * be returned to the caller.    *    * @param listeners List of MetaStoreEventListener listeners.    * @param eventType Type of the notification event.    * @param event The ListenerEvent with information about the event.    * @param environmentContext An EnvironmentContext object with parameters sent by the HMS client.    * @return A list of key/value pair parameters that the listeners set. The returned object will return an empty    *         map if no parameters were updated or if no listeners were notified.    * @throws MetaException If an error occurred while calling the listeners.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|notifyEvent
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|MetaStoreEventListener
argument_list|>
name|listeners
parameter_list|,
name|EventType
name|eventType
parameter_list|,
name|ListenerEvent
name|event
parameter_list|,
name|EnvironmentContext
name|environmentContext
parameter_list|)
throws|throws
name|MetaException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|event
argument_list|,
literal|"The event must not be null."
argument_list|)
expr_stmt|;
name|event
operator|.
name|setEnvironmentContext
argument_list|(
name|environmentContext
argument_list|)
expr_stmt|;
return|return
name|notifyEvent
argument_list|(
name|listeners
argument_list|,
name|eventType
argument_list|,
name|event
argument_list|)
return|;
block|}
comment|/**    * Notify a list of listeners about a specific metastore event. Each listener notified might update    * the (ListenerEvent) event by setting a parameter key/value pair. These updated parameters will    * be returned to the caller.    *    * Sometimes these events are run inside a DB transaction and might cause issues with the listeners,    * for instance, Sentry blocks the HMS until an event is seen committed on the DB. To notify the listener about this,    * a new parameter to verify if a transaction is active is added to the ListenerEvent, and is up to the listener    * to skip this notification if so.    *    * @param listeners List of MetaStoreEventListener listeners.    * @param eventType Type of the notification event.    * @param event The ListenerEvent with information about the event.    * @param environmentContext An EnvironmentContext object with parameters sent by the HMS client.    * @param parameters A list of key/value pairs with the new parameters to add.    * @param ms The RawStore object from where to check if a transaction is active.    * @return A list of key/value pair parameters that the listeners set. The returned object will return an empty    *         map if no parameters were updated or if no listeners were notified.    * @throws MetaException If an error occurred while calling the listeners.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|notifyEvent
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|MetaStoreEventListener
argument_list|>
name|listeners
parameter_list|,
name|EventType
name|eventType
parameter_list|,
name|ListenerEvent
name|event
parameter_list|,
name|EnvironmentContext
name|environmentContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
specifier|final
name|RawStore
name|ms
parameter_list|)
throws|throws
name|MetaException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|event
argument_list|,
literal|"The event must not be null."
argument_list|)
expr_stmt|;
name|event
operator|.
name|putParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
if|if
condition|(
name|ms
operator|!=
literal|null
condition|)
block|{
name|event
operator|.
name|putParameter
argument_list|(
name|HIVE_METASTORE_TRANSACTION_ACTIVE
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|ms
operator|.
name|isActiveTransaction
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|notifyEvent
argument_list|(
name|listeners
argument_list|,
name|eventType
argument_list|,
name|event
argument_list|,
name|environmentContext
argument_list|)
return|;
block|}
block|}
end_class

end_unit

