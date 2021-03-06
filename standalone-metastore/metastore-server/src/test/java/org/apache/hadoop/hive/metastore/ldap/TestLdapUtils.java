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
name|metastore
operator|.
name|ldap
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|MetastoreConf
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestLdapUtils
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCreateCandidatePrincipalsForUserDn
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|String
name|userDn
init|=
literal|"cn=user1,ou=CORP,dc=mycompany,dc=com"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|userDn
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
name|userDn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateCandidatePrincipalsForUserWithDomain
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|String
name|userWithDomain
init|=
literal|"user1@mycompany.com"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|userWithDomain
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
name|userWithDomain
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateCandidatePrincipalsLdapDomain
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_DOMAIN
argument_list|,
literal|"mycompany.com"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"user1@mycompany.com"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateCandidatePrincipalsUserPatternsDefaultBaseDn
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_GUIDKEY
argument_list|,
literal|"sAMAccountName"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_BASEDN
argument_list|,
literal|"dc=mycompany,"
operator|+
literal|"dc=com"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"sAMAccountName=user1,dc=mycompany,dc=com"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateCandidatePrincipals
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_BASEDN
argument_list|,
literal|"dc=mycompany,"
operator|+
literal|"dc=com"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_USERDNPATTERN
argument_list|,
literal|"cn=%s,ou=CORP1,dc=mycompany,dc=com:cn=%s,ou=CORP2,dc=mycompany,dc=com"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"cn=user1,ou=CORP1,dc=mycompany,dc=com"
argument_list|,
literal|"cn=user1,ou=CORP2,dc=mycompany,dc=com"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|LdapUtils
operator|.
name|createCandidatePrincipals
argument_list|(
name|conf
argument_list|,
literal|"user1"
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|actual
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractFirstRdn
parameter_list|()
block|{
name|String
name|dn
init|=
literal|"cn=user1,ou=CORP1,dc=mycompany,dc=com"
decl_stmt|;
name|String
name|expected
init|=
literal|"cn=user1"
decl_stmt|;
name|String
name|actual
init|=
name|LdapUtils
operator|.
name|extractFirstRdn
argument_list|(
name|dn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractBaseDn
parameter_list|()
block|{
name|String
name|dn
init|=
literal|"cn=user1,ou=CORP1,dc=mycompany,dc=com"
decl_stmt|;
name|String
name|expected
init|=
literal|"ou=CORP1,dc=mycompany,dc=com"
decl_stmt|;
name|String
name|actual
init|=
name|LdapUtils
operator|.
name|extractBaseDn
argument_list|(
name|dn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractBaseDnNegative
parameter_list|()
block|{
name|String
name|dn
init|=
literal|"cn=user1"
decl_stmt|;
name|assertNull
argument_list|(
name|LdapUtils
operator|.
name|extractBaseDn
argument_list|(
name|dn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

