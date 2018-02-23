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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  * Represents a set of Transactions returned by Hive. Supports opening, writing to  * and commiting/aborting each transaction. The interface is designed to ensure  * transactions in a batch are used up sequentially. To stream to the same HiveEndPoint  * concurrently, create separate StreamingConnections.  *  * Note on thread safety: At most 2 threads can run through a given TransactionBatch at the same  * time.  One thread may call {@link #heartbeat()} and the other all other methods.  * Violating this may result in "out of sequence response".  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|TransactionBatch
block|{
enum|enum
name|TxnState
block|{
name|INACTIVE
argument_list|(
literal|"I"
argument_list|)
block|,
name|OPEN
argument_list|(
literal|"O"
argument_list|)
block|,
name|COMMITTED
argument_list|(
literal|"C"
argument_list|)
block|,
name|ABORTED
argument_list|(
literal|"A"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|code
decl_stmt|;
name|TxnState
parameter_list|(
name|String
name|code
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
block|}
block|;
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|code
return|;
block|}
block|}
comment|/**    * Activate the next available transaction in the current transaction batch.    * @throws StreamingException if not able to switch to next Txn    * @throws InterruptedException if call in interrupted    */
name|void
name|beginNextTransaction
parameter_list|()
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Get Id of currently open transaction.    * @return transaction id    */
name|Long
name|getCurrentTxnId
parameter_list|()
function_decl|;
comment|/**    * Get write Id mapping to currently open transaction.    * @return write id    */
name|Long
name|getCurrentWriteId
parameter_list|()
function_decl|;
comment|/**    * get state of current transaction.    */
name|TxnState
name|getCurrentTransactionState
parameter_list|()
function_decl|;
comment|/**    * Commit the currently open transaction.    * @throws StreamingException if there are errors committing    * @throws InterruptedException if call in interrupted    */
name|void
name|commit
parameter_list|()
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Abort the currently open transaction.    * @throws StreamingException if there are errors    * @throws InterruptedException if call in interrupted    */
name|void
name|abort
parameter_list|()
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Remaining transactions are the ones that are not committed or aborted or open.    * Current open transaction is not considered part of remaining txns.    * @return number of transactions remaining this batch.    */
name|int
name|remainingTransactions
parameter_list|()
function_decl|;
comment|/**    *  Write record using RecordWriter.    * @param record  the data to be written    * @throws StreamingException if there are errors when writing    * @throws InterruptedException if call in interrupted    */
name|void
name|write
parameter_list|(
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    *  Write records using RecordWriter.    * @throws StreamingException if there are errors when writing    * @throws InterruptedException if call in interrupted    */
name|void
name|write
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|records
parameter_list|)
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Issues a heartbeat to hive metastore on the current and remaining txn ids    * to keep them from expiring.    * @throws StreamingException if there are errors    */
name|void
name|heartbeat
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Close the TransactionBatch.    * @throws StreamingException if there are errors closing batch    * @throws InterruptedException if call in interrupted    */
name|void
name|close
parameter_list|()
throws|throws
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
name|boolean
name|isClosed
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

