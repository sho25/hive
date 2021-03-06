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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|metrics
operator|.
name|common
operator|.
name|Metrics
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
name|metrics
operator|.
name|common
operator|.
name|MetricsConstant
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
name|metrics
operator|.
name|common
operator|.
name|MetricsFactory
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
name|ddl
operator|.
name|table
operator|.
name|lock
operator|.
name|show
operator|.
name|ShowLocksOperation
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HashSet
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
name|Objects
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * An implementation of HiveLockManager for use with {@link org.apache.hadoop.hive.ql.lockmgr.DbTxnManager}.  * Note, this lock manager is not meant to be stand alone.  It cannot be used without the DbTxnManager.  * See {@link DbTxnManager#getMS()} for important concurrency/metastore access notes.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|DbLockManager
implements|implements
name|HiveLockManager
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|DbLockManager
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|private
name|long
name|MAX_SLEEP
decl_stmt|;
comment|//longer term we should always have a txn id and then we won't need to track locks here at all
specifier|private
name|Set
argument_list|<
name|DbHiveLock
argument_list|>
name|locks
decl_stmt|;
specifier|private
name|long
name|nextSleep
init|=
literal|50
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|DbTxnManager
name|txnManager
decl_stmt|;
name|DbLockManager
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|DbTxnManager
name|txnManager
parameter_list|)
block|{
name|locks
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|txnManager
operator|=
name|txnManager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setContext
parameter_list|(
name|HiveLockManagerCtx
name|ctx
parameter_list|)
throws|throws
name|LockException
block|{   }
annotation|@
name|Override
specifier|public
name|HiveLock
name|lock
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|,
name|boolean
name|keepAlive
parameter_list|)
throws|throws
name|LockException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|lock
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|objs
parameter_list|,
name|boolean
name|keepAlive
parameter_list|,
name|DriverState
name|driverState
parameter_list|)
throws|throws
name|LockException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Send a lock request to the metastore.  This is intended for use by    * {@link DbTxnManager}.    * @param lock lock request    * @param isBlocking if true, will block until locks have been acquired    * @throws LockException    * @return the result of the lock attempt    */
name|LockState
name|lock
parameter_list|(
name|LockRequest
name|lock
parameter_list|,
name|String
name|queryId
parameter_list|,
name|boolean
name|isBlocking
parameter_list|,
name|List
argument_list|<
name|HiveLock
argument_list|>
name|acquiredLocks
parameter_list|)
throws|throws
name|LockException
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|queryId
argument_list|,
literal|"queryId cannot be null"
argument_list|)
expr_stmt|;
name|nextSleep
operator|=
literal|50
expr_stmt|;
comment|/*      * get from conf to pick up changes; make sure not to set too low and kill the metastore      * MAX_SLEEP is the max time each backoff() will wait for, thus the total time to wait for      * successful lock acquisition is approximately (see backoff()) maxNumWaits * MAX_SLEEP.      */
name|MAX_SLEEP
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|15000
argument_list|,
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_SLEEP_BETWEEN_RETRIES
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|maxNumWaits
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_NUMRETRIES
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Requesting: queryId="
operator|+
name|queryId
operator|+
literal|" "
operator|+
name|lock
argument_list|)
expr_stmt|;
name|LockResponse
name|res
init|=
name|txnManager
operator|.
name|getMS
argument_list|()
operator|.
name|lock
argument_list|(
name|lock
argument_list|)
decl_stmt|;
comment|//link lockId to queryId
name|LOG
operator|.
name|info
argument_list|(
literal|"Response to queryId="
operator|+
name|queryId
operator|+
literal|" "
operator|+
name|res
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isBlocking
condition|)
block|{
if|if
condition|(
name|res
operator|.
name|getState
argument_list|()
operator|==
name|LockState
operator|.
name|WAITING
condition|)
block|{
return|return
name|LockState
operator|.
name|WAITING
return|;
block|}
block|}
name|int
name|numRetries
init|=
literal|0
decl_stmt|;
name|long
name|startRetry
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|res
operator|.
name|getState
argument_list|()
operator|==
name|LockState
operator|.
name|WAITING
operator|&&
name|numRetries
operator|++
operator|<
name|maxNumWaits
condition|)
block|{
name|backoff
argument_list|()
expr_stmt|;
name|res
operator|=
name|txnManager
operator|.
name|getMS
argument_list|()
operator|.
name|checkLock
argument_list|(
name|res
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|retryDuration
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startRetry
decl_stmt|;
name|DbHiveLock
name|hl
init|=
operator|new
name|DbHiveLock
argument_list|(
name|res
operator|.
name|getLockid
argument_list|()
argument_list|,
name|queryId
argument_list|,
name|lock
operator|.
name|getTxnid
argument_list|()
argument_list|,
name|lock
operator|.
name|getComponent
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|locks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|boolean
name|logMsg
init|=
literal|false
decl_stmt|;
for|for
control|(
name|DbHiveLock
name|l
range|:
name|locks
control|)
block|{
if|if
condition|(
name|l
operator|.
name|txnId
operator|!=
name|hl
operator|.
name|txnId
condition|)
block|{
comment|//locks from different transactions detected (or from transaction and read-only query in autocommit)
name|logMsg
operator|=
literal|true
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|l
operator|.
name|txnId
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|l
operator|.
name|queryId
operator|.
name|equals
argument_list|(
name|hl
operator|.
name|queryId
argument_list|)
condition|)
block|{
comment|//here means no open transaction, but different queries
name|logMsg
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|logMsg
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"adding new DbHiveLock("
operator|+
name|hl
operator|+
literal|") while we are already tracking locks: "
operator|+
name|locks
argument_list|)
expr_stmt|;
block|}
block|}
name|locks
operator|.
name|add
argument_list|(
name|hl
argument_list|)
expr_stmt|;
if|if
condition|(
name|res
operator|.
name|getState
argument_list|()
operator|!=
name|LockState
operator|.
name|ACQUIRED
condition|)
block|{
if|if
condition|(
name|res
operator|.
name|getState
argument_list|()
operator|==
name|LockState
operator|.
name|WAITING
condition|)
block|{
comment|/**            * the {@link #unlock(HiveLock)} here is more about future proofing when support for            * multi-statement txns is added.  In that case it's reasonable for the client            * to retry this part of txn or try something else w/o aborting the whole txn.            * Also for READ_COMMITTED (when and if that is supported).            */
name|unlock
argument_list|(
name|hl
argument_list|)
expr_stmt|;
comment|//remove the locks in Waiting state
name|LockException
name|le
init|=
operator|new
name|LockException
argument_list|(
literal|null
argument_list|,
name|ErrorMsg
operator|.
name|LOCK_ACQUIRE_TIMEDOUT
argument_list|,
name|lock
operator|.
name|toString
argument_list|()
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|retryDuration
argument_list|)
argument_list|,
name|res
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TXN_MGR_DUMP_LOCK_STATE_ON_ACQUIRE_TIMEOUT
argument_list|)
condition|)
block|{
name|showLocksNewFormat
argument_list|(
name|le
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
name|le
throw|;
block|}
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
operator|+
literal|" "
operator|+
name|res
argument_list|)
throw|;
block|}
name|acquiredLocks
operator|.
name|add
argument_list|(
name|hl
argument_list|)
expr_stmt|;
name|Metrics
name|metrics
init|=
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|METASTORE_HIVE_LOCKS
argument_list|)
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
name|warn
argument_list|(
literal|"Error Reporting hive client metastore lock operation to Metrics system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|res
operator|.
name|getState
argument_list|()
return|;
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
name|lock
operator|.
name|getTxnid
argument_list|()
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
name|lock
operator|.
name|getTxnid
argument_list|()
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
name|LockException
name|le
init|=
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
name|lock
operator|.
name|getTxnid
argument_list|()
argument_list|)
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|le
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|le
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
block|}
specifier|private
name|void
name|showLocksNewFormat
parameter_list|(
name|String
name|preamble
parameter_list|)
throws|throws
name|LockException
block|{
name|ShowLocksResponse
name|rsp
init|=
name|getLocks
argument_list|()
decl_stmt|;
comment|// write the results in the file
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|1024
operator|*
literal|2
argument_list|)
decl_stmt|;
name|DataOutputStream
name|os
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
try|try
block|{
name|ShowLocksOperation
operator|.
name|dumpLockInfo
argument_list|(
name|os
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|os
operator|.
name|flush
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|baos
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Dumping lock info for "
operator|+
name|preamble
operator|+
literal|" failed: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Used to make another attempt to acquire a lock (in Waiting state)    * @param extLockId    * @return result of the attempt    * @throws LockException    */
name|LockState
name|checkLock
parameter_list|(
name|long
name|extLockId
parameter_list|)
throws|throws
name|LockException
block|{
try|try
block|{
return|return
name|txnManager
operator|.
name|getMS
argument_list|()
operator|.
name|checkLock
argument_list|(
name|extLockId
argument_list|)
operator|.
name|getState
argument_list|()
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
name|void
name|unlock
parameter_list|(
name|HiveLock
name|hiveLock
parameter_list|)
throws|throws
name|LockException
block|{
name|long
name|lockId
init|=
operator|(
operator|(
name|DbHiveLock
operator|)
name|hiveLock
operator|)
operator|.
name|lockId
decl_stmt|;
name|boolean
name|removed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unlocking "
operator|+
name|hiveLock
argument_list|)
expr_stmt|;
name|txnManager
operator|.
name|getMS
argument_list|()
operator|.
name|unlock
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
comment|//important to remove after unlock() in case it fails
name|removed
operator|=
name|locks
operator|.
name|remove
argument_list|(
name|hiveLock
argument_list|)
expr_stmt|;
name|Metrics
name|metrics
init|=
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|metrics
operator|.
name|decrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|METASTORE_HIVE_LOCKS
argument_list|)
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
name|warn
argument_list|(
literal|"Error Reporting hive client metastore unlock operation to Metrics system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removed a lock "
operator|+
name|removed
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchLockException
name|e
parameter_list|)
block|{
comment|//if metastore has no record of this lock, it most likely timed out; either way
comment|//there is no point tracking it here any longer
name|removed
operator|=
name|locks
operator|.
name|remove
argument_list|(
name|hiveLock
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Metastore could find no record of lock "
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
name|TxnOpenException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Attempt to unlock lock "
operator|+
name|JavaUtils
operator|.
name|lockIdToString
argument_list|(
name|lockId
argument_list|)
operator|+
literal|"associated with an open transaction, "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
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
if|if
condition|(
name|removed
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removed a lock "
operator|+
name|hiveLock
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLocks
parameter_list|(
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"releaseLocks: "
operator|+
name|hiveLocks
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveLock
name|lock
range|:
name|hiveLocks
control|)
block|{
try|try
block|{
name|unlock
argument_list|(
name|lock
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e
parameter_list|)
block|{
comment|// Not sure why this method doesn't throw any exceptions,
comment|// but since the interface doesn't allow it we'll just swallow them and
comment|// move on.
comment|//This OK-ish since releaseLocks() is only called for RO/AC queries; it
comment|//would be really bad to eat exceptions here for write operations
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|)
throws|throws
name|LockException
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|(
name|locks
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|)
throws|throws
name|LockException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|ShowLocksResponse
name|getLocks
parameter_list|()
throws|throws
name|LockException
block|{
return|return
name|getLocks
argument_list|(
operator|new
name|ShowLocksRequest
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|ShowLocksResponse
name|getLocks
parameter_list|(
name|ShowLocksRequest
name|showLocksRequest
parameter_list|)
throws|throws
name|LockException
block|{
try|try
block|{
return|return
name|txnManager
operator|.
name|getMS
argument_list|()
operator|.
name|showLocks
argument_list|(
name|showLocksRequest
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
name|void
name|close
parameter_list|()
throws|throws
name|LockException
block|{
for|for
control|(
name|HiveLock
name|lock
range|:
name|locks
control|)
block|{
name|unlock
argument_list|(
name|lock
argument_list|)
expr_stmt|;
block|}
name|locks
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareRetry
parameter_list|()
throws|throws
name|LockException
block|{
comment|// NOP
block|}
annotation|@
name|Override
specifier|public
name|void
name|refresh
parameter_list|()
block|{
comment|// NOP
block|}
specifier|static
class|class
name|DbHiveLock
extends|extends
name|HiveLock
block|{
name|long
name|lockId
decl_stmt|;
name|String
name|queryId
decl_stmt|;
name|long
name|txnId
decl_stmt|;
name|List
argument_list|<
name|LockComponent
argument_list|>
name|components
decl_stmt|;
name|DbHiveLock
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|lockId
operator|=
name|id
expr_stmt|;
block|}
name|DbHiveLock
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|queryId
parameter_list|,
name|long
name|txnId
parameter_list|,
name|List
argument_list|<
name|LockComponent
argument_list|>
name|components
parameter_list|)
block|{
name|lockId
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
name|this
operator|.
name|txnId
operator|=
name|txnId
expr_stmt|;
name|this
operator|.
name|components
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|components
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockObject
name|getHiveLockObject
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockMode
name|getHiveLockMode
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mayContainComponents
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|LockComponent
argument_list|>
name|getHiveLockComponents
parameter_list|()
block|{
return|return
name|components
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|instanceof
name|DbHiveLock
condition|)
block|{
return|return
name|lockId
operator|==
operator|(
operator|(
name|DbHiveLock
operator|)
name|other
operator|)
operator|.
name|lockId
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|lockId
operator|%
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
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
name|JavaUtils
operator|.
name|lockIdToString
argument_list|(
name|lockId
argument_list|)
operator|+
literal|" queryId="
operator|+
name|queryId
operator|+
literal|" "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|txnId
argument_list|)
return|;
block|}
block|}
comment|/**    * Clear the memory of the locks in this object.  This won't clear the locks from the database.    * It is for use with    * {@link #DbLockManager(HiveConf, DbTxnManager)} .commitTxn} and    * {@link #DbLockManager(HiveConf, DbTxnManager)} .rollbackTxn}.    */
name|void
name|clearLocalLockRecords
parameter_list|()
block|{
name|locks
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// Sleep before we send checkLock again, but do it with a back off
comment|// off so we don't sit and hammer the metastore in a tight loop
specifier|private
name|void
name|backoff
parameter_list|()
block|{
name|nextSleep
operator|*=
literal|2
expr_stmt|;
if|if
condition|(
name|nextSleep
operator|>
name|MAX_SLEEP
condition|)
name|nextSleep
operator|=
name|MAX_SLEEP
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|nextSleep
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

