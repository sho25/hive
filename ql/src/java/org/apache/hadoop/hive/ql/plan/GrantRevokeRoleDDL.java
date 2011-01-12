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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
package|;
end_package

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
name|api
operator|.
name|PrincipalType
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"grant or revoke roles"
argument_list|)
specifier|public
class|class
name|GrantRevokeRoleDDL
block|{
specifier|private
name|boolean
name|grant
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PrincipalDesc
argument_list|>
name|principalDesc
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|roles
decl_stmt|;
specifier|private
name|String
name|grantor
decl_stmt|;
specifier|private
name|PrincipalType
name|grantorType
decl_stmt|;
specifier|private
name|boolean
name|grantOption
decl_stmt|;
specifier|public
name|GrantRevokeRoleDDL
parameter_list|()
block|{   }
specifier|public
name|GrantRevokeRoleDDL
parameter_list|(
name|boolean
name|grant
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|List
argument_list|<
name|PrincipalDesc
argument_list|>
name|principalDesc
parameter_list|,
name|String
name|grantor
parameter_list|,
name|PrincipalType
name|grantorType
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|grant
operator|=
name|grant
expr_stmt|;
name|this
operator|.
name|principalDesc
operator|=
name|principalDesc
expr_stmt|;
name|this
operator|.
name|roles
operator|=
name|roles
expr_stmt|;
name|this
operator|.
name|grantor
operator|=
name|grantor
expr_stmt|;
name|this
operator|.
name|grantorType
operator|=
name|grantorType
expr_stmt|;
name|this
operator|.
name|grantOption
operator|=
name|grantOption
expr_stmt|;
block|}
comment|/**    * @return grant or revoke privileges    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"grant (or revoke)"
argument_list|)
specifier|public
name|boolean
name|getGrant
parameter_list|()
block|{
return|return
name|grant
return|;
block|}
specifier|public
name|void
name|setGrant
parameter_list|(
name|boolean
name|grant
parameter_list|)
block|{
name|this
operator|.
name|grant
operator|=
name|grant
expr_stmt|;
block|}
comment|/**    * @return a list of principals    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"principals"
argument_list|)
specifier|public
name|List
argument_list|<
name|PrincipalDesc
argument_list|>
name|getPrincipalDesc
parameter_list|()
block|{
return|return
name|principalDesc
return|;
block|}
specifier|public
name|void
name|setPrincipalDesc
parameter_list|(
name|List
argument_list|<
name|PrincipalDesc
argument_list|>
name|principalDesc
parameter_list|)
block|{
name|this
operator|.
name|principalDesc
operator|=
name|principalDesc
expr_stmt|;
block|}
comment|/**    * @return a list of roles    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"roles"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getRoles
parameter_list|()
block|{
return|return
name|roles
return|;
block|}
specifier|public
name|void
name|setRoles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|)
block|{
name|this
operator|.
name|roles
operator|=
name|roles
expr_stmt|;
block|}
specifier|public
name|String
name|getGrantor
parameter_list|()
block|{
return|return
name|grantor
return|;
block|}
specifier|public
name|void
name|setGrantor
parameter_list|(
name|String
name|grantor
parameter_list|)
block|{
name|this
operator|.
name|grantor
operator|=
name|grantor
expr_stmt|;
block|}
specifier|public
name|PrincipalType
name|getGrantorType
parameter_list|()
block|{
return|return
name|grantorType
return|;
block|}
specifier|public
name|void
name|setGrantorType
parameter_list|(
name|PrincipalType
name|grantorType
parameter_list|)
block|{
name|this
operator|.
name|grantorType
operator|=
name|grantorType
expr_stmt|;
block|}
specifier|public
name|boolean
name|isGrantOption
parameter_list|()
block|{
return|return
name|grantOption
return|;
block|}
specifier|public
name|void
name|setGrantOption
parameter_list|(
name|boolean
name|grantOption
parameter_list|)
block|{
name|this
operator|.
name|grantOption
operator|=
name|grantOption
expr_stmt|;
block|}
block|}
end_class

end_unit

