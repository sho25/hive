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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
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
name|HashSet
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
name|util
operator|.
name|Set
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
name|InterfaceAudience
operator|.
name|Private
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
name|metastore
operator|.
name|HiveMetaStore
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
name|IMetaStoreClient
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
name|api
operator|.
name|HiveObjectPrivilege
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
name|api
operator|.
name|HiveObjectRef
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
name|api
operator|.
name|HiveObjectType
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
name|api
operator|.
name|MetaException
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
name|api
operator|.
name|PrincipalType
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
name|api
operator|.
name|PrivilegeBag
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
name|api
operator|.
name|PrivilegeGrantInfo
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
name|api
operator|.
name|Role
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
name|AuthorizationUtils
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
name|HiveAccessController
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
name|HiveMetastoreClientFactory
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
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
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
name|HiveRole
import|;
end_import

begin_comment
comment|/**  * Implements functionality of access control statements for sql standard based  * authorization  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|SQLStdHiveAccessController
implements|implements
name|HiveAccessController
block|{
specifier|private
specifier|final
name|HiveMetastoreClientFactory
name|metastoreClientFactory
decl_stmt|;
specifier|private
specifier|final
name|HiveAuthenticationProvider
name|authenticator
decl_stmt|;
specifier|private
name|String
name|currentUserName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HiveRole
argument_list|>
name|currentRoles
decl_stmt|;
specifier|private
name|HiveRole
name|adminRole
decl_stmt|;
specifier|private
specifier|final
name|String
name|ADMIN_ONLY_MSG
init|=
literal|"User has to belong to ADMIN role and "
operator|+
literal|"have it as current role, for this action."
decl_stmt|;
name|SQLStdHiveAccessController
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|this
operator|.
name|metastoreClientFactory
operator|=
name|metastoreClientFactory
expr_stmt|;
name|this
operator|.
name|authenticator
operator|=
name|authenticator
expr_stmt|;
name|initUserRoles
argument_list|()
expr_stmt|;
block|}
comment|/**    * (Re-)initialize currentRoleNames if necessary.    * @throws HiveAuthzPluginException    */
specifier|private
name|void
name|initUserRoles
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
comment|//to aid in testing through .q files, authenticator is passed as argument to
comment|// the interface. this helps in being able to switch the user within a session.
comment|// so we need to check if the user has changed
name|String
name|newUserName
init|=
name|authenticator
operator|.
name|getUserName
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentUserName
operator|==
name|newUserName
condition|)
block|{
comment|//no need to (re-)initialize the currentUserName, currentRoles fields
return|return;
block|}
name|this
operator|.
name|currentUserName
operator|=
name|newUserName
expr_stmt|;
name|this
operator|.
name|currentRoles
operator|=
name|getRolesFromMS
argument_list|()
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|HiveRole
argument_list|>
name|getRolesFromMS
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
name|List
argument_list|<
name|Role
argument_list|>
name|roles
decl_stmt|;
try|try
block|{
name|roles
operator|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
operator|.
name|list_roles
argument_list|(
name|currentUserName
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveRole
argument_list|>
name|currentRoles
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveRole
argument_list|>
argument_list|(
name|roles
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Role
name|role
range|:
name|roles
control|)
block|{
if|if
condition|(
operator|!
name|HiveMetaStore
operator|.
name|ADMIN
operator|.
name|equalsIgnoreCase
argument_list|(
name|role
operator|.
name|getRoleName
argument_list|()
argument_list|)
condition|)
block|{
name|currentRoles
operator|.
name|add
argument_list|(
operator|new
name|HiveRole
argument_list|(
name|role
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|adminRole
operator|=
operator|new
name|HiveRole
argument_list|(
name|role
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|currentRoles
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Failed to retrieve roles for "
operator|+
name|currentUserName
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
throws|,
name|HiveAccessControlException
block|{
name|hivePrivileges
operator|=
name|expandAndValidatePrivileges
argument_list|(
name|hivePrivileges
argument_list|)
expr_stmt|;
name|IMetaStoreClient
name|metastoreClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
comment|// authorize the grant
name|GrantPrivAuthUtils
operator|.
name|authorize
argument_list|(
name|hivePrincipals
argument_list|,
name|hivePrivileges
argument_list|,
name|hivePrivObject
argument_list|,
name|grantOption
argument_list|,
name|metastoreClient
argument_list|,
name|authenticator
operator|.
name|getUserName
argument_list|()
argument_list|,
name|getCurrentRoles
argument_list|()
argument_list|,
name|isUserAdmin
argument_list|()
argument_list|)
expr_stmt|;
comment|// grant
name|PrivilegeBag
name|privBag
init|=
name|getThriftPrivilegesBag
argument_list|(
name|hivePrincipals
argument_list|,
name|hivePrivileges
argument_list|,
name|hivePrivObject
argument_list|,
name|grantorPrincipal
argument_list|,
name|grantOption
argument_list|)
decl_stmt|;
try|try
block|{
name|metastoreClient
operator|.
name|grant_privileges
argument_list|(
name|privBag
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error granting privileges: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|expandAndValidatePrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivileges
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
comment|// expand ALL privileges, if any
name|hivePrivileges
operator|=
name|expandAllPrivileges
argument_list|(
name|hivePrivileges
argument_list|)
expr_stmt|;
name|SQLAuthorizationUtils
operator|.
name|validatePrivileges
argument_list|(
name|hivePrivileges
argument_list|)
expr_stmt|;
return|return
name|hivePrivileges
return|;
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|expandAllPrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivileges
parameter_list|)
block|{
name|Set
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivSet
init|=
operator|new
name|HashSet
argument_list|<
name|HivePrivilege
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilege
name|hivePrivilege
range|:
name|hivePrivileges
control|)
block|{
if|if
condition|(
name|hivePrivilege
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"ALL"
argument_list|)
condition|)
block|{
comment|// expand to all supported privileges
for|for
control|(
name|SQLPrivilegeType
name|privType
range|:
name|SQLPrivilegeType
operator|.
name|values
argument_list|()
control|)
block|{
name|hivePrivSet
operator|.
name|add
argument_list|(
operator|new
name|HivePrivilege
argument_list|(
name|privType
operator|.
name|name
argument_list|()
argument_list|,
name|hivePrivilege
operator|.
name|getColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|hivePrivSet
operator|.
name|add
argument_list|(
name|hivePrivilege
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ArrayList
argument_list|<
name|HivePrivilege
argument_list|>
argument_list|(
name|hivePrivSet
argument_list|)
return|;
block|}
comment|/**    * Create thrift privileges bag    *    * @param hivePrincipals    * @param hivePrivileges    * @param hivePrivObject    * @param grantorPrincipal    * @param grantOption    * @return    * @throws HiveAuthzPluginException    */
specifier|private
name|PrivilegeBag
name|getThriftPrivilegesBag
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
name|HiveObjectRef
name|privObj
init|=
name|SQLAuthorizationUtils
operator|.
name|getThriftHiveObjectRef
argument_list|(
name|hivePrivObject
argument_list|)
decl_stmt|;
name|PrivilegeBag
name|privBag
init|=
operator|new
name|PrivilegeBag
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilege
name|privilege
range|:
name|hivePrivileges
control|)
block|{
if|if
condition|(
name|privilege
operator|.
name|getColumns
argument_list|()
operator|!=
literal|null
operator|&&
name|privilege
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Privileges on columns not supported currently"
operator|+
literal|" in sql standard authorization mode"
argument_list|)
throw|;
block|}
name|PrivilegeGrantInfo
name|grantInfo
init|=
name|getThriftPrivilegeGrantInfo
argument_list|(
name|privilege
argument_list|,
name|grantorPrincipal
argument_list|,
name|grantOption
argument_list|)
decl_stmt|;
for|for
control|(
name|HivePrincipal
name|principal
range|:
name|hivePrincipals
control|)
block|{
name|HiveObjectPrivilege
name|objPriv
init|=
operator|new
name|HiveObjectPrivilege
argument_list|(
name|privObj
argument_list|,
name|principal
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|grantInfo
argument_list|)
decl_stmt|;
name|privBag
operator|.
name|addToPrivileges
argument_list|(
name|objPriv
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|privBag
return|;
block|}
specifier|private
name|PrivilegeGrantInfo
name|getThriftPrivilegeGrantInfo
parameter_list|(
name|HivePrivilege
name|privilege
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
try|try
block|{
return|return
name|AuthorizationUtils
operator|.
name|getThriftPrivilegeGrantInfo
argument_list|(
name|privilege
argument_list|,
name|grantorPrincipal
argument_list|,
name|grantOption
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
throws|,
name|HiveAccessControlException
block|{
name|hivePrivileges
operator|=
name|expandAndValidatePrivileges
argument_list|(
name|hivePrivileges
argument_list|)
expr_stmt|;
name|IMetaStoreClient
name|metastoreClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
comment|// authorize the revoke, and get the set of privileges to be revoked
name|List
argument_list|<
name|HiveObjectPrivilege
argument_list|>
name|revokePrivs
init|=
name|RevokePrivAuthUtils
operator|.
name|authorizeAndGetRevokePrivileges
argument_list|(
name|hivePrincipals
argument_list|,
name|hivePrivileges
argument_list|,
name|hivePrivObject
argument_list|,
name|grantOption
argument_list|,
name|metastoreClient
argument_list|,
name|authenticator
operator|.
name|getUserName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// unfortunately, the metastore api revokes all privileges that match on
comment|// principal, privilege object type it does not filter on the grator
comment|// username.
comment|// So this will revoke privileges that are granted by other users.This is
comment|// not SQL compliant behavior. Need to change/add a metastore api
comment|// that has desired behavior.
name|metastoreClient
operator|.
name|revoke_privileges
argument_list|(
operator|new
name|PrivilegeBag
argument_list|(
name|revokePrivs
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error revoking privileges"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
throws|,
name|HiveAccessControlException
block|{
comment|// only user belonging to admin role can create new roles.
if|if
condition|(
operator|!
name|isUserAdmin
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
literal|"Current user : "
operator|+
name|currentUserName
operator|+
literal|" is not"
operator|+
literal|" allowed to add roles. "
operator|+
name|ADMIN_ONLY_MSG
argument_list|)
throw|;
block|}
try|try
block|{
name|String
name|grantorName
init|=
name|adminGrantor
operator|==
literal|null
condition|?
literal|null
else|:
name|adminGrantor
operator|.
name|getName
argument_list|()
decl_stmt|;
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
operator|.
name|create_role
argument_list|(
operator|new
name|Role
argument_list|(
name|roleName
argument_list|,
literal|0
argument_list|,
name|grantorName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error create role"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
comment|// only user belonging to admin role can drop existing role
if|if
condition|(
operator|!
name|isUserAdmin
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
literal|"Current user : "
operator|+
name|currentUserName
operator|+
literal|" is not"
operator|+
literal|" allowed to drop role. "
operator|+
name|ADMIN_ONLY_MSG
argument_list|)
throw|;
block|}
try|try
block|{
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
operator|.
name|drop_role
argument_list|(
name|roleName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error dropping role"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRole
argument_list|>
name|getRoles
parameter_list|(
name|HivePrincipal
name|hivePrincipal
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
try|try
block|{
name|List
argument_list|<
name|Role
argument_list|>
name|roles
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
operator|.
name|list_roles
argument_list|(
name|hivePrincipal
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|hivePrincipal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveRole
argument_list|>
name|hiveRoles
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveRole
argument_list|>
argument_list|(
name|roles
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Role
name|role
range|:
name|roles
control|)
block|{
name|hiveRoles
operator|.
name|add
argument_list|(
operator|new
name|HiveRole
argument_list|(
name|role
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|hiveRoles
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error listing roles for user "
operator|+
name|hivePrincipal
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|roleNames
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
if|if
condition|(
operator|!
name|isUserAdmin
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
literal|"Current user : "
operator|+
name|currentUserName
operator|+
literal|" is not"
operator|+
literal|" allowed to grant role. Currently "
operator|+
name|ADMIN_ONLY_MSG
argument_list|)
throw|;
block|}
for|for
control|(
name|HivePrincipal
name|hivePrincipal
range|:
name|hivePrincipals
control|)
block|{
for|for
control|(
name|String
name|roleName
range|:
name|roleNames
control|)
block|{
try|try
block|{
name|IMetaStoreClient
name|mClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
name|mClient
operator|.
name|grant_role
argument_list|(
name|roleName
argument_list|,
name|hivePrincipal
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|hivePrincipal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|grantorPrinc
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|grantorPrinc
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error granting roles for "
operator|+
name|hivePrincipal
operator|.
name|getName
argument_list|()
operator|+
literal|" to role "
operator|+
name|roleName
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
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
name|roleNames
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
if|if
condition|(
name|grantOption
condition|)
block|{
comment|// removing grant privileges only is not supported in metastore api
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Revoking only the admin privileges on "
operator|+
literal|"role is not currently supported"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isUserAdmin
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
literal|"Current user : "
operator|+
name|currentUserName
operator|+
literal|" is not"
operator|+
literal|" allowed to revoke role. "
operator|+
name|ADMIN_ONLY_MSG
argument_list|)
throw|;
block|}
for|for
control|(
name|HivePrincipal
name|hivePrincipal
range|:
name|hivePrincipals
control|)
block|{
for|for
control|(
name|String
name|roleName
range|:
name|roleNames
control|)
block|{
try|try
block|{
name|IMetaStoreClient
name|mClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
name|mClient
operator|.
name|revoke_role
argument_list|(
name|roleName
argument_list|,
name|hivePrincipal
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|hivePrincipal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error revoking roles for "
operator|+
name|hivePrincipal
operator|.
name|getName
argument_list|()
operator|+
literal|" to role "
operator|+
name|roleName
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
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
throws|,
name|HiveAccessControlException
block|{
comment|// only user belonging to admin role can list role
if|if
condition|(
operator|!
name|isUserAdmin
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
literal|"Current user : "
operator|+
name|currentUserName
operator|+
literal|" is not"
operator|+
literal|" allowed to list roles. "
operator|+
name|ADMIN_ONLY_MSG
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
operator|.
name|listRoleNames
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error listing all roles"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
try|try
block|{
name|IMetaStoreClient
name|mClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeInfo
argument_list|>
name|resPrivInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// get metastore/thrift privilege object using metastore api
name|List
argument_list|<
name|HiveObjectPrivilege
argument_list|>
name|msObjPrivs
init|=
name|mClient
operator|.
name|list_privileges
argument_list|(
name|principal
operator|.
name|getName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|SQLAuthorizationUtils
operator|.
name|getThriftHiveObjectRef
argument_list|(
name|privObj
argument_list|)
argument_list|)
decl_stmt|;
comment|// convert the metastore thrift objects to result objects
for|for
control|(
name|HiveObjectPrivilege
name|msObjPriv
range|:
name|msObjPrivs
control|)
block|{
comment|// result principal
name|HivePrincipal
name|resPrincipal
init|=
operator|new
name|HivePrincipal
argument_list|(
name|msObjPriv
operator|.
name|getPrincipalName
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getHivePrincipalType
argument_list|(
name|msObjPriv
operator|.
name|getPrincipalType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// result privilege
name|PrivilegeGrantInfo
name|msGrantInfo
init|=
name|msObjPriv
operator|.
name|getGrantInfo
argument_list|()
decl_stmt|;
name|HivePrivilege
name|resPrivilege
init|=
operator|new
name|HivePrivilege
argument_list|(
name|msGrantInfo
operator|.
name|getPrivilege
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// result object
name|HiveObjectRef
name|msObjRef
init|=
name|msObjPriv
operator|.
name|getHiveObject
argument_list|()
decl_stmt|;
name|HivePrivilegeObject
name|resPrivObj
init|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|getPluginObjType
argument_list|(
name|msObjRef
operator|.
name|getObjectType
argument_list|()
argument_list|)
argument_list|,
name|msObjRef
operator|.
name|getDbName
argument_list|()
argument_list|,
name|msObjRef
operator|.
name|getObjectName
argument_list|()
argument_list|)
decl_stmt|;
comment|// result grantor principal
name|HivePrincipal
name|grantorPrincipal
init|=
operator|new
name|HivePrincipal
argument_list|(
name|msGrantInfo
operator|.
name|getGrantor
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getHivePrincipalType
argument_list|(
name|msGrantInfo
operator|.
name|getGrantorType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|HivePrivilegeInfo
name|resPrivInfo
init|=
operator|new
name|HivePrivilegeInfo
argument_list|(
name|resPrincipal
argument_list|,
name|resPrivilege
argument_list|,
name|resPrivObj
argument_list|,
name|grantorPrincipal
argument_list|,
name|msGrantInfo
operator|.
name|isGrantOption
argument_list|()
argument_list|)
decl_stmt|;
name|resPrivInfos
operator|.
name|add
argument_list|(
name|resPrivInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|resPrivInfos
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Error showing privileges: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|HivePrivilegeObjectType
name|getPluginObjType
parameter_list|(
name|HiveObjectType
name|objectType
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
switch|switch
condition|(
name|objectType
condition|)
block|{
case|case
name|DATABASE
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|DATABASE
return|;
case|case
name|TABLE
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
return|;
case|case
name|COLUMN
case|:
case|case
name|GLOBAL
case|:
case|case
name|PARTITION
case|:
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Unsupported object type "
operator|+
name|objectType
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected object type "
operator|+
name|objectType
argument_list|)
throw|;
block|}
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
name|HiveAccessControlException
throws|,
name|HiveAuthzPluginException
block|{
if|if
condition|(
literal|"NONE"
operator|.
name|equalsIgnoreCase
argument_list|(
name|roleName
argument_list|)
condition|)
block|{
comment|// for set role NONE, reset roles to default roles.
name|currentRoles
operator|.
name|clear
argument_list|()
expr_stmt|;
name|currentRoles
operator|.
name|addAll
argument_list|(
name|getRolesFromMS
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|HiveRole
name|role
range|:
name|getRolesFromMS
argument_list|()
control|)
block|{
comment|// set to one of the roles user belongs to.
if|if
condition|(
name|role
operator|.
name|getRoleName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|roleName
argument_list|)
condition|)
block|{
name|currentRoles
operator|.
name|clear
argument_list|()
expr_stmt|;
name|currentRoles
operator|.
name|add
argument_list|(
name|role
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|// set to ADMIN role, if user belongs there.
if|if
condition|(
name|HiveMetaStore
operator|.
name|ADMIN
operator|.
name|equalsIgnoreCase
argument_list|(
name|roleName
argument_list|)
operator|&&
literal|null
operator|!=
name|this
operator|.
name|adminRole
condition|)
block|{
name|currentRoles
operator|.
name|clear
argument_list|()
expr_stmt|;
name|currentRoles
operator|.
name|add
argument_list|(
name|adminRole
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If we are here it means, user is requesting a role he doesn't belong to.
throw|throw
operator|new
name|HiveAccessControlException
argument_list|(
name|currentUserName
operator|+
literal|" doesn't belong to role "
operator|+
name|roleName
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRole
argument_list|>
name|getCurrentRoles
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
name|initUserRoles
argument_list|()
expr_stmt|;
return|return
name|currentRoles
return|;
block|}
comment|/**    * @return true only if current role of user is Admin    * @throws HiveAuthzPluginException    */
name|boolean
name|isUserAdmin
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
name|List
argument_list|<
name|HiveRole
argument_list|>
name|roles
decl_stmt|;
try|try
block|{
name|roles
operator|=
name|getCurrentRoles
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|HiveRole
name|role
range|:
name|roles
control|)
block|{
if|if
condition|(
name|role
operator|.
name|getRoleName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveMetaStore
operator|.
name|ADMIN
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

