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
name|LongColumnVector
import|;
end_import

begin_comment
comment|/**  * Interface for reading integers.  */
end_comment

begin_interface
interface|interface
name|IntegerReader
block|{
comment|/**    * Seek to the position provided by index.    * @param index    * @throws IOException    */
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Skip number of specified rows.    * @param numValues    * @throws IOException    */
name|void
name|skip
parameter_list|(
name|long
name|numValues
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Check if there are any more values left.    * @return    * @throws IOException    */
name|boolean
name|hasNext
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return the next available value.    * @return    * @throws IOException    */
name|long
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return the next available vector for values.    * @return    * @throws IOException    */
name|void
name|nextVector
parameter_list|(
name|LongColumnVector
name|previous
parameter_list|,
name|long
name|previousLen
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

