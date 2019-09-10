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
name|ql
operator|.
name|lockmgr
package|;
end_package

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
name|Configurable
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
name|ddl
operator|.
name|database
operator|.
name|lock
operator|.
name|LockDatabaseDesc
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
name|ddl
operator|.
name|database
operator|.
name|unlock
operator|.
name|UnlockDatabaseDesc
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
name|ddl
operator|.
name|table
operator|.
name|lock
operator|.
name|LockTableDesc
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
name|ddl
operator|.
name|table
operator|.
name|lock
operator|.
name|UnlockTableDesc
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
name|DriverState
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
name|lockmgr
operator|.
name|HiveLockObject
operator|.
name|HiveLockObjectData
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

begin_comment
comment|/**  * An implementation HiveTxnManager that includes internal methods that all  * transaction managers need to implement but that we don't want to expose to  * outside.  */
end_comment

begin_class
specifier|abstract
class|class
name|HiveTxnManagerImpl
implements|implements
name|HiveTxnManager
implements|,
name|Configurable
block|{
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|c
parameter_list|)
block|{
name|setConf
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
name|conf
operator|=
operator|(
name|HiveConf
operator|)
name|c
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|abstract
specifier|protected
name|void
name|destruct
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|closeTxnManager
parameter_list|()
block|{
name|destruct
argument_list|()
expr_stmt|;
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
parameter_list|,
name|DriverState
name|driverState
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finalize
parameter_list|()
throws|throws
name|Throwable
block|{
name|destruct
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|lockTable
parameter_list|(
name|Hive
name|db
parameter_list|,
name|LockTableDesc
name|lockTbl
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveLockManager
name|lockMgr
init|=
name|getAndCheckLockManager
argument_list|()
decl_stmt|;
name|HiveLockMode
name|mode
init|=
name|HiveLockMode
operator|.
name|valueOf
argument_list|(
name|lockTbl
operator|.
name|getMode
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|tabName
init|=
name|lockTbl
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|Table
name|tbl
init|=
name|db
operator|.
name|getTable
argument_list|(
name|tabName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tbl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Table "
operator|+
name|tabName
operator|+
literal|" does not exist "
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|lockTbl
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|HiveLockObjectData
name|lockData
init|=
operator|new
name|HiveLockObjectData
argument_list|(
name|lockTbl
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
literal|"EXPLICIT"
argument_list|,
name|lockTbl
operator|.
name|getQueryStr
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
name|HiveLock
name|lck
init|=
name|lockMgr
operator|.
name|lock
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|tbl
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|lck
operator|==
literal|null
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
name|Partition
name|par
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|tbl
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|par
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Partition "
operator|+
name|partSpec
operator|+
literal|" for table "
operator|+
name|tabName
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|HiveLock
name|lck
init|=
name|lockMgr
operator|.
name|lock
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|par
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|lck
operator|==
literal|null
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|unlockTable
parameter_list|(
name|Hive
name|hiveDB
parameter_list|,
name|UnlockTableDesc
name|unlockTbl
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveLockManager
name|lockMgr
init|=
name|getAndCheckLockManager
argument_list|()
decl_stmt|;
name|String
name|tabName
init|=
name|unlockTbl
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|HiveLockObject
name|obj
init|=
name|HiveLockObject
operator|.
name|createFrom
argument_list|(
name|hiveDB
argument_list|,
name|tabName
argument_list|,
name|unlockTbl
operator|.
name|getPartSpec
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|lockMgr
operator|.
name|getLocks
argument_list|(
name|obj
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|locks
operator|==
literal|null
operator|)
operator|||
operator|(
name|locks
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Table "
operator|+
name|tabName
operator|+
literal|" is not locked "
argument_list|)
throw|;
block|}
name|Iterator
argument_list|<
name|HiveLock
argument_list|>
name|locksIter
init|=
name|locks
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|locksIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|HiveLock
name|lock
init|=
name|locksIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|lockMgr
operator|.
name|unlock
argument_list|(
name|lock
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|lockDatabase
parameter_list|(
name|Hive
name|hiveDB
parameter_list|,
name|LockDatabaseDesc
name|lockDb
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveLockManager
name|lockMgr
init|=
name|getAndCheckLockManager
argument_list|()
decl_stmt|;
name|HiveLockMode
name|mode
init|=
name|HiveLockMode
operator|.
name|valueOf
argument_list|(
name|lockDb
operator|.
name|getMode
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|lockDb
operator|.
name|getDatabaseName
argument_list|()
decl_stmt|;
name|Database
name|dbObj
init|=
name|hiveDB
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Database "
operator|+
name|dbName
operator|+
literal|" does not exist "
argument_list|)
throw|;
block|}
name|HiveLockObjectData
name|lockData
init|=
operator|new
name|HiveLockObjectData
argument_list|(
name|lockDb
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
literal|"EXPLICIT"
argument_list|,
name|lockDb
operator|.
name|getQueryStr
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLock
name|lck
init|=
name|lockMgr
operator|.
name|lock
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|lockData
argument_list|)
argument_list|,
name|mode
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|lck
operator|==
literal|null
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|unlockDatabase
parameter_list|(
name|Hive
name|hiveDB
parameter_list|,
name|UnlockDatabaseDesc
name|unlockDb
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveLockManager
name|lockMgr
init|=
name|getAndCheckLockManager
argument_list|()
decl_stmt|;
name|String
name|dbName
init|=
name|unlockDb
operator|.
name|getDatabaseName
argument_list|()
decl_stmt|;
name|Database
name|dbObj
init|=
name|hiveDB
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Database "
operator|+
name|dbName
operator|+
literal|" does not exist "
argument_list|)
throw|;
block|}
name|HiveLockObject
name|obj
init|=
operator|new
name|HiveLockObject
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|lockMgr
operator|.
name|getLocks
argument_list|(
name|obj
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|locks
operator|==
literal|null
operator|)
operator|||
operator|(
name|locks
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Database "
operator|+
name|dbName
operator|+
literal|" is not locked "
argument_list|)
throw|;
block|}
for|for
control|(
name|HiveLock
name|lock
range|:
name|locks
control|)
block|{
name|lockMgr
operator|.
name|unlock
argument_list|(
name|lock
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * Gets the lock manager and verifies if the explicit lock is supported    * @return  the lock manager    * @throws HiveException    */
specifier|protected
name|HiveLockManager
name|getAndCheckLockManager
parameter_list|()
throws|throws
name|HiveException
block|{
name|HiveLockManager
name|lockMgr
init|=
name|getLockManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|lockMgr
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"LockManager cannot be acquired"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|supportsExplicitLock
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|LOCK_REQUEST_UNSUPPORTED
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|lockMgr
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|recordSnapshot
parameter_list|(
name|QueryPlan
name|queryPlan
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isImplicitTransactionOpen
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|LockResponse
name|acquireMaterializationRebuildLock
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|long
name|txnId
parameter_list|)
throws|throws
name|LockException
block|{
comment|// This is default implementation. Locking only works for incremental maintenance
comment|// which only works for DB transactional manager, thus we cannot acquire a lock.
return|return
operator|new
name|LockResponse
argument_list|(
literal|0L
argument_list|,
name|LockState
operator|.
name|NOT_ACQUIRED
argument_list|)
return|;
block|}
block|}
end_class

end_unit

