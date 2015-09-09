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
name|exec
operator|.
name|tez
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

begin_comment
comment|/**  * Key-values interface for the Reader used by ReduceRecordSource  */
end_comment

begin_interface
specifier|public
interface|interface
name|KeyValuesAdapter
block|{
comment|/**    * Get the key for current record    * @return    * @throws IOException    */
name|Object
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the values for the current record    * @return    * @throws IOException    */
name|Iterable
argument_list|<
name|Object
argument_list|>
name|getCurrentValues
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Move to the next record    * @return true if successful, false if there are no more records to process    * @throws IOException    */
name|boolean
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

