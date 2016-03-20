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
name|HiveAuthorizationProvider
import|;
end_import

begin_comment
comment|/**  * Interface for hive authorization plugins. Plugins will be better shielded from changes  * to this interface by extending AbstractHiveAuthorizer instead of extending this  * interface directly.  *  * Note that this interface is for limited use by specific apache projects, including  * Apache Ranger (formerly known as Argus), and Apache Sentry, and is subject to  * change across releases.  *  * Used by the DDLTasks for access control statement,  * and for checking authorization from Driver.doAuthorization()  *  * This a more generic version of  *  {@link HiveAuthorizationProvider} that lets you define the behavior of access control  *  statements and does not make assumptions about the privileges needed for a hive operation.  * This is referred to as V2 authorizer in other parts of the code.  */
end_comment

begin_interface
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Argus (incubating)"
block|}
argument_list|)
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
comment|/**    * Grant privileges for principals on the object    * @param hivePrincipals    * @param hivePrivileges    * @param hivePrivObject    * @param grantorPrincipal    * @param grantOption    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Revoke privileges for principals on the object    * @param hivePrincipals    * @param hivePrivileges    * @param hivePrivObject    * @param grantorPrincipal    * @param grantOption    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Create role    * @param roleName    * @param adminGrantor - The user in "[ WITH ADMIN<user> ]" clause of "create role"    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Drop role    * @param roleName    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Get the grant information for principals granted the given role    * @param roleName    * @return    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
function_decl|;
comment|/**    * Get the grant information of roles the given principal belongs to    * @param principal    * @return    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
function_decl|;
comment|/**    * Grant roles in given roles list to principals in given hivePrincipals list    * @param hivePrincipals    * @param roles    * @param grantOption    * @param grantorPrinc    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Revoke roles in given roles list to principals in given hivePrincipals list    * @param hivePrincipals    * @param roles    * @param grantOption    * @param grantorPrinc    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Check if user has privileges to do this action on these objects    * @param hiveOpType    * @param inputsHObjs    * @param outputHObjs    * @param context    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
function_decl|;
comment|/**    * Filter out any objects that should not be shown to the user, from the list of    * tables or databases coming from a 'show tables' or 'show databases' command    * @param listObjs List of all objects obtained as result of a show command    * @param context    * @return filtered list of objects that will be returned to the user invoking the command    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
function_decl|;
comment|/**    * @return all existing roles    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Show privileges for given principal on given object    * @param principal    * @param privObj    * @return    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
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
comment|/**    * Set the current role to roleName argument    * @param roleName    * @throws HiveAccessControlException    * @throws HiveAuthzPluginException    */
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
function_decl|;
comment|/**    * @return List having names of current roles    * @throws HiveAuthzPluginException    */
name|List
argument_list|<
name|String
argument_list|>
name|getCurrentRoleNames
parameter_list|()
throws|throws
name|HiveAuthzPluginException
function_decl|;
comment|/**    * Modify the given HiveConf object to configure authorization related parameters    * or other parameters related to hive security    * @param hiveConf    * @throws HiveAuthzPluginException    */
name|void
name|applyAuthorizationConfigPolicy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveAuthzPluginException
function_decl|;
comment|/**    * Get a {@link HiveAuthorizationTranslator} implementation. See    * {@link HiveAuthorizationTranslator} for details. Return null if no    * customization is needed. Most implementations are expected to return null.    *    * The java signature of the method makes it necessary to only return Object    * type so that older implementations can extend the interface to build    * against older versions of Hive that don't include this additional method    * and HiveAuthorizationTranslator class. However, if a non null value is    * returned, the Object has to be of type HiveAuthorizationTranslator    *    * @return    * @throws HiveException    */
name|Object
name|getHiveAuthorizationTranslator
parameter_list|()
throws|throws
name|HiveAuthzPluginException
function_decl|;
comment|/**    * TableMaskingPolicy defines how users can access base tables. It defines a    * policy on what columns and rows are hidden, masked or redacted based on    * user, role or location.    */
comment|/**    * getRowFilterExpression is called once for each table in a query. It expects    * a valid filter condition to be returned. Null indicates no filtering is    * required.    *    * Example: table foo(c int) -> "c> 0&& c % 2 = 0"    *    * @param database    *          the name of the database in which the table lives    * @param table    *          the name of the table in question    * @return    * @throws SemanticException    */
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
function_decl|;
comment|/**    * needTransform() is called once per user in a query. If the function returns    * true a call to needTransform(String database, String table) will happen.    * Returning false short-circuits the generation of row/column transforms.    *    * @return    * @throws SemanticException    */
specifier|public
name|boolean
name|needTransform
parameter_list|()
function_decl|;
comment|/**    * needTransform(String database, String table) is called once per table in a    * query. If the function returns true a call to getRowFilterExpression and    * getCellValueTransformer will happen. Returning false short-circuits the    * generation of row/column transforms.    *    * @param database    *          the name of the database in which the table lives    * @param table    *          the name of the table in question    * @return    * @throws SemanticException    */
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
function_decl|;
comment|/**    * getCellValueTransformer is called once per column in each table accessed by    * the query. It expects a valid expression as used in a select clause. Null    * is not a valid option. If no transformation is needed simply return the    * column name.    *    * Example: column a -> "a" (no transform)    *    * Example: column a -> "reverse(a)" (call the reverse function on a)    *    * Example: column a -> "5" (replace column a with the constant 5)    *    * @param database    * @param table    * @param columnName    * @return    * @throws SemanticException    */
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
function_decl|;
block|}
end_interface

end_unit

