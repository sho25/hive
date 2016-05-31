begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|impl
package|;
end_package

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
name|base
operator|.
name|Preconditions
import|;
end_import

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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|hive
operator|.
name|llap
operator|.
name|security
operator|.
name|LlapTokenIdentifier
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
name|TokenIdentifier
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

begin_class
specifier|public
specifier|final
class|class
name|LlapTokenChecker
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
name|LlapTokenChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|LlapTokenInfo
block|{
specifier|public
specifier|final
name|String
name|userName
decl_stmt|;
specifier|public
specifier|final
name|String
name|appId
decl_stmt|;
specifier|public
specifier|final
name|boolean
name|isSigningRequired
decl_stmt|;
specifier|public
name|LlapTokenInfo
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|,
name|boolean
name|isSigningRequired
parameter_list|)
block|{
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|appId
operator|=
name|appId
expr_stmt|;
name|this
operator|.
name|isSigningRequired
operator|=
name|isSigningRequired
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|LlapTokenInfo
name|NO_SECURITY
init|=
operator|new
name|LlapTokenInfo
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|LlapTokenInfo
name|getTokenInfo
parameter_list|(
name|String
name|clusterId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
return|return
name|NO_SECURITY
return|;
name|UserGroupInformation
name|current
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|String
name|kerberosName
init|=
name|current
operator|.
name|hasKerberosCredentials
argument_list|()
condition|?
name|current
operator|.
name|getShortUserName
argument_list|()
else|:
literal|null
decl_stmt|;
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
init|=
name|getLlapTokens
argument_list|(
name|current
argument_list|,
name|clusterId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|tokens
operator|==
literal|null
operator|||
name|tokens
operator|.
name|isEmpty
argument_list|()
operator|)
operator|&&
name|kerberosName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"No tokens or kerberos for "
operator|+
name|current
argument_list|)
throw|;
block|}
return|return
name|getTokenInfoInternal
argument_list|(
name|kerberosName
argument_list|,
name|tokens
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|getLlapTokens
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
init|=
literal|null
decl_stmt|;
for|for
control|(
name|TokenIdentifier
name|id
range|:
name|ugi
operator|.
name|getTokenIdentifiers
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|LlapTokenIdentifier
operator|.
name|KIND_NAME
operator|.
name|equals
argument_list|(
name|id
operator|.
name|getKind
argument_list|()
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Token {}"
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
name|LlapTokenIdentifier
name|llapId
init|=
operator|(
name|LlapTokenIdentifier
operator|)
name|id
decl_stmt|;
if|if
condition|(
operator|!
name|clusterId
operator|.
name|equals
argument_list|(
name|llapId
operator|.
name|getClusterId
argument_list|()
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|tokens
operator|==
literal|null
condition|)
block|{
name|tokens
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|tokens
operator|.
name|add
argument_list|(
operator|(
name|LlapTokenIdentifier
operator|)
name|id
argument_list|)
expr_stmt|;
block|}
return|return
name|tokens
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|LlapTokenInfo
name|getTokenInfoInternal
parameter_list|(
name|String
name|kerberosName
parameter_list|,
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
parameter_list|)
block|{
assert|assert
operator|(
name|tokens
operator|!=
literal|null
operator|&&
operator|!
name|tokens
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
name|kerberosName
operator|!=
literal|null
assert|;
if|if
condition|(
name|tokens
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|LlapTokenInfo
argument_list|(
name|kerberosName
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
return|;
block|}
name|String
name|userName
init|=
name|kerberosName
decl_stmt|,
name|appId
init|=
literal|null
decl_stmt|;
name|boolean
name|isSigningRequired
init|=
literal|false
decl_stmt|;
for|for
control|(
name|LlapTokenIdentifier
name|llapId
range|:
name|tokens
control|)
block|{
name|String
name|newUserName
init|=
name|llapId
operator|.
name|getRealUser
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|userName
operator|!=
literal|null
operator|&&
operator|!
name|userName
operator|.
name|equals
argument_list|(
name|newUserName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Ambiguous user name from credentials - "
operator|+
name|userName
operator|+
literal|" and "
operator|+
name|newUserName
operator|+
literal|" from "
operator|+
name|llapId
operator|+
operator|(
operator|(
name|kerberosName
operator|==
literal|null
operator|)
condition|?
operator|(
literal|"; has kerberos credentials for "
operator|+
name|kerberosName
operator|)
else|:
literal|""
operator|)
argument_list|)
throw|;
block|}
name|userName
operator|=
name|newUserName
expr_stmt|;
name|String
name|newAppId
init|=
name|llapId
operator|.
name|getAppId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|newAppId
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|appId
argument_list|)
operator|&&
operator|!
name|appId
operator|.
name|equals
argument_list|(
name|newAppId
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Ambiguous app ID from credentials - "
operator|+
name|appId
operator|+
literal|" and "
operator|+
name|newAppId
operator|+
literal|" from "
operator|+
name|llapId
argument_list|)
throw|;
block|}
name|appId
operator|=
name|newAppId
expr_stmt|;
block|}
name|isSigningRequired
operator|=
name|isSigningRequired
operator|||
name|llapId
operator|.
name|isSigningRequired
argument_list|()
expr_stmt|;
block|}
assert|assert
name|userName
operator|!=
literal|null
assert|;
return|return
operator|new
name|LlapTokenInfo
argument_list|(
name|userName
argument_list|,
name|appId
argument_list|,
name|isSigningRequired
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|checkPermissions
parameter_list|(
name|String
name|clusterId
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|,
name|Object
name|hint
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
return|return;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|current
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|String
name|kerberosName
init|=
name|current
operator|.
name|hasKerberosCredentials
argument_list|()
condition|?
name|current
operator|.
name|getShortUserName
argument_list|()
else|:
literal|null
decl_stmt|;
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
init|=
name|getLlapTokens
argument_list|(
name|current
argument_list|,
name|clusterId
argument_list|)
decl_stmt|;
name|checkPermissionsInternal
argument_list|(
name|kerberosName
argument_list|,
name|tokens
argument_list|,
name|userName
argument_list|,
name|appId
argument_list|,
name|hint
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|void
name|checkPermissionsInternal
parameter_list|(
name|String
name|kerberosName
parameter_list|,
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|,
name|Object
name|hint
parameter_list|)
block|{
if|if
condition|(
name|kerberosName
operator|!=
literal|null
operator|&&
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|appId
argument_list|)
operator|&&
name|kerberosName
operator|.
name|equals
argument_list|(
name|userName
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|tokens
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|LlapTokenIdentifier
name|llapId
range|:
name|tokens
control|)
block|{
name|String
name|tokenUser
init|=
name|llapId
operator|.
name|getRealUser
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|,
name|tokenAppId
init|=
name|llapId
operator|.
name|getAppId
argument_list|()
decl_stmt|;
if|if
condition|(
name|checkTokenPermissions
argument_list|(
name|userName
argument_list|,
name|appId
argument_list|,
name|tokenUser
argument_list|,
name|tokenAppId
argument_list|)
condition|)
return|return;
block|}
block|}
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Unauthorized to access "
operator|+
name|userName
operator|+
literal|", "
operator|+
name|appId
operator|.
name|hashCode
argument_list|()
operator|+
literal|" ("
operator|+
name|hint
operator|+
literal|")"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|void
name|checkPermissions
parameter_list|(
name|LlapTokenInfo
name|prm
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|,
name|Object
name|hint
parameter_list|)
block|{
if|if
condition|(
name|userName
operator|==
literal|null
condition|)
block|{
assert|assert
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|appId
argument_list|)
assert|;
return|return;
block|}
if|if
condition|(
operator|!
name|checkTokenPermissions
argument_list|(
name|userName
argument_list|,
name|appId
argument_list|,
name|prm
operator|.
name|userName
argument_list|,
name|prm
operator|.
name|appId
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Unauthorized to access "
operator|+
name|userName
operator|+
literal|", "
operator|+
name|appId
operator|.
name|hashCode
argument_list|()
operator|+
literal|" ("
operator|+
name|hint
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|checkTokenPermissions
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|,
name|String
name|tokenUser
parameter_list|,
name|String
name|tokenAppId
parameter_list|)
block|{
return|return
name|userName
operator|.
name|equals
argument_list|(
name|tokenUser
argument_list|)
operator|&&
operator|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|appId
argument_list|)
operator|||
name|appId
operator|.
name|equals
argument_list|(
name|tokenAppId
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

