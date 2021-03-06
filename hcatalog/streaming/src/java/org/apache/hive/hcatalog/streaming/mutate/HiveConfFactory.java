begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
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
name|conf
operator|.
name|Configuration
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

begin_comment
comment|/** Creates/configures {@link HiveConf} instances with required ACID attributes.  * @deprecated as of Hive 3.0.0  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|HiveConfFactory
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveConfFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TRANSACTION_MANAGER
init|=
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
decl_stmt|;
specifier|public
specifier|static
name|HiveConf
name|newInstance
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|metaStoreUri
parameter_list|)
block|{
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|configuration
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|HiveConf
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|configuration
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|configuration
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hiveConf
operator|=
operator|(
name|HiveConf
operator|)
name|configuration
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hiveConf
operator|==
literal|null
condition|)
block|{
name|hiveConf
operator|=
name|HiveConfFactory
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|metaStoreUri
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|HiveConfFactory
operator|.
name|overrideSettings
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
return|return
name|hiveConf
return|;
block|}
specifier|public
specifier|static
name|HiveConf
name|newInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|metaStoreUri
parameter_list|)
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaStoreUri
operator|!=
literal|null
condition|)
block|{
name|setHiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
name|metaStoreUri
argument_list|)
expr_stmt|;
block|}
name|overrideSettings
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|public
specifier|static
name|void
name|overrideSettings
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|setHiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
argument_list|,
name|TRANSACTION_MANAGER
argument_list|)
expr_stmt|;
name|setHiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setHiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EXECUTE_SET_UGI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Avoids creating Tez Client sessions internally as it takes much longer currently
name|setHiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|,
literal|"mr"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|var
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Overriding HiveConf setting : {} = {}"
argument_list|,
name|var
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setVar
argument_list|(
name|var
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|var
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Overriding HiveConf setting : {} = {}"
argument_list|,
name|var
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setBoolVar
argument_list|(
name|var
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

