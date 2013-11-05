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
name|session
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|history
operator|.
name|HiveHistory
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
name|session
operator|.
name|SessionState
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
name|common
operator|.
name|util
operator|.
name|HiveVersionInfo
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
operator|.
name|ExecuteStatementOperation
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
name|operation
operator|.
name|GetCatalogsOperation
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
name|operation
operator|.
name|GetColumnsOperation
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
name|operation
operator|.
name|GetFunctionsOperation
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
name|operation
operator|.
name|GetSchemasOperation
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
name|operation
operator|.
name|GetTableTypesOperation
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
name|operation
operator|.
name|GetTypeInfoOperation
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
name|operation
operator|.
name|MetadataOperation
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
name|operation
operator|.
name|OperationManager
import|;
end_import

begin_comment
comment|/**  * HiveSession  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveSessionImpl
implements|implements
name|HiveSession
block|{
specifier|private
specifier|final
name|SessionHandle
name|sessionHandle
init|=
operator|new
name|SessionHandle
argument_list|()
decl_stmt|;
specifier|private
name|String
name|username
decl_stmt|;
specifier|private
specifier|final
name|String
name|password
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionConf
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
specifier|private
specifier|final
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|SessionState
name|sessionState
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FETCH_WORK_SERDE_CLASS
init|=
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveSessionImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|SessionManager
name|sessionManager
decl_stmt|;
specifier|private
name|OperationManager
name|operationManager
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|metastoreClient
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|OperationHandle
argument_list|>
name|opHandleSet
init|=
operator|new
name|HashSet
argument_list|<
name|OperationHandle
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|HiveSessionImpl
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
name|sessionConf
parameter_list|)
block|{
name|this
operator|.
name|username
operator|=
name|username
expr_stmt|;
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
if|if
condition|(
name|sessionConf
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|sessionConf
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|hiveConf
operator|.
name|set
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// set an explicit session name to control the download directory name
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVESESSIONID
operator|.
name|varname
argument_list|,
name|sessionHandle
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sessionState
operator|=
operator|new
name|SessionState
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SessionManager
name|getSessionManager
parameter_list|()
block|{
return|return
name|sessionManager
return|;
block|}
specifier|public
name|void
name|setSessionManager
parameter_list|(
name|SessionManager
name|sessionManager
parameter_list|)
block|{
name|this
operator|.
name|sessionManager
operator|=
name|sessionManager
expr_stmt|;
block|}
specifier|private
name|OperationManager
name|getOperationManager
parameter_list|()
block|{
return|return
name|operationManager
return|;
block|}
specifier|public
name|void
name|setOperationManager
parameter_list|(
name|OperationManager
name|operationManager
parameter_list|)
block|{
name|this
operator|.
name|operationManager
operator|=
name|operationManager
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|void
name|acquire
parameter_list|()
throws|throws
name|HiveSQLException
block|{
comment|// need to make sure that the this connections session state is
comment|// stored in the thread local for sessions.
name|SessionState
operator|.
name|setCurrentSessionState
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|void
name|release
parameter_list|()
block|{
assert|assert
name|sessionState
operator|!=
literal|null
assert|;
comment|// no need to release sessionState...
block|}
specifier|public
name|SessionHandle
name|getSessionHandle
parameter_list|()
block|{
return|return
name|sessionHandle
return|;
block|}
specifier|public
name|String
name|getUsername
parameter_list|()
block|{
return|return
name|username
return|;
block|}
specifier|public
name|String
name|getPassword
parameter_list|()
block|{
return|return
name|password
return|;
block|}
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEFETCHOUTPUTSERDE
argument_list|,
name|FETCH_WORK_SERDE_CLASS
argument_list|)
expr_stmt|;
return|return
name|hiveConf
return|;
block|}
specifier|public
name|IMetaStoreClient
name|getMetaStoreClient
parameter_list|()
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|metastoreClient
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|metastoreClient
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
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
return|return
name|metastoreClient
return|;
block|}
specifier|public
name|GetInfoValue
name|getInfo
parameter_list|(
name|GetInfoType
name|getInfoType
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
switch|switch
condition|(
name|getInfoType
condition|)
block|{
case|case
name|CLI_SERVER_NAME
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
literal|"Hive"
argument_list|)
return|;
case|case
name|CLI_DBMS_NAME
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
literal|"Apache Hive"
argument_list|)
return|;
case|case
name|CLI_DBMS_VER
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
name|HiveVersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
return|;
case|case
name|CLI_MAX_COLUMN_NAME_LEN
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
literal|128
argument_list|)
return|;
case|case
name|CLI_MAX_SCHEMA_NAME_LEN
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
literal|128
argument_list|)
return|;
case|case
name|CLI_MAX_TABLE_NAME_LEN
case|:
return|return
operator|new
name|GetInfoValue
argument_list|(
literal|128
argument_list|)
return|;
case|case
name|CLI_TXN_CAPABLE
case|:
default|default:
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Unrecognized GetInfoType value: "
operator|+
name|getInfoType
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|executeStatement
parameter_list|(
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
name|statement
argument_list|,
name|confOverlay
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|OperationHandle
name|executeStatementAsync
parameter_list|(
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
name|runAsync
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|ExecuteStatementOperation
name|operation
init|=
name|operationManager
operator|.
name|newExecuteStatementOperation
argument_list|(
name|getSession
argument_list|()
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
name|runAsync
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getTypeInfo
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetTypeInfoOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetTypeInfoOperation
argument_list|(
name|getSession
argument_list|()
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getCatalogs
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetCatalogsOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetCatalogsOperation
argument_list|(
name|getSession
argument_list|()
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getSchemas
parameter_list|(
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetSchemasOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetSchemasOperation
argument_list|(
name|getSession
argument_list|()
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getTables
parameter_list|(
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
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|MetadataOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetTablesOperation
argument_list|(
name|getSession
argument_list|()
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|tableTypes
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getTableTypes
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetTableTypesOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetTableTypesOperation
argument_list|(
name|getSession
argument_list|()
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getColumns
parameter_list|(
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
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetColumnsOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetColumnsOperation
argument_list|(
name|getSession
argument_list|()
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|tableName
argument_list|,
name|columnName
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|OperationHandle
name|getFunctions
parameter_list|(
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
name|acquire
argument_list|()
expr_stmt|;
name|OperationManager
name|operationManager
init|=
name|getOperationManager
argument_list|()
decl_stmt|;
name|GetFunctionsOperation
name|operation
init|=
name|operationManager
operator|.
name|newGetFunctionsOperation
argument_list|(
name|getSession
argument_list|()
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|OperationHandle
name|opHandle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
try|try
block|{
name|operation
operator|.
name|run
argument_list|()
expr_stmt|;
name|opHandleSet
operator|.
name|add
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
return|return
name|opHandle
return|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|acquire
argument_list|()
expr_stmt|;
comment|/**        *  For metadata operations like getTables(), getColumns() etc,        * the session allocates a private metastore handler which should be        * closed at the end of the session        */
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
comment|// Iterate through the opHandles and close their operations
for|for
control|(
name|OperationHandle
name|opHandle
range|:
name|opHandleSet
control|)
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
block|}
name|opHandleSet
operator|.
name|clear
argument_list|()
expr_stmt|;
name|HiveHistory
name|hiveHist
init|=
name|sessionState
operator|.
name|getHiveHistory
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|hiveHist
condition|)
block|{
name|hiveHist
operator|.
name|closeStream
argument_list|()
expr_stmt|;
block|}
name|sessionState
operator|.
name|close
argument_list|()
expr_stmt|;
name|release
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failure to close"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|SessionState
name|getSessionState
parameter_list|()
block|{
return|return
name|sessionState
return|;
block|}
specifier|public
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|username
return|;
block|}
specifier|public
name|void
name|setUserName
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
name|this
operator|.
name|username
operator|=
name|userName
expr_stmt|;
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
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|cancelOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
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
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
name|operationManager
operator|.
name|closeOperation
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
name|opHandleSet
operator|.
name|remove
argument_list|(
name|opHandle
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
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
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperationResultSetSchema
argument_list|(
name|opHandle
argument_list|)
return|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
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
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperationNextRowSet
argument_list|(
name|opHandle
argument_list|,
name|orientation
argument_list|,
name|maxRows
argument_list|)
return|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
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
name|acquire
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|sessionManager
operator|.
name|getOperationManager
argument_list|()
operator|.
name|getOperationNextRowSet
argument_list|(
name|opHandle
argument_list|)
return|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|HiveSession
name|getSession
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

