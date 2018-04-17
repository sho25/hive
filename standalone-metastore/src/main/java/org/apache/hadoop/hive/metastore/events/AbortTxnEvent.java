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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|events
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|metastore
operator|.
name|IHMSHandler
import|;
end_import

begin_comment
comment|/**  * AbortTxnEvent  * Event generated for roll backing a transaction  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|AbortTxnEvent
extends|extends
name|ListenerEvent
block|{
specifier|private
specifier|final
name|Long
name|txnId
decl_stmt|;
comment|/**    *    * @param transactionId Unique identification for the transaction that got rolledback.    * @param handler handler that is firing the event    */
specifier|public
name|AbortTxnEvent
parameter_list|(
name|Long
name|transactionId
parameter_list|,
name|IHMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|txnId
operator|=
name|transactionId
expr_stmt|;
block|}
comment|/**    * @return Long txnId    */
specifier|public
name|Long
name|getTxnId
parameter_list|()
block|{
return|return
name|txnId
return|;
block|}
block|}
end_class

end_unit

