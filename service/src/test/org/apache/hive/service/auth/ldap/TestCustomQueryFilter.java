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
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|ldap
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|naming
operator|.
name|NamingException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
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
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCustomQueryFilter
block|{
specifier|private
specifier|static
specifier|final
name|String
name|USER2_DN
init|=
literal|"uid=user2,ou=People,dc=example,dc=com"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USER1_DN
init|=
literal|"uid=user1,ou=People,dc=example,dc=com"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CUSTOM_QUERY
init|=
literal|"(&(objectClass=person)(|(uid=user1)(uid=user2)))"
decl_stmt|;
specifier|private
name|FilterFactory
name|factory
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|DirSearch
name|search
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.root.logger"
argument_list|,
literal|"DEBUG,console"
argument_list|)
expr_stmt|;
name|factory
operator|=
operator|new
name|CustomQueryFilterFactory
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFactory
parameter_list|()
block|{
name|conf
operator|.
name|unset
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
operator|.
name|varname
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
argument_list|,
name|CUSTOM_QUERY
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testApplyPositive
parameter_list|()
throws|throws
name|AuthenticationException
throws|,
name|NamingException
throws|,
name|IOException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
argument_list|,
name|CUSTOM_QUERY
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|search
operator|.
name|executeCustomQuery
argument_list|(
name|eq
argument_list|(
name|CUSTOM_QUERY
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|USER1_DN
argument_list|,
name|USER2_DN
argument_list|)
argument_list|)
expr_stmt|;
name|Filter
name|filter
init|=
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"user1"
argument_list|)
expr_stmt|;
name|filter
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"user2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|AuthenticationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testApplyNegative
parameter_list|()
throws|throws
name|AuthenticationException
throws|,
name|NamingException
throws|,
name|IOException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
argument_list|,
name|CUSTOM_QUERY
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|search
operator|.
name|executeCustomQuery
argument_list|(
name|eq
argument_list|(
name|CUSTOM_QUERY
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|USER1_DN
argument_list|,
name|USER2_DN
argument_list|)
argument_list|)
expr_stmt|;
name|Filter
name|filter
init|=
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"user3"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

