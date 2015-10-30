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
name|hwi
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletContextEvent
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
comment|/**  * After getting a contextInitialized event this component starts an instance of  * the HiveSessionManager.  *   */
end_comment

begin_class
specifier|public
class|class
name|HWIContextListener
implements|implements
name|javax
operator|.
name|servlet
operator|.
name|ServletContextListener
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|l4j
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HWIContextListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * The Hive Web Interface manages multiple hive sessions. This event is used    * to start a Runnable, HiveSessionManager as a thread inside the servlet    * container.    *     * @param sce    *          An event fired by the servlet context on startup    */
specifier|public
name|void
name|contextInitialized
parameter_list|(
name|ServletContextEvent
name|sce
parameter_list|)
block|{
name|ServletContext
name|sc
init|=
name|sce
operator|.
name|getServletContext
argument_list|()
decl_stmt|;
name|HWISessionManager
name|hs
init|=
operator|new
name|HWISessionManager
argument_list|()
decl_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionManager created."
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|hs
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionManager thread started."
argument_list|)
expr_stmt|;
name|sc
operator|.
name|setAttribute
argument_list|(
literal|"hs"
argument_list|,
name|hs
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionManager placed in application context."
argument_list|)
expr_stmt|;
block|}
comment|/**    * When the Hive Web Interface is closing we locate the Runnable    * HiveSessionManager and set it's internal goOn variable to false. This    * should allow the application to gracefully shutdown.    *     * @param sce    *          An event fired by the servlet context on context shutdown    */
specifier|public
name|void
name|contextDestroyed
parameter_list|(
name|ServletContextEvent
name|sce
parameter_list|)
block|{
name|ServletContext
name|sc
init|=
name|sce
operator|.
name|getServletContext
argument_list|()
decl_stmt|;
name|HWISessionManager
name|hs
init|=
operator|(
name|HWISessionManager
operator|)
name|sc
operator|.
name|getAttribute
argument_list|(
literal|"hs"
argument_list|)
decl_stmt|;
if|if
condition|(
name|hs
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"HWISessionManager was not found in context"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"HWISessionManager goOn set to false. Shutting down."
argument_list|)
expr_stmt|;
name|hs
operator|.
name|setGoOn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

