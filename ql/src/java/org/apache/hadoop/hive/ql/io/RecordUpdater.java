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
name|io
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
name|serde2
operator|.
name|SerDeStats
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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

begin_comment
comment|/**  * API for supporting updating records.  */
end_comment

begin_interface
specifier|public
interface|interface
name|RecordUpdater
block|{
comment|/**    * Insert a new record into the table.    * @param currentTransaction the transaction id of the current transaction.    * @param row the row of data to insert    * @throws IOException    */
name|void
name|insert
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Update an old record with a new set of values.    * @param currentTransaction the current transaction id    * @param originalTransaction the row's original transaction id    * @param rowId the original row id    * @param row the new values for the row    * @throws IOException    */
name|void
name|update
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|long
name|originalTransaction
parameter_list|,
name|long
name|rowId
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete a row from the table.    * @param currentTransaction the current transaction id    * @param originalTransaction the rows original transaction id    * @param rowId the original row id    * @throws IOException    */
name|void
name|delete
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|long
name|originalTransaction
parameter_list|,
name|long
name|rowId
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Flush the current set of rows to the underlying file system, so that    * they are available to readers. Most implementations will need to write    * additional state information when this is called, so it should only be    * called during streaming when a transaction is finished, but the    * RecordUpdater can't be closed yet.    * @throws IOException    */
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Close this updater. No further calls are legal after this.    * @param abort Can the data since the last flush be discarded?    * @throws IOException    */
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the statistics information    * @return SerDeStats    */
name|SerDeStats
name|getStats
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

