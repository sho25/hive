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
name|security
operator|.
name|token
operator|.
name|delegation
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
name|hive
operator|.
name|thrift
operator|.
name|DelegationTokenIdentifier
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|AbstractDelegationTokenSelector
import|;
end_import

begin_comment
comment|/**  * A delegation token that is specialized for Hive  */
end_comment

begin_class
specifier|public
class|class
name|DelegationTokenSelector
extends|extends
name|AbstractDelegationTokenSelector
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
block|{
specifier|public
name|DelegationTokenSelector
parameter_list|()
block|{
name|super
argument_list|(
name|DelegationTokenIdentifier
operator|.
name|HIVE_DELEGATION_KIND
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

