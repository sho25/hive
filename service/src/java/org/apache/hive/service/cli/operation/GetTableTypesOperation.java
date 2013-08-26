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
name|TableType
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
comment|/**  * GetTableTypesOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|GetTableTypesOperation
extends|extends
name|MetadataOperation
block|{
specifier|protected
specifier|static
name|TableSchema
name|RESULT_SET_SCHEMA
init|=
operator|new
name|TableSchema
argument_list|()
operator|.
name|addStringColumn
argument_list|(
literal|"TABLE_TYPE"
argument_list|,
literal|"Table type name."
argument_list|)
decl_stmt|;
specifier|private
name|RowSet
name|rowSet
decl_stmt|;
specifier|private
specifier|final
name|TableTypeMapping
name|tableTypeMapping
decl_stmt|;
specifier|protected
name|GetTableTypesOperation
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
name|GET_TABLE_TYPES
argument_list|)
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
name|rowSet
operator|=
operator|new
name|RowSet
argument_list|()
expr_stmt|;
for|for
control|(
name|TableType
name|type
range|:
name|TableType
operator|.
name|values
argument_list|()
control|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
name|RESULT_SET_SCHEMA
argument_list|,
operator|new
name|String
index|[]
block|{
name|tableTypeMapping
operator|.
name|mapToClientType
argument_list|(
name|type
operator|.
name|toString
argument_list|()
argument_list|)
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

