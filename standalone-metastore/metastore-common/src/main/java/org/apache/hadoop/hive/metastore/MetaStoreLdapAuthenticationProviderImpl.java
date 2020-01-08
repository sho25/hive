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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|naming
operator|.
name|NamingException
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
name|annotations
operator|.
name|VisibleForTesting
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
name|ImmutableList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|ldap
operator|.
name|ChainFilterFactory
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
name|ldap
operator|.
name|CustomQueryFilterFactory
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
name|ldap
operator|.
name|LdapSearchFactory
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
name|ldap
operator|.
name|Filter
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
name|ldap
operator|.
name|DirSearch
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
name|ldap
operator|.
name|DirSearchFactory
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
name|ldap
operator|.
name|FilterFactory
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
name|ldap
operator|.
name|GroupFilterFactory
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
name|ldap
operator|.
name|LdapUtils
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
name|ldap
operator|.
name|UserFilterFactory
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
name|ldap
operator|.
name|UserSearchFilterFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|// This file is copy of LdapAuthenticationProviderImpl from hive.auth. We should try to
end_comment

begin_comment
comment|//  deduplicate the code.
end_comment

begin_class
specifier|public
class|class
name|MetaStoreLdapAuthenticationProviderImpl
implements|implements
name|MetaStorePasswdAuthenticationProvider
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MetaStoreLdapAuthenticationProviderImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|FilterFactory
argument_list|>
name|FILTER_FACTORIES
init|=
name|ImmutableList
operator|.
expr|<
name|FilterFactory
operator|>
name|of
argument_list|(
operator|new
name|CustomQueryFilterFactory
argument_list|()
argument_list|,
operator|new
name|ChainFilterFactory
argument_list|(
operator|new
name|UserSearchFilterFactory
argument_list|()
argument_list|,
operator|new
name|UserFilterFactory
argument_list|()
argument_list|,
operator|new
name|GroupFilterFactory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
specifier|private
specifier|final
name|DirSearchFactory
name|searchFactory
decl_stmt|;
specifier|public
name|MetaStoreLdapAuthenticationProviderImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
operator|new
name|LdapSearchFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|MetaStoreLdapAuthenticationProviderImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|DirSearchFactory
name|searchFactory
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|searchFactory
operator|=
name|searchFactory
expr_stmt|;
name|filter
operator|=
name|resolveFilter
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|Authenticate
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|AuthenticationException
block|{
name|DirSearch
name|search
init|=
literal|null
decl_stmt|;
name|String
name|bindUser
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_BIND_USER
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|bindUser
argument_list|)
condition|)
block|{
name|bindUser
operator|=
literal|null
expr_stmt|;
block|}
name|String
name|bindPassword
decl_stmt|;
try|try
block|{
name|bindPassword
operator|=
name|MetastoreConf
operator|.
name|getPassword
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_BIND_PASSWORD
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|bindPassword
argument_list|)
condition|)
block|{
name|bindPassword
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|bindPassword
operator|=
literal|null
expr_stmt|;
block|}
name|boolean
name|usedBind
init|=
name|bindUser
operator|!=
literal|null
operator|&&
name|bindPassword
operator|!=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|usedBind
condition|)
block|{
comment|// If no bind user or bind password was specified,
comment|// we assume the user we are authenticating has the ability to search
comment|// the LDAP tree, so we use it as the "binding" account.
comment|// This is the way it worked before bind users were allowed in the LDAP authenticator,
comment|// so we keep existing systems working.
name|bindUser
operator|=
name|user
expr_stmt|;
name|bindPassword
operator|=
name|password
expr_stmt|;
block|}
try|try
block|{
name|search
operator|=
name|createDirSearch
argument_list|(
name|bindUser
argument_list|,
name|bindPassword
argument_list|)
expr_stmt|;
name|applyFilter
argument_list|(
name|search
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|usedBind
condition|)
block|{
comment|// If we used the bind user, then we need to authenticate again,
comment|// this time using the full user name we got during the bind process.
name|createDirSearch
argument_list|(
name|search
operator|.
name|findUserDn
argument_list|(
name|user
argument_list|)
argument_list|,
name|password
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NamingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Unable to find the user in the LDAP tree. "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
name|ServiceUtils
operator|.
name|cleanup
argument_list|(
name|LOG
argument_list|,
name|search
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|DirSearch
name|createDirSearch
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|user
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Error validating LDAP user:"
operator|+
literal|" a null or blank user name has been provided"
argument_list|)
throw|;
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|password
argument_list|)
operator|||
name|password
operator|.
name|getBytes
argument_list|()
index|[
literal|0
index|]
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Error validating LDAP user:"
operator|+
literal|" a null or blank password has been provided"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|principals
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
name|user
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|iterator
init|=
name|principals
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|String
name|principal
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|searchFactory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|,
name|principal
argument_list|,
name|password
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AuthenticationException
name|ex
parameter_list|)
block|{
if|if
condition|(
operator|!
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
name|ex
throw|;
block|}
block|}
block|}
throw|throw
operator|new
name|AuthenticationException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"No candidate principals for %s was found."
argument_list|,
name|user
argument_list|)
argument_list|)
throw|;
block|}
specifier|private
specifier|static
name|Filter
name|resolveFilter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
for|for
control|(
name|FilterFactory
name|filterProvider
range|:
name|FILTER_FACTORIES
control|)
block|{
name|Filter
name|filter
init|=
name|filterProvider
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
return|return
name|filter
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|applyFilter
parameter_list|(
name|DirSearch
name|client
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LdapUtils
operator|.
name|hasDomain
argument_list|(
name|user
argument_list|)
condition|)
block|{
name|filter
operator|.
name|apply
argument_list|(
name|client
argument_list|,
name|LdapUtils
operator|.
name|extractUserName
argument_list|(
name|user
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filter
operator|.
name|apply
argument_list|(
name|client
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

