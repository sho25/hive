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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_comment
comment|/**  * LlapObjectCache. Llap implementation for the shared object cache.  *  */
end_comment

begin_class
specifier|public
class|class
name|LlapObjectCache
implements|implements
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
name|ObjectCache
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LlapObjectCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ExecutorService
name|staticPool
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|isLogInfoEnabled
init|=
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|registry
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|softValues
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ReentrantLock
argument_list|>
name|locks
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ReentrantLock
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|release
parameter_list|(
name|String
name|key
parameter_list|)
block|{
comment|// nothing to do, soft references will clean themselves up
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
block|{
name|T
name|value
init|=
literal|null
decl_stmt|;
name|ReentrantLock
name|objectLock
init|=
literal|null
decl_stmt|;
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|value
operator|=
operator|(
name|T
operator|)
name|registry
operator|.
name|getIfPresent
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|key
operator|+
literal|" in cache"
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
if|if
condition|(
name|locks
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|objectLock
operator|=
name|locks
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|objectLock
operator|=
operator|new
name|ReentrantLock
argument_list|()
expr_stmt|;
name|locks
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|objectLock
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|objectLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|value
operator|=
operator|(
name|T
operator|)
name|registry
operator|.
name|getIfPresent
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|key
operator|+
literal|" in cache"
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|value
operator|=
name|fn
operator|.
name|call
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Caching new object for key: "
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
name|registry
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|locks
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|objectLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|retrieveAsync
parameter_list|(
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|staticPool
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|retrieve
argument_list|(
name|key
argument_list|,
name|fn
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing key: "
operator|+
name|key
argument_list|)
expr_stmt|;
name|registry
operator|.
name|invalidate
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

