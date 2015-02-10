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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|orc
operator|.
name|streams
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
name|ColumnVector
import|;
end_import

begin_comment
comment|/**  * Column stream reader interface.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ColumnStream
block|{
comment|/**    * Closes all internal stream readers.    * @throws IOException    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns next column vector from the stream.    * @param previousVector    * @param batchSize    * @return    * @throws IOException    */
specifier|public
name|ColumnVector
name|nextVector
parameter_list|(
name|ColumnVector
name|previousVector
parameter_list|,
name|int
name|batchSize
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

