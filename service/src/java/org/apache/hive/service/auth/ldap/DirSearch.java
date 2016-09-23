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
name|Closeable
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

begin_comment
comment|/**  * The object used for executing queries on the Directory Service.  */
end_comment

begin_interface
specifier|public
interface|interface
name|DirSearch
extends|extends
name|Closeable
block|{
comment|/**    * Finds user's distinguished name.    * @param user username    * @return DN for the specified username    * @throws NamingException    */
name|String
name|findUserDn
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|NamingException
function_decl|;
comment|/**    * Finds groups that contain the specified user.    * @param userDn user's distinguished name    * @return list of groups    * @throws NamingException    */
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
function_decl|;
comment|/**    * Executes an arbitrary query.    * @param query any query    * @return list of names in the namespace    * @throws NamingException    */
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
function_decl|;
block|}
end_interface

end_unit

