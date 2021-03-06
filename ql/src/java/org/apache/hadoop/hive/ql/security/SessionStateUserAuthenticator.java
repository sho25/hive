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
name|ql
operator|.
name|security
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|metadata
operator|.
name|HiveException
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
name|session
operator|.
name|ISessionAuthState
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
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Authenticator that returns the userName set in SessionState. For use when authorizing with HS2  * so that HS2 can set the user for the session through SessionState  */
end_comment

begin_class
specifier|public
class|class
name|SessionStateUserAuthenticator
implements|implements
name|HiveAuthenticationProvider
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ISessionAuthState
name|sessionState
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|groups
decl_stmt|;
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getGroupNames
parameter_list|()
block|{
comment|// In case of embedded hs2, sessionState.getUserName()=null
if|if
condition|(
name|groups
operator|==
literal|null
operator|&&
name|sessionState
operator|.
name|getUserName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|groups
operator|=
name|UserGroupInformation
operator|.
name|createRemoteUser
argument_list|(
name|sessionState
operator|.
name|getUserName
argument_list|()
argument_list|)
operator|.
name|getGroups
argument_list|()
expr_stmt|;
block|}
return|return
name|groups
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|sessionState
operator|.
name|getUserName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|destroy
parameter_list|()
throws|throws
name|HiveException
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|arg0
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|setSessionState
parameter_list|(
name|ISessionAuthState
name|sessionState
parameter_list|)
block|{
name|this
operator|.
name|sessionState
operator|=
name|sessionState
expr_stmt|;
block|}
block|}
end_class

end_unit

