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
name|Collection
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
name|DirContext
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Implements search for LDAP.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LdapSearch
implements|implements
name|DirSearch
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LdapSearch
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|baseDn
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|groupBases
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|userBases
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|userPatterns
decl_stmt|;
specifier|private
specifier|final
name|QueryFactory
name|queries
decl_stmt|;
specifier|private
specifier|final
name|DirContext
name|ctx
decl_stmt|;
comment|/**    * Construct an instance of {@code LdapSearch}.    * @param conf Hive configuration    * @param ctx Directory service that will be used for the queries.    * @throws NamingException    */
specifier|public
name|LdapSearch
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|DirContext
name|ctx
parameter_list|)
throws|throws
name|NamingException
block|{
name|baseDn
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_BASEDN
argument_list|)
expr_stmt|;
name|userPatterns
operator|=
name|LdapUtils
operator|.
name|parseDnPatterns
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_USERDNPATTERN
argument_list|)
expr_stmt|;
name|groupBases
operator|=
name|LdapUtils
operator|.
name|patternsToBaseDns
argument_list|(
name|LdapUtils
operator|.
name|parseDnPatterns
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_GROUPDNPATTERN
argument_list|)
argument_list|)
expr_stmt|;
name|userBases
operator|=
name|LdapUtils
operator|.
name|patternsToBaseDns
argument_list|(
name|userPatterns
argument_list|)
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
name|queries
operator|=
operator|new
name|QueryFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Closes this search object and releases any system resources associated    * with it. If the search object is already closed then invoking this    * method has no effect.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|ctx
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NamingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception when closing LDAP context:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|String
name|findUserDn
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|NamingException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|allLdapNames
decl_stmt|;
if|if
condition|(
name|LdapUtils
operator|.
name|isDn
argument_list|(
name|user
argument_list|)
condition|)
block|{
name|String
name|userBaseDn
init|=
name|LdapUtils
operator|.
name|extractBaseDn
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|String
name|userRdn
init|=
name|LdapUtils
operator|.
name|extractFirstRdn
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|allLdapNames
operator|=
name|execute
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|userBaseDn
argument_list|)
argument_list|,
name|queries
operator|.
name|findUserDnByRdn
argument_list|(
name|userRdn
argument_list|)
argument_list|)
operator|.
name|getAllLdapNames
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|allLdapNames
operator|=
name|findDnByPattern
argument_list|(
name|userPatterns
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|allLdapNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|allLdapNames
operator|=
name|execute
argument_list|(
name|userBases
argument_list|,
name|queries
operator|.
name|findUserDnByName
argument_list|(
name|user
argument_list|)
argument_list|)
operator|.
name|getAllLdapNames
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|allLdapNames
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|allLdapNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected exactly one user result for the user: {}, but got {}. Returning null"
argument_list|,
name|user
argument_list|,
name|allLdapNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Matched users: {}"
argument_list|,
name|allLdapNames
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|findDnByPattern
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|patterns
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|NamingException
block|{
for|for
control|(
name|String
name|pattern
range|:
name|patterns
control|)
block|{
name|String
name|baseDnFromPattern
init|=
name|LdapUtils
operator|.
name|extractBaseDn
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
name|String
name|rdn
init|=
name|LdapUtils
operator|.
name|extractFirstRdn
argument_list|(
name|pattern
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"%s"
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
name|execute
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|baseDnFromPattern
argument_list|)
argument_list|,
name|queries
operator|.
name|findDnByPattern
argument_list|(
name|rdn
argument_list|)
argument_list|)
operator|.
name|getAllLdapNames
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|list
return|;
block|}
block|}
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|String
name|findGroupDn
parameter_list|(
name|String
name|group
parameter_list|)
throws|throws
name|NamingException
block|{
return|return
name|execute
argument_list|(
name|groupBases
argument_list|,
name|queries
operator|.
name|findGroupDnById
argument_list|(
name|group
argument_list|)
argument_list|)
operator|.
name|getSingleLdapName
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|boolean
name|isUserMemberOfGroup
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|groupDn
parameter_list|)
throws|throws
name|NamingException
block|{
name|String
name|userId
init|=
name|LdapUtils
operator|.
name|extractUserName
argument_list|(
name|user
argument_list|)
decl_stmt|;
return|return
name|execute
argument_list|(
name|userBases
argument_list|,
name|queries
operator|.
name|isUserMemberOfGroup
argument_list|(
name|userId
argument_list|,
name|groupDn
argument_list|)
argument_list|)
operator|.
name|hasSingleResult
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|findGroupsForUser
parameter_list|(
name|String
name|userDn
parameter_list|)
throws|throws
name|NamingException
block|{
name|String
name|userName
init|=
name|LdapUtils
operator|.
name|extractUserName
argument_list|(
name|userDn
argument_list|)
decl_stmt|;
return|return
name|execute
argument_list|(
name|groupBases
argument_list|,
name|queries
operator|.
name|findGroupsForUser
argument_list|(
name|userName
argument_list|,
name|userDn
argument_list|)
argument_list|)
operator|.
name|getAllLdapNames
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|executeCustomQuery
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|NamingException
block|{
return|return
name|execute
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|baseDn
argument_list|)
argument_list|,
name|queries
operator|.
name|customQuery
argument_list|(
name|query
argument_list|)
argument_list|)
operator|.
name|getAllLdapNamesAndAttributes
argument_list|()
return|;
block|}
specifier|private
name|SearchResultHandler
name|execute
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|baseDns
parameter_list|,
name|Query
name|query
parameter_list|)
block|{
name|List
argument_list|<
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
argument_list|>
name|searchResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Executing a query: '{}' with base DNs {}."
argument_list|,
name|query
operator|.
name|getFilter
argument_list|()
argument_list|,
name|baseDns
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|aBaseDn
range|:
name|baseDns
control|)
block|{
try|try
block|{
name|NamingEnumeration
argument_list|<
name|SearchResult
argument_list|>
name|searchResult
init|=
name|ctx
operator|.
name|search
argument_list|(
name|aBaseDn
argument_list|,
name|query
operator|.
name|getFilter
argument_list|()
argument_list|,
name|query
operator|.
name|getControls
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|searchResult
operator|!=
literal|null
condition|)
block|{
name|searchResults
operator|.
name|add
argument_list|(
name|searchResult
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NamingException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception happened for query '"
operator|+
name|query
operator|.
name|getFilter
argument_list|()
operator|+
literal|"' with base DN '"
operator|+
name|aBaseDn
operator|+
literal|"'"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|SearchResultHandler
argument_list|(
name|searchResults
argument_list|)
return|;
block|}
block|}
end_class

end_unit

