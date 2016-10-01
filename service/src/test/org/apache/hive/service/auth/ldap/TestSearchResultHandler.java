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
name|util
operator|.
name|AbstractCollection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Iterator
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
name|javax
operator|.
name|naming
operator|.
name|NamingEnumeration
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
name|naming
operator|.
name|directory
operator|.
name|SearchResult
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

begin_import
import|import static
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
name|LdapTestUtils
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
name|TestSearchResultHandler
block|{
name|SearchResultHandler
name|handler
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testHandle
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"1"
argument_list|)
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"2"
argument_list|,
literal|"3"
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
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
literal|"1"
argument_list|,
literal|"2"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|handler
operator|.
name|handle
argument_list|(
operator|new
name|SearchResultHandler
operator|.
name|RecordProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|process
parameter_list|(
name|SearchResult
name|record
parameter_list|)
throws|throws
name|NamingException
block|{
name|actual
operator|.
name|add
argument_list|(
name|record
operator|.
name|getNameInNamespace
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|actual
operator|.
name|size
argument_list|()
operator|<
literal|2
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllLdapNamesNoRecords
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addEmptySearchResult
argument_list|()
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|handler
operator|.
name|getAllLdapNames
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Resultset size"
argument_list|,
literal|0
argument_list|,
name|actual
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllLdapNamesWithExceptionInNamingEnumerationClose
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"1"
argument_list|)
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
name|doThrow
argument_list|(
name|NamingException
operator|.
name|class
argument_list|)
operator|.
name|when
argument_list|(
name|resultCollection
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|handler
operator|.
name|getAllLdapNames
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Resultset size"
argument_list|,
literal|2
argument_list|,
name|actual
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllLdapNames
parameter_list|()
throws|throws
name|NamingException
block|{
name|String
name|objectDn1
init|=
literal|"cn=a1,dc=b,dc=c"
decl_stmt|;
name|String
name|objectDn2
init|=
literal|"cn=a2,dc=b,dc=c"
decl_stmt|;
name|String
name|objectDn3
init|=
literal|"cn=a3,dc=b,dc=c"
decl_stmt|;
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
name|objectDn1
argument_list|)
operator|.
name|addSearchResultWithDns
argument_list|(
name|objectDn2
argument_list|,
name|objectDn3
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
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
name|objectDn1
argument_list|,
name|objectDn2
argument_list|,
name|objectDn3
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|handler
operator|.
name|getAllLdapNames
argument_list|()
decl_stmt|;
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
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllLdapNamesAndAttributes
parameter_list|()
throws|throws
name|NamingException
block|{
name|SearchResult
name|searchResult1
init|=
name|mockSearchResult
argument_list|(
literal|"cn=a1,dc=b,dc=c"
argument_list|,
name|mockAttributes
argument_list|(
literal|"attr1"
argument_list|,
literal|"attr1value1"
argument_list|)
argument_list|)
decl_stmt|;
name|SearchResult
name|searchResult2
init|=
name|mockSearchResult
argument_list|(
literal|"cn=a2,dc=b,dc=c"
argument_list|,
name|mockAttributes
argument_list|(
literal|"attr1"
argument_list|,
literal|"attr1value2"
argument_list|,
literal|"attr2"
argument_list|,
literal|"attr2value1"
argument_list|)
argument_list|)
decl_stmt|;
name|SearchResult
name|searchResult3
init|=
name|mockSearchResult
argument_list|(
literal|"cn=a3,dc=b,dc=c"
argument_list|,
name|mockAttributes
argument_list|(
literal|"attr1"
argument_list|,
literal|"attr1value3"
argument_list|,
literal|"attr1"
argument_list|,
literal|"attr1value4"
argument_list|)
argument_list|)
decl_stmt|;
name|SearchResult
name|searchResult4
init|=
name|mockSearchResult
argument_list|(
literal|"cn=a4,dc=b,dc=c"
argument_list|,
name|mockEmptyAttributes
argument_list|()
argument_list|)
decl_stmt|;
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResults
argument_list|(
name|searchResult1
argument_list|)
operator|.
name|addSearchResults
argument_list|(
name|searchResult2
argument_list|,
name|searchResult3
argument_list|)
operator|.
name|addSearchResults
argument_list|(
name|searchResult4
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
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
literal|"cn=a1,dc=b,dc=c"
argument_list|,
literal|"attr1value1"
argument_list|,
literal|"cn=a2,dc=b,dc=c"
argument_list|,
literal|"attr1value2"
argument_list|,
literal|"attr2value1"
argument_list|,
literal|"cn=a3,dc=b,dc=c"
argument_list|,
literal|"attr1value3"
argument_list|,
literal|"attr1value4"
argument_list|,
literal|"cn=a4,dc=b,dc=c"
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|handler
operator|.
name|getAllLdapNamesAndAttributes
argument_list|()
decl_stmt|;
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
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasSingleResultNoRecords
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addEmptySearchResult
argument_list|()
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|handler
operator|.
name|hasSingleResult
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasSingleResult
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|handler
operator|.
name|hasSingleResult
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasSingleResultManyRecords
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"1"
argument_list|)
operator|.
name|addSearchResultWithDns
argument_list|(
literal|"2"
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|handler
operator|.
name|hasSingleResult
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NamingException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testGetSingleLdapNameNoRecords
parameter_list|()
throws|throws
name|NamingException
block|{
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addEmptySearchResult
argument_list|()
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
try|try
block|{
name|handler
operator|.
name|getSingleLdapName
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetSingleLdapName
parameter_list|()
throws|throws
name|NamingException
block|{
name|String
name|objectDn
init|=
literal|"cn=a,dc=b,dc=c"
decl_stmt|;
name|MockResultCollection
name|resultCollection
init|=
name|MockResultCollection
operator|.
name|create
argument_list|()
operator|.
name|addEmptySearchResult
argument_list|()
operator|.
name|addSearchResultWithDns
argument_list|(
name|objectDn
argument_list|)
decl_stmt|;
name|handler
operator|=
operator|new
name|SearchResultHandler
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
name|String
name|expected
init|=
name|objectDn
decl_stmt|;
name|String
name|actual
init|=
name|handler
operator|.
name|getSingleLdapName
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertAllNamingEnumerationsClosed
argument_list|(
name|resultCollection
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertAllNamingEnumerationsClosed
parameter_list|(
name|MockResultCollection
name|resultCollection
parameter_list|)
throws|throws
name|NamingException
block|{
for|for
control|(
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|namingEnumeration
range|:
name|resultCollection
control|)
block|{
name|verify
argument_list|(
name|namingEnumeration
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|MockResultCollection
extends|extends
name|AbstractCollection
argument_list|<
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
argument_list|>
block|{
name|List
argument_list|<
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|static
name|MockResultCollection
name|create
parameter_list|()
block|{
return|return
operator|new
name|MockResultCollection
argument_list|()
return|;
block|}
name|MockResultCollection
name|addSearchResultWithDns
parameter_list|(
name|String
modifier|...
name|dns
parameter_list|)
throws|throws
name|NamingException
block|{
name|results
operator|.
name|add
argument_list|(
name|mockNamingEnumeration
argument_list|(
name|dns
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|MockResultCollection
name|addSearchResults
parameter_list|(
name|SearchResult
modifier|...
name|dns
parameter_list|)
throws|throws
name|NamingException
block|{
name|results
operator|.
name|add
argument_list|(
name|mockNamingEnumeration
argument_list|(
name|dns
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|MockResultCollection
name|addEmptySearchResult
parameter_list|()
throws|throws
name|NamingException
block|{
name|addSearchResults
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|results
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|results
operator|.
name|size
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

