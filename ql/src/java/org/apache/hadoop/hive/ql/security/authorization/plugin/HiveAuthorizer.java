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
name|Public
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
import|;
end_import

begin_comment
comment|/**  * Interface for hive authorization plugins.  * Used by the DDLTasks for access control statement,  * and for checking authorization from Driver.doAuthorization()  *  * This a more generic version of  *  {@link HiveAuthorizationProvider} that lets you define the behavior of access control  *  statements and does not make assumptions about the privileges needed for a hive operation.  * This is referred to as V2 authorizer in other parts of the code.  */
end_comment

begin_interface
annotation|@
name|Public
annotation|@
name|Evolving
specifier|public
interface|interface
name|HiveAuthorizer
block|{
specifier|public
enum|enum
name|VERSION
block|{
name|V1
block|}
empty_stmt|;
comment|/**    * @return version of HiveAuthorizer interface that is implemented by this instance    */
specifier|public
name|VERSION
name|getVersion
parameter_list|()
function_decl|;
comment|/**    * Grant privileges for principals on the object    * @param hivePrincipals    * @param hivePrivileges    * @param hivePrivObject    * @param grantorPrincipal    * @param grantOption    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Revoke privileges for principals on the object    * @param hivePrincipals    * @param hivePrivileges    * @param hivePrivObject    * @param grantorPrincipal    * @param grantOption    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Create role    * @param roleName    * @param adminGrantor - The user in "[ WITH ADMIN<user> ]" clause of "create role"    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Drop role    * @param roleName    * @throws HiveAuthorizationPluginException    */
name|void
name|dropRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Get roles that this user/role belongs to    * @param hivePrincipal - user or role    * @return list of roles    * @throws HiveAuthorizationPluginException    */
name|List
argument_list|<
name|String
argument_list|>
name|getRoles
parameter_list|(
name|HivePrincipal
name|hivePrincipal
parameter_list|)
throws|throws
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Grant roles in given roles list to principals in given hivePrincipals list    * @param hivePrincipals    * @param roles    * @param grantOption    * @param grantorPrinc    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Revoke roles in given roles list to principals in given hivePrincipals list    * @param hivePrincipals    * @param roles    * @param grantOption    * @param grantorPrinc    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Check if user has privileges to do this action on these objects    * @param hiveOpType    * @param inputsHObjs    * @param outputHObjs    * @throws HiveAuthorizationPluginException    */
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
name|inputsHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|)
throws|throws
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * @return all existing roles    * @throws HiveAuthorizationPluginException    */
name|List
argument_list|<
name|String
argument_list|>
name|getAllRoles
parameter_list|()
throws|throws
name|HiveAuthorizationPluginException
function_decl|;
comment|/**    * Show privileges for given principal on given object    * @param principal    * @param privObj    * @return    * @throws HiveAuthorizationPluginException    */
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
name|HiveAuthorizationPluginException
function_decl|;
comment|//other functions to be added -
comment|//showUsersInRole(rolename)
comment|//isSuperuser(username)
block|}
end_interface

end_unit

