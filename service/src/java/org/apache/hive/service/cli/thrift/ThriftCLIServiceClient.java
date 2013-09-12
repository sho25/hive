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
name|CLIServiceClient
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

begin_comment
comment|/**  * ThriftCLIServiceClient.  *  */
end_comment

begin_class
specifier|public
class|class
name|ThriftCLIServiceClient
extends|extends
name|CLIServiceClient
block|{
specifier|private
specifier|final
name|TCLIService
operator|.
name|Iface
name|cliService
decl_stmt|;
specifier|public
name|ThriftCLIServiceClient
parameter_list|(
name|TCLIService
operator|.
name|Iface
name|cliService
parameter_list|)
block|{
name|this
operator|.
name|cliService
operator|=
name|cliService
expr_stmt|;
block|}
specifier|public
name|void
name|checkStatus
parameter_list|(
name|TStatus
name|status
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|TStatusCode
operator|.
name|ERROR_STATUS
operator|.
name|equals
argument_list|(
name|status
operator|.
name|getStatusCode
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|status
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#openSession(java.lang.String, java.lang.String, java.util.Map)    */
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
try|try
block|{
name|TOpenSessionReq
name|req
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|req
operator|.
name|setUsername
argument_list|(
name|username
argument_list|)
expr_stmt|;
name|req
operator|.
name|setPassword
argument_list|(
name|password
argument_list|)
expr_stmt|;
name|req
operator|.
name|setConfiguration
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|TOpenSessionResp
name|resp
init|=
name|cliService
operator|.
name|OpenSession
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|SessionHandle
argument_list|(
name|resp
operator|.
name|getSessionHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#closeSession(org.apache.hive.service.cli.SessionHandle)    */
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
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"open with impersonation operation is not supported in the client"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#closeSession(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TCloseSessionReq
name|req
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TCloseSessionResp
name|resp
init|=
name|cliService
operator|.
name|CloseSession
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getInfo(org.apache.hive.service.cli.SessionHandle, java.util.List)    */
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
name|infoType
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
comment|// FIXME extract the right info type
name|TGetInfoReq
name|req
init|=
operator|new
name|TGetInfoReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|,
name|infoType
operator|.
name|toTGetInfoType
argument_list|()
argument_list|)
decl_stmt|;
name|TGetInfoResp
name|resp
init|=
name|cliService
operator|.
name|GetInfo
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|GetInfoValue
argument_list|(
name|resp
operator|.
name|getInfoValue
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#executeStatement(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.util.Map)    */
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
name|executeStatementInternal
argument_list|(
name|sessionHandle
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#executeStatementAsync(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.util.Map)    */
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
name|executeStatementInternal
argument_list|(
name|sessionHandle
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|OperationHandle
name|executeStatementInternal
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
parameter_list|,
name|boolean
name|isAsync
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|TExecuteStatementReq
name|req
init|=
operator|new
name|TExecuteStatementReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|,
name|statement
argument_list|)
decl_stmt|;
name|req
operator|.
name|setConfOverlay
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|req
operator|.
name|setRunAsync
argument_list|(
name|isAsync
argument_list|)
expr_stmt|;
name|TExecuteStatementResp
name|resp
init|=
name|cliService
operator|.
name|ExecuteStatement
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getTypeInfo(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TGetTypeInfoReq
name|req
init|=
operator|new
name|TGetTypeInfoReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TGetTypeInfoResp
name|resp
init|=
name|cliService
operator|.
name|GetTypeInfo
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getCatalogs(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TGetCatalogsReq
name|req
init|=
operator|new
name|TGetCatalogsReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TGetCatalogsResp
name|resp
init|=
name|cliService
operator|.
name|GetCatalogs
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getSchemas(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.lang.String)    */
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
try|try
block|{
name|TGetSchemasReq
name|req
init|=
operator|new
name|TGetSchemasReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|req
operator|.
name|setCatalogName
argument_list|(
name|catalogName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setSchemaName
argument_list|(
name|schemaName
argument_list|)
expr_stmt|;
name|TGetSchemasResp
name|resp
init|=
name|cliService
operator|.
name|GetSchemas
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getTables(org.apache.hive.service.cli.SessionHandle, java.lang.String, java.lang.String, java.lang.String, java.util.List)    */
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
try|try
block|{
name|TGetTablesReq
name|req
init|=
operator|new
name|TGetTablesReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|req
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setTableTypes
argument_list|(
name|tableTypes
argument_list|)
expr_stmt|;
name|req
operator|.
name|setSchemaName
argument_list|(
name|schemaName
argument_list|)
expr_stmt|;
name|TGetTablesResp
name|resp
init|=
name|cliService
operator|.
name|GetTables
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getTableTypes(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TGetTableTypesReq
name|req
init|=
operator|new
name|TGetTableTypesReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TGetTableTypesResp
name|resp
init|=
name|cliService
operator|.
name|GetTableTypes
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getColumns(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TGetColumnsReq
name|req
init|=
operator|new
name|TGetColumnsReq
argument_list|()
decl_stmt|;
name|req
operator|.
name|setSessionHandle
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|setCatalogName
argument_list|(
name|catalogName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setSchemaName
argument_list|(
name|schemaName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setColumnName
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|TGetColumnsResp
name|resp
init|=
name|cliService
operator|.
name|GetColumns
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getFunctions(org.apache.hive.service.cli.SessionHandle)    */
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
try|try
block|{
name|TGetFunctionsReq
name|req
init|=
operator|new
name|TGetFunctionsReq
argument_list|(
name|sessionHandle
operator|.
name|toTSessionHandle
argument_list|()
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|req
operator|.
name|setCatalogName
argument_list|(
name|catalogName
argument_list|)
expr_stmt|;
name|req
operator|.
name|setSchemaName
argument_list|(
name|schemaName
argument_list|)
expr_stmt|;
name|TGetFunctionsResp
name|resp
init|=
name|cliService
operator|.
name|GetFunctions
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OperationHandle
argument_list|(
name|resp
operator|.
name|getOperationHandle
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getOperationStatus(org.apache.hive.service.cli.OperationHandle)    */
annotation|@
name|Override
specifier|public
name|OperationState
name|getOperationStatus
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|TGetOperationStatusReq
name|req
init|=
operator|new
name|TGetOperationStatusReq
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TGetOperationStatusResp
name|resp
init|=
name|cliService
operator|.
name|GetOperationStatus
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|OperationState
operator|.
name|getOperationState
argument_list|(
name|resp
operator|.
name|getOperationState
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#cancelOperation(org.apache.hive.service.cli.OperationHandle)    */
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
try|try
block|{
name|TCancelOperationReq
name|req
init|=
operator|new
name|TCancelOperationReq
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TCancelOperationResp
name|resp
init|=
name|cliService
operator|.
name|CancelOperation
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#closeOperation(org.apache.hive.service.cli.OperationHandle)    */
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
try|try
block|{
name|TCloseOperationReq
name|req
init|=
operator|new
name|TCloseOperationReq
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TCloseOperationResp
name|resp
init|=
name|cliService
operator|.
name|CloseOperation
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getResultSetMetadata(org.apache.hive.service.cli.OperationHandle)    */
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
try|try
block|{
name|TGetResultSetMetadataReq
name|req
init|=
operator|new
name|TGetResultSetMetadataReq
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
decl_stmt|;
name|TGetResultSetMetadataResp
name|resp
init|=
name|cliService
operator|.
name|GetResultSetMetadata
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|TableSchema
argument_list|(
name|resp
operator|.
name|getSchema
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#fetchResults(org.apache.hive.service.cli.OperationHandle, org.apache.hive.service.cli.FetchOrientation, long)    */
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
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|TFetchResultsReq
name|req
init|=
operator|new
name|TFetchResultsReq
argument_list|()
decl_stmt|;
name|req
operator|.
name|setOperationHandle
argument_list|(
name|opHandle
operator|.
name|toTOperationHandle
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|setOrientation
argument_list|(
name|orientation
operator|.
name|toTFetchOrientation
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|setMaxRows
argument_list|(
name|maxRows
argument_list|)
expr_stmt|;
name|TFetchResultsResp
name|resp
init|=
name|cliService
operator|.
name|FetchResults
argument_list|(
name|req
argument_list|)
decl_stmt|;
name|checkStatus
argument_list|(
name|resp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|RowSet
argument_list|(
name|resp
operator|.
name|getResults
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#fetchResults(org.apache.hive.service.cli.OperationHandle)    */
annotation|@
name|Override
specifier|public
name|RowSet
name|fetchResults
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
comment|// TODO: set the correct default fetch size
return|return
name|fetchResults
argument_list|(
name|opHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_NEXT
argument_list|,
literal|10000
argument_list|)
return|;
block|}
block|}
end_class

end_unit

