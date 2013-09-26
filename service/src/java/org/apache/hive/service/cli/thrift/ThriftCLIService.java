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
name|net
operator|.
name|InetSocketAddress
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
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|hive
operator|.
name|service
operator|.
name|AbstractService
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
name|cli
operator|.
name|CLIService
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
name|FetchOrientation
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
name|GetInfoType
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
name|GetInfoValue
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
name|HiveSQLException
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
name|OperationHandle
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
name|OperationState
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
name|RowSet
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
name|SessionHandle
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
name|TableSchema
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
name|server
operator|.
name|TServer
import|;
end_import

begin_comment
comment|/**  * ThriftCLIService.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ThriftCLIService
extends|extends
name|AbstractService
implements|implements
name|TCLIService
operator|.
name|Iface
implements|,
name|Runnable
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
name|ThriftCLIService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|CLIService
name|cliService
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TStatus
name|OK_STATUS
init|=
operator|new
name|TStatus
argument_list|(
name|TStatusCode
operator|.
name|SUCCESS_STATUS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TStatus
name|ERROR_STATUS
init|=
operator|new
name|TStatus
argument_list|(
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|portNum
decl_stmt|;
specifier|protected
name|InetSocketAddress
name|serverAddress
decl_stmt|;
specifier|protected
name|TServer
name|server
decl_stmt|;
specifier|protected
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
name|httpServer
decl_stmt|;
specifier|private
name|boolean
name|isStarted
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|isEmbedded
init|=
literal|false
decl_stmt|;
specifier|protected
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
name|int
name|minWorkerThreads
decl_stmt|;
specifier|protected
name|int
name|maxWorkerThreads
decl_stmt|;
specifier|protected
specifier|static
name|HiveAuthFactory
name|hiveAuthFactory
decl_stmt|;
specifier|public
name|ThriftCLIService
parameter_list|(
name|CLIService
name|cliService
parameter_list|,
name|String
name|serviceName
parameter_list|)
block|{
name|super
argument_list|(
name|serviceName
argument_list|)
expr_stmt|;
name|this
operator|.
name|cliService
operator|=
name|cliService
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|init
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
name|super
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isStarted
operator|&&
operator|!
name|isEmbedded
condition|)
block|{
operator|new
name|Thread
argument_list|(
name|this
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|isStarted
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|isStarted
operator|&&
operator|!
name|isEmbedded
condition|)
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Thrift server has stopped"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|httpServer
operator|!=
literal|null
operator|)
operator|&&
name|httpServer
operator|.
name|isStarted
argument_list|()
condition|)
block|{
try|try
block|{
name|httpServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Http server has stopped"
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
name|error
argument_list|(
literal|"Error stopping Http server: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|isStarted
operator|=
literal|false
expr_stmt|;
block|}
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TOpenSessionResp
name|OpenSession
parameter_list|(
name|TOpenSessionReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TOpenSessionResp
name|resp
init|=
operator|new
name|TOpenSessionResp
argument_list|()
decl_stmt|;
try|try
block|{
name|SessionHandle
name|sessionHandle
init|=
name|getSessionHandle
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setSessionHandle
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: set real configuration map
name|resp
operator|.
name|setConfiguration
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
specifier|private
name|String
name|getUserName
parameter_list|(
name|TOpenSessionReq
name|req
parameter_list|)
block|{
if|if
condition|(
name|hiveAuthFactory
operator|!=
literal|null
operator|&&
name|hiveAuthFactory
operator|.
name|getRemoteUser
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|hiveAuthFactory
operator|.
name|getRemoteUser
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|req
operator|.
name|getUsername
argument_list|()
return|;
block|}
block|}
name|SessionHandle
name|getSessionHandle
parameter_list|(
name|TOpenSessionReq
name|req
parameter_list|)
throws|throws
name|HiveSQLException
throws|,
name|LoginException
throws|,
name|IOException
block|{
name|String
name|userName
init|=
name|getUserName
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|SessionHandle
name|sessionHandle
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|cliService
operator|.
name|getHiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|)
operator|.
name|equals
argument_list|(
name|HiveAuthFactory
operator|.
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|toString
argument_list|()
argument_list|)
operator|&&
name|cliService
operator|.
name|getHiveConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
condition|)
block|{
name|String
name|delegationTokenStr
init|=
literal|null
decl_stmt|;
try|try
block|{
name|delegationTokenStr
operator|=
name|cliService
operator|.
name|getDelegationTokenFromMetaStore
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// The delegation token is not applicable in the given deployment mode
block|}
name|sessionHandle
operator|=
name|cliService
operator|.
name|openSessionWithImpersonation
argument_list|(
name|userName
argument_list|,
name|req
operator|.
name|getPassword
argument_list|()
argument_list|,
name|req
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|delegationTokenStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sessionHandle
operator|=
name|cliService
operator|.
name|openSession
argument_list|(
name|userName
argument_list|,
name|req
operator|.
name|getPassword
argument_list|()
argument_list|,
name|req
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sessionHandle
return|;
block|}
annotation|@
name|Override
specifier|public
name|TCloseSessionResp
name|CloseSession
parameter_list|(
name|TCloseSessionReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TCloseSessionResp
name|resp
init|=
operator|new
name|TCloseSessionResp
argument_list|()
decl_stmt|;
try|try
block|{
name|SessionHandle
name|sessionHandle
init|=
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|cliService
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetInfoResp
name|GetInfo
parameter_list|(
name|TGetInfoReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetInfoResp
name|resp
init|=
operator|new
name|TGetInfoResp
argument_list|()
decl_stmt|;
try|try
block|{
name|GetInfoValue
name|getInfoValue
init|=
name|cliService
operator|.
name|getInfo
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|,
name|GetInfoType
operator|.
name|getGetInfoType
argument_list|(
name|req
operator|.
name|getInfoType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setInfoValue
argument_list|(
name|getInfoValue
operator|.
name|toTGetInfoValue
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TExecuteStatementResp
name|ExecuteStatement
parameter_list|(
name|TExecuteStatementReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TExecuteStatementResp
name|resp
init|=
operator|new
name|TExecuteStatementResp
argument_list|()
decl_stmt|;
try|try
block|{
name|SessionHandle
name|sessionHandle
init|=
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|statement
init|=
name|req
operator|.
name|getStatement
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
name|req
operator|.
name|getConfOverlay
argument_list|()
decl_stmt|;
name|Boolean
name|runAsync
init|=
name|req
operator|.
name|isRunAsync
argument_list|()
decl_stmt|;
name|OperationHandle
name|operationHandle
init|=
name|runAsync
condition|?
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
else|:
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
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|operationHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetTypeInfoResp
name|GetTypeInfo
parameter_list|(
name|TGetTypeInfoReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetTypeInfoResp
name|resp
init|=
operator|new
name|TGetTypeInfoResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|operationHandle
init|=
name|cliService
operator|.
name|getTypeInfo
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|operationHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetCatalogsResp
name|GetCatalogs
parameter_list|(
name|TGetCatalogsReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetCatalogsResp
name|resp
init|=
operator|new
name|TGetCatalogsResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getCatalogs
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetSchemasResp
name|GetSchemas
parameter_list|(
name|TGetSchemasReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetSchemasResp
name|resp
init|=
operator|new
name|TGetSchemasResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getSchemas
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|,
name|req
operator|.
name|getCatalogName
argument_list|()
argument_list|,
name|req
operator|.
name|getSchemaName
argument_list|()
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetTablesResp
name|GetTables
parameter_list|(
name|TGetTablesReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetTablesResp
name|resp
init|=
operator|new
name|TGetTablesResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getTables
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|,
name|req
operator|.
name|getCatalogName
argument_list|()
argument_list|,
name|req
operator|.
name|getSchemaName
argument_list|()
argument_list|,
name|req
operator|.
name|getTableName
argument_list|()
argument_list|,
name|req
operator|.
name|getTableTypes
argument_list|()
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetTableTypesResp
name|GetTableTypes
parameter_list|(
name|TGetTableTypesReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetTableTypesResp
name|resp
init|=
operator|new
name|TGetTableTypesResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getTableTypes
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetColumnsResp
name|GetColumns
parameter_list|(
name|TGetColumnsReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetColumnsResp
name|resp
init|=
operator|new
name|TGetColumnsResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getColumns
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|,
name|req
operator|.
name|getCatalogName
argument_list|()
argument_list|,
name|req
operator|.
name|getSchemaName
argument_list|()
argument_list|,
name|req
operator|.
name|getTableName
argument_list|()
argument_list|,
name|req
operator|.
name|getColumnName
argument_list|()
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetFunctionsResp
name|GetFunctions
parameter_list|(
name|TGetFunctionsReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetFunctionsResp
name|resp
init|=
operator|new
name|TGetFunctionsResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationHandle
name|opHandle
init|=
name|cliService
operator|.
name|getFunctions
argument_list|(
operator|new
name|SessionHandle
argument_list|(
name|req
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
argument_list|,
name|req
operator|.
name|getCatalogName
argument_list|()
argument_list|,
name|req
operator|.
name|getSchemaName
argument_list|()
argument_list|,
name|req
operator|.
name|getFunctionName
argument_list|()
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetOperationStatusResp
name|GetOperationStatus
parameter_list|(
name|TGetOperationStatusReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetOperationStatusResp
name|resp
init|=
operator|new
name|TGetOperationStatusResp
argument_list|()
decl_stmt|;
try|try
block|{
name|OperationState
name|operationState
init|=
name|cliService
operator|.
name|getOperationStatus
argument_list|(
operator|new
name|OperationHandle
argument_list|(
name|req
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setOperationState
argument_list|(
name|operationState
operator|.
name|toTOperationState
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TCancelOperationResp
name|CancelOperation
parameter_list|(
name|TCancelOperationReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TCancelOperationResp
name|resp
init|=
operator|new
name|TCancelOperationResp
argument_list|()
decl_stmt|;
try|try
block|{
name|cliService
operator|.
name|cancelOperation
argument_list|(
operator|new
name|OperationHandle
argument_list|(
name|req
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TCloseOperationResp
name|CloseOperation
parameter_list|(
name|TCloseOperationReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TCloseOperationResp
name|resp
init|=
operator|new
name|TCloseOperationResp
argument_list|()
decl_stmt|;
try|try
block|{
name|cliService
operator|.
name|closeOperation
argument_list|(
operator|new
name|OperationHandle
argument_list|(
name|req
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TGetResultSetMetadataResp
name|GetResultSetMetadata
parameter_list|(
name|TGetResultSetMetadataReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TGetResultSetMetadataResp
name|resp
init|=
operator|new
name|TGetResultSetMetadataResp
argument_list|()
decl_stmt|;
try|try
block|{
name|TableSchema
name|schema
init|=
name|cliService
operator|.
name|getResultSetMetadata
argument_list|(
operator|new
name|OperationHandle
argument_list|(
name|req
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setSchema
argument_list|(
name|schema
operator|.
name|toTTableSchema
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
name|TFetchResultsResp
name|FetchResults
parameter_list|(
name|TFetchResultsReq
name|req
parameter_list|)
throws|throws
name|TException
block|{
name|TFetchResultsResp
name|resp
init|=
operator|new
name|TFetchResultsResp
argument_list|()
decl_stmt|;
try|try
block|{
name|RowSet
name|rowSet
init|=
name|cliService
operator|.
name|fetchResults
argument_list|(
operator|new
name|OperationHandle
argument_list|(
name|req
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
argument_list|,
name|FetchOrientation
operator|.
name|getFetchOrientation
argument_list|(
name|req
operator|.
name|getOrientation
argument_list|()
argument_list|)
argument_list|,
name|req
operator|.
name|getMaxRows
argument_list|()
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setResults
argument_list|(
name|rowSet
operator|.
name|toTRowSet
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setHasMoreRows
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|OK_STATUS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|HiveSQLException
operator|.
name|toTStatus
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|void
name|run
parameter_list|()
function_decl|;
block|}
end_class

end_unit

