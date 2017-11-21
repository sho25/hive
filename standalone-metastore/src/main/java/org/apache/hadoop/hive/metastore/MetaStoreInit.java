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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|metastore
operator|.
name|utils
operator|.
name|JavaUtils
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|hooks
operator|.
name|JDOConnectionURLHook
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * MetaStoreInit defines functions to init/update MetaStore connection url.  *  */
end_comment

begin_class
specifier|public
class|class
name|MetaStoreInit
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
name|MetaStoreInit
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
class|class
name|MetaStoreInitData
block|{
name|JDOConnectionURLHook
name|urlHook
init|=
literal|null
decl_stmt|;
name|String
name|urlHookClassName
init|=
literal|""
decl_stmt|;
block|}
comment|/**    * Updates the connection URL in hiveConf using the hook (if a hook has been    * set using hive.metastore.ds.connection.url.hook property)    * @param originalConf - original configuration used to look up hook settings    * @param activeConf - the configuration file in use for looking up db url    * @param badUrl    * @param updateData - hook information    * @return true if a new connection URL was loaded into the thread local    *         configuration    * @throws MetaException    */
specifier|static
name|boolean
name|updateConnectionURL
parameter_list|(
name|Configuration
name|originalConf
parameter_list|,
name|Configuration
name|activeConf
parameter_list|,
name|String
name|badUrl
parameter_list|,
name|MetaStoreInitData
name|updateData
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|connectUrl
init|=
literal|null
decl_stmt|;
name|String
name|currentUrl
init|=
name|MetaStoreInit
operator|.
name|getConnectionURL
argument_list|(
name|activeConf
argument_list|)
decl_stmt|;
try|try
block|{
comment|// We always call init because the hook name in the configuration could
comment|// have changed.
name|initConnectionUrlHook
argument_list|(
name|originalConf
argument_list|,
name|updateData
argument_list|)
expr_stmt|;
if|if
condition|(
name|updateData
operator|.
name|urlHook
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|badUrl
operator|!=
literal|null
condition|)
block|{
name|updateData
operator|.
name|urlHook
operator|.
name|notifyBadConnectionUrl
argument_list|(
name|badUrl
argument_list|)
expr_stmt|;
block|}
name|connectUrl
operator|=
name|updateData
operator|.
name|urlHook
operator|.
name|getJdoConnectionUrl
argument_list|(
name|originalConf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception while getting connection URL from the hook: "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|connectUrl
operator|!=
literal|null
operator|&&
operator|!
name|connectUrl
operator|.
name|equals
argument_list|(
name|currentUrl
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Overriding %s with %s"
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTURLKEY
operator|.
name|toString
argument_list|()
argument_list|,
name|connectUrl
argument_list|)
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|activeConf
argument_list|,
name|ConfVars
operator|.
name|CONNECTURLKEY
argument_list|,
name|connectUrl
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|static
name|String
name|getConnectionURL
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|CONNECTURLKEY
argument_list|,
literal|""
argument_list|)
return|;
block|}
comment|// Multiple threads could try to initialize at the same time.
specifier|synchronized
specifier|private
specifier|static
name|void
name|initConnectionUrlHook
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|MetaStoreInitData
name|updateData
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
name|String
name|className
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|CONNECTURLHOOK
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|updateData
operator|.
name|urlHookClassName
operator|=
literal|""
expr_stmt|;
name|updateData
operator|.
name|urlHook
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|boolean
name|urlHookChanged
init|=
operator|!
name|updateData
operator|.
name|urlHookClassName
operator|.
name|equals
argument_list|(
name|className
argument_list|)
decl_stmt|;
if|if
condition|(
name|updateData
operator|.
name|urlHook
operator|==
literal|null
operator|||
name|urlHookChanged
condition|)
block|{
name|updateData
operator|.
name|urlHookClassName
operator|=
name|className
operator|.
name|trim
argument_list|()
expr_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|urlHookClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|updateData
operator|.
name|urlHookClassName
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|updateData
operator|.
name|urlHook
operator|=
operator|(
name|JDOConnectionURLHook
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|urlHookClass
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

