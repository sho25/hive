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
name|security
operator|.
name|authorization
operator|.
name|plugin
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
import|;
end_import

begin_comment
comment|/**  * Represents the user or role in grant/revoke statements  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|""
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
class|class
name|HivePrincipal
block|{
specifier|public
enum|enum
name|HivePrincipalType
block|{
name|USER
block|,
name|ROLE
block|,
name|UNKNOWN
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Principal [name="
operator|+
name|name
operator|+
literal|", type="
operator|+
name|type
operator|+
literal|"]"
return|;
block|}
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|HivePrincipalType
name|type
decl_stmt|;
specifier|public
name|HivePrincipal
parameter_list|(
name|String
name|name
parameter_list|,
name|HivePrincipalType
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
if|if
condition|(
name|type
operator|==
name|HivePrincipalType
operator|.
name|ROLE
condition|)
block|{
comment|// lower case role to make operations on it case insensitive
comment|// when the old default authorization gets deprecated, this can move
comment|// to ObjectStore code base
name|this
operator|.
name|name
operator|=
name|name
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
block|}
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
name|HivePrincipalType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|name
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|name
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|type
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|type
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|HivePrincipal
name|other
init|=
operator|(
name|HivePrincipal
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|other
operator|.
name|name
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|type
operator|!=
name|other
operator|.
name|type
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

