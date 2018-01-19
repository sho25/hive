begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestACLConfigurationParser
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10_000L
argument_list|)
specifier|public
name|void
name|test
parameter_list|()
block|{
name|ACLConfigurationParser
name|aclConf
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_ALL_ACCESS"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_ALL_ACCESS"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"*"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_INVALID1"
argument_list|,
literal|"u1, u2, u3"
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_INVALID1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|" "
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_NONE"
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_NONE"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|" "
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_VALID1"
argument_list|,
literal|"user1,user2"
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_VALID1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"user1,user2"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_VALID2"
argument_list|,
literal|"user1,user2 group1,group2"
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_VALID2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|contains
argument_list|(
literal|"group1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|contains
argument_list|(
literal|"group2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"user1,user2 group1,group2"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"ACL_VALID3"
argument_list|,
literal|"user1 group1"
argument_list|)
expr_stmt|;
name|aclConf
operator|=
operator|new
name|ACLConfigurationParser
argument_list|(
name|conf
argument_list|,
literal|"ACL_VALID3"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|contains
argument_list|(
literal|"group1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"user1 group1"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|aclConf
operator|.
name|addAllowedUser
argument_list|(
literal|"user2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"user2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"user1,user2 group1"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|aclConf
operator|.
name|addAllowedGroup
argument_list|(
literal|"group2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|contains
argument_list|(
literal|"group1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|contains
argument_list|(
literal|"group2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"user1,user2 group1,group2"
argument_list|,
name|aclConf
operator|.
name|toAclString
argument_list|()
argument_list|)
expr_stmt|;
name|aclConf
operator|.
name|addAllowedUser
argument_list|(
literal|"*"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aclConf
operator|.
name|getAllowedGroups
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

