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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
comment|/**    * Commit a transaction to make the writes visible for readers. Include    * other partitions that may have been added independently.    *    * @param partitions - extra partitions to commit.    * @throws StreamingException - if there are errors when committing the open transaction.    */
specifier|default
name|void
name|commitTransactionWithPartition
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
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
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
comment|/**    * Get the partitions used during the streaming. This partitions haven't    * been committed to the metastore.    * @return partitions.    */
specifier|default
name|Set
argument_list|<
name|String
argument_list|>
name|getPartitions
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Returns the file that would be used by the writer to write the rows.    * given the parameters    * @param partitionValues partition values    * @param bucketId bucket id    * @param minWriteId min write Id    * @param maxWriteId max write Id    * @param statementId statement Id    * @return the location of the file.    * @throws StreamingException when the path is not found    */
specifier|default
name|Path
name|getDeltaFileLocation
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|Integer
name|bucketId
parameter_list|,
name|Long
name|minWriteId
parameter_list|,
name|Long
name|maxWriteId
parameter_list|,
name|Integer
name|statementId
parameter_list|)
throws|throws
name|StreamingException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_interface

end_unit

