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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
package|;
end_package

begin_comment
comment|/**  * Represents a connection to a HiveEndPoint. Used to acquire transaction batches.  */
end_comment

begin_interface
specifier|public
interface|interface
name|StreamingConnection
block|{
comment|/**    * Acquires a new batch of transactions from Hive.     * @param numTransactionsHint is a hint from client indicating how many transactions client needs.    * @param writer  Used to write record. The same writer instance can    *                      be shared with another TransactionBatch (to the same endpoint)    *                      only after the first TransactionBatch has been closed.    *                      Writer will be closed when the TransactionBatch is closed.    * @return    * @throws ConnectionError    * @throws InvalidPartition    * @throws StreamingException    * @return a batch of transactions    */
specifier|public
name|TransactionBatch
name|fetchTransactionBatch
parameter_list|(
name|int
name|numTransactionsHint
parameter_list|,
name|RecordWriter
name|writer
parameter_list|)
throws|throws
name|ConnectionError
throws|,
name|StreamingException
throws|,
name|InterruptedException
function_decl|;
comment|/**    * Close connection    */
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

