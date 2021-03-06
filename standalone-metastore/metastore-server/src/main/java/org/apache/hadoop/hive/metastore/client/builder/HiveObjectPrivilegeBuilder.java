begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|builder
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
name|metastore
operator|.
name|api
operator|.
name|HiveObjectPrivilege
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
name|HiveObjectRef
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
name|MetaException
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
name|PrivilegeGrantInfo
import|;
end_import

begin_comment
comment|/**  * Builder for {@link HiveObjectPrivilege}.  All values must be set.  */
end_comment

begin_class
specifier|public
class|class
name|HiveObjectPrivilegeBuilder
block|{
specifier|private
name|HiveObjectRef
name|hiveObjectRef
decl_stmt|;
specifier|private
name|String
name|principleName
decl_stmt|;
specifier|private
name|PrincipalType
name|principalType
decl_stmt|;
specifier|private
name|PrivilegeGrantInfo
name|grantInfo
decl_stmt|;
specifier|private
name|String
name|authorizer
decl_stmt|;
specifier|public
name|HiveObjectPrivilegeBuilder
name|setHiveObjectRef
parameter_list|(
name|HiveObjectRef
name|hiveObjectRef
parameter_list|)
block|{
name|this
operator|.
name|hiveObjectRef
operator|=
name|hiveObjectRef
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HiveObjectPrivilegeBuilder
name|setPrincipleName
parameter_list|(
name|String
name|principleName
parameter_list|)
block|{
name|this
operator|.
name|principleName
operator|=
name|principleName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HiveObjectPrivilegeBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|HiveObjectPrivilegeBuilder
name|setGrantInfo
parameter_list|(
name|PrivilegeGrantInfo
name|grantInfo
parameter_list|)
block|{
name|this
operator|.
name|grantInfo
operator|=
name|grantInfo
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HiveObjectPrivilegeBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|HiveObjectPrivilege
name|build
parameter_list|()
throws|throws
name|MetaException
block|{
if|if
condition|(
name|hiveObjectRef
operator|==
literal|null
operator|||
name|principleName
operator|==
literal|null
operator|||
name|principalType
operator|==
literal|null
operator|||
name|grantInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"hive object reference, principle name and type, and grant info "
operator|+
literal|"must all be provided"
argument_list|)
throw|;
block|}
return|return
operator|new
name|HiveObjectPrivilege
argument_list|(
name|hiveObjectRef
argument_list|,
name|principleName
argument_list|,
name|principalType
argument_list|,
name|grantInfo
argument_list|,
name|authorizer
argument_list|)
return|;
block|}
block|}
end_class

end_unit

