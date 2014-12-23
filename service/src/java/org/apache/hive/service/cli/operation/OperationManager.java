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
name|operation
package|;
end_package

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|Schema
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
name|OperationLog
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
name|OperationStatus
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
name|RowSetFactory
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
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|ConsoleAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Layout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|PatternLayout
import|;
end_import

begin_comment
comment|/**  * OperationManager.  *  */
end_comment

begin_class
specifier|public
class|class
name|OperationManager
extends|extends
name|AbstractService
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|OperationManager
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
specifier|final
name|Map
argument_list|<
name|OperationHandle
argument_list|,
name|Operation
argument_list|>
name|handleToOperation
init|=
operator|new
name|HashMap
argument_list|<
name|OperationHandle
argument_list|,
name|Operation
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|OperationManager
parameter_list|()
block|{
name|super
argument_list|(
name|OperationManager
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
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
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_ENABLED
argument_list|)
condition|)
block|{
name|boolean
name|isVerbose
init|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_VERBOSE
argument_list|)
decl_stmt|;
name|initOperationLogCapture
argument_list|(
name|isVerbose
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Operation level logging is turned off"
argument_list|)
expr_stmt|;
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
comment|// TODO
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
comment|// TODO
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initOperationLogCapture
parameter_list|(
name|boolean
name|isVerbose
parameter_list|)
block|{
comment|// There should be a ConsoleAppender. Copy its Layout.
name|Logger
name|root
init|=
name|Logger
operator|.
name|getRootLogger
argument_list|()
decl_stmt|;
name|Layout
name|layout
init|=
literal|null
decl_stmt|;
name|Enumeration
argument_list|<
name|?
argument_list|>
name|appenders
init|=
name|root
operator|.
name|getAllAppenders
argument_list|()
decl_stmt|;
while|while
condition|(
name|appenders
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|Appender
name|ap
init|=
operator|(
name|Appender
operator|)
name|appenders
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|ap
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|ConsoleAppender
operator|.
name|class
argument_list|)
condition|)
block|{
name|layout
operator|=
name|ap
operator|.
name|getLayout
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
specifier|final
name|String
name|VERBOSE_PATTERN
init|=
literal|"%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n"
decl_stmt|;
specifier|final
name|String
name|NONVERBOSE_PATTERN
init|=
literal|"%-5p : %m%n"
decl_stmt|;
if|if
condition|(
name|isVerbose
condition|)
block|{
if|if
condition|(
name|layout
operator|==
literal|null
condition|)
block|{
name|layout
operator|=
operator|new
name|PatternLayout
argument_list|(
name|VERBOSE_PATTERN
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot find a Layout from a ConsoleAppender. Using default Layout pattern."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|layout
operator|=
operator|new
name|PatternLayout
argument_list|(
name|NONVERBOSE_PATTERN
argument_list|)
expr_stmt|;
block|}
comment|// Register another Appender (with the same layout) that talks to us.
name|Appender
name|ap
init|=
operator|new
name|LogDivertAppender
argument_list|(
name|layout
argument_list|,
name|this
argument_list|,
name|isVerbose
argument_list|)
decl_stmt|;
name|root
operator|.
name|addAppender
argument_list|(
name|ap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ExecuteStatementOperation
name|newExecuteStatementOperation
parameter_list|(
name|HiveSession
name|parentSession
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
name|runAsync
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|ExecuteStatementOperation
name|executeStatementOperation
init|=
name|ExecuteStatementOperation
operator|.
name|newExecuteStatementOperation
argument_list|(
name|parentSession
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
name|runAsync
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|executeStatementOperation
argument_list|)
expr_stmt|;
return|return
name|executeStatementOperation
return|;
block|}
specifier|public
name|GetTypeInfoOperation
name|newGetTypeInfoOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|)
block|{
name|GetTypeInfoOperation
name|operation
init|=
operator|new
name|GetTypeInfoOperation
argument_list|(
name|parentSession
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|GetCatalogsOperation
name|newGetCatalogsOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|)
block|{
name|GetCatalogsOperation
name|operation
init|=
operator|new
name|GetCatalogsOperation
argument_list|(
name|parentSession
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|GetSchemasOperation
name|newGetSchemasOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|catalogName
parameter_list|,
name|String
name|schemaName
parameter_list|)
block|{
name|GetSchemasOperation
name|operation
init|=
operator|new
name|GetSchemasOperation
argument_list|(
name|parentSession
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|MetadataOperation
name|newGetTablesOperation
parameter_list|(
name|HiveSession
name|parentSession
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
block|{
name|MetadataOperation
name|operation
init|=
operator|new
name|GetTablesOperation
argument_list|(
name|parentSession
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
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|GetTableTypesOperation
name|newGetTableTypesOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|)
block|{
name|GetTableTypesOperation
name|operation
init|=
operator|new
name|GetTableTypesOperation
argument_list|(
name|parentSession
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|GetColumnsOperation
name|newGetColumnsOperation
parameter_list|(
name|HiveSession
name|parentSession
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
block|{
name|GetColumnsOperation
name|operation
init|=
operator|new
name|GetColumnsOperation
argument_list|(
name|parentSession
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
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|GetFunctionsOperation
name|newGetFunctionsOperation
parameter_list|(
name|HiveSession
name|parentSession
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
block|{
name|GetFunctionsOperation
name|operation
init|=
operator|new
name|GetFunctionsOperation
argument_list|(
name|parentSession
argument_list|,
name|catalogName
argument_list|,
name|schemaName
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|addOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
specifier|public
name|Operation
name|getOperation
parameter_list|(
name|OperationHandle
name|operationHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|Operation
name|operation
init|=
name|getOperationInternal
argument_list|(
name|operationHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|operation
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Invalid OperationHandle: "
operator|+
name|operationHandle
argument_list|)
throw|;
block|}
return|return
name|operation
return|;
block|}
specifier|private
specifier|synchronized
name|Operation
name|getOperationInternal
parameter_list|(
name|OperationHandle
name|operationHandle
parameter_list|)
block|{
return|return
name|handleToOperation
operator|.
name|get
argument_list|(
name|operationHandle
argument_list|)
return|;
block|}
specifier|private
specifier|synchronized
name|Operation
name|removeTimedOutOperation
parameter_list|(
name|OperationHandle
name|operationHandle
parameter_list|)
block|{
name|Operation
name|operation
init|=
name|handleToOperation
operator|.
name|get
argument_list|(
name|operationHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|operation
operator|!=
literal|null
operator|&&
name|operation
operator|.
name|isTimedOut
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
condition|)
block|{
name|handleToOperation
operator|.
name|remove
argument_list|(
name|operationHandle
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|addOperation
parameter_list|(
name|Operation
name|operation
parameter_list|)
block|{
name|handleToOperation
operator|.
name|put
argument_list|(
name|operation
operator|.
name|getHandle
argument_list|()
argument_list|,
name|operation
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|synchronized
name|Operation
name|removeOperation
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
block|{
return|return
name|handleToOperation
operator|.
name|remove
argument_list|(
name|opHandle
argument_list|)
return|;
block|}
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
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getStatus
argument_list|()
return|;
block|}
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
name|Operation
name|operation
init|=
name|getOperation
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
name|OperationState
name|opState
init|=
name|operation
operator|.
name|getStatus
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
if|if
condition|(
name|opState
operator|==
name|OperationState
operator|.
name|CANCELED
operator|||
name|opState
operator|==
name|OperationState
operator|.
name|CLOSED
operator|||
name|opState
operator|==
name|OperationState
operator|.
name|FINISHED
operator|||
name|opState
operator|==
name|OperationState
operator|.
name|ERROR
operator|||
name|opState
operator|==
name|OperationState
operator|.
name|UNKNOWN
condition|)
block|{
comment|// Cancel should be a no-op in either cases
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": Operation is already aborted in state - "
operator|+
name|opState
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|opHandle
operator|+
literal|": Attempting to cancel from state - "
operator|+
name|opState
argument_list|)
expr_stmt|;
name|operation
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
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
name|Operation
name|operation
init|=
name|removeOperation
argument_list|(
name|opHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|operation
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Operation does not exist!"
argument_list|)
throw|;
block|}
name|operation
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TableSchema
name|getOperationResultSetSchema
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getResultSetSchema
argument_list|()
return|;
block|}
specifier|public
name|RowSet
name|getOperationNextRowSet
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getNextRowSet
argument_list|()
return|;
block|}
specifier|public
name|RowSet
name|getOperationNextRowSet
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
return|return
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getNextRowSet
argument_list|(
name|orientation
argument_list|,
name|maxRows
argument_list|)
return|;
block|}
specifier|public
name|RowSet
name|getOperationLogRowSet
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
comment|// get the OperationLog object from the operation
name|OperationLog
name|operationLog
init|=
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getOperationLog
argument_list|()
decl_stmt|;
if|if
condition|(
name|operationLog
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Couldn't find log associated with operation handle: "
operator|+
name|opHandle
argument_list|)
throw|;
block|}
comment|// read logs
name|List
argument_list|<
name|String
argument_list|>
name|logs
decl_stmt|;
try|try
block|{
name|logs
operator|=
name|operationLog
operator|.
name|readOperationLog
argument_list|(
name|isFetchFirst
argument_list|(
name|orientation
argument_list|)
argument_list|,
name|maxRows
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
comment|// convert logs to RowSet
name|TableSchema
name|tableSchema
init|=
operator|new
name|TableSchema
argument_list|(
name|getLogSchema
argument_list|()
argument_list|)
decl_stmt|;
name|RowSet
name|rowSet
init|=
name|RowSetFactory
operator|.
name|create
argument_list|(
name|tableSchema
argument_list|,
name|getOperation
argument_list|(
name|opHandle
argument_list|)
operator|.
name|getProtocolVersion
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|log
range|:
name|logs
control|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
operator|new
name|String
index|[]
block|{
name|log
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
return|;
block|}
specifier|private
name|boolean
name|isFetchFirst
parameter_list|(
name|FetchOrientation
name|fetchOrientation
parameter_list|)
block|{
comment|//TODO: Since OperationLog is moved to package o.a.h.h.ql.session,
comment|// we may add a Enum there and map FetchOrientation to it.
if|if
condition|(
name|fetchOrientation
operator|.
name|equals
argument_list|(
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Schema
name|getLogSchema
parameter_list|()
block|{
name|Schema
name|schema
init|=
operator|new
name|Schema
argument_list|()
decl_stmt|;
name|FieldSchema
name|fieldSchema
init|=
operator|new
name|FieldSchema
argument_list|()
decl_stmt|;
name|fieldSchema
operator|.
name|setName
argument_list|(
literal|"operation_log"
argument_list|)
expr_stmt|;
name|fieldSchema
operator|.
name|setType
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|addToFieldSchemas
argument_list|(
name|fieldSchema
argument_list|)
expr_stmt|;
return|return
name|schema
return|;
block|}
specifier|public
name|OperationLog
name|getOperationLogByThread
parameter_list|()
block|{
return|return
name|OperationLog
operator|.
name|getCurrentOperationLog
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|Operation
argument_list|>
name|removeExpiredOperations
parameter_list|(
name|OperationHandle
index|[]
name|handles
parameter_list|)
block|{
name|List
argument_list|<
name|Operation
argument_list|>
name|removed
init|=
operator|new
name|ArrayList
argument_list|<
name|Operation
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|OperationHandle
name|handle
range|:
name|handles
control|)
block|{
name|Operation
name|operation
init|=
name|removeTimedOutOperation
argument_list|(
name|handle
argument_list|)
decl_stmt|;
if|if
condition|(
name|operation
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Operation "
operator|+
name|handle
operator|+
literal|" is timed-out and will be closed"
argument_list|)
expr_stmt|;
name|removed
operator|.
name|add
argument_list|(
name|operation
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|removed
return|;
block|}
block|}
end_class

end_unit

