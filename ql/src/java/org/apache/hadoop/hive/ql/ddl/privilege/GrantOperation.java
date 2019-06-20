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
name|ddl
operator|.
name|privilege
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
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|DDLOperation
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
name|HiveAuthorizer
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
name|HivePrivilegeObject
import|;
end_import

begin_comment
comment|/**  * Operation process of granting.  */
end_comment

begin_class
specifier|public
class|class
name|GrantOperation
extends|extends
name|DDLOperation
argument_list|<
name|GrantDesc
argument_list|>
block|{
specifier|public
name|GrantOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|GrantDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|HiveAuthorizer
name|authorizer
init|=
name|PrivilegeUtils
operator|.
name|getSessionAuthorizer
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|//Convert to object types used by the authorization plugin interface
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|hivePrincipals
init|=
name|AuthorizationUtils
operator|.
name|getHivePrincipals
argument_list|(
name|desc
operator|.
name|getPrincipals
argument_list|()
argument_list|,
name|PrivilegeUtils
operator|.
name|getAuthorizationTranslator
argument_list|(
name|authorizer
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|hivePrivileges
init|=
name|AuthorizationUtils
operator|.
name|getHivePrivileges
argument_list|(
name|desc
operator|.
name|getPrivileges
argument_list|()
argument_list|,
name|PrivilegeUtils
operator|.
name|getAuthorizationTranslator
argument_list|(
name|authorizer
argument_list|)
argument_list|)
decl_stmt|;
name|HivePrivilegeObject
name|hivePrivilegeObject
init|=
name|PrivilegeUtils
operator|.
name|getAuthorizationTranslator
argument_list|(
name|authorizer
argument_list|)
operator|.
name|getHivePrivilegeObject
argument_list|(
name|desc
operator|.
name|getPrivilegeSubject
argument_list|()
argument_list|)
decl_stmt|;
name|HivePrincipal
name|grantorPrincipal
init|=
operator|new
name|HivePrincipal
argument_list|(
name|desc
operator|.
name|getGrantor
argument_list|()
argument_list|,
name|AuthorizationUtils
operator|.
name|getHivePrincipalType
argument_list|(
name|desc
operator|.
name|getGrantorType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|authorizer
operator|.
name|grantPrivileges
argument_list|(
name|hivePrincipals
argument_list|,
name|hivePrivileges
argument_list|,
name|hivePrivilegeObject
argument_list|,
name|grantorPrincipal
argument_list|,
name|desc
operator|.
name|isGrantOption
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

