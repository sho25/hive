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
operator|.
name|auth
package|;
end_package

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
name|hive
operator|.
name|service
operator|.
name|server
operator|.
name|HiveServer2
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

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

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

begin_class
specifier|public
class|class
name|TestCustomAuthentication
block|{
specifier|private
specifier|static
name|HiveServer2
name|hiveserver2
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|hiveConfBackup
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|writeXml
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
name|hiveConfBackup
operator|=
name|baos
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"hive.server2.authentication"
argument_list|,
literal|"CUSTOM"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"hive.server2.custom.authentication.class"
argument_list|,
literal|"org.apache.hive.service.auth.TestCustomAuthentication$SimpleAuthenticationProviderImpl"
argument_list|)
expr_stmt|;
name|FileOutputStream
name|fos
init|=
operator|new
name|FileOutputStream
argument_list|(
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
operator|.
name|toURI
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|writeXml
argument_list|(
name|fos
argument_list|)
expr_stmt|;
name|fos
operator|.
name|close
argument_list|()
expr_stmt|;
name|hiveserver2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveserver2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveserver2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"hiveServer2 start ......"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveConf
operator|!=
literal|null
operator|&&
name|hiveConfBackup
operator|!=
literal|null
condition|)
block|{
name|FileOutputStream
name|fos
init|=
operator|new
name|FileOutputStream
argument_list|(
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
operator|.
name|toURI
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|fos
operator|.
name|write
argument_list|(
name|hiveConfBackup
argument_list|)
expr_stmt|;
name|fos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hiveserver2
operator|!=
literal|null
condition|)
block|{
name|hiveserver2
operator|.
name|stop
argument_list|()
expr_stmt|;
name|hiveserver2
operator|=
literal|null
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"hiveServer2 stop ......"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCustomAuthentication
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|url
init|=
literal|"jdbc:hive2://localhost:10000/default"
decl_stmt|;
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hive.jdbc.HiveDriver"
argument_list|)
expr_stmt|;
try|try
block|{
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|,
literal|"wronguser"
argument_list|,
literal|"pwd"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Expected Exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Peer indicated failure: Error validating the login"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Connection
name|connection
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|,
literal|"hiveuser"
argument_list|,
literal|"hive"
argument_list|)
decl_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|">>> PASSED testCustomAuthentication"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SimpleAuthenticationProviderImpl
implements|implements
name|PasswdAuthenticationProvider
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|userMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|SimpleAuthenticationProviderImpl
parameter_list|()
block|{
name|init
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
name|userMap
operator|.
name|put
argument_list|(
literal|"hiveuser"
argument_list|,
literal|"hive"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|Authenticate
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
operator|!
name|userMap
operator|.
name|containsKey
argument_list|(
name|user
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Invalid user : "
operator|+
name|user
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|userMap
operator|.
name|get
argument_list|(
name|user
argument_list|)
operator|.
name|equals
argument_list|(
name|password
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Invalid passwd : "
operator|+
name|password
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

