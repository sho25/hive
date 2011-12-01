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
name|shims
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|bio
operator|.
name|SocketConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|handler
operator|.
name|RequestLogHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|webapp
operator|.
name|WebAppContext
import|;
end_import

begin_comment
comment|/**  * Jetty23Shims.  *  */
end_comment

begin_class
specifier|public
class|class
name|Jetty23Shims
implements|implements
name|JettyShims
block|{
specifier|public
name|Server
name|startServer
parameter_list|(
name|String
name|listen
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|Server
name|s
init|=
operator|new
name|Server
argument_list|()
decl_stmt|;
name|s
operator|.
name|setupListenerHostPort
argument_list|(
name|listen
argument_list|,
name|port
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|private
specifier|static
class|class
name|Server
extends|extends
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
implements|implements
name|JettyShims
operator|.
name|Server
block|{
specifier|public
name|void
name|addWar
parameter_list|(
name|String
name|war
parameter_list|,
name|String
name|contextPath
parameter_list|)
block|{
name|WebAppContext
name|wac
init|=
operator|new
name|WebAppContext
argument_list|()
decl_stmt|;
name|wac
operator|.
name|setContextPath
argument_list|(
name|contextPath
argument_list|)
expr_stmt|;
name|wac
operator|.
name|setWar
argument_list|(
name|war
argument_list|)
expr_stmt|;
name|RequestLogHandler
name|rlh
init|=
operator|new
name|RequestLogHandler
argument_list|()
decl_stmt|;
name|rlh
operator|.
name|setHandler
argument_list|(
name|wac
argument_list|)
expr_stmt|;
name|this
operator|.
name|addHandler
argument_list|(
name|rlh
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setupListenerHostPort
parameter_list|(
name|String
name|listen
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
name|SocketConnector
name|connector
init|=
operator|new
name|SocketConnector
argument_list|()
decl_stmt|;
name|connector
operator|.
name|setPort
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|connector
operator|.
name|setHost
argument_list|(
name|listen
argument_list|)
expr_stmt|;
name|this
operator|.
name|addConnector
argument_list|(
name|connector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

