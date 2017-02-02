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
name|Preconditions
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
name|List
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
name|SearchControls
import|;
end_import

begin_import
import|import
name|org
operator|.
name|stringtemplate
operator|.
name|v4
operator|.
name|ST
import|;
end_import

begin_comment
comment|/**  * The object that encompasses all components of a Directory Service search query.  *<br>  * @see LdapSearch  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|Query
block|{
specifier|private
specifier|final
name|String
name|filter
decl_stmt|;
specifier|private
specifier|final
name|SearchControls
name|controls
decl_stmt|;
comment|/**    * Constructs an instance of Directory Service search query.    * @param filter search filter    * @param controls search controls    */
specifier|public
name|Query
parameter_list|(
name|String
name|filter
parameter_list|,
name|SearchControls
name|controls
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|controls
operator|=
name|controls
expr_stmt|;
block|}
comment|/**    * Returns search filter.    * @return search filter    */
specifier|public
name|String
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
comment|/**    * Returns search controls.    * @return search controls    */
specifier|public
name|SearchControls
name|getControls
parameter_list|()
block|{
return|return
name|controls
return|;
block|}
comment|/**    * Creates Query Builder.    * @return query builder.    */
specifier|public
specifier|static
name|QueryBuilder
name|builder
parameter_list|()
block|{
return|return
operator|new
name|QueryBuilder
argument_list|()
return|;
block|}
comment|/**    * A builder of the {@link Query}.    */
specifier|public
specifier|static
specifier|final
class|class
name|QueryBuilder
block|{
specifier|private
name|ST
name|filterTemplate
decl_stmt|;
specifier|private
specifier|final
name|SearchControls
name|controls
init|=
operator|new
name|SearchControls
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|returningAttributes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|QueryBuilder
parameter_list|()
block|{
name|controls
operator|.
name|setSearchScope
argument_list|(
name|SearchControls
operator|.
name|SUBTREE_SCOPE
argument_list|)
expr_stmt|;
name|controls
operator|.
name|setReturningAttributes
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets search filter template.      * @param filterTemplate search filter template      * @return the current instance of the builder      */
specifier|public
name|QueryBuilder
name|filter
parameter_list|(
name|String
name|filterTemplate
parameter_list|)
block|{
name|this
operator|.
name|filterTemplate
operator|=
operator|new
name|ST
argument_list|(
name|filterTemplate
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets mapping between names in the search filter template and actual values.      * @param key marker in the search filter template.      * @param value actual value      * @return the current instance of the builder      */
specifier|public
name|QueryBuilder
name|map
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|filterTemplate
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets mapping between names in the search filter template and actual values.      * @param key marker in the search filter template.      * @param values array of values      * @return the current instance of the builder      */
specifier|public
name|QueryBuilder
name|map
parameter_list|(
name|String
name|key
parameter_list|,
name|String
index|[]
name|values
parameter_list|)
block|{
name|filterTemplate
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets attribute that should be returned in results for the query.      * @param attributeName attribute name      * @return the current instance of the builder      */
specifier|public
name|QueryBuilder
name|returnAttribute
parameter_list|(
name|String
name|attributeName
parameter_list|)
block|{
name|returningAttributes
operator|.
name|add
argument_list|(
name|attributeName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the maximum number of entries to be returned as a result of the search.      *<br>      * 0 indicates no limit: all entries will be returned.      * @param limit The maximum number of entries that will be returned.      * @return the current instance of the builder      */
specifier|public
name|QueryBuilder
name|limit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|controls
operator|.
name|setCountLimit
argument_list|(
name|limit
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|void
name|validate
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|filterTemplate
operator|!=
literal|null
argument_list|,
literal|"filter is required for LDAP search query"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|createFilter
parameter_list|()
block|{
return|return
name|filterTemplate
operator|.
name|render
argument_list|()
return|;
block|}
specifier|private
name|void
name|updateControls
parameter_list|()
block|{
if|if
condition|(
operator|!
name|returningAttributes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|controls
operator|.
name|setReturningAttributes
argument_list|(
name|returningAttributes
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|returningAttributes
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Builds an instance of {@link Query}.      * @return configured directory service query      */
specifier|public
name|Query
name|build
parameter_list|()
block|{
name|validate
argument_list|()
expr_stmt|;
name|String
name|filter
init|=
name|createFilter
argument_list|()
decl_stmt|;
name|updateControls
argument_list|()
expr_stmt|;
return|return
operator|new
name|Query
argument_list|(
name|filter
argument_list|,
name|controls
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

