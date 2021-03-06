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
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TRowSet
import|;
end_import

begin_interface
specifier|public
interface|interface
name|RowSet
extends|extends
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
block|{
name|RowSet
name|addRow
parameter_list|(
name|Object
index|[]
name|fields
parameter_list|)
function_decl|;
name|RowSet
name|extractSubset
parameter_list|(
name|int
name|maxRows
parameter_list|)
function_decl|;
name|int
name|numColumns
parameter_list|()
function_decl|;
name|int
name|numRows
parameter_list|()
function_decl|;
name|long
name|getStartOffset
parameter_list|()
function_decl|;
name|void
name|setStartOffset
parameter_list|(
name|long
name|startOffset
parameter_list|)
function_decl|;
name|TRowSet
name|toTRowSet
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

