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
name|plan
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
name|plan
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
name|plan
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
name|plan
operator|.
name|UnlockTableDesc
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
comment|/**  * An interface that allows Hive to manage transactions.  All classes  * implementing this should extend {@link HiveTxnManagerImpl} rather than  * implementing this directly.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveTxnManager
block|{
comment|/**    * Open a new transaction.    * @param user Hive user who is opening this transaction.    * @return The new transaction id    * @throws LockException if a transaction is already open.    */
name|long
name|openTxn
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|LockException
function_decl|;
comment|/**    * Get the lock manager.  This must be used rather than instantiating an    * instance of the lock manager directly as the transaction manager will    * choose which lock manager to instantiate.    * @return the instance of the lock manager    * @throws LockException if there is an issue obtaining the lock manager.    */
name|HiveLockManager
name|getLockManager
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * Acquire all of the locks needed by a query.  If used with a query that    * requires transactions, this should be called after {@link #openTxn(String)}.    * A list of acquired locks will be stored in the    * {@link org.apache.hadoop.hive.ql.Context} object and can be retrieved    * via {@link org.apache.hadoop.hive.ql.Context#getHiveLocks}.    *    * @param plan query plan    * @param ctx Context for this query    * @param username name of the user for this query    * @throws LockException if there is an error getting the locks.  Use {@link LockException#getCanonicalErrorMsg()}    * to get more info on how to handle the exception.    */
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
function_decl|;
comment|/**    * Release specified locks.    * Transaction aware TxnManagers, which has {@code supportsAcid() == true},    * will track locks internally and ignore this parameter    * @param hiveLocks The list of locks to be released.    */
name|void
name|releaseLocks
parameter_list|(
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
parameter_list|)
throws|throws
name|LockException
function_decl|;
comment|/**    * Commit the current transaction.  This will release all locks obtained in    * {@link #acquireLocks(org.apache.hadoop.hive.ql.QueryPlan,    * org.apache.hadoop.hive.ql.Context, java.lang.String)}.    * @throws LockException if there is no current transaction or the    * transaction has already been committed or aborted.    */
name|void
name|commitTxn
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * Abort the current transaction.  This will release all locks obtained in    * {@link #acquireLocks(org.apache.hadoop.hive.ql.QueryPlan,    * org.apache.hadoop.hive.ql.Context, java.lang.String)}.    * @throws LockException if there is no current transaction or the    * transaction has already been committed or aborted.    */
name|void
name|rollbackTxn
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * Send a heartbeat to the transaction management storage so other Hive    * clients know that the transaction and locks held by this client are    * still valid.  For implementations that do not require heartbeats this    * can be a no-op.    * @throws LockException If current transaction exists or the transaction    * has already been committed or aborted.    */
name|void
name|heartbeat
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * Get the transactions that are currently valid.  The resulting    * {@link ValidTxnList} object is a thrift object and can    * be  passed to  the processing    * tasks for use in the reading the data.  This call should be made once up    * front by the planner and should never be called on the backend,    * as this will violate the isolation level semantics.    * @return list of valid transactions.    * @throws LockException    */
name|ValidTxnList
name|getValidTxns
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * This call closes down the transaction manager.  All open transactions    * are aborted.  If no transactions are open but locks are held those locks    * are released.  This method should be called if processing of a session    * is being halted in an abnormal way.  It avoids locks and transactions    * timing out.    */
name|void
name|closeTxnManager
parameter_list|()
function_decl|;
comment|/**    * Indicate whether this lock manager supports the use of<code>lock    *<i>database</i></code> or<code>lock<i>table</i></code>.    * @return    */
name|boolean
name|supportsExplicitLock
parameter_list|()
function_decl|;
comment|/**    * This function is called to lock the table when explicit lock command is    * issued on a table.    * @param hiveDB    an object to communicate with the metastore    * @param lockTbl   table locking info, such as table name, locking mode    * @return 0 if the locking succeeds, 1 otherwise.    * @throws HiveException    */
name|int
name|lockTable
parameter_list|(
name|Hive
name|hiveDB
parameter_list|,
name|LockTableDesc
name|lockTbl
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * This function is called to unlock the table when explicit unlock command is    * issued on a table.    * @param hiveDB    an object to communicate with the metastore    * @param unlockTbl table unlocking info, such as table name    * @return 0 if the locking succeeds, 1 otherwise.    * @throws HiveException    */
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
function_decl|;
comment|/**    * This function is called to lock the database when explicit lock command is    * issued on a database.    * @param hiveDB    an object to communicate with the metastore    * @param lockDb    database locking info, such as database name, locking mode    * @return 0 if the locking succeeds, 1 otherwise.    * @throws HiveException    */
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
function_decl|;
comment|/**    * This function is called to unlock the database when explicit unlock command    * is issued on a database.    * @param hiveDB    an object to communicate with the metastore    * @param unlockDb  database unlocking info, such as database name    * @return 0 if the locking succeeds, 1 otherwise.    * @throws HiveException    */
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
function_decl|;
comment|/**    * Indicate whether this transaction manager returns information about locks in the new format    * for show locks or the old one.    * @return true if the new format should be used.    */
name|boolean
name|useNewShowLocksFormat
parameter_list|()
function_decl|;
comment|/**    * Indicate whether this transaction manager supports ACID operations    * @return true if this transaction manager does ACID    */
name|boolean
name|supportsAcid
parameter_list|()
function_decl|;
comment|/**    * This behaves exactly as    * https://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html#setAutoCommit(boolean)    */
name|void
name|setAutoCommit
parameter_list|(
name|boolean
name|autoCommit
parameter_list|)
throws|throws
name|LockException
function_decl|;
comment|/**    * This behaves exactly as    * https://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html#getAutoCommit()    */
name|boolean
name|getAutoCommit
parameter_list|()
function_decl|;
name|boolean
name|isTxnOpen
parameter_list|()
function_decl|;
comment|/**    * if {@code isTxnOpen()}, returns the currently active transaction ID    */
name|long
name|getCurrentTxnId
parameter_list|()
function_decl|;
comment|/**    * 0..N Id of current statement within currently opened transaction    */
name|int
name|getStatementId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

