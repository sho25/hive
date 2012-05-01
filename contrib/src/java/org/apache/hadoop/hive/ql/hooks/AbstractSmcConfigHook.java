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
name|hooks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hooks
operator|.
name|conf
operator|.
name|FBHiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_comment
comment|/**  * An abstract class which should be extended by hooks which read configurations from an SMC  * config tier.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractSmcConfigHook
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AbstractSmcConfigHook
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONFIG_FIELD
init|=
literal|"config"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ENABLED_FIELD
init|=
literal|"enabled"
decl_stmt|;
specifier|private
name|ThreadLocal
argument_list|<
name|ConnectionUrlFactory
argument_list|>
name|urlFactory
init|=
literal|null
decl_stmt|;
comment|/**    * Given a HiveConf, checks if the SMC hook enabled config is set to true    *    * @param conf    * @return    */
specifier|protected
name|boolean
name|isEnabled
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|boolean
name|enabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|FBHiveConf
operator|.
name|ENABLED_CONFIG
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"SMC hook is not enabled."
argument_list|)
expr_stmt|;
block|}
return|return
name|enabled
return|;
block|}
comment|/**    * In each top level config object (jo) there is an enabled field.  This method checks that that    * field exists, is set properly, and is set to true.    *    * @param jo    * @param packageName    * @return    * @throws JSONException    */
specifier|protected
name|boolean
name|isConfigEnabled
parameter_list|(
name|JSONObject
name|jo
parameter_list|,
name|String
name|packageName
parameter_list|)
throws|throws
name|JSONException
block|{
name|boolean
name|enabled
init|=
literal|false
decl_stmt|;
name|Object
name|enabledObj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|jo
operator|.
name|has
argument_list|(
name|ENABLED_FIELD
argument_list|)
condition|)
block|{
name|enabledObj
operator|=
name|jo
operator|.
name|get
argument_list|(
name|ENABLED_FIELD
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enabledObj
operator|==
literal|null
operator|||
operator|!
operator|(
name|enabledObj
operator|instanceof
name|Boolean
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"enabled not properly set!"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|enabled
operator|=
name|enabledObj
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"package "
operator|+
name|packageName
operator|+
literal|" is not enabled"
argument_list|)
expr_stmt|;
block|}
return|return
name|enabled
return|;
block|}
comment|/**    * Given a HiveConf object, this method goes to the config tier and retrieves the underlying    * config object (whether that's an array, object, or any other type of JSON).  It also performs    * checks that the tier can be retrieved, the package name is set, the config is enabled, etc.    *    * @param conf    * @return    * @throws JSONException    * @throws ServiceException    * @throws TException    */
specifier|protected
name|Object
name|getConfigObject
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|JSONException
throws|,
name|Exception
throws|,
name|TException
block|{
comment|// Get the properties for this package
name|String
name|packageName
init|=
name|conf
operator|.
name|get
argument_list|(
name|FBHiveConf
operator|.
name|FB_CURRENT_CLUSTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|packageName
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to use configs stored in SMC - no hive package set."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|urlFactory
operator|==
literal|null
condition|)
block|{
name|urlFactory
operator|=
operator|new
name|ThreadLocal
argument_list|<
name|ConnectionUrlFactory
argument_list|>
argument_list|()
expr_stmt|;
name|urlFactory
operator|.
name|set
argument_list|(
name|HookUtils
operator|.
name|getUrlFactory
argument_list|(
name|conf
argument_list|,
name|FBHiveConf
operator|.
name|CONNECTION_FACTORY
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|s
init|=
name|urlFactory
operator|.
name|get
argument_list|()
operator|.
name|getValue
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|FBHiveConf
operator|.
name|HIVE_CONFIG_TIER
argument_list|)
argument_list|,
name|packageName
argument_list|)
decl_stmt|;
name|JSONObject
name|jo
init|=
operator|new
name|JSONObject
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Object
name|configObj
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|isConfigEnabled
argument_list|(
name|jo
argument_list|,
name|packageName
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|jo
operator|.
name|has
argument_list|(
name|CONFIG_FIELD
argument_list|)
condition|)
block|{
name|configObj
operator|=
name|jo
operator|.
name|get
argument_list|(
name|CONFIG_FIELD
argument_list|)
expr_stmt|;
block|}
return|return
name|configObj
return|;
block|}
block|}
end_class

end_unit

