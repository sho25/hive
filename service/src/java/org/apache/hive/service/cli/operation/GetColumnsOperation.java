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
name|DatabaseMetaData
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
name|regex
operator|.
name|Pattern
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
name|ColumnDescriptor
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
name|Type
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
comment|/**  * GetColumnsOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetColumnsOperation
extends|extends
name|MetadataOperation
block|{
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
name|addPrimitiveColumn
argument_list|(
literal|"TABLE_CAT"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Catalog name. NULL if not applicable"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"TABLE_SCHEM"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Schema name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"TABLE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Table name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"COLUMN_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Column name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"DATA_TYPE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"SQL type from java.sql.Types"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"TYPE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Data source dependent type name, for a UDT the type name is fully qualified"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"COLUMN_SIZE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Column size. For char or date types this is the maximum number of characters,"
operator|+
literal|" for numeric or decimal types this is precision."
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"BUFFER_LENGTH"
argument_list|,
name|Type
operator|.
name|TINYINT_TYPE
argument_list|,
literal|"Unused"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"DECIMAL_DIGITS"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"The number of fractional digits"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"NUM_PREC_RADIX"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Radix (typically either 10 or 2)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"NULLABLE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Is NULL allowed"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"REMARKS"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Comment describing column (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"COLUMN_DEF"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Default value (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SQL_DATA_TYPE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Unused"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SQL_DATETIME_SUB"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Unused"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"CHAR_OCTET_LENGTH"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"For char types the maximum number of bytes in the column"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"ORDINAL_POSITION"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Index of column in table (starting at 1)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"IS_NULLABLE"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"\"NO\" means column definitely does not allow NULL values; "
operator|+
literal|"\"YES\" means the column might allow NULL values. An empty "
operator|+
literal|"string means nobody knows."
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SCOPE_CATALOG"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Catalog of table that is the scope of a reference attribute "
operator|+
literal|"(null if DATA_TYPE isn't REF)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SCOPE_SCHEMA"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Schema of table that is the scope of a reference attribute "
operator|+
literal|"(null if the DATA_TYPE isn't REF)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SCOPE_TABLE"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Table name that this the scope of a reference attribure "
operator|+
literal|"(null if the DATA_TYPE isn't REF)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SOURCE_DATA_TYPE"
argument_list|,
name|Type
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"Source type of a distinct type or user-generated Ref type, "
operator|+
literal|"SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"IS_AUTO_INCREMENT"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Indicates whether this column is auto incremented."
argument_list|)
decl_stmt|;
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
name|String
name|columnName
decl_stmt|;
specifier|private
specifier|final
name|RowSet
name|rowSet
decl_stmt|;
specifier|protected
name|GetColumnsOperation
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
name|super
argument_list|(
name|parentSession
argument_list|,
name|OperationType
operator|.
name|GET_COLUMNS
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
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|rowSet
operator|=
name|RowSetFactory
operator|.
name|create
argument_list|(
name|RESULT_SET_SCHEMA
argument_list|,
name|getProtocolVersion
argument_list|()
argument_list|)
expr_stmt|;
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
name|Pattern
name|columnPattern
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|columnName
operator|!=
literal|null
condition|)
block|{
name|columnPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|convertIdentifierPattern
argument_list|(
name|columnName
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|dbNames
init|=
name|metastoreClient
operator|.
name|getDatabases
argument_list|(
name|schemaPattern
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|dbNames
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|dbName
range|:
name|dbNames
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
name|Collections
operator|.
name|sort
argument_list|(
name|tableNames
argument_list|)
expr_stmt|;
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
name|TableSchema
name|schema
init|=
operator|new
name|TableSchema
argument_list|(
name|metastoreClient
operator|.
name|getSchema
argument_list|(
name|dbName
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnDescriptor
name|column
range|:
name|schema
operator|.
name|getColumnDescriptors
argument_list|()
control|)
block|{
if|if
condition|(
name|columnPattern
operator|!=
literal|null
operator|&&
operator|!
name|columnPattern
operator|.
name|matcher
argument_list|(
name|column
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Object
index|[]
name|rowData
init|=
operator|new
name|Object
index|[]
block|{
literal|null
block|,
comment|// TABLE_CAT
name|table
operator|.
name|getDbName
argument_list|()
block|,
comment|// TABLE_SCHEM
name|table
operator|.
name|getTableName
argument_list|()
block|,
comment|// TABLE_NAME
name|column
operator|.
name|getName
argument_list|()
block|,
comment|// COLUMN_NAME
name|column
operator|.
name|getType
argument_list|()
operator|.
name|toJavaSQLType
argument_list|()
block|,
comment|// DATA_TYPE
name|column
operator|.
name|getTypeName
argument_list|()
block|,
comment|// TYPE_NAME
name|column
operator|.
name|getType
argument_list|()
operator|.
name|getColumnSize
argument_list|()
block|,
comment|// COLUMN_SIZE
literal|null
block|,
comment|// BUFFER_LENGTH, unused
name|column
operator|.
name|getType
argument_list|()
operator|.
name|getDecimalDigits
argument_list|()
block|,
comment|// DECIMAL_DIGITS
name|column
operator|.
name|getType
argument_list|()
operator|.
name|getNumPrecRadix
argument_list|()
block|,
comment|// NUM_PREC_RADIX
name|DatabaseMetaData
operator|.
name|columnNullable
block|,
comment|// NULLABLE
name|column
operator|.
name|getComment
argument_list|()
block|,
comment|// REMARKS
literal|null
block|,
comment|// COLUMN_DEF
literal|null
block|,
comment|// SQL_DATA_TYPE
literal|null
block|,
comment|// SQL_DATETIME_SUB
literal|null
block|,
comment|// CHAR_OCTET_LENGTH
name|column
operator|.
name|getOrdinalPosition
argument_list|()
block|,
comment|// ORDINAL_POSITION
literal|"YES"
block|,
comment|// IS_NULLABLE
literal|null
block|,
comment|// SCOPE_CATALOG
literal|null
block|,
comment|// SCOPE_SCHEMA
literal|null
block|,
comment|// SCOPE_TABLE
literal|null
block|,
comment|// SOURCE_DATA_TYPE
literal|"NO"
block|,
comment|// IS_AUTO_INCREMENT
block|}
decl_stmt|;
name|rowSet
operator|.
name|addRow
argument_list|(
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

