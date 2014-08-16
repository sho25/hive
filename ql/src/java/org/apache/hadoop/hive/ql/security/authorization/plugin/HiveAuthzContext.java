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
comment|/**  * Provides context information in authorization check call that can be used for  * auditing and/or authorization.  * It is an immutable class. Builder inner class is used instantiate it.  */
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
specifier|final
class|class
name|HiveAuthzContext
block|{
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|String
name|userIpAddress
decl_stmt|;
specifier|private
name|String
name|commandString
decl_stmt|;
comment|/**      * Get user's ip address. This is set only if the authorization      * api is invoked from a HiveServer2 instance in standalone mode.      * @return ip address      */
specifier|public
name|String
name|getUserIpAddress
parameter_list|()
block|{
return|return
name|userIpAddress
return|;
block|}
specifier|public
name|void
name|setUserIpAddress
parameter_list|(
name|String
name|userIpAddress
parameter_list|)
block|{
name|this
operator|.
name|userIpAddress
operator|=
name|userIpAddress
expr_stmt|;
block|}
specifier|public
name|String
name|getCommandString
parameter_list|()
block|{
return|return
name|commandString
return|;
block|}
specifier|public
name|void
name|setCommandString
parameter_list|(
name|String
name|commandString
parameter_list|)
block|{
name|this
operator|.
name|commandString
operator|=
name|commandString
expr_stmt|;
block|}
specifier|public
name|HiveAuthzContext
name|build
parameter_list|()
block|{
return|return
operator|new
name|HiveAuthzContext
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|final
name|String
name|userIpAddress
decl_stmt|;
specifier|private
specifier|final
name|String
name|commandString
decl_stmt|;
specifier|private
name|HiveAuthzContext
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|userIpAddress
operator|=
name|builder
operator|.
name|userIpAddress
expr_stmt|;
name|this
operator|.
name|commandString
operator|=
name|builder
operator|.
name|commandString
expr_stmt|;
block|}
specifier|public
name|String
name|getIpAddress
parameter_list|()
block|{
return|return
name|userIpAddress
return|;
block|}
specifier|public
name|String
name|getCommandString
parameter_list|()
block|{
return|return
name|commandString
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
literal|"HiveAuthzContext [userIpAddress="
operator|+
name|userIpAddress
operator|+
literal|", commandString="
operator|+
name|commandString
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

