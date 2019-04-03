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
name|streaming
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
name|metastore
operator|.
name|api
operator|.
name|TxnToWriteId
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
comment|/**  * Common interface for transaction in HiveStreamingConnection.  */
end_comment

begin_interface
specifier|public
interface|interface
name|StreamingTransaction
block|{
comment|/**    * get ready for the next transaction.    * @throws StreamingException    */
name|void
name|beginNextTransaction
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * commit transaction.    * @throws StreamingException    */
name|void
name|commit
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Commit transaction and sent to the metastore the created partitions    * in the process.    * @param partitions to commit.    * @throws StreamingException    */
name|void
name|commit
parameter_list|(
annotation|@
name|Nullable
name|Set
argument_list|<
name|String
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Commits atomically together with a key and a value.    * @param partitions to commit.    * @param key to commit.    * @param value to commit.    * @throws StreamingException    */
name|void
name|commit
parameter_list|(
annotation|@
name|Nullable
name|Set
argument_list|<
name|String
argument_list|>
name|partitions
parameter_list|,
annotation|@
name|Nullable
name|String
name|key
parameter_list|,
annotation|@
name|Nullable
name|String
name|value
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Abort a transaction.    * @throws StreamingException    */
name|void
name|abort
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Write data withing a transaction. This expectects beginNextTransaction    * to have been called before this and commit to be called after.    * @param record bytes to write.    * @throws StreamingException    */
name|void
name|write
parameter_list|(
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Write data within a transaction.    * @param stream stream to write.    * @throws StreamingException    */
name|void
name|write
parameter_list|(
name|InputStream
name|stream
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Free/close resources used by the streaming transaction.    * @throws StreamingException    */
name|void
name|close
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * @return true if closed.    */
name|boolean
name|isClosed
parameter_list|()
function_decl|;
comment|/**    * @return the state of the current transaction.    */
name|HiveStreamingConnection
operator|.
name|TxnState
name|getCurrentTransactionState
parameter_list|()
function_decl|;
comment|/**    * @return remaining number of transactions    */
name|int
name|remainingTransactions
parameter_list|()
function_decl|;
comment|/**    * @return the current transaction id being used    */
name|long
name|getCurrentTxnId
parameter_list|()
function_decl|;
comment|/**    * @return the current write id being used    */
name|long
name|getCurrentWriteId
parameter_list|()
function_decl|;
comment|/**    * Get the partitions that were used in this transaction. They may have    * been created    * @return list of partitions    */
name|Set
argument_list|<
name|String
argument_list|>
name|getPartitions
parameter_list|()
function_decl|;
comment|/**    * @return get the paris for transaction ids&lt;--&gt; write ids    */
name|List
argument_list|<
name|TxnToWriteId
argument_list|>
name|getTxnToWriteIds
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

