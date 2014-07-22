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
literal|"OperationManager"
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
specifier|synchronized
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
block|}
end_class

end_unit

