begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|messaging
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

begin_comment
comment|/**  * HCat message sent when an open transaction is done.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|OpenTxnMessage
extends|extends
name|EventMessage
block|{
specifier|protected
name|OpenTxnMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|OPEN_TXN
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the list of txns opened    * @return The lists of TxnIds    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Long
argument_list|>
name|getTxnIds
parameter_list|()
function_decl|;
block|}
end_class

end_unit

