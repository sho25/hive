begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|streaming
operator|.
name|mutate
operator|.
name|client
operator|.
name|lock
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Timer
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|LockComponentBuilder
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
name|LockRequestBuilder
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
name|LockComponent
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
name|LockRequest
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
name|LockResponse
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
name|LockState
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
name|thrift
operator|.
name|TException
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

begin_comment
comment|/**  * Manages the state required to safely read/write from/to an ACID table.  */
end_comment

begin_class
specifier|public
class|class
name|Lock
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
name|Lock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|HEARTBEAT_FACTOR
init|=
literal|0.75
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_HEARTBEAT_PERIOD
init|=
literal|275
decl_stmt|;
specifier|private
specifier|final
name|IMetaStoreClient
name|metaStoreClient
decl_stmt|;
specifier|private
specifier|final
name|HeartbeatFactory
name|heartbeatFactory
decl_stmt|;
specifier|private
specifier|final
name|LockFailureListener
name|listener
decl_stmt|;
specifier|private
specifier|final
name|Collection
argument_list|<
name|Table
argument_list|>
name|sinks
decl_stmt|;
specifier|private
specifier|final
name|Collection
argument_list|<
name|Table
argument_list|>
name|tables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|lockRetries
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryWaitSeconds
decl_stmt|;
specifier|private
specifier|final
name|String
name|user
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Timer
name|heartbeat
decl_stmt|;
specifier|private
name|Long
name|lockId
decl_stmt|;
specifier|private
name|Long
name|transactionId
decl_stmt|;
specifier|public
name|Lock
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|Options
name|options
parameter_list|)
block|{
name|this
argument_list|(
name|metaStoreClient
argument_list|,
operator|new
name|HeartbeatFactory
argument_list|()
argument_list|,
name|options
operator|.
name|hiveConf
argument_list|,
name|options
operator|.
name|listener
argument_list|,
name|options
operator|.
name|user
argument_list|,
name|options
operator|.
name|sources
argument_list|,
name|options
operator|.
name|sinks
argument_list|,
name|options
operator|.
name|lockRetries
argument_list|,
name|options
operator|.
name|retryWaitSeconds
argument_list|)
expr_stmt|;
block|}
comment|/** Visible for testing only. */
name|Lock
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HeartbeatFactory
name|heartbeatFactory
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|LockFailureListener
name|listener
parameter_list|,
name|String
name|user
parameter_list|,
name|Collection
argument_list|<
name|Table
argument_list|>
name|sources
parameter_list|,
name|Collection
argument_list|<
name|Table
argument_list|>
name|sinks
parameter_list|,
name|int
name|lockRetries
parameter_list|,
name|int
name|retryWaitSeconds
parameter_list|)
block|{
name|this
operator|.
name|metaStoreClient
operator|=
name|metaStoreClient
expr_stmt|;
name|this
operator|.
name|heartbeatFactory
operator|=
name|heartbeatFactory
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|lockRetries
operator|=
name|lockRetries
expr_stmt|;
name|this
operator|.
name|retryWaitSeconds
operator|=
name|retryWaitSeconds
expr_stmt|;
name|this
operator|.
name|sinks
operator|=
name|sinks
expr_stmt|;
name|tables
operator|.
name|addAll
argument_list|(
name|sources
argument_list|)
expr_stmt|;
name|tables
operator|.
name|addAll
argument_list|(
name|sinks
argument_list|)
expr_stmt|;
if|if
condition|(
name|LockFailureListener
operator|.
name|NULL_LISTENER
operator|.
name|equals
argument_list|(
name|listener
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No {} supplied. Data quality and availability cannot be assured."
argument_list|,
name|LockFailureListener
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Attempts to acquire a read lock on the table, returns if successful, throws exception otherwise. */
specifier|public
name|void
name|acquire
parameter_list|()
throws|throws
name|LockException
block|{
name|lockId
operator|=
name|internalAcquire
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|initiateHeartbeat
argument_list|()
expr_stmt|;
block|}
comment|/** Attempts to acquire a read lock on the table, returns if successful, throws exception otherwise. */
specifier|public
name|void
name|acquire
parameter_list|(
name|long
name|transactionId
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
name|transactionId
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid transaction id: "
operator|+
name|transactionId
argument_list|)
throw|;
block|}
name|lockId
operator|=
name|internalAcquire
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|transactionId
operator|=
name|transactionId
expr_stmt|;
name|initiateHeartbeat
argument_list|()
expr_stmt|;
block|}
comment|/** Attempts to release the read lock on the table. Throws an exception if the lock failed at any point. */
specifier|public
name|void
name|release
parameter_list|()
throws|throws
name|LockException
block|{
if|if
condition|(
name|heartbeat
operator|!=
literal|null
condition|)
block|{
name|heartbeat
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
name|internalRelease
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Lock [metaStoreClient="
operator|+
name|metaStoreClient
operator|+
literal|", lockId="
operator|+
name|lockId
operator|+
literal|", transactionId="
operator|+
name|transactionId
operator|+
literal|"]"
return|;
block|}
specifier|private
name|long
name|internalAcquire
parameter_list|(
name|Long
name|transactionId
parameter_list|)
throws|throws
name|LockException
block|{
name|int
name|attempts
init|=
literal|0
decl_stmt|;
name|LockRequest
name|request
init|=
name|buildLockRequest
argument_list|(
name|transactionId
argument_list|)
decl_stmt|;
do|do
block|{
name|LockResponse
name|response
init|=
literal|null
decl_stmt|;
try|try
block|{
name|response
operator|=
name|metaStoreClient
operator|.
name|lock
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
literal|"Unable to acquire lock for tables: ["
operator|+
name|join
argument_list|(
name|tables
argument_list|)
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|response
operator|!=
literal|null
condition|)
block|{
name|LockState
name|state
init|=
name|response
operator|.
name|getState
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
operator|==
name|LockState
operator|.
name|NOT_ACQUIRED
operator|||
name|state
operator|==
name|LockState
operator|.
name|ABORT
condition|)
block|{
comment|// I expect we'll only see NOT_ACQUIRED here?
break|break;
block|}
if|if
condition|(
name|state
operator|==
name|LockState
operator|.
name|ACQUIRED
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Acquired lock {}"
argument_list|,
name|response
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|response
operator|.
name|getLockid
argument_list|()
return|;
block|}
if|if
condition|(
name|state
operator|==
name|LockState
operator|.
name|WAITING
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toMillis
argument_list|(
name|retryWaitSeconds
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{           }
block|}
block|}
name|attempts
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|attempts
operator|<
name|lockRetries
condition|)
do|;
throw|throw
operator|new
name|LockException
argument_list|(
literal|"Could not acquire lock on tables: ["
operator|+
name|join
argument_list|(
name|tables
argument_list|)
operator|+
literal|"]"
argument_list|)
throw|;
block|}
specifier|private
name|void
name|internalRelease
parameter_list|()
block|{
try|try
block|{
comment|// if there is a transaction then this lock will be released on commit/abort/rollback instead.
if|if
condition|(
name|lockId
operator|!=
literal|null
operator|&&
name|transactionId
operator|==
literal|null
condition|)
block|{
name|metaStoreClient
operator|.
name|unlock
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Released lock {}"
argument_list|,
name|lockId
argument_list|)
expr_stmt|;
name|lockId
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Lock "
operator|+
name|lockId
operator|+
literal|" failed."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|listener
operator|.
name|lockFailed
argument_list|(
name|lockId
argument_list|,
name|transactionId
argument_list|,
name|asStrings
argument_list|(
name|tables
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|LockRequest
name|buildLockRequest
parameter_list|(
name|Long
name|transactionId
parameter_list|)
block|{
if|if
condition|(
name|transactionId
operator|==
literal|null
operator|&&
operator|!
name|sinks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot sink to tables outside of a transaction: sinks="
operator|+
name|asStrings
argument_list|(
name|sinks
argument_list|)
argument_list|)
throw|;
block|}
name|LockRequestBuilder
name|requestBuilder
init|=
operator|new
name|LockRequestBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Table
name|table
range|:
name|tables
control|)
block|{
name|LockComponentBuilder
name|componentBuilder
init|=
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|setTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sinks
operator|.
name|contains
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|componentBuilder
operator|.
name|setSemiShared
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|componentBuilder
operator|.
name|setShared
argument_list|()
expr_stmt|;
block|}
name|LockComponent
name|component
init|=
name|componentBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|requestBuilder
operator|.
name|addLockComponent
argument_list|(
name|component
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|transactionId
operator|!=
literal|null
condition|)
block|{
name|requestBuilder
operator|.
name|setTransactionId
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
block|}
name|LockRequest
name|request
init|=
name|requestBuilder
operator|.
name|setUser
argument_list|(
name|user
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|request
return|;
block|}
specifier|private
name|void
name|initiateHeartbeat
parameter_list|()
block|{
name|int
name|heartbeatPeriod
init|=
name|getHeartbeatPeriod
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Heartbeat period {}s"
argument_list|,
name|heartbeatPeriod
argument_list|)
expr_stmt|;
name|heartbeat
operator|=
name|heartbeatFactory
operator|.
name|newInstance
argument_list|(
name|metaStoreClient
argument_list|,
name|listener
argument_list|,
name|transactionId
argument_list|,
name|tables
argument_list|,
name|lockId
argument_list|,
name|heartbeatPeriod
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getHeartbeatPeriod
parameter_list|()
block|{
name|int
name|heartbeatPeriod
init|=
name|DEFAULT_HEARTBEAT_PERIOD
decl_stmt|;
if|if
condition|(
name|hiveConf
operator|!=
literal|null
condition|)
block|{
comment|// This value is always in seconds and includes an 's' suffix.
name|String
name|txTimeoutSeconds
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_TIMEOUT
argument_list|)
decl_stmt|;
if|if
condition|(
name|txTimeoutSeconds
operator|!=
literal|null
condition|)
block|{
comment|// We want to send the heartbeat at an interval that is less than the timeout.
name|heartbeatPeriod
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
call|(
name|int
call|)
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|txTimeoutSeconds
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|txTimeoutSeconds
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|HEARTBEAT_FACTOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|heartbeatPeriod
return|;
block|}
comment|/** Visible for testing only. */
name|Long
name|getLockId
parameter_list|()
block|{
return|return
name|lockId
return|;
block|}
comment|/** Visible for testing only. */
name|Long
name|getTransactionId
parameter_list|()
block|{
return|return
name|transactionId
return|;
block|}
comment|/** Visible for testing only. */
specifier|static
name|String
name|join
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Object
argument_list|>
name|values
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|values
argument_list|,
literal|","
argument_list|)
return|;
block|}
comment|/** Visible for testing only. */
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|asStrings
parameter_list|(
name|Collection
argument_list|<
name|Table
argument_list|>
name|tables
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|strings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tables
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Table
name|descriptor
range|:
name|tables
control|)
block|{
name|strings
operator|.
name|add
argument_list|(
name|descriptor
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|descriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|strings
return|;
block|}
comment|/** Constructs a lock options for a set of Hive ACID tables from which we wish to read. */
specifier|public
specifier|static
specifier|final
class|class
name|Options
block|{
name|Set
argument_list|<
name|Table
argument_list|>
name|sources
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Table
argument_list|>
name|sinks
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|LockFailureListener
name|listener
init|=
name|LockFailureListener
operator|.
name|NULL_LISTENER
decl_stmt|;
name|int
name|lockRetries
init|=
literal|5
decl_stmt|;
name|int
name|retryWaitSeconds
init|=
literal|30
decl_stmt|;
name|String
name|user
decl_stmt|;
name|HiveConf
name|hiveConf
decl_stmt|;
comment|/** Adds a table for which a shared lock will be requested. */
specifier|public
name|Options
name|addSourceTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|addTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
name|sources
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Adds a table for which a semi-shared lock will be requested. */
specifier|public
name|Options
name|addSinkTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|addTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
name|sinks
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|void
name|addTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Set
argument_list|<
name|Table
argument_list|>
name|tables
parameter_list|)
block|{
name|checkNotNullOrEmpty
argument_list|(
name|databaseName
argument_list|)
expr_stmt|;
name|checkNotNullOrEmpty
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|databaseName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tables
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Options
name|user
parameter_list|(
name|String
name|user
parameter_list|)
block|{
name|checkNotNullOrEmpty
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Options
name|configuration
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Sets a listener to handle failures of locks that were previously acquired. */
specifier|public
name|Options
name|lockFailureListener
parameter_list|(
name|LockFailureListener
name|listener
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Options
name|lockRetries
parameter_list|(
name|int
name|lockRetries
parameter_list|)
block|{
name|checkArgument
argument_list|(
name|lockRetries
operator|>
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockRetries
operator|=
name|lockRetries
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Options
name|retryWaitSeconds
parameter_list|(
name|int
name|retryWaitSeconds
parameter_list|)
block|{
name|checkArgument
argument_list|(
name|retryWaitSeconds
operator|>
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryWaitSeconds
operator|=
name|retryWaitSeconds
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
specifier|static
name|void
name|checkArgument
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkNotNull
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkNotNullOrEmpty
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|value
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

