begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
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
name|HiveAuthenticationProvider
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
name|HiveAuthzSessionContext
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|fallback
operator|.
name|FallbackHiveAuthorizer
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

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

begin_comment
comment|/**  * Test HiveAuthorizer for invoking checkPrivilege Methods for authorization call  * Authorizes user sam and rob.  */
end_comment

begin_class
specifier|public
class|class
name|DummyHiveAuthorizer
extends|extends
name|FallbackHiveAuthorizer
block|{
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|allowedUsers
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"sam"
argument_list|,
literal|"rob"
argument_list|)
decl_stmt|;
name|DummyHiveAuthorizer
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|HiveAuthenticationProvider
name|hiveAuthenticator
parameter_list|,
name|HiveAuthzSessionContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|hiveConf
argument_list|,
name|hiveAuthenticator
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPrivileges
parameter_list|(
name|HiveOperationType
name|hiveOpType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|String
name|user
init|=
literal|null
decl_stmt|;
name|String
name|errorMessage
init|=
literal|""
decl_stmt|;
try|try
block|{
name|user
operator|=
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Unable to get UserGroupInformation"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isOperationAllowed
argument_list|(
name|user
argument_list|)
condition|)
block|{
name|errorMessage
operator|=
literal|"Operation type "
operator|+
name|hiveOpType
operator|+
literal|" not allowed for user:"
operator|+
name|user
expr_stmt|;
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|errorMessage
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|isOperationAllowed
parameter_list|(
name|String
name|user
parameter_list|)
block|{
return|return
name|allowedUsers
operator|.
name|contains
argument_list|(
name|user
argument_list|)
return|;
block|}
block|}
end_class

end_unit

