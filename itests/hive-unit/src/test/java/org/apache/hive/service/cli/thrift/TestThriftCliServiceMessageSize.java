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
name|cli
operator|.
name|thrift
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|Statement
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreTestUtils
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
name|auth
operator|.
name|HiveAuthConstants
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
name|After
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
name|Before
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

begin_class
specifier|public
class|class
name|TestThriftCliServiceMessageSize
block|{
specifier|protected
specifier|static
name|int
name|port
decl_stmt|;
specifier|protected
specifier|static
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
specifier|protected
specifier|static
name|HiveServer2
name|hiveServer2
decl_stmt|;
specifier|protected
specifier|static
name|ThriftCLIServiceClient
name|client
decl_stmt|;
specifier|protected
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
specifier|static
name|String
name|USERNAME
init|=
literal|"anonymous"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|PASSWORD
init|=
literal|"anonymous"
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Find a free port
name|port
operator|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|protected
specifier|static
name|void
name|startHiveServer2WithConf
parameter_list|(
name|HiveServer2
name|hiveServer2
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
comment|// Start HiveServer2 with given config
comment|// Fail if server doesn't start
try|try
block|{
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
comment|// Wait for startup to complete
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HiveServer2 started on port "
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|stopHiveServer2
parameter_list|(
name|HiveServer2
name|hiveServer2
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveServer2
operator|!=
literal|null
condition|)
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{   }
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{    }
annotation|@
name|Test
specifier|public
name|void
name|testMessageSize
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|transportMode
init|=
literal|"binary"
decl_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|,
name|HiveAuthConstants
operator|.
name|AuthTypes
operator|.
name|NONE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|,
name|transportMode
argument_list|)
expr_stmt|;
name|HiveServer2
name|hiveServer2
init|=
operator|new
name|HiveServer2
argument_list|()
decl_stmt|;
name|String
name|url
init|=
literal|"jdbc:hive2://localhost:"
operator|+
name|port
operator|+
literal|"/default"
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
comment|// First start HS2 with high message size limit. This should allow connections
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_MESSAGE_SIZE
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|startHiveServer2WithConf
argument_list|(
name|hiveServer2
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Started Thrift CLI service with message size limit "
operator|+
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_MESSAGE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
comment|// With the high message size limit this connection should work
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
name|Statement
name|stmt
init|=
name|connection
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Statement is null"
argument_list|,
name|stmt
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"set hive.support.concurrency = false"
argument_list|)
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopHiveServer2
argument_list|(
name|hiveServer2
argument_list|)
expr_stmt|;
comment|// Now start HS2 with low message size limit. This should prevent any connections
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_MESSAGE_SIZE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|startHiveServer2WithConf
argument_list|(
name|hiveServer2
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Started Thrift CLI service with message size limit "
operator|+
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_MESSAGE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|Exception
name|caughtException
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// This should fail
name|connection
operator|=
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
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|caughtException
operator|=
name|err
expr_stmt|;
block|}
comment|// Verify we hit an error while connecting
name|assertNotNull
argument_list|(
name|caughtException
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stopHiveServer2
argument_list|(
name|hiveServer2
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

