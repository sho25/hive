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
name|hive
operator|.
name|service
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_comment
comment|/**  * CompositeService.  *  */
end_comment

begin_class
specifier|public
class|class
name|CompositeService
extends|extends
name|AbstractService
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
name|CompositeService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Service
argument_list|>
name|serviceList
init|=
operator|new
name|ArrayList
argument_list|<
name|Service
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|CompositeService
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Collection
argument_list|<
name|Service
argument_list|>
name|getServices
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|serviceList
argument_list|)
return|;
block|}
specifier|protected
specifier|synchronized
name|void
name|addService
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
name|serviceList
operator|.
name|add
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|boolean
name|removeService
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
return|return
name|serviceList
operator|.
name|remove
argument_list|(
name|service
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|init
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
for|for
control|(
name|Service
name|service
range|:
name|serviceList
control|)
block|{
name|service
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|n
init|=
name|serviceList
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|Service
name|service
init|=
name|serviceList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|service
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error starting services "
operator|+
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Note that the state of the failed service is still INITED and not
comment|// STARTED. Even though the last service is not started completely, still
comment|// call stop() on all services including failed service to make sure cleanup
comment|// happens.
name|stop
argument_list|(
name|i
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Failed to Start "
operator|+
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|getServiceState
argument_list|()
operator|==
name|STATE
operator|.
name|STOPPED
condition|)
block|{
comment|// The base composite-service is already stopped, don't do anything again.
return|return;
block|}
if|if
condition|(
name|serviceList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|stop
argument_list|(
name|serviceList
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|void
name|stop
parameter_list|(
name|int
name|numOfServicesStarted
parameter_list|)
block|{
comment|// stop in reserve order of start
for|for
control|(
name|int
name|i
init|=
name|numOfServicesStarted
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|Service
name|service
init|=
name|serviceList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Error stopping "
operator|+
name|service
operator|.
name|getName
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * JVM Shutdown hook for CompositeService which will stop the given    * CompositeService gracefully in case of JVM shutdown.    */
specifier|public
specifier|static
class|class
name|CompositeServiceShutdownHook
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|CompositeService
name|compositeService
decl_stmt|;
specifier|public
name|CompositeServiceShutdownHook
parameter_list|(
name|CompositeService
name|compositeService
parameter_list|)
block|{
name|this
operator|.
name|compositeService
operator|=
name|compositeService
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// Stop the Composite Service
name|compositeService
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Error stopping "
operator|+
name|compositeService
operator|.
name|getName
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

