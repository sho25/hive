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

begin_comment
comment|/**  * A factory for a {@link Filter} that check whether provided user could be found in the directory.  *<br>  * The produced filter object filters out all users that are not found in the directory.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|UserSearchFilterFactory
implements|implements
name|FilterFactory
block|{
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|Filter
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|groupFilter
init|=
name|MetastoreConf
operator|.
name|getStringCollection
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_GROUPFILTER
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|userFilter
init|=
name|MetastoreConf
operator|.
name|getStringCollection
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLAIN_LDAP_USERFILTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupFilter
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|groupFilter
operator|.
name|contains
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|groupFilter
operator|=
name|Collections
operator|.
name|emptySet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|userFilter
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|userFilter
operator|.
name|contains
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|userFilter
operator|=
name|Collections
operator|.
name|emptySet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|groupFilter
operator|.
name|isEmpty
argument_list|()
operator|&&
name|userFilter
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|UserSearchFilter
argument_list|()
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|UserSearchFilter
implements|implements
name|Filter
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|DirSearch
name|client
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|AuthenticationException
block|{
try|try
block|{
name|String
name|userDn
init|=
name|client
operator|.
name|findUserDn
argument_list|(
name|user
argument_list|)
decl_stmt|;
comment|// This should not be null because we were allowed to bind with this username
comment|// safe check in case we were able to bind anonymously.
if|if
condition|(
name|userDn
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Authentication failed: User search failed"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NamingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"LDAP Authentication failed for user"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

