begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  *  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|auth
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|annotations
operator|.
name|CreateLdapServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|annotations
operator|.
name|CreateTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|annotations
operator|.
name|ApplyLdifFiles
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|annotations
operator|.
name|ContextEntry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|annotations
operator|.
name|CreateDS
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|annotations
operator|.
name|CreateIndex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|annotations
operator|.
name|CreatePartition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|integ
operator|.
name|AbstractLdapTestUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|directory
operator|.
name|server
operator|.
name|core
operator|.
name|integ
operator|.
name|FrameworkRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|ldap
operator|.
name|LdapAuthenticationTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|ldap
operator|.
name|User
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
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

begin_comment
comment|/**  * TestSuite to test Hive's LDAP Authentication provider with an  * in-process LDAP Server (Apache Directory Server instance).  *  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|FrameworkRunner
operator|.
name|class
argument_list|)
annotation|@
name|CreateLdapServer
argument_list|(
name|transports
operator|=
block|{
annotation|@
name|CreateTransport
argument_list|(
name|protocol
operator|=
literal|"LDAP"
argument_list|)
block|,
annotation|@
name|CreateTransport
argument_list|(
name|protocol
operator|=
literal|"LDAPS"
argument_list|)
block|}
argument_list|)
annotation|@
name|CreateDS
argument_list|(
name|partitions
operator|=
block|{
annotation|@
name|CreatePartition
argument_list|(
name|name
operator|=
literal|"example"
argument_list|,
name|suffix
operator|=
literal|"dc=example,dc=com"
argument_list|,
name|contextEntry
operator|=
annotation|@
name|ContextEntry
argument_list|(
name|entryLdif
operator|=
literal|"dn: dc=example,dc=com\n"
operator|+
literal|"dc: example\n"
operator|+
literal|"objectClass: top\n"
operator|+
literal|"objectClass: domain\n\n"
argument_list|)
argument_list|,
name|indexes
operator|=
block|{
annotation|@
name|CreateIndex
argument_list|(
name|attribute
operator|=
literal|"objectClass"
argument_list|)
block|,
annotation|@
name|CreateIndex
argument_list|(
name|attribute
operator|=
literal|"cn"
argument_list|)
block|,
annotation|@
name|CreateIndex
argument_list|(
name|attribute
operator|=
literal|"uid"
argument_list|)
block|}
argument_list|)
block|}
argument_list|)
annotation|@
name|ApplyLdifFiles
argument_list|(
literal|"ldap/example.com.ldif"
argument_list|)
specifier|public
class|class
name|TestLdapAtnProviderWithMiniDS
extends|extends
name|AbstractLdapTestUnit
block|{
specifier|private
specifier|static
specifier|final
name|String
name|GROUP1_NAME
init|=
literal|"group1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP2_NAME
init|=
literal|"group2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP3_NAME
init|=
literal|"group3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP4_NAME
init|=
literal|"group4"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|User
name|USER1
init|=
name|User
operator|.
name|builder
argument_list|()
operator|.
name|id
argument_list|(
literal|"user1"
argument_list|)
operator|.
name|useIdForPassword
argument_list|()
operator|.
name|dn
argument_list|(
literal|"uid=user1,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|User
name|USER2
init|=
name|User
operator|.
name|builder
argument_list|()
operator|.
name|id
argument_list|(
literal|"user2"
argument_list|)
operator|.
name|useIdForPassword
argument_list|()
operator|.
name|dn
argument_list|(
literal|"uid=user2,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|User
name|USER3
init|=
name|User
operator|.
name|builder
argument_list|()
operator|.
name|id
argument_list|(
literal|"user3"
argument_list|)
operator|.
name|useIdForPassword
argument_list|()
operator|.
name|dn
argument_list|(
literal|"cn=user3,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|User
name|USER4
init|=
name|User
operator|.
name|builder
argument_list|()
operator|.
name|id
argument_list|(
literal|"user4"
argument_list|)
operator|.
name|useIdForPassword
argument_list|()
operator|.
name|dn
argument_list|(
literal|"cn=user4,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
name|LdapAuthenticationTestCase
name|testCase
decl_stmt|;
specifier|private
name|LdapAuthenticationTestCase
operator|.
name|Builder
name|defaultBuilder
parameter_list|()
block|{
return|return
name|LdapAuthenticationTestCase
operator|.
name|builder
argument_list|()
operator|.
name|ldapServer
argument_list|(
name|ldapServer
argument_list|)
return|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|ldapServer
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|ldapServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLDAPServer
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
name|ldapServer
operator|.
name|isStarted
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ldapServer
operator|.
name|getPort
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithShortname
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithShortnameOldConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindNegativeWithShortname
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindNegativeWithShortnameOldConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|getDn
argument_list|()
argument_list|,
name|USER2
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDN
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDNOldConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDNWrongOldConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=DummyPeople,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDNWrongConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=DummyPeople,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=DummyGroups,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDNBlankConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|" "
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|" "
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindPositiveWithDNBlankOldConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|""
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindNegativeWithDN
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|getDn
argument_list|()
argument_list|,
name|USER2
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserBindNegativeWithDNOldConfig
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|getDn
argument_list|()
argument_list|,
name|USER2
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFailsUsingWrongPassword
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserFilterPositive
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER1
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER2
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER1
operator|.
name|getId
argument_list|()
argument_list|,
name|USER2
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserFilterNegative
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER2
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER1
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER3
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGroupFilterPositive
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP1_NAME
argument_list|,
name|GROUP2_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP2_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGroupFilterNegative
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP2_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP1_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserAndGroupFilterPositive
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER1
operator|.
name|getId
argument_list|()
argument_list|,
name|USER2
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP1_NAME
argument_list|,
name|GROUP2_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUserAndGroupFilterNegative
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"uid=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|userFilters
argument_list|(
name|USER1
operator|.
name|getId
argument_list|()
argument_list|,
name|USER2
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP3_NAME
argument_list|,
name|GROUP3_NAME
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER3
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER3
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCustomQueryPositive
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|,
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(&(objectClass=person)(|(uid=%s)(uid=%s)))"
argument_list|,
name|USER1
operator|.
name|getId
argument_list|()
argument_list|,
name|USER4
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCustomQueryNegative
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(&(objectClass=person)(uid=%s))"
argument_list|,
name|USER1
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    Test to test the LDAP Atn to use a custom LDAP query that returns    a) A set of group DNs    b) A combination of group(s) DN and user DN    LDAP atn is expected to extract the members of the group using the attribute value for    "hive.server2.authentication.ldap.groupMembershipKey"    */
annotation|@
name|Test
specifier|public
name|void
name|testCustomQueryWithGroupsPositive
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"dc=example,dc=com"
argument_list|)
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|,
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(&(objectClass=groupOfNames)(|(cn=%s)(cn=%s)))"
argument_list|,
name|GROUP1_NAME
argument_list|,
name|GROUP2_NAME
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER2
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
comment|/* the following test uses a query that returns a group and a user entry.        the ldap atn should use the groupMembershipKey to identify the users for the returned group        and the authentication should succeed for the users of that group as well as the lone user4 in this case     */
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"dc=example,dc=com"
argument_list|)
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|,
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(|(&(objectClass=groupOfNames)(cn=%s))(&(objectClass=person)(sn=%s)))"
argument_list|,
name|GROUP1_NAME
argument_list|,
name|USER4
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER1
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"dc=example,dc=com"
argument_list|)
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|,
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupMembership
argument_list|(
literal|"uniqueMember"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(&(objectClass=groupOfUniqueNames)(cn=%s))"
argument_list|,
name|GROUP4_NAME
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCustomQueryWithGroupsNegative
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|baseDN
argument_list|(
literal|"dc=example,dc=com"
argument_list|)
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|,
literal|"uid=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|customQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"(&(objectClass=groupOfNames)(|(cn=%s)(cn=%s)))"
argument_list|,
name|GROUP1_NAME
argument_list|,
name|GROUP2_NAME
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER3
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticateFails
argument_list|(
name|USER3
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGroupFilterPositiveWithCustomGUID
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"cn=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP3_NAME
argument_list|)
operator|.
name|guidKey
argument_list|(
literal|"cn"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER3
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER3
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGroupFilterPositiveWithCustomAttributes
parameter_list|()
block|{
name|testCase
operator|=
name|defaultBuilder
argument_list|()
operator|.
name|userDNPatterns
argument_list|(
literal|"cn=%s,ou=People,dc=example,dc=com"
argument_list|)
operator|.
name|groupDNPatterns
argument_list|(
literal|"cn=%s,ou=Groups,dc=example,dc=com"
argument_list|)
operator|.
name|groupFilters
argument_list|(
name|GROUP4_NAME
argument_list|)
operator|.
name|guidKey
argument_list|(
literal|"cn"
argument_list|)
operator|.
name|groupMembership
argument_list|(
literal|"uniqueMember"
argument_list|)
operator|.
name|groupClassKey
argument_list|(
literal|"groupOfUniqueNames"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithId
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|.
name|assertAuthenticatePasses
argument_list|(
name|USER4
operator|.
name|credentialsWithDn
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

