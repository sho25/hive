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
name|Set
import|;
end_import

begin_interface
specifier|public
interface|interface
name|RecordWriter
block|{
comment|/**    * Initialize record writer.    *    * @param connection - streaming connection    * @param minWriteId - min write id    * @param maxWriteID - max write id    * @throws StreamingException - thrown when initialization failed    */
name|void
name|init
parameter_list|(
name|StreamingConnection
name|connection
parameter_list|,
name|long
name|minWriteId
parameter_list|,
name|long
name|maxWriteID
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Writes using a hive RecordUpdater.    *    * @param writeId - the write ID of the table mapping to Txn in which the write occurs    * @param record  - the record to be written    * @throws StreamingException - thrown when write fails    */
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
comment|/**    * Writes using a hive RecordUpdater. The specified input stream will be automatically closed    * by the API after reading all the records out of it.    *    * @param writeId     - the write ID of the table mapping to Txn in which the write occurs    * @param inputStream - the record to be written    * @throws StreamingException - thrown when write fails    */
name|void
name|write
parameter_list|(
name|long
name|writeId
parameter_list|,
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Flush records from buffer. Invoked by TransactionBatch.commitTransaction()    *    * @throws StreamingException - thrown when flush fails    */
name|void
name|flush
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Close the RecordUpdater. Invoked by TransactionBatch.close()    *    * @throws StreamingException - thrown when record writer cannot be closed.    */
name|void
name|close
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Get the set of partitions that were added by the record writer.    *    * @return - set of partitions    */
name|Set
argument_list|<
name|String
argument_list|>
name|getPartitions
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

