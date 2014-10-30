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
name|Unstable
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

begin_comment
comment|/**  * Helper class that can be used by authorization implementations to set a  * default list of 'safe' HiveConf parameters that can be edited by user. It  * uses HiveConf white list parameters to enforce this. This can be called from  * HiveAuthorizer.applyAuthorizationConfigPolicy  *  * The set of config parameters that can be set is restricted to parameters that  * don't allow for any code injection, and config parameters that are not  * considered an 'admin config' option.  *  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Argus (incubating)"
block|}
argument_list|)
annotation|@
name|Evolving
annotation|@
name|Unstable
specifier|public
class|class
name|SettableConfigUpdater
block|{
specifier|public
specifier|static
name|void
name|setHiveConfWhiteList
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|String
name|whiteListParamsStr
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|whiteListParamsStr
operator|==
literal|null
operator|&&
name|whiteListParamsStr
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Configuration parameter "
operator|+
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST
operator|.
name|varname
operator|+
literal|" is not iniatialized."
argument_list|)
throw|;
block|}
comment|// append regexes that user wanted to add
name|String
name|whiteListAppend
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST_APPEND
argument_list|)
decl_stmt|;
if|if
condition|(
name|whiteListAppend
operator|!=
literal|null
operator|&&
operator|!
name|whiteListAppend
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|whiteListParamsStr
operator|=
name|whiteListParamsStr
operator|+
literal|"|"
operator|+
name|whiteListAppend
expr_stmt|;
block|}
name|hiveConf
operator|.
name|setModifiableWhiteListRegex
argument_list|(
name|whiteListParamsStr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

