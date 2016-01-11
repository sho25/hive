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
operator|.
name|server
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestHS2HttpServer -- executes tests of HiveServer2 HTTP Server  */
end_comment

begin_class
specifier|public
class|class
name|TestHS2HttpServer
block|{
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTests
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStackServket
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|baseURL
init|=
literal|"http://localhost:"
operator|+
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|getDefaultValue
argument_list|()
operator|+
literal|"/stacks"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseURL
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_OK
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|conn
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|contents
init|=
literal|false
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"Process Thread Dump:"
argument_list|)
condition|)
block|{
name|contents
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|contents
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTests
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

