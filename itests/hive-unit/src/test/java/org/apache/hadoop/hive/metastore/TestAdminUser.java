begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/** * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
operator|.
name|HMSHandler
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|SQLStdHiveAuthorizerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestAdminUser
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testCreateAdminNAddUser
parameter_list|()
throws|throws
name|IOException
throws|,
name|Throwable
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|USERS_IN_ADMIN_ROLE
argument_list|,
literal|"adminuser"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
name|SQLStdHiveAuthorizerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|RawStore
name|rawStore
init|=
operator|new
name|HMSHandler
argument_list|(
literal|"testcreateroot"
argument_list|,
name|conf
argument_list|)
operator|.
name|getMS
argument_list|()
decl_stmt|;
name|Role
name|adminRole
init|=
name|rawStore
operator|.
name|getRole
argument_list|(
name|HiveMetaStore
operator|.
name|ADMIN
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|adminRole
operator|.
name|getOwnerName
argument_list|()
operator|.
name|equals
argument_list|(
name|HiveMetaStore
operator|.
name|ADMIN
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rawStore
operator|.
name|listPrincipalGlobalGrants
argument_list|(
name|HiveMetaStore
operator|.
name|ADMIN
argument_list|,
name|PrincipalType
operator|.
name|ROLE
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getGrantInfo
argument_list|()
operator|.
name|getPrivilege
argument_list|()
argument_list|,
literal|"All"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rawStore
operator|.
name|listRoles
argument_list|(
literal|"adminuser"
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRoleName
argument_list|()
argument_list|,
name|HiveMetaStore
operator|.
name|ADMIN
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

