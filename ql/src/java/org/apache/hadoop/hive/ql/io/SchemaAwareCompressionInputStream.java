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
name|io
operator|.
name|compress
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  * SchemaAwareCompressionInputStream adds the ability to inform the compression  * stream what column is being read.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|SchemaAwareCompressionInputStream
extends|extends
name|CompressionInputStream
block|{
specifier|protected
name|SchemaAwareCompressionInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**    * The column being read    *    * @param columnIndex the index of the column. Use -1 for non-column data    */
specifier|public
specifier|abstract
name|void
name|setColumnIndex
parameter_list|(
name|int
name|columnIndex
parameter_list|)
function_decl|;
block|}
end_class

end_unit

