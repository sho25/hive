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
name|serde2
operator|.
name|thrift
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * An interface for TProtocols that actually write out nulls - This should be  * for all those that don't actually use fieldids in the written data like  * TCTLSeparatedProtocol.  *   */
end_comment

begin_interface
specifier|public
interface|interface
name|WriteNullsProtocol
block|{
comment|/**    * Was the last primitive read really a NULL. Need only be called when the    * value of the primitive was 0. ie the protocol should return 0 on nulls and    * the caller will then check if it was actually null For boolean this is    * false.    */
specifier|public
name|boolean
name|lastPrimitiveWasNull
parameter_list|()
throws|throws
name|TException
function_decl|;
comment|/**    * Write a null    */
specifier|public
name|void
name|writeNull
parameter_list|()
throws|throws
name|TException
function_decl|;
block|}
end_interface

end_unit

