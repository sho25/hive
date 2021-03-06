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
name|java
operator|.
name|util
operator|.
name|List
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
name|Function
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
name|InvalidObjectException
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
name|NotificationEventRequest
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
name|NotificationEventResponse
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
name|SQLForeignKey
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
name|SQLPrimaryKey
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
name|CurrentNotificationEventId
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

begin_comment
comment|/**  * A wrapper around {@link ObjectStore} that allows us to inject custom behaviour  * on to some of the methods for testing.  */
end_comment

begin_class
specifier|public
class|class
name|InjectableBehaviourObjectStore
extends|extends
name|ObjectStore
block|{
specifier|public
name|InjectableBehaviourObjectStore
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * A utility class that allows people injecting behaviour to determine if their injections occurred.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|BehaviourInjection
parameter_list|<
name|T
parameter_list|,
name|F
parameter_list|>
implements|implements
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|T
argument_list|,
name|F
argument_list|>
block|{
specifier|protected
name|boolean
name|injectionPathCalled
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|nonInjectedPathCalled
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|assertInjectionsPerformed
parameter_list|(
name|boolean
name|expectedInjectionCalled
parameter_list|,
name|boolean
name|expectedNonInjectedPathCalled
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedInjectionCalled
argument_list|,
name|injectionPathCalled
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNonInjectedPathCalled
argument_list|,
name|nonInjectedPathCalled
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * A utility class to pass the arguments of the caller to the stub method.    */
specifier|public
class|class
name|CallerArguments
block|{
specifier|public
name|String
name|dbName
decl_stmt|;
specifier|public
name|String
name|tblName
decl_stmt|;
specifier|public
name|String
name|funcName
decl_stmt|;
specifier|public
name|String
name|constraintTblName
decl_stmt|;
specifier|public
name|CallerArguments
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|Table
argument_list|,
name|Table
argument_list|>
name|getTableModifier
init|=
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|Partition
argument_list|,
name|Partition
argument_list|>
name|getPartitionModifier
init|=
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listPartitionNamesModifier
init|=
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|NotificationEventResponse
argument_list|,
name|NotificationEventResponse
argument_list|>
name|getNextNotificationModifier
init|=
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
name|callerVerifier
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|NotificationEvent
argument_list|,
name|Boolean
argument_list|>
name|addNotificationEventModifier
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
name|alterTableModifier
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CurrentNotificationEventId
argument_list|,
name|CurrentNotificationEventId
argument_list|>
name|getCurrNotiEventIdModifier
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|List
argument_list|<
name|Partition
argument_list|>
argument_list|,
name|Boolean
argument_list|>
name|alterPartitionsModifier
init|=
literal|null
decl_stmt|;
comment|// Methods to set/reset getTable modifier
specifier|public
specifier|static
name|void
name|setGetTableBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|Table
argument_list|,
name|Table
argument_list|>
name|modifier
parameter_list|)
block|{
name|getTableModifier
operator|=
operator|(
name|modifier
operator|==
literal|null
operator|)
condition|?
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
else|:
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetGetTableBehaviour
parameter_list|()
block|{
name|setGetTableBehaviour
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Methods to set/reset getPartition modifier
specifier|public
specifier|static
name|void
name|setGetPartitionBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|Partition
argument_list|,
name|Partition
argument_list|>
name|modifier
parameter_list|)
block|{
name|getPartitionModifier
operator|=
operator|(
name|modifier
operator|==
literal|null
operator|)
condition|?
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
else|:
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetGetPartitionBehaviour
parameter_list|()
block|{
name|setGetPartitionBehaviour
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Methods to set/reset listPartitionNames modifier
specifier|public
specifier|static
name|void
name|setListPartitionNamesBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|modifier
parameter_list|)
block|{
name|listPartitionNamesModifier
operator|=
operator|(
name|modifier
operator|==
literal|null
operator|)
condition|?
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
else|:
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetListPartitionNamesBehaviour
parameter_list|()
block|{
name|setListPartitionNamesBehaviour
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Methods to set/reset getNextNotification modifier
specifier|public
specifier|static
name|void
name|setGetNextNotificationBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|NotificationEventResponse
argument_list|,
name|NotificationEventResponse
argument_list|>
name|modifier
parameter_list|)
block|{
name|getNextNotificationModifier
operator|=
operator|(
name|modifier
operator|==
literal|null
operator|)
condition|?
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Functions
operator|.
name|identity
argument_list|()
else|:
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setAddNotificationModifier
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|NotificationEvent
argument_list|,
name|Boolean
argument_list|>
name|modifier
parameter_list|)
block|{
name|addNotificationEventModifier
operator|=
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetAddNotificationModifier
parameter_list|()
block|{
name|setAddNotificationModifier
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetGetNextNotificationBehaviour
parameter_list|()
block|{
name|setGetNextNotificationBehaviour
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Methods to set/reset caller checker
specifier|public
specifier|static
name|void
name|setCallerVerifier
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
name|verifier
parameter_list|)
block|{
name|callerVerifier
operator|=
name|verifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetCallerVerifier
parameter_list|()
block|{
name|setCallerVerifier
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setAlterTableModifier
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
name|modifier
parameter_list|)
block|{
name|alterTableModifier
operator|=
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetAlterTableModifier
parameter_list|()
block|{
name|setAlterTableModifier
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setAlterPartitionsBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|List
argument_list|<
name|Partition
argument_list|>
argument_list|,
name|Boolean
argument_list|>
name|modifier
parameter_list|)
block|{
name|alterPartitionsModifier
operator|=
name|modifier
expr_stmt|;
block|}
comment|// ObjectStore methods to be overridden with injected behavior
annotation|@
name|Override
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|getTableModifier
operator|.
name|apply
argument_list|(
name|super
operator|.
name|getTable
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|writeIdList
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|getTableModifier
operator|.
name|apply
argument_list|(
name|super
operator|.
name|getTable
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|writeIdList
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
block|{
return|return
name|getPartitionModifier
operator|.
name|apply
argument_list|(
name|super
operator|.
name|getPartition
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partVals
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|short
name|max
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|listPartitionNamesModifier
operator|.
name|apply
argument_list|(
name|super
operator|.
name|listPartitionNames
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|max
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|NotificationEventResponse
name|getNextNotification
parameter_list|(
name|NotificationEventRequest
name|rqst
parameter_list|)
block|{
return|return
name|getNextNotificationModifier
operator|.
name|apply
argument_list|(
name|super
operator|.
name|getNextNotification
argument_list|(
name|rqst
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|alterTable
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|Table
name|newTable
parameter_list|,
name|String
name|queryValidWriteIds
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|alterTableModifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|dbname
argument_list|)
decl_stmt|;
name|args
operator|.
name|tblName
operator|=
name|name
expr_stmt|;
name|Boolean
name|success
init|=
name|alterTableModifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid alterTable operation on Catalog : "
operator|+
name|catName
operator|+
literal|" DB: "
operator|+
name|dbname
operator|+
literal|" table: "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|alterTable
argument_list|(
name|catName
argument_list|,
name|dbname
argument_list|,
name|name
argument_list|,
name|newTable
argument_list|,
name|queryValidWriteIds
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addNotificationEvent
parameter_list|(
name|NotificationEvent
name|entry
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|addNotificationEventModifier
operator|!=
literal|null
condition|)
block|{
name|Boolean
name|success
init|=
name|addNotificationEventModifier
operator|.
name|apply
argument_list|(
name|entry
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid addNotificationEvent operation on DB: "
operator|+
name|entry
operator|.
name|getDbName
argument_list|()
operator|+
literal|" table: "
operator|+
name|entry
operator|.
name|getTableName
argument_list|()
operator|+
literal|" event : "
operator|+
name|entry
operator|.
name|getEventType
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|addNotificationEvent
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|callerVerifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|)
decl_stmt|;
name|args
operator|.
name|tblName
operator|=
name|tbl
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|Boolean
name|success
init|=
name|callerVerifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid Create Table operation on DB: "
operator|+
name|args
operator|.
name|dbName
operator|+
literal|" table: "
operator|+
name|args
operator|.
name|tblName
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createFunction
parameter_list|(
name|Function
name|func
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|callerVerifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|func
operator|.
name|getDbName
argument_list|()
argument_list|)
decl_stmt|;
name|args
operator|.
name|funcName
operator|=
name|func
operator|.
name|getFunctionName
argument_list|()
expr_stmt|;
name|Boolean
name|success
init|=
name|callerVerifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid Create Function operation on DB: "
operator|+
name|args
operator|.
name|dbName
operator|+
literal|" function: "
operator|+
name|args
operator|.
name|funcName
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|createFunction
argument_list|(
name|func
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|addPrimaryKeys
parameter_list|(
name|List
argument_list|<
name|SQLPrimaryKey
argument_list|>
name|pks
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|callerVerifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|pks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_db
argument_list|()
argument_list|)
decl_stmt|;
name|args
operator|.
name|constraintTblName
operator|=
name|pks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_name
argument_list|()
expr_stmt|;
name|Boolean
name|success
init|=
name|callerVerifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid Add Primary Key operation on DB: "
operator|+
name|args
operator|.
name|dbName
operator|+
literal|" table: "
operator|+
name|args
operator|.
name|constraintTblName
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|addPrimaryKeys
argument_list|(
name|pks
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|addForeignKeys
parameter_list|(
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|fks
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|callerVerifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|fks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFktable_db
argument_list|()
argument_list|)
decl_stmt|;
name|args
operator|.
name|constraintTblName
operator|=
name|fks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFktable_name
argument_list|()
expr_stmt|;
name|Boolean
name|success
init|=
name|callerVerifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid Add Foreign Key operation on DB: "
operator|+
name|args
operator|.
name|dbName
operator|+
literal|" table: "
operator|+
name|args
operator|.
name|constraintTblName
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|addForeignKeys
argument_list|(
name|fks
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|alterDatabase
parameter_list|(
name|String
name|catalogName
parameter_list|,
name|String
name|dbname
parameter_list|,
name|Database
name|db
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|callerVerifier
operator|!=
literal|null
condition|)
block|{
name|CallerArguments
name|args
init|=
operator|new
name|CallerArguments
argument_list|(
name|dbname
argument_list|)
decl_stmt|;
name|callerVerifier
operator|.
name|apply
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|alterDatabase
argument_list|(
name|catalogName
argument_list|,
name|dbname
argument_list|,
name|db
argument_list|)
return|;
block|}
comment|// Methods to set/reset getCurrentNotificationEventId modifier
specifier|public
specifier|static
name|void
name|setGetCurrentNotificationEventIdBehaviour
parameter_list|(
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|CurrentNotificationEventId
argument_list|,
name|CurrentNotificationEventId
argument_list|>
name|modifier
parameter_list|)
block|{
name|getCurrNotiEventIdModifier
operator|=
name|modifier
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|resetGetCurrentNotificationEventIdBehaviour
parameter_list|()
block|{
name|setGetCurrentNotificationEventIdBehaviour
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CurrentNotificationEventId
name|getCurrentNotificationEventId
parameter_list|()
block|{
name|CurrentNotificationEventId
name|id
init|=
name|super
operator|.
name|getCurrentNotificationEventId
argument_list|()
decl_stmt|;
if|if
condition|(
name|getCurrNotiEventIdModifier
operator|!=
literal|null
condition|)
block|{
name|id
operator|=
name|getCurrNotiEventIdModifier
operator|.
name|apply
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid getCurrentNotificationEventId"
argument_list|)
throw|;
block|}
block|}
return|return
name|id
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|alterPartitions
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|part_vals
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|newParts
parameter_list|,
name|long
name|writeId
parameter_list|,
name|String
name|queryWriteIdList
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
if|if
condition|(
name|alterPartitionsModifier
operator|!=
literal|null
condition|)
block|{
name|Boolean
name|success
init|=
name|alterPartitionsModifier
operator|.
name|apply
argument_list|(
name|newParts
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|success
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"InjectableBehaviourObjectStore: Invalid alterPartitions operation on Catalog : "
operator|+
name|catName
operator|+
literal|" DB: "
operator|+
name|dbname
operator|+
literal|" table: "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|alterPartitions
argument_list|(
name|catName
argument_list|,
name|dbname
argument_list|,
name|name
argument_list|,
name|part_vals
argument_list|,
name|newParts
argument_list|,
name|writeId
argument_list|,
name|queryWriteIdList
argument_list|)
return|;
block|}
block|}
end_class

end_unit

