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
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|shims
operator|.
name|JettyShims
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_comment
comment|/**  * TestHWIServer.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHWIServer
extends|extends
name|TestCase
block|{
specifier|public
name|TestHWIServer
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
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|testServerInit
parameter_list|()
throws|throws
name|Exception
block|{
name|StringBuilder
name|warFile
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"../build/hwi/hive-hwi-"
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// try retrieve version from build.properties file
try|try
block|{
name|props
operator|.
name|load
argument_list|(
operator|new
name|FileInputStream
argument_list|(
literal|"../build.properties"
argument_list|)
argument_list|)
expr_stmt|;
name|warFile
operator|.
name|append
argument_list|(
name|props
operator|.
name|getProperty
argument_list|(
literal|"version"
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|".war"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|JettyShims
operator|.
name|Server
name|webServer
decl_stmt|;
name|webServer
operator|=
name|ShimLoader
operator|.
name|getJettyShims
argument_list|()
operator|.
name|startServer
argument_list|(
literal|"0.0.0.0"
argument_list|,
literal|9999
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|webServer
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|addWar
argument_list|(
name|warFile
operator|.
name|toString
argument_list|()
argument_list|,
literal|"/hwi"
argument_list|)
expr_stmt|;
name|webServer
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// webServer.join();
name|webServer
operator|.
name|stop
argument_list|()
expr_stmt|;
assert|assert
operator|(
literal|true
operator|)
assert|;
block|}
block|}
end_class

end_unit

