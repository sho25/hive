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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_comment
comment|/**  *  * HiveSessionHookContextImpl.  * Session hook context implementation which is created by session  manager  * and passed to hook invocation.  */
end_comment

begin_class
specifier|public
class|class
name|HiveSessionHookContextImpl
implements|implements
name|HiveSessionHookContext
block|{
specifier|private
specifier|final
name|HiveSession
name|hiveSession
decl_stmt|;
name|HiveSessionHookContextImpl
parameter_list|(
name|HiveSession
name|hiveSession
parameter_list|)
block|{
name|this
operator|.
name|hiveSession
operator|=
name|hiveSession
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveConf
name|getSessionConf
parameter_list|()
block|{
return|return
name|hiveSession
operator|.
name|getHiveConf
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionUser
parameter_list|()
block|{
return|return
name|hiveSession
operator|.
name|getUserName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionHandle
parameter_list|()
block|{
return|return
name|hiveSession
operator|.
name|getSessionHandle
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

