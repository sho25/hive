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
operator|.
name|sqlstd
package|;
end_package

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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzPluginException
import|;
end_import

begin_enum
specifier|public
enum|enum
name|SQLPrivTypeGrant
block|{
name|SELECT_NOGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|SELECT
argument_list|,
literal|false
argument_list|)
block|,
name|SELECT_WGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|SELECT
argument_list|,
literal|true
argument_list|)
block|,
name|INSERT_NOGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|INSERT
argument_list|,
literal|false
argument_list|)
block|,
name|INSERT_WGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|INSERT
argument_list|,
literal|true
argument_list|)
block|,
name|UPDATE_NOGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|UPDATE
argument_list|,
literal|false
argument_list|)
block|,
name|UPDATE_WGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|UPDATE
argument_list|,
literal|true
argument_list|)
block|,
name|DELETE_NOGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|DELETE
argument_list|,
literal|false
argument_list|)
block|,
name|DELETE_WGRANT
argument_list|(
name|SQLPrivilegeType
operator|.
name|DELETE
argument_list|,
literal|true
argument_list|)
block|,
name|OWNER_PRIV
argument_list|(
literal|"OBJECT OWNERSHIP"
argument_list|)
block|,
name|ADMIN_PRIV
argument_list|(
literal|"ADMIN PRIVILEGE"
argument_list|)
block|;
comment|// This one can be used to deny permission for performing the operation
specifier|private
specifier|final
name|SQLPrivilegeType
name|privType
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|withGrant
decl_stmt|;
specifier|private
specifier|final
name|String
name|privDesc
decl_stmt|;
name|SQLPrivTypeGrant
parameter_list|(
name|SQLPrivilegeType
name|privType
parameter_list|,
name|boolean
name|isGrant
parameter_list|)
block|{
name|this
operator|.
name|privType
operator|=
name|privType
expr_stmt|;
name|this
operator|.
name|withGrant
operator|=
name|isGrant
expr_stmt|;
name|this
operator|.
name|privDesc
operator|=
name|privType
operator|.
name|toString
argument_list|()
operator|+
operator|(
name|withGrant
condition|?
literal|" with grant"
else|:
literal|""
operator|)
expr_stmt|;
block|}
comment|/**    * Constructor for privileges that are not the standard sql types, but are used by    * authorization rules    * @param privDesc    */
name|SQLPrivTypeGrant
parameter_list|(
name|String
name|privDesc
parameter_list|)
block|{
name|this
operator|.
name|privDesc
operator|=
name|privDesc
expr_stmt|;
name|this
operator|.
name|privType
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|withGrant
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Find matching enum    * @param privType    * @param isGrant    * @return    */
specifier|public
specifier|static
name|SQLPrivTypeGrant
name|getSQLPrivTypeGrant
parameter_list|(
name|SQLPrivilegeType
name|privType
parameter_list|,
name|boolean
name|isGrant
parameter_list|)
block|{
name|String
name|typeName
init|=
name|privType
operator|.
name|name
argument_list|()
operator|+
operator|(
name|isGrant
condition|?
literal|"_WGRANT"
else|:
literal|"_NOGRANT"
operator|)
decl_stmt|;
return|return
name|SQLPrivTypeGrant
operator|.
name|valueOf
argument_list|(
name|typeName
argument_list|)
return|;
block|}
comment|/**    * Find matching enum    *    * @param privTypeStr    *          privilege type string    * @param isGrant    * @return    * @throws HiveAuthzPluginException    */
specifier|public
specifier|static
name|SQLPrivTypeGrant
name|getSQLPrivTypeGrant
parameter_list|(
name|String
name|privTypeStr
parameter_list|,
name|boolean
name|isGrant
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|SQLPrivilegeType
name|ptype
init|=
name|SQLPrivilegeType
operator|.
name|getRequirePrivilege
argument_list|(
name|privTypeStr
argument_list|)
decl_stmt|;
return|return
name|getSQLPrivTypeGrant
argument_list|(
name|ptype
argument_list|,
name|isGrant
argument_list|)
return|;
block|}
specifier|public
name|SQLPrivilegeType
name|getPrivType
parameter_list|()
block|{
return|return
name|privType
return|;
block|}
specifier|public
name|boolean
name|isWithGrant
parameter_list|()
block|{
return|return
name|withGrant
return|;
block|}
comment|/**    * @return String representation for use in error messages    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|privDesc
return|;
block|}
block|}
end_enum

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

