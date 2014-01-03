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
name|OperationManager
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

begin_interface
specifier|public
interface|interface
name|HiveSession
block|{
name|TProtocolVersion
name|getProtocolVersion
parameter_list|()
function_decl|;
comment|/**    * Set the session manager for the session    * @param sessionManager    */
specifier|public
name|void
name|setSessionManager
parameter_list|(
name|SessionManager
name|sessionManager
parameter_list|)
function_decl|;
comment|/**    * Get the session manager for the session    */
specifier|public
name|SessionManager
name|getSessionManager
parameter_list|()
function_decl|;
comment|/**    * Set operation manager for the session    * @param operationManager    */
specifier|public
name|void
name|setOperationManager
parameter_list|(
name|OperationManager
name|operationManager
parameter_list|)
function_decl|;
specifier|public
name|SessionHandle
name|getSessionHandle
parameter_list|()
function_decl|;
specifier|public
name|String
name|getUsername
parameter_list|()
function_decl|;
specifier|public
name|String
name|getPassword
parameter_list|()
function_decl|;
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
function_decl|;
specifier|public
name|IMetaStoreClient
name|getMetaStoreClient
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
comment|/**    * getInfo operation handler    * @param getInfoType    * @return    * @throws HiveSQLException    */
specifier|public
name|GetInfoValue
name|getInfo
parameter_list|(
name|GetInfoType
name|getInfoType
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
comment|/**    * execute operation handler    * @param statement    * @param confOverlay    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * execute operation handler    * @param statement    * @param confOverlay    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * getTypeInfo operation handler    * @return    * @throws HiveSQLException    */
specifier|public
name|OperationHandle
name|getTypeInfo
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
comment|/**    * getCatalogs operation handler    * @return    * @throws HiveSQLException    */
specifier|public
name|OperationHandle
name|getCatalogs
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
comment|/**    * getSchemas operation handler    * @param catalogName    * @param schemaName    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * getTables operation handler    * @param catalogName    * @param schemaName    * @param tableName    * @param tableTypes    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * getTableTypes operation handler    * @return    * @throws HiveSQLException    */
specifier|public
name|OperationHandle
name|getTableTypes
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
comment|/**    * getColumns operation handler    * @param catalogName    * @param schemaName    * @param tableName    * @param columnName    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * getFunctions operation handler    * @param catalogName    * @param schemaName    * @param functionName    * @return    * @throws HiveSQLException    */
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
function_decl|;
comment|/**    * close the session    * @throws HiveSQLException    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|void
name|cancelOperation
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|void
name|closeOperation
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|TableSchema
name|getResultSetMetadata
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
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
function_decl|;
specifier|public
name|RowSet
name|fetchResults
parameter_list|(
name|OperationHandle
name|opHandle
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|SessionState
name|getSessionState
parameter_list|()
function_decl|;
specifier|public
name|String
name|getUserName
parameter_list|()
function_decl|;
specifier|public
name|void
name|setUserName
parameter_list|(
name|String
name|userName
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

