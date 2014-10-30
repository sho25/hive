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
operator|.
name|sqlstd
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|HadoopDefaultAuthenticator
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
name|DisallowTransformHook
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
name|HiveAuthorizer
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
name|HiveAuthorizerFactory
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
name|HiveAuthzSessionContext
operator|.
name|Builder
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
operator|.
name|CLIENT_TYPE
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test SQLStdHiveAccessController  */
end_comment

begin_class
specifier|public
class|class
name|TestSQLStdHiveAccessControllerCLI
block|{
comment|/**    * Test that SQLStdHiveAccessController is not applying config restrictions on CLI    *    * @throws HiveAuthzPluginException    */
annotation|@
name|Test
specifier|public
name|void
name|testConfigProcessing
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
name|HiveConf
name|processedConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|SQLStdHiveAccessController
name|accessController
init|=
operator|new
name|SQLStdHiveAccessController
argument_list|(
literal|null
argument_list|,
name|processedConf
argument_list|,
operator|new
name|HadoopDefaultAuthenticator
argument_list|()
argument_list|,
name|getCLISessionCtx
argument_list|()
argument_list|)
decl_stmt|;
name|accessController
operator|.
name|applyAuthorizationConfigPolicy
argument_list|(
name|processedConf
argument_list|)
expr_stmt|;
comment|// check that hook to disable transforms has not been added
name|assertFalse
argument_list|(
literal|"Check for transform query disabling hook"
argument_list|,
name|processedConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|)
operator|.
name|contains
argument_list|(
name|DisallowTransformHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify that some dummy param can be set
name|processedConf
operator|.
name|verifyAndSet
argument_list|(
literal|"dummy.param"
argument_list|,
literal|"dummy.val"
argument_list|)
expr_stmt|;
name|processedConf
operator|.
name|verifyAndSet
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HiveAuthzSessionContext
name|getCLISessionCtx
parameter_list|()
block|{
name|Builder
name|ctxBuilder
init|=
operator|new
name|HiveAuthzSessionContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|ctxBuilder
operator|.
name|setClientType
argument_list|(
name|CLIENT_TYPE
operator|.
name|HIVECLI
argument_list|)
expr_stmt|;
return|return
name|ctxBuilder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Verify that exceptiion is thrown if authorization is enabled from hive cli,    * when sql std auth is used    */
annotation|@
name|Test
specifier|public
name|void
name|testAuthEnableError
parameter_list|()
block|{
name|HiveConf
name|processedConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|processedConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|HiveAuthorizerFactory
name|authorizerFactory
init|=
operator|new
name|SQLStdHiveAuthorizerFactory
argument_list|()
decl_stmt|;
name|HiveAuthorizer
name|authorizer
init|=
name|authorizerFactory
operator|.
name|createHiveAuthorizer
argument_list|(
literal|null
argument_list|,
name|processedConf
argument_list|,
operator|new
name|HadoopDefaultAuthenticator
argument_list|()
argument_list|,
name|getCLISessionCtx
argument_list|()
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"SQL standards based authorization should not be enabled from hive cli"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

