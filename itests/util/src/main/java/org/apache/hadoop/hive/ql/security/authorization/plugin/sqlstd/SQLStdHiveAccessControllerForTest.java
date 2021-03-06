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
operator|.
name|sqlstd
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|HiveMetastoreClientFactory
import|;
end_import

begin_comment
comment|/**  * Extends SQLStdHiveAccessController to relax the restriction of not being able to run dfs  * and set commands, so that it is easy to test using .q file tests.  * To be used for testing purposes only!  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|SQLStdHiveAccessControllerForTest
extends|extends
name|SQLStdHiveAccessControllerWrapper
block|{
name|SQLStdHiveAccessControllerForTest
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|,
name|HiveAuthzSessionContext
name|ctx
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|super
argument_list|(
name|metastoreClientFactory
argument_list|,
name|conf
argument_list|,
name|authenticator
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|applyAuthorizationConfigPolicy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|super
operator|.
name|applyAuthorizationConfigPolicy
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
comment|// remove restrictions on the variables that can be set using set command
name|hiveConf
operator|.
name|setModifiableWhiteListRegex
argument_list|(
literal|".*"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

