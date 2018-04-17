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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

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
comment|/**  * OpenTxnEvent  * Event generated for open transaction event.  */
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
name|OpenTxnEvent
extends|extends
name|ListenerEvent
block|{
specifier|private
name|List
argument_list|<
name|Long
argument_list|>
name|txnIds
decl_stmt|;
comment|/**    * @param txnIds List of unique identification for the transaction just opened.    * @param handler handler that is firing the event    */
specifier|public
name|OpenTxnEvent
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|txnIds
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
name|this
operator|.
name|txnIds
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|txnIds
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return List<Long> txnIds    */
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getTxnIds
parameter_list|()
block|{
return|return
name|txnIds
return|;
block|}
block|}
end_class

end_unit

