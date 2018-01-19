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

begin_comment
comment|/**  * Since Hadoop ships with different versions of Jetty in different versions,  * Hive uses a shim layer to access the parts of the API that have changed.  * Users should obtain an instance of this class using the ShimLoader factory.  */
end_comment

begin_interface
specifier|public
interface|interface
name|JettyShims
block|{
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
function_decl|;
comment|/**    * Server.    *    */
interface|interface
name|Server
block|{
name|void
name|addWar
parameter_list|(
name|String
name|war
parameter_list|,
name|String
name|mount
parameter_list|)
function_decl|;
name|void
name|start
parameter_list|()
throws|throws
name|Exception
function_decl|;
name|void
name|join
parameter_list|()
throws|throws
name|InterruptedException
function_decl|;
name|void
name|stop
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
block|}
end_interface

end_unit

