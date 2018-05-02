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

begin_comment
comment|/**  * @deprecated as of Hive 3.0.0, replaced by {@link org.apache.hive.streaming.RecordWriter}  */
end_comment

begin_interface
annotation|@
name|Deprecated
specifier|public
interface|interface
name|RecordWriter
block|{
comment|/** Writes using a hive RecordUpdater    *    * @param writeId the write ID of the table mapping to Txn in which the write occurs    * @param record the record to be written    */
name|void
name|write
parameter_list|(
name|long
name|writeId
parameter_list|,
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/** Flush records from buffer. Invoked by TransactionBatch.commit() */
name|void
name|flush
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/** Clear bufferred writes. Invoked by TransactionBatch.abort() */
name|void
name|clear
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/** Acquire a new RecordUpdater. Invoked when    * StreamingConnection.fetchTransactionBatch() is called */
name|void
name|newBatch
parameter_list|(
name|Long
name|minWriteId
parameter_list|,
name|Long
name|maxWriteID
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/** Close the RecordUpdater. Invoked by TransactionBatch.close() */
name|void
name|closeBatch
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
block|}
end_interface

end_unit

