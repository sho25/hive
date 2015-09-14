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
operator|.
name|thrift
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|auth
operator|.
name|HiveAuthFactory
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
name|KerberosSaslHelper
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
name|PlainSaslHelper
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
name|*
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
name|TApplicationException
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
name|protocol
operator|.
name|TBinaryProtocol
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
name|protocol
operator|.
name|TProtocol
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
name|protocol
operator|.
name|TProtocolException
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
name|TSocket
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketException
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * RetryingThriftCLIServiceClient. Creates a proxy for a CLIServiceClient  * implementation and retries calls to it on failure.  */
end_comment

begin_class
specifier|public
class|class
name|RetryingThriftCLIServiceClient
implements|implements
name|InvocationHandler
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RetryingThriftCLIServiceClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ThriftCLIServiceClient
name|base
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryLimit
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryDelaySeconds
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|public
specifier|static
class|class
name|CLIServiceClientWrapper
extends|extends
name|CLIServiceClient
block|{
specifier|private
specifier|final
name|ICLIService
name|cliService
decl_stmt|;
specifier|public
name|CLIServiceClientWrapper
parameter_list|(
name|ICLIService
name|icliService
parameter_list|)
block|{
name|cliService
operator|=
name|icliService
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SessionHandle
name|openSession
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|openSession
argument_list|(
name|username
argument_list|,
name|password
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDelegationToken
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|owner
parameter_list|,
name|String
name|renewer
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getDelegationToken
argument_list|(
name|sessionHandle
argument_list|,
name|authFactory
argument_list|,
name|owner
argument_list|,
name|renewer
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancelDelegationToken
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|tokenStr
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|cliService
operator|.
name|cancelDelegationToken
argument_list|(
name|sessionHandle
argument_list|,
name|authFactory
argument_list|,
name|tokenStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|renewDelegationToken
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|tokenStr
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|cliService
operator|.
name|renewDelegationToken
argument_list|(
name|sessionHandle
argument_list|,
name|authFactory
argument_list|,
name|tokenStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SessionHandle
name|openSession
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|openSession
argument_list|(
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SessionHandle
name|openSessionWithImpersonation
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
parameter_list|,
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|openSessionWithImpersonation
argument_list|(
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|,
name|delegationToken
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeSession
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|cliService
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|GetInfoValue
name|getInfo
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|GetInfoType
name|getInfoType
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getInfo
argument_list|(
name|sessionHandle
argument_list|,
name|getInfoType
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|executeStatement
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|statement
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|executeStatementAsync
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|statement
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|executeStatementAsync
argument_list|(
name|sessionHandle
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getTypeInfo
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getTypeInfo
argument_list|(
name|sessionHandle
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getCatalogs
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getCatalogs
argument_list|(
name|sessionHandle
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getSchemas
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getSchemas
argument_list|(
name|sessionHandle
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getTables
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableTypes
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getTables
argument_list|(
name|sessionHandle
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|tableTypes
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getTableTypes
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getColumns
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getColumns
argument_list|(
name|sessionHandle
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|columnName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationHandle
name|getFunctions
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|,
name|String
name|functionName
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getFunctions
argument_list|(
name|sessionHandle
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|functionName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperationStatus
name|getOperationStatus
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getOperationStatus
argument_list|(
name|opHandle
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancelOperation
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|cliService
operator|.
name|cancelOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOperation
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|cliService
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableSchema
name|getResultSetMetadata
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|getResultSetMetadata
argument_list|(
name|opHandle
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RowSet
name|fetchResults
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|,
name|FetchOrientation
name|orientation
parameter_list|,
name|long
name|maxRows
parameter_list|,
name|FetchType
name|fetchType
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|cliService
operator|.
name|fetchResults
argument_list|(
name|opHandle
argument_list|,
name|orientation
argument_list|,
name|maxRows
argument_list|,
name|fetchType
argument_list|)
return|;
block|}
block|}
specifier|protected
name|RetryingThriftCLIServiceClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|retryLimit
operator|=
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
expr_stmt|;
name|retryDelaySeconds
operator|=
operator|(
name|int
operator|)
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_RETRY_DELAY_SECONDS
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|CLIServiceClient
name|newRetryingCLIServiceClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|RetryingThriftCLIServiceClient
name|retryClient
init|=
operator|new
name|RetryingThriftCLIServiceClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|retryClient
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
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|)
argument_list|)
expr_stmt|;
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
name|RetryingThriftCLIServiceClient
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
name|retryClient
argument_list|)
decl_stmt|;
return|return
operator|new
name|CLIServiceClientWrapper
argument_list|(
name|cliService
argument_list|)
return|;
block|}
specifier|protected
name|void
name|connectWithRetry
parameter_list|(
name|int
name|retries
parameter_list|)
throws|throws
name|HiveSQLException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|connect
argument_list|(
name|conf
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|e
parameter_list|)
block|{
if|if
condition|(
name|i
operator|+
literal|1
operator|==
name|retries
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Unable to connect after "
operator|+
name|retries
operator|+
literal|" retries"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Connection attempt "
operator|+
name|i
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|retryDelaySeconds
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
if|if
condition|(
name|transport
operator|!=
literal|null
operator|&&
name|transport
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|String
name|host
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_PORT
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Connecting to "
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
argument_list|)
expr_stmt|;
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TSocket
operator|)
name|transport
operator|)
operator|.
name|setTimeout
argument_list|(
operator|(
name|int
operator|)
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SERVER_READ_SOCKET_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|*
literal|1000
argument_list|)
expr_stmt|;
try|try
block|{
operator|(
operator|(
name|TSocket
operator|)
name|transport
operator|)
operator|.
name|getSocket
argument_list|()
operator|.
name|setKeepAlive
argument_list|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SERVER_TCP_KEEP_ALIVE
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error setting keep alive to "
operator|+
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SERVER_TCP_KEEP_ALIVE
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|String
name|userName
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_USER
argument_list|)
decl_stmt|;
name|String
name|passwd
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_CLIENT_PASSWORD
argument_list|)
decl_stmt|;
try|try
block|{
name|transport
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransport
argument_list|(
name|userName
argument_list|,
name|passwd
argument_list|,
name|transport
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SaslException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error creating plain SASL transport"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|base
operator|=
operator|new
name|ThriftCLIServiceClient
argument_list|(
operator|new
name|TCLIService
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Connected!"
argument_list|)
expr_stmt|;
return|return
name|transport
return|;
block|}
specifier|protected
class|class
name|InvocationResult
block|{
specifier|final
name|boolean
name|success
decl_stmt|;
specifier|final
name|Object
name|result
decl_stmt|;
specifier|final
name|Throwable
name|exception
decl_stmt|;
name|InvocationResult
parameter_list|(
name|boolean
name|success
parameter_list|,
name|Object
name|result
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|this
operator|.
name|success
operator|=
name|success
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|this
operator|.
name|exception
operator|=
name|exception
expr_stmt|;
block|}
block|}
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
name|InvocationResult
name|result
decl_stmt|;
try|try
block|{
name|Object
name|methodResult
init|=
name|method
operator|.
name|invoke
argument_list|(
name|base
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|result
operator|=
operator|new
name|InvocationResult
argument_list|(
literal|true
argument_list|,
name|methodResult
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UndeclaredThrowableException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|HiveSQLException
condition|)
block|{
name|HiveSQLException
name|hiveExc
init|=
operator|(
name|HiveSQLException
operator|)
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|Throwable
name|cause
init|=
name|hiveExc
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|cause
operator|instanceof
name|TApplicationException
operator|)
operator|||
operator|(
name|cause
operator|instanceof
name|TProtocolException
operator|)
operator|||
operator|(
name|cause
operator|instanceof
name|TTransportException
operator|)
condition|)
block|{
name|result
operator|=
operator|new
name|InvocationResult
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|,
name|hiveExc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|hiveExc
throw|;
block|}
block|}
else|else
block|{
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|o
parameter_list|,
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
name|int
name|attempts
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|attempts
operator|++
expr_stmt|;
name|InvocationResult
name|invokeResult
init|=
name|invokeInternal
argument_list|(
name|method
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|invokeResult
operator|.
name|success
condition|)
block|{
return|return
name|invokeResult
operator|.
name|result
return|;
block|}
comment|// Error because of thrift client, we have to recreate base object
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
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|attempts
operator|>=
name|retryLimit
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" failed after "
operator|+
name|attempts
operator|+
literal|" retries."
argument_list|,
name|invokeResult
operator|.
name|exception
argument_list|)
expr_stmt|;
throw|throw
name|invokeResult
operator|.
name|exception
throw|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Last call ThriftCLIServiceClient."
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" failed, attempts = "
operator|+
name|attempts
argument_list|,
name|invokeResult
operator|.
name|exception
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|retryDelaySeconds
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getRetryLimit
parameter_list|()
block|{
return|return
name|retryLimit
return|;
block|}
specifier|public
name|int
name|getRetryDelaySeconds
parameter_list|()
block|{
return|return
name|retryDelaySeconds
return|;
block|}
block|}
end_class

end_unit

