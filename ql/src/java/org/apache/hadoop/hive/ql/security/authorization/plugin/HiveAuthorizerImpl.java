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
name|hive
operator|.
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
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

begin_comment
comment|/**  * Convenience implementation of HiveAuthorizer.  * You can customize the behavior by passing different implementations of  * {@link HiveAccessController} and {@link HiveAuthorizationValidator} to constructor.  *  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|""
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
class|class
name|HiveAuthorizerImpl
extends|extends
name|AbstractHiveAuthorizer
block|{
name|HiveAccessController
name|accessController
decl_stmt|;
name|HiveAuthorizationValidator
name|authValidator
decl_stmt|;
specifier|public
name|HiveAuthorizerImpl
parameter_list|(
name|HiveAccessController
name|accessController
parameter_list|,
name|HiveAuthorizationValidator
name|authValidator
parameter_list|)
block|{
name|this
operator|.
name|accessController
operator|=
name|accessController
expr_stmt|;
name|this
operator|.
name|authValidator
operator|=
name|authValidator
expr_stmt|;
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
name|accessController
operator|.
name|grantPrivileges
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
expr_stmt|;
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
name|accessController
operator|.
name|revokePrivileges
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
expr_stmt|;
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
name|accessController
operator|.
name|createRole
argument_list|(
name|roleName
argument_list|,
name|adminGrantor
argument_list|)
expr_stmt|;
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
name|accessController
operator|.
name|dropRole
argument_list|(
name|roleName
argument_list|)
expr_stmt|;
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
name|accessController
operator|.
name|grantRole
argument_list|(
name|hivePrincipals
argument_list|,
name|roles
argument_list|,
name|grantOption
argument_list|,
name|grantorPrinc
argument_list|)
expr_stmt|;
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
name|accessController
operator|.
name|revokeRole
argument_list|(
name|hivePrincipals
argument_list|,
name|roles
argument_list|,
name|grantOption
argument_list|,
name|grantorPrinc
argument_list|)
expr_stmt|;
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
name|authValidator
operator|.
name|checkPrivileges
argument_list|(
name|hiveOpType
argument_list|,
name|inputHObjs
argument_list|,
name|outputHObjs
argument_list|,
name|context
argument_list|)
expr_stmt|;
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
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
return|return
name|authValidator
operator|.
name|filterListCmdObjects
argument_list|(
name|listObjs
argument_list|,
name|context
argument_list|)
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
throws|,
name|HiveAccessControlException
block|{
return|return
name|accessController
operator|.
name|getAllRoles
argument_list|()
return|;
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
throws|,
name|HiveAccessControlException
block|{
return|return
name|accessController
operator|.
name|showPrivileges
argument_list|(
name|principal
argument_list|,
name|privObj
argument_list|)
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
name|accessController
operator|.
name|setCurrentRole
argument_list|(
name|roleName
argument_list|)
expr_stmt|;
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
return|return
name|accessController
operator|.
name|getCurrentRoleNames
argument_list|()
return|;
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
return|return
name|accessController
operator|.
name|getPrincipalGrantInfoForRole
argument_list|(
name|roleName
argument_list|)
return|;
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
return|return
name|accessController
operator|.
name|getRoleGrantInfoForPrincipal
argument_list|(
name|principal
argument_list|)
return|;
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
name|accessController
operator|.
name|applyAuthorizationConfigPolicy
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRowFilterExpression
parameter_list|(
name|String
name|database
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|authValidator
operator|.
name|getRowFilterExpression
argument_list|(
name|table
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCellValueTransformer
parameter_list|(
name|String
name|database
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|authValidator
operator|.
name|getCellValueTransformer
argument_list|(
name|database
argument_list|,
name|table
argument_list|,
name|columnName
argument_list|)
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
name|authValidator
operator|.
name|needTransform
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needTransform
parameter_list|(
name|String
name|database
parameter_list|,
name|String
name|table
parameter_list|)
block|{
return|return
name|authValidator
operator|.
name|needTransform
argument_list|(
name|database
argument_list|,
name|table
argument_list|)
return|;
block|}
block|}
end_class

end_unit

