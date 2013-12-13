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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|OperationType
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
comment|/**  * GetTablesOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetTablesOperation
extends|extends
name|MetadataOperation
block|{
specifier|private
specifier|final
name|String
name|catalogName
decl_stmt|;
specifier|private
specifier|final
name|String
name|schemaName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|tableTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|RowSet
name|rowSet
init|=
operator|new
name|RowSet
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|TableTypeMapping
name|tableTypeMapping
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableSchema
name|RESULT_SET_SCHEMA
init|=
operator|new
name|TableSchema
argument_list|()
operator|.
name|addStringColumn
argument_list|(
literal|"TABLE_CAT"
argument_list|,
literal|"Catalog name. NULL if not applicable."
argument_list|)
operator|.
name|addStringColumn
argument_list|(
literal|"TABLE_SCHEM"
argument_list|,
literal|"Schema name."
argument_list|)
operator|.
name|addStringColumn
argument_list|(
literal|"TABLE_NAME"
argument_list|,
literal|"Table name."
argument_list|)
operator|.
name|addStringColumn
argument_list|(
literal|"TABLE_TYPE"
argument_list|,
literal|"The table type, e.g. \"TABLE\", \"VIEW\", etc."
argument_list|)
operator|.
name|addStringColumn
argument_list|(
literal|"REMARKS"
argument_list|,
literal|"Comments about the table."
argument_list|)
decl_stmt|;
specifier|protected
name|GetTablesOperation
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
name|super
argument_list|(
name|parentSession
argument_list|,
name|OperationType
operator|.
name|GET_TABLES
argument_list|)
expr_stmt|;
name|this
operator|.
name|catalogName
operator|=
name|catalogName
expr_stmt|;
name|this
operator|.
name|schemaName
operator|=
name|schemaName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|String
name|tableMappingStr
init|=
name|getParentSession
argument_list|()
operator|.
name|getHiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TABLE_TYPE_MAPPING
argument_list|)
decl_stmt|;
name|tableTypeMapping
operator|=
name|TableTypeMappingFactory
operator|.
name|getTableTypeMapping
argument_list|(
name|tableMappingStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableTypes
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|tableTypes
operator|.
name|addAll
argument_list|(
name|tableTypes
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.Operation#run()    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
try|try
block|{
name|IMetaStoreClient
name|metastoreClient
init|=
name|getParentSession
argument_list|()
operator|.
name|getMetaStoreClient
argument_list|()
decl_stmt|;
name|String
name|schemaPattern
init|=
name|convertSchemaPattern
argument_list|(
name|schemaName
argument_list|)
decl_stmt|;
name|String
name|tablePattern
init|=
name|convertIdentifierPattern
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|dbName
range|:
name|metastoreClient
operator|.
name|getDatabases
argument_list|(
name|schemaPattern
argument_list|)
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
name|metastoreClient
operator|.
name|getTables
argument_list|(
name|dbName
argument_list|,
name|tablePattern
argument_list|)
decl_stmt|;
for|for
control|(
name|Table
name|table
range|:
name|metastoreClient
operator|.
name|getTableObjectsByName
argument_list|(
name|dbName
argument_list|,
name|tableNames
argument_list|)
control|)
block|{
name|Object
index|[]
name|rowData
init|=
operator|new
name|Object
index|[]
block|{
name|DEFAULT_HIVE_CATALOG
block|,
name|table
operator|.
name|getDbName
argument_list|()
block|,
name|table
operator|.
name|getTableName
argument_list|()
block|,
name|tableTypeMapping
operator|.
name|mapToClientType
argument_list|(
name|table
operator|.
name|getTableType
argument_list|()
argument_list|)
block|,
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"comment"
argument_list|)
block|}
decl_stmt|;
if|if
condition|(
name|tableTypes
operator|.
name|isEmpty
argument_list|()
operator|||
name|tableTypes
operator|.
name|contains
argument_list|(
name|tableTypeMapping
operator|.
name|mapToClientType
argument_list|(
name|table
operator|.
name|getTableType
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
name|RESULT_SET_SCHEMA
argument_list|,
name|rowData
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|setState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.Operation#getResultSetSchema()    */
annotation|@
name|Override
specifier|public
name|TableSchema
name|getResultSetSchema
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|assertState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
return|return
name|RESULT_SET_SCHEMA
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.Operation#getNextRowSet(org.apache.hive.service.cli.FetchOrientation, long)    */
annotation|@
name|Override
specifier|public
name|RowSet
name|getNextRowSet
parameter_list|(
name|FetchOrientation
name|orientation
parameter_list|,
name|long
name|maxRows
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|assertState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
name|validateDefaultFetchOrientation
argument_list|(
name|orientation
argument_list|)
expr_stmt|;
if|if
condition|(
name|orientation
operator|.
name|equals
argument_list|(
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|)
condition|)
block|{
name|rowSet
operator|.
name|setStartOffset
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
operator|.
name|extractSubset
argument_list|(
operator|(
name|int
operator|)
name|maxRows
argument_list|)
return|;
block|}
block|}
end_class

end_unit

