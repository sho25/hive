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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
import|;
end_import

begin_comment
comment|// This file is copies from org.apache.hive.service.auth.AuthenticationProviderFactory. Need to
end_comment

begin_comment
comment|// deduplicate this code.
end_comment

begin_comment
comment|/**  * This class helps select a {@link MetaStorePasswdAuthenticationProvider} for a given {@code  * AuthMethod}.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|MetaStoreAuthenticationProviderFactory
block|{
specifier|public
enum|enum
name|AuthMethods
block|{
name|LDAP
argument_list|(
literal|"LDAP"
argument_list|)
block|,
name|PAM
argument_list|(
literal|"PAM"
argument_list|)
block|,
name|CUSTOM
argument_list|(
literal|"CUSTOM"
argument_list|)
block|,
name|NONE
argument_list|(
literal|"NONE"
argument_list|)
block|,
name|CONFIG
argument_list|(
literal|"CONFIG"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|authMethod
decl_stmt|;
name|AuthMethods
parameter_list|(
name|String
name|authMethod
parameter_list|)
block|{
name|this
operator|.
name|authMethod
operator|=
name|authMethod
expr_stmt|;
block|}
specifier|public
name|String
name|getAuthMethod
parameter_list|()
block|{
return|return
name|authMethod
return|;
block|}
specifier|public
specifier|static
name|AuthMethods
name|getValidAuthMethod
parameter_list|(
name|String
name|authMethodStr
parameter_list|)
throws|throws
name|AuthenticationException
block|{
for|for
control|(
name|AuthMethods
name|auth
range|:
name|AuthMethods
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|authMethodStr
operator|.
name|equals
argument_list|(
name|auth
operator|.
name|getAuthMethod
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|auth
return|;
block|}
block|}
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Not a valid authentication method"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|MetaStoreAuthenticationProviderFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|MetaStorePasswdAuthenticationProvider
name|getAuthenticationProvider
parameter_list|(
name|AuthMethods
name|authMethod
parameter_list|)
throws|throws
name|AuthenticationException
block|{
return|return
name|getAuthenticationProvider
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|authMethod
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|MetaStorePasswdAuthenticationProvider
name|getAuthenticationProvider
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|AuthMethods
name|authMethod
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
name|authMethod
operator|==
name|AuthMethods
operator|.
name|LDAP
condition|)
block|{
return|return
operator|new
name|MetaStoreLdapAuthenticationProviderImpl
argument_list|(
name|conf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|authMethod
operator|==
name|AuthMethods
operator|.
name|CUSTOM
condition|)
block|{
return|return
operator|new
name|MetaStoreCustomAuthenticationProviderImpl
argument_list|(
name|conf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|authMethod
operator|==
name|AuthMethods
operator|.
name|CONFIG
condition|)
block|{
return|return
operator|new
name|MetaStoreConfigAuthenticationProviderImpl
argument_list|(
name|conf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|authMethod
operator|==
name|AuthMethods
operator|.
name|NONE
condition|)
block|{
return|return
operator|new
name|MetaStoreAnonymousAuthenticationProviderImpl
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Unsupported authentication method"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

