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
name|thrift
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
name|io
operator|.
name|Text
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
name|AbstractDelegationTokenIdentifier
import|;
end_import

begin_comment
comment|/**  * A delegation token identifier that is specific to Hive.  */
end_comment

begin_class
specifier|public
class|class
name|DelegationTokenIdentifier23
extends|extends
name|AbstractDelegationTokenIdentifier
block|{
specifier|public
specifier|static
specifier|final
name|Text
name|HIVE_DELEGATION_KIND
init|=
operator|new
name|Text
argument_list|(
literal|"HIVE_DELEGATION_TOKEN"
argument_list|)
decl_stmt|;
comment|/**    * Create an empty delegation token identifier for reading into.    */
specifier|public
name|DelegationTokenIdentifier23
parameter_list|()
block|{   }
comment|/**    * Create a new delegation token identifier    * @param owner the effective username of the token owner    * @param renewer the username of the renewer    * @param realUser the real username of the token owner    */
specifier|public
name|DelegationTokenIdentifier23
parameter_list|(
name|Text
name|owner
parameter_list|,
name|Text
name|renewer
parameter_list|,
name|Text
name|realUser
parameter_list|)
block|{
name|super
argument_list|(
name|owner
argument_list|,
name|renewer
argument_list|,
name|realUser
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Text
name|getKind
parameter_list|()
block|{
return|return
name|HIVE_DELEGATION_KIND
return|;
block|}
block|}
end_class

end_unit

