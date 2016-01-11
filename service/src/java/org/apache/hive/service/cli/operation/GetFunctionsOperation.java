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
name|List
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
name|exec
operator|.
name|FunctionInfo
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
name|exec
operator|.
name|FunctionRegistry
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
name|HivePrivilegeObject
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilegeObjectUtils
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
name|CLIServiceUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * GetFunctionsOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetFunctionsOperation
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
literal|"FUNCTION_CAT"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Function catalog (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FUNCTION_SCHEM"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Function schema (may be null)"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FUNCTION_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"Function name. This is the name used to invoke the function"
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
literal|"Explanatory comment on the function"
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"FUNCTION_TYPE"
argument_list|,
name|Type
operator|.
name|INT_TYPE
argument_list|,
literal|"Kind of function."
argument_list|)
operator|.
name|addPrimitiveColumn
argument_list|(
literal|"SPECIFIC_NAME"
argument_list|,
name|Type
operator|.
name|STRING_TYPE
argument_list|,
literal|"The name which uniquely identifies this function within its schema"
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
name|functionName
decl_stmt|;
specifier|private
specifier|final
name|RowSet
name|rowSet
decl_stmt|;
specifier|public
name|GetFunctionsOperation
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
name|functionName
operator|=
name|functionName
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
comment|// get databases for schema pattern
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
name|List
argument_list|<
name|String
argument_list|>
name|matchingDbs
decl_stmt|;
try|try
block|{
name|matchingDbs
operator|=
name|metastoreClient
operator|.
name|getDatabases
argument_list|(
name|schemaPattern
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
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
comment|// authorize this call on the schema objects
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
init|=
name|HivePrivilegeObjectUtils
operator|.
name|getHivePrivDbObjects
argument_list|(
name|matchingDbs
argument_list|)
decl_stmt|;
name|String
name|cmdStr
init|=
literal|"catalog : "
operator|+
name|catalogName
operator|+
literal|", schemaPattern : "
operator|+
name|schemaName
decl_stmt|;
name|authorizeMetaGets
argument_list|(
name|HiveOperationType
operator|.
name|GET_FUNCTIONS
argument_list|,
name|privObjs
argument_list|,
name|cmdStr
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
operator|(
literal|null
operator|==
name|catalogName
operator|||
literal|""
operator|.
name|equals
argument_list|(
name|catalogName
argument_list|)
operator|)
operator|&&
operator|(
literal|null
operator|==
name|schemaName
operator|||
literal|""
operator|.
name|equals
argument_list|(
name|schemaName
argument_list|)
operator|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|functionNames
init|=
name|FunctionRegistry
operator|.
name|getFunctionNames
argument_list|(
name|CLIServiceUtils
operator|.
name|patternToRegex
argument_list|(
name|functionName
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|functionName
range|:
name|functionNames
control|)
block|{
name|FunctionInfo
name|functionInfo
init|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|functionName
argument_list|)
decl_stmt|;
name|Object
name|rowData
index|[]
init|=
operator|new
name|Object
index|[]
block|{
literal|null
block|,
comment|// FUNCTION_CAT
literal|null
block|,
comment|// FUNCTION_SCHEM
name|functionInfo
operator|.
name|getDisplayName
argument_list|()
block|,
comment|// FUNCTION_NAME
literal|""
block|,
comment|// REMARKS
operator|(
name|functionInfo
operator|.
name|isGenericUDTF
argument_list|()
condition|?
name|DatabaseMetaData
operator|.
name|functionReturnsTable
else|:
name|DatabaseMetaData
operator|.
name|functionNoTable
operator|)
block|,
comment|// FUNCTION_TYPE
name|functionInfo
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
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

