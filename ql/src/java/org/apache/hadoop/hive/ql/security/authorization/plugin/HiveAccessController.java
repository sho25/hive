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

begin_comment
comment|/**  * Interface that is invoked by access control commands, including grant/revoke role/privileges,  * create/drop roles, and commands to read the state of authorization rules.  * Methods here have corresponding methods in HiveAuthorizer, check method documentation there.  */
end_comment

begin_interface
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
interface|interface
name|HiveAccessController
block|{
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
function_decl|;
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
function_decl|;
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
function_decl|;
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
function_decl|;
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
throws|,
name|HiveAccessControlException
function_decl|;
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
function_decl|;
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
function_decl|;
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
function_decl|;
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
function_decl|;
name|void
name|setCurrentRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
function_decl|;
name|List
argument_list|<
name|HiveRole
argument_list|>
name|getCurrentRoles
parameter_list|()
throws|throws
name|HiveAuthzPluginException
function_decl|;
block|}
end_interface

end_unit

