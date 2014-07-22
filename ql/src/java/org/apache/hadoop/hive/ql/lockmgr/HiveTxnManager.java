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

begin_comment
comment|/**  * An interface that allows Hive to manage transactions.  All classes  * implementing this should extend {@link HiveTxnManagerImpl} rather than  * implementing this directly.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveTxnManager
block|{
comment|/**    * Open a new transaction.    * @param user Hive user who is opening this transaction.    * @throws LockException if a transaction is already open.    */
name|void
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
comment|/**    * Acquire all of the locks needed by a query.  If used with a query that    * requires transactions, this should be called after {@link #openTxn(String)}.    * A list of acquired locks will be stored in the    * {@link org.apache.hadoop.hive.ql.Context} object and can be retrieved    * via {@link org.apache.hadoop.hive.ql.Context#getHiveLocks}.    * @param plan query plan    * @param ctx Context for this query    * @param username name of the user for this query    * @throws LockException if there is an error getting the locks    */
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
comment|/**    * Indicate whether this transaction manager returns information about locks in the new format    * for show locks or the old one.    * @return true if the new format should be used.    */
name|boolean
name|useNewShowLocksFormat
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

