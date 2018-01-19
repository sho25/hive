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
name|Attribute
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
name|Attributes
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
name|BasicAttribute
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
name|BasicAttributes
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|when
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|OngoingStubbing
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|LdapTestUtils
block|{
specifier|private
name|LdapTestUtils
parameter_list|()
block|{   }
specifier|public
specifier|static
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|mockEmptyNamingEnumeration
parameter_list|()
throws|throws
name|NamingException
block|{
return|return
name|mockNamingEnumeration
argument_list|(
operator|new
name|SearchResult
index|[
literal|0
index|]
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|mockNamingEnumeration
parameter_list|(
name|String
modifier|...
name|dns
parameter_list|)
throws|throws
name|NamingException
block|{
return|return
name|mockNamingEnumeration
argument_list|(
name|mockSearchResults
argument_list|(
name|dns
argument_list|)
operator|.
name|toArray
argument_list|(
operator|new
name|SearchResult
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|mockNamingEnumeration
parameter_list|(
name|SearchResult
modifier|...
name|searchResults
parameter_list|)
throws|throws
name|NamingException
block|{
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|ne
init|=
operator|(
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
operator|)
name|mock
argument_list|(
name|NamingEnumeration
operator|.
name|class
argument_list|)
decl_stmt|;
name|mockHasMoreMethod
argument_list|(
name|ne
argument_list|,
name|searchResults
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchResults
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|SearchResult
argument_list|>
name|mockedResults
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|searchResults
argument_list|)
decl_stmt|;
name|mockNextMethod
argument_list|(
name|ne
argument_list|,
name|mockedResults
argument_list|)
expr_stmt|;
block|}
return|return
name|ne
return|;
block|}
specifier|public
specifier|static
name|void
name|mockHasMoreMethod
parameter_list|(
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|ne
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|NamingException
block|{
name|OngoingStubbing
argument_list|<
name|Boolean
argument_list|>
name|hasMoreStub
init|=
name|when
argument_list|(
name|ne
operator|.
name|hasMore
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hasMoreStub
operator|=
name|hasMoreStub
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|hasMoreStub
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|mockNextMethod
parameter_list|(
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|ne
parameter_list|,
name|List
argument_list|<
name|SearchResult
argument_list|>
name|searchResults
parameter_list|)
throws|throws
name|NamingException
block|{
name|OngoingStubbing
argument_list|<
name|SearchResult
argument_list|>
name|nextStub
init|=
name|when
argument_list|(
name|ne
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|SearchResult
name|searchResult
range|:
name|searchResults
control|)
block|{
name|nextStub
operator|=
name|nextStub
operator|.
name|thenReturn
argument_list|(
name|searchResult
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|SearchResult
argument_list|>
name|mockSearchResults
parameter_list|(
name|String
index|[]
name|dns
parameter_list|)
block|{
name|List
argument_list|<
name|SearchResult
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|dn
range|:
name|dns
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|mockSearchResult
argument_list|(
name|dn
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
specifier|public
specifier|static
name|SearchResult
name|mockSearchResult
parameter_list|(
name|String
name|dn
parameter_list|,
name|Attributes
name|attributes
parameter_list|)
block|{
name|SearchResult
name|searchResult
init|=
name|mock
argument_list|(
name|SearchResult
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|searchResult
operator|.
name|getNameInNamespace
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|dn
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|searchResult
operator|.
name|getAttributes
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
return|return
name|searchResult
return|;
block|}
specifier|public
specifier|static
name|Attributes
name|mockEmptyAttributes
parameter_list|()
throws|throws
name|NamingException
block|{
return|return
name|mockAttributes
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Attributes
name|mockAttributes
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|NamingException
block|{
return|return
name|mockAttributes
argument_list|(
operator|new
name|NameValues
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Attributes
name|mockAttributes
parameter_list|(
name|String
name|name1
parameter_list|,
name|String
name|value1
parameter_list|,
name|String
name|name2
parameter_list|,
name|String
name|value2
parameter_list|)
throws|throws
name|NamingException
block|{
if|if
condition|(
name|name1
operator|.
name|equals
argument_list|(
name|name2
argument_list|)
condition|)
block|{
return|return
name|mockAttributes
argument_list|(
operator|new
name|NameValues
argument_list|(
name|name1
argument_list|,
name|value1
argument_list|,
name|value2
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|mockAttributes
argument_list|(
operator|new
name|NameValues
argument_list|(
name|name1
argument_list|,
name|value1
argument_list|)
argument_list|,
operator|new
name|NameValues
argument_list|(
name|name2
argument_list|,
name|value2
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|Attributes
name|mockAttributes
parameter_list|(
name|NameValues
modifier|...
name|namedValues
parameter_list|)
throws|throws
name|NamingException
block|{
name|Attributes
name|attributes
init|=
operator|new
name|BasicAttributes
argument_list|()
decl_stmt|;
for|for
control|(
name|NameValues
name|namedValue
range|:
name|namedValues
control|)
block|{
name|Attribute
name|attr
init|=
operator|new
name|BasicAttribute
argument_list|(
name|namedValue
operator|.
name|name
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|value
range|:
name|namedValue
operator|.
name|values
control|)
block|{
name|attr
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|attributes
operator|.
name|put
argument_list|(
name|attr
argument_list|)
expr_stmt|;
block|}
return|return
name|attributes
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|NameValues
block|{
specifier|final
name|String
name|name
decl_stmt|;
specifier|final
name|String
index|[]
name|values
decl_stmt|;
specifier|public
name|NameValues
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|values
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

