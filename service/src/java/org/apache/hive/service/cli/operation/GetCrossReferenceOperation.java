begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Arrays
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
name|ForeignKeysRequest
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
name|SQLForeignKey
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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|thrift
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
comment|/**  * GetCrossReferenceOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetCrossReferenceOperation
extends|extends
name|MetadataOperation
block|{
comment|/**   PKTABLE_CAT String => parent key table catalog (may be null)   PKTABLE_SCHEM String => parent key table schema (may be null)   PKTABLE_NAME String => parent key table name   PKCOLUMN_NAME String => parent key column name   FKTABLE_CAT String => foreign key table catalog (may be null) being exported (may be null)   FKTABLE_SCHEM String => foreign key table schema (may be null) being exported (may be null)   FKTABLE_NAME String => foreign key table name being exported   FKCOLUMN_NAME String => foreign key column name being exported   KEY_SEQ short => sequence number within foreign key( a value of 1 represents the first column of the foreign key, a value of 2 would represent the second column within the foreign key).   UPDATE_RULE short => What happens to foreign key when parent key is updated:   importedNoAction - do not allow update of parent key if it has been imported   importedKeyCascade - change imported key to agree with parent key update   importedKeySetNull - change imported key to NULL if its parent key has been updated   importedKeySetDefault - change imported key to default values if its parent key has been updated   importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)   DELETE_RULE short => What happens to the foreign key when parent key is deleted.   importedKeyNoAction - do not allow delete of parent key if it has been imported   importedKeyCascade - delete rows that import a deleted key   importedKeySetNull - change imported key to NULL if its primary key has been deleted   importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)   importedKeySetDefault - change imported key to default if its parent key has been deleted   FK_NAME String => foreign key name (may be null)   PK_NAME String => parent key name (may be null)   DEFERRABILITY short => can the evaluation of foreign key constraints be deferred until commit   importedKeyInitiallyDeferred - see SQL92 for definition   importedKeyInitiallyImmediate - see SQL92 for definition   importedKeyNotDeferrable - see SQL92 for definition  */
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
literal|"PKTABLE_CAT"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Parent key table catalog (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"PKTABLE_SCHEM"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Parent key table schema (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"PKTABLE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Parent Key table name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"PKCOLUMN_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Parent Key column name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FKTABLE_CAT"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Foreign key table catalog (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FKTABLE_SCHEM"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Foreign key table schema (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FKTABLE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Foreign Key table name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FKCOLUMN_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Foreign Key column name"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"KEQ_SEQ"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Sequence number within primary key"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"UPDATE_RULE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"What happens to foreign key when parent key is updated"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"DELETE_RULE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"What happens to foreign key when parent key is deleted"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FK_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Foreign key name (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"PK_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Primary key name (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"DEFERRABILITY"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Can the evaluation of foreign key constraints be deferred until commit"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|parentCatalogName
decl_stmt|;
specifier|private
specifier|final
name|String
name|parentSchemaName
decl_stmt|;
specifier|private
specifier|final
name|String
name|parentTableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|foreignCatalogName
decl_stmt|;
specifier|private
specifier|final
name|String
name|foreignSchemaName
decl_stmt|;
specifier|private
specifier|final
name|String
name|foreignTableName
decl_stmt|;
specifier|private
specifier|final
name|RowSet
name|rowSet
decl_stmt|;
specifier|public
name|GetCrossReferenceOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|parentCatalogName
parameter_list|,
name|String
name|parentSchemaName
parameter_list|,
name|String
name|parentTableName
parameter_list|,
name|String
name|foreignCatalog
parameter_list|,
name|String
name|foreignSchema
parameter_list|,
name|String
name|foreignTable
parameter_list|)
block|{
name|super
argument_list|(
name|parentSession
argument_list|,
name|OperationType
operator|.
name|GET_FUNCTIONS
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentCatalogName
operator|=
name|parentCatalogName
expr_stmt|;
name|this
operator|.
name|parentSchemaName
operator|=
name|parentSchemaName
expr_stmt|;
name|this
operator|.
name|parentTableName
operator|=
name|parentTableName
expr_stmt|;
name|this
operator|.
name|foreignCatalogName
operator|=
name|foreignCatalog
expr_stmt|;
name|this
operator|.
name|foreignSchemaName
operator|=
name|foreignSchema
expr_stmt|;
name|this
operator|.
name|foreignTableName
operator|=
name|foreignTable
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
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|runInternal
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
name|ForeignKeysRequest
name|fkReq
init|=
operator|new
name|ForeignKeysRequest
argument_list|(
name|parentSchemaName
argument_list|,
name|parentTableName
argument_list|,
name|foreignSchemaName
argument_list|,
name|foreignTableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|fks
init|=
name|metastoreClient
operator|.
name|getForeignKeys
argument_list|(
name|fkReq
argument_list|)
decl_stmt|;
if|if
condition|(
name|fks
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|SQLForeignKey
name|fk
range|:
name|fks
control|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
operator|new
name|Object
index|[]
block|{
name|parentCatalogName
block|,
name|fk
operator|.
name|getPktable_db
argument_list|()
block|,
name|fk
operator|.
name|getPktable_name
argument_list|()
block|,
name|fk
operator|.
name|getPkcolumn_name
argument_list|()
block|,
name|foreignCatalogName
block|,
name|fk
operator|.
name|getFktable_db
argument_list|()
block|,
name|fk
operator|.
name|getFktable_name
argument_list|()
block|,
name|fk
operator|.
name|getFkcolumn_name
argument_list|()
block|,
name|fk
operator|.
name|getKey_seq
argument_list|()
block|,
name|fk
operator|.
name|getUpdate_rule
argument_list|()
block|,
name|fk
operator|.
name|getDelete_rule
argument_list|()
block|,
name|fk
operator|.
name|getFk_name
argument_list|()
block|,
name|fk
operator|.
name|getPk_name
argument_list|()
block|,
literal|0
block|}
argument_list|)
expr_stmt|;
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
operator|new
name|ArrayList
argument_list|<
name|OperationState
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
argument_list|)
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
operator|new
name|ArrayList
argument_list|<
name|OperationState
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
argument_list|)
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

