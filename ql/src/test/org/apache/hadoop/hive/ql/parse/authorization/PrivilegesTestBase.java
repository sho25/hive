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
name|parse
operator|.
name|authorization
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|ql
operator|.
name|QueryState
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
name|Hive
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
name|plan
operator|.
name|DDLWork
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
name|plan
operator|.
name|GrantDesc
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
name|plan
operator|.
name|PrincipalDesc
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
name|plan
operator|.
name|PrivilegeDesc
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
name|PrivilegeType
import|;
end_import

begin_class
specifier|public
class|class
name|PrivilegesTestBase
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|DB
init|=
literal|"default"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"table1"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|TABLE_QNAME
init|=
name|DB
operator|+
literal|"."
operator|+
name|TABLE
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|USER
init|=
literal|"user1"
decl_stmt|;
specifier|public
specifier|static
name|void
name|grantUserTable
parameter_list|(
name|String
name|privStr
parameter_list|,
name|PrivilegeType
name|privType
parameter_list|,
name|QueryState
name|queryState
parameter_list|,
name|Hive
name|db
parameter_list|)
throws|throws
name|Exception
block|{
name|DDLWork
name|work
init|=
name|AuthorizationTestUtil
operator|.
name|analyze
argument_list|(
literal|"GRANT "
operator|+
name|privStr
operator|+
literal|" ON TABLE "
operator|+
name|TABLE
operator|+
literal|" TO USER "
operator|+
name|USER
argument_list|,
name|queryState
argument_list|,
name|db
argument_list|)
decl_stmt|;
name|GrantDesc
name|grantDesc
init|=
name|work
operator|.
name|getGrantDesc
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Grant should not be null"
argument_list|,
name|grantDesc
argument_list|)
expr_stmt|;
comment|//check privileges
for|for
control|(
name|PrivilegeDesc
name|privilege
range|:
name|ListSizeMatcher
operator|.
name|inList
argument_list|(
name|grantDesc
operator|.
name|getPrivileges
argument_list|()
argument_list|)
operator|.
name|ofSize
argument_list|(
literal|1
argument_list|)
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|privType
argument_list|,
name|privilege
operator|.
name|getPrivilege
argument_list|()
operator|.
name|getPriv
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//check other parts
for|for
control|(
name|PrincipalDesc
name|principal
range|:
name|ListSizeMatcher
operator|.
name|inList
argument_list|(
name|grantDesc
operator|.
name|getPrincipals
argument_list|()
argument_list|)
operator|.
name|ofSize
argument_list|(
literal|1
argument_list|)
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|PrincipalType
operator|.
name|USER
argument_list|,
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|USER
argument_list|,
name|principal
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expected table"
argument_list|,
name|grantDesc
operator|.
name|getPrivilegeSubjectDesc
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TABLE_QNAME
argument_list|,
name|grantDesc
operator|.
name|getPrivilegeSubjectDesc
argument_list|()
operator|.
name|getObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

