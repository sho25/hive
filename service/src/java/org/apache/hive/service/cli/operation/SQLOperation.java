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
name|io
operator|.
name|IOException
import|;
end_import

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
name|Properties
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
name|CommandNeedRetryException
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
name|Driver
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
name|parse
operator|.
name|VariableSubstitution
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
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
name|SerDe
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
name|SerDeUtils
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|objectinspector
operator|.
name|StructField
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
name|objectinspector
operator|.
name|StructObjectInspector
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
name|io
operator|.
name|BytesWritable
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
comment|/**  * SQLOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|SQLOperation
extends|extends
name|ExecuteStatementOperation
block|{
specifier|private
name|Driver
name|driver
init|=
literal|null
decl_stmt|;
specifier|private
name|CommandProcessorResponse
name|response
decl_stmt|;
specifier|private
name|TableSchema
name|resultSchema
init|=
literal|null
decl_stmt|;
specifier|private
name|Schema
name|mResultSchema
init|=
literal|null
decl_stmt|;
specifier|private
name|SerDe
name|serde
init|=
literal|null
decl_stmt|;
specifier|public
name|SQLOperation
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
parameter_list|)
block|{
comment|// TODO: call setRemoteUser in ExecuteStatementOperation or higher.
name|super
argument_list|(
name|parentSession
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|prepare
parameter_list|()
throws|throws
name|HiveSQLException
block|{   }
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
name|String
name|statement_trimmed
init|=
name|statement
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|statement_trimmed
operator|.
name|split
argument_list|(
literal|"\\s"
argument_list|)
decl_stmt|;
name|String
name|cmd_1
init|=
name|statement_trimmed
operator|.
name|substring
argument_list|(
name|tokens
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
name|String
name|errorMessage
init|=
literal|""
decl_stmt|;
name|String
name|SQLState
init|=
literal|null
decl_stmt|;
try|try
block|{
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|getParentSession
argument_list|()
operator|.
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
comment|// In Hive server mode, we are not able to retry in the FetchTask
comment|// case, when calling fetch queries since execute() has returned.
comment|// For now, we disable the test attempts.
name|driver
operator|.
name|setTryCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|String
name|subStatement
init|=
operator|new
name|VariableSubstitution
argument_list|()
operator|.
name|substitute
argument_list|(
name|getParentSession
argument_list|()
operator|.
name|getHiveConf
argument_list|()
argument_list|,
name|statement
argument_list|)
decl_stmt|;
name|response
operator|=
name|driver
operator|.
name|run
argument_list|(
name|subStatement
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|response
operator|.
name|getResponseCode
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error while processing statement: "
operator|+
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|response
operator|.
name|getSQLState
argument_list|()
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
throw|;
block|}
name|mResultSchema
operator|=
name|driver
operator|.
name|getSchema
argument_list|()
expr_stmt|;
if|if
condition|(
name|mResultSchema
operator|!=
literal|null
operator|&&
name|mResultSchema
operator|.
name|isSetFieldSchemas
argument_list|()
condition|)
block|{
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|mResultSchema
argument_list|)
expr_stmt|;
name|setHasResultSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setHasResultSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveSQLException
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
name|e
throw|;
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
literal|"Error running query: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|setState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|CANCELED
argument_list|)
expr_stmt|;
if|if
condition|(
name|driver
operator|!=
literal|null
condition|)
block|{
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
name|driver
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
name|SessionState
name|session
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|session
operator|.
name|getTmpOutputFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|session
operator|.
name|getTmpOutputFile
argument_list|()
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
if|if
condition|(
name|driver
operator|!=
literal|null
condition|)
block|{
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
name|driver
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
name|SessionState
name|session
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|session
operator|.
name|getTmpOutputFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|session
operator|.
name|getTmpOutputFile
argument_list|()
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
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
if|if
condition|(
name|resultSchema
operator|==
literal|null
condition|)
block|{
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|driver
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|resultSchema
return|;
block|}
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
name|ArrayList
argument_list|<
name|String
argument_list|>
name|rows
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|setMaxRows
argument_list|(
operator|(
name|int
operator|)
name|maxRows
argument_list|)
expr_stmt|;
try|try
block|{
name|driver
operator|.
name|getResults
argument_list|(
name|rows
argument_list|)
expr_stmt|;
name|getSerDe
argument_list|()
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|RowSet
name|rowSet
init|=
operator|new
name|RowSet
argument_list|()
decl_stmt|;
name|Object
index|[]
name|deserializedFields
init|=
operator|new
name|Object
index|[
name|fieldRefs
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|Object
name|rowObj
decl_stmt|;
name|ObjectInspector
name|fieldOI
decl_stmt|;
for|for
control|(
name|String
name|rowString
range|:
name|rows
control|)
block|{
name|rowObj
operator|=
name|serde
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
name|rowString
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StructField
name|fieldRef
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|fieldOI
operator|=
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|deserializedFields
index|[
name|i
index|]
operator|=
name|convertLazyToJava
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|rowObj
argument_list|,
name|fieldRef
argument_list|)
argument_list|,
name|fieldOI
argument_list|)
expr_stmt|;
block|}
name|rowSet
operator|.
name|addRow
argument_list|(
name|resultSchema
argument_list|,
name|deserializedFields
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
return|;
block|}
catch|catch
parameter_list|(
name|IOException
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
catch|catch
parameter_list|(
name|CommandNeedRetryException
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
catch|catch
parameter_list|(
name|Exception
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
comment|/**    * Convert a LazyObject to a standard Java object in compliance with JDBC 3.0 (see JDBC 3.0    * Specification, Table B-3: Mapping from JDBC Types to Java Object Types).    *    * This method is kept consistent with {@link HiveResultSetMetaData#hiveTypeToSqlType}.    */
specifier|private
specifier|static
name|Object
name|convertLazyToJava
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
name|Object
name|obj
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|o
argument_list|,
name|oi
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
decl_stmt|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|oi
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
argument_list|)
condition|)
block|{
return|return
operator|new
name|String
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|obj
argument_list|)
return|;
block|}
comment|// for now, expose non-primitive as a string
comment|// TODO: expose non-primitive as a structured object while maintaining JDBC compliance
if|if
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
return|return
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
return|;
block|}
return|return
name|obj
return|;
block|}
specifier|private
name|SerDe
name|getSerDe
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|serde
operator|!=
literal|null
condition|)
block|{
return|return
name|serde
return|;
block|}
try|try
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
init|=
name|mResultSchema
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|StringBuilder
name|namesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|typesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldSchemas
operator|!=
literal|null
operator|&&
operator|!
name|fieldSchemas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|fieldSchemas
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
block|{
name|namesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|columnNames
operator|.
name|add
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|namesSb
operator|.
name|append
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|names
init|=
name|namesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|types
init|=
name|typesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|serde
operator|=
operator|new
name|LazySimpleSerDe
argument_list|()
expr_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Column names: "
operator|+
name|names
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|names
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|types
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Column types: "
operator|+
name|types
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|types
argument_list|)
expr_stmt|;
block|}
name|serde
operator|.
name|initialize
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|,
name|props
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not create ResultSet: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|serde
return|;
block|}
block|}
end_class

end_unit

