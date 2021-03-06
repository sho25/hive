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
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|fallback
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
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
name|parse
operator|.
name|SemanticException
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
name|security
operator|.
name|HiveAuthenticationProvider
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|AbstractHiveAuthorizer
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|DisallowTransformHook
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAccessControlException
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzContext
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzPluginException
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzSessionContext
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveOperationType
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrincipal
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilege
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilegeInfo
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilegeObject
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveRoleGrant
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|SettableConfigUpdater
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|Operation2Privilege
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|SQLAuthorizationUtils
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|SQLPrivTypeGrant
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
name|Arrays
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

begin_class
specifier|public
class|class
name|FallbackHiveAuthorizer
extends|extends
name|AbstractHiveAuthorizer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FallbackHiveAuthorizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HiveAuthzSessionContext
name|sessionCtx
decl_stmt|;
specifier|private
specifier|final
name|HiveAuthenticationProvider
name|authenticator
decl_stmt|;
specifier|private
name|String
index|[]
name|admins
init|=
literal|null
decl_stmt|;
specifier|public
name|FallbackHiveAuthorizer
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|HiveAuthenticationProvider
name|hiveAuthenticator
parameter_list|,
name|HiveAuthzSessionContext
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|authenticator
operator|=
name|hiveAuthenticator
expr_stmt|;
name|this
operator|.
name|sessionCtx
operator|=
name|applyTestSettings
argument_list|(
name|ctx
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|String
name|adminString
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|USERS_IN_ADMIN_ROLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|adminString
operator|!=
literal|null
condition|)
block|{
name|admins
operator|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|USERS_IN_ADMIN_ROLE
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Change the session context based on configuration to aid in testing of sql    * std auth    *    * @param ctx    * @param conf    * @return    */
specifier|static
name|HiveAuthzSessionContext
name|applyTestSettings
parameter_list|(
name|HiveAuthzSessionContext
name|ctx
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TEST_AUTHORIZATION_SQLSTD_HS2_MODE
argument_list|)
operator|&&
name|ctx
operator|.
name|getClientType
argument_list|()
operator|==
name|HiveAuthzSessionContext
operator|.
name|CLIENT_TYPE
operator|.
name|HIVECLI
condition|)
block|{
comment|// create new session ctx object with HS2 as client type
name|HiveAuthzSessionContext
operator|.
name|Builder
name|ctxBuilder
init|=
operator|new
name|HiveAuthzSessionContext
operator|.
name|Builder
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|ctxBuilder
operator|.
name|setClientType
argument_list|(
name|HiveAuthzSessionContext
operator|.
name|CLIENT_TYPE
operator|.
name|HIVESERVER2
argument_list|)
expr_stmt|;
return|return
name|ctxBuilder
operator|.
name|build
argument_list|()
return|;
block|}
return|return
name|ctx
return|;
block|}
annotation|@
name|Override
specifier|public
name|VERSION
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
operator|.
name|V1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|grantPrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|hivePrincipals
parameter_list|,
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivileges
parameter_list|,
name|HivePrivilegeObject
name|hivePrivObject
parameter_list|,
name|HivePrincipal
name|grantorPrincipal
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"grantPrivileges not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|revokePrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|hivePrincipals
parameter_list|,
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivileges
parameter_list|,
name|HivePrivilegeObject
name|hivePrivObject
parameter_list|,
name|HivePrincipal
name|grantorPrincipal
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"revokePrivileges not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createRole
parameter_list|(
name|String
name|roleName
parameter_list|,
name|HivePrincipal
name|adminGrantor
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"createRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dropRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"dropRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRoleGrant
argument_list|>
name|getPrincipalGrantInfoForRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"getPrincipalGrantInfoForRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRoleGrant
argument_list|>
name|getRoleGrantInfoForPrincipal
parameter_list|(
name|HivePrincipal
name|principal
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"getRoleGrantInfoForPrincipal not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|grantRole
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|hivePrincipals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|HivePrincipal
name|grantorPrinc
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"grantRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|revokeRole
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|hivePrincipals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|HivePrincipal
name|grantorPrinc
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"revokeRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPrivileges
parameter_list|(
name|HiveOperationType
name|hiveOpType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|String
name|userName
init|=
name|authenticator
operator|.
name|getUserName
argument_list|()
decl_stmt|;
comment|// check privileges on input and output objects
name|List
argument_list|<
name|String
argument_list|>
name|deniedMessages
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|checkPrivileges
argument_list|(
name|hiveOpType
argument_list|,
name|inputHObjs
argument_list|,
name|userName
argument_list|,
name|Operation2Privilege
operator|.
name|IOType
operator|.
name|INPUT
argument_list|,
name|deniedMessages
argument_list|)
expr_stmt|;
name|checkPrivileges
argument_list|(
name|hiveOpType
argument_list|,
name|outputHObjs
argument_list|,
name|userName
argument_list|,
name|Operation2Privilege
operator|.
name|IOType
operator|.
name|OUTPUT
argument_list|,
name|deniedMessages
argument_list|)
expr_stmt|;
name|SQLAuthorizationUtils
operator|.
name|assertNoDeniedPermissions
argument_list|(
operator|new
name|HivePrincipal
argument_list|(
name|userName
argument_list|,
name|HivePrincipal
operator|.
name|HivePrincipalType
operator|.
name|USER
argument_list|)
argument_list|,
name|hiveOpType
argument_list|,
name|deniedMessages
argument_list|)
expr_stmt|;
block|}
comment|// Adapted from SQLStdHiveAuthorizationValidator, only check privileges for LOAD/ADD/DFS/COMPILE and admin privileges
specifier|private
name|void
name|checkPrivileges
parameter_list|(
name|HiveOperationType
name|hiveOpType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|hiveObjects
parameter_list|,
name|String
name|userName
parameter_list|,
name|Operation2Privilege
operator|.
name|IOType
name|ioType
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|deniedMessages
parameter_list|)
block|{
if|if
condition|(
name|hiveObjects
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|admins
operator|!=
literal|null
operator|&&
name|Arrays
operator|.
name|stream
argument_list|(
name|admins
argument_list|)
operator|.
name|parallel
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|n
lambda|->
name|n
operator|.
name|equals
argument_list|(
name|userName
argument_list|)
argument_list|)
condition|)
block|{
return|return;
comment|// Skip rest of checks if user is admin
block|}
comment|// Special-casing for ADMIN-level operations that do not require object checking.
if|if
condition|(
name|Operation2Privilege
operator|.
name|isAdminPrivOperation
argument_list|(
name|hiveOpType
argument_list|)
condition|)
block|{
comment|// Require ADMIN privilege
name|deniedMessages
operator|.
name|add
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|ADMIN_PRIV
operator|.
name|toString
argument_list|()
operator|+
literal|" on "
operator|+
name|ioType
argument_list|)
expr_stmt|;
return|return;
comment|// Ignore object, fail if not admin, succeed if admin.
block|}
name|boolean
name|needAdmin
init|=
literal|false
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|hiveObj
range|:
name|hiveObjects
control|)
block|{
comment|// If involving local file system
if|if
condition|(
name|hiveObj
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
operator|.
name|LOCAL_URI
condition|)
block|{
name|needAdmin
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|needAdmin
condition|)
block|{
switch|switch
condition|(
name|hiveOpType
condition|)
block|{
case|case
name|ADD
case|:
case|case
name|DFS
case|:
case|case
name|COMPILE
case|:
name|needAdmin
operator|=
literal|true
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
if|if
condition|(
name|needAdmin
condition|)
block|{
name|deniedMessages
operator|.
name|add
argument_list|(
literal|"ADMIN"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filterListCmdObjects
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
block|{
return|return
name|listObjs
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllRoles
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"getAllRoles not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeInfo
argument_list|>
name|showPrivileges
parameter_list|(
name|HivePrincipal
name|principal
parameter_list|,
name|HivePrivilegeObject
name|privObj
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"showPrivileges not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCurrentRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"setCurrentRole not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCurrentRoleNames
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"getCurrentRoleNames not implemented in FallbackHiveAuthorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|applyAuthorizationConfigPolicy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
comment|// from SQLStdHiveAccessController.applyAuthorizationConfigPolicy()
if|if
condition|(
name|sessionCtx
operator|.
name|getClientType
argument_list|()
operator|==
name|HiveAuthzSessionContext
operator|.
name|CLIENT_TYPE
operator|.
name|HIVESERVER2
operator|&&
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|)
condition|)
block|{
comment|// Configure PREEXECHOOKS with DisallowTransformHook to disallow transform queries
name|String
name|hooks
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|hooks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|hooks
operator|=
name|DisallowTransformHook
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|hooks
operator|=
name|hooks
operator|+
literal|","
operator|+
name|DisallowTransformHook
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Configuring hooks : "
operator|+
name|hooks
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|,
name|hooks
argument_list|)
expr_stmt|;
name|SettableConfigUpdater
operator|.
name|setHiveConfWhiteList
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|String
name|curBlackList
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_BUILTIN_UDF_BLACKLIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|curBlackList
operator|!=
literal|null
operator|&&
name|curBlackList
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|"reflect,reflect2,java_method"
argument_list|)
condition|)
block|{
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_BUILTIN_UDF_BLACKLIST
argument_list|,
literal|"reflect,reflect2,java_method,in_file"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|applyRowFilterAndColumnMasking
parameter_list|(
name|HiveAuthzContext
name|context
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|privObjs
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needTransform
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

