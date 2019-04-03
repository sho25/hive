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
comment|/**  * A factory for a {@link Filter} based on a custom query.  *<br>  * The produced filter object filters out all users that are not found in the search result  * of the query provided in Hive configuration.  * @see org.apache.hadoop.hive.conf.HiveConf.ConfVars#HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY  */
end_comment

begin_class
specifier|public
class|class
name|CustomQueryFilterFactory
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
name|HiveConf
name|conf
parameter_list|)
block|{
name|String
name|customQuery
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|customQuery
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CustomQueryFilter
argument_list|(
name|customQuery
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|CustomQueryFilter
implements|implements
name|Filter
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
name|CustomQueryFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|query
decl_stmt|;
specifier|public
name|CustomQueryFilter
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|resultList
decl_stmt|;
try|try
block|{
name|resultList
operator|=
name|client
operator|.
name|executeCustomQuery
argument_list|(
name|query
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|resultList
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|matchedDn
range|:
name|resultList
control|)
block|{
name|String
name|shortUserName
init|=
name|LdapUtils
operator|.
name|getShortName
argument_list|(
name|matchedDn
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"<queried user="
operator|+
name|shortUserName
operator|+
literal|",user="
operator|+
name|user
operator|+
literal|">"
argument_list|)
expr_stmt|;
if|if
condition|(
name|shortUserName
operator|.
name|equalsIgnoreCase
argument_list|(
name|user
argument_list|)
operator|||
name|matchedDn
operator|.
name|equalsIgnoreCase
argument_list|(
name|user
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Authentication succeeded based on result set from LDAP query"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Authentication failed based on result set from custom LDAP query"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Authentication failed: LDAP query "
operator|+
literal|"from property returned no data"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

