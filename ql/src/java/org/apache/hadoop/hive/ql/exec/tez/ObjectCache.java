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
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ObjectRegistry
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * ObjectCache. Tez implementation based on the tez object registry.  *  */
end_comment

begin_class
specifier|public
class|class
name|ObjectCache
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ObjectCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// ObjectRegistry is available via the Input/Output/ProcessorContext.
comment|// This is setup as part of the Tez Processor construction, so that it is available whenever an
comment|// instance of the ObjectCache is created. The assumption is that Tez will initialize the Processor
comment|// before anything else.
specifier|private
specifier|volatile
specifier|static
name|ObjectRegistry
name|staticRegistry
decl_stmt|;
specifier|private
specifier|static
name|ExecutorService
name|staticPool
decl_stmt|;
specifier|private
specifier|final
name|ObjectRegistry
name|registry
decl_stmt|;
specifier|public
name|ObjectCache
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|staticRegistry
argument_list|,
literal|"Object registry not setup yet. This should have been setup by the TezProcessor"
argument_list|)
expr_stmt|;
name|registry
operator|=
name|staticRegistry
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setupObjectRegistry
parameter_list|(
name|ObjectRegistry
name|objectRegistry
parameter_list|)
block|{
name|staticRegistry
operator|=
name|objectRegistry
expr_stmt|;
name|staticPool
operator|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
expr_stmt|;
block|}
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
comment|// nothing to do
name|LOG
operator|.
name|info
argument_list|(
literal|"Releasing key: "
operator|+
name|key
argument_list|)
expr_stmt|;
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
parameter_list|)
throws|throws
name|HiveException
block|{
name|T
name|value
init|=
literal|null
decl_stmt|;
try|try
block|{
name|value
operator|=
operator|(
name|T
operator|)
name|registry
operator|.
name|get
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|key
operator|+
literal|" in cache with value: "
operator|+
name|value
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
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|value
return|;
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
decl_stmt|;
try|try
block|{
name|value
operator|=
operator|(
name|T
operator|)
name|registry
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|fn
operator|.
name|call
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Caching key: "
operator|+
name|key
argument_list|)
expr_stmt|;
name|registry
operator|.
name|cacheForVertex
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|key
operator|+
literal|" in cache with value: "
operator|+
name|value
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
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
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
name|delete
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

