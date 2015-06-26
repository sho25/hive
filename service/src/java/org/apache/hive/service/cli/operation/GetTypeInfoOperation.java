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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveOperationType
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
comment|/**  * GetTypeInfoOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetTypeInfoOperation
extends|extends
name|MetadataOperation
block|{
specifier|private
specifier|final
specifier|static
name|TableSchema
name|RESULT_SET_SCHEMA
init|=
operator|new
name|TableSchema
argument_list|()
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"TYPE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Type name"
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
literal|"SQL data type from java.sql.Types"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"PRECISION"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Maximum precision"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"LITERAL_PREFIX"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Prefix used to quote a literal (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"LITERAL_SUFFIX"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Suffix used to quote a literal (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"CREATE_PARAMS"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Parameters used in creating the type (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"NULLABLE"
argument_list|,
name|Type
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"Can you use NULL for this type"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"CASE_SENSITIVE"
argument_list|,
name|Type
operator|.
name|BOOLEAN_TYPE
argument_list|,
literal|"Is it case sensitive"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SEARCHABLE"
argument_list|,
name|Type
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"Can you use \"WHERE\" based on this type"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"UNSIGNED_ATTRIBUTE"
argument_list|,
name|Type
operator|.
name|BOOLEAN_TYPE
argument_list|,
literal|"Is it unsigned"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FIXED_PREC_SCALE"
argument_list|,
name|Type
operator|.
name|BOOLEAN_TYPE
argument_list|,
literal|"Can it be a money value"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"AUTO_INCREMENT"
argument_list|,
name|Type
operator|.
name|BOOLEAN_TYPE
argument_list|,
literal|"Can it be used for an auto-increment value"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"LOCAL_TYPE_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Localized version of type name (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"MINIMUM_SCALE"
argument_list|,
name|Type
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"Minimum scale supported"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"MAXIMUM_SCALE"
argument_list|,
name|Type
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"Maximum scale supported"
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
literal|"NUM_PREC_RADIX"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Usually 2 or 10"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RowSet
name|rowSet
decl_stmt|;
specifier|protected
name|GetTypeInfoOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|)
block|{
name|super
argument_list|(
name|parentSession
argument_list|,
name|OperationType
operator|.
name|GET_TYPE_INFO
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|isAuthV2Enabled
argument_list|()
condition|)
block|{
name|authorizeMetaGets
argument_list|(
name|HiveOperationType
operator|.
name|GET_TYPEINFO
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
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
name|type
operator|.
name|getName
argument_list|()
block|,
comment|// TYPE_NAME
name|type
operator|.
name|toJavaSQLType
argument_list|()
block|,
comment|// DATA_TYPE
name|type
operator|.
name|getMaxPrecision
argument_list|()
block|,
comment|// PRECISION
name|type
operator|.
name|getLiteralPrefix
argument_list|()
block|,
comment|// LITERAL_PREFIX
name|type
operator|.
name|getLiteralSuffix
argument_list|()
block|,
comment|// LITERAL_SUFFIX
name|type
operator|.
name|getCreateParams
argument_list|()
block|,
comment|// CREATE_PARAMS
name|type
operator|.
name|getNullable
argument_list|()
block|,
comment|// NULLABLE
name|type
operator|.
name|isCaseSensitive
argument_list|()
block|,
comment|// CASE_SENSITIVE
name|type
operator|.
name|getSearchable
argument_list|()
block|,
comment|// SEARCHABLE
name|type
operator|.
name|isUnsignedAttribute
argument_list|()
block|,
comment|// UNSIGNED_ATTRIBUTE
name|type
operator|.
name|isFixedPrecScale
argument_list|()
block|,
comment|// FIXED_PREC_SCALE
name|type
operator|.
name|isAutoIncrement
argument_list|()
block|,
comment|// AUTO_INCREMENT
name|type
operator|.
name|getLocalizedName
argument_list|()
block|,
comment|// LOCAL_TYPE_NAME
name|type
operator|.
name|getMinimumScale
argument_list|()
block|,
comment|// MINIMUM_SCALE
name|type
operator|.
name|getMaximumScale
argument_list|()
block|,
comment|// MAXIMUM_SCALE
literal|null
block|,
comment|// SQL_DATA_TYPE, unused
literal|null
block|,
comment|// SQL_DATETIME_SUB, unused
name|type
operator|.
name|getNumPrecRadix
argument_list|()
comment|//NUM_PREC_RADIX
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

