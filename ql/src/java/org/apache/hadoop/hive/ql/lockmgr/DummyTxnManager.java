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
name|common
operator|.
name|ValidTxnListImpl
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
name|DummyPartition
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
name|hadoop
operator|.
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link HiveTxnManager} that does not support  * transactions.  This provides default Hive behavior.  */
end_comment

begin_class
class|class
name|DummyTxnManager
extends|extends
name|HiveTxnManagerImpl
block|{
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
name|DummyTxnManager
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HiveLockManager
name|lockMgr
decl_stmt|;
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
comment|// No-op
return|return
literal|0L
return|;
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
if|if
condition|(
name|lockMgr
operator|==
literal|null
condition|)
block|{
name|boolean
name|supportConcurrency
init|=
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
decl_stmt|;
if|if
condition|(
name|supportConcurrency
condition|)
block|{
name|String
name|lockMgrName
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_MANAGER
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|lockMgrName
operator|==
literal|null
operator|)
operator|||
operator|(
name|lockMgrName
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|LOCKMGR_NOT_SPECIFIED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating lock manager of type "
operator|+
name|lockMgrName
argument_list|)
expr_stmt|;
name|lockMgr
operator|=
operator|(
name|HiveLockManager
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getClassByName
argument_list|(
name|lockMgrName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|lockMgr
operator|.
name|setContext
argument_list|(
operator|new
name|HiveLockManagerCtx
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// set hiveLockMgr to null just in case this invalid manager got set to
comment|// next query's ctx.
if|if
condition|(
name|lockMgr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|lockMgr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e1
parameter_list|)
block|{
comment|//nothing can do here
block|}
name|lockMgr
operator|=
literal|null
expr_stmt|;
block|}
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|LOCKMGR_NOT_INITIALIZED
operator|.
name|getMsg
argument_list|()
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Concurrency mode is disabled, not creating a lock manager"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|// Force a re-read of the configuration file.  This is done because
comment|// different queries in the session may be using the same lock manager.
name|lockMgr
operator|.
name|refresh
argument_list|()
expr_stmt|;
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
comment|// Make sure we've built the lock manager
name|getLockManager
argument_list|()
expr_stmt|;
comment|// If the lock manager is still null, then it means we aren't using a
comment|// lock manager
if|if
condition|(
name|lockMgr
operator|==
literal|null
condition|)
return|return;
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|lockObjects
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLockObj
argument_list|>
argument_list|()
decl_stmt|;
comment|// Sort all the inputs, outputs.
comment|// If a lock needs to be acquired on any partition, a read lock needs to be acquired on all
comment|// its parents also
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
condition|)
block|{
continue|continue;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding "
operator|+
name|input
operator|.
name|getName
argument_list|()
operator|+
literal|" to list of lock inputs"
argument_list|)
expr_stmt|;
if|if
condition|(
name|input
operator|.
name|getType
argument_list|()
operator|==
name|ReadEntity
operator|.
name|Type
operator|.
name|DATABASE
condition|)
block|{
name|lockObjects
operator|.
name|addAll
argument_list|(
name|getLockObjects
argument_list|(
name|plan
argument_list|,
name|input
operator|.
name|getDatabase
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|input
operator|.
name|getType
argument_list|()
operator|==
name|ReadEntity
operator|.
name|Type
operator|.
name|TABLE
condition|)
block|{
name|lockObjects
operator|.
name|addAll
argument_list|(
name|getLockObjects
argument_list|(
name|plan
argument_list|,
literal|null
argument_list|,
name|input
operator|.
name|getTable
argument_list|()
argument_list|,
literal|null
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lockObjects
operator|.
name|addAll
argument_list|(
name|getLockObjects
argument_list|(
name|plan
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|input
operator|.
name|getPartition
argument_list|()
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|HiveLockMode
name|lockMode
init|=
name|getWriteEntityLockMode
argument_list|(
name|output
argument_list|)
decl_stmt|;
if|if
condition|(
name|lockMode
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding "
operator|+
name|output
operator|.
name|getName
argument_list|()
operator|+
literal|" to list of lock outputs"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|lockObj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|output
operator|.
name|getType
argument_list|()
operator|==
name|WriteEntity
operator|.
name|Type
operator|.
name|DATABASE
condition|)
block|{
name|lockObjects
operator|.
name|addAll
argument_list|(
name|getLockObjects
argument_list|(
name|plan
argument_list|,
name|output
operator|.
name|getDatabase
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|lockMode
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|output
operator|.
name|getTyp
argument_list|()
operator|==
name|WriteEntity
operator|.
name|Type
operator|.
name|TABLE
condition|)
block|{
name|lockObj
operator|=
name|getLockObjects
argument_list|(
name|plan
argument_list|,
literal|null
argument_list|,
name|output
operator|.
name|getTable
argument_list|()
argument_list|,
literal|null
argument_list|,
name|lockMode
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|output
operator|.
name|getTyp
argument_list|()
operator|==
name|WriteEntity
operator|.
name|Type
operator|.
name|PARTITION
condition|)
block|{
name|lockObj
operator|=
name|getLockObjects
argument_list|(
name|plan
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|output
operator|.
name|getPartition
argument_list|()
argument_list|,
name|lockMode
argument_list|)
expr_stmt|;
block|}
comment|// In case of dynamic queries, it is possible to have incomplete dummy partitions
elseif|else
if|if
condition|(
name|output
operator|.
name|getTyp
argument_list|()
operator|==
name|WriteEntity
operator|.
name|Type
operator|.
name|DUMMYPARTITION
condition|)
block|{
name|lockObj
operator|=
name|getLockObjects
argument_list|(
name|plan
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|output
operator|.
name|getPartition
argument_list|()
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lockObj
operator|!=
literal|null
condition|)
block|{
name|lockObjects
operator|.
name|addAll
argument_list|(
name|lockObj
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getOutputLockObjects
argument_list|()
operator|.
name|put
argument_list|(
name|output
argument_list|,
name|lockObj
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|lockObjects
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|ctx
operator|.
name|isNeedLockMgr
argument_list|()
condition|)
block|{
return|return;
block|}
name|HiveLockObject
operator|.
name|HiveLockObjectData
name|lockData
init|=
operator|new
name|HiveLockObject
operator|.
name|HiveLockObjectData
argument_list|(
name|plan
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
literal|"IMPLICIT"
argument_list|,
name|plan
operator|.
name|getQueryStr
argument_list|()
argument_list|)
decl_stmt|;
comment|// Lock the database also
name|String
name|currentDb
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
decl_stmt|;
name|lockObjects
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|currentDb
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|dedupLockObjects
argument_list|(
name|lockObjects
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
init|=
name|lockMgr
operator|.
name|lock
argument_list|(
name|lockObjects
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveLocks
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|LOCK_CANNOT_BE_ACQUIRED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|ctx
operator|.
name|setHiveLocks
argument_list|(
name|hiveLocks
argument_list|)
expr_stmt|;
block|}
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
comment|// No-op
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
comment|// No-op
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
comment|// No-op
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
return|return
operator|new
name|ValidTxnListImpl
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsExplicitLock
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
name|useNewShowLocksFormat
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
name|supportsAcid
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|protected
name|void
name|destruct
parameter_list|()
block|{
if|if
condition|(
name|lockMgr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|lockMgr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e
parameter_list|)
block|{
comment|// Not much I can do about it.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got exception when closing lock manager "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Dedup the list of lock objects so that there is only one lock per table/partition.    * If there is both a shared and exclusive lock for the same object, this will deduped    * to just a single exclusive lock.  Package level so that the unit tests    * can access it.  Not intended for use outside this class.    * @param lockObjects    */
specifier|static
name|void
name|dedupLockObjects
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|lockObjects
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|HiveLockObj
argument_list|>
name|lockMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HiveLockObj
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HiveLockObj
name|lockObj
range|:
name|lockObjects
control|)
block|{
name|String
name|lockName
init|=
name|lockObj
operator|.
name|getName
argument_list|()
decl_stmt|;
name|HiveLockObj
name|foundLock
init|=
name|lockMap
operator|.
name|get
argument_list|(
name|lockName
argument_list|)
decl_stmt|;
if|if
condition|(
name|foundLock
operator|==
literal|null
operator|||
name|lockObj
operator|.
name|getMode
argument_list|()
operator|==
name|HiveLockMode
operator|.
name|EXCLUSIVE
condition|)
block|{
name|lockMap
operator|.
name|put
argument_list|(
name|lockName
argument_list|,
name|lockObj
argument_list|)
expr_stmt|;
block|}
block|}
comment|// copy set of deduped locks back to original list
name|lockObjects
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|HiveLockObj
name|lockObj
range|:
name|lockMap
operator|.
name|values
argument_list|()
control|)
block|{
name|lockObjects
operator|.
name|add
argument_list|(
name|lockObj
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HiveLockMode
name|getWriteEntityLockMode
parameter_list|(
name|WriteEntity
name|we
parameter_list|)
block|{
name|HiveLockMode
name|lockMode
init|=
name|we
operator|.
name|isComplete
argument_list|()
condition|?
name|HiveLockMode
operator|.
name|EXCLUSIVE
else|:
name|HiveLockMode
operator|.
name|SHARED
decl_stmt|;
comment|//but the writeEntity is complete in DDL operations, and we need check its writeType to
comment|//to determine the lockMode
switch|switch
condition|(
name|we
operator|.
name|getWriteType
argument_list|()
condition|)
block|{
case|case
name|DDL_EXCLUSIVE
case|:
return|return
name|HiveLockMode
operator|.
name|EXCLUSIVE
return|;
case|case
name|DDL_SHARED
case|:
return|return
name|HiveLockMode
operator|.
name|SHARED
return|;
case|case
name|DDL_NO_LOCK
case|:
return|return
literal|null
return|;
default|default:
comment|//other writeTypes related to DMLs
return|return
name|lockMode
return|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|getLockObjects
parameter_list|(
name|QueryPlan
name|plan
parameter_list|,
name|Database
name|db
parameter_list|,
name|Table
name|t
parameter_list|,
name|Partition
name|p
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|)
throws|throws
name|LockException
block|{
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|locks
init|=
operator|new
name|LinkedList
argument_list|<
name|HiveLockObj
argument_list|>
argument_list|()
decl_stmt|;
name|HiveLockObject
operator|.
name|HiveLockObjectData
name|lockData
init|=
operator|new
name|HiveLockObject
operator|.
name|HiveLockObjectData
argument_list|(
name|plan
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
literal|"IMPLICIT"
argument_list|,
name|plan
operator|.
name|getQueryStr
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|db
operator|!=
literal|null
condition|)
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|locks
return|;
block|}
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|t
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
name|mode
operator|=
name|HiveLockMode
operator|.
name|SHARED
expr_stmt|;
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|locks
return|;
block|}
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|p
operator|instanceof
name|DummyPartition
operator|)
condition|)
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|p
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// All the parents are locked in shared mode
name|mode
operator|=
name|HiveLockMode
operator|.
name|SHARED
expr_stmt|;
comment|// For dummy partitions, only partition name is needed
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|p
operator|instanceof
name|DummyPartition
condition|)
block|{
name|name
operator|=
name|p
operator|.
name|getName
argument_list|()
operator|.
name|split
argument_list|(
literal|"@"
argument_list|)
index|[
literal|2
index|]
expr_stmt|;
block|}
name|String
name|partialName
init|=
literal|""
decl_stmt|;
name|String
index|[]
name|partns
init|=
name|name
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|int
name|len
init|=
name|p
operator|instanceof
name|DummyPartition
condition|?
name|partns
operator|.
name|length
else|:
name|partns
operator|.
name|length
operator|-
literal|1
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partialSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|len
condition|;
name|idx
operator|++
control|)
block|{
name|String
name|partn
init|=
name|partns
index|[
name|idx
index|]
decl_stmt|;
name|partialName
operator|+=
name|partn
expr_stmt|;
name|String
index|[]
name|nameValue
init|=
name|partn
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|nameValue
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|partialSpec
operator|.
name|put
argument_list|(
name|nameValue
index|[
literal|0
index|]
argument_list|,
name|nameValue
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
try|try
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
operator|new
name|DummyPartition
argument_list|(
name|p
operator|.
name|getTable
argument_list|()
argument_list|,
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
operator|+
literal|"/"
operator|+
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|+
literal|"/"
operator|+
name|partialName
argument_list|,
name|partialSpec
argument_list|)
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
name|partialName
operator|+=
literal|"/"
expr_stmt|;
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|p
operator|.
name|getTable
argument_list|()
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|locks
return|;
block|}
block|}
end_class

end_unit

