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
name|cli
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
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|Service
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
name|cli
operator|.
name|session
operator|.
name|HiveSession
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
name|cli
operator|.
name|thrift
operator|.
name|RetryingThriftCLIServiceClient
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
name|cli
operator|.
name|thrift
operator|.
name|ThriftCLIService
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
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
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
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Test CLI service with a retrying client. All tests should pass. This is to validate that calls  * are transferred successfully.  */
end_comment

begin_class
specifier|public
class|class
name|TestRetryingThriftCLIServiceClient
block|{
specifier|protected
specifier|static
name|ThriftCLIService
name|service
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveServer2
name|server
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|init
parameter_list|()
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|,
literal|15000
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
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
name|HiveConf
operator|.
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|,
literal|"binary"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_RETRY_LIMIT
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_THREADS
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startHiveServer
parameter_list|()
throws|throws
name|InterruptedException
block|{
comment|// Start hive server2
name|server
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|server
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|server
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"## HiveServer started"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|stopHiveServer
parameter_list|()
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
comment|// kill server
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|RetryingThriftCLIServiceClientTest
extends|extends
name|RetryingThriftCLIServiceClient
block|{
name|int
name|callCount
init|=
literal|0
decl_stmt|;
name|int
name|connectCount
init|=
literal|0
decl_stmt|;
specifier|static
name|RetryingThriftCLIServiceClientTest
name|handlerInst
decl_stmt|;
specifier|protected
name|RetryingThriftCLIServiceClientTest
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|CLIServiceClientWrapper
name|newRetryingCLIServiceClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|handlerInst
operator|=
operator|new
name|RetryingThriftCLIServiceClientTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TTransport
name|tTransport
init|=
name|handlerInst
operator|.
name|connectWithRetry
argument_list|(
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_RETRY_LIMIT
argument_list|)
argument_list|)
decl_stmt|;
name|ICLIService
name|cliService
init|=
operator|(
name|ICLIService
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|RetryingThriftCLIServiceClientTest
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
argument_list|,
name|CLIServiceClient
operator|.
name|class
operator|.
name|getInterfaces
argument_list|()
argument_list|,
name|handlerInst
argument_list|)
decl_stmt|;
return|return
operator|new
name|CLIServiceClientWrapper
argument_list|(
name|cliService
argument_list|,
name|tTransport
argument_list|,
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|InvocationResult
name|invokeInternal
parameter_list|(
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"## Calling: "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|", "
operator|+
name|callCount
operator|+
literal|"/"
operator|+
name|getRetryLimit
argument_list|()
argument_list|)
expr_stmt|;
name|callCount
operator|++
expr_stmt|;
return|return
name|super
operator|.
name|invokeInternal
argument_list|(
name|method
argument_list|,
name|args
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|TTransport
name|connect
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveSQLException
throws|,
name|TTransportException
block|{
name|connectCount
operator|++
expr_stmt|;
return|return
name|super
operator|.
name|connect
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRetryBehaviour
parameter_list|()
throws|throws
name|Exception
block|{
name|startHiveServer
argument_list|()
expr_stmt|;
comment|// Check if giving invalid address causes retry in connection attempt
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|,
literal|17000
argument_list|)
expr_stmt|;
try|try
block|{
name|RetryingThriftCLIServiceClientTest
operator|.
name|newRetryingCLIServiceClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected to throw exception for invalid port"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|sqlExc
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|sqlExc
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TTransportException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|sqlExc
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Reset port setting
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|,
literal|15000
argument_list|)
expr_stmt|;
comment|// Create client
name|RetryingThriftCLIServiceClient
operator|.
name|CLIServiceClientWrapper
name|cliServiceClient
init|=
name|RetryingThriftCLIServiceClientTest
operator|.
name|newRetryingCLIServiceClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"## Created client"
argument_list|)
expr_stmt|;
name|stopHiveServer
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// submit few queries
try|try
block|{
name|RetryingThriftCLIServiceClientTest
operator|.
name|handlerInst
operator|.
name|callCount
operator|=
literal|0
expr_stmt|;
name|RetryingThriftCLIServiceClientTest
operator|.
name|handlerInst
operator|.
name|connectCount
operator|=
literal|0
expr_stmt|;
name|cliServiceClient
operator|.
name|openSession
argument_list|(
literal|"anonymous"
argument_list|,
literal|"anonymous"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|exc
parameter_list|)
block|{
name|exc
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|exc
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|RetryingThriftCLIServiceClientTest
operator|.
name|handlerInst
operator|.
name|callCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|RetryingThriftCLIServiceClientTest
operator|.
name|handlerInst
operator|.
name|connectCount
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cliServiceClient
operator|.
name|closeTransport
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTransportClose
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|HiveSQLException
block|{
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|,
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
name|startHiveServer
argument_list|()
expr_stmt|;
name|RetryingThriftCLIServiceClient
operator|.
name|CLIServiceClientWrapper
name|client
init|=
name|RetryingThriftCLIServiceClientTest
operator|.
name|newRetryingCLIServiceClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|client
operator|.
name|closeTransport
argument_list|()
expr_stmt|;
try|try
block|{
name|client
operator|.
name|openSession
argument_list|(
literal|"anonymous"
argument_list|,
literal|"anonymous"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be able to open session when transport is closed."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|ignored
parameter_list|)
block|{        }
block|}
finally|finally
block|{
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|stopHiveServer
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionLifeAfterTransportClose
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|HiveSQLException
block|{
try|try
block|{
name|startHiveServer
argument_list|()
expr_stmt|;
name|CLIService
name|service
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Service
name|s
range|:
name|server
operator|.
name|getServices
argument_list|()
control|)
block|{
if|if
condition|(
name|s
operator|instanceof
name|CLIService
condition|)
block|{
name|service
operator|=
operator|(
name|CLIService
operator|)
name|s
expr_stmt|;
block|}
block|}
if|if
condition|(
name|service
operator|==
literal|null
condition|)
block|{
name|service
operator|=
operator|new
name|CLIService
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|RetryingThriftCLIServiceClient
operator|.
name|CLIServiceClientWrapper
name|client
init|=
name|RetryingThriftCLIServiceClientTest
operator|.
name|newRetryingCLIServiceClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|conf
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_CLOSE_SESSION_ON_DISCONNECT
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|"anonymous"
argument_list|,
literal|"anonymous"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
name|HiveSession
name|session
init|=
name|service
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
decl_stmt|;
name|OperationHandle
name|op1
init|=
name|session
operator|.
name|executeStatementAsync
argument_list|(
literal|"show databases"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|op1
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeTransport
argument_list|()
expr_stmt|;
comment|// Verify that session wasn't closed on transport close.
name|assertEquals
argument_list|(
name|session
argument_list|,
name|service
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
argument_list|)
expr_stmt|;
comment|// Should be able to execute without failure in the session whose transport has been closed.
name|OperationHandle
name|op2
init|=
name|session
operator|.
name|executeStatementAsync
argument_list|(
literal|"show databases"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|op2
argument_list|)
expr_stmt|;
comment|// Make new client, since transport was closed for the last one.
name|client
operator|=
name|RetryingThriftCLIServiceClientTest
operator|.
name|newRetryingCLIServiceClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
comment|// operations will be lost once owning session is closed.
for|for
control|(
name|OperationHandle
name|op
range|:
operator|new
name|OperationHandle
index|[]
block|{
name|op1
block|,
name|op2
block|}
control|)
block|{
try|try
block|{
name|client
operator|.
name|getOperationStatus
argument_list|(
name|op
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have failed."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|ignored
parameter_list|)
block|{          }
block|}
block|}
finally|finally
block|{
name|stopHiveServer
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

