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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapProxy
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapProxy
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
name|exec
operator|.
name|tez
operator|.
name|LlapObjectCache
import|;
end_import

begin_comment
comment|/**  * ObjectCacheFactory returns the appropriate cache depending on settings in  * the hive conf.  */
end_comment

begin_class
specifier|public
class|class
name|ObjectCacheFactory
block|{
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|ObjectCache
argument_list|>
name|llapQueryCaches
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
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
name|ObjectCacheFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ObjectCacheFactory
parameter_list|()
block|{
comment|// avoid instantiation
block|}
comment|/**    * Returns the appropriate cache    */
specifier|public
specifier|static
name|ObjectCache
name|getCache
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|queryId
parameter_list|,
name|boolean
name|isPlanCache
parameter_list|)
block|{
comment|// LLAP cache can be disabled via config or isPlanCache
return|return
name|getCache
argument_list|(
name|conf
argument_list|,
name|queryId
argument_list|,
name|isPlanCache
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Returns the appropriate cache    * @param conf    * @param queryId    * @param isPlanCache    * @param llapCacheAlwaysEnabled  Whether to always return LLAP cache regardless    *        of config settings disabling LLAP cache. Valid only if running LLAP.    * @return    */
specifier|public
specifier|static
name|ObjectCache
name|getCache
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|queryId
parameter_list|,
name|boolean
name|isPlanCache
parameter_list|,
name|boolean
name|llapCacheAlwaysEnabled
parameter_list|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
if|if
condition|(
name|LlapProxy
operator|.
name|isDaemon
argument_list|()
condition|)
block|{
comment|// daemon
if|if
condition|(
name|isLlapCacheEnabled
argument_list|(
name|conf
argument_list|,
name|isPlanCache
argument_list|,
name|llapCacheAlwaysEnabled
argument_list|)
condition|)
block|{
comment|// LLAP object cache, unlike others, does not use globals. Thus, get the existing one.
return|return
name|getLlapObjectCache
argument_list|(
name|queryId
argument_list|)
return|;
block|}
else|else
block|{
comment|// no cache
return|return
operator|new
name|ObjectCacheWrapper
argument_list|(
operator|new
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
name|exec
operator|.
name|mr
operator|.
name|ObjectCache
argument_list|()
argument_list|,
name|queryId
argument_list|)
return|;
block|}
block|}
else|else
block|{
comment|// container
if|if
condition|(
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
name|exec
operator|.
name|tez
operator|.
name|ObjectCache
operator|.
name|isObjectRegistryConfigured
argument_list|()
condition|)
block|{
return|return
operator|new
name|ObjectCacheWrapper
argument_list|(
operator|new
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
name|exec
operator|.
name|tez
operator|.
name|ObjectCache
argument_list|()
argument_list|,
name|queryId
argument_list|)
return|;
block|}
else|else
block|{
comment|// Tez processor needs to configure object registry first.
return|return
literal|null
return|;
block|}
block|}
block|}
else|else
block|{
comment|// mr or spark
return|return
operator|new
name|ObjectCacheWrapper
argument_list|(
operator|new
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
name|exec
operator|.
name|mr
operator|.
name|ObjectCache
argument_list|()
argument_list|,
name|queryId
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isLlapCacheEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|isPlanCache
parameter_list|,
name|boolean
name|llapCacheAlwaysEnabled
parameter_list|)
block|{
return|return
operator|(
name|llapCacheAlwaysEnabled
operator|||
operator|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_OBJECT_CACHE_ENABLED
argument_list|)
operator|&&
operator|!
name|isPlanCache
operator|)
operator|)
return|;
block|}
specifier|private
specifier|static
name|ObjectCache
name|getLlapObjectCache
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
comment|// If order of events (i.e. dagstart and fragmentstart) was guaranteed, we could just
comment|// create the cache when dag starts, and blindly return it to execution here.
if|if
condition|(
name|queryId
operator|==
literal|null
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Query ID cannot be null"
argument_list|)
throw|;
name|ObjectCache
name|result
init|=
name|llapQueryCaches
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
return|return
name|result
return|;
name|result
operator|=
operator|new
name|LlapObjectCache
argument_list|()
expr_stmt|;
name|ObjectCache
name|old
init|=
name|llapQueryCaches
operator|.
name|putIfAbsent
argument_list|(
name|queryId
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|old
operator|==
literal|null
operator|&&
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Created object cache for "
operator|+
name|queryId
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|old
operator|!=
literal|null
operator|)
condition|?
name|old
else|:
name|result
return|;
block|}
specifier|public
specifier|static
name|void
name|removeLlapQueryCache
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing object cache for "
operator|+
name|queryId
argument_list|)
expr_stmt|;
block|}
name|llapQueryCaches
operator|.
name|remove
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

