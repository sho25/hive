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
name|EnumMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

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
comment|/**  * Default implementation of {@link HiveResourceACLs}.  */
end_comment

begin_class
specifier|public
class|class
name|HiveResourceACLsImpl
implements|implements
name|HiveResourceACLs
block|{
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
name|userPermissions
init|=
operator|new
name|HashMap
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
argument_list|()
decl_stmt|;
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
name|groupPermissions
init|=
operator|new
name|HashMap
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
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
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
block|{
return|return
name|userPermissions
return|;
block|}
annotation|@
name|Override
specifier|public
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
block|{
return|return
name|groupPermissions
return|;
block|}
specifier|public
name|void
name|addUserEntry
parameter_list|(
name|String
name|user
parameter_list|,
name|Privilege
name|priv
parameter_list|,
name|AccessResult
name|result
parameter_list|)
block|{
if|if
condition|(
name|userPermissions
operator|.
name|containsKey
argument_list|(
name|user
argument_list|)
condition|)
block|{
name|userPermissions
operator|.
name|get
argument_list|(
name|user
argument_list|)
operator|.
name|put
argument_list|(
name|priv
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
name|entry
init|=
operator|new
name|EnumMap
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
argument_list|(
name|Privilege
operator|.
name|class
argument_list|)
decl_stmt|;
name|entry
operator|.
name|put
argument_list|(
name|priv
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|userPermissions
operator|.
name|put
argument_list|(
name|user
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addGroupEntry
parameter_list|(
name|String
name|group
parameter_list|,
name|Privilege
name|priv
parameter_list|,
name|AccessResult
name|result
parameter_list|)
block|{
if|if
condition|(
name|groupPermissions
operator|.
name|containsKey
argument_list|(
name|group
argument_list|)
condition|)
block|{
name|groupPermissions
operator|.
name|get
argument_list|(
name|group
argument_list|)
operator|.
name|put
argument_list|(
name|priv
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
name|entry
init|=
operator|new
name|EnumMap
argument_list|<
name|Privilege
argument_list|,
name|AccessResult
argument_list|>
argument_list|(
name|Privilege
operator|.
name|class
argument_list|)
decl_stmt|;
name|entry
operator|.
name|put
argument_list|(
name|priv
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|groupPermissions
operator|.
name|put
argument_list|(
name|group
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

