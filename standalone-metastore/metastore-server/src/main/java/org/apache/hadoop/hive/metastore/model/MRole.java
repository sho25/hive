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
name|MRole
block|{
specifier|private
name|String
name|roleName
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|String
name|ownerName
decl_stmt|;
specifier|public
name|MRole
parameter_list|()
block|{   }
specifier|public
name|MRole
parameter_list|(
name|String
name|roleName
parameter_list|,
name|int
name|createTime
parameter_list|,
name|String
name|ownerName
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|roleName
operator|=
name|roleName
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
block|}
comment|/**    * @return role name    */
specifier|public
name|String
name|getRoleName
parameter_list|()
block|{
return|return
name|roleName
return|;
block|}
comment|/**    * @param roleName    */
specifier|public
name|void
name|setRoleName
parameter_list|(
name|String
name|roleName
parameter_list|)
block|{
name|this
operator|.
name|roleName
operator|=
name|roleName
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
comment|/**    * @param createTime    *          role create time    */
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
comment|/**    * @return the principal name who created this role    */
specifier|public
name|String
name|getOwnerName
parameter_list|()
block|{
return|return
name|ownerName
return|;
block|}
specifier|public
name|void
name|setOwnerName
parameter_list|(
name|String
name|ownerName
parameter_list|)
block|{
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
block|}
block|}
end_class

end_unit

