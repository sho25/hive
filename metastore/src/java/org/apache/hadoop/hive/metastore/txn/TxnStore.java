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
operator|.
name|txn
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
name|annotations
operator|.
name|VisibleForTesting
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
name|InterfaceStability
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
name|RetrySemantics
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|Set
import|;
end_import

begin_comment
comment|/**  * A handler to answer transaction related calls that come into the metastore  * server.  *  * Note on log messages:  Please include txnid:X and lockid info using  * {@link org.apache.hadoop.hive.common.JavaUtils#txnIdToString(long)}  * and {@link org.apache.hadoop.hive.common.JavaUtils#lockIdToString(long)} in all messages.  * The txnid:X and lockid:Y matches how Thrift object toString() methods are generated,  * so keeping the format consistent makes grep'ing the logs much easier.  *  * Note on HIVE_LOCKS.hl_last_heartbeat.  * For locks that are part of transaction, we set this 0 (would rather set it to NULL but  * Currently the DB schema has this NOT NULL) and only update/read heartbeat from corresponding  * transaction in TXNS.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|TxnStore
block|{
enum|enum
name|MUTEX_KEY
block|{
name|Initiator
block|,
name|Cleaner
block|,
name|HouseKeeper
block|,
name|CompactionHistory
block|,
name|CheckLock
block|,
name|WriteSetCleaner
block|,
name|CompactionScheduler
block|}
comment|// Compactor states (Should really be enum)
name|String
name|INITIATED_RESPONSE
init|=
literal|"initiated"
decl_stmt|;
name|String
name|WORKING_RESPONSE
init|=
literal|"working"
decl_stmt|;
name|String
name|CLEANING_RESPONSE
init|=
literal|"ready for cleaning"
decl_stmt|;
name|String
name|FAILED_RESPONSE
init|=
literal|"failed"
decl_stmt|;
name|String
name|SUCCEEDED_RESPONSE
init|=
literal|"succeeded"
decl_stmt|;
name|String
name|ATTEMPTED_RESPONSE
init|=
literal|"attempted"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|TIMED_OUT_TXN_ABORT_BATCH_SIZE
init|=
literal|50000
decl_stmt|;
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
function_decl|;
comment|/**    * Get information about open transactions.  This gives extensive information about the    * transactions rather than just the list of transactions.  This should be used when the need    * is to see information about the transactions (e.g. show transactions).    * @return information about open transactions    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|GetOpenTxnsInfoResponse
name|getOpenTxnsInfo
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * Get list of valid transactions.  This gives just the list of transactions that are open.    * @return list of open transactions, as well as a high water mark.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|GetOpenTxnsResponse
name|getOpenTxns
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * Get the count for open transactions.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|void
name|countOpenTxns
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * Open a set of transactions    * @param rqst request to open transactions    * @return information on opened transactions    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|OpenTxnsResponse
name|openTxns
parameter_list|(
name|OpenTxnRequest
name|rqst
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Abort (rollback) a transaction.    * @param rqst info on transaction to abort    * @throws NoSuchTxnException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|abortTxn
parameter_list|(
name|AbortTxnRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|MetaException
throws|,
name|TxnAbortedException
function_decl|;
comment|/**    * Abort (rollback) a list of transactions in one request.    * @param rqst info on transactions to abort    * @throws NoSuchTxnException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|abortTxns
parameter_list|(
name|AbortTxnsRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|MetaException
function_decl|;
comment|/**    * Commit a transaction    * @param rqst info on transaction to commit    * @throws NoSuchTxnException    * @throws TxnAbortedException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|commitTxn
parameter_list|(
name|CommitTxnRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|TxnAbortedException
throws|,
name|MetaException
function_decl|;
comment|/**    * Obtain a lock.    * @param rqst information on the lock to obtain.  If the requester is part of a transaction    *             the txn information must be included in the lock request.    * @return info on the lock, including whether it was obtained.    * @throws NoSuchTxnException    * @throws TxnAbortedException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|CannotRetry
specifier|public
name|LockResponse
name|lock
parameter_list|(
name|LockRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|TxnAbortedException
throws|,
name|MetaException
function_decl|;
comment|/**    * Check whether a lock has been obtained.  This is used after {@link #lock} returned a wait    * state.    * @param rqst info on the lock to check    * @return info on the state of the lock    * @throws NoSuchTxnException    * @throws NoSuchLockException    * @throws TxnAbortedException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|LockResponse
name|checkLock
parameter_list|(
name|CheckLockRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|NoSuchLockException
throws|,
name|TxnAbortedException
throws|,
name|MetaException
function_decl|;
comment|/**    * Unlock a lock.  It is not legal to call this if the caller is part of a txn.  In that case    * the txn should be committed or aborted instead.  (Note someday this will change since    * multi-statement transactions will allow unlocking in the transaction.)    * @param rqst lock to unlock    * @throws NoSuchLockException    * @throws TxnOpenException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|unlock
parameter_list|(
name|UnlockRequest
name|rqst
parameter_list|)
throws|throws
name|NoSuchLockException
throws|,
name|TxnOpenException
throws|,
name|MetaException
function_decl|;
comment|/**    * Get information on current locks.    * @param rqst lock information to retrieve    * @return lock information.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|ShowLocksResponse
name|showLocks
parameter_list|(
name|ShowLocksRequest
name|rqst
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Send a heartbeat for a lock or a transaction    * @param ids lock and/or txn id to heartbeat    * @throws NoSuchTxnException    * @throws NoSuchLockException    * @throws TxnAbortedException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|heartbeat
parameter_list|(
name|HeartbeatRequest
name|ids
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|NoSuchLockException
throws|,
name|TxnAbortedException
throws|,
name|MetaException
function_decl|;
comment|/**    * Heartbeat a group of transactions together    * @param rqst set of transactions to heartbat    * @return info on txns that were heartbeated    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|HeartbeatTxnRangeResponse
name|heartbeatTxnRange
parameter_list|(
name|HeartbeatTxnRangeRequest
name|rqst
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Submit a compaction request into the queue.  This is called when a user manually requests a    * compaction.    * @param rqst information on what to compact    * @return id of the compaction that has been started or existing id if this resource is already scheduled    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|CompactionResponse
name|compact
parameter_list|(
name|CompactionRequest
name|rqst
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Show list of current compactions    * @param rqst info on which compactions to show    * @return compaction information    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|ShowCompactResponse
name|showCompact
parameter_list|(
name|ShowCompactRequest
name|rqst
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Add information on a set of dynamic partitions that participated in a transaction.    * @param rqst dynamic partition info.    * @throws NoSuchTxnException    * @throws TxnAbortedException    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|addDynamicPartitions
parameter_list|(
name|AddDynamicPartitions
name|rqst
parameter_list|)
throws|throws
name|NoSuchTxnException
throws|,
name|TxnAbortedException
throws|,
name|MetaException
function_decl|;
comment|/**    * Clean up corresponding records in metastore tables    * @param type Hive object type    * @param db database object    * @param table table object    * @param partitionIterator partition iterator    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|cleanupRecords
parameter_list|(
name|HiveObjectType
name|type
parameter_list|,
name|Database
name|db
parameter_list|,
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|partitionIterator
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Timeout transactions and/or locks.  This should only be called by the compactor.    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|performTimeOuts
parameter_list|()
function_decl|;
comment|/**    * This will look through the completed_txn_components table and look for partitions or tables    * that may be ready for compaction.  Also, look through txns and txn_components tables for    * aborted transactions that we should add to the list.    * @param maxAborted Maximum number of aborted queries to allow before marking this as a    *                   potential compaction.    * @return list of CompactionInfo structs.  These will not have id, type,    * or runAs set since these are only potential compactions not actual ones.    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|Set
argument_list|<
name|CompactionInfo
argument_list|>
name|findPotentialCompactions
parameter_list|(
name|int
name|maxAborted
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Sets the user to run as.  This is for the case    * where the request was generated by the user and so the worker must set this value later.    * @param cq_id id of this entry in the queue    * @param user user to run the jobs as    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|setRunAs
parameter_list|(
name|long
name|cq_id
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * This will grab the next compaction request off of    * the queue, and assign it to the worker.    * @param workerId id of the worker calling this, will be recorded in the db    * @return an info element for this compaction request, or null if there is no work to do now.    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|CompactionInfo
name|findNextToCompact
parameter_list|(
name|String
name|workerId
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * This will mark an entry in the queue as compacted    * and put it in the ready to clean state.    * @param info info on the compaction entry to mark as compacted.    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|markCompacted
parameter_list|(
name|CompactionInfo
name|info
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Find entries in the queue that are ready to    * be cleaned.    * @return information on the entry in the queue.    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|List
argument_list|<
name|CompactionInfo
argument_list|>
name|findReadyToClean
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * This will remove an entry from the queue after    * it has been compacted.    *     * @param info info on the compaction entry to remove    */
annotation|@
name|RetrySemantics
operator|.
name|CannotRetry
specifier|public
name|void
name|markCleaned
parameter_list|(
name|CompactionInfo
name|info
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Mark a compaction entry as failed.  This will move it to the compaction history queue with a    * failed status.  It will NOT clean up aborted transactions in the table/partition associated    * with this compaction.    * @param info information on the compaction that failed.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|CannotRetry
specifier|public
name|void
name|markFailed
parameter_list|(
name|CompactionInfo
name|info
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Clean up aborted transactions from txns that have no components in txn_components.  The reson such    * txns exist can be that now work was done in this txn (e.g. Streaming opened TransactionBatch and    * abandoned it w/o doing any work) or due to {@link #markCleaned(CompactionInfo)} being called.    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|cleanEmptyAbortedTxns
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * This will take all entries assigned to workers    * on a host return them to INITIATED state.  The initiator should use this at start up to    * clean entries from any workers that were in the middle of compacting when the metastore    * shutdown.  It does not reset entries from worker threads on other hosts as those may still    * be working.    * @param hostname Name of this host.  It is assumed this prefixes the thread's worker id,    *                 so that like hostname% will match the worker id.    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|revokeFromLocalWorkers
parameter_list|(
name|String
name|hostname
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * This call will return all compaction queue    * entries assigned to a worker but over the timeout back to the initiated state.    * This should be called by the initiator on start up and occasionally when running to clean up    * after dead threads.  At start up {@link #revokeFromLocalWorkers(String)} should be called    * first.    * @param timeout number of milliseconds since start time that should elapse before a worker is    *                declared dead.    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|revokeTimedoutWorkers
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Queries metastore DB directly to find columns in the table which have statistics information.    * If {@code ci} includes partition info then per partition stats info is examined, otherwise    * table level stats are examined.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|findColumnsWithStats
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Record the highest txn id that the {@code ci} compaction job will pay attention to.    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|setCompactionHighestTxnId
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|long
name|highestTxnId
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * For any given compactable entity (partition, table if not partitioned) the history of compactions    * may look like "sssfffaaasffss", for example.  The idea is to retain the tail (most recent) of the    * history such that a configurable number of each type of state is present.  Any other entries    * can be purged.  This scheme has advantage of always retaining the last failure/success even if    * it's not recent.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|purgeCompactionHistory
parameter_list|()
throws|throws
name|MetaException
function_decl|;
comment|/**    * WriteSet tracking is used to ensure proper transaction isolation.  This method deletes the     * transaction metadata once it becomes unnecessary.      */
annotation|@
name|RetrySemantics
operator|.
name|SafeToRetry
specifier|public
name|void
name|performWriteSetGC
parameter_list|()
function_decl|;
comment|/**    * Determine if there are enough consecutive failures compacting a table or partition that no    * new automatic compactions should be scheduled.  User initiated compactions do not do this    * check.    * @param ci  Table or partition to check.    * @return true if it is ok to compact, false if there have been too many failures.    * @throws MetaException    */
annotation|@
name|RetrySemantics
operator|.
name|ReadOnly
specifier|public
name|boolean
name|checkFailedCompactions
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|)
throws|throws
name|MetaException
function_decl|;
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|numLocksInLockTable
parameter_list|()
throws|throws
name|SQLException
throws|,
name|MetaException
function_decl|;
annotation|@
name|VisibleForTesting
name|long
name|setTimeout
parameter_list|(
name|long
name|milliseconds
parameter_list|)
function_decl|;
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|MutexAPI
name|getMutexAPI
parameter_list|()
function_decl|;
comment|/**    * This is primarily designed to provide coarse grained mutex support to operations running    * inside the Metastore (of which there could be several instances).  The initial goal is to     * ensure that various sub-processes of the Compactor don't step on each other.    *     * In RDMBS world each {@code LockHandle} uses a java.sql.Connection so use it sparingly.    */
specifier|public
specifier|static
interface|interface
name|MutexAPI
block|{
comment|/**      * The {@code key} is name of the lock. Will acquire and exclusive lock or block.  It retuns      * a handle which must be used to release the lock.  Each invocation returns a new handle.      */
specifier|public
name|LockHandle
name|acquireLock
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**      * Same as {@link #acquireLock(String)} but takes an already existing handle as input.  This       * will associate the lock on {@code key} with the same handle.  All locks associated with      * the same handle will be released together.      * @param handle not NULL      */
specifier|public
name|void
name|acquireLock
parameter_list|(
name|String
name|key
parameter_list|,
name|LockHandle
name|handle
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|static
interface|interface
name|LockHandle
block|{
comment|/**        * Releases all locks associated with this handle.        */
specifier|public
name|void
name|releaseLocks
parameter_list|()
function_decl|;
block|}
block|}
comment|/**    * Once a {@link java.util.concurrent.ThreadPoolExecutor.Worker} submits a job to the cluster,    * it calls this to update the metadata.    * @param id {@link CompactionInfo#id}    */
annotation|@
name|RetrySemantics
operator|.
name|Idempotent
specifier|public
name|void
name|setHadoopJobId
parameter_list|(
name|String
name|hadoopJobId
parameter_list|,
name|long
name|id
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

