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
name|hive
operator|.
name|service
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
comment|/**  * ServiceOperations.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ServiceOperations
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
name|AbstractService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ServiceOperations
parameter_list|()
block|{   }
comment|/**    * Verify that that a service is in a given state.    * @param state the actual state a service is in    * @param expectedState the desired state    * @throws IllegalStateException if the service state is different from    * the desired state    */
specifier|public
specifier|static
name|void
name|ensureCurrentState
parameter_list|(
name|Service
operator|.
name|STATE
name|state
parameter_list|,
name|Service
operator|.
name|STATE
name|expectedState
parameter_list|)
block|{
if|if
condition|(
name|state
operator|!=
name|expectedState
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"For this operation, the "
operator|+
literal|"current service state must be "
operator|+
name|expectedState
operator|+
literal|" instead of "
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
comment|/**    * Initialize a service.    *<br>    * The service state is checked<i>before</i> the operation begins.    * This process is<i>not</i> thread safe.    * @param service a service that must be in the state    *   {@link Service.STATE#NOTINITED}    * @param configuration the configuration to initialize the service with    * @throws RuntimeException on a state change failure    * @throws IllegalStateException if the service is in the wrong state    */
specifier|public
specifier|static
name|void
name|init
parameter_list|(
name|Service
name|service
parameter_list|,
name|HiveConf
name|configuration
parameter_list|)
block|{
name|Service
operator|.
name|STATE
name|state
init|=
name|service
operator|.
name|getServiceState
argument_list|()
decl_stmt|;
name|ensureCurrentState
argument_list|(
name|state
argument_list|,
name|Service
operator|.
name|STATE
operator|.
name|NOTINITED
argument_list|)
expr_stmt|;
name|service
operator|.
name|init
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
comment|/**    * Start a service.    *<br>    * The service state is checked<i>before</i> the operation begins.    * This process is<i>not</i> thread safe.    * @param service a service that must be in the state    *   {@link Service.STATE#INITED}    * @throws RuntimeException on a state change failure    * @throws IllegalStateException if the service is in the wrong state    */
specifier|public
specifier|static
name|void
name|start
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
name|Service
operator|.
name|STATE
name|state
init|=
name|service
operator|.
name|getServiceState
argument_list|()
decl_stmt|;
name|ensureCurrentState
argument_list|(
name|state
argument_list|,
name|Service
operator|.
name|STATE
operator|.
name|INITED
argument_list|)
expr_stmt|;
name|service
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Initialize then start a service.    *<br>    * The service state is checked<i>before</i> the operation begins.    * This process is<i>not</i> thread safe.    * @param service a service that must be in the state    *   {@link Service.STATE#NOTINITED}    * @param configuration the configuration to initialize the service with    * @throws RuntimeException on a state change failure    * @throws IllegalStateException if the service is in the wrong state    */
specifier|public
specifier|static
name|void
name|deploy
parameter_list|(
name|Service
name|service
parameter_list|,
name|HiveConf
name|configuration
parameter_list|)
block|{
name|init
argument_list|(
name|service
argument_list|,
name|configuration
argument_list|)
expr_stmt|;
name|start
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop a service.    *<br>Do nothing if the service is null or not    * in a state in which it can be/needs to be stopped.    *<br>    * The service state is checked<i>before</i> the operation begins.    * This process is<i>not</i> thread safe.    * @param service a service or null    */
specifier|public
specifier|static
name|void
name|stop
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
if|if
condition|(
name|service
operator|!=
literal|null
condition|)
block|{
name|Service
operator|.
name|STATE
name|state
init|=
name|service
operator|.
name|getServiceState
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
operator|==
name|Service
operator|.
name|STATE
operator|.
name|STARTED
condition|)
block|{
name|service
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Stop a service; if it is null do nothing. Exceptions are caught and    * logged at warn level. (but not Throwables). This operation is intended to    * be used in cleanup operations    *    * @param service a service; may be null    * @return any exception that was caught; null if none was.    */
specifier|public
specifier|static
name|Exception
name|stopQuietly
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
try|try
block|{
name|stop
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"When stopping the service "
operator|+
name|service
operator|.
name|getName
argument_list|()
operator|+
literal|" : "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|e
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

