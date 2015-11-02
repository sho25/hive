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
extends|extends
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|RecordReader
block|{
comment|/**    * Does the reader have more rows available.    * @return true if there are more rows    * @throws java.io.IOException    */
name|boolean
name|hasNext
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Read the next row.    * @param previous a row object that can be reused by the reader    * @return the row that was read    * @throws java.io.IOException    */
name|Object
name|next
parameter_list|(
name|Object
name|previous
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

