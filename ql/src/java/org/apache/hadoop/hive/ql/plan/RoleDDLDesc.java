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
name|io
operator|.
name|Serializable
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
literal|"Create Role"
argument_list|)
specifier|public
class|class
name|RoleDDLDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|PrincipalType
name|principalType
decl_stmt|;
specifier|private
name|boolean
name|group
decl_stmt|;
specifier|private
name|RoleOperation
name|operation
decl_stmt|;
specifier|private
name|String
name|resFile
decl_stmt|;
specifier|private
name|String
name|roleOwnerName
decl_stmt|;
comment|/**    * thrift ddl for the result of show roles.    */
specifier|private
specifier|static
specifier|final
name|String
name|roleNameSchema
init|=
literal|"role#string"
decl_stmt|;
comment|/**    * thrift ddl for the result of show role.    */
specifier|private
specifier|static
specifier|final
name|String
name|roleDescSchema
init|=
literal|"role,create_time,principal_name,principal_type,grant_option,grant_time,grantor#"
operator|+
literal|"string:bigint:string:string:boolean:bigint:string"
decl_stmt|;
specifier|public
specifier|static
name|String
name|getRoleNameSchema
parameter_list|()
block|{
return|return
name|roleNameSchema
return|;
block|}
specifier|public
specifier|static
name|String
name|getRoleDescSchema
parameter_list|()
block|{
return|return
name|roleDescSchema
return|;
block|}
specifier|public
specifier|static
enum|enum
name|RoleOperation
block|{
name|DROP_ROLE
argument_list|(
literal|"drop_role"
argument_list|)
block|,
name|CREATE_ROLE
argument_list|(
literal|"create_role"
argument_list|)
block|,
name|SHOW_ROLE_GRANT
argument_list|(
literal|"show_role_grant"
argument_list|)
block|,
name|SHOW_ROLES
argument_list|(
literal|"show_roles"
argument_list|)
block|,
name|SET_ROLE
argument_list|(
literal|"set_role"
argument_list|)
block|,
name|SHOW_CURRENT_ROLE
argument_list|(
literal|"show_current_role"
argument_list|)
block|;
specifier|private
name|String
name|operationName
decl_stmt|;
specifier|private
name|RoleOperation
parameter_list|()
block|{     }
specifier|private
name|RoleOperation
parameter_list|(
name|String
name|operationName
parameter_list|)
block|{
name|this
operator|.
name|operationName
operator|=
name|operationName
expr_stmt|;
block|}
specifier|public
name|String
name|getOperationName
parameter_list|()
block|{
return|return
name|operationName
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|operationName
return|;
block|}
block|}
specifier|public
name|RoleDDLDesc
parameter_list|()
block|{   }
specifier|public
name|RoleDDLDesc
parameter_list|(
name|String
name|roleName
parameter_list|,
name|RoleOperation
name|operation
parameter_list|)
block|{
name|this
argument_list|(
name|roleName
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
name|operation
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RoleDDLDesc
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|RoleOperation
name|operation
parameter_list|,
name|String
name|roleOwnerName
parameter_list|)
block|{
name|this
operator|.
name|name
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
name|operation
operator|=
name|operation
expr_stmt|;
name|this
operator|.
name|roleOwnerName
operator|=
name|roleOwnerName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|)
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|roleName
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|roleName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"role operation"
argument_list|)
specifier|public
name|RoleOperation
name|getOperation
parameter_list|()
block|{
return|return
name|operation
return|;
block|}
specifier|public
name|void
name|setOperation
parameter_list|(
name|RoleOperation
name|operation
parameter_list|)
block|{
name|this
operator|.
name|operation
operator|=
name|operation
expr_stmt|;
block|}
specifier|public
name|PrincipalType
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
name|PrincipalType
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
name|boolean
name|getGroup
parameter_list|()
block|{
return|return
name|group
return|;
block|}
specifier|public
name|void
name|setGroup
parameter_list|(
name|boolean
name|group
parameter_list|)
block|{
name|this
operator|.
name|group
operator|=
name|group
expr_stmt|;
block|}
specifier|public
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
specifier|public
name|void
name|setResFile
parameter_list|(
name|String
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
specifier|public
name|String
name|getRoleOwnerName
parameter_list|()
block|{
return|return
name|roleOwnerName
return|;
block|}
specifier|public
name|void
name|setRoleOwnerName
parameter_list|(
name|String
name|roleOwnerName
parameter_list|)
block|{
name|this
operator|.
name|roleOwnerName
operator|=
name|roleOwnerName
expr_stmt|;
block|}
block|}
end_class

end_unit

