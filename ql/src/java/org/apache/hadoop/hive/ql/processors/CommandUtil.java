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
name|processors
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|conf
operator|.
name|HiveConf
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAccessControlException
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzContext
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
name|HiveOperationType
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilegeObject
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
name|ql
operator|.
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_class
class|class
name|CommandUtil
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CommandUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Authorize command of given type and arguments    *    * @param ss    * @param type    * @param command    * @return null if there was no authorization error. Otherwise returns  CommandProcessorResponse    * capturing the authorization error    */
specifier|static
name|CommandProcessorResponse
name|authorizeCommand
parameter_list|(
name|SessionState
name|ss
parameter_list|,
name|HiveOperationType
name|type
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|command
parameter_list|)
block|{
if|if
condition|(
name|ss
operator|==
literal|null
condition|)
block|{
comment|// ss can be null in unit tests
return|return
literal|null
return|;
block|}
if|if
condition|(
name|ss
operator|.
name|isAuthorizationModeV2
argument_list|()
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|)
condition|)
block|{
name|String
name|errMsg
init|=
literal|"Error authorizing command "
operator|+
name|command
decl_stmt|;
try|try
block|{
name|authorizeCommandThrowEx
argument_list|(
name|ss
argument_list|,
name|type
argument_list|,
name|command
argument_list|)
expr_stmt|;
comment|// authorized to perform action
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|CommandProcessorResponse
operator|.
name|create
argument_list|(
name|e
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveAccessControlException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|CommandProcessorResponse
operator|.
name|create
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Authorize command. Throws exception if the check fails    * @param ss    * @param type    * @param command    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
specifier|static
name|void
name|authorizeCommandThrowEx
parameter_list|(
name|SessionState
name|ss
parameter_list|,
name|HiveOperationType
name|type
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|command
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|HivePrivilegeObject
name|commandObj
init|=
name|HivePrivilegeObject
operator|.
name|createHivePrivilegeObject
argument_list|(
name|command
argument_list|)
decl_stmt|;
name|HiveAuthzContext
operator|.
name|Builder
name|ctxBuilder
init|=
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|ctxBuilder
operator|.
name|setCommandString
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|' '
argument_list|)
operator|.
name|join
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setUserIpAddress
argument_list|(
name|ss
operator|.
name|getUserIpAddress
argument_list|()
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setForwardedAddresses
argument_list|(
name|ss
operator|.
name|getForwardedAddresses
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|.
name|getAuthorizerV2
argument_list|()
operator|.
name|checkPrivileges
argument_list|(
name|type
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|commandObj
argument_list|)
argument_list|,
literal|null
argument_list|,
name|ctxBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

