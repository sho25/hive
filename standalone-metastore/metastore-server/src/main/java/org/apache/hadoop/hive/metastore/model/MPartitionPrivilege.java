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
name|model
package|;
end_package

begin_class
specifier|public
class|class
name|MPartitionPrivilege
block|{
specifier|private
name|String
name|principalName
decl_stmt|;
specifier|private
name|String
name|principalType
decl_stmt|;
specifier|private
name|MPartition
name|partition
decl_stmt|;
specifier|private
name|String
name|privilege
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|String
name|grantor
decl_stmt|;
specifier|private
name|String
name|grantorType
decl_stmt|;
specifier|private
name|boolean
name|grantOption
decl_stmt|;
specifier|private
name|String
name|authorizer
decl_stmt|;
specifier|public
name|MPartitionPrivilege
parameter_list|()
block|{   }
specifier|public
name|MPartitionPrivilege
parameter_list|(
name|String
name|principalName
parameter_list|,
name|String
name|principalType
parameter_list|,
name|MPartition
name|partition
parameter_list|,
name|String
name|privilege
parameter_list|,
name|int
name|createTime
parameter_list|,
name|String
name|grantor
parameter_list|,
name|String
name|grantorType
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|String
name|authorizer
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|principalName
operator|=
name|principalName
expr_stmt|;
name|this
operator|.
name|principalType
operator|=
name|principalType
expr_stmt|;
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
name|this
operator|.
name|privilege
operator|=
name|privilege
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|createTime
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
name|this
operator|.
name|authorizer
operator|=
name|authorizer
expr_stmt|;
block|}
specifier|public
name|String
name|getPrincipalName
parameter_list|()
block|{
return|return
name|principalName
return|;
block|}
specifier|public
name|void
name|setPrincipalName
parameter_list|(
name|String
name|principalName
parameter_list|)
block|{
name|this
operator|.
name|principalName
operator|=
name|principalName
expr_stmt|;
block|}
comment|/**    * @return a set of privileges this user/role/group has    */
specifier|public
name|String
name|getPrivilege
parameter_list|()
block|{
return|return
name|privilege
return|;
block|}
comment|/**    * @param dbPrivilege a set of privileges this user/role/group has    */
specifier|public
name|void
name|setPrivilege
parameter_list|(
name|String
name|dbPrivilege
parameter_list|)
block|{
name|this
operator|.
name|privilege
operator|=
name|dbPrivilege
expr_stmt|;
block|}
comment|/**    * @return create time    */
specifier|public
name|int
name|getCreateTime
parameter_list|()
block|{
return|return
name|createTime
return|;
block|}
comment|/**    * @param createTime create time    */
specifier|public
name|void
name|setCreateTime
parameter_list|(
name|int
name|createTime
parameter_list|)
block|{
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
block|}
comment|/**    * @return the grantor    */
specifier|public
name|String
name|getGrantor
parameter_list|()
block|{
return|return
name|grantor
return|;
block|}
comment|/**    * @param grantor    */
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
name|String
name|getPrincipalType
parameter_list|()
block|{
return|return
name|principalType
return|;
block|}
specifier|public
name|void
name|setPrincipalType
parameter_list|(
name|String
name|principalType
parameter_list|)
block|{
name|this
operator|.
name|principalType
operator|=
name|principalType
expr_stmt|;
block|}
specifier|public
name|MPartition
name|getPartition
parameter_list|()
block|{
return|return
name|partition
return|;
block|}
specifier|public
name|void
name|setPartition
parameter_list|(
name|MPartition
name|partition
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
block|}
specifier|public
name|boolean
name|getGrantOption
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
specifier|public
name|String
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
name|String
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
name|String
name|getAuthorizer
parameter_list|()
block|{
return|return
name|authorizer
return|;
block|}
specifier|public
name|void
name|setAuthorizer
parameter_list|(
name|String
name|authorizer
parameter_list|)
block|{
name|this
operator|.
name|authorizer
operator|=
name|authorizer
expr_stmt|;
block|}
block|}
end_class

end_unit

