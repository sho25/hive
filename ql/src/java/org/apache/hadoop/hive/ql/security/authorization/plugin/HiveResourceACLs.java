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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Captures authorization policy information on a {@link HivePrivilegeObject}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveResourceACLs
block|{
comment|/**    * Privilege types.    */
enum|enum
name|Privilege
block|{
name|SELECT
block|,
name|UPDATE
block|,
name|CREATE
block|,
name|DROP
block|,
name|ALTER
block|,
name|INDEX
block|,
name|LOCK
block|,
name|READ
block|,
name|WRITE
block|}
empty_stmt|;
comment|/**    * Privilege access result.    */
enum|enum
name|AccessResult
block|{
name|ALLOWED
block|,
name|NOT_ALLOWED
block|,
name|CONDITIONAL_ALLOWED
block|}
empty_stmt|;
comment|/**    * @return Returns mapping of user name to privilege-access result pairs    */
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
argument_list|>
name|getUserPermissions
parameter_list|()
function_decl|;
comment|/**    * @return Returns mapping of group name to privilege-access result pairs    */
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
argument_list|>
name|getGroupPermissions
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

