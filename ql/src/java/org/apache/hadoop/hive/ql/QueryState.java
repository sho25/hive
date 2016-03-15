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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|metadata
operator|.
name|HiveException
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
name|plan
operator|.
name|HiveOperation
import|;
end_import

begin_comment
comment|/**  * The class to store query level info such as queryId. Multiple queries can run  * in the same session, so SessionState is to hold common session related info, and  * each QueryState is to hold query related info.  *  */
end_comment

begin_class
specifier|public
class|class
name|QueryState
block|{
comment|/**    * current configuration.    */
specifier|private
specifier|final
name|HiveConf
name|queryConf
decl_stmt|;
comment|/**    * type of the command.    */
specifier|private
name|HiveOperation
name|commandType
decl_stmt|;
specifier|public
name|QueryState
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|QueryState
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|boolean
name|runAsync
parameter_list|)
block|{
name|this
operator|.
name|queryConf
operator|=
name|createConf
argument_list|(
name|conf
argument_list|,
name|confOverlay
argument_list|,
name|runAsync
argument_list|)
expr_stmt|;
block|}
comment|/**    * If there are query specific settings to overlay, then create a copy of config    * There are two cases we need to clone the session config that's being passed to hive driver    * 1. Async query -    *    If the client changes a config setting, that shouldn't reflect in the execution already underway    * 2. confOverlay -    *    The query specific settings should only be applied to the query config and not session    * @return new configuration    */
specifier|private
name|HiveConf
name|createConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|boolean
name|runAsync
parameter_list|)
block|{
if|if
condition|(
operator|(
name|confOverlay
operator|!=
literal|null
operator|&&
operator|!
name|confOverlay
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|conf
operator|=
operator|(
name|conf
operator|==
literal|null
condition|?
operator|new
name|HiveConf
argument_list|()
else|:
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
operator|)
expr_stmt|;
comment|// apply overlay query specific settings, if any
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confEntry
range|:
name|confOverlay
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|conf
operator|.
name|verifyAndSet
argument_list|(
name|confEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|confEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error applying statement specific settings"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|runAsync
condition|)
block|{
name|conf
operator|=
operator|(
name|conf
operator|==
literal|null
condition|?
operator|new
name|HiveConf
argument_list|()
else|:
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|,
name|QueryPlan
operator|.
name|makeQueryId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
operator|(
name|queryConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
operator|)
return|;
block|}
specifier|public
name|String
name|getQueryString
parameter_list|()
block|{
return|return
name|queryConf
operator|.
name|getQueryString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getCommandType
parameter_list|()
block|{
if|if
condition|(
name|commandType
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|commandType
operator|.
name|getOperationName
argument_list|()
return|;
block|}
specifier|public
name|HiveOperation
name|getHiveOperation
parameter_list|()
block|{
return|return
name|commandType
return|;
block|}
specifier|public
name|void
name|setCommandType
parameter_list|(
name|HiveOperation
name|commandType
parameter_list|)
block|{
name|this
operator|.
name|commandType
operator|=
name|commandType
expr_stmt|;
block|}
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|queryConf
return|;
block|}
block|}
end_class

end_unit

