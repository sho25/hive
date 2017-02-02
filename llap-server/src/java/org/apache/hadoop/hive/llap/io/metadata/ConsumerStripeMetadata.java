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
name|metadata
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|ColumnEncoding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|RowIndex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|RowIndexEntry
import|;
end_import

begin_interface
specifier|public
interface|interface
name|ConsumerStripeMetadata
block|{
name|int
name|getStripeIx
parameter_list|()
function_decl|;
name|long
name|getRowCount
parameter_list|()
function_decl|;
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|getEncodings
parameter_list|()
function_decl|;
name|String
name|getWriterTimezone
parameter_list|()
function_decl|;
name|RowIndexEntry
name|getRowIndexEntry
parameter_list|(
name|int
name|colIx
parameter_list|,
name|int
name|rgIx
parameter_list|)
function_decl|;
comment|// TODO: remove?
name|RowIndex
index|[]
name|getRowIndexes
parameter_list|()
function_decl|;
name|boolean
name|supportsRowIndexes
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

