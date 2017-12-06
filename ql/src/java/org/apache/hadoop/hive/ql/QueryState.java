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
name|lockmgr
operator|.
name|HiveTxnManager
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
comment|/**  * The class to store query level info such as queryId. Multiple queries can run  * in the same session, so SessionState is to hold common session related info, and  * each QueryState is to hold query related info.  */
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
comment|/**    * transaction manager used in the query.    */
specifier|private
name|HiveTxnManager
name|txnManager
decl_stmt|;
comment|/**    * Private constructor, use QueryState.Builder instead    * @param conf The query specific configuration object    */
specifier|private
name|QueryState
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|queryConf
operator|=
name|conf
expr_stmt|;
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
specifier|public
name|HiveTxnManager
name|getTxnManager
parameter_list|()
block|{
return|return
name|txnManager
return|;
block|}
specifier|public
name|void
name|setTxnManager
parameter_list|(
name|HiveTxnManager
name|txnManager
parameter_list|)
block|{
name|this
operator|.
name|txnManager
operator|=
name|txnManager
expr_stmt|;
block|}
comment|/**    * Builder to instantiate the QueryState object.    */
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|runAsync
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|generateNewQueryId
init|=
literal|false
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
comment|/**      * Default constructor - use this builder to create a QueryState object      */
specifier|public
name|Builder
parameter_list|()
block|{     }
comment|/**      * Set this to true if the configuration should be detached from the original config. If not      * set the default value is false.      * @param runAsync If the configuration should be detached      * @return The builder      */
specifier|public
name|Builder
name|withRunAsync
parameter_list|(
name|boolean
name|runAsync
parameter_list|)
block|{
name|this
operator|.
name|runAsync
operator|=
name|runAsync
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set this if there are specific configuration values which should be added to the original      * config. If at least one value is set, then the configuration will be detached from the      * original one.      * @param confOverlay The query specific parameters      * @return The builder      */
specifier|public
name|Builder
name|withConfOverlay
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|this
operator|.
name|confOverlay
operator|=
name|confOverlay
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set this to true if new queryId should be generated, otherwise the original one will be kept.      * If not set the default value is false.      * @param generateNewQueryId If new queryId should be generated      * @return The builder      */
specifier|public
name|Builder
name|withGenerateNewQueryId
parameter_list|(
name|boolean
name|generateNewQueryId
parameter_list|)
block|{
name|this
operator|.
name|generateNewQueryId
operator|=
name|generateNewQueryId
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source HiveConf object used to create the QueryState. If runAsync is false, and the      * confOverLay is empty then we will reuse the conf object as a backing datastore for the      * QueryState. We will create a clone of the conf object otherwise.      * @param hiveConf The source HiveConf      * @return The builder      */
specifier|public
name|Builder
name|withHiveConf
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Creates the QueryState object. The default values are:      * - runAsync false      * - confOverlay null      * - generateNewQueryId false      * - conf null      * @return The generated QueryState object      */
specifier|public
name|QueryState
name|build
parameter_list|()
block|{
name|HiveConf
name|queryConf
init|=
name|hiveConf
decl_stmt|;
if|if
condition|(
name|queryConf
operator|==
literal|null
condition|)
block|{
comment|// Generate a new conf if necessary
name|queryConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|runAsync
operator|||
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
comment|// Detach the original conf if necessary
name|queryConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|queryConf
argument_list|)
expr_stmt|;
block|}
comment|// Set the specific parameters if needed
if|if
condition|(
name|confOverlay
operator|!=
literal|null
operator|&&
operator|!
name|confOverlay
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
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
name|queryConf
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
comment|// Generate the new queryId if needed
if|if
condition|(
name|generateNewQueryId
condition|)
block|{
name|queryConf
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
block|}
return|return
operator|new
name|QueryState
argument_list|(
name|queryConf
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

