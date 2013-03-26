begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_comment
comment|/**  * The base create permissions for ddl objects.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|GroupPermissionsDesc
block|{
specifier|public
name|String
name|group
decl_stmt|;
specifier|public
name|String
name|permissions
decl_stmt|;
specifier|public
name|GroupPermissionsDesc
parameter_list|()
block|{}
specifier|protected
specifier|static
name|boolean
name|xequals
parameter_list|(
name|Object
name|a
parameter_list|,
name|Object
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
return|return
literal|true
return|;
else|else
return|return
literal|false
return|;
block|}
return|return
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|xequals
parameter_list|(
name|boolean
name|a
parameter_list|,
name|boolean
name|b
parameter_list|)
block|{
return|return
name|a
operator|==
name|b
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|xequals
parameter_list|(
name|int
name|a
parameter_list|,
name|int
name|b
parameter_list|)
block|{
return|return
name|a
operator|==
name|b
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|xequals
parameter_list|(
name|char
name|a
parameter_list|,
name|char
name|b
parameter_list|)
block|{
return|return
name|a
operator|==
name|b
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|GroupPermissionsDesc
operator|)
condition|)
return|return
literal|false
return|;
name|GroupPermissionsDesc
name|that
init|=
operator|(
name|GroupPermissionsDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|group
argument_list|,
name|that
operator|.
name|group
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|permissions
argument_list|,
name|that
operator|.
name|permissions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

