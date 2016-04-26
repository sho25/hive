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
name|orc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatch
import|;
end_import

begin_comment
comment|/**  * A row-by-row iterator for ORC files.  */
end_comment

begin_interface
specifier|public
interface|interface
name|RecordReader
block|{
comment|/**    * Read the next row batch. The size of the batch to read cannot be    * controlled by the callers. Caller need to look at    * VectorizedRowBatch.size of the retunred object to know the batch    * size read.    * @param batch a row batch object to read into    * @return were more rows available to read?    * @throws java.io.IOException    */
name|boolean
name|nextBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the row number of the row that will be returned by the following    * call to next().    * @return the row number from 0 to the number of rows in the file    * @throws java.io.IOException    */
name|long
name|getRowNumber
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the progress of the reader through the rows.    * @return a fraction between 0.0 and 1.0 of rows read    * @throws java.io.IOException    */
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Release the resources associated with the given reader.    * @throws java.io.IOException    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Seek to a particular row number.    */
name|void
name|seekToRow
parameter_list|(
name|long
name|rowCount
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

