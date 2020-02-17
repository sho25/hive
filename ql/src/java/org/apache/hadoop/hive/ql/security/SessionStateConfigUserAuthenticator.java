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
name|ArrayList
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
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * Authenticator to be used for testing and debugging. This picks the user.name  * set in SessionState config, if that is null, it returns value of  * System property user.name  */
end_comment

begin_class
specifier|public
class|class
name|SessionStateConfigUserAuthenticator
implements|implements
name|HiveAuthenticationProvider
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ISessionAuthState
name|sessionState
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
return|return
name|groupNames
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
name|String
name|newUserName
init|=
name|sessionState
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"user.name"
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|newUserName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|newUserName
return|;
block|}
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

