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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|IMetaStoreClient
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|CompositeService
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
name|ServiceException
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
name|session
operator|.
name|SessionManager
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
name|TProtocolVersion
import|;
end_import

begin_comment
comment|/**  * CLIService.  *  */
end_comment

begin_class
specifier|public
class|class
name|CLIService
extends|extends
name|CompositeService
implements|implements
name|ICLIService
block|{
specifier|public
specifier|static
specifier|final
name|TProtocolVersion
name|SERVER_VERSION
decl_stmt|;
static|static
block|{
name|TProtocolVersion
index|[]
name|protocols
init|=
name|TProtocolVersion
operator|.
name|values
argument_list|()
decl_stmt|;
name|SERVER_VERSION
operator|=
name|protocols
index|[
name|protocols
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
block|}
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CLIService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|SessionManager
name|sessionManager
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|metastoreClient
decl_stmt|;
specifier|private
name|String
name|serverUserName
init|=
literal|null
decl_stmt|;
specifier|public
name|CLIService
parameter_list|()
block|{
name|super
argument_list|(
literal|"CLIService"
argument_list|)
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
name|sessionManager
operator|=
operator|new
name|SessionManager
argument_list|()
expr_stmt|;
name|addService
argument_list|(
name|sessionManager
argument_list|)
expr_stmt|;
try|try
block|{
name|HiveAuthFactory
operator|.
name|loginFromKeytab
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|serverUserName
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getShortUserName
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getUGIForConf
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Unable to login to kerberos with given principal/keytab"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Unable to login to kerberos with given principal/keytab"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
try|try
block|{
comment|// make sure that the base scratch directories exists and writable
name|setupStagingDir
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|setupStagingDir
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALSCRATCHDIR
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setupStagingDir
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DOWNLOADED_RESOURCES_DIR
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|eIO
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Error setting stage directories"
argument_list|,
name|eIO
argument_list|)
throw|;
block|}
try|try
block|{
comment|// Initialize and test a connection to the metastore
name|metastoreClient
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|metastoreClient
operator|.
name|getDatabases
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
literal|"Unable to connect to MetaStore!"
argument_list|,
name|e
argument_list|)
throw|;
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
name|metastoreClient
operator|!=
literal|null
condition|)
block|{
name|metastoreClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SessionHandle
name|openSession
parameter_list|(
name|TProtocolVersion
name|protocol
parameter_list|,
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
name|SessionHandle
name|sessionHandle
init|=
name|sessionManager
operator|.
name|openSession
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": openSession()"
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
block|}
specifier|public
name|SessionHandle
name|openSessionWithImpersonation
parameter_list|(
name|TProtocolVersion
name|protocol
parameter_list|,
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
name|SessionHandle
name|sessionHandle
init|=
name|sessionManager
operator|.
name|openSession
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|,
literal|true
argument_list|,
name|delegationToken
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": openSession()"
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
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
name|SessionHandle
name|sessionHandle
init|=
name|sessionManager
operator|.
name|openSession
argument_list|(
name|SERVER_VERSION
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": openSession()"
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#openSession(java.lang.String, java.lang.String, java.util.Map)    */
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
name|SessionHandle
name|sessionHandle
init|=
name|sessionManager
operator|.
name|openSession
argument_list|(
name|SERVER_VERSION
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|configuration
argument_list|,
literal|true
argument_list|,
name|delegationToken
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": openSession()"
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
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
name|sessionManager
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": closeSession()"
argument_list|)
expr_stmt|;
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
name|getInfoType
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|GetInfoValue
name|infoValue
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getInfo
argument_list|(
name|getInfoType
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getInfo()"
argument_list|)
expr_stmt|;
return|return
name|infoValue
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#executeStatement(org.apache.hive.service.cli.SessionHandle,    *  java.lang.String, java.util.Map)    */
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|executeStatement
argument_list|(
name|statement
argument_list|,
name|confOverlay
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": executeStatement()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#executeStatementAsync(org.apache.hive.service.cli.SessionHandle,    *  java.lang.String, java.util.Map)    */
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|executeStatementAsync
argument_list|(
name|statement
argument_list|,
name|confOverlay
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": executeStatementAsync()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getTypeInfo()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getCatalogs
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getCatalogs()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getSchemas
argument_list|(
name|catalogName
argument_list|,
name|schemaName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getSchemas()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getTables
argument_list|(
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|tableTypes
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getTables()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getTableTypes
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getTableTypes()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getColumns
argument_list|(
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|columnName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getColumns()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
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
name|OperationHandle
name|opHandle
init|=
name|sessionManager
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
operator|.
name|getFunctions
argument_list|(
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|sessionHandle
operator|+
literal|": getFunctions()"
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.ICLIService#getOperationStatus(org.apache.hive.service.cli.OperationHandle)    */
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
name|OperationStatus
name|opStatus
init|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperationStatus
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": getOperationStatus()"
argument_list|)
expr_stmt|;
return|return
name|opStatus
return|;
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
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getParentSession
argument_list|()
operator|.
name|cancelOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": cancelOperation()"
argument_list|)
expr_stmt|;
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
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getParentSession
argument_list|()
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": closeOperation"
argument_list|)
expr_stmt|;
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
name|TableSchema
name|tableSchema
init|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getParentSession
argument_list|()
operator|.
name|getResultSetMetadata
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": getResultSetMetadata()"
argument_list|)
expr_stmt|;
return|return
name|tableSchema
return|;
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
name|RowSet
name|rowSet
init|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getParentSession
argument_list|()
operator|.
name|fetchResults
argument_list|(
name|opHandle
argument_list|,
name|orientation
argument_list|,
name|maxRows
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": fetchResults()"
argument_list|)
expr_stmt|;
return|return
name|rowSet
return|;
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
name|RowSet
name|rowSet
init|=
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getParentSession
argument_list|()
operator|.
name|fetchResults
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": fetchResults()"
argument_list|)
expr_stmt|;
return|return
name|rowSet
return|;
block|}
comment|// obtain delegation token for the give user from metastore
specifier|public
specifier|synchronized
name|String
name|getDelegationTokenFromMetaStore
parameter_list|(
name|String
name|owner
parameter_list|)
throws|throws
name|HiveSQLException
throws|,
name|UnsupportedOperationException
throws|,
name|LoginException
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_USE_THRIFT_SASL
argument_list|)
operator|||
operator|!
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"delegation token is can only be obtained for a secure remote metastore"
argument_list|)
throw|;
block|}
try|try
block|{
name|Hive
operator|.
name|closeCurrent
argument_list|()
expr_stmt|;
return|return
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|getDelegationToken
argument_list|(
name|owner
argument_list|,
name|owner
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
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
name|UnsupportedOperationException
condition|)
block|{
throw|throw
operator|(
name|UnsupportedOperationException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error connect metastore to setup impersonation"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// create the give Path if doesn't exists and make it writable
specifier|private
name|void
name|setupStagingDir
parameter_list|(
name|String
name|dirPath
parameter_list|,
name|boolean
name|isLocal
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|scratchDir
init|=
operator|new
name|Path
argument_list|(
name|dirPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
if|if
condition|(
name|isLocal
condition|)
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fs
operator|=
name|scratchDir
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|scratchDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|scratchDir
argument_list|)
expr_stmt|;
name|FsPermission
name|fsPermission
init|=
operator|new
name|FsPermission
argument_list|(
operator|(
name|short
operator|)
literal|0777
argument_list|)
decl_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|scratchDir
argument_list|,
name|fsPermission
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

