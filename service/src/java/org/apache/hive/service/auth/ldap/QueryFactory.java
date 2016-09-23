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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
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

begin_comment
comment|/**  * A factory for common types of directory service search queries.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|QueryFactory
block|{
specifier|private
specifier|final
name|String
name|guidAttr
decl_stmt|;
specifier|private
specifier|final
name|String
name|groupClassAttr
decl_stmt|;
specifier|private
specifier|final
name|String
name|groupMembershipAttr
decl_stmt|;
comment|/**    * Constructs the factory based on provided Hive configuration.    * @param conf Hive configuration    */
specifier|public
name|QueryFactory
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|guidAttr
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_GUIDKEY
argument_list|)
expr_stmt|;
name|groupClassAttr
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_GROUPCLASS_KEY
argument_list|)
expr_stmt|;
name|groupMembershipAttr
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_GROUPMEMBERSHIP_KEY
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns a query for finding Group DN based on group unique ID.    * @param groupId group unique identifier    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|findGroupDnById
parameter_list|(
name|String
name|groupId
parameter_list|)
block|{
return|return
name|Query
operator|.
name|builder
argument_list|()
operator|.
name|filter
argument_list|(
literal|"(&(objectClass=<groupClassAttr>)(<guidAttr>=<groupID>))"
argument_list|)
operator|.
name|map
argument_list|(
literal|"guidAttr"
argument_list|,
name|guidAttr
argument_list|)
operator|.
name|map
argument_list|(
literal|"groupClassAttr"
argument_list|,
name|groupClassAttr
argument_list|)
operator|.
name|map
argument_list|(
literal|"groupID"
argument_list|,
name|groupId
argument_list|)
operator|.
name|limit
argument_list|(
literal|2
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a query for finding user DN based on user RDN.    * @param userRdn user RDN    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|findUserDnByRdn
parameter_list|(
name|String
name|userRdn
parameter_list|)
block|{
return|return
name|Query
operator|.
name|builder
argument_list|()
operator|.
name|filter
argument_list|(
literal|"(&(|(objectClass=person)(objectClass=user)(objectClass=inetOrgPerson))"
operator|+
literal|"(<userRdn>))"
argument_list|)
operator|.
name|limit
argument_list|(
literal|2
argument_list|)
operator|.
name|map
argument_list|(
literal|"userRdn"
argument_list|,
name|userRdn
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a query for finding user DN based on DN pattern.    *<br>    * Name of this method was derived from the original implementation of LDAP authentication.    * This method should be replaced by {@link QueryFactory#findUserDnByRdn(java.lang.String).    *    * @param rdn user RDN    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|findDnByPattern
parameter_list|(
name|String
name|rdn
parameter_list|)
block|{
return|return
name|Query
operator|.
name|builder
argument_list|()
operator|.
name|filter
argument_list|(
literal|"(<rdn>)"
argument_list|)
operator|.
name|map
argument_list|(
literal|"rdn"
argument_list|,
name|rdn
argument_list|)
operator|.
name|limit
argument_list|(
literal|2
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a query for finding user DN based on user unique name.    * @param userName user unique name (uid or sAMAccountName)    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|findUserDnByName
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
return|return
name|Query
operator|.
name|builder
argument_list|()
operator|.
name|filter
argument_list|(
literal|"(&(|(objectClass=person)(objectClass=user)(objectClass=inetOrgPerson))"
operator|+
literal|"(|(uid=<userName>)(sAMAccountName=<userName>)))"
argument_list|)
operator|.
name|map
argument_list|(
literal|"userName"
argument_list|,
name|userName
argument_list|)
operator|.
name|limit
argument_list|(
literal|2
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a query for finding groups to which the user belongs.    * @param userName username    * @param userDn user DN    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|findGroupsForUser
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|userDn
parameter_list|)
block|{
return|return
name|Query
operator|.
name|builder
argument_list|()
operator|.
name|filter
argument_list|(
literal|"(&(objectClass=<groupClassAttr>)(|(<groupMembershipAttr>=<userDn>)"
operator|+
literal|"(<groupMembershipAttr>=<userName>)))"
argument_list|)
operator|.
name|map
argument_list|(
literal|"groupClassAttr"
argument_list|,
name|groupClassAttr
argument_list|)
operator|.
name|map
argument_list|(
literal|"groupMembershipAttr"
argument_list|,
name|groupMembershipAttr
argument_list|)
operator|.
name|map
argument_list|(
literal|"userName"
argument_list|,
name|userName
argument_list|)
operator|.
name|map
argument_list|(
literal|"userDn"
argument_list|,
name|userDn
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Returns a query object created for the custom filter.    *<br>    * This query is configured to return a group membership attribute as part of the search result.    * @param searchFilter custom search filter    * @return an instance of {@link Query}    */
specifier|public
name|Query
name|customQuery
parameter_list|(
name|String
name|searchFilter
parameter_list|)
block|{
name|Query
operator|.
name|QueryBuilder
name|builder
init|=
name|Query
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|filter
argument_list|(
name|searchFilter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|groupMembershipAttr
argument_list|)
condition|)
block|{
name|builder
operator|.
name|returnAttribute
argument_list|(
name|groupMembershipAttr
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

