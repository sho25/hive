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

begin_interface
specifier|public
interface|interface
name|StreamingConnection
extends|extends
name|ConnectionInfo
extends|,
name|PartitionHandler
block|{
comment|/**    * Returns hive configuration object used during connection creation.    *    * @return - hive conf    */
name|HiveConf
name|getHiveConf
parameter_list|()
function_decl|;
comment|/**    * Begin a transaction for writing.    *    * @throws StreamingException - if there are errors when beginning transaction    */
name|void
name|beginTransaction
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Write record using RecordWriter.    *    * @param record - the data to be written    * @throws StreamingException - if there are errors when writing    */
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
comment|/**    * Write record using RecordWriter.    *    * @param inputStream - input stream of records    * @throws StreamingException - if there are errors when writing    */
name|void
name|write
parameter_list|(
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Commit a transaction to make the writes visible for readers.    *    * @throws StreamingException - if there are errors when committing the open transaction    */
name|void
name|commitTransaction
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Manually abort the opened transaction.    *    * @throws StreamingException - if there are errors when aborting the transaction    */
name|void
name|abortTransaction
parameter_list|()
throws|throws
name|StreamingException
function_decl|;
comment|/**    * Closes streaming connection.    */
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Gets stats about the streaming connection.    *    * @return - connection stats    */
name|ConnectionStats
name|getConnectionStats
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

