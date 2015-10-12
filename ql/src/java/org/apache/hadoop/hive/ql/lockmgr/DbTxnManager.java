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
name|ql
operator|.
name|lockmgr
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|JavaUtils
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
name|ValidTxnList
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
name|*
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
name|Context
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
name|ErrorMsg
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
name|QueryPlan
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
name|hooks
operator|.
name|Entity
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|HiveException
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
name|metadata
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
name|List
import|;
end_import

begin_comment
comment|/**  * An implementation of HiveTxnManager that stores the transactions in the  * metastore database.  */
end_comment

begin_class
specifier|public
class|class
name|DbTxnManager
extends|extends
name|HiveTxnManagerImpl
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|DbTxnManager
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|private
name|DbLockManager
name|lockMgr
init|=
literal|null
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|client
init|=
literal|null
decl_stmt|;
comment|/**    * The Metastore NEXT_TXN_ID.NTXN_NEXT is initialized to 1; it contains the next available    * transaction id.  Thus is 1 is first transaction id.    */
specifier|private
name|long
name|txnId
init|=
literal|0
decl_stmt|;
comment|/**    * assigns a unique monotonically increasing ID to each statement    * which is part of an open transaction.  This is used by storage    * layer (see {@link org.apache.hadoop.hive.ql.io.AcidUtils#deltaSubdir(long, long, int)})    * to keep apart multiple writes of the same data within the same transaction    * Also see {@link org.apache.hadoop.hive.ql.io.AcidOutputFormat.Options}    */
specifier|private
name|int
name|statementId
init|=
operator|-
literal|1
decl_stmt|;
name|DbTxnManager
parameter_list|()
block|{   }
annotation|@
name|Override
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setHiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ErrorMsg
operator|.
name|DBTXNMGR_REQUIRES_CONCURRENCY
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|openTxn
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|LockException
block|{
name|init
argument_list|()
expr_stmt|;
if|if
condition|(
name|isTxnOpen
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
literal|"Transaction already opened. "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
name|txnId
operator|=
name|client
operator|.
name|openTxn
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|statementId
operator|=
literal|0
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opened "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|txnId
return|;
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
name|e
argument_list|,
name|ErrorMsg
operator|.
name|METASTORE_COMMUNICATION_FAILED
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HiveLockManager
name|getLockManager
parameter_list|()
throws|throws
name|LockException
block|{
name|init
argument_list|()
expr_stmt|;
if|if
condition|(
name|lockMgr
operator|==
literal|null
condition|)
block|{
name|lockMgr
operator|=
operator|new
name|DbLockManager
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
return|return
name|lockMgr
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|acquireLocks
parameter_list|(
name|QueryPlan
name|plan
parameter_list|,
name|Context
name|ctx
parameter_list|,
name|String
name|username
parameter_list|)
throws|throws
name|LockException
block|{
name|acquireLocks
argument_list|(
name|plan
argument_list|,
name|ctx
argument_list|,
name|username
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is for testing only.  Normally client should call {@link #acquireLocks(org.apache.hadoop.hive.ql.QueryPlan, org.apache.hadoop.hive.ql.Context, String)}    * @param isBlocking if false, the method will return immediately; thus the locks may be in LockState.WAITING    * @return null if no locks were needed    */
name|LockState
name|acquireLocks
parameter_list|(
name|QueryPlan
name|plan
parameter_list|,
name|Context
name|ctx
parameter_list|,
name|String
name|username
parameter_list|,
name|boolean
name|isBlocking
parameter_list|)
throws|throws
name|LockException
block|{
name|init
argument_list|()
expr_stmt|;
comment|// Make sure we've built the lock manager
name|getLockManager
argument_list|()
expr_stmt|;
name|boolean
name|atLeastOneLock
init|=
literal|false
decl_stmt|;
name|LockRequestBuilder
name|rqstBuilder
init|=
operator|new
name|LockRequestBuilder
argument_list|()
decl_stmt|;
comment|//link queryId to txnId
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting lock request transaction to "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
operator|+
literal|" for queryId="
operator|+
name|plan
operator|.
name|getQueryId
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|setTransactionId
argument_list|(
name|txnId
argument_list|)
operator|.
name|setUser
argument_list|(
name|username
argument_list|)
expr_stmt|;
comment|// For each source to read, get a shared lock
for|for
control|(
name|ReadEntity
name|input
range|:
name|plan
operator|.
name|getInputs
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|input
operator|.
name|needsLock
argument_list|()
operator|||
name|input
operator|.
name|isUpdateOrDelete
argument_list|()
condition|)
block|{
comment|// We don't want to acquire readlocks during update or delete as we'll be acquiring write
comment|// locks instead.
continue|continue;
block|}
name|LockComponentBuilder
name|compBuilder
init|=
operator|new
name|LockComponentBuilder
argument_list|()
decl_stmt|;
name|compBuilder
operator|.
name|setShared
argument_list|()
expr_stmt|;
name|Table
name|t
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|input
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|DATABASE
case|:
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|input
operator|.
name|getDatabase
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|TABLE
case|:
name|t
operator|=
name|input
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|compBuilder
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|PARTITION
case|:
case|case
name|DUMMYPARTITION
case|:
name|compBuilder
operator|.
name|setPartitionName
argument_list|(
name|input
operator|.
name|getPartition
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|=
name|input
operator|.
name|getPartition
argument_list|()
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|compBuilder
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// This is a file or something we don't hold locks for.
continue|continue;
block|}
name|LockComponent
name|comp
init|=
name|compBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding lock component to lock request "
operator|+
name|comp
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|addLockComponent
argument_list|(
name|comp
argument_list|)
expr_stmt|;
name|atLeastOneLock
operator|=
literal|true
expr_stmt|;
block|}
comment|// For each source to write to, get the appropriate lock type.  If it's
comment|// an OVERWRITE, we need to get an exclusive lock.  If it's an insert (no
comment|// overwrite) than we need a shared.  If it's update or delete then we
comment|// need a SEMI-SHARED.
for|for
control|(
name|WriteEntity
name|output
range|:
name|plan
operator|.
name|getOutputs
argument_list|()
control|)
block|{
if|if
condition|(
name|output
operator|.
name|getType
argument_list|()
operator|==
name|Entity
operator|.
name|Type
operator|.
name|DFS_DIR
operator|||
name|output
operator|.
name|getType
argument_list|()
operator|==
name|Entity
operator|.
name|Type
operator|.
name|LOCAL_DIR
condition|)
block|{
comment|// We don't lock files or directories.
continue|continue;
block|}
name|LockComponentBuilder
name|compBuilder
init|=
operator|new
name|LockComponentBuilder
argument_list|()
decl_stmt|;
name|Table
name|t
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"output is null "
operator|+
operator|(
name|output
operator|==
literal|null
operator|)
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|output
operator|.
name|getWriteType
argument_list|()
condition|)
block|{
case|case
name|DDL_EXCLUSIVE
case|:
case|case
name|INSERT_OVERWRITE
case|:
name|compBuilder
operator|.
name|setExclusive
argument_list|()
expr_stmt|;
break|break;
case|case
name|INSERT
case|:
case|case
name|DDL_SHARED
case|:
name|compBuilder
operator|.
name|setShared
argument_list|()
expr_stmt|;
break|break;
case|case
name|UPDATE
case|:
case|case
name|DELETE
case|:
name|compBuilder
operator|.
name|setSemiShared
argument_list|()
expr_stmt|;
break|break;
case|case
name|DDL_NO_LOCK
case|:
continue|continue;
comment|// No lock required here
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown write type "
operator|+
name|output
operator|.
name|getWriteType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|output
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|DATABASE
case|:
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|output
operator|.
name|getDatabase
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|TABLE
case|:
case|case
name|DUMMYPARTITION
case|:
comment|// in case of dynamic partitioning lock the table
name|t
operator|=
name|output
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|compBuilder
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|PARTITION
case|:
name|compBuilder
operator|.
name|setPartitionName
argument_list|(
name|output
operator|.
name|getPartition
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|=
name|output
operator|.
name|getPartition
argument_list|()
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|compBuilder
operator|.
name|setDbName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|compBuilder
operator|.
name|setTableName
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// This is a file or something we don't hold locks for.
continue|continue;
block|}
name|LockComponent
name|comp
init|=
name|compBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding lock component to lock request "
operator|+
name|comp
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|addLockComponent
argument_list|(
name|comp
argument_list|)
expr_stmt|;
name|atLeastOneLock
operator|=
literal|true
expr_stmt|;
block|}
comment|// Make sure we need locks.  It's possible there's nothing to lock in
comment|// this operation.
if|if
condition|(
operator|!
name|atLeastOneLock
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No locks needed for queryId"
operator|+
name|plan
operator|.
name|getQueryId
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|isTxnOpen
argument_list|()
condition|)
block|{
name|statementId
operator|++
expr_stmt|;
block|}
name|LockState
name|lockState
init|=
name|lockMgr
operator|.
name|lock
argument_list|(
name|rqstBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|plan
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|isBlocking
argument_list|,
name|locks
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setHiveLocks
argument_list|(
name|locks
argument_list|)
expr_stmt|;
return|return
name|lockState
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTxn
parameter_list|()
throws|throws
name|LockException
block|{
if|if
condition|(
operator|!
name|isTxnOpen
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Attempt to commit before opening a transaction"
argument_list|)
throw|;
block|}
try|try
block|{
name|lockMgr
operator|.
name|clearLocalLockRecords
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Committing txn "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
name|txnId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchTxnException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Metastore could not find "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|TXN_NO_SUCH_TRANSACTION
argument_list|,
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TxnAbortedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Transaction "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
operator|+
literal|" aborted"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|TXN_ABORTED
argument_list|,
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
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
name|ErrorMsg
operator|.
name|METASTORE_COMMUNICATION_FAILED
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|txnId
operator|=
literal|0
expr_stmt|;
name|statementId
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollbackTxn
parameter_list|()
throws|throws
name|LockException
block|{
if|if
condition|(
operator|!
name|isTxnOpen
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Attempt to rollback before opening a transaction"
argument_list|)
throw|;
block|}
try|try
block|{
name|lockMgr
operator|.
name|clearLocalLockRecords
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Rolling back "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|rollbackTxn
argument_list|(
name|txnId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchTxnException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Metastore could not find "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|TXN_NO_SUCH_TRANSACTION
argument_list|,
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
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
name|ErrorMsg
operator|.
name|METASTORE_COMMUNICATION_FAILED
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|txnId
operator|=
literal|0
expr_stmt|;
name|statementId
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|heartbeat
parameter_list|()
throws|throws
name|LockException
block|{
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
decl_stmt|;
if|if
condition|(
name|isTxnOpen
argument_list|()
condition|)
block|{
comment|// Create one dummy lock so we can go through the loop below, though we only
comment|//really need txnId
name|DbLockManager
operator|.
name|DbHiveLock
name|dummyLock
init|=
operator|new
name|DbLockManager
operator|.
name|DbHiveLock
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
name|locks
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|locks
operator|.
name|add
argument_list|(
name|dummyLock
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|locks
operator|=
name|lockMgr
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Sending heartbeat for "
argument_list|)
operator|.
name|append
argument_list|(
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|" and"
argument_list|)
decl_stmt|;
for|for
control|(
name|HiveLock
name|lock
range|:
name|locks
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|lock
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isTxnOpen
argument_list|()
operator|&&
name|locks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// No locks, no txn, we outta here.
return|return;
block|}
for|for
control|(
name|HiveLock
name|lock
range|:
name|locks
control|)
block|{
name|long
name|lockId
init|=
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|lock
operator|)
operator|.
name|lockId
decl_stmt|;
try|try
block|{
name|client
operator|.
name|heartbeat
argument_list|(
name|txnId
argument_list|,
name|lockId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchLockException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to find lock "
operator|+
name|JavaUtils
operator|.
name|lockIdToString
argument_list|(
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|LOCK_NO_SUCH_LOCK
argument_list|,
name|JavaUtils
operator|.
name|lockIdToString
argument_list|(
name|lockId
argument_list|)
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchTxnException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to find transaction "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|TXN_NO_SUCH_TRANSACTION
argument_list|,
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TxnAbortedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Transaction aborted "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|TXN_ABORTED
argument_list|,
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
argument_list|)
throw|;
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
name|ErrorMsg
operator|.
name|METASTORE_COMMUNICATION_FAILED
operator|.
name|getMsg
argument_list|()
operator|+
literal|"("
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
operator|+
literal|","
operator|+
name|lock
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|ValidTxnList
name|getValidTxns
parameter_list|()
throws|throws
name|LockException
block|{
name|init
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|client
operator|.
name|getValidTxns
argument_list|(
name|txnId
argument_list|)
return|;
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
name|ErrorMsg
operator|.
name|METASTORE_COMMUNICATION_FAILED
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsExplicitLock
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|useNewShowLocksFormat
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsAcid
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|destruct
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|isTxnOpen
argument_list|()
condition|)
name|rollbackTxn
argument_list|()
expr_stmt|;
if|if
condition|(
name|lockMgr
operator|!=
literal|null
condition|)
name|lockMgr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught exception "
operator|+
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" with message<"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|">, swallowing as there is nothing we can do with it."
argument_list|)
expr_stmt|;
comment|// Not much we can do about it here.
block|}
block|}
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|LockException
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Must call setHiveConf before any other "
operator|+
literal|"methods."
argument_list|)
throw|;
block|}
try|try
block|{
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|client
operator|=
name|db
operator|.
name|getMSC
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|METASTORE_COULD_NOT_INITIATE
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|METASTORE_COULD_NOT_INITIATE
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTxnOpen
parameter_list|()
block|{
return|return
name|txnId
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentTxnId
parameter_list|()
block|{
return|return
name|txnId
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getStatementId
parameter_list|()
block|{
return|return
name|statementId
return|;
block|}
block|}
end_class

end_unit

